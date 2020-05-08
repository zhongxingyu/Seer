 package net.hisme.masaki.kyoani.activities;
 
 import net.hisme.masaki.kyoani.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.widget.TextView;
 import android.content.Intent;
 
 public class MainActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
		TextView account = (TextView) findViewById(R.id.label_account);
		account.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this,
						AccountActivity.class);
				startActivity(intent);
			}
		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_account:
 			startActivity(new Intent(MainActivity.this, AccountActivity.class));
 			return true;
 		}
 		return false;
 	}
 }
