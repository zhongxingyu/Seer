 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 public class Exit extends Obs{
 
 	private boolean locked;
 
 	private Exit() {
 		super();
 		this.locked = false;
 	}
 
 	public Exit(Point point,float dx,float dy,float dz,MATERIALS material) {
 		super(point, dx,dy,dz,material);
 		this.locked = false;
		setLoopSound(new Sound(SOUNDS.DOOR, new Point(0,0,0), true, true, 1.0f));
 	}
 
 	public Exit(Point point,float dx,float dy,float dz,MATERIALS material,boolean locked) {
 		super(point, dx,dy,dz,material);
 		this.locked = locked;
 		setLoopSound(new Sound(SOUNDS.DOOR, new Point(0,0,0), true, true));
 	}
 
 	public void collision(Player player, Level level, int currentRoom){
 		System.out.println("Collision with door");
 		if(!locked){
 			if(getPos().getX() == level.getRoomList().get(currentRoom).getPos().getX()){
 				player.setPos(level.getRoomList().get(currentRoom-1).entrance.getX() - 0.11f, player.getPos().getY(), player.getPos().getZ());	
 			}else if(getPos().getX() + getDx() == level.getRoomList().get(currentRoom).getPos().getX() + level.getRoomList().get(currentRoom).getDx()){
 				player.setPos(level.getRoomList().get(currentRoom+1).entrance.getX() + 0.11f, player.getPos().getY(), player.getPos().getZ());
 			}else if(getPos().getY() == level.getRoomList().get(currentRoom).getPos().getY()){
 				player.setPos(player.getPos().getX(), level.getRoomList().get(currentRoom+1).entrance.getY() - 0.11f, player.getPos().getZ());
 			}else{
 				player.setPos(player.getPos().getX(), level.getRoomList().get(currentRoom+1).entrance.getY() + 0.11f, player.getPos().getZ());
 			}
 			level.updateSpawnPoint(player, level);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Door [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
 				+ ", lenght=" + getDy() + ", material=" + getMaterial() + "]";
 	}
 	@Override
 	public void draw(){
 		glColor4f(0.80f, 0f, 0f, 0f);
 		glLineWidth(1f);
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
 }
