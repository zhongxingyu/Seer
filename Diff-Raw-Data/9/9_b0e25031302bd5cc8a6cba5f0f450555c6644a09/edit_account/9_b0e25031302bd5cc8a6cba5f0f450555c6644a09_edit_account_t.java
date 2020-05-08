 package com.example.gkaakash;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.R.color;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.SpannableString;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.gkaakash.controller.Account;
 import com.gkaakash.controller.Preferences;
 import com.gkaakash.controller.Report;
 import com.gkaakash.controller.Startup;
 
 public class edit_account extends Activity {
 	static Object[] accCodeCheckFlag_Obj;
 	String accCodeCheckFlag;
 	private ListView List;
 	private EditText etSearch;
 	Spinner sSearchAccountBy;
 	private ArrayList<String> array_sort = new ArrayList<String>();
 	private ArrayList<String> getList = new ArrayList<String>();
 	private ArrayList<String> accCode_list = new ArrayList<String>();
 	int textlength = 0;
 	static Integer client_id;
 	private Account account;
 	private Object[] accountnames;
 	private Object[] accountcodes;
 	// List getList,array_sort;
 	// List accCode_list;
 	AlertDialog dialog;
 	static Object[] accountDetail;
 	ArrayList accountDetailList;
 	static int flag = 1;
 	module m;
 	private Report reports;
 	static String IPaddr;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_acc_tab);
 		IPaddr = MainActivity.IPaddr;
 	    System.out.println("in createorg"+IPaddr);
 		account = new Account(IPaddr);
 		client_id = Startup.getClient_id();
 		m = new module();
 		reports = new Report(IPaddr);
 		List = (ListView) findViewById(R.id.ltAccname);
 		List.setCacheColorHint(color.transparent);
 		List.setTextFilterEnabled(true);
 
 		etSearch = (EditText) findViewById(R.id.etSearch);
 		sSearchAccountBy = (Spinner) findViewById(R.id.sSearchAccountBy);
 
 		Preferences preferencObj = new Preferences(IPaddr);
 		// call getPrefernece to get set preference related to account code flag
 		accCodeCheckFlag_Obj = preferencObj.getPreferences(new Object[] { "1" },client_id);
 		accCodeCheckFlag=(String)accCodeCheckFlag_Obj[0];
 		
 		// set visibility of spinner
 		if (accCodeCheckFlag.equalsIgnoreCase("automatic")) {
 			sSearchAccountBy.setVisibility(Spinner.GONE);
 		} else {
 			sSearchAccountBy.setVisibility(Spinner.VISIBLE);
 		}
 
 		// when spinner(search by account name or code) item selected, set the
 		// hint in search edittext
 		setOnItemSelectedListener();
 
 		//	get all acoount names in list view on load
 		accountnames = (Object[]) account.getAllAccountNames(client_id);
 		getResultList(accountnames);
 
 		// search account
 		searchAccount();
 		// edit or delete account
 		editAccount();
 
 	}
 
 	private void setOnItemSelectedListener() {
 		sSearchAccountBy.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v,
 					int position, long id) {
 				if (position == 0) {
 					etSearch.setHint("Search by name");
 					flag = 1;
 					// get all acoount names in list view
 					accountnames = (Object[]) account
 							.getAllAccountNames(client_id);
 					getResultList(accountnames);
 				}
 				if (position == 1) {
 					etSearch.setHint("Search by code");
 					flag = 2;
 					// get all acoount codes in list view
 					accountcodes = (Object[]) account
 							.getAllAccountCodes(client_id);
 					getResultList(accountcodes);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// do nothing!!
 
 			}
 		});
 
 	}
 
 	private void editAccount() {
 		List = (ListView) findViewById(R.id.ltAccname);
 		List.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view,
 					final int position, long id) {
 
 				final CharSequence[] items = { "Edit account", "Delete account" };
 				// creating a dialog box for popup
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						edit_account.this);
 				// setting title
 				builder.setTitle("Edit/Delete Account");
 				// adding items
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface which, final int pos) {
 						if (accCodeCheckFlag.equals("automatic")) {
 							flag = 1;
 						}
 						final TextView tvaccname = (TextView) view.findViewById(R.id.tvRowTitle1);
 						Object[] params1 = new Object[] {
 								tvaccname.getText().toString(),
 								flag, pos };
 						System.out.println(tvaccname.getText().toString());
 						final String accountDeleteValue = (String) account
 								.deleteAccountNameMaster(params1, client_id);
 						System.out.println("value returned:"+accountDeleteValue);
 						String message = "";
 						System.out.println("value" + accountDeleteValue);
 						if ("account deleted"
 								.equals(accountDeleteValue)) {
 							message = "edit";
 						}else if ("account can be edited"
 								.equals(accountDeleteValue)) {
 							message = "edit";
 						} else if ("has both opening balance and trasaction"
 								.equals(accountDeleteValue)) {
 							message = "' has both opening balance and transaction, it can't be";
 						} else if ("has opening balance"
 								.equals(accountDeleteValue)) {
 							message = "' has opening balance, it can't be";
 						} else if ("has transaction".equals(accountDeleteValue)) {
 							message = "' has transaction, it can't be";
 						}
 
 						final String msg = message;
 
 						// code for the actions to be performed on clicking
 						// popup item goes here ...
 						switch (pos) {
 						case 0: {
 
 							// Toast.makeText(edit_account.this,"Clicked on:"+items[pos],Toast.LENGTH_SHORT).show();
 							LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 							View layout = inflater.inflate(
 									R.layout.edit_account,
 									(ViewGroup) findViewById(R.id.layout_root));
 							AlertDialog.Builder builder = new AlertDialog.Builder(
 									edit_account.this);
 							builder.setView(layout);
 							builder.setTitle("Edit account");
 
 							// get account details
 							String queryParam = tvaccname.getText().toString();
 							if (accCodeCheckFlag.equals("automatic")) {
 								// search by account name
 								Object[] params = new Object[] { 2, queryParam };
 								accountDetail = (Object[]) account.getAccount(
 										params, client_id);
 							} else if (sSearchAccountBy.getVisibility() == View.VISIBLE) {
 								// Its visible
 								if (sSearchAccountBy.getSelectedItemPosition() == 0) {
 									// search by account name
 									Object[] params = new Object[] { 2,
 											queryParam };
 									accountDetail = (Object[]) account
 											.getAccount(params, client_id);
 
 								} else if (sSearchAccountBy
 										.getSelectedItemPosition() == 1) {
 									// search by account code
 									Object[] params = new Object[] { 1,
 											queryParam };
 									accountDetail = (Object[]) account
 											.getAccount(params, client_id);
 
 								}
 							}
 
 							accountDetailList = new ArrayList();
 							for (Object ad : accountDetail) {
 								Object a = (Object) ad;
 								accountDetailList.add(a.toString());
 
 							}
 
 							// account name
 							final Button bEditAccountName = (Button) layout
 									.findViewById(R.id.bEditAccountName);
 							final TextView tvEditAccountName = (TextView) layout
 									.findViewById(R.id.tvEditAccountName);
 							final String oldAccountName = accountDetailList
 									.get(3).toString();
 							tvEditAccountName.setText(oldAccountName);
 							final EditText etEditAccountName = (EditText) layout
 									.findViewById(R.id.etEditAccountName);
 							etEditAccountName.setVisibility(EditText.GONE);
 							tvEditAccountName
 							.setOnClickListener(new View.OnClickListener() {
 
 								@Override
 								public void onClick(View v) {
 									tvEditAccountName
 									.setVisibility(TextView.GONE);
 									etEditAccountName
 									.setVisibility(EditText.VISIBLE);
 									etEditAccountName
 									.setText(oldAccountName);
 									bEditAccountName
 									.setVisibility(Button.GONE);
 								}
 							});
 
 							// opening balance
 							final Button bEditOpBal = (Button) layout
 									.findViewById(R.id.bEditOpBal);
 							final TextView tvEditOpBal = (TextView) layout
 									.findViewById(R.id.tvEditOpBal);
 							final String oldOpBal = String.format(
 									"%.2f",
 									Float.valueOf(
 											accountDetailList.get(4).toString()
 											.trim()).floatValue());
 							tvEditOpBal.setText(oldOpBal);
 							final EditText etEditOpBal = (EditText) layout
 									.findViewById(R.id.etEditOpBal);
 							etEditOpBal.setVisibility(EditText.GONE);
 
 							if ("Direct Income".equals(accountDetailList.get(1)
 									.toString())
 									|| "Direct Expense"
 									.equals(accountDetailList.get(1)
 											.toString())
 											|| "Indirect Income"
 											.equals(accountDetailList.get(1)
 													.toString())
 													|| "Indirect Expense"
 													.equals(accountDetailList.get(1)
 															.toString())) {
 								// opening balance is always 0 for above 4
 								// groups, hence set clickable=false
 								etEditOpBal.setClickable(false);
 								bEditOpBal.setVisibility(Button.GONE);
 							} else {
 								// set visibility of edittext for editing
 								// opening balance
 								tvEditOpBal
 								.setOnClickListener(new View.OnClickListener() {
 									@Override
 									public void onClick(View v) {
 										tvEditOpBal
 										.setVisibility(TextView.GONE);
 										etEditOpBal
 										.setVisibility(EditText.VISIBLE);
 										etEditOpBal.setText(oldOpBal);
 										bEditOpBal
 										.setVisibility(Button.GONE);
 									}
 								});
 							}
 
 							String dialog_button = "Save";
 							/*
 							 * if account has opening balance or transaction or
 							 * both it can not be deleted so make textview
 							 * non-editable and edit button invisible and set
 							 * warning at the top
 							 */
 							if (!"edit".equals(msg)) {
 								tvEditAccountName.setClickable(false);
 								tvEditOpBal.setClickable(false);
 								bEditAccountName.setVisibility(Button.GONE);
 								bEditOpBal.setVisibility(Button.GONE);
 								dialog_button = "Ok";
 								TextView tvwarning = (TextView) layout
 										.findViewById(R.id.tvwarning);
 								tvwarning.setVisibility(TextView.VISIBLE);
 								tvwarning.setText("Account '" + oldAccountName
 										+ msg + " edited.");
 							}
 
 							// set account code
 							final TextView tvEditAccountCode = (TextView) layout
 									.findViewById(R.id.tvEditAccountCode);
 							tvEditAccountCode.setText(accountDetailList.get(0)
 									.toString());
 
 							// set group name
 							final TextView tvEditGroupName = (TextView) layout
 									.findViewById(R.id.tvEditGroupName);
 							tvEditGroupName.setText(accountDetailList.get(1)
 									.toString());
 
 							// set subgroup name
 							final TextView tvEditSubgroupName = (TextView) layout
 									.findViewById(R.id.tvEditSubgroupName);
 							tvEditSubgroupName.setText(accountDetailList.get(2)
 									.toString());
 
 							builder.setPositiveButton(dialog_button,
 									new OnClickListener() {
 
 								public void onClick(
 										DialogInterface dialog,
 										int which) {
 
 									// get all values
 									String newAccountName;
 									if (etEditAccountName
 											.getVisibility() == View.VISIBLE) {
 										newAccountName = etEditAccountName
 												.getText().toString()
 												.trim();
 									} else {
 										newAccountName = tvEditAccountName
 												.getText().toString();
 									}
 
 									String newOpBal;
 									if (etEditOpBal.getVisibility() == View.VISIBLE) {
 										newOpBal = etEditOpBal
 												.getText().toString();
 										if (newOpBal.length() < 1) {
 											newOpBal = "";
 										} else {
 											newOpBal = String
 													.format("%.2f",
 															Float.valueOf(
 																	newOpBal.trim())
 																	.floatValue());
 										}
 									} else {
 										newOpBal = tvEditOpBal
 												.getText().toString();
 									}
 									String groupname = tvEditGroupName
 											.getText().toString();
 									String subgroupname = tvEditSubgroupName
 											.getText().toString();
 									String accountcode = tvEditAccountCode
 											.getText().toString();
 
 									if ((newAccountName.length() < 1)
 											&& ("".equals(newOpBal))) {
 										String message = "Please fill field";
 										m.toastValidationMessage(
 												edit_account.this,
 												message);
 
 									} else if ("".equals(newOpBal)) {
 										String message = "Please fill amount field";
 										m.toastValidationMessage(
 												edit_account.this,
 												message);
 									} else if ((newAccountName.length() < 1)) {
 										String message = "Please fill accountname field";
 										m.toastValidationMessage(
 												edit_account.this,
 												message);
 									}
 									if ((newAccountName.length() >= 1)
 											&& (!"".equals(newOpBal))) {
 										String accountcode_exist = account
 												.checkAccountName(
 														new Object[] {
 																newAccountName,
 																"", "" },
 																client_id);
 										if (!newAccountName
 												.equalsIgnoreCase(oldAccountName)
 												&& accountcode_exist
 												.equals("exist")) {
 											String message = "Account '"
 													+ newAccountName
 													+ "' already exist";
 											m.toastValidationMessage(
 													edit_account.this,
 													message);
 
 										} else {
 
 											Object[] params;
 											if ("Direct Income"
 													.equals(accountDetailList
 															.get(1)
 															.toString())
 															|| "Direct Expense"
 															.equals(accountDetailList
 																	.get(1)
 																	.toString())
 																	|| "Indirect Income"
 																	.equals(accountDetailList
 																			.get(1)
 																			.toString())
 																			|| "Indirect Expense"
 																			.equals(accountDetailList
 																					.get(1)
 																					.toString())) {
 												params = new Object[] {
 														newAccountName,
 														accountcode,
 														groupname };
 
 											} else {
 												params = new Object[] {
 														newAccountName,
 														accountcode,
 														groupname,
 														newOpBal };
 											}
 											account.editAccount(params,
 													client_id);
 
 											// set alert messages after
 											// account edit
 											if (!newAccountName
 													.equalsIgnoreCase(oldAccountName)
 													&& !newOpBal
 													.equals(oldOpBal)) {
 
 												String message = "Account name has been changed from '"
 														+ oldAccountName
 														+ "' to '"
 														+ newAccountName
 														+ "' and opening balance has been changed from '"
 														+ oldOpBal
 														+ "' to '"
 														+ newOpBal
 														+ "'";
 												m.toastValidationMessage(
 														edit_account.this,
 														message);
 											} else if (!newAccountName
 													.equalsIgnoreCase(oldAccountName)) {
 												String message = "Account name has been changed from '"
 														+ oldAccountName
 														+ "' to '"
 														+ newAccountName
 														+ "'";
 												m.toastValidationMessage(
 														edit_account.this,
 														message);
 											} else if (!newOpBal
 													.equals(oldOpBal)) {
 												String message = "Opening balance has been changed from '"
 														+ oldOpBal
 														+ "' to '"
 														+ newOpBal
 														+ "'";
 												m.toastValidationMessage(
 														edit_account.this,
 														message);
 											} else {
 												if ("edit".equals(msg)) {
 													String message = "No changes made";
 													m.toastValidationMessage(
 															edit_account.this,
 															message);
 												}
 											}
 
 											setaccountlist();
 
 										}
 
 									}
 
 								}// end of onclick
 							});// end of onclickListener
 
 							builder.setNegativeButton("Cancel",
 									new OnClickListener() {
 
 								@Override
 								public void onClick(
 										DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 
 								}
 							});
 
 							dialog = builder.create();
 							((Dialog) dialog).show();
 							WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 							// customizing the width and location of the dialog
 							// on screen
 							lp.copyFrom(dialog.getWindow().getAttributes());
 							// lp.height = 600;
 							lp.width = 400;
 							dialog.getWindow().setAttributes(lp);
 						}
 						break;
 						case 1: { 
 							final AlertDialog.Builder builder = new AlertDialog.Builder(edit_account.this);
 							builder.setMessage("Are you sure you want to delete account '"+tvaccname.getText().toString()+ "'?")
 							.setPositiveButton("Yes", new OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									
 									Object[] params1 = new Object[] {
 											tvaccname.getText().toString(),
 											flag, pos };
 									
 									
									if ("account deleted".equals(accountDeleteValue) || "' has opening balance, it can't be".equalsIgnoreCase(msg)) {
 										System.out.println(tvaccname.getText().toString());
 										account.deleteAccount(params1, client_id);
 										m.toastValidationMessage(edit_account.this, "Account '"+ tvaccname.getText().toString()
 												+"' has been deleted successfully.");
 										setaccountlist();
 										System.out.println("if"+accountDeleteValue);
 									} else {
 										System.out.println("else"+accountDeleteValue);
 
 										m.toastValidationMessage(
 												edit_account.this,
 												"Account '"
 														+ tvaccname.getText().toString()
 																+ msg + " deleted.");
 									}
 								}
 							})
 							.setNegativeButton("No", new OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// do nothing
 									System.out.println("in delete");
 									
 								}
 							});
 							AlertDialog alert = builder.create();
 							alert.show();
 							
 						}
 						break;
 						}
 					}
 				});
 				// building a complete dialog
 				dialog = builder.create();
 				dialog.show();
 
 			}
 		});
 
 	}
 
 	// search account
 	private void searchAccount() {
 		// attaching listener to textView
 		etSearch.addTextChangedListener(new TextWatcher() {
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// Abstract Method of TextWatcher Interface.
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				// for loop for search
 				textlength = etSearch.getText().length();
 				array_sort.clear();
 				System.out.println("glist:"+getList+etSearch.getText().toString());
 
 				if(textlength > 0){
 					System.out.println("lengh is"+textlength+"get list size"+getList.size());
 					for (int i = 0; i < getList.size(); i++) {
 						System.out.println("we are in for"+etSearch.getText().toString());
 						try {
 							if(etSearch.getText().toString().equalsIgnoreCase(getList.get(i).substring(0, textlength))){
 								System.out.println("same");
 								array_sort.add(getList.get(i));
 							}else{
 								System.out.println("not same");
 							}
 						} catch (Exception e) {
 							System.out.println("not same");
 						}
 					}
 				}
 
 				if(textlength == 0){
 					filllist(getList);
 				}else{
 					System.out.println("array:"+array_sort);
 					filllist(array_sort);
 				}
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// Abstract Method of ArrayAdapter Interface
 			}
 		});
 
 	}// end of search account by name
 
 	// get all acoount names or account codes depending upon parameter
 	void getResultList(Object[] param) {
 		getList.clear();
 		for (Object an : param) {
 			getList.add((String) an); // acc_names
 			System.out.println("Rsize:"+param.length);
 
 		}
 
 		filllist(getList);
 		System.out.println("Rsize:"+getList.size());
 		// List.setAdapter(new ArrayAdapter<String>(this,
 		// android.R.layout.simple_list_item_1, getList));  
 
 	}
 
 	void setaccountlist() {
 		getList.clear();
 		if (accCodeCheckFlag.equals("automatic")) {
 			// get all updated acoount names in list view
 			accountnames = (Object[]) account.getAllAccountNames(client_id);
 			getResultList(accountnames);
 		} else if (sSearchAccountBy.getVisibility() == View.VISIBLE) {
 			// Its visible
 			if (sSearchAccountBy.getSelectedItemPosition() == 0) {
 				// for search by account name, get all updated acoount names in
 				// list view
 				accountnames = (Object[]) account.getAllAccountNames(client_id);
 				getResultList(accountnames);
 
 			} else if (sSearchAccountBy.getSelectedItemPosition() == 1) {
 				// search by account code
 				accountnames = (Object[]) account.getAllAccountCodes(client_id);
 				getResultList(accountnames);
 
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume() to execute code when tab is changed
 	 * because when the tab is clicked onResume is called for that activity
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume(); 
 		System.out.println("ON RESUME");
 		// get all acoount names in list view on load
 		accountnames = (Object[]) account.getAllAccountNames(client_id);
 		getResultList(accountnames);
 		setaccountlist();
 
 	}
 	
 	private void filllist(ArrayList<String> list) {
 
 		String[] abc = new String[] { "rowid", "col_1" };
 		int[] pqr = new int[] { R.id.tvRowTitle1, R.id.tvSubItem1 };
 		
 		System.out.println("size:" + list.toString().length()+ "" + list);
 		System.out.println("size:" + list);
 
 		List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
 		for (int i = 0; i < list.size(); i++) {
 
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("rowid", "" + list.get(i));
 			String Acc_name = list.get(i);
 			String financialFromDate = Startup.getfinancialFromDate();
 			String financialToDate = Startup.getFinancialToDate();
 			if (sSearchAccountBy.getSelectedItemPosition() == 1) {
 				// if search by account code, get account name by code
 				Object[] params = new Object[] {Acc_name};
 				Object Acc_name1 = (Object)Account.getAccountNameByAccountCode(params, client_id);
 				Acc_name = Acc_name1.toString();
 			}
 			//calculate closing balance and set the list
 			Object[] params1 = new Object[] { Acc_name.toString(), financialFromDate,
 					financialFromDate, financialToDate };
 			Object[] calculateBalance = (Object[]) reports.calculateBalance(params1, client_id);
 
 			SpannableString rsSymbol = new SpannableString(edit_account.this.getText(R.string.Rs)); 
 			if (!calculateBalance[2].toString().equals("0.00")) {
 				map.put("col_1", rsSymbol +" "+calculateBalance[2].toString()+ " ("+calculateBalance[6].toString()+")");
 			}else{
 				map.put("col_1", rsSymbol +" "+calculateBalance[2].toString());
 			}
 			fillMaps.add(map);
 		} 
 
 		SimpleAdapter adap = new SimpleAdapter(this, fillMaps,
 				R.layout.child_row1, abc, pqr);
 
 		List.setAdapter(adap);
 	}
 
 
 
 
 
 
 
 }
