 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.service;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import org.joda.time.DateTime;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 
 @Stateless
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public class GameService {
 
 	@PersistenceContext
 	EntityManager em;
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public Game store(Game game) {
 		Game storedGame;
 		if (game.getId() == null) {
 			em.persist(game);
 			storedGame = game;
 		} else {
 			storedGame = em.merge(game);
 		}
 		em.flush();
 		return storedGame;
 	}
 
 	public List<Game> findAll() {
 		TypedQuery<Game> query = em.createNamedQuery("game_findAll", Game.class);
 		return query.getResultList();
 	}
 
 	public Game findByName(String gameName) {
 		TypedQuery<Game> query = em.createNamedQuery("game_findByName", Game.class);
 		query.setParameter("name", gameName);
 		return query.getSingleResult();
 	}
 
 	public Game findById(long gameId) {
 		return em.find(Game.class, gameId);
 	}
 
 	public List<Game> findAllGamesPlayedBy(Player player) {
 		Query query = em.createNativeQuery(Game.findAllGamesPlayedBy, Game.class);
 		query = query.setParameter(1, player.getId());
 		return query.getResultList();
 	}
 
 	public List<Game> findAllPlayedGames() {
 		Query query = em.createNativeQuery(Game.findAllPlayedGames, Game.class);
 		return query.getResultList();
 	}
 
 	public List<Object[]> findAllScoreCountForEachGames() {
 		Query query = em.createNativeQuery(Game.findAllScoreCountForEachGames);
 		return query.getResultList();
 	}
 
 	public List<Game> findAllGamesOneCreditedBy(Player player) {
 		Query query = em.createNativeQuery(Game.findAllGamesOneCreditedBy, Game.class);
 		query = query.setParameter(1, player.getId());
 		return query.getResultList();
 	}
 
 	public long getNumberOfGames() {
 		Query query = em.createNativeQuery(Game.getNumberOfGames);
 		return Queries.getCount(query);
 	}
 
 	public List<Game> findAllUnplayedGames() {
 		Query query = em.createNativeQuery(Game.findAllUnplayedGames, Game.class);
 		return query.getResultList();
 	}
 
 	public List<Game> findAllPlayedGamesThisMonth() {
 		Query query = em.createNativeQuery(Game.findAllPlayedGamesThisMonth, Game.class);
 		Date beginDate = new DateTime().withDayOfMonth(1).toDate();
		Date endDate = new DateTime().withDayOfMonth(31).toDate();
 		query.setParameter(1, beginDate);
 		query.setParameter(2, endDate);
 		return query.getResultList();
 	}
 
 }
