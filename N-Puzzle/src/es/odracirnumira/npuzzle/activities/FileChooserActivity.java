package es.odracirnumira.npuzzle.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.adapters.FileListChooserAdapter;
import es.odracirnumira.npuzzle.util.ListUtilities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity for picking up files (or directories) from the file system. This activity displays the
 * content of a directory of the file system, and lets the user navigate it.
 * <p>
 * This activity can be configured to select either files, or directories or both, and also can be
 * configured to select only one or multiple items. Use the input extras
 * {@link #INPUT_SELECTION_MODE} and {@link #INPUT_TYPE_MODE} to set these parameters (these extras
 * must be defined before starting the activity, otherwise an exception is thrown). The initial
 * directory that is displayed by the activity can be set via the intent extra
 * {@link #INPUT_INTIAL_DIRECTORY}.
 * <p>
 * For each file or directory, this activity displays its name, size (if it is a file), and an icon.
 * If the file is an image, the activity also displays its thumbnail.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class FileChooserActivity extends Activity {
	/**
	 * Input extra that defines the number of elements to be selected. Its value can be:
	 * 
	 * <ul>
	 * <li>{@link #MODE_SELECTION_MULTIPLE}.
	 * <li>{@link #MODE_SELECTION_SINGLE}.
	 * </ul>
	 */
	public static final String INPUT_SELECTION_MODE = "InputSelectionMode";

	/**
	 * Input extra that defines the type of element to be selected. Its value can be:
	 * 
	 * <ul>
	 * <li>{@link #MODE_TYPE_FILE}.
	 * <li>{@link #MODE_TYPE_DIRECTORY}.
	 * <li>{@link #MODE_TYPE_FILE_AND_DIRECTORY}.
	 * </ul>
	 */
	public static final String INPUT_TYPE_MODE = "InputTypeMode";

	/**
	 * Input extra that defines the initial directory that is displayed by the activity. Its value
	 * should be a valid path on the file system. Otherwise, an empty list is displayed. If not
	 * specified or null, the root of the file system is taken.
	 */
	public static final String INPUT_INTIAL_DIRECTORY = "InitialDirectory";

	/**
	 * Key for the output extra that defines the list of selected files. The result is an
	 * {@link ArrayList} of String that contains the set of selected files. If no file is selected,
	 * this output is not defined.
	 */
	public static final String KEY_RESULT_SELECTED_FILES = "SelectedFiles";

	/**
	 * Mode that indicates that only one file or directory can be selected by the activity.
	 */
	public static final String MODE_SELECTION_SINGLE = FileListChooserAdapter.SelectionMode.SINGLE
			.name();

	/**
	 * Mode that indicates that multiple files or directories can be selected by the activity.
	 */
	public static final String MODE_SELECTION_MULTIPLE = FileListChooserAdapter.SelectionMode.MULTIPLE
			.name();

	/**
	 * Mode that indicates that this activity can only be used to select files, not directories.
	 */
	public static final String MODE_TYPE_FILE = FileListChooserAdapter.TypeMode.FILE.name();

	/**
	 * Mode that indicates that this activity can only be used to select directories, not files.
	 */
	public static final String MODE_TYPE_DIRECTORY = FileListChooserAdapter.TypeMode.DIRECTORY
			.name();

	/**
	 * Mode that indicates that this activity can be used to selected both files and directories.
	 */
	public static final String MODE_TYPE_FILE_AND_DIRECTORY = FileListChooserAdapter.TypeMode.FILE_AND_DIRECTORY
			.name();

	/**
	 * ListView used to display files and directories.
	 */
	private ListView listView;

	/**
	 * Button that finishes the activity and returns the selected files.
	 */
	private Button okButton;

	/**
	 * Button that cancels the activity without returning files.
	 */
	private Button cancelButton;

	/**
	 * TextView that displays the name of the current directory.
	 */
	private TextView currentDirectoryTextView;

	/**
	 * The HorizontalScrollView that hosts the label with the current directory.
	 */
	private HorizontalScrollView currentDirectoryHorizontalScrollView;

	/**
	 * Selection mode used by the activity (single or multiple).
	 */
	private FileListChooserAdapter.SelectionMode selectionMode;

	/**
	 * Type mode used by the activity (files, directories, or files and directories).
	 */
	private FileListChooserAdapter.TypeMode typeMode;

	/**
	 * Initial directory passed to the activity via the {@link #INPUT_INTIAL_DIRECTORY} paramter.
	 */
	private String initialDirectory;

	/**
	 * Current directory being displayed by the activity.
	 */
	private String currentDirectory;

	/**
	 * Adapter that displays the set of files.
	 */
	private FileListChooserAdapter adapter;

	/**
	 * Set of selected files. Used by {@link #adapter} to display the selected files.
	 */
	private HashSet<File> selectedFiles;

	/**
	 * List that contains the files in the current directory. Used by {@link #adapter} to display
	 * the set of files in the current directory.
	 */
	private List<File> currentDirectoryFiles;

	/**
	 * Broadcast receiver used for listening to the event that a SD card has been mounted or
	 * unmounted. This is used to update the action bar.
	 */
	private SDCardMountedBroadcastReceiver sdcardMountedBroadcastReceiver;

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Check all input paramters. They must be defined.
		 */
		try {
			this.selectionMode = FileListChooserAdapter.SelectionMode.valueOf(getIntent()
					.getStringExtra(INPUT_SELECTION_MODE));
		} catch (Exception e) {
			throw new RuntimeException("Must specify a valid selection mode");
		}

		try {
			this.typeMode = FileListChooserAdapter.TypeMode.valueOf(getIntent().getStringExtra(
					INPUT_TYPE_MODE));
		} catch (Exception e) {
			throw new RuntimeException("Must specify a valid type mode");
		}

		this.initialDirectory = getIntent().getStringExtra(INPUT_INTIAL_DIRECTORY);

		if (this.initialDirectory == null) {
			this.initialDirectory = "/";
		}

		this.selectedFiles = new HashSet<File>();
		this.currentDirectoryFiles = new ArrayList<File>();

		this.setContentView(R.layout.file_chooser_activity);
		this.currentDirectoryTextView = (TextView) findViewById(R.id.currentDirectoryTextView);

		/*
		 * Restore previous state if available.
		 */
		if (savedInstanceState != null) {
			for (String fileName : savedInstanceState.getStringArrayList("selectedFiles")) {
				this.selectedFiles.add(new File(fileName));
			}

			this.currentDirectory = savedInstanceState.getString("currentDirectory");
		}

		/*
		 * Set the current directory if this is the first time the activity is created.
		 */
		if (this.currentDirectory == null) {
			this.currentDirectory = this.initialDirectory;
		}

		/*
		 * List empty view.
		 */
		this.listView = (ListView) findViewById(R.id.fileList);

		TextView emptyView = new TextView(this);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setText(R.string.empty_directory);
		emptyView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		((ViewGroup) this.listView.getParent()).addView(emptyView);
		this.listView.setEmptyView(emptyView);

		/*
		 * Initialize adapter and initialize view with the files of the current directory.
		 */
		this.currentDirectoryHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.currentDirectoryHorizontalScrollView);
		this.currentDirectoryFiles = new ArrayList<File>();
		this.adapter = new FileListChooserAdapter(this.currentDirectoryFiles, this.selectedFiles,
				this.selectionMode, this.typeMode);
		this.changeDirectory(this.currentDirectory, null, false);

		this.listView.setAdapter(this.adapter);
		this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				File clickedFile = (File) adapter.getItem(position);

				if (clickedFile.isDirectory()) {
					changeDirectory(clickedFile.getAbsolutePath(), null, false);
				} else if (clickedFile.isFile()) {
					/*
					 * If it is a clicked file and the mode is SINGLE FILE, return the selected
					 * file.
					 */
					if (typeMode == FileListChooserAdapter.TypeMode.FILE
							&& selectionMode == FileListChooserAdapter.SelectionMode.SINGLE) {
						Intent data = new Intent();
						ArrayList<String> result = new ArrayList<String>();
						result.add(clickedFile.getAbsolutePath());
						data.putStringArrayListExtra(KEY_RESULT_SELECTED_FILES, result);
						setResult(Activity.RESULT_OK, data);
						finish();
					} else if ((typeMode == FileListChooserAdapter.TypeMode.FILE || typeMode == FileListChooserAdapter.TypeMode.FILE_AND_DIRECTORY)
							&& (selectionMode == FileListChooserAdapter.SelectionMode.MULTIPLE)) {
						/*
						 * If this is a file with a check box (MULTIPLE FILE or MULTIPLE FILE AND
						 * DIRECTORY), we select or deselect the file.
						 */
						if (selectedFiles.contains(clickedFile)) {
							selectedFiles.remove(clickedFile);
						} else {
							selectedFiles.add(clickedFile);
						}

						adapter.notifyDataSetChanged();
					}
				}
			}
		});

		/*
		 * Scroll listener that updates the scrolling state of the adapter. This is required if we
		 * want the adapter to compute files' thumbnails only when the view is not scrolling.
		 */
		this.listView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					adapter.setScrolling(false);
				}
				else{
					adapter.setScrolling(true);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {

			}
		});

		/*
		 * Ok and cancel buttons listeners.
		 */
		this.okButton = (Button) findViewById(R.id.okButton);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);

		/*
		 * If there are selected files, put them into the result data. Then finish the activity.
		 */
		this.okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (selectedFiles.size() != 0) {
					Intent data = new Intent();

					data.putStringArrayListExtra(KEY_RESULT_SELECTED_FILES,
							selectedFilesArrayList());
					setResult(Activity.RESULT_OK, data);
				}

				finish();
			}
		});

		/*
		 * Cancel button, just closes the activity.
		 */
		this.cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		/*
		 * Hide OK button depending on the selected modes.
		 */
		if (this.typeMode == FileListChooserAdapter.TypeMode.FILE
				&& this.selectionMode == FileListChooserAdapter.SelectionMode.SINGLE) {
			this.okButton.setVisibility(View.GONE);
		}

		this.sdcardMountedBroadcastReceiver = new SDCardMountedBroadcastReceiver();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();

		/*
		 * We invalidate the action bar because the SD card may have been mounted or removed while
		 * we were not active.
		 */
		this.invalidateOptionsMenu();

		/*
		 * Register the broadcast receiver for SD card mount/unmount events.
		 */
		IntentFilter filter = new IntentFilter();
		filter.addDataScheme("file");
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

		this.registerReceiver(this.sdcardMountedBroadcastReceiver, filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();

		/*
		 * Unregister broadcast receiver.
		 */
		this.unregisterReceiver(this.sdcardMountedBroadcastReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	public void onBackPressed() {
		/*
		 * If we are at the root directory, finish the activity.
		 */
		if (this.currentDirectory.equals("/")) {
			finish();
		} else {
			/*
			 * Otherwise, move back one directory.
			 */
			moveUp();
		}
	}

	/**
	 * Moves the current directory one step up in the directories hierarchy.
	 */
	private void moveUp() {
		/*
		 * Move one directory up in the hierarchy and refresh the view.
		 */
		String parent = new File(this.currentDirectory).getParent();

		if (parent != null) {
			this.changeDirectory(parent, this.currentDirectory, true);
		}
	}

	/**
	 * Changes the directory that is being displayed by the activity.
	 * 
	 * @param newDirectory
	 *            the new directory to display.
	 * @param centerDirectory
	 *            used if <code>centerOnDirectory</code> is true. May be null otherwise. If
	 *            <code>newDirectory</code> contains this directory, and
	 *            <code>centerOnDirectory</code> is true, then the view's upper element will be this
	 *            directory.
	 * @param centerOnDirectory
	 *            see <code>centerDirectory</code>.
	 */
	private void changeDirectory(String newDirectory, String centerDirectory,
			boolean centerOnDirectory) {
		this.currentDirectory = newDirectory;

		File[] files = new File(this.currentDirectory).listFiles();

		/*
		 * Clear the list of files first.
		 */
		this.currentDirectoryFiles.clear();

		/*
		 * Change the directory label. We scroll the text view to the rightmost end.
		 */
		this.currentDirectoryTextView.setText(newDirectory);
		/*
		 * Must post this scroll. Otherwise we will scroll to the current directory, not the new
		 * directory (that is, we must wait for a new layout pass to complete before scrolling,
		 * otherwise we will not be scrolling to the right position).
		 */
		this.currentDirectoryHorizontalScrollView.post(new Runnable() {
			public void run() {
				currentDirectoryHorizontalScrollView.fullScroll(View.FOCUS_RIGHT);
			}
		});

		/*
		 * If the new directory could be accessed...
		 */
		if (files != null) {
			ListUtilities.merge(currentDirectoryFiles, files);
			Collections.sort(currentDirectoryFiles, new FileComparator());

			/*
			 * If we must center the list view on the current directory...
			 */
			if (centerOnDirectory && currentDirectoryFiles.size() != 0) {
				int position;

				String dirName = new File(centerDirectory).getAbsolutePath();

				for (position = 0; position < currentDirectoryFiles.size(); position++) {
					if (dirName.equals(currentDirectoryFiles.get(position).getAbsolutePath())) {
						break;
					}
				}

				if (position == currentDirectoryFiles.size()) {
					position = 0;
				}

				/*
				 * Note this notification to the adapter must go before setting the list view
				 * position.
				 */
				adapter.notifyDataSetChanged();
				this.listView.setSelectionFromTop(position, 0);
			} else {
				/*
				 * Otherwise, move list to the top.
				 */
				adapter.notifyDataSetChanged();
				listView.setSelectionFromTop(0, 0);
			}
		} else {
			/*
			 * If there is any error accessing the parent directory, just show it as an empty
			 * directory.
			 */
			adapter.notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.file_chooser_activity_menu, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (this.selectionMode == FileListChooserAdapter.SelectionMode.SINGLE) {
			menu.findItem(R.id.menuItemSelectAll).setVisible(false);
			menu.findItem(R.id.menuItemUnselectAll).setVisible(false);
			menu.findItem(R.id.menuItemUnselectAllFromDirectory).setVisible(false);
		} else {
			menu.findItem(R.id.menuItemSelectAll).setVisible(true);
			menu.findItem(R.id.menuItemUnselectAll).setVisible(true);
			menu.findItem(R.id.menuItemUnselectAllFromDirectory).setVisible(true);
		}

		String externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();

		if (externalDir != null) {
			menu.findItem(R.id.menuItemGoToExternalDir).setVisible(true);
		} else {
			menu.findItem(R.id.menuItemGoToExternalDir).setVisible(false);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuItemBack:
				this.moveUp();
				return true;
			case R.id.menuItemSelectAll:
				this.selectAll();
				return true;
			case R.id.menuItemUnselectAll:
				this.unselectAll();
				return true;
			case R.id.menuItemUnselectAllFromDirectory:
				this.unselectAllFromDirectory();
				return true;
			case R.id.menuItemGoToRoot:
				this.changeDirectory("/", null, false);
				return true;
			case R.id.menuItemGoToExternalDir:
				File externalDir = Environment.getExternalStorageDirectory();

				if (externalDir != null) {
					this.changeDirectory(externalDir.getAbsolutePath(), null, false);
				}

				return true;
		}

		return false;
	}

	/**
	 * Selects all the selectable items from the current directory.
	 */
	private void selectAll() {
		if (this.selectionMode == FileListChooserAdapter.SelectionMode.MULTIPLE) {
			int originalSize = this.selectedFiles.size();

			if (this.typeMode == FileListChooserAdapter.TypeMode.FILE) {
				for (File f : this.currentDirectoryFiles) {
					if (f.isFile()) {
						this.selectedFiles.add(f);
					}
				}
			} else if (this.typeMode == FileListChooserAdapter.TypeMode.DIRECTORY) {
				for (File f : this.currentDirectoryFiles) {
					if (f.isDirectory()) {
						this.selectedFiles.add(f);
					}
				}
			} else if (this.typeMode == FileListChooserAdapter.TypeMode.FILE_AND_DIRECTORY) {
				this.selectedFiles.addAll(currentDirectoryFiles);
			}

			if (this.selectedFiles.size() != originalSize) {
				this.adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * Unselects all the selected items from all directories.
	 */
	private void unselectAll() {
		int originalSize = this.selectedFiles.size();
		this.selectedFiles.clear();

		if (this.selectedFiles.size() != originalSize) {
			this.adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Unselects all the selected items from the current directory.
	 */
	private void unselectAllFromDirectory() {
		int originalSize = this.selectedFiles.size();
		this.selectedFiles.removeAll(this.currentDirectoryFiles);

		if (this.selectedFiles.size() != originalSize) {
			this.adapter.notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state.getBundle("superState"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	public void onSaveInstanceState(Bundle outState) {
		Bundle superState = new Bundle();

		super.onSaveInstanceState(superState);

		outState.putBundle("superState", superState);

		ArrayList<String> selectedFileNames = new ArrayList<String>();

		for (File file : this.selectedFiles) {
			selectedFileNames.add(file.getAbsolutePath());
		}

		outState.putStringArrayList("selectedFiles", selectedFileNames);
		outState.putString("currentDirectory", this.currentDirectory);
	}

	/**
	 * To sort files. First go directories, then files. They are lexically sorted.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class FileComparator implements Comparator<File> {
		public int compare(File object1, File object2) {
			if (object1.isDirectory() && !object2.isDirectory()) {
				return -1;
			} else if (!object1.isDirectory() && object2.isDirectory()) {
				return 1;
			} else {
				return object1.getName().compareTo(object2.getName());
			}
		}
	}

	/**
	 * Returns an array that contains the set of selected files.
	 */
	private ArrayList<String> selectedFilesArrayList() {
		ArrayList<String> result = new ArrayList<String>();

		for (File f : this.selectedFiles) {
			result.add(f.getAbsolutePath());
		}

		return result;
	}

	/**
	 * Broadcast receiver that is notified when the external storage area is mounted or removed.
	 * This broadcast receiver is used for updating the menu as new media storage devices are
	 * mounted or removed from the file system.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class SDCardMountedBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// SD card has been mounted
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				invalidateOptionsMenu();
			} else {
				// SD card has been unmounted
				if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_REMOVED)
						|| action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED)
						|| action.equalsIgnoreCase(Intent.ACTION_MEDIA_BAD_REMOVAL)
						|| action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
					invalidateOptionsMenu();
				}
			}
		}
	}
}
