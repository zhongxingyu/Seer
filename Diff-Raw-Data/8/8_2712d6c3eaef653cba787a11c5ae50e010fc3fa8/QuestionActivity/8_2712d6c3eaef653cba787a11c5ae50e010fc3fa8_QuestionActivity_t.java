 package com.flashmath.activity;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.flashmath.R;
 import com.flashmath.fragment.ArithmeticQuestionAnswerFragment;
 import com.flashmath.fragment.ArithmeticQuestionFragment;
 import com.flashmath.fragment.FractionQuestionAnswerFragment;
 import com.flashmath.fragment.FractionQuestionFragment;
 import com.flashmath.fragment.GeometryQuestionAnswerFragment;
 import com.flashmath.fragment.GeometryQuestionFragment;
 import com.flashmath.fragment.QuestionFragment;
 import com.flashmath.models.ArithmeticQuestion;
 import com.flashmath.models.FractionQuestion;
 import com.flashmath.models.GeometryQuestion;
 import com.flashmath.models.Question;
 import com.flashmath.network.FlashMathClient;
 import com.flashmath.util.ColorUtil;
 import com.flashmath.util.QuestionUtil;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class QuestionActivity extends Activity {
 
 	public static final String IS_MOCK_QUIZ_INTENT_KEY = "isMockQuiz";
 	private static final String NEXT_QUESTION_BTN_TITLE = "Next";
 	private static final String VERIFY_ANSWER_BTN_TITLE = "Answer";
 	private static final String END_QUIZ_BTN_TITLE = "Finish";
 	public static final String QUESTIONS_ANSWERED_INTENT_KEY = "QUESTIONS_ANSWERED";
 	public QuestionFragment qf;
 	public Fragment qaf;
 	private int currentQuestionIndex;
 	private ArrayList<Question> questionList;
 	private TextView tvQuestionProgress;
 	private Button btnVerifyAndNextQuestion;
 	private String subject;
 	private String backgroundColor;
 	private boolean isMockQuiz;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//we will use progress wheel when it's needed (e.g. network requests)
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		setContentView(R.layout.activity_question);
 		
 		btnVerifyAndNextQuestion = (Button) findViewById(R.id.btnVerifyAndNextQuestion);
 		btnVerifyAndNextQuestion.setVisibility(View.VISIBLE);
 		subject = getIntent().getStringExtra("subject");
 		isMockQuiz = getIntent().getBooleanExtra(IS_MOCK_QUIZ_INTENT_KEY, true);
 		btnVerifyAndNextQuestion.setBackground(ColorUtil.getButtonStyle(subject, this));
 		Button btnClear = (Button) findViewById(R.id.btnClear);
 		btnClear.setBackground(ColorUtil.getButtonStyle(subject, this));
 		backgroundColor = ColorUtil.identifySubjectColor(subject);
 		tvQuestionProgress = (TextView) findViewById(R.id.tvQuestionProgress);
 		TextView tvQuestionSlash = (TextView) findViewById(R.id.tvQuestionSlash);
 		TextView tvQuestionTotal = (TextView) findViewById(R.id.tvQuestionTotal);
 		tvQuestionProgress.setTextColor(ColorUtil.subjectColorInt(subject));
 		tvQuestionSlash.setTextColor(ColorUtil.subjectColorInt(subject));
 		tvQuestionTotal.setTextColor(ColorUtil.subjectColorInt(subject));
 		ActionBar ab = getActionBar();
 		ab.setIcon(ColorUtil.getBarIcon(subject, this));
 		String subjectTitle = Character.toUpperCase(subject.charAt(0))+subject.substring(1);
 		ab.setTitle(subjectTitle + " Questions");
 		
 		if (subject.equalsIgnoreCase("Fractions")) {
 			qf = new FractionQuestionFragment();
 		} else if (subject.equalsIgnoreCase("Geometry")) {
 			qf = new GeometryQuestionFragment();
 		} else {
 			qf = new ArithmeticQuestionFragment();
 		}
 		//disable the button until we have data loaded...
 		btnVerifyAndNextQuestion.setEnabled(false);
 		if (!isMockQuiz) {
 			setupServerQuestions();
 		} else {
 			currentQuestionIndex = 0;
 			JSONArray jsonQuestions = QuestionUtil.getMockQuestions(subject);
 			questionList = new ArrayList<Question>();
 			
 			if (subject.equalsIgnoreCase("Fractions")) {
 				questionList.addAll(FractionQuestion.fromJSONArray(jsonQuestions, subject));
 			} else if (subject.equalsIgnoreCase("Geometry")) {
 				questionList.addAll(GeometryQuestion.fromJSONArray(jsonQuestions, subject));
 			} else {
 				questionList.addAll(ArithmeticQuestion.fromJSONArray(jsonQuestions, subject));
 			}
 		}
 		qf.setBackgroundColor(backgroundColor);
 		
 		getFragmentManager().beginTransaction().add(R.id.fragmentForQuestion, qf).commit();
 	}
 	
 	@Override
 	protected void onResume() {
 		if (isMockQuiz) {
 			postQuestionLoaded();
 		}
 		super.onResume();
 	}
 
 	private void setupServerQuestions() {
 		setProgressBarIndeterminateVisibility(true);
 		currentQuestionIndex = 0;
 		FlashMathClient client = FlashMathClient.getClient(this);
 		client.getQuestions(subject, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject response) {
 				JSONArray jsonResults = null;
 				try {
 					jsonResults = response.getJSONArray("questions");
 					
 					questionList = new ArrayList<Question>();
 					
 					if (subject.equalsIgnoreCase("Fractions")) {
 						questionList.addAll(FractionQuestion.fromJSONArray(jsonResults, subject));
 					} else if (subject.equalsIgnoreCase("Geometry")) {
 						questionList.addAll(GeometryQuestion.fromJSONArray(jsonResults, subject));
 					} else {
 						questionList.addAll(ArithmeticQuestion.fromJSONArray(jsonResults, subject));
 					}
 					
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 				postQuestionLoaded();
 			}
 			
 			@Override
 			public void onFailure(Throwable arg0, JSONObject errorResponse) {
 				super.onFailure(arg0, errorResponse);
 				btnVerifyAndNextQuestion.setEnabled(true);
 				setProgressBarIndeterminateVisibility(false);
 			}
 		});
 	}
 
 	protected void postQuestionLoaded() {
 		qf.setQuestion(questionList.get(currentQuestionIndex));
 		if (subject.equalsIgnoreCase("Fractions")) {
			((FractionQuestionFragment) qf).setupParameters();
 			qaf = new FractionQuestionAnswerFragment();
 			((FractionQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 		} else if (subject.equalsIgnoreCase("Geometry")) {
			((GeometryQuestionFragment) qf).setupParameters();
 			qaf = new GeometryQuestionAnswerFragment();
 			((GeometryQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 		} else {
			((ArithmeticQuestionFragment) qf).setupParameters();
 			qaf = new ArithmeticQuestionAnswerFragment();
 			((ArithmeticQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 		}
 		
 		tvQuestionProgress.setText(String.valueOf(currentQuestionIndex + 1));
 		//data is loaded, we can enable the button
 		btnVerifyAndNextQuestion.setEnabled(true);
 		setProgressBarIndeterminateVisibility(false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.question, menu);
 		return true;
 	}
 
 	public void onClearQuestionAnswer(View v) {
 		qf.clearAnswerFields();
 	}
 	
 	public void onVerifyAndNextQuestionPressed(View v) {
 		if (btnVerifyAndNextQuestion.getText().toString().equals(VERIFY_ANSWER_BTN_TITLE)) {
 			onVerifyQuestionAnswer(v);
 		} else if (btnVerifyAndNextQuestion.getText().toString().equals(NEXT_QUESTION_BTN_TITLE)){
 			onNextQuestion(v);
 		} else {
 			qf.saveUserAnswer();
 			finalizeQuiz();
 		}
 	}
 	
 	public void onVerifyQuestionAnswer(View v) {
 		qf.saveUserAnswer();
 		
 		if (subject.equalsIgnoreCase("Fractions")) {
 			((FractionQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 
 			getFragmentManager().beginTransaction()
 			.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 								 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 			.replace(R.id.fragmentForQuestion, (FractionQuestionAnswerFragment) qaf)
 			.addToBackStack(null)
 			.commit();
 		} else if (subject.equalsIgnoreCase("Geometry")) {
 			((GeometryQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 			getFragmentManager().beginTransaction()
 			.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 								 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 			.replace(R.id.fragmentForQuestion, (GeometryQuestionAnswerFragment) qaf)
 			.addToBackStack(null)
 			.commit();
 		} else {
 			((ArithmeticQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 			getFragmentManager().beginTransaction()
 			.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 								 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 			.replace(R.id.fragmentForQuestion, (ArithmeticQuestionAnswerFragment) qaf)
 			.addToBackStack(null)
 			.commit();
 		}
 		
 		//we reached end of questions, remove the Next Question button and 
 		//let user use End Quiz button
 		if(currentQuestionIndex+1 >= this.questionList.size()) {
 			btnVerifyAndNextQuestion.setText(END_QUIZ_BTN_TITLE);
 		} else {
 			btnVerifyAndNextQuestion.setText(NEXT_QUESTION_BTN_TITLE);
 		}
 	}
 
 	public void onNextQuestion(View v) {
 		if (currentQuestionIndex < this.questionList.size()) {
 			btnVerifyAndNextQuestion.setText(VERIFY_ANSWER_BTN_TITLE);
 			//if user forgot to press save button and just presses next question
 			qf.saveUserAnswer();
 
 			Question q = qf.getQuestion();
 			this.questionList.remove(currentQuestionIndex);
 			this.questionList.add(currentQuestionIndex, q);
 			currentQuestionIndex++;
 
 			if(currentQuestionIndex < this.questionList.size()) {
 	            getFragmentManager().popBackStack();
 	            getFragmentManager().beginTransaction().remove(qf).commit();
 	            if (this.subject.equalsIgnoreCase("Fractions")) {
 	            	qf = new FractionQuestionFragment();
 	            } else if (this.subject.equalsIgnoreCase("Geometry")) {
 	            	qf = new GeometryQuestionFragment();
 				} else {
 					qf = new ArithmeticQuestionFragment();
 				}
 	            
 	            qf.setBackgroundColor(backgroundColor);
 	            qf.setQuestion(this.questionList.get(currentQuestionIndex));
 	            getFragmentManager().beginTransaction().add(R.id.fragmentForQuestion, qf).commit();
 	            tvQuestionProgress.setText(String.valueOf(currentQuestionIndex + 1));
 			} else {
 				finalizeQuiz();
 			}
 		}
 		else {
 			finalizeQuiz();
 		}
 	}
 
 	public void onEndQuiz(View v) {
 		qf.saveUserAnswer();
 		finalizeQuiz();
 	}
 	
 	private void finalizeQuiz() {
 		Intent i = new Intent(this, ResultActivity.class);
 		i.putExtra(QuestionActivity.QUESTIONS_ANSWERED_INTENT_KEY, this.questionList);
 		i.putExtra(ResultActivity.SUBJECT_INTENT_KEY, subject);
 		i.putExtra(QuestionActivity.IS_MOCK_QUIZ_INTENT_KEY, isMockQuiz);
 		startActivity(i);
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent i = new Intent(this, SubjectActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(i);
 	}
 }
