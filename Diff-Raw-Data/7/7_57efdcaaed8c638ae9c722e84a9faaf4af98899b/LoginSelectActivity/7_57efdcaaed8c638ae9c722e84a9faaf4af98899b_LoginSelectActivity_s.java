 package dk.illution.computer.info;
 
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Scanner;
 
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.accounts.OperationCanceledException;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 class GoogleAuthTokenValidator extends AsyncTask<String, Void, String> {
 
 	private Activity activity;
 	private Context appContext;
 
 	public GoogleAuthTokenValidator (Activity activity) {
 		appContext = activity.getApplicationContext();
 		this.activity = activity;
 	}
 
 	protected String doInBackground (String... params) {
 		URL url;
 		HttpURLConnection connection;
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(appContext);
 
 		try {
 			String response= "";
 
 			// Set the URL
 			url=new URL(preferences.getString("preference_endpoint", null) + "/login/device/google?access_token=" + params[0]);
 
 
 			// Open connection
 			connection=(HttpURLConnection)url.openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestMethod("GET");
 
 			// Set headers
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
 			// Send request
 			PrintWriter out = new PrintWriter(connection.getOutputStream());
 			out.close();
 
 			// Get stream
 			Scanner inStream = new Scanner(connection.getInputStream());
 
 			// Loop through stream and add get the response
 			while(inStream.hasNextLine())
 				response+=(inStream.nextLine());
 
 			// Return the response
 			return response;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	protected void onPostExecute(String response) {
 		if (response != null) {
 			if (ComputerInfo.parseUserTokenResponse(response, appContext)) {
 				ComputerInfo.launchComputerList(activity);
 			} else {
 				Toast.makeText(appContext, this.activity.getString(R.string.login_error_google_validation), Toast.LENGTH_LONG).show();
 			}
 		} else {
 			Toast.makeText(appContext, this.activity.getString(R.string.login_error_connection), Toast.LENGTH_LONG).show();
 		}
 	}
 }
 
 public class LoginSelectActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login_select);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		final Button usernameAndPasswordButton = (Button) findViewById(R.id.username_and_password_button);
 		final Button googleButton = (Button) findViewById(R.id.google_button);
 
 		usernameAndPasswordButton
 				.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						ComputerInfo.launchLogin(LoginSelectActivity.this);
 					}
 				});
 
 		googleButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				final Activity activity = LoginSelectActivity.this;
 				final Context context = activity.getApplicationContext();
 
 				AccountManager am = AccountManager.get(context);
 				final Account[] accounts = am.getAccountsByType("com.google");
 				CharSequence [] accountNames = new CharSequence[accounts.length];
 
 
 				if (accounts.length <= 0) {
 					Toast.makeText(context, LoginSelectActivity.this.getString(R.string.login_error_google_no_accounts), Toast.LENGTH_LONG).show();
 					return;
 				}
 
 				for (int i = 0; i <= accounts.length - 1; i++) {
 					accountNames[i] = accounts[i].name;
 				}
 
 				//Prepare the list dialog box
 				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 				//Set its title
 				builder.setTitle("Choose an account");
 				//Set the list items and assign with the click listener
 				builder.setItems(accountNames, new DialogInterface.OnClickListener() {
 
 					// Click listener
 					public void onClick(DialogInterface dialog, int item) {
 						//
 						AccountManager manager = AccountManager.get(activity);
 
 						manager.getAuthToken(accounts[item], "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email", null, activity, new AccountManagerCallback<Bundle>() {
 							public void run(AccountManagerFuture<Bundle> future) {
 								try {
 									// If the user has authorized your application to use the tasks API
 									// a token is available.
 									String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
 									Log.d("ComputerInfo", token);
 									new GoogleAuthTokenValidator (LoginSelectActivity.this).execute(token);
 									// Now you can use the Tasks API...
 								} catch (OperationCanceledException e) {
 									// TODO: The user has denied you access to the API, you should handle that
 								} catch (Exception e) {
 									e.printStackTrace();
 								}
 							}
 						}, null);
 
 					}
 
 				});
 
 				AlertDialog alert = builder.create();
 				//display dialog box
 				alert.show();
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_login_select, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_preferences:
 			ComputerInfo.launchPreferences(this);
 			return true;
 		case R.id.menu_about:
 			ComputerInfo.launchAbout(this);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
