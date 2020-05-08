 package ru.redsolution.rosyama;
 
 import java.io.File;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 public class Main extends Activity implements OnClickListener {
 	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";
 
 	private static final int OPTION_MENU_PREFERENCE_ID = 1;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
 
 	private Uri requestedUri;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		findViewById(R.id.photo).setOnClickListener(this);
 		findViewById(R.id.send).setOnClickListener(this);
 		findViewById(R.id.get).setOnClickListener(this);
 		requestedUri = null;
 		if (savedInstanceState != null) {
 			String stringUri = savedInstanceState
 					.getString(SAVED_REQUESTED_URI);
 			if (stringUri != null)
 				requestedUri = Uri.parse(stringUri);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (!((Rosyama) getApplication()).hasLogin()) {
 			Intent intent = new Intent(this, Login.class);
 			startActivity(intent);
 			finish();
 			return;
 		}
 		enables();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(SAVED_REQUESTED_URI, requestedUri == null ? null
 				: requestedUri.toString());
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
 			if (resultCode != RESULT_OK)
 				requestedUri = null;
 			else
 				((Rosyama) getApplication()).setPhoto(requestedUri.getPath());
 			enables();
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, OPTION_MENU_PREFERENCE_ID, 0,
 				getString(R.string.preferences)).setIcon(
 				android.R.drawable.ic_menu_preferences);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case OPTION_MENU_PREFERENCE_ID:
 			Intent intent = new Intent(this, Login.class);
 			startActivity(intent);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		switch (v.getId()) {
 		case R.id.photo:
 			requestedUri = getNextUri(".jpg");
 			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
 			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 			break;
 		case R.id.send:
 			intent = new Intent(this, Address.class);
 			startActivity(intent);
 			break;
 		case R.id.get:
 			if (((Rosyama) getApplication()).pdf()) {
 				intent = new Intent(android.content.Intent.ACTION_SEND);
 				intent.setType("text/plain");
				Uri uri = Uri.fromFile(new File(((Rosyama) getApplication())
						.getPdf()));
				System.out.println(uri.getPath());
 				System.out.println(uri);
 				intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
 				startActivity(Intent.createChooser(intent,
 						getString(R.string.email)));
 			} else {
 				Toast.makeText(this, getString(R.string.pdf_fail),
 						Toast.LENGTH_LONG).show();
 			}
 			break;
 		}
 	}
 
 	private Uri getNextUri(String extention) {
 		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
 				Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention));
 	}
 
 	private void enables() {
 		findViewById(R.id.send).setEnabled(
 				((Rosyama) getApplication()).hasPhoto());
 		findViewById(R.id.get).setEnabled(((Rosyama) getApplication()).hasId());
 	}
 }
