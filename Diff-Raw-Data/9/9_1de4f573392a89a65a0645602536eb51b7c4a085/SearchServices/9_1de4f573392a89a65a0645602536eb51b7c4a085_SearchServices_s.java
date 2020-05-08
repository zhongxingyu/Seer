 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package app.gui.searchServices;
 
 import app.gui.MainWindowIWD;
 import app.gui.detailspanel.AlphaJPanel;
 import app.gui.detailspanel.RoundWindow;
 import app.gui.detailspanel.RoundWindowUtils;
 import app.gui.svgComponents.Canvas;
 import app.gui.svgComponents.SVGCanvasLayers;
 import app.utils.MyLogger;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Level;
 import bridge.ODBridge;
 import org.apache.batik.bridge.UpdateManagerEvent;
 import org.apache.batik.bridge.UpdateManagerListener;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.batik.swing.gvt.JGVTComponentListener;
 
 /**
  *
  * @author vara
  */
 public class SearchServices extends AlphaJPanel implements MouseListener,
         MouseMotionListener {
 
     Canvas can;
     private Point.Double centerPoint = new Point.Double(0, 0);
     private Point.Double currentPos = new Point.Double(0, 0);
     private double radius = 0;
     private DocumentStateChangedListener svgViewListener;
     private boolean enabled = false;
     private ODBridge odbConnector = null;
     private RoundWindow detailsPane;
     private SearchServicesPanel guiForSearchServ = new SearchServicesPanel();
     private Rectangle visibleRec = new Rectangle(0, 0, 0, 0);
     private boolean needRepaint = false;
     private boolean dragged = false;
 
     public SearchServices(Canvas canvas) {
         can = canvas;
         init();
     }
 
     private void init() {
         setLayout(null);
         svgViewListener = new DocumentStateChangedListener();
 
         //don't paint background
         setOpaque(false);
 
     }
 
     protected void installRoundWindow(RoundWindow rw) {
 
         if (detailsPane == null || !detailsPane.equals(rw)) {
            System.out.println("Initial round window and fill content");
             detailsPane = rw;
             detailsPane.getDecoratePanel().getContent().setIcon(MainWindowIWD.createNavigationIcon("searchServices32"));
             detailsPane.setDynamicRevalidate(true);
             detailsPane.setUpperThresholdAlpha(0.6f);
             detailsPane.setAlpha(0.0f);
             detailsPane.getContentPane().setUpperThresholdAlpha(0.75f);
             detailsPane.setTitle("Search Services");
             detailsPane.getContentPane().add(guiForSearchServ);
             detailsPane.setVisible(false);
             detailsPane.addPropertyChangeListener(new PropertyChangeListener() {
 
                 @Override
                 public void propertyChange(PropertyChangeEvent evt) {
                     if (evt.getPropertyName().equals(ALPHA_CHANGE)) {
                         float newAlpha = (Float) evt.getNewValue();
                         if (setAlpha(newAlpha)) {
                             repaint();
                         } else if (newAlpha < .1f) {
                             enabled = false;
                         }
 
                     }
                 }
             });
         } else {
             System.out.println(getClass().getCanonicalName() + "-> content roundWin no changed");
         }
         detailsPane.setEnabled(true);
     }
 
     @Override
     public void setEnabled(boolean val) {
         //super.setEnabled(val);	    
         if (val) {
             enabled = val;
             Container parent = can.getParent();
             if (parent instanceof SVGCanvasLayers) {
                 Container cont = RoundWindowUtils.getRoundWindowFromContainer(parent);
                 if (cont != null) {
                     installRoundWindow((RoundWindow) cont);
                 }
 
             } else {
                 String info = getClass().getCanonicalName() + " method init -> " +
                         "parent SVGCanvasLayers no detect, details window will not be displayed !";
                 System.err.println(info);
             }
         } else {
             detailsPane.setEnabled(false);
         }
         //setCenterPoint(0.0,0.0);
         //setCurrentPosition(0.0,0.0);
         repaint(visibleRec);
     }
 
     public boolean isEnabledSearchServices() {
         return enabled;
     }
 
     public void initODBConnector(ODBridge odbb) {
         odbConnector = odbb;
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g.create();
         if (isEnabledSearchServices()) {
 
             AffineTransform dispTr = svgViewListener.getRenderingTransform();
             dispTr.preConcatenate(g2.getTransform());
             //System.out.println("Dispplay Transform "+dispTr);
             Shape shape = dispTr.createTransformedShape(getPaintingShape());
             Point2D centPoint = dispTr.transform(getCenterPoint(), null);
 
             g2.translate(centPoint.getX(), centPoint.getY());
             g2.draw(shape);
             g2.translate(-centPoint.getX(), -centPoint.getY());
 
         }
         g2.draw(visibleRec);
         g2.dispose();
     }
 
     public Shape getPaintingShape() {
         return new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
     }
 
     protected static void paintCircle(Graphics2D g2, double radius, Point.Double center, Point.Double currPos) {
 
         float dash[] = {10.0f};
         float widthStroke = 1.0f;
         BasicStroke bsLine = new BasicStroke(widthStroke);
         BasicStroke bsCircle = new BasicStroke(widthStroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
         Color fillBacgroundColor = new Color(0, 150, 255, 100);
         Ellipse2D circle = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
         Ellipse2D centerCircle = new Ellipse2D.Double(center.x - 2, center.y - 2, 4, 4);
 
         g2.setStroke(bsLine);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(Color.GREEN);
         g2.fill(centerCircle);
         g2.setColor(Color.BLACK);
         g2.drawLine((int) center.x, (int) center.y, (int) currPos.x, (int) currPos.y);
         g2.setStroke(bsCircle);
         g2.translate(center.x, center.y);
         g2.draw(circle);
 
         g2.setColor(fillBacgroundColor);
         g2.fill(circle);
         g2.translate(-center.x, -center.y);
         g2.setStroke(bsLine);
     }
 
     public DocumentStateChangedListener getDocumentStateChanged() {
         return svgViewListener;
     }
 
     public void setCenterPoint(double x, double y) {
         centerPoint.setLocation(x, y);
         guiForSearchServ.setCenterPoint(getCenterPoint());
     }
 
     public void setRadius(double r) {
         radius = r;
     }
 
     public double getRadius() {
         return radius;
     }
 
     public void setCurrentPosition(double x, double y) {
         currentPos.setLocation(x, y);
         double rad = centerPoint.distance(getCurrentPosition());
         setRadius(rad);
         guiForSearchServ.setRadius(getRadius());
         guiForSearchServ.setCurrentPos(getCurrentPosition());
     }
 
     public Point.Double getCenterPoint() {
         return centerPoint;
     }
 
     public Point2D.Double getCurrentPosition() {
         return currentPos;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1 && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && isEnabledSearchServices()) {
 
             Point2D p = transformPoint(e.getPoint());
             setCenterPoint(p.getX(), p.getY());
             needRepaint = true;
             setDragged(true);
         //System.out.println(""+e.getX()+","+ e.getY() +" -> "+getCenterPoint().getX()+","+getCenterPoint().getY());
         }
     }
 
     public Point2D transformPoint(Point2D p) {
         return p;
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         setDragged(false);
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         //System.out.println("Search services component size "+getSize());
         //System.out.println(getClass().getCanonicalName()+" rendering transform "+listeners.getRenderingTransform());
         //System.out.println("Search services component size "+getSize()+"\nVisible rect "+getVisibleRect());
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
 
         if (isEnabledSearchServices() && isDragged()) {
 
             Point2D p = transformPoint(e.getPoint());
             setCurrentPosition(p.getX(), p.getY());
             /*
             int gap = 10;
             int x = (int)(getCenterPoint().getX()-getRadius()-gap);
             int y = (int)(getCenterPoint().getY()-getRadius()-gap);
             int w = (int)(getRadius()*2+gap*2);
             int h = w;
 
             if(x<0){ w+=x; x=0; }
             if(y<0){ h+=y; y=0; }
             
             if(visibleRec.getWidth()>w || visibleRec.getHeight()>h || needRepaint){
             repaint(visibleRec);
             needRepaint = false;
             }
             //System.out.println("Search Services : Visible Rect. "+visibleRec);
             visibleRec = new Rectangle(x,y,w,h);
             repaint(visibleRec);
              */
             repaint();
         }
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 
     /**
      * @return the dragged
      */
     private boolean isDragged() {
         return dragged;
     }
 
     /**
      * @param dragged the dragged to set
      */
     private void setDragged(boolean dragged) {
         this.dragged = dragged;
     }
 
     public class DocumentStateChangedListener implements JGVTComponentListener,
             UpdateManagerListener {
 
         private AffineTransform rendTransform = new AffineTransform();
         private AffineTransform invTransform = new AffineTransform();
 
         public DocumentStateChangedListener() {
         }
 
         @Override
         public void managerStarted(UpdateManagerEvent e) {
         }
 
         @Override
         public void managerSuspended(UpdateManagerEvent e) {
         }
 
         @Override
         public void managerResumed(UpdateManagerEvent e) {
         }
 
         @Override
         public void managerStopped(UpdateManagerEvent e) {
         }
 
         @Override
         public void updateStarted(UpdateManagerEvent e) {
         }
 
         @Override
         public void updateCompleted(UpdateManagerEvent e) {
         }
 
         @Override
         public void updateFailed(UpdateManagerEvent e) {
         }
 
         //JGVTComponentListener
         @Override
         public void componentTransformChanged(ComponentEvent event) {
 
             JSVGCanvas canvas = (JSVGCanvas) event.getComponent();
             AffineTransform at = canvas.getRenderingTransform();
             setRenderingTransform(at);
             if (at.getDeterminant() != 0) {
                 try {
                     AffineTransform inv = at.createInverse();
                     setInvTransform(inv);
                 } catch (NoninvertibleTransformException ex) {
                     MyLogger.log.log(Level.SEVERE, null, ex);
                     System.err.println("" + ex);
                 }
             } else {
                 setInvTransform(at);
             }
 
         }
 
         public synchronized AffineTransform getRenderingTransform() {
             return new AffineTransform(rendTransform);
         }
 
         /**
          * @param renderingTranform the renderingTranform to set
          */
         public void setRenderingTransform(AffineTransform renderingTranform) {
             this.rendTransform = renderingTranform;
         }
 
         /**
          * @return the invTransform
          */
         public AffineTransform getInvTransform() {
             return new AffineTransform(invTransform);
         }
 
         /**
          * @param invTransform the invTransform to set
          */
         public void setInvTransform(AffineTransform invTransform) {
             this.invTransform = invTransform;
         }
     }//DocumentStateChangedListener
 }
