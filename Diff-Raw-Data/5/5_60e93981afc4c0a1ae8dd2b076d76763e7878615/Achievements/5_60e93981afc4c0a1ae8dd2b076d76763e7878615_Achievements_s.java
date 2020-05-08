 package com.oakonell.chaotictactoe;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.widget.Toast;
 
 import com.google.android.gms.games.GamesClient;
 import com.oakonell.chaotictactoe.googleapi.GameHelper;
 import com.oakonell.chaotictactoe.model.Board;
 import com.oakonell.chaotictactoe.model.Cell;
 import com.oakonell.chaotictactoe.model.Game;
 import com.oakonell.chaotictactoe.model.GameMode;
 import com.oakonell.chaotictactoe.model.Marker;
 import com.oakonell.chaotictactoe.model.Player;
 import com.oakonell.chaotictactoe.model.State;
 
 public class Achievements {
 	private static final int NUM_MOVES_LONG_HAUL = 20;
 	private static final int NUM_MOVES_BEFORE_CLEAN_SLATE = 5;
 	protected static final int NUM_BOARD_REVISITS_FOR_DEJA_VU = 3;
 
 	private BooleanAchievement friendlyFire = new BooleanAchievement(
 			R.string.achievement_friendly_fire,
 			R.string.offline_achievement_friendly_fire) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (!outcome.getLastMove().getPlayer()
 					.equals(game.getLocalPlayer())) {
 				return;
 			}
 			if (outcome.getLastMove().getPlayedMarker() != Marker.EMPTY) {
 				return;
 			}
 			Player localPlayer = game.getLocalPlayer();
 
 			if (outcome.getLastMove().getPreviousMarker() != localPlayer
 					.getMarker()) {
 				return;
 			}
 
 			// if there are ANY opponent markers, this was a friendly fire
 			Marker opponentMarker = localPlayer.opponent().getMarker();
 			int count = 0;
 			Board board = game.getBoard();
 
 			int size = board.getSize();
 			for (int x = 0; x < size; ++x) {
 				for (int y = 0; y < size; ++y) {
 					Marker boardMarker = board.getCell(x, y);
 					if (boardMarker == opponentMarker) {
 						count++;
 					}
 				}
 			}
 
 			if (count > 0) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private BooleanAchievement fork = new BooleanAchievement(
 			R.string.achievement_fork_in_the_road,
 			R.string.offline_achievement_fork_in_the_road) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (outcome.isDraw())
 				return;
 			Player localPlayer = game.getLocalPlayer();
 			if (!outcome.getWinner().equals(localPlayer)) {
 				return;
 			}
 			if (!outcome.getLastMove().getPlayer().equals(outcome.getWinner())) {
 				return;
 			}
 			Marker marker = outcome.getWinner().getMarker();
 			// count possible wins from the previous state
 			// if at least two wins for this marker is possible, it is a fork
 			int count = 0;
 			Board board = game.getBoard().copy();
 
 			board.clearMarker(outcome.getLastMove().getCell(),
 					game.getLocalPlayer());
 			int size = board.getSize();
 			for (int x = 0; x < size; ++x) {
 				for (int y = 0; y < size; ++y) {
 					Marker boardMarker = board.getCell(x, y);
 					if (boardMarker != Marker.EMPTY)
 						continue;
 
 					Cell cell = new Cell(x, y);
 					State localOutcome = board.placeMarker(cell, localPlayer,
 							marker);
 					if (localOutcome.getWinner() == localPlayer) {
 						count++;
 					}
 					board.clearMarker(cell, localPlayer);
 				}
 			}
 
 			if (count > 1) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private BooleanAchievement oops = new BooleanAchievement(
 			R.string.achievement_oops, R.string.offline_achievement_oops) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isReverse()
 					|| game.getMarkerChance().isNormal()) {
 				return;
 			}
 			if (game.getMode() == GameMode.PASS_N_PLAY) {
 				return;
 			}
 			if (outcome.isDraw()) {
 				return;
 			}
 			if (outcome.getWinner().equals(game.getLocalPlayer())) {
 				return;
 			}
 			if (outcome.getWinner().equals(outcome.getLastMove().getPlayer())) {
 				return;
 			}
 
 			Player localPlayer = game.getLocalPlayer();
 			Marker marker = outcome.getWinner().getMarker();
 			// count possible wins from the previous state
 			// if num wins is less than possible moves for this marker, it was a
 			// mistake
 			int possibleMoves = 0;
 			int opponentWins = 0;
 			Board board = game.getBoard().copy();
 
 			board.clearMarker(outcome.getLastMove().getCell(),
 					game.getLocalPlayer());
 			int size = board.getSize();
 			for (int x = 0; x < size; ++x) {
 				for (int y = 0; y < size; ++y) {
 					Marker boardMarker = board.getCell(x, y);
 					if (boardMarker != Marker.EMPTY)
 						continue;
 					possibleMoves++;
 
 					Cell cell = new Cell(x, y);
 					State localOutcome = board.placeMarker(cell, localPlayer,
 							marker);
 					if (localOutcome.getWinner().equals(localPlayer.opponent())) {
 						opponentWins++;
 					}
 					board.clearMarker(cell, localPlayer);
 				}
 			}
 
 			if (opponentWins < possibleMoves) {
 				unlock(gameHelper, context);
 			}
 		}
 
 	};
 	private BooleanAchievement goodSport = new BooleanAchievement(
 			R.string.achievement_good_sport,
 			R.string.offline_achievement_good_sport) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isReverse()) {
 				return;
 			}
 			if (game.getMode() == GameMode.PASS_N_PLAY) {
 				return;
 			}
 			if (outcome.isDraw()) return;
 			if (outcome.getWinner().equals(game.getLocalPlayer())) {
 				return;
 			}
 			if (outcome.getLastMove().getPlayer().equals(outcome.getWinner())) {
 				return;
 			}
 
 			if (outcome.getWinner() == null) {
 				return;
 			}
 
 			if (game.getBoard().isFull()) {
 				unlock(gameHelper, context);
 			}
 		}
 
 	};
 	private BooleanAchievement twoBirds = new BooleanAchievement(
 			R.string.achievement_two_birds_with_one_stone,
 			R.string.offline_achievement_two_birds_with_one_stone) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (outcome.isDraw()) return;
 			if (!outcome.getWinner().equals(game.getLocalPlayer()))
 				return;
 			if (!outcome.getLastMove().getPlayer().equals(outcome.getWinner())) {
 				return;
 			}
 
 			if (outcome.getWins().size() != 2) {
 				return;
 			}
 
 			unlock(gameHelper, context);
 
 		}
 	};
 
 	private BooleanAchievement goodSamaritan = new BooleanAchievement(
 			R.string.achievement_the_good_samaritan,
 			R.string.offline_achievement_the_good_samaritan) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMode() == GameMode.PASS_N_PLAY) {
 				return;
 			}
 			if (outcome.isDraw()) return;
 			if (!outcome.getWinner().equals(game.getLocalPlayer())) {
 				return;
 			}
 			if (!outcome.getLastMove().getPlayer().equals(outcome.getWinner())) {
 				return;
 			}
 
 			if (game.getNumberOfMoves() == game.getBoard().getSize()) {
 				unlock(gameHelper, context);
 			}
 		}
 
 	};
 	private BooleanAchievement reverseWin = new BooleanAchievement(
 			R.string.achievement_reverse_win,
 			R.string.offline_achievement_reverse_win) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (!game.getMarkerChance().isReverse()) {
 				return;
 			}
 			if (game.getMode() == GameMode.PASS_N_PLAY)
 				return;
 			if (outcome.isDraw()) return;
 			if (!outcome.getWinner().equals(game.getLocalPlayer()))
 				return;
 
 			if (game.getMarkerChance().isReverse()) {
 				unlock(gameHelper, context);
 			}
 		}
 
 	};
 
 	private BooleanAchievement onlyXsOrOs = new BooleanAchievement(
 			R.string.achievement_only_xs_or_os,
 			R.string.offline_achievement_only_xs_or_os) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (outcome.getWinner() != game.getLocalPlayer())
 				return;
 
 			Board board = game.getBoard();
 			int size = board.getSize();
 			int numX = 0;
 			int numO = 0;
 			for (int x = 0; x < size; x++) {
 				for (int y = 0; y < size; y++) {
 					Marker marker = board.getCell(x, y);
 					if (marker == Marker.X) {
 						numX++;
 					} else if (marker == Marker.O) {
 						numO++;
 					}
 					if (numO > 0 && numX > 0)
 						break;
 				}
 			}
 			if (numX == 0 || numO == 0) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private BooleanAchievement chaoticDraw = new BooleanAchievement(
 			R.string.achievement_chaotic_draw,
 			R.string.offline_achievement_chaotic_draw) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMode() == GameMode.PASS_N_PLAY)
 				return;
 
 			if (game.getMarkerChance().isChaotic()) {
 				if (outcome.isDraw()) {
 					unlock(gameHelper, context);
 				}
 			}
 		}
 
 	};
 	private BooleanAchievement dejaVu = new BooleanAchievement(
 			R.string.achievement_dejavu, R.string.offline_achievement_deja_vu) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMode() == GameMode.PASS_N_PLAY)
 				return;
 
 			if (game.getBoard().isEmpty())
 				return;
 			if (game.getNumberOfTimesInThisState() > NUM_BOARD_REVISITS_FOR_DEJA_VU) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private BooleanAchievement longHaul = new BooleanAchievement(
 			R.string.achievement_the_long_haul,
 			R.string.offline_achievement_long_haul) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMode() == GameMode.PASS_N_PLAY)
 				return;
 
 			if (game.getMarkerChance().isChaotic()
 					&& game.getNumberOfMoves() > NUM_MOVES_LONG_HAUL) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private BooleanAchievement withALittleHelp = new BooleanAchievement(
 			R.string.achievement_with_a_little_help_from_my_friends,
 			R.string.offline_achievement_with_a_little_help) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (outcome.isDraw()) return;
 			if (!outcome.getWinner().equals(game.getLocalPlayer()))
 				return;
 			if (!outcome.getLastMove().getPlayer().equals(outcome.getWinner())) {
 				return;
 			}
 
 			if (game.getNumberOfMoves() == game.getBoard().getSize()) {
 				unlock(gameHelper, context);
 			}
 		}
 
 	};
 	private BooleanAchievement cleanSlate = new BooleanAchievement(
 			R.string.achievement_a_clean_slate,
			R.string.offline_achievement_chaotic_draw) {
 
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMode() == GameMode.PASS_N_PLAY)
 				return;
 			if (game.getNumberOfMoves() >= NUM_MOVES_BEFORE_CLEAN_SLATE
 					&& game.getBoard().isEmpty()) {
 				unlock(gameHelper, context);
 			}
 		}
 	};
 
 	private IncrementalAchievement plainJaneCount = new IncrementalAchievement(
 			R.string.achievement_plain_jane) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isNormal()) {
 				increment(gameHelper, context);
 			}
 		}
 	};
 	private IncrementalAchievement chaoticCount = new IncrementalAchievement(
 			R.string.achievement_chaos_theory) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isChaotic()) {
 				increment(gameHelper, context);
 			}
 		}
 	};
 
 	private IncrementalAchievement reversiCount = new IncrementalAchievement(
 			R.string.achievement_reversi) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isReverse()) {
 				increment(gameHelper, context);
 			}
 		}
 	};
 
 	private IncrementalAchievement customCount = new IncrementalAchievement(
 			R.string.achievement_the_customer) {
 		@Override
 		public void testAndSet(GameHelper gameHelper, Context context,
 				Game game, State outcome) {
 			if (game.getMarkerChance().isCustom()) {
 				increment(gameHelper, context);
 			}
 		}
 	};
 
 	private List<Achievement> endGameAchievements = new ArrayList<Achievements.Achievement>();
 	private List<Achievement> inGameAchievements = new ArrayList<Achievements.Achievement>();
 
 	public Achievements() {
 		inGameAchievements.add(dejaVu);
 		inGameAchievements.add(cleanSlate);
 		inGameAchievements.add(friendlyFire);
 
 		endGameAchievements.add(onlyXsOrOs);
 		endGameAchievements.add(chaoticDraw);
 		endGameAchievements.add(longHaul);
 		endGameAchievements.add(withALittleHelp);
 
 		endGameAchievements.add(reverseWin);
 		endGameAchievements.add(goodSamaritan);
 		endGameAchievements.add(twoBirds);
 		endGameAchievements.add(goodSport);
 		endGameAchievements.add(oops);
 		endGameAchievements.add(fork);
 
 		endGameAchievements.add(plainJaneCount);
 		endGameAchievements.add(chaoticCount);
 		endGameAchievements.add(reversiCount);
 		endGameAchievements.add(customCount);
 	}
 
 	private interface Achievement {
 		void push(GamesClient client, Context context);
 
 		void testAndSet(GameHelper gameHelper, Context context, Game game,
 				State outcome);
 
 		boolean isPending();
 	}
 
 	private abstract static class BooleanAchievement implements Achievement {
 		private boolean value = false;
 		private final int achievementId;
 		private final int stringId;
 
 		BooleanAchievement(int achievementId, int stringId) {
 			this.achievementId = achievementId;
 			this.stringId = stringId;
 		}
 
 		@Override
 		public boolean isPending() {
 			return value;
 		}
 
 		public void push(GamesClient client, Context context) {
 			if (value) {
 				client.unlockAchievement(context.getString(achievementId));
 				value = false;
 			}
 		}
 
 		public void unlock(GameHelper helper, Context context) {
 			if (helper.isSignedIn()) {
 				helper.getGamesClient().unlockAchievement(
 						context.getString(achievementId));
 			} else {
 				if (!value) {
 					Toast.makeText(
 							context,
 							context.getString(R.string.offline_achievement_label)
									+ ": " + context.getString(stringId),
 							Toast.LENGTH_LONG).show();
 				}
 				value = true;
 			}
 		}
 	}
 
 	private static abstract class IncrementalAchievement implements Achievement {
 		private int count = 0;
 		private final int achievementId;
 
 		IncrementalAchievement(int achievementId) {
 			this.achievementId = achievementId;
 		}
 
 		@Override
 		public boolean isPending() {
 			return count > 0;
 		}
 
 		public void push(GamesClient client, Context context) {
 			if (count > 0) {
 				client.incrementAchievement(context.getString(achievementId),
 						count);
 				count = 0;
 			}
 		}
 
 		public void increment(GameHelper helper, Context context) {
 			if (helper.isSignedIn()) {
 				helper.getGamesClient().incrementAchievement(
 						context.getString(achievementId), 1);
 			} else {
 				count++;
 			}
 		}
 	}
 
 	public boolean hasPending() {
 		for (Achievement each : endGameAchievements) {
 			if (each.isPending())
 				return true;
 		}
 		for (Achievement each : inGameAchievements) {
 			if (each.isPending())
 				return true;
 		}
 		return false;
 	}
 
 	public void pushToGoogle(GameHelper helper, Context context) {
 		if (!helper.isSignedIn())
 			return;
 
 		GamesClient client = helper.getGamesClient();
 		for (Achievement each : endGameAchievements) {
 			each.push(client, context);
 		}
 		for (Achievement each : inGameAchievements) {
 			each.push(client, context);
 		}
 	}
 
 	public void testAndSetForInGameAchievements(GameHelper gameHelper,
 			Context context, Game game, State outcome) {
 		for (Achievement each : inGameAchievements) {
 			each.testAndSet(gameHelper, context, game, outcome);
 		}
 	}
 
 	public void testAndSetForGameEndAchievements(GameHelper gameHelper,
 			Context context, Game game, State outcome) {
 		for (Achievement each : endGameAchievements) {
 			each.testAndSet(gameHelper, context, game, outcome);
 		}
 	}
 }
