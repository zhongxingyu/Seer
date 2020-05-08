 /*
  * NaviToolbar.java
  *
  * Created on 2009-04-15, 20:45:36
  */
 
 package app.navigps.gui.ToolBar;
 
 import app.navigps.gui.ToolBar.UI.NaviToolBarUI;
 import app.navigps.gui.borders.OvalBorder;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.RoundRectangle2D;
 import javax.swing.JToolBar;
 import javax.swing.border.Border;
 import org.jdesktop.animation.timing.Animator;
 import org.jdesktop.animation.timing.TimingTargetAdapter;
 import org.jdesktop.animation.timing.interpolation.PropertySetter;
 
 /**
  *
  * @author Grzegorz (wara) Warywoda
  */
 public class NaviToolBar extends JToolBar{
 
     private  Animator animator;
 
     public NaviToolBar(String name){
         super(name);
         init();
     }
 
     private void init() {
         setBorder(new OvalBorder(3,3,3,3,10,10,new Color(166,166,166)));
         addSeparator();
         setUI(new NaviToolBarUI());
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         Border bord = getBorder();
         if(bord instanceof OvalBorder){
 
             OvalBorder ovb= (OvalBorder)bord;
             RoundRectangle2D clip = new RoundRectangle2D.Double(-1,-1,
                     getWidth()+1,getHeight()+1, ovb.getRecW(), ovb.getRecH());            
             Area newClip = new Area(g.getClip());
             Area visbClip = new Area(clip);
             newClip.intersect(visbClip);
             GeneralPath gpClip = new GeneralPath(newClip);
             g.setClip(gpClip);
         }
         super.paintComponent(g);
     }
 
     @Override
     public void setVisible(boolean aFlag) {
         if(isVisible() != aFlag){
             super.setVisible(aFlag);
         }
     }
 
     @Override
     public void setLocation(Point p) {
         super.setLocation(p);
     }
 
     @Override
     public void setBounds(Rectangle r) {
         animationBounds(this, r);
     }
 
     private void animationBounds(Component comp,Rectangle newrec){
         Rectangle oldrec = comp.getBounds();//current position
         if(newrec.equals(oldrec)){
             //System.out.println("the same rect, return");
             return;
         }
         comp.setLocation(newrec.x, newrec.y);
         Dimension oldDim = new Dimension(oldrec.width, oldrec.height);
         Dimension newDim = new Dimension(newrec.width, newrec.height);
 
         if(animator != null && animator.isRunning()){
             animator.stop();
         }
 
         animator = PropertySetter.createAnimator(500,comp,"size",oldDim,newDim);
         animator.setDeceleration(.7f);
         animator.addTarget(new TimingTargetAdapter(){
 
             @Override
             public void timingEvent(float fraction) {
                 //System.out.println("timing event "+fraction);
                //repaint();                
             }
 
             @Override
             public void end() {
                 //animatorCount--;
                 //repaint();
                validate();
             }
 
             @Override
             public void begin() {
                 //animatorCount++;
             }
 
         });
         animator.start();
     }
 }
