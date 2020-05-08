 package com.pigote.dpfinal;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.text.StaticLayout;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.pigote.dpfinal.MWReaderXmlParser.Entry;
 import com.pigote.dpfinal.db.DBHandler;
 
 public class DoRead extends AsyncTask<String, String, String>{
 	
 	private TextView translated;
 	
 	@Override
 	protected String doInBackground(String... params) {
 		// params comes from the execute() call: params[0] is the url.
         try {
             return goRead(params[0]);
         } catch (Exception e) {
         	//toastMsg("Unable to connect to reading service");
         	Log.d("myDebug", "goRead Exception! " + e.getMessage());
             return null;
         }
 	}
 	
     // onPostExecute displays the results of the AsyncTask.
     @Override
     protected void onPostExecute(String result) {
         if (result!=null){
         	//make array from db of audio files in local storage
         	//play file array
         	Activity activity = DPfinal.getActivity();
         	translated = (TextView) activity.findViewById(R.id.translatedText);
         	translated.setText("Array played");
         }
     }
    
      // Do the web service tango here
     private String goRead(String toRead) throws Exception {
     	
     	List<Entry> entries = new ArrayList<Entry>();
     	// Query local db, fill array of missing words
     	String[] missing = DPfinal.getDBHandler().getMissingWords(toRead);
     	// go online, find missing words and add to db
     	InputStream is = null;
     	MWReaderXmlParser xmlParser = new MWReaderXmlParser();
     	
     	final String pre = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
     	final String post = "?key=df7d3120-f7c0-4150-bcf4-93e04f72f6db";
         	        
         for (int i = 0; i<missing.length; i++){
         	if (!missing[i].equals("*")){
 	        	try {
 	        		URL url = new URL(pre+missing[i].toLowerCase(Locale.ENGLISH)+post);
 	        		//URL url = new URL(pre+"the"+post);
 	        		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	        		conn.setReadTimeout(10000 /* milliseconds */);
 		            conn.setConnectTimeout(15000 /* milliseconds */);
 		            conn.setRequestMethod("GET");
 		            conn.setDoInput(true);
 		            // Starts the query
 		            conn.connect();
 		            int response = conn.getResponseCode();
 		            Log.d("myDebug", "Trying to get : " + missing[i].toLowerCase(Locale.ENGLISH));
 		            if (response==200){
 		            	is = conn.getInputStream();
 			            entries.add(xmlParser.parse(is));
 			            entries.get(i).addWord(missing[i].toLowerCase(Locale.ENGLISH));
 		            } else {
 		            	Activity activity = DPfinal.getActivity();
 		            	translated = (TextView) activity.findViewById(R.id.translatedText);
 		            	translated.setText("Connection error" + response);
 		            }
 		            conn.disconnect();
 		            
 		         } finally {
 		             if (is != null) {
 		                 is.close();
 		             } 
 		         }
         	}
        } 
         //go over entries, store in db
       for (int i =0; i<entries.size(); i++){
        	DPfinal.getDBHandler().addEntry(entries.get(i));
         }
         return toRead;
      }
 }
