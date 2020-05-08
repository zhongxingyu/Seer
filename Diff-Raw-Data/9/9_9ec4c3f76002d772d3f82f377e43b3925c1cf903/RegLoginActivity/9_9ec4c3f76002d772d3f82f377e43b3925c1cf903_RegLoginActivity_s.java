 package com.zrd.zr.letuwb;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import weibo4android.OAuthConstant;
 import weibo4android.User;
 import weibo4android.Weibo;
 import weibo4android.WeiboException;
 import weibo4android.http.AccessToken;
 import weibo4android.http.OAuthVerifier;
 import weibo4android.http.RequestToken;
 
 import com.zrd.zr.letuwb.R;
 import com.zrd.zr.pnj.SecureURL;
 import com.zrd.zr.protos.WeibousersProtos.UCMappings;
 import com.zrd.zr.weiboes.Sina;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RegLoginActivity extends Activity {
 	private static boolean mIfQuit = false;
 	private static ArrayList<Context> mContexts = new ArrayList<Context>();
 	
 	private TableLayout mTableBackground;
 	private EditText mEditUsername;
 	private EditText mEditPassword;
 	private EditText mEditRepeat;
 	
 	private TableRow mRowRepeat;
 	private TableRow mRowLoginReg;
 	private CheckBox mCheckRemember;
 	private Button mBtnLogin;
 	private Button btnGuest;
 	private Button btnReg;
 	private Button btnLetMeReg;
 	private ListView mListAccounts;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.reg_login);
 		
 		mTableBackground = (TableLayout) findViewById(R.id.tlBackground);
 		mEditUsername = (EditText) findViewById(R.id.etUsername);
 		mEditPassword = (EditText) findViewById(R.id.etPassword);
 		mEditRepeat = (EditText) findViewById(R.id.etRepeat);
 		mRowRepeat = (TableRow) findViewById(R.id.trRepeat);
 		mRowLoginReg = (TableRow) findViewById(R.id.trLoginReg);
 		mCheckRemember = (CheckBox) findViewById(R.id.cbRemember);
 		mBtnLogin = (Button) findViewById(R.id.btnLogin);
 		btnGuest = (Button) findViewById(R.id.btnGuest);
 		btnReg = (Button) findViewById(R.id.btnReg);
 		btnReg.setVisibility(Button.GONE);
 		btnLetMeReg = (Button) findViewById(R.id.btnLetmereg);
 		btnLetMeReg.setText(Html.fromHtml("<u>" + getString(R.string.label_letmereg) + "</u>"));
 		mListAccounts = (ListView)findViewById(R.id.lvAccounts);
 		
 		mRowLoginReg.setVisibility(TableRow.GONE);
 		
 		initAccountsList();
 		mListAccounts.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				int position = arg2;
 				switch (position) {
 				case 0://means going to add a account
 					mRowLoginReg.setVisibility(TableRow.VISIBLE);
 					break;
 				default:
 					int idx = position - 1;
 					if (idx < 0) idx = 0;
 					ArrayList<String[]> list = EntranceActivity.getStoredAccounts();
 					if (idx < list.size()) {
 						String[] pair = list.get(idx);
 						mEditUsername.setText(pair[0]);
 						mEditPassword.setText(pair[1]);
 						mBtnLogin.performClick();
 						EntranceActivity.delAccount(pair[0], pair[1]);
 						EntranceActivity.saveAccount(pair[0], pair[1]);
 					}
 					break;
 				}
 			}
 			
 		});
 		
 		mListAccounts.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 				// TODO Auto-generated method stub
 				mListAccounts.setTag(position);
 				new AlertDialog.Builder(RegLoginActivity.this)
 					.setTitle(R.string.tips_confirmdelaccount)
 					.setPositiveButton(
 						R.string.label_ok,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								int idx = (Integer)mListAccounts.getTag() - 1;
 								if (idx >= 0) {
 									ArrayList<String[]> list = EntranceActivity.getStoredAccounts();
 									String usr = list.get(idx)[0];
 									String pwd = list.get(idx)[1];
 									EntranceActivity.delAccount(usr, pwd);
 									list.remove(idx);
 									String[] usernames = new String[list.size() + 1];
 									usernames[0] = getString(R.string.label_addaccount);
 									for (int i = 1; i < usernames.length; i++) {
 										usernames[i] = list.get(i - 1)[0];
 									}
 									mListAccounts.setAdapter(
 										new ArrayAdapter<String>(
 											RegLoginActivity.this,
 											android.R.layout.simple_list_item_1,
 											usernames
 										)
 									);
 								}
 							}
 							
 						}
 					)
 					.setNegativeButton(R.string.label_cancel, null)
 					.create()
 					.show();
 				return false;
 			}
 			
 		});
 		
 		btnLetMeReg.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mRowRepeat.getVisibility() == TableRow.GONE) {
 					mRowRepeat.setVisibility(TableRow.VISIBLE);
 					mBtnLogin.setVisibility(Button.GONE);
 					btnReg.setVisibility(Button.VISIBLE);
 					btnLetMeReg.setText(Html.fromHtml("<u>" + getString(R.string.label_letmelogon) + "</u>"));
 				} else {
 					mRowRepeat.setVisibility(TableRow.GONE);
 					mBtnLogin.setVisibility(Button.VISIBLE);
 					btnReg.setVisibility(Button.GONE);
 					btnLetMeReg.setText(Html.fromHtml("<u>" + getString(R.string.label_letmereg) + "</u>"));
 				}
 			}
 			
 		});
 		
 		/*
 		 * login SINA_weibo with the input account
 		 */
 		mBtnLogin.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mRowRepeat.setVisibility(TableRow.GONE);
 				if (mEditUsername.getText().toString().trim().equals("")
 					|| mEditPassword.getText().toString().trim().equals("")) {
 					Toast.makeText(RegLoginActivity.this, R.string.err_needaccount, Toast.LENGTH_LONG).show();
 					return;
 				}
 				
 				/*
 				 * get SINA_weibo's token and token secret for the account
 				 */
 				new AlertDialog.Builder(RegLoginActivity.this)
 					.setIcon(R.drawable.icon)
 					.setTitle(R.string.title_attention)
					.setMessage(R.string.tips_wheterprofileimagecouldbeusedornot)
 					.setPositiveButton(
 						R.string.label_allright,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								AsyncLogin asyncLogin = new AsyncLogin();
 								asyncLogin.execute();
 							}
 							
 						}
 					)
 					.setNegativeButton(R.string.label_letmethink, null)
 					.create()
 					.show();	
 			}
 		});
 		
 		btnGuest.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				finish();
 			}
 			
 		});
 		
 		btnReg.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mRowRepeat.getVisibility() == TableRow.GONE) {
 					mRowRepeat.setVisibility(TableRow.VISIBLE);
 					return;
 				} else {
 					if (mEditUsername.getText().toString().trim().equals("")
 						|| mEditPassword.getText().toString().trim().equals("")
 						|| mEditRepeat.getText().toString().trim().equals("")) {
 						Toast.makeText(RegLoginActivity.this, R.string.err_needaccount, Toast.LENGTH_LONG).show();
 						return;
 					}
 					if (mEditPassword.getText().toString().equals(mEditRepeat.getText().toString())) {
 						Map<String, String> actions = new HashMap<String, String>();
 						actions.put("usr", mEditUsername.getText().toString());
 						actions.put("pwd", mEditPassword.getText().toString());
 						actions.put("rpt", mEditRepeat.getText().toString());
 						actions.put("ckey", EntranceActivity.getClientKey());
 						String sBackMsg = "";
 						try {
 							sBackMsg = AsyncUploader.post(EntranceActivity.URL_SITE + "reglogin.php", actions, null);
 							String ss[] = EntranceActivity.getPhpMsg(sBackMsg);
 							if (ss != null) {
 								sBackMsg = ss[1];
 							} else {
 								sBackMsg = "~Wrong connection.~";
 							}
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							sBackMsg = "~Wrong connection.~";
 							e.printStackTrace();
 						}
 						String[] msgparts = sBackMsg.split("\\.");
 						Toast.makeText(RegLoginActivity.this, msgparts[0], Toast.LENGTH_LONG).show();
 						if (msgparts.length == 2 && msgparts[0].equals("Registered")) {
 							try {
 								EntranceActivity.setAccountId(Integer.parseInt(msgparts[1]));
 							} catch (NumberFormatException e) {
 								EntranceActivity.setAccountId(-1);
 							}
 							EntranceActivity.setPrivilege(0);
 							finish();
 						} else {
 							EntranceActivity.setPrivilege(1);
 						}
 					} else {
 						Toast.makeText(RegLoginActivity.this, R.string.err_needrepeat, Toast.LENGTH_SHORT).show();
 						return;
 					}
 				}
 			}
 			
 		});
 	}
 	
 	public static boolean ifQuitIsSet() {
 		return mIfQuit;
 	}
 	
 	public static void updateTitle(
 		int resTitleIcon, int resTitleName, User user) {
 		if (mContexts.size() == 0) return;
 		for (int i = 0; i < mContexts.size(); i++) {
 			Context context = mContexts.get(i);
 			ImageView iv = (ImageView)((Activity)context).findViewById(resTitleIcon);
 			TextView tv = (TextView)((Activity)context).findViewById(resTitleName);
 			if (iv != null && tv != null && user != null) {
 				AsyncImageLoader loader = new AsyncImageLoader(context,
 					iv, R.drawable.person);
 				loader.execute(user.getProfileImageURL());
 				tv.setText(user.getScreenName());
 			}
 		}
 	}
 	
 	public void initAccountsList() {
 		/*
 		 * initialize the accounts list
 		 */
 		ArrayList<String[]> list = EntranceActivity.getStoredAccounts();
 		String[] usernames = new String[list.size() + 1];
 		usernames[0] = getString(R.string.label_addaccount);
 		for (int i = 1; i < usernames.length; i++) {
 			usernames[i] = list.get(i - 1)[0];
 			if (i == 1) usernames[i] += ("(" + getString(R.string.label_default) + ")");
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 			this,
 			android.R.layout.simple_list_item_1,
 			usernames
 		);
 		mListAccounts.setAdapter(adapter);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 		
 		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			mTableBackground.setBackgroundResource(R.drawable.bg_h);
 		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			mTableBackground.setBackgroundResource(R.drawable.bg);
 		}
 	}
 
 	public static Sina login(String username, String password) {
 		/*
 		 * get SINA_weibo's token and token secret for the account
 		 */
 		Weibo weibo;
 		RequestToken requestToken;
 		try {
 			Sina sina = new Sina(true);
 			weibo = sina.getWeibo();
 			requestToken = weibo.getOAuthRequestToken();
 			OAuthConstant.getInstance().setRequestToken(requestToken);
 			OAuthVerifier oauthVerifier = weibo.getOAuthVerifier(username, password);
 			String verifier = oauthVerifier.getVerifier();
 			AccessToken accessToken = requestToken.getAccessToken(verifier);
 			OAuthConstant.getInstance().setAccessToken(accessToken);
 			
 			sina.getWeibo().setOAuthAccessToken(
 				accessToken.getToken(),
 				accessToken.getTokenSecret()
 			);
 			User user = sina.getWeibo().showUser("" + accessToken.getUserId());
 			sina.setLoggedInUser(user);
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 			SecureURL su = new SecureURL();
 			String strURL = "";
 			try {
 				strURL = EntranceActivity.URL_SITE + "updusr.php?"
 					+ "user[id]=" + sina.getLoggedInUser().getId()
 					+ "&user[screen_name]=" + URLEncoder.encode(sina.getLoggedInUser().getScreenName(), "UTF-8")
 					+ "&user[name]=" + URLEncoder.encode(sina.getLoggedInUser().getName(), "UTF-8")
 					+ "&user[province]=" + sina.getLoggedInUser().getProvince()
 					+ "&user[city]=" + sina.getLoggedInUser().getCity()
 					+ "&user[location]=" + URLEncoder.encode(sina.getLoggedInUser().getLocation(), "UTF-8")
 					+ "&user[description]=" + URLEncoder.encode(sina.getLoggedInUser().getDescription(), "UTF-8")
 					+ "&user[url]=" + sina.getLoggedInUser().getURL()
 					+ "&user[profile_image_url]=" + sina.getLoggedInUser().getProfileImageURL()
 					+ "&user[domain]=" + sina.getLoggedInUser().getUserDomain()
 					+ "&user[gender]=" + sina.getLoggedInUser().getGender()
 					+ "&user[followers_count]=" + sina.getLoggedInUser().getFollowersCount()
 					+ "&user[friends_count]=" + sina.getLoggedInUser().getFriendsCount()
 					+ "&user[statuses_count]=" + sina.getLoggedInUser().getStatusesCount()
 					+ "&user[favourites_count]=" + sina.getLoggedInUser().getFavouritesCount()
 					+ "&user[created_at]=" + sdf.format(sina.getLoggedInUser().getCreatedAt())
 					+ "&user[allow_all_act_msg]=" + (sina.getLoggedInUser().isAllowAllActMsg() ? 1 : 0)
 					+ "&user[geo_enabled]=" + (sina.getLoggedInUser().isGeoEnabled() ? 1 : 0)
 					+ "&user[verified]=" + (sina.getLoggedInUser().isVerified() ? 1 : 0)
 					+ "&channelid=" + "0"
 					+ "&clientkey=" + EntranceActivity.getClientKey();
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			URLConnection conn = su.getConnection(strURL);
 			if (conn != null) {
 				try {
 					conn.connect();
 					InputStream is = conn.getInputStream();
 					UCMappings mappings = UCMappings.parseFrom(is);
 					if ((mappings.getFlag() % 10) == 3
 						&& mappings.getMappingCount() > 0) {
 						//need to replace the client key with the returned one
 						EntranceActivity.setClientKey(mappings.getMapping(0).getClientkey());
 						SharedPreferences.Editor edit = EntranceActivity.mPreferences.edit();
 						edit.putString(EntranceActivity.CONFIG_CLIENTKEY, EntranceActivity.getClientKey());
 						edit.commit();
 						/*
 						 * and if there are any possessions belong to the old one,
 						 * we should merge them into the new one's.
 						 * and it'll be done on server through script updusr.php.
 						 */
 						sina.setTag(R.string.tips_associated);
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			return sina;
 		} catch (WeiboException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static void addContext(Context context) {
 		if (!mContexts.contains(context)) {
 			mContexts.add(context);
 		}
 	}
 	
 	/*
 	 * show a dialog that let user select to log in or not
 	 */
 	public static void shallWeLogin(int titleId, final Context context) {
 		addContext(context);
 		AlertDialog dlg = new AlertDialog.Builder(context)
 			.setPositiveButton(context.getString(R.string.label_letmelogin), new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					Intent intent = new Intent();
 					intent.setClass(context, RegLoginActivity.class);
 					context.startActivity(intent);
 				}
 				
 			})
 			.setNegativeButton(context.getString(R.string.label_letmebe), null)
 			.create();
 		if (titleId > 0) {
 			dlg.setTitle(context.getString(titleId));
 		}
 		dlg.show();
 	}
 	
 	/*
 	 * try to put login at background by using class AsyncTask
 	 */
 	private class AsyncLogin extends AsyncTask<Object, Object, Sina>{
 		
 		private Dialog mDlgProgress;
 		
 		public AsyncLogin() {
 			this.mDlgProgress = new Dialog(RegLoginActivity.this, R.style.Dialog_Clean);
 			mDlgProgress.setContentView(R.layout.custom_dialog_loading);
 			TextView tv = (TextView) mDlgProgress.findViewById(R.id.tvCustomDialogTitle);
 			tv.setText(R.string.label_logging);
 	        WindowManager.LayoutParams lp = mDlgProgress.getWindow().getAttributes();
 	        lp.alpha = 1.0f;
 	        mDlgProgress.getWindow().setAttributes(lp);
 	        mDlgProgress.setCancelable(false);
 		}
 
 		@Override
 		protected void onPostExecute(Sina sina) {
 			// TODO Auto-generated method stub
 			
 			if (sina != null) {
 				WeiboPage.setSina(sina);
 				
 				if (mCheckRemember.isChecked()) {
 					EntranceActivity.saveAccount(
 						mEditUsername.getText().toString(),
 						mEditPassword.getText().toString()
 					);
 				}
 				initAccountsList();
 				updateTitle(
 					R.id.ivTitleIcon, R.id.tvTitleName,
 					WeiboPage.getSina() == null ? null : WeiboPage.getSina().getLoggedInUser()
 				);
 				
 				if (sina.getTag() != null) {
 					if ((Integer)sina.getTag() == R.string.tips_associated) {
 						Toast.makeText(
 							RegLoginActivity.this,
 							getString(R.string.tips_loggedin)
 								+ "\n"
 								+ getString(R.string.tips_associated),
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						Toast.makeText(
 							RegLoginActivity.this,
 							R.string.tips_loggedin,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				} else {
 					Toast.makeText(
 						RegLoginActivity.this,
 						R.string.tips_loggedin,
 						Toast.LENGTH_LONG
 					).show();
 				}
 				finish();
 			} else {
 				Toast.makeText(
 					RegLoginActivity.this, 
 					R.string.tips_loginfailed, 
 					Toast.LENGTH_LONG
 				).show();
 				if (WeiboPage.getSina() != null) {
 					WeiboPage.getSina().setLoggedInUser(null);
 				}
 			}
 			
 			mDlgProgress.dismiss();
 			
 			super.onPostExecute(sina);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			mDlgProgress.show();
 			super.onPreExecute();
 		}
 
 		@Override
 		protected Sina doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 			
 			Sina sina = login(
 				mEditUsername.getText().toString(), 
 				mEditPassword.getText().toString()
 			);
 			
 			return sina;
 		}
 		
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (WeiboPage.getSina() != null && WeiboPage.getSina().isLoggedIn()) {
 				
 			} else {
 				/*
 				Toast.makeText(
 					RegLoginActivity.this,
 					R.string.tips_havetologin,
 					Toast.LENGTH_LONG
 				).show();
 				return true;
 				*/
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		menu.add(
 			Menu.NONE, 
 			Menu.FIRST + 1, 
 			1, 
 			getString(R.string.omenuitem_quit)
 		).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 		return true;//super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case Menu.FIRST + 1:
 			mIfQuit = true;
 			finish();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
