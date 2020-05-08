 package oshop.services.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Service;
 import oshop.dao.GenericDao;
 import oshop.dto.GenericListDto;
 import oshop.model.Product;
 import oshop.model.ProductCategory;
 import oshop.services.ProductCategoryService;
 import oshop.services.converter.EntityConverter;
 
 import javax.annotation.Resource;
 import java.util.List;
 import java.util.Map;
 
 @Service("productCategoryService")
 public class ProductCategoryServiceImpl extends GenericServiceImpl<ProductCategory, Integer> implements ProductCategoryService {
 
     private static final Log log = LogFactory.getLog(ProductCategoryServiceImpl.class);
 
     @Resource
     private GenericDao<ProductCategory, Integer> productCategoryDao;
 
     @Resource
     private GenericDao<Product, Integer> productDao;
 
     @Resource(name = "productCategoryToDTOConverter")
     private EntityConverter<ProductCategory, Integer> converter;
 
     @Resource(name = "productToDTOConverter")
     private EntityConverter<Product, Integer> productConverter;
 
     @Override
     protected GenericDao<ProductCategory, Integer> getDao() {
         return productCategoryDao;
     }
 
     @Override
     protected EntityConverter<ProductCategory, Integer> getToDTOConverter() {
         return converter;
     }
 
     public GenericListDto<Product> getProductsByCategory(
             Integer id, Map<String, List<String>> filters, Map<String, List<String>> sorters,
             Integer limit, Integer offset)
                 throws Exception {
 
         Criteria criteria = productDao.createCriteria();
         criteria.createAlias("category", "c").add(Restrictions.eq("c.id", id));
        getFilter().applyFilters(filters, criteria);
         getSorter().applySorters(sorters, criteria);
 
         List<Product> list = productConverter.convert(productDao.list(criteria, offset, limit));
 
         return getCountAndPrepareListDto(list, criteria);
 
     }
 }
