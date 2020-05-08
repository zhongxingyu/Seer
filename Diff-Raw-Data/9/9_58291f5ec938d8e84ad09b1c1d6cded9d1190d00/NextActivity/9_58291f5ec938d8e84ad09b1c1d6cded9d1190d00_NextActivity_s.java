 package edu.uci.ics.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class NextActivity  extends Activity {
 	
 	private Button mBackButton;
 	private RadioGroup radioGroup;
 	private RadioButton radio0, radio1, radio2, radio3;
 	private TextView mTimeLabel;
 	private Handler mHandler = new Handler();
 	private long mStart;
 	private long mPause = 0;
 	private boolean getWrongAnswers = true;
	private static long duration = 180000;//180000;
 	private Random rand = new Random();
 	private int questionNumber; // used to generate a question number 0-7 to pick what question we ask
 	long elapsed = duration;
 	long now = 0;
 	private String correctAnswer;
 	private DbAdapter db;
 	private Cursor cur;
 	private boolean pause;
 
 	private int numOfCorrectAnswers;
 	private int totalNumOfQuestions;
 	
 	private Runnable updateTask = new Runnable() {
 		public void run() {	
 			
 			if(!pause){
 				now = SystemClock.uptimeMillis();
 				elapsed = duration - (now - mStart);
 				if (elapsed > 0) {
 					int seconds = (int) (elapsed / 1000);
 					int minutes = seconds / 60;
 					seconds     = seconds % 60;
 	
 					if (seconds < 10) {
 						mTimeLabel.setText("" + minutes + ":0" + seconds);
 					} else {
 						mTimeLabel.setText("" + minutes + ":" + seconds);            
 					}
 	
 					mHandler.postAtTime(this, now + 1000);
 				}
 				else {
 					mHandler.removeCallbacks(this);
 					
 					//TODO: need to direct to finish state
 					Intent intent = new Intent(NextActivity.this, FinishActivity.class);
 					intent.putExtra("numCorrect", numOfCorrectAnswers);
 					intent.putExtra("totalQuestions", totalNumOfQuestions);
 					intent.putExtra("totalTime", duration);
 //					if(totalNumOfQuestions == 0){
 //						intent.putExtra("timePerQuestion", duration);
 //					}
 //					else{
 //						intent.putExtra("timePerQuestion", duration / totalNumOfQuestions);
 //					}
 					//TODO: need to add timePerQuestion
 					startActivity(intent);
 					finish();
 				}
 			}
 		}	
 	};
 	
 	public void createQuestion(final Bundle savedInstanceState)
 	{
 		final TextView tv = (TextView)this.findViewById(R.id.textView1);
 		questionNumber = rand.nextInt(10); //later, make this a random number from 0-7
 		System.err.println(questionNumber);
 		ArrayList<String>answers = new ArrayList<String>();
         tv.setText("");
         db = new DbAdapter(this);
         
         cur = db.pickQuestion(questionNumber);
         cur.moveToFirst();
         
         int index = (rand.nextInt(cur.getCount())) ; // pick a random row in the set. This will be our question
         cur.moveToPosition(index);
         String question = "";
         switch (questionNumber){
         	case 0://who directed the movie Y?
 	            question = "Who directed the movie " + cur.getString(0)  + "?";
 	            tv.setText(question);
 	            correctAnswer = cur.getString(1);
 	            
 	            answers.add(correctAnswer);
 	            //this loop grabs 3 wrong answers. The loop exists because if, for example, the answer is the name of a director, that director shows up multiple times in the table
 	            //thus, we need to check to make sure we don't have the same director
 	            while (getWrongAnswers)
 	            {
 	            	cur.moveToPosition(rand.nextInt(cur.getCount())); //move to random position in table
 	            	String temp = cur.getString(1);
 	            	if (!answers.contains(temp)) //if the answer is different than the correct answer (i.e. incorrect answer)
 	            	{
 	            		answers.add(temp); //add it to our list of possible answers
 	            	}
 	            	if (answers.size() >= 4) //if we have 4 answers
 	            	{
 	            		getWrongAnswers = false;
 	            	}
 	            }
 	            break;
         	case 1: // when was the movie X released?
         		question = "When was the movie " + cur.getString(0)  + " released?";
 	            tv.setText(question);
 	            correctAnswer = cur.getString(1);
 	            
 	            answers.add(correctAnswer);
 	            //this loop grabs 3 wrong answers. The loop exists because if, for example, the answer is the name of a director, that director shows up multiple times in the table
 	            //thus, we need to check to make sure we don't have the same director
 	            while (getWrongAnswers)
 	            {
 	            	cur.moveToPosition(rand.nextInt(cur.getCount())); //move to random position in table
 	            	String temp = cur.getString(1);
 	            	if (!answers.contains(temp)) //if the answer is different than the correct answer (i.e. incorrect answer)
 	            	{
 	            		answers.add(temp); //add it to our list of possible answers
 	            	}
 	            	if (answers.size() >= 4) //if we have 4 answers
 	            	{
 	            		getWrongAnswers = false;
 	            	}
 	            }
         		break;
 
         	case 2://in which movie did the stars X and Y star in together?
         		cur.moveToFirst();
         		ArrayList<String> movies = new ArrayList<String>();//contains all movies
         		ArrayList<String> potentialMovies = new ArrayList<String>();//contains all movies with at least 2 stars
         		while (!cur.isAfterLast())
         		{
         			if (!movies.contains(cur.getString(1)))
         			{
         				movies.add(cur.getString(1));//grab every movie
         			}
         			//System.out.println(Integer.valueOf(cur.getString(0)));
         			if (Integer.valueOf(cur.getString(0)) > 1 && !potentialMovies.contains(cur.getString(1)))
         			{
         				potentialMovies.add(cur.getString(1)); //grab every movie that has two actors
         			}
         			
         			cur.moveToNext();
         		}
         		
         		correctAnswer = potentialMovies.get(rand.nextInt(potentialMovies.size())); // pick a random movie that has at least two actors
         		
         		movies.remove(correctAnswer); //remove the right answer from our collection
         		ArrayList<String> actors = new ArrayList<String>();
         		System.err.println(correctAnswer);
         		Cursor cur2 = db.getActors(correctAnswer);
         		cur2.moveToFirst();
         		while (!cur2.isAfterLast())
         		{
         			actors.add(cur2.getString(0) + " " + cur2.getString(1));
         			cur2.moveToNext();
         		}
         		
         		String actor1 = actors.get(rand.nextInt(actors.size()));
         		
         		actors.remove(actor1);
         		String actor2 = actors.get(rand.nextInt(actors.size()));
         		
         		question = "In Which Movie did the stars " + actor1 + " and " + actor2 + " appear together?";
         		tv.setText(question);
         		answers.add(correctAnswer);
         		cur.moveToFirst();
         		
         		while (getWrongAnswers)
 	            {
 	            	String temp = movies.get(rand.nextInt(movies.size()));
 	            	movies.remove(temp);
 	            	if (!answers.contains(temp)) //if the answer is different than the correct answer (i.e. incorrect answer)
 	            	{
 	            		answers.add(temp); //add it to our list of possible answers
 	            	}
 	            	if (answers.size() >= 4) //if we have 4 answers
 	            	{
 	            		getWrongAnswers = false;
 	            	}
 	            }
         		
         		break;
         	case 3://who starred in the movie X?
         		
         		correctAnswer = cur.getString(1) + " " + cur.getString(2);
         		answers.add(correctAnswer);
         		String movie = cur.getString(0);
         		ArrayList<String>potentialActors = new ArrayList<String>();
         		actors = new ArrayList<String>();
         		cur.moveToFirst();
         		while (!cur.isAfterLast())
         		{
         				if (movie.equals(cur.getString(0)) && !correctAnswer.equals(cur.getString(1) + " " + cur.getString(2))
         						&& !potentialActors.contains(cur.getString(1) + " " + cur.getString(2)))
         				{
         					potentialActors.add(cur.getString(1) + " " + cur.getString(2));//get actors who also starred in the movie
         				}
         				else if (!correctAnswer.equals(cur.getString(1) + " " + cur.getString(2)) && !potentialActors.contains(cur.getString(1) + " " + cur.getString(2)) 
         						&& !actors.contains(cur.getString(1) + " " + cur.getString(2)))
         				{
         					actors.add(cur.getString(1) + " " + cur.getString(2));//get actors who didnt star in the movie
         				}
         				cur.moveToNext();
         		}
         		while (getWrongAnswers)
 	            {
 	            	String temp = actors.get(rand.nextInt(actors.size()));
 	            	if (!answers.contains(temp)) //if the answer is different than the correct answer (i.e. incorrect answer)
 	            	{
 	            		answers.add(temp); //add it to our list of possible answers
 	            	}
 	            	if (answers.size() >= 4) //if we have 4 answers
 	            	{
 	            		getWrongAnswers = false;
 	            	}
 	            }
         		
 
         		System.err.println(correctAnswer);
         		
         		question = "Who starred in the movie " + movie + "?";
         		tv.setText(question);
         		cur.moveToFirst();
         		
 
         		
         		break;
         	case 4: //who DIDN'T Appear in film X?
         		cur.moveToFirst();
         		potentialMovies = new ArrayList<String>();//contains all movies with at least 3 stars
         		potentialActors = new ArrayList<String>();//all the actors who starred in the movie we pick
         		while (!cur.isAfterLast())
         		{
         			//System.out.println(Integer.valueOf(cur.getString(0)));
         			if (Integer.valueOf(cur.getString(0)) > 2 && !potentialMovies.contains(cur.getString(1)))
         			{
         				potentialMovies.add(cur.getString(1)); //grab every movie that has three actors
         			}
         			
         			cur.moveToNext();
         		}
         		//System.out.println("test1");
         		movie = potentialMovies.get(rand.nextInt(potentialMovies.size()));
         		question = "Who did NOT appear in the movie " + movie + "?";
         		tv.setText(question);
         		System.out.println(movie);
         		cur2 = db.getActors(movie);
         		//System.out.println("test2");
         		while (getWrongAnswers)
 	            {
         			cur2.moveToPosition(rand.nextInt(cur2.getCount()));
 	            	String temp = cur2.getString(0) + " " + cur2.getString(1);
 	            	if (!answers.contains(temp)) //if the answer is different than the correct answer (i.e. incorrect answer)
 	            	{
 	            		answers.add(temp); //add it to our list of possible answers
 	            	}
 	            	if (answers.size() >= 3) //if we have 3 answers
 	            	{
 	            		getWrongAnswers = false;
 	            	}
 	            }
         		//System.out.println("test3");
         		cur2.moveToFirst();
         		while (!cur2.isAfterLast())
         		{
         			potentialActors.add(cur2.getString(0) + " " + cur2.getString(1));
         			cur2.moveToNext();
         		}
         		//System.out.println("test4");
         		Cursor cur3 = db.getActors();
         		boolean getRightAnswer = true;
         		while (getRightAnswer)
         		{
         			cur3.moveToPosition(rand.nextInt(cur3.getCount()));
         			if (!potentialActors.contains(cur3.getString(0) + " " + cur3.getString(1)))
         			{
         				correctAnswer = cur3.getString(0) + " " + cur3.getString(1);
         				getRightAnswer = false;
         			}
         		}
         		//System.out.println("test5");
         		System.err.println(correctAnswer);
         		answers.add(correctAnswer);
         		break;
         	case 5: //who directed the star X?
         		ArrayList<String>potentialDirectors = new ArrayList<String>();
         		actor1 = cur.getString(1) + " " + cur.getString(2); //pick an actor
         		correctAnswer = cur.getString(0); //get the director we want
         		answers.add(correctAnswer);
         		question = "Who directed the star " + actor1 + "?";
         		tv.setText(question);
         		cur.moveToFirst();
         		while (!cur.isAfterLast()) //grab all the directors who directed our star
         		{
         			if (actor1.equals(cur.getString(1) + " " + cur.getString(2)))
         			{
         				potentialDirectors.add(cur.getString(0));
         			}
         			cur.moveToNext();
         		}
         		while (getWrongAnswers)
         		{
         			cur.moveToPosition(rand.nextInt(cur.getCount()));
         			if (!potentialDirectors.contains(cur.getString(0))) //if the director never directed X
         			{	
         				answers.add(cur.getString(0));
         			}
         			if (answers.size() >= 4)
         			{
         				getWrongAnswers = false;
         			}
         		}
         		System.err.println(correctAnswer);
         		break;
         		
         	case 6: //who DIDN'T direct the star X?
         		potentialDirectors = new ArrayList<String>();
         		potentialActors = new ArrayList<String>();
         		cur.moveToFirst();
         		while(!cur.isAfterLast())
         		{
         			if (cur.getInt(0) > 3) //if the actor has been directed 3 times
         			{
         				potentialActors.add(cur.getString(1) + ";" + cur.getString(2));
         			}
         			potentialDirectors.add(cur.getString(3)); //holds every director
         			cur.moveToNext();
         		}
         		String[] splitActor = potentialActors.get(rand.nextInt(potentialActors.size())).split(";");
         		question = "Who did NOT direct the star " + splitActor[0] + " " + splitActor[1] + "?";
         		System.out.println(splitActor[0] + " " + splitActor[1]);
         		tv.setText(question);
         		//System.out.println(splitActor[0] + " " + splitActor[1]);
         		cur2 = db.getDirectors(splitActor[0], splitActor[1]);
         		cur2.moveToFirst();
         		while (getWrongAnswers)
         		{
         			cur2.moveToPosition(rand.nextInt(cur2.getCount()));
         			String temp = cur2.getString(0);
         			if (!answers.contains(temp))
         			{
         				answers.add(cur2.getString(0));//add a director who did direct the star
         			}
         			if (answers.size()>=3)
         			{
         				getWrongAnswers = false;
         			}
         		}
         		while (!cur2.isAfterLast())
         		{
         			if (potentialDirectors.contains(cur2.getString(0)))
         			{
         				potentialDirectors.remove(cur2.getString(0)); // remove the director from our list of possible correct answers
         			}
         			cur2.moveToNext();
         		}
         		correctAnswer = potentialDirectors.get(rand.nextInt(potentialDirectors.size()));
         		answers.add(correctAnswer);
         		System.err.println(correctAnswer);
         		break;
         	case 7: //which star appears in both movie X and movie Y?
         		String movie1 = "";
 				String movie2 = "";
 				potentialActors = new ArrayList<String>();//actors who have appeared in 2+ films
 				actors = new ArrayList<String>(); //any actor
 				movies = new ArrayList<String>();
 				cur.moveToFirst();
 				
 				while (!cur.isAfterLast()) //grab actors who have appeared in at least 2 films
 				{
 					if (cur.getInt(0) > 1) // if star shows up at least twice
 					{
 						potentialActors.add(cur.getString(1) + " " + cur.getString(2));
 					}
 					if (!actors.contains(cur.getString(1) + " " + cur.getString(2)))
 					{
 						actors.add(cur.getString(1) + " " + cur.getString(2));
 					}
 					cur.moveToNext();
 				}
 				
 				correctAnswer = potentialActors.get(rand.nextInt(potentialActors.size()));
 				answers.add(correctAnswer);
 				potentialActors.remove(correctAnswer);
 				actors.remove(correctAnswer);
 				cur2 = db.pickQuestion(3);
 				
 				cur2.moveToFirst();
 				
 				System.err.println(correctAnswer);
 				while (!cur2.isAfterLast())
 				{
 					
 					if (correctAnswer.equals(cur2.getString(1) + " " + cur2.getString(2)))
 					{
 						movies.add(cur2.getString(0));//find two movies the actor starred in
 					}
 					if (movies.contains(cur2.getString(0)))
 					{
 						actors.remove(cur2.getString(1) + " " + cur2.getString(2));//remove all actors who starred in the potential films
 					}
 					cur2.moveToNext();
 				}
 				
 				movie1 = movies.get(rand.nextInt(movies.size()));
 				
 				movies.remove(movie1);
 				movie2 = movies.get(rand.nextInt(movies.size()));
 				
 				question = "Who starred in the movies " + movie1 + " and " + movie2 + "?";
 				tv.setText(question);
 				while (getWrongAnswers)
 				{
 					String temp = potentialActors.get(rand.nextInt(potentialActors.size()));
 					potentialActors.remove(temp);
 					answers.add(temp);
 					if (answers.size() >= 4)
 					{
 						getWrongAnswers =false;
 					}
 				}
 				
 				System.err.println(correctAnswer);
 				break;
         	case 8: //which star did not appear in the same movie with the star X?
         		potentialActors = new ArrayList<String>();//actors who have appeared with our actor
 				actors = new ArrayList<String>();
 				movies = new ArrayList<String>();
 				cur.moveToFirst();
 				System.out.println("test1");
 				while (!cur.isAfterLast())//grab all actors who have at least 2 co-actors
 				{
 					if (cur.getInt(0) > 2) //movie has 3 actors
 					{
 						potentialActors.add(cur.getString(2) +  ";" + cur.getString(3));
 					}
 					cur.moveToNext();
 				}
 				System.out.println("test2");
 				actor1 = potentialActors.get(rand.nextInt(potentialActors.size()));//this is our star X
 				splitActor = actor1.split(";");
 				actor1 = splitActor[0] + " " + splitActor[1];
 				cur2 = db.getMovies(splitActor[0], splitActor[1]); // get all movies this actor has starred in
 				question = "which star did not appear in the same movie with the star " + " " + actor1 + "?";
 				tv.setText(question);
 				System.out.println(actor1);
 				potentialActors = new ArrayList<String>(); //pool of people who starred with our X
 				cur2.moveToFirst();
 				System.out.println("test4");
 				while (!cur2.isAfterLast())
 				{
 					movies.add(cur2.getString(0)); // get all movie X has starred in
 					cur2.moveToNext();
 				}
 				System.out.println("test5");
         		for (String temp: movies)
         		{
         			cur3 = db.getActors(temp);
         			cur3.moveToFirst();
         			while (!cur3.isAfterLast())
         			{
         				if (!potentialActors.contains(cur3.getString(0) + " " + cur3.getString(1))
         						&& !actor1.equals(cur3.getString(0) + " " + cur3.getString(1)))
         				{
         					potentialActors.add(cur3.getString(0) + " " + cur3.getString(1));
         				}
         				cur3.moveToNext();
         			}
         		}
         		System.out.println("test6");
         		cur3 = db.getActors();
         		cur3.moveToFirst();
         		while (!cur3.isAfterLast())
         		{
         			if (!potentialActors.contains(cur3.getString(0) + " " + cur3.getString(1)))
         			{
         				actors.add(cur3.getString(0) + " " + cur3.getString(1)); //pool of right answers
         			}
         			cur3.moveToNext();
         		}
         		System.out.println("test7");
         		correctAnswer = actors.get(rand.nextInt(actors.size()));
         		answers.add(correctAnswer);
         		System.out.println("test8");
         		while (getWrongAnswers)
         		{
         			String temp = potentialActors.get(rand.nextInt(potentialActors.size()));
         			potentialActors.remove(temp);
         			answers.add(temp);
         			if (answers.size() >= 4)
         			{
         				getWrongAnswers = false;
         			}
         		}
         		System.out.println("test9");
 				System.err.println(correctAnswer);
         		break;
         	case 9://Who directed the star X in year Y?
         		actor1 = cur.getString(3) + " " + cur.getString(4);
         		String year = cur.getString(1);
         		correctAnswer = cur.getString(0);
         		answers.add(correctAnswer);
         		question = "Who directed the star " + actor1 + " in the year " + year + "?";
         		tv.setText(question);
         		cur.moveToFirst();
         		potentialDirectors = new ArrayList<String>();
         		ArrayList<String> directors = new ArrayList<String>();
         		System.out.println("test1");
         		while (!cur.isAfterLast())
         		{
         			if (year.equals(cur.getString(1)) && actor1.equals(cur.getString(3) + " " + cur.getString(4))
         					&& !potentialDirectors.contains(cur.getString(0))) //if same year and same actor and we don't already have the director
         			{
         				potentialDirectors.add(cur.getString(0));
         				System.out.println(actor1 + " " + cur.getString(0));
         			}
         			cur.moveToNext();
         		}
         		System.out.println("test2");
         		cur.moveToFirst();
         		while (!cur.isAfterLast())
         		{
         			if (!potentialDirectors.contains(cur.getString(0)) && !directors.contains(cur.getString(0)))
         			{
         				//System.out.println(cur.getString(0));
         				directors.add(cur.getString(0));
         			}
         			cur.moveToNext();
         		}
         		System.out.println("test3");
         		System.out.println(directors.size());
         		while (getWrongAnswers)
         		{
         			String temp = directors.get(rand.nextInt(directors.size()));
         			directors.remove(temp);
         			answers.add(temp);
         			if (answers.size() >= 4)
         			{
         				getWrongAnswers = false;
         			}
         		}
         		System.out.println("test4");
         		System.err.println(correctAnswer);
         		break;
             default:
             	correctAnswer = "";
             	break;
             	
            
         }
 
         //give each radio button a random answer in our answer set
         db.close();
         int radioAnswer = rand.nextInt(answers.size());
         this.radio0.setText(answers.get(radioAnswer));
         answers.remove(radioAnswer);
         
         radioAnswer = rand.nextInt(answers.size());
         this.radio1.setText(answers.get(radioAnswer));
         answers.remove(radioAnswer);
         
         radioAnswer = rand.nextInt(answers.size());
         this.radio2.setText(answers.get(radioAnswer));
         answers.remove(radioAnswer);
         
         radioAnswer = rand.nextInt(answers.size());
         this.radio3.setText(answers.get(radioAnswer));
         answers.remove(radioAnswer);
         
         this.radio0.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				if (radio0.getText().equals(correctAnswer))
 				{
 					//tv.setText("Correct!");
 					promptUser("CORRECT!");
 				}
 				else
 				{
 					//tv.setText("Incorrect!");
 					promptUser("INCORRECT!");
 				}
 			}
         	
         });
         this.radio1.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View arg0) {
  				if (radio1.getText().equals(correctAnswer))
  				{
  					//tv.setText("Correct!");
 					promptUser("CORRECT!");
  				}
  				else
  				{
  					//tv.setText("Incorrect!");
 					promptUser("INCORRECT!");
 
  				}
  			}
          	
          });
         this.radio2.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View arg0) {
  				if (radio2.getText().equals(correctAnswer))
  				{
  					//tv.setText("Correct!");
 					promptUser("CORRECT!");
  				}
  				else
  				{
  					//tv.setText("Incorrect!");
 					promptUser("INCORRECT!");
 
  				}
  			}
          	
          });
         this.radio3.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View arg0) {
  				if (radio3.getText().equals(correctAnswer))
  				{
  					//tv.setText("Correct!");
 					promptUser("CORRECT!");
  				}
  				else
  				{
  					//tv.setText("Incorrect!");
 					promptUser("INCORRECT!");
 
  				}
  			}
          	
          });
         
         // Retrieve the button, change its value and add an event listener
         this.mBackButton = (Button)this.findViewById(R.id.backButton);
         this.mBackButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				cur.close();
 				db.close();
 				mHandler.removeCallbacks(updateTask);
 				finish();
 			}
         });
         cur.close();
         db.close();
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.next);
         this.radio0 = (RadioButton)this.findViewById(R.id.radio0);
         this.radio1 = (RadioButton)this.findViewById(R.id.radio1);
         this.radio2 = (RadioButton)this.findViewById(R.id.radio2);
         this.radio3 = (RadioButton)this.findViewById(R.id.radio3);
         mTimeLabel = (TextView)this.findViewById(R.id.timeLabel);
         if(getIntent().getExtras().getLong("pauseTime") == 0){
         	mStart = SystemClock.uptimeMillis();
         }
         else{
             mStart = getIntent().getExtras().getLong("pauseTime");
         }
         
         numOfCorrectAnswers = getIntent().getExtras().getInt("numCorrect");
         totalNumOfQuestions = getIntent().getExtras().getInt("totalQuestions");
         //TODO: need to add timePerQuestion
         
         //start the timer
         mHandler.post(updateTask);
         createQuestion(savedInstanceState);
         pause = false;
     }
     
     private void toNextQuestion(){
 		Intent intent = new Intent(NextActivity.this, NextActivity.class);
 		intent.putExtra("pauseTime", mStart);
 		intent.putExtra("numCorrect", numOfCorrectAnswers);
 		intent.putExtra("totalQuestions", totalNumOfQuestions);
 		//TODO: need to add timePerQuestion
 		startActivity(intent);
 		finish();
     }
     
     private void promptUser(String title){
     	AlertDialog.Builder alertDialog = new AlertDialog.Builder(NextActivity.this);
 		alertDialog.setTitle(title);
 		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 		    	mStart += (SystemClock.uptimeMillis() - mPause);
 		    	pause = false;
 		    	toNextQuestion();
 		    }
 		});
 		alertDialog.show();
 		if(title.equals("CORRECT!")){
 			numOfCorrectAnswers++;
 		}
 		totalNumOfQuestions++;
 		
 		pause = true;
 		mPause = SystemClock.uptimeMillis();
     }
     
     @Override
     public void onPause()
     {
     	super.onPause();
     	mPause = SystemClock.uptimeMillis();
     	System.out.println("Paused");
     }
     
     @Override
     public void onResume()
     {
     	super.onResume();
     	System.out.println("Resumed");
     	if (mPause != 0)
     	{
     		System.out.println("mStart: " + mStart);
     		mStart += (SystemClock.uptimeMillis()- mPause);
     		System.out.println("mStart: " + mStart);
     		mPause = 0;
     	}
     	
     	System.out.println("Resumed");
     	
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState)
     {
     	super.onSaveInstanceState(savedInstanceState);
     	 savedInstanceState.putLong("pauseTime",SystemClock.uptimeMillis());
     	 savedInstanceState.putLong("savedMStart", mStart);
     	 savedInstanceState.putString("question", (String) ((TextView)this.findViewById(R.id.textView1)).getText());
     	 savedInstanceState.putString("radio0", (String) ((RadioButton)this.findViewById(R.id.radio0)).getText());
     	 savedInstanceState.putString("radio1", (String) ((RadioButton)this.findViewById(R.id.radio1)).getText());
     	 savedInstanceState.putString("radio2", (String) ((RadioButton)this.findViewById(R.id.radio2)).getText());
     	 savedInstanceState.putString("radio3", (String) ((RadioButton)this.findViewById(R.id.radio3)).getText());
     	 savedInstanceState.putString("answer", correctAnswer);
     	 
     	 System.out.println("Saved");
     }
    
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState)
     {
     	super.onRestoreInstanceState(savedInstanceState);
     	System.out.println("Restored");
     	mPause = savedInstanceState.getLong("pauseTime");
     	mStart = savedInstanceState.getLong("savedMStart");
     	System.out.println("mStart: " + mStart);
     	mStart += (SystemClock.uptimeMillis() - mPause);
     	//System.out.println(SystemClock.uptimeMillis()- mPause);
     	mPause = 0;
 		((TextView)this.findViewById(R.id.textView1)).setText(savedInstanceState.getString("question"));
 		((RadioButton)this.findViewById(R.id.radio0)).setText(savedInstanceState.getString("radio0"));
 		((RadioButton)this.findViewById(R.id.radio1)).setText(savedInstanceState.getString("radio1"));
 		((RadioButton)this.findViewById(R.id.radio2)).setText(savedInstanceState.getString("radio2"));
 		((RadioButton)this.findViewById(R.id.radio3)).setText(savedInstanceState.getString("radio3"));
 		correctAnswer = savedInstanceState.getString("answer");
 		
 //	   	System.out.println((String) ((TextView)this.findViewById(R.id.textView1)).getText());
 //	   	System.out.println((String) ((RadioButton)this.findViewById(R.id.radio0)).getText());
 //	   	System.out.println((String) ((RadioButton)this.findViewById(R.id.radio1)).getText());
 //	   	System.out.println((String) ((RadioButton)this.findViewById(R.id.radio2)).getText());
 //	   	System.out.println((String) ((RadioButton)this.findViewById(R.id.radio3)).getText());
 			
     	System.out.println("mStart: " + mStart);
     	System.out.println("Restored");
     }
 
 }
