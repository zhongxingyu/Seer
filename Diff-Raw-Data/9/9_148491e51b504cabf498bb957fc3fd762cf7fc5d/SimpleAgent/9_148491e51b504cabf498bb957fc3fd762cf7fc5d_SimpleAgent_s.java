 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.LinkedList;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Gender;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * A basic implementation of the IAgent interface.
  * @author Albin
  */
 public class SimpleAgent extends AbstractAgent {
 
 	private double maxSpeed;
 	private double visionRange;
 	private double maxAcceleration;
 	
 	public SimpleAgent(String name, Position p, Color c, int width, int height, 
 			Vector velocity, double maxSpeed, double maxAcceleration,double visionRange) {
 		super(name, p, c, width, height, velocity);
 		this.maxSpeed = maxSpeed;
 		this.maxAcceleration = maxAcceleration;
 		this.visionRange = visionRange;
 	}
 		@Override
 	public List<IAgent> reproduce(IAgent agent) {
 		return new LinkedList<IAgent>();
 	}
 		
 	/**
 	 * @author Sebbe
 	 */
 	@Override
 	public void updatePosition(List<IPopulation> predators,
 							   List<IPopulation> preys, Dimension gridDimension) {
 		setNewVelocity(predators, preys, gridDimension);
 		getPosition().addVector(getVelocity());
 	}
 	
 	/**
 	 * @author Sebbe
 	 */
 	private void setNewVelocity(List<IPopulation> predators, List<IPopulation> preys, Dimension dim){
 		/* 
 		 * "Predator Force" is defined as the sum of the vectors pointing away from all the predators in vision, weighted by
 		 * the inverse of the distance to the predators, then normalized to have unit norm. 
 		 * Can be interpreted as the average sum of forces that the agent feels, weighted
 		 * by how close the source of the force is.
 		 */
 		Vector predatorForce = new Vector(0,0);
 		int nVisiblePredators = 0;
 		for(IPopulation pop : predators) {
 			for(IAgent predator : pop.getAgents()) {
 				Position p = predator.getPosition();
 				double distance = getPosition().getDistance(p);
 				if(distance<=visionRange){ //If predator is in vision range for prey
 					/*
 					 * Create a vector that points away from the predator.
 					 */
 					Vector newForce = new Vector(this.getPosition(),p);
 					
 					/*
 					 * Add this vector to the predator force, with proportion to how close the predator is.
 					 * Closer predators will affect the force more than those far away. 
 					 * There is a "+0.0000001" in the multiplication. That is just if the distance
 					 * gets 0. You can't divide by 0, but you can with 0.0000001.
 					 */
 					predatorForce.add(newForce.multiply(1/this.getPosition().getDistance(p)+0.0000001));
 					nVisiblePredators++;
 				}
 			}
 		}
 		
 		if(nVisiblePredators==0){ //No predators near --> Be unaffected
 			predatorForce.setVector(0,0);
 		} else { //Else set the force depending on visible predators and normalize it to maxAcceleration.
 			double norm = predatorForce.getNorm();
 			predatorForce.multiply(maxAcceleration/norm);
 		}	
 		
 		/*
 		 * The positions below is just an orthogonal projection on to the walls.
 		 */
 		Position xWallLeft = new Position(0,this.getPosition().getY());
 		Position xWallRight = new Position(dim.getWidth(),this.getPosition().getY());
 		Position yWallBottom = new Position(this.getPosition().getX(),0);
 		Position yWallTop = new Position(this.getPosition().getX(),dim.getHeight());
 		
 		/*
 		 * The environment force is at the moment defined as 1/(wall-constant*(distance to wall)^2).
 		 * The agents feel the forces from the wall directly to the left, right, top and bottom.
 		 * There is a "-1" in the equation just to make it more unlikely that they actually make it to the wall,
 		 * despite the force they feel (can be interpreted as they stop 1 pixel before the wall).
 		 */
 		Vector environmentForce = new Vector(0,0);
 		double xWallLeftForce = 1/Math.pow((this.getPosition().getDistance(xWallLeft)-1)/WALL_CONSTANT,2);
 		double xWallRightForce = -1/Math.pow((this.getPosition().getDistance(xWallRight)-1)/WALL_CONSTANT,2);
 		double yWallBottomForce = 1/Math.pow((this.getPosition().getDistance(yWallBottom)-1)/WALL_CONSTANT,2);
 		double yWallTopForce = -1/Math.pow((this.getPosition().getDistance(yWallTop)-1)/WALL_CONSTANT,2);
 		
 		/*
 		 * Add the forces from left and right to form the total force from walls in x-axis.
 		 * Add the forces from top and bottom to form the total force from walls in y-axis.
 		 * Create a force vector of the forces.
 		 */
 		double xForce = (xWallLeftForce + xWallRightForce);
 		double yForce = (yWallBottomForce + yWallTopForce);
 		environmentForce.setVector(xForce, yForce);
 		
 		/*
 		 * Sum the forces from walls and predators to form the acceleration force.
 		 * If the acceleration exceeds maximum acceleration --> scale it to maxAcceleration,
 		 * but keep the correct direction of the acceleration.
 		 */
 		Vector acceleration = environmentForce.add(predatorForce);
 		double accelerationNorm = acceleration.getNorm();
 		if(accelerationNorm > maxAcceleration){
 			acceleration.multiply(maxAcceleration/accelerationNorm); 
 		}
 		
 		/*
 		 * The new velocity is then just:
 		 * v(t+dt) = v(t)+a(t+1)*dt, where dt = 1 in this case.
 		 * If speed exceeds maxSpeed --> scale it to maxSpeed, but keep the correct direction.
 		 */
 		Vector newVelocity = this.getVelocity().add(acceleration);
 		double speed = newVelocity.getNorm();
		if(speed > maxAcceleration){
 			newVelocity.multiply(maxSpeed/speed); 
 		}
 		
 		this.getVelocity().setVector(newVelocity);
 	}
 	
 	
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
