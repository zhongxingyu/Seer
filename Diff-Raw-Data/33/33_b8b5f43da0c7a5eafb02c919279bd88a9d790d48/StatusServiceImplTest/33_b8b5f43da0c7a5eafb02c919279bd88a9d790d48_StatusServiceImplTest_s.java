 package thesmith.eventhorizon.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import thesmith.eventhorizon.AppBaseTest;
 import thesmith.eventhorizon.model.Account;
 import thesmith.eventhorizon.model.Status;
 
 public class StatusServiceImplTest extends AppBaseTest {
   @Autowired
   private StatusService service;
   
   private Status status;
   
   @Override
   public void setUp() throws Exception {
     super.setUp();
     status = new Status();
     status.setDomain("domain");
     status.setPersonId("id"+Math.random());
     status.setCreated(new Date());
     status.setStatus("status");
   }
   
   @Test
   public void testShouldCreateStatus() throws Exception {
     service.create(status);
     assertNotNull(status.getId());
     
     Status stat = service.find(status.getPersonId(), status.getDomain(), status.getCreated());
     assertEquals(status.getStatus(), stat.getStatus());
     
     Date date = new Date(status.getCreated().getTime()+50L);
     stat = service.find(status.getPersonId(), status.getDomain(), date);
   }
   
   @Test
   public void testShouldCreateStatusesFromTwitter() throws Exception {
     Account account = new Account();
     account.setPersonId("thesmith"+Math.random());
     account.setDomain("twitter");
     account.setUserId("thesmith");
     account.setTemplate("{ago}, <a herf='{userUrl}'>I</a> <a href='{titleUrl}'>{title}</a>");
     
     List<Status> statuses = service.list(account, new Date());
     assertNotNull(statuses);
     assertTrue(statuses.size() > 0);
     Status stat = statuses.get(0);
     assertNotNull(stat);
     assertNotNull(stat.getStatus());
   }
 }
