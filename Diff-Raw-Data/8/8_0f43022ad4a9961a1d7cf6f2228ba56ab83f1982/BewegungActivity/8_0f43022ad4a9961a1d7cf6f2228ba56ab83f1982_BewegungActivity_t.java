 package de.tuchemnitz.remoteclient;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import de.tuchemnitz.remoteclient.NetworkModule.VIDEOSTATE;
 
 /**
  * 
  * @file   BewegungActivity.java
  * @author Riko Streller
  * 
  * Creates a surface for control simple movement of the robot
  *
  */
 public class BewegungActivity extends SherlockActivity {
 
 	static private int bewegungsart = R.id.bewa_rbutton_LAUFEN;
 	private RadioGroup bewart_radiogroup1;
 	private RadioGroup bewart_radiogroup2;
 	private RadioGroup bewart_radiogroupA;
 	private OnCheckedChangeListener Grp1Listener = null;
 	private OnCheckedChangeListener Grp2Listener = null;
 
 	private MenuItem BatteryIcon;
 	private MenuItem ConnectIcon;
 	
 	
 	/**
 	 * Loads/sets up the Activity layout and registers the callback.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_bewegung);
 		
 		final ImageView videopic = (ImageView) findViewById(R.id.bew_videoimage);
 		videopic.setAlpha(VideoModule.Videotransparency_bewact);
 		
 		Callbacksplit.registerBewegungActivity(this);
 		
 		Log.v("BewAct", "init BewActivity");
 		bewart_radiogroup1 = (RadioGroup) findViewById(R.id.bew_radioGroup1);
 		if (bewart_radiogroup1 != null)
 			bewart_radiogroup1.check(bewegungsart);
 		bewart_radiogroup2 = (RadioGroup) findViewById(R.id.bew_radioGroup2);
 		if (bewart_radiogroup2 != null)
 			bewart_radiogroup2.check(bewegungsart);
 		bewart_radiogroupA = (RadioGroup) findViewById(R.id.bew_radioGroupA);
 		if (bewart_radiogroupA != null)
 			bewart_radiogroupA.check(bewegungsart);
 		setListeners();
 		
		if(VideoModule.Videotransparency_bewact != 0 && VideoModule.isVideoThreadStarted())
 		{
 			NetworkModule.Video(VIDEOSTATE.ON, VideoModule.getVideoServerPort());
 		}
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		Callbacksplit.setActiveActivity(this);
 		VideoModule.setVideoPicture((ImageView) findViewById(R.id.bew_videoimage));
 		
 	}
 	@Override
 	protected void onPause(){
 		super.onPause();
 		Callbacksplit.unsetActiveActivity();
 		VideoModule.unsetVideoPicture();
 	}
 	
 	@Override
     public void onDestroy(){
 		super.onDestroy();
     	Callbacksplit.registerBewegungActivity(null);
     	NetworkModule.Video(VIDEOSTATE.OFF);
     }
 
 	/**
 	 * Loads ActionBar, enables Back button and
 	 * gets and refreshes Battery and Connection Icons.
 	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getMenuInflater().inflate(R.menu.activity_main, menu);
     	super.onCreateOptionsMenu(menu);
         getSupportMenuInflater().inflate(R.menu.actionbar, menu);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         BatteryIcon = (MenuItem)menu.findItem(R.id.acb_battery);
         setActBarBatteryIcon(Callbacksplit.getsavedBatteryStateIcon());
         ConnectIcon = (MenuItem)menu.findItem(R.id.acb_connect);
         setActBarConnectIcon();
         
         ((MenuItem)menu.findItem(R.id.acb_m_2)).setVisible(false);
         
         return true;
     }
 	
     /**
      * Handles option menu clicks.
      * Shows other activities or closes the application.
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		super.onOptionsItemSelected(item);
 		Intent intent;
 		switch(item.getItemId()){
 		case android.R.id.home:
 		case R.id.acb_m_1:
 			finish();
 			break;
 		case R.id.acb_m_2:
 			break;
 		case R.id.acb_m_3:
 			intent = new Intent(Callbacksplit.getMainActivity(), SprachausgabeActivity.class);
 			finish();
 			startActivity(intent);
 			break;
 		case R.id.acb_m_4:
 			intent = new Intent(Callbacksplit.getMainActivity(), SpecialsActivity.class);
 			finish();
 			startActivity(intent);
 			break;
 		case R.id.acb_m_5:
 			intent = new Intent(Callbacksplit.getMainActivity(), ConfigActivity.class);
 			finish();
 			startActivity(intent);
 			break;
 		case R.id.acb_video:
 			if(VideoModule.Videotransparency_bewact != 0)
 			{
 				VideoModule.create_dialog(BewegungActivity.this, false);
 			}
 			else
 			{
 				VideoModule.create_dialog(BewegungActivity.this, true);
 			}
 			break;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Creates and initialise the listener-functions for Radiobuttons
 	 * 
 	 * This function is used from the onCreate-function of this activity
 	 */
 	private void setListeners(){
 		
 		if (bewart_radiogroupA != null)
 			bewart_radiogroupA.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(RadioGroup group, int checkedId) {
 					bewegungsart = checkedId;
 					Log.v("Check", "Clicked on All: " + Integer.toString(checkedId));
 				}
 			});
 		if (bewart_radiogroup1 != null)
 		{
 			Grp1Listener = new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(RadioGroup group, int checkedId) {
 					if (checkedId != -1)
 					{
 						bewegungsart = checkedId;
 						bewart_radiogroup2.setOnCheckedChangeListener(null);
 						bewart_radiogroup2.clearCheck();
 						bewart_radiogroup2.setOnCheckedChangeListener(Grp2Listener);
 					}
 					Log.v("Check", "Clicked on 1: " + Integer.toString(checkedId));
 				}
 			};
 			bewart_radiogroup1.setOnCheckedChangeListener(Grp1Listener);
 		}
 		if (bewart_radiogroup2 != null)
 		{
 			Grp2Listener = new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(RadioGroup group, int checkedId) {
 					if (checkedId != -1)
 					{
 						bewegungsart = checkedId;
 						bewart_radiogroup1.setOnCheckedChangeListener(null);
 						bewart_radiogroup1.clearCheck();
 						bewart_radiogroup1.setOnCheckedChangeListener(Grp1Listener);
 					}
 					Log.v("Check", "Clicked on 2: " + Integer.toString(checkedId));
 				}
 			};
 			bewart_radiogroup2.setOnCheckedChangeListener(Grp2Listener);
 		}
 	}
 	
 	/*bewa_radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 	
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		Toast toast = Toast.makeText(MainActivity.this, "checked: " + Integer.toString(checkedId), Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
     	toast.show();
 	}
 	});*/
 	/* ------------ Funktion bei OK Button ------------ */
 	/*Button dial_button_ok = (Button) bewa_dialog.findViewById(R.id.bewa_OKbutton);
 	dial_button_ok.setOnClickListener(new OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			bewa_dialog.dismiss();
 			final RadioGroup bewa_radiogroup = (RadioGroup)bewa_dialog.findViewById(R.id.bew_radioGroup1);
 			int checkedId = bewa_radiogroup.getCheckedRadioButtonId();
 			
 			bewegungsart = checkedId;
 		}
 	});*/
 	
 	
 	/************** BEW_BUTTONS ******************
      *********************************************/
 	
 	/**
 	 * Functionality for the Arrow-Up-Button
 	 * 
 	 * @param view		ignored, ID of the button, which trigger this function
 	 *  
 	 * Depending on what is chosen by the radiobuttons
 	 * the function makes the Networkmodule send a command 
 	 * to the robot to rise the head, one of the arms or to walk forward
 	 */
     public void bew_button1_event(View view) {
     	int MoveType;
     	String ToastStr;
     	
     	switch(bewegungsart)
     	{
     	case R.id.bewa_rbutton_LAUFEN:
     		MoveType = 0;	// MOVE
     		ToastStr = "vorwärts";
     		break;
     	case R.id.bewa_rbutton_ARM_L:
     		MoveType = 1;	// ARM
     		ToastStr = "l. Arm hoch";
     		break;
     	case R.id.bewa_rbutton_ARM_R:
     		MoveType = 2;	// ARM
     		ToastStr = "r. Arm hoch";
     		break;
     	default:
     		MoveType = 3;	// HEAD
     		ToastStr = "Kopf vor";
     		break;
     	}
 		NetworkModule.Move(MoveType, NetworkModule.MOVE_UP);
     	Toast toast = Toast.makeText(BewegungActivity.this, ToastStr, Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
     	toast.show();
     }
     
     /**
 	 * Functionality for the Arrow-Left-Button
 	 * 
 	 * @param view		ignored, ID of the button, which trigger this function
 	 *  
 	 * Depending on what is chosen by the radiobuttons
 	 * the function makes the Networkmodule send a command 
 	 * to the robot to turn the head left, move one arm left or makes the feet turn the robot left
 	 */
     public void bew_button2_event(View view) {
     	int MoveType;
     	String ToastStr;
     	
     	switch(bewegungsart)
     	{
     	case R.id.bewa_rbutton_LAUFEN:
     		MoveType = 0;	// MOVE
     		ToastStr = "links";
     		break;
     	case R.id.bewa_rbutton_ARM_L:
     		MoveType = 1;	// ARM
     		ToastStr = "l. Arm links";
     		break;
     	case R.id.bewa_rbutton_ARM_R:
     		MoveType = 2;	// ARM
     		ToastStr = "r. Arm links";
     		break;
     	default:
     		MoveType = 3;	// HEAD
     		ToastStr = "Kopf links";
     		break;
     	}
 		NetworkModule.Move(MoveType, NetworkModule.MOVE_LEFT);
     	Toast toast = Toast.makeText(BewegungActivity.this, ToastStr, Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
     	toast.show();
     }
     
     /**
 	 * Functionality for the Arrow-Right-Button
 	 * 
 	 * @param view		ignored, ID of the button, which trigger this function
 	 *  
 	 * Depending on what is chosen by the radiobuttons
 	 * the function makes the Networkmodule send a command 
 	 * to the robot to turn the head right, move one arm right or makes the feet turn the robot right
 	 */
     public void bew_button3_event(View view) {
     	int MoveType;
     	String ToastStr;
     	
     	switch(bewegungsart)
     	{
     	case R.id.bewa_rbutton_LAUFEN:
     		MoveType = 0;	// MOVE
     		ToastStr = "rechts";
     		break;
     	case R.id.bewa_rbutton_ARM_L:
     		MoveType = 1;	// ARM
     		ToastStr = "l. Arm rechts";
     		break;
     	case R.id.bewa_rbutton_ARM_R:
     		MoveType = 2;	// ARM
     		ToastStr = "r. Arm rechts";
     		break;
     	default:
     		MoveType = 3;	// HEAD
     		ToastStr = "Kopf rechts";
     		break;
     	}
 		NetworkModule.Move(MoveType, NetworkModule.MOVE_RIGHT);
     	Toast toast = Toast.makeText(BewegungActivity.this, ToastStr, Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
     	toast.show();
     }
     
     /**
 	 * Functionality for the Arrow-Down-Button
 	 * 
 	 * @param view		ignored, ID of the button, which trigger this function
 	 *  
 	 * Depending on what is chosen by the radiobuttons
 	 * the function makes the Networkmodule send a command 
 	 * to the robot to lower the head, lower one of the arms or to walk backward
 	 */
     public void bew_button4_event(View view) {
     	int MoveType;
     	String ToastStr;
     	
     	switch(bewegungsart)
     	{
     	case R.id.bewa_rbutton_LAUFEN:
     		MoveType = 0;	// MOVE
     		ToastStr = "rückwärts";
     		break;
     	case R.id.bewa_rbutton_ARM_L:
     		MoveType = 1;	// ARM
     		ToastStr = "l. Arm runter";
     		break;
     	case R.id.bewa_rbutton_ARM_R:
     		MoveType = 2;	// ARM
     		ToastStr = "r. Arm runter";
     		break;
     	default:
     		MoveType = 3;	// HEAD
     		ToastStr = "Kopf hinter";
     		break;
     	}
 		NetworkModule.Move(MoveType, NetworkModule.MOVE_DOWN);
     	Toast toast = Toast.makeText(BewegungActivity.this, ToastStr, Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
     	toast.show();
     }
     
     /**
 	 * Functionality for the STOP-Button
 	 * 
 	 * @param view		ignored, ID of the button, which trigger this function
 	 *  
 	 * By pressing this Button a STOP signal is sended and cancels every action of the robot
 	 */
     public void bew_button5_event(View view) {
     	
     	NetworkModule.Stop();
     	Toast toast = Toast.makeText(BewegungActivity.this, "STOP", Toast.LENGTH_SHORT);
     	toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
     	toast.show();
     }
     
     
     /************ Dynamisches Aenderugszeug *****************
      ********************************************************/
     
     /**
      * Sets the Battery Icon in the ActionBar.
      * 
      * @param pic	Drawable of the Battery Icon
      */
     public void setActBarBatteryIcon(Drawable pic){
     	if(pic!=null)
     		BatteryIcon.setIcon(pic);
     }
     
     /**
      * Refreshes the ActionBar's network state icon.
      */
     public void setActBarConnectIcon(){
     	if(NetworkModule.IsConnected()==NetworkModule.CONN_CLOSED)
     	{
     		ConnectIcon.setIcon(R.drawable.network_disconnected);
     	}
     	else
     	{
     		ConnectIcon.setIcon(R.drawable.network_connected);
     	}
     }
 
 }
