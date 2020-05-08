 package edu.upenn.cis350;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * The landing page after a user logs into the app. Allows the user to choose a
  * game to play, and shows the user progress in terms of points.
  * 
  * Also allows the user to logout of the app.
  * 
  * @author Sriram Radhakrishnan
  * 
  */
 public class HomeActivity extends Activity {
 
 	/* Dialog Codes */
 	private final static int LOGOUT = 1;
 
 	private String username;
 	private TextView scoreField;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.home);
 
 		username = getIntent().getStringExtra(Constants.UNEXTRA);
 
 		TextView name = (TextView) findViewById(R.id.homeName);
 		scoreField = (TextView) findViewById(R.id.homeScore);
 
 		// Shouldn't happen
 		if (name == null || scoreField == null) {
 			return;
 		}
 
 		name.setText("Welcome: " + IOBasic.fullName(username));
 		setScore(IOBasic.getPoints(username));
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setScore(IOBasic.getPoints(username));
 	}
 
	@Override
	public void onBackPressed() {
		createDialog(LOGOUT);
	}

 	private void setScore(int score) {
 		String message = "Score: " + score + " Point";
 		if (score != 1)
 			message = message + "s";
 
 		scoreField.setText(message);
 	}
 
 	private void createDialog(int id) {
 
 		if (id == LOGOUT) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.logoutMessage);
 			builder.setPositiveButton(R.string.yes,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							setResult(Constants.LOGOUT_SUCCESSFUL);
 							finish();
 						}
 					});
 			builder.setNegativeButton(R.string.no,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 			builder.create().show();
 		}
 
 	}
 
 	private void startGame(Class c) {
 		Intent i = new Intent(this, c);
 		i.putExtra(Constants.UNEXTRA, username);
 		startActivity(i);
 	}
 
 	public void onTapGame1(View view) {
 		startGame(CalorieCounterActivity.class);
 	}
 
 	public void onTapGame2(View view) {
 		startGame(RankingGameActivity.class);
 	}
 
 	public void onTapGame3(View view) {
 		startGame(OneRightPriceActivity.class);
 	}
 
 	public void onTapLogout(View view) {
 		createDialog(LOGOUT);
 	}
 
 }
