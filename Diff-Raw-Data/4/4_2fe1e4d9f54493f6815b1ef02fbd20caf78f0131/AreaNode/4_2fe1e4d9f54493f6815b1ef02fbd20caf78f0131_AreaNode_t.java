 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Quad;
 
 /**
  *
  * @author armageddon
  */
 abstract public class AreaNode extends Node{
     
     static final float AREA_PADDING = 0.125f;
     static final float AREA_FONT_SIZE = 0.1f;
     static final float AREA_FONT_PADDING = AREA_PADDING + 0.05f;
     static final float AREA_FONT_HEIGHT = 0.02f;
     
     //generation parameters
     static final int AREA_GEN_SHELTER_MAX =0;
     static final int AREA_GEN_HUMANS_MAX  =0;
     static final int AREA_GEN_ZOMBIES_MAX =0;
     
     
     
     int numZombies;
     int numHumans;
     int numSoldiers;
     int numAvailShelters;
     int numHumansInShelters;
 
     
     Vector2f pos;
     Vector2f size;
     
     Node connections;
     
     BitmapText statusText;
 
     public AreaNode(Vector2f pos, Vector2f size) {
         this.numZombies = 0;
         this.numHumans = 0;
         this.numSoldiers = 0;
         this.numAvailShelters = 0;
         this.numHumansInShelters = 0;
         this.pos = pos;
         this.size = size;
         
         connections = new Node("connections");
         attachChild(connections);
         
         
         setLocalTranslation(pos.x, pos.y, 0.0f);
     }
     
     public void updateGraphics(float tpf){
         statusText.setText(String.format("Z: %d\nH: %d\nAS: %d\nHS: %d\nS: %d", numZombies, numHumans, numAvailShelters, numHumansInShelters, numSoldiers));
     }
     
     
     
     public void connect(AreaNode to){
         AreaConnectionNode acn = new AreaConnectionNode(this, to);
         connections.attachChild(acn);
     }
     
     public Vector2f getPosition(){
         return pos;
     }
     
     public Vector2f getSize(){
         return size;
     }
 
     void planZombieMovements(float tpf) {
         if(numZombies > 0){
             for(Spatial sp : connections.getChildren()){
                 AreaConnectionNode acn = (AreaConnectionNode)sp;
                 AreaNode an = acn.getTo();
                 if(numZombies > an.getNumZombies()){
                     acn.moveZombies(((numZombies-an.getNumZombies())/4));
                 }
             }
         }
     }
     
     void moveZombies(float tpf) {
         for(Spatial sp : connections.getChildren()){
             AreaConnectionNode acn = (AreaConnectionNode)sp;
             acn.executeMovements();
         }
     }
     
     void humanSearchShelter(float tpf){
         if((numHumans > 0) && (numZombies > 0)){
            
            int goesToShelter = (int)(1);
             if (goesToShelter > numAvailShelters-numHumansInShelters){
                 goesToShelter = numAvailShelters-numHumansInShelters;
             }
             numHumansInShelters += goesToShelter;
             numHumans -= goesToShelter;
         }
     }
 
     void eatZombies(float tpf) {
         if(numHumans > 0){
             double ph = (1f-1f/Math.sqrt(numHumans/5.0+1f))*0.2+0.05;
             double pz = (0.7f/Math.sqrt(numZombies/20.0+1f))+0.3f;
             int numBitten = (int)(numZombies * ph * pz);
             if(numBitten > numHumans) numBitten = numHumans;
             numHumans -= numBitten;
             numZombies += numBitten;
         }
     }
 
     public int getNumZombies() {
         return numZombies;
     }
 
     public int getNumHumans() {
         return numHumans;
     }
 
     public int getNumSoldiers() {
         return numSoldiers;
     }
 
     public void setNumZombies(int numZombies) {
         this.numZombies = numZombies;
     }
 
     public void setNumHumans(int numHumans) {
         this.numHumans = numHumans;
     }
 
     public void setNumSoldiers(int numSoldiers) {
         this.numSoldiers = numSoldiers;
     }
     
     public int getNumAvailShelters() {
         return numAvailShelters;
     }
 
     public void setNumAvailShelters(int availShelters) {
         this.numAvailShelters = availShelters;
     }
 
     public int getNumHumansInShelters() {
         return numHumansInShelters;
     }
 
     public void setNumHumansInShelters(int humansInShelters) {
         this.numHumansInShelters = humansInShelters;
     }
     
     
     
 }
