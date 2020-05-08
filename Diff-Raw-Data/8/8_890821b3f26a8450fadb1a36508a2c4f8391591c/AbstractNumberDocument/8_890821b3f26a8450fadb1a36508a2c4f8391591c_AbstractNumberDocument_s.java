 /*
  * AbstractNumberField.java
  *
  * Created on May 7, 2013, 10:41 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package com.rameses.rcp.control.text;
 
 import java.beans.Beans;
 import java.text.DecimalFormat;
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.PlainDocument;
 
 /**
  *
  * @author wflores
  */
 public abstract class AbstractNumberDocument extends PlainDocument
 {    
     private AbstractNumberDocument self = this;    
     private boolean validateEntry = true;
     private double minValue;
     private double maxValue;
     
     private DecimalFormat formatter;
     private String format;
 
     private CustomInputVerifier inputVerifier;
     private Number value;
     private String stringValue;
 
     public AbstractNumberDocument()
     {
         inputVerifier = new CustomInputVerifier(this);
         minValue = -1.0;
         maxValue = -1.0;
         setFormat("#,##0.00");
     }
 
     public abstract void refresh();     
     public abstract Number decode(String value);
     public abstract Number convertValue(Number value);  
     protected abstract Number getPrimitiveValue(Number value);     
     
     public InputVerifier getInputVerifier() { return inputVerifier; }
         
     public double getMinValue() { return minValue; }
     public void setMinValue(double minValue) { this.minValue = minValue; }
 
     public double getMaxValue() { return maxValue; }
     public void setMaxValue(double maxValue) { this.maxValue = maxValue; }
     
     public String getFormat() { return format; }
     public void setFormat(String format) 
     { 
         this.format = format; 
         if (this.format == null)
             this.formatter = null; 
         else 
             this.formatter = new DecimalFormat(this.format); 
     }
 
     public final String getValueAsString() { return stringValue; }
     
     public Number getValue() { return value; }
 
     public void setValue(Number value)
     {
         this.value = value;
         this.stringValue = (value != null) ? value.toString() : null;
         
         if (value != null)
         {
             try
             {
                 Number num = convertValue(value); 
                 String text = (num == null? "": num.toString());
                 
                 super.remove(0, getLength()); 
                 super.insertString(0, text, null);
                 
                 this.stringValue = (num == null? null: num.toString()); 
                 this.value = num;                
             }
             catch(RuntimeException re) {
                 throw re;
             }
             catch(Exception ex) {
                 throw new RuntimeException(ex.getMessage(), ex);
             }
         }
     }
     
     public void setValue(String text) 
     {
         try 
         {
             super.remove(0, getLength());
             insertString(0, text, null); 
         } 
         catch (BadLocationException ex) 
         {
             this.stringValue = null;
             this.value = null; 
         } 
     }
        
     public void insertString(int offs, String str, AttributeSet a) throws BadLocationException 
     {
         if (Beans.isDesignTime())
         {
             super.insertString(offs, str, a);
             return;
         }
         
         if (!validateEntry) 
         {
             super.insertString(offs, str, a); 
             validateEntry = true;
             return;
         }
         
         StringBuffer sb = new StringBuffer(getText(0, getLength()));
         sb.insert(offs, str);
         
         if (sb.toString().equals("-"))
         {
             super.insertString(offs, str, a);
             stringValue = null;
             value = null;
         }
         else
         {
             String text = sb.toString();
             if (text.equals("-.")) 
                 sb.insert(1, "0");
             else if (text.equals(".")) 
                 sb.insert(0, "0");
                         
             Number num = decode(sb.toString());
             if (num != null)
             {
                 super.insertString(offs, str, a);
                 if (text.equals("-.") || text.equals("."))
                     super.insertString(offs, "0", a);
                 
                 stringValue = sb.toString();
                 value = num;
             }
         }
     }
     
     public void remove(int offs, int len) throws BadLocationException 
     {
         StringBuffer sb = new StringBuffer(getText(0, getLength()));
         sb.delete(offs, offs+len);
         if (sb.length() == 0)
         {
             super.remove(offs, len);
             stringValue = null;
             value = null;
         }
         else
         {
             Number num = decode(sb.toString());
             super.remove(offs, len);
             stringValue = sb.toString();
             value = num;
         }
     }
 
     protected boolean isInRange(Number value)
     {
         try 
         {
             inputVerifier.checkRange(value);
             return true;
         }
         catch(Exception ex) {
             return false;
         }
     }
     
     protected String formatValue(Number value) 
     {
         if (value == null) return null;
         if (formatter == null) return value.toString(); 
         
         String pattern = getFormat();
         if (pattern == null || pattern.trim().length() == 0) return value.toString();
         
         DecimalFormat formatter = new DecimalFormat(pattern);
         return formatter.format(value);
     }    
 
    void showFormattedText(boolean show) 
     {
         try 
         {
             super.remove(0, getLength());
             
             Number num = getValue();
             if (num == null) return;
             
             if (show)
             {
                 String snum = formatValue(num); 
                 super.insertString(0, snum, null);
             }
             else {
                 super.insertString(0, num.toString(), null); 
             }
         } 
         catch (BadLocationException ex) {}
     }
  
     private void finalizeValue() 
     {
         try
         {
             if (this.value == null) return;
             
             Number num = convertValue(this.value);
             String snum = formatValue(num);
             
             super.remove(0, getLength()); 
             super.insertString(0, snum, null); 
             
             this.stringValue = snum;
             this.value = num;
         }
         catch(Exception ex) {;}
     }
     
     // <editor-fold defaultstate="collapsed" desc=" CustomInputVerifier (Class) "> 
     
     private class CustomInputVerifier extends InputVerifier
     {
         //private Tooltip tooltip;
         
         public CustomInputVerifier(AbstractNumberDocument document) 
         {
             //this.tooltip = new Tooltip();
         }
         
         public boolean verify(JComponent input) 
         { 
             JTextComponent jtc = (JTextComponent) input;
             if (!jtc.isEnabled()) return true;
             if (!jtc.isEditable()) return true;
             
             Number num = self.value;
             if (num != null)
             {
                 try {
                     checkRange(num);
                 }
                 catch(Exception ex)
                 {
                     input.putClientProperty("Verification-Error", ex.getMessage());
                     //tooltip.setText(ex.getMessage());
                     //tooltip.show(input);
                     return false;
                 }
             } 
             else if (jtc.getText() != null && jtc.getText().length() > 0) {
                 jtc.setText(""); 
             } 
             
             input.putClientProperty("Verification-Error", null);
             //tooltip.hide();
             self.finalizeValue();
             return true;
         }
         
         public void checkRange(Number num) throws Exception
         {
             if (num == null) return;
             
             double dv = num.doubleValue();
             if (self.minValue >= 0 && self.maxValue >= 0)
             {
                 if (dv >= self.minValue && dv <= self.maxValue) {;}
                 else
                     throw new Exception("The value entered must be between " + self.minValue + " and " + self.maxValue);
             }
             else
             {
                 if (self.minValue >= 0 && dv < self.minValue)
                     throw new Exception("The value entered must be greater than or equal to " + self.minValue);
 
                 if (self.maxValue >= 0 && dv > self.maxValue) 
                     throw new Exception("The value entered must be less than or equal to " + self.maxValue);
             }
         }
     } 
     
     // </editor-fold>
     
 }
