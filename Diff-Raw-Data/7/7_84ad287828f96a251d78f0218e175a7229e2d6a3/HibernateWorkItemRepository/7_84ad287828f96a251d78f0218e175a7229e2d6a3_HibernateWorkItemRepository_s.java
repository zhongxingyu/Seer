 package de.objectcode.time4u.client.store.impl.hibernate;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import de.objectcode.time4u.client.store.api.IWorkItemRepository;
 import de.objectcode.time4u.client.store.api.RepositoryException;
 import de.objectcode.time4u.client.store.api.event.ActiveWorkItemRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.DayInfoRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.TimePolicyRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.WorkItemRepositoryEvent;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.DayInfo;
 import de.objectcode.time4u.server.api.data.DayInfoSummary;
 import de.objectcode.time4u.server.api.data.DayTag;
 import de.objectcode.time4u.server.api.data.EntityType;
 import de.objectcode.time4u.server.api.data.TimePolicy;
 import de.objectcode.time4u.server.api.data.WeekTimePolicy;
 import de.objectcode.time4u.server.api.data.WorkItem;
 import de.objectcode.time4u.server.api.filter.DayInfoFilter;
 import de.objectcode.time4u.server.api.filter.TimePolicyFilter;
 import de.objectcode.time4u.server.entities.ActiveWorkItemEntity;
 import de.objectcode.time4u.server.entities.DayInfoEntity;
 import de.objectcode.time4u.server.entities.DayTagEntity;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.TimePolicyEntity;
 import de.objectcode.time4u.server.entities.WeekTimePolicyEntity;
 import de.objectcode.time4u.server.entities.WorkItemEntity;
 import de.objectcode.time4u.server.entities.context.SessionPersistenceContext;
 import de.objectcode.time4u.server.entities.revision.IRevisionGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 import de.objectcode.time4u.server.entities.revision.SessionRevisionGenerator;
 
 public class HibernateWorkItemRepository implements IWorkItemRepository
 {
   private final HibernateRepository m_repository;
   private final HibernateTemplate m_hibernateTemplate;
 
   HibernateWorkItemRepository(final HibernateRepository repository, final HibernateTemplate hibernateTemplate)
   {
     m_repository = repository;
     m_hibernateTemplate = hibernateTemplate;
   }
 
   /**
    * {@inheritDoc}
    */
   public DayInfo getDayInfo(final CalendarDay day) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<DayInfo>() {
       public DayInfo perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         criteria.add(Restrictions.eq("date", day.getDate()));
 
         final DayInfoEntity entity = (DayInfoEntity) criteria.uniqueResult();
 
         if (entity == null) {
           return null;
         }
 
         final DayInfo dayInfo = new DayInfo();
 
         entity.toDTO(dayInfo);
 
         return dayInfo;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<DayInfoSummary> getDayInfoSummaries(final DayInfoFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<List<DayInfoSummary>>() {
       public List<DayInfoSummary> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         if (filter.getFrom() != null) {
           criteria.add(Restrictions.ge("date", filter.getFrom().getDate()));
         }
         if (filter.getTo() != null) {
           criteria.add(Restrictions.lt("date", filter.getTo().getDate()));
         }
         if (filter.getMinRevision() != null) {
           criteria.add(Restrictions.ge("revision", filter.getMinRevision()));
         }
         if (filter.getMaxRevision() != null) {
           criteria.add(Restrictions.le("revision", filter.getMaxRevision()));
         }
         if (filter.getLastModifiedByClient() != null) {
           criteria.add(Restrictions.eq("lastModifiedByClient", filter.getLastModifiedByClient()));
         }
         criteria.addOrder(Order.asc("date"));
 
         final List<?> result = criteria.list();
         final List<DayInfoSummary> dayInfos = new ArrayList<DayInfoSummary>();
 
         for (final Object row : result) {
           final DayInfoEntity entity = (DayInfoEntity) row;
           final DayInfoSummary dayInfo = new DayInfoSummary();
 
           entity.toSummaryDTO(dayInfo);
           dayInfos.add(dayInfo);
         }
 
         return dayInfos;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public List<DayInfo> getDayInfos(final DayInfoFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<List<DayInfo>>() {
       public List<DayInfo> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayInfoEntity.class);
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         if (filter.getFrom() != null) {
           criteria.add(Restrictions.ge("date", filter.getFrom().getDate()));
         }
         if (filter.getTo() != null) {
           criteria.add(Restrictions.lt("date", filter.getTo().getDate()));
         }
         if (filter.getMinRevision() != null) {
           criteria.add(Restrictions.ge("revision", filter.getMinRevision()));
         }
         if (filter.getMaxRevision() != null) {
           criteria.add(Restrictions.le("revision", filter.getMaxRevision()));
         }
         if (filter.getLastModifiedByClient() != null) {
           criteria.add(Restrictions.eq("lastModifiedByClient", filter.getLastModifiedByClient()));
         }
         criteria.addOrder(Order.asc("date"));
 
         final List<?> result = criteria.list();
         final List<DayInfo> dayInfos = new ArrayList<DayInfo>();
 
         for (final Object row : result) {
           final DayInfoEntity entity = (DayInfoEntity) row;
           final DayInfo dayInfo = new DayInfo();
 
           entity.toDTO(dayInfo);
           dayInfos.add(dayInfo);
         }
 
         return dayInfos;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public void storeDayInfo(final DayInfo dayInfo, final boolean modifiedByOwner) throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation() {
       public void perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.DAYINFO, m_repository
             .getOwner().getId());
 
         DayInfoEntity dayInfoEntity = null;
 
         if (dayInfo.getId() == null) {
           dayInfo.setId(m_repository.generateLocalId(EntityType.DAYINFO));
         } else {
           dayInfoEntity = (DayInfoEntity) session.get(DayInfoEntity.class, dayInfo.getId());
         }
 
         if (dayInfoEntity == null) {
           dayInfoEntity = new DayInfoEntity(dayInfo.getId(), revisionLock.getLatestRevision(), dayInfo
               .getLastModifiedByClient(), (PersonEntity) session.get(PersonEntity.class, m_repository.getOwner()
               .getId()), dayInfo.getDay().getDate());
 
           session.persist(dayInfoEntity);
         }
         dayInfoEntity.fromDTO(new SessionPersistenceContext(session), dayInfo);
         dayInfoEntity.setRevision(revisionLock.getLatestRevision());
         if (modifiedByOwner) {
           dayInfoEntity.setLastModifiedByClient(m_repository.getClientId());
         }
         dayInfoEntity.validate();
 
         session.flush();
 
         dayInfoEntity.toDTO(dayInfo);
       }
     });
 
     m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(dayInfo));
   }
 
   /**
    * {@inheritDoc}
    */
   public void storeWorkItem(final WorkItem workItem) throws RepositoryException
   {
     final DayInfo dayInfo = m_hibernateTemplate
         .executeInTransaction(new HibernateTemplate.OperationWithResult<DayInfo>() {
           public DayInfo perform(final Session session)
           {
             final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
             final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.DAYINFO, m_repository
                 .getOwner().getId());
 
             WorkItemEntity workItemEntity;
 
             if (workItem.getId() != null) {
               workItemEntity = (WorkItemEntity) session.get(WorkItemEntity.class, workItem.getId());
 
               workItemEntity.getDayInfo().setRevision(revisionLock.getLatestRevision());
 
               workItemEntity.fromDTO(new SessionPersistenceContext(session), workItem, m_repository.getClientId());
               workItemEntity.getDayInfo().validate();
               session.flush();
             } else {
               final Criteria criteria = session.createCriteria(DayInfoEntity.class);
               criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
               criteria.add(Restrictions.eq("date", workItem.getDay().getDate()));
 
               DayInfoEntity dayInfoEntity = (DayInfoEntity) criteria.uniqueResult();
 
               if (dayInfoEntity == null) {
                 // This is save because the revision counter locks this section
                 final String dayInfoId = m_repository.generateLocalId(EntityType.DAYINFO);
 
                 dayInfoEntity = new DayInfoEntity(dayInfoId, revisionLock.getLatestRevision(), m_repository
                     .getClientId(), (PersonEntity) session.get(PersonEntity.class, m_repository.getOwner().getId()),
                     workItem.getDay().getDate());
 
                 session.persist(dayInfoEntity);
               }
               final String workItemId = m_repository.generateLocalId(EntityType.WORKITEM);
               workItemEntity = new WorkItemEntity(workItemId, dayInfoEntity);
 
               workItemEntity.fromDTO(new SessionPersistenceContext(session), workItem, m_repository.getClientId());
 
               session.persist(workItemEntity);
               dayInfoEntity.getWorkItems().put(workItemEntity.getId(), workItemEntity);
               dayInfoEntity.validate();
               session.flush();
             }
 
             final DayInfo dayInfo = new DayInfo();
 
             workItemEntity.getDayInfo().toDTO(dayInfo);
             workItemEntity.toDTO(workItem);
 
             return dayInfo;
           }
         });
 
     m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(dayInfo));
     m_repository.fireRepositoryEvent(new WorkItemRepositoryEvent(dayInfo.getWorkItems()));
   }
 
   /**
    * {@inheritDoc}
    */
   public void deleteWorkItem(final WorkItem workItem) throws RepositoryException
   {
     if (workItem.getId() == null) {
       return;
     }
     final DayInfo dayInfo = m_hibernateTemplate
         .executeInTransaction(new HibernateTemplate.OperationWithResult<DayInfo>() {
           public DayInfo perform(final Session session)
           {
             final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
             final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.DAYINFO, m_repository
                 .getOwner().getId());
 
             if (workItem.getId() != null) {
               final WorkItemEntity workItemEntity = (WorkItemEntity) session
                   .get(WorkItemEntity.class, workItem.getId());
               final DayInfoEntity dayInfoEntity = workItemEntity.getDayInfo();
 
               dayInfoEntity.setRevision(revisionLock.getLatestRevision());
 
               // Workaround to enforce lazy initialization at this point
               dayInfoEntity.getWorkItems().size();
               dayInfoEntity.getWorkItems().remove(workItemEntity.getId());
               session.delete(workItemEntity);
 
               workItemEntity.getDayInfo().validate();
               session.flush();
 
               final DayInfo dayInfo = new DayInfo();
 
               workItemEntity.getDayInfo().toDTO(dayInfo);
 
               return dayInfo;
             }
             return null;
           }
         });
 
     if (dayInfo != null) {
       m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(dayInfo));
       m_repository.fireRepositoryEvent(new WorkItemRepositoryEvent(dayInfo.getWorkItems()));
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public WorkItem getActiveWorkItem() throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<WorkItem>() {
       public WorkItem perform(final Session session)
       {
         final ActiveWorkItemEntity activeWorkItemEntity = (ActiveWorkItemEntity) session.get(
             ActiveWorkItemEntity.class, m_repository.getOwner().getId());
 
         if (activeWorkItemEntity != null && activeWorkItemEntity.getWorkItem() != null) {
           final WorkItem workItem = new WorkItem();
 
           activeWorkItemEntity.getWorkItem().toDTO(workItem);
 
           return workItem;
         }
         return null;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public void setActiveWorkItem(final WorkItem workItem) throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<Object>() {
       public Object perform(final Session session)
       {
         WorkItemEntity workItemEntity = null;
 
         if (workItem != null) {
           workItemEntity = (WorkItemEntity) session.get(WorkItemEntity.class, workItem.getId());
 
           if (workItemEntity == null) {
             throw new RuntimeException("WorkItem " + workItem.getId() + " not found");
           }
         }
 
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
 
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.ACTIVE_WORKITEM, m_repository
             .getOwner().getId());
 
         ActiveWorkItemEntity activeWorkItemEntity = (ActiveWorkItemEntity) session.get(ActiveWorkItemEntity.class,
             m_repository.getOwner().getId());
 
         if (activeWorkItemEntity == null) {
           // This is save because the revision counter locks this section
           activeWorkItemEntity = new ActiveWorkItemEntity(revisionLock.getLatestRevision(), (PersonEntity) session.get(
               PersonEntity.class, m_repository.getOwner().getId()), workItemEntity);
 
           session.persist(activeWorkItemEntity);
         } else {
           activeWorkItemEntity.setRevision(revisionLock.getLatestRevision());
           activeWorkItemEntity.setLastModifiedByClient(m_repository.getClientId());
           activeWorkItemEntity.setWorkItem(workItemEntity);
         }
         session.flush();
 
         return null;
       }
     });
 
     m_repository.fireRepositoryEvent(new ActiveWorkItemRepositoryEvent(workItem));
   }
 
   public void setRegularTime(final CalendarDay fromDay, final CalendarDay untilDay, final Integer regularTime,
       final Set<String> tags) throws RepositoryException
   {
     final List<DayInfoSummary> result = m_hibernateTemplate
         .executeInTransaction(new HibernateTemplate.OperationWithResult<List<DayInfoSummary>>() {
           public List<DayInfoSummary> perform(final Session session)
           {
             final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
             final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.DAYINFO, m_repository
                 .getOwner().getId());
 
             final Calendar from = fromDay.getCalendar();
             final Calendar until = untilDay.getCalendar();
 
             final List<DayInfoSummary> result = new ArrayList<DayInfoSummary>();
             while (from.compareTo(until) <= 0) {
               final Criteria criteria = session.createCriteria(DayInfoEntity.class);
               criteria.add(Restrictions.eq("date", new Date(from.getTimeInMillis())));
 
               DayInfoEntity dayInfoEntity = (DayInfoEntity) criteria.uniqueResult();
 
               if (dayInfoEntity == null) {
                 dayInfoEntity = new DayInfoEntity(m_repository.generateLocalId(EntityType.DAYINFO), revisionLock
                     .getLatestRevision(), m_repository.getClientId(), (PersonEntity) session.get(PersonEntity.class,
                     m_repository.getOwner().getId()), new Date(from.getTimeInMillis()));
 
                 if (regularTime != null) {
                   dayInfoEntity.setRegularTime(regularTime);
                 }
                 if (tags != null) {
                   final Set<DayTagEntity> dayTags = new HashSet<DayTagEntity>();
 
                   for (final String tag : tags) {
                     final DayTagEntity dayTag = (DayTagEntity) session.get(DayTagEntity.class, tag);
 
                     if (dayTag != null) {
                       dayTags.add(dayTag);
                     }
                   }
 
                   dayInfoEntity.setTags(dayTags);
                   dayInfoEntity.setHasTags(!dayTags.isEmpty());
                 }
                 session.persist(dayInfoEntity);
               } else {
                 dayInfoEntity.setRevision(revisionLock.getLatestRevision());
                 dayInfoEntity.setLastModifiedByClient(m_repository.getClientId());
                 if (regularTime != null) {
                   dayInfoEntity.setRegularTime(regularTime);
                 }
                 if (tags != null) {
                   final Iterator<DayTagEntity> it = dayInfoEntity.getTags().iterator();
 
                   while (it.hasNext()) {
                     if (!tags.contains(it.next().getName())) {
                       it.remove();
                     }
                   }
                   for (final String tag : tags) {
                     final DayTagEntity dayTag = (DayTagEntity) session.get(DayTagEntity.class, tag);
 
                     if (dayTag != null) {
                       dayInfoEntity.getTags().add(dayTag);
                     }
                   }
                   dayInfoEntity.setHasTags(!dayInfoEntity.getTags().isEmpty());
                 }
               }
 
               final DayInfoSummary dayInfo = new DayInfoSummary();
 
               dayInfoEntity.toSummaryDTO(dayInfo);
               result.add(dayInfo);
 
               from.add(Calendar.DAY_OF_MONTH, 1);
             }
             session.flush();
 
             return result;
           }
         });
 
     m_repository.fireRepositoryEvent(new DayInfoRepositoryEvent(result));
   }
 
   /**
    * {@inheritDoc}
    */
   public List<TimePolicy> getTimePolicies(final TimePolicyFilter filter) throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<List<TimePolicy>>() {
       public List<TimePolicy> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(TimePolicyEntity.class);
 
         criteria.add(Restrictions.eq("person.id", m_repository.getOwner().getId()));
         if (filter.getDeleted() != null) {
           criteria.add(Restrictions.eq("deleted", filter.getDeleted()));
         }
         if (filter.getMinRevision() != null) {
           criteria.add(Restrictions.ge("revision", filter.getMinRevision()));
         }
         if (filter.getMaxRevision() != null) {
           criteria.add(Restrictions.le("revision", filter.getMaxRevision()));
         }
         if (filter.getLastModifiedByClient() != null) {
           criteria.add(Restrictions.eq("lastModifiedByClient", filter.getLastModifiedByClient()));
         }
 
         criteria.addOrder(Order.asc("id"));
 
         final List<TimePolicy> result = new ArrayList<TimePolicy>();
 
         for (final Object row : criteria.list()) {
           if (row instanceof WeekTimePolicyEntity) {
             final WeekTimePolicy timePolicy = new WeekTimePolicy();
 
             ((WeekTimePolicyEntity) row).toDTO(timePolicy);
 
             result.add(timePolicy);
           }
         }
 
         return result;
       }
 
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public void storeTimePolicy(final TimePolicy timePolicy, final boolean modifiedByOwner) throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation() {
       public void perform(final Session session)
       {
         final IRevisionGenerator revisionGenerator = new SessionRevisionGenerator(session);
         final IRevisionLock revisionLock = revisionGenerator.getNextRevision(EntityType.TIMEPOLICY, m_repository
             .getOwner().getId());
 
         if (timePolicy.getId() == null) {
           timePolicy.setId(m_repository.generateLocalId(EntityType.TIMEPOLICY));
         }
 
         if (timePolicy instanceof WeekTimePolicy) {
           final WeekTimePolicyEntity timePolicyEntity = new WeekTimePolicyEntity(timePolicy.getId(), revisionLock
               .getLatestRevision(), m_repository.getClientId(), (PersonEntity) session.get(PersonEntity.class,
               m_repository.getOwner().getId()));
 
           timePolicyEntity.fromDTO((WeekTimePolicy) timePolicy);
           if (modifiedByOwner) {
             timePolicyEntity.setLastModifiedByClient(m_repository.getClientId());
           }
           session.merge(timePolicyEntity);
           session.flush();
 
           timePolicyEntity.toDTO((WeekTimePolicy) timePolicy);
         }
       }
     });
 
     m_repository.fireRepositoryEvent(new TimePolicyRepositoryEvent(timePolicy));
   }
 
   /**
    * {@inheritDoc}
    */
   public List<DayTag> getDayTags() throws RepositoryException
   {
     return m_hibernateTemplate.executeInTransaction(new HibernateTemplate.OperationWithResult<List<DayTag>>() {
       public List<DayTag> perform(final Session session)
       {
         final Criteria criteria = session.createCriteria(DayTagEntity.class);
         criteria.add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)));
         criteria.addOrder(Order.asc("name"));
 
         final List<DayTag> result = new ArrayList<DayTag>();
         for (final Object row : criteria.list()) {
           final DayTag dayTag = new DayTag();
 
           ((DayTagEntity) row).toDTO(dayTag);
 
           result.add(dayTag);
         }
 
         return result;
       }
     });
   }
 
   /**
    * {@inheritDoc}
    */
   public void storeDayTags(final List<DayTag> dayTags) throws RepositoryException
   {
     m_hibernateTemplate.executeInTransaction(new HibernateTemplate.Operation() {
       public void perform(final Session session)
       {
         final Set<String> names = new HashSet<String>();
 
         for (final DayTag dayTag : dayTags) {
           names.add(dayTag.getName());
 
           final DayTagEntity entity = new DayTagEntity();
           entity.fromDTO(dayTag);
           session.merge(entity);
         }
 
         final Criteria criteria = session.createCriteria(DayTagEntity.class);
 
         for (final Object row : criteria.list()) {
           final DayTagEntity entity = (DayTagEntity) row;
 
           if (!names.contains(entity.getName())) {
             entity.setDeleted(true);
           }
         }
       }
     });
   }
 }
