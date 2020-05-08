 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
 import edu.nwu.bioinformatics.commons.StringUtils;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.util.ObjectSetUtil;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.annotation.Propagation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 @Transactional
 public class TemplateService {
     public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
     public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
     private StudyCalendarAuthorizationManager authorizationManager;
     private StudyDao studyDao;
     private SiteDao siteDao;
     private StudySiteDao studySiteDao;
     private DeltaDao deltaDao;
     private SiteService siteService;
 
     public static final String USER_IS_NULL = "User is null";
     public static final String SITE_IS_NULL = "Site is null";
     public static final String SITES_LIST_IS_NULL = "Sites List is null";
     public static final String STUDY_IS_NULL = "Study is null";
     public static final String LIST_IS_NULL = "List parameter is null";
     public static final String STUDIES_LIST_IS_NULL = "StudiesList is null";
     public static final String STRING_IS_NULL = "String parameter is null";
 
     public void assignTemplateToSites(Study studyTemplate, List<Site> sites) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (sites == null) {
             throw new IllegalArgumentException(SITES_LIST_IS_NULL);
         }
         for (Site site : sites) {
             StudySite ss = new StudySite();
             ss.setStudy(studyTemplate);
             ss.setSite(site);
             studySiteDao.save(ss);
         }
     }
     
     public void assignTemplateToParticipantCds(Study studyTemplate, Site site, List<String> assignedUserIds, List<String> availableUserIds) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (assignedUserIds == null) {
             throw new IllegalArgumentException(LIST_IS_NULL);
         }
         if (availableUserIds == null) {
             throw new IllegalArgumentException(LIST_IS_NULL);
         }
         List<StudySite> studySites = studyTemplate.getStudySites();
         for (StudySite studySite : studySites) {
             if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                 String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                 authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
                 ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
                 authorizationManager.removeProtectionGroupUsers(availableUserIds, studySitePG);
             }
         }
     }
     
     public void removeTemplateFromSites(Study studyTemplate, List<Site> sites) {
         List<StudySite> studySites = studyTemplate.getStudySites();
         List<StudySite> toRemove = new LinkedList<StudySite>();
         List<Site> cannotRemove = new LinkedList<Site>();
         for (Site site : sites) {
             for (StudySite studySite : studySites) {
                 if (studySite.getSite().equals(site)) {
                     if (studySite.isUsed()) {
                         cannotRemove.add(studySite.getSite());
                     } else {
                         try {
                             authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(studySite));
                         } catch (RuntimeException e) {
                             throw e;
                         } catch (Exception e) {
                             throw new StudyCalendarSystemException(e);
                         }
                         toRemove.add(studySite);
                     }
                 }
             }
         }
         for (StudySite studySite : toRemove) {
             Site siteAssoc = studySite.getSite();
             siteAssoc.getStudySites().remove(studySite);
             siteDao.save(siteAssoc);
             Study studyAssoc = studySite.getStudy();
             studyAssoc.getStudySites().remove(studySite);
             studyDao.save(studyAssoc);
         }
         if (cannotRemove.size() > 0) {
             StringBuilder msg = new StringBuilder("Cannot remove ")
                 .append(StringUtils.pluralize(cannotRemove.size(), "site"))
                 .append(" (");
             for (Iterator<Site> it = cannotRemove.iterator(); it.hasNext();) {
                 Site site = it.next();
                 msg.append(site.getName());
                 if (it.hasNext()) msg.append(", ");
             }
             msg.append(") from study ").append(studyTemplate.getName())
                 .append(" because there are participant(s) assigned");
             throw new StudyCalendarValidationException(msg.toString());
         }
     }
     
     public void assignMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
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
                     authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
                 }
             }
         }
     }
     
     public Map getParticipantCoordinators(Study studyTemplate, Site site) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         Map<String, List> pcdMap = new HashMap<String, List>();
         List<StudySite> studySites = studyTemplate.getStudySites();
         for (StudySite studySite : studySites) {
             if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                pcdMap = authorizationManager.getUsers(PARTICIPANT_COORDINATOR_GROUP, DomainObjectTools.createExternalObjectId(studySite), DomainObjectTools.createExternalObjectId(site));
             }
         }
         return pcdMap;
     }
 
     public Map getSiteLists(Study studyTemplate) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         Map<String, List> siteLists = new HashMap<String, List>();
         List<Site> availableSites = new ArrayList<Site>();
         List<Site> assignedSites = new ArrayList<Site>();
         List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
         for (ProtectionGroup site : allSitePGs) {
             availableSites.add(DomainObjectTools.loadFromExternalObjectId(site.getProtectionGroupName(), siteDao));
         }
         for (StudySite ss : studyTemplate.getStudySites()) {
             assignedSites.add(ss.getSite());
         }
         availableSites = (List) ObjectSetUtil.minus(availableSites, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);
 
         return siteLists;
     }
     
     public Map getTemplatesLists(Site site, User participantCdUser) throws Exception {
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (participantCdUser == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
         Map<String, List> templatesMap = new HashMap<String, List>();
         List<Study> assignedTemplates = new ArrayList<Study>();
         List<Study> allTemplates = new ArrayList<Study>();
 
         List<StudySite> studySites = site.getStudySites();
         for (StudySite studySite : studySites) {
             allTemplates.add(studySite.getStudy());
             if (authorizationManager.isUserPGAssigned(DomainObjectTools.createExternalObjectId(studySite), participantCdUser.getUserId().toString())) {
                 assignedTemplates.add(studySite.getStudy());
             }
         }
 
         List<Study> availableTemplates = (List) ObjectSetUtil.minus(allTemplates, assignedTemplates);
         templatesMap.put(StudyCalendarAuthorizationManager.ASSIGNED_PES, assignedTemplates);
         templatesMap.put(StudyCalendarAuthorizationManager.AVAILABLE_PES, availableTemplates);
         return templatesMap;
     }
     
     public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
         if(siteName == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         return authorizationManager.getPGByName(siteName);
     }
     
     public List checkOwnership(String userName, List<Study> studies) throws Exception {
         if (userName == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         if (studies == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         return authorizationManager.checkOwnership(userName, studies);
     }
     
     public List getSitesForTemplateSiteCd(String userName, Study study) {
         if (userName == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         if (study == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         List<Site> sites = siteService.getSitesForSiteCd(userName);
         List<StudySite> allStudySites = study.getStudySites();
         List<Site> templateSites = new ArrayList<Site>();
         for (Site site : sites) {
             for (StudySite studySite : allStudySites) {
                 if (studySite.getSite().getId() == site.getId()) {
                     templateSites.add(site);
                 }
             }
         }
 
         return templateSites;
     }
     
     
     public void removeMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
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
                     String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                     ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
                     authorizationManager.removeProtectionGroupUsers(userIds, studySitePG);
                 }
             }
         }
     }
     
     @Transactional(propagation = Propagation.SUPPORTS)
     public <P extends PlanTreeNode<?>> P findParent(PlanTreeNode<P> node) {
         if (node.getParent() != null) {
             return node.getParent();                                                                                                    
         } else {
             Delta<P> delta = deltaDao.findDeltaWhereAdded(node);
             if (delta == null) {
                 throw new StudyCalendarSystemException("Could not locate delta where %s was added", node);
             } else {
                 return delta.getNode();
             }
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <T extends PlanTreeNode<?>> T findAncestor(PlanTreeNode<?> node, Class<T> klass) {
         boolean moreSpecific = DomainObjectTools.isMoreSpecific(node.getClass(), klass);
         boolean parentable = PlanTreeNode.class.isAssignableFrom(node.parentClass());
         if (moreSpecific && parentable) {
             PlanTreeNode<?> parent = findParent((PlanTreeNode<? extends PlanTreeNode<?>>) node);
             if (klass.isAssignableFrom(parent.getClass())) {
                 return (T) parent;
             } else {
                 return findAncestor(parent, klass);
             }
         } else {
             throw new StudyCalendarSystemException("%s is not a descendant of %s",
                 node.getClass().getSimpleName(), klass.getSimpleName());
         }
     }
 
     // this is PlanTreeNode instead of PlanTreeNode<?> due to a javac bug
     public Study findStudy(PlanTreeNode node) {
         if (node instanceof PlannedCalendar) return ((PlannedCalendar) node).getStudy();
         else return findAncestor(node, PlannedCalendar.class).getStudy();
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
 
     public void setDeltaDao(DeltaDao deltaDao) {
         this.deltaDao = deltaDao;
     }
 
     @Required
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
         this.authorizationManager = authorizationManager;
     }
 
     @Required
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 }
