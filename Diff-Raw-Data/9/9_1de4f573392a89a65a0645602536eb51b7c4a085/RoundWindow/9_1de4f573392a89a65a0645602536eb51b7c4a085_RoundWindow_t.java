 package app.gui.detailspanel;
 
 import app.gui.RepaintManager.AlphaRepaintManager;
 import app.gui.borders.AlphaBorder;
 import app.gui.borders.OvalBorder;
 import app.gui.borders.RoundBorder;
 import app.utils.Utils;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.RoundRectangle2D;
 import javax.swing.RepaintManager;
 import javax.swing.SwingUtilities;
 import javax.swing.border.Border;
 import org.jdesktop.animation.timing.Animator;
 import org.jdesktop.animation.timing.Animator.Direction;
 import org.jdesktop.animation.timing.Animator.RepeatBehavior;
 import org.jdesktop.animation.timing.TimingTarget;
 
 /**
  * Created on 2008-12-08, 21:25:25
  * @author vara
  */
 public class RoundWindow extends RoundJPanel
         implements MouseListener,MouseMotionListener,FocusListener{
 
 
     private int sensitiveMouseReaction = 8;        
 
     private boolean resizeWidthPanel = false;
     private boolean resizeHeghtPanel = false;
     private boolean cursorChanged = false;
 
     private boolean needRevalidate = false;
     private boolean dynamicRevalidate = false;
 
     private Dimension defaultSize = new Dimension(330,400);
 
     private Color[] colorBorderGlow = {new Color(230,230,230,168),new Color(70,102,146,180),
                                        new Color(190,190,190, 250),new Color(255,255,255, 255)};
 
     private boolean decoratedWindow = true;
 
     private Animator animator;
     private int animationDuration = 4000;
 
     private AbstractDecoratePanel decorate = new DecoratePanel();
     private RoundWindowRootPane rootPane;
 
     private Insets innerGap = new Insets(7,7,7,7);
     private int decorateAndContentGap = 5;
 
     public RoundWindow(){
         super(20,20);
         setSize(defaultSize);
         addMouseListener(this);
         addMouseMotionListener(this);
         OvalBorder mainBorder = (OvalBorder)getRoundBorder();
         mainBorder.setInsets(new Insets(3,3,3,0));
 
         setFocusable(true);
         addFocusListener(this);
         super.setLayout(new BorderLayout(0,getDecorateAndContentGap()));
         
         animator = new Animator(animationDuration, 1,
                 RepeatBehavior.LOOP, new WindowDisplayBehavior());
 
         //((DecoratePanel)decorate).addActionListenerToCloseButton(new CloseAction());
 
         installRepaintManager();
 
         //contentPane.setBorder(new OvalBorder(3,5,3,5, mainBorder.getRecW(),
           //      mainBorder.getRecH(), mainBorder.getBorderColor()));
 
         add(decorate,BorderLayout.NORTH);
         setRootPane(createRootPane());
         getContentPane().setBorder(new OvalBorder(3,5,3,5, mainBorder.getRecW(),
                 mainBorder.getRecH(), mainBorder.getBorderColor()));
         super.setEnabled(false);
     }
 
     public boolean isEmpty(){
         return !(getRoundWindowRootPane().getContentPane().getComponents().length>0);
     }
     
     public RoundRectangle2D getWindowShape(){
         Point.Double corners = getRoundCorner();
         return new RoundRectangle2D.Double(
                 0,0, getWidth(), getHeight(),corners.x,corners.y);
     }
 
     protected RoundWindowRootPane createRootPane(){
         rootPane = new RoundWindowRootPane();
         return rootPane;
     }
 
     public RoundWindowRootPane getRoundWindowRootPane() {
         return rootPane;
     }
 
     protected void setRootPane(RoundWindowRootPane rp){
         add(rp, BorderLayout.CENTER);
     }
 
     public AlphaJPanel getContentPane(){        
         return rootPane.getContentPane();
     }
 
     public void setTitle(String str){
 
         getDecoratePanel().setTitle(str);
     }
 
     @Override
     public void setEnabled(final boolean aFlag) {
         super.setEnabled(aFlag);
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 displayPanel(aFlag);
             }
         });        
     }
 
     public void displayPanel(boolean val){
         
         float frac = .0f;
         if(animator.isRunning()){
             animator.cancel();
             frac = getAlpha();
         }
         if(val){
            animator.setStartFraction(frac);
            animator.setStartDirection(Direction.FORWARD);
         }else{
            animator.setStartFraction(Math.max(getContentPane().getAlpha(),getAlpha()));
            animator.setStartDirection(Direction.BACKWARD);
         }
         animator.start();
     }
     public void updateMyUI(){
         Container parent = getParent();
         if(parent == null)
             return;
 
         int x = (getParent().getWidth()-getWidth());
         int y = (getParent().getHeight()-getHeight())>>1;
         updateMyUI(x, y, getWidth(), getHeight());
         
         //getVerboseStream().outputVerboseStream(getClass().getSimpleName()+" UpdateMyUI\n Parent size ["+width+","+height+"]" +
         //        "\tLocation on parent component ["+getLocation().getX()+","+getLocation().y+"]");
     }
      public void updateMyUI(final int x,final int y,final int width,final int height){
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 setBounds(x,y, width, height);
                 if(isDynamicRevalidate())
                     revalidate();
             }
         });
      }
     private void installRepaintManager() {
         AlphaRepaintManager manager = new AlphaRepaintManager();
         RepaintManager.setCurrentManager(manager);
     }
     
 
     @Override
     public void paintComponent(Graphics g){
         if(isVisible() && getAlpha()>0){
 
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D)g.create();
             GradientPaint gp = new GradientPaint(0.0f, (float) (getHeight()>>1),new Color(1,51,90,255),
                                                 (float)(getWidth()>>1), 80.0f,new Color(43,105,152,255));
             //GradientPaint gp = new GradientPaint(0.0f, (float) getHeight(),Utils.colorAlpha(0,0,0,getAlpha()),
             //                                    0.0f, 0.0f,Utils.colorAlpha(90,122,166,getAlpha()));
             RoundBorder mainBorder = getRoundBorder();
             Insets outerIns = super.getInsets();
             int x = outerIns.left;
             int y = outerIns.top;
             int w = getWidth()-outerIns.left-outerIns.right;
             int h = getHeight()-outerIns.top-outerIns.bottom;
 
             RoundRectangle2D outerBorderShape = OvalBorder.createOuterShape(x,y,w,h,
                     mainBorder.getRecW(), mainBorder.getRecH(),outerIns);
             
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
             g2.setPaint(gp);
             g2.fillRoundRect(x+1, y+1, w-1, h-1,(int)mainBorder.getRecW(),(int)mainBorder.getRecH());
             
             BorderEfects.paintBorderGlow(g2,4,outerBorderShape,colorBorderGlow);
             g2.dispose();
         }        
     }
 
     @Override
     public Insets getInsets() {
         Insets in = super.getInsets();
         Insets inner = getInnerGap();
         return new Insets(in.top+inner.top, in.left+inner.left,
                 in.bottom+inner.bottom, in.right+inner.right);
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         
 //        if(!toggleButton.isVisible())
   //          toggleButton.showToggleButton();
     //    else
       //      toggleButton.hideToggleButton();
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
        
         if(e.getButton()==MouseEvent.BUTTON1 && e.getX()<sensitiveMouseReaction){
             System.out.println("Resize X side "+getClass().getSimpleName());
             resizeWidthPanel = true;
         }else if(e.getButton()==MouseEvent.BUTTON1 && e.getY()<sensitiveMouseReaction){
             System.out.println("Resize Y side "+getClass().getSimpleName());
             resizeHeghtPanel = true;
         }
     }
     @Override
     public void mouseReleased(MouseEvent e) {
 
         if(needRevalidate || !isDynamicRevalidate()){                        
             revalidate();
             needRevalidate=false;
         }
         resizeWidthPanel = false;
         resizeHeghtPanel = false;
     }
     @Override
     public void mouseEntered(MouseEvent e) {}
     @Override
     public void mouseExited(MouseEvent e) {
         //resizeWidthPanel = false;
         //resizeHeghtPanel = false;
     }
     @Override
     public void mouseDragged(MouseEvent e) {
         if(resizeWidthPanel || resizeHeghtPanel){
             int width =0,height = 0;
             if(resizeWidthPanel){
                 width = getWidth()-e.getX()+2;
                 setSize(width, getHeight());
             }
             else{
                 height = getHeight()-e.getY();
                 setSize(getWidth(), height);
             }
             updateMyUI();
             needRevalidate=true;
         }
     }
     @Override
     public void mouseMoved(MouseEvent e) {
         
         if(e.getX()<sensitiveMouseReaction||e.getY()<sensitiveMouseReaction){
             setCursor(Cursor.getPredefinedCursor( e.getX()<sensitiveMouseReaction ?
                                       Cursor.W_RESIZE_CURSOR:Cursor.N_RESIZE_CURSOR));
             cursorChanged = true;           
         }else if(cursorChanged && !resizeWidthPanel&&!resizeHeghtPanel){
             setCursor(Cursor.getDefaultCursor());
         }
     }
 
     /**
      * @return the decoratePanel
      */
     public boolean isDecoratedWindow() {
         return decoratedWindow;
     }
 
     /**
      * @param decoratePanel the decoratePanel to set
      */
     public void setDecorateWindow(boolean decorated) {
         if(decorated){
             if(!this.isAncestorOf(this.decorate)){
                 add(this.decorate,BorderLayout.NORTH);
             }
         }else{
             this.remove(this.decorate);
         }
         this.decoratedWindow = decorated;
         revalidate();
     }
 
     /**
      * @return the dynamicRevalidate
      */
     public boolean isDynamicRevalidate() {
         return dynamicRevalidate;
     }
 
     public boolean setAlphaBorder(float alphaBorder){
         Border bord = getBorder();
         if(bord instanceof AlphaBorder){
             return ((AlphaBorder)bord).setAlpha(alphaBorder);
         }
         return false;
     }
 
     public void setAlphaToAllRootWindow(float alpha){
         setAlphaBorder(alpha);
         setAlpha(alpha);
     }
     /**
      * @param dynamicRevalidate the dynamicRevalidate to set
      */
     public void setDynamicRevalidate(boolean dynamicRevalidate) {
         this.dynamicRevalidate = dynamicRevalidate;
     }   
 
     /**
      * @return the decorate
      */
     public AbstractDecoratePanel getDecoratePanel() {
         return decorate;
     }
 
     /**
      * @param decorate the decorate to set
      */
     public void setDecoratePanel(AbstractDecoratePanel decorate) {
         this.decorate = decorate;
     }
 
     /**
      * @return the innerGap
      */
     public Insets getInnerGap() {
         return innerGap;
     }
 
     /**
      * @param innerGap the innerGap to set
      */
     public void setInnerGap(Insets innerGap) {
         this.innerGap = innerGap;
     }    
 
     @Override
     public void focusGained(FocusEvent e) {
         System.out.println("Focus gained");
     }
 
     @Override
     public void focusLost(FocusEvent e) {
         System.out.println("Focus lost");
     }
 
     /**
      * @return the decorateAndContentGap
      */
     public int getDecorateAndContentGap() {
         return decorateAndContentGap;
     }
 
     /**
      * @param decorateAndContentGap the decorateAndContentGap to set
      */
     public void setDecorateAndContentGap(int decorateAndContentGap) {
         LayoutManager lm = getLayout();
         if(lm instanceof BorderLayout){
             ((BorderLayout)lm).setVgap(decorateAndContentGap);
         }
         this.decorateAndContentGap = decorateAndContentGap;
         lm.layoutContainer(this);
         if(isVisible())
             getRoundWindowRootPane().revalidate();
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean retVal = false;
         if(!(obj instanceof RoundWindow))
             return retVal;
         RoundWindow tmp = (RoundWindow)obj;
         retVal = getDecoratePanel().getTitle().equals(tmp.getDecoratePanel().getTitle());
         
         return retVal;
     }
 
     private class WindowDisplayBehavior implements TimingTarget{
         @Override
         public void timingEvent(float arg0) {
             if(arg0>0 && arg0<Math.max(getContentPane().getUpperThresholdAlpha(),
                                        getUpperThresholdAlpha())){
                 Color outerColor = ((OvalBorder)getRoundBorder()).getBorderColor();
 
                 if(arg0<getRoundBorder().getUpperThresholdAlpha()){
                     ((OvalBorder)getRoundBorder()).setBorderColor(Utils.colorAlpha(outerColor, arg0));
                 }
                 setAlphaToAllRootWindow(arg0);
                 getContentPane().setAlpha(arg0);
                 getDecoratePanel().setAlpha(arg0);
                 repaint();
             }else
                 animator.stop();
         }
         @Override
         public void begin() {
             if(isEnabled())
                 setVisible(isEnabled());
         }
         @Override
         public void end() {
             if(!isEnabled())
                 setVisible(isEnabled());
         }
         @Override
         public void repeat() {
         }
     }
 /*
     class CloseAction implements ActionListener,TimingTarget{
         private Animator animator;
 
         public CloseAction(){
             animator = new Animator(animationDuration, 1,
             RepeatBehavior.LOOP, this);
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
            animator.setStartFraction(getContentPane().getAlpha());
            animator.setStartDirection(Direction.BACKWARD);
            animator.start();
         }
 
         @Override
         public void timingEvent(float arg0) {
             if(getContentPane().setAlpha(arg0)){
             setSize(getWidth()-1, getHeight());
             updateMyUI();
             //repaint();
             }else{
                 end();
             }
         }
         @Override
         public void begin() {}
         @Override
         public void end() {}
         @Override
         public void repeat() {}
     }
  */
 }
