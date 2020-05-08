 package com.example.buzyvoctrainer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.support.v4.app.DialogFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 /*
 public class MainActivity extends Activity {
     public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
     
     EditText editChapter;
 	TextView mainFeedback;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         editChapter = (EditText) findViewById(R.id.edit_chapter);
         mainFeedback = (TextView) findViewById(R.id.main_feedback);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void sendMessage(View view) {
         String message = editChapter.getText().toString();
         editChapter.setText("");
         try {
         	getAssets().open(message+".csv");
             Intent intent = new Intent(this, DisplayMessageActivity.class);
             intent.putExtra(EXTRA_MESSAGE, message);
             startActivity(intent);
         }
         catch( IOException e ) {
     	    mainFeedback.setText( "IOException during open: " + e.getMessage() );
         }
     }
 }
 */
 
 class LessonDisplay {
 	String lesson;
 	int counter;
 	LessonDisplay(String lesson, int counter) {
 		this.lesson = lesson;
 		this.counter = counter;
 	}
 }
 
 public class MainActivity extends FragmentActivity implements ReinitDBDialogFragment.NoticeDialogListener, OnItemClickListener
 {
     public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 
 	ListView listView;
     DatabaseHandler db;
     ArrayList<LessonDisplay> data;
 
     /** Called when the activity is first created. */
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_main);
 
     	listView = (ListView) findViewById(R.id.listView1);
 
         // This array list will be updated each time.
         data = new ArrayList<LessonDisplay>();
         data.add(new LessonDisplay("1", 1));
         data.add(new LessonDisplay("22", 2));
         data.add(new LessonDisplay("23", 3));
         data.add(new LessonDisplay("24", 2));
         data.add(new LessonDisplay("25", 1));
         data.add(new LessonDisplay("26", 2));
         data.add(new LessonDisplay("27-1", 0));
         data.add(new LessonDisplay("27-2", 1));
         data.add(new LessonDisplay("28-1", 1));
         data.add(new LessonDisplay("28-2", 1));
         data.add(new LessonDisplay("28-3", 8));
         data.add(new LessonDisplay("28-4", 4));
         data.add(new LessonDisplay("29", 1));
 /*
         ArrayAdapter<String> dataAdapter =
                 new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
   */
         LessonDisplayLayoutAdapter dataAdapter = new LessonDisplayLayoutAdapter(this, data);
         
         listView.setAdapter(dataAdapter);
         listView.setOnItemClickListener(this);
         
         db = new DatabaseHandler(this);
         MyApplication app = (MyApplication)getApplicationContext();
         app.db = db;
     }
 
     public void onItemClick(AdapterView<?> l, View v, int position, long id) {
     	Log.d("Debug:", "onItemClick invoked.");
         // Get the item that was clicked
         Object o = l.getAdapter().getItem(position);
         LessonDisplay d = (LessonDisplay)o;
         //String keyword = o.toString();
         String keyword = d.lesson;
     	Log.d("Debug:", "onItemClick keyword: " + keyword);
         
         try {
         	getAssets().open(keyword+".csv");
             Intent intent = new Intent(this, DisplayMessageActivity.class);
             intent.putExtra(EXTRA_MESSAGE, keyword);
             startActivity(intent);
         }
         catch( IOException e ) {
         	Log.d("Error:", "onItemClick: no such asset: " + keyword + ".csv");
     	    // mainFeedback.setText( "IOException during open: " + e.getMessage() );
         }
         
         // Do something when a list item is clicked
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.menu_reinit:
                 Log.d("Debug", "Reinit called.");
                 showNoticeDialog();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void reinit_db() {
     	db.onUpgrade(db.getWritableDatabase(), 0, 0);
 
     	Log.d("Insert: ", "Inserting ..");
 
     	for (LessonDisplay lesson : data) {
     		try {
    			InputStreamReader reader = new InputStreamReader(getAssets().open(lesson.lesson+".csv"), "UTF-8");
     			BufferedReader br = new BufferedReader(reader); 
     			long lesson_id = db.addLesson(new Lesson(-1, lesson.lesson));
     			String line;
     			while ((line = br.readLine()) != null) {
     				try {
     					String comps[] = line.split(";");
     					long vocable_id = db.addVocable(new Vocable(-1, comps[0], comps[1], comps[2], comps[3], comps[4] ) );
     					db.addLessonVocable(new LessonVocable(-1, lesson_id, vocable_id ) );
     				}
     				catch (ArrayIndexOutOfBoundsException a) {
     					Log.d("ArrayIndexOutOfBoundsException", "ArrayIndexOutOfBoundsException on line " + line);
     				}
     			}
     		}
     		catch (IOException localIOException) {
     			// localIOException.printStackTrace();
     			Log.d("IOException", "IOException trying to feed file into db");
     		}
 
     	}
     }
     
     public void showNoticeDialog() {
         // Create an instance of the dialog fragment and show it
     	DialogFragment dialog = new ReinitDBDialogFragment();
         dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
     }
 
     // The dialog fragment receives a reference to this Activity through the
     // Fragment.onAttach() callback, which it uses to call the following methods
     // defined by the NoticeDialogFragment.NoticeDialogListener interface
     @Override
     public void onDialogPositiveClick(DialogFragment dialog) {
         Log.d("Debug", "Performing reinit.");
     	reinit_db();
         // User touched the dialog's positive button
     }
 
     @Override
     public void onDialogNegativeClick(DialogFragment dialog) {
         Log.d("Debug", "Reinit cancelled.");
         // User touched the dialog's negative button
     }
 }
 
