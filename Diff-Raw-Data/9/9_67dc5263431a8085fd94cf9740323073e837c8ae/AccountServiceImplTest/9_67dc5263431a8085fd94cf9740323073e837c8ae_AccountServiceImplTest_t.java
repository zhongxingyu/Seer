 package thesmith.eventhorizon.service;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import thesmith.eventhorizon.AppBaseTest;
 import thesmith.eventhorizon.model.Account;
 
 public class AccountServiceImplTest extends AppBaseTest {
   @Autowired
   private AccountService service;
   
   private Account account;
   
   @Override
   public void setUp() throws Exception {
     super.setUp();
     account = new Account();
     account.setDomain(AccountService.DOMAIN.twitter.toString());
     account.setPersonId("id"+Math.random());
     account.setUserId("id");
     account.setTemplate("template");
   }
   
   @Test
   public void testShouldCreateAccount() throws Exception {
     service.create(account);
     assertNotNull(account.getId());
     
     Account acc = service.find(account.getPersonId(), account.getDomain());
     assertEquals(account.getUserId(), acc.getUserId());
     
     assertEquals(1, service.list(account.getPersonId()).size());
 
     service.delete(account.getPersonId(), account.getDomain());
     assertEquals(0, service.list(account.getPersonId()).size());
   }
   
   @Test
   public void testShouldListAll() throws Exception {
     service.create(account);
     assertNotNull(account.getId());
     
     List<Account> accounts = service.listAll(account.getPersonId());
 
     Account flickr = null;
     for (Account account: accounts) {
       if ("lastfm".equals(account.getDomain()))
         flickr = account;
     }
     assertNotNull(flickr);
    assertNull(flickr.getUserId());
   }
   
   @Test
   public void testShouldOnlyCreateAccountOnce() throws Exception {
     String personId = "blah"+Math.random();
     for (int i=0; i<5; i++) {
       this.createAccount(personId, AccountService.DOMAIN.flickr.toString(), null);
     }
     
     service.find(personId, "somedomain");
   }
   
   private void createAccount(String personId, String domain, Date processed) {
     Account account = new Account();
     account.setDomain(domain);
     account.setPersonId(personId);
     account.setUserId("id");
     account.setTemplate("template");
     account.setProcessed(processed);
     service.create(account);
   }
   
   @Test
   public void testShouldGetAccountsToProcess() throws Exception {
     Calendar cal = Calendar.getInstance();
     cal.add(Calendar.DAY_OF_WEEK, 1);
     
     this.createAccount("someguy"+Math.random(), AccountService.DOMAIN.flickr.toString(), cal.getTime());
     this.createAccount("someotherguy"+Math.random(), AccountService.DOMAIN.flickr.toString(), null);
     this.createAccount("not", AccountService.DOMAIN.flickr.toString(), new Date());
     
     List<Account> toProcess = service.toProcess(10);
     assertNotNull(toProcess);
     for (Account account: toProcess) {
       assertNotSame("not", account.getPersonId());
       service.delete(account.getPersonId(), account.getDomain());
     }
   }
 }
