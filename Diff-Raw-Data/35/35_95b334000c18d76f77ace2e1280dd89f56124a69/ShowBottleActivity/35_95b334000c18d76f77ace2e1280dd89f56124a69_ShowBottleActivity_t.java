 package org.catalog.android.client;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.catalog.model.Bottle;
 import org.catalog.model.BottleImage;
 import org.catalog.web.WebAppConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ShowBottleActivity extends Activity {
 
 	Bottle bottle;
 	BottleImage bottleImage;
 	int bottleId = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.show_bottle_layout);
 
 		final TextView txtViewAge = (TextView) findViewById(R.id.txtViewAge);
 		final TextView txtViewAlcohol = (TextView) findViewById(R.id.txtViewAlcohol);
 		final TextView txtViewAlcoholType = (TextView) findViewById(R.id.txtViewAlcoholType);
 		final TextView txtViewCity = (TextView) findViewById(R.id.txtViewCity);
 		final TextView txtViewColour = (TextView) findViewById(R.id.txtViewColour);
 		final TextView txtViewContent = (TextView) findViewById(R.id.txtViewContent);
 		final TextView txtViewContinent = (TextView) findViewById(R.id.txtViewContinent);
 		final TextView txtViewCountry = (TextView) findViewById(R.id.txtViewCountry);
 		final TextView txtViewId = (TextView) findViewById(R.id.txtViewId);
 		final TextView txtViewManufacturer = (TextView) findViewById(R.id.txtViewManufacturer);
 		final TextView txtViewMaterial = (TextView) findViewById(R.id.txtViewMaterial);
 		final TextView txtViewName = (TextView) findViewById(R.id.txtViewName);
 		final TextView txtViewNote = (TextView) findViewById(R.id.txtViewNote);
 		final TextView txtViewShape = (TextView) findViewById(R.id.txtViewShape);
 		final TextView txtViewShell = (TextView) findViewById(R.id.txtViewShell);
 		final ImageView imgShowBottleImage = (ImageView) findViewById(R.id.imgShowBottleImage);
 		Bundle extras = getIntent().getExtras();
 		final int bottleId = extras.getInt(getString(R.string.bottleid));
 
 		new Thread(new Runnable() {
 			ProgressDialog dialog = new ProgressDialog(ShowBottleActivity.this);
 
 			@Override
 			public void run() {
 				// Start the indeterminate dialog.
 				ShowBottleActivity.this.runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						dialog.setCancelable(false);
 						dialog.setIndeterminate(true);
 						dialog.setTitle(getString(R.string.please_wait));
 						dialog.setMessage(getString(R.string.getting_bottle_info));
 						dialog.show();
 					}
 				});
 
 				// Getting the bottle to the bottlewebapp.apphb.com.
 				try {
 					bottle = WebAppConnection.recieveBottle(new URI(
 							getString(R.string.url_get_bottle) + bottleId));
 					bottleImage = WebAppConnection.recieveBottleImage(bottleId, new URI(
 							getString(R.string.url_get_bottle_image) + bottleId));
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 
 				// Closing the indeterminate dialog.
 				// Setting the UI.
 				ShowBottleActivity.this.runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						if (dialog.isShowing()) {
 							dialog.cancel();
 						}
 						if (bottle != null) {
 							txtViewAge.setText(String.valueOf(bottle.getAge()));
 							txtViewAlcohol.setText(bottle.getAlcohol());
 							txtViewAlcoholType.setText(bottle.getAlcoholType());
 							txtViewCity.setText(bottle.getCity());
 							txtViewColour.setText(bottle.getColor());
 							txtViewContent.setText(bottle.getContent());
 							txtViewContinent.setText(bottle.getContinent());
 							txtViewCountry.setText(bottle.getCountry());
 							txtViewId.setText(String.valueOf(bottle.getID()));
 							txtViewManufacturer.setText(bottle.getManufacturer());
 							txtViewMaterial.setText(bottle.getMaterial());
 							txtViewName.setText(bottle.getName());
 							txtViewNote.setText(bottle.getNote());
 							txtViewShape.setText(bottle.getShape());
 							txtViewShell.setText(bottle.getShell());
 						} else {
 							AlertDialog errorDialog = new AlertDialog.Builder(
 									ShowBottleActivity.this).create();
 							errorDialog.setTitle(getString(R.string.error));
 							errorDialog.setMessage(getString(R.string.no_bottle));
 							errorDialog.setButton(getString(R.string.ok),
 									new DialogInterface.OnClickListener() {
 
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											finish();
 										}
 									});
 							errorDialog.show();
 						}
 						if (bottleImage != null) {
 							imgShowBottleImage.setImageBitmap(bottleImage.getImage());
 						}
 					}
 				});
 			}
 		}).start();
 	}
 }
