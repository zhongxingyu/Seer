 package game.states;
 
 import java.awt.event.KeyEvent;
 
 import initial3d.engine.*;
 import initial3d.engine.xhaust.InventoryHolder;
 import initial3d.engine.xhaust.Pane;
 import initial3d.renderer.Util;
 import game.Game;
 import game.GameState;
 import game.bound.BoundingSphere;
 import game.entity.Entity;
 import game.entity.WallEntity;
 import game.entity.moveable.MoveableEntity;
 import game.entity.moveable.PlayerEntity;
 import game.floor.Floor;
 import game.floor.FloorGenerator;
 import game.net.packets.MovementPacket;
 
 /***
  * The game!
  * 
  * @author Ben
  * 
  */
 public class PlayState extends GameState {
 	public PlayState() {
 	}
 
 	MovableReferenceFrame cameraRf = null;
 	private boolean mouseLock = false;
 	
 	private boolean transmittedStop = false;
 
 	private double cam_pitch = 0;
 	private double player_yaw = 0;
 	
 	private double targetFov = -1;
 
 	@Override
 	public void initalise() {
 		
 		
 		for(int i = 0; i < Game.getInstance().players.length; i++)
 		{
 			Game.getInstance().players[i].addToScene(scene);
 		}
 		
 		System.out.println(Game.getInstance().getLevel());
 		Game.getInstance().getLevel().addToScene(scene);
 
 		MovableReferenceFrame cameraRf = new MovableReferenceFrame(Game.getInstance().player);
 		scene.getCamera().trackReferenceFrame(cameraRf);
 		cameraRf.setPosition(Vec3.create(0, 0.5, -0.9));
 //		cameraRf.setOrientation(Quat.create(Math.PI / 3.6f, Vec3.i));
 		Pane p = new Pane(250, 50);
 		InventoryHolder i = new InventoryHolder();
 		p.getRoot().add(i);
 		p.requestVisible(true);
 		p.setPosition(-275, -275);
 		p.getRoot().setOpaque(false);
 		scene.addDrawable(p);
 		
 		
 		Game.getInstance().getWindow().setMouseCapture(true);
 		
 		scene.setAmbient(new Color(0.1f, 0.1f, 0.1f));
 		scene.setFogColor(Color.BLACK);
 		scene.setFogParams(255f * 1.5f, 512f * 1.5f);
 		scene.setFogEnabled(true);
 		
 		Light l = new Light.DirectionalLight(ReferenceFrame.SCENE_ROOT, Color.WHITE, Vec3.create(0, 1, 1));
 		//scene.addLight(l);
 		
 		Light l2 = new Light.SphericalPointLight(Game.getInstance().player, Color.ORANGE, 0.25f);
 		scene.addLight(l2);
 		
 		Game.getInstance().player.getMeshContexts().get(0).setHint(MeshContext.HINT_SMOOTH_SHADING);		
 	}
 
 	@Override
 	public void update(double delta) {
 		RenderWindow rwin = Game.getInstance().getWindow();
 		if(targetFov < 0) targetFov = scene.getCamera().getFOV();
 
 		double speed = 1;
 		double sprintMulti = 1.5f;
 
 		int mx = rwin.pollMouseTravelX();
 		int my = rwin.pollMouseTravelY();
 
 		// 200px == pi / 4 ??
 
 		// these are confusing names, aren't they?
 		double rotx = mx / 800d * Math.PI;
 		double roty = my / 800d * Math.PI;
 
 		cam_pitch = Util.clamp(cam_pitch + roty, -Math.PI * 0.499, Math.PI * 0.499);
 		player_yaw -= rotx;
 
 		Camera cam = scene.getCamera();
 
 		MovableReferenceFrame rf = (MovableReferenceFrame) cam.getTrackedReferenceFrame();
 		rf.setOrientation(Quat.create(cam_pitch, Vec3.i));
 		
 		Vec3 cnorm = cam.getNormal().flattenY().unit();
 		Vec3 cup = Vec3.j;
 		Vec3 cside = Vec3.j.cross(cnorm);
 
 		Vec3 v = Vec3.zero;
 
 		if (rwin.getKey(KeyEvent.VK_W)) {
 			v = v.add(cnorm);
 		}
 		if (rwin.getKey(KeyEvent.VK_S)) {
 			v = v.add(cnorm.neg());
 		}
 		if (rwin.getKey(KeyEvent.VK_A)) {
 			v = v.add(cside);
 		}
 		if (rwin.getKey(KeyEvent.VK_D)) {
 			v = v.add(cside.neg());
 		}
 		
 		// temp
 		if(rwin.getKey(KeyEvent.VK_O)) {
 			this.scene.getCamera().setFOV(this.scene.getCamera().getFOV() + 0.01);
 		} else if(rwin.getKey(KeyEvent.VK_P)) {
 			this.scene.getCamera().setFOV(this.scene.getCamera().getFOV() - 0.01);
 		}
 		
 		double maxDelta = 0.01;
 		boolean sprinting = rwin.getKey(KeyEvent.VK_SHIFT);
 		if(sprinting) targetFov = Math.PI / 2.2;
 		else targetFov = Math.PI / 3;
 		
 		double dta = this.scene.getCamera().getFOV() - targetFov;
 		if(Math.abs(dta) > 0.1)
 		{
 			if(dta > maxDelta)
 				dta = maxDelta;
 			else if(dta < -maxDelta)
 				dta = -maxDelta;
 			
 			this.scene.getCamera().setFOV(this.scene.getCamera().getFOV() - dta);
 		}
 		
 		if (v.mag() > 0.0001) {
 			v = v.unit().scale(speed * (sprinting ? sprintMulti : 1));
 		}
 		
		if(Game.getInstance().getLevel().collides(Game.getInstance().player.getNextBound(), true)){
 			System.out.println("denied");
			Game.getInstance().player.fix();
 		}
 		
 		//TODO also needs to be changed
 		Game.getInstance().player.updateMotion(Game.getInstance().player.getPosition(), v, Quat.create(player_yaw, Vec3.j), Vec3.zero, System.currentTimeMillis());
 		
 		//other Ben's doing...
 		if(!v.equals(Vec3.zero))
 		{
 			Game.getInstance().transmitPlayerPosition();
 			transmittedStop = false;
 		}
 		else if(!transmittedStop)
 		{
 			Game.getInstance().transmitPlayerPosition();
 			transmittedStop = true;
 		}
 		
 	}
 
 	@Override
 	public void destroy() {
 	}
 
 	private void setFirstPerson(boolean _val) {
 		MovableReferenceFrame cameraRf = (MovableReferenceFrame) scene.getCamera().getTrackedReferenceFrame();
 		if (_val) {
 			cameraRf.setPosition(Vec3.create(0, 0.5, 0));
 			cameraRf.setOrientation(Game.getInstance().player.getOrientation());
 		} else {
 			cameraRf.setPosition(Vec3.create(0, 9, -10));
 			cameraRf.setOrientation(Quat.create(Math.PI / 3.6f, Vec3.i));
 		}
 	}
 	
 	private void scroll(double val){
 		if ((val < 0 && cameraRf.getPosition().mag() < 1) || 
 			(val > 0 && cameraRf.getPosition().mag() > 20) || 
 			(val==0)){
 			return;
 		}
 		
 		cameraRf.setPosition(cameraRf.getPosition().scale(val));
 	}
 }
