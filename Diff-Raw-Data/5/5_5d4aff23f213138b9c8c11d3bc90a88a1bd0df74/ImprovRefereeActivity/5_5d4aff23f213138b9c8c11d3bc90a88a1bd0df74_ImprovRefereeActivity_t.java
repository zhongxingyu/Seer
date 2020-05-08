 package org.bullecarree.improv.referee;
 
 import java.io.IOException;
 
 import org.bullecarree.improv.model.Improv;
 import org.bullecarree.improv.model.ImprovRenderer;
 import org.bullecarree.improv.model.ImprovType;
 import org.bullecarree.improv.reader.ImprovFileReader;
 
 import android.app.Activity;
 import android.content.IntentSender.OnFinished;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ImprovRefereeActivity extends Activity {
 
     protected static final long CAUCUS_DURATION_MS = 20 * 1000;
 
     private ProgressBar barTimeProgress;
 
     private ProgressTimer caucusTimer;
 
     private ProgressTimer improvTimer;
 
     private Improv currentImprov = new Improv();
 
     private ImprovFileReader improvReader = null;
 
     private ImprovRenderer renderer;
 
     private TextView barTimeMessage;
 
     private enum State {
         NONE, CAUCUS, CAUCUS_PAUSED, GAME, GAME_PAUSED
     };
 
     private State state = State.NONE;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
        // Disable screen saver
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
         renderer = new ImprovRenderer(getString(R.string.typeCompared),
                 getString(R.string.typeMixt), getString(R.string.unlimited),
                 getString(R.string.categoryFree));
 
         improvReader = new ImprovFileReader();
         try {
             improvReader.readImprovs();
         } catch (IOException e) {
             throw new RuntimeException("Error while parsing file", e);
         }
 
         currentImprov = improvReader.nextImprov();
         
         configureTimers();
 
         configureTimerButtons();
 
         configureNavigation();
 
         loadImprov(currentImprov);
 
     }
 
     private void configureTimers() {
         barTimeProgress = (ProgressBar) findViewById(R.id.barTime);
         barTimeMessage = (TextView) findViewById(R.id.barTimeMessage);
 
         ProgressListener updateProgressBar = new ProgressListener() {
             public void onTick(int progress, long durationMillis) {
                 barTimeProgress.setProgress(progress);
 
                 long remainingMs = 0;
                 if (caucusTimer.isRunning()) {
                     remainingMs = CAUCUS_DURATION_MS - durationMillis;
                 } else {
                     remainingMs = (currentImprov.getDuration() * 1000)
                             - durationMillis;
                 }
 
                 Log.d("prout",
                         "Duration millis : " + String.valueOf(durationMillis));
                 Log.d("rpout", "Remaining ms : " + remainingMs);
                 int remainingS = (int) remainingMs / 1000;
                 Log.d("prout", "Remaining s : " + remainingS);
 
                 barTimeMessage.setText(renderer.displayTime(remainingS));
             };
 
         };
 
         caucusTimer = new ProgressTimer((int) CAUCUS_DURATION_MS / 1000);
         caucusTimer.addProgressListener(updateProgressBar);
 
         improvTimer = new ProgressTimer(currentImprov.getDuration());
         improvTimer.addProgressListener(updateProgressBar);
     }
 
     private void configureNavigation() {
 
         final Button btnPrev = (Button) findViewById(R.id.btnPrevImprov);
         final Button btnNext = (Button) findViewById(R.id.btnNextImprov);
 
         btnPrev.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 currentImprov = improvReader.previousImprov();
                 loadImprov(currentImprov);
             }
         });
 
         btnNext.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 currentImprov = improvReader.nextImprov();
                 loadImprov(currentImprov);
             }
         });
 
     }
 
     private void enableNavigation(boolean enabled) {
         final Button btnPrev = (Button) findViewById(R.id.btnPrevImprov);
         final Button btnNext = (Button) findViewById(R.id.btnNextImprov);
         btnPrev.setEnabled(enabled);
         btnNext.setEnabled(enabled);
     }
     
     private void configureTimerButtons() {
         // Set button to start the caucus ; each button
         // should stop the other timer
         final Button btnCaucus = (Button) findViewById(R.id.btnCaucus);
         final Button btnImprov = (Button) findViewById(R.id.btnImprov);
         final Button btnPause = (Button) findViewById(R.id.btnPause);
         final Button btnReset = (Button) findViewById(R.id.btnReset);
 
         btnPause.setEnabled(false);
         btnReset.setEnabled(false);
 
         btnCaucus.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 btnCaucus.setEnabled(false);
                 btnImprov.setEnabled(false);
                 btnPause.setEnabled(true);
                 btnReset.setEnabled(true);
                 
                 enableNavigation(false);
                 
                 improvTimer.stop();
                 if (state == State.NONE) {
                     barTimeProgress.setProgress(0);
                     barTimeMessage.setText(renderer
                             .displayTime((int) CAUCUS_DURATION_MS / 1000));
                     caucusTimer.start();
                 } else if (state == State.CAUCUS_PAUSED) {
                     caucusTimer.resume();
                 }
                 state = State.CAUCUS;
 
             }
         });
 
         btnImprov.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 btnCaucus.setEnabled(false);
                 btnImprov.setEnabled(false);
                 btnPause.setEnabled(true);
                 btnReset.setEnabled(true);
 
                 enableNavigation(false);
                 
                 if (state == State.NONE || state == State.CAUCUS
                         || state == State.CAUCUS_PAUSED) {
                     barTimeProgress.setProgress(0);
                     barTimeMessage.setText(renderer.displayTime(currentImprov
                             .getDuration()));
                     caucusTimer.stop();
                     improvTimer.start();
                 } else if (state == State.GAME_PAUSED) {
                     improvTimer.resume();
                 }
                 state = State.GAME;
             }
         });
 
         btnPause.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (state == State.CAUCUS) {
                     btnCaucus.setEnabled(true);
                 }
                 btnImprov.setEnabled(true);
                 btnPause.setEnabled(false);
                 
                 enableNavigation(false);
                 
                 caucusTimer.stop();
                 improvTimer.stop();
                 if (state == State.GAME) {
                     state = State.GAME_PAUSED;
                 } else if (state == State.CAUCUS) {
                     state = State.CAUCUS_PAUSED;
                 }
             }
         });
 
         btnReset.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 btnCaucus.setEnabled(true);
                 btnImprov.setEnabled(true);
                 btnPause.setEnabled(false);
                 btnReset.setEnabled(false);
                 barTimeProgress.setProgress(0);
                 barTimeMessage.setText("-");
                 caucusTimer.reset();
                 improvTimer.reset();
                 enableNavigation(true);
                 state = State.NONE;
             }
         });
     }
 
     private void loadImprov(Improv improv) {
         renderer.setImprov(improv);
         TextView c = (TextView) findViewById(R.id.improvCategory);
         c.setText(renderer.getCategory());
 
         c = (TextView) findViewById(R.id.improvPlayerCount);
         c.setText(renderer.getPlayerCount());
 
         c = (TextView) findViewById(R.id.improvType);
         c.setText(renderer.getType());
 
         c = (TextView) findViewById(R.id.improvTitle);
         c.setText(renderer.getTitle());
 
         c = (TextView) findViewById(R.id.improvDuration);
         c.setText(renderer.getDuration());
         
         improvTimer.setDurationInSeconds(improv.getDuration());
         
     }
 
     /**
      * The 'pause' callback of the activity ; this is used when you navigate
      * from a place to another
      */
     @Override
     protected void onPause() {
         super.onPause();
         stopAllTheClocks();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         stopAllTheClocks();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         stopAllTheClocks();
     }
 
     /**
      * The 'resume' callback of the activity. Used when navigating back here.
      */
     @Override
     protected void onResume() {
         super.onResume();
         restartAllTheClocks();
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         restartAllTheClocks();
     }
 
     private void stopAllTheClocks() {
         improvTimer.stop();
         caucusTimer.stop();
     }
 
     private void restartAllTheClocks() {
         // A timer probably has to be resumed
         if (state == State.CAUCUS) {
             caucusTimer.resume();
         } else if (state == State.GAME) {
             improvTimer.resume();
         }
     }
 
 }
