 package tk.djcrazy.autoconnect;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 @ContentView(R.layout.activity_main)
 public class MainActivity extends RoboActivity {
 
 	public static final String USER_INFO = "user_info";
 	public static final String USER_NAME = "user_name";
 	public static final String PASSWORD = "password";
 	public static final String LAST_LOGIN_TIME = "last_login_time";
 	
 	
 	@InjectView(R.id.username)
 	private EditText userName;
 	@InjectView(R.id.password)
 	private EditText password;
 	@InjectView(R.id.sure)
 	private Button sure;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         SharedPreferences userInfo = getSharedPreferences("user_info", MODE_PRIVATE);  
         userName.setText( userInfo.getString("user_name", ""));
         password.setText( userInfo.getString("password", ""));
         sure.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String name =  userName.getText().toString().trim();
 				String pwd = password.getText().toString().trim();
 				if (name.length()<1|| pwd.length()<1) {
 					Toast.makeText(MainActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
 				} else {
 			        Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
 			        editor.putString(USER_NAME, name);
 			        editor.putString(PASSWORD, pwd);
 			        editor.commit();
 					Toast.makeText(MainActivity.this, "用户名和密码已保存", Toast.LENGTH_SHORT).show();
 					finish();
 				}
 			}
 		});
 	}
 }
