 package model.undo;
 
import java.io.Serializable;

 import model.Product;
 import model.ProductContainer;
 import model.ProductManager;
 import model.ProductQuantity;
 import model.Unit;
 
public class AddProduct implements Command, Serializable {
 	private final ProductManager productManager;
 	private final float quantity;
 	private final Unit unit;
 	private final String barcode;
 	private final String description;
 	private final int shelfLife;
 	private final int threeMonthSupply;
 	private ProductContainer container;
 	private Product product;
 
 	public AddProduct(String barcode, String description, int shelfLife, int threeMonthSupply,
 			float quantity, Unit unit, ProductManager productManager) {
 		super();
 		this.productManager = productManager;
 		this.quantity = quantity;
 		this.unit = unit;
 		this.barcode = barcode;
 		this.description = description;
 		this.shelfLife = shelfLife;
 		this.threeMonthSupply = threeMonthSupply;
 		container = null;
 		product = null;
 	}
 
 	@Override
 	public void execute() {
 		ProductQuantity pq = new ProductQuantity(quantity, unit);
 		product = new Product(barcode, description, shelfLife, threeMonthSupply, pq,
 				productManager);
 		if (container.canAddProduct(product.getBarcode())) {
 			container.add(product);
 		}
 	}
 
 	public String getBarcode() {
 		return barcode;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public void setContainer(ProductContainer container) {
 		this.container = container;
 	}
 
 	@Override
 	public void undo() {
 		if (container.canRemove(product)) {
 			container.remove(product);
 		}
 		if (product.getProductContainers().isEmpty())
 			productManager.unmanage(product);
 	}
 }
