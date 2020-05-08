 package org.geworkbench.bison.datastructure.biocollections.views;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 
 /**
  * 
  * Interface of the view of microarray dataset for a given marker subset and a given microarray subset.
  * 
  * Copyright: Copyright (c) 2003 -2004
  * 
  * Company: Columbia University
  * 
  * @author Adam Margolin
  * @version $Id$
  */
 public interface DSMicroarraySetView<T extends DSGeneMarker, Q extends DSMicroarray>
 		extends DSDataSetView<Q> {
 	
 	/**
 	 * Return the number of items.
 	 * 
 	 * This interface accesses two inter collections: a collection of (gene) markers and a collection of microarrays/items.
 	 * This method return the size of the latter. It should always return items().size, thus redundant.
 	 * 
 	 * @return
 	 */
 	public int size();
 	
 	/**
 	 * @return A DSItemList containing all the <code>Q</code> type objects
 	 *         (generally microarrays) associated with this
 	 *         <code>DSDataView</code>.
 	 */
 	public DSItemList<Q> items();
 	
 	/**
 	 * Assigns a specific item panel selection.
 	 */
 	public void setItemPanel(DSPanel<Q> mArrayPanel);
 
 	/**
 	 * Assigns a specific item panel selection.
 	 */
 	public DSPanel<Q> getItemPanel();
 
 	/**
 	 * Return the numeric value of a given row, namely the (gene) marker of a given index.
 	 */ 
     public double[] getRow(int index);
 
     public double getValue(int markerIndex, int arrayIndex);
 
 	public double getValue(T object, int arrayIndex);
 
 	public double getMeanValue(T marker, int maIndex);
 
 	public DSMicroarraySet getMicroarraySet();
 
 	/**
 	 * @return A DSItemList containing all the <code>T</code> type objects
 	 *         (generally markers) associated with this <code>DSDataView</code>.
 	 */
 	public DSItemList<T> markers();
 
 	/**
	 * Set/resets marker subselection based on activated panels.
	 * 
	 * @param status
	 */
	public void useMarkerPanel(boolean status);

	/**
 	 * Allows to assign a specific microarray panel selection
 	 * 
 	 * @param markerPanel
 	 *            DSPanel
 	 */
 	public void setMarkerPanel(DSPanel<T> markerPanel);
 
 	/**
 	 * Allows to retrieve the marker panel selection
 	 */
 	public DSPanel<T> getMarkerPanel();
 
 	public DSItemList<T> allMarkers();
 
 	public Q get(int index);
 
 	public DSItemList<T> getUniqueMarkers();
 }
