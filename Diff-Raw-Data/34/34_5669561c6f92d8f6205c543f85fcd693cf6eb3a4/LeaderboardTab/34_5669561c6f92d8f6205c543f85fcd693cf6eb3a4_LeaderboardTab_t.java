 package com.example.zootypers.ui;
 
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
import com.example.zootypers.core.ScoreEntry;

 /**
  * Represents a Tab in the leaderboard layout
  * @author ZooTypers
  *
  */
 public class LeaderboardTab extends Fragment {
 	
 
 	private final int MAX_LENGTH = 10;
 
 	/**
 	 * create a new view for the fragment
 	 * 
 	 */
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		}
 		return container;
 	}
 	
 	/**
 	 * sets up the leaderboard list using the given array of entries
 	 * @param view the leaderboard view
 	 * @param seArray the list of entries to put in the leaderboard
 	 */
 	protected void setupLBList(View view, ScoreEntry[] seArray) {
 		int size = seArray.length;
 		if (seArray.length > 10) {
 			size = MAX_LENGTH;
 		}
 		for (int i = 0; i < size; i++) {
 			// get the text boxes with for the leaderboard
 			TextView currentPlayerText = (TextView) getByStringId(view, 
 					"player_name" + i);
 			TextView currentScoreText = (TextView) getByStringId(view,
 					"player_score" + i);
 
 			// get the entries from the ScoreEntry
 			ScoreEntry currentSE = seArray[i];
 			String currentPlayer = currentSE.getName();
 			String currentScore = currentSE.getScore() + "";
 			
 			// set the text fields
 			currentPlayerText.setText(currentPlayer);
 			currentScoreText.setText(currentScore);
 		}
 	}
 	
 	/**
 	 * sets up the leaderboard list using the given array of entries, rank, and relativeRank.
 	 * used for relative score fragment
 	 * @param view the leaderboard view
 	 * @param seArray the list of entries to put in the leaderboard
 	 * @param rank the ranking position of the current user
 	 * @param how many users before and after to show relative to the current user
 	 */
 	protected void setupLBList(View view, ScoreEntry[] seArray, int rank, int relativeRank) {
 
 		// find the position of the user to highlight
 		int userPosition = relativeRank;
 		if (rank + relativeRank < MAX_LENGTH) {
 			int temp = rank + relativeRank;
 			int offset = MAX_LENGTH - temp + 1;
 			userPosition -= offset;
 		}
 		
 		// find the rank to start the leaderboard at
 		int startRank = rank - 5;
 		if (startRank < 1) {
 			startRank = 1;
 		}
 
 		// fill out the ranks of the leaderboard
 		for (int j = 0; j < MAX_LENGTH; j++) {
 			TextView currentRank = (TextView) getByStringId(view,
 					"relative_rank" + j);
 			String currentRankString = startRank + "";
 			if (j == userPosition) {
 				// mark the current user position
 				currentRankString = "<b>" + startRank + "</b>";
 			}
 			
 			currentRank.setText(Html.fromHtml(currentRankString));
 			startRank++;
 		}
 		
 		// find the size of the list to display
 		int size = seArray.length;
 		if (size > 10) {
 			size = MAX_LENGTH;
 		}
 				
 		for (int i = 0; i < size; i++) {
 			// get the text boxes with for the leaderboard
 			TextView currentPlayerText = (TextView) getByStringId(view, 
 					"player_name" + i);
 			TextView currentScoreText = (TextView) getByStringId(view,
 					"player_score" + i);
 
 			// get the entries from the ScoreEntry
 			ScoreEntry currentSE = seArray[i];
 			String currentPlayer = currentSE.getName();;
 			String currentScore = currentSE.getScore() + "";
 			if (i == userPosition) {
 				// mark the current user
 				currentPlayer = "<b>" + currentPlayer + "</b>";
 				currentScore = "<b>" + currentScore + "</b>";
 			}
 			
 			currentPlayerText.setText(Html.fromHtml(currentPlayer));
 			currentScoreText.setText(Html.fromHtml(currentScore));
 		}
 	}
 	/**
 	 * @param id The id of the View to get as a String.
 	 * @return The View object with that id
 	 */
 	private final View getByStringId(View view, final String id) {
 		return view.findViewById(getResources().getIdentifier(id, "id", getActivity().getPackageName()));
 	}
 }
