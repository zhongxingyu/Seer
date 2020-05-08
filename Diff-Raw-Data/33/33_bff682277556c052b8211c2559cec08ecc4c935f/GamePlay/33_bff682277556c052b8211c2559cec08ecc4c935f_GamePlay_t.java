 package jbs.org.MemoryMap;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class GamePlay extends Activity implements OnClickListener{
     
 	level_one state = new level_one(); //creates new level
 	LinkedList<String> questions = state.getQuestions(); //creates list of questions
 	LinkedList<String> answers = state.getAnswers(); //creates list of answers
 	HashMap<String, String> map = state.getMap(); //maps questions to answers
 	String current_question = "question"; //stores currently selected question
 	String previous_current_question = "question"; //stores previous selected question
 	int q_no = 99; //marks list position of question selected
 	int a_no = 99; //marks list position of answer selected
 	String current_answer = "answer"; //stores currently selected answer
 	int q_pressed = 0; // equals one if a question is selected
 	int a_pressed = 0; // equals one if an answer is selected
 	int errorCount = 0; // keeps track of wrongly matched pairs to play sound
 	int moves = 0; //keeps track of the total moves a player took
 	int remaining = 9; //keeps track of when the player has won
 	LinkedList<String> qs = level_one.randomizer(questions); //randomizes the question list
 	LinkedList<String> ans = level_one.randomizer(answers); //randomizes the answer list
 	
 	private final View game_questions[] = new View[9]; //creates list of question buttons
 	private final View game_answers[] = new View[9]; // creates list of answer buttons
 	
 	
 	/** Called when the activity is first created. */	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gamescreen);
         
 		findViews();
 		setBlanks();
 		setInvisible();
 		
     }
 	
 	/**
 	 * Assigns functionality to all buttons. Essentially controls the entire game.
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	public void onClick(View v) {
 		switch(v.getId()) {	
 		case R.id.q1:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(0);
 				q_no = 0;
 				((Button) game_questions[0]).setText(qs.get(0));
 			}
 			break;
 		case R.id.q2:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(1);
 				q_no = 1;
 				((Button) game_questions[1]).setText(qs.get(1));
 			}
 			break;
 		case R.id.q3:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(2);
 				q_no = 2;
 				((Button) game_questions[2]).setText(qs.get(2));
 			}
 			break;
 		case R.id.q4:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(3);
 				q_no = 3;
 				((Button) game_questions[3]).setText(qs.get(3));
 			}
 			break;
 		case R.id.q5:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(4);
 				q_no = 4;
 				((Button) game_questions[4]).setText(qs.get(4));
 			}
 			break;
 		case R.id.q6:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(5);
 				q_no = 5;
 				((Button) game_questions[5]).setText(qs.get(5));
 			}
 			break;
 		case R.id.q7:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(6);
 				q_no = 6;
 				((Button) game_questions[6]).setText(qs.get(6));
 			}
 			break;
 		case R.id.q8:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(7);
 				q_no = 7;
 				((Button) game_questions[7]).setText(qs.get(7));
 			}
 			break;
 		case R.id.q9:
 			if(q_pressed!=1){
 				q_pressed = 1;
 				a_pressed = 0;
 				current_question = qs.get(8);
 				q_no = 8;
 				((Button) game_questions[8]).setText(qs.get(8));
 			}
 			break;
 		case R.id.a1:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(0);
 				a_no = 0;
 			}
 			break;
 		case R.id.a2:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(1);
 				a_no = 1;
 			}
 			break;
 		case R.id.a3:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(2);
 				a_no = 2;
 			}
 			break;
 		case R.id.a4:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(3);
 				a_no = 3;
 			}
 			break;
 		case R.id.a5:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(4);
 				a_no = 4;
 			}
 			break;
 		case R.id.a6:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(5);
 				a_no = 5;
 			}
 			break;
 		case R.id.a7:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(6);
 				a_no = 6;
 			}
 			break;
 		case R.id.a8:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(7);
 				a_no = 7;
 			}
 			break;
 		case R.id.a9:
 			if(a_pressed!=1){
 				a_pressed = 1;
 				q_pressed = 0;
 				current_answer = ans.get(8);
 				a_no = 8;
 			}
 			break;			
 		}
 		moves++;
 		errorCount++;
 		
 		//This loop runs only if the user makes a successful match
 		if(!previous_current_question.equals(current_question) && map.get(current_question).equals(current_answer)){
 			qs.remove(q_no);
 			qs.add(q_no,"check");
 			ans.remove(a_no);
 			ans.add(a_no,"check");
 			Toast.makeText(getBaseContext(), current_answer+" is indeed in "+current_question, Toast.LENGTH_SHORT).show();
 			previous_current_question = current_question;
 			remaining = remaining-1;
 			a_pressed = 0;
 			q_pressed = 0;
 			MusicNoLoop.playMe(this, R.raw.crowdexcited);
 			errorCount = 0;
 		}
 		setInvisible();
 		Log.d("unexpected","end of on click");
 		setBlanks();
 		//setViews();
 		if(a_pressed==1){
 			((Button) game_answers[a_no]).setText(ans.get(a_no));
 		}
 		if(q_pressed==1){
 			((Button) game_questions[q_no]).setText(qs.get(q_no));
 		}
 		if(remaining==0){
 			
 			Toast.makeText(getBaseContext(), "Congratulations!!! You just won in "+moves+" moves. Do try again!", Toast.LENGTH_LONG).show();
 			finish();
 			
 		}
 		
 		//Responsible for fail sound after every wrong match
 		if(errorCount==2){
 			errorCount = 0;
 			MusicNoLoop.playMe(this, R.raw.handbrakeone);
 		}
 
 	} 
 	
 	/**
 	 * finds and assigns button views to listener
 	 */
 	private void findViews() {
 	      game_questions[0] = findViewById(R.id.q1);
 	      game_questions[0].setOnClickListener(this);
 	      game_questions[1] = findViewById(R.id.q2);
 	      game_questions[1].setOnClickListener(this);
 	      game_questions[2] = findViewById(R.id.q3);
 	      game_questions[2].setOnClickListener(this);
 	      game_questions[3] = findViewById(R.id.q4);
 	      game_questions[3].setOnClickListener(this);
 	      game_questions[4] = findViewById(R.id.q5);
 	      game_questions[4].setOnClickListener(this);
 	      game_questions[5] = findViewById(R.id.q6);
 	      game_questions[5].setOnClickListener(this);
 	      game_questions[6] = findViewById(R.id.q7);
 	      game_questions[6].setOnClickListener(this);
 	      game_questions[7] = findViewById(R.id.q8);
 	      game_questions[7].setOnClickListener(this);
 	      game_questions[8] = findViewById(R.id.q9);
 	      game_questions[8].setOnClickListener(this);
 	      game_answers[0] = findViewById(R.id.a1);
 	      game_answers[0].setOnClickListener(this);
 	      game_answers[1] = findViewById(R.id.a2);
 	      game_answers[1].setOnClickListener(this);
 	      game_answers[2] = findViewById(R.id.a3);
 	      game_answers[2].setOnClickListener(this);
 	      game_answers[3] = findViewById(R.id.a4);
 	      game_answers[3].setOnClickListener(this);
 	      game_answers[4] = findViewById(R.id.a5);
 	      game_answers[4].setOnClickListener(this);
 	      game_answers[5] = findViewById(R.id.a6);
 	      game_answers[5].setOnClickListener(this);
 	      game_answers[6] = findViewById(R.id.a7);
 	      game_answers[6].setOnClickListener(this);
 	      game_answers[7] = findViewById(R.id.a8);
 	      game_answers[7].setOnClickListener(this);
 	      game_answers[8] = findViewById(R.id.a9);
 	      game_answers[8].setOnClickListener(this);
 	}
 	
 	/**
 	 * Labels all buttons with the string they represent
 	 * Useful for debugging purposes
 	 */
 	private void setViews(){
 		for(int i=0; i<9; i++){
 			((Button) game_questions[i]).setText(qs.get(i));
 			((Button) game_answers[i]).setText(ans.get(i));
 		}
 	}
 	
 	/**
 	 * Sets all button text to blank
 	 */
 	private void setBlanks(){
 		for(int i=0; i<9; i++){
 			((Button) game_questions[i]).setText("");
 			((Button) game_answers[i]).setText("");
 		}
 	}
 	
 	/**
 	 * Makes all matched pairs invisible
 	 */
 	public void setInvisible(){
 		for(int i=0; i<9; i++){
 			if (qs.get(i).equals("check")){
 				game_questions[i].setVisibility(View.INVISIBLE);
 			}
 			if (ans.get(i).equals("check")){
 				game_answers[i].setVisibility(View.INVISIBLE);
 			}
 			/**
 			if (((Button)game_questions[i]).getText().equals("check")){
 				game_questions[i].setVisibility(View.INVISIBLE);
 			}
 			if (((Button)game_answers[i]).getText().equals("check")){
 				game_answers[i].setVisibility(View.INVISIBLE);
 			}
 			*/
 		}
 	}
 		
 	
 	/**
 	 * Overridden methods below created to control background music
 	 */
	 
	@Override
 	   //Background music is free: djpretzel, Vigilante - Short Skirts (Tifa's Theme).mp3
	  
 	   protected void onResume() {
 	      super.onResume();
 	      Music.play(this, R.raw.game);
 	   }
 
 	   
 	   @Override
 	   protected void onPause() {
 	      super.onPause();
 	      Music.stop(this);
	      MusicNoLoop.stopMe(this);
 	   }
	   
 	
 }
