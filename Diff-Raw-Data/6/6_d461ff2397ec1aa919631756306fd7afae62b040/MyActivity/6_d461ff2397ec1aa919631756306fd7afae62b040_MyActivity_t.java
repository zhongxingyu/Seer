 package com.example.Krakdroid;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import com.futuresimple.krakdroid.KrakDroidContract;
 
 public class MyActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Uri newSolution = getContentResolver().insert(KrakDroidContract.Solutions.CONTENT_URI, new ContentValues());
         String lastSegment = newSolution.getLastPathSegment();
         long solutionId = Long.parseLong(lastSegment);
 
         Cursor query = getContentResolver().query(KrakDroidContract.DataSets.CONTENT_URI, null, null, null, null);
         String[] names = query.getColumnNames();
         query.moveToFirst();
         do {
             int id = query.getInt(0);
             int value = query.getInt(1);
 
             String answer = "";
             if (value % 3 == 0) {
                 answer = answer.concat("Krak");
             }
             if (value % 5 == 0) {
                 answer = answer.concat("Droid");
             }
             if (value % 3 != 0 && value % 5 != 0)
             {
                 answer = String.valueOf(value);
             }
 
 
             ContentValues values = new ContentValues();
             values.put(KrakDroidContract.Answers.DATASET_ID, id);
             values.put(KrakDroidContract.Answers.SOLUTION_ID, solutionId);
             values.put(KrakDroidContract.Answers.VALUE, answer);
 
             getContentResolver().insert(newSolution, values);
 
         } while(query.moveToNext());
 
 
         ContentValues v = new ContentValues();
         v.put(KrakDroidContract.Submissions.SOLUTION_ID, solutionId);
 
 
        //Uri submission = KrakDroidContract.Submissions.buildSubmissionUri(solutionId);
        Uri submitted = getContentResolver().insert(KrakDroidContract.Submissions.CONTENT_URI, v);
     }
 }
