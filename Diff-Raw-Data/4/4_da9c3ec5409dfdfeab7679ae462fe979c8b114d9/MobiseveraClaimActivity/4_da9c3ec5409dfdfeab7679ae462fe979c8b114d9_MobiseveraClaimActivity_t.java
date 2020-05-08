 package com.digitalfingertip.mobisevera.activity;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.digitalfingertip.mobisevera.MobiseveraCommsUtils;
 import com.digitalfingertip.mobisevera.MobiseveraConstants;
 import com.digitalfingertip.mobisevera.MobiseveraContentStore;
 import com.digitalfingertip.mobisevera.MobiseveraNaviAdapter;
 import com.digitalfingertip.mobisevera.MobiseveraNaviContainer;
 import com.digitalfingertip.mobisevera.R;
 import com.digitalfingertip.mobisevera.S3CaseItem;
 import com.digitalfingertip.mobisevera.S3PhaseItem;
 import com.digitalfingertip.mobisevera.S3WorkTypeItem;
 
 /**
  * This activity is used to pick the amount of time the user want to claim and launch subactivities to
  * get the project, phase and work type
  * @author juha
  *
  */
 
 public class MobiseveraClaimActivity extends Activity implements OnClickListener, OnItemClickListener {
 
 	/**
 	 * Containers for selected project, phase and worktype.
 	 */
 	
 	private S3CaseItem selectedCase = null;
 	private S3PhaseItem selectedPhase = null;
 	private S3WorkTypeItem selectedWorkType = null;
 	
 	private String selectedDescription = null;
 	
 	MobiseveraNaviAdapter listAdapter = null;
 	
 	public static final String TAG = "Sevedroid";
 	private static final int PROJECT_NAVI_INDEX = 0;
 	private static final int PHASE_NAVI_INDEX = 1;
 	private static final int WORKTYPE_NAVI_INDEX = 2;
 	private static final int DESCRIPTION_NAVI_INDEX = 3;
 	private static final int DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE = 0;
 	private static final int DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE = 1;
 	private static final int DIALOG_ID_FAILED_HOURS_PUBLISHING = 2;
 	private static final int DIALOG_ID_SUCCEEDED_HOURS_PUBLISHING = 3;
 	private static final int DIALOG_ID_NOT_CONNECTED = 4;
 	private static final int DIALOG_ID_MISSING_DESCRIPTION = 5;
 	private static final int DIALOG_ID_BAD_EVENT_DATE = 6;
 	private static final int DIALOG_ID_MISSING_PHASE_GUID = 7;
 	private static final int DIALOG_ID_BAD_HOURS_QUANTITY = 8;
 	private static final int DIALOG_ID_BAD_USER_GUID = 9;
 	private static final int DIALOG_ID_BAD_WORKTYPE_GUID = 10;
 	// Parcel id's for saving instance state
 	protected static final String CASEITEMLIST_PARCEL_ID = "caseItemParcelID";
 	protected static final String PHASEITEMLIST_PARCEL_ID = "phaseItemParcelID";
 	protected static final String WORKTYPEITEMLIST_PARCEL_ID = "workTypeItemParcelID";
 	protected static final String DESCRIPTION_PARCEL_ID = "descriptionParcelID";
 	protected static final String HOUR_PARCEL_ID = "hourParcelID";
 	protected static final String MINUTE_PARCEL_ID = "minuteParcelID";
 	/**
 	 * Current hour as selected by the user for claiming 
 	 */
 	private int mHour = 0;
 	/**
 	 * Current minute as selected byt the user for claiming
 	 */
 	private int mMinute = 0;
 	
 	/**
 	 * Field used to post the status of the publish of the hour claim to S3.
 	 */
 	private boolean hourEntryStatus;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d(TAG,"OnCreate called on MobiseveraClaimActivity");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_claim);
 		if(savedInstanceState == null) {
 			Log.d(TAG,"Instance state is null, recreating activity.");
 		} else {
 			Log.d(TAG,"Instance state not, null, unparcelling.");
 			this.selectedCase = (S3CaseItem)savedInstanceState.get(CASEITEMLIST_PARCEL_ID);
 			this.selectedPhase = (S3PhaseItem)savedInstanceState.get(PHASEITEMLIST_PARCEL_ID);
 			this.selectedWorkType = (S3WorkTypeItem)savedInstanceState.get(WORKTYPEITEMLIST_PARCEL_ID);
 			this.selectedDescription = (String)savedInstanceState.getString(DESCRIPTION_PARCEL_ID);
 			this.mHour = (int)savedInstanceState.getInt(HOUR_PARCEL_ID);
 			this.mMinute = (int)savedInstanceState.getInt(MINUTE_PARCEL_ID);
 		}
 		TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
 		Button showHourWidgetButton = (Button)findViewById(R.id.showHourWidgetButton);
 		Button submitClaimButton = (Button)findViewById(R.id.submitClaimButton);
 		EditText descEditText = (EditText)findViewById(R.id.descriptionEditText);
 		descEditText.setVisibility(View.GONE);
 		moveTimeFromPickerToButton(mHour, mMinute);
 		showHourWidgetButton.setOnClickListener(this);
 		submitClaimButton.setOnClickListener(this);
 		claimTimePicker.setIs24HourView(true);
 		claimTimePicker.setVisibility(View.GONE);
         updateNaviTitles();		
 	}
 	
 	
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putParcelable(CASEITEMLIST_PARCEL_ID, selectedCase);
 		outState.putParcelable(PHASEITEMLIST_PARCEL_ID, selectedPhase);
 		outState.putParcelable(WORKTYPEITEMLIST_PARCEL_ID, selectedWorkType);
 		outState.putString(DESCRIPTION_PARCEL_ID, selectedDescription);
 		outState.putInt(HOUR_PARCEL_ID,mHour);
 		outState.putInt(MINUTE_PARCEL_ID,mMinute);
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.showHourWidgetButton) {
 			//If timepicker is invisible, then make it visible, if it is visible, then
 			//get the shown date to be displayed in the button and make datepicker go gone.
 			Log.d(TAG,"Show time widget button clicked!");
 			TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
 			if(claimTimePicker.getVisibility() == View.GONE) {
 				Log.d(TAG,"Moving time from button to picker and making picker visible.");
 				moveTimeFromButtonToTimePicker();
 				claimTimePicker.setVisibility(View.VISIBLE);
 				return;
 			} else if(claimTimePicker.getVisibility() == View.VISIBLE);
 				Log.d(TAG,"Moving time from picker to button and hiding picker.");
 				moveTimeFromPickerToButton(claimTimePicker.getCurrentHour(),claimTimePicker.getCurrentMinute());
 				claimTimePicker.setVisibility(View.GONE);
 				return;
 		} else if(v.getId() == R.id.submitClaimButton) {
 			Log.d(TAG,"Starting to publish hours.");
 			if(MobiseveraCommsUtils.checkIfConnected(this) == false) {
 				showDialog(DIALOG_ID_NOT_CONNECTED);
 				return;
 			}
 			//TODO:Critical: Should check that user does not try to claim to inactive cases/phases/work types!
 			String description = selectedDescription;
 			if(description == null || description.isEmpty()) {
 				showDialog(DIALOG_ID_MISSING_DESCRIPTION);
 				return;
 			}
 			Calendar claimDate = Calendar.getInstance();
 			String eventDate = MobiseveraConstants.S3_DATE_FORMATTER.format(claimDate.getTime());
 			if(eventDate == null || eventDate.isEmpty()) {
 				showDialog(DIALOG_ID_BAD_EVENT_DATE);
 				return;
 			}
 			if(selectedPhase == null || selectedPhase.getPhaseGUID() == null || selectedPhase.getPhaseGUID().isEmpty()) {
 				showDialog(DIALOG_ID_MISSING_PHASE_GUID);
 				return;
 			}
 			String phaseGuid = selectedPhase.getPhaseGUID();
 			
 			String hours = ((EditText)findViewById(R.id.hours_amount)).getText().toString();
 			String minutes = ((EditText)findViewById(R.id.minutes_amount)).getText().toString();
 			String quantity = hours+"."+Math.round((Integer.parseInt(minutes))/0.6);
 			if(quantity == null || quantity.isEmpty()) {
 				showDialog(DIALOG_ID_BAD_HOURS_QUANTITY);
 				return;
 			}
 			MobiseveraContentStore scs = new MobiseveraContentStore(this);
 			String userGuid = scs.fetchUserGUID();
 			if(userGuid == null || userGuid.isEmpty()) {
 				showDialog(DIALOG_ID_BAD_USER_GUID);
 				return;
 			}
 			if(selectedWorkType == null || selectedWorkType.getWorkTypeGUID() == null || selectedWorkType.getWorkTypeGUID().isEmpty()) {
 				showDialog(DIALOG_ID_BAD_WORKTYPE_GUID);
 				return;
 			}
 			String workTypeGuid = selectedWorkType.getWorkTypeGUID();
 			String [] params = {description, eventDate, phaseGuid, quantity, userGuid, workTypeGuid};
 			new PublishHourEntryTask(this).execute(params);
 		}
 	}
 	
 	
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		String message = "";
 		switch(id) {
 		case DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE:
 			message = getString(R.string.dialog_text_select_phase_before_worktype);
 			break;
 		case DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE:
 			message = getString(R.string.dialog_text_select_project_before_phase);
 			break;
 		case DIALOG_ID_FAILED_HOURS_PUBLISHING:
 			message = getString(R.string.dialog_text_hours_publish_failed);
 			break;
 		case DIALOG_ID_SUCCEEDED_HOURS_PUBLISHING:
 			message = getString(R.string.dialog_text_hours_publish_ok);
 			break;
 		case DIALOG_ID_NOT_CONNECTED:
 			message = getString(R.string.dialog_text_hours_publish_ok);
 			break;
 		case DIALOG_ID_MISSING_DESCRIPTION:
 			message = getString(R.string.dialog_text_missing_description);
 			break;
 		case DIALOG_ID_BAD_EVENT_DATE:
 			message = getString(R.string.dialog_text_bad_event_date);
 			break;
 		case DIALOG_ID_MISSING_PHASE_GUID:
 			message = getString(R.string.dialog_text_missing_phase_guid);
 			break;
 		case DIALOG_ID_BAD_HOURS_QUANTITY:
 			message = getString(R.string.dialog_text_bad_hours_quantity);
 			break;
 		case DIALOG_ID_BAD_USER_GUID:
 			message = getString(R.string.dialog_text_bad_used_guid);
 			break;
 		case DIALOG_ID_BAD_WORKTYPE_GUID:
 			message = getString(R.string.dialog_text_bad_worktype_guid);
 			break;
 		} 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(message)
 			.setCancelable(false)
 			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					return;
 				}
 			});
 		AlertDialog alert = builder.create();
 		return alert;	
 	}
 
 	private void moveTimeFromButtonToTimePicker() {
 		TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
 		claimTimePicker.setCurrentHour(mHour);
 		claimTimePicker.setCurrentMinute(mMinute);
 	}
 
 	/**
 	 * Move hour and minutedisplay from datepicker to a button value, while maintaining the
 	 * hour and minute values as instance parameters to this activity.
 	 * @param currentHour
 	 * @param currentMinute
 	 */
 
 	private void moveTimeFromPickerToButton(Integer currentHour,
 			Integer currentMinute) {
 		Button showHourWidgetButton = (Button)this.findViewById(R.id.showHourWidgetButton);
 		this.mHour = currentHour;
 		this.mMinute = currentMinute;
 		String hourLabel = getString(R.string.hour_label);
 		String minutesLabel = getString(R.string.minute_label);
 		showHourWidgetButton.setText(""+mHour+hourLabel+" "+mMinute+minutesLabel);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Log.d(TAG,"OnItemClick called for MobiseveraClaimActivity. Click on pos: "+position);
 		Intent newIntent = MobiseveraNaviContainer.getIntentForNaviSelection(this, 
 				MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY, position);
 		int requestCode = MobiseveraNaviContainer.getRequestCodeForNaviSelection(MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY, 
 				position);
 		//set appropriate GUID as extra data to intent, because it is needed in the query
 		if(position == PROJECT_NAVI_INDEX) {
 			//TODO: For consistency, it would be nice that the required USER GUID would be passed in from here.
 			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, ""); 
 		} else if(position == PHASE_NAVI_INDEX) {
 			if(selectedCase == null) {
 				showDialog(DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE);
 				return;
 			}
 			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, selectedCase.getCaseGuid());
 		} else if(position == WORKTYPE_NAVI_INDEX) {
 			if(selectedPhase == null) {
 				showDialog(DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE);
 				return;
 			}
 			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, selectedPhase.getPhaseGUID());
 		} else if(position == DESCRIPTION_NAVI_INDEX) {
 			EditText editor = (EditText)findViewById(R.id.descriptionEditText);
 			int editorVisibility = editor.getVisibility();
 			if(editorVisibility == View.GONE) {
 				Log.d(TAG,"Making edittext visible");
 				editor.setVisibility(View.VISIBLE);
 				editor.requestFocus();
 				return;
 			} else if(editorVisibility == View.VISIBLE) {
 				this.selectedDescription = editor.getText().toString();
 				Log.d(TAG,"Making edittext gone");
 				editor.setVisibility(View.GONE);
 				updateNaviTitles();
 				return;
 			}
 		}
 		Log.d(TAG,"Launching new activity with class: "+newIntent.getClass()+" with request code: "+requestCode);
 		startActivityForResult(newIntent,requestCode);
 		
 	}
 	
 	/**
 	 * Update the navigation with new information when it is received. For example, when user selects a new project,
 	 * this changes the "Project [not selected] to Project: [project name].
 	 * Data is coming from this class's instance variables
 	 */
 	
 	private void updateNaviTitles() {
 		Log.d(TAG,"Updating navititles. ");
 		String[] naviTitles = MobiseveraNaviContainer.getNaviarrayForActivity(this, MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY);
 		for(int i = 0; i < naviTitles.length; i++) {
 			switch(i) {
 			case 0: 
 				if(selectedCase != null && selectedCase.getCaseInternalName() != null) {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedCase.getCaseInternalName());
 				} else {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
 							getString(R.string.not_selected));					
 				}
 				break;
 			case 1:
 				if(selectedPhase != null && selectedPhase.getPhaseName() != null) {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedPhase.getPhaseName());
 				} else {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
 							getString(R.string.not_selected));					
 				}
 				break;
 			case 2: 
 				if(selectedWorkType != null && selectedWorkType.getWorkTypeName() != null) {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedWorkType.getWorkTypeName());
 				} else {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
 							getString(R.string.not_selected));					
 				}
 				break;
 			case 3:
 				if(selectedDescription != null) {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, truncateDesc(selectedDescription));
 				} else {
 					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
 							getString(R.string.not_selected));
 				}
 			}
 		}
 		ListView lv = (ListView)this.findViewById(R.id.main_claim_navi_list);
 		listAdapter = new MobiseveraNaviAdapter(this,R.layout.mobisevera_list_item,naviTitles);
         lv.setOnItemClickListener(this);
 		lv.setAdapter(listAdapter);
 	}
 	
 	/**
 	 * Make the given claim description text shorter (17 chars + 3 for ellipsis) so that it can fit in Mobisevera menu
 	 * @param selectedDescriptionParam
 	 * @return
 	 */
 	
 	private CharSequence truncateDesc(final String selectedDescriptionParam) {
 		if(selectedDescriptionParam == null) {
 			return null;
 		} else {
 			try {
 				return(selectedDescriptionParam.substring(0,17)+"...");
 			} catch (IndexOutOfBoundsException e) {
 				return selectedDescriptionParam;
 			}
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(TAG,"onActivityResult of MobiseveraClaimActivity called! request: "+requestCode+" result:"+resultCode);
 		super.onActivityResult(requestCode, resultCode, data);
		if(data == null) {
			Log.d(TAG, "Result is null, user probably returned from selector using back button.");
			return;
		}
 		if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PHASE) {
 			Log.d(TAG,"Get phase:");
 			S3PhaseItem phaseItem = (S3PhaseItem)data.getParcelableExtra(MobiseveraConstants.PHASE_PARCEL_EXTRA_ID);
 			if(phaseItem != null) {
 				this.selectedPhase=phaseItem;
 				updateNaviTitles();
 			} else {
 				Log.e(TAG,"Got null phase bean from the intent.");
 				return;
 			}
 		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PROJECT) {
 			Log.d(TAG,"Get project:");
 				S3CaseItem caseItem = (S3CaseItem)data.getParcelableExtra(MobiseveraConstants.CASE_PARCEL_EXTRA_ID);
 				if(caseItem != null) {
 					this.selectedCase=caseItem;
 					updateNaviTitles();
 				} else {
 					Log.e(TAG,"Got null case bean from the intent.");
 					return;
 				}
 		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_WORKTYPE) {
 			Log.d(TAG,"Get worktype:");
 			S3WorkTypeItem workTypeItem = (S3WorkTypeItem)data.getParcelableExtra(MobiseveraConstants.WORKTYPE_PARCEL_EXTRA_ID);
 			if(workTypeItem != null) {
 				this.selectedWorkType=workTypeItem;
 				updateNaviTitles();
 			} else {
 				Log.e(TAG,"Got null worktype bean from the intent.");
 				return;
 			}
 		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_DESCRIPTION) {
 			Log.d(TAG,"Get description:");
 		} else {
 			throw new IllegalStateException("Activity called with unsupported requestcode: "+requestCode);
 		}
 	}
 	
 	protected void receivePublishHourEntryReadyEvent() {
 		Log.d(TAG,"Received hour entry ready event on UI thread...");
 		Log.d(TAG,"Result was: "+hourEntryStatus);
 		if(hourEntryStatus) {
 			Toast.makeText(this, "Your work hours have been saved!", Toast.LENGTH_SHORT).show();	
 		} else {
 			showDialog(DIALOG_ID_FAILED_HOURS_PUBLISHING);
 		}
 		
 	}
 	
 	
 	/**
 	 * AsyncTask for calling the IHourEntry
 	 */
 	
 	private class PublishHourEntryTask extends AsyncTask<String, Integer, Boolean> {
 		
 		MobiseveraClaimActivity mParent = null;
 		
 		PublishHourEntryTask(MobiseveraClaimActivity activity) {
 			mParent = activity;
 		}
 
 		/**
 		 * Publish this hour entry
 		 * @params String Description
 		 * @params String EventDate - the date formatted YYYY-MM-DD
 		 * @params String Phase's guid
 		 * @params String quantity amount of hours formatted as "1.5" (that's DOT, not comma)
 		 * @params String user guid as obtained from app config
 		 * @params String work type GUID
 		 */
 		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			//gather necessary parameters
 			String description = TextUtils.htmlEncode(params[0]);
 			String eventDate = params[1];
 			String phaseGuid = params[2];
 			String quantity = params[3];
 			String userGuid = params[4];
 			String workTypeGuid = params[5];
 			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
 			boolean res = scu.publishHourEntry(mParent, description, eventDate, phaseGuid, quantity, userGuid, workTypeGuid);
 			return new Boolean(res);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(TAG,"onPostExecute on PublishHourEntryTask firing.");
 			if(result) {
 				mParent.hourEntryStatus = true;
 			} else {
 				mParent.hourEntryStatus = false;
 			}
 			mParent.receivePublishHourEntryReadyEvent();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 		
 		
 	}
 	
 
 }
