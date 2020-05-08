 package com.htb.cnk;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.htb.cnk.adapter.StatisticsAdapter;
 import com.htb.cnk.data.Statistics;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.ErrorNum;
 import com.htb.constant.Server;
 
 /**
  * @author josh
  * 
  */
 public class StatisticsActivity extends BaseActivity {
 	public final static int QUERY_BY_TIME = 0;
 	public final static int QUERY_TODAY = 1;
 	private Button mBackBtn;
 	private Button mQueryTodayBtn;
 	private Button mQueryByTimeBtn;
 	private Button mPrintBtn;
 	private Button mStartTimeBtn;
 	private Button mStartDateBtn;
 	private Button mEndTimeBtn;
 	private Button mEndDateBtn;
 	private TextView mStartDate;
 	private TextView mStartTime;
 	private TextView mEndDate;
 	private TextView mEndTime;
 	private TextView mTotalAmount;
 	private ListView mSalesData;
 	ProgressDialog mpDialog;
 
 	private Calendar mStart = Calendar.getInstance();
 	private Calendar mEnd = Calendar.getInstance();
 	private Calendar mStartSet = Calendar.getInstance();
 	private Calendar mEndSet = Calendar.getInstance();
 	private String mLatestStatistics;
 	private int mQueryMode;
 	private Statistics mStatistics;
 	private StatisticsAdapter mStatisticsAdapter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.statistics_activity);
 		mStatistics = new Statistics(StatisticsActivity.this);
 		findViews();
 		setClickListener();
 		setAdapter();
 		downloadDB();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mQueryTodayBtn = (Button) findViewById(R.id.queryToday);
 		mQueryByTimeBtn = (Button) findViewById(R.id.queryByTime);
 		mPrintBtn = (Button) findViewById(R.id.print);
 		mStartDateBtn = (Button) findViewById(R.id.setStartDate);
 		mStartTimeBtn = (Button) findViewById(R.id.setStartTime);
 		mEndDateBtn = (Button) findViewById(R.id.setEndDate);
 		mEndTimeBtn = (Button) findViewById(R.id.setEndTime);
 		mStartDate = (TextView) findViewById(R.id.startDate);
 		mStartTime = (TextView) findViewById(R.id.startTime);
 		mEndDate = (TextView) findViewById(R.id.endDate);
 		mEndTime = (TextView) findViewById(R.id.endTime);
 		mTotalAmount = (TextView) findViewById(R.id.totalAmount);
 		mSalesData = (ListView) findViewById(R.id.salesData);
 	}
 
 	private void setClickListener() {
 		mBackBtn.setOnClickListener(backClicked);
 		mQueryByTimeBtn.setOnClickListener(queryByTimeClicked);
 		mQueryTodayBtn.setOnClickListener(queryTodayClicked);
 		mPrintBtn.setOnClickListener(printClicked);
 		mStartDateBtn.setOnClickListener(startDateClicked);
 		mStartTimeBtn.setOnClickListener(startTimeClicked);
 		mEndDateBtn.setOnClickListener(endDateClicked);
 		mEndTimeBtn.setOnClickListener(endTimeClicked);
 	}
 
 	private void downloadDB() {
 		mpDialog = new ProgressDialog(StatisticsActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setTitle("请稍等..");
 		mpDialog.setMessage("正在加载销售数据...");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mpDialog.show();
 		new Thread() {
 			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 				int ret = mStatistics.downloadDB(Server.SERVER_DB_SALES);
 				if (ret < 0) {
 					handler.sendEmptyMessage(ret);
 					return;
 				}
 
 				String respond = Http.get(Server.LATEST_STATISTICS, "do=get");
 				if (respond == null) {
 					handler.sendEmptyMessage(ErrorNum.GET_LATEST_STATISTICS_FAILED);
 					return;
 				}
 				int start = respond.indexOf("[") + 1;
 				int end = respond.indexOf("]");
 				mLatestStatistics = respond.substring(start, end);
 				handler.sendEmptyMessage(0);
 			}
 		}.start();
 	}
 
 	private void setAdapter() {
 		mStatisticsAdapter = new StatisticsAdapter(getApplicationContext(),
 				mStatistics) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				TextView dishName;
 				TextView salesCount;
 				TextView totalAmount;
 				TextView percentage;
 
 				if (convertView == null) {
 					convertView = LayoutInflater.from(StatisticsActivity.this)
 							.inflate(R.layout.item_statistics, null);
 				}
 
 				dishName = (TextView) convertView.findViewById(R.id.dishName);
 				salesCount = (TextView) convertView
 						.findViewById(R.id.salesCount);
 				totalAmount = (TextView) convertView
 						.findViewById(R.id.totalAmount);
 				percentage = (TextView) convertView
 						.findViewById(R.id.percentage);
 
 				dishName.setText(mStatistics.getName(position));
 				salesCount.setText(Integer.toString(mStatistics
 						.getQuantity(position)));
 				totalAmount.setText(Double.toString(mStatistics
 						.getAmount(position)));
 				DecimalFormat df = new DecimalFormat("0.00");
 				double percent = (mStatistics.getAmount(position) * 100)
 						/ mStatistics.getTotalAmount();
 				percentage.setText(df.format(percent) + "%");
 				return convertView;
 			}
 		};
 		mSalesData.setAdapter(mStatisticsAdapter);
 	}
 
 	private boolean isDateTimeSet() {
 		if (mStartDateBtn.getText().toString().indexOf("-") <= 0) {
 			DateTimeNotSetAlert("没有设置开始日期");
 			return false;
 		}
 
 		if (mStartTimeBtn.getText().toString().indexOf(":") <= 0) {
 			DateTimeNotSetAlert("没有设置开始时间");
 			return false;
 		}
 
 		if (mEndDateBtn.getText().toString().indexOf("-") <= 0) {
 			DateTimeNotSetAlert("没有设置结束日期");
 			return false;
 		}
 
 		if (mEndTimeBtn.getText().toString().indexOf(":") <= 0) {
 			DateTimeNotSetAlert("没有设置结束时间");
 			return false;
 		}
 
 		return true;
 	}
 
 	private void DateTimeNotSetAlert(String err) {
 		new AlertDialog.Builder(StatisticsActivity.this).setTitle("请注意")
 				.setMessage(err)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						downloadDB();
 					}
 				}).show();
 	}
 
 	private void updateLatestStatistics() {
 		new AlertDialog.Builder(StatisticsActivity.this)
 				.setTitle("请注意")
 				.setMessage("是否清零销售记录")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						new Thread() {
 							public void run() {
 								SimpleDateFormat time = new SimpleDateFormat(
 										"HH:mm");
 								SimpleDateFormat date = new SimpleDateFormat(
 										"yyyy-MM-dd");
 								mLatestStatistics = date.format(mEnd.getTime())
 										+ " " + time.format(mEnd.getTime());
 								Http.get(
 										Server.LATEST_STATISTICS,
 										"do=set&time="
 												+ time.format(mEnd.getTime())
 												+ "&date="
 												+ date.format(mEnd.getTime()));
 
 							}
 						}.start();
 					}
 				})
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				}).show();
 	}
 
 	private void updateData(Calendar start, Calendar end) {
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		mStartDate.setText(date.format(start.getTime()));
 		mStartTime.setText(time.format(start.getTime()));
 		mEndDate.setText(date.format(end.getTime()));
 		mEndTime.setText(time.format(end.getTime()));
 		mTotalAmount.setText(Double.toString(mStatistics.getTotalAmount()));
 		mStatisticsAdapter.notifyDataSetChanged();
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			StatisticsActivity.this.finish();
 		}
 	};
 
 	private void dbErrAlert() {
 		new AlertDialog.Builder(StatisticsActivity.this).setTitle("错误")
 				.setMessage("销售数据出错,需从新下载!")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				}).show();
 	}
 
 	private OnClickListener queryTodayClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			Date d;
 			try {
 				d = df.parse(mLatestStatistics);
 				mStart.setTime(d);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			mEnd.setTimeInMillis(System.currentTimeMillis());
			
			int ret = mStatistics.perpareResult(mStart, mEnd);
			if (ret < 0) {
				dbErrAlert();
				return;
			}
 			updateData(mStart, mEnd);
 			mQueryMode = QUERY_TODAY;
 		}
 	};
 
 	private OnClickListener queryByTimeClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (isDateTimeSet()) {
 				int ret = mStatistics.perpareResult(mStartSet, mEndSet);
 				if (ret < 0) {
 					dbErrAlert();
 					return;
 				}
 				updateData(mStartSet, mEndSet);
 				mQueryMode = QUERY_BY_TIME;
 			}
 		}
 	};
 
 	private OnClickListener printClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (mStatistics.count() <= 0) {
 				Toast.makeText(getApplicationContext(), "没有可打印信息",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 			new Thread() {
 				public void run() {
 					int ret = mStatistics.print(mStart, mEnd);
 					handlerPrint.sendEmptyMessage(ret);
 				}
 			}.start();
 
 			if (mQueryMode == QUERY_TODAY) {
 				updateLatestStatistics();
 			}
 		}
 	};
 
 	private OnClickListener startDateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			DatePickerDialog date = new DatePickerDialog(
 					StatisticsActivity.this, startDateListener,
 					mStart.get(Calendar.YEAR), mStart.get(Calendar.MONTH),
 					mStart.get(Calendar.DAY_OF_MONTH));
 			date.setCancelable(false);
 			date.show();
 		}
 	};
 
 	private OnClickListener startTimeClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TimePickerDialog time = new TimePickerDialog(
 					StatisticsActivity.this, startTimeListener,
 					mStart.get(Calendar.HOUR_OF_DAY),
 					mStart.get(Calendar.MINUTE), true);
 			time.setCancelable(false);
 			time.show();
 		}
 	};
 
 	private OnClickListener endDateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			DatePickerDialog date = new DatePickerDialog(
 					StatisticsActivity.this, endDateListener,
 					mEnd.get(Calendar.YEAR), mEnd.get(Calendar.MONTH),
 					mEnd.get(Calendar.DAY_OF_MONTH));
 			date.setCancelable(false);
 			date.show();
 		}
 	};
 
 	private OnClickListener endTimeClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TimePickerDialog time = new TimePickerDialog(
 					StatisticsActivity.this, endTimeListener,
 					mEnd.get(Calendar.HOUR_OF_DAY), mEnd.get(Calendar.MINUTE),
 					true);
 			time.setCancelable(false);
 			time.show();
 		}
 	};
 
 	private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker arg0, int year, int month, int day) {
 			mStartSet.set(year, month, day);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			mStartDateBtn.setText(df.format(mStartSet.getTime()));
 		}
 	};
 
 	private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker arg0, int year, int month, int day) {
 			mEndSet.set(year, month, day);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			mEndDateBtn.setText(df.format(mEndSet.getTime()));
 		}
 	};
 
 	private TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mStartSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			mStartSet.set(Calendar.MINUTE, minute);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			mStartTimeBtn.setText(df.format(mStartSet.getTime()));
 		}
 	};
 
 	private TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mEndSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			mEndSet.set(Calendar.MINUTE, minute);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			mEndTimeBtn.setText(df.format(mEndSet.getTime()));
 		}
 	};
 
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				mpDialog.cancel();
 				new AlertDialog.Builder(StatisticsActivity.this)
 						.setTitle("错误")
 						.setMessage("下载销售数据失败,错误码:" + msg.what)
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										finish();
 									}
 								}).show();
 			} else {
 				mpDialog.cancel();
 			}
 		}
 	};
 
 	private Handler handlerPrint = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				mpDialog.cancel();
 				new AlertDialog.Builder(StatisticsActivity.this)
 						.setTitle("错误")
 						.setMessage("打印出现错误,请检查打印机" + msg.what)
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 									}
 								}).show();
 			} else {
 				mpDialog.cancel();
 			}
 		}
 	};
 }
