 /*
  *   Project: Speedith
  * 
  * File name: SpiderDiagramPanel.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2011 Matej Urbas
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
 
 /*
  * SpiderDiagramPanel.java
  *
  * Created on 29-Sep-2011, 13:46:10
  */
 package speedith.ui;
 
 import icircles.gui.*;
 import icircles.util.CannotDrawException;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Iterator;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import speedith.core.lang.CompoundSpiderDiagram;
 import speedith.core.lang.NullSpiderDiagram;
 import speedith.core.lang.PrimarySpiderDiagram;
 import speedith.core.lang.SpiderDiagram;
 import speedith.core.lang.reader.ReadingException;
 import speedith.core.lang.reader.SpiderDiagramsReader;
 import speedith.core.reasoning.args.selection.SelectionStep;
 import static speedith.i18n.Translations.i18n;
 
 /**
  * This panel displays all types of {@link SpiderDiagram spider diagrams}.
  *
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public class SpiderDiagramPanel extends javax.swing.JPanel {
     
     // <editor-fold defaultstate="collapsed" desc="Fields">
     /**
      * The default size of the {@link SpeedithCirclesPanel circle panels}.
      */
    private static final Dimension PrimaryDiagramSize = new Dimension(100, 100);
     // </editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Constructors">
     /**
      * Creates a new compound spider diagram panel with nothing displayed on it.
      * <p>You can set a diagram to be shown through {@link SpiderDiagramPanel#setDiagram(speedith.core.lang.SpiderDiagram)}.</p>
      */
     public SpiderDiagramPanel() {
         this(null);
     }
 
     /**
      * Creates a new compound spider diagram panel with the given compound
      * spider diagram.
      *
      * @param diagram the diagram to display in this panel. <p>May be {@code null}
      * in which case nothing will be displayed.</p>
      */
     public SpiderDiagramPanel(SpiderDiagram diagram) {
         initComponents();
         setDiagram(diagram);
     }
     //</editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         diagrams = new javax.swing.JPanel();
 
         setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
 
         diagrams.setBackground(new java.awt.Color(255, 255, 255));
         diagrams.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 onMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout diagramsLayout = new javax.swing.GroupLayout(diagrams);
         diagrams.setLayout(diagramsLayout);
         diagramsLayout.setHorizontalGroup(
             diagramsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 330, Short.MAX_VALUE)
         );
         diagramsLayout.setVerticalGroup(
             diagramsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 234, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(diagrams, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(diagrams, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void onMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onMouseClicked
         fireSpiderDiagramClicked(0, null);
     }//GEN-LAST:event_onMouseClicked
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel diagrams;
     // End of variables declaration//GEN-END:variables
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Private Fields">
     private SpiderDiagram diagram;
     /**
      * Indicates which elements in the currently displayed diagram should be
      * highlightable by the user.
      */
     private int highlightMode = SelectionStep.None;
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Public Properties">
     /**
      * Returns the currently presented diagram. <p>May be {@code null}, which
      * indicates that the panel is empty.</p>
      *
      * @return the currently presented diagram, or {@code null} is no diagram is
      * currently being shown.
      */
     public SpiderDiagram getDiagram() {
         return diagram;
     }
 
     /**
      * Sets the currently shown diagram. <p>May be {@code null}, which indicates
      * that the panel should be empty.</p>
      *
      * @param diagram the new diagram to show, or {@code null} if no diagram is
      * to be shown.
      */
     public final void setDiagram(SpiderDiagram diagram) {
         if (this.diagram != diagram) {
             this.diagram = diagram;
             diagrams.removeAll();
             if (this.diagram != null) {
                 try {
                     drawDiagram();
                 } catch (Exception ex) {
                     drawErrorLabel();
                 }
             } else {
                 drawNoDiagramLabel();
             }
             // If the highlight mode has been set, do apply it to the
             // underlying panels.
             applyHighlightModeToPanels();
             // We have to repaint the content (Java does not repaint it--because
             // removing and adding panels doesn't seem to trigger a repaint).
             validate();
             repaint();
         }
     }
 
     /**
      * Returns the string representation of the currently shown diagram.
      *
      * @return the string representation of the currently shown diagram.
      */
     public String getDiagramString() {
         return this.diagram == null ? "" : this.diagram.toString();
     }
 
     /**
      * Sets the currently shown diagram via its string representation. <p>This
      * method tries to parse the string into a spider diagram and draws it.</p>
      * <p>Here is an example of a valid string:
      * <pre>BinarySD {
      *      operator = "op -->",
      *      arg1 = PrimarySD {spiders = ["s1", "s2", "s3"], habitats = [("s1", [(["A"], ["B", "C"]), (["A", "B"], ["C"]), (["A", "B", "C"], [])]), ("s2", [(["B"], ["A", "C"])]), ("s3", [(["B"], ["A", "C"])])], sh_zones = [(["B"], ["A", "C"])]},
      *      arg2 = PrimarySD {spiders = ["s1", "s2", "s3"], habitats = [("s1", [(["A"], ["B", "C"]), (["A", "B"], ["C"]), (["A", "B", "C"], [])]), ("s2", [(["B"], ["A", "C"])]), ("s3", [(["B"], ["A", "C"])])], sh_zones = [(["B"], ["A", "C"])]}
      * }</pre> </p>
      *
      * @param diagram the string representation of the diagram to present.
      * @throws ReadingException thrown if the string does not represent a valid
      * spider diagram.
      * @throws IllegalArgumentException thrown if the string is a valid spider
      * diagram but not a primary spider diagram.
      */
     public void setDiagramString(String diagram) throws ReadingException {
         if (diagram == null || diagram.isEmpty()) {
             setDiagram(null);
         } else {
             setDiagram(SpiderDiagramsReader.readSpiderDiagram(diagram));
         }
     }
 
     /**
      * Returns the set of flags that determines which elements of the diagram
      * may be highlighted with the mouse. <p>This flag can be a (binary)
      * combination of the following flags: <ul> <li>{@link SpeedithCirclesPanel#Spiders}:
      * which indicates that spiders will be highlighted when the user hovers
      * over them.</li> <li>{@link SpeedithCirclesPanel#Zones}: which indicates that
      * zones will be highlighted when the user hovers over them.</li> <li>{@link SpeedithCirclesPanel#Contours}:
      * which indicates that circle contours will be highlighted when the user
      * hovers over them.</li> </ul></p> <p> The {@link SpeedithCirclesPanel#All} and {@link SpeedithCirclesPanel#None}
      * flags can also be used. These indicate that all diagram or no elements
      * (respectively) can be highlighted with the mouse.</p>
      *
      * @return the set of flags that determines which elements of the diagram
      * may be highlighted with the mouse.
      */
     public int getHighlightMode() {
         return highlightMode;
     }
 
     /**
      * Sets the set of flags that determines which elements of the diagram may
      * be highlighted with the mouse. <p>See {@link SelectionStep#getSelectableElements()}
      * for a list of possible values.</p>
      * @param highlightMode the new set of flags that determines which elements
      * of the diagram may be highlighted with the mouse.
      */
     public void setHighlightMode(int highlightMode) {
         if (this.highlightMode != (highlightMode & SelectionStep.All)) {
             this.highlightMode = highlightMode & SelectionStep.All;
             applyHighlightModeToPanels();
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Events">
     /**
      * Registers the given {@link SpiderDiagramClickListener diagram click listener}
      * to the events which are fired when the user clicks on particular diagram
      * elements. <p><span style="font-weight:bold">Note</span>: the events are
      * invoked regardless of whether {@link SpiderDiagramPanel#getHighlightMode()}
      * flags are set.</p>
      *
      * @param l the event listener to register.
      */
     public void addSpiderDiagramClickListener(SpiderDiagramClickListener l) {
         this.listenerList.add(SpiderDiagramClickListener.class, l);
     }
 
     /**
      * Removes the given {@link SpiderDiagramClickListener diagram click listener}
      * from the events which are fired when the user clicks on particular
      * diagram elements. <p>The given listener will no longer receive these
      * events.</p>
      *
      * @param l the event listener to deregister.
      */
     public void removeSpiderDiagramClickListener(SpiderDiagramClickListener l) {
         this.listenerList.remove(SpiderDiagramClickListener.class, l);
     }
 
     /**
      * Returns the array of all {@link SpiderDiagramPanel#addSpiderDiagramClickListener(icircles.gui.SpiderDiagramClickListener) registered}
      * {@link SpiderDiagramClickListener diagram click listeners}.
      *
      * @return the array of all {@link SpiderDiagramPanel#addSpiderDiagramClickListener(icircles.gui.SpiderDiagramClickListener) registered}
      * {@link SpiderDiagramClickListener diagram click listeners}.
      */
     public SpiderDiagramClickListener[] getSpiderDiagramClickListeners() {
         return listenerList.getListeners(SpiderDiagramClickListener.class);
     }
 
     protected void fireSpiderDiagramClicked(int subDiagramIndex, DiagramClickEvent info) {
         SpiderDiagramClickListener[] ls = listenerList.getListeners(SpiderDiagramClickListener.class);
         if (ls != null && ls.length > 0) {
             SpiderDiagramClickEvent e = new SpiderDiagramClickEvent(this, diagram, info, subDiagramIndex);
             for (int i = 0; i < ls.length; i++) {
                 ls[i].spiderDiagramClicked(e);
             }
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Private Helper Methods">
     /**
      * This method does not remove any components, it just adds an error label
      * saying 'Drawing failed'.
      */
     private void drawErrorLabel() {
         JLabel errorLabel = new JLabel();
         errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         errorLabel.setText(i18n("PSD_LABEL_DISPLAY_ERROR"));
         diagrams.add(errorLabel);
         refreshPrefSize();
     }
 
     /**
      * This method does not remove any components, it just adds an error label
      * saying 'No diagram'.
      */
     private void drawNoDiagramLabel() {
         JLabel noDiagramLbl = new JLabel();
         noDiagramLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         noDiagramLbl.setText(i18n("CSD_PANEL_NO_DIAGRAM"));
         diagrams.add(noDiagramLbl);
         refreshPrefSize();
     }
 
     /**
      * This method puts the diagram panels onto this one. <p>This method does
      * not remove any components, it just adds them to this panel.</p> <p>In
      * case of failure, this method throws an exception and does not put any
      * components onto this panel.</p>
      */
     private void drawDiagram() throws CannotDrawException {
         if (diagram != null) {
             if (diagram instanceof CompoundSpiderDiagram) {
                 CompoundSpiderDiagram csd = (CompoundSpiderDiagram) diagram;
                 switch (csd.getOperator()) {
                     case Conjunction:
                     case Disjunction:
                     case Equivalence:
                     case Implication:
                         drawInfixDiagram(csd);
                         break;
                     case Negation:
                         drawPrefixDiagram(csd);
                         break;
                     default:
                         throw new AssertionError(speedith.core.i18n.Translations.i18n("GERR_ILLEGAL_STATE"));
                 }
             } else if (diagram instanceof PrimarySpiderDiagram) {
                 drawPrimaryDiagram((PrimarySpiderDiagram) diagram);
             } else if (diagram instanceof NullSpiderDiagram) {
                 drawNullSpiderDiagram();
             } else {
                 throw new IllegalArgumentException(i18n("SD_PANEL_UNKNOWN_DIAGRAM_TYPE"));
             }
         }
     }
 
     private void drawInfixDiagram(CompoundSpiderDiagram csd) throws CannotDrawException {
         if (csd != null && csd.getOperandCount() > 0) {
             int gridx = 0, nextSubdiagramIndex = 1;
             diagrams.setLayout(new GridBagLayout());
 
             // Now start adding the panels of operand diagrams onto the surface
             Iterator<SpiderDiagram> sdIter = csd.getOperands().iterator();
             nextSubdiagramIndex = addInfixSpiderDiagramPanel(nextSubdiagramIndex, sdIter.next(), gridx);
             while (sdIter.hasNext()) {
                 addInfixOperator(csd, ++gridx);
                 nextSubdiagramIndex = addInfixSpiderDiagramPanel(nextSubdiagramIndex, sdIter.next(), ++gridx);
             }
             refreshPrefSize();
         } else {
             throw new AssertionError(speedith.core.i18n.Translations.i18n("GERR_ILLEGAL_STATE"));
         }
     }
 
     private void addInfixOperator(CompoundSpiderDiagram csd, int gridx) {
         diagrams.add(registerSubdiagramClickListener(new OperatorPanel(csd.getOperator(), getFont()), 0), getSubdiagramLayoutConstraints(gridx, false, 0, 0));
     }
 
     private int addInfixSpiderDiagramPanel(int nextSubdiagramIndex, SpiderDiagram curSD, int gridx) throws CannotDrawException {
         GridBagConstraints gridBagConstraints;
         JPanel sdp = registerSubdiagramClickListener(DiagramVisualisation.getSpiderDiagramPanel(curSD), nextSubdiagramIndex);
         gridBagConstraints = getSubdiagramLayoutConstraints(gridx, true, sdp.getPreferredSize().width, 1);
         diagrams.add(sdp, gridBagConstraints);
         return nextSubdiagramIndex + curSD.getSubDiagramCount();
     }
 
     /**
      * This function registers a click listener to the given panel. The
      * registered listener will invoke the {@link SpiderDiagramPanel#addSpiderDiagramClickListener(speedith.ui.SpiderDiagramClickListener)
      * spider diagram click event} of this panel.
      *
      * @param diagramPanel
      * @param sd
      * @param nextSubdiagramIndex
      */
     private JPanel registerSubdiagramClickListener(JPanel diagramPanel, final int nextSubdiagramIndex) {
         if (diagramPanel instanceof SpeedithCirclesPanel) {
             SpeedithCirclesPanel cp = (SpeedithCirclesPanel) diagramPanel;
             cp.setPreferredSize(PrimaryDiagramSize);
             cp.setMinimumSize(PrimaryDiagramSize);
             cp.addDiagramClickListener(new DiagramClickListener() {
 
                 public void spiderClicked(SpiderClickedEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex, e);
                 }
 
                 public void zoneClicked(ZoneClickedEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex, e);
                 }
 
                 public void contourClicked(ContourClickedEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex, e);
                 }
             });
         } else if (diagramPanel instanceof SpiderDiagramPanel) {
             SpiderDiagramPanel sdp = (SpiderDiagramPanel) diagramPanel;
             sdp.addSpiderDiagramClickListener(new SpiderDiagramClickListener() {
 
                 public void spiderDiagramClicked(SpiderDiagramClickEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex + e.getSubDiagramIndex(), e.getDetailedEvent());
                 }
             });
         } else if (diagramPanel instanceof NullSpiderDiagramPanel) {
             diagramPanel.setFont(getFont());
             diagramPanel.addMouseListener(new MouseListener() {
 
                 public void mouseClicked(MouseEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex, null);
                 }
 
                 public void mousePressed(MouseEvent e) {
                 }
 
                 public void mouseReleased(MouseEvent e) {
                 }
 
                 public void mouseEntered(MouseEvent e) {
                 }
 
                 public void mouseExited(MouseEvent e) {
                 }
             });
         } else if (diagramPanel instanceof OperatorPanel) {
             diagramPanel.addMouseListener(new MouseListener() {
 
                 public void mouseClicked(MouseEvent e) {
                     fireSpiderDiagramClicked(nextSubdiagramIndex, null);
                 }
 
                 public void mousePressed(MouseEvent e) {
                 }
 
                 public void mouseReleased(MouseEvent e) {
                 }
 
                 public void mouseEntered(MouseEvent e) {
                 }
 
                 public void mouseExited(MouseEvent e) {
                 }
             });
         } else {
             throw new IllegalStateException(speedith.core.i18n.Translations.i18n("GERR_ILLEGAL_STATE"));
         }
         return diagramPanel;
     }
 
     private GridBagConstraints getSubdiagramLayoutConstraints(int gridx, boolean fill, int weightx, int weighty) {
         GridBagConstraints gridBagConstraints;
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = gridx;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = fill ? java.awt.GridBagConstraints.BOTH : GridBagConstraints.NONE;
         gridBagConstraints.weightx = weightx;
         gridBagConstraints.weighty = weighty;
         gridBagConstraints.insets.set(3, 2, 3, 2);
         return gridBagConstraints;
     }
 
     private void drawPrefixDiagram(CompoundSpiderDiagram csd) throws CannotDrawException {
         if (csd != null && csd.getOperandCount() == 1) {
             diagrams.add(registerSubdiagramClickListener(new OperatorPanel(csd.getOperator(), getFont()), 0));
             diagrams.add(registerSubdiagramClickListener(DiagramVisualisation.getSpiderDiagramPanel(csd.getOperands().get(0)), 1));
             refreshPrefSize();
         } else {
             throw new AssertionError(speedith.core.i18n.Translations.i18n("GERR_ILLEGAL_STATE"));
         }
     }
 
     private void drawPrimaryDiagram(PrimarySpiderDiagram psd) throws CannotDrawException {
         if (psd == null) {
             throw new AssertionError(speedith.core.i18n.Translations.i18n("GERR_ILLEGAL_STATE"));
         } else {
             diagrams.setLayout(new GridBagLayout());
             GridBagConstraints gbc = getSubdiagramLayoutConstraints(0, true, 1, 1);
             diagrams.add(registerSubdiagramClickListener(DiagramVisualisation.getSpiderDiagramPanel(psd), 0), gbc);
             refreshPrefSize();
         }
     }
 
     private void drawNullSpiderDiagram() {
         diagrams.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new java.awt.GridBagConstraints();
         diagrams.add(registerSubdiagramClickListener(new NullSpiderDiagramPanel(getFont()), 0), gbc);
         refreshPrefSize();
     }
 
     private void refreshPrefSize() {
 //        Dimension prefSize = new Dimension();
 //        for (Component component : diagrams.getComponents()) {
 //            final Dimension curPrefSize = component.getPreferredSize();
 //            prefSize.height = Math.max(prefSize.height, curPrefSize.height);
 //            prefSize.width += curPrefSize.width + 40;
 //        }
 //        
 //        prefSize.height += 10;
 //        prefSize.width += 10;
 //        setPreferredSize(prefSize);
 //        setMinimumSize(prefSize);
 //        invalidate();
     }
 
     /**
      * This method applies the currently set {@link SpiderDiagramPanel#setHighlightMode(int)
      * highlight mode} to the underlying {@link SpeedithCirclesPanel diagram presentation panels}.
      * <p>This method has to be invoked whenever the highlight mode changes or
      * when the current diagram changes.</p>
      */
     private void applyHighlightModeToPanels() {
         if (this.diagram != null) {
             for (Component component : this.diagrams.getComponents()) {
                 if (component instanceof SpeedithCirclesPanel) {
                     ((SpeedithCirclesPanel) component).setHighlightMode(highlightMode);
                 } else if (component instanceof SpiderDiagramPanel) {
                     ((SpiderDiagramPanel) component).setHighlightMode(highlightMode);
                 } else if (component instanceof OperatorPanel) {
                     OperatorPanel operatorPanel = (OperatorPanel) component;
                     operatorPanel.setHighlighting((highlightMode & SelectionStep.Operators) == SelectionStep.Operators);
                 } else if (component instanceof NullSpiderDiagramPanel) {
                     NullSpiderDiagramPanel nsdp = (NullSpiderDiagramPanel) component;
                     nsdp.setHighlighting((highlightMode & SelectionStep.NullSpiderDiagrams) == SelectionStep.NullSpiderDiagrams);
                 }
             }
         }
     }
     // </editor-fold>
 }
