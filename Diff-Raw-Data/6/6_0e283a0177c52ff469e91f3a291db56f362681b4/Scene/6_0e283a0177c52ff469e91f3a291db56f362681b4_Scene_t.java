 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package cnerydb;
 
 import java.io.Serializable;
 import java.util.Set;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class Scene extends BaseEntity implements ConditionProvider<SceneCondition>, Comparable<Scene>, Serializable {
     private String name;
     private char statePersistence;
     private int minutes;
     private char resetAction;
     private Set<SceneAction> sceneActions;
     private Set<SceneCondition> sceneConditions;
     private Set<SceneActivation> sceneActivations;
 
     public void addChild(SceneAction child) {
         child.setParent(this);
         getSceneActions().add(child);
     }
 
     public void addChild(SceneCondition child) {
         child.setParent(this);
         getSceneConditions().add(child);
     }
 
     public void addChild(SceneActivation child) {
         child.setParent(this);
         getSceneActivations().add(child);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public int compareTo(Scene o) {
        int cmp;
        if (getName() == null || o.getName() == null)
            cmp = (getName() == null ? -1 : (o.getName() == null ? 0 : 1));
        else
            cmp = getName().compareTo(o.getName());
         if (cmp != 0)
             return cmp;
         return getUUID().compareTo(o.getUUID());
     }
 
     public int getMinutes() {
         return minutes;
     }
 
     public void setMinutes(int minutes) {
         this.minutes = minutes;
     }
 
     public char getResetAction() {
         return resetAction;
     }
 
     public void setResetAction(char resetAction) {
         this.resetAction = resetAction;
     }
 
     public char getStatePersistence() {
         return statePersistence;
     }
 
     public void setStatePersistence(char statePersistence) {
         this.statePersistence = statePersistence;
     }
 
     public Set<SceneAction> getSceneActions() {
         return sceneActions;
     }
 
     public void setSceneActions(Set sceneActions) {
         this.sceneActions = sceneActions;
     }
 
     public Set<SceneCondition> getSceneConditions() {
         return sceneConditions;
     }
 
     public void setSceneConditions(Set<SceneCondition> sceneConditions) {
         this.sceneConditions = sceneConditions;
     }
 
     public Set<SceneActivation> getSceneActivations() {
         return sceneActivations;
     }
 
     public void setSceneActivations(Set<SceneActivation> sceneActivations) {
         this.sceneActivations = sceneActivations;
     }
 
     @Override
     public Set<SceneCondition> getConditions() {
         return getSceneConditions();
     }
 }
