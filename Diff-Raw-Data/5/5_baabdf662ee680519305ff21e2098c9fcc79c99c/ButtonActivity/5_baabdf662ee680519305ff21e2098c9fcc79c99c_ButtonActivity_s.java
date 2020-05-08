 package com.example.hotbutton;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import com.example.hotbutton.MainActivity.clientBuzz;
 import com.example.hotbutton.MainActivity.clientLoginAndPlay;
 import com.example.hotbutton.R.drawable;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ButtonActivity extends Activity {
 	
 	Boolean loggedIn = true;
 	//Socket client;
 	clientLoginAndPlay listener;
 	
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_button);
         
         
         final clientLoginAndPlay listener = new clientLoginAndPlay();
         listener.execute("listening");
         
         ImageView imageViewButton = (ImageView) findViewById(R.id.imageViewButton);
         imageViewButton.setEnabled(false);
         
         
         
         // buzz
         imageViewButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
             	
             	Toast.makeText(v.getContext(), "buzz", Toast.LENGTH_SHORT).show();
       
             	listener.listening = false;
             	new clientBuzz().execute();
             	
             }
         });
        
     }
     
  public class clientLoginAndPlay extends AsyncTask<String, String, String> {
     	
     	PrintWriter out;
     	BufferedReader in;
     	
       
     	
     	
     	public Boolean listening = false;
     	
     	protected String doInBackground(String... params) {
 			
     		
     		listening = true;
     		
 			try {
 				
 				out = new PrintWriter(MainActivity.socket.getOutputStream(),true);
 				in = new BufferedReader(new InputStreamReader(MainActivity.socket.getInputStream()));
 	    		
 				
 				if(!loggedIn) {
 					// send loginname to server
 					String name = params[0];
 					
 					Log.d("CLIENT", "Try login as: " + name);
 					out.println("login-" + name);
 				}
 				
 				
 				
 				while(listening)
 				{
 					
 					try {
 					
 						String line = in.readLine();
 						
 						// debug
 						Log.d("SERVER SAYS", line);
 						
 						publishProgress(line);
 					
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						//e.printStackTrace();
 					}
 				}
 				
 				Log.d("clientLoginAndPlay", "Stop listening");
 				
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			}
     		
 					
 			return null;
 		}
 
 		protected void onProgressUpdate(String... status) {
     		
 			ImageView imageViewButton = (ImageView) findViewById(R.id.imageViewButton);
 			TextView textViewMessage = (TextView) findViewById(R.id.textViewMessage);
         	
         	if(status[0].startsWith("lock") && loggedIn)
         	{
         		textViewMessage.setText("");
         		
     			imageViewButton.setEnabled(false);
     			imageViewButton.setImageResource(R.drawable.button_off);
                 
                 return;
         	}
         	
         	if(status[0].startsWith("unlock") && loggedIn)
         	{
         		textViewMessage.setText("");
         		
     			imageViewButton.setEnabled(true);
     			imageViewButton.setImageResource(R.drawable.button_on);
                 
                 return;
         	}
         	
        	if(status[0].startsWith("winner") && loggedIn)
         	{
         		textViewMessage.setText("Gewonnen");
         		
     			imageViewButton.setEnabled(false);
     			imageViewButton.setImageResource(R.drawable.button_off);
                 
                 return;
         	}
         	
        	if(status[0].startsWith("looser") && loggedIn)
         	{
         		textViewMessage.setText("Verloren");
         		
     			imageViewButton.setEnabled(false);
     			imageViewButton.setImageResource(R.drawable.button_off);
                 
                 return;
         	}
     	}
     }
     public class clientBuzz extends AsyncTask<String, String, String> {
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			super.onPreExecute();
 			
 			ImageView imageViewButton = (ImageView) findViewById(R.id.imageViewButton);
 			imageViewButton.setEnabled(false);
 			imageViewButton.setImageResource(R.drawable.button_off);
 			
 			
 		}
 		
 		
 		@Override
 		protected String doInBackground(String... params) {
 			
 
 			Log.d("clientBuzz", "BUZZ");
 			
 			PrintWriter out;
 			try {
 				out = new PrintWriter(MainActivity.socket.getOutputStream(),true);
 				
 				out.println("buzz");
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 			return null;
 		}
 		protected void onPostExecute (String arg) {
 			
 			listener = new clientLoginAndPlay();
 			listener.execute("listening");
 		}
     }
 }
