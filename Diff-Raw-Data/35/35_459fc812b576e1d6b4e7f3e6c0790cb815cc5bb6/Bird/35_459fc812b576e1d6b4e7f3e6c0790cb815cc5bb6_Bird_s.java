 package es.clementin.ludum;
 
 import java.util.List;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.xsmalldeadguyx.astar.Node;
 import org.xsmalldeadguyx.astar.Pathfinder;
 
 import com.jme3.math.Vector2f;
 
 enum Direction {
 	NORTH, EAST, SOUTH, WEST
 }
 
 public class Bird extends Entity{
 	Direction direction;
 	
 	public float fullness = 1;
 	private float fullnessIncrease;
 	private float fullnessDecrease;
 	private float fullnessGained = 0;
 	private float fullnessThreshold;
 	private final float fullnessThresholdMultiplier = 0.1f;
 	private final float fullnessThresholdBase = 0.2f;
 	
 	public boolean eating = false;
 	private Shrub target;
 	
 	private final float chanceOfGenerationalIncrease = 0.5f;
 	private final float generationalMultiplier = 1.15f;
 	private final float chanceOfSpawningOffspring = 0.0001f;
 	
 	private float beakLength;
 	private float height;
 	private float hardiness;
 	
 	int age = 0;
 	boolean pathfinding = false;
 	int pathIndex = 0;
 	
 	List<Node> path;
 	
 	private Vector2f oldGridPos;
 	private Vector2f dGridPos;
 	
 	float stepsLeft = 0;
 	
 	public Bird(int x, int y){
 		this(x, y,
 			((Main.rand.nextFloat() - 0.5f) * 1f) + 1,
 			((Main.rand.nextFloat() - 0.5f) * 1f) + 1,
 			((Main.rand.nextFloat() - 0.5f) * 1f) + 1
 		);
 	}
 	
 	public Bird(int x, int y, float beakLength, float height, float hardiness){
 		super(x, y, new Vector2f(0, 0));
 		this.direction = Direction.SOUTH;
 		this.oldGridPos = this.gridPos.clone();
 		
 		this.fullnessIncrease = ((Main.rand.nextInt(50) + 50 ) / 100f) * 0.0013f;
 		this.fullnessDecrease = ((Main.rand.nextInt(50) + 50 ) / 100f) * 0.004f;
 		
 		this.beakLength = beakLength;
 		this.height = height;
 		this.hardiness = hardiness;
 		
 		switch(Main.goalType){
 			case BEAK:
 				Main.currentBest = Math.max(beakLength, Main.currentBest);
 				break;
 			case HEIGHT:
 				Main.currentBest = Math.max(height, Main.currentBest);
 				break;
 			case HARDINESS:
 				Main.currentBest = Math.max(hardiness, Main.currentBest);
 				break;
 		}
 	}
 	
 	public void updateImage(){
 		switch(direction){
 			case SOUTH:
 				this.texPos.set(0, 3);
 				break;
 			case EAST:
 				this.texPos.set(0, 1);
 				break;
 			case WEST:
 				this.texPos.set(0, 0);
 				break;
 			case NORTH:
 				this.texPos.set(0, 2);
 				break;
 		}
 		
 		if(this.eating){
 			this.texPos.y += 4;
 		}
 
 		this.texPos = this.texPos.mult(Main.textures.size);
 	}
 	
 	public void findFood(){
 		float choice = Main.rand.nextFloat();
 		float ftf = 1;
 		if(choice <= 0.333){
 			// Going for a spiky shrub
			target = Main.spikyShrubs.get(Main.rand.nextInt(Main.spikyShrubs.size()));
 			ftf = hardiness;
 		}
 		else if(choice <= 0.666){
 			// Going for a tall shrub
			target = Main.tallShrubs.get(Main.rand.nextInt(Main.tallShrubs.size()));
 			ftf = height;
 		}
 		else {
 			// Going for an intricate shrub
			target = Main.intricateShrubs.get(Main.rand.nextInt(Main.intricateShrubs.size()));
 			ftf = beakLength;
 		}
 		fullnessThreshold = (fullnessThresholdBase + (ftf * fullnessThresholdMultiplier));
 	}
 	
 	public void moveToFood(){
		moveToPoint(this.target.gridPos);
 	}
 	
 	private void moveToPoint(Vector2f p){
 		this.path = Pathfinder.generate((int) gridPos.x, (int) gridPos.y, (int) p.x, (int) p.y, Main.world.walkable);
 		
 		if(this.path.size() > 0){
 			pathfinding = true;
 		}
 	}
 	
 	public void eatFood(){
 		if(this.fullnessGained > fullnessThreshold || this.fullness >= 1){
 			this.eating = false;
 			this.fullnessGained = 0;
 		}
 		else {
 			float fi = this.fullnessDecrease;
 			if(this.target instanceof SpikyShrub){
 				fi *= this.hardiness;
 			}
 			else if(this.target instanceof TallShrub){
 				fi *= this.height;
 			}
 			else if(this.target instanceof IntricateShrub){
 				fi *= this.beakLength;
 			}
 			this.fullness += fi;
 			this.fullnessGained += fi;
 		}
 		updateImage();
 	}
 	
 	public void getHungry(){
 		if(fullness > fullnessIncrease){
 			fullness -= fullnessIncrease;
 		}
 		else if(fullness > 0){
 			fullness = 0;
 		}
 		else{
 			die();
 		}
 	}
 	
 	private void die(){
 		Main.deadBirds.add(this);
 	}
 	
 	public void spawnOffspring(){
 		if(Main.rand.nextFloat() < chanceOfSpawningOffspring){
 			float gm;
 			if(Main.rand.nextFloat() > chanceOfGenerationalIncrease){
 				gm = 1;
 			}
 			else {
 				gm = generationalMultiplier;
 			}
 			Main.newBirds.add(new Bird((int) gridPos.x, (int) gridPos.y, 
 				beakLength * gm,
 				height * gm,
 				hardiness * gm
 			));
 		}
 	}
 	
 	private void drawHealthbar(){
 		GL11.glBegin(GL11.GL_QUADS);
 		
 		Color.black.bind();
 		GL11.glVertex2f(screenPos.x, screenPos.y + Main.TILE_SIZE);
 		GL11.glVertex2f(screenPos.x, screenPos.y + Main.TILE_SIZE + 3);
 		GL11.glVertex2f(screenPos.x + Main.TILE_SIZE, screenPos.y + Main.TILE_SIZE + 3);
 		GL11.glVertex2f(screenPos.x + Main.TILE_SIZE, screenPos.y + Main.TILE_SIZE);
 		
 		Color.red.bind();
 		GL11.glVertex2f(screenPos.x, screenPos.y + Main.TILE_SIZE + 1);
 		GL11.glVertex2f(screenPos.x, screenPos.y + Main.TILE_SIZE + 2);
 		GL11.glVertex2f(screenPos.x + Main.TILE_SIZE * fullness, screenPos.y + Main.TILE_SIZE + 2);
 		GL11.glVertex2f(screenPos.x + Main.TILE_SIZE * fullness, screenPos.y + Main.TILE_SIZE + 1);
 		
 		GL11.glEnd();
 	}
 	
 	public void draw(){
 		super.draw();
 		drawHealthbar();
 	}
 	
 	public void animate(){
 		if(stepsLeft > 0){
 			syncPosition();
 			stepsLeft--;
 		}
 		else {
 			move();
 		}
 	}
 	
 	protected void syncPosition(){
 		this.screenPos = this.screenPos.add(this.dGridPos.mult(Main.TILE_SIZE).divide(10));
 	}
 	
 	public void move(){	
 		if(pathIndex == path.size() - 1){
 			this.pathfinding = false;
 			this.eating = true;
 			this.pathIndex = 0;
 			return;
 		}
 		
 		Node nextNode = this.path.get(pathIndex);
 		this.pathIndex++;
 		this.gridPos.set(nextNode.x, nextNode.y);
 		
 		stepsLeft = 10;
 		
 		this.dGridPos = this.gridPos.subtract(this.oldGridPos);
 		if(Math.abs(dGridPos.x) > Math.abs(dGridPos.y)){
 			// It moved further horizontally than vertically
 			if(dGridPos.x > 0){
 				this.direction = Direction.EAST;
 			}
 			else {
 				this.direction = Direction.WEST;
 			}
 		}
 		else {
 			// It moved further vertically than horizontally
 			if(dGridPos.y > 0){
 				this.direction = Direction.SOUTH;
 			}
 			else {
 				this.direction = Direction.NORTH;
 			}
 		}
 		
 		this.oldGridPos = this.gridPos.clone();
 		
 		updateImage();
 	}
 	
 	
 }
