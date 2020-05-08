 package com.example.gameapp;
 
 import java.util.Random;
 
 import com.example.gameapp.SQLiteAdapter;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class GameActivity extends Activity {
 	
 	private final long roundTime = 3000;
 	private final long countBy = 1000;
 	private final int E_BUTTON_INCREMENT = 75;
 	private final int N_BUTTON_INCREMENT = 100;
 	private final int H_BUTTON_INCREMENT = 150;
 	private final int MIN_BUTTON = 100;
 	
 	private MediaPlayer gameMusic;
 	private EditText input;
 	private Context context = this;
 	private long startTime;
 	private long randomTime;
 	private int difficultyIncrement;
 	private ImageButton[] button = new ImageButton[9];
 	private int randButton;
 	private int score;
 	private int roundNum;
 	private TextView timerTextView;
 	private TextView scoreTextView;
 	private TextView roundTextView;
 	private TextView winLoseTextView;
 	private LinearLayout gameLayout;
 	private MyCountDownTimer roundTimer;
 	private MyCountDownTimer randomTimer;
 	private MyCountDownTimer buttonTimer;
 	private boolean roundDisplay;
 	private boolean inbetween;
 	private boolean waiting;
 	private boolean buttonHere;
 	private int newWin;
 	private String difficulty;
 	private boolean musicState;
 	private SQLiteAdapter mySQLiteAdapter;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_activity);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			difficulty = extras.getString("difficulty");
 			musicState = extras.getBoolean("music");
 		}
		if (difficulty == "Normal") {
				difficultyIncrement = N_BUTTON_INCREMENT;
		} else if (difficulty == "Hard") {
 			difficultyIncrement = H_BUTTON_INCREMENT;
 		} else {
			difficultyIncrement = E_BUTTON_INCREMENT;
 		}
 		startTime = 2000;
 		newWin = 0;
 		score = 0;
 		roundNum = 1;
 		randomTime = 500 + randomTime();
 		waiting = false;
 		buttonHere = false;
 		roundDisplay = true;
 		inbetween = false;
 		timerTextView = (TextView) findViewById(R.id.timerTextView);
 		scoreTextView = (TextView) findViewById(R.id.scoreTextView);
 		roundTextView = (TextView) findViewById(R.id.roundTextView);
 		winLoseTextView = (TextView) findViewById(R.id.winLoseTextView);
 		roundTimer = new MyCountDownTimer(roundTime, countBy);
 		randomTimer = new MyCountDownTimer(randomTime, countBy);
 		buttonTimer = new MyCountDownTimer(startTime, countBy);
 		
 		gameLayout = (LinearLayout) findViewById(R.id.gameLayout);
 		//gameLayout.setOnClickListener(buttonClickListener);
 		gameLayout.setOnTouchListener(buttonTouchListener);
 		
 		button[0] = (ImageButton) findViewById(R.id.button0);
 		button[1] = (ImageButton) findViewById(R.id.button1);
 		button[2] = (ImageButton) findViewById(R.id.button2);
 		button[3] = (ImageButton) findViewById(R.id.button3);
 		button[4] = (ImageButton) findViewById(R.id.button4);
 		button[5] = (ImageButton) findViewById(R.id.button5);
 		button[6] = (ImageButton) findViewById(R.id.button6);
 		button[7] = (ImageButton) findViewById(R.id.button7);
 		button[8] = (ImageButton) findViewById(R.id.button8);
 		
 		for (int i = 0; i < 9; i++) {
 			button[i].setOnTouchListener(buttonTouchListener);
 			button[i].setVisibility(-1);
 		}
 		
 		roundTimer.start();
 			
 	}
 	
 	OnTouchListener buttonTouchListener = new OnTouchListener(){
 		
 	
 	@Override
     public boolean onTouch(View v, MotionEvent event) {
 		int action = event.getAction();
         if (action == MotionEvent.ACTION_DOWN){
         	if ((!roundDisplay) && (!waiting)) {
         		if (v.getId() == button[randButton].getId()) {
         			if (newWin == 0) {
         				newWin = 1;
         			}
         			/*if (buttonHere) {
     				buttonTimer.cancel();
         			} else if (inbetween) {
     				randomTimer.cancel();
         			}*/
     			} else {
     				newWin = -1;
     				//win = false;
     				//buttonTimer.cancel();
     				//randomTimer.cancel();
     			}
         	}
         }
         return false;
 	}
 	};
 	
 	public class MyCountDownTimer extends CountDownTimer {
 		
 		public MyCountDownTimer(long startTime, long countBy) {
 			super(startTime, countBy);
 		}
 		
 		//when the timer finishes and they haven't cleared all the cards
 		//tell them they lost and disable the buttons because the Alert Dialog doesn't work
 		@Override
 		public void onFinish() {
 			if (newWin != -1) {
 				if (roundDisplay) {
 					randButton = randomButton();
 					randomTime = 500 + randomTime();
 					randomTimer = null;
 					randomTimer = new MyCountDownTimer(randomTime, countBy);
 					roundTextView.setVisibility(-1);
 					roundDisplay = false;
 					//winLoseTextView.setVisibility(-1);
 					randomTimer.start();
 					inbetween = true;
 				} else if (waiting) {
 					if (startTime - difficultyIncrement >= 100) {
 						startTime -=  difficultyIncrement;
 					} else {
 						startTime = MIN_BUTTON;
 					}
 					buttonTimer = null;
 					buttonTimer = new MyCountDownTimer(startTime, countBy);
 					button[randButton].setVisibility(-1);
 					winLoseTextView.setVisibility(-1);
 					roundTextView.setText(getResources().getString(R.string.round) + " " + String.valueOf(roundNum));
 					roundTextView.setVisibility(1);
 					waiting = false;
 					roundTimer.start();
 					roundDisplay = true;
 					newWin = 0;
 				} else if (inbetween) {
 					inbetween = false;
 					buttonTimer.start();
 					buttonHere = true;
 					button[randButton].setVisibility(1);
 				} else if (buttonHere) {
 					if (newWin == 1) {
 						winLoseTextView.setText(getResources().getString(R.string.win));
 						winLoseTextView.setVisibility(1);
 						score += difficultyIncrement;
 						scoreTextView.setText(String.valueOf(score));
 						buttonHere = false;
 						roundNum += 1;
 						roundTimer.start();
 						waiting = true;
 					} else {
 						winLoseTextView.setText(getResources().getString(R.string.timesup));
 						loseCondition();
 					}
 				}
 			} else {
 				winLoseTextView.setText(getResources().getString(R.string.soon));
 				loseCondition();
 			}
 		}
 		
 		//tick the timer down by 1 second
 		@Override
 		public void onTick(long millisUntilFinished) {
 			timerTextView.setText(String.valueOf(millisUntilFinished));
 			if (buttonHere) {
 				timerTextView.setVisibility(1);
 			} else {
 				timerTextView.setVisibility(-1);
 			}
 		}
 	}
 	public int randomTime() {
 		int rTime;
 		Random random = new Random();
 		rTime = random.nextInt(2000);
 		return rTime;
 	}
 	public int randomButton() {
 		int rButton;
 		Random random = new Random();
 		rButton = random.nextInt(9);
 		return rButton;
 	}
 	
 	public void loseCondition() {
 		input = new EditText(this);
 		button[randButton].setEnabled(false);
 		winLoseTextView.setVisibility(1);
 		roundTextView.setText(getResources().getString(R.string.finished));
 		roundTextView.setVisibility(1);
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		input.setSingleLine(true);
 		input.setHint("AAA");
 		builder.setTitle(getResources().getString(R.string.lose));
 		builder.setMessage(getResources().getString(R.string.score) + " " + String.valueOf(score));
 		builder.setView(input);
 		builder.setCancelable(false);
 		builder.setPositiveButton("Back to Menu", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id){
 				String enteredInitials;
 				if (input.getText().length() > 3) {
 					enteredInitials = input.getText().toString().substring(2);
 				} else {
 					enteredInitials = input.getText().toString();
 				}
 				mySQLiteAdapter = new SQLiteAdapter(context);
 		        mySQLiteAdapter.openToWrite();
 		        mySQLiteAdapter.scoreInsert(enteredInitials, score);
 		        mySQLiteAdapter.close();
 		        finish();
 			}
 		});
 		AlertDialog finishedDialog = builder.create();
 		finishedDialog.show();
 		
 	}
 	
 	@Override
 	public void finish(){
 		Intent passData = new Intent(GameActivity.this, Menu.class);
 		passData.putExtra("difficulty", difficulty);
 		passData.putExtra("music", musicState);
 		setResult(RESULT_OK, passData);
 		super.finish();
 	}
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		startMedia();
 	}
 
 	@Override
 	protected void onStop(){
 		gameMusic.release();
 		gameMusic = null;
 		super.onStop();
 	}
 	
 	public void openMedia(){
 		gameMusic = MediaPlayer.create(getApplicationContext(), R.raw.game_music);
 		gameMusic.start();
 		gameMusic.setLooping(true);
 	}
 
 	public void closeMedia(){
 		gameMusic.release();
 		gameMusic = null;
 	}
 
 	public void startMedia(){
 		if (musicState){
 			openMedia();
 		} 
 	}
 }
