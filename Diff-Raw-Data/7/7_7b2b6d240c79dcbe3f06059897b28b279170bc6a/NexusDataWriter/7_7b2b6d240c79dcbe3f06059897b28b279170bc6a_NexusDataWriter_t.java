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
 
 package gda.data.scan.datawriter;
 
 import gda.configuration.properties.LocalProperties;
 import gda.data.NumTracker;
 import gda.data.PathConstructor;
 import gda.data.metadata.GDAMetadataProvider;
 import gda.data.metadata.IMetadataEntry;
 import gda.data.metadata.Metadata;
 import gda.data.nexus.INeXusInfoWriteable;
 import gda.data.nexus.NeXusUtils;
 import gda.data.nexus.NexusFileFactory;
 import gda.data.nexus.NexusGlobals;
 import gda.data.nexus.extractor.NexusExtractor;
 import gda.data.nexus.extractor.NexusGroupData;
 import gda.data.nexus.tree.INexusTree;
 import gda.data.nexus.tree.NexusTreeAppender;
 import gda.data.nexus.tree.NexusTreeNode;
 import gda.data.nexus.tree.NexusTreeProvider;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.detector.NexusDetector;
 import gda.device.scannable.ScannableUtils;
 import gda.factory.Finder;
 import gda.jython.InterfaceProvider;
 import gda.scan.IScanDataPoint;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 
 import org.nexusformat.NeXusFileInterface;
 import org.nexusformat.NexusException;
 import org.nexusformat.NexusFile;
 import org.python.core.PyList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 /**
  * DataWriter that outputs NeXus files and optionally a SRS/Text file as well.
  */
 public class NexusDataWriter extends DataWriterBase implements DataWriter {
 	private static final Logger logger = LoggerFactory.getLogger(NexusDataWriter.class);
 
 	/**
 	 * Property to control the level of instrumentation of the nexus api
 	 */
 	public static final String GDA_NEXUS_INSTRUMENT_API = "gda.nexus.instrumentApi";
 
 	/**
 	 * Property to control the file format. Defaults to {@link NexusGlobals#GDA_NX_DEFAULT}
 	 */
 	public static final String GDA_DATA_NEXUS_BACKEND = "gda.data.nexus.backend";
 	
 	public static final String GDA_NEXUS_METADATAPROVIDER_NAME = "gda.nexus.metadata.provider.name";
 
 	/**
 	 * Property specifying whether SRS data files should be written in addition to NeXus files. Default is {@code true}.
 	 */
 	public static final String GDA_NEXUS_CREATE_SRS = "gda.nexus.createSRS";
 
 	/**
 	 * Boolean property specifying whether nxs/dat filenames should be prefixed with the beamline name; if {@code true},
 	 * files will be named (e.g.) {@code "i23-999.nxs"} instead of just {@code "999.nxs"}
 	 */
 	public static final String GDA_NEXUS_BEAMLINE_PREFIX = "gda.nexus.beamlinePrefix";
 
 	static final int MAX_DATAFILENAME = 255;
 
 	/** Default NeXus format */
 	protected String defaultNeXusBackend = null;
 
 	/** Are we going to write an SRS file as well ? */
 	private boolean createSrsFile = false;
 
 	// beamline name
 	protected String beamline = null;
 
 	// Directory to write data to
 	protected String dataDir = null;
 
 	// file name with no extension
 	protected String fileBaseName = null;
 	protected String fileBaseUrl = null;
 
 	// file names
 	private String nexusFileNameTemplate = null;
 	private String txtFileNameTemplate = null;
 	protected String nexusFileName = null;
 	protected String txtFileName = null;
 
 	// Fully qualified filenames
 	protected String nexusFileUrl = null;
 	protected String txtFileUrl = null;
 
 	// Relative filenames
 	protected String nexusRelativeUrl = null;
 	protected String txtRelativeUrl = null;
 
 	// NeXus entry name
 	protected String entryName = "entry1";
 
 	/** File Handle for NeXus file */
 	protected NeXusFileInterface file;
 	/** File Handle for text file */
 	protected FileWriter txtfile;
 
 	/**
 	 * The current run number.
 	 */
 	protected Long scanNumber;
 
 	Long getScanNumber() throws Exception {
 		configureScanNumber(null); // ensure it has been configured
 		return scanNumber;
 	}
 
 	protected Collection<SelfCreatingLink> scannableID;
 
 	boolean firstData = true;
 
 	protected int scanPointNumber = -1;
 
 	IScanDataPoint thisPoint;
 
 	protected Metadata metadata = null;
 
 	private boolean setupPropertiesDone = false;
 
 	private boolean fileNumberConfigured = false;
 
 	/**
 	 * Constructor. This attempts to read the java.property which defines the beamline name.
 	 */
 	public NexusDataWriter() {
 		super();
 	}
 
 	public NexusDataWriter(Long fileNumber) {
 		scanNumber = fileNumber;
 	}
 
 	protected void setupProperties() throws InstantiationException {
 		if (setupPropertiesDone)
 			return;
 		metadata = GDAMetadataProvider.getInstance();
 
 		try {
 			beamline = metadata.getMetadataValue("instrument", "gda.instrument", null);
 		} catch (DeviceException e1) {
 		}
 
 		// If the beamline name isn't set then default to 'base'.
 		if (beamline == null) {
 			// If the beamline name is not set then use 'base'
 			beamline = "base";
 		}
 
 		// Check to see if we want to use a different NeXus backend format.
 		defaultNeXusBackend = LocalProperties.get(GDA_DATA_NEXUS_BACKEND);
 		if (defaultNeXusBackend == null) {
 			defaultNeXusBackend = NexusGlobals.GDA_NX_DEFAULT;
 		}
 
 		// Check to see if the data directory has been defined.
 		dataDir = PathConstructor.createFromDefaultProperty();
 		if (dataDir == null) {
 			// this java property is compulsory - stop the scan
 			throw new InstantiationException("cannot work out data directory - cannot create a new data file.");
 		}
 
 		// Check to see if we want to create a text/SRS file as well.
 		createSrsFile = LocalProperties.check(GDA_NEXUS_CREATE_SRS, true);
 
 		if( beforeScanMetaData== null ){
 			String metaDataProviderName = LocalProperties.get(GDA_NEXUS_METADATAPROVIDER_NAME);
 			if( StringUtils.hasLength(metaDataProviderName)){
 				NexusTreeAppender metaDataProvider = Finder.getInstance().find(metaDataProviderName);
 				InterfaceProvider.getTerminalPrinter().print("Getting meta data before scan");
 				beforeScanMetaData = new NexusTreeNode("before_scan", NexusExtractor.NXCollectionClassName, null);
 				metaDataProvider.appendToTopNode(beforeScanMetaData);
 			}
 		}
 		if( beforeScanMetaData == null){
 			InterfaceProvider.getTerminalPrinter().print("Meta data before_scan is not being added");
 			beforeScanMetaData = new NexusTreeNode("before_scan", NexusExtractor.NXCollectionClassName, null);
 			beforeScanMetaData.addChildNode(new NexusTreeNode("disabled", NexusExtractor.AttrClassName, beforeScanMetaData,
 					new NexusGroupData("True")));
 
 		}
 		setupPropertiesDone = true;
 
 	}
 
 	public INexusTree getBeforeScanMetaData() {
 		return beforeScanMetaData;
 	}
 
 	public void setBeforeScanMetaData(INexusTree beforeScanMetaData) {
 		this.beforeScanMetaData = beforeScanMetaData;
 	}
 
 	@Override
 	public void configureScanNumber(Long _scanNumber) throws Exception {
 		if (!fileNumberConfigured) {
 			if (_scanNumber != null) {
 				// the scan or other datawriter has set the id
 				scanNumber = _scanNumber;
 			} else {
 				if (scanNumber == null) {
 					setupProperties();
 					// not set in a constructor so get from num tracker
 					try {
 						NumTracker runNumber = new NumTracker(beamline);
 						// Get the next run number
 						scanNumber = runNumber.incrementNumber();
 					} catch (IOException e) {
 						logger.error("ERROR: Could not instantiate NumTracker in NexusDataWriter().", e);
 						throw new InstantiationException(
 								"ERROR: Could not instantiate NumTracker in NexusDataWriter()." + e.getMessage());
 					}
 				}
 			}
 			fileNumberConfigured = true;
 		}
 	}
 
 	protected static int[] generateStartPosPrefix(int currentPoint, int[] scanDimensions) {
 		if (scanDimensions.length == 1) {
 			return new int[] { currentPoint };
 		}
 		int[] scanNumbers = new int[scanDimensions.length];
 		int[] totals = new int[scanDimensions.length];
 		totals[scanDimensions.length - 1] = 1;
 		for (int i = scanDimensions.length - 2; i > -1; i--) {
 			totals[i] = scanDimensions[i + 1] * totals[i + 1];
 		}
 		if (currentPoint > totals[0] * scanDimensions[0]) {
 			throw new IllegalArgumentException("currentPoint is larger than expected from reported scan dimensions");
 		}
 
 		int remainder = currentPoint;
 		for (int j = 0; j < scanDimensions.length - 1; j++) {
 			scanNumbers[j] = remainder / totals[j];
 			remainder = remainder - scanNumbers[j] * totals[j];
 		}
 		scanNumbers[scanDimensions.length - 1] = remainder;
 		return scanNumbers;
 	}
 
 	protected static int[] generateDataStartPos(int[] dataStartPosPrefix, int[] dataDimensions) {
 		int[] dataStartPos = null;
 		if (dataStartPosPrefix != null) {
 			dataStartPos = Arrays.copyOf(dataStartPosPrefix, dataStartPosPrefix.length
 					+ (dataDimensions != null ? dataDimensions.length : 0));
 		} else if (dataDimensions != null) {
 			dataStartPos = new int[dataDimensions.length];
 		}
 		return dataStartPos;
 	}
 
 	/**
 	 * calculate dimensionality of data to be written
 	 * 
 	 * @param make
 	 *            if true calculate for pre-allocation (first Dim NX_UNLIMITED)
 	 * @param dataDimPrefix
 	 *            set to null if not point dependent
 	 * @param dataDimensions
 	 * @return dimensions
 	 */
 	protected static int[] generateDataDim(boolean make, int[] dataDimPrefix, int[] dataDimensions) {
 		int[] dataDim = null;
 		if (dataDimPrefix != null) {
 			// do not attempt to add dataDimensions if not set or indicates single point
 			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length > 1 || dataDimensions[0] > 1) ? dataDimensions.length : 0;
 
 			if (make) {
 				dataDim = new int[dataDimPrefix.length + dataDimensionToAdd];
 				Arrays.fill(dataDim, NexusFile.NX_UNLIMITED);
 			} else {
 				dataDim = Arrays.copyOf(dataDimPrefix, dataDimPrefix.length + dataDimensionToAdd);
 			}
 			if (dataDimensionToAdd > 0 && dataDimensions != null) {
 				for (int i = dataDimPrefix.length; i < dataDimPrefix.length + dataDimensionToAdd; i++) {
 					dataDim[i] = dataDimensions[i - dataDimPrefix.length];
 				}
 			}
 		} else if (dataDimensions != null) {
 			dataDim = Arrays.copyOf(dataDimensions, dataDimensions.length);
 		}

 		return dataDim;
 	}
 
 	/*
 	 * The dimensions of the scan {@link Scan#getDimensions()}
 	 */
 	int[] scanDimensions;
 	/*
 	 * Fields present for convenience The location within data at which data is to be written - not taking into account
 	 * the dimensions of the data itself The length of this array should match the length of scanDimensions and the
 	 * values are calculated from the getCurrentPointNumber() method of the ScanDataPoint. For a 2d scan of 4 x 3 e.g.
 	 * scan simpleScannable1, 0., 3, 1., simpleScannable2, 0., 2, 1. the values of scanPointNumbers will be [0,0],
 	 * [0,1], [0,2], [1,0], [1,1], [1,2], [2,0], [2,1], [2,2], [3,0], [3,1], [3,2]
 	 */
 	int[] dataStartPosPrefix;
 	/*
 	 * Fields present for convenience The dimensions of the to be written - not taking into account the dimensions of
 	 * the data itself The length of this array should match the length of scanDimensions and the values are all 1 but
 	 * for the first which is NexusFile.NX_UNLIMITED;
 	 */
 	int[] dataDimPrefix;
 
 	private INexusTree beforeScanMetaData;
 
 	@Override
 	public void addData(IScanDataPoint dataPoint) throws Exception {
 		thisPoint = dataPoint;
 		scanPointNumber++;
 
 		// Some Debug messages
 		logger.debug("Adding SDP with UUID: {}", dataPoint.getUniqueName());
 		if (firstData) {
 			logger.debug("Scan Parameters...");
 			logger.debug("Scan Command : {}", dataPoint.getCommand());
 			logger.debug("Instrument Name : {}", dataPoint.getInstrument());
 			logger.debug("Number of Points : {}", dataPoint.getNumberOfPoints());
 		}
 		logger.debug("Current Point : {}", dataPoint.getCurrentPointNumber());
 
 		if (scanPointNumber != dataPoint.getCurrentPointNumber()) {
 			logger.warn("The DataWriter(" + scanPointNumber + ") and the DataPoint("
 					+ dataPoint.getCurrentPointNumber() + ") disagree about the point number!");
 		}
 		dataStartPosPrefix = generateStartPosPrefix(thisPoint.getCurrentPointNumber(), thisPoint.getScanDimensions());
 
 		try {
 			if (firstData) {
 				scanDimensions = dataPoint.getScanDimensions();
 				dataDimPrefix = new int[scanDimensions.length];
 				Arrays.fill(dataDimPrefix, 1);
 				this.prepareFileAndStructure();
 				firstData = false;
 			}
 		} finally {
 			firstData = false;
 		}
 		dataPoint.setCurrentFilename(getCurrentFileName());
 
 		try {
 			if (createSrsFile) {
 				txtfile.write(dataPoint.toFormattedString() + "\n");
 				txtfile.flush();
 			}
 
 			for (Scannable scannable : thisPoint.getScannables()) {
 				writeScannable(scannable);
 			}
 
 			for (Detector detector : thisPoint.getDetectors()) {
 				writeDetector(detector);
 			}
 
 			file.flush();
 
 		} catch (Exception ex) {
 			String error = "Exception occurred writing the nexus file. The nexus file is not being written correctly or has not been written.";
 			logger.error(error, ex);
 			terminalPrinter.print(error);
 			throw ex;
 		} finally {
 			// Even if there was an exception we call super
 			// that way the ascii file is still written.
 			try {
 				super.addData(this, dataPoint);
 			} catch (Exception e) {
 				logger.error("exception received from DataWriterBase.addData(...)", e);
 			}
 		}
 	}
 
 	private void writeGenericDetector(String detectorName, int[] dataDimensions, Object newData) throws NexusException {
 
 		// Navigate to correct location in the file.
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 		file.opengroup(detectorName, "NXdetector");
 
 		if (newData instanceof INexusTree) {
 			INexusTree detectorData = (INexusTree) newData;
 			for (INexusTree subTree : detectorData) {
 				writeHere(file, subTree, false, false, null);
 			}
 		} else {
 
 			int[] startPos = generateDataStartPos(dataStartPosPrefix, dataDimensions);
 			int[] dimArray = generateDataDim(false, dataDimPrefix, dataDimensions);
 
 			// Open data array.
 			file.opendata("data");
 
 			file.putslab(newData, startPos, dimArray);
 
 			// Close data
 			file.closedata();
 
 		}
 		// Close NXdetector
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 		// Close NXentry
 		file.closegroup();
 	}
 
 	private void writeDetector(Detector detector) throws DeviceException, NexusException {
 		if (detector.createsOwnFiles()) {
 			writeFileCreatorDetector(detector.getName(), extractFileName(detector.getName()),
 					detector.getDataDimensions());
 		} else {
 			if (detector instanceof NexusDetector) {
 				writeNexusDetector((NexusDetector) detector);
 				// } else if (detector instanceof CounterTimer) {
 			} else if (detector.getExtraNames() != null && detector.getExtraNames().length > 0) {
 				double[] data = extractDoubleData(detector.getName());
 				if (data != null) {
 					writeCounterTimer(detector, data);
 				} else {
 					writeCounterTimer(detector);
 				}
 			} else {
 				writeGenericDetector(detector.getName(), detector.getDataDimensions(),
 						extractDoubleData(detector.getName()));
 			}
 		}
 	}
 
 	private static int getIntfromBuffer(Object buf) {
 		if (buf instanceof Object[])
 			buf = ((Object[]) buf)[0];
 		if (buf instanceof Number)
 			return ((Number) buf).intValue();
 		return Integer.parseInt(buf.toString());
 	}
 
 	void writeHere(NeXusFileInterface file, INexusTree tree, boolean makeData, boolean attrOnly,
 			List<SelfCreatingLink> links) throws NexusException {
 		if (!tree.isPointDependent() && !makeData) {
 			return;
 		}
 		String name = tree.getName();
 		String nxClass = tree.getNxClass();
 		Boolean dataOpen = false;
 		Boolean loopNodes = true;
 		Boolean attrBelowThisOnly = attrOnly;
 		Boolean nxClassIsSDS = nxClass.equals(NexusExtractor.SDSClassName);
 		Boolean nxClassIsAttr = nxClass.equals(NexusExtractor.AttrClassName);
 		Boolean nxClassIsExternalSDS = nxClass.equals(NexusExtractor.ExternalSDSLink);
 		if (nxClassIsExternalSDS) {
 			if (makeData) {
 				NexusGroupData data = tree.getData();
 				try {
 					String filePath = new String((byte[]) data.getBuffer(), "UTF-8");
 					File f = new File(filePath);
 					if (!f.exists())
 						logger.warn("file " + filePath + " does not exist at time of adding link");
 					file.linkexternaldataset(name, filePath);
 					links.add(new ExteneralNXlink(name, filePath));
 				} catch (UnsupportedEncodingException e) {
 					throw new NexusException(
 							"supported encoding in creating string for external linking -- this should never happen");
 				}
 			}
 			return;
 		}
 		if (nxClassIsAttr) {
 			if (makeData) {
 				NexusGroupData data = tree.getData();
 				if (data != null && data.getBuffer() != null) {
 					if ("axis".equals(name) || "label".equals(name)) {
 						Integer axisno = getIntfromBuffer(data.getBuffer());
 						axisno += thisPoint.getScanDimensions().length;
 						file.putattr(name, axisno.toString().getBytes(), NexusFile.NX_CHAR);
 					} else {
 						file.putattr(name, data.getBuffer(), data.type);
 					}
 				}
 			}
 			return;
 		}
 		if (attrOnly) {
 			return;
 		}
 		if (!name.isEmpty() && !nxClass.isEmpty()) {
 			if (!nxClassIsSDS) {
 				if (!(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))) {
 					file.makegroup(name, nxClass);
 				}
 				file.opengroup(name, nxClass);
 			}
 
 			NexusGroupData sds = tree.getData();
 			if (sds != null) {
 				if (sds.dimensions != null) {
 					for (int i : sds.dimensions) {
 						if (i == 0)
 							throw new NexusException("Data for " + name + " is invalid. SDS Dimension = 0");
 					}
 				}
 				if (makeData) {
 					int[] dataDimMake = generateDataDim(tree.isPointDependent(),
 							tree.isPointDependent() ? scanDimensions : null, sds.dimensions);
 
 					// TODO allow NGD to specify compression and chunks
					if (sds.dimensions != null && sds.dimensions.length > 1) {
 						int[] chunks = Arrays.copyOf(dataDimMake, dataDimMake.length);
 						for (int i = 0; i < chunks.length; i++) {
 							if (chunks[i] == -1) chunks[i] = 1;
 						}
 						file.compmakedata(name, sds.type, dataDimMake.length, dataDimMake, NexusFile.NX_COMP_LZW_LVL1, chunks);
 					} else {
 						file.makedata(name, sds.type, dataDimMake.length, dataDimMake);
 					}
 					
 					file.opendata(name);
 					if (!tree.isPointDependent()) {
 						int[] dataDim = generateDataDim(false, null, sds.dimensions);
 						int[] dataStartPos = generateDataStartPos(null, sds.dimensions);
 						file.putslab(sds.getBuffer(), dataStartPos, dataDim);
 					}
 					if (links != null && sds.isDetectorEntryData) {
 						links.add(new SelfCreatingLink(file.getdataID()));
 					}
 
 					dataOpen = true;
 					attrBelowThisOnly = true;
 				} else {
 					int[] dataDim = generateDataDim(false, dataDimPrefix, sds.dimensions);
 					int[] dataStartPos = generateDataStartPos(dataStartPosPrefix, sds.dimensions);
 
 					// Open data array.
 					file.opendata(name);
 
 					file.putslab(sds.getBuffer(), dataStartPos, dataDim);
 					dataOpen = true;
 
 					// Close data - do not add children as attributes added for first point only
 					loopNodes = false;
 
 				}
 			}
 		} else {
 			logger.warn("Name or class is empty:");
 		}
 		try {
 			if (loopNodes) {
 				for (INexusTree branch : tree) {
 					writeHere(file, branch, makeData, attrBelowThisOnly, links);
 				}
 			}
 		} finally {
 			if (dataOpen) {
 				file.closedata();
 			}
 			if (!name.isEmpty() && !nxClass.isEmpty() && !nxClassIsSDS) {
 				file.closegroup();
 			}
 		}
 	}
 
 	private void writeNexusDetector(NexusDetector detector) throws NexusException {
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 		try {
 			INexusTree tree = extractNexusTree(detector.getName());
 			for (INexusTree subTree : tree) {
 				if (subTree.getNxClass().equals(NexusExtractor.NXDetectorClassName))
 					writeHere(file, subTree, false, false, null);
 				else if (subTree.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
 					file.closegroup();
 					writeHere(file, subTree, false, false, null);
 					file.opengroup("instrument", "NXinstrument");
 				}
 			}
 		} finally {
 			file.closegroup();
 			file.closegroup();
 		}
 	}
 
 	private Object extractDetectorObject(String detectorName) {
 		int index = thisPoint.getDetectorNames().indexOf(detectorName);
 		Object object = thisPoint.getDetectorData().get(index);
 		return object;
 	}
 
 	private double[] extractDoubleData(String detectorName) {
 		double[] data = null;
 		Object object = extractDetectorObject(detectorName);
 		data = extractDoubleData(detectorName, object);
 		return data;
 	}
 
 	private String extractFileName(String detectorName) {
 		return (String) extractDetectorObject(detectorName);
 	}
 
 	private INexusTree extractNexusTree(String detectorName) {
 		return ((NexusTreeProvider) extractDetectorObject(detectorName)).getNexusTree();
 	}
 
 	/**
 	 * @param detectorName
 	 * @param object
 	 * @return the data read from the detector
 	 * @throws NumberFormatException
 	 */
 	private double[] extractDoubleData(String detectorName, Object object) throws NumberFormatException {
 		double[] data = null;
 		if (object instanceof double[]) {
 			data = (double[]) object;
 		} else if (object instanceof PyList) {
 			// coerce PyList into double array.
 			int length = ((PyList) object).__len__();
 			data = new double[length];
 			for (int i = 0; i < length; i++) {
 				data[i] = Double.valueOf(((PyList) object).__getitem__(i).toString());
 			}
 		} else if (object instanceof int[]) {
 			int[] idata = (int[]) object;
 			data = new double[idata.length];
 			for (int i = 0; i < data.length; i++) {
 				data[i] = idata[i];
 			}
 		} else if (object instanceof long[]) {
 			long[] ldata = (long[]) object;
 			data = new double[ldata.length];
 			for (int i = 0; i < data.length; i++) {
 				data[i] = ldata[i];
 			}
 		} else if (object instanceof String[]) {
 			String[] sdata = (String[]) object;
 			data = new double[sdata.length];
 			for (int i = 0; i < data.length; i++) {
 				data[i] = Double.valueOf(sdata[i]);
 			}
 		} else if (object instanceof Number[]) {
 			Number[] ldata = (Number[]) object;
 			data = new double[ldata.length];
 			for (int i = 0; i < data.length; i++) {
 				data[i] = ldata[i].doubleValue();
 			}
 		} else if (object instanceof Double) {
 			data = new double[] { (Double) object };
 		} else if (object instanceof Integer) {
 			data = new double[] { (Integer) object };
 		} else if (object instanceof Long) {
 			data = new double[] { (Long) object };
 		} else {
 			logger.error("cannot handle data of type " + object.getClass().getName() + " from detector: "
 					+ detectorName + ". NO DATA WILL BE WRITTEN TO NEXUS FILE!");
 		}
 		return data;
 	}
 
 	private Double[] extractDoublePositions(String scannableName) {
 		int index = thisPoint.getScannableNames().indexOf(scannableName);
 
 		if (index > -1) {
 			Object position = thisPoint.getPositions().get(index);
 			if (position != null) {
 				return ScannableUtils.objectToArray(position);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Perform any tasks that should be done at the end of a scan and close the file.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public void completeCollection() throws Exception {
 		releaseFile();
 		super.completeCollection();
 	}
 
 	/**
 	 * Releases the file handle.
 	 */
 	public void releaseFile() {
 		try {
 			if (file != null) {
 				file.flush();
 				file.finalize();
 			}
 			if (txtfile != null) {
 				txtfile.flush();
 				txtfile.close();
 			}
 		} catch (NexusException ne) {
 			String error = "NeXusException occurred when closing file: ";
 			logger.error(error + ne.getMessage());
 			terminalPrinter.print(error);
 			terminalPrinter.print(ne.getMessage());
 		} catch (Throwable et) {
 			String error = "Error occurred when closing data file(s): ";
 			logger.error(error + et.getMessage());
 			terminalPrinter.print(error);
 			terminalPrinter.print(et.getMessage());
 		} finally {
 			file = null;
 			txtfile = null;
 		}
 	}
 
 	@Override
 	public String getCurrentFileName() {
 		return nexusFileUrl;
 	}
 
 	public Object getData() {
 		return file;
 	}
 
 	private void prepareFileAndStructure() throws Exception {
 		setupProperties();
 		createNextFile();
 		makeMetadata();
 		makeScannablesAndMonitors();
 		makeDetectors();
 	}
 
 	private void makeMetadata() {
 		try {
 			file.opengroup(this.entryName, "NXentry");
 
 			NeXusUtils.writeNexusString(file, "scan_command", thisPoint.getCommand());
 			String scanid = "";
 			try {
 				scanid = metadata.getMetadataValue(GDAMetadataProvider.SCAN_IDENTIFIER);
 			} catch (DeviceException e) {
 				// do nothing
 			}
 			NeXusUtils.writeNexusString(file, "scan_identifier", scanid.isEmpty() ? thisPoint.getUniqueName() : scanid);
 			NeXusUtils.writeNexusIntegerArray(file, "scan_dimensions", thisPoint.getScanDimensions());
 			NeXusUtils.writeNexusString(file, "title", metadata.getMetadataValue("title"));
 			createCustomMetaData();
 		} catch (Exception e) {
 			logger.info("error writing less important scan information", e);
 		} finally {
 			try {
 				file.closegroup();
 			} catch (NexusException e) {
 				// this just needs to work
 			}
 		}
 	}
 
 	/**
 	 * Override to provide additional meta data, if required. Does nothing otherwise.
 	 * 
 	 * @throws NexusException
 	 */
 	// allow inheriting classes to throw this exception
 	protected void createCustomMetaData() throws NexusException {
 		
 		if( beforeScanMetaData != null ){
 			writeHere(file, beforeScanMetaData, true, false, null);
 		}
 	}
 
 	protected String getGroupClassFor(@SuppressWarnings("unused") Scannable s) {
 		String groupName = "NXpositioner";
 		return groupName;
 	}
 
 	/**
 	 * Create NXpositioner/NXmonitor class for each Scannable.
 	 */
 	private void makeScannablesAndMonitors() {
 		scannableID = new Vector<SelfCreatingLink>();
 		Collection<Scannable> scannablesAndMonitors = new Vector<Scannable>();
 		scannablesAndMonitors.addAll(thisPoint.getScannables());
 		makeScannablesAndMonitors(scannablesAndMonitors);
 	}
 	
 	protected void makeScannablesAndMonitors(Collection<Scannable> scannablesAndMonitors) {
 
 		String axislist = "1";
 		for (int j = 2; j <= thisPoint.getScanDimensions().length; j++) {
 			axislist = axislist + String.format(",%d", j);
 		}
 
 		try {
 			file.opengroup(this.entryName, "NXentry");
 			file.opengroup("instrument", "NXinstrument");
 
 			int[] dataDim = generateDataDim(true, scanDimensions, null);
 
 			int inputnameindex = 0;
 			int extranameindex = 0;
 			for (Scannable scannable : scannablesAndMonitors) {
 
 				String[] inputNames = scannable.getInputNames();
 				String[] extraNames = scannable.getExtraNames();
 
 				String groupName = getGroupClassFor(scannable);
 				file.makegroup(scannable.getName(), groupName);
 				file.opengroup(scannable.getName(), groupName);
 
 				// Check to see if the scannable will write its own info into NeXus
 				if (scannable instanceof INeXusInfoWriteable) {
 					((INeXusInfoWriteable) scannable).writeNeXusInformation(file);
 				}
 
 				// loop over input names...
 				for (String element : inputNames) {
 					// Create the data array (with an unlimited scan
 					// dimension)
 					file.makedata(element, NexusFile.NX_FLOAT64, dataDim.length, dataDim);
 
 					// Get a link ID to this data set.
 					file.opendata(element);
 
 					// assign axes
 					if (thisPoint.getScanDimensions().length > 0) {
 						// TODO
 						// in all likelihood this will not give the right axis assignment
 						// for scannables with multiple input names
 						// this is not solvable given the current data in SDP
 
 						if ((thisPoint.getScanDimensions().length) > inputnameindex) {
 							file.putattr("label", String.format("%d", inputnameindex + 1).getBytes(), NexusFile.NX_CHAR);
 							file.putattr("primary", "1".getBytes(), NexusFile.NX_CHAR);
 						}
 						file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
 					}
 
 					scannableID.add(new SelfCreatingLink(file.getdataID()));
 					file.closedata();
 					inputnameindex++;
 				}
 
 				for (String element : extraNames) {
 
 					// Create the data array (with an unlimited scan
 					// dimension)
 					file.makedata(element, NexusFile.NX_FLOAT64, dataDim.length, dataDim);
 
 					// Get a link ID to this data set.
 					file.opendata(element);
 
 					if (thisPoint.getDetectorNames().isEmpty() && extranameindex == 0) {
 						file.putattr("signal", "1".getBytes(), NexusFile.NX_CHAR);
 					}
 
 					scannableID.add(new SelfCreatingLink(file.getdataID()));
 					file.closedata();
 					extranameindex++;
 				}
 
 				// Close NXpositioner/NXmonitor
 				file.closegroup();
 			}
 
 			// Close NXinstrument...
 			file.closegroup();
 			// Close NXentry...
 			file.closegroup();
 		} catch (NexusException e) {
 			String error = "NeXus file creation failed during makeScannables: ";
 			logger.error(error + e.getMessage(), e);
 			terminalPrinter.print(error);
 			terminalPrinter.print(e.getMessage());
 		}
 	}
 
 	private void makeFallbackNXData() throws NexusException {
 		file.opengroup(this.entryName, "NXentry");
 		// Make and open NXdata
 		file.makegroup("default", "NXdata");
 		file.opengroup("default", "NXdata");
 
 		makeAxesLinks();
 
 		// close NXdata
 		file.closegroup();
 
 		// close NXentry
 		file.closegroup();
 	}
 
 	protected void makeAxesLinks() {
 		// Make links to all scannables.
 		for (SelfCreatingLink id : scannableID) {
 			try {
 				id.create(file);
 			} catch (NexusException e) {
 				logger.warn("Error in makeLink (reported to NX group) for " + id.toString() + "with error"
 						+ e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Create NXdetector class for each Detector.
 	 */
 	private void makeDetectors() {
 		try {
 			Vector<Detector> detectors = thisPoint.getDetectors();
 
 			if (detectors.size() == 0) {
 				makeFallbackNXData();
 				return;
 			}
 
 			// create an NXdetector for each detector...
 			for (Detector detector : detectors) {
 				try {
 					makeDetectorEntry(detector);
 				} catch(Exception e){
 					throw new DeviceException("Error making detector entry for detector " + detector.getName(),e);
 				}
 			}
 		} catch (NexusException e) {
 			String error = "NeXus file creation failed during makeDetectors: ";
 			logger.error(error + e.getMessage(), e);
 			terminalPrinter.print(error);
 			terminalPrinter.print(e.getMessage());
 		} catch (DeviceException de) {
 			String error = "DeviceException during NeXus file creation: ";
 			logger.error(error + de.getMessage(), de);
 			terminalPrinter.print(error);
 			terminalPrinter.print(de.getMessage());
 		}
 	}
 
 	/**
 	 * @param detector
 	 * @throws DeviceException
 	 * @throws NexusException
 	 */
 	private void makeDetectorEntry(Detector detector) throws DeviceException, NexusException {
 		logger.debug("Making NXdetector for " + detector.getName() + " in NeXus file.");
 
 		if (detector instanceof NexusDetector) {
 			logger.debug("Creating NexusTree entry in NeXus file.");
 			INexusTree detTree = extractNexusTree(detector.getName());
 			for (INexusTree det : detTree) {
 				if (det.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
 					makeGenericDetector(det.getName(), null, 0, detector, det);
 				} else if (det.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
 					// FIXME -- if this doesn't explode I am truly surprised
 					file.opengroup(this.entryName, NexusExtractor.NXEntryClassName);
 					try {
 						writeHere(file, det, firstData, false, null);
 					} finally {
 						file.closegroup();
 					}
 				}
 			}
 		} else if (detector.createsOwnFiles()) {
 			logger.debug("Creating File Creator Detector entry in NeXus file.");
 			makeFileCreatorDetector(detector.getName(), detector.getDataDimensions(), detector);
 		} else if (detector.getExtraNames().length > 0) {
 			logger.debug("Creating Detector entry in NeXus file.");
 			makeCounterTimer(detector);
 		} else {
 			logger.debug("Creating Generic Detector entry in NeXus file.");
 			makeGenericDetector(detector.getName(), detector.getDataDimensions(), NexusFile.NX_FLOAT64, detector, null);
 		}
 	}
 
 	/**
 	 * Helper routine to create and write string based data items into the current position in a NeXus file.
 	 * 
 	 * @param dataName
 	 * @param dataValue
 	 * @throws NexusException
 	 */
 	private void makeCreateStringData(String dataName, String dataValue) throws NexusException {
 		int[] arr = { dataValue.length() };
 		file.makedata(dataName, NexusFile.NX_CHAR, 1, arr);
 		file.opendata(dataName);
 		file.putdata(dataValue.getBytes());
 		file.closedata();
 	}
 
 	private void writeFileCreatorDetector(String detectorName, String dataFileName,
 			@SuppressWarnings("unused") int[] detectorDataDimensions) throws NexusException {
 
 		if (dataFileName.length() > 255) {
 			logger.error("The detector (" + detectorName + ") returned a file name (of length " + dataFileName.length()
 					+ ") which is greater than the max allowed length (" + MAX_DATAFILENAME + ").");
 		}
 
 		// Navigate to correct location in the file.
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 		file.opengroup(detectorName, "NXdetector");
 
 		file.opengroup("data_file", "NXnote");
 
 		// Open filename array.
 		file.opendata("file_name");
 
 		logger.debug("Filename received from detector: " + dataFileName);
 
 		// Now lets construct the relative (to the nexus data file) path to the file.
 		if (dataFileName.startsWith(dataDir)) {
 			dataFileName = dataFileName.substring(dataDir.length());
 			// Check for a leading '/'
 			if (dataFileName.startsWith("/") == false) {
 				dataFileName = "/" + dataFileName;
 			}
 			// Make the path relative.
 			dataFileName = "." + dataFileName;
 		}
 
 		// Set all the start positions to be zero (except for the first
 		// dimension which is the scan point)
 		int[] dataDimensions = new int[] { MAX_DATAFILENAME };
 		int[] dataDim = generateDataDim(false, dataDimPrefix, dataDimensions);
 		int[] dataStartPos = generateDataStartPos(dataStartPosPrefix, dataDimensions);
 
 		byte filenameBytes[] = new byte[MAX_DATAFILENAME];
 		java.util.Arrays.fill(filenameBytes, (byte) 0); // zero terminate
 
 		for (int k = 0; k < dataFileName.length(); k++) {
 			filenameBytes[k] = (byte) dataFileName.charAt(k);
 		}
 		file.putslab(filenameBytes, dataStartPos, dataDim);
 		file.putattr("data_filename", new int[] { 1 }, NexusFile.NX_INT32);
 		// Close filename array.
 		file.closedata();
 
 		// Close the data_file NXnote
 		file.closegroup();
 
 		// Close NXdetector
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 		// Close NXentry
 		file.closegroup();
 	}
 
 	private void makeFileCreatorDetector(String detectorName, @SuppressWarnings("unused") int[] dataDimensions,
 			Object detector) throws NexusException {
 		// Navigate to the relevant section in file...
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 
 		// Create NXdetector
 		file.makegroup(detectorName, "NXdetector");
 		file.opengroup(detectorName, "NXdetector");
 
 		// Metadata items
 		makeCreateStringData("description", "Generic GDA Detector - External Files");
 		makeCreateStringData("type", "Detector");
 
 		// Check to see if the detector will write its own info into NeXus
 		if (detector instanceof INeXusInfoWriteable) {
 			((INeXusInfoWriteable) detector).writeNeXusInformation(file);
 		}
 
 		file.makegroup("data_file", "NXnote");
 		file.opengroup("data_file", "NXnote");
 
 		int[] dataDim = generateDataDim(true, scanDimensions, new int[] { MAX_DATAFILENAME });
 
 		file.makedata("file_name", NexusFile.NX_CHAR, dataDim.length, dataDim);
 
 		// Close NXnote
 		file.closegroup();
 
 		// close NXdetector
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 
 		//FIXME this data group does not contain detector data, 
 		// we should not create this
 		// if this is the only detector the fallback group would be better 
 		// Make and open NXdata
 		file.makegroup(detectorName, "NXdata");
 		file.opengroup(detectorName, "NXdata");
 
 		makeAxesLinks();
 
 		// close NXdata
 		file.closegroup();
 
 		// close NXentry
 		file.closegroup();
 	}
 
 	/**
 	 * Creates an NXdetector for a generic detector (ie one without a special create routine).
 	 * 
 	 * @param detector
 	 * @throws NexusException
 	 */
 	private void makeGenericDetector(String detectorName, int[] dataDimensions, int type, Object detector,
 			INexusTree detectorData) throws NexusException {
 		// Navigate to the relevant section in file...
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 
 		// Create NXdetector
 		file.makegroup(detectorName, "NXdetector");
 		file.opengroup(detectorName, "NXdetector");
 
 		// Metadata items
 		try {
 			if (!(detector instanceof NexusDetector)) {
 				makeCreateStringData("description", ((Detector) detector).getDescription());
 				makeCreateStringData("type", ((Detector) detector).getDetectorType());
 				makeCreateStringData("id", ((Detector) detector).getDetectorID());
 			}
 		} catch (DeviceException e) {
 			e.printStackTrace();
 		}
 
 		// Check to see if the detector will write its own info into NeXus
 		if (detector instanceof INeXusInfoWriteable) {
 			((INeXusInfoWriteable) detector).writeNeXusInformation(file);
 		}
 
 		List<SelfCreatingLink> links = new Vector<SelfCreatingLink>();
 		if (detectorData != null) {
 			for (INexusTree subTree : detectorData) {
 				writeHere(file, subTree, true, false, links);
 			}
 		} else if (detector instanceof Detector && ((Detector) detector).getExtraNames().length > 0) {
 
 			// Detectors with multiple extra names can act like countertimers
 
 			int[] dataDim = generateDataDim(true, scanDimensions, null);
 
 			for (int j = 0; j < ((Detector) detector).getExtraNames().length; j++) {
 
 				file.makedata(((Detector) detector).getExtraNames()[j], NexusFile.NX_FLOAT64, dataDim.length, dataDim);
 
 				// Get a link ID to this data set
 				file.opendata(((Detector) detector).getExtraNames()[j]);
 				SelfCreatingLink detectorID = new SelfCreatingLink(file.getdataID());
 				file.closedata();
 
 				// close NXdetector
 				file.closegroup();
 				// close NXinstrument
 				file.closegroup();
 
 				if (j == 0) {
 					// If this is the first channel then we need to create (and
 					// open) the NXdata item and link to the scannables.
 					file.makegroup(((Detector) detector).getName(), "NXdata");
 					file.opengroup(((Detector) detector).getName(), "NXdata");
 
 					makeAxesLinks();
 				} else {
 					// Just open it.
 					file.opengroup(((Detector) detector).getName(), "NXdata");
 				}
 
 				// Make a link to the data array
 				detectorID.create(file);
 
 				// close NXdata
 				file.closegroup();
 
 				// Navigate back to the NXdetector, so we can add the next
 				// channel.
 				file.opengroup("instrument", "NXinstrument");
 				file.opengroup(((Detector) detector).getName(), "NXdetector");
 
 			}
 
 		} else {
 			// even make data area for detectors that first create their own files
 			int[] dataDim = generateDataDim(true, scanDimensions, dataDimensions);
 
 			// make the data array to store the data...
 			file.makedata("data", type, dataDim.length, dataDim);
 
 			// Get a link ID to this data set.
 			file.opendata("data");
 			links.add(new SelfCreatingLink(file.getdataID()));
 			file.closedata();
 		}
 
 		// close NXdetector
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 
 		// Make and open NXdata
 		file.makegroup(detectorName, "NXdata");
 		file.opengroup(detectorName, "NXdata");
 
 		// Make a link to the data array
 		for (SelfCreatingLink link : links) {
 			link.create(file);
 		}
 
 		makeAxesLinks();
 
 		// close NXdata
 		file.closegroup();
 
 		// close NXentry
 		file.closegroup();
 	}
 
 	/**
 	 * Creates an NXdetector for a CounterTimer.
 	 * 
 	 * @param detector
 	 * @throws NexusException
 	 * @throws DeviceException
 	 */
 	private void makeCounterTimer(Detector detector) throws NexusException, DeviceException {
 		SelfCreatingLink detectorID;
 		// CounterTimer ct = (CounterTimer) detector;
 
 		// Navigate to the relevant section in file...
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 
 		// Create NXdetector
 		file.makegroup(detector.getName(), "NXdetector");
 		file.opengroup(detector.getName(), "NXdetector");
 
 		// Metadata items
 		makeCreateStringData("description", detector.getDescription());
 		makeCreateStringData("type", detector.getDetectorType());
 		makeCreateStringData("id", detector.getDetectorID());
 
 		// Check to see if the detector will write its own info into NeXus
 		if (detector instanceof INeXusInfoWriteable) {
 			((INeXusInfoWriteable) detector).writeNeXusInformation(file);
 		}
 		int[] dataDim = generateDataDim(true, scanDimensions, null);
 
 		final String[] extraNames = detector.getExtraNames();
 		for (int j = 0; j < extraNames.length; j++) {
 
 			// this can fail if the list of names contains duplicates
 			file.makedata(extraNames[j], NexusFile.NX_FLOAT64, dataDim.length, dataDim);
 
 			// Get a link ID to this data set
 			file.opendata(extraNames[j]);
 			detectorID = new SelfCreatingLink(file.getdataID());
 			file.closedata();
 
 			// close NXdetector
 			file.closegroup();
 			// close NXinstrument
 			file.closegroup();
 
 			if (j == 0) {
 				// If this is the first channel then we need to create (and
 				// open) the NXdata item and link to the scannables.
 				file.makegroup(detector.getName(), "NXdata");
 				file.opengroup(detector.getName(), "NXdata");
 
 				makeAxesLinks();
 			} else {
 				// Just open it.
 				file.opengroup(detector.getName(), "NXdata");
 			}
 
 			// Make a link to the data array
 			detectorID.create(file);
 
 			// close NXdata
 			file.closegroup();
 
 			// Navigate back to the NXdetector, so we can add the next
 			// channel.
 			file.opengroup("instrument", "NXinstrument");
 			file.opengroup(detector.getName(), "NXdetector");
 
 		}
 
 		// Close NXdetector
 		file.closegroup();
 
 		// Close NXinstrument
 		file.closegroup();
 
 		// close NXentry
 		file.closegroup();
 	}
 
 	/**
 	 * wrap the dreaded static so behaviour can be customised
 	 * 
 	 * @return reference to file
 	 * @throws Exception
 	 */
 	protected NeXusFileInterface createFile() throws Exception {
 		return NexusFileFactory.createFile(nexusFileUrl, defaultNeXusBackend, LocalProperties.check(GDA_NEXUS_INSTRUMENT_API));
 	}
 	
 	/**
 	 * Create the next file. First increment the file number and then try and get a NeXus file handle from
 	 * {@link NexusFileFactory}.
 	 * @throws Exception 
 	 */
 	public void createNextFile() throws Exception {
 		try {
 			if (file != null) {
 				try {
 					file.flush();
 					file.finalize();
 				} catch (Throwable et) {
 					String error = "Error closing NeXus file.";
 					logger.error(error + et.getMessage());
 					terminalPrinter.print(error);
 					terminalPrinter.print(et.getMessage());
 				}
 			}
 
 			if (txtfile != null) {
 				try {
 					txtfile.flush();
 					txtfile.close();
 				} catch (Throwable et) {
 					String error = "Error closing ascii data file.";
 					logger.error(error + et.getMessage());
 					terminalPrinter.print(error);
 					terminalPrinter.print(et.getMessage());
 				}
 			}
 
 			// set the entry name
 			// this.entryName = "scan_" + run;
 			this.entryName = "entry1";
 
 			// construct filename
 			if (nexusFileNameTemplate != null) {
 				nexusFileName = String.format(nexusFileNameTemplate, scanNumber);
 			} else if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
 				nexusFileName = beamline + "-" + scanNumber + ".nxs";
 			} else {
 				nexusFileName = Long.toString(scanNumber) + ".nxs";
 			}
 
 			if (!dataDir.endsWith(File.separator)) {
 				dataDir += File.separator;
 			}
 
 			nexusFileUrl = dataDir + nexusFileName;
 
 			if (txtFileNameTemplate != null) {
 				txtFileName = String.format(txtFileNameTemplate, scanNumber);
 			} else if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
 				txtFileName = beamline + "-" + scanNumber + ".dat";
 			} else {
 				txtFileName = scanNumber + ".dat";
 			}
 			txtFileUrl = dataDir + txtFileName;
 
 			// Check to see if the file(s) already exists!
 			if (new File(nexusFileUrl).exists()) {
 				throw new Exception("The file " + nexusFileUrl + " already exists.");
 			}
 
 			// create nexus file and return handle
 			file = createFile();
 			if (createSrsFile) {
 				// Check to see if the file(s) already exists!
 				final File textFile = new File(txtFileUrl);
 				if (textFile.exists()) {
 					throw new Exception("The file " + txtFileUrl + " already exists.");
 				}
 				textFile.getParentFile().mkdirs();
 				if (!textFile.getParentFile().exists()) {
 					throw new Exception("Cannot create text file folder: " + textFile.getParentFile());
 				}
 				txtfile = new FileWriter(txtFileUrl);
 				// Now create the SRS header
 				txtfile.write(" &SRS\n");
 				// Write all the metadata items to the file.
 				if (metadata != null) {
 					for (IMetadataEntry entry : metadata.getMetadataEntries()) {
 						txtfile.write(entry.getName() + "=" + entry.getMetadataValue() + "\n");
 					}
 				}
 				txtfile.write(" &END\n");
 				// now write the column headings
 				txtfile.write(thisPoint.getHeaderString());
 				txtfile.write("\n");
 			}
 
 			// If we have been return a null file reference then there was
 			// some problem creating the file.
 			if (file == null) {
 				throw new Exception();
 			}
 
 			// Print informational message to console.
 			terminalPrinter.print("Writing data to file (NeXus): " + nexusFileUrl);
 			if (createSrsFile) {
 				terminalPrinter.print("Also creating file (txt): " + txtFileUrl);
 			}
 		} catch (Error ex) {
 			String error = "Failed to create file (" + nexusFileUrl;
 			if (createSrsFile) {
 				error += " or " + txtFileUrl;
 			}
 			error += ")";
 			error += ". Nexus binary library was not found. Inform Data Acquisition.";
 			logger.error(error, ex);
 			if (currentScanController != null) {
 				currentScanController.haltCurrentScan();
 			}
 			if (terminalPrinter != null){
 				terminalPrinter.print(error);
 				terminalPrinter.print(ex.getMessage());
 			}
 			throw ex;
 		} catch (Exception ex) {
 				String error = "Failed to create file (" + nexusFileUrl;
 				if (createSrsFile) {
 					error += " or " + txtFileUrl;
 				}
 				error += ")";
 				logger.error(error, ex);
 				if (currentScanController != null) {
 					currentScanController.haltCurrentScan();
 				}
 				if (terminalPrinter != null){
 					terminalPrinter.print(error);
 					terminalPrinter.print(ex.getMessage());
 				}
 				throw ex;
 			}
 
 	}
 
 	/**
 	 * Returns the full path of the folder which data files are written to.
 	 * 
 	 * @return the full path of the folder which data files are written
 	 */
 	public String getDataDir() {
 		return dataDir;
 	}
 
 	/**
 	 * Not used in this implementation.
 	 * 
 	 * @param header
 	 *            String
 	 */
 	@Override
 	public void setHeader(String header) {
 	}
 
 	/**
 	 * Writes the data for a given scannable to an existing NXpositioner.
 	 * 
 	 * @param scannable
 	 * @throws NexusException
 	 */
 	protected void writeScannable(Scannable scannable) throws NexusException {
 		int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
 		int[] dimArray = generateDataDim(false, dataDimPrefix, null);
 
 		// Get inputnames and positions
 		String[] inputNames = scannable.getInputNames();
 		String[] extraNames = scannable.getExtraNames();
 		Double[] positions = extractDoublePositions(scannable.getName());
 
 		logger.debug("Writing data for scannable (" + scannable.getName() + ") to NeXus file.");
 
 		// Navigate to correct location in the file.
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 		file.opengroup(scannable.getName(), getGroupClassFor(scannable));
 
 		// Loop over inputNames...
 		for (int i = 0; i < inputNames.length; i++) {
 			// Open data item
 			file.opendata(inputNames[i]);
 
 			double[] tmpData = new double[1];
 			tmpData[0] = positions[i];
 
 			file.putslab(tmpData, startPos, dimArray);
 
 			file.closedata();
 		}
 
 		// and now over extraNames...
 		for (int i = 0; i < extraNames.length; i++) {
 			// Open data item
 			file.opendata(extraNames[i]);
 			double[] tmpData = new double[1];
 			tmpData[0] = positions[inputNames.length + i];
 
 			file.putslab(tmpData, startPos, dimArray);
 
 			file.closedata();
 		}
 
 		// close NXpositioner/NXmonitor
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 		// Close NXentry
 		file.closegroup();
 	}
 
 	private void writeCounterTimer(Detector detector, double newData[]) throws NexusException {
 		int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
 		int[] dimArray = generateDataDim(false, dataDimPrefix, null);
 
 		logger.debug("Writing data for Detector (" + detector.getName() + ") to NeXus file.");
 
 		// Navigate to correct location in the file.
 		file.opengroup(this.entryName, "NXentry");
 		file.opengroup("instrument", "NXinstrument");
 		file.opengroup(detector.getName(), "NXdetector");
 
 		for (int j = 0; j < detector.getExtraNames().length; j++) {
 			file.opendata(detector.getExtraNames()[j]);
 
 			double[] tmpData = new double[1];
 			tmpData[0] = newData[j];
 
 			file.putslab(tmpData, startPos, dimArray);
 
 			// Close data
 			file.closedata();
 		}
 
 		// Close NXdetector
 		file.closegroup();
 		// close NXinstrument
 		file.closegroup();
 		// Close NXentry
 		file.closegroup();
 	}
 
 	private void writeCounterTimer(Detector detector) throws NexusException {
 		double[] newData = extractDoubleData(detector.getName());
 		writeCounterTimer(detector, newData);
 	}
 
 	@Override
 	public String getCurrentScanIdentifier() {
 		try {
 			return String.valueOf(getScanNumber());
 		} catch (Exception e) {
 			logger.error("Error getting scanIdentifier", e);
 		}
 		return "unknown";
 	}
 
 	/**
 	 * @param txtFileNameTemplate
 	 *            the txtFileNameTemplate to set
 	 */
 	public void setTxtFileNameTemplate(String txtFileNameTemplate) {
 		this.txtFileNameTemplate = txtFileNameTemplate;
 	}
 
 	/**
 	 * @param nexusFileNameTemplate
 	 *            the nexusFileNameTemplate to set
 	 * @throws Exception
 	 */
 	public void setNexusFileNameTemplate(String nexusFileNameTemplate) throws Exception {
 		this.nexusFileNameTemplate = nexusFileNameTemplate;
 		// We calculate some probable paths now so that the probable
 		// file name and path are known should the intended file path
 		// be queried before being written. (NOTE this is happening in
 		// XasAsciiNexusDataWriter currently.
 		this.nexusFileName = String.format(nexusFileNameTemplate, getScanNumber());
 		this.nexusFileUrl = dataDir + nexusFileName;
 	}
 
 	@Override
 	public void addDataWriterExtender(final IDataWriterExtender e) {
 		super.addDataWriterExtender(e);
 		this.createSrsFile = false;
 	}
 
 	public boolean isFirstData() {
 		return firstData;
 	}
 }
