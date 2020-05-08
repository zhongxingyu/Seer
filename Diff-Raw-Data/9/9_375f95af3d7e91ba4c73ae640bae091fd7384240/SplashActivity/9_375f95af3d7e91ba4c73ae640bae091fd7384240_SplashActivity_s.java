 package com.boh.flatmate;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.boh.flatmate.R;
 import com.boh.flatmate.connection.ServerConnection;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.StrictMode;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class SplashActivity extends Activity {
 
 	ServerConnection connection = new ServerConnection();
 	String FILENAME;
 	final Handler myHandler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Show the splash screen
 		setContentView(R.layout.splash);
 
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
 		StrictMode.setThreadPolicy(policy);
 		FILENAME = this.getFilesDir().getPath().toString() + "UserAuthCode.txt";
 
 	}
 
 	@Override
 	public void onStart(){
 		super.onStart();
 
 		Thread splashWait = new Thread(){
 			@Override
 			public void run(){
 				try {
 					int i = 0;
 					int showSplashFor = 20;
 					while(i < showSplashFor){
 						sleep(100);
 						i += 1;
 					}
 				} catch(InterruptedException e){
 				}finally {
 					int loggedin = userLogin();
 
 					if(loggedin == 1){
 						startApp();
 						finish();
 					}else{
 						myHandler.post(newLogin);
 					}
 				}
 			}
 		};
 		splashWait.start();
 	}
 
 	final Runnable newLogin = new Runnable() {
 		public void run() {
 			newLogin();
 		}
 	};
 
 	private int userLogin(){
 		File logFile = new File(FILENAME);
 		String authCode = "null";
 		if (logFile.exists())
 		{
 			BufferedReader input;
 			try {
 				input = new BufferedReader(new FileReader(FILENAME));
 			} catch (FileNotFoundException e1) {
 				return 0;
 			}
 			try {
 				String line = null;
 				if (( line = input.readLine()) != null){
 					authCode = line;
 				}
 			}catch (Exception e){
 
 			}
 			try {
 				input.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			System.out.println(authCode);
 			return 1;		//sucessfull login here return 1; test return 0;
 		}
 
 		return 0;
 
 	}
 
 	private void newLogin(){
 		View loginBox = findViewById(R.id.loginBox);
 		loginBox.setVisibility(View.VISIBLE);
 		AccountManager am = AccountManager.get(this);
 		Account[] accounts = am.getAccountsByType("com.google");
		Account googleAccount = accounts[0];
		String email = googleAccount.name;
 		EditText emailBox = (EditText) findViewById(R.id.emailTbox);
 
 		emailBox.setText(email);
 
 		//temp for testing ---- use above line for real
 		//emailBox.setText("adam9@bla.com");
 		//EditText passwordBox1 = (EditText) findViewById(R.id.passwordBox);
 		//passwordBox1.setText("Password");
 
 
 		Button login = (Button) findViewById(R.id.loginButton);
 		login.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				EditText emailBox2 = (EditText) findViewById(R.id.emailTbox);		
 				String emailInput = emailBox2.getText().toString();
 				EditText passwordBox1 = (EditText) findViewById(R.id.passwordBox);			
 				String passwordInput = passwordBox1.getText().toString();
 				Button login2 = (Button) findViewById(R.id.loginButton);
 				login2.setVisibility(View.GONE);
 				Button register = (Button) findViewById(R.id.registerButton);
 				register.setVisibility(View.GONE);
 				ProgressBar spinner = (ProgressBar) findViewById(R.id.loginSpinner);
 				spinner.setVisibility(View.VISIBLE);
 				new serverLogin().execute(emailInput,passwordInput);
 			}
 		});
 
 	}
 
 	private void serverLogin(String key){
 		if(key != "failed"){
 			File logFile = new File(FILENAME);
 			if (!logFile.exists())
 			{
 				try
 				{
 					logFile.createNewFile();
 				} 
 				catch (IOException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			try
 			{
 				//BufferedWriter for performance, true to set append to file flag
 				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
 				buf.append(key);
 				buf.newLine();
 				buf.close();
 			}
 			catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			startApp();
 			finish();
 		}else{
 			Button login2 = (Button) findViewById(R.id.loginButton);
 			login2.setVisibility(View.VISIBLE);
 			Button register = (Button) findViewById(R.id.registerButton);
 			register.setVisibility(View.VISIBLE);
 			ProgressBar spinner = (ProgressBar) findViewById(R.id.loginSpinner);
 			spinner.setVisibility(View.GONE);
 			Toast toast = Toast.makeText(getApplicationContext(), "Failed to Login... Please Try Again!", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 
 	private void startApp() {
 		Intent intent = new Intent(SplashActivity.this, FlatMate.class);
 		startActivity(intent);
 	}
 
 	private class serverLogin extends AsyncTask<String,Void,String>{
 
 		@Override
 		protected String doInBackground(String... details) {
 			String key = connection.login(details[0], details[1]);
 			return key;
 		}
 
 		protected void onPostExecute(String result) {
 			serverLogin(result);
 		}
 
 	}
 }
