 package com.omartech.tdg.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.omartech.tdg.mapper.ItemMapper;
 import com.omartech.tdg.model.Coinage;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.utils.ProductStatus;
 
 @Service
 public class ItemService {
 	@Autowired
 	private ItemMapper itemMapper;
 
 	@Autowired
 	private ProductService productService;
 	
 	@Transactional
 	public void insertItem(Item item) {
 		if(item.getAvailableQuantity() < item.getSafeStock()){
 			item.setActive(0);
 		}else{
 			item.setActive(1);
 		}
 		item.setStatus(ProductStatus.OK);
 		itemMapper.insertItem(item);
 	}
 	/**
 	 * 根据itemId和数量来返回对应的价格，是单价，不是乘上数量之后的价格
 	 */
 	public float getPriceByItemId(int id, int count){
 		Item item = getItemById(id);
 		Date now = new Date(System.currentTimeMillis());
 		Date begin = item.getPromotionTime();
 		Date end = item.getPromotionEnd();
 		int min = item.getMinimumQuantity();
 		int max = item.getMaximumAcceptQuantity();
 		float result = 0f;
 		if(count < max && count > min){//优先批发价
 			float pifa = item.getWholePrice();
 			if(pifa - 0.0 < 0.01)
 				result = item.getRetailPrice();
 			else
 				result =  item.getWholePrice();
 		}
 		if(begin != null && end !=null){//如果在优惠期就用优惠价
 			if(now.after(begin) && end.after(now)){
 				float pro = item.getPromotionPrice();
 				if(pro - 0.0 < 0.001){
 					pro = item.getRetailPrice();
 				}
				if(result > 0){
					if(pro < result){
						result = pro;
					}
				}else{
 					result = pro;
 				}
 			}else{
 				result = item.getRetailPrice();
 			}
 		}else{
 			result = item.getRetailPrice();
 		}
 		return result;
 	}
 	public Item getItemBySku(String sku) {//for 卖家
 		Item item = itemMapper.getItemBySku(sku);
 		return item;
 	}
 	
 	public Item getItemById(int id){//for 系统
 		Item item = itemMapper.getItemById(id);
 		return item;
 	}
 	
 
 	public List<Item> getItemsByProductId(int productId) {
 		List<Item> items = itemMapper.getItemsByProductIdAndStatus(productId, ProductStatus.OK);
 		return items;
 	}
 	public List<Item> getItemsByProductIdAndStatus(int productId, int statusId){
 		List<Item> items = itemMapper.getItemsByProductIdAndStatus(productId, statusId);
 		return items;
 	}
 
 	public void updateItem(Item item){
 		itemMapper.updateItem(item);
 	}
 	
 	public void updateItemStatus(int itemId, int statusId){
 		Item item = getItemById(itemId);
 		item.setStatus(statusId);
 		updateItem(item);
 	}
 	
 	public void deleteItem(int itemId){
 		updateItemStatus(itemId, ProductStatus.Deleted);
 //		Item item = getItemById(itemId);
 //		int productId = item.getProductId();
 //		List<Item> items = getItemsByProductIdAndStatus(productId, ProductStatus.OK);
 //		if(items.size() == 0){
 //			productService.updateProductStatus(productId, ProductStatus.NoChildren);
 //		}
 	}
 	
 	public ItemMapper getItemMapper() {
 		return itemMapper;
 	}
 
 	public void setItemMapper(ItemMapper itemMapper) {
 		this.itemMapper = itemMapper;
 	}
 
 }
