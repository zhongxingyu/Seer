 package com.jjaz.aetherflames;
 
 import java.util.Map;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsConnectorManager;
 import org.andengine.util.HorizontalAlign;
 
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class SceneUpdateHandler implements IUpdateHandler
 {
 	public SceneUpdateHandler()
 	{
 	}
 	
 	boolean deleted(PhysicsConnector pc)
 	{
 		Body body = pc.getBody();
 		if(body == null)
 		{
 			return false;
 		}
 		Object userData = body.getUserData();
 		if(userData == null)
 		{
 			return false;
 		}
 		if(userData.equals("delete"))
 		{
 			IEntity shape = pc.getShape();
 
 			AetherFlamesActivity.mPhysicsWorld.unregisterPhysicsConnector(pc);
 			AetherFlamesActivity.mPhysicsWorld.destroyBody(body);
 			AetherFlamesActivity.mScene.detachChild(shape);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed)
 	{
 		AetherFlamesActivity.mPhysicsWorld.onUpdate(pSecondsElapsed);
 		PhysicsConnectorManager pcm = AetherFlamesActivity.mPhysicsWorld.getPhysicsConnectorManager();
 		for(int i = 0; i < pcm.size(); i++)
 		{
 			PhysicsConnector pc = pcm.get(i);
 			if(deleted(pc))
 			{
 				continue;
 			}
 		}
 		
 		for (Map.Entry<Integer,Ship> shipEntry : AetherFlamesActivity.ships.entrySet()) 
 		{
 			Ship ship = shipEntry.getValue();
 			ship.updateStatusBars();
 			ship.regen();
 		}
 		
 		if(AetherFlamesActivity.ships.size() == 1)
 		{
 			Ship winner = AetherFlamesActivity.ships.entrySet().iterator().next().getValue();
 			final Text winText = new Text(AetherFlamesActivity.CAMERA_WIDTH/2, AetherFlamesActivity.CAMERA_HEIGHT/2, AetherFlamesActivity.mFont, "Player " + winner.id + " wins!", new TextOptions(HorizontalAlign.CENTER), AetherFlamesActivity.mVertexBufferObjectManager);
 			float textHeight = winText.getHeight();
 			float textWidth = winText.getWidth();
 			winText.setY(AetherFlamesActivity.CAMERA_HEIGHT/2 - textHeight/2);
 			winText.setX(AetherFlamesActivity.CAMERA_WIDTH/2 - textWidth/2);
 			AetherFlamesActivity.mScene.attachChild(winText);
 			AetherFlamesActivity.mGameEngine.stop();
 		}
		//AetherFlamesActivity.mClientGameManager.sendUpdates();
 	}
 
 	@Override
 	public void reset()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
