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
 package org.jtalks.poulpe.service.transactional;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.entity.User;
 import org.jtalks.common.security.acl.AclManager;
 import org.jtalks.common.security.acl.sids.UserGroupSid;
 import org.jtalks.common.security.acl.sids.UserSid;
 import org.jtalks.common.service.exceptions.NotFoundException;
 import org.jtalks.common.service.transactional.AbstractTransactionalEntityService;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.dto.SecurityGroupList;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.logic.UserBanner;
 import org.jtalks.poulpe.service.GroupService;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import ru.javatalks.utils.general.Assert;
 
 import java.util.List;
 
 /**
  * @author alexander afanasiev
  * @author stanislav bashkirtsev
  */
 public class TransactionalGroupService extends AbstractTransactionalEntityService<Group, GroupDao>
         implements GroupService {
     private final UserBanner userBanner;
     private final AclManager manager;

     /**
      * Create an instance of entity based service
      *
      * @param groupDao   - data access object, which should be able do all CRUD
      *                   operations.
      * @param userBanner - class for working with banning users instance
      */
    public TransactionalGroupService(GroupDao groupDao, UserBanner userBanner, AclManager manager) {
         this.dao = groupDao;
         this.userBanner = userBanner;
         this.manager = manager;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getAll() {
         return dao.getAll();
     }
 
     @Override
     public SecurityGroupList getSecurityGroups() {
         return new SecurityGroupList(dao.getAll()).withAnonymousGroup();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getByNameContains(String name) {
         return dao.getByNameContains(name);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getByName(String name) {
         return dao.getByName(name);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteGroup(Group group) throws NotFoundException {
         Assert.throwIfNull(group, "group");
         dao.delete(group);
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         User currentUser = (User) auth.getPrincipal();
         UserGroupSid sid = new UserGroupSid(group);
         UserSid sidHeier = new UserSid(currentUser);
         try {
             manager.deleteSid(sid, sidHeier);
         } catch (EmptyResultDataAccessException noSidError) {
             throw new NotFoundException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void saveGroup(Group group) {
         Assert.throwIfNull(group, "group");
         group.setName(group.getName().trim());
         dao.saveOrUpdate(group);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getBannedUsersGroups() {
         return userBanner.getBannedUsersGroups();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<PoulpeBranch> getModeratedBranches(Group group) {
         return dao.getModeratingBranches(group);
     }
 }
