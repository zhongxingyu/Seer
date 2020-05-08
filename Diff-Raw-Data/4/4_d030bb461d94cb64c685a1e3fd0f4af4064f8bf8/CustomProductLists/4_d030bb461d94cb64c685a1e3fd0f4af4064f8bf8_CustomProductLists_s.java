 package imat.backend;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.ait.dat215.project.IMatDataHandler;
 import se.chalmers.ait.dat215.project.Product;
 import se.chalmers.ait.dat215.project.ProductCategory;
 
 public class CustomProductLists {
 	public static final IMatDataHandler IDH = IMatDataHandler.getInstance();
 	private static boolean isGenerated;
 
 	private static List<Product> potatoes = new ArrayList<Product>();
 	private static List<Product> bake = new ArrayList<Product>();
 	private static List<Product> rice = new ArrayList<Product>();
 	private static List<Product> spices = new ArrayList<Product>();
 	private static List<Product> home = new ArrayList<Product>();
 
 	public static void generateCustomLists() {
 		Random generator = new Random();
 		potatoes = IDH.findProducts("potatis");
 		bake = IDH.findProducts("mjöl");
 		bake.add(IDH.getProduct(95));
 		spices = IDH.getProducts(ProductCategory.HERB);
 		spices.add(IDH.getProduct(94));
 		rice = IDH.findProducts("ris");
 		for (int i = 0; i < 8; i++) {
			home.add(IDH.getProduct(generator.nextInt(149)));
 		}
 		Collections.sort(potatoes, new ProductNameSort());
 		Collections.sort(bake, new ProductNameSort());
 		Collections.sort(spices, new ProductNameSort());
 		Collections.sort(rice, new ProductNameSort());
 		Collections.sort(bake, new ProductNameSort());
 	}
 
 	public static List<Product> getProductList(String s) {
 		if(!isGenerated) {
 			generateCustomLists();
 			isGenerated = true;
 		}
 		switch (s) {
 		case "Potatis":
 			return potatoes;
 		case "Ris":
 			return rice;
 		case "Baka":
 			return bake;
 		case "Kryddor":
 			return spices;
 		case "Hem":
 			return home;
 		default:
 			return null;
 		}
 	}
 
 }
