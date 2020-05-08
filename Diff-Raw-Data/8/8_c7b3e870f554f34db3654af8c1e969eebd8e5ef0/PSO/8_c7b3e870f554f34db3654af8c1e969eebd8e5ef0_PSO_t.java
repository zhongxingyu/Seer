 package pso;
 
 import java.util.Random;
 import log.PSOLogger;
 
 /**
  * Particle Swarm Optimization algorithm implementation.
  * Class contains all steps of algorithms in separate methods. 
  *
  * @author Marek Hlav <mark.hlavac@gmail.com>
  *
  */
 public class PSO {
 	
 	protected Swarm _swarm;
 	protected Particle _target;
 
 	/**
 	 * Empty constructor.
 	 */
 	public PSO(){
 	}
 	
 	public Swarm getSwarm() {
 		return _swarm;
 	}
 
 	
 	/**
 	 * Body of algorithm execution.
 	 * Algorithm sequence:
 	 * 1. generate target particle
 	 * 2. generate initial swarm
 	 * 3. start execution cycle (based on number of iterations in setup)
 	 * 3A. calculate fitness
 	 * 3B. update global fitness
 	 * 3C. check early end condition (based on fitness tolerance value in setup)
 	 * 3D. update velocities
 	 * 3E. update locations
 	 * 4. calculate fitness
 	 * 5. update global fitness
 	 */
 	public void run(){
 		generateTargetParticle();
 		generateInitialSwarm();
 		
 		for (int iter = 0; iter < Setup.ITERATIONS; iter++){
 			calculateFitness();
 			updateGlobalFitness();
 			
 			if (isEarlyEnd()){
 				PSOLogger.logEarlyEnd(iter, _swarm);
 				return;
 			}
 			
 			updateVelocities(iter);
 			updateLocations();
 			
 			//System.out.println("----");
 			PSOLogger.logIteration(iter, _swarm);
 			//PSOLogger.logBestLocation(_swarm);
 			//PSOLogger.logParticleLocation(_target);
 			//System.out.println("----");
 		}
 		
 		calculateFitness();
 		updateGlobalFitness();
 		
 		PSOLogger.logIteration(Setup.ITERATIONS, _swarm);
 	}
 	
 	/**
 	 * Generate particle solution which describes where we are going to converge.
 	 */
 	public void generateTargetParticle(){
 		_target = new Particle(Setup.DIMENSIONS);
 		Random generator = new Random();
 		int randValue;
 		
 		for (int j = 0; j < Setup.DIMENSIONS; j++){
 			// generate random position value
 			randValue = generator.nextInt(Setup.XMAX) + Setup.XMIN;
 			_target.setLocationValueAt(j, randValue);
 			
 			randValue = Setup.SPEED_MIN + generator.nextInt(Setup.SPEED_MAX - Setup.SPEED_MIN);
 			_target.setVelocityValueAt(j, randValue);
 		}
 	}
 	
 	/**
 	 * Generate initial swarm with random particles.
      * Every particle has generated its own location and velocity vector. 
 	 */
 	public void generateInitialSwarm(){
 		_swarm = new Swarm(Setup.PARTICLES);
 		
 		Particle particle;
 		Random generator = new Random();
 		int randValue;
 		for (int i = 0; i < Setup.PARTICLES; i++){
 			// generate particle
 			particle = new Particle(Setup.DIMENSIONS);
 			
 			for (int j = 0; j < Setup.DIMENSIONS; j++){
 				// generate random position value
 				randValue = generator.nextInt(Setup.XMAX) + Setup.XMIN;
 				particle.setLocationValueAt(j, randValue);
 				
 				randValue = Setup.SPEED_MIN + generator.nextInt(Setup.SPEED_MAX - Setup.SPEED_MIN);
 				particle.setVelocityValueAt(j, randValue);
 			}
 			
 			_swarm.addParticleAt(particle, i);
 		}
 	}
 	
 	/**
 	 * Fitness function calculation.
 	 * It describes the quality of particle in search space.
 	 */
	protected void calculateFitness(){
 		Particle particle;
 		double fitness;
 		int targetLoc, particleLoc, distance, absDistance; 
 		
 		for (int i = 0; i < Setup.PARTICLES; i++){
 			particle = _swarm.getParticleAt(i);
 			fitness = 0;
 			
 			for (int j = 0; j < Setup.DIMENSIONS; j++){
 				targetLoc = _target.getLocationValueAt(j);
 				particleLoc = particle.getLocationValueAt(j);
 				
 				distance = targetLoc - particleLoc;
 				fitness += Math.pow(distance, 2);
 			}
 			
 			absDistance = (int)fitness;
 			
 			if (fitness > 0)
 				fitness = 100 / Math.sqrt(fitness);
 			else 
 				fitness = 0;
 			
 			particle.setDistance(absDistance);
 			particle.setFitness(i, fitness);
 		}
 	}
 	
 	/**
 	 * Set best swarm (global) fitness from all best local positions of particles in swarm.
 	 * gBest = max(p[0].lBest, p[1].lBest .. p[N].lBest)
 	 */
 	public void updateGlobalFitness(){
 		Particle particle;
 		Particle globalBestParticle = _swarm.getParticleAt(0);
 		
 		for (int i = 1; i < Setup.PARTICLES; i++){
 			particle = _swarm.getParticleAt(i);
 			//System.out.println("...");
 			//PSOLogger.logParticleLocation(particle);
 			//System.out.println("...");
 			
 			if (globalBestParticle.getLocalBestFitness() < particle.getLocalBestFitness())
 				globalBestParticle = particle;
 		}
 		
 		_swarm.setGlobalBestParticle(globalBestParticle);
 	}
 	
 	/**
 	 * Calculate new velocity for each particle.
 	 * There are list of input variables for calculation formula:
 	 * - current location
 	 * - current velocity
 	 * - social influence accelerator coefficient c1
 	 * - cognitive influence accelerator coefficient c2
 	 * - inertia weight of particle
 	 * - stochastic coefficients r1, r2
 	 * - global best location
 	 * - local best location
 	 * 
 	 * @param iter		current iteration number
 	 */
 	public void updateVelocities(int iter){
 		Particle particle;
 		Particle globalBestParticle = _swarm.getGlobalBestParticle();
 		double velocity;
 		Random generator = new Random();
 		
 		// formula variables
 		int currentVelocity;
 		double weight = calculateWeight(iter);
 		double c1 = Setup.C1;
 		double c2 = Setup.C2;
 		double r1, r2;
 		int bestLocal, bestGlobal, currLocation;
 		int[] currentLocationValues;
 		int[] localBestLocationValues;
 		int[] globalBestLocationValue = globalBestParticle.getBestLocation();
 		
 		// formula components
 		double cognitiveComponent;
 		double socialComponent;
 		double inertia;
 		
		for (int i = 0; i < Setup.PARTICLES; i++){
 			particle = _swarm.getParticleAt(i);
 			currentLocationValues = particle.getLocation();
 			localBestLocationValues = particle.getBestLocation();
 			r1 = generator.nextDouble();
 			r2 = generator.nextDouble();
 			
 			for (int j = 0; j < Setup.DIMENSIONS; j++){
 				currentVelocity = particle.getVelocityValueAt(j);
 				bestLocal = localBestLocationValues[j];
 				bestGlobal = globalBestLocationValue[j];
 				currLocation = currentLocationValues[j];
 				
 				inertia = weight * currentVelocity;
 				cognitiveComponent = c1 * r1 * (bestLocal - currLocation); 
 				socialComponent = c2 * r2 * (bestGlobal - currLocation);
 				
 				velocity = inertia + cognitiveComponent + socialComponent;
 				
 				particle.setVelocityValueAt(j, (int) velocity);
 			}
 		}
 	}
 	
 	/**
 	 * Inertia weight calculation.
 	 * Final weight should descend by increasing number of iterations.
 	 * 
 	 * @param iter		current iteration number
 	 * @return			inertia weight
 	 */
 	public double calculateWeight(int iter){
 		double wmin = Setup.WMIN;
 		double wmax = Setup.WMAX;
 		int iterMax = Setup.ITERATIONS;
 		
 		return wmax - ((wmax - wmin) / iterMax) * iter; 
 	}
 	
 	/**
 	 * Calculate new location for every particles. 
 	 */
 	public void updateLocations(){
 		Particle particle;
 		int location;
 		
		for (int i = 0; i < Setup.PARTICLES; i++){
 			particle = _swarm.getParticleAt(i);
 			
 			for (int j = 0; j < Setup.DIMENSIONS; j++){
 				location = particle.getLocationValueAt(j) + particle.getVelocityValueAt(j);
 				particle.setLocationValueAt(j, location);
 			}
 		}
 	}
 	
 	/**
 	 * Early end condition definition.
 	 * 
 	 * @return		is early end condition accepted?
 	 */
 	public boolean isEarlyEnd(){
 		return _swarm.getGlobalBestParticle().getFitness() >= Setup.FITNESS_TOLERANCE;
 	}
 }
