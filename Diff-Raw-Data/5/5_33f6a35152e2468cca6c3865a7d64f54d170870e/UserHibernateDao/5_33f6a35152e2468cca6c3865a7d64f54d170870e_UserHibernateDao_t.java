 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.model.dao.hibernate;
 
 import org.hibernate.Query;
 import org.hibernate.type.StandardBasicTypes;
 import org.jtalks.common.model.dao.hibernate.AbstractHibernateParentRepository;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.dao.UserDao;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.pages.Pagination;
 
 import java.math.BigInteger;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Hibernate implementation of UserDao.
  *
  * @author Vyacheslav Zhivaev
  * @author Alexey Grigorev
  */
 public class UserHibernateDao extends AbstractHibernateParentRepository<PoulpeUser> implements UserDao {
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<PoulpeUser> findPoulpeUsersPaginated(String searchString, Pagination paginate) {
         Query query = getSession().getNamedQuery("findUsersByLikeUsername");
         query.setString("username", MessageFormat.format("%{0}%", prepareString4Sql(searchString)));
         paginate.addPagination(query);
 
         @SuppressWarnings("unchecked")
         List<PoulpeUser> result = query.list();
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int countUsernameMatches(String searchString) {
         Query query = getSession().getNamedQuery("countUsersByLikeUsername");
         query.setString("username", MessageFormat.format("%{0}%", prepareString4Sql(searchString)));
 
         Number result = (Number) query.uniqueResult();
         return result.intValue();
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public PoulpeUser getByUsername(String username) {
         Query query = getSession().getNamedQuery("findUsersByUsername");
         query.setString("username", username);
 
         return (PoulpeUser) query.uniqueResult();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<PoulpeUser> getUsersInGroups(List<Group> groups) {
         Query query = getSession().getNamedQuery("findBannedUsers");
 
         ArrayList groupsIds = new ArrayList();
         for (Group group : groups) {
             groupsIds.add(new BigInteger(group.getId() + ""));
         }
         query.setParameterList("bannedGroups", groupsIds, StandardBasicTypes.BIG_INTEGER);
 
         @SuppressWarnings("unchecked")
         List<PoulpeUser> result = query.list();
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<PoulpeUser> findUsersNotInGroups(String availableFilterText, List<Group> groups, Pagination paginate) {
         Query query = getSession().getNamedQuery("findUnbannedUsersByLikeUsername");
         query.setString("username", MessageFormat.format("%{0}%", availableFilterText));
 
         ArrayList groupsIds = new ArrayList();
         for (Group group : groups) {
             groupsIds.add(new BigInteger(group.getId() + ""));
         }
 
         query.setParameterList("bannedGroups", groupsIds, StandardBasicTypes.BIG_INTEGER);
         paginate.addPagination(query);
 
         @SuppressWarnings("unchecked")
         List<PoulpeUser> result = query.list();
         return result;
     }
 	
 	//Escape character % for LIKE in SQL query
 	private String prepareString4Sql(String s){
 		return s.replace("%", "\\%");
 	}
 
 }
