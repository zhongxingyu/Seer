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
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 
 import com.anzymus.neogeo.hiscores.domain.Achievement;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Title;
 import com.anzymus.neogeo.hiscores.domain.UnlockedTitle;
 import com.anzymus.neogeo.hiscores.service.PlayerService;
 import com.anzymus.neogeo.hiscores.service.TitleService;
 
 @ManagedBean
 public class TitleBean {
 
 	@ManagedProperty(value = "#{param.titleId}")
 	private String titleId;
 
 	@ManagedProperty(value = "#{param.playerId}")
 	private String playerId;
 
 	@EJB
 	private PlayerService playerService;
 
 	@EJB
 	private TitleService titleService;
 
 	private Achievement achievement;
 	private Title title;
 	private Player player;
 
 	private Set<Player> players = new TreeSet<Player>();
 
 	@PostConstruct
 	public void init() {
 		title = titleService.findById(Long.parseLong(titleId));
 		player = playerService.findById(Long.parseLong(playerId));
 		achievement = playerService.getAchievementFor(player, title);
 		Set<UnlockedTitle> unlockedTitles = title.getUnlockedTitles();
 		for (UnlockedTitle unlockedTitle : unlockedTitles) {
 			players.add(unlockedTitle.getPlayer());
 		}
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public Title getTitle() {
 		return title;
 	}
 
 	public void setTitle(Title title) {
 		this.title = title;
 	}
 
 	public Achievement getAchievement() {
 		return achievement;
 	}
 
 	public void setAchievement(Achievement achievement) {
 		this.achievement = achievement;
 	}
 
 	public String getPlayerId() {
 		return playerId;
 	}
 
 	public void setPlayerId(String playerId) {
 		this.playerId = playerId;
 	}
 
 	public String getTitleId() {
 		return titleId;
 	}
 
 	public void setTitleId(String titleId) {
 		this.titleId = titleId;
 	}
 
 	public Set<Player> getPlayers() {
 		return players;
 	}
 
 	public void setPlayers(Set<Player> players) {
 		this.players = players;
 	}
 }
