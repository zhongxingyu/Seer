 package com.placella.socialconnections;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class Activity_Login extends Activity {
 	Activity self = this;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 				
 		Button logInBtn = (Button) findViewById(R.id.LogInBtn);
 		logInBtn.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				new Thread(
 					new Runnable() {
 						public void run() {
 						    EditText username = (EditText) findViewById(R.id.userET);
 						    EditText password = (EditText) findViewById(R.id.passET);
 						    final boolean success = RemoteAuth.login(
 						    	self,
 						    	CONFIG.webUrl + "remote.php",
 						    	username.getText().toString(),
 						    	password.getText().toString()
 						    );
 							self.runOnUiThread(new Runnable() {
 								public void run() {
 								    if (! success) {
 								    	new Dialog(self, RemoteAuth.getResponse()).show();
 								    } else {
 								    	int accessLevel = RemoteAuth.getAccessLevel();
 								        Bundle b = new Bundle();
 								    	String token = RemoteAuth.getToken();
 								        b.putString("token", token);
 								        b.putInt("accesslevel", accessLevel);
 								        Intent intent;
 								    	if (accessLevel == ACCESSLEVEL.STUDENT) {
 									        intent = new Intent(self, Activity_Main.class);
 									        intent.putExtras(b);
								    		startActivity(intent);
 								    	} else if (accessLevel == ACCESSLEVEL.LECTURER) {
 									        intent = new Intent(self, Activity_Main.class);
 									        intent.putExtras(b);
								    		startActivity(intent);
 								    	} else if (accessLevel == ACCESSLEVEL.ADMIN) {
 									    	new Dialog(self, R.string.noAdminAccess).show();
 								    	} else {
 									    	new Dialog(self, R.string.unknowAuthError).show();
 								    	}
 								    }
 								}
 							});
 						}
 					}
 				).start();
 			}
 		});
 	}
 
     /**
      * Creates the menu from XML file
      */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}

	/**
 	 * Called whenever an item in the options menu is selected.
      *
 	 * @param item The menu item that was selected
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_openbrowser) {
 			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CONFIG.webUrl)));
         }
         return super.onOptionsItemSelected(item);
     }
 }
