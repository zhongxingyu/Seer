 package de.hswt.hrm.catalog.service;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.catalog.dao.core.IActivityDao;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 import de.hswt.hrm.catalog.dao.core.ITargetDao;
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.exception.NotImplementedException;
 
 @Creatable
 public class CatalogService {
     private final static Logger LOG = LoggerFactory.getLogger(CatalogService.class);
     private final static String NO_ID_ERROR = "You cannot update an item if it does not have an ID.";
 
     private final IActivityDao activityDao;
     private final ICurrentDao currentDao;
     private final ITargetDao targetDao;
 
     @Inject
     public CatalogService(IActivityDao activityDao, ICurrentDao currentDao, ITargetDao targetDao) {
        checkNotNull(activityDao, "Activity DAO must be injected properly.");
        checkNotNull(currentDao, "Current DAO must be injected properly.");
        checkNotNull(targetDao, "Target DAO must be injected properly.");
 
         this.activityDao = activityDao;
         LOG.info("ActivityDao injected into CatalogService.");
         this.currentDao = currentDao;
         LOG.info("CurrentDao injected into CatalogService.");
         this.targetDao = targetDao;
         LOG.info("TargetDao injected into CatalogService.");
     }
 
     public Collection<ICatalogItem> findAllCatalogItem() throws DatabaseException {
         Collection<Activity> activities = activityDao.findAll();
         Collection<Current> currents = currentDao.findAll();
         Collection<Target> targets = targetDao.findAll();
 
         int itemsSize = activities.size() + currents.size() + targets.size();
         List<ICatalogItem> items = new ArrayList<>(itemsSize);
         items.addAll(activities);
         items.addAll(currents);
         items.addAll(targets);
 
         return items;
     }
 
     public Collection<Activity> findAllActivity() throws DatabaseException {
         return activityDao.findAll();
     }
 
     public Activity findActivityById(final int id) throws ElementNotFoundException,
             DatabaseException {
         return activityDao.findById(id);
     }
 
     public Collection<Current> findAllCurrent() throws DatabaseException {
         return currentDao.findAll();
     }
 
     public Current findCurrentById(final int id) throws ElementNotFoundException, DatabaseException {
         return currentDao.findById(id);
     }
 
     public Collection<Target> findAllTarget() throws DatabaseException {
         return targetDao.findAll();
     }
 
     public Target findTargetById(final int id) throws ElementNotFoundException, DatabaseException {
         return targetDao.findById(id);
     }
 
     public Activity insertActivity(Activity activity) throws SaveException {
         return activityDao.insert(activity);
     }
 
     public Current insertCurrent(Current current) throws SaveException {
         return currentDao.insert(current);
     }
 
     public Target insertTarget(Target target) throws SaveException {
         return targetDao.insert(target);
     }
 
     public void updateActivity(Activity activity) throws ElementNotFoundException, SaveException {
         activityDao.update(activity);
     }
 
     public void updateCurrent(Current current) throws ElementNotFoundException, SaveException {
         currentDao.update(current);
     }
 
     public void updateTarget(Target target) throws ElementNotFoundException, SaveException {
         targetDao.update(target);
     }
 
     public Collection<Catalog> findAllCatalog() {
         throw new NotImplementedException();
     }
 
     public Catalog findCatalogById(final int id) {
         throw new NotImplementedException();
     }
 
     public Catalog insertCatalog(final Catalog catalog) {
         throw new NotImplementedException();
     }
 
     public void updateCatlaog(final Catalog catalog) {
         throw new NotImplementedException();
     }
 
     /**
      * 
      * 
      * @param activity
      * @throws ElementNotFoundException
      * @throws DatabaseException
      * @throws IllegalStateException
      *             If activity does not have a valid id.
      */
     public void refresh(Activity activity) throws ElementNotFoundException, DatabaseException {
         checkState(activity.getId() >= 0, NO_ID_ERROR);
 
         Activity fromDb = findActivityById(activity.getId());
 
         activity.setName(fromDb.getName());
         activity.setText(fromDb.getText());
     }
 
     /**
      * 
      * @param current
      * @throws ElementNotFoundException
      * @throws DatabaseException
      * @throws IllegalStateException
      *             If current does not have a valid id.
      */
     public void refresh(Current current) throws ElementNotFoundException, DatabaseException {
         checkState(current.getId() >= 0, NO_ID_ERROR);
 
         Current fromDb = findCurrentById(current.getId());
 
         current.setName(fromDb.getName());
         current.setText(fromDb.getText());
     }
 
     /**
      * 
      * @param target
      * @throws ElementNotFoundException
      * @throws DatabaseException
      * @throws IllegalStateException
      *             If target does not have a valid id.
      */
     public void refresh(Target target) throws ElementNotFoundException, DatabaseException {
         checkState(target.getId() >= 0, NO_ID_ERROR);
 
         Target fromDb = findTargetById(target.getId());
 
         target.setName(fromDb.getName());
         target.setText(fromDb.getText());
     }
 
     public void refresh(Catalog catalog) {
 
         throw new NotImplementedException();
     }
 }
