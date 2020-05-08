 package com.barchart.netty.util.point;
 
import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 
 public class TestNetUtil {
 
 	static final Logger log = LoggerFactory.getLogger(TestNetUtil.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPointForm() {
 
 		final Config conf = ConfigFactory.parseString("{ "
				+ "localAddress = datalan/12345, " //
				+ "remoteAddress = feedlan 23456, " //
				+ "packetTTL = 33, " //
 				+ "pipeline = advanced, " //
 				+ "custom1 = signature, " //
 				+ "custom2 = 1000, " //
 				+ "custom3 = [ 1, 2, 3 ], " //
 				+ "}");
 
 		final NetPoint point = NetPoint.from(conf);
 
 		log.debug("point : {}", point);
 
 		assertEquals("datalan", point.getLocalAddress().getHost());
 		assertEquals(12345, point.getLocalAddress().getPort());
 
 		assertEquals("feedlan", point.getRemoteAddress().getHost());
 		assertEquals(23456, point.getRemoteAddress().getPort());
 
 		assertEquals(33, point.getPacketTTL());
 
 		assertEquals("advanced", point.getPipeline());
 
 		assertEquals("signature", point.load("custom1"));
 
 		assertEquals(1000, point.load("custom2"));
 
 		final ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(1);
 		list.add(2);
 		list.add(3);
 		assertEquals(list, point.load("custom3"));
 
 	}
 
 }
