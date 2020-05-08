 package mmb.foss.aueb.icong;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import mmb.foss.aueb.icong.boxes.Box;
 import mmb.foss.aueb.icong.boxes.SavedState;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class DrawableAreaView extends View {
 
 	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private ArrayList<Box> boxes = new ArrayList<Box>();
 	private Context mContext;
 	private Box selectedBox = null;
 	private int pressedX, pressedY;
 	private int originalX, originalY;
 	private int[] buttonCenter = new int[2];
 	private int WIDTH, HEIGHT;
 	private ArrayList<BoxButtonPair[]> lines = new ArrayList<BoxButtonPair[]>();
 	private Box box = null;
 	private int buttonPressed = -1;
 	private int buttonHovered = -1;
 	private boolean drawingline = false;
 	private boolean foundPair = false;
 	private int lineStartX, lineStartY, lineCurrentX, lineCurrentY;
 	private long tap;
 	private final int DOUBLE_TAP_INTERVAL = (int) (0.3 * 1000);
 	private BitmapDrawable trash;
 	private boolean showTrash;
 	private int trashX, trashY;
 	private Box possibleTrash;
 
 	public DrawableAreaView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 		mContext = context;
 		paint.setColor(Color.BLACK);
 		WIDTH = MainActivity.width;
 		HEIGHT = MainActivity.height;
 		boxes = SavedState.getBoxes();
 		lines = SavedState.getLines();
 	}
 
 	protected void onDraw(Canvas c) {
 		if (WIDTH == 0 || trash == null) {
 			WIDTH = this.getWidth();
 			HEIGHT = this.getHeight();
 			InputStream is = mContext.getResources().openRawResource(
 					R.drawable.trash);
 			Bitmap originalBitmap = BitmapFactory.decodeStream(is);
 			int w = WIDTH / 10, h = (w * originalBitmap.getHeight())
 					/ originalBitmap.getWidth();
 			trash = new BitmapDrawable(mContext.getResources(),
 					Bitmap.createScaledBitmap(originalBitmap, w, h, true));
 			trashX = (WIDTH - trash.getBitmap().getWidth()) / 2;
 			trashY = HEIGHT - 40;
 		}
 		for (Box box : boxes) {
 			// TODO: Zooming to be removed
 			box.setZoom(1.8);
 			c.drawBitmap(box.getBitmap(), box.getX(), box.getY(), null);
 			for (int i = 0; i < box.getNumOfButtons(); i++) {
 				if (box.isPressed(i)) {
 					buttonCenter = box.getButtonCenter(i);
 					c.drawCircle(buttonCenter[0], buttonCenter[1],
 							box.getButtonRadius(i), paint);
 				}
 			}
 		}
 		for (BoxButtonPair[] line : lines) {
 			Box box0 = line[0].getBox(), box1 = line[1].getBox();
 			int button0 = line[0].getButton(), button1 = line[1].getButton();
 			int[] center0 = box0.getButtonCenter(button0), center1 = box1
 					.getButtonCenter(button1);
 			c.drawLine(center0[0], center0[1], center1[0], center1[1], paint);
 		}
 		if (drawingline) {
 			c.drawLine(lineStartX, lineStartY, lineCurrentX, lineCurrentY,
 					paint);
 		}
 		if (showTrash) {
 
 			c.drawBitmap(trash.getBitmap(), trashX, trashY, paint);
 		}
 	}
 
 	public void addBox(Box box) {
 		int x, y;
 		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			y = getLower() + 15;
 			x = (WIDTH / 2) - (box.getWidth() / 2);
 		} else {
 			y = (HEIGHT / 2) - (box.getHeight() / 2);
 			x = getRighter() + 15;
 		}
 		box.setY(y);
 		box.setX(x);
 		boxes.add(box);
 		SavedState.addBox(box);
 		invalidate();
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			if (showTrash && onTrash(event.getX(), event.getY())) {
 				// if trash icon is visible and clicked delete the possibleTrash
 				// box
 				deleteBox(possibleTrash);
 				possibleTrash = null;
 			}
 			box = getBoxTouched((int) event.getX(), (int) event.getY());
 			if (box != null) {
 				// if we have touched inside a box
 				selectedBox = box;
 				buttonPressed = box.isButton((int) event.getX(),
 						(int) event.getY());
 				// TODO double tap implementation
 				long tap = System.currentTimeMillis();
 				if (System.currentTimeMillis() - this.tap < DOUBLE_TAP_INTERVAL) {
 					// if we have double tapped inside a box
 					System.out.println("this is double tap");
 				} else {
 					System.out.println("this is NOT double tap");
 				}
 				this.tap = tap;
 				if (buttonPressed == -1) {
 					// if we haven't touched the box's button
 					pressedX = (int) event.getX();
 					pressedY = (int) event.getY();
 					originalX = box.getX();
 					originalY = box.getY();
 					showTrash = true;
 					possibleTrash = box;
 				} else {
 					// if we have touched the box's button
 					showTrash = false;
 					possibleTrash = null;
 					if (buttonPressed >= box.getNoOfInputs()) {
 						// if button pressed is an output button
 						if (!box.isPressed(buttonPressed)) {
 							// if the button pressed wasn't pressed before
 							box.setButtonPressed(buttonPressed);
 						} else {
 							// if the button pressed was pressed before deletes
 							// this connection/line
 							removeLine(box,buttonPressed);
 						}
 						int[] center = box.getButtonCenter(buttonPressed);
 						lineStartX = center[0];
 						lineStartY = center[1];
 						lineCurrentX = lineStartX;
 						lineCurrentY = lineStartY;
 						drawingline = true;
 					}
 					invalidate();
 					selectedBox = null;
 				}
 			} else {
 				// if we haven't touched inside a box
 				showTrash = false;
 				possibleTrash = null;
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (selectedBox != null) {
 				// if we have selected a box by tapping once in it
 				selectedBox.setX((int) event.getX() - (pressedX - originalX));
 				selectedBox.setY((int) event.getY() - (pressedY - originalY));
 				invalidate();
 			}
 			if (drawingline) {
 				// if we have pressed a previously not pressed box's output
 				// button
 				lineCurrentX = (int) event.getX();
 				lineCurrentY = (int) event.getY();
 				Box boxHovered = getBoxTouched((int) event.getX(),
 						(int) event.getY());
 				if (boxHovered != null) {
 					// if we have drawned a line on another box
 					buttonHovered = boxHovered.isButton((int) event.getX(),
 							(int) event.getY());
 					if (buttonHovered != -1) {
						// if we have drawned a line on FIXME another's box's
						// button
						if (buttonHovered < boxHovered.getNoOfInputs()) {
 							// if we have drawned a line on another's box's
 							// input button
 							int[] center = boxHovered
 									.getButtonCenter(buttonHovered);
 							lineStartX = center[0];
 							lineStartY = center[1];
 							boxHovered.setButtonPressed(buttonHovered);
 							drawingline = false;
 							BoxButtonPair[] line = {
 									new BoxButtonPair(box, buttonPressed),
 									new BoxButtonPair(boxHovered, buttonHovered) };
 							lines.add(line);
 							SavedState.addLine(line);
 							foundPair = true;
 						}
 					}
 				}
 			}
 			invalidate();
 			break;
 		case MotionEvent.ACTION_UP:
 			drawingline = false;
 			selectedBox = null;
 			// if when drawing a line stops and we haven'd reached another box's
 			// input button then erase the line and unpress the button
 			if (!foundPair && buttonPressed != -1 && box != null)
 				if (!((buttonPressed + 1) <= box.getNoOfInputs()))
 					box.unsetButtonPressed(buttonPressed);
 			foundPair = false;
 			pressedX = pressedY = originalX = originalY = 0;
 			// TODO implement here to pou peftei
 			invalidate();
 			return false;
 		}
 		return true;
 	}
 
 	// returns the lower pixel of the lower element
 	private int getLower() {
 		int y = 0;
 		for (Box box : boxes) {
 			if (y < box.getYY())
 				y = box.getYY();
 		}
 		return y;
 	}
 
 	// returns the righter pixel of the righter element
 	private int getRighter() {
 		int x = 0;
 		for (Box box : boxes) {
 			if (x < box.getXX())
 				x = box.getXX();
 		}
 		return x;
 	}
 
 	// returns the box that was touched
 	private Box getBoxTouched(int x, int y) {
 		for (Box b : boxes) {
 			if (b.isOnBox(x, y)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	private boolean onTrash(float f, float g) {
 		boolean isOnTrash = false;
 		if (f >= trashX && f <= (trashX + trash.getBitmap().getWidth())
 				&& g >= trashY && g <= (trashY + trash.getBitmap().getHeight())) {
 			isOnTrash = true;
 		}
 		return isOnTrash;
 	}
 
 	private void deleteBox(Box box2del) {
 		boxes.remove(box2del);
 		removeLines(box2del);
 		SavedState.removeBox(box2del);
 	}
 
 	private void removeLine(Box box, int button) {
 		BoxButtonPair pair = new BoxButtonPair(box, button);
 		for (BoxButtonPair[] line : lines) {
 			if (line[0].equals(pair)) {
 				Box otherBox = line[1].getBox();
 				int otherButton = line[1].getButton();
 				lines.remove(line);
 				SavedState.removeLine(line);
 				otherBox.unsetButtonPressed(otherButton);
 				break;
 			} else if (line[1].equals(pair)) {
 				Box otherBox = line[0].getBox();
 				int otherButton = line[0].getButton();
 				lines.remove(line);
 				SavedState.removeLine(line);
 				otherBox.unsetButtonPressed(otherButton);
 				break;
 			}
 		}
 	}
 
 	private void removeLines(Box box) {
 		for (int i = 0; i < box.getNumOfButtons(); i++) {
 			removeLine(box, i);
 		}
 	}
 
 }
