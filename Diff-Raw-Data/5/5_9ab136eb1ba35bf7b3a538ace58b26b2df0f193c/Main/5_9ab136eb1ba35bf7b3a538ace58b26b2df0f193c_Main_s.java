 /*
  * @(#) Main.java
  */
 package ntango;
 
 import touch.*;
 import touch.ui.*;
 import tween.*;
 
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.Paint;
 import java.awt.Shape;
 import java.awt.Rectangle;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.Area;
 import java.awt.geom.AffineTransform;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 
 public class Main extends SurfaceFrame implements Tweenable, ActionListener, KeyListener {
 
    protected static final int WIDGET_LAYER = 0;
    protected static final int PLOT_LAYER = 1;
    protected static final int WORLD_LAYER = 2;
    protected static final int TOOLBAR_LAYER = 3;
    
    public static final Paint BACKGROUND =
    //new GradientPaint(0, 0, new Color(32, 172, 75),
    //0, 800, new Color(25, 54, 26));
    new GradientPaint(0, 0, new Color(77, 181, 219),
                      0, 800, new Color(31, 69, 112));
 
    protected Model model;
    protected WorldView view;
    protected Toolbar tools;
    protected BufferedImage logo;
    
    public static Main instance;
 
    
    public Main() throws Exception { 
       super("NetTango");
       this.model = new Model(this);
       //this.model.load("models/wolfsheep.nlogo");
       //this.model.setup();
       this.view = new WorldView(model);
       this.tools = new Toolbar(model);
       this.logo = Palette.createImage("/images/Logo.png");
       addKeyListener(this);
       
       instance = this;
    }
 
    public void addModelWidget(Touchable t) {
       addTouchable(t, WIDGET_LAYER);
    }
 
    public void addModelPlot(Touchable t) {
       addTouchable(t, PLOT_LAYER);
    }
 
    public Plot getPlot(String name) {
       for (Touchable t : getWidgets(PLOT_LAYER)) {
          Plot plot = (Plot)t;
          if (name.equals(plot.getTitle())) {
             return plot;
          }
       }
       return null;
    }
 
    //------------------------------------------------------------
    // APPLICATION EVENTS
    //------------------------------------------------------------
    protected void startup(int w, int h) {
       addLayers(3);
       addTouchable(view, WORLD_LAYER);
       addTouchable(tools, TOOLBAR_LAYER);
       layout(w, h);
    }
    
    protected void layout(int w, int h) {
       view.setWidth(590);
       view.setHeight(590);
       view.setPosition(w/2 - view.getWidth() / 2,
                        h/2 - view.getHeight() / 2 - 25);
       tools.setPosition(w/2 - tools.getWidth() / 2,
                         h - tools.getHeight() - 15);
    }
 
    protected void shutdown() { }
 
    protected void animate() {
       if (model.getPlayHead() == model.getStream().getMaxIndex()) {
          enableWidgets(WIDGET_LAYER);
       } else {
          disableWidgets(WIDGET_LAYER);
       }
    }
    
    protected void resized(int w, int h) {  }
 
    protected void loadModel(String path, String name) {
       if (!model.getName().equals(name)) {
 		  clearWidgets(WIDGET_LAYER);
         clearWidgets(PLOT_LAYER);
 		  model.load(path);
 		  model.setup();
 		  model.setName(name);
 		  layout(getWidth(), getHeight());
       }
    }
    
    public void loadModel(TouchEvent te) {
       String key = "tag" + String.format("%02X", te.getTagID());
       String val = getProperty(key);
       if (val != null) {
          String [] sa = val.split(":");
          if (sa.length == 2) {
             loadModel(sa[0], sa[1]);
          }
       }
    }
    
    //------------------------------------------------------------
    // DRAWING EVENTS
    //------------------------------------------------------------
    protected void drawBackground(Graphics2D g, int w, int h) {
       g.setPaint(BACKGROUND);
       g.fillRect(0, 0, w, h);
 
       g.drawImage(logo, w - logo.getWidth() - 15, 15, null);
       g.setColor(new Color(0xbbffffff, true));
       g.setFont(new Font(null, 0, 20));
       g.drawString(model.getName(), 15, 37);
    }
   
 
    protected void drawForeground(Graphics2D g, int w, int h) {
       AffineTransform save = g.getTransform();
       g.transform(w2s);
       g.setTransform(save);
    }
 
    protected void drawContent(Graphics2D g, int w, int h) {
    }
 
    //------------------------------------------------------------
    // TWEENS
    //------------------------------------------------------------
    public void startTween(String property, Tween tween) { }
    public void endTween(String property, Tween tween) { }
    public void setTweenValue(String property, Tween tween) { }
 
    protected void onDown(float tx, float ty) { }
    protected void onDrag(float tx, float ty) { }
    protected void onRelease(float tx, float ty) { }
 
    
    //------------------------------------------------------------
    // TOUCH EVENTS
    //------------------------------------------------------------
    public boolean containsTouch(TouchEvent event) {
       return true;
    }
    
    public void touchDown(TouchFrame frame) {
       onDown(frame.getX(), frame.getY());
    }
 
    public void touchDrag(TouchFrame frame) {
       onDrag(frame.getX(), frame.getY());
       for (TouchEvent te : frame.getTouchEvents()) {
          if (te.isTag()) {
             loadModel(te);
          }
       }
    }
 
    public void touchRelease(TouchFrame frame) {
       onRelease(frame.getX(), frame.getY());
    }
 
    //------------------------------------------------------------
    // MOUSE EVENTS
    //------------------------------------------------------------
    public void mousePressed(MouseEvent e) {
       onDown(e.getX(), e.getY());
    }
 
    public void mouseReleased(MouseEvent e) {
       onRelease(e.getX(), e.getY());
    }
 
    public void mouseMoved(MouseEvent e) {
    }
 
    public void mouseDragged(MouseEvent e) {
       onDrag(e.getX(), e.getY());
    }
 
    public void keyPressed(KeyEvent e) {
       float cx, cy;
       switch (e.getKeyCode()) {
       case KeyEvent.VK_MINUS:
          cx = view.getCenterX();
          cy = view.getCenterY();
          view.setWidth((int)(view.getWidth() / 1.1));
          view.setHeight((int)(view.getHeight() / 1.1));
          view.setCenterPosition(cx, cy);
          break;
 
       case KeyEvent.VK_EQUALS:
          cx = view.getCenterX();
          cy = view.getCenterY();
          view.setWidth((int)(view.getWidth() * 1.1));
          view.setHeight((int)(view.getHeight() * 1.1));
          view.setCenterPosition(cx, cy);
          break;
 
       case KeyEvent.VK_1:
         loadModel("models/wolfsheep.nlogo", "Wolf Sheep Predation");
          break;
 
       case KeyEvent.VK_2:
          loadModel("models/virus.nlogo", "Disease Model");
          break;
 
       case KeyEvent.VK_3:
          loadModel("models/fireflies.nlogo", "Fireflies");
          break;
 
       case KeyEvent.VK_4:
          loadModel("models/flocking.nlogo", "Flocking");
          break;
 
       case KeyEvent.VK_5:
          loadModel("models/traffic.nlogo", "Traffic");
          break;
 
       case KeyEvent.VK_6:
          loadModel("models/moths.nlogo", "Moths");
          break;
 
       case KeyEvent.VK_RIGHT:
          //pan(-10, 0);
          break;
 
       case KeyEvent.VK_LEFT:
          //pan(10, 0);
          break;
 
       case KeyEvent.VK_UP:
          //pan(0, 10);
          break;
 
       case KeyEvent.VK_DOWN:
          //pan(0, -10);
          break;
       }
    }
 
    public void keyReleased(KeyEvent e) { }
    public void keyTyped(KeyEvent e) { }
 
    
    //------------------------------------------------------------
    // MAIN APPLICATION ENTRY POINT
    //------------------------------------------------------------
    public static void main(String[] args) {
 
       //-------------------------------------------------
       // Launch viewer
       //-------------------------------------------------
       javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
             try {
                new Main();
             } catch (Exception x) {
                x.printStackTrace();
                System.exit(1);
             }
          }
       });
    }
 }
