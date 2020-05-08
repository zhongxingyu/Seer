 package android.game.guessmynumber;
 
 import java.util.Random;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ResultActivity extends Activity {
 
 	String []result = new String[4];
 	TextView message;
 	String cardMode;
 	MaintainDatabase maintainDB;
 	DisplayResult displayResult;
 	String imageName[] = new String[2];
 	Music music;
 	Settings setting;
 	Toast toast ;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_result);
 		message = (TextView)findViewById(R.id.textViewResultMessage);
 		setting = new Settings(getApplicationContext());
 		cardMode = setting.getCardMode();
 		setbackground();
 		Bundle extras = getIntent().getExtras();
 	    if (extras != null) {
 	    	result = extras.getStringArray("result");
 	    }
 	    message.setText("The Secret Number is " + result[0]);
 		if(cardMode.equals("0")){
 			 Random random = new Random();
 			 int randomNum = random.nextInt(10);
 			 if(randomNum > 7){
 				 result[0] = Integer.toString(Integer.parseInt(result[0]) + randomNum);
 			 }
 		}
 		else{
 	    	if(result[1] == null || !result[1].toString().equals(result[0].toString())){
 	    		
 	    		String msg = "Try Again";
 	    		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
 	    		toast.show();
 	    	 	if(setting.getMusic() == true){
 	    	 		music = new Music("fail", getApplicationContext());
 	    	 		music.Start();
 	    	 	}
 	    		
 	    	}
 	    	else{
 	    		String msg = "Congratulations";
 	    		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
 	    		toast.show();
 
 	    	 	if(setting.getMusic() == true){
 	    	 		music = new Music("win", getApplicationContext());
 	    	 		music.Start();
 	    	 	}
 	    		//Insert into Database
 	    	    maintainDB = new MaintainDatabase(getApplicationContext(), result[4], result[2].toString()
 	    	    													, result[3].toString());
 	    	    maintainDB.insertToDatabase();
 	    		
 	    	}
 		}
 	}
 	
 	@Override
 	/**Diaplay back button only**/
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub		
 		switch(item.getItemId()){
 		case android.R.id.home:
 			//Intent upIntent = new Intent(this, MainActivity.class);
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
		if(setting.getMusic() == true){
 			music.Stop();
 		}
 	}
 	
 	public void setbackground(){
 		DisplayResult displayResult = new DisplayResult();
  	    String imageName = displayResult.returnbackResultgroundName();
  	    int id1 = getResources().getIdentifier(
  	    		imageName, "drawable", getPackageName() );
 
  	    LinearLayout layout = (LinearLayout)findViewById(R.id.resultBackground);
  	    layout.setBackgroundResource(id1);
 	}
 }
