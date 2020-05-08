 package org.deegree.securityproxy.logging;
 
import org.deegree.securityproxy.logger.Log4jSecurityRequestResponseLogger;
 import org.deegree.securityproxy.logger.SecurityRequestResposeLogger;
 import org.junit.Test;
 
 /**
  * Tests for {@link Log4JSecurityRequestResponseLogger}
  * 
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 public class Log4JReportLoggerTest {
 
     @Test(expected = IllegalArgumentException.class)
     public void testReportLoggerLogInfoShouldThrowIllegalArgumentExceptionOnNullReport() {
        SecurityRequestResposeLogger logger = new Log4jSecurityRequestResponseLogger();
         logger.logProxyReportInfo( null );
     }
 }
