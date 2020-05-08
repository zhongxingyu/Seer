 package org.sharedsolar.adapter;
 
 import java.util.ArrayList;
 
 import org.sharedsolar.model.AccountModel;
 import org.sharedsolar.model.CreditSummaryModel;
 import org.sharedsolar.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class VendorAddCreditAdapter extends ArrayAdapter<CreditSummaryModel> {
 
 	private LayoutInflater mInflater;
 	private ArrayList<CreditSummaryModel> modelList;
 	private TextView creditAddedTV, newBalanceTV;
 	private Button submitBtn;
 	private int balance;
 
 	public VendorAddCreditAdapter(Context context, int textViewResourceId,
 			ArrayList<CreditSummaryModel> modelList, TextView creditAddedTV,
 			TextView newBalanceTV, Button submitBtn, int balance) {
 		super(context, textViewResourceId);
 		mInflater = LayoutInflater.from(context);
 		this.modelList = modelList;
 		this.creditAddedTV = creditAddedTV;
 		this.newBalanceTV = newBalanceTV;
 		this.submitBtn = submitBtn;
 		this.balance = balance;
 	}
 
 	public int getCount() {
 		return modelList.size();
 	}
 
 	public CreditSummaryModel getItem(int position) {
 		return modelList.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.vendor_add_credit_item,
 					null);
 			holder = new ViewHolder();
 			holder.denominationText = (TextView) convertView
 					.findViewById(R.id.vendorAddCreditDenomination);
 			holder.addedCountText = (TextView) convertView
 					.findViewById(R.id.vendorAddCreditAddedCount);
 			holder.remainCountText = (TextView) convertView
 					.findViewById(R.id.vendorAddCreditRemainCount);
 			holder.minusBtn = (Button) convertView
 					.findViewById(R.id.vendorAddCreditMinusBtn);
 			holder.plusBtn = (Button) convertView
 					.findViewById(R.id.vendorAddCreditPlusBtn);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		CreditSummaryModel model = modelList.get(position);
 		if (model != null) {
 			holder.denominationText.setText(String.valueOf(model
 					.getDenomination()));
 			holder.addedCountText.setText("0");
 			holder.remainCountText.setText(String.valueOf(model.getCount()));
 			holder.minusBtn.setEnabled(false);
 			if (model.getCount() >= 1) {
 				holder.plusBtn.setEnabled(true);
 			} else {
 				holder.plusBtn.setEnabled(false);
 			}
 			holder.plusBtn.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					updateCredit(v, 1);
 				}
 			});
 			holder.minusBtn.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					updateCredit(v, -1);
 				}
 			});
 		}
 		return convertView;
 	}
 
 	public void updateCredit(View v, int sign) {
 		LinearLayout row = (LinearLayout) v.getParent().getParent();
 		TextView denominationText = (TextView) row.getChildAt(1);
 		TextView addedCountText = (TextView) row.getChildAt(2);
 		TextView remainCountText = (TextView) row.getChildAt(3);
 		int denomination = Integer.parseInt(denominationText.getText()
 				.toString());
 		int addedCount = Integer.parseInt(addedCountText.getText().toString());
 		int remainCount = Integer
 				.parseInt(remainCountText.getText().toString());
		int creditAdded = (int)Double.parseDouble(creditAddedTV.getText().toString());
 		// update text
 		addedCount += sign;
 		addedCountText.setText(String.valueOf(addedCount));
 		remainCount -= sign;
 		remainCountText.setText(String.valueOf(remainCount));
 		creditAdded += sign * denomination;
 		creditAddedTV.setText(AccountModel.crIntToString(creditAdded * 100));
 		newBalanceTV.setText(AccountModel.crIntToString(creditAdded * 100 + balance));
 		// minusBtn
 		if (sign == -1) {
 			if (addedCount <= 0) {
 				v.setEnabled(false);
 			}
 			if (remainCount >= 1) {
 				((Button) (((LinearLayout) v.getParent()).getChildAt(0)))
 						.setEnabled(true);
 			}
 		}
 		// plusBtn
 		if (sign == 1) {
 			if (addedCount >= 1) {
 				((Button) (((LinearLayout) v.getParent()).getChildAt(1)))
 						.setEnabled(true);
 			}
 			if (remainCount <= 0) {
 				v.setEnabled(false);
 			}
 		}
 		// submitBtn
 		submitBtn.setEnabled(creditAdded > 0);
 
 	}
 
 	static class ViewHolder {
 		TextView denominationText;
 		TextView addedCountText;
 		TextView remainCountText;
 		Button minusBtn;
 		Button plusBtn;
 	}
 }
