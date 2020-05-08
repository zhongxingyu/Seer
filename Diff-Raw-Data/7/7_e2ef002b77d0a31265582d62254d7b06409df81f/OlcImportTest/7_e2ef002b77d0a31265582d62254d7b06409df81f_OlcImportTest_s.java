 package ch.ubx.startlist.server.admin;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.ubx.startlist.server.AirfieldDAOobjectify;
 import ch.ubx.startlist.server.FeGenDAOobjectify;
 import ch.ubx.startlist.shared.Airfield;
 import ch.ubx.startlist.shared.FeFlightEntry;
 import ch.ubx.startlist.shared.FePlace;
 import ch.ubx.startlist.shared.FeYear;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.googlecode.objectify.Key;
 
 public class OlcImportTest {
 
 	private final static LocalDatastoreServiceTestConfig datastore = new LocalDatastoreServiceTestConfig();
 	private final static LocalServiceTestHelper helper = new LocalServiceTestHelper(datastore);
 	private static FeGenDAOobjectify<FeYear, Long> yearDAO;
 	private static FeGenDAOobjectify<FePlace, String> placeDAO;
 	private static AirfieldDAOobjectify airfieldDAO;
 	private static Key<FeYear> currentYear;
	private static Key<FePlace> p1k;
 	private static String aa = "TOCUM1";
 	private static String c = "AU";
 
 	@Before
 	public void setUp() throws Exception {
 		helper.setUp();
 		yearDAO = new FeGenDAOobjectify<FeYear, Long>(FeYear.class);
 		currentYear = yearDAO.getOrCreateKey(2012L);
 		placeDAO = new FeGenDAOobjectify<FePlace, String>(FePlace.class);
 		airfieldDAO = new AirfieldDAOobjectify();
 		Airfield af = new Airfield();
 		af.setName(aa);
 		af.setId(aa);
 		af.setCountry(c);
 		airfieldDAO.addAirfield(af);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		helper.tearDown();
 	}
 
 	@Test
 	public void testImportFromOLC() {
		p1k = placeDAO.getOrCreateKey(aa, currentYear);
 		List<FeFlightEntry> list = OlcImport.importFromOLC(aa, 2012, 5);
 		assertEquals("Should import 5 Flight", 5, list.size());
 		for (FeFlightEntry fe : list) {
 			assertEquals("Landing place should be " + aa, aa, fe.getLandingPlace());
 		}
 		FeFlightEntry fe = list.get(0);
 		assertEquals("CompetitionID wrong", "ES", fe.getCompetitionID());
 		assertEquals("Club wrong", "Southern Riverina GC", fe.getClub());
 		assertEquals("Pilot wrong", "Bernie Sizer", fe.getPilot());
 		assertEquals("RegistrationGlider wrong", "VH-GES", fe.getRegistrationGlider());
 	}
 
 }
