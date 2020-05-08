 package mcontrollers;
 
 import gui.inventory.IInventoryView;
 import gui.inventory.ProductContainerData;
 import gui.product.ProductData;
 
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.Action;
 import model.Action.ActionType;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainer;
 import model.ProductManager;
 
 /**
  * Controller object that acts as a liaison between the model's ProductManager and the GUI
  * view.
  * 
  * @author Matt Hess
  * @version 1.0 CS 340 Group 4 Phase 2
  * 
  */
 public class ProductListener implements Observer {
 
 	IInventoryView view;
 	
 	public ProductListener(IInventoryView view, ProductManager productManager) {
 		this.view = view;
 		productManager.addObserver(this);
 	}
 
 	/**
 	 * Method intended to notify the view when ProductManager sends a "change" notice.
 	 * 
 	 * @param o
 	 *            The object being observed (which has access to the model)
 	 * @param arg
 	 *            Object passed to the view so the view can determine which changes to make
 	 * 
 	 * @pre o != null
 	 * @pre arg != null
 	 * @pre o instanceof ProductManager
 	 * @post true
 	 * 
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		Action action = (Action)arg;
 		Product product = (Product)action.getObject();
 		
 		if(action.getAction().equals(ActionType.CREATE)){
 			ProductContainerData productContainerData = view.getSelectedProductContainer();
 			ProductContainer container = (ProductContainer) productContainerData.getTag();
 			// add the new container to the ones to be displayed
 			container.add(product);
 			Iterator<Product> iter = container.getProductsIterator();
 			ProductData[] products = new ProductData[container.getProductsSize()];
 			
 			int i = 0;
 			while(iter.hasNext()){
 				Product prod = iter.next();
 				ProductData productData = new ProductData();
 				productData.setBarcode(prod.getBarcode());
 				productData.setDescription(prod.getDescription());
 				productData.setShelfLife(String.valueOf(prod.getShelfLife()));
 				productData.setSize(String.valueOf(prod.getSize().getQuantity()));
 				productData.setSupply(String.valueOf(prod.getThreeMonthSupply()));
 				productData.setTag(prod);
 				products[i++] = productData;
				if(product.equals(prod))
					productData.setCount("1");
				else
					productData.setCount(String.valueOf(container.getItemsForProduct(prod).size()));
 			}
 			
 			view.setProducts(products);
 		}
 	}
 }
