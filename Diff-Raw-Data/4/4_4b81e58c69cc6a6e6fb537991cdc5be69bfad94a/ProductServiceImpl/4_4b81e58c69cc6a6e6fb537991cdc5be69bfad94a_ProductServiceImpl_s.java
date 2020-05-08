 package com.euroit.militaryshop.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.euroit.common.exception.VisibleProductNotFoundException;
 import com.euroit.militaryshop.dto.DictionaryEntryDto;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.dto.TrolleyItemDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.persistence.dao.CategoryDao;
 import com.euroit.militaryshop.persistence.dao.ItemDao;
 import com.euroit.militaryshop.persistence.dao.ProductDao;
 import com.euroit.militaryshop.persistence.entity.Category;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopItem;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopProduct;
 import com.euroit.militaryshop.service.DictionaryEntryService;
 import com.euroit.militaryshop.service.ProductService;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.appengine.api.datastore.Key;
 
 @Service
 public class ProductServiceImpl implements ProductService {
 
     Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
 
 	private ProductDao productDao;
 
 	private CategoryDao categoryDao;
 
     private DictionaryEntryService dictionaryEntryService;
 
     private ItemDao itemDao;
 	
 	@Override
     @Transactional
 	public void createProduct(String fullName, String shortName,
                               String internalCatalogCode, String categoryCode, Float price) {
 		Category cat = categoryDao.getByCode(categoryCode);
 		
 		ProductDto productDto = new ProductDto();
 		productDto.setFullName(fullName);
 		productDto.setShortName(shortName);
 		productDto.setInternalCatalogCode(internalCatalogCode);
 		productDto.setPrice(price);
 		
 		List<Long> categoryIds = new ArrayList<Long>();
 		categoryIds.add(cat.getKey().getId());
 		
 		productDto.setCategoryIds(categoryIds);
 		
 		productDao.createOrSave(productDto);
 	}
 
     @Transactional
 	private List<ProductDto> getProducts(String categoryCode, Boolean visible) {
 		
 		List<MilitaryShopProduct> retList = productDao.getProducts(categoryCode, visible);
 		List<ProductDto> retListDto = new ArrayList<ProductDto>();
 		
 		for (MilitaryShopProduct product: retList) {
 			ProductDto dto = new ProductDto(product.getKey().getId(), product.getFullName(), product.getShortName());
 			
 			dto.setInternalCatalogCode(product.getInternalCatalogCode());
 			dto.setSmallImageName(product.getSmallImageName());
 			dto.setBigImageName(product.getBigImageName());
 			dto.setSmallImageName(product.getSmallImageName());
 			dto.setPrice(product.getPrice());
             dto.setVisible(product.isVisible());
             
             if (visible != null && visible) {
             	//TODO should be in BO defined
            	if (product.getItems() != null && !product.getItems().isEmpty() 
            			&& product.getItems().get(0) != null) {
             		dto.setDefaultItemId(product.getItems().get(0).getKey().getId());
             	}
             }
             
 			retListDto.add(dto);
 		}
 		
 		return retListDto;
 	}
 
     @Override
     public ProductDto getVisibleProduct(long productId) throws VisibleProductNotFoundException {
         ProductDto retDto = findProductById(productId);
 
         if (retDto == null || !retDto.isVisible()) {
             throw new VisibleProductNotFoundException();
         }
 
         Map<String, Set<DictionaryEntryDto>> propertiesMap = new HashMap<String, Set<DictionaryEntryDto>>();
         Set<DictionaryEntryDto> colorValuesSet = new LinkedHashSet<DictionaryEntryDto>();
         Set<DictionaryEntryDto> materialValuesSet = new LinkedHashSet<DictionaryEntryDto>();
         Set<DictionaryEntryDto> sizeValuesSet = new LinkedHashSet<DictionaryEntryDto>();
 
         propertiesMap.put(DictionaryName.COLOR.name(), colorValuesSet);
         propertiesMap.put(DictionaryName.MATERIAL.name(), materialValuesSet);
         propertiesMap.put(DictionaryName.SIZE.name(), sizeValuesSet);
 
         for (MilitaryShopItemDto item : retDto.getItems()) {
             loadAndAddValue(colorValuesSet, item.getColorId());
             loadAndAddValue(materialValuesSet, item.getMaterialId());
             loadAndAddValue(sizeValuesSet, item.getSizeId());
         }
 
         retDto.setPropertiesMap(propertiesMap);
         
         prepareItemsJson(retDto); 
 
         return retDto;
     }
 
 	private void prepareItemsJson(ProductDto retDto) {
 		try {
 			List<TrolleyItemDto> trolleyItemList = new ArrayList<TrolleyItemDto>();
 			
 			for (MilitaryShopItemDto item : retDto.getItems()) {
 				TrolleyItemDto itemDto = new TrolleyItemDto();
 				itemDto.setItemId(item.getId());
 				itemDto.setPrice(retDto.getPrice() != null ? retDto.getPrice() : 0);
 				itemDto.setMaterialId(item.getMaterialId());
 				itemDto.setColorId(item.getColorId());
 				itemDto.setSizeId(item.getSizeId());
 				
 				trolleyItemList.add(itemDto);				
 			}
 			
 			retDto.setItemsJson(new ObjectMapper().writeValueAsString(trolleyItemList));
 		} catch (Exception e) {
 			log.warn("Error during preparing items json. The itemsJson will be initialized with null.", e);
 		}
 	}
 
     @Transactional
     private void loadAndAddValue(Set<DictionaryEntryDto> valuesSet, long id) {
 
         if (id != 0) {
             DictionaryEntryDto dto = new DictionaryEntryDto(id);
 
             if (!valuesSet.contains(dto)) {
                 DictionaryEntryDto dictionaryEntryDto = dictionaryEntryService.findDictionaryEntryById(id);
                 valuesSet.add(dictionaryEntryDto);
             }
         }
     }
 
     @Transactional
     private ProductDto findProductById(long productId) {
         ProductDto retDto = null;
         MilitaryShopProduct ret = productDao.findProductById(productId);
 
         if (ret != null) {
             retDto = new ProductDto(ret.getKey().getId(), ret.getFullName(), ret.getShortName());
             retDto.setBigImageName(ret.getBigImageName());
             retDto.setSmallImageName(ret.getSmallImageName());
             retDto.setInternalCatalogCode(ret.getInternalCatalogCode());
             retDto.setPrice(ret.getPrice());
             retDto.setVisible(ret.isVisible());
 
             List<Long> categoryIds = new ArrayList<Long>();
             for (Key categoryKey : ret.getCategories()) {
                 categoryIds.add(categoryKey.getId());
             }
 
             retDto.setCategoryIds(categoryIds);
 
             List<MilitaryShopItem> items = ret.getItems();
 
             if (log.isTraceEnabled()) {
            	    log.trace(String.format("Product items: %s", items));
             }
 
             List<MilitaryShopItemDto> itemsDto = new ArrayList<MilitaryShopItemDto>();
 
             for (MilitaryShopItem item : items) {
                 MilitaryShopItemDto dto = new MilitaryShopItemDto();
                 dto.setId(item.getKey().getId());
 
                 if (item.getColorKey() != null) {
                     dto.setColorId(item.getColorKey().getId());
                 }
 
                 if (item.getMaterialKey() != null) {
                     dto.setMaterialId(item.getMaterialKey().getId());
                 }
 
                 if (item.getSizeKey() != null) {
                     dto.setSizeId(item.getSizeKey().getId());
                 }
 
                 dto.setShortName(item.getShortName());
                 dto.setProductId(item.getProductKey().getId());
 
                 itemsDto.add(dto);
             }
 
             retDto.setItems(itemsDto);
         }
         return retDto;
     }
 
     @Override
     @Transactional
 	public ProductDto getProductForAdminCard(long productId) {
 		return findProductById(productId);
 	}
 
 	@Override
     @Transactional
 	public List<ProductDto> getAllProducts() {
 		return getProducts(null, null);
 	}
 	
 	@Override
     @Transactional
 	public void createOrSaveProduct(ProductDto productDto) {
 		productDao.createOrSave(productDto);
 	}
 
 	@Override
     @Transactional
 	public List<ProductDto> getAllProductsByCategory(String categoryCode) {
 		return getProducts(categoryCode, null);
 	}
 
     @Override
     @Transactional
     public List<ProductDto> getVisibleProductsByCategory(String categoryCode) {
         return getProducts(categoryCode, true);
     }
 
     @Override
     @Transactional
     public boolean hasItems(long productId) {
         if (productId == 0) {
             return false;
         }
 
         List<MilitaryShopItem> itemList = itemDao.getItemsForProduct(productId);
         return itemList != null && itemList.size() > 0;
     }
 
     @Override
     @Transactional
     public List<ProductDto> getVisibleProducts() {
         return getProducts(null, true);
     }
 
     @Autowired
     public void setDictionaryEntryService(DictionaryEntryService dictionaryEntryService) {
         this.dictionaryEntryService = dictionaryEntryService;
     }
 
     @Autowired
     public void setProductDao(ProductDao productDao) {
         this.productDao = productDao;
     }
 
     @Autowired
     public void setCategoryDao(CategoryDao categoryDao) {
         this.categoryDao = categoryDao;
     }
 
     @Autowired
     public void setItemDao(ItemDao itemDao) {
         this.itemDao = itemDao;
     }
 
 	@Override
 	public ProductDto getProductByCatalogCode(String catalogCode) {
 		MilitaryShopProduct product = productDao.findProductByCatalogCode(catalogCode);
 		
 		ProductDto productDto = null;
 		
 		if (product != null) {
 			productDto = new ProductDto();
 			productDto.setId(product.getKey().getId());
 			productDto.setInternalCatalogCode(product.getInternalCatalogCode());
 		}
 		
 		return productDto;
 	}
 
 	@Override
 	public long getProductIdByCatalogCode(String catalogCode) {
 		return getProductByCatalogCode(catalogCode).getId();
 	}
 
 	@Override
 	@Transactional
 	public ProductDto getProductForTrolley(long productId) {
 		MilitaryShopProduct product = productDao.findProductById(productId);
 		ProductDto productDto = new ProductDto();
 		
 		productDto.setPrice(product.getPrice());
 		productDto.setSmallImageName(product.getSmallImageName());
 
         log.debug("Fetching product for trolley. {}", productDto);
 
 		return productDto;
 	}
 }
