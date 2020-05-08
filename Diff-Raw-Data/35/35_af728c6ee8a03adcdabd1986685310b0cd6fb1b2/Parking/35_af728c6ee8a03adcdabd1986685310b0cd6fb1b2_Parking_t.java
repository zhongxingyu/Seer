 package com.uncc.gameday.activities;
 
 import android.app.FragmentManager;
 import android.os.Bundle;
 import android.view.View;
 
 import com.uncc.gameday.R;
 import com.uncc.gameday.activities.parking.LotViewFragment;
 import com.uncc.gameday.parking.ParkingChoice;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Parking.
  */
 public class Parking extends MenuActivity {
 	
 	LotViewFragment f;
 
 	/* (non-Javadoc)
 	 * @see com.uncc.gameday.activities.MenuActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_parking);
 	}
 	
 	/**
 	 * On clicking a parking lot button, show that fragment
 	 *
 	 * @param v the v
 	 */
	public void onButtonClick(View v) {
 		// Set up some initial fragment boilerplate
 		FragmentManager fm = this.getFragmentManager();
 		LotViewFragment f = new LotViewFragment();
 		Bundle fBundle = new Bundle();
 		
 		// Get the actual parking lot we will load (store as a String)
 		switch (v.getId()) {
			case R.id.buttonBlack:
 				fBundle.putCharArray("CHOICE", ParkingChoice.BLACK.getValue().toCharArray());
 				break;
			case R.id.buttonBlue:
 				fBundle.putCharArray("CHOICE", ParkingChoice.BLUE.getValue().toCharArray());
 				break;
			case R.id.buttonGold:
 				fBundle.putCharArray("CHOICE", ParkingChoice.GOLD.getValue().toCharArray());
 				break;
			case R.id.buttonGreen:
 				fBundle.putCharArray("CHOICE", ParkingChoice.GREEN.getValue().toCharArray());
 				break;
			case R.id.buttonOrange:
 				fBundle.putCharArray("CHOICE", ParkingChoice.ORANGE.getValue().toCharArray());
 				break;
			case R.id.buttonPink:
 				fBundle.putCharArray("CHOICE", ParkingChoice.PINK.getValue().toCharArray());
 				break;
			case R.id.buttonPurple:
 				fBundle.putCharArray("CHOICE", ParkingChoice.PURPLE.getValue().toCharArray());
 				break;
			case R.id.buttonRed:
 				fBundle.putCharArray("CHOICE", ParkingChoice.RED.getValue().toCharArray());
 				break;
			case R.id.buttonSilver:
 				fBundle.putCharArray("CHOICE", ParkingChoice.SILVER.getValue().toCharArray());
 				break;
			case R.id.buttonWhite:
 				fBundle.putCharArray("CHOICE", ParkingChoice.WHITE.getValue().toCharArray());
 				break;
			case R.id.buttonYellow:
 				fBundle.putCharArray("CHOICE", ParkingChoice.YELLOW.getValue().toCharArray());
 				break;
 		}
 		
 		// Start up the fragment with the information
 		// Note the setArguments() and getArguments() function
 		f.setArguments(fBundle);
 		f.show(fm, "lot_view");
 		this.f = f;
 	}
 	
 }
