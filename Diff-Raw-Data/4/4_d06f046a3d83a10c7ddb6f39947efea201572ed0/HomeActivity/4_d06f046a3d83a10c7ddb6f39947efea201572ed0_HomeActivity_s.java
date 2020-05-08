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
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.SpannableStringBuilder;
 import android.text.format.DateUtils;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import java.util.List;
 
 public class HomeActivity extends Activity {
     private GameDatabase database;
     private ListView gameList;
 
     private final View.OnClickListener replayListener = new View.OnClickListener() {
         @Override public void onClick(View button) {
             int position = gameList.getPositionForView((View) button.getParent());
             Game game = (Game) gameList.getAdapter().getItem(position);
             launchGame(game.replay());
         }
     };
 
     private final View.OnClickListener resumeListener = new View.OnClickListener() {
         @Override public void onClick(View view) {
             int position = gameList.getPositionForView(view);
             Game game = (Game) gameList.getAdapter().getItem(position);
             launchGame(game);
         }
     };
 
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         database = GameDatabase.getInstance(getApplicationContext());
 
         setContentView(R.layout.home);
         gameList = (ListView) findViewById(R.id.gameList);
         gameList.setItemsCanFocus(true);
 
     }
 
     @Override public void onResume() {
         super.onResume();
         gameList.setAdapter(new GameListAdapter(database.allGames()));
     }
 
     @Override protected void onPause() {
         super.onPause();
     }
 
     @Override protected void onDestroy() {
         super.onDestroy();
         database = null;
     }
 
     @Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.home, menu);
         return true;
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.newGame:
             Intent newGameIntent = new Intent(this, SetUpActivity.class);
             startActivity(newGameIntent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void launchGame(Game game) {
         Intent intent = new Intent(getApplicationContext(), GameActivity.class);
         intent.putExtra(GameActivity.EXTRA_GAME, Json.gameToJson(game));
         startActivity(intent);
     }
 
     private class GameListAdapter extends BaseAdapter {
         private final List<Game> games;
 
         private GameListAdapter(List<Game> games) {
             this.games = games;
         }
 
         @Override public int getCount() {
             return games.size();
         }
 
         @Override public Game getItem(int i) {
             return games.get(i);
         }
 
         @Override public long getItemId(int i) {
             return games.get(i).getDateStarted();
         }
 
         @Override public View getView(int position, View recycle, ViewGroup parent) {
             LinearLayout layout = (LinearLayout) ((recycle == null)
                     ? getLayoutInflater().inflate(R.layout.game_item, parent, false)
                     : recycle);
             TextView players = (TextView) layout.findViewById(R.id.players);
             TextView summary = (TextView) layout.findViewById(R.id.summary);
             Button replay = (Button) layout.findViewById(R.id.replay);
             Game game = getItem(position);
             int maxTotal = game.maxTotal();
 
             SpannableStringBuilder ssb = new SpannableStringBuilder();
             for (int p = 0, size = game.playerCount(); p < size; p++) {
                 if (p > 0) {
                     ssb.append("  ");
                 }
                 String name = game.playerName(p);
                 String total = Integer.toString(game.playerTotal(p));
                 ssb.append(name);
                 ssb.append("\u00a0");
 
                 ssb.append(total);
                 ssb.setSpan(new ForegroundColorSpan(game.playerColor(p)),
                         ssb.length() - total.length(), ssb.length(), 0);
                 if (maxTotal == game.playerTotal(p)) {
                     ssb.setSpan(new StyleSpan(Typeface.BOLD),
                             ssb.length() - total.length(), ssb.length(), 0);
                 }
             }
             players.setText(ssb);
 
             StringBuilder rounds = new StringBuilder();
            if (game.roundCount() == 0) {
                 rounds.append("1 round. ");
             } else {
                 rounds.append(Integer.toString(game.roundCount()));
                 rounds.append(" rounds. ");
             }
             rounds.append("Started ");
             rounds.append(DateUtils.getRelativeTimeSpanString(getApplicationContext(),
                     game.getDateStarted(), true));
             summary.setText(rounds);
 
             replay.setOnClickListener(replayListener);
             layout.setOnClickListener(resumeListener);
             layout.setFocusable(true);
             layout.setBackgroundResource(android.R.drawable.list_selector_background);
             return layout;
         }
     }
 }
