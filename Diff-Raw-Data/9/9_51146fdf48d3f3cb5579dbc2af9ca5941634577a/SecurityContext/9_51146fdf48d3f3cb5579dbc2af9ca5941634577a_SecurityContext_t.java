 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.security;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.proc.Procedure;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.storage.Storage;
 import org.rapidcontext.core.storage.StorageException;
 import org.rapidcontext.core.type.Role;
 import org.rapidcontext.util.ArrayUtil;
 import org.rapidcontext.util.BinaryUtil;
 
 /**
  * The application security context. This class provides static
  * methods for authentication and resource authorization. It stores
  * the currently authenticated user in a thread-local storage, so
  * user credentials must be provided separately for each execution
  * thread. It is important that the manager is initialized before
 * any authentication calls are made, or they will fail.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class SecurityContext {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(SecurityContext.class.getName());
 
     /**
      * The user authentication realm.
      */
     // TODO: setup user realm in some non-hardcoded way
     public static final String REALM = "RapidContext";
 
     /**
      * The user object storage path.
      */
     public static final Path USER_PATH = new Path("/user/");
 
     /**
      * The data storage used for reading and writing configuration
      * data.
      */
     private static Storage dataStorage = null;
 
     /**
      * The map of all users. The map is indexed by the user names and
      * contains user objects.
      */
     private static HashMap users = new HashMap();
 
     /**
      * The currently authenticated users. This is a thread-local
      * variable containing one user object per thread. If the value
      * is set to null, no user is currently authenticated for the
      * thread.
      */
     private static ThreadLocal authUser = new ThreadLocal();
 
     /**
      * Initializes the security context. It can be called multiple
      * times in order to re-read the configuration data from the
      * data storage. The data store specified will be used for
      * reading and writing users and roles both during initialization
      * and later.
      *
      * @param storage        the data storage to use
      *
      * @throws StorageException if the storage couldn't be read or
      *             written
      */
     public static void init(Storage storage) throws StorageException {
         Object[]  objs;
         User      user;
 
         dataStorage = storage;
         users.clear();
         // TODO: What if there are many users?
         objs = dataStorage.loadAll(USER_PATH);
         for (int i = 0; i < objs.length; i++) {
             if (objs[i] instanceof Dict) {
                 user = new User((Dict) objs[i]);
                 users.put(user.getName(), user);
             }
         }
         if (users.size() <= 0) {
             LOG.info("creating default 'admin' user");
             user = new User("admin");
             user.setDescription("Default administrator user");
             user.setEnabled(true);
            user.setRoles(new String[] { "admin" });
             storeUser(user);
             users.put("admin", user);
         }
         // TODO: create default system user?
     }
 
     /**
      * Returns the currently authenticated user for this thread.
      *
      * @return the currently authenticated user, or
      *         null if no user is currently authenticated
      */
     public static User currentUser() {
         return (User) authUser.get();
     }
 
     /**
      * Checks if the currently authenticated user has access to an
      * object.
      *
      * @param obj            the object to check
      *
      * @return true if the current user has access, or
      *         false otherwise
      */
     public static boolean hasAccess(Object obj) {
         return hasAccess(obj, null);
     }
 
     /**
      * Checks if the currently authenticated user has access to an
      * object.
      *
      * @param obj            the object to check
      * @param caller         the caller procedure, or null for none
      *
      * @return true if the current user has access, or
      *         false otherwise
      */
     public static boolean hasAccess(Object obj, String caller) {
         if (obj instanceof Restricted) {
             return ((Restricted) obj).hasAccess();
         } else if (obj instanceof Procedure) {
             return hasAccess("procedure",
                              ((Procedure) obj).getName(),
                              caller);
         } else {
             return true;
         }
     }
 
     /**
      * Checks if the currently authenticated user has access to an
      * object.
      *
      * @param type           the object type
      * @param name           the object name
      *
      * @return true if the current user has access, or
      *         false otherwise
      */
     public static boolean hasAccess(String type, String name) {
         return hasAccess(type, name, null);
     }
 
     /**
      * Checks if the currently authenticated user has access to an
      * object.
      *
      * @param type           the object type
      * @param name           the object name
      * @param caller         the caller procedure, or null for none
      *
      * @return true if the current user has access, or
      *         false otherwise
      */
     public static boolean hasAccess(String type, String name, String caller) {
         String[]  list;
         Role      role;
 
         if (hasAdmin()) {
             return true;
         } else if (currentUser() != null) {
             list = currentUser().getRoles();
             for (int i = 0; i < list.length; i++) {
                 role = Role.find(dataStorage, list[i], true);
                 if (role != null && role.hasAccess(type, name, caller)) {
                     return true;
                 }
             }
             return false;
         } else {
             // TODO: support anonymous access
             return false;
         }
     }
 
     /**
      * Checks if the currently authenticated user has admin access.
      *
      * @return true if the current user has admin access, or
      *         false otherwise
      */
     public static boolean hasAdmin() {
         User  user = currentUser();
 
         return user != null && user.hasRole("Admin");
     }
 
     /**
      * Creates a unique number to be used once for hashing.
      *
      * @return the unique hash number
      */
     public static String nonce() {
         return String.valueOf(System.currentTimeMillis());
     }
 
     /**
      * Verifies that the specified nonce is sufficiently recently
      * generated to be acceptable.
      *
      * @param nonce          the nonce to check
      *
      * @throws SecurityException if the nonce was invalid
      */
     public static void verifyNonce(String nonce) throws SecurityException {
         try {
             long since = System.currentTimeMillis() - Long.parseLong(nonce);
             if (since > DateUtils.MILLIS_PER_MINUTE * 240) {
                 LOG.info("stale authentication one-off number");
                 throw new SecurityException("stale authentication one-off number");
             }
         } catch (NumberFormatException e) {
             LOG.info("invalid authentication one-off number");
             throw new SecurityException("invalid authentication one-off number");
         }
     }
 
     /**
      * Authenticates the specified user. This method will verify
      * that the user exists and is enabled. It should only be called
      * if a previous user authentication can be trusted, either via
      * a cookie, command-line login or similar. After a successful
      * authentication the current user will be set to the specified
      * user.
      *
      * @param name           the user name
      *
      * @throws SecurityException if the user failed authentication
      */
     public static void auth(String name) throws SecurityException {
         User    user = (User) users.get(name);
         String  msg;
 
         if (user == null) {
             msg = "user " + name + " does not exist";
             LOG.info("failed authentication: " + msg);
             throw new SecurityException(msg);
         } else if (!user.isEnabled()) {
             msg = "user " + name + " is disabled";
             LOG.info("failed authentication: " + msg);
             throw new SecurityException(msg);
         }
         authUser.set(user);
     }
 
     /**
      * Authenticates the specified used with an MD5 two-step hash.
      * This method will verify that the user exists, is enabled and
      * that the password hash plus the specified suffix will MD5 hash
      * to the specified string, After a successful authentication the
      * current user will be set to the specified user.
      *
      * @param name           the user name
      * @param suffix         the user password hash suffix to append
      * @param hash           the expected hashed result
      *
      * @throws Exception if the user failed authentication
      */
     public static void authHash(String name, String suffix, String hash)
     throws Exception {
 
         User    user = (User) users.get(name);
         String  test;
         String  msg;
 
         if (user == null) {
             msg = "user " + name + " does not exist";
             LOG.info("failed authentication: " + msg);
             throw new SecurityException(msg);
         } else if (!user.isEnabled()) {
             msg = "user " + name + " is disabled";
             LOG.info("failed authentication: " + msg);
             throw new SecurityException(msg);
         }
         test = BinaryUtil.hashMD5(user.getPasswordHash() + suffix);
         if (user.getPasswordHash().length() > 0 && !test.equals(hash)) {
             msg = "invalid password for user " + name;
             LOG.info("failed authentication: " + msg +
                      ", expected: " + test + ", received: " + hash);
             throw new SecurityException(msg);
         }
         authUser.set(user);
     }
 
     /**
      * Removes any previous authentication. I.e. the current user
      * will be reset to the anonymous user.
      */
     public static void authClear() {
         authUser.set(null);
     }
 
     /**
      * Returns all user names.
      *
      * @return all user names
      */
     public static String[] getUserNames() {
         return ArrayUtil.stringKeys(users);
     }
 
     /**
      * Returns the user for the specified user name.
      *
      * @param name           the user name
      *
      * @return the user object, or
      *         null if not found
      */
     public static User getUser(String name) {
         return (User) users.get(name);
     }
 
     /**
      * Saves a user to the application data store.
      *
      * @param user           the user to save
      *
      * @throws StorageException if the data couldn't be written
      * @throws SecurityException if the current user doesn't have
      *             admin access
      */
     public static void saveUser(User user)
         throws StorageException, SecurityException {
 
         String  name = user.getName();
 
         try {
             if (!hasAdmin()) {
                 LOG.info("failed to modify user " + name +
                          ": user " + SecurityContext.currentUser() +
                          " lacks admin privileges");
                 throw new SecurityException("Permission denied");
             }
             storeUser(user);
         } finally {
             User newUser = loadUser(name);
             if (newUser == null) {
                 user.setEnabled(false);
                 users.remove(name);
             } else {
                 users.put(name, newUser);
             }
         }
     }
 
     /**
      * Updates the current user password in the application data store.
      *
      * @param password       the new user password
      *
      * @throws StorageException if the data couldn't be written
      * @throws SecurityException if the current user isn't logged in
      */
     public static void updatePassword(String password)
         throws StorageException, SecurityException {
 
         User  user = SecurityContext.currentUser();
 
         if (user == null) {
             LOG.info("failed to modify user password: user not logged in");
             throw new SecurityException("Permission denied");
         }
         user.setPassword(password);
         storeUser(user);
     }
 
     /**
      * Loads a user object.
      *
      * @param name           the user name
      *
      * @return the user object, or
      *         null if not found
      *
      * @throws StorageException if the data couldn't be read
      */
     private static User loadUser(String name) throws StorageException {
         Path path = USER_PATH.child(name, false);
         Dict dict = (Dict) dataStorage.load(path);
         return (dict == null) ? null : new User(dict);
     }
 
     /**
      * Stores a user object.
      *
      * @param user           the user to store
      *
      * @throws StorageException if the data couldn't be written
      */
     private static void storeUser(User user) throws StorageException {
         Path path = USER_PATH.child(user.getName(), false);
         dataStorage.store(path, user.getData());
     }
 }
