 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 
 /**
  * Registration is the process of adding or finding users in the system and
  * associating that user with a current or new game.  Ficha colors are 
  * determined dynamically, by the available colors per game.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.ejb.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import mx.ecosur.multigame.MessageSender;
 import mx.ecosur.multigame.ejb.interfaces.RegistrarLocal;
 import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
 import mx.ecosur.multigame.enums.GameState;
 import mx.ecosur.multigame.exception.InvalidRegistrationException;
 import mx.ecosur.multigame.model.interfaces.*;
 
 @Stateless
 @RolesAllowed("admin")
 @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 public class Registrar implements RegistrarRemote, RegistrarLocal {
 
     private MessageSender messageSender;
 
    @PersistenceContext (unitName="MultiGame")
     EntityManager em;
 
     /**
      * Default constructor
      */
     public Registrar() throws InstantiationException, IllegalAccessException,
             ClassNotFoundException
     {
         super();
         messageSender = new MessageSender();
         messageSender.initialize();
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#register(java.lang.String)
      */
     public Registrant register(Registrant registrant) {
         /* TODO: inject or make this query static */
         Query query = em.createNamedQuery("getRegistrantByName");
         query.setParameter("name", registrant.getName());
         @SuppressWarnings ("unchecked")
         List<Registrant> registrants = query.getResultList();
         if (registrants.size() == 0) {
                 em.persist(registrant);
         } else {
                 registrant = (Registrant) registrants.get(0);
         }
 
         return registrant;
     }
 
     /**
      * Registers a robot with she specified Game object.
      *
      * TODO:  Make this generic.
      * @throws InvalidRegistrationException
      */
     public Game registerPlayer (Game game, Registrant registrant)
             throws InvalidRegistrationException
     {
         /* Set messaging */
         if (game.getMessageSender() == null)
             game.setMessageSender(messageSender);
 
         if (!em.contains(game)) {
             Game test = em.find(game.getClass(), game.getId());
             if (test == null)
                 em.persist(game);
             else
                 game = test;
         }
 
         if (!em.contains(registrant)) {
             Registrant test = em.find (registrant.getClass(), registrant.getId());
             if (test == null)
                 em.persist(registrant);
             else
                 registrant = test;
         }
 
         registrant.setLastRegistration(System.currentTimeMillis());
         GamePlayer player = game.registerPlayer (registrant);
         em.persist(player);
         messageSender.sendPlayerChange(game);
         return game;
 }
 
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#registerAgent(mx.ecosur.multigame.model.Game, mx.ecosur.multigame.model.Agent)
      */
     public Game registerAgent(Game game, Agent agent) throws
             InvalidRegistrationException
     {
         /* Set messaging */
         game.setMessageSender(messageSender);
 
         if (!em.contains(game)) {
             Game test = em.find(game.getClass(), game.getId());
             if (test == null)
                 em.persist(game);
             else
                 game = test;
         }
 
         if (!em.contains(agent)) {
             Agent test = em.find (agent.getClass(), agent.getId());
             if (test == null)
                 em.persist(agent);
             else
                 agent = test;
         }
 
         agent = game.registerAgent (agent);
 
         /* Merge changes*/
         em.merge(agent);
         em.merge(game);
 
         messageSender.sendPlayerChange(game);
         return game;
     }
 
     public Game unregister(Game game, GamePlayer player) throws InvalidRegistrationException {
 
         /* Remove the user from the Game */
         if (!em.contains(game))
             game = em.find(game.getClass(), game.getId());
 
         /* refresh the game object */
         em.refresh (game);
 
         /* Set messaging */
         game.setMessageSender(messageSender);
 
         game.removePlayer(player);
         game.setState(GameState.ENDED);
         messageSender.sendPlayerChange(game);
         return game;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.RegistrarInterface#getUnfinishedGames(mx.ecosur.multigame.model.Registrant)
      */
     public List<Game> getUnfinishedGames(Registrant player) {
         List<Game> ret = new ArrayList<Game>();
         Query query = em.createNamedQuery("getCurrentGames");
         query.setParameter("registrant", player);
         query.setParameter("state",GameState.ENDED);
         @SuppressWarnings("unchecked")
         List<Game> games = query.getResultList();
         for (Game impl : games) {
             ret.add(impl);
         }
 
         return ret;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.ejb.RegistrarInterface#getPendingGames(mx.ecosur.multigame.model.Registrant)
      */
     public List<Game> getPendingGames(Registrant player) {
         List<Game> ret = new ArrayList<Game>();
         Query query = em.createNamedQuery("getAvailableGames");
         query.setParameter("registrant", player);
         query.setParameter("state", GameState.WAITING);
         /* HACK:  hand narrow the lists of pending games against unfinished */
         List<Game> joinedGames = getUnfinishedGames (player);
         @SuppressWarnings("unchecked")  
         List<Game> games = query.getResultList();
         for (Game impl : games) {
             Game game = impl;
             if (joinedGames.contains(game))
                 continue;
             ret.add(game);
         }
 
         return ret;
     }
 }
