 package com.buildbotwatcher;
 
 import java.sql.Time;
 
 import com.buildbotwatcher.worker.Build;
 import com.buildbotwatcher.worker.Builder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class BuildActivity extends Activity {
 	private Build			_build;
 	private Builder			_builder;
 	private Menu			_menu;
 
 	static final Class<?>	PARENT_ACTIVITY = BuilderActivity.class;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 		setContentView(R.layout.build);
 	
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 		
 		Bundle bundle = getIntent().getExtras();
 		_build = (Build) bundle.get("build");
 		_builder = (Builder) bundle.get("builder");
 		setupUi();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		_menu = menu;
 		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.build, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, PARENT_ACTIVITY);
 			intent.putExtra("builder", _builder);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 			
 		case R.id.menu_refresh:
 			item.setEnabled(false);
 			refresh();
 			return true;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void setupUi() {
 		setTitle(String.valueOf(_build.getNumber()));
 		TextView res = (TextView) findViewById(R.id.result);
 		if (_build.getText().containsKey("build"))
 			res.setText(_build.getText().get("build"));
 		else
 			res.setText(String.format(getResources().getString(R.string.build_failure), _build.getText().get("failed")));
 		((TextView) findViewById(R.id.builder)).setText(_builder.getName());
 		((TextView) findViewById(R.id.number)).setText(String.valueOf(_build.getNumber()));
 		((TextView) findViewById(R.id.reason)).setText(String.valueOf(_build.getReason()));
 		((TextView) findViewById(R.id.slave)).setText(_build.getSlaveName());
 		((TextView) findViewById(R.id.start)).setText(_build.getTimeStart().toLocaleString());
 		((TextView) findViewById(R.id.end)).setText(_build.getTimeEnd().toLocaleString());
 		Time duration = new Time(_build.getTimeEnd().getTime() - _build.getTimeStart().getTime());
 		((TextView) findViewById(R.id.duration)).setText(String.format("%02d:%02d", duration.getMinutes(), duration.getSeconds()));
 	}
 	
 	private void refresh() {
 		_menu.findItem(R.id.menu_refresh).setEnabled(false);
 		new RefreshBuild().execute();
 	}
 	
 	private class RefreshBuild extends AsyncTask<Void, Integer, Build> {
 		protected Build doInBackground(Void... v) {
 			return _builder.getBuild(_build.getNumber(), true);
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			// TODO
 		}
 
 		protected void onPostExecute(Build result) {
 			_build = result;
 			setupUi();
 			_menu.findItem(R.id.menu_refresh).setEnabled(true);
 		}
 	}
 }
