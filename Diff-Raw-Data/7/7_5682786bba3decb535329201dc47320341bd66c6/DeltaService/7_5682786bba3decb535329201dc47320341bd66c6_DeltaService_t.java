 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Child;
 import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
 import edu.northwestern.bioinformatics.studycalendar.service.delta.Mutator;
 import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
 import edu.northwestern.bioinformatics.studycalendar.service.delta.DeltaIterator;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 /**
  * Provides methods for calculating deltas and for applying them to PlannedCalendars.
  * Also provides maintenance methods for deltas.
  *
  * @author Rhett Sutphin
  */
 @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
 public class DeltaService {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private MutatorFactory mutatorFactory;
     private DaoFinder daoFinder;
     private DeltaDao deltaDao;
     private ChangeDao changeDao;
     private TemplateService templateService;
     private NowFactory nowFactory;
 
     /**
      * Applies all the deltas in the given revision to the source study,
      * returning a new, transient Study.  The revision might be
      * and in-progress amendment or a customization.
      */
     public Study revise(Study source, Revision revision) {
         log.debug("Revising {} with {}", source, revision);
         Study revised = source.transientClone();
         apply(revised, revision);
         return revised;
     }
 
     /**
      * Applies the given amendment to the calendar for the subject in the
      * given assignment.
      */
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void amend(StudySubjectAssignment assignment, Amendment amendment) {
        if (assignment == null) {
            throw new StudyCalendarSystemException("Assignment can not be null");
        }
        if (amendment == null) {
            throw new StudyCalendarSystemException("Amendment can not be null");
        }
        
         if (!assignment.getStudySite().getStudy().getAmendmentsList().contains(amendment)) {
             throw new StudyCalendarSystemException("The amendment {} does not apply to assignment {}", amendment.getId(), assignment.getId());
         }
         if (assignment.getCurrentAmendment().equals(amendment.getPreviousAmendment())) {
             apply(assignment.getScheduledCalendar(), amendment);
             assignment.setCurrentAmendment(amendment);
         } else {
             amend(assignment, amendment.getPreviousAmendment());
             amend(assignment, amendment);
         }
     }
 
     public <T extends PlanTreeNode<?>> T revise(T source) {
         return revise(source, null);
     }
 
     @SuppressWarnings({ "unchecked" })
     public <T extends PlanTreeNode<?>> T revise(T source, Revision revision) {
         PlannedCalendar calendar;
         if (PlannedCalendar.class.isAssignableFrom(source.getClass())) {
             calendar = (PlannedCalendar) source;
         } else {
             calendar = templateService.findAncestor(source, PlannedCalendar.class);
         }
 
         if (revision == null) {
             revision = calendar.getStudy().getDevelopmentAmendment();
         }
 
         Study revised = revise(calendar.getStudy(), revision);
         return (T) templateService.findEquivalentChild(revised.getPlannedCalendar(), source);
     }
 
     /**
      * Applies all the deltas in the given revision directly to the given study.
      */
     public void apply(Study target, Revision revision) {
         log.debug("Applying {} to {}", revision, target);
         DeltaIterator di = new DeltaIterator(revision.getDeltas(), target, templateService, false);
         while (di.hasNext()){
             Delta<?> delta = di.next();
             Changeable affected = findNodeForDelta(target, delta);
             for (Change change : delta.getChanges()) {
                 log.debug("Applying change {} on {}", change, affected);
                 mutatorFactory.createMutator(affected, change).apply(affected);
             }
         }
     }
 
     public void revert(Study target, Revision revision) {
         log.debug("Reverting {} from {}", revision, target);
         DeltaIterator di = new DeltaIterator(revision.getDeltas(), target, templateService, true);
         while (di.hasNext()) {
             Delta<?> delta = di.next();
             Changeable affected = findNodeForDelta(target, delta);
             List<Change> reverseChanges = new ArrayList<Change>(delta.getChanges());
             Collections.reverse(reverseChanges);
             for (Change change : reverseChanges) {
                 log.debug("Rolling back change {} on {}", change, affected);
                 mutatorFactory.createMutator(affected, change).revert(affected);
             }
         }
     }
 
     private void apply(ScheduledCalendar target, Revision revision) {
         log.debug("Applying {} to {}", revision, target);
         DeltaIterator di = new DeltaIterator(revision.getDeltas(),  target.getAssignment().getStudySite().getStudy(), templateService, false);
         while (di.hasNext()){
             Delta<?> delta = di.next();
             Changeable affected = findNodeForDelta(target.getAssignment().getStudySite().getStudy(), delta);
             for (Change change : delta.getChanges()) {
                 log.debug("Applying change {} on {}", change, affected);
                 Mutator mutator = mutatorFactory.createMutator(affected, change);
                 if (mutator.appliesToExistingSchedules()) {
                     mutator.apply(target);
                 }
             }
         }
     }
 
     /**
      * Rolls the given study instance back to the state it was in in the previous amendment.
      * Unlike {@link AmendmentService#getAmendedStudy}, it directly modifies the passed-in
      * instance &mdash; You may want to pass in a transient instance.
      *
      * @throws StudyCalendarValidationException if the study doesn't have a previous amendment
      * @param source
      * @return the passed-in instance (for chaining)
      */
     public Study amendToPreviousVersion(Study source) {
         if (source.getAmendment() == null || source.getAmendment().getPreviousAmendment() == null) {
             throw new StudyCalendarValidationException("%s does not have a previous amendment", source);
         }
         log.debug("Rolling {} back to {}", source, source.getAmendment().getPreviousAmendment().getName());
         revert(source, source.getAmendment());
         source.setAmendment(source.getAmendment().getPreviousAmendment());
         return source;
     }
 
     /**
      * Merge the change for the node into the target revision
      * @param target
      * @param node
      * @param change
      */
     @Transactional(propagation = Propagation.SUPPORTS)
     public void updateRevision(Revision target, Changeable node, Change change) {
         log.debug("Updating {}", target);
         if (node.isDetached()) {
             log.debug("{} is detached; apply {} directly", node, change);
             mutateNode(node, change);
         } else {
             log.debug("{} is part of the live tree; track {} separately", node, change);
             Delta<?> existing = null;
             for (Delta<?> delta : target.getDeltas()) {
                 if (templateService.isEquivalent(delta.getNode(), node)) {
                     existing = delta;
                     break;
                 }
             }
             if (existing == null) {
                 log.debug("  - this is the first change; create new delta", change, node);
                 target.getDeltas().add(Delta.createDeltaFor(node, change));
             } else {
                 log.debug("  - it has been changed before; merge into existing delta {}", existing);
                 change.mergeInto(existing, nowFactory.getNow());
             }
         }
     }
 
     /**
      * Merge the change for the node into the target revision
      * @param target
      * @param node
      * @param change
      */
     @Transactional(propagation = Propagation.SUPPORTS)
     public void updateRevisionForStudy(Revision target, Study node, Change change) {
         log.debug("Updating {}", target);
         Delta<?> existing = null;
         for (Delta<?> delta : target.getDeltas()) {
             if (templateService.isEquivalent(delta.getNode(), node)) {
                 existing = delta;
                 break;
             }
         }
         if (existing == null) {
             log.debug("  - this is the first change; create new delta", change, node);
             target.getDeltas().add(Delta.createDeltaFor(node, change));
         } else {
             log.debug("  - it has been changed before; merge into existing delta {}", existing);
             change.mergeInto(existing, nowFactory.getNow());
         }
     }
 
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void mutateNode(Changeable node, Change change) {
         mutatorFactory.createMutator(node, change).apply(node);
     }
 
     private <C extends Changeable> C findNodeForDelta(Study revised, Delta<C> delta) {
         C affected = templateService.findEquivalentChild(revised, delta.getNode());
         if (affected == null) {
             throw new StudyCalendarSystemException(
                 "Could not find a node in the target study matching the node in %s", delta);
         }
         return affected;
     }
 
     public void saveRevision(Revision revision) {
         if (revision == null) {
             throw new NullPointerException("Can't save a null revision");
         }
         if (!(revision instanceof MutableDomainObject)) {
             throw new StudyCalendarSystemException("%s is not a MutableDomainObject",
                 revision.getClass().getName());
         }
         findDaoAndSave((MutableDomainObject) revision);
         for (Delta<?> delta : revision.getDeltas()) {
             log.debug("saveRevision: examining delta {}", delta);
             for (Change change : delta.getChanges()) {
                 if (change.getAction() == ChangeAction.ADD) {
                     log.debug("saveRevision: examining add {}", change);
                     Child child = ((Add) change).getChild();
                     if (child != null) {
                         log.debug("saveRevision: saving added child {}", child);
                         findDaoAndSave(child);
                     }
                 }
             }
             log.debug("saveRevision: saving delta node {}", delta.getNode());
             findDaoAndSave(delta.getNode());
             log.debug("saveRevision: saving delta {}", delta);
             deltaDao.save(delta);
         }
     }
 
     private void findDaoAndSave(MutableDomainObject object) {
         DomainObjectDao<?> dao = daoFinder.findDao(object.getClass());
         if (!(dao instanceof MutableDomainObjectDao)) {
             throw new StudyCalendarSystemException("%s does not implement %s", dao.getClass().getName(),
                 MutableDomainObjectDao.class.getName());
         }
         ((MutableDomainObjectDao) dao).save(object);
     }
 
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void delete(Delta<?> delta) {
         for (Change change : delta.getChanges()) {
             if (change.getAction() == ChangeAction.ADD) {
                 Child child = findChangeChild((Add) change);
                 templateService.delete(child);
             }
         }
         for (Change change : new ArrayList<Change>(delta.getChanges())) {
             delta.removeChange(change, nowFactory.getNow());
             changeDao.delete(change);
         }
         deltaDao.delete(delta);
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public Child findChangeChild(ChildrenChange change) {
         Child child = change.getChild();
         if (child == null) {
             Parent parent = (Parent) change.getDelta().getNode();
             child = (Child) findDaoAndLoad(change.getChildId(), parent.childClass());
         }
         return child;
     }
 
     @SuppressWarnings({ "unchecked" })
     private <T extends DomainObject> T findDaoAndLoad(int id, Class<T> klass) {
         DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(klass);
         return dao.getById(id);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setMutatorFactory(MutatorFactory mutatorFactory) {
         this.mutatorFactory = mutatorFactory;
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
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 
     @Required
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
     @Required
     public void setNowFactory(NowFactory nowFactory) {
         this.nowFactory = nowFactory;
     }
 }
