 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.repo.ActivationEmailLinkRepo;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.web.security.Security;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 @Service
 @Transactional
 public class PhysicalResourceGroupService extends AbstractFullTextSearchService<PhysicalResourceGroup> {
 
   private final Logger log = LoggerFactory.getLogger(this.getClass());
 
   @Resource
   private PhysicalResourceGroupRepo physicalResourceGroupRepo;
 
   @Resource
   private ActivationEmailLinkRepo activationEmailLinkRepo;
 
   @Resource
   private EmailSender emailSender;
 
   @Resource
   private LogEventService logEventService;
 
   @Resource
   private InstituteService instituteService;
 
   @PersistenceContext
   private EntityManager entityManager;
 
   public long count() {
     return physicalResourceGroupRepo.count();
   }
 
   public void delete(final long primaryKey) {
     physicalResourceGroupRepo.delete(primaryKey);
   }
 
   public PhysicalResourceGroup find(final Long id) {
     return physicalResourceGroupRepo.findOne(id);
   }
 
   public List<PhysicalResourceGroup> findAll() {
     return physicalResourceGroupRepo.findAll();
   }
 
   public List<PhysicalResourceGroup> findEntries(int firstResult, int maxResults, Sort sort) {
     return physicalResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public void createForGroupsIfNotExist(Collection<UserGroup> sabGroups, String managerEmail) {
     for (UserGroup userGroup : sabGroups) {
       createPhysicalResourceGroupIfNotExist(userGroup, managerEmail);
     }
   }
 
   @VisibleForTesting
   PhysicalResourceGroup createPhysicalResourceGroupIfNotExist(final UserGroup userGroup, final String managerEmail) {
     Preconditions.checkArgument(userGroup.getInstituteShortName().isPresent(),
         "The shortName of the institute should be present for userGroup: {}", userGroup);
 
    if (CollectionUtils.isEmpty(findByAdminGroup(userGroup.getId()))) {
       PhysicalResourceGroup prg = new PhysicalResourceGroup();
       prg.setAdminGroup(userGroup.getId());
 
      Institute institute = instituteService.findByShortName(userGroup.getInstituteShortName().get());
       prg.setInstitute(institute);
       prg.setManagerEmail(managerEmail);
 
       return save(prg);
     }
     return null;
   }
 
   public PhysicalResourceGroup save(final PhysicalResourceGroup physicalResourceGroup) {
     physicalResourceGroupRepo.save(physicalResourceGroup);
 
     // log event after creation, so the ID is set by hibernate
     logEventService.logCreateEvent(Security.getUserDetails(), physicalResourceGroup);
 
     return physicalResourceGroup;
   }
 
   public PhysicalResourceGroup update(final PhysicalResourceGroup physicalResourceGroup) {
     logEventService.logUpdateEvent(Security.getUserDetails(), "", physicalResourceGroup);
 
     return physicalResourceGroupRepo.save(physicalResourceGroup);
   }
 
   public boolean hasRelatedPhysicalResourceGroup(UserGroup group) {
     return (group != null) && (physicalResourceGroupRepo.findByAdminGroup(group.getId()) != null);
   }
 
   public Collection<PhysicalResourceGroup> findAllForAdminGroups(Collection<UserGroup> groups) {
     if (groups.isEmpty()) {
       return Collections.emptyList();
     }
 
     Collection<String> groupIds = newArrayList(transform(groups, new Function<UserGroup, String>() {
       @Override
       public String apply(UserGroup group) {
         return group.getId();
       }
     }));
 
     return physicalResourceGroupRepo.findByAdminGroupIn(groupIds);
   }
 
   public List<PhysicalResourceGroup> findByAdminGroup(String groupId) {
     return physicalResourceGroupRepo.findByAdminGroup(groupId);
   }
 
   public ActivationEmailLink findActivationLink(String uuid) {
     ActivationEmailLink activationEmailLink = activationEmailLinkRepo.findByUuid(uuid);
 
     if (activationEmailLink != null) {
       activationEmailLink.setSourceObject(find(activationEmailLink.getSourceId()));
     }
 
     return activationEmailLink;
   }
 
   public Collection<PhysicalResourceGroup> findAllWithPorts() {
     Specification<PhysicalResourceGroup> withPhysicalPorts = new Specification<PhysicalResourceGroup>() {
       @Override
       public Predicate toPredicate(Root<PhysicalResourceGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.isNotEmpty(root.get(PhysicalResourceGroup_.physicalPorts));
       }
     };
 
     return physicalResourceGroupRepo.findAll(withPhysicalPorts);
   }
 
   public void activate(ActivationEmailLink activationEmailLink) {
     log.info("Activating link [{}] for physical resource group: {}", activationEmailLink.getUuid(), activationEmailLink
         .getSourceObject().getName());
     activationEmailLink.activate();
     activationEmailLink.getSourceObject().setActive(true);
 
     activationEmailLinkRepo.save(activationEmailLink);
     update(activationEmailLink.getSourceObject());
 
     // Log event after creation, so the ID is set by hibernate
     logEventService.logCreateEvent(Security.getUserDetails(), activationEmailLink);
   }
 
   @Transactional
   public ActivationEmailLink sendActivationRequest(PhysicalResourceGroup physicalResourceGroup) {
     deActivatePhysicalResourceGroup(physicalResourceGroup);
 
     ActivationEmailLink link = activationEmailLinkRepo.save(new ActivationEmailLink(physicalResourceGroup));
 
     emailSender.sendActivationMail(link);
 
     link.emailWasSent();
     return activationEmailLinkRepo.save(link);
   }
 
   private void deActivatePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
     physicalResourceGroup.setActive(false);
     physicalResourceGroupRepo.save(physicalResourceGroup);
   }
 
   public List<Long> findAllIds(Sort sort) {
     return physicalResourceGroupRepo.findIdsWithWhereClause(Optional.<Specification<PhysicalResourceGroup>> absent(),
         Optional.<Sort> fromNullable(sort));
   }
 
   @Override
   protected EntityManager getEntityManager() {
     return entityManager;
   }
 
 }
