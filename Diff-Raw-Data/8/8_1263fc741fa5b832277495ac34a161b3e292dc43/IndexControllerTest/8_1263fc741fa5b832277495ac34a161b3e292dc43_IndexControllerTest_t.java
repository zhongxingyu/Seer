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
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.ui.ModelMap;
 
 import thesmith.eventhorizon.DataStoreBaseTest;
 import thesmith.eventhorizon.model.Account;
 import thesmith.eventhorizon.model.Snapshot;
 import thesmith.eventhorizon.model.Status;
 import thesmith.eventhorizon.service.AccountService;
 import thesmith.eventhorizon.service.SnapshotService;
 import thesmith.eventhorizon.service.StatusService;
 import thesmith.eventhorizon.service.UserService;
 import thesmith.eventhorizon.service.impl.AccountServiceImpl;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 
 public class IndexControllerTest extends DataStoreBaseTest {
   private IndexController controller;
 
   @Autowired
   private StatusService statusService;
   private UserService userService;
   private AccountService accountService;
   private SnapshotService snapshotService;
 
   @Before
   public void setUp() throws Exception {
     super.setUp();
     userService = createMock(UserService.class);
     accountService = createMock(AccountService.class);
     snapshotService = createMock(SnapshotService.class);
 
     controller = new IndexController();
     controller.setStatusService(statusService);
     controller.setAccountService(accountService);
     controller.setUserService(userService);
     controller.setSnapshotService(snapshotService);
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void shouldRetrieveIndexes() throws Exception {
     ModelMap model = new ModelMap();
     
     List<Account> accounts = Lists.newArrayList();
     Account account = new Account();
     account.setPersonId("person");
     account.setDomain("twitter");
     account.setTemplate(AccountServiceImpl.defaults.get(account.getDomain()));
     accounts.add(account);
 
     Account account2 = new Account();
     account2.setPersonId("person");
     account2.setDomain("lastfm");
     account2.setTemplate(AccountServiceImpl.defaults.get(account.getDomain()));
     accounts.add(account2);
    EasyMock.expect(accountService.list("person")).andReturn(accounts);
     
     List<Key> statusIds = Lists.newArrayList();
     Status twitterStatus = new Status();
     twitterStatus.setDomain("twitter");
     twitterStatus.setPersonId("person");
     twitterStatus.setTitle("title");
     twitterStatus.setTitleUrl("titleUrl");
     twitterStatus.setCreated(new Date());
     statusService.create(twitterStatus);
     statusIds.add(twitterStatus.getId());
 
     Status lastfmStatus = new Status();
     lastfmStatus.setDomain("lastfm");
     lastfmStatus.setPersonId("person");
     lastfmStatus.setTitle("title");
     lastfmStatus.setTitleUrl("titleUrl");
     lastfmStatus.setCreated(new Date());
     statusService.create(lastfmStatus);
     statusIds.add(lastfmStatus.getId());
     
     Snapshot snapshot = new Snapshot();
     snapshot.setPersonId("person");
     snapshot.setCreated(new Date());
     snapshot.setStatusIds(statusIds);
     EasyMock.expect(snapshotService.find(EasyMock.matches("person"), EasyMock.isA(Date.class))).andReturn(snapshot);
 
     EasyMock.replay(accountService, userService, snapshotService);
 
     String view = controller.index("person", 2010, 01, 01, 00, 00, 00, model, new MockHttpServletRequest());
     assertEquals("index/index", view);
     assertEquals("person", model.get("personId"));
     assertNotNull(model.get("from"));
     assertTrue(model.containsKey("statuses"));
 
     List<Status> statuses = (List<Status>) model.get("statuses");
     assertNotNull(statuses.get(0).getStatus());
     assertNotNull(statuses.get(1).getStatus());
   }
 }
