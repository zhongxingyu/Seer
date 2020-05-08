 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 public abstract class Weapon{
 	public float damage;
 	private float range;
	public Soldier owner;
	
	public Weapon(float damage,float range, Soldier owner) {
 		this.damage = damage;
 		this.range = range;
		this.owner = owner;
 	}
 
 	public abstract boolean canShoot(Collection<Soldier> enemies);
 	public abstract boolean fireWeapon();
 }
