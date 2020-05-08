 package org.dawnsci.conversion;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionVisitor;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 class ConversionContext implements IConversionContext {
 
 	private ConversionScheme    conversionScheme;
 	private IConversionVisitor  conversionVisitor;
 	private List<String>        filePaths;
 	private ILazyDataset        lazyDataset;
 	private List<String>        datasetNames;
 	private String              outputFolder;
 	private Map<Integer,String> sliceDimensions;
 	private Object              userObject;
 	private IMonitor            monitor;
 	private File                selectedConversionFile;
 	private String              selectedH5Path;
 	private String              axisDatasetName;
 	private int                 workSize=100;
 	private boolean             expression=false;
 	private Slice[]             selectedSlice;
 	private int[]               selectedShape;
 	
 	public ConversionScheme getConversionScheme() {
 		return conversionScheme;
 	}
 	public void setConversionScheme(ConversionScheme conversionScheme) {
 		this.conversionScheme = conversionScheme;
 	}
 	public List<String> getFilePaths() {
 		return filePaths;
 	}
 	public void setFilePaths(String... paths) throws Exception {
 		this.filePaths = new ArrayList<String>(paths.length);
 		
 		for (int i = 0; i < paths.length; i++) {
 			// In order to parse the regex, it must have / not \
 			if (paths[i] == null)
				throw new Exception("A folder, file or list of files compatible with the conversion tool needs to be selected.");
 			String path = paths[i].replace('\\', '/');
 			if (path.startsWith("file:/")) {
 				path = path.substring("file:/".length());
 			}
 			filePaths.add(path);
 		}
 	}
 	public List<String> getDatasetNames() {
 		return datasetNames;
 	}
 	public void setDatasetName(String datasetName) {
 		this.datasetNames = Arrays.asList(datasetName);
 	}
 	public void setDatasetNames(List<String> datasetNames) {
 		this.datasetNames = datasetNames;
 	}
 	public String getOutputPath() {
 		return outputFolder;
 	}
 	public void setOutputPath(String outputFolder) {
 		this.outputFolder = outputFolder;
 	}
 
 	
 	/**
 	 * 
 	 * @param dim
 	 * @param sliceString either an integer to hold the dimension constant or
 	 * a range of the form "start:end" where start is the start index and end is
 	 * the end index or the string "all" to use the size of the dataset (start=0,
 	 * end-length dimension).
 	 */
 	public void addSliceDimension(int dim, String sliceString) {
 		if (sliceDimensions == null) sliceDimensions = new HashMap<Integer,String>(7);
 		sliceDimensions.put(dim, sliceString);
 	}
 	/**
 	 * 
 	 * @return the dimensions to slice in, may be null, in which case no slice done.
 	 */
 	public Map<Integer, String> getSliceDimensions() {
 		return sliceDimensions;
 	}
 	public Object getUserObject() {
 		return userObject;
 	}
 	public void setUserObject(Object userObject) {
 		this.userObject = userObject;
 	}
 	@Override
 	public IMonitor getMonitor() {
 		return monitor;
 	}
 	@Override
 	public void setMonitor(IMonitor monitor) {
 		this.monitor = monitor;
 	}
 	public IConversionVisitor getConversionVisitor() {
 		return conversionVisitor;
 	}
 	public void setConversionVisitor(IConversionVisitor conversionVisitor) {
 		this.conversionVisitor = conversionVisitor;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((axisDatasetName == null) ? 0 : axisDatasetName.hashCode());
 		result = prime
 				* result
 				+ ((conversionScheme == null) ? 0 : conversionScheme.hashCode());
 		result = prime * result
 				+ ((datasetNames == null) ? 0 : datasetNames.hashCode());
 		result = prime * result + (expression ? 1231 : 1237);
 		result = prime * result
 				+ ((filePaths == null) ? 0 : filePaths.hashCode());
 		result = prime * result
 				+ ((lazyDataset == null) ? 0 : lazyDataset.hashCode());
 		result = prime * result
 				+ ((outputFolder == null) ? 0 : outputFolder.hashCode());
 		result = prime
 				* result
 				+ ((selectedConversionFile == null) ? 0
 						: selectedConversionFile.hashCode());
 		result = prime * result
 				+ ((sliceDimensions == null) ? 0 : sliceDimensions.hashCode());
 		result = prime * result
 				+ ((userObject == null) ? 0 : userObject.hashCode());
 		result = prime * result + workSize;
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ConversionContext other = (ConversionContext) obj;
 		if (axisDatasetName == null) {
 			if (other.axisDatasetName != null)
 				return false;
 		} else if (!axisDatasetName.equals(other.axisDatasetName))
 			return false;
 		if (conversionScheme != other.conversionScheme)
 			return false;
 		if (datasetNames == null) {
 			if (other.datasetNames != null)
 				return false;
 		} else if (!datasetNames.equals(other.datasetNames))
 			return false;
 		if (expression != other.expression)
 			return false;
 		if (filePaths == null) {
 			if (other.filePaths != null)
 				return false;
 		} else if (!filePaths.equals(other.filePaths))
 			return false;
 		if (lazyDataset == null) {
 			if (other.lazyDataset != null)
 				return false;
 		} else if (!lazyDataset.equals(other.lazyDataset))
 			return false;
 		if (outputFolder == null) {
 			if (other.outputFolder != null)
 				return false;
 		} else if (!outputFolder.equals(other.outputFolder))
 			return false;
 		if (selectedConversionFile == null) {
 			if (other.selectedConversionFile != null)
 				return false;
 		} else if (!selectedConversionFile.equals(other.selectedConversionFile))
 			return false;
 		if (sliceDimensions == null) {
 			if (other.sliceDimensions != null)
 				return false;
 		} else if (!sliceDimensions.equals(other.sliceDimensions))
 			return false;
 		if (userObject == null) {
 			if (other.userObject != null)
 				return false;
 		} else if (!userObject.equals(other.userObject))
 			return false;
 		if (workSize != other.workSize)
 			return false;
 		return true;
 	}
 	public ILazyDataset getLazyDataset() {
 		return lazyDataset;
 	}
 	public void setLazyDataset(ILazyDataset lazyDataset) {
 		this.lazyDataset = lazyDataset;
 	}
 	public File getSelectedConversionFile() {
 		return selectedConversionFile;
 	}
 	public void setSelectedConversionFile(File selectedConversionFile) {
 		this.selectedConversionFile = selectedConversionFile;
 	}
 	@Override
 	public String getAxisDatasetName() {
 		return axisDatasetName;
 	}
 	@Override
 	public void setAxisDatasetName(String axisDatasetName) {
 		this.axisDatasetName = axisDatasetName;
 		
 	}
 	public int getWorkSize() {
 		return workSize;
 	}
 	public void setWorkSize(int workSize) {
 		this.workSize = workSize;
 	}
 	public boolean isExpression() {
 		return expression;
 	}
 	public void setExpression(boolean expression) {
 		this.expression = expression;
 	}
 	public String getSelectedH5Path() {
 		return selectedH5Path;
 	}
 	public void setSelectedH5Path(String selectedH5Path) {
 		this.selectedH5Path = selectedH5Path;
 	}
 	@Override
 	public Slice[] getSelectedSlice() {
 		return selectedSlice;
 	}
 	@Override
 	public void setSelectedSlice(Slice[] slice) {
 		selectedSlice = slice;
 	}
 	@Override
 	public int[] getSelectedShape() {
 		return selectedShape;
 	}
 	@Override
 	public void setSelectedShape(int[] shape) {
 		this.selectedShape = shape;
 	}
 
 }
