 package Chapter.Seven;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.ProjectionList;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import Chapter.Seven.pojo.Product;
 import Chapter.Seven.pojo.Software;
 import Chapter.Seven.pojo.Supplier;
 import Chapter.Three.Cameron_Mckenzie.HibernateUtil;
 
 public class CriteriaExample {
   // *** Methods that use displayObjectList
 
   public void executeRowCountCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     criteria.setProjection(Projections.rowCount());
     displayObjectList(criteria.list());
     transaction.commit();
   }
 
   private void displayObjectList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No objects to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       Object obj = iterator.next();
       System.out.println(obj);
     }
   }
 
   // *** Methods that use displayObjectsList
 
   public void executeAggregatesCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     ProjectionList projectionList = Projections.projectionList();
     projectionList.add(Projections.max("price"));
     projectionList.add(Projections.min("price"));
     projectionList.add(Projections.avg("price"));
     projectionList.add(Projections.countDistinct("description"));
     criteria.setProjection(projectionList);
     displayObjectsList(criteria.list());
     transaction.commit();
   }
 
   public void executeProjectionCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     ProjectionList projectionList = Projections.projectionList();
     projectionList.add(Projections.property("name"));
     projectionList.add(Projections.property("description"));
     criteria.setProjection(projectionList);
     displayObjectsList(criteria.list());
     transaction.commit();
   }
 
   public void executeGroupByCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     ProjectionList projectionList = Projections.projectionList();
     projectionList.add(Projections.groupProperty("name"));
     projectionList.add(Projections.property("price"));
     criteria.setProjection(projectionList);
     displayObjectsList(criteria.list());
     transaction.commit();
   }
 
   private void displayObjectsList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No objects to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       System.out.println("New object");
       Object[] obj = (Object[]) iterator.next();
       for (int i = 0; i < obj.length; i++) {
         System.out.println(obj[i]);
       }
 
     }
   }
 
   // **** Methods that use displayProductsList
 
   // public void executeDistinctCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // crit.add(Restrictions.like("name", "K%"));
   // crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
   // List results = crit.list();
   // displayProductsList(results);
   // }
 
   // public void executeSimpleCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeEqualsCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.eq("name", "Mouse"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeNotEqualsCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.ne("name", "Mouse"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeLikePatternCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.like("name", "Mou%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeILikeMatchModeCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.ilike("name", "browser", MatchMode.END));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeNullCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.isNull("name"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeGreaterThanCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeAndCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // crit.add(Restrictions.like("name", "K%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeOrCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // LogicalExpression orExp = Restrictions.or(price, name);
   // crit.add(orExp);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeAndOrCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // LogicalExpression orExp = Restrictions.or(price, name);
   // crit.add(orExp);
   // crit.add(Restrictions.ilike("description", "blocks%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeDisjunctionCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // Criterion desc = Restrictions.ilike("description", "blocks%");
   // Disjunction disjunction = Restrictions.disjunction();
   // disjunction.add(price);
   // disjunction.add(name);
   // disjunction.add(desc);
   // crit.add(disjunction);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeSQLCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.sqlRestriction("{alias}.name like 'Mou%'"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executePagingCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.setFirstResult(1);
   // crit.setMaxResults(2);
   // List results = crit.list();
   // displayProductsList(results);
   // }

  public void executeUniqueResultCriteria() {
    Session session = HibernateUtil.getSession();
    Transaction transaction = session.beginTransaction();
    Criteria criteria = session.createCriteria(Product.class);
    Criterion price = Restrictions.gt("price", new Double(25.0));
    criteria.setMaxResults(1);
    Product product = (Product) criteria.uniqueResult();
    // test for null here if needed
    List results = new ArrayList();
    results.add(product);
    displayProductsList(results);
    transaction.commit();
  }
 
   public void executeUniqueResultExceptionCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     Product product = (Product) criteria.uniqueResult();
     List results = new ArrayList();
     results.add(product);
     displayProductsList(results);
     transaction.commit();
   }
 
   public void executeOrderCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     criteria.add(Restrictions.gt("price", new Double(25.0)));
     criteria.addOrder(Order.desc("price"));
     displayProductsList(criteria.list());
     transaction.commit();
   }
 
   public void executeManyToOneAssociationsCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     Criteria supplierCriteria = criteria.createCriteria("supplier");
     supplierCriteria.add(Restrictions.eq("name", "MegaInc"));
     displayProductsList(criteria.list());
     transaction.commit();
   }
 
   public void executeNotNullOrZeroQBECriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criteria criteria = session.createCriteria(Product.class);
     Product product = new Product();
     product.setName("Mouse");
     Example example = Example.create(product);
     example.excludeZeroes();
     criteria.add(example);
     displayProductsList(criteria.list());
     transaction.commit();
   }
 
   public void executeQBEAdvancedCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Product product = new Product();
     product.setName("M%");
 
     Example productExample = Example.create(product);
     productExample.excludeProperty("price");
     productExample.enableLike();
 
     Supplier supplier = new Supplier();
     supplier.setName("SuperCorp");
 
     Criteria productCriteria = session.createCriteria(Product.class);
     Criteria supplierCriteria = productCriteria.createCriteria("supplier");
     supplierCriteria.add(Example.create(supplier));
     productCriteria.add(productExample);
 
     displayProductsList(productCriteria.list());
     transaction.commit();
   }
 
   public void displayProductsList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No products to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       Product product = (Product) iterator.next();
 
       StringBuilder stringBuilder = new StringBuilder();
       stringBuilder.append(product.getSupplier().getName());
       stringBuilder.append("\t");
       stringBuilder.append(product.getName());
       stringBuilder.append("\t");
       stringBuilder.append(product.getPrice());
       stringBuilder.append("\t");
       stringBuilder.append(product.getDescription());
 
       System.out.println(stringBuilder.toString());
     }
   }
 
   // *** Methods that use displaySupplierList
 
   public void executeOneToManyAssociationsCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Criteria supplierCriteria = session.createCriteria(Supplier.class);
     Criteria productCriteria = supplierCriteria.createCriteria("products");
     productCriteria.add(Restrictions.gt("price", new Double(25.0)));
 
     displaySupplierList(supplierCriteria.list());
     transaction.commit();
   }
 
   public void executeAssociationsSortingCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Criteria supplierCriteria = session.createCriteria(Supplier.class);
     supplierCriteria.addOrder(Order.desc("name"));
 
     Criteria productCriteria = supplierCriteria.createCriteria("products");
     productCriteria.add(Restrictions.gt("price", new Double(25.0)));
 
     displaySupplierList(productCriteria.list());
     transaction.commit();
   }
 
   public void executeQBECriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Supplier supplier = new Supplier();
     supplier.setName("MegaInc");
 
     Criteria supplierCriteria = session.createCriteria(Supplier.class);
     supplierCriteria.add(Example.create(supplier));
 
     displaySupplierList(supplierCriteria.list());
     transaction.commit();
   }
 
   private void displaySupplierList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No suppliers to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       Supplier supplier = (Supplier) iterator.next();
       System.out.println(supplier.getName());
     }
   }
 
   // *** Populate the database
 
   public void populate() {
     Supplier supplier = null;
     Product product = null;
     Software software = null;
 
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     supplier = new Supplier();
     supplier.setName("SuperCorp");
 
     product = new Product();
     product.setName("Mouse");
     product.setDescription("Optical Wheel Mouse");
     product.setPrice(20.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     product = new Product();
     product.setName("Mouse");
     product.setDescription("One Button Mouse");
     product.setPrice(22.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     software = new Software();
     software.setName("Web Browser");
     software.setDescription("Blocks Pop-ups");
     software.setVersion("2.0");
     software.setSupplier(supplier);
     supplier.addProduct(software);
 
     session.save(supplier);
 
     supplier = new Supplier();
     supplier.setName("MegaInc");
 
     product = new Product();
     product.setName("Keyboard");
     product.setDescription("101 Keys");
     product.setPrice(30.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     software = new Software();
     software.setName("Email");
     software.setDescription("Blocks spam");
     software.setPrice(49.99);
     software.setVersion("4.1 RMX Edition");
     software.setSupplier(supplier);
 
     supplier.addProduct(software);
 
     session.save(supplier);
     transaction.commit();
   }
 
   public static void main(String args[]) {
     CriteriaExample example = new CriteriaExample();
 
     System.out.println("=== Populate Database ===");
     example.populate();
 
     // System.out.println("=== Execute One-To-Many Associations Criteria ===");
     // BUG: runs, but doesn't produce correct output
     // example.executeOneToManyAssociationsCriteria();
 
     // System.out.println("=== Execute Association Sorting Criteria ===");
     // BUG: runs, but doesn't produce correct output
     // example.executeAssociationsSortingCriteria();
 
     // System.out.println("=== Execute QBE Criteria ===");
     // BUG: runs, but doesn't produce correct output
     // example.executeQBECriteria();
 
     // System.out.println("=== Execute Row Count Criteria ===");
     // example.executeRowCountCriteria();
 
     // System.out.println("=== Execute Group By Criteria ===");
     // example.executeGroupByCriteria();
 
     // System.out.println("=== Execute Projection Criteria ===");
     // example.executeProjectionCriteria();
 
     // System.out.println("=== Execute Aggregate Criteria ===");
     // example.executeAggregatesCriteria();
 
     // -- Methods that use displayProductsList
 
     // System.out.println("=== Execute QBE Advanced Criteria ===");
     // example.executeQBEAdvancedCriteria();
 
     // System.out.println("=== Execute Not Null Or Zero QBE Criteria ===");
     // example.executeNotNullOrZeroQBECriteria();
 
     // System.out.println("=== Execute Many To One Association Criteria ===");
     // example.executeManyToOneAssociationsCriteria();
 
     // System.out.println("=== Execute Order Criteria ===");
     // example.executeOrderCriteria();
 
    // System.out.println("=== Execute Non-Unique Result Exception Criteria ===");
     // example.executeUniqueResultExceptionCriteria();
     // BUG: throws org.hibernate.NonUniqueResultException: query did not return a unique result

    System.out.println("=== Execute Unique Result Exception Criteria ===");
    example.executeUniqueResultCriteria();
   }
 }
