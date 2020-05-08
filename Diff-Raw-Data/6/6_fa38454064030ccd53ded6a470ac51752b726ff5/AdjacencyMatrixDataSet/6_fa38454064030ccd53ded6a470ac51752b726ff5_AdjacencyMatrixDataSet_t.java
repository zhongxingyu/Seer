 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import java.io.File;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author John Watkinson
  */
 public class AdjacencyMatrixDataSet extends CSAncillaryDataSet implements DSAncillaryDataSet {
 
     static Log log = LogFactory.getLog(AdjacencyMatrixDataSet.class);
 
     /**
      *
      */
     private static final long serialVersionUID = -6835973287728524201L;
     private AdjacencyMatrix matrix;
     private int geneId;
     private double threshold;
     private int depth;
     private String networkName;
 
     public AdjacencyMatrixDataSet(AdjacencyMatrix matrix, int geneId, double threshold, int depth, String name, String networkName, DSMicroarraySet parent) {
         super(parent, name);
         setID(RandomNumberGenerator.getID());
         this.matrix = matrix;
         this.geneId = geneId;
         this.threshold = threshold;
         this.depth = depth;
         this.networkName = networkName;
     }
 
     public void writeToFile(String fileName) {
         File file = new File(fileName);
 
         try {
             file.createNewFile();
             if (!file.canWrite()) {
                 JOptionPane.showMessageDialog(null, "Cannot write to specified file.");
                 return;
             }
             BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             DSMicroarraySet mset = matrix.getMicroarraySet();
             DSItemList<DSGeneMarker> markers = mset.getMarkers();
             HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix.getGeneRows();
             for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows.entrySet()) {
                 String geneName = markers.get(entry.getKey()).getLabel();
                writer.write(geneName + "\t");
                 HashMap<Integer, Float> destRows = entry.getValue();
                 for (Map.Entry<Integer, Float> entry2 : destRows.entrySet()) {
                     String geneName2 = markers.get(entry2.getKey()).getLabel();
                    writer.write(geneName2 + "\t" + entry2.getValue() + "\t");
                 }
                writer.write("\n");
             }
             writer.close();
         } catch (IOException e) {
             log.error(e);
         }
     }
 
     public AdjacencyMatrix getMatrix() {
         return matrix;
     }
 
     public void setMatrix(AdjacencyMatrix matrix) {
         this.matrix = matrix;
     }
 
     public int getGeneId() {
         return geneId;
     }
 
     public void setGeneId(int geneId) {
         this.geneId = geneId;
     }
 
     public double getThreshold() {
         return threshold;
     }
 
     public void setThreshold(double threshold) {
         this.threshold = threshold;
     }
 
     public int getDepth() {
         return depth;
     }
 
     public void setDepth(int depth) {
         this.depth = depth;
     }
 
     public File getDataSetFile() {
         // no-op
         return null;
     }
 
     public void setDataSetFile(File file) {
         // no-op
     }
 
     public String getNetworkName() {
         return networkName;
     }
 
     public void setNetworkName(String networkName) {
         this.networkName = networkName;
     }
 }
