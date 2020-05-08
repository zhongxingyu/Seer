 package au.com.dius.resilience.model.test;
 
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.model.IncidentFactory;
 import junit.framework.TestCase;
 
 public class IncidentFactoryTest extends TestCase{
 
 	public void testCreateWithNameAndDateCreated() {
 		int dateCreated = 100;
 		String name = "incident";
		Incident incident = IncidentFactory.createIncident(name, dateCreated);
 		
 		assertEquals(name, incident.getName());
 		assertEquals(dateCreated, incident.getDateCreated());
 	}
 }
