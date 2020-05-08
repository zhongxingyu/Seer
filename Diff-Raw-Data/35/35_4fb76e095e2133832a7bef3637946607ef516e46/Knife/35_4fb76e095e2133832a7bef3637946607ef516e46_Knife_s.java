 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 public class Knife extends Weapon {
 
	public Knife() {
		super(1,2);
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
