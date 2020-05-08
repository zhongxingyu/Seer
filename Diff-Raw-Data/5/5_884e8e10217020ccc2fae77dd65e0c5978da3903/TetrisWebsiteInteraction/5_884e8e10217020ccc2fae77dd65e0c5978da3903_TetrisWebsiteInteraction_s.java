 // This program is responsible for the interaction with the Flash game, i.e.
 // Input: Reading the screen 
 // Output: Applying key strokes for rotation, drop and hold button, mouse clicks 
 // for focusing on the browser window (if necessary)
 
 package TetrisBattleBot;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.MouseInfo;
 
 import java.lang.Math;
 import java.awt.image.BufferedImage;
 
 public class TetrisWebsiteInteraction {
 
   public static Robot robot;
 
   // delay after pressing space if it will 
   // lead to a filled line
   public static int FILL_LINE_DELAY = 50;
   // key press delay for other movements
   public static int KEY_DELAY   = 10;
   // delay between key press and key release
   public static final int KEY_PRESS_KEY_RELEASE_DELAY   = 20;
   // number of milliseconds this robot sleeps
   // after generating an event
   public static final int AUTO_DELAY  = 30;
   // random delay range
   public static final int RAND_DELAY_RANGE = 20;
 
   
   // Types of Tetrominoes
   //
   // 0  **    1 **     2    **   3    *      4  ****   5 *        6   *
   //    **       **        **        ***                 ***        ***
   //
   //  RGB values for current Tetromino
   //  (r,g,b) 
   // 0: 251 184 0
   // 1: 238 26 72
   // 2: 117 208 0
   // 3: 191 47 163
   // 4: 54 174 255
   // 5: 49 69 237
   // 6: 247 106 0
   public static final int[][] CURRENTRGB = {{251,184,0},{238,26,72},{117,208,0},
                                             {191,47,163},{54,174,255},{49,69,237},{247,106,0}};
   //  RGB values for all Tetrominos other than the current one
   //  (r,g,b)
   // 0: 216 144 0
   // 1: 197 0 40
   // 2: 83 167 0
   // 3: 152 14 124
   // 4: 32 134 212
   // 5: 20 36 194
   // 6: 212 71 0
   public static final int[][] OTHERRGB = {{216,144,0},{197,0,40},{83,167,0},
                                           {152,14,124},{32,134,212},{20,36,194},{212,71,0}};
 
   
   public static Rectangle TetrisRect;
 
 
 
   // delay by d + rand(0...RAND_DELAY_RANGE) milliseconds
   public static void delay(int d) {
     robot.delay((int) (Math.random() * RAND_DELAY_RANGE) + d);
   }
   
 
   // countdown for delay (in seconds)
   public static void countdown(int s) {
     for (int i = s; i >= 1; i--) {
       System.out.println(i + "sec...");
       robot.delay(1000);
     }
   }
 
   // define a rectangle given the points pt1 and pt2
   // (which form two corners of the rectangle)
   // required: height > 0, width > 0
   public static Rectangle setRectangle(Point pt1, Point pt2) {
     Rectangle rect = new Rectangle( Math.min(pt1.x, pt2.x),  Math.min(pt1.y, pt2.y),
                                     Math.abs(pt1.x - pt2.x), Math.abs(pt1.y - pt2.y));
 
     System.out.println("set rectangle to " + rect.toString());
     return rect;
   }
   
   // initializes TetrisRect
   // s1 specifies the duration (in seconds) until the cursor must be moved
   // to the first point. s2 specifies the delay (in seconds) between choosing
   // the first and the second point bounding the rectangle
   public static void chooseRectangle(int s1, int s2) {
     System.out.println("Move your cursor to the upper left corner of the Tetris board. ");
     countdown(s1);
     
     Point pt1 = new Point(MouseInfo.getPointerInfo().getLocation());
     System.out.println(pt1.toString());
 
     System.out.println("Move your cursor to the lower right corner of the Tetris board.");
     countdown(s2);	
 
     Point pt2 = new Point(MouseInfo.getPointerInfo().getLocation());
     System.out.println(pt2.toString());
     
     TetrisRect = setRectangle(pt1, pt2);
   }
 	
   // match two rgb triples
   // returns matched type of Tetromino
   // if no match return 0
   public static int matchTetromino(int[] rgb, int[][] rgbvalues) {
     for (int i = 0; i < 7; i++) {
       for (int j = 0; j < 3; j++) {
         if (rgbvalues[i][j] != rgb[j]) break;
         if (j == 2) return i;
       }
     }
     return -1; // no match
   }
     
   // returns red, green, blue values separately in an
   // array
   private static int[] getRGB(BufferedImage Img, int x, int y) {
     int rgb = Img.getRGB(x,y);
     int[] A = new int[3];
     A[0] = (rgb >> 16) & 0xFF;
     A[1] = (rgb >> 8) & 0xFF;
     A[2] = rgb & 0xFF;
     return A;
   }
   
   // detect Tetromino using pixels in rect from screen
   public static int detectTetrominoSimple() {
     BufferedImage Img = robot.createScreenCapture(TetrisRect);
     for (int x = 0; x < TetrisRect.width; x++) {
       for (int y = 0; y <  TetrisRect.height; y++) {
         int ret = matchTetromino(getRGB(Img, x, y), CURRENTRGB);
         if (ret != -1) return ret;
       }
     }
     return -1; // none detected
   }
 
   // detect current Tetris board configuration
   // the different types of the Tetrominos are marked with
   // integers i = 10 through 16, i representing Tetromino type i - 10
   public static TetrisBoard detectTetrisBoard(int[][] rgbvalues, TetrisBoard TB) {
     int[][] A = new int[TetrisBoard.BOARDH][TetrisBoard.BOARDH];
 
     BufferedImage Img = robot.createScreenCapture(TetrisRect);
     int hstep = TetrisRect.width  / TetrisBoard.BOARDW;
     int vstep = TetrisRect.height / TetrisBoard.BOARDH;
     int hmargin = Math.max(hstep / 14, 1);
     int vmargin = Math.max(vstep / 14, 1);
 
     for (int j = 0; j < TetrisBoard.BOARDH; j++) {
       for (int i = 0; i < TetrisBoard.BOARDW; i++) {
         int ret = -1;
         for (int y = vmargin + j * vstep; y < (j+1) * vstep - vmargin; y++) {
           for (int x = hmargin + i * hstep; x < (i+1) * hstep - hmargin; x++) {
             ret = matchTetromino(getRGB(Img, x, y), rgbvalues);
             if (ret != -1) {
               // found Tetromino, break condition for loops
               x = TetrisRect.width;
               y = TetrisRect.height;
             }
           }
         }
         A[j][i] = (ret == -1) ? 0 : ret + 10;
       }
     }
     return new TetrisBoard(A, TetrisBoard.TILEMARKERINIT, TB.score, TB.filledlinebef);
   }
 
   public enum MoveType { 
     UP, DOWN, LEFT, RIGHT, SHIFT, SPACE, CLICK, CTRL
         }
     
 
   // simulate a key press in detail - used in pressKey(..)
   private synchronized static void type(int key) {
     delay(KEY_DELAY);
     robot.keyPress(key);
     delay(KEY_PRESS_KEY_RELEASE_DELAY);
     robot.keyRelease(key);
   }
 
 
   // simulate a key press
   public static void  pressKey(MoveType t) {
 
     try { 
       switch (t) {       
         case UP:    type(KeyEvent.VK_UP);
           break;
         case DOWN:  type(KeyEvent.VK_DOWN);
           break;
         case LEFT:  type(KeyEvent.VK_LEFT);
           break;
         case RIGHT: type(KeyEvent.VK_RIGHT);
           break;
         case SHIFT: type(KeyEvent.VK_SHIFT);
           break;
         case SPACE: type(KeyEvent.VK_SPACE);
           break;
         case CTRL:  type(KeyEvent.VK_CONTROL);
           break;
         case CLICK: // left click
           robot.mousePress(InputEvent.BUTTON1_MASK);
           robot.delay(200);
           robot.mouseRelease(InputEvent.BUTTON1_MASK);
           robot.delay(200);
           break;
       }
 
       System.out.println(t.toString());
       
 	    
     } catch (Throwable trw)
     {
       trw.printStackTrace();
     }
   }
 
     
   public static void doMove(TetrisMove move, int type) {
 
 
     // replace 3 with 2 if CTRL button can be pressed
     // currently it's not working
     // -> deactivated left rotation (CTRL key press)
     if (move.rot <= 3) {
       for (int i = 0; i < move.rot; i++) {
         pressKey(MoveType.UP);
       }
     } 
     else {
       pressKey(MoveType.CTRL);
     }
 
     int pos; // default starting position
     if (type == 0) pos = TetrisBoard.LMARGIN + 4;
     else pos = TetrisBoard.LMARGIN + 3;
 	
     if (move.pos >= pos) {
       for (int i = pos; i < move.pos; i++) {
         pressKey(MoveType.RIGHT);
       }
     }
     else {
       for (int i = pos; i > move.pos; i--) {
         pressKey(MoveType.LEFT);
       }
     }
 
     pressKey(MoveType.SPACE);
     robot.delay(FILL_LINE_DELAY);  // extra delay after pressing "SPACE", at the beginning
   }
 
   // initialize robot
   public static void initRobot() {
     try { 
       robot = new Robot();
     }
     catch (AWTException e) {
       System.out.println("Error while constructing Robot object");
     }
     // sets the number of milliseconds this robot sleeps after
     // generating an event.
     robot.setAutoDelay(AUTO_DELAY);
     // sets whether this robot automatically invokes waitForIdle 
     // after generating an event.
     robot.setAutoWaitForIdle(true);
   }
 
 
   
   public static void main(String[] args) throws AWTException {
        
     // initialize robot
     initRobot();

     // initialize the Tetris board
     TetrisBoard currBoard = new TetrisBoard();
 
     // get rectangle TetrisRect specifying the Tetris board
     chooseRectangle(3, 2);
 
     // we only look at the Tetris board for
     // determining the current Tetromino, the
     // "next" window is ignored
     int type;
     for (int t = 1; ; t++) {
       // detect current tetromino
       type = detectTetrominoSimple();
       if (type == -1) {
         System.out.println("none detected");
         continue;
       }
 
       TetrisMove move = TetrisStrategy.computeBestMove(currBoard, type);
       // do the move and update the board accordingly
       doMove(move, type); // <-- contains small Feedback
       currBoard.dropTetromino(type, move.rot, move.pos, currBoard.tilemarker++);
       currBoard.clearLinesUpdateHeight();
 
 
       // large Feedback
       int diff = currBoard.verifyCorrect(detectTetrisBoard(OTHERRGB, currBoard));
       
       currBoard.printFullBoard();   // only debug
       if (diff > 0) {
         System.out.println("large Feedback loop: ERROR occured - diff = " + diff); // only debug
 
         // adaptive delay controller
         /*
         if (diff > 5) 
           FILL_LINE_DELAY += (int)  ((double) FILL_LINE_DELAY * 0.1);
         else
           KEY_DELAY += (int)  ((double) KEY_DELAY * 0.1);
         System.out.println("Current delays: " + FILL_LINE_DELAY + " and " + KEY_DELAY);
         */
       }
       
     }
   }
 }
