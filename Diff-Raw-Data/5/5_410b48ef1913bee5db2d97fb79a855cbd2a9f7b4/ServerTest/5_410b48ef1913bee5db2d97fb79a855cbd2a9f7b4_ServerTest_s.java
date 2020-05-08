 package edu.colorado.csci3308.inventory;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class ServerTest {
 
 	@Test
 	public void testServerConstructor1() {
 		Server s = new Server("This is a test.");
 		assertTrue(s.getHostname().equals("This is a test."));
 	}
 //
 	@Test
 	public void testServerConstructor2() {
 		Server s = new Server(new Integer(1),"server1.eng.sldomaserver1in.com","10.1.0.101","01:23:45:67:89:ab", 0,null ,null,"","aaa",true,false,"");
 		assertTrue(s.getServerId().equals(1));
 	}
 	
 	private Server makeServer(){
 		return new Server(new Integer(1), "server1.eng.sldomaserver1in.com", "10.1.0.101", "01:23:45:67:89:ab",
 				250, 12, 123, 1234, new ChassisModel(1, "Model-T", 40, 314), new Motherboard(1, "DoD", "F-22", "defense.gov"), "Processor1, Processor2", "Code Tangerine",
 				true, true, "Card 1, Card 2");
 	}
 
 	@Test
 	public void testServerConstructor3() {
 		Server s = makeServer();
 		assertTrue(s.getServerId().equals(1));
 	}
 
 	@Test
 	public void testGetMotherboard() {
 		Server s = makeServer();
 		Motherboard m = s.getMotherboard();
 		Motherboard n = new Motherboard(1, "DoD", "F-22", "defense.gov");
 		assertTrue(m.getId().equals(n.getId()) && m.getManufacturer().equals(n.getManufacturer()) &&
 				m.getModel().equals(n.getModel()) && m.getUrl().equals(n.getUrl()));
 	}
 
 	@Test
 	public void testGetServerId() {
 		Server s = makeServer();
 		assertTrue(s.getServerId().equals(1));
 	}
 
 	@Test
 	public void testGetScanDate() {
 		assertTrue(Server.getScanDate() == null);
 	}
 
 	@Test
 	public void testGetHostname() {
 		Server s = makeServer();
 		assertTrue(s.getHostname().equals("server1.eng.sldomaserver1in.com"));
 	}
 
 	@Test
 	public void testGetIpAddress() {
 		Server s = makeServer();
 		assertTrue(s.getIpAddress().equals("10.1.0.101"));
 	}
 
 	@Test
 	public void testGetMacAddress() {
 		Server s = makeServer();
 		assertTrue(s.getMacAddress().equals("01:23:45:67:89:ab"));
 	}
 
 	@Test
 	public void testGetTotalMemory() {
 		Server s = makeServer();
 		assertTrue(s.getTotalMemory().equals(250));
 	}
 
 	@Test
 	public void testGetTopHeight() {
 		Server s = makeServer();
 		assertTrue(s.getTopHeight().equals(12));
 	}
 
 	@Test
 	public void testSetTopHeight() {
 		Server s = makeServer();
 		s.setTopHeight(654);
 		assertTrue(s.getTopHeight().equals(654));
 	}
 
 	@Test
 	public void testGetLocationId() {
 		Server s = makeServer();
 		assertTrue(s.getLocationId().equals(123));
 	}
 
 	@Test
 	public void testGetRackId() {
 		Server s = makeServer();
 		assertTrue(s.getRackId().equals(1234));
 	}
 
 	@Test
 	public void testGetChassisModel() {
 		Server s = makeServer();
 		ChassisModel c = s.getChassisModel();
 		ChassisModel d = new ChassisModel(1, "Model-T", 40, 314);
 		assertTrue(c.getHeight().equals(d.getHeight()) && c.getId().equals(d.getId()) && c.getMaxDataDrives().equals(d.getMaxDataDrives()) &&
 				c.getModelDescription().equals(d.getModelDescription()));
 	}
 
 	@Test
 	public void testGetProcessors() {
 		Server s = makeServer();
 		assertTrue(s.getProcessors().equals("Processor1, Processor2"));
 	}
 
 	@Test
 	public void testGetProcessorsArray() {
 		Server s = makeServer();
 		String[] a = new String[2];
 		a[0] = "Processor1";
 		a[1]= " Processor2";
 		assertTrue(s.getProcessorsArray()[0].equals(a[0]) &&
 				s.getProcessorsArray()[1].equals(a[1]));
 	}
 
 	@Test
 	public void testGetSystemname() {
 		Server s = makeServer();
 		assertTrue(s.getSystemname() == null);
 	}
 
 	@Test
 	public void testGetServerScanDate() {
 		Server s = makeServer();
 		assertTrue(s.getServerScanDate() == null);
 	}
 
 	@Test
 	public void testGetCodeRevision() {
 		Server s = makeServer();
 		assertTrue(s.getCodeRevision().equals("Code Tangerine"));
 	}
 
 	@Test
 	public void testSetLocationId() {
 		Server s = makeServer();
 		s.setLocationId(123456789);
 		assertTrue(s.getLocationId().equals(123456789));
 	}
 
 	@Test
 	public void testSetRackId() {
 		Server s = makeServer();
 		s.setRackId(147852369);
 		assertTrue(s.getRackId().equals(147852369));
 	}
 
 	@Test
 	public void testIsPingable() {
 		Server s = makeServer();
 		assertTrue(s.isPingable());
 	}
 
 	@Test
 	public void testLastScanOK() {
 		Server s = makeServer();
 		assertTrue(s.lastScanOK());
 	}
 
 	@Test
 	public void testGetCards() {
 		Server s = makeServer();
 		assertTrue(s.getCards().equals("Card 1, Card 2"));
 	}
 	
 	@Test
 	public void testGetCardsArray(){
 		Server s = makeServer();
		assertTrue(s.getCardsArray().equals(new String[] {"Card 1", " Card 2"}));
 	}
 /*  -------------------------------------------------------------------
  * We cannot test these functions with JUnit
 	@Test
 	public void testScan() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testScanSystemName() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testScanCards() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testShutdown() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testExecute() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testSudoExecute() {
 		fail("Not yet implemented");
 	}
 */
 	@Test
 	public void testToString() {
 		Server s = makeServer();
 		assertTrue(s.toString().equals("Server " + s.getServerId() + ": " + s.getHostname() + " " + s.getIpAddress() + " " + 
 				s.getMacAddress() + " " + s.getTotalMemory() + " " + s.getTopHeight() + " " + s.getLocationId()	+ " " + s.getRackId() + 
 				" " + s.getChassisModel() + " " + s.getMotherboard() + " " + s.getProcessors() + " " + s.getCards() + " " + s.getSystemname()
 				+ " " + s.getScanDate() + " " + s.getServerScanDate()));
 	}
 
 }
