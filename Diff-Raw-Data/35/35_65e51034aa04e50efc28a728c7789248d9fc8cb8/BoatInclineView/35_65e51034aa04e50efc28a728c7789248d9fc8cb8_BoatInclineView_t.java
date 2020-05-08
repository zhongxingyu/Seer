 package com.rullelinjeapp.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.rullelinjeapp.R;
 import com.rullelinjeapp.R.id;
 import com.rullelinjeapp.customviews.DrawingBoatLines;
 
 public class BoatInclineView extends Activity {
 	final static private String TAG = "##### BoatInclineView";
 
 	public void logm(String line) {
 		Log.i(TAG, line);
 	}
 
 	final static String basePath = Environment.getExternalStorageDirectory()
 			.toString() + "/KrengeApp";
 	final static int ID_MENU_SAVE_CANVAS = 1;
 	final static int KRENGE_VINKEL_RESULT = 2;
 	final static String photoPath = basePath + "/temp_photo.jpg";
 
 	DrawingBoatLines angleLineView;
 	ArrayList<ImageButton> angleButtons;
 	private HashSet<Uri> filePaths = new HashSet<Uri>(4);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_result);
 		File baseFile = new File(basePath);
 		if (!baseFile.exists()) {
 			try {
 				baseFile.mkdirs();
 				baseFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			baseFile.mkdirs();
 		}
 
 		startCamera();
 
 		setUpButtons();
 		angleLineView = (DrawingBoatLines) findViewById(id.draw_boat_lines);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 	}
 
 	private void saveAllAngleLines() {
 		for (int i = 0; i < angleLineView.angles.size(); i++) {
 			saveCanvas(i);
 		}
 		Toast.makeText(getApplicationContext(),
 				"Bildene er lagret i SD-kortet.", Toast.LENGTH_LONG).show();
 	}
 
 	private void saveCanvas(int boatNumber) {
 		FileOutputStream fileOutPut = null;
 		File file = new File(basePath + File.separator + "BaatNummer"
 				+ (boatNumber + 1) + ".jpg");
 		filePaths.add(Uri.fromFile(file));
 		try {
 			if (!file.exists()) {
 				file.createNewFile();
 			}
 			fileOutPut = new FileOutputStream(file);
 			Bitmap bitmap = Bitmap.createBitmap(angleLineView.getWidth(),
 					angleLineView.getHeight(), Bitmap.Config.ARGB_8888);
 			Canvas canvas = new Canvas(bitmap);
 			canvas.drawColor(Color.WHITE);
 			angleLineView.drawR(canvas, boatNumber);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutPut);
 			logm("Lagret Canvas");
 			fileOutPut.flush();
 			fileOutPut.close();
 			fileOutPut = null;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			logm("Klarte ikke lagre canvas");
 		} catch (IOException e) {
 			logm("Klarte ikke lage fil");
 			e.printStackTrace();
 		}
 	}
 
 	protected void sendResultEmail() {
 		for (int i = 0; i < angleLineView.angles.size(); i++) {
 			saveCanvas(i);
 		}
 		try {
 			Intent emailIntent = new Intent(
 					android.content.Intent.ACTION_SEND_MULTIPLE);
 
 			String address = "";
 			String subject = "Resultater fra KrengeAppen";
 			String emailtext = "Resultatene er vedlagt som bilder.";
 
 			emailIntent.setType("plain/text");
 			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
 					new String[] { address });
 			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailtext);
 
 			ArrayList<Uri> uris = new ArrayList<Uri>(filePaths);
 			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
 
 			this.startActivity(Intent
 					.createChooser(emailIntent, "Send mail..."));
 
 		} catch (Throwable t) {
 			Toast.makeText(this, "Kunne dessverre ikke sende mail. Beklager!",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void startCamera() {
 		Intent CameraIntent = new Intent(this, CameraActivity.class);
 		this.startActivityForResult(CameraIntent, KRENGE_VINKEL_RESULT);
 	}
 
 	private void setUpButtons() {
 		angleButtons = new ArrayList<ImageButton>();
 		angleButtons.add((ImageButton) findViewById(id.boat1));
 		angleButtons.add((ImageButton) findViewById(id.boat2));
 		angleButtons.add((ImageButton) findViewById(id.boat3));
 		angleButtons.add((ImageButton) findViewById(id.boat4));
 		for (int i = 0; i < angleButtons.size(); i++) {
 			final int index = i;
 			angleButtons.get(i).setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					angleLineView.setSelectedIndex(index);
 				}
 			});
 		}
 		((Button) findViewById(R.id.finnKrengeVinkel_Result))
 				.setOnClickListener(finnKrengevinkelButtonListener);
 		((Button) findViewById(R.id.save_image_Result))
 				.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						saveAllAngleLines();
 					}
 				});
 		((Button) findViewById(R.id.send_image_Result))
 				.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						sendResultEmail();
 					}
 				});
 
 	}
 
 	private OnClickListener finnKrengevinkelButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			startCamera();
 		}
 	};
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_OK
 				&& requestCode == KRENGE_VINKEL_RESULT) {
 			addAngle(data.getDoubleExtra("angle", 0));
 
 		}
 	}
 
 	private void addAngle(Double angle) {
 		if (angle == 999) {
 			Toast.makeText(getApplicationContext(),
 					"Fant ikke vinkel. Pr�v p� nytt.", Toast.LENGTH_LONG)
 					.show();
 			// angle = Math.random()*(Math.PI/2);
 		}
 		int angleIndex = angleLineView.setAngle(angle);
 		if (angleIndex > 2) {
			Button finnKrengeVinkelButton = ((Button) findViewById(id.finnKrengeVinkel_Result));
			finnKrengeVinkelButton.setEnabled(false);
			float alpha = 0.60f;
			AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
			alphaUp.setFillAfter(true);
			finnKrengeVinkelButton.startAnimation(alphaUp);

 		}
 		angleButtons.get(angleIndex).setVisibility(0);
 
 	}
 
 	// andoird menu
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, ID_MENU_SAVE_CANVAS, Menu.NONE,
 				R.string.menu_save_all_lines);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == ID_MENU_SAVE_CANVAS) {
 			saveAllAngleLines();
 			return true;
 		}
 		return false;
 	}
 
 }
