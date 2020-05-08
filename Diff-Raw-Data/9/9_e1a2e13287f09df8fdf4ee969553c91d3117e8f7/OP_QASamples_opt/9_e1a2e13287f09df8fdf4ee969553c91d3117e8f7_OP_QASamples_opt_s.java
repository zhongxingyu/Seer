 package org.gwaspi.netCDF.operations;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.netCDF.markers.MarkerSet_opt;
 import org.gwaspi.samples.SampleSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.NetcdfFileWriteable;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class OP_QASamples_opt implements MatrixOperation {
 
 	private final Logger log = LoggerFactory.getLogger(OP_QASamples_opt.class);
 
 	private int rdMatrixId;
 
 	public OP_QASamples_opt(int rdMatrixId) {
 		this.rdMatrixId = rdMatrixId;
 	}
 
 	public int processMatrix() throws IOException, InvalidRangeException {
 		int resultOpId = Integer.MIN_VALUE;
 
 		Map<String, Object> wrSampleSetMissingCountMap = new LinkedHashMap();
 		Map<String, Object> wrSampleSetMissingRatioMap = new LinkedHashMap();
 		Map<String, Object> wrSampleSetHetzyRatioMap = new LinkedHashMap();
 
 		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixId);
 
 		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
 
 		MarkerSet_opt rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
 		rdMarkerSet.initFullMarkerIdSetMap();
 
 		Map<String, Object> rdChrSetMap = rdMarkerSet.getChrInfoSetMap();
 
 		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; // This to test heap usage of copying locally the Map from markerset
 
 		SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
 		Map<String, Object> rdSampleSetMap = rdSampleSet.getSampleIdSetMap();
 
 		// Iterate through samples
 		int sampleNb = 0;
 		for (String sampleId : rdSampleSetMap.keySet()) {
 			Integer missingCount = 0;
 			Integer heterozygCount = 0;
 
 			// Iterate through markerset
 			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
 			int markerIndex = 0;
 			for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
 				byte[] tempGT = (byte[]) entry.getValue();
 				if (tempGT[0] == AlleleBytes._0 && tempGT[1] == AlleleBytes._0) {
 					missingCount++;
 				}
 
 				// WE DON'T WANT NON AUTOSOMAL CHR FOR HETZY
 				String currentChr = MarkerSet_opt.getChrByMarkerIndex(rdChrSetMap, markerIndex);
 				if (!currentChr.equals("X")
 						&& !currentChr.equals("Y")
 						&& !currentChr.equals("XY")
 						&& !currentChr.equals("MT")) {
 					if (tempGT[0] != tempGT[1]) {
 						heterozygCount++;
 					}
 				}
 				markerIndex++;
 			}
 
 			wrSampleSetMissingCountMap.put(sampleId, missingCount);
 
 			double missingRatio = (double) missingCount / rdMarkerSet.getMarkerIdSetMap().size();
 			wrSampleSetMissingRatioMap.put(sampleId, missingRatio);
 			double heterozygRatio = (double) heterozygCount / (rdMarkerSet.getMarkerIdSetMap().size() - missingCount);
 			wrSampleSetHetzyRatioMap.put(sampleId, heterozygRatio);
 
 			sampleNb++;
 
 			if (sampleNb % 100 == 0) {
 				log.info("Processed samples: {}", sampleNb);
 			}
 		}
 
 		// Write census, missing-ratio and mismatches to netCDF
 		NetcdfFileWriteable wrNcFile = null;
 		try {
 			// CREATE netCDF-3 FILE
 
 			OperationFactory wrOPHandler = new OperationFactory(rdMatrixMetadata.getStudyId(),
 					"Sample QA", //friendly name
 					"Sample census on " + rdMatrixMetadata.getMatrixFriendlyName() + "\nSamples: " + wrSampleSetMissingCountMap.size(), //description
 					wrSampleSetMissingCountMap.size(),
 					rdMatrixMetadata.getMarkerSetSize(),
 					0,
 					cNetCDF.Defaults.OPType.SAMPLE_QA.toString(),
 					rdMatrixMetadata.getMatrixId(), // Parent matrixId
 					-1);                            // Parent operationId
 
 			wrNcFile = wrOPHandler.getNetCDFHandler();
 			try {
 				wrNcFile.create();
 			} catch (IOException ex) {
 				log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
 			}
 			//log.trace("Done creating netCDF handle: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 			//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
 			// SAMPLESET
 			ArrayChar.D2 samplesD2 = Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
 
 			int[] sampleOrig = new int[]{0, 0};
 			try {
 				wrNcFile.write(cNetCDF.Variables.VAR_OPSET, sampleOrig, samplesD2);
 			} catch (IOException ex) {
 				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
 			} catch (InvalidRangeException ex) {
 				log.error(null, ex);
 			}
 			log.info("Done writing SampleSet to matrix");
 
 			//WRITE MARKERSET TO MATRIX
 			ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetMap(), cNetCDF.Strides.STRIDE_MARKER_NAME);
 			int[] markersOrig = new int[]{0, 0};
 			try {
 				wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, markersOrig, markersD2);
 			} catch (IOException ex) {
 				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
 			} catch (InvalidRangeException ex) {
 				log.error(null, ex);
 			}
 			log.info("Done writing MarkerSet to matrix");
 			//</editor-fold>
 
 			//<editor-fold defaultstate="collapsed" desc="CENSUS DATA WRITER">
 			// MISSING RATIO
 			Utils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrSampleSetMissingRatioMap, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
 
 			// MISSING COUNT
 			Utils.saveIntMapD1ToWrMatrix(wrNcFile, wrSampleSetMissingCountMap, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);
 
 			// HETEROZYGOSITY RATIO
 			Utils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrSampleSetHetzyRatioMap, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
 			//</editor-fold>
 
 			resultOpId = wrOPHandler.getResultOPId();
 		} finally {
 			if (null != rdNcFile) {
 				try {
 					rdNcFile.close();
 					wrNcFile.close();
 				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
 				}
 			}
 
 			org.gwaspi.global.Utils.sysoutCompleted("Sample QA");
 		}
 
 		return resultOpId;
 	}
 }
