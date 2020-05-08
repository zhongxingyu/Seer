 package org.foi.fmoed.fragments;
 
 import org.foi.fmoed.R;
 import org.foi.fmoed.activities.IdeasActivity;
 import org.foi.fmoed.adapters.GroupAdapter;
 import org.foi.fmoed.managers.DatabaseManager;
 import org.foi.fmoed.managers.SessionManager;
 import org.foi.fmoed.managers.SettingsManager;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * A dummy fragment representing a section of the app, but that simply
  * displays dummy text.
  */
 public class SectionFragment extends Fragment {
 
 	private int sectionNumber;
 	private View rootView;
 
 	/**
 	 * The fragment argument representing the section number for this
 	 * fragment.
 	 */
 	public static final String ARG_SECTION_NUMBER = "section_number";
 
 	SessionManager sessionManager;
 	SettingsManager settingsManager;
 	DatabaseManager databaseManager;
 
 	public SectionFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		if (sessionManager == null) {
 			sessionManager = new SessionManager(getActivity());
 		}
 		
 		if (databaseManager == null) {
 			databaseManager = new DatabaseManager(getActivity());
 		}
 
 		sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
 		settingsManager = new SettingsManager(getActivity());
 
 		if (sectionNumber == 1) {
 			rootView = inflater.inflate(R.layout.groups_list, container,
 					false);
 			ListView groupList = (ListView) rootView
 					.findViewById(R.id.group_list);
 			GroupAdapter groupAdapter = new GroupAdapter(getActivity());
 			groupList.setAdapter(groupAdapter);
 			
 		} else if (sectionNumber == 2) {
 			rootView = inflater.inflate(R.layout.create_group_layout,
 					container, false);
 			
 			ImageButton createGroupButton = (ImageButton) rootView.findViewById(R.id.create_group_button);
 			
 			createGroupButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					if (settingsManager.getUserName() != "error" 
 							&& !settingsManager.getUserName().equals("")){
 						IdeasActivity.resetStaticVariables();
 						EditText groupNameInput = (EditText) rootView.findViewById(R.id.editText1);
 						String sessionName = groupNameInput.getText().toString();
 						
 						// user nedd to type group name
 						if(sessionName != null && sessionName.length() == 0) {
 							Toast.makeText(getActivity(), 
 									"Please provide group name", 
 										Toast.LENGTH_SHORT).show();
 						} else {
 							sessionManager.startSession(sessionName);
 						}
 					} else {
 						Toast.makeText(getActivity(), 
 							"Please enter your username (Swipe left)", 
 								Toast.LENGTH_SHORT).show();
 					}
 				}
 			});
 
 		} else {
 			rootView = inflater.inflate(R.layout.settings, container,
 					false);
 			
 			final EditText username = (EditText) rootView.findViewById(R.id.username);
 			if (!settingsManager.getUserName().equals("error"))
 				username.setText(settingsManager.getUserName());
 			
 			ImageButton saveButton = (ImageButton) rootView.findViewById(R.id.save_button);
 			saveButton.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
					if (!username.getText().toString().trim().equals("")) {
						settingsManager.setUserName(username.getText().toString().trim());
 						Toast.makeText(getActivity(), "Username saved.", Toast.LENGTH_SHORT).show();
 					} else {
 						Toast.makeText(getActivity(), "Please enter a valid username", Toast.LENGTH_SHORT).show();
 					}
 				}
 			});
 		}
 
 		return rootView;
 	}
 }
