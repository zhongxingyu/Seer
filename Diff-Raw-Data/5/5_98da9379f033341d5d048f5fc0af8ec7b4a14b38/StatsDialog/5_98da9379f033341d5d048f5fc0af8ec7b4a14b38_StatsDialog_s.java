 package com.netwokz.tiktaktoe;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * Created by Stephen on 9/4/13.
  */
 public class StatsDialog extends DialogFragment {
 
     TextView mTotalPlayedView;
     TextView mTotalTimeView;
     TextView mTotalGamesWonView;
     TextView mTotalGamesLostView;
     TextView mTotalGamesTieView;
 
     SharedPreferences mPrefs;
     SharedPreferences.Editor mPrefsEditor;
 
     long mGamesPlayed;
     long mGameTime;
     long mGamesWon;
     long mGamesLost;
     long mGamesTie;
 
     private final String STAT_ALL_TIME_GAMES_PLAYED = "all_time_games_played";
     private final String STAT_ALL_TIME_GAMES_WON = "all_time_games_won";
     private final String STAT_ALL_TIME_GAMES_LOST = "all_time_games_lost";
     private final String STAT_ALL_TIME_TIME_PLAYED = "all_time_time_played";
     private final String STAT_ALL_TIME_GAMES_TIE = "all_time_games_tie";
 
     public static StatsDialog newInstance() {
         StatsDialog fragment = new StatsDialog();
         return fragment;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         View view = getActivity().getLayoutInflater().inflate(R.layout.stats, null);
         mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
         mPrefsEditor = mPrefs.edit();
 
         AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(getActivity());
         initializeViews(view);
        mDialogBuilder.setView(view);

         getStats();
         updateStatViews();

         return mDialogBuilder.create();
     }
 
     private void initializeViews(View view) {
         mTotalPlayedView = (TextView) view.findViewById(R.id.total_games);
         mTotalTimeView = (TextView) view.findViewById(R.id.total_time_value);
         mTotalGamesWonView = (TextView) view.findViewById(R.id.total_games_won);
         mTotalGamesLostView = (TextView) view.findViewById(R.id.total_games_lost);
         mTotalGamesTieView = (TextView) view.findViewById(R.id.total_games_tied);
     }
 
     public void getStats() {
         mGamesPlayed = mPrefs.getLong(STAT_ALL_TIME_GAMES_PLAYED, -1);
         mGameTime = mPrefs.getLong(STAT_ALL_TIME_GAMES_WON, -1);
         mGamesWon = mPrefs.getLong(STAT_ALL_TIME_GAMES_LOST, -1);
         mGamesLost = mPrefs.getLong(STAT_ALL_TIME_TIME_PLAYED, -1);
         mGamesTie = mPrefs.getLong(STAT_ALL_TIME_GAMES_TIE, -1);
     }
 
     public void updateStatViews() {
         mTotalPlayedView.setText(String.valueOf(mGamesPlayed));
         mTotalTimeView.setText(String.valueOf(mGameTime));
         mTotalGamesWonView.setText(String.valueOf(mGamesWon));
         mTotalGamesLostView.setText(String.valueOf(mGamesLost));
         mTotalGamesTieView.setText(String.valueOf(mGamesTie));
     }
 }
