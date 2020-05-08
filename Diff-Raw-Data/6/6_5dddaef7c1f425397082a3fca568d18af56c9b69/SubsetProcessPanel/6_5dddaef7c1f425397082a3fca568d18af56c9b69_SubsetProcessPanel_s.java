 /*
  * SubsetProcessPanel.java
  *
  * Created on September 6, 2007, 12:57 PM
  *
  * Applied Science Associates, Inc.
  * Copyright 2007.  All rights reserved.
  */
 package com.asascience.edc.gui;
 
 import gov.noaa.pmel.swing.JSlider2Date;
 
 import java.awt.BorderLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import net.miginfocom.swing.MigLayout;
 import ucar.nc2.ui.widget.BAMutil;
 import ucar.nc2.ui.widget.FileManager;
 import ucar.nc2.ui.widget.IndependentWindow;
 import ucar.nc2.ui.gis.worldmap.WorldMapBean;
 import ucar.ma2.Array;
 import ucar.ma2.IndexIterator;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1D;
 import ucar.nc2.dataset.NetcdfDataset;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.grid.GeoGrid;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ui.grid.GridUI;
 import ucar.nc2.units.DateUnit;
 import ucar.unidata.geoloc.LatLonRect;
 import ucar.util.prefs.PreferencesExt;
 
 import com.asascience.edc.ArcType;
 import com.asascience.edc.Configuration;
 import com.asascience.edc.nc.GridReader;
 import com.asascience.edc.nc.NcReaderBase;
 import com.asascience.edc.nc.NetcdfConstraints;
 import com.asascience.edc.nc.io.NcProperties;
 import com.asascience.edc.nc.io.NetcdfGridWriter;
 import com.asascience.openmap.ui.OMSelectionMapPanel;
 import com.asascience.openmap.utilities.GeoConstraints;
 import com.asascience.ui.IndeterminateProgressDialog;
 import com.asascience.utilities.FileMonitor;
 import com.asascience.utilities.Utils;
 
 /**
  * 
  * @author CBM
  */
 public class SubsetProcessPanel extends JPanel {
 
 	private static final String GRIDVIEW_FRAME_SIZE = "GridUIWindowSize";
 	public static final String AOI_LIST = "aoilist";
 	private PreferencesExt mainPrefs;
 	private JFrame mainFrame;
 	private NetcdfConstraints constraints;
 	// private MapPanel mapPanel;
 	private OMSelectionMapPanel mapPanel;
 	private SelectionPanelBase selPanel;
 	private IndependentWindow viewerWindow;
 	private GridUI gridUI;
 	// private Date[] timeDates;
 	// private Date startTime;
 	// private Date endTime;
 	private NetcdfDataset ncd;
 	private GridDataset gridDataset;
 	private List<GridDataset> gdsList;
 	// private GridReader gridReader;
 	protected NcReaderBase ncReader;
 	private OpendapInterface parent;
 	private FileManager fileChooser;
 	private List aoiList;
 	private String homeDir;
 	private String sysDir;
 	private DimMapDialog dimMap;
 	private String ncOutPath;
 
 	/**
 	 * Creates a new instance of SubsetProcessPanel
 	 * 
 	 * @param prefs
 	 * @param fileChooser
 	 * @param caller
 	 * @param cons
 	 * @param ncd
 	 * @param homeDir
 	 * @param sysDir
 	 */
 	public SubsetProcessPanel(ucar.util.prefs.PreferencesExt prefs, FileManager fileChooser, OpendapInterface caller,
 		NetcdfConstraints cons, NetcdfDataset ncd, String homeDir, String sysDir) {
 		// try {
 
 		this.mainPrefs = prefs;
 		this.mainFrame = caller.mainFrame;
 		this.parent = caller;
 		this.fileChooser = fileChooser;
 		this.constraints = cons;
 		this.ncd = ncd;
 		this.homeDir = Utils.appendSeparator(homeDir);
 		this.sysDir = Utils.appendSeparator(sysDir);
 
 		// retrieve the aoiList from the preferences
 		aoiList = (ArrayList) prefs.getList(AOI_LIST, null);
 
 		initComponents();
 	}
 
 	public boolean initData() {
 		try {
 			gdsList = new ArrayList<GridDataset>();
 			// try it as a grid first...
 			ncReader = new GridReader(ncd, constraints);
 			// if(!gridReader.initialize()){
 			// throw new NcGridReadException("Error Reading Grid Dataset");
 			// }
 			int ret = ncReader.initialize();
 			switch (ret) {
 				case NcReaderBase.INIT_OK:
 					gdsList.add(((GridReader) ncReader).getGridDataset());
 
 					setNcName(ncd.getTitle(), ncd.getLocation());
 					// setGridDataset(((GridReader)ncReader).getGridDataset());
 					setNcExtent(ncReader.getBounds());
 					// setTimeDates(gridReader.getTimes());
 					if (ncReader.isHasTime()) {
 						// timeDates = ncReader.getTimes();
 						dateSlider.setRange(ncReader.getStartTime(), ncReader.getEndTime());
 						lblDateIncrement.setText(lblDateIncrement.getText() + constraints.getTimeInterval());
 						lblNumDatesSelected.setText("# Timesteps Selected: " + Math.round(calcNumTimesteps()));
 					} else {
 						dateSlider.reset();
 						dateSlider.setVisible(false);
 						lblDateIncrement.setText(lblDateIncrement.getText() + "NA");
 						lblNumDatesSelected.setText("# Timesteps Selected: " + "NA");
 					}
 
 					setVariables(((GridReader) ncReader).getGeoGrids());
 					// setBandDims();
 
 					if (!((GridReader) ncReader).isRegularSpatial()) {
 						if (selPanel.getPanelType() == SelectionPanelBase.ESRI) {
 							selPanel.gridRegularity(((GridReader) ncReader).isRegularSpatial());
 						}
 					}
 
 					validate();
 
 					return true;
 				case NcReaderBase.NO_GRIDDATASET:
 					JOptionPane.showMessageDialog(mainFrame,
 						"A GridDataset could not be built from the selected dataset.", "Could not open dataset",
 						JOptionPane.WARNING_MESSAGE);
 					break;
 				case NcReaderBase.NO_GEOGRIDS:
 					JOptionPane.showMessageDialog(mainFrame,
 						"The selected dataset does not contain any gridded fields.", "Could not open dataset",
 						JOptionPane.WARNING_MESSAGE);
 					break;
 				case NcReaderBase.INVALID_BOUNDS:
 					JOptionPane.showMessageDialog(mainFrame, "The selected dataset has invalid bounds.",
 						"Could not open dataset", JOptionPane.WARNING_MESSAGE);
 					break;
 				case NcReaderBase.UNDEFINED_ERROR:
 					JOptionPane.showMessageDialog(mainFrame,
 						"An undefined error was encountered when reading the selected dataset.\n\n"
 							+ "See the \"edcsysout.log\" file for further details", "Could not open dataset",
 						JOptionPane.WARNING_MESSAGE);
 					break;
 			}
 
 			// <editor-fold defaultstate="collapsed"
 			// desc=" Old Init-check Method ">
 			// if(ret == 0){//the dataset contains geogrids
 			// gdsList.add(((GridReader)ncReader).getGridDataset());
 			//
 			// setNcName(ncd.getTitle(), ncd.getLocation());
 			// // setGridDataset(((GridReader)ncReader).getGridDataset());
 			// setNcExtent(ncReader.getBounds());
 			// // setTimeDates(gridReader.getTimes());
 			// if(ncReader.isHasTime()){
 			// timeDates = ncReader.getTimes();
 			// dateSlider.setRange(ncReader.getStartTime(),
 			// ncReader.getEndTime());
 			// lblDateIncrement.setText(lblDateIncrement.getText() +
 			// constraints.getTimeInterval());
 			// lblNumDatesSelected.setText("# Timesteps Selected: " +
 			// Math.round(calcNumTimesteps()));
 			// }else{
 			// dateSlider.reset();
 			// dateSlider.setVisible(false);
 			// lblDateIncrement.setText(lblDateIncrement.getText() + "NA");
 			// lblNumDatesSelected.setText("# Timesteps Selected: " + "NA");
 			// }
 			//
 			// setVariables(((GridReader)ncReader).getGeoGrids());
 			// // setBandDims();
 			//
 			// if(!((GridReader)ncReader).isRegularSpatial()){
 			// if(selPanel.getPanelType() == SelectionPanelBase.ESRI){
 			// selPanel.gridRegularity(((GridReader)ncReader).isRegularSpatial());
 			// }
 			// }
 			//
 			// validate();
 			//
 			// return true;
 			// }else if(ret == 1){//there are no geogrids in the dataset
 			// // ncReader = new NonGridReader(ncd, constraints);
 			// // if(ncReader.initialize()){
 			// // dimMap = new DimMapDialog(mainFrame,
 			// ((NonGridReader)ncReader).getDimNames());
 			// // dimMap.setVisible(true);
 			// //
 			// // ((NonGridReader)ncReader).applyMap(
 			// // dimMap.getTimeDim(), dimMap.getXDim(),
 			// // dimMap.getYDim(), dimMap.getZDim());
 			// //// System.err.println("time=" + dimMap.getTimeDim());
 			// //// System.err.println("lat=" + dimMap.getYDim());
 			// //// System.err.println("lon=" + dimMap.getXDim());
 			// //// System.err.println("level=" + dimMap.getZDim());
 			// //
 			// //// if(true){
 			// //// return false;
 			// //// }
 			// //
 			// // setNcName(ncd.getTitle(), ncd.getLocation());
 			// //
 			// // setNcExtent(ncReader.getBounds());
 			// //
 			// // if(ncReader.isHasTime()){
 			// // timeDates = ncReader.getTimes();
 			// // dateSlider.setRange(ncReader.getStartTime(),
 			// ncReader.getEndTime());
 			// // lblDateIncrement.setText(lblDateIncrement.getText() +
 			// constraints.getTimeInterval());
 			// // lblNumDatesSelected.setText("# Timesteps Selected: " +
 			// calcNumTimesteps());
 			// // }else{
 			// // dateSlider.reset();
 			// // dateSlider.setVisible(false);
 			// // lblDateIncrement.setText(lblDateIncrement.getText() + "NA");
 			// // lblNumDatesSelected.setText("# Timesteps Selected: " + "NA");
 			// // }
 			// //
 			// // setVariables(((NonGridReader)ncReader).getVars());
 			// //
 			// // return true;
 			// // }
 			//
 			//
 			// JOptionPane.showMessageDialog(mainFrame,
 			// "The selected dataset does not contain any gridded fields.",
 			// "Could not open dataset", JOptionPane.WARNING_MESSAGE);
 			// }else if(ret == 2){
 			//
 			// }
 			// </editor-fold>
 
 			// }catch(NcGridReadException ex){
 			// JOptionPane.showMessageDialog(mainFrame,
 			// "The selected dataset does not contain any gridded fields.",
 			// "Could not open dataset", JOptionPane.WARNING_MESSAGE);
 			// System.err.println("SPP:initData:");
 			// System.err.println(ex.getMessage());
 			// // ex.printStackTrace();
 		} catch (IOException ex) {
 			System.err.println("SPP:initData:");
 			ex.printStackTrace();
 		} catch (Exception ex) {
 			System.err.println("SPP:initData:");
 			ex.printStackTrace();
 		}
 		return false;
 	}
 
 	// These were all long....
 	private double calcNumTimesteps() {
 		// selected Range in seconds
 		double rng = (dateSlider.getMaxValue().getCalendar().getTime().getTime() - dateSlider.getMinValue()
 			.getCalendar().getTime().getTime()) / 1000;
 
 		// long inter = Long.parseLong(constraints.getTimeInterval());
 		double inter = Double.parseDouble(constraints.getTimeInterval());
 		if (inter > 0) {
 			return (rng / inter) + 1;// add 1 to account for the start/end time
 		}
 
 		return 0;
 	}
 
 	private void initComponents() {
 		try {
 			setLayout(new BorderLayout());
 			setBorder(BorderFactory.createRaisedBevelBorder());
 
 			lblNcName = new JLabel("NetCDF Filename:");
 			btnAddDataset = new JButton("Add Dataset");
 			btnAddDataset.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 					// use a method in the parent OpendapInterface to add the
 					// variables to the
 					// current S&P tab
 					parent.addDataset(SubsetProcessPanel.this);
 				}
 			});
 
 			// make a panel to hold the name of the Netcdf file
 			JPanel namePanel = new JPanel();
 			namePanel.add(lblNcName, "center");
 			namePanel.add(btnAddDataset);
 			add(namePanel, BorderLayout.PAGE_START);
 
 			// create the map panel
 			String gisDataDir = sysDir + "data";
 			mapPanel = new OMSelectionMapPanel(constraints, gisDataDir, true);
 			mapPanel.setBorder(new EtchedBorder());
 			mapPanel.addPropertyChangeListener(new PropertyChangeListener() {
 
 				public void propertyChange(PropertyChangeEvent e) {
 					String name = e.getPropertyName();
 					if (name.equals("boundsStored")) {
 						if (selPanel != null) {
 							selPanel.setHasGeoSub(true);
 						}
 					} else if (name.equals(OMSelectionMapPanel.AOI_SAVE)) {
 						String s = javax.swing.JOptionPane.showInputDialog(mainFrame, "Enter a name for the AOI:", e
 							.getOldValue().toString());
 						if (s == null) {
 							return;
 						}
 						if (mapPanel.getSelectedExtent() != null) {
 							GeoConstraints cons = new GeoConstraints();
 							cons.setExtentName(s);
 							cons.setBoundingBox(mapPanel.getSelectedExtent());
 
 							if (aoiList == null) {
 								aoiList = new ArrayList();
 							}
 
 							aoiList.add(cons);
 							saveAois();
 						}
 					} else if (name.equals(OMSelectionMapPanel.AOI_APPLY)) {
 						/** unnecessary - aoiList already loaded. */
 						// aoiList = (ArrayList)mainPrefs.getList(AOI_LIST,
 						// null);
 
 						boolean bail = false;
 						if (aoiList == null) {
 							bail = true;
 						}
 						if (aoiList.size() == 0) {
 							bail = true;
 						}
 						if (bail) {
 							JOptionPane.showMessageDialog(mainFrame, "There are no saved AOI's to select from.");
 							return;
 						}
 
 						Object[] aois = new Object[aoiList.size()];
 						GeoConstraints cons;
 						for (int i = 0; i < aoiList.size(); i++) {
 							cons = (GeoConstraints) aoiList.get(i);
 							aois[i] = cons.getExtentName();
 						}
 						String s = (String) JOptionPane.showInputDialog(mainFrame, "Choose an AOI from the list:",
 							"Select AOI", javax.swing.JOptionPane.PLAIN_MESSAGE, null, aois, aois[0].toString());
 
 						if (s == null) {
 							return;
 						}
 
 						for (int i = 0; i < aoiList.size(); i++) {
 							cons = (GeoConstraints) aoiList.get(i);
 							if (s.equals(cons.getExtentName())) {
 								mapPanel.makeSelectedExtentLayer(cons.getBoundingBox());
 								break;
 							}
 						}
 					} else if (name.equals(OMSelectionMapPanel.AOI_CLEAR)) {
 						mapPanel.clearSelectedExtent();
 					} else if (name.equals(OMSelectionMapPanel.AOI_REMALL)) {
 						boolean bail = false;
 						if (aoiList == null) {
 							bail = true;
 						}
 						if (aoiList.size() == 0) {
 							bail = true;
 						}
 						if (bail) {
 							JOptionPane.showMessageDialog(mainFrame, "There are no saved AOI's to select from.");
 							return;
 						}
 
 						if (JOptionPane.showConfirmDialog(mainFrame,
 							"Are you sure you wish to clear the list of saved AOI's?\n"
 								+ "This action cannot be undone.", "Clear AOI List", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 
 							aoiList = new ArrayList();
 							saveAois();
 						}
 					} else if (name.equals(OMSelectionMapPanel.AOI_MANUAL)) {
 						JDialog red = mapPanel.makeRectEntryDialog(mainFrame, "Enter Rectangle Coordinates", true);
 						red.setVisible(true);
 					}
 				}
 			});
 
 			add(mapPanel, BorderLayout.CENTER);
 
 			selPanel = new GeneralSelectionPanel(constraints, this);
 			switch (Configuration.DISPLAY_TYPE) {
 				case Configuration.DisplayType.GENERAL:
 					break;
 				case Configuration.DisplayType.ESRI:
 					selPanel = new EsriSelectionPanel(constraints, this);
 					break;
 				case Configuration.DisplayType.OILMAP:
 					selPanel = new OilmapSelectionPanel(constraints, this);
 					break;
 			}
 
 			selPanel.addPropertyChangeListener(new PropertyChangeListener() {
 
 				public void propertyChange(PropertyChangeEvent e) {
 					if (btnProcess != null) {
 						String name = e.getPropertyName();
 						if (name.equals("processEnabled")) {
 							btnProcess.setEnabled(Boolean.valueOf(e.getNewValue().toString()));
 						}
 					}
 				}
 			});
 			add(selPanel, BorderLayout.LINE_END);
 
 			// create a panel to hold the time and processing panels
 			JPanel pageEndPanel = new JPanel(new MigLayout("insets 0, fill"));
 			pageEndPanel.setBorder(new EtchedBorder());
 			// pageEndPanel.setLayout(new BoxLayout(pageEndPanel,
 			// BoxLayout.Y_AXIS));
 
 			// create a panel to hold all the time related components
 			JPanel timePanel = new JPanel(new MigLayout("insets 0, fill"));
 			timePanel.setBorder(new EtchedBorder());
 
 			dateSlider = new JSlider2Date();
 			dateSlider.setAlwaysPost(true);
 			dateSlider.setHandleSize(7);// default is 6
 			dateSlider.addPropertyChangeListener(new PropertyChangeListener() {
 
 				public void propertyChange(PropertyChangeEvent evt) {
 					String name = evt.getPropertyName();
 					if (name.equals("minValue")) {
 						// constraints.setStartTime(dateSlider.getMinValue().getCalendar().getTime());
 					} else if (name.equals("maxValue")) {
 						// constraints.setEndTime(dateSlider.getMaxValue().getCalendar().getTime());
 					}
 					if (ncReader.isHasTime()) {
 						lblNumDatesSelected.setText("# Timesteps Selected: " + Math.round(calcNumTimesteps()));
 					}
 				}
 			});
 			lblDateIncrement = new JLabel("Time Interval (sec): ");
 			lblNumDatesSelected = new JLabel("# Timesteps Selected: ");
 			// TODO: add a means for remembering time-ranges?
 			timePanel.add(lblDateIncrement, "gap 0, gapright 20, center, split 2");
 			timePanel.add(lblNumDatesSelected, "gap 0, gapleft 20, center, wrap");
 			timePanel.add(dateSlider, "gap 0, grow, center");
 
 			// add the time panel to the SubsetProcessPanel
 			pageEndPanel.add(timePanel, "grow, wrap");
 
 			JPanel processPanel = new JPanel();
 			processPanel.setBorder(new EtchedBorder());
 			btnProcess = new JButton("Process");
 			btnProcess.setToolTipText("Apply the specified spatial & temporal constraints\n"
 				+ "and export the selected variables to the desired output format.");
 			btnProcess.setEnabled(false);
 			btnProcess.addActionListener(new ProcessDataListener());
 			processPanel.add(btnProcess);
 
 			JButton preview = new JButton("Preview Entire Dataset");
 			preview.setToolTipText("Preview the entire dataset without applying\n"
 				+ "any spatial or temporal subsetting.");
 			preview.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 					if (gridDataset != null) {
 						if (gridUI == null) {
 							makeGridUI();
 						}
 						gridUI.setDataset(gridDataset);
 						viewerWindow.show();
 					} else {
 						System.err.println("LinkingPanel: gridDataset = null");
 					}
 				}
 			});
 			// processPanel.add(preview);
 
 			pageEndPanel.add(processPanel, "grow");
 
 			add(pageEndPanel, BorderLayout.PAGE_END);
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private boolean rasterNameOk(String name) {
 		if (name.contains(" ")) {
 			return false;
 		}
 		if (name.contains("@")) {
 			return false;
 		}
 		if (name.contains("#")) {
 			return false;
 		}
 		if (name.contains("$")) {
 			return false;
 		}
 		if (name.contains("^")) {
 			return false;
 		}
 		if (name.contains("&")) {
 			return false;
 		}
 		if (name.contains("*")) {
 			return false;
 		}
 		if (name.contains("!")) {
 			return false;
 		}
 		if (name.length() > 10) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean addDataset(NetcdfDataset ncds) {
 		// boolean ret = false;
 		try {
 			// ret = canMerge(ncds, ncd);
 			String s = canMerge2(ncds, ncd);
 			if (!s.equals("equal")) {
 				// if(!ret){
 				JOptionPane.showMessageDialog(mainFrame, "The selected dataset is not"
 					+ " compatible with the first dataset.\n\n" + s + "\n\nPlease try another dataset.",
 					"Incompatible Datasets", JOptionPane.WARNING_MESSAGE);
 				// btnAddDataset.setEnabled(true);
 				return false;
 			} else {
 				GridReader gr = new GridReader(ncds, constraints);
 				int ret = gr.initialize();
 				switch (ret) {
 					case NcReaderBase.INIT_OK:
 						gdsList.add(gr.getGridDataset());
 						addVariables(gr.getGeoGrids());
 						return true;
 					default:
 						return false;
 				}
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public String canMerge2(NetcdfDataset ncds1, NetcdfDataset ncds2) {
 		StringBuilder ret = new StringBuilder();
 		try {
 			List coords1 = ncds1.getCoordinateAxes();
 			List coords2 = ncds2.getCoordinateAxes();
 			CoordinateAxis c1;
 			CoordinateAxis c2;
 			if (coords1.size() == coords2.size()) {
 				for (int i = 0; i < coords1.size(); i++) {
 					c1 = (CoordinateAxis) coords1.get(i);
 					c2 = (CoordinateAxis) coords2.get(i);
 					if (c1.getAxisType() == c2.getAxisType()) {
 						if (c1.getMinValue() == c2.getMinValue()) {
 							if (c1.getMaxValue() == c2.getMaxValue()) {
 								if (c1.getDataType() == c2.getDataType()) {
 									continue;
 								} else {
 									ret.append("Unequal coordinate axis data type: name=" + c1.getName() + ": c1="
 										+ c1.getDataType() + " c2=" + c2.getDataType());
 									break;
 								}
 							} else {
 								ret.append("Unequal coordinate axis maximum value: name=" + c1.getName() + ": c1="
 									+ c1.getMaxValue() + " c2=" + c2.getMaxValue());
 								break;
 							}
 						} else {
 							ret.append("Unequal coordinate axis minimum value: name=" + c1.getName() + ": c1="
 								+ c1.getMinValue() + " c2=" + c2.getMinValue());
 							break;
 						}
 					} else {
 						ret.append("Unequal coordinate axis type: name=" + c1.getName() + ": c1=" + c1.getAxisType()
 							+ " c2=" + c2.getAxisType());
 						break;
 					}
 				}
 				// if the StringBuilder has no characters, all checks were
 				// successfull
 				if (ret.length() == 0) {
 					ret.append("equal");
 				}
 			} else {
 				ret.append("Unequal number of coordinate axes: c1=" + coords1.size() + " c2=" + coords2.size());
 			}
 		} catch (Exception ex) {
 			ret.delete(0, ret.length() - 1);
 			ret.append("Error: There was an error checking the dataset for compatibility.\n\n"
 				+ "See \"edcsysout.log\" for more details.");
 			ex.printStackTrace();
 		}
 		return ret.toString();
 	}
 
 	public boolean canMerge(NetcdfDataset ncds1, NetcdfDataset ncds2) {
 		// StringBuilder ret = new StringBuilder();
 		try {
 			List coords1 = ncds1.getCoordinateAxes();
 			List coords2 = ncds2.getCoordinateAxes();
 			if (coords1.size() == coords2.size()) {
 				CoordinateAxis c1;
 				CoordinateAxis c2;
 				for (int i = 0; i < coords1.size(); i++) {
 					c1 = (CoordinateAxis) coords1.get(i);
 					c2 = (CoordinateAxis) coords2.get(i);
 					if (c1.getAxisType() != c2.getAxisType()) {
 						System.err.println("Unequal coordinate axis type: name=" + c1.getName() + ": c1="
 							+ c1.getAxisType() + " c2=" + c2.getAxisType());
 						return false;
 					}
 					if (c1.getMinValue() != c2.getMinValue()) {
 						System.err.println("Unequal coordinate axis minimum value: name=" + c1.getName() + ": c1="
 							+ c1.getMinValue() + " c2=" + c2.getMinValue());
 						return false;
 					}
 					if (c1.getMaxValue() != c2.getMaxValue()) {
 						System.err.println("Unequal coordinate axis maximum value: name=" + c1.getName() + ": c1="
 							+ c1.getMaxValue() + " c2=" + c2.getMaxValue());
 						return false;
 					}
 					if (c1.getDataType() != c2.getDataType()) {
 						System.err.println("Unequal coordinate axis dataType: name=" + c1.getName() + ": c1="
 							+ c1.getDataType() + " c2=" + c2.getDataType());
 						return false;
 					}
 				}
 				// ret.append("equal");
 				return true;// only triggered if all of the above are equal for
 				// all axes
 			} else {
 				System.err.println("Unequal number of coordinate axes: c1=" + coords1.size() + " c2=" + coords2.size());
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public void saveAois() {
 		if (mainPrefs != null) {
 			mainPrefs.putList(AOI_LIST, aoiList);
 		}
 	}
 
 	public void setGridDataset(GridDataset ds) {
 		gridDataset = ds;
 	}
 
 	private void makeGridUI() {
 		// a little tricky to get the parent right for GridUI
 		viewerWindow = new IndependentWindow("Grid Viewer", BAMutil.getImage("netcdfUI"));
 
 		gridUI = new GridUI((PreferencesExt) mainPrefs.node("GridUI"), viewerWindow, fileChooser, 800);
 		gridUI.addMapBean(new WorldMapBean());
 		// gridUI.addMapBean(new
 		// thredds.viewer.gis.shapefile.ShapeFileBean("WorldDetailMap",
 		// "Global Detailed Map", "WorldDetailMap", WorldDetailMap));
 		// gridUI.addMapBean(new
 		// thredds.viewer.gis.shapefile.ShapeFileBean("USDetailMap",
 		// "US Detailed Map", "USMap", USMap));
 
 		viewerWindow.setComponent(gridUI);
 		viewerWindow.setBounds((Rectangle) mainPrefs.getBean(GRIDVIEW_FRAME_SIZE, new Rectangle(77, 22, 700, 900)));
 	}
 
 	public void setNcName(String name, String location) {
 		lblNcName.setText("NetCDF Filename:  " + name);
 		lblNcName.setToolTipText(location);
 	}
 
 	public void setBandDims() {
 		// cbBandDim.removeAllItems();
 		// if(!constraints.getTimeDim().equals("null"))
 		// cbBandDim.addItem(constraints.getTimeDim());
 		// if(!constraints.getZDim().equals("null"))
 		// cbBandDim.addItem(constraints.getZDim());
 	}
 
 	public Variable getVarByName(String name) {
 		if (selPanel != null) {
 			for (Variable v : selPanel.getLocalVariables()) {
 				if (v.getName().equals(name)) {
 					return v;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public GeoGrid getGridByName(String name, boolean isDescription) {
 		if (selPanel != null) {
 			for (GeoGrid g : selPanel.getLocalGeoGrids()) {
 				if (!isDescription) {
 					if (g.getName().equals(name)) {// orig
 						return g;
 					}
 				} else {
 					if (g.getName().equals(selPanel.getVarNameFromDescription(name))) {
 						return g;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public void setVariables(List vars) {
 		if (selPanel != null) {
 			if (vars.get(0) instanceof GeoGrid) {
 				selPanel.setGeoGridVars(vars);
 			} else if (vars.get(0) instanceof Variable) {
 				selPanel.setVariables(vars);
 			}
 		}
 	}
 
 	public void addVariables(List vars) {
 		if (selPanel != null) {
 			if (vars.get(0) instanceof GeoGrid) {
 				selPanel.addGeoGridVars(vars);
 			} else if (vars.get(0) instanceof Variable) {
 				selPanel.addVariables(vars);
 			}
 		}
 	}
 
 	// public void setVariables(List<Variable> vars){
 	// if(selPanel != null){
 	// selPanel.setVariables(vars);
 	// }
 	// }
 	//    
 	// public void setVariables(List<GeoGrid> geoGrids) {
 	// if(selPanel != null){
 	// selPanel.setGeoGridVars(geoGrids);
 	// }
 	// }
 	public void setVariables() {
 		if (selPanel != null) {
 			selPanel.setGeoGridVars();
 		}
 	}
 
 	public void setNcExtent(LatLonRect llRect) {
 		// //for testing extent
 		// llRect = new LatLonRect(new LatLonPointImpl(-90, -0),
 		// new LatLonPointImpl(90, 360));
 		mapPanel.makeDataExtentLayer(llRect);
 	}
 
 	public void setConstraints(NetcdfConstraints cons) {
 		constraints = cons;
 	}
 
 	int runOK = -1;
 
 	class ProcessPropertyListener implements PropertyChangeListener {
 
 		private String ncPath = "null";
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			if (evt.getPropertyName().equals("close")) {
 				if (runOK == NetcdfGridWriter.SUCCESSFUL_PROCESS) {
 					System.err.println("Processing completed successfully: \"" + ncPath + "\"");
 					if (Configuration.CLOSE_AFTER_PROCESSING) {
 						// parent.closeInterface();
 						parent.formWindowClose(ncPath.replace(".nc", ".xml"));
 					} else {
 						parent.loadDatasetInViewer(ncPath);
 					}
 
 				} else if (runOK == NetcdfGridWriter.UNDEFINED_ERROR) {
 					System.err.println("Undefined error when processing.");
 					JOptionPane.showMessageDialog(mainFrame, "There was a problem extracting the data.\n"
 						+ "Please try again or refer to the \"edcsysout.log\" for more details.", "Processing Error",
 						JOptionPane.ERROR_MESSAGE);
 				} else if (runOK == NetcdfGridWriter.CANCELLED_PROCESS) {
 					// TODO: processing cancelled - perform actions to "reset"
 					System.err.println("Processing cancelled by user.");
 					File f = new File(ncOutPath);
 					if (Configuration.USE_SUBDIRECTORIES) {
 						if (f.getParentFile().getName().equals(f.getName().replace(".nc", ""))) {
 							Utils.deleteDirectory(f.getParentFile());
 						}
 					} else {
 						if (f.exists()) {
 							f.delete();
 							f = new File(f.getName().replace(".nc", ".xml"));
 							if (f.exists()) {
 								f.delete();
 							}
 						}
 					}
 				}
 
 				// "suggest" that the garbage collector clean-up
 				// System.gc();//BAD: can result in performance "black-hole"
 			} else if (evt.getPropertyName().equals("done")) {
 				ncPath = (String) evt.getNewValue();
 			}
 		}
 	}
 
 	class ProcessDataListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			if (selPanel != null) {
 				// get the extent set by the user
 				LatLonRect bounds = mapPanel.getSelectedExtent();
 				// if no user extent, use entire extent
 				if (bounds == null) {
 					bounds = ncReader.getBounds();
 				}// bounds = parent.getNcExtent();
 				constraints.setBoundingBox(bounds);
 
 				// set the start & end date/time
 				constraints.setStartTime(null);
 				constraints.setEndTime(null);
 				if (ncReader.isHasTime()) {
 					constraints.setStartTime(dateSlider.getMinValue().getCalendar().getTime());
 					constraints.setEndTime(dateSlider.getMaxValue().getCalendar().getTime());
 				}
 
 				// set the panel type
 				constraints.setPanelType(selPanel.getPanelType());
 
 				double lonSpan = Math.abs(constraints.getWesternExtent()) - Math.abs(constraints.getEasternExtent());
 				double latSpan = constraints.getNorthernExtent() - constraints.getSouthernExtent();
 
 				// if(!selPanel.isHasGeoSub() | (Math.abs(lonSpan) > 90 |
 				// Math.abs(latSpan) > 90)){
 				if ((Math.abs(lonSpan) > 90 | Math.abs(latSpan) > 90)) {
 					if (JOptionPane.showConfirmDialog(mainFrame, "The geospatial subset is larger than 90 degrees.\n"
 						+ "This may result in a very large dataset.\n\nDo you wish to continue?", "Geospatial Subset",
 						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
 						return;
 					}
 				}
 				// int rng = endSlider.getValue() - startSlider.getValue();
 				if (ncReader.isHasTime()) {
 					double rng = calcNumTimesteps();
 					// System.err.println("timesteps="+rng);
 					if (rng >= 100) {
 						if (JOptionPane.showConfirmDialog(mainFrame, "The temporal subset has not been indicated"
 							+ " or is larger than 100 timesteps.\n"
 							+ "This may result in a very large dataset.\n\nDo you wish to continue?",
 							"Temporal Subset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
 							return;
 						}
 					}
 				}
 			} else {
 				return;
 			}
 
 			// String homeDir = Utils.retrieveHomeDirectory("ODC");
 
 			boolean cont = false;
 			boolean skip = false;
 			boolean rept = false;
 			String outname = "outname";
 			String inname = "outname";
 			if (selPanel.getCblVars().getSelItemsSize() > 0) {
 				inname = selPanel.getCblVars().getSelectedItems().get(0);
 			}
 			if (inname != null) {
 				// alter default name
 				if (Configuration.USE_VARIABLE_NAME_FOR_OUTPUT) {
 					// just the variable name
 					outname = inname.substring(1, inname.indexOf("]"));
 				} else {
 					// the description with underscores
 					outname = selPanel.getFullDescriptionFromShortDescription(inname).replace(" ", "_");
 				}
 			}
 			if (outname == null) {
 				outname = "outname";
 			}
 			if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {
 				if (selPanel.isMakeRaster()) {
					// if longer, trim the name down to the first 9 characters
					if (outname.length() > 9) {
						outname = outname.substring(0, 8);
 					}
 				}
 			}
 
 			File f;
 			if (Configuration.USE_SUBDIRECTORIES) {
 				f = Utils.createIncrementalName(homeDir, outname, null);
 				outname = f.getName();
 			} else {
 				f = Utils.createIncrementalName(homeDir, outname, ".nc");
 				outname = f.getName().replace(".nc", "");
 			}
 
 			do {
 				skip = false;
 
 				outname = (String) JOptionPane.showInputDialog(mainFrame, "Enter a name for the output file:",
 					"Output Name", JOptionPane.OK_CANCEL_OPTION, null, null, outname);
 				// System.err.println(outname);
 				// check to see if the name is empty or cancel was clicked
 
 				if (outname == null) {// cancel
 					return;
 				}
 				if (outname.equals("")) {
 					rept = true;
 				}
 				if (rept) {
 					JOptionPane.showMessageDialog(mainFrame, "Name cannot be blank", "Invalid Name",
 						JOptionPane.WARNING_MESSAGE);
 				} else {
 
 					if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {// ESRI
 						if (selPanel.isMakeRaster()) {
 							// if a raster - check to make sure none of the
 							// raster naming rules were violated
 							if (!rasterNameOk(outname)) {
 								JOptionPane.showMessageDialog(null, "The name contains illegal characters "
 									+ "or is too long.\nThe maximum allowable size is 10 characters.\n"
 									+ "Special characters (i.e. @, #, $, \"space\", etc.) are not allowed.",
 									"Invalid Name", JOptionPane.WARNING_MESSAGE);
 								skip = true;
 							}
 						}
 					}
 
 					if (!skip) {
 						if (Configuration.USE_SUBDIRECTORIES) {
 							f = new File(homeDir + File.separator + outname);
 						} else {
 							f = new File(homeDir + File.separator + outname + ".nc");
 						}
 
 						if (f.exists()) {
 							if (Configuration.ALLOW_FILE_REPLACEMENT) {
 								int i = JOptionPane.showConfirmDialog(mainFrame,
 									"An output file with this name already exists." + "\nDo you wish to replace it?",
 									"Duplicate Name", JOptionPane.YES_NO_OPTION);
 								if (i == JOptionPane.YES_OPTION) {
 									// System.err.println(f.getParentFile().getAbsolutePath());
 									if (Configuration.USE_SUBDIRECTORIES) {
 										Utils.deleteDirectory(f);
 									} else {
 										f.delete();
 									}
 
 									// f.delete();
 									cont = true;
 								} else {
 									cont = false;
 								}
 							} else {
 								JOptionPane.showMessageDialog(mainFrame,
 									"An output file with this name already exists."
 										+ "\nPlease select a different name to continue.", "Duplicate Name",
 									JOptionPane.OK_OPTION);
 								cont = false;
 							}
 						} else {
 							cont = true;
 						}
 					}
 				}
 			} while (!cont);
 
 			if (f != null) {
 				if (Configuration.USE_SUBDIRECTORIES) {
 					if (!f.mkdirs()) {
 						System.err.println("SubsetProcessPanel.ProcesDataListener: " + "Could not make directory \""
 							+ f.getAbsolutePath() + "\"");
 					}
 				} else {
 					try {
 						f.createNewFile();
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 
 			ncOutPath = "";
 			if (Configuration.USE_SUBDIRECTORIES) {
 				ncOutPath = f.getAbsolutePath() + File.separator + outname + ".nc";
 			} else {
 				ncOutPath = f.getAbsolutePath();
 			}
 
 			IndeterminateProgressDialog pd = new IndeterminateProgressDialog(mainFrame, "Progress", new ImageIcon(Utils
 				.getImageResource("ASA.png", SubsetProcessPanel.class)));
 			ProcessDataTask pdt = new ProcessDataTask("Processing Data...", ncOutPath, outname);
 			pdt.addPropertyChangeListener(new ProcessPropertyListener());
 			pd.setRunTask(pdt);
 			pd.runTask();
 		}
 	}
 
 	private JButton btnProcess;
 	private JButton btnAddDataset;
 	private JSlider2Date dateSlider;
 	private JLabel lblDateIncrement;
 	private JLabel lblNumDatesSelected;
 	private JLabel lblNcName;
 
 	// replaced by "dateSlider"...TODO: DONE: remove these controls/methods
 	// private JSlider startSlider;
 	// private JSlider endSlider;
 	// private JLabel lblStartTime;
 	// private JLabel lblEndTime;
 
 	// private JTextField tfStartTime;
 	// private JTextField tfEndTime;
 	// private JSpinner sTimeSpan;
 	public class ProcessDataTask extends com.asascience.utilities.BaseTask {
 
 		private List<String> ncPaths;
 		private String ncPath;
 		private String outname;
 
 		public ProcessDataTask(String name, String ncfile, String outname) {
 			super();
 			this.setName(name);
 			this.ncPath = ncfile;
 			this.outname = outname;
 		}
 
 		@Override
 		public Void doInBackground() {
 			try {
 				firePropertyChange("progress", null, 0);// starts the dialog
 				Thread.sleep(500);// sleep for a half second to allow the dialog
 				// to display
 				firePropertyChange("note", null, "Initializing...");
 				Thread.sleep(500);
 				// System.err.println("StartRun: " +
 				// constraints.getBoundingBox().toString2());
 				constraints.resetVariables();
 				for (String s : selPanel.getCblVars().getSelectedItems()) {
 					// constraints.addVariable(s);//orig
 					constraints.addVariable(selPanel.getVarNameFromDescription(s));
 				}
 
 				// //MOVED TO PROCESSDATALISTENER
 				// LatLonRect bounds = mapPanel.getSelectedExtent();
 				// //if no user extent, use entire extent
 				// if(bounds == null) bounds = gridReader.getBounds();//bounds =
 				// parent.getNcExtent();
 				// constraints.setBoundingBox(bounds);
 
 				NcProperties props = new NcProperties();
 				ArcType type = ArcType.NULL;
 				List<String> vars = constraints.getSelVars();
 
 				// if(makeRaster){
 				switch (selPanel.getPanelType()) {
 					case 0:// general
 						constraints.setTrimByIndex(selPanel.getTrimByIndex());
 						constraints.setUseAllValues(selPanel.isUseAllLevels());
 						break;
 					case 1:// ESRI
 						type = ArcType.FEATURE;
 						constraints.setTrimByIndex(selPanel.getTrimByIndex());
 						constraints.setUseAllValues(selPanel.isUseAllLevels());
 						props.setBandDim(constraints.getBandDim());
 						props.setTrimByDim(constraints.getTrimByDim());
 						props.setTrimByValue(selPanel.getTrimByValue());
 
 						if (selPanel.isMakeRaster()) {// RASTER
 							type = ArcType.RASTER;
 							// constraints.setBandDim((String)cbBandDim.getSelectedItem());
 							// constraints.setTrimByIndex(cbTrimBy.getSelectedIndex());
 							// constraints.setTrimByIndex(selPanel.getTrimByIndex());
 							// props.setBandDim(constraints.getBandDim());
 							// props.setTrimByDim(constraints.getTrimByDim());
 							// props.setTrimByValue(selPanel.getTrimByValue());
 
 							// ensure that the correct variable dimensions are
 							// used
 							GeoGrid grid = getGridByName(vars.get(0), false);
 							if (grid != null) {
 								constraints.setXDim(grid.getXDimension().getName());
 								constraints.setYDim(grid.getYDimension().getName());
 							}
 							// }else if(constraints.getSelVars().size() > 1){
 						} else if (selPanel.isMakeVector()) {// VECTOR
 							type = ArcType.VECTOR;
 							vars.clear();
 							props.setUVar(selPanel.getVarNameFromDescription(selPanel.getUVar()));
 							props.setVVar(selPanel.getVarNameFromDescription(selPanel.getVVar()));
 							vars.add(props.getUVar());
 							vars.add(props.getVVar());
 						}
 						break;
 					case 2:// OILMAP
 						props.setUVar(selPanel.getUVar());
 						props.setVVar(selPanel.getVVar());
 						props.setSurfLayer(selPanel.getSurfaceLevel());
 						props.setVectorType(selPanel.isVectorType());
 						break;
 				}
 
 				// ensure that the zVar tag and tVar tag are null unless one of
 				// the variables has a z dimension
 				boolean hasZ = false;
 				boolean hasT = false;
 				for (String s : vars) {
 					GeoGrid g = getGridByName(s, false);
 					if (g != null) {
 						GridCoordSystem gcs = g.getCoordinateSystem();
 						CoordinateAxis1D vAxis = gcs.getVerticalAxis();
 						if (vAxis != null) {
 							hasZ = true;
 						}
 						if (gcs.hasTimeAxis()) {
 							hasT = true;
 						}
 					}
 				}
 				if (!hasZ) {
 					constraints.setZDim("null");
 				}
 				String sTime;
 				if (!hasT) {
 					constraints.setTimeDim("null");
 					constraints.setTVar("null");
 					sTime = "null";
 				} else {
 					sTime = constraints.getStartTime().toString();
 				}
 
 				// props.setNcPath(ncPath);
 				// props.setOutPath(outname);
 				// props.setStartTime(sTime);
 				// props.setTimeInterval(constraints.getTimeInterval());
 				// props.setTimeUnits(constraints.getTimeUnits());
 				// props.setTime(constraints.getTimeDim());
 				// props.setTVar(constraints.getTVar());
 				// props.setXCoord(constraints.getXDim());
 				// props.setYCoord(constraints.getYDim());
 				// props.setZCoord(constraints.getZDim());
 				// props.setProjection(constraints.getProjection());
 				//
 				//
 				// props.setType(type);
 				// props.setVars(vars);
 
 				FileMonitor fm = new FileMonitor(ncPath);
 				fm.addPropertyChangeListener(new PropertyChangeListener() {
 
 					public void propertyChange(PropertyChangeEvent evt) {
 						double mbytes = Utils.roundDouble(Utils.Memory.Convert.bytesToMegabytes(((Long) evt
 							.getNewValue()).longValue()), 2);
 						firePropertyChange("note", null, "Writing File... : " + String.valueOf(mbytes) + " mb");
 					}
 				});
 				fm.startMonitor();
 
 				// firePropertyChange("note", null, "Writing...");
 				// Thread.sleep(250);
 				System.err.println("Processing data...");
 				// is it necessary to go through the reader??
 				// runOK = ncReader.extractData2(constraints, ncPath, gdsList,
 				// this.getPropertyChangeSupport());
 				NetcdfGridWriter writer = new NetcdfGridWriter(this.getPropertyChangeSupport());
 				runOK = writer.writeFile(ncPath, gdsList, constraints, type);
 
 				fm.stopMonitor();
 
 				// System.err.println("runOK = " + runOK);
 				// only write the props file if the nc file was generated
 				// properly
 				if (runOK == NetcdfGridWriter.SUCCESSFUL_PROCESS) {
 					firePropertyChange("note", null, "Writing properties file...");
 					Thread.sleep(250);
 
 					props.setNcPath(ncPath);
 					props.setOutPath(outname);
 					props.setStartTime(sTime);
 					props.setTimeInterval(constraints.getTimeInterval());
 					props.setTimeUnits(constraints.getTimeUnits());
 					props.setTime(constraints.getTimeDim());
 					props.setTVar(constraints.getTVar());
 					props.setXCoord(constraints.getXDim());
 					props.setYCoord(constraints.getYDim());
 					props.setZCoord(constraints.getZDim());
 					props.setProjection(constraints.getProjection());
 
 					props.setType(type);
 					props.setVars(vars);
 
 					props.setTimes(calculateTimes());
 
 					props.writeFile();
 					firePropertyChange("done", null, ncPath);
 				} else if (runOK == NetcdfGridWriter.CANCELLED_PROCESS) {
 					firePropertyChange("cancel", false, true);
 				} else if (runOK == NetcdfGridWriter.UNDEFINED_ERROR) {
 					firePropertyChange("error", false, true);
 				}
 
 			} catch (Exception ex) {
 				System.err.println("PD:run:");
 				ex.printStackTrace();
 			}
 
 			firePropertyChange("close", false, true);
 			return null;
 		}
 
 		private List<String> calculateTimes() {
 			List<String> tStrings = new ArrayList<String>();
 			Date[] times;
 			Variable tVar = null;
 			try {
 				NetcdfFile ncfile = NetcdfFile.open(ncPath);
 				tVar = ncfile.findVariable(constraints.getTVar());
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 			if (tVar == null) {
 				return tStrings;
 			}
 			try {
 				Array arr = tVar.read();
 				DateUnit du;
 				String uString = tVar.getUnitsString();
 				IndexIterator iter = arr.getIndexIterator();
 				times = new Date[(int) tVar.getSize()];
 				int i = 0;
 				double t;
 				while (iter.hasNext()) {
 					t = iter.getDoubleNext();
 					du = new DateUnit(t + " " + uString);
 					times[i] = du.getDate();
 					i++;
 				}
 
 				// removed ':ss' for timeslider compatibility
 				SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
 				df.setTimeZone(TimeZone.getTimeZone("GMT"));
 				for (Date d : times) {
 					tStrings.add(df.format(d));
 				}
 
 			} catch (Exception ex) {
 				tStrings = new ArrayList<String>();
 				ex.printStackTrace();
 			}
 			return tStrings;
 		}
 	}
 	/* Original runnable class for processing data */
 	// // class ProcessData implements Runnable {
 	// //
 	// // private String ncPath;
 	// // private String outname;
 	// //
 	// // public ProcessData(String ncpath, String out) {
 	// // ncPath = ncpath;
 	// // outname = out;
 	// // }
 	// //
 	// // public void run() {
 	// // try{
 	// //// System.err.println("StartRun: " +
 	// constraints.getBoundingBox().toString2());
 	// // constraints.resetVariables();
 	// // for(String s : selPanel.getCblVars().getSelectedItems()){
 	// // constraints.addVariable(s);
 	// // }
 	// //
 	// //// //MOVED TO PROCESSDATALISTENER
 	// //// LatLonRect bounds = mapPanel.getSelectedExtent();
 	// //// //if no user extent, use entire extent
 	// //// if(bounds == null) bounds = gridReader.getBounds();//bounds =
 	// parent.getNcExtent();
 	// //// constraints.setBoundingBox(bounds);
 	// //
 	// // NcProperties props = new NcProperties();
 	// // ArcType type = ArcType.NULL;
 	// // List<String> vars = constraints.getSelVars();
 	// //
 	// //// if(makeRaster){
 	// // switch(selPanel.getPanelType()){
 	// // case 0://general
 	// //
 	// // break;
 	// // case 1://ESRI
 	// // type = ArcType.FEATURE;
 	// // if(selPanel.isMakeRaster()){//RASTER
 	// // type = ArcType.RASTER;
 	// //// constraints.setBandDim((String)cbBandDim.getSelectedItem());
 	// //// constraints.setTrimByIndex(cbTrimBy.getSelectedIndex());
 	// // constraints.setTrimByIndex(selPanel.getTrimByIndex());
 	// // props.setBandDim(constraints.getBandDim());
 	// // props.setTrimByDim(constraints.getTrimByDim());
 	// // props.setTrimByValue(selPanel.getTrimByValue());
 	// //
 	// // //ensure that the correct variable dimensions are used
 	// // GeoGrid grid = getGridByName(vars.get(0));
 	// // if(grid != null){
 	// // constraints.setXDim(grid.getXDimension().getName());
 	// // constraints.setYDim(grid.getYDimension().getName());
 	// // }
 	// //// }else if(constraints.getSelVars().size() > 1){
 	// // }else if(selPanel.isMakeVector()){//VECTOR
 	// // type = ArcType.VECTOR;
 	// // vars.clear();
 	// // props.setUVar(selPanel.getUVar());
 	// // props.setVVar(selPanel.getVVar());
 	// // vars.add(props.getUVar());
 	// // vars.add(props.getVVar());
 	// // }
 	// // break;
 	// // case 2://OILMAP
 	// // props.setUVar(selPanel.getUVar());
 	// // props.setVVar(selPanel.getVVar());
 	// // props.setSurfLayer(selPanel.getSurfaceLevel());
 	// // props.setVectorType(selPanel.isVectorType());
 	// // break;
 	// // }
 	// //
 	// // //ensure that the zVar tag and tVar tag are null unless one of the
 	// variables has a z dimension
 	// // boolean hasZ = false;
 	// // boolean hasT = false;
 	// // for(String s : vars){
 	// // GeoGrid g = getGridByName(s);
 	// // if(g != null){
 	// // GridCoordSystem gcs = g.getCoordinateSystem();
 	// // CoordinateAxis1D vAxis = gcs.getVerticalAxis();
 	// // if(vAxis != null){
 	// // hasZ = true;
 	// // }
 	// // if(gcs.hasTimeAxis()){
 	// // hasT = true;
 	// // }
 	// // }
 	// // }
 	// // if(!hasZ){
 	// // constraints.setZDim("null");
 	// // }
 	// // String sTime;
 	// // if(!hasT){
 	// // constraints.setTimeDim("null");
 	// // constraints.setTVar("null");
 	// // sTime = "null";
 	// // }else{
 	// // sTime = constraints.getStartTime().toString();
 	// // }
 	// //
 	// // props.setNcPath(ncPath);
 	// // props.setOutPath(outname);
 	// // props.setStartTime(sTime);
 	// // props.setTimeInterval(constraints.getTimeInterval());
 	// // props.setTimeUnits(constraints.getTimeUnits());
 	// // props.setTime(constraints.getTimeDim());
 	// // props.setTVar(constraints.getTVar());
 	// // props.setXCoord(constraints.getXDim());
 	// // props.setYCoord(constraints.getYDim());
 	// // props.setZCoord(constraints.getZDim());
 	// // props.setProjection(constraints.getProjection());
 	// //
 	// //
 	// // props.setType(type);
 	// // props.setVars(vars);
 	// //
 	// // runOK = gridReader.extractData2(constraints, ncPath);
 	// // System.err.println("runOK = " + runOK);
 	// // //only write the props file if the nc file was generated properly
 	// // if(runOK){
 	// // props.writeFile();
 	// // }
 	// // }catch(Exception ex){
 	// // System.err.println("PD:run:");
 	// // ex.printStackTrace();
 	// // }
 	// // }
 	// // }
 }
