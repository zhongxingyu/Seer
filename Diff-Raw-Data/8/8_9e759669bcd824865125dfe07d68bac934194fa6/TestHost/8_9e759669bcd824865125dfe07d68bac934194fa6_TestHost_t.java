 package name.xumingjun.rest;
 
 public class TestHost {
 	Host host = new Host();
 //	@Test
 	public void testHardware() {
 		Object s = host.getHardware();
 		System.out.println(s);
 	}
 //	@Test
 	public void testUpTime() {
 		for(String raw : new String [] {
 			"20:42:18 up 1 day, 11 min,  1 user,  load average: 0.27, 0.08, 0.07",
 			"20:42:18 up 1 day, 2:17,  1 user,  load average: 0.27, 0.08, 0.07",
 			"20:42:18 up 2342,  2 users,  load average: 0.27, 0.08, 0.07"
 		}) {
 			System.out.println(host.parseUpTime(raw));
 		}
 	}
 }
