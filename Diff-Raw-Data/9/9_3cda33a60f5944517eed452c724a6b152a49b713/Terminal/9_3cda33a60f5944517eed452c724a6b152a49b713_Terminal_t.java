 package netproj.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import netproj.hosts.SimpleHost;
 import netproj.hosts.StaticRouter;
 import netproj.skeleton.Device;
 import netproj.skeleton.Link;
 import netproj.skeleton.Message;
 
 public class Terminal {
 	public static void main(String args[]) {
 		example();
 	}
 	
 	/* This example is running on a network like this:
 	 * 
 	 *    H1   H3   H4
 	 *    |    |    |
 	 *    R1 - R2 - R3 - H5
 	 *    |         |
 	 *    H2        H6
 	 * 
 	 * ...where H1 is host1, R1 is router1, etc.
 	 * Every link is a 1024 bps bidirectional link, and all devices have buffer size 10.
 	 * 
 	 * Holy crap guys, we need a better way to specify networks than pure code.
 	 * This is a million lines of code for a simplistic 9-device network.
 	 */
 	static void example() {
 		StaticRouter router1 = new StaticRouter(10, 3, 0x100);
 		SimpleHost host1 = new SimpleHost(0x101);
 		SimpleHost host2 = new SimpleHost(0x102);
 		StaticRouter router2 = new StaticRouter(10, 3, 0x200);
 		SimpleHost host3 = new SimpleHost(0x201);
 		StaticRouter router3 = new StaticRouter(10, 4, 0x300);
 		SimpleHost host4 = new SimpleHost(0x301);
 		SimpleHost host5 = new SimpleHost(0x302);
 		SimpleHost host6 = new SimpleHost(0x303);
 		List<Device> devs;
 		Link link;
 		
 		// Connect R1 and R2
 		devs = new ArrayList<Device>();
 		devs.add(router1);
 		devs.add(router2);
 		link = new Link(10, 1024, devs);
 		router1.setLink(0, link);
 		router2.setLink(0, link);
 		
 		// Connect R2 and R3
 		devs = new ArrayList<Device>();
 		devs.add(router2);
 		devs.add(router3);
 		link = new Link(10, 1024, devs);
 		router2.setLink(1, link);
 		router3.setLink(0, link);
 		
 		// Connect H1 and R1
 		devs = new ArrayList<Device>();
 		devs.add(host1);
 		devs.add(router1);
 		link = new Link(10, 1024, devs);
 		host1.setLink(0, link);
 		router1.setLink(1, link);
 		
 		// Connect H2 and R1
 		devs = new ArrayList<Device>();
 		devs.add(host2);
 		devs.add(router1);
 		link = new Link(10, 1024, devs);
 		host2.setLink(0, link);
 		router1.setLink(2, link);
 		
 		// Connect H3 and R2
 		devs = new ArrayList<Device>();
 		devs.add(host3);
 		devs.add(router2);
 		link = new Link(10, 1024, devs);
 		host3.setLink(0, link);
 		router2.setLink(2, link);
 		
 		// Connect H4 and R3
 		devs = new ArrayList<Device>();
 		devs.add(host4);
 		devs.add(router3);
 		link = new Link(10, 1024, devs);
 		host4.setLink(0, link);
 		router3.setLink(1, link);
 		
 		// Connect H5 and R3
 		devs = new ArrayList<Device>();
 		devs.add(host5);
 		devs.add(router3);
 		link = new Link(10, 1024, devs);
 		host5.setLink(0, link);
 		router3.setLink(2, link);
 		
 		// Connect H6 and R3
 		devs = new ArrayList<Device>();
 		devs.add(host6);
 		devs.add(router3);
 		link = new Link(10, 1024, devs);
 		host6.setLink(0, link);
 		router3.setLink(3, link);
 		
 		// Give R1 a routing table
 		router1.addRoutingTableEntry(host1.getAddress(), 32, 1);
 		router1.addRoutingTableEntry(host2.getAddress(), 32, 2);
 		router1.addRoutingTableEntry(0, 0, 0);
 		
 		// Give R2 a routing table
		router2.addRoutingTableEntry(host1.getAddress(), 24, 0);
 		router2.addRoutingTableEntry(host3.getAddress(), 32, 2);
 		router2.addRoutingTableEntry(0, 0, 1);
 		
 		// Give R3 a routing table
 		router3.addRoutingTableEntry(0, 0, 0);
 		router3.addRoutingTableEntry(host4.getAddress(), 32, 1);
 		router3.addRoutingTableEntry(host5.getAddress(), 32, 2);
 		router3.addRoutingTableEntry(host6.getAddress(), 32, 3);
 		
 		System.err.println("Sending from host1 to host6");
 		host1.sendMessage(new Message(host1.getAddress(), host6.getAddress(), 1024), 0);
 		System.err.println("Sending from host5 to host1");
 		host5.sendMessage(new Message(host5.getAddress(), host1.getAddress(), 1024), 0);
 		System.err.println("Sending from host3 to host4");
 		host3.sendMessage(new Message(host3.getAddress(), host4.getAddress(), 1024), 0);
 	}
 }
