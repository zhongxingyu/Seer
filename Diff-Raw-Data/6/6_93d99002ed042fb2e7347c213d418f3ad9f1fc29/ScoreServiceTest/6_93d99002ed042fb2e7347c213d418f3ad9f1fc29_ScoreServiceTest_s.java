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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import java.util.List;
 import org.apache.commons.lang.RandomStringUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 
 public class ScoreServiceTest extends AbstractTest {
 
     private static Game game1, game2;
 
     private static Player player;
     private static String level;
 
     String pictureUrl = "http://www.imageshack.com";
 
     @BeforeClass
     public static void init() {
        game1 = gameService.store(new Game(RandomStringUtils.random(10)));
        game2 = gameService.store(new Game(RandomStringUtils.random(10)));
        player = playerService.store(new Player(RandomStringUtils.random(5)));
         level = "MVS";
     }
 
     @Test
     public void should_add_hiscore() {
         Score score = new Score("100", player, level, game1, pictureUrl);
         scoreService.store(score);
 
         Scores scores = scoreService.findAllByGame(game1);
 
         assertTrue(scores.contains(score));
     }
 
     @Test
     public void should_not_add_same_score() {
         Scores scoresInitial = scoreService.findAll();
 
         Score score = new Score("999", player, level, game1, pictureUrl);
         scoreService.store(score);
 
         score = new Score("999", player, level, game1, pictureUrl);
         scoreService.store(score);
 
         Scores scoresFinal = scoreService.findAll();
         assertEquals(scoresFinal.count(), scoresInitial.count() + 1);
     }
 
     @Test
     public void should_find_scores_by_player() {
         int initialCount = scoreService.findAllByPlayer(player).count();
 
         Score score1 = new Score("123", player, "MVS", game1, pictureUrl);
         Score score2 = new Score("456", player, "Normal", game1, pictureUrl);
         Score score3 = new Score("1mn32", player, "Easy", game2, pictureUrl);
 
         scoreService.store(score1);
         scoreService.store(score2);
         scoreService.store(score3);
 
         Scores scores = scoreService.findAllByPlayer(player);
 
         assertEquals(initialCount + 3, scores.count());
 
         assertTrue(scores.contains(score1));
         assertTrue(scores.contains(score2));
         assertTrue(scores.contains(score3));
     }
 
     @Test
     public void should_find_last_scores_order_by_date_desc() {
         List<Score> scores = scoreService.findLastScoresOrderByDateDesc();
         assertNotNull(scores);
     }
 }
