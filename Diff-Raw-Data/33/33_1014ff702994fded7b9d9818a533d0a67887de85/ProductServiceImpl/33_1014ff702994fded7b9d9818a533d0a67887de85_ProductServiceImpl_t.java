 package com.xone.service.app;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.model.hibernate.app.ImageUploadedDao;
 import com.xone.model.hibernate.app.PersonDao;
 import com.xone.model.hibernate.app.ProductCheckDao;
 import com.xone.model.hibernate.app.ProductDao;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.entity.Person;
 import com.xone.model.hibernate.entity.Product;
 import com.xone.model.hibernate.entity.ProductCheck;
 import com.xone.model.hibernate.support.Pagination;
 import com.xone.model.utils.MyModelUtils;
 
 public class ProductServiceImpl implements ProductService {
     private static final Log log = LogFactory.getLog(ProductServiceImpl.class);
 
     @Autowired
     protected ProductDao productDao;
 
     @Autowired
     protected ImageUploadedDao imageUploadedDao;
 
     @Autowired
     protected ProductCheckDao productCheckDao;
     
     @Autowired
     protected PersonDao personDao;
 
     public PersonDao getPersonDao() {
         return personDao;
     }
 
     public void setPersonDao(PersonDao personDao) {
         this.personDao = personDao;
     }
 
     /* (non-Javadoc)
      * @see com.xone.service.app.ProductService#updateFlagDeletedWhenExpired()
      */
     @Override
     public void updateFlagDeletedWhenExpired() {
         DetachedCriteria c = DetachedCriteria.forClass(Product.class);
         c.add(Restrictions.le("productValid", new Date()));
         c.add(Restrictions.eq("flagDeleted", Product.FlagDeleted.NORMAL.getValue()));
         List<Product> l = getProductDao().findByDetachedCriteria(c);
         for(Product p : l){
             p.setFlagDeleted(Product.FlagDeleted.DELETED.getValue());
             getProductDao().update(p);
         }
     }
 
 	@Override
 	public Map<String, String> updateCloseRecord(Long productId, Long userId) {
         Product product = getProductDao().findById(productId);
         Map<String, String> map = new HashMap<String, String>();
         if (null == product || null == product.getId()) {
         	map.put("msg", "记录不存在。");
         	return map;
         }
         if (!product.isCloseable()) {
         	map.put("msg", "记录还不可关闭。");
         	return map;
         }
         if (Math.abs(product.getUserCreated() - userId) > 0) {
         	map.put("msg", "非法操作记录。");
         	return map;
         }
         product.setCheckStatus(Product.CheckStatus.CLOSED.getValue());
         product.setUserUpdated(userId);
         getProductDao().update(product);
         Date date = new Date();
         ProductCheck productCheck = new ProductCheck();
         productCheck.setProductId(product.getId());
         productCheck.setDateCheck(date);
         productCheck.setFlagDeleted(ProductCheck.FlagDeleted.NORMAL.getValue());
         productCheck.setUserApply(userId);
         productCheck.setDateApply(date);
         productCheck.setCheckStatus(Product.CheckStatus.CLOSED.getValue());
         productCheck.setRemark("用户自己关闭");
         productCheckDao.save(productCheck);
 		return null;
 	}
 
     @Override
     public Product save(Product entity) {
         entity.setFlagDeleted(Product.FlagDeleted.NORMAL.getValue());
         return getProductDao().save(entity);
     }
 
     @Override
     public Product findById(Long id) {
         Product product = getProductDao().findById(id);
         if (null != product) {
             List<Long> ids = getImageUploadedDao().findAllIdsByRefId(product.getId(), ImageUploaded.RefType.PRODUCT);
             product.setIds(ids);
             
             List<ProductCheck> checks = getProductCheckDao().findByProductId(product.getId());
             product.setProductCheckList(checks);
             
             if (null != product.getUserCreated()) {
                 Person person = getPersonDao().findById(product.getUserCreated());
                 if (null != person) {
                 	product.setPerson(person);
                 }
             }
         }
         return product;
     }
 
     @Override
     public Product update(Product entity) {
         return getProductDao().update(entity);
     }
 
     @Override
     public void delete(Product entity) {
         getProductDao().deleteById(entity.getId());
     }
 
     @Override
     public Product save(Product entity, ImageUploaded imageUploaded) {
         entity.setCheckStatus(Product.CheckStatus.WAITING.getValue());
         entity.setFlagDeleted(Product.FlagDeleted.NORMAL.getValue());
         entity.setProductValid(DateUtils.addDays(new Date(), 30));
         entity = getProductDao().save(entity);
         imageUploaded.setRefId(entity.getId());
         imageUploaded.setRefType(ImageUploaded.RefType.PRODUCT.getValue());
         imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         imageUploaded = getImageUploadedDao().save(imageUploaded);
         entity.getIds().add(imageUploaded.getId());
         return entity;
     }
 
     @Override
     public Product save(Product entity, List<ImageUploaded> imageUploadeds) {
         entity.setCheckStatus(Product.CheckStatus.WAITING.getValue());
         entity.setFlagDeleted(Product.FlagDeleted.NORMAL.getValue());
         entity.setProductValid(DateUtils.addDays(new Date(), 30));
         entity = getProductDao().save(entity);
         for (ImageUploaded imageUploaded : imageUploadeds) {
             imageUploaded.setRefId(entity.getId());
             imageUploaded.setRefType(ImageUploaded.RefType.PRODUCT.getValue());
             imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         }
         imageUploadeds = getImageUploadedDao().save(imageUploadeds);
         List<Long> ids = new ArrayList<Long>();
         for (ImageUploaded imageUploaded : imageUploadeds) {
             ids.add(imageUploaded.getId());
         }
         entity.setIds(ids);
         return entity;
     }
 
     @Override
     public Product update(Product entity, List<ImageUploaded> imageUploadeds, List<Long> imageIds) {
         for (ImageUploaded imageUploaded : imageUploadeds) {
             imageUploaded.setRefId(entity.getId());
             imageUploaded.setRefType(ImageUploaded.RefType.PRODUCT.getValue());
             imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         }
         if (!imageUploadeds.isEmpty()) {
             imageUploadeds = getImageUploadedDao().save(imageUploadeds);
         }
         Map<Long, Boolean> imageIdMap = new HashMap<Long, Boolean>();
         List<Long> ids = new ArrayList<Long>();
         for (Long id : imageIds) {
         	if (null == id) {
         		continue;
         	}
             imageIdMap.put(id, true);
         }
         for (Long id : entity.getIds()) {
             if (null == imageIdMap.get(id)) {
                 getImageUploadedDao().deleteLogicById(id);
             } else {
                 ids.add(id);
             }
         }
         for (ImageUploaded imageUploaded : imageUploadeds) {
             ids.add(imageUploaded.getId());
         }
         entity.setIds(ids);
         
         // 审核信息处理
         Date dateCheck = new Date();
         
         ProductCheck check = entity.getProductCheck();
 //        if (Product.CheckStatus.DENIED.getValue().equals(check.getCheckStatus()) || Product.CheckStatus.PASSED.getValue().equals(check.getCheckStatus())) {
             check.setProductId(entity.getId());
             check.setDateCheck(dateCheck);
             check.setFlagDeleted(ProductCheck.FlagDeleted.NORMAL.getValue());
             check.setUserApply(entity.getUserApply());
             check.setDateApply(entity.getDateApply());
            /**
             * 修改代码为选择数据关闭状态，数据的状态不发生变更2013-09-14 22:32
             */
            if (null == check.getCheckStatus()) {
             	check.setCheckStatus(Product.CheckStatus.WAITING.getValue());
             }
//            if (Product.CheckStatus.DENIED.getValue().equals(check.getCheckStatus()) 
//            		|| Product.CheckStatus.PASSED.getValue().equals(check.getCheckStatus())) {
//            } else{
//            	check.setCheckStatus(Product.CheckStatus.WAITING.getValue());
//            }
             
             productCheckDao.save(check);
             
             entity.setDateCheck(dateCheck);
             entity.setCheckStatus(check.getCheckStatus());
 //        }
 
         entity = getProductDao().update(entity);
         
         return entity;
     }
 
     @Override
     public List<Product> findAllByMap(Map<String, Object> params) {
     	List<Product> list = getProductDao().findAllProductByUserRef(params);
 //        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Product.class);
 //        String gtDateCreated = params.get("gtDateCreated");
 //        if (!StringUtils.isBlank(gtDateCreated)) {
 //            try {
 //                detachedCriteria.add(Restrictions.gt("dateCreated", DateUtils.parseDate(gtDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
 //            } catch (ParseException e) {
 //                e.printStackTrace();
 //            }
 //        }
 //        String ltDateCreated = params.get("ltDateCreated");
 //        if (!StringUtils.isBlank(ltDateCreated)) {
 //            try {
 //                detachedCriteria.add(Restrictions.lt("dateCreated", DateUtils.parseDate(ltDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
 //            } catch (ParseException e) {
 //                e.printStackTrace();
 //            }
 //        }
 //        String saleType = params.get("saleType");
 //        if (!StringUtils.isBlank(saleType)) {
 //            detachedCriteria.add(Restrictions.eq("saleType", saleType));
 //        }
 //        String productName = params.get("productName");
 //        if (!StringUtils.isBlank(productName)) {
 //            detachedCriteria.add(Restrictions.like("productName", "%" + productName + "%"));
 //        }
 //        String productType = params.get("productType");
 //        if (!StringUtils.isBlank(productType)) {
 //            detachedCriteria.add(Restrictions.like("productType", "%" + productType + "%"));
 //        }
 //        String checkStatus = params.get("checkStatus");
 //        if (!StringUtils.isBlank(checkStatus)) {
 //            detachedCriteria.add(Restrictions.eq("checkStatus", checkStatus));
 //        }
 //        String productAddress = params.get("productAddress");
 //        if (!StringUtils.isBlank(productAddress)) {
 //            detachedCriteria.add(Restrictions.like("productAddress", "%" + productAddress + "%"));
 //        }
 //        String productLocation = params.get("productLocation");
 //        if (!StringUtils.isBlank(productLocation)) {
 //            detachedCriteria.add(Restrictions.like("productLocation", "%" + productLocation + "%"));
 //        }
 //        String flagDeleted = params.get("flagDeleted");
 //        if (!StringUtils.isBlank(flagDeleted)) {
 //            detachedCriteria.add(Restrictions.eq("flagDeleted", flagDeleted));
 //        }
 //        String userCreated = params.get("userCreated");
 //        if (!StringUtils.isBlank(userCreated)) {
 //            detachedCriteria.add(Restrictions.eq("userCreated", Long.parseLong(userCreated)));
 //        }
 //        detachedCriteria.addOrder(Order.desc("dateCreated"));
 //        List<Product> list = getProductDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
 //        if (null != list && !list.isEmpty()) {
 //            List<Long> ids = new ArrayList<Long>();
 //            for (Product p : list) {
 //                ids.add(p.getId());
 //            }
 //            Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.PRODUCT, 0, ids.size() * 3);
 //            for (int i = 0; i < ids.size(); i++) {
 //                Product ip = list.get(i);
 //                List<Long> imageIds = maps.get(ip.getId());
 //                if (null != imageIds) {
 //                    ip.setIds(maps.get(ip.getId()));
 //                }
 //                list.set(i, ip);
 //            }
 //        }
         return getProductWithImageIdByProducts(list);
     }
     
     @Override
     public List<Product> findAllByIds(List<Long> ids, Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Product.class);
         if (null != ids && !ids.isEmpty()) {
             detachedCriteria.add(Restrictions.in("id", ids));
         }
         String checkStatus = params.get("checkStatus");
         if (!StringUtils.isBlank(checkStatus)) {
             detachedCriteria.add(Restrictions.eq("checkStatus", checkStatus));
         }
         String flagDeleted = params.get("flagDeleted");
         if (!StringUtils.isBlank(flagDeleted)) {
             detachedCriteria.add(Restrictions.eq("flagDeleted", flagDeleted));
         }
         detachedCriteria.addOrder(Order.desc("dateCreated"));
         List<Product> list = getProductDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
         return getProductWithImageIdByProducts(list);
     }
     
     /**
      * 返回带图片Id的产品信息
      * @param list
      * @return
      */
     protected List<Product> getProductWithImageIdByProducts(List<Product> list) {
         if (null != list && !list.isEmpty()) {
             List<Long> ids = new ArrayList<Long>();
             for (Product p : list) {
                 ids.add(p.getId());
             }
             Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.PRODUCT, 0, ids.size() * 3);
             for (int i = 0; i < ids.size(); i++) {
                 Product ip = list.get(i);
                 List<Long> imageIds = maps.get(ip.getId());
                 if (null != imageIds) {
                     ip.setIds(maps.get(ip.getId()));
                 }
                 list.set(i, ip);
             }
         }
         return list;
     }
 
 	@Override
 	public List<Product> findAllByMapForUser(Map<String, String> params) {
       DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Product.class);
       String gtDateCreated = params.get("gtDateCreated");
       if (!StringUtils.isBlank(gtDateCreated)) {
           try {
               detachedCriteria.add(Restrictions.gt("dateCreated", DateUtils.parseDate(gtDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
           } catch (ParseException e) {
               e.printStackTrace();
           }
       }
       String ltDateCreated = params.get("ltDateCreated");
       if (!StringUtils.isBlank(ltDateCreated)) {
           try {
               detachedCriteria.add(Restrictions.lt("dateCreated", DateUtils.parseDate(ltDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
           } catch (ParseException e) {
               e.printStackTrace();
           }
       }
       String saleType = params.get("saleType");
       if (!StringUtils.isBlank(saleType)) {
           detachedCriteria.add(Restrictions.eq("saleType", saleType));
       }
       String productName = params.get("productName");
       if (!StringUtils.isBlank(productName)) {
           detachedCriteria.add(Restrictions.like("productName", "%" + productName + "%"));
       }
       String productType = params.get("productType");
       if (!StringUtils.isBlank(productType)) {
           detachedCriteria.add(Restrictions.like("productType", "%" + productType + "%"));
       }
       String checkStatus = params.get("checkStatus");
       if (!StringUtils.isBlank(checkStatus)) {
           detachedCriteria.add(Restrictions.eq("checkStatus", checkStatus));
       }
       String productAddress = params.get("productAddress");
       if (!StringUtils.isBlank(productAddress)) {
           detachedCriteria.add(Restrictions.like("productAddress", "%" + productAddress + "%"));
       }
       String productLocation = params.get("productLocation");
       if (!StringUtils.isBlank(productLocation)) {
           detachedCriteria.add(Restrictions.like("productLocation", "%" + productLocation + "%"));
       }
       String flagDeleted = params.get("flagDeleted");
       if (!StringUtils.isBlank(flagDeleted)) {
           detachedCriteria.add(Restrictions.eq("flagDeleted", flagDeleted));
       }
       String userCreated = params.get("userCreated");
       if (!StringUtils.isBlank(userCreated)) {
           detachedCriteria.add(Restrictions.eq("userCreated", Long.parseLong(userCreated)));
       }
       detachedCriteria.addOrder(Order.desc("dateCreated"));
       List<Product> list = getProductDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
       if (null != list && !list.isEmpty()) {
           List<Long> ids = new ArrayList<Long>();
           for (Product p : list) {
               ids.add(p.getId());
           }
           Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.PRODUCT, 0, ids.size() * 3);
           for (int i = 0; i < ids.size(); i++) {
               Product ip = list.get(i);
               List<Long> imageIds = maps.get(ip.getId());
               if (null != imageIds) {
                   ip.setIds(maps.get(ip.getId()));
               }
               list.set(i, ip);
           }
       }
       return list;
 	}
 
     @Override
     public Product findByMap(Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Product.class);
         String id = params.get("id");
         if (!StringUtils.isBlank(id)) {
             detachedCriteria.add(Restrictions.eq("id", Long.parseLong(id)));
         }
         List<Product> l = getProductDao().findListByDetachedCriteria(detachedCriteria, 0, 1);
         if (null == l || l.isEmpty()) {
             return new Product();
         }
         Product p = l.get(0);
         List<Long> ids = getImageUploadedDao().findAllIdsByRefId(p.getId(), ImageUploaded.RefType.PRODUCT);
         p.setIds(ids);
         if (null != p.getUserCreated()) {
             Person person = getPersonDao().findById(p.getUserCreated());
             if (null != person) {
                 p.setPerson(person);
             }
         }
 		if (StringUtils.isNotBlank(params.get("checklist"))
 				&& "productCheckList".equalsIgnoreCase(params.get("checklist"))) {
 			p.setProductCheckList(getProductCheckDao().findByProductId(
 					p.getId()));
 		}
         return p;
     }
 
     public Pagination findByParams(Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Product.class);
 
         handleCriteriaByParams(detachedCriteria, params);
 
         int pageSize = MyModelUtils.parseInt(params.get("pageSize"), 20);
         int startIndex = MyModelUtils.parseInt(params.get("pageNo"), 0);
         Pagination p = getProductDao().findByDetachedCriteria(detachedCriteria, pageSize, startIndex);
         p.setList(getProductWithImageIdByProducts(p.getList()));
         return p;
     }
     
     protected void handleCriteriaByParams(DetachedCriteria criteria, Map<String, String> params){
         String id = params.get("id");
         if (!StringUtils.isBlank(id)) {
             criteria.add(Restrictions.eq("id", new Long(id)));
         }
         String idMin = params.get("idMin");
         if (!StringUtils.isBlank(idMin)) {
             criteria.add(Restrictions.ge("id", new Long(idMin)));
         }
         String idMax = params.get("idMax");
         if (!StringUtils.isBlank(idMax)) {
             criteria.add(Restrictions.le("id", new Long(idMax)));
         }
         String productName = params.get("productName");
         if (!StringUtils.isBlank(productName)) {
             criteria.add(Restrictions.like("productName", "%" + productName + "%"));
         }
         String productType = params.get("productType");
         if (!StringUtils.isBlank(productType)) {
             criteria.add(Restrictions.like("productType", "%" + productType + "%"));
         }
         String checkStatus = params.get("checkStatus");
         if (!StringUtils.isBlank(checkStatus)) {
             criteria.add(Restrictions.like("checkStatus", "%" + checkStatus + "%"));
         }
         String saleType = params.get("saleType");
         if (!StringUtils.isBlank(saleType)) {
             criteria.add(Restrictions.like("saleType", "%" + saleType + "%"));
         }
         String productPrice = params.get("productPrice");
         if (!StringUtils.isBlank(productPrice)) {
             criteria.add(Restrictions.like("productPrice", "%" + productPrice + "%"));
         }
         String productNum = params.get("productNum");
         if (!StringUtils.isBlank(productNum)) {
             criteria.add(Restrictions.like("productNum", "%" + productNum + "%"));
         }
         String productValidMin = params.get("productValidMin");
         if (!StringUtils.isBlank(productValidMin)) {
             try {
                 criteria.add(Restrictions.ge("productValid", DateUtils.parseDate(productValidMin, "yyyy-MM-dd" )));
             } catch (ParseException e) {
                 log.error("[productValidMin] parsed exception :", e);
             }
         }
         String productValidMax = params.get("productValidMax");
         if (!StringUtils.isBlank(productValidMax)) {
             try {
                 criteria.add(Restrictions.lt("productValid", DateUtils.addDays(DateUtils.parseDate(productValidMax, "yyyy-MM-dd" ), 1)));
             } catch (ParseException e) {
                 log.error("[productValidMax] parsed exception :", e);
             }
         }
         String productAddress = params.get("productAddress");
         if (!StringUtils.isBlank(productAddress)) {
             criteria.add(Restrictions.like("productAddress", "%" + productAddress + "%"));
         }
         String productLocation = params.get("productLocation");
         if (!StringUtils.isBlank(productLocation)) {
             criteria.add(Restrictions.like("productLocation", "%" + productLocation + "%"));
         }
         String productDesc = params.get("productDesc");
         if (!StringUtils.isBlank(productDesc)) {
             criteria.add(Restrictions.like("productDesc", "%" + productDesc + "%"));
         }
         String flagDeleted = params.get("flagDeleted");
         if (!StringUtils.isBlank(flagDeleted)) {
             criteria.add(Restrictions.like("flagDeleted", "%" + flagDeleted + "%"));
         }
         String userApply = params.get("userApply");
         if (!StringUtils.isBlank(userApply)) {
             criteria.add(Restrictions.eq("userApply", new Long(userApply)));
         }
         String userApplyMin = params.get("userApplyMin");
         if (!StringUtils.isBlank(userApplyMin)) {
             criteria.add(Restrictions.ge("userApply", new Long(userApplyMin)));
         }
         String userApplyMax = params.get("userApplyMax");
         if (!StringUtils.isBlank(userApplyMax)) {
             criteria.add(Restrictions.le("userApply", new Long(userApplyMax)));
         }
         String dateApplyMin = params.get("dateApplyMin");
         if (!StringUtils.isBlank(dateApplyMin)) {
             try {
                 criteria.add(Restrictions.ge("dateApply", DateUtils.parseDate(dateApplyMin, "yyyy-MM-dd" )));
             } catch (ParseException e) {
                 log.error("[dateApplyMin] parsed exception :", e);
             }
         }
         String dateApplyMax = params.get("dateApplyMax");
         if (!StringUtils.isBlank(dateApplyMax)) {
             try {
                 criteria.add(Restrictions.lt("dateApply", DateUtils.addDays(DateUtils.parseDate(dateApplyMax, "yyyy-MM-dd" ), 1)));
             } catch (ParseException e) {
                 log.error("[dateApplyMax] parsed exception :", e);
             }
         }
         String userCheck = params.get("userCheck");
         if (!StringUtils.isBlank(userCheck)) {
             criteria.add(Restrictions.eq("userCheck", new Long(userCheck)));
         }
         String userCheckMin = params.get("userCheckMin");
         if (!StringUtils.isBlank(userCheckMin)) {
             criteria.add(Restrictions.ge("userCheck", new Long(userCheckMin)));
         }
         String userCheckMax = params.get("userCheckMax");
         if (!StringUtils.isBlank(userCheckMax)) {
             criteria.add(Restrictions.le("userCheck", new Long(userCheckMax)));
         }
         String dateCheckMin = params.get("dateCheckMin");
         if (!StringUtils.isBlank(dateCheckMin)) {
             try {
                 criteria.add(Restrictions.ge("dateCheck", DateUtils.parseDate(dateCheckMin, "yyyy-MM-dd" )));
             } catch (ParseException e) {
                 log.error("[dateCheckMin] parsed exception :", e);
             }
         }
         String dateCheckMax = params.get("dateCheckMax");
         if (!StringUtils.isBlank(dateCheckMax)) {
             try {
                 criteria.add(Restrictions.lt("dateCheck", DateUtils.addDays(DateUtils.parseDate(dateCheckMax, "yyyy-MM-dd" ), 1)));
             } catch (ParseException e) {
                 log.error("[dateCheckMax] parsed exception :", e);
             }
         }
         String userCreated = params.get("userCreated");
         if (!StringUtils.isBlank(userCreated)) {
             criteria.add(Restrictions.eq("userCreated", new Long(userCreated)));
         }
         String userCreatedMin = params.get("userCreatedMin");
         if (!StringUtils.isBlank(userCreatedMin)) {
             criteria.add(Restrictions.ge("userCreated", new Long(userCreatedMin)));
         }
         String userCreatedMax = params.get("userCreatedMax");
         if (!StringUtils.isBlank(userCreatedMax)) {
             criteria.add(Restrictions.le("userCreated", new Long(userCreatedMax)));
         }
         String dateCreatedMin = params.get("dateCreatedMin");
         if (!StringUtils.isBlank(dateCreatedMin)) {
             try {
                 criteria.add(Restrictions.ge("dateCreated", DateUtils.parseDate(dateCreatedMin, "yyyy-MM-dd" )));
             } catch (ParseException e) {
                 log.error("[dateCreatedMin] parsed exception :", e);
             }
         }
         String dateCreatedMax = params.get("dateCreatedMax");
         if (!StringUtils.isBlank(dateCreatedMax)) {
             try {
                 criteria.add(Restrictions.lt("dateCreated", DateUtils.addDays(DateUtils.parseDate(dateCreatedMax, "yyyy-MM-dd" ), 1)));
             } catch (ParseException e) {
                 log.error("[dateCreatedMax] parsed exception :", e);
             }
         }
         String userUpdated = params.get("userUpdated");
         if (!StringUtils.isBlank(userUpdated)) {
             criteria.add(Restrictions.eq("userUpdated", new Long(userUpdated)));
         }
         String userUpdatedMin = params.get("userUpdatedMin");
         if (!StringUtils.isBlank(userUpdatedMin)) {
             criteria.add(Restrictions.ge("userUpdated", new Long(userUpdatedMin)));
         }
         String userUpdatedMax = params.get("userUpdatedMax");
         if (!StringUtils.isBlank(userUpdatedMax)) {
             criteria.add(Restrictions.le("userUpdated", new Long(userUpdatedMax)));
         }
         String lastUpdatedMin = params.get("lastUpdatedMin");
         if (!StringUtils.isBlank(lastUpdatedMin)) {
             try {
                 criteria.add(Restrictions.ge("lastUpdated", DateUtils.parseDate(lastUpdatedMin, "yyyy-MM-dd" )));
             } catch (ParseException e) {
                 log.error("[lastUpdatedMin] parsed exception :", e);
             }
         }
         String lastUpdatedMax = params.get("lastUpdatedMax");
         if (!StringUtils.isBlank(lastUpdatedMax)) {
             try {
                 criteria.add(Restrictions.lt("lastUpdated", DateUtils.addDays(DateUtils.parseDate(lastUpdatedMax, "yyyy-MM-dd" ), 1)));
             } catch (ParseException e) {
                 log.error("[lastUpdatedMax] parsed exception :", e);
             }
         }
         
         criteria.addOrder(Order.desc("dateCreated"));
     }
 
 
 
     public ImageUploadedDao getImageUploadedDao() {
         return imageUploadedDao;
     }
 
     public void setImageUploadedDao(ImageUploadedDao imageUploadedDao) {
         this.imageUploadedDao = imageUploadedDao;
     }
 
     public ProductDao getProductDao() {
         return productDao;
     }
 
     public void setProductDao(ProductDao productDao) {
         this.productDao = productDao;
     }
 
     public ProductCheckDao getProductCheckDao() {
         return productCheckDao;
     }
 
     public void setProductCheckDao(ProductCheckDao productCheckDao) {
         this.productCheckDao = productCheckDao;
     }
 
 }
