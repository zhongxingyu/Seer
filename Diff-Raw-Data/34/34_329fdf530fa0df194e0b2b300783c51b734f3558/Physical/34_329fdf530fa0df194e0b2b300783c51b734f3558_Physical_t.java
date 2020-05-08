 package com.Capslock.Engine.Physics;
 
 import com.Capslock.Engine.Math.CVector2D;
 
 /*
  * The Physical class is the class which implements all of the properties of a physical object in the real world.
  * It allows the object to have the following:
  * 
  * 		Velocity
  * 		Acceleration
  * 		Mass
  * 		Friction
  * 		Forces (applied instantaneously to the object)
  * 
  * The velocity is the number of pixels that the entity will move every second.
  * We determine how far to move based on the current framerate of the game.
  * The current framerate is acquired from the Engine Instance class which must be passed to this class on creation.
  * 
  * The acceleration is the change in velocity that occurs every second. The change in velocity is also determined by the 
  * framerate which is acquired using the engine instance class.
  */
 
 public abstract class Physical extends Collidable{
 	
 	/*
 	 * The velocity is the number of pixels that the entity will move every second.
 	 */
 	protected CVector2D Velocity = new CVector2D(0, 0);
 	
 	/*
 	 * The acceleration is the number of pixels the velocity will change by in a given second.
 	 */
 	protected CVector2D Acceleration = new CVector2D(0, 0);
 	
 	/*
 	 * The friction is the factor by which the velocity will slow down every second.
 	 * The friction factor is essentially an indication of how much of the 
 	 * friction will always act on an object, regardless of whether it is accelerating, at a constant speed, or at rest.
 	 * 
 	 * Details on the friction factor are contained within method 1.
 	 * 
 	 * WE WILL BE USING METHOD 1 AS OUR IMPLEMENTATION.
 	 * 
 	 * there are two ways to apply the friction. the first is to calculate approximately how much of the velocity to remove given the current framerate like this:
 	 * 
 	 * 1.		
 	 * 			The "friction rating" will be split in to 100 parts, although it can be set higher if it needs to be. kind of like a percentage of how much friction acts on the object. 
 	 * 			a friction rating of 100 will mean that the object will stop within 1 second.
 	 * 			a friction rating of 0 will mean that friction does not act o the object.
 	 * 			if the user desires for the object to stop in under 1 second, then the friction rating should be increased above 100.
 	 * 			a friction rating of 1000 should stop the object in 0.1 of a second and so on. 
 	 * 
 	 * 			the friction rating will be described as a vector, because the friction rating can be different along the x and y axes.
 	 * 			
 	 * 			this formula will be applied along each axis independently. here is what the formula will look like along the x axis.
 	 * 
 	 * 				
 	 * 
 	 * 			Here we take aq portion of the velocity away from itself. the portion will increase as the frictionratio increases. 
 	 * 
 	 * 
 	 * 2. 
 	 * 		I have left this method in the comments in case for any reason we wish to use it. however, i have not researched it fully enough and as a result i cannot be positive thatit is a valid method.
 	 * 
 	 * 		velocity = velocity * nth root of ratio (where n equals the framerate)
 	 */
 	
 	protected CVector2D Friction = new CVector2D(0, 0);
 	
 	/*
 	 * The mass is simply how much of an effect a force will have on the acceleration of an object.
 	 * 
 	 * if an object has a mass of 1, then the force will produce an acceleration the same as it's magnitude:
 	 * 		mass = 1.
 	 * 		force = 100.
 	 * 		acceleration = 100.
 	 * 
 	 * if an object has a mass of 2, then the force will produce an acceleration of half of the force's magnitude:
 	 * 		mass = 2.
 	 * 		force = 100.
 	 * 		acceleration = 50.
 	 * 
 	 * The mass will never equal 0, so as for the event not to arise in which we would be dividing by 0. as a result, if the user of this class attempts to set the mass to 0,
 	 * it will automatically be set to the minimum mass as described by MASS_MIN. The value of Mass Min has no real significance. is is merely a very small number.
 	 */
 	
 	public static final double MASS_MIN = 0.0000001;
 	
 	protected double Mass = 1;
 	
 	/*
 	 * the force applied to an object is used to change the acceleration of the object. in general it will not be required as the user will tend to wish to  change the acceleration directly.
 	 * However, since it would be slighty difficult to implement, it has been added in purely for convenience.
 	 * 
 	 * when a force is "applied" to the object, the force applied is added on the the force vector.
 	 * then, when the move method is called, this force vector is scalar multiplied by the reciprocal of the mass and then added to the acceleration.
 	 * 
 	 * 
 	 * once the force has been applied to the acceleration, the force is reset to (0, 0)
 	 */
 	
 	protected CVector2D Force = new CVector2D(0, 0);
 	
 	
 	/*
 	 * The move method is the main method which will be called in a physical entity. it does the following, in order:
 	 * 		
 	 * 		apply the forces to the acceleration
 	 * 		apply the acceleration to the velocity
 	 * 		apply friction to the velocity
 	 * 		apply the velocity to the position
 	 * 	
 	 *	in order to carry out these tasks we create a method for each. This simply means that if a user wishes to adjust the behaviour of the physical entity they can do so.
 	 *
 	 */
 	
	public void move(float framerate){
 		applyForces(framerate);
 		applyAcceleration(framerate);
 		applyFriction(framerate);
 		applyVelocity(framerate);
 	}
 	
	protected void applyForces(float framerate){
 		/*
 		 * This method applies any forces present to the object.
 		 * This method differs from the "applyForce" method.
 		 * This method contains a calculation which changes the acceleration of the object, and sets the acting forces to 0.
 		 * the "applyForce" method is a public method available to the user which allows them to adjust the "Force" vector.
 		 */
 		
 		Acceleration = Acceleration.add(Force.scalarMultiply((double)framerate/Mass));
 		Force = new CVector2D(0, 0);
 	}
 	
	protected void applyAcceleration(float framerate){
 		/*
 		 * This method applies the current acceleration of the object to the velocity.
 		 */
 		Velocity = Velocity.add(Acceleration.scalarMultiply((double)1/(double)framerate));
 	}
 	
	protected void applyFriction(float framerate){
 		/*
 		 * This method applies friction to the velocity on the x and y axes independently.
 		 * for details on the implementation and method behind this method, consult the comments regarding the "friction" variable.
 		 */
 		
 		Velocity.setX(Velocity.getX() - (Velocity.getX() * ((Friction.getX()/100)/(double)framerate)));
 		Velocity.setY(Velocity.getY() - (Velocity.getY() * ((Friction.getY()/100)/(double)framerate)));
 	}
 	
	protected void applyVelocity(float framerate){
 		Position = Position.add(Velocity.scalarMultiply((double)1/(double)framerate));
 	}
 	
 	/*
 	 *	Get and Set methods.
 	 */
 	
 	public CVector2D getVelocity(){
 		return Velocity;
 	}
 	
 	public void setVelocity(CVector2D newVelocity){
 		Velocity = newVelocity;
 	}
 	
 	public void setVelocity(double newVelocityX, double newVelocityY){
 		Velocity = new CVector2D(newVelocityX, newVelocityY);
 	}
 	
 	public CVector2D getAcceleration(){
 		return Acceleration;
 	}
 	
 	public void setAcceleration(CVector2D newAcceleration){
 		Acceleration = newAcceleration;
 	}
 	
 	public void setAcceleration(double newAccelerationX, double newAccelerationY){
 		Acceleration = new CVector2D(newAccelerationX, newAccelerationY);
 	}
 	
 	public CVector2D getFriction(){
 		return Friction;
 	}
 	
 	public void setFriction(CVector2D newFriction){
 		Friction = newFriction;
 	}
 	
 	public void setFriction(double newFrictionX, double newFrictionY){
 		Friction = new CVector2D(newFrictionX, newFrictionY);
 	}
 	
 	public CVector2D getForce(){
 		return Force;
 	}
 	
 	public void setForce(CVector2D newForce){
 		Force = newForce;
 	}
 	
 	public void setForce(double newForceX, double newForceY){
 		Force = new CVector2D(newForceX, newForceY);
 	}
 	
 	public double getMass(){
 		return Mass;
 	}
 	
 	public void setMass(double newMass){
 		if(newMass>=MASS_MIN){
 			Mass = newMass;
 		}else{
 			Mass = MASS_MIN;
 		}
 	}
 	
 	/*
 	 * The apply Force method allows the user to apply a force, for a single frame, which will effect the acceleration of the object.
 	 * This force will be aplied relative to how high the framerate is. as a result, users may wish t apply a force every frame for
 	 * however long they require; not just once.
 	 * 
 	 * when a force is applied it is added on to the current force. this allows for multiple forces to be applied in the same frame.
 	 * once the fporce has been applied to the object's acceleration, the force vector is reset to (0, 0).
 	 * 
 	 * for more details see the comments at the force variable.
 	 */
 
 	public void applyForce(CVector2D appliedForce){
 		Force = Force.add(appliedForce);
 	}
 	
 	public void applyForce(double appliedForceX, double appliedForceY){
 		Force  = Force.add(new CVector2D(appliedForceX, appliedForceY));
 	}
 }
