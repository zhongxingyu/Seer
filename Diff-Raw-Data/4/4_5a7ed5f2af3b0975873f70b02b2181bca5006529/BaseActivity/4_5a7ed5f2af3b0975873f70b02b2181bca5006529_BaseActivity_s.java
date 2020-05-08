 package com.voc4u.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.voc4u.R;
 import com.voc4u.activity.init.Init;
 import com.voc4u.controller.Word;
 import com.voc4u.controller.WordController;
 import com.voc4u.setting.CommonSetting;
 import com.voc4u.ws.AddWord;
 
 public class BaseActivity extends Activity implements OnMenuItemClickListener {
 
 	public static final int DIALOG_ADD_WORD = 101;
 	public static final int DIALOG_CONFIRM_CONTINUE_SAVE_SETTING = 103;
 	public static final int DIALOG_MUST_CHECK_AT_LEAST_ONE = 102;
 	public static final int DIALOG_PROGRESS = 104;
 	public static final int DIALOG_ADD_WORD_WARN = 105;
 	public static final int DIALOG_RESET_DB = 106;
 	public static final int DIALOG_SHOW_INFO = 107;
 	public static final int DIALOG_TTS_DATA_MISSING = 109;
 	public static final int DIALOG_EDIT_WORD = 110;
 	public static final int DIALOG_CONTEXT_MENU = 111;
 	
 	public static final String FROM_INIT = "FROM_INIT";
 
 	private MenuItem mMenuDictionary;
 	private MenuItem mSpeachSetting;
 	private MenuItem mAddWord;
 	private MenuItem mHelp;
 
 	interface OnWordAdd
 	{
 		long onWordAdd(WordController wc, String learn, String nativ);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (hasShowDictionary())
 			mMenuDictionary = menu.add(R.string.dic_menu_dictionary)
 					.setOnMenuItemClickListener(this);
 		else
 			mMenuDictionary = null;
		mSpeachSetting = menu.add(R.string.btn_menu_speech_setting)
				.setOnMenuItemClickListener(this);
 
 		mAddWord = menu.add(R.string.add_word_button)
 				.setOnMenuItemClickListener(this);
 
 		if (GetShowInfoType() != null)
 			mHelp = menu.add(R.string.dic_menu_info)
 					.setOnMenuItemClickListener(this);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	protected boolean hasShowDictionary() {
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemClick(MenuItem item) {
 		if (item == mMenuDictionary)
 			WordController.getInstance(this).showWordsMenu();
 		else if (item == mSpeachSetting)
 			onShowSpeechMenu();
 		else if (item == mAddWord)
 			showDialog(DIALOG_ADD_WORD);
 		else if (item == mHelp)
 			showDialog(DIALOG_SHOW_INFO);
 		return true;
 	}
 
 	protected void onShowSpeechMenu() {
 		ComponentName componentToLaunch = new ComponentName(
 				"com.android.settings",
 				"com.android.settings.TextToSpeechSettings");
 		Intent intent = new Intent();
 		intent.addCategory(Intent.CATEGORY_LAUNCHER);
 		intent.setComponent(componentToLaunch);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) 
 	{
 		if (id == DIALOG_ADD_WORD) 
 		{
 			final Dialog dialog = createDialogAddWord(new OnWordAdd() {
 				
 				@Override
 				public long onWordAdd(WordController wc, String learn, String nativ) {
 					long id = wc.addWordEx(
 							WordController.CUSTOM_WORD_LESSON, learn, nativ, 1, 1);
 					return id;
 				}
 			});
 			return dialog;
 		}
 		else if (id == DIALOG_ADD_WORD_WARN) 
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.add_word_title);
 			builder.setMessage(R.string.msg_must_be_fill_both_edit_field);
 			builder.setCancelable(true);
 			builder.setPositiveButton(android.R.string.ok,
 					new DialogInterface.OnClickListener() 
 			{
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 			// builder.set
 			return builder.create();
 		} else if (id == DIALOG_SHOW_INFO) 
 		{
 			return DialogInfo.create(this);
 		} else
 			return super.onCreateDialog(id);
 	}
 
 	protected Dialog createDialogAddWord(final OnWordAdd onWordAdd) {
 		// Context mContext = getApplicationContext();
 		final Dialog dialog = new Dialog(this);
 
 		// dialog.
 		dialog.setContentView(R.layout.dialog_add_word);
 		dialog.setTitle(R.string.add_word_title);
 
 		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 		lp.copyFrom(dialog.getWindow().getAttributes());
 		lp.width = WindowManager.LayoutParams.FILL_PARENT;
 		// lp.height = WindowManager.LayoutParams.FILL_PARENT;
 		// dialog.show();
 		dialog.getWindow().setAttributes(lp);
 
 		final EditText edtNative = (EditText) dialog
 				.findViewById(R.id.edtNative);
 		final EditText edtLern = (EditText) dialog
 				.findViewById(R.id.edtLearn);
 		Button btnAdd = (Button) dialog.findViewById(R.id.btnAdd);
 		btnAdd.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String nat = edtNative.getText().toString().replace(",", "|");
 				String lern = edtLern.getText().toString().replace(",", "|");
 
 				if (nat.length() < 2 || lern.length() < 2) {
 					showDialog(DIALOG_ADD_WORD_WARN);
 					return;
 				} else
 					dialog.dismiss();
 
 				WordController wc = WordController.getInstance(BaseActivity.this);
 				
 				long id = onWordAdd.onWordAdd(wc, lern, nat);
 
 				Word word = new Word(id, lern, nat, 1, 1);
 				onAddCustomWord(word);
 				
 				// add word to internet
 				new AddWord(word, wc);
 
 			}
 		});
 		return dialog;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		if (id == DIALOG_ADD_WORD) 
 		{
 			final EditText edtNative = (EditText) dialog
 					.findViewById(R.id.edtNative);
 			final EditText edtLern = (EditText) dialog
 					.findViewById(R.id.edtLearn);
 			
 			edtLern.setText("");
 			edtNative.setText("");
 		
 		} 
 		else if (id == DIALOG_SHOW_INFO) {
 			DialogInfo.setup(this, GetShowInfoType(), dialog);
 		} else
 			super.onPrepareDialog(id, dialog);
 	}
 
 	protected String GetShowInfoType() {
 		return null;
 	}
 
 	protected void onAddCustomWord(Word word) {
 		String tst = getResources().getString(R.string.toas_word_is_add,
 				word.getLern(), word.getNative());
 		Toast.makeText(BaseActivity.this, tst, Toast.LENGTH_SHORT).show();
 	}
 
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 
 		CommonSetting.restore(this);
 
 		boolean showinfo = getIntent().getBooleanExtra("showinfo", true);
 		if(showinfo)
 		{
 			String showtype = GetShowInfoType();
 			if (showtype != null && !DialogInfo.GetChecked(showtype))
 				showDialog(DIALOG_SHOW_INFO);
 			
 			getIntent().putExtra("showinfo", false);
 		}
 	}
 
 	public void onResumeSuccess() {
 
 	}
 
 	@Override
 	protected void onResume() 
 	{
 		if (CommonSetting.lernCode == null || CommonSetting.nativeCode == null) 
 		{
 			Intent init = new Intent(this, Init.class);
 			startActivity(init);
 			finish();
 		} 
 		else
 			onResumeSuccess();
 
 		super.onResume();
 	}
 
 }
