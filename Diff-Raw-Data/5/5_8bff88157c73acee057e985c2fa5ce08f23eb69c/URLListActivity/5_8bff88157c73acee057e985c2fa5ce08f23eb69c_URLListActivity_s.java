 package com.pandanomic.hologoogl;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.FragmentActivity;
 import android.text.InputType;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import org.json.JSONObject;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 
 /**
  * An activity representing a list of URLs. This activity
  * has different presentations for handset and tablet-size devices. On
  * handsets, the activity presents a list of items, which when touched,
  * lead to a {@link URLDetailActivity} representing
  * item details. On tablets, the activity presents the list of items and
  * item details side-by-side using two vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link URLListFragment} and the item details
  * (if present) is a {@link URLDetailFragment}.
  * <p>
  * This activity also implements the required
  * {@link URLListFragment.Callbacks} interface
  * to listen for item selections.
  */
 public class URLListActivity extends FragmentActivity
         implements URLListFragment.Callbacks {
 
     /**
      * Whether or not the activity is in two-pane mode, i.e. running on a tablet
      * device.
      */
     private boolean mTwoPane;
     private static final int AUTHORIZATION_CODE = 1993;
     private static final int ACCOUNT_CODE = 1601;
     private AuthPreferences authPreferences;
     private AccountManager accountManager;
     private final String SCOPE = "https://www.googleapis.com/auth/urlshortener";
     private boolean loggedIn = false;
     private int APIVersion;
     private Menu optionsMenu;
     private URLListFragment listFragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_url_list);
         listFragment = ((URLListFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.url_list));
 
         if (findViewById(R.id.url_detail_container) != null) {
             // The detail container view will be present only in the
             // large-screen layouts (res/values-large and
             // res/values-sw600dp). If this view is present, then the
             // activity should be in two-pane mode.
             mTwoPane = true;
 
             // In two-pane mode, list items should be given the
             // 'activated' state when touched.
             ((URLListFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.url_list))
                     .setActivateOnItemClick(true);
         }
 
         APIVersion = Build.VERSION.SDK_INT;
 
 
         accountManager = AccountManager.get(this);
         authPreferences = new AuthPreferences(this);
         loggedIn = authPreferences.loggedIn();
         if (authPreferences.getUser() != null && authPreferences.getToken() != null) {
             // Account exists, refresh stuff and make button log out
             Toast.makeText(this, "Have token for: " + authPreferences.getToken(), Toast.LENGTH_LONG).show();
             loggedIn = true;
         } else {
             // No account, refresh only anonymous ones and leave button alone
         }
 
         // TODO: If exposing deep links into your app, handle intents here.
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.shorten_new_URL:
                 newURLDialog();
                 return true;
             case R.id.refresh_url_list:
                 refreshList();
                 return true;
             case R.id.login:
                 accountSetup();
                 return true;
             case R.id.logout:
                 logout();
                 Toast.makeText(this, "You are now logged out", Toast.LENGTH_LONG).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.action_settings:
                 return true;
             case R.id.send_feedback:
                 sendFeedback();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         this.optionsMenu = menu;
 		getMenuInflater().inflate(R.menu.urllist_menu, menu);
 
         if (loggedIn) {
             menu.findItem(R.id.login).setVisible(false);
             menu.findItem(R.id.logout).setVisible(true);
         } else {
             menu.findItem(R.id.login).setVisible(true);
             menu.findItem(R.id.logout).setVisible(false);
         }
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (loggedIn) {
             menu.findItem(R.id.login).setVisible(false);
             menu.findItem(R.id.logout).setVisible(true);
         } else {
             menu.findItem(R.id.login).setVisible(true);
             menu.findItem(R.id.logout).setVisible(false);
         }
         return super.onPrepareOptionsMenu(menu);
     }
 
     /**
      * Callback method from {@link URLListFragment.Callbacks}
      * indicating that the item with the given ID was selected.
      */
     @Override
     public void onItemSelected(String id) {
         if (mTwoPane) {
             // In two-pane mode, show the detail view in this activity by
             // adding or replacing the detail fragment using a
             // fragment transaction.
             Bundle arguments = new Bundle();
 //            arguments.putString(URLDetailFragment.ARG_ITEM_ID, id);
             arguments.putString(URLDetailFragment.ARG_URL_STRING, id);
             URLDetailFragment fragment = new URLDetailFragment();
             fragment.setArguments(arguments);
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.url_detail_container, fragment)
                     .commit();
 
         } else {
             // In single-pane mode, simply start the detail activity
             // for the selected item ID.
             if (!checkNetwork()) {
                 return;
             }
             Intent detailIntent = new Intent(this, URLDetailActivity.class);
 //            detailIntent.putExtra(URLDetailFragment.ARG_ITEM_ID, id);
             detailIntent.putExtra(URLDetailFragment.ARG_URL_STRING, id);
             startActivity(detailIntent);
         }
     }
 
 	public void accountSetup() {
 		Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[] {"com.google"}, false, null, null, null, null);
         startActivityForResult(intent, ACCOUNT_CODE);
     }
 
     private void requestToken() {
         Account userAccount = null;
         String user = authPreferences.getUser();
         for (Account account : accountManager.getAccountsByType("com.google")) {
             if (account.name.equals(user)) {
                 userAccount = account;
 
                 break;
             }
         }
 
         accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this,
                 new OnTokenAcquired(), null);
     }
 
     /**
      * call this method if your token expired, or you want to request a new
      * token for whatever reason. call requestToken() again afterwards in order
      * to get a new token.
      */
     private void invalidateToken() {
         AccountManager accountManager = AccountManager.get(this);
         accountManager.invalidateAuthToken("com.google",
                 authPreferences.getToken());
 
         authPreferences.setToken(null);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (resultCode == RESULT_OK) {
             if (requestCode == AUTHORIZATION_CODE) {
                 requestToken();
             } else if (requestCode == ACCOUNT_CODE) {
                 String accountName = data
                         .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                 authPreferences.setUser(accountName);
 
                 // invalidate old tokens which might be cached. we want a fresh
                 // one, which is guaranteed to work
                 invalidateToken();
 
                 requestToken();
             }
         }
     }
 
     private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
 
         @Override
         public void run(AccountManagerFuture<Bundle> result) {
             try {
                 Bundle bundle = result.getResult();
 
                 Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                 if (launch != null) {
                     startActivityForResult(launch, AUTHORIZATION_CODE);
                 } else {
                     String token = bundle
                             .getString(AccountManager.KEY_AUTHTOKEN);
 
                     authPreferences.setToken(token);
                     Intent intent = new Intent(URLListActivity.this, URLListActivity.class);
                     startActivity(intent);
                     finish();
 
                     // Do stuff with token
                 }
             } catch (Exception e) {
                 Log.e("OnTokenAcquired run", "Failed to acquire token");
 //                throw new RuntimeException(e);
             }
         }
     }
 
     private void logout() {
         invalidateToken();
         AccountManager accountManager = AccountManager.get(this);
         accountManager.invalidateAuthToken("com.google",
                 authPreferences.getToken());
 
         authPreferences.logout();
         loggedIn = false;
     }
 
     private void refreshList() {
         if (!checkNetwork()) {
             return;
         }
 
         JSONObject result;
         String resultURL = null;
         String longUrl = null;
         String created = null;
         String authToken = authPreferences.getToken();
         try {
             result = new GetTask(this, 2).execute(authToken).get(5, TimeUnit.SECONDS);
 
             Log.d("get", result.toString());
 
             if (result == null) {
                 Toast.makeText(this, "Error retrieving data", Toast.LENGTH_LONG).show();
                 return;
             }
 
             // TODO: Check for error
 
 //            resultURL = result.getString("id");
 //            longUrl = result.getString("longUrl");
 //            created = result.getString("created");
 
 
         } catch (InterruptedException e) {
             e.printStackTrace();
         } catch (ExecutionException e) {
             e.printStackTrace();
         } catch (TimeoutException e) {
             e.printStackTrace();
         }
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Done?");
         alert.setCancelable(true);
 //        alert.setMessage(longUrl);
         alert.show();
 //        setRefreshActionButtonState(true);
     }
 
     private void setRefreshActionButtonState(final boolean refreshing) {
         if (optionsMenu != null) {
             final MenuItem refreshItem = optionsMenu
                     .findItem(R.id.refresh_metrics);
             if (refreshItem != null) {
                 if (refreshing) {
                     refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                 } else {
                     refreshItem.setActionView(null);
                 }
             }
         }
     }
 
     private void newURLDialog() {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Shorten New URL");
         alert.setCancelable(true);
 
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
         input.setHint("Type or paste a URL here");
         alert.setView(input);
         alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 String urlToShare = input.getText().toString();
 
                 // Make sure it's not empty
                 if (urlToShare == null || urlToShare.matches("")) {
                     Toast.makeText(getBaseContext(), "Please enter a URL!", Toast.LENGTH_LONG).show();
                 }
                 else if (!Patterns.WEB_URL.matcher(urlToShare).matches()) {
                     // Validate URL pattern
                     Toast.makeText(getBaseContext(), "Please enter a valid URL!", Toast.LENGTH_LONG).show();
                 }
                 else {
                     hideKeyboard(input);
                     // Let's go get that URL!
                     // Trim any trailing spaces (sometimes keyboards will autocorrect .com with a space at the end)
                     generateShortenedURL(urlToShare.trim());
                 }
             }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 hideKeyboard(input);
             }
         });
 
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     hideKeyboard(input);
                 }
             });
         }
 
         alert.show();
 
         // Show keyboard
         input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 input.post(new Runnable() {
                     @Override
                     public void run() {
                         InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                         imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                     }
                 });
             }
         });
         input.requestFocus();
     }
 
     private void hideKeyboard(EditText input) {
         InputMethodManager imm = (InputMethodManager)getSystemService(
                 Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
     }
 
     private void generateShortenedURL(String input) {
         ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         if (!checkNetwork()) {
             return;
         }
 
         ProgressDialog dialog = new ProgressDialog(this);
         dialog.setTitle("Shortening...");
         dialog.setMessage("Please wait.");
         dialog.setIndeterminate(true);
         dialog.setCancelable(false);
         dialog.show();
 
         URLShortener shortener = new URLShortener();
         Log.d("hologoogl", "generating");
         final String resultURL = shortener.generate(input);
         if (dialog != null) {
             dialog.dismiss();
         }
         Log.d("hologoogl", "done generating");
 
         Log.d("hologoogl", "Generated " + resultURL);
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(resultURL)
                 .setCancelable(true)
                 .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         shareURL(resultURL);
                     }
         });
 
         alert.setNegativeButton("Copy", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 copyURL(resultURL);
             }
         });
 
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     listFragment.addURL(resultURL.substring(7));
                 }
             });
         }
         alert.show();
 
     }
 
     private void copyURL(String input) {
         ClipboardManager clipboard = (ClipboardManager)
                 getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
         ClipData clip = ClipData.newPlainText("Shortened URL", input);
         clipboard.setPrimaryClip(clip);
         Toast.makeText(getBaseContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
     }
 
     private void shareURL(String input) {
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("text/plain");
         intent.putExtra(Intent.EXTRA_TEXT, input);
         intent.putExtra(Intent.EXTRA_SUBJECT, "Shared from Holo Goo.gl");
         startActivity(Intent.createChooser(intent, "Share"));
     }
 
     private void sendFeedback() {
         Intent gmail = new Intent(Intent.ACTION_VIEW);
         gmail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
         gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "pandanomic@gmail.com" });
         gmail.setData(Uri.parse("pandanomic@gmail.com"));
         gmail.putExtra(Intent.EXTRA_SUBJECT, "Holo Goo.gl Feedback");
         gmail.setType("plain/text");
         startActivity(gmail);
     }
 
     private boolean checkNetwork() {
         ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         boolean airplaneMode = checkAirplaneMode();
         if (!(networkInfo != null && networkInfo.isConnected())) {
             if (airplaneMode) {
                 Toast.makeText(this, "Please disable airplane mode or turn on WiFi first!", Toast.LENGTH_LONG).show();
                 return false;
             }
             Toast.makeText(this, "Could not connect, please check your internet connection.", Toast.LENGTH_LONG).show();
             return false;
         }
 
         return true;
     }
 
     private boolean checkAirplaneMode() {
         if (APIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             return Settings.System.getInt(getBaseContext().getContentResolver(),
                     Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
         }
         else {
             return Settings.System.getInt(getBaseContext().getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) != 0;
         }
     }
 
     private void reauthorizeGoogle() {
         // if response is a 401-unauthorized
     }
 }
