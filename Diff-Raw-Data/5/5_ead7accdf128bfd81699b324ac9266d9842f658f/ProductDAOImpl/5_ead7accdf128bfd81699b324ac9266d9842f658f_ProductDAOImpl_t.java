 package it.bp.noticeboard.dao;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.SQLQuery;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.classic.Session;
 import it.bp.noticeboard.model.EcoQuery;
 import org.springframework.stereotype.Component;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import it.bp.noticeboard.model.Product;
 
 /**
  * @author renan
  * @since Apr 27, 2009 2:57:13 PM
  */
 @Component("productDAO")
 public class ProductDAOImpl implements ProductDAO {
     private static Logger logger = Logger.getLogger(ProductDAOImpl.class);
 
     SessionFactory sessionFactory;
 
     @Autowired
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     public long countProducts(EcoQuery ecoQuery) {
         Session session = sessionFactory.getCurrentSession();
 
         logger.info("Counting items for pagination");
         String productsQuery = "select * from noticeboard where " + ecoQuery.getWhereClause();
         logger.info("Query to retrieve count products: " + productsQuery);
         SQLQuery query = session.createSQLQuery(productsQuery);
         long count = query.list().size();
         return count;
 
     }
 
     public List selectProducts(EcoQuery ecoQuery) {
         Session session = sessionFactory.getCurrentSession();
 
         logger.info("Counting items for pagination");
        String productsQuery = "select * from noticeboard where " + ecoQuery.getWhereClause() + " order by catalog_desc, start_date, product_name";
         logger.info("Query to retrieve count products: " + productsQuery);
         SQLQuery query = session.createSQLQuery(productsQuery);
         query.addEntity(Product.class);
 
         return query.list();
     }
 
     public Map selectProducts(int lastColumn, int pageSize, EcoQuery ecoQuery, long totalItems) {
         Session session = sessionFactory.getCurrentSession();
         logger.info("Query to retrieve title: " + ecoQuery.getSqlStatement());
         SQLQuery query = session.createSQLQuery(ecoQuery.getSqlStatement());
 
         String title = (String) query.list().get(0);
 
        String productsQuery = "select * from noticeboard where " + ecoQuery.getWhereClause() + " order by catalog_desc, start_date, product_name";
         logger.info("Query to retrieve products: " + productsQuery);
 
 
         query = session.createSQLQuery(productsQuery);
         query.addEntity(Product.class);
         query.setMaxResults(pageSize);
         query.setFirstResult(lastColumn);
 
         List products = query.list();
 
         Map map = new HashMap();
 
         map.put("title", title);
         int nextColumn = lastColumn + pageSize;
 
         if (nextColumn >= totalItems) {
             nextColumn = 0;
         }
         map.put("lastColumn", nextColumn);
         map.put("products", products);
 
         return map;
     }
 
     public List<Product> listAllOrdered() {
         org.hibernate.Session session = sessionFactory.getCurrentSession();
         Criteria criteria = session.createCriteria(Product.class);
         criteria.addOrder(Order.asc("id"));
         return criteria.list();
     }
 
     public List<Product> listCatalogOrdered(Long catalogId) {
         org.hibernate.Session session = sessionFactory.getCurrentSession();
         Criteria criteria = session.createCriteria(Product.class);
         criteria.add(Restrictions.eq("catalogId", catalogId));
         criteria.addOrder(Order.asc("id"));
         return criteria.list();
     }
 
     public List<Long> listCatalogIds() {
         org.hibernate.Session session = sessionFactory.getCurrentSession();
         Query query = session.createQuery("select distinct catalogId from it.bp.noticeboard.model.Product");
         return query.list();
     }
 
 }
