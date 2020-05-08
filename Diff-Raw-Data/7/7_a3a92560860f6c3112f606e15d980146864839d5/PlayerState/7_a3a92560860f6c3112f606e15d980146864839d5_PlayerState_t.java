 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hopkins.rocknrollracing.state;
 
 /**
  *
  * @author ian
  */
 public class PlayerState {
     public static final int STARTING_MONEY = 200000;
     public static final int INFINITE_MONEY = Integer.MAX_VALUE;
     
     public CarColor Color;
     public CarModel Model;
     public Hero Hero;
     public int Money;
     public int Points;
     public UpgradeState Upgrades;
     
     public PlayerState() {
         Color = CarColor.Red;
         Model = CarModel.DirtDevil;
         Hero = Hero.Snake;
         Money = STARTING_MONEY;
         Points = 0;
         Upgrades = new UpgradeState();
     }
     
     public boolean hasInfiniteMoney() {
         return (Money == INFINITE_MONEY);
     }
     
     public boolean canAfford(CarModel m) {
         return (hasInfiniteMoney()) || (Money >= m.getPrice());
     }
     
     public void buyCar(CarModel m, CarColor color) {
         Color = color;
         Model = m;
         if (!hasInfiniteMoney()) {
             Money -= m.getPrice();
         }
         
         Upgrades = new UpgradeState();
         Upgrades.setLevel(UpgradeType.Weapon, m.getWeapon().ordinal());
         Upgrades.setLevel(UpgradeType.Boost, m.getBoost().ordinal());
         Upgrades.setLevel(UpgradeType.Drop, m.getDrop().ordinal());
     }
     
     public void awardPlace(PodiumPlace place) {
         if (Money != INFINITE_MONEY) {
             Money += place.getPrizeMoney();
         }
         Points += place.getPoints();
     }
     
     public Upgrade getCurrentUpgrade(UpgradeType type) {
         int level = Upgrades.getLevel(type);
         return Upgrade.All[type.ordinal()][level];
     }
     
     public Upgrade getNextUpgrade(UpgradeType type) {
         int nextlevel = Upgrades.getLevel(type);
         if (type.isAmmo()) {
             nextlevel++;
         }
         return Upgrade.All[type.ordinal()][nextlevel];
     }
     
     public boolean isAmmoUpgrade(UpgradeType type) {
         switch (type) {
             case Armor:
             case Engine:
             case Shocks:
             case Tires:
                 return false;
             default:
                 return true;
         }
     }
     
     public boolean canUpgrade(UpgradeType type) {
         if (isAmmoUpgrade(type)) {
            return (Upgrades.getCharges(type) < 7);
         } else {
            return (Upgrades.getLevel(type) < 3);
         }
     }
     
     public void buyUpgrade(UpgradeType type) {
         Upgrade next = getNextUpgrade(type);
         
         if (!hasInfiniteMoney()) {
             Money -= next.getPrice();
         }
         if (type.isAmmo()) {
             Upgrades.incCharges(type);
         } else {
             Upgrades.incLevel(type);
         }
     }
 }
