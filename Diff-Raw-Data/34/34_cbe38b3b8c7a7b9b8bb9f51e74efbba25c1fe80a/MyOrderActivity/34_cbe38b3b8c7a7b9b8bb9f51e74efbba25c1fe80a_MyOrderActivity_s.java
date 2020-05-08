 package com.htb.cnk;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder.OrderedDish;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.OrderBaseActivity;
 
 /**
  * @author josh
  *
  */
 public class MyOrderActivity extends OrderBaseActivity {
 	private MyOrderAdapter mMyOrderAdapter;
 	private ProgressDialog mpDialog;
 	private TableSetting mSettings = new TableSetting();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setOrderViews();
 		fillOrderData();
 		setOrderClickListener();
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
 				TextView dishName;
 				TextView dishPrice;
 				TextView dishQuantity;
 				Button plusBtn;
 				Button minusBtn;
 				Button plus5Btn;
 				Button minus5Btn;
 
 				if(convertView==null)
 				{
 					convertView=LayoutInflater.from(MyOrderActivity.this).inflate(R.layout.item_ordereddish, null);
 				}
 				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
 
 				dishName = (TextView) convertView.findViewById(R.id.dishName);
 				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
 				dishQuantity = (TextView) convertView.findViewById(R.id.dishQuantity);
 				plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
 				minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
 				plus5Btn = (Button) convertView.findViewById(R.id.dishPlus5);
 				minus5Btn = (Button) convertView.findViewById(R.id.dishMinus5);
 
 				dishName.setText(dishDetail.getName());
 				dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
 				dishQuantity.setText(Integer.toString(dishDetail.getQuantity()));
 
 				plusBtn.setTag(position);
 				plusBtn.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						final int position = Integer.parseInt(v.getTag().toString());
 						updateDishQuantity(position, 1);
 					}
 				});
 
 				minusBtn.setTag(position);
 				minusBtn.setOnClickListener(minusClicked);
 
 				plus5Btn.setTag(position);
 
 				plus5Btn.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						final int position = Integer.parseInt(v.getTag().toString());
 						updateDishQuantity(position, 5);
 					}
 				});
 
 				minus5Btn.setTag(position);
 
 				minus5Btn.setOnClickListener(minus5Clicked);
 				return convertView;
 			}
 		};
 	}
 
 	private void setOrderClickListener() {
 		mSubmitBtn.setOnClickListener(submitBtnClicked);
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
 			.setMessage("确认删除" + mMyOrder.getOrderedDish(position).getName())
 			.setPositiveButton("确定",
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 							updateDishQuantity(position, -quantity);
 						}
 					})
 			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog,
 						int which) {
 
 				}
 			}).show();
 		}
 	}
 	
 	private OnClickListener backClicked = new OnClickListener() {
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(MyOrderActivity.this, MenuActivity.class);
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
 
 	private OnClickListener submitBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (mMyOrder.count() <= 0) {
 				new AlertDialog.Builder(MyOrderActivity.this)
 				.setTitle("请注意")
 				.setMessage("您还没有点任何东西")
 				.setPositiveButton("确定",
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 
 						}
 				}).show();
 				return ;
 			}
 			mpDialog = new ProgressDialog(MyOrderActivity.this);  
 	        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        mpDialog.setTitle("请稍等");
 	        mpDialog.setMessage("正在提交订单...");  
 	        mpDialog.setIndeterminate(false);
 	        mpDialog.setCancelable(false);
 	        mpDialog.show();
 			new Thread() {
 				public void run() {
 					String ret = mMyOrder.submit();
 					if (ret == null) {
 						handler.sendEmptyMessage(-1);
 					} else {
 						if ("".equals(ret)) {
 							handler.sendEmptyMessage(0);
							mSettings.updatusStatus(Info.getTableId(), 1);
 						} else {
 							handler.sendEmptyMessage(-1);
 						}						
 						Log.d("Respond", ret);
 					}
 				}
 			}.start();
 		}
 	};
 
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				new AlertDialog.Builder(MyOrderActivity.this)
 				.setCancelable(false)
 				.setTitle("出错了")
 				.setMessage("提交订单失败")
 				.setPositiveButton("确定",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 							
 						}
 				})
 				.show();
 			} else {
 				new AlertDialog.Builder(MyOrderActivity.this)
 				.setCancelable(false)
 				.setTitle("提示")
 				.setMessage("订单已提交")
 				.setPositiveButton("确定",
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 							mMyOrder.clear();
 							mMyOrderAdapter.notifyDataSetChanged();
 							finish();
 						}
 				}).show();
 			}
 		}
 	};
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		boolean ret = super.onKeyDown(keyCode, event);
 		Intent intent = new Intent();
 		intent.setClass(MyOrderActivity.this, MenuActivity.class);
 		startActivity(intent);
 		return ret;
 	}
 
 	
 }
