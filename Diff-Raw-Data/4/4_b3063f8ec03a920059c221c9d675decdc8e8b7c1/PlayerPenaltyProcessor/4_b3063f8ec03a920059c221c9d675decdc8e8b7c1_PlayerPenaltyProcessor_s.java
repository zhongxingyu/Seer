 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package jipdbs.web.processors;
 
 import iddb.api.RemotePermissions;
 import iddb.core.IDDBService;
 import iddb.core.model.Penalty;
 import iddb.core.model.PenaltyHistory;
 import iddb.core.model.Player;
 import iddb.core.model.Server;
 import iddb.core.util.Functions;
 import iddb.exception.EntityDoesNotExistsException;
 import iddb.web.security.service.UserServiceFactory;
 
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.web.Flash;
 import jipdbs.web.MessageResource;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ar.sgt.resolver.SimpleEntry;
 import ar.sgt.resolver.exception.HttpError;
 import ar.sgt.resolver.exception.ProcessorException;
 import ar.sgt.resolver.processor.ResolverContext;
 import ar.sgt.resolver.utils.UrlReverse;
 
 public class PlayerPenaltyProcessor extends SimpleActionProcessor {
 
 	private static final Logger log = LoggerFactory.getLogger(PlayerPenaltyProcessor.class);
 	
 	/* (non-Javadoc)
 	 * @see jipdbs.web.processors.FlashResponseProcessor#processProcessor(ar.sgt.resolver.processor.ResolverContext)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public String doProcess(ResolverContext ctx)
 			throws ProcessorException {
 
 		IDDBService app = (IDDBService) ctx.getServletContext().getAttribute("jipdbs");
 		HttpServletRequest req = ctx.getRequest();
 		
 		String playerId = ctx.getRequest().getParameter("k");
 		String type = ctx.getParameter("type");
 		String reason = ctx.getRequest().getParameter("reason");
 		String duration = ctx.getRequest().getParameter("duration");
 		String durationType = ctx.getRequest().getParameter("dt");
 		String rm = ctx.getParameter("rm");
 		
 		UrlReverse reverse = new UrlReverse(ctx.getServletContext());
 		String redirect;
 		try {
 			redirect = reverse.resolve("playerinfo", new Entry[]{new SimpleEntry("key", playerId)});
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new ProcessorException(e);
 		}
 		
 		Player player = null;
 		try {
 			player = app.getPlayer(playerId);
 		} catch (EntityDoesNotExistsException e) {
 			log.error(e.getMessage());
 			throw new HttpError(HttpServletResponse.SC_NOT_FOUND);
 		}
 		
 		Server server;
 		try {
 			server = app.getServer(player.getServer(), true);
 		} catch (EntityDoesNotExistsException e) {
 			log.error(e.getMessage());
 			throw new ProcessorException(e);
 		}
 		
 		if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), server.getAdminLevel())) {
 			Flash.error(req, MessageResource.getMessage("forbidden"));
 			log.debug("Forbidden");
 			throw new HttpError(HttpServletResponse.SC_FORBIDDEN);
 		}
 		
 		Player currentPlayer = UserServiceFactory.getUserService().getSubjectPlayer(player.getServer());
 		if (currentPlayer == null) {
 			log.error("No player for current user");
 			throw new HttpError(HttpServletResponse.SC_FORBIDDEN);
 		}
 		
 		if (currentPlayer.getLevel() <= player.getLevel()) {
 			Flash.error(req, MessageResource.getMessage("low_level_admin"));
 			return redirect;
 		}
 		
 		Integer funcId;
 		Penalty penalty = null;
 		if ("true".equals(rm)) {
 			funcId = PenaltyHistory.FUNC_ID_RM;
 			String penaltyId = ctx.getParameter("key");
 			try {
 				penalty = app.getPenalty(Long.parseLong(penaltyId));
 			} catch (NumberFormatException e) {
 				log.debug("Invalid penalty id");
 				Flash.error(req, MessageResource.getMessage("forbidden"));
 				throw new HttpError(HttpServletResponse.SC_FORBIDDEN);
 			} catch (EntityDoesNotExistsException e) {
 				log.debug("Invalid penalty id");
 				throw new HttpError(HttpServletResponse.SC_NOT_FOUND);
 			}
 			String res = removePenalty(req, redirect, player, server, penalty);
 			if (res != null) return res;
 		} else {
 			if (StringUtils.isEmpty(reason)) {
 				Flash.error(req, MessageResource.getMessage("reason_field_required"));
 				return redirect;
 			}
 			funcId = PenaltyHistory.FUNC_ID_ADD;
 			penalty = new Penalty();
 			String res = createPenalty(req, penalty, type, reason, duration, durationType, redirect, player, server, currentPlayer);
 			if (res != null) return res;
 		}
 
 		try {
 			app.updatePenalty(penalty, UserServiceFactory.getUserService().getCurrentUser().getKey(), funcId);	
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			Flash.error(req, e.getMessage());
 		}
 		
 		return redirect;
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param redirect
 	 * @param player
 	 * @param server
 	 * @param penalty
 	 * @return
 	 * @throws HttpError
 	 */
 	private String removePenalty(HttpServletRequest req, String redirect,
 			Player player, Server server, Penalty penalty) throws HttpError {
 		if (!penalty.getPlayer().equals(player.getKey())) {
 			log.debug("Penalty is not associated to this player");
 			Flash.error(req, MessageResource.getMessage("forbidden"));
 			throw new HttpError(HttpServletResponse.SC_FORBIDDEN);
 		}
 		if (penalty.getType().equals(Penalty.BAN)) {
 			if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), server.getPermission(RemotePermissions.REMOVE_BAN))) {
 				log.debug("Cannot remove ban");
 				Flash.error(req, MessageResource.getMessage("forbidden"));
 				throw new HttpError(HttpServletResponse.SC_FORBIDDEN);	
 			}
 			if (!((server.getRemotePermission() & RemotePermissions.REMOVE_BAN) == RemotePermissions.REMOVE_BAN)) {
 				Flash.error(req, MessageResource.getMessage("remote_action_not_available"));
 				return redirect;
 			}
 			Flash.info(req, MessageResource.getMessage("local_action_pending"));
 			penalty.setSynced(false);
 		} else {
 			if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), server.getPermission(RemotePermissions.REMOVE_NOTICE))) {
 				log.debug("Cannot remove notice");
 				Flash.error(req, MessageResource.getMessage("forbidden"));
 				throw new HttpError(HttpServletResponse.SC_FORBIDDEN);	
 			}
 			if (!((server.getRemotePermission() & RemotePermissions.REMOVE_NOTICE) == RemotePermissions.REMOVE_NOTICE)) {
 				Flash.warn(req, MessageResource.getMessage("local_action_only"));
 				penalty.setSynced(true);
 				penalty.setActive(false);
 			} else {
 				Flash.info(req, MessageResource.getMessage("local_action_pending"));
 				penalty.setSynced(false);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param penalty
 	 * @param type
 	 * @param reason
 	 * @param duration
 	 * @param durationType
 	 * @param redirect
 	 * @param player
 	 * @param server
 	 * @param currentPlayer
 	 * @return
 	 * @throws HttpError
 	 */
 	private String createPenalty(HttpServletRequest req, Penalty penalty, String type,
 			String reason, String duration, String durationType,
 			String redirect, Player player, Server server, Player currentPlayer)
 			throws HttpError {
 		
 		penalty.setReason(reason);
 		penalty.setPlayer(player.getKey());
 		
 		if (currentPlayer != null) penalty.setAdmin(currentPlayer.getKey());
 		
 		if (type.equals("notice")) {
 			if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), server.getPermission(RemotePermissions.ADD_NOTICE))) {
 				Flash.error(req, MessageResource.getMessage("forbidden"));
 				throw new HttpError(HttpServletResponse.SC_FORBIDDEN);	
 			}
 			penalty.setType(Penalty.NOTICE);
 			if ((server.getRemotePermission() & RemotePermissions.ADD_NOTICE) == RemotePermissions.ADD_NOTICE) {
 				penalty.setSynced(false);
 				penalty.setActive(false);
 				Flash.info(req, MessageResource.getMessage("local_action_pending"));
 			} else {
 				penalty.setSynced(true);
 				penalty.setActive(true);
 				Flash.warn(req, MessageResource.getMessage("local_action_only"));
 			}
 		} else {
 			if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), server.getPermission(RemotePermissions.ADD_BAN))) {
 				Flash.error(req, MessageResource.getMessage("forbidden"));
 				throw new HttpError(HttpServletResponse.SC_FORBIDDEN);	
 			}			
 			Long dm = Functions.time2minutes(duration + durationType);
 			if (dm.equals(0)) {
 				Flash.error(req, MessageResource.getMessage("duration_field_required"));
 				return redirect;			
 			}
 			if ((server.getRemotePermission() & RemotePermissions.ADD_BAN) == RemotePermissions.ADD_BAN) {
 				penalty.setSynced(false);
 				penalty.setActive(false);
 				Flash.info(req, MessageResource.getMessage("local_action_pending"));
 			} else {
 				Flash.error(req, MessageResource.getMessage("remote_action_not_available"));
 				return redirect;
 			}
 			penalty.setType(Penalty.BAN);
 			penalty.setDuration(dm);
 		}
 		return null;
 	}
 
 }
