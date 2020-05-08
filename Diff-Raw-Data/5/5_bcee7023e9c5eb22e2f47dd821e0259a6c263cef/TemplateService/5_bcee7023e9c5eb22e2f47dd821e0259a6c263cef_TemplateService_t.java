 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
 import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 import static java.util.Arrays.asList;
 
 /**
  * This service provides methods for:
  *   - analyzing the plan node tree in the presence of deltas
  *   - performing security-related operations on templates
  *
  * This is a fairly low-level service, with no dependencies on other services.
  *
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 @Transactional
 public class TemplateService {
     public static final String SUBJECT_COORDINATOR_ACCESS_ROLE = "SUBJECT_COORDINATOR";
     public static final String SUBJECT_COORDINATOR_GROUP = "SUBJECT_COORDINATOR";
     private StudyCalendarAuthorizationManager authorizationManager;
     private StudyDao studyDao;
     private SiteDao siteDao;
     private StudySiteDao studySiteDao;
     private DeltaDao deltaDao;
     private UserRoleDao userRoleDao;
     private AuthorizationService authorizationService;
     private DaoFinder daoFinder;
 
     public static final String USER_IS_NULL = "User is null";
     public static final String SITE_IS_NULL = "Site is null";
     public static final String STUDY_IS_NULL = "Study is null";
     public static final String LIST_IS_NULL = "List parameter is null";
     public static final String STUDIES_LIST_IS_NULL = "StudiesList is null";
     public static final String STRING_IS_NULL = "String parameter is null";
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User assignTemplateToSubjectCoordinator(
             Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
     ) {
         if (study == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (user == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
 
         UserRole userRole = user.getUserRole(SUBJECT_COORDINATOR);
         if (!userRole.getStudySites().contains(findStudySite(study, site))) {
             userRole.addStudySite(findStudySite(study, site));
             userRoleDao.save(userRole);
 
             assignMultipleTemplates(asList(study), site, user.getCsmUserId().toString());
         }
 
         return user;
     }
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User removeAssignedTemplateFromSubjectCoordinator(
             Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
     ) {
         if (study == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (user == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
         UserRole userRole = user.getUserRole(SUBJECT_COORDINATOR);
         StudySite studySite = study.getStudySite(site);
         if (user.hasAssignment(studySite)) {
             throw new StudyCalendarValidationException("%s is still responsible for one or more subjects on %s at %s.  Please reassign those subjects before removing %s from that study and site.",
                     user.getName(), study.getAssignedIdentifier(), site.getName(), user.getName());
         }
         if (userRole.getStudySites().contains(studySite)) {
             userRole.removeStudySite(studySite);
             userRoleDao.save(userRole);
 
             removeMultipleTemplates(asList(study), site, user.getCsmUserId().toString());
         }
 
         return user;
     }
 
     public void assignMultipleTemplates(List<Study> studyTemplates, Site site, String userId) {
         if (studyTemplates == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (userId == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         List<String> assignedUserIds = new ArrayList<String>();
 
         assignedUserIds.add(userId);
 
         for (Study template : studyTemplates) {
             List<StudySite> studySites = template.getStudySites();
             for (StudySite studySite : studySites) {
                 if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                     String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                     authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, SUBJECT_COORDINATOR_ACCESS_ROLE);
                 }
             }
         }
     }
 
     public void removeMultipleTemplates(List<Study> studyTemplates, Site site, String userId) {
         if (studyTemplates == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (userId == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         List<String> userIds = new ArrayList<String>();
 
         userIds.add(userId);
 
         for (Study template : studyTemplates) {
             List<StudySite> studySites = template.getStudySites();
             for (StudySite studySite : studySites) {
 
                 if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                     ProtectionGroup studySitePG = authorizationManager.getProtectionGroup(studySite);
                     authorizationManager.removeProtectionGroupUsers(userIds, studySitePG);
                 }
             }
         }
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <P extends Parent> P findParent(Child<P> node) {
         if (node.getParent() != null) {
             return node.getParent();
         } else {
             Delta<P> delta = deltaDao.findDeltaWhereAdded(node);
             if (delta != null) {
                 return delta.getNode();
             }
 
             delta = deltaDao.findDeltaWhereRemoved(node);
             if (delta != null) {
                 return delta.getNode();
             }
 
             throw new StudyCalendarSystemException("Could not locate delta where %s was added or where it was removed", node);
         }
     }
 
     @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <T extends Child> T findAncestor(Child node, Class<T> klass) {
         boolean moreSpecific = DomainObjectTools.isMoreSpecific(node.getClass(), klass);
         boolean parentable = PlanTreeNode.class.isAssignableFrom(node.parentClass());
         if (moreSpecific && parentable) {
             Parent parent = findParent(node);
             if (klass.isAssignableFrom(parent.getClass())) {
                 return (T) parent;
             } else {
                 if (parent instanceof Child){
                     return findAncestor((Child) parent, klass);
                 }
             }
         } else {
             throw new StudyCalendarSystemException("%s is not a descendant of %s",
                     node.getClass().getSimpleName(), klass.getSimpleName());
         }
         return null;
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     public <T extends Child> Collection<T> findChildren(Parent node, Class<T> childClass) {
         List<T> children = new LinkedList<T>();
         findChildren(node, childClass, children);
         return children;
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     private <T extends Child> void findChildren(Parent node, Class<T> childClass, Collection<T> target) {
         if (childClass.isAssignableFrom(node.childClass())) {
             target.addAll(node.getChildren());
         } else {
             for (Object o : node.getChildren()) {
                 if (o instanceof Parent) {
                     findChildren((Parent) o, childClass, target);
                 }
             }
         }
     }
 
     // this is PlanTreeNode instead of PlanTreeNode<?> due to a javac bug
     @SuppressWarnings({"RawUseOfParameterizedType"})
     @Transactional(propagation = Propagation.SUPPORTS)
     public Study findStudy(PlanTreeNode node) {
         if (node instanceof PlannedCalendar) return ((PlannedCalendar) node).getStudy();
         else return findAncestor(node, PlannedCalendar.class).getStudy();
     }
 
     /**
      * Finds the node in the given study which matches the type and id of parameter node.
      */
     @SuppressWarnings({ "unchecked" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <C extends Changeable> C findEquivalentChild(Study study, C node) {
         if (isEquivalent(node, study)) {
             return (C)study;
         }
         if (node instanceof Population) {
             for (Population pop : study.getPopulations()) {
                 if (isEquivalent(pop, node)) {
                     return (C) pop;
                 }
             }
         }
         return findEquivalentChild(study.getPlannedCalendar(), node);
     }
 
     @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <C extends Changeable> C findEquivalentChild(MutableDomainObject tree, C parameterNode) {
         if (isEquivalent(tree, parameterNode)) return (C) tree;
         if (tree instanceof Parent) {
             for (Object child : ((Parent) tree).getChildren()) {
                 C match = findEquivalentChild((Child) child, parameterNode);
                 if (match != null) return match;
             }
         }
         return null;
     }
 
     @Transactional(propagation = Propagation.SUPPORTS)
     public boolean isEquivalent(MutableDomainObject node, MutableDomainObject toMatch) {
         return (toMatch == node) ||
                 (sameClassIgnoringProxies(toMatch, node) && identifiersMatch(toMatch, node));
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <T extends PlanTreeNode> T findCurrentNode(T transientNode) {
         if (!transientNode.isMemoryOnly()) {
             return transientNode;
         }
         GridIdentifiableDao<T> dao
             = (GridIdentifiableDao<T>) daoFinder.findDao(transientNode.getClass());
         return dao.getByGridId(transientNode.getGridId());
     }
 
     // This is not a general solution, but it will work for all PlanTreeNode subclasses
     private boolean sameClassIgnoringProxies(Object toMatch, Object node) {
         return toMatch.getClass().isAssignableFrom(node.getClass())
                 || node.getClass().isAssignableFrom(toMatch.getClass());
     }
 
     private boolean identifiersMatch(MutableDomainObject toMatch, MutableDomainObject node) {
         boolean idMatch, gridIdMatch;
         idMatch = gridIdMatch = false;
 
         if (toMatch.getId() != null && node.getId() != null) {
             idMatch = toMatch.getId().equals(node.getId());
         }
         if (toMatch.getGridId() != null && node.getGridId() != null) {
             gridIdMatch = toMatch.getGridId().equals(node.getGridId());
         }
         return (idMatch || gridIdMatch);
     }
 
     public List<ReleasedTemplate> getPendingTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) {
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
         List<Study> subjectAssignableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
                 devableStudies,
                 authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
                 subjectAssignableStudies
         );
 
         List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
 
         List<ReleasedTemplate> pendingTemplates = new ArrayList<ReleasedTemplate>();
 
         for (ReleasedTemplate releasedTemplate : releasedTemplates) {
             Study releasedTemplateStudy = releasedTemplate.getStudy();
             List<Site> sites = releasedTemplateStudy.getSites();
             if (sites.size() == 0) {
                 if (!pendingTemplates.contains(releasedTemplate)) {
                     pendingTemplates.add(releasedTemplate);
                 }
             }
             for (Site site : sites) {
                 if (!isStudyAssignedToAnySite(releasedTemplateStudy) ||
                         !isStudyApprovedBySite(site, releasedTemplateStudy) ||
                         !isSubjectCoordinatorAssignedToStudy(releasedTemplateStudy)) {
                     if (!pendingTemplates.contains(releasedTemplate)) {
                         pendingTemplates.add(releasedTemplate);
                     }
                 }
             }
         }
         Collections.sort(pendingTemplates, new AlphabeticallyOrderedComparator());
         return pendingTemplates;
     }
 
     public static class AlphabeticallyOrderedComparator implements Comparator<ReleasedTemplate> {
         public static final Comparator<? super ReleasedTemplate> INSTANCE = new AlphabeticallyOrderedComparator();
         public int compare(ReleasedTemplate rt1, ReleasedTemplate rt2) {
             return rt1.getDisplayName().toLowerCase().compareTo(rt2.getDisplayName().toLowerCase());
         }
     }
 
     public List<ReleasedTemplate> getReleasedAndAssignedTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) {
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<Study> subjectAssignableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
                 devableStudies,
                 authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
                 subjectAssignableStudies
         );
 
         List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
 
         List<ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<ReleasedTemplate>();
 
         for (ReleasedTemplate releasedTemplate : releasedTemplates) {
             Study releasedTemplateStudy = releasedTemplate.getStudy();
             List<Site> sites = releasedTemplateStudy.getSites();
             for (Site site : sites) {
                 if (isStudyAssignedToAnySite(releasedTemplateStudy) &&
                         isStudyApprovedBySite(site, releasedTemplateStudy) &&
                         isSubjectCoordinatorAssignedToStudy(releasedTemplateStudy)) {
                     if (!releasedAndAssignedTemplates.contains(releasedTemplate)) {
                         releasedAndAssignedTemplates.add(releasedTemplate);
                     }
                 }
             }
         }
 
         log.debug("released and assigned templates are {}", releasedAndAssignedTemplates);
         return releasedAndAssignedTemplates;
     }
 
     public List<ReleasedTemplate> getReleasedTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) {
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<Study> subjectAssignableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
                 devableStudies,
                 authorizationService.filterStudiesForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
                 subjectAssignableStudies
         );
 
 
         List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
         log.debug("released templates are {}", releasedTemplates);
         return releasedTemplates;
     }
 
     public List<DevelopmentTemplate> getInDevelopmentTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) {
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, authorizationService.filterStudiesForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<DevelopmentTemplate> inDevelopmentTemplates = new ArrayList<DevelopmentTemplate>();
         for (Study devableStudy : devableStudies) {
             if (devableStudy.isInDevelopment()) {
                 inDevelopmentTemplates.add(new DevelopmentTemplate(devableStudy));
             }
         }
         log.debug("in-development templates are {}", inDevelopmentTemplates);
         return inDevelopmentTemplates;
     }
 
 
     private List<Study> union(List<Study>... lists) {
         Set<Study> union = new LinkedHashSet<Study>();
         for (List<Study> list : lists) {
             union.addAll(list);
         }
         return new ArrayList<Study>(union);
     }
 
     private boolean isStudyAssignedToAnySite(Study study) {
         return !study.getStudySites().isEmpty();
     }
 
     private boolean isStudyApprovedBySite(Site site, Study study) {
        if (site != null && site.getStudySite(study) != null) {
            return site.getStudySite(study).getUnapprovedAmendments().isEmpty();
        }
        return false;
     }
 
     private boolean isSubjectCoordinatorAssignedToStudy(Study study) {
         for (StudySite studySite : study.getStudySites()) {
             List<UserRole> userRoles = studySite.getUserRoles();
             for (UserRole userRole : userRoles) {
                 if (userRole.getRole().equals(SUBJECT_COORDINATOR)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     protected <T extends Changeable> void delete(Collection<T> collection) {
         for (T t : collection) {
             delete(t);
         }
     }
 
     // TODO: this should be in a more generic service, perhaps
     @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
     public <T extends MutableDomainObject> void delete(T object) {
         if (object != null) {
             DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(object.getClass());
             if (!(dao instanceof DeletableDomainObjectDao)) {
                 throw new StudyCalendarSystemException(
                         "DAO for %s (%s) does not implement the deletable interface",
                         object.getClass().getSimpleName(), dao.getClass().getName()
                 );
             }
             DeletableDomainObjectDao<T> deleter = (DeletableDomainObjectDao) dao;
             if (object instanceof Parent) {
                 Parent innerNode = (Parent) object;
                 delete(innerNode.getChildren());
                 innerNode.getChildren().clear();
             }
             deleter.delete(object);
         }
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setStudySiteDao(StudySiteDao studySiteDao) {
         this.studySiteDao = studySiteDao;
     }
 
     @Required
     public void setDeltaDao(DeltaDao deltaDao) {
         this.deltaDao = deltaDao;
     }
 
     @Required
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
         this.authorizationManager = authorizationManager;
     }
 
     @Required
     public void setUserRoleDao(UserRoleDao userRoleDao) {
         this.userRoleDao = userRoleDao;
     }
 
     @Required
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 
     @Required
     public void setAuthorizationService(AuthorizationService authorizationService) {
         this.authorizationService = authorizationService;
     }
 }
