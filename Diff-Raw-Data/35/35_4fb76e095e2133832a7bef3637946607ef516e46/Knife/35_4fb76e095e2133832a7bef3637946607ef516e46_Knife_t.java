 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 public class Knife extends Weapon {
 
	public Knife(float damage, float range, Soldier owner) {
		super(damage, range, owner);
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
