 package com.gvccracing.android.tttimer.Tabs;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.gvccracing.android.tttimer.R;
 import com.gvccracing.android.tttimer.DataAccess.AppSettingsCP.AppSettings;
 import com.gvccracing.android.tttimer.DataAccess.RaceCP.Race;
 import com.gvccracing.android.tttimer.DataAccess.RaceInfoViewCP.RaceInfoResultsView;
 import com.gvccracing.android.tttimer.DataAccess.RaceInfoViewCP.RaceInfoView;
 import com.gvccracing.android.tttimer.DataAccess.RaceLocationCP.RaceLocation;
 import com.gvccracing.android.tttimer.DataAccess.RaceResultsCP.RaceResults;
 import com.gvccracing.android.tttimer.Dialogs.AdminAuthView;
 import com.gvccracing.android.tttimer.Dialogs.AdminMenuView;
 import com.gvccracing.android.tttimer.Dialogs.MarshalLocations;
 import com.gvccracing.android.tttimer.Dialogs.OtherRaceResults;
 import com.gvccracing.android.tttimer.Utilities.TimeFormatter;
 import com.gvccracing.android.tttimer.Utilities.Enums.RaceType;
 
 public class RaceInfoTab extends BaseTab implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
 
 	public static final String RaceInfoTabSpecName =  "RaceInfoTab";
 	
 	private static final int COURSE_RECORD_LOADER = 0x213;
 
 	private static final int APP_SETTINGS_LOADER_RACEINFO = 0x22;
 
 	private static final int RACE_INFO_LOADER = 0x114;
 	
 	private TextView raceDate;
 	private TextView raceCourseName;
 	private TextView raceType;
 	private TextView raceStartInterval;
 	private TextView raceDistance;
 	private TextView courseRecord;
 	private TextView raceLaps;
 	private LinearLayout llRaceLaps;
 	private String distanceUnit;
 	private String distance;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.tab_race_info, container, false);
         
         ((Button) view.findViewById(R.id.btnMarshalLocations)).setOnClickListener(this);
         ((Button) view.findViewById(R.id.btnPreviousResults)).setOnClickListener(this);
         ((Button) view.findViewById(R.id.btnAdminMenu)).setOnClickListener(this);
         
         raceDate = ((TextView) view.findViewById(R.id.raceDate));
         raceCourseName = ((TextView) view.findViewById(R.id.raceCourseName));
         raceType = ((TextView) view.findViewById(R.id.raceType));
         raceStartInterval = ((TextView) view.findViewById(R.id.raceStartInterval));
         raceDistance = ((TextView) view.findViewById(R.id.raceDistance));
         courseRecord = ((TextView) view.findViewById(R.id.courseRecord));
         raceLaps = ((TextView) view.findViewById(R.id.raceLaps));
         llRaceLaps = ((LinearLayout) view.findViewById(R.id.llRaceLaps));
         
         return view;
     }	
 	
 	@Override
 	public void onResume() {
 		super.onResume(); 
 		// Initialize the cursor loader for the race info
 		getActivity().getSupportLoaderManager().initLoader(RACE_INFO_LOADER, null, this);
 
 	    getActivity().getSupportLoaderManager().initLoader(APP_SETTINGS_LOADER_RACEINFO, null, this);
 	    
 	    getActivity().getSupportLoaderManager().initLoader(COURSE_RECORD_LOADER, null, this);
 	}
 
 	@Override
 	public String TabSpecName() {
 		return RaceInfoTabSpecName;
 	}
 
 	@Override
 	protected String LOG_TAG() {
 		return RaceInfoTabSpecName;
 	}
 
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {	
 		Log.i(LOG_TAG(), "onCreateLoader start: id=" + Integer.toString(id));
 		CursorLoader loader = null;
 		String[] projection;
 		String selection;
 		String[] selectionArgs = null;
 		String sortOrder;
 		switch(id){
 			case RACE_INFO_LOADER:
 				projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceDate, RaceLocation.CourseName, Race.RaceType, Race.StartInterval, RaceLocation.Distance, Race.NumLaps};
 				selection = Race.getTableName() + "." + Race._ID + "=" + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name);
 				selectionArgs = null;
 				sortOrder = Race.getTableName() + "." + Race._ID;
 				loader = new CursorLoader(getActivity(), RaceInfoView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 			case APP_SETTINGS_LOADER_RACEINFO:
 				projection = new String[]{AppSettings.AppSettingName, AppSettings.AppSettingValue};
 				selection = null;
 				sortOrder = null;
 				selectionArgs = null;
 				loader = new CursorLoader(getActivity(), AppSettings.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 			case COURSE_RECORD_LOADER:
 				projection = new String[]{RaceResults.getTableName() + "." + RaceResults._ID + " as _id", RaceResults.ElapsedTime};
 				selection = Race.getTableName() + "." + Race.RaceLocation_ID + " in (" + 
 							SQLiteQueryBuilder.buildQueryString(true, RaceInfoView.getTableName(), new String[]{Race.RaceLocation_ID}, 
 																Race.getTableName() + "." + Race._ID + "=" + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name), null, null, Race.getTableName() + "." + Race._ID, "1") + ")";
 				selectionArgs = null;
 				sortOrder = RaceResults.getTableName() + "." + RaceResults.ElapsedTime;
 				loader = new CursorLoader(getActivity(), RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 		}
 		Log.i(LOG_TAG(), "onCreateLoader complete: id=" + Integer.toString(id));
 		return loader;
 	}
 
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		try{
 			Log.i(LOG_TAG(), "onLoadFinished start: id=" + Integer.toString(loader.getId()));			
 			switch(loader.getId()){
 				case RACE_INFO_LOADER:
 					cursor.moveToFirst();
 					if(cursor.getCount() > 0){
 						Long raceDateMS = cursor.getLong(cursor.getColumnIndex(Race.RaceDate));
 						String courseName = cursor.getString(cursor.getColumnIndex(RaceLocation.CourseName));
 						long raceTypeID = cursor.getLong(cursor.getColumnIndex(Race.RaceType));
 						String raceTypeName = RaceType.DescriptionFromRaceTypeID(raceTypeID);
 						String startIntervalText = Long.toString(cursor.getLong(cursor.getColumnIndex(Race.StartInterval)));
 						long numRaceLaps = cursor.getLong(cursor.getColumnIndex(Race.NumLaps));
						distance = Float.toString(cursor.getFloat(cursor.getColumnIndex(RaceLocation.Distance)) * (float)numRaceLaps);						
 						
 						if(raceTypeID == 1){							
 							raceLaps.setText(Long.toString(numRaceLaps));
 							llRaceLaps.setVisibility(View.VISIBLE);
 						}else{
 							llRaceLaps.setVisibility(View.GONE);
 						}
 						
 						Date raceDateTemp = new Date(raceDateMS);
 						SimpleDateFormat formatter = new SimpleDateFormat("M/d/yy");
 						raceDate.setText(formatter.format(raceDateTemp).toString());
 						raceCourseName.setText(courseName);
 						raceType.setText(raceTypeName);
 						raceStartInterval.setText(startIntervalText);
 						SetDistance();
 					}
 					break;
 				case APP_SETTINGS_LOADER_RACEINFO:	
 					getActivity().getSupportLoaderManager().restartLoader(RACE_INFO_LOADER, null, this);
 					getActivity().getSupportLoaderManager().restartLoader(COURSE_RECORD_LOADER, null, this);
 
 					
 					Integer distanceUnitID = Integer.parseInt(AppSettings.ReadValue(getActivity(), AppSettings.AppSetting_DistanceUnits_Name, "0"));
 					distanceUnit = "mi";
 					switch(distanceUnitID){
 						case 0:
 							distanceUnit = "mi";
 							break;
 						case 1:
 							distanceUnit = "km";
 							break;
 						default:
 							distanceUnit = "mi";
 							break;
 					}
 					SetDistance();
 					break;	
 				case COURSE_RECORD_LOADER:
 					if(cursor != null && cursor.getCount() > 0){
 						cursor.moveToFirst();
 						long elapsedTime = cursor.getLong(cursor.getColumnIndex(RaceResults.ElapsedTime));
 						if (courseRecord != null) {
 				        	courseRecord.setText(TimeFormatter.Format(elapsedTime, true, true, true, true, true, false, false, false));
 				        }
 					}
 					break;
 			}
 			Log.i(LOG_TAG(), "onLoadFinished complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG(), "onLoadFinished error", ex); 
 		}
 	}
 
 	private void SetDistance()
 	{
 		if (distance != "" && distanceUnit == "")
 		{
 			raceDistance.setText(distance);
 		}
 		else if (distanceUnit != "" && distance != "")
 		{
 			distance += distanceUnit;
 			raceDistance.setText(distance);
 		}
 	}
 	
 	public void onLoaderReset(Loader<Cursor> loader) {
 		try{
 			Log.i(LOG_TAG(), "onLoaderReset start: id=" + Integer.toString(loader.getId()));
 			switch(loader.getId()){
 				case RACE_INFO_LOADER:
 					break;
 				case APP_SETTINGS_LOADER_RACEINFO:
 					break;
 				case COURSE_RECORD_LOADER:
 					break;
 			}
 			Log.i(LOG_TAG(), "onLoaderReset complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG(), "onLoaderReset error", ex); 
 		}
 	}
 
 	public void onClick(View v) {
         FragmentManager fm = getActivity().getSupportFragmentManager();
 		switch (v.getId())
 		{
 			case R.id.btnMarshalLocations:
 				showMarshalLocations(v);
 				break;
 			case R.id.btnPreviousResults:
 				showChoosePreviousRace();
 				break;
 			case R.id.btnAdminMenu:
 				if(Boolean.parseBoolean(AppSettings.ReadValue(getActivity(), AppSettings.AppSetting_AdminMode_Name, "false"))){
 					AdminMenuView adminMenuDialog = new AdminMenuView();
 					adminMenuDialog.show(fm, AdminMenuView.LOG_TAG);
 				}else{
 					AdminAuthView adminAuthDialog = new AdminAuthView();
 			        adminAuthDialog.show(fm, AdminAuthView.LOG_TAG);
 				}
 				break;
 		}
 	}
 
 	private void showChoosePreviousRace() {
 		OtherRaceResults previousResultsDialog = new OtherRaceResults();
 		FragmentManager fm = getParentActivity().getSupportFragmentManager();
 		previousResultsDialog.show(fm, OtherRaceResults.LOG_TAG);
 	}
 
 	private void showMarshalLocations(View v) {
 		MarshalLocations marshalLocationsDialog = new MarshalLocations();
 		FragmentManager fm = getParentActivity().getSupportFragmentManager();
 		marshalLocationsDialog.show(fm, MarshalLocations.LOG_TAG);
 	}
 }
