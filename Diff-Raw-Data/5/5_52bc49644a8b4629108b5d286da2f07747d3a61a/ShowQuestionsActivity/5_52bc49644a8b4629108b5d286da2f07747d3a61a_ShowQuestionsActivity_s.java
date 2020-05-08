 package epfl.sweng.showquestions;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import epfl.sweng.R;
 import epfl.sweng.tasks.LoadRandomQuestion;
 
 /**
  * Activity showing a question
  */
 public class ShowQuestionsActivity extends Activity {
 	
 	/**
 	 * Method invoked at the creation of the Activity. 
 	 * Triggers the display of a random question fetched from the server.
 	 * @param 
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_questions);
         new LoadRandomQuestion(this).execute();
     }
     
     
     /**
      * Display a question on the screen
      * @param Question question The question to be displayed
      */
     public void displayQuestion(final Question question) {
     	
     	final ListView listView = (ListView) findViewById(R.id.listView);
         final TextView questionTxt = (TextView) findViewById(R.id.question);
         final Button button = (Button) findViewById(R.id.button);
         
         questionTxt.setText(question.getQuestion());
                        
         // Instantiating array adapter to populate the listView
         // Using an ArrayList instead of an Array to populate, for future modifications
         // The layout android.R.layout.simple_list_item_single_choice creates radio button for each listview item
         final ArrayList<String> listAnswers = new ArrayList<String>();
         listAnswers.addAll(Arrays.asList(question.getAnswers()));
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
         														android.R.layout.simple_list_item_single_choice,
         														listAnswers);
 
         listView.setAdapter(adapter);
         listView.setEnabled(true);
         
         button.setEnabled(false);
         
         // Instantiating a boolean array to keep track of the clicked items
         // Item #i has been clicked <=> clickedItems[i]==true
         final boolean[] clickedItems = new boolean[question.getAnswers().length];
         
         // Implementing the interaction with the user
         listView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		
         		// User clicked the right answer
         		if (position == question.getSolutionIndex()) {
         			clickedItems[position] = true;
         			listAnswers.set(position, listAnswers.get(position) + " \u2714");
         			listView.setEnabled(false);
         			button.setEnabled(true);
        		}
        		
         		// User clicked the wrong answer for first time
        		else if (!clickedItems[position]) {
         			clickedItems[position] = true;
         			listAnswers.set(position, listAnswers.get(position) + " \u2718");
         		}
         		
         		adapter.notifyDataSetChanged();
         	}
         });
     }
     
     public void nextQuestion(View currentView) {
         new LoadRandomQuestion(this).execute();
     }
 
     
 }
