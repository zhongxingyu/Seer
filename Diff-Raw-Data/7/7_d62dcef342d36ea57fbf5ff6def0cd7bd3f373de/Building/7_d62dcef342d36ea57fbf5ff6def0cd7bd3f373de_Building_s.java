 package com.group7.dragonwars.engine;
 
 import java.util.*;
 
 public class Building {
 
     private String buildingName;
     private Integer captureDifficulty, remainingCaptureTime;
     private Double attackBonus, defenseBonus;
     private Player owner;
     private Boolean goalBuilding;
     private Player lastCapturer;
     private String spriteLocation, spriteDir, spritePack;
     private List<Unit> producableUnits = new ArrayList<Unit>();
 
 
     public Building(String name, Integer captureDifficulty, Double attackBonus,
                     Double defenseBonus, Boolean goalBuilding, String spriteLocation,
                     String spriteDir, String spritePack) {
         this.buildingName = name;
 
         this.captureDifficulty = captureDifficulty;
         this.remainingCaptureTime = this.captureDifficulty;
 
         this.attackBonus = attackBonus;
         this.defenseBonus = defenseBonus;
 
         this.goalBuilding = goalBuilding;
 
         this.spriteLocation = spriteLocation;
         this.spriteDir = spriteDir;
         this.spritePack = spritePack;
 
     }
 
     public Building(Building building) {
         this.buildingName = building.getBuildingName();
 
         this.captureDifficulty = building.getCaptureDifficulty();
         this.remainingCaptureTime = this.captureDifficulty;
 
         this.attackBonus = building.getAttackBonus();
         this.defenseBonus = building.getDefenseBonus();
 
         this.goalBuilding = building.isGoalBuilding();
 
         this.spriteLocation = building.getSpriteLocation();
         this.spriteDir = building.getSpriteDir();
         this.spritePack = building.getSpritePack();
     }
 
     public Boolean canProduceUnits() {
        return !this.producableUnits.isEmpty()
     }
 
     public Boolean addProducableUnit(Unit unit) {
         this.producableUnits.add(unit);
     }
 
     public Player getLastCapturer() {
         return this.lastCapturer;
     }
 
     public void setLastCapturer(Player player) {
         this.lastCapturer = player;
     }
 
     public String getBuildingName() {
         return this.buildingName;
     }
 
     public Integer getCaptureDifficulty() {
         return this.captureDifficulty;
     }
 
     public Integer getRemainingCaptureTime() {
         return this.remainingCaptureTime;
     }
 
     public Double getAttackBonus() {
         return this.attackBonus;
     }
 
     public Double getDefenseBonus() {
         return this.defenseBonus;
     }
 
     public Boolean hasOwner() {
         return this.owner != null;
     }
 
     public Player getOwner() {
         return this.owner;
     }
 
     public void setOwner(Player player) {
         this.owner = player;
     }
 
     public Boolean isGoalBuilding() {
         return this.goalBuilding;
     }
 
     public void reduceCaptureTime(Integer captureAmount) {
         this.remainingCaptureTime -= captureAmount;
 
         if (this.remainingCaptureTime <= 0) {
             this.remainingCaptureTime = 0;
             this.owner = this.lastCapturer;
         }
     }
 
     public void resetCaptureTime() {
         this.remainingCaptureTime = this.captureDifficulty;
     }
 
     public String getSpriteLocation() {
         return this.spriteLocation;
     }
 
     public String getSpriteDir() {
         return this.spriteDir;
     }
 
     public String getSpritePack() {
         return this.spritePack;
     }
 }
