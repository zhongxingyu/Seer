 package de.kile.zapfmaster2000.rest.api.statistics;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 
 import de.kile.zapfmaster2000.rest.constants.PlatformConstants;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.AchievementResponseBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.AlcoholResponseBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.AmountResponseBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.DrawCountResponseBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.DrinkProgressResponseBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.RankBuilder;
 import de.kile.zapfmaster2000.rest.impl.core.statistics.UserBuilder;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 
 
 /**
  * 
  * @author PB
  *
  */
 @Path("statistics")
 public class UserStatsResource {
 
 	private static final Logger LOG = Logger.getLogger(RankingsResource.class);
 
 	/**
 	 * Returns {@link UserStatsResponse} as a compilation of different
 	 * statistics about a {@link User}.
 	 * 
 	 * @param pToken
 	 *            Token of {@link Account} used for authentication.
 	 * 
 	 * @param pProgressFrom
 	 *            start of time span for the {@link DrinkProgressResponse}. For
 	 *            format see {@link PlatformConstants}. <code>null</code>
 	 *            results in full list.
 	 * @param pProgressTo
 	 *            end of time span for the {@link DrinkProgressResponse}. For
 	 *            format see {@link PlatformConstants}. <code>null</code>
 	 *            results in list until now.
 	 * @param pProgressInterval
 	 *            interval for the {@link DrinkProgressResponse}.
 	 *            <code>null</code> results in 60 minutes.
 	 * @param pUser
 	 *            user id of {@link User} for all statistics.
 	 * @return <ul>
 	 *         <li>OK: {@link UserStatsResponse}</li>
 	 *         <li>BAD_REQUEST: if <code>pUser</code>,
 	 *         <code>pProgressFrom</code>, <code>pProgressTo</code> or
 	 *         <code>pProgressInterval</code> cannot be parsed.</li>
 	 *         <li>FORBIDDEN: if authentication with <code>pToken</code> fails.</li>
 	 *         </ul>
 	 */
 	@Path("userStats")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveUserStats(@QueryParam("user") String pUser,
 			@QueryParam("progressFrom") String pProgressFrom,
 			@QueryParam("progressTo") String pProgressTo,
 			@QueryParam("progressInterval") String pProgressInterval,
 			@QueryParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 
 			Date dProgressFrom = null;
 			Date dProgressTo = null;
 
 			long user = -1;
 			int interval = 60;
 
 			SimpleDateFormat df = new SimpleDateFormat(
 					PlatformConstants.DATE_TIME_FORMAT);
 
 			if (pProgressFrom != null) {
 				try {
 					dProgressFrom = df.parse(pProgressFrom);
 				} catch (ParseException e) {
 					LOG.error("Could not parse date pFrom " + pProgressFrom, e);
 					return Response.status(Status.BAD_REQUEST).build();
 				}
 			}
 			if (pProgressTo != null) {
 				try {
 					dProgressTo = df.parse(pProgressTo);
 				} catch (ParseException e) {
 					LOG.error("Could not parse date pTo " + pProgressTo, e);
 					return Response.status(Status.BAD_REQUEST).build();
 				}
 			}
 
 			if (pProgressInterval != null) {
 				try {
 					interval = Integer.parseInt(pProgressInterval);
 				} catch (NumberFormatException e) {
 					LOG.error("Could not parse integer pProgressInterval "
 							+ pProgressInterval, e);
 					return Response.status(Status.BAD_REQUEST).build();
 				}
 			}
 			User userR = Zapfmaster2000Core.INSTANCE.getAuthService().retrieveUser(
 					pToken);
			if(userR == null){
 				LOG.error("pUser must be given in this context.");
 				return Response.status(Status.BAD_REQUEST).build();
			}else if (pUser == null) {
 				
 				user = userR.getId();
 			}else{
 				try {
 					user = Long.parseLong(pUser);
 				} catch (NumberFormatException e) {
 					LOG.error("Could not parse user id pUser " + pUser, e);
 					return Response.status(Status.BAD_REQUEST).build();
 					
 				}
 			}
 			
 			AmountResponse amount = AmountResponseBuilder
 					.retrieveAmountResponse(user, account);
 			AchievementResponse achievement = AchievementResponseBuilder
 					.retrieveAchievementResponse(user, account);
 			DrawCountResponse drawCount = DrawCountResponseBuilder
 					.retrieveDrawCountResponse(user, account);
 			AlcoholLevelResponse promille = AlcoholResponseBuilder
 					.retrieveAlcoholLevelResponse(user, account);
 			DrinkProgressResponse progress = DrinkProgressResponseBuilder
 					.retrieveDrinkResponse(account, user, dProgressFrom,
 							dProgressTo, interval);
 			RankResponse rank = RankBuilder.retrieveRank(user, account);
 			UserResponse userResponse = UserBuilder.retrieveUserResponse(account, pToken, pUser,userR);
 			
 
 			UserStatsResponse response = new UserStatsResponse();
 
 			response.setAchievement(achievement);
 			response.setAmount(amount);
 			response.setDrawCount(drawCount);
 			response.setProgress(progress);
 			response.setPromille(promille);
 			response.setRank(rank);
 			response.setUser(userResponse);
 			return Response.ok(response).build();
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 
 	}
 }
