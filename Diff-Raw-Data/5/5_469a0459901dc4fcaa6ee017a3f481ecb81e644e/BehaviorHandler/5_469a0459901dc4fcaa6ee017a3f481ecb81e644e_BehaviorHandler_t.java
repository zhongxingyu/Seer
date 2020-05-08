 /*
 This file is part of Champions.
 
     Champions is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Champions is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Champions.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.championsdev.champions.library.behavior;
 
 import com.github.championsdev.champions.library.BasicHandler;
 
 import java.util.HashMap;
 
 /**
  * @author B2OJustin
  */
 public class BehaviorHandler  {
    private static BehaviorHandler instance = new BehaviorHandler();
 
     private HashMap<String, WeaponBehavior> weaponBehaviorMap = new HashMap<>();
     private HashMap<String, CPlayerBehavior> playerBehaviorMap = new HashMap<>();
     private HashMap<String, SkillBehavior> skillBehaviorMap = new HashMap<>();
 
     private WeaponBehavior defaultWeaponBehavior = new BasicWeaponBehavior();
     private SkillBehavior defaultSkillBehavior = new BasicSkillBehavior();
     private CPlayerBehavior defaultCPlayerBehavior = new BasicCPlayerBehavior();
 
    public static BehaviorHandler getInstance() {
         return instance;
     }
 
     private BehaviorHandler() {
     }
 
     public BehaviorHandler register(String id, WeaponBehavior behavior) {
         weaponBehaviorMap.put(id, behavior);
         return this;
     }
 
     public BehaviorHandler register(String id, CPlayerBehavior behavior) {
         playerBehaviorMap.put(id, behavior);
         return this;
     }
 
     public BehaviorHandler register(String id, SkillBehavior behavior) {
         skillBehaviorMap.put(id, behavior);
         return this;
     }
 
     public WeaponBehavior getWeaponBehavior(String id) {
         return weaponBehaviorMap.get(id);
     }
 
     public CPlayerBehavior getCPlayerBehavior(String id) {
         return playerBehaviorMap.get(id);
     }
 
     public SkillBehavior getSkillBehavior(String id) {
         return skillBehaviorMap.get(id);
     }
 
     public WeaponBehavior getDefaultWeaponBehavior() {
         return defaultWeaponBehavior;
     }
 
     public void setDefaultWeaponBehavior(WeaponBehavior defaultWeaponBehavior) {
         this.defaultWeaponBehavior = defaultWeaponBehavior;
     }
 
     public SkillBehavior getDefaultSkillBehavior() {
         return defaultSkillBehavior;
     }
 
     public void setDefaultSkillBehavior(SkillBehavior defaultSkillBehavior) {
         this.defaultSkillBehavior = defaultSkillBehavior;
     }
 
     public CPlayerBehavior getDefaultCPlayerBehavior() {
         return defaultCPlayerBehavior;
     }
 
     public void setDefaultCPlayerBehavior(CPlayerBehavior defaultCPlayerBehavior) {
         this.defaultCPlayerBehavior = defaultCPlayerBehavior;
     }
 }
