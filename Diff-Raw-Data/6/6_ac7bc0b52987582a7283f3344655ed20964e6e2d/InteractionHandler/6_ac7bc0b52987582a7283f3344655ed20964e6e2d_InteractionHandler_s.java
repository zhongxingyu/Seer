 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jp.atr.dni.bmi.desktop.timeline;
 
 import static jp.atr.dni.bmi.desktop.timeline.TimelineTopComponent.SCROLLBAR_HEIGHT;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Point2D;
 import javax.media.opengl.GL2;
 import jp.atr.dni.bmi.desktop.timeline.model.ViewerChannel;
 
 /**
  * 
  * @author makoto
  */
 public class InteractionHandler implements KeyListener, MouseListener,
         MouseMotionListener, MouseWheelListener {
 
    /** a mapping of interactor names to interactors */
 //	LinkedHashMap<String, Mode> modes = new LinkedHashMap<String, Interactor>();
    // interactor names
    public static String DRAG_MODE = "Drag Mode";
    public static String VIEW_MODE = "View Mode";
    /** the current interaction */
 //	private Mode mode;
    /** the canvas */
    private TimelineTopComponent canvas;
    /** the previously point clicked by the mouse, in screen coordinates */
    private Point screenPickedPoint;
    /** the previously point clicked by the mouse, in virtual coordinates */
    private Point2D pickedPoint;
    /** the current mouse location, in screen coordinates */
    private Point screenCurrentPoint;
    /** the current mouse location, in virtual coordinates */
    private Point2D currentPoint;
    /** the previous mouse location, in screen coordinates */
    private Point screenPreviousPoint;
    /** the previous mouse location, in virtual coordinates */
    private Point2D previousPoint;
    /** the amout to translate the canvas */
    private double TRANSLATE_AMOUNT = 25.0;
    /** the amout to scale the canvas */
    private double SCALE_AMOUNT = 1.1;
    private ViewerChannel selectedChannel;
    private boolean draggingVerticalScrollbar;
    private boolean draggingHorizontalScrollbar;
    private long timespan;
 
    /**
     * Constructs the mode handler.
     *
     * @param canvas - the canvas
     */
    public InteractionHandler(TimelineTopComponent canvas) {
       this.canvas = canvas;
       draggingVerticalScrollbar = false;
       draggingHorizontalScrollbar = false;
    }
 
    /**
     * Sets the given object as selected and deselects the previously selected
     * object.
     *
     * @param object - the object to select
     */
    public void setSelectedObject(ViewerChannel object) {
 
       // deselect previous object
       if (selectedChannel != null) {
          selectedChannel.setSelected(false);
       }
 
       selectedChannel = object;
       if (selectedChannel != null) {
          selectedChannel.setSelected(true);
 //			canvas.getMessageBar().setSelectedObject(object.getAttributeValue("name"));
       } else {
          selectedChannel = null;
 //			canvas.getMessageBar().setSelectedObject("");
       }
    }
 
    /**
     * Gets the last picked point in screen coordinates.
     */
    public Point getScreenPickedPoint() {
       return screenPickedPoint;
    }
 
    /**
     * Gets the last picked point in virtual coordinates.
     */
    public Point2D getPickedPoint() {
       return pickedPoint;
    }
 
    /**
     * Gets the previous mouse location in screen coordinates.
     */
    public Point getScreenPreviousPoint() {
       return screenPreviousPoint;
    }
 
    /**
     * Gets the previous mouse location in virtual coordinates.
     */
    public Point2D getPreviousPoint() {
       return previousPoint;
    }
 
    /**
     * Gets the current mouse location in screen coordinates.
     */
    public Point getScreenCurrentPoint() {
       return screenCurrentPoint;
    }
 
    /**
     * Gets the current mouse location in virtual coordinates.
     */
    public Point2D getCurrentPoint() {
       return currentPoint;
    }
 
    /**
     * Informs the current interactor of a key pressed events. Also checks if
     * the key pressed is a hotkey that transforms the canvas or selects a
     * new interaction.
     */
    public void keyPressed(KeyEvent arg0) {
       switch (arg0.getKeyCode()) {
 
          // canvas operations
          case KeyEvent.VK_LEFT:
             canvas.setTranslationX(canvas.getTranslationX() + TRANSLATE_AMOUNT / canvas.getScale());
             break;
          case KeyEvent.VK_RIGHT:
             canvas.setTranslationX(canvas.getTranslationX() - TRANSLATE_AMOUNT / canvas.getScale());
             break;
          case KeyEvent.VK_UP:
             canvas.setTranslationY(canvas.getTranslationY() + TRANSLATE_AMOUNT / canvas.getScale());
             break;
          case KeyEvent.VK_DOWN:
             canvas.setTranslationY(canvas.getTranslationY() - TRANSLATE_AMOUNT / canvas.getScale());
             break;
          case KeyEvent.VK_PAGE_UP:
             canvas.setScale(canvas.getScale() * SCALE_AMOUNT);
             break;
          case KeyEvent.VK_PAGE_DOWN:
             canvas.setScale(canvas.getScale() / SCALE_AMOUNT);
             break;
          case KeyEvent.VK_V:
             canvas.zoomAll();
             break;
 //		case KeyEvent.VK_G:
 //			canvas.getGrid().setVisible(!canvas.getGrid().isVisible());
 //			mainWindow.setGridVisible(canvas.getGrid().isVisible());
 //			break;
       }
    }
 
    @Override
    public void mousePressed(MouseEvent me) {
 
       // update the virtual points
       screenCurrentPoint = me.getPoint();
       screenPickedPoint = screenCurrentPoint;
       currentPoint = canvas.getVirtualCoordinates(me.getX(), me.getY());
       pickedPoint = currentPoint;
       if (previousPoint == null) {
          screenPreviousPoint = screenCurrentPoint;
          previousPoint = currentPoint;
       }
       draggingHorizontalScrollbar = isInsideHorizontalScrollbar(screenPickedPoint);
       draggingVerticalScrollbar = isInsideVerticalScrollbar(screenPickedPoint);
    }
 
    /**
     *
     * @param arg0
     */
    public void mouseReleased(MouseEvent arg0) {
       draggingVerticalScrollbar = false;
       draggingHorizontalScrollbar = false;
    }
 
    /**
     * Updates the current and previous points
     */
    public void mouseMoved(MouseEvent me) {
       screenCurrentPoint = me.getPoint();
       currentPoint = canvas.getVirtualCoordinates(me.getX(), me.getY());
       if (previousPoint == null) {
          screenPreviousPoint = screenCurrentPoint;
          previousPoint = currentPoint;
       }
 
       screenPreviousPoint = me.getPoint();
       previousPoint = canvas.getVirtualCoordinates(me.getX(), me.getY());
    }
 
    /**
     * Updates the current and previous points. If the left button is
     * performing the drag, then the current interactor is informed of the
     * event. If the right button is performing the drag, then the canvas is
     * panned and interactors are not informed of the event.
     */
    public void mouseDragged(MouseEvent me) {
 
       int meX = me.getX();
       int meY = me.getY();
       double halfWidth = canvas.getWidth() / 5d;
       double invHalfWidth = canvas.getWidth() - halfWidth;
 
       screenCurrentPoint = me.getPoint();
       currentPoint = canvas.getVirtualCoordinates(meX, meY);
 
 //      if (canvas.getDataUpper().getX() < halfWidth && (meX - previousPoint.getX()) < 0) {
 //         screenCurrentPoint.setLocation(screenCurrentPoint.getX(), meY);
 //         currentPoint = canvas.getVirtualCoordinates(screenCurrentPoint.getX(), me.getY());
 //      } else if (canvas.getDataLower().getX() > canvas.getWidth() - halfWidth) {
 //         screenCurrentPoint.setLocation(screenCurrentPoint.getX()-1, meY);
 //         currentPoint = canvas.getVirtualCoordinates(screenCurrentPoint.getX(), me.getY());
 //      } else {
 //
 //      }
 
       double dx = currentPoint.getX() - previousPoint.getX();
       double dy = currentPoint.getY() - previousPoint.getY();
 
       Point2D timeStart = canvas.getScreenCoordinates(0, 0);
       Point2D timeEnd = canvas.getScreenCoordinates(timespan, 0);
 
 //      System.out.println("halfWidth: " + halfWidth + "\tinvHalfWidth: " + invHalfWidth + "\tdx: " + dx + "\tstart: " + timeStart.getX() + "\tend: " + timeEnd.getX());
 
 
       if (draggingHorizontalScrollbar) {
          canvas.setTranslationX(canvas.getTranslationX() - dx * ((canvas.getWidth() - SCROLLBAR_HEIGHT) / (canvas.getDataUpperX() - canvas.getDataLowerX())));
       } else if (draggingVerticalScrollbar) {
          canvas.setTranslationY(canvas.getTranslationY() - dy * ((canvas.getHeight() - SCROLLBAR_HEIGHT) / (canvas.getDataUpperY())));
       } else {
          if (timeEnd.getX() < halfWidth) {
             dx = halfWidth - timeEnd.getX();
          } else if (timeStart.getX() > invHalfWidth) {
             dx = timeStart.getX() - invHalfWidth;
             dx *= -1;
          }
 
          canvas.setTranslationX(canvas.getTranslationX() + dx);
          canvas.setTranslationY(canvas.getTranslationY() + dy);
 
          screenPreviousPoint.setLocation(screenCurrentPoint.getX(), screenCurrentPoint.getY());
          previousPoint = canvas.getVirtualCoordinates(screenPreviousPoint.getX(), screenPreviousPoint.getY());
       }
 
       /**
        * The mouse wheel controls the current zoom factor. Iteractors do not
        * receive mouse wheel events.
        */
   

   

   
 
    public void mouseWheelMoved(MouseWheelEvent arg0) {
       if (arg0.getWheelRotation() < 0) {
          canvas.setScale(canvas.getScale() * SCALE_AMOUNT);
       } else if (arg0.getWheelRotation() > 0) {
          canvas.setScale(canvas.getScale() / SCALE_AMOUNT);
       }
    }
 
    /**
     * Not implemented.
     */
    public void mouseClicked(MouseEvent me) {
 //      System.out.println(me.getYOnScreen() + "\t" + me.getY() + "\t"+canvas.getScale()*(-canvas.getScale() / (canvas.getHeight()*.5)));
 //      GLU glu = new GLU();
 //
 //      int viewport[] = new int[4];
 //      float mvmatrix[] = new float[16];
 //      float projmatrix[] = new float[16];
 //
 //      canvas.getGlCanvas().getGL().glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
 //      canvas.getGlCanvas().getGL().glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
 //      canvas.getGlCanvas().getGL().glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
 //      int realy = 0;// GL y coord pos
 //      realy = viewport[3] - (int) me.getY() - 1;
 //
 //      float wcoord[] = new float[4];// wx, wy, wz;// returned xyz coords
 //
 //      glu.gluUnProject((float) me.getX(), (float) realy, 0.0f, //
 //              mvmatrix, 0,
 //              projmatrix, 0,
 //              viewport, 0,
 //              wcoord, 0);
 //          System.out.println("World coords at z=0.0 are ( " //
 //                             + wcoord[0] + ", " + wcoord[1] + ", " + wcoord[2]
 //                             + ")");
 //          glu.gluUnProject((float) me.getX(), (float) realy, 1.0f, //
 //              mvmatrix, 0,
 //              projmatrix, 0,
 //              viewport, 0,
 //              wcoord, 0);
 //          System.out.println("World coords at z=1.0 are (" //
 //                             + wcoord[0] + ", " + wcoord[1] + ", " + wcoord[2]
 //                             + ")");
    }
 
    /**
     * Not implemented.
     */
    public void keyTyped(KeyEvent arg0) {
    }
 
    /**
     * Not implemented.
     */
    public void mouseEntered(MouseEvent arg0) {
    }
 
    /**
     * Not implemented.
     */
    public void mouseExited(MouseEvent arg0) {
    }
 
    @Override
    public void keyReleased(KeyEvent ke) {
       //throw new UnsupportedOperationException("Not supported yet.");
    }
 
    private boolean isInsideVerticalScrollbar(Point p) {
       double x = p.getX();
       double y = p.getY();
 
       return x > 0 && x < SCROLLBAR_HEIGHT && y > canvas.getDataLowerY() && y < canvas.getDataLowerY() + canvas.getDataUpperY();
    }
 
    private boolean isInsideHorizontalScrollbar(Point p) {
       double x = p.getX();
       double y = p.getY();
 
       return x > SCROLLBAR_HEIGHT + canvas.getDataLowerX() && x < SCROLLBAR_HEIGHT + canvas.getDataUpperX() && y > canvas.getHeight() - SCROLLBAR_HEIGHT;
    }
 
    /**
     * @return the timespan
     */
    public long getTimespan() {
       return timespan;
    }
 
    /**
     * @param timespan the timespan to set
     */
    public void setTimespan(long timespan) {
       this.timespan = timespan;
    }
 }
