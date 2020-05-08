 package org.xmms2.server;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 /**
  * @author Eclipser
  */
 public class ServiceTest extends Activity
 {

     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.service_test);
     }
 
     public void start(View view)
     {
         startService(new Intent(this, Server.class));
     }
 
     public void stop(View view)
     {
         stopService(new Intent(this, Server.class));
     }

 }
