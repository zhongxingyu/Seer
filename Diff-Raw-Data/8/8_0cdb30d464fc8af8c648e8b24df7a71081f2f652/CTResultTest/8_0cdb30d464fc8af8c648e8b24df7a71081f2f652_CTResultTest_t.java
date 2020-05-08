 
 package se.nctrl.jenkins.plugin;
 
 
import java.util.Calendar;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author karl l <karl@ninjacontrol.com>
  */
 public class CTResultTest {
     
     
 
     @Test
     public void testAddChild() {
         
         CTResult parent = new CTResult(null);
         CTResult child = new CTResult(null);
 
        
         parent.addChild(child);
         assertEquals(parent, child.getParent());
     
     }
 
     @Test
     public void testProps()
     {
         CTResult t = CTResultTestUtil.getMockResultObject();
        String tz = Calendar.getInstance().getTimeZone().getDisplayName(false, 0);
         
         assertEquals(t.getCases(), 128);
         assertEquals(t.getUser(),"karl");
         assertEquals(t.getHost(),"devbox");
         assertEquals(t.getHosts(),"devbox ");
         assertEquals(t.getEmulator_vsn(),"5.9.3");
         assertEquals(t.getEmulator(),"beam");
         assertEquals(t.getOtp_release(),"R15B03");
        assertEquals(t.getStarted().toString(),"Mon Nov 19 16:37:20 " + tz + " 2012");
        assertEquals(t.getFinished().toString(),"Mon Nov 19 17:07:56 " + tz + " 2012");
         assertEquals(t.getFailed(),0);
         assertEquals(t.getSuccessful(),127);
         assertEquals(t.getUser_skipped(),1);
         assertEquals(t.getAuto_skipped(),0);
     }
     
 }
