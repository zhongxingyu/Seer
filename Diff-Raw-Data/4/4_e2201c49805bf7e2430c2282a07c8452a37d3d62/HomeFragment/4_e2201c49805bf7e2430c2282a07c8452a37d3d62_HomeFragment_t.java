 /*
  * The MIT License
  *
  * Copyright (c) 2013 Petar Petrov
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
 package net.vexelon.myglob.fragments;
 
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.vexelon.mobileops.InvalidCredentialsException;
 import net.vexelon.mobileops.SecureCodeRequiredException;
 import net.vexelon.myglob.AccountsArrayAdapter;
 import net.vexelon.myglob.Operations;
 import net.vexelon.myglob.R;
 import net.vexelon.myglob.actions.AccountStatusAction;
 import net.vexelon.myglob.actions.Action;
 import net.vexelon.myglob.actions.ActionResult;
 import net.vexelon.myglob.configuration.AccountPreferencesActivity;
 import net.vexelon.myglob.configuration.Defs;
 import net.vexelon.myglob.configuration.GlobalSettings;
 import net.vexelon.myglob.users.User;
 import net.vexelon.myglob.users.UsersManager;
 import net.vexelon.myglob.utils.Utils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.Html;
 import android.text.format.DateFormat;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HomeFragment extends BaseFragment implements OnClickListener, OnTouchListener {
 	// unique ID
 	public static final int TAB_ID = 0;
 	
 	public static HomeFragment newInstance() {
 		HomeFragment fragment = new HomeFragment();
 
         Bundle args = new Bundle();
         fragment.setArguments(args);
         
         return fragment;		
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.main, container, false);
 		
 		TextView tvPhoneNumber = (TextView) v.findViewById(R.id.tv_profile_number);
 		tvPhoneNumber.setOnClickListener(this);
 		tvPhoneNumber.setOnTouchListener(this);
 		
 		ImageView ivSelection = (ImageView) v.findViewById(R.id.iv_user_selection);
 		ivSelection.setImageResource(R.drawable.ab_default_holo_dark);		
 //
 		return v;
 	}
 	
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 	}
 	
     @Override
     public void onStart() {
 //    	// trap Operations selection
 //    	Spinner spinnerOptions = (Spinner) findViewById(R.id.SpinnerOptions);
 //        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 //        	@Override
 //        	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 //        		Operations operation = (Operations) parentView.getSelectedItem();
 //        		GlobalSettings.getInstance().putLastSelectedOperation(operation);
 //        	}
 //        	
 //        	@Override
 //        	public void onNothingSelected(AdapterView<?> parentView) {
 //        	}
 //		});   
 //        
 //        // trap phone number selection
 //        Spinner spinnerAccounts = (Spinner) findViewById(R.id.SpinnerUserAccounts);
 //        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 //        	@Override
 //        	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 //        		String phoneNumber = (String) parentView.getSelectedItem();
 //        		GlobalSettings.getInstance().putLastSelectedAccount(phoneNumber);
 //        	}
 //        	
 //        	@Override
 //        	public void onNothingSelected(AdapterView<?> parentView) {
 //        	}
 //		});
     	
 		// load last saved operation info (if available)
 		if (GlobalSettings.getInstance().getLastCheckedInfo() != GlobalSettings.NO_INFO) {
 			TextView textContent = (TextView) getView().findViewById(R.id.tv_status_content);
 			textContent.setText(Html.fromHtml(GlobalSettings.getInstance().getLastCheckedInfo()));
 		}    	
     	
     	updateProfileView(GlobalSettings.getInstance().getLastSelectedAccount());
         // pre-select
     	
     	super.onStart();    	
     }	
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		// app resumed
 	}
 	
 	@Override
 	public void onClick(View v) {
 		int id = v.getId();
 		
 		switch(id) {
 		case R.id.tv_profile_number:
 			showAccountsList(new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					final String[] items = UsersManager.getInstance().getUsersPhoneNumbersList();
 					updateProfileView(items[which]);
 					
 					dialog.dismiss();
 				}
 			});
 			break;
 		}
 	}
 	
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		int id = v.getId();
 		
 		switch(id) {
 		case R.id.tv_profile_number:
 			
 			ImageView ivSelection = (ImageView) getView().findViewById(R.id.iv_user_selection);
 			
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				ivSelection.setImageResource(R.drawable.ab_pressed_holo_dark);				
 				return true;
 			case MotionEvent.ACTION_UP:
 				ivSelection.setImageResource(R.drawable.ab_default_holo_dark);	
 				onClick(v);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch(requestCode) {
     	case Defs.INTENT_ACCOUNT_ADD_RQ:
     		if (resultCode == Activity.RESULT_OK) {
     			Toast.makeText(this.getActivity().getApplicationContext(), 
     					R.string.text_account_created, Toast.LENGTH_SHORT).show();
     		}
     		break;
 
     	case Defs.INTENT_ACCOUNT_EDIT_RQ:
     		if (resultCode == Activity.RESULT_OK) {
     			Toast.makeText(this.getActivity().getApplicationContext(), 
     					R.string.text_account_saved, Toast.LENGTH_SHORT).show();
     		}
     		else if (resultCode == Defs.INTENT_RESULT_ACCOUT_DELETED) {
     			Toast.makeText(this.getActivity().getApplicationContext(), 
     					R.string.text_account_removed, Toast.LENGTH_SHORT).show();
     		}
     		break;
     	}
 
         // pre-select
         if (GlobalSettings.getInstance().getLastSelectedAccount() != GlobalSettings.NO_ACCOUNT) {
         	updateProfileView(GlobalSettings.getInstance().getLastSelectedAccount());
         }     	
     }
     
     /**
      * Show preferences Activity where new account may be added
      */
     public void showAddAccount() {
     	Intent intent = new Intent(this.getActivity(), AccountPreferencesActivity.class);
 		intent.putExtra(Defs.INTENT_ACCOUNT_ADD, true);
 		startActivityForResult(intent, Defs.INTENT_ACCOUNT_ADD_RQ);    	
     }
     
     /**
      * Show list of accounts that can be edited
      */
     public void showEditAccount() {
     	
     	final FragmentActivity activity = this.getActivity();
 
     	showAccountsList(new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					final String[] items = UsersManager.getInstance().getUsersPhoneNumbersList();
 					
 					Intent intent = new Intent(activity.getApplicationContext(), 
 							AccountPreferencesActivity.class);
 					intent.putExtra(Defs.INTENT_ACCOUNT_EDIT, true);
 					intent.putExtra(Defs.INTENT_ACCOUNT_PHONENUMBER, items[which]);
 					startActivityForResult(intent, Defs.INTENT_ACCOUNT_EDIT_RQ);
 
 					dialog.dismiss();
 				}
 			});
     }
     
     /**
      * Update all data with respect to selected account
      */
     private void updateProfileView(String phoneNumber) {
     	if (Defs.LOG_ENABLED)
     		Log.v(Defs.LOG_TAG, "Updating selection for: " + phoneNumber);
     	
     	View v = getView();
     	
     	User user = UsersManager.getInstance().getUserByPhoneNumber(phoneNumber);
     	if (user != null) {
     		setText(v, R.id.tv_profile_number, user.getPhoneNumber());
     		setText(v, R.id.tv_profile_name, user.getAccountName());
     		setText(v, R.id.tv_checks_today, String.valueOf(user.getChecksToday()));
     		setText(v, R.id.tv_checks_overal, String.valueOf(user.getChecksTotal()));
     		setText(v, R.id.tv_traffic_today, 
     				Formatter.formatFileSize(this.getActivity(), user.getTrafficToday())); 
     		setText(v, R.id.tv_traffic_overal, 
     				Formatter.formatFileSize(this.getActivity(), user.getTrafficTotal())); 
     		
     		Calendar calendar = Calendar.getInstance();
     		calendar.setTimeInMillis(user.getLastCheckDateTime());
     		
     		StringBuilder dateText = new StringBuilder(100);
     		dateText.append(getString(R.string.text_from))
     		.append(" ")
    		.append(new SimpleDateFormat("dd-MM-yy HH:mm").format(calendar.getTime()));
     		
     		setText(v, R.id.tv_profile_lastchecked_at, dateText.toString());
     		
     	} else {
 			Toast.makeText(this.getActivity().getApplicationContext(), 
 					R.string.text_account_not_found, Toast.LENGTH_SHORT).show();
     	}
     }
     
 
 	/**
 	 * Show a list of user accounts
 	 */
 	private void showAccountsList(DialogInterface.OnClickListener clickListener) {
     	if (Defs.LOG_ENABLED)
     		Log.v(Defs.LOG_TAG, "showAccountsList()");
     	
 		final String[] items = UsersManager.getInstance().getUsersPhoneNumbersList();
 		if (items != null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
 			builder.setTitle(R.string.dlg_account_select_title);
 			builder.setCancelable(true);
 			builder.setNegativeButton(R.string.dlg_msg_cancel, new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 //			builder.setSingleChoiceItems(items, -1, clickListener);
 			builder.setSingleChoiceItems(
 					new AccountsArrayAdapter(this.getActivity(), android.R.layout.select_dialog_singlechoice, items), 
 					-1, 
 					clickListener);			
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		} else {
 			Toast.makeText(this.getActivity().getApplicationContext(), 
 					R.string.text_account_no_account, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Get selected spinner option and update view
 	 */
 	public void updateStatus(Operations operation) {
     	if (Defs.LOG_ENABLED)
     		Log.v(Defs.LOG_TAG, "updateStatus with opId= " + operation.getId());
     	
 		View v = this.getView();
 		
 		final FragmentActivity activity = getActivity();
 		
 		if (UsersManager.getInstance().size() > 0) {
 
 			final Operations finalOperation = operation;
 			final TextView tvContent = (TextView) v.findViewById(R.id.tv_status_content);
 			final TextView tvPhoneNumber = (TextView) v.findViewById(R.id.tv_profile_number);
 			final String phoneNumber = (String) tvPhoneNumber.getText();
 
 			// show progress
 			final ProgressDialog myProgress = ProgressDialog.show(activity, 
 					getResString(R.string.dlg_progress_title), 
 					getResString(R.string.dlg_progress_message), 
 					true);
 
 			new Thread() {
 				public void run() {
 
 					try {
 						User user = UsersManager.getInstance().getUserByPhoneNumber(phoneNumber);
 						
 						Action action = new AccountStatusAction(finalOperation, user);
 						
 						// remember last account and operation
 						GlobalSettings.getInstance().putLastSelectedAccount(phoneNumber);
 						GlobalSettings.getInstance().putLastSelectedOperation(finalOperation);
 						
 						final ActionResult actionResult = action.execute();
 						
 						// save last found 
 						GlobalSettings.getInstance().putLastCheckedInfo(actionResult.getString());
 						
 						// update user info
 						UsersManager.getInstance().setUserResult(user, actionResult);
 						SharedPreferences prefs = activity.getSharedPreferences(Defs.PREFS_USER_PREFS, 0);
 						UsersManager.getInstance().save(prefs);
 
 						// update text field
 						activity.runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								tvContent.setText(Html.fromHtml(actionResult.getString()));
 								updateProfileView(phoneNumber);
 							}
 						});
 						
 					} catch (InvalidCredentialsException e) {
 						// Show error dialog
 						Utils.showAlertDialog(activity, R.string.dlg_error_msg_invalid_credentials, 
 								R.string.dlg_error_msg_title);
 					} catch (SecureCodeRequiredException e) {
 						// Show error dialog
 						Utils.showAlertDialog(activity, R.string.dlg_error_msg_securecode, R.string.dlg_error_msg_title);
 					} catch (Exception e) {
 						Log.e(Defs.LOG_TAG, "Error updating status!", e);
 						// Show error dialog
 						final String msg = e.getMessage();
 						Utils.showAlertDialog(activity, msg, getResString(R.string.dlg_error_msg_title));
 					}
 
 					// close progress bar dialog
 					activity.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							myProgress.dismiss();
 						}
 					});
 				};
 			}.start();
 		} else  {
 			// show add user screen
 			Toast.makeText(this.getActivity().getApplicationContext(), 
 					R.string.text_account_add_new, Toast.LENGTH_SHORT).show();
 
 		}
 	}
 	
 }
