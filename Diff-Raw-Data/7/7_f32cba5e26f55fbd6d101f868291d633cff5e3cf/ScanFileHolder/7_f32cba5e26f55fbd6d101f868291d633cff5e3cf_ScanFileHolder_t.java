 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
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
 
 import gda.analysis.io.IFileLoader;
 import gda.analysis.io.IFileSaver;
 import gda.analysis.io.ScanFileHolderException;
 import gda.analysis.utils.DatasetMaths;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 
 import org.python.core.PyException;
 import org.python.core.PyInteger;
 import org.python.core.PyList;
 import org.python.core.PyNone;
 import org.python.core.PySlice;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.PilatusTiffLoader;
 import uk.ac.diamond.scisoft.analysis.io.SRSLoader;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Class that contains all the info from a single data file. It can also contain a list of names datasets.
  *
  * @deprecated Use {@link LoaderFactory} and {@link DataHolder}
  */
 @Deprecated
 public class ScanFileHolder implements Serializable, IScanFileHolder {
 	transient public static int warnEverySoMany = 1000;
 	transient public static int warningNo = 0;
 	private static void issueDeprecatedWarning() {
 		if (warningNo++ % warnEverySoMany == 0) {
 			Throwable e = new Throwable();
 			StackTraceElement[] trace = e.getStackTrace();
 			StackTraceElement[] newtrace = new StackTraceElement[trace.length - 1];
 			for (int i = 1; i < trace.length; i++) {
 				newtrace[i-1] = trace[i];
 			}
 			e.setStackTrace(newtrace);
 			logger.info("this method is deprecated - use an AbstractDataset", e);
 		}
 	}
 	
 	/**
 	 * Setup the logging facilities
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(ScanFileHolder.class);
 
 	/**
 	 * container for all the lines of the data file
 	 */
 	protected DataHolder holder;
 
 	/**
 	 * Container for an image which is associated with the file.
 	 */
 	DataSet image;
 
 	/**
 	 * Constructor
 	 */
 	public ScanFileHolder() {
 		holder = new DataHolder();
 	}
 
 	@Override
 	public void load(IFileLoader loader) throws ScanFileHolderException {
 		load(loader, null);
 	}
 
 	@Override
 	public void load(IFileLoader loader, IMonitor mon) throws ScanFileHolderException {
 		holder = loader.loadFile(mon);
 		// This has been removed but it may cause issues?
 		//System.gc();
 	}
 
 	@Override
 	public IMetaData getMetadata() {
 		return holder.getMetadata();
 	}
 
 	/**
 	 * Lowest level save routine
 	 * 
 	 * @param saver
 	 *            An object which implements the IFileSaver interface this specifically designed to save in the
 	 *            appropriate file type
 	 */
 	@Override
 	public void save(IFileSaver saver) throws ScanFileHolderException {
 		saver.saveFile(holder);
 	}
 
 	@Override
 	@Deprecated
 	public void setPilatusConversionLocation(String fileName) throws ScanFileHolderException {
 		throw new ScanFileHolderException("No longer required or supported");
 	}
 
 	@Override
 	@Deprecated
 	public String getPilatusConversionLocation() throws ScanFileHolderException {
 		throw new ScanFileHolderException("No longer required or supported");
 	}
 
 	@Override
 	@Deprecated
 	public void loadPilatusData(String fileName) throws ScanFileHolderException {
 		issueDeprecatedWarning();
 		load(new PilatusTiffLoader(fileName));
 		image = DataSet.convertToDataSet(holder.getDataset(0));
 	}
 
 	@Override
 	public void loadSRS(String fileName) throws ScanFileHolderException {
 		// now simply load in using the new system
 		load(new SRSLoader(fileName));
 	}
 
 	@Override
 	public void info() {
 
 		String out;
 
 		if (holder.size() > 0) {
 			Integer outVal = holder.getDataset(0).getSize();
 			out = "ScanFileHolder object containing " + outVal.toString() + " lines of data per DataSet.";
 		} else {
 			out = "An empty ScanFileHolder";
 		}
 
 		TerminalPrinter.print(out);
 
 		this.ls();
 	}
 
 	@Override
 	@Deprecated
 	public double getPixel(int xCoordinate, int yCoordinate) {
 		issueDeprecatedWarning();
 		return image.getDouble(yCoordinate, xCoordinate); // row-major ordering
 	}
 
 	@Override
 	public void setAxis(String axisName, IDataset inData) throws ScanFileHolderException {
 		try {
 			if (holder.contains(axisName)) {
 				if (inData instanceof AbstractDataset)
					holder.addDataset(axisName, inData);
 				else
					holder.addDataset(axisName, inData);
 				return;
 			}
 
 			logger.info("Axis does not exist, create new one ");
 			this.addDataSet(axisName, inData);
 
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem adding data", e);
 		}
 
 	}
 
 	@Override
 	public DataSet getAxis(String axisName) throws IllegalArgumentException {
 		AbstractDataset a = holder.getDataset(axisName);
 		if (a != null) {
 			DataSet data = DataSet.convertToDataSet(a).clone();
 			if (data != null)
 				return data;
 		}
 
 		String msg = "Axis name " + axisName + " not recognised. Available axes: " + Arrays.toString(getHeadings());
 		logger.error(msg);
 		throw new IllegalArgumentException(msg);
 
 	}
 
 	@Override
 	public AbstractDataset getAxis(int axisNumber) throws IllegalArgumentException {
 		try {
 			return holder.getDataset(axisNumber).clone();
 		} catch (ArrayIndexOutOfBoundsException e) {
 			logger.error("{} is not a valid position in ScanFileHolder", axisNumber);
 			throw new IllegalArgumentException(axisNumber + " is not a valid position in the ScanFileHolder");
 		}
 	}
 
 	@Override
 	public List<Double> getInterpolatedX(String XAxis, String YAxis, double yPosition) {
 		return DatasetUtils.crossings(holder.getDataset(XAxis), holder.getDataset(YAxis), yPosition);
 	}
 
 	@Override
 	public List<Double> getInterpolatedX(String XAxis, String YAxis, double yPosition, double VarianceProportion) {
 		return DatasetUtils.crossings(holder.getDataset(XAxis), holder.getDataset(YAxis), yPosition, VarianceProportion);
 	}
 
 	@Deprecated
 	@Override
 	public List<Double> getInterpolatedX(AbstractDataset XAxis, AbstractDataset YAxis, double yPosition) {
 		return DatasetUtils.crossings(XAxis, YAxis, yPosition);
 	}
 
 	@Deprecated
 	@Override
 	public List<Double> getInterpolatedX(AbstractDataset XAxis, AbstractDataset YAxis, double yPosition, double VarianceProportion) {
 		return DatasetUtils.crossings(XAxis, YAxis, yPosition, VarianceProportion);
 	}
 
 	@Override
 	public AbstractDataset getDataSet(String deviceName) {
 		return getAxis(deviceName);
 	}
 
 	@Override
 	public IDataset getDataset(String deviceName) {
 		return getAxis(deviceName);
 	}
 
 	@Override
 	public void setDataSet(String name, IDataset inData) throws ScanFileHolderException {
 		setAxis(name, inData);
 	}
 
 	@Override
 	public void setDataset(String name, IDataset inData) throws ScanFileHolderException {
 		setAxis(name, inData);
 	}
 
 	@Override
 	public void addDataSet(String name, IDataset inData) throws ScanFileHolderException {
 		try {
 			holder.addDataset(name, inData);
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem adding data", e);
 		}
 	}
 
 	@Override
 	public void addDataset(String name, IDataset inData) throws ScanFileHolderException {
 		try {
 			holder.addDataset(name, inData);
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem adding data", e);
 		}
 	}
 
 	@Override
 	public void ls() {
 		StringBuilder text = new StringBuilder();
 		for (int i = 0, imax = holder.namesSize(); i < imax; i++) {
 			text.append(i);
 			text.append('\t');
 			text.append(holder.getName(i));
 			text.append('\n');
 		}
 		TerminalPrinter.print(text.toString());
 	}
 
 	@Override
 	public double getMin(String Axis) {
 		return holder.getDataset(Axis).min().doubleValue();
 	}
 
 	@Override
 	public double getMax(String Axis) {
 		return holder.getDataset(Axis).max().doubleValue();
 	}
 
 	@Override
 	public int[] getMinPos(String Axis) {
 		return holder.getDataset(Axis).minPos();
 	}
 
 	@Override
 	public int[] getMaxPos(String Axis) {
 		return holder.getDataset(Axis).maxPos();
 	}
 
 	@Override
 	public double getMinPos(String XAxis, String YAxis) {
 		int[] Position = holder.getDataset(YAxis).minPos();
 		return holder.getDataset(XAxis).getDouble(Position);
 	}
 
 	@Override
 	public double getMaxPos(String XAxis, String YAxis) {
 		int[] Position = holder.getDataset(YAxis).maxPos();
 		return holder.getDataset(XAxis).getDouble(Position);
 	}
 
 	@Override
 	@Deprecated
 	public double centroid(DoubleDataset x, DoubleDataset y) {
 		issueDeprecatedWarning();
 		return DatasetMaths.centroid(y, x)[0];
 	}
 
 	@Override
 	public String[] getHeadings() {
 		return holder.getNames();
 	}
 
 	@Override
 	public int getNumberOfAxis() {
 		return holder.size();
 	}
 
 	@Override
 	public void clear() {
 		holder.clear();
 	}
 
 	// JYTHON OVERLOADED FUNCTIONALITY
 
 	/**
 	 * Jython overloaded function to allow for data to be obtained as a jython container
 	 * 
 	 * @param value
 	 *            The number of the point to be interrogated
 	 * @return the object containing true
 	 */
 	@SuppressWarnings("unused")
 	public Object __contains__(Integer value) {
 		return true;
 	}
 
 	/**
 	 * Jython overloaded function to allow for data to be obtained as a jython container
 	 * 
 	 * @param value
 	 *            The number of the point to be interrogated
 	 * @return the object which is the result
 	 */
 	public Object __getitem__(Integer value) {
 		if ((value < 0) || (value >= holder.size())) {
 			logger.error("The value {} is not within the ScanFileHolders bounds", value);
 			throw new PyException();
 		}
 		return DataSet.convertToDataSet(holder.getDataset(value));
 	}
 
 	/**
 	 * Python overloaded function to allow for the slicing of data from the ScanFileHolder
 	 * 
 	 * @param slice
 	 *            The PySlice which is to be interrogated
 	 * @return A python list of datasets, which was asked for.
 	 */
 	public Object __getitem__(PySlice slice) {
 		PyList out = new PyList();
 		int start, stop, step;
 
 		if (slice.step instanceof PyNone) {
 			step = 1;
 		} else {
 			step = ((PyInteger) slice.step).getValue();
 		}
 
 		if (slice.start instanceof PyNone) {
 			start = (step > 0) ? 0 : holder.size() - 1;
 		} else {
 			start = ((PyInteger) slice.start).getValue();
 		}
 
 		if (slice.stop instanceof PyNone) {
 			stop = (step > 0) ? holder.size() : -1;
 		} else {
 			stop = ((PyInteger) slice.stop).getValue();
 		}
 
 		int end;
 		if (step > 0) {
 			end = (stop - start + step - 1) / step;
 		} else {
 			end = (stop - start + step + 1) / step;
 		}
 
 		for (int i = 0; i < end; i++) {
 			out.add(holder.getDataset(i * step + start));
 		}
 		return out;
 	}
 
 	/**
 	 * Not implemented, as you cannot remove an element from this type of class
 	 * 
 	 * @param value
 	 * @return null;
 	 */
 	@SuppressWarnings("unused")
 	public Object __delitem__(Integer value) {
 		return null;
 	}
 
 	/**
 	 * Not implemented as this is a read only class
 	 * 
 	 * @param value
 	 * @param newValue
 	 */
 	@SuppressWarnings("unused")
 	public void __setitem__(Integer value, Double newValue) {
 
 	}
 
 	/**
 	 * Gets the number of objects in the class
 	 * 
 	 * @return An object integer containing the number of elements.
 	 */
 	public Object __len__() {
 		return holder.size();
 	}
 }
