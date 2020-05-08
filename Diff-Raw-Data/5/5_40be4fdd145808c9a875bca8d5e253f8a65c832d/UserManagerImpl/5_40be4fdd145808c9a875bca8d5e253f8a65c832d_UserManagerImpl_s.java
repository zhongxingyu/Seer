 package polly.core.users;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import polly.configuration.PollyConfiguration;
 import polly.core.persistence.PersistenceManagerImpl;
 import polly.data.Attribute;
 import polly.events.Dispatchable;
 import polly.events.EventProvider;
 import polly.util.CaseInsensitiveStringKeyMap;
 
 import de.skuzzle.polly.parsing.Prepare;
 import de.skuzzle.polly.parsing.declarations.Declaration;
 import de.skuzzle.polly.parsing.declarations.Declarations;
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.PersistenceManager;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.WriteAction;
 import de.skuzzle.polly.sdk.constraints.AttributeConstraint;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.UserEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserListener;
 import de.skuzzle.polly.sdk.exceptions.AlreadySignedOnException;
 import de.skuzzle.polly.sdk.exceptions.ConstraintException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.UnknownAttributeException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  */
 public class UserManagerImpl extends AbstractDisposable implements UserManager {
 
     private static Logger logger = Logger.getLogger(UserManagerImpl.class.getName());
     
     
     private final static AttributeConstraint NO_CONSTRAINT = new AttributeConstraint() {
         
         @Override
         public boolean accept(String value) {
             return true;
         }
     };
     
     
     private PersistenceManagerImpl persistence;
 
     /**
      * Stores the currently signed on users. Key: the nickname in lower case.
      */
     private Map<String, User> onlineCache;
     private File declarationCachePath;
     private Map<String, AttributeConstraint> constraints;
     private EventProvider eventProvider;
     private Namespace namespace;
     private User admin;
     private boolean registeredChanged;
     private List<User> registeredUsers;
     
     
     
     public UserManagerImpl(PersistenceManagerImpl persistence, 
             PollyConfiguration cfg, EventProvider eventProvider) {
         this.eventProvider = eventProvider;
         this.persistence = persistence;
         this.onlineCache = Collections.synchronizedMap(
                 new CaseInsensitiveStringKeyMap<User>());
         this.declarationCachePath = new File(cfg.getDeclarationCachePath());
         this.constraints = new HashMap<String, AttributeConstraint>();
         this.namespace = new Namespace();
         try {
 			this.namespace.restore(new File(cfg.getDeclarationCachePath()));
 		} catch (IOException e) {
 			logger.warn("No declarations restored", e);
 		}
         Prepare.operators(this.namespace);
         Prepare.namespaces(this.namespace);
         Namespace.setTempVarLifeTime(cfg.getTempVarLifeTime());
         Namespace.setIgnoreUnknownIdentifiers(cfg.isIgnoreUnknownIdentifiers());
     }
     
     
     
     public Namespace getNamespace() {
         return this.namespace;
     }
     
     
 
     @Override
     public void deleteDeclaration(User user, String id) {
         Declarations d = this.namespace.getNamespaceFor(user.getName());
         if (d == null) {
             return;
         }
         d.remove(id);
     }
     
     
 
     @Override
     public synchronized Set<String> getDeclaredIdentifiers(String namespace) {
         Declarations d = this.namespace.getNamespaceFor(namespace);
         if (d == null) {
             return Collections.emptySet();
         }
         
         Set<Declaration> decls = d.getDeclarations();
         Set<String> result = new HashSet<String>();
         for (Declaration decl : decls) {
             result.add(decl.toString());
         }
         return result;
     }
     
     
     
     public void setAdmin(User admin) {
         this.admin = admin;
     }
     
     
     
     public User getAdmin() {
         return this.admin;
     }
     
     
     
     @Override
     public User getUser(IrcUser user) {
         return this.onlineCache.get(user.getNickName());
     }
 
     
     
     @Override
     public User getUser(String registeredName) {
         try {
             this.persistence.readLock();
             return this.persistence.findSingle(User.class, 
                     "USER_BY_NAME", registeredName);
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
 
     @Override
     public User updateUser(User old, User updated) {
         try {
             this.persistence.writeLock();
             this.persistence.startTransaction();
             old.setHashedPassword(updated.getHashedPassword());
             old.setUserLevel(updated.getUserLevel());
             this.persistence.commitTransaction();
             logger.info("User '" + old + "' updated to '" + updated + "'");
         } catch (DatabaseException e) {
             e.printStackTrace();
         } finally {
             this.persistence.writeUnlock();
         }
         return old;
     }
     
     
     
     public void addUser(User user) throws UserExistsException, DatabaseException {
         try {
             this.persistence.writeLock();
             User check = this.persistence.findSingle(User.class, "USER_BY_NAME", 
                     user.getName());
             
             if (check != null) {
                 logger.trace("User already exists.");
                 throw new UserExistsException(check);
             }
             this.persistence.startTransaction();
             this.persistence.persist(user);
             this.persistence.commitTransaction();
             this.registeredChanged = true;
             logger.info("Added user " + user);
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
 
     @Override
     public void addUser(String name, String password, int level) 
             throws UserExistsException, DatabaseException {
        this.addUser(new polly.data.User(name, password, level));
     }
 
     
     
     @Override
     public void deleteUser(User user) throws DatabaseException {
         try {
             this.persistence.writeLock();
             this.logoff(user);
             this.persistence.startTransaction();
             this.persistence.remove(user);
             this.persistence.commitTransaction();
             this.registeredChanged = true;
             logger.info("Deleted user " + user);
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
     
     
     @Override
     public synchronized User logon(String from, String registeredName, String password) 
             throws UnknownUserException, AlreadySignedOnException {
         logger.info("Trying to log on user '" + registeredName + "'.");
         User user = this.getUser(registeredName);
         if (user == null) {
             throw new UnknownUserException(registeredName);
         }
         
         this.checkAlreadySignedOn(user);
         
         if (user.checkPassword(password)) {
             user.setCurrentNickName(from);
             this.onlineCache.put(user.getCurrentNickName(), user);
             logger.info("Irc User " + from + " successfully logged in as " + 
                     registeredName);
             
             ((polly.data.User) user).setLoginTime(System.currentTimeMillis());
             UserEvent e = new UserEvent(this, user);
             this.fireUserSignedOn(e);
             return user;
         }
         
         logger.warn("Login from '" + from + "' with user name '" + registeredName + 
                 "' failed: Invalid password.");
         return null;
     }
     
     
     
     public synchronized User logonWithoutPassword(String from) throws 
                 AlreadySignedOnException, UnknownUserException {
         logger.info("Trying to autologon user '" + from + "'.");
         User user = this.getUser(from);
         if (user == null) {
             throw new UnknownUserException(from);
         }
         
         this.checkAlreadySignedOn(user);
         
         user.setCurrentNickName(from);
         this.onlineCache.put(user.getCurrentNickName(), user);
         logger.info("Irc User " + from + " successfully logged in as " + 
                 from);
         
         ((polly.data.User) user).setLoginTime(System.currentTimeMillis());
         UserEvent e = new UserEvent(this, user);
         this.fireUserSignedOn(e);
         return user;
     }
     
     
     
     private void checkAlreadySignedOn(User user) throws AlreadySignedOnException {        
         if (this.onlineCache.containsKey(user.getCurrentNickName())) {
             throw new AlreadySignedOnException(user.getName());
         }
     }
 
     
     
     @Override
     public void logoff(User user) {
         logger.info("User " + user + " logged off.");
         UserEvent e = new UserEvent(this, user);
         this.onlineCache.remove(user.getCurrentNickName());
         
         this.fireUserSignedOff(e);
     }
     
     
     
     @Override
     public void logoff(IrcUser user) {
         this.logoff(user, false);
     }
     
     
     
     public synchronized void logoff(IrcUser user, boolean auto) {
         logger.info("User " + user + " logged off.");
         UserEvent e = new UserEvent(this, this.getUser(user), auto);
         this.onlineCache.remove(user.getNickName());
 
         this.fireUserSignedOff(e);
     }
     
     
     
     public synchronized void logoffAll() {
         //  HACK: copy users to not get a concurrent modification exception
         Collection<User> online = new ArrayList<User>(this.onlineCache.values());
         
         for (User user : online) {
             IrcUser tmp = new IrcUser(user.getCurrentNickName(), "", "");
             this.logoff(tmp, true);
         }
     }
     
 
 
     @Override
     public boolean isSignedOn(IrcUser user) {
         return this.onlineCache.containsKey(user.getNickName());
     }
     
     
     
     @Override
     public boolean isSignedOn(User user) {
         return this.onlineCache.containsKey(user.getCurrentNickName());
     }
     
     
     
     @Override
     public List<User> getRegisteredUsers() {
         if (this.registeredChanged || this.registeredUsers == null) {
             try {
                 this.persistence.readLock();
                 this.registeredUsers = 
                     this.persistence.findList(User.class, polly.data.User.ALL_USERS);
             } finally {
                 this.persistence.readUnlock();
             }
         }
         return this.registeredUsers;
     }
     
     
     
     @Override
     public Collection<User> getOnlineUsers() {
         return Collections.unmodifiableCollection(this.onlineCache.values());
     }
     
     
     
     public synchronized void traceNickChange(IrcUser oldUser, IrcUser newUser) {
         logger.debug("Tracing nickchange from '" + oldUser + "' to '" + newUser + "'");
         User tmp = this.onlineCache.get(oldUser.getNickName());
         tmp.setCurrentNickName(newUser.getNickName());
         this.onlineCache.remove(oldUser.getNickName());
         this.onlineCache.put(newUser.getNickName(), tmp);
     }
     
     
     
     public List<Attribute> getAllAttributes() {
         try {
             this.persistence.readLock();
             return this.persistence.findList(Attribute.class, Attribute.ALL_ATTRIBUTES);
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
     
     @Override
     public void addAttribute(final String name, final String defaultValue, 
             AttributeConstraint constraint) throws DatabaseException {
         
         try {
             this.persistence.writeLock();
             
             this.constraints.put(name.toLowerCase(), constraint);
             
             Attribute att = new Attribute(name, defaultValue);
             Attribute check = this.persistence.findSingle(Attribute.class, 
                     "ATTRIBUTE_BY_NAME", name);
             
             if (check != null) {
                 logger.trace("Tried to add an attribute that already existed: " + att + 
                         ". Existing attribute: " + check);
                 return;
             }
             List<User> all = this.persistence.findList(User.class, "ALL_USERS");
             this.persistence.startTransaction();
             this.persistence.persist(att);
             
             logger.trace("Adding new attribute to each user.");
             for (User user : all) {
                 polly.data.User u = (polly.data.User) user;
                 u.getAttributes().put(name, defaultValue);
             }
             this.persistence.commitTransaction();
             logger.info("Attribute " + att + " added.");
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
     
     @Override
     public void setAttributeFor(final User user, final String attribute, 
             String value) throws DatabaseException, ConstraintException {
         // check if attribute exists:
         user.getAttribute(attribute);
         
         if (value.equalsIgnoreCase("%default%")) {
             Attribute attr = this.persistence.findSingle(Attribute.class, 
                 "ATTRIBUTE_BY_NAME", attribute);
             
             value = attr.getDefaultValue();
         }
         
         final String valueCopy = value;
         AttributeConstraint constraint = this.constraints.get(attribute.toLowerCase());
         if (!constraint.accept(value)) {
             throw new ConstraintException("'" + value + 
                "' ist kein gltiger Wert fr das Attribut '" + attribute + "'");
         }
         
         this.persistence.atomicWriteOperation(new WriteAction() {
             
             @Override
             public void performUpdate(PersistenceManager persistence) {
                 ((polly.data.User) user).setAttribute(attribute, valueCopy);
             }
         });
     }
 
 
 
     @Override
     public void addAttribute(String name, String defaultValue) throws DatabaseException {
         this.addAttribute(name, defaultValue, NO_CONSTRAINT);
     }
 
 
 
     @Override
     public void removeAttribute(String name) throws DatabaseException {
         try {
             this.persistence.writeLock();
             List<User> all = this.persistence.findList(User.class, "ALL_USERS");
             Attribute att = this.persistence.findSingle(
                     Attribute.class, "ATTRIBUTE_BY_NAME", name);
             
             if (att == null) {
                 throw new UnknownAttributeException(name);
             }
             
             this.persistence.startTransaction();
             logger.trace("Removing attribute from all users.");
             for (User user : all) {
                 polly.data.User u = (polly.data.User) user;
                 u.getAttributes().remove(name);
             }
             this.persistence.remove(att);
             this.persistence.commitTransaction();
             this.constraints.remove(name);
             logger.info("Attribute " + att + " removed.");
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         logger.debug("Storing declaration cache to disk.");
         try {
             this.namespace.store(this.declarationCachePath);
             this.namespace.dispose();
         } catch (IOException e) {
             logger.error("Error while storing namespaces",e);
         }
         this.persistence = null;
         this.onlineCache.clear();
         this.onlineCache = null;
         this.namespace = null;
     }
 
 
 
     @Override
     public User createUser(String name, String password, int userLevel) {
         polly.data.User result = new polly.data.User(name, password, userLevel);
         logger.trace("Adding all attributes to new user.");
         for (Attribute att : this.getAllAttributes()) {
             result.getAttributes().put(att.getName(), att.getDefaultValue());
         }
         return result;
     }
     
     
     
     @Override
     public void addUserListener(UserListener listener) {
         this.eventProvider.addListener(UserListener.class, listener);
     }
     
     
     
     @Override
     public void removeUserListener(UserListener listener) {
         this.eventProvider.removeListener(UserListener.class, listener);
     }
     
     
     
     protected void fireUserSignedOn(final UserEvent e) {
         final List<UserListener> listeners = 
             this.eventProvider.getListeners(UserListener.class);
         
         Dispatchable<UserListener, UserEvent> d = 
             new Dispatchable<UserListener, UserEvent>(listeners, e) {
                 @Override
                 public void dispatch(UserListener listener, UserEvent event) {
                     listener.userSignedOn(event);
                 }
         };
         this.eventProvider.dispatchEvent(d);
     }
     
     
     
     protected void fireUserSignedOff(final UserEvent e) {
         final List<UserListener> listeners = 
             this.eventProvider.getListeners(UserListener.class);
         
         Dispatchable<UserListener, UserEvent> d = 
             new Dispatchable<UserListener, UserEvent>(listeners, e) {
                 @Override
                 public void dispatch(UserListener listener, UserEvent event) {
                     listener.userSignedOff(event);
                 }
         };
         this.eventProvider.dispatchEvent(d);
     }
 }
