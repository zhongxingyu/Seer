 package com.example.gkaakash;
 
 
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.gkaakash.controller.Account;
 import com.gkaakash.controller.Organisation;
 import com.gkaakash.controller.Preferences;
 import com.gkaakash.controller.Report;
 import com.gkaakash.controller.Startup;
 import com.gkaakash.controller.Transaction;
 import com.gkaakash.controller.User;
 
 public class menu extends Activity{
 	//adding a class property to hold a reference to the controls
 	String  voucherTypeFlag;
 	private int group1Id = 1;
 	int Edit = Menu.FIRST;
 	int Delete = Menu.FIRST +1;
 	int Finish = Menu.FIRST +2;
 	AlertDialog dialog;
 	final Context context = this;
 	static String fromday, frommonth, fromyear, today, tomonth, toyear; 
 	private Integer client_id;
 	private Account account;
 	private Preferences preferences;
 	private Organisation organisation;
 	private Report report;
 	AlertDialog help_dialog;
 	static String financialFromDate;
 	static String financialToDate;
 	static String givenfromDateString;
 	static String givenToDateString;
 	DecimalFormat mFormat;
 	static boolean validateDateFlag;
 	static String selectedAccount;
 	static boolean cleared_tran_flag;
 	static boolean narration_flag;
 	static ArrayList<String> accdetailsList;
 	boolean reportmenuflag;
 	static String orgtype,userrole;
 	static String OrgName;
 	TextView tvWarning, tvusername;
 	module m;
 	static String[] menuOptions;
 	static String rollover;
 	private User user;
 	RadioButton rbmanager,rbRoleChecked,rboperator;
 	RadioButton rbGenderChecked,rbMale;
 	String gender,username,password,confpassword;
 	RadioGroup  radiogender ,radiorole ;
 	EditText eusername,epassword,econfpassword;
 	String login_time;
 	String logout_time;
 	EditText oldpass,newpass,confirmpass;
 	CharSequence[] items;
 	boolean reset_password_flag = false;
 	static String IPaddr;
 	ListView listView;
 	Date date = null;
 	
 	private Transaction transaction;
 	static boolean flag;
 	static final int FROM_DATE_DIALOG_ID = 0, TO_DATE_DIALOG_ID = 1;
 	Button from_btn_ID, to_btn_ID;
 	/*
     //adding options to the options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     menu.add(group1Id, Edit, Edit, "Edit");
     menu.add(group1Id, Delete, Delete, "Delete");
     menu.add(group1Id, FinishAlertDialog help_dialog;, Finish, "Finish");
     return super.onCreateOptionsMenu(menu); 
     }
 
     //code for the actions to be performed on clicking options menu goes here ...
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case 1:
         Toast msg = Toast.makeText(menu.this, "Menu 1", Toast.LENGTH_LONG);
         msg.show();
         return true;
         }
         return super.onOptionsItemSelected(item);
     }
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 * send the users login and logout timings to the backend
 	 * and pass on the activity to the main page
 	 */
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage("Do you want to logout?")
 		.setCancelable(false)
 
 		.setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 				Date date = new Date();
 				logout_time = dateFormat.format(date);
 				System.out.println("date"+login_time+"and"+logout_time+"  username"+username+"  userrole"+userrole);
 
 				Object[] params = new Object[]{username,userrole,login_time,logout_time};
 				user.setLoginLogoutTiming(params, client_id);
 
 				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent); 
 			}
 		}).setNegativeButton("NO", 
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				//do nothing
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();     
 
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.master_menu);
 
 		IPaddr = MainActivity.IPaddr;
 		System.out.println("in createorg"+IPaddr);
 		account = new Account(IPaddr);
 		preferences = new Preferences(IPaddr);
 		organisation = new Organisation(IPaddr);
 		report = new Report(IPaddr);
 		client_id= Startup.getClient_id();
 		m= new module();
 		reportmenuflag = MainActivity.reportmenuflag;
 
 		// create instance of user class to call setUser method
 		user = new User(IPaddr);
 		// get the client_id from startup
 
 		//get financial from and to date, split and store day, month and year in seperate variable
 		financialFromDate =Startup.getfinancialFromDate();
 		String dateParts[] = financialFromDate.split("-");
 		fromday = dateParts[0];
 		frommonth = dateParts[1];
 		fromyear = dateParts[2];
 
 		financialToDate = Startup.getFinancialToDate();
 		String dateParts1[] = financialToDate.split("-");
 		today = dateParts1[0];
 		tomonth = dateParts1[1];
 		toyear = dateParts1[2];
 		
 		//for two digit format date for dd and mm
 		mFormat= new DecimalFormat("00");
 		mFormat.setRoundingMode(RoundingMode.DOWN);
 
 		OrgName = MainActivity.organisationName;
 		
 		userrole = MainActivity.user_role;
 		username = MainActivity.username;
 		reset_password_flag = MainActivity.reset_password_flag;
 		
 		//reportmenuflag will tell you from which page u are came createOrg or selectOtg
 		if (reportmenuflag == true) {
 			orgtype=MainActivity.orgTypeFlag;
 		} else {
 			Object[] params = new Object[]{OrgName};
 			orgtype = (String) organisation.getorgTypeByname(params, client_id);
 		}
 		System.out.println("params are:"+OrgName+userrole+username+reset_password_flag+orgtype);
 		//get the last login timing of user from the database
 		
 		
 		
 		
 		//set title
 		TextView org = (TextView)findViewById(R.id.org_name);
 		org.setText(OrgName + ", "+orgtype);
 		TextView tvdate = (TextView)findViewById(R.id.date);
 		tvdate.setText(m.changeDateFormat(financialFromDate)+" To "+m.changeDateFormat(financialToDate));
 		
 		Button btn_optionsMenu= (Button) findViewById(R.id.btn_optionsMenu);
 		btn_optionsMenu.setVisibility(View.GONE);
 		
 		Button btn_changeInputs= (Button) findViewById(R.id.btn_changeInputs);
 		btn_changeInputs.setVisibility(View.GONE);
 		
 		//set user details
 		TextView tvuser = (TextView)findViewById(R.id.user);
 		tvuser.setText(Character.toString(userrole.charAt(0)).toUpperCase()+userrole.substring(1));
 		tvusername = (TextView)findViewById(R.id.username);
 		//set the login timing of user in the database
 		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 		Date today_date = new Date();
 		login_time = dateFormat.format(today_date);
 		
 		if(!userrole.equalsIgnoreCase("guest")){
 			Object[] params = new Object[]{userrole,username};
 			String result = user.getLastLoginTiming(params, client_id);
 			System.out.println("last login time"+result);
 			if(!result.equalsIgnoreCase("")){
 				//DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				try {
 					date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(result);
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				tvusername.setText(username + "\nLast login: "+ date.toLocaleString());
 			}else{
 				tvusername.setText(username);
 			}
 		}else{
 			tvusername.setText(username);
 		}
 		
 //		final SpannableString s = new SpannableString(context.getText(R.string.about_para));
 //		Linkify.addLinks(s, Linkify.WEB_URLS);
 		TextView tvabout = (TextView)findViewById(R.id.about);
 		tvabout.setText(context.getText(R.string.about_para));
 		
 		
 		reset_password(reset_password_flag);
 
 		user.getUserNemeOfOperatorRole(client_id);
 		/*
 		 * create separate lists for menu options, their respective heading and description
 		 * access to menus as per the role
 		 * Admin: All menus
 		 * Guest: All menus except rollover, user account settings
 		 * Manager: All menus Except delete org and rollover
 		 * Operator: All menus except rollover, delete org, bank recon, income-exp report, adding new user
 		 */
 		ArrayList<String> menuOptions = new ArrayList<String>(Arrays.asList("Create account", "Transaction", "Reports",
 				"Bank Reconciliation", "Preferences","Rollover","Export organisation","User account settings","Help"));
 		ArrayList<Integer> image_ids = new ArrayList<Integer>(Arrays.asList(R.drawable.account_logo, 
 				R.drawable.money_image, R.drawable.report_logo,
 				R.drawable.bank_recon_logo, R.drawable.settings_logo, 
 				R.drawable.rollover_logo, R.drawable.export_logo,
 				R.drawable.account_setting, R.drawable.help_logo));
 		ArrayList<String> description = new ArrayList<String>(Arrays.asList(
 				"Create/Search/Edit/Delete ledger accounts", 
 				"Make voucher entry for eg. Journal, Contra, Payment etc...", 
 				"View reports such as Ledger, Trial Balance etc...",
 				"Reconcile and compare", 
 				"Edit/Delete organisation, Add/Edit/Delete project",
 				"Transfer the holdings to the next financial year",
 				"Export organisation data from one device to another",
 				"Change Username/Password, Add/Edit/Delete user role",
 				"How to use ABT"));
 		
 		//modify menu list according to the user
 		if(userrole.equalsIgnoreCase("admin")){
 			//no changes in above 3 lists, display them as it is
 		}else if(userrole.equalsIgnoreCase("guest"))
 		{
 			//remove user account settings
 			menuOptions.remove(7);			
 			image_ids.remove(7);
 			description.remove(7);
 			
 		}else if(userrole.equalsIgnoreCase("manager")){
 			//change label preference
 			menuOptions.remove(4);
 			menuOptions.add(4, "Add Projects");
 			description.remove(4);
 			description.add(4, "Add/Edit/Delete project");
 			//remove rollover
 			menuOptions.remove(5);
 			image_ids.remove(5);
 			description.remove(5);
 		}else{//operator
 			menuOptions.remove(4);
 			menuOptions.add(4, "Add Projects");
 			menuOptions.remove(3); //bank recon
 			//menuOptions.remove(4); //preferences
 			menuOptions.remove(4); //rollover
 			//menuOptions.remove(2); //reports
 			image_ids.remove(3);
 			//image_ids.remove(4);
 			image_ids.remove(4);
 			//image_ids.remove(2);
 			description.remove(4);
 			description.add(4, "Add/Edit/Delete project");
 			description.remove(3);
 			//description.remove(4);
 			description.remove(4);
 			//description.remove(2);
 		}
 		System.out.println("before listview");
 		listView = (ListView) findViewById(R.id.listView1);
 		
 		String[] from = new String[] {"image", "label","sub_title"};
 		int[] to = new int[] {R.id.grid_item_image, R.id.grid_item_label, R.id.sub_title};
 
 		final List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
 	
 		for(int i = 0; i < menuOptions.size(); i++){
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("image", "" + image_ids.get(i));
 			map.put("label", "" + menuOptions.get(i));
 			map.put("sub_title", "" + description.get(i));
 			fillMaps.add(map);
 		} 
 		SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.menu_list, from, to);
 		listView.setAdapter(adapter);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 //				Toast.makeText(
 //						getApplicationContext(),
 //						((TextView) v.findViewById(R.id.grid_item_label))
 //						.getText(), Toast.LENGTH_SHORT).show();
 
 				if(position == 0)
 				{
 					MainActivity.tabFlag = true;
 					Intent intent = new Intent(context, account_tab.class);
 					// To pass on the value to the next page
 					startActivity(intent);
 				}
 
 				//for "transaction"
 				if(position == 1)
 				{
 					transaction = new Transaction(IPaddr);
 					flag = true;
 					Intent intent = new Intent(context, transaction_tab.class);
 					// To pass on the value to the next page
 					startActivity(intent);
 				}
 				if(userrole.equalsIgnoreCase("guest") || userrole.equalsIgnoreCase("admin") || userrole.equalsIgnoreCase("manager"))
 				{
 					//for "reports"
 					if(position == 2)
 					{
 						Intent intent = new Intent(context, reportMenu.class);
 						// To pass on the value to the next page
 						startActivity(intent);
 					}
 					//bank reconciliation
 					if(position == 3){
 						reportMenu reportM = new reportMenu();
 						reportM.callLedgerOrCashFlowOrBankRecon(context,"BR",bankReconciliation.class);
 					}
 					//for "adding project", adding popup menu ...
 					if(position == 4)
 					{
 						addPreferences();
 
 					}
 					if(userrole.equalsIgnoreCase("guest")){
 						// for rollOver
 						if (position == 5) {
 							rollover();
 						}
 						if(position == 6){
 							export();
 						}
 						//for help
 						if(position == 7){
 							Intent intent = new Intent(context, Help.class);
 							// To pass on the value to the next page
 							startActivity(intent);
 						}
 						
 					}
 					else if(userrole.equalsIgnoreCase("admin")){
 						
 						// for rollOver
 						if (position == 5) {
 							rollover();
 						}
 						exportToAbout(position,6);
 
 					}else{ //for manager only
 						exportToAbout(position,5);
 					}
 
 				}else{//for operator only
 					//for "reports"
 					if(position == 2)
 					{
 						Intent intent = new Intent(context, reportMenu.class);
 						// To pass on the value to the next page
 						startActivity(intent);
 					}
 					//for "adding project", adding popup menu ...
 					if(position == 3)
 					{
 						addPreferences();
 
 					}else{
 						exportToAbout(position,4);
 					}
 				}
 
 			}
 
 			private void exportToAbout(int position, int i) {
 				//export organisation
 				if(position == i){
 
 					export();
 				}
 				//for settings
 				if(position == i+1){
 					settings();
 				}
 				//for help
 				if(position == i+2){
 					Intent intent = new Intent(context, Help.class);
 					// To pass on the value to the next page
 					startActivity(intent);
 				}
 			}
 		});
 	}
 
 
 	private void reset_password(boolean reset_password_flag) {
 		if(reset_password_flag){
 			/* false for u don't want to close the dialog on clicking cancel button
 			 * if u want to close pass true as a param
 			 */
			reset_password_flag = false;
 			m.resetPassword(context,username,userrole,false,client_id);
 		}		
 	}
 
 	protected void rollover() {
         Object[] rollover_exist_params = new Object[] {OrgName,financialFromDate,financialToDate};
         Boolean existRollOver = report.existRollOver(rollover_exist_params);
 
         if (existRollOver.equals(false)) {
             Object[] rollover_params = new Object[] {OrgName, financialFromDate,financialToDate,
                     orgtype };
             rollover = report.rollOver(rollover_params,client_id);
             if(rollover.equalsIgnoreCase("false"))
             {
                 m.toastValidationMessage(context,"can not rollover , since financial year is not completed!!");
             }else
             {
                 m.toastValidationMessage(context,"rollover has been done!!");
             }       
 
         }else{
             m.toastValidationMessage(context,"rollover has done already!!");
         }
 
     }
 
 
 	protected void export() {
 		//		Intent email = new Intent(Intent.ACTION_SEND);
 		//        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"holyantony1492@gmail.com"});
 		//        //email.putExtra(Intent.EXTRA_CC, new String[]{ to});
 		//        //email.putExtra(Intent.EXTRA_BCC, new String[]{to});
 		//        email.putExtra(Intent.EXTRA_SUBJECT, "testing email");
 		//        email.putExtra(Intent.EXTRA_TEXT, "hello world");
 		//
 		//        //File root = Environment.getExternalStorageDirectory();
 		//        File file = new File("/opt/abt/export/bckp.xml");
 		//        if (!file.exists() || !file.canRead()) {
 		//            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
 		//            finish();
 		//            return;
 		//        }
 		//        Uri uri = Uri.parse("file://" + file);
 		//        email.putExtra(Intent.EXTRA_STREAM, uri);
 		//        
 		//        
 		//        //need this to prompts email client only
 		//        email.setType("message/rfc822");
 		//        startActivity(Intent.createChooser(email, "Choose an Email client :"));
 		AlertDialog.Builder builder = new AlertDialog.Builder(
 				context);
 		//builder.setTitle("Aakash Business Tool");
 		builder.setMessage("Do you want to export organisation?");
 		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				Object[] export = new Object[] {OrgName, financialFromDate,financialToDate};
 				//call back-end to export organisation 
 				String encrypted_db = organisation.Export(export,client_id);
 //				Toast.makeText(menu.this, encrypted_db, Toast.LENGTH_SHORT).show();
 				//copy export dir from /opt/abt/ to sdcard
 				String[] command = {"rm -r /mnt/sdcard/export", "busybox cp /data/local/abt/opt/abt/export/ /mnt/sdcard/ -R"};
 				module.RunAsRoot(command);
 				m.toastValidationMessage(context, "organisation "+OrgName+" has been exported to /mnt/sdcard/export/");
 			}
 
 		});
 		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 			}
 
 		});
 
 		AlertDialog about_dialog = builder.create();
 		about_dialog.show();
 
 	}
 
 	
 	protected void settings() {
 		if(userrole.equalsIgnoreCase("operator")){
 			items = new CharSequence[]{ "Change Username","Change Password"};
 		}else{
 			items = new CharSequence[]{ "Change Username","Change Password", "Add user"};
 		}
 
 		//creating a dialog box for popup
 		final AlertDialog.Builder builder = new AlertDialog.Builder(menu.this);
 		//setting title
 		builder.setTitle("User settings");
 		//adding items
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog,
 					int pos) {
 				if(pos == 0){
 					final View layout1 = m.builder_with_inflater(context, "",
 							R.layout.change_password);
 					LinearLayout l1 = (LinearLayout) layout1
 							.findViewById(R.id.changepassword);
 					l1.setVisibility(View.GONE);
 
 					final TextView error_msg = (TextView) layout1
 							.findViewById(R.id.tverror_msg1);
 					Button save = (Button) layout1.findViewById(R.id.btnSave);
 					TextView header = (TextView) layout1
 							.findViewById(R.id.tvheader1);
 					header.setText("Change username");
 					
 					final EditText olduser_name = (EditText) layout1
 							.findViewById(R.id.etOld_User_Name);
 					olduser_name.setTextColor(Color.parseColor("#AEC6CF"));
 					olduser_name.setText(username);
 					olduser_name.setEnabled(false);
 					
 					final EditText newuser_name = (EditText) layout1
 							.findViewById(R.id.etNewUsername);
 					newuser_name.requestFocus();
 					
 					Button cancel = (Button) layout1
 							.findViewById(R.id.btnCancel);
 					cancel.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							m.dialog.cancel();
 						}
 					});
 
 					save.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							String olduserName = olduser_name.getText()
 									.toString();
 							System.out.println("olduserName:" + olduserName);
 							
 							String newusername = newuser_name.getText()
 									.toString();
 							System.out.println("new username:" + newusername);
 
 							EditText password = (EditText) layout1
 									.findViewById(R.id.etPassword);
 							String password1 = password.getText().toString();
 							System.out.println("password:" + password1);
 							// System.out.println("username:"+username);
 							// System.out.println("userrole:"+userrole1);
 
 							if (!"".equals(olduserName)
 									& !"".equals(newusername)
 									& !"".equals(password1)) {
 								Boolean username_result = user.changeUserName( 
 										new Object[] { olduserName,
 												newusername, password1,
 												userrole }, client_id);
 								System.out.println("r:" + username_result);
 								if (username_result == true) {
 									m.dialog.cancel();
 									Toast.makeText(context, "Username updated successully", Toast.LENGTH_SHORT).show();
 									if (date != null) {
 										//add last login date
 										tvusername.setText(newusername + "\nLast login: "+ date.toLocaleString());
 									}else{
 										tvusername.setText(newusername);
 									}
 									username = newusername;
 
 								} else {
 									error_msg.setVisibility(TextView.VISIBLE);
 									error_msg
 									.setText("Password is invalid");
 									password.setText("");
 								}
 							} else {
 								error_msg.setVisibility(TextView.VISIBLE);
 								error_msg.setText("Fill the empty fields");
 
 							}
 						}
 					});
 
 				}
 				if(pos == 1){
 					final View layout = m.builder_with_inflater(context, "",
 							R.layout.change_password);
 
 					LinearLayout l1=(LinearLayout) layout.findViewById(R.id.changeusername);
 					l1.setVisibility(View.GONE);
 					Button cancel = (Button) layout
 							.findViewById(R.id.btnCancel);
 					TextView header = (TextView) layout.findViewById(R.id.tvheader1);
 					header.setText("Change password");
 
 					final TextView error_msg = (TextView) layout.findViewById(R.id.tverror_msg1);
 					cancel.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							m.dialog.cancel();
 
 						}
 					});
 
 					Button save = (Button) layout
 							.findViewById(R.id.btnSave);
 
 
 					save.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							oldpass = (EditText) layout
 									.findViewById(R.id.etOldPass);
 							String old_pass=oldpass.getText().toString(); 
 							System.out.println("oldpass:"+old_pass);
 							newpass = (EditText) layout
 									.findViewById(R.id.etNewPass);
 							String new_pass=newpass.getText().toString();
 							System.out.println("newpass:"+new_pass);
 
 							confirmpass = (EditText) layout
 									.findViewById(R.id.etconfirmPass);
 							String confirm_pass=confirmpass.getText().toString();
 							System.out.println("confirm_pass:"+confirm_pass);
 
 							String username;
 
 //							Toast.makeText(context, "lll:"+MainActivity.username_flag, Toast.LENGTH_SHORT).show();
 							
 							username=MainActivity.username;
 							System.out.println("username1:"+username);
 
 
 							if(!"".equals(old_pass)&!"".equals(new_pass)&!"".equals(confirm_pass)){
 								if(new_pass.equals(confirm_pass)){
 
 									Boolean result= user.changePassword(new Object[]{username,old_pass,new_pass,userrole}, client_id);
 									System.out.println("r:"+result);
 									if(result==false){
 										error_msg.setVisibility(TextView.VISIBLE);
 
 										error_msg.setText("Invalid password");
 										oldpass.setText("");
 										newpass.setText("");
 										confirmpass.setText(""); 
 									}else {
 										m.dialog.cancel();
 										Toast.makeText(context, "Password updated successully", Toast.LENGTH_SHORT).show();
 									}
 								}else {
 									error_msg.setVisibility(TextView.VISIBLE);
 
 									error_msg.setText( "New password and confirm password fields doesnot match!");
 									newpass.setText("");
 									confirmpass.setText(""); 
 								}
 
 							}else {
 								error_msg.setVisibility(TextView.VISIBLE);
 								error_msg.setText("Fill the empty fields");	
 							}
 						}
 					});
 
 
 				}
 				if(pos==2){
 					Intent intent = new Intent(context, User_table.class);
 					startActivity(intent);
 
 				}
 			}				        	
 		});
 		dialog=builder.create();
 		((Dialog) dialog).show();
 	}
 
 	protected void addPreferences() {
 		if(userrole.equalsIgnoreCase("Admin") || userrole.equalsIgnoreCase("Guest")){
 			items = new CharSequence[]{ "Edit/Delete organisation", "Add/Edit/Delete project" };
 			//creating a dialog box for popup
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			//setting title
 			builder.setTitle("Select preference");
 			//adding items
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog1, int pos) {
 					//code for the actions to be performed on clicking popup item goes here ...
 					switch (pos) {
 					case 0:
 					{
 
 						MainActivity.editDetails=true;
 						Object[] editDetails = (Object[])organisation.getOrganisation(client_id);
 						accdetailsList = new ArrayList<String>();
 						for(Object row2 : editDetails){
 							Object[] a2=(Object[])row2;
 							ArrayList<String> accdetails = new ArrayList<String>();
 							for(int i=0;i<a2.length;i++){
 								accdetails.add((String) a2[i].toString());
 							}
 							accdetailsList.addAll(accdetails);
 						}
 
 						//System.out.println("details:"+accdetailsList);
 
 						Intent intent = new Intent(context, orgDetails.class);
 						// To pass on the value to the next page
 						startActivity(intent);
 					}break;
 					case 1:
 					{
 						Intent intent = new Intent(context, addProject.class);
 						// To pass on the value to the next page
 						startActivity(intent);
 
 					}break;
 					}
 				}
 			});
 			//building a complete dialog
 			dialog=builder.create();
 			dialog.show();
 
 		}else{
 			Intent intent = new Intent(context, addProject.class);
 			// To pass on the value to the next page
 			startActivity(intent);
 		}
 		
 		
 	}
 	
 	
 	private void reset() {
 		eusername.setText("");
 		epassword.setText("");
 		econfpassword.setText("");
 		rbMale.setChecked(true);
 	}
 }
