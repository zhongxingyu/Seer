 package com.helper;
 /*
  * Copyright (c) 2007, Romain Guy
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *   * Neither the name of the TimingFramework project nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 import java.awt.GraphicsConfiguration;
 import java.awt.Transparency;
 import java.awt.Graphics;
 import java.awt.GraphicsEnvironment;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.io.IOException;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.FlatteningPathIterator;
 import java.awt.geom.IllegalPathStateException;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 import java.awt.GraphicsConfiguration;
 import java.awt.Transparency;
 import java.awt.Graphics;
 import java.awt.GraphicsEnvironment;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.io.IOException;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LinearGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.RoundRectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Map;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import org.jdesktop.animation.timing.Animator;
 import org.jdesktop.animation.timing.Animator.Direction;
 import org.jdesktop.animation.timing.interpolation.PropertySetter;
 import org.jdesktop.animation.timing.triggers.MouseTrigger;
 import org.jdesktop.animation.timing.triggers.MouseTriggerEvent;
 /**
  *
  * @author Romain Guy <romain.guy@mac.com>
  */
 
 public class MorphingPanel extends JLayeredPane {
     private JLayeredPane frame;
     protected static int height = 662;
     private DirectionButton button;
     
     public MorphingPanel(FadingButtonTF aud,FadingButtonTF pmen) {
         add(buildControls(aud,pmen),1);
        // JButton test = new JButton("home");
        // test.setPreferredSize(new Dimension(100,100));
        // test.setBounds(100,50,100,100);
        // add(test,0);
         frame = this;
     }
     
     public void setsize(int x ,int y){
     	frame.setSize(75, height);
     }
     
    public void runStage(){
    	button.setStage();
     }
         
     public void addBtn(JButton jb){
     	frame.add(jb,0);
     }
     
     private JComponent buildControls(FadingButtonTF aud,FadingButtonTF pmen) {
         button = new DirectionButton("", frame,aud,pmen);
         button.setBounds(0, 0, 100, height);
         
         return button;
     }
     
     public static class DirectionButton extends JButton implements MouseListener {
         
         private Map desktopHints;
         private float morphing = 0.0f;
         private JLayeredPane p;
         private FadingButtonTF aud,pmen;
         private boolean coming, runonce;
         private Animator animator;
         
         private DirectionButton(String text, JLayeredPane p, FadingButtonTF aud,FadingButtonTF pmen) {
             super("");
             coming = true;
             runonce = false;
             this.p = p;
             this.aud = aud;
             this.pmen = pmen;
             this.addMouseListener(this);
             setupTriggers();
             setFont(getFont().deriveFont(Font.BOLD));
             setOpaque(false);
             setBorderPainted(false);
             setContentAreaFilled(false);
             setFocusPainted(false);
         }
         
        public void setStage(){
         	this.doClick();
         }
         
         private void setupTriggers() {
             animator = PropertySetter.createAnimator(
                     1500, this, "morphing", 0.0f, 1.0f);
             animator.setAcceleration(0.2f);
             animator.setDeceleration(0.3f);
             //MouseTrigger.addTrigger(this, animator, MouseTriggerEvent.ENTER, false);
         }
         
         private Morphing2D createMorph() {
             Shape sourceShape = new RoundRectangle2D.Double(0, 0,
                     15 , height, 0, 12.0);
             
             Shape destinationShape = new RoundRectangle2D.Double(0, 0,
                     100 , height, 0, 12.0);
            
             return new Morphing2D(sourceShape, destinationShape);
         }
         
         private Morphing2D createMorphBack() {
             Shape sourceShape = new RoundRectangle2D.Double(0, 0,
                     15 , height, 0, 12.0);
             
             Shape destinationShape = new RoundRectangle2D.Double(0, 0,
                     100 , height, 0, 12.0);
            
             return new Morphing2D(destinationShape,sourceShape);
         }
         
         public float getMorphing() {
             return morphing;
         }
 
         public void setMorphing(float morphing) {
             this.morphing = morphing;
             repaint();
         }
         
         @Override
         protected void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D) g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
             
             if (desktopHints == null) {
                 Toolkit tk = Toolkit.getDefaultToolkit();
                 desktopHints = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
             }
 
             if (desktopHints != null) {
                 g2.addRenderingHints(desktopHints);
             }
             
             LinearGradientPaint p;
             Color[] colors;
             if (!getModel().isArmed()) {
                 colors = new Color[] {
                 		Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK};
             } else {
                 colors = new Color[] {
                 		Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK};
             }
             
             p = new LinearGradientPaint(0.0f, 0.0f, 0.0f, getHeight(),
                 new float[] { 0.0f, 0.5f, 0.501f, 1.0f },
                 colors);
             
             g2.setPaint(p);
             Morphing2D morph;
             if(coming){
             	morph = createMorph();
             }
             else{
             	morph = createMorphBack();
             }
             morph.setMorphing(getMorphing());
             g2.fill(morph);
             
             int width = g2.getFontMetrics().stringWidth(getText());
             
             int x = (getWidth() - width) / 2;
             int y = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1;
             
             g2.setColor(Color.WHITE);
             g2.drawString(getText(), x, y + 1);
             g2.setColor(Color.WHITE);
             g2.drawString(getText(), x, y);
         }
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if(!animator.isRunning()){
 				if(runonce){
 					if(coming){
 						coming=false;
 						aud.disable();
 						pmen.disable();
 					} else{
 						coming = true;
 						aud.begin();
 						pmen.begin();
 					}
 				}
 				else{
 					runonce = true;
 					aud.begin();
 					pmen.begin();
 				}
 				animator.start();
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
     }
 
 }
 /*
  * $Id: Morphing2D.java,v 1.1 2007/01/26 17:35:35 gfx Exp $
  *
  * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
  * Santa Clara, California 95054, U.S.A. All rights reserved.
  *
  * Licensed under LGPL.
  */
 
 /**
  * <p>A morphing shape is a shape which geometry is constructed from two
  * other shapes: a start shape and an end shape.</p>
  * <p>The morphing property of a morphing shape defines the amount of
  * transformation applied to the start shape to turn it into the end shape.</p>
  * <p>Both shapes must have the same winding rule.</p>
  *
  * @author Jim Graham
  * @author Romain Guy <romain.guy@mac.com> (Maintainer)
  */
 class Morphing2D implements Shape {
     private double morph;
     private Geometry startGeometry;
     private Geometry endGeometry;
 
     /**
      * <p>Creates a new morphing shape. A morphing shape can be used to turn
      * one shape into another one. The transformation can be controlled by the
      * morph property.</p>
      *
      * @param startShape the shape to morph from
      * @param endShape   the shape to morph to
      *
      * @throws IllegalPathStateException if the shapes do not have the same
      *                                   winding rule
      * @see #getMorphing()
      * @see #setMorphing(double)
      */
     public Morphing2D(Shape startShape, Shape endShape) {
         startGeometry = new Geometry(startShape);
         endGeometry = new Geometry(endShape);
         if (startGeometry.getWindingRule() != endGeometry.getWindingRule()) {
             throw new IllegalPathStateException("shapes must use same " +
                                                 "winding rule");
         }
         double tvals0[] = startGeometry.getTvals();
         double tvals1[] = endGeometry.getTvals();
         double masterTvals[] = mergeTvals(tvals0, tvals1);
         startGeometry.setTvals(masterTvals);
         endGeometry.setTvals(masterTvals);
     }
 
     /**
      * <p>Returns the morphing value between the two shapes.</p>
      *
      * @return the morphing value between the two shapes
      *
      * @see #setMorphing(double)
      */
     public double getMorphing() {
         return morph;
     }
 
     /**
      * <p>Sets the morphing value between the two shapes. This value controls
      * the transformation from the start shape to the end shape. A value of 0.0
      * is the start shap. A value of 1.0 is the end shape. A value of 0.5 is a
      * new shape, morphed half way from the start shape to the end shape.</p>
      * <p>The specified value should be between 0.0 and 1.0. If not, the value
      * is clamped in the appropriate range.</p>
      *
      * @param morph the morphing value between the two shapes
      *
      * @see #getMorphing()
      */
     public void setMorphing(double morph) {
         if (morph > 1) {
             morph = 1;
         } else if (morph >= 0) {
             // morphing is finite, not NaN, and in range
         } else {
             // morph is < 0 or NaN
             morph = 0;
         }
         this.morph = morph;
     }
 
     private static double interp(double v0, double v1, double t) {
         return (v0 + ((v1 - v0) * t));
     }
 
     private static double[] mergeTvals(double tvals0[], double tvals1[]) {
         int i0 = 0;
         int i1 = 0;
         int numtvals = 0;
         while (i0 < tvals0.length && i1 < tvals1.length) {
             double t0 = tvals0[i0];
             double t1 = tvals1[i1];
             if (t0 <= t1) {
                 i0++;
             }
             if (t1 <= t0) {
                 i1++;
             }
             numtvals++;
         }
         double newtvals[] = new double[numtvals];
         i0 = 0;
         i1 = 0;
         numtvals = 0;
         while (i0 < tvals0.length && i1 < tvals1.length) {
             double t0 = tvals0[i0];
             double t1 = tvals1[i1];
             if (t0 <= t1) {
                 newtvals[numtvals] = t0;
                 i0++;
             }
             if (t1 <= t0) {
                 newtvals[numtvals] = t1;
                 i1++;
             }
             numtvals++;
         }
         return newtvals;
     }
 
     /**
      * @{inheritDoc}
      */
     public Rectangle getBounds() {
         return getBounds2D().getBounds();
     }
 
     /**
      * @{inheritDoc}
      */
     public Rectangle2D getBounds2D() {
         int n = startGeometry.getNumCoords();
         double xmin, ymin, xmax, ymax;
         xmin = xmax = interp(startGeometry.getCoord(0), endGeometry.getCoord(0),
                              morph);
         ymin = ymax = interp(startGeometry.getCoord(1), endGeometry.getCoord(1),
                              morph);
         for (int i = 2; i < n; i += 2) {
             double x = interp(startGeometry.getCoord(i),
                               endGeometry.getCoord(i), morph);
             double y = interp(startGeometry.getCoord(i + 1),
                               endGeometry.getCoord(i + 1), morph);
             if (xmin > x) {
                 xmin = x;
             }
             if (ymin > y) {
                 ymin = y;
             }
             if (xmax < x) {
                 xmax = x;
             }
             if (ymax < y) {
                 ymax = y;
             }
         }
         return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean contains(double x, double y) {
         throw new InternalError("unimplemented");
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean contains(Point2D p) {
         return contains(p.getX(), p.getY());
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean intersects(double x, double y, double w, double h) {
         throw new InternalError("unimplemented");
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean intersects(Rectangle2D r) {
         return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean contains(double x, double y, double w, double h) {
         throw new InternalError("unimplemented");
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean contains(Rectangle2D r) {
         return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
     }
 
     /**
      * @{inheritDoc}
      */
     public PathIterator getPathIterator(AffineTransform at) {
         return new Iterator(at, startGeometry, endGeometry, morph);
     }
 
     /**
      * @{inheritDoc}
      */
     public PathIterator getPathIterator(AffineTransform at, double flatness) {
         return new FlatteningPathIterator(getPathIterator(at), flatness);
     }
 
     private static class Geometry {
         static final double THIRD = (1.0 / 3.0);
         static final double MIN_LEN = 0.001;
         double bezierCoords[];
         int numCoords;
         int windingrule;
         double myTvals[];
 
         public Geometry(Shape s) {
             // Multiple of 6 plus 2 more for initial moveto
             bezierCoords = new double[20];
             PathIterator pi = s.getPathIterator(null);
             windingrule = pi.getWindingRule();
             if (pi.isDone()) {
                 // We will have 1 segment and it will be all zeros
                 // It will have 8 coordinates (2 for moveto, 6 for cubic)
                 numCoords = 8;
             }
             double coords[] = new double[6];
             int type = pi.currentSegment(coords);
             pi.next();
             if (type != PathIterator.SEG_MOVETO) {
                 throw new IllegalPathStateException("missing initial moveto");
             }
             double curx = bezierCoords[0] = coords[0];
             double cury = bezierCoords[1] = coords[1];
             double newx, newy;
             numCoords = 2;
             while (!pi.isDone()) {
                 if (numCoords + 6 > bezierCoords.length) {
                     // Keep array size to a multiple of 6 plus 2
                     int newsize = (numCoords - 2) * 2 + 2;
                     double newCoords[] = new double[newsize];
                     System.arraycopy(bezierCoords, 0, newCoords, 0, numCoords);
                     bezierCoords = newCoords;
                 }
                 switch (pi.currentSegment(coords)) {
                     case PathIterator.SEG_MOVETO:
                         throw new InternalError(
                                 "Cannot handle multiple subpaths");
                     case PathIterator.SEG_CLOSE:
                         if (curx == bezierCoords[0] && cury == bezierCoords[1])
                         {
                             break;
                         }
                         coords[0] = bezierCoords[0];
                         coords[1] = bezierCoords[1];
                         /* NO BREAK */
                     case PathIterator.SEG_LINETO:
                         newx = coords[0];
                         newy = coords[1];
                         // A third of the way from curxy to newxy:
                         bezierCoords[numCoords++] = interp(curx, newx, THIRD);
                         bezierCoords[numCoords++] = interp(cury, newy, THIRD);
                         // A third of the way from newxy back to curxy:
                         bezierCoords[numCoords++] = interp(newx, curx, THIRD);
                         bezierCoords[numCoords++] = interp(newy, cury, THIRD);
                         bezierCoords[numCoords++] = curx = newx;
                         bezierCoords[numCoords++] = cury = newy;
                         break;
                     case PathIterator.SEG_QUADTO:
                         double ctrlx = coords[0];
                         double ctrly = coords[1];
                         newx = coords[2];
                         newy = coords[3];
                         // A third of the way from ctrlxy back to curxy:
                         bezierCoords[numCoords++] = interp(ctrlx, curx, THIRD);
                         bezierCoords[numCoords++] = interp(ctrly, cury, THIRD);
                         // A third of the way from ctrlxy to newxy:
                         bezierCoords[numCoords++] = interp(ctrlx, newx, THIRD);
                         bezierCoords[numCoords++] = interp(ctrly, newy, THIRD);
                         bezierCoords[numCoords++] = curx = newx;
                         bezierCoords[numCoords++] = cury = newy;
                         break;
                     case PathIterator.SEG_CUBICTO:
                         bezierCoords[numCoords++] = coords[0];
                         bezierCoords[numCoords++] = coords[1];
                         bezierCoords[numCoords++] = coords[2];
                         bezierCoords[numCoords++] = coords[3];
                         bezierCoords[numCoords++] = curx = coords[4];
                         bezierCoords[numCoords++] = cury = coords[5];
                         break;
                 }
                 pi.next();
             }
             // Add closing segment if either:
             // - we only have initial moveto - expand it to an empty cubic
             // - or we are not back to the starting point
             if ((numCoords < 8) ||
                 curx != bezierCoords[0] ||
                 cury != bezierCoords[1]) {
                 newx = bezierCoords[0];
                 newy = bezierCoords[1];
                 // A third of the way from curxy to newxy:
                 bezierCoords[numCoords++] = interp(curx, newx, THIRD);
                 bezierCoords[numCoords++] = interp(cury, newy, THIRD);
                 // A third of the way from newxy back to curxy:
                 bezierCoords[numCoords++] = interp(newx, curx, THIRD);
                 bezierCoords[numCoords++] = interp(newy, cury, THIRD);
                 bezierCoords[numCoords++] = newx;
                 bezierCoords[numCoords++] = newy;
             }
             // Now find the segment endpoint with the smallest Y coordinate
             int minPt = 0;
             double minX = bezierCoords[0];
             double minY = bezierCoords[1];
             for (int ci = 6; ci < numCoords; ci += 6) {
                 double x = bezierCoords[ci];
                 double y = bezierCoords[ci + 1];
                 if (y < minY || (y == minY && x < minX)) {
                     minPt = ci;
                     minX = x;
                     minY = y;
                 }
             }
             // If the smallest Y coordinate is not the first coordinate,
             // rotate the points so that it is...
             if (minPt > 0) {
                 // Keep in mind that first 2 coords == last 2 coords
                 double newCoords[] = new double[numCoords];
                 // Copy all coordinates from minPt to the end of the
                 // array to the beginning of the new array
                 System.arraycopy(bezierCoords, minPt,
                                  newCoords, 0,
                                  numCoords - minPt);
                 // Now we do not want to copy 0,1 as they are duplicates
                 // of the last 2 coordinates which we just copied.  So
                 // we start the source copy at index 2, but we still
                 // copy a full minPt coordinates which copies the two
                 // coordinates that were at minPt to the last two elements
                 // of the array, thus ensuring that thew new array starts
                 // and ends with the same pair of coordinates...
                 System.arraycopy(bezierCoords, 2,
                                  newCoords, numCoords - minPt,
                                  minPt);
                 bezierCoords = newCoords;
             }
             /* Clockwise enforcement:
              * - This technique is based on the formula for calculating
              *   the area of a Polygon.  The standard formula is:
              *   Area(Poly) = 1/2 * sum(x[i]*y[i+1] - x[i+1]y[i])
              * - The returned area is negative if the polygon is
              *   "mostly clockwise" and positive if the polygon is
              *   "mostly counter-clockwise".
              * - One failure mode of the Area calculation is if the
              *   Polygon is self-intersecting.  This is due to the
              *   fact that the areas on each side of the self-intersection
              *   are bounded by segments which have opposite winding
              *   direction.  Thus, those areas will have opposite signs
              *   on the acccumulation of their area summations and end
              *   up canceling each other out partially.
              * - This failure mode of the algorithm in determining the
              *   exact magnitude of the area is not actually a big problem
              *   for our needs here since we are only using the sign of
              *   the resulting area to figure out the overall winding
              *   direction of the path.  If self-intersections cause
              *   different parts of the path to disagree as to the
              *   local winding direction, that is no matter as we just
              *   wait for the final answer to tell us which winding
              *   direction had greater representation.  If the final
              *   result is zero then the path was equal parts clockwise
              *   and counter-clockwise and we do not care about which
              *   way we order it as either way will require half of the
              *   path to unwind and re-wind itself.
              */
             double area = 0;
             // Note that first and last points are the same so we
             // do not need to process coords[0,1] against coords[n-2,n-1]
             curx = bezierCoords[0];
             cury = bezierCoords[1];
             for (int i = 2; i < numCoords; i += 2) {
                 newx = bezierCoords[i];
                 newy = bezierCoords[i + 1];
                 area += curx * newy - newx * cury;
                 curx = newx;
                 cury = newy;
             }
             if (area < 0) {
                 /* The area is negative so the shape was clockwise
                  * in a Euclidean sense.  But, our screen coordinate
                  * systems have the origin in the upper left so they
                  * are flipped.  Thus, this path "looks" ccw on the
                  * screen so we are flipping it to "look" clockwise.
                  * Note that the first and last points are the same
                  * so we do not need to swap them.
                  * (Not that it matters whether the paths end up cw
                  *  or ccw in the end as long as all of them are the
                  *  same, but above we called this section "Clockwise
                  *  Enforcement", so we do not want to be liars. ;-)
                  */
                 // Note that [0,1] do not need to be swapped with [n-2,n-1]
                 // So first pair to swap is [2,3] and [n-4,n-3]
                 int i = 2;
                 int j = numCoords - 4;
                 while (i < j) {
                     curx = bezierCoords[i];
                     cury = bezierCoords[i + 1];
                     bezierCoords[i] = bezierCoords[j];
                     bezierCoords[i + 1] = bezierCoords[j + 1];
                     bezierCoords[j] = curx;
                     bezierCoords[j + 1] = cury;
                     i += 2;
                     j -= 2;
                 }
             }
         }
 
         public int getWindingRule() {
             return windingrule;
         }
 
         public int getNumCoords() {
             return numCoords;
         }
 
         public double getCoord(int i) {
             return bezierCoords[i];
         }
 
         public double[] getTvals() {
             if (myTvals != null) {
                 return myTvals;
             }
 
             // assert(numCoords >= 8);
             // assert(((numCoords - 2) % 6) == 0);
             double tvals[] = new double[(numCoords - 2) / 6 + 1];
 
             // First calculate total "length" of path
             // Length of each segment is averaged between
             // the length between the endpoints (a lower bound for a cubic)
             // and the length of the control polygon (an upper bound)
             double segx = bezierCoords[0];
             double segy = bezierCoords[1];
             double tlen = 0;
             int ci = 2;
             int ti = 0;
             while (ci < numCoords) {
                 double prevx, prevy, newx, newy;
                 prevx = segx;
                 prevy = segy;
                 newx = bezierCoords[ci++];
                 newy = bezierCoords[ci++];
                 prevx -= newx;
                 prevy -= newy;
                 double len = Math.sqrt(prevx * prevx + prevy * prevy);
                 prevx = newx;
                 prevy = newy;
                 newx = bezierCoords[ci++];
                 newy = bezierCoords[ci++];
                 prevx -= newx;
                 prevy -= newy;
                 len += Math.sqrt(prevx * prevx + prevy * prevy);
                 prevx = newx;
                 prevy = newy;
                 newx = bezierCoords[ci++];
                 newy = bezierCoords[ci++];
                 prevx -= newx;
                 prevy -= newy;
                 len += Math.sqrt(prevx * prevx + prevy * prevy);
                 // len is now the total length of the control polygon
                 segx -= newx;
                 segy -= newy;
                 len += Math.sqrt(segx * segx + segy * segy);
                 // len is now sum of linear length and control polygon length
                 len /= 2;
                 // len is now average of the two lengths
 
                 /* If the result is zero length then we will have problems
                  * below trying to do the math and bookkeeping to split
                  * the segment or pair it against the segments in the
                  * other shape.  Since these lengths are just estimates
                  * to map the segments of the two shapes onto corresponding
                  * segments of "approximately the same length", we will
                  * simply fudge the length of this segment to be at least
                  * a minimum value and it will simply grow from zero or
                  * near zero length to a non-trivial size as it morphs.
                  */
                 if (len < MIN_LEN) {
                     len = MIN_LEN;
                 }
                 tlen += len;
                 tvals[ti++] = tlen;
                 segx = newx;
                 segy = newy;
             }
 
             // Now set tvals for each segment to its proportional
             // part of the length
             double prevt = tvals[0];
             tvals[0] = 0;
             for (ti = 1; ti < tvals.length - 1; ti++) {
                 double nextt = tvals[ti];
                 tvals[ti] = prevt / tlen;
                 prevt = nextt;
             }
             tvals[ti] = 1;
             return (myTvals = tvals);
         }
 
         public void setTvals(double newTvals[]) {
             double oldCoords[] = bezierCoords;
             double newCoords[] = new double[2 + (newTvals.length - 1) * 6];
             double oldTvals[] = getTvals();
             int oldci = 0;
             double x0, xc0, xc1, x1;
             double y0, yc0, yc1, y1;
             x0 = xc0 = xc1 = x1 = oldCoords[oldci++];
             y0 = yc0 = yc1 = y1 = oldCoords[oldci++];
             int newci = 0;
             newCoords[newci++] = x0;
             newCoords[newci++] = y0;
             double t0 = 0;
             double t1 = 0;
             int oldti = 1;
             int newti = 1;
             while (newti < newTvals.length) {
                 if (t0 >= t1) {
                     x0 = x1;
                     y0 = y1;
                     xc0 = oldCoords[oldci++];
                     yc0 = oldCoords[oldci++];
                     xc1 = oldCoords[oldci++];
                     yc1 = oldCoords[oldci++];
                     x1 = oldCoords[oldci++];
                     y1 = oldCoords[oldci++];
                     t1 = oldTvals[oldti++];
                 }
                 double nt = newTvals[newti++];
                 // assert(nt > t0);
                 if (nt < t1) {
                     // Make nt proportional to [t0 => t1] range
                     double relt = (nt - t0) / (t1 - t0);
                     newCoords[newci++] = x0 = interp(x0, xc0, relt);
                     newCoords[newci++] = y0 = interp(y0, yc0, relt);
                     xc0 = interp(xc0, xc1, relt);
                     yc0 = interp(yc0, yc1, relt);
                     xc1 = interp(xc1, x1, relt);
                     yc1 = interp(yc1, y1, relt);
                     newCoords[newci++] = x0 = interp(x0, xc0, relt);
                     newCoords[newci++] = y0 = interp(y0, yc0, relt);
                     xc0 = interp(xc0, xc1, relt);
                     yc0 = interp(yc0, yc1, relt);
                     newCoords[newci++] = x0 = interp(x0, xc0, relt);
                     newCoords[newci++] = y0 = interp(y0, yc0, relt);
                 } else {
                     newCoords[newci++] = xc0;
                     newCoords[newci++] = yc0;
                     newCoords[newci++] = xc1;
                     newCoords[newci++] = yc1;
                     newCoords[newci++] = x1;
                     newCoords[newci++] = y1;
                 }
                 t0 = nt;
             }
             bezierCoords = newCoords;
             numCoords = newCoords.length;
             myTvals = newTvals;
         }
     }
 
     private static class Iterator implements PathIterator {
         AffineTransform at;
         Geometry g0;
         Geometry g1;
         double t;
         int cindex;
 
         public Iterator(AffineTransform at,
                         Geometry g0, Geometry g1,
                         double t) {
             this.at = at;
             this.g0 = g0;
             this.g1 = g1;
             this.t = t;
         }
 
         /**
          * @{inheritDoc}
          */
         public int getWindingRule() {
             return g0.getWindingRule();
         }
 
         /**
          * @{inheritDoc}
          */
         public boolean isDone() {
             return (cindex > g0.getNumCoords());
         }
 
         /**
          * @{inheritDoc}
          */
         public void next() {
             if (cindex == 0) {
                 cindex = 2;
             } else {
                 cindex += 6;
             }
         }
 
         double dcoords[];
 
         /**
          * @{inheritDoc}
          */
         public int currentSegment(float[] coords) {
             if (dcoords == null) {
                 dcoords = new double[6];
             }
             int type = currentSegment(dcoords);
             if (type != SEG_CLOSE) {
                 coords[0] = (float) dcoords[0];
                 coords[1] = (float) dcoords[1];
                 if (type != SEG_MOVETO) {
                     coords[2] = (float) dcoords[2];
                     coords[3] = (float) dcoords[3];
                     coords[4] = (float) dcoords[4];
                     coords[5] = (float) dcoords[5];
                 }
             }
             return type;
         }
 
         /**
          * @{inheritDoc}
          */
         public int currentSegment(double[] coords) {
             int type;
             int n;
             if (cindex == 0) {
                 type = SEG_MOVETO;
                 n = 2;
             } else if (cindex >= g0.getNumCoords()) {
                 type = SEG_CLOSE;
                 n = 0;
             } else {
                 type = SEG_CUBICTO;
                 n = 6;
             }
             if (n > 0) {
                 for (int i = 0; i < n; i++) {
                     coords[i] = interp(g0.getCoord(cindex + i),
                                        g1.getCoord(cindex + i),
                                        t);
                 }
                 if (at != null) {
                     at.transform(coords, 0, coords, 0, n / 2);
                 }
             }
             return type;
         }
     }
 }
 
 /**
  * <p><code>GraphicsUtilities</code> contains a set of tools to perform
  * common graphics operations easily. These operations are divided into
  * several themes, listed below.</p>
  * <h2>Compatible Images</h2>
  * <p>Compatible images can, and should, be used to increase drawing
  * performance. This class provides a number of methods to load compatible
  * images directly from files or to convert existing images to compatibles
  * images.</p>
  * <h2>Creating Thumbnails</h2>
  * <p>This class provides a number of methods to easily scale down images.
  * Some of these methods offer a trade-off between speed and result quality and
  * shouuld be used all the time. They also offer the advantage of producing
  * compatible images, thus automatically resulting into better runtime
  * performance.</p>
  * <p>All these methodes are both faster than
  * {@link java.awt.Image#getScaledInstance(int, int, int)} and produce
  * better-looking results than the various <code>drawImage()</code> methods
  * in {@link java.awt.Graphics}, which can be used for image scaling.</p>
  * <h2>Image Manipulation</h2>
  * <p>This class provides two methods to get and set pixels in a buffered image.
  * These methods try to avoid unmanaging the image in order to keep good
  * performance.</p>
  *
  * @author Romain Guy <romain.guy@mac.com>
  */
 class GraphicsUtilities {
     private GraphicsUtilities() {
     }
 
     // Returns the graphics configuration for the primary screen
     private static GraphicsConfiguration getGraphicsConfiguration() {
         return GraphicsEnvironment.getLocalGraphicsEnvironment().
                     getDefaultScreenDevice().getDefaultConfiguration();
     }
 
     /**
      * <p>Returns a new <code>BufferedImage</code> using the same color model
      * as the image passed as a parameter. The returned image is only compatible
      * with the image passed as a parameter. This does not mean the returned
      * image is compatible with the hardware.</p>
      *
      * @param image the reference image from which the color model of the new
      *   image is obtained
      * @return a new <code>BufferedImage</code>, compatible with the color model
      *   of <code>image</code>
      */
     public static BufferedImage createColorModelCompatibleImage(BufferedImage image) {
         ColorModel cm = image.getColorModel();
         return new BufferedImage(cm,
             cm.createCompatibleWritableRaster(image.getWidth(),
                                               image.getHeight()),
             cm.isAlphaPremultiplied(), null);
     }
 
     /**
      * <p>Returns a new compatible image with the same width, height and
      * transparency as the image specified as a parameter.</p>
      *
      * @see java.awt.Transparency
      * @see #createCompatibleImage(int, int)
      * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
      * @see #createCompatibleTranslucentImage(int, int)
      * @see #loadCompatibleImage(java.net.URL)
      * @see #toCompatibleImage(java.awt.image.BufferedImage)
      * @param image the reference image from which the dimension and the
      *   transparency of the new image are obtained
      * @return a new compatible <code>BufferedImage</code> with the same
      *   dimension and transparency as <code>image</code>
      */
     public static BufferedImage createCompatibleImage(BufferedImage image) {
         return createCompatibleImage(image, image.getWidth(), image.getHeight());
     }
 
     /**
      * <p>Returns a new compatible image of the specified width and height, and
      * the same transparency setting as the image specified as a parameter.</p>
      *
      * @see java.awt.Transparency
      * @see #createCompatibleImage(java.awt.image.BufferedImage)
      * @see #createCompatibleImage(int, int)
      * @see #createCompatibleTranslucentImage(int, int)
      * @see #loadCompatibleImage(java.net.URL)
      * @see #toCompatibleImage(java.awt.image.BufferedImage)
      * @param width the width of the new image
      * @param height the height of the new image
      * @param image the reference image from which the transparency of the new
      *   image is obtained
      * @return a new compatible <code>BufferedImage</code> with the same
      *   transparency as <code>image</code> and the specified dimension
      */
     public static BufferedImage createCompatibleImage(BufferedImage image,
                                                       int width, int height) {
         return getGraphicsConfiguration().createCompatibleImage(width, height,
                                                    image.getTransparency());
     }
 
     /**
      * <p>Returns a new opaque compatible image of the specified width and
      * height.</p>
      *
      * @see #createCompatibleImage(java.awt.image.BufferedImage)
      * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
      * @see #createCompatibleTranslucentImage(int, int)
      * @see #loadCompatibleImage(java.net.URL)
      * @see #toCompatibleImage(java.awt.image.BufferedImage)
      * @param width the width of the new image
      * @param height the height of the new image
      * @return a new opaque compatible <code>BufferedImage</code> of the
      *   specified width and height
      */
     public static BufferedImage createCompatibleImage(int width, int height) {
         return getGraphicsConfiguration().createCompatibleImage(width, height);
     }
 
     /**
      * <p>Returns a new translucent compatible image of the specified width
      * and height.</p>
      *
      * @see #createCompatibleImage(java.awt.image.BufferedImage)
      * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
      * @see #createCompatibleImage(int, int)
      * @see #loadCompatibleImage(java.net.URL)
      * @see #toCompatibleImage(java.awt.image.BufferedImage)
      * @param width the width of the new image
      * @param height the height of the new image
      * @return a new translucent compatible <code>BufferedImage</code> of the
      *   specified width and height
      */
     public static BufferedImage createCompatibleTranslucentImage(int width,
                                                                  int height) {
         return getGraphicsConfiguration().createCompatibleImage(width, height,
                                                    Transparency.TRANSLUCENT);
     }
 
     /**
      * <p>Returns a new compatible image from a URL. The image is loaded from the
      * specified location and then turned, if necessary into a compatible
      * image.</p>
      *
      * @see #createCompatibleImage(java.awt.image.BufferedImage)
      * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
      * @see #createCompatibleImage(int, int)
      * @see #createCompatibleTranslucentImage(int, int)
      * @see #toCompatibleImage(java.awt.image.BufferedImage)
      * @param resource the URL of the picture to load as a compatible image
      * @return a new translucent compatible <code>BufferedImage</code> of the
      *   specified width and height
      * @throws java.io.IOException if the image cannot be read or loaded
      */
     public static BufferedImage loadCompatibleImage(URL resource)
             throws IOException {
         BufferedImage image = ImageIO.read(resource);
         return toCompatibleImage(image);
     }
 
     /**
      * <p>Return a new compatible image that contains a copy of the specified
      * image. This method ensures an image is compatible with the hardware,
      * and therefore optimized for fast blitting operations.</p>
      *
      * @see #createCompatibleImage(java.awt.image.BufferedImage)
      * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
      * @see #createCompatibleImage(int, int)
      * @see #createCompatibleTranslucentImage(int, int)
      * @see #loadCompatibleImage(java.net.URL)
      * @param image the image to copy into a new compatible image
      * @return a new compatible copy, with the
      *   same width and height and transparency and content, of <code>image</code>
      */
     public static BufferedImage toCompatibleImage(BufferedImage image) {
         if (image.getColorModel().equals(
                 getGraphicsConfiguration().getColorModel())) {
             return image;
         }
 
         BufferedImage compatibleImage =
                 getGraphicsConfiguration().createCompatibleImage(
                     image.getWidth(), image.getHeight(),
                     image.getTransparency());
         Graphics g = compatibleImage.getGraphics();
         g.drawImage(image, 0, 0, null);
         g.dispose();
 
         return compatibleImage;
     }
 
     /**
      * <p>Returns a thumbnail of a source image. <code>newSize</code> defines
      * the length of the longest dimension of the thumbnail. The other
      * dimension is then computed according to the dimensions ratio of the
      * original picture.</p>
      * <p>This method favors speed over quality. When the new size is less than
      * half the longest dimension of the source image,
      * {@link #createThumbnail(BufferedImage, int)} or
      * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
      * to ensure the quality of the result without sacrificing too much
      * performance.</p>
      *
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
      * @param image the source image
      * @param newSize the length of the largest dimension of the thumbnail
      * @return a new compatible <code>BufferedImage</code> containing a
      *   thumbnail of <code>image</code>
      * @throws IllegalArgumentException if <code>newSize</code> is larger than
      *   the largest dimension of <code>image</code> or &lt;= 0
      */
     public static BufferedImage createThumbnailFast(BufferedImage image,
                                                     int newSize) {
         float ratio;
         int width = image.getWidth();
         int height = image.getHeight();
 
         if (width > height) {
             if (newSize >= width) {
                 throw new IllegalArgumentException("newSize must be lower than" +
                                                    " the image width");
             } else if (newSize <= 0) {
                  throw new IllegalArgumentException("newSize must" +
                                                     " be greater than 0");
             }
 
             ratio = (float) width / (float) height;
             width = newSize;
             height = (int) (newSize / ratio);
         } else {
             if (newSize >= height) {
                 throw new IllegalArgumentException("newSize must be lower than" +
                                                    " the image height");
             } else if (newSize <= 0) {
                  throw new IllegalArgumentException("newSize must" +
                                                     " be greater than 0");
             }
 
             ratio = (float) height / (float) width;
             height = newSize;
             width = (int) (newSize / ratio);
         }
 
         BufferedImage temp = createCompatibleImage(image, width, height);
         Graphics2D g2 = temp.createGraphics();
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
         g2.dispose();
 
         return temp;
     }
 
     /**
      * <p>Returns a thumbnail of a source image.</p>
      * <p>This method favors speed over quality. When the new size is less than
      * half the longest dimension of the source image,
      * {@link #createThumbnail(BufferedImage, int)} or
      * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
      * to ensure the quality of the result without sacrificing too much
      * performance.</p>
      *
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
      * @param image the source image
      * @param newWidth the width of the thumbnail
      * @param newHeight the height of the thumbnail
      * @return a new compatible <code>BufferedImage</code> containing a
      *   thumbnail of <code>image</code>
      * @throws IllegalArgumentException if <code>newWidth</code> is larger than
      *   the width of <code>image</code> or if code>newHeight</code> is larger
      *   than the height of <code>image</code> or if one of the dimensions
      *   is &lt;= 0
      */
     public static BufferedImage createThumbnailFast(BufferedImage image,
                                                     int newWidth, int newHeight) {
         if (newWidth >= image.getWidth() ||
             newHeight >= image.getHeight()) {
             throw new IllegalArgumentException("newWidth and newHeight cannot" +
                                                " be greater than the image" +
                                                " dimensions");
         } else if (newWidth <= 0 || newHeight <= 0) {
             throw new IllegalArgumentException("newWidth and newHeight must" +
                                                " be greater than 0");
         }
 
         BufferedImage temp = createCompatibleImage(image, newWidth, newHeight);
         Graphics2D g2 = temp.createGraphics();
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
         g2.dispose();
 
         return temp;
     }
 
     /**
      * <p>Returns a thumbnail of a source image. <code>newSize</code> defines
      * the length of the longest dimension of the thumbnail. The other
      * dimension is then computed according to the dimensions ratio of the
      * original picture.</p>
      * <p>This method offers a good trade-off between speed and quality.
      * The result looks better than
      * {@link #createThumbnailFast(java.awt.image.BufferedImage, int)} when
      * the new size is less than half the longest dimension of the source
      * image, yet the rendering speed is almost similar.</p>
      *
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
      * @param image the source image
      * @param newSize the length of the largest dimension of the thumbnail
      * @return a new compatible <code>BufferedImage</code> containing a
      *   thumbnail of <code>image</code>
      * @throws IllegalArgumentException if <code>newSize</code> is larger than
      *   the largest dimension of <code>image</code> or &lt;= 0
      */
     public static BufferedImage createThumbnail(BufferedImage image,
                                                 int newSize) {
         int width = image.getWidth();
         int height = image.getHeight();
 
         boolean isWidthGreater = width > height;
 
         if (isWidthGreater) {
             if (newSize >= width) {
                 throw new IllegalArgumentException("newSize must be lower than" +
                                                    " the image width");
             }
         } else if (newSize >= height) {
             throw new IllegalArgumentException("newSize must be lower than" +
                                                " the image height");
         }
 
         if (newSize <= 0) {
             throw new IllegalArgumentException("newSize must" +
                                                " be greater than 0");
         }
 
         float ratioWH = (float) width / (float) height;
         float ratioHW = (float) height / (float) width;
 
         BufferedImage thumb = image;
 
         do {
             if (isWidthGreater) {
                 width /= 2;
                 if (width < newSize) {
                     width = newSize;
                 }
                 height = (int) (width / ratioWH);
             } else {
                 height /= 2;
                 if (height < newSize) {
                     height = newSize;
                 }
                 width = (int) (height / ratioHW);
             }
 
 
             BufferedImage temp = createCompatibleImage(image, width, height);
             Graphics2D g2 = temp.createGraphics();
             g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
             g2.drawImage(thumb, 0, 0, temp.getWidth(), temp.getHeight(), null);
             g2.dispose();
 
             thumb = temp;
         } while (newSize != (isWidthGreater ? width : height));
 
         return thumb;
     }
 
     /**
      * <p>Returns a thumbnail of a source image.</p>
      * <p>This method offers a good trade-off between speed and quality.
      * The result looks better than
      * {@link #createThumbnailFast(java.awt.image.BufferedImage, int)} when
      * the new size is less than half the longest dimension of the source
      * image, yet the rendering speed is almost similar.</p>
      *
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
      * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
      * @see #createThumbnail(java.awt.image.BufferedImage, int)
      * @param image the source image
      * @param newWidth the width of the thumbnail
      * @param newHeight the height of the thumbnail
      * @return a new compatible <code>BufferedImage</code> containing a
      *   thumbnail of <code>image</code>
      * @throws IllegalArgumentException if <code>newWidth</code> is larger than
      *   the width of <code>image</code> or if code>newHeight</code> is larger
      *   than the height of <code>image or if one the dimensions is not &gt; 0</code>
      */
     public static BufferedImage createThumbnail(BufferedImage image,
                                                 int newWidth, int newHeight) {
         int width = image.getWidth();
         int height = image.getHeight();
 
         if (newWidth >= width || newHeight >= height) {
             throw new IllegalArgumentException("newWidth and newHeight cannot" +
                                                " be greater than the image" +
                                                " dimensions");
         } else if (newWidth <= 0 || newHeight <= 0) {
             throw new IllegalArgumentException("newWidth and newHeight must" +
                                                " be greater than 0");
         }
 
         BufferedImage thumb = image;
 
         do {
             if (width > newWidth) {
                 width /= 2;
                 if (width < newWidth) {
                     width = newWidth;
                 }
             }
 
             if (height > newHeight) {
                 height /= 2;
                 if (height < newHeight) {
                     height = newHeight;
                 }
             }
 
             BufferedImage temp = createCompatibleImage(image, width, height);
             Graphics2D g2 = temp.createGraphics();
             g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
             g2.drawImage(thumb, 0, 0, temp.getWidth(), temp.getHeight(), null);
             g2.dispose();
 
             thumb = temp;
         } while (width != newWidth || height != newHeight);
 
         return thumb;
     }
 
     /**
      * <p>Returns an array of pixels, stored as integers, from a
      * <code>BufferedImage</code>. The pixels are grabbed from a rectangular
      * area defined by a location and two dimensions. Calling this method on
      * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
      * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
      *
      * @param img the source image
      * @param x the x location at which to start grabbing pixels
      * @param y the y location at which to start grabbing pixels
      * @param w the width of the rectangle of pixels to grab
      * @param h the height of the rectangle of pixels to grab
      * @param pixels a pre-allocated array of pixels of size w*h; can be null
      * @return <code>pixels</code> if non-null, a new array of integers
      *   otherwise
      * @throws IllegalArgumentException is <code>pixels</code> is non-null and
      *   of length &lt; w*h
      */
     public static int[] getPixels(BufferedImage img,
                                   int x, int y, int w, int h, int[] pixels) {
         if (w == 0 || h == 0) {
             return new int[0];
         }
 
         if (pixels == null) {
             pixels = new int[w * h];
         } else if (pixels.length < w * h) {
             throw new IllegalArgumentException("pixels array must have a length" +
                                                " >= w*h");
         }
 
         int imageType = img.getType();
         if (imageType == BufferedImage.TYPE_INT_ARGB ||
             imageType == BufferedImage.TYPE_INT_RGB) {
             Raster raster = img.getRaster();
             return (int[]) raster.getDataElements(x, y, w, h, pixels);
         }
 
         // Unmanages the image
         return img.getRGB(x, y, w, h, pixels, 0, w);
     }
 
     /**
      * <p>Writes a rectangular area of pixels in the destination
      * <code>BufferedImage</code>. Calling this method on
      * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
      * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
      *
      * @param img the destination image
      * @param x the x location at which to start storing pixels
      * @param y the y location at which to start storing pixels
      * @param w the width of the rectangle of pixels to store
      * @param h the height of the rectangle of pixels to store
      * @param pixels an array of pixels, stored as integers
      * @throws IllegalArgumentException is <code>pixels</code> is non-null and
      *   of length &lt; w*h
      */
     public static void setPixels(BufferedImage img,
                                  int x, int y, int w, int h, int[] pixels) {
         if (pixels == null || w == 0 || h == 0) {
             return;
         } else if (pixels.length < w * h) {
             throw new IllegalArgumentException("pixels array must have a length" +
                                                " >= w*h");
         }
 
         int imageType = img.getType();
         if (imageType == BufferedImage.TYPE_INT_ARGB ||
             imageType == BufferedImage.TYPE_INT_RGB) {
             WritableRaster raster = img.getRaster();
             raster.setDataElements(x, y, w, h, pixels);
         } else {
             // Unmanages the image
             img.setRGB(x, y, w, h, pixels, 0, w);
         }
     }
 }
