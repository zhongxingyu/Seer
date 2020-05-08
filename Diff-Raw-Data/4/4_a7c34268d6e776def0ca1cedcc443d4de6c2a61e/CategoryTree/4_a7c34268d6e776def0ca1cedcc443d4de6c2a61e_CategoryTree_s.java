 package com.grepdeals.consts;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public enum CategoryTree {
 
     Category(null),
     
     Automotive(Category),
     FinancialServices(Category),
 
     AutoGlassServices(Automotive),
     AutoRepairServices(Automotive),
     AutoPartsAccessories(Automotive),
     BodyShopsPainting(Automotive),
     CarDealers(Automotive),
     CarWashDetailing(Automotive),
     GasServicesStations(Automotive),
     MotorcycleDealers(Automotive),
     MotorcycleRepair(Automotive),
     OilChange(Automotive),
     Parking(Automotive),
     ScootersAndMopeds(Automotive),
     StereoInstallation(Automotive),
     TiresAndWheels(Automotive),
     Towing(Automotive),
 
     BanksAndCreditUnions(FinancialServices),
     CheckCashingPaydaylons(FinancialServices),
     CreditCounselingServices(FinancialServices),
     FinancialAdvising(FinancialServices),
     Insurance(FinancialServices),
     Investing(FinancialServices),
     MortgageBrokers(FinancialServices),
     StockBrokers(FinancialServices),
     TaxPreparation(FinancialServices);
 
 
     private final CategoryTree parent;
     private final List<CategoryTree> children = new ArrayList<CategoryTree>();
     private final List<CategoryTree> allChildren = new ArrayList<CategoryTree>();
 
     CategoryTree(CategoryTree parent) {
         this.parent = parent;
         if (parent != null) {
             parent.addChild(this);
         }
     }
 
     public CategoryTree parent() {
         return parent;
     }
 
     public boolean is(CategoryTree other) {
         if (other == null) {
             return false;
         }
 
         for (CategoryTree categoryTree = this; categoryTree != null; categoryTree = categoryTree.parent()) {
             if (other == categoryTree) {
                 return true;
             }
         }
         return false;
     }
 
     public List<? extends CategoryTree> children() {
         return Collections.unmodifiableList(children);
     }
 
     public List<? extends CategoryTree> allChildren() {
         return Collections.unmodifiableList(allChildren);
     }
 
     private void addChild(CategoryTree child) {
         this.children.add(child);
 
         List<CategoryTree> greatChildren = new ArrayList<CategoryTree>();
         greatChildren.add(child);
         greatChildren.addAll(child.allChildren());
 
         CategoryTree currentAncestor = this;
         while (currentAncestor != null) {
             currentAncestor.allChildren.addAll(greatChildren);
             currentAncestor = currentAncestor.parent;
         }
     }
 
     public String getName(){
         return name();
     }
 }
