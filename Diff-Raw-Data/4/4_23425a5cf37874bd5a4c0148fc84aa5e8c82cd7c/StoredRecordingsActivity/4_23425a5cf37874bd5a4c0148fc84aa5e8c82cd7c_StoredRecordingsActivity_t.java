 package ceu.marten.ui;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import ceu.marten.bplux.R;
 import ceu.marten.model.Recording;
 import ceu.marten.model.io.DatabaseHelper;
 import ceu.marten.ui.adapters.StoredRecordingsListAdapter;
 
 import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
 import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.stmt.QueryBuilder;
 
 public class StoredRecordingsActivity extends
 		OrmLiteBaseActivity<DatabaseHelper> implements OnDismissCallback {
 
 	private static final String TAG = StoredRecordingsActivity.class.getName();
 	
 	private ListView lvRecordings;
 	String recordingName;
 	private StoredRecordingsListAdapter baseAdapter;
 	private ArrayList<Recording> recordingsArrayList = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_stored_recordings);
 		loadRecordings();
 		setupRecordingListView();
 	}
 
 
 	private void setupRecordingListView() {
 		
 		final OnItemClickListener shortPressListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> AdapterView, View v,
 					int position, long id) {
 				recordingName = ((TextView) v.findViewById(R.id.dli_name)).getText().toString();
 				sendDataViaEmail();
 			}
 		};
 		lvRecordings = (ListView) findViewById(R.id.lvSessions);
 		lvRecordings.setOnItemClickListener(shortPressListener);
 		baseAdapter = new StoredRecordingsListAdapter(this, recordingsArrayList);
 		setSwipeToDismissAdapter();
 
 	}
 	public void sendDataViaEmail() {
 
 		File root = Environment.getExternalStorageDirectory();
 		File F = new File(root+"/"+recordingName + ".zip");
 		Uri U = Uri.fromFile(F);
 		Intent i = new Intent(Intent.ACTION_SEND);
 		i.setType("application/zip");
 		i.putExtra(Intent.EXTRA_STREAM, U);
 		startActivity(Intent.createChooser(i, "select email client"));
 		
 	}
 	
 	private void setSwipeToDismissAdapter() {
 		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
 				this);
 		swipeAdapter.setAbsListView(lvRecordings);
 		lvRecordings.setAdapter(baseAdapter);
 	}
 
 	@Override
 	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
 		for (int position : reverseSortedPositions) {
 			baseAdapter.remove(position);
 			Dao<Recording, Integer> dao = null;
 
 			try {
 				dao = getHelper().getRecordingDao();
 				dao.delete(recordingsArrayList.get(position));
 			} catch (SQLException e) {
 				Log.e(TAG, "Exception removing recording from database ",e);
 			}
 			recordingsArrayList.remove(position);
			deleteFile(recordingName + ".zip");
 		}
 		Toast.makeText(this, "recording removed ", Toast.LENGTH_SHORT).show();
 	}
 
 	public void loadRecordings() {
 		Dao<Recording, Integer> dao;
 		recordingsArrayList = new ArrayList<Recording>();
 		try {
 			dao = getHelper().getRecordingDao();
 			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
 			builder.orderBy("startDate", false).limit(30L);
 			recordingsArrayList = (ArrayList<Recording>) dao.query(builder.prepare());
 		} catch (SQLException e) {
 			Log.e(TAG, "exception loading recordings from database ",e);
 		}
 	}
 	
 
 }
