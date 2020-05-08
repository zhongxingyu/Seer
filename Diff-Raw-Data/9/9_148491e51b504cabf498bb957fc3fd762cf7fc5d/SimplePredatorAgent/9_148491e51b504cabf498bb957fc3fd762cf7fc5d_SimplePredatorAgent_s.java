 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * 
  * @author Henrik Its purpose is simply to hunt down the SimpleAgent (or any
  *         other agent) in a simple way
  */
 public class SimplePredatorAgent extends AbstractAgent {
 
 	private double maxSpeed;
 	private double visionRange;
 	private double maxAcceleration;
 
 	public SimplePredatorAgent(String name, Position p, Color c, int width,
 			int height, Vector velocity, double maxSpeed,
 			double maxAcceleration, double visionRange) {
 		super(name, p, c, width, height, velocity);
 		this.maxSpeed = maxSpeed;
 		this.maxAcceleration = maxAcceleration;
 		this.visionRange = visionRange;
 
 	}
 
 	@Override
 	public void updatePosition(List<IPopulation> predators,
 			List<IPopulation> preys, Dimension dim) {
 		setNewVelocity(predators, preys, dim);
 		getPosition().addVector(getVelocity());
 	}
 
 	@Override
 	public List<IAgent> reproduce(IAgent agent) {
 		// TODO Auto-generated method stub
 		System.err.println("ERROR IN REPRODUCE METHOD, METHOD UNDEFINED");
 		return null;
 	}
 
 	/**
 	 * @author Sebastian/Henrik
 	 * @param predators
 	 * @param preys
 	 * @param dim
 	 */
 	private void setNewVelocity(List<IPopulation> predators,
 			List<IPopulation> preys, Dimension dim) {
 		/* 
 		 * "Predator Force" is AT THE MOMENT defined as the sum of the vectors pointing away from 
 		 * all the preys in vision, weighted by the inverse of the distance to the preys, 
 		 * then normalized to have unit norm. 
 		 * Can be interpreted as the average sum of forces that the agent feels, weighted
 		 * by how close the source of the force is.
 		 */
 		Vector preyForce = new Vector(0, 0);
 		int nVisiblePreys = 0;
 		for (IPopulation pop : preys) {
 			for (IAgent a : pop.getAgents()) {
 				Position p = a.getPosition();
 				double distance = getPosition().getDistance(p);
 				if (distance <= visionRange) {
 					/*
 					 * Create a vector that points towards the prey.
 					 */
 					Vector newForce = new Vector(p, getPosition());
 					
 					/*
 					 * Add this vector to the prey force, with proportion to how close the prey is.
 					 * Closer preys will affect the force more than those far away. 
 					 * There is a "+0.0000001" in the multiplication. That is just if the distance
 					 * gets 0. You can't divide by 0, but you can with 0.0000001.
 					 */
 					preyForce.add(newForce.multiply(1/getPosition().getDistance(p) + 0.0000001));
 					nVisiblePreys++;
 				 }
 			}
 		}
 
 		if (nVisiblePreys == 0) { //No preys near --> Be unaffected
 			preyForce.setVector(0,0);
 		} else { //Else set the force depending on visible preys and normalize it to maxAcceleration.
 			preyForce.multiply(1/(double)nVisiblePreys);
 			double norm = preyForce.getNorm();
 			preyForce.multiply(maxAcceleration/norm);
 		}
 
 		/*
 		 * The positions below is just an orthogonal projection on to the walls.
 		 */
 		Position xWallLeft = new Position(0, getPosition().getY());
 		Position xWallRight = new Position(dim.getWidth(), getPosition().getY());
 		Position yWallBottom = new Position(getPosition().getX(), 0);
 		Position yWallTop = new Position(getPosition().getX(), dim.getHeight());
 
 		/*
 		 * The environment force is at the moment defined as 1/(wall-constant*(distance to wall)^2).
 		 * The agents feel the forces from the wall directly to the left, right, top and bottom.
 		 * There is a "-1" in the equation just to make it more unlikely that they actually make it to the wall,
 		 * despite the force they feel (can be interpreted as they stop 1 pixel before the wall).
 		 */
 		Vector environmentForce = new Vector(0, 0);
 		double xWallLeftForce = 1/Math.pow((getPosition().getDistance(xWallLeft)-1)/WALL_CONSTANT, 2);
 		double xWallRightForce = -1/Math.pow((getPosition().getDistance(xWallRight)-1)/WALL_CONSTANT,2);
 		double yWallBottomForce = 1/Math.pow((getPosition().getDistance(yWallBottom)-1)/WALL_CONSTANT, 2);
 		double yWallTopForce = -1/Math.pow((getPosition().getDistance(yWallTop)-1)/WALL_CONSTANT,2);
 
 		/*
 		 * Add the forces from left and right to form the total force from walls in x-axis.
 		 * Add the forces from top and bottom to form the total force from walls in y-axis.
 		 * Create a force vector of the forces.
 		 */
 		double xForce = (xWallLeftForce + xWallRightForce);
 		double yForce = (yWallBottomForce + yWallTopForce);
 		environmentForce.setVector(xForce, yForce);
 		
 		/*
 		 * Sum the forces from walls and preys to form the acceleration force.
 		 * If the acceleration exceeds maximum acceleration --> scale it to maxAcceleration,
 		 * but keep the correct direction of the acceleration.
 		 */
 		Vector acceleration = environmentForce.add(preyForce);
 		double accelerationNorm = acceleration.getNorm();
 		if (accelerationNorm > maxAcceleration) {
 			acceleration.multiply(maxAcceleration/accelerationNorm);
 		}
 		
 		/*
 		 * The new velocity is then just:
 		 * v(t+dt) = v(t)+a(t+1)*dt, where dt = 1 in this case.
 		 * If speed exceeds maxSpeed --> scale it to maxSpeed, but keep the correct direction.
 		 */
 		Vector newVelocity = getVelocity().add(acceleration);
 		double speed = newVelocity.getNorm();
		if (speed > maxAcceleration) {
 			// Scales the norm of the vector back to maxSpeed
 			newVelocity.multiply(maxSpeed / speed);
 		}
 		
 		getVelocity().setVector(newVelocity);
 
 	}
 }
