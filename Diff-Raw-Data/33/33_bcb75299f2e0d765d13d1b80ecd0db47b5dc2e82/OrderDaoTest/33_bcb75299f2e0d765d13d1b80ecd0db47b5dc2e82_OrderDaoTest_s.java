 package com.sk.domain.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.Set;
 
import org.hamcrest.collection.IsCollectionContaining;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.sk.domain.Category;
 import com.sk.domain.Order;
 import com.sk.domain.Product;
 import com.sk.domain.ProductWithQuantity;
 import com.sk.domain.ShoppingCart;
 import com.sk.util.BaseIntegration;
 import com.sk.util.builder.CategoryBuilder;
 import com.sk.util.builder.OrderBuilder;
 import com.sk.util.builder.ProductBuilder;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
 
 public class OrderDaoTest extends BaseIntegration {
 
 	@Autowired
 	private OrderDao orderDao;
 
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm ss");
 
 	@Test
 	public void shouldPersistOrder() {
 		Date now = new Date();
 
		// Replace with shopping cart builder when cenk pushes shop.cart.builder
		ShoppingCart shoppingCart = new ShoppingCart();
 		Category category = new CategoryBuilder().persist(getSession());
 		Product product1 = new ProductBuilder().category(category).persist(getSession());
 		Product product2 = new ProductBuilder().category(category).persist(getSession());
 
		shoppingCart.getItems().add(new ProductWithQuantity(product1, 1));
		shoppingCart.getItems().add(new ProductWithQuantity(product2, 2));
		// //////////////////////////////////////////////////////////////////////
 
 		Order toPersist = new OrderBuilder().shoppingCart(shoppingCart).orderDate(now).build();
 
 		toPersist = orderDao.persist(toPersist);
 
 		flushAndClear();
 
 		Order fromDb = reget(toPersist);
 
 		assertThat(dateFormat.format(fromDb.getOrderDate()), equalTo(dateFormat.format(now)));
 		
 		assertThat(fromDb.getShoppingCart().getItems().size(), equalTo(2));
 		assertThat(fromDb.getShoppingCart().getItems(), equalTo(toPersist.getShoppingCart().getItems()));
 
 	}
 
 }
