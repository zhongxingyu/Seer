 package ru.inventos.yum;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import ru.inventos.yum.customHolo.AdapterView;
 import ru.inventos.yum.customHolo.AdapterView.OnItemSelectedListener;
 import ru.inventos.yum.customHolo.Spinner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MyOrders extends Activity implements OnItemSelectedListener,  OrderReceiver {
 	private final static String DATE_FORMAT = "dd.MM.yyyy";
 	private final static byte SORT_BY_DATE = 0;
 	private final static byte SORT_BY_STATUS = 1;
 	private final static byte SORT_BY_COST = 2;
 	private final static byte SORT_BY_TIME = 3;
 	private Spinner mSortSpinner;
 	private MyOrdersListAdapter mListAdapter; 
 	private TextView mPeriodStartText;
 	private TextView mPeriodEndText;
 	private ImageButton mAllBtn;
 	private ImageButton mSelectivelyBtn;
 	private RelativeLayout mPeriodFrame;
 	private GregorianCalendar mCalendar;
 	private String[] mDaysOfWeek;
 	private SimpleDateFormat mFormatter;
 	private NetStorage mNetStorage;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_orders);
 		mCalendar = new GregorianCalendar(); 
 		mDaysOfWeek = this.getResources().getStringArray(R.array.days_of_week_cut);
 		mFormatter = new SimpleDateFormat(DATE_FORMAT);
 		createSortSpinner();
 		findViewsAndInit();
 		createButtons();
 		mNetStorage = new NetStorage(this);
 		mNetStorage.getOrders(this);
 	}
 	
 	private void createSortSpinner() {
 		mSortSpinner = (Spinner) findViewById(R.id.my_orders_sort);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 					R.array.my_orders_sort_type, R.layout.my_orders_sort);
 		adapter.setDropDownViewResource(R.layout.dropdown_item);
 		mSortSpinner.setAdapter(adapter);
 		mSortSpinner.setOnItemSelectedListener(this);
 		mSortSpinner.setSelection(-1);
 		mSortSpinner.setEnabled(false);
 	}
 	
 	public void onPeriodStartClick(View v) {
		mPeriodStartText.setClickable(false);
		mPeriodEndText.setClickable(false);
 		Intent intent = new Intent(this, CalendarActivity.class);
 		Date date = mListAdapter.getMinDate();
 		intent.putExtra(Consts.CALENDAR_DATE, date);
 		startActivityForResult(intent, Consts.MY_ORDERS_PERIOD_START_REQUEST);
 	}
 	
 	public void onPeriodEndClick(View v) {
		mPeriodStartText.setClickable(false);
		mPeriodEndText.setClickable(false);
 		Intent intent = new Intent(this, CalendarActivity.class);
 		Date date = mListAdapter.getMaxDate();
 		intent.putExtra(Consts.CALENDAR_DATE, date);
 		startActivityForResult(intent, Consts.MY_ORDERS_PERIOD_END_REQUEST);
 		
 	}
 	
 	private String getDayOfWeek(Date date) {
 		mCalendar.setTime(date);
 		final int dayOfWeek = mCalendar.get(GregorianCalendar.DAY_OF_WEEK) - 1;
 		return mDaysOfWeek[dayOfWeek];
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Date date = null;
 		String str = null;
 		if (requestCode == Consts.MY_ORDERS_PERIOD_START_REQUEST && data != null) {
 			date = (Date) data.getSerializableExtra(Consts.MY_ORDERS_DATE);
 			if (mListAdapter != null) {
 				mListAdapter.setPeriod(date, null);
 			}
 			str = getDayOfWeek(date) + ", " + mFormatter.format(date);
			mPeriodStartText.setText(str);			
 		}
 		else if (requestCode == Consts.MY_ORDERS_PERIOD_END_REQUEST && data != null) {
 			date = (Date) data.getSerializableExtra(Consts.MY_ORDERS_DATE);
 			if (mListAdapter != null) {
 				mListAdapter.setPeriod(null, date);
 			}
 			str = getDayOfWeek(date) + ", " + mFormatter.format(date);
 			mPeriodEndText.setText(str);
 		}
		mPeriodStartText.setClickable(true);
		mPeriodEndText.setClickable(true);
 	}
 	
 	private void findViewsAndInit() {
 		mPeriodStartText = (TextView) findViewById(R.id.my_orders_period_start);
 		mPeriodEndText = (TextView) findViewById(R.id.my_orders_period_end);
 		mPeriodFrame = (RelativeLayout) findViewById(R.id.my_orders_period_frame);
 		mPeriodFrame.setVisibility(RelativeLayout.GONE);		
 	}
 	
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if (mListAdapter != null) {
 			short currentSort = mListAdapter.getSortMode();
 			short current = (short) Math.abs(currentSort);
 			switch (position) {
 			case SORT_BY_DATE:
 				if (current == MyOrdersListAdapter.SORT_MODE_ASC_BY_DATE) {
 					mListAdapter.setSort((short) (currentSort * -1));
 				}
 				else {
 					mListAdapter.setSort(MyOrdersListAdapter.SORT_MODE_ASC_BY_DATE);
 				}
 				break;
 			case SORT_BY_STATUS:
 				if (current == MyOrdersListAdapter.SORT_MODE_ASC_BY_STATUS) {
 					mListAdapter.setSort((short) (currentSort * -1));
 				}
 				else {
 					mListAdapter.setSort(MyOrdersListAdapter.SORT_MODE_ASC_BY_STATUS);
 				}
 				break;
 			case SORT_BY_COST:
 				if (current == MyOrdersListAdapter.SORT_MODE_ASC_BY_PRICE) {
 					mListAdapter.setSort((short) (currentSort * -1));
 				}
 				else {
 					mListAdapter.setSort(MyOrdersListAdapter.SORT_MODE_ASC_BY_PRICE);
 				}
 				break;
 			case SORT_BY_TIME:
 				if (current == MyOrdersListAdapter.SORT_MODE_ASC_BY_TIME) {
 					mListAdapter.setSort((short) (currentSort * -1));
 				}
 				else {
 					mListAdapter.setSort(MyOrdersListAdapter.SORT_MODE_ASC_BY_TIME);
 				}
 				break;			
 			}
 		}
 	}
 	
 	private void createButtons() {
 		mAllBtn = (ImageButton) findViewById(R.id.my_orders_all_btn);
 		mAllBtn.setSelected(true);
 		mSelectivelyBtn = (ImageButton) findViewById(R.id.my_orders_selectively_btn);
 	}
 	
 	
 	@Override
     public void onNothingSelected(AdapterView<?> parentView) {        
     }
 	
 	private void initList() {
 		mListAdapter.init();
 		Date date = mListAdapter.getMinDate();
 		String str = getDayOfWeek(date) + ", " + mFormatter.format(date);
 		mPeriodStartText.setText(str);		
 		date = mListAdapter.getMaxDate();
 		str = getDayOfWeek(date) + ", " + mFormatter.format(date);
 		mPeriodEndText.setText(str);
 		
 	}
 	
 	public void onAllBtnClick(View v) {
 		if (mListAdapter != null) {
 			mAllBtn.setSelected(true);
 			mSelectivelyBtn.setSelected(false);
 			mPeriodFrame.setVisibility(RelativeLayout.GONE);
 			initList();
 		}
 	}
 	
 	public void onSelectivelyBtnClick(View v) {
 		if (mListAdapter != null) {
 			mAllBtn.setSelected(false);
 			mSelectivelyBtn.setSelected(true);
 			mPeriodFrame.setVisibility(RelativeLayout.VISIBLE);
 		}
 	}
 	
 	@Override
 	public void receiveOrders(OrderItem[] orders) {
 		ListView list = (ListView) findViewById(R.id.my_orders_list);
 		if (orders != null && orders.length > 0) {
 			mSortSpinner.setEnabled(true);
 			mListAdapter = new MyOrdersListAdapter(this, orders);
 			list.setAdapter(mListAdapter);
 			initList();
 		}
 	}
 }
