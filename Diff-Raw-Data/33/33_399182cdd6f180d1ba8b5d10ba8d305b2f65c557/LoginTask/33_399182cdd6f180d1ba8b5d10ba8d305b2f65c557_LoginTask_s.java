 package com.mais.leantasks.asyncTask;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.sax.StartElementListener;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.mais.leantasks.AccountManagement;
 import com.mais.leantasks.MainActivity;
 import com.mais.leantasks.R;
 import com.mais.leantasks.http.WebAPI;
 import com.mais.leantasks.model.User;
 import com.mais.leantasks.security.Encrypt;
 import com.mais.leantasks.sql.Table;
 
 public class LoginTask extends AsyncTask<String, Integer, Void> {
 
 	private ProgressBar progressBar;
 	private TextView textInfo;
 	private Activity activity;
 
 	public LoginTask(ProgressBar progressBar, TextView textInfo, Activity activity) {
 		super();
 		this.progressBar = progressBar;
 		this.textInfo = textInfo;
 		this.activity = activity;
 	}
 
 	@Override
 	protected Void doInBackground(String... params) {
 		if (params.length < 2) {
 			publishProgress(R.string.wrong_login);
 			return null;
 		}
 
 		String login = params[0];
 		String password = Encrypt.md5(params[1]);
 
 		if(login == null || password == null)
 		{
 			publishProgress(R.string.wrong_login);
 			return null;
 		}
 		
 		try {
 			if (!WebAPI.isExists(login)) {
 				publishProgress(R.string.wrong_login);
 				return null;
 			}
 			
 			Table table = Table.getInstance(activity);
 			List<User> users = (List<User>)table.users.select("usr_name='"+ login +"' AND "+"usr_password='" + password+"'");
 						
 			if(!users.isEmpty()){
 				User user = users.get(0);
 				user.setLoggedIn(1);
 				table.users.update(user);
 				System.out.println("correct login local");
 				Intent i = new Intent(activity,
 	                    MainActivity.class);
 				AccountManagement managementActivity = (AccountManagement)activity;
 				managementActivity.changeActivity(new Intent(activity, MainActivity.class));
 			} else {
 				boolean isValidUser = WebAPI.isCorrectCredentials(login, password);
 				System.out.println(isValidUser);
 				if(isValidUser){
 					User newUser = new User();
 					newUser.setName(login);
 					newUser.setPassword(password);
 					newUser.setLoggedIn(1);
 					table.users.create(newUser);
 					AccountManagement managementActivity = (AccountManagement)activity;
 					managementActivity.changeActivity(new Intent(activity, MainActivity.class));
 				} else {
 					publishProgress(R.string.wrong_login);
					System.out.println("wrong login remote");
 					return null;
 				}
 			}
 			
 		} catch (Exception e1) {
 			publishProgress(R.string.service_problem);
 			return null;
 		}
 
 		activity.finish();
 
 		return null;
 	}
 
 	private void addUser(String login, String password) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private boolean existsUserLocally(String login, String password) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	private void markAsAuthenticated(String login) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void onPostExecute(Void result) {
 		progressBar.setVisibility(ProgressBar.INVISIBLE);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		progressBar.setVisibility(ProgressBar.VISIBLE);
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		String text = activity.getResources().getString(values[0]);
 		textInfo.setText(text);
 	}
 
 }
