 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.dip.komusubi.lunch.module;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import jp.dip.komusubi.lunch.model.Order;
 import jp.dip.komusubi.lunch.model.OrderLine;
 import jp.dip.komusubi.lunch.model.Product;
 import jp.dip.komusubi.lunch.model.Shop;
 import jp.dip.komusubi.lunch.model.User;
 import jp.dip.komusubi.lunch.module.dao.OrderDao;
 import jp.dip.komusubi.lunch.module.dao.ProductDao;
 import jp.dip.komusubi.lunch.module.dao.ShopDao;
 
 import org.komusubi.common.util.Resolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * basket.
  * @author jun.ozeki
  * @since 2011/11/23
  */
 public class Basket implements Iterable<Order>, Serializable {
 
 	private static final long serialVersionUID = -4354827769351959572L;
 	private static final Logger logger = LoggerFactory.getLogger(Basket.class);
 	private User user;
 	private List<Order> orders;
 	@Inject	private OrderDao orderDao;
 	@Inject	private ProductDao productDao;
 	@Inject	private ShopDao shopDao;
 	@Inject @Named("date") private Resolver<Date> dateResolver; 
 	
 	public Basket() {
 		this(null);
 	}
 	
 	public Basket(User user) {
 		this.user = user;
 		this.orders = new ArrayList<>();
 	}
 	
 	// for unit test.
 	Basket(OrderDao orderDao, ProductDao productDao, ShopDao shopDao, User user) {
 		this(user);
 		this.orderDao = orderDao;
 		this.productDao = productDao;
 		this.shopDao = shopDao;
 	}
 	
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
	public User getUser() {
	    return user;
	}
	
 	public Iterator<Order> iterator() {
 		return orders.iterator();
 	}
 	
 	public Order getOrder(int index) {
 		return orders.get(index);
 	}
 	
 	public List<Order> getOrders() {
 		return orders;
 	}
 	
 	public void add(Product product) {
 		add(product, 1);
 	}
 	
 	public void add(Product product, int quantity) {
 		if (product == null)
 			throw new IllegalArgumentException("product is null");
 		boolean found = false;
 		Order order = null;
 		for (Order o: orders) {
 			if (o.getShop().getId().equals(product.getShopId())) {
 				order = o;
 				found = true;
 			} 
 		}
 		if (!found) {
 			order = new Order()
 							.setUser(user)
 							.setShop(product.getShop());
 //							.setShop(shopDao.find(product.getShopId()));
 			orders.add(order);
 		}
 		// 同じProductが既に存在しても1明細として追加。 数量を加算すべき？
		order.addLine(new OrderLine()
 								.setProduct(product)
 								.setDatetime(dateResolver.resolve())
 								.setQuantity(quantity));
 	}
 	
 	public void add(String productId) {
 		add(productId, 1);
 	}
 
 	public void add(String productId, int quantity) {
 		add(productDao.find(productId), quantity);
 	}
 	
 	public int amount() {
 		int amount = 0;
 		for (Order order: orders) {
 			for (OrderLine o: order) {
 				amount += o.getAmount();
 			}
 		}
 		return amount;
 	}
 
 	public void clear() {
 		orders.clear();
 	}
 	
 	public void clear(String shopId) {
 		clear(shopDao.find(shopId));
 	}
 	
 	public void clear(Shop shop) {
 		if (shop == null) {
 			logger.info("shop is null, nothing to do");
 			return;
 		}
 		for (Order o: orders) {
 			if (o.getShop().equals(shop))
 				orders.remove(o);
 		}
 	}
 	
 	public boolean remove(Product product) {
 		if (product == null)
 			throw new IllegalArgumentException("product MUST not be null.");
 		return remove(product.getId());
 	}
 	
 	public boolean remove(String productId) {
 		boolean result = false;
 		for (Order order: orders) {
 			result = order.remove(productId);
 		}
 		return result;
 	}
 
 //	public void modify(String productId, int quantity) {
 //		if (productId == null || "".equals(productId))
 //			throw new IllegalArgumentException("wrong argument is productId:" + productId);
 //
 //		for (Order order: orders) {
 //			for (OrderLine orderLine: order) {
 //				if (productId.equals(orderLine.getProduct().getId())) {
 //					orderLine.setQuantity(quantity);
 //					break;
 //				}
 //			}
 //		}
 //	}
 	
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Basket [user=").append(user).append(", orders=").append(orders)
 				.append(", orderDao=").append(orderDao).append(", productDao=").append(productDao)
 				.append(", shopDao=").append(shopDao).append("]");
 		return builder.toString();
 	}
 }
