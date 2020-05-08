 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.service;
 
 import java.util.Collection;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mne.advertmanager.dao.GenericDao;
 import com.mne.advertmanager.model.Author;
 import com.mne.advertmanager.model.Product;
 import com.mne.advertmanager.model.ProductGroup;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 public class ProductService {
 
     private GenericDao<Product, Long> productDao;
     private AuthorService authorService;
     private ProductGroupService pgService;
 
     public void setProductDao(GenericDao<Product, Long> productDao) {
         this.productDao = productDao;
     }
 
     public void setAuthorService(AuthorService authorService) {
         this.authorService = authorService;
     }
 
     public void setProductGroupService(ProductGroupService pgService) {
         this.pgService = pgService;
     }
     
     
 
 //============================ findAllProducts =================================
    @Transactional(readOnly = true)
     public Collection<Product> findAllProducts() {
         return productDao.findByQuery("Product.findAll");
     }
     
    @Transactional(readOnly = true)
     public Product findProductByLink(String link) {
         
         Product result = null;
 
         Collection<Product> data =  productDao.findByQuery("Product.findByProductLink",link);
         if (data != null && data.size()>0)
             result = data.iterator().next();
         
         return result;
     }    
 
 //============================ createProduct ===================================
     @Transactional
     public void createProduct(Product product) {
         
         Author author=null;
         ProductGroup pg = null;
         author = product.getAuthorId();
         pg = product.getProductGroupId();
         
         if (author!=null )
             author = authorService.createOrUpdate(author);
         if (pg != null)
             pg = pgService.createOrUpdate(pg);
         
         product.setAuthorId(author);
         product.setProductGroupId(pg);
         productDao.create(product);
     }
     
 }
