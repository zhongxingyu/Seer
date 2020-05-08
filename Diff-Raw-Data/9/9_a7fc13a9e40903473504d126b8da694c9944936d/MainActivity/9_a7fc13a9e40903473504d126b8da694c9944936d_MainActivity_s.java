 package com.flyingh.viewimage;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity {
 	private static final String TAG = "MainActivity";
 	private EditText editText;
 	private ImageView imageView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		editText = (EditText) findViewById(R.id.site);
 		imageView = (ImageView) findViewById(R.id.imageView);
 		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
 			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
 		}
 	}
 
 	public void show(View view) {
 		try {
 			InputStream is = new URL(editText.getText().toString()).openStream();
 			imageView.setImageBitmap(BitmapFactory.decodeStream(is));
 		} catch (Exception e) {
 			Log.i(TAG, e.toString());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
