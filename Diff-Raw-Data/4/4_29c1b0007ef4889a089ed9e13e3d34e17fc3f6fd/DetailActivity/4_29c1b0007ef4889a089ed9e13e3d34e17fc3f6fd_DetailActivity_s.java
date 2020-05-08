 package com.bigpupdev.synodroid.ui;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.action.DeleteTaskAction;
 import com.bigpupdev.synodroid.action.DownloadOriginalLinkAction;
 import com.bigpupdev.synodroid.action.EnumShareAction;
 import com.bigpupdev.synodroid.action.GetFilesAction;
 import com.bigpupdev.synodroid.action.GetTaskPropertiesAction;
 import com.bigpupdev.synodroid.action.PauseTaskAction;
 import com.bigpupdev.synodroid.action.ResumeTaskAction;
 import com.bigpupdev.synodroid.action.UpdateFilesAction;
 import com.bigpupdev.synodroid.action.UpdateTaskAction;
 import com.bigpupdev.synodroid.action.UpdateTaskPropertiesAction;
 import com.bigpupdev.synodroid.adapter.Detail;
 import com.bigpupdev.synodroid.adapter.Detail2Progress;
 import com.bigpupdev.synodroid.adapter.Detail2Text;
 import com.bigpupdev.synodroid.adapter.DetailAction;
 import com.bigpupdev.synodroid.adapter.DetailProgress;
 import com.bigpupdev.synodroid.adapter.DetailText;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.OriginalFile;
 import com.bigpupdev.synodroid.data.SharedDirectory;
 import com.bigpupdev.synodroid.data.Task;
 import com.bigpupdev.synodroid.data.TaskDetail;
 import com.bigpupdev.synodroid.data.TaskFile;
 import com.bigpupdev.synodroid.data.TaskProperties;
 import com.bigpupdev.synodroid.data.TaskStatus;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.utils.ActivityHelper;
 import com.bigpupdev.synodroid.utils.EulaHelper;
 import com.bigpupdev.synodroid.utils.UIUtils;
 import com.bigpupdev.synodroid.utils.Utils;
 import com.bigpupdev.synodroid.utils.ViewPagerIndicator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class DetailActivity extends BaseActivity{
 	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
 	private static final String PREFERENCE_GENERAL = "general_cat";
 	
 	MyAdapter mAdapter;
     ViewPager mPager;
     ViewPagerIndicator mIndicator;
     Task task;
     TaskStatus status;
 	
     
     // The seeding ratio
 	private int seedingRatio;
 	// The seeding time
 	private int seedingTime;
 
 	private int ul_rate;
 	private int dl_rate;
 	private int priority;
 	private int max_peers;
 	private String destination;
 
 	private int[] priorities;
 	private String[] destinations;
 
 	// Flag to know of the user changed seeding parameters
 	private boolean seedingChanged = false;
 	// The values of seeding time
 	private int[] seedingTimes;
 	
     private static final int MENU_PAUSE = 1;
 	private static final int MENU_DELETE = 2;
 	private static final int MENU_CANCEL = 3;
 	private static final int MENU_RESUME = 4;
 	private static final int MENU_RETRY = 5;
 	private static final int MENU_CLEAR = 6;
 	private static final int MENU_PARAMETERS = 7;
 	private static final int TASK_PARAMETERS_DIALOG = 3;
 	private static final int TASK_PROPERTIES_DIALOG = 4;
 	
 	private static final int MAIN_ITEM = 0;
 	private static final int TRANSFER_ITEM = 1;
 	private static final int FILE_ITEM = 2;
 	
 	public void setStatus(TaskStatus pStatus){
 		status = pStatus;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	public void onPause() {
 		super.onPause();
 		// Try to update the details
 		updateTask(false);
 		Synodroid app = (Synodroid) getApplication();
 		app.pauseServer();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		try{
 			if (((Synodroid)getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Resuming detail activity.");
 		}
 		catch (Exception ex){/*DO NOTHING*/}
 		
 		// Check for fullscreen
 		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)) {
 			// Set fullscreen or not
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 
 		// Launch the gets task's details recurrent action
 		Synodroid app = (Synodroid) getApplication();
 		app.resumeServer();
 	}
 	
 	@Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         getActivityHelper().setupSubActivity();
     }
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 
 		if (hasFocus) {
 			SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 			if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)) {
 				// Set fullscreen or not
 				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			} else {
 				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			}
 		}
 	}
 	
 	/**
 	 * Return a sub detail list for the general's tab
 	 */
 	private List<Detail> buildTransferDetails(TaskDetail details) {
 		ArrayList<Detail> result = new ArrayList<Detail>();
 
 		// Set the result to be returned to the previous activity
 		Intent previous = new Intent();
 		previous.putExtra("com.bigpupdev.synodroid.ds.Details", details);
 		setResult(Activity.RESULT_OK, previous);
 
 		// ------------ Status
 		try {
 			result.add(new DetailText(getString(R.string.detail_status), TaskStatus.getLabel(this, details.status)));
 		} catch (NullPointerException e) {
 			result.add(new DetailText(getString(R.string.detail_status), getString(R.string.detail_unknown)));
 		} catch (IllegalArgumentException e) {
 			result.add(new DetailText(getString(R.string.detail_status), getString(R.string.detail_unknown)));
 		}
 		// ------------ Transfered
 		String transfered = getString(R.string.detail_progress_download) + " " + Utils.bytesToFileSize(details.bytesDownloaded, true, getString(R.string.detail_unknown));
 		if (details.isTorrent) {
 			String upload = getString(R.string.detail_progress_upload) + " " + Utils.bytesToFileSize(details.bytesUploaded, true, getString(R.string.detail_unknown)) + " (" + details.bytesRatio + " %)";
 			Detail2Text tr = new Detail2Text(getString(R.string.detail_transfered));
 			tr.setValue1(transfered);
 			tr.setValue2(upload);
 			result.add(tr);
 		} else {
 			result.add(new DetailText(getString(R.string.detail_transfered), transfered));
 		}
 		// ------------- Progress
 		long downloaded = details.bytesDownloaded;
 		long filesize = details.fileSize;
 		String downPerStr = getString(R.string.detail_unknown);
 		int downPer = 0;
 		if (filesize != -1) {
 			try {
 				downPer = (int) ((downloaded * 100) / filesize);
 			} catch (ArithmeticException e) {
 				downPer = 100;
 			}
 			if (downPer == 100 && downloaded != filesize){
 				downPer = 99;
 			}
 			downPerStr = "" + downPer + "%";
 		}
 
 		int upPerc = 0;
 		String upPercStr = getString(R.string.detail_unknown);
 		Integer uploadPercentage = Utils.computeUploadPercent(details);
 		if (uploadPercentage != null) {
 			upPerc = uploadPercentage.intValue();
 			upPercStr = "" + upPerc + "%";
 		}
 		// If it is a torrent
 		Detail proDetail = null;
 		if (details.isTorrent) {
 			Detail2Progress progDetail = new Detail2Progress(getString(R.string.detail_progress));
 			proDetail = progDetail;
 			progDetail.setProgress1(getString(R.string.detail_progress_download) + " " + downPerStr, downPer);
 			progDetail.setProgress2(getString(R.string.detail_progress_upload) + " " + upPercStr, upPerc);
 		} else {
 			DetailProgress progDetail = new DetailProgress(getString(R.string.detail_progress), R.layout.details_progress_template1);
 			proDetail = progDetail;
 			progDetail.setProgress(getString(R.string.detail_progress_download) + " " + downPerStr, downPer);
 		}
 		result.add(proDetail);
 		// ------------ Speed
 		String speed = getString(R.string.detail_progress_download) + " " + details.speedDownload + " KB/s";
 		if (details.isTorrent) {
 			speed = speed + " - " + getString(R.string.detail_progress_upload) + " " + details.speedUpload + " KB/s";
 		}
 		result.add(new DetailText(getString(R.string.detail_speed), speed));
 		// ------------ Peers
 		if (details.isTorrent) {
 			String peers = details.peersCurrent + " / " + details.peersTotal;
 			DetailProgress peersDetail = new DetailProgress(getString(R.string.detail_peers), R.layout.details_progress_template2);
 			int pProgress = 0;
 			if (details.peersTotal != 0) {
 				pProgress = (int) ((details.peersCurrent * 100) / details.peersTotal);
 			}
 			peersDetail.setProgress(peers, pProgress);
 			result.add(peersDetail);
 		}
 		// ------------ Seeders / Leechers
 		if (details.isTorrent) {
 			String seedStr = getString(R.string.detail_unvailable);
 			String leechStr = getString(R.string.detail_unvailable);
 			if (details.seeders != null)
 				seedStr = details.seeders.toString();
 			if (details.leechers != null)
 				leechStr = details.leechers.toString();
 			String seeders = seedStr + " - " + leechStr;
 			result.add(new DetailText(getString(R.string.detail_seeders_leechers), seeders));
 		}
 		// ------------ ETAs
 		String etaUpload = getString(R.string.detail_unknown);
 		String etaDownload = getString(R.string.detail_unknown);
 		if (details.speedDownload != 0) {
 			long sizeLeft = filesize - downloaded;
 			long timeLeft = (long) (sizeLeft / (details.speedDownload * 1000));
 			etaDownload = Utils.computeTimeLeft(timeLeft);
 		} else {
 			if (downPer == 100) {
 				etaDownload = getString(R.string.detail_finished);
 			}
 		}
 		Long timeLeftSize = null;
 		long uploaded = details.bytesUploaded;
 		double ratio = ((double) (details.seedingRatio)) / 100.0d;
 		if (details.speedUpload != 0 && details.seedingRatio != 0) {
 			long sizeLeft = (long) ((filesize * ratio) - uploaded);
 			timeLeftSize = (long) (sizeLeft / (details.speedUpload * 1000));
 		}
 		// If the user defined a minimum seeding time AND we are in seeding
 		// mode
 		TaskStatus tsk_status = details.getStatus();
 
 		Long timeLeftTime = null;
 		if (details.seedingInterval != 0 && tsk_status == TaskStatus.TASK_SEEDING) {
 			timeLeftTime = (details.seedingInterval * 60) - details.seedingElapsed;
 			if (timeLeftTime < 0) {
 				timeLeftTime = null;
 			}
 		}
 		// At least one time has been computed
 		if (timeLeftTime != null || timeLeftSize != null) {
 			// By default take the size time
 			Long time = timeLeftSize;
 			// Except if it is null
 			if (timeLeftSize == null) {
 				time = timeLeftTime;
 			} else {
 				// If time is not null
 				if (timeLeftTime != null) {
 					// Get the higher value
 					if (timeLeftTime > timeLeftSize) {
 						time = timeLeftTime;
 					}
 				}
 			}
 			etaUpload = Utils.computeTimeLeft(time);
 		} else if (upPerc == 100) {
 			etaUpload = getString(R.string.detail_finished);
 		}
 		// In case the user set the seedin time to forever
 		if (details.seedingInterval == -1) {
 			etaUpload = getString(R.string.detail_forever);
 		}
 		Detail etaDet = null;
 		// If it is a torrent then show the upload ETA
 		if (details.isTorrent) {
 			Detail2Text etaDetail = new Detail2Text(getString(R.string.detail_eta));
 			etaDet = etaDetail;
 			etaDetail.setValue1(getString(R.string.detail_progress_download) + " " + etaDownload);
 			etaDetail.setValue2(getString(R.string.detail_progress_upload) + " " + etaUpload);
 		}
 		// Otherwise only show the download ETA
 		else {
 			DetailText etaDetail = new DetailText(getString(R.string.detail_eta));
 			etaDet = etaDetail;
 			etaDetail.setValue(getString(R.string.detail_progress_download) + " " + etaDownload);
 		}
 		result.add(etaDet);
 		// ------------ Pieces
 		if (details.isTorrent) {
 			String pieces = details.piecesCurrent + " / " + details.piecesTotal;
 			DetailProgress piecesDetail = new DetailProgress(getString(R.string.detail_pieces), R.layout.details_progress_template2);
 			int piProgress = 0;
 			if (details.piecesTotal != 0) {
 				piProgress = (int) ((details.piecesCurrent * 100) / details.piecesTotal);
 			}
 			piecesDetail.setProgress(pieces, piProgress);
 			result.add(piecesDetail);
 		}
 		// Update seeding parameters
 		seedingRatio = details.seedingRatio;
 		seedingTime = details.seedingInterval;
 		return result;
 	}
 	
 	/**
 	 * Return a sub detail list for the general's tab
 	 */
 	private List<Detail> buildGeneralDetails(TaskDetail details) {
 		ArrayList<Detail> result = new ArrayList<Detail>();
 		// FileName
 		result.add(new DetailText(getString(R.string.detail_filename), details.fileName));
 		setTitle(details.fileName);
 		// Destination
 		DetailText destDetail = new DetailText(getString(R.string.detail_destination), details.destination);
 		result.add(destDetail);
 		// File size
 		result.add(new DetailText(getString(R.string.detail_filesize), Utils.bytesToFileSize(details.fileSize, true, getString(R.string.detail_unknown))));
 		// Creation time
 		result.add(new DetailText(getString(R.string.detail_creationtime), Utils.computeDate(details.creationDate)));
 		// URL
 		final String originalLink = details.url;
 		DetailText urlDetail = new DetailText(getString(R.string.detail_url), originalLink);
 		urlDetail.setAction(new DetailAction() {
 			public void execute(Detail detailsP) {
 				if ((task.isTorrent || task.isNZB)) {
 					Synodroid app = (Synodroid) getApplication();
 					task.originalLink = originalLink;
 					app.executeAsynchronousAction((DetailMain)mAdapter.getItem(MAIN_ITEM), new DownloadOriginalLinkAction(task), false);
 				}
 			}
 		});
 		result.add(urlDetail);
 		// Username
 		result.add(new DetailText(getString(R.string.detail_username), details.userName));
 		return result;
 	}
 	
 	public void updateActionBarTitle(String title){
     	ActivityHelper ah = getActivityHelper();
     	if (ah != null) ah.setActionBarTitle(title, false);
     }
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
 	 */
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		// Prepare the task's parameters dialog
 		case TASK_PARAMETERS_DIALOG:
 			final EditText seedRatio = (EditText) dialog.findViewById(R.id.seedingPercentage);
 			final Spinner seedTime = (Spinner) dialog.findViewById(R.id.seedingTime);
 			seedRatio.setText("" + seedingRatio);
 			// Try to find the right value
 			int pos = 0;
 			for (int iLoop = 0; iLoop < seedingTimes.length; iLoop++) {
 				if (seedingTimes[iLoop] == seedingTime) {
 					pos = iLoop;
 					break;
 				}
 			}
 			seedTime.setSelection(pos);
 			break;
 		case TASK_PROPERTIES_DIALOG:
 			final EditText seedRatioP = (EditText) dialog.findViewById(R.id.seedingPercentage);
 			final Spinner seedTimeP = (Spinner) dialog.findViewById(R.id.seedingTime);
 
 			final EditText ul_rateP = (EditText) dialog.findViewById(R.id.ul_rate);
 			final EditText dl_rateP = (EditText) dialog.findViewById(R.id.dl_rate);
 			final EditText max_peersP = (EditText) dialog.findViewById(R.id.max_peers);
 			final Spinner destinationP = (Spinner) dialog.findViewById(R.id.destination);
 			final Spinner priorityP = (Spinner) dialog.findViewById(R.id.priority);
 			ul_rateP.setText("" + ul_rate);
 			dl_rateP.setText("" + dl_rate);
 			max_peersP.setText("" + max_peers);
 
 			seedRatioP.setText("" + seedingRatio);
 			// Try to find the right value
 			int position = 0;
 			for (int iLoop = 0; iLoop < seedingTimes.length; iLoop++) {
 				if (seedingTimes[iLoop] == seedingTime) {
 					position = iLoop;
 					break;
 				}
 			}
 			seedTimeP.setSelection(position);
 
 			// Try to find the right value
 			position = 0;
 			for (int iLoop = 0; iLoop < priorities.length; iLoop++) {
 				if (priorities[iLoop] == priority) {
 					position = iLoop;
 					break;
 				}
 			}
 			priorityP.setSelection(position);
 
 			// Try to find the right value
 			position = 0;
 			ArrayAdapter<String> sa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, destinations);
 			sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			for (int iLoop = 0; iLoop < destinations.length; iLoop++) {
 				if (destinations[iLoop] == destination) {
 					position = iLoop;
 				}
 			}
 			destinationP.setAdapter(sa);
 			destinationP.setSelection(position);
 			break;
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		// Create the task's parameters dialog
 		case TASK_PARAMETERS_DIALOG:
 			// Create the view
 			View container = inflater.inflate(R.layout.seeding_parameters, null, false);
 			final EditText seedRatio = (EditText) container.findViewById(R.id.seedingPercentage);
 			final Spinner seedTime = (Spinner) container.findViewById(R.id.seedingTime);
 			// Create the dialog
 			builder.setTitle(getString(R.string.seeding_parameters_time));
 			builder.setView(container);
 			builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialogP, int whichP) {
 					seedingChanged = true;
 					try {
 						int seedR = Integer.parseInt(seedRatio.getText().toString());
 						seedingRatio = seedR;
 						int pos = seedTime.getSelectedItemPosition();
 						seedingTime = seedingTimes[pos];
 						// At the end, update the task.
 						updateTask(true);
 					}
 					// The ratio is not an integer
 					catch (NumberFormatException ex) {
 						// NTD: the input method does not allow to set a float or
 						// a string
 					}
 				}
 			});
 			builder.setNegativeButton(getString(R.string.button_cancel), null);
 			return builder.create();
 		case TASK_PROPERTIES_DIALOG:
 			// Create the view
 			View containerP = inflater.inflate(R.layout.task_properties, null, false);
 			final EditText seedRatioP = (EditText) containerP.findViewById(R.id.seedingPercentage);
 			final EditText ul_rateP = (EditText) containerP.findViewById(R.id.ul_rate);
 			final EditText dl_rateP = (EditText) containerP.findViewById(R.id.dl_rate);
 			final EditText max_peersP = (EditText) containerP.findViewById(R.id.max_peers);
 			final Spinner destinationP = (Spinner) containerP.findViewById(R.id.destination);
 			final Spinner priorityP = (Spinner) containerP.findViewById(R.id.priority);
 			final Spinner seedTimeP = (Spinner) containerP.findViewById(R.id.seedingTime);
 			// Create the dialog
 			builder.setTitle(getString(R.string.task_parameters));
 			builder.setView(containerP);
 			builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialogP, int whichP) {
 					seedingChanged = true;
 					try {
 						ul_rate = Integer.parseInt(ul_rateP.getText().toString());
 						dl_rate = Integer.parseInt(dl_rateP.getText().toString());
 						max_peers = Integer.parseInt(max_peersP.getText().toString());
 						destination = destinations[destinationP.getSelectedItemPosition()];
 						priority = priorities[priorityP.getSelectedItemPosition()];
 
 						int seedR = Integer.parseInt(seedRatioP.getText().toString());
 						seedingRatio = seedR;
 						int pos = seedTimeP.getSelectedItemPosition();
 						seedingTime = seedingTimes[pos];
 						// At the end, update the task.
 						updateTask(true);
 					}
 					// The ratio is not an integer
 					catch (NumberFormatException ex) {
 						// NTD: the input method does not allow to set a float or
 						// a string
 					}
 				}
 			});
 			builder.setNegativeButton(getString(R.string.button_cancel), null);
 			return builder.create();
 		default:
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void handleMessage(Message msgP) {
 		Synodroid app = (Synodroid) getApplication();
 		DetailMain main = (DetailMain) mAdapter.getItem(MAIN_ITEM);
 		DetailFiles files = (DetailFiles)mAdapter.getItem(FILE_ITEM);
 		try{
 			if (((Synodroid)getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "DetailActivity: Message received with ID = "+ msgP.what);
 		}catch (Exception ex){/*DO NOTHING*/}
 		switch (msgP.what) {
 		case ResponseHandler.MSG_DETAILS_FILES_RETRIEVED:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive file listing message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			List<TaskFile> tfile = (List<TaskFile>) msgP.obj;
 			try{
 				files.fileAdapter.updateFiles(tfile);	
 			
 			} catch (Exception e) {
 				if (app.DEBUG) Log.e(Synodroid.DS_TAG, "DetailActivity: An error occured while trying to update files list:", e);
 			}
 			break;
 		case ResponseHandler.MSG_PROPERTIES_RECEIVED:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive task properties message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			TaskProperties tp = (TaskProperties) msgP.obj;
 			ul_rate = tp.ul_rate;
 			dl_rate = tp.dl_rate;
 			max_peers = tp.max_peers;
 			priority = tp.priority;
 			seedingRatio = tp.seeding_ratio;
 			seedingTime = tp.seeding_interval;
 			destination = tp.destination;
 
 			try {
 				showDialog(TASK_PROPERTIES_DIALOG);
 			} catch (Exception e) {
 			}
 			break;
 		case ResponseHandler.MSG_SHARED_DIRECTORIES_RETRIEVED:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive share directory listing message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			List<SharedDirectory> newDirs = (List<SharedDirectory>) msgP.obj;
 			destinations = new String[newDirs.size()];
 			for (int iLoop = 0; iLoop < newDirs.size(); iLoop++) {
 				SharedDirectory sharedDir = newDirs.get(iLoop);
 				destinations[iLoop] = sharedDir.name;
 				if (sharedDir.isCurrent) {
 					destination = sharedDir.name;
 				}
 			}
 			app.executeAsynchronousAction(main, new GetTaskPropertiesAction(task), false, false);
 			break;
 		// Details updated
 		case ResponseHandler.MSG_DETAILS_RETRIEVED:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive task detail message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			TaskDetail details = (TaskDetail) msgP.obj;
 			if (!task.isTorrent && !task.isNZB && files != null){
 				files.updateEmptyValues(getString(R.string.empty_file_list_wrong_type), false);
 			}
 			if (!task.status.equals(details.status) && files != null && (task.isTorrent || task.isNZB)){
 				if (details.status.equals(TaskStatus.TASK_DOWNLOADING.name())) {
 					files.updateEmptyValues(getString(R.string.empty_list_loading), true);
 					app.executeAsynchronousAction(main, new GetFilesAction(task), false);
 				}
 				else{
 					files.updateEmptyValues(getString(R.string.empty_file_list), false);
 					files.resetList();
 				}
 			}
 			task.status = details.status;
 			task.isTorrent = details.isTorrent;
 			task.isNZB = details.isNZB;
 			if (main != null){
 				main.genAdapter.updateDetails(buildGeneralDetails(details));
 				((DetailTransfer)mAdapter.getItem(TRANSFER_ITEM)).transAdapter.updateDetails(buildTransferDetails(details));
 			}
 			getIntent().putExtra("com.bigpupdev.synodroid.ds.Details", task);
 			
 			setStatus(details.getStatus());
 			updateActionBarTitle(details.fileName);
 			if (UIUtils.isICS()){
 				invalidateOptionsMenu();
 			}
 			break;
 		case ResponseHandler.MSG_ERROR:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive error message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			SynoServer server = ((Synodroid) getApplication()).getServer();
 			if (server != null)
 				server.setLastError((String) msgP.obj);
 			main.showError(server.getLastError(), null);
 			break;
 		case ResponseHandler.MSG_ORIGINAL_FILE_RETRIEVED:
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Receive original file retreived message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			OriginalFile oriFile = (OriginalFile) msgP.obj;
 			File path = Environment.getExternalStorageDirectory();
 			path = new File(path, "download");
 			File file = new File(path, oriFile.fileName);
 			try {
 				// Make sure the Pictures directory exists.
 				path.mkdirs();
 				StringBuffer rawData = oriFile.rawData;
 				OutputStream os = new FileOutputStream(file);
 				os.write(rawData.toString().getBytes());
 				os.close();
 				Toast toast = Toast.makeText(this, getString(R.string.action_download_original_saved), Toast.LENGTH_SHORT);
 				toast.show();
 			} catch (Exception e) {
 				// Unable to create file, likely because external storage is
 				// not currently mounted.
 				try{
 					if (((Synodroid)getApplication()).DEBUG) Log.w(Synodroid.DS_TAG, "Error writing " + file + " to SDCard.", e);
 				}catch (Exception ex){/*DO NOTHING*/}
 				Toast toast = Toast.makeText(this, getString(R.string.action_download_original_failed), Toast.LENGTH_LONG);
 				toast.show();
 			}
 			break;
 		default: 
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG, "DetailActivity: Ignored message ID = "+ msgP.what);
 			}catch (Exception ex){/*DO NOTHING*/}
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		if (!UIUtils.isHoneycomb()){
 			getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
 		}
         super.onCreateOptionsMenu(menu);
         return true;
     }
 	
 	/**
 	 * Create the option menu of this activity
 	 */
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		if (UIUtils.isHoneycomb()){ 
 			getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
 		}
         if (status != null) {
 			switch (status) {
 			case TASK_DOWNLOADING:
 				menu.add(0, MENU_PAUSE, 0, getString(R.string.action_pause)).setIcon(R.drawable.ic_menu_pause);
 				menu.add(0, MENU_DELETE, 0, getString(R.string.action_delete)).setIcon(android.R.drawable.ic_menu_delete);
 				break;
 			case TASK_PRE_SEEDING:
 			case TASK_SEEDING:
 				menu.add(0, MENU_PAUSE, 0, getString(R.string.action_pause)).setIcon(R.drawable.ic_menu_pause);
 				menu.add(0, MENU_CANCEL, 0, getString(R.string.action_cancel)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 				break;
 			case TASK_PAUSED:
 				menu.add(0, MENU_RESUME, 0, getString(R.string.action_resume)).setIcon(android.R.drawable.ic_menu_revert);
 				menu.add(0, MENU_DELETE, 0, getString(R.string.action_delete)).setIcon(android.R.drawable.ic_menu_delete);
 				break;
 			case TASK_ERROR:
 			case TASK_ERROR_DEST_NO_EXIST:
 			case TASK_ERROR_DEST_DENY:
 			case TASK_ERROR_QUOTA_REACHED:
 			case TASK_ERROR_TIMEOUT:
 			case TASK_ERROR_EXCEED_MAX_FS_SIZE:
 			case TASK_ERROR_BROKEN_LINK:
 			case TASK_ERROR_DISK_FULL:
 			case TASK_ERROR_EXCEED_MAX_TEMP_FS_SIZE:
 			case TASK_UNKNOWN:
 			case TASK_ERROR_EXCEED_MAX_DEST_FS_SIZE:
 				menu.add(0, MENU_RETRY, 0, getString(R.string.action_retry)).setIcon(android.R.drawable.ic_menu_revert);
 				menu.add(0, MENU_DELETE, 0, getString(R.string.action_delete)).setIcon(android.R.drawable.ic_menu_delete);
 				break;
 			case TASK_FINISHED:
 				menu.add(0, MENU_RESUME, 0, getString(R.string.action_resume)).setIcon(android.R.drawable.ic_menu_revert);
 			case TASK_FINISHING:
 				menu.add(0, MENU_CLEAR, 0, getString(R.string.action_clear)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 				break;
 			case TASK_HASH_CHECKING:
 			case TASK_WAITING:
 				menu.add(0, MENU_PAUSE, 0, getString(R.string.action_pause)).setIcon(R.drawable.ic_menu_pause);
 				menu.add(0, MENU_DELETE, 0, getString(R.string.action_delete)).setIcon(android.R.drawable.ic_menu_delete);
 				break;
 			}
 		}
 		if (task.isTorrent) {
 			if (task.getStatus() == TaskStatus.TASK_DOWNLOADING || task.getStatus() == TaskStatus.TASK_SEEDING) {
 				menu.add(0, MENU_PARAMETERS, 0, getString(R.string.task_parameters)).setIcon(android.R.drawable.ic_menu_preferences).setEnabled(true);
 			} else {
 				menu.add(0, MENU_PARAMETERS, 0, getString(R.string.task_parameters)).setIcon(android.R.drawable.ic_menu_preferences).setEnabled(false);
 			}
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	/**
 	 * Interact with the user when a menu is selected
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Synodroid app = (Synodroid) getApplication();
 		DetailMain main = (DetailMain)mAdapter.getItem(MAIN_ITEM);
 		if (item.getItemId() == R.id.menu_refresh) {
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Menu refresh selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			((Synodroid) getApplication()).forceRefresh();
             return true;
         }
 		else if (item.getItemId() == MENU_PAUSE){
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Menu pause selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			app.executeAction(main, new PauseTaskAction(task), true);
 			return true;
 		}
 		else if (item.getItemId() == MENU_DELETE || item.getItemId() == MENU_CANCEL || item.getItemId() == MENU_CLEAR){
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Menu cancel/delete/clear selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			app.executeAction(main, new DeleteTaskAction(task), true);
 			return true;
 		}
 		else if (item.getItemId() == MENU_RESUME || item.getItemId() == MENU_RETRY){
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Menu resume/retry selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			app.executeAction(main, new ResumeTaskAction(task), true);
 			return true;
 		}
 		else if (item.getItemId() == MENU_PARAMETERS){
 			try{
 				if (app.DEBUG) Log.d(Synodroid.DS_TAG,"DetailActivity: Menu task properties selected.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			if (app.getServer().getDsmVersion() == DSMVersion.VERSION3_1 || app.getServer().getDsmVersion() == DSMVersion.VERSION3_2 || app.getServer().getDsmVersion() == DSMVersion.VERSION4_0) {
 				app.executeAsynchronousAction(main, new EnumShareAction(), false, false);
 			} else {
 				try {
 					showDialog(TASK_PARAMETERS_DIALOG);
 				} catch (Exception e) {
 					// Dialog failed to display. Probably already displayed. Ignore!
 				}
 			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void updateRefreshStatus(boolean refreshing) {
         getActivityHelper().setRefreshActionButtonCompatState(refreshing);
     }
 	
 	@Override
 	public boolean onSearchRequested() {
 		return false;
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// ignore orientation change
 		super.onConfigurationChanged(newConfig);
 	}
 	
 	/**
 	 * Update the current task
 	 */
 	public void updateTask(boolean forceRefreshP) {
 		Synodroid app = (Synodroid) getApplication();
 		DetailMain main = (DetailMain)mAdapter.getItem(MAIN_ITEM);
 		DetailFiles files = (DetailFiles)mAdapter.getItem(FILE_ITEM);
 		List<TaskFile> modifiedTaskFiles = null;
 		SynoServer server = null;
 		try {
 			server = app.getServer();
 		} catch (Exception e) {
 		}
 
 		if (server != null) {
 			if (server.getDsmVersion() == DSMVersion.VERSION3_1 || server.getDsmVersion() == DSMVersion.VERSION3_2 || app.getServer().getDsmVersion() == DSMVersion.VERSION4_0) {
 				if (files != null && files.fileAdapter != null){
 					modifiedTaskFiles = files.fileAdapter.getModifiedTaskList();
 				}
 				else{
 					modifiedTaskFiles = new ArrayList<TaskFile>();
 				}
 				
 				if (modifiedTaskFiles != null && modifiedTaskFiles.size() > 0) {
 					UpdateFilesAction update = new UpdateFilesAction(task, modifiedTaskFiles);
 					app.getServer().executeAsynchronousAction(main, update, forceRefreshP);
 					seedingChanged = false;
 				} else if (seedingChanged) {
 					UpdateTaskPropertiesAction update = new UpdateTaskPropertiesAction(task, ul_rate, dl_rate, priority, max_peers, destination, seedingRatio, seedingTime);
 					app.getServer().executeAsynchronousAction(main, update, forceRefreshP);
 					seedingChanged = false;
 				}
 
 			} else {
 				if (files != null){
					modifiedTaskFiles = files.fileAdapter.getModifiedTaskList();
 				}
 				else{
 					modifiedTaskFiles = new ArrayList<TaskFile>();
 				}
 				
 				if ((modifiedTaskFiles != null && modifiedTaskFiles.size() > 0) || (seedingChanged)) {
 
 					UpdateTaskAction update = new UpdateTaskAction(task, modifiedTaskFiles, seedingRatio, seedingTime);
 					app.getServer().executeAsynchronousAction(main, update, forceRefreshP);
 					seedingChanged = false;
 				}
 			}
 		}
 
 	}
 	
 	public static class MyAdapter extends FragmentPagerAdapter implements ViewPagerIndicator.PageInfoProvider{
 		int mItemsNum;
 		private DetailActivity mCurActivity;
 		private DetailMain main;
 		private DetailFiles files;
 		private DetailTransfer transfer;
 		private boolean debug = false;
 		
 		public MyAdapter(FragmentManager pFm, int pItemNum, DetailActivity pCurActivity, boolean p_debug) {
 			super(pFm);
 			mItemsNum = pItemNum;
 			mCurActivity = pCurActivity;
 			debug = p_debug;
         }
 
 		/**
 		 * This override prevents the pager to destroy non adjacent pages. 
 		 */
 		@Override
         public void destroyItem(View container, int position, Object object){
 			try{
 				if (debug) Log.d(Synodroid.DS_TAG, "DetailActivity: View pager attemps to destroy pager number: "+position);
 			}catch (Exception ex){/*DO NOTHING*/}
 			if (position == 2){
 				mCurActivity.updateTask(false);
 			}
 		}
 		
 		@Override
         public int getCount() {
             return mItemsNum;
         }
 
         @Override
         public Fragment getItem(int position) {
         	if (position == 0){
         		if (main == null)
         			main = new DetailMain();
         		return main;
         	}
         	else if (position == 1){
         		if (transfer == null)
             		transfer = new DetailTransfer();
         		return transfer;
         	}
         	else{
         		if (files == null)
             		files = new DetailFiles();
         		return files;
         		
         	}
         }
 
         public String getTitle(int pos){
         	if (pos == 0)
         		return mCurActivity.getString(R.string.tab_main);
         	else if (pos == 1)
         		return mCurActivity.getString(R.string.tab_transfer);
         	else
         		return mCurActivity.getString(R.string.tab_file);
 		}
 
     }
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (!EulaHelper.hasAcceptedEula(this)) {
             EulaHelper.showEula(false, this);
         }
         setContentView(R.layout.activity_detail);
         Intent intent = getIntent();
 		task = (Task) intent.getSerializableExtra("com.bigpupdev.synodroid.ds.Details");
         mAdapter = new MyAdapter(getSupportFragmentManager(), 3, this, ((Synodroid)getApplication()).DEBUG);
         
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setAdapter(mAdapter);
         // Find the indicator from the layout
         mIndicator = (ViewPagerIndicator)findViewById(R.id.indicator);
         
         // Set the indicator as the pageChangeListener
         mPager.setOnPageChangeListener(mIndicator);
         
         // Initialize the indicator. We need some information here:
         // * What page do we start on.
         // * How many pages are there in total
         // * A callback to get page titles
         mIndicator.init(0, mAdapter.getCount(), mAdapter);
 		Resources res = getResources();
 		Drawable prev = res.getDrawable(R.drawable.indicator_prev_arrow);
 		Drawable next = res.getDrawable(R.drawable.indicator_next_arrow);
 		mIndicator.setFocusedTextColor(new int[]{255, 255, 255});
 		mIndicator.setUnfocusedTextColor(new int[]{120, 120, 120});
 		
 		// Set images for previous and next arrows.
 		mIndicator.setArrows(prev, next);
 		
 		mIndicator.setOnClickListener(new OnIndicatorClickListener());
 
 		// Create the seeding time int array
 		String[] timesArray = getResources().getStringArray(R.array.seeding_time_array_values);
 		seedingTimes = new int[timesArray.length];
 		for (int iLoop = 0; iLoop < timesArray.length; iLoop++) {
 			seedingTimes[iLoop] = Integer.parseInt(timesArray[iLoop]);
 		}
 
 		String[] priorityArray = getResources().getStringArray(R.array.priority_array_value);
 		priorities = new int[priorityArray.length];
 		for (int iLoop = 0; iLoop < priorityArray.length; iLoop++) {
 			priorities[iLoop] = Integer.parseInt(priorityArray[iLoop]);
 		}
 
 		getActivityHelper().setupActionBar(task.fileName, false);
     }
 
 	class OnIndicatorClickListener implements ViewPagerIndicator.OnClickListener{
 		public void onCurrentClicked(View v) {}
 		
 		public void onNextClicked(View v) {
 			mPager.setCurrentItem(Math.min(mAdapter.getCount() - 1, mIndicator.getCurrentPosition() + 1));
 		}
 
 		public void onPreviousClicked(View v) {
 			mPager.setCurrentItem(Math.max(0, mIndicator.getCurrentPosition() - 1));
 		}
     	
     }
 
 }
