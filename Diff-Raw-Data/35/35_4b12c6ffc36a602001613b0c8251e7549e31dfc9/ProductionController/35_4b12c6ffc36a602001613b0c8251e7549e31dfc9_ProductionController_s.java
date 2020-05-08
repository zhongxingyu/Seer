 package controller;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.Ingredient;
 import model.Model;
 import model.Order;
 import model.ProductionOrder;
 import model.Recipe;
 import view.KrustyView;
 
 public class ProductionController {
 
 	public static final int PRODUCTION = 0;
 	public static final int FREEZING = 1;
 	public static final int PACKAGING_IN_BAGS = 2;
 	public static final int PACKAGING_IN_CARTONS = 3;
 	public static final int LOADING_ON_PALLETS = 4;
 	
 	private Timer timer;
 	private int currentStageInProduction = PRODUCTION;
 	private KrustyView view;
 	private Model model;
 	private ProductionOrder currentProductionOrder;
 	private Order currentOrder;
 	private ArrayList<ProductionOrder> orders;
 	private SimpleDateFormat sdf;
 	private int currentProductionOrderNumber = Integer.MAX_VALUE;
 	
 	public ProductionController(int runTime, Model model, KrustyView view) {
 		this.view = view;
 		this.model = model;
 		
 		sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
 		timer = new Timer();
 		timer.schedule(new ProductionTask(), 0, (runTime / 5) * 1000 );
 	}
 	
 	class ProductionTask extends TimerTask {
 
 		@Override
 		public void run() {
 			Calendar cal = Calendar.getInstance();
 			if(orders == null || currentProductionOrderNumber >= orders.size()) {
 				currentOrder = model.getNextOrder();
 				if(currentOrder == null) {
 					view.insertToProductionBox(sdf.format(cal.getTime()) + " - No orders to produce");
 					return;
 				}
 				orders = currentOrder.productionOrders;		
 				if(orders.size() > 0) {
 					currentProductionOrderNumber = 0;
 				} else {
 					view.insertToProductionBox(sdf.format(cal.getTime()) + " - No production orders in order number " + Integer.toString(currentOrder.id));
 					return;
 				}
 			}
 			String message = sdf.format(cal.getTime()) + " - Order number " + Integer.toString(currentOrder.id) + ", " ;
 			switch (currentStageInProduction) {
 			case PRODUCTION :
				currentProductionOrder = orders.get(currentProductionOrderNumber);			
 				updateRawMaterialQuantities(currentProductionOrder.recipe);
 				message += currentProductionOrder.recipe.name + " in production";
 				currentStageInProduction++;
 				break;
 			case FREEZING :
 				message += currentProductionOrder.recipe.name + " in freezing";
 				currentStageInProduction++;
 				break;
 			case PACKAGING_IN_BAGS :
 				message += currentProductionOrder.recipe.name + " in packaging in bags";
 				currentStageInProduction++;
 				break;
 			case PACKAGING_IN_CARTONS :
 				message += currentProductionOrder.recipe.name + " in packaging in cartons";
 				currentStageInProduction++;
 				break;
 			case LOADING_ON_PALLETS :
 				message += currentProductionOrder.recipe.name + " in loading on pallets";
 				currentStageInProduction = PRODUCTION;
 				currentProductionOrderNumber++;
 				model.createPallet(currentOrder, currentProductionOrder);
 				break;
 			}
 			view.insertToProductionBox(message);			
 		}
 		
 		private void updateRawMaterialQuantities(Recipe recipe) {
 			for(Ingredient i : recipe.ingredients) {
 				if(!model.isEnoughRawMaterials(i)) {
 					int newQuantity = 100000;
 					model.updateQuantity(i.rawMaterial, newQuantity);
//					Calendar cal = Calendar.getInstance();
					//XXX: The row below makes the application freeze, any ideas why?
//					view.insertToProductionBox(sdf.format(cal.getTime()) + " - " + i.rawMaterial.name + " quantity restored to " + newQuantity + " units");
 				}
 			}
 		}
 	}
 	
 }
