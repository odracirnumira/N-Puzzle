package es.odracirnumira.npuzzle.adapters;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.util.ImageUtilities;
import es.odracirnumira.npuzzle.util.MathUtilities;
import es.odracirnumira.npuzzle.util.UIUtilities;
import es.odracirnumira.npuzzle.util.cache.BitmapMemoryLimitedCache;
import es.odracirnumira.npuzzle.util.cache.CacheUtils;
import es.odracirnumira.npuzzle.util.cache.ICache;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter specialized in managing a list of files ({@link File}, which may be a file or a
 * directory) to be displayed in an activity where the used can pick one or several files and/or
 * directories.
 * <p>
 * For each file, this adapter returns a view with an icon, its name and size. If possible, a
 * thumbnail for the file is created (this happens for instance for image files). Thumbnails are
 * loaded automatically as long as the view associated with this adapter is not scrolling. The
 * adapter has to be reported when the view stops scrolling. In order to do so, just call the
 * {@link #setScrolling(boolean)} method when the scrolling state of the view changes.
 * <p>
 * This adapter offers multiple configuration options, depending on how the file picking system is
 * supposed to work.
 * <p>
 * Use {@link SelectionMode} to determine how much items (either files or directories) can be
 * selected. Use {@link TypeMode} to determine what can be selected (either files, or directories or
 * both). If only one element can be chosen and it is a directory, then directory views will have a
 * checkbox next to them so they can be selected. If multiple items can be selected, then whatever
 * can be selected (files and/or directories) will have a checkbox next to them so they can be
 * selected.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class FileListChooserAdapter extends BaseAdapter {
	/**
	 * The selection mode. This determines how many items can be selected from the list of files and
	 * directories.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public enum SelectionMode {
		SINGLE, MULTIPLE
	}

	/**
	 * The type of element that can be selected.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public enum TypeMode {
		/**
		 * Only files can be selected.
		 */
		FILE,
		/**
		 * Only directories can be selected.
		 */
		DIRECTORY,
		/**
		 * Both files and directories can be selected.
		 */
		FILE_AND_DIRECTORY
	}

	/**
	 * The list of {@link File}s that are being managed by the adapter.
	 */
	private List<File> files;

	/**
	 * A collection that contains all the selected files.
	 */
	private Collection<File> selectedFiles;

	/**
	 * The selection mode.
	 */
	private SelectionMode selectionMode;

	/**
	 * The type mode.
	 */
	private TypeMode typeMode;

	/**
	 * The cache that stores images thumbnails.
	 */
	private volatile ICache<File, Bitmap> thumbnailsCache;

	/**
	 * The files whose thumbnails are being loaded on the background thread. This is used to avoid
	 * relaunching the task that loads the thumbnail if it is already being loaded.
	 */
	private Set<File> thumbnailsBeingLoaded;

	/**
	 * Lock for {@link #thumbnailsBeingLoaded}, since it can be accessed from multiple threads.
	 */
	private Object thumbnailsBeingLoadedLock = new Object();

	/**
	 * A map that stores icons for each file extension. By having this map, we do not need to
	 * compute the icon for extensions we already have retrieved an icon for.
	 */
	private Map<String, Drawable> fileIcons;

	/**
	 * The executor service that is in charge of loading image thumbnails. This is just a
	 * single-threaded executor.
	 */
	private ScheduledExecutorService thumbnailsExecutorService;

	/**
	 * Maximum width for thumbnails, in pixels.
	 */
	private int thumbnailMaxWidth = 72;

	/**
	 * Maximum height for thumbnails, in pixels.
	 */
	private int thumbnailMaxHeight = 72;

	/**
	 * Flag that tells if the view that this adapter is providing data to, is scrolling or not. If
	 * it is scrolling, the adapter will not compute the visible files' thumbnails.
	 */
	private boolean scrolling;

	/**
	 * Padding for each returned view.
	 */
	private static final int ITEM_PADDING = (int) UIUtilities.convertDpToPixel(NPuzzleApplication
			.getApplication().getResources().getDimension(R.dimen.list_item_default_padding),
			NPuzzleApplication.getApplication());

	/**
	 * Handler that is notified when a new image is available, so the
	 * {@link #notifyDataSetChanged()} is called on the UI thread.
	 */
	private Handler thumbnailLoadedHandler = new Handler() {
		public void handleMessage(Message message) {
			notifyDataSetChanged();
		}
	};

	/**
	 * Constructor.
	 * 
	 * @param files
	 *            the list of {@link File}s the adapter will manage.
	 * @param selectedFiles
	 *            the set of initially selected files. This set is updated as files are selected and
	 *            unselected, so the user can keep track of the current selection be querying the
	 *            collection.
	 * @param selectionMode
	 *            the selection mode.
	 * @param typeMode
	 *            the type mode.
	 */
	public FileListChooserAdapter(List<File> files, Collection<File> selectedFiles,
			SelectionMode selectionMode, TypeMode typeMode) {
		this.files = files;
		this.selectedFiles = selectedFiles;
		this.selectionMode = selectionMode;
		this.typeMode = typeMode;

		int cacheSize = ((ActivityManager) NPuzzleApplication.getApplication().getSystemService(
				Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024 / 8;

		this.thumbnailsCache = CacheUtils.getSynchronizedCache(new BitmapMemoryLimitedCache<File>(
				cacheSize));
		this.fileIcons = new HashMap<String, Drawable>();
		this.thumbnailsBeingLoaded = new HashSet<File>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return this.files.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		return this.files.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		File file = (File) getItem(position);

		// Reuse convertView if possible
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.checkable_list_item_2, parent, false);

			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
			holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		/*
		 * If we are reusing the convert view we must set some parts to visible state, since they
		 * may have been set gone in previous iterations.
		 */
		if (convertView != null) {
			holder.text2.setVisibility(View.VISIBLE);
			holder.checkBox.setVisibility(View.VISIBLE);
		}

		boolean isFile = file.isFile();
		boolean isDirectory = file.isDirectory();

		// Hide checkbox if necessary
		boolean hideCheckBox = false;

		if (selectionMode == SelectionMode.SINGLE) {
			if (typeMode == TypeMode.FILE) {
				hideCheckBox = true;
			} else if (typeMode == TypeMode.DIRECTORY) {
				if (isFile || !isDirectory) {
					hideCheckBox = true;
				}
			}
		} else if (selectionMode == SelectionMode.MULTIPLE) {
			if (typeMode == TypeMode.FILE && (isDirectory || !isFile)) {
				hideCheckBox = true;
			} else if (typeMode == TypeMode.DIRECTORY && (isFile || !isDirectory)) {
				hideCheckBox = true;
			} else if (typeMode == TypeMode.FILE_AND_DIRECTORY && (!isFile || !isDirectory)) {
				hideCheckBox = true;
			}
		}

		holder.checkBox.setTag(file);

		if (hideCheckBox) {
			holder.checkBox.setVisibility(View.GONE);
		} else {
			holder.checkBox
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							File file = (File) buttonView.getTag();

							if (isChecked) {
								/*
								 * If we have checked the entry, we must add the file to the list of
								 * selected files. However, if we are in SINGLE mode, we should
								 * uncheck whatever entries were checked, which is what we do in the
								 * next if statement.
								 */
								if (selectionMode == SelectionMode.SINGLE) {
									/*
									 * IMPORTANT: due to the way that ListView works we have to do
									 * some funny stuff. ListView does not necessarily keep all its
									 * items in its list of children. Only visible items are
									 * guaranteed to be accessible. In a normal scenario, we would
									 * examine all of its children, get the one that is checked,
									 * uncheck it, and finally remove its associated file
									 * (accessible via the tag property) from the list of selected
									 * files.
									 * 
									 * However, since we cannot expect all of its children to be
									 * accessible, we must clear the list of selected files here,
									 * since we may not find the selected entry in the list of
									 * children of the ListView. Clearing the list of selected files
									 * is not a problem since we know it can have only one element,
									 * the one we are precisely trying to remove.
									 * 
									 * After clearing the list, we examine the list's children. If
									 * one of them is the checked item, we uncheck it. As stated
									 * above, we may not find such child, in which case, the next
									 * time it is created it will be in an unchecked state since the
									 * corresponding file will not be in the list of selected files.
									 */
									selectedFiles.clear();

									for (int i = 0; i < parent.getChildCount(); i++) {
										View child = parent.getChildAt(i);

										CheckBox itemCheckBox = (CheckBox) child
												.findViewById(R.id.checkBox);

										if (itemCheckBox != null && itemCheckBox != buttonView) {
											if (itemCheckBox.isChecked()) {
												itemCheckBox.setChecked(false);
												break;
											}
										}
									}
								}

								/*
								 * Add selected file.
								 */
								selectedFiles.add(file);
							} else {
								/*
								 * If we unchecked the entry, remove it from the list of selected
								 * files.
								 */
								selectedFiles.remove(file);
							}
						}
					});

			/*
			 * Check the entry according to the stored state. NOTE this has to go before setting the
			 * listener, since otherwise the listener that the convertView stored (which is not the
			 * one we want to use) will be used, causing unintended behavior.
			 */
			if (this.selectedFiles.contains(file)) {
				holder.checkBox.setChecked(true);
			} else {
				holder.checkBox.setChecked(false);
			}
		}

		// File name
		holder.text1.setText(file.getName());

		// File size in bytes
		if (isFile || !isDirectory) {
			holder.text2.setText(MathUtilities.fromByteSizeToStringSize(file.length()));
		} else {
			holder.text2.setVisibility(View.GONE);
		}

		// ICON

		/*
		 * Icon. If this is a directory, just set our custom directory icon. Otherwise, if we have
		 * an icon for the file in the cache, set it. Otherwise, if this extension has a
		 * corresponding icon in the "fileIcons" cache, use that icon. Otherwise, if an appropriate
		 * application is found to open the file, show the application's icon. Finally, if none of
		 * the above is true, show a default icon.
		 * 
		 * If the file is an image, we will spawn a new task to load its icon, as long as we are
		 * allowed (we are not scrolling, etc.).
		 */
		if (isDirectory) {
			holder.icon.setImageResource(R.drawable.directory);
		} else if (isFile) {
			/*
			 * If this file thumbnail icon is in the cache, use it.
			 */
			Bitmap thumbnail = this.thumbnailsCache.get(file);

			if (thumbnail != null) {
				holder.icon.setImageBitmap(thumbnail);
			} else {
				this.thumbnailsCache.remove(file);

				Uri fileUri = Uri.fromFile(file);
				String fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
						.toLowerCase();
				String mimeType = MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(fileExtension);

				/*
				 * If we have a matching icon for this extension, use that icon.
				 */
				Drawable icon;
				if ((icon = this.fileIcons.get(fileExtension)) != null) {
					holder.icon.setImageDrawable(icon);
				} else {
					/*
					 * If no icon was found either, try to find a matching application for the file.
					 */
					PackageManager manager = parent.getContext().getPackageManager();

					Intent intent = new Intent(Intent.ACTION_VIEW);

					// intent.setData(fileUri);
					intent.setType(mimeType);
					List<ResolveInfo> matchInfo = manager.queryIntentActivities(intent, 0);

					if (matchInfo.size() != 0) {
						/*
						 * If a matching icon is found, show it.
						 */
						holder.icon.setImageDrawable(matchInfo.get(0).loadIcon(manager));
						if (fileExtension != null) {
							this.fileIcons.put(fileExtension, holder.icon.getDrawable());
						}
					} else {
						/*
						 * Otherwise, show a default icon.
						 */
						holder.icon.setImageResource(R.drawable.generic_file);
						if (fileExtension != null) {
							this.fileIcons.put(fileExtension, holder.icon.getDrawable());
						}
					}
				}

				/*
				 * If this file is an image and we are not already computing its thumbnail, try to
				 * load its thumbnail on a background thread.
				 */
				if (this.canLoadThumbnails() && mimeType != null && mimeType.startsWith("image/")) {
					synchronized (this.thumbnailsBeingLoadedLock) {
						if (!this.thumbnailsBeingLoaded.contains(file)) {
							this.thumbnailsBeingLoaded.add(file);
							this.loadImage(file, this.thumbnailMaxWidth, this.thumbnailMaxHeight);
						}
					}
				}
			}
		} else {
			holder.icon.setImageResource(R.drawable.generic_file);
		}

		convertView.setPadding(ITEM_PADDING, 0, ITEM_PADDING, 0);

		return convertView;
	}

	/**
	 * Sets if the view of this adapter is scrolling. This flag should be used so the adapter knows
	 * when it can compute the files' thumbnails.
	 * 
	 * @param scrolling
	 *            true if the view is scrolling, and false otherwise.
	 */
	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;

		/*
		 * We need to notify for changes, because by the time the scroll has stopped, the view has
		 * probably already drawn its children (without thumbnail). By notifying for changes, the
		 * views's thumbnails will be computed if necessary.
		 */
		if (!scrolling) {
			this.notifyDataSetChanged();
		}
	}

	/**
	 * Returns true if we can compute the thumbnails of the files that are currently visible.
	 */
	private boolean canLoadThumbnails() {
		return !this.scrolling;
	}

	/**
	 * Post a runnable into the {@link #thumbnailsExecutorService} to load the thumbnail of
	 * <code>f</code>, being <code>width</code> and <code>height</code> the maximum width and height
	 * for the returned thumbnail. If the thumbnail for the file is properly computed, the cache is
	 * updated and the handler is notified so the adapter is notified for changes.
	 */
	private void loadImage(final File f, final int width, final int height) {
		if (this.thumbnailsExecutorService == null) {
			/*
			 * Create executor whose thread is terminated after three seconds without an incoming
			 * task.
			 */
			ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
			executor.setKeepAliveTime(3, TimeUnit.SECONDS);
			executor.allowCoreThreadTimeOut(true);
			this.thumbnailsExecutorService = executor;
		}

		try {
			this.thumbnailsExecutorService.execute(new Runnable() {
				public void run() {
					Bitmap scaledBitmap = ImageUtilities.secureDecode(f.getAbsolutePath(), width,
							height);

					if (scaledBitmap != null) {
						thumbnailsCache.put(f, (scaledBitmap));
						thumbnailLoadedHandler.sendEmptyMessage(0);
					}

					/*
					 * Remove the file from the list of files whose thumbnail is being loaded.
					 */
					synchronized (thumbnailsBeingLoadedLock) {
						thumbnailsBeingLoaded.remove(f);
					}
				}
			});
		} catch (RejectedExecutionException e) {

		}
	}

	/**
	 * View holder for this adapter.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class ViewHolder {
		public TextView text1;
		public TextView text2;
		public CheckBox checkBox;
		public ImageView icon;
	}
}