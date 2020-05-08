 /**
  * Copyright (C) 2012 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.rounds;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.text.SpannableStringBuilder;
 import android.text.style.AbsoluteSizeSpan;
 import android.text.style.StyleSpan;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 public final class GameActivity extends Activity {
     private Game game;
     private PowerManager.WakeLock wakeLock;
     private GameDatabase database;
 
     private View layout;
     private JogWheel jogWheel;
     private ScoreHistoryTable scoreHistoryTable;
     private TextView labelTextView;
     private TextView valueTextView;
 
     private ActionBarBackground actionBarBackground;
     private ActionBar actionBar;
     private ImageButton nextRound;
     private TextView roundTextView;
     private ImageButton previousRound;
 
     private volatile boolean savePending = false;
     private final Runnable saveRunnable = new Runnable() {
         @Override public void run() {
             savePending = false;
             database.saveLater(game);
         }
     };
 
     @Override public void onCreate(Bundle savedState) {
         super.onCreate(savedState);
 
         database = GameDatabase.getInstance(getApplicationContext());
 
         Intent intent = getIntent();
         String gameId = savedState != null
                 ? savedState.getString(IntentExtras.GAME_ID)
                 : intent.getStringExtra(IntentExtras.GAME_ID);
         game = database.get(gameId);
         game.setRound(game.roundCount() - 1);
 
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
 
         getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 
         layout = getLayoutInflater().inflate(R.layout.jogwheel, null);
         setContentView(layout);
 
         labelTextView = (TextView) layout.findViewById(R.id.label);
         valueTextView = (TextView) layout.findViewById(R.id.value);
 
         scoreHistoryTable = new ScoreHistoryTable(getApplicationContext(),
                 (TableLayout) layout.findViewById(R.id.runningScores),
                 (TableLayout) layout.findViewById(R.id.currentScores),
                 (HorizontalScrollView) layout.findViewById(R.id.runningScoresScroller),
                 game);
 
         jogWheel = (JogWheel) layout.findViewById(R.id.jogWheel);
         jogWheel.setModel(game);
         jogWheel.setListener(new JogWheel.Listener() {
             @Override public void selecting(int player, int value) {
                 int selectingFrom = game.playerScore(player, game.round());
 
                 labelTextView.setText(game.playerName(player));
                 labelTextView.setTextColor(game.playerColor(player));
                 labelTextView.setVisibility(View.VISIBLE);
 
                 SpannableStringBuilder ssb = new SpannableStringBuilder();
                 if (selectingFrom != 0 && selectingFrom != value) {
                     ssb.append(Integer.toString(selectingFrom));
                     if (value > selectingFrom) {
                         ssb.append(" + ").append(Integer.toString(value - selectingFrom));
                     } else {
                         ssb.append(" - ").append(Integer.toString(selectingFrom - value));
                     }
                     ssb.append(" = ");
                 }
                 String valueString = (value > 0 ? "+" : "") + Integer.toString(value);
                 ssb.append(valueString);
                 ssb.setSpan(new AbsoluteSizeSpan(32, true),
                         ssb.length() - valueString.length(), ssb.length(), 0);
                 ssb.setSpan(new StyleSpan(Typeface.BOLD),
                         ssb.length() - valueString.length(), ssb.length(), 0);
 
                 valueTextView.setText(ssb);
                 valueTextView.setVisibility(View.VISIBLE);
                 actionBar.hide();
             }
             @Override public void selected(int player, int value) {
                 int round = game.round();
                 game.setPlayerScore(player, round, value);
                 scoreHistoryTable.scoreChanged(player, round);
                 updateActionBarBackground();
                 roundChanged();
                 saveLater();
             }
             @Override public void cancelled() {
                 roundChanged();
             }
         });
 
         View roundPicker = getLayoutInflater().inflate(R.layout.round_picker, null);
         nextRound = (ImageButton) roundPicker.findViewById(R.id.nextRound);
         roundTextView = (TextView) roundPicker.findViewById(R.id.roundNumber);
         previousRound = (ImageButton) roundPicker.findViewById(R.id.previousRound);
         nextRound.setOnClickListener(new View.OnClickListener() {
             @Override public void onClick(View view) {
                 game.setRound(game.round() + 1);
                 roundChanged();
             }
         });
         previousRound.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 game.setRound(game.round() - 1);
                 roundChanged();
             }
         });
 
         actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                 ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
         actionBar.setCustomView(roundPicker);
         actionBarBackground = new ActionBarBackground(getResources());
         actionBar.setBackgroundDrawable(actionBarBackground);
         updateActionBarBackground();
 
         roundChanged();
     }
 
     @Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.game, menu);
        menu.findItem(R.id.randomPlayer).setEnabled(game.playerCount() > 1);
         return true;
     }
 
     @Override protected void onSaveInstanceState(Bundle savedState) {
         super.onSaveInstanceState(savedState);
         savedState.putString(IntentExtras.GAME_ID, game.getId());
     }
 
     /**
      * Saves the game at some point in the future. Multiple calls to this method
      * will be coalesced into a single filesystem write.
      */
     private void saveLater() {
         if (savePending) {
             return;
         }
 
         layout.getHandler().postDelayed(saveRunnable, TimeUnit.SECONDS.toMillis(30));
         savePending = true;
     }
 
     private void roundChanged() {
         scoreHistoryTable.roundCountChanged();
         roundTextView.setText("Round " + Integer.toString(game.round() + 1));
         labelTextView.setVisibility(View.INVISIBLE);
         previousRound.setEnabled(game.round() > 0);
         nextRound.setEnabled(game.round() < game.roundCount() - 1
                 || game.hasNonZeroScore(game.round()));
         valueTextView.setVisibility(View.INVISIBLE);
         actionBar.show();
         jogWheel.invalidate();
     }
 
     private void updateActionBarBackground() {
         int color = Color.WHITE;
         int maxTotal = Integer.MIN_VALUE;
         for (int p = 0; p < game.playerCount(); p++) {
             int playerTotal = game.playerTotal(p);
             if (playerTotal > maxTotal) {
                 color = game.playerColor(p);
                 maxTotal = playerTotal;
             } else if (playerTotal == maxTotal) {
                 color = Color.WHITE;
             }
         }
         actionBarBackground.setColor(color);
     }
 
     @Override protected void onDestroy() {
         super.onDestroy();
         database = null;
     }
 
     @Override protected void onPause() {
         super.onPause();
 
         if (wakeLock.isHeld()) {
             wakeLock.release();
         }
 
         layout.getHandler().removeCallbacks(saveRunnable);
         database.save(game);
     }
 
     @Override protected void onResume() {
         super.onResume();
 
         boolean useWakeLock = true; // TODO: make this a preference?
         if (useWakeLock && !wakeLock.isHeld()) {
             wakeLock.acquire();
         }
 
         roundChanged();
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.editPlayers:
             Intent intent = new Intent(this, SetUpActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             intent.putExtra(IntentExtras.GAME_ID, game.getId());
             intent.putExtra(IntentExtras.IS_NEW_GAME, false);
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
             return true;
 
         case R.id.randomPlayer:
            if (game.playerCount() < 2) {
                throw new IllegalStateException();
            }
             List<Integer> playersToEliminate = new ArrayList<Integer>();
             for (int p = 0; p < game.playerCount(); p++) {
                 playersToEliminate.add(p);
             }
             Collections.shuffle(playersToEliminate);
             jogWheel.selectPlayer(playersToEliminate);
             return true;
 
         case android.R.id.home:
             intent = new Intent(this, HomeActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
             return true;
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
