 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 /**
  *
  * @author Christian
  */
 public class ProductCatalogue implements IProductCatalogue{
     
     private EntityManagerFactory emf;
     private final Class<Product> clazz;
     
     public ProductCatalogue(String puName){
         this.emf = Persistence.createEntityManagerFactory(puName);
         this.clazz = Product.class;
     }
     
 
     @Override
     public void add(Product product) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction t = em.getTransaction();
             try{
                 t.begin();
                 em.persist(product);
                 t.commit();
             } finally{
                 if(t.isActive())
                     t.rollback();
             }
         }finally{
             em.close();
         }
     }
     
     @Override
     public void remove(Long id) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction t = em.getTransaction();
             try{
                 t.begin();
                 Product product = find(id);
                 if(product != null){
                     Product toBeRemoved = em.merge(product);
                     em.remove(toBeRemoved);
                     t.commit();
                 }
             }finally{
                 if(t.isActive())
                     t.rollback();
             }
         }finally{
             em.close();
         }
     }
 
     @Override
     public void update(Product product) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction t = em.getTransaction();
             try{
                 t.begin();
                 em.merge(product);
                 t.commit();
             }finally{
                 if(t.isActive())
                     t.rollback();
             }
         }finally{
             em.close();
         }
     }
 
     @Override
     public Product find(Long id) {
         EntityManager em = emf.createEntityManager();
         Product product = em.find(clazz, id);
         return product;
     }
 
     @Override
     public List<Product> getRange(int first, int nItems) {
         EntityManager em = emf.createEntityManager();
         Query query = em.createQuery("SELECT p FROM "+ clazz.getSimpleName().toUpperCase() + " p");
         List<Product> list = query.getResultList();
         return list.subList(first, nItems);
     }
 
     @Override
     public int getCount() {
         EntityManager em = emf.createEntityManager();
         Query query = em.createQuery("SELECT COUNT(p) FROM " + clazz.getSimpleName().toUpperCase() + " p");
        int count = ((Long) query.getSingleResult()).intValue();
         return count;
     }
     
 }
