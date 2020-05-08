 package com.yutao.smsforward;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	ModelView modelView;
 	EditText editText2;
 	EditText editText3;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		modelView = (ModelView) findViewById(R.id.model_view);
 		editText2 = (EditText) findViewById(R.id.editText2);
 		editText3 = (EditText) findViewById(R.id.editText3);
 
 		SharedPreferences prefs = Utils.prefs(this);
 		String strings = prefs
 				.getString(Constant.SOURCE_MOBILE_NUMBERS_KEY, "");
		String[] stringArray = strings.split("|");
 		modelView.setHint("请输入来源号码");
 		modelView.init(stringArray);
 
 		String string2 = prefs.getString(Constant.SMS_BODY_KEYWOR_KEY, "");
 		String string3 = prefs.getString(Constant.DEST_MOBILE_NUMBER_KEY, "");
 
 		editText2.setText(string2);
 		editText3.setText(string3);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		String[] stringArray = modelView.getStrings();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < stringArray.length; i++) {
 			if (i > 0) {
 				sb.append("|");
 			}
 			sb.append(stringArray[i]);
 		}
 
 		String string2 = editText2.getText().toString().trim();
 		String string3 = editText3.getText().toString().trim();
 		if (TextUtils.isEmpty(sb.toString()) || TextUtils.isEmpty(string2)
 				|| TextUtils.isEmpty(string3)) {
 			Toast.makeText(this, "设置失败!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		SharedPreferences prefs = Utils.prefs(this);
 
 		prefs.edit()
 				.putString(Constant.SOURCE_MOBILE_NUMBERS_KEY, sb.toString())
 				.putString(Constant.SMS_BODY_KEYWOR_KEY, string2)
 				.putString(Constant.DEST_MOBILE_NUMBER_KEY, string3).commit();
 		Toast.makeText(this, "设置成功!", Toast.LENGTH_SHORT).show();
 		
 	}
 }
