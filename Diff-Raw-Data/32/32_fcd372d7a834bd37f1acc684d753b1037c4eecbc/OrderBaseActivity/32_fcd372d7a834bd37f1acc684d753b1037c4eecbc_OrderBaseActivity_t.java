 package com.htb.cnk.lib;
 
 import java.text.DecimalFormat;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.InputFilter;
 import android.text.method.DigitsKeyListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.LoginDlg;
 import com.htb.cnk.R;
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.PhoneOrder;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.constant.Permission;
 
 /**
  * @author josh
  * 
  */
 public class OrderBaseActivity extends BaseActivity {
 	protected static int FLAVOR = 0;
 	protected Button mBackBtn;
 	protected Button mSubmitBtn;
 	protected Button mLeftBtn;
 	protected Button mRefreshBtn;
 	protected Button mComment;
 	protected TextView mTableNumTxt;
 	protected TextView mDishCountTxt;
 	protected TextView mTotalPriceTxt;
 	protected ListView mMyOrderLst;
 	protected PhoneOrder mMyOrder;
 	protected ProgressDialog mpDialog;
 	protected Handler mSubmitHandler;
 	protected MyOrderAdapter mMyOrderAdapter;
 	protected int persons;
 	private TableSetting mSettings;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.myorder_activity);
 		mMyOrder = new PhoneOrder(OrderBaseActivity.this);
 		mSettings = new TableSetting();
 		mpDialog = new ProgressDialog(OrderBaseActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		getPersons();
 		getFLavor();
 
 		findViews();
 		fillData();
 		setClickListener();
 
 	}
 
 	public void getPersons() {
 		new Thread() {
 			public void run() {
 				int ret = MyOrder.loodPersons(Info.getTableId());
 				if (ret < 0) {
 					persons = 0;
 				} else {
 					persons = ret;
 				}
 			}
 		}.start();
 	}
 
 	public void getFLavor() {
 		new Thread() {
 			public void run() {
 				int ret = mMyOrder.getFLavorFromServer();
 				flavorHandler.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back_btn);
 		mSubmitBtn = (Button) findViewById(R.id.submit);
 		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
 		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
 		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
 		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
 		mLeftBtn = (Button) findViewById(R.id.left_btn);
 		mRefreshBtn = (Button) findViewById(R.id.refresh);
 		mComment = (Button) findViewById(R.id.comment);
 	}
 
 	private void fillData() {
 		mTableNumTxt.setText(Info.getTableName());
 		updateTabelInfos();
 	}
 
 	private void setClickListener() {
 		mBackBtn.setOnClickListener(backBtnClicked);
 		mSubmitBtn.setOnClickListener(submitBtnClicked);
 		mComment.setOnClickListener(commentClicked);
 	}
 
 	protected void updateTabelInfos() {
 		DecimalFormat format = new DecimalFormat("#.00");
 		mDishCountTxt.setText(Integer.toString(mMyOrder.getTotalQuantity())
 				+ " 道菜");
 		mTotalPriceTxt.setText(format.format(mMyOrder.getTotalPrice()) + " 元");
 	}
 
 	protected void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	/**
 	 * @return
 	 */
 	protected int updateStatus(int tableId, int orderType) {
 		return mSettings.updateStatus(Info.getTableId(),
 				TableSetting.PHONE_ORDER);
 	}
 
 	protected void showUpdateQuantityDlg(final int index) {
 		final EditText changeTableText = new EditText(OrderBaseActivity.this);
 		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
 		changeTableText
 				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
 		if (persons > 0) {
 			changeTableText.setText(Integer.toString(persons));
 		}
 		final AlertDialog.Builder quantitySettingDlg = new AlertDialog.Builder(
 				OrderBaseActivity.this);
 		quantitySettingDlg.setTitle("请输入数量");
 		quantitySettingDlg.setIcon(R.drawable.ic_launcher);
 		quantitySettingDlg.setCancelable(false);
 		quantitySettingDlg.setView(changeTableText);
 		quantitySettingDlg.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String quantity;
 						quantity = changeTableText.getEditableText().toString();
 						if (quantity.equals("")) {
 							new AlertDialog.Builder(OrderBaseActivity.this)
 									.setCancelable(false).setTitle("注意")
 									.setMessage("数量不能为空")
 									.setPositiveButton("确定", null).show();
 							return;
 						}
 
 						float quantityValue = Float.parseFloat(quantity);
 						if (quantityValue > 0) {
 							mMyOrder.updateQuantity(index, quantityValue);
 							mMyOrderAdapter.notifyDataSetChanged();
 							updateTabelInfos();
 						} else {
 							new AlertDialog.Builder(OrderBaseActivity.this)
 									.setCancelable(false).setTitle("注意")
 									.setMessage("数量不合法")
 									.setPositiveButton("确定", null).show();
 						}
 
 					}
 				});
 		quantitySettingDlg.setNegativeButton("取消", null);
 		quantitySettingDlg.show();
 	}
 
 	private void customerSubmitOrderDlg() {
 		new AlertDialog.Builder(OrderBaseActivity.this).setTitle("提交订单")
 				.setMessage("呼叫服务员确认订单")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						LoginDlg loginDlg = new LoginDlg(
 								OrderBaseActivity.this, LoginDlg.ACTION_SUBMIT);
 						loginDlg.show(Permission.STUFF);
 					}
 				}).setNegativeButton("继续点菜", null).show();
 	}
 
 	public void submitOrder() {
 		showProgressDlg("正在提交订单...");
 		new Thread() {
 			public void run() {
 				int ret = mMyOrder.submit();
 				mSubmitHandler.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 
 	protected void showSetPersonsDlg() {
 		final EditText changeTableText = new EditText(OrderBaseActivity.this);
 		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
 		changeTableText
 				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
 		if (persons > 0) {
 			changeTableText.setText(Integer.toString(persons));
 		}
 		final AlertDialog.Builder personSettingDlg = new AlertDialog.Builder(
 				OrderBaseActivity.this);
 		personSettingDlg.setTitle("请输入人数");
 		personSettingDlg.setIcon(R.drawable.ic_launcher);
 		personSettingDlg.setCancelable(false);
 		personSettingDlg.setView(changeTableText);
 		personSettingDlg.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String persons;
 						persons = changeTableText.getEditableText().toString();
 						if (persons.equals("")) {
 							new AlertDialog.Builder(OrderBaseActivity.this)
 									.setCancelable(false).setTitle("注意")
 									.setMessage("人数不能为空")
 									.setPositiveButton("确定", null).show();
 							return;
 						}
 
 						int personCount = Integer.parseInt(persons);
 						if (personCount > 0) {
 							mMyOrder.setPersons(personCount);
 							if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 								customerSubmitOrderDlg();
 							} else {
 								submitOrder();
 							}
 						} else {
 							new AlertDialog.Builder(OrderBaseActivity.this)
 									.setCancelable(false).setTitle("注意")
 									.setMessage("人数不合法")
 									.setPositiveButton("确定", null).show();
 						}
 
 					}
 				});
 		personSettingDlg.setNegativeButton("取消", null);
 		personSettingDlg.show();
 	}
 
 	public void flavorDialog(final int position, final boolean[] selected) {
 		new AlertDialog.Builder(OrderBaseActivity.this)
 				.setTitle("口味选择")
 				.setMultiChoiceItems(MyOrder.mFlavor, null,
 						new DialogInterface.OnMultiChoiceClickListener() {
 
 							@Override
 							public void onClick(
 									DialogInterface dialogInterface, int which,
 									boolean isChecked) {
 								selected[which] = isChecked;
 							}
 						})
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialogInterface,
 							int which) {
 						StringBuffer flavorStrBuf = new StringBuffer();
 						String flavorStr = null;
 						for (int i = 0; i < selected.length; i++) {
 							if (selected[i] == true) {
 								flavorStrBuf.append(MyOrder.mFlavor[i] + ",");
 								flavorStr = flavorStrBuf.toString().substring(
 										0, flavorStrBuf.length() - 1);
 							}
 						}
 						mMyOrder.setFlavor(flavorStr, position);
 					}
 				}).setNegativeButton("取消", null).show();
 	}
 
 	private OnClickListener submitBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (Info.getTableId() < 0) {
 				new AlertDialog.Builder(OrderBaseActivity.this).setTitle("请注意")
 						.setMessage("菜谱模式，不能提交订单！")
 						.setPositiveButton("确定", null).show();
 				return;
 			}
 			if (mMyOrder.count() <= 0) {
 				new AlertDialog.Builder(OrderBaseActivity.this).setTitle("请注意")
 						.setMessage("您还没有点任何东西").setPositiveButton("确定", null)
 						.show();
 				return;
 			}
 			if (Setting.enabledPersons()) {
 				showSetPersonsDlg();
 			} else {
 				mMyOrder.setPersons(0);
 				if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 					customerSubmitOrderDlg();
 				} else {
 					submitOrder();
 				}
 			}
 		}
 	};
 
 	private OnClickListener commentClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showComment();
 		}
 	};
 
 	protected void showComment() {
 		LayoutInflater factory = LayoutInflater.from(OrderBaseActivity.this);
 		final View DialogView = factory.inflate(R.layout.comment_dialog, null);
 
 		final EditText commentET = (EditText) DialogView
 				.findViewById(R.id.comment);
 
 		commentET.setText(mMyOrder.getComment());
 
 		AlertDialog dlg = new AlertDialog.Builder(OrderBaseActivity.this)
 				.setTitle("备注").setView(DialogView)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.setComment(commentET.getText().toString());
 					}
 				})
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).setCancelable(false).create();
 		dlg.show();
 	}
 
 	private OnClickListener backBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			OrderBaseActivity.this.finish();
 		}
 	};
 
 	protected OnClickListener flavorClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Button text = (Button) v.findViewById(R.id.flavor);
 			text.setTextColor(android.graphics.Color.WHITE);
 			final int position = Integer.parseInt(v.getTag().toString());
 			final boolean selected[] = new boolean[MyOrder.mFlavor.length];
 			flavorDialog(position, selected);
 		}
 	};
 
 	Handler flavorHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				Toast.makeText(getApplicationContext(),
						"点菜口味数据不对，亲不要使用口味功能，请联系工程师！", Toast.LENGTH_LONG)
 						.show();
 				FLAVOR = 1;
 			} else {
 				FLAVOR = 0;
 			}
 		}
 	};
 
 }
