 package com.grahamp1.polldance ;
 
 import android.app.Activity ;
 import android.os.Bundle ;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  * Activity for the Question Selection Screen
  *
  * EF, MG
  */
 public class QuestionSelectionActivity extends Activity
 {
     private static final int QUESTIONS_XML = R.raw.questions ;
    private static final String NEWLINE = System.getProperty( "NEWLINE" ) ;
 
     private ArrayAdapter<String> _adapter ;
 
     private ArrayList<Question> _questions ;
 
     private int _selected ;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState ) ;
         setContentView( R.layout.activity_question_selection ) ;
 
         // parse xml
         _questions = importXmlQuestions( QUESTIONS_XML ) ;
 
 
         // DISPLAY
         // array of questions for display
         String[] questionsList = new String[_questions.size()] ;
         Log.wtf("a", String.valueOf(_questions.size()));
 
         int i = 0 ;
         for( Question q : _questions )
         {
             questionsList[i] = q.getText() ;
             i ++ ;
         }
 
         // load questions to ListView
         ArrayAdapter<String> adapter = new ArrayAdapter<String>( this , android.R.layout.simple_list_item_single_choice , questionsList ) ;
         ListView listView = (ListView) findViewById( R.id.qs_listView ) ;
         listView.setAdapter( adapter ) ;
 
         // response when an item is selected
         listView.setClickable( true ) ;
         listView.setChoiceMode( ListView.CHOICE_MODE_SINGLE ) ;
         _selected = listView.getSelectedItemPosition() ;
     }
 
 
     private ArrayList<Question> importXmlQuestions( int resource )
     {
         XMLParser parser = new XMLParser();
         ArrayList<Question> ret = new ArrayList<Question>();
 
         try
         {
             ret = parser.getQuestions(getXmlString(resource));
         }
         catch (Exception e)
         {}

         return ret ;
     }
 
     private String getXmlString( int resource )
     {
         InputStream stream = getResources().openRawResource(resource) ;
         InputStreamReader streamReader = new InputStreamReader(stream) ;
         BufferedReader buffRead = new BufferedReader(streamReader);
 
         StringBuilder builder  = new StringBuilder();
         String line;
 
         try
         {
             while ((line = buffRead.readLine()) != null)
             {
                 builder.append(line + NEWLINE);
             }
         }
         catch (IOException e)
         {}
 
         return builder.toString();
     }
 }
