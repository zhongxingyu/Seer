 package ws.wiklund.guides.util;
 
 import java.io.IOException;
 
 import ws.wiklund.guides.R;
 import ws.wiklund.guides.bolaget.SystembolagetParser;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.guides.model.Beverage;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class DownloadBeverageTask extends AsyncTask<String, Void, Beverage> {
 	private BeverageDatabaseHelper helper;
 	private Boolean useSubTasks;
 	private Activity activity;
 	
 	@SuppressWarnings("rawtypes")
 	private Class activityClass;
 
 	private String no;
 	private String errorMsg;
 	
 	private long startRead = 0;
 	
 	private ProgressDialog dialog;
 
 	@SuppressWarnings("rawtypes")
 	public DownloadBeverageTask(BeverageDatabaseHelper helper, Boolean useSubTasks, Activity activity, Class activityClass) {
 		this.helper = helper;
 		this.useSubTasks = useSubTasks;
 		this.activity = activity;
 		this.activityClass = activityClass;
 	}
 
 	@Override
 	protected Beverage doInBackground(String... no) {
 		this.no = no[0];
		
		publishProgress();
		
 		try {
 			if(this.no == null) {
 	        	Log.w(DownloadBeverageTask.class.getName(), "Failed to get info for wine,  no is null");		        	
 	        	errorMsg = activity.getString(R.string.genericParseError);
 			} else {
 				startRead = System.currentTimeMillis();
 				return SystembolagetParser.parseResponse(this.no, helper, useSubTasks);
 			}
 		} catch (IOException e) {
         	Log.w(DownloadBeverageTask.class.getName(), "Failed to get info for wine with no: " + this.no + " after " + (System.currentTimeMillis() - startRead + " milli secs"), e);
         	errorMsg = activity.getString(R.string.genericParseError);
 		}
 
         return null;
 	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		dialog.setMessage(String.format(activity.getString(R.string.systembolaget_wait_msg), new Object[]{this.no}));
		super.onProgressUpdate(values);
	}
 
 	@Override
 	protected void onPostExecute(Beverage beverage) {
 		Intent intent = new Intent(activity, activityClass);
 
 		if (beverage != null) {
 			Log.d(DownloadBeverageTask.class.getName(), "Got beverage after " + (System.currentTimeMillis() - startRead) + " milli secs");
 			
 			intent.putExtra("ws.wiklund.guides.model.Beverage", beverage);
 			activity.startActivity(intent);
 		} else {
 			Toast.makeText(activity, errorMsg == null ? String.format(activity.getString(R.string.missingNoError), this.no) : errorMsg, Toast.LENGTH_SHORT).show();
 			errorMsg = null;
 		}
 		
 		dialog.dismiss();
 		super.onPostExecute(beverage);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		dialog = new ProgressDialog(activity, R.style.CustomDialog);
 		dialog.setTitle(activity.getString(R.string.wait));
 		dialog.setIndeterminate(true);
 		dialog.setCancelable(false);
 		dialog.show();
 
 		super.onPreExecute();
 	}
 
 }
