 package org.ivan.simple.game;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 import org.ivan.simple.PandaBaseActivity;
 import org.ivan.simple.R;
 import org.ivan.simple.choose.LevelChooseActivity;
 import org.ivan.simple.game.tutorial.MessageTutorialGame;
 import org.ivan.simple.game.tutorial.Solutions;
 import org.ivan.simple.settings.SettingsInGamePanel;
 
 public class GameActivity extends PandaBaseActivity {
 
     public static final String PAUSE_TITLE = "Pause";
     private GameControl gControl;
 	private int levid;
     private Dialog tutorialDialog;
     private Dialog loseDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
         levid = intent.getIntExtra(LevelChooseActivity.LEVEL_ID, 0);
         setContentView(R.layout.activity_main);
         View settingsBtn = prepare(findViewById(R.id.game_settings));
         settingsBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 gotoSettingsScreen();
             }
         });
         View helpBtn = prepare(findViewById(R.id.game_help));
         helpBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startTutorial(0);
             }
         });
 //        GameView gView = (GameView) findViewById(R.id.game);
         RelativeLayout contentPanel = (RelativeLayout) findViewById(R.id.game_content_panel);
         GameView gView = levid == 1 ? new MessageTutorialGame(this) : new GameView(this);
         contentPanel.addView(gView);
         gView.setLevId(levid);
         gControl = gView.getControl();
         settingsBtn.bringToFront();
         helpBtn.bringToFront();
         app().getSettingsModel().registerControlChangeObserver(gControl);
         settingsDialog.setTitle(PAUSE_TITLE);
         initLoseDialog();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	// TODO Auto-generated method stub
     	switch (item.getItemId()) {
     	case R.id.increaseFPS:
     		GameManager.changeFPS(5);
     		return true;
     	case R.id.decreaseFPS:
     		GameManager.changeFPS(-5);
     		return true;
     	case R.id.restart:
 //    		restart();
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
 
 
 //    public void restart() {
 
     public void switchBackToChooseActivity(boolean complete, int score) {
 		Intent resultIntent = new Intent();
 		resultIntent.putExtra(LevelChooseActivity.LEVEL_COMPLETE, complete);
 		resultIntent.putExtra(LevelChooseActivity.COMPLETE_SCORE, score);
 		setResult(RESULT_OK, resultIntent);
 		finish();
 	}
 
     @Override
     public void onBackPressed() {
     	super.onBackPressed();
     }
 
     @Override
     protected void onPause() {
     	super.onPause();
     	gControl.stopManager();
     	System.out.println("onPause!");
     }
 
     private boolean firstRun = true;
     @Override
     protected void onResume() {
     	super.onResume();
         if(firstRun) {
             firstRun = false;
         } else {
             gotoSettingsScreen();
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
     	super.onConfigurationChanged(newConfig);
     }
 
     @Override
     protected void onDestroy() {
     	super.onDestroy();
         app().getSettingsModel().unregisterControlChangeObserver(gControl);
         gControl.releaseResources();
         loseDialog.cancel();
     }
 
     public void startTutorial(int type) {
         tutorialDialog = new Dialog(this);
         tutorialDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         tutorialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
         GameView nestedGame = new GameView(this);
         nestedGame.setLevId(-1);
         nestedGame.getControl().setAutoControls(Solutions.getDemo(type));
         tutorialDialog.setContentView(R.layout.tutorial_dialog);
         ((RelativeLayout) tutorialDialog.findViewById(R.id.tutorial_content_panel)).addView(nestedGame);
         Button continueBtn = (Button) tutorialDialog.findViewById(R.id.continue_button);
         continueBtn.setTypeface(app().getFontProvider().regular());
         continueBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 stopTutorial();
             }
         });
         continueBtn.bringToFront();
         tutorialDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
             @Override
             public void onDismiss(DialogInterface dialogInterface) {
                 gControl.startManager();
             }
         });
         gControl.stopManager();
         tutorialDialog.show();
     }
 
     public void stopTutorial() {
         tutorialDialog.cancel();
     }
 
     @Override
     protected void gotoSettingsScreen() {
         SettingsInGamePanel settings = new SettingsInGamePanel(this, app().getSettingsModel());
         settingsDialog.setContentView(settings);
         settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
             @Override
             public void onDismiss(DialogInterface dialogInterface) {
                 gControl.startManager();
             }
         });
         settings.setExitOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 settingsDialog.cancel();
                 finish();
             }
         });
         settings.setReplayOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 gControl.restartGame();
                 settingsDialog.cancel();
             }
         });
         settings.setResumeOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 settingsDialog.cancel();
             }
         });
         gControl.stopManager();
         settingsDialog.show();
     }
 
     public void showLoseDialog() {
         loseDialog.show();
     }
 
     private void initLoseDialog() {
         loseDialog = new Dialog(this);
         loseDialog.setTitle("Oops...");
         loseDialog.setContentView(R.layout.lose_dialog);
         loseDialog.getWindow().setBackgroundDrawableResource(R.drawable.settings_border);
         loseDialog.findViewById(R.id.replay).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 gControl.restartGame();
                loseDialog.cancel();
             }
         });
         loseDialog.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
     }
 
     /** Show an event in the LogCat view, for debugging */
 	private void dumpEvent(MotionEvent event) {
 		String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
 		"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
 		StringBuilder sb = new StringBuilder();
 		int action = event.getAction();
 		int actionCode = event.getActionMasked();// & MotionEvent.ACTION_MASK;
 		sb.append("event ACTION_" ).append(names[actionCode]);
 		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
 		|| actionCode == MotionEvent.ACTION_POINTER_UP) {
 		sb.append("(pid " ).append(
 		action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
 		sb.append(")" );
 		}
 		sb.append("[");
 		for (int i = 0; i < event.getPointerCount(); i++) {
 		sb.append("#" ).append(i);
 		sb.append("(pid " ).append(event.getPointerId(i));
 		sb.append(")=" ).append((int) event.getX(i));
 		sb.append("," ).append((int) event.getY(i));
 		if (i + 1 < event.getPointerCount())
 		sb.append(";" );
 		}
 		sb.append("]" );
 		if(actionCode == MotionEvent.ACTION_MOVE) return;
 		Log.d("DumpEvent", sb.toString());
 	}
 
 }
