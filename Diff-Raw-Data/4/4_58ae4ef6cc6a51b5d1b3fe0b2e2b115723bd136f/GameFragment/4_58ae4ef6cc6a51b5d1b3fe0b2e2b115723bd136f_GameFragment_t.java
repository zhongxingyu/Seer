 package com.austin.pinochletally;
 
 import com.austin.pinochletally.HistoryContract.HistoryEntry;
 
 import android.R.color;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class GameFragment extends Fragment implements OnClickListener {
 		
 	Integer mTeam1Score = 0;
 	Integer mTeam2Score = 0;
 	int[] mSavedTeam1Scores;
 	int[] mSavedTeam2Scores;
 
 	public GameFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_game,
 				container, false);
 		
 		// click listener
         Button addBtn = (Button) rootView.findViewById(R.id.add_scores);
         addBtn.setOnClickListener(this);
         Button undoBtn = (Button) rootView.findViewById(R.id.undo);
         undoBtn.setOnClickListener(this);
         Button clearBtn = (Button) rootView.findViewById(R.id.clear);
         clearBtn.setOnClickListener(this);
         
         EditText editText = (EditText) rootView.findViewById(R.id.team1_tricks);
         editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				boolean handled = false;
 		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
 		            int val = Integer.parseInt(v.getText().toString());
 		            EditText tricks2 = (EditText) getActivity().findViewById(R.id.team2_tricks);
 		            tricks2.setText(Integer.toString(250 - val));
 		            //handled = true;
 		        }
 		        return handled;
 			}
         });
         
         
 		return rootView;
 	}
 	
 	@Override
 	public void onViewStateRestored(Bundle savedInstanceState) {
 		super.onViewStateRestored(savedInstanceState);
 		
 
         if(((MainActivity)getActivity()).mTeam1Scores != null && ((MainActivity)getActivity()).mTeam2Scores != null) {
 		    for(int score : ((MainActivity)getActivity()).mTeam1Scores) {
 		    	addTeam1Score(score);
 		    }
 		    for(int score : ((MainActivity)getActivity()).mTeam2Scores) {
 		    	addTeam2Score(score);
 		    }
         }
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch(view.getId()) {
 		case R.id.add_scores:
 			addScores(view);
 			break;
 		case R.id.undo:
 			undo(view);
 			break;
 		case R.id.clear:
 			clear(view);
 		default:
 			break;
 		
 		}
 		
 	}
 	
 	public void errorToast(String errorMsg) {
 		Context context = getActivity().getApplicationContext();
 		int duration = Toast.LENGTH_SHORT;
 		
 		Toast toast = Toast.makeText(context, errorMsg, duration);
 		toast.show();
 	}
 	
 	// reset scores page entirely
 	public void clear(View view) {
 		mTeam1Score = 0;
 		mTeam2Score = 0;
 		LinearLayout scrollArea1 = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 		LinearLayout scrollArea2 = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 		scrollArea1.removeAllViews();
 		scrollArea2.removeAllViews();
 		scrollArea1.invalidate();
 		scrollArea2.invalidate();
 
 
 		// update total scores
 		TextView score1 = (TextView)getActivity().findViewById(R.id.team1_score);
 		score1.setText(mTeam1Score.toString());
 		
 		TextView score2 = (TextView)getActivity().findViewById(R.id.team2_score);
 		score2.setText(mTeam2Score.toString());
 		
 		// zero out various entries
 		EditText meld1 = (EditText)getActivity().findViewById(R.id.team1_meld);
 		EditText meld2 = (EditText)getActivity().findViewById(R.id.team2_meld);
 		EditText trick1 = (EditText)getActivity().findViewById(R.id.team1_tricks);
 		EditText trick2 = (EditText)getActivity().findViewById(R.id.team2_tricks);
 		EditText bid = (EditText)getActivity().findViewById(R.id.bid);
 		meld1.setText("");
 		trick1.setText("");
 		meld2.setText("");
 		trick2.setText("");
 		bid.setText("");
 	}
 	
 	// remove last set of scores entered
 	public void undo(View view) {
 		// make sure we have something to undo
 		LinearLayout scrollArea1 = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 		LinearLayout scrollArea2 = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 		if(scrollArea1.getChildCount() < 2 || scrollArea2.getChildCount() < 2) {
 			return;
 		}
 		
 		// get IDs of the last 2 text boxes in each LinearLayout
 		TextView v1 = (TextView) scrollArea1.getChildAt(scrollArea1.getChildCount() - 1);
 		TextView v2 = (TextView) scrollArea1.getChildAt(scrollArea1.getChildCount() - 2);
 		Integer toSubtract = Integer.parseInt(v1.getText().toString()) + Integer.parseInt(v2.getText().toString());
 		mTeam1Score -= toSubtract;
 		
 		// remove text boxes
 		scrollArea1.removeViewAt(scrollArea1.getChildCount() - 1);
 		scrollArea1.removeViewAt(scrollArea1.getChildCount() - 1);
 
 		// figure out scores to subtract
 		TextView total1 = (TextView)getActivity().findViewById(R.id.team1_score);
 		Integer newTotal = Integer.parseInt(total1.getText().toString()) - toSubtract;
 		total1.setText(newTotal.toString());
 		scrollArea1.invalidate();
 
 		TextView v3 = (TextView) scrollArea2.getChildAt(scrollArea2.getChildCount() - 1);
 		TextView v4 = (TextView) scrollArea2.getChildAt(scrollArea2.getChildCount() - 2);
 		toSubtract = Integer.parseInt(v3.getText().toString()) + Integer.parseInt(v4.getText().toString());
 		mTeam2Score -= toSubtract;
 		
 		// remove text boxes
 		scrollArea2.removeViewAt(scrollArea2.getChildCount() - 1);
 		scrollArea2.removeViewAt(scrollArea2.getChildCount() - 1);
 
 		// figure out scores to subtract
 		TextView total2 = (TextView)getActivity().findViewById(R.id.team2_score);
 		newTotal = Integer.parseInt(total2.getText().toString()) - toSubtract;
 		total2.setText(newTotal.toString());
 		scrollArea2.invalidate();
 	}
 	
 	// add to total scores based on values entered
 	public void addScores(View view) {
 		/*
 		 * lots of error checking
 		 * 		melds & tricks have numbers (fail = do nothing)
 		 * 		tricks add to 250 (fail = do nothing)
 		 * 		bid value entered (fail = do nothing)
 		 * 
 		 * add meld & trick scores to each team's scroll, recalc total score
 		 * if game over, announce win, ask about archiving game
 		 * 
 		 */
 		
 		if(mTeam1Score > 1500 || mTeam2Score > 1500) {
 			errorToast("Game already over. You may undo to add scores.");
 			return;
 		}
 		
 		EditText name1 = (EditText)getActivity().findViewById(R.id.team1name);
 		String name1val;
 		if(name1.getText().length() < 1) {
 			name1val = "Team 1";
 		}
 		else {
 			name1val = name1.getText().toString();
 		}
 		
 		EditText name2 = (EditText)getActivity().findViewById(R.id.team2name);
 		String name2val;
 		if(name2.getText().length() < 1) {
 			name2val = "Team 2";
 		}
 		else {
 			name2val = name2.getText().toString();
 		}
 		
 		EditText meld1 = (EditText)getActivity().findViewById(R.id.team1_meld);
 		if(meld1.getText().length() < 1) {
 			// error
 			errorToast("Invalid team 1 meld amount");
 			return;
 		}
 		int meld1val = Integer.parseInt(meld1.getText().toString());
 
 		EditText meld2 = (EditText)getActivity().findViewById(R.id.team2_meld);
 		if(meld2.getText().length() < 1) {
 			// error
 			errorToast("Invalid team 2 meld amount");
 			return;
 		}
 		int meld2val = Integer.parseInt(meld2.getText().toString());
 
 		EditText trick1 = (EditText)getActivity().findViewById(R.id.team1_tricks);
 		if(trick1.getText().length() < 1) {
 			// error
 			errorToast("Invalid team 1 trick amount");
 			return;
 		}
 		int trick1val = Integer.parseInt(trick1.getText().toString());
 
 		EditText trick2 = (EditText)getActivity().findViewById(R.id.team2_tricks);
 		if(trick2.getText().length() < 1) {
 			// error
 			errorToast("Invalid team 2 trick amount");
 			return;
 		}
 		int trick2val = Integer.parseInt(trick2.getText().toString());
 
 		EditText bid = (EditText)getActivity().findViewById(R.id.bid);
 		if(bid.getText().length() < 3) {
 			// error
 			errorToast("Invalid bid amount");
 			return;
 		}
 		int bidval = Integer.parseInt(bid.getText().toString());
 		
 		ToggleButton team = (ToggleButton)getActivity().findViewById(R.id.bidding_team);
 		Boolean team1 = team.isChecked();
 		
 		
 		if(trick1val + trick2val != 250) {
 			// error
 			errorToast("Trick values do not add to 250");
 			return;
 		}
 		if(bidval < 250) {
 			// error
 			errorToast("Bid value below 250");
 			return;
 		}
 		
 		if(team1) {
 			if(bidval > meld1val + trick1val) {
 				// set
 				addTeam1Score(0);
 				addTeam1Score(-1 * bidval);
 			}
 			else {
 				// made it
 				addTeam1Score(meld1val);
 				addTeam1Score(trick1val);
 			}
 			if(trick2val > 0) {
 				// saved meld
 				addTeam2Score(meld2val);
 				addTeam2Score(trick2val);
 			}
 		}
 		else {
 			if(bidval > meld2val + trick2val) {
 				// set
 				addTeam2Score(0);
 				addTeam2Score(-1 * bidval);
 			}
 			else {
 				// made it
 				addTeam2Score(meld2val);
 				addTeam2Score(trick2val);
 			}
 			if(trick1val > 0) {
 				// saved meld
 				addTeam1Score(meld1val);
 				addTeam1Score(trick1val);
 			}
 		}
 
 		
 		// zero out various entries
 		meld1.setText("");
 		trick1.setText("");
 		meld2.setText("");
 		trick2.setText("");
 		bid.setText("");
 		
 		// check if game has ended
 		if((team1 && mTeam1Score > 1500) || (mTeam1Score > 1500 && mTeam2Score < 1500)) {
 			// bid and go out, or don't bid and go out when the other team doesn't
 			
 			HistoryDbHelper mDbHelper = new HistoryDbHelper(getActivity());
 			SQLiteDatabase db = mDbHelper.getWritableDatabase();
 			
 			// Create a new map of values, where column names are the keys
 			ContentValues values = new ContentValues();
 			values.put(HistoryEntry.COLUMN_NAME_WINNING_TEAM, name1val);
 			values.put(HistoryEntry.COLUMN_NAME_WINNING_SCORE, mTeam1Score);
 			values.put(HistoryEntry.COLUMN_NAME_LOSING_TEAM, name2val);
 			values.put(HistoryEntry.COLUMN_NAME_LOSING_SCORE, mTeam2Score);
 			
 			// Insert the new row, returning the primary key value of the new row
 			long newRowId;
 			newRowId = db.insert(
 			         HistoryEntry.TABLE_NAME,
 			         null,
 			         values);
 			
 			// Add to the history fragment
 			HistoryFragment historyFrag = (HistoryFragment)getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
 			historyFrag.addGameEntry(name1val, name2val, Integer.toString(mTeam1Score), Integer.toString(mTeam2Score));
 			
 			errorToast("Team 1 wins! Game saved to History");
 		}
 		else if(mTeam2Score > 1500) {
 			// other team didn't win, and we went out, so we win
 			
 			HistoryDbHelper mDbHelper = new HistoryDbHelper(getActivity());
 			SQLiteDatabase db = mDbHelper.getWritableDatabase();
 			
 			// Create a new map of values, where column names are the keys
 			ContentValues values = new ContentValues();
 			values.put(HistoryEntry.COLUMN_NAME_WINNING_TEAM, name2val);
 			values.put(HistoryEntry.COLUMN_NAME_WINNING_SCORE, mTeam2Score);
 			values.put(HistoryEntry.COLUMN_NAME_LOSING_TEAM, name1val);
 			values.put(HistoryEntry.COLUMN_NAME_LOSING_SCORE, mTeam1Score);
 			
 			// Insert the new row, returning the primary key value of the new row
 			long newRowId;
 			newRowId = db.insert(
 			         HistoryEntry.TABLE_NAME,
 			         null,
 			         values);
 			
			// Add to the history fragment
			HistoryFragment historyFrag = (HistoryFragment)getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
			historyFrag.addGameEntry(name2val, name1val, Integer.toString(mTeam2Score), Integer.toString(mTeam1Score));
			
 			errorToast("Team 2 wins! Game saved to History");
 		}
 	}
 	
 	private void addTeam1Score(Integer value) {
 		mTeam1Score += value;
 		LinearLayout scrollArea = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 		// add textview
 		TextView newScore = new TextView(getActivity());
 		newScore.setText(value.toString());
 		
 		scrollArea.addView(newScore);
 		
 		// update total scores
 		TextView score1 = (TextView)getActivity().findViewById(R.id.team1_score);
 		score1.setText(mTeam1Score.toString());
 	}
 	
 	private void addTeam2Score(Integer value) {
 		mTeam2Score += value;
 		LinearLayout scrollArea = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 		// add textview
 		TextView newScore = new TextView(getActivity());
 		newScore.setText(value.toString());
 		
 		scrollArea.addView(newScore);
 		
 		TextView score2 = (TextView)getActivity().findViewById(R.id.team2_score);
 		score2.setText(mTeam2Score.toString());
 	}
 }
