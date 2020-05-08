 package com.example;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * Created with IntelliJ IDEA.
  * User: stanleyhon
  * Date: 9/07/12
  * Time: 4:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Menu extends Activity {
 
    @Override
    public void onCreate (Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       setContentView(R.layout.menu);
    }
 
    public void goHotseat (View view) {
       Intent intent = new Intent(this, Hotseat.class);
       startActivity(intent);
    }
 
     public void goAbout (View view) {
         Intent intent = new Intent(this, About.class);
         startActivity(intent);
     }
 
 }
