 package cl.droid.transantiago;
 
 public class ServiceActivity extends Activity {
 	final Activity activity = this;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.web);
 		WebView webView = (WebView) findViewById(R.id.webview);
         webView.getSettings().setJavaScriptEnabled(true);
         String url = getIntent().getStringExtra("url");
         String params = getIntent().getStringExtra("params");
         webView.setWebChromeClient(new WebChromeClient() {
             public void onProgressChanged(WebView view, int progress)
             {
                 activity.setTitle("Loading...");
                 activity.setProgress(progress * 100);
  
                 if(progress == 100)
                     activity.setTitle(R.string.app_name);
 //                Log.i(OpenSatNavConstants.LOG_TAG, view.getUrl());
             }
         });
         
         webView.loadUrl(url);
 	}
 
 }
