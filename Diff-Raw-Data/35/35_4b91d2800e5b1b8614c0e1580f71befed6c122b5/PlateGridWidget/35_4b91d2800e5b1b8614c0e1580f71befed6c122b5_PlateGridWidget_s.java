 package edu.ualberta.med.scannerconfig.widgets;
 
 import java.awt.geom.Rectangle2D;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.ualberta.med.scannerconfig.FlatbedImageScan;
 import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.IPlateSettingsListener;
 import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateSettings;
 import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateSettings.PlateOrientation;
 
 public class PlateGridWidget implements IPlateSettingsListener, MouseMoveListener,
     Listener, ControlListener, MouseListener, KeyListener, PaintListener {
 
     private static Logger log = LoggerFactory.getLogger(PlateGridWidget.class.getName());
 
     private enum DragMode {
         NONE,
         MOVE,
         RESIZE_HORIZONTAL_LEFT,
         RESIZE_HORIZONTAL_RIGHT,
         RESIZE_VERTICAL_TOP,
         RESIZE_VERTICAL_BOTTOM,
         RESIZE_BOTTOM_RIGHT,
         RESIZE_TOP_LEFT
     };
 
     private final PlateSettings plateSettings;
 
     private final Canvas canvas;
 
     protected ListenerList changeListeners = new ListenerList();
 
     private Image imageBuffer;
 
     private GC imageGC;
 
     private boolean drag = false;
 
     private final Point startDragMousePt = new Point(0, 0);
 
     private Rectangle2D.Double startGridRect = new Rectangle2D.Double(0, 0, 0, 0);
 
     private DragMode dragMode = DragMode.NONE;
 
     private Image image;
 
     public PlateGridWidget(Composite parent, PlateSettings plateSettings) {
         Assert.isNotNull(plateSettings);
         this.plateSettings = plateSettings;
         this.plateSettings.addPlateBaseChangeListener(this);
 
         canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
         canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
         canvas.getParent().layout();
         canvas.pack();
         canvas.setFocus();
         canvas.redraw();
         canvas.update();
         canvas.addMouseMoveListener(this);
         canvas.addControlListener(this);
         canvas.addMouseListener(this);
         canvas.addKeyListener(this);
         canvas.addPaintListener(this);
         setEnabled();
     }
 
     public void imageUpdated(Image image) {
         this.image = image;
         canvas.redraw();
         setEnabled();
     }
 
     @Override
     public void plateGridChange(Event e) {
         switch (e.type) {
         case IPlateSettingsListener.ORIENTATION:
         case IPlateSettingsListener.GRID_DIMENSIONS:
         case IPlateSettingsListener.TEXT_CHANGE:
             canvas.redraw();
             break;
 
         case IPlateSettingsListener.ENABLED:
             setEnabled();
             break;
 
         default:
             Assert.isTrue(false, "invalid event received: " + e); //$NON-NLS-1$
         }
     }
 
     private Rectangle2D.Double resizeHorizontalLeft(Rectangle2D.Double gridRect, int mousePosX) {
         double delta = mousePosX - startDragMousePt.x;
         double right = startGridRect.x + startGridRect.width;
         Rectangle2D.Double result = new Rectangle2D.Double();
         result.x = Math.max(0, Math.min(right, startGridRect.x + delta));
         result.y = gridRect.y;
         result.width = Math.max(0, Math.min(right, startGridRect.width - delta));
         result.height = gridRect.height;
         return result;
     }
 
     private Rectangle2D.Double resizeHorizontalRight(Rectangle2D.Double gridRect, int mousePosX,
         int maxSizeX) {
         double delta = mousePosX - startDragMousePt.x;
         Rectangle2D.Double result = new Rectangle2D.Double();
         result.x = gridRect.x;
         result.y = gridRect.y;
         result.width = Math.max(0, Math.min(maxSizeX, startGridRect.width + delta));
         result.height = gridRect.height;
         return result;
     }
 
     private Rectangle2D.Double resizeVerticalTop(Rectangle2D.Double gridRect, int mousePosY) {
         double delta = mousePosY - startDragMousePt.y;
         double bottom = startGridRect.y + startGridRect.height;
         Rectangle2D.Double result = new Rectangle2D.Double();
         result.x = gridRect.x;
         result.y = Math.max(0, Math.min(bottom, startGridRect.y + delta));
         result.width = gridRect.width;
         result.height = Math.max(0, Math.min(bottom, startGridRect.height - delta));
         return result;
     }
 
     private Rectangle2D.Double resizeVerticalBottom(Rectangle2D.Double gridRect, int mousePosY,
         int maxSizeY) {
         double delta = mousePosY - startDragMousePt.y;
         Rectangle2D.Double result = new Rectangle2D.Double();
         result.x = gridRect.x;
         result.y = gridRect.y;
         result.width = gridRect.width;
         result.height = Math.max(0, Math.min(maxSizeY, startGridRect.height + delta));
         return result;
     }
 
     @Override
     public void mouseMove(MouseEvent e) {
         if (image == null) return;
 
         canvas.setFocus();
 
         if (drag) {
             Point canvasSize = canvas.getSize();
             Rectangle2D.Double newGridRect = new Rectangle2D.Double(startGridRect.x, startGridRect.y,
                 startGridRect.width, startGridRect.height);
             int delta;
 
             switch (dragMode) {
             case MOVE:
                 delta = e.x - startDragMousePt.x;
                 if (delta < 0) {
                     newGridRect.x = Math.max(0, startGridRect.x + delta);
                 } else {
                     newGridRect.x = Math.min(canvasSize.x - startGridRect.width - 4,
                         startGridRect.x + delta);
                 }
 
                 delta = e.y - startDragMousePt.y;
                 if (delta < 0) {
                     newGridRect.y = Math.max(0, startGridRect.y + delta);
                 } else {
                     newGridRect.y = Math.min(canvasSize.y - startGridRect.height - 4,
                         startGridRect.y + delta);
                 }
                 break;
             case RESIZE_HORIZONTAL_LEFT:
                 newGridRect = resizeHorizontalLeft(newGridRect, e.x);
                 break;
             case RESIZE_HORIZONTAL_RIGHT:
                 newGridRect = resizeHorizontalRight(newGridRect, e.x, canvasSize.x);
                 break;
             case RESIZE_VERTICAL_TOP:
                 newGridRect = resizeVerticalTop(newGridRect, e.y);
                 break;
             case RESIZE_VERTICAL_BOTTOM:
                 newGridRect = resizeVerticalBottom(newGridRect, e.y, canvasSize.y);
                 break;
             case RESIZE_BOTTOM_RIGHT:
                 newGridRect = resizeHorizontalRight(newGridRect, e.x, canvasSize.x);
                 newGridRect = resizeVerticalBottom(newGridRect, e.y, canvasSize.y);
                 break;
 
             case RESIZE_TOP_LEFT:
                 newGridRect = resizeHorizontalLeft(newGridRect, e.x);
                 newGridRect = resizeVerticalTop(newGridRect, e.y);
             default:
                 // do nothing
             }
 
             log.debug("mousemove: plateGrid: {}", newGridRect);
             canvas.redraw();
             notifyChangeListener(newGridRect);
             return;
         }
 
         canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));
 
         /*
          * Creates rectangles on the perimeter of the gridRegion, the code then checks for
          * mouse-rectangle intersection to check for moving and resizing of the widget.
          */
         Rectangle2D.Double plateRect = getPlate();
         if (plateRect != null) {
             if (plateRect.contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_HAND));
                 dragMode = DragMode.MOVE;
             } else if (new Rectangle(
                 (int) (plateRect.x + plateRect.width),
                 (int) (plateRect.y + plateRect.height),
                 15,
                 15).contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                 dragMode = DragMode.RESIZE_BOTTOM_RIGHT;
             } else if (new Rectangle(
                 (int) (plateRect.x - 10),
                 (int) (plateRect.y - 10),
                 15, 15).contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZENWSE));
                 dragMode = DragMode.RESIZE_TOP_LEFT;
             } else if (new Rectangle(
                 (int) (plateRect.x + plateRect.width),
                 (int) (plateRect.y),
                 10,
                 (int) plateRect.height)
                 .contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEE));
                 dragMode = DragMode.RESIZE_HORIZONTAL_RIGHT;
             } else if (new Rectangle(
                 (int) (plateRect.x - 10),
                 (int) (plateRect.y),
                 10,
                 (int) plateRect.height).contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEW));
                 dragMode = DragMode.RESIZE_HORIZONTAL_LEFT;
             } else if (new Rectangle(
                 (int) plateRect.x,
                 (int) (plateRect.y - 10),
                 (int) plateRect.width,
                 10).contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZEN));
                 dragMode = DragMode.RESIZE_VERTICAL_TOP;
             } else if (new Rectangle(
                 (int) plateRect.x,
                 (int) (plateRect.y + plateRect.height),
                 (int) plateRect.width,
                 10).contains(e.x, e.y)) {
                 canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_SIZES));
                 dragMode = DragMode.RESIZE_VERTICAL_BOTTOM;
             } else {
                 dragMode = DragMode.NONE;
             }
         }
     }
 
     @Override
     public void handleEvent(Event event) {
         // do nothing
     }
 
     @Override
     public void controlMoved(ControlEvent e) {
         if (image == null) return;
         canvas.redraw();
     }
 
     @Override
     public void controlResized(ControlEvent e) {
         if (image == null) return;
         canvas.redraw();
     }
 
     @Override
     public void mouseDown(MouseEvent e) {
         if (image == null) return;
 
         if (dragMode != DragMode.NONE) {
             drag = true;
             startDragMousePt.y = e.y;
             startDragMousePt.x = e.x;
             startGridRect = getPlate();
 
         }
         canvas.redraw();
     }
 
     @Override
     public void mouseDoubleClick(MouseEvent e) {
     }
 
     @Override
     public void mouseUp(MouseEvent e) {
         if (image == null) return;
 
         drag = false;
         dragMode = DragMode.NONE;
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         if (image == null) return;
 
         Point canvasSize;
 
         if (!drag) {
             Rectangle2D.Double plateRect = getPlate();
             log.debug("keyPressed: event: {}, plate: {}", e, plateRect);
 
             switch (e.keyCode) {
             case SWT.ARROW_LEFT:
                 if (plateRect.x - 1 > 0) {
                     plateRect.x += -1;
                 }
                 break;
             case SWT.ARROW_RIGHT:
                 canvasSize = canvas.getSize();
                 if (plateRect.x + plateRect.width + 1 < canvasSize.x) {
                     plateRect.x += 1;
                 }
                 break;
             case SWT.ARROW_UP:
                 if (plateRect.y - 1 > 0) {
                     plateRect.y += -1;
                 }
                 break;
             case SWT.ARROW_DOWN:
                 canvasSize = canvas.getSize();
                 if (plateRect.y + plateRect.height + 1 < canvasSize.y) {
                     plateRect.y += 1;
                 }
                 break;
             }
             canvas.redraw();
             log.debug("keyPressed: after change: plate: {}", plateRect);
             notifyChangeListener(plateRect);
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
     }
 
     @Override
     public void paintControl(PaintEvent e) {
         Rectangle2D.Double plateGrid = getPlate();
         if ((image == null) || (plateGrid == null)) {
             e.gc.setForeground(new Color(canvas.getDisplay(), 255, 255, 255));
             e.gc.fillRectangle(0, 0, canvas.getSize().x, canvas.getSize().y);
             return;
         }
 
        log.debug("paintControl: plateGrid: {}", plateGrid);
 
         Rectangle imageRect = image.getBounds();
         imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
         imageGC = new GC(imageBuffer);
         imageGC.drawImage(image, 0, 0, imageRect.width, imageRect.height,
             0, 0, canvas.getBounds().width, canvas.getBounds().height);
 
         drawGrid(imageGC, plateGrid, plateSettings.getPlateDimensions());
 
         // draw plate rect minus 1 pixel on each side so that it acts as a
         // border for the grid
         Rectangle plateRectBoundary = new Rectangle(
             (int) (plateGrid.x - 1),
             (int) (plateGrid.y - 1),
             (int) (plateGrid.width + 2),
             (int) (plateGrid.height + 2));
        imageGC.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));
         imageGC.drawRectangle(plateRectBoundary);
 
         // create drag circles
         int left = plateRectBoundary.x;
         int top = plateRectBoundary.y;
         int right = plateRectBoundary.x + plateRectBoundary.width - 3;
         int bottom = plateRectBoundary.y + plateRectBoundary.height - 3;
 
        imageGC.setForeground(new Color(canvas.getDisplay(), 0, 0, 255));
         imageGC.drawOval(left, top, 6, 6);
         imageGC.drawOval(right, bottom, 6, 6);
 
         e.gc.drawImage(imageBuffer, 0, 0);
         imageGC.dispose();
         imageBuffer.dispose();
     }
 
     private void drawGrid(GC gc, Rectangle2D.Double gridRect, Pair<Integer, Integer> dimensions) {
         int rows = dimensions.getLeft();
         int cols = dimensions.getRight();
         double cellWidth = gridRect.width / cols;
         double cellHeight = gridRect.height / rows;

        Rectangle cellRect;
        Color foregroundColor = new Color(canvas.getDisplay(), 0, 255, 0);
        Color a1BackgroundColor = new Color(canvas.getDisplay(), 255, 255, 0);
        gc.setForeground(foregroundColor);
         int a1Row = 0;
         int a1Col = 0;
         if (plateSettings.getOrientation().equals(PlateOrientation.LANDSCAPE)) {
             a1Col = cols - 1;
         }
 
         double cx, cy = gridRect.y;
         for (int row = 0; row < rows; row++, cy += cellHeight) {
             cx = gridRect.x;
 
             for (int col = 0; col < cols; col++, cx += cellWidth) {
                cellRect = new Rectangle((int) cx, (int) cy,
                    (int) cellWidth, (int) cellHeight);
 
                 gc.drawRectangle(cellRect);
 
                 if ((row == a1Row) && (col == a1Col)) {
                     gc.setAlpha(125);
                     gc.setBackground(a1BackgroundColor);
                     gc.fillRectangle(cellRect);
                 }
             }
         }
     }
 
     private void setEnabled() {
         if ((canvas == null) || canvas.isDisposed())
             return;
 
         canvas.redraw();
         canvas.update();
     }
 
     public void dispose() {
         plateSettings.removePlateSettingsChangeListener(this);
     }
 
     /**
      * Used to receive updates when user changes dimensions of grid.
      */
     public void addPlateWidgetChangeListener(IPlateGridWidgetListener listener) {
         changeListeners.add(listener);
     }
 
     /**
      * These factors depend on the size and DPI of the image being displayed, and the size of the
      * canvas it is being displayed in.
      * 
      * @return a Pair, the left contains the widht factor and the right the height factor.
      */
     private Pair<Double, Double> getConversionFactors() {
         if (image == null) return null;
 
         Rectangle imgBounds = image.getBounds();
         Point canvasSize = canvas.getSize();
 
         double widthFactor = (double)
             FlatbedImageScan.PLATE_IMAGE_DPI * canvasSize.x / imgBounds.width;
         double heightFactor = (double)
             FlatbedImageScan.PLATE_IMAGE_DPI * canvasSize.y / imgBounds.height;
         return new ImmutablePair<Double, Double>(widthFactor, heightFactor);
     }
 
     private Rectangle2D.Double getPlate() {
         Rectangle2D.Double plateInInches = plateSettings.getPlate();
         if (plateInInches == null) return null;
 
         Pair<Double, Double> factors = getConversionFactors();
         if (factors == null) return null;
         double widthFactor = factors.getLeft();
         double heightFactor = factors.getRight();
 
         Rectangle2D.Double result = new Rectangle2D.Double(
             plateInInches.x * widthFactor,
             plateInInches.y * heightFactor,
             plateInInches.width * widthFactor,
             plateInInches.height * heightFactor);
         return result;
     }
 
     private void notifyChangeListener(final Rectangle2D.Double newPlate) {
         Object[] listeners = changeListeners.getListeners();
 
         Pair<Double, Double> factors = getConversionFactors();
         if (factors == null) {
             throw new IllegalStateException("cannot get conversion factors");
         }
         double widthFactor = factors.getLeft();
         double heightFactor = factors.getRight();
 
         final Rectangle2D.Double plateInInches = new Rectangle2D.Double(
             newPlate.x / widthFactor,
             newPlate.y / heightFactor,
             newPlate.width / widthFactor,
             newPlate.height / heightFactor);
 
         for (int i = 0; i < listeners.length; ++i) {
             final IPlateGridWidgetListener l = (IPlateGridWidgetListener) listeners[i];
             SafeRunnable.run(new SafeRunnable() {
                 @Override
                 public void run() {
                     l.plateUpdated(plateInInches);
                 }
             });
         }
     }
 
 }
