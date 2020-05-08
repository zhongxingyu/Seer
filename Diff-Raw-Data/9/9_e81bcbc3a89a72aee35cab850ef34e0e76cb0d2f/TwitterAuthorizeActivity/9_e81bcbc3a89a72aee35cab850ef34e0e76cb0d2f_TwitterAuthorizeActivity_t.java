 package orz.kassy.dts.twitter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import orz.kassy.dts.twitter.R;
     
 /**
  * Twitter で OAuthするためのアクティビティね（WebViewがでるの）    
  * @author kashimoto
  */
 public class TwitterAuthorizeActivity extends Activity implements OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		String strUrl = "";
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.twitter_authorize);
 
 		setResult(-1);
 
 		Button btn = (Button)findViewById(R.id.btnpin);
 		btn.setOnClickListener(this);
 		
 		Bundle extras = getIntent().getExtras();
 		// 認証用ページのURLを読みだすよ
 		if(extras != null) {
 			if(extras.containsKey("authurl")) {
 				strUrl = extras.getString("authurl");
 			}
 		} else {
		    Utils.showToast(this, R.string.auth_fail);
 			finish();
 		}
 		WebView webview = (WebView)findViewById(R.id.webview);
 		webview.loadUrl(strUrl);
 	}
 
 	@Override
 	public void onClick(View v) {
 		int id = v.getId();
 		
 		switch(id) {
 		case R.id.btnpin:
 			EditText edit = (EditText)findViewById(R.id.editpin);
 			String strEdit = edit.getText().toString();
 			if(strEdit.trim().length() > 0) {
 				Intent intent = new Intent();
 				intent.putExtra("pincode", edit.getText().toString());
 				setResult(0, intent);
 				finish();
 			}
 			break;
 		}
 	}
 }
