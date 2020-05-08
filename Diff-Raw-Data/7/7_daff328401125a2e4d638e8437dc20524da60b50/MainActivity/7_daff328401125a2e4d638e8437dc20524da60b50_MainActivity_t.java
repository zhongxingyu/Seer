 package com.example.grantmobile;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity 
 {
 	TextView tvTest; 
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		Uri data 					= getIntent().getData();
 		
 		tvTest 						= (TextView) findViewById(R.id.tvTest); 
 		
 		tvTest.setText("Uri data: " + data);//FOR TESTING PURPOSES ONLY.  D E L E T E BEFORE GOING INTO PRODUCTION
 				
 		int workMonthId				= getWorkMonthId(data);
 		
 		tvTest.setText("ID = " 		+ workMonthId);//FOR TESTING PURPOSES ONLY.  D E L E T E BEFORE GOING INTO PRODUCTION
 		
 		/******************************************************************************************************
 		 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.                 *
 		 *                                                                                                    *
 		 ******************************************************************************************************/
 		if (workMonthId == -1)
 		{
 			
 			closeDialog();
 			
 		}//ENDIF
 		
 		else
 		{
 			Intent intent = new Intent(this, CalendarActivity.class);
 			intent.putExtra("workMonthId", workMonthId);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 		}//ENDELSE
 		
 	}//END ONCREATE
 	
 	/**********************************************************************************************************
 	 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.  IF DATA IS NULL, IT*
 	 * RETURNS A -1 THAT SIGNIFIES THAT THE APP HAS BEEN OPENED BY CLICKING ON THE APP. IF DATA IS NOT NULL,  *
 	 * IT RETURNS THE WORKMONTHID NUMBER THAT WILL BE NEEDED IN CALENDAR ACTIVITY.                            *
 	 *                                                                                                        *
 	 * @param data                                                                                            *
 	 * @return                                                                                                *
 	 *                                                                                                        *
 	 **********************************************************************************************************/
 	
 	private int getWorkMonthId(Uri data) 
 	{			
 		if (data == null)
 		{
 			return -1;
 		}//ENDIF
 		
 		String id = data.getQueryParameter("ID");
 		
		if (id != null) {
			try {
				return Integer.parseInt(id);
			} catch (NumberFormatException e) { }
		}
		return -1;
 	}//END GETWORKMONTHID
 
 	
 	
 	/*@SuppressWarnings("unused")
 	private int IntParse(String queryParameter) 
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}//END INTPARSE
 */	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}//END ONCREATEOPTIONSMENU
 	
 	/**
 	 * This procedure launches the close dialog to inform them that they must run this app through an email or link.
 	 */
 	private void closeDialog() {
 		String message = "You have to access this app through the email link";
 		
 		//Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 		
 		new AlertDialog.Builder(this)
 		.setTitle("MSTC Grant App")
 		.setMessage(message)
 		.setCancelable(false)
 		.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				finish();
 				
 			}
 		})
 		.show();
 		
 		
 	}
 
 }//END MAIN ACTIVITY
