 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.vetoable;
 
 import com.dmdirc.util.collections.ListenerList;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 
 import javax.swing.DefaultComboBoxModel;
 
 /**
  * Vetoable combo box model.
  *
  * @param <T> Type to be stored
  */
 public class VetoableComboBoxModel<T> extends DefaultComboBoxModel<T> {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Listener list. */
     private final ListenerList listeners = new ListenerList();
 
     /**
      * Constructs an empty DefaultComboBoxModel object.
      */
     public VetoableComboBoxModel() {
         super();
     }
 
     /**
      * Constructs a DefaultComboBoxModel object initialized with an array of objects.
      *
      * @param items an array of Object objects
      */
     public VetoableComboBoxModel(final T[] items) {
         super(items);
     }
 
     /**
      * Adds a vetaoble selection listener to this model.
      *
      * @param l Listener to add
      */
     public void addVetoableSelectionListener(
             final VetoableChangeListener l) {
         listeners.add(VetoableChangeListener.class, l);
     }
 
     /**
      * Removes a vetoable selection listener from this model.
      *
      * @param l Listener to remove
      */
     public void removeVetoableSelectionListener(
             final VetoableChangeListener l) {
         listeners.remove(VetoableChangeListener.class, l);
     }
 
     /**
      * Fires a vetoable selection change event.
      *
      * @param oldValue Old value
      * @param newValue New value
      *
      * @return true iif the event is to be vetoed
      */
     protected boolean fireVetoableSelectionChange(final Object oldValue, final Object newValue) {
         final PropertyChangeEvent event = new PropertyChangeEvent(this, "selection", oldValue,
                 newValue);
         for (VetoableChangeListener listener : listeners.get(VetoableChangeListener.class)) {
             try {
                 listener.vetoableChange(event);
             } catch (PropertyVetoException ex) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public void setSelectedItem(final Object anObject) {
         final Object oldItem = getSelectedItem();
 
         if (!fireVetoableSelectionChange(oldItem, anObject)) {
            super.setSelectedItem(anObject);
         }
     }
 
 }
