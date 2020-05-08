 package org.kevinvalk.hce;
 
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.kevinvalk.hce.applet.passport.Passport;
 import org.kevinvalk.hce.applet.passport.PassportApplet;
 import org.kevinvalk.hce.framework.Applet;
import org.kevinvalk.hce.framework.TagWrapper;
import org.kevinvalk.hce.framework.HceFramework;
 
 import android.app.ActionBar;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener {
 
 	// NFC HCE
 	private PassportApplet passportApplet = null;
 	private Passport passport = null;
 	private HceFramework framework = null;
 	
 	// Settings
 	private NfcAdapter adapter;
     private PendingIntent pendingIntent;
     private IntentFilter[] filters;
     private String[][] techLists;
     private WakeLock wakeLock;
     private PowerManager powerManager;
 	private static final String TECH_ISO_PCDA = "android.nfc.tech.IsoPcdA";
 	
 	private static final String TAG = "HCE";
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current dropdown position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	
 	private void initFramework()
 	{
 		// Setup my applets
 		if (passport == null)
 			passport = new Passport("L898902C<", "690806", "940623");
 		if (passportApplet == null)
 			passportApplet = new PassportApplet(passport);
 		
 		// Enable NFC HCE and register our applets
 		if (framework == null)
 			framework = new HceFramework();
 		framework.register(passportApplet);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
         // Get power management
         powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		// Set up the dropdown list navigation in the action bar.
 		actionBar.setListNavigationCallbacks(
 		// Specify a SpinnerAdapter to populate the dropdown list.
 				new ArrayAdapter<String>(actionBar.getThemedContext(),
 						android.R.layout.simple_list_item_1,
 						android.R.id.text1, new String[] {
 								getString(R.string.title_section1),
 								getString(R.string.title_section2),
 								getString(R.string.title_section3), }), this);
 		
         // Fix adapter settings
         adapter = NfcAdapter.getDefaultAdapter(this);
         adapter.setNdefPushMessage(null, this);
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
             adapter.setBeamPushUris(null, this);
         
         // Setup our framework
         initFramework();
         
         // Register new tech
         pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
         filters = new IntentFilter[] {new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
         techLists = new String[][] { { TECH_ISO_PCDA } };
         
         // Force intent
         Intent intent = getIntent();
         String action = intent.getAction();
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
             handleTag(intent);
 	}
 	
     @Override
     public void onNewIntent(Intent intent)
     {
         handleTag(intent);
     }
     
     private void handleTag(Intent intent)
     {       
         try {
             Tag tag = null;
             if (intent.getExtras() != null) {
                 tag = (Tag) intent.getExtras().get(NfcAdapter.EXTRA_TAG);
             }
             if (tag == null)
             {
                 return;
             }
             
             
             List<String> techList = Arrays.asList(tag.getTechList());
             if (!techList.contains(TECH_ISO_PCDA)) {
                 return;
             }
 
             TagWrapper tw = new TagWrapper(tag, TECH_ISO_PCDA);
             if (!tw.isConnected())
                 tw.connect();
             
             // Let the framework handle the tag
             if (! framework.handleTag(tw))
             {
             	Log.w(TAG, "Failed to handle the tag");
             }
             
         }
         catch (Exception e)
         {
             throw new RuntimeException(e);
         }
     }
 
     @Override
 	public void onResume()
 	{
     	super.onResume();
     	
     	//if (wakeLock != null)
     		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getString(R.string.app_name));
         wakeLock.acquire();
         
 		if (adapter != null)
 			adapter.enableForegroundDispatch(this, pendingIntent, filters,techLists);
 	}
     
     @Override
     public void onPause()
 	{
 		super.onPause();
 		if (adapter != null)
 			adapter.disableForegroundDispatch(this);
 		
         if (wakeLock != null)
             wakeLock.release();
 	}
     
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current dropdown position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current dropdown position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int position, long id) {
 		// When the given dropdown item is selected, show its contents in the
 		// container view.
 		Fragment fragment = new DummySectionFragment();
 		Bundle args = new Bundle();
 		args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
 		fragment.setArguments(args);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.container, fragment).commit();
 		return true;
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
 					container, false);
 			TextView dummyTextView = (TextView) rootView
 					.findViewById(R.id.section_label);
 			dummyTextView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 			return rootView;
 		}
 	}
 
 }
