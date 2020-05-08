 package pl.byd.promand.Team2;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.promand.Team2.R;
 import pl.byd.promand.Team2.otherActivities.MainActivity;
 
 public class MyActivity extends SherlockActivity {
    final String PASSWORD = "12345";
    final String LOGIN = "PHYSCOMP";
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
     }
 
     public void btn_login_click(View v){
         EditText et_login = (EditText)findViewById(R.id.et_login);
         String temp_login = et_login.getText().toString();
         EditText et_pass = (EditText)findViewById(R.id.et_pass);
         String temp_pass = et_pass.getText().toString();
         if(!PASSWORD.equals(temp_pass) || !LOGIN.equals(temp_login)){
             Toast.makeText(this,"Incorrect login or password!",1000).show();
         }
         else{
            Intent intent = new Intent(this, MainActivity.class);
            MyActivity.this.startActivity(intent);
         }
     }
 
 
 }
