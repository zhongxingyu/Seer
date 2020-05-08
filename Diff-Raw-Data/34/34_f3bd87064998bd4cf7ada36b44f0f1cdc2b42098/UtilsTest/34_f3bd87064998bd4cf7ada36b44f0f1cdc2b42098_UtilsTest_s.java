 package jpower.core.test;
 
 import jpower.core.utils.NetUtils;
 import jpower.core.utils.ThreadUtils;
 import org.junit.Test;
 
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 
 import static org.junit.Assert.assertTrue;
 
 public class UtilsTest {
     @Test
     public void testNetPing() throws UnknownHostException {
         assertTrue(NetUtils.ping(Inet4Address.getLocalHost().getHostAddress()));
     }
 
     @Test
     public void testThreadSleep() {
         int time = 1000;
         int range = 20;
         long begin = System.currentTimeMillis();
         ThreadUtils.sleep(time);
         long end = System.currentTimeMillis();
         long slept = end - begin;
        boolean decent = (slept < (time + range)) || (slept > (time + range));
         assertTrue("Expected " + time + "ms of sleep, but got " + slept + "ms of sleep.", decent);
     }
 }
