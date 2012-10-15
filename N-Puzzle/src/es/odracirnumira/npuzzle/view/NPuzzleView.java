package es.odracirnumira.npuzzle.view;

import java.util.ArrayList;
import java.util.List;

import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.model.NPuzzle.Direction;
import es.odracirnumira.npuzzle.model.NPuzzle.ITileListener;
import es.odracirnumira.npuzzle.util.ImageUtilities;
import es.odracirnumira.npuzzle.util.MathUtilities;
import es.odracirnumira.npuzzle.util.UIUtilities;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/*
 * TODO: 
 * - make stylable:
 * 		border image
 * 
 * - add stylable option that makes the view does not show a default image even it no image has
 * been set by the user. In that case, a grey board is displayed 
 * 
 * - add stylable option that makes the tiles smooth. 
 * 
 * - If the image changes or rotates while a tile is moving or the user is dragging a tile, the tile animation
 * should stop.
 */
/**
 * A {@code View} that can be used to show an {@link NPuzzle} and interact with it. This view is
 * connected to the underlying {@code NPuzzle} as an observer.
 * <p>
 * When the puzzle is modified, the view is modified to display the new puzzle, by showing an
 * animation that moves the tile to the empty position. To control that the puzzle is not modified
 * until the animation ends, you can use the {@link INPuzzleViewListener} interface to register a
 * listener that is notified when the animation starts and ends (this restriction can be violated,
 * but in that case the current animation will be abruptly terminated).
 * <p>
 * The view can also be used to modify the underlying {@code NPuzzle}. When a tile is moved in the
 * view, the {@code NPuzzle} is modified so it reflects the change that has been made.
 * <p>
 * This class can be drawn even if no puzzle or image is set. However, in that case, an empty view
 * will be displayed.
 * <p>
 * To set an image, use the {@link #setImage(Bitmap)} method. You can use
 * {@link #createDefaultImage(int)} to create a default image for the puzzle, which shows numbered
 * tiles.
 * <p>
 * This is an styleable class. See the <i>NPuzzleView</i> styleable resource for more information.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleView extends View implements ITileListener {
	/**
	 * The N puzzle being represented.
	 */
	private NPuzzle puzzle;

	/**
	 * The list of listeners being reported about events that take place in the view.
	 */
	private List<INPuzzleViewListener> listeners;

	/**
	 * The gesture detector. Can only be used if there is a puzzle and an image set.
	 */
	private GestureDetector gestureDetector;

	/**
	 * The height of each tile of the board. It obviously takes into account the rotation (
	 * {@link #imageRotation}). Computed in {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int tileWidth;

	/**
	 * The width of each tile. It obviously takes into account the rotation ( {@link #imageRotation}
	 * ). Computed in {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int tileHeight;

	/**
	 * The original image must be resized to fit the view's dimensions. This is the width it will
	 * have once it is resized and rotated (according to {@link #imageRotation}). See
	 * {@link #fitWidth}. Computed in {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int resizedImageWidth;

	/**
	 * The original image must be resized to fit the view's dimensions. This is the height it will
	 * have once it is resized and rotated (according to {@link #imageRotation}). See
	 * {@link #fitWidth}. Computed in {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int resizedImageHeight;

	/**
	 * The original image must be resized to fit the view's dimensions. If {@link #fitWidth} is
	 * true, then this value is the gap there is between the top of the view and the first pixel of
	 * the image. We use this value to center the image within the view, and it also takes into
	 * account the puzzle frame and the padding. Computed in
	 * {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int heightGap;

	/**
	 * The original image must be resized to fit the view's dimensions. If {@link #fitWidth} is
	 * false, then this value is the gap there is between the left edge of the view and the first
	 * pixel of the image. We use this value to center the image within the view, and it also takes
	 * into account the puzzle frame and the padding. Computed in
	 * {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	private int widthGap;

	/**
	 * If "fitWidth" is true, it means that the image must be resized so its width matches the
	 * view's width (its height will be less or equal than that of the view). Otherwise, it is the
	 * height that must be resized to match the view's height. Computed in
	 * {@link #computeBoardPositionsAndTileDimensions()}.
	 */
	boolean fitWidth;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the time when the animation that moves
	 * the tile started.
	 */
	private long movingTileStartTime;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the time when the animation that moves
	 * the tile must end. It can be equal to {@link #movingTileStartTime} if the animation must end
	 * immediately.
	 */
	private long movingTileEndTime;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the tile that is being moved. Its value
	 * is -1 if no tile is being moved.
	 */
	private int movingTile;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the position that the tile occupied
	 * before being moved.
	 */
	private int movingPos;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the direction the tile is moving to.
	 */
	private Direction movingTileDirection;

	/**
	 * When a tile is being moved automatically because the {@code NPuzzle} changed or because the
	 * user stopped dragging a tile, this value represents the initial visual coordinates of the
	 * tile. This value ranges from 0 to 1, and represents how much of the total path it has already
	 * moved. For instance, if the tile is being moved because the model changed, its value will be
	 * 0, since the tile that will move is placed at the beginning of the path it has to go through.
	 * However, if the tile is being dragged by the user and, when it is dropped, the tile has gone
	 * through 60% of the path, this value would be 0.6.
	 */
	private float movingTileInitialPercentage;

	/**
	 * The image being displayed.
	 */
	private Bitmap image;

	/**
	 * Used during drawing.
	 */
	private Rect tileDestRectangle;

	/**
	 * Used during drawing.
	 */
	private Rect tileSrcRectangle;

	/**
	 * Used during drawing.
	 */
	private Point tileCoordinates;

	/**
	 * Paint object used for painting each tile's border.
	 */
	private Paint tileFramePaint;

	/**
	 * Length of the moving animation in milliseconds. This is the length of the whole animation, so
	 * if a tile is moving from an intermediate point, the length of the resulting animation should
	 * shrink.
	 */
	private long animationDuration;

	/**
	 * Touch events are only handled when this flag is true. During animations, touch events are
	 * disabled.
	 */
	private boolean handleTouchEvents;

	/**
	 * Tile that is being dragged. If no tile is being dragged, the value of this variable is -1.
	 */
	private int draggingTile;

	/**
	 * This value goes from 0 to 1, and represents how much of the path between the natural position
	 * of {@link #draggingTile} and the empty tile has been completed by the tile that is being
	 * dragged.
	 */
	private float draggingTilePercentage;

	/**
	 * This boolean value controls scroll events. Scroll events are problematic in the sense that if
	 * we start a scroll event, then release the finger, disable touch events, perform another
	 * scroll while touch events are disabled, and finally enable back touch events while we are
	 * still doing the second scroll, the gesture detector will think that the initial DOWN event
	 * that started the second scroll is the DOWN event that started the first scroll. Put it
	 * another way, when the user releases the finger, the DOWN event that is used in the onScroll()
	 * method of the gesture listener is not nullified, so whatever event was used will be reused
	 * unless it is replaced by another DOWN event.
	 * <p>
	 * To avoid this problem, we use this flag: whenever a DOWN event is detected, it is set to
	 * true, so the gesture detector will be able to handle scroll events. When a UP event is
	 * detected, the flag is set to false, so the gesture detector will not handle scroll events
	 * unless a new DOWN event is detected.
	 */
	private boolean handleScroll;

	/**
	 * Boolean flag that tells if the view has been assigned a final size. It is important to know
	 * this because only after a size has been assigned to the view we can determine where the board
	 * will be placed, it actual size, etc.
	 * <p>
	 * This flag is set in {@link #onMeasure(int, int)}.
	 */
	private boolean measured;

	/**
	 * Boolean flag that tells if the view has been assigned a final size and that size was computed
	 * with an image set (that is, the computed size is based on the image's size).
	 * <p>
	 * This flag is set on {@link #onMeasure(int, int)}.
	 */
	private boolean measuredWithImage;

	/**
	 * This flag is activated if we are using the default image.
	 */
	private boolean isDefaultImageBeingUsed;

	/**
	 * The rotation of the image. Defaults to 0. Can be 0, 90, 180 and 270.
	 */
	private int imageRotation;

	/**
	 * Rotation matrix used when drawing tiles.
	 */
	private Matrix tileRotationMatrix;

	/**
	 * Used in drawing.
	 */
	private RectF floatRect;

	/**
	 * The width of the tiles' border. This is measured in pixels.
	 */
	private int tileBorderWidth;

	/**
	 * The default value for {@link #animationDuration}.
	 */
	private static final int DEFAULT_ANIMATION_DURATION = 300;

	/**
	 * The default value for {@link #imageRotation}.
	 */
	private static final int DEFAULT_IMAGE_ROTATION = 0;

	/**
	 * The default width of the tiles' border. It is measured in DP.
	 */
	private static final int DEFAULT_TILE_BORDER_WIDTH = 1;

	/**
	 * The image that is drawn onto the canvas, that is, the board itself, must not be greater than
	 * the hardware limit. Otherwise, nothing will be drawn. This is the maximum width that we can
	 * safely assume. If the image's width is greater than this, we will scale it down.
	 * <p>
	 * Thus, it is recommended that the image's width be no greater than this, since otherwise the
	 * view will have to scale it down.
	 */
	public static final int IMAGE_MAX_WIDTH = 2048;

	/**
	 * The image that is drawn onto the canvas, that is, the board itself, must not be greater than
	 * the hardware limit. Otherwise, nothing will be drawn. This is the maximum height that we can
	 * safely assume. If the image's height is greater than this, we will scale it down.
	 * <p>
	 * Thus, it is recommended that the image's height be no greater than this, since otherwise the
	 * view will have to scale it down.
	 */
	public static final int IMAGE_MAX_HEIGHT = 2048;

	/**
	 * Interface for objects that want to be notified by the {@link NPuzzleView} when important
	 * events take place in the view.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface INPuzzleViewListener {
		/**
		 * Called when the animation that was taking place ends. When the model is modified, the
		 * {@code NPuzzleView} starts an animation that shows the corresponding tile moving to the
		 * position occupied by the empty tile. When such animation ends, this method is called.
		 * <p>
		 * The underlying {@code NPuzzle} can be modified as quick as possible. However, if the view
		 * shows a tile moving and the model is modified before the tile reaches its final position,
		 * that animation will be ended abruptly before starting the new one. If the user wants to
		 * avoid this behavior, he should not modify the model between calls of
		 * {@link #movingTileAnimationStarted()} and this method, that is, once this
		 * {@link #movingTileAnimationStarted()} is called, the user should modify the model only
		 * after this method is called.
		 */
		public void movingTileAnimationEnded();

		/**
		 * Called when an animation that will move a tile from its current position to the position
		 * of the empty tile starts. This is called for instance when the model is modified, or when
		 * the user presses a tile on the view and the tile starts moving as a result.
		 * <p>
		 * The underlying {@code NPuzzle} can be modified as quick as possible. However, if the view
		 * shows a tile moving and the model is modified before the tile reaches its final position,
		 * that animation will be ended abruptly before starting the new one. If the user wants to
		 * avoid this behavior, he should not modify the model between calls of this method and
		 * {@link #movingTileAnimationEnded()}, that is, once this method is called, the user should
		 * modify the model only after {@link #movingTileAnimationEnded()} is called.
		 */
		public void movingTileAnimationStarted();
	}

	public NPuzzleView(Context context) {
		super(context);
		this.initView(null);
	}

	public NPuzzleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initView(attrs);
	}

	/**
	 * Initializes the view.
	 * 
	 * @param attrs
	 *            the XML attributes. May be null if no attributes are specified.
	 */
	private void initView(AttributeSet attrs) {
		this.listeners = new ArrayList<INPuzzleViewListener>();
		this.tileDestRectangle = new Rect();
		this.tileSrcRectangle = new Rect();
		this.tileCoordinates = new Point();
		this.tileFramePaint = new Paint();
		this.tileFramePaint.setStyle(Style.FILL);
		this.tileFramePaint.setColor(Color.WHITE);
		this.tileBorderWidth = (int) UIUtilities.convertDpToPixel(DEFAULT_TILE_BORDER_WIDTH,
				getContext());
		this.tileFramePaint.setStrokeWidth(0);
		this.movingTile = -1;
		this.draggingTile = -1;
		this.setTileAnimationDuration(DEFAULT_ANIMATION_DURATION);
		this.handleTouchEvents = true;
		this.handleScroll = false;
		this.isDefaultImageBeingUsed = false;
		this.tileRotationMatrix = new Matrix();
		this.floatRect = new RectF();

		/*
		 * If attributes were specified, retrieve their values.
		 */
		if (attrs != null) {
			TypedArray a = NPuzzleApplication.getApplication().getTheme()
					.obtainStyledAttributes(attrs, R.styleable.NPuzzleView, 0, 0);

			this.setTileAnimationDuration(a.getInteger(
					R.styleable.NPuzzleView_tileAnimationDuration, DEFAULT_ANIMATION_DURATION));

			this.setImageRotation(a.getInteger(R.styleable.NPuzzleView_imageRotation,
					DEFAULT_IMAGE_ROTATION));

			a.recycle();
		}

		// Gesture detector
		this.gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
			public boolean onSingleTapUp(MotionEvent e) {
				// We prevent from moving tiles while we are in the middle of an animation
				if (handleTouchEvents) {
					int pressedTile = drawingCoordinatesToTile(e.getX(), e.getY());

					if (pressedTile != -1) {
						if (puzzle.canMove(pressedTile)) {
							puzzle.moveTile(pressedTile);
						}
					}
				}

				return true;
			}

			public void onShowPress(MotionEvent e) {

			}

			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				// Prevent scrolling while performing an animation
				if (handleTouchEvents) {
					/*
					 * We only handle the scroll event if we are not in an animation and also there
					 * has been a DOWN event after the last UP event.
					 */
					if (!handleScroll) {
						return true;
					}

					/*
					 * If we start dragging we must check if the tile that was initially pressed can
					 * be moved. If so, set the value of "draggingTile".
					 */
					if (draggingTile == -1) {
						int pressedTile = drawingCoordinatesToTile(e1.getX(), e1.getY());

						if (pressedTile != -1) {
							if (puzzle.canMove(pressedTile)) {
								draggingTile = pressedTile;
							}
						}
					}

					/*
					 * If a tile is being dragged, we must update the corresponding
					 * "draggingTilePercentage" value.
					 */
					if (draggingTile != -1) {
						float oldPercentage = draggingTilePercentage;

						Direction dir = puzzle.moveDirection(draggingTile);

						float verticalDistance = e2.getY() - e1.getY();
						float horizontalDistance = e2.getX() - e1.getX();

						switch (dir) {
							case UP:
								draggingTilePercentage = -verticalDistance / tileHeight;
								break;
							case DOWN:
								draggingTilePercentage = verticalDistance / tileHeight;
								break;
							case LEFT:
								draggingTilePercentage = -horizontalDistance / tileWidth;
								break;
							case RIGHT:
								draggingTilePercentage = horizontalDistance / tileWidth;
								break;
						}

						if (draggingTilePercentage < 0) {
							draggingTilePercentage = 0;
						} else if (draggingTilePercentage > 1) {
							draggingTilePercentage = 1;
						}

						if (oldPercentage != draggingTilePercentage) {
							invalidateTileRegion(puzzle.getTilePosition(draggingTile), dir);
						}
					}
				}

				return true;
			}

			public void onLongPress(MotionEvent e) {

			}

			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}

			public boolean onDown(MotionEvent e) {
				if (handleTouchEvents) {
					handleScroll = true;
					return false;
				}

				return true;
			}
		});

		this.gestureDetector.setIsLongpressEnabled(false);

		if (isInEditMode()) {
			this.setNPuzzle(NPuzzle.newNPuzzleFromSideSize(3));

			if (this.image == null) {
				this.setImage(createDefaultImage(this.puzzle.getN()));
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * We do not process the event if we were explicitly told so (handleTouchEvents==false) or
		 * if there is no image or puzzle set (in that case we cannot process the event because
		 * there is no puzzle to interact with).
		 */
		if (!handleTouchEvents || this.image == null || this.puzzle == null) {
			return false;
		}

		boolean consumed = this.gestureDetector.onTouchEvent(event);

		/*
		 * The ACTION_UP event is consumed by the gesture detector only in "tap up" or "fling".
		 * However, if we do not perform a tap or a fling, we have to handle the ACTION_UP event,
		 * since we may be dragging a tile, in which case we have to either move the tile (in case
		 * it has been moved 50% of its way to the empty tile) or put it back to its original place.
		 */
		if (event.getAction() == MotionEvent.ACTION_UP) {
			this.handleScroll = false;

			if (!consumed) {
				if (this.draggingTile != -1) {
					if (this.draggingTilePercentage > 0.5) {
						this.puzzle.moveTile(this.draggingTile);
					} else {
						// Start animation to put the tile back to its natural position
						this.draggingTilePercentage = (1 - this.draggingTilePercentage);
						this.startMovingAnimation(this.draggingTile, this.puzzle
								.getEmptyTilePosition(), this.puzzle.moveDirection(
								this.puzzle.getEmptyTilePosition(),
								this.puzzle.getTilePosition(this.draggingTile)));
					}

					this.draggingTile = -1;
				}
			}
		}

		return true;
	}

	protected void onDraw(Canvas canvas) {
		/*
		 * If either the puzzle or the image is not set, draw a blank view.
		 */
		if (this.puzzle == null || this.image == null) {
			canvas.drawARGB(0, 0, 0, 0);
			return;
		}

		/*
		 * Otherwise, draw the board.
		 */
		// Draw static tiles
		for (int i = 0; i < this.puzzle.getNumTiles() - 1; i++) {
			if (i != this.movingTile && i != this.draggingTile) {
				int tilePosition = this.puzzle.getTilePosition(i);

				// Convert tile position to canvas coordinates
				this.tilePositionToCanvasCoordinates(tilePosition, this.tileCoordinates);

				// Draw tile
				this.drawTile(canvas, i, this.tileCoordinates);
			}
		}

		// Draw moving tile if there is a tile moving
		if (this.movingTile != -1) {
			this.drawMovingTile(canvas);
		}

		// Draw dragging tile if there is a tile being dragged
		if (this.draggingTile != -1) {
			this.drawDraggingTile(canvas);
		}
	}

	/**
	 * Sets the duration of the animation that is displayed when a tile is moved.
	 * 
	 * @param duration
	 *            the duration. Cannot be a negative number.
	 */
	public void setTileAnimationDuration(long duration) {
		if (duration < 0) {
			throw new IllegalArgumentException("Duration cannot be negative");
		}

		this.animationDuration = duration;
	}

	/**
	 * Returns the duration of the animation that is displayed when a tile is moved.
	 * 
	 * @return the duration of the animation that is displayed when a tile is moved.
	 */
	public long getTileAnimationDuration() {
		return this.animationDuration;
	}

	/**
	 * Returns the image being displayed by the puzzle, or null if not set. Note that this Bitmap
	 * may not necessarily be that set in {@link #setImage(Bitmap)}, since it may have been
	 * resampled.
	 * 
	 * @return the image being displayed by the puzzle, or null if not set.
	 */
	public Bitmap getImage() {
		return this.image;
	}

	/**
	 * Sets the rotation of the image being displayed by the puzzle. This method can be called even
	 * if no image or puzzle is set. If the image is set, a layout pass will be requested, since the
	 * view may change its size.
	 * 
	 * @param rotation
	 *            the rotation of the image. Can be 0, 90, 180 and 270.
	 */
	public void setImageRotation(int rotation) {
		if (rotation != 0 && rotation != 90 && rotation != 180 && rotation != 270) {
			throw new IllegalArgumentException("Invalid rotation: " + rotation);
		}

		this.imageRotation = rotation;

		if (this.image != null) {
			/*
			 * Request a layout and invalidation. Invalidation is required, since the dimensions of
			 * the view may not change after the layout, in which case the view would not be
			 * redrawn.
			 * 
			 * If after the rotation the image changes its size, the tile positions will be
			 * recomputed automatically in onSizeChanged().
			 * 
			 * Note that here we call computeBoardPositionsAndTileDimensions() if possible. Why?
			 * Because the view's size may not change even if the new image has a different size.
			 * This may happen for instance if the view has fixed dimensions or if the new image has
			 * the same size as the previous one. In that case, since the view's size will not
			 * change after the layout pass, onSizeChanged() will not be called, so we need to
			 * manually call computeBoardPositionsAndTileDimensions(). If the view's size finally
			 * changes, computeBoardPositionsAndTileDimensions() will be called twice, one in
			 * onSizeChanged() and another one here, but only the call in onSizeChanged() will
			 * prevail and render the correct values.
			 */
			if (this.measured && this.puzzle != null) {
				this.computeBoardPositionsAndTileDimensions();
			}

			this.requestLayout();
			this.invalidate();
		}

		this.measured = true;
	}

	/**
	 * Returns the clockwise rotation (in degrees) of the image displayed by the puzzle. If !=0, it
	 * means that the original image is being displayed rotated clockwise by the number of degrees
	 * specified here.
	 * <p>
	 * The rotation value can be 0, 90, 180 and 270.
	 * 
	 * @return the clockwise rotation of the image displayed by the puzzle.
	 */
	public int getImageRotation() {
		return this.imageRotation;
	}

	/**
	 * Sets the {@link NPuzzle} displayed by the {@code NPuzzleView}. Use null to set no puzzle, in
	 * which case the view will display no puzzle.
	 * <p>
	 * This can be used to replace a previously set puzzle.
	 * 
	 * @param puzzle
	 *            the puzzle that is displayed. May be null.
	 */
	public void setNPuzzle(NPuzzle puzzle) {
		if (this.puzzle != null) {
			this.puzzle.removeTileListener(this);
		}

		this.puzzle = puzzle;

		if (this.puzzle != null) {
			// Add this view as a listener of the puzzle
			this.puzzle.addTileListener(this);
		}

		/*
		 * If the view has already been measured it means that it is currently being displayed, so
		 * since the puzzle has changed, we have to redraw it and recompute all the variables
		 * associated with the board.
		 */
		if (this.measuredWithImage && this.puzzle != null) {
			this.computeBoardPositionsAndTileDimensions();
		}

		invalidate();
	}

	/**
	 * This method must be called when the user is done with the current puzzle. This unregisters
	 * the view from the puzzle as a listener. In order to reuse this view, a new puzzle should be
	 * set. Otherwise, the view will not be synchronized with the puzzle.
	 */
	public void unregisterPuzzle() {
		this.puzzle.removeTileListener(this);
	}

	/**
	 * Adds a listener to the set of listeners reported by the view.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addViewListener(INPuzzleViewListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null listener");
		}

		this.listeners.add(listener);
	}

	/**
	 * Removes a listener from the list of listeners reported by the view.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeViewListener(INPuzzleViewListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null listener");
		}

		this.listeners.remove(listener);
	}

	/**
	 * Sets the image to display on the view. This can be used to replace whatever image was being
	 * displayed. Can be null to display no image.
	 * <p>
	 * This method may resample the input Bitmap if it is too large. If the input bitmap is not to
	 * be resampled by the view, be sure that its width and height are no larger than
	 * {@value #IMAGE_MAX_WIDTH} and {@value #IMAGE_MAX_HEIGHT} respectively. Otherwise, the view
	 * will resample the input Bitmap.
	 * 
	 * @param bitmap
	 *            the bitmap to display. Can be null to display no image.
	 */
	public void setImage(Bitmap bitmap) {
		/*
		 * Force the image to be smaller than the specific hardware limits (only if bitmap is not
		 * null).
		 */
		if (bitmap != null
				&& (bitmap.getWidth() > IMAGE_MAX_WIDTH || bitmap.getHeight() > IMAGE_MAX_HEIGHT)) {
			bitmap = ImageUtilities.resampleBitmap(bitmap, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
		}

		this.image = bitmap;

		/*
		 * If the new image's size is different from the size of the previous image (or the previous
		 * image was null), we should request a layout, because the view's size may change. In that
		 * case, the variables associated with the board will be recomputed when onSizeChanged() is
		 * called again.
		 * 
		 * Note that here we call computeBoardPositionsAndTileDimensions() if possible. Why? Because
		 * the view's size may not change even if the new image has a different size. This may
		 * happen for instance if the view has fixed dimensions or if the new image has the same
		 * size as the previous one. In that case, since the view's size will not change after the
		 * layout pass, onSizeChanged() will not be called, so we need to manually call
		 * computeBoardPositionsAndTileDimensions(). If the view's size finally changes,
		 * computeBoardPositionsAndTileDimensions() will be called twice, one in onSizeChanged() and
		 * another one here, but only the call in onSizeChanged() will prevail and render the
		 * correct values.
		 */
		if (this.measured && this.puzzle != null && this.image != null) {
			this.computeBoardPositionsAndTileDimensions();
		}

		requestLayout();
		invalidate();
	}

	/**
	 * Enables or disables touch events for the view.
	 * 
	 * @param enabled
	 *            true to enable touch events, and false to disable them.
	 */
	public void enableTouchEvents(boolean enabled) {
		this.handleTouchEvents = enabled;
	}

	/**
	 * Returns true if the default image is being used, and false otherwise.
	 * 
	 * @return true if the default image is being used, and false otherwise.
	 */
	public boolean isDisplayingDefaultImage() {
		return this.isDefaultImageBeingUsed;
	}

	/**
	 * Returns the {@link NPuzzle} that the view is displaying, or null if no puzzle is set.
	 * 
	 * @return the {@link NPuzzle} that the view is displaying, or null if no puzzle is set.
	 */
	public NPuzzle getNPuzzle() {
		return this.puzzle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (this.image == null) {
			/*
			 * If the image is not set, accept input dimensions.
			 */
			setMeasuredDimension(
					Math.max(getSuggestedMinimumWidth(), MeasureSpec.getSize(widthMeasureSpec)),
					Math.max(getSuggestedMinimumHeight(), MeasureSpec.getSize(heightMeasureSpec)));
		} else {
			/*
			 * Otherwise, if the image has been set, we can compute the size of the view based on
			 * the image's dimensions.
			 * 
			 * In case we are passed UNSPECIFIED, we will use the image dimensions (plus padding).
			 * Note that we use the dimensions of the rotated image.
			 */
			int defaultWidth = getDefaultSize(this.getRotatedImageWidth() + getPaddingLeft()
					+ getPaddingRight(), widthMeasureSpec);
			int defaultHeight = getDefaultSize(this.getRotatedImageHeight() + getPaddingTop()
					+ getPaddingBottom(), heightMeasureSpec);

			/*
			 * Once we have computed the default size of this view, we will try to reduce the
			 * dimension (width or height) that does not fit the view, if possible. This means that
			 * if the image dimension that does not fit the view is UNSPECIFIED or AT_MOST, it will
			 * be reduced to the scaled image size plus padding.
			 */
			Pair<Integer, Integer> finalDimensions = this.reduceNotFittingDimensionIfPossible(
					defaultWidth, MeasureSpec.getMode(widthMeasureSpec), defaultHeight,
					MeasureSpec.getMode(heightMeasureSpec));

			setMeasuredDimension(Math.max(getSuggestedMinimumWidth(), finalDimensions.first),
					Math.max(getSuggestedMinimumHeight(), finalDimensions.second));

			this.measuredWithImage = true;
		}

		this.measured = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (this.measuredWithImage && this.puzzle != null) {
			computeBoardPositionsAndTileDimensions();
		}
	}

	/**
	 * Given the default size of the view (as computed by {@link #getDefaultSize(int, int)}), this
	 * method tries to reduce the view's size as much as possible, if possible.
	 * <p>
	 * When the default size of the view is determined, we must take into account that the puzzle
	 * itself, that is, the board that is displayed by this view, is going to be scaled to occupy as
	 * much space as possible. The scaled image will have one of its dimensions (width or height)
	 * match the corresponding view size, while the other dimension will be smaller than the other
	 * view size (in this calculations we take into account that padding removes part of the usable
	 * area). We want the fitting dimension remain the same. However, the non-fitting dimension
	 * should be reduced if its measure mode allows it, in order to reduce the space it occupies at
	 * a minimum level (that is, it will fit the image). Thus, after calling this method, the
	 * fitting dimension will remain the same. However, if the non-fitting dimension has a measure
	 * mode of UNSPECIFIED or AT_MOST, it will be reduced to fit the size of the scaled image.
	 * 
	 * @param viewWidth
	 *            the default view width.
	 * @param widthMeasureMode
	 *            the width measure mode.
	 * @param viewHeight
	 *            the default view height.
	 * @param heightMeasureMode
	 *            the default height measure mode.
	 * @return a pair with the new dimensions. The first dimension is the width, and the second
	 *         dimension is the height. Use this pair to set the dimensions of the view in
	 *         {@link #onMeasure(int, int)}.
	 */
	private Pair<Integer, Integer> reduceNotFittingDimensionIfPossible(int viewWidth,
			int widthMeasureMode, int viewHeight, int heightMeasureMode) {

		/*
		 * If none of the dimensions can be modified, return the input size.
		 */
		if (widthMeasureMode == MeasureSpec.EXACTLY && heightMeasureMode == MeasureSpec.EXACTLY) {
			return new Pair<Integer, Integer>(viewWidth, viewHeight);
		}

		// Dimensions once padding is removed. This is the room we have to draw the puzzle
		int availableWidth = viewWidth - getPaddingLeft() - getPaddingRight();
		int availableHeight = viewHeight - getPaddingTop() - getPaddingBottom();

		float viewWHRatio = availableWidth / (float) availableHeight;
		float imageWHRatio = this.getRotatedImageWidth() / (float) this.getRotatedImageHeight();

		/*
		 * If "fitWidth" is true, it means that the image must be resized so its width matches the
		 * view's width without padding (its height will be less or equal than that of the view).
		 * Otherwise, it is the height that must be resized to match the view's height (without
		 * padding).
		 */
		boolean fitWidth = viewWHRatio < imageWHRatio;

		int tileWidth;
		int tileHeight;

		if (fitWidth) {
			tileWidth = availableWidth / this.puzzle.getSideNumTiles();
			tileHeight = (int) (this.getRotatedImageHeight()
					/ (this.getRotatedImageWidth() / (float) availableWidth) / this.puzzle
					.getSideNumTiles());
		} else {
			tileHeight = availableHeight / this.puzzle.getSideNumTiles();
			tileWidth = (int) (this.getRotatedImageWidth()
					/ (this.getRotatedImageHeight() / (float) availableHeight) / this.puzzle
					.getSideNumTiles());
		}

		int resizedImageWidth = tileWidth * this.puzzle.getSideNumTiles();
		int resizedImageHeight = tileHeight * this.puzzle.getSideNumTiles();

		int finalWidth = viewWidth;
		int finalHeight = viewHeight;

		/*
		 * Now, the final step: if we can reduce the dimension that does not fit the view, reduce
		 * it.
		 */
		if (fitWidth) {
			finalWidth = viewWidth;

			// Reduce height if possible
			if (heightMeasureMode == MeasureSpec.UNSPECIFIED
					|| heightMeasureMode == MeasureSpec.AT_MOST) {
				finalHeight = viewHeight - (availableHeight - resizedImageHeight);
			}
		} else {
			finalHeight = viewHeight;

			// Reduce width if possible
			if (widthMeasureMode == MeasureSpec.UNSPECIFIED
					|| widthMeasureMode == MeasureSpec.AT_MOST) {
				finalWidth = viewWidth - (availableWidth - resizedImageWidth);
			}
		}

		return new Pair<Integer, Integer>(finalWidth, finalHeight);
	}

	/**
	 * Method that computes the position of the board as well as its size. This method is called
	 * once a definitive size has been set to the view. From that size and the {@link #image}'s size
	 * and rotation ({@link #imageRotation}), this method computes where the board should be placed
	 * within the view, as well as the size of each tile. In practice, this method sets values for
	 * {@link #fitWidth}, {@link #tileWidth}, {@link #tileHeight}, {@link #widthGap},
	 * {@link #heightGap}, {@link #resizedImageWidth} and {@link #resizedImageHeight}.
	 * <p>
	 * This method can only be called if {@link #measured} is true and {@link #puzzle} is not null.
	 * Otherwise, an exception is thrown.
	 */
	private void computeBoardPositionsAndTileDimensions() {
		if (!this.measured || this.puzzle == null || this.image == null) {
			throw new IllegalStateException(
					"This method can only be called once the view has been set a size and there are an image and a puzzle set");
		}

		// Width and height of the view once we remove padding
		int viewWidth = this.getWidth() - getPaddingLeft() - getPaddingRight();
		int viewHeight = this.getHeight() - getPaddingTop() - getPaddingBottom();

		float viewWHRatio = viewWidth / (float) viewHeight;
		float imageWHRatio = this.getRotatedImageWidth() / (float) this.getRotatedImageHeight();

		/*
		 * If "fitWidth" is true, it means that the image must be resized so its width matches the
		 * view's width without padding (its height will be less or equal than that of the view).
		 * Otherwise, it is the height that must be resized to match the view's height (without
		 * padding).
		 * 
		 * Note that we account for the rotation (we used getRotatedImageXXX() above and it will
		 * also be used below).
		 */
		this.fitWidth = viewWHRatio < imageWHRatio;

		if (this.fitWidth) {
			this.tileWidth = viewWidth / this.puzzle.getSideNumTiles();
			this.tileHeight = (int) (this.getRotatedImageHeight()
					/ (this.getRotatedImageWidth() / (float) viewWidth) / this.puzzle
					.getSideNumTiles());
		} else {
			this.tileHeight = viewHeight / this.puzzle.getSideNumTiles();
			this.tileWidth = (int) (this.getRotatedImageWidth()
					/ (this.getRotatedImageHeight() / (float) viewHeight) / this.puzzle
					.getSideNumTiles());
		}

		this.resizedImageWidth = this.tileWidth * this.puzzle.getSideNumTiles();
		this.resizedImageHeight = this.tileHeight * this.puzzle.getSideNumTiles();
		this.widthGap = (viewWidth - this.resizedImageWidth) / 2 + getPaddingLeft();
		this.heightGap = (viewHeight - this.resizedImageHeight) / 2 + getPaddingTop();
	}

	/**
	 * Given the coordinates of a point of this view (relative to this view), this method returns
	 * the tile under those coordinates. Returns -1 if no tile is under the specified coordinates.
	 * 
	 * @param x
	 *            the x coordinate of the point.
	 * @param y
	 *            the y coordinate of the point.
	 * @return the tile under the coordinates, or -1 if no tile is under such coordinates.
	 */
	private int drawingCoordinatesToTile(float x, float y) {
		/*
		 * If the point falls outside the puzzle, return -1.
		 */
		if (x < this.widthGap || x >= this.widthGap + this.resizedImageWidth) {
			return -1;
		}

		if (y < this.heightGap || y >= this.heightGap + this.resizedImageHeight) {
			return -1;
		}

		/*
		 * Otherwise, get the position of the tile that has been pressed.
		 */
		int column = ((int) (x - this.widthGap)) / this.tileWidth;
		int row = ((int) (y - this.heightGap)) / this.tileHeight;

		return this.puzzle.getTileAtPosition(row * this.puzzle.getSideNumTiles() + column);
	}

	/**
	 * If there is a moving tile ({@link #movingTile} is not -1), this method draws it. It is drawn
	 * rotated.
	 * 
	 * @param canvas
	 *            the canvas.
	 */
	private void drawMovingTile(Canvas canvas) {
		if (this.movingTile != -1) {
			float percentage;

			if (this.movingTileStartTime == this.movingTileEndTime) {
				percentage = 1;
			} else {
				percentage = (System.currentTimeMillis() - this.movingTileStartTime)
						/ (float) (this.movingTileEndTime - this.movingTileStartTime);
			}

			if (percentage > 1) {
				percentage = 1;
			}

			/*
			 * Get the drawing coordinates of the tile. The coordinates are those that the tile
			 * would normally occupy if it were not moving, but slightly modified by a factor that
			 * depends on the direction of the movement as well as how much of the animation is
			 * completed so far.
			 */
			this.tilePositionToCanvasCoordinates(this.movingPos, this.tileCoordinates);

			switch (this.movingTileDirection) {
				case UP:
					this.tileCoordinates.y -= this.tileHeight
							* this.movingTileInitialPercentage
							+ (this.tileHeight - this.tileHeight * this.movingTileInitialPercentage)
							* percentage;

					break;
				case DOWN:
					this.tileCoordinates.y += this.tileHeight
							* this.movingTileInitialPercentage
							+ (this.tileHeight - this.tileHeight * this.movingTileInitialPercentage)
							* percentage;

					break;
				case LEFT:
					this.tileCoordinates.x -= this.tileWidth * this.movingTileInitialPercentage
							+ (this.tileWidth - this.tileWidth * this.movingTileInitialPercentage)
							* percentage;

					break;
				case RIGHT:
					this.tileCoordinates.x += this.tileWidth * this.movingTileInitialPercentage
							+ (this.tileWidth - this.tileWidth * this.movingTileInitialPercentage)
							* percentage;

					break;
			}

			// Draw the tile
			this.drawTile(canvas, this.movingTile, this.tileCoordinates);

			// If animation is done, stop by setting movingTile to -1
			if (percentage >= 1) {
				this.handleTouchEvents = true;
				this.movingTile = -1;

				// Report listeners
				this.fireAnimationFinishedEvent();
			} else {
				// Otherwise post a draw request to continue drawing
				this.invalidateTileRegion(this.movingPos, this.movingTileDirection);
			}
		}
	}

	/**
	 * If there is a dragging tile ({@link #draggingTile}), this method draws it. It is drawn
	 * rotated.
	 * 
	 * @param canvas
	 *            the canvas.
	 */
	private void drawDraggingTile(Canvas canvas) {
		if (this.draggingTile != -1) {
			// Get the direction of the movement
			Direction dir = this.puzzle.moveDirection(this.draggingTile);

			/*
			 * Get the drawing coordinates of the tile. The coordinates are those that the tile
			 * would normally occupy if it were not being dragged, but slightly modified by a factor
			 * that depends on the direction of the movement as well as how much the tile has been
			 * dragged so far.
			 */
			this.tilePositionToCanvasCoordinates(this.puzzle.getTilePosition(this.draggingTile),
					this.tileCoordinates);

			switch (dir) {
				case UP:
					this.tileCoordinates.y -= this.tileHeight * this.draggingTilePercentage;
					break;
				case DOWN:
					this.tileCoordinates.y += this.tileHeight * this.draggingTilePercentage;
					break;
				case LEFT:
					this.tileCoordinates.x -= this.tileWidth * this.draggingTilePercentage;
					break;
				case RIGHT:
					this.tileCoordinates.x += this.tileWidth * this.draggingTilePercentage;
					break;
			}

			// Draw the tile
			this.drawTile(canvas, this.draggingTile, this.tileCoordinates);
		}
	}

	/**
	 * Draws a tile at the given coordinates. The tile is drawn rotated according to
	 * {@link #imageRotation}. Thus, the tile's coordinates should also account for the rotation.
	 * For static tiles, the {@link #tilePositionToCanvasCoordinates(int, Point)} gives the
	 * appropriate coordinates. For moving tiles, you should compute the specific coordinates to
	 * use.
	 * <p>
	 * Note that this method modifies {@link #tileDestRectangle} and {@link #tileSrcRectangle}.
	 * 
	 * @param canvas
	 *            the canvas.
	 * @param tile
	 *            the tile to draw.
	 * @param coordinates
	 *            the coordinates of the top left corner of the tile.
	 */
	private void drawTile(Canvas canvas, int tile, Point coordinates) {
		/*
		 * Draw tile. We must compute both the coordinates of the tile on the view and the
		 * coordinates of the tile on the original image. We must also take into account the
		 * rotation of the puzzle's image.
		 * 
		 * Rotation of the puzzle's image is accomplished via the Canvas.rotate() method. Thus, we
		 * must be very careful regarding the coordinates of both the source and destination tiles.
		 */

		/*
		 * Destination coordinates, based on the size of each tile on the board.
		 */
		this.tileDestRectangle.left = coordinates.x;
		this.tileDestRectangle.top = coordinates.y;
		this.tileDestRectangle.right = coordinates.x + this.tileWidth;
		this.tileDestRectangle.bottom = coordinates.y + this.tileHeight;

		/*
		 * The tileDestRectangle is properly computed by now. However, we must take into account the
		 * fact that in the end the tile is drawn with a rotated canvas (Canvas.rotate()). To
		 * compensate the rotation introduced by the canvas, we need to rotate the rectangle counter
		 * clockwise the same amount that the canvas will rotate it later clockwise.
		 * 
		 * When the canvas draws the rotated tile, this rotation will be canceled out with the
		 * canvas' own rotation.
		 */
		this.rotateRect(-this.imageRotation, canvas.getWidth() / 2, canvas.getHeight() / 2,
				this.tileDestRectangle);

		/*
		 * Source coordinates, based on the size of each tile on the original image and the
		 * rotation. Rotation is fundamental here, since it determines what portion of the source
		 * image must be drawn for tile "tile".
		 */
		int tileImageW = this.image.getWidth() / this.puzzle.getSideNumTiles();
		int tileImageH = this.image.getHeight() / this.puzzle.getSideNumTiles();
		int sideNumTiles = this.puzzle.getSideNumTiles();

		/*
		 * row and column represent the row and column of the original image's tile that we should
		 * use if the image were not rotated. However, if the image is rotated, we need to transform
		 * the row and column according to the rotation angle.
		 */
		int column = tile % sideNumTiles;
		int row = tile / sideNumTiles;

		int rotatedRow = row;
		int rotatedColumn = column;

		switch (this.imageRotation) {
			case 90:
				rotatedColumn = row;
				rotatedRow = sideNumTiles - column - 1;
				break;
			case 180:
				rotatedRow = sideNumTiles - row - 1;
				rotatedColumn = sideNumTiles - column - 1;
				break;
			case 270:
				rotatedColumn = sideNumTiles - row - 1;
				rotatedRow = column;
				break;
		}

		/*
		 * Now we can compute the actual piece of the original image that will be drawn on the
		 * board.
		 */
		this.tileSrcRectangle.left = rotatedColumn * tileImageW;
		this.tileSrcRectangle.top = rotatedRow * tileImageH;
		this.tileSrcRectangle.right = this.tileSrcRectangle.left + tileImageW;
		this.tileSrcRectangle.bottom = this.tileSrcRectangle.top + tileImageH;

		/*
		 * Draw tile. Since we need to take into account rotation, we will rotate the canvas.
		 */
		canvas.save();
		canvas.rotate(this.imageRotation, canvas.getWidth() / 2, canvas.getHeight() / 2);

		/*
		 * Draw border of tile first. We draw a filled rectangle. Initially we were drawing the
		 * border of a rectangle wit a specific width. However, since the width of the border grows
		 * not only inside the rectangle, but also outside (thus overflowing the specified rectangle
		 * coordinates), we decided to draw a simple rectangle and then overlap the tile's image.
		 */
		canvas.drawRect(this.tileDestRectangle, this.tileFramePaint);

		/*
		 * Now draw the tile itself. Since we do not want the tile's image to erase the border of
		 * the tile, we need to constraint the drawing area. We do so by intersecting the current
		 * canvas area with a rectangle which is "this.tileBorderWidth" pixels smaller than the
		 * tile's size.
		 */
		this.tileDestRectangle.bottom -= this.tileBorderWidth;
		this.tileDestRectangle.top += this.tileBorderWidth;
		this.tileDestRectangle.left += this.tileBorderWidth;
		this.tileDestRectangle.right -= this.tileBorderWidth;

		canvas.clipRect(tileDestRectangle);

		/*
		 * We must undo these changes to draw the image on the canvas. Otherwise the image will be
		 * reduced to match the tile's frame size, which is not what we want.
		 */
		this.tileDestRectangle.bottom += this.tileBorderWidth;
		this.tileDestRectangle.top -= this.tileBorderWidth;
		this.tileDestRectangle.left -= this.tileBorderWidth;
		this.tileDestRectangle.right += this.tileBorderWidth;

		canvas.drawBitmap(this.image, this.tileSrcRectangle, this.tileDestRectangle, null);

		canvas.restore();
	}

	/**
	 * Given the position of a tile <b>that is not moving</b>, this method returns the canvas
	 * coordinates where it should be painted. The result is set in the input argument
	 * <code>coordinates</code>.
	 * <p>
	 * This method can only be called once the view has been measured (
	 * {@link #onSizeChanged(int, int, int, int)}).
	 */
	private void tilePositionToCanvasCoordinates(int tilePos, Point coordinates) {
		if (this.fitWidth) {
			coordinates.x = (tilePos % this.puzzle.getSideNumTiles()) * this.tileWidth;
			coordinates.y = (tilePos / this.puzzle.getSideNumTiles()) * this.tileHeight;
		} else {
			coordinates.x = (tilePos % this.puzzle.getSideNumTiles()) * this.tileWidth;
			coordinates.y = (tilePos / this.puzzle.getSideNumTiles()) * this.tileHeight;
		}

		coordinates.x += this.widthGap;
		coordinates.y += this.heightGap;
	}

	/**
	 * Starts an animation that moves the tile <code>tile</code> from <code>pos</code> to the next
	 * tile in the direction <code>direction</code>. This is the animation that gets drawn by the
	 * {@link #drawMovingTile(Canvas)} method.
	 * <p>
	 * When this method is called, whatever animation was previously taking place is canceled (it is
	 * actually ended, so the animation that was taking place ends abruptly instead of doing it
	 * smoothly). Also, touch events are disabled until the animation ends, because we do not want
	 * the user to modify the view if an animation is pending.
	 * <p>
	 * If the <code>tile</code> was the tile being dragged ({@link #draggingTile}), then the
	 * animation does not start from scratch. Instead, it starts from the point where the user
	 * stopped dragging the tile, which is determined by {@link #draggingTilePercentage}.
	 * <p>
	 * This method cancels the dragging tile.
	 */
	private void startMovingAnimation(int tile, int pos, Direction direction) {
		// Starts animation
		this.movingTile = tile;
		this.movingTileStartTime = System.currentTimeMillis();
		this.movingPos = pos;
		this.movingTileDirection = direction;
		this.handleTouchEvents = false;

		if (this.draggingTile != -1 && tile == this.draggingTile) {
			this.movingTileEndTime = this.movingTileStartTime
					+ (long) (this.animationDuration * (1 - this.draggingTilePercentage));
			this.movingTileInitialPercentage = this.draggingTilePercentage;
		} else {
			this.movingTileEndTime = this.movingTileStartTime + this.animationDuration;
			this.movingTileInitialPercentage = 0;
		}

		// Cancel dragging tile
		this.draggingTile = -1;

		invalidateTileRegion(pos, direction);

		// Report listeners
		this.fireAnimationStartedEvent();
	}

	/**
	 * Given a tile position of the puzzle and a direction, this method invalidates (for drawing)
	 * the region consisting of the tile at <code>tilePos</code> and the tile next to it in the
	 * specified direction.
	 * <p>
	 * This method is used to optimize drawing: when a tile is being moved or dragged, calling this
	 * method to fire a draw event is much more efficient than re-redrawing the whole view.
	 */
	private void invalidateTileRegion(int tilePos, Direction direction) {
		Point coordinates = new Point();
		tilePositionToCanvasCoordinates(tilePos, coordinates);

		Rect dirtyRegion = new Rect();

		switch (direction) {
			case UP:
				dirtyRegion.bottom = coordinates.y + this.tileHeight;
				dirtyRegion.left = coordinates.x;
				dirtyRegion.right = coordinates.x + this.tileWidth;
				dirtyRegion.top = coordinates.y - this.tileHeight;
				break;
			case DOWN:
				dirtyRegion.bottom = coordinates.y + this.tileHeight * 2;
				dirtyRegion.left = coordinates.x;
				dirtyRegion.right = coordinates.x + this.tileWidth;
				dirtyRegion.top = coordinates.y;
				break;
			case LEFT:
				dirtyRegion.bottom = coordinates.y + this.tileHeight;
				dirtyRegion.left = coordinates.x - this.tileWidth;
				dirtyRegion.right = coordinates.x + this.tileWidth;
				dirtyRegion.top = coordinates.y;
				break;
			case RIGHT:
				dirtyRegion.bottom = coordinates.y + this.tileHeight;
				dirtyRegion.left = coordinates.x;
				dirtyRegion.right = coordinates.x + this.tileWidth * 2;
				dirtyRegion.top = coordinates.y;
				break;
		}

		invalidate(dirtyRegion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.odracirnumira.npuzzle.npuzzle.NPuzzle.ITileListener#tileMoved(int, int, int)
	 */
	public void tileMoved(int tile, int oldPos, int newPos) {
		this.startMovingAnimation(tile, oldPos, puzzle.moveDirection(oldPos, newPos));
	}

	/**
	 * Fires the event that reports to {@link #listeners} that the animation of the tile moving has
	 * started.
	 */
	private void fireAnimationStartedEvent() {
		for (INPuzzleViewListener listener : this.listeners) {
			listener.movingTileAnimationStarted();
		}
	}

	/**
	 * Fires the event that reports to {@link #listeners} that the animation of the tile moving has
	 * finished.
	 */
	private void fireAnimationFinishedEvent() {
		for (INPuzzleViewListener listener : this.listeners) {
			listener.movingTileAnimationEnded();
		}
	}

	/**
	 * This method creates a default image for the puzzle. The <code>NPuzzleView</code>
	 * automatically uses this method when the user has not set a custom image. By doing so, an
	 * image can be displayed even if the user does not set a custom image.
	 * 
	 * @param N
	 *            the value of N for the puzzle.
	 * @return the default image, or null if it could not be created.
	 */
	public static Bitmap createDefaultImage(int N) {
		// TODO: we should check that the returned image is no larger than the max NPuzzleView's
		// allowed dimensions
		if (!MathUtilities.isPerfectSquare(N + 1) || N < NPuzzle.MIN_N || N > NPuzzle.MAX_N) {
			throw new IllegalArgumentException("Invalid value for N");
		}

		try {
			/*
			 * We have to allocate enough space for each tile so the number inside it perfectly
			 * fits. Thus, we have to measure the size of all the numbers of the puzzle, and get the
			 * maximum height and width.
			 */
			Paint textPaint = new Paint();

			int pixel = (int) UIUtilities.convertDpToPixel(20, NPuzzleApplication.getApplication());
			textPaint.setTextSize(pixel);
			textPaint.setAntiAlias(true);

			int maxHeight = 0;
			int maxWidth = 0;

			int numTiles = N + 1;
			int sideSize = (int) Math.sqrt(N + 1);

			for (int i = 0; i < numTiles; i++) {
				String iS = Integer.toString(i);
				Rect bounds = new Rect();
				textPaint.getTextBounds(iS, 0, iS.length(), bounds);

				int height = bounds.bottom - bounds.top;
				int width = (int) textPaint.measureText(iS);

				if (height > maxHeight) {
					maxHeight = height;
				}

				if (width > maxWidth) {
					maxWidth = width;
				}
			}

			/*
			 * Tiles are squared, so we get the largest dimension.
			 */
			int tileSize = Math.max(maxWidth, maxHeight);

			// Add a small gap so the text does not touch the tile border
			tileSize += (int) UIUtilities.convertDpToPixel(10, NPuzzleApplication.getApplication());

			/*
			 * Now draw all the tiles onto a Bitmap.
			 */
			Bitmap result = Bitmap.createBitmap(tileSize * sideSize, tileSize * sideSize,
					Config.ARGB_8888);

			Canvas canvas = new Canvas(result);

			canvas.drawARGB(255, 100, 100, 100);

			for (int i = 0; i < numTiles; i++) {
				/*
				 * Compute offset to center the text.
				 */
				String iS = Integer.toString(i);
				Rect bounds = new Rect();
				textPaint.getTextBounds(iS, 0, iS.length(), bounds);

				int topBottomOffset = (int) ((tileSize - (bounds.bottom - bounds.top)) / 2);
				int leftRightOffset = (tileSize - (int) textPaint.measureText(iS)) / 2;

				/*
				 * Draw the text.
				 */
				canvas.drawText(Integer.toString(i), tileSize * (i % sideSize) + leftRightOffset,
						tileSize * (i / sideSize + 1) - topBottomOffset, textPaint);
			}

			return result;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

	/**
	 * This method returns the width of {@link #image} after applying a rotation of
	 * {@link #imageRotation} degrees.
	 * 
	 * @return the width to {@link #image} after applying a rotation of {@link #imageRotation}
	 *         degrees.
	 */
	private int getRotatedImageWidth() {
		if (this.imageRotation == 0 || this.imageRotation == 180) {
			return this.image.getWidth();
		} else {
			return this.image.getHeight();
		}
	}

	/**
	 * This method returns the height of {@link #image} after applying a rotation of
	 * {@link #imageRotation} degrees.
	 * 
	 * @return the height of {@link #image} after applying a rotation of {@link #imageRotation}
	 *         degrees.
	 */
	private int getRotatedImageHeight() {
		if (this.imageRotation == 0 || this.imageRotation == 180) {
			return this.image.getHeight();
		} else {
			return this.image.getWidth();
		}
	}

	/**
	 * Rotates the given rectangle around the given center the specified degrees. The degrees must
	 * be 0, 90, 180 or 270.
	 * <p>
	 * The input rectangle is modified.
	 */
	private void rotateRect(int degrees, int x, int y, Rect rect) {
		tileRotationMatrix.reset();
		tileRotationMatrix.preRotate(degrees, x, y);
		floatRect.bottom = rect.bottom;
		floatRect.left = rect.left;
		floatRect.top = rect.top;
		floatRect.right = rect.right;
		tileRotationMatrix.mapRect(floatRect);
		rect.bottom = (int) floatRect.bottom;
		rect.left = (int) floatRect.left;
		rect.right = (int) floatRect.right;
		rect.top = (int) floatRect.top;
	}
}
