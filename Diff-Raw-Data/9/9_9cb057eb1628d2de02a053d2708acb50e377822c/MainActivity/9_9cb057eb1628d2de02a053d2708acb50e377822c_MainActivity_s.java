 package com.igen.lockpoc;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Switch;
 
 import java.io.*;
 import java.net.*;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
    public void switchState(View view) throws IOException,UnknownHostException {
     	new SendTask().execute(view);

     }
     
 	private class SendTask extends AsyncTask<View,Void,Void> {
 		Socket arduinoSocket;
 		PrintWriter arduino;
 		
 		@Override
 		protected Void doInBackground(View... params) {
 		    try {
 				arduinoSocket = new Socket("192.168.2.177",44);
 				arduino = new PrintWriter(arduinoSocket.getOutputStream(),true);
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
		   
	    	
 	    	
 		    if(((Switch) params[0]).isChecked()){
 		    	arduino.println("l");
 		    }	
 		    else {
 		    	arduino.println("u");
 		    }
 		    
 	    	arduino.close();
 	    	try {
 				arduinoSocket.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 }    
 
 
