 package org.geworkbench.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.util.AffyAnnotationUtil;
 
 /**  
  * @author Nikhil
  */
 public class SampleFileParser {
 
 	static Log log = LogFactory.getLog(SOFTFileFormat.class);
 	private static final String commentSign1 = "#";
 	private static final String commentSign2 = "!";
 	private static final String commentSign3 = "^";
 
 	static final char ABSENT    = 'A';
     static final char PRESENT   = 'P';
     static final char MARGINAL  = 'M';
     static final char UNDEFINED = '\0';  
     char detectionStatus = UNDEFINED;
     
 	CSMicroarraySet maSet = new CSMicroarraySet();
 	private int possibleMarkers = 0; 
  	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
 	 */
 	public DSMicroarraySet getMArraySet(File file)
 			throws InputFileFormatException, InterruptedIOException {   
 		
 		BufferedReader in = null;
		final int extSeperater = '.';
 		String fileName = file.getName();
		int dotIndex = fileName.lastIndexOf(extSeperater);
		if (dotIndex != -1) {
			fileName = fileName.substring(0, dotIndex);
		}
 		maSet.setLabel(fileName);
 		String arrayName = null;
 		int m = 0;
 		int valueLabel = 0; 
 	 	int call = 0;
 	 	int detection = 0;
 		List<String> markers = new ArrayList<String>();
 		try {
 			in = new BufferedReader(new FileReader(file));
 			if(in != null){
 				try {
 					String tempName = null;
 					String header = null;
 					
 					while ((header = in.readLine()) != null) {
 						/*
 					 	* Adding comments to Experiment Information tab.
 					 	*We will ignore the line which start with '!platform_table_end', '!platform_table_begin', '!sample_table_begin'
 					 	*and '!sample_table_end'
 					 	*/
 						if (header.startsWith(commentSign1) || header.startsWith(commentSign2)) {
 							if(!header.equalsIgnoreCase("!sample_table_end") && !header.equalsIgnoreCase("!sample_table_begin")){
 								// to be consistent, this detailed information should be used else where instead of as "description" field
 								// maSet.setDescription(header.substring(1));
 							}
 						}	
 						String[] temp = null;
 						if(header.startsWith(commentSign3)){
 							temp = header.split("=");
 							String temP = temp[0].trim();
 							if(temP.equals("^SAMPLE")){
 								tempName = temp[1].trim();
 								header = in.readLine();
 								if(!header.startsWith(commentSign2)){
 									arrayName = tempName;
 								}
 							}
 						}
 						if(header.startsWith(commentSign2)){
 							temp = header.split("=");
 							String temP1 = temp[0].trim();
 							if(temP1.equals("!Sample_title")){
 								String temp1 = tempName
 												+ " ("
 												+temp[1].trim()
 												+")";
 								arrayName = temp1;
 							}
 						}
 						if(!header.startsWith(commentSign1) && !header.startsWith(commentSign2) && !header.startsWith(commentSign3)){
 							if(header.subSequence(0, 6).equals("ID_REF")){
 								String[] valueLabels = null;
 								valueLabels = header.split("\t");
 								for(int p=0; p < valueLabels.length; p++){
 									if(valueLabels[p].equals("VALUE")){
 										valueLabel = p;
 									}
 									if(valueLabels[p].equals("DETECTION_CALL") || valueLabels[p].equals("ABS_CALL")){
 										call = p;
 									}
 									if(valueLabels[p].equals("DETECTION P-VALUE") || valueLabels[p].equals("DETECTION_P")){
 										detection = p;
 									}
 								}
 							}
 							if(!header.subSequence(0, 6).equals("ID_REF")){
 								String[] mark = header.split("\t");
 								markers.add(mark[0]);
 								String markerName = new String(mark[0].trim());
 								CSExpressionMarker marker = new CSExpressionMarker(m);
 								marker.setLabel(markerName);
 								maSet.getMarkers().add(m, marker);
 								m++;	
 							}
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		//Getting the markers size to include in a loop
 		possibleMarkers = markers.size();
 		
 		int i = 0;
 		CSMicroarray array = new CSMicroarray(i, possibleMarkers,
 				arrayName,
 				DSMicroarraySet.affyTxtType);
 		maSet.add(array);
 		//This buffered reader is used to put insert marker values for one sample at a time from the Series file
 		boolean absCallFound = false;
 		boolean pValueFound = false;
 		BufferedReader out = null;
 		try {
 			int j = 0;
 			out = new BufferedReader(new FileReader(file));
 			try {
 				String line = out.readLine();
 				while (line != null) {
 					
 					if(!line.startsWith(commentSign1) && !line.startsWith(commentSign2) && !line.startsWith(commentSign3)){
 						if(!line.subSequence(0, 6).equals("ID_REF")){
 							String[] values = line.split("\t");
 							char C;
 							String ca = null;
 							String valString = null;
 							valString = values[valueLabel].trim();
 							if(valString == null){
 								Float v = Float.NaN;
 								CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(v);
 								DSMicroarray microarray = (DSMicroarray)maSet.get(0);
 								microarray.setMarkerValue(j, markerValue);
 								if (v.isNaN()) {
 									markerValue.setMissing(true);
 								} else {
 									markerValue.setPresent();
 								}
 								if(detection !=0 || call != 0) {
 									if(detection != 0){
 										String token = null;
 										token = values[detection].trim();
 										Object value = null;
 										value = Double.valueOf(token);
 										markerValue.setConfidence(( (Double) value).doubleValue());
 										pValueFound = true;
 									}
 									if(call != 0){
 										ca = values[call].trim();
 										C = ca.charAt(0);
 										char Call = Character.toUpperCase(C);
 										if (Call == PRESENT || Call == ABSENT || Call == MARGINAL){
 											this.detectionStatus = Call;
 											absCallFound = true;
 											if (!pValueFound){
 												switch (Call){
 													case PRESENT: 
 														markerValue.setPresent();
 														break;
 													case ABSENT:
 														markerValue.setAbsent();
 														break;
 													case MARGINAL:
 														markerValue.setMarginal();
 														break;
 												}
 											}
 										}
 									}
 									if (!absCallFound && !pValueFound){
 							    		  markerValue.setPresent();
 							    	}
 								}
 							}else { 
 								float value = Float.NaN;
 								try {
 									value = Float.parseFloat(valString);
 								} catch (NumberFormatException nfe) {
 								}
 								// put values directly into CSMicroarray inside of
 								// maSet
 								Float v = value;
 								CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
 										v);
 								DSMicroarray microarray = (DSMicroarray)maSet.get(0);
 								microarray.setMarkerValue(j, markerValue);
 								if (v.isNaN()) {
 									markerValue.setMissing(true);
 								} else {
 									markerValue.setPresent();
 								}
 								if(detection !=0 || call != 0){
 									if(detection != 0){
 										String token = null;
 										token = values[detection].trim();
 										Object value1 = null;
 										value1 = Double.valueOf(token);
 										markerValue.setConfidence(( (Double) value1).doubleValue());
 										pValueFound = true;
 									}
 									if(call != 0){
 										ca = values[call].trim();
 										C = ca.charAt(0);
 										char Call = Character.toUpperCase(C);
 										if (Call == PRESENT || Call == ABSENT || Call == MARGINAL){
 											this.detectionStatus = Call;
 											absCallFound = true;
 											if (!pValueFound){
 												switch (Call){
 												case PRESENT: 
 													markerValue.setPresent();
 													break;
 												case ABSENT:
 						    					  	markerValue.setAbsent();
 						    					  	break;
 												case MARGINAL:
 						    					  	markerValue.setMarginal();
 						    					  	break;
 												}
 											}
 										}
 									}
 									if (!absCallFound && !pValueFound){
 										markerValue.setPresent();
 									}
 								}
 							}
 							j++;
 						}
 					}
 					line = out.readLine();
 				}
 				String result = null;
 				for (int n = 0; n < possibleMarkers; n++) {
 					result = AffyAnnotationUtil.matchAffyAnnotationFile(maSet);
 					if (result != null) {
 						break;
 					}
 				}
 				if (result == null) {
 					AffyAnnotationUtil.matchAffyAnnotationFile(maSet);
 				} else {
 					maSet.setCompatibilityLabel(result);
 				}
 				for (DSGeneMarker marker : maSet.getMarkers()) {
 					String token = marker.getLabel();
 					String[] locusResult = AnnotationParser.getInfo(token,
 							AnnotationParser.LOCUSLINK);
 					String locus = "";
 					if ((locusResult != null)
 							&& (!locusResult[0].trim().equals(""))) {
 						locus = locusResult[0].trim();
 					}
 					if (locus.compareTo("") != 0) {
 						try {
 							marker.setGeneId(Integer.parseInt(locus));
 						} catch (NumberFormatException e) {
 							log.info("Couldn't parse locus id: " + locus);
 						}
 					}
 					String[] geneNames = AnnotationParser.getInfo(token,
 							AnnotationParser.ABREV);
 					if (geneNames != null) {
 						marker.setGeneName(geneNames[0]);
 					}
 
 					marker.getUnigene().set(token);
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return maSet;
 	}
 }
 
