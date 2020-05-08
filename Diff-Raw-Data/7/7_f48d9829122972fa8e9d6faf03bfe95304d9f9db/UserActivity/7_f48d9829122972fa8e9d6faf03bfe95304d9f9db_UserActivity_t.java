 package km81m.say_it_right.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import km81m.say_it_right.R;
 import km81m.say_it_right.logic.dao.SettingsDAO;
 import km81m.say_it_right.logic.dao.SettingsDAOImpl;
 import km81m.say_it_right.logic.dao.UserDAO;
 import km81m.say_it_right.logic.dao.UserDAOImpl;
 import km81m.say_it_right.logic.entities.User;
 
 /**
  * User: alexeydushenin
  * Date: 11/9/13
  * Time: 6:32 PM
  */
 public class UserActivity extends Activity {
 
     private UserDAO userDAO = UserDAOImpl.INSTANCE;
     private SettingsDAO settingsDAO = SettingsDAOImpl.INSTANCE;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user);
     }
 
     public void addUser(View view) {
         EditText userNameET = (EditText) findViewById(R.id.user_data);
         String userName = userNameET.getText().toString().trim();
         if (!userName.isEmpty()) {
             User user = new User();
             user.setName(userName);
             user = userDAO.save(user);
             settingsDAO.updateActiveUser(user);
             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
            finish();
         }
     }
 
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
 }
