 package logic;
 
 import java.util.List;
 
 import models.Capacity;
 import models.Component;
 import models.DispositionManufacture;
 import models.DispositionOrder;
 import models.DistributionWish;
 import models.Item;
 import models.ProductionOrder;
 import models.WaitingList;
 import models.Workplace;
 import play.Logger;
 import play.test.Fixtures;
 import utils.ItemHelper;
 
 public class ApplicationLogic {
 
 	public static void resetData() {
 		Logger.info("reset Data");
 		Fixtures.deleteAllModels();
 		Fixtures.loadModels("initial-items.yml", "initial-workplaces.yml", "ItemTime.yml", "initial-dispositionOrder.yml", "initial-productionPlan.yml", "initial-components.yml");
 	}
 
 	public static void wishToPlan() {
 		List<DistributionWish> wishList = DistributionWish.findAll();
 		for (DistributionWish wish : wishList) {
 			DispositionManufacture disp = DispositionManufacture.find("byItem", wish.item).first();
 			disp.distributionWish = wish.period0;
 			disp.save();
 			Logger.info("wishToPlan %s", disp);
 		}
 		Parser.setDependencies();
 	}
 
 	public static void planToOrder() {
 		List<DispositionManufacture> plans = DispositionManufacture.findAll();
 		Workplace.deleteAllProductionPlanLists();
 		for (DispositionManufacture dispo : plans) {
 			ProductionOrder prodOrder = ProductionOrder.find("byItem", dispo.item).first();
 			Item item = dispo.getItemAsObject();
 			if (prodOrder != null) {
 				prodOrder.amount += dispo.production;
 			} else {
 				prodOrder = new ProductionOrder();
 				prodOrder.item = item.itemId;
 				prodOrder.orderNumber = item.itemNumber;
 				prodOrder.amount = dispo.production;
 			}
 			prodOrder.save();
 			prodOrder.assignToWorkplaces();
 		}
 	}
 
 	public static void calculateCapacity() {
 		List<Workplace> places = Workplace.findAll();
 		for (Workplace workplace : places) {
 			Capacity cap = Capacity.find("byWorkplace", workplace.workplaceId).first();
 			if (cap == null) {
 				cap = new Capacity();
 				cap.workplace = workplace.workplaceId;
 			}
 
 			List<WaitingList> wList = workplace.getWaitingListAsObjectList();
 			if (wList != null && !wList.isEmpty()) {
 				for (WaitingList wait : wList) {
 					cap.time += wait.timeneed;
 					cap.setupTime += ItemHelper.getSetupTime(cap.workplace, wait.item);
 				}
 			}
 
 			WaitingList inWork = workplace.getInWorkAsObject();
 			if (inWork != null) {
 				cap.time += inWork.timeneed;
 			}
 
 			List<ProductionOrder> pOrders = workplace.getProductionPlanListAsObjectList();
 			if (pOrders != null && !pOrders.isEmpty()) {
 				for (ProductionOrder productionOrder : pOrders) {
 					cap.time += productionOrder.amount * ItemHelper.getProcessTime(cap.workplace, productionOrder.item);
 					cap.setupTime += ItemHelper.getSetupTime(cap.workplace, productionOrder.item);
 				}
 			}
 
 			cap.totaltime = cap.time + cap.setupTime;
 
			if (cap.totaltime <= 3600) {
				cap.shift = 1;
				cap.overtime = cap.totaltime - 2400;
 			}
			if (cap.totaltime <= 4800) {
 				cap.shift = 2;
				cap.overtime = cap.totaltime - 3600;
			}
			if (cap.totaltime > 4800) {
 				cap.shift = 3;
				cap.overtime = cap.totaltime - 4800;
 			}
 			cap.save();
 		}
 	}
 	
 	public static void calculateDisposition() {		
 		List<DispositionOrder> dispoOrders = DispositionOrder.findAll();
 		for (DispositionOrder dispoOrder : dispoOrders) {		
 			//aktueller Verbrauch			
 			List<Component> components = Component.find("byItem", dispoOrder.item).fetch();
 			for (Component component : components) {
 				DispositionManufacture dm = DispositionManufacture.find("byItem", component.parent).first();
 				if (dm != null) {
 					dispoOrder.consumptionPeriod0 += dm.production * component.amount;
 				}
 			}
 			
 			List<WaitingList> waitingLists = WaitingList.find("byItem", dispoOrder.item).fetch();
 			if (waitingLists != null && waitingLists.size() > 0) {
 				for (WaitingList waiting : waitingLists) {
 					if (waiting.inWork == false){
 						dispoOrder.consumptionPeriod0 += waiting.amount;
 					}	
 				}
 				
 			}
 			
 			//Verbrauch Prognosen
 			if (dispoOrder.usedP1 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem", "P1").first();
 				dispoOrder.consumptionPeriod1 += wish.period1 * dispoOrder.usedP1;
 				dispoOrder.consumptionPeriod2 += wish.period2 * dispoOrder.usedP1;
 				dispoOrder.consumptionPeriod3 += wish.period3 * dispoOrder.usedP1;
 			}
 			
 			if (dispoOrder.usedP2 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem","P2").first();
 				dispoOrder.consumptionPeriod1 += wish.period1 * dispoOrder.usedP2;
 				dispoOrder.consumptionPeriod2 += wish.period2 * dispoOrder.usedP2;
 				dispoOrder.consumptionPeriod3 += wish.period3 * dispoOrder.usedP2;
 			}
 			
 			if (dispoOrder.usedP3 > 0) {
 				DistributionWish wish = DistributionWish.find("byItem","P3").first();
 				dispoOrder.consumptionPeriod1 += wish.period1 * dispoOrder.usedP3;
 				dispoOrder.consumptionPeriod2 += wish.period2 * dispoOrder.usedP3;
 				dispoOrder.consumptionPeriod3 += wish.period3 * dispoOrder.usedP3;
 			}
 			
 			dispoOrder.save();
 		}
 	}
 
 }
