 package com.htb.cnk;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.OrderedDish;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.OrderBaseActivity;
 import com.htb.cnk.ui.base.TableBaseActivity;
 import com.htb.constant.ErrorNum;
 
 /**
  * @author josh
  * 
  */
 public class MyOrderActivity extends OrderBaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setOrderViews();
 		fillOrderData();
 		setOrderClickListener();
 		mSubmitHandler = handler;
 	}
 
 	private void setOrderViews() {
 		mLeftBtn.setVisibility(View.GONE);
 		mRefreshBtn.setVisibility(View.GONE);
 	}
 
 	private void fillOrderData() {
 		mMyOrderAdapter = getMyOrderAdapterInstance();
 		mMyOrderLst.setAdapter(mMyOrderAdapter);
 	}
 
 	private MyOrderAdapter getMyOrderAdapterInstance() {
 		return new MyOrderAdapter(this, mMyOrder) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
 				ItemViewHolder itemViewHolder;
 				if (convertView == null) {
 					convertView = LayoutInflater.from(MyOrderActivity.this)
 							.inflate(R.layout.item_ordereddish, null);
 					itemViewHolder = new ItemViewHolder(convertView);
 					itemViewHolder.setOnClickListener();
 					convertView.setTag(itemViewHolder);
 				} else {
 					itemViewHolder = (ItemViewHolder) convertView.getTag();
 				}
 				itemViewHolder.setTag(position);
 				itemViewHolder.fillData(dishDetail);
 				return convertView;
 			}
 		};
 	}
 
 	private void setOrderClickListener() {
 		mBackBtn.setOnClickListener(backClicked);
 	}
 
 	private void updateDishQuantity(int position, int quantity) {
 		if (quantity < 0) {
 			mMyOrder.minus(position, -quantity);
 		} else {
 			mMyOrder.add(position, quantity);
 		}
 
 		mMyOrderAdapter.notifyDataSetChanged();
 		updateTabelInfos();
 	}
 
 	private void minusDishQuantity(final int position, final int quantity) {
 		if (mMyOrder.getOrderedDish(position).getQuantity() > quantity) {
 			updateDishQuantity(position, -quantity);
 		} else {
 			new AlertDialog.Builder(MyOrderActivity.this)
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
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			if (Info.getMenu() == Info.ORDER_LIST_MENU) {
 				intent.setClass(MyOrderActivity.this, MenuActivity.class);
 			} else {
 				intent.setClass(MyOrderActivity.this, QuickMenuActivity.class);
 			}
 			startActivity(intent);
 			finish();
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
 
 	private OnLongClickListener quantityClicked = new OnLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			showUpdateQuantityDlg(position);
 			return false;
 		}
 
 	};
 
 	Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				errMsg(msg);
 			} else {
 				updateItemStatus();
 				submitSucceed();
 			}
 		}
 
 		private void updateItemStatus() {
 			new Thread() {
 				public void run() {
 					try {
 						int ret = updateStatus(Info.getTableId(),
 								TableSetting.MY_ORDER);
 						if (ret < 0) {
 							handler.sendEmptyMessage(ret);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}.start();
 		}
 
 		private void errMsg(Message msg) {
 			String errMsg = "提交订单失败";
 			if (msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
 					|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER) {
 				errMsg += ":无法连接打印机或打印机缺纸";
 			}
 			new AlertDialog.Builder(MyOrderActivity.this).setCancelable(false)
 					.setTitle("出错了").setMessage(errMsg)
 					.setPositiveButton("确定", null).show();
 		}
 	};
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		boolean ret = super.onKeyDown(keyCode, event);
 		Intent intent = new Intent();
 		intent.setClass(MyOrderActivity.this, MenuActivity.class);
 		startActivity(intent);
 		return ret;
 	}
 
 	private void submitSucceed() {
 		new AlertDialog.Builder(MyOrderActivity.this).setCancelable(false)
 				.setTitle("提示").setMessage("订单已提交")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.clear();
 						mMyOrderAdapter.notifyDataSetChanged();
 						finish();
 						if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 							Info.setMode(Info.WORK_MODE_WAITER);
 							Intent intent = new Intent();
 							intent.setClass(MyOrderActivity.this,
									TableActivity.class);
 							startActivity(intent);
 						}
 					}
 				}).show();
 	}
 
 	class ItemViewHolder {
 		TextView dishName;
 		TextView dishPrice;
 		TextView dishQuantity;
 		Button plusBtn;
 		Button minusBtn;
 		// Button plus5Btn;
 		// Button minus5Btn;
 		Button flavorBtn;
 
 		public ItemViewHolder(View convertView) {
 			dishName = (TextView) convertView.findViewById(R.id.dishName);
 			dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
 			dishQuantity = (TextView) convertView
 					.findViewById(R.id.dishQuantity);
 			plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
 			minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
 			// plus5Btn = (Button) convertView.findViewById(R.id.dishPlus5);
 			// minus5Btn = (Button) convertView.findViewById(R.id.dishMinus5);
 			flavorBtn = (Button) convertView.findViewById(R.id.flavor);
 		}
 
 		public void setOnClickListener() {
 			plusBtn.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					final int position = Integer
 							.parseInt(v.getTag().toString());
 					updateDishQuantity(position, 1);
 				}
 			});
 			// plus5Btn.setOnClickListener(new OnClickListener() {
 			//
 			// public void onClick(View v) {
 			// final int position = Integer
 			// .parseInt(v.getTag().toString());
 			// updateDishQuantity(position, 5);
 			// }
 			// });
 			minusBtn.setOnClickListener(minusClicked);
 			// minus5Btn.setOnClickListener(minus5Clicked);
 
 			flavorBtn.setOnClickListener(flavorClicked);
 			dishQuantity.setOnLongClickListener(quantityClicked);
 		}
 
 		public void setTag(int position) {
 			plusBtn.setTag(position);
 			minusBtn.setTag(position);
 			// plus5Btn.setTag(position);
 			// minus5Btn.setTag(position);
 			flavorBtn.setTag(position);
 			dishQuantity.setTag(position);
 		}
 
 		public void fillData(OrderedDish dishDetail) {
 			dishName.setText(dishDetail.getName());
 			dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
 			dishQuantity
 					.setText(MyOrder.convertFloat(dishDetail.getQuantity()));
 
 		}
 	}
 }
