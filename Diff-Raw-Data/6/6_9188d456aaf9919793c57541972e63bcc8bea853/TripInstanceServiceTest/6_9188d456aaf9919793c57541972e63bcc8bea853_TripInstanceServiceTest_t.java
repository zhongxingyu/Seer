 package be.kdg.groepi.service;
 
 import be.kdg.groepi.model.*;
 import com.sun.xml.internal.bind.v2.TODO;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static be.kdg.groepi.utils.DateUtil.dateToLong;
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gregory
  * Date: 12/02/13
  * Time: 15:01
  * To change this template use File | Settings | File Templates.
  */
 public class TripInstanceServiceTest {
 
     private TripInstance tripinstance;
     private User user;
     //private Long tripId;
 
     @Before
     public void beforeEachTest(){
         user = new User("TIMMEH", "TIM@M.EH", "hemmit", dateToLong(4,5,2011,15,32,0));
         UserService.createUser(user);
         // TODO: Relatie leggen tussen trip en tripinstance
        Trip trip = new Trip("Onze eerste trip","Hopelijk is deze niet te saai!",true,true,user);// trip aanmaken
        TripService.createTrip(trip);
         tripinstance = new TripInstance("Bachelor feestje","Iemand gaat trouwen, bier en vrouwen ole",false,user,trip);
        TripInstanceService.createTripInstance(tripinstance);
     }
 
     @After
     public void afterEachTest(){
         tripinstance = null;
        for (TripInstance tripInstance : TripInstanceService.getAllTripInstances()) {
            TripInstanceService.deleteTripInstance(tripinstance);
         }
     }
 
     @Test
     public void createTripInstance(){
         assertEquals("createTripInstance: ", tripinstance, TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void updateTripInstances(){
         tripinstance.setAvailable(Boolean.FALSE);
         tripinstance.setDescription("Ho-ho-ho edited");
 
         TripInstanceService.updateTripInstance(tripinstance);
         assertEquals("updateTripInstances: ", tripinstance, TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void deleteTripInstance(){
         assertNotNull("deleteTripInstance: Trip found", TripInstanceService.getTripInstanceById(tripinstance.getId()));
         TripInstanceService.deleteTripInstance(tripinstance);
         assertNull("deleteTripInstance: TripInstance not found", TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void addUserToTripInstance(){
         assertTrue("TripInstance: tripInstance should have no participants", tripinstance.getParticipants().isEmpty());
         tripinstance.addParticipantToTripInstance(user);
         assertFalse("TripInstance: tripinstance should have participants", tripinstance.getParticipants().isEmpty());
     }
 
     @Test
     public void addCostToTripInstance(){
         assertTrue("TripInstance: tripInstance should have no costs", tripinstance.getCosts().isEmpty());
         Cost cost = new Cost("BEN's cost", 35.53);
         CostService.createCost(cost);
         tripinstance.addCostToTripInstance(cost);
         assertFalse("TripInstance: tripinstance should have costs", tripinstance.getCosts().isEmpty());
     }
 
     @Test
     public void addRequirementToTripInstance(){
         assertTrue("Trip: trip should have no requirements", tripinstance.getRequirements().isEmpty());
         Requirement requirement = new Requirement("BEN");
         RequirementService.createRequirement(requirement);
         tripinstance.addRequirementToTripInstance(requirement);
         assertFalse("TripInstance: tripinstance should have requirements", tripinstance.getRequirements().isEmpty());
     }
 
     @Test
     public void addMessageToTripInstance(){
         assertTrue("TripInstance: tripinstance should have no messages", tripinstance.getMessages().isEmpty());
         Message message = new Message("BEN's message", dateToLong(12, 10, 1990, 8, 17, 35));
         MessageService.createMessage(message);
         tripinstance.addMessageToTripInstance(message);
         assertFalse("TripInstance: tripInstance should have messages", tripinstance.getMessages().isEmpty());
     }
 
 
 
     /*@Test
     public void createTimedTrip(){}
     @Test
     public void updateTimedTrip(){}
     @Test
     public void deleteTimedTrip(){}
     @Test
     public void createRecurrentTrip(){}
     @Test
     public void updateRecurrence(){}
     @Test
     public void deleteRecurrenceInstances(){}
     @Test
     public void deleteRecurrence(){} */
 }
