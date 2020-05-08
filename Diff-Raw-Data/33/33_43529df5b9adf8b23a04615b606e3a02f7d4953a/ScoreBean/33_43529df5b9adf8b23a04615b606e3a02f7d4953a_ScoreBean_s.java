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
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.context.FacesContext;
 import com.anzymus.neogeo.hiscores.clients.AuthenticationFailed;
 import com.anzymus.neogeo.hiscores.clients.NeoGeoFansClient;
 import com.anzymus.neogeo.hiscores.clients.NeoGeoFansClientFactory;
 import com.anzymus.neogeo.hiscores.domain.Game;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.anzymus.neogeo.hiscores.service.GameService;
 import com.anzymus.neogeo.hiscores.service.PlayerService;
 import com.anzymus.neogeo.hiscores.service.ScoreService;
 import com.anzymus.neogeo.hiscores.service.TitleUnlockingService;
 import com.google.common.annotations.VisibleForTesting;
 
 @ManagedBean
 public class ScoreBean {
 
     @EJB
     ScoreService scoreService;
 
     @EJB
     GameService gameService;
 
     @EJB
     PlayerService playerService;
 
     @EJB
     TitleUnlockingService titleUnlockingService;
 
     @ManagedProperty(value = "#{param.scoreId}")
     private String id;
 
     private String fullname = "";
     private String score = "";
     private String password = "";
     private String pictureUrl = "";
     private String message;
     private long currentGame;
     private String currentLevel = "MVS";
     private static final int MAX_MESSAGE_LENGTH = 255;
 
     @VisibleForTesting
     NeoGeoFansClientFactory neoGeoFansClientFactory = new NeoGeoFansClientFactory();
 
     @VisibleForTesting
     FacesContext facesContext = FacesContext.getCurrentInstance();
 
     @PostConstruct
     public void init() {
         if (id != null) {
             Score scoreFromDb = scoreService.findById(Integer.parseInt(id));
             fullname = scoreFromDb.getPlayer().getFullname();
             score = scoreFromDb.getValue();
             pictureUrl = scoreFromDb.getPictureUrl();
             message = scoreFromDb.getMessage();
             currentLevel = scoreFromDb.getLevel();
             currentGame = scoreFromDb.getGame().getId();
         }
     }
 
     public String add() {
         try {
             NeoGeoFansClient ngfClient = neoGeoFansClientFactory.create();
             boolean isAuthenticated = ngfClient.authenticate(fullname, password);
             if (isAuthenticated) {
                 Game game = gameService.findById(currentGame);
                 Player player = playerService.findByFullname(fullname);
                 if (player == null) {
                     player = playerService.store(new Player(fullname));
                 }
                 Score scoreToAdd = new Score(score, player, currentLevel, game, pictureUrl);
                 int end = message.length() > MAX_MESSAGE_LENGTH ? MAX_MESSAGE_LENGTH : message.length();
                 scoreToAdd.setMessage(message.substring(0, end));
                 scoreService.store(scoreToAdd);
                 if ("MVS".equals(currentLevel)) {
                     String message = NeoGeoFansClient.toMessage(scoreToAdd);
                     Long postId = game.getPostId();
                     if (postId == null) {
                         postId = 41930L;
                     }
                     ngfClient.post(message, postId);
                 }
                 titleUnlockingService.searchUnlockedTitlesFor(player);
                 return "home";
             } else {
                 facesContext.addMessage(null, new FacesMessage("Your NGF account is invalid"));
                 return "score/create";
             }
         } catch (AuthenticationFailed ex) {
             Logger.getLogger(ScoreBean.class.getName()).log(Level.SEVERE, null, ex);
             facesContext.addMessage(null, new FacesMessage(ex.getMessage()));
             return "score/create";
         }
     }
 
     public String edit() {
         try {
             NeoGeoFansClient authenticator = neoGeoFansClientFactory.create();
             boolean isAuthenticated = authenticator.authenticate(fullname, password);
             if (isAuthenticated) {
                 Game game = gameService.findById(currentGame);
                 Score scoreFromDb = scoreService.findById(Integer.parseInt(id));
                 scoreFromDb.setMessage(message);
                 scoreFromDb.setGame(game);
                 scoreFromDb.setPictureUrl(pictureUrl);
                 scoreFromDb.setLevel(currentLevel);
                 scoreFromDb.setValue(score);
                 scoreService.store(scoreFromDb);
                 return "home";
             } else {
                 facesContext.addMessage(null, new FacesMessage("The password is not a valid NGF password"));
                 return "score/edit";
             }
         } catch (AuthenticationFailed ex) {
             Logger.getLogger(ScoreBean.class.getName()).log(Level.SEVERE, null, ex);
             facesContext.addMessage(null, new FacesMessage(ex.getMessage()));
             return "score/edit";
         }
     }
 
     public List<Game> getGames() {
         return gameService.findAll();
     }
 
     public List<String> getLevelList() {
         return Levels.list();
     }
 
     public long getCurrentGame() {
         return currentGame;
     }
 
     public void setCurrentGame(long currentGame) {
         this.currentGame = currentGame;
     }
 
     public String getCurrentLevel() {
         return currentLevel;
     }
 
     public void setCurrentLevel(String currentLevel) {
         this.currentLevel = currentLevel;
     }
 
     public String getFullname() {
         return fullname;
     }
 
     public void setFullname(String fullname) {
         this.fullname = fullname;
     }
 
     public GameService getGameService() {
         return gameService;
     }
 
     public void setGameService(GameService gameService) {
         this.gameService = gameService;
     }
 
     public String getPictureUrl() {
         return pictureUrl;
     }
 
     public void setPictureUrl(String pictureUrl) {
         this.pictureUrl = pictureUrl;
     }
 
     public String getScore() {
         return score;
     }
 
     public void setScore(String score) {
         this.score = score;
     }
 
     public ScoreService getScoreService() {
         return scoreService;
     }
 
     public void setScoreService(ScoreService scoreService) {
         this.scoreService = scoreService;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 }
