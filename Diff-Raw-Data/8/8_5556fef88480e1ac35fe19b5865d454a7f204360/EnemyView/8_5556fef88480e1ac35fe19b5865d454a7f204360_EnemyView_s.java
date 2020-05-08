 package com.secondhand.view.entities;
 
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.secondhand.model.Enemy;
 
 public class EnemyView extends BlackHoleView{
 
 	public EnemyView(final PhysicsWorld physicsWorld, final Enemy enemy) {
 		super(physicsWorld, enemy);
 
 		getBody().setLinearDamping(1.2f);
 	}
 
 }
