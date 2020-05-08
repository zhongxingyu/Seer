 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.action.AddTaskAction;
 import com.bigpupdev.synodroid.action.EnumShareAction;
 import com.bigpupdev.synodroid.action.GetAllAndOneDetailTaskAction;
 import com.bigpupdev.synodroid.action.GetDirectoryListShares;
 import com.bigpupdev.synodroid.action.SetShared;
 import com.bigpupdev.synodroid.action.SynoAction;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.Folder;
 import com.bigpupdev.synodroid.data.SharedDirectory;
 import com.bigpupdev.synodroid.data.SharedFolderSelection;
 import com.bigpupdev.synodroid.data.SynoProtocol;
 import com.bigpupdev.synodroid.data.Task;
 import com.bigpupdev.synodroid.data.TaskContainer;
 import com.bigpupdev.synodroid.data.TaskDetail;
 import com.bigpupdev.synodroid.preference.PreferenceFacade;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.ui.SynodroidFragment;
 import com.bigpupdev.synodroid.action.ShowDetailsAction;
 import com.bigpupdev.synodroid.action.TaskActionMenu;
 import com.bigpupdev.synodroid.adapter.ActionAdapter;
 import com.bigpupdev.synodroid.adapter.TaskAdapter;
 import com.bigpupdev.synodroid.utils.ActionModeHelper;
 import com.bigpupdev.synodroid.utils.EulaHelper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.WindowManager.BadTokenException;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 /**
  * This activity list all current tasks
  * 
  * @author eric.taix at gmail.com
  */
 public class DownloadFragment extends SynodroidFragment implements OnCheckedChangeListener{
 	private static final String PREFERENCE_GENERAL = "general_cat";
 	private static final String PREFERENCE_AUTO_DSM = "general_cat.auto_detect_DSM";
 	private static final String PREFERENCE_SHOW_GET_STARTED = "general_cat.show_get_started";
 	private static final String PREFERENCE_DEF_SRV = "servers_cat.default_srv";
 	private static final String PREFERENCE_SERVER = "servers_cat";
 	
 	// The connection dialog ID
 	private static final int CONNECTION_DIALOG_ID = 1;
 	// No server configured
 	private static final int NO_SERVER_DIALOG_ID = 2;
 
 	// The torrent listview
 	public ListView taskView;
 	// The total upload rate view
 	private TextView totalUpView;
 	private TextView totalTasksView;
 	// The total download rate view
 	private TextView totalDownView;
 	// Flag to tell app that the connect dialog is opened
 	private boolean connectDialogOpened = false;
 	
 	public boolean alreadyCanceled = false;
 	public ActionModeHelper mCurrentActionMode;
 	
 	private android.view.View.OnClickListener ocl;
 	
 	private void updateEmptyValues(String text, boolean showPB){
 		View empty = taskView.getEmptyView();
 		if (empty != null){
 			ProgressBar pb = (ProgressBar) empty.findViewById(R.id.empty_pb);
 			TextView tv = (TextView) empty.findViewById(R.id.empty_text);
 			if (showPB){
 				pb.setVisibility(View.VISIBLE);
 			}
 			else{
 				pb.setVisibility(View.GONE);
 			}
 			
 			tv.setText(text);
 		}
 	}
 	
 	private void killDialog(int ID){
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Closing dialog box ID: "+ID);
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		// Dissmiss the connection dialog
 		try {
 			getActivity().dismissDialog(ID);
 		}
 		// Nothing to do because it can occured when a new instance is
 		// created
 		// if a SynoCollector thread is already running
 		catch (IllegalArgumentException ex) {
 		}
 		catch (NullPointerException ex) {
 		}
 	}
 	
 	/**
 	 * Handle the message
 	 */
 	@SuppressWarnings("unchecked")
 	public void handleMessage(Message msg) {
 		final Activity a = getActivity();
 		// Update tasks
 		if (msg.what == ResponseHandler.MSG_TASK_DL_WAIT){
 			Toast toast = Toast.makeText(getActivity(), getString(R.string.wait_for_download), Toast.LENGTH_SHORT);
 			toast.show();
 		}
 		else if (msg.what == ResponseHandler.MSG_TASKS_UPDATED) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received task updated message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			TaskContainer container = (TaskContainer) msg.obj;
 			List<Task> tasks = container.getTasks();
 			// Get the adapter
 			TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
 			validateChecked(taskAdapter.updateTasks(tasks, checked_tasks_id));
 			
 			// Dismiss the connection dialog
 			killDialog(CONNECTION_DIALOG_ID);
 			
 			// Update total rates
 			totalUpView.setText(container.getTotalUp());
 			totalDownView.setText(container.getTotalDown());
 			totalTasksView.setText(String.format("%d", container.getTotalTasks()));
 			updateEmptyValues(a.getString(R.string.empty_download_list), false);
 		}
 		// Update a task's detail
 		else if (msg.what == ResponseHandler.MSG_DETAILS_RETRIEVED) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received detail retrieve message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			TaskDetail details = (TaskDetail) msg.obj;
 			// Get the adapter
 			TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
 			taskAdapter.updateFromDetail(details, checked_tasks_id);
 		}
 		// An error message
 		else if (msg.what == ResponseHandler.MSG_ERROR) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.w(Synodroid.DS_TAG,"DownloadFragment: Received error message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			// Change the title
 			((HomeActivity)a).updateActionBarTitle(a.getString(R.string.app_name), false);
 			((HomeActivity)a).updateActionBarTitleOCL(ocl);
 			updateEmptyValues(a.getString(R.string.empty_not_connected), false);
 			
 			// No tasks
 			ArrayList<Task> tasks = new ArrayList<Task>();
 			// Get the adapter AND update datas
 			TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
 			taskView.setOnItemClickListener(taskAdapter);
 			taskView.setOnItemLongClickListener(taskAdapter);
 			validateChecked(taskAdapter.updateTasks(tasks, checked_tasks_id));
 			
 			// Dismiss the connection dialog
 			killDialog(CONNECTION_DIALOG_ID);
 			
 			// Show the error
 			// Save the last error inside the server to surive UI rotation and
 			// pause/resume.
 			final SynoServer server = ((Synodroid) a.getApplication()).getServer();
 			if (server != null) {
 				server.setLastError((String) msg.obj);
 				showError(server.getLastError(), new Dialog.OnClickListener() {
 					public void onClick(DialogInterface dialogP, int whichP) {
 						// Ask to reconnect when connection is lost.
 						if (server != null) {
 							if (!server.isConnected()) {
 								showDialogToConnect(false, null, false);
 							}
 						}
 					}
 				});
 			}
 		}
 		// Connection is done
 		else if (msg.what == ResponseHandler.MSG_CONNECTED) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received connected to server message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			final SynoServer server = ((Synodroid) a.getApplication()).getServer();
 			// Change the title
 			String title = server.getNickname();
 			
 			((HomeActivity)a).updateActionBarTitle(title, server.getProtocol() == SynoProtocol.HTTPS);
 			((HomeActivity)a).updateActionBarTitleOCL(ocl);
 			
 			// Dissmiss the connection dialog
 			killDialog(CONNECTION_DIALOG_ID);
 			
 			updateEmptyValues(a.getString(R.string.empty_list_loading), true);
 			
 		}
 		// Connecting to the server
 		else if (msg.what == ResponseHandler.MSG_CONNECTING) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received connecting message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			// Clear the prevous task list
 			TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
 			validateChecked(taskAdapter.updateTasks(new ArrayList<Task>(), checked_tasks_id));
 			// Show the connection dialog
 			try {
 				a.showDialog(CONNECTION_DIALOG_ID);
 			} catch (Exception e) {
 				// Unable to show dialog probably because intent has been closed. Ignoring...
 			}
 			updateEmptyValues(a.getString(R.string.empty_not_connected), false);
 		}
 		// Show task's details
 		else if (msg.what == ResponseHandler.MSG_SHOW_DETAILS) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received show single task detail message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			// Starting new intent
 			Intent next = new Intent();
 			next.setClass(getActivity(), DetailActivity.class);
 			next.putExtra("com.bigpupdev.synodroid.ds.Details", (Task) msg.obj);
 			startActivity(next);
 		}
 		// Shared directories have been retrieved
 		else if (msg.what == ResponseHandler.MSG_SHARED_DIRECTORIES_RETRIEVED) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received shared directory listing message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(a);
 			
 			if (((Synodroid)a.getApplication()).getServer().getDsmVersion().greaterThen(DSMVersion.VERSION3_0)){
 				final SharedFolderSelection sf = (SharedFolderSelection) msg.obj;
 				final String[] dirNames = new String[sf.childrens.size()];
 				final String[] dirIDs = new String[sf.childrens.size()];
 				for (int iLoop = 0; iLoop < sf.childrens.size(); iLoop++) {
 					Folder sharedDir = sf.childrens.get(iLoop);
 					dirNames[iLoop] = sharedDir.name;
 					dirIDs[iLoop] = sharedDir.id;
 					
 				}
 				builder.setTitle(getString(R.string.shared_dir_title)+":\n"+sf.name);
 				builder.setItems(dirNames, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						dialog.dismiss();
 						Synodroid app = (Synodroid) a.getApplication();
 						app.executeAsynchronousAction(DownloadFragment.this, new GetDirectoryListShares(dirIDs[item]), true);
 					}
 				});
 				builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						dialog.dismiss();
 					}
 				});
 				if (!sf.name.equals("/")){
 					builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int item) {
 							dialog.dismiss();
 							Synodroid app = (Synodroid) a.getApplication();
 							app.executeAsynchronousAction(DownloadFragment.this, new SetShared(null, sf.name), true);
 						}
 					});	
 				}
 				
 
 			}
 			else{
 				List<SharedDirectory> newDirs = (List<SharedDirectory>) msg.obj;
 				final String[] dirNames = new String[newDirs.size()];
 				int selected = -1;
 				for (int iLoop = 0; iLoop < newDirs.size(); iLoop++) {
 					SharedDirectory sharedDir = newDirs.get(iLoop);
 					dirNames[iLoop] = sharedDir.name;
 					if (sharedDir.isCurrent) {
 						selected = iLoop;
 					}
 				}
 				builder.setTitle(getString(R.string.shared_dir_title));
 				builder.setSingleChoiceItems(dirNames, selected, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						dialog.dismiss();
 						Synodroid app = (Synodroid) a.getApplication();
 						app.executeAsynchronousAction(DownloadFragment.this, new SetShared(null, dirNames[item]), true);
 					}
 				});
 
 				
 			}
 			
 			AlertDialog alert = builder.create();
 			try {
 				alert.show();
 			} catch (BadTokenException e) {
 				// Unable to show dialog probably because intent has been closed. Ignoring...
 			}
 		}
 		else if (msg.what == ResponseHandler.MSG_SHARED_NOT_SET) {
 			try{
 				if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Received no shared folder set message.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			Synodroid app = (Synodroid) a.getApplication();
 			if (app.getServer().getDsmVersion().greaterThen(DSMVersion.VERSION3_0)){
         		app.executeAsynchronousAction(this, new GetDirectoryListShares("fm_root"), false);
         	}
         	else{
         		app.executeAsynchronousAction(this, new EnumShareAction(), false);
         	}
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         ocl = new android.view.View.OnClickListener(){
 			public void onClick(View v) {
 				showDialogToConnect(false, null, false);
 			}
 		};
 	}
 	
 	/**
 	 * Activity creation
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Creating download fragment.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		if (savedInstanceState != null){
 			alreadyCanceled = savedInstanceState.getBoolean("alreadyCanceled");
 		}
 		
 		super.onCreateView(inflater, container, savedInstanceState);
 		
 		mCurrentActionMode = ((BaseActivity) getActivity()).getActionModeHelper();
 		
 		RelativeLayout downloadContent = (RelativeLayout) inflater.inflate(R.layout.download_list, null, false);
 		taskView = (ListView) downloadContent.findViewById(android.R.id.list);
 		totalUpView = (TextView) downloadContent.findViewById(R.id.id_total_upload);
 		totalDownView = (TextView) downloadContent.findViewById(R.id.id_total_download);
 		totalTasksView = (TextView) downloadContent.findViewById(R.id.id_total_num_dl);
 		// Create the task adapter
 		TaskAdapter taskAdapter = new TaskAdapter(this);
 		taskView.setAdapter(taskAdapter);
 		taskView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		taskView.setOnItemClickListener(taskAdapter);
 		taskView.setOnItemLongClickListener(taskAdapter);
 		View empty = downloadContent.findViewById(android.R.id.empty);
 		taskView.setEmptyView(empty);
 		
 		return downloadContent;
 	}
 
 
 	public TaskAdapter getTaskAdapter() {
 		TaskAdapter result = null;
 		if (taskView != null) {
 			result = (TaskAdapter) taskView.getAdapter();
 		}
 		return result;
 	}
 
 	/**
 	 * Handle all new intent
 	 * 
 	 * @param intentP
 	 */
 	private boolean handleIntent(Intent intentP) {
 		String action = intentP.getAction();
 		if (action != null) {
 			Uri uri = null;
 			boolean out_url = false;
 			boolean use_safe = false;
 			
 			if (action.equals(Intent.ACTION_VIEW)) {
 				try{
 					if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: New action_view intent recieved.");
 				}catch (Exception ex){/*DO NOTHING*/}
 				
 				uri = intentP.getData();
 				if (uri.toString().startsWith("http://magnet/")){
 					uri = Uri.parse(uri.toString().replace("http://magnet/", "magnet:"));
 				}
 				else if (uri.toString().startsWith("https://magnet/")){
 					uri = Uri.parse(uri.toString().replace("https://magnet/", "magnet:"));
 				}
 				
 				if (!uri.toString().startsWith("file")) {
 					use_safe = true;
 					out_url = true;
 				}	
 			} else if (action.equals(Intent.ACTION_SEND)) {
 				try{
 					if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: New action_send intent recieved.");
 				}catch (Exception ex){/*DO NOTHING*/}
 				
 				String uriString = (String) intentP.getExtras().get(Intent.EXTRA_TEXT);
 				if (uriString == null) {
 					return true;
 				}
 				uri = Uri.parse(uriString);
 				out_url = true;
 			} else {
 				return true;
 			}
 			// If uri is not null
 			if (uri != null) {
 				try{
 					if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Processing intent...");
 				}catch (Exception ex){/*DO NOTHING*/}
 				
 				AddTaskAction addTask = new AddTaskAction(uri, out_url, use_safe);
 				Synodroid app = (Synodroid) getActivity().getApplication();
 				app.executeAction(this, addTask, true);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Show the dialog to connect to a server
 	 */
 	public void showDialogToConnect(boolean autoConnectIfOnlyOneServerP, final List<SynoAction> actionQueueP, final boolean automated) {
 		SharedPreferences generalPref = getActivity().getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		SharedPreferences serverPref = getActivity().getSharedPreferences(PREFERENCE_SERVER, Activity.MODE_PRIVATE);
 		boolean autoDetect = generalPref.getBoolean(PREFERENCE_AUTO_DSM, true);
 		String defaultSrv = serverPref.getString(PREFERENCE_DEF_SRV, "0");
 		
 		final Activity a = getActivity();
 		if (!connectDialogOpened && a != null) {
 			final Synodroid app = (Synodroid) a.getApplication();
 			if (app != null){
 				if (!app.isNetworkAvailable())
 					return;
 				
 				final ArrayList<SynoServer> servers = PreferenceFacade.loadServers(a, PreferenceManager.getDefaultSharedPreferences(a), app.DEBUG, autoDetect);
 				// If at least one server
 				if (servers.size() != 0) {
 					// If more than 1 server OR if we don't want to autoconnect then
 					// show the dialog
 					if (servers.size() > 1 || !autoConnectIfOnlyOneServerP) {
 						boolean skip = false;
 						String[] serversTitle = new String[servers.size()];
 						for (int iLoop = 0; iLoop < servers.size(); iLoop++) {
 							SynoServer s = servers.get(iLoop);
 							serversTitle[iLoop] = s.getNickname();
 							
 							//Check if default server and connect to it skipping the dialog...
 							if (defaultSrv.equals(s.getID()) && autoConnectIfOnlyOneServerP){
 								app.connectServer(DownloadFragment.this, s, actionQueueP, automated);
 								skip = true;
 							}
 						}
 						if (!skip){
 							connectDialogOpened = true;
 							AlertDialog.Builder builder = new AlertDialog.Builder(a);
 							builder.setTitle(getString(R.string.menu_connect));
 							// When the user select a server
 							builder.setItems(serversTitle, new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int item) {
 									SynoServer server = servers.get(item);
 									// Change the server
 									app.connectServer(DownloadFragment.this, server, actionQueueP, automated);
 									dialog.dismiss();
 								}
 							});
 							AlertDialog connectDialog = builder.create();
 							try {
 								connectDialog.show();
 							} catch (BadTokenException e) {
 								// Unable to show dialog probably because intent has been closed. Ignoring...
 							}
 							connectDialog.setOnDismissListener(new OnDismissListener() {
 								public void onDismiss(DialogInterface dialog) {
 									connectDialogOpened = false;
 								}
 							});
 						}
 					} else {
 						// Auto connect to the first server
 						if (servers.size() > 0) {
 							SynoServer server = servers.get(0);
 							// Change the server
 							app.connectServer(DownloadFragment.this, server, actionQueueP, automated);
 						}
 					}
 				}
 				// No server then show the dialog to configure a server
 				else {
 					// Only if the EULA has been accepted. If the EULA has not been
 					// accepted, it means that the EULA is currenlty being displayed so
 					// don't show the "Wizard" dialog
 					if (EulaHelper.hasAcceptedEula(a) && !alreadyCanceled) {
 						try {
 							a.showDialog(NO_SERVER_DIALOG_ID);
 						} catch (Exception e) {
 							// Unable to show dialog probably because intent has been closed or the dialog is already displayed. Ignoring...
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		final Activity a = getActivity();
 		
 		SharedPreferences preferences = a.getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		if (preferences.getBoolean(PREFERENCE_SHOW_GET_STARTED, true)){
 			return;
 		}
 		if (!EulaHelper.hasAcceptedEula(a)) {
 			EulaHelper.showEula(false, a);
 			return;
 		}
 		 	 
 		/**
 		 * Intents are driving me insane.
 		 * 
 		 * When an intent has been handle by the app I mark the flag activity launched from history on so we do not reprocess that intent again. This simplify was more how I was handling intents before and is effective in every cases in all android 1.5 up versions...
 		 * 
 		 * */
 
 		boolean connectToServer = true;
 		// Get the current main intent
 		Intent intent = a.getIntent();
 		String action = intent.getAction();
 		// Check if it is a actionable Intent
 		if (action != null && (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))) {
 			// REUSE INTENT CHECK: check if the intent is comming out of the history.
 			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
 				// Not from history -> process intent
 				connectToServer = handleIntent(intent);
 			}
 			else{
 				try{
 					if (((Synodroid)a.getApplication()).DEBUG) Log.i(Synodroid.DS_TAG,"DownloadFragment: This was an old intent. Skipping it...");
 				}
 				catch (Exception ex){/*DO NOTHING*/}
 			}
 		} 
 		
 		// PREVENT INTENT REUSE: We mark the intent so from now on, the program
 		// thinks its from history
 		intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
 		a.setIntent(intent);
 
 		// There are some case where the connected server does not show up in
 		// the title bar on top. This fixes thoses cases.
 		SynoServer server = ((Synodroid) a.getApplication()).getServer();
 		if (server != null && server.isConnected()) {
 			String title = server.getNickname();
 			
 			((HomeActivity)a).updateActionBarTitle(title, server.getProtocol() == SynoProtocol.HTTPS);
 			((HomeActivity)a).updateActionBarTitleOCL(ocl);
 			
 			// Launch the gets task's details recurrent action
 			Synodroid app = (Synodroid) a.getApplication();
 			app.setRecurrentAction(this, new GetAllAndOneDetailTaskAction(server.getSortAttribute(), server.isAscending(), (TaskAdapter) taskView.getAdapter()));
 
 			app.resumeServer();
 			
 			updateEmptyValues(a.getString(R.string.empty_list_loading), true);
 		}
 		// No server then display the connection dialog
 		else {
 			if (connectToServer)
 				showDialogToConnect(true, null, true);
 		}
 	}
 
 	/**
 	 * A task as been clicked by the user
 	 * 
 	 * @param taskP
 	 */
 	public void onTaskClicked(final Task taskP) {
 		Synodroid app = (Synodroid) getActivity().getApplication();
 		app.executeAction(DownloadFragment.this, new ShowDetailsAction(taskP), true);
 	}
 
 	/**
 	 * A task as been long clicked by the user
 	 * 
 	 * @param taskP
 	 */
 	public void onTaskLongClicked(final Task taskP) {
 		final Activity a = getActivity();
 		AlertDialog.Builder builder = new AlertDialog.Builder(a);
 		builder.setTitle(getString(R.string.dialog_title_action));
 		final ActionAdapter adapter = new ActionAdapter(a, taskP);
 		if (adapter.getCount() != 0) {
 			builder.setAdapter(adapter, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					TaskActionMenu taskAction = (TaskActionMenu) adapter.getItem(which);
 					// Only if TaskActionMenu is enabled: it seems that even if the
 					// item is
 					// disable the user can tap it
 					if (taskAction.isEnabled()) {
 						Synodroid app = (Synodroid) a.getApplication();
 						app.executeAction(DownloadFragment.this, taskAction.getAction(), true);
 					}
 				}
 			});
 			AlertDialog connectDialog = builder.create();
 			try {
 				connectDialog.show();
 			} catch (BadTokenException e) {
 				// Unable to show dialog probably because intent has been closed. Ignoring...
 			}
 		}
 	}
 
 	
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		// Save UI state changes to the savedInstanceState.
 		// This bundle will be passed to onCreate if the process is
 		// killed and restarted.
 		savedInstanceState.putBoolean("alreadyCanceled", alreadyCanceled);
 
 		// etc.
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	// List of checkbox and task
 	public List<Task> checked_tasks = new ArrayList<Task>();
 	public List<Integer> checked_tasks_id = new ArrayList<Integer>();
 	
 	public void resetChecked(){
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Resetting check selection.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		checked_tasks = new ArrayList<Task>();
 		checked_tasks_id = new ArrayList<Integer>();
 		TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
 		taskAdapter.clearTasksSelection();
 	
 	}
 	
 	public void validateChecked(ArrayList<Integer> currentTasks){
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DownloadFragment: Validating checked items.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		List<Integer> toDel = new ArrayList<Integer>();
 		
 		for (Integer i : checked_tasks_id) {
 			if (!currentTasks.contains(i)){
 				toDel.add(checked_tasks_id.indexOf(i));
 			}
 		}
 		Collections.sort(toDel, Collections.reverseOrder());
 		
 		for (Integer pos : toDel){
 			try{
 				checked_tasks.remove(pos.intValue());
 			}catch (IndexOutOfBoundsException e){ /*IGNORE*/}
 			try{
 				checked_tasks_id.remove(pos.intValue());
 			}catch (IndexOutOfBoundsException e){ /*IGNORE*/}
 		}
 		
 		if (checked_tasks_id.size() == 0){
 			mCurrentActionMode.stopActionMode();
 		}
 		else{
 			String selected = getActivity().getString(R.string.selected);
 			mCurrentActionMode.setTitle(Integer.toString(checked_tasks_id.size()) +" "+ selected);
 		}
 	}
 	
 	public void onCheckedChanged(CompoundButton button, boolean check) {
 		Task t = (Task)button.getTag();
 		if (check){
 			if (checked_tasks_id.contains(t.taskId)) return;
 			t.selected = true;
 			
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"DownloadFragment: Task id "+t.taskId+" checked.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			mCurrentActionMode.startActionMode(this);
 			checked_tasks.add(t);
 			checked_tasks_id.add(t.taskId);
 		}
 		else{
 			if (!checked_tasks_id.contains(t.taskId)) return;
 			t.selected = false;
 			
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"DownloadFragment: Task id "+t.taskId+" unchecked.");
 			}catch (Exception ex){/*DO NOTHING*/}
 
 			checked_tasks.remove(t);
 			checked_tasks_id.remove(checked_tasks_id.indexOf(t.taskId));
 			if (checked_tasks_id.size() == 0){
 				if (!mCurrentActionMode.terminating){
 					mCurrentActionMode.stopActionMode();
 				}
 			}
 		}
 		String selected = getActivity().getString(R.string.selected);
 		mCurrentActionMode.setTitle(Integer.toString(checked_tasks_id.size()) +" "+ selected);
 	}
 	
 }
