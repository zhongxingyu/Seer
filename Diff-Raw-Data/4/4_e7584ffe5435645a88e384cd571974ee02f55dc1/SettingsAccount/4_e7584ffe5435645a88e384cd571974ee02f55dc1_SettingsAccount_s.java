 package com.ioabsoftware.gameraven;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.DialogInterface.OnShowListener;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ioabsoftware.gameraven.networking.HandlesNetworkResult;
 import com.ioabsoftware.gameraven.networking.NetworkTask;
 import com.ioabsoftware.gameraven.networking.Session;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 public class SettingsAccount extends PreferenceActivity implements HandlesNetworkResult {
 
 	public static final int ADD_ACCOUNT_DIALOG = 300;
 	public static final int VERIFY_ACCOUNT_DIALOG = 301;
 	public static final int MODIFY_ACCOUNT_DIALOG = 302;
 	
 	String verifyUser;
 	String verifyPass;
 
 	PreferenceCategory accounts;
 	Preference clickedAccount;
 
 	SharedPreferences settings;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		settings = AllInOneV2.getSettingsPref();
 		if (AllInOneV2.getUsingLightTheme()) {
         	setTheme(R.style.MyThemes_LightTheme);
         }
 		
 		super.onCreate(savedInstanceState);
         
         addPreferencesFromResource(R.xml.settingsaccount);
 
         Drawable aBarDrawable;
 		if (AllInOneV2.getUsingLightTheme())
 			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_dark_holo);
 		else
 			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_light_holo);
 		
 		aBarDrawable.setColorFilter(AllInOneV2.getAccentColor(), PorterDuff.Mode.SRC_ATOP);
 		getActionBar().setBackgroundDrawable(aBarDrawable);
 
         accounts = (PreferenceCategory) findPreference("accounts");
 		updateAccountList();
         
         findPreference("addAccount").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     showDialog(ADD_ACCOUNT_DIALOG);
                     return true;
                 }
 
         });
 	}
 
 	private void updateAccountList() {
 		accounts.removeAll();
 		
 		String def = settings.getString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT);
 		
 		for (String s : AllInOneV2.getAccounts().getKeys()) {
         	Preference pref = new Preference(this);
         	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         		@Override
         		public boolean onPreferenceClick(Preference preference) {
                 	clickedAccount = preference;
         			showDialog(MODIFY_ACCOUNT_DIALOG);
         			return true;
         		}
 			});
         	
         	String sig = settings.getString("customSig" + s, "");
         	
         	pref.setTitle(s);
         	
         	if (s.equals(def) && !sig.equals(""))
         		pref.setSummary("Default account, custom signature applied");
         	else if (s.equals(def))
         		pref.setSummary("Default account");
         	else if (!sig.equals(""))
         		pref.setSummary("Custom signature applied");
         	
         	pref.setPersistent(false);
         	
         	accounts.addPreference(pref);
 		}
 	}
 	
 	// creates dialogs
     @Override
     protected Dialog onCreateDialog(int id) {
     	Dialog dialog = null;
     	switch (id) {
     	case ADD_ACCOUNT_DIALOG:
     		dialog = createAddAccountDialog();
     		break;
     	case VERIFY_ACCOUNT_DIALOG:
     		ProgressDialog d = new ProgressDialog(this);
     		d.setTitle("Verifying Account...");
     		dialog = d;
     		break;
     	case MODIFY_ACCOUNT_DIALOG:
     		dialog = createModifyAccountV2Dialog();
     		break;
     	}
     	return dialog;
     }
     
     private Dialog createAddAccountDialog() {
     	AlertDialog.Builder b = new AlertDialog.Builder(this);
     	LayoutInflater inflater = getLayoutInflater();
     	final View v = inflater.inflate(R.layout.addaccount, null);
     	b.setView(v);
     	b.setTitle("Add Account");
     	
     	b.setNegativeButton("Cancel", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				removeDialog(ADD_ACCOUNT_DIALOG);
 			}
 		});
     	
     	b.setPositiveButton("OK", null);
     	
     	final AlertDialog d = b.create();
     	d.setOnShowListener(new OnShowListener() {
 			@Override
 			public void onShow(DialogInterface dialog) {
 				d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
 
 		            @Override
 		            public void onClick(View view) {
						verifyUser = ((TextView) v.findViewById(R.id.addaccUser)).getText().toString();
 						verifyPass = ((TextView) v.findViewById(R.id.addaccPassword)).getText().toString();
 						
 						if (verifyUser.indexOf('@') == -1) {
 							showDialog(VERIFY_ACCOUNT_DIALOG);
 							new NetworkTask(SettingsAccount.this,
 									NetDesc.VERIFY_ACCOUNT_S1, Method.GET,
 									new HashMap<String, String>(), Session.ROOT, null)
 									.execute();
 						}
 						else {
 							Crouton.showText(SettingsAccount.this, 
 									"Please use your username, not your email address.", 
 									AllInOneV2.getCroutonStyle());
 						}
 					}
 		        });
 			}
 		});
 				
     	return d;
     }
     
     private Dialog createModifyAccountV2Dialog() {
     	AlertDialog.Builder b = new AlertDialog.Builder(this);
     	LayoutInflater inflater = getLayoutInflater();
     	final View v = inflater.inflate(R.layout.modifyaccountv2, null);
     	b.setView(v);
     	b.setTitle("Modify "  + clickedAccount.getTitle().toString());
     	
     	Button deleteAcc = (Button) v.findViewById(R.id.modaccDeleteAcc);
     	final CheckBox defaultAcc = (CheckBox) v.findViewById(R.id.modaccDefaultAccount);
     	final EditText sigContent = (EditText) v.findViewById(R.id.modaccSigContent);
     	final TextView sigCounter = (TextView) v.findViewById(R.id.modaccSigCounter);
     	
 		if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT)))
 			defaultAcc.setChecked(true);
 		else
 			defaultAcc.setChecked(false);
 		
 		defaultAcc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked) {
 					settings.edit().putString("defaultAccount", clickedAccount.getTitle().toString()).commit();
 					Crouton.showText(SettingsAccount.this, "Default account saved.", AllInOneV2.getCroutonStyle());
 				}
 				else {
 					settings.edit().putString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT).commit();
 					settings.edit().putLong("notifsLastPost", 0).commit();
 					Crouton.showText(SettingsAccount.this, "Default account removed.", AllInOneV2.getCroutonStyle());
 				}
 			}
 		});
     	
     	deleteAcc.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT)))
 					settings.edit().putString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT).commit();
 
 				settings.edit().remove("customSig" + clickedAccount.getTitle().toString()).commit();
 				
 				AllInOneV2.getAccounts().removeValue(clickedAccount.getTitle().toString());
 				accounts.removePreference(clickedAccount);
				accounts.removePreference(clickedAccount);
 				dismissDialog(MODIFY_ACCOUNT_DIALOG);
 				Crouton.showText(SettingsAccount.this, "Account removed.", AllInOneV2.getCroutonStyle());
 			}
         });
     	
     	sigContent.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
 				int length = escapedSig.length();
 				int lines = 0;
 				for(int i = 0; i < escapedSig.length(); i++) {
 				    if(escapedSig.charAt(i) == '\n') lines++;
 				}
 				
 				sigCounter.setText((1 - lines) + " line break(s), " + (160 - length) + " characters available");
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 	    });
 		
 		sigContent.setText(settings.getString("customSig" + clickedAccount.getTitle().toString(), ""));
     	
     	b.setPositiveButton("Save Sig", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
 				int length = escapedSig.length();
 				int lines = 0;
 				for(int i = 0; i < escapedSig.length(); i++) {
 				    if(escapedSig.charAt(i) == '\n') lines++;
 				}
 				
 				if (length < 161) {
 					if (lines < 2) {
 						settings.edit().putString("customSig" + clickedAccount.getTitle().toString(), 
 								sigContent.getText().toString()).commit();
 						
 						Crouton.showText(SettingsAccount.this, "Signature saved.", AllInOneV2.getCroutonStyle());
 					}
 					else
 						Crouton.showText(SettingsAccount.this, 
 								"Signatures can only have 1 line break.", 
 								AllInOneV2.getCroutonStyle());
 				}
 				else
 					Crouton.showText(SettingsAccount.this, 
 							"Signatures can only have a maximum of 160 characters.", 
 							AllInOneV2.getCroutonStyle());
 			}
 		});
     	
     	b.setNeutralButton("Clear Sig", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				settings.edit().putString("customSig" + clickedAccount.getTitle().toString(), "").commit();
 				sigContent.setText("");
 				Crouton.showText(SettingsAccount.this, "Signature cleared and saved.", AllInOneV2.getCroutonStyle());
 			}
 		});
     	
     	b.setNegativeButton("Close", null);
     	
     	AlertDialog d = b.create();
     	
     	d.setOnDismissListener(new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				updateAccountList();
 				removeDialog(MODIFY_ACCOUNT_DIALOG);
 			}
 		});
     	
     	return d;
     }
     
     @Override
 	public void handleNetworkResult(Response res, NetDesc desc) {
 		if (res != null) {
 			Document pRes = null;
 			try {
 				pRes = res.parse();
 			} catch (IOException e) {
 				// should never fail
 				e.printStackTrace();
 			}
 			if (desc == NetDesc.VERIFY_ACCOUNT_S1) {
 				String loginKey = pRes.getElementsByAttributeValue("name",
 						"key").attr("value");
 				HashMap<String, String> loginData = new HashMap<String, String>();
 				// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
 				loginData.put("EMAILADDR", verifyUser);
 				loginData.put("PASSWORD", verifyPass);
 				loginData.put("path", Session.ROOT);
 				loginData.put("key", loginKey);
 				new NetworkTask(SettingsAccount.this, NetDesc.VERIFY_ACCOUNT_S2,
 						Method.POST, res.cookies(), Session.ROOT
 								+ "/user/login.html", loginData).execute();
 			}
 			else if (desc == NetDesc.VERIFY_ACCOUNT_S2) {
 				if (!res.url().toString().equals(Session.ROOT + "/user/login.html")) {
 					AllInOneV2.getAccounts().put(verifyUser, verifyPass);
 		    		dismissDialog(VERIFY_ACCOUNT_DIALOG);
 					removeDialog(ADD_ACCOUNT_DIALOG);
 					Crouton.showText(this, "Verification succeeded.", AllInOneV2.getCroutonStyle());
 				}
 				else {
 					dismissDialog(VERIFY_ACCOUNT_DIALOG);
 					showDialog(ADD_ACCOUNT_DIALOG);
 					Crouton.showText(this, 
 							"Verification failed. Check your username and password and try again.", 
 							AllInOneV2.getCroutonStyle());
 				}
 			}
 		}
 		else {
 			dismissDialog(VERIFY_ACCOUNT_DIALOG);
 			Crouton.showText(this, "Network connection failed. Check your network settings.", AllInOneV2.getCroutonStyle());
 		}
 	}
 
 	@Override
 	public void preExecuteSetup(NetDesc desc) {}
 
 	@Override
 	public void postExecuteCleanup(NetDesc desc) {
 		updateAccountList();
 	}
 }
