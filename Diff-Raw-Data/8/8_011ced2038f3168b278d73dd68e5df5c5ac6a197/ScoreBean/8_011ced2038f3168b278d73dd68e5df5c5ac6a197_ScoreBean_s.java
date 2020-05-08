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
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 
 import com.anzymus.neogeo.hiscores.clients.AuthenticationFailed;
 import com.anzymus.neogeo.hiscores.clients.Messages;
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
 	public static final String ALL_CLEAR = "ALL CLEAR";
 	public static final String DEFAULT_GAME = "3 Count Bout (3 minutes)";
 	public static final long DEFAULT_POST_ID = 41930;
 	public static final Logger LOG = Logger.getLogger(ScoreBean.class.getName());
 
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
 	private String stage;
 	private long currentGame;
 	private String currentLevel = "MVS";
 	private Boolean postOnNgf = false;
 	private Boolean allClear = false;
 
 	private static final int MAX_MESSAGE_LENGTH = 255;
 
 	@VisibleForTesting
 	NeoGeoFansClientFactory neoGeoFansClientFactory = new NeoGeoFansClientFactory();
 
 	@VisibleForTesting
 	FacesContext facesContext = FacesContext.getCurrentInstance();
 
 	private boolean showPostCheckBox;
 
 	private List<String> customStageValues = new ArrayList<String>();
 
 	private Map<String, String> parameters;
 
 	@PostConstruct
 	public void init() {
 		parameters = facesContext.getExternalContext().getRequestParameterMap();
 		Game game;
 		if (initAfterGameSelection()) {
 			initFromRequest();
 			game = gameService.findById(currentGame);
 		} else if (initBeforeEditing()) {
 			game = initFromDatabase();
 		} else {
 			game = gameService.findByName(DEFAULT_GAME);
 			currentGame = game.getId();
 		}
		updateFormWithSelectedGame(game);
 	}
 
 	private Game initFromDatabase() throws NumberFormatException {
 		Score scoreFromDb = scoreService.findById(Integer.parseInt(id));
 		if (scoreFromDb == null) {
			throw new IllegalStateException("Score with id : '" + id + "' not found in database");
 		}
 		Game game = scoreFromDb.getGame();
 		fullname = scoreFromDb.getPlayer().getFullname();
 		score = scoreFromDb.getValue();
 		pictureUrl = scoreFromDb.getPictureUrl();
 		message = scoreFromDb.getMessage();
 		stage = scoreFromDb.getStage();
 		currentLevel = scoreFromDb.getLevel();
 		currentGame = game.getId();
 		allClear = scoreFromDb.getAllClear();
 		return game;
 	}
 
 	private void initFromRequest() throws NumberFormatException {
 		fullname = parameters.get("form:fullname");
 		score = parameters.get("form:score");
 		pictureUrl = parameters.get("form:pictureUrl");
 		message = parameters.get("form:message");
 		stage = parameters.get("form:stage");
 		currentLevel = parameters.get("form:currentLevel");
 		currentGame = Long.valueOf(parameters.get("form:selectGame"));
 		allClear = "true".equals(parameters.get("form:allClear"));
 	}
 
 	private boolean initBeforeEditing() {
 		return id != null;
 	}
 
 	private boolean initAfterGameSelection() {
 		return parameters.get("form:id") != null;
 	}
 
 	public String add() {
 		try {
 			NeoGeoFansClient ngfClient = neoGeoFansClientFactory.create();
 			boolean isAuthenticated = ngfClient.authenticate(fullname, password);
 			if (isAuthenticated) {
 				acceptScore(ngfClient);
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
 				updateScore();
 				return "home";
 			} else {
 				facesContext.addMessage(null, new FacesMessage("The password is not a valid NGF password"));
 				return "score/edit";
 			}
 		} catch (AuthenticationFailed ex) {
 			LOG.log(Level.SEVERE, null, ex);
 			facesContext.addMessage(null, new FacesMessage(ex.getMessage()));
 			return "score/edit";
 		}
 	}
 
 	private void removeValidationErrors() {
 		Iterator<FacesMessage> messages = facesContext.getMessages();
 		while (messages.hasNext()) {
 			messages.next();
 			messages.remove();
 		}
 	}
 
 	private void updateScore() throws NumberFormatException {
 		Game game = gameService.findById(currentGame);
 		Score scoreFromDb = scoreService.findById(Integer.parseInt(id));
 		scoreFromDb.setMessage(message);
 		scoreFromDb.setGame(game);
 		scoreFromDb.setPictureUrl(pictureUrl);
 		scoreFromDb.setLevel(currentLevel);
 		scoreFromDb.setValue(score);
 		scoreFromDb.setAllClear(allClear || ALL_CLEAR.equals(stage));
 		scoreFromDb.setStage(stage);
 		scoreService.store(scoreFromDb);
 		updateFormWithSelectedGame(game);
 	}
 
 	private void acceptScore(NeoGeoFansClient ngfClient) {
 		Game game = gameService.findById(currentGame);
 		Player player = playerService.findByFullname(fullname);
 		if (player == null) {
 			player = playerService.store(new Player(fullname));
 		}
 		Score scoreToAdd = new Score(score, player, currentLevel, game, pictureUrl);
 		scoreToAdd.setAllClear(allClear || ALL_CLEAR.equals(stage));
 		scoreToAdd.setStage(stage);
 		int end = message.length() > MAX_MESSAGE_LENGTH ? MAX_MESSAGE_LENGTH : message.length();
 		scoreToAdd.setMessage(message.substring(0, end));
 		scoreService.store(scoreToAdd);
 		postOnNgf(scoreToAdd, game, ngfClient);
 		titleUnlockingService.searchUnlockedTitlesFor(player);
 	}
 
 	private void postOnNgf(Score scoreToAdd, Game game, NeoGeoFansClient ngfClient) {
 		if (postOnNgf) {
 			if ("MVS".equals(currentLevel)) {
 				Long postId = game.getPostId();
 				if (postId == null) {
 					postId = 41930L;
 				}
 				ngfClient.post(Messages.toMessage(scoreToAdd), postId);
 			}
 		}
 	}
 
 	public void selectGame(ValueChangeEvent event) {
 		long gameId = (Long) event.getNewValue();
 		Game game = gameService.findById(gameId);
 		updateFormWithSelectedGame(game);
 	}
 
 	private void updateFormWithSelectedGame(Game game) {
 		Long postId = game.getPostId();
 		if (postId != null) {
 			showPostCheckBox = postId != DEFAULT_POST_ID;
 			String stageValues = game.getCustomStageValues();
 			if (stageValues != null) {
 				customStageValues.clear();
 				customStageValues.addAll(Arrays.asList(stageValues.split(";")));
 				customStageValues.add(ALL_CLEAR);
 				stage = customStageValues.get(0);
 			}
 		}
 		removeValidationErrors();
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
 
 	public Boolean getPostOnNgf() {
 		return postOnNgf;
 	}
 
 	public void setPostOnNgf(Boolean postOnNgf) {
 		this.postOnNgf = postOnNgf;
 	}
 
 	public Boolean getAllClear() {
 		return allClear;
 	}
 
 	public void setAllClear(Boolean allClear) {
 		this.allClear = allClear;
 	}
 
 	public String getStage() {
 		return stage;
 	}
 
 	public void setStage(String stage) {
 		this.stage = stage;
 	}
 
 	public boolean isShowPostCheckBox() {
 		return showPostCheckBox;
 	}
 
 	public void setShowPostCheckBox(boolean showPostCheckBox) {
 		this.showPostCheckBox = showPostCheckBox;
 	}
 
 	public List<String> getCustomStageValues() {
 		return customStageValues;
 	}
 
 	public void setCustomStageValues(List<String> customStageValues) {
 		this.customStageValues = customStageValues;
 	}
 
 }
