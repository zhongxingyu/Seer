 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.authorizable;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.sakaiproject.nakamura.lite.LoggingStorageListener;
 import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
 import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
 import org.sakaiproject.nakamura.lite.accesscontrol.PrincipalValidatorResolverImpl;
 import org.sakaiproject.nakamura.lite.storage.ConcurrentLRUMap;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractAuthorizableManagerImplTest {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(AbstractAuthorizableManagerImplTest.class);
     private StorageClient client;
     private ConfigurationImpl configuration;
     private StorageClientPool clientPool;
     private Map<String, CacheHolder> sharedCache = new ConcurrentLRUMap<String, CacheHolder>(1000);
     private PrincipalValidatorResolver principalValidatorResolver = new PrincipalValidatorResolverImpl();
 
     @Before
     public void before() throws StorageClientException, AccessDeniedException, ClientPoolException,
             ClassNotFoundException, IOException {
         configuration = new ConfigurationImpl();
         Map<String, Object> properties = Maps.newHashMap();
         properties.put("keyspace", "n");
         properties.put("acl-column-family", "ac");
         properties.put("authorizable-column-family", "au");
         configuration.activate(properties);
         clientPool = getClientPool(configuration);
         client = clientPool.getClient();
         AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                 configuration);
         authorizableActivator.setup();
         LOGGER.info("Setup Complete");
     }
 
     protected abstract StorageClientPool getClientPool(Configuration configuration2) throws ClassNotFoundException;
 
     @After
     public void after() throws ClientPoolException {
        if ( client != null ) {
            client.close();
        }
     }
 
     @Test
     public void testAuthorizableManager() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         Assert.assertNotNull(currentUser);
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         Assert.assertNotNull(authorizableManager.findAuthorizable(User.ADMIN_USER));
         Assert.assertNotNull(authorizableManager.findAuthorizable(User.ANON_USER));
         Assert.assertEquals(currentUser, authorizableManager.getUser());
     }
 
     @Test
     public void testAuthorizableManagerAccessDenied() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "wrong-password");
 
         Assert.assertNull(currentUser);
     }
 
     @Test
     public void testAuthorizableManagerUserNotFound() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("nonuser", "wrong-password");
 
         Assert.assertNull(currentUser);
     }
 
     @Test
     public void testAuthorizableManagerCheckUser() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         Authorizable a = authorizableManager.findAuthorizable(User.ADMIN_USER);
         Authorizable an = authorizableManager.findAuthorizable(User.ANON_USER);
         Authorizable missing = authorizableManager.findAuthorizable("missinguser");
         Assert.assertNull(missing);
         Assert.assertNotNull(a);
         Assert.assertNotNull(an);
         Assert.assertFalse(a instanceof Group);
         Assert.assertFalse(an instanceof Group);
         User user = (User) a;
         String[] principals = user.getPrincipals();
         Assert.assertNotNull(principals);
         Assert.assertEquals(1, principals.length);
         Assert.assertTrue(user.isAdmin());
 
         User anon = (User) an;
         principals = anon.getPrincipals();
         Assert.assertNotNull(principals);
         Assert.assertEquals(0, principals.length);
         Assert.assertFalse(anon.isAdmin());
 
     }
 
     @Test
     public void testAuthorizableManagerCreateUser() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         authorizableManager.delete("testuser");
 
         Assert.assertTrue(authorizableManager.createUser("testuser", "Test User", "test",
                 ImmutableMap.of("testkey", (Object) "testvalue", Authorizable.PRINCIPALS_FIELD,
                         "administrators;testers", Authorizable.AUTHORIZABLE_TYPE_FIELD,
                         Authorizable.GROUP_VALUE)));
         Assert.assertFalse(authorizableManager.createUser("testuser", "Test User", "test",
                 ImmutableMap.of("testkey", (Object) "testvalue", Authorizable.PRINCIPALS_FIELD,
                         "administrators;testers")));
 
         Authorizable a = authorizableManager.findAuthorizable("testuser");
         Assert.assertNotNull(a);
         Assert.assertFalse(a instanceof Group);
         User user = (User) a;
         String[] principals = user.getPrincipals();
         Assert.assertNotNull(principals);
         LOGGER.info("Principals {} ", Arrays.toString(principals));
         Assert.assertEquals(3, principals.length);
         Assert.assertTrue(user.isAdmin());
 
     }
 
     @Test
     public void testAuthorizableManagerCreateUserDenied() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         authorizableManager.delete("testuser2");
 
         Assert.assertTrue(authorizableManager.createUser("testuser2", "Test User", "test",
                 ImmutableMap.of("testkey", (Object) "testvalue", Authorizable.PRINCIPALS_FIELD, "testers",
                         Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE)));
         Assert.assertFalse(authorizableManager.createUser("testuser2", "Test User", "test",
                 ImmutableMap.of("testkey", (Object) "testvalue", Authorizable.PRINCIPALS_FIELD,
                         "administrators;testers")));
 
         Authorizable a = authorizableManager.findAuthorizable("testuser2");
         Assert.assertNotNull(a);
         Assert.assertFalse(a instanceof Group);
         User user = (User) a;
         String[] principals = user.getPrincipals();
         LOGGER.info("Principals {} ", Arrays.toString(principals));
         Assert.assertArrayEquals(new String[] { "testers", Group.EVERYONE }, principals);
 
         Assert.assertFalse(user.isAdmin());
 
         AccessControlManagerImpl userAccessControlManagerImpl = new AccessControlManagerImpl(
                 client, user, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
         AuthorizableManagerImpl userAuthorizableManager = new AuthorizableManagerImpl(user, client,
                 configuration, userAccessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         try {
             userAuthorizableManager.createUser("testuser3", "Test User", "test", ImmutableMap.of(
                     "testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers",
                     Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE));
             Assert.fail();
         } catch (AccessDeniedException e) {
             LOGGER.info(" Correctly denied access {} ", e.getMessage());
         }
 
         try {
             userAuthorizableManager.createUser("testuser4", "Test User", "test", ImmutableMap.of(
                     "testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers"));
             Assert.fail();
         } catch (AccessDeniedException e) {
             LOGGER.info(" Correctly denied access {} ", e.getMessage());
         }
 
     }
 
     @Test
     public void testAuthorizableManagerCreateGroup() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         authorizableManager.delete("user2");
         authorizableManager.delete("user3");
         authorizableManager.delete("testgroup");
 
         Assert.assertTrue(authorizableManager.createUser("user2", "TestUser2", null, ImmutableMap
                 .of("testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers")));
         Assert.assertTrue(authorizableManager.createUser("user3", "TestUser", null, ImmutableMap
                 .of("testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers")));
         Assert.assertTrue(authorizableManager.createGroup("testgroup", "Test Group", ImmutableMap
                 .of("testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers",
                         Authorizable.MEMBERS_FIELD, "user1;user2")));
         Assert.assertFalse(authorizableManager.createGroup("testgroup", "Test Group", ImmutableMap
                 .of("testkey", (Object) "testvalue",  Authorizable.PRINCIPALS_FIELD, "administrators;testers",
                         Authorizable.MEMBERS_FIELD, "user1;user2", Authorizable.AUTHORIZABLE_TYPE_FIELD,
                         Authorizable.GROUP_VALUE)));
 
         Authorizable a = authorizableManager.findAuthorizable("testgroup");
         Assert.assertNotNull(a);
         Assert.assertTrue(a instanceof Group);
         Group g = (Group) a;
         String[] principals = g.getPrincipals();
         LOGGER.info("Principals {} ", Arrays.toString(principals));
         Assert.assertArrayEquals(new String[] { "administrators", "testers", Group.EVERYONE }, principals);
         String[] members = g.getMembers();
         LOGGER.info("Members {} ", Arrays.toString(members));
         Assert.assertArrayEquals(new String[] { "user1", "user2" }, members);
 
         g.setProperty("SomeValue", "AValue");
         g.setProperty(Authorizable.PASSWORD_FIELD, "badpassword");
         g.removeProperty("testkey");
         g.addPrincipal("tester2");
         g.removePrincipal("testers");
         // adding user 3 should make it a member of testgroup and give it the
         // pricipal testgroup
         g.addMember("user3");
         g.removeMember("user2");
 
         principals = g.getPrincipals();
         List<String> principalList = Lists.newArrayList(principals);
         Collections.sort(principalList);
         principals = principalList.toArray(new String[principalList.size()]);
         LOGGER.info("Principals before save {} ", Arrays.toString(principals));
         Assert.assertArrayEquals(new String[] { "administrators", Group.EVERYONE, "tester2"  }, principals);
         members = g.getMembers();
         LOGGER.info("Members {} ", Arrays.toString(members));
         Assert.assertArrayEquals(new String[] { "user1", "user3" }, members);
 
         LOGGER.info("Updating Group with changed membership ----------------------");
         authorizableManager.updateAuthorizable(g);
         LOGGER.info("Done Updating Group with changed membership ----------------------");
 
         Authorizable a2 = authorizableManager.findAuthorizable("testgroup");
         Assert.assertNotNull(a2);
         Assert.assertTrue(a2 instanceof Group);
         Group g2 = (Group) a2;
         principals = g2.getPrincipals();
         LOGGER.info("Principals {} ", Arrays.toString(principals));
         principalList = Lists.newArrayList(principals);
         Collections.sort(principalList);
         principals = principalList.toArray(new String[principalList.size()]);
         Assert.assertArrayEquals(new String[] { "administrators", Group.EVERYONE, "tester2" }, principals);
         members = g2.getMembers();
         LOGGER.info("Members {} ", Arrays.toString(members));
         Assert.assertArrayEquals(new String[] { "user1", "user3" }, members);
         Assert.assertNull(g2.getProperty(Authorizable.PASSWORD_FIELD));
 
         // Test that User3 no has testgroup as a principal.
         Authorizable a3 = authorizableManager.findAuthorizable("user3");
         Assert.assertNotNull(a3);
         Assert.assertFalse(a3 instanceof Group);
         User u3 = (User) a3;
         principals = u3.getPrincipals();
         LOGGER.info("Principals {} ", Arrays.toString(principals));
         Assert.assertArrayEquals(new String[] { "administrators", "testers", "testgroup",
             Group.EVERYONE },
                 principals);
 
     }
 
     @Test
     public void testFindAuthorizable() throws StorageClientException, AccessDeniedException {
         try {
             AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
             User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
             AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
                     client, currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
             AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                     client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
             for (int i = 0; i < 10; i++) {
                 authorizableManager.delete("testfinduser" + i);
                 Assert.assertTrue(authorizableManager.createUser("testfinduser" + i, "TestUser",
                         null, ImmutableMap.of("rep:principalName", (Object) ("principal" + i),
                                 "sakai:groupproperty", "groupprop", "sakai:userprop", "userprop")));
                 authorizableManager.delete("testgroup" + i);
                 Assert.assertTrue(authorizableManager.createGroup("testgroup" + i,
                         "Test Group" + i, ImmutableMap.of("rep:principalName",
                                 (Object) ("principal" + i), "sakai:groupproperty", "groupprop",
                                 "sakai:grprop", "grprop")));
             }
             for (int i = 0; i < 10; i++) {
                 Iterator<Authorizable> userIterator = authorizableManager.findAuthorizable(
                         "rep:principalName", "principal" + i, User.class);
                 Assert.assertNotNull(userIterator);
                 Assert.assertTrue(userIterator.hasNext());
                 Authorizable a = userIterator.next();
                 Assert.assertFalse(userIterator.hasNext());
                 Assert.assertTrue(a instanceof User);
                 User u = (User) a;
                 Assert.assertEquals("testfinduser" + i, u.getId());
             }
             for (int i = 0; i < 10; i++) {
                 Iterator<Authorizable> groupIterator = authorizableManager.findAuthorizable(
                         "rep:principalName", "principal" + i, Group.class);
                 Assert.assertNotNull(groupIterator);
                 Assert.assertTrue(groupIterator.hasNext());
                 Authorizable a = groupIterator.next();
                 Assert.assertFalse(groupIterator.hasNext());
                 Assert.assertTrue(a instanceof Group);
                 Group u = (Group) a;
                 Assert.assertEquals("testgroup" + i, u.getId());
             }
             for (int i = 0; i < 10; i++) {
                 Iterator<Authorizable> groupIterator = authorizableManager.findAuthorizable(
                         "rep:principalName", "principal" + i, Authorizable.class);
                 Assert.assertNotNull(groupIterator);
                 Assert.assertTrue(groupIterator.hasNext());
                 Authorizable a = groupIterator.next();
                 if (a instanceof Group) {
                     Assert.assertEquals("testgroup" + i, a.getId());
                 } else {
                     Assert.assertEquals("testfinduser" + i, a.getId());
                 }
                 Assert.assertTrue(groupIterator.hasNext());
                 a = groupIterator.next();
                 if (a instanceof Group) {
                     Assert.assertEquals("testgroup" + i, a.getId());
                 } else {
                     Assert.assertEquals("testfinduser" + i, a.getId());
                 }
                 Assert.assertFalse(groupIterator.hasNext());
             }
         } catch (UnsupportedOperationException e) {
             LOGGER.warn("Finder methods not implemented, FIXME");
         }
 
     }
 
     @Test
     public void testAuthorizableManagerNullProperties() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
         User currentUser = AuthenticatorImpl.authenticate("admin", "admin");
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());
 
         authorizableManager.delete("testuser");
 
         Assert.assertTrue(authorizableManager.createUser("testuser", "Test User", "test",
                 null));
         Authorizable user = authorizableManager.findAuthorizable("testuser");
         Assert.assertNotNull(user);
         Assert.assertTrue(user instanceof User);
 
         authorizableManager.delete("testgroup");
         Assert.assertTrue(authorizableManager.createGroup("testgroup", "Test Group", null));
         Authorizable group = authorizableManager.findAuthorizable("testgroup");
         Assert.assertNotNull(group);
         Assert.assertTrue(group instanceof Group);
     }
 
 }
