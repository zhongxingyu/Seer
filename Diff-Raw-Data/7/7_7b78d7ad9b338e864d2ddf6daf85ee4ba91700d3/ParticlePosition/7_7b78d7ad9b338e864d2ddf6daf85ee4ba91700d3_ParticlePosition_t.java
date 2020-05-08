 package pf.particle;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Random;
 
 import pf.distr.NormalDistribution;
 
 import pf.floors.AreaLayerModel;
 import pf.floors.Area;
 import pf.floors.EmptyArea;
 
 import pf.particle.PositionModel;
 import pf.particle.PositionModelUpdateListener;
 import pf.particle.MotionProvider;
 import pf.particle.ProbabilityMap;
 
 //import pf.service.wifi.WifiPositionUpdateListener;
 
 import pf.utils.GridPoint2D;
 import pf.utils.Line2D;
 import pf.utils.Point2D;
 import pf.utils.Rectangle;
 
 
 
 /**
  * ParticlePosition class. Represents a model of position computation by means
  * of a particle filter.
  */
 public class ParticlePosition implements PositionModel {
  // also implements WifiPositionUpdateListener 
 
 	private Set<Particle> particles;
 	private Area mArea;
 	private PositionModelUpdateListener mPositionListener;
 	private MotionProvider mProvider;
 	private double[] mCloudAverageState;
 
 	private ProbabilityMap mWifiProbabilityMap;
 
 	private static final int DEFAULT_PARTICLE_COUNT = 100;
 	private static final double DEFAULT_ALPHA = .99f;
 	private static final double HEADING_SIGMA = (double) (Math.PI * 10f / 180f);
 	private static final double DEFAULT_STEP_LENGTH = .70f;
 	private static final double DEFAULT_STEP_LENGTH_SPREAD = .1f * DEFAULT_STEP_LENGTH;
 	private static final double HEADING_DEFLECTION = 0.0f;
 	private static final double DEFAULT_HEADING_SPREAD = 10f / 180f * (double) Math.PI;
 	private static final int DEFAULT_WEIGHT = 1000;
 	private static final double NANO = Math.pow(10, 9);
 
 	private double mPositionSigma = 1.0f;
 	private double mHeading; // yaw 
 	private double[] mCoords;
 	private Rectangle mBox;
 
 	private int mNumberOfParticles;
 	private double mStepProbability;
 	private double mStepLength;
 	private double mStepLengthSpread;
 	private double mHeadingSpread;
 	private boolean mCheckWallsCollisions = true;
 
 	private ParticleGenerationMode mParticleGeneration = ParticleGenerationMode.GAUSSIAN;
 
 	// private ParticleGenerationMode mParticleGeneration = ParticleGenerationMode.UNIFORM;
 
 	public enum ParticleGenerationMode {
 		GAUSSIAN, UNIFORM
 	}
 
 
 
 	public ParticlePosition(PositionModelUpdateListener positionListener) {
 
 		int numberOfParticles = mNumberOfParticles;
 		particles = new HashSet<Particle>(numberOfParticles);
 		while (numberOfParticles > 0) {
 			particles.add(Particle.polarNormalDistr(-30, 25, 1, 
 					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
 					mStepLengthSpread, DEFAULT_WEIGHT));
 			numberOfParticles--;
 		}
 		
 		for (Particle p : particles) {
 			System.out.println(p);
 		}
 		mArea = new EmptyArea();
 		mPositionListener = positionListener;
 
 		mCoords = new double[4];
 		mCloudAverageState = new double[4];
 	}
 
 
 
 	/**
 	 * ParticlePosition model constructor.
 	 * 
 	 * @param numberOfParticles
 	 * @param posX
 	 *            x coordinate of the position
 	 * @param posY
 	 *            y coordinate of the position
 	 * @param sigma
 	 *            standard deviation of the distance from the posX, posY
 	 * @param positionListener
 	 */
 	/*
 	public ParticlePosition(double posX, double posY, double sigma, int area,
 			PositionModelUpdateListener positionListener) {
 		this(posX, posY, sigma, area, positionListener, null);
 	}*/
 
 	/**
 	 * ParticlePosition model constructor.
 	 * 
 	 * @param numberOfParticles
 	 * @param posX
 	 *            x coordinate of the position
 	 * @param posY
 	 *            y coordinate of the position
 	 * @param sigma
 	 *            standard deviation of the distance from the posX, posY
 	 * @param positionListener
 	 */
 	public ParticlePosition(double posX, double posY, double sigma,
 			PositionModelUpdateListener positionListener) {
 		this(positionListener);
 		mPositionSigma = sigma;
 		setPosition(posX, posY, DEFAULT_WEIGHT);
 	}
 
 
 	public ParticlePosition(double posX, double posY, double sigma,
 			PositionModelUpdateListener positionListener,
 			ParticleGenerationMode particleGeneration) {
 		this(positionListener);
 		mPositionSigma = sigma;
 		mParticleGeneration = particleGeneration;
 		setPosition(posX, posY, DEFAULT_WEIGHT);
 	}
 
 
 
 	public ParticlePosition(double x, double y) {
 		//this(x, y, 1, null, ParticleGenerationMode.GAUSSIAN);
 		int numberOfParticles = DEFAULT_PARTICLE_COUNT;
 		particles = new HashSet<Particle>(numberOfParticles);
 		while (numberOfParticles > 0) {
 			particles.add(Particle.polarNormalDistr(x, y, 1, 
 					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
 					mStepLengthSpread, DEFAULT_WEIGHT));
 			numberOfParticles--;
 		}
 		
 		mArea = new EmptyArea();
 
 		mCoords = new double[4];
 		mCloudAverageState = new double[4];
 	}
 
 
 
 	public void setArea(Area area) {
 		mArea = area;
 	}
 
 
 
 	public Area getArea() {
 		return mArea;
 	}
 
 
 	@Override
 	public void setPositionProvider(MotionProvider provider) {
 		if (mProvider != null) {
 			mProvider.unregister(this);
 		}
 		this.mProvider = provider;
 		mProvider.register(this);
 	}
 
 
 
 	@Override
 	public void setPosition(double posX, double posY, int weight) {
 		if (mParticleGeneration == ParticleGenerationMode.GAUSSIAN) {
 			setPositionNormDistr(posX, posY, mPositionSigma, weight);
 		} else {
 			setPositionEvenlySpread(posX, posY, 2*mPositionSigma, 2*mPositionSigma, DEFAULT_WEIGHT);
 		}
 	}
 
 
 	public void setPositionNormDistr(double posX, double posY, double sigma, int weight) {
 		int number = mNumberOfParticles;
 		particles = new HashSet<Particle>(mNumberOfParticles);
 		while (number > 0) {
 			particles.add(Particle.polarNormalDistr(posX, posY, sigma,
 					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
 					mStepLengthSpread, weight));
 			number--;
 		}
 		computeCloudAverageState();
 	}
 
 
 	public void setPositionEvenlySpread(double posX, double posY, double spreadX, double spreadY, int weight) {
 		int number = mNumberOfParticles;
 		particles = new HashSet<Particle>(mNumberOfParticles);
 		while (number > 0) {
 			particles.add(Particle.evenSpread(posX, posY, spreadX, spreadY,
 					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
 					mStepLengthSpread, weight));
 			number--;
 		}
 		computeCloudAverageState();
 	}
 
 
 
 	private void setPositionBasedOnProbabilityMap(ProbabilityMap map, int area) {
 
 		particles = new HashSet<Particle>(mNumberOfParticles);
 		int gridSpacing = map.getGridSize();
 		GridPoint2D origin = map.getOrigin();
 		GridPoint2D size = map.getSize();
 		double total = 0.0;
 		for (int x = origin.x; x < origin.x + size.x; x++) {
 			for (int y = origin.y; y < origin.y + size.y; y++) {
 				total += map.getProbability(x, y);
 			}
 		}
 		System.out.println("total probability sum = " + total);
 		for (int x = origin.x; x < origin.x + size.x; x++) {
 			for (int y = origin.y; y < origin.y + size.y; y++) {
 
 				System.out.println("setPositionBasedOnProbabilityMap: setting position on "+ x + " " + y);
 				double prob = map.getProbability(x, y);
 				int number = (int) (prob * mNumberOfParticles / total);
 				if (prob > 0 && number == 0)
 					number = 1;
 
 				while (number > 0) {
 					particles.add(Particle.evenSpread(gridSpacing * x, gridSpacing * y, gridSpacing,
 							gridSpacing, HEADING_DEFLECTION, mHeadingSpread,
 							mStepLength, mStepLengthSpread, DEFAULT_WEIGHT));
 					number--;
 				}
 			}
 		}
 		computeCloudAverageState();
 	}
 
 
 
 	/*
 	@Override
 	public PositionRenderer getRenderer() {
 		return new ParticlePositionRenderer(this);
 	}*/
 
 
 
 	/**
 	 * Box Rectangle of the bounding box of all particles.
 	 * 
 	 * @return
 	 */
 	public Rectangle getBox() {
 		return mBox;
 	}
 
 
 
 	public AreaLayerModel getWallsModel() {
 		return mArea.getWallsModel();
 	}
 
 
 	private void adjustStepLengthDistribution() {
 		/*
 		 * for (Particle particle: particles) {
 		 * 
 		 * }
 		 */
 	}
 
 	private void adjustHeadingDistribution() {
 
 		double stdDeviation = 0.0;
 		for (Particle particle : particles) {
 			stdDeviation += particle.state[2] - mCoords[2];
 		}
 
 		for (Particle particle : particles) {
 			particle.state[2] = mCoords[2] + mHeadingSpread
 					* NormalDistribution.inverse(Math.random());
 		}
 	}
 
 
 	/**
 	 * Compute new position of the particle cloud based on a step event.
 	 * 
 	 * @param hdg
 	 * @param length
 	 */
 	public void onStep(double hdg, double length) {
 
 		if (length > -5.0) {
 			System.out.println("onStep(hdg: " + hdg + ", length: " + length + ")\n");
 			HashSet<Particle> living = new HashSet<Particle>(particles.size());
 			for (Particle particle : particles) {
 				Particle newParticle = updateParticle(particle, hdg, length);
 				if (newParticle.getWeight() > 0) {
 					living.add(newParticle);
 				}
 			}
 			particles.clear();
 			particles.addAll(living);
 			System.out.println("No. particles = " + particles.size());
 
 
 			if (particles.size() < 0.65*DEFAULT_PARTICLE_COUNT) {
 				System.out.println("Too few particles! Resampling...");
 				resample();
 				System.out.println("After resampling: No. particles = " + particles.size());
 			}
 
 			computeCloudAverageState();
 
 			// adjustStepLengthDistribution();
 			// adjustHeadingDistribution();
 		}
 	}
 
 
 	/**
 	 * Compute new position of the particle cloud based on a step event.
 	 * 
 	 * @param alpha
 	 * @param hdg
 	 * @param hdgSpread
 	 * @param length
 	 * @param lengthSpread
 	 */
 	public void onStep(double alpha, double hdg, double hdgSpread,
 			double length, double lengthSpread) {
 
 		if (length > -5.0) {
 
 			System.out.println("onStep(hdg: " + hdg + ", length: " + length + ", hdgSpread: " + hdgSpread + 
 				", lengthSpread: " + lengthSpread);
 
 			HashSet<Particle> living = new HashSet<Particle>();
 			for (Particle particle : particles) {
 				if (Math.random() > alpha) {
 					living.add(particle);
 					continue;
 				}
 				double lengthFactor = 1.0 + lengthSpread * NormalDistribution.inverse(Math.random()) / length;
 				Particle newParticle = updateParticle(particle, hdg + hdgSpread
 						* NormalDistribution.inverse(Math.random()), lengthFactor);
 				if (newParticle.getWeight() > 0) {
 					living.add(particle);
 				}
 			}
 			particles.clear();
 			particles.addAll(living);
 
 			if (particles.size() < 0.65 * mNumberOfParticles) {
 				resample();
 			}
 
 			System.out.println("onStep resampling finished: No. particles = " + particles.size());
 
 			computeCloudAverageState();
 		}
 	}
 
 
 
 
 	/**
 	 * Update state of a single particle
 	 * 
 	 * @param particle
 	 *            updated particle
 	 * @param hdg
 	 *            heading in which the particle moves
 	 * @param lengthModifier
 	 *            distance of the particle travel
 	 * @return an updated particle 
 	 */
 	private Particle updateParticle(Particle particle, double hdg, double length) {
 
 		Random ran = new Random();
 
		//System.out.println("updateParticle(): hdg = " + hdg + ", length = " + length);
 		double[] state = particle.getState();
 
 		// Gaussian noise: http://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
		double deltaX = (length * Math.sin(hdg)) + ran.nextGaussian() * 0.5;
		double deltaY = (length * Math.cos(hdg)) + ran.nextGaussian() * 0.5;
 		Line2D trajectory = new Line2D(state[0], state[1], state[0] + deltaX, state[1] + deltaY);
 
 
 		if (mArea != null) {
 
 			// wall collision
 			if (mCheckWallsCollisions) {
 				Collection<Line2D> walls = mArea.getWallsModel().getWalls();
 				for (Line2D wall: walls) {
 					if (trajectory.intersect(wall)) {
 						//System.out.println("Particle collided with wall and is assigned weight 0!");
 						mNumberOfParticles--;
 						return particle.copy(0);   // Return a dead particle of weight 0
 					}
 				}
 			}
 		}
 
 		state[0] += deltaX;
 		state[1] += deltaY;
 
 		return new Particle(state[0], state[1], hdg, length, particle.getWeight());
 	}
 
 
 
 
 	/**
 	 * 
 	 * @param particle
 	 * @return
 	 */
 	public double getSurvivalProbability(Particle particle) {
 
 		return mWifiProbabilityMap.getProbability(particle.state[0], particle.state[1]);
 
 	}
 
 
 
 	/**
 	 * Update particle cloud based on a new RSS measurement and image.
 	 */
 	public void onRssImageUpdate(double sigma, double x, double y) {
 
 		System.out.println("onRssMeasurement()");
 		HashSet<Particle> living = new HashSet<Particle>();
 
 
 		if (particles.isEmpty()) {
 			System.out.println("Particles don't exist! Regenerating particles based on WiFi location");
 
 			int numberOfParticles = DEFAULT_PARTICLE_COUNT;
 			particles = new HashSet<Particle>(numberOfParticles);
 			while (numberOfParticles > 0) {
 				particles.add(Particle.polarNormalDistr(x, y, 1, 
 							HEADING_DEFLECTION, mHeadingSpread, mStepLength,
 							mStepLengthSpread, DEFAULT_WEIGHT));
 				numberOfParticles--;	
 				}	
 			mNumberOfParticles = DEFAULT_PARTICLE_COUNT;
 		}
 
 
 		else {
 
 			for (Particle particle : particles) {
 				Particle newParticle = particle.copy(particle.getWeight());
 
 
 				double result = (particle.getX()-x)*(particle.getX()-x)+(particle.getY()-y)*(particle.getY()-y);
 				double firstPart = 1.0/(Math.sqrt(2.0*Math.PI) * sigma);
 				double secondPart = Math.exp(-result/(2.0 * sigma * sigma));
 				double finalResult = firstPart * secondPart;
 				//System.out.println("finalResult: " + finalResult);
 
 				newParticle.setWeight((int)(Math.round(particle.getWeight()*finalResult)));
 
 
 				//System.out.println("Updated particle weight:" + newParticle.getWeight());
 				if (newParticle.getWeight() >= 1) {
 					living.add(newParticle);
 				}
 			}
 			particles.clear();
 			particles.addAll(living);
 			System.out.println(particles.size());
 			System.out.println("WiFi/Image update finished! Resampling...");
 			resample();
 		}
 		
 		System.out.println("After resampling: No. particles = " + particles.size());
 
 		computeCloudAverageState();
 	}
 
 
 
 
 
 	/**
 	 * Computes average state values of the particle cloud.
 	 */
 	private void computeCloudAverageState() {
 
 		mCloudAverageState[0] = mCloudAverageState[1] = mCloudAverageState[2] = mCloudAverageState[3] = 0.0;
 		double[] max = new double[] { Double.NEGATIVE_INFINITY,
 				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
 				Double.NEGATIVE_INFINITY };
 		double[] min = new double[] { Double.POSITIVE_INFINITY,
 				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
 				Double.POSITIVE_INFINITY };
 		for (Particle particle : particles) {
 			for (int i = 0; i < particle.getState().length-1; i++) {
 				mCloudAverageState[i] += particle.getState()[i];
 				min[i] = Math.min(min[i], particle.getState()[i]);
 				max[i] = Math.max(max[i], particle.getState()[i]);
 			}
 		}
 		for (int i = 0; i < 4; i++) {
 			mCloudAverageState[i] /= particles.size();
 		}
 
 		System.out.println("Avg x: " + mCloudAverageState[0] + " Avg y: " + mCloudAverageState[1]);
 
 
 		mBox = new Rectangle(min[0] - 2 * DEFAULT_STEP_LENGTH, min[1] - 2
 				* DEFAULT_STEP_LENGTH, max[0] + 2 * DEFAULT_STEP_LENGTH, max[1]
 				+ 2 * DEFAULT_STEP_LENGTH);
 
 		long timestamp = System.nanoTime();
 		if (mArea != null) {
 			mArea.setBoundingBox(mBox);
 		}
 		//Log.d(TAG, "computeCloudAverageState: mBox = " + mBox);
 		//Log.d(TAG, "setBoundingBox took " + (System.nanoTime() - timestamp) / NANO);
 		//Log.d(TAG, "working set has " + mArea.getWallsModel().getWorkingSet().size() + " walls");
 
 	}
 
 
 
 
 	/**
 	 * Resample particles. A particle is selected at a frequency proportional to its weight.
 	 * Newly generated particles all have DEFAULT_WEIGHT.
 	 */
 	private void resample() {
 
 		ArrayList<Particle> temp = new ArrayList<Particle>();
 		ArrayList<Double> freq = new ArrayList<Double>();
 		temp.addAll(particles);
 		System.out.println("particles size: "+particles.size());
 		System.out.println("temp size: "+temp.size());
 		particles.clear();
 		mNumberOfParticles = 0;
 
 		int sum = 0;
 		for (Particle p: temp) {
 			sum += p.getWeight();
 		}
 		double cumulativeFreq = 0;
 		for (int i=0; i<temp.size(); i++) {
 			cumulativeFreq += temp.get(i).getWeight() / sum;
 			freq.add(new Double(cumulativeFreq));
 		}
 
 		Random generator = new Random();
 		double r = generator.nextDouble();
 
 		for (int i=0; i<DEFAULT_PARTICLE_COUNT; i++) {
 			for (int j=0; j<temp.size(); j++) {
 				if (r >= freq.get(j)) {
 					particles.add(temp.get(j).copy(DEFAULT_WEIGHT));
 					mNumberOfParticles++;
 					break;
 				}
 			}
 			r = generator.nextDouble();
 		}
 	}
 
 
 
 
 	/**
 	 * Retrieve the collection of particles.
 	 * 
 	 * @return the collection of particles.
 	 */
 	public Collection<Particle> getParticles() {
 		return particles;
 	}
 
 
 	@Override
 	public String toString() {
 		return "particle(count: " + particles.size() + ", hdg:"
 				+ String.format("%.2f", 180 * mHeading / Math.PI) + ", x:"
 				+ String.format("%.2f", mCoords[0]) + ", y:"
 				+ String.format("%.2f", mCoords[1]) + ", hdg:"
 				+ String.format("%.2f", mCoords[2]) + ", step:"
 				+ String.format("%.2f", mCoords[3]);
 	}
 
 
 
 
 	private double mLastNotifiedHeading;
 	private static final double HEADING_DIFF_THRESHOLD = (double) (5f / 180f * Math.PI);
 
 
 	@Override
 	public void positionChanged(double heading, double x, double y) {
 		mHeading = heading;
 
 		if (x * x + y * y != 0.0) {
 			long timestamp = System.nanoTime();
 			onStep(mStepProbability, heading, HEADING_SIGMA,
 					DEFAULT_STEP_LENGTH, DEFAULT_STEP_LENGTH_SPREAD);
 			//Log.d(TAG, "onStep took " + (System.nanoTime() - timestamp) / NANO);
 			if (mPositionListener != null)
 				mPositionListener.updatePosition(this);
 		} else {
 			double delta = Math.abs(mHeading - mLastNotifiedHeading);
 			if (delta > HEADING_DIFF_THRESHOLD && mPositionListener != null) {
 				mLastNotifiedHeading = mHeading;
 				mPositionListener.updateHeading(this);
 			}
 		}
 	}
 
 
 	
 	public void wifiPositionChanged(double heading, double x, double y) {
 		// do nothing
 	}
 
 
 	
 	public ProbabilityMap getProbabilityMap() {
 		return mWifiProbabilityMap;
 	}
 
 
 	public double[] getCoords() {
 		mCoords[0] = mCloudAverageState[0];
 		mCoords[1] = mCloudAverageState[1];
 		mCoords[2] = mCloudAverageState[2];
 		mCoords[3] = mCloudAverageState[3];
 		return mCoords;
 	}
 
 
 	public String getCenter() {
 		return mCloudAverageState[0] + " " + mCloudAverageState[1];
 	}
 
 	
 	public double getPrecision() {
 		double sdX = 0.0;
 		double sdY = 0.0;
 		for (Particle particle: particles) {
 			sdX += (particle.state[0] - mCoords[0]) * (particle.state[0] - mCoords[0]);
 			sdY += (particle.state[1] - mCoords[1]) * (particle.state[1] - mCoords[1]);
 		}
 		sdX = Math.sqrt(sdX / particles.size());
 		sdY = Math.sqrt(sdY / particles.size());
 		return Math.max(sdX, sdY);
 	}
 
 	public double getHeading() {
 		return mHeading;
 	}
 }
