 package smartpool.web;
 
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.ui.ModelMap;
 import smartpool.domain.Carpool;
 import smartpool.persistence.dao.*;
 import smartpool.service.*;
 import smartpool.web.form.CarpoolUpdateFormValidator;
 import smartpool.web.form.CreateCarpoolFormValidator;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 import java.util.Properties;
 
import static org.junit.Assert.assertEquals;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class CarpoolControllerIT {
 
     @Mock
     HttpServletRequest request;
     @Mock
     MailService mailService;
     @Mock
     Properties appProperties;
     private final CarpoolService carpoolService = new CarpoolService(new CarpoolDao(), new BuddyDao(), new RouteDao(), new CarpoolBuddyDao());
     private final JoinRequestService joinRequestService = new JoinRequestService(new JoinRequestDao(), new CarpoolBuddyDao(), mailService, appProperties);
     private final BuddyService buddyService = new BuddyService(new BuddyDao());
     private final RouteService routeService = new RouteService(new RouteDao());
     private final CarpoolBuddyService carpoolBuddyService = new CarpoolBuddyService(new CarpoolBuddyDao());
     private final CreateCarpoolFormValidator createCarpoolFormValidator = new CreateCarpoolFormValidator();
     private CarpoolUpdateFormValidator updateValidator;
 
 
     @Test
     public void shouldSearchForCarpool() {
         initMocks(this);
         updateValidator = new CarpoolUpdateFormValidator();
         CarpoolController carpoolController = new CarpoolController(carpoolService, joinRequestService, buddyService, routeService,createCarpoolFormValidator,mailService, carpoolBuddyService, appProperties, updateValidator);
         CarpoolService carpoolService = new CarpoolService(
                 new CarpoolDao(), new BuddyDao(), new RouteDao(), new CarpoolBuddyDao());
         JoinRequestService joinRequestService = new JoinRequestService(
                 new JoinRequestDao(), new CarpoolBuddyDao(), mailService, appProperties);
         BuddyService buddyService = new BuddyService(new BuddyDao());
         RouteService routeService = new RouteService(new RouteDao());
         CreateCarpoolFormValidator validator = new CreateCarpoolFormValidator();
         MailService mailService = new MailService(null);
         CarpoolBuddyService carpoolBuddyService = new CarpoolBuddyService(new CarpoolBuddyDao());
 
         ModelMap model = new ModelMap();
         request.setAttribute("query", "Sony Centre");
         carpoolController.searchByLocation(model, request);
         List<Carpool> searchResult = (List<Carpool>) model.get("searchResult");
        assertEquals(2, searchResult.size());
     }
 }
