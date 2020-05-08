 package edu.chl.codenameg.model.entity;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.CollisionEvent;
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
public class LiftableBlock extends Block{
 	
 	private static final Hitbox hb = new Hitbox(32,32);
 	private PlayerCharacter pc;
 	private Vector2D gravity;
 	private Vector2D vector;
 	private boolean onGround;
 
 	public void collide(CollisionEvent evt){
 		super.collide(evt);
 
 	}
 
 	public LiftableBlock(Position ps){
 		super(ps);
 		this.gravity = new Vector2D(0,1);
 		this.vector = new Vector2D(0,0);
 	}
 	
 	public LiftableBlock() {
 		super();
 	}
 	
 	public String getType() {
 		return "LiftableBlock";
 	}
 	
 	public List<String> getCollideTypes() {
 		List<String> list = new ArrayList<String>();
 		list.add("Block");
 		list.add("PlayerCharacter");
 		return list;
 	}
 	
 	public void update() {
 		this.update(10);
 	}
 	
 	public void update(int elapsedTime) {
 	if (pc != null){
 		if (this.pc.getDirection()==Direction.RIGHT){
 			this.setPosition(new Position((pc.getPosition().getX()+ pc.getHitbox().getWidth()),(pc.getPosition().getY()+ pc.getHitbox().getHeight()-this.getHitbox().getHeight())));	
 		}else{
 			this.setPosition(new Position((pc.getPosition().getX()- this.getHitbox().getWidth()),(pc.getPosition().getY()+ pc.getHitbox().getHeight()-this.getHitbox().getHeight())));		
 		}
 		this.setVector2D(pc.getVector2D());
 	}else{
 		this.gravity = new Vector2D(0,0.98f);
 	}
 	/**if (!onGround) {
 		this.vector.add(this.gravity);
 		this.gravity.add(new Vector2D(0,0.1f));
 	} else {
 		this.gravity = new Vector2D(0,1);
 	}
 	this.onGround = false;
 	}
 	**/
 	}
 	public void lift(PlayerCharacter pc) {
 	this.pc = pc;
 	//this.setHitbox(null);
 	System.out.println("Lyfter");
 	}
 
 	public void drop(PlayerCharacter pc) {
 	//this.setHitbox(hb);
 	this.pc=null;
	System.out.println("Slpper");
 	}
 }
