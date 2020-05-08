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
 import mx.ecosur.multigame.impl.model.GridGame;
 import mx.ecosur.multigame.model.interfaces.*;
 import mx.ecosur.multigame.MessageSender;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.*;
 import org.drools.io.ResourceFactory;
 
 @Stateless
 @RolesAllowed("admin")
 @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {
 
     private MessageSender messageSender;
         
    @PersistenceContext (unitName="MultiGame")
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
         /** TODO:  Inject or make this query static */
         Query query = em.createNamedQuery("getGameById");
         query.setParameter("id", gameId);
         Game impl;
         try {
             impl = (Game) query.getSingleResult();
         } catch (NoResultException e) {
             throw new RuntimeException ("UNABLE TO FIND GAME WITH ID: " + gameId);
         }
 
         Game game = impl;
         game.setMessageSender(messageSender);
         return game;
     }
 
     public void shareGame (Game impl) {
         try {
             em.merge(impl);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.model.Move)
      */
     public Move doMove(Game game, Move move) throws InvalidMoveException {
         /* Refresh a detached GamePlayer in the Move */
         GamePlayer player = move.getPlayerModel ();
 
         /* Refresh a detached Game in GamePlayer */
         if (!em.contains (game))
             game = em.find (game.getClass(),game.getId());
 
         if (!em.contains (player))
             player = em.find(player.getClass(), player.getId());
 
         move.setPlayerModel (player);
 
 
         /* Refresh any detached GridCells */
         Cell current = move.getCurrentCell();
         Cell dest = move.getDestinationCell();
 
 
         if (current != null) {
             if (!em.contains (current)) {
                 Cell impl = (em.find (
                     current.getClass(), current.getId()));
                 if (impl != null)
                     current = impl;
             }
 
             move.setCurrentCell (current);
         }
 
         if (dest != null) {
             if (!em.contains (dest)) {
                 Cell impl = (em.find (dest.getClass(), dest.getId()));
                 if (impl != null)
                     dest = impl;
             }
             move.setDestinationCell (dest);
         }
 
         if (!em.contains(move) && move.getId() == 0) {
             em.persist(move);
         }
         else {
             Move test = em.find (move.getClass(), move.getId());
             if (test != null)
                 move = test;
         }
             /* Load the Kbase */
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
         kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (game.getChangeSet())),
                 ResourceType.CHANGE_SET);
         KnowledgeBuilderErrors errors = kbuilder.getErrors();
         if (errors.size() == 0)
             kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
         else {
             for (KnowledgeBuilderError error : errors) {
                 System.out.println(error);
             }
 
             throw new RuntimeException ("Unable to load rule base!");
         }
 
         /* Embed the kbase into the game */
         ((GridGame) game).setKbase(kbase);
 
             /* Execute the move */
         game.setMessageSender(messageSender);
         move = game.move (move);
         if (move.getStatus().equals(MoveStatus.INVALID))
             throw new InvalidMoveException ("INVALID Move. [" + move.toString() + "]");
         return move;
     }
 
 
     public Suggestion makeSuggestion(Game game, Suggestion suggestion) throws InvalidSuggestionException {
 
         /* Refresh a detached Game */
         if (!em.contains (game))
             game = em.find (game.getClass(), game.getId());
         else
             em.refresh (game);
 
             /* Refresh a detached GamePlayer in the Suggestion */
         GamePlayer player = suggestion.listSuggestor();
 
         if (!em.contains (player)) {
             player = em.find(player.getClass(), player.getId());
             suggestion.attachSuggestor (player);
         }
 
         /* Refresh any detached GridCells and Move */
         Move move = suggestion.listMove();
         Cell current = move.getCurrentCell();
         Cell dest = move.getDestinationCell();
 
         if (current != null) {
             if (!em.contains (current)) {
                 Cell impl = (em.find (
                     current.getClass(), current.getId()));
                 if (impl != null)
                     current = impl;
                 else
                     em.persist(current);
             }
         }
 
         if (dest != null) {
             if (!em.contains (dest)) {
                 Cell impl = (em.find (
                     dest.getClass(), dest.getId()));
                 if (impl != null)
                     dest = impl;
                 else
                     em.persist(dest);
             }
 
         }
 
         move.setCurrentCell (current);
         move.setDestinationCell (dest);
 
             /* Refresh a detached GamePlayer in the Move */
         player = move.getPlayerModel();
 
         if (!em.contains (player)) {
             player = em.find(player.getClass(), player.getId());
             move.setPlayerModel(player);
         }
 
         if (!em.contains(move)) {
             Move impl = em.find (move.getClass(), move.getId());
             if (impl != null) {
                 impl.setStatus(move.getStatus());
                 move = impl;
             } else {
                 em.persist(move);
             }
         }
 
         suggestion.attachMove(move);
 
         /* If suggestion has no id, it should be persisted, otherwise reattach */
         if (!em.contains (suggestion)) {
             if (suggestion.getId() == 0)
                 em.persist (suggestion);
             else {
                 Suggestion impl = em.find (suggestion.getClass(), suggestion.getId());
                 if (impl != null) {
                     impl.setStatus(suggestion.getStatus());
                     suggestion = impl;
                 }
             }
         }
         
             /* Make the suggestion */
         game.setMessageSender(messageSender);
 
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
         kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (game.getChangeSet())),
                 ResourceType.CHANGE_SET);
         KnowledgeBuilderErrors errors = kbuilder.getErrors();
         if (errors.size() == 0)
             kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
         else {
             for (KnowledgeBuilderError error : errors) {
                 System.out.println(error);
             }
 
             throw new RuntimeException ("Unable to load rule base!");
         }
 
 
         /* Embed the kbase into the game */
         ((GridGame) game).setKbase(kbase);
         Suggestion ret = game.suggest(suggestion);
 
         if (suggestion.getStatus().equals(SuggestionStatus.INVALID))
             throw new InvalidSuggestionException ("INVALID Move.");        
 
          /* Return the processed suggestion */
         return ret;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
      */
     public Collection<Move> getMoves(int gameId) {
         Collection<Move> ret = null;
 
         Game game = getGame(gameId);
         if (game != null)
             ret = game.listMoves();
 
         return ret;
     }
 
     public void addMessage(ChatMessage chatMessage) {
         /* chat message sender may be detatched */
         GamePlayer sender = chatMessage.getSender();
         if (!em.contains(sender))
             chatMessage.setSender(em.find(sender.getClass(), sender.getId()));
         em.merge (chatMessage);
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.SharedBoardInterface#updateMove(mx.ecosur.multigame.model.Move)
      */
     public Move updateMove(Move move) {
         /* Refresh the GamePlayer impl reference and proceed to merge any changes in
          * the move back into the backend
          */
         if (!em.contains(move.getPlayerModel())) {
             GamePlayer player = em.find (move.getPlayerModel().getClass(), move.getPlayerModel().getId());
             move.setPlayerModel(player);
         }
 
         em.merge(move);
         return move;
     }
 }
