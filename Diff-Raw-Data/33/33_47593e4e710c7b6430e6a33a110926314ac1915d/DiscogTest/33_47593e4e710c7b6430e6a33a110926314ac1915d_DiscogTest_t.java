 package uk.co.brotherlogic.mdb;
 
 import junit.framework.TestCase;
 import uk.co.brotherlogic.mdb.parsers.DiscogParser;
 import uk.co.brotherlogic.mdb.record.Record;
 
/**
 * Testing the discogs parser
 * 
 * @author simon
 * 
 */
 public class DiscogTest extends TestCase {
 
	/** MV & EE - Drone Trailer */
	private static final int DISCOG_NUMBER = 1642454;

	/**
	 * Constructor
	 */
 	public DiscogTest() {
 		super();
 		Connect.setForDevMode();
 	}
 
	/**
	 * Main test method
	 */
	public final void testDiscog() {
 		DiscogParser parser = new DiscogParser();
 
 		// This is MV & EE - Drone Trailer
 		try {
			Record r = parser.parseDiscogRelease(DISCOG_NUMBER);
 
 			// Check that the format tracks are correct
 			assert (r.getTracks().iterator().next().getFormTrackNumber() != -1);
			assert (r.getDiscogsNum() == DISCOG_NUMBER);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 }
