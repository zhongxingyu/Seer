 package org.gwaspi.netCDF.operations;
 
 import org.gwaspi.constants.cDBGWASpi;
 import org.gwaspi.constants.cDBOperations;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.database.DbManager;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.ServiceLocator;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class OperationMetadata {
 
 	private static final Logger log = LoggerFactory.getLogger(OperationMetadata.class);
 
 	private int op_id = Integer.MIN_VALUE;
 	private int parentMatrixId = Integer.MIN_VALUE;
 	private int parentOperationId = Integer.MIN_VALUE;
 	private String op_name = "";
 	private String netCDF_name = "";
 	private String description = "";
 	private String pathToMatrix = "";
 	private String gtCode = "";
 	private int opSetSize = Integer.MIN_VALUE;
 	private int implicitSetSize = Integer.MIN_VALUE;
 	private int studyId = Integer.MIN_VALUE;
 
 	public OperationMetadata(int opId) throws IOException {
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 		List<Map<String, Object>> rs = dBManager.executeSelectStatement(
 				"SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId + "  WITH RR");
 
 		op_id = opId;
 
 		if (!rs.isEmpty()) {
 			//PREVENT PHANTOM-DB READS EXCEPTIONS
 			if (!rs.isEmpty() && rs.get(0).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
 				parentMatrixId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_MATRIXID).toString());
 				parentOperationId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_OPID).toString());
 				op_name = rs.get(0).get(cDBOperations.f_OP_NAME).toString();
 				netCDF_name = rs.get(0).get(cDBOperations.f_OP_NETCDF_NAME).toString();
 				description = rs.get(0).get(cDBOperations.f_DESCRIPTION).toString();
 				studyId = (Integer) rs.get(0).get(cDBOperations.f_STUDYID);
 			}
 
 			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 			String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
 			pathToMatrix = pathToStudy + netCDF_name + ".nc";
 			NetcdfFile ncfile = null;
 			if (new File(pathToMatrix).exists()) {
 				try {
 					ncfile = NetcdfFile.open(pathToMatrix);
 //					gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();
 
 					Dimension setDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
 					opSetSize = setDim.getLength();
 
 					Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
 					implicitSetSize = implicitDim.getLength();
 				} catch (IOException ex) {
					log.error("Cannot open file: " + ncfile.getLocation(), ex);
 				} finally {
 					if (null != ncfile) {
 						try {
 							ncfile.close();
 						} catch (IOException ex) {
 							log.warn("Cannot close file: " + ncfile.getLocation(), ex);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public OperationMetadata(String netCDFname) throws IOException {
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 
 		String sql = "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_OP_NETCDF_NAME + "='" + netCDFname + "' ORDER BY " + cDBOperations.f_ID + " DESC  WITH RR";
 		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);
 
 		//PREVENT PHANTOM-DB READS EXCEPTIONS
 		if (!rs.isEmpty() && rs.get(0).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
 			op_id = Integer.parseInt(rs.get(0).get(cDBOperations.f_ID).toString());
 			parentMatrixId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_MATRIXID).toString());
 			parentOperationId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_OPID).toString());
 			op_name = rs.get(0).get(cDBOperations.f_OP_NAME).toString();
 			netCDF_name = netCDFname;
 			description = rs.get(0).get(cDBOperations.f_DESCRIPTION).toString();
 			studyId = (Integer) rs.get(0).get(cDBOperations.f_STUDYID);
 		}
 
 		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
 		pathToMatrix = pathToStudy + netCDF_name + ".nc";
 		NetcdfFile ncfile = null;
 		if (new File(pathToMatrix).exists()) {
 			try {
 				ncfile = NetcdfFile.open(pathToMatrix);
 
 //				gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();
 
 				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
 				opSetSize = markerSetDim.getLength();
 
 				Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
 				implicitSetSize = implicitDim.getLength();
 
 			} catch (IOException ex) {
				log.error("Cannot open file: " + ncfile.getLocation(), ex);
 			} finally {
 				if (null != ncfile) {
 					try {
 						ncfile.close();
 					} catch (IOException ex) {
 						log.warn("Cannot close file: " + ncfile.getLocation(), ex);
 					}
 				}
 			}
 		}
 	}
 
 	public int getOPId() {
 		return op_id;
 	}
 
 	public int getParentMatrixId() {
 		return parentMatrixId;
 	}
 
 	public int getStudyId() {
 		return studyId;
 	}
 
 	public String getOPName() {
 		return op_name;
 	}
 
 	public String getMatrixCDFName() {
 		return netCDF_name;
 	}
 
 	public String getGenotypeCode() {
 		return gtCode;
 	}
 
 	public int getOpSetSize() {
 		return opSetSize;
 	}
 
 	public String getPathToMatrix() {
 		return pathToMatrix;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public int getParentOperationId() {
 		return parentOperationId;
 	}
 
 	public int getImplicitSetSize() {
 		return implicitSetSize;
 	}
 }
