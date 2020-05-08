 package org.touchirc.activity;
 
 import org.touchirc.R;
 import org.touchirc.R.layout;
 import org.touchirc.TouchIrc;
 import org.touchirc.adapter.ServerAdapter;
 import org.touchirc.irc.IrcBinder;
 import org.touchirc.irc.IrcBot;
 import org.touchirc.irc.IrcService;
 import org.touchirc.model.Server;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class ExistingServersActivity extends SherlockListActivity implements ServiceConnection{
 
 	private ListView servers_LV;
 	private SparseArray<Server> servers;
 	private ServerAdapter adapterServer;
 	private int idSelectedServer; // selected server's index in the listView
 	private Server selectedServer;
 	private Context c;
 	private ActionBar actionBar;
 	private TouchIrc touchIrc;
 	private View viewItem;
 	
 	protected ActionMode mActionMode; // Variable used for triggering the actionMode (ActionBar)
 	private IrcService ircService;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.ircService = null;
 		Intent intent = new Intent(this, IrcService.class);
 		getApplicationContext().bindService(intent, this, 0);
 
 
 		// Allows the icon to do "Previous"
 		this.actionBar = getSupportActionBar();
 		this.actionBar.setDisplayHomeAsUpEnabled(true);
 
 		setContentView(layout.listview_layout);
 
 		// Collect the context
 		c = this;
 		touchIrc = TouchIrc.getInstance();
 
 		// Collect the server widgets ListView (LV)
 		this.servers_LV = (ListView) findViewById(android.R.id.list);
 
 		// the LV is always focused on its last item
 		this.servers_LV.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
 
 		// Collect the Servers list
 		this.servers = touchIrc.getAvailableServers();
 
 		this.actionBar.setTitle("Servers  (" + this.servers.size() + ")");
 
 		/**
 		 * 
 		 * When the user click on an item an ActionMode Bar appears.
 		 * It provides him some features on the selected server : Set auto-connect this server, Edit, Delete.
 		 * 
 		 */
 
 		servers_LV.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long arg3) {
 				
 				idSelectedServer = (int) adapterServer.getItemId(position);
 				selectedServer = adapterServer.getItem(position);
 				viewItem = v;
 
 				// if the ActionMode is already displayed
 				if (mActionMode != null) {
 					mActionMode.finish(); // closing it
 					mActionMode = startActionMode(new ActionModeServerSettings()); // and launching it to update values
 				}
 				else{
 					mActionMode = startActionMode(new ActionModeServerSettings()); // launching the ActionMode
 				}
 				// the item is selected
 				viewItem.setSelected(true);
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * Here, we define the behavior of the actionMode according the input events
 	 * thanks to a specific class
 	 * 
 	 */
 
 	private final class ActionModeServerSettings implements ActionMode.Callback {
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 
 			MenuInflater inflater = getSupportMenuInflater();
 
 			// Load the contextual Menu
 			inflater.inflate(R.menu.context_menu_server, menu);
 
 			// if the server is already the auto-connected one, we cannot set it
 			if(selectedServer.isAutoConnect()){
 				menu.getItem(2).setTitle(R.string.disAUTO);
 			}
 			else{
 				menu.getItem(2).setTitle(R.string.AUTO);
 			} 
 
 			return true;
 		}
 
 		// Called each time the action mode is shown. Always called after
 		// onCreateActionMode, but
 		// may be called multiple times if the mode is invalidated.
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false; // if nothing is done
 		}
 
 		// Called when the user selects a contextual menu item
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 
 			switch (item.getItemId()) {		
 
 			// ########## if the item "AutoConnect" is selected ##########
 			case R.id.autoConnect :
 
 				if(selectedServer.isAutoConnect()){
 					// The selected server loose its status of auto-connected server
 					selectedServer.disableAutoConnect();
 					touchIrc.updateServer(idSelectedServer, selectedServer, c);
 					mode.getMenu().getItem(2).setTitle(R.string.AUTO);
 				}
 				else{
 					// The selected server is now the auto-connected one
 					selectedServer.enableAutoConnect();
 					touchIrc.updateServer(idSelectedServer, selectedServer, c);
 					mode.getMenu().getItem(2).setTitle(R.string.disAUTO);
 					String s = getResources().getString(R.string.nowUsedForAutoConnection);
 					Toast.makeText(c, selectedServer.getName() + s, Toast.LENGTH_LONG).show();
 				}				
 
 				// Notifying the adapter to update the display
 				adapterServer.notifyDataSetInvalidated();
 
 				return true;
 
 			// ########## if the item "Edit" is selected ##########		
 			case R.id.edit : 
 
 				// Prepare the Intent to switch on the activity which allows to modify the server
 				Intent i = new Intent(ExistingServersActivity.this, CreateServerActivity.class);
 
 				// Put the informations of the current server into a Bundle object
 				Bundle b = new Bundle();
 				b.putInt("ServerId", servers.keyAt(servers.indexOfValue(selectedServer)));
 
 				// Assign the Bundle to the Intent
 				i.putExtra("ServerIdentification", b);
 
 				// Start the Intent
 				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(i);
 
 				finish(); // close the current activity
 
 				return true;
 
 			// ########## if the item "Delete" is selected ##########
 			case R.id.delete :
 
 				// Instantiate an AlertDialog.Builder with its constructor
 				AlertDialog.Builder builder = new AlertDialog.Builder(ExistingServersActivity.this);
 
 				// Chain together various setter methods to set the dialog characteristics
 				builder.setTitle(R.string.deleteServer)
 				.setMessage("Would you really like to delete the server : " + selectedServer.getName() + " ?")
 				.setIcon(android.R.drawable.ic_menu_delete);
 
 				builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						
 						// Removal throughout the db
 						if (touchIrc.deleteServer(idSelectedServer, getApplicationContext())){ // if the deletion is successful
 							// Reload the new server list
 							servers = touchIrc.getAvailableServers();
 							// Notify the adapter that the list's state has changed
 							adapterServer.notifyDataSetChanged();
 							// Update the number of available servers in the TV
 							actionBar.setTitle("Servers  (" + servers.size() + ")");
 						}
 						
 						/** ------------------------------------------------------- **/
 						
 						
 					}
 				});
 				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 
 
 				// Get the AlertDialog from create()
 				AlertDialog dialog = builder.create();
 				dialog.show();
 
 				return true;
 
 			default:
 				mode.finish(); // the actionMode disappears
 				return false;
 			}
 		}
 
 		// Called when the user exits the action mode
 		public void onDestroyActionMode(ActionMode mode) {
 			// the actionMode is destroyed by putting it at null
 			mActionMode = null;
 			// the item is unselected
 			viewItem.setSelected(false);
 		}
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id){
 		viewItem.setSelected(true);
 		super.onListItemClick(l, v, position, id);
 		
 		if(mActionMode == null){
 
			if(TouchIrc.getInstance().getAvailableProfiles().size() != 0 || TouchIrc.getInstance().getIdDefaultProfile() != -1){
 				Log.i("TouchIRC ", "Attempt to connect to the server " + this.servers.get((int)adapterServer.getItemId(position)).getName() +" !");
 				IrcBot bot = ircService.getBot(adapterServer.getItem(position));
 				if (bot.isConnected) {
 					Intent intentChat = new Intent(getApplicationContext(),	ConversationActivity.class);
 					intentChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					intentChat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					intentChat.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 					getApplicationContext().startActivity(intentChat);
 				} else {
 					new ConnectingTask(bot, v).execute();
 				}
 			}
 			else{
 				String msgToast;
 				Intent intent;
 				if(TouchIrc.getInstance().getAvailableProfiles().size() != 0){
 					msgToast = getResources().getString(R.string.youNeedAProfile);
 					Toast.makeText(this, msgToast, Toast.LENGTH_LONG).show();
 					intent = new Intent(this, CreateProfileActivity.class);
 				}
 				else{
 					msgToast = getResources().getString(R.string.chooseADefaultProfile);
 					Toast.makeText(this, msgToast, Toast.LENGTH_LONG).show();
 					intent = new Intent(this, ExistingProfilesActivity.class);
 				}
 				startActivity(intent);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * This method is called each time the system goes back on this activity
 	 * 
 	 */
 
 	@ Override
 	protected void onResume(){
 		super.onResume();
 		
 		touchIrc = TouchIrc.getInstance();
 		servers = touchIrc.getAvailableServers();
 		
 		if(adapterServer != null){ //TODO Fix it, it's ugly
 			// Update the list and its display
 			this.adapterServer.notifyDataSetChanged();
 			this.adapterServer.notifyDataSetInvalidated();
 		}
 		this.actionBar.setTitle("Servers  (" + this.servers.size() + ")");
 	}
 
 	/**
 	 * Define the display of the actionBar
 	 * 
 	 * @param menu
 	 * @return true
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.existing_object_activity, menu);
 		return true;
 	}
 	
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the icon 
 	 * in the action bar: When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			intent = new Intent(this, MenuActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		case R.id.add :
 			intent = new Intent(ExistingServersActivity.this, CreateServerActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			Bundle bundle = new Bundle();
 			bundle.putBoolean("comingFromExistingServersActivity", true);
 			intent.putExtra("AddingFromExistingServersActivity", bundle);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the physical button 
 	 * "Back" (on device) : When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 
 			Intent intent = new Intent(this, MenuActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		this.ircService = null;
 		
 	}
 
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder binder) {
 		this.ircService = ((IrcBinder) binder).getService();
 		// Put an ArrayAdapter so that the LV and the servers list be linked
 		this.adapterServer =  new ServerAdapter(servers, c, ircService);
 		this.setListAdapter(adapterServer);
 	}
 	
 	private class ConnectingTask extends AsyncTask<Void, Integer, Long> {
 		private View view;
 		private IrcBot bot;
 
 		public ConnectingTask(IrcBot bot, View v) {
 			this.bot = bot;
 			this.view = v;
 		}
 
 		protected Long doInBackground(Void... useless) {
 			while (!bot.isConnected)
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {}
 
 			return (long) 0;
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 		}
 
 		protected void onPreExecute() {
 			view.findViewById(R.id.serverSpinner).setVisibility(View.VISIBLE);
 		}
 
 		protected void onPostExecute(Long result) {
 			view.findViewById(R.id.serverSpinner).setVisibility(View.GONE);
 			adapterServer.notifyDataSetChanged();
 			//view.findViewById(R.id.serverConnectedImageView).setVisibility(View.VISIBLE);
 		}
 	}
 	
 }
