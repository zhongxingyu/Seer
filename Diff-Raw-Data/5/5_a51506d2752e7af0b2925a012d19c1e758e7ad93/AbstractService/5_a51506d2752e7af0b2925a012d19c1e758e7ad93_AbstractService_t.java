 package net.pickapack.service;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
 import com.j256.ormlite.misc.TransactionManager;
 import com.j256.ormlite.stmt.DeleteBuilder;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.table.TableUtils;
 import net.pickapack.model.ModelElement;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 /**
  * @author Min Cai
  */
 public class AbstractService implements Service {
     private JdbcPooledConnectionSource connectionSource;
 
     /**
      * @param databaseUrl
      * @param dataClasses
      */
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
     }
 
     /**
      *
      */
     public void stop() {
         try {
             this.connectionSource.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @return
      */
     public <TItem extends ModelElement> List<TItem> getAllItems(Dao<TItem, Long> dao) {
         try {
             return dao.queryForAll();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param first
      * @param count
      * @return
      */
     public <TItem extends ModelElement> List<TItem> getAllItems(Dao<TItem, Long> dao, long first, long count) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().offset(first).limit(count).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @return
      */
     public <TItem extends ModelElement> long getNumAllItems(Dao<TItem, Long> dao) {
         try {
             return dao.countOf();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param <TItemDirectory>
      * @param dao
      * @param parent
      * @return
      */
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> List<TItem> getItemsByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("parentId", parent.getId()).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param <TItemDirectory>
      * @param dao
      * @param parent
      * @param first
      * @param count
      * @return
      */
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
 
     /**
      * @param <TItem>
      * @param <TItemDirectory>
      * @param dao
      * @param parent
      * @return
      */
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> long getNumItemsByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
            PreparedQuery<TItem> query = dao.queryBuilder().setCountOf(true).where().eq("parentId", parent.getId()).prepare();
             return dao.countOf(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param id
      * @return
      */
     public <TItem extends ModelElement> TItem getItemById(Dao<TItem, Long> dao, long id) {
         try {
             return dao.queryForId(id);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param title
      * @return
      */
     public <TItem extends ModelElement> TItem getFirstItemByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("title", title).prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param <TItemDirectory>
      * @param dao
      * @param parent
      * @return
      */
     public <TItem extends ModelElement, TItemDirectory extends ModelElement> TItem getFirstItemByParent(Dao<TItem, Long> dao, TItemDirectory parent) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("parentId", parent.getId()).and().prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param title
      * @return
      */
     public <TItem extends ModelElement> TItem getLatestItemByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().orderBy("createTime", false).where().eq("title", title).prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param title
      * @return
      */
     public <TItem extends ModelElement> List<TItem> getItemsByTitle(Dao<TItem, Long> dao, String title) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().where().eq("title", title).prepare();
             return dao.query(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param title
      * @param first
      * @param count
      * @return
      */
     public <TItem extends ModelElement> List<TItem> getItemsByTitle(Dao<TItem, Long> dao, String title, long first, long count) {
         try {
             QueryBuilder<TItem, Long> queryBuilder = dao.queryBuilder();
             queryBuilder.where().eq("title", title);
             queryBuilder.offset(first).limit(count);
             return dao.query(queryBuilder.prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @param title
      * @return
      */
     public <TItem extends ModelElement> long getNumItemsByTitle(Dao<TItem, Long> dao, String title) {
         try {
            PreparedQuery<TItem> query = dao.queryBuilder().setCountOf(true).where().eq("title", title).prepare();
             return dao.countOf(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <TItem>
      * @param dao
      * @return
      */
     public <TItem extends ModelElement> TItem getFirstItem(Dao<TItem, Long> dao) {
         try {
             PreparedQuery<TItem> query = dao.queryBuilder().prepare();
             return dao.queryForFirst(query);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      *
      * @param dao
      * @param item
      * @return
      */
     public <TItem extends ModelElement> long addItem(final Dao<TItem, Long> dao, final TItem item) {
         addItems(dao, new ArrayList<TItem>() {{
             add(item);
         }});
         return item.getId();
     }
 
     /**
      * @param dao
      * @param items
      */
     public <TItem extends ModelElement> void addItems(final Dao<TItem, Long> dao, final List<TItem> items) {
         try {
             TransactionManager.callInTransaction(getConnectionSource(),
                     new Callable<Void>() {
                         public Void call() throws Exception {
                             for (TItem item : items) {
                                 dao.create(item);
                             }
                             return null;
                         }
                     });
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param dao
      * @param id
      */
     public <TItem extends ModelElement> void removeItemById(Dao<TItem, Long> dao, final long id) {
         removeItemsByIds(dao, new ArrayList<Long>(){{
             add(id);
         }});
     }
 
     /**
      * @param dao
      * @param items
      */
     public <TItem extends ModelElement> void removeItems(Dao<TItem, Long> dao, final List<TItem> items) {
         removeItemsByIds(dao, new ArrayList<Long>(){{
             for (TItem item : items) {
                 add(item.getId());
             }
         }});
     }
 
     /**
      * @param dao
      * @param ids
      */
     public <TItem extends ModelElement> void removeItemsByIds(final Dao<TItem, Long> dao, final List<Long> ids) {
         try {
             DeleteBuilder<TItem,Long> deleteBuilder = dao.deleteBuilder();
             deleteBuilder.where().in("id", ids);
             dao.delete(deleteBuilder.prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param dao
      *
      */
     public <TItem extends ModelElement> void clearItems(Dao<TItem, Long> dao) {
         try {
             dao.delete(dao.deleteBuilder().prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param dao
      * @param item
      */
     public <TItem extends ModelElement> void updateItem(Dao<TItem, Long> dao, final TItem item) {
         updateItems(dao, new ArrayList<TItem>(){{
             add(item);
         }});
     }
 
     /**
      * @param dao
      * @param items
      */
     public <TItem extends ModelElement> void updateItems(final Dao<TItem, Long> dao, final List<TItem> items) {
         try {
             TransactionManager.callInTransaction(getConnectionSource(),
                     new Callable<Void>() {
                         public Void call() throws Exception {
                             for (TItem item : items) {
                                 dao.update(item);
                             }
 
                             return null;
                         }
                     });
 
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param <ModelElementT>
      * @param <D>
      * @param clz
      * @return
      */
     protected <ModelElementT extends ModelElement, D extends Dao<ModelElementT, Long>> D createDao(Class<ModelElementT> clz) {
         try {
             return DaoManager.createDao(this.connectionSource, clz);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @return
      */
     protected JdbcPooledConnectionSource getConnectionSource() {
         return connectionSource;
     }
 }
