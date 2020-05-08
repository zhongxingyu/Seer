 package nanofuntas.fbl;
 
 import org.json.simple.JSONObject;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class SettingsFragment extends Fragment {
 	private final boolean DEBUG = true; 
 	private final String TAG = "SettingFragment";
 	
 	private EditText mTNameCreate = null;
 	private Button mCreateTeam = null;
 	private EditText mTNameJoin = null;
 	private Button mJoinTeam = null;	
 	private EditText mPNameIncruit = null;
 	private Button mIncruitPlayer = null;	
 	private EditText mYourName = null;
 	private EditText mYourPosition = null;
 	private Button mUpdateMyProfile = null;
 	
 	private TextView mTestSettings = null;
 	
 	private SharedPreferences settings = null;
 	private SharedPreferences.Editor editor = null;
 	
 	public SettingsFragment() {
     }
 
     public static final String ARG_SECTION_NUMBER = "section_number";
 
     @Override
     public void onStart(){
     	super.onStart();        	    	
     	initViews();
     	
     	settings = getActivity().getSharedPreferences(Config.FBL_SETTINGS, 0);
     	editor = settings.edit();
     	
     	final long UID = settings.getLong(Config.KEY_UID, 0);
     	
     	mCreateTeam.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if (DEBUG) Log.d(TAG, "Create Team clicked!");
 
 				String teamName = mTNameCreate.getText().toString();
 				long tid = ServerIface.createTeam(UID, teamName);
 				
 				editor.putLong(Config.KEY_TID, tid);
 				editor.commit();
 				
 				mTestSettings.setText("Team Created, TID:" + Long.toString(tid));
 			}    		
     	});
     	
     	mJoinTeam.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if (DEBUG) Log.d(TAG, "Join Team clicked!");
 				
 				String teamName = mTNameJoin.getText().toString();
 				long tid = ServerIface.joinTeam(UID, teamName);
 				
 				editor.putLong(Config.KEY_TID, tid);
 				editor.commit();
 				
 				mTestSettings.setText("You Joined, TID:" + Long.toString(tid));
 			}
     	});
     	
     	mIncruitPlayer.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if (DEBUG) Log.d(TAG, "Incruit Player clicked!");
				long tid = settings.getLong(Config.KEY_TID, 0);
 				
				if (tid <= 0) {
 					mTestSettings.setText("Please Join or Create Team First!");
 					return;
 				} 
 				
 				String playerName = mPNameIncruit.getText().toString();
				long uid = ServerIface.incruitPlayer(tid, playerName);
 
 				if (uid > 0) {
 					mTestSettings.setText("Player Incruited, UID:" + uid);
 				} else {
 					mTestSettings.setText("No such Player, UID:" + uid);
 				}
 			}    		
     	});
     	
     	mUpdateMyProfile.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if (DEBUG) Log.d(TAG, "Update Your Profile clicked!");
 				
 				String myName = mYourName.getText().toString();
 				String myPosition = mYourPosition.getText().toString();
 				
 				JSONObject myProfile = new JSONObject();
 				myProfile.put(Config.KEY_NAME, myName);
 				myProfile.put(Config.KEY_POSITION, myPosition);
 				
 				String result = ServerIface.updateMyProfile(UID, myProfile);
 				if (result.equals(Config.KEY_OK)) {
 					mTestSettings.setText("My Profile updated OK");
 				}
 			}    		
     	});
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {    	
     	View view = inflater.inflate(R.layout.fragment_settings, container, false);
         return view;
     }
 
     private void initViews() {
     	if (DEBUG) Log.d(TAG, "initViews()");
     	
     	mTNameCreate = (EditText) getView().findViewById(R.id.tname_create);
     	mCreateTeam = (Button) getView().findViewById(R.id.create_team);
     	mTNameJoin = (EditText) getView().findViewById(R.id.tname_join);
     	mJoinTeam = (Button) getView().findViewById(R.id.join_team);	
     	mPNameIncruit = (EditText) getView().findViewById(R.id.pname_incruit);
     	mIncruitPlayer = (Button) getView().findViewById(R.id.incruit_player);	
     	mYourName = (EditText) getView().findViewById(R.id.ur_name_update);
     	mYourPosition = (EditText) getView().findViewById(R.id.ur_position_update);
     	mUpdateMyProfile = (Button) getView().findViewById(R.id.update_my_profile);
     	
     	mTestSettings = (TextView) getView().findViewById(R.id.test_setting);
     }
     
 }
