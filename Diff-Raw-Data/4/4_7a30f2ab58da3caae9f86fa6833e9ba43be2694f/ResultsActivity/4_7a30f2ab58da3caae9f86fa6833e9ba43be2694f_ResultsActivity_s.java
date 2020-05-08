 package org.brianfletcher;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Facebook.DialogListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ResultsActivity extends Activity implements DialogListener, RequestListener{
 	private Facebook facebook;
 	private int correctAnswerCount;
 	private int numberOfQuestions;
 	private Quiz quiz;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.results);
 		//Intent intent = getIntent();
 		quiz = (Quiz)getApplicationContext();
 		HashMap<Integer,Bundle> levelResults = quiz.getLevelResults();
 		facebook = new Facebook(getResources().getString(R.string.facebook_key));
 		
 		/*correctAnswerCount = intent.getIntExtra("correctAnswerCount", -1);
 		String levelName = intent.getStringExtra("levelName");
 		numberOfQuestions = intent.getIntExtra("numberOfQuestions", -1);
 		
 		
 		
 		TextView scoreTitleTextView = (TextView) findViewById(R.id.score_result);
 		
 		scoreTitleTextView.setText(levelName + " score");
     	
 		TextView scoreTextView = (TextView) findViewById(R.id.score);
 		scoreTextView.setText(correctAnswerCount + " / " + numberOfQuestions);
     	*/
 		
 		String scoreText = "";
 		for (int i = 1; i < quiz.getNoOfLevels() + 1; i++){
 			Bundle levelResult = levelResults.get(i);
 			correctAnswerCount += levelResult.getInt("correctAnswerCount");
 			numberOfQuestions += levelResult.getInt("numberOfQuestions");
 			scoreText += levelResult.getString("level_name") +" : " + levelResult.getInt("correctAnswerCount") +"/"+levelResult.getInt("numberOfQuestions") + "\n";
 		}
 		scoreText += "Total : " + correctAnswerCount + " / " + numberOfQuestions;
 		
 		TextView scoreTextView = (TextView) findViewById(R.id.score);
 		scoreTextView.setText(scoreText);
 		
     	Button mainMenuButton = (Button)findViewById(R.id.mainmenu);
     	
     	mainMenuButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent intent = new Intent(ResultsActivity.this,MainMenu.class);
		    	quiz.resetQuiz();
 				startActivity(intent);
 				finish();
 			}
 		});
     	    	
     	Button facebookButton = (Button)findViewById(R.id.facebookButton);
     	final Activity me = this;
     	facebookButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				//Intent intent = new Intent(ResultsActivity.this,FacebookAuthenticate.class);
 				//startActivity(intent);
 				Log.i("ResultsActivity.java", "about to authorize");
 				facebook.authorize(me, new String[]{"publish_stream"}, (DialogListener) me);
 
 			}
 		});
 	}
 	@Override
 	protected void onPause() {
 		super.onPause();
 		//finish();
 	}
 	
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
 	
 	// Dialog listener methods
 	public void onComplete(Bundle values) {
 		Bundle parameters = new Bundle();
 		
 		parameters.putString("link", getString(R.string.app_url));
 		parameters.putString("message", "I scored " + correctAnswerCount + " / " + numberOfQuestions + " in the " + getString(R.string.app_name));
 		parameters.putString("name", getString(R.string.app_name));
 		
 		//facebook.dialog(me, "feed", parameters, (DialogListener) me);
 		AsyncFacebookRunner asynRunner = new AsyncFacebookRunner(facebook);
 		asynRunner.request("me/feed", parameters, "POST", (RequestListener) this, null );
 
 	}
 
 	public void onFacebookError(FacebookError e) {
 		System.out.println("There was a facebook error " + e.getMessage());
 	}
 
 	public void onError(DialogError e) {
 		System.out.println("onError");
 	}
 
 	public void onCancel() {
 		System.out.println("onCancel");
 	}
 	
 	public void onComplete(String response, Object state) {
 		System.out.println("onComplete");
 		
 	}
 	public void onIOException(IOException e, Object state) {
 		System.out.println("onIOException");
 		
 	}
 	public void onFileNotFoundException(FileNotFoundException e, Object state) {
 		System.out.println("onFileNotFoundException");
 		
 	}
 	public void onMalformedURLException(MalformedURLException e, Object state) {
 		System.out.println("onMalformedURLException");
 		
 	}
 	public void onFacebookError(FacebookError e, Object state) {
 		System.out.println("onFacebookError");
 	}
 }
