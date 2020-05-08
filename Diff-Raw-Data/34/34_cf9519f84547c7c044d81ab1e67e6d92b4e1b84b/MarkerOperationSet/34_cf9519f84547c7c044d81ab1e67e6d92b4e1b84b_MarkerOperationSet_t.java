 package org.gwaspi.netCDF.operations;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.model.MarkerKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.ArrayInt;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class MarkerOperationSet extends AbstractOperationSet<MarkerKey> {
 
 	private static final Logger log = LoggerFactory.getLogger(MarkerOperationSet.class);
 
 	public MarkerOperationSet(int studyId, int opId) throws IOException {
 		super(studyId, opId, MarkerKey.KEY_FACTORY);
 	}
 
 	//<editor-fold defaultstate="expanded" desc="CHROMOSOME INFO">
 	public Map<String, Object> getChrInfoSetMap() {
 		NetcdfFile ncfile = null;
 		Map<String, Object> chrInfoMap = new LinkedHashMap<String, Object>();
 
 		try {
 			ncfile = NetcdfFile.open(getOperationMetadata().getPathToMatrix());
 
 			// GET NAMES OF CHROMOSOMES
 			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX);
 
 			if (null == var) {
 				return null;
 			}
 
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
			var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_INFO); // Nb of markers, first physical position, last physical position, start index number in MarkerSet
			if (var == null) {
				return null;
			}
 			dataType = var.getDataType();
 			varShape = var.getShape();
 
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
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 
 		return chrInfoMap;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="OPERATION-SET PICKERS">
 	public Map<MarkerKey, Object> pickValidMarkerSetItemsByValue(NetcdfFile ncfile, String variable, Set<Object> criteria, boolean includes) {
 		Map<MarkerKey, Object> returnMap = new LinkedHashMap<MarkerKey, Object>();
 		Map<MarkerKey, Object> readMap = fillOpSetMapWithVariable(ncfile, variable);
 
 		if (includes) {
 			for (Map.Entry<MarkerKey, Object> entry : readMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object value = entry.getValue();
 				if (criteria.contains(value)) {
 					returnMap.put(key, value);
 				}
 			}
 		} else {
 			for (Map.Entry<MarkerKey, Object> entry : readMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object value = entry.getValue();
 				if (!criteria.contains(value)) {
 					returnMap.put(key, value);
 				}
 			}
 		}
 
 		return returnMap;
 	}
 
 	private static <MarkerKey> Map<MarkerKey, Object> pickValidMarkerSetItemsByKey(Map<MarkerKey, Object> map, Set<Object> criteria, boolean includes) {
 		Map<MarkerKey, Object> returnMap = new LinkedHashMap<MarkerKey, Object>();
 
 		if (includes) {
 			for (Map.Entry<MarkerKey, Object> entry : map.entrySet()) {
 				MarkerKey key = entry.getKey();
 				if (criteria.contains(key)) {
 					returnMap.put(key, entry.getValue());
 				}
 			}
 		} else {
 			for (Map.Entry<MarkerKey, Object> entry : map.entrySet()) {
 				MarkerKey key = entry.getKey();
 				if (!criteria.contains(key)) {
 					returnMap.put(key, entry.getValue());
 				}
 			}
 		}
 
 		return returnMap;
 	}
 	//</editor-fold>
 }
