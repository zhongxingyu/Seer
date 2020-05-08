 package dk.illution.computer.info;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Scanner;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 class SignIn extends AsyncTask<String, Void, String> {
 
 	private Activity activity;
 	private Context appContext;
 
 	public SignIn (Activity activity) {
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
 			url=new URL(preferences.getString("preference_endpoint", null) + "/login/device");
 
 			// Set parameters
 			String param = "username=" + URLEncoder.encode(params[0], "UTF-8") + "&password=" + URLEncoder.encode(params[1], "UTF-8");
 
 			// Open connection
 			connection = (HttpURLConnection)url.openConnection();
 			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
 			connection.setRequestMethod("POST");
 
 			// Set headers
 			connection.setFixedLengthStreamingMode(param.getBytes().length);
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
 			// Send request
 			PrintWriter out = new PrintWriter(connection.getOutputStream());
 			out.print(param);
 			out.close();
 
 			// Get stream
 			Scanner inStream = new Scanner(connection.getInputStream());
 
 			// Loop through stream and add get the response
 			while(inStream.hasNextLine())
 				response+=(inStream.nextLine());
 
 			if (connection.getResponseCode() != 200) {
 				return "error.statusCode." + connection.getResponseCode();
 			}
 			// Return the response
 			return response;
 		}
 		// Catch some errors
 		catch (MalformedURLException ex) {
 			return "error.malformedUrl";
 		}
 		catch (IOException ex) {
 			return "error.io";
 		}
 		catch (Exception ex) {
 			return "error.general";
 		}
 	}
 
 	protected void onPostExecute(String response) {
 		if (response != null && !response.startsWith("error")) {
 			if (ComputerInfo.parseUserTokenResponse(response, appContext)) {
 				LoginActivity.dialog.hide();
 				ComputerInfo.launchComputerList(activity);
 			} else {
 				LoginActivity.dialog.hide();
 				Toast.makeText(appContext, this.activity.getString(R.string.login_error_validation), Toast.LENGTH_LONG).show();
 			}
 		} else if (response.equals("error.io")) {
 			LoginActivity.dialog.hide();
 			Toast.makeText(appContext, this.activity.getString(R.string.login_error_connection), Toast.LENGTH_LONG).show();
 		} else if (response.startsWith("error.statusCode")) {
 			Toast.makeText(appContext, this.activity.getString(R.string.login_error_status_code) + response, Toast.LENGTH_LONG).show();
 		} else if (response.startsWith("error.malformedUrl")) {
 			Toast.makeText(appContext, this.activity.getString(R.string.login_error_malformedUrl) + response, Toast.LENGTH_LONG).show();
 		} else if (response.startsWith("error.general")) {
 			Toast.makeText(appContext, this.activity.getString(R.string.login_error_general) + response, Toast.LENGTH_LONG).show();
 		}
 		if (response.startsWith("error")) {
 			LoginActivity.dialog.hide();
 			Log.d("ComputerInfo", "An error occured in the login process: " + response);
 		}
 	}
 }
 
 public class LoginActivity extends Activity {
 
 	public static ProgressDialog dialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		final Button loginButton = (Button) findViewById(R.id.login_button);
 		final Button signUpButton = (Button) findViewById(R.id.sign_up_button);
 
 
 
 		loginButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				final TextView usernameBox = (TextView) findViewById(R.id.username_box);
 				final TextView passwordBox = (TextView) findViewById(R.id.password_box);
 				dialog = ProgressDialog.show(LoginActivity.this, "",
 						LoginActivity.this.getString(R.string.login_loading), true);
 				dialog.setCancelable(true);
 				new SignIn(LoginActivity.this).execute(usernameBox.getText().toString(),
 						passwordBox.getText().toString());
 			}
 		});
 
 		signUpButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ComputerInfo.launchComputerList(LoginActivity.this);
 			}
 		});
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			ComputerInfo.launchLoginSelect(LoginActivity.this);
 			return true;
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 }
