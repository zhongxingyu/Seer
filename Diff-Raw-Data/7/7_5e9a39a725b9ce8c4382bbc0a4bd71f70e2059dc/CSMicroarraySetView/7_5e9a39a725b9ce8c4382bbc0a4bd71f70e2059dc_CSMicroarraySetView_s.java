 package org.geworkbench.bison.datastructure.biocollections.views;
 
 import java.io.Serializable;
 
 import org.apache.commons.collections15.set.ListOrderedSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 
 /**
  * View of microarray dataset for a given marker subset and a given microarray
  * subset.
  * 
  * <p>
  * Copyright: Copyright (c) 2003 -2004
  * </p>
  * <p/>
  * <p>
  * Company: Columbia University
  * </p>
  * 
  * @author Adam Margolin
  * @version $Id$
  */
 public class CSMicroarraySetView<T extends DSGeneMarker, Q extends DSMicroarray>
 		implements DSMicroarraySetView<T, Q>, Serializable {
 
 	private static final long serialVersionUID = 1452815738190971373L;
 
 	final private DSMicroarraySet dataSet;
 
 	/**
 	 * Contains the active microarrays, organized as a DSPanel.
 	 */
 	private DSPanel<DSMicroarray> itemPanel = new CSPanel<DSMicroarray>("");
 
 	/**
 	 * Designates if the microarray subselection imposed by the activated
 	 * phenotypic categories is imposed on the this microarray set view.
 	 */
 	private boolean useItemPanel = false;
 
 	/**
 	 * Contains the active markers, organized as a DSPanel.
 	 */
 	private DSPanel<T> markerPanel = new CSPanel<T>("");
 
 	/**
 	 * Designates if the marker subselection imposed by the activated panels is
 	 * imposed on the this microarray set view.
 	 */
 	private boolean useMarkerPanel = false;
 
 	public CSMicroarraySetView(DSMicroarraySet dataSet) {
 		this.dataSet = dataSet;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSItemList<T> markers() {
 		if(dataSet==null)
 			return markerPanel;
 		
 		// todo Why is this size > 0 requirement here? Should probably be
 		// changed to return markerPanel if boolean is set no matter what
 		if (markerPanel != null && useMarkerPanel && markerPanel.size() > 0) {
 			return markerPanel;
 		} else {
 			// please note that in fact T is only allowed to be DSGeneMarker
 			// we should in fact get rid of these generics not properly
 			// designed/documented
 			return (DSItemList<T>) dataSet.getMarkers();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSItemList<T> getUniqueMarkers() {
		if (useMarkerPanel && markerPanel.size() > 0) {
 			ListOrderedSet<T> orderedSet = new ListOrderedSet<T>();
			for (T t : markerPanel) {
 				orderedSet.add(t);
 			}
 
 			CSItemList<T> itemList = new CSItemList<T>();
 			for (T item : orderedSet) {
 				itemList.add(item);
 			}
 
 			return itemList;
 		} else {
 			if (dataSet == null) {
 				return null;
 			} else {
 				return (DSItemList<T>) dataSet.getMarkers();
 			}
 		}
 	}
 
 	/**
 	 * Set/resets marker subselection based on activated panels.
 	 * 
 	 * @param status
 	 */
 	@Override
 	public void useMarkerPanel(boolean status) {
 		useMarkerPanel = status;
 	}
 
 	/**
 	 * Gets the status of marker activation
 	 * 
 	 * @return the status of marker activation
 	 */
 	@Override
 	public boolean useMarkerPanel() {
 		return useMarkerPanel;
 	}
 
 	@Override
 	public void setMarkerPanel(DSPanel<T> markerPanel) {
 		this.markerPanel = markerPanel;
 	}
 
 	@Override
 	public DSPanel<T> getMarkerPanel() {
 		return markerPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSItemList<T> allMarkers() {
 		if (dataSet instanceof DSMicroarraySet) {
 			return (DSItemList<T>) dataSet.getMarkers();
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public double getValue(int markerIndex, int arrayIndex) {
 		DSMicroarray ma = get(arrayIndex);
 		DSGeneMarker marker = markers().get(markerIndex);
 		return ma.getMarkerValue(marker).getValue();
 	}
 
 	@Override
 	public double getValue(T marker, int arrayIndex) {
 		DSMicroarray ma = get(arrayIndex);
 		return ma.getMarkerValue(marker.getSerial()).getValue();
 	}
 
 	@Override
 	public double getMeanValue(T marker, int arrayIndex) {
 		DSMicroarray ma = get(arrayIndex);
 		// This is a bit incorrect because it does not limit to only the
 		// selected markers
 		return getMicroarraySet().getMeanValue(marker, ma.getSerial());
 	}
 
 	@Override
 	public double[] getRow(int index) {
 		double[] rowVals = new double[this.size()];
 		for (int itemCtr = 0; itemCtr < rowVals.length; itemCtr++) {
 			rowVals[itemCtr] = getValue(index, itemCtr);
 		}
 		return rowVals;
 	}
 
 	@Override
 	public DSMicroarraySet getMicroarraySet() {
 		return (DSMicroarraySet) getDataSet();
 	}
 
 	@Override
 	public int size() {
 		return items().size();
 	}
 
 	/**
 	 * @return The microarray at the desiganted index position, if
 	 *         <code>index</code> is non-negative and no more than the total
 	 *         number of microarrays in the set. <code>null</code> otherwise.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSItemList<Q> items() {
 		if ((useItemPanel && (itemPanel != null) && (itemPanel.size() > 0))
 				|| dataSet == null) {
 			return (DSItemList<Q>) itemPanel;
 		} else {
 			// to change
 			return (DSItemList<Q>) dataSet;
 		}
 	}
 
 	/**
 	 * Set/reset microarray subselection based on activated phenotypes.
 	 * 
 	 * @param status
 	 */
 	@Override
 	public void useItemPanel(boolean status) {
 		useItemPanel = status;
 	}
 
 	/**
 	 * Gets the statuc of Phenotype Activation
 	 * 
 	 * @return
 	 */
 	@Override
 	public boolean useItemPanel() {
 		return useItemPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setItemPanel(DSPanel<Q> mArrayPanel) {
 		this.itemPanel = (DSPanel<DSMicroarray>) mArrayPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSPanel<Q> getItemPanel() {
 		return (DSPanel<Q>) itemPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DSDataSet<Q> getDataSet() {
 		return (DSDataSet<Q>) dataSet;
 	}
 
 	@Override
 	public Q get(int index) {
 		return items().get(index);
 	}
 
 }
