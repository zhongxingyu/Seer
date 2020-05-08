 package edu.gatech.statusquo.spacetrader.presenter;
 
 import java.util.HashMap;
 
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.widgets.Shell;
 
 import edu.gatech.statusquo.spacetrader.driver.*;
 import edu.gatech.statusquo.spacetrader.model.*;
 import edu.gatech.statusquo.spacetrader.model.Good.GoodType;
 import edu.gatech.statusquo.spacetrader.view.*;
 import edu.gatech.statusquo.spacetrader.presenter.VitalsPresenter;
 
 public class TradeGoodsPresenter {
 	Shell shell;
 	Driver driver;
 	TradeGoodsView tradeGoodsView;
 	VitalsView vitalsView;
 	Player player;
 	SolarSystem solarSystem;
 	Ship ship;
 	HashMap<GoodType, Integer> marketQuantity;
 	HashMap<GoodType, Integer> marketPrice;
 	final int fuelPrice = 15;
 	/**
 	 * 
 	 * @param s
 	 * @param d
 	 * @param tgv
 	 * @param p
 	 * @param ss
 	 * @param vv
 	 */
 	public TradeGoodsPresenter(Shell s, Driver d, TradeGoodsView tgv, Player p, SolarSystem ss, VitalsView vv) {
 		this.shell = s;
 		this.driver = d;
 		this.tradeGoodsView = tgv;
 		this.vitalsView = vv;
 		this.player = p;
 		this.solarSystem = ss;
 		ship = player.getShip();
 		marketQuantity = solarSystem.getMarketQuantity();
 		marketPrice = solarSystem.getMarketPrice();
 				
 		setListeners();
 		fillTradeGoodsTable();
 	}
 	
 	/**
 	 * sets all trade good listeners
 	 */
 	private void setListeners() {
 		/**
 		 * Mouse listener for the marketplace, allows entering of quantities for buying
 		 */
 		
 		tradeGoodsView.btnBuy.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				String quantText = tradeGoodsView.text.getText();
 				
 				/**
 				 * Throws error message if no amount is entered when the player attempts a transaction
 				 */
 				
 				if (quantText.equals("") || quantText.equals("Enter Qty")) {
 					NotificationsView.list_1.add("Please enter an amount in the text box");
 					return;
 				}
 				int quant = Integer.parseInt(quantText);
 				
 				/**
 				 * Check to make sure a player has selected a good before attempting a transaction 
 				 */
 				
 				if (tradeGoodsView.table_1.getSelection().length == 0) {
 					NotificationsView.list_1.add("Please select an item from the market");
 					return;
 				} else {
 //					int price = Integer.parseInt(tradeGoodsView.table_1.getSelection()[0].getText(1));
 					GoodType goodType = GoodType.valueOf(tradeGoodsView.table_1.getSelection()[0].getText(0));
 					int price = marketPrice.get(goodType);
 					int marketCount = marketQuantity.get(goodType);
 					int cargoCountLeft = ship.countCargoLeft();
 					
 					/**
 					 * Check to make sure the player has enough currency to complete a transaction
 					 */
 				    if(Player.getCurrency() < quant * price){
 						NotificationsView.list_1.add("Sorry, you do not have enough currency to make this purchase");
 						NotificationsView.list_1.select(NotificationsView.list_1.getItemCount() - 1);
 						NotificationsView.list_1.showSelection();
 						return;
 						/**
 						 * Check to make sure enough of quantity is available to purchase
 						 */
 				    } else if(quant > marketCount) {
 						NotificationsView.list_1.add("Sorry, there is not enough of this item in the market.");
 						NotificationsView.list_1.select(NotificationsView.list_1.getItemCount() - 1);
 						NotificationsView.list_1.showSelection();
 						return;
 						
 						/**
 						 * Check to make sure the player has enough room in the cargobay for the new goods
 						 */
 				    } else if(quant > cargoCountLeft){
 						NotificationsView.list_1.add("Sorry, you do not have enough space in your cargo bay.");
 						NotificationsView.list_1.select(NotificationsView.list_1.getItemCount() - 1);
 						NotificationsView.list_1.showSelection();
 						return;
 						
 						/**If checks pass the goods are added to the cargo bay
 						 */
 				    } else {
 				    	ship.addCargo(quant, goodType);
 				    	player.setCurrency(Player.getCurrency() - (quant*price));
 				    	//going to decrement the markets quantity depending qty bought
 				    	//getCurrentQty - qtyBought = newMarketQty, puts(goodtype, newmarketqty)
 				    	int newMarketQty = (marketQuantity.get(goodType)) - quant;
 				    	marketQuantity.put(goodType, newMarketQty);
 				    	fillTradeGoodsTable();
 				    	VitalsPresenter.setPlayerVitals();
 				    	VitalsPresenter.setShipVitals();
 				    }
 				}
 			}
 		});
 
 		/**
 		 * Button listener for selling transactions
 		 */
 		tradeGoodsView.btnSell.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				String quantText = tradeGoodsView.text_1.getText();
 				/**
 				 * Check to make sure an amount is entered
 				 */
 				if (quantText.equals("") || quantText.equals("Enter Qty")) {
 					NotificationsView.list_1.add("Please enter an amount in the text box");
 					return;
 				}
 				int quant = Integer.parseInt(quantText);
 				/**
 				 * Check to make sure an item is selected before attempting transaction
 				 */
 				if (tradeGoodsView.table_1.getSelection().length == 0) {
 					NotificationsView.list_1.add("Please select an item from the market");
 					return;
 					
 				} else {
 //					int price = Integer.parseInt(tradeGoodsView.table_1.getSelection()[0].getText(1));
 					GoodType goodType = GoodType.valueOf(tradeGoodsView.table_1.getSelection()[0].getText(0));
 					int price = marketPrice.get(goodType);
 					int cargoCount = ship.countCargo(goodType);
 					/**
 					 * Check to make sure player has the items available to sell
 					 */
 				    if (quant > cargoCount){
 				    	String msg = "Sorry, you do not have enough " 
 				    			+ tradeGoodsView.table_1.getSelection()[0].getText(0)
 				    			+ " items in your cargo bay to sell";
 						NotificationsView.list_1.add(msg);
 						NotificationsView.list_1.select(NotificationsView.list_1.getItemCount() - 1);
 						NotificationsView.list_1.showSelection();
 						return;
 						/**
 						 * If checks pass the items are removed from cargobay and the market pays the player
 						 */
 				    } else {
 				    	ship.removeCargo(quant, goodType);
 				    	player.setCurrency(Player.getCurrency() + (quant*price));
 	                    //going to increment the markets quantity depending qty sold
                         //getCurrentQty + qtyBought = newMarketQty, puts(goodtype, newmarketqty)
                         int newMarketQty = (marketQuantity.get(goodType)) + quant;
                         marketQuantity.put(goodType, newMarketQty);
                         fillTradeGoodsTable();
 				    	VitalsPresenter.setPlayerVitals();
 				    	VitalsPresenter.setShipVitals();
 				    }
 				}
 			}
 		});
 		
 		tradeGoodsView.btnBuyFuel.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				String quantText = tradeGoodsView.text_2.getText();
 	
 			
 				if (quantText.equals("") || quantText.equals("Enter Qty")) {
 					NotificationsView.list_1.add("Please enter an amount in the text box");
 					return;
 				}
 			
 				int quant = Integer.parseInt(quantText);
 				int fuelLevel = ship.getFuelLevel();
 				int fuelCapacity = ship.getFuelCapacity();
 				
 				
 				if(fuelCapacity < fuelLevel + quant){
 					NotificationsView.list_1.add("This amount of fuel would cause your tank to burst");
 					return;
 				}
 				
 				else if(Player.getCurrency() < quant * fuelPrice){
 					NotificationsView.list_1.add("Purchasing this amount of fuel would put you in the poor house");
 					return;
 				}
 				
 				else{
 					ship.addFuel(quant);
 			    	player.setCurrency(Player.getCurrency() - (quant*fuelPrice));
 			    	VitalsPresenter.setPlayerVitals();
 				}														
 			}	
 		});
 		
 		/**
 		 * creates mouse listeners
 		 */
 
 		tradeGoodsView.text.addMouseListener(new MouseAdapter() {
 		    @Override
 		    public void mouseDown(MouseEvent e) {
 		    	tradeGoodsView.text.setText("");
 		    }
 		});
 
 		tradeGoodsView.text_1.addMouseListener(new MouseAdapter() {
 		    @Override
 		    public void mouseDown(MouseEvent e) {
 		    	tradeGoodsView.text_1.setText("");
 		    }
 		});
 	}
 	
 	/**
 	 * Fills market with items, quantities and prices based on criteria
 	 */
 	private void fillTradeGoodsTable() {
		HashMap<GoodType, Integer> marketPrice = solarSystem.getMarketPrice();
		HashMap<GoodType, Integer> marketQuantity = solarSystem.getMarketQuantity();
 		
 		String[] water = {"WATER", Integer.toString(marketPrice.get(GoodType.WATER)), Integer.toString(marketQuantity.get(GoodType.WATER)), "N/A"};
 		tradeGoodsView.waterItem.setText(water);
 		String[] fur = {"FUR", Integer.toString(marketPrice.get(GoodType.FUR)), Integer.toString(marketQuantity.get(GoodType.FUR)), "N/A"};
 		tradeGoodsView.furItem.setText(fur);
 		String[] food = {"FOOD", Integer.toString(marketPrice.get(GoodType.FOOD)), Integer.toString(marketQuantity.get(GoodType.FOOD)), "N/A"};
 		tradeGoodsView.foodItem.setText(food);
 		String[] ore = {"ORE", Integer.toString(marketPrice.get(GoodType.ORE)), Integer.toString(marketQuantity.get(GoodType.ORE)), "N/A"};
 		tradeGoodsView.oreItem.setText(ore);
 		String[] firearms = {"FIREARM", Integer.toString(marketPrice.get(GoodType.FIREARM)), Integer.toString(marketQuantity.get(GoodType.FIREARM)), "N/A"};
 		tradeGoodsView.firearmsItem.setText(firearms);
 		String[] medicine = {"MEDICINE", Integer.toString(marketPrice.get(GoodType.MEDICINE)), Integer.toString(marketQuantity.get(GoodType.MEDICINE)), "N/A"};
 		tradeGoodsView.medicineItem.setText(medicine);
 		String[] machines = {"MACHINE", Integer.toString(marketPrice.get(GoodType.MACHINE)), Integer.toString(marketQuantity.get(GoodType.MACHINE)), "N/A"};
 		tradeGoodsView.machinesItem.setText(machines);
 		String[] narcotics = {"NARCOTIC", Integer.toString(marketPrice.get(GoodType.NARCOTIC)), Integer.toString(marketQuantity.get(GoodType.NARCOTIC)), "N/A"};
 		tradeGoodsView.narcoticsItem.setText(narcotics);
 		String[] robots = {"ROBOT", Integer.toString(marketPrice.get(GoodType.ROBOT)), Integer.toString(marketQuantity.get(GoodType.ROBOT)), "N/A"};
 		tradeGoodsView.robotsItem.setText(robots);
 	}
 }
