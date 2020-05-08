 package il.ac.tau.team3.shareaprayer;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.common.SPGeoPoint;
 import il.ac.tau.team3.common.SPUtils;
 import il.ac.tau.team3.common.UnknownLocationException;
 import il.ac.tau.team3.spcomm.ACommHandler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.PopupWindow.OnDismissListener;
 import android.widget.QuickContactBadge;
 import android.widget.RemoteViews.ActionException;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.TimePicker;
  
 public class UIUtils {
  
 	static String _sNewPlaceQues = "Do you want to create a public praying place?";
 	static String _sNoPraySelected = "Please select at least one pray before creating a new place.";
 	static String _sAlreadyRegisterAlertMsg = "You are already registered to this place.";
 	static String _sWantToRegisterQues = "Would you like to register to this place?";
 	static String _sUserNotRegisterMsg = "You are not register to this place.";
 	static String _sUserNotOwnerMsg = "You can't delete this place, because you are not the owner.";
 
 	static class UpdateUI<T> extends ACommHandler<T> {
 		FindPrayer activity;
  
 		public UpdateUI(FindPrayer a_activity) {
 			activity = a_activity;
 		}
  
 		@Override
 		public void onRecv(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 			
 			}
 		}
 
 		@Override
 		public void onError(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 			}
 		}
 	}
 	
 	static void RegisterClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay, boolean praysWishes[]) {
 		GeneralUser user = placeOverlay.getThisUser();
 		if (user == null) {
 			Log.d("UIUtils:createRegisterDialog", "Error: user is null");
 			return;
 		} else {
 			String name = user.getName();
 			if (name == null || name == "") {
 				Log.d("UIUtils:createRegisterDialog",
 						"Error: name is null or empty.");
 				return;
 			}
 		}
 
 		
 		placeOverlay
 				.getActivity()
 				.getSPComm()
 				.requestPostRegister(place, user, praysWishes,
 						new UpdateUI<String>(placeOverlay.getActivity()));
 
 		
 
 	}
 
 	static void DeleteClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay) {
 		if (place.getOwner().getName().equals(placeOverlay.getThisUser().getName())) {
 		
 			placeOverlay
 					.getActivity()
 					.getSPComm()
 					.deletePlace(place,
 							new UpdateUI<String>(placeOverlay.getActivity()));
 		} else {
 			createAlertDialog(_sUserNotOwnerMsg, placeOverlay.getActivity());
 		}
 	}
 
 	static void UnregisterClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay, boolean praysWishes[]) {
 		GeneralUser user = placeOverlay.getThisUser();
 		if (user == null) {
 			Log.d("UIUtils:createRegisterDialog", "Error: user is null");
 			return;
 		} else {
 			String name = user.getName();
 			if (name == null || name == "") {
 				Log.d("UIUtils:createRegisterDialog",
 						"Error: name is null or empty.");
 				return;
 			}
 		}
 	
 		placeOverlay
 				.getActivity()
 				.getSPComm()
 				.removeJoiner(place, user, praysWishes,
 						new UpdateUI<Void>(placeOverlay.getActivity()));
 
 	
 
 	}
 	
 	static class PrayUIObj	{
 		public TextView prayTime;
 		public CheckBox prayCheckBox;
 		public TextView prayNumOfUsers;
 		public boolean	wish = false;
 		public boolean  exists = false;
 		public PrayUIObj(TextView prayTime, CheckBox prayCheckBox) {
 			super();
 			this.prayTime = prayTime;
 			this.prayCheckBox = prayCheckBox;
 		}
 		public PrayUIObj(TextView prayTime, CheckBox prayCheckBox, TextView prayNumOfUsers) {
 			super();
 			this.prayTime = prayTime;
 			this.prayCheckBox = prayCheckBox;
 			this.prayNumOfUsers = prayNumOfUsers;
 		}
 		
 	}
 	
 	private static boolean[] toPrayerWishes(Map<String, PrayUIObj> ui)	{
 		return new boolean[]{ui.get("Shaharit").wish, ui.get("Minha").wish, ui.get("Arvit").wish};
 	}
 	
 	/*package*/ static void createRegisterDialog(Map<String, Integer> joinersMap, final GeneralPlace place, final PlaceArrayItemizedOverlay placeOverlay)
 	{
 		if (placeOverlay == null || placeOverlay.getThisUser() == null || place == null) 
 		{
 			Log.d("UIUtils::createRegisterDialog",
 					"placeOverlay == null || placeOverlay.getThisUser() == null || place == null");
 			return;
 		}
 
  
 		final Dialog dialog = new Dialog(placeOverlay.getActivity());
 		dialog.setContentView(R.layout.dialog_place_registration);
 		dialog.setTitle(place.getName());
 		
 		// Address and Dates
 		TextView placeAddress = (TextView) dialog.findViewById(R.id.DPRaddress);
 		placeAddress.setText(place.getAddress());
 		// TODO Date is wrong
 		TextView placeDates = (TextView) dialog.findViewById(R.id.DPRdates);
 		placeDates.setText(printDateFromDate(place.getEndDate()));
 		
 		final Map<String, PrayUIObj> JoinersUI = new HashMap<String, PrayUIObj>();
 		
 		JoinersUI.put("Shaharit", new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeShaharit), 
 												(CheckBox) dialog.findViewById(R.id.DPRcheckboxShaharit),
 												(TextView) dialog.findViewById(R.id.DPRnumberOfUsersShaharit)));
 		
 		JoinersUI.put("Minha", 	  new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeMinha), 
 											    (CheckBox) dialog.findViewById(R.id.DPRcheckboxMinha),
 											    (TextView) dialog.findViewById(R.id.DPRnumberOfUsersMinha)));
 		
 		JoinersUI.put("Arvit",    new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeArvit), 
 											    (CheckBox) dialog.findViewById(R.id.DPRcheckboxArvit),
 											    (TextView) dialog.findViewById(R.id.DPRnumberOfUsersArvit)));
 		
 		for (Pray p : place.getPraysOfTheDay())	{
 			if (null != p)	{
 				final PrayUIObj uiObj = JoinersUI.get(p.getName());
 				if (uiObj == null)	{
 					continue;
 				}
 				uiObj.exists = true;
 				uiObj.prayTime.setText(printTimeFromCalendar(p.getStartTime()));
 				uiObj.prayTime.setTextColor(Color.WHITE);
 				uiObj.prayCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						if (isChecked) {
 							uiObj.wish = true;
 						} else {
 							uiObj.wish = false;
 						}
 					}
 				});
 				Boolean isSigned = p.isJoinerSigned(placeOverlay.getThisUser());
 				uiObj.prayCheckBox.setChecked(isSigned);
 				uiObj.prayCheckBox.setClickable(true);
 				uiObj.prayCheckBox.setTextColor(Color.WHITE);
 				uiObj.prayNumOfUsers.setText(Integer.toString(p.numberOfJoiners()));
 				uiObj.prayNumOfUsers.setTextColor(Color.WHITE);
 			}
 		}
 
 		//People Button: Temporary
 		final Map<String, Integer> finalJoinersMap = joinersMap;
 		Button peopleButton = (Button) dialog.findViewById(R.id.DPRShowPeople);
 		peopleButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				final Dialog listDialog = new Dialog(placeOverlay.getActivity());
 				listDialog.setContentView(R.layout.dialog_registered_users_list);
 				listDialog.setTitle("Users Registered:");
 				ListView lv = (ListView) listDialog.findViewById(R.id.DRUList);
 				
 				ArrayList<HashMap<String, String>> prayersList = new ArrayList<HashMap<String, String>>();
 				
 				for (String joiner : finalJoinersMap.keySet()){
 					HashMap<String, String> tempmap = new HashMap<String, String>();
 					tempmap.put("User", joiner);
 					tempmap.put("int", Integer.toString(finalJoinersMap.get(joiner)));
 					prayersList.add(tempmap);
 				}
 				
 				SimpleAdapter mPrays = new SimpleAdapter(placeOverlay.getActivity(), prayersList, R.layout.dialog_registered_users_row,
 				            new String[] {"User", "int"}, new int[] {R.id.DRUUsername1, R.id.DRUInteger});
 				try{
 					lv.setAdapter(mPrays);
 				}catch (NullPointerException npe){}
 				listDialog.show();			
 
 			};
 		});
 		
 		// Delete Set and Cancel Buttons:
 		Button setButton = (Button) dialog.findViewById(R.id.DPRSetButton);
 		Button cancelButton = (Button) dialog.findViewById(R.id.DPRCancelButton);
 		Button deleteButton = (Button) dialog.findViewById(R.id.DPRDeleteButton);
 		 
 		deleteButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				DeleteClick(place, placeOverlay);
 				dialog.dismiss();
 			};
 		});
 		if (place.getOwner() != null && placeOverlay.getThisUser() != null) {
 			if (!(place.getOwner().getName().equals(placeOverlay.getThisUser().getName()))) {
 				deleteButton.setVisibility(View.INVISIBLE);
 			}
 		}
 		final boolean allTrues[] = new boolean[3];
 		for (int i=0; i<3; i++){ allTrues[i] = true;}
 		setButton.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View view) 
 			{
 				UnregisterClick(place, placeOverlay, allTrues);
 				RegisterClick(place, placeOverlay, toPrayerWishes(JoinersUI));
 				dialog.dismiss();
 			};
 		});
 		
 		cancelButton.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View view) 
 			{
 				dialog.dismiss();
 			};
 		});
 
 		dialog.show();
 	}
 
 	
 	static class ListDialog extends ListActivity
 	{
 		private Map map;
 		private Activity activity;
 		
 		public ListDialog(Map<String, Integer> map, Activity activity ){
 			super();
 			this.map = map;
 			this.activity = activity;
 		}
 	}
 			
 	
 
 
 	static void createAlertDialog(String msg, Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(msg);
 		builder.setCancelable(true);
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	
 	
 	
 	
 	static class CreatePlaceDialog	{		private Dialog dialog;
 		private EditText editAddress;
 		private Calendar startDate = new GregorianCalendar(); 
 		private Calendar endDate = new GregorianCalendar(); 
 		private TextView fromDate;
         private TextView toDate;
         private FindPrayer activity;
         private Button changeStartDate;
         private Button changeEndDate;
         private final int NUMBER_OF_PRAYS = 3;
         private boolean prays[] = new boolean[NUMBER_OF_PRAYS];
         private CheckBox[] checkBoxes = new CheckBox[NUMBER_OF_PRAYS];
         private TextView[] timeTextViews = new TextView[NUMBER_OF_PRAYS];
         private Calendar[] prayTimes = new GregorianCalendar[NUMBER_OF_PRAYS];
       
         
         private class DatePickerClickListener implements OnClickListener	{
         	
         	private Calendar cal;
         	private TextView textStr;
         	
         	public DatePickerClickListener(Calendar a_cal, TextView a_textStr)	{
         		cal = a_cal;
         		textStr = a_textStr;
         	}
 
 			public void onClick(View v) {
 				DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
                 {
                     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                     {
                     	cal.set(year, monthOfYear, dayOfMonth);
                         //monthOfYear++;
                     	textStr.setText(printDateFromCalendar(cal));
                         // TODO Send dates to server
                     }
                 };
                 DatePickerDialog datePickerDialog = 
                 	new DatePickerDialog(
                 			CreatePlaceDialog.this.activity, 
                 			mDateSetListener, 
                 			cal.get(Calendar.YEAR), 
                 			cal.get(Calendar.MONTH), 
                 			cal.get(Calendar.DAY_OF_MONTH));
                 datePickerDialog.show();
 			}
         	
         }
         
         class PrayTimePickDialog extends TimePickerDialog {
 
         	private CheckBox checkBox;
         	private int prayIndex;
         	
         	public PrayTimePickDialog(final TextView a_timeStr, int defHour, int defMin, 
         			CheckBox a_checkBox, final int a_prayIndex, int a_resIcon)	{
         		super(activity, 
         				new OnTimeSetListener() {
         			
         			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
     					SPUtils.debugFuncStart("timePickerDialog.onTimeSet", view, hourOfDay, minute);
     					CreatePlaceDialog.this.prays[a_prayIndex] = true;
     					prayTimes[a_prayIndex].set(2000, 1, 1, hourOfDay, minute, 0);
     					// TODO: CLEAN THIS
     					a_timeStr.setText((hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute + " ");
     					
     				}
         			
         		}, defHour, defMin, true);
         		
                 this.setIcon(a_resIcon);
                 this.setInverseBackgroundForced(true);          
                 this.setCancelable(true);              //
                 this.setCanceledOnTouchOutside(true);  //
         		checkBox = a_checkBox;
         	}
         	
         	@Override
 			public void cancel()	{
         		SPUtils.debugFuncStart("timePickerDialog.onCancel", dialog);
         		super.cancel();
         	}
         	
         	@Override
         	public void dismiss()	{
         		SPUtils.debugFuncStart("timePickerDialog.onDismiss", dialog);
                 SPUtils.debug("--> prays["+prayIndex+"] = " + "prays["+prayIndex+"]");
                 checkBox.setChecked(prays[prayIndex]);
                 super.dismiss();
         	}
         	
         	
         	
         }
         
         class CheckBoxListener implements OnCheckedChangeListener
         {              
         	private TextView timeTextView;
         	private int		 index;
         	private CheckBox checkBox;
         	int defHour;
         	int defMinutes;
         	int resIcon;
         	
         	
             public CheckBoxListener(TextView timeTextView, int index, CheckBox checkBox, 
             		int defHour, int defMinutes, int resIcon) {
 				super();
 				this.timeTextView = timeTextView;
 				this.index = index;
 				this.checkBox = checkBox;
 				this.defHour = defHour;
 				this.defMinutes = defMinutes;
 				this.resIcon = resIcon;
 				
 			}
 
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
                 SPUtils.debugFuncStart("pray1.onCheckedChanged", buttonView, isChecked);
                 if (isChecked)
                 {
                     (new PrayTimePickDialog(timeTextView, defHour, defMinutes, checkBox, index, resIcon)).show();
                 }
                 else
                 {
                     prays[index] = false;
                     timeTextView.setText("");
                 }                
             }
         };
 		
 		public CreatePlaceDialog(final SPGeoPoint point, final FindPrayer a_activity, final GeneralUser user)	{
 			if (point == null || a_activity == null || user == null)
 			{
 				
 				Log.d("UIUtils::createRegisterDialog", "point == null || activity == null || user == null");
 				// TODO: change to checked exception
 				throw new NullPointerException("CreatePlaceDialog: executed with NULL!!!!");
 				//return;
 			}
 			
 			for (int i = 0; i < prayTimes.length; prayTimes[i] = new GregorianCalendar(),i++);
 			  
 			activity = a_activity;
 			
 			dialog = new Dialog(activity);
 			dialog.setContentView(R.layout.dialog_place_create);
 			dialog.setTitle(R.string.create_place_title);
 			
 			editAddress = (EditText) dialog.findViewById(R.id.CPDeditText1);
 			
 			fromDate = (TextView) dialog.findViewById(R.id.CPDFromDatetextView);
 			toDate   = (TextView) dialog.findViewById(R.id.CPDToDatetextView);
 			fromDate.setText(printDateFromCalendar(startDate)); 
 	        toDate.setText(printDateFromCalendar(endDate)); 
 	        
 	        changeStartDate = (Button) dialog.findViewById(R.id.CPDChange1button);
 	        changeEndDate = (Button) dialog.findViewById(R.id.CPDChange2button);
 	        
 			changeStartDate.setOnClickListener(new DatePickerClickListener(startDate, fromDate));
 			changeEndDate.setOnClickListener(new DatePickerClickListener(endDate, toDate));
 			
 			
 			checkBoxes[0] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox1);
 			timeTextViews[0] = (TextView) dialog.findViewById(R.id.CPDshahritTime);
 			checkBoxes[0].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[0], 
 					0, checkBoxes[0], 7, 0, R.drawable.shaharit_small));
 			
 			checkBoxes[1] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox2);
 			timeTextViews[1] = (TextView) dialog.findViewById(R.id.CPDminhaTime);
 			checkBoxes[1].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[1], 
 					1, checkBoxes[0], 15, 0, R.drawable.minha_small));
 			
 			checkBoxes[2] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox3);
 			timeTextViews[2] = (TextView) dialog.findViewById(R.id.CPDarvitTime);
 			checkBoxes[2].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[2], 
 					2, checkBoxes[0], 19, 0, R.drawable.arvit_small));
 			
 			Button createButton = (Button) dialog.findViewById(R.id.CPDCreateButton);
 	        Button cancelButton = (Button) dialog.findViewById(R.id.CPDCancelButton);
 	        
 	    
 			createButton.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View view) {
					final Date finalstartDate = new Date(startDate.get(Calendar.YEAR),startDate.get(Calendar.MONTH),startDate.get(Calendar.DAY_OF_MONTH));
					final Date finalendDate = new Date(endDate.get(Calendar.YEAR),endDate.get(Calendar.MONTH),endDate.get(Calendar.DAY_OF_MONTH));
 					CreateNewPlace_YesClick(prays, user, activity, point, finalstartDate, finalendDate, prayTimes);
 					dialog.dismiss();
 
 				};
 
 			});
 
 			cancelButton.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View view) {
 
 					dialog.dismiss();
 
 				};
 
 			});
 
 			
 			dialog.show();
 			
 		}
 
 	};
 
 	static void createNewPlaceDialog(final SPGeoPoint point, final FindPrayer activity, final GeneralUser user) 
 	{
 		try {
 			new CreatePlaceDialog(point, activity, user);
 		} catch (NullPointerException e)	{
 			
 		}
 		
      }
 	
 	static void CreateNewPlace_YesClick(boolean prays[], GeneralUser user,
 			FindPrayer activity, SPGeoPoint point, Date startDate, Date endDate , Calendar[] prayTimes) {
 		GeneralPlace newMinyan = new GeneralPlace(user, user.getName()
 				+ "'s Place", "", point, startDate,endDate);
 		//newMinyan.setPrays(prays);
 		Calendar c = new GregorianCalendar(2011,2,2,15,30);
 		//Calendar c = new GregorianCalendar();
 		List<GeneralUser> j = new ArrayList<GeneralUser>();
 		j.add(user);
 		if (prays[0]) {
 			
 			Pray pray = new Pray(prayTimes[0], c, "Shaharit", j);
 			newMinyan.setPraysOfTheDay(0, pray);
 		}
 		if (prays[1]) {
 			Pray pray = new Pray(prayTimes[1], c, "Minha", j);
 			newMinyan.setPraysOfTheDay(1, pray);
 		}
 		if (prays[2]) {
 			Pray pray = new Pray(prayTimes[2], c, "Arvit", j);
 			newMinyan.setPraysOfTheDay(2, pray);
 		}
 
 		activity.getSPComm().requestPostNewPlace(newMinyan,
 				new UpdateUI<Long>(activity));
 
 		
 	}
 
 	
 	public static String printDateFromCalendar(Calendar c) {
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		int month = (c.get(Calendar.MONTH)+1);
 		int year = c.get(Calendar.YEAR);
 		return ((month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day + "/" + year);
 	}
 
 	private static String printDateFromDate(Date d) {
 		int day = d.getDate();
 		int month = d.getMonth()+1;
 		int year = d.getYear();
 		return ((month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day + "/" + year);
 	}
 
 	private static String printTimeFromDate(Date time) {
 		int hour = time.getHours();
 		int minutes = time.getMinutes();
 		return ((hour < 10 ? "0" : "") + hour + ":" + (minutes < 10 ? "0" : "") + minutes + " ");
 	}
     
 	private static String printTimeFromCalendar(Calendar cal) {
 		int hour = cal.getTime().getHours();
 		int minutes = cal.getTime().getMinutes();
 		return ((hour < 10 ? "0" : "") + hour + ":" + (minutes < 10 ? "0" : "") + minutes + " ");
 	}
     
 }
