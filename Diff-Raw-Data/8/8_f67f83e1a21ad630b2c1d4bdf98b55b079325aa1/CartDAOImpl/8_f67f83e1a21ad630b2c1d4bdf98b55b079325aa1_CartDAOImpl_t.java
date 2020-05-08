 package cz.muni.fi.bapr.dao.hibernate;
 
 import cz.muni.fi.bapr.dao.CartDAO;
 import cz.muni.fi.bapr.entity.Cart;
 import cz.muni.fi.bapr.entity.Customer;
 import cz.muni.fi.bapr.entity.Product;
 import cz.muni.fi.bapr.util.CartStats;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * @author Andrej Kuročenko <andrej@kurochenko.net>
  */
 @Repository
 public class CartDAOImpl extends AbstractDAOImpl<Cart> implements CartDAO {
 
     @PersistenceContext
     private EntityManager entityManager;
 
 
     public CartDAOImpl() {
         super(Cart.class);
     }
 
     @Override
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 
     @Override
     public List<Cart> findByCustomer(Customer customer) {
         return super.findListByParam("customer", customer);
     }
 
     @Override
     public Cart matchCustomerProduct(Customer customer, Product product) {
         Cart result = null;
 
         try {
             Query query = getEntityManager().createQuery("from Cart c where c.customer = :customer and c.product = :product ");
             query.setParameter("customer", customer);
             query.setParameter("product", product);
             result = (Cart) query.getSingleResult();
         } catch (NoResultException e) {
         }
         return result;
     }
 
     @Override
     public CartStats sumStats(Customer customer) {
         CartStats result = null;
 
         try {
             Query query = getEntityManager().createQuery("select sum(c.amount), sum(c.amount * c.product.price), sum(c.amount * (c.product.price + (c.product.price * (c.product.vat.vat / 100)))) from Cart c where c.customer = :customer");
             query.setParameter("customer", customer);
             Object[] queryResult = (Object[]) query.getSingleResult();
             result = new CartStats();
            if (queryResult[0] != null && queryResult[1] != null && queryResult[2] != null) {
                result.setAmount(BigDecimal.valueOf((Long) queryResult[0]));
                result.setPrice((BigDecimal) queryResult[1]);
                result.setPriceVat((BigDecimal) queryResult[2]);
            }
         } catch (NoResultException e) {
         }
         return result;
     }
 }
