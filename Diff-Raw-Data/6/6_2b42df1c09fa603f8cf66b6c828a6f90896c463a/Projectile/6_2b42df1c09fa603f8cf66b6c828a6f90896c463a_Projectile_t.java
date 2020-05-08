 package com.gmail.bruno.jumpandshoot.Entity;
 
import com.jme3.math.Vector3f;
 import com.jme3.scene.debug.Arrow;
 
 /*
  * Projectile
  * 
  * Version 1.0
  *
  * 2012-05-27
  * 
  * Copyright  2012, Bruno Gustav Lanevik. All rights reserved.
  */
 
 public abstract class Projectile extends Arrow{
	public Projectile(Vector3f extent, int damage, float size, String texture) {
 		// TODO Auto-generated constructor stub
		this.setArrowExtent(extent);
 	}
 
 	protected boolean isMelee;
 	
 }
