 package grid;
 
 import graphics.AcceleratedImage;
 import graphics.ButtonListener;
 import graphics.Tooltip;
 import graphics.Tooltip.TooltipTheme;
 import image.ImageLoader;
 import io.Listener;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.LinearGradientPaint;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.RoundRectangle2D;
 
 /**
  * Implements a Selection pane on top of a {@link Grid} object.
  * That is, this class contains the ability for the user to create selections on the {@link Grid}
  *  by dragging the mouse.
  * A selection keeps track of the area selected and also contains a toolbar shown to the right of
  *  the selection which allows the user to manipulate the selection.
  * 
  * @author zirbinator
  */
 public class Selection implements Runnable
 {
     private static AcceleratedImage saveImg;
     private static AcceleratedImage copyImg;
     private static AcceleratedImage rotateCWImg;
     private static AcceleratedImage rotateCCWImg;
     private static AcceleratedImage clearImg;
     private static AcceleratedImage squareImg;
     private static AcceleratedImage circleImg;
     private static AcceleratedImage slideInImg;
     private static AcceleratedImage slideOutImg;
     private static AcceleratedImage closeImg;
     
     private static boolean imagesLoaded = false;
     private boolean selecting;
     private boolean created;
     private ButtonListener save;
     private ButtonListener copy;
     private ButtonListener rotateCW;
     private ButtonListener rotateCCW;
     private ButtonListener clear;
     private ButtonListener square;
     private ButtonListener circle;
     private ButtonListener minimize;
     private ButtonListener close;
     
     private static final Color selectionBorderColor = Color.black;
     private static final Color selectionColor = new Color(219, 198, 35);
     
     private Direction directionHeld;
     private double toolbarPos;
     
     private Grid grid;
     
     private static final int handleSize = 20;
     private static final int toolbarWidth = 295;
     private static final int toolbarHeight = 55;
     private static final int minToolbarPos = 35;
     
     private static final long toolbarSlideTime = 175;
     
     private Rectangle2D selection;
     private Point dragOrigin;
     private static final Rectangle savePos      = new Rectangle(10,  10, 35, 35);
     private static final Rectangle copyPos      = new Rectangle(55,  10, 35, 35);
     private static final Rectangle rotateCWPos  = new Rectangle(100, 10, 35, 35);
     private static final Rectangle rotateCCWPos = new Rectangle(145, 10, 35, 35);
     private static final Rectangle clearPos     = new Rectangle(190, 10, 35, 35);
     private static final Rectangle squarePos    = new Rectangle(235, 10, 15, 15);
     private static final Rectangle circlePos    = new Rectangle(235, 30, 15, 15);
     private static final Rectangle minimizePos  = new Rectangle(270, 10, 15, 15);
     private static final Rectangle closePos     = new Rectangle(270, 30, 15, 15);
     
     private ToolbarState toolbarState;
     private Tooltip saveTooltip;
     private Tooltip copyTooltip;
     private Tooltip rotateCWTooltip;
     private Tooltip rotateCCWTooltip;
     private Tooltip clearTooltip;
     private Tooltip squareTooltip;
     private Tooltip circleTooltip;
     private Tooltip minimizeTooltip;
     private Tooltip closeTooltip;
     
     /**
      * Creates a new Selection on top of the given {@link Grid}.
      * 
      * @param grid - the Grid object of which this Selection would select cells
      */
     public Selection(Grid grid)
     {
         this.grid = grid;
         selecting = false;
         created = false;
         selection = null;
         directionHeld = Direction.NONE;
         toolbarPos = 0;
         toolbarState = ToolbarState.IN;
         
         if (!imagesLoaded)
         {
             saveImg = ImageLoader.load("save");
             copyImg = ImageLoader.load("copy");
             rotateCWImg = ImageLoader.load("rotate_cw");
             rotateCCWImg = ImageLoader.load("rotate_ccw");
             clearImg = ImageLoader.load("clear");
             squareImg = ImageLoader.load("square");
             circleImg = ImageLoader.load("circle");
             slideInImg = ImageLoader.load("slide_in");
             slideOutImg = ImageLoader.load("slide_out");
             closeImg = ImageLoader.load("close_small");
             
             imagesLoaded = true;
         }
         
         try
         {
             save      = new ButtonListener(null, "save",      this);
             copy      = new ButtonListener(null, "copy",      this);
             rotateCW  = new ButtonListener(null, "rotateCW",  this);
             rotateCCW = new ButtonListener(null, "rotateCCW", this);
             clear     = new ButtonListener(null, "clear",     this);
             square    = new ButtonListener(null, "square",    this);
             circle    = new ButtonListener(null, "oval",      this);
             minimize  = new ButtonListener(null, "minimize",  this);
             close     = new ButtonListener(null, "close",     this);
         }
         catch (NoSuchMethodException ex)
         {
             ex.printStackTrace();
         }
         
         TooltipTheme theme = new TooltipTheme();
         saveTooltip = new Tooltip("Save as a Pattern [^S]", null, theme);
         copyTooltip = new Tooltip("Copy [^C]", null, theme);
         rotateCWTooltip = new Tooltip("Rotate Clockwise [^R]", null, theme);
         rotateCCWTooltip = new Tooltip("Rotate Counterclockwise [^Shift-R]", null, theme);
         clearTooltip = new Tooltip("Clear [^D]", null, theme);
         squareTooltip = new Tooltip("Create Square", null, theme);
         circleTooltip = new Tooltip("Create Oval", null, theme);
         minimizeTooltip = new Tooltip("Minimize Toolbar", null, theme);
         closeTooltip = new Tooltip("Close Selection", null, theme);
         
         Listener.requestNotification(this, "keyReleased", Listener.TYPE_KEY_RELEASED);
         
         new Thread(this).start();
     }
     
     /**
      * Runs the Selection in its own thread by constantly updating the toolbar.
      * 
      * @see Runnable#run()
      */
     public void run()
     {
         long lastUpdate = System.nanoTime();
         while (true)
         {
             long elapsed = (System.nanoTime() - lastUpdate)/1000000;
             if (selection != null && !selecting)
             {
                 if (toolbarState == ToolbarState.MOVING_IN)
                 {
                     toolbarPos -= (double)elapsed/toolbarSlideTime;
                     if (toolbarPos <= (double)minToolbarPos/toolbarWidth)
                     {
                         toolbarPos = (double)minToolbarPos/toolbarWidth;
                         toolbarState = ToolbarState.IN;
                         
                         save.setOn(false);
                         copy.setOn(false);
                         rotateCW.setOn(false);
                         rotateCCW.setOn(false);
                         clear.setOn(false);
                         square.setOn(false);
                         circle.setOn(false);
                     }
                 }
                 else if (toolbarState == ToolbarState.MOVING_OUT)
                 {
                     toolbarPos += (double)elapsed/toolbarSlideTime;
                     if (toolbarPos >= 1)
                     {
                         toolbarPos = 1;
                         toolbarState = ToolbarState.OUT;
                         
                         save.setOn(true);
                         copy.setOn(true);
                         rotateCW.setOn(true);
                         rotateCCW.setOn(true);
                         clear.setOn(true);
                         square.setOn(true);
                         circle.setOn(true);
                     }
                 }
             }
             
             try
             {
                 Rectangle toolbar = getToolbar();
                 
                 save.setButton(new Rectangle(
                         toolbar.x + savePos.x - toolbarWidth + toolbar.width,
                         toolbar.y + savePos.y, savePos.width, savePos.height));
                 copy.setButton(new Rectangle(
                         toolbar.x + copyPos.x - toolbarWidth + toolbar.width,
                         toolbar.y + copyPos.y, copyPos.width, copyPos.height));
                 rotateCW.setButton(new Rectangle(
                         toolbar.x + rotateCWPos.x - toolbarWidth + toolbar.width,
                         toolbar.y + rotateCWPos.y, rotateCWPos.width, rotateCWPos.height));
                 rotateCCW.setButton(new Rectangle(
                         toolbar.x + rotateCCWPos.x - toolbarWidth + toolbar.width,
                         toolbar.y + rotateCCWPos.y, rotateCCWPos.width, rotateCCWPos.height));
                 clear.setButton(new Rectangle(
                         toolbar.x + clearPos.x - toolbarWidth + toolbar.width,
                         toolbar.y + clearPos.y, clearPos.width, clearPos.height));
                 square.setButton(new Rectangle(
                         toolbar.x + squarePos.x - toolbarWidth + toolbar.width,
                         toolbar.y + squarePos.y, squarePos.width, squarePos.height));
                 circle.setButton(new Rectangle(
                         toolbar.x + circlePos.x - toolbarWidth + toolbar.width,
                         toolbar.y + circlePos.y, circlePos.width, circlePos.height));
                 minimize.setButton(new Rectangle(
                         toolbar.x + minimizePos.x - toolbarWidth + toolbar.width,
                         toolbar.y + minimizePos.y, minimizePos.width, minimizePos.height));
                 close.setButton(new Rectangle(
                         toolbar.x + closePos.x - toolbarWidth + toolbar.width,
                         toolbar.y + closePos.y, closePos.width, closePos.height));
                 
                 saveTooltip.setHoverArea(save.getButton());
                 copyTooltip.setHoverArea(copy.getButton());
                 rotateCWTooltip.setHoverArea(rotateCW.getButton());
                 rotateCCWTooltip.setHoverArea(rotateCCW.getButton());
                 clearTooltip.setHoverArea(clear.getButton());
                 squareTooltip.setHoverArea(square.getButton());
                 circleTooltip.setHoverArea(circle.getButton());
                 minimizeTooltip.setHoverArea(minimize.getButton());
                 closeTooltip.setHoverArea(close.getButton());
             }
             catch (NullPointerException ex) { }
             
             lastUpdate = System.nanoTime();
             try
             {
                 Thread.sleep(20);
             }
             catch (InterruptedException ex) { }
         }
     }
     
     /**
      * Gets the area of the {@link Grid} currently selected.
      * 
      * @return the selected area, in cell coordinates
      */
     public Rectangle getSelected()
     {
         return new Rectangle(
                 (int)Math.floor(Math.min(selection.getX(), selection.getMaxX())),
                 (int)Math.floor(Math.min(selection.getY(), selection.getMaxY())),
                 (int)Math.abs(selection.getMaxX() - selection.getMinX()),
                 (int)Math.abs(selection.getMaxY() - selection.getMinY()));
     }
     
     /**
      * Sets the currently selected area.
      * Note that no bound-checking, etc. is done with the given area.
      * 
      * @param selection - the area of the grid to be selected by this Selection
      */
     public void setSelection(Rectangle selection)
     {
        this.selection = new Rectangle(selection);
     }
     
     /**
      * Gets the location of the current selection on the screen.
      * 
      * @return the location of the selection, in pixels
      */
     private Rectangle2D getSelectionOnScreen()
     {
         return new Rectangle2D.Double(
                 grid.toPixel(Math.min(selection.getX() - grid.x, selection.getMaxX() - grid.x)),
                 grid.toPixel(Math.min(selection.getY() - grid.y, selection.getMaxY() - grid.y)),
                 Math.max(1, grid.toPixel(Math.abs(selection.getMaxX() - selection.getMinX()))),
                 Math.max(1, grid.toPixel(Math.abs(selection.getMaxY() - selection.getMinY()))));
     }
     
     /**
      * Gets the current location of the toolbar on the screen.
      * The toolbar 
      * 
      * @return the location of the toolbar, in pixels
      */
     private Rectangle getToolbar()
     {
         int yShift = (int) (15*grid.zoom/35);
         Rectangle2D selection = getSelectionOnScreen();
         
         return new Rectangle( (int)selection.getMaxX(), (int)selection.getY() + yShift,
                 (int)(toolbarWidth - toolbarWidth*(1 - toolbarPos)), toolbarHeight);
     }
     
     /**
      * Invoked by the listener when a key is released.
      * If the control key is held, the event is used to manipulate the selection.
      * 
      * @param e - the triggering event
      */
     public void keyReleased(KeyEvent e)
     {
         if (Listener.controlHeld())
         {
             if (e.getKeyCode() == KeyEvent.VK_S && !Listener.shiftHeld())
             {
                 save();
             }
             else if (e.getKeyCode() == KeyEvent.VK_C && !Listener.shiftHeld())
             {
                 copy();
             }
             else if (e.getKeyCode() == KeyEvent.VK_X && !Listener.shiftHeld())
             {
                 cut();
             }
             else if (e.getKeyCode() == KeyEvent.VK_R)
             {
                 if (Listener.shiftHeld())
                 {
                     rotateCCW();
                 }
                 else
                 {
                     rotateCW();
                 }
             }
             else if (e.getKeyCode() == KeyEvent.VK_D && !Listener.shiftHeld())
             {
                 clear();
             }
         }
     }
     
     /**
      * This method should be invoked by the containing {@link Grid} when any mouse event
      *  occurs (and the event has not been consumed).
      * The {@link Grid} should not handle the event (i.e. should not place/remove cells) if this
      *  method returns true.
      * 
      * @param e - the triggering event
      * @return true if the event has been used by the Selection and should not be used by the
      *  {@link Grid}, false otherwise
      */
     public synchronized boolean mousePressed(MouseEvent e)
     {
         if (Listener.shiftHeld())
         {
             selecting = true;
             created = false;
             selection = new Rectangle2D.Double(grid.toCell(e.getX()) + grid.x,
                     grid.toCell(e.getY()) + grid.y, 0, 0);
             toolbarPos = 0;
             return true;
         }
         else if (selection != null)
         {
             dragOrigin = e.getLocationOnScreen();
             if (getToolbar().contains(e.getLocationOnScreen()))
             {
                 return true;
             }
             // TODO determine which handle is held by which is closest to the click
             else if (
                 Math.abs(grid.toPixel(selection.getMinX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMaxY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.DOWN_LEFT;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getMaxX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMinY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.UP_RIGHT;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getMaxX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMaxY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.DOWN_RIGHT;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getMinX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMinY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.UP_LEFT;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getCenterX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMinY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.UP;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getMaxX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getCenterY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.RIGHT;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getCenterX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getMaxY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.DOWN;
             }
             else if (
                 Math.abs(grid.toPixel(selection.getMinX() - grid.x) - e.getX()) < handleSize &&
                 Math.abs(grid.toPixel(selection.getCenterY() - grid.y) - e.getY()) < handleSize)
             {
                 directionHeld = Direction.LEFT;
             }
             else
             {
                 directionHeld = Direction.NONE;
                 return false;
             }
             return true;
         }
         return false;
     }
     
     /**
      * This method should be invoked by the containing {@link Grid} whenever the mouse is released
      *  (and the event has not been consumed).
      * The selection area is rounded so that partial cells are no longer selected.
      * 
      * @param e - the triggering event
      */
     public synchronized void mouseReleased(MouseEvent e)
     {
         if (selecting || directionHeld != Direction.NONE)
         {
             int left = (int)Math.round(selection.getMinX());
             int right = (int)Math.round(selection.getMaxX());
             int top = (int)Math.round(selection.getMinY());
             int bottom = (int)Math.round(selection.getMaxY());
             
            if (left == right || bottom == top)
            {
                selection = null;
            }
            else
            {
                selection = new Rectangle2D.Double(left, top, right - left, bottom - top);
                if (!created)
                {
                    toolbarState = ToolbarState.MOVING_OUT;
                }
                
                created = true;
            }
             selecting = false;
         }
         directionHeld = Direction.NONE;
     }
     
     /**
      * This method should be invoked by the containing {@link Grid} whenever the mouse is dragged
      *  and the {@link Grid} is not handling it (by creating/removing cells).
      * 
      * @param e - the triggering event
      */
     public synchronized void mouseDragged(MouseEvent e)
     {
         if (selecting)
         {
             selection = new Rectangle2D.Double(selection.getX(), selection.getY(),
                     grid.toCell(e.getX()) - selection.getX() + grid.x,
                     grid.toCell(e.getY()) - selection.getY() + grid.y);
         }
         else if (directionHeld != Direction.NONE)
         {
             switch (directionHeld)
             {
             case UP:
                 selection = new Rectangle2D.Double(
                         selection.getX(),
                         selection.getY() + grid.toCell(e.getY() - dragOrigin.y),
                         selection.getWidth(),
                         selection.getHeight() - grid.toCell(e.getY() - dragOrigin.y));
                 break;
             case UP_RIGHT:
                 selection = new Rectangle2D.Double(
                         selection.getX(),
                         selection.getY() + grid.toCell(e.getY() - dragOrigin.y),
                         selection.getWidth() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight() - grid.toCell(e.getY() - dragOrigin.y));
                 break;
             case RIGHT:
                 selection = new Rectangle2D.Double(
                         selection.getX(),
                         selection.getY(),
                         selection.getWidth() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight());
                 break;
             case DOWN_RIGHT:
                 selection = new Rectangle2D.Double(
                         selection.getX(),
                         selection.getY(),
                         selection.getWidth() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight() + grid.toCell(e.getY() - dragOrigin.y));
                 break;
             case DOWN:
                 selection = new Rectangle2D.Double(
                         selection.getX(),
                         selection.getY(),
                         selection.getWidth(),
                         selection.getHeight() + grid.toCell(e.getY() - dragOrigin.y));
                 break;
             case DOWN_LEFT:
                 selection = new Rectangle2D.Double(
                         selection.getX() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getY(),
                         selection.getWidth() - grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight() + grid.toCell(e.getY() - dragOrigin.y));
                 break;
             case LEFT:
                 selection = new Rectangle2D.Double(
                         selection.getX() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getY(),
                         selection.getWidth() - grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight());
                 break;
             case UP_LEFT:
                 selection = new Rectangle2D.Double(
                         selection.getX() + grid.toCell(e.getX() - dragOrigin.x),
                         selection.getY() + grid.toCell(e.getY() - dragOrigin.y),
                         selection.getWidth() - grid.toCell(e.getX() - dragOrigin.x),
                         selection.getHeight() - grid.toCell(e.getY() - dragOrigin.y));
                 break;
             default:
                 selection = null;
             }
             dragOrigin = e.getLocationOnScreen();
         }
     }
     
     /**
      * Invoked when the save button is pressed or ctrl-S is released.
      * Saves the Selection as a pattern.
      * TODO save Selection as a pattern
      */
     public void save()
     {
         
     }
     
     /**
      * Invoked when the copy button is pressed or ctrl-C is released.
      * Copies the contents of this Selection onto the clipboard of the {@link Grid}.
      * 
      * @see Grid#copy(Rectangle)
      */
     public void copy()
     {
         grid.copy(getSelected());
     }
     
     /**
      * Invoked when ctrl-X is released.
      * Cuts the contents of this Selection onto the clipboard of the {@link Grid}.
      * This is equivalent to calling {@link #copy()} and {@link #clear()} in succession.
      * 
      * @see #copy()
      * @see #clear()
      */
     public void cut()
     {
         copy();
         clear();
     }
     
     /**
      * Invoked when the rotate clockwise button is pressed or ctrl-R is released.
      * Rotate the contents of this Selection clockwise and adjusts the selected area to the
      *  transformed area.
      * 
      * @see Grid#rotateCW(Rectangle)
      */
     public void rotateCW()
     {
         selection = grid.rotateCW(getSelected());
     }
     
     /**
      * Invoked when the rotate counter-clockwise button is pressed or ctrl-R is released.
      * Rotate the contents of this Selection counter-clockwise and adjusts the selected area to the
      *  transformed area.
      * 
      * @see Grid#rotateCCW(Rectangle)
      */
     public void rotateCCW()
     {
         selection = grid.rotateCCW(getSelected());
     }
     
     /**
      * Invoked when the clear button is pressed or ctrl-D is released.
      * Clears the selected area of living cells.
      * 
      * @see Grid#clear(Rectangle))
      */
     public void clear()
     {
         grid.clear(getSelected());
     }
     
     /**
      * Invoked when the square button is pressed.
      * Creates a rectangle out of the border of the selection.
      * 
      * @see Grid#square(Rectangle)
      */
     public void square()
     {
         grid.square(getSelected());
     }
     
     /**
      * Invoked when the circle button is pressed.
      * Creates an oval inside of the selection.
      * 
      * @see Grid#oval(Rectangle)
      */
     public void oval()
     {
         grid.oval(getSelected());
     }
     
     /**
      * Invoked when the minimize button is pressed.
      * Minimizes or unminimizes the toolbar, hiding all but the minimize and close buttons when
      *  minimized.
      */
     public void minimize()
     {
         if (toolbarState == ToolbarState.IN)
         {
             toolbarState = ToolbarState.MOVING_OUT;
         }
         else if (toolbarState == ToolbarState.OUT)
         {
             toolbarState = ToolbarState.MOVING_IN;
         }
     }
     
     /**
      * Closes the selection, removing it entirely.
      */
     public void close()
     {
         selection = null;
         created = false;
     }
     
     /**
      * Draws the selection area onto the given graphics context.
      * This should be called by the enclosing {@link Grid} every draw cycle.
      * 
      * @param g - the graphics context
      */
     public synchronized void draw(Graphics2D g)
     {
         if (selection != null)
         {
             Rectangle2D selection = getSelectionOnScreen();
             double x = selection.getX();
             double y = selection.getY();
             double width = selection.getWidth();
             double height = selection.getHeight();
             
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
             
             final double selectionWidth = Math.max(0.5, Math.sqrt(grid.zoom/5));
             final double selectionBorderWidth = selectionWidth/3;
             final double handleSize = Math.max(5.5, Selection.handleSize*grid.zoom/50);
             
             // draw the toolbar
             if (toolbarPos > 0)
             {
                 Rectangle toolbar = getToolbar();
                 final int arc = 10;
                 final Color[] colors = { new Color(60, 60, 60), new Color(40, 40, 40, 150) };
                 final float[] dist = { 0.2f, 1 };
                 
                 // draw a shadow
                 if (toolbar.height + toolbar.y - y > height)
                 {
                     int fadeSize = (int) Math.max(10, Math.min(width, 18));
                     
                     final float[] sdist = { 0, 1 };
                     final Color[] scolors = { new Color(60, 60, 60), new Color(0, 0, 0, 0) };
                     
                     g.setPaint(new LinearGradientPaint(
                             new Point2D.Double(toolbar.x, toolbar.y),
                             new Point2D.Double(toolbar.x - fadeSize, toolbar.y),
                             sdist, scolors));
                     g.fillRect(toolbar.x - fadeSize,
                             Math.max(toolbar.y, (int)(y + height)),
                             fadeSize,
                             (int)(toolbar.getMaxY() - Math.max(toolbar.y, (int)(y + height))));
                 }
                 
                 // draw the toolbar background
                 g.setClip(toolbar.x, toolbar.y, toolbar.width + 1, toolbar.height + 1);
                 g.setPaint(new LinearGradientPaint(
                         new Point2D.Double(toolbar.x, toolbar.y),
                         new Point2D.Double(toolbar.x + toolbar.width, toolbar.y),
                         dist, colors));
                 g.fillRoundRect(toolbar.x - arc, toolbar.y,
                         toolbar.width + arc, toolbar.height, arc, arc);
                 
                 g.setColor(Color.black);
                 g.drawRoundRect(toolbar.x - arc, toolbar.y,
                         toolbar.width + arc, toolbar.height, arc, arc);
                 
                 // draw the buttons
                 saveImg.draw(save.getButton().x, save.getButton().y, g);
                 copyImg.draw(copy.getButton().x, copy.getButton().y, g);
                 rotateCWImg.draw(rotateCW.getButton().x, rotateCW.getButton().y, g);
                 rotateCCWImg.draw(rotateCCW.getButton().x, rotateCCW.getButton().y, g);
                 squareImg.draw(square.getButton().x, square.getButton().y, g);
                 circleImg.draw(circle.getButton().x, circle.getButton().y, g);
                 clearImg.draw(clear.getButton().x, clear.getButton().y, g);
                 
                 float slideAlpha = (float)toolbarPos;
                 slideOutImg.setTransparency(1 - slideAlpha);
                 slideOutImg.draw(minimize.getButton().x, minimize.getButton().y, g);
                 slideInImg.setTransparency(slideAlpha);
                 slideInImg.draw(minimize.getButton().x, minimize.getButton().y, g);
                 
                 closeImg.draw(close.getButton().x, close.getButton().y, g);
                 
                 g.setClip(null);
                 
                 saveTooltip.draw(g);
                 copyTooltip.draw(g);
                 rotateCWTooltip.draw(g);
                 rotateCCWTooltip.draw(g);
                 clearTooltip.draw(g);
                 squareTooltip.draw(g);
                 circleTooltip.draw(g);
                 minimizeTooltip.draw(g);
                 closeTooltip.draw(g);
             }
             
             // draw the selection
             Area a = new Area(new RoundRectangle2D.Double(
                     x - selectionWidth - selectionBorderWidth,
                     y - selectionWidth - selectionBorderWidth,
                     width + 2*selectionWidth + 2*selectionBorderWidth,
                     height + 2*selectionWidth + 2*selectionBorderWidth,
                     5*selectionWidth, 5*selectionWidth));
             a.subtract(new Area(new RoundRectangle2D.Double(
                     x + selectionWidth + selectionBorderWidth,
                     y + selectionWidth + selectionBorderWidth,
                     width - 2*selectionWidth - 2*selectionBorderWidth,
                     height - 2*selectionWidth - 2*selectionBorderWidth,
                     3*selectionWidth, 3*selectionWidth)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getCenterX() - grid.x) - 
                             handleSize/2 - selectionBorderWidth, 
                     grid.toPixel(this.selection.getMaxY() - grid.y) - 
                             handleSize/2 - selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getCenterX() - grid.x) - 
                             handleSize/2 - selectionBorderWidth, 
                     grid.toPixel(this.selection.getMinY() - grid.y) - 
                             handleSize/2 - selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getMinX() - grid.x) - 
                             handleSize/2 - selectionBorderWidth, 
                     grid.toPixel(this.selection.getCenterY() - grid.y) - 
                             handleSize/2 - selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getMaxX() - grid.x) - 
                             handleSize/2 - selectionBorderWidth, 
                     grid.toPixel(this.selection.getCenterY() - grid.y) - 
                             handleSize/2 - selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth,
                     handleSize + 2*selectionBorderWidth)));
             
             g.setColor(selectionBorderColor);
             g.fill(a);
             
             a = new Area(new RoundRectangle2D.Double(
                     x - selectionWidth, y - selectionWidth,
                     width + 2*selectionWidth, height + 2*selectionWidth,
                     5*selectionWidth, 5*selectionWidth));
             a.subtract(new Area(new RoundRectangle2D.Double(
                     x + selectionWidth, y + selectionWidth,
                     width - 2*selectionWidth, height - 2*selectionWidth,
                     3*selectionWidth, 3*selectionWidth)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getCenterX() - grid.x) - handleSize/2, 
                     grid.toPixel(this.selection.getMaxY() - grid.y) - handleSize/2,
                     handleSize, handleSize)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getCenterX() - grid.x) - handleSize/2, 
                     grid.toPixel(this.selection.getMinY() - grid.y) - handleSize/2,
                     handleSize, handleSize)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getMaxX() - grid.x) - handleSize/2, 
                     grid.toPixel(this.selection.getCenterY() - grid.y) - handleSize/2,
                     handleSize, handleSize)));
             a.add(new Area(new Ellipse2D.Double(
                     grid.toPixel(this.selection.getMinX() - grid.x) - handleSize/2, 
                     grid.toPixel(this.selection.getCenterY() - grid.y) - handleSize/2,
                     handleSize, handleSize)));
             
             g.setColor(selectionColor);
             g.fill(a);
             
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_OFF);
         }
     }
     
     private enum ToolbarState
     {
         OUT,
         MOVING_OUT,
         MOVING_IN,
         IN;
     }
     
     private enum Direction
     {
         NONE,
         UP,
         UP_RIGHT,
         RIGHT,
         DOWN_RIGHT,
         DOWN,
         DOWN_LEFT,
         LEFT,
         UP_LEFT;
     }
 }
