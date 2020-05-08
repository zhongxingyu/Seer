 package org.dawnsci.conversion.converters;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.common.services.conversion.IConversionContext;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * AbstractConversion details converting from hdf/nexus to other
  * things only at the moment.
  * 
  * @author fcp94556
  *
  */
 public abstract class AbstractConversion {
 	
 	protected IConversionContext context;
 
 	public AbstractConversion(IConversionContext context) {
 		this.context = context;
 	}
 
 	public void process(IConversionContext context) throws Exception {
 		
 		// If they directly specify an ILazyDataset, loop it and only
 		// it directly. Ignore file paths.
 		if (context.getLazyDataset()!=null) {
 			final ILazyDataset lz = context.getLazyDataset();
 			if (lz!=null) iterate(lz, lz.getName(), context);
 
 		} else {
 			final List<String> filePaths = context.getFilePaths();
 			for (String filePathRegEx : filePaths) {
 				final List<File> paths = expand(filePathRegEx);
 				for (File path : paths) {
 					
 					context.setSelectedConversionFile(path);
 					if (path.isFile()) {
 						final List<String> sets  = getDataNames(path);
 						final List<String> names = context.getDatasetNames();
 						for (String nameRegExp : names) {
 							final List<String> data = getData(sets, nameRegExp);
 							if (data == null) continue;
 							for (String dsPath : data) {
 								final ILazyDataset lz = getLazyDataset(path, dsPath, context);
 								if (lz!=null) iterate(lz, dsPath, context);
 							}
 						}
 					} else { 
 						final ILazyDataset lz = getLazyDataset(path, null, context);
 						iterate(lz, path.getName(), context);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Override this method to provide things which should happen after the processing.
 	 * @param context
 	 */
 	public void close(IConversionContext context) throws Exception{
 		
 	}
 	
 	protected IConversionContext getContext() {
 		return context;
 	}
 
 	/**
 	 * Please implement this method to process a single conversion. The files will have been 
 	 * expanded, the datasets expanded, the slice done, you need to implement the writing of the
 	 * appropriate file for this slice.
 	 * 
 	 * @param slice
 	 * @param context, used to provide the output location mainly.
 	 */
 	protected abstract void convert(IDataset slice) throws Exception;
 	
 
 	/**
 	 * This method can be overridden for returning stacks of images from 
 	 * a directory for instance.
 	 * 
 	 * @param path
 	 * @param dsPath
 	 * @param sliceDimensions
 	 * @param context, might be null for testing
 	 * @return
 	 * @throws Exception
 	 */
 	protected ILazyDataset getLazyDataset(final File                 path, 
 						                  final String               dsPath,
 						                  final IConversionContext   context) throws Exception {
 		// if there is a lazydataset, we return it
 		ILazyDataset lazy = context.getLazyDataset();
 		if (lazy != null)
 			return context.getLazyDataset();
 
 		final IDataHolder   dh = LoaderFactory.getData(path.getAbsolutePath());
 		context.setSelectedH5Path(dsPath);
 		if (context.getSliceDimensions()==null) {
 			// Because the data might be lazy and unloadable. We want to load all the data now.
 			IDataset data = LoaderFactory.getDataSet(path.getAbsolutePath(), dsPath, null);
 			data.setName(dsPath);
 			convert(data);
 			return null;
 		}
 		if (context.getMonitor()!=null) {
 			context.getMonitor().subTask("Process '"+dsPath+"'");
 		}
 		return dh.getLazyDataset(dsPath);
 	}
 		
 	protected void iterate(final ILazyDataset         lz, 
 			               final String               nameFrag,
 		                   final IConversionContext   context) throws Exception {
 		
 		multiRangeIterate(lz,nameFrag,context);
 	}
 	
 	/**
 	 * Method tries to get the input datasets with the regular expressions, if any, expanded.
 	 * @return
 	 */
 	protected List<String> getExpandedDatasets() throws Exception {
 		
 		final List<String> names = new ArrayList<String>(31);
 		
 		final List<String> filePaths = context.getFilePaths();
 		for (String filePathRegEx : filePaths) {
 			final List<File> paths = expand(filePathRegEx);
 			for (File path : paths) {
 				
 				context.setSelectedConversionFile(path);
 				if (path.isFile()) {
 					final List<String> sets   = getDataNames(path);
 					final List<String> dNames = context.getDatasetNames();
 					for (String nameRegExp : dNames) {
 						final List<String> data = getData(sets, nameRegExp);
 					    names.addAll(data);
 					}
 				}
 			}
 		}
 		if (names.isEmpty()) return null;
 		return names;
 	}
 
 	private void multiRangeIterate(final ILazyDataset         lz, 
 			               final String               nameFrag,
 		                   final IConversionContext   context) throws Exception {
 		
 		final Map<Integer, String> sliceDimensions = context.getSliceDimensions();
 
 		final int[] fullDims = lz.getShape();
 		
 		//Construct Slice String
 		StringBuilder sb = new StringBuilder();
 		
 		for (int i = 0; i < fullDims.length; i++) {
 			if (sliceDimensions.containsKey(i)) {
 				String s = sliceDimensions.get(i);
 				if (s.contains("all")) s = ":";
 				sb.append(s);
 				sb.append(",");
 			} else {
 				sb.append(":");
 				sb.append(',');
 			}
 		}
 		
 		sb.deleteCharAt(sb.length()-1);
 		Slice[] slices = Slice.convertFromString(sb.toString());
 		
 		//TODO all this horrible-ness could probably be reduced by taking an initial slice
 		//view from the original lazy dataset
 		
 		//create array of ignored axes values
 		Set<Integer> axesSet = new HashSet<Integer>();
 		for (int i = 0; i < fullDims.length; i++) axesSet.add(i);
 		axesSet.removeAll(sliceDimensions.keySet());
 		int[] axes = new int[axesSet.size()];
 		int count = 0;
 		Iterator<Integer> iter = axesSet.iterator();
 		while (iter.hasNext()) axes[count++] = iter.next(); 
 
 		//determine shape of sliced dataset
 		int[] slicedShape = lz.getShape().clone();
 		for (int i = 0; i < slices.length; i++) {
 			if (slices[i].getStop() == null && slices[i].getLength() ==-1) continue;
 			slicedShape[i] = slices[i].getNumSteps();
 		}
 
 		PositionIterator pi = new PositionIterator(lz.getShape(), slices, axes);
 		int[] pos = pi.getPos();
 
 		while (pi.hasNext()) {
 
 			int[] end = pos.clone();
 			for (int i = 0; i<pos.length;i++) {
 				end[i]++;
 			}
 
 			for (int i = 0; i < axes.length; i++){
 				end[axes[i]] = fullDims[axes[i]];
 			}
 
 			int[] st = pos.clone();
 			for (int i = 0; i < st.length;i++) st[i] = 1;
 
 			Slice[] slice = Slice.convertToSlice(pos, end, st);
 			String sliceName = Slice.createString(slice);
 
 			Slice[] outSlice = new Slice[slices.length];
 			for (int i = 0; i < slices.length; i++) {
 				if (slice[i].getStop() == null && slice[i].getLength() ==-1) {
 					outSlice[i] = new Slice();
 				} else {
 					int nSteps = slice[i].getNumSteps();
 					int offset = (slice[i].getStart()-slices[i].getStart())/slices[i].getStep();
 					outSlice[i] = new Slice(offset,offset+nSteps);
 				}
 			}
 
 			AbstractDataset data = (AbstractDataset)lz.getSlice(slice);
 			data = data.squeeze();
 			data.setName(nameFrag + " ("+ sliceName+")");
 			context.setSelectedSlice(outSlice);
 			context.setSelectedShape(slicedShape);
 			convert(data);
 			if (context.getMonitor() != null) {
 				IMonitor mon = context.getMonitor();
 				if (mon.isCancelled()) return;
 			}
 		}
 	}
 	
 	/**
 	 * Used when dims are not the same as the entire set, for instance when doing a slice.
 	 * @param val
 	 * @param longShape
 	 * @param set
 	 * @return
 	 * @throws Exception
 	 */
 	public static AbstractDataset getSet(final Object  val, final long[] longShape, final Dataset set) throws Exception {
 
 		final int[] intShape  = getInt(longShape);
          
 		AbstractDataset ret = null;
         if (val instanceof byte[]) {
         	ret = new ByteDataset((byte[])val, intShape);
         } else if (val instanceof short[]) {
         	ret = new ShortDataset((short[])val, intShape);
         } else if (val instanceof int[]) {
         	ret = new IntegerDataset((int[])val, intShape);
         } else if (val instanceof long[]) {
         	ret = new LongDataset((long[])val, intShape);
         } else if (val instanceof float[]) {
         	ret = new FloatDataset((float[])val, intShape);
         } else if (val instanceof double[]) {
         	ret = new DoubleDataset((double[])val, intShape);
         } else {
         	throw new Exception("Cannot deal with data type "+set.getDatatype().getDatatypeDescription());
         }
         
 		if (set.getDatatype().isUnsigned()) {
 			switch (ret.getDtype()) {
 			case AbstractDataset.INT32:
 				ret = new LongDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 32);
 				break;
 			case AbstractDataset.INT16:
 				ret = new IntegerDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 16);
 				break;
 			case AbstractDataset.INT8:
 				ret = new ShortDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 8);
 				break;
 			}
 		}
 
         return ret;
        
 	}
 
 	/**
 	 * Get a int[] from a long[]
 	 * @param longShape
 	 * @return
 	 */
 	public static int[] getInt(long[] longShape) {
 		final int[] intShape  = new int[longShape.length];
 		for (int i = 0; i < intShape.length; i++) intShape[i] = (int)longShape[i];
 		return intShape;
 	}
 
 	public List<String> getData(File path, String datasetName) throws Exception {
         return getData(getDataNames(path), datasetName);
 	}
 	
 	private List<String> getData(List<String> sets, String datasetName) {
 
 		final List<String> ds = new ArrayList<String>(7);
 		for (String hdfPath : sets) {
 			if (hdfPath.matches(datasetName)) {
 				ds.add(hdfPath);
 			}
 		}
 
 		return ds.isEmpty() ? null : ds;
 	}
 	
 
 	/**
 	 * Can be used to get a list of Dataset which should be converted. Processes the
 	 * regexp for the dataset path and returns the Dataset which can be sliced to get
 	 * the array of numbers for the export.
 	 * 
 	 * @param ioFile
 	 * @param context
 	 * @return null if none match, the datasets otherwise
 	 * @throws Exception
 	 */
 	
 	public List<String> getDataNames(File ioFile) throws Exception {
 
 		if (ioFile.isDirectory()) return Collections.emptyList();
 		final IDataHolder   dh    = LoaderFactory.getData(ioFile.getAbsolutePath());
 		
 		if (dh == null || dh.getNames() == null) return Collections.emptyList();
 		return Arrays.asList(dh.getNames());
 	}
 
 	/**
 	 * expand the regex according to the javadoc for getFilePath().
 	 * @param context
 	 * @return
 	 */
 	public List<File> expand(String path) {
 		
 		final List<File> files = new ArrayList<File>(7);
 		path = path.replace('\\', '/');
 		final String dir    = path.substring(0, path.lastIndexOf("/"));
 		final String regexp = path.substring(path.lastIndexOf("/")+1);
 		
 		final File[] fa = new File(dir).listFiles();
 		for (File file : fa) {
 			if (regexp==null || "".equals(regexp)) {
 				files.add(file);
 				continue;
 			}
 			if (file.getName().matches(regexp)) files.add(file);
 		}
 		
 		return files.isEmpty() ? null : files;
 	}
 	
 	
 	protected String getFileNameNoExtension(File file) {
 		final String fileName = file.getName();
 		int posExt = fileName.lastIndexOf(".");
 		// No File Extension
 		return posExt == -1 ? fileName : fileName.substring(0, posExt);
 	}
 
 	
 	/**
 	 * Determines the HDF5 Datatype for an abstract dataset.
 	 * @param a
 	 * @return data type
 	 */
 	public static Datatype getDatatype(IDataset a) throws Exception {
 		
 		// There is a smarter way of doing this, but am in a hurry...
 		if (a instanceof ByteDataset || a instanceof BooleanDataset) {
          	return new H5Datatype(Datatype.CLASS_INTEGER, 8/8, Datatype.NATIVE, Datatype.SIGN_NONE);
          	
         } else if (a instanceof ShortDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 16/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof IntegerDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
        	
         } else if (a instanceof LongDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof FloatDataset) {
         	return new H5Datatype(Datatype.CLASS_FLOAT, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof DoubleDataset) {
         	return new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
       	    
         } else if (a instanceof StringDataset) {
         	return new H5Datatype(Datatype.CLASS_CHAR, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
         }
         
         throw new Exception("Cannot deal with data type "+a.getClass().getName());
 	}
 	
 	public static long[] getLong(int[] intShape) {
 		final long[] longShape  = new long[intShape.length];
 		for (int i = 0; i < intShape.length; i++) longShape[i] = intShape[i];
 		return longShape;
 	}
 
 }
