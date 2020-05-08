 
 package se.mah.kd330a.project.find;
 
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.find.data.RoomDbHandler;
 import se.mah.kd330a.project.find.view.FragmentBuilding;
 import se.mah.kd330a.project.find.view.FragmentFloorMap;
 import se.mah.kd330a.project.find.view.FragmentResult;
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 //import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 //import android.widget.Toast;
 import android.widget.TextView.OnEditorActionListener;
 import android.database.Cursor;
 
 public class FragmentFind extends Fragment implements LoaderCallbacks<Cursor> {
 
 	private static final String FIND_SPINNER_STATE = "spinChoice";
 
 	private String selposFind = null;
 	private int spin_selected = -1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getLoaderManager().initLoader(1, null, this);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		ViewGroup rootView = (ViewGroup) inflater
 				.inflate(R.layout.fragment_screen_find, container, false);
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		Spinner spinnerFind = (Spinner) getView().findViewById(R.id.spinner_find_building);
 		ArrayAdapter<CharSequence> spinFindadapter = ArrayAdapter
 				.createFromResource(getActivity(), R.array.find_building_array,
 						android.R.layout.simple_spinner_item);
 		spinFindadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerFind.setAdapter(spinFindadapter);
 
 		spinnerFind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			/**
 			 * Called when a new item is selected (in the Spinner)
 			 */
 			public void onItemSelected(AdapterView<?> parent,
 					View view, int pos, long id) {
 
 				parent.getItemAtPosition(pos);
 				Resources res = getResources();
 				String[] findCode = res
 						.getStringArray(R.array.find_building_code_array);
 
 				selposFind = findCode[pos];
 				spin_selected = pos;
 
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 				// Do nothing, just another required interface callback
 			}
 		});
 
 		if (spin_selected > -1) 
 			spinnerFind.setSelection(spin_selected, true);
 		
 		Button btn_Search = (Button) getView().findViewById(R.id.button_find_navigation);
 		btn_Search.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				find_button_navigation(view);
 			}
 		});
 		
 		AutoCompleteTextView etRoomNr = (AutoCompleteTextView) getView().findViewById(R.id.editText_find_room);
 		etRoomNr.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_GO){
 					find_button_navigation(v);
 				}
 				return false;
 			}		
 		});
 	}
 
 	public void find_button_navigation(View v) {
 
 		// ---get the EditText view---
 		AutoCompleteTextView txt_room_code = (AutoCompleteTextView) getView().findViewById(R.id.editText_find_room);
 		// ---set the data to pass---
 		RoomDbHandler dbHandler;
 		String roomNr = selposFind + txt_room_code.getText().toString();
		String floorMapCode = selposFind + "_" + txt_room_code.getText().toString();
 
 		if (txt_room_code.length() == 0 && selposFind.length() > 0) {
 			showBuilding(selposFind);
 			//Toast.makeText(getActivity(), "to the building", Toast.LENGTH_SHORT).show();
 		}
 		if (roomNr.length() > 2) {
 			dbHandler = new RoomDbHandler(getActivity());
 
 			if (dbHandler.isRoomExistsNavigation(roomNr)) {
 				startNavigation(roomNr);
 			}
 			else if (dbHandler.isRoomExists(roomNr)) {
 				//go to floor maps
				showFloorMap(floorMapCode);
				Toast.makeText(getActivity(), "go to floor maps; roomNr: "+roomNr, Toast.LENGTH_LONG).show();
 			}
 			else
 				Toast.makeText(getActivity(), getString(R.string.find_db_error), Toast.LENGTH_LONG).show();
 		}
 		//Hiding the keyboard

 		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
 	}
 
 	private void showFloorMap(String floorMapCode) {
 		Fragment fragment = new FragmentFloorMap();
 		Bundle args = new Bundle();
 		args.putString(FragmentFloorMap.ARG_FLOORMAP, floorMapCode);
 		fragment.setArguments(args);
 
 		FragmentManager	 fragmentManager = getActivity().getSupportFragmentManager();
 
 		FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();	
 		fragmentTrans.replace(R.id.content_frame, fragment);
 		fragmentTrans.addToBackStack(null);
 		fragmentTrans.commit();		
 	}
 
 	private void showBuilding(String buildingCode) {
 
 		Fragment fragment = new FragmentBuilding();
 		Bundle args = new Bundle();
 		args.putString(FragmentBuilding.ARG_BUILDING, buildingCode);
 		fragment.setArguments(args);
 
 		FragmentManager	 fragmentManager = getActivity().getSupportFragmentManager();
 
 		FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();	
 		fragmentTrans.replace(R.id.content_frame, fragment);
 		fragmentTrans.addToBackStack(null);
 		fragmentTrans.commit();
 	}
 
 	private void startNavigation(String roomNr) {
 
 		Fragment fragment = new FragmentResult();
 		Bundle args = new Bundle();
 		args.putString(FragmentResult. ARG_ROOMNR, roomNr);
 		args.putInt(FragmentResult.ARG_BUILDINGPOS, spin_selected);
 		fragment.setArguments(args);
 
 		FragmentManager	 fragmentManager = getActivity().getSupportFragmentManager();
 
 		FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();	
 		fragmentTrans.replace(R.id.content_frame, fragment);
 		fragmentTrans.addToBackStack(null);
 		fragmentTrans.commit();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(FIND_SPINNER_STATE, spin_selected);
 	}
 
 	
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		//Log.i("project", "onActivityCreated");
 		super.onActivityCreated(savedInstanceState);
 		
 		if (savedInstanceState != null) {
 			//Log.i("project", "onActivityCreated save1 " + savedInstanceState.getInt(FIND_SPINNER_STATE));
 			spin_selected = savedInstanceState.getInt(FIND_SPINNER_STATE);
 		}
 
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
