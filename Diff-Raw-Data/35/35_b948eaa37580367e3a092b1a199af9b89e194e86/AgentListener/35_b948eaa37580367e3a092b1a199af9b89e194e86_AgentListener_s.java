 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 /**
 * @author awaterma@ecosur.mx
 */
 
 package mx.ecosur.multigame.jms;
 
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 import javax.annotation.security.RunAs;
 import javax.ejb.EJB;
 import javax.ejb.MessageDriven;
 import javax.ejb.MessageDrivenContext;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ObjectMessage;
import javax.transaction.*;
 
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
 
 import mx.ecosur.multigame.enums.GameEvent;
 
import mx.ecosur.multigame.enums.MoveStatus;
 import mx.ecosur.multigame.enums.SuggestionStatus;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 
 import mx.ecosur.multigame.exception.InvalidSuggestionException;
 import mx.ecosur.multigame.model.interfaces.*;
 
 
 @RunAs("j2ee")
 @MessageDriven(mappedName = "MultiGame")
 public class AgentListener implements MessageListener {
 
     @EJB
     private SharedBoardLocal sharedBoard;
 
     @Resource
     private MessageDrivenContext mdContext;
 
     private static Logger logger = Logger.getLogger(AgentListener.class.getCanonicalName());
 
     private static GameEvent[] gameEvents = { GameEvent.PLAYER_CHANGE };
 
     private static GameEvent[] suggestionEvents = { GameEvent.SUGGESTION_APPLIED, GameEvent.SUGGESTION_EVALUATED};
 
     private static final long serialVersionUID = -312450142866686545L;
 
     public void onMessage(Message message) {
         boolean matched = false;
         Move moved = null;

         try {
             String gameEvent = message.getStringProperty("GAME_EVENT");
             GameEvent event = GameEvent.valueOf(gameEvent);
             ObjectMessage msg = (ObjectMessage) message;
             for (GameEvent possible : gameEvents) {
                 if (event.equals(possible)) {                    
                     matched = true;
                     Game game = (Game) msg.getObject();
                     message.acknowledge();
                     List<GamePlayer> players = game.listPlayers();
                     for (GamePlayer p : players) {
                         if (p instanceof Agent) {
                             Agent agent = (Agent) p;
                             if (agent.ready()) {
                                 Set<Move> moves = agent.determineMoves(game);
                                 for (Move move : moves) {
                                     moved = sharedBoard.doMove(game, move);
                                     if (moved == null) {
                                         throw new RuntimeException ("Ready Agent cannot submit move!");
                                     }
 
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }
 
             if (!matched) {
                 for (GameEvent possible : suggestionEvents) {
                     if (event.equals(possible)) {
                         matched = true;
                         Suggestion suggestion = (Suggestion) msg.getObject();
                         SuggestionStatus oldStatus = suggestion.getStatus();
                         Game game = (Game) msg.getObject();
                         message.acknowledge();
                         List<GamePlayer> players = game.listPlayers();
                         for (GamePlayer p : players) {
                             if (p instanceof Agent) {
                                 Agent agent = (Agent) p;
                                 suggestion = (agent.processSuggestion (game, suggestion));
                             }
                         }
 
                         SuggestionStatus newStatus = suggestion.getStatus();
 
                         if (oldStatus != newStatus && (
                                 newStatus.equals(SuggestionStatus.ACCEPT) || newStatus.equals(SuggestionStatus.REJECT)))
                         {
                              try {
                                  sharedBoard.makeSuggestion (game, suggestion);
                              } catch (InvalidSuggestionException e) {
                                  logger.severe("InvalidSuggestionException: " + e.getMessage());
                                  e.printStackTrace();
                              }
                         }
                     }
                 }
             }
 
         } catch (JMSException e) {
             logger.warning("Not able to process game message: " + e.getMessage());
             e.printStackTrace();
             throw new RuntimeException (e);
         } catch (RuntimeException e) {
             logger.warning ("RuntimeException generated! " + e.getMessage());
             e.printStackTrace();
         } catch (InvalidMoveException e) {
             logger.severe(e.getMessage());
             e.printStackTrace();
         }
     }
 }
