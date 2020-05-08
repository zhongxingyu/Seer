 package net.kokkeli.resources;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import com.google.inject.Inject;
 
 import net.kokkeli.ISettings;
 import net.kokkeli.data.ILogger;
 import net.kokkeli.data.LogSeverity;
 import net.kokkeli.data.Role;
 import net.kokkeli.data.Track;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.ISessionService;
 import net.kokkeli.data.services.ITrackService;
 import net.kokkeli.data.services.ServiceException;
 import net.kokkeli.player.IPlayer;
 import net.kokkeli.server.ITemplateService;
 
 /**
  * Class for root resources.
  * 
  * Should only redirect to login or main page.
  * @author Hekku2
  *
  */
 @Path("/")
 public class RootResource extends BaseResource {
     private static final String FORM_ID = "id";
     
     private final ITrackService trackService;
     
     /**
      * Creates resource
      * @param logger
      */
     @Inject
     protected RootResource(ILogger logger, ITemplateService templateService, IPlayer player, ISessionService sessions, ISettings settings, ITrackService trackService) {
         super(logger, templateService, player, sessions, settings);
         
         this.trackService = trackService;
     }
    
     /**
      * Redirects user to correct page
      * @return HTML-page, either login or main page
      * @throws ServiceException Thrown when there is problem with rendering.
      */
     @GET
     @Produces("text/html")
     @Access(Role.USER)
     public Response redirect(@Context HttpServletRequest req) throws ServiceException {
         return Response.seeOther(settings.getURI("index")).build();
     }
     
     @POST
     @Produces("text/html")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Access(Role.ADMIN)
     @Path("/addToQueue")
     public Response addToQueue(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) throws BadRequestException{
         try {
             long id = Long.parseLong(formParams.getFirst(FORM_ID));
             
             Track track = trackService.get(id);
             
             if (track.getLocation() == null){
                 log(String.format("Track #%s did not have a location.", track.getId()), LogSeverity.TRACE);
                 return Response.status(Status.INTERNAL_SERVER_ERROR).build();
             }
             
             player.addToQueue(track.getLocation());
             log(String.format("Track #%s added to queue.", track.getId()),LogSeverity.TRACE);
             return Response.ok().build();
         } catch (NumberFormatException e) {
             throw new BadRequestException("Id was not in correct format.", e);
         } catch (ServiceException e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         } catch (NotFoundInDatabase e) {
             return Response.status(Status.NOT_FOUND).build();
         }
     }
     
     @POST
     @Produces("text/html")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Access(Role.ADMIN)
     @Path("/selectPlaylist")
     public Response selectPlaylist(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) throws BadRequestException{
         try {
             long id = Long.parseLong(formParams.getFirst(FORM_ID));
             player.selectPlaylist(id);
             
             log(String.format("Playlist #%s selected.", id),LogSeverity.TRACE);
             return Response.ok().build();
         } catch (NumberFormatException e) {
             throw new BadRequestException("Id was not in correct format.", e);
         } catch (ServiceException e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         } catch (NotFoundInDatabase e) {
             return Response.status(Status.NOT_FOUND).build();
         }
     }
     
     @POST
     @Produces("text/html")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Access(Role.ADMIN)
     @Path("/play")
     public Response play(@Context HttpServletRequest req){
         try {
            if (player.playlistPlaying()){
                player.play();
                log("Playing started.", LogSeverity.TRACE);
            }
             return Response.ok().build();
         } catch (ServiceException e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 }
