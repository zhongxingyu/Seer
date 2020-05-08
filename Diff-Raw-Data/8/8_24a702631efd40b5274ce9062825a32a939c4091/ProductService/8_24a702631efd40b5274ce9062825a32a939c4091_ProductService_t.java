 package com.cqlybest.common.service;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Junction;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cqlybest.common.bean.Product;
 import com.cqlybest.common.bean.ProductComment;
 import com.cqlybest.common.bean.ProductFilterItem;
 import com.cqlybest.common.bean.ProductGroup;
 import com.cqlybest.common.bean.ProductTravel;
 import com.cqlybest.common.dao.ImageDao;
 import com.cqlybest.common.dao.ProductDao;
 
 @Service
 public class ProductService {
 
   @Autowired
   private ProductDao productDao;
   @Autowired
   private ImageDao imageDao;
 
   @Transactional
   public void add(Product product) {
     product.setId(UUID.randomUUID().toString());
     Date now = new Date();
     product.setCreatedTime(now);
     product.setLastUpdated(now);
     productDao.saveOrUpdate(product);
   }
 
   @Transactional
   public void add(ProductTravel travel) {
     productDao.saveOrUpdate(travel);
   }
 
   @Transactional
   public void add(ProductComment comment) {
     productDao.saveOrUpdate(comment);
   }
 
   public Product get(String id) {
     Product product = productDao.findById(id);
     product.setTravels(productDao.getTravels(id));
     product.setPosters(imageDao.queryImagesWithoutData("product-poster", id));
     product.setPhotos(imageDao.queryImagesWithoutData("product-photo", id));
     product.setComments(productDao.getComments(id));
     return product;
   }
 
   @Transactional
   public void update(String id, String name, Object value) {
     productDao.update(id, name, value);
   }
 
   @Transactional
   public void update(String[] ids, String name, Object value) {
     productDao.update(ids, name, value);
   }
 
   @Transactional
   public void updateTravel(Integer id, String name, String value) {
     productDao.updateTravel(id, name, value);
   }
 
   @Transactional
   public void delete(String[] ids) {
     productDao.delete(ids);
   }
 
   @Transactional
   public void deleteTravel(Integer id) {
     productDao.deleteTravel(id);
   }
 
   @Transactional
   public void deleteComment(Integer id) {
     productDao.deleteComment(id);
   }
 
   public Long queryProductTotal(Boolean hot, Boolean red, Boolean spe, Boolean pub, String name) {
     return productDao.findProductTotal(hot, red, spe, pub, name);
   }
 
   public List<Product> queryProduct(Boolean hot, Boolean red, Boolean spe, Boolean pub,
       String name, int page, int pageSize) {
     return productDao.findProductTotal(hot, red, spe, pub, name, page, pageSize);
   }
 
   public List<Product> queryProducts(ProductGroup productGroup, Set<ProductFilterItem> filterItems,
       Integer page, Integer pageSize) {
     Disjunction or = Restrictions.disjunction();
     Conjunction and = Restrictions.conjunction();
     generateGroupCondition(or, "recommendedMonths", productGroup.getGroupMonths());
     generateGroupCondition(or, "crowds", productGroup.getGroupCrowds());
     generateGroupCondition(or, "traffics", productGroup.getGroupTraffics());
     generateGroupCondition(or, "types", productGroup.getGroupTypes());
     generateGroupCondition(or, "grades", productGroup.getGroupGrades());
     generateGroupCondition(or, "keywords", productGroup.getGroupKeywords());
     generateGroupCondition(or, "departureCities", productGroup.getGroupDepartureCities());
     generateGroupCondition(or, "destinations", productGroup.getGroupDestinations());
    List<Product> products = productDao.getProducts(or, and, page, pageSize);
    if (pageSize != null) {
      for (Product product : products) {
        product.setPosters(imageDao.queryImagesWithoutData("product-poster", product.getId()));
      }
    }
    return products;
   }
 
   private void generateGroupCondition(Junction condition, String property, String values) {
     if (values != null) {
       for (String value : values.split(",")) {
         condition.add(Restrictions.like(property, value, MatchMode.ANYWHERE));
       }
     }
   }
 
 }
