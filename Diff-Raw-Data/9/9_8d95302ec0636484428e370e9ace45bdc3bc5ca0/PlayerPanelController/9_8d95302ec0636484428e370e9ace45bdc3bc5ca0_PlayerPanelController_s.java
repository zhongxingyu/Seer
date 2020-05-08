 package com.pedrero.eclihand.controller.panel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 
 import com.pedrero.eclihand.controller.EntityDisplayerController;
 import com.pedrero.eclihand.model.dto.PlayerDto;
 import com.pedrero.eclihand.model.dto.TeamDto;
 import com.pedrero.eclihand.service.PlayerService;
 import com.pedrero.eclihand.ui.EntityDisplayerComponent;
 import com.pedrero.eclihand.ui.panel.entity.PlayerPanel;
 import com.pedrero.eclihand.utils.UpdatableContentDisplayer;
 
 @Controller
 public class PlayerPanelController implements
 		EntityDisplayerController<PlayerDto>, UpdatableContentDisplayer {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3448580944349868849L;
 
 	@Resource
 	private PlayerPanel playerPanel;
 
 	@Resource
 	private PlayerService playerService;
 
 	private PlayerDto player;
 
 	@Override
 	public void init() {
 		playerPanel.init();
 	}
 
 	public void searchPlayerAndDisplay(Long teamId) {
		PlayerDto player = playerService.findById(teamId);
		searchPlayerAndDisplay(player);
 	}
 
 	public void searchPlayerAndDisplay(PlayerDto entity) {
 		playerPanel.display(entity);
 	}
 
 	@Override
 	public void display(PlayerDto entity) {
		searchPlayerAndDisplay(entity);
		player = entity;
 	}
 
 	@Override
 	public EntityDisplayerComponent<PlayerDto> getEntityDisplayerComponent() {
 		return playerPanel;
 	}
 
 	@Override
 	public void makeUpdatable() {
 		playerPanel.getPlayerPropertyDisplayer().makeUpdatable();
 		playerPanel.getTeamTable().makeUpdatable();
 		playerPanel.setUpdatable(true);
 	}
 
 	@Override
 	public void makeReadOnly() {
 		playerPanel.getPlayerPropertyDisplayer().makeReadOnly();
 		playerPanel.getTeamTable().makeReadOnly();
 		playerPanel.setUpdatable(false);
 	}
 
 	@Override
 	public void validateChanges() {
 		playerPanel.getPlayerPropertyDisplayer().validateChanges();
 		playerPanel.getTeamTable().validateChanges();
 		Set<TeamDto> teamList = new HashSet<TeamDto>(playerPanel.getTeamTable()
 				.retrieveData());
 		player.setTeams(teamList);
 		playerService.update(player);
 	}
 
 }
