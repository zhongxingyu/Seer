 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
import java.util.Iterator;
 
 public class Rifle extends Weapon {
 
	Collection<Bullet> bullets;
	
	public Rifle(float damage, float range, Soldier owner) {
		super(2, 2, owner);
 	}
 
 	@Override
 	public boolean canShoot(Collection<Soldier> enemies) {
 		// TODO Auto-generated method stub
		Soldier enemy;
		float directionAmount = owner.faceLeft ? -1 : 1;
		for (Iterator<Soldier> iterator = enemies.iterator(); iterator.hasNext();) {
			enemy = (Soldier)iterator.next();
		}
 		return false;
 	}
 
 	@Override
 	public boolean fireWeapon() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
