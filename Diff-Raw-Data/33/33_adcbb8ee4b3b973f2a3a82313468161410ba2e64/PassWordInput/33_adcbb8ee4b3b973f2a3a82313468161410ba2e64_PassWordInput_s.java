 package org.geometerplus.fbreader.fbreader;
 
 import org.LXZKimage.R;
 import org.geometerplus.zlibrary.core.options.ZLStringOption;
 import org.geometerplus.zlibrary.core.resources.ZLResource;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class PassWordInput extends Dialog implements
 		android.view.View.OnClickListener {
 
 	public PassWordInput(Context context) {
 		super(context);
 	}
 
 	private EditText editText;
 	private Button btnOk;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pass_word_input);
		final ZLResource resource = ZLResource.resource("messageBoxStr");
		setTitle(resource.getResource("passwordTitle").getValue());
 		editText = (EditText) findViewById(R.id.passInput_Input);
 		btnOk = (Button) findViewById(R.id.passInput_Ok);
		btnOk.setText(resource.getResource("passwordOk").getValue());
 		btnOk.setOnClickListener(this);
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.passInput_Ok:
 			passWord.setValue(editText.getText().toString());
 		default:
 			dismiss();
 		}
 	}
 
 	private static final ZLStringOption passWord = new ZLStringOption("Verify",
 			"verifyCode", "");
 
 	public static boolean hasPassWord() {
 		if ("123456".equals(passWord.getValue())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
