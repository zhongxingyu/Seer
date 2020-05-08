 /*
 * Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
 * 
 * Licensed under the Academic Free License v. 3.2. 
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 /**
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.session;
 
 import static org.junit.Assert.*;
 
 import java.rmi.RemoteException;
 import java.util.List;
import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import com.sun.appserv.security.ProgrammaticLogin;
 import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
 import mx.ecosur.multigame.enums.GameState;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 import mx.ecosur.multigame.exception.InvalidRegistrationException;
 import mx.ecosur.multigame.impl.Color;
 import mx.ecosur.multigame.impl.entity.gente.GenteGame;
 import mx.ecosur.multigame.impl.entity.gente.GenteMove;
 import mx.ecosur.multigame.impl.entity.gente.GentePlayer;
 import mx.ecosur.multigame.impl.model.GridCell;
 import mx.ecosur.multigame.impl.model.GridPlayer;
 import mx.ecosur.multigame.impl.model.GridRegistrant;
 import mx.ecosur.multigame.model.Game;
 import mx.ecosur.multigame.model.GamePlayer;
 import mx.ecosur.multigame.model.Move;
 import mx.ecosur.multigame.model.Registrant;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class GenteSharedBoardTest {
 	
 	private RegistrarRemote registrar;
 	
 	private SharedBoardRemote board;
 	
 	private int gameId;
 	
 	private GentePlayer alice, bob, charlie, denise;
 	
 	private GridCell center;
 	
 
 	@Before
 	public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException, Exception {
         ProgrammaticLogin login = new ProgrammaticLogin();
         login.login("MultiGame", "test","FileRealm", false);
         InitialContext ic = new InitialContext();
 
 		
 		registrar = (RegistrarRemote) ic.lookup(
 			"mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");
 		
 		GridRegistrant[] registrants = {
 			new GridRegistrant ("alice"),
 			new GridRegistrant ("bob"),
 			new GridRegistrant ("charlie"),
 			new GridRegistrant ("denise")};
 		
 		GenteGame game = new GenteGame ();	
 		Game boardGame = new Game (game);
 		
 		for (int i = 0; i < 4; i++) {
 			Registrant registrant = registrar.register(new Registrant (registrants [ i ]));
 			boardGame = registrar.registerPlayer(boardGame, registrant);
 		}
 
         gameId = boardGame.getId();
 		
 		/* Get the SharedBoard */
 		board = (SharedBoardRemote) ic.lookup(
 				"mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
 		boardGame = board.getGame(gameId);
 		game = (GenteGame) boardGame.getImplementation();
 		
 		int row = game.getRows()/2;
 		int column = game.getColumns()/2;
 		
 		center = new GridCell (row, column, Color.YELLOW);
 		
 		/* Set the GamePlayers from the SharedBoard */
 		List<GridPlayer> players = game.getPlayers();
 		for (GridPlayer p : players) {
 			if (p.getRegistrant().getName().equals("alice"))
 				alice = (GentePlayer) p;
 			else if (p.getRegistrant().getName().equals("bob"))
 				bob = (GentePlayer) p;
 			else if (p.getRegistrant().getName().equals("charlie"))
 				charlie = (GentePlayer) p;
 			else if (p.getRegistrant().getName().equals("denise"))
 				denise = (GentePlayer) p;
 		}
 		
 		assertNotNull ("Alice not found in game!", alice);
 		assertNotNull ("Bob not found in game!", bob);
 		assertNotNull ("Charlie not found in game!", charlie);
 		assertNotNull ("Denise not found in game!", denise);
 	}
 	
 	@After
 	public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
 		Game boardGame = board.getGame(gameId);		
 		registrar.unregister(boardGame, new GamePlayer (alice));
 		registrar.unregister(boardGame, new GamePlayer (bob));
 		registrar.unregister(boardGame, new GamePlayer (charlie));
 		registrar.unregister(boardGame, new GamePlayer (denise));
 	}
 	
 	/**
 	 * Simple test to determine if there are the correct number of squares
 	 * after the game state is set to BEGIN.
 	 * @throws RemoteException
 	 */
 	@Test
 	public void testGetGameGrid() throws RemoteException {
 		Game boardGame = board.getGame(gameId);
 		GenteGame game = (GenteGame) boardGame.getImplementation();
 		assertTrue (game.getGrid().getCells().size() == 0);
 	}
 	
 	/** 
 	 * Tests the first move logic.  This tests the positive condition.
 	 * @throws InvalidMoveException 
 	 * @throws RemoteException 
 	 */
 	@Test
 	public void testFirstMove () throws InvalidMoveException, RemoteException {
 		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
 		GenteMove move = new GenteMove (alice, center);
 		Move mv = board.doMove(new Game (game), new Move (move));
 		move = (GenteMove) mv.getImplementation();
 		game = (GenteGame) board.getGame(gameId).getImplementation();
 		assertNotNull (game.getGrid().getLocation(move.getDestinationCell()));
 	}	
 
 	/**
 	 * Tests the first move logic.  This tests the negative condition.
 	 * @throws RemoteException 
 	 */
 	@Test
 	public void testBadFirstMove () throws RemoteException {
 		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
 		int row = center.getRow() -1;
 		int col = center.getColumn() + 1;
 		
 		GridCell center = new GridCell (row, col, alice.getColor());
 		GenteMove move = new GenteMove (alice, center);
 
 		try {
 			board.doMove (new Game (game), new Move (move));
 			fail ("Invalid Move must be thrown!");
 		} catch (InvalidMoveException e) {
 			assertTrue (e != null);
 		} 
 	}
 	
 	@Test
 	public void testFormTria () throws InvalidMoveException, RemoteException {
 		/* Round 1 */
 		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
 		GenteMove move = new GenteMove (alice, center);
 		Move mv = board.doMove(new Game (game), new Move (move));
 		
 		GridCell cell = new GridCell (1, 1, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (3,1, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (5,1, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		/* Round 2 */
 		
 		cell = new GridCell (7, 1, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (1, 2, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (3,2, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (5, 2, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		
 		/* Round 3 */
 
 		cell = new GridCell (7, 2, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (1, 3, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		move = (GenteMove) mv.getImplementation();		
 		assertEquals (1, move.getTrias().size());
 		
 		game = (GenteGame) board.getGame(gameId).getImplementation();
 		List<GridPlayer> players = game.getPlayers();
 		for (GridPlayer player : players) {
 			if (! (player.getId() == bob.getId()))
 				continue;
 			bob = (GentePlayer) player;
 			break;
 		}
 		
 		assertEquals (1, bob.getTrias().size());
 	}
 	
 	@Test
 	public void testSelfishScoring () throws InvalidMoveException, RemoteException {
 		
 		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
 		
 		/* Round 1 */
 		GenteMove move = new GenteMove (alice, center);
 		Move mv = board.doMove(new Game (game), new Move (move));
 
 		GridCell cell = new GridCell (1, 1, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (3,1, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (5,1, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		/* Round 2 */
 		cell = new GridCell (7, 1, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (1, 2, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (3,2, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (5, 2, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		
 		/* Round 3 */
 		cell = new GridCell (7, 2, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		cell = new GridCell (1, 3, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		move = (GenteMove) mv.getImplementation();		
 		assertEquals (1, move.getTrias().size());
 		
 		game = (GenteGame) board.getGame(gameId).getImplementation();
 		List<GridPlayer> players = game.getPlayers();
 		for (GridPlayer player : players) {
 			if (! (player.getId() == bob.getId()))
 				continue;
 			bob = (GentePlayer) player;
 			break;
 		}
 		
 		assertEquals (1, bob.getTrias().size());
 		bob.setTurn(true);
 		
 		cell = new GridCell (3,3, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 
 		denise.setTurn(true);
 		cell = new GridCell (5, 3, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		/* Round 4 */
 		alice.setTurn(true);
 		cell = new GridCell (7, 3, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		bob.setTurn(true);
 		cell = new GridCell (9,1, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		charlie.setTurn(true);
 		cell = new GridCell (3, 5, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		denise.setTurn(true);
 		cell = new GridCell (5, 5, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		/* Round 5 */
 		cell = new GridCell (7, 5, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (9, 2, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (3, 6, charlie.getColor());
 		move = new GenteMove (charlie, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (5, 6, denise.getColor());
 		move = new GenteMove (denise, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		/* Round 6 */
 		cell = new GridCell (7, 6, alice.getColor());
 		move = new GenteMove (alice, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		cell = new GridCell (9,3, bob.getColor());
 		move = new GenteMove (bob, cell);
 		mv = board.doMove(new Game (game), new Move (move));
 		
 		/* Game should be over, with Bob the winner */
 		game = (GenteGame) board.getGame(gameId).getImplementation();
 		assertEquals (GameState.ENDED, game.getState());
 		
 		Set <GentePlayer> winners = game.getWinners();
 		
 		assertTrue (winners.size() == 1);
 		for (GentePlayer player: winners) {
 			assertEquals (bob.getId(), player.getId());
 			assertEquals (player.getPoints(), 10);
 		}
 	}
 
     
}
