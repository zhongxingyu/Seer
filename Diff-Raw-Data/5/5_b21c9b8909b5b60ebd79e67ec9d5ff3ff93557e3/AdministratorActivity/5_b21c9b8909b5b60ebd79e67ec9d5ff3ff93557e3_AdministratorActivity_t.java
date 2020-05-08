 package nz.ac.otago.linguistics.spre;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Launch activity. From here the experiment administrator can start an
  * experiment session or export the results.
  * 
  * @author Tonic Artos
  */
 public class AdministratorActivity extends Activity  {
 	private View.OnClickListener runExperimentClickListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 	        Intent intent = new Intent(getApplicationContext(), ExperimentActivity.class);
 			startActivity(intent);
 		}
 	};
 	private View.OnClickListener exportDataClickListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 				exportData();
 			} else {
 				Toast.makeText(getApplicationContext(), "External storage is unavailable at the moment. Please try again later.", Toast.LENGTH_LONG);
 			}
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		((Button) findViewById(R.id.button_run_experiment)).setOnClickListener(runExperimentClickListener);
 		((Button) findViewById(R.id.button_export_data)).setOnClickListener(exportDataClickListener);
 		updateRecordCountDisplay();
 	}
 
 	private void updateRecordCountDisplay() {
 		DatabaseHelper db = new DatabaseHelper(this);
 		Cursor c = db.getReadableDatabase().query(ExperimentData.TABLE, new String[] {ExperimentData.KEY_ROWID}, null, null, null, null, null);
 		if (c.getCount() == 0) {
 			findViewById(R.id.text_num_records).setVisibility(View.INVISIBLE);
 		} else {
 			((TextView) findViewById(R.id.text_num_records)).setText(c.getCount() + " Records");
 		}
 		c.close();
 		db.close();
 	}
 
 	protected void exportData() {
 		DatabaseHelper db = new DatabaseHelper(getApplicationContext());
 		Cursor c = db.getReadableDatabase().query(ExperimentData.TABLE, new String[] {ExperimentData.KEY_ROWID, ExperimentData.KEY_DATA}, null, null, null, null, null);
 		if (!c.moveToFirst()) {
 			//nothing to write out.
 			return;
 		}
 		File path = Environment.getExternalStoragePublicDirectory("SPRE");
 		File file = new File(path, "experimentdata.csv");
 //		file.setReadable(true, false);
 		
 		try {
 			path.mkdirs();
 			BufferedWriter out = new BufferedWriter(new FileWriter(file));
 			do {
 				out.write("\nSession ID, " + c.getInt(c.getColumnIndex(ExperimentData.KEY_ROWID)) + "\n");
 				out.write(c.getString(c.getColumnIndex(ExperimentData.KEY_DATA)));
 			} while (c.moveToNext());
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			Log.w("ExternalStorage", "Error writing " + file, e);
 			Toast.makeText(this, "An error was encountered", Toast.LENGTH_LONG).show();
 		}
 		MediaScannerConnection.scanFile(getApplicationContext(), new String[] {file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
 			@Override
 			public void onScanCompleted(final String path, final Uri uri) {
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
//						Toast.makeText(getApplicationContext(), path + " " + uri, Toast.LENGTH_LONG).show();
						Toast.makeText(getApplicationContext(), "Data exported to SPRE/experimentdata.csv", Toast.LENGTH_LONG).show();
 					}
 				});
 			}
 		});
 		c.close();
 		db.close();
 		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateRecordCountDisplay();
 	}
 }
