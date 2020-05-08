 package net.pickapack.service;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.table.TableUtils;
 import net.pickapack.Pair;
 import net.pickapack.event.BlockingEventDispatcher;
 import net.pickapack.model.ModelElement;
 import net.pickapack.service.event.AfterItemsAddedEvent;
 import net.pickapack.service.event.AfterItemsUpdatedEvent;
 import net.pickapack.service.event.BeforeItemsRemovedEvent;
 import net.pickapack.service.event.ServiceEvent;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AbstractService implements Service {
     private JdbcPooledConnectionSource connectionSource;
     private BlockingEventDispatcher<ServiceEvent> blockingEventDispatcher;
 
     public AbstractService(String databaseUrl, List<Class<? extends ModelElement>> dataClasses) {
         try {
             this.connectionSource = new JdbcPooledConnectionSource(databaseUrl);
             this.connectionSource.setCheckConnectionsEveryMillis(0);
             this.connectionSource.setTestBeforeGet(true);
 
             for (Class<?> dataClz : dataClasses) {
                 TableUtils.createTableIfNotExists(this.connectionSource, dataClz);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
 
         this.blockingEventDispatcher = new BlockingEventDispatcher<ServiceEvent>();
     }
 
     public void stop() {
         try {
             this.connectionSource.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> List<TItem> getAllItems(Dao<TItem, Long> dao) {
         try {
             return dao.queryForAll();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> List<TItem> getAllItems(Dao<TItem, Long> dao, long first, long count) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().offset(first).limit(count).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> long getNumAllItems(Dao<TItem, Long> dao) {
         try {
             return dao.countOf();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> List<TItem> getItemsByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("parentId", parent.getId()).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> List<TItem> getItemsByParent(Dao<TItem, Long> dao, TItemDirectory parent, long first, long count) {
         try {
             QueryBuilder<TItem, Long> queryBuilder = dao.queryBuilder();
             queryBuilder.offset(first).limit(count);
             queryBuilder.where().eq("parentId", parent.getId());
             PreparedQuery<TItem> query = queryBuilder.prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> long getNumItemsByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("parentId", parent.getId()).prepare();
             return dao.countOf(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> TItem getItemById(Dao<TItem, Long> dao, long id) {
         try {
             return dao.queryForId(id);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> TItem getFirstItemByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("title", title).prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> TItem getFirstItemByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("parentId", parent.getId()).and().prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> TItem getLatestItemByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().orderBy("createTime", false).where().eq("title", title).prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> List<TItem> getItemsByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("title", title).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> List<TItem> getItemsByTitle(Dao<TItem, Long> dao, String title, long first, long count) {
         try {
             QueryBuilder<TItem,Long> queryBuilder = dao.queryBuilder();
             queryBuilder.where().eq("title", title);
             queryBuilder.offset(first).limit(count);
             return dao.query(queryBuilder.prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> long getNumItemsByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("title", title).prepare();
             return dao.countOf(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> TItem getFirstItem(Dao<TItem, Long> dao) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> long addItem(Dao<TItem, Long> dao, Class<TItem> clz, TItem item) {
         List<TItem> items = new ArrayList<TItem>();
         items.add(item);
         addItems(dao, clz, items);
         return item.getId();
     }
 
     public <TItem extends ModelElement> void addItems(Dao<TItem, Long> dao, Class<TItem> clz, List<TItem> items) {
         try {
             List<Long> ids = new ArrayList<Long>();
 
             for (TItem item : items) {
                 dao.create(item);
                 ids.add(item.getId());
             }
 
             fireAfterItemsAdded(clz, ids);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> void removeItemById(Dao<TItem, Long> dao, Class<TItem> clz, long id) {
         List<Long> ids = new ArrayList<Long>();
         ids.add(id);
         removeItemsByIds(dao, clz, ids);
     }
 
     public <TItem extends ModelElement> void removeItems(Dao<TItem, Long> dao, Class<TItem> clz, List<TItem> items) {
         List<Long> ids = new ArrayList<Long>();
         for (TItem item : items) {
             ids.add(item.getId());
         }
         removeItemsByIds(dao, clz, ids);
     }
 
     public <TItem extends ModelElement> void removeItemsByIds(Dao<TItem, Long> dao, Class<TItem> clz, List<Long> ids) {
         try {
             List<Pair<Long, Long>> idAndOldParentIds = new ArrayList<Pair<Long, Long>>();
 
             for (long id : ids) {
                 idAndOldParentIds.add(new Pair<Long, Long>(id, getItemById(dao, id).getParentId()));
             }
 
             fireBeforeItemsRemoved(clz, idAndOldParentIds);
 
             for (long id : ids) {
                 dao.deleteById(id);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> void clearItems(Dao<TItem, Long> dao, Class<TItem> clz) {
         try {
             List<Pair<Long, Long>> idAndOldParentIds = new ArrayList<Pair<Long, Long>>();
 
             for (TItem item : getAllItems(dao)) {
                 idAndOldParentIds.add(new Pair<Long, Long>(item.getId(), item.getParentId()));
             }
 
             fireBeforeItemsRemoved(clz, idAndOldParentIds);
 
             dao.delete(dao.deleteBuilder().prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <TItem extends ModelElement> void updateItem(Dao<TItem, Long> dao, Class<TItem> clz, TItem item) {
         List<TItem> items = new ArrayList<TItem>();
         items.add(item);
         updateItems(dao, clz, items);
     }
 
     public <TItem extends ModelElement> void updateItems(Dao<TItem, Long> dao, Class<TItem> clz, List<TItem> items) {
         try {
             List<Pair<Long, Long>> idAndOldParentIds = new ArrayList<Pair<Long, Long>>();
 
             for (TItem item : items) {
                 long oldParentId = getItemById(dao, item.getId()).getParentId();
                 dao.update(item);
                 idAndOldParentIds.add(new Pair<Long, Long>(item.getId(), oldParentId));
             }
 
             fireAfterItemsUpdated(clz, idAndOldParentIds);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void fireAfterItemsAdded(Class<?> clz, List<Long> itemIds) {
         this.blockingEventDispatcher.dispatch(new AfterItemsAddedEvent(clz, itemIds));
     }
 
     private void fireAfterItemsUpdated(Class<?> clz, List<Pair<Long, Long>> idAndOldParentIds) {
         this.blockingEventDispatcher.dispatch(new AfterItemsUpdatedEvent(clz, idAndOldParentIds));
     }
 
     private void fireBeforeItemsRemoved(Class<?> clz, List<Pair<Long, Long>> idAndOldParentIds) {
         this.blockingEventDispatcher.dispatch(new BeforeItemsRemovedEvent(clz, idAndOldParentIds));
     }
 
     @SuppressWarnings("unchecked")
    protected <ModelElementT extends ModelElement> Dao<ModelElementT, Long> createDao(Class<ModelElementT> clz) {
         try {
            return (Dao<ModelElementT, Long>) DaoManager.createDao(this.connectionSource, clz);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected JdbcPooledConnectionSource getConnectionSource() {
         return connectionSource;
     }
 
     public BlockingEventDispatcher<ServiceEvent> getBlockingEventDispatcher() {
         return blockingEventDispatcher;
     }
 }
