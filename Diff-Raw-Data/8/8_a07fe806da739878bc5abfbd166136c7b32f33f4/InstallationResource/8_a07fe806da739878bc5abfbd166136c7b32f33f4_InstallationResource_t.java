 package de.kile.zapfmaster2000.rest.api.installation;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Admin;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 
 @Path("/install")
 public class InstallationResource {
 
 	@GET
 	@Path("/status")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveStatus() {
 
 		StatusResponse response = new StatusResponse();
 		if (checkIsNewInstallation()) {
 			response.setStatus("new");
 		} else {
 			response.setStatus("installed");
 		}
 
 		return Response.ok(response).build();
 	}
 
 	@POST
 	@Path("/firstadmin")
 	@Produces(MediaType.APPLICATION_JSON)
	public Response createFirstAdmin(String adminName, String password) {
 		if (checkIsNewInstallation()) {
			createAdmin(adminName, password);
 
 			String token = Zapfmaster2000Core.INSTANCE.getAuthService()
					.loginAdmin(adminName, password);
 			if (token == null) {
 				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 			} else {
 				return Response.ok(token).build();
 			}
 
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 	}
 
 	private void createAdmin(String name, String password) {
 		Admin admin = Zapfmaster2000Factory.eINSTANCE.createAdmin();
 		admin.setName(name);
 		admin.setPassword(password);
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		session.save(admin);
 
 		tx.commit();
 	}
 
 	private boolean checkIsNewInstallation() {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Long> result = session.createQuery("SELECT COUNT(*) FROM Admin")
 				.list();
 
 		boolean isNew = result.size() != 1 || result.get(0) == 0;
 
 		tx.commit();
 		return isNew;
 	}
 
 }
