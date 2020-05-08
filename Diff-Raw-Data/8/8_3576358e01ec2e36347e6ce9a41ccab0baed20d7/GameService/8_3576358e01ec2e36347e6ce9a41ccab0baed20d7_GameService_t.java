 package com.khobar.springgames.service;
 
 import javax.validation.ValidationException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.khobar.springgames.domain.Game;
 import com.khobar.springgames.domain.Player;
 import com.khobar.springgames.repository.GameRepository;
 
 @Service
 public class GameService {
 	@Autowired
 	private GameRepository gameRepository;
 
 	@Transactional
 	public Game save(Game game) {
 		// TODO implement checking
 		Player player1 = game.getPlayer1();
 		Player player2 = game.getPlayer2();
 		if (player1.getDiscipline().equals(player2.getDiscipline())) {
			game.setDiscipline(player1.getDiscipline());
 			return gameRepository.save(game);
 		} else {
 			throw new ValidationException(
 					"Unable to make game with two players which discipline is different: "
 							+ player1.getDiscipline().getName() + "!="
 							+ player2.getDiscipline().getName());
 		}
 	}
 
 }
