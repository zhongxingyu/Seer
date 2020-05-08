 package com.blissapplications.formframeworks.rules;
 
 import android.widget.TextView;
 import com.blissapplications.formframeworks.forms.IValidator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Luis
  * Date: 25/08/13
  * Time: 19:45
  */
 public class PasswordFieldRule implements IValidator {
 
   private TextView password1;
   private TextView password2;
 
   public PasswordFieldRule(TextView textView1, TextView textView2){
     this.password1 = textView1;
    this.password1 = textView2;
   }
 
   @Override
   public boolean validate() {
    String v1 = password1.getText().toString();
    String v2 = password2.getText().toString();
     if(v1.equals(v2)){
       return true;
     }else{
       //TODO localize this
       password2.setError("Sorry, password values don't match!");
       return false;
     }
   }
 }
