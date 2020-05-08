 package tu.kom.uhg;
 
 import android.os.Bundle;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.app.AlertDialog.Builder;
 
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TableLayout;
 
 import android.util.Log;
 import android.view.View.OnClickListener;
 import android.view.Window;
 
 import android.view.View;
 import android.util.Log;
 
 public class QuizActivity extends GenericActivity {
 	
 	Intent menu = null;
 	BufferedReader bReader = null;
 	static JSONArray quesList = null;
 	
 	EditText question = null;
 	RadioButton answer1 = null;
 	RadioButton answer2 = null;
 	RadioButton answer3 = null;
 	RadioButton answer4 = null;
 	RadioGroup answers = null;
 	Button finish = null;
 	int selectedAnswer = -1;
 	int quesIndex = 0;
 	int numEvents = 0;
 	int selected[] = null;
 	int correctAns[] = null;
 	boolean review =false;
 	Button prev, next = null;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quiz);
 
 		TableLayout quizLayout = (TableLayout) findViewById(R.id.quizLayout);		
 		quizLayout.setVisibility(android.view.View.INVISIBLE);		
 		
 		try {
 			loadQuestions();
 			question = (EditText) findViewById(R.id.question);
 			answer1 = (RadioButton) findViewById(R.id.a0);
 			answer2 = (RadioButton) findViewById(R.id.a1);
 			answer3 = (RadioButton) findViewById(R.id.a2);
 			answer4 = (RadioButton) findViewById(R.id.a3);
 			answers = (RadioGroup) findViewById(R.id.answers);
 			RadioGroup questionLayout = (RadioGroup)findViewById(R.id.answers);
 			Button finish = (Button)findViewById(R.id.finish);
 			finish.setOnClickListener(finishListener);
 
 			prev = (Button)findViewById(R.id.Prev);
 			prev.setOnClickListener(prevListener);
 			next = (Button)findViewById(R.id.Next);
 			next.setOnClickListener(nextListener);
 
 
 			selected = new int[getQuesList().length()];
 			java.util.Arrays.fill(selected, -1);
 			correctAns = new int[getQuesList().length()];
 			java.util.Arrays.fill(correctAns, -1);
 
 
 			this.showQuestion(0,review);
 
 			quizLayout.setVisibility(android.view.View.VISIBLE);
 
 		} catch (Exception e) {
 			Log.e("", e.getMessage().toString(), e.getCause());
 		} 
 
 	}
 
 
 	private void showQuestion(int qIndex,boolean review) {
 		try {
 			JSONObject aQues = getQuesList().getJSONObject(qIndex);
 			String quesValue = aQues.getString("Question");
 			if (correctAns[qIndex] == -1) {
 				String correctAnsStr = aQues.getString("CorrectAnswer");
 				correctAns[qIndex] = Integer.parseInt(correctAnsStr);
 			}
 
 			question.setText(quesValue.toCharArray(), 0, quesValue.length());
 			answers.check(-1);
 			answer1.setTextColor(Color.BLACK);
 			answer2.setTextColor(Color.BLACK);
 			answer3.setTextColor(Color.BLACK);
 			answer4.setTextColor(Color.BLACK);
 			JSONArray ansList = aQues.getJSONArray("Answers");
 			String aAns = ansList.getJSONObject(0).getString("Answer");
 			answer1.setText(aAns.toCharArray(), 0, aAns.length());
 			aAns = ansList.getJSONObject(1).getString("Answer");
 			answer2.setText(aAns.toCharArray(), 0, aAns.length());
 			aAns = ansList.getJSONObject(2).getString("Answer");
 			answer3.setText(aAns.toCharArray(), 0, aAns.length());
 			aAns = ansList.getJSONObject(3).getString("Answer");
 			answer4.setText(aAns.toCharArray(), 0, aAns.length());
 			Log.d("",selected[qIndex]+"");
 			if (selected[qIndex] == 0)
 				answers.check(R.id.a0);
 			if (selected[qIndex] == 1)
 				answers.check(R.id.a1);
 			if (selected[qIndex] == 2)
 				answers.check(R.id.a2);
 			if (selected[qIndex] == 3)
 				answers.check(R.id.a3);
 
 			setScoreTitle();
 			if (quesIndex == (getQuesList().length()-1)) 
 				next.setEnabled(false);
 
 			if (quesIndex == 0)
 				prev.setEnabled(false);
 
 			if (quesIndex > 0)
 				prev.setEnabled(true);
 
 			if (quesIndex < (getQuesList().length()-1))
 				next.setEnabled(true);
 
 
 			if (review) {
 				Log.d("review",selected[qIndex]+""+correctAns[qIndex]);;	
 				if (selected[qIndex] != correctAns[qIndex]) {
 					if (selected[qIndex] == 0)
 						answer1.setTextColor(Color.RED);
 					if (selected[qIndex] == 1)
 						answer2.setTextColor(Color.RED);
 					if (selected[qIndex] == 2)
 						answer3.setTextColor(Color.RED);
 					if (selected[qIndex] == 3)
 						answer4.setTextColor(Color.RED);
 				}
 				if (correctAns[qIndex] == 0)
 					answer1.setTextColor(Color.GREEN);
 				if (correctAns[qIndex] == 1)
 					answer2.setTextColor(Color.GREEN);
 				if (correctAns[qIndex] == 2)
 					answer3.setTextColor(Color.GREEN);
 				if (correctAns[qIndex] == 3)
 					answer4.setTextColor(Color.GREEN);
 			}
 		} catch (Exception e) {
 			Log.e(this.getClass().toString(), e.getMessage(), e.getCause());
 		}
 	}
 
 
 	private OnClickListener finishListener = new OnClickListener() {
 		public void onClick(View v) {
 			setAnswer();
 			//Calculate Score
			int score = 0;
 			for(int i=0; i<correctAns.length; i++){
 				if ((correctAns[i] != -1) && (correctAns[i] == selected[i]))
 					score++;
 			}
			//convert date to format ("dd.mm.yy")
			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("dd.mm.yy", Locale.GERMANY);
			String date = df.format(c.getTime());
 			
			addScore("Quiz", date, score*100);
 			AlertDialog alertDialog;
 			alertDialog = new AlertDialog.Builder(QuizActivity.this).create();
 			alertDialog.setTitle("Score");
 			alertDialog.setMessage((score) +" out of " + (QuizActivity.getQuesList().length()));
 
 			//retake button
 			/* 
 			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Retake", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which) {
 					review = false;
 					quesIndex=0;
 					QuizActivity.this.showQuestion(0, review);
 				}
 			});
 			*/
 
 			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Review", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which) {
 					review = true;
 					quesIndex=0;
 					QuizActivity.this.showQuestion(0, review);
 				}
 			});
 
 			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Quit", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which) {
 					review = false;
 					finish();
 				}
 			});
 
 			alertDialog.show();
 
 		}
 	};
 
 	private void setAnswer() {
 		if (answer1.isChecked())
 			selected[quesIndex] = 0;
 		if (answer2.isChecked())
 			selected[quesIndex] = 1;
 		if (answer3.isChecked())
 			selected[quesIndex] = 2;
 		if (answer4.isChecked())
 			selected[quesIndex] = 3;
 
 		Log.d("",Arrays.toString(selected));
 		Log.d("",Arrays.toString(correctAns));
 
 	}
 
 	private OnClickListener nextListener = new OnClickListener() {
 		public void onClick(View v) {
 			setAnswer();
 			quesIndex++;
 			if (quesIndex >= QuizActivity.getQuesList().length())
 				quesIndex = QuizActivity.getQuesList().length() - 1;
 
 			showQuestion(quesIndex,review);
 		}
 	};
 
 	private OnClickListener prevListener = new OnClickListener() {
 		public void onClick(View v) {
 			setAnswer();
 			quesIndex--;
 			if (quesIndex < 0)
 				quesIndex = 0;
 
 			showQuestion(quesIndex,review);
 		}
 	};
 
 	private void setScoreTitle() {
 		this.setTitle("SciQuiz3     " + (quesIndex+1)+ "/" + QuizActivity.getQuesList().length());
 	}
 	
 	
 	
 	
 	
 	 private void loadQuestions() throws Exception {
 	    	try {
 	    		Bundle extras = getIntent().getExtras();
 	    		String fileSuffix;
 	    		if (extras != null) {
 		    		fileSuffix = extras.getString("markerId");
 		    		if (fileSuffix == null) fileSuffix = "default";
 	    		}else{
 	    			fileSuffix = "default";
 	    		}
 	    		int resId = getResources().getIdentifier("raw/questions_" + fileSuffix, "txt", getPackageName());
 	    		if (resId == 0)
 	    			resId = getResources().getIdentifier("raw/questions_default", "txt", getPackageName());
 	    		
 	    		InputStream questions = this.getBaseContext().getResources()
 					.openRawResource(resId);
 	    		
 				bReader = new BufferedReader(new InputStreamReader(questions));
 				StringBuilder quesString = new StringBuilder();
 				String aJsonLine = null;
 				while ((aJsonLine = bReader.readLine()) != null) {
 					quesString.append(aJsonLine);
 				}
 				Log.d(this.getClass().toString(), quesString.toString());
 				JSONObject quesObj = new JSONObject(quesString.toString());
 				quesList = quesObj.getJSONArray("Questions");
 				Log.d(this.getClass().getName(),
 					"Num Questions " + quesList.length());
 	    	} catch (Exception e){
 	    		
 	    	} finally {
 				try {
 					if (bReader != null)
 						bReader.close();
 				} catch (Exception e) {
 					Log.e("", e.getMessage().toString(), e.getCause());
 				}
 
 	    	}
 	    	
 
 	    }
 	    
 	    public static JSONArray getQuesList() {
 	    	return quesList;
 	    }
 
 }
