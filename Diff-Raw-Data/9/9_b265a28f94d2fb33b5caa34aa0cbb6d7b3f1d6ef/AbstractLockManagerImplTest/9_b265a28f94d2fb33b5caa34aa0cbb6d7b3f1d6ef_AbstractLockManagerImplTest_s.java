 package org.sakaiproject.nakamura.lite.lock;
 
 import java.io.IOException;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.lock.AlreadyLockedException;
 import org.sakaiproject.nakamura.api.lite.lock.LockState;
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 
 public abstract class AbstractLockManagerImplTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLockManagerImplTest.class);
     private ConfigurationImpl configuration;
     private StorageClientPool clientPool;
     private StorageClient client;
 
     @Before
     public void before() throws StorageClientException, AccessDeniedException, ClientPoolException,
             ClassNotFoundException, IOException {
 
         Map<String, Object> properties = Maps.newHashMap();
         properties.put("keyspace", "n");
         properties.put("acl-column-family", "ac");
         properties.put("authorizable-column-family", "au");
         properties.put("content-column-family", "cn");
        properties.put("lock-column-family", "ln");
         configuration = new ConfigurationImpl();
         configuration.activate(properties);
         clientPool = getClientPool(configuration);
         client = clientPool.getClient();
         AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                 configuration);
         authorizableActivator.setup();
         LOGGER.info("Setup Complete");
     }
 
     protected abstract StorageClientPool getClientPool(Configuration configuration)
             throws ClassNotFoundException;
 
     @Test
     public void testRootLock() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/", 10000, "Some Extra Information");
         
         checkOwnerLockWithToken(lockManagerImpl, "/", "/", token);
         checkOwnerLockWithToken(lockManagerImpl, "/", "/test/test234/sadsdf", token);
 
         checkOwnerLockWithNoToken(lockManagerImpl, "/", "/", "wrong-token");
         checkOwnerLockWithNoToken(lockManagerImpl, "/", "/test/test234/sadsdf", "wrong-token");
 
 
         lockManagerImpl.unlock("/", token);
         
         checkNotLocked(lockManagerImpl, "/", token);
         checkNotLocked(lockManagerImpl, "/werttrew", token);
     }
 
     @Test
     public void testDeepLock() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/sub/folder", 10000, "Some Extra Information");
         
         checkOwnerLockWithToken(lockManagerImpl, "/sub/folder", "/sub/folder", token);
         checkOwnerLockWithToken(lockManagerImpl, "/sub/folder", "/sub/folder/test/test234/sadsdf", token);
 
         checkNotLocked(lockManagerImpl, "/sub", token);
         checkNotLocked(lockManagerImpl, "/subtest/test123", token);
 
         lockManagerImpl.unlock("/sub/folder", token);
         
         checkNotLocked(lockManagerImpl, "/sub/folder", token);
         checkNotLocked(lockManagerImpl, "/sub.folder/test/test123", token);
 
     }
 
     @Test
     public void testDeepLockExpire() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException, InterruptedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/sub/expire", 2, "Some Extra Information");
         
         checkOwnerLockWithToken(lockManagerImpl, "/sub/expire", "/sub/expire", token);
         checkOwnerLockWithToken(lockManagerImpl, "/sub/expire", "/sub/expire/test234/sadsf", token);
         
         checkNotLocked(lockManagerImpl, "/sub", token);
         checkNotLocked(lockManagerImpl, "/subtest/test123", token);
 
 
         LOGGER.info("Sleeping for 2001ms to allow lock to expire");
         Thread.sleep(2001L);
 
         checkNotLocked(lockManagerImpl, "/sub/expire", token);
         checkNotLocked(lockManagerImpl, "/sub/expire/test23234", token);
         
         lockManagerImpl.unlock("/sub/expire", token);
 
     }
     
     private void checkOwnerLockWithToken(LockManagerImpl lockManagerImpl, String path, String testPath, String token) throws StorageClientException {
         Assert.assertTrue(lockManagerImpl.isLocked(testPath));
         LockState lockState = lockManagerImpl.getLockState(testPath, token);
         Assert.assertTrue(lockState.isOwner());
         Assert.assertEquals(path, lockState.getLockPath());
         Assert.assertTrue(lockState.hasMatchedToken());
         Assert.assertEquals(token, lockState.getToken());
     }
     private void checkOwnerLockWithNoToken(LockManagerImpl lockManagerImpl, String path, String testPath, String token) throws StorageClientException {
         Assert.assertTrue(lockManagerImpl.isLocked(testPath));
         LockState lockState = lockManagerImpl.getLockState(testPath, token);
         Assert.assertTrue(lockState.isOwner());
         Assert.assertEquals(path, lockState.getLockPath());
         Assert.assertFalse(lockState.hasMatchedToken());
     }
 
     private void checkLocked(LockManagerImpl lockManagerImpl, String path, String testPath, String token) throws StorageClientException {
         Assert.assertTrue(lockManagerImpl.isLocked(testPath));
         LockState lockState = lockManagerImpl.getLockState(testPath, token);
         Assert.assertFalse(lockState.isOwner());
         Assert.assertEquals(path, lockState.getLockPath());
         Assert.assertFalse(lockState.hasMatchedToken());
     }
 
     private void checkNotLocked(LockManagerImpl lockManagerImpl,String testPath, String token) throws StorageClientException {
         Assert.assertFalse(lockManagerImpl.isLocked(testPath));
         LockState lockState = lockManagerImpl.getLockState(testPath, token);
         Assert.assertFalse(lockState.isOwner());
         Assert.assertNull(lockState.getLockPath());
         Assert.assertFalse(lockState.hasMatchedToken());
     }
 
     @Test
     public void testOtherUserLock() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException, InterruptedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/sub/ieb", 2, "Some Extra Information");
         
         checkOwnerLockWithToken(lockManagerImpl, "/sub/ieb", "/sub/ieb", token);
         checkOwnerLockWithToken(lockManagerImpl, "/sub/ieb", "/sub/ieb/sdfsd/sdf/sdfsdf", token);
         
         
         User user2 = new User(ImmutableMap.of(User.ID_FIELD, (Object) "scl",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl2 = new LockManagerImpl(client, configuration, user2,
                 null);
         
         checkLocked(lockManagerImpl2, "/sub/ieb", "/sub/ieb", token);
         checkLocked(lockManagerImpl2, "/sub/ieb", "/sub/ieb/sdfs/sdf/sdf", token);
         
         
         lockManagerImpl.unlock("/sub/ieb", token);
 
 
     }
 
     
     @Test
     public void testOtherUserReLock() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException, InterruptedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/sub/iebrelock", 20, "Some Extra Information");
 
         checkOwnerLockWithToken(lockManagerImpl, "/sub/iebrelock", "/sub/iebrelock", token);
         checkOwnerLockWithToken(lockManagerImpl, "/sub/iebrelock", "/sub/iebrelock/sdfsd/sdf/sdfsdf", token);
         
         User user2 = new User(ImmutableMap.of(User.ID_FIELD, (Object) "scl",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl2 = new LockManagerImpl(client, configuration, user2,
                 null);
         try {
             String token2 = lockManagerImpl2.lock("/sub/iebrelock", 30, "Some More Information");
             Assert.fail(token2);
         } catch ( AlreadyLockedException e) {
             LOGGER.debug("Ok");
         }
         
         lockManagerImpl.unlock("/sub/iebrelock", token);
 
         
         
     }
     
     @Test
     public void testReLockExpire() throws StorageClientException, AccessDeniedException,
             AlreadyLockedException, InterruptedException {
         User currentUser = new User(ImmutableMap.of(User.ID_FIELD, (Object) "ieb",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl = new LockManagerImpl(client, configuration, currentUser,
                 null);
         String token = lockManagerImpl.lock("/sub/iebrelockexpire", 2, "Some Extra Information");
 
         checkOwnerLockWithToken(lockManagerImpl, "/sub/iebrelockexpire", "/sub/iebrelockexpire", token);
         checkOwnerLockWithToken(lockManagerImpl, "/sub/iebrelockexpire", "/sub/iebrelockexpire/sdfsd/sdf/sdfsdf", token);
 
 
 
         
         // the lock will have expired so we can replace it.
         User user2 = new User(ImmutableMap.of(User.ID_FIELD, (Object) "scl",
                 User.PASSWORD_FIELD, "test"));
         LockManagerImpl lockManagerImpl2 = new LockManagerImpl(client, configuration, user2,
                 null);
 
         checkLocked(lockManagerImpl2, "/sub/iebrelockexpire", "/sub/iebrelockexpire", token);
         checkLocked(lockManagerImpl2, "/sub/iebrelockexpire", "/sub/iebrelockexpire/sdf/sdf/sdf", token);
 
         LOGGER.info("Sleeping for 2001ms to allow lock to expire");
         Thread.sleep(2001L);
 
         checkNotLocked(lockManagerImpl, "/sub/iebrelockexpire", token);
         checkNotLocked(lockManagerImpl, "/sub/iebrelockexpire/sdf/sdf/sdf", token);
 
         checkNotLocked(lockManagerImpl2, "/sub/iebrelockexpire", token);
         checkNotLocked(lockManagerImpl2, "/sub/iebrelockexpire/sdf/sdf/sdf", token);
 
         String token2 = lockManagerImpl2.lock("/sub/iebrelockexpire", 30, "Some More Information");
 
         checkOwnerLockWithToken(lockManagerImpl2, "/sub/iebrelockexpire", "/sub/iebrelockexpire", token2);
         checkOwnerLockWithToken(lockManagerImpl2, "/sub/iebrelockexpire", "/sub/iebrelockexpire/sdfsd/sdf/sdfsdf", token2);
 
         checkLocked(lockManagerImpl, "/sub/iebrelockexpire", "/sub/iebrelockexpire", token);
         checkLocked(lockManagerImpl, "/sub/iebrelockexpire", "/sub/iebrelockexpire/sdf/sdf/sdf", token);
 
         
         lockManagerImpl2.unlock("/sub/iebexpire", token2);
    
         
     }
 
 
 }
