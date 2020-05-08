 /**
  * 
  */
 package com.sjsu.siquoia;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.Spinner;
 
 /**
  * @author Parnit Sainion
  *
  */
 public class NewQuizActivity extends Activity {
 	
 	//Declare variables
 	private ArrayList<String> subjects, topics, subtopics;
 	private Spinner subjectSpinner, topicSpinner, subtopicSpinner;
 	private ArrayAdapter <String> subjectAdapter, topicAdapter, subtopicAdapter ;
	private Button createButton;
 	private NewQuizActivity newQuizActivity = this;
 	private ProgressDialog progressBar;
 	private final String SUBJECT = "subject";
 	private final String TOPIC = "topic";
 	private final String SUBTOPIC ="subtopic";
 	
 	//url to get data from database
 	private final String  SUBJECT_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getSubject.php";
 	private final String  TOPIC_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getTopic.php";
 	private final String  SUBTOPIC_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getSubtopic.php";
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);		
 		//set view
 		setContentView(R.layout.new_quiz_layout);
 		
 		subjects = new ArrayList<String>();
 		topics = new ArrayList<String>();
 		subtopics = new ArrayList<String>();
 		
		//get different spinners and create button
 		subjectSpinner= (Spinner) findViewById(R.id.subjectSpinner);
 		topicSpinner= (Spinner) findViewById(R.id.topicSpinner);
 		subtopicSpinner= (Spinner) findViewById(R.id.subtopicSpinner);
		createButton = (Button) findViewById(R.id.createQuizButton);
 		
 		//add initial values to arrays
 		new SiQuoiaGetInfoTask().execute(SUBJECT);
 		
 		//set listeners for  spinners
		setItemChangeListeners();
		createButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//String subject = subjectSpinner.getSelectedItem().toString();
				//String topic = topicSpinner.getSelectedItem().toString();
				//String subtopic = subtopicSpinner.getSelectedItem().toString();		
			}
			
		});
 	}
 
 	
 	/**
 	 * Set on ItemChangeListener for three spinners
 	 */
 	public void setItemChangeListeners()
 	{
 		subjectSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				String subject = subjectSpinner.getSelectedItem().toString();
 				if(!subject.equalsIgnoreCase("All"))
 				{
 					new SiQuoiaGetInfoTask().execute(TOPIC,subject);
 				}	
 				else
 				{
 					//set adapters topics
 					topics.clear();
 					topics.add("All");
 					topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 					topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					topicSpinner.setAdapter(topicAdapter);
 					
 					//set adapters subtopic
 					subtopics.clear();
 					subtopics.add("All");
 					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subtopicSpinner.setAdapter(subtopicAdapter);		
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}	
 		});
 		
 		topicSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				String topic = topicSpinner.getSelectedItem().toString();
 				if(!topic.equalsIgnoreCase("All"))
 				{
 					new SiQuoiaGetInfoTask().execute(SUBTOPIC,subjectSpinner.getSelectedItem().toString(),topic);
				}		
				else
				{
					//set adapters subtopic
					subtopics.clear();
					subtopics.add("All");
					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					subtopicSpinner.setAdapter(subtopicAdapter);	
				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}			
 		});
 		
 		subtopicSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}			
 		});
 	}
 	
 	public String getSubject()
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(SUBJECT_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair("",""));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			//HttpResponse response = httpclient.execute(httppost);
 			
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
     	return message;
     }
 	
 	public String getTopic(String subject)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(TOPIC_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair(SUBJECT,subject));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			//HttpResponse response = httpclient.execute(httppost);
 			
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
     	return message;
     }
 	
 	public String getSubtopic(String subject, String topic)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(SUBTOPIC_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair(SUBJECT,subject));
         	data.add(new BasicNameValuePair(TOPIC,topic));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			//HttpResponse response = httpclient.execute(httppost);
 			
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
     	return message;
     }
 	
     /**
      * This is the background task that will get either the subject,topic or sub-topics from the database.
      * @author Parnit Sainion
      *
      */
     class SiQuoiaGetInfoTask extends AsyncTask<String, String, String>
     {
     	String task;
     	@Override
 		protected void onPreExecute() {
 			//create the progress dialog and display it
     		progressBar = new ProgressDialog(NewQuizActivity.this);
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setMessage("Please Wait");
 			progressBar.show();			
 		}
     	
     	@Override
 		protected String doInBackground(String... input) {
     		task = input[0];
     		if(task.equalsIgnoreCase(SUBJECT))
     			return getSubject();
     		else if(task.equalsIgnoreCase(TOPIC))
     			return getTopic(input[1]);
     		else
     			return getSubtopic(input[1],input[2]);
 		}
 		
 		protected void onPostExecute(String result) {
 		
 				//get preferences
 				//SharedPreferences preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 				
 				//update user info
 				//SharedPreferences.Editor perferenceUpdater = preferences.edit();
 				
 				//commit preference changes
 				//perferenceUpdater.commit();
 
     		System.out.println(result);
 				if(task.equalsIgnoreCase(SUBJECT))
     			{
 					//set subject topics
 					subjects = SiQuoiaJSONParser.parseSubject(result);
 					subjectAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subjects);
 					subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subjectSpinner.setAdapter(subjectAdapter);
 					
 
 					//set adapters topics
 					topics.clear();
 					topics.add("All");
 					topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 					topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					topicSpinner.setAdapter(topicAdapter);
 					
 					//set adapters subtopic
 					subtopics.clear();
 					subtopics.add("All");
 					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subtopicSpinner.setAdapter(subtopicAdapter);					
     			}
 	    		else if(task.equalsIgnoreCase(TOPIC))
 	    		{
 	    			topics = SiQuoiaJSONParser.parseTopics(result);
 	    			topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 	    			topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			topicSpinner.setAdapter(topicAdapter);
 	    			
 	    			subtopics.clear();
 	    			subtopics.add("All");
 	    			subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 	    			subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			subtopicSpinner.setAdapter(subtopicAdapter);
 	    		}
 	    		else
 	    		{
 	    			subtopics = SiQuoiaJSONParser.parseSubtopics(result);
 	    			subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 	    			subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			subtopicSpinner.setAdapter(subtopicAdapter);
 	    		}
 			
 				//close progress dialog
 				progressBar.dismiss();
 		}    	
     }
 }
