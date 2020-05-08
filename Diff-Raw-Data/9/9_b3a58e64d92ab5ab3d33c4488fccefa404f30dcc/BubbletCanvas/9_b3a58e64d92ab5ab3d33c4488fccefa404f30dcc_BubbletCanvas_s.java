 
 import graviton.observerPattern.Observable;
 import graviton.observerPattern.ObserverManager;
 import highscore.HighScoreManager;
 import highscore.IHighScoreSubject;
 
 import java.util.Random;
 import java.util.Vector;
 
 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 
 public class BubbletCanvas extends Canvas 
 							implements IHighScoreSubject, 
 										CommandListener, 
 										Observable {
     private int[][] field;
     private int score;
     private int screenWidth;
     private int screenHeight;
 
     private int cellWidth;
     private int cellHeight; 
 
     private int fieldWidth;
     private int fieldHeight;
 
     private int cursorIndexPosX;
     private int cursorIndexPosY;
 
     private Vector affectedSlices = new Vector(10);
     private Vector CellSet = new Vector(20);
 
     private Random rand = new Random(System.currentTimeMillis());
 
     private final int RED = 0;
     private final int GREEN = 1;
     private final int BLUE = 2;
     private final int PURPLE = 3;
     private final int YELLOW = 4;
     private final int TURQUOISE = 5;
     private final int WHITE = 6;
     private final int MARKED = 7;
 
     private boolean initialDraw = true;
     private boolean drawWholeScreen = false;
     private boolean updateCrosshairsOnly = false;
     private boolean gameFinished = false;
 
     private MIDlet bubblet;
     private Command exitCmd;
     private Command startCmd;
     private Command instructCmd;
     private Command backCmd;
     private Command aboutCmd;
     private Command highScoreCmd;
     
     private Form instructionForm;
     private Form aboutForm;
     private Form highScoreForm;
     
     private StringItem instructStringItem;
     private StringItem aboutStringItem;
     private StringItem highScoreStringItem;
     
     private ObserverManager observerManager;
     private HighScoreManager highScore;
 
     public BubbletCanvas(MIDlet pMidlet, int pFieldWidth, int pFieldHeight) {
         highScore = HighScoreManager.getInstance(5,false,"bubblet");
         observerManager = new ObserverManager();
         observerManager.addObserver(highScore);
         
         // Internal self reference for CommandListeners
         final Displayable bubbletCanvas = this;
 
         // Prepare Instructions Screen
         backCmd = new Command("Back", Command.SCREEN, 1);
         instructionForm = new Form("Instructions");
         instructStringItem = new StringItem(
                 "Bubblet Instructions",
                 "Search for contiguous cells (two or more) " +
                 "with the same color and touch them to dissolve them. " +
                 "Try to dissolve as many fields as possible.");
         instructionForm.append(instructStringItem);
         instructionForm.addCommand(backCmd);
         instructionForm.setCommandListener(new CommandListener() {
             public void commandAction(Command c, Displayable s) {
                 if (c == backCmd) {
                     updateCrosshairsOnly = false;
                     CellSet.removeAllElements();
                     drawWholeScreen = true;
                     ((Bubblet) bubblet).getDisplay().setCurrent(bubbletCanvas);
                 }
             }
         });
 
         // Prepare the About Screen
         aboutForm = new Form("About");
         aboutStringItem = new StringItem(
                 "About Bubblet",
                 "This game was originally written by Juan Antonio Agudo at http://keyboardsamurais.de \n" +
                 "It's licensed under GPLv2 licence \n"+
                 "Ported to S40 by: Antti Pohjola\n"+
                 "You can get the source code from: https://github.com/Summeli/Bubblet \n\n" +
                 "- it was strongly inspired by the original Bubblet at http://oopdreams.com " +
                 "and its Windows port by Daniel Klein at http://www.hobsoft.de. " +
                 "Special thanks go to M. Serhat Cinar at http://graviton.de for his kind advice.");
         aboutForm.append(aboutStringItem);
         aboutForm.addCommand(backCmd);
         aboutForm.setCommandListener(new CommandListener() {
             public void commandAction(Command c, Displayable s) {
                 if (c == backCmd) {
                     updateCrosshairsOnly = false;
                     CellSet.removeAllElements();
                     drawWholeScreen = true;
                     ((Bubblet) bubblet).getDisplay().setCurrent(bubbletCanvas);
                 }
             }
         });
 
         // prepare HighScore Screen
 
         highScoreForm= new Form("HighScore");
         highScoreForm.addCommand(backCmd);
         highScoreForm.setCommandListener(new CommandListener() {
             public void commandAction(Command c, Displayable s) {
                 if (c == backCmd) {
 		            score = 0;
 		            gameFinished = false;
 		            initialDraw = true;
 		            repaint();
 		            ((Bubblet) bubblet).getDisplay().setCurrent(bubbletCanvas);
                 }
             }
         });
 
         // Set the Display of our app
 
         bubblet = pMidlet;
 
         startCmd = new Command("Start", Command.SCREEN, 1);
         instructCmd = new Command("Instructions", Command.SCREEN, 2);
         aboutCmd = new Command("About", Command.SCREEN, 3);
         highScoreCmd = new Command("Highscore", Command.SCREEN, 4);
         exitCmd = new Command("Exit", Command.SCREEN, 5);
 
         addCommand(startCmd);
         addCommand(instructCmd);
         addCommand(aboutCmd);
         addCommand(highScoreCmd);
         addCommand(exitCmd);
         setCommandListener(this);
 
         fieldWidth = pFieldWidth;
         fieldHeight = pFieldHeight;
         field = new int[pFieldWidth][pFieldHeight];
     }
 
 
     protected void drawCrosshairs(Graphics g) {
 	int x = cursorIndexPosX * cellWidth;
 	int y = cursorIndexPosY * cellHeight;
 
 	int x1 = x + 2;
 	int y1 = y + 2;
 	int x2 = x + cellWidth - 5;
 	int y2 = y + cellHeight - 6;
 
 	// Draw Crosshairs
 	g.setColor(0, 0, 0);
 	g.drawLine(x1, y1, x2, y2);
 	g.drawLine(x2, y1, x1, y2);
     }
 
     protected void paint(Graphics g) {
         if (initialDraw) {
             // initialize sizes of basic screen elements (only once)
 	    screenWidth = g.getClipHeight();
             screenHeight = g.getClipWidth();
             cellWidth = screenWidth / fieldWidth;
             cellHeight = screenHeight / fieldHeight;
 
             // Draw blank screen
             g.setColor(255, 255, 255);
             g.fillRect(0, 0, screenWidth, screenHeight);
 
             // initialize random game field
             for (int i = 0; i < fieldWidth; i++) {
                 for (int j = 0; j < fieldHeight; j++) {
                     field[i][j] = getRandomNumber();
                 }
             }
 //            int [][] field =  {{0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,6,6,6,6,6 },
 //                               {0,0,0,0,6,1,1,1,6,0 },
 //                               {0,0,0,0,6,1,0,1,6,0 },
 //                               {0,0,0,0,1,1,0,1,1,1 }};
 //            this.field = field;
             // Draw all cells
             drawWholeBoard(g);
 
             // Initialize crosshairs coordinates on first paint() call
             cursorIndexPosX = 0;
             cursorIndexPosY = 0;
 
             initialDraw = false;
 
         } else if (updateCrosshairsOnly && CellSet.size() > 0) {
             // Redraw last field that was touched by the crosshair
             CellBlock crosshairCell = (CellBlock) CellSet.firstElement();
             drawField(g, crosshairCell.xval, crosshairCell.yval);
             CellSet.removeAllElements();
 
             updateCrosshairsOnly = false;
         } else if (gameFinished) {
             // Draw blank screen
             observerManager.notifyObservers(this);
 	        highScoreStringItem = new StringItem("Your score is: "+score+" Highscores: ",highScore.getSimpleList());
 			if(highScoreForm.size() == 0){
 				highScoreForm.append(highScoreStringItem);
 			}else{
 		        highScoreForm.set(0,highScoreStringItem);
 			}
 	        ((Bubblet) bubblet).getDisplay().setCurrent(highScoreForm);
 	        return;
         }else if(drawWholeScreen){
             g.setColor(255, 255, 255);
             g.fillRect(0, 0, screenWidth * 2, screenHeight * 2);
             drawWholeBoard(g);
         } else {
             // draw board after the dissolve of some fields
             for (int i = 0; i < affectedSlices.size(); i++) {
                 for (int j = 0; j < fieldHeight; j++) {
                     drawField(g, ((Integer) affectedSlices.elementAt(i)).intValue(), j);
                 }
             }
             affectedSlices.removeAllElements();
         }
 
         if (!gameFinished)
 	    drawCrosshairs(g);
     }
 
     private void drawWholeBoard(Graphics g){
         for (int y = 0; y < fieldWidth; y++) {
             for (int x = 0; x < fieldHeight; x++) {
                 drawField(g, x, y);
             }
         }
     }
     
     private void drawField(Graphics g, int ix, int iy) {
         // Determine cell color
         switch (field[iy][ix]) {
         case RED:
             g.setColor(255, 0, 0);
             break;
         case GREEN:
             g.setColor(0, 255, 0);
             break;
         case BLUE:
             g.setColor(0, 0, 255);
             break;
         case PURPLE:
             g.setColor(255, 0, 255);
             break;
         case YELLOW:
             g.setColor(255, 255, 0);
             break;
         case TURQUOISE:
             g.setColor(0, 255, 255);
             break;
         case WHITE:
             g.setColor(255, 255, 255);
             break;
         }
 
 	int x = cellWidth * ix;
 	int y = cellHeight *iy;
 
         // Draw cell
         if (field[iy][ix] == WHITE) {
 	    g.fillRect(x, y, cellWidth, cellHeight);
         } else {
             g.fillRect(x, y, cellWidth - 3, cellHeight - 4);
             g.setColor(0, 0, 0);
             g.drawRect(x, y, cellWidth - 3, cellHeight - 4);
         }
     }
 
     public void pointerPressed(int x, int y){
     	cursorIndexPosY = y / cellHeight;
     	cursorIndexPosX = x / cellWidth;
 
         int selectedColor = field[cursorIndexPosY][cursorIndexPosX];
         // Is it white? then do nothing at all
         if (selectedColor != WHITE) {
             // Find neighboring cells with same colour in x and y direction
             gatherColoredNeighbors(new CellBlock(cursorIndexPosX, cursorIndexPosY), selectedColor);
 
             score += (CellSet.size() * CellSet.size());
             CellBlock cell;
 
             // mark all erased cells
             for (int i = 0; i < CellSet.size(); i++) {
                 cell = (CellBlock) CellSet.elementAt(i);
                 field[cell.yval][cell.xval] = MARKED;
             }
 
             // make cells fall
             while (CellSet.size() > 0 && (cell = (CellBlock) CellSet.firstElement()) != null) {
                 affectedSlices.addElement(new Integer(cell.xval));
                 // current element must not be iterated twice
                 CellSet.removeElement(cell);
 
                 int startposY = cell.yval;
                 int yPosUp = startposY;
                 int yPosDown = startposY;
                 int uppermostY = startposY; // Describes the highest element
                 // of the continuous white cells
 
                 int lowermostY = startposY; // Describes the lowest element
                 // of the continuous white cells
 
                 boolean sliceUpWardsInterruped = false;
                 boolean sliceDownWardsInterruped = false;
                 while (yPosDown < fieldHeight || yPosUp >= 0) {
                     // first we gather the uppermost and lowermost affected
                     // element then we remove all elements from the CellSet
                     if (--yPosUp >= 0 && field[yPosUp][cell.xval] == MARKED && !sliceUpWardsInterruped) {
                         if (field[yPosUp][cell.xval] != MARKED) {
                             sliceUpWardsInterruped = true;
                         } else {
                             uppermostY = yPosUp;
                             CellBlock c = new CellBlock(cell.xval, yPosUp);
                             CellSet.removeElement(c);
                         }
                     } else {
                         sliceUpWardsInterruped = true;
                     }
                     if (++yPosDown < fieldHeight && field[yPosDown][cell.xval] == MARKED
                             && !sliceDownWardsInterruped) {
                         if (field[yPosDown][cell.xval] != MARKED) {
                             sliceDownWardsInterruped = true;
                         } else {
                             lowermostY = yPosDown;
                             CellSet.removeElement(new CellBlock(cell.xval, yPosDown));
                         }
                     } else {
                         sliceDownWardsInterruped = true;
                     }
                     if (sliceUpWardsInterruped && sliceDownWardsInterruped) {
                         break;
                     }
                 }
                 multiArrayMoveSliceY(uppermostY, lowermostY, cell.xval);
             }
             CellSet.removeAllElements();
 
             // move empty slices to the left corner
             
             for (int i = 0; i < fieldWidth - 1; i++) {
                 if (field[i][fieldHeight - 1] == WHITE) {
                     moveSliceLeft(i);
                     break;
                 }
             }
         }
 
         gameFinished = isGameFinished();
 
     // draw new data
     repaint();
     	
     }
     public void keyPressed(int pKeyCode) {
         if (gameFinished) {
             return;
         }
 
         // if
         if (pKeyCode != Canvas.KEY_NUM5) {
             updateCrosshairsOnly = true;
             CellSet.addElement(new CellBlock(cursorIndexPosX, cursorIndexPosY));
         }
 
         // preserve debug message
         if (Canvas.KEY_NUM7 != pKeyCode) {
             // Translate native keycodes to standard codes
             pKeyCode = getGameAction(pKeyCode);
         }
         switch (pKeyCode) {
         
         //        case -1:
         case Canvas.UP:
             //        case Canvas.KEY_NUM2: // up
             if (cursorIndexPosY > 0)
                 cursorIndexPosY--;
             break;
         //        case -2:
         case Canvas.DOWN:
             //        case Canvas.KEY_NUM8: // down
             if (cursorIndexPosY < 9)
                 cursorIndexPosY++;
             break;
         //        case -3:
         case Canvas.LEFT:
             //        case Canvas.KEY_NUM4: // left
             if (cursorIndexPosX > 0)
                 cursorIndexPosX--;
             break;
         //        case -4:
         case Canvas.RIGHT:
             //        case Canvas.KEY_NUM6: // right
             if (cursorIndexPosX < 9)
                 cursorIndexPosX++;
             break;
         case Canvas.KEY_NUM7: // debug key - draws whole screen anew
             updateCrosshairsOnly = false;
             CellSet.removeAllElements();
             drawWholeScreen = true;
 //            drawWholeBoard();
             break;
         case Canvas.FIRE: // fire
             int selectedColor = field[cursorIndexPosY][cursorIndexPosX];
             // Is it white? then do nothing at all
             if (selectedColor != WHITE) {
                 // Find neighboring cells with same colour in x and y direction
                 gatherColoredNeighbors(new CellBlock(cursorIndexPosX, cursorIndexPosY), selectedColor);
 
                 score += (CellSet.size() * CellSet.size());
                 CellBlock cell;
 
                 // mark all erased cells
                 for (int i = 0; i < CellSet.size(); i++) {
                     cell = (CellBlock) CellSet.elementAt(i);
                     field[cell.yval][cell.xval] = MARKED;
                 }
 
                 // make cells fall
                 while (CellSet.size() > 0 && (cell = (CellBlock) CellSet.firstElement()) != null) {
                     affectedSlices.addElement(new Integer(cell.xval));
                     // current element must not be iterated twice
                     CellSet.removeElement(cell);
 
                     int startposY = cell.yval;
                     int yPosUp = startposY;
                     int yPosDown = startposY;
                     int uppermostY = startposY; // Describes the highest element
                     // of the continuous white cells
 
                     int lowermostY = startposY; // Describes the lowest element
                     // of the continuous white cells
 
                     boolean sliceUpWardsInterruped = false;
                     boolean sliceDownWardsInterruped = false;
                     while (yPosDown < fieldHeight || yPosUp >= 0) {
                         // first we gather the uppermost and lowermost affected
                         // element then we remove all elements from the CellSet
                         if (--yPosUp >= 0 && field[yPosUp][cell.xval] == MARKED && !sliceUpWardsInterruped) {
                             if (field[yPosUp][cell.xval] != MARKED) {
                                 sliceUpWardsInterruped = true;
                             } else {
                                 uppermostY = yPosUp;
                                 CellBlock c = new CellBlock(cell.xval, yPosUp);
                                 CellSet.removeElement(c);
                             }
                         } else {
                             sliceUpWardsInterruped = true;
                         }
                         if (++yPosDown < fieldHeight && field[yPosDown][cell.xval] == MARKED
                                 && !sliceDownWardsInterruped) {
                             if (field[yPosDown][cell.xval] != MARKED) {
                                 sliceDownWardsInterruped = true;
                             } else {
                                 lowermostY = yPosDown;
                                 CellSet.removeElement(new CellBlock(cell.xval, yPosDown));
                             }
                         } else {
                             sliceDownWardsInterruped = true;
                         }
                         if (sliceUpWardsInterruped && sliceDownWardsInterruped) {
                             break;
                         }
                     }
                     multiArrayMoveSliceY(uppermostY, lowermostY, cell.xval);
                 }
                 CellSet.removeAllElements();
 
                 // move empty slices to the left corner
                 for (int i = 0; i < fieldWidth - 1; i++) {
                     if (field[fieldHeight - 1][i] == WHITE) {
                         moveSliceLeft(i);
                         break;
                     }
                 }
             }
 
             gameFinished = isGameFinished();
 
         }// END SWITCH
 
         // draw new data
         repaint();
     }
 
     private void moveSliceLeft(int x) {
         if (x > fieldWidth) {
             return;
         }
         Vector storage = new Vector(10);
         Vector slice;
 
         for (int i = x; i < fieldHeight; i++) {
             slice = new Vector(10);
             for (int j = 0; j < fieldWidth; j++) {
                 int tmp = field[j][i];
                 if (tmp != WHITE) {
                     slice.addElement(new CellBlock(i, j, field[j][i]));
                     field[j][i] = WHITE;
                 } else {
                     continue;
                 }
             }
             if (slice.size() > 0) {
                 storage.addElement(slice);
             }
             affectedSlices.addElement(new Integer(i));
         }
 
         for (int i = 0; i < storage.size(); i++) {
             slice = (Vector) storage.elementAt(i);
 
             for (int j = 0; j < slice.size(); j++) {
                 CellBlock c = (CellBlock) slice.elementAt(j);
                 field[c.yval][x + i] = c.color;
             }
         }
 
     }
 
     protected void showNotify() {
         drawWholeScreen = true;
     }
 
     private void multiArrayMoveSliceY(int uppermostY, int lowermostY, int xIndex) {
         // copy above cells to downside
         int distance = (lowermostY - uppermostY) + 1;
         for (int i = lowermostY; i >= 0; i--) {
 
             int sourceField = i - distance;
             if (sourceField >= 0) {
                 field[i][xIndex] = field[sourceField][xIndex];
             } else {
                 field[i][xIndex] = WHITE;
             }
         }
     }
 
     private void printArraySlice(int xIndex) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < 10; i++) {
             sb.append(field[i][xIndex] + "|");
         }
         System.out.println(sb);
     }
 
     public boolean isGameFinished() {
         boolean noMoreContiguousBlocks = true;
         for (int i = 0; i < fieldHeight; i++) {
             for (int j = 0; j < fieldWidth; j++) {
                 if (field[j][i] != WHITE && hasNeighbors(new CellBlock(i, j, field[j][i]))) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private boolean hasNeighbors(CellBlock pCell) {
         Vector v = new Vector(10);
         // check for cell with same colour above the current cell
         if (pCell.yval > 0 && field[pCell.yval - 1][pCell.xval] == pCell.color) {
             return true;
         }
         // check for cell with same colour below the current cell
         if (pCell.yval < fieldWidth) {
             if (pCell.yval + 1 < fieldWidth) {
                 if (field[pCell.yval + 1][pCell.xval] == pCell.color) {
                     return true;
                 }
             }
         }
         // check for cell with same colour right from the current cell
         if (pCell.xval > 0 && field[pCell.yval][pCell.xval - 1] == pCell.color) {
             return true;
         }
         // check for cell with same colour right from the current cell
         if (pCell.xval < fieldHeight) {
             if (pCell.xval + 1 < fieldHeight) {
                 if (field[pCell.yval][pCell.xval + 1] == pCell.color) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     // recursive method, that gathers information about
     // contiguous spaces of the same color
     private void gatherColoredNeighbors(CellBlock pCell, int pColor) {
         Vector v = new Vector(10);
         // check for cell with same colour above the current cell
         if (pCell.yval > 0 && field[pCell.yval - 1][pCell.xval] == pColor) {
             CellBlock c = new CellBlock(pCell.xval, pCell.yval - 1);
             if (!CellSet.contains(c)) {
                 v.addElement(c);
                 CellSet.addElement(c);
             }
         }
         // check for cell with same colour below the current cell
         if (pCell.yval < fieldWidth) {
             if (pCell.yval + 1 < fieldWidth) {
                 if (field[pCell.yval + 1][pCell.xval] == pColor) {
                     CellBlock c = new CellBlock(pCell.xval, pCell.yval + 1);
                     if (!CellSet.contains(c)) {
                         v.addElement(c);
                         CellSet.addElement(c);
                     }
                 }
             }
         }
 
         // check for cell with same colour right from the current cell
         if (pCell.xval > 0 && field[pCell.yval][pCell.xval - 1] == pColor) {
             CellBlock c = new CellBlock(pCell.xval - 1, pCell.yval);
             if (!CellSet.contains(c)) {
                 v.addElement(c);
                 CellSet.addElement(c);
             }
         }
         // check for cell with same colour right from the current cell
         if (pCell.xval < fieldHeight) {
             if (pCell.xval + 1 < fieldHeight) {
                 if (field[pCell.yval][pCell.xval + 1] == pColor) {
                     CellBlock c = new CellBlock(pCell.xval + 1, pCell.yval);
                     if (!CellSet.contains(c)) {
                         v.addElement(c);
                         CellSet.addElement(c);
                     }
                 }
             }
         }
 
         // recursive call for all cells of the same colour that were found
         for (int i = 0; i < v.size(); i++) {
             gatherColoredNeighbors((CellBlock) v.elementAt(i), pColor);
         }
         
         // if the size of CellSet equals one it means, that there is only 
         // one cell, and no neighbours of that color, so nothing can be dissolved
 		if(CellSet.size() == 1){
 			CellSet.removeAllElements();
 		}
     }
 
     private int getRandomNumber() {
         int diff = rand.nextInt();
         if (diff < 0)
             diff *= -1;
         return diff % 6;
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < fieldWidth; i++) {
             for (int j = 0; j < fieldHeight; j++) {
                 sb.append(field[i][j] + " ");
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
     // representation of a x and y coordinate tuple
     private class CellBlock {
         int xval;
 
         int yval;
 
         int color;
 
         public CellBlock(int x, int y) {
             this.xval = x;
             this.yval = y;
         }
 
         public CellBlock(int x, int y, int color) {
             this.xval = x;
             this.yval = y;
             this.color = color;
         }
 
         public boolean equals(Object o) {
             if (!(o instanceof CellBlock)) {
                 return false;
             }
             if (o == this) {
                 return true;
             }
             CellBlock c = (CellBlock) o;
             if (c.xval == xval && c.yval == yval) {
                 return true;
             }
             return false;
         }
 
         public String toString() {
             return "X" + xval + "/Y" + yval;
         }
     }
 
     public void commandAction(Command c, Displayable d) {
         if (c == exitCmd) {
             Bubblet b = (Bubblet) bubblet;
             try {
                 b.destroyApp(false);
             } catch (MIDletStateChangeException e) {
                 throw new RuntimeException(e.getMessage());
             }
             b.notifyDestroyed();
         } else if (c == startCmd) {
             score = 0;
             gameFinished = false;
             initialDraw = true;
             repaint();
         } else if (c == instructCmd) {
             ((Bubblet) bubblet).getDisplay().setCurrent(instructionForm);
         } else if (c == aboutCmd) {
             ((Bubblet) bubblet).getDisplay().setCurrent(aboutForm);
         }else if( c == highScoreCmd){
             highScoreStringItem = new StringItem("Your current score is: "+score+" Highscores: ",highScore.getSimpleList());
 			if(highScoreForm.size() == 0){
 				highScoreForm.append(highScoreStringItem);
 			}else{
 		        highScoreForm.set(0,highScoreStringItem);
 			}
             ((Bubblet) bubblet).getDisplay().setCurrent(highScoreForm);
         }
     }
     public int getScore (){
         return score;
     }
     public String getPlayerName(){
         return null;
     }
     public ObserverManager getObserverManager() {
         return observerManager;
     }
 
 }
