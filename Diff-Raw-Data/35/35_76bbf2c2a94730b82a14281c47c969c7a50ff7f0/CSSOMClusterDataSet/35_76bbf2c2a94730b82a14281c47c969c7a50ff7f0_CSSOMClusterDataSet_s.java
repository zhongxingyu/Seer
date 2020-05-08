 package org.geworkbench.bison.model.clusters;
 
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSDataSetView;
 
 /**
  * @author John Watkinson
  */
 @SuppressWarnings("rawtypes")
 public class CSSOMClusterDataSet extends CSAncillaryDataSet implements DSSOMClusterDataSet {
 
 	private static final long serialVersionUID = -1517435442151993543L;
 	
 	private int rows, columns;
     private SOMCluster[][] clusters;
     private DSDataSetView parentSet;
 
     @SuppressWarnings("unchecked")
 	public CSSOMClusterDataSet(SOMCluster[][] clusters, String name, DSDataSetView dataSetView) {
         super(dataSetView.getDataSet(), name);
         rows = clusters.length;
         if (rows > 0) {
             columns = clusters[0].length;
         }
         this.clusters = clusters;
         this.parentSet = dataSetView;
     }
 
     public SOMCluster[][] getClusters() {
         return clusters;
     }
 
    public void writeToFile(String fileName) {
        // No-op
    }

     public DSDataSetView getDataSetView() {
         return parentSet;
     }
 
     public Cluster getCluster(int index) {
         if (columns == 0) {
             throw new IndexOutOfBoundsException("Invalid cluster index.");
         }
         int row = index / columns;
         int col = index % columns;
         return getCluster(row, col);
     }
 
     public int getRows() {
         return rows;
     }
 
     public int getColumns() {
         return columns;
     }
 
     public SOMCluster getCluster(int row, int column) {
         return clusters[row][column];
     }
 
     public int getNumberOfClusters() {
         return rows * columns;
     }
 
 }
