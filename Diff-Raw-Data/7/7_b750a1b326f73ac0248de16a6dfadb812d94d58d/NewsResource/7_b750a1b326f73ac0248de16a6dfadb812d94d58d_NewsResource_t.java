 package de.kile.zapfmaster2000.rest.api.news;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.ecore.EClass;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.constants.PlatformConstants;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.util.NewsAdapter;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.News;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 @Path("news")
 public class NewsResource {
 
 	private static final Logger LOG = Logger.getLogger(NewsResource.class);
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveNews(@QueryParam("start") int pStart,
 			@QueryParam("length") int pLength,
 			@QueryParam("token") String pToken) {
 
 		LOG.debug("Retrieving news");
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<News> result = session
 					.createQuery(
 							"SELECT n FROM News n WHERE n.account.id = :accountId"
 									+ " ORDER BY n.date DESC")
 					.setLong("accountId", account.getId())
 					.setMaxResults(pLength).setFirstResult(pStart).list();
 			tx.commit();
 
 			// adapt news (to ResponseBean)
 			List<AbstractNewsResponse> newsResp = new ArrayList<>();
 			NewsAdapter adapter = new NewsAdapter();
 			for (News news : result) {
 				newsResp.add(adapter.adapt(news));
 			}
 
 			return Response.ok(newsResp.toArray()).build();
 
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 	}
 
 	@GET
 	@Path("/drawings")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveDrawings(@QueryParam("token") String pToken) {
 		return retrieveDrawings(pToken, 0, 50);
 	}
 
 	@GET
 	@Path("/drawings")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveDrawings(@QueryParam("token") String pToken,
 			@QueryParam("start") int start, @QueryParam("length") int length) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<Drawing> result = session
 					.createQuery(
 							"FROM Drawing d"
 									// + " JOIN FETCH d.user JOIN FETCH d.keg "
 									// + " JOIN FETCH d.keg.box"
 									+ " WHERE d.user.account.id = :accountId"
 									+ " ORDER BY d.date DESC")
 					.setLong("accountId", account.getId())
 					.setMaxResults(length).setFirstResult(start).list();
 
 			List<DrawingResponse> response = new ArrayList<>();
 
 			SimpleDateFormat format = new SimpleDateFormat(
 					PlatformConstants.DATE_TIME_FORMAT);
 
 			for (Drawing drawing : result) {
 				DrawingResponse r = new DrawingResponse();
 				r.setAmount(drawing.getAmount());
 				r.setBoxId(drawing.getKeg().getBox().getId());
 				r.setBoxName(drawing.getKeg().getBox().getLocation());
 				r.setDate(format.format(drawing.getDate()));
 				r.setDrawId(drawing.getId());
 				r.setUserId(drawing.getUser().getId());
 				r.setUserImage(drawing.getUser().getImagePath());
 				r.setUserName(drawing.getUser().getName());
 				response.add(r);
 			}
 
 			tx.commit();
 			return Response.ok(response).build();
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 
 	}
 
 	@POST
	@Path("/drawings/{drawId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response changeDrawAmount(@PathParam("drawId") long drawId,
 			@FormParam("amount") double amount,
 			@FormParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			EClass drawingClass = Zapfmaster2000Package.eINSTANCE.getDrawing();
 			Drawing drawing = (Drawing) session.get(drawingClass.getName(),
 					drawId);
 
 			if (drawing == null
 					|| drawing.getUser().getAccount().getId() != account
 							.getId()) {
 				tx.commit();
 				return Response.status(Status.FORBIDDEN).build();
 			}
 			
 			drawing.setAmount(amount);
 			session.update(drawing);
 
 			tx.commit();
 			
 			return Response.ok(true).build();
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 	}
 }
