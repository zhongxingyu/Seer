 package edu.psu.sweng.ff.controller;
 
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import edu.psu.sweng.ff.common.DatabaseException;
 import edu.psu.sweng.ff.common.Draft;
 import edu.psu.sweng.ff.common.DraftException;
 import edu.psu.sweng.ff.common.League;
 import edu.psu.sweng.ff.common.Member;
 import edu.psu.sweng.ff.common.Player;
 import edu.psu.sweng.ff.dao.LeagueDAO;
 import edu.psu.sweng.ff.dao.MemberDAO;
 import edu.psu.sweng.ff.dao.PlayerDAO;
 import edu.psu.sweng.ff.dao.RosterDAO;
 import edu.psu.sweng.ff.notification.EmailNotifier;
 import edu.psu.sweng.ff.schedule.ScheduleGenerator;
 
 @Path("/league")
 public class LeagueController {
 
 	private final static String TOKEN_HEADER = "X-UserToken";
 
 	@Context UriInfo uriInfo;
 	
 	@GET
 	@Produces({MediaType.APPLICATION_JSON})
 	public Response getLeagues(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@QueryParam("member") String userName
 	    )
 	{
 
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		System.out.println(requester.getUserName() + " is loading leagues");
 		
 		LeagueDAO dao = new LeagueDAO();
 		List<League> leagues = null;
 		if (userName == null || userName.length() == 0) {
 			leagues = dao.loadAll();
 		} else {
 			MemberDAO mdao = new MemberDAO();
 			Member m = null;
 			try {
 				m = mdao.loadByUserName(userName);
 			} catch (DatabaseException e) {
 				throw new WebApplicationException(e);
 			}
 			leagues = dao.loadByMember(m);
 		}
 
 		Gson gson = new Gson();
 		String json = gson.toJson(leagues);
 
 		return Response.ok().entity(json).build();
 		
 	}
 	
 	
 	@GET
 	@Produces({MediaType.APPLICATION_JSON})
 	@Path("/{id}")
 	public Response getLeagueById(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int id
 	    )
 	{
 
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		System.out.println(requester.getUserName() + " is loading league " + id);
 		
 		LeagueDAO dao = new LeagueDAO();
 		League l = dao.loadById(id);
 		
 		Gson gson = new Gson();
 		String json = gson.toJson(l);
 
 		return Response.ok().entity(json).build();
 		
 	}
 
 	@PUT
 	@Path("/{id}")
 	public Response updateLeague(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int id,
 		String json
 		)
 	{
 		Gson gson = new Gson();
 		League league = gson.fromJson(json, League.class);
 
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 
 		System.out.println(requester.getUserName() + " is updating league " + league.getId());
 
 		LeagueDAO dao = new LeagueDAO();
 		dao.update(league);
 		
 		return Response.ok().build();
 		
 	}
 
 	@POST
 	@Path("/{id}/join")
 	@Consumes({MediaType.APPLICATION_JSON})
 	public Response joinLeague(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int lid
 		)
 	{
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 
 		System.out.println(requester.getUserName() + " is joining league " + lid);
 
 		LeagueDAO dao = new LeagueDAO();
 		dao.joinLeague(lid, requester.getUserName());
 
 		return Response.ok().build();
 		
 	}
 	
 	@POST
 	@Consumes({MediaType.APPLICATION_JSON})
 	public Response createLeague(
 		@HeaderParam(TOKEN_HEADER) String token,
 		String json
 		)
 	{
 		Gson gson = new Gson();
 		League league = gson.fromJson(json, League.class);
 		
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 		LeagueDAO dao = new LeagueDAO();
 		boolean ok = dao.store(league);
 		
 		UriBuilder ub = uriInfo.getAbsolutePathBuilder();
 		URI leagueUri = ub.path(String.valueOf(league.getId())).build();
 		
 		System.out.println("member " + requester.getUserName()
 				+ " created new leauge " + league.getName() + " with id "
 				+ league.getId());
 		
 		return Response.created(leagueUri).build();
 		
 	}
 	
 	@POST
 	@Path("/{id}/startdraft")
 	public Response startDraft(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int leagueId
 		)
 	{
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 		LeagueDAO dao = new LeagueDAO();
 		League l = dao.loadById(leagueId);
 		Draft draft = l.getDraft();
 		draft.setNotifier(new EmailNotifier());
 		draft.setRosterStore(new RosterDAO());
 
 		System.out.println("member " + requester.getUserName()
 				+ " is starting the draft process on league " + l.getId());
 
 		if (l.getCommissioner().equals(requester)) {
 			
 			try {
 			
 				draft.setPlayerSource(new PlayerDAO());
 				l.startDraft();
 				ScheduleGenerator sgen = new ScheduleGenerator();
 				l.setSchedule(sgen.generateSchedule(l));
 				dao.update(l);
 			
 			} catch (Exception e) {
 				throw new WebApplicationException(e);
 			}
 			
 		} else {
 			
 			throw new WebApplicationException();
 			
 		}
 	
 		return Response.ok().build();
 	}
 	
 	@GET
 	@Path("/{id}/players")
 	@Produces({MediaType.APPLICATION_JSON})
 	public Response getAvailablePlayers(
 			@HeaderParam(TOKEN_HEADER) String token,
 			@PathParam("id") int leagueId
 		)
 	{
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 		LeagueDAO dao = new LeagueDAO();
 		League l = dao.loadById(leagueId);
 		Draft draft = l.getDraft();
 		
 		List<Player> players = null;
 		if (draft.getWaitingFor().equals(requester)) {
 
 			System.out.println("member " + requester.getUserName()
 					+ " is getting available players for draft round "
 					+ draft.getRound() + " in league " + l.getId());
 			
 			try {
 			
 				draft.setPlayerSource(new PlayerDAO());
 				players = l.getAvailablePlayers();
 			
 			} catch (Exception e) {
 				throw new WebApplicationException(e);
 			}
 			
 		} else {
 			
 			throw new WebApplicationException();
 			
 		}
 		
 		Gson gson = new Gson();
 		String json = gson.toJson(players);
 
 		return Response.ok().entity(json).build();
 		
 	}
 
 	@POST
 	@Path("/{id}/draftplayer")
 	public Response draftPlayer(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int leagueId,
 		String json
 		)
 	{
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 		Gson gson = new Gson();
 		Player player = gson.fromJson(json, Player.class);
 		
 		LeagueDAO dao = new LeagueDAO();
 		League l = dao.loadById(leagueId);
 		Draft draft = l.getDraft();
 		draft.setNotifier(new EmailNotifier());
 		draft.setRosterStore(new RosterDAO());
 		
 		if (draft.getWaitingFor().equals(requester)) {
 
 			System.out.println("member " + requester.getUserName()
 					+ " is drafting player " + player.getLastName()
 					+ " for draft round "
 					+ draft.getRound() + " in league " + l.getId());
 			
 			try {
 				draft.draftPlayer(player);
				dao.store(l);
 			} catch (DraftException e) {
 				throw new WebApplicationException();
 			}
 			
 		} else {
 			
 			throw new WebApplicationException();
 			
 		}
 	
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("/{id}/invite")
 	public Response invite(
 		@HeaderParam(TOKEN_HEADER) String token,
 		@PathParam("id") int leagueId,
 		String json
 		)
 	{
 		Member requester = this.lookupByToken(token);
 		if (requester == null) {
 			System.out.println("unknown token " + token);
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		
 		Gson gson = new Gson();
 		Type collectionType = new TypeToken<List<String>>(){}.getType();
 		List<String> emails = gson.fromJson(json, collectionType);
 		
 		EmailNotifier mailer = new EmailNotifier();
 		MemberDAO mdao = new MemberDAO();
 		Iterator<String> it = emails.iterator();
 		while (it.hasNext()) {
 			String email = it.next();
 			mailer.invite(requester, email);
 			mdao.invite(email, leagueId);
 		}
 		
 		return Response.ok().build();
 		
 	}
 	
 	
 	private Member lookupByToken(String t) {
 		
 		MemberDAO dao = new MemberDAO();
 		return dao.loadByToken(t);
 		
 	}
 	
 }
