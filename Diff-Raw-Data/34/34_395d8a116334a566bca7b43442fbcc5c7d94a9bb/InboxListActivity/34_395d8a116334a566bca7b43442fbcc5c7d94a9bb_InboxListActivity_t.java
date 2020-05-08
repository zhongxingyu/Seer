 package com.lcm.data.sms;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.lcm.data.ExpenditureDBAdaptor;
 import com.lcm.data.ParsedData;
 import com.lcm.data.SMSDbAdapter;
 import com.lcm.data.parse.NotValidSmsDataException;
 import com.lcm.smsSmini.R;
 import com.lcm.view.MainActivity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InboxListActivity extends Activity {
 	private static final String TAG = "InboxListActivity";
 	private static final boolean DEBUG = false;
 	ArrayAdapter<ParsedData> inboxAdapter;
 	final ArrayList<ParsedData> parsedList = new ArrayList<ParsedData>();
 	SMSConverter smsConverter;
 	SMSDbAdapter smsDbAdapter;
 	Context context = this;
 	protected boolean confirmed = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.inbox);
 
 		smsConverter = new SMSConverter(context);
 		updateSmsList(null, null);
 		
 		inboxAdapter = new SmsItemAdapter(this, R.layout.sms, R.id.smsStat,	parsedList);
 		((ListView) findViewById(R.id.inboxList11)).setAdapter(inboxAdapter);
 		Button button = ((Button) findViewById(R.id.inbox_loadButton));
 		button.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(event.getAction()==MotionEvent.ACTION_UP) {
 					loadSelectedSms();
 				}
 				return true;
 			}
 		});
 	}
 
 	
 	private void loadSelectedSms() {
 //		smsDbAdapter = new SMSDbAdapter(context);
 //		smsDbAdapter.open();
 		ArrayList<ParsedData> parsedData = new ArrayList<ParsedData>();//ParsedData[smsList.size()];
 		for(ParsedData sd : parsedList) {
 			if(sd.isFlag()==true) {
 				if(DEBUG) Log.i("db_log","in loadSelectedSms..");
 				// make database for updated one; use parse class
 				// need to find out if the data is already in the DB
 				sd.setFlag(false);
 				parsedData.add(sd);
 			}
 		}
 		if(parsedData.size()!=0) { 
 			Intent handleIntent = new Intent(this, HandleReceivedSms.class);
 			handleIntent.putExtra("ParsedData",parsedData);
 			handleIntent.putExtra("Source","InboxListActivity");
 			startActivity(handleIntent);
 		}
 //		smsDbAdapter.close();
 		finish();
 	}
 
 	private class SmsItemAdapter extends ArrayAdapter<ParsedData> {
 		private ArrayList<ParsedData> items;
 		private int resource;
 
 		public SmsItemAdapter(Context context, int resource,
 				int textViewResourceId, ArrayList<ParsedData> data) {
 			super(context, resource, textViewResourceId, data);
 			this.items = data;
 			this.resource = resource;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = li.inflate(resource, null);
 			}
 			ParsedData e = items.get(position);
 			if (e != null) {
 				((TextView) v.findViewById(R.id.smsStat)).setText("Date: "+e.getDateFormat());
 				((TextView) v.findViewById(R.id.smsBody)).setText(e.getSpent() + "\t" + e.getDetail());
 //				CheckBox cb = (CheckBox) v.findViewById(R.id.smsLoad);
 //				cb.setChecked(e.isFlag());
 				if(e.isFlag()) {
 					((TextView) v.findViewById(R.id.smsStat)).setTextColor(Color.YELLOW);
 					((TextView) v.findViewById(R.id.smsBody)).setTextColor(Color.YELLOW);
 				} else {
 					((TextView) v.findViewById(R.id.smsStat)).setTextColor(Color.WHITE);
 					((TextView) v.findViewById(R.id.smsBody)).setTextColor(Color.WHITE);
 				}
 				//cb.setFocusable(false);
 				//cb.setOnClickListener(new LoadChecked(position,cb));
 				v.setOnClickListener(new LoadChecked(position));
 			}
 			return v;
 		}
 	}
 	
 	public class LoadChecked implements OnClickListener {
 		int position;
 		//CheckBox cb;
 		
 		public LoadChecked(int position) { //,CheckBox cb) {
 			this.position = position;
 			//this.cb = cb;
 		}
 
 		public void onClick(View v) {
 			// check if checked sms is feasible one, otherwise show a dialog that check if it is right one
 //			if(cb.isChecked()) {
 //				int[] phoneNum = getResources().getIntArray(R.array.phone_no);
 //				boolean isBanks = false;
 //				String addr = null;
 //				for(int num:phoneNum) {
 //					addr = smsList.get(position).getAddr();
 //					if(addr.contains("+82")) {
 //						addr = addr.substring(3);
 //					}
 //					if(Integer.parseInt(addr)==num) isBanks = true;
 //				}
 //				if(!isBanks) {
 //					checkDialog(addr);
 //				}
 //				if(confirmed) smsList.get(position).setLoad(cb.isChecked());
 //				if(!confirmed) cb.setChecked(false);
 //			} else {
 //				smsList.get(position).setLoad(cb.isChecked());
 //			}
 //			smsList.get(position).setLoad(cb.isChecked());
 //			CheckBox cb = (CheckBox) v.findViewById(R.id.smsLoad);
 //			cb.setChecked(!cb.isChecked());
 			//parsedList.get(position).setFlag(cb.isChecked());
 			if(parsedList.get(position).isFlag()) {
 				parsedList.get(position).setFlag(false);
 				((TextView) v.findViewById(R.id.smsStat)).setTextColor(Color.WHITE);
 				((TextView) v.findViewById(R.id.smsBody)).setTextColor(Color.WHITE);
 			} else {
 				parsedList.get(position).setFlag(true);
 				((TextView) v.findViewById(R.id.smsStat)).setTextColor(Color.YELLOW);
 				((TextView) v.findViewById(R.id.smsBody)).setTextColor(Color.YELLOW);
 			}
 		}
 	}
 
 	/**
 	 * getting sms from sms data base
 	 * @param smsArg
 	 * @param smsString
 	 */
 	public void updateSmsList(String[] smsArg, String[] smsString) {
 		// SAMSUNG GA CP URL: content://com.btb.sec.mms.provider/message
 		// SAMSUNG CP URL: content://com.sec.mms.provider/message
 			
 		// LG CP URL: content://com.lge.messageprovider/msg/inbox
 			// incoming number: sender, body: body.
 		// General: content://sms/inbox
 		String bodyIdString = "body";
 		String addrIdString = "address";
 		String dateIdString = "date";
 		String _idIdString = "_id";
 		
 		boolean mmsAvailable = false;
 		Cursor result = null;
 		
 		if(!mmsAvailable) {
 			// for other phones
 			result = getContentResolver().query(
 					Uri.parse("content://sms/inbox"), null, null, null, null);
 			if(result.getCount()==0) result.close();
 			else {
 				Log.e(TAG,"Normal Phone it is!!!!!!!!");
 				mmsAvailable = true;
 			}
 		}
 		if(!mmsAvailable) {
 			// for LG phone
 			result = getContentResolver().query(
 					Uri.parse("content://com.lge.messageprovider/msg/inbox"), null, null, null, null);
 			if(result.getCount()==0) result.close();
 			else {
 				Log.e(TAG,"LG Phone it is!!!!!!!!");
 				mmsAvailable = true;
 				bodyIdString = "body";
 				dateIdString = "date";
 				addrIdString = "sender";
 				_idIdString = "_id";
 			}
 		}
 		if(!mmsAvailable) {
 			// for SamSung phone
 			result = getContentResolver().query(
 					Uri.parse("content://com.sec.mms.provider/message"), null, null, null, null);
 			if(result.getCount()==0) result.close();
 			else {
 				Log.e(TAG,"Samsung Phone it is!!!!!!!!");
 				mmsAvailable = true;
 				bodyIdString = "Title";
 				dateIdString = "RegTime";
 				addrIdString = "MDN1st";
 				_idIdString = "RootID";
 			}
 		}
 		if(!mmsAvailable) {
 			// for SamSungGalaxy A phone
 			result = getContentResolver().query(
 					Uri.parse("content://com.btb.sec.mms.provider/message"), null, null, null, null);
 			if(result.getCount()==0) result.close();
 			else {
 				Log.e(TAG,"Samsung Phone it is!!!!!!!!");
 				mmsAvailable = true;
 				bodyIdString = "Title";
 				dateIdString = "RegTime";
 				addrIdString = "MDN1st";
 				_idIdString = "RootID";
 			}
 		}	
 		
 		int bodyid = result.getColumnIndex(bodyIdString);
 		int addrid = result.getColumnIndex(addrIdString);
 		int dateid = result.getColumnIndex(dateIdString);
 		int _idid = result.getColumnIndex(_idIdString);
 		
 		ExpenditureDBAdaptor expenditureDBAdaptor = new ExpenditureDBAdaptor(this);
 		expenditureDBAdaptor.open();
 		
 		if (result != null) {
 			while (result.moveToNext()) {
 				SmsData sd = new SmsData(result.getString(bodyid),
 						result.getString(addrid), result.getString(dateid), Integer.parseInt(result.getString(_idid)));
 				// check if the SmsData is ok to be shown using parser
				Log.e(TAG,"updateSMSList: "+sd.getDate() + "," + sd.getBody());
 				if(smsConverter.isValidSms(sd) && !expenditureDBAdaptor.isDataExist(Long.parseLong(sd.getDate()))
 						// should check if sd is already put into the DB & check DB code here 
 						) {
 //					Log.e("MMS------", "address: " + result.getString(addrid)+" _id: " + result.getString(_idid));
 //					smsList.add(sd);
 					try {
 						parsedList.add(smsConverter.convertSms(sd));
 					} catch (NotValidSmsDataException e) {e.printStackTrace();}
 				}
 			}
 		}
 		
 		expenditureDBAdaptor.close();
 		result.close();
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, MainActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 		finish();
 	}
 	
 	/*
 	private void checkDialog(String number){
 		confirmed = false;
 		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
 		alt_bld.setMessage("Do you want to include "+ number +" anyway?").setCancelable(false)
 			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						confirmed  = true;
 						Toast.makeText(InboxListActivity.this, "Confirmed", Toast.LENGTH_SHORT).show();
 						// Action for 'Yes' Button
 					}
 			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// Action for 'NO' Button
 					dialog.cancel();
 				}
 			});
 		AlertDialog alert = alt_bld.create();
 		alert.setTitle("This isn\'t from any banks");
 		alert.setIcon(R.drawable.icon);
 		alert.show();
 	} */
 
 }
