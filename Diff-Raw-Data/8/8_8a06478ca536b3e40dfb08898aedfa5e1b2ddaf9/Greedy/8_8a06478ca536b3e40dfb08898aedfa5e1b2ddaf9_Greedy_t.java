 /**
  * @author Luuk Holleman
  * @date 15 april
  */
 package tspAlgorithm;
 
 import java.util.ArrayList;
 
 import order.Location;
 import order.Product;
 
 public class Greedy extends TSPAlgorithm {
 	public static String name = "Greedy";
 	/**
 	 *  de gegeven product, alleen de producten die nog niet in de route staatn
 	 *  dus aan het eind is deze arraylist leeg
 	 */
 	private ArrayList<Product> products = new ArrayList<Product>();
 	
 	// de locatie van de robot, de robot start op 0, 0
 	private Location location = new Location(0, 0);
 	
 	/**
 	 * De berekende route, wordt gevuld met objecten uit products
 	 */
 	private ArrayList<Product> route = new ArrayList<Product>();
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public ArrayList<Product> calculateRoute(ArrayList<Product> products, int numberOfRobots, int currentRobot) {
 		this.products = splitOrder(products, numberOfRobots, currentRobot);
 		
 		this.route = new ArrayList<Product>();
 		
 		while(nextNode(this.products));
 		
 		return this.route;
 	}
 	
 	/**
 	 * Berekent het dichtsbijzijnde product
 	 * verwijderd het product uit products zodat het niet meer meegenomen wordt
 	 * 
 	 * @param products
 	 * @return product
 	 */
 	private boolean nextNode(ArrayList<Product> products)
 	{
 		// de arraylist is leeg, niks te berekenen. return false
 		if(products.size() == 0) return false;
 		float minDistance = 0;
 		Product minProduct = null;
 		
 		// we lopen nu elk product af en berekenen de afstand. de kortste wordt opgeslagen
 		for(Product product : products) {
 			float distance = location.getDistanceTo(product.getLocation());
 			
 			// als minDistance is de eerste keer, dan moet hij altijd geset worden.
 			// de andere statement is als de net berekende distance korter is dan de vorige
 			if(minDistance == 0 || minDistance > distance) {
 				minDistance = distance;
 				minProduct = product;
				
				// de robot is nu op de locatie van dit product, update de locatie
				location = product.getLocation();	
			}		
 		}
 		
 		// we hebben het dichtsbijzijnde product, voeg hem toe aan de route, verwijderen van de nog te berekenen producten
 		route.add(minProduct);
 		products.remove(minProduct);
 		
 		// we hebben een node! return true
 		return true;
 		
 	}
 
 }
