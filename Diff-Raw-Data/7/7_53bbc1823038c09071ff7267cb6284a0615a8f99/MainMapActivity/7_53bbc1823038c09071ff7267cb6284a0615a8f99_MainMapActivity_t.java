 package pl.edu.agh.io.coordinator;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.Message;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import pl.edu.agh.io.coordinator.utils.Alerts;
 import pl.edu.agh.io.coordinator.utils.chat.ChatState;
 import pl.edu.agh.io.coordinator.utils.container.DataContainer;
 import pl.edu.agh.io.coordinator.utils.container.DataContainer.OnDataContainerChangesListener;
 import pl.edu.agh.io.coordinator.utils.layersmenu.LayersMenuListener;
 import pl.edu.agh.io.coordinator.utils.layersmenu.LayersMenuState;
 import pl.edu.agh.io.coordinator.utils.net.IJSonProxy;
 import pl.edu.agh.io.coordinator.utils.net.JSonProxy;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidLayerException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidMapItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainMapActivity extends Activity implements
 		ChatFragment.OnFragmentInteractionListener, LayersMenuListener,
 		OnDataContainerChangesListener {
 
 	public final static String ITEM_POSITION = "pl.edu.agh.io.coordinator.ITEM_POSITION";
 	public static MainMapActivity currentInstance = null;
 	
 	private boolean layersMenuVisible = false;
 	private boolean chatVisible = false;
 	
 	private boolean isMenuCreated = false;
 	
 	private static final LatLng DEFAULT_POSITION = new LatLng(50.061368, 19.936924); // Cracow
 
 	private DataContainer dataContainer = new DataContainer(this);
 	private boolean loggingOut = false;
 	private LayersMenuFragment layersFragment = new LayersMenuFragment();
 	private LayersMenuState savedLayersMenuState = null;
 	private ChatState savedChatState = null;
 
 	private GoogleMap googleMap;
 	private Location myLocation;
 
 	private ChatFragment chatFragment = ChatFragment.newInstance();
 
 	private Set<UserItem> userItems;
 	private Set<User> users;
 	private Set<Group> groups;
 
 	private Set<Layer> layers;
 
 	private HashMap<MapItem, Marker> mapItemToMarker = new HashMap<MapItem, Marker>();
 
 	private MainThread mainThread;
 	
 	private Map<String, Boolean> activeLayers = new HashMap<String, Boolean>();
 	
 	//private Set<Thread> threads = new HashSet<Thread>();
 	
 	private class MainThread extends Thread {
 		
 		private boolean stopped = false;
 		
 		public synchronized void safelyStop() {
 			stopped = true;
 		}
 		
 		@Override
 		public void run() {
 			boolean isStopped = false;
 			while (true) {
 				try {
 					Thread.sleep(1000);
 					Log.d("MainMapActivity", "getting data from server");
 					new GetUsersInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Intent());
 					new GetUserItemsInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Intent());
 					new GetGroupsInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Intent());
 					if (chatFragment.isActive()) {
 						new GetMessagesInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 					}
 					if (layers != null)
 						for (Layer layer : layers)
 							new GetMapItemsInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, layer);
 					synchronized (this) {
 						Log.d("MainMapActivity.MainThread", "checking isStopped");
 						isStopped = stopped;
 					}
 					if (isStopped) {
 						Log.d("MainMapActivity.MainThread", "stopping main thread");
 						break;
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	public LayersMenuState getSavedLayersMenuState() {
 		return this.savedLayersMenuState;
 	}
 
 	public void setSavedLayersMenuState(LayersMenuState state) {
 		this.savedLayersMenuState = state;
 	}
 	
 	public ChatState getSavedChatState() {
 		return this.savedChatState;
 	}
 	
 	public void setSavedChatState(ChatState state) {
 		this.savedChatState = state;
 	}
 	
 	public boolean isLayerActive(String layer) {
 		if (activeLayers.containsKey(layer)) {
 			return this.activeLayers.get(layer);
 		} else {
 			return false;
 		}
 	}
 	
 	private void initFragments(FragmentManager fragmentManager) {
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		fragmentTransaction.add(R.id.layersFrame, layersFragment);
 		fragmentTransaction.hide(layersFragment);
 		fragmentTransaction.add(R.id.chatFrame, chatFragment);
 		fragmentTransaction.hide(chatFragment);
 		fragmentTransaction.commit();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("MainMapActivity", "starting onCreate");
 		super.onCreate(savedInstanceState);
 
 		currentInstance = this;
 		
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_main_map);
 
 		FragmentManager fragmentManager = getFragmentManager();
 		initFragments(fragmentManager);
 
 		new GetLayersInBackground().executeOnExecutor(
 				AsyncTask.THREAD_POOL_EXECUTOR, new Intent());
 
 		setUpMapIfNeeded();
 		mainThread = new MainThread();
 
 		// threads.add(mainThread);
 		mainThread.start();
 
 		if (savedInstanceState != null) {
 			Log.d("MainMapActivity", "starting getting saved state");
 			layersMenuVisible = savedInstanceState.getBoolean("layersMenuVisible");
 			chatVisible = savedInstanceState.getBoolean("chatVisible");
 			savedLayersMenuState = savedInstanceState.getParcelable("savedLayersMenuState");
 			activeLayers = new HashMap<String, Boolean>(savedLayersMenuState.layersChecks);
 			savedChatState = savedInstanceState.getParcelable("savedChatState");
 			if (savedChatState != null) {
 				Log.d("MainMapActivity", "starting ... " + savedChatState.messages.size());
 			} else {
 				Log.d("MainMapActivity", "starting ... " + "null");
 			}
			CameraUpdate update = CameraUpdateFactory.newCameraPosition((CameraPosition) savedInstanceState
					.getParcelable("cameraPosition"));
			googleMap.moveCamera(update);
 		}
 		
 	}
 
 	@Override
 	protected void onRestart() {
 		Log.d("MainMapActivity", "starting onRestart");
 		FragmentManager fragmentManager = getFragmentManager();
 		initFragments(fragmentManager);
 		super.onRestart();
 	}
 	
 	@Override
 	protected void onStart() {
 		Log.d("MainMapActivity", "starting onStart");
 		super.onStart();
 	}
 	
 	@Override
 	protected void onResume() {
 		Log.d("MainMapActivity", "starting onResume");
 		setUpMapIfNeeded();
 		if (!isMenuCreated) {
 			invalidateOptionsMenu();
 		}
 		super.onResume();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d("MainMapActivity", "starting onSaveInstanceState");
 		FragmentManager fragmentManager = getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		fragmentTransaction.remove(layersFragment);
 		fragmentTransaction.remove(chatFragment);
 		fragmentTransaction.commit();
 		outState.putBoolean("layersMenuVisible", layersMenuVisible);
 		outState.putBoolean("chatVisible", chatVisible);
 		outState.putParcelable("savedLayersMenuState", savedLayersMenuState);
 		outState.putParcelable("savedChatState", savedChatState);
		outState.putParcelable("cameraPosition", googleMap.getCameraPosition());
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	protected void onPause() {
 		Log.d("MainMapActivity", "starting onPause");
 		isMenuCreated = false;
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStop() {
 		Log.d("MainMapActivity", "starting onStop");
 		super.onStop();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		Log.d("MainMapActivity", "starting onDestroy");
 		mainThread.safelyStop();
 		super.onDestroy();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.d("MainMapActivity", "starting onCreateOptionsMenu");
 		getMenuInflater().inflate(R.menu.main_map, menu);
 		menu.getItem(0).setChecked(chatVisible);
 		menu.getItem(1).setChecked(layersMenuVisible);
 		isMenuCreated = true;
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Log.d("MainMapActivity", "starting onPrepareOptionsMenu");
 		FragmentManager fragmentManager = getFragmentManager();
 		if (layersMenuVisible) {
 			Log.d("MainMapActivity", "getting saved state: layersMenuVisible");
 			FragmentTransaction lmvTransaction = fragmentManager.beginTransaction();
 			lmvTransaction.show(layersFragment);
 			lmvTransaction.commit();
 		}
 		if (chatVisible) {
 			Log.d("MainMapActivity", "getting saved state: chatVisible");
 			FragmentTransaction cvTransaction = fragmentManager.beginTransaction();
 			cvTransaction.show(chatFragment);
 			cvTransaction.commit();
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.actionChat:
 			Log.d("MainMapActivity", "LMDEBUG: Chat clicked");
 			chatVisible = !item.isChecked();
 			item.setChecked(!item.isChecked());
 			FragmentManager fragmentManager = getFragmentManager();
 			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 			if (item.isChecked()) {
 				fragmentTransaction.show(chatFragment);
 			} else
 				fragmentTransaction.hide(chatFragment);
 			fragmentTransaction.commit();
 			return true;
 		case R.id.actionLayers:
 			Log.d("MainMapActivity", "LMDEBUG: Layers menu clicked");
 			layersMenuVisible = !item.isChecked();
 			item.setChecked(!item.isChecked());
 			FragmentManager fragmentManager2 = getFragmentManager();
 			FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
 			if (item.isChecked()) {
 				// fragmentTransaction2.add(R.id.layersFrame, layersFragment);
 				fragmentTransaction2.show(layersFragment);
 			} else
 				// fragmentTransaction2.remove(layersFragment);
 				fragmentTransaction2.hide(layersFragment);
 			fragmentTransaction2.commit();
 			return true;
 		case R.id.actionCreateGroup:
 			Intent intentCreateGroup = new Intent(MainMapActivity.this, CreateGroupActivity.class);
 			startActivity(intentCreateGroup);
 			return true;
 		case R.id.actionRemoveGroup:
 			Intent intentRemoveGroup = new Intent(MainMapActivity.this, RemoveGroupActivity.class);
 			startActivity(intentRemoveGroup);
 			return true;
 		case R.id.actionCreateItem:
 			myLocation=new Location("justTest");
 			myLocation.setLatitude(50.0);
 			myLocation.setLongitude(20.0);
 			Intent intentCreateItem = new Intent(MainMapActivity.this, CreateMapItemActivity.class);
 			intentCreateItem.putExtra(ITEM_POSITION, myLocation);
 			startActivity(intentCreateItem);
 			return true;
 		case R.id.actionSettings:
 			Intent intentSettings = new Intent(MainMapActivity.this, NotImplementedYetActivity.class);
 			startActivity(intentSettings);
 			return true;
 		case R.id.actionLogout:
 			if (!loggingOut) {
 				loggingOut = true;
 				Intent intent = new Intent();
 				new LogoutInBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, intent);
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void setUpMapIfNeeded() {
 		if (googleMap == null) {
 			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
 					R.id.mainMapFragment)).getMap();
 			if (googleMap == null) {
 				// TODO: print error
 			} else {
 				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_POSITION, 16.0f);
 				googleMap.moveCamera(cameraUpdate);
 				googleMap.setMyLocationEnabled(true);
 				googleMap.getUiSettings().setMyLocationButtonEnabled(true);
 
 				googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 					
 					@Override
 					public void onInfoWindowClick(Marker marker) {
 						Toast.makeText(getApplicationContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
 					}
 					
 				});
 
 				googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
 					
 					@Override
 					public void onMapLongClick(LatLng arg0) {
 						Intent intentCreateItem = new Intent(MainMapActivity.this, CreateMapItemActivity.class);
 						Location location = new Location("Map item");
 						location.setLatitude(arg0.latitude);
 						location.setLongitude(arg0.longitude);
 						intentCreateItem.putExtra(ITEM_POSITION, location);
 						startActivity(intentCreateItem);
 					}
 					
 				});
 				
 			}
 		}
 	}
 
 	private class LogoutInBackground extends AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				proxy.logout();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				MainMapActivity.this.finish();
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 				loggingOut = false;
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetUsersInBackground extends
 			AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			Log.d("MainMapActivity", "GetUsersInBackground.doInBackground()");
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				users = proxy.getUsers();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				if (MainMapActivity.this.layersFragment != null)
 					MainMapActivity.this.layersFragment.setPeople(users);
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetLayersInBackground extends
 			AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				layers = proxy.getLayers();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				layersFragment.setLayers(layers);
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetMapItemsInBackground extends
 			AsyncTask<Layer, Void, Exception> {
 
 		Set<MapItem> mapItems;
 		Layer layer;
 
 		@Override
 		protected Exception doInBackground(Layer... params) {
 
 			Log.d("MainMapActivity", "GetMapItemsInBackground.doInBackground()");
 
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				mapItems = proxy.getMapItems(params[0]);
 				layer = params[0];
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			} catch (InvalidLayerException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				if (layersFragment != null)
 					dataContainer.newMapItemsSet(layer, mapItems);
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			} else if (result instanceof InvalidLayerException) {
 				Alerts.invalidLayer(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class RemoveMapItemInBackground extends
 			AsyncTask<MapItem, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(MapItem... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				proxy.removeMapItem(params[0]);
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			} catch (InvalidMapItemException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			} else if (result instanceof InvalidMapItemException) {
 				Alerts.invalidMapItem(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetGroupsInBackground extends
 			AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				groups = proxy.getGroups();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				if (layersFragment != null)
 					layersFragment.setGroups(groups);
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetUserItemsInBackground extends
 			AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				userItems = proxy.getPossibleUserItems();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				if (layersFragment != null)
 					layersFragment.setItems(userItems);
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class SendMessageInBackground extends
 			AsyncTask<String, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(String... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 			Log.d("MainMapActivity", "SendMessageInBackground.doInBackground()");
 			try {
 				proxy.sendMessage(params[0]);
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			Log.d("MainMapActivity", "SendMessageInBackground.onPostExecute()");
 			if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class GetMessagesInBackground extends
 			AsyncTask<Void, Void, Exception> {
 
 		List<Message> messages;
 
 		@Override
 		protected Exception doInBackground(Void... params) {
 			IJSonProxy proxy = JSonProxy.getInstance();
 
 			try {
 				messages = proxy.getMessages();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				for (Message m : messages) {
 					if (!chatFragment.isActive()) {
 						Log.d("MainMapActivity", "starting dropping message");
 						new WaitForChatActivation(m).start();
 						//new AddMessageAfterActivation(m).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
 					} else {
 						Log.d("MainMapActivity", "starting adding message");
 						chatFragment.newMessage(m);
 					}
 				}
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 			} else if (result instanceof InvalidSessionIDException) {
 				Alerts.invalidSessionId(MainMapActivity.this);
 			}
 		}
 
 	}
 
 	private class WaitForChatActivation extends Thread {
 		
 		private Message message;
 		
 		public WaitForChatActivation(Message message) {
 			this.message = message;
 		}
 		
 		@Override
 		public void run() {
 			Log.d("MainMapActivity", "starting waiting thread");
 			ChatFragment chatFragment = MainMapActivity.this.chatFragment;
 			while (!chatFragment.isActive()) {
 				ChatFragment tempChatFragment = MainMapActivity.currentInstance.chatFragment;
 				if (tempChatFragment != null) {
 					chatFragment = tempChatFragment;
 				}
 			}
 			Log.d("MainMapActivity", "finished waiting thread");
 			new AddMessageAfterActivation(message).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
 		}
 		
 	}
 	
 	private class AddMessageAfterActivation extends AsyncTask<Void, Void, Void> {
 		
 		private Message message;
 		
 		public AddMessageAfterActivation(Message message) {
 			this.message = message;
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			ChatFragment chatFragment = MainMapActivity.currentInstance.chatFragment;
 			if ((chatFragment != null) && (chatFragment.isActive())) {
 				Log.d("MainMapActivity", "starting adding message after waiting");
 				chatFragment.newMessage(message);
 			} else {
 				Log.d("MainMapActivity", "starting dropping message again");
 				new WaitForChatActivation(message).start();
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onChatSendMessage(String text) {
 		new SendMessageInBackground().executeOnExecutor(
 				AsyncTask.THREAD_POOL_EXECUTOR, text);
 	}
 
 	@Override
 	public void itemChecked(String item) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing itemChecked, item = " + item);
 	}
 
 	@Override
 	public void itemUnchecked(String item) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing itemUnhecked, item = " + item);
 	}
 
 	@Override
 	public void userChecked(String user) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing userChecked, user = " + user);
 	}
 
 	@Override
 	public void userUnchecked(String user) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing userUnchecked, user = " + user);
 	}
 
 	@Override
 	public void groupChecked(String group) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing groupChecked, group = " + group);
 	}
 
 	@Override
 	public void groupUnchecked(String group) {
 		// TODO Auto-generated method stub
 		Log.d("MainMapActivity", "executing groupUnchecked, group = " + group);
 	}
 
 	@Override
 	public void layerChecked(String layer) {
 		Log.d("MainMapActivity", "executing layerChecked, layer = " + layer);
 		activeLayers.put(layer, true);
 		dataContainer.showLayer(layer);
 	}
 
 	@Override
 	public void layerUnchecked(String layer) {
 		Log.d("MainMapActivity", "executing layerUnchecked, layer = " + layer);
 		activeLayers.put(layer, false);
 		dataContainer.hideLayer(layer);
 	}
 
 	@Override
 	public void mapItemAdded(Layer layer, MapItem mapItem) {
 		Log.d("MainMapActivity", "adding mapItem " + mapItem.getData()
 				+ " to layer " + layer.getName());
 		if (layer.getName().equals("notes")) {
 			if(googleMap!=null){
 			Marker marker = googleMap
 					.addMarker(new MarkerOptions()
 							.position(
 									new LatLng(mapItem.getPosition()
 											.getLatitude(), mapItem
 											.getPosition().getLongitude()))
 							.title(getString(R.string.layer_note))
 							.snippet(mapItem.getData())
 							.icon(BitmapDescriptorFactory
 									.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
 			// .icon(BitmapDescriptorFactory.fromResource(R.drawable.action_help)));
 			mapItemToMarker.put(mapItem, marker);
 			}
 		} else if (layer.getName().equals("images")) {
 			if(googleMap!=null){
 			Marker marker = googleMap
 					.addMarker(new MarkerOptions()
 							.position(
 									new LatLng(mapItem.getPosition()
 											.getLatitude(), mapItem
 											.getPosition().getLongitude()))
 							.title(getString(R.string.layer_image))
 							.snippet(mapItem.getData())
 							.icon(BitmapDescriptorFactory
 									.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
 			mapItemToMarker.put(mapItem, marker);
 			}
 		} else if (layer.getName().equals("videos")) {
 			if(googleMap!=null){
 			Marker marker = googleMap.addMarker(new MarkerOptions()
 					.position(mapItem.getPosition().getLatLng())
 					.title(getString(R.string.layer_video))
 					.snippet(mapItem.getData())
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
 			mapItemToMarker.put(mapItem, marker);
 			}
 		}
 	}
 
 	@Override
 	public void mapItemRemoved(Layer layer, MapItem mapItem) {
 		Log.d(this.toString(), "removing mapItem " + mapItem.getData()
 				+ " from layer " + layer.getName());
 		if (mapItemToMarker.containsKey(mapItem)) {
 			mapItemToMarker.get(mapItem).remove(); // remove Marker from map
 			mapItemToMarker.remove(mapItem);
 		}
 	}
 
 }
