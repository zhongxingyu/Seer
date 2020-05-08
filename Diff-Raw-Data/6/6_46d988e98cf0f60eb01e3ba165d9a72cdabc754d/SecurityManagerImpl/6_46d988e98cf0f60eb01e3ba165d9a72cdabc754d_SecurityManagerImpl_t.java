 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or
  * Your) shall mean a person or an entity, and all other entities that control,
  * are controlled by, or are under common control with the entity. Control for
  * purposes of this definition means (i) the direct or indirect power to cause
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
  * or (iii) beneficial ownership of such entity.
  *
  * This License is granted provided that You agree to the conditions described
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  * no-charge, irrevocable, transferable and royalty-free right and license in
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and
  * have distributed to and by third parties the caIntegrator2 Software and any
  * modifications and derivative works thereof; and (iii) sublicense the
  * foregoing rights set out in (i) and (ii) to third parties, including the
  * right to license such rights to further third parties. For sake of clarity,
  * and not by way of limitation, NCI shall have no right of accounting or right
  * of payment from You or Your sub-licensees for the rights granted under this
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the
  * above copyright notice, this list of conditions and the disclaimer and
  * limitation of liability of Article 6, below. Your redistributions in object
  * code form must reproduce the above copyright notice, this list of conditions
  * and the disclaimer of Article 6 in the documentation and/or other materials
  * provided with the distribution, if any.
  *
  * Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: This product includes software
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do
  * not include such end-user documentation, You shall include this acknowledgment
  * in the Software itself, wherever such third-party acknowledgments normally
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software.
  * This License does not authorize You to use any trademarks, service marks,
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM,
  * except as required to comply with the terms of this License.
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this
  * Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their
  * obligation to secure any required permissions from such third parties before
  * incorporating the Software into such third party proprietary software
  * programs. In the event that You fail to obtain such permissions, You agree
  * to indemnify NCI for any claims against NCI by such third parties, except to
  * the extent prohibited by law, resulting from Your failure to obtain such
  * permissions.
  *
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the Software, or any derivative works of the
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.security;
 
 import gov.nih.nci.caintegrator2.application.study.AuthorizedStudyElementsGroup;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.instancelevel.InstanceLevelSecurityHelper;
 import gov.nih.nci.security.dao.GroupSearchCriteria;
 import gov.nih.nci.security.dao.ProtectionElementSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSInsufficientAttributesException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.hibernate.Session;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 /**
  * Providers methods to access authentication and authorization data.
  */
 public class SecurityManagerImpl implements SecurityManager {
 
     private static final String APPLICATION_CONTEXT_NAME = "caintegrator2";
     private static final String STUDY_MANAGER_ROLE = "STUDY_MANAGER_ROLE";
     private static final String STUDY_INVESTIGATOR_ROLE = "STUDY_INVESTIGATOR_ROLE";
     private static final String STUDY_OBJECT = "gov.nih.nci.caintegrator2.domain.translational.Study";
     private static final String STUDY_ATTRIBUTE = "id";
     private static final String AUTHORIZED_STUDY_ELEMENTS_GROUP_OBJECT =
                             "gov.nih.nci.caintegrator2.application.study.AuthorizedStudyElementsGroup";
     private static final String AUTHORIZED_STUDY_ELEMENTS_GROUP_ATTRIBUTE = "id";
     private static final String UNCHECKED = "unchecked";
 
     private AuthorizationManagerFactory authorizationManagerFactory;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void createProtectionElement(StudyConfiguration studyConfiguration) throws CSException {
         if (doesUserExist(studyConfiguration.getUserWorkspace().getUsername())) {
             User user = retrieveCsmUser(studyConfiguration.getUserWorkspace().getUsername());
             String userId = String.valueOf(user.getUserId());
             ProtectionElement element = createProtectionElementInstance(studyConfiguration);
             element.setProtectionElementName(studyConfiguration.getStudy().getShortTitleText());
             Set<User> owners = new HashSet<User>();
             owners.add(user);
             element.setOwners(owners);
             element.setProtectionGroups(retrieveProtectionGroups(userId, STUDY_MANAGER_ROLE));
 
             getAuthorizationManager().createProtectionElement(element);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     public void deleteProtectionElement(StudyConfiguration studyConfiguration) throws CSException {
         ProtectionElement element = createProtectionElementInstance(studyConfiguration);
         SearchCriteria elementCriteria = new ProtectionElementSearchCriteria(element);
         List<ProtectionElement> retrievedElements = getAuthorizationManager().getObjects(elementCriteria);
         for (ProtectionElement pe : retrievedElements) {
             getAuthorizationManager().removeProtectionElement(String.valueOf(pe.getProtectionElementId()));
         }
         List<AuthorizedStudyElementsGroup> authStudyElementsGroups =
                                                 studyConfiguration.getAuthorizedStudyElementsGroups();
         for (AuthorizedStudyElementsGroup aseg : authStudyElementsGroups) {
             deleteProtectionElement(aseg);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     public void initializeFiltersForUserGroups(String username, Session session) throws CSException {
         if (doesUserExist(username)) {
             List<String> groupNames = new ArrayList<String>();
             String userId = String.valueOf(retrieveCsmUser(username).getUserId());
             for (Group group : (Set<Group>) getAuthorizationManager().getGroups(userId)) {
                 groupNames.add(group.getGroupName());
             }
             InstanceLevelSecurityHelper.initializeFiltersForGroups(groupNames.toArray(new String[groupNames.size()]),
                                                                    session, getAuthorizationManager());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<AuthorizedStudyElementsGroup> retrieveAuthorizedStudyElementsGroupsForInvestigator(String username,
                                                                     Set<AuthorizedStudyElementsGroup> availableGroups)
         throws CSException {
         if (!doesUserExist(username)) {
             return new HashSet<AuthorizedStudyElementsGroup>();
         }
 
         Set<AuthorizedStudyElementsGroup> authorizedStudyElementsGroups = new HashSet<AuthorizedStudyElementsGroup>();
         Set<ProtectionGroup> userProtectionGroups =
             retrieveProtectionGroups(String.valueOf(retrieveCsmUser(username).getUserId()),
                                                                         STUDY_INVESTIGATOR_ROLE);
         Set<Long> authorizedStudyElementsGroupIds = retrieveAuthorizedStudyElementsGroupIds(userProtectionGroups);
         for (AuthorizedStudyElementsGroup group : availableGroups) {
             if (authorizedStudyElementsGroupIds.contains(group.getId())) {
                 authorizedStudyElementsGroups.add(group);
             }
         }
         return authorizedStudyElementsGroups;
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<StudyConfiguration> retrieveManagedStudyConfigurations(String username, Collection<Study> studies)
         throws CSException {
         if (!doesUserExist(username)) {
             return new HashSet<StudyConfiguration>();
         }
         Set<StudyConfiguration> managedStudies = new HashSet<StudyConfiguration>();
         Set<ProtectionGroup> studyManagerProtectionGroups =
             retrieveProtectionGroups(String.valueOf(retrieveCsmUser(username).getUserId()), STUDY_MANAGER_ROLE);
         Set<Long> managedStudyIds = retrieveStudyIds(studyManagerProtectionGroups);
         for (Study study : studies) {
             // I think there's a bug with this function, so having to do it the hard way.
 //            if (getAuthorizationManager().checkPermission(username, STUDY_OBJECT, STUDY_ATTRIBUTE,
 //                                String.valueOf(study.getId()), Constants.CSM_UPDATE_PRIVILEGE)) {
 //                managedStudies.add(study.getStudyConfiguration());
 //            }
             if (managedStudyIds.contains(study.getId())) {
                 managedStudies.add(study.getStudyConfiguration());
             }
         }
         return managedStudies;
     }
 
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     private Set<Long> retrieveStudyIds(Set<ProtectionGroup> protectionGroups) throws CSException {
         Set<Long> managedStudyIds = new HashSet<Long>();
         for (ProtectionGroup group : protectionGroups) {
             Set<ProtectionElement> elements =
                 getAuthorizationManager().getProtectionElements(String.valueOf(group.getProtectionGroupId()));
             for (ProtectionElement element : elements) {
                 if (STUDY_OBJECT.equals(element.getObjectId()) && NumberUtils.isNumber(element.getValue())) {
                     managedStudyIds.add(Long.valueOf(element.getValue()));
                 }
             }
         }
         return managedStudyIds;
     }
 
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     private Set<Long> retrieveAuthorizedStudyElementsGroupIds(Set<ProtectionGroup> protectionGroups)
                                                                                     throws CSException {
         Set<Long> authorizedStudyElementsGroupIds = new HashSet<Long>();
         for (ProtectionGroup group : protectionGroups) {
             Set<ProtectionElement> elements =
                 getAuthorizationManager().getProtectionElements(String.valueOf(group.getProtectionGroupId()));
             for (ProtectionElement element : elements) {
                 if (AUTHORIZED_STUDY_ELEMENTS_GROUP_OBJECT.equals(element.getObjectId())
                                                             && NumberUtils.isNumber(element.getValue())) {
                     authorizedStudyElementsGroupIds.add(Long.valueOf(element.getValue()));
                 }
             }
         }
         return authorizedStudyElementsGroupIds;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean doesUserExist(String username) {
         try {
             return retrieveCsmUser(username) != null ? true : false;
         } catch (CSException e) {
             return false;
         }
     }
 
     private User retrieveCsmUser(String username) throws CSException {
         return getAuthorizationManager().getUser(username);
     }
 
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     private Set<ProtectionGroup> retrieveProtectionGroups(String userId, String csmRoleToBeRetrieved)
             throws CSException {
         Set<ProtectionGroup> protectionGroups = new HashSet<ProtectionGroup>();
         Set<Group> groups = getAuthorizationManager().getGroups(userId);
         for (Group group : groups) {
             Set<ProtectionGroupRoleContext> pgrcs =
                 getAuthorizationManager().getProtectionGroupRoleContextForGroup(String.valueOf(group.getGroupId()));
             for (ProtectionGroupRoleContext pgrc : pgrcs) {
                 for (Role role : (Set<Role>) pgrc.getRoles()) {
                     if (csmRoleToBeRetrieved.equals(role.getName())) {
                         protectionGroups.add(pgrc.getProtectionGroup());
                         break;
                     }
                 }
             }
         }
         return protectionGroups;
     }
 
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     private Set<ProtectionGroup> retrieveProtectionGroups(Group group, String csmRoleToBeRetrieved) throws CSException {
         Set<ProtectionGroup> protectionGroups = new HashSet<ProtectionGroup>();
         Set<ProtectionGroupRoleContext> pgrcs = getAuthorizationManager()
             .getProtectionGroupRoleContextForGroup(String.valueOf(group.getGroupId()));
         for (ProtectionGroupRoleContext pgrc : pgrcs) {
             for (Role role : (Set<Role>) pgrc.getRoles()) {
                 if (csmRoleToBeRetrieved.equals(role.getName())) {
                     protectionGroups.add(pgrc.getProtectionGroup());
                     break;
                 }
             }
         }
         return protectionGroups;
     }
 
     private ProtectionElement createProtectionElementInstance(StudyConfiguration studyConfiguration) {
         ProtectionElement element = new ProtectionElement();
         element.setAttribute(STUDY_ATTRIBUTE);
         element.setObjectId(STUDY_OBJECT);
         element.setValue(String.valueOf(studyConfiguration.getStudy().getId()));
         return element;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public AuthorizationManager getAuthorizationManager() throws CSException {
         return authorizationManagerFactory.getAuthorizationManager(APPLICATION_CONTEXT_NAME);
     }
 
     /**
      * @return the authorizationManagerFactory
      */
     public AuthorizationManagerFactory getAuthorizationManagerFactory() {
         return authorizationManagerFactory;
     }
 
     /**
      * @param authorizationManagerFactory the authorizationManagerFactory to set
      */
     public void setAuthorizationManagerFactory(AuthorizationManagerFactory authorizationManagerFactory) {
         this.authorizationManagerFactory = authorizationManagerFactory;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void createProtectionElement(StudyConfiguration studyConfiguration,
             AuthorizedStudyElementsGroup authorizedStudyElementsGroup) throws CSException {
         if (doesUserExist(studyConfiguration.getUserWorkspace().getUsername())) {
             User user = retrieveCsmUser(studyConfiguration.getUserWorkspace().getUsername());
             ProtectionElement element = createProtectionElementInstance(authorizedStudyElementsGroup);
             if (element == null) {
                 throw new CSInsufficientAttributesException();
             } else {
                 Group authorizedGroup = authorizedStudyElementsGroup.getAuthorizedGroup();
                 element.setProtectionElementName(authorizedGroup.getGroupName());
                 Set<User> owners = new HashSet<User>();
                 owners.add(user);
                 element.setOwners(owners);

                Set<ProtectionGroup> protectionGroups = new HashSet<ProtectionGroup>();
                protectionGroups.addAll(retrieveProtectionGroups(authorizedGroup, STUDY_INVESTIGATOR_ROLE));
                protectionGroups.addAll(retrieveProtectionGroups(authorizedGroup, STUDY_MANAGER_ROLE));
                element.setProtectionGroups(protectionGroups);
                 getAuthorizationManager().createProtectionElement(element);
             }
         }
 
     }
 
     /**
      * Create ProtectElementInstance in CSM.
      * @param authorizedStudyElementsGroup
      * @return element or null if ProtectionElement can not be created
      */
     private ProtectionElement createProtectionElementInstance(
             AuthorizedStudyElementsGroup authorizedStudyElementsGroup) {
         ProtectionElement element = new ProtectionElement();
         String value = String.valueOf(authorizedStudyElementsGroup.getId());
         if (value.equalsIgnoreCase("null")) {
             element = null;
         } else {
             element.setAttribute(AUTHORIZED_STUDY_ELEMENTS_GROUP_ATTRIBUTE);
             element.setObjectId(AUTHORIZED_STUDY_ELEMENTS_GROUP_OBJECT);
             element.setValue(value);
         }
         return element;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // CSM API is untyped
     public void deleteProtectionElement(AuthorizedStudyElementsGroup authorizedStudyElementsGroup) throws CSException {
         ProtectionElement element = createProtectionElementInstance(authorizedStudyElementsGroup);
         SearchCriteria elementCriteria = new ProtectionElementSearchCriteria(element);
         List<ProtectionElement> retrievedElements = getAuthorizationManager().getObjects(elementCriteria);
         for (ProtectionElement pe : retrievedElements) {
             getAuthorizationManager().removeProtectionElement(String.valueOf(pe.getProtectionElementId()));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public Collection<Group> getUnauthorizedGroups(StudyConfiguration studyConfiguration) throws CSException {
         SearchCriteria criteria = new GroupSearchCriteria(new Group());
         List<Group> groups = getAuthorizationManager().getObjects(criteria);
         List<AuthorizedStudyElementsGroup> authorizedGroups = studyConfiguration.getAuthorizedStudyElementsGroups();
         final List<String> authorizedGroupNames = Lists.newArrayList();
         for (AuthorizedStudyElementsGroup aseg : authorizedGroups) {
             authorizedGroupNames.add(aseg.getAuthorizedGroup().getGroupName());
         }
         Collection<Group> results = Collections2.filter(groups, new Predicate<Group>() {
             @Override
             public boolean apply(Group group) {
                 return !authorizedGroupNames.contains(group.getGroupName());
             }
         });
         return results;
     }
 }
