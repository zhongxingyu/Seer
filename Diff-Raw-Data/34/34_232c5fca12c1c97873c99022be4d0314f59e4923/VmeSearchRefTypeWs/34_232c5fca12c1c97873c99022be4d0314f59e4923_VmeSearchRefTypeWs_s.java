 package org.vme.web.service;
 
 import java.util.UUID;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.fao.fi.vme.batch.reference.ReferenceDataHardcodedBatch;
 import org.vme.dao.ReferenceDAO;
 import org.vme.web.service.io.ReferencesRequest;
 import org.vme.web.service.io.ServiceResponse;
 
 /**
  * 
  * @author Fabrizio Sibeni, Erik van Ingen
  * 
  */
 @Path("/references/{concept}/{lang}/")
 @Singleton
 public class VmeSearchRefTypeWs {
 
 	@Inject
 	private ReferenceDataHardcodedBatch batch;
 
 	@Inject
 	private ReferenceDAO referenceDAO;
 
 	@PostConstruct
 	public void postConstructBatch() {
 		batch.run();
 	}
 
 	@Path("/list")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response find(@PathParam("concept") String concept, @PathParam("lang") String lang) {
 		try {
 			ReferencesRequest refRequest = new ReferencesRequest(UUID.randomUUID());
 			refRequest.setConcept(concept);
			ServiceResponse<?> result = ServiceInvoker.invoke(referenceDAO, refRequest);
 			return Response.status(200).entity(result).build();
		} catch (Throwable t) {
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 
 	}
 
 	@Path("/batch")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response find() {
 		try {
 			batch.run();
 			return Response.status(200).entity("ReferenceDataHardcodedBatch run successfully").build();
 		} catch (Throwable t) {
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 	}
 
 }
