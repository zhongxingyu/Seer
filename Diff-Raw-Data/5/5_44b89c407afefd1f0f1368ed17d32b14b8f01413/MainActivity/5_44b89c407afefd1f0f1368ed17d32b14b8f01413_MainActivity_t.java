 package com.fun.midworx;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 import com.fun.midworx.crouton.Configuration;
 import com.fun.midworx.crouton.Crouton;
 import com.fun.midworx.crouton.Style;
 import com.fun.midworx.views.BoxesContainer;
 import com.fun.midworx.views.LetterOrganizer;
 import com.fun.midworx.views.ScoreManager;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class MainActivity extends MidWorxActivity {
     private static final int MAX_GAME_SECONDS = 20;
     private BoxesContainer mBoxesContainer;
     private TextView mScoreText;
     private int mLeftSecs;
     private TextView mTimeText;
 	private LetterOrganizer letterOrganizer;
     private Words mWords;
     private ScoreManager mScoreManager;
     private int mGameNumber;
     private Style croutonStyle;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
             mWords = new Words(getApplicationContext());
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         croutonStyle = new Style.Builder()
                 .setConfiguration(new Configuration.Builder()
                         .setDuration(300)
                         .build())
                 .build();
 
         mGameNumber = 0;
 
         mScoreManager = new ScoreManager();
 
         setContentView(R.layout.activity_main);
 
         setGuessButton();
         setNextButton();
 
         letterOrganizer = new LetterOrganizer(findViewById(R.id.letters_organizer));
 
         mBoxesContainer = (BoxesContainer) findViewById(R.id.words_boxes_layout);
         mScoreText = (TextView) findViewById(R.id.score_txt);
         mTimeText = (TextView) findViewById(R.id.time_txt);
 
         startNewGame();
     }
 
     private void startNewGame() {
         mBoxesContainer.clear();
         mGameNumber++;
 
         //dummy data
         List<String> words = null;
         try {
             words = mWords.getWord();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         ArrayList<ArrayList<String>> wordsByLength = new ArrayList<ArrayList<String>>();
         for(int i = 0; i < 6; ++i) {
             wordsByLength.add(new ArrayList<String>());
         }
 
         for (String word: words) {
             wordsByLength.get(word.length()-1).add(word);
         }
 
         for (int i = 2; i < 6; ++i) {
             mBoxesContainer.addBox(wordsByLength.get(i));
         }
 
         startTimer();
 
 		List<String> letters = new ArrayList<String>();
         String lettersWord = wordsByLength.get(5).get(0);
         for (int i = 0; i < lettersWord.length(); ++i) {
             letters.add(String.valueOf(lettersWord.charAt(i)));
         }
 
 		letterOrganizer.setLettersPool(letters);
 
         letterOrganizer.show();
         findViewById(R.id.next_btn).setVisibility(View.GONE);
         findViewById(R.id.guess_btn).setVisibility(View.VISIBLE);
     }
 
     private enum EndGameReason {TIMEOUT,GUESS_ALL_WORDS}
 
     private void startTimer() {
         mLeftSecs = MAX_GAME_SECONDS;
         mTimeText.post(new Runnable() {
             @Override
             public void run() {
                 mTimeText.setText("Time: " + mLeftSecs);
                 mLeftSecs--;
                 if (mLeftSecs >= 0)
                     mTimeText.postDelayed(this, 1000);
                 else
                     endGame(EndGameReason.TIMEOUT);
             }
         });
     }
 
     private void endGame(EndGameReason reason) {
         if (reason == EndGameReason.TIMEOUT) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
             builder.setMessage("Your score is " + mScoreManager.getSessionScore()).setTitle("Game Timeout!");
             letterOrganizer.hide();
             builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.guess_btn).setVisibility(View.INVISIBLE);
                     mBoxesContainer.showUnguessed();
                 }
             });
             builder.setCancelable(false);
             AlertDialog dialog = builder.create();
             dialog.show();
         }
         mBoxesContainer.permitDefinitions();
     }
 
     private void setGuessButton() {
         findViewById(R.id.guess_btn).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 guessWord(getCurrentGuess());
             }
         });
     }
 
     private void setNextButton() {
         findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startNewGame();
             }
         });
     }
 
     private void guessWord(String word) {
         if (mBoxesContainer.guessWord(word))
             updateScore(word);
     }
 
     private void updateScore(String word) {
         int earnedPoints = mScoreManager.guessedWord(word, mGameNumber);
         mScoreText.setText("Score: " + mScoreManager.getSessionScore());
         Crouton.makeText(this, earnedPoints + " points!!!", croutonStyle).show();
     }
 
     private String getCurrentGuess() {
 		return letterOrganizer.getCurrentGuessAndReset();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         //Crouton.cancelAllCroutons();
     }
 
 }
