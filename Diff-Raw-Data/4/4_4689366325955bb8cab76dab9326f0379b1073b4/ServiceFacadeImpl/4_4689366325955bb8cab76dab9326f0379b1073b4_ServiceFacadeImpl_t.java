 package de.lmu.ios.geomelody.facade;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.Provider;
 
 import com.sun.jersey.api.json.JSONWithPadding;
 import com.sun.jersey.spi.resource.Singleton;
 
 import de.lmu.ios.geomelody.dom.Filters;
 import de.lmu.ios.geomelody.dom.Location;
 import de.lmu.ios.geomelody.dom.Song;
 import de.lmu.ios.geomelody.server.Progress;
 import de.lmu.ios.geomelody.server.Result;
 import de.lmu.ios.geomelody.service.SongMappingService;
 import de.lmu.ios.geomelody.service.model.ProgressStatus;
 
 @Provider
 @Singleton
 @Path("/v1")
 @Consumes({ "application/x-javascript", MediaType.APPLICATION_XML,
 		MediaType.APPLICATION_JSON })
 @Produces({ "application/x-javascript", MediaType.APPLICATION_XML,
 		MediaType.APPLICATION_JSON })
 public class ServiceFacadeImpl implements ServiceFacade {
 	@Context
 	private UriInfo uri;
 
 	private SongMappingService songMappingService;
 
 	public SongMappingService getClassificationService() {
 		return songMappingService;
 	}
 
 	public void setSongMappingService(SongMappingService songMappingService) {
 		this.songMappingService = songMappingService;
 	}
 
 	@Override
 	@GET
 	@Path("/job/progress/{id}")
 	public Response getProgress(@PathParam("id") String id,
 			@QueryParam("callback") String callback) {
 		Response response = Response.status(404).build();
 
 		ProgressStatus status = Progress.Instance.getStatus(id);
 		if (status != null) {
 			response = Response
 					.status(200)
 					.entity(new JSONWithPadding(
 							new GenericEntity<ProgressStatus>(status) {
 							}, callback)).build();
 		} else {
 			response = Response.status(500).build();
 		}
 
 		return response;
 	}
 
 	@Override
 	@GET
 	@Path("/job/result/{id}")
 	public Response getResult(@PathParam("id") String id,
 			@QueryParam("callback") String callback) {
 		Response response = Response.status(404).build();
 
 		Object result = Result.Instance.getResult(id);
 		if (result != null) {
 			response = Response
 					.status(200)
 					.entity(new JSONWithPadding(new GenericEntity<Object>(
 							result) {
 					}, callback)).build();
 		} else {
 			response = Response.status(500).build();
 		}
 
 		return response;
 	}
 
 	@Override
 	@POST
 	@Path("/song")
 	public Response saveSong(Song song) {
 		songMappingService.saveSong(song);
 		return Response.status(200).build();
 
 	}
 
 	@Override
	@POST
	@Path("/songs/nearby")
 	public Response getNearestSongs(Location location, Filters filters,
 			@QueryParam("callback") String callback) {
 		return Response
 				.status(200)
 				.entity(new JSONWithPadding(new GenericEntity<Object>(
 						songMappingService.getkNearestSongs(location, filters,
 								20)) {
 				}, callback)).build();
 	}
 }
