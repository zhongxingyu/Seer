 package qp.main.entity;
 
 import java.util.ArrayList;
 
 import qp.main.threads.ProjectileMovement;
 
 public class Projectile extends Entity{
 	public double x, y;
 	public int width;
 	public int height;
 	public double dx,dy;
 	public boolean exists = true;
 	public static ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
 	public Projectile(double x, double y, int w, int h, double dx, double dy){
 		this.x = x;
 		this.y = y;
 		this.width = w;
 		this.height = h;
 		this.dx = dx;
 		this.dy = dy;
 		projectiles.add(this);
 		new Thread(new ProjectileMovement(this)).start();
 	}
 	public void move(){
 		this.x += dx;
 		this.y += dy;
 	}
 	public static ArrayList<Projectile> getProjectiles(){
 		return projectiles;
 	}
 	public void remove(){
 		exists = false;
 		projectiles.remove(this);
 	}
 	@Override
 	void move(double dx, double dy) {
 		this.x += dx;
 		this.y += dy;
 	}
 }
