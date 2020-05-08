 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 public class Rifle extends Weapon {
 
	public Rifle(float damage, float range) {
		super(2, 2);
 	}
 
 	@Override
 	public boolean canShoot(Collection<Soldier> enemies) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean fireWeapon() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
