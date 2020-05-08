 package com.localsocial.localtweets;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import com.localsocial.Device;
 import com.localsocial.JSONException;
 import com.localsocial.LocalSocial;
 import com.localsocial.LocalSocialFactory;
 import com.localsocial.LoggerFactory;
 import com.localsocial.Neighbourhood;
 import com.localsocial.Platform;
 import com.localsocial.config.SimpleAppConfiguration;
 import com.localsocial.model.Network;
 import com.localsocial.oauth.AccessToken;
 import com.localsocial.oauth.OAuthConsumer;
 import com.localsocial.oauth.RequestToken;
 import com.localsocial.oauth.Verifier;
 import com.localsocial.proximity.observers.NeighbourhoodObserver;
 import com.localsocial.proximity.observers.NeighbourhoodObserverAdapter;
 import com.localsocial.remote.exception.LocalSocialError;
 import com.localsocial.remote.exception.LocalSocialException;
 import com.localsocial.remote.exception.UnauthorizedException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends ListActivity {
 
     // logging
     private boolean d = true;
     private final String TAG = getClass().getName();
 
     // listview
     private LayoutInflater mInflater;
     //private Vector<String> devices = new Vector<String>();
     private Vector<LocalTweetsItem> devices = new Vector<LocalTweetsItem>();
     private CustomAdapter adapter;
 
     // executor
     protected final Executor m_executor = Executors.newCachedThreadPool();
 
     // handler
     protected final Handler m_handler = new Handler();
 
     // dialog codes
     private static final int REQUEST_ENABLE_BT = 0;
     private static final int REQUEST_CODE_AUTH_TWITTER = 1;
 
     private static final int DIALOG_FINISH_NO_BT = 2;
     private static final int DIALOG_FINISH_AUTH_ERROR = 3;
 
     // Twitter
     public Network twitter;
     public boolean triedNetwork = false;
 
     // LocalSocial
     private LocalSocial m_localsocial;
     private NeighbourhoodObserver m_nobby = null;
     private Neighbourhood m_neighbourhood;
 
     // LocalSocial config
     private final static String CALLBACK = "LocalTweets://callback";
     private final static String NAME = "LocalTweets";
     private final static String KEY = "UpLHN9YbMcBHMVnWNcyouev0LegFdGlPsaA0S4b9";
     private final static String SECRET = "RqMe7ntR7hxgNomZqxq1FcgEWEmAQRtNAAcBtbiz";
     private SimpleAppConfiguration m_sac = new SimpleAppConfiguration(CALLBACK, NAME, KEY, SECRET, null);
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         adapter = new CustomAdapter(this, R.layout.main, R.id.device, devices);
         setListAdapter(adapter);
         getListView().setTextFilterEnabled(true);
 
         // *** LocalSocial Config ***
         Platform platform = new Platform();
         platform.setContext(getApplication());
         LoggerFactory.load(platform);
         m_sac.setPlatformContext(platform);
         LocalSocialFactory.setDefaultConfig(LocalSocialFactory.populate(m_sac));
         m_localsocial = LocalSocialFactory.getLocalSocial();
 
         if (d) Log.d(TAG, "LocalSocial configured");
         // ***
 
     }
 
     public void onListItemClick(ListView parent, View v, int position, long id) {
         CustomAdapter adapter = (CustomAdapter) parent.getAdapter();
         LocalTweetsItem lt_item = adapter.getItem(position);
 
         if (null != lt_item.getNetwork()) {
             Uri uri = Uri.parse("http://mobile.twitter.com/" + lt_item.getTitle());
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             startActivity(intent);
         } else {
             Toast.makeText(this, String.format(getString(R.string.toast_no_twitter_for_device), lt_item.getTitle()), Toast.LENGTH_LONG).show();
         }
     }
 
     private class CustomAdapter extends ArrayAdapter<LocalTweetsItem> {
 
         public CustomAdapter(Context context, int resource,
                              int textViewResourceId, List<LocalTweetsItem> lt_items) {
             super(context, resource, textViewResourceId, lt_items);
 
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder = null;
 
             TextView device_name = null;
             LocalTweetsItem lt_item = getItem(position);
 
             if (null == convertView) {
                 convertView = mInflater.inflate(R.layout.item, null);
                 holder = new ViewHolder(convertView);
                 convertView.setTag(holder);
             }
 
             holder = (ViewHolder) convertView.getTag();
             device_name = holder.getItem();
             device_name.setText(lt_item.getName());
 
             Log.d("Device", "Trying to get name..." + lt_item.getName() + " of the device " + lt_item.getDevice().getAddress());
 
             return convertView;
         }
     }
 
     /**
      * Wrapper for row data.
      */
     private class ViewHolder {
         private View row;
         private TextView item = null;
 
         public ViewHolder(View row) {
             this.row = row;
         }
 
         public TextView getItem() {
             if (null == item) {
                 item = (TextView) row.findViewById(R.id.device);
             }
             return item;
         }
     }
 
     @Override
     protected void onStart() {
         Log.i(TAG, "onStart()");
         super.onStart();
         bootstrap();
     }
 
     @Override
     protected void onPause() {
         Log.i(TAG, "onPause()");
         stopScan();
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         Log.i(TAG, "onResume()");
         super.onResume();
     }
 
 
     /**
      * Called when a launched activity exits
      */
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_ENABLE_BT:
                 if (resultCode != RESULT_OK || !bluetoothEnabled()) {
                     showDialog(DIALOG_FINISH_NO_BT);
                     return;
                 }
                 bootstrap();
                 break;
             case REQUEST_CODE_AUTH_TWITTER:
                 Log.d(TAG, "Auth twitter result = " + resultCode);
                 if (resultCode == RESULT_OK) {
                     Log.d(TAG, "Network added");
                     String location = getLocation(data.getStringExtra("result"));
                     loadAndPublishNetwork(location);
                     if (d) Log.d(TAG, "Twitter account name = " + twitter.m_name);
                 } else if (resultCode == RESULT_CANCELED) {
                     Log.d(TAG, "Auth cancelled");
                     finishAuthFailed(this.getString(R.string.twitter_auth_cancelled));
                 } else if (resultCode == TwitterAuthActivity.RESULT_LOCALSOCIAL_ERROR) {
                     Log.d(TAG, "LocalSocial Error :-/");
                     finishAuthFailed(this.getString(R.string.twitter_auth_fail));
                 } else {
                     Log.d(TAG, "Auth failed unknown code : " + resultCode);
                     finishAuthFailed(this.getString(R.string.twitter_auth_fail));
                 }
                 break;
             default:
                 break;
         }
     }
 
     private void finishAuthFailed(String msg) {
         triedNetwork = true;
         Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
         // TODO is it the right thing to do?
         startScan();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     /**
      * Called when a menu item is clicked
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.discovery_status:
                 Intent discoverableIntent = new
                         Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                 discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                 startActivity(discoverableIntent);
                 break;
             case R.id.scan_status:
                 startScan();
                 break;
         }
         return true;
     }
 
     /**
      * Dialogs
      *
      * @param id dialog id
      */
     protected Dialog onCreateDialog(int id) {
         Dialog dialog;
         switch (id) {
             case DIALOG_FINISH_NO_BT:
                 dialog = createRetryExitDialog(this.getString(R.string.no_bluetooth_warning));
                 break;
             case DIALOG_FINISH_AUTH_ERROR:
                 dialog = createRetryExitDialog(this.getString(R.string.ls_auth_error));
                 break;
             default:
                 Log.e(TAG, "Unknown dialog : " + id);
                 dialog = null;
         }
         return dialog;
     }
 
     private AlertDialog createRetryExitDialog(String msg) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(msg)
                 .setCancelable(false)
                 .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         bootstrap();
                     }
                 })
                 .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         finish();
                     }
                 });
         return builder.create();
     }
 
     /**
      * LocalSocial bootstrap
      */
     protected void bootstrap() {
         if (d) Log.d(TAG, "bootstrap");
         if (!bluetoothEnabled()) {
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         } else {
             if (isOnline()) {
                 if (!authorised()) {
                     if (d) Log.d(TAG, "not authorised");
                     try {
                         if (d) Log.d(TAG, "trying to load access token");
                         m_localsocial.loadAccessToken();
                     } catch (UnauthorizedException e) {
                         if (d) Log.d(TAG, "notauth exception");
                         e.printStackTrace();
                     }
                     if (!authorised()) {
                         authorise();
                         return;
                     }
                 }
                 boolean forceReAuth = false; //For Testing
                 if ((forceReAuth || null == twitter) && !triedNetwork) {
                     doGetMyTwitter(forceReAuth);
                     return;
                 }
                 startScan();
             } else {
                 Toast.makeText(this, this.getString(R.string.toast_offline_warning), Toast.LENGTH_LONG).show();
             }
         }
     }
 
     /*
     * Check if authorised with LocalSocial
     */
 
     private boolean authorised() {
         try {
             return m_localsocial.getAccessToken() != null;
         } catch (UnauthorizedException e) {
             return false;
         }
     }
 
     /**
      * Check if a device supports Bluetooth
      *
      * @return true if device provides Bluetooth support and it is enabled
      */
     private boolean bluetoothEnabled() {
         BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBluetoothAdapter == null) {
             throw new LocalSocialError("no bluetooth support");
         }
         return mBluetoothAdapter.isEnabled();
     }
 
     /**
      * Check if a device is connected to a network
      *
      * @return true if connected, false otherwise
      */
     public boolean isOnline() {
         ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         return (netInfo != null && netInfo.isConnectedOrConnecting());
     }
 
     /**
      * Authorise with LocalSocial
      */
     private void authorise() {
         doAuthorise();
     }
 
     /**
      * Authorise with LocalSocial
      */
     public void doAuthorise() {
         m_executor.execute(new Runnable() {
             public void run() {
                 try {
                     OAuthConsumer m_consumer = m_localsocial.getOAuthConsumer();
                     RequestToken rt = m_consumer.generateRequestToken();
                     Verifier v = m_consumer.authorise(rt);
                     AccessToken at = m_consumer.exchange(rt, v);
                     LocalSocial.Credentials creds = LocalSocialFactory.createCredentials(at);
                     m_localsocial.saveAccessToken(at);
                     m_localsocial.getConfig().setCredentials(creds);
                     m_handler.post(new Runnable() {
                         public void run() {
                             bootstrap();
                         }
                     });
                 }
                 catch (Exception e) {
                     //ToDo Need to handle ls runtime errors fuck sake
                     e.printStackTrace();
                 }
             }
         });
     }
 
     private void startScan() {
         Log.d(TAG, "startScan");
         if (null != m_localsocial) {
             m_localsocial.getNeighbourhood().observeNeighbourhood(getNeighbourhoodObserver());
             m_localsocial.getNeighbourhood().startScan();
         }
     }
 
     private void stopScan() {
         Log.d(TAG, "stopScan");
         if (null != m_localsocial) {
             m_localsocial.getNeighbourhood().removeObserver(getNeighbourhoodObserver());
             m_localsocial.getNeighbourhood().stopScan();
         }
     }
 
     private void restartScan() {
         Log.d(TAG, "restartScan");
         if (null != m_localsocial && !m_localsocial.getNeighbourhood().isCurrentlyScanning()) {
             stopScan();
             startScan();
         }
     }
 
     /*
      * NeighbourhoodObserver
      */
 
     protected NeighbourhoodObserver getNeighbourhoodObserver() {
         if (null == m_nobby) {
             m_nobby = new NeighbourhoodObserverAdapter() {
                 final Map<String, Device> m_inRange = new HashMap<String, Device>();
 
                 @Override
                 public void discovered(final Device device) {
 
                     if (d) Log.d("Scan", "discovered : " + device.getAddress());
 
                     device.setTimeout(60000);
                     synchronized (m_inRange) {
                         String mac = device.getAddress();
                         if (!m_inRange.containsKey(mac)) {
                             m_inRange.put(mac, device);
                             m_executor.execute(new Runnable() {
                                 public void run() {
                                     try {
                                         getNetworkInfo(device);
                                     } catch (LocalSocialError lse) {
                                         lse.printStackTrace();
                                     } catch (LocalSocialException e) {
                                         e.printStackTrace();
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }
                                 }
                             });
 
                             LocalTweetsItem lt_item = new LocalTweetsItem(device);
                             try {
                                 Network try_twitter = getTwitterInfo(device.getAddress());
                                 lt_item.setNetwork(try_twitter);
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
 
                             if (d) Log.d("Device", "discovered " + lt_item.getDevice().getAddress());
 
                             devices.add(lt_item);
                             adapter.notifyDataSetChanged();
                         }
                     }
                 }
 
                 @Override
                 public void scanModeChanged(String address, int mode) {
                     if (d) Log.d("Scan", "scan mode changed to " + mode + " for " + address);
                 }
 
                 @Override
                 public void inRange(Device device) {
                     if (d) Log.d("Scan", "inRange : " + device.getAddress());
                 }
 
                 @Override
                 public void outOfRange(final Device device) {
                     if (d) Log.d("Scan", "outOfRange : " + device.getAddress());
 
                     LocalTweetsItem to_remove = null;
                     Iterator<LocalTweetsItem> it = devices.iterator();
                     while (it.hasNext()) {
                         LocalTweetsItem curr = it.next();
                         if (curr.getDevice().getAddress() == device.getAddress()) {
                             to_remove = curr;
                         }
                     }
 
                     m_inRange.remove(device.getAddress());
                     devices.remove(to_remove);
                     adapter.notifyDataSetChanged();
                 }
             };
         }
         return m_nobby;
     }
 
 
     // Twitter related
 
     /**
      * Starts TwitterAuthActivity
      */
     private void startTwitterAuth() {
         Intent intent = new Intent(MainActivity.this, TwitterAuthActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         intent.putExtra("URL", "/auth/twitter");
         intent.putExtra("NETWORK", "twitter");
         intent.putExtra("API", m_localsocial.getConfig().getBase());
         startActivityForResult(intent, REQUEST_CODE_AUTH_TWITTER);
     }
 
     private String getLocation(String u) {
         URI uri = URI.create(u);
         String t = uri.getPath();
         if (uri.getQuery() != null) {
             t += "?" + uri.getQuery();
         }
         return t;
     }
 
     public void doGetMyTwitter(final boolean forceReAuth) {
         m_executor.execute(new Runnable() {
             public void run() {
                 try {
                     getMyTwitter(forceReAuth);
                 } catch (UnauthorizedException e) {
                     Log.e(TAG, "error" + e.getMessage());
                 } catch (IOException e) {
                     Log.e(TAG, "error" + e.getMessage());
                 }
             }
         });
     }
 
     public void getMyTwitter(final boolean forceReAuth) throws IOException {
         triedNetwork = true;
         if (!forceReAuth) {
             try {
                 twitter = m_localsocial.getRemoteFactory().getTulsiRemote().getNetwork("Twitter", m_localsocial.getNeighbourhood().getAddress());
             } catch (Throwable t) {
                 Log.e(TAG, "Error getting twitter : " + t.getMessage());
             }
         }
 
        if (d && null != twitter) {
            Log.d("temp", "Twitter Name = " + twitter.getName());
        }
 
         m_handler.post(new Runnable() {
             public void run() {
                 if (forceReAuth || null == twitter) {
                     startTwitterAuth();
                 } else {
                     bootstrap();
                 }
             }
         });
     }
 
     private void loadAndPublishNetwork(final String location) {
         m_executor.execute(new Runnable() {
 
             public void run() {
                 try {
                     twitter = m_localsocial.getRemoteFactory().getNetworkRemote().getNetworkFromLocation(location);
                     if (null != twitter) {
                         m_localsocial.getRemoteFactory().getTulsiRemote().publish(twitter,
                                 m_localsocial.getNeighbourhood().getAddress());
                     }
                 } catch (Throwable t) {
                     t.printStackTrace();
                 } finally {
                     triedNetwork = true;
                 }
                 m_handler.post(new Runnable() {
                     public void run() {
                         bootstrap();
                     }
                 });
             }
         });
     }
 
     /**
      * Get network info from a device found via Bluetooth scan
      *
      * @param device
      * @throws IOException
      * @throws JSONException
      */
     private void getNetworkInfo(final Device device) throws IOException, JSONException {
 //        Network network = getTwitterInfo(device.getAddress());
 //        final TweetAroundItem tai = new TweetAroundItem(device, network);
 //        m_handler.post(new Runnable() {
 //            public void run() {
 //                if (!(m_prefs.getBoolean("twitOnlyPref", false) && null == tai.network)) {
 //                    addTweetAroundItem(tai, null);
 //                }
 //            }
 //        });
     }
 
     /**
      * Get device's Twitter network info
      *
      * @param macAddress MAC address of a device found via Bluetooth scan
      * @return
      * @throws IOException
      * @throws JSONException
      */
     private Network getTwitterInfo(String macAddress) throws IOException, JSONException {
         Log.d(TAG, "MainActivity.getTwitterInfo");
         Network n = null;
         try {
             n = m_localsocial.getRemoteFactory().getTulsiRemote().getNetwork("Twitter", macAddress);
         } catch (Throwable t) {
             Log.d(TAG, "Error getting twitter : " + t.getMessage());
         }
         return n;
     }
 
 
 }
