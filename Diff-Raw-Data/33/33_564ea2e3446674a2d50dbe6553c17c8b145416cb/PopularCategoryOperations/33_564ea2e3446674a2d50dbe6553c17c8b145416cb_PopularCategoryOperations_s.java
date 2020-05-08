 package edu.wustl.cab2b.server.category;
 
 import java.sql.Connection;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Session;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.common.category.CategoryPopularity;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.util.ConnectionUtil;
 import edu.wustl.common.hibernate.HibernateDatabaseOperations;
 import edu.wustl.common.hibernate.HibernateUtil;
 import edu.wustl.common.querysuite.metadata.category.Category;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 
 /**
  * @author hrishikesh_rajpathak
  *
  * Class for doing all the database operations related to popular category feature. 
  */
 public class PopularCategoryOperations {
 
     /**
      * This menthod returns list of objects of type CategoryPopularity. In other words, it returns all the entity ids with their respective 
      * popularity count that are present in database.
      * 
      * @return list of all the objects of CategoryPopularity class present in database
      */
     public Collection<CategoryPopularity> getPopularityForAllCategories() {
         Session session = HibernateUtil.newSession();
         Collection<CategoryPopularity> list = new HibernateDatabaseOperations<CategoryPopularity>(session).retrieve(CategoryPopularity.class.getName());
         return list;
     }
 
     /**
      * This method increments popularity of all the entities passed by one.
      * 
      * @param entities list of entities for which count is to be increased. 
      * @param object of HibernateDatabaseOperations
      * @param session
      */
     private void persistPopularity(Collection<EntityInterface> entities,
                                    HibernateDatabaseOperations<CategoryPopularity> hiberOp) {
 
         for (EntityInterface entityInterface : entities) {
             if (Utility.isCategory(entityInterface)) {
                 Connection connection = ConnectionUtil.getConnection();
                Category category = CategoryCache.getInstance(connection).getCategoryByEntityId(entityInterface.getId());
                 CategoryOperations categoryOperations = new CategoryOperations();
                 Collection<EntityInterface> entityCollection = categoryOperations.getAllSourceClasses(category);
                 for (EntityInterface en : entityCollection) {
                     persistPopularity(en, hiberOp);
                 }
             }
             persistPopularity(entityInterface, hiberOp);
         }
     }
 
     /**
      * Persists the given object of CategoryPopularity
      * 
      * @param entitiy
      * @param hiberOp
      */
     private synchronized void persistPopularity(EntityInterface entitiy,
                                                 HibernateDatabaseOperations<CategoryPopularity> hiberOp) {
         long entityid = entitiy.getId();
         CategoryPopularity categoryPopularity = getPopularity(entityid, hiberOp);
         hiberOp.insertOrUpdate(categoryPopularity);
     }
 
     /**
      * This menthod returns CategoryPopularity object for given entity. Before returning it increases its popularity by one
      * If entity is not present in the database, it returns CategoryPopularity object with popularity set to 1
      * 
      * @param entity id for which the popularity count is needed
      * @return popularity count
      */
     private CategoryPopularity getPopularity(long entityId, HibernateDatabaseOperations<CategoryPopularity> hiberOp) {
         List<CategoryPopularity> list = hiberOp.retrieve(CategoryPopularity.class.getName(), "entityId", entityId);
         CategoryPopularity categoryPopularity = null;
         if (!list.isEmpty()) {
             categoryPopularity = list.get(0);
         }
         if (categoryPopularity == null) {
             categoryPopularity = new CategoryPopularity();
             categoryPopularity.setEntityId(entityId);
             categoryPopularity.setPopularity(0);
         }
         categoryPopularity.incPopularity();
         return categoryPopularity;
     }
 
     /**
      * Method to increase popularity of all the entities involved in the query
      * 
      * @param query Instance of ICab2bQuery
      */
     public void setPopularity(ICab2bQuery query) {
         Session session = HibernateUtil.newSession();
         try {
             HibernateDatabaseOperations<CategoryPopularity> hiberOp = new HibernateDatabaseOperations<CategoryPopularity>(
                     session);
             Set<EntityInterface> entityList = new HashSet<EntityInterface>();
             IConstraints constraints = query.getConstraints();
             Iterator<IExpression> expression = constraints.iterator();
             while (expression.hasNext()) {
                 EntityInterface entityInterface = expression.next().getQueryEntity().getDynamicExtensionsEntity();
                 entityList.add(entityInterface);
             }
             persistPopularity(entityList, hiberOp);
         } finally {
             session.close();
         }
     }
 }
