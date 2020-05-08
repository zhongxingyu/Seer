 package org.geworkbench.components.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.ProgressMonitorInputStream;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.bison.parsers.resources.Resource;
 import org.geworkbench.components.parsers.microarray.DataSetFileFormat;
 
 /**
  * Sequence and Pattern Plugin
  * 
  * @author yc2480
  * @version $Id: RMAExpressFileFormat.java,v 1.16 2008/10/08 16:26:57 chiangy
  *          Exp $
  * 
  */
 public class RMAExpressFileFormat extends DataSetFileFormat {
 
 	static Log log = LogFactory.getLog(RMAExpressFileFormat.class);
 
 	private static final String commentSign1 = "#";
 	private static final String commentSign2 = "!";
 	private static final String columnSeperator = "\t";
 	private static final String lineSeperator = "\n";
 	private static final String[] maExtensions = { "txt", "tsv", "TSV" };
 	private static final String duplicateLabelModificator = "_2";
 	/* ex: test => test_2 */
 
 	ExpressionResource resource = new ExpressionResource();
 	RMAExpressFilter maFilter = null;
 	private int possibleMarkers = 0;
 
 	/**
 	 * 
 	 */
 	public RMAExpressFileFormat() {
 		formatName = "Tab-Delimited (RMAExpress, GEO series matrix etc)";
 		maFilter = new RMAExpressFilter();
 		Arrays.sort(maExtensions);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getResource(java.io.File)
 	 */
 	public Resource getResource(File file) {
 		try {
 			resource.setReader(new BufferedReader(new FileReader(file)));
 			resource.setInputFileName(file.getName());
 		} catch (IOException ioe) {
 			ioe.printStackTrace(System.err);
 		}
 		return resource;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getFileExtensions()
 	 */
 	public String[] getFileExtensions() {
 		return maExtensions;
 	}
 	
 	/*public boolean checkFormat(File file)
 	{
 		return true;
 	}*/
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#checkFormat(java.io.File)
 	 *      FIXME In here we should also check (among other things) that: The
 	 *      values of the data points respect their expected type. IMPORTANT!
 	 *      After Mantis #1551, this step will be required even if you don't
 	 *      want to checkFormat.
 	 */
 	public boolean checkFormat(File file) throws InterruptedIOException {
 		boolean columnsMatch = true;
 		boolean noDuplicateMarkers = true;
 		boolean noDuplicateArrays = true;
 		BufferedReader reader = null;
 		ProgressMonitorInputStream progressIn = null;
 		try {
 			FileInputStream fileIn = new FileInputStream(file);
 		    progressIn = new ProgressMonitorInputStream(
 					null, "Checking File Format", fileIn);
 			reader = new BufferedReader(new InputStreamReader(
 					progressIn));
 			 
 			String line = null;
 			int totalColumns = 0;
 			List<String> markers = new ArrayList<String>();
 			List<String> arrays = new ArrayList<String>();
 			int lineIndex = 0;
 			int headerLineIndex = 0; 
 			
 			while ((line = reader.readLine()) != null) { // for each line
 				if ((line.indexOf(commentSign1) < 0)
 						&& (line.indexOf(commentSign2) != 0)
 						&& (line.length() > 0)) {// we'll skip comments and
 					// anything before header
 					if (headerLineIndex == 0)// no header detected yet, then
 						// this is the header.
 						headerLineIndex = lineIndex;
 					String token = null;
 					int columnIndex = 0;
 					int accessionIndex = 0;
 					StringTokenizer st = new StringTokenizer(line,
 							columnSeperator + lineSeperator);
 					while (st.hasMoreTokens()) { // for each column
 						token = st.nextToken().trim();
 						if (token.equals("")) {// header
 							accessionIndex = columnIndex;
 						} else if ((headerLineIndex > 0) && (columnIndex == 0)) {
 							/*
 							 * if this line is after header, then first column
 							 * should be our marker name
 							 */
 							if (markers.contains(token)) {// duplicate markers
 								noDuplicateMarkers = false;
 								log.error("Duplicate Markers: "+token);
 								return false;
 							} else {
 								markers.add(token);
 							}
 						} else if (headerLineIndex == lineIndex) {
 							/*
 							 * this is header line for RMA file
 							 */
 							if (arrays.contains(token)) {// duplicate arrays
 								noDuplicateArrays = false;
 								log.error("Duplicate Arrays labels " + token
 										+ " in " + file.getName());
 								return false;
 							} else {
 								arrays.add(token);
 							}
 						}
 						columnIndex++;
						lineIndex++;
 					}
 					/* check if column match or not */
 					if (headerLineIndex > 0) {
 						/*
 						 * if this line is real data, we assume lines after
 						 * header are real data. (we might have bug here)
 						 */
 						if (totalColumns == 0) /* not been set yet */
 							totalColumns = columnIndex - accessionIndex;
 						else if (columnIndex != totalColumns)// if not equal
 							columnsMatch = false;
 					}
 				}
 			}
 			possibleMarkers = markers.size();
 			fileIn.close();
 		
 		} catch (java.io.InterruptedIOException ie) {
 			if ( progressIn.getProgressMonitor().isCanceled())
 			{			    
 				throw ie;				 
 			}			 
 			else
 			   ie.printStackTrace();
 		} catch (Exception e) {
 			 
 			e.printStackTrace();
 			 
 		} finally {
     		try {
 				reader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (columnsMatch && noDuplicateMarkers && noDuplicateArrays)
 			return true;
 		else
 			return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.microarray.DataSetFileFormat#getDataFile(java.io.File)
 	 */
 	@SuppressWarnings("unchecked")
 	public DSDataSet getDataFile(File file) throws InputFileFormatException, InterruptedIOException{
 		  
 		  return (DSDataSet) getMArraySet(file);
 	    
 	}
 	 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
 	 */
 	@SuppressWarnings("unchecked")
 	public DSMicroarraySet getMArraySet(File file)
 			throws InputFileFormatException, InterruptedIOException {
 
 		/* the sign between file name and extesion, ex: file.ext */
 		final int extSeperater = '.';
 
 		try
 		{
 		 if (!checkFormat(file)) {
 			log
 					.info("RMAExpressFileFormat::getMArraySet - "
 							+ "Attempting to open a file that does not comply with the "
 							+ "RMA express file format.");
 			 throw new InputFileFormatException(
 					"Attempting to open a file that does not comply with the "
 							+ "RMA express file format.");
 		 }
 		} catch (InterruptedIOException ie) {
 			throw ie;
 		}
 		CSExprMicroarraySet maSet = new CSExprMicroarraySet();
 		String fileName = file.getName();
 		int dotIndex = fileName.lastIndexOf(extSeperater);
 		if (dotIndex != -1) {
 			fileName = fileName.substring(0, dotIndex);
 		}
 		maSet.setLabel(fileName);
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(file));
 			if (in != null) {
 				String header = in.readLine();
 				if (header == null) {
 					throw new InputFileFormatException("File is empty.");
 				}
 				while (header != null
 						&& (header.startsWith(commentSign1) || header
 								.startsWith(commentSign2))
 						|| StringUtils.isEmpty(header)) {
 					header = in.readLine();
 				}
 				if (header == null) {
 					throw new InputFileFormatException(
 							"File is empty or consists of only comments.\n"
 									+ "RMAExpressFileFormat expected");
 				}
 
 				/* for mantis issue:1349 */
 				header = StringUtils.replace(header, "\"", "");
 
 				StringTokenizer headerTokenizer = new StringTokenizer(header,
 						columnSeperator, false);
 				int n = headerTokenizer.countTokens();
 				if (n <= 1) {
 					throw new InputFileFormatException(
 							"Attempting to open a file that does not comply with the RMA Express format.\n"
 									+ "Invalid header: " + header);
 				}
 				n -= 1;
 
 				String line = in.readLine();
 				line = StringUtils.replace(line, "\"", "");
 				int m = 0;
 
 				/* Skip first token */
 				headerTokenizer.nextToken();
 				int duplicateLabels = 0;
 
 				try {
 					for (int i = 0; i < n; i++) {
 						String arrayName = headerTokenizer.nextToken();
 						CSMicroarray array = new CSMicroarray(i,
 								possibleMarkers, arrayName, null, null, false,
 								DSMicroarraySet.affyTxtType);
 						maSet.add(array);
 						/*
 						 * FIXME: this will only fix one duplicate per unique
 						 * label. should handle unlimited duplicate.
 						 */
 						if (maSet.size() != (i + 1)) {
 							log.info("We got a duplicate label of array");
 							array.setLabel(array.getLabel()
 									+ duplicateLabelModificator);
 							maSet.add(array);
 							duplicateLabels++;
 						}
 					}
 				} catch (OutOfMemoryError e) {
 					log.error(e);
 					throw new InputFileFormatException(
 							"Attempting to open a file that is larger than allocated memory can handle.");
 				}
 				while ((line != null) // modified for mantis issue: 1349
 						&& (!StringUtils.isEmpty(line))
 						&& (!line.trim().startsWith(commentSign2))) {
 					String[] tokens = line.split(columnSeperator);
 					int length = tokens.length;
 					if (length != (n + 1)) {
 						log.error("Warning: Could not parse line #" + (m + 1)
 								+ ". Line should have " + (n + 1)
 								+ " lines, has " + length + ".");
 						if ((m == 0) && (length == n + 2))
 							// TODO Is this file from R's RMA, without first
 							// column in header?
 							throw new InputFileFormatException(
 									"Attempting to open a file that does not comply with the "
 											+ "RMA Express format."
 											+ "\n"
 											+ "Warning: Could not parse line #"
 											+ (m + 1)
 											+ ". Line should have "
 											+ (n + 1)
 											+ " columns, but it has "
 											+ length
 											+ ".\n"
 											+ "This file looks like R's RMA format, which needs manually add a tab in the beginning of the header to make it a valid RMA format.");
 						else
 							throw new InputFileFormatException(
 									"Attempting to open a file that does not comply with the "
 											+ "RMA Express format." + "\n"
 											+ "Warning: Could not parse line #"
 											+ (m + 1) + ". Line should have "
 											+ (n + 1) + " columns, but it has "
 											+ length + ".");
 					}
 					String markerName = new String(tokens[0].trim());
 					CSExpressionMarker marker = new CSExpressionMarker(m);
 					marker.setLabel(markerName);
 					maSet.getMarkerVector().add(m, marker);
 					for (int i = 0; i < n; i++) {
 						String valString = "";
 						if ((i + 1) < tokens.length) {
 							valString = tokens[i + 1];
 						}
 						if (valString.trim().length() == 0) {
 							// put values directly into CSMicroarray inside of
 							// maSet
 							Float v = Float.NaN;
 							CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
 									v);
 							maSet.get(i).setMarkerValue(m, markerValue);
 							if (v.isNaN()) {
 								markerValue.setMissing(true);
 							} else {
 								markerValue.setPresent();
 							}
 						} else {
 							float value = Float.NaN;
 							try {
 								value = Float.parseFloat(valString);
 							} catch (NumberFormatException nfe) {
 								log.error("We expect a number, but we got: "
 										+ valString);
 							}
 							// put values directly into CSMicroarray inside of
 							// maSet
 							Float v = value;
 							CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
 									v);
 							try {
 								maSet.get(i).setMarkerValue(m, markerValue);
 							} catch (IndexOutOfBoundsException ioobe) {
 								log.error("i=" + i + ", m=" + m);
 							}
 							if (v.isNaN()) {
 								markerValue.setMissing(true);
 							} else {
 								markerValue.setPresent();
 							}
 						}
 					}
 					m++;
 					line = in.readLine();
 					line = StringUtils.replace(line, "\"", "");
 				}
 				// Set chip-type
 				String result = null;
 				for (int i = 0; i < m; i++) {
 					result = AnnotationParser.matchChipType(maSet, maSet
 							.getMarkerVector().get(i).getLabel(), false);
 					if (result != null) {
 						break;
 					}
 				}
 				if (result == null) {
 					AnnotationParser.matchChipType(maSet, "Unknown", true);
 				} else {
 					maSet.setCompatibilityLabel(result);
 				}
 				for (DSGeneMarker marker : maSet.getMarkerVector()) {
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
 			}
 		} catch (InputFileFormatException e) {
 			throw e;
 		} catch (InterruptedIOException ie) {
 			throw ie;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
     		try {
 				in.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return maSet;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List getOptions() {
 		// TODO Implement this org.geworkbench.components.parsers.FileFormat
 		// abstract method
 		throw new UnsupportedOperationException(
 				"Method getOptions() not yet implemented.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getFileFilter()
 	 */
 	public FileFilter getFileFilter() {
 		return maFilter;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.microarray.DataSetFileFormat#getDataFile(java.io.File[])
 	 */
 	@SuppressWarnings("unchecked")
 	public DSDataSet getDataFile(File[] files) {
 		// TODO Implement this
 		// org.geworkbench.components.parsers.microarray.DataSetFileFormat
 		// abstract method
 		throw new UnsupportedOperationException(
 				"Method getDataFile(File[] files) not yet implemented.");
 	}
 
 	/**
 	 * Defines a <code>FileFilter</code> to be used when the user is prompted
 	 * to select Affymetrix input files. The filter will only display files
 	 * whose extension belongs to the list of file extension defined in {@link
 	 * #affyExtensions}.
 	 * 
 	 * @author yc2480
 	 * 
 	 */
 	class RMAExpressFilter extends FileFilter {
 
 		public String getDescription() {
 			return getFormatName();
 		}
 
 		public boolean accept(File f) {
 			boolean returnVal = false;
 			for (int i = 0; i < maExtensions.length; ++i)
 				if (f.isDirectory() || f.getName().endsWith(maExtensions[i])) {
 					return true;
 				}
 			return returnVal;
 		}
 	}
 }
