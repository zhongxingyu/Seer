 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.awt.Insets;
 import java.util.Locale;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.wings.border.*;
 import org.wings.plaf.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SOptionPane
     extends SDialog
     implements ActionListener {
 
 
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "OptionPaneCG";
 
     /**
      * Actionb Performed Value if Yes is Choosen
      */
     public static final String YES_ACTION = "YES";
     
     /**
      * Action Performed Value if No is choosen
      */
     public static final String NO_ACTION = "NO";
     
     /**
      * Action Performed Value if Ok is choosen
      */
     public static final String OK_ACTION = "OK";
     
     /**
      * Action Performed Value if Cancel is choosen
      */
     public static final String CANCEL_ACTION = "CANCEL";
     
     /**
      * Action Performed Value Unknow
      */
     public static final String UNKNOWN_ACTION = "UNKNOWN";
     
     /**
      * Return value if Ok is choosen
      */
     public static final int OK_OPTION = JOptionPane.OK_OPTION;
     
     /**
      * Return value if Cancel is choosen
      */
     public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
 
     /**
      * Return Value if Yes is choosen
      */
     public static final int YES_OPTION = JOptionPane.YES_OPTION;
 
     /**
      * Return value if no is choosen
      */
     public static final int NO_OPTION = JOptionPane.NO_OPTION;
 
     /**
      * TODO: documentation
      */
     public static final int RESET_OPTION = 999;
 
     /**
      * Type meaning look and feel should not supply any options -- only
      * use the options from the JOptionPane.
      */
     public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
 
     /**
      * Used for showConfirmDialog.
      */
     public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
     
     /**
      *  Used for showConfirmDialog.
      */
     public static final int OK_CANCEL_RESET_OPTION = OK_CANCEL_OPTION + 1000;
     
     /**
      *  Used for showConfirmDialog.
      */
     public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
 
     /**
      *  Used for showConfirmDialog.
      */
     public static final int YES_NO_RESET_OPTION = YES_NO_OPTION + 1000;
 
     /**
      *  Used for showConfirmDialog.
      */
     public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
 
     /**
      *  Used for showConfirmDialog.
      */
     public static final int YES_NO_CANCEL_RESET_OPTION = YES_NO_CANCEL_OPTION + 1000;
 
     //
     // Message types. Used UI to determine the kind of icon to display,
     // and possibly what behavior to give based on the type.
     //
     /*
      * Error messages.
      */
     public static final int  ERROR_MESSAGE = javax.swing.JOptionPane.ERROR_MESSAGE;
     /*
      * Information messages.
      */
     public static final int  INFORMATION_MESSAGE = javax.swing.JOptionPane.INFORMATION_MESSAGE;
     /*
      * Warning messages.
      */
     public static final int  WARNING_MESSAGE = javax.swing.JOptionPane.WARNING_MESSAGE;
     /*
      * Questions.
      */
     public static final int  QUESTION_MESSAGE = javax.swing.JOptionPane.QUESTION_MESSAGE;
     /*
      * No icon.
      */
     public static final int   PLAIN_MESSAGE = javax.swing.JOptionPane.PLAIN_MESSAGE;
   
     /**
      * ContentPane with border layout.
      */
     private final SContainer contents = new SPanel(new SBorderLayout());
 
     /**
      * Die Message des OptionPanes wird hier rein getan.
      */
     private final SContainer optionData = new SPanel(new SFlowDownLayout());
 
     /**
      * Die Message des OptionPanes wird hier rein getan.
      */
     private final SContainer images = new SPanel();
 
     /**
      * Panel with Option Buttons
      */
     protected final SContainer optionButtons =  new SPanel(new SFlowLayout( RIGHT ));
 
     /**
      * OK Button
      */
     protected final SButton optionOK = createButton("OK");
 
     /**
      * Cancel Button
      */
     protected final SButton optionCancel = createButton("Cancel");
 
     /**
      * Yes Button
      */
     protected final SButton optionYes = createButton("Yes");
 
     /**
      * No Button
      */
     protected final SButton optionNo = createButton("No");
     
     /**
      * Reset Button
      */
     private final SResetButton optionReset = createResetButton("Reset");
 
     final SBorder empty = new SEmptyBorder(new Insets(6,24,6,24));
     
     /**
      * Icon for Inform Dialog
      */
     protected static final SIcon messageImage = new ResourceImageIcon("org/wings/icons/Inform.gif");
     
     /**
      * Icon for Input Dialog
      */
     protected static final SIcon questionImage = new ResourceImageIcon("org/wings/icons/Question.gif");
 
     /**
      * Icon for Show Confirm Dialog
      */
     protected static final SIcon yesnoImage =  new ResourceImageIcon("org/wings/icons/Question.gif");
 
     /**
      * Icon for Error Dialog
      */
     protected static final SIcon errorImage =  new ResourceImageIcon("org/wings/icons/Warn.gif");
   
     //  protected final SLabel messageLabel  = new SLabel(messageImage);
     //  protected final SLabel questionLabel = new SLabel(questionImage);
     //  protected final SLabel yesnoLabel    = new SLabel(yesnoImage);
     protected final SLabel imageLabel = new SLabel();
 
     /**
      * The chosen option
      * @see #OK_OPTION
      * @see #YES_OPTION
      * @see #CANCEL_OPTION
      * @see #NO_OPTION
      */
     protected Object selected = null;
 
     /*
      * Icon used in pane.
      */
     protected SIcon                 icon;
 
     /*
      * Message to display.
      */
     protected Object                message;
 
     /*
      * Options to display to the user.
      */
     protected Object[]              options;
 
     /*
      * Value that should be initialy selected in options.
      */
     protected Object                initialValue;
 
     /*
      * Message type. 
      */
     protected int                   messageType;
   
     /**
      * Default Constructor for <code>SOptionPane</code>
      * Against the Standard Swing Implementation there is no Standard Message
      */
     public SOptionPane() {
         this("");
     }
 
    protected void setCG(ComponentCG newCG) {
        super.setCG(newCG);
    }
  
     /*
      * Creates a instance of <code>SOptionPane</code> with a message
      *
      * @param message the <code>Object</code> to display
      */
     public SOptionPane(Object message) {
         this(message, PLAIN_MESSAGE);
     }
   
     /*
      * Creates an instance of <code>SOptionPane</code> to display a message
      * with the specified message and the default options,
      *
      * @param message the <code>Object</code> to display
      * @param messageType the type of message to be displayed:
      *                    ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                    QUESTION_MESSAGE, or PLAIN_MESSAGE
      */
     public SOptionPane(Object message, int messageType) {
         this(message, messageType, DEFAULT_OPTION);
     }
   
     /**
      * Creates an instance of <code>JOptionPane</code> to display a message
      * with the specified message type and options.
      *
      * @param message the <code>Object</code> to display
      * @param messageType the type of message to be displayed:
      *                    ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                    QUESTION_MESSAGE, or PLAIN_MESSAGE
      * @param optionType the options to display in the pane:
      *                   DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION
      *                   OK_CANCEL_OPTION
      */
     public SOptionPane(Object message, int messageType, int optionType) {
         this(message, messageType, optionType, null);
     }
   
     /**
      * Creates an instance of <code>JOptionPane</code> to display a message
      * with the specified message type, options, and icon.
      *
      * @param message the <code>Object</code> to display
      * @param messageType the type of message to be displayed:
      *                    ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                    QUESTION_MESSAGE, or PLAIN_MESSAGE
      * @param optionType the options to display in the pane:
      *                   DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION
      *                   OK_CANCEL_OPTION
      * @param icon the <code>Icon</code> image to display
      */
     public SOptionPane(Object message, int messageType, int optionType,  SIcon icon) {
         this(message, messageType, optionType, icon, null);
     }
   
     /**
      * Creates an instance of JOptionPane to display a message
      * with the specified message type, icon, and options.
      * None of the options is initially selected.
      * <p>
      * The options objects should contain either instances of
      * <code>Component</code>s, (which are added directly) or
      * <code>Strings</code> (which are wrapped in a <code>JButton</code>).
      * If you provide <code>Component</code>s, you must ensure that when the
      * <code>Component</code> is clicked it messages <code>setValue</code>
      * in the created <code>JOptionPane</code>.
      *
      * @param message the <code>Object</code> to display
      * @param messageType the type of message to be displayed:
      *                    ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                    QUESTION_MESSAGE, or PLAIN_MESSAGE
      * @param optionType the options to display in the pane:
      *                   DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION
      *                   OK_CANCEL_OPTION; only meaningful if the
      *                   <code>options</code> parameter is <code>null</code>
      * @param icon the <code>Icon</code> image to display
      * @param options  the choices the user can select
      */
     public SOptionPane(Object message, int messageType, int optionType,  
                        SIcon icon, Object[] options) {
         this(message, messageType, optionType, icon, options, null);
     }
   
     /**
      * Creates an instance of <code>JOptionPane</code> to display a message
      * with the specified message type, icon, and options, with the
      * initially-selected option specified.
      *
      * @param message the <code>Object</code> to display
      * @param messageType the type of message to be displayed:
      *                    ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                    QUESTION_MESSAGE, or PLAIN_MESSAGE
      * @param optionType the options to display in the pane:
      *                   DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION
      *                   OK_CANCEL_OPTION; only meaningful if the
      *                   <code>options</code> parameter is <code>null</code>
      * @param icon the Icon image to display
      * @param options  the choices the user can select
      * @param initialValue the choice that is initially selected
      */
     public SOptionPane(Object message, int messageType, int optionType,
                        SIcon icon, Object[] options, Object initialValue) {
         this.message = message;
         this.options = options;
         this.initialValue = initialValue;
         this.icon = icon;
         SGridLayout layout = new SGridLayout(1);
         layout.setBorder(1);
         setLayout(layout);
         initPanel();
         setOptionType(optionType);
         setMessageType(messageType);
         // value = UNINITIALIZED_VALUE;
         // inputValue = UNINITIALIZED_VALUE;
     }
 
     /*
      * The chosen option.
      * @see #OK_OPTIONhttp://localhost:8080/RLSAdmin/Admin/mP1
      * @see #YES_OPTION
      * @see #CANCEL_OPTION
      * @see #NO_OPTION
      */
     public final Object getValue() {
         return selected;
     }
 
     private final void initPanel() {
         optionButtons.add(optionOK, "OK");
         optionButtons.add(optionYes, "YES");
         optionButtons.add(optionCancel, "CANCEL");
         optionButtons.add(optionNo, "NO");
         optionButtons.add(optionReset, "RESET");
         
         /*    images.add(messageLabel);
               messageLabel.setToolTipText("info");
               images.add(questionLabel);
               questionLabel.setToolTipText("question");
               images.add(yesnoLabel);
               yesnoLabel.setToolTipText("question");
         */
         images.add(imageLabel);
         imageLabel.setToolTipText("");
 
         optionData.setBorder(empty);
         contents.add(optionData, SBorderLayout.CENTER);
         contents.add(images, SBorderLayout.WEST);
         add(contents);
 
         add(optionButtons);
     }
 
     /**
      * Generic Button creation
      *
      * @param label the <code>String</code> to display on the button
      */
     protected final SButton createButton(String label) {
         SButton b = new SButton(label);
         b.addActionListener(this);
         return b;
     }
 
     /**
      * TODO: documentation
      */
     protected final SResetButton createResetButton(String label) {
         SResetButton b = new SResetButton(label);
         b.addActionListener(this);
         return b;
     }
 
     /**
      * TODO: documentation
      *
      * @param l
      */
     public void setLocale(Locale l) {
         super.setLocale(l);
         optionOK.setText("OK");
         optionCancel.setText("Cancel");
         optionYes.setText("Yes");
         optionNo.setText("No");
         optionReset.setText("Reset");
     }
 
     /**
      * TODO: documentation
      *
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         hide();
         selected = e.getSource();
 
         if ( e.getSource()==optionOK ) {
             fireActionPerformed(OK_ACTION);
         }
         else if ( e.getSource()==optionYes ) {
             fireActionPerformed(YES_ACTION);
         }
         else if ( e.getSource()==optionCancel ) {
             fireActionPerformed(CANCEL_ACTION);
         }
         else if ( e.getSource()==optionNo ) {
             fireActionPerformed(NO_ACTION);
         }
         else {
             fireActionPerformed(UNKNOWN_ACTION);
         }
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void resetOptions() {
         optionOK.setVisible(false);
         optionYes.setVisible(false);
         optionCancel.setVisible(false);
         optionNo.setVisible(false);
         optionReset.setVisible(false);
 
         //    messageLabel.setVisible(false);
         //    questionLabel.setVisible(false);
         //    yesnoLabel.setVisible(false);
         imageLabel.setVisible(false);
     }
 
     SContainer customButtons = null;
 
     /**
      * TODO: documentation
      */
     public void setOptions(Object[] options ) {
         resetOptions();
 
         if ( customButtons==null )
             customButtons = new SPanel();
 
         for ( int i=0; i<options.length; i++ ) {
             if ( options[i] instanceof SComponent ) {
                 if ( options[i] instanceof SAbstractButton )
                     ((SAbstractButton)options[i]).addActionListener(this);
                 customButtons.add((SComponent)options[i]);
             }
             else {
                 SButton b = new SButton(options[i].toString());
                 b.addActionListener(this);
                 customButtons.add(b);
             }
         }
 
         add(customButtons);
     }
 
     /**
      * Sets the option pane's message type.
      * Dependent to the MessageType there wil be displayed a different Message Label
      * @param newType an integer specifying the kind of message to display:
      *                ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
      *                QUESTION_MESSAGE, or PLAIN_MESSAGE
      *
      * description: The option pane's message type.
      */
     public void setMessageType(int newType) {
         switch(newType) {
         case ERROR_MESSAGE: {
             imageLabel.setIcon(errorImage);
             imageLabel.setToolTipText("Error");
             break;
         }
         case INFORMATION_MESSAGE: {
             //informationLabel.setVisible(true);
             imageLabel.setIcon(messageImage);
             imageLabel.setToolTipText("Information");
             break;
         }
         case WARNING_MESSAGE: {
             imageLabel.setIcon(yesnoImage);
             imageLabel.setToolTipText("Warning");
             //warningLabel.setVisble(true);
             break;
         }
         case QUESTION_MESSAGE: {
             //questionLabel.setVisible(true);
             imageLabel.setIcon(questionImage);
             imageLabel.setToolTipText("Question");
             break;
         }
         case PLAIN_MESSAGE:
         default: {
             imageLabel.setIcon(null);
             imageLabel.setToolTipText("");
             //questionLabel.setIcon(null);
         }
         }
     
         messageType = newType;
     }
   
     /**
      * Returns the message type.
      *
      * @return an integer specifying the message type
      *
      * @see #setMessageType
      */
     public int getMessageType() {
         return messageType;
     }
   
     /**
      * TODO: documentation
      *
      * @param newType
      */
     public void setOptionType(int newType) {
         resetOptions();
 
         switch ( newType ) {
         case DEFAULT_OPTION:
             optionOK.setVisible(true);
             break;
 
         case OK_CANCEL_OPTION:
             optionOK.setVisible(true);
             optionCancel.setVisible(true);
             break;
 
         case OK_CANCEL_RESET_OPTION:
             optionOK.setVisible(true);
             optionCancel.setVisible(true);
             optionReset.setVisible(true);
             break;
 
         case YES_NO_OPTION:
             optionYes.setVisible(true);
             optionNo.setVisible(true);
             break;
 
         case YES_NO_RESET_OPTION:
             optionYes.setVisible(true);
             optionNo.setVisible(true);
             optionReset.setVisible(true);
             break;
 
         case YES_NO_CANCEL_OPTION:
             optionYes.setVisible(true);
             optionNo.setVisible(true);
             optionCancel.setVisible(true);
             break;
 
         case YES_NO_CANCEL_RESET_OPTION:
             optionYes.setVisible(true);
             optionNo.setVisible(true);
             optionCancel.setVisible(true);
             optionReset.setVisible(true);
             break;
         }
     }
 
     /**
      * TODO: documentation
      */
     public void showOption(SComponent c, String title, Object message) {
         if (title != null)
             setTitle(title);
 
         optionData.removeAll();
         if ( message instanceof SComponent ) {
             optionData.add((SComponent)message);
         }
         else {
             StringTokenizer stt = new StringTokenizer(message.toString(), "\n");
             while ( stt.hasMoreElements() ) {
                 optionData.add(new SLabel(stt.nextElement().toString()));           
             }
         }
         show(c);
     }
 
     /**
      * TODO: documentation
      */
     public static void showMessageDialog(SComponent parent, Object message, 
                                          ActionListener al) {
         showMessageDialog(parent, message, null, 0, al);
     }
 
     /**
      * TODO: documentation
      */
     public static void showMessageDialog(SComponent parent, Object message) {
         showMessageDialog(parent, message, null, 0, null);
     }
 
     /**
      * TODO: documentation
      */
     public static void showMessageDialog(SComponent parent, Object message, String title) {
         showMessageDialog(parent, message, title, 0, null);
     }
 
     /**
      * TODO: documentation
      */
     public static void showMessageDialog(SComponent parent, Object message, 
                                          String title, int messageType, ActionListener al) {
         SOptionPane p = new SOptionPane();
 
         p.showPlainMessage(parent, message, title);
         p.addActionListener(al);
     }
 
     /**
      * TODO: documentation
      */
     public static void showPlainMessageDialog(SComponent parent, Object message, 
                                               String title) {
         showPlainMessageDialog(parent, message, title, 0, null);
     }
 
 
     /**
      * TODO: documentation
      */
     public static void showPlainMessageDialog(SComponent parent, Object message, 
                                               String title, int messageType,
                                               ActionListener al) {
         SOptionPane p = new SOptionPane();
 
         p.showPlainMessage(parent, message, title);
         p.addActionListener(al);
         p.setMessageType(PLAIN_MESSAGE);
         //    p.messageLabel.setVisible(false);
     }
 
     /**
      * TODO: documentation
      */
     public void showPlainMessage(SComponent parent, Object message,  String title) {
         showOption(parent, title, message);
 
         setOptionType(DEFAULT_OPTION);
         setMessageType(PLAIN_MESSAGE);
         //    messageLabel.setVisible(true);
     }
 
 
     /**
      * TODO: documentation
      */
     public void showQuestion(SComponent parent,  Object message, String title) {
         showOption(parent, title, message);
         
         setOptionType(OK_CANCEL_OPTION);
         setMessageType(QUESTION_MESSAGE);
         //    questionLabel.setVisible(true);
     }
 
     public void showInput(SComponent parent,  Object message, 
                           SComponent inputElement,  String title) {
         showOption(parent, title, message);
         optionData.add(inputElement);
         
         setOptionType(OK_CANCEL_OPTION);
         setMessageType(QUESTION_MESSAGE);
         //    questionLabel.setVisible(true);
     }
 
     /**
      * TODO: documentation
      *
      * @param message
      * @return
      */
     // protected String getSimpleInput(Object message) {
     //     return JOptionPane.showInputDialog(this, message);
     // }
 
 
     public static void showInputDialog(SComponent parent, Object question, 
                                        String title, SComponent inputElement,
                                        ActionListener al) {
         SOptionPane p = new SOptionPane();
 
         p.showInput(parent, question, inputElement, title);
         p.addActionListener(al);
     }
 
     /**
      * TODO: documentation
      */
     public static void showQuestionDialog(SComponent parent, Object question,
                                           String title,  ActionListener al) {
         SOptionPane p = new SOptionPane();
 
         p.showQuestion(parent, question, title);
         p.addActionListener(al);
     }
 
     /**
      * TODO: documentation
      */
     public static void showPlainQuestionDialog(SComponent parent, Object question,
                                                String title, ActionListener al) {
         SOptionPane p = new SOptionPane();
 
         p.showQuestion(parent, question, title);
         p.addActionListener(al);
         p.setMessageType(PLAIN_MESSAGE);
         //    p.questionLabel.setVisible(false);
     }
 
 
     /**
      * TODO: documentation
      */
     public static void showConfirmDialog(SComponent parent, Object message, 
                                          String title) {
         showConfirmDialog(parent, message, title, 0, null);
     }
 
     /**
      * TODO: documentation
      */
     public static void showConfirmDialog(SComponent parent, Object message,
                                          String title, ActionListener al) {
         showConfirmDialog(parent, message, title, 0, al);
     }
 
     /**
      * TODO: documentation
      */
     public static void showConfirmDialog(SComponent parent, Object message, 
                                          String title, int type) {
         showConfirmDialog(parent, message, title, type, null);
     }
 
     /**
      * TODO: documentation
      */
     public static void showConfirmDialog(SComponent parent, Object message,
                                          String title, int type, ActionListener al) {
         showConfirmDialog(parent, message, title, type, al, null);
     }
 
     /**
      * TODO: documentation
      */
     public static void showConfirmDialog(SComponent parent, Object message,
                                          String title, int type, ActionListener al, 
                                          SLayoutManager layout) {
         SOptionPane p = new SOptionPane();
         if ( layout!=null ) {
             p.optionButtons.setLayout(layout);
         } // end of if ()
         
         p.addActionListener(al);
         p.showQuestion(parent, message, title, type);
     }
 
     public void showYesNo(SComponent parent, Object question, String title) {
         showOption(parent, title, question);
         setOptionType(YES_NO_OPTION);
         setMessageType(INFORMATION_MESSAGE);
         //    yesnoLabel.setVisible(true);
     }
 
     public void showQuestion(SComponent parent, Object question, String title, int type) {
         showOption(parent, title, question);
         setOptionType(type);
         setMessageType(QUESTION_MESSAGE);
     }
 
     /**
      * TODO: documentation
      */
     public static void showYesNoDialog(SComponent parent, Object question,
                                        String title, ActionListener al) {
         SOptionPane p = new SOptionPane();
         p.addActionListener(al);
 
         p.showYesNo(parent, question, title);
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(OptionPaneCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
