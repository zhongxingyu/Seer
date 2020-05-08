 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.core.editors.CollectionEditor;
 import edu.northwestern.bioinformatics.studycalendar.core.editors.ControlledVocabularyEditor;
 import edu.northwestern.bioinformatics.studycalendar.core.editors.EditorUtils;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.auditing.AuditEventDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
 import gov.nih.nci.cabig.ctms.audit.domain.Operation;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.transaction.annotation.Transactional;
 import java.util.*;
 
 /**
  * @author Jalpa Patel
  */
 
 @Transactional
 public class UserActionService {
     private UserActionDao userActionDao;
     private AuditEventDao auditEventDao;
     private ApplicationSecurityManager applicationSecurityManager;
     private DaoFinder daoFinder;
     private AmendmentDao amendmentDao;
     private PopulationDao populationDao;
 
     public List<UserAction> getUndoableActions(String context) {
         List<UserAction> userActions = userActionDao.getUserActionsByContext(context);
         Collections.reverse(userActions);
         List<UserAction> undoable = new ArrayList<UserAction>();
 
         for (UserAction ua : userActions) {
             if (isUndoableUserAction(ua)) {
                 undoable.add(ua);
             }
         }
         return undoable;
     }
 
     private boolean isUndoableUserAction(UserAction ua) {
         if (!ua.getUser().getName().equals(applicationSecurityManager.getUserName())) {
             return false;
         } else if (ua.isUndone()) {
             return false;
         }
         return isUndoableActionWithAuditEvent(ua);
     }
 
     private boolean isUndoableActionWithAuditEvent(UserAction ua) {
         List<AuditEvent> events = auditEventDao.getAuditEventsByUserActionId(ua.getGridId());
         for (AuditEvent ae : events) {
             List<AuditEvent> laterEvents = auditEventDao.getAuditEventsWithLaterTimeStamp(
                         ae.getReference().getClassName(), ae.getReference().getId(), ae.getInfo().getTime());
             for (AuditEvent laterEvent : laterEvents) {
                 if (laterEvent.getUserActionId() == null) {
                     return false;
                 } else {
                     UserAction ua1 = userActionDao.getByGridId(laterEvent.getUserActionId());
                     if (ua1 != null) {
                         if (!ua1.getUser().getName().equals(ua.getUser().getName())) {
                             return false;
                         } else if (!ua1.getContext().equals(ua.getContext())) {
                             return false;
                         }
                     }
                 }
             }
         }
         return true;
     }
 
     @SuppressWarnings({ "unchecked" })
     public UserAction applyUndo(UserAction userAction) {
         List<AuditEvent> auditEvents = auditEventDao.getAuditEventsWithValuesByUserActionId(userAction.getGridId());
 
         for (AuditEvent ae : auditEvents) {
             applyUndoToAuditEvent(ae);
         }
         userAction.setUndone(true);
         return userAction;
     }
 
     private void applyUndoToAuditEvent(AuditEvent ae) {
         Integer object_Id = ae.getReference().getId();
         if (object_Id == null) {
             throw new StudyCalendarError("Audit event's reference object id is null. This shouldn't be possible");
         }
         Class<? extends DomainObject> klass = findClass(ae.getReference().getClassName());
         DeletableDomainObjectDao dao = findDaoAndLoad(klass);
         AbstractMutableDomainObject entity = (AbstractMutableDomainObject) dao.getById(object_Id);
 
         if (ae.getOperation().equals(Operation.UPDATE)) {
            saveOrUpdateEntity(ae, dao, entity);
         } else if (ae.getOperation().equals(Operation.CREATE)) {
             if (entity != null) {
                 deleteEntity(dao, entity);
             }
         } else if (ae.getOperation().equals(Operation.DELETE)) {
             if (entity == null) {
                 try {
                     AbstractMutableDomainObject newObj = (AbstractMutableDomainObject) klass.newInstance();
                     saveOrUpdateEntity(ae, dao, newObj);
                 } catch (InstantiationException e) {
                     throw new StudyCalendarError("This shouldn't be possible", e);
                 } catch (IllegalAccessException e) {
                     throw new StudyCalendarError("This shouldn't be possible", e);
                 }
             }
         } else {
            throw new StudyCalendarError("Unexpected Audit Event. Undo to the audit event is not possible");
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     private void saveOrUpdateEntity(AuditEvent ae, DeletableDomainObjectDao dao, AbstractMutableDomainObject entity) {
         BeanWrapperImpl objWrapper = createBeanWrapper(entity);
 
         List<DataAuditEventValue> values = ae.getValues();
         if (values.size() == 1 && isVersionAttribute(values.get(0).getAttributeName())) {
             return;
         }
 
         for (DataAuditEventValue value : values) {
             if( !isVersionAttribute(value.getAttributeName())) {
                 objWrapper.setPropertyValue(value.getAttributeName(), value.getPreviousValue());
             }
         }
         dao.save(entity);
     }
 
     @SuppressWarnings({ "unchecked" })
     private void deleteEntity(DeletableDomainObjectDao dao, AbstractMutableDomainObject entity) {
         dao.delete(entity);
     }
 
     private BeanWrapperImpl createBeanWrapper(AbstractMutableDomainObject entity) {
         BeanWrapperImpl objWrapper = new BeanWrapperImpl(entity);
         objWrapper.registerCustomEditor(Date.class,
                 new CustomDateEditor(DateFormat.getISO8601Format(), true));
         objWrapper.registerCustomEditor(ScheduledActivityMode.class,
                 new ControlledVocabularyEditor(ScheduledActivityMode.class));
         objWrapper.registerCustomEditor(Amendment.class, new DaoBasedEditor(amendmentDao));
         objWrapper.registerCustomEditor(HashSet.class, "populations", new CollectionEditor(Set.class, populationDao));
         objWrapper.registerCustomEditor(TreeSet.class, "labels", new CollectionEditor(SortedSet.class));
         objWrapper.registerCustomEditor(List.class, "properties", new CollectionEditor(ArrayList.class) {
             protected Object convertElement(String element) {
                 SubjectProperty sp = new SubjectProperty();
                 String[] propertyValues = EditorUtils.splitValue(element);
                 if (propertyValues.length == 0) {
                     throw new StudyCalendarError("There are no property values for subject properties");
                 } else {
                     if (propertyValues.length > 1) {
                        sp.setName(EditorUtils.getDecodedString(propertyValues[0]));
                        sp.setValue(EditorUtils.getDecodedString(propertyValues[1]));
                     } else {
                        sp.setName(EditorUtils.getDecodedString(propertyValues[0]));
                        sp.setValue(EditorUtils.getDecodedString(null));
                     }
                 }
                 return sp;
             }
         });
         return objWrapper;
     }
 
     private boolean isVersionAttribute(String attributeName) {
         return attributeName.equals("version");
     }
 
     @SuppressWarnings({ "unchecked" })
     private DeletableDomainObjectDao findDaoAndLoad(Class<? extends DomainObject> klass) {
         DomainObjectDao<?> dao = daoFinder.findDao(klass);
         if ( !(dao instanceof DeletableDomainObjectDao)) {
             throw new StudyCalendarSystemException("%s does not implement %s", dao.getClass().getName(),
                 DeletableDomainObjectDao.class.getName());
         }
         return ((DeletableDomainObjectDao) dao);
     }
 
     @SuppressWarnings({ "unchecked" })
     private <T extends DomainObject> Class<T> findClass(String className) {
          try {
              return (Class<T>) Class.forName(className);
          } catch (ClassNotFoundException e) {
              throw new StudyCalendarError("Class " + className + " does not exist", e);
          }
     }
     @Required
     public void setUserActionDao(UserActionDao userActionDao) {
         this.userActionDao = userActionDao;
     }
 
     @Required
     public void setAuditEventDao(AuditEventDao auditEventDao) {
         this.auditEventDao = auditEventDao;
     }
 
     @Required
     public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
         this.applicationSecurityManager = applicationSecurityManager;
     }
 
     @Required
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 
     @Required
     public void setAmendmentDao(AmendmentDao amendmentDao) {
         this.amendmentDao = amendmentDao;
     }
 
     @Required
     public void setPopulationDao(PopulationDao populationDao) {
         this.populationDao = populationDao;
     }
 }
