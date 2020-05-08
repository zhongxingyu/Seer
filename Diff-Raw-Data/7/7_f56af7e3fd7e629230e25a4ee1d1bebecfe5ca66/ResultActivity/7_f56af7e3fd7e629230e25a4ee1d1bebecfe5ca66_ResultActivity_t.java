 package com.education.flashmath;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.codepath.oauth.OAuthLoginActivity;
 import com.education.flashmath.models.Question;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphViewStyle;
 import com.jjoe64.graphview.LineGraphView;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class ResultActivity extends OAuthLoginActivity<TwitterClient> {
 
 	private ArrayList<Question> resultList;
 	private int score = 0;
 	private String subject;
 	private LinearLayout llStats;
 	private TextView tvScore;
 	private TextView tvRank;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_result);
 		llStats = (LinearLayout) findViewById(R.id.llStats);
 		tvScore = (TextView) findViewById(R.id.tvScore);
 		tvRank = (TextView) findViewById(R.id.tvRank);
 		if (savedInstanceState == null) {
 			resultList = (ArrayList<Question>) getIntent().getSerializableExtra("QUESTIONS_ANSWERED");
 			subject = getIntent().getStringExtra("subject");
 			evaluate();	
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.result, menu);
 		return true;
 	}
 
 	
 	public void evaluate(){
 		//if (resultList != null) {
 		for(int i = 0; i < resultList.size(); i++){
 			String correctAnswer = resultList.get(i).getCorrectAnswer();
 			if (resultList.get(i).getUserAnswer().equals(correctAnswer)){
 				score++;
 			}
 		}
 		AsyncHttpClient client = new AsyncHttpClient();
 		client.get("http://flashmathapi.herokuapp.com/scores/" + subject + "/" + String.valueOf(score) + "/",
 				new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonScores) {
 				GraphViewData[] data = new GraphViewData[jsonScores.length()];
 				for (int i = 0; i < jsonScores.length(); i++) {
 					try {
 						data[i] = (new GraphViewData(i + 1, jsonScores.getJSONObject(i).getInt("value")));
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				GraphView graphView = new LineGraphView(ResultActivity.this, "Scores for " + subject);
 				GraphViewStyle style = new GraphViewStyle();
 				style.setVerticalLabelsColor(Color.BLACK);
 				style.setHorizontalLabelsColor(Color.BLACK);
 				graphView.addSeries(new GraphViewSeries("Scores", null, data));
 				graphView.setGraphViewStyle(style);
 				llStats.addView(graphView); 
 				tvScore.setText(score + " / " + resultList.size());
 				tvRank.setText("1st");
 			}
 		});
 	}
 	
 	public void tweetScore(View v) {
 		if (!getClient().isAuthenticated()) {
 			getClient().connect();
 		} else {
 			tweet();
 		}
 	}
 	
 	//action bar menu icon
 	public void onMenu(MenuItem mi){
 
 	}
 
 	//view Answers
 	public void onViewAnswers(View v){
 		//AnswersActivity
 	}
 
 	//goes back to the first questions
 	public void onTryAgain(View v){
 		Intent i = new Intent(this, QuestionActivity.class);
 		startActivity(i);
 	}
 	
 	private void tweet() {
 		getClient().sendTweet(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject object) {
 				Toast.makeText(ResultActivity.this, "Sent tweet!", Toast.LENGTH_SHORT).show();
 			}
 			
 			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(e, errorResponse);
 				e.printStackTrace();
				Toast.makeText(ResultActivity.this, errorResponse.toString(), Toast.LENGTH_SHORT).show();
 			}
 		}, "Wassssup everyone");
 	}
 
 	@Override
 	public void onLoginSuccess() {
 	}
 
 	@Override
 	public void onLoginFailure(Exception e) {
 		Toast.makeText(ResultActivity.this, "Error logging into Twitter!", Toast.LENGTH_SHORT).show();
 	}
 }
