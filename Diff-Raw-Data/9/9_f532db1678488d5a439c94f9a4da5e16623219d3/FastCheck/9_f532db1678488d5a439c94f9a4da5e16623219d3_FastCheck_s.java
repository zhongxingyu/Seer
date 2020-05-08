package org.jboss.as.quickstarts.kitchensink.rest;
 
 import javax.enterprise.context.RequestScoped;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 /**
  * FastCheck is responsible for authorizing a transaction. This includes account
  * and TAN-code checkup. If both are valid the FastCheck answers the client with
  * success and queues a transaction + updates the distributed caches account
  * status immediately. The TAN-code should be invalidated as soon as possible
  * too.
  * 
  * @author wolfi
  * 
  */
 
 @Path("/transactions")
 @RequestScoped
 public class FastCheck {
 
 	public static final String VALID_POS_ID = "1060";
 	public static final String VALID_TAN_CODE = "191823901283";
 	public static final double CREDIT = 100d;
 
 	/**
 	 * At least the put works. Should probably exchanged by post with a
 	 * structure like this:
 	 * http://stackoverflow.com/questions/2637017/how-do-i-do
 	 * -a-multipart-form-file-upload-with-jax-rs
 	 * 
 	 * @param posId
 	 * @param tanCode
 	 * @param amount
 	 * @return
 	 */
 	@POST
 	@Path("/tan/{tanCode}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public String transaction(@PathParam("tanCode") String tanCode, TransactionRequest transactionRequest) {
 		String result = null;
 		boolean success = true;
 
 		// method needs @Form parameter with @POST
 		/*
 		if (posId != null && VALID_POS_ID.equals(VALID_POS_ID)) {
 			if (tanCode != null && VALID_TAN_CODE.equals(VALID_TAN_CODE)) {
 				// see if account has enough credit
 				success = CREDIT - amount > 0.1d;
 			}
 		}
 		*/
 
 		if (success) {
 			result = new String("SUCCESS");
 		} else {
 			// TODO: use ExceptionMapper here
 		}
 
 		return result;
 	}
 
 	@GET
 	public String getAllTransactions() {
 		return "Much has happened since you've started to participate in history.";
 	}
 
 }
