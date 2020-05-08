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
 package nl.surfnet.bod.domain;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.validation.ConstraintViolationException;
 
 import nl.surfnet.bod.AppConfiguration;
 import nl.surfnet.bod.config.IntegrationDbConfiguration;
 import nl.surfnet.bod.repo.InstituteRepo;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.data.domain.Sort;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.annotation.DirtiesContext.ClassMode;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class } )
 @Transactional
 @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
 public class PhysicalResourceGroupDbTest {
 
   @Resource
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Resource
   private PhysicalResourceGroupRepo physicalResourceGroupRepo;
 
   @Resource
   private InstituteRepo instituteRepo;
 
   @Test
   public void countAllPhysicalResourceGroups() {
     long count = physicalResourceGroupService.count();
 
     persistNewPhysicalResourceGroup();
 
     assertThat(count + 1, is(physicalResourceGroupService.count()));
   }
 
   @Test
   public void findPhysicalResourceGroup() {
     PhysicalResourceGroup physicalResourceGroup = persistNewPhysicalResourceGroup();
 
     PhysicalResourceGroup freshLoadedGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());
 
     assertThat(physicalResourceGroup, is(freshLoadedGroup));
   }
 
   @Test
   public void findPhysicalResourceGroupEntries() {
     persistNewPhysicalResourceGroup();
 
     long count = physicalResourceGroupService.count();
 
     int maxResults = count > 20 ? 20 : (int) count;
 
     List<PhysicalResourceGroup> result = physicalResourceGroupService.findEntries(0, maxResults, new Sort("id"));
 
     assertThat(result, hasSize((int) count));
   }
 
   @Test
   public void testUpdatePhysicalResourceGroupUpdate() {
     PhysicalResourceGroup physicalResourceGroup = persistNewPhysicalResourceGroup();
 
     Integer initialVersion = physicalResourceGroup.getVersion();
 
     physicalResourceGroup.setAdminGroup("New group");
 
     PhysicalResourceGroup merged = physicalResourceGroupService.update(physicalResourceGroup);
 
     physicalResourceGroupRepo.flush();
 
     assertThat(merged.getId(), is(physicalResourceGroup.getId()));
     assertThat(merged.getVersion(), greaterThan(initialVersion));
   }
 
   @Test
   public void savePhysicalResourceGroup() {
     PhysicalResourceGroup physicalResourceGroup = persistNewPhysicalResourceGroup();
 
     assertThat(physicalResourceGroup.getId(), greaterThan(0L));
   }
 
   @Test
   public void deletePhysicalResourceGroup() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().withNoIds().create();
 
    group.setInstitute(instituteRepo.save(group.getInstitute()));
     physicalResourceGroupService.save(group);
     physicalResourceGroupRepo.flush();
 
     physicalResourceGroupService.delete(group.getId());
     physicalResourceGroupRepo.flush();
 
     assertThat(physicalResourceGroupService.find(group.getId()), nullValue());
   }
 
   @Test(expected = ConstraintViolationException.class)
   public void physicalResourceGroupWithoutAEmailNotSave() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setManagerEmail("").create();
 
    group.setInstitute(instituteRepo.save(group.getInstitute()));
     physicalResourceGroupService.save(group);
 
     physicalResourceGroupRepo.flush();
   }
 
   private PhysicalResourceGroup persistNewPhysicalResourceGroup() {
     PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().withNoIds().create();
     physicalResourceGroup.setInstitute(instituteRepo.save(physicalResourceGroup.getInstitute()));
     physicalResourceGroupService.save(physicalResourceGroup);
     physicalResourceGroupRepo.flush();
 
     return physicalResourceGroup;
   }
 
 }
