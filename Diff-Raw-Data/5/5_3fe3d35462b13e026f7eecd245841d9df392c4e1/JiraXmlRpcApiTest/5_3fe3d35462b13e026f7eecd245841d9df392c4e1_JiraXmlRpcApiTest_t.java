 package lv.jake.jiw.services;
 
 import junit.framework.TestCase;
 import lv.jake.jiw.domain.JiraIssue;
 
 import java.net.MalformedURLException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import static org.mockito.Mockito.*;
 /**
  * Author: Konstantin Zmanovsky
  * Date: Apr 16, 2010
  * Time: 2:29:43 PM
  */
 public class JiraXmlRpcApiTest extends TestCase {
     public void testConvertMapToJiraIssue() throws MalformedURLException, JiwServiceException {
         final HashMap<String, String> map = new HashMap<String, String>();
         map.put("key", "ISU-1");
         map.put("summary", "Issue Summary");
         map.put("priority", "Critical");
        map.put("created", "2010-03-22 12:35:37.0");
         map.put("duedate", "2010-04-22 00:00:00.0");
         map.put("updated", "2010-04-07 13:38:53.0");
 
         final JiraIssue issue = JiraXmlRpcApi.convertMapToJiraIssue(map);
 
        Calendar calendarCreated = new GregorianCalendar(2010, 02, 22, 12, 35, 37);
         Calendar calendarDue = new GregorianCalendar(2010, 03, 22, 0, 0, 0);
         Calendar calendarLastUpdated = new GregorianCalendar(2010, 03, 07, 13, 38, 53);
 
         assertEquals("ISU-1", issue.getKey());
         assertEquals("Issue Summary", issue.getSummary());
         assertEquals("Critical", issue.getPriority());
        assertEquals(calendarCreated.getTime(), issue.getCreatedDate());
         assertEquals(calendarDue.getTime(), issue.getDueDate());
         assertEquals(calendarLastUpdated.getTime(), issue.getLastUpdateDate());
     }
 }
