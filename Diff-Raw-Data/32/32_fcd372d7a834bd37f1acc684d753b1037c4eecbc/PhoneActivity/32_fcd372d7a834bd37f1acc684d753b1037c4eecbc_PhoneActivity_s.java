 package com.htb.cnk;
 
 import android.app.AlertDialog;
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
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.OrderedDish;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.OrderBaseActivity;
 
 public class PhoneActivity extends OrderBaseActivity {
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		showProgressDlg("正在更新数据，请稍等");
 		updatePhoneOrderInfos();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setPhoneOrderClickListener();
 		fillPhoneOrderData();
 		mSubmitHandler = handler;
 	}
 
 	public void showDeletePhoneOrderProcessDlg() {
 		delPhoneOrderhandler.sendEmptyMessage(2);
 	}
 
 	private void setPhoneOrderClickListener() {
 		mBackBtn.setOnClickListener(backBtnClicked);
 		mLeftBtn.setOnClickListener(leftBtnClicked);
 		mRefreshBtn.setOnClickListener(refreshBtnClicked);
 	}
 
 	private void fillPhoneOrderData() {
 		mLeftBtn.setText(R.string.phoneAdd);
 		mTableNumTxt.setText(Info.getTableName());
 		mMyOrderAdapter = getMyOrderAdapterInstance();
 	}
 
 	private MyOrderAdapter getMyOrderAdapterInstance() {
 
 		return new MyOrderAdapter(this, mMyOrder) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				viewHolder1 holder1;
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
 					holder1.flavorBtn = (Button) convertView
 							.findViewById(R.id.flavor);
 					convertView.setTag(holder1);
 				} else {
 					holder1 = (viewHolder1) convertView.getTag();
 				}
 
 				holder1.dishName.setText(dishDetail.getName());
 				holder1.dishPrice
 						.setText(Double.toString(dishDetail.getPrice())
 								+ " 元/份");
 				holder1.dishQuantity.setText(MyOrder.convertFloat(dishDetail
 						.getQuantity()));
 
 				holder1.plusBtn.setTag(position);
 				holder1.plusBtn.setOnClickListener(plusClicked);
 
 				holder1.minusBtn.setTag(position);
 				holder1.minusBtn.setOnClickListener(minusClicked);
 
 				holder1.flavorBtn.setTag(position);
				holder1.flavorBtn.setOnClickListener(flavorClicked);
 				return convertView;
 			}
 
 			class viewHolder1 {
 				TextView dishName;
 				TextView dishPrice;
 				TextView dishQuantity;
 				Button plusBtn;
 				Button minusBtn;
 				Button flavorBtn;
 			}
 		};
 	}
 
 	Handler queryHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mMyOrderLst.setAdapter(mMyOrderAdapter);
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				mMyOrder.phoneClear();
 				queryWarningDialog();
 			} else if (msg.what == MyOrder.RET_NULL_PHONE_ORDER) {
 				mMyOrder.phoneClear();
 				toastText(R.string.delPhoneWarning);
 			}
 			mMyOrderAdapter.notifyDataSetChanged();
 			updateTabelInfos();
 		}
 	};
 	protected void queryThread() {
 		new Thread() {
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
 		}.start();
 	}
 
 	private void updateDishQuantity(final int position, final int quantity) {
 		if (quantity < 0) {
 			minusThread(position, quantity);
 		} else {
 			mMyOrder.add(position, quantity);
 			mMyOrderAdapter.notifyDataSetChanged();
 			updateTabelInfos();
 		}
 
 	}
 
 	private void minusThread(final int position, final int quantity) {
 		new Thread() {
 			public void run() {
 				int ret = mMyOrder.minus(position, -quantity);
 				delPhoneOrderhandler.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 
 	private void updatePhoneOrderInfos() {
 		queryThread();
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
 						"确认删除" + mMyOrder.getOrderedDish(position).getName())
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						updateDishQuantity(position, -quantity);
 					}
 				}).setNegativeButton("取消", null).show();
 	}
 
 	private void cleanThread() {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = updateStatus(Info.getTableId(),TableSetting.PHONE_ORDER); 
 					if(ret < 0){
 						queryHandler.sendEmptyMessage(ret);
 						return;
 					}
 					ret = mMyOrder.cleanServerPhoneOrder(Info.getTableId());
 					if (ret < 0) {
 						queryHandler.sendEmptyMessage(ret);
 						return;
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void phoneWarningDialog() {
 		new AlertDialog.Builder(PhoneActivity.this).setCancelable(false)
 				.setTitle("提示").setMessage("订单已提交")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				}).show();
 	}
 
 	/**
 	 * 
 	 */
 	private void queryWarningDialog() {
 		new AlertDialog.Builder(PhoneActivity.this).setTitle("请注意")
 				.setMessage("无法连接服务器").setPositiveButton("确定", null)
 				.show();
 	}
 
 	/**
 	 * @param errMsg
 	 */
 	private void errMsgDialog(String errMsg) {
 		new AlertDialog.Builder(PhoneActivity.this)
 				.setCancelable(false).setTitle("出错了")
 				.setMessage(errMsg).setPositiveButton("确定", null)
 				.show();
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
 
 	private OnClickListener plusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			updateDishQuantity(position, 1);
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
 			showProgressDlg("正在更新数据，请稍等");
 			updatePhoneOrderInfos();
 		}
 	};
 
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				// TODO combine MyOrder
 				String errMsg = "提交订单失败";
 				if (isPrinterError(msg)) {
 					errMsg += ":无法连接打印机或打印机缺纸";
 				}
 				errMsgDialog(errMsg);
 			} else {
 				cleanThread();
 				phoneWarningDialog();
 			}
 		}
 	};
 
 	private Handler delPhoneOrderhandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				errMsgDialog("删除失败");
 			} else {
 				switch (msg.what) {
 				case 0:
 					mMyOrderAdapter.notifyDataSetChanged();
 					updateTabelInfos();
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 }
