 package com.fresko;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class Retriver extends Activity implements WorkSurface.TouchCallback {
 	protected static final String TEAM_ID = "fresko_team";
 	Connector connector;
 	WorkSurface surface;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 		surface = (WorkSurface) findViewById(R.id.workTable);
 		surface.setTouchCallback(this);
 
 		final Button buttonLoad = (Button) findViewById(R.id.buttonLoad);
 		buttonLoad.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				//if (surface.getChunks() == null) 
 				{
 					connector = new Connector(Connector.DEFAULT_SERVICE_URL);
 					Bitmap[][] array = null;
 					try {
 						array = connector.connectAsArrayOfImages();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						Log.w("Fresko", e);
 						e.printStackTrace();
 					}
 					surface.setChunks(array);
 				}
 			}
 		});
 		Button buttonSend = (Button) findViewById(R.id.buttonSend);
 		buttonSend.setOnClickListener(new View.OnClickListener( ) {
 			
 			@Override
 			public void onClick(View v) {
				String secret = "dummy";		
 				String answer = connector.sendAnswer(secret);
 				Log.w("Answer", answer);
 			}
 		});
 		Log.w("Fresko", "End of onCreate");
 	}
 
 	@Override
 	public void handleSelectedUpdate() {
 		Log.w("Fresko", "Selected " + surface.getSelected() );
 	}
 
 	@Override
 	public void handleDestinationUpdate() {
 		Log.w("Fresko", "Destination " + surface.getDestination() );
 	}
 }
