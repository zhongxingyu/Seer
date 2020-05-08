 package org.geworkbench.components.parsers;
 
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
 
 import javax.swing.filechooser.FileFilter;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @version 1.0
 * @author xuegong wang
  */
 
 public class RMAExpressFileFormat extends DataSetFileFormat {
 
     String[] maExtensions = {"txt"};
     ExpressionResource resource = new ExpressionResource();
     RMAExpressFilter maFilter = null;
 
     public RMAExpressFileFormat() {
         formatName = "RMA Express, Tab-Delimited";
         maFilter = new RMAExpressFilter();
         Arrays.sort(maExtensions);
     }
 
     public Resource getResource(File file) {
         try {
             resource.setReader(new BufferedReader(new FileReader(file)));
             resource.setInputFileName(file.getName());
         } catch (IOException ioe) {
             ioe.printStackTrace(System.err);
         }
         return resource;
     }
 
     public String[] getFileExtensions() {
         return maExtensions;
     }
 
     public boolean checkFormat(File file) {
         return true;
     }
 
     public DSDataSet getDataFile(File file) {
         return (DSDataSet) getMArraySet(file);
     }
 
     public DSMicroarraySet getMArraySet(File file) {
         CSExprMicroarraySet maSet = new CSExprMicroarraySet();
         String fileName = file.getName();
         int dotIndex = fileName.lastIndexOf('.');
         if (dotIndex != -1) {
             fileName = fileName.substring(0, dotIndex);
         }
         maSet.setLabel(fileName);
         try {
             BufferedReader in = new BufferedReader(new FileReader(file));
             if (in != null) {
                 String header = in.readLine();
                 if (header == null) {
                     throw new Exception("File is empty.");
                 }
                 while (header != null && header.startsWith("#")) {
                     header = in.readLine();
                 }
                 if (header == null) {
                     throw new Exception("File is empty or consists of only comments.");
                 }
                 StringTokenizer headerTokenizer = new StringTokenizer(header, "\t", false);
                 int n = headerTokenizer.countTokens();
                 if (n <= 1) {
                     throw new Exception("Invalid header: " + header);
                 }
                 n -= 1;
                 ArrayList<Float>[] values = new ArrayList[n];
                 for (int i = 0; i < n; i++) {
                     values[i] = new ArrayList<Float>();
                 }
                 String line = in.readLine();
                 int m = 0;
                 while (line != null) {
                     String[] tokens = line.split("\t");
                     int length = tokens.length;
                     if (length != (n + 1)) {
                         System.out.println("Warning: Could not parse line #" + (m + 1) + ". Line should have " + (n+1) + " lines, has " + length + ".");
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
                             values[i].add(Float.NaN);
                         } else {
                             float value = Float.NaN;
                             try {
                                 value = Float.parseFloat(valString);
                             } catch (NumberFormatException nfe) {
 
                             }
                             values[i].add(value);
                         }
                     }
                     m++;
                     line = in.readLine();
                 }
                 // Skip first token
                 headerTokenizer.nextToken();
                 for (int i = 0; i < n; i++) {
                     String arrayName = headerTokenizer.nextToken();
                     CSMicroarray array = new CSMicroarray(i, m, arrayName, null, null, false, DSMicroarraySet.affyTxtType);
                     maSet.add(array);
                     for (int j = 0; j < m; j++) {
                         Float v = values[i].get(j);
                         CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(v);
                         if (v.isNaN()) {
                             markerValue.setMissing(true);
                         } else {
                             markerValue.setPresent();
                         }
                         array.setMarkerValue(j, markerValue);
                     }
                 }
                 // Set chip-type
                 String result = null;
                 for (int i = 0; i < m; i++) {
                     result = AnnotationParser.matchChipType(maSet, maSet.getMarkerVector().get(i).getLabel(), false);
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
                     String[] locusResult = AnnotationParser.getInfo(token, AnnotationParser.LOCUSLINK);
                     String locus = " ";
                     if ((locusResult != null) && (!locusResult[0].equals(""))) {
                         locus = locusResult[0];
                     }
                     if (locus.compareTo(" ") != 0) {
                         marker.setGeneId(Integer.parseInt(locus));
                     }
                     String[] geneNames = AnnotationParser.getInfo(token, AnnotationParser.ABREV);
                     if (geneNames != null) {
                         marker.setGeneName(geneNames[0]);
                     }
 
                     marker.getUnigene().set(token);
 
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return maSet;
     }
 
     public List getOptions() {
        // todo Implement this org.geworkbench.components.parsers.FileFormat abstract method
         throw new UnsupportedOperationException("Method getOptions() not yet implemented.");
     }
 
     public FileFilter getFileFilter() {
         return maFilter;
     }
 
     /**
      * getDataFile
      *
      * @param files File[]
      * @return DataSet
      */
     public DSDataSet getDataFile(File[] files) {
         return null;
     }
 
     /**
      * Defines a <code>FileFilter</code> to be used when the user is prompted
      * to select Affymetrix input files. The filter will only display files
      * whose extension belongs to the list of file extension defined in {@link
      * #affyExtensions}.
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
