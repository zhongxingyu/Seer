 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.picketlink.idm.impl.api.session.managers;
 
 import org.picketlink.idm.api.RelationshipManager;
 import org.picketlink.idm.api.Group;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.api.IdentityType;
 import org.picketlink.idm.api.RelationshipManagerFeaturesDescription;
 import org.picketlink.idm.api.IdentitySearchCriteria;
 import org.picketlink.idm.api.IdentitySearchCriteriumType;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.spi.model.IdentityObjectRelationshipType;
 import org.picketlink.idm.spi.model.IdentityObjectRelationship;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.model.IdentityObjectType;
 import org.picketlink.idm.spi.store.IdentityObjectSearchCriteriaType;
 import org.picketlink.idm.impl.api.session.managers.AbstractManager;
 import org.picketlink.idm.impl.api.session.IdentitySessionImpl;
 import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;
 import org.picketlink.idm.impl.api.model.SimpleGroup;
 import org.picketlink.idm.impl.api.model.GroupKey;
 import org.picketlink.idm.impl.api.model.SimpleUser;
 import org.picketlink.idm.impl.cache.RelationshipSearchImpl;
 import org.picketlink.idm.impl.cache.GroupSearchImpl;
 import org.picketlink.idm.impl.cache.UserSearchImpl;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Arrays;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class RelationshipManagerImpl extends AbstractManager implements RelationshipManager, Serializable
 {
 
    private static Logger log = Logger.getLogger(RelationshipManagerImpl.class.getName());
 
 
    RelationshipManagerFeaturesDescription featuresDescription;
 
    public static final IdentityObjectRelationshipType MEMBER = new IdentityObjectRelationshipType()
    {
       public String getName()
       {
          return "JBOSS_IDENTITY_MEMBERSHIP";
       }
    };
    
    private static final long serialVersionUID = -1054805796187123311L;
 
    public RelationshipManagerImpl(IdentitySessionImpl session)
    {
       super(session);
 
       featuresDescription = new RelationshipManagerFeaturesDescription()
       {
          public boolean isIdentityAssociationSupported(String fromGroupType)
          {
 
             IdentityObjectType identityOT = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType();
             IdentityObjectType groupIdentityOT = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(fromGroupType);
 
             try
             {
                return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                   isRelationshipTypeSupported(groupIdentityOT, identityOT, MEMBER);
             }
             catch (IdentityException e)
             {
                return false;
             }
 
          }
 
          public boolean isGroupAssociationSupported(String fromGroupType, String toGroupType)
          {
             IdentityObjectType toGroupOT = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(toGroupType);
             IdentityObjectType fromGroupOT = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(fromGroupType);
 
             try
             {
                return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                   isRelationshipTypeSupported(fromGroupOT, toGroupOT, MEMBER);
             }
             catch (IdentityException e)
             {
                return false;
             }
          }
 
          public boolean isIdentitiesSearchCriteriumTypeSupported(IdentitySearchCriteriumType constraintType)
          {
             IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType();
 
             IdentityObjectSearchCriteriaType constraint = IdentityObjectSearchCriteriaType.valueOf(constraintType.name());
 
 
             if (constraint != null)
             {
                return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                   isSearchCriteriaTypeSupported(objectType, constraint);
             }
             
             return false;
          }
 
          public boolean isGroupsSearchCriteriumTypeSupported(String groupType, IdentitySearchCriteriumType constraintType)
          {
             IdentityObjectType objectType = getSessionContext().getIdentityObjectTypeMapper().getIdentityObjectType(groupType);
 
             IdentityObjectSearchCriteriaType constraint = IdentityObjectSearchCriteriaType.valueOf(constraintType.name());
 
             if (constraint != null)
             {
                return getSessionContext().getIdentityStoreRepository().getSupportedFeatures().
                   isSearchCriteriaTypeSupported(objectType, constraint);
             }
 
             return false;
          }
       };
    }
 
    public RelationshipManagerFeaturesDescription getFeaturesDescription()
    {
       return featuresDescription;
    }
 
    public void associateGroups(Collection<Group> parents, Collection<Group> members) throws IdentityException
    {
       checkNotNullArgument(parents, "parents");
       checkNotNullArgument(members, "members");
 
       for (Iterator<Group> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
       {
          Group parent = parentsIterator.next();
 
          for (Iterator<Group> membersIterator = members.iterator(); membersIterator.hasNext();)
          {
             Group member = membersIterator.next();
 
             associateGroups(parent, member);
          }
       }
 
    }
 
    public void associateGroups(Group parent, Collection<Group> members) throws IdentityException
    {
       checkNotNullArgument(parent, "parent");
       checkNotNullArgument(members, "members");
 
       associateGroups(Arrays.asList(parent), members);
    }
 
    public void associateGroupsByKeys(Collection<String> parentIds, Collection<String> memberIds) throws IdentityException
    {
       checkNotNullArgument(parentIds, "Parents Ids");
       checkNotNullArgument(memberIds, "Members Ids");
 
       for (Iterator<String> parentsIterator = parentIds.iterator(); parentsIterator.hasNext();)
       {
          String parentId = parentsIterator.next();
 
          for (Iterator<String> membersIterator = memberIds.iterator(); membersIterator.hasNext();)
          {
             String memberId = membersIterator.next();
 
             associateGroupsByKeys(parentId, memberId);
          }
       }
    }
 
    public void associateGroupsByKeys(String parentId, Collection<String> memberIds) throws IdentityException
    {
       checkNotNullArgument(parentId, "Parent Id");
 
       associateGroupsByKeys(Arrays.asList(parentId), memberIds);
    }
 
    public void associateGroups(Group parent, Group member) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parent, "Parent group");
          checkNotNullArgument(member, "Member group");
 
          preGroupAssociationCreate(parent, member);
 
          getRepository().createRelationship(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER, null, true);
 
          if (cache != null)
          {
             cache.invalidateAllQueries(cacheNS);
             cache.invalidateAllSearches(cacheNS);
          }
 
          postGroupAssociationCreate(parent, member);
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
                            
 
    public void associateGroupsByKeys(String parentId, String memberId) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parentId, "Parent Id");
          checkNotNullArgument(memberId, "Member Id");
 
          Group parent = new SimpleGroup(new GroupKey(parentId));
          Group member = new SimpleGroup(new GroupKey(memberId));
 
 
          preGroupAssociationCreate(parent, member);
 
          getRepository().createRelationship(getInvocationContext(), createIdentityObjectForGroupId(parentId), createIdentityObjectForGroupId(memberId), MEMBER, null, true);
 
          if (cache != null)
          {
             cache.invalidateAllQueries(cacheNS);
             cache.invalidateAllSearches(cacheNS);
          }
 
 
          postGroupAssociationCreate(parent, member);
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
 
    public void associateUsers(Collection<Group> parents, Collection<User> members) throws IdentityException
    {
       checkNotNullArgument(parents, "parents");
       checkNotNullArgument(members, "members");
 
       for (Iterator<Group> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
       {
          Group parent = parentsIterator.next();
 
          for (Iterator<User> membersIterator = members.iterator(); membersIterator.hasNext();)
          {
             User member = membersIterator.next();
 
             associateUser(parent, member);
          }
       }
    }
 
    public void associateUser(Group parent, Collection<User> members) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent group");
 
       associateUsers(Arrays.asList(parent), members);
 
    }
 
    public void associateUsersByKeys(Collection<String> parents, Collection<String> members) throws IdentityException
    {
 
       checkNotNullArgument(parents, "parents");
       checkNotNullArgument(members, "members");
 
       for (Iterator<String> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
       {
          String parent = parentsIterator.next();
 
          for (Iterator<String> membersIterator = members.iterator(); membersIterator.hasNext();)
          {
             String member = membersIterator.next();
 
             associateUserByKeys(parent, member);
          }
       }
 
    }
 
    public void associateUsersByKeys(String parentId, Collection<String> members) throws IdentityException
    {
       checkNotNullArgument(parentId, "Parent Id");
 
       associateUsersByKeys(Arrays.asList(parentId), members);
 
    }
 
    public void associateUser(Group parent, User member) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parent, "Parent group");
          checkNotNullArgument(member, "Member user");
 
          preUserAssociationCreate(parent, member);
 
          getRepository().createRelationship(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER, null, true);
 
          if (cache != null)
          {
             cache.invalidateAllQueries(cacheNS);
             cache.invalidateAllSearches(cacheNS);
          }
 
 
          postUserAssociationCreate(parent, member);
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
 
 
    public void associateUserByKeys(String parentId, String memberId) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parentId, "Parent group Id");
          checkNotNullArgument(memberId, "Member user Id");
 
          Group parent = new SimpleGroup(new GroupKey(parentId));
          User member = new SimpleUser(memberId);
 
          preUserAssociationCreate(parent, member);
 
          getRepository().createRelationship(getInvocationContext(), createIdentityObjectForGroupId(parentId), createIdentityObjectForUserName(memberId), MEMBER, null, true);
 
          if (cache != null)
          {
             cache.invalidateAllQueries(cacheNS);
             cache.invalidateAllSearches(cacheNS);
          }
 
 
          postUserAssociationCreate(parent, member);
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
 
    public void disassociateGroups(User user) throws IdentityException
    {
       checkNotNullArgument(user, "User");
 
       Collection<Group> groups = findAssociatedGroups(user, (String)null);
       Set<User> users = new HashSet<User>();
       users.add(user);
       
       disassociateUsers(groups, users);
 
    }
 
    public void disassociateGroups(String userId) throws IdentityException
    {
       checkNotNullArgument(userId, "User Id");
 
       disassociateGroups(createUserFromId(userId));
    }
 
    public void disassociateGroups(Collection<Group> parents, Collection<Group> members) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
 
          for (Iterator<Group> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             Group parent = parentsIterator.next();
 
             for (Iterator<Group> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                Group member = membersIterator.next();
 
                preGroupAssociationRemove(parent, member);
 
                getRepository().removeRelationship(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER, null);
 
                if (cache != null)
                {
                   cache.invalidateAllQueries(cacheNS);
                   cache.invalidateAllSearches(cacheNS);
                }
 
                postGroupAssociationRemove(parent, member);
 
             }
          }
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
 
    }
 
    public void disassociateGroups(Group parent, Collection<Group> members) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent");
 
       disassociateGroups(Arrays.asList(parent), members);
    }
 
    public void disassociateGroupsByKeys(Collection<String> parents, Collection<String> members) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
          for (Iterator<String> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             String parent = parentsIterator.next();
 
             for (Iterator<String> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                String member = membersIterator.next();
 
                Group parentGroup = new SimpleGroup(new GroupKey(parent));
                Group memberGroup = new SimpleGroup(new GroupKey(member));
 
                preGroupAssociationRemove(parentGroup, memberGroup);
 
                getRepository().removeRelationship(getInvocationContext(), createIdentityObjectForGroupId(parent), createIdentityObjectForGroupId(member), MEMBER, null);
 
                if (cache != null)
                {
                   cache.invalidateAllQueries(cacheNS);
                   cache.invalidateAllSearches(cacheNS);
                }
 
 
                postGroupAssociationRemove(parentGroup, memberGroup);
 
             }
          }
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public void disassociateGroupsByKeys(String parent, Collection<String> members) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent Id");
 
       disassociateGroupsByKeys(Arrays.asList(parent), members);
    }
 
    public void disassociateUsers(Collection<Group> parents, Collection<User> members) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
          for (Iterator<Group> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             Group parent = parentsIterator.next();
 
             for (Iterator<User> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                User member = membersIterator.next();
 
                preUserAssociationRemove(parent, member);
 
                getRepository().removeRelationship(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER, null);
 
                if (cache != null)
                {
                   cache.invalidateAllQueries(cacheNS);
                   cache.invalidateAllSearches(cacheNS);
                }
 
 
                postUserAssociationRemove(parent, member);
             }
          }
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
 
    public void disassociateUsers(Group parent, Collection<User> members) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent group");
 
       disassociateUsers(Arrays.asList(parent), members);
    }
 
    public void disassociateUsersByKeys(Collection<String> parents, Collection<String> members) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
 
          for (Iterator<String> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             String parent = parentsIterator.next();
 
             for (Iterator<String> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                String member = membersIterator.next();
 
                Group parentGroup = new SimpleGroup(new GroupKey(parent));
                User memberUser = new SimpleUser(member);
 
                preUserAssociationRemove(parentGroup, memberUser);
 
                getRepository().removeRelationship(getInvocationContext(), createIdentityObjectForGroupId(parent), createIdentityObjectForUserName(member), MEMBER, null);
 
                if (cache != null)
                {
                   cache.invalidateAllQueries(cacheNS);
                   cache.invalidateAllSearches(cacheNS);
                }
 
 
                postUserAssociationRemove(parentGroup, memberUser);
 
             }
          }
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public void disassociateUsersByKeys(String parent, Collection<String> members) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent Id");
 
       disassociateUsersByKeys(Arrays.asList(parent), members);
 
    }
 
    public <G extends IdentityType, I extends IdentityType> boolean isAssociated(Collection<G> parents, Collection<I> members) throws IdentityException
    {
       try
       {
 //TODO: maybe IdentityStore should have isRelationshipPresent method to improve this?
 
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             for (G parent : parents)
             {
                search.addParent(parent);
             }
             for (I member : members)
             {
                search.addMember(member);
             }
 
             Boolean result = cache.getRelationshipSearch(cacheNS, search);
 
             if (result != null)
             {
                return result;
             }
          }
 
 
          for (Iterator<G> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             IdentityType parent = parentsIterator.next();
 
             for (Iterator<I> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                IdentityType member = membersIterator.next();
 
                Collection<IdentityObjectRelationship> relationships = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER);
 
                if (relationships.size() == 0)
                {
                   if (cache != null)
                   {
                      RelationshipSearchImpl search = new RelationshipSearchImpl();
                      for (G p : parents)
                      {
                         search.addParent(p);
                      }
                      for (I m : members)
                      {
                         search.addMember(m);
                      }
                      cache.putRelationshipSearch(cacheNS, search, false);
 
                   }
 
                   return false;
                }
             }
          }
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             for (G p : parents)
             {
                search.addParent(p);
             }
             for (I m : members)
             {
                search.addMember(m);
             }
             cache.putRelationshipSearch(cacheNS, search, true);
 
          }
 
          return true;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public boolean isAssociatedByKeys(Collection<String> parents, Collection<String> members) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parents, "parents");
          checkNotNullArgument(members, "members");
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             for (String parent : parents)
             {
                search.addParent(parent);
             }
             for (String member : members)
             {
                search.addMember(member);
             }
 
             Boolean result = cache.getRelationshipSearch(cacheNS, search);
 
             if (result != null)
             {
                return result;
             }
          }
 
          for (Iterator<String> parentsIterator = parents.iterator(); parentsIterator.hasNext();)
          {
             String parent = parentsIterator.next();
 
             for (Iterator<String> membersIterator = members.iterator(); membersIterator.hasNext();)
             {
                String member = membersIterator.next();
 
                Collection<IdentityObjectRelationship> relationships = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER);
 
                if (relationships.size() == 0)
                {
                   if (cache != null)
                   {
                      RelationshipSearchImpl search = new RelationshipSearchImpl();
                      for (String p : parents)
                      {
                         search.addParent(p);
                      }
                      for (String m : members)
                      {
                         search.addMember(m);
                      }
                      cache.putRelationshipSearch(cacheNS, search, false);
 
                   }
 
                   return false;
                }
             }
          }
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             for (String p : parents)
             {
                search.addParent(p);
             }
             for (String m : members)
             {
                search.addMember(m);
             }
             cache.putRelationshipSearch(cacheNS, search, true);
 
          }
 
          return true;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public <G extends IdentityType, I extends IdentityType> boolean isAssociated(G parent, I member) throws IdentityException
    {
       checkNotNullArgument(parent, "Parent IdentityType");
       checkNotNullArgument(member, "Member IdentityType");
 
 
       Set<G> parents = new HashSet<G>();
       parents.add(parent);
       Set<I> members = new HashSet<I>();
       members.add(member);
 
       return isAssociated(parents, members);
    }
 
    public boolean isAssociatedByKeys(String parent, String member) throws IdentityException
    {
       try
       {
          checkNotNullArgument(parent, "Parent Id");
          checkNotNullArgument(member, "Member Id");
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             search.addParent(parent);
             search.addMember(member);
 
             Boolean result = cache.getRelationshipSearch(cacheNS, search);
 
             if (result != null)
             {
                return result;
             }
          }
 
          Collection<IdentityObjectRelationship> relationships = getRepository().resolveRelationships(getInvocationContext(), createIdentityObject(parent), createIdentityObject(member), MEMBER);
 
          boolean result = true;
 
          if (relationships.size() == 0)
          {
             result = false;
          }
 
          if (cache != null)
          {
             RelationshipSearchImpl search = new RelationshipSearchImpl();
             search.addParent(parent);
             search.addMember(member);
             cache.putRelationshipSearch(cacheNS, search, result);
          }
 
          return result;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    private Collection<Group> findAssociatedGroupsCascaded(Collection<Group> previous, Group group, String groupType, boolean parent, IdentitySearchCriteria criteria) throws IdentityException
    {
       Collection<Group> results = findAssociatedGroups(group, groupType, parent, false, criteria);
 
       List<Group> newResults = new LinkedList<Group>();
 
 
       // For each result make recursive call unless it is already in previous results
       for (Group result : results)
       {
          if (!previous.contains(result))
          {
             newResults.add(result);
             previous.add(result);
             newResults.addAll(findAssociatedGroupsCascaded(previous, result, groupType, parent, criteria));
          }
       }
 
       return newResults;
 
    }
 
    public Collection<Group> findAssociatedGroups(Group group, String groupType, boolean parent, boolean cascade, IdentitySearchCriteria criteria) throws IdentityException
    {
 
       try
       {
          checkNotNullArgument(group, "Group");
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addAssociatedGroupId(group.getKey());
             search.setGroupType(groupType);
             search.setParent(parent);
             search.setCascade(cascade);
             search.setSearchCriteria(criteria);
 
             Collection<Group> results = cache.getGroupSearch(cacheNS, search);
             if (results != null)
             {
                return results;
             }
          }
 
          List<Group> identities = new LinkedList<Group>();
 
          IdentityObjectType iot = groupType != null ? getIdentityObjectType(groupType) : null;
 
 
          if (cascade)
          {
             Set<Group> prev = new HashSet<Group>();
             prev.add(group);
             identities = (List<Group>)findAssociatedGroupsCascaded(prev, group, groupType, parent, criteria);
 
             try
             {
                //TODO: don't perform when only one repository call was made
                if (criteria != null)
                {
                   IdentitySearchCriteriaImpl.applyCriteria(identitySession, convertSearchControls(criteria), identities);
                }
             }
             catch (Exception e)
             {
                throw new IdentityException("Failed to apply criteria", e);
             }
 
 
          }
          else
          {
 
             Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(group), MEMBER, parent, convertSearchControls(criteria));
 
             for (IdentityObject io : ios)
             {
                if ((iot == null && !io.getIdentityType().getName().equals(getUserObjectType().getName())) ||
                   (iot != null && io.getIdentityType().getName().equals(iot.getName())))
                {
                   identities.add(createGroup(io));
                }
             }
          }
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addAssociatedGroupId(group.getKey());
             search.setGroupType(groupType);
             search.setParent(parent);
             search.setCascade(cascade);
             search.setSearchCriteria(criteria);
 
             cache.putGroupSearch(cacheNS, search, identities);
 
          }
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
 
    }
 
    public Collection<Group> findAssociatedGroups(String groupId, String groupType, boolean parent, boolean cascade, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(groupId, "Group Id");
 //      checkNotNullArgument(groupType, "Group type");
 
       Group group = createGroupFromId(groupId);
 
       return findAssociatedGroups(group, groupType, parent, cascade, criteria);
    }
 
    public Collection<Group> findAssociatedGroups(Group group, String groupType, boolean parent, boolean cascade) throws IdentityException
    {
       checkNotNullArgument(group, "Group");
 //      checkNotNullArgument(groupType, "Group type");
 
       return findAssociatedGroups(group, groupType, parent, cascade, null);
    }
 
    public Collection<Group> findAssociatedGroups(User user, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       try
       {
          checkNotNullArgument(user, "User");
          //checkNotNullArgument(groupType, "Group type");
 
          if (cache != null)
         {
            GroupSearchImpl search = new GroupSearchImpl();
            search.addAssociatedUserId(user.getKey());
            search.setGroupType(groupType);
            search.setSearchCriteria(criteria);
 
            Collection<Group> results = cache.getGroupSearch(cacheNS, search);
            if (results != null)
            {
               return results;
            }
         }
 
          List<Group> identities = new LinkedList<Group>();
 
          IdentityObjectType iot = groupType != null ? getIdentityObjectType(groupType) : null;
 
          Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(user), MEMBER, false, convertSearchControls(criteria));
 
          for (IdentityObject io : ios)
          {
             if (iot == null || io.getIdentityType().getName().equals(iot.getName()))
             {
                identities.add(createGroup(io));
             }
          }
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addAssociatedUserId(user.getKey());
             search.setGroupType(groupType);
             search.setSearchCriteria(criteria);
 
             cache.putGroupSearch(cacheNS, search, identities);
 
          }
 
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public Collection<Group> findAssociatedGroups(String userName, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
       //checkNotNullArgument(groupType, "Group type");
 
       User user = createUserFromId(userName);
 
       return findAssociatedGroups(user, groupType, criteria);
    }
 
    public Collection<Group> findAssociatedGroups(User user, String groupType) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       //checkNotNullArgument(groupType, "Group type");
 
       return findAssociatedGroups(user, groupType, null);
    }
 
    public Collection<Group> findAssociatedGroups(User user, IdentitySearchCriteria criteria) throws IdentityException
    {
       try
       {
          checkNotNullArgument(user, "User");
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addAssociatedUserId(user.getKey());
             search.setSearchCriteria(criteria);
 
             Collection<Group> results = cache.getGroupSearch(cacheNS, search);
             if (results != null)
             {
                return results;
             }
          }
 
          List<Group> identities = new LinkedList<Group>();
 
          Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(user), MEMBER, false, convertSearchControls(criteria));
 
 
          String userTypeName = getUserObjectType().getName();
 
          for (IdentityObject io : ios)
          {
 
             // Filter out users
             if (!io.getIdentityType().getName().equals(userTypeName))
                identities.add(createGroup(io));
          }
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addAssociatedUserId(user.getKey());
             search.setSearchCriteria(criteria);
 
             cache.putGroupSearch(cacheNS, search, identities);
          }
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public Collection<Group> findAssociatedGroups(String userName, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
 
       User user = createUserFromId(userName);
 
       return findAssociatedGroups(user, criteria);
    }
 
    public Collection<Group> findAssociatedGroups(User user) throws IdentityException
    {
       checkNotNullArgument(user, "User");
 
       return findAssociatedGroups(user, (IdentitySearchCriteria)null);
    }
 
    public Collection<User> findAssociatedUsers(Group group, boolean cascade, IdentitySearchCriteria criteria) throws IdentityException
    {
       try
       {
          checkNotNullArgument(group, "Group");
 
          if (cache != null)
          {
             UserSearchImpl search = new UserSearchImpl();
             search.addAssociatedGroupId(group.getKey());
             search.setCascade(cascade);
             search.setSearchCriteria(criteria);
 
             Collection<User> results = cache.getUserSearch(cacheNS, search);
             if (results != null)
             {
                return results;
             }
          }
 
          List<User> identities = new LinkedList<User>();
 
          if (cascade)
          {
             // Do non cascaded call
 
             identities.addAll(findAssociatedUsers(group, false, criteria));
 
             // Find all associated groups (cascaded)
             Collection<Group> groups = findAssociatedGroups(group, null, true, true, criteria);
 
 
             for (Group asociatedGroup : groups)
             {
                identities.addAll(findAssociatedUsers(asociatedGroup, false, criteria));
             }
 
             try
             {
 
                //TODO: don't perform when only one repository call was made
                if (criteria != null)
                {
                   IdentitySearchCriteriaImpl.applyCriteria(identitySession, convertSearchControls(criteria), identities);
                }
             }
             catch (Exception e)
             {
                throw new IdentityException("Failed to apply criteria", e);
             }
 
          }
          else
          {
 
             Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(group), MEMBER, true, convertSearchControls(criteria));
 
             String userTypeName = getUserObjectType().getName();
 
             for (IdentityObject io : ios)
             {
                //Filter out groups
                if (io.getIdentityType().getName().equals(userTypeName))
                {
                   identities.add(createUser(io));
                }
             }
          }
 
          if (cache != null)
          {
             UserSearchImpl search = new UserSearchImpl();
             search.addAssociatedGroupId(group.getKey());
             search.setCascade(cascade);
             search.setSearchCriteria(criteria);
 
             cache.putUserSearch(cacheNS, search, identities);
          }
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public Collection<User> findAssociatedUsers(String groupId, boolean cascade, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(groupId, "Group Id");
 
       Group group = createGroupFromId(groupId);
 
       return findAssociatedUsers(group, cascade, criteria);
    }
 
    public Collection<User> findAssociatedUsers(Group group, boolean cascade) throws IdentityException
    {
       checkNotNullArgument(group, "Group");
 
       return findAssociatedUsers(group, cascade, null);
    }
 
    public Collection<Group> findRelatedGroups(User user, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       try
       {
          checkNotNullArgument(user, "User");
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addRelatedUserId(user.getKey());
             search.setGroupType(groupType);
             search.setSearchCriteria(criteria);
 
             Collection<Group> results = cache.getGroupSearch(cacheNS, search);
             if (results != null)
             {
                return results;
             }
          }
 
          List<Group> identities = new LinkedList<Group>();
 
          Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(user), null, false, convertSearchControls(criteria));
 
 
          String userTypeName = getUserObjectType().getName();
 
          for (IdentityObject io : ios)
          {
             // Filter out users
             if (!io.getIdentityType().getName().equals(userTypeName))
                identities.add(createGroup(io));
          }
 
          if (cache != null)
          {
             GroupSearchImpl search = new GroupSearchImpl();
             search.addRelatedUserId(user.getKey());
             search.setGroupType(groupType);
             search.setSearchCriteria(criteria);
 
            Collection<Group> results = cache.getGroupSearch(cacheNS, search);
            if (results != null)
            {
               return results;
            }
          }
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public Collection<Group> findRelatedGroups(String userName, String groupType, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(userName, "User name");
 
       User user = createUserFromId(userName);
 
       return findRelatedGroups(user, groupType, criteria);
    }
 
    public Collection<User> findRelatedUsers(Group group, IdentitySearchCriteria criteria) throws IdentityException
    {
       try
       {
          checkNotNullArgument(group, "Group");
 
          if (cache != null)
          {
             UserSearchImpl search = new UserSearchImpl();
             search.addRelatedGroupId(group.getKey());
             search.setSearchCriteria(criteria);
 
             Collection<User> results = cache.getUserSearch(cacheNS, search);
             if (results != null)
             {
                return results;
             }
          }
 
          List<User> identities = new LinkedList<User>();
 
          Collection<IdentityObject> ios = getRepository().findIdentityObject(getInvocationContext(), createIdentityObject(group), null, true, convertSearchControls(criteria));
 
          String userTypeName = getUserObjectType().getName();
 
          for (IdentityObject io : ios)
          {
             if (io.getIdentityType().getName().equals(userTypeName))
             {
                User user = createUser(io);
 
                if (!identities.contains(user))
                {
                   identities.add(createUser(io));
                }
             }
          }
 
          if (cache != null)
          {
             UserSearchImpl search = new UserSearchImpl();
             search.addRelatedGroupId(group.getKey());
             search.setSearchCriteria(criteria);
 
             cache.putUserSearch(cacheNS, search, identities);
 
          }
 
          return identities;
       }
       catch (IdentityException e)
       {
          if (log.isLoggable(Level.FINER))
          {
             log.log(Level.FINER, "Exception occurred: ", e);
          }
          throw e;
       }
    }
 
    public Collection<User> findRelatedUsers(String groupId, IdentitySearchCriteria criteria) throws IdentityException
    {
       checkNotNullArgument(groupId, "Group Id");
 
       Group group = createGroupFromId(groupId);
 
       return findRelatedUsers(group, criteria);
    }
 }
