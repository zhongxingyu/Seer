 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.TableAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.ui.base.TableBaseActivity;
 
 public class TableActivity extends TableBaseActivity {
 
 	static final String TAG = "TablesActivity";
 
 	private final String IMAGE_ITEM = "imageItem";
 	private final String ITEM_TEXT = "ItemText";
 
 	private final static int EXTERN_PAGE_NUM = 1; // 除了楼层以外还有几个页面
 	protected Button mBackBtn;
 	protected Button mUpdateBtn;
 	protected Button mStatisticsBtn;
 	protected Button mManageBtn;
 
 	private LinearLayout layoutBottom;
 	ArrayList<HashMap<String, Object>> mTableItem = new ArrayList<HashMap<String, Object>>();
 	private TextView imgCur;
 	private boolean flag = false;
 
 	private ViewPager mPageView;
 	private GridView mGridView;
 	private ArrayList<View> pageViewsList;
 	private LayoutInflater inflater;
 	protected SimpleAdapter mImageItems;
 
 	@Override
 	protected void onResume() {
 		super.onResume();
		showProgressDlg(getResources().getString(R.string.getStatus));
 		if (mImageItems != null) {
 			mTableInfo.clearLstImageItem();
 			updateGridViewAdapter(currentPage);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.table_activity);
 		mTableInfo = new TableAdapter(mTableItem, mNotification, mSettings,
 				TableActivity.this);
 		findViews();
 		mpDialog.show();
 		setClickListeners();
 		setHandler();
 	}
 
 	private void setHandler() {
 		setTableHandler(tableHandler);
 		mNotificationHandler = notificationHandler;
 		setRingtoneHandler(ringtoneHandler);
 		mTotalPriceTableHandler = totalPriceTableHandler;
 		mChangeTIdHandler = changeTIdHandler;
 		mCopyTIdHandler = copyTIdHandler;
 		mCombineTIdHandler = combineTIdHandler;
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		layoutBottom = (LinearLayout) findViewById(R.id.layout_scr_bottom);
 		imgCur = new TextView(TableActivity.this);
 		mPageView = (ViewPager) findViewById(R.id.scr);
 		mOrderNotification = (Button) findViewById(R.id.orderNotification);
 		mStatusBar = (TextView) findViewById(R.id.statusBar);
 	}
 
 	protected void setClickListeners() {
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(checkOutClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	//TODO define
 	Handler changeTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.changeTIdWarning);
 				break;
 			case -1:
 				showNetworkErrDlg("转台失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 				break;
 			default:
 				if (!isPrinterError(msg)) {
 					binderStart();
 				}
 				toastText(R.string.changeSucc);
 				break;
 			}
 		}
 	};
 
 	//TODO define
 	Handler copyTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.copyTIdwarning);
 				break;
 			case -1:
 				showNetworkErrDlg("复制失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 				break;
 			default:
 				intent.setClass(TableActivity.this, MyOrderActivity.class);
 				Info.setMode(Info.WORK_MODE_WAITER);
 				TableActivity.this.startActivity(intent);
 				break;
 			}
 		}
 	};
 
 	Handler combineTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.checkOutWarning);
 				break;
 			case -1:
 				showNetworkErrDlg("合并出错，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 				break;
 			default:
 				if (isPrinterError(msg)) {
 					toastText(R.string.combineError);
 				} else {
 					binderStart();
 					toastText(R.string.combineSucc);
 				}
 				break;
 			}
 		}
 	};
 
 	Handler ringtoneHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what > 0) {
 				mRingtone.play();
 			}
 		}
 	};
 
 	Handler notificationHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				toastText(R.string.notificationWarning);
 			} else {
 				binderStart();
 			}
 		}
 	};
 
 	Handler notificationTypeHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				toastText(R.string.notificationTypeWarning);
 			}
 		}
 	};
 
 	Handler totalPriceTableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				showNetworkErrDlg("统计失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else {
 				if (mTotalPrice <= 0) {
 					toastText(R.string.dishNull);
 				} else {
 					sendPriceToCheckout();
 				}
 			}
 		}
 	};
 
 	/**
 	 * 
 	 */
 	private void sendPriceToCheckout() {
 		Intent checkOutIntent = new Intent();
 		Bundle bundle = new Bundle();
 		bundle.putDouble("price", mTotalPrice);
 		bundle.putIntegerArrayList("tableId",
 				(ArrayList<Integer>) selectedTable);
 		bundle.putStringArrayList("tableName", (ArrayList<String>) tableName);
 		checkOutIntent.putExtras(bundle);
 		checkOutIntent.setClass(TableActivity.this, CheckOutActivity.class);
 		TableActivity.this.startActivity(checkOutIntent);
 	}
 
 	Handler tableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 //				 if (NETWORK_ARERTDIALOG == 1) {
 //				 mNetWrorkcancel.cancel();
 //				 }
 //				showNetworkErrDlg(getResources().getString(
 //						R.string.networkErrorWarning));
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					if (!flag) {
 						initViewPager();
 					}
 					updateGrid(currentPage);
 					flag = true;
 					if (getSettings().hasPendedPhoneOrder()) {
 						ringtoneHandler.sendEmptyMessage(1);
 					}
 					break;
 				default:
 					Log.e(TAG,
 							"unhandled case:"
 									+ msg.what
 									+ (new Exception()).getStackTrace()[2]
 											.getLineNumber());
 					break;
 				}
 			}
 		}
 	};
 
 	private void initViewPager() {
 		imgCur.setTextColor(0xFF4D2412);
 		imgCur.setTextSize(22);
 		layoutBottom.addView(imgCur);
 		setCurPage(0);
 		mPageView.getLayoutParams().height = this.getWindowManager()
 				.getDefaultDisplay().getHeight() * 4 / 5;
 		mImageItems = new SimpleAdapter(TableActivity.this, mTableItem,
 				R.layout.table_item, new String[] { IMAGE_ITEM, ITEM_TEXT },
 				new int[] { R.id.ItemImage, R.id.ItemText }) {
 		};
 
 		inflater = getLayoutInflater();
 		pageViewsList = new ArrayList<View>();
 		pageViewsList.add(inflater.inflate(R.layout.gridview, null));
 		mPageView.setAdapter(new GuidePageAdapter());
 		mPageView.setOnPageChangeListener(new GuidePageChangeListener());
 	}
 
 	/**
 	 * 更新当前页码
 	 */
 	public void setCurPage(int page) {
 		switch (page) {
 		case 0:
 			if (!Setting.enableChargedAreaCheckout()) {
 				imgCur.setText("全部");
 			} else {
 				imgCur.setText("负责区域");
 			}
 			break;
 
 		default:
 			imgCur.setText(page - EXTERN_PAGE_NUM + 1 + "楼");
 			break;
 		}
 		currentPage = page;
 	}
 
 	private void updateGrid(int page) {
 		updateGridViewAdapter(page);
 	}
 
 	/**
 	 * @param page
 	 */
 	private void updateGridViewAdapter(int page) {
 		mTableInfo.clearLstImageItem();
 		mGridView.setOnItemClickListener(tableItemClickListener);
 		switch (page) {
 		case 0:
 			if (!Setting.enableChargedAreaCheckout()) {
 				mTableInfo.filterTables(page, TableAdapter.FILTER_NONE);
 				imgCur.setText("全部");
 			} else {
 				mTableInfo.filterTables(page, TableAdapter.FILTER_SCOPE);
 				imgCur.setText("负责区域");
 			}
 			break;
 
 		default:
 			mTableInfo.filterTables(page - EXTERN_PAGE_NUM,
 					TableAdapter.FILTER_FLOOR);
 			break;
 		}
 
 		mImageItems.notifyDataSetChanged();
 	}
 
 	/** 指引页面改监听器 */
 	class GuidePageChangeListener implements OnPageChangeListener {
 
 		public void onPageScrollStateChanged(int arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onPageScrolled(int arg0, float arg1, int arg2) {
 			// TODO Auto-generated method stub
 		}
 
 		public void onPageSelected(int arg0) {
 			setCurPage(arg0);
 			mTableInfo.clearLstImageItem();
 			updateGridViewAdapter(arg0);
 		}
 
 	}
 
 	/** 指引页面Adapter */
 	class GuidePageAdapter extends PagerAdapter {
 		private ViewGroup layout;
 
 		public void init() {
 			mGridView = (GridView) layout.findViewById(R.id.gridview);
 			mGridView.setAdapter(mImageItems);
 			mGridView.setOnItemClickListener(tableItemClickListener);
 		}
 
 		@Override
 		public int getCount() {
 			return getSettings().getFloorNum() + EXTERN_PAGE_NUM;
 		}
 
 		@Override
 		public boolean isViewFromObject(View arg0, Object arg1) {
 			return arg0 == arg1;
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return super.getItemPosition(object);
 		}
 
 		// 这里是销毁上次滑动的页面，很重要
 		@Override
 		public void destroyItem(View arg0, int arg1, Object arg2) {
 			View view = (View) arg2;
 			((ViewPager) arg0).removeView(view);
 			view = null;
 		}
 
 		// 这里是初始化gridView的过程
 		@Override
 		public Object instantiateItem(View arg0, int arg1) {
 			LayoutInflater inflate = getLayoutInflater();
 			layout = (ViewGroup) inflater.inflate(R.layout.gridview, null);
 			init();
 			((ViewPager) arg0).addView(layout);
 			layout.setTag(arg1);
 			return layout;
 		}
 
 		@Override
 		public void restoreState(Parcelable arg0, ClassLoader arg1) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public Parcelable saveState() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void finishUpdate(View arg0) {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 }
