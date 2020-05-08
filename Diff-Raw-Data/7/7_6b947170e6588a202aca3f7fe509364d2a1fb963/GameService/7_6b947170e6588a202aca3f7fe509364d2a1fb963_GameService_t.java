 package pl.dagguh.soccerbackend.game.boundary;
 
 import pl.dagguh.soccerbackend.game.control.exceptions.InvalidGameTokenException;
 import pl.dagguh.soccerbackend.game.control.exceptions.GameNotFoundException;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Table;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import org.apache.log4j.Logger;
 import pl.dagguh.soccerbackend.game.control.*;
 import pl.dagguh.soccerbackend.game.entity.Game;
 import pl.dagguh.soccerbackend.game.entity.GameField;
 import pl.dagguh.soccerbackend.player.boundary.PlayerService;
 import pl.dagguh.soccerbackend.player.control.exceptions.PlayerNotFoundException;
 import pl.dagguh.soccerbackend.player.control.PlayerToken;
 import pl.dagguh.soccerbackend.player.control.exceptions.TicketMismatchException;
 
 /**
  * @author Maciej Kwidziński <maciek.kwidzinski@gmail.com>
  */
 @Stateless
 @Path("/game")
 @Table()
 public class GameService {
 
 	private static Logger log = Logger.getLogger(GameService.class);
 	@PersistenceContext
 	private EntityManager em;
 	@EJB
 	private PlayerService playerService;
 	@EJB
 	private GameLogic gameLogic;
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String hostGame(PlayerToken playerToken) {
 		try {
 			log.info("Creating new game with " + playerToken);
 			playerService.validatePlayerToken(playerToken);
 			Game unmergedGame = gameLogic.createNewGame(playerToken.getNick());
 			Game mergedGame = em.merge(unmergedGame);
 			log.info("New game created " + mergedGame);
 			return Long.toString(mergedGame.getId());
 		} catch (PlayerNotFoundException e) {
 			return "-1";
 		} catch (TicketMismatchException e) {
 			return "-1";
 		}
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	@Path("/join/{gameId}")
 	public void join(PlayerToken playerToken, @PathParam("gameId") String gameId) {
 		try {
 			log.info(playerToken + " trying to join game " + gameId);
 			playerService.validatePlayerToken(playerToken);
 			Game game = find(gameId);
 			gameLogic.joinToGame(game, playerToken.getNick());
 			em.merge(game);
 		} catch (PlayerNotFoundException ex) {
 		} catch (TicketMismatchException ex) {
 		}
 	}
 
 	@GET
 	@Path("/{gameId}")
 	@Produces(MediaType.APPLICATION_XML)
 	public Game find(@PathParam("gameId") String gameId) {
 		try {
 			return find(Long.parseLong(gameId));
 		} catch (GameNotFoundException e) {
 			throw new WebApplicationException(404);
 		} catch (NumberFormatException e) {
 			throw new WebApplicationException(400);
 		}
 	}
 
 	private Game find(long gameId) throws GameNotFoundException {
 		Game game = em.find(Game.class, gameId);
 		if (null == game) {
 			throw new GameNotFoundException();
 		}
 		return game;
 	}
 
 	@GET
 	@Path("/list")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String findOpenGame() {
 		try {
 			TypedQuery<Long> query = em.createQuery("SELECT e.id FROM Game e WHERE e.gameStatus = :gameStatus", Long.class);
 			query.setParameter("gameStatus", GameStatus.WAITING_FOR_OPPONENT);
 			List<Long> gameIds = query.getResultList();
 			log.info("Found open game ids " + gameIds);
 			return Long.toString(gameIds.get(0));
 		} catch (IndexOutOfBoundsException e) {
 			log.info("No open games found");
 			return "-1";
 		}
 	}
 
 	@POST
 	@Produces(MediaType.TEXT_PLAIN)
 	@Path("/status/{gameId}")
 	public GameStatus getGameStatus(@PathParam("gameId") String gameId) {
 		Game game = find(gameId);
 		return game.getGameStatus();
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/field/{gameId}")
 	public GameField getGameField(@PathParam("gameId") String gameId) {
 		Game game = find(gameId);
 		return game.getGameField();
 	}
 
 	public void validateGameToken(GameToken gameToken) throws PlayerNotFoundException, TicketMismatchException, InvalidGameTokenException {
 		PlayerToken playerToken = gameToken.getPlayerToken();
 		playerService.validatePlayerToken(playerToken);
 		Game game = find(gameToken.getGameId());
 		String nickFromToken = playerToken.getNick();
 		if (game.getBluePlayerNick().equals(nickFromToken) || game.getRedPlayerNick().equals(nickFromToken)) {
 		} else {
 			throw new InvalidGameTokenException(gameToken);
 		}
 	}
 
 	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces(MediaType.APPLICATION_XML)
 	@Path("/move/{gameId}")
 	public MoveStatus makeMove(Move move) {
 		try {
 			GameToken gameToken = move.getGameToken();
 			validateGameToken(gameToken);
 			Game game = find(gameToken.getGameId());
 			MoveStatus status = gameLogic.makeMove(move.getMoveDirection(), game);
 			GameStatus gameStatusAfterMove = gameLogic.interpretMoveStatus(status, game.getGameStatus());
 			game.setGameStatus(gameStatusAfterMove);
 			em.merge(game);
 			return status;
 		} catch (PlayerNotFoundException ex) {
 		} catch (TicketMismatchException ex) {
 		} catch (InvalidGameTokenException ex) {
 		}
 		return MoveStatus.REJECTED;
 	}
 
 	@GET
	@Path("/{color}/{gameId}")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getPlayerNick(@PathParam("color") String color, @PathParam("gameId") String gameId) {
 		Game game = find(gameId);
 		if (color.equals("red")) {
 			return game.getRedPlayerNick();
 		} else if (color.equals("blue")) {
 			return game.getBluePlayerNick();
 		} else {
 			String message = "Unknown player color " + color;
 			log.warn(message);
 			return message;
 		}
 	}
 }
