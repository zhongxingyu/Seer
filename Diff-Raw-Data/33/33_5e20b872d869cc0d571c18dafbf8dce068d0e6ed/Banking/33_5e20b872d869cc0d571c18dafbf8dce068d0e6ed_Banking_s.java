 
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Equipment;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 import org.powerbot.game.bot.Context;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 /**
  *
  * @author PimGame
  */
 public class Banking extends Node {
 
     public static final Area area = new Area(new Tile[]{
                 new Tile(3267, 3175, 0), new Tile(3274, 3175, 0),
                 new Tile(3274, 3160, 0), new Tile(3267, 3156, 0)
             });
     public static boolean ringOn = false;
 
     @Override
     public String toString() {
         return "Banking...";
     }
 
     @Override
     public boolean activate() {
         return playerAtBank() && Inventory.getCount(Variables.INV_FILTER) < 1;
     }
 
     @Override
     public void execute() {
         final SceneObject bank = SceneEntities.getNearest(Variables.BANK);
 
         if (Smelter.bars >= Variables.oresAmount && Variables.oresAmount != 0) {
             Context.get().getScriptHandler().stop();
             System.out.println("Reached the amount of ores to smelt.");
         } else {
         }
 
         if (Smelter.ring == true) {
             Widgets.get(548).getChild(112).interact("Worn Equipment");
             sleep(500);
             ringOn = Equipment.containsOneOf(Variables.RING_OF_FORGING);
 
             if (ringOn == false) {
 
                 Widgets.get(548).getChild(111).interact("Inventory");
                 bank.interact("Bank", "Bank booth");
                 while (!Bank.isOpen()) {
                     sleep(500);
                 }
 
                 Bank.depositInventory();
                 while (Inventory.getCount() > 0) {
                     sleep(500);
                 }
 
                 if (Bank.getItemCount(Variables.RING_OF_FORGING) > 0) {
 
                     Bank.withdraw(Variables.RING_OF_FORGING, 1);
                     while (Inventory.getCount(Variables.RING_OF_FORGING) < 1) {
                         sleep(500);
                     }
 
                     Bank.close();
                     while (Bank.isOpen()) {
                         sleep(500);
                     }
 
                     Equipment.equip(Variables.RING_OF_FORGING);
                     while (!Equipment.containsOneOf(Variables.RING_OF_FORGING)) {
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find any Rings of Forgings... Shutting down.");
                     Context.get().getScriptHandler().shutdown();
                 }
             }
         }
 
         if (!Bank.isOpen()) {
            bank.interact("Bank", "Bank booth");
             while (!Bank.isOpen()) {
                 sleep(Random.nextInt(750, 1500));
             }
         }
 
         while (Inventory.getCount() > 0) {
             Bank.depositInventory();
             sleep(1000);
         }
 
 
         switch (Variables.bar) {
             case "Bronze":
                 if (Bank.getItem(Variables.COPPER_ID) != null && Bank.getItem(Variables.TIN_ID) != null && Bank.getItem(Variables.COPPER_ID).getStackSize() >= 14 && Bank.getItem(Variables.TIN_ID).getStackSize() >= 14) {
                     Bank.withdraw(Variables.COPPER_ID, 14);
                     while (Inventory.getCount(Variables.COPPER_ID) < 0) {
                         sleep(500);
                     }
                     Bank.withdraw(Variables.TIN_ID, 14);
                     while (Inventory.getCount(Variables.TIN_ID) < 0) {
                         sleep(500);
                     }
                     System.out.println("Banked.");
                 } else {
                     System.out.println("Could not find Copper and Tin ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Iron":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 0);
                     while (Inventory.getCount(Variables.SELECTED_ORE) < 0) {
                         sleep(500);
                     }
                     System.out.println("Banked.");
                 } else {
                     System.out.println("Could not find Iron ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Steel":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null && Bank.getItem(Variables.COAL_IDS) != null) {
 
                     Bank.withdraw(Variables.SELECTED_ORE, 9);
                     sleep(1200);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
 
 
                     while (Inventory.getCount(Variables.COAL_IDS) == 0) {
                         Bank.withdraw(Variables.COAL_IDS, 18);
                         sleep(500);
                     }
                     System.out.println("Banked.");
                 } else {
                     System.out.println("Could not find Coal and Iron ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Mithril":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null && Bank.getItem(Variables.COAL_IDS) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 5);
                     sleep(1200);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
 
                     while (Inventory.getCount(Variables.COAL_IDS) == 0) {
                         Bank.withdraw(Variables.COAL_IDS, 20);
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find Mithril and/or Coal ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Adamant":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null && Bank.getItem(Variables.COAL_IDS) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 4);
                     sleep(1200);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
 
                     while (Inventory.getCount(Variables.COAL_IDS) == 0) {
                         Bank.withdraw(Variables.COAL_IDS, 24);
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find Adamant and/or Coal ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Rune":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null && Bank.getItem(Variables.COAL_IDS) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 3);
                     sleep(1200);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
 
                     while (Inventory.getCount(Variables.COAL_IDS) == 0) {
                         Bank.withdraw(Variables.COAL_IDS, 25);
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find Runite and/or Coal ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Gold":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 0);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find Gold ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
             case "Silver":
                 if (Bank.getItem(Variables.SELECTED_ORE) != null) {
                     Bank.withdraw(Variables.SELECTED_ORE, 0);
                     while (Inventory.getCount(Variables.SELECTED_ORE) == 0) {
                         sleep(500);
                     }
                 } else {
                     System.out.println("Could not find Silver ore... Shutting down.");
                     Context.get().getScriptHandler().stop();
                 }
                 break;
         }
 
 
 
         Bank.close();
         while (Bank.isOpen()) {
             sleep(500);
         }
 
         if (Smelter.rest == true) {
             if (Walking.getEnergy() < Smelter.restpercentage) {
                 Widgets.get(750).getChild(2).interact("Rest");
                 while (Walking.getEnergy() < Smelter.restpercentage) {
                     sleep(500);
                 }
             }
         }
 
     }
 
     public static boolean playerAtBank() {
         return area.contains(Players.getLocal().getLocation());
     }
 }
