 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 
 /**
  * The SharedBoardEJB handles operations between players and the shared game
  * board.  The SharedBoardEJB manges game specific events, such as validating a
  * specific move on a game board, making a specific move, and modifying a 
  * previous move.  Clients can also add chat messages to the message stream,
  * increment players turns (soon to be phased into the game rules), and get
  * a list of players for a specific game.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.ejb.impl;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.*;
 
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
 import mx.ecosur.multigame.enums.SuggestionStatus;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 
 import mx.ecosur.multigame.enums.MoveStatus;
 
 import mx.ecosur.multigame.exception.InvalidSuggestionException;
 import mx.ecosur.multigame.grid.entity.GridGame;
 import mx.ecosur.multigame.model.interfaces.*;
 import mx.ecosur.multigame.MessageSender;
 
 @Stateless
 @RolesAllowed("admin")
 @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {
 
 
     private static final Logger logger;
 
     static {
         logger = Logger.getLogger(SharedBoard.class.getCanonicalName());
     }
 
     private MessageSender messageSender;
         
     @PersistenceContext (unitName = "MultiGamePU")
     EntityManager em;
 
     public SharedBoard () throws InstantiationException, IllegalAccessException,
             ClassNotFoundException
     {
         super();
         messageSender = new MessageSender();
         messageSender.initialize();
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
      */
     public Game getGame(int gameId) {
         return em.find(GridGame.class, gameId);
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.entity.Move)
      */
     public Move doMove(Game game, Move move) throws InvalidMoveException {
         move = em.merge(move);
         game = em.find(game.getClass(), game.getId());
 
         /* Now that entities are managed, execute rules on move and game */
         game.setMessageSender(messageSender);
         game.move (move);
 
         if (move.getStatus().equals(MoveStatus.INVALID))
             throw new InvalidMoveException ("INVALID Move. [" + move.toString() + "]");
 
         /* Handle case of agents */
 		/* TODO: implement contract for sorted set of players */
         List<GamePlayer> players = game.listPlayers();
         for (int i = 0; i < players.size(); i++) {
            GamePlayer p = players.get(i);
            /* Spin until no more agents */
            if (p instanceof Agent) {
                Agent a = (Agent) p;
                if (a.ready()) {
                    List <Move> moves = a.determineMoves(game);
                    for (Move m : moves) {
                      m = em.merge(m);
                       game.move(m);
                       if (m.getStatus() != MoveStatus.INVALID) {
                           break;
                       }
                    }
                 }
            }
         }
 
         return move;
     }
 
 
     public Suggestion makeSuggestion(Game game, Suggestion suggestion) throws InvalidSuggestionException {
         Move move  = em.merge(suggestion.listMove());
         suggestion = em.merge(suggestion);
         /* Move must be reattached as em removes it from the suggestion after the merge */
         suggestion.attachMove(move);
         game = em.find(game.getClass(), game.getId());
         game.setMessageSender(messageSender);
         suggestion = game.suggest(suggestion);
         if (suggestion.getStatus().equals(SuggestionStatus.INVALID))
             throw new InvalidSuggestionException ("INVALID Move suggested!");
         return suggestion;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
      */
     public Collection<Move> getMoves(int gameId) {
         Game game = em.find(GridGame.class, gameId);
         return game.listMoves();
     }
 
     public ChatMessage addMessage(ChatMessage chatMessage) {
         return em.merge (chatMessage);
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.SharedBoardInterface#updateMove(mx.ecosur.multigame.entity.Move)
      */
     public Move updateMove(Move move) {
         return em.merge(move);
     }
 }
