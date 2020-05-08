 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /*
  * This class has been modified to stricly create an upward 'explosion' of particles
  * 
  * Modified for the MadBlueBalls game
  */
 
 public class ParticleSystem{
 	private int numParts;
 	private Random rand;
 	private int x;
 	private int y;
 	private List<Particle> plist;
 	private Vector gravity;
 	
 	
 	public ParticleSystem(int x, int y, int number)
 	{
 		gravity = new Vector(0,9.8);
 		numParts = number;
 		rand = new Random();
 		this.x = x;
 		this.y = y;
 		plist = new ArrayList<Particle>();
 	}
 	
 	public void create()
 	{
 		
 		for (int i=0; i<numParts; i++)
 		{
 			double vX = ((rand.nextDouble()*2)-1)*100;
 			double vY =(rand.nextDouble()*-1)*100;
 			double life = (rand.nextDouble()*100)+150;
			Particle p = new Particle(x, y, vX, vY, 1000, .01, life);
			p.multVelocity(8);
			plist.add(p);
 		}
 	}
 	
 	public void update()
 	{
 		for (int i=0; i<plist.size(); i++)
 		{
 			Particle P = plist.get(i);
 			double life = P.getLifespan();
 			if (life<0)
 			{
 				plist.remove(i);
 			}
 			else
 			{
 				P.applyForce(gravity);
 				P.updateLocation();
 				P.setLifespan(life-1);
 			}
 		}
 	}
 	
 	public ArrayList<Particle> getParticles()
 	{
 		return new ArrayList<Particle>(plist);
 	}
 	
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 	
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 
 
 }
