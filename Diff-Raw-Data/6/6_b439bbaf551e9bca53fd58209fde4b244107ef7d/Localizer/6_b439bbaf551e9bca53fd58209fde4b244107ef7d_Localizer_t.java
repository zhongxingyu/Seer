 import java.util.ArrayList;
 import java.util.Arrays;
 
 
 /**
  ** Localizer: perform localization calculations
  **
  **
  **
  **
  */
 public class Localizer extends Thread {
 	public static final int NUM_PARTICLES = 10000;
 	public static final double PARTICLE_TOLERANCE = .95;
 	
 
 	private boolean localized;
 	private boolean updateReady;
 	private double dx,dy,dYaw;
 	// Doubles for means of all particles
 	private double meanX,meanY,meanYaw;
 	private int[][] map;
 	private GridMap gmap;
 	private double[] ranges;
 	private ArrayList<Particle> particleList;
 	private Particle expectedLocation;
 
 	/**
 	 * Create a new Localizer object to be used in a map with the given
 	 * dimensions. Fills the list of particles with NUM_PARTICLES particles or
 	 * mapw*maph particles, whichever is smaller. All particles are initalized
 	 * with equal weight, 1 divided by the number of particles.
 	 *
 	 * @param   map the map!
 	 */
 	public Localizer(int[][] map, GridMap gmap) {
 		localized = false;
 		this.map = map;
 		this.gmap = gmap;
 		int mapw = map.length;
 		int maph = map[0].length;
 		int numParticles = (NUM_PARTICLES > mapw*maph) ? 
 				mapw*maph : NUM_PARTICLES;
 		int ppp = Math.round(mapw*maph/numParticles);   // pixels per particle
 		particleList = new ArrayList<Particle>(numParticles);
 		int x = 0, y = 0;
 		double weight = 1.0/numParticles;
 
 		for (int i = 0; i < numParticles; i++) {
 			if (map[x][y] != 0) {   // Can't be in an obstacle, silly
 				particleList.add(i,new Particle(x, y, 0, weight));
 			} else {
 			    for (int j = 1; j < numParticles; j++) {
			        if (map[(x+j)%mapw][((x+i)/mapw + y)%maph] == 255) {
 			            particleList.add(i, 
 			                new Particle((x+j)%mapw,(x+i)/mapw + y,0, weight));
 			            break;
 			        }
 			    }
 			}
 			x = (x+ppp) % mapw;
 			if (x < ppp) y ++;
 		}
 		expectedLocation = null;
 		updateReady = false;
 
 		drawMap();
 		gmap.repaint();
		
		System.out.printf("NUM_PARTICLES = %d\nParticles in ArrayList = %d\n",
		    NUM_PARTICLES,particleList.size());
 
 	}
 
 	public synchronized void predict() {
         while (!updateReady) {
 				try {
 					wait();
 				} catch (InterruptedException e) {};
 		}
 		updateReady = false;	
 	    
 		// Do we have enough particles?
 		if (effectiveSampleSize() < PARTICLE_TOLERANCE * NUM_PARTICLES) {
 			int[] indexCopyList = resample();
 			for (int i = 0; i < indexCopyList.length; i++) {
 				Particle temp = (Particle) particleList.get(i).clone();
 				particleList.add(temp);
 			}
 		}
 		//   for (j = 1 to M) do {Prediction after action A}
 		int m = particleList.size();
 		for (int j = 0; j < m; j++) {
 			// Drifts, not sure exactly how we should do this, paper suggests using numbers selected randomly
 			//  from a gaussian.
 			double distDrift;
 			double yawDrift;
 			Particle temp = particleList.get(j);
 
 			gmap.clearParticle(temp.getX(),temp.getY());
 
 			double tx = temp.getX() + dx;
 			double ty = temp.getY() + dy;
 			double tp = temp.getPose() + dYaw;
 			// If we are greater than pi, wrap around to negatives.
 			if (tp > Math.PI)
 				tp = tp - 2*Math.PI;
 			temp.move(tx, ty, tp);
 
 			gmap.setParticle(temp.getX(),temp.getY());
 
 			//     X^k+1_j = F(X^k_j,A)
 			particleList.set(j, temp);
 			//   end for
 		}
 	}
 	/*
 	 * This will update and normalize the weights
 	 */
 	public void update() {
 		//   for(j = 1 to M) do {Update the weights}
 		//      W^k+1_j  = W^K_j * W(s,X^k+1_j)
 		//   end for 
 		//   for(j=1 to M) do {Normalize the weights}
 		//     W^k+1_j = (W^k+1_j)/(Sumi=1 to M (W^k+1_i))
 		//   end for
 		double thesum = 0;
 		for (Particle p : particleList) {
 			double weight = p.getWeight() * prob(p,ranges);
 			// accrue sum for normalizing
 			thesum += weight;
 			p.setWeight(weight);
 		}
 		// Normalize the weights
 		for (Particle p : particleList) {
 			double weight = p.getWeight() / thesum;
 			p.setWeight(weight);
 		}
 	}
 	
 	/**
 	 * Returns the effective sample size used for weeding out bad particles.
 	 */
 	public double effectiveSampleSize() {
 		// Effective Sample Size = (M)/(1 + c*(v_t)^2)
 		double ess = particleList.size() / (1 + coeffVariance());
 		return ess;
 	}
 	
 	/**
      * @TODO Kozak write what this does because I have no clue lolol --Jim
      */
 	public double coeffVariance() {
 		double c = 0;
 		for (int i = 0; i < particleList.size(); i++) {
 			c += Math.pow((particleList.size() * particleList.get(i).getWeight())-1,2); 
 		}
 		c = c * (1.0/particleList.size());
 		return c;
 	}
 	
 	/**
 	 * Called when the number of particles, according to the effective sample
 	 * size, drops below the threshold PARTICLE_TOLERANCE percentage.
 	 *
 	 * @return  integer array containing @TODO what does this return?
 	 */
 	public int[] resample() {
 		int[] index = new int[particleList.size()];
 		// require sumofi=1 to N (Wi) = 1
 		if (sumOf(particleList) != 1) {
 			return null;
 		}
 		// Q = sumsum(W); 
 		double[] q = cumsum(particleList);
 		// t = rand(N+1);
 		double[] t = randArray(particleList.size()+1); // t is an array of N+1 random numbers
 		// T = sort(t);
 		Arrays.sort(t);
 		//T(N+1) = 1; i = 1; j = 1
 		int i = 1;
 		int j = 1;
 		// while( i <= N) do
 		while( i <= particleList.size()) {
 			//  if T[i] < Q[j] then
 			if (t[i] < q[j]) { 
 				// Index[i] = j;
 				index[i] = j;
 				// i++
 				i++;
 			} else {
 				//  else
 				// j++;
 				j++;
 			}
 			//  end if
 			// end while
 		}
 		// Return(Index)
 		return index;
 	}
 
     /**
      * Calculates the probability of a particular particle represents the 
      * actual position and pose of the robot based on sensor dataz
      *
      * @param   p       Particle in question
      * @param   ranges  sensor dataz
      * @return  probability (weight) of the given particle
      */
 	private double prob(Particle p, double[] ranges) {
 		double varX = getVariance(0);
 		double varY = getVariance(1);
 		double varYaw = getVariance(2);
 		double sdevX = Math.sqrt(varX);
 		double sdevY = Math.sqrt(varY);
 		double sdevYaw = Math.sqrt(varYaw);
 		double prob = (1 / Math.sqrt(2*Math.PI*sdevX))
 			*Math.pow(Math.E,-1*Math.pow(p.getX()-meanX,2))/(2*varX);		
 		prob = prob * (1 / Math.sqrt(2*Math.PI*sdevY))
 		*Math.pow(Math.E,-1*Math.pow(p.getX()-meanY,2))/(2*varY);		
 		prob = prob * (1 / Math.sqrt(2*Math.PI*sdevYaw))
 		*Math.pow(Math.E,-1*Math.pow(p.getX()-meanYaw,2))/(2*varYaw);		
 		return prob;
 	}
 	
 	/**
 	 * Returns an array of random numbers 0.0 <= x < 1.0 of given size
 	 */
 	public double[] randArray(int size) {
 		double[] array = new double[size];
 		for (int i = 0; i < size;i ++) {
 			array[i] = Math.random();
 		}
 		return array;
 	}
 	
 	/*
 	 * Calculate running totals :
 	 * array[j] = sumof[l=0,j](list[l])
 	 */
 	public double[] cumsum(ArrayList<Particle> list) {
 		double sum = 0;
 		double[] array = new double[list.size()];
 		for (int i = 0; i < list.size(); i++) {
 			sum += list.get(i).getWeight();
 			array[i] = sum;
 		}
 		return array;
 	}
 	
 	/**
 	 * Sums up the weights of the all the existing particles and returns it
 	 * as a double
 	 */
 	public double sumOf(ArrayList<Particle> list) {
 		double sum = 0;
 		for (int i = 0; i < list.size();i++) {
 			sum += list.get(i).getWeight();
 		}
 		return sum;
 	}
 	
 	/**
 	 * Check if the robot has localized via the localized flag
 	 */
 	public synchronized boolean isLocalized() {
 		return localized;
 	}
 
     /**
      * Get an array containing all the weights of the existing particles
      */
 	public double[] getWeights() {
 		double[] weights = new double[particleList.size()];
 		for(int i = 0; i < weights.length; i++) {
 			weights[i] = particleList.get(i).getWeight();
 		}
 		return weights;
 	}
 	// we might not need this, not sure yet BROH.
 	private double getStandardDev(int v) {
 		return Math.sqrt(getVariance(v));
 	}
 	
 	/* 
 	 * Calculate variance for x/y/yaw
 	 * @param v - 0 for x, 1 for y, 2 for yaw
 	 */
 	private double getVariance(int v) {
 		double variance = 0;
 		for (Particle p : particleList) {
 			if (v==0) 
 				variance += (meanX - p.getX())*(meanX - p.getX());
 			if (v==1)
 				variance += (meanY - p.getY())*(meanY - p.getY());
 			if (v==2) 
 				variance += (meanYaw - p.getPose())*(meanYaw - p.getPose());
 		}
 		return variance;
 	}
 	
 	/*
 	 * Calculate mean values for x/y/yaw
 	 * @param int v - 0 for x, 1 for y, 2 for yaw 
 	 */
 	private double getMean(int v) {
 		double mean = 0;
 		for (Particle p : particleList) {
 			if (v == 0)
 				mean += p.getX();
 			if (v == 1) 
 				mean += p.getY();
 			if (v == 2)
 				mean += p.getPose();
 		}
 		mean = mean / particleList.size();
 		return mean;
 	}
 
     /**
      * Called by the wanderer to update the localizer with the robot's change
      * in position and pose, as well as the newest sensor dataz
      */
 	public synchronized void receiveUpdate(double dx, double dy, double dYaw,
 			double[] ranges) {
 		this.dx = dx;
 		this.dy = dy;
 		this.dYaw = dYaw;
 		this.ranges = ranges;
 
 		updateReady = true;
 		notifyAll();
 	}
 
 
     /**
      * Draws the map all pretty-like for us to look at
      */
 	public void drawMap() {
 		for (Particle p : particleList) {
 			gmap.setParticle(p.getX(), p.getY());
 		}
 	}
 	
 	/**
 	 * Checks all the particles to see if any of them are inside an obstacle.
 	 * This is impossible, so any that it finds get a weight of zero. This
 	 * ensures that they get wiped out and replaced on the next update.
 	 */
 	private void collisionCheck() {
 	    for (Particle p : particleList) {
 	        if (map[(int)Math.round(p.getX())][(int)Math.round(p.getY())] == 0)
 	            p.setWeight(0.0);
 	    }
 	}
 
 
 	/**
 	 * What this thread does when it runs, yo
 	 */
 	@Override
 	public void run() {
 		while (!localized) {
 			/* I think this is the right order...
 	        @TODO Make sure this is right, then do it, son.
 	        predict
 	        update
 	        if (effectiveSampleSize() < threshold) resample;
 	        Nah G, we do EFF in predict yoh. Otherwise, lookin good holmes I'm gonna do it. -AK
 			 */
 			predict();
 			update();
 
 		}
 	}
 
 }
