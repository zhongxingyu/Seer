 package uk.co.brotherlogic.mdb;
 
 import junit.framework.TestCase;
 import uk.co.brotherlogic.mdb.parsers.DiscogParser;
 import uk.co.brotherlogic.mdb.record.Record;
 
 public class DiscogTest extends TestCase {
 
 	public DiscogTest() {
 		super();
 		Connect.setForDevMode();
 	}
 
	public void testDiscog() {
 		DiscogParser parser = new DiscogParser();
 
 		// This is MV & EE - Drone Trailer
 		try {
			Record r = parser.parseDiscogRelease(1642454);
 
 			// Check that the format tracks are correct
 			assert (r.getTracks().iterator().next().getFormTrackNumber() != -1);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 }
