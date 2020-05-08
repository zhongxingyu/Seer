 package org.gwaspi.netCDF.markers;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.MatrixMetadata;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayByte;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.ArrayDouble;
 import ucar.ma2.ArrayFloat;
 import ucar.ma2.ArrayInt;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 
 /**
  * This modification of the MarkerSet can create instances of the MarkerSetMap
  * The markerSet Object has several methods to initialize it's Map, which can later
  * be filled with it's corresponding values.
  * The Map is not copied or passed as a parameter to the fillers.
  * The Maps are made public so that they can be referenced from other classes,
  * avoiding duplication of large Map datasets in memory.
  *
  * The matrix netCDF file is opened at creation of the MarkerSet and closed
  * at finalization of the class. No need to pass a netCDF handler anymore.
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class MarkerSet_opt {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(MarkerSet_opt.class);
 
 	// MARKERSET_MEATADATA
 	private ImportFormat technology = ImportFormat.UNKNOWN; // platform
 	private int markerSetSize = 0; // probe_nb
 	private MatrixMetadata matrixMetadata;
 	private NetcdfFile ncfile = null;
 	private int startMkIdx = 0;
 	private int endMkIdx = Integer.MIN_VALUE;
 	private Map<String, Object> markerIdSetMap = new LinkedHashMap<String, Object>();
 	private Map<String, Object> markerRsIdSetMap = new LinkedHashMap<String, Object>();
 
 	public MarkerSet_opt(int studyId, int matrixId) throws IOException {
 		matrixMetadata = MatricesList.getMatrixMetadataById(matrixId);
 		technology = matrixMetadata.getTechnology();
 		markerSetSize = matrixMetadata.getMarkerSetSize();
 
 		ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
 	}
 
 	public MarkerSet_opt(int studyId, String netCDFPath, String netCDFName) throws IOException {
 		matrixMetadata = MatricesList.getMatrixMetadata(netCDFPath, studyId, netCDFName);
 		technology = matrixMetadata.getTechnology();
 		markerSetSize = matrixMetadata.getMarkerSetSize();
 
 		ncfile = NetcdfFile.open(netCDFPath);
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		try {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close netCDF file: " + ncfile.getLocation(), ex);
 				}
 			}
 		} finally {
 			super.finalize();
 		}
 	}
 
 	// ACCESSORS
 	public ImportFormat getTechnology() {
 		return technology;
 	}
 
 	public int getMarkerSetSize() {
 		return markerSetSize;
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="MARKERSET INITILAIZERS">
 	//USE MARKERID AS KEYS
 	public void initFullMarkerIdSetMap() {
 		startMkIdx = 0;
 		endMkIdx = Integer.MIN_VALUE;
 		initMarkerIdSetMap(startMkIdx, endMkIdx);
 	}
 
 	public void initMarkerIdSetMap(int _startMkInd, int _endMkIdx) {
 		startMkIdx = _startMkInd;
 		endMkIdx = _endMkIdx;
 
 		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERSET);
 
 		if (var != null) {
 
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 
 			try {
 				// KEEP INDEXES REAL
 				if (startMkIdx < 0) {
 					startMkIdx = 0;
 				}
 				if (endMkIdx < 0 || endMkIdx >= markerSetSize) {
 					endMkIdx = markerSetSize - 1;
 				}
 
 				if (dataType == DataType.CHAR) {
 					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
 					markerIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
 				}
 				if (dataType == DataType.BYTE) {
 					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
 					markerIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 	}
 
 	// USE RSID AS KEYS
 	public void initFullRsIdSetMap() {
 		startMkIdx = 0;
 		endMkIdx = Integer.MIN_VALUE;
 		initRsIdSetMap(startMkIdx, endMkIdx);
 	}
 
 	public void initRsIdSetMap(int _startMkInd, int _endMkIdx) {
 		startMkIdx = _startMkInd;
 		endMkIdx = _endMkIdx;
 
 		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
 
 		if (var != null) {
 
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 
 			try {
 				// KEEP INDEXES REAL
 				if (startMkIdx == Integer.MIN_VALUE) {
 					startMkIdx = 0;
 				}
 				if (endMkIdx == Integer.MIN_VALUE) {
 					endMkIdx = markerSetSize - 1;
 				}
 
 				if (dataType == DataType.CHAR) {
 					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
 				}
 				if (dataType == DataType.BYTE) {
 					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="CHROMOSOME INFO">
 	/**
      * This Method is safe to return an independent Map.
 	 * The size of this Map is very small.
 	 */
 	public Map<String, Object> getChrInfoSetMap() {
 
 		Map<String, Object> chrInfoMap = new LinkedHashMap<String, Object>();
 
 		// GET NAMES OF CHROMOSOMES
 		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX);
 		if (var != null) {
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 			try {
 				if (dataType == DataType.CHAR) {
 					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:7:1)");
 					chrInfoMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 
 			// GET INFO FOR EACH CHROMOSOME
 			var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_INFO); //Nb of markers, first physical position, last physical position, end index number in MarkerSet
 			dataType = var.getDataType();
 			varShape = var.getShape();
 			if (null == var) {
 				return null;
 			}
 
 			try {
 				if (dataType == DataType.INT) {
 					ArrayInt.D2 chrSetAI = (ArrayInt.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:3:1)");
 					org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(chrSetAI, chrInfoMap);
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		} else {
 			return null;
 		}
 
 		return chrInfoMap;
 	}
 
 	public static String getChrByMarkerIndex(Map<String, Object> chrInfoMap, int markerIndex) {
 		String result = null;
 		for (Map.Entry<String, Object> entry : chrInfoMap.entrySet()) {
 			String chr = entry.getKey();
 			int[] value = (int[]) entry.getValue();
 			if (markerIndex <= value[3] && result == null) {
 				result = chr.toString();
 			}
 		}
 		return result;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="MARKERSET FILLERS">
 	public void fillGTsForCurrentSampleIntoInitMap(int sampleNb) throws IOException {
 
 		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);
 
 		if (var != null) {
 
 			int[] varShape = var.getShape();
 
 			try {
 				// KEEP INDEXES REAL
 				if (startMkIdx < 0) {
 					startMkIdx = 0;
 				}
 				if (endMkIdx < 0 || endMkIdx >= markerSetSize) {
 					endMkIdx = markerSetSize - 1;
 				}
 
 				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) var.read("(" + sampleNb + ":" + sampleNb + ":1, "
 						+ startMkIdx + ":" + endMkIdx + ":1, "
 						+ "0:" + (varShape[2] - 1) + ":1)");
 
 				int[] shp = gt_ACD3.getShape();
 				int reducer = 0;
 				if (shp[0] == 1) {
 					reducer++;
 				}
 				if (shp[1] == 1) {
 					reducer++;
 				}
 				if (shp[2] == 1) {
 					reducer++;
 				}
 
 				if (reducer == 1) {
 					ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
 					org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToMapValues(gt_ACD2, markerIdSetMap);
 				} else if (reducer == 2) {
 					ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
 					org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToMapValues(gt_ACD1, markerIdSetMap);
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 	}
 
 	public void fillInitMapWithVariable(String variable) {
 
 		Variable var = ncfile.findVariable(variable);
 		if (var != null) {
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 
 			try {
 				if (dataType == DataType.CHAR) {
 					if (varShape.length == 2) {
 						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
 						org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, markerIdSetMap);
 					}
 				}
 				if (dataType == DataType.DOUBLE) {
 					if (varShape.length == 1) {
 						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
 						org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(markerSetAF, markerIdSetMap);
 					}
 					if (varShape.length == 2) {
 						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1))");
 						org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToMapValues(markerSetAF, markerIdSetMap);
 					}
 				}
 				if (dataType == DataType.INT) {
 					if (varShape.length == 1) {
 						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
 						org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToMapValues(markerSetAD, markerIdSetMap);
 					}
 					if (varShape.length == 2) {
 						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1))");
 						org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(markerSetAD, markerIdSetMap);
 					}
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 	}
 
 	public void fillMarkerSetMapWithChrAndPos() {
 
 		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 		if (var != null) {
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 
 			try {
 				if (dataType == DataType.CHAR) {
 					if (varShape.length == 2) {
 						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
 						org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, markerIdSetMap);
 					}
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 
 		var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 		if (var != null) {
 			DataType dataType = var.getDataType();
 //			int[] varShape = var.getShape();
 
 			try {
 				if (dataType == DataType.INT) {
 					ArrayInt.D1 markerSetAI = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
 
 					int[] shape = markerSetAI.getShape();
 					Index index = markerSetAI.getIndex();
 					Iterator<Entry<String, Object>> it = markerIdSetMap.entrySet().iterator();
 					for (int i = 0; i < shape[0]; i++) {
 						Entry<String, Object> entry = it.next();
 						Object[] chrInfo = new Object[2];
 						chrInfo[0] = entry.getValue(); // CHR
 						chrInfo[1] = markerSetAI.getInt(index.set(i)); // POS
 						entry.setValue(chrInfo);
 					}
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 	}
 
 	public void fillWith(Object value) {
 		fillWith(markerIdSetMap, value);
 	}
 
 	public static <K, V> void fillWith(Map<K, V> map, V defaultVal) {
 
 		for (Map.Entry<K, V> entry : map.entrySet()) {
 			entry.setValue(defaultVal);
 		}
 	}
 
 	// HELPERS TO TRANSFER VALUES FROM ONE Map TO ANOTHER
 	public static <K, V> void replaceWithValuesFrom(Map<K, V> toBeModified, Map<K, V> newValuesSource) {
 
 		for (Map.Entry<K, V> entry : toBeModified.entrySet()) {
 			entry.setValue(newValuesSource.get(entry.getKey()));
 		}
 	}
 
 	// HELPER TO APPEND VALUE TO AN EXISTING Map CHAR VALUE
 	public void appendVariableToMarkerSetMapValue(String variable, String separator) {
 
 		Variable var = ncfile.findVariable(variable);
 		if (var != null) {
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 			try {
 				if (dataType == DataType.CHAR) {
 					if (varShape.length == 2) {
 						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
 
 						int[] shape = markerSetAC.getShape();
 						Index index = markerSetAC.getIndex();
 						Iterator<Entry<String, Object>> it = markerIdSetMap.entrySet().iterator();
 						for (int i = 0; i < shape[0]; i++) {
 							Entry<String, Object> entry = it.next();
 							String value = entry.getValue().toString();
 							if (!value.isEmpty()) {
 								value += separator;
 							}
 							StringBuilder newValue = new StringBuilder();
 							for (int j = 0; j < shape[1]; j++) {
 								newValue.append(markerSetAC.getChar(index.set(i, j)));
 							}
 							entry.setValue(value + newValue.toString().trim());
 						}
 					}
 				}
 				if (dataType == DataType.FLOAT) {
 					if (varShape.length == 1) {
 						ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
 
 						int[] shape = markerSetAF.getShape();
 						Index index = markerSetAF.getIndex();
 						Iterator<Entry<String, Object>> it = markerIdSetMap.entrySet().iterator();
 						for (int i = 0; i < shape[0]; i++) {
 							Entry<String, Object> entry = it.next();
 							String value = entry.getValue().toString();
 							if (!value.isEmpty()) {
 								value += separator;
 							}
 							Float floatValue = markerSetAF.getFloat(index.set(i));
 							entry.setValue(value + floatValue.toString());
 						}
 					}
 				}
 				if (dataType == DataType.INT) {
 					if (varShape.length == 1) {
 						ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
 
 						int[] shape = markerSetAF.getShape();
 						Index index = markerSetAF.getIndex();
 						Iterator<Entry<String, Object>> it = markerIdSetMap.entrySet().iterator();
 						for (int i = 0; i < shape[0]; i++) {
 							Entry<String, Object> entry = it.next();
 							String value = entry.getValue().toString();
 							if (!value.isEmpty()) {
 								value += separator;
 							}
 							Integer intValue = markerSetAF.getInt(index.set(i));
 							entry.setValue(value + intValue.toString());
 						}
 					}
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		}
 	}
 
 	/**
 	 * HELPER GETS DICTIONARY OF CURRENT MATRIX. IS CONCURRENT TO INSTANTIATED Map
 	 */
 	public Map<String, Object> getDictionaryBases() throws IOException {
 		Map<String, Object> dictionnary = new LinkedHashMap<String, Object>();
 		try {
 			Variable varBasesDict = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
 			if (null != varBasesDict) {
 				int[] dictShape = varBasesDict.getShape();
 
 				ArrayChar.D2 dictAlleles_ACD2 = (ArrayChar.D2) varBasesDict.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (dictShape[1] - 1) + ":1)");
 
 				Index index = dictAlleles_ACD2.getIndex();
 				Iterator<String> it = markerIdSetMap.keySet().iterator();
 				for (int i = 0; i < dictShape[0]; i++) {
 					String key = it.next();
 					StringBuilder alleles = new StringBuilder("");
 					// Get Alleles
 					for (int j = 0; j < dictShape[1]; j++) {
 						alleles.append(dictAlleles_ACD2.getChar(index.set(i, j)));
 					}
 					dictionnary.put(key, alleles.toString().trim());
 				}
 			}
 		} catch (InvalidRangeException ex) {
 			log.error("Cannot read data", ex);
 		}
 		return dictionnary;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="MARKERSET PICKERS">
 	/**
 	 * THESE Maps DO NOT CONTAIN SAME ITEMS AS INIT Map.
 	 * RETURN Map OK
 	 */
 	public Map<String, Object> pickValidMarkerSetItemsByValue(String variable, Set<Object> criteria, boolean includes) {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 		this.fillInitMapWithVariable(variable);
 
 		if (includes) {
 			for (Map.Entry<String, Object> entry : markerIdSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = entry.getValue();
 				if (criteria.contains(value)) {
 					returnMap.put(key, value);
 				}
 			}
 		} else {
 			for (Map.Entry<String, Object> entry : markerIdSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = entry.getValue();
 				if (!criteria.contains(value)) {
 					returnMap.put(key, value);
 				}
 			}
 		}
 
 		return returnMap;
 	}
 
 	public Map<String, Object> pickValidMarkerSetItemsByKey(Set<Object> criteria, boolean includes) {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 
 		if (includes) {
 			for (Map.Entry<String, Object> entry : markerIdSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = entry.getValue();
 				if (criteria.contains(key)) {
 					returnMap.put(key, value);
 				}
 			}
 		} else {
 			for (Map.Entry<String, Object> entry : markerIdSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = entry.getValue();
 				if (!criteria.contains(key)) {
 					returnMap.put(key, value);
 				}
 			}
 		}
 
 		return returnMap;
 	}
 	//</editor-fold>
 
 	public Map<String, Object> getMarkerIdSetMap() {
 		return markerIdSetMap;
 	}
 
 	public Map<String, Object> getMarkerRsIdSetMap() {
 		return markerRsIdSetMap;
 	}
 }
