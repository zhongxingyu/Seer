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
 
 	private boolean localized;
 	private boolean updateReady;
 	private double dx,dy,dYaw;
 	private int[][] map;
 	private double[] ranges;
 	private ArrayList<Particle> particleList;
 	private Particle expectedLocation;
 	private static double particleTolerance = .95;
 	
 	/**
 	 * Create a new Localizer object to be used in a map with the given
 	 * dimensions. Fills the list of particles with NUM_PARTICLES particles or
 	 * mapw*maph particles, whichever is smaller. All particles are initalized
 	 * with equal weight, 1 divided by the number of particles.
 	 *
 	 * @param   map the map!
 	 */
 	public Localizer(int[][] map) {
 		localized = false;
 		this.map = map;
 		int mapw = map.length;
 		int maph = map[0].length;
 		int numParticles = (NUM_PARTICLES > mapw*maph) ? 
 		    mapw*maph : NUM_PARTICLES;
 		int ppp = Math.round(mapw*maph/numParticles);   // pixels per particle
 		particleList = new ArrayList<Particle>(numParticles);
 		int x = 0, y = 0;
 		double weight = 1.0/numParticles;
 		
 		for (int i = 0; i < numParticles; i++) {
 		    particleList.add(i, new Particle(x, y, 0, weight));
 		    x = (x+ppp) % mapw;
 		    if (x < ppp) y ++;
 		}
 		expectedLocation = null;
 		updateReady = false;
 
 	}
 	
 	public void predict(double[] scan) {
 		//
 		//   k = k + 1;
 		//   if (ESS(W) < B * M) then {Particle Population Depleted}
 		if (effectiveSampleSize() < (particleTolerance*particleList.size())) {
 		//     Index Resample(W);
 			int[] index = resample();
 			// Create new particles identical to those in index. (Not sure how we want to do this yet)
 		//     S^k_i = S^k_i(Index);
 		//   end if
 		}
 		//   for (j = 1 to M) do {Prediction after action A}
 		//     X^k+1_j = F(X^k_j,A)
 		//   end for
 		//   s = Sense()
 		//
 		//
 		//
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
 	}
 	public double effectiveSampleSize() {
 		// Effective Sample Size = (M)/(1 + c*(v_t)^2)
 		double ess = particleList.size() / (1 + coeffVariance());
 		return ess;
 	}
 	public double coeffVariance() {
 		double c = 0;
 		for (int i = 0; i < particleList.size(); i++) {
 			c += Math.pow((particleList.size() * particleList.get(i).getWeight())-1,2); 
 		}
 		c = c * (1.0/particleList.size());
 		return c;
 	}
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
 	public double sumOf(ArrayList<Particle> list) {
 		double sum = 0;
 		for (int i = 0; i < list.size();i++) {
 			sum += list.get(i).getWeight();
 		}
 		return sum;
 	}
 	public synchronized boolean isLocalized() {
 		return localized;
 	}
 	
 	public double[] getWeights() {
 	    double[] weights = new double[particleList.size()];
 	    for(int i = 0; i < weights.length; i++) {
 	        weights[i] = particleList.get(i).getWeight();
 	    }
 	    return weights;
 	}
 	
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
 	 * What this thread does when it runs, yo
 	 */
 	@Override
 	public void run() {
 	    while (!localized) {
 	        if (!updateReady) {
	            wait();
 	            continue;
 	        }
 	        /* I think this is the right order...
 	        
 	        predict
 	        update
 	        if (effectiveSampleSize() < threshold) resample;
 	        
 	        */
 	    }
 	}
 	
 }
