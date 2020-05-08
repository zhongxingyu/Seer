 package physicsForces;
 
 import simulation.Mass;
 import util.Vector;
 
 public class Viscosity extends Force{
 	
 	public Viscosity(Vector viscosity)
 	{
 		super(viscosity);
 	}
 //
 //	public Vector getViscosity() {
 //		return this;
 //	}
 //	
 	public void massInitialize(Mass mass)
 	{
			this.setDirection(mass.getAcceleration().getDirection()+180);

		
 		
 	}
 	
 	public Vector returnForce()
 	{
 		return this;
 	}
 }
