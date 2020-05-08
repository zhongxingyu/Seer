 package thesmith.eventhorizon.controller;
 
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.ui.ModelMap;
 
 import thesmith.eventhorizon.model.Account;
 import thesmith.eventhorizon.model.Status;
 import thesmith.eventhorizon.service.AccountService;
 import thesmith.eventhorizon.service.StatusService;
 import thesmith.eventhorizon.service.UserService;
 
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 
 public class IndexControllerTest {
   private IndexController controller;
   
   private StatusService statusService;
   private UserService userService;
   private AccountService accountService;
   
   @Before
   public void setUp() throws Exception {    
     statusService = createMock(StatusService.class);
     userService = createMock(UserService.class);
     accountService = createMock(AccountService.class);
     
     controller = new IndexController();
     controller.setStatusService(statusService);
     controller.setAccountService(accountService);
     controller.setUserService(userService);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void shouldRetrieveIndexes() throws Exception {
     ModelMap model = new ModelMap();
     List<Account> accounts = Lists.newArrayList();
     Account account = new Account();
     account.setPersonId("person");
     account.setDomain("twitter");
     accounts.add(account);
     
     Account account2 = new Account();
     account2.setPersonId("person");
     account2.setDomain("lastfm");
     accounts.add(account2);
    EasyMock.expect(accountService.listAll("person")).andReturn(accounts);
     
     Status twitterStatus = new Status();
     twitterStatus.setDomain("twitter");
     twitterStatus.setPersonId("person");
     twitterStatus.setStatus("twitter status");
     EasyMock.expect(statusService.find(EasyMock.isA(Account.class), EasyMock.isA(Date.class))).andReturn(twitterStatus);
     
     Status lastfmStatus = new Status();
     lastfmStatus.setDomain("lastfm");
     lastfmStatus.setPersonId("person");
     lastfmStatus.setStatus("lastfm status");
     EasyMock.expect(statusService.find(EasyMock.isA(Account.class), EasyMock.isA(Date.class))).andReturn(lastfmStatus);
     EasyMock.replay(accountService, statusService, userService);
     
     String view = controller.index("person", 2010, 01, 01, 00, 00, 00, model);
     assertEquals("index/index", view);
     assertEquals("person", model.get("personId"));
     assertNotNull(model.get("from"));
     assertTrue(model.containsKey("statuses"));
     
     List<Status> statuses = (List<Status>) model.get("statuses");
     assertEquals(twitterStatus.getStatus(), statuses.get(0).getStatus());
     assertEquals(lastfmStatus.getStatus(), statuses.get(1).getStatus());
   }
 }
