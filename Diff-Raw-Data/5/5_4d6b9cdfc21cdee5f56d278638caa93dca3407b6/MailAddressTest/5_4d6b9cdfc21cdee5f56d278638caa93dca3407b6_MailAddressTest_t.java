 package jamm.backend;
 
 import java.util.Set;
 import java.util.List;
 import java.util.HashSet;
 import javax.naming.NamingException;
 
 import junit.framework.TestCase;
 
 import jamm.backend.MailAddress;
 
 public class MailAddressTest extends TestCase
 {
     public MailAddressTest(String name)
     {
         super(name);
     }
 
     public void testHostFromAddress()
     {
         assertEquals("testing with user@host address",
                      "realtors.org",
                      MailAddress.hostFromAddress(mUserAtHost));
 
        assertNull("testing with user address only",
                   MailAddress.hostFromAddress(mUserOnly));
     }
 
     public void testUserFromAddress()
     {
         assertEquals("testing with user@host address",
                      "kgarner",
                      MailAddress.userFromAddress(mUserAtHost));
 
         assertEquals("testing with user address only",
                      "root",
                      MailAddress.userFromAddress(mUserOnly));
     }
     
     String mUserAtHost = "kgarner@realtors.org";
     String mUserOnly = "root";
 }
