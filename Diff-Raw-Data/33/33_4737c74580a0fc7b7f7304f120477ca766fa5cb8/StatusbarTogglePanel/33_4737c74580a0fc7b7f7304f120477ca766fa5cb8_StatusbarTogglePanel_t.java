 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.border.Border;
 
 /**
  * A panel shown in the status bar which displays a {@link StatusbarPopupWindow}
  * when the clicks it.
  *
  * @since 0.6.6
  */
 public abstract class StatusbarTogglePanel<T extends JComponent> extends
        StatusbarPanel<T> implements HierarchyBoundsListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
 
     /**
      * Creates a new {@link StatusbarTogglePanel}, using the specified label.
      *
      * @param label The component to be displayed in the status bar
      */
     public StatusbarTogglePanel(final T label) {
         super(label);
 
        addHierarchyBoundsListener(this);
     }
 
     /**
      * Creates a new {@link StatusbarPanel}.
      *
      * @param label The component to be displayed in the status bar
      * @param nonSelectedBorder The border for when the panel is unselected
      * @param selectedBorder The border for when for the panel is selected
      */
     public StatusbarTogglePanel(final T label, final Border nonSelectedBorder,
             final Border selectedBorder) {
         super(label, nonSelectedBorder, selectedBorder);
 
        addHierarchyBoundsListener(this);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (isDialogOpen()) {
             closeDialog();
         } else {
             openDialog();
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent e) {
         // Don't care
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent e) {
         // Don't care
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent e) {
         // Don't care
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent e) {
         // Don't care
     }
 
     /** {@inheritDoc} */
     @Override
    public void ancestorMoved(final HierarchyEvent e) {
         refreshDialog();
     }
 
     /** {@inheritDoc} */
     @Override
    public void ancestorResized(final HierarchyEvent e) {
         refreshDialog();
     }
 }
