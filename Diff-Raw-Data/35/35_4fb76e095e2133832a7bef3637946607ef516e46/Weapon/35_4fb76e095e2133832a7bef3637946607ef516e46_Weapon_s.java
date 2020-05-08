 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 public abstract class Weapon{
 	public float damage;
 	private float range;

	public Weapon(float damage,float range) {
 		this.damage = damage;
 		this.range = range;
 	}
 
 	public abstract boolean canShoot(Collection<Soldier> enemies);
 	public abstract boolean fireWeapon();
 }
