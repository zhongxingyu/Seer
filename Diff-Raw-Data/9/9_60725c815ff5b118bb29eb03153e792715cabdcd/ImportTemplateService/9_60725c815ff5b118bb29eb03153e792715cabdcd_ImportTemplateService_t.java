 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 @Transactional
 public class ImportTemplateService {
     private StudyXmlSerializer studyXmlSerializer;
     private ActivityDao activityDao;
     private SourceDao sourceDao;
     private StudyDao studyDao;
     private DaoFinder daoFinder;
     private AmendmentService amendmentService;
     private TemplateService templateService;
     private StudyService studyService;
     private PlannedActivityDao plannedActivityDao;
     private StudySegmentDao studySegmentDao;
     private PeriodDao periodDao;
     private EpochDao epochDao;
     private ChangeDao changeDao;
     private DeltaDao deltaDao;
     private AmendmentDao amendmentDao;
 
     public void readAndSaveTemplate(InputStream stream) {
         Study study = studyXmlSerializer.readDocument(stream);
 
         // We do this so hibernate doesn't try to save the amendment
         study.setAmendment(null);
 
//        String devKey = study.getDevelopmentAmendment().getNaturalKey();

         // Check if development amendment is persisted and if it is, delete it
         if (study.getDevelopmentAmendment() != null) {
             String amendmentNaturalKey = study.getDevelopmentAmendment().getNaturalKey();
             Amendment dev = amendmentDao.getByNaturalKey(amendmentNaturalKey);
             if (dev != null) {
                 deleteAmendment(study, study.getDevelopmentAmendment());
                 study.setDevelopmentAmendment(null);
                 studyDao.save(study);
             }
         }
 
         try {
             stream.reset();
         } catch (IOException ioe) {
             throw new StudyCalendarSystemException("Problem importing template");
         }
         study = studyXmlSerializer.readDocument(stream);
//        study.setAmendment(cur);
 
         templatePostProcessing(study);
     }
 
     /**
      * @param study study that needs to be created or imported
      */
     public void templatePostProcessing(Study study) {
         resolveExistingActivitiesAndSources(study);
         resolveChangeChildrenFromPlanTreeNodeTree(study);
     }
 
     /**
      * Creates or updates the study
      * <p> Creates a new study if study does not exists, i.e. existingTemplate parameter is null
      * <p>Or if study already exists, updates the study by merging the new template with the existing one using these rules..
      * <li>Do not change any existing released amendments </li>
      * <li>Import any new released amendments (automatically creating activities, etc.)</li>
      * <li>Update the existing development amendment with any changes in the new one  </li>
      * </p>
      *
      * @param existingTemplate study which already exists and which needs to merged from new study
      * @param newTemplate      new study
      */
     public void mergeTemplate(final Study existingTemplate, final Study newTemplate) {
         if (existingTemplate == null) {
             templatePostProcessing(newTemplate);
         }//FIXME:Saurabh: implement the logic of merging two templates
 
     }
 
     protected void resolveExistingActivitiesAndSources(Study study) {
         List<PlannedActivity> all = new LinkedList<PlannedActivity>();
 
 
         List<Amendment> reverse = new LinkedList<Amendment>(study.getAmendmentsList());
         Collections.reverse(reverse);
         if (study.getDevelopmentAmendment() != null) {
             reverse.add(study.getDevelopmentAmendment());
         }
         for (Amendment amendment : reverse) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     if (change.getAction() == ChangeAction.ADD) {
                         PlanTreeNode<?> child = ((Add) change).getChild();
                         // Need this if change is already persisted in the database, then it won't have a child
                         // and we need to find from the child id
                         if (child == null) {
                             Class childClass = ((PlanTreeInnerNode) delta.getNode()).childClass();
                             DomainObjectDao dao = daoFinder.findDao(childClass);
                             child = (PlanTreeNode<?>) dao.getById(((ChildrenChange)change).getChildId());
                         }
                         
                         if (child instanceof PlannedActivity) {
                             all.add((PlannedActivity) child);
                         } else {
                             all.addAll(templateService.findChildren((PlanTreeInnerNode) child, PlannedActivity.class));
                         }
                     }
                 }
             }
         }
         Amendment dev = study.getDevelopmentAmendment();
         Amendment cur = study.getAmendment();
         study.setAmendment(null);
         study.setDevelopmentAmendment(null);
         for (PlannedActivity plannedActivity : all) {
             Activity activity = plannedActivity.getActivity();
 
             Activity existingActivity = activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName());
             if (existingActivity != null) {
                 plannedActivity.setActivity(existingActivity);
             } else {
                 Source existingSource = sourceDao.getByName(activity.getSource().getName());
                 if (existingSource != null) {
                     activity.setSource(existingSource);
                 }
             }
 
             sourceDao.save(plannedActivity.getActivity().getSource());
             activityDao.save(plannedActivity.getActivity());
         }
         study.setAmendment(cur);
         study.setDevelopmentAmendment(dev);
     }
 
     protected void resolveChangeChildrenFromPlanTreeNodeTree(Study study) {
         List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
         Amendment development = study.getDevelopmentAmendment();
         Collections.reverse(amendments);
         study.setAmendment(null);
         study.setDevelopmentAmendment(null);
 
         // StudyDao is being used instead of StudyService because we don't want to cascade to the amendments yet
         studyDao.save(study);
 
         for (Amendment amendment : amendments) {
             // If amendment already exists, we don't want to amend the study with it twice.
             if (amendment.getId() == null) {
                 for (Delta delta : amendment.getDeltas()) {
                     // resolve node
                     PlanTreeNode<?> deltaNode = findRealNode(delta.getNode());
                     if (deltaNode != null) {
                         delta.setNode(deltaNode);
                     } else {
                         throw new IllegalStateException("Delta " + delta.getGridId() + " references unknown node " + delta.getNode().getGridId());
                     }
 
                     // resolve child nodes
                     for (Object oChange : delta.getChanges()) {
                         if (oChange instanceof ChildrenChange) {
                             ChildrenChange change = (ChildrenChange) oChange;
                             PlanTreeNode<?> nodeTemplate = change.getChild();
 
                             // If node template is not null, element has yet to be persisted
                             if (nodeTemplate != null) {
                                 PlanTreeNode<?> node = findRealNode(nodeTemplate);
                                 if (node != null) {
                                     change.setChild(node);
                                 }
                             }
                         }
                     }
                 }
                 study.setDevelopmentAmendment(amendment);
                study.setAmendment(amendment.getPreviousAmendment());
                 amendmentService.amend(study);
             }
         }
 
         // Resolve delta nodes and child nodes for development amendment
         if (development != null) {
             for (Delta delta : development.getDeltas()) {
                 // resolve node
                 PlanTreeNode<?> deltaNode = findRealNode(delta.getNode());
                 if (deltaNode != null) {
                     delta.setNode(deltaNode);
                 } else {
                     throw new IllegalStateException("Delta " + delta.getGridId() + " references unknown node " + delta.getNode().getGridId());
                 }
 
                 // resolve child nodes
                 for (Object oChange : delta.getChanges()) {
                     if (oChange instanceof ChildrenChange) {
                         ChildrenChange change = (ChildrenChange) oChange;
                         PlanTreeNode<?> nodeTemplate = change.getChild();
                         PlanTreeNode<?> node = findRealNode(nodeTemplate);
                         if (node != null) {
                             change.setChild(node);
                         }
                     }
                 }
             }
         }
         study.setDevelopmentAmendment(development);
         studyService.save(study);
 
     }
 
     private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
         GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
         return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
     }
 
     public void deleteAmendment(Study study, Amendment amendment) {
         amendmentDao.delete(amendment);
         deleteDeltas(amendment.getDeltas());
     }
 
     protected void deleteDeltas (List<Delta<?>> deltas) {
         for (Delta delta : deltas) {
             deltaDao.delete(delta);
 
 
             List<Change> changesToRemove = new ArrayList<Change>();
             for (Object oChange : delta.getChanges()) {
                 Change change = (Change) oChange;
                 changesToRemove.add(change);
                 changeDao.delete(change);
                 if (ChangeAction.ADD.equals(change.getAction())) {
 
                     Class childClass = ((PlanTreeInnerNode) delta.getNode()).childClass();
                     DomainObjectDao dao = daoFinder.findDao(childClass);
                     PlanTreeNode<?> child = (PlanTreeNode<?>) dao.getById(((ChildrenChange)change).getChildId());
 
                     if (child instanceof Epoch) {
                         deleteEpoch((Epoch) child);
                     } else if (child instanceof StudySegment) {
                         deleteStudySegment((StudySegment) child);
                     } else if (child instanceof Period) {
                         deletePeriod((Period) child);
                     } else if (child instanceof PlannedActivity) {
                         deletePlannedActivity((PlannedActivity) child);
                     }
                 }
 
             }
             for (Change change : changesToRemove) {
                 delta.removeChange(change);
             }
         }
         deltas.clear();
     }
 
     // We don't want to add delete-orphaned to the hibernate cascade because
     // if planned tree node is part of another amendment, we don't want to destroy
     // that association also.
     protected void deletePlannedCalendar(PlannedCalendar calendar) {
         deleteEpochs(calendar.getEpochs());
     }
 
     protected void deleteEpochs(List<Epoch> epochs) {
         for (Epoch epoch : epochs) {
             deleteEpoch(epoch);
         }
         epochs.clear();
     }
 
     protected void deleteEpoch(Epoch epoch) {
         deleteStudySegments(epoch.getStudySegments());
         epochDao.delete(epoch);
     }
 
     protected void deleteStudySegments(List<StudySegment> segments) {
         for (StudySegment segment : segments) {
             deleteStudySegment(segment);
         }
         segments.clear();
     }
 
     private void deleteStudySegment(StudySegment segment) {
         deletePeriods(segment.getPeriods());
         studySegmentDao.delete(segment);
     }
 
     protected void deletePeriods(Set<Period> periods) {
         for(Period period : periods) {
             deletePeriod(period);
         }
         periods.clear();
     }
 
     private void deletePeriod(Period period) {
         deletePlannedActivities(period.getPlannedActivities());
         periodDao.delete(period);
     }
 
     protected void deletePlannedActivities(List<PlannedActivity> plannedActivities) {
         for (PlannedActivity activity : plannedActivities) {
             deletePlannedActivity(activity);
         }
         plannedActivities.clear();
     }
 
     private void deletePlannedActivity(PlannedActivity activity) {
         plannedActivityDao.delete(activity);
     }
 
     ////// Bean Setters
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
         this.studyXmlSerializer = studyXmlSerializer;
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 
     @Required
     public void setAmendmentService(AmendmentService amendmentService) {
         this.amendmentService = amendmentService;
     }
 
     @Required
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
     @Required
     public void setStudyService(StudyService studyService) {
         this.studyService = studyService;
     }
 
     @Required
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 
     @Required
     public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
         this.studySegmentDao = studySegmentDao;
     }
 
     @Required
     public void setPeriodDao(PeriodDao periodDao) {
         this.periodDao = periodDao;
     }
 
     @Required
     public void setEpochDao(EpochDao epochDao) {
         this.epochDao = epochDao;
     }
 
     @Required
     public void setChangeDao(ChangeDao changeDao) {
         this.changeDao = changeDao;
     }
 
     @Required
     public void setDeltaDao(DeltaDao deltaDao) {
         this.deltaDao = deltaDao;
     }
 
     @Required
     public void setAmendmentDao(AmendmentDao amendmentDao) {
         this.amendmentDao = amendmentDao;
     }
 }
