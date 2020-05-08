 /*
  * The MIT License
  *
  * Copyright (c) 2010 Petar Petrov
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.myglob;
 
 import net.vexelon.mobileops.InvalidCredentialsException;
 import net.vexelon.mobileops.SecureCodeRequiredException;
 import net.vexelon.myglob.actions.AccountStatusAction;
 import net.vexelon.myglob.actions.Action;
 import net.vexelon.myglob.configuration.AccountPreferencesActivity;
 import net.vexelon.myglob.configuration.Defs;
 import net.vexelon.myglob.configuration.GlobalSettings;
 import net.vexelon.myglob.configuration.LegacySettings;
 import net.vexelon.myglob.users.AccountType;
 import net.vexelon.myglob.users.User;
 import net.vexelon.myglob.users.UsersManager;
 import net.vexelon.myglob.utils.Utils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff.Mode;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author p.petrov
  * <p>
  * <pre>
  * Milestone 02
  * 1. [DONE] Add spinner for user accounts
  * 2. [DONE] Add user account class and move methods for handling account info
  * 3. [DONE] Refactor LoginActivity - NewAccountActivity
  * 4. [DONE] Add AccountManagement menu button for general account management (Spinner of accounts + sepearet Activity for options)
  * 5. [DONE] Refactor MainActivity class"" with proper actions for updating account info
  * 6. [DONE] Make previous account preferences work
  * 7. Solve problem with security codes on GLB site
  *
  * Milestone 01
  * 1. [DONE] Complete Spinner actions
  * 2. [DONE] Test secure code image occurrence
  * 3. [DONE] Test saving/loading of options
  * 4. [DONE] Add/Finish About activity
  * 5. [DONE] Add Progress dialog(s)
  * 6. [DONE] Add strings to resources
  * 7. [DONE] Test error message screens
  * 8. [DONE] Add images and fix/adjust layout
  * 9. [DONE] What to do if key-create fails ??
  * 10. Protect IV
  * 11. [DONE] Remove log tags
  * </pre>
  * 
  */
 public class MainActivity extends Activity {
 
 	private Activity _activity = null;
 	//private ArrayAdapter<String> _adapterAccounts = null;
 	private AccountsArrayAdapter _adapterAccounts = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         _activity = this;
 
         /**
          * load preferences
          */
         final SharedPreferences prefsUsers = this.getSharedPreferences(Defs.PREFS_USER_PREFS, 0);
         UsersManager.getInstance().reloadUsers(prefsUsers);
 
         SharedPreferences prefsGeneral = this.getSharedPreferences(Defs.PREFS_ALL_PREFS, 0);
         GlobalSettings.getInstance().init(prefsGeneral);
 
         /**
          * initialize UI
          */
 
         this.setTitle(getResString(R.string.app_name) + " - " + getResString(R.string.about_tagline));
 
         // init options spinner
         Spinner spinnerOptions = (Spinner) findViewById(R.id.SpinnerOptions);
         OperationsArrayAdapter adapter = new OperationsArrayAdapter(this, android.R.layout.simple_spinner_item,
         		new Operations[]{
         		Operations.CHECK_CURRENT_BALANCE,
 				Operations.CHECK_AVAIL_MINUTES,
 				Operations.CHECK_AVAIL_DATA,
 				Operations.CHECK_SMS_PACKAGE,
 				Operations.CHECK_CREDIT_LIMIT,
 				Operations.CHECK_ALL,
         });
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinnerOptions.setAdapter(adapter);
         
         // pre-select Operation
         int pos = adapter.getItemPosition(GlobalSettings.getInstance().getLastSelectedOperation());
         if (pos != -1) {
         	spinnerOptions.setSelection(pos);
         }
 
         // populate available accounts
         updateAccountsSpinner();
 
         // create update button
         Button btnUpdate = (Button) findViewById(R.id.ButtonUpdate);
         btnUpdate.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				updateSelectedStatus();
 			}
 		});
 
         //btnUpdate.getBackground().setColorFilter(0x2212FF00, Mode.LIGHTEN);
         btnUpdate.getBackground().setColorFilter(Defs.CLR_BUTTON_UPDATE, Mode.MULTIPLY);
         
         // load last saved operation info (if available)
         if (GlobalSettings.getInstance().getLastCheckedInfo() != GlobalSettings.NO_INFO) {
         	TextView textContent = (TextView) findViewById(R.id.TextContent);
         	textContent.setText(Html.fromHtml(GlobalSettings.getInstance().getLastCheckedInfo()));
         }
 
         /**
          * try to find legacy users and add them to UsersManager
          */
         final LegacySettings legacySettings = new LegacySettings();
         legacySettings.init(prefsGeneral);
         if (legacySettings.getPhoneNumber() != null) {
 
 			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(_activity);
 			alertBuilder.setTitle(R.string.dlg_legacyuser_title)
 				.setMessage(String.format(getResString(R.string.dlg_legacyuser_msg), legacySettings.getPhoneNumber()))
 				.setIcon(R.drawable.alert)
 				.setPositiveButton(getResString(R.string.dlg_msg_yes), new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// Create & Add user
 			        	User user = new User().setAccountName(legacySettings.getPhoneNumber())
 							.setPhoneNumber(legacySettings.getPhoneNumber())
 							.setAccountType(AccountType.Globul); //V1.1.0 has only Globul support
 
 			        	try {
 				        	if (UsersManager.getInstance().isUserExists(legacySettings.getPhoneNumber()))
 				        		throw new Exception(getResString(R.string.err_msg_user_already_exists));
 
 				        	UsersManager.getInstance().addUser(user);
 			        		UsersManager.getInstance().setUserPassword(user, legacySettings.getPassword());
 			        		UsersManager.getInstance().save(prefsUsers);
 			        		updateAccountsSpinner();
 			        	}
 			        	catch(Exception e) {
 			        		Utils.showAlertDialog(_activity, String.format(getResString(R.string.dlg_error_msg_legacy_user_failed), e.getMessage()), getResString(R.string.dlg_error_msg_title));
 			        	}
 			        	legacySettings.clear();
 						dialog.dismiss();
 					}
 				})
 				.setNegativeButton(getResString(R.string.dlg_msg_no), new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						legacySettings.clear();
 						dialog.dismiss();
 					}
 				}).show();
         }
     }
     
     @Override
     protected void onStart() {
     	
     	// trap Operations selection
     	Spinner spinnerOptions = (Spinner) findViewById(R.id.SpinnerOptions);
         spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         	@Override
         	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
         		Operations operation = (Operations) parentView.getSelectedItem();
         		GlobalSettings.getInstance().putLastSelectedOperation(operation);
         	}
         	
         	@Override
         	public void onNothingSelected(AdapterView<?> parentView) {
         	}
 		});   
         
         // trap phone number selection
         Spinner spinnerAccounts = (Spinner) findViewById(R.id.SpinnerUserAccounts);
         spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         	@Override
         	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
         		String phoneNumber = (String) parentView.getSelectedItem();
         		GlobalSettings.getInstance().putLastSelectedAccount(phoneNumber);
         	}
         	
         	@Override
         	public void onNothingSelected(AdapterView<?> parentView) {
         	}
 		});           
     	
     	super.onStart();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch(requestCode) {
     	case Defs.INTENT_ACCOUNT_ADD_RQ:
     		if (resultCode == RESULT_OK) {
     			Toast.makeText(getApplicationContext(), R.string.text_account_created, Toast.LENGTH_SHORT).show();
     		}
     		break;
 
     	case Defs.INTENT_ACCOUNT_EDIT_RQ:
     		if (resultCode == RESULT_OK) {
     			Toast.makeText(getApplicationContext(), R.string.text_account_saved, Toast.LENGTH_SHORT).show();
     		}
     		else if (resultCode == Defs.INTENT_RESULT_ACCOUT_DELETED) {
     			Toast.makeText(getApplicationContext(), R.string.text_account_removed, Toast.LENGTH_SHORT).show();
     		}
     		break;
     	}
 
     	updateAccountsSpinner();
     }
 
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		return initMenu(menu);
 //	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return initMenu(menu);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		Intent intent = null;
 
 		switch(item.getItemId()) {
 
 		case Defs.MENU_ADD_ACCOUNT:
 			intent = new Intent(this, AccountPreferencesActivity.class);
 			intent.putExtra(Defs.INTENT_ACCOUNT_ADD, true);
 			startActivityForResult(intent, Defs.INTENT_ACCOUNT_ADD_RQ);
 			break;
 
 		case Defs.MENU_MANAGE_ACCOUNTS:
 			showAccountsList();
 			break;
 
 		case Defs.MENU_ABOUT:
 			intent = new Intent(this, AboutActivity.class);
 			startActivity(intent);
 			break;
 		}
 
 		return true;
 	}
 
 	private boolean initMenu(Menu menu) {
 		menu.clear();
 		menu.add(1, Defs.MENU_ADD_ACCOUNT, 0, getResString(R.string.menu_add_account)).setIcon(R.drawable.ic_menu_invite);
 		menu.add(1, Defs.MENU_MANAGE_ACCOUNTS, 0, getResString(R.string.menu_manage_accounts)).setIcon(R.drawable.ic_menu_manage);
 		menu.add(1, Defs.MENU_ABOUT, 15, getResString(R.string.menu_about)).setIcon(R.drawable.ic_menu_help);
 		return true;
 	}
 
 	/**
 	 * Show a list of user accounts
 	 */
 	private void showAccountsList() {
 
 		final String[] items = UsersManager.getInstance().getUsersPhoneNumbersList();
 		if (items != null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.dlg_account_select_title);
 			builder.setCancelable(true);
 			builder.setNegativeButton(getResString(R.string.dlg_msg_cancel), new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Intent intent = new Intent(getApplicationContext(), AccountPreferencesActivity.class);
 					intent.putExtra(Defs.INTENT_ACCOUNT_EDIT, true);
 					intent.putExtra(Defs.INTENT_ACCOUNT_PHONENUMBER, items[which]);
 					startActivityForResult(intent, Defs.INTENT_ACCOUNT_EDIT_RQ);
 
 					dialog.dismiss();
 				}
 			});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		else {
 			Toast.makeText(getApplicationContext(), R.string.text_account_no_account, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Prefill accounts data in spinner
 	 */
 	private void updateAccountsSpinner() {
 		Spinner spinnerAccounts = (Spinner) findViewById(R.id.SpinnerUserAccounts);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.LayoutNoAccounts);
 		final String[] items = UsersManager.getInstance().getUsersPhoneNumbersList();
 
 		if (items != null) {
 			//_adapterAccounts = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
 			_adapterAccounts = new AccountsArrayAdapter(this, android.R.layout.simple_spinner_item, items);
 			_adapterAccounts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	        spinnerAccounts.setAdapter(_adapterAccounts);
 
 	        // visualize component
 	        spinnerAccounts.setVisibility(Spinner.VISIBLE);
 	        layout.setVisibility(LinearLayout.INVISIBLE);
 	        
 	        // pre-select
 	        if (GlobalSettings.getInstance().getLastSelectedAccount() != GlobalSettings.NO_ACCOUNT) {
 		        spinnerAccounts.setSelection(
 		        		_adapterAccounts.getItemPosition(GlobalSettings.getInstance().getLastSelectedAccount()));
 	        }
 		}
 		else {
 			// remove all items, if any
 //			if (_adapterAccounts != null) {
 //				_adapterAccounts.clear();
 //			}
 
 			// no accounts, no selection
 			spinnerAccounts.setVisibility(Spinner.INVISIBLE);
 			layout.setVisibility(LinearLayout.VISIBLE);
 		}
 	}
 
 	/**
 	 * Get selected spinner option and update view
 	 */
 	private void updateSelectedStatus() {
 		Spinner spinnerAccounts = (Spinner) findViewById(R.id.SpinnerUserAccounts);
 
 		if (UsersManager.getInstance().size() > 0 && spinnerAccounts.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
 
 			Spinner spinnerOptions = (Spinner) findViewById(R.id.SpinnerOptions);
 			final Operations operation = (Operations) spinnerOptions.getSelectedItem();
 			final TextView tx = (TextView) _activity.findViewById(R.id.TextContent);
 			final String phoneNumber = (String) spinnerAccounts.getItemAtPosition(spinnerAccounts.getSelectedItemPosition());
 
 			// show progress
 			final ProgressDialog myProgress = ProgressDialog.show(this, getResString(R.string.dlg_progress_title), getResString(R.string.dlg_progress_message), true);
 
 			// do work
 			new Thread() {
 				public void run() {
 
 					try {
 						Action action = new AccountStatusAction(operation,
 								UsersManager.getInstance().getUserByPhoneNumber(phoneNumber));
 						
 						// remember last account and operation
 						GlobalSettings.getInstance().putLastSelectedAccount(phoneNumber);
 						GlobalSettings.getInstance().putLastSelectedOperation(operation);
 						
 						final String data = action.execute().getString();
 						
 						// save what was last found
 						GlobalSettings.getInstance().putLastCheckedInfo(data);
 
 						// update text field
 						_activity.runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								tx.setText(Html.fromHtml(data));
 								//WebView wv = (WebView) _activity.findViewById(R.id.TextContent);
 								//wv.loadData(data, "text/html", "utf-8");
 							}
 						});
 						
 					}
 					catch (InvalidCredentialsException e) {
 						// Show error dialog
 						Utils.showAlertDialog(_activity, R.string.dlg_error_msg_invalid_credentials, 
 								R.string.dlg_error_msg_title);
 					}
 					catch (SecureCodeRequiredException e) {
 						// Show error dialog
 						Utils.showAlertDialog(_activity, R.string.dlg_error_msg_securecode, R.string.dlg_error_msg_title);
 					}
 					catch (Exception e) {
 						Log.e(Defs.LOG_TAG, "Error updating status!", e);
 						// Show error dialog
 						final String msg = e.getMessage();
 						Utils.showAlertDialog(_activity, msg, getResString(R.string.dlg_error_msg_title));
 					}
 
 					// close progress bar dialog
 					_activity.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							myProgress.dismiss();
 						}
 					});
 				};
 			}.start();
 		}
 		else  {
 
 			// show add user screen
 			Toast.makeText(getApplicationContext(), R.string.text_account_add_new, Toast.LENGTH_SHORT).show();
 
 		} // end if
 	}
 
 	private String getResString(int id) {
 		return this.getResources().getString(id);
 	}
 }
