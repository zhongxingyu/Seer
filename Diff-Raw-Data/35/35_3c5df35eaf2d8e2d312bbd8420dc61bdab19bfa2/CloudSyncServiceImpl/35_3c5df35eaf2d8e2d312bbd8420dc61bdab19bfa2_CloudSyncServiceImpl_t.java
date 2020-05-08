 package com.github.cwilper.fcrepo.cloudsync.service;
 
 import com.github.cwilper.fcrepo.cloudsync.api.CloudSyncService;
 import com.github.cwilper.fcrepo.cloudsync.api.Configuration;
 import com.github.cwilper.fcrepo.cloudsync.api.NameConflictException;
 import com.github.cwilper.fcrepo.cloudsync.api.ObjectInfo;
 import com.github.cwilper.fcrepo.cloudsync.api.ObjectSet;
 import com.github.cwilper.fcrepo.cloudsync.api.ObjectStore;
 import com.github.cwilper.fcrepo.cloudsync.api.ProviderAccount;
 import com.github.cwilper.fcrepo.cloudsync.api.ResourceInUseException;
 import com.github.cwilper.fcrepo.cloudsync.api.ResourceNotFoundException;
 import com.github.cwilper.fcrepo.cloudsync.api.Space;
 import com.github.cwilper.fcrepo.cloudsync.api.SystemLog;
 import com.github.cwilper.fcrepo.cloudsync.api.Task;
 import com.github.cwilper.fcrepo.cloudsync.api.TaskLog;
 import com.github.cwilper.fcrepo.cloudsync.api.User;
 import com.github.cwilper.fcrepo.cloudsync.service.backend.TaskManager;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.ConfigurationDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.DuraCloudDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.ObjectSetDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.ObjectStoreDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.SystemLogDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.TaskDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.TaskLogDao;
 import com.github.cwilper.fcrepo.cloudsync.service.dao.UserDao;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import javax.annotation.PreDestroy;
 import javax.sql.DataSource;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.List;
 
 public class CloudSyncServiceImpl implements CloudSyncService {
 
     private static final Logger logger = LoggerFactory.getLogger(CloudSyncServiceImpl.class);
 
     private final JdbcTemplate db;
 
     private final ConfigurationDao configurationDao;
     private final UserDao userDao;
     private final TaskDao taskDao;
     private final ObjectSetDao objectSetDao;
     private final ObjectStoreDao objectStoreDao;
     private final SystemLogDao systemLogDao;
     private final TaskLogDao taskLogDao;
     private final DuraCloudDao duraCloudDao;
 
     private final TaskManager taskManager;
 
     public CloudSyncServiceImpl(DataSource dataSource,
                                 PlatformTransactionManager txMan) {
         db = new JdbcTemplate(dataSource);
        TransactionTemplate tt = new TransactionTemplate(txMan);
 
         configurationDao = new ConfigurationDao(db);
         userDao = new UserDao(db);
         objectSetDao = new ObjectSetDao(db);
         objectStoreDao = new ObjectStoreDao(db);
         taskDao = new TaskDao(db, tt, objectSetDao, objectStoreDao);
         systemLogDao = new SystemLogDao(db);
         taskLogDao = new TaskLogDao(db);
         duraCloudDao = new DuraCloudDao();
 
         if (db.queryForInt("select count(*) from sys.systables where tablename = 'CLOUDSYNC'") == 0) {
             initDb();
         }
         logger.info("Service initialization complete. Ready to handle requests.");
 
         taskManager = new TaskManager(taskDao, taskLogDao, objectSetDao, objectStoreDao);
         taskManager.start();
     }
 
     @PreDestroy
     public void close() {
         taskManager.shutdown();
     }
 
     private void initDb() {
         logger.info("First run detected. Creating database tables.");
         db.execute("create table CloudSync(schemaVersion int)");
         db.update("insert into CloudSync values (1)");
         configurationDao.initDb();
         userDao.initDb();
         objectSetDao.initDb();
         objectStoreDao.initDb();
         taskDao.initDb();
         systemLogDao.initDb();
         taskLogDao.initDb();
     }
 
     // -----------------------------------------------------------------------
     //                            Configuration
     // -----------------------------------------------------------------------
 
     @Override
     public Configuration getConfiguration() {
         return configurationDao.getConfiguration();
     }
 
     @Override
     public Configuration updateConfiguration(Configuration configuration) {
         return configurationDao.updateConfiguration(configuration);
     }
 
     // -----------------------------------------------------------------------
     //                                Users
     // -----------------------------------------------------------------------
 
     @Override
     public User createUser(User user) throws NameConflictException {
         try {
             return userDao.createUser(user);
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("User name is already in use", e);
         }
     }
 
     @Override
     public List<User> listUsers() {
         return userDao.listUsers();
     }
 
     @Override
     public User getUser(String id) throws ResourceNotFoundException {
         User result = userDao.getUser(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such user: " + id);
         }
         return result;
     }
 
     @Override
     public User getCurrentUser() {
         return userDao.getCurrentUser();
     }
 
     @Override
     public User updateUser(String id, User user)
             throws ResourceNotFoundException, NameConflictException {
         try {
             User result = userDao.updateUser(id, user);
             if (result == null) {
                 throw new ResourceNotFoundException("No such user: " + id);
             }
             return result;
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("User name is already in use", e);
         }
     }
 
     @Override
     public void deleteUser(String id) throws ResourceInUseException {
         if (!id.equals(getCurrentUser().getId())) {
             userDao.deleteUser(id);
         } else {
             throw new ResourceInUseException("You can't delete yourself");
         }
     }
 
     // -----------------------------------------------------------------------
     //                                Tasks
     // -----------------------------------------------------------------------
 
     @Override
     public Task createTask(Task task) throws NameConflictException {
         try {
             return taskDao.createTask(task);
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Task name is already in use", e);
         }
     }
 
     @Override
     public List<Task> listTasks() {
         return taskDao.listTasks();
     }
 
     @Override
     public Task getTask(String id) throws ResourceNotFoundException {
         Task result = taskDao.getTask(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such task: " + id);
         }
         return result;
     }
 
     @Override
     public Task updateTask(String id, Task task)
             throws ResourceNotFoundException, NameConflictException {
         try {
             Task result = taskDao.updateTask(id, task);
             if (result == null) {
                 throw new ResourceNotFoundException("No such task: " + id);
             }
             return result;
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Task name is already in use", e);
         }
     }
 
     @Override
     public void deleteTask(String id) throws ResourceInUseException {
         if (taskDao.getTask(id).getState().equals(Task.IDLE)) {
             try {
                 taskDao.deleteTask(id);
             } catch (DataIntegrityViolationException e) {
                 throw new ResourceInUseException("Task cannot be deleted; it is being used by a task log", e);
             }
         } else {
             throw new ResourceInUseException("Task cannot be deleted while active");
         }
     }
 
     // -----------------------------------------------------------------------
     //                             Object Sets
     // -----------------------------------------------------------------------
 
     @Override
     public ObjectSet createObjectSet(ObjectSet objectSet)
             throws NameConflictException {
         try {
             return objectSetDao.createObjectSet(objectSet);
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Object set name is already in use", e);
         }
     }
 
     @Override
     public List<ObjectSet> listObjectSets() {
         return objectSetDao.listObjectSets();
     }
 
     @Override
     public ObjectSet getObjectSet(String id) throws ResourceNotFoundException {
         ObjectSet result = objectSetDao.getObjectSet(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such object set: " + id);
         }
         return result;
     }
 
     @Override
     public ObjectSet updateObjectSet(String id, ObjectSet objectSet)
             throws ResourceNotFoundException, NameConflictException {
         try {
             ObjectSet result = objectSetDao.updateObjectSet(id, objectSet);
             if (result == null) {
                 throw new ResourceNotFoundException("No such object set: " + id);
             }
             return result;
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Object set name is already in use", e);
         }
     }
 
     @Override
     public void deleteObjectSet(String id) throws ResourceInUseException {
         try {
             objectSetDao.deleteObjectSet(id);
         } catch (DataIntegrityViolationException e) {
             throw new ResourceInUseException("Object set is currently being used by a task", e);
         }
     }
 
     // -----------------------------------------------------------------------
     //                            Object Stores
     // -----------------------------------------------------------------------
 
     @Override
     public ObjectStore createObjectStore(ObjectStore objectStore)
             throws NameConflictException {
         try {
             return objectStoreDao.createObjectStore(objectStore);
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Object store name is already in use", e);
         }
     }
 
     @Override
     public List<ObjectStore> listObjectStores() {
         return objectStoreDao.listObjectStores();
     }
 
     @Override
     public ObjectStore getObjectStore(String id) throws ResourceNotFoundException {
         ObjectStore result = objectStoreDao.getObjectStore(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such object store: " + id);
         }
         return result;
     }
 
     @Override
     public List<ObjectInfo> queryObjectStore(String id, String set, long limit, long offset) {
         return objectStoreDao.queryObjectStore(id, set, limit, offset);
     }
 
     @Override
     public ObjectStore updateObjectStore(String id, ObjectStore objectStore)
             throws ResourceNotFoundException, NameConflictException {
         try {
             ObjectStore result = objectStoreDao.updateObjectStore(id, objectStore);
             if (result == null) {
                 throw new ResourceNotFoundException("No such object store: " + id);
             }
             return result;
         } catch (DuplicateKeyException e) {
             throw new NameConflictException("Object store name is already in use", e);
         }
     }
 
     @Override
     public void deleteObjectStore(String id) throws ResourceInUseException {
         try {
             objectStoreDao.deleteObjectStore(id);
         } catch (DataIntegrityViolationException e) {
             throw new ResourceInUseException("Object store is currently being used by a task", e);
         }
     }
 
     // -----------------------------------------------------------------------
     //                             System Logs
     // -----------------------------------------------------------------------
 
     @Override
     public List<SystemLog> listSystemLogs() {
         return systemLogDao.listSystemLogs();
     }
 
     @Override
     public SystemLog getSystemLog(String id) throws ResourceNotFoundException {
         SystemLog result = systemLogDao.getSystemLog(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such system log: " + id);
         }
         return result;
     }
 
     @Override
     public InputStream getSystemLogContent(String id) throws ResourceNotFoundException {
         try {
             return systemLogDao.getSystemLogContent(id);
         } catch (FileNotFoundException e) {
             throw new ResourceNotFoundException("No such system log: " + id, e);
         }
     }
 
     @Override
     public void deleteSystemLog(String id) {
         // TODO: throw ResourceInUseException if id = latest system log (still being written)
         systemLogDao.deleteSystemLog(id);
     }
 
     // -----------------------------------------------------------------------
     //                              Task Logs
     // -----------------------------------------------------------------------
 
     @Override
     public List<TaskLog> listTaskLogs() {
         return taskLogDao.listTaskLogs();
     }
 
     @Override
     public TaskLog getTaskLog(String id) throws ResourceNotFoundException {
         TaskLog result = taskLogDao.getTaskLog(id);
         if (result == null) {
             throw new ResourceNotFoundException("No such task log: " + id);
         }
         return result;
     }
 
     @Override
     public InputStream getTaskLogContent(String id) throws ResourceNotFoundException {
         try {
             return taskLogDao.getTaskLogContent(id);
         } catch (FileNotFoundException e) {
             throw new ResourceNotFoundException("No such task log: " + id, e);
         }
     }
 
     @Override
     public void deleteTaskLog(String id) {
         // TODO: throw ResourceInUseException if task log is still being written
         taskLogDao.deleteTaskLog(id);
     }
 
     // -----------------------------------------------------------------------
     //                               DuraCloud
     // -----------------------------------------------------------------------
 
     @Override
     public List<ProviderAccount> listProviderAccounts(String url,
                                                String username,
                                                String password) {
         return duraCloudDao.listProviderAccounts(url, username, password);
     }
 
     @Override
     public List<Space> listSpaces(String url,
                            String username,
                            String password,
                            String providerAccountId) {
         return duraCloudDao.listSpaces(url, username, password, providerAccountId);
     }
 }
