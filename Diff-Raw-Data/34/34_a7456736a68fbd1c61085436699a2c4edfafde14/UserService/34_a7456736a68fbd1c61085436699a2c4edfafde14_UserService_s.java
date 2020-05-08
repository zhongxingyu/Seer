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
 package org.jtalks.poulpe.service;
 
 import java.util.Collection;
 
 import org.jtalks.common.model.entity.User;
 
 /**
  * Service interface for operations with {@code User} persistent entity.
  * 
  * @author Guram Savinov
  */
public interface UserService extends org.jtalks.common.service.UserService {
 
     /**
      * Sets permanent ban status for the user list.
      * 
      * @param users
      *            the users to update ban status
      * @param permanentBan
      *            the permanent ban status
      * @param banReason
      *            the ban reason description
      */
     public void setPermanentBanStatus(Collection<User> users, boolean permanentBan,
             String banReason);
 
     /**
      * Sets temporary ban status for the user list.
      * 
      * @param users
      *            the users to update ban status
      * @param days
      *            the length of the temporary ban status in days
      * @param banReason
      *            the ban reason description
      */
     public void setTemporaryBanStatus(Collection<User> users, int days,
             String banReason);
 
 }
