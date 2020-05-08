 package game.GameStates;
 
 import java.awt.event.KeyEvent;
 
 import initial3d.engine.MovableReferenceFrame;
 import initial3d.engine.Quat;
 import initial3d.engine.Vec3;
import game.floor.Floor;
import game.floor.FloorGenerator;
 import game.Game;
 import game.GameState;
 import game.entity.Entity;
 import game.entity.PlayerEntity;
 import game.entity.WallEntity;
 
 /***
  * The game!
  * @author Ben
  *
  */
 public class PlayState extends GameState {	
 	public PlayState(Game parent) {
 		super(parent);
 	}
 	
 	PlayerEntity player = null;
 	
 	@Override
 	public void initalise() {
 		player = new PlayerEntity(Vec3.create(1, 0, 1));
 		player.addToScene(scene);
 		
 		FloorGenerator fg = new FloorGenerator(123873123312l);
 		Floor f = fg.getFloor(0);
 		
 		for(WallEntity we : f.getWalls())
 		{
 			we.addToScene(scene);
 		}
 		
 		
 		MovableReferenceFrame cameraRf = new MovableReferenceFrame(player);
 		scene.getCamera().trackReferenceFrame(cameraRf);
 		cameraRf.setPosition(Vec3.create(0, 9, -10));
 		cameraRf.setOrientation(Quat.create(Math.PI / 3.6f, Vec3.i));
 	}
 
 	@Override
 	public void update(double delta) {
 		if(game.getWindow().getKey(KeyEvent.VK_UP))
 			this.player.moveTo(this.player.getPosition().add(Vec3.create(0, 0, 0.1)));
 		if(game.getWindow().getKey(KeyEvent.VK_DOWN))
 			this.player.moveTo(this.player.getPosition().add(Vec3.create(0, 0, -0.1)));
 		if(game.getWindow().getKey(KeyEvent.VK_LEFT))
 			this.player.moveTo(this.player.getPosition().add(Vec3.create(0.1, 0, 0)));
 		if(game.getWindow().getKey(KeyEvent.VK_RIGHT))
 			this.player.moveTo(this.player.getPosition().add(Vec3.create(-0.1, 0, 0)));
 	}
 
 	@Override
 	public void destroy() {
 	}	
 }
