 package org.bonitasoft.demo.callforpaper.rest;
 
 import java.net.URI;
 
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.bonitasoft.demo.callforpaper.model.Paper;
 import org.bonitasoft.demo.callforpaper.service.PaperService;
 
 @Path("/paper")
 public class PaperRestService {
 
 	@Inject
 	private PaperService paperService;
 
 	@Context
 	private UriInfo uriInfo;
 
 	@GET
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response getAllPapers(@QueryParam(value = "state") String state) {
 		Paper[] papers;
 		if (state == null || "".equals(state)) {
 			papers = paperService.getAllPapers().toArray(new Paper[] {});
 		} else {
 			papers = paperService.getPapersByState(state).toArray(new Paper[] {});
 		}
 		return Response.ok(papers).build();
 	}
 
 	@GET
 	@Path("/{id}")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response getPaper(@PathParam("id") String id) {
 		Paper paper = paperService.getPaper(Long.valueOf(id));
 		if (paper == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		return Response.ok(paper).build();
 	}
 
 	@POST
 	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response createPaper(Paper paper) {
 		paperService.createPaper(paper);
 		URI uri = uriInfo.getAbsolutePathBuilder().path(paper.getId().toString()).build();
 		return Response.created(uri).build();
 	}

 	@PUT
 	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response updatePaper(Paper paper) {
 		paperService.updatePaper(paper);
		return Response.ok().build();
 	}
 
 	@DELETE
 	@Path("/{id}")
 	public Response deletePaper(@PathParam("id") String id) {
 		paperService.removePaper(Long.valueOf(id));
 		return Response.noContent().build();
 	}
 }
