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
 package org.jtalks.common.model.dao;
 
 import org.jtalks.common.model.entity.User;
 
 import java.util.List;
 
 /**
  * Data access object for manipulating groups
  *
  * @author Konstantin Akimov
  */
public interface GroupDao<Group> extends GenericDao<Group> {
 
     /**
      * Get the list of all groups.
      *
      * @return list of groups
      */
     List<Group> getAll();
 
     /**
      * Get the list of all groups which match to the specified name
      *
      * @param name specified name
      * @return list of groups
      */
     List<Group> getMatchedByName(String name);
 
     /**
      * Get the list of all groups, where user is present
      *
      * @param user specified name of user
      * @return list of groups
      */
     List<Group> getGroupsOfUser(User user);
 
     /**
      * Get user group with specified name.
      *
      * @param name group name
      * @return user group
      */
     Group getGroupByName(String name);
 }
