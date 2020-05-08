 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Sep 17, 2007
  */
 package com.soartech.simjr.ui;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import com.soartech.math.Vector3;
 import com.soartech.simjr.adaptables.Adaptables;
 import com.soartech.simjr.sim.Entity;
 import com.soartech.simjr.sim.EntityConstants;
 import com.soartech.simjr.sim.EntityController;
 import com.soartech.simjr.sim.EntityPrototype;
 import com.soartech.simjr.sim.EntityPrototypes;
 import com.soartech.simjr.sim.SimpleTerrain;
 import com.soartech.simjr.sim.Simulation;
 import com.soartech.simjr.sim.entities.DefaultEntity;
 import com.soartech.simjr.util.SwingTools;
 
 /**
  * A small window with controls for manipulating an entity's velocity vector.
  * 
  * @author ray
  */
 public class EntityVelocityController extends JPanel implements EntityController
 {
     private static final long serialVersionUID = -1821108908003291325L;
     
     private static final int XY_HEIGHT = 150;
     private static final int Z_WIDTH = 20;
     private static final Stroke XY_VECTOR_STROKE = new BasicStroke(3.0f);
     private static final Stroke Z_INDICATOR_STROKE = new BasicStroke(3.0f);
     private static final double CLICK_TOLERANCE = 10.0;
     private static final int END_RADIUS = 4;
     
     private Entity entity;
     private double maxSpeed = 350.0;
     private double transitionSpeed = 5.0;
     private double numberOfOvalsInController = 4.0;
     private double transitionRing = 1.0;
     private boolean linearController = true;
     
     private List<EntityController> oldControllers = new ArrayList<EntityController>();
     
     private ZController zController = new ZController();
     private XYController xyController = new XYController();
     
     /**
      * Create a manual controller window for the given entity at the given screen
      * location. If an entity controller is already installed on the vehicle it
      * is removed. When the manual controller is closed, the original controller
      * is restored to the entity.
      * 
      * @param entity the entity to control
      * @param location the screen location of the controller window
      * @return the frame containing the controller window.
      */
     public static JFrame createControllerFrame(Entity entity, Point location)
     {
         JFrame frame = new JFrame(entity.getName());
         
         Insets insets = frame.getInsets();
         frame.setSize(XY_HEIGHT + Z_WIDTH, XY_HEIGHT + insets.bottom + insets.top);
         
         final EntityVelocityController controller = new EntityVelocityController(entity);
         frame.setContentPane(controller);
         frame.setAlwaysOnTop(true);
         frame.setVisible(true);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent arg0)
             {
                 controller.dispose();
             }});
         
         if(location != null)
         {
             frame.setLocation(location);
         }
         return frame;
     }
     
     /**
      * @param entity
      */
     public EntityVelocityController(Entity entity)
     {
         super(new BorderLayout());
         
         entity.addCapability(this);
         assert this.entity == entity;
         
         final EntityPrototype typeInfo = entity.getPrototype();
         maxSpeed = typeInfo.getProperty(EntityConstants.PROPERTY_MAXSPEED, maxSpeed);
         
         add(zController, BorderLayout.WEST);
         add(xyController, BorderLayout.CENTER);
     }
     
     public void setMaxSpeed(double maxSpeed)
     {
         this.maxSpeed = maxSpeed;
     }
     
     public void setTransitionSpeed(double transitionSpeed)
     {
         this.transitionSpeed = transitionSpeed;
     }
     
     public void setTransitionRing(int ring)
     {
         this.transitionRing = ring;
     }
     
     public void setNumberOfOvalsInController(int ovals)
     {
         this.numberOfOvalsInController = ovals;
     }
     
     public void setLinearController(boolean isLinear)
     {
         this.linearController = isLinear;
     }
     
     /**
      * Call dispose() to remove the controller from its entity.
      */
     public void dispose()
     {
         synchronized (entity.getSimulation().getLock())
         {
             entity.setVelocity(Vector3.ZERO);
             entity.removeCapability(this);
             assert entity == null;
         }
     }
     
     private void setVelocity(Vector3 v)
     {
         entity.setVelocity(v);
         
         if(!new Vector3(v.x, v.y, 0.0).epsilonEquals(Vector3.ZERO))
         {
             entity.setHeading(Math.atan2(v.y, v.x));
         }
         repaint();
     }
     
     private void setPanelExtents(JComponent c, int width, int height)
     {
         Dimension d = new Dimension(width, height);
         
         c.setPreferredSize(d);
         c.setMinimumSize(d);
         c.setMaximumSize(d);
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.EntityController#getEntity()
      */
     public Entity getEntity()
     {
         return entity;
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.EntityCapability#attach(com.soartech.simjr.sim.Entity)
      */
     public void attach(Entity entity)
     {
         if(this.entity != null)
         {
             throw new IllegalStateException("already attached to entity");
         }
         
         this.entity = entity;
         
         //remove any existing entity controller capabilities form the entity
         oldControllers.clear();
         EntityController ec = Adaptables.adapt(entity, EntityController.class);
         while(ec != null)
         {
             oldControllers.add(ec);
             entity.removeCapability(ec);
             ec = Adaptables.adapt(entity, EntityController.class);
         }
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.EntityCapability#detach()
      */
     public void detach()
     {
         //re-attach the old entity controllers
         for(EntityController ec : oldControllers)
         {
             if(ec != null)
             {
                 entity.addCapability(ec);
             }
         }
         
         entity = null;
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.EntityController#openDebugger()
      */
     public void openDebugger()
     {
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.Tickable#tick(double)
      */
     @Override
     public void tick(double dt)
     {
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
      */
     public Object getAdapter(Class<?> klass)
     {
         return Adaptables.adaptUnchecked(this, klass, false);
     }
 
     /* (non-Javadoc)
      * @see java.awt.Component#toString()
      */
     @Override
     public String toString()
     {
         return "Manual";
     }
 
     private void drawEnd(Graphics g, int x, int y, boolean fill)
     {
         if(fill)
         {
             g.fillOval(x - END_RADIUS, y - END_RADIUS, 2 * END_RADIUS + 1, 2 * END_RADIUS + 1);
         }
         else
         {
             g.drawOval(x - END_RADIUS, y - END_RADIUS, 2 * END_RADIUS + 1, 2 * END_RADIUS + 1);
         }
     }
     
     private class XYController extends JPanel
     {
         private static final long serialVersionUID = 5020576921434286997L;
         private boolean dragging = false;
         
         public XYController()
         {
             setBorder(BorderFactory.createLineBorder(Color.BLACK));
             setToolTipText("");
             addMouseListener(new MouseAdapter() {
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mousePressed(MouseEvent e)
                 {
                     if(!SwingUtilities.isLeftMouseButton(e))
                     {
                         return;
                     }
                     
                     if(e.getPoint().distance(getSpeedPoint()) < CLICK_TOLERANCE)
                     {
                         dragging = true;
                     }
                 }
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseReleased(MouseEvent e)
                 {
                     repaint();
                     dragging = false;
                 }
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseClicked(MouseEvent e)
                 {
                     Point center = new Point(getWidth() / 2, getHeight() / 2);
                     if(SwingUtilities.isLeftMouseButton(e) &&
                        (e.getPoint().distance(center) < CLICK_TOLERANCE ||
                         e.getPoint().distance(getSpeedPoint()) < CLICK_TOLERANCE))
                     {
                         setVelocity(Vector3.ZERO);
                     }
                 }});
             
             addMouseMotionListener(new MouseMotionAdapter() {
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseDragged(MouseEvent e)
                 {
                     if(dragging)
                     {
                         updateVelocity(e.getPoint());
                     }
                 }});
         }
         
         private void updateVelocity(Point p)
         {
             // Modified to be either a linear controller or a linear + exponential hybrid by ALT
             
             if(p.x < 0) p.x = 0;
             if(p.x >= getWidth()) p.x = getWidth() - 1;
             if(p.y < 0) p.y = 0;
             if(p.y >= getHeight()) p.y = getHeight() - 1;
             
             Point center = new Point(getWidth() / 2, getHeight() / 2);
             double xOff = p.x - center.x;
             double yOff = -(p.y - center.y);
             
             // Exponential Curve
             
             double xPercentOnCurve = xOff / (getWidth() / 2.0);
             double yPercentOnCurve = yOff / (getHeight() / 2.0);
             
             // Pythagorean Theorem
             double percentOfMaxTopSpeed = Math.sqrt(Math.pow(xPercentOnCurve, 2) + Math.pow(yPercentOnCurve, 2));
             
             if (percentOfMaxTopSpeed > 1.0)
             {
                 percentOfMaxTopSpeed = 1.0;
             }
             else if (percentOfMaxTopSpeed < 0.0)
             {
                 percentOfMaxTopSpeed = 0.0;
                 throw new AssertionError("Math doesn't work!");
             }
             
             double x = 0.0;
             double y = 0.0;
             
             if (linearController)
             {
                double arcTan = Math.atan(Math.abs(yPercentOnCurve/xPercentOnCurve));
                 
                 x = Math.abs(percentOfMaxTopSpeed * Math.cos(arcTan)) * maxSpeed;
                 y = Math.abs(percentOfMaxTopSpeed * Math.sin(arcTan)) * maxSpeed;
                 
                 if (xOff < 0.0)
                 {
                     x *= -1;
                 }
                 
                 if (yOff < 0.0)
                 {
                     y *= -1;
                 }
             }
             else if (percentOfMaxTopSpeed <= (transitionRing/numberOfOvalsInController))
             {
                 // Linear
                 x = xPercentOnCurve / (transitionRing/numberOfOvalsInController) * transitionSpeed;
                 y = yPercentOnCurve / (transitionRing/numberOfOvalsInController) * transitionSpeed;
             }
             else if (percentOfMaxTopSpeed > (transitionRing/numberOfOvalsInController))
             {   
                 // Non-Linear, Exponential
                 // Derived via Y=CB^X with two equations
                 
                 // T = Top Speed
                 // M = Transition Speed
                 // N = Number of Ovals in Controller
                 // R = The Ring to Complete the Transition On
                 // R is between 1 and N
                 
                 // T = C * B ^ 1
                 // M = C * B ^ R/N
                 
                 // C = M * (T/M)^(-R/(N-R))
                 double C = Math.abs(transitionSpeed * Math.pow(maxSpeed/transitionSpeed, -transitionRing/(numberOfOvalsInController - transitionRing)));
                 // B = (T/M)^(1.0/(1.0 - R/N))
                 double B = Math.abs(Math.pow(maxSpeed/transitionSpeed, 1.0/(1.0 - transitionRing/numberOfOvalsInController)));
                 
                 // The magnitude of the velocity vector (always positive)
                 double speed = C * Math.pow(B, percentOfMaxTopSpeed);
                 
                 // Calculation of the vector math here
                 //
                 // |
                 // |     o
                 // |    /|
                 // | v / |
                 // |  /  | y
                 // | /   |
                 // _/____|_________________
                 //     x
                 //
                 // x = v * cos(arctan(y/x))
                 // y = v * sin(arctan(y/x))
                 
                 // If we do these as absolute values, then at the
                 // end we just check if xOff and yOff are negative
                 // and apply the correction
                 
                 // The angle of the velocity vector
                double arcTan = Math.atan(Math.abs(yPercentOnCurve/xPercentOnCurve));
                 
                 x = Math.abs(speed * Math.cos(arcTan));
                 y = Math.abs(speed * Math.sin(arcTan));
                 
                 if (xOff < 0.0)
                 {
                     x *= -1;
                 }
                 
                 if (yOff < 0.0)
                 {
                     y *= -1;
                 }
             }
             
             setVelocity(new Vector3(x, y, entity.getVelocity().z));
         }
         
         // Modified to be either a linear controller or a linear + exponential hybrid by ALT
         private Vector3 speedToPixel(Vector3 velo)
         {
             double totalSpeed = Math.sqrt(Math.pow(velo.x, 2) + Math.pow(velo.y, 2));
                                     
             double percentageX = 0.0;
             double percentageY = 0.0;
             
             if (linearController)
             {
                 percentageX = Math.abs(velo.x) / maxSpeed;
                 percentageY = Math.abs(velo.y) / maxSpeed;
                 
                 if (velo.x < 0.0)
                 {
                     percentageX *= -1;
                 }
                 
                 if (velo.y < 0.0)
                 {
                     percentageY *= -1;
                 }
             }
             else if (totalSpeed <= transitionSpeed)
             {
                 // Linear Speed Calculation
                 percentageX = (Math.abs(velo.x) / transitionSpeed) * (transitionRing/numberOfOvalsInController);
                 percentageY = (Math.abs(velo.y) / transitionSpeed) * (transitionRing/numberOfOvalsInController);
                 
                 if (velo.x < 0.0)
                 {
                     percentageX *= -1;
                 }
                 
                 if (velo.y < 0.0)
                 {
                     percentageY *= -1;
                 }
             }
             else
             {
                 // Exponential Speed Calculation
                 
                 // We are getting the % in pixels based on the total speed, length of the velocity vector
                 
                 // T = Top Speed
                 // M = Transition Speed
                 // N = Number of Ovals in Controller
                 // R = The Ring to Complete the Transition On
                 // R is between 1 and N
                 
                 // This is: (T/M)^(1.0/(1.0 - R/N)
                 // Otherwise known as B
                 double logConstant = Math.pow(maxSpeed/transitionSpeed, 1.0/(1.0 - transitionRing/numberOfOvalsInController));
                 // This is: 1/(M*((T/M)^(-R/(N-R))))
                 // Otherwise known as C^(-1)
                 double CInverse = 1.0/(transitionSpeed * Math.pow(maxSpeed/transitionSpeed, -transitionRing/(numberOfOvalsInController-transitionRing)));
                 
                 // Basically we are taking the function: Y=C*B^X
                 // And solving for X.  The result is:
                 // Log(Base: B) of (Y * C^(-1)) = X
                 // Which using the change of base formula is:
                 // log(Y * C^(-1))/log(B) = X
                 double percentage = Math.log(Math.abs(totalSpeed) * CInverse) / Math.log(logConstant);
                 
                double arcTan = Math.atan(Math.abs(velo.y/velo.x));
                 
                 percentageX = Math.abs(percentage * Math.cos(arcTan));
                 percentageY = Math.abs(percentage * Math.sin(arcTan));
                 
                 if (velo.x < 0.0)
                 {
                     percentageX *= -1;
                 }
                 
                 if (velo.y < 0.0)
                 {
                     percentageY *= -1;
                 }
             }
             
             // Since we have the percentages, we return the percentages converted to pixels
             return new Vector3(percentageX * (getWidth() / 2.0), percentageY * (getHeight() / 2.0), 0.0);
         }
         
         private Point getSpeedPoint()
         {
             Point center = new Point(getWidth() / 2, getHeight() / 2);
             
             Vector3 velocity = entity.getVelocity();
             
             Vector3 pixels = speedToPixel(velocity);
             
             return new Point((new Double(center.x + pixels.x)).intValue(),
                              (new Double(center.y - pixels.y)).intValue());
         }
         
         /* (non-Javadoc)
          * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
          */
         @Override
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
             
             Graphics2D g2d = (Graphics2D) g;
             SwingTools.enableAntiAliasing(g2d);
             
             int width = getWidth();
             int height = getHeight();
             Point center = new Point(width / 2, height / 2);
             
             g.setColor(Color.WHITE);
             g.fillRect(0, 0, width, height);
             
             g.setColor(Color.GRAY);
             for(int i = 0; i < numberOfOvalsInController; ++i)
             {
                 double percentage = (i+1.0)/numberOfOvalsInController;
 
                 int x = (new Double(percentage * (getWidth() / 2.0))).intValue();
                 int y = (new Double(percentage * (getHeight() / 2.0))).intValue();
                 
                 g.drawOval(center.x - x, center.y - y, 2 * x, 2 * y);
             }
             
             Vector3 velocity = entity.getVelocity();
             Vector3 pixels = speedToPixel(velocity);
             int x = (new Double(center.x + pixels.x)).intValue();
             int y = (new Double(center.y - pixels.y)).intValue();
             
             g.setColor(Color.BLACK);
             g2d.setStroke(XY_VECTOR_STROKE);
             g.drawLine(center.x, center.y, x, y);
             drawEnd(g, x, y, true);
             
             if(dragging)
             {
                 g.setColor(Color.GREEN);
                 drawEnd(g, x, y, false);
             }
             g.setColor(Color.GRAY);
             drawEnd(g, center.x, center.y, true);
             g.drawString("dx/dy", 3, getHeight() - 5);
         }
 
         /* (non-Javadoc)
          * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
          */
         @Override
         public String getToolTipText(MouseEvent event)
         {
             if(event.getPoint().distance(getSpeedPoint()) < CLICK_TOLERANCE)
             {
                 Vector3 v = entity.getVelocity();
                 return String.format("dx=%.1f dy=%.1f dz=%.1f (m/s)", v.x, v.y, v.z);
 
             }
             return super.getToolTipText(event);
         }
     }
     
     private class ZController extends JPanel
     {
         private static final long serialVersionUID = 2295455727570570840L;
         
         private boolean dragging = false;
         
         public ZController()
         {
             setPanelExtents(this, Z_WIDTH, XY_HEIGHT);
             setBorder(BorderFactory.createLineBorder(Color.BLACK));
             setToolTipText("");
             
             addMouseListener(new MouseAdapter() {
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseClicked(MouseEvent e)
                 {
                     // Clicking the center point or on the end point resets
                     // the vector to zero.
                     if(SwingUtilities.isLeftMouseButton(e) &&
                        (Math.abs(e.getPoint().y - getHeight() / 2) < CLICK_TOLERANCE) ||
                        Math.abs(e.getPoint().y - getSpeedPoint()) < CLICK_TOLERANCE)
                     {
                         Vector3 v = entity.getVelocity();
                         setVelocity(new Vector3(v.x, v.y, 0.0));
                     }
                 }
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mousePressed(MouseEvent e)
                 {
                     if(SwingUtilities.isLeftMouseButton(e) &&
                        Math.abs(e.getPoint().y - getSpeedPoint()) < CLICK_TOLERANCE)
                     {
                         dragging = true;
                         repaint();
                     }
                 }
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseReleased(MouseEvent e)
                 {
                     dragging = false;
                     repaint();
                 }});
             
             addMouseMotionListener(new MouseMotionAdapter() {
 
                 /* (non-Javadoc)
                  * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
                  */
                 @Override
                 public void mouseDragged(MouseEvent e)
                 {
                     if(!dragging)
                     {
                         return;
                     }
                     double zOff = (getHeight() / 2) - e.getPoint().y;
                     double z = (zOff / getHeight()) * 2.0 * maxSpeed;
                     Vector3 v = entity.getVelocity();
                     setVelocity(new Vector3(v.x, v.y, z));
                 }});
         }
 
         private int getSpeedPoint()
         {
             return getHeight() / 2 - speedToPixelY(entity.getVelocity().z);
         }
         
         private int speedToPixelY(double speed)
         {
             return (int) ((speed / (maxSpeed * 2.0)) * (double) getHeight());
         }
         
         /* (non-Javadoc)
          * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
          */
         @Override
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
             
             Graphics2D g2d = (Graphics2D) g;
             SwingTools.enableAntiAliasing(g2d);
             
             int width = getWidth();
             int height = getHeight();
             Point center = new Point(width / 2, height / 2);
             
             g.setColor(Color.WHITE);
             g.fillRect(0, 0, width, height);
             
             g.setColor(Color.GRAY);
             g.drawString("dz", 2, getHeight() - 5);
             g.drawLine(0, center.y, width, center.y);
             double ds = maxSpeed / 4;
             for(int i = 0; i < 4; ++i)
             {
                 double speed = maxSpeed - ds * i;
                 int y = speedToPixelY(speed);
                 
                 g.drawLine(0, center.y - y, width, center.y - y);
                 g.drawLine(0, center.y + y, width, center.y + y);
             }
             
             Vector3 velocity = entity.getVelocity();
             int y = center.y - speedToPixelY(velocity.z);
             
             g.setColor(Color.BLACK);
             g2d.setStroke(Z_INDICATOR_STROKE);
             g.drawLine(center.x, y, center.x, center.y);
             
             drawEnd(g, center.x, y, true);
             g.fillOval(center.x - END_RADIUS, y - END_RADIUS, 2 * END_RADIUS + 1, 2 * END_RADIUS + 1);
             if(dragging)
             {
                 g.setColor(Color.GREEN);
                 drawEnd(g, center.x, y, false);
             }
             g.setColor(Color.GRAY);
             drawEnd(g, center.x, center.y, true);
         }
 
         /* (non-Javadoc)
          * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
          */
         @Override
         public String getToolTipText(MouseEvent event)
         {
             if(Math.abs(event.getPoint().y - getSpeedPoint()) < CLICK_TOLERANCE)
             {
                 Vector3 v = entity.getVelocity();
                 return String.format("dx=%.1f dy=%.1f dz=%.1f (m/s)", v.x, v.y, v.z);
             }
             return super.getToolTipText(event);
         }
         
     }
     
     public static void main(String[] args)
     {
         Simulation sim = new Simulation(SimpleTerrain.createExampleTerrain());
         Entity truck = new DefaultEntity("truck", EntityPrototypes.NULL);
         truck.setVelocity(new Vector3(50, 25, 75));
         sim.addEntity(truck);
         
         JFrame frame = createControllerFrame(truck, null);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 }
