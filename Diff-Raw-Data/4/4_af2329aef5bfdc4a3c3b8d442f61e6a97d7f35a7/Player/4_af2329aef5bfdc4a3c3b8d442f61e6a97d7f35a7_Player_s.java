 package GameObjects;
 
 import java.awt.event.KeyEvent;
 import Game.MainGame;
 import Game.Ressource;
 import Game.SharedRessources.RessourceType;
 import GameComponents.ObjectRenderer.Shape;
 import GameComponents.RigidBody.ForceMode;
 import Helpers.Color;
 import Maths.Vector2;
 
 public class Player extends GameChar 
 {
 
 	ParticleEffects effect = new ParticleEffects(transform,2000);
 	public Shoot secondEffect = new Shoot(transform);
 
 
 	long lastTime = 0;
 	final static long effectTimeThreshold = 200; // wait 000ms to toggle effect
 
 
 	public Player()
 	{
 		super();
 		objectRenderer.shape= Shape.Square;
 		objectRenderer.SetTexture("rocket_ship");
 	}
 
 	public void Update()
 	{
 		super.Update();
 
 		// Player Stuff
 		effect.Update();
 
 		secondEffect.Update();
 
 		PlayerControls();
 
 
 		Vector2 charFrontInWorldCoordinates = transform.LocalDirectionToWorld(new Vector2(0,1)).Normalized();
 		//MainGame.debug.DrawLine(transform.position,charFrontInWorldCoordinates,100,Color.Blue);
 
 	}
 
 	private void PlayerControls()
 	{
 		if(MainGame.controls.isPressed(KeyEvent.VK_RIGHT))
 		{
 			rigidBody.PushTorque(10,ForceMode.Impulse);
 		}
 		if(MainGame.controls.isPressed(KeyEvent.VK_LEFT))
 		{
 			rigidBody.PushTorque(-10,ForceMode.Impulse);
 		}
 		if(MainGame.controls.isPressed(KeyEvent.VK_UP))
 		{
 			Vector2 objectFrontInWorldCoordinates = transform.LocalDirectionToWorld(new Vector2(0,1));
 			rigidBody.PushForce(Vector2.Scale(1000, objectFrontInWorldCoordinates),ForceMode.Impulse);
 			effect.TurnOn();
 		}
 		else
 		{
 			effect.TurnOff();
 		}
 
		if(MainGame.controls.isPressed(KeyEvent.VK_SPACE))
 		{

 			secondEffect.TurnOff();
 			secondEffect.i++;
 			long time = System.currentTimeMillis();
 			if( time - lastTime >  effectTimeThreshold)
 			{
 				lastTime = time;
 
 				if(!secondEffect.isTurnedOn)
 				{
 					secondEffect.TurnOn();
 
 				}
 
 			}
 		}
 
 	}
 
 }
