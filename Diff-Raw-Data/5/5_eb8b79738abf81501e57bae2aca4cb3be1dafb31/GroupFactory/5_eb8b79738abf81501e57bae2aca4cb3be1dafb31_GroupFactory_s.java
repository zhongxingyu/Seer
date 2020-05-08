 package com.testapp.server.jdo;
 
 import java.util.Collection;
 
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.apache.commons.lang.NotImplementedException;
 
 import com.testapp.client.pos.UserGroup;
 
 public class GroupFactory extends PersistentObjectFactory<UserGroup> {
 
 	private static GroupFactory instance = new GroupFactory();
 	
 	public static GroupFactory getInstance () {
 		return instance;
 	}
 	
 	@Override
 	protected Class<UserGroup> getObjectClass() {
 		return UserGroup.class;
 	}
 
 	public GroupFactory() {}
 	
 	/**
 	 * Adds the users with the given ids into the group.
 	 * 
 	 * If a user is already a member of the group, it will be ignored.
 	 * 
 	 * It is the responsibility of the caller to ensure the id's of the users
 	 * are valid id's for users in the database
 	 * 
 	 * @param groupId
 	 * @param users
 	 */
 	public void addUsersToGroup(long groupId, Collection<Long> users) {
 		UserGroup group = get(groupId);
 
 		group.addMembers(users);
 		
 		save(group);
 	}
 	
 	/**
 	 * Return whether the user is a member of the group or not
 	 * 
 	 * @param groupId
 	 * @param userId
 	 * @return true - the user is a member of the group, false - the user is not a member
 	 */
 	public boolean isMemberOf(long groupId, long userId) {
 		UserGroup group = get(groupId);
 		return group==null ? false : group.isMemberOf(userId);
 	}
 	
 	/**
 	 * Return a collection of user ids who are members of the group.
 	 * 
 	 * @param groupId
 	 * @return
 	 */
 	public Collection<Long> getGroupMembers(long groupId) {
 		UserGroup group = get(groupId);
 		return group==null ? null : group.getMembers();
 	}
 }
