 package com.hifiremote.jp1;
 
 import java.text.ParseException;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 public class NumberDeviceParm
   extends DeviceParameter
 {
   public NumberDeviceParm( String name, Integer defaultValue )
   {
     this( name, defaultValue, 8 );
   }
 
   public NumberDeviceParm( String name, Integer defaultValue, int bits )
   {
     this( name, defaultValue, 0, (( 1 << bits ) - 1 ));
   }
 
   public NumberDeviceParm( String name, Integer defaultValue, int min, int max )
   {
     super( name, defaultValue );
     this.min = min;
     this.max = max;
     IntVerifier verifier = new IntVerifier( min, max, true );
     tf = new JTextField();
     String helpText = "Enter a number in the range " + min + ".." + max + ".";
     if ( defaultValue != null )
       helpText += "  The default is " + defaultValue + ".";
     tf.setToolTipText( helpText );
     tf.setInputVerifier( verifier );
   }
 
   public JComponent getComponent()
   {
     return tf;
   }
 
   public Object getValue()
   {
     String text = tf.getText();
     if (( text == null ) || ( text.length() == 0 ))
       return null;
     return new Integer( tf.getText());
   }
 
   public void setValue( Object value )
   {
    tf.setText( value.toString());
   }
 
   private JTextField tf = null;
   private int min;
   private int max;
 }
