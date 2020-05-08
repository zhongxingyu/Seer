 package ca.ubc.cpsc310.gitlab.client.user;
 
 import java.util.LinkedList;
 
 import java.util.List;
 
 import ca.ubc.cpsc310.gitlab.client.products.ProductItem;
 
 public class User implements IUser {
 
 	
 	private static final long serialVersionUID = -4678920906536621479L;
 	
 	private List<ProductItem> shoppingCart = new LinkedList<ProductItem>();
 	private List<ProductItem> wishList = new LinkedList<ProductItem>();
 	
 	private String name;
 	private String language;
 	
 	public class User implements IUser {
 
 		
 		private static final long serialVersionUID = -4678920906536621479L;
 		
 		private ArrayList<ProductItem> shoppingCart = new ArrayList<ProductItem>();
 		private ArrayList<ProductItem> wishList = new ArrayList<ProductItem>();
 		
 		private String name;
 		private String language;
 
 	
 	public User()
 	{
 		
 	}
 	
 	@Override
 	public String getLanguage() {
 		return language;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public List<ProductItem> getWishList() {
		return shoppingCart;
 	}
 
 	@Override
 	public List<ProductItem> getShoppingCart() {
		return wishList;
 	}
 
 	@Override
 	public void setLanguage(String language) {
 		this.language = language;
 		
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public void addItemToWishList(ProductItem o) {
 		this.wishList.add(o);
 		
 	}
 	@Override
 	
 	public void removeItemFromWishList(ProductItem o) {
 		this.wishList.remove(o);
 	}
 	
 	@Override
 	public void addItemToShoppingCart(ProductItem o) {
 		this.shoppingCart.add(o);
 	}
 
 	@Override
 	public void removeItemFromShoppingCart(ProductItem o) {
 		this.shoppingCart.remove(o);
 	}
 }
