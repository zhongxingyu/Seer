 /*
  * Close button for the register of tabs
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 /*
  * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *
  *   - Neither the name of Oracle or the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package teo.isgci.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.plaf.basic.BasicButtonUI;
 
import teo.isgci.util.Utility;

 /**
  * Component to be used as tabComponent. Contains a JLabel to show the text and
  * a JButton to close the tab it belongs to.
  */
 public class ISGCITabComponent extends JPanel {
     /**
      * Should be changed every time the class is changed.
      */
     private static final long serialVersionUID = 1L;
     
     /** Parent. */
     private final JTabbedPane parent;
 
     /** 
      * Creates a new tabbedcomponent with close button and name.
      * 
      * @param pane
      *          The parent.
      * @param name
      *          The name that will be next to the close button, compiled with
      *          LaTeX.
      */
     public ISGCITabComponent(final JTabbedPane pane, String name) {
         // unset default FlowLayout' gaps
         super(new FlowLayout(FlowLayout.LEFT, 0, 0));
         if (pane == null) {
             throw new NullPointerException("TabbedPane is null");
         }
         this.parent = pane;
         setOpaque(false);
 
         // transform titles of JTabbedPane to their LaTeX text
        LatexLabel label = new LatexLabel(Utility.getShortName(name));
         // make background transparent
         label.setBackground(new Color(0, 0, 0, 0));
         
         // prevent nullpointerexceptions
         label.addMouseListener(new MouseListener() {
             
             @Override
             public void mouseReleased(MouseEvent e) { }
             
             @Override
             public void mousePressed(MouseEvent e) {
                 int i = parent.indexOfTabComponent(ISGCITabComponent.this);
                 parent.setSelectedIndex(i);
             }
             
             @Override
             public void mouseExited(MouseEvent e) { }
             
             @Override
             public void mouseEntered(MouseEvent e) { }
             
             @Override
             public void mouseClicked(MouseEvent e) { }
         });
         
         add(label);
         
         // add more space between the label and the button
         final int gap = 5;
         label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, gap));
         
         // tab button
         JButton button = new TabButton();
         add(button);
         
         // add more space to the top of the component
         setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
 
     }
 
     /** Close button. */
     private class TabButton extends JButton implements ActionListener {
         /**
          * Should be changed every time the class is changed.
          */
         private static final long serialVersionUID = 1L;
 
         /** Creates a new close button for the tab. */
         public TabButton() {
             final int size = 17;
             setPreferredSize(new Dimension(size, size));
             setToolTipText("Close this tab");
             // Make the button looks the same for all Laf's
             setUI(new BasicButtonUI());
             // Make it transparent
             setContentAreaFilled(false);
             // No need to be focusable
             setFocusable(false);
             setBorder(BorderFactory.createEtchedBorder());
             setBorderPainted(false);
             // Making nice rollover effect
             // we use the same listener for all buttons
             addMouseListener(BUTTONMOUSELISTENER);
             setRolloverEnabled(true);
             // Close the proper tab by clicking the button
             addActionListener(this);
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             int i = parent.indexOfTabComponent(ISGCITabComponent.this);
             
             
             if (i != -1) {
                 parent.remove(i);                
                 if (parent.getTabCount() > 1 
                         && i == parent.getTabCount() - 1) {
                     parent.setSelectedIndex(parent.getTabCount() - 2);
                 }
             }
         }
 
         // we don't want to update UI for this button
         @Override
         public void updateUI() { }
 
         // paint the cross
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D) g.create();
             // shift the image for pressed buttons
             if (getModel().isPressed()) {
                 g2.translate(1, 1);
             }
             g2.setStroke(new BasicStroke(2));
             g2.setColor(Color.BLACK);
             if (getModel().isRollover()) {
                 g2.setColor(Color.MAGENTA);
             }
 
             final int delta = 6;
 
             g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
                     - delta - 1);
             g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
                     - delta - 1);
             g2.dispose();
         }
     }
 
     /**
      * Listener for tab component.
      */
     private static final MouseListener BUTTONMOUSELISTENER
         = new MouseAdapter() {
         public void mouseEntered(MouseEvent e) {
             Component component = e.getComponent();
             if (component instanceof AbstractButton) {
                 AbstractButton button = (AbstractButton) component;
                 button.setBorderPainted(true);
             }
         }
 
         public void mouseExited(MouseEvent e) {
             Component component = e.getComponent();
             if (component instanceof AbstractButton) {
                 AbstractButton button = (AbstractButton) component;
                 button.setBorderPainted(false);
             }
         }
     };
 }
 
 /* EOF */
