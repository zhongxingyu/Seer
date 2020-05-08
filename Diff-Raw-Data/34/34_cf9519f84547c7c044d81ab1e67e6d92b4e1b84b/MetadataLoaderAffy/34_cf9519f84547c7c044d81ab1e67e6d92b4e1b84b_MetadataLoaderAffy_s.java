 package org.gwaspi.netCDF.loader;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import org.gwaspi.constants.cImport.Annotation.Affymetrix_GenomeWide6;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.global.Text;
 import org.gwaspi.model.MarkerKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class MetadataLoaderAffy implements MetadataLoader {
 
 	private final Logger log
 			= LoggerFactory.getLogger(MetadataLoaderAffy.class);
 
 	/** Duplicate SNPs to be removed */
 	private static final SNPBlacklist snpBlackList = new SNPBlacklist();
 
 	private String annotationPath;
 	private int studyId;
 	private ImportFormat format;
 
 	public MetadataLoaderAffy(String annotationPath, ImportFormat format, int studyId) {
 
 		this.annotationPath = annotationPath;
 		this.studyId = studyId;
 		this.format = format;
 	}
 
 	@Override
 	public Map<MarkerKey, Object> getSortedMarkerSetWithMetaData() throws IOException {
 		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
 
 		SortedMap<String, String> tempTM = parseAnnotationBRFile(); // affyId, rsId,chr,pseudo-autosomal,pos, strand, alleles, plus-alleles
 
 		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
 		log.info(Text.All.processing);
 
 		Map<MarkerKey, Object> markerMetadataMap = new LinkedHashMap<MarkerKey, Object>();
 		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
 			// keyValues = chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
 			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
 			int pos;
 			try {
 				pos = Integer.parseInt(keyValues[3]);
 			} catch (Exception ex) {
 				pos = 0;
 				log.warn(null, ex);
 			}
 
 			// valValues = rsId;strand;alleles
 			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);
 			valValues = fixRsId(keyValues, valValues);
 			keyValues = fixChrData(keyValues);
 
 			Object[] markerInfo = new Object[8];
 			markerInfo[0] = keyValues[4]; // 0 => affyid
 			markerInfo[1] = valValues[0]; // 1 => rsId
 			markerInfo[2] = keyValues[0]; // 2 => chr
 			markerInfo[3] = keyValues[1]; // 3 => pseudo-autosomal1
 			markerInfo[4] = keyValues[2]; // 4 => pseudo-autosomal2
 			markerInfo[5] = pos; // 5 => pos
 			markerInfo[6] = valValues[1]; // 6 => strand
 			markerInfo[7] = valValues[2]; // 7 => alleles
 
 			markerMetadataMap.put(MarkerKey.valueOf(keyValues[4]), markerInfo);
 		}
 
 		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
 		MetadataLoaderPlink.logAsWhole(startTime, annotationPath, description, studyId);
 		return markerMetadataMap;
 	}
 
 	private SortedMap<String, String> parseAnnotationBRFile() throws IOException {
 		FileReader fr = new FileReader(annotationPath);
 		BufferedReader inputAnnotationBr = new BufferedReader(fr);
 		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());
 
 		String header = "";
		while (!header.startsWith("\"Probe Set ID") && header != null) { // FIXME change order (do null-check first!)
 			header = inputAnnotationBr.readLine();
 		}
 
 		String l;
 		int count = 0;
 		while ((l = inputAnnotationBr.readLine()) != null) {
 			String[] affy6Vals = l.split("\",\"");
 			Affymetrix_GenomeWide6.init(annotationPath);
 			String affyId = affy6Vals[Affymetrix_GenomeWide6.markerId].replace("\"", "");
 			String chr = affy6Vals[Affymetrix_GenomeWide6.chr].replace("\"", "");
 			String inFinalList = affy6Vals[Affymetrix_GenomeWide6.in_final_list].replace("\"", "");
 
 			if (!affyId.startsWith("AFFX-") && !inFinalList.equals("NO") && !chr.equals("---") && !snpBlackList.getAffyIdBlacklist().contains(affyId)) {
 				// chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
 				StringBuilder sbKey = new StringBuilder(chr); // 0 => chr
 				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pseudo_a1]); // 1 => pseudo-autosomal1
 				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pseudo_a2]); // 2 => pseudo-autosomal2
 				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pos]); // 3 => pos
 				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbKey.append(affyId); // 4 => markerId
 
 				// rsId;strand;alleles
 				StringBuilder sbVal = new StringBuilder(affy6Vals[Affymetrix_GenomeWide6.rsId]); // 0 => rsId
 				sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbVal.append(affy6Vals[Affymetrix_GenomeWide6.strand]); // 1 => strand
 				sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
 				sbVal.append(affy6Vals[Affymetrix_GenomeWide6.alleleA]).append(affy6Vals[Affymetrix_GenomeWide6.alleleB]); // 2 => alleles
 
 				sortedMetadataTM.put(sbKey.toString(), sbVal.toString());
 			}
 			count++;
 
 			if (count == 1) {
 				log.info(Text.All.processing);
 			} else if (count % 100000 == 0) {
 				log.info("Parsed annotation lines: {}", count);
 			}
 		}
 		log.info("Parsed annotation lines: {}", count);
 
 		inputAnnotationBr.close();
 
 		return sortedMetadataTM;
 	}
 
 	private static String[] fixRsId(String[] keyValues, String[] valValues) throws IOException {
 		// CHECK IF RSID EXISTS, USE AFFYID IF NOT
 
 		// valValues rsId;strand;alleles
 		// keyValues chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId
 		if (valValues[0].equals("---")) {
 			valValues[0] = keyValues[4];
 		}
 		return valValues;
 	}
 
 	private static String[] fixChrData(String[] keyValues) throws IOException {
 		// CHECK FOR PSEUDO-AUTOSOMAL FLAG => XY
 
 		//keyValues chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId
 		if (keyValues[1].equals("1")) {
 			keyValues[0] = "XY";
 		}
 		if (keyValues[2].equals("1")) {
 			keyValues[0] = "XY";
 		}
 		return keyValues;
 	}
 }
