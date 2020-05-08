 package at.roadrunner.android.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.GridView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import at.roadrunner.android.Config;
 import at.roadrunner.android.R;
 import at.roadrunner.android.couchdb.CouchDBService;
 import at.roadrunner.android.setup.CouchDB;
 import at.roadrunner.android.setup.SystemTestCases;
 import at.roadrunner.android.setup.SystemTestCases.TestCase;
 public class SystemTest extends Activity {
 	
 	private final String RESULT = "RESULT";
 	private final String TESTCASE = "TESTCASE";
 	
 	private ArrayList<HashMap<String, String>> _testList;
 	private SimpleAdapter _adapter;
 	
 	private GridView _testCaseList;
 	private ProgressBar _progressBar;
 	private Context _context;
 	
 	@SuppressWarnings("unused")
 	private static final String TAG = "SystemTest";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_systemtest);
 		_context = this;
 		_testCaseList = (GridView) findViewById(R.id.TestCaseList);
 		_progressBar = (ProgressBar) findViewById(R.id.systemstate_progressbar);
 		
         _testList = new ArrayList<HashMap<String, String>>();
         _adapter = new SimpleAdapter(this, _testList, R.layout.row_item_systemtest,
 	                    new String[] {RESULT, TESTCASE}, new int[] {R.id.RESULT, R.id.TESTCASE});
         _testCaseList.setAdapter(_adapter);
         
         new UpdateTask().execute();
 	}
 	
 	class UpdateTask extends AsyncTask<Void, TestCase, Void>  {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			SystemTestCases testCases = new SystemTestCases(_context);
 			
 			publishProgress(testCases.localCouchDBInstalled());
 			publishProgress(testCases.localIniFileExists());
 			publishProgress(testCases.localCouchDBRunning());
 			publishProgress(testCases.localCouchDBReachable());
 			publishProgress(testCases.localAdminUserExists());
 			publishProgress(testCases.localDatabaseExists());
 			publishProgress(testCases.remoteCouchDBReachable());
 			publishProgress(testCases.localInitialReplicationExists());
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			_progressBar.setVisibility(View.INVISIBLE);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			_progressBar.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected void onProgressUpdate(TestCase... testCase) {
 			HashMap<String, String> testListItem = new HashMap<String, String>();
 			testListItem.put(RESULT, testCase[0].getResult());
 	        testListItem.put(TESTCASE, testCase[0].getTestCase());
 	        _testList.add(testListItem);
 	        
 	        _testCaseList.setAdapter(_adapter);
 		};
 	}
 	
 	private void fixUpProblems() {
 		int problemIndex = 0;
 		
 		// for all entries in the adapter
 		for (HashMap<String, String> map : _testList) {
 			if (map.get(RESULT).equals(getString(R.string.systemtest_fail) ) ) {
 				switch (problemIndex) {
 				case 0:
 					downloadCouchDB();
 					break;
 				case 1:
 					installCouchDB();
 					break;
 				case 2:
 				case 3:
 					runCouchDB();
					break;
 				case 4:
 					insertAdminUser();
					break;
 				case 5:
 					createDatabase();
					break;
 				case 6:
 					remoteDB();
 					break;
 				case 7:
 					replicateInitial();
 				}
 				refreshGUI();
 				break;
 			}
 			
 			problemIndex++;
 		}
 	}
 
 	private void downloadCouchDB() {
 		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 		alertBuilder.setMessage(R.string.roadrunner_dialog_missingCouchDB);
 		alertBuilder.setCancelable(false);
 		alertBuilder.setPositiveButton(R.string.app_dialog_yes, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Config.COUCHDB_PACKAGE));
 				startActivity(intent);
 			}
 		});
 		alertBuilder.setNegativeButton(R.string.app_dialog_no, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert = alertBuilder.create();
 		
 		alert.show();
 	}
 	
 	
 	private void installCouchDB() {
 		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 		alertBuilder.setMessage(R.string.roadrunner_dialog_firstStartCouchDB);
 		alertBuilder.setCancelable(true);
 		
 		AlertDialog alert = alertBuilder.create();
 		alert.show();
 	}
 	
 	private void runCouchDB() {
 		CouchDBService.startCouchDB(this);
 	}
 
 	private void insertAdminUser() {
 		new CouchDB().insertRoadrunnerUser();
 		CouchDBService.restartCouchDB(this);
 	}
 	
 	private void createDatabase() {
 		new CouchDB().createRoadrunnerDB();
 	}
 	
 	private void replicateInitial() {
 		new CouchDB().replicateInitialDocuments(_context);
 	}
 	
 	private void remoteDB() {
 		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 		alertBuilder.setMessage(R.string.roadrunner_dialog_checkConnection);
 		AlertDialog alert = alertBuilder.create();
 		
 		alert.show();		
 	}
 	
 	/*
 	 * refresh the GUI -> restart activity
 	 */
 	private void refreshGUI() {	
 		onCreate(null);
 	}
 	
 	/*
 	 * inflate menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.systemtest_menu, menu);
 		return true;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		
 		boolean isFixUpNeeded = false;
 		
 		// disable fixItUp if there are no problems
 		for (HashMap<String, String> map : _testList) {
 			if (map.get(RESULT).equals(getString(R.string.systemtest_fail) )) {
 				isFixUpNeeded = true;
 				break;
 			}
 		}
 		
 		MenuItem item = menu.findItem(R.id.systemtest_menu_fixitup);
 		item.setEnabled(isFixUpNeeded);
 
 		return true;
 	}
 	
 	/*
      * Event OptionsMenuItemSelected
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.systemtest_menu_refresh:
         	refreshGUI();
         	return true;
         case R.id.systemtest_menu_fixitup:
         	fixUpProblems();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
