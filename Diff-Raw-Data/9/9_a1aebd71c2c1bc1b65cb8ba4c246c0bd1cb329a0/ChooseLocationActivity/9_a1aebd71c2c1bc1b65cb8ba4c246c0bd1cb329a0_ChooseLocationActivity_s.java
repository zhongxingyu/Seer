 package se.chalmers.project14.main;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 
 public class ChooseLocationActivity extends Activity {
 
 	public final static String EXTRA_MESSAGE = "se.chalmers.project14.main.EXTRA_MESSAGE";
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_choose_location);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_choose_location, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void searchLocationButton(View view) {
		Intent intent = new Intent(this, MainActivity.class);
 		EditText editText = (EditText) findViewById(R.id.search_locationText);
 		String location = editText.getText().toString(); // I have Edithuset == 
     	intent.putExtra(EXTRA_MESSAGE, location);
     	startActivity(intent);
 	}
 
 }
