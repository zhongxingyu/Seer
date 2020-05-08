 package com.alta189.cyborg.perms;
 
 import com.alta189.simplesave.Configuration;
 import com.alta189.simplesave.Database;
 import com.alta189.simplesave.DatabaseFactory;
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.alta189.simplesave.exceptions.TableRegistrationException;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import org.pircbotx.User;
 
 public class PermissionManager {
 	private static final Map<String, CyborgUser> users = new HashMap<String, CyborgUser>();
 	private static final Map<String, CyborgGroup> groups = new HashMap<String, CyborgGroup>();
 	private static Database db;
 	private static Configuration dbConfig;
 	private static SaveThread saveThread;
 
 	protected static boolean init() {
 		db = DatabaseFactory.createNewDatabase(dbConfig);
 		try {
 			db.registerTable(CyborgUser.class);
 			db.registerTable(CyborgGroup.class);
 			db.connect();
 		} catch (ConnectionException e) {
 			e.printStackTrace();
 			return false;
 		} catch (TableRegistrationException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		for (CyborgUser user : db.select(CyborgUser.class).execute().find()) {
 			user.load();
 			users.put(user.getName().toLowerCase(), user);
 		}
 
 		for (CyborgGroup group : db.select(CyborgGroup.class).execute().find()) {
 			group.load();
 			groups.put(group.getName().toLowerCase(), group);
 		}
 
 		saveThread = new SaveThread();
 		saveThread.start();
 
 		return true;
 	}
 
 	protected static boolean close() {
		saveThread.interrupt();
 		if (db.isConnected()) {
 			for (CyborgUser user : users.values()) {
 				user.flush();
 				db.save(CyborgUser.class, user);
 			}
 
 			for (CyborgGroup group : groups.values()) {
 				group.flush();
 				db.save(CyborgGroup.class, group);
 			}
 
 			try {
 				db.close();
 			} catch (ConnectionException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	protected static void setDbConfig(Configuration dbConfig) {
 		PermissionManager.dbConfig = dbConfig;
 	}
 
 	public static CyborgGroup getGroup(String group) {
 		return groups.get(group.toLowerCase());
 	}
 
 	public static CyborgUser getUser(String user) {
 		return users.get(user.toLowerCase());
 	}
 
 	public static CyborgUser getUserFromHostname(String hostname) {
 		for (CyborgUser user : users.values()) {
 			if (user.getHostnames().contains(hostname)) {
 				return user;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if someone with the given hostname has the given perm
 	 * @param hostname
 	 * @param perm
 	 * @return Whether they have the perm.
 	 */
 	public static boolean hasPerm(String hostname, String perm) {
 		return hasPerm(hostname, perm, false);
 	}
 
 	public static boolean hasPerm(String hostname, String perm, boolean ignoreWildcard) {
 		CyborgUser user = getUserFromHostname(hostname);
 		return user != null && user.hasPerm(perm, ignoreWildcard);
 	}
 
 	public static boolean hasPerm(User user, String perm) {
 		return hasPerm(user, perm, false);
 	}
 
 	public static boolean hasPerm(User user, String perm, boolean ignoreWildcard) {
 		return hasPerm(user.getLogin() + "@" + user.getHostmask(), perm, ignoreWildcard);
 	}
 
 	public static void registerUser(String name, String hostname, String password) {
 		if (getUser(name) != null) {
 			return;
 		}
 		CyborgUser user = new CyborgUser();
 		user.setName(name);
 		user.addHostname(hostname);
 		user.setPassword(password);
 		users.put(name.toLowerCase(), user);
 	}
 
 	public static void registerUser(String name, String login, String hostname, String password) {
 		registerUser(name, login + "@" + hostname, password);
 	}
 
 	public static void addGroup(CyborgGroup group) {
 		if (groups.get(group.getName().toLowerCase()) != null) {
 			return;
 		}
 		groups.put(group.getName().toLowerCase(), group);
 	}
 
 	public static Collection<CyborgUser> getUsers() {
 		return Collections.unmodifiableCollection(users.values());
 	}
 
 	public static Collection<CyborgGroup> getGroups() {
 		return Collections.unmodifiableCollection(groups.values());
 	}
 
 	protected static Database getDatabase() {
 		return db;
 	}
 
 }
