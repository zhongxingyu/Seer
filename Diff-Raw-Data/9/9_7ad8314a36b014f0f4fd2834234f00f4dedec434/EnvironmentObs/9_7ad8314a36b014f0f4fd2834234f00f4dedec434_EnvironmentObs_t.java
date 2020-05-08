 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor3f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glVertex3f;
 
 public class EnvironmentObs extends Obs {
 	
 	//private Sound collisionSound = new Sound(HANDSONWALL,getPos(),10,10);
 	
 	public EnvironmentObs() {
 		super();
 	}
 	
 	public EnvironmentObs(Point point, SOUNDS sound, boolean loop, boolean reverb) {
 		setPos(point);
 		setDx(0);
 		setDz(0);
 		setDy(0);
 		setMaterial(MATERIALS.ROCK);
		setLoopSound(new Sound(sound, new Point(point.getX(),point.getY(),point.getZ()), loop, reverb, 0.5f));
 	}
 	
 	public void collision(Player player, Level level, int currentRoom){
 		System.out.println("Collision with EnvironmentObs");
 	}
 	
 	@Override
 	public String toString() {
 		return "EnvironmentObs [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
 				+ ", lenght=" + getDy() + ", material=" + getMaterial() + "]";
 	}
 	@Override
 	public void draw(){
 	   	glColor3f(0.2f, 0.6f, 0.4f);
 		glBegin(GL_LINE_STRIP); float f = 0.0f; for(int i = 0; i<30;i++){
 			glVertex3f(getPos().getX()+(float)Math.cos(f),
 					getPos().getY()+(float)Math.sin(f), 0); f = (float) (f +(2*Math.PI/30)); }
 		glEnd();
 	}
 }
