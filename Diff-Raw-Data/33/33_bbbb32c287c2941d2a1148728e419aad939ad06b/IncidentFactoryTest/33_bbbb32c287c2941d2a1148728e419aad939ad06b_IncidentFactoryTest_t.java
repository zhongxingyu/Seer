 package au.com.dius.resilience.model.test;
 
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.model.IncidentFactory;
 import junit.framework.TestCase;
 
 public class IncidentFactoryTest extends TestCase{
 
 	public void testCreateWithNameAndDateCreated() {
 		int dateCreated = 100;
 		String name = "incident";
		String note = "The monkeys are armed with nunchucks!";
		Incident incident = IncidentFactory.createIncident(name, dateCreated, note);
 		
 		assertEquals(name, incident.getName());
 		assertEquals(dateCreated, incident.getDateCreated());
 	}
 }
