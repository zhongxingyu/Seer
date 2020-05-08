 package uw.cse403.nonogramfun;
 
 import android.os.Bundle;
 
 import android.app.Activity;
import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class MainActivity extends Activity {
 	private EditText myText;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         myText = (EditText) findViewById(R.id.editText1);
         
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     /** Called when the user clicks the Access button 
      * @throws SQLException */
     private void accessServer() throws SQLException {
         // Do something in response to button
    	Log.d("MainActivity","accessServer()");
     	Connection con = DriverManager.getConnection("jdbc:mysql://fdb5.biz.nf:3306", 
     			"1361466", "uwcse403nonogram");
     	PreparedStatement statement = con.prepareStatement("SELECT first_name FROM developers WHERE id = 1");
     	ResultSet result = statement.executeQuery();
     	myText.setText(result.getString("1"));
     }
     
 }
