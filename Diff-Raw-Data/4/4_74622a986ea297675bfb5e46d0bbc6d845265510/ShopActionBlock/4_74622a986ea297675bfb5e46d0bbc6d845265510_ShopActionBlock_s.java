 package com.ngdb.web.components.shopitem;
 
 import com.ngdb.entities.shop.ShopItem;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 
 public class ShopActionBlock {
 
 	@Inject
 	private CurrentUser currentUser;
 
 	@Property
 	@Parameter
 	private ShopItem shopItem;
 
 	@Inject
 	private Session session;
 
 	@Property
 	@Parameter(required = false)
 	private boolean asButton;
 
 	public User getUser() {
 		return currentUser.getUser();
 	}
 
 	public boolean isBuyable() {
 		return currentUser.canBuy(getShopItemFromDb());
 	}
 
 	public boolean isSoldable() {
 		return currentUser.canMarkAsSold(getShopItemFromDb());
 	}
 
 	public boolean isRemoveable() {
 		return currentUser.canRemove(getShopItemFromDb());
 	}
 
 	public boolean isEditable() {
 		return currentUser.canEdit(getShopItemFromDb());
 	}
 
 	private ShopItem getShopItemFromDb() {
 		return (ShopItem) session.load(ShopItem.class, shopItem.getId());
 	}
 
 }
