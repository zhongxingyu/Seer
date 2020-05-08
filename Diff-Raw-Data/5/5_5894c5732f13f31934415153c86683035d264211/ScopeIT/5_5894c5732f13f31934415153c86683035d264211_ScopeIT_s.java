 package org.osiam.client;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.connector.OsiamConnector;
 import org.osiam.client.exception.ForbiddenException;
 import org.osiam.client.oauth.AccessToken;
 import org.osiam.client.oauth.GrantType;
 import org.osiam.client.oauth.Scope;
 import org.osiam.client.query.Query;
 import org.osiam.client.update.UpdateGroup;
 import org.osiam.client.update.UpdateUser;
 import org.osiam.resources.scim.Group;
 import org.osiam.resources.scim.SCIMSearchResult;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 import com.github.springtestdbunit.annotation.ExpectedDatabase;
 import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed_scope.xml")
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 public class ScopeIT {
 
     private static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
     private static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
     protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
     protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
     private static final String CLIENT_ID = "example-client";
     private static final String CLIENT_SECRET = "secret";
     private OsiamConnector oConnector;
     private AccessToken accessToken;
     private OsiamConnector.Builder oConBuilder;
 
     @Before
     public void setUp() throws Exception {
 
         oConBuilder = new OsiamConnector.Builder()
                 .setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS)
                 .setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                 .setClientId(CLIENT_ID)
                 .setClientSecret(CLIENT_SECRET)
                 .setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS)
                 .setUserName("marissa")
                 .setPassword("koala");
     }
 
     @Test(expected = ForbiddenException.class)
     public void getting_user_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         retrieveUser();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void getting_group_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         retrieveGroup();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void getting_all_users_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         getAllUsers();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void getting_all_groups_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         getAllGroups();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void creating_a_user_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         createUser();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void creating_a_group_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         createGroup();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void getting_current_user_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         getCurrentUser();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void updating_user_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         updateUser();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void updating_group_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         updateGroup();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void searching_for_user_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         searchForUsers();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void searching_for_group_in_DELETE_scope_raises_exception() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         searchForGroups();
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void deleting_user_in_GET_scope_raises_exception() {
         setScope(Scope.GET);
         retrieveAccessToken();
         oConnector.deleteUser(VALID_USER_ID, accessToken);
         fail("Exception expected");
     }
 
     @Test(expected = ForbiddenException.class)
     public void deleting_group_in_GET_scope_raises_exception() {
         setScope(Scope.GET);
         retrieveAccessToken();
         oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
         fail("Exception expected");
     }
 
     @Test
     public void get_user_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(retrieveUser(), is(notNullValue()));
     }
 
     @Test
     public void get_group_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(retrieveGroup(), is(notNullValue()));
     }
 
     @Test
     public void get_all_users_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(getAllUsers(), is(notNullValue()));
     }
 
     @Test
     public void get_all_groups_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(getAllGroups(), is(notNullValue()));
     }
 
     @Test
     @Ignore("/User/me is no longer available and '/me' is not yet supported by connector")
     public void get_current_user_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(getCurrentUser(), is(notNullValue()));
     }
 
     @Test
     public void create_user_in_POST_scope_works() {
         setScope(Scope.POST);
         retrieveAccessToken();
         assertThat(createUser(), is(notNullValue()));
     }
 
     @Test
     public void create_group_in_POST_scope_works() {
         setScope(Scope.POST);
         retrieveAccessToken();
         assertThat(createGroup(), is(notNullValue()));
     }
 
     @Test
     public void update_user_in_PATCH_scope_works() {
         setScope(Scope.PATCH);
         retrieveAccessToken();
         assertThat(updateUser(), is(notNullValue()));
     }
 
     @Test
     public void update_group_in_PATCH_scope_works() {
         oConnector = oConBuilder.setScope(Scope.PATCH).build();
         retrieveAccessToken();
         assertThat(updateGroup(), is(notNullValue()));
     }
 
     @Test
     public void search_for_users_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(searchForUsers(), is(notNullValue()));
     }
 
     @Test
     public void search_for_groups_in_GET_scope_works() {
         setScope(Scope.GET);
         retrieveAccessToken();
         assertThat(searchForGroups(), is(notNullValue()));
     }
 
     private void setScope(Scope scope) {
         oConnector = oConBuilder.setScope(scope).build();
     }
 
     @Test
     @ExpectedDatabase(value = "/database_expected_scope_delete_user.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
     public void delete_user_in_DELETE_scope_works() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         oConnector.deleteUser(VALID_USER_ID, accessToken);
     }
 
     @Test
     @ExpectedDatabase(value = "/database_expected_scope_delete_group.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
     public void delete_group_in_DELETE_scope_works() {
         setScope(Scope.DELETE);
         retrieveAccessToken();
         oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
     }
 
     private User createUser() {
         User user = new User.Builder("userName").build();
         return oConnector.createUser(user, accessToken);
     }
 
     private Group createGroup() {
         Group group = new Group.Builder().setDisplayName("displayName").build();
         return oConnector.createGroup(group, accessToken);
     }
 
     private void retrieveAccessToken() {
         accessToken = oConnector.retrieveAccessToken();
     }
 
     private User retrieveUser() {
         return oConnector.getUser(VALID_USER_ID, accessToken);
     }
 
     private Group retrieveGroup() {
         return oConnector.getGroup(VALID_GROUP_ID, accessToken);
     }
 
     private List<User> getAllUsers() {
         return oConnector.getAllUsers(accessToken);
     }
 
     private List<Group> getAllGroups() {
         return oConnector.getAllGroups(accessToken);
     }
 
     private User getCurrentUser() {
         return oConnector.getMe(accessToken);
     }
 
     private User updateUser() {
         UpdateUser updateUser = new UpdateUser.Builder().updateUserName("newUserName").updateActive(false).build();
         return oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
     }
 
     private Group updateGroup() {
         UpdateGroup updateGroup = new UpdateGroup.Builder().updateDisplayName("irrelevant").build();
         return oConnector.updateGroup(VALID_GROUP_ID, updateGroup, accessToken);
     }
 
     private SCIMSearchResult<User> searchForUsers() {
        Query query = new Query.Builder(User.class).setStartIndex(0).build();
         return oConnector.searchUsers(query, accessToken);
     }
 
     private SCIMSearchResult<Group> searchForGroups() {
        Query query = new Query.Builder(Group.class).setStartIndex(0).build();
         return oConnector.searchGroups(query, accessToken);
     }
 
 }
