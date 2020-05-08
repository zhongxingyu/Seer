 package logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.sql.Delete;
 
 import com.ning.http.client.oauth.ConsumerKey;
 
 import models.Capacity;
 import models.Component;
 import models.DispositionManufacture;
 import models.DispositionOrder;
 import models.DistributionWish;
 import models.Item;
 import models.OpenOrder;
 import models.ProductionOrder;
 import models.User;
 import models.WaitingList;
 import models.Workplace;
 import play.Logger;
 import play.test.Fixtures;
 import utils.ItemHelper;
 
 public class ApplicationLogic {
 
 	public static void resetData() {
 		Logger.info("reset Data");
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("initial-items.yml", "initial-workplaces.yml", "ItemTime.yml", "initial-dispositionOrder.yml", "initial-productionPlan.yml",
 				"initial-components.yml");
 	}
 
 	public static void wishToPlan() {
 		List<DistributionWish> wishList = DistributionWish.findAll();
 		if (wishList == null || wishList.isEmpty()) {
 			wishList = new ArrayList<>();
 			List<Item> pItems = Item.find("byType", "P").fetch();
 			for (Item item : pItems) {
 				DistributionWish wish = new DistributionWish();
 				wish.item = item.itemId;
 				wish.save();
 				wishList.add(wish);
 			}
 		}
 		for (DistributionWish wish : wishList) {
 			DispositionManufacture disp = DispositionManufacture.find("byItem", wish.item).first();
 			disp.distributionWish = wish.period0;
 			disp.save();
 			// Logger.info("wishToPlan %s", disp);
 		}
 		// calcProductionPlan();
 	}
 
 	public static void calcProductionPlan() {
 		// Logger.info("setDependencies");
 		List<DispositionManufacture> disps = DispositionManufacture.findAll();
 		DispositionManufacture parent = new DispositionManufacture();
 		for (int i = 0, length = disps.size(); i < length; i++) {
 			DispositionManufacture disp = disps.get(i);
 			Item item = Item.find("byItemId", disp.item).first();
 			if ("P".equals(item.type)) {
 				parent = new DispositionManufacture();
 			} else {
 				disp.distributionWish = parent.production;
 			}
 			boolean mulitpleItem = item.itemNumber == 26 || item.itemNumber == 16 || item.itemNumber == 17 ? true : false;
			if (mulitpleItem) {
				disp.stock = item.amount / 3;
			} else {
				disp.stock = item.amount;
			}
				
			
 			// TODO in item model yml aufnehmen
 			disp.safetyStock = disp.safetyStock > 0 ? disp.safetyStock : 100;
 			disp.parentWaitingList = parent.waitingList;
 			disp.inWork = 0;
 			disp.waitingList = 0;
 			List<WaitingList> wL = WaitingList.find("byItem", disp.item).fetch();
 			for (WaitingList waitingList : wL) {
 				Workplace wP = Workplace.find("byWorkplaceId", waitingList.workplace).first();
 				if (wP.inWork != null && wP.inWork.equals(waitingList.waitingListId)) {
 					if (mulitpleItem) {
 						disp.inWork += waitingList.amount / 3;
 					} else {
 						disp.inWork += waitingList.amount;
 					}
 				} else {
 					if (mulitpleItem) {
 						disp.waitingList += waitingList.amount / 3;
 					} else {
 						disp.waitingList += waitingList.amount;
 					}
 				}
 			}
 			disp.production = disp.distributionWish + disp.parentWaitingList + disp.safetyStock - disp.stock - disp.waitingList - disp.inWork;
 			if (disp.production < 0) {
 				disp.production = 0;
 			}
 			disp.save();
 			if (disp.itemChilds != null && disp.itemChilds.length > 0) {
 				parent = disp;
 			}
 //			Logger.info("disp: %s", disp);
 		}
 	}
 
 	public static void planToOrder() {
 		
 		List<DispositionManufacture> plans = DispositionManufacture.find("order by itemNumber asc").fetch();
 //		List<DispositionManufacture> plans = DispositionManufacture.findAll();
 		Workplace.deleteAllProductionPlanLists();
 		Logger.info("planToOrder %s", ProductionOrder.findAll().size());
 		ProductionOrder.deleteAll();
 		int no = 0;
 		for (DispositionManufacture dispo : plans) {
 			ProductionOrder prodOrder = ProductionOrder.find("byItem", dispo.item).first();
 			Item item = dispo.getItemAsObject();
 			if (prodOrder != null) {
 //				Logger.info("pOrder not null: %s", prodOrder);
 				prodOrder.amount += dispo.production;
 			} else {
 				prodOrder = new ProductionOrder();
 				prodOrder.item = item.itemId;
 //				prodOrder.itemNumber = item.itemNumber;
 				prodOrder.orderNumber = no;
 				no++;
 				prodOrder.amount = dispo.production;
 				prodOrder.assignToWorkplaces();
 //				Logger.info("pOrder null: %s", prodOrder);
 			}
 			
 			prodOrder.save();
 			
 		}
 	}
 
 	public static void calculateCapacity() {
 
 		//Gib mir alle Arbeitsplätze
 		List<Workplace> places = Workplace.findAll();
 		Capacity.deleteAll();
 		Logger.info("calculateCapacity: %s", places.size());
 		
 		//Rechne für jeden Arbeitsplatz die Kapazität aus
 		for (Workplace workplace : places) {
 			
 			//Gibt es bereits ein Kapazitätsobjekt zu dem Arbeitsplatz
 			Capacity cap = Capacity.find("byWorkplace", workplace.workplaceId).first();
 			//Falls nicht, ein neues Anlegen
 			if (cap == null) {
 				cap = new Capacity();
 				cap.workplace = workplace.workplaceId;
 //				Logger.info("create new capacity %s", cap);
 			}
 			//Alles auf 0 setzen
 			cap.time = 0;
 			cap.setupTime = 0;
 			cap.totaltime = 0;
 			cap.overtime = 0;
 			cap.shift = 0;
 
 			//Die Zeiten für die Warteschlangen errechnen und hinzufügen, falls vorhanden
 			List<WaitingList> wList = workplace.getWaitingListAsObjectList();
 			if (wList != null && !wList.isEmpty()) {
 				// Logger.info("wList: %s", wList.size());
 				int time = 0;
 				int setup = 0;
 				//Für jedes Item in der Warteschlange Zeit und Rüstzeit hinzuaddieren
 				for (WaitingList wait : wList) {			
 					time += wait.timeneed;
 					setup += ItemHelper.getSetupTime(cap.workplace, wait.item);
 				}
 				// Logger.info("WaitL: %s", time);
 				cap.time += time;
 				cap.setupTime += setup;
 			}
 
 			//Zeit für das in Arbeit befindliche Item hinzuaddieren
 			WaitingList inWork = workplace.getInWorkAsObject();
 
 			if (inWork != null) {
 				// Logger.info("inWork: %s", inWork.amount);
 				cap.time += inWork.timeneed;
 			}
 
 			//Die Zeiten für die Produktionsaufträge errechnen und hinzufügen.
 			List<ProductionOrder> pOrders = workplace.getProductionPlanListAsObjectList();
 			if (pOrders != null && !pOrders.isEmpty()) {
 				// Logger.info("pOrders: %s", pOrders.size());
 				int time = 0;
 				for (ProductionOrder productionOrder : pOrders) {
 					time += productionOrder.amount * ItemHelper.getProcessTime(cap.workplace, productionOrder.item);
 					cap.setupTime += ItemHelper.getSetupTime(cap.workplace, productionOrder.item);
 				}
 				// Logger.info("pOrders: %s", time);
 				cap.time += time;
 			}
 
 			cap.totaltime = cap.time + cap.setupTime;
 
 			if (cap.totaltime == 0) {
 				cap.shift = 0;
 				cap.overtime = 0;
 			} else if (cap.totaltime <= 3600) {
 				cap.shift = 1;
 				if (cap.totaltime < 2400) {
 					cap.overtime = 0;
 				} else {
 					cap.overtime = cap.totaltime - 2400;
 				}
 			} else if (cap.totaltime <= 6000) {
 				cap.shift = 2;
 				if (cap.totaltime < 4800) {
 					cap.overtime = 0;
 				} else {
 					cap.overtime = cap.totaltime - 4800;
 				}
 			} else if (cap.totaltime > 6000) {
 				cap.shift = 3;
 				if (cap.totaltime < 7200) {
 					cap.overtime = 0;
 				} else {
 					cap.overtime = cap.totaltime - 7200;
 				}
 			}
 			//overtime per day
 			cap.overtime = (int)Math.ceil(cap.overtime / 5);
 			cap.save();
 //			Logger.info("capacity %s", cap);
 		}
 	}
 	
 	public static void calculateDisposition() {	
 		calculateConsumption();
 		List<User> users = User.findAll();
 		int actPeriod = Integer.valueOf(users.get(0).period);
 		List<DispositionOrder> dispoOrders = DispositionOrder.findAll();
 		for (DispositionOrder dispoOrder : dispoOrders) {
 			//TODO calculateExpectedArrival Methode dynamisch statt hardcoded
 			dispoOrder.expectedArrival = calculateExpectedArrival("recommended", dispoOrder.item, 5);
 			Item item = Item.find("byItemId", dispoOrder.item).first();
 			dispoOrder.amount = item.amount;
 			int stock = item.amount;
 			int amt0 = dispoOrder.consumptionPeriod0;
 			int amt1 = amt0 + dispoOrder.consumptionPeriod1;
 			int amt2 = amt1 + dispoOrder.consumptionPeriod2;
 			int amt3 = amt2 + dispoOrder.consumptionPeriod3;
 			
 			//remove inward amount from consumption amount
 			List<OpenOrder> openOrders = OpenOrder.find("byItem", dispoOrder.item).fetch();
 			for (OpenOrder order : openOrders) {
 				//TODO calculateExpectedArrival Methode dynamisch statt hardcoded
 				order.expectedArrival = order.orderPeriod + calculateExpectedArrival("recommended", dispoOrder.item, order.mode);
 				double delta = order.expectedArrival - actPeriod;			
 				if (delta <= 1) {
 					//amt0 = (order.amount > amt0) ? 0 : (amt0 - order.amount);
 					amt0 -= order.amount;
 				} else if (delta <= 2) {
 					//amt1 = (order.amount > amt1) ? 0 : (amt1 - order.amount);
 					amt1 -= order.amount;
 				} else if (delta <= 3) {
 					//amt2 = (order.amount > amt2) ? 0 : (amt2 - order.amount);
 				} else {
 					//amt3 = (order.amount > amt3) ? 0 : (amt3 - order.amount);
 					amt2 -= order.amount;
 				}
 			}
 			
 			//Set futureStock after consumption and expected orders
 			item.futureStock0 = stock - amt0;
 			item.futureStock1 = stock - amt1;
 			item.futureStock2 = stock - amt2;
 			item.futureStock3 = stock - amt3;
 			
 			int period = -1;
 			int quantity = 0;
 			//quantity anpassen?!
 			if (stock == 0) {
 				period = 0;
 				quantity = dispoOrder.consumptionPeriod0;
 			} else if (stock - amt0 <= 0) {
 				period = 0;
 				//quantity = amt0;
 				quantity = dispoOrder.consumptionPeriod0;
 			} else if (stock - amt1 <= 0 && dispoOrder.expectedArrival >= (1 + actPeriod)) {
 				period = 1;
 				//quantity = amt1;
 				quantity = dispoOrder.consumptionPeriod1 + dispoOrder.consumptionPeriod0;
 			} else if (stock - amt2 <= 0 && dispoOrder.expectedArrival >= (2 + actPeriod)) {
 				period = 2;
 				//quantity = amt2;
 				quantity = dispoOrder.consumptionPeriod2 + dispoOrder.consumptionPeriod1 + dispoOrder.consumptionPeriod0;
 			} else if (stock - amt3 <= 0 && dispoOrder.expectedArrival >= (3 + actPeriod)) {
 				period = 3;
 				//quantity = amt3;
 				quantity = dispoOrder.consumptionPeriod3 + dispoOrder.consumptionPeriod2 + dispoOrder.consumptionPeriod1 + dispoOrder.consumptionPeriod0;
 			}
 			
 			if (period == -1) continue;
 			
 			//Bestellmenge = Diskontmenge
 			if (quantity < dispoOrder.discount) {
 				dispoOrder.quantity = dispoOrder.discount;
 			} else {
 				dispoOrder.quantity = quantity;
 			}
 			
 			//Wenn Lieferzeit zu lang, dann Express Bestellung
 			if (Math.round(dispoOrder.expectedArrival) > (period + actPeriod)) {
 				dispoOrder.mode = 4;
 				dispoOrder.expectedArrival = calculateExpectedArrival("recommended", dispoOrder.item, 4);
 			} else {
 				dispoOrder.mode = 5;
 			}
 			
 			//add new orders to future stock
 			
 			
 			dispoOrder.save();
 			
 		}
 	}
 
 	public static void calculateConsumption() {
 		List<DispositionOrder> dispoOrders = DispositionOrder.findAll();
 		for (DispositionOrder dispoOrder : dispoOrders) {
 			// aktueller Verbrauch
 			List<Component> components = Component.find("byItem", dispoOrder.item).fetch();
 			int actConsumption = 0;
 			for (Component component : components) {
 				DispositionManufacture dm = DispositionManufacture.find("byItem", component.parent).first();
 				if (dm != null) {
 					actConsumption += dm.production * component.amount;
 				}
 			}
 			dispoOrder.consumptionPeriod0 = actConsumption;
 
 			List<WaitingList> waitingLists = WaitingList.find("byItem", dispoOrder.item).fetch();
 			if (waitingLists != null && waitingLists.size() > 0) {
 				for (WaitingList waiting : waitingLists) {
 					if (waiting.inWork == false) {
 						dispoOrder.consumptionPeriod0 += waiting.amount;
 					}
 				}
 
 			}
 
 			// Verbrauch Prognosen
 			if (dispoOrder.usedP1 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem", "P1").first();
 				dispoOrder.consumptionPeriod1 = wish.period1 * dispoOrder.usedP1;
 				dispoOrder.consumptionPeriod2 = wish.period2 * dispoOrder.usedP1;
 				dispoOrder.consumptionPeriod3 = wish.period3 * dispoOrder.usedP1;
 			}
 
 			if (dispoOrder.usedP2 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem", "P2").first();
 				dispoOrder.consumptionPeriod1 = wish.period1 * dispoOrder.usedP2;
 				dispoOrder.consumptionPeriod2 = wish.period2 * dispoOrder.usedP2;
 				dispoOrder.consumptionPeriod3 = wish.period3 * dispoOrder.usedP2;
 			}
 
 			if (dispoOrder.usedP3 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem", "P3").first();
 				dispoOrder.consumptionPeriod1 = wish.period1 * dispoOrder.usedP3;
 				dispoOrder.consumptionPeriod2 = wish.period2 * dispoOrder.usedP3;
 				dispoOrder.consumptionPeriod3 = wish.period3 * dispoOrder.usedP3;
 			}
 
 			dispoOrder.save();
 		}
 	}
 	
 	public static double calculateExpectedArrival(String method, String itemId, int mode) {
 		List<User> users = User.findAll();
 		int period = Integer.valueOf(users.get(0).period);
 		double expectedArrival = 0.0;
 		DispositionOrder dispoOrder = DispositionOrder.find("byItem", itemId).first();
 		expectedArrival = 0.2 + period;
 		//If express order half delivery time and no variance
 		if (mode == 4) {
 			expectedArrival += (dispoOrder.deliveryTime / 2); 
 		} else {
 			expectedArrival += dispoOrder.deliveryTime;
 			switch (method) {
 				case "optimistic": {break;}
 				case "riskaverse": {expectedArrival += dispoOrder.deliveryVariance; break;}
 				case "recommended": {expectedArrival += (dispoOrder.deliveryVariance * 0.75); break;}
 			}
 		}
 //		Logger.info("Expected arrival for %s: %s", dispoOrder.item, expectedArrival);
 		return expectedArrival;
 	}
 
 }
