 package com.instrument.shakemusic;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/*
 	 * onClick Guitar
 	 */
 
 	public void onClickGuitar(View v) {
 		Intent guitar = new Intent(MainActivity.this, ComposeActivity.class);
 		guitar.putExtra(Instrument.INSTRUMENT, Instrument.GUITAR);
 		startActivity(guitar);
 	}
 
 	/*
 	 * onClick Play
 	 */
 
 	public void onClickPlay(View v) {
 		Intent play = new Intent(MainActivity.this, PlayActivity.class);
 		startActivity(play);
 	}
 
 	/*
 	 * onClick Piano
 	 */
 
 	public void onClickPiano(View v) {
 		Intent piano = new Intent(MainActivity.this, ComposeActivity.class);
 		piano.putExtra(Instrument.INSTRUMENT, Instrument.PIANO);
 		startActivity(piano);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case R.id.language:
			Intent lIntent = new Intent("com.instrument.shakemusic.LANG");
 			startActivity(lIntent);
 			break;
 
 		case R.id.instructions:
			Intent i = new Intent("com.instrument.shakemusic.INSTRUCTION");
 			startActivity(i);
 			break;
 
 		case R.id.exit:
 			this.finish();
 			Intent intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 			break;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onBackPressed() {
 		new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle("Closing Application")
 				.setMessage("Are you sure you want to close this application?")
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								finish();
 							}
 
 						}).setNegativeButton("No", null).show();
 	}
 }
