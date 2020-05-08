 package in.bizquiz.app;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.FacebookRequestError;
 import com.facebook.HttpMethod;
 import com.facebook.Request;
 import com.facebook.RequestAsyncTask;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.parse.LogInCallback;
 import com.parse.ParseException;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseUser;
 
 public class ScoreActivity extends Activity {
 
 	ProgressDialog pDialog;
 
 	int score ;
 	int finalscore=0;
 	BQParse parse;
 	String username;
     SharedPreferences sp;   
 	String category_name;
 	TextView badge;
 	int[] user_score=new int[8];
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		parse = new BQParse(ScoreActivity.this);
 		setUpParse();
 		
 		setContentView(R.layout.activity_scores);
 		badge=(TextView) findViewById(R.id.badge);
 		badge.setVisibility(View.GONE);
 		category_name=getIntent().getStringExtra("cat_name");
 		Log.d("catname",category_name);
 		
 		sp=this.getSharedPreferences("First_run", MODE_PRIVATE);
 		SharedPreferences.Editor editor = sp.edit();
 		Boolean first_attempt=sp.getBoolean(category_name+"_run", true);
 		int first_time_score=sp.getInt(category_name,-1);
 		boolean first_time_score_cat7=sp.getBoolean(Integer.toString(QuizDetails.getday()), true);
 		
 		username=sp.getString("Username", " ");
 	    
 		TextView tvScore = (TextView) findViewById(R.id.tv_score);
 		score=QuizDetails.getscore();   
 	    
 		tvScore.setText("Score : "+score);
 		Log.d("usernme",username);
 	    
 	   if((first_attempt && first_time_score==-1) || (category_name.compareToIgnoreCase(getResources().getString(R.string.Category6))==0 && first_time_score_cat7)){
 	        	
 	    	      if(QuizDetails.getscore()==QuizDetails.getmax_ques()){
	    	    	  badge.setVisibility(View.VISIBLE); 
	    	    	  badge.setText("Congratulations! You scored"+ QuizDetails.getscore()/QuizDetails.getmax_ques()+".Badge awarded."); 
 	    	      }
 	    	      	    	  	  
 	    	  	  if(category_name.compareToIgnoreCase(getResources().getString(R.string.Category6))==0){
 	    	  		  
 	    	  		  editor.putBoolean(Integer.toString(QuizDetails.getday()),false);
 	    	  		  
 	    	  		  int temp=sp.getInt("Category6", 0);
 	    	  		  if(temp==-1){
 	    	  			  temp=0;
 	    	  		  }
 	    	  		  editor.putInt(category_name,QuizDetails.getscore()+temp);
 	    	  		  QuizDetails.set_score(temp+QuizDetails.getscore());
 	    	  		  
 	    	  	  }else{
 	    	  		  
 	    	  	   editor.putInt(category_name,QuizDetails.getscore());  //stores the score of the particular category
 	    	  	  
 	    	  	  }
 	    	  	  editor.putBoolean(category_name+"_run",false);  //score will be calculated only first time
 	    	  	  editor.commit();
 	    	 
 	    	  	    user_score[0] =sp.getInt("Category1", 0);
 	    	  	    user_score[1] =sp.getInt("Category2", 0);
 	    	  	    user_score[2] =sp.getInt("Category3", 0);
 	    	  	    user_score[3] =sp.getInt("Category4", 0);
 	    	  	    user_score[4] =sp.getInt("Category5", 0);
 	    	  	    user_score[5] =sp.getInt("Category6", 0);
 	    	  	    user_score[6] =sp.getInt("Category7", 0);
 	    	  	    user_score[7] =sp.getInt("Category8", 0);
 	    		    
 	    		    for(int i=0;i<8;i++){
 	    		    	if(user_score[i]==-1){
 	    		    		    		     
 	    		    		user_score[i]=0;
 	    		    	}
 	    		    	
 	    		    	finalscore=finalscore+user_score[i];
 	    		    }
 	    		   	    		    	     	
 	  	  new Updatescore(ScoreActivity.this).execute(username,category_name,Integer.toString(QuizDetails.getscore()),
 	  			  Integer.toString(finalscore));
 	    
 	    }else{
 	    	Log.d("Attempt","SOrry Second attempt");
 	    }
 		
 		
 		
 		
 		ImageButton shareButton = (ImageButton) findViewById(R.id.bt_share);
 		shareButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				shareScoreOnFacebook(score);
 			}
 		});
 		
 		
 	}
 
 	public void setUpParse(){
 
 		parse.initBQParse();
 		parse.initializeParseFacebookUtils();
 
 	}
 	
 	public void shareScoreOnFacebook(final int score){
 		
 	    pDialog = ProgressDialog.show(
 	            ScoreActivity.this, "", "Logging in...", true);
 	    List<String> permissions = Arrays.asList("basic_info", "user_about_me",
 	            "user_relationships", "user_birthday", "user_location");
 	    
 	    ParseFacebookUtils.logIn(permissions,  ScoreActivity.this, new LogInCallback() {
 	        @Override
 	        public void done(ParseUser user, ParseException err) {
 	            pDialog.dismiss();
 	            if (user == null) {
 	                Log.d("NewsInShorts-ParseAPI",
 	                        "Uh oh. The user cancelled the Facebook login.");
 	            } else if (user.isNew()) {
 	                Log.d("NewsInShorts-ParseAPI",
 	                        "User signed up and logged in through Facebook!");
 	        	    List<String> permissions = Arrays.asList("publish_actions");
 	        		Session.NewPermissionsRequest publishAction = new Session.NewPermissionsRequest( ScoreActivity.this, permissions);
 	        		ParseFacebookUtils.getSession().requestNewPublishPermissions(publishAction);
 	        		ParseFacebookUtils.saveLatestSessionData(user);
 	        	    makeMyPost(score);
 
 	        		//showMeNews();
 	            } else {
 	                Log.d("NewsInShorts-ParseAPI",
 	                        "User logged in through Facebook!");
 	                makeMyPost(score);
 
 	                //showMeNews();
 	            }
 	        }
 	    });
 
 		
 		
 		
 	}
 	
 	public void makeMyPost(final int score){
 	try{
 		Session session = Session.getActiveSession();
 	    if (session != null){
 
 
 	        Bundle postParams = new Bundle();
 	        postParams.putString("name", "***URGENT***");
 	        postParams.putString("caption", "This is an automated status update by BizQuiz for Android");
 	        postParams.putString("description", "score is "+score);
 //	        postParams.putString("link", "http://blog.boilingstocks.com");
 	        postParams.putString("picture", "http://blog.boilingstocks.com/wp-content/uploads/2013/06/keep-calm-and-carry-on-programming-150x150.png");
 
 	        Request.Callback callback= new Request.Callback() {
 	            public void onCompleted(Response response) {
 	                JSONObject graphResponse = response
 	                                           .getGraphObject()
 	                                           .getInnerJSONObject();
 	                String postId = null;
 	                try {
 	                    postId = graphResponse.getString("id");
 	                } catch (JSONException e) {
 	                    Log.i("BizQuiz FB",
 	                        "JSON error "+ e.getMessage());
 	                }
 	                FacebookRequestError error = response.getError();
 	                if (error != null) {
 	                    Toast.makeText(ScoreActivity.this,
 	                         error.getErrorMessage(),
 	                         Toast.LENGTH_SHORT).show();
 	                    } else {
 	                        Toast.makeText(ScoreActivity.this, 
 	                             "Post Successful",
 	                             Toast.LENGTH_LONG).show();
 	                }
 	            }
 	        };
 
 	        Request request = new Request(session, "me/feed", postParams, 
 	                              HttpMethod.POST, callback);
 
 	        RequestAsyncTask task = new RequestAsyncTask(request);
 	        task.execute();
 	    }
 	}catch(Exception e){
 		e.printStackTrace();
 	}
 
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
 	}
 	    
 	
 	class Updatescore extends AsyncTask<String, Void, Boolean> {
 
 		Context context;
 		ProgressDialog pDialog;
 		JSONObject jsonObject = new JSONObject();
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 	    JSONParser jsonParser = new JSONParser();
 	    String url="http://bizquiz.in/BizQuiz/UpdateScore.php";
 	    int status;
 		
 	    
 	    public Updatescore(Context ctx) {
 			this.context=ctx;
 			pDialog= new ProgressDialog(context);
 		}
 	    
 	    @Override
 		protected void onPreExecute(){
 			pDialog.setTitle("Please Wait...");
 			pDialog.setMessage("Updating Score..");
 			pDialog.setIndeterminate(true);
 			pDialog.show();
 		}
 
 
 		@Override
 		protected Boolean doInBackground(String... str) {
 			
             Log.d("username",str[0]);
 			
 			params.add(new BasicNameValuePair("username",str[0]));
 			params.add(new BasicNameValuePair("Category_name",str[1]));
 			params.add(new BasicNameValuePair("Category_score",str[2]));
 			params.add(new BasicNameValuePair("final_score",str[3]));
 			
 			
 			jsonObject = jsonParser.makeHttpRequest(url, "GET", params);
 			
 			
 			try {
 				
 				status = jsonObject.getInt("status");
 				
                 Log.d("status",Integer.toString(status));
                                 				
 			    } catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			   }
 			
 			return true;
 		}
 	
 		protected void onPostExecute(Boolean b){
 			
 			pDialog.dismiss();
 			if(status==1)
             {
 			  Toast.makeText(context, "Score Updated", Toast.LENGTH_LONG).show();
   		   }
   		   else {
   			   Toast.makeText(context, "Please try again",
   					   Toast.LENGTH_LONG).show();
   			   	   
 		}
 		}
 	}
 	
 	@Override
     public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent back = new Intent(ScoreActivity.this,Categories.class);
 //       setIntent.addCategory(Intent.CATEGORY_HOME);
 //       setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(back);
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
    		// Inflate the menu; this adds items to the action bar if it is present.
    		getMenuInflater().inflate(R.menu.activity_home, menu);
    		return true;
    	}
         
 
       
 	public boolean onOptionsItemSelected(MenuItem item) {
 
    		switch (item.getItemId()) {
    		case R.id.menu_settings:
    			startActivity(new Intent(this, Settings.class));
    			finish();
    			return true;
    		case R.id.menu_about:
    			startActivity(new Intent(this, AboutUs.class));
    			finish();
    			return true;
    		case R.id.menu_exit:
    			Intent intent = new Intent(Intent.ACTION_MAIN);
    			intent.addCategory(Intent.CATEGORY_HOME);
    			finish();
    			startActivity(intent);
    			return true;	
    		case R.id.menu_feedback:
    			Intent intent1=new Intent(this,Feedback.class);
    			finish();
    			startActivity(intent1);
    			return true;
    		case R.id.menu_statistics:
  		     startActivity(new Intent(this,Statistics.class));
  		     finish();
  		     return true;
    		case R.id.menu_archive:
    			Intent intent3 = new Intent(this,ArchiveMonthsActivity.class);
    			finish();
    			startActivity(intent3);
    			return true;	
   		
  		     
    		default:
    			return super.onOptionsItemSelected(item);
    		}
     
        }
 }
