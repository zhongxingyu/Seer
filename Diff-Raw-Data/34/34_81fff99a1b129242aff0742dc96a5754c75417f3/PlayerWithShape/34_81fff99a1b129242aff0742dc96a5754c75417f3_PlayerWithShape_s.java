 package org.frankversnel.nl.poortjes.dummy;
 
 import org.frankversnel.nl.poortjes.GameObject;
 import org.frankversnel.nl.poortjes.collision.CollisionLevel;
 import org.frankversnel.nl.poortjes.collision.component.Collidable;
 import org.frankversnel.nl.poortjes.collision.component.CollidableBox;
 import org.frankversnel.nl.poortjes.component.Speed;
 import org.frankversnel.nl.poortjes.component.Transform;
 import org.frankversnel.nl.poortjes.game.Player;
 import org.frankversnel.nl.poortjes.gameloop.GameLoop;
 import org.frankversnel.nl.poortjes.movement.Moveable;
 import org.frankversnel.nl.poortjes.rendering.RenderingManager;
 import org.frankversnel.nl.poortjes.rendering.component.DrawableShape;
 
 public class PlayerWithShape extends GameObject {
 
 	public PlayerWithShape(RenderingManager renderManager,
 			CollisionLevel collisionLevel, GameLoop gameLoop,
 			float positionX, float positionY, String shapeId) {
		new Transform(this, positionX, positionY, 100, 100, 0);
 		new Moveable(this, gameLoop);
 		new Speed(this, 0.015f, 0.006f);
 		new DrawableShape(this, renderManager, shapeId);
 
 		Collidable collidable = new CollidableBox(this, collisionLevel);
 		new Player(this, collidable);
 	}
 
 }
