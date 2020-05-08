 /**
  *
  */
 package ca.eandb.jmist.framework.measurement;
 
 import java.util.Arrays;
 
 import ca.eandb.jmist.math.MathUtil;
 import ca.eandb.jmist.math.SphericalCoordinates;
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * A <code>CollectorSphere</code> where each stack of sensors spans the same
  * polar angle.
  * @author Brad Kimmel
  */
 public final class EqualPolarAnglesCollectorSphere extends
 		AbstractCollectorSphere {
 
 	/**
 	 * Creates a new <code>EqualPolarAnglesCollectorSphere</code>.
 	 * @param stacks The number of stacks to divide each hemisphere into.
 	 * @param slices The number of slices to divide the sphere into (about the
 	 * 		azimuthal angle).
 	 * @param upper A value indicating whether to record hits for the upper
 	 * 		hemisphere.
 	 * @param lower A value indicating whether to record hits for the lower
 	 * 		hemisphere.
 	 * @throws IllegalArgumentException if both <code>upper</code> and
 	 * 		<code>lower</code> are <code>false</code>.
 	 */
 	public EqualPolarAnglesCollectorSphere(int stacks, int slices, boolean upper, boolean lower) {
 
 		super();
 
 		if (!upper && !lower) {
 			throw new IllegalArgumentException("One of upper or lower must be true.");
 		}
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 
 		/* Each stack has "slices" sensors, except for the ones at the very top
 		 * of the upper hemisphere and the bottom of the lower hemisphere,
 		 * which have one (circular) patch.
 		 */
 		int sensors = hemispheres * ((stacks - 1) * slices + 1);
 
 		super.initialize(sensors);
 
 		this.stacks = stacks;
 		this.slices = slices;
 		this.upper = upper;
 		this.lower = lower;
 		
 		if (stacks == 1) {
 			boundaries = new double[]{ Math.PI / 2.0 };
 		} else {
 			double theta = 0.5 * Math.PI / (double) (stacks - 1);
 			double A = (((double) slices) + 2.0 * Math.cos(theta)) / (double) (slices + 2);
 			double B;
 			
 			boundaries = new double[stacks];
 			boundaries[0] = Math.acos(A);
 			for (int i = 1; i < stacks - 1; i++) {
 				theta = 0.5 * Math.PI * ((double) i / (double) (stacks - 1));
 				B = 2.0 * Math.cos(theta) - A;
 				boundaries[i] = Math.acos(B);
 				
 				System.err.printf("theta=%f; theta_interval=(%f, %f)", Math.toDegrees(theta), Math.toDegrees(boundaries[i - 1]), Math.toDegrees(boundaries[i]));
 				System.err.println();
 				assert(MathUtil.inRangeOO(theta, boundaries[i-1], boundaries[i]));
 				
 				A = B;
 			}
 			
 			boundaries[stacks - 1] = 0.5 * Math.PI;
 		}
 		
 
 	}
 
 	/**
 	 * Creates a copy of an existing
 	 * <code>EqualPolarAnglesCollectorSphere</code>.
 	 * @param other The <code>EqualPolarAnglesCollectorSphere</code> to copy.
 	 */
 	public EqualPolarAnglesCollectorSphere(EqualPolarAnglesCollectorSphere other) {
 		super(other);
 		this.stacks = other.stacks;
 		this.slices = other.slices;
 		this.upper = other.upper;
 		this.lower = other.lower;
 		this.boundaries = other.boundaries;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.AbstractCollectorSphere#clone()
 	 */
 	@Override
 	public CollectorSphere clone() {
 		return new EqualPolarAnglesCollectorSphere(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorCenter(int)
 	 */
 	public SphericalCoordinates getSensorCenter(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		assert(hemispheres > 0);
 
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the center of
 		 * a patch on the lower hemisphere, then compute the center of the
 		 * corresponding patch on the upper hemisphere and then adjust it
 		 * after.
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		int stack;
 		int slice;
 
 		if (sensor == 0) {
 
 			/* The first sensor is the one at the top (or bottom). */
 			stack = slice = 0;
 
 		} else { /* sensor > 0 */
 
 			stack = (sensor - 1) / slices + 1;
 			slice = (sensor - 1) % slices;
 
 			if (this.upper && this.lower && sensorOnLower) {
 				slice = slices - 1 - slice;
 			}
 
 		}
 
 		double phi = 2.0 * Math.PI * ((double) slice / (double) slices);
 		double theta = 0.5 * Math.PI * ((double) stack / (double) (stacks - 1));
 
 		if (sensorOnLower) {
 			theta = Math.PI - theta;
 		}
 
 		return SphericalCoordinates.canonical(theta, phi);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorProjectedSolidAngle(int)
 	 */
 	public double getSensorProjectedSolidAngle(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the projected
 		 * solid angle of a patch on the lower hemisphere, then compute the
  		 * projected solid angle of the corresponding patch on the upper
  		 * hemisphere (they will be equal).
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		assert(0 <= sensor && sensor < patchesPerHemisphere);
 
 		int stack = (sensor > 0) ? (sensor - 1) / slices + 1 : 0;
 
 		if (sensor > 0) {
 
 			assert(stack > 0);
 
 			return 0.5 * Math.PI * (Math.cos(2.0 * boundaries[stack - 1]) - Math.cos(2.0 * boundaries[stack])) / (double) slices;
 
 		} else { /* sensor == 0 */
 
 			return 0.5 * Math.PI * (1.0 - Math.cos(2.0 * boundaries[0]));
 
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorSolidAngle(int)
 	 */
 	public double getSensorSolidAngle(int sensor) {
 
 		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 		int patchesPerHemisphere = this.sensors() / hemispheres;
 
 		/* The sensor is on the lower hemisphere if there is no upper
 		 * hemisphere or if the sensor ID is in the second half of the
 		 * sensors.
 		 */
 		boolean sensorOnLower = !upper || sensor >= patchesPerHemisphere;
 
 		/* If both hemispheres are present, and we are computing the projected
 		 * solid angle of a patch on the lower hemisphere, then compute the
  		 * projected solid angle of the corresponding patch on the upper
  		 * hemisphere (they will be equal).
 		 */
 		if (upper && lower && sensorOnLower) {
 			sensor = this.sensors() - 1 - sensor;
 		}
 
 		assert(0 <= sensor && sensor < patchesPerHemisphere);
 
 		int stack = (sensor > 0) ? (sensor - 1) / slices + 1 : 0;
 
 		if (sensor > 0) {
 
 			assert(stack > 0);
 
 			return 2.0 * Math.PI * (Math.cos(boundaries[stack - 1]) - Math.cos(boundaries[stack])) / (double) slices;
 
 		} else { /* sensor == 0 */
 
 			return 2.0 * Math.PI * (1.0 - Math.cos(boundaries[0]));
 
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.AbstractCollectorSphere#getSensor(ca.eandb.jmist.toolkit.SphericalCoordinates)
 	 */
 	@Override
 	protected int getSensor(SphericalCoordinates v) {
 		v = v.canonical();
 
 		double theta = v.polar();
 		double phi = v.azimuthal();
 		boolean hitUpper = theta < (0.5 * Math.PI);
 
 		if ((hitUpper && !upper) || (!hitUpper && !lower)) {
 			return AbstractCollectorSphere.MISS;
 		}
 
 		theta = upper ? theta : Math.PI - theta;
 
 //		int hemispheres = (upper ? 1 : 0) + (lower ? 1 : 0);
 
 		int stack;
 		if (theta < 0.5 * Math.PI) {
 			stack = Arrays.binarySearch(boundaries, theta);
 			if (stack < 0) {
 				stack = -(stack + 1);
 			}
 			assert(stack < stacks);
 		} else { /* theta > 0.5 * Math.PI */
 			stack = Arrays.binarySearch(boundaries, Math.PI - theta);
 			if (stack < 0) {
 				stack = -(stack + 1);
 			}
 			assert(stack < stacks);
 			stack = 2 * stacks - 1 - stack;
 		}
 		
 		if (stack == 0) {
 			return 0;
 		} else if (stack == 2 * stacks - 1) {
 			return sensors() - 1;
 		} else { /* 0 < stack < 2 * stacks - 1 */
 			if (phi < 0.0) phi += 2.0 * Math.PI;
 			if (phi >= 2.0 * Math.PI) phi -= 2.0 * Math.PI;
 			
			int slice = (int) ((double) slices / (phi / (2.0 * Math.PI)));
 			slice = MathUtil.threshold(slice, 0, slices - 1);
 			
 			return 1 + (stack - 1) * slices + slice;
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.measurement.AbstractCollectorSphere#getSensor(ca.eandb.jmist.toolkit.Vector3)
 	 */
 	@Override
 	protected int getSensor(Vector3 v) {
 		return this.getSensor(SphericalCoordinates.fromCartesian(v));
 	}
 
 
 	/** The number of stacks per hemisphere. */
 	private final int stacks;
 
 	/** The number of slices. */
 	private final int slices;
 
 	/** A value indicating whether the upper hemisphere is measured. */
 	private final boolean upper;
 
 	/** A value indicating whether the lower hemisphere is measured. */
 	private final boolean lower;
 	
 	private final double[] boundaries;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 6947672588017728172L;
 
 }
