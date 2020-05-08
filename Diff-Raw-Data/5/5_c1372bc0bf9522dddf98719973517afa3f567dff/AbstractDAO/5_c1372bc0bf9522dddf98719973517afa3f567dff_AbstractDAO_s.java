 package dit126.group4.group4shop.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.TypedQuery;
 
 /**
  * Contains CRUD operations for ProductCatalogue, OrderBook, UserRegister
  *
  * @author David
  */
 public abstract class AbstractDAO<T, K> implements IDAO<T, K>{
     
     protected EntityManagerFactory emf;
     private final Class<T> clazz;
     protected AbstractDAO(Class <T> clazz, String puName){
         this.clazz = clazz;
         emf = Persistence.createEntityManagerFactory(puName);
     }
     
     
     @Override
     public void add(T t) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction trans = em.getTransaction();
             try{
                 trans.begin();
                 em.persist(t);
                 trans.commit();
             } finally{
                 if(trans.isActive())
                     trans.rollback();
             }
         }finally{
             em.close();
         }
     }
     
     @Override
     public void remove(K id) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction trans = em.getTransaction();
             try{
                 trans.begin();
                 T t = find(id);
                 if(t != null){
                     T toBeRemoved = em.merge(t);
                     em.remove(toBeRemoved);
                     trans.commit();
                 }
             }finally{
                 if(trans.isActive())
                     trans.rollback();
             }
         }finally{
             em.close();
         }
     }
     
     @Override
     public void update(T t) {
         EntityManager em = emf.createEntityManager();
         try{
             EntityTransaction trans = em.getTransaction();
             try{
                 trans.begin();
                 em.merge(t);
                 trans.commit();
             }finally{
                 if(trans.isActive())
                     trans.rollback();
             }
         }finally{
             em.close();
         }
     }
     
     @Override
     public T find(K id) {
         EntityManager em = emf.createEntityManager();
        T t = em.find(clazz, id);
        return t;
     }
     
     @Override
     public List<T> getRange(int first, int nItems) {
         EntityManager em = emf.createEntityManager();
         TypedQuery query = em.createQuery("SELECT p FROM "+ clazz.getSimpleName() + " p", clazz);
         List<T> result = query.getResultList();
         return result.subList(first, nItems);
         /*List<T> list = new ArrayList<>();
         int i = first;
         while (i < (first+nItems) && i < result.size()){
             list.add(result.get(i));
             i++;
         }
         return list;*/
     }
     
     @Override
     public int getCount() {
         EntityManager em = emf.createEntityManager();
         TypedQuery query = em.createQuery("SELECT COUNT(p) FROM " + clazz.getSimpleName() + " p", clazz);
         int count = ((Long) query.getSingleResult()).intValue();
         return count;
     }
 }
