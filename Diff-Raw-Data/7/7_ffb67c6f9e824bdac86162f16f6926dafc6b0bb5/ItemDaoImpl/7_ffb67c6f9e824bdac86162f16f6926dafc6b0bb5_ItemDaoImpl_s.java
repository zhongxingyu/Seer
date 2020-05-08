 package com.euroit.militaryshop.persistence.dao.impl;
 
 import com.euroit.eshop.dto.BaseTrolleyItemDto;
 import com.euroit.eshop.persistence.dao.impl.BaseEntityManagerSupport;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.persistence.dao.ItemDao;
 import com.euroit.militaryshop.persistence.entity.DictionaryEntry;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopItem;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopProduct;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import org.apache.commons.lang.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.Query;
 import java.util.*;
 
 /**
  * @author Roman Tuchin
  */
 @Repository
 public class ItemDaoImpl extends BaseEntityManagerSupport implements ItemDao {
 
     private static final Logger LOG = LoggerFactory.getLogger(ItemDaoImpl.class);
     
     public static final int IMAGE_NAME_LENGTH = 6;
 
     @Override
     public long createOrSave(MilitaryShopItemDto militaryShopItemDto) {
 
         MilitaryShopProduct product = em.find(MilitaryShopProduct.class, militaryShopItemDto.getProductId());
 
         MilitaryShopItem item = null;
 
         if (militaryShopItemDto.getId() != 0) {
             Key itemKey = new KeyFactory.Builder(MilitaryShopProduct.class.getSimpleName(),
                     militaryShopItemDto.getProductId()).addChild(MilitaryShopItem.class.getSimpleName(),
                     militaryShopItemDto.getId()).getKey();
 
             item = em.find(MilitaryShopItem.class, itemKey);
         }
 
         if (item == null) {
             item = new MilitaryShopItem();
             product.getItems().add(item);
             
            if (militaryShopItemDto.getBigImageName() == null) {
                 String bigImageName = String.format("%s.jpg", RandomStringUtils.randomAlphanumeric(IMAGE_NAME_LENGTH));
                 militaryShopItemDto.setBigImageName(bigImageName);
             }
 
            if (militaryShopItemDto.getSmallImageName() == null) {
 				String smallImageName = String.format("%s.jpg", RandomStringUtils.randomAlphanumeric(IMAGE_NAME_LENGTH));
 				militaryShopItemDto.setSmallImageName(smallImageName);
             }
         }
 
         item.setSmallImageName(militaryShopItemDto.getSmallImageName());
         item.setBigImageName(militaryShopItemDto.getBigImageName());
         item.setShortName(militaryShopItemDto.getShortName());
         item.setInternalCatalogCode(militaryShopItemDto.getInternalCatalogCode());
 
         if (militaryShopItemDto.getColorId() != 0) {
             item.setColorKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     militaryShopItemDto.getColorId()));
         } else {
             item.setColorKey(null);
         }
 
         if (militaryShopItemDto.getMaterialId() != 0) {
             item.setMaterialKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     militaryShopItemDto.getMaterialId()));
         } else {
             item.setMaterialKey(null);
         }
 
         if (militaryShopItemDto.getSizeId() != 0) {
             item.setSizeKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     militaryShopItemDto.getSizeId()));
         } else {
             item.setSizeKey(null);
         }
 
         item.setProductKey(KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(),
                 militaryShopItemDto.getProductId()));
 
         LOG.trace("An item has been successfully persisted");
         em.merge(product);
         return em.merge(item).getKey().getId();
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<MilitaryShopItem> getProductItemsByProperties(long productId, long colorId, long materialId, long sizeId) {
         StringBuilder sb = new StringBuilder("select from MilitaryShopItem i where i.productKey = :productKey and ");
         sb.append("i.colorKey = :colorKey and i.materialKey = :materialKey and sizeKey = :sizeKey");
 
         Query query = em.createQuery(sb.toString());
 
         query.setParameter("productKey", KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
 
         if (colorId != 0) {
             query.setParameter("colorKey", KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), colorId));
         } else {
             query.setParameter("colorKey", null);
         }
 
         if (materialId != 0) {
             query.setParameter("materialKey", KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), materialId));
         } else {
             query.setParameter("materialKey", null);
         }
 
         if (sizeId != 0) {
             query.setParameter("sizeKey", KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), sizeId));
         } else {
             query.setParameter("sizeKey", null);
         }
 
         return query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<MilitaryShopItem> getItemsForProduct(long productId) {
         Query query = em.createQuery("select from MilitaryShopItem i where i.productKey = :productKey");
         query.setParameter("productKey", KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
 
         return query.getResultList();
     }
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<BaseTrolleyItemDto, MilitaryShopItem> getItemsByIds(Set<BaseTrolleyItemDto> baseItemSet) {
 		Query query = em.createQuery("select from MilitaryShopItem i where i.key in (:itemsKeyList)");
 		List<Key> itemKeys = new ArrayList<Key>();
 		Map<BaseTrolleyItemDto, MilitaryShopItem> retMap = 
 				new LinkedHashMap<BaseTrolleyItemDto, MilitaryShopItem>();
 		
 		for (BaseTrolleyItemDto baseItem : baseItemSet) {
 			Key itemKey = new KeyFactory.Builder(MilitaryShopProduct.class.getSimpleName(),
 					baseItem.getProductId()).addChild(MilitaryShopItem.class.getSimpleName(),
 							baseItem.getItemId()).getKey();
 			itemKeys.add(itemKey);
 			retMap.put(baseItem, null);
 		}
 		
 		query.setParameter("itemsKeyList", itemKeys);
 		
 		List<MilitaryShopItem> itemsList = query.getResultList();
 		
 		for (MilitaryShopItem item : itemsList) {
 			BaseTrolleyItemDto baseItem = new BaseTrolleyItemDto();
 			baseItem.setItemId(item.getKey().getId());
 			baseItem.setProductId(item.getProductKey().getId());
 			
 			retMap.put(baseItem, item);
 		}
 		
 		return retMap;
 	}
 
     @Override
     public MilitaryShopItem findItemByKey(Key itemKey) {
         return em.find(MilitaryShopItem.class, itemKey);
     }
 }
