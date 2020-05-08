 package com.ngdb.web.components.shopitem;
 
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import com.ngdb.entities.shop.ShopItem;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 
 public class SoldButton {
 
 	@Property
 	@Parameter
 	private ShopItem shopItem;
 
 	@InjectPage
 	private com.ngdb.web.pages.Market marketPage;
 
 	@Inject
 	private CurrentUser currentUser;
 
 	@Property
 	@Parameter
 	private boolean asButton;
 
 	@CommitAfter
 	Object onActionFromSold(ShopItem shopItem) {
 		shopItem.sold();
 		marketPage.setUser(currentUser.getUser());
 		return marketPage;
 	}
 }
