 package com.africaapps.league.web.server.controller.team;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.africaapps.league.dto.PlayerMatchEventSummary;
 import com.africaapps.league.dto.PlayerMatchSummary;
 import com.africaapps.league.dto.TeamSummary;
 import com.africaapps.league.dto.UserPlayerSummary;
 import com.africaapps.league.dto.UserTeamScoreHistorySummary;
 import com.africaapps.league.dto.UserTeamSummary;
 import com.africaapps.league.exception.InvalidPlayerException;
 import com.africaapps.league.exception.LeagueException;
 import com.africaapps.league.model.game.TeamFormat;
 import com.africaapps.league.model.game.User;
 import com.africaapps.league.model.game.UserLeague;
 import com.africaapps.league.model.game.UserPlayerStatus;
 import com.africaapps.league.model.game.UserTeam;
 import com.africaapps.league.model.game.UserTeamStatus;
 import com.africaapps.league.service.game.team.UserTeamService;
 import com.africaapps.league.service.team.TeamService;
 import com.africaapps.league.web.server.controller.BaseLeagueController;
 
 @Controller
 @RequestMapping(value = "/team")
 public class TeamController extends BaseLeagueController {
 
 	@Autowired
 	private UserTeamService userTeamService;
 	@Autowired
 	private TeamService teamService;
 
 	private static Logger logger = LoggerFactory.getLogger(TeamController.class);
 
 	@RequestMapping(value = "/list")
 	public String getUserTeams(HttpServletRequest request,
 			@RequestParam(required = false, value = NEW_USER_PARAM) String newUser,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = USERNAME_PARAM) String username, 
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message, 
 			ModelMap model) {
 		logger.info("Getting teams for user: " + userId + " newUser:" + newUser + " username: " + username);
 		User user = getUser(request, userId, username);
 		if (user != null) {
 			model.remove(USER_ID_PARAM);
 			model.addAttribute(USER_ID_PARAM, user.getId().toString());
 			logger.debug("Found corresponding user: " + user);
 		} else {
 			logger.error("Teams: list() Unknown user!");
 			if (username != null) {
 				model.remove(USERNAME_PARAM);
 				model.addAttribute(USERNAME_PARAM, username);
 			}
 			return "redirect:/user/startRegister";
 		}
 
 		if (newUser == null || !newUser.equalsIgnoreCase("true")) {
 			try {
 				List<UserTeam> teams = userTeamService.getTeams(user.getId());
 				logger.info("Got teams: " + teams.size());
 				model.addAttribute("teams", teams);
 			} catch (LeagueException e) {
 				model.addAttribute("message", "Unable to load your teams");
 				return TEAMS_PAGE_MAPPING;
 			}
 		} else {
 			model.addAttribute("newUser", "true");
 		}
 		if (isValid(message)) {
 			model.remove("message");
 			model.addAttribute("message", message);
 		}
 		logger.info("Any message ?: "+model.get("message"));
 		return TEAMS_PAGE_MAPPING;
 	}
 
 	@RequestMapping(value = "/create")
 	public String startCreateTeam(HttpServletRequest request,
 			@RequestParam(required = false, value = TEAM_NAME_PARAM) String teamName,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = USERNAME_PARAM) String username, 
 			ModelMap model) {
 		User user = getUser(request, userId, username);
 		try {
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, user.getId().toString());
 				if (isValid(teamName)) {
 					UserTeam userTeam = userTeamService.getTeam(user.getId(), teamName);
 					if (userTeam == null) {
 						UserLeague league = getDefaultUserLeague();
 						userTeam = new UserTeam();
 						userTeam.setName(teamName);
 						userTeam.setCurrentScore(0);
 						userTeam.setAvailableMoney(getInitTeamMoney());
 						userTeam.setCurrentFormat(getDefaultTeamFormat(league.getLeague().getLeagueType().getId()));
 						userTeam.setStatus(UserTeamStatus.INCOMPLETE);
 						userTeam.setUserLeague(league);
 						userTeam.setUser(user);
 						userTeamService.saveTeam(userTeam);
 						logger.info("Created team: " + userTeam);
 					} else {
 						logger.info("Got existing team: " + userTeam);
 					}
 					List<UserTeam> teams = new ArrayList<>();
 					teams.add(userTeam);
 					model.addAttribute("teams", teams);
 				} else {
 					model.addAttribute("message", "Enter a team name");
 					return TEAMS_PAGE_MAPPING;
 				}
 			} else {
 				logger.error("startCreateTeam() Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error creating team: ", e);
 			model.addAttribute("message", "Unable to create your team!");
 		}
 		return TEAMS_PAGE_MAPPING;
 	}
 
 	protected Long getInitTeamMoney() {
 		// TODO
 		return Long.valueOf("25000000"); // 25 million
 	}
 
 	protected UserLeague getDefaultUserLeague() throws LeagueException {
 		return userTeamService.getDefaultUserLeague();
 	}
 
 	protected TeamFormat getDefaultTeamFormat(long leagueTypeId) throws LeagueException {
 		return userTeamService.getDefaultTeamFormat(leagueTypeId);
 	}
 
 	@RequestMapping(value = "/players")
 	public String getUserTeamPlayers(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "message") String message,
 			ModelMap model) {
 		logger.info("Getting players for user: " + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId)) {
 					UserTeamSummary userTeam = userTeamService.getTeamWithPlayers(Long.valueOf(teamId));
 					if (userTeam != null) {
 						logger.info("UserTeamSummary: "+userTeam.getDefenders().size()+", "+userTeam.getGoalKeepers().size()
 								+", "+userTeam.getMidfielders().size()+", "+userTeam.getStrikers().size()
 								+", sub:"+userTeam.getSubstitutes().size());
 						userTeam.setUserId(user.getId());
 					}
 					model.addAttribute("team", userTeam);
 					if (message != null && !message.trim().equals("")) {
 						model.addAttribute("message", message);
 					}
 					return PLAYERS_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return TEAMS_PAGE_MAPPING;
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's players: ", e);
 			model.addAttribute("message", "Unable to view your players");
 		}
 		return TEAMS_PAGE_MAPPING;
 	}
 	
 	@RequestMapping(value = "/findPlayer")
 	public String addPlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			@RequestParam(required = false, value="type") String playerType,
 			ModelMap model) {
 		logger.info("Finding player: " + userId + " teamId:" + teamId+" playerType:"+playerType);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId)) {
 					List<TeamSummary> teams = teamService.getTeams(Long.valueOf(teamId));
 					Collections.sort(teams, new Comparator<TeamSummary>() {
 						@Override
 						public int compare(TeamSummary o1, TeamSummary o2) {
 							return o1.getTeamName().compareTo(o2.getTeamName());
 						}});
 					model.addAttribute("teams", teams);
 					model.remove(USER_ID_PARAM);
 					model.addAttribute("userid", userId);
 					model.remove(TEAM_ID_PARAM);
 					model.addAttribute("teamid", teamId);
 					model.remove("type");
 					model.addAttribute("type", playerType);
 					return TEAMS_SEARCH_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting teams: ", e);
 			model.addAttribute("message", "Unable to view teams");
 		}
 		return "/team/players?userid="+userId+"&teamid="+teamId;
 	}
 	
 	@RequestMapping(value = "/teamPlayers")
 	public String findPlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			@RequestParam(required = false, value="type") String type,
 			@RequestParam(required = false, value = "team") String team, 
 			ModelMap model) {
 		logger.info("Finding team's player: " + userId + " teamId:" + teamId+" playerType:"+type+" team:"+team);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(team)) {
 					List<UserPlayerSummary> players = userTeamService.getTeamPlayers(Long.valueOf(team), Long.valueOf(teamId), type);
 					Collections.sort(players, new Comparator<UserPlayerSummary>(){
 						@Override
 						public int compare(UserPlayerSummary o1, UserPlayerSummary o2) {
 							String n1 = o1.getFirstName() + " " + o1.getLastName();
 							String n2 = o2.getFirstName() + " " + o2.getFirstName();
 							return n1.compareTo(n2);
 						}});
 					logger.info("Got "+players.size()+" of type:"+type);
 					model.remove("players");
 					model.addAttribute("players", players);
 					model.remove(USER_ID_PARAM);
 					model.addAttribute(USER_ID_PARAM, userId);
 					model.remove("teamid");
 					model.addAttribute("teamid", teamId); //user's team
 					model.remove("team");
 					model.addAttribute("team", team);  //actual team
 					model.remove("type");
 					model.addAttribute("type", type);
 					return TEAM_PLAYERS_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No teams specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's players: ", e);
 			model.addAttribute("message", "Unable to view team's players");
 		}
 		return "/team/players?userid="+userId+"&teamid="+teamId;
 	}
 	
 	@RequestMapping(value = "/addPlayer")
 	public String addPlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			@RequestParam(required = false, value="type") String type,
 			@RequestParam(required = false, value = "team") String team,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId, 
 			ModelMap model) {
 		logger.info("Adding player to team  userId:" + userId + " teamId:" + teamId+" playerType:"+type+" team:"+team + " poolPlayerId:"+poolPlayerId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(team) && isValidId(poolPlayerId)) {
 					model.remove(USER_ID_PARAM);
 					model.addAttribute(USER_ID_PARAM, userId);
 					model.remove(TEAM_ID_PARAM);
 					model.addAttribute(TEAM_ID_PARAM, teamId);
 					String message = userTeamService.addPlayerToUserTeam(user, Long.valueOf(teamId), Long.valueOf(team), Long.valueOf(poolPlayerId), type); 
 					if (message != null) {
 						model.addAttribute("message", message);
 					}					
 				} else {
 					model.addAttribute("message", "No team or player specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (InvalidPlayerException e) {
 			logger.error("Invalid player to add to team:"+e.getMessage());
 			model.addAttribute("message", e.getMessage());
 		} catch (LeagueException e) {
 			logger.error("Error adding player to team: ", e);
 			model.addAttribute("message", "Unable to add player to team");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:"+url);
 		return url;
 	}
 
 	@RequestMapping(value = "/changePlayerStatus")
 	public String startChangePlayerStatus(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			@RequestParam(required = false, value="type") String type,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId, 
 			ModelMap model) {
 		logger.info("Changing player's status - userId:" + userId + " teamId:" + teamId+" playerType:"+type+ " poolPlayerId:"+poolPlayerId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, userId);
 				model.remove(TEAM_ID_PARAM);
 				model.addAttribute(TEAM_ID_PARAM, teamId); //user's team id
 				if (isValidId(teamId) && isValidId(poolPlayerId)) {
 					UserPlayerSummary player = userTeamService.getTeamPlayer(Long.valueOf(teamId), Long.valueOf(poolPlayerId));
 					if (player != null) {
 						model.remove("statuses");
 						model.addAttribute("statuses", getStatuses(player.getStatus()));
 						model.remove("player");
 						model.addAttribute("player", player);
 					}					
 				} else {
 					model.addAttribute("message", "No team or player specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (InvalidPlayerException e) {
 			logger.error("Invalid player to change status:"+e.getMessage());
 			model.addAttribute("message", e.getMessage());
 		} catch (LeagueException e) {
 			logger.error("Error getting player to change status: ", e);
 			model.addAttribute("message", "Unable to change player's status");
 		}
 		return "playerStatus";
 	}
 	
 	@RequestMapping(value = "/setPlayerStatus")
 	public String setPlayerStatus(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId, 
 			@RequestParam(required = false, value = "status") String status, 
 			ModelMap model) {
		logger.info("Updating player's status - userId:" + userId + " teamId:" + teamId+" poolPlayerId:"+poolPlayerId+" status:"+status);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, userId);
 				model.remove(TEAM_ID_PARAM);
 				model.addAttribute(TEAM_ID_PARAM, teamId); //user's team
 				if (isValidId(teamId) && isValidId(poolPlayerId) && isValid(status)) {
 					try {
 						userTeamService.setPlayerStatus(Long.valueOf(teamId), Long.valueOf(poolPlayerId), status);
 					} catch (InvalidPlayerException e) {
 						model.addAttribute("message", e.getMessage());
 					}
 				} else {
 					model.addAttribute("message", "No player or status specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing player's status: ", e);
 			model.addAttribute("message", "Unable to change player's status");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:"+url);
 		return url;
 	}
 	
 	@RequestMapping(value = "/set")
 	public String acceptTeam(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			ModelMap model) {
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId)) {
 					model.remove(USER_ID_PARAM);
 					model.addAttribute(USER_ID_PARAM, userId);
 					model.remove(TEAM_ID_PARAM);
 					model.addAttribute(TEAM_ID_PARAM, teamId);
 					
 					String message = userTeamService.setTeam(user, Long.valueOf(teamId)); 
 					if (message != null) {
 						model.addAttribute("message", message);
 					}					
 				} else {
 					model.addAttribute("message", "No team or player specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error accepting team: ", e);
 			model.addAttribute("message", "Unable to accept team");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:"+url);
 		return url;
 	}
 	
 	private List<String> getStatuses(UserPlayerStatus playerStatus) {
 		List<String> statuses = new ArrayList<String>();
 		for(UserPlayerStatus s : UserPlayerStatus.values()) {
			if (s != playerStatus && !(s == UserPlayerStatus.CAPTAIN && playerStatus == UserPlayerStatus.SUBSTITUTE )) {
 				statuses.add(s.name());
 			}
 		}
 		return statuses;
 	}
 	
 	@RequestMapping(value = "/changeFormat")
 	public String startChangeFormat(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, 
 			ModelMap model) {
 		logger.info("Finding team's current format: " + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, userId);
 				if (isValidId(teamId)) {					
 					UserTeam team = userTeamService.getTeam(Long.valueOf(teamId));
 					model.remove("formats");
 					if (team != null) {						
 						model.remove("team");
 						model.addAttribute("team", team);
 						List<TeamFormat> formats = userTeamService.getTeamFormats(team.getUserLeague().getLeague().getLeagueType().getId());
 						for(int i=formats.size()-1;i>=0;i--) {
 							TeamFormat format = formats.get(i);
 							if (format.getId().equals(team.getCurrentFormat().getId())) {
 								formats.remove(i);
 								break;
 							}
 						}
 						model.addAttribute("formats", formats);
 						logger.info("Added formats:"+formats.size());
 					} else {
 						model.addAttribute("formats", new ArrayList<TeamFormat>());
 					}					
 					return CHANGE_TEAM_FORMAT_MAPPING;
 				} else {
 					model.addAttribute("message", "No teams specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team formats: ", e);
 			model.addAttribute("message", "Unable to get team formats");
 		}
 		return "/team/players?userid="+userId+"&teamid="+teamId;
 	}
 	
 	@RequestMapping(value = "/setFormat")
 	public String changeFormat(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "formatid") String formatId,
 			ModelMap model) {
 		logger.info("Changing team's current format userId: " + userId + " teamId:" + teamId+" formatId:"+formatId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, userId);
 				model.remove("team");
 				model.remove("teamid");
 				model.addAttribute("teamid", teamId);
 				if (isValidId(teamId) && isValidId(formatId)) {					
 					userTeamService.setTeamFormat(Long.valueOf(teamId), Long.valueOf(formatId));
 				} else {
 					model.addAttribute("message", "No team or format specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing team format: ", e);
 			model.addAttribute("message", "Unable to change team's format");
 		}
 		return "redirect:/team/players";
 	}
 
 	@RequestMapping(value = "/viewPlayerMatches")
 	public String viewPlayerMatches(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			ModelMap model) {
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(poolPlayerId)) {
 					updateAttributes(model, userId, teamId, poolPlayerId, null);			
 					List<PlayerMatchSummary> matches = userTeamService.getPoolPlayerMatches(Long.valueOf(poolPlayerId));					
 					model.addAttribute("matches", matches);
 					return PLAYER_MATCHES_PAGE_MAPPING;				
 				} else {
 					model.addAttribute("message", "No team or player specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting player's matches team: ", e);
 			model.addAttribute("message", "Unable to view player's matches");
 		}
 		String url = "redirect:/team/viewPlayerMatches";
 		logger.info("Going back to players page:"+url);
 		return url;
 	}
 	
 	//viewMatchEvents?userid=${userid}&teamid=${teamid}&poolplayerid=${match.poolPlayerId}&matchid=${match.matchId}
 	@RequestMapping(value = "/viewMatchEvents")
 	public String viewPlayerMatchEvents(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = MATCH_ID_PARAM) String matchId,
 			ModelMap model) {
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(poolPlayerId) && isValidId(matchId)) {
 					updateAttributes(model, userId, teamId, poolPlayerId, matchId);					
 					List<PlayerMatchEventSummary> events = userTeamService.getPoolPlayerMatchEvents(Long.valueOf(poolPlayerId), Long.valueOf(matchId));					
 					model.addAttribute("events", events);
 					return PLAYER_MATCH_EVENTS_PAGE_MAPPING;				
 				} else {
 					model.addAttribute("message", "No team, player or match specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting player's matches team: ", e);
 			model.addAttribute("message", "Unable to view player's matches");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:"+url);
 		return url;
 	}
 	
 	@RequestMapping(value = "/teamHistory")
 	public String viewTeamHistoryPoints(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message,
 			ModelMap model) {
 		logger.info("Getting team's history userId:"+userId+" teamId:"+teamId);
 		User user = getUser(request, userId, null);
 		if (message != null) {
 			model.remove("message");
 			model.addAttribute("message", message);
 		}
 		try {
 			if (user != null) {
 				if (isValidId(teamId)) {
 					updateAttributes(model, userId, teamId, null, null);					
 					List<UserTeamScoreHistorySummary> scores = userTeamService.getUserTeamScoreHistory(user, Long.valueOf(teamId));					
 					model.addAttribute("scores", scores);
 					logger.info("Got scores: "+scores.size());
 					return USER_TEAM_SCORE_HISTORY_PAGE_MAPPING;				
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's history: ", e);
 			model.addAttribute("message", "Unable to view team's history");
 		}
 		return "redirect:/team/list";
 	}
 	
 	//teamHistoryPlayerPoints
 	@RequestMapping(value = "/teamHistoryPlayersPoints")
 	public String viewTeamHistoryPlayersPoints(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = MATCH_ID_PARAM) String matchId,
 			ModelMap model) {
 		logger.info("Getting team's player points history userId:"+userId+" teamId:"+teamId+" matchId: "+matchId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(matchId)) {
 					updateAttributes(model, userId, teamId, null, matchId);					
 					List<UserTeamScoreHistorySummary> scores = userTeamService.getUserTeamScorePlayersHistory(user, Long.valueOf(teamId), Long.valueOf(matchId));					
 					model.addAttribute("scores", scores);
 					logger.info("Got scores: "+scores.size());
 					return USER_TEAM_SCORE_PLAYERS_HISTORY_PAGE_MAPPING;				
 				} else {
 					model.addAttribute("message", "No team or match specified");
 				}
 			} else {
 				logger.error("Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's player points history: ", e);
 			model.addAttribute("message", "Unable to view team's player points history");
 		}
 		return "redirect:/team/teamHistory";
 	}
 }
