 package tests;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.List;
 
 import vms.*;
 import vms.Alert.AlertType;
 
 import org.junit.Test;
 
 import common.Coord;
 import common.Course;
 import common.UpdateData;
 import common.Vessel;
 import common.Vessel.VesselType;
 
 public class RadarMonitorTest {
 	private boolean REFRESH_CALLED = false;
 	private List<Alert> ALERTS = null;
 	
 	private class FakeUI implements RadarMonitor.Observer {
 
 		@Override
 		public void refresh(List<Alert> alerts, List<Vessel> vessels) {
 			REFRESH_CALLED = true;
 			ALERTS = alerts;
 		}
 		
 	}
 	
 	private void _ResetUI() {
 		REFRESH_CALLED = false;
 		ALERTS = null;
 	}
 
 	@Test
 	public void test() {
 		RadarMonitor radar = new RadarMonitor();
 		radar.setRange(new Coord(-200, -200), new Coord(200, 200));
 		assertEquals(0, radar.getVesselCount());
 		assertArrayEquals(new Vessel[0], radar.getVessels().toArray());
 		
 		// XXX Additional tests forthcoming
 	}
 	
	@Test
 	public void testObservers() {
 		RadarMonitor radar = new RadarMonitor();
 		radar.setRange(new Coord(-2000, -2000), new Coord(2000, 2000));
 		radar.registerObserver(new FakeUI());
 
 		Calendar time = Calendar.getInstance();
 		radar.refresh(time);
 		assertTrue(REFRESH_CALLED);
 		assertEquals(0, ALERTS.size());
 		_ResetUI();
 		
 		//Enter first boat, check if refresh was called
 		radar.update(new UpdateData("fakeID", VesselType.BOAT, new Coord(0, 0), new Course(0,0), time));
 		assertTrue(REFRESH_CALLED);
 		assertEquals(0, ALERTS.size());
 		_ResetUI();
 
 		//Enter another boat within no-risk zone
 		radar.update(new UpdateData("fakeID2", VesselType.BOAT, new Coord(250, 250), new Course(0,0), time));
 		assertTrue(REFRESH_CALLED);
 		assertEquals(0, ALERTS.size());
 		_ResetUI();
 
 		//Move second boat within low-risk zone
		time.add(Calendar.SECOND, 1);
 		radar.update(new UpdateData("fakeID2", VesselType.BOAT, new Coord(100, 100), new Course(0,0), time));
 		assertTrue(REFRESH_CALLED);
 		assertEquals(1, ALERTS.size());
 		assertEquals(AlertType.LOWRISK, ALERTS.get(0).getType());
 		assertEquals("fakeID", ALERTS.get(0).getVessels().get(0).getId());
 		assertEquals("fakeID2", ALERTS.get(0).getVessels().get(1).getId());
 		_ResetUI();
 		
 		//Move second boat into high-risk zone, and set course to move away
		time.add(Calendar.SECOND, 1);
		radar.update(new UpdateData("fakeID2", VesselType.BOAT, new Coord(0, 0), new Course(100,100), time));
 		assertTrue(REFRESH_CALLED);
 		assertEquals(1, ALERTS.size());
 		assertEquals(AlertType.HIGHRISK, ALERTS.get(0).getType());
 		assertEquals("fakeID", ALERTS.get(0).getVessels().get(0).getId());
 		assertEquals("fakeID2", ALERTS.get(0).getVessels().get(1).getId());
 		_ResetUI();
 		
 		//After 1 second, second boat moves into low-risk
 		time.add(Calendar.SECOND, 1);
 		radar.refresh(time);
 		assertTrue(REFRESH_CALLED);
 		assertEquals(1, ALERTS.size());
 		assertEquals(AlertType.LOWRISK, ALERTS.get(0).getType());
 		assertEquals("fakeID", ALERTS.get(0).getVessels().get(0).getId());
 		assertEquals("fakeID2", ALERTS.get(0).getVessels().get(1).getId());
 		_ResetUI();
 		
 		//After another second, boat moves into no-risk
 		time.add(Calendar.SECOND, 1);
 		radar.refresh(time);
 		assertTrue(REFRESH_CALLED);
 		assertEquals(0, ALERTS.size());
 	}
 	
 	public void setUp() {
 		_ResetUI();
 	}
 }
