 package com.rameses.rcp.control;
 
 import com.rameses.rcp.ui.ControlProperty;
 import com.rameses.rcp.util.ActionMessage;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.rcp.util.UIInputUtil;
 import com.rameses.util.ValueUtil;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.beans.Beans;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author Windhel
  */
 
 public class XDateField extends XTextField {
     
    private static int CURRENT_MILLENIUM = 2000;
    
     private Date currentDate;
     private SimpleDateFormat inputFormatter;
     private SimpleDateFormat outputFormatter;
     private SimpleDateFormat valueFormatter;
     private String valueFormat;
     private String outputFormat;
     private String inputFormat;
     private Date date;
     private String formattedString;
     private int txtYPos;
     private int txtXPos;
     private String guideFormat;
     private char dateSeparator;
     private boolean autoComplete;
     
     public XDateField() {
         setOutputFormat("yyyy-MM-dd");
         setInputFormat("yyyy-MM-dd");
         DateFieldSupport dateFieldSupport = new DateFieldSupport();
         addFocusListener(dateFieldSupport);
         addKeyListener(dateFieldSupport);
         guideFormat = getInputFormat();
     }
     
     public Object getValue() {
         if( Beans.isDesignTime())
             return "";
         
         try {
             if ( !ValueUtil.isEmpty(getText()) ) {
                 date = inputFormatter.parse(getText());
                 formattedString = inputFormatter.format(date);
             }
         } catch(Exception e) {
             formattedString = null;
             ActionMessage actionMessage = getActionMessage();
             actionMessage.addMessage("", "Expected format for {0} is " + inputFormat, new Object[] {getCaption()});
             
             if(actionMessage.hasMessages())  {
                 ControlProperty controlProperty = getControlProperty();
                 controlProperty.setErrorMessage(actionMessage.toString());
             }
         }
         
         return formattedString;
     }
     
     public void setValue(Object value) {
         if ( value instanceof KeyEvent ) {
             String text = ((KeyEvent) value).getKeyChar()+"";
             if ( text.matches("[\\d]")) {
                 setText( text );
             }
         } else {
             if ( value != null ) {
                 try{
                     value = outputFormatter.parse(value.toString());
                 }catch(Exception ex) { ex.printStackTrace(); }
             }
            setText( value==null? "" : outputFormatter.format(value) );
         }
     }
     
     public void refresh() {
         Object value = UIControlUtil.getBeanValue(this);
         setValue(value);
     }
     
     public void load() {
         setInputVerifier(UIInputUtil.VERIFIER);
         guideFormat = getInputFormat();
     }
     
     public String getOutputFormat() {
         return outputFormat;
     }
     
     public void setOutputFormat(String pattern) {
         this.outputFormat = pattern;
         if( !ValueUtil.isEmpty(pattern) )
             outputFormatter = new SimpleDateFormat(pattern);
         else
             outputFormatter = null;
     }
     
     public String getInputFormat() {
         return inputFormat;
     }
     
     public void setInputFormat(String inputFormat) {
         this.inputFormat = inputFormat;
         if( !ValueUtil.isEmpty(inputFormat) )
             inputFormatter = new SimpleDateFormat(inputFormat);
         else
             inputFormatter = null;
     }
     
     
     public boolean isAutoComplete() {
         return autoComplete;
     }
     
     public void setAutoComplete(boolean autoComplete) {
         this.autoComplete = autoComplete;
     }
     
     public String getValueFormat() {
         return valueFormat;
     }
     
     public void setValueFormat(String valueFormat) {
         this.valueFormat = valueFormat;
         if( !ValueUtil.isEmpty(valueFormat))
             valueFormatter = new SimpleDateFormat(valueFormat);
         else
             valueFormatter = null;
     }
     
     private final void showFormattedValue(boolean formatted) throws ParseException {
         Object value = UIControlUtil.getBeanValue(this);
         if( formatted && outputFormatter !=null && value!=null ) {
             setText( outputFormatter.format(outputFormatter.parse(value.toString())) );
         } else {
             if( value == null )
                 setText("");
             else {
                 setText( inputFormatter.format(inputFormatter.parse(value.toString())) );
             }
         }
     }
     
     public void calculatePosition() {
         txtYPos = (int)(getHeight() /2) + (getInsets().top + (int)(getInsets().bottom / 2));
         for(char c : getInputFormat().toCharArray()) {
             if(c != 'y' && c != 'M' && c != 'd') {
                 dateSeparator = c;
             }
         }
         
         if(super.getText().length() <= getInputFormat().length())
             guideFormat = getInputFormat().substring(super.getText().length());
         txtXPos = getInsets().left;
         for(int i = 0 ; i < super.getText().length() ; i++) {
             txtXPos = txtXPos + (getFontMetrics(getFont()).charWidth(super.getText().charAt(i)));
         }
     }
     
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         if(Beans.isDesignTime() == false) {
             g.setColor(Color.LIGHT_GRAY);
             g.setFont(getFont());
             calculatePosition();
             g.drawString(guideFormat, txtXPos, txtYPos);
         }
     }
     
     
     //<editor-fold defaultstate="collapsed" desc="  DateFieldSupport (class)  ">
     private class DateFieldSupport implements FocusListener, KeyListener {
         
         public void focusGained(FocusEvent e){
             try {
                 showFormattedValue(false);
             }catch(Exception ex) { ex.printStackTrace(); }
         }
         
         public void focusLost(FocusEvent e) {
             if ( e.isTemporary() ) return;
             
             //insert autocomplete here
             
             
             try{
                 showFormattedValue(true);
             }catch(Exception ex) { ex.printStackTrace(); }
         }
         
         public void keyTyped(KeyEvent e) {}
         
         public void keyPressed(KeyEvent e) {
             if(e.getKeyChar() != dateSeparator &&
                     e.getKeyChar() != KeyEvent.VK_BACK_SPACE &&
                     e.getKeyChar() != KeyEvent.VK_HOME &&
                     e.getKeyChar() != KeyEvent.VK_END &&
                     e.getKeyChar() != KeyEvent.VK_LEFT &&
                     e.getKeyChar() != KeyEvent.VK_KP_RIGHT &&
                     e.getKeyChar() != KeyEvent.VK_KP_LEFT &&
                     e.getKeyCode() != 37 &&
                     e.getKeyCode() != 39) {
                 if(XDateField.this.getInputFormat().length() > XDateField.this.getText().length() ) {
                     if(XDateField.this.getInputFormat().charAt(XDateField.this.getText().length()) == '-')
                         XDateField.this.setText(XDateField.this.getText() + dateSeparator);
                 }
             }
         }
         
         public void keyReleased(KeyEvent e) {
             
         }
         
     }
     //</editor-fold>
     
 }
