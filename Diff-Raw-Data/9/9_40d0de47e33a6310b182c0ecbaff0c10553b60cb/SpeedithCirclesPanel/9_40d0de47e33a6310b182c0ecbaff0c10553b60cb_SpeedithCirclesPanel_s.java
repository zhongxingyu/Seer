 /*
  *   Project: Speedith
  * 
  * File name: SpeedithCirclesPanel.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2012 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package speedith.ui;
 
 import icircles.concreteDiagram.CircleContour;
 import icircles.concreteDiagram.ConcreteDiagram;
 import icircles.concreteDiagram.ConcreteSpiderFoot;
 import icircles.concreteDiagram.ConcreteZone;
 import icircles.gui.CirclesPanelEx;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import javax.swing.event.MouseInputListener;
 import speedith.core.reasoning.args.selection.SelectionStep;
 
 /**
  * Speedith's special version of the {@link CirclesPanelEx circles panel}. It 
  * supports automatic highlighting of diagram elements.
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public class SpeedithCirclesPanel extends CirclesPanelEx {
     
     //<editor-fold defaultstate="collapsed" desc="Fields">
     /**
      * This set of flag determines which elements of the diagram may be
      * highlighted with the mouse. <p>Possible values are:
      * <ul>
      * <li>{@link SelectionStep#AllPrimaryElements},</li>
      * <li>{@link SelectionStep#Spiders},</li>
      * <li>{@link SelectionStep#Contours}, and</li>
      * <li>{@link SelectionStep#Zones}.</li>
      * </ul></p>
      */
     private int highlightMode = SelectionStep.None;
     private static final double HIGHLIGHT_CONTOUR_TOLERANCE = 6;
     //</editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Constructors">
     public SpeedithCirclesPanel() {
         this(null);
     }
 
     public SpeedithCirclesPanel(ConcreteDiagram diagram) {
         super(diagram);
         // Register mouse listeners
         CirclesPanelMouseHandler mouseHandler = new CirclesPanelMouseHandler();
         addMouseListener(mouseHandler);
         addMouseMotionListener(mouseHandler);
     }
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Automatic Highlighting">
     /**
      * Returns the set of flags that determines which elements of the diagram
      * may be highlighted with the mouse. <p>This flag can be a (binary)
      * combination of the following flags: <ul> <li>{@link SelectionStep#Spiders}:
      * which indicates that spiders will be highlighted when the user hovers
      * over them.</li> <li>{@link SelectionStep#Zones}: which indicates that
      * zones will be highlighted when the user hovers over them.</li> <li>{@link SelectionStep#Contours}:
      * which indicates that circle contours will be highlighted when the user
      * hovers over them.</li> </ul></p> <p> The {@link SelectionStep#AllPrimaryElements} and {@link SelectionStep#None} flags
      * can also be used. These indicate that all diagram or no elements (respectively) can be
      * highlighted with the mouse.</p>
      *
      * @return the set of flags that determines which elements of the diagram
      * may be highlighted with the mouse.
      */
     public int getHighlightMode() {
         return highlightMode;
     }
 
     /**
      * Sets the set of flags that determines which elements of the diagram may
      * be highlighted with the mouse. <p>Possible values are:
      * <ul>
      * <li>{@link SelectionStep#AllPrimaryElements},</li>
      * <li>{@link SelectionStep#Spiders},</li>
      * <li>{@link SelectionStep#Contours}, and</li>
      * <li>{@link SelectionStep#Zones}.</li>
      * </ul></p>
      *
      * @param highlightMode the new set of flags that determines which elements
      * of the diagram may be highlighted with the mouse.
      */
     public void setHighlightMode(int highlightMode) {
         this.highlightMode = highlightMode & SelectionStep.AllPrimaryElements;
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Events">
     /**
      * Registers the given {@link DiagramClickListener diagram click listener}
      * to the events which are fired when the user clicks on particular diagram
      * elements. <p><span style="font-weight:bold">Note</span>: the events are
      * invoked regardless of whether {@link SpeedithCirclesPanel#getHighlightMode()}
      * flags are set.</p>
      *
      * @param l the event listener to register.
      */
     public void addDiagramClickListener(DiagramClickListener l) {
         this.listenerList.add(DiagramClickListener.class, l);
     }
 
     /**
      * Removes the given {@link DiagramClickListener diagram click listener}
      * from the events which are fired when the user clicks on particular
      * diagram elements. <p>The given listener will no longer receive these
      * events.</p>
      *
      * @param l the event listener to deregister.
      */
     public void removeDiagramClickListener(DiagramClickListener l) {
         this.listenerList.remove(DiagramClickListener.class, l);
     }
 
     /**
      * Returns the array of all {@link SpeedithCirclesPanel#addDiagramClickListener(icircles.gui.DiagramClickListener) registered}
      * {@link DiagramClickListener diagram click listeners}.
      *
      * @return the array of all {@link SpeedithCirclesPanel#addDiagramClickListener(icircles.gui.DiagramClickListener) registered}
      * {@link DiagramClickListener diagram click listeners}.
      */
     public DiagramClickListener[] getDiagramClickListeners() {
         return listenerList.getListeners(DiagramClickListener.class);
     }
 
     protected void fireSpiderClickedEvent(ConcreteSpiderFoot foot, MouseEvent clickInfo) {
         DiagramClickListener[] ls = listenerList.getListeners(DiagramClickListener.class);
         if (ls != null && ls.length > 0) {
             SpiderClickedEvent e = new SpiderClickedEvent(this, getDiagram(), clickInfo, this.toDiagramCoordinates(clickInfo.getPoint()), foot);
             for (int i = 0; i < ls.length; i++) {
                 ls[i].spiderClicked(e);
             }
         }
     }
 
     protected void fireZoneClickedEvent(ConcreteZone zone, MouseEvent clickInfo) {
         DiagramClickListener[] ls = listenerList.getListeners(DiagramClickListener.class);
         if (ls != null && ls.length > 0) {
             ZoneClickedEvent e = new ZoneClickedEvent(this, getDiagram(), clickInfo, this.toDiagramCoordinates(clickInfo.getPoint()), zone);
             for (int i = 0; i < ls.length; i++) {
                 ls[i].zoneClicked(e);
             }
         }
     }
 
     protected void fireContourClickedEvent(CircleContour contour, MouseEvent clickInfo) {
         DiagramClickListener[] ls = listenerList.getListeners(DiagramClickListener.class);
         if (ls != null && ls.length > 0) {
             ContourClickedEvent e = new ContourClickedEvent(this, getDiagram(), clickInfo, this.toDiagramCoordinates(clickInfo.getPoint()), contour);
             for (int i = 0; i < ls.length; i++) {
                 ls[i].contourClicked(e);
             }
         }
     }
     // </editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Helper Classes">
     private class CirclesPanelMouseHandler implements MouseInputListener {
 
         public CirclesPanelMouseHandler() {
         }
 
         public void mouseClicked(MouseEvent e) {
             if (getDiagram() != null) {
                 Point p = toDiagramCoordinates(e.getPoint());
 
 //                if ((getHighlightMode() & Contours) == Contours) {
                 CircleContour contour = getDiagram().getCircleContourAtPoint(p, HIGHLIGHT_CONTOUR_TOLERANCE / getScaleFactor());
                 if (contour != null) {
                     fireContourClickedEvent(contour, e);
                     return;
                 }
 //                }
 
 //                if ((getHighlightMode() & Spiders) == Spiders) {
                ConcreteSpiderFoot foot = getDiagram().getSpiderFootAtPoint(p);
                 if (foot != null) {
                     fireSpiderClickedEvent(foot, e);
                     return;
                 }
 //                }
 
 //                if ((getHighlightMode() & Zones) == Zones) {
                 ConcreteZone zone = getDiagram().getZoneAtPoint(p);
                 if (zone != null) {
                     fireZoneClickedEvent(zone, e);
                 }
 //                }
             }
         }
 
         public void mousePressed(MouseEvent e) {
         }
 
         public void mouseReleased(MouseEvent e) {
         }
 
         public void mouseEntered(MouseEvent e) {
         }
 
         public void mouseExited(MouseEvent e) {
             setHighlightedZone(null);
             setHighlightedContour(null);
             setHighlightedFoot(null);
         }
 
         public void mouseDragged(MouseEvent e) {
         }
 
         public void mouseMoved(MouseEvent e) {
             if (getHighlightMode() != 0 && getDiagram() != null) {
                 Point p = toDiagramCoordinates(e.getPoint());
 
                 // Check if the mouse hovers over a contour:
                 if ((getHighlightMode() & SelectionStep.Contours) == SelectionStep.Contours) {
                     CircleContour contour = getDiagram().getCircleContourAtPoint(p, HIGHLIGHT_CONTOUR_TOLERANCE / getScaleFactor());
                     if (contour != null) {
                         setHighlightedContour(contour);
                         return;
                     }
                 }
 
                 // Check if the mouse hovers over a spider:
                 if ((getHighlightMode() & SelectionStep.Spiders) == SelectionStep.Spiders) {
                     ConcreteSpiderFoot foot = getDiagram().getSpiderFootAtPoint(p, getScaleFactor());
                     if (foot != null) {
                         setHighlightedFoot(foot);
                         return;
                     }
                 }
 
                 // Check if the mouse hovers over a zone:
                 if ((getHighlightMode() & SelectionStep.Zones) == SelectionStep.Zones) {
                     ConcreteZone zone = getDiagram().getZoneAtPoint(p);
                     if (zone != null) {
                         setHighlightedZone(zone);
                         return;
                     }
                 }
 
                 setHighlightedZone(null);
                 setHighlightedContour(null);
                 setHighlightedFoot(null);
             }
         }
     }
     //</editor-fold>
     
 }
