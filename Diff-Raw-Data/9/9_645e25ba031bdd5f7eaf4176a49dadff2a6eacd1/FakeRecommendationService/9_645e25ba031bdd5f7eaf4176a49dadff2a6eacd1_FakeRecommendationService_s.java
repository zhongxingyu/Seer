 package eu.elderspaces.recommendations.services;
 
 import it.cybion.commons.web.responses.ResponseStatus;
 import it.cybion.commons.web.services.base.JsonService;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 
 import eu.elderspaces.model.Club;
 import eu.elderspaces.model.Event;
 import eu.elderspaces.model.Person;
 import eu.elderspaces.model.recommendations.PaginatedResult;
 import eu.elderspaces.recommendations.RecommendationsEndpoint;
 import eu.elderspaces.recommendations.core.FakeStaticRecommender;
 import eu.elderspaces.recommendations.core.Recommender;
 import eu.elderspaces.recommendations.exceptions.RecommenderException;
 import eu.elderspaces.recommendations.responses.EntityRecommendationResponse;
 
 /**
  * 
  * @author micheleminno
  * 
  */
 
 @Path(RecommendationsEndpoint.REST_RADIX + RecommendationsEndpoint.FAKE_RECOMMENDATIONS)
 @Produces(MediaType.APPLICATION_JSON)
 public class FakeRecommendationService extends JsonService {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(FakeRecommendationService.class);
     
     private final Recommender recommender;
     
     @Inject
     public FakeRecommendationService(final FakeStaticRecommender recommender) {
     
         this.recommender = recommender;
     }
     
     @GET
     @Path(RecommendationsEndpoint.FRIENDS + "/{userId}")
     public Response getFriends(@PathParam("userId") final String userId) {
     
         LOGGER.info("Friends recommendation service called with userId: " + userId);
         return getRecommendationResponse(userId, Person.class);
     }
     
     @GET
     @Path(RecommendationsEndpoint.EVENTS + "/{userId}")
     public Response getEvents(@PathParam("userId") final String userId) {
     
         LOGGER.info("Events recommendation service called with userId: " + userId);
         return getRecommendationResponse(userId, Event.class);
     }
     
     @GET
     @Path(RecommendationsEndpoint.CLUBS + "/{userId}")
     public Response getClubs(@PathParam("userId") final String userId) {
     
         LOGGER.info("Clubs recommendation service called with userId: " + userId);
         return getRecommendationResponse(userId, Club.class);
     }
     
     private Response getRecommendationResponse(final String userId, final Class requestedClass) {
     
         final PaginatedResult recommendationReport;
         
         try {
             recommendationReport = recommender.getRecommendedEntities(userId, requestedClass);
         } catch (final RecommenderException e) {
             return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
         }
         
         LOGGER.info("Event recommendations retrieved");
         
         return success(new EntityRecommendationResponse(ResponseStatus.OK,
                "Events recommendations", recommendationReport));
     }
     
 }
