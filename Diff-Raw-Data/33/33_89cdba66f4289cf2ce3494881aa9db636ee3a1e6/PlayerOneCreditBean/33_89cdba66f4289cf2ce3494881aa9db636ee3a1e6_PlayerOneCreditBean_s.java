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
 
 package com.anzymus.neogeo.hiscores.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 import com.anzymus.neogeo.hiscores.service.GameService;
 import com.anzymus.neogeo.hiscores.service.PlayerService;
 import com.anzymus.neogeo.hiscores.service.ScoreService;
 import com.anzymus.neogeo.hiscores.service.TitleService;
 
 @ManagedBean
 public class PlayerOneCreditBean {
 
     @EJB
     ScoreService scoreService;
 
     @EJB
     PlayerService playerService;
 
     @EJB
     GameService gameService;
 
     @EJB
     TitleService titleService;
 
     @ManagedProperty(value = "#{param.fullname}")
     private String fullname;
 
     private List<ScoreItem> scoreItemsOneCredit = new ArrayList<ScoreItem>();
 
     private Player player;
 
     @PostConstruct
     public void init() {
        player = playerService.findByFullname(fullname);
        loadScoreItemsOneCredit();
     }
 
     private void loadScoreItemsOneCredit() {
         for (Game game : gameService.findAllGamesOneCreditedBy(player)) {
             extractScoreItemsFromGame(scoreItemsOneCredit, game);
         }
     }
 
     public String getFullname() {
         return fullname;
     }
 
     public void setFullname(String fullname) {
         this.fullname = fullname;
     }
 
     public List<ScoreItem> getScoresOneCredit() {
         return scoreItemsOneCredit;
     }
 
     private void extractScoreItemsFromGame(List<ScoreItem> scoreItems, Game game) {
         Scores scores = scoreService.findAllOneCreditScoresByGame(game);
         for (String level : Levels.list()) {
             Players.extractScoreItemsFromLevels(scoreItems, game, scores, level, fullname);
         }
     }
 
 }
