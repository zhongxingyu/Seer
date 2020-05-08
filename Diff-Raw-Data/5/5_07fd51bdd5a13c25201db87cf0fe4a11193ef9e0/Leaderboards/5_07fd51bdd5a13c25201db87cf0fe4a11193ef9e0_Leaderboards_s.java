 package com.oakonell.chaotictactoe;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gms.games.GamesClient;
 import com.google.android.gms.games.leaderboard.LeaderboardVariant;
 import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
 import com.google.android.gms.games.leaderboard.SubmitScoreResult;
 import com.google.android.gms.games.leaderboard.SubmitScoreResult.Result;
 import com.oakonell.chaotictactoe.googleapi.GameHelper;
 import com.oakonell.chaotictactoe.model.Game;
 import com.oakonell.chaotictactoe.model.GameMode;
 import com.oakonell.chaotictactoe.model.Marker;
 import com.oakonell.chaotictactoe.model.ScoreCard;
 import com.oakonell.chaotictactoe.model.State;
 
 public class Leaderboards {
 
 	public List<String> getLeaderboardIds(Context context) {
 		List<String> result = new ArrayList<String>();
 		result.add(context
 				.getString(R.string.leaderboard_shortest_chaotic_game));
 		result.add(context
 				.getString(R.string.leaderboard_longest_chaotic_mode_game));
 		result.add(context
 				.getString(R.string.leaderboard_most_wins_in_a_session));
 		return result;
 	}
 
 	public void submitGame(GameHelper gameHelper, final Context context,
 			Game game, State outcome, ScoreCard score) {
 		if (game.getMode() == GameMode.PASS_N_PLAY) {
 			return;
 		}
 		if (outcome.isDraw())
 			return;
 		
 		if (!game.getLocalPlayer().equals(outcome.getWinner())) {
 			return;
 		}
 
 		GamesClient gamesClient = gameHelper.getGamesClient();
 		int wins = 0;
 		if (game.getLocalPlayer().getMarker() == Marker.X) {
 			wins = score.getXWins();
 		} else {
 			wins = score.getOWins();
 		}
 		submitMostWins(context, gamesClient, wins);
 		
 		if (!game.getMarkerChance().isChaotic()) {
 			return;
 		}
 		int numMoves = game.getNumberOfMoves();
 
 		submitLongestGame(context, gamesClient, numMoves);
 		submitShortestGame(context, gamesClient, numMoves);
 
 	}
 
 	
 	private void submitMostWins(final Context context,
 			GamesClient gamesClient, int submittedScore) {
 		final String id = context
 				.getString(R.string.leaderboard_most_wins_in_a_session);
 		gamesClient.submitScoreImmediate(new BasicOnScoreSubmittedListener(
 				context, id) {
 
 			@Override
 			protected void beatDaily(final Context context, Result daily) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_daily_wins, daily.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatWeekly(final Context context, Result weekly) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_weekly_wins, weekly.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatAllTime(final Context context, Result allTime) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_alltime_wins, allTime.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 		}, id, submittedScore);
 	}
 
 	
 	private void submitShortestGame(final Context context,
 			GamesClient gamesClient, int submittedScore) {
 		final String id = context
 				.getString(R.string.leaderboard_shortest_chaotic_game);
 		gamesClient.submitScoreImmediate(new BasicOnScoreSubmittedListener(
 				context, id) {
 
 			@Override
 			protected void beatDaily(Context context, Result daily) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_daily_min, daily.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatWeekly(Context context, Result weekly) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_weekly_min, weekly.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatAllTime(Context context, Result allTime) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_alltime_min, allTime.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 		}, id, submittedScore);
 	}
 
 	private void submitLongestGame(final Context context,
 			GamesClient gamesClient, int submittedScore) {
 		final String id = context
 				.getString(R.string.leaderboard_longest_chaotic_mode_game);
 		gamesClient.submitScoreImmediate(new BasicOnScoreSubmittedListener(
 				context, id) {
 
 			@Override
 			protected void beatDaily(final Context context, Result daily) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_daily_max, daily.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatWeekly(final Context context, Result weekly) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_weekly_max, weekly.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			protected void beatAllTime(final Context context, Result allTime) {
 				Toast.makeText(
 						context,
 						context.getResources().getString(
 								R.string.beat_alltime_max, allTime.rawScore),
 						Toast.LENGTH_SHORT).show();
 			}
 		}, id, submittedScore);
 	}
 
 	public abstract static class BasicOnScoreSubmittedListener implements
 			OnScoreSubmittedListener {
 		private Context context;
 		private String id;
 
 		BasicOnScoreSubmittedListener(Context context, String id) {
 			this.id = id;
 			this.context = context;
 		}
 
 		@Override
 		public void onScoreSubmitted(int status, SubmitScoreResult result) {
 			if (status != GamesClient.STATUS_OK) {
 				// report an error?
 				Log.e("Leaderboard", "Error submitting leaderboard score- "
 						+ status);
 				return;
 			}
 			if (!result.getLeaderboardId().equals(id))
 				return;
 			onScoreSubmitSuccess(result);
 		}
 
 		protected void onScoreSubmitSuccess(SubmitScoreResult result) {
 			Result allTime = result
 					.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
 			if (allTime != null && allTime.newBest) {
 				beatAllTime(context, allTime);
 				return;
 			}
 			Result weekly = result
 					.getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY);
 			if (weekly != null && weekly.newBest) {
 				beatWeekly(context, weekly);
 				return;
 			}
 			Result daily = result
 					.getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY);
 			if (daily != null && daily.newBest) {
 				beatDaily(context, daily);
 				return;
 			}
 		}
 
 		protected void beatDaily(final Context context, Result daily) {
 		}
 
 		protected void beatWeekly(final Context context, Result weekly) {
 		}
 
 		protected void beatAllTime(final Context context, Result allTime) {
 		}
 	}
 }
