 /*******************************************************
  * @Title: TranslateActivity.java
  * @Package org.shangjiyu.twidere.extension.translaetor
  * @Description: TODO(用一句话描述该文件做什么)
  * @author shangjiyu
  * @date 2013-10-11 下午10:01:38
  * @version V1.0
  ********************************************************/
 
 package org.shangjiyu.twidere.extension.translator;
 
 import org.mariotaku.twidere.Twidere;
 import org.mariotaku.twidere.model.ParcelableStatus;
 import org.shangjiyu.twidere.extension.translator.R;
 import org.shangjiyu.twidere.extension.translator.BDTranslate.BDTranslateException;
 import org.shangjiyu.twidere.extension.translator.BDTranslate.BDTranslateResponse;
 import org.shangjiyu.twidere.extension.translator.MSTranslate.MSTranslateException;
 import org.shangjiyu.twidere.extension.translator.MSTranslate.MSTranslateResponse;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /********************************************************
  * @ClassName: TranslateActivity
  * @Description: TODO(显示的唯一一个activity)
  * @author shangjiyu
  * @date 2013-10-11 下午10:01:38
  */
 
 public class TranslateActivity extends Activity implements Constants,OnClickListener {	
 
 	private TextView mPreview, layoutTitle;
 	private ImageButton mActionButton, translateButton;
 	private ProgressBar mProgress;
 	private String mUser;
 	private ParcelableStatus mStatus;
 	private MSTranslateTask mMSTranslateTask;
 	private BDTranslateTask mBDTranslateTask;
 	private SharedPreferences sharedPreferences;
 	private boolean isTranslated = false;
 	private boolean isOrignal = true;
 	private String ORIGINAL_STRING = "";
 	private String TRANSLATED_STRING = "";
 	
 	public static boolean isBaiduTranslateAPI = true;
 	
 	/********************************************************
 	 *Title: onCreate
 	 *Description: 
 	 * @param savedInstanceState
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 *********************************************************/
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		final Intent intent = getIntent();
 //		final Uri data = intent.getData();
 		final String action = intent.getAction(); 
 		setContentView(R.layout.main);
 		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		if (sharedPreferences.getBoolean(getString(R.string.baidu_translate_api_checkbox), false)) {
 			isBaiduTranslateAPI = true;
 		}else if (sharedPreferences.getBoolean(getString(R.string.microsoft_translate_api_checkbox), false)) {
 			isBaiduTranslateAPI = false;
 		}
 		
 		mPreview = (TextView) findViewById(R.id.text);
 		layoutTitle = (TextView) findViewById(R.id.translat_layout_title);
 		mActionButton = (ImageButton) findViewById(R.id.action);
 		translateButton = (ImageButton) findViewById(R.id.translate);
 		mProgress = (ProgressBar) findViewById(R.id.progress);
 		TRANSLATED_STRING = savedInstanceState != null ? savedInstanceState.getString(Twidere.INTENT_KEY_TEXT) : null;
 		mUser = savedInstanceState != null ? savedInstanceState.getString(Twidere.INTENT_KEY_USER) : null;
 		translateButton.setEnabled(false);
 		if (TRANSLATED_STRING == null || mUser == null) {
 			if (Twidere.INTENT_ACTION_EXTENSION_OPEN_STATUS.equals(action)) {
 				mStatus = Twidere.getStatusFromIntent(getIntent());
 				if (mStatus == null || mStatus.text_html == null) {
 					finish();
 					return;
 				}
 				mUser = mStatus.screen_name;
 				mPreview.setText(Html.fromHtml(mStatus.text_plain));
 				ORIGINAL_STRING = mPreview.getText().toString();
 				initTranslateTask();
 			}
 		} else {
 			mPreview.setText(TRANSLATED_STRING);
 		}
 		
 	}
 
 	/********************************************************
 	 *Title: onSaveInstanceState
 	 *Description: 
 	 * @param outState
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 *********************************************************/
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		outState.putString(Twidere.INTENT_KEY_TEXT, TRANSLATED_STRING);
 		outState.putString(Twidere.INTENT_KEY_USER, mUser);
 		super.onSaveInstanceState(outState);
 	}
 	
 	/********************************************************
 	 * @Title: initTranslateTask
 	 * @Description: TODO(initial translate task)
 	 * @param     设定文件
 	 * @return void    返回类型
 	 * @throws
 	 */
 	private void initTranslateTask(){
 		if (isBaiduTranslateAPI) {
 			layoutTitle.setText(R.string.baidu_translate_layout_title);
 			BDTranslate.BDTRANSLATEKEY_STRING = sharedPreferences.getString(getString(R.string.baidu_client_id), Constants.BAIDU_CLIENT_ID);
 			if (mBDTranslateTask != null) {
 				mBDTranslateTask.cancel(true);
 			}else {
 				mBDTranslateTask = new BDTranslateTask(ORIGINAL_STRING);
 				mBDTranslateTask.execute();
 			}
 		}else {
 			layoutTitle.setText(R.string.Bing_translate_layout_title);
 			MSTranslate.CLIEND_SECRET_STRING = sharedPreferences.getString(getString(R.string.Bing_client_secret), Constants.MICROSOFT_CLIEN_SECRET);
 			if (mMSTranslateTask != null) {
 				mMSTranslateTask.cancel(true);
 			}else {
 				mMSTranslateTask = new MSTranslateTask(ORIGINAL_STRING);
 				mMSTranslateTask.execute();
 			}
 		}
 	}
 	
 	/********************************************************
 	 *Title: onClick
 	 *Description: 
 	 * @param v
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 *********************************************************/
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.action:
 			if (TRANSLATED_STRING == null) {
 				if (mStatus == null) return;
 				initTranslateTask();
 			} else {
 				if (mUser == null || TRANSLATED_STRING == null) return;
 				final Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_TEXT, "@" + mUser + ": " + TRANSLATED_STRING);
 				startActivity(Intent.createChooser(intent, getString(R.string.share)));
 			}
 			break;
 		case R.id.translate:
 			if (mPreview.getText() != null) {
 				if (isTranslated) {
 					if (isOrignal) {
 						mPreview.setText(TRANSLATED_STRING);
 						isOrignal = false;
 					}else {
 						mPreview.setText(ORIGINAL_STRING);
 						isOrignal = true;
 					}
 				}else {
 					initTranslateTask();
 				}
 			}
 			break;
 		}
 	}
 	
 	/********************************************************
 	 * @ClassName: MSTranslateTask
 	 * @Description: TODO(Bing translate api)
 	 * @author jiyu
 	 * @date 2013-10-13 下午6:35:48
 	 */
 	public final class MSTranslateTask extends AsyncTask<Void, Void, Object> {
 
 		private final String srcContent;
 
 		public MSTranslateTask(String srcContent) {
 			this.srcContent = srcContent;
 		}
 
 		@Override
 		protected Object doInBackground(Void... args) {
 			final MSTranslate translate = new MSTranslate();
 			try {
 				return translate.postTranslate(srcContent,"zh-CHS");
 			} catch (Exception e) {
 				// TODO: handle exception
 				return e;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			mProgress.setVisibility(View.GONE);
 			mActionButton.setVisibility(View.VISIBLE);
 			mActionButton.setImageResource(result instanceof MSTranslateResponse ? R.drawable.ic_menu_share
 					: R.drawable.ic_menu_send);
 			if (result instanceof MSTranslateResponse ) {
 				TRANSLATED_STRING = ((MSTranslateResponse ) result).translateResult;
 				mPreview.setText(TRANSLATED_STRING);
 				isTranslated = true;
 				isOrignal = false;
				translateButton.setEnabled(true);
 			} else if (result instanceof MSTranslateException) {
 				System.out.println(((MSTranslateException) result).getMessage());
 				Toast.makeText(TranslateActivity.this,
 						getString(R.string.error_message, ((MSTranslateException) result).getMessage()),
 						Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(TranslateActivity.this, R.string.error_unknown_error, Toast.LENGTH_LONG).show();
 			}
 			mMSTranslateTask = null;
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			Toast.makeText(TranslateActivity.this, R.string.waiting_for_bing_translated_text, Toast.LENGTH_SHORT).show();
 			super.onPreExecute();
 		}
 	}
 	
 	/********************************************************
 	 * @ClassName: BDTranslateTask
 	 * @Description: TODO(baiud translate api)
 	 * @author shangjiyu
 	 * @date 2013-10-13 下午6:35:20
 	 */
 	public final class BDTranslateTask extends AsyncTask<Void, Void, Object> {
 
 		private final String srcContent;
 
 		public BDTranslateTask(String srcContent) {
 			this.srcContent = srcContent;
 		}
 
 		@Override
 		protected Object doInBackground(Void... args) {
 			final BDTranslate translate = new BDTranslate();
 			try {
 				return translate.postTranslate(srcContent);
 			} catch (Exception e) {
 				// TODO: handle exception
 				return e;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			mProgress.setVisibility(View.GONE);
 			mActionButton.setVisibility(View.VISIBLE);
 			mActionButton.setImageResource(result instanceof BDTranslateResponse ? R.drawable.ic_menu_share
 					: R.drawable.ic_menu_send);
 			if (result instanceof BDTranslateResponse ) {
 				TRANSLATED_STRING = ((BDTranslateResponse ) result).translateResult;
 				mPreview.setText(TRANSLATED_STRING);
 				isTranslated = true;
 				isOrignal = false;
				translateButton.setEnabled(true);
 			} else if (result instanceof BDTranslateException) {
 				Toast.makeText(TranslateActivity.this,
 						getString(R.string.error_message, ((BDTranslateException) result).getMessage()),
 						Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(TranslateActivity.this, R.string.error_unknown_error, Toast.LENGTH_LONG).show();
 			}
 			mBDTranslateTask = null;
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			Toast.makeText(TranslateActivity.this, R.string.waiting_for_baidu_translated_text, Toast.LENGTH_LONG).show();
 			super.onPreExecute();
 		}
 	}
 }
 
