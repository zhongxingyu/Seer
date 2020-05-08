 package org.goplayer.game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.goplayer.exception.NotFreeCoordException;
 import org.goplayer.exception.SuicideException;
 import org.goplayer.exception.UnknownPlayerException;
 import org.goplayer.go.Goban;
 import org.goplayer.go.Stone;
 import org.goplayer.go.StoneColor;
 import org.goplayer.move.AbandonMove;
 import org.goplayer.move.IMove;
 import org.goplayer.move.PassMove;
 import org.goplayer.move.StoneMove;
 import org.goplayer.player.DeterminedPlayer;
 import org.goplayer.player.IPlayer;
 import org.goplayer.util.Coord;
 import org.goplayer.util.MoveHistory;
 
 // TODO manage ko rule
 public class Game {
 
 	private final Goban goban;
 	private final Map<StoneColor, IPlayer> players = new HashMap<StoneColor, IPlayer>();
 	private final Map<StoneColor, Integer> lostStones = new HashMap<StoneColor, Integer>();
 	private StoneColor nextPlayerColor = StoneColor.BLACK;
 	private boolean previousHasPassed = false;
 	private IPlayer winner = null;
 	private final MoveHistory history = new MoveHistory();
 
 	public Game(Goban goban, IPlayer blackPlayer, IPlayer whitePlayer) {
 		if (getRunningGameOn(goban) != null) {
 			throw new IllegalArgumentException(
 					"The given goban is already in a running game.");
 		} else {
 			this.goban = goban;
 			players.put(StoneColor.BLACK, blackPlayer);
 			players.put(StoneColor.WHITE, whitePlayer);
 			lostStones.put(StoneColor.BLACK, 0);
 			lostStones.put(StoneColor.WHITE, 0);
 			games.add(this);
 		}
 	}
 
 	public Goban getGoban() {
 		return goban;
 	}
 
 	public IPlayer getPlayer(StoneColor color) {
 		return players.get(color);
 	}
 
 	public int getLostStonesCount(StoneColor color) {
 		return lostStones.get(color);
 	}
 
 	private void captureStones() {
 		Set<Block> blocks = Block.getAllBlocks(goban);
 		Stone lastStone = history.getLast().getStone();
 		for (Block block : blocks) {
 			if (block.contains(lastStone)) {
 				/*
 				 * Do not treat the block containing the last stone before the
 				 * end. This stone has the priority over the capturing, so other
 				 * blocks may be captured before.
 				 */
 				continue;
 			} else {
 				captureBlockIfSurrounded(block);
 			}
 		}
 
 		/*
 		 * Re-check the block of the last stone. If well played, it should be
 		 * not captured.
 		 */
 		Block block = Block.getBlockCovering(goban, history.getLast()
 				.getCoord());
 		captureBlockIfSurrounded(block);
 	}
 
 	private void captureBlockIfSurrounded(Block block) {
 		if (block.getLiberties().isEmpty()) {
 			StoneColor color = block.getColor();
 			lostStones.put(color, lostStones.get(color) + block.size());
 			for (Stone stone : block) {
 				goban.setCoordContent(goban.getStoneCoord(stone), null);
 			}
 		} else {
 			// do nothing
 		}
 	}
 
 	public StoneColor getPlayerColor(final IPlayer player) {
 		for (StoneColor color : StoneColor.values()) {
 			final IPlayer p1 = players.get(color);
 			if (p1 == player) {
 				return color;
 			} else {
 				continue;
 			}
 		}
 		throw new UnknownPlayerException(player);
 	}
 
 	public StoneColor getNextPlayerColor() {
 		return nextPlayerColor;
 	}
 
 	public void setNextPlayerColor(StoneColor color) {
 		nextPlayerColor = color;
 	}
 
 	public IPlayer getNextPlayer() {
 		return players.get(getNextPlayerColor());
 	}
 
 	public void setNextPlayer(IPlayer player) {
 		setNextPlayerColor(getPlayerColor(player));
 	}
 
 	public void finish(IPlayer winner) {
 		if (players.containsValue(winner)) {
 			this.winner = winner;
 		} else {
 			throw new UnknownPlayerException(winner);
 		}
 	}
 
 	public boolean isFinished() {
 		return winner != null;
 	}
 
 	public void play() {
 		if (isFinished()) {
 			throw new RuntimeException("The game is finished, nobody can play");
 		} else {
 			IPlayer player = getNextPlayer();
 			IMove move = player.play(getGoban());
 			if (move instanceof StoneMove) {
 				Coord coord = ((StoneMove) move).getCoord();
 				StoneColor color = getPlayerColor(player);
 				if (getGoban().getCoordContent(coord) != null) {
 					throw new NotFreeCoordException(color, coord);
 				} else if (!isSuicideAllowed()
 						&& isSuicide(nextPlayerColor, coord)) {
 					throw new SuicideException(color, coord);
 				} else {
 					Stone stone = new Stone(nextPlayerColor);
 					getGoban().setCoordContent(coord, stone);
 					history.add(coord, stone);
 					captureStones();
 				}
 				previousHasPassed = false;
 			} else if (move instanceof PassMove) {
 				if (previousHasPassed) {
 					finish(computeWinner());
 				} else {
 					previousHasPassed = true;
 				}
 			} else if (move instanceof AbandonMove) {
 				List<IPlayer> remaining = new ArrayList<IPlayer>(
 						players.values());
 				remaining.remove(player);
 				finish(remaining.get(0));
 			} else {
 				throw new RuntimeException("Not managed case: "
 						+ move.getClass());
 			}
 			nextPlayerColor = nextPlayerColor == StoneColor.BLACK ? StoneColor.WHITE
 					: StoneColor.BLACK;
 		}
 	}
 
 	private IPlayer computeWinner() {
 		// TODO compute winner using user feedback
 		return players.get(StoneColor.BLACK);
 	}
 
 	public IPlayer getWinner() {
 		return winner;
 	}
 
 	public MoveHistory getHistory() {
 		return history.clone();
 	}
 
 	private static final Set<Game> games = new HashSet<Game>();
 
 	public static Set<Game> getAllGames() {
 		return new HashSet<Game>(games);
 	}
 
 	public static Set<Game> getAllRunningGames() {
 		HashSet<Game> games = new HashSet<Game>();
 		for (Game game : games) {
 			if (!game.isFinished()) {
 				games.add(game);
 			} else {
 				continue;
 			}
 		}
 		return games;
 	}
 
 	public static Set<Game> getAllFinishedGames() {
 		HashSet<Game> games = new HashSet<Game>();
 		for (Game game : games) {
 			if (game.isFinished()) {
 				games.add(game);
 			} else {
 				continue;
 			}
 		}
 		return games;
 	}
 
 	public static Set<Game> getAllGamesOn(Goban goban) {
 		HashSet<Game> games = new HashSet<Game>();
 		for (Game game : games) {
 			if (game.getGoban() == goban) {
 				games.add(game);
 			} else {
 				continue;
 			}
 		}
 		return games;
 	}
 
 	public static Game getRunningGameOn(Goban goban) {
 		for (Game game : games) {
 			if (game.getGoban() == goban && !game.isFinished()) {
 				return game;
 			} else {
 				continue;
 			}
 		}
 		return null;
 	}
 
 	private boolean isSuicideAllowed = true;
 
 	public boolean isSuicideAllowed() {
 		return isSuicideAllowed;
 	}
 
	public void setSuicideAllowed(boolean isSuicideForbiden) {
		this.isSuicideAllowed = isSuicideForbiden;
 	}
 
 	public boolean isSuicide(StoneColor color, Coord coord) {
 		Map<StoneColor, IPlayer> fakePlayers = new HashMap<StoneColor, IPlayer>(
 				players);
 		fakePlayers.put(color, new DeterminedPlayer(coord));
 		Game fakeGame = new Game(goban.clone(),
 				fakePlayers.get(StoneColor.BLACK),
 				fakePlayers.get(StoneColor.WHITE));
 		fakeGame.setSuicideAllowed(true); // avoid redo suicide checks
 		fakeGame.setNextPlayerColor(color);
 		fakeGame.play();
 		return fakeGame.getGoban().getCoordContent(coord) == null;
 	}
 }
