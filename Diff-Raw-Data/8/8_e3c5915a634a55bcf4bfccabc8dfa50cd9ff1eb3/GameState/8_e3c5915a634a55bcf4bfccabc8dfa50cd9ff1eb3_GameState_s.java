 package com.voracious.dragons.common;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import com.voracious.dragons.client.graphics.Drawable;
 import com.voracious.dragons.client.towers.Tower;
 import com.voracious.dragons.client.units.Unit;
 
 public class GameState implements Drawable {
 
     private List<Tower> towers;
     private List<Unit> units;
 
     public GameState() {
         super();
         towers=new ArrayList<>();
         units=new ArrayList<>();
     }
 
     @Override
     synchronized
     public void draw(Graphics2D g) {
         for (Tower t : towers)
             t.draw(g);
         
         for (Unit u : units)
             u.draw(g);
     }
 
     synchronized
     public void tick() {
         for (Tower t : towers){
         	this.attack_an_Unit(t);
             t.tick();
         }
         
         for (Unit u : units)
             u.tick();
     }
 
     synchronized
     public void addTower(Tower t) {
         towers.add(t);
     }
 
     synchronized
     public void removeTower(Tower t) {
         towers.remove(t);
     }
 
     synchronized
     public void addUnit(Unit u) {
         units.add(u);
     }
 
     synchronized
     public void removeUnit(Unit u) {
         units.remove(u);
     }
     
     public void attack_an_Unit(Tower t){
     	ArrayList<Unit> tmp=new ArrayList<Unit>();
     	for(Unit u:units){
     		double xs=t.getX()-u.getX();
     		double ys=t.getY()-u.getY();
     		double dist=Math.sqrt((xs*xs)+(ys*ys));
     		if(dist<=t.getRange()){
     			tmp.add(u);
     		}
     	}
     	int randLoc=(int) (Math.random()*(tmp.size()));
    	t.attackUnit(tmp.get(randLoc));
     }

 }
