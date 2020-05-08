 package pl.warsjawa.android2;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import java.net.URL;
 
 public class LoginActivity extends BaseActivity {
 
     private static final String REDIRECT_URL = "warsjawa://android2";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         WebView webView = new WebView(getApplicationContext());
         webView.setWebViewClient(new WebViewClient() {
             @Override
             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 if (url.startsWith(REDIRECT_URL)) {
                     handleLoggedOn(url);
                     return true;
                 }
                 return super.shouldOverrideUrlLoading(view, url);
             }
         });
         setContentView(webView);
         webView.loadUrl("https://secure.meetup.com/oauth2/authorize"
                 + "?client_id=a7r6qufa5utub7l5m1eva4hsaa"
                 + "&response_type=token"
                 + "&redirect_uri=" + REDIRECT_URL
                 + "&set_mobile=on");
     }
 
     private void handleLoggedOn(String url) {
         String token = getToken(url);
        if (token != null) {
            new PreferenceManager(this).saveToken(token);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
         finish();
     }
 
     private String getToken(String url) {
         String fragment = Uri.parse(url).getFragment();
         for (String info : fragment.split("&")) {
             if (info.startsWith("access_token=")) {
                 return info.substring("access_token=".length());
             }
         }
         return null;
     }
 }
