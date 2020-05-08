 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.implclasses;
 
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swt.interfaces.ITextImplClass;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * Implementation class for <code>JTextComponent</code> and subclasses.
  *
  * @author BREDEX GmbH
  * @created 19.04.2006
  */
 public class TextImplClass extends AbstractControlImplClass 
     implements ITextImplClass {
     
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         TextImplClass.class);
     
     /** The <code>swt Text</code> instance. */
     private Text m_textComponent;
     
     /**
      * @return The <code>swt Text</code> instance.
      */
     private Text getTextComponent() {
         return (Text)getComponent();
     }
     
     /**
      * {@inheritDoc}
      */
     public Control getComponent() {
         return m_textComponent;
     }
     
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_textComponent = (Text)graphicsComponent;
     }
 
     /**
      * Sets the caret at the position <code>index</code>.
      * @param index The caret position
      */
     private void setCaretPosition(final int index) {
         if (index < 0) {
             throw new StepExecutionException("Invalid position: " + index, //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.INPUT_FAILED));
         }
         int index2 = 0;
         String text = getText();
         if (text != null) {
             index2 = index > text.length() ? text.length() : index;
         }
         setSelection(index2);
     }
     
     /**
      * @return The text
      */
     protected String getText() {
         String actual = (String)getEventThreadQueuer().invokeAndWait(
             "getText", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return getTextComponent().getText();
                 }
             });
         return actual;
     }
     
     /**
      * 
      * @return the selectes text
      */
     protected String getSelectionText() {
         String actual = (String)getEventThreadQueuer().invokeAndWait(
             "getText", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return getTextComponent().getSelectionText();
                 }
             });
         return actual;
     }
     
     /**
      * Action to read the value of a TextField to store it in a variable
      * in the Client
      * @param variable the name of the variable
      * @return the text value.
      */
     public String gdReadValue(String variable) {
         return getText();
     }
     
     /**
      * @param text The text to insert at the current caret position
      */
     protected void insertText(final String text) {
         // Scroll it to visible first to ensure that the typing
         // performs correctly.
         getRobot().scrollToVisible(getComponent(), null);
         getRobot().type(getComponent(), text);
     }
     
     /**
      * Selects all text in the component.
      */
     protected void selectAll() {
         final String totalText = getText();
        try {
            getRobot().keyStroke(getRobot().getSystemModifierSpec() + " A"); //$NON-NLS-1$
        } catch (StepExecutionException see) {
            /*This might happen under certain circumstances e.g. on MacOS X see
              bug 342691 */ 
            log.warn(see);
        }
         
         if (!totalText.equals(getSelectionText())) {
             // the selection failed for some reason
             getEventThreadQueuer().invokeAndWait("text.selectAll", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             getTextComponent().selectAll();
                             return null;
                         }
                     });
         }
         
         String selectionText = getSelectionText();
         if (!totalText.equals(selectionText)) {
             log.warn("SelectAll failed!\n"  //$NON-NLS-1$
                 + "Total text: '" + totalText + "'\n"   //$NON-NLS-1$//$NON-NLS-2$
                 + "Selected text: '" + selectionText + "'"); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
     
     /**
      * Selects text in the component.
      * 
      * @param start The index at which the selection begins.
      * @param end   The index at which the selection ends.
      */
     protected void setSelection(final int start, final int end) {
         getEventThreadQueuer().invokeAndWait("setSelection", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     getTextComponent().setSelection(start, end);
                     return null;
                 }
             });
 
     }
     
     /**
      * Selects text in the component, from <code>start</code> to the end of the 
      * text.
      * 
      * @param start The index at which the selection begins.
      */
     protected void setSelection(final int start) {
         getEventThreadQueuer().invokeAndWait("setSelection", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     getTextComponent().setSelection(start);
                     return null;
                 }
             });
 
     }
 
     /**
      * @param pattern The pattern to find
      * @return The start index of the pattern
      * @throws StepExecutionException If the pattern is invalid or cannot be found
      */
     protected int getPatternIndex(String pattern) 
         throws StepExecutionException {
         
         if (pattern == null || pattern.length() == 0) {
             throw new StepExecutionException("Invalid pattern for insertion", //$NON-NLS-1$
                 EventFactory.createActionError());
         }
         String tcText = getText();
         int index = tcText.indexOf(pattern);
         if (index == -1) {
             throw new StepExecutionException("The pattern '" + pattern //$NON-NLS-1$
                 + "' could not be found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         return index;
     }
     
     /**
      * Verifies if the textfield shows the passed text.
      * @param text The text to verify.
      * @param operator The operator used to verify
      */
     public void gdVerifyText(String text, String operator) {
         Verifier.match(getText(), text, operator);
     }
     
     /**
      * Verifies if the textfield shows the passed text.
      * @param text The text to verify.
      */
     public void gdVerifyText(String text) {
         gdVerifyText(text, MatchUtil.DEFAULT_OPERATOR);
     }
     
     /**
      * Types <code>text</code> into the component. This replaces the shown content.
      * @param text the text to type in
      */
     public void gdReplaceText(String text) {
         gdSelect();
         if (StringConstants.EMPTY.equals(text)) {
             getRobot().keyType(getComponent(), SWT.DEL);
         }
         insertText(text);
     }
 
     /**
      * Types <code>text</code> into the component.
      * @param text the text to type in
      */
     public void gdInputText(String text) {
         if (!hasFocus()) {
             gdClick(1);
         }
         insertText(text);
     }
 
     
     /**
      * Inserts <code>text</code> at the position <code>index</code>.
      * @param text The text to insert
      * @param index The position for insertion
      */
     public void gdInsertText(String text, int index) {
         gdClick(1);
         setCaretPosition(index);
         insertText(text);
     }
     
     /**
      * Inserts <code>text</code> before or after the first appearance of
      * <code>pattern</code>.
      * @param text The text to insert
      * @param pattern The pattern to find the position for insertion
      * @param operator Operator to select Matching Algorithm
      * @param after If <code>true</code>, the text will be inserted after the
      *            pattern, otherwise before the pattern.
      * @throws StepExecutionException If the pattern is invalid or cannot be found
      */
     public void gdInsertText(String text, String pattern, String operator, 
             boolean after)
         throws StepExecutionException {
         
         if (text == null) {
             throw new StepExecutionException(
                 "The text to be inserted must not be null", EventFactory //$NON-NLS-1$
                     .createActionError());
         }
         final MatchUtil.FindResult matchedText = MatchUtil.getInstance().
             find(getText(), pattern, operator);
         
         if ((matchedText == null) || (matchedText.getStr() == null)) {
             throw new StepExecutionException("The pattern '" + pattern //$NON-NLS-1$
                 + "' could not be found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
 
         int index = matchedText.getPos();
         
         int insertPos = after ? index + matchedText.getStr().length() : index;
         gdInsertText(text, insertPos);
     }
     
     /**
      * Verifies the editable property.
      * @param editable The editable property to verify.
      */
     public void gdVerifyEditable(boolean editable) {
         Verifier.equals(editable, isEditable());
     }
 
     /**
      * select the whole text of the textfield by calling "selectAll()".
      */
     public void gdSelect() {
         gdClick(1);
         // Wait a while. Without this, we got no selectAll sometimes!
         TimeUtil.delay(100);
         selectAll();
     }
     
     /**
      * Selects the first appearance of <code>pattern</code> in the text
      * component's content.
      * @param pattern The pattern to select
      * @param operator operator
      * @throws StepExecutionException If the pattern is invalid or cannot be found
      */
     public void gdSelect(final String pattern, String operator) 
         throws StepExecutionException {
         
         gdClick(1);
         final MatchUtil.FindResult matchedText = MatchUtil.getInstance().
             find(getText(), pattern, operator);
         if (matchedText == null || matchedText.getStr().length() == 0) {
             throw new StepExecutionException("Invalid pattern for selection", //$NON-NLS-1$
                 EventFactory.createActionError());
         }
         final int index = matchedText.getPos();
         if (operator.startsWith("not")) { //$NON-NLS-1$
             if (pattern.equals(getText())) {
                 String msg = "The pattern '" + pattern //$NON-NLS-1$
                     + "' is equal to current text"; //$NON-NLS-1$
                 throw new StepExecutionException(msg, EventFactory
                     .createActionError(TestErrorEvent
                         .EXECUTION_ERROR, new String[] {msg}));
             } else if (index > 0) {
                 // select part before pattern
                 setSelection(0, index);
             } else {
                 // select part after pattern
                 setSelection(matchedText.getStr().length(), getText().length());
             }
         } else {
             setSelection(index, index + matchedText.getStr().length());
         }
     }
     
     /**
      * Returns the editable state of the component.
      * 
      * @return  <code>true</code> if the component is editable. Otherwise
      *          <code>false</code>.
      */
     protected boolean isEditable() {
         return ((Boolean)getEventThreadQueuer().invokeAndWait(
                 "isEditable", //$NON-NLS-1$
                 new IRunnable() {
                 public Object run() {
                     return getTextComponent().getEditable() 
                         && getTextComponent().getEnabled()
                             ? Boolean.TRUE : Boolean.FALSE; // see findBugs
                 }
             })).booleanValue();
     }
     
     /**
      * performs a <code>count</code> -click with the left button on the
      * textfield. The click will be performed at the first caret position.
      * @param count the number of clicks
      */
     public void gdClick(int count) {
         gdClick(count, 1);
     }
     
     /**
      * performs a <code>count</code> -click on the textfield. The click will
      * be performed at the first caret position.
      * @param count the number of clicks
      * @param button the button that was clicked
      */
     public void gdClick(final int count, final int button) {
         gdClickDirect(count, button, 6, POS_UNIT_PIXEL, 50, POS_UNI_PERCENT);
     }
     
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         return null;
     }
 }
