 package org.hackystat.sensorbase.resource.projects;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import java.util.ArrayList;
import java.util.List;

 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
 import org.hackystat.sensorbase.resource.projects.jaxb.Spectators;
 import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
 import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Tests the SensorBase REST API for Project spectators.  
  * A project spectator can be added to the project by the owner.  The project spectator can 
  * view the sensordata associated with the project. 
  * 
  * @author Philip M. Johnson
  */
 public class TestProjectSpectatorRestApi extends SensorBaseRestApiHelper {
 
   private static String testUser1 = "TestProjectSpectatorUser1@hackystat.org";
   private static String testUser2 = "TestProjectSpectatorUser2@hackystat.org";
   private static String testProject1 = "TestSpectatorProject1";
   private static SensorBaseClient client1;
   private static SensorBaseClient client2;
   
   /**
    * Starts the server going for these tests, and makes sure our test user is registered.
    * 
    * @throws Exception If problems occur setting up the server.
    */
   @BeforeClass
   public static void setupMembership() throws Exception {
     SensorBaseClient.registerUser(getHostName(), testUser1);
     SensorBaseClient.registerUser(getHostName(), testUser2);
     client1 = new SensorBaseClient(getHostName(), testUser1, testUser1);
     client2 = new SensorBaseClient(getHostName(), testUser2, testUser2);
     client1.deleteProject(testUser1, testProject1);
   }
   
 
   /**
    * Tests the normal project spectator use case.
    * <ul>
    * <li> testUser1 creates a new project called TestProject1, and puts testUser2 on the 
    * spectator list.
    * <li> testUser2 can now retrieve the project and any sensor data. 
    * <li> testUser1 then deletes the Project.
    * </ul>
    * @throws Exception If problems occur.
    */
   @Test
   public void testSpectatorship() throws Exception {
     // First, check that testUser1 has only one defined project (the default).
     assertEquals("Size is 1", 1, client1.getProjectIndex(testUser1).getProjectRef().size());
     // Construct the project representation that is owned by testUser1.
     Project project = makeProject(testProject1);
     project.getSpectators().getSpectator().add(testUser2);
    // PUT it to the server. Note that client1 corresponds to testUser1.
     client1.putProject(project);
    // Ensure that testUser2 can retrieve the project. Client2 corresponds to testUser2.
     project = client2.getProject(testUser1, testProject1);
     // Ensure that testUser2 can retrieve the data
     client2.getProjectSensorData(testUser1, testProject1);
     // Make sure that testUser2 is a spectator.
     assertTrue("Checking spectator 1", project.getSpectators().getSpectator().contains(testUser2));
    
    // Make sure that the project shows up in testUser2's projectindex.
    ProjectIndex index = client2.getProjectIndex(testUser2);
    // Build a list of the projectNames in this index.
    List<String> projectNames = new ArrayList<String>();
    for (ProjectRef ref : index.getProjectRef()) {
      projectNames.add(ref.getName());
    }
    assertTrue("Testing project is in index", projectNames.contains(project.getName()));

     // Now delete the Project.
     client1.deleteProject(testUser1, testProject1);
   }
   
  
   /**
    * Create a project with the passed name.
    * The project start time is now, and the end time is one day from now.
    * @param projectName The name of the Project.
    * @return The newly created Project representation.
    */
   private Project makeProject(String projectName) {
     String owner = testUser1;
     Project project = new Project();
     project.setOwner(owner);
     project.setName(projectName);
     project.setDescription("Test Project Spectators");
     XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
     project.setStartTime(tstamp);
     project.setEndTime(Tstamp.incrementDays(tstamp, 1));
     UriPatterns uris = new UriPatterns();
     uris.getUriPattern().add("**/test/**");
     project.setUriPatterns(uris);
     Invitations invitations = new Invitations();
     project.setInvitations(invitations);
     project.setSpectators(new Spectators());
     return project;
   }
  
 }
