 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.dto;
 
 //import com.muni.fi.pa165.entities.Monster;
 //import com.muni.fi.pa165.entities.Weapon;
 import com.muni.fi.pa165.dto.MonsterWeaponPkDto;
 import java.util.Objects;
 
 /**
  *
  * @author irina
  */
 public class MonsterWeaponDto {
 
  
     private Long monsterId;
     private Long weaponId;
     private Integer hitRate;
     private Integer damage;
     private Integer efficiency;
     private String description;
     private MonsterWeaponPkDto id;
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public MonsterWeaponPkDto getId() {
         return id;
     }
 //
     public void setId(MonsterWeaponPkDto id) {
         this.id = id;
     }
     
     
     
 
 
     public Long getMonster() {
         return monsterId;
     }
 
     public void setMonster(Long monster) {
         this.monsterId = monster;
     }
 
     public Long getWeapon() {
         return weaponId;
     }
 
     public void setWeapon(Long weapon) {
         this.weaponId = weapon;
     }
 
     
    
 
     public Integer getHitRate() {
         return hitRate;
     }
 
     public void setHitRate(Integer hitRate) {
         this.hitRate = hitRate;
     }
 
     public Integer getDamage() {
         return damage;
     }
 
     public void setDamage(Integer damage) {
         this.damage = damage;
     }
 
  
     public Integer getEfficiency() {
         return efficiency;
     }
 
     public void setEfficiency(Integer efficiency) {
         this.efficiency = efficiency;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 47 * hash + Objects.hashCode(this.monsterId);
         hash = 47 * hash + Objects.hashCode(this.weaponId);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
        final MonsterWeaponDto other = (MonsterWeaponDto) obj;
        if (!Objects.equals(this.id, other.id)) {
             return false;
         }
         return true;
     }
 
   
     
     
 }
