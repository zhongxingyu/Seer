 package com.ss.stg;
 
 import java.lang.ref.WeakReference;
 import java.text.MessageFormat;
import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import com.ss.stg.dto.SubtourItem;
 import com.ss.stg.dto.TourObject;
 import com.ss.stg.ws2.DTOApi;
 import com.ss.stg.ws2.IWebService;
 import com.ss.stg.ws2.WSThread;
 
 import android.R.integer;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TourEditActivity extends FragmentActivity implements OnDateSetListener {
 
 	public static final int REQ_EDIT_SUBTOUR = 100;
 	public static final int REQ_ADD_SUBTOUR = 101;
 
 	private Toast toast = null;
 
 	private Handler handler = null;
 	private Button okButton = null;
 	private Button cancelButton = null;
 	private Button startDateButton = null;
 	private Button endDateButton = null;
 	private Button addSubtourButton = null;
 	private Spinner statuSpinner = null;
 	private EditText nameEditText = null;
 	private TextView nameTextView = null;
 	private EditText costEditText = null;
 	private ListView subtourListView = null;
 	private TourObject tourObject = null;
 
 	public void setTour(TourObject tour) {
 		this.tourObject = tour;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tour_edit);
 
 		okButton = (Button) findViewById(R.id.tour_edit_ok);
 		cancelButton = (Button) findViewById(R.id.tour_edit_cancel);
 		startDateButton = (Button) findViewById(R.id.tour_edit_start_date);
 		endDateButton = (Button) findViewById(R.id.tour_edit_end_date);
 		addSubtourButton = (Button) findViewById(R.id.tour_edit_addsubtour);
 		statuSpinner = (Spinner) findViewById(R.id.tour_edit_status);
 		nameEditText = (EditText) findViewById(R.id.tour_edit_name);
 		nameTextView = (TextView) findViewById(R.id.tour_edit_name_r);
 		subtourListView = (ListView) findViewById(R.id.tour_edit_subtourlist);
 		costEditText = (EditText) findViewById(R.id.tour_edit_cost);
 
 		String[] mItems = getResources().getStringArray(R.array.tour_status_dict);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
 		statuSpinner.setAdapter(adapter);
 
 		// boolean readonly = false;
 		final Intent intent = getIntent();
 		final String tourId = intent.getStringExtra("tourid");
 		final String userId = intent.getStringExtra("userid");
 
		handler = new STGHandler(this);

 		if (tourId != null && !tourId.equals("")) {
 			// readonly = intent.getBooleanExtra("readonly", true);
 			WSThread thread = null;
			HashMap<String, Object> params = new HashMap<String, Object>();
 			params.put(IWebService.PARAM__GET_TOUR_BY_TOURID, tourId);
 			thread = new WSThread(handler, IWebService.ID__GET_TOUR_BY_TOURID, params);
 			thread.startWithProgressDialog(this);
 
 			nameTextView.setVisibility(View.VISIBLE);
 			nameEditText.setVisibility(View.GONE);
 		}
 
 		startDateButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				TourDateEditDialogFragment dialog = new TourDateEditDialogFragment();
 				if (!startDateButton.getText().equals("")) {
 					Bundle bundle = new Bundle();
 					int[] tag = (int[]) startDateButton.getTag();
 					bundle.putInt("Year", tag[0]);
 					bundle.putInt("Month", tag[1]);
 					bundle.putInt("Day", tag[2]);
 					dialog.setArguments(bundle);
 				}
 
 				dialog.show(TourEditActivity.this.getSupportFragmentManager(), "start");
 			}
 		});
 
 		endDateButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				TourDateEditDialogFragment dialog = new TourDateEditDialogFragment();
 				if (!endDateButton.getText().equals("")) {
 					Bundle bundle = new Bundle();
 					int[] tag = (int[]) endDateButton.getTag();
 					bundle.putInt("Year", tag[0]);
 					bundle.putInt("Month", tag[1]);
 					bundle.putInt("Day", tag[2]);
 					dialog.setArguments(bundle);
 				}
 
 				dialog.show(TourEditActivity.this.getSupportFragmentManager(), "end");
 			}
 		});
 
 		addSubtourButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(TourEditActivity.this, SubtourEditActivity.class);
 				intent.putExtra("pos", 0);
 				startActivityForResult(intent, TourEditActivity.REQ_ADD_SUBTOUR);
 			}
 		});
 
 		okButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				// 保存TourObject
 				TourObject tour2 = TourEditActivity.this.tourObject;
 				if (tour2 == null) {
 					tour2 = new TourObject();
					tour2.setTourId("");
 				}
 
 				if (startDateButton.getTag() == null) {
 					toast = Toast.makeText(TourEditActivity.this, "开始时间不能为空", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				if (endDateButton.getTag() == null) {
 					toast = Toast.makeText(TourEditActivity.this, "结束时间不能为空", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				if (nameEditText.getText().toString().equals("") && nameTextView.getText().toString().equals("")) {
 					toast = Toast.makeText(TourEditActivity.this, "旅行名称不能为空", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 
 				int[] s = (int[]) startDateButton.getTag();
 				Calendar c1 = Calendar.getInstance();
 				c1.set(Calendar.YEAR, s[0]);
 				c1.set(Calendar.MONTH, s[1]);
 				c1.set(Calendar.DATE, s[2]);
 
 				int[] e = (int[]) endDateButton.getTag();
 				Calendar c2 = Calendar.getInstance();
 				c2.set(Calendar.YEAR, e[0]);
 				c2.set(Calendar.MONTH, e[1]);
 				c2.set(Calendar.DATE, e[2]);
 
 				tour2.setBeginDate(c1.getTime());
 				tour2.setEndDate(c2.getTime());
 				tour2.setCost(Integer.parseInt(costEditText.getText().toString().replace(",", "")));
 				tour2.setStatus(statuSpinner.getSelectedItem().toString());
 				tour2.setTourName(nameEditText.getText().toString());
 				tour2.setUserId(userId);
 
 				WSThread thread = null;
 				HashMap<String, Object> params2 = new HashMap<String, Object>();
 				params2.put(IWebService.PARAM__SAVE_TOUR__TOUR, tour2);
 				params2.put(IWebService.PARAM__SAVE_TOUR__SUBTOURS, ((SubtourAdapter) subtourListView.getAdapter()).getList());
 				params2.put(IWebService.PARAM__SAVE_TOUR__REMOVED_SUBTOURS, ((SubtourAdapter) subtourListView.getAdapter()).getRemovedList());
 				thread = new WSThread(handler, IWebService.ID__SAVE_TOUR, params2);
 				thread.startWithProgressDialog(TourEditActivity.this);
 			}
 		});
 
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				setResult(RESULT_CANCELED);
 				finish();
 			}
 		});
 	}
 
 	private static class STGHandler extends Handler {
 
 		private WeakReference<Activity> refActivity = null;
 
 		private STGHandler(Activity activity) {
 			this.refActivity = new WeakReference<Activity>(activity);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			Activity activity = refActivity.get();
 
 			super.handleMessage(msg);
 
 			if (msg.what == IWebService.ID__GET_TOUR_BY_TOURID) {
 
 				TourObject tour = (TourObject) msg.getData().getSerializable(IWebService.WS_RETURN);
 
 				((TourEditActivity) activity).setTour(tour);
 
 				TextView nameTextView = (TextView) activity.findViewById(R.id.tour_edit_name_r);
 				nameTextView.setText(tour.getTourName());
 
 				EditText nameEditText = (EditText) activity.findViewById(R.id.tour_edit_name);
 				nameEditText.setText(tour.getTourName());
 
 				EditText costEditText = (EditText) activity.findViewById(R.id.tour_edit_cost);
 				costEditText.setText(String.valueOf(tour.getCost()));
 
 				Calendar c = Calendar.getInstance();
 				c.setTime(tour.getBeginDate());
 				int year = c.get(Calendar.YEAR);
 				int month = c.get(Calendar.MONTH);
 				int day = c.get(Calendar.DAY_OF_MONTH);
 				int[] tag = new int[] { year, month, day };
 				Button startDateButton = (Button) activity.findViewById(R.id.tour_edit_start_date);
 				startDateButton.setTag(tag);
 				startDateButton.setText(MessageFormat.format("{0}-{1}-{2}", String.valueOf(tag[0]), tag[1] + 1, tag[2]));
 
 				c.setTime(tour.getEndDate());
 				year = c.get(Calendar.YEAR);
 				month = c.get(Calendar.MONTH);
 				day = c.get(Calendar.DAY_OF_MONTH);
 				int[] tag2 = new int[] { year, month, day };
 				Button endDateButton = (Button) activity.findViewById(R.id.tour_edit_end_date);
 				endDateButton.setTag(tag2);
 				endDateButton.setText(MessageFormat.format("{0}-{1}-{2}", String.valueOf(tag2[0]), tag2[1] + 1, tag2[2]));
 
 				Spinner statuSpinner = (Spinner) activity.findViewById(R.id.tour_edit_status);
 				if (tour.getStatus().equals("未进行")) {
 					statuSpinner.setSelection(0);
 				} else if (tour.getStatus().equals("进行中")) {
 					statuSpinner.setSelection(1);
 				} else if (tour.getStatus().equals("已结束")) {
 					statuSpinner.setSelection(2);
 				}
 
 				ListView subtourListView = (ListView) activity.findViewById(R.id.tour_edit_subtourlist);
 				SubtourAdapter subtourAdapter = new SubtourAdapter(activity, SubtourAdapter.EDITABLE, tour.getSubtourList());
 				subtourListView.setAdapter(subtourAdapter);
 				setListViewHeightBasedOnChildren(subtourListView);
 
 			} else if (msg.what == IWebService.ID__SAVE_TOUR) {
 				if (msg.getData().getBoolean(IWebService.WS_RETURN)) {
 					Toast toast = Toast.makeText(activity, "保存完毕", Toast.LENGTH_SHORT);
 					toast.show();
 					refActivity.get().setResult(RESULT_OK);
 					refActivity.get().finish();
 				} else {
 					Toast toast = Toast.makeText(activity, "保存失败", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			}
 
 			else {
 				showNetworkErrorDialog();
 			}
 		}
 
 		private void showNetworkErrorDialog() {
 
 			new AlertDialog.Builder(this.refActivity.get()).setTitle("error").setMessage("网络连接出错").setNeutralButton("返回", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dlg, int sumthin) {
 
 				}
 			}).show();
 
 		}
 	}
 
 	public static void setListViewHeightBasedOnChildren(ListView listView) {
 		ListAdapter listAdapter = listView.getAdapter();
 		if (listAdapter == null) {
 			return;
 		}
 
 		int totalHeight = 0;
 		for (int i = 0; i < listAdapter.getCount(); i++) {
 			View listItem = listAdapter.getView(i, null, listView);
 			listItem.measure(0, 0);
 			totalHeight += listItem.getMeasuredHeight() + 4;
 		}
 
 		ViewGroup.LayoutParams params = listView.getLayoutParams();
 		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
 		((MarginLayoutParams) params).setMargins(4, 4, 4, 4);
 		listView.setLayoutParams(params);
 	}
 
 	private void setSartDateValue(int year, int monthOfYear, int dayOfMonth) {
 		int[] tag = new int[] { year, monthOfYear, dayOfMonth };
 		startDateButton.setTag(tag);
 		startDateButton.setText(MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), monthOfYear + 1, dayOfMonth));
 	}
 
 	private void setEndDateValue(int year, int monthOfYear, int dayOfMonth) {
 		int[] tag = new int[] { year, monthOfYear, dayOfMonth };
 		endDateButton.setTag(tag);
 		endDateButton.setText(MessageFormat.format("{0}-{1}-{2}", String.valueOf(year), monthOfYear + 1, dayOfMonth));
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 		if (view.getTag().equals("start")) {
 			setSartDateValue(year, monthOfYear, dayOfMonth);
 		} else if (view.getTag().equals("end")) {
 			setEndDateValue(year, monthOfYear, dayOfMonth);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case REQ_ADD_SUBTOUR:
 			if (resultCode == RESULT_OK) {
 				int pos = data.getIntExtra("pos", 0);
 				SubtourItem subtourItem = (SubtourItem) data.getSerializableExtra("subtour");

				if (subtourListView.getAdapter() == null) {
					subtourListView.setAdapter(new SubtourAdapter(this, SubtourAdapter.EDITABLE, new ArrayList<SubtourItem>()));
				}

 				((SubtourAdapter) subtourListView.getAdapter()).insert(subtourItem, pos);
 				((SubtourAdapter) subtourListView.getAdapter()).notifyDataSetChanged();
 				setListViewHeightBasedOnChildren(subtourListView);
 			}
 			break;
 		case REQ_EDIT_SUBTOUR:
 			if (resultCode == RESULT_OK) {
 				int pos = data.getIntExtra("pos", 0);
 				SubtourItem subtourItem = (SubtourItem) data.getSerializableExtra("subtour");
 				SubtourItem subtourItem2 = ((SubtourAdapter) subtourListView.getAdapter()).getItem(pos);
 				subtourItem2.setBeginDate(subtourItem.getBeginDate());
 				subtourItem2.setEndDate(subtourItem.getEndDate());
 				subtourItem2.setSightName(subtourItem.getSightName());
 				subtourItem2.setSubtourId(subtourItem.getSubtourId());
 				subtourItem2.setSubtourName(subtourItem.getSubtourName());
 				subtourItem2.setTourId(subtourItem.getTourId());
 				((SubtourAdapter) subtourListView.getAdapter()).notifyDataSetChanged();
 			}
 			break;
 		}
 	}
 }
