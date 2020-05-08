 package edu.auburn.csse.comp3710;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import edu.auburn.csse.comp3710.R;
 import edu.auburn.csse.comp3710.DataHelper.QuestionTypes;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Questions extends Activity {
 	int score;
 	QuestionTypes topic;
 	public DataHelper QuestionDB;
 	int difficulty = 1;
 	int QuestionCount = 1;
 	Question currentQuestion = null;
 	
 	/* Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("onCreate", "Called");
         
         Bundle extras = getIntent().getExtras();
         difficulty = extras.getInt("difficulty") + 1;
         
         // "Any", "Engineering", "Sports", "General"
         switch (extras.getInt("topic")) {
         case 0:
         	topic = QuestionTypes.Any;
         	break;
         case 1:
         	topic = QuestionTypes.Sports;
         	break;
        case 2:
         	topic = QuestionTypes.General;
         	break;
         }
         
         Context context = getApplicationContext();
         CharSequence text = "Difficulty: " + Integer.toString(difficulty);
         int duration = Toast.LENGTH_SHORT;
 
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
         
         // initialize db
         QuestionDB = new DataHelper(this);
         setContentView(R.layout.questions);
         nextQuestion(); 
         
         score = 0;  
         
         //set up lifeline listners
         
         Button HintButtom = (Button)findViewById(R.id.HintButton);
         HintButtom.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TextView hintText = (TextView)findViewById(R.id.hint);
 				hintText.setVisibility(hintText.VISIBLE);
 				Button hintButton = (Button)findViewById(R.id.HintButton);
 				hintButton.setVisibility(hintButton.INVISIBLE);
 			}
 		});
         
         Button FiftyFiftyButton = (Button)findViewById(R.id.FiftyFiftyButton);
         FiftyFiftyButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Button answer1 = (Button)findViewById(R.id.Answer1);
 				Button answer2 = (Button)findViewById(R.id.Answer2);
 				Button answer3 = (Button)findViewById(R.id.Answer3);
 				Button answer4 = (Button)findViewById(R.id.Answer4);
 				int deleted = 0;
 				
 				if(answer1.getText() != currentQuestion.getCorrectAnswer() && deleted != 2)
 				{
 					answer1.setVisibility(answer1.INVISIBLE);
 					deleted++;
 				}
 				
 				if(answer2.getText() != currentQuestion.getCorrectAnswer() && deleted != 2)
 				{
 					answer2.setVisibility(answer2.INVISIBLE);
 					deleted++;
 				}
 				
 				if(answer3.getText() != currentQuestion.getCorrectAnswer() && deleted != 2)
 				{
 					answer3.setVisibility(answer3.INVISIBLE);
 					deleted++;
 				}
 				
 				if(answer4.getText() != currentQuestion.getCorrectAnswer() && deleted != 2)
 				{
 					answer4.setVisibility(answer4.INVISIBLE);
 					deleted++;
 				}
 				
 				Button FiftyFiftyBtn = (Button)findViewById(R.id.FiftyFiftyButton);
 				FiftyFiftyBtn.setVisibility(FiftyFiftyBtn.INVISIBLE);
 			}		
 		});
         
         
         Button ButtonCram = (Button)findViewById(R.id.CramButton);
         ButtonCram.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				Button answer1 = (Button)findViewById(R.id.Answer1);
 				Button answer2 = (Button)findViewById(R.id.Answer2);
 				Button answer3 = (Button)findViewById(R.id.Answer3);
 				Button answer4 = (Button)findViewById(R.id.Answer4);
 				
 				answer1.setVisibility(answer1.INVISIBLE);
 				answer2.setVisibility(answer2.INVISIBLE);
 				answer3.setVisibility(answer3.INVISIBLE);
 				answer4.setVisibility(answer4.INVISIBLE);
 				
 				
 				//random number between 0-3
 				Random generator = new Random();
 				int generatedRandom = generator.nextInt(100);
 				
 				if(answer1.getText() == currentQuestion.getCorrectAnswer())
 				{					
 					if(generatedRandom < 75)
 					{
 						answer1.setVisibility(answer1.VISIBLE);
 					}
 					else
 					{
 						answer2.setVisibility(answer2.VISIBLE);
 					}
 					
 				}
 				
 				else if(answer2.getText() == currentQuestion.getCorrectAnswer())
 				{
 					if(generatedRandom < 75)
 					{
 						answer2.setVisibility(answer2.VISIBLE);
 					}
 					else
 					{
 						answer3.setVisibility(answer3.VISIBLE);
 					}
 					
 				}
 				
 				else if(answer3.getText() == currentQuestion.getCorrectAnswer())
 				{
 					if(generatedRandom < 75)
 					{
 						answer3.setVisibility(answer3.VISIBLE);
 					}
 					else
 					{
 						answer4.setVisibility(answer4.VISIBLE);
 					}
 					
 				}
 				
 				else if(answer4.getText() == currentQuestion.getCorrectAnswer())
 				{
 					if(generatedRandom < 75)
 					{
 						answer4.setVisibility(answer4.VISIBLE);
 					}
 					else
 					{
 						answer2.setVisibility(answer2.VISIBLE);
 					}
 				
 				}
 				
 				Button CramButton = (Button)findViewById(R.id.CramButton);
 				CramButton.setVisibility(CramButton.INVISIBLE);
 				
 			}
 		});
         
     }
     
     public void setCurrentQuestion(int difficulty) {
     	currentQuestion = new Question(QuestionDB, difficulty, topic);
     	String[] WrongAnswers = currentQuestion.getWrongAnswers();
     	//TODO: Dynamically change order so first answer is not always the correct answer 
     	try {
 	    	TextView question = (TextView)findViewById(R.id.Question);
 	    	question.setText(currentQuestion.getQuestion());
 	    	
 	    	TextView hint = (TextView)findViewById(R.id.hint);
 	    	hint.setText("HINT: " + currentQuestion.getHint());
 	    	hint.setVisibility(hint.INVISIBLE);
 	    	
 	    	//Always set all buttons as visible incase there has been a 50/50 call
 	    		    	
 	    	ArrayList<Button> list = new ArrayList<Button>(4);
 	    	list.add((Button)findViewById(R.id.Answer1));
 	    	list.add((Button)findViewById(R.id.Answer2));
 	    	list.add((Button)findViewById(R.id.Answer3));
 	    	list.add((Button)findViewById(R.id.Answer4));
 
 	    	Collections.shuffle(list);
 	    	
 	    	Button btn = list.remove(0);
 	    	btn.setText(currentQuestion.getCorrectAnswer());
 	    	btn.setVisibility(btn.VISIBLE);
 	    	btn.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					score += 1;
 	    	    	updateQuestions();
 				}
 			});
 	    	btn = list.remove(0);
 	    	btn.setText(WrongAnswers[0]);
 	    	btn.setVisibility(btn.VISIBLE);
 	    	btn.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 				    endQuestions();					
 				}
 			});
 	    	btn = list.remove(0);
 	    	btn.setText(WrongAnswers[1]);
 	    	btn.setVisibility(btn.VISIBLE);
 	    	btn.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 				    endQuestions();					
 				}
 			});
 			btn = list.remove(0);
 	    	btn.setText(WrongAnswers[2]);
 	    	btn.setVisibility(btn.VISIBLE);
 	    	btn.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 				    endQuestions();					
 				}
 			});
     	}
     	catch(Exception e) {
     		Log.e("Questions", e.getMessage());
     	}
 	}
     
     public void endQuestions() {
     	Intent resultIntent = new Intent();
     	resultIntent.putExtra("result", score);
     	setResult(Activity.RESULT_OK, resultIntent);
     	finish();
     }
     
     public void updateQuestions() {
     	if (score == 12) {
     		endQuestions();
     	}
 		nextQuestion();
     }
     
     public void nextQuestion()
     {
     	
     	TextView classTextView = (TextView)findViewById(R.id.classification);
     	
     	if( QuestionCount < 13)
     	{
 	    	difficulty = QuestionCount/3;
 	    	
 	    	if (QuestionCount % 3 != 0)
 	    	{
 	    		difficulty++;
 	    	}
 	    		
 	    	switch (difficulty) 
 	    	{
 				case 1:
 					setCurrentQuestion(1);
 					classTextView.setText("Classification: FRESHMAN");
 					break;
 	
 				case 2:
 					setCurrentQuestion(2);
 					classTextView.setText("Classification: SOPHOMORE");
 					break;
 		
 				case 3:
 					setCurrentQuestion(3);
 					classTextView.setText("Classification: JUNIOR");
 					break;
 	
 				case 4:
 					setCurrentQuestion(4);
 					classTextView.setText("Classification: SENIOR");
 					break;
 	
 				
 				default:
 					break;
 			}
 	    	
 	    	QuestionCount++;
     	}
     }
     
     public void executeHint()
     {
     	TextView hint = (TextView)findViewById(R.id.hint);
     	hint.setVisibility(1);
     }
 
 }
