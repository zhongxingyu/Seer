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
 
 package com.dmdirc.addons.ui_swing.components.validating;
 
 import com.dmdirc.addons.ui_swing.components.JIconTextField;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.util.validators.ValidationResponse;
 import com.dmdirc.util.validators.Validator;
 
 import java.awt.Image;
 import java.awt.Insets;
 
 import javax.swing.ImageIcon;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 /**
  * Validating Text field.
  */
 public class ValidatingJTextField extends JIconTextField implements DocumentListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 2;
     /** Validator. */
     private Validator<String> validator;
     /** Error image. */
     private final ImageIcon errorIcon;
 
     /**
      * Instantiates a new Validating text field.
      *
      * @param iconManager Icon manager
      * @param validator   Validator instance
      */
     public ValidatingJTextField(final IconManager iconManager,
             final Validator<String> validator) {
         this(iconManager.getImage("input-error"), "", validator);
     }
 
     /**
      * Instantiates a new Validating text field.
      *
      * @param iconManager Icon manager
      * @param text        Text to display in textfield
      * @param validator   Validator instance
      */
     public ValidatingJTextField(final IconManager iconManager, final String text,
             final Validator<String> validator) {
         this(iconManager.getImage("input-error"), text, validator);
     }
 
     /**
      * Instantiates a new Validating text field.
      *
      * @param icon      Icon to show on error
      * @param text      Text to display in textfield
      * @param validator Validator instance
      */
     public ValidatingJTextField(final Image icon, final String text,
             final Validator<String> validator) {
         super();
         this.validator = validator;
         errorIcon = new ImageIcon(icon);
         setText(text);
 
         setMargin(new Insets(0, 0, 0, errorIcon.getIconWidth()));
 
         checkError();
 
         getDocument().addDocumentListener(this);
     }
 
     /**
      * Updates the validator for this textfield.
      *
      * @param validator new validator
      */
     public void setValidator(final Validator<String> validator) {
         this.validator = validator;
         checkError();
     }
 
     /**
      * Checks the text for errors and sets the error state accordingly.
      */
     public void checkError() {
         if (isEnabled()) {
             final ValidationResponse vr = validator.validate(getText());
             setMessage(vr.getFailureReason());
            firePropertyChange("validationResult", getMessage() != null, !vr.isFailure());
             if (vr.isFailure()) {
                 setIcon(errorIcon);
             } else {
                 setIcon(null);
             }
         } else {
             setIcon(null);
             setMessage(null);
            firePropertyChange("validationResult", !getMessage().isEmpty(), true);
         }
     }
 
     /**
      * Checks if the text validates.
      *
      * @see com.dmdirc.util.validators.Validator#validate(Object)
      *
      * @return true iif the text validates
      */
     public boolean validateText() {
         if (isEnabled()) {
             return !validator.validate(getText()).isFailure();
         } else {
             return true;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void changedUpdate(final DocumentEvent e) {
         checkError();
     }
 
     /** {@inheritDoc} */
     @Override
     public void insertUpdate(final DocumentEvent e) {
         checkError();
     }
 
     /** {@inheritDoc} */
     @Override
     public void removeUpdate(final DocumentEvent e) {
         checkError();
     }
 
 }
