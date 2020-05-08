 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.inspector;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.SDAPlotter;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSet3DPlot2DMulti;
 import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetTableView;
 import uk.ac.diamond.scisoft.analysis.rcp.views.ImageExplorerView;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * All inspection tabs obey this interface
  */
 interface InspectionTab {
 	/**
 	 * @return string to be used for title of tab 
 	 */
 	public String getTabTitle();
 
 	/**
 	 * Create the composite for tab
 	 * @param parent
 	 * @return composite
 	 */
 	public Composite createTabComposite(Composite parent);
 
 	/**
 	 * Set the dataset and axis selection for tab
 	 * @param data
 	 * @param datasetAxisList
 	 * @param plotAxislist 
 	 */
 	public void setParameters(ILazyDataset data, List<AxisSelection> datasetAxisList, List<PlotAxisProperty> plotAxislist);
 
 	/**
 	 * Show slice of dataset using tab configuration
 	 * @param monitor
 	 * @param slices
 	 */
 	public void pushToView(IMonitor monitor, List<SliceProperty> slices);
 
 	/**
 	 * @return true if tab can plot constant in place of dataset
 	 */
 	public boolean canPlotConstant();
 
 	/**
 	 * Check whether data has sufficient rank for tab
 	 * @param data
 	 * @return true if data is compatible with tab
 	 */
 	public boolean checkCompatible(ILazyDataset data);
 
 	/**
 	 * Draw tab
 	 */
 	public void drawTab();
 
 	/**
 	 * @return number of axes (used in inspection)
 	 */
 	public int getNumAxes();
 
 	/**
 	 * @return inspector type of tab
 	 */
 	public InspectorType getType();
 
 	/**
 	 * @return boolean array of which dimensions are used
 	 */
 	public boolean[] getUsedDims();
 
 	/**
 	 * Stop inspection process in tab (for long running processes)
 	 */
 	public void stopInspection();
 
 }
 
 /**
  * Abstract base class
  */
 abstract class ATab implements InspectionTab {
 	protected static final Logger logger = LoggerFactory.getLogger(ATab.class);
 	protected static final String PLOTNAME = "Dataset Plot";
 
 	protected String text;
 	protected String[] axes;
 	protected List<Combo> combos;
 	protected List<AxisSelection> daxes = null;
 	protected List<PlotAxisProperty> paxes = null;
 	protected Composite composite;
 	protected InspectorType itype;
 	protected IWorkbenchPartSite site;
 	protected ILazyDataset dataset;
 	protected int comboOffset = 0;
 
 	public ATab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
 		site = partSite;
 		itype = type;
 		text = title;
 		axes = axisNames;
 	}
 
 	@Override
 	public void setParameters(ILazyDataset data, List<AxisSelection> datasetAxisList, List<PlotAxisProperty> plotAxisList) {
 		dataset = data;
 		daxes = datasetAxisList;
 		paxes  = plotAxisList;
 	}
 
 	@Override
 	final public String getTabTitle() {
 		return text;
 	}
 
 	@Override
 	final public InspectorType getType() {
 		return itype;
 	}
 
 	@Override
 	public boolean checkCompatible(ILazyDataset data) {
 		boolean isCompatible = data.getRank() >= axes.length;
 		if (composite != null)
 			composite.setEnabled(isCompatible);
 		return isCompatible;
 	}
 
 	@Override
 	final public int getNumAxes() {
 		if (axes == null)
 			return 0;
 		return axes.length;
 	}
 
 	@Override
 	public boolean canPlotConstant() {
 		return false;
 	}
 
 }
 
 /**
  * Straightforward plotting tabs
  */
 class PlotTab extends ATab {
 	private static final String VOLVIEWNAME = "Remote Volume Viewer";
 	private String explorerName;
 	// this is the current limit on the number of lines that stack can handle well
 	private static final int STACKPLOTLIMIT = 100;
 	private static final int MULTIIMAGESLIMIT = DataSet3DPlot2DMulti.MAX_IMAGES;
 
 	private PropertyChangeListener axesListener = null;
 	private ImageExplorerView explorer = null;
 	protected boolean runLongJob = false;
 	private boolean plotStackIn3D = false;
 
 	public PlotTab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
 		super(partSite, type, title, axisNames);
 	}
 
 	@Override
 	public Composite createTabComposite(Composite parent) {
 		ScrolledComposite sComposite = new ScrolledComposite(parent, SWT.VERTICAL | SWT.HORIZONTAL);
 		Composite holder = new Composite(sComposite, SWT.NONE);
 		holder.setLayout(new GridLayout(2, false));
 
 		combos = new ArrayList<Combo>();
 
 		SelectionAdapter listener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Combo c = (Combo) e.widget;
 				int i = combos.indexOf(c);
 				if (i >= 0 && paxes != null) {
 					PlotAxisProperty p = paxes.get(i);
 					String item = c.getItem(c.getSelectionIndex()); 
 					if (item.equals(p.getName()))
 						return;
 					p.setName(item, false);
 					repopulateCombos(null, null);
 				}
 			}
 		};
 		createCombos(holder, listener);
 
 		if (daxes != null)
 			populateCombos();
 
 		if (itype == InspectorType.LINESTACK) {
 			final Button b = new Button(holder, SWT.CHECK);
 			b.setText("In 3D");
 			b.setToolTipText("Check to plot stack of lines in 3D");
 			b.setSelection(plotStackIn3D);
 			b.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					plotStackIn3D = b.getSelection();
 
 					if (paxes != null) { // signal a replot without a slice reset
 						PlotAxisProperty p = paxes.get(0);
 						p.fire(new PropertyChangeEvent(p, PlotAxisProperty.plotUpdate, p.getName(), p.getName()));
 					}
 				}
 			});
 		}
 
 		holder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		sComposite.setContent(holder);
 		holder.setSize(holder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 		composite = sComposite;
 		return composite;
 	}
 
 	protected void createCombos(Composite cHolder, SelectionListener listener) {
 		for (int i = 0; i < axes.length; i++) { // create combo box for each axis
 			new Label(cHolder, SWT.NONE).setText(axes[i]);
 			Combo c = new Combo(cHolder, SWT.READ_ONLY);
 			c.add("               ");
 			c.addSelectionListener(listener);
 			combos.add(c);
 		}
 	}
 
 	@Override
 	public void drawTab() {
 		repopulateCombos(null, null);
 	}
 
 	@Override
 	public synchronized void stopInspection() {
 		runLongJob = false;
 	}
 
 	private synchronized void setInspectionRunning() {
 		runLongJob = true;
 	}
 
 	private synchronized boolean canContinueInspection() {
 		return runLongJob;
 	}
 
 	@Override
 	public void setParameters(ILazyDataset data, List<AxisSelection> datasetAxisList, List<PlotAxisProperty> plotAxisList) {
 		if (axesListener != null && daxes != null) {
 			for (AxisSelection a : daxes) {
 				a.removePropertyChangeListener(axesListener);
 			}
 		}
 		super.setParameters(data, datasetAxisList, plotAxisList);
 		if (daxes != null) {
 			if (axesListener == null) {
 				axesListener = new PropertyChangeListener() {
 					@Override
 					public void propertyChange(PropertyChangeEvent evt) {
 						repopulateCombos(evt.getOldValue().toString(), evt.getNewValue().toString());
 					}
 				};
 			}
 			for (AxisSelection a : daxes) {
 				a.addPropertyChangeListener(axesListener);
 			}
 		}
 
 		if (combos != null)
 			populateCombos();
 	}
 
 	/**
 	 * @return list of axis datasets
 	 */
 	protected List<AxisChoice> getChosenAxes() {
 		List<String> names = getChosenAxisNames();
 		List<AxisChoice> list = new ArrayList<AxisChoice>();
 
 		for (AxisSelection s : daxes) {
 			AxisChoice a = null;
 			String sname = s.getSelectedName();
 			if (names.indexOf(sname) != -1) {
 				a = s.getAxis(sname);
 				names.remove(sname);
 			}
 			if (a != null) {
 				list.add(a);
 			} else {
 //				logger.warn("No axis of names {} found in selection {}", names, s);
 				list.add(s.getSelectedAxis());
 			}
 		}
 		return list;
 	}
 
 	final protected LinkedList<String> getChosenAxisNames() { // get all chosen axis names from combo boxes
 		LinkedList<String> pAxes = new LinkedList<String>();
 		if (paxes != null) {
 			for (PlotAxisProperty p : paxes) {
 				if (!p.isInSet())
 					continue;
 
 				String n = p.getName();
 				if (n != null) {
 					pAxes.add(n);
 				} else {
 					logger.error("No axis selected in {}", p);
 				}
 			}
 		}
 
 		return pAxes;
 	}
 
 	final protected LinkedList<String> getSelectedAxisNames() { // get all selected axes
 		LinkedList<String> sAxes = new LinkedList<String>();
 		if (daxes != null) {
 			for (AxisSelection s : daxes) {
 				String n = s.getSelectedName();
 				if (n != null) {
 					sAxes.add(n);
 				} else {
 					logger.error("No axis selected in {}", s);
 				}
 			}
 		}
 
 		return sAxes;
 	}
 
 	// get selected axes to add into combos
 	final protected HashMap<Integer, String> getSelectedComboAxisNames() {
 		HashMap<Integer, String> sAxes = new HashMap<Integer, String>();
 		if (daxes != null) {
 			for (int i = 0, imax = daxes.size(); i < imax; i++) {
 				AxisSelection s = daxes.get(i);
 				String n = s.getSelectedName();
 				if (n != null) {
 					sAxes.put(i, n);
 				} else {
 					logger.warn("No axis selection available in {}", s);
 				}
 			}
 		}
 		
 		return sAxes;
 	}
 	
 	@Override
 	public boolean[] getUsedDims() {
 		List<String> sAxes = getSelectedAxisNames();
 		List<String> cAxes = getChosenAxisNames();
 		HashSet<String> chosenAxes = new HashSet<String>();
 		int cSize = combos.size() - comboOffset;
 		for (int i = 0; i < cSize; i++) {
 			PlotAxisProperty p = paxes.get(i + comboOffset);
 			chosenAxes.add(p.getName());
 		}
 
 		if (chosenAxes.size() != cAxes.size()) {
 			logger.debug("Chosen sets are unequal in size!");
 		}
 		for (String s : cAxes) {
 			if (!chosenAxes.contains(s)) {
 				logger.debug("Chosen set does not contain " + s);				
 			}
 		}
 
 		boolean[] used = new boolean[sAxes.size()];
 		for (int i = 0, imax = sAxes.size(); i < imax; i++) {
 			ILazyDataset selectedAxis = daxes.get(i).getSelectedAxis().getValues();
 			if (selectedAxis == null) {
 				continue;
 			}
 			used[i] = chosenAxes.contains(sAxes.get(i));
 		}
 		return used;
 	}
 
 	final public int[] getOrder(int rank) {
 		if (rank == 1)
 			return new int[] { 0 };
 	
 		LinkedList<Integer> orders = new LinkedList<Integer>();
 		for (int i = 0; i < rank; i++)
 			orders.add(i);
 
 		int[] cOrder = new int[rank];
 		int i = 0;
 		for (PlotAxisProperty p : paxes) {
 			if (!p.isInSet())
 				continue;
 
 			int d = p.getDimension();
 			cOrder[i++] = d;
 			orders.remove((Integer) d) ;
 		}
 
 		for (; i < rank; i++) {
 			int d = orders.removeFirst();
 			cOrder[i] = d;
 		}
 		return cOrder;
 	}
 
 	protected void populateCombos() {
 		int cSize = combos.size() - comboOffset;
 		HashMap<Integer, String> sAxes = getSelectedComboAxisNames();
 
 		for (int i = 0; i < cSize; i++) {
 			Combo c = combos.get(i + comboOffset);
 			c.removeAll();
 
 			ArrayList<Integer> keyList = new ArrayList<Integer>(sAxes.keySet());
 			Collections.sort(keyList);
 			Integer lastKey = keyList.get(keyList.size() - 1);
 			String a = sAxes.get(lastKey); // reverse order
 			PlotAxisProperty p = paxes.get(i + comboOffset);
 			p.clear();
 
 			if (axes.length == 1) { // for 1D plots and 1D dataset table, remove single point axes
 				int[] shape = dataset.getShape();
 				while (shape[lastKey] == 1) {
 					lastKey--;
 				}
 				a = sAxes.get(lastKey); // reverse order
 				for (int j : keyList) {
 					String n = sAxes.get(j);
 					p.put(j, n);
 					if (shape[j] != 1)
 						c.add(n);
 				}
 			} else {
 				for (int j : keyList) {
 					String n = sAxes.get(j);
 					p.put(j, n);
 					c.add(n);
 				}
 			}
 			c.setText(a);
 			sAxes.remove(lastKey);
 			p.setName(a, false);
 			p.setInSet(true);
 		}
 	}
 
 	protected void repopulateCombos(String oldName, String newName) {
 		if (combos == null)
 			return;
 
 		// cascade through plot axes strings and indices
 		// reduce choice each time
 		HashMap<Integer, String> sAxes = getSelectedComboAxisNames();
 		if (sAxes.size() == 0)
 			return;
 		int cSize = combos.size() - comboOffset;
 		int dmax = daxes.size();
 		String a = null;
 		if (oldName != null && newName != null) { // only one dataset axis has changed
 			LinkedList<String> oAxes = paxes.get(comboOffset).getNames(); // old axes
 			if (dmax != oAxes.size()) {
 				logger.error("First axis combo has less choice than rank of dataset");
 				return;
 			}
 			// find changed dimension
 			LinkedList<String> cAxes = getChosenAxisNames(); // old choices
 			Map<String, Integer> axesMap = new LinkedHashMap<String, Integer>();
 			Map<String, Integer> oldMap = paxes.get(comboOffset).getValue().getMap();
 			for (String n : oldMap.keySet()) {
 				axesMap.put(n.equals(oldName) ? newName : n, oldMap.get(n));
 			}
 			PlotAxisProperty p = null;
 			for (int i = 0; i < cSize; i ++) {
 				Combo c = combos.get(i + comboOffset);
 				c.removeAll();
 				p = paxes.get(i + comboOffset);
 				p.clear();
 
 				String curAxis = cAxes.get(i);
 				a = oldName.equals(curAxis) ? newName : curAxis;
 
 				if (axes.length == 1) { // for 1D plots and 1D dataset table
 					int[] shape = dataset.getShape();
 					for (String n : axesMap.keySet()) {
 						Integer j = axesMap.get(n);
 						p.put(j, n);
 						if (shape[j] != 1)
 							c.add(n);
 					}
 				} else {
 					for (String n : axesMap.keySet()) {
 						Integer j = axesMap.get(n);
 						p.put(j, n);
 						c.add(n);
 					}
 				}
 				c.setText(a);
 				axesMap.remove(a);
 				p.setName(a, false);
 				p.setInSet(true);
 			}
 			// do not need to notify plot axes listeners
 			if (a != null && p != null) {
 				if (p.isInSet()) {
 					p.setName(a);
 				}
 			}
 			return;
 		}
 
 		PlotAxisProperty p = paxes.get(comboOffset);
 		Map<String, Integer> axesMap = new LinkedHashMap<String, Integer>(p.getValue().getMap());
 		a = p.getName();
 		axesMap.remove(a);
 		for (int i = 1; i < cSize; i++) {
 			Combo c = combos.get(i + comboOffset);
 			p = paxes.get(i + comboOffset);
 			a = p.getName();
 			c.removeAll();
 			if (!axesMap.containsKey(a)) {
 				a = axesMap.keySet().iterator().next(); // attempt to get a valid name
 			}
 			p.clear();
 
 			if (axes.length == 1) { // for 1D plots and 1D dataset table
 				int[] shape = dataset.getShape();
 				for (String n : axesMap.keySet()) {
 					Integer j = axesMap.get(n);
 					p.put(j, n);
 					if (shape[j] != 1)
 						c.add(n);
 				}
 			} else {
 				for (String n : axesMap.keySet()) {
 					Integer j = axesMap.get(n);
 					p.put(j, n);
 					c.add(n);
 				}
 			}
 
 			c.setText(a);
 			axesMap.remove(a);
 			p.setName(a, false);
 		}
 		if (a != null) {
 			if (p.isInSet()) {
 				p.setName(a);
 			}
 		}
 	}
 
 	protected List<AbstractDataset> sliceAxes(List<AxisChoice> axes, Slice[] slices, int[] order) {
 		List<AbstractDataset> slicedAxes = new ArrayList<AbstractDataset>();
 
 		boolean[] used = getUsedDims();
 		for (int o : order) {
 			if (used[o]) {
 				AxisChoice c = axes.get(o);
 				int[] imap = c.getIndexMapping();
 
 				// We need to reorder multidimensional axis values to match reorder data  
 				int[] reorderAxesDims = new int[imap.length];
				for (int i = 0, j = 0; i < order.length && j < imap.length; i++) {
 					int idx = ArrayUtils.indexOf(imap, order[i]);
 					if (idx != ArrayUtils.INDEX_NOT_FOUND)
 						reorderAxesDims[j++] = idx;
 				}
 
 				ILazyDataset axesData = c.getValues();
 				Slice[] s = new Slice[imap.length];
 				for (int i = 0; i < s.length; i++)
 					s[i] = slices[imap[i]];
 
 				AbstractDataset slicedAxis = DatasetUtils.convertToAbstractDataset(axesData.getSlice(s));
 
 				AbstractDataset reorderdAxesData = DatasetUtils.transpose(slicedAxis, reorderAxesDims);
 //				reorderdAxesData.setName(axesData.getName());
 
 				reorderdAxesData.setName(c.getLongName());
 
 				slicedAxes.add(reorderdAxesData.squeeze());
 			}
 		}
 
 		return slicedAxes;
 	}
 
 	protected AbstractDataset sliceData(IMonitor monitor, Slice[] slices) {
 		AbstractDataset slicedData = null;
 		try {
 			slicedData = DatasetUtils.convertToAbstractDataset(dataset.getSlice(monitor, slices));
 		} catch (Exception e) {
 			logger.error("Problem getting slice of data: {}", e);
 			logger.error("Tried to get slices: {}", Arrays.toString(slices));
 		}
 		return slicedData;
 	}
 
 	protected AbstractDataset slicedAndReorderData(IMonitor monitor, Slice[] slices, int[] order) {
 		AbstractDataset reorderedData = null;
 		AbstractDataset slicedData = sliceData(monitor, slices);
 		if (slicedData == null)
 			return null;
 		reorderedData = DatasetUtils.transpose(slicedData, order);
 
 		reorderedData.setName(slicedData.getName());
 		reorderedData.squeeze();
 		if (reorderedData.getSize() < 1)
 			return null;
 
 		return reorderedData;
 	}
 
 	protected void swapFirstTwoInOrder(int[] order) {
 		if (order.length > 1) {
 			final int t = order[0];
 			order[0] = order[1];
 			order[1] = t;
 		}
 	}
 
 	private AbstractDataset make1DAxisSlice(List<AbstractDataset> slicedAxes, int dim) {
 		AbstractDataset axisSlice = slicedAxes.get(dim);
 		
 		// 2D plots can only handle 1D axis.
 		if (axisSlice.getRank() > 1) {
 			int rank = axisSlice.getRank();
 			Slice[] sl = new Slice[rank];
 			for (int idx = 0; idx < rank; idx++)
 				if (idx != dim)
 					sl[idx] = new Slice(0,1,1);
 				else 
 					sl[idx] = new Slice();
 			
 			logger.warn("2D plots can only handle 1D axis. Taking first slice from {} dataset", axisSlice.getName());
 			AbstractDataset d = axisSlice.getSlice(sl).squeeze();
 			if (d.getRank() == 0) {
 				d.setShape(1);
 			}
 			return d;
 		}
 
 		if (axisSlice.getRank() == 0)
 			axisSlice.setShape(1);
 
 		return axisSlice;
 	}
 
 	/**
 	 * Check rank of dataset and correct if necessary
 	 * @param a
 	 * @param rank
 	 * @return true if something wrong
 	 */
 	private boolean isRankBad(AbstractDataset a, int rank) {
 		if (a == null)
 			return true;
 		int r = a.getRank();
 		if (r > rank)
 			return true;
 		if (r == rank)
 			return false;
 		int[] s = Arrays.copyOf(a.getShape(), rank);
 
 		for (; r < rank; r++) {
 			s[r] = 1;
 		}
 		a.setShape(s);
 		return false;
 	}
 
 	@Override
 	public void pushToView(IMonitor monitor, List<SliceProperty> sliceProperties) {
 		if (dataset == null)
 			return;
 
 		Slice[] slices = new Slice[sliceProperties.size()];
 		for (int i = 0; i < slices.length; i++) {
 			slices[i] = sliceProperties.get(i).getValue();
 		}
 
 		int[] order = getOrder(daxes.size());
 		// FIXME: Image, surface and volume plots can't work with multidimensional axis data
 		List<AbstractDataset> slicedAxes = sliceAxes(getChosenAxes(), slices, order);
 
 		if (itype == InspectorType.IMAGE || itype == InspectorType.SURFACE || itype == InspectorType.IMAGEXP  || itype == InspectorType.MULTIIMAGES) {
 			// note that the DataSet plotter's 2D image/surface mode is row-major
 			swapFirstTwoInOrder(order);
 		}
 
 		AbstractDataset reorderedData;
 		IMetaData metaDataObject = null;
 		try {
 			metaDataObject = dataset.getMetadata();
 		} catch (Exception e1) {
 			logger.error("Metadata cannot be retrieved from " + dataset.getName(), e1);
 		}
 
 		switch(itype) {
 		case LINE:
 			reorderedData = slicedAndReorderData(monitor, slices, order);
 			if (isRankBad(reorderedData, 1)) {
 				try {
 					SDAPlotter.clearPlot(PLOTNAME);
 				} catch (Exception e) {
 					logger.error("Could not clear plot", e);
 				}
 				return;
 			}
 
 			try {
 				SDAPlotter.updatePlot(PLOTNAME, slicedAxes.get(0), reorderedData);
 			} catch (Exception e) {
 				logger.error("Could not plot 1d line");
 				return;
 			}
 
 			break;
 		case LINESTACK:
 			reorderedData = slicedAndReorderData(monitor, slices, order);
 			if (isRankBad(reorderedData, 2)) {
 				try {
 					SDAPlotter.clearPlot(PLOTNAME);
 				} catch (Exception e) {
 					logger.error("Could not clear plot", e);
 				}
 				return;
 			}
 
 			final int[] dims = reorderedData.getShape();
 			int lines = dims[1];
 			if (lines > STACKPLOTLIMIT) {
 				logger.warn("Try plot too many lines in stack plot: reduced from {} lines to {}", lines, STACKPLOTLIMIT);
 				int d = order[1];
 				SliceProperty p = sliceProperties.get(d);
 				Slice s = p.getValue();
 				Integer st = s.getStart();
 				p.setStop((st == null ? 0 : st) + STACKPLOTLIMIT*s.getStep(), true);
 				return;
 			}
 
			AbstractDataset zaxis = make1DAxisSlice(slicedAxes, 1);
 			AbstractDataset xaxisarray = slicedAxes.get(0);
 
 			AbstractDataset[] xaxes = new AbstractDataset[lines];
 			if (xaxisarray.getRank() == 1)
 				for (int i = 0; i < lines; i++)
 					xaxes[i] = xaxisarray;
 			else
 				for (int i = 0; i < lines; i++)
 					xaxes[i] = xaxisarray.getSlice(new int[] {0, i}, new int[] {dims[0], i+1}, null).squeeze();
 
 			AbstractDataset[] yaxes = new AbstractDataset[lines];
 			boolean isDimAxis = slicedAxes.get(1).getName().startsWith(AbstractExplorer.DIM_PREFIX);
 			for (int i = 0; i < lines; i++) {
 				AbstractDataset slice = reorderedData.getSlice(new int[] {0, i}, new int[] {dims[0], i+1}, null);
 				slice.squeeze();
 				if (isDimAxis) {
 					slice.setName(String.format("%s[%d]", dataset.getName(), i));
 				} else {
 					String z = lines == 1 && zaxis.getRank() == 0 ? zaxis.getString() : zaxis.getString(i);
 					slice.setName(String.format("%s[%d=%s]", dataset.getName(), i, z));
 				}
 				yaxes[i] = slice;
 			}
 			try {
 				if (plotStackIn3D)
 					SDAPlotter.updateStackPlot(PLOTNAME, xaxes, yaxes, zaxis);
 				else {
 					SDAPlotter.updatePlot(PLOTNAME, xaxes, yaxes);
 				}
 			} catch (Exception e) {
 				logger.error("Could not plot 1d stack");
 			}
 			break;
 		case IMAGE:
 		case SURFACE:
 			reorderedData = slicedAndReorderData(monitor, slices, order);
 			if (isRankBad(reorderedData, 2)) {
 				try {
 					SDAPlotter.clearPlot(PLOTNAME);
 				} catch (Exception e) {
 					logger.error("Could not clear plot", e);
 				}
 				return;
 			}
 
 			reorderedData.setName(dataset.getName()); // TODO add slice string
 			if (metaDataObject != null) {
 				reorderedData.setMetadata(metaDataObject);
 			}
 
 			try {
 				AbstractDataset xAxisSlice = make1DAxisSlice(slicedAxes, 0);
 				AbstractDataset yAxisSlice = make1DAxisSlice(slicedAxes, 1);
 				
 				if (itype == InspectorType.IMAGE)
 					SDAPlotter.imagePlot(PLOTNAME, xAxisSlice, yAxisSlice, reorderedData);
 				else
 					SDAPlotter.surfacePlot(PLOTNAME, xAxisSlice, yAxisSlice, reorderedData);
 			} catch (Exception e) {
 				logger.error("Could not plot image or surface");
 			}
 			break;
 		case IMAGEXP:
 			if (isExplorerNull())
 				return;
 
 			pushImages(monitor, slices, order);
 			break;
 		case MULTIIMAGES:
 			pushMultipleImages(monitor, sliceProperties, slices, slicedAxes, order);
 			break;
 		case VOLUME:
 			reorderedData = slicedAndReorderData(monitor, slices, order);
 			if (isRankBad(reorderedData, 3)) {
 				return;
 			}
 
 			try {
 				SDAPlotter.volumePlot(VOLVIEWNAME, reorderedData);
 			} catch (Exception e) {
 				logger.error("Could not plot volume");
 			}
 			break;
 		case DATA1D:
 		case DATA2D:
 		case EMPTY:
 		case POINTS1D:
 		case POINTS2D:
 		case POINTS3D:
 			break;
 		}
 
 	}
 
 	private boolean isExplorerNull() {
 		if (explorerName == null || explorer == null) {
 			site.getShell().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						explorer = (ImageExplorerView) site.getPage().showView(ImageExplorerView.ID, null,
 								IWorkbenchPage.VIEW_CREATE);
 						if (explorer != null) {
 							explorerName = explorer.getPlotViewName();
 						} else {
 							explorerName = null;
 						}
 					} catch (PartInitException e) {
 						logger.error("Cannot find image explorer view");
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 
 		return explorerName == null;
 	}
 
 	private void pushImages(final IMonitor monitor, final Slice[] slices, final int[] order) {
 		// work out slicing result
 		int[] shape = dataset.getShape();
 		int smax = slices.length;
 		if (smax < 2)
 			smax = 2;
 		final int sliceAxis = order[2];
 		final Slice[] subSlices = new Slice[smax];
 		for (int i = 0; i < smax; i++) {
 			if (i < slices.length) {
 				subSlices[i] = i == sliceAxis ? slices[i].clone() : slices[i];
 			} else {
 				subSlices[i] = new Slice(shape[i]);
 			}
 			shape[i] = slices[i].getNumSteps();
 		}
 
 		final int nimages = shape[sliceAxis];
 
 		try {
 			SDAPlotter.setupNewImageGrid(explorerName, nimages);
 		} catch (Exception e) {
 			logger.warn("Problem with setting up image explorer", e);
 		}
 
 		try {
 			Slice subSlice = subSlices[sliceAxis];
 			int start = subSlice.getStart() == null ? 0 : subSlice.getStart();
 			subSlices[sliceAxis].setStop(start+1);
 			setInspectionRunning();
 			boolean memoryOK = true;
 			for (int i = 0; i < nimages; i++) {
 				try {
 					subSlices[sliceAxis].setPosition(start + i);
 					AbstractDataset slicedData = sliceData(monitor, subSlices);
 					if (slicedData == null)
 						return;
 
 					AbstractDataset reorderedData = DatasetUtils.transpose(slicedData, order);
 
 					reorderedData.setName(slicedData.getName());
 					reorderedData.squeeze();
 					if (reorderedData.getSize() < 1)
 						return;
 
 					reorderedData.setName(dataset.getName() + "." + i);
 					if (!canContinueInspection()) {
 						return;
 					}
 
 					if (explorer.isStopped()) {
 						stopInspection();
 						return;
 					}
 
 					SDAPlotter.plotImageToGrid(explorerName, reorderedData, true);
 					if (!memoryOK)
 						logger.warn("... memory reduction successful");
 					memoryOK = true;
 				} catch (OutOfMemoryError e) {
 					if (!memoryOK) // only allow one GC per slice
 						throw e;
 					memoryOK = false;
 					logger.warn("Ran out of memory: attempting to reduce memory used...");
 					System.gc();
 					i--;  // try again after memory reduction
 				}
 			}
 		} catch (Exception e) {
 			logger.warn("Problem with sending data to image explorer", e);
 		} finally {
 			stopInspection();
 		}
 	}
 
 	private void pushMultipleImages(final IMonitor monitor, List<SliceProperty> sliceProperties, final Slice[] slices, List<AbstractDataset> slicedAxes, final int[] order) {
 		// work out slicing result
 		int[] shape = dataset.getShape();
 		int smax = slices.length;
 		if (smax < 2)
 			smax = 2;
 		final int sliceAxis = order[2];
 		final Slice[] subSlices = new Slice[smax];
 		for (int i = 0; i < smax; i++) {
 			if (i < slices.length) {
 				subSlices[i] = i == sliceAxis ? slices[i].clone() : slices[i];
 			} else {
 				subSlices[i] = new Slice(shape[i]);
 			}
 			shape[i] = slices[i].getNumSteps();
 		}
 
 		final int nimages = shape[sliceAxis];
 
 		if (nimages > MULTIIMAGESLIMIT) {
 			logger.warn("Try plot too many images in multiple images plot: reduced from {} images to {}", nimages, MULTIIMAGESLIMIT);
 			SliceProperty p = sliceProperties.get(sliceAxis);
 			Slice s = p.getValue();
 			Integer st = s.getStart();
 			p.setStop((st == null ? 0 : st) + MULTIIMAGESLIMIT*s.getStep(), true);
 			return;
 		}
 
 		AbstractDataset yaxis = make1DAxisSlice(slicedAxes, 1);
 		AbstractDataset xaxis = make1DAxisSlice(slicedAxes, 0);
 
 		try {
 			Slice subSlice = subSlices[sliceAxis];
 			int start = subSlice.getStart() == null ? 0 : subSlice.getStart();
 			subSlices[sliceAxis].setStop(start+1);
 			setInspectionRunning();
 
 			IDataset[] images = new IDataset[nimages];
 			for (int i = 0; i < nimages; i++) {
 				subSlices[sliceAxis].setPosition(start + i);
 				AbstractDataset slicedData = sliceData(monitor, subSlices);
 				if (slicedData == null)
 					return;
 
 				AbstractDataset reorderedData = DatasetUtils.transpose(slicedData, order);
 
 				reorderedData.setName(slicedData.getName());
 				reorderedData.squeeze();
 				if (reorderedData.getSize() < 1)
 					return;
 
 				reorderedData.setName(dataset.getName() + "." + i);
 				if (!canContinueInspection()) {
 					return;
 				}
 
 				images[i] = reorderedData;
 			}
 			SDAPlotter.imagesPlot(PLOTNAME, xaxis, yaxis, images);
 		} catch (Exception e) {
 			logger.warn("Problem with sending data to image explorer", e);
 		} finally {
 			stopInspection();
 		}
 	}
 }
 
 /**
  * Straightforward dataset table tabs
  */
 class DataTab extends PlotTab {
 
 	public DataTab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
 		super(partSite, type, title, axisNames);
 	}
 
 	@Override
 	public void pushToView(IMonitor monitor, List<SliceProperty> sliceProperties) {
 		if (dataset == null)
 			return;
 
 		Slice[] slices = new Slice[sliceProperties.size()];
 		for (int i = 0; i < slices.length; i++) {
 			slices[i] = sliceProperties.get(i).getValue();
 		}
 
 		int[] order = getOrder(daxes.size());
 		final List<AbstractDataset> slicedAxes = sliceAxes(getChosenAxes(), slices, order);
 
 
 		if (itype == InspectorType.DATA2D) {
 			swapFirstTwoInOrder(order);
 		}
 
 		final AbstractDataset reorderedData = slicedAndReorderData(monitor, slices, order);
 		if (reorderedData == null)
 			return;
 		
 		reorderedData.setName(dataset.getName());
 		reorderedData.squeeze();
 		if (reorderedData.getSize() < 1)
 			return;
 
 		switch (itype) {
 		case DATA1D:
 			if (reorderedData.getRank() != 1)
 				return;
 
 			composite.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					DatasetTableView tableView = getDatasetTableView();
 					if (tableView == null)
 						return;
 					tableView.setData(reorderedData.reshape(reorderedData.getShape()[0], 1), slicedAxes.get(0), null);		
 				}
 			});
 			break;
 		case DATA2D:
 			if (reorderedData.getRank() != 2)
 				return;
 			composite.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					DatasetTableView tableView = getDatasetTableView();
 					if (tableView == null)
 						return;
 					tableView.setData(reorderedData, slicedAxes.get(1), slicedAxes.get(0));
 				}
 			});
 			break;
 		case EMPTY:
 		case IMAGE:
 		case LINE:
 		case LINESTACK:
 		case IMAGEXP:
 		case MULTIIMAGES:
 		case POINTS1D:
 		case POINTS2D:
 		case POINTS3D:
 		case SURFACE:
 		case VOLUME:
 			break;
 		}
 	}
 
 	private DatasetTableView getDatasetTableView() {
 		DatasetTableView view = null;
 
 		// check if Dataset Table View is open
 		try {
 			view = (DatasetTableView) site.getPage().showView(DatasetTableView.ID, null, IWorkbenchPage.VIEW_CREATE);
 		} catch (PartInitException e) {
 			logger.error("All over now! Cannot find dataset table view: {} ", e);
 		}
 		return view;
 	}
 }
 
 /**
  * Scatter point plots
  */
 class ScatterTab extends PlotTab {
 	private static final int POINTSIZE = 4;
 	private static final String CONSTANT = "constant";
 	private static final String DATA = "data";
 	private boolean useData; // true if we want to use dataset values for size of points
 
 	public ScatterTab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
 		super(partSite, type, title, axisNames);
 		comboOffset = 1;
 	}
 
 	@Override
 	protected void createCombos(Composite cHolder, SelectionListener listener) {
 		new Label(cHolder, SWT.NONE).setText("size");
 		Combo c = new Combo(cHolder, SWT.READ_ONLY);
 		c.add("               ");
 		c.addSelectionListener(listener);
 		combos.add(c);
 		super.createCombos(cHolder, listener);
 	}
 
 	@Override
 	public boolean checkCompatible(ILazyDataset data) {
 		boolean isCompatible = false;
 		int rank = data.getRank();
 		if (rank == 1)
 			isCompatible = true;
 		else
 			isCompatible = rank >= axes.length - 1;
 		if (composite != null)
 			composite.setEnabled(isCompatible);
 		return isCompatible;
 	}
 
 	@Override
 	public boolean canPlotConstant() {
 		return true;
 	}
 
 	@Override
 	public boolean[] getUsedDims() {
 		boolean[] used = super.getUsedDims();
 
 		if (daxes != null && daxes.size() == 1)
 			used[0] = true;
 		return used;
 	}
 
 	@Override
 	protected List<AxisChoice> getChosenAxes() {
 		if (daxes != null && daxes.size() != 1)
 			return super.getChosenAxes();
 
 		List<String> names = getChosenAxisNames();
 		List<AxisChoice> list = new ArrayList<AxisChoice>();
 
 		for (String n : names) {
 			AxisChoice a = null;
 			for (AxisSelection s : daxes) {
 				a = s.getAxis(n);
 				if (a != null) {
 					break;
 				}
 			}
 			if (a != null) {
 				list.add(a);
 			} else {
 				logger.warn("No axis of names {} found in selections {}", names, daxes);
 			}
 		}
 		return list;
 	}
 
 	protected LinkedList<String> getAllAxisNames() {
 		// get all axis names for dimensions > 1
 		LinkedList<String> sAxes = new LinkedList<String>();
 		if (daxes != null) {
 			for (AxisSelection a : daxes) {
 				if (a.getLength() > 0) {
 					for (int j = 0, jmax = a.size(); j < jmax; j++) {
 						sAxes.add(a.getName(j));
 					}
 				}
 			}
 		}
 	
 		return sAxes;
 	}
 
 	@Override
 	protected void populateCombos() {
 		Combo c = combos.get(0);
 		c.removeAll();
 		c.add(CONSTANT);
 		String name = dataset == null ? null : dataset.getName();
 		if (name == null || name.length() == 0)
 			c.add(DATA);
 		else
 			c.add(name);
 		c.setText(CONSTANT);
 		if (paxes != null) {
 			PlotAxisProperty p = paxes.get(0);
 			p.setName(CONSTANT, false);
 		}
 
 		if (daxes!= null && daxes.size() != 1) {
 			super.populateCombos();
 			return;
 		}
 
 		int cSize = combos.size() - comboOffset;
 		LinkedList<String> sAxes = getAllAxisNames();
 		int jmax = daxes.size();
 
 		for (int i = 0; i < cSize; i ++) {
 			c = combos.get(i + comboOffset);
 			c.removeAll();
 			PlotAxisProperty p = paxes.get(i + comboOffset);
 
 			String a;
 			if (i < jmax) {
 				a = daxes.get(i).getSelectedName();
 				if (!sAxes.contains(a)) {
 					a = sAxes.getLast();
 				}
 			} else {
 				a = sAxes.getLast();
 			}
 
 			p.clear();
 			int pmax = sAxes.size();
 			for (int j = 0; j < pmax; j++) {
 				String n = sAxes.get(j);
 				p.put(j, n);
 				c.add(n);
 			}
 			c.setText(a);
 			sAxes.remove(a);
 			p.setName(a, false);
 			p.setInSet(true);
 		}
 	}
 
 	@Override
 	protected void repopulateCombos(String oldName, String newName) {
 		if (combos == null)
 			return;
 
 		if (daxes != null && daxes.size() != 1) {
 			super.repopulateCombos(oldName, newName);
 			return;
 		}
 
 		// cascade through plot axes strings and indices
 		// reduce choice each time
 		int cSize = combos.size() - comboOffset;
 		LinkedList<String> sAxes = getAllAxisNames();
 
 		int jmax = daxes.size();
 		if (jmax == 0)
 			return;
 
 		boolean fromAxisSelection = oldName != null && newName != null;
 		String a = null;
 		for (int i = 0; i < cSize; i ++) {
 			Combo c = combos.get(i + comboOffset);
 			a = (fromAxisSelection && i < jmax) ? daxes.get(i).getSelectedName() : c.getItem(c.getSelectionIndex());
 			c.removeAll();
 
 			if (!sAxes.contains(a)) {
 				a = sAxes.get(0);
 			}
 			if (!fromAxisSelection) {
 				if (i < jmax) daxes.get(i).selectAxis(a, false);
 			}
 			for (String p: sAxes)
 				c.add(p);
 
 			c.setText(a);
 			sAxes.remove(a);
 			if (paxes != null) {
 				PlotAxisProperty p = paxes.get(i + comboOffset);
 				if (p.isInSet()) {
 					p.setName(a, false);
 				}
 			}
 
 		}
 		if (a != null && paxes != null) {
 			if (paxes.get(cSize-1 + comboOffset).isInSet()) {
 				paxes.get(cSize-1 + comboOffset).setName(a);
 			}
 		}
 
 	}
 
 	@Override
 	protected List<AbstractDataset> sliceAxes(List<AxisChoice> axes, Slice[] slices, int[] order) {
 		if (daxes.size() != 1)
 			return super.sliceAxes(axes, slices, order);
 
 		List<AbstractDataset> slicedAxes = new ArrayList<AbstractDataset>();
 		if (slices.length != 1) {
 			logger.error("No slices defined");
 			return null;
 		}
 
 		Slice s = slices[0];
 		if (s != null && !s.isSliceComplete()) {
 			for (AxisChoice a : axes) {
 				slicedAxes.add(DatasetUtils.convertToAbstractDataset(a.getValues().getSlice(s)));
 			}
 		} else {
 			for (AxisChoice a : axes) {
 				slicedAxes.add(DatasetUtils.convertToAbstractDataset(a.getValues()));
 			}
 		}
 
 		return slicedAxes;
 	}
 
 	@Override
 	public void pushToView(IMonitor monitor, List<SliceProperty> sliceProperties) {
 		if (dataset == null)
 			return;
 
 		useData = !CONSTANT.equals(paxes.get(0).getName());
 
 		Slice[] slices = new Slice[sliceProperties.size()];
 		for (int i = 0; i < slices.length; i++) {
 			slices[i] = sliceProperties.get(i).getValue();
 		}
 
 		List<AxisChoice> axes = getChosenAxes();
 		int rank = daxes.size();
 		int[] order = getOrder(rank);
 		List<AbstractDataset> slicedAxes = sliceAxes(axes, slices, order);
 		if (slicedAxes == null)
 			return;
 
 
 		AbstractDataset reorderedData = slicedAndReorderData(monitor, slices, order);
 		if (reorderedData == null)
 			return;
 
 		// TODO cope with axis datasets that are >1 dimensions
 		AbstractDataset x;
 		IDataset y;
 		switch (itype) {
 		case POINTS1D:
 			x = slicedAxes.get(0);
 			y = reorderedData.flatten();
 			if (!x.isCompatibleWith(y)) {
 				logger.error("Could not match axis to data for scatter plot");
 				return;
 			}
 			IDataset size = useData ? y : new IntegerDataset(x.getSize()).fill(POINTSIZE);
 			try {
 				SDAPlotter.scatter2DPlot(PLOTNAME, x.flatten(), y, size);
 			} catch (Exception e) {
 				logger.error("Could not plot 1d points");
 				return;
 			}
 			break;
 		case POINTS2D:
 			if (!useData) { // TODO >1D dataset
 				x = slicedAxes.get(0).flatten();
 				y = slicedAxes.get(1).flatten();
 				int length = Math.min(x.getSize(), y.getSize());
 				Slice slice = new Slice(length);
 				reorderedData = new IntegerDataset(length).fill(POINTSIZE);
 				try {
 					SDAPlotter.scatter2DPlot(PLOTNAME, x.getSlice(slice), y.getSlice(slice), reorderedData);
 				} catch (Exception e) {
 					logger.error("Could not plot 2d points");
 					return;
 				}
 			} else {
 				if (reorderedData.getRank() == 1) {
 					x = slicedAxes.get(0);
 					y = slicedAxes.get(1);
 				} else {
 					List<AbstractDataset> grid = DatasetUtils.meshGrid(slicedAxes.get(0), slicedAxes.get(1));
 					x = grid.get(0);
 					y = grid.get(1);
 				}
 				if (!reorderedData.isCompatibleWith(x) || !reorderedData.isCompatibleWith(y)) {
 					logger.error("Could not match axes to data for scatter plot");
 					return;
 				}
 				try {
 					SDAPlotter.scatter2DPlot(PLOTNAME, x.flatten(), ((AbstractDataset) y).flatten(), reorderedData.flatten());
 				} catch (Exception e) {
 					logger.error("Could not plot 2d points");
 					return;
 				}
 			}
 			break;
 		case POINTS3D:
 			if (!useData) { // TODO >1D dataset
 				x = slicedAxes.get(0).flatten();
 				y = slicedAxes.get(1).flatten();
 				AbstractDataset z = slicedAxes.get(2).flatten();
 				int length = Math.min(x.getSize(), y.getSize());
 				length = Math.min(length, z.getSize());
 				Slice slice = new Slice(length);
 				reorderedData = new IntegerDataset(length).fill(POINTSIZE);
 				try {
 					SDAPlotter.scatter3DPlot(PLOTNAME, x.getSlice(slice), y.getSlice(slice), z.getSlice(slice), reorderedData);
 				} catch (Exception e) {
 					logger.error("Could not plot 3d points");
 					return;
 				}
 			} else {
 				IDataset z;
 				x = DatasetUtils.convertToAbstractDataset(axes.get(0).getValues());
 				y = DatasetUtils.convertToAbstractDataset(axes.get(1).getValues());
 				z = DatasetUtils.convertToAbstractDataset(axes.get(2).getValues());
 				if (reorderedData.getRank() == 1) {
 				} else {
 					List<AbstractDataset> grid = DatasetUtils.meshGrid(x, (AbstractDataset) y, (AbstractDataset) z);
 					x = grid.get(0);
 					y = grid.get(1);
 					z = grid.get(2);
 				}
 				if (!reorderedData.isCompatibleWith(x) || !reorderedData.isCompatibleWith(y) || !reorderedData.isCompatibleWith(z)) {
 					logger.error("Could not match axes to data for scatter plot");
 					return;
 				}
 				try {
 					SDAPlotter.scatter3DPlot(PLOTNAME, x.flatten(), ((AbstractDataset) y).flatten(), ((AbstractDataset) z).flatten(), reorderedData.flatten());
 				} catch (Exception e) {
 					logger.error("Could not plot 3d points");
 					return;
 				}
 			}
 			break;
 		case DATA1D:
 		case DATA2D:
 		case EMPTY:
 		case IMAGE:
 		case LINE:
 		case LINESTACK:
 		case IMAGEXP:
 		case MULTIIMAGES:
 		case SURFACE:
 		case VOLUME:
 			break;
 		}
 	}
 }
 
