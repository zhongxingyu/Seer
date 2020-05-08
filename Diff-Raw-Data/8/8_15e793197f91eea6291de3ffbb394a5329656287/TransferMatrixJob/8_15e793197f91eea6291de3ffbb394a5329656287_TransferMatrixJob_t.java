 /**
  * Java Modular Image Synthesis Toolkit (JMIST)
  * Copyright (C) 2008-2013 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.eandb.jmist.framework.job;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import ca.eandb.jdcp.job.AbstractParallelizableJob;
 import ca.eandb.jdcp.job.TaskWorker;
 import ca.eandb.jmist.framework.ProbabilityDensityFunction;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.framework.SurfacePointGeometry;
 import ca.eandb.jmist.framework.measurement.CollectorSphere;
 import ca.eandb.jmist.framework.measurement.CollectorSphere.Callback;
 import ca.eandb.jmist.framework.measurement.SpectrophotometerCollectorSphere;
 import ca.eandb.jmist.framework.pdf.DiracProbabilityDensityFunction;
 import ca.eandb.jmist.framework.random.RandomUtil;
 import ca.eandb.jmist.framework.random.SimpleRandom;
 import ca.eandb.jmist.framework.scatter.SurfaceScatterer;
 import ca.eandb.jmist.math.MathUtil;
 import ca.eandb.jmist.math.SphericalCoordinates;
 import ca.eandb.jmist.math.Vector3;
 import ca.eandb.jmist.util.matlab.MatlabWriter;
 import ca.eandb.util.io.Archive;
 import ca.eandb.util.progress.ProgressMonitor;
 
 /**
  * @author Brad Kimmel
  *
  */
 public final class TransferMatrixJob extends AbstractParallelizableJob {
 
 	/** Serialization version ID. */
 	private static final long serialVersionUID = 843938200854073006L;
 
 	/** Parameterization strategy for exitant vector. */
 	public static interface ExitantVectorStrategy extends Serializable {
 
 		/**
 		 * Compute the vector to record to represent the outgoing direction.
 		 * @param in The incident direction.
 		 * @param out The exitant direction.
 		 * @return The reparameterized exitant direction.
 		 */
 		Vector3 getExitantVector(Vector3 in, Vector3 out);
 
 	}
 
 	/** Parameterization strategies for the exitant vector. */
 	public static enum ExitantVectorStrategies implements ExitantVectorStrategy {
 
 		/** Record the exitant vector directly. */
 		DIRECT {
 			public Vector3 getExitantVector(Vector3 in, Vector3 out) {
 				return out;
 			}
 		},
 
 		/**
 		 * Record the difference between the incident and exitant azimuthal
 		 * angles.
 		 */
 		RELATIVE {
 			public Vector3 getExitantVector(Vector3 in, Vector3 out) {
 				SphericalCoordinates si = SphericalCoordinates.fromCartesian(in.opposite());
 				SphericalCoordinates so = SphericalCoordinates.fromCartesian(out);
 				SphericalCoordinates r = SphericalCoordinates.canonical(so.polar(), so.azimuthal() - si.azimuthal());
 				return r.toCartesian();
 			}
 		},
 
 		/** Record the half-angle vector. */
 		HALF_ANGLE {
 			public Vector3 getExitantVector(Vector3 in, Vector3 out) {
 				return out.minus(in).unit();
 			}
 		},
 
 		/**
 		 * Record the half-angle vector, with the azimuthal angle expressed as
 		 * the difference between that of the half-angle vector and the
 		 * incident vector.
 		 */
 		HALF_ANGLE_RELATIVE {
 			public Vector3 getExitantVector(Vector3 in, Vector3 out) {
 				return RELATIVE.getExitantVector(in,
 						HALF_ANGLE.getExitantVector(in, out));
 			}
 		}
 
 	}
 
 	/** The <code>TaskWorker</code> that performs the work for this job. */
 	private final PhotometerTaskWorker worker;
 
 	private final SurfaceScatterer[] specimens;
 	private final ProbabilityDensityFunction[] channels;
 	private final String[] channelNames;
 	private final long samplesPerMeasurement;
 	private final long samplesPerTask;
 	private final int totalTasks;
 	private transient int[] sca;
 	private transient int[] abs;
 	private transient int[] cast;
 	private transient int nextMeasurementIndex = 0;
 	private transient long outstandingSamplesPerMeasurement = 0;
 	private transient int tasksReturned = 0;
 
 	/** A builder for creating <code>TransferMatrixJob</code>s. */
 	public static final class Builder {
 
 		/** Default number of samples per measurement. */
 		public static final long DEFAULT_SAMPLES_PER_MEASUREMENT = 10000;
 
 		private final List<SurfaceScatterer> specimens = new ArrayList<SurfaceScatterer>();
 		private final List<ProbabilityDensityFunction> channels = new ArrayList<ProbabilityDensityFunction>();
 		private final List<String> channelNames = new ArrayList<String>();
 		private boolean adjoint = false;
 		private long samplesPerMeasurement = 0;
 		private long samplesPerTask = 0;
 		private ExitantVectorStrategy exitantVectorStrategy = ExitantVectorStrategies.DIRECT;
 		private CollectorSphere incidentCollector = null;
 		private CollectorSphere exitantCollector = null;
 
 		/** Create the <code>TransferMatrixJob</code> instance. */
 		public TransferMatrixJob build() {
 			setDefaults();
 			return new TransferMatrixJob(
					specimens.toArray(new SurfaceScatterer[0]),
					channels.toArray(new ProbabilityDensityFunction[0]),
					channelNames.toArray(new String[0]),
 					samplesPerMeasurement,
 					samplesPerTask,
 					adjoint,
 					exitantVectorStrategy,
 					incidentCollector,
 					exitantCollector);
 		}
 
 		/** Sets any default parameters that were not specified by the client. */
 		private void setDefaults() {
 			if (samplesPerMeasurement <= 0) {
 				samplesPerMeasurement = DEFAULT_SAMPLES_PER_MEASUREMENT;
 			}
 			if (samplesPerTask <= 0) {
 				samplesPerTask = samplesPerMeasurement;
 			}
 			if (incidentCollector == null) {
 				incidentCollector = new SpectrophotometerCollectorSphere();
 			}
 			if (exitantCollector == null) {
 				exitantCollector = new SpectrophotometerCollectorSphere();
 			}
 		}
 
 		/**
 		 * Adds the provided <code>SurfaceScatterer</code> specimen to the job.
 		 * @param specimens The <code>SurfaceScatterer</code> specimen to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addSpecimen(SurfaceScatterer specimen) {
 			specimens.add(specimen);
 			return this;
 		}
 
 		/**
 		 * Adds the provided <code>SurfaceScatterer</code> specimens to the job.
 		 * @param specimens The <code>SurfaceScatterer</code> specimens to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addSpecimens(Iterable<SurfaceScatterer> specimens) {
 			for (SurfaceScatterer specimen : specimens) {
 				this.specimens.add(specimen);
 			}
 			return this;
 		}
 
 		/**
 		 * Add the specified channel to the job.
 		 * @param name The name of the channel.
 		 * @param channel The <code>ProbabilityDensityFunction</code> for the
 		 * 		channel to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addChannel(String name, ProbabilityDensityFunction channel) {
 			channelNames.add(name);
 			channels.add(channel);
 			return this;
 		}
 
 		/**
 		 * Add the specified channel to the job.
 		 * @param name The name of the channel.
 		 * @param channel The <code>ProbabilityDensityFunction</code> for the
 		 * 		channel to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addChannel(ProbabilityDensityFunction channel) {
 			channelNames.add(String.format("Channel %d", channels.size()));
 			channels.add(channel);
 			return this;
 		}
 
 		/**
 		 * Adds the specified wavelength to the job.
 		 * @param wavelength The wavelength to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addWavelength(double wavelength) {
 			String name = String.format("%d nm", (int) Math.round(wavelength / 1e-9));
 			return addChannel(name, new DiracProbabilityDensityFunction(wavelength));
 		}
 
 		/**
 		 * Adds the specified wavelengths to the job.
 		 * @param wavelength The array of wavelengths to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addWavelengths(double[] wavelengths) {
 			for (double wavelength : wavelengths) {
 				addWavelength(wavelength);
 			}
 			return this;
 		}
 
 		/**
 		 * Adds the specified wavelengths to the job.
 		 * @param wavelength The list of wavelengths to add.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder addWavelengths(Iterable<Double> wavelengths) {
 			for (double wavelength : wavelengths) {
 				addWavelength(wavelength);
 			}
 			return this;
 		}
 
 		/**
 		 * Sets a value indicating whether to trace rays in the reverse
 		 * direction (i.e., from the eye direction).
 		 * @param adjoint A value indicating whether to trace rays in the
 		 * 		reverse direction (i.e., from the eye direction).
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setAdjoint(boolean adjoint) {
 			this.adjoint = adjoint;
 			return this;
 		}
 
 		/**
 		 * Sets the number of samples to use for each measurement (i.e., for
 		 * each specimen/wavelength pair).
 		 * @param samplesPerMeasurement The number of samples to use for each
 		 * 		measurement.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setSamplesPerMeasurement(long samplesPerMeasurement) {
 			this.samplesPerMeasurement = samplesPerMeasurement;
 			return this;
 		}
 
 		/**
 		 * Sets the number of samples to use for each task.
 		 * @param samplesPerTask The number of samples to use for each task.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setSamplesPerTask(long samplesPerTask) {
 			this.samplesPerTask = samplesPerTask;
 			return this;
 		}
 
 		/**
 		 * Sets the number of samples to use for each task to target the
 		 * specified number of tasks per measurement.  The number of samples
 		 * per measurement must already have been set.
 		 * @param tasksPerMeasurement The target number of tasks per
 		 * 		measurement.
 		 * @return A reference to this <code>Builder</code>.
 		 * @throws IllegalStateException If the number of samples per
 		 * 		measurement has not been set.
 		 * @see #setSamplesPerMeasurement(long)
 		 * @see #setSamplesPerTask(long)
 		 */
 		public Builder setTasksPerMeasurement(long tasksPerMeasurement) {
 			if (samplesPerMeasurement <= 0) {
 				throw new IllegalStateException("Samples per measurement must be specified before tasks per measurement");
 			}
 			samplesPerTask = samplesPerMeasurement / tasksPerMeasurement;
 			if (samplesPerMeasurement % tasksPerMeasurement != 0) {
 				samplesPerTask++;
 			}
 			return this;
 		}
 
 		/**
 		 * Sets the exitant vector parameterization to use.
 		 * @param exitantVectorStrategy The <code>ExitantVectorStrategy</code>
 		 * 		to use.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setExitantVectorStrategy(ExitantVectorStrategy exitantVectorStrategy) {
 			this.exitantVectorStrategy = exitantVectorStrategy;
 			return this;
 		}
 
 		/**
 		 * Sets the <code>CollectorSphere</code> to use for both the incident
 		 * and exitant vectors.
 		 * @param collector The <code>CollectorSphere</code> to use.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setCollectorSphere(CollectorSphere collector) {
 			this.incidentCollector = collector;
 			this.exitantCollector = collector;
 			return this;
 		}
 
 		/**
 		 * Sets the <code>CollectorSphere</code> to use for the incident
 		 * directions.
 		 * @param incidentCollector The <code>CollectorSphere</code> to use.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setIncidentCollectorSphere(CollectorSphere incidentCollector) {
 			this.incidentCollector = incidentCollector;
 			return this;
 		}
 
 		/**
 		 * Sets the <code>CollectorSphere</code> to use for the exitant
 		 * directions.
 		 * @param exitantCollector The <code>CollectorSphere</code> to use.
 		 * @return A reference to this <code>Builder</code>.
 		 */
 		public Builder setExitantCollectorSphere(CollectorSphere exitantCollector) {
 			this.exitantCollector = exitantCollector;
 			return this;
 		}
 
 	}
 
 	private TransferMatrixJob(SurfaceScatterer[] specimens,
 			ProbabilityDensityFunction[] channels, String[] channelNames,
 			long samplesPerMeasurement, long samplesPerTask, boolean adjoint,
 			ExitantVectorStrategy exitantVectorStrategy,
 			CollectorSphere incidentCollector, CollectorSphere exitantCollector) {
 
 		this.worker						= new PhotometerTaskWorker(
 												incidentCollector, exitantCollector, 
 												exitantVectorStrategy, adjoint);
 		this.specimens					= specimens;
 		this.channels					= channels;
 		this.channelNames				= channelNames;
 		this.samplesPerMeasurement		= samplesPerMeasurement;
 		this.samplesPerTask				= samplesPerTask;
 		this.totalTasks					= specimens.length
 												* channels.length
 												* ((int) (samplesPerMeasurement / samplesPerTask) + ((samplesPerMeasurement % samplesPerTask) > 0 ? 1
 														: 0));
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jdcp.job.AbstractParallelizableJob#initialize()
 	 */
 	@Override
 	public void initialize() {
 		int numInSensors = worker.incidentCollector.sensors();
 		int numOutSensors = worker.exitantCollector.sensors();
 		this.sca = new int[channels.length * specimens.length * numInSensors * numOutSensors];
 		this.abs = new int[channels.length * specimens.length * numInSensors];
 		this.cast = new int[channels.length * specimens.length * numInSensors];
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#getNextTask()
 	 */
 	public synchronized Object getNextTask() {
 
 		if (outstandingSamplesPerMeasurement < samplesPerMeasurement) {
 
 			Task task = this.getPhotometerTask(this.nextMeasurementIndex);
 
 			if (++this.nextMeasurementIndex >= channels.length * specimens.length) {
 				this.outstandingSamplesPerMeasurement += this.samplesPerTask;
 				this.nextMeasurementIndex = 0;
 			}
 
 			return task;
 
 		} else {
 
 			return null;
 
 		}
 
 	}
 
 	private Task getPhotometerTask(int measurementIndex) {
 		return new Task(
 				this.getSpecimen(measurementIndex),
 				this.getChannel(measurementIndex),
 				Math.min(samplesPerTask, samplesPerMeasurement - outstandingSamplesPerMeasurement),
 				measurementIndex
 		);
 	}
 	
 	private SurfaceScatterer getSpecimen(int measurementIndex) {
 		return this.specimens[measurementIndex / channels.length];
 	}
 
 	private ProbabilityDensityFunction getChannel(int measurementIndex) {
 		int specimenMeasurementIndex = measurementIndex % channels.length;
 		return this.channels[specimenMeasurementIndex];
 	}
 	
 	private static final class TaskResult implements Serializable {
 		
 		/** Serialization version ID. */
 		private static final long serialVersionUID = -1374555802323806923L;
 		
 		public final int[] sca;
 		public final int[] abs;
 		public final int[] cast;
 		
 		public TaskResult(int numInSensors, int numOutSensors) {
 			sca = new int[numInSensors * numOutSensors];
 			abs = new int[numInSensors];
 			cast = new int[numInSensors];
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#submitTaskResults(java.lang.Object, java.lang.Object, ca.eandb.util.progress.ProgressMonitor)
 	 */
 	public void submitTaskResults(Object task, Object results,
 			ProgressMonitor monitor) {
 
 		Task		info			= (Task) task;
 		TaskResult	tr				= (TaskResult) results;
 		int			numInSensors	= worker.incidentCollector.sensors();
 		int			numOutSensors	= worker.exitantCollector.sensors();
 
 		MathUtil.addRange(sca, info.measurementIndex * numInSensors * numOutSensors, tr.sca);
 		MathUtil.addRange(abs, info.measurementIndex * numInSensors, tr.abs);
 		MathUtil.addRange(cast, info.measurementIndex * numInSensors, tr.cast);
 		
 		monitor.notifyProgress(++this.tasksReturned, this.totalTasks);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#isComplete()
 	 */
 	public boolean isComplete() {
 		return this.tasksReturned >= this.totalTasks;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#finish()
 	 */
 	public void finish() throws IOException {
 
 		MatlabWriter matlab = new MatlabWriter(createFileOutputStream("tm.mat"));
 		
 		int numInSensors = worker.incidentCollector.sensors();
 		int numOutSensors = worker.exitantCollector.sensors();
 		matlab.write("sca", sca, new int[]{ numOutSensors, numInSensors, channels.length, specimens.length });
 		matlab.write("abs", abs, new int[]{ numInSensors, channels.length, specimens.length });
 		matlab.write("cast", cast, new int[]{ numInSensors, channels.length, specimens.length });
 		matlab.write("channels", channelNames);
 		matlab.write("adjoint", worker.adjoint);
 		
 		writeCollectorSphere("incident", worker.incidentCollector, matlab);
 		writeCollectorSphere("exitant", worker.exitantCollector, matlab);
 
 		matlab.close();
 
 	}
 	
 	/**
 	 * Write the specifications for a <code>CollectorSphere</code> to the
 	 * provided MATLAB target.
 	 * @param name The name to use for the collector sphere.
 	 * @param collector The <code>CollectorSphere</code> to record.
 	 * @param matlab The <code>MatlabWriter</code> to write to.
 	 * @throws IOException If an error occurs while writing to the provided
 	 * 		<code>MatlabWriter</code>.
 	 */
 	private void writeCollectorSphere(String name, CollectorSphere collector, MatlabWriter matlab) throws IOException {
 		
 		int numSensors = collector.sensors();
 		double[] polar = new double[numSensors];
 		double[] azimuthal = new double[numSensors];
 		double[] solidAngle = new double[numSensors];
 		double[] projectedSolidAngle = new double[numSensors];
 		                       
 		for (int sensor = 0; sensor < numSensors; sensor++) {
 
 			SphericalCoordinates exitantAngle = collector.getSensorCenter(sensor);
 			
 			polar[sensor] = exitantAngle.polar();
 			azimuthal[sensor] = exitantAngle.azimuthal();
 			solidAngle[sensor] = collector.getSensorSolidAngle(sensor);
 			projectedSolidAngle[sensor] = collector.getSensorProjectedSolidAngle(sensor);
 
 		}
 		
 		matlab.write(name + "_sensorPolarAngle", polar);
 		matlab.write(name + "_sensorAzimuthalAngle", azimuthal);
 		matlab.write(name + "_sensorSolidAngle", solidAngle);
 		matlab.write(name + "_sensorProjectedSolidAngle", projectedSolidAngle);
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jdcp.job.AbstractParallelizableJob#archiveState(ca.eandb.util.io.Archive)
 	 */
 	@Override
 	protected void archiveState(Archive ar) throws IOException, ClassNotFoundException {
 		sca = (int[]) ar.archiveObject(sca);
 		abs = (int[]) ar.archiveObject(abs);
 		cast = (int[]) ar.archiveObject(cast);
 		nextMeasurementIndex = ar.archiveInt(nextMeasurementIndex);
 		outstandingSamplesPerMeasurement = ar.archiveLong(outstandingSamplesPerMeasurement);
 		tasksReturned = ar.archiveInt(tasksReturned);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#worker()
 	 */
 	public TaskWorker worker() {
 		return this.worker;
 	}
 
 	private static class Task implements Serializable {
 
 		/** Serialization version ID. */
 		private static final long serialVersionUID = -5989258164757195409L;
 		
 		public final SurfaceScatterer			specimen;
 		public final ProbabilityDensityFunction	channel;
 		public final long						samples;
 		public final int						measurementIndex;
 
 		public Task(SurfaceScatterer specimen,
 				ProbabilityDensityFunction channel,
 				long samples, int measurementIndex) {
 			this.specimen			= specimen;
 			this.channel			= channel;
 			this.samples			= samples;
 			this.measurementIndex	= measurementIndex;
 		}
 
 	}
 
 	private static class PhotometerTaskWorker implements TaskWorker, Serializable {
 
 		/** Serialization version ID. */
 		private static final long serialVersionUID = -7665433785262998136L;
 		
 		/**
 		 * The prototype <code>CollectorSphere</code> from which clones are
 		 * constructed to record hits to (incident direction).
 		 */
 		private final CollectorSphere incidentCollector;
 		
 		/**
 		 * The prototype <code>CollectorSphere</code> from which clones are
 		 * constructed to record hits to (exitant direction).
 		 */
 		private final CollectorSphere exitantCollector;
 		
 		/** The transformation to apply to the exitant vector before recording. */
 		private final ExitantVectorStrategy exitantVectorStrategy;
 
 		/** Indicates whether to trace the ray backwards (i.e., from the eye). */
 		private final boolean adjoint;
 
 		/**
 		 * Creates a new <code>PhotometerTaskWorker</code>.
 		 * @param specimen The <code>SurfaceScatterer</code> to be measured.
 		 * @param incidentCollector The prototype <code>CollectorSphere</code>
 		 * 		from which clones are constructed to record hits to (incident
 		 * 		direction).
 		 * @param exitantCollector The prototype <code>CollectorSphere</code>
 		 * 		from which clones are constructed to record hits to (exitant
 		 * 		direction).
 		 */
 		public PhotometerTaskWorker(CollectorSphere incidentCollector,
 				CollectorSphere exitantCollector,
 				ExitantVectorStrategy exitantVectorStrategy, boolean adjoint) {
 			this.incidentCollector = incidentCollector;
 			this.exitantCollector = exitantCollector;
 			this.exitantVectorStrategy = exitantVectorStrategy;
 			this.adjoint = adjoint;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.TaskWorker#performTask(java.lang.Object, ca.eandb.util.progress.ProgressMonitor)
 		 */
 		public Object performTask(Object task, ProgressMonitor monitor) {
 
 			Task				info			= (Task) task;
 			Random				rng				= new SimpleRandom();
 			final int			numInSensors	= incidentCollector.sensors();
 			final int			numOutSensors	= exitantCollector.sensors();
 			final TaskResult	result			= new TaskResult(numInSensors, numOutSensors);
 			final long			progInterval	= MathUtil.clamp(info.samples / 1000, 1, 1000);
 			long				progCountdown	= 1;
 			final int[]			sensor0			= new int[]{ -1 };
 			
 			for (long i = 0; i < info.samples; i++) {
 				
 				if (--progCountdown == 0) {
 					if (!monitor.notifyProgress((double) i / (double) info.samples)) {
 						monitor.notifyCancelled();
 						return null;
 					}
 					progCountdown = progInterval;
 				}
 				
 				Vector3 in;
 				sensor0[0] = -1;
 				do {
 					in = RandomUtil.uniformOnSphere(rng).toCartesian();
 					incidentCollector.record(in, new Callback() {
 						public void record(int sensor) {
 							result.cast[sensor]++;
 							sensor0[0] = sensor;
 						}
 					});
 				} while (sensor0[0] < 0);
 				
 				double wavelength = info.channel.sample(rng);
 				Vector3 v = info.specimen.scatter(SurfacePointGeometry.STANDARD, in, adjoint, wavelength, rng);
 				
 				if (v != null) {
 					v = exitantVectorStrategy.getExitantVector(in, v);
 					exitantCollector.record(v, new Callback() {
 						public void record(int sensor) {
 							result.sca[sensor0[0] * numOutSensors + sensor]++;
 						}
 					});
 				} else {
 					result.abs[sensor0[0]]++;
 				}
 				
 			}
 			
 			monitor.notifyProgress(1.0);
 			monitor.notifyComplete();
 			
 			return result;
 			
 		}
 
 	}
 
 }
