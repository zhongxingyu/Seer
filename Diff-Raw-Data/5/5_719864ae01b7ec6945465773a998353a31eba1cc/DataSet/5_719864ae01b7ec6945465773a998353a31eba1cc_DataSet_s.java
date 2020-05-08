 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.analysis;
 
 import gda.analysis.functions.dataset.IDataSetFunction;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.python.core.Py;
 import org.python.core.PyException;
 import org.python.core.PyInteger;
 import org.python.core.PyNone;
 import org.python.core.PySequence;
 import org.python.core.PySlice;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import Jama.Matrix;
 
 /**
  * This class is for populating the Java and Jython namespace for back compatibility.
  * <p>
  * Do not use for Java classes after switch-over
  * @deprecated Use {@link DoubleDataset}
  */
 @Deprecated
 public class DataSet extends DoubleDataset {
 	/**
 	 * Setup the logging facilities
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(DataSet.class);
 
 	private static final long updateInterval = 100; // period in milliseconds between issuing log info
 	transient private static long nextTime = 0;
 
 	transient public static int warnEverySoMany = 1000;
 	transient public static int warningNo = 0;
 
 	private static void issueDeprecatedWarning() {
 		if (warningNo++ % warnEverySoMany == 0) {
 			warningNo = 1; // reset to prevent overflow
 			if (System.currentTimeMillis() >= nextTime) {
 				nextTime = System.currentTimeMillis() + updateInterval;
 
 				Throwable e = new Throwable();
 				StackTraceElement[] trace = e.getStackTrace();
 				StackTraceElement[] newtrace = new StackTraceElement[trace.length - 1];
 				for (int i = 1; i < trace.length; i++) {
 					newtrace[i-1] = trace[i];
 				}
 				e.setStackTrace(newtrace);
 				logger.info("gda.analysis.DataSet is deprecated - use an AbstractDataset", e);
 				if (warnEverySoMany > 1)
 					logger.info(String.format("this is throttled (and rate-limited) - you are only seeing one message out of %d", warnEverySoMany));
 			}
 		}
 	}
 
 	/**
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset()}
 	 */
 	@Deprecated
 	public DataSet() {
 		issueDeprecatedWarning();
 	}
 
 	/**
 	 * Constructor setting a name
 	 * 
 	 * @param name
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset()}
 	 */
 	@Deprecated
 	public DataSet(String name) {
 		this();
 		this.name = name;
 	}
 
 	/**
 	 * @param name
 	 * @param dimensions
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(int...)} and {@link DoubleDataset#setName(String)}
 	 */
 	@Deprecated
 	public DataSet(String name, int... dimensions) {
 		super(dimensions);
 		this.name = name;
 		issueDeprecatedWarning();
 	}
 
 	/**
 	 * Constructs a zero-filled dataset with the specified dimensions
 	 * 
 	 * @param dimensions
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(int...)}
 	 */
 	@Deprecated
 	public DataSet(int... dimensions) {
 		super(dimensions);
 		issueDeprecatedWarning();
 	}
 
 	/**
 	 * Constructs a DataSet with the specified capacity using the given buffer if it has the correct size otherwise
 	 * create new buffer populated with copied elements and truncated or padded
 	 * 
 	 * @param data
 	 * @param dimensions
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)}
 	 */
 	@Deprecated
 	public DataSet(double[] data, int... dimensions) {
 		super(data, dimensions);
 		issueDeprecatedWarning();
 	}
 
 	/**
 	 * @param name
 	 * @param inData
 	 * @deprecated
 	 */
 	@Deprecated
 	public DataSet(String name, double[][] inData) {
 		this(inData);
 		this.name = name;
 	}
 
 	/**
 	 * Constructor which takes a double array of arrays to make a 2D dataset object.
 	 * 
 	 * @param inData
 	 *            The data to construct the dataset from
 	 * @deprecated
 	 */
 	@Deprecated
 	public DataSet(double[][] inData) {
 		this(inData.length, inData[0].length);
 
 		// store this in row-major order where we want the index to change slowest when
 		// going along each row in an array
 		int count = 0;
 		for (int i = 0, imax = inData.length; i < imax; i++) {
 			for (int j = 0, jmax = inData[0].length; j < jmax; j++) {
 				data[count++] = inData[i][j];
 			}
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param inData
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)} and {@link DoubleDataset#setName(String)}
 	 */
 	@Deprecated
 	public DataSet(String name, double[] inData) {
 		this(inData);
 		this.name = name;
 	}
 
 	/**
 	 * Constructor which takes a double Collection to make a 1D dataset object.
 	 * 
 	 * @param inData
 	 *            The data to construct the dataset from
 	 * @deprecated Use {@link AbstractDataset#createFromList(List)}
 	 */
 	@Deprecated
 	public DataSet(final List<Double> inData) {
 		issueDeprecatedWarning();
 		size = inData.size();
 		shape = new int[] { size };
 		try {
 			odata = data = new double[size];
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 		for (int i = 0; i < size; i++) {
 			data[i] = inData.get(i);
 		}
 	}
 
 	/**
 	 * Constructor which takes a double array to make a 1D dataset object.
 	 * 
 	 * @param inData
 	 *            The data to construct the dataset from
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)}
 	 */
 	@Deprecated
 	public DataSet(double[] inData) {
 		issueDeprecatedWarning();
 		size = inData.length;
 		shape = new int[] { size };
 		try {
 			odata = data = new double[size];
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 		for (int i = 0; i < size; i++) {
 			data[i] = inData[i];
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param height
 	 * @param width
 	 * @param inData
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)}
 	 */
 	@Deprecated
 	public DataSet(String name, int height, int width, double[] inData) {
 		this(height, width, inData);
 		this.name = name;
 	}
 
 	/**
 	 * Constructor for the dataset which creates a height x width matrix
 	 * 
 	 * @param height
 	 *            Height of the matrix
 	 * @param width
 	 *            Width of the matrix
 	 * @param inData
 	 *            Data in the matrix, where width is fastest direction
 	 */
 	@Deprecated
 	public DataSet(int height, int width, double[] inData) {
 		issueDeprecatedWarning();
 		// check the inputs are sensible
 		if (width <= 0) {
 			logger.error("width argument is " + width + " which is an illegal argument as it is zero or negative");
 			throw new IllegalArgumentException("width argument is " + width
 					+ " which is an illegal argument as it is zero or negative");
 		}
 
 		if (height <= 0) {
 			logger.error("height argument is " + height + " which is an illegal argument as it is zero or negative");
 			throw new IllegalArgumentException("height argument is " + height
 					+ " which is an illegal argument as it is zero or negative");
 		}
 
 		// first check to make sure there is enough data in the data array to
 		// fill the new vector
 		if ((width * height) > inData.length) {
 			logger.error("Not enough data provided to dataset " + inData.length + " provided but " + width * height
 					+ " needed");
 			throw new IllegalArgumentException("Not enough data provided to dataset " + inData.length
 					+ " provided but " + width * height + " needed");
 		} else if ((width * height) < inData.length) {
 			logger.warn("Not all the dataset given fits into the size specified");
 		}
 
 		// store this in row-major order where we want the index to change slowest when
 		// going along each row of an image
 		this.shape = new int[] { height, width };
 		size = width * height;
 		try {
 			odata = data = new double[size];
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 		for (int i = 0; i < size; i++) {
 			data[i] = inData[i];
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param depth
 	 * @param height
 	 * @param width
 	 * @param inData
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)} and {@link DoubleDataset#setName(String)}
 	 */
 	@Deprecated
 	public DataSet(String name, int depth, int height, int width, double[] inData) {
 		this(depth, height, width, inData);
 		this.name = name;
 	}
 
 	/**
 	 * Constructor for the dataset which will be three dimensional
 	 * 
 	 * @param depth
 	 *            depth of the dataset (slowest)
 	 * @param height
 	 *            height of the dataset (fast)
 	 * @param width
 	 *            width of the dataset (fastest)
 	 * @param inData
 	 *            The data to be read in.
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(double[], int...)}
 	 */
 	@Deprecated
 	public DataSet(int depth, int height, int width, double[] inData) {
 		issueDeprecatedWarning();
 
 		// check the inputs are sensible
 		if (width <= 0) {
 			logger.error("width argument is " + width + " which is an illegal argument as it is zero or negative");
 			throw new IllegalArgumentException("width argument is " + width
 					+ " which is an illegal argument as it is zero or negative");
 		}
 
 		if (height <= 0) {
 			logger.error("height argument is " + height + " which is an illegal argument as it is zero or negative");
 			throw new IllegalArgumentException("height argument is " + height
 					+ " which is an illegal argument as it is zero or negative");
 		}
 
 		if (depth <= 0) {
 			logger.error("depth argument is " + depth + " which is an illegal argument as it is zero or negative");
 			throw new IllegalArgumentException("depth argument is " + depth
 					+ " which is an illegal argument as it is zero or negative");
 		}
 
 		if ((width * height * depth) > inData.length) {
 			logger.error("Not enough data provided to dataset " + inData.length + " provided but " + width * height
 					* depth + " needed");
 			throw new IllegalArgumentException("Not enough data provided to dataset " + inData.length
 					+ " provided but " + width * height * depth + " needed");
 		}
 
 		// store this in row-major order where we want the index to change slowest when
 		// going along rows of image
 		this.shape = new int[] { depth, height, width };
 		size = width * height * depth;
 		try {
 			odata = data = new double[size];
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 		for (int i = 0; i < size; i++) {
 			data[i] = inData[i];
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param inputMatrix
 	 * @deprecated
 	 */
 	@Deprecated
 	public DataSet(String name, Matrix inputMatrix) {
 		this(inputMatrix);
 		this.name = name;
 	}
 
 	/**
 	 * Constructor which creates the dataset from a Jama matrix.
 	 * 
 	 * @param inputMatrix
 	 * @deprecated use {@link Jama.Matrix#getRowPackedCopy()} and {@link #DataSet(double[])}
 	 */
 	@Deprecated
 	public DataSet(Matrix inputMatrix) {
 		issueDeprecatedWarning();
 
 		// make sure the input Matrix Exists
 		if (inputMatrix == null) {
 			logger.error("Input matrix to the dataset is null");
 			throw new IllegalArgumentException("Input matrix to the dataset is null");
 		}
 		int dims[] = new int[] { inputMatrix.getRowDimension(), inputMatrix.getColumnDimension() };
 
 		// check the dimensions
 		for (int i = 0, imax = dims.length; i < imax; i++) {
 			// make sure the indices aren't zero or negative
 			if (dims[i] <= 0) {
 				logger.error("Argument " + i + " is " + dims[i]
 						+ " which is an illegal argument as it is zero or negative");
 				throw new IllegalArgumentException("Argument " + i + " is " + dims[i]
 						+ " which is an illegal argument as it is zero or negative");
 			}
 		}
 		try {
 			// store this in row-major order where we want the index to change slowest when
 			// going along rows of image
 			odata = inputMatrix.getRowPackedCopy();
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 		setData();
 		size = data.length;
 		shape = dims;
 	}
 
 	/**
 	 * Constructor based on replicating a dataset (does not copy reserved space)
 	 * 
 	 * @param inputDataSet
 	 *            The dataset to be replicated.
 	 * @deprecated Use {@link AbstractDataset#clone}
 	 */
 	@Deprecated
 	public DataSet(DataSet inputDataSet) {
 		issueDeprecatedWarning();
 
 		// make sure the input DataSet Exists
 		if (inputDataSet == null) {
 			logger.error("Input dataset to the dataset is null");
 			throw new IllegalArgumentException("Input dataset to the dataset is null");
 		}
 		try {
 			// first set the dimensions
 			shape = inputDataSet.getShape();
 
 			// copy across the data
 			if (inputDataSet.isContiguous()) {
 				odata = data = inputDataSet.data.clone();
 			} else {
 				odata = data = new double[inputDataSet.size];
 
 				IndexIterator iter = inputDataSet.getIterator();
 				for (int i = 0; iter.hasNext(); i++) {
 					data[i] = inputDataSet.data[iter.index];
 				}
 			}
 
 		} catch (OutOfMemoryError e) {
 			logger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 
 		name = new String(inputDataSet.getName());
 		size = inputDataSet.size;
 	}
 
 	/**
 	 * Constructs a DataSet using given sequence (of sequences...)
 	 * 
 	 * @param seq
 	 * @deprecated Use {@link DoubleDataset#createFromObject(Object)}
 	 */
 	@Deprecated
 	public DataSet(PySequence seq) {
 		issueDeprecatedWarning();
		DataSet d = convertToDataSet(createFromObject(seq));
 		odata = data = d.data;
 		shape = d.shape;
 		name = d.name;
 		size = d.size;
 	}
 
 	/**
 	 * Convert an abstract dataset to a DataSet
 	 * 
 	 * @param data
 	 * @return converted dataset
 	 */
 	public static DataSet convertToDataSet(ILazyDataset data) {
 		DoubleDataset dds;
 		DataSet result;
 		if (data instanceof DataSet) {
 			return (DataSet) data;
 		} else if (data instanceof DoubleDataset) {
 			dds = (DoubleDataset) data;
 		} else {
 			dds = (DoubleDataset) DatasetUtils.cast(DatasetUtils.convertToAbstractDataset(data), AbstractDataset.FLOAT64);
 		}
 		result = new DataSet(dds.getData(), dds.getShape());
 		result.setName(data.getName());
 		result.metadataStructure = dds.getMetadata();
 		return result;
 	}
 
 	/**
 	 * @deprecated Use {@link AbstractDataset#toString}
 	 */
 	@Deprecated
 	public void disp() {
 		TerminalPrinter.print(toString());
 	}
 
 	/**
 	 * Function that gets the size in each dimension of the dataset
 	 * 
 	 * @return an integer array of the size of each direction of the dataset
 	 * @deprecated Use {@link AbstractDataset#getShape}
 	 */
 	@Deprecated
 	public int[] getDimensions() {
 		return getShape();
 	}
 
 	/**
 	 * @return number of array dimensions
 	 * @deprecated Use {@link AbstractDataset#getRank}
 	 */
 	@Deprecated
 	public int getNdim() {
 		return getRank();
 	}
 
 	/**
 	 * @param shape
 	 * @return empty dataset of doubles
 	 * @deprecated Use {@link DoubleDataset#DoubleDataset(int...)} or {@link AbstractDataset#zeros}
 	 */
 	@Deprecated
 	public static DataSet zeros(int... shape) {
 		return new DataSet(shape);
 	}
 
 	/**
 	 * @param shape
 	 * @return empty dataset of doubles
 	 * @deprecated Use {@link DoubleDataset#ones(int...)} or {@link AbstractDataset#ones}
 	 */
 	@Deprecated
 	public static DataSet ones(int... shape) {
 		return zeros(shape).fill(1.0);
 	}
 
 	@Override
 	public DataSet clone() {
 		return new DataSet(this);
 	}
 
 	/**
 	 * @return a (contiguous) copy
 	 * @deprecated Use {@link DataSet#clone}
 	 */
 	@Deprecated
 	public DataSet copy() {
 		return clone();
 	}
 
 	/**
 	 * Function that returns a double array of the data in the Dataset
 	 * 
 	 * @return The double array containing the data
 	 * @deprecated Use {@link AbstractDataset#synchronizedCopy()} with {@link AbstractDataset#getBuffer()} or
 	 *             {@link DoubleDataset#getData()}
 	 */
 	@Deprecated
 	public synchronized double[] doubleArray() {
 		IndexIterator it = getIterator();
 		double[] result = new double[size];
 		for (int i = 0; it.hasNext(); i++) {
 			result[i] = data[it.index];
 		}
 		return result;
 	}
 
 	@Override
 	public double[] getBuffer() {
 		return data;
 	}
 
 	/**
 	 * Function that returns a 2D double array of the data in the dataset
 	 * 
 	 * @return The 2D array of doubles
 	 * @throws IllegalArgumentException
 	 *             This is thrown if the dataset can't be passed as a Matrix
 	 * @deprecated {@link DatasetUtils#createJavaArray(AbstractDataset)}
 	 */
 	@Deprecated
 	public synchronized double[][] doubleMatrix() throws IllegalArgumentException {
 		// only return if it's a 2D dataset
 		if (shape.length != 2) {
 			logger.error(
 					"doubleMatrix needs to be passed a 2-dimensional dataset, you passed it a {}-dimensional dataset",
 					shape.length);
 			throw new IllegalArgumentException(this + " needs to be passed a 2-dimensional dataset, you passed it a "
 					+ shape.length + "-dimensional dataset");
 		}
 		return (double[][]) DatasetUtils.createJavaArray(this);
 	}
 
 	/**
 	 * Convert this dataset to a double dataset
 	 * 
 	 * @return converted dataset
 	 */
 	public DoubleDataset convertToDoubleDataset() {
 		return super.getView();
 	}
 
 	/**
 	 * @param indices
 	 * @param values
 	 * @deprecated Use {@link AbstractDataset#put(int[], Object[])}
 	 */
 	@Deprecated
 	public void put(int[] indices, double[] values) {
 		put(indices, Arrays.asList(values).toArray());
 	}
 
 	/**
 	 * @param indices
 	 * @deprecated Use {@link AbstractDataset#take(int[], Integer)}
 	 */
 	@Deprecated
 	public DataSet take(int[] indices) {
 		return convertToDataSet(take(indices, null));
 	}
 
 	/**
 	 * Function that returns the JAMA matrix of the 2D dataset
 	 * 
 	 * @return The JAMA Matrix of the whole object
 	 * @throws IllegalArgumentException
 	 *             This is thrown if there are any problems, but mainly if the dataset isnt a 2D set that can be turned
 	 *             into a matrix.
 	 * @deprecated use {@link #getData} and {@link Jama.Matrix#Matrix(double[], int )}
 	 */
 	@Deprecated
 	public Matrix getJamaMatrix() throws IllegalArgumentException {
 		try {
 			return new Matrix(this.doubleMatrix());
 		} catch (IllegalArgumentException e) {
 			logger.error("{} needs to be passed a 2-dimensional dataset, you passed it a dataset of rank {}", this, shape.length);
 			throw new IllegalArgumentException(this
 					+ " needs to be passed a 2-dimensional dataset, you passed it a " + this.shape.length
 					+ "-dimensional dataset");
 		}
 	}
 
 	/**
 	 * @return variance
 	 * @deprecated Use {@link AbstractDataset#variance}
 	 */
 	@Deprecated
 	public double var() {
 		return variance().doubleValue();
 	}
 
 	/**
 	 * @param isDataSetWholePopulation
 	 * @return variance
 	 * @deprecated Use {@link AbstractDataset#variance(boolean)}
 	 */
 	@Deprecated
 	public double var(boolean isDataSetWholePopulation) {
 		return variance(isDataSetWholePopulation).doubleValue();
 	}
 
 	/**
 	 * @return standard deviation
 	 * @deprecated Use {@link AbstractDataset#stdDeviation}
 	 */
 	@Deprecated
 	public double std() {
 		return stdDeviation().doubleValue();
 	}
 
 	/**
 	 * @param isDataSetWholePopulation
 	 * @return standard deviation
 	 * @deprecated Use {@link AbstractDataset#stdDeviation(boolean)}
 	 */
 	@Deprecated
 	public double std(boolean isDataSetWholePopulation) {
 		return stdDeviation(isDataSetWholePopulation).doubleValue();
 	}
 
 	/**
 	 * @return root mean square value
 	 * @deprecated Use {@link AbstractDataset#rootMeanSquare}
 	 */
 	@Deprecated
 	public double rms() {
 		return rootMeanSquare().doubleValue();
 	}
 
 	/**
 	 * @return peak to peak value
 	 * @deprecated Use {@link AbstractDataset#peakToPeak}
 	 */
 	@Deprecated
 	public double range() {
 		return peakToPeak().doubleValue();
 	}
 
 	/**
 	 * @return absolute index of maximum
 	 * @deprecated Use {@link AbstractDataset#argMax()}
 	 */
 	@Deprecated
 	public int argmax() {
 		return argMax();
 	}
 
 	/**
 	 * @return absolute index of minimum
 	 * @deprecated Use {@link AbstractDataset#argMin()}
 	 */
 	@Deprecated
 	public int argmin() {
 		return argMin();
 	}
 
 	/**
 	 * Most basic form, assumes the points are equally spaced and calculates the derivative based on that. Very rough
 	 * and ready, but could be useful
 	 * 
 	 * @return The dataset containing the derivative.
 	 * @deprecated Use {@link #getIndices()} and {@link Maths#derivative(AbstractDataset, AbstractDataset, int)}
 	 */
 	@Deprecated
 	public DataSet diff() {
 		return diff(1);
 	}
 
 	/**
 	 * More interactive form of getting the derivative, which allows the user to select the amount of smoothing they
 	 * want to use.
 	 * 
 	 * @param n
 	 *            The spread on either side of the derivative calculation
 	 * @return The dataset containing the derivative.
 	 * @deprecated Use {@link #getIndices()} and {@link Maths#derivative(AbstractDataset, AbstractDataset, int)}
 	 */
 	@Deprecated
 	public DataSet diff(int n) {
 		// first make the x value dataset
 		DataSet xValues = getIndexDataSet();
 		return convertToDataSet(Maths.derivative(xValues, this, n));
 	}
 
 	/**
 	 * This calculates the derivative, based on the associated x coordinates passed to the function
 	 * 
 	 * @param xValues
 	 *            the associated x values.
 	 * @return The dataset containing the derivative
 	 * @deprecated Use {@link Maths#derivative(AbstractDataset, AbstractDataset, int)}
 	 */
 	@Deprecated
 	public DataSet diff(DataSet xValues) {
 		return convertToDataSet(Maths.derivative(xValues, this, 1));
 	}
 
 	/**
 	 * This calculates the derivative, based on the associated x coordinates passed to the function
 	 * 
 	 * @param xValues
 	 *            the associated x values.
 	 * @param n
 	 *            The spread on either side of the derivative calculation
 	 * @return The dataset containing the derivative
 	 * @deprecated Use {@link Maths#derivative(AbstractDataset, AbstractDataset, int)}
 	 */
 	@Deprecated
 	public DataSet diff(DataSet xValues, int n) {
 		return convertToDataSet(Maths.derivative(xValues, this, n));
 	}
 
 	/**
 	 * Generate an index dataset for current dataset. It is an error to call this for datasets that are not 1D
 	 * 
 	 * @return an index dataset
 	 * @deprecated Use {@link AbstractDataset#getIndices}
 	 */
 	@Deprecated
 	public DataSet getIndexDataSet() {
 		// now create another dataset to plot against
 		if (shape.length != 1) {
 			logger.error("Input dataset has dimensionality greater than one: {}", shape.length);
 			throw new IllegalArgumentException("Dimensionality of input dataset too large: " + shape.length);
 		}
 		DataSet x = new DataSet(this);
 		for (int i = 0; i < size; i++) {
 			x.data[i] = i;
 		}
 		return x;
 	}
 
 	/**
 	 * @return DataSet
 	 * @deprecated Use {@link Stats#averageDeviation}
 	 */
 	@Deprecated
 	public double averageDeviation() {
 		return ((Number) Stats.averageDeviation(this)).doubleValue();
 	}
 
 	/**
 	 * @return product of all elements in dataset
 	 * @deprecated Use {@link #product}
 	 */
 	@Deprecated
 	public double prod() {
 		return (Double) product();
 	}
 
 	/**
 	 * Function to subsample a dataset to a smaller dimensions by the mean value of a set of points
 	 * 
 	 * @param numberOfPoints
 	 * @return DataSet
 	 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.dataset.function.Downsample}
 	 */
 	@Deprecated
 	public DataSet subSampleMean(int numberOfPoints) {
 		int rank = getRank();
 		int[] binshape = new int[rank];
 		Arrays.fill(binshape, numberOfPoints);
 
 		Downsample ds = new Downsample(DownsampleMode.MEAN, binshape);
 		return convertToDataSet(ds.value(this).get(0));
 	}
 
 	/**
 	 * Function to subsample a dataset to a smaller dimensions by the max value of a set of points
 	 * 
 	 * @param numberOfPoints
 	 * @return DataSet
 	 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.dataset.function.Downsample}
 	 */
 	@Deprecated
 	public DataSet subSampleMax(int numberOfPoints) {
 		int rank = getRank();
 		int[] binshape = new int[rank];
 		Arrays.fill(binshape, numberOfPoints);
 
 		Downsample ds = new Downsample(DownsampleMode.MAXIMUM, binshape);
 		return convertToDataSet(ds.value(this).get(0));
 	}
 
 	/**
 	 * This command runs the function that is input into it, defined by the IDataSetFunction interface. It then calls the
 	 * execute method on that object and returns the result in the form of a list of datasets
 	 * 
 	 * @param function
 	 *            The DatasetToDatasetFunction that describes the operator to be performed.
 	 * @return A list of DataSets which is obtained from the functions execute method.
 	 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.dataset.function.DatasetToDatasetFunction#value(IDataset...)}
 	 */
 	@Deprecated
 	public List<DataSet> exec(IDataSetFunction function) {
 		return function.execute(this);
 	}
 
 	/**
 	 * @return first value contained in dataset
 	 * @deprecated Use get(0)
 	 */
 	@Deprecated
 	public double getFirstValue() {
 		return data[0];
 	}
 
 	/**
 	 * @return last value contained in dataset
 	 * @deprecated Use get()
 	 */
 	@Deprecated
 	public double getLastValue() {
 		return get(getNDPosition(size - 1));
 	}
 
 	/**
 	 * Create a 1D dataset
 	 * 
 	 * @param start
 	 *            begin
 	 * @param stop
 	 *            end
 	 * @param length
 	 *            number of points
 	 * @return Values in closed interval given by parameters
 	 * @deprecated Use {@link DatasetUtils#linSpace}
 	 */
 	@Deprecated
 	public static DataSet linspace(double start, double stop, int length) {
 		return convertToDataSet(DatasetUtils.linSpace(start, stop, length, AbstractDataset.FLOAT64));
 	}
 
 	public static DataSet arange(double stop) {
 		return arange(0, stop, 1);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @return dataset spaced by 1.0 in semi-open interval given by parameters
 	 * @deprecated Use {@link DoubleDataset#arange(double, double, double)}
 	 */
 	@Deprecated
 	public static DataSet arange(double start, double stop) {
 		return arange(start, stop, 1);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @return dataset spaced by 1.0 in semi-open interval given by parameters
 	 * @deprecated Use {@link DoubleDataset#arange(double, double, double)}
 	 */
 	@Deprecated
 	public static DataSet arange(double start, int stop) { // needed to stop AbstractDataset.arange being called by Jython
 		return arange(start, stop, 1);
 	}
 
 	public static DataSet arange(double start, double stop, double step) {
 		return convertToDataSet(AbstractDataset.arange(start, stop, step, AbstractDataset.FLOAT64));
 	}
 
 	/**
 	 * Jython method to create a DataSet
 	 *
 	 * @param mat
 	 * @return dataset populated by items in sequence
 	 */
 	public static DataSet array(Matrix mat) {
 		return new DataSet(mat);
 	}
 
 	/**
 	 * Jython method to create a dataset
 	 *
 	 * @param seq
 	 * @return dataset populated by items in sequence
 	 */
 	public static DataSet array(PySequence seq) {
 		return new DataSet(seq);
 	}
 
 	/**
 	 * @param stop
 	 * @return slice
 	 * @deprecated Use {@link #getSlice(int[], int[], int[])}
 	 */
 	@Deprecated
 	public DataSet getSlice(int stop) {
 		return getSlice(null, new int[] { stop }, null);
 	}
 
 	/**
 	 * @param stop
 	 * @return slice
 	 * @deprecated Use {@link #getSlice(int[], int[], int[])}
 	 */
 	@Deprecated
 	public DataSet getSlice(int[] stop) {
 		return getSlice(null, stop, null);
 	}
 
 	/**
 	 * @param start
 	 *            specifies the starting index
 	 * @param stop
 	 *            specifies the stopping index (nb, this is <b>not</b> included in the slice)
 	 * @return The dataset of the sliced data.
 	 * @deprecated Use {@link #getSlice(int[], int[], int[])}
 	 */
 	@Deprecated
 	public DataSet getSlice(int start, int stop) {
 		return getSlice(new int[] { start }, new int[] { stop }, null);
 	}
 
 	/**
 	 * @param stop
 	 * @return slice
 	 * @deprecated Use {@link #getSlice(int[], int[], int[])}
 	 */
 	@Deprecated
 	public DataSet getSlice(int[] start, int[] stop) {
 		return getSlice(start, stop, null);
 	}
 
 	/**
 	 * @param start
 	 *            specifies the starting index
 	 * @param stop
 	 *            specifies the stopping index (nb, this is <b>not</b> included in the slice)
 	 * @param step
 	 *            specifies the step size
 	 * @return The dataset of the sliced data.
 	 * @deprecated Use {@link #getSlice(int[], int[], int[])}
 	 */
 	@Deprecated
 	public DataSet getSlice(int start, int stop, int step) {
 		return getSlice(new int[] { start }, new int[] { stop }, new int[] { step });
 	}
 
 	@Override
 	public DataSet getSlice(int[] start, int[] stop, int[] step) {
 		return convertToDataSet(super.getSlice(start, stop, step));
 	}
 
 	/**
 	 * @param start
 	 * @param value
 	 * @deprecated Use {@link #setSlice(Object, int[], int[], int[])}
 	 */
 	@Deprecated
 	public void setSlice(int start, Object value) {
 		setSlice(value, new int[] { start }, null, null);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param value
 	 * @deprecated Use {@link #setSlice(Object, int[], int[], int[])}
 	 */
 	@Deprecated
 	public void setSlice(int start, int stop, Object value) {
 		setSlice(value, new int[] { start }, new int[] { stop }, null);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @param value
 	 * @deprecated Use {@link #setSlice(Object, int[], int[], int[])}
 	 */
 	@Deprecated
 	public void setSlice(int start, int stop, int step, Object value) {
 		setSlice(value, new int[] { start }, new int[] { stop }, new int[] { step });
 	}
 
 	/**
 	 * @param start
 	 * @param value
 	 * @deprecated Use {@link #setSlice(Object, int[], int[], int[])}
 	 */
 	@Deprecated
 	public void setSlice(int[] start, Object value) {
 		setSlice(value, start, null, null);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param value
 	 * @deprecated Use {@link #setSlice(Object, int[], int[], int[])}
 	 */
 	@Deprecated
 	public void setSlice(int[] start, int[] stop, Object value) {
 		setSlice(value, start, stop, null);
 	}
 
 	/**
 	 * @param data
 	 * @return dataset with axes reversed
 	 * @deprecated Use {@link DatasetUtils#transpose(AbstractDataset, int...)}
 	 */
 	@Deprecated
 	public static DataSet transpose(DataSet data) {
 		return convertToDataSet(DatasetUtils.transpose(data));
 	}
 
 	/**
 	 * @param data
 	 * @param axes
 	 * @return dataset with axes permuted
 	 * @deprecated Use {@link DatasetUtils#transpose(AbstractDataset, int...)}
 	 */
 	@Deprecated
 	public static DataSet transpose(DataSet data, int... axes) {
 		return convertToDataSet(DatasetUtils.transpose(data, axes));
 	}
 
 	/**
 	 * @param data
 	 * @return copy of an array flattened to 1D
 	 * @deprecated Use {@link #copy()} and {@link #flatten()}
 	 */
 	@Deprecated
 	public static DataSet flatten(DataSet data) {
 		DataSet newData = data.copy();
 		newData.setShape(new int[] { newData.getSize() });
 		return newData;
 	}
 
 	/**
 	 * Follows numpy api
 	 * @param a
 	 * @param repeats 
 	 * @return tiled dataset
 	 * @deprecated Use {@link DatasetUtils#repeat(AbstractDataset, int[], int)}
 	 */
 	@Deprecated
 	public static DataSet repeat(DataSet a, int repeats) {
 		return repeat(a, new int[] {repeats}, -1);
 	}
 
 	/**
 	 * Follows numpy api
 	 * @param a
 	 * @param repeats 
 	 * @return tiled dataset
 	 * @deprecated Use {@link DatasetUtils#repeat(AbstractDataset, int[], int)}
 	 */
 	@Deprecated
 	public static DataSet repeat(DataSet a, int[] repeats) {
 		return repeat(a, repeats, -1);
 	}
 
 	/**
 	 * Follows numpy api
 	 * @param a
 	 * @param repeats 
 	 * @param axis 
 	 * @return tiled dataset
 	 * @deprecated Use {@link DatasetUtils#repeat(AbstractDataset, int[], int)}
 	 */
 	@Deprecated
 	public static DataSet repeat(DataSet a, int repeats, int axis) {
 		return repeat(a, new int[] {repeats}, axis);
 	}
 
 
 	/**
 	 * Constructs a dataset which has its elements along an axis replicated from
 	 * the original dataset by the number of times given in the repeats array.
 	 * 
 	 * By default, axis=-1 implies using a flattened version of the input dataset 
 	 *
 	 * @param a
 	 * @param repeats 
 	 * @param axis
 	 * @return dataset
 	 * @deprecated Use {@link DatasetUtils#repeat(AbstractDataset, int[], int)}
 	 */
 	@Deprecated
 	public static DataSet repeat(DataSet a, int[] repeats, int axis) {
 		return convertToDataSet(DatasetUtils.repeat(a, repeats, axis));
 	}
 
 	/**
 	 * Construct a dataset that contains the original dataset repeated
 	 * the number of times in each axis given by corresponding entries in the reps array
 	 *
 	 * @param A
 	 * @param reps 
 	 * @return tiled dataset
 	 * @deprecated Use {@link DatasetUtils#tile(AbstractDataset, int...)}
 	 */
 	@Deprecated
 	public static DataSet tile(DataSet A, int... reps) {
 		return convertToDataSet(DatasetUtils.tile(A, reps));
 	}
 
 	/**
 	 * Permute copy of dataset's axes so that given order is old order:
 	 * 
 	 * <pre>
 	 *  axisPerm = (p(0), p(1),...) => newdata(n(0), n(1),...) = olddata(o(0), o(1), ...)
 	 *  such that n(i) = o(p(i)) for all i
 	 * </pre>
 	 * 
 	 * I.e. for a 3D dataset (1,0,2) implies the new dataset has its 1st dimension running
 	 * along the old dataset's 2nd dimension and the new 2nd is the old 1st. The 3rd dimension
 	 * is left unchanged.
 	 * 
 	 * @param data
 	 * @param axisPermutation
 	 * @return remapped copy of data
 	 * @deprecated Use {@link DatasetUtils#transpose(AbstractDataset, int...)}
 	 */
 	@Deprecated
 	public static DataSet permuteAxes(DataSet data, int... axisPermutation) {
 		return convertToDataSet(DatasetUtils.transpose(data, axisPermutation));
 	}
 
 	// Jython methods
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#add(AbstractDataset, AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __add__(DataSet other) {
 		return convertToDataSet(Maths.add(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#add(AbstractDataset, Object)}
 	 */
 	@Deprecated
 	public DataSet __add__(double other) {
 		return convertToDataSet(Maths.add(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#add(AbstractDataset, Object)}
 	 */
 	@Deprecated
 	public DataSet __radd__(double other) {
 		return convertToDataSet(Maths.add(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return sum
 	 * @deprecated Use {@link #iadd(Object)}
 	 */
 	@Deprecated
 	public DataSet __iadd__(Object other) {
 		return (DataSet) iadd(other);
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#subtract(AbstractDataset, AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __sub__(DataSet other) {
 		return convertToDataSet(Maths.subtract(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#subtract(Object, Object)}
 	 */
 	@Deprecated
 	public DataSet __sub__(double other) {
 		return convertToDataSet(Maths.subtract(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#subtract(Object, Object)}
 	 */
 	@Deprecated
 	public DataSet __rsub__(double other) {
 		return convertToDataSet(Maths.subtract(other, this));
 	}
 
 	/**
 	 * @param other
 	 * @return subtraction
 	 * @deprecated Use {@link #isubtract(Object)}
 	 */
 	@Deprecated
 	public DataSet __isub__(Object other) {
 		return (DataSet) isubtract(other);
 	}
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#multiply(AbstractDataset, AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __mul__(DataSet other) {
 		return convertToDataSet(Maths.multiply(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#multiply(AbstractDataset, Object)}
 	 */
 	@Deprecated
 	public DataSet __mul__(double other) {
 		return convertToDataSet(Maths.multiply(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#multiply(AbstractDataset, Object)}
 	 */
 	@Deprecated
 	public DataSet __rmul__(double other) {
 		return convertToDataSet(Maths.multiply(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return product
 	 * @deprecated Use {@link #imultiply(Object)}
 	 */
 	@Deprecated
 	public DataSet __imul__(Object other) {
 		return (DataSet) imultiply(other);
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#divide(AbstractDataset, AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __div__(DataSet other) {
 		return convertToDataSet(Maths.divide(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#divide(Object, Object)}
 	 */
 	@Deprecated
 	public DataSet __div__(double other) {
 		return convertToDataSet(Maths.divide(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#divide(Object, Object)}
 	 */
 	@Deprecated
 	public DataSet __rdiv__(double other) {
 		return convertToDataSet(Maths.divide(other, this));
 	}
 
 	/**
 	 * @param other
 	 * @return dividend
 	 * @deprecated Use {@link #idivide(Object)}
 	 */
 	@Deprecated
 	public DataSet __idiv__(Object other) {
 		return (DataSet) idivide(other);
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#power(AbstractDataset, AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __pow__(DataSet other) {
 		return convertToDataSet(Maths.power(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return DataSet
 	 * @deprecated Use {@link Maths#power(Object, Object)}
 	 */
 	@Deprecated
 	public DataSet __pow__(double other) {
 		return convertToDataSet(Maths.power(this, other));
 	}
 
 	/**
 	 * @param other
 	 * @return product
 	 * @deprecated Use {@link #ipower(Object)}
 	 */
 	@Deprecated
 	public DataSet __ipow__(Object other) {
 		return (DataSet) ipower(other);
 	}
 
 	/**
 	 * overriding the unary "-" operator <br>
 	 * This returns a new dataset with values of opposite sign
 	 *
 	 * @return negative dataset
 	 * @deprecated Use {@link Maths#negative(AbstractDataset)}
 	 */
 	@Deprecated
 	public DataSet __neg__() {
 		return convertToDataSet(Maths.negative(this));
 	}
 
 	// Logical Operators
 
 	/**
 	 * overriding the "==" operator.<br>
 	 * <br>
 	 * This returns true if the magnitudes of the 2 vectors are identical <br>
 	 * <br>
 	 * <i><b>Warning!! as this is using floating point accuracy, two processed <br>
 	 * vectors are very unlikely to be equal, so be careful using this!</b></i><br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '==' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __eq__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 == V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 
 		return out2;
 	}
 
 	/**
 	 * overriding the "!=" operator.<br>
 	 * <br>
 	 * This returns false if the magnitudes of the 2 vectors are identical <br>
 	 * <br>
 	 * <i><b>Warning!! as this is using floating point accuracy, two processed <br>
 	 * vectors are very unlikely to be equal, so be careful using this!</b></i><br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '!=' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __ne__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 != V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 		return out2;
 	}
 
 	/**
 	 * overriding the ">" operator.<br>
 	 * <br>
 	 * This returns true if the first vectors is greater than the magnitude of the second vector<br>
 	 * <br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '>' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __gt__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 > V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 		return out2;
 	}
 
 	/**
 	 * overriding the "<" operator.<br>
 	 * <br>
 	 * This returns false if the first vectors is greater than the magnitude of the second vector<br>
 	 * <br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '<' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __lt__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 < V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 		return out2;
 	}
 
 	/**
 	 * overriding the ">=" operator.<br>
 	 * <br>
 	 * This returns true if the first vector is greater than or equal to the magnitude of the second vector<br>
 	 * <br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '>=' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __ge__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 >= V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 		return out2;
 	}
 
 	/**
 	 * overriding the "<=" operator.<br>
 	 * <br>
 	 * This returns true if the first vectors is less than or equal to the magnitude of the second vector<br>
 	 * <br>
 	 * 
 	 * @param other
 	 *            The DataVector1D value after the '<=' sign
 	 * @return PyInteger, containing 1 for true, and 0 for false.
 	 */
 	public Object __le__(DataSet other) {
 		double V1 = 0.0;
 		double V2 = 0.0;
 
 		for (int i = 0; i < data.length; i++) {
 			V1 = V1 + this.data[i] * this.data[i];
 			V2 = V2 + other.data[i] * other.data[i];
 		}
 
 		if (V1 <= V2) {
 			PyInteger output = new PyInteger(1);
 			return output;
 		}
 		PyInteger out2 = new PyInteger(0);
 		return out2;
 	}
 
 	/**
 	 * Jython overloaded function to allow for data to be obtained as a jython container
 	 * 
 	 * @param value
 	 *            The number of the point to be interrogated
 	 * @return the object containing true
 	 */
 	@SuppressWarnings("unused")
 	@Deprecated
 	public Object __contains__(Integer value) {
 		return true;
 	}
 
 	/**
 	 * Jython overloaded function to allow for data to be obtained as a jython container
 	 * 
 	 * @param index
 	 *            The number of the point to be interrogated
 	 * @return the object which is the result
 	 */
 	public Object __getitem__(Integer index) {
 
 		if (index < -shape[0] || index >= shape[0]) {
 			logger.error("The value {} is not within the dataset's bounds", index);
 			throw new PyException(Py.IndexError);
 		}
 		if (index < 0)
 			index += shape[0];
 
 		// first check the dimensionality
 		if (shape.length == 1) {
 			return data[index];
 		}
 		// otherwise slice
 		Object[] indexes = {index};
 		return __getitem__(indexes);
 	}
 
 	/**
 	 * @param index array
 	 * @return Dataset of specifies item
 	 */
 	public Object __getitem__(Integer[] index) {
 		int[] start = new int[shape.length];
 		// first check the dimensionality
 		int vlen;
 		if (index.length > shape.length) {
 			vlen = shape.length;
 		} else if (index.length == shape.length) {
 			vlen = index.length;
 		} else {
 			// incomplete indexes implies slice
 			return __getitem__((Object[]) index);
 		}
 		int i;
 		for (i = 0; i < vlen; i++) {
 			int d = index[i];
 			if (d < -shape[i] || d >= shape[i]) {
 				logger.error("The value {} is not within the dataset's bounds", index);
 				throw new PyException(Py.IndexError);
 			}
 			if (d < 0)
 				d += shape[i];
 			start[i] = d;
 		}
 		for (; i < shape.length; i++) {
 			start[i] = 0;
 		}
 		return get(start);
 	}
 
 	/**
 	 * @param slice
 	 * @return Dataset of specified slice
 	 */
 	public DataSet __getitem__(PySlice slice) {
 		int start, stop, step;
 
 		// step
 		if (slice.step instanceof PyNone) {
 			step = 1;
 		} else {
 			step = ((PyInteger) slice.step).getValue();
 		}
 
 		// start
 		if (slice.start instanceof PyNone) {
 			start = step > 0 ? 0 : shape[0] - 1;
 		} else {
 			start = ((PyInteger) slice.start).getValue();
 		}
 
 		// stop
 		if (slice.stop instanceof PyNone) {
 			stop = step > 0 ? shape[0] : -1;
 		} else {
 			stop = ((PyInteger) slice.stop).getValue();
 		}
 
 		return getSlice(start, stop, step);
 	}
 
 	/**
 	 * @param indexes can be a mixed array of integers or slices
 	 * @return Dataset of specified sub-dataset
 	 */
 	public DataSet __getitem__(Object[] indexes) {
 		int[] start, stop, step;
 		int slen;
 		int orank = shape.length;
 
 		// first check the dimensionality
 		if (indexes.length > orank) {
 			slen = orank;
 		} else {
 			slen = indexes.length;
 		}
 		start = new int[orank];
 		stop = new int[orank];
 		step = new int[orank];
 		boolean[] rdim = new boolean[orank];
 		int rank = 0;
 
 		int i;
 		for (i = 0; i < slen; i++) {
 			if (indexes[i] instanceof Integer) {
 				// nb specifying indexes whilst using slices will reduce rank
 				rdim[i] = true;
 				start[i] = (Integer) indexes[i];
 				if (start[i] < -shape[i] || start[i] >= shape[i]) {
 					logger.error("The value {} is not within the dataset's bounds", start);
 					throw new PyException(Py.IndexError);
 				}
 				if (start[i] < 0)
 					start[i] += shape[i];
 
 				stop[i] = start[i] + 1;
 				step[i] = 1;
 			} else if (indexes[i] instanceof PySlice) {
 				rdim[i] = false;
 				rank++;
 				PySlice slice = (PySlice) indexes[i];
 				// start
 				if (slice.start instanceof PyNone) {
 					start[i] = 0;
 				} else {
 					start[i] = ((PyInteger) slice.start).getValue();
 				}
 
 				// stop
 				if (slice.stop instanceof PyNone) {
 					stop[i] = shape[i];
 				} else {
 					stop[i] = ((PyInteger) slice.stop).getValue();
 				}
 
 				// step
 				if (slice.step instanceof PyNone) {
 					step[i] = 1;
 				} else {
 					step[i] = ((PyInteger) slice.step).getValue();
 				}
 			}
 		}
 		for (; i < orank; i++) {
 			rdim[i] = false;
 			rank++;
 			start[i] = 0;
 			stop[i] = shape[i];
 			step[i] = 1;
 		}
 		DataSet dataSlice = getSlice(start, stop, step);
 		if (rank < orank) {
 			int[] oldShape = dataSlice.getDimensions();
 			int[] newShape = new int[rank];
 			int j = 0;
 			for (i = 0; i < orank; i++) {
 				if (!rdim[i]) {
 					newShape[j++] = oldShape[i];
 				}
 			}
 			dataSlice.setShape(newShape);
 		}
 		return dataSlice;
 	}
 
 	/**
 	 * Not implemented, as you can't remove an element from this type of class
 	 * 
 	 * @param index
 	 * @return null;
 	 */
 	@SuppressWarnings("unused")
 	public Object __delitem__(Integer index) {
 		return null;
 	}
 
 	/**
 	 * @param index
 	 * @param newValue
 	 */
 	public void __setitem__(Integer index, Double newValue) {
 		if (shape.length > 1) {
 			logger.error("Cannot set an implicit slice to a single value");
 			throw new PyException(Py.NotImplementedError);
 		}
 		if (index < -size || index >= size) {
 			logger.error("The value {} is not within the dataset's bounds", index);
 			throw new PyException(Py.IndexError);
 		}
 		if (index < 0)
 			index += size;
 
 		data[index] = newValue;
 		setDirty();
 	}
 
 	/**
 	 * @param index array
 	 * @param newValue
 	 */
 	public void __setitem__(Integer[] index, Double newValue) {
 		int rank = shape.length;
 		int[] start = new int[rank];
 		// first check the dimensionality
 		int vlen;
 		if (index.length > rank) {
 			vlen = rank;
 		} else {
 			vlen = index.length;
 		}
 		int i;
 		for (i = 0; i < vlen; i++) {
 			start[i] = index[i];
 			if (start[i] < -shape[i] || start[i] >= shape[i]) {
 				logger.error("The value {} is not within the dataset's bounds", start);
 				throw new PyException(Py.IndexError);
 			}
 			if (start[i] < 0)
 				start[i] += shape[i];
 		}
 		for (; i < rank; i++) {
 			start[i] = 0;
 		}
 
 		set(newValue, start);
 	}
 
 	/**
 	 * @param slice
 	 * @param newValue
 	 */
 	public void __setitem__(PySlice slice, Double newValue) {
 		int start, stop, step;
 
 		// start
 		if (slice.start instanceof PyNone) {
 			start = 0;
 		} else {
 			start = ((PyInteger) slice.start).getValue();
 		}
 
 		// stop
 		if (slice.stop instanceof PyNone) {
 			stop = shape[0];
 		} else {
 			stop = ((PyInteger) slice.stop).getValue();
 		}
 
 		// step
 		if (slice.step instanceof PyNone) {
 			step = 1;
 		} else {
 			step = ((PyInteger) slice.step).getValue();
 		}
 
 		setSlice(start, stop, step, newValue);
 	}
 
 	/**
 	 * @param slice
 	 * @param newValues
 	 */
 	public void __setitem__(PySlice slice, DataSet newValues) {
 		int start, stop, step;
 
 		// start
 		if (slice.start instanceof PyNone) {
 			start = 0;
 		} else {
 			start = ((PyInteger) slice.start).getValue();
 		}
 
 		// stop
 		if (slice.stop instanceof PyNone) {
 			stop = shape[0];
 		} else {
 			stop = ((PyInteger) slice.stop).getValue();
 		}
 
 		// step
 		if (slice.step instanceof PyNone) {
 			step = 1;
 		} else {
 			step = ((PyInteger) slice.step).getValue();
 		}
 
 		setSlice(start, stop, step, newValues);
 	}
 
 	/**
 	 * @param slice
 	 * @param newValue
 	 */
 	public void __setitem__(PySlice[] slice, Double newValue) {
 		int[] start, stop, step;
 		int slen;
 		int rank = shape.length;
 
 		// first check the dimensionality
 		if (slice.length > rank) {
 			slen = rank;
 		} else {
 			slen = slice.length;
 		}
 		start = new int[rank];
 		stop = new int[rank];
 		step = new int[rank];
 		int i;
 		for (i = 0; i < slen; i++) {
 			// start
 			if (slice[i].start instanceof PyNone) {
 				start[i] = 0;
 			} else {
 				start[i] = ((PyInteger) slice[i].start).getValue();
 			}
 
 			// stop
 			if (slice[i].stop instanceof PyNone) {
 				stop[i] = shape[i];
 			} else {
 				stop[i] = ((PyInteger) slice[i].stop).getValue();
 			}
 
 			// step
 			if (slice[i].step instanceof PyNone) {
 				step[i] = 1;
 			} else {
 				step[i] = ((PyInteger) slice[i].step).getValue();
 			}
 		}
 		for (; i < rank; i++) {
 			start[i] = 0;
 			stop[i] = shape[i];
 			step[i] = 1;
 		}
 		setSlice(newValue, start, stop, step);
 	}
 
 	/**
 	 * @param slice
 	 * @param newValues
 	 */
 	public void __setitem__(PySlice[] slice, DataSet newValues) {
 		int[] start, stop, step;
 		int slen;
 		int rank = shape.length;
 
 		// first check the dimensionality
 		if (slice.length > rank) {
 			slen = rank;
 		} else {
 			slen = slice.length;
 		}
 		start = new int[rank];
 		stop = new int[rank];
 		step = new int[rank];
 		int i;
 		for (i = 0; i < slen; i++) {
 			// start
 			if (slice[i].start instanceof PyNone) {
 				start[i] = 0;
 			} else {
 				start[i] = ((PyInteger) slice[i].start).getValue();
 			}
 
 			// stop
 			if (slice[i].stop instanceof PyNone) {
 				stop[i] = shape[i];
 			} else {
 				stop[i] = ((PyInteger) slice[i].stop).getValue();
 			}
 
 			// step
 			if (slice[i].step instanceof PyNone) {
 				step[i] = 1;
 			} else {
 				step[i] = ((PyInteger) slice[i].step).getValue();
 			}
 		}
 		for (; i < rank; i++) {
 			start[i] = 0;
 			stop[i] = shape[i];
 			step[i] = 1;
 		}
 		setSlice(newValues, start, stop, step);
 	}
 
 	/**
 	 * Gets the number of objects in the class
 	 * 
 	 * @return An object integer containing the number of elements.
 	 */
 	@Deprecated
 	public Object __len__() {
 		return getSize();
 	}
 
 	/**
 	 * Jython iterator for dataset
 	 */
 	public class DatasetIterator {
 
 		private DataSet dataset;
 		private IndexIterator iterator;
 
 		public DatasetIterator(DataSet d) {
 			dataset = d;
 			iterator = d.getIterator();
 		}
 
 		public double __next__() {
 			return next();
 		}
 
 		public double next() {
 			if (iterator.hasNext())
 				return dataset.getAbs(iterator.index);
 			throw new PyException(Py.StopIteration);
 		}
 	}
 
 	/**
 	 * Gets a Jython iterator object
 	 * @return iterator
 	 */
 	@Deprecated
 	public DatasetIterator __iter__() {
 		return new DatasetIterator(this);
 	}
 
 	// override methods that create new datasets to return DataSet
 	@Override
 	public DataSet getView() {
 		return convertToDataSet(super.getView());
 	}
 
 	@Override
 	@Deprecated
 	public DataSet append(AbstractDataset other, int axis) {
 		return convertToDataSet(DatasetUtils.append(this, other, axis));
 	}
 
 	@Override
 	public DataSet fill(Object v) {
 		return convertToDataSet(super.fill(v));
 	}
 
 	@Override
 	public DataSet flatten() {
 		return convertToDataSet(super.flatten());
 	}
 
 	@Override
 	public DataSet getByBoolean(BooleanDataset selection) {
 		return convertToDataSet(super.getByBoolean(selection));
 	}
 
 	@Override
 	public DataSet getByIndex(IntegerDataset index) {
 		return convertToDataSet(super.getByIndex(index));
 	}
 
 	@Override
 	public Double max() {
 		return (Double) super.max();
 	}
 
 	@Override
 	public Double min() {
 		return (Double) super.min();
 	}
 
 	@Override
 	public Double mean() {
 		return (Double) super.mean();
 	}
 
 	@Override
 	public DataSet max(int axis) {
 		return convertToDataSet(super.max(axis));
 	}
 
 	@Override
 	public DataSet mean(int axis) {
 		return convertToDataSet(super.mean(axis));
 	}
 
 	@Override
 	public DataSet min(int axis) {
 		return convertToDataSet(super.min(axis));
 	}
 
 	@Override
 	public DataSet peakToPeak(int axis) {
 		return convertToDataSet(super.peakToPeak(axis));
 	}
 
 	@Override
 	public DataSet product(int axis) {
 		return convertToDataSet(super.product(axis));
 	}
 
 	@Override
 	public DataSet rootMeanSquare(int axis) {
 		return convertToDataSet(super.rootMeanSquare(axis));
 	}
 
 	@Override
 	public DataSet sort(Integer axis) {
 		return convertToDataSet(super.sort(axis));
 	}
 
 	@Override
 	public DataSet stdDeviation(int axis) {
 		return convertToDataSet(super.stdDeviation(axis));
 	}
 
 	@Override
 	public Double sum() {
 		return (Double) super.sum();
 	}
 
 	@Override
 	public DataSet sum(int axis) {
 		return convertToDataSet(super.sum(axis));
 	}
 
 	@Override
 	public DataSet swapaxes(int axis1, int axis2) {
 		return convertToDataSet(super.swapaxes(axis1, axis2));
 	}
 
 	@Override
 	public DataSet take(int[] indices, Integer axis) {
 		return convertToDataSet(super.take(indices, axis));
 	}
 
 	@Override
 	public DataSet transpose(int... axes) {
 		return convertToDataSet(super.transpose(axes));
 	}
 
 	@Override
 	public DataSet variance(int axis) {
 		return convertToDataSet(super.variance(axis));
 	}
 }
