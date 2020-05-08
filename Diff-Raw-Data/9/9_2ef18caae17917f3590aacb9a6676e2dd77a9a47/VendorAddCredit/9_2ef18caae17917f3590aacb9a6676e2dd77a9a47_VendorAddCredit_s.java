 package org.sharedsolar;
 
 import java.util.ArrayList;
 
 import org.sharedsolar.R;
 import org.sharedsolar.adapter.VendorAddCreditAdapter;
 import org.sharedsolar.db.DatabaseAdapter;
 import org.sharedsolar.model.AccountModel;
 import org.sharedsolar.model.CreditSummaryModel;
 import org.sharedsolar.tool.Connector;
 import org.sharedsolar.tool.MyUI;
 
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class VendorAddCredit extends ListActivity {
 
 	private ArrayList<CreditSummaryModel> modelList;
 	private ArrayList<CreditSummaryModel> addedModelList;
 	private AccountModel accountModel;
 	private VendorAddCreditAdapter vendorAddCreditAdapter;
 	private DatabaseAdapter dbAdapter;
 	private String info;
 	private int newCr;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.vendor_add_credit);
 
 		// get account model from extras
 		Bundle extras = getIntent().getExtras();
 		accountModel = new AccountModel(extras.getString("aid"),
 				extras.getString("cid"), extras.getInt("cr"));
 		((TextView) findViewById(R.id.vendorAddCreditAid)).setText(accountModel
 				.getAid());
 		((TextView) findViewById(R.id.vendorAddCreditBalanceTV))
 				.setText(accountModel.getCrString());
 		((TextView) findViewById(R.id.vendorAddCreditNewBalanceTV))
 				.setText(accountModel.getCrString());
 
 		// get model list from db
 		dbAdapter = new DatabaseAdapter(this);
 		dbAdapter.open();
 		modelList = dbAdapter.getCreditSummaryModelList();
 		dbAdapter.close();
 
 		// list adapter
 		vendorAddCreditAdapter = new VendorAddCreditAdapter(this,
 				R.layout.vendor_add_credit_item, modelList,
 				(TextView) findViewById(R.id.vendorAddCreditAddedTV),
 				(TextView) findViewById(R.id.vendorAddCreditNewBalanceTV),
 				(Button) findViewById(R.id.vendorAddCreditSubmitBtn),
 				accountModel.getCr());
 		setListAdapter(vendorAddCreditAdapter);
 
 		// submit
 		Button submitBtn = (Button) findViewById(R.id.vendorAddCreditSubmitBtn);
 		submitBtn.setOnClickListener(submitBtnClickListener);
 		submitBtn.setEnabled(false);
 	}
 
 	View.OnClickListener submitBtnClickListener = new View.OnClickListener() {
 		public void onClick(View view) {
 			info = getString(R.string.accountLabel) + " "
 					+ accountModel.getAid() + "\n\n";
 			// build new model list
 			ListView list = getListView();
 			addedModelList = new ArrayList<CreditSummaryModel>();
 			for (int i = 0; i < list.getChildCount(); i++) {
 				LinearLayout row = (LinearLayout) list.getChildAt(i);
 				TextView denominationTV = (TextView) row.getChildAt(1);
 				TextView addedCountTV = (TextView) row.getChildAt(2);
 				int denomination = Integer.parseInt(denominationTV.getText()
 						.toString());
 				int addedCount = Integer.parseInt(addedCountTV.getText()
 						.toString());
 				if (addedCount != 0) {
 					addedModelList.add(new CreditSummaryModel(denomination,
 							addedCount));
 					info += getString(R.string.denomination) + " "
 							+ denomination + ": " + addedCount + " "
 							+ getString(R.string.added) + "\n";
 				}
 			}
 			// update info string
 			String creditAdded = ((TextView) findViewById(R.id.vendorAddCreditAddedTV))
 					.getText().toString();
 			String newBalance = ((TextView) findViewById(R.id.vendorAddCreditNewBalanceTV))
 					.getText().toString();
			newCr = (int) Double.parseDouble(creditAdded);
 			if (newCr > 0) {
 				info += "\n" + getString(R.string.creditAddedLabel) + " "
 						+ creditAdded + "\n"
 						+ getString(R.string.newBalanceLabel) + " "
 						+ newBalance;
 				String message = info + "\n\n"
 						+ getString(R.string.addCreditConfirm);
 				MyUI.showlDialog(view.getContext(), R.string.addCredit,
 						message, R.string.yes, R.string.no,
 						submitDialoguePositiveClickListener);
 			}
 		}
 	};
 
 	DialogInterface.OnClickListener submitDialoguePositiveClickListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int id) {
 			dialog.cancel();
 			// http post
 			Connector connector = new Connector(VendorAddCredit.this);
 			int status = connector.vendorAddCredit(
 					getString(R.string.addCreditUrl), VendorAddCredit.this,
 					accountModel.getAid(), accountModel.getCid(), newCr);
 			if (status == Connector.CONNECTION_SUCCESS) {
 				// update db
 				dbAdapter.open();
 				dbAdapter.sellToken(addedModelList, accountModel.getAid());
 				dbAdapter.close();
 				Intent intent = new Intent(VendorAddCredit.this,
 						VendorAddCreditReceipt.class);
 				intent.putExtra("info", info);
 				startActivity(intent);
 			} else {
 				int message;
 				if (status == Connector.CONNECTION_TIMEOUT) {
 					message = R.string.addCreditTimeout;
 				} else {
 					message = R.string.addCreditError;
 				}
 				MyUI.showNeutralDialog(VendorAddCredit.this,
 						R.string.addCredit, message, R.string.ok);
 			}
 
 		}
 	};
 }
