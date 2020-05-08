 package com.example.wecharades.views;
 /** TODO: 
  * REGISTER SCORE POINTS IF GUESSED RIGHT
  * DO NOT DOWNLOAD EVERYTIME. OVERRIDE THE ONRESUME and ONPAUSE?
  * 
  */
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.example.wecharades.R;
 import com.example.wecharades.model.Turn;
 import com.example.wecharades.presenter.GuessCharadePresenter;
 
 public class GuessCharadeActivity extends GenericActivity  {
 
 	private TextView possibleLetters;
 	private EditText answerWord;
 	private VideoView videoView;
 	private GuessCharadePresenter presenter;
 	private TextView timerView;
 	public int gameState = NO_GAME;
 	public static final int NO_GAME = 0;
 	public  static final int GAME_FINISHED = 1;
 	private Turn turn;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.guessvideo);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		
 		answerWord = (EditText) findViewById(R.id.editAnswerCharade);
 		videoView = (VideoView) findViewById(R.id.streamedVideoSurface);
 		possibleLetters = (TextView) findViewById(R.id.possibleLetters);
 		possibleLetters.setVisibility(4);
 		timerView = (TextView) findViewById(R.id.timerView);
 		timerView.setVisibility(4);
 		turn = (Turn) getIntent().getExtras().getSerializable("turn");
 		presenter = new GuessCharadePresenter(this);
 		presenter.initializeTimer(timerView);
 		presenter.downloadVideo(GuessCharadeActivity.this, videoView, turn);
 		
 	}
 	@Override
 	protected void onResume(){
 		if(presenter.downloadState == GuessCharadePresenter.DOWNLOAD_FINISHED && gameState == NO_GAME){
 			presenter.playVideo();
 		}
 		super.onResume();
 	}
 	/**
 	 * TODO: Should register the score and so on if the user has guessed the right words.
 	 * @param view
 	 */
 	public void onClickGuess(View view){
 		if(presenter.checkRightWord(answerWord) == true){
 			videoView.stopPlayback();
 			gameState = GAME_FINISHED;
 			turn.setRecPlayerScore(3);
			turn.setAnsPlayerScore(5); 
 			AlertDialog.Builder mDialog = new AlertDialog.Builder(GuessCharadeActivity.this);
 			mDialog.setTitle("Charade");
 			mDialog
 			.setMessage("You Guessed Right!")
 			.setCancelable(false)
 			.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					videoView.stopPlayback();
 					presenter.timer.cancel();
 					Intent intent = new Intent(GuessCharadeActivity.this, StartActivity.class);/*TODO:GameDashboard.class*/
 					startActivity(intent);
 					finish();
 				}
 			});
 			AlertDialog alertDialog = mDialog.create();
 			alertDialog.show();
 		}
 		else{
 			AlertDialog.Builder mDialog = new AlertDialog.Builder(GuessCharadeActivity.this);
 			mDialog.setTitle("Charade");
 			mDialog
 			.setMessage("You Guessed Wrong! Hurry up!")
 			.setNegativeButton("Retry",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					dialog.cancel();
 				}
 			});
 			AlertDialog alertDialog = mDialog.create();
 			alertDialog.show();
 		}			
 	}
 
 	@Override
 	public TextView getTextArea() {
 		return possibleLetters;
 	}
 
 	@Override
 	public void showMessage(String str){
 		possibleLetters.setVisibility(0);
 		super.showMessage(str);
 	}
 	/**
 	 * Shows up an finishAlertDialog and stops the video.
 	 */
 	public void finishDialog() {
 		videoView.stopPlayback();
 		AlertDialog.Builder builder = new AlertDialog.Builder(GuessCharadeActivity.this);
 		builder.setTitle("Game Over")
 		.setMessage("The right charade is: " + presenter.currentWord.toLowerCase() + ".")
 		.setCancelable(false)
 		.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intent = new Intent(GuessCharadeActivity.this, StartActivity.class);/*TODO:GameDashboard.class*/
 				startActivity(intent);
 				finish();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();

 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			showDialog();
 			return true;
 		}
 		if ((keyCode == KeyEvent.KEYCODE_MENU)) {
 			showDialog();
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Shows up an Dialog for pressing the back and settings button.
 	 */
 	private void showDialog(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(GuessCharadeActivity.this);
 		builder.setMessage("If you exit you'll lose this turn!").setTitle("Warning!")
 		.setCancelable(true)
 		.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				turn.setRecPlayerScore(1);//TODO: what score should rec player get if answerplayer exits?
 				turn.setAnsPlayerScore(0);//TODO: 0 score if exits this turn.
 				dialog.cancel();
 				finish();
 			}
 		})
 		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		AlertDialog mAlert = builder.create();
 		mAlert.show();
 	}
 }
 
