 package me.poernomo.geoquiz;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class QuizActivity extends Activity {
 
 	private static final String TAG = "QuizActivity";
 	private static final String KEY_INDEX = "index";
 	private static final String KEY_CHEATER = "cheater";
 
 	private Button mTrueButton, mFalseButton, mCheatButton;
 	private ImageButton mNextButton, mPreviousButton;
 	private TextView mQuestionTextView;
 
 
 	private TrueFalse[] mQuestionBank = new TrueFalse[] {
 			new TrueFalse(R.string.question_canberra, true),
 			new TrueFalse(R.string.question_sydney, false),
 			new TrueFalse(R.string.question_bali, true),
 			new TrueFalse(R.string.question_vancouver, false) };
 	
 	private int mCurrentIndex = 0;
	private boolean mHasCheated;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate(Bundle) called");
 		setContentView(R.layout.activity_quiz);
 
 		mTrueButton = (Button) findViewById(R.id.true_button);
 		mTrueButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 				checkAnswer(true);
 			}
 		});
 
 		mFalseButton = (Button) findViewById(R.id.false_button);
 		mFalseButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 				checkAnswer(false);
 			}
 		});
 
 		mCheatButton = (Button) findViewById(R.id.cheat_button);
 		mCheatButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 				Intent i = new Intent(QuizActivity.this, CheatActivity.class);
 				i.putExtra(CheatActivity.EXTRA_ANSWER_IS_TRUE,
 						mQuestionBank[mCurrentIndex].isTrueQuestion());
 				startActivityForResult(i, 0);
 			}
 		});
 
 		mNextButton = (ImageButton) findViewById(R.id.next_button);
 		mNextButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 
 				getNextQuestion();
 			}
 		});
 
 		mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
 		mPreviousButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 
 				getPrevQuestion();
 			}
 		});
 
 		mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
 		mQuestionTextView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 				getNextQuestion();
 
 			}
 		});
 
 		if (savedInstanceState != null)
 		{
 			mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
			mHasCheated = savedInstanceState.getBoolean(KEY_CHEATER, false);
 		}
 		updateQuestion();
 
 	}
 
 	private void getPrevQuestion()
 	{
 		mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
 		if (mCurrentIndex < 0)
 			mCurrentIndex += mQuestionBank.length;
 		updateQuestion();
 	}
 
 	private void getNextQuestion()
 	{
 		mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
 		updateQuestion();
 	}
 
 	private void updateQuestion()
 	{
 		// Log.d(TAG, "Updating question text for question #" + mCurrentIndex,
 		// new Exception());
		mHasCheated = false; // TODO: detect which questions the user has cheated on
 		int question = mQuestionBank[mCurrentIndex].getQuestion();
 		mQuestionTextView.setText(question);
 	}
 
 	private void checkAnswer(boolean userPressedTrue)
 	{
 		int messageResId;
 		boolean correctAnswer = mQuestionBank[mCurrentIndex].isTrueQuestion();
		if (mHasCheated)
 			messageResId = R.string.judgment_toast;
 		else
 		{
 			if (correctAnswer == userPressedTrue)
 
 				messageResId = R.string.correct_toast;
 			else
 				messageResId = R.string.incorrect_toast;
 		}
 		Toast.makeText(QuizActivity.this, messageResId, Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		Log.i(TAG, "onSaveInstanceState");
 		outState.putInt(KEY_INDEX, mCurrentIndex);
		outState.putBoolean(KEY_CHEATER, mHasCheated);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		// super.onActivityResult(requestCode, resultCode, data);
 		if (data != null)
			mHasCheated = data.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN,
 					false);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		Log.d(TAG, "onDestroy() called");
 	}
 
 	@Override
 	protected void onPause()
 	{
 		// TODO Auto-generated method stub
 		super.onPause();
 		Log.d(TAG, "onPause() called");
 	}
 
 	@Override
 	protected void onResume()
 	{
 		// TODO Auto-generated method stub
 		super.onResume();
 		Log.d(TAG, "onResume() called");
 	}
 
 	@Override
 	protected void onStart()
 	{
 		// TODO Auto-generated method stub
 		super.onStart();
 		Log.d(TAG, "onStart() called");
 	}
 
 	@Override
 	protected void onStop()
 	{
 		// TODO Auto-generated method stub
 		super.onStop();
 		Log.d(TAG, "onStop() called");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.quiz, menu);
 		return true;
 	}
 
 }
