 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 public class Trap extends Obs {
 	
 	//private Sound trapKillSound = new Sound(TRAPKILLSOUND,getPos(),10,10);
 	//private Sound trapSound = new Sound(TRAPSOUND,getPos(),10,10);
 	
 	public Trap() {
 		super();
 		setLoopSound(new Sound(SOUNDS.TRAP_ROTER, new Point(0,0,0), true, true));
 	}
 	
 	public Trap(Point point,float dx,float dy,float dz,MATERIALS material) {
 		super(point, dx,dy,dz,material);
 		setLoopSound(new Sound(SOUNDS.TRAP_ROTER, new Point(0,0,0), true,true));
		setEmitSound(true);
 	}
 	
 	public void update(){
 		//trapSound.loop();
 	}
 	public void collision(Player player, Level level, int currentRoom){
 		System.out.println("Collision with trap");
 		player.kill(level);
 	}
 	@Override
 	public void draw(){
 		glColor4f(0.5f, 0.4f, 0.3f, 0.2f);
 	   	 glLineWidth(1.2f);
 	   	 glBegin(GL_LINES);
 	   		 //Bottom Line
 	   		 glVertex2i((int)getPos().getX(), (int)getPos().getY());
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)getPos().getY());
 	   		 //Left Line
 	   		 glVertex2i((int)getPos().getX(), (int)getPos().getY());
 	   		 glVertex2i((int)getPos().getX(), (int)(getPos().getY()+this.getDy()));
 	   		 //Top Line
 	   		 glVertex2i((int)getPos().getX(), (int)(getPos().getY()+this.getDy()));
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)(getPos().getY()+this.getDy()));
 	   		 //Right Line
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)(getPos().getY()+this.getDy()));
 	   		 glVertex2i((int)(getPos().getX()+this.getDx()), (int)getPos().getY());
 	   	 glEnd();
 	}
 	
 	@Override
 	public String toString() {
 		return "Trap [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
 				+ ", lenght=" + getDy() + ", material=" + getMaterial() + "]";
 	}
 }
