 /*
  * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.actions;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.JPasswordField;
 import javax.swing.text.JTextComponent;
 
 /**
  * Copy action.
  */
 public final class CopyAction extends AbstractAction { 
     
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     
     /** Text component to be acted upon. */
     private final JTextComponent comp; 
  
     /** 
      * Instantiates a new copy action.
      * 
      * @param comp Component to be acted upon
      */
     public CopyAction(final JTextComponent comp) { 
         super("Copy"); 
         
         this.comp = comp; 
     } 
  
    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
     public void actionPerformed(final ActionEvent e) { 
         comp.copy(); 
     } 
  
     /** {@inheritDoc} */
     public boolean isEnabled() { 
         return comp.isEnabled() 
                 && comp.getSelectedText() != null
                 && !(comp instanceof JPasswordField); 
     } 
 } 
