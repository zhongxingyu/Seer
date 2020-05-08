 package ginger.connexus.activity;
 
 import ginger.connexus.util.AccountUtils;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 
 public class BaseActivity extends FragmentActivity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (!AccountUtils.isAuthenticated(this)) {
             AccountUtils.startAuthenticationFlow(this, getIntent());
         }
     }
 
 }
