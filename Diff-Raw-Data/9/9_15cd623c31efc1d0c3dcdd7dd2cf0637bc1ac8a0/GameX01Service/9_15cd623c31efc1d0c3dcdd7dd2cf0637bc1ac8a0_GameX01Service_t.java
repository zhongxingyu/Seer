 package org.opendarts.core.x01.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.opendarts.core.model.dart.IComputerThrow;
 import org.opendarts.core.model.dart.IDart;
 import org.opendarts.core.model.dart.IDartsThrow;
 import org.opendarts.core.model.dart.impl.ThreeDartsThrow;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.player.IComputerPlayer;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.model.session.ISet;
 import org.opendarts.core.model.session.impl.GameSet;
 import org.opendarts.core.service.game.IGameService;
 import org.opendarts.core.x01.model.GameX01;
 import org.opendarts.core.x01.model.GameX01Definition;
 import org.opendarts.core.x01.model.WinningX01DartsThrow;
 import org.opendarts.core.x01.service.impl.BestDart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameX01Service.
  */
 public class GameX01Service implements IGameService {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(GameX01Service.class);
 
	/** The best darts. */
 	private final Map<Integer, BestDart> bestDarts;
 
 	/**
 	 * Instantiates a new game service.
 	 */
 	public GameX01Service() {
 		super();
 		this.bestDarts = new HashMap<Integer, BestDart>();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.IGameService#createGame(org.opendarts.prototype.model.ISet, org.opendarts.prototype.model.IGameDefinition, java.util.List)
 	 */
 	@Override
 	public IGame createGame(ISet set, List<IPlayer> players) {
 		GameX01Definition gameDef = (GameX01Definition) set.getGameDefinition();
 		GameX01 result = new GameX01((GameSet) set, players,
 				gameDef.getStartScore());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.IGameService#startGame(org.opendarts.prototype.model.IGame)
 	 */
 	@Override
 	public void startGame(IGame igame) {
 		GameX01 game = (GameX01) igame;
 		LOG.info("Game {} started", igame);
		game.initGame();
 	}
 
	/* (non-Javadoc)
	 * @see org.opendarts.core.service.game.IGameService#cancelGame(org.opendarts.core.model.game.IGame)
	 */
 	@Override
 	public void cancelGame(IGame igame) {
 		GameX01 game = (GameX01) igame;
 		game.cancelGame();
 		LOG.info("Game {} canceled", igame);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.game.IGameService#addPlayerThrow(org.opendarts.prototype.internal.model.game.x01.GameX01, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	public void addPlayerThrow(IGame igame, IPlayer player,
 			IDartsThrow idartThrow) {
 		GameX01 game = (GameX01) igame;
 		ThreeDartsThrow dartThrow = (ThreeDartsThrow) idartThrow;
 		game.addPlayerThrow(player, dartThrow);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.game.IGameService#addWinningPlayerThrow(org.opendarts.prototype.internal.model.game.x01.GameX01, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	public void addWinningPlayerThrow(IGame igame, IPlayer player,
 			IDartsThrow idartThrow) {
 		GameX01 game = (GameX01) igame;
 		WinningX01DartsThrow dartThrow = (WinningX01DartsThrow) idartThrow;
 		game.addWinningPlayerThrow(player, dartThrow);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.game.IGameService#getComputerDartsThrow(org.opendarts.core.model.game.IGame, org.opendarts.core.model.player.IComputerPlayer)
 	 */
 	@Override
 	public IComputerThrow getComputerDartsThrow(IGame igame,
 			IComputerPlayer player) {
 		GameX01 game = (GameX01) igame;
 		ComputerPlayerThrow computerPlayerThrow = new ComputerPlayerThrow(game,
 				player);
 		int score = game.getScore(game.getCurrentPlayer());
 		return computerPlayerThrow.getComputerThrow(score);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.game.IGameService#chooseBestDart(int, int)
 	 */
 	@Override
 	public IDart chooseBestDart(IComputerPlayer player, int score, int nbDart,
 			IGame game) {
 		BestDart bestDart = this.bestDarts.get(nbDart);
 		if (bestDart == null) {
 			Properties props = new Properties();
 			URL resource = this
 					.getClass()
 					.getClassLoader()
 					.getResource(
 							MessageFormat.format("BestDart_{0}.properties",
 									nbDart));
 			InputStream in = null;
 			try {
 				in = resource.openStream();
 				props.load(in);
 				bestDart = new BestDart(nbDart, props);
 				this.bestDarts.put(nbDart, bestDart);
 			} catch (IOException e) {
 				LOG.error("Fail to load computer level stats", e);
 			} finally {
 				try {
 					if (in != null) {
 						in.close();
 					}
 				} catch (IOException e) {
 					LOG.error("Fail to load computer level stats", e);
 				}
 			}
 		}
 		return bestDart.getBestDart(player, score, game);
 	}
 }
