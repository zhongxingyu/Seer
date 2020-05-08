 package com.htb.cnk;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.MyOrder.OrderedDish;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.constant.ErrorNum;
 
 /**
  * @author josh
  * 
  */
 public class PhoneActivity extends BaseActivity {
 
 	private Button mBackBtn;
 	private Button mLeftBtn;
 	private Button mSubmitBtn;
 	private Button mRefresh;
 	private TextView mTableNumTxt;
 	private TextView mDishCountTxt;
 	private TextView mTotalPriceTxt;
 	private ListView mMyOrderLst;
 	private MyOrder mMyOrder;
 	private MyOrderAdapter mMyOrderAdapter;
 	private ProgressDialog mpDialog;
 	private TableSetting mSettings = new TableSetting();
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d("onResume", "onResume");
 		mpDialog.setMessage("正在更新数据，请稍等");
 		mpDialog.show();
 		updateTabelInfos();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.myorder_activity);
 		mMyOrder = new MyOrder(PhoneActivity.this);
 		mpDialog = new ProgressDialog(PhoneActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setTitle("请稍等");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		findViews();
 		setClickListener();
 		fillData();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back_btn);
 		mLeftBtn = (Button) findViewById(R.id.left_btn);
 		mSubmitBtn = (Button) findViewById(R.id.submit);
 		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
 		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
 		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
 		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
 		mRefresh = (Button)findViewById(R.id.refresh);
 	}
 
 	private void fillData() {
 		mLeftBtn.setText(R.string.phoneAdd);
 		mTableNumTxt.setText(Info.getTableName());
 		mRefresh.setOnClickListener(refreshBtnClicked);
 		mMyOrderAdapter = getMyOrderAdapterInstance();
 	}
 
 	private MyOrderAdapter getMyOrderAdapterInstance() {
 
 		return new MyOrderAdapter(this, mMyOrder) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				viewHolder1 holder1;
 				Log.d("position", "position:" + position);
 				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
 				if (convertView == null) {
 					convertView = LayoutInflater.from(PhoneActivity.this)
 							.inflate(R.layout.item_ordereddish, null);
 
 					holder1 = new viewHolder1();
 					holder1.dishName = (TextView) convertView
 							.findViewById(R.id.dishName);
 					holder1.dishPrice = (TextView) convertView
 							.findViewById(R.id.dishPrice);
 					holder1.dishQuantity = (TextView) convertView
 							.findViewById(R.id.dishQuantity);
 					holder1.plusBtn = (Button) convertView
 							.findViewById(R.id.dishPlus);
 					holder1.minusBtn = (Button) convertView
 							.findViewById(R.id.dishMinus);
 					holder1.plus5Btn = (Button) convertView
 							.findViewById(R.id.dishPlus5);
 					holder1.minus5Btn = (Button) convertView
 							.findViewById(R.id.dishMinus5);
 
 					convertView.setTag(holder1);
 				} else {
 					holder1 = (viewHolder1) convertView.getTag();
 				}
 				holder1.dishName.setText(dishDetail.getName());
 				holder1.dishPrice
 						.setText(Double.toString(dishDetail.getPrice())
 								+ " 元/份");
 				holder1.dishQuantity.setText(Integer.toString(dishDetail
 						.getQuantity()));
 
 				holder1.plusBtn.setTag(position);
 				holder1.plusBtn.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						final int position = Integer.parseInt(v.getTag()
 								.toString());
 						updateDishQuantity(position, 1);
 					}
 				});
 
 				holder1.minusBtn.setTag(position);
 				holder1.minusBtn.setOnClickListener(minusClicked);
 
 				holder1.plus5Btn.setTag(position);
 
 				holder1.plus5Btn.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						final int position = Integer.parseInt(v.getTag()
 								.toString());
 						updateDishQuantity(position, 5);
 					}
 				});
 
 				holder1.minus5Btn.setTag(position);
 				holder1.minus5Btn.setOnClickListener(minus5Clicked);
 				return convertView;
 			}
 			class viewHolder1 {
 				TextView dishName;
 				TextView dishPrice;
 				TextView dishQuantity;
 				Button plusBtn;
 				Button minusBtn;
 				Button plus5Btn;
 				Button minus5Btn;
 			}
 		};
 	}
 
 	private void setClickListener() {
 		mBackBtn.setOnClickListener(backBtnClicked);
 		mSubmitBtn.setOnClickListener(submitBtnClicked);
 		mLeftBtn.setOnClickListener(leftBtnClicked);
 	}
 
 	Handler queryHandler = new Handler() {
 		public void handleMessage(Message msg) {
 
 			mMyOrderLst.setAdapter(mMyOrderAdapter);
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				mMyOrder.phoneClear();
 				new AlertDialog.Builder(PhoneActivity.this)
 				.setTitle("请注意")
 				.setMessage("无法连接服务器")
 				.setPositiveButton("确定",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								
 							}
 						}).show();
 			} else if (msg.what == MyOrder.RET_NULL_PHONE_ORDER) {
 				mMyOrder.phoneClear();
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.delPhoneWarning),
 						Toast.LENGTH_SHORT).show();
 			}
 			
 			mMyOrderAdapter.notifyDataSetChanged();
 			 mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity())
 			 + " 道菜");
 			 mTotalPriceTxt.setText(Double.toString(mMyOrder.getTotalPrice())
 			 + " 元");
 			mpDialog.cancel();
 		}
 	};
 
 	class queryThread implements Runnable {
 		public void run() {
 			int ret = -1;
 			try {
 				ret = mMyOrder.getPhoneOrderFromServer(Info.getTableId());
 				queryHandler.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 				queryHandler.sendEmptyMessage(ret);
 			}
 		}
 
 	}
 
 	private void updateDishQuantity(final int position, final int quantity) {
 		if (quantity < 0) {
 			mpDialog.setMessage("正在删除...");
 			mpDialog.show();
 			new Thread() {
 				public void run() {
 					int ret = mMyOrder.minus(position, -quantity);
 					delPhoneOrderhandler.sendEmptyMessage(ret);
 				}
 			}.start();
 			
 		} else {
 			mMyOrder.add(position, quantity);
 		}
 
 		mMyOrderAdapter.notifyDataSetChanged();
 		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity())
 				+ " 道菜");
 		mTotalPriceTxt
 				.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
 	}
 
 	private void updateTabelInfos() {
 		new Thread(new queryThread()).start();
 	}
 
 	private void minusDishQuantity(final int position, final int quantity) {
 		if (mMyOrder.getOrderedDish(position).getQuantity() > quantity) {
 			updateDishQuantity(position, -quantity);
 		} else {
 			minusDishDialog(position, quantity);
 		}
 	}
 
 	private void minusDishDialog(final int position, final int quantity) {
 		new AlertDialog.Builder(PhoneActivity.this)
 				.setTitle("请注意")
 				.setMessage(
 						"确认删除"
 								+ mMyOrder.getOrderedDish(position)
 										.getName())
 				.setPositiveButton("确定",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								updateDishQuantity(position, -quantity);
 							}
 						}).setNegativeButton("取消", null).show();
 	}
 
 	//TODO bug might exist
 
 	private void submitThread() {
 		mpDialog.setTitle("请稍等");
 		mpDialog.setMessage("正在提交订单...");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				int ret = mMyOrder.submit();
 				if (ret < 0) {
 					handler.sendEmptyMessage(-1);
 				} else {
					handler.sendEmptyMessage(0);

 					int result = mSettings.getItemTableStatus(Info.getTableId());
					if( result >= 50){
						mSettings.updateStatus(Info.getTableId(),result);
 					}else{
 						mSettings.updateStatus(Info.getTableId(), 1);
 					}

 				}
 			}
 		}.start();
 	}
 
 	private OnClickListener backBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			PhoneActivity.this.finish();
 		}
 	};
 
 	private OnClickListener minusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			minusDishQuantity(position, 1);
 		}
 	};
 
 	private OnClickListener minus5Clicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			minusDishQuantity(position, 5);
 		}
 	};
 
 	private OnClickListener submitBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (mMyOrder.count() <= 0) {
 				new AlertDialog.Builder(PhoneActivity.this)
 						.setTitle("请注意")
 						.setMessage("您还没有点任何东西")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 
 									}
 								}).show();
 				return;
 			}
 			submitThread();
 		}
 	};
 
 	private OnClickListener leftBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(PhoneActivity.this, MenuActivity.class);
 			Info.setMode(Info.WORK_MODE_PHONE);
 			startActivity(intent);
 		}
 	};
 	
 	private OnClickListener refreshBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mpDialog.setMessage("正在更新数据，请稍等");
 			mpDialog.show();
 			updateTabelInfos();
 		}
 	};
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				//TODO combine MyOrder
 				String errMsg = "提交订单失败";
 				if (msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT || msg.what == ErrorNum.PRINTER_ERR_NO_PAPER) {
 					errMsg += ":无法连接打印机或打印机缺纸";
 				}
 				new AlertDialog.Builder(PhoneActivity.this)
 						.setCancelable(false).setTitle("出错了")
 						.setMessage(errMsg).setPositiveButton("确定", null)
 						.show();
 			} else {
 				new AlertDialog.Builder(PhoneActivity.this)
 						.setCancelable(false)
 						.setTitle("提示")
 						.setMessage("订单已提交")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										mMyOrder.clear();
 										finish();
 									}
 								}).show();
 			}
 		}
 	};
 
 	private Handler delPhoneOrderhandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				new AlertDialog.Builder(PhoneActivity.this)
 						.setCancelable(false)
 						.setTitle("出错了")
 						.setMessage("删除失败")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 
 									}
 								}).show();
 			} else {
 				mMyOrderAdapter.notifyDataSetChanged();
 				mDishCountTxt.setText(Integer
 						.toString(mMyOrder.totalQuantity())
 						+ " 道菜");
 				mTotalPriceTxt.setText(Double
 						.toString(mMyOrder.getTotalPrice())
 						+ " 元");
 			}
 		}
 	};
 
 }
