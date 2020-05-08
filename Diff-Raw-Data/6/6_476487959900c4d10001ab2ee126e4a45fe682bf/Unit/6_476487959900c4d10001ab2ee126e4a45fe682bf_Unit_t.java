 package com.group7.dragonwars.engine;
 
 import java.util.LinkedList;
 
 public class Unit {
 
     private String name;
     private Integer maxMovement, movement;
     private Double maxHealth, health;
     private Double attack, meleeDefense, rangeDefense;
     private Position position;
     private Boolean hasFinishedTurn = false;
     private Player owner;
     private Boolean isFlying;
     private String spriteLocation, spriteDir, spritePack;
 
 
     public Unit(String name, Double maxHealth, Integer maxMovement,
                 Double attack, Double meleeDefense, Double rangeDefense,
                 Boolean isFlying, String spriteLocation,
                 String spriteDir, String spritePack) {
         this.name = name;
 
         this.maxHealth = maxHealth;
         this.health = this.maxHealth;
 
         this.maxMovement = maxMovement;
         this.movement = this.maxMovement;
 
         this.attack = attack;
         this.meleeDefense = meleeDefense;
         this.rangeDefense = rangeDefense;
 
         this.isFlying = isFlying;
         this.spriteLocation = spriteLocation;
         this.spriteDir = spriteDir;
         this.spritePack = spritePack;
     }
 
     public Boolean isDead() {
         return health <= 0;
     }
 
     public Double getHealth() {
         return this.health;
     }
 
     public Double getMaxHealth() {
         return this.maxHealth;
     }
 
     public Double getAttack() {
         return this.attack;
     }
 
     public Double getMeleeDefense() {
         return this.meleeDefense;
     }
 
     public Double getRangeDefense() {
         return this.rangeDefense;
     }
 
     public Position getPosition() {
         return this.position;
     }
 
     public Integer getRemainingMovement() {
         return this.movement;
     }
 
     public Integer getMaxMovement() {
         return this.maxMovement;
     }
 
     public Player getOwner() {
         return this.owner;
     }
 
     public void setOwner(Player player) {
         this.owner = player;
     }
 
     public void reduceHealth(Double damage) {
         this.health -= damage;
     }
 
     public void setPosition(Position position) {
         this.position = position;
     }
 
     public void restoreHealth(Double heal) {
         Double newHealth = this.health = heal;
         this.health = (newHealth <= maxHealth) ? newHealth : maxHealth;
     }
 
     public Boolean hasFinishedTurn() {
         return this.hasFinishedTurn;
     }
 
     public String toString() {
         return this.name;
     }
 
     public Boolean isRanged() {
         return false;
     }
 
     public Boolean isFlying() {
        return this.isFlying;
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
 
     public String getUnitName() {
         return this.name;
     }
}
