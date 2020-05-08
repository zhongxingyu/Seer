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
 
 	private ViewGroup layout;
 	private ViewPager mPageView;
 	private GridView mGridView;
 	private ArrayList<View> pageViewsList;
 	private LayoutInflater inflater;
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (NETWORK_ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			NETWORK_ARERTDIALOG = 0;
 		}
 		showProgressDlg(getResources().getString(R.string.getStatus));
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Info.setMode(Info.WORK_MODE_WAITER);
 		setContentView(R.layout.table_activity);
 		findViews();
 		mpDialog.show();
 		setClickListeners();
 		setHandler();
 		mTableInfo = new TableAdapter(mTableItem, mNotification, mSettings);
 	}
 
 	private void setHandler() {
 		setTableHandler(tableHandler);
 		mNotificationHandler = notificationHandler;
 		setRingtoneHandler(ringtoneHandler);
 		mTotalPriceTableHandler = totalPriceTableHandler;
 		mChangeTIdHandler = changeTIdHandler;
 		mCopyTIdHandler = copyTIdHandler;
 		mCombineTIdHandler = combineTIdHandler;
 		mNotificationTypeHandler = notificationTypeHandler;
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		layoutBottom = (LinearLayout) findViewById(R.id.layout_scr_bottom);
 		imgCur = new TextView(TableActivity.this);
 		mPageView = (ViewPager) findViewById(R.id.scr);
 	}
 
 	protected void setClickListeners() {
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(checkOutClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	Handler changeTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.changeTIdWarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("转台失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else if (isPrinterError(msg)) {
 				toastText(R.string.changeSucc);
 			} else {
 				binderStart();
 				toastText(R.string.changeSucc);
 			}
 		}
 	};
 
 	Handler copyTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.copyTIdwarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("复制失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else {
 				intent.setClass(TableActivity.this, MyOrderActivity.class);
 				Info.setMode(Info.WORK_MODE_WAITER);
 				TableActivity.this.startActivity(intent);
 			}
 		}
 	};
 
 	Handler combineTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.checkOutWarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("合并出错，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else if (isPrinterError(msg)) {
 				toastText(R.string.combineError);
 			} else {
 				binderStart();
 				toastText(R.string.combineSucc);
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
 				netWorkDialogShow("统计失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else {
 				mTotalPrice = (double) msg.what;
 				if (mTotalPrice <= 0) {
 					toastText(R.string.dishNull);
 				} else {
 					sendPriceToCheckout();
 				}
 			}
 			mpDialog.cancel();
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
 				if (NETWORK_ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				netWorkDialogShow(getResources().getString(
 						R.string.networkErrorWarning));
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
 				case DISABLE_GRIDVIEW:
 					if (mGridView != null)
 						mGridView.setOnItemClickListener(null);
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
 
 	public void init(int page) {
 		mGridView = (GridView) layout.findViewById(R.id.gridview);
 		mGridView.setAdapter(mImageItems);
 		mGridView.setOnItemClickListener(tableItemClickListener);
 	}
 
 	/**
 	 * 更新当前页码
 	 */
 	public void setCurPage(int page) {
 		switch (page) {
 		case 0:
 			imgCur.setText("全部");
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
 		mGridView.setOnItemClickListener(tableItemClickListener);
 		switch (page) {
 		case 0:
 			mTableInfo.filterTables(page, TableAdapter.FILTER_NONE);
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
 			updateGridViewAdapter(arg0);
 		}
 
 	}
 
 	/** 指引页面Adapter */
 	class GuidePageAdapter extends PagerAdapter {
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
 			init(arg1);
 			((ViewPager) arg0).addView(layout);
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
