 package dk.frv.eavdam.layers;
 
 import com.bbn.openmap.InformationDelegator;
 import com.bbn.openmap.Layer;
 import com.bbn.openmap.LayerHandler;
 import com.bbn.openmap.MapBean;
 import com.bbn.openmap.event.InfoDisplayEvent;
 import com.bbn.openmap.event.MapMouseEvent;
 import com.bbn.openmap.event.MapMouseListener;
 import com.bbn.openmap.event.NavMouseMode;
 import com.bbn.openmap.event.SelectMouseMode;
 import com.bbn.openmap.gui.LayersMenu;
 import com.bbn.openmap.gui.OpenMapFrame;
 import com.bbn.openmap.layer.OMGraphicHandlerLayer;
 import com.bbn.openmap.omGraphics.OMAction;
 import com.bbn.openmap.omGraphics.OMCircle;
 import com.bbn.openmap.omGraphics.OMDistance;
 import com.bbn.openmap.omGraphics.OMGraphic;
 import com.bbn.openmap.omGraphics.OMGraphicList;
 import com.bbn.openmap.omGraphics.OMList;
 import com.bbn.openmap.omGraphics.OMPoly;
 import com.bbn.openmap.omGraphics.OMRect;
 import com.bbn.openmap.omGraphics.OMText;
 import com.bbn.openmap.proj.Length;
 import com.bbn.openmap.proj.Projection;
 import com.bbn.openmap.tools.drawing.DrawingTool;
 import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
 import com.bbn.openmap.tools.drawing.OMDrawingTool;
 import dk.frv.eavdam.app.SidePanel;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.AISFixedStationCoverage;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.AISSlotMap;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.io.XMLImporter;
 import dk.frv.eavdam.menus.EavdamMenu;
 import dk.frv.eavdam.menus.OptionsMenuItem;
 import dk.frv.eavdam.menus.SlotMapDialog;
 import dk.frv.eavdam.menus.StationInformationMenu;
 import dk.frv.eavdam.menus.StationInformationMenuItem;
 import dk.frv.eavdam.utils.DBHandler;
 import dk.frv.eavdam.utils.FATDMAUtils;
 import dk.frv.eavdam.utils.HealthCheckHandler;
 import dk.frv.eavdam.utils.RoundCoverage;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.bind.JAXBException;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import skt.swing.tree.check.*;
 
 public class StationLayer extends OMGraphicHandlerLayer implements MapMouseListener, ActionListener, WindowListener, DrawingToolRequestor {
 
 	private static final long serialVersionUID = 1L;
 	
     private MapBean mapBean;
 	private OpenMapFrame openMapFrame;
 	private LayerHandler layerHandler;
 	private LayersMenu layersMenu;
 	private OMGraphicList graphics = new OMGraphicList();
 	private InformationDelegator infoDelegator;
     private DrawingTool drawingTool;
     private final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;
 	private SidePanel sidePanel;
 	private OMAISBaseStationTransmitCoverageLayer transmitCoverageLayer;
 	private OMAISBaseStationReceiveCoverageLayer receiveCoverageLayer;
 	private OMAISBaseStationInterferenceCoverageLayer interferenceCoverageLayer;	
 	private EavdamMenu eavdamMenu;	
 	private JMenuItem editStationMenuItem;
 	private JMenuItem showOnMapMenuItem;
 	private JMenuItem hideStationMenuItem;
 	private JMenuItem showHiddenStationsMenuItem;
 	private JMenuItem generateSlotMapMenuItem;
 	private JMenuItem editTransmitCoverageMenuItem;
 	private JMenuItem resetTransmitCoverageToCircleMenuItem;
 	private JMenuItem resetTransmitCoverageToPolygonMenuItem;
 	private JMenuItem editReceiveCoverageMenuItem;
 	private JMenuItem resetReceiveCoverageToCircleMenuItem;
 	private JMenuItem resetReceiveCoverageToPolygonMenuItem;
 	private JMenuItem editInterferenceCoverageMenuItem;	
 	private JMenuItem resetInterferenceCoverageToCircleMenuItem;
 	private JMenuItem resetInterferenceCoverageToPolygonMenuItem;	
 	private OMBaseStation currentlySelectedOMBaseStation;
 	
 	private JCheckBox showStationNamesOnMapCheckBox;
 	private JCheckBox showMMSINumbersOnMapCheckBox;
 	
 	private JDialog showOnMapDialog;
 	private JTree tree;
 	private CheckTreeManager checkTreeManager;
 	private JButton okShowOnMapMenuButton;
 	private JButton cancelShowOnMapMenuButton;
 	
 	private JDialog slotMapDialog;
 	private JDialog waitDialog;
 	private JProgressBar progressBar;
 	
 	private EAVDAMData data;
 	//private int currentIcons = -1;
 	
 	private boolean stationsInitiallyUpdated = false;
 	
 	private OMGraphicHandlerLayer currentlyEditingLayer;
 	
 	private Map<OMBaseStation, OMGraphic> transmitCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 	private Map<OMBaseStation, OMGraphic> receiveCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 	private Map<OMBaseStation, OMGraphic> interferenceCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 	
 	private List<OMGraphic> hiddenBaseStations;
 	private List<OMGraphic> hiddenTransmitCoverageAreas;
 	private List<OMGraphic> hiddenReceiveCoverageAreas;
 	private List<OMGraphic> hiddenInterferenceCoverageAreas;
 	
 	public static String INITIAL_LAYERS_FILE_NAME = "initialLayers.txt";
 	private Map<String, Boolean> initiallySelectedLayerClassNames;
 	private List<Layer> initiallySelectedLayers;
 	private Map<Layer, Boolean> initiallySelectedLayersVisibilities;
 	
 	private boolean initiallySelectedShowStationNamesOnMap;
 	private boolean initiallySelectedShowMMSINumbersOnMap;
 	private Map<String, Boolean> initiallySelectedStations;
 	
 	private int currentX = -1;
 	private int currentY = -1;
 	
 	public static boolean windowReady = false;
 	
     public StationLayer() {
 		showOnMapMenuItem = new JMenuItem("Show on map");
 		showOnMapMenuItem.addActionListener(this);
 		if (showStationNamesOnMapCheckBox == null) {
 			showStationNamesOnMapCheckBox = new JCheckBox("Show station names on map");
 		}
 		if (showMMSINumbersOnMapCheckBox == null) {
 			showMMSINumbersOnMapCheckBox = new JCheckBox("Show MMSI numbers on map");
 		}
 	}
 
     public OMAISBaseStationTransmitCoverageLayer getTransmitCoverageLayer() {
         return transmitCoverageLayer;
     }
 
     public OMAISBaseStationReceiveCoverageLayer getReceiveCoverageLayer() {
         return receiveCoverageLayer;
     }
 
     public OMAISBaseStationInterferenceCoverageLayer getInterferenceCoverageLayer() {
         return interferenceCoverageLayer;
     }
 	
 	public JDialog getWaitDialog() {
 		return waitDialog;
 	}	
 
 	public JDialog getSlotMapDialog() {
 		return slotMapDialog;
 	}
 	
 	public void setSlotMapDialog(JDialog slotMapDialog) {
 		this.slotMapDialog = slotMapDialog;
 	}
 	
 	public JMenuItem getShowOnMapMenuItem() {
 		return showOnMapMenuItem;
 	}
 	
     public void addBaseStation(Object datasetSource, EAVDAMUser owner, AISFixedStationData stationData) {
 	
 	    byte[] bytearr = null;		
 		
 		if (datasetSource == null ||  // own stations
 				datasetSource instanceof String) {  // simulations
 			
 			if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_own_operative.png");				
 				}
 			
 			} else if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ||
 					stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_SIMULATED) {
 					
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_own_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_own_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_own_planned.png");
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_own_planned.png");				
 				}					
 					
 			}
 							
 		} else if (datasetSource instanceof EAVDAMUser) {  // Other user's dataset		
 		
 			if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_other_operative.png");
 				}
 			
 			} else if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ||
 					stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_SIMULATED) {
 					
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_other_planned.png");				
 				}					
 					
 			}
 		}
         
 		if (data == null) {
 			data = DBHandler.getData();
 		}
 		
 		boolean needsSaving = false;
 		
         OMBaseStation base = new OMBaseStation(datasetSource, stationData, owner, bytearr);
 		Antenna antenna = stationData.getAntenna();
 		if (antenna != null) {
 			if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
 				if (stationData.getStationType() != AISFixedStationType.RECEIVER) {
 					if (stationData.getTransmissionCoverage() != null && stationData.getTransmissionCoverage().getCoveragePoints() != null) {
 						base.setTransmitCoverageArea(stationData.getTransmissionCoverage().getCoveragePoints());
 					} else {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(),
 							stationData.getLon(), (double) antenna.getHeading().intValue(), (double) antenna.getFieldOfViewAngle().intValue(), 25);
 						base.setTransmitCoverageArea(points);
 						data = saveCoverage(data, base, points, transmitCoverageLayer);
 						needsSaving = true;
 					}
					//if (stationData.getInterferenceCoverage() != null && stationData.getInterferenceCoverage().getCoveragePoints() != null) {  // XXX: FOR TESTING
				//		base.setInterferenceCoverageArea(stationData.getInterferenceCoverage().getCoveragePoints());
					//} else {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(),
 							(double) antenna.getHeading().intValue(), (double) antenna.getFieldOfViewAngle().intValue(), 25);
 						base.setInterferenceCoverageArea(points);
 						data = saveCoverage(data, base, points, interferenceCoverageLayer);
 						needsSaving = true;					
					//}
 				}			
 				if (stationData.getReceiveCoverage() != null && stationData.getReceiveCoverage().getCoveragePoints() != null) {
 					base.setReceiveCoverageArea(stationData.getReceiveCoverage().getCoveragePoints());
 				} else {
 					ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(),
 						(double) antenna.getHeading().intValue(), (double) antenna.getFieldOfViewAngle().intValue(),25);
 					base.setReceiveCoverageArea(points);
 					data = saveCoverage(data, base, points, receiveCoverageLayer);		
 					needsSaving = true;				
 				}				
 			} else {
 				if (stationData.getStationType() != AISFixedStationType.RECEIVER) {
 					if (stationData.getTransmissionCoverage() != null && stationData.getTransmissionCoverage().getCoveragePoints() != null) {
 						base.setTransmitCoverageArea(stationData.getTransmissionCoverage().getCoveragePoints());
 					} else {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), 25);
 						base.setTransmitCoverageArea(points);
 						data = saveCoverage(data, base, points, transmitCoverageLayer);						
 						needsSaving = true;						
 					}
 					if (stationData.getInterferenceCoverage() != null && stationData.getInterferenceCoverage().getCoveragePoints() != null) {
 						base.setInterferenceCoverageArea(stationData.getInterferenceCoverage().getCoveragePoints());
 					} else {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(stationData.getLat(), stationData.getLon(), 25);
 						base.setInterferenceCoverageArea(points);
 						data = saveCoverage(data, base, points, interferenceCoverageLayer);						
 						needsSaving = true;						
 					}
 				}			
 				if (stationData.getReceiveCoverage() != null && stationData.getReceiveCoverage().getCoveragePoints() != null) {
 					base.setReceiveCoverageArea(stationData.getReceiveCoverage().getCoveragePoints());
 				} else {
 					ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), 25);
 					base.setReceiveCoverageArea(points);
 					data = saveCoverage(data, base, points, receiveCoverageLayer);	
 					needsSaving	= true;					
 				}
 			}
 						
 		}
 
 		if (needsSaving) {
 			DBHandler.saveData(data);
 		}
 		
 		graphics.add(base);
 
 		if (showStationNamesOnMapCheckBox.isSelected() || showMMSINumbersOnMapCheckBox.isSelected()) {
 			String text = "";
 			if (showStationNamesOnMapCheckBox.isSelected() && !showMMSINumbersOnMapCheckBox.isSelected()) {
 				text = stationData.getStationName();
 			} else if (showMMSINumbersOnMapCheckBox.isSelected() && !showStationNamesOnMapCheckBox.isSelected()) {
 				if (stationData.getMmsi() != null) {
 					text = stationData.getMmsi();
 				}
 			} else if (showMMSINumbersOnMapCheckBox.isSelected() && showStationNamesOnMapCheckBox.isSelected()) {
 				if (stationData.getMmsi() == null) {
 					text = stationData.getStationName();
 				} else {
 					text = stationData.getStationName() + " (" + stationData.getMmsi() + ")";					
 				}
 			}
 			if (!text.equals("")) {
 				OMText omText = new OMText(stationData.getLat()-0.2, stationData.getLon(), text, OMText.JUSTIFY_CENTER);
 				omText.setBaseline(OMText.BASELINE_MIDDLE);
 				Color c = new Color(0, 0, 0, 255);
 				omText.setLinePaint(c);		
 				graphics.add(omText);
 			}
 		}
 		
 		graphics.project(getProjection(), true);
 		this.repaint();
 		this.validate();
 
 		if (stationData.getStationType() != AISFixedStationType.RECEIVER) {	
 			Object transmitCoverageAreaGraphics = transmitCoverageLayer.addTransmitCoverageArea(base);
 			if (transmitCoverageAreaGraphics != null) {
 				if (transmitCoverageAreaGraphics instanceof OMCircle) {
 					transmitCoverageAreas.put(base, (OMCircle) transmitCoverageAreaGraphics);
 				} else if (transmitCoverageAreaGraphics instanceof OMPoly) {
 					transmitCoverageAreas.put(base, (OMPoly) transmitCoverageAreaGraphics);
 				}		
 			}
 			Object interferenceCoverageAreaGraphics = interferenceCoverageLayer.addInterferenceCoverageArea(base);
 			if (interferenceCoverageAreaGraphics != null) {
 				if (interferenceCoverageAreaGraphics instanceof OMCircle) {
 					interferenceCoverageAreas.put(base, (OMCircle) interferenceCoverageAreaGraphics);
 				} else if (interferenceCoverageAreaGraphics instanceof OMPoly) {
 					interferenceCoverageAreas.put(base, (OMPoly) interferenceCoverageAreaGraphics);
 				}		
 			}			
 		}
 		Object receiveCoverageAreaGraphics = receiveCoverageLayer.addReceiveCoverageArea(base);
 		if (receiveCoverageAreaGraphics != null) {
 			if (receiveCoverageAreaGraphics instanceof OMCircle) {
 				receiveCoverageAreas.put(base, (OMCircle) receiveCoverageAreaGraphics);
 			} else if (receiveCoverageAreaGraphics instanceof OMPoly) {
 				receiveCoverageAreas.put(base, (OMPoly) receiveCoverageAreaGraphics);
 			}		
 		}
 
 	}
 
 	@Override
 	public synchronized OMGraphicList prepare() {
 		graphics.project(getProjection(), true);
 		return graphics;
 	}
 
 	public MapMouseListener getMapMouseListener() {
 		return this;
 	}
 		
 	@Override
 	public String[] getMouseModeServiceList() {
 		String[] ret = new String[2];
 		ret[0] = NavMouseMode.modeID;
 		ret[1] = SelectMouseMode.modeID;
 		return ret;
 	}
 	
 	 public DrawingTool getDrawingTool() {
         // Usually set in the findAndInit() method.
         return drawingTool;
     }
 
     public void setDrawingTool(DrawingTool dt) {
         // Called by the findAndInit method.
         drawingTool = dt;
     }
 	
     /**
      * Called when the DrawingTool is complete, providing the layer with the
      * modified OMGraphic.
      */
 	@Override	 
     public void drawingComplete(OMGraphic omg, OMAction action) {
 	
 		if (currentlyEditingLayer != null) {
 			ArrayList<double[]> points = new ArrayList<double[]>();
 		
 			if (omg instanceof OMCircle) {
 				double[] radiuses = new double[2];
 				double degrees = ((OMCircle) omg).getRadius();
 				double radians = RoundCoverage.degrees2radians(degrees);
 				double kilometers = radians * RoundCoverage.EARTH_RADIUS;
 				radiuses[0] = kilometers;
 				radiuses[1] = kilometers;
 				points.add(radiuses);
 				
 			} else if (omg instanceof OMPoly) {					
 				double[] latlonArray = ((OMPoly) omg).getLatLonArray();
 				int index=0;
 				while (index+1<latlonArray.length) {
 					double lat = latlonArray[index];
 					double lon = latlonArray[index+1];
 					double[] latlon = new double[2];
 					latlon[0] = RoundCoverage.radians2degrees(lat);
 					latlon[1] = RoundCoverage.radians2degrees(lon);
 					points.add(latlon);
 					index = index+2;
 				}
 			}
 
 			if (data == null) {
 				data = DBHandler.getData();                        
 			}
 			data = saveCoverage(data, currentlySelectedOMBaseStation, points, currentlyEditingLayer);
 			currentlyEditingLayer = null;
 			DBHandler.saveData(data);    					
 			
 		}
 	
 /*
         Object obj = omg.getAppObject();
 
         if (obj != null && (obj == internalKey || obj == externalKey)
                 && !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {
 
             java.awt.Shape filterShape = omg.getShape();
             OMGraphicList filteredList = filter(filterShape,
                     (omg.getAppObject() == internalKey));
             if (Debug.debugging("demo")) {
                 Debug.output("DemoLayer filter: "
                         + filteredList.getDescription());
             }
         } else {
             if (!doAction(omg, action)) {
                 // null OMGraphicList on failure, should only occur if
                 // OMGraphic is added to layer before it's ever been
                 // on the map.
                 setList(new OMGraphicList());
                 doAction(omg, action);
             }
         }
 */
         repaint();
     }		
 	
 	@Override
 	public boolean mouseClicked(MouseEvent e) {
 	
 		currentX = e.getX();
 		currentY = e.getY();
 	
 		if (SwingUtilities.isLeftMouseButton(e)) {
     		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
     		for (OMGraphic omGraphic : allClosest) {
     			if (omGraphic instanceof OMBaseStation) {
     				OMBaseStation omBaseStation = (OMBaseStation) omGraphic;
     				sidePanel.showInfo(omBaseStation);
     				return true;
     			}
     		}
     	} else if (SwingUtilities.isRightMouseButton(e)) {
             OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
             if (allClosest == null || allClosest.isEmpty()) {			  
 	            JPopupMenu popup = new JPopupMenu();			
                 //popup.add(eavdamMenu.getShowOnMapMenu());
 				popup.add(showOnMapMenuItem);
 				if (hiddenBaseStations != null && !hiddenBaseStations.isEmpty()) {
 					showHiddenStationsMenuItem = new JMenuItem("Show " + hiddenBaseStations.size() + " hidden stations");
 					showHiddenStationsMenuItem.addActionListener(this);
 					popup.add(showHiddenStationsMenuItem);
 				}
 				generateSlotMapMenuItem = new JMenuItem("Generate slotmap");
 				generateSlotMapMenuItem.addActionListener(this);
 				popup.add(generateSlotMapMenuItem);
                 popup.show(mapBean, e.getX(), e.getY());
                 return true;
             } else {
         		for (OMGraphic omGraphic : allClosest) {
         			if (omGraphic instanceof OMBaseStation) {
         			    currentlySelectedOMBaseStation = (OMBaseStation) omGraphic;
         	            JPopupMenu popup = new JPopupMenu();					
                         editStationMenuItem = new JMenuItem("Edit station information");
                         editStationMenuItem.addActionListener(this);
                         popup.add(editStationMenuItem);
 						hideStationMenuItem = new JMenuItem("Hide station from map");
                         hideStationMenuItem.addActionListener(this);
                         popup.add(hideStationMenuItem);						
 						JSeparator last = new JSeparator();
 						popup.add(last);
 						if (currentlySelectedOMBaseStation.getDatasetSource() == null ||  // own stations
 								currentlySelectedOMBaseStation.getDatasetSource() instanceof String) {  // simulation	
 							AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 							Antenna antenna = stationData.getAntenna();
 							if (antenna != null && antenna.getAntennaType() == AntennaType.DIRECTIONAL) {							
 								if (currentlySelectedOMBaseStation.getStationData().getStationType() != AISFixedStationType.RECEIVER) {	
 									if (transmitCoverageLayer.isVisible() && antenna != null) {
 										editTransmitCoverageMenuItem = new JMenuItem("Edit transmit coverage");
 										editTransmitCoverageMenuItem.addActionListener(this);								
 										popup.add(editTransmitCoverageMenuItem);							
 										//resetTransmitCoverageToCircleMenuItem = new JMenuItem("Reset transmit coverage to circle");
 										//resetTransmitCoverageToCircleMenuItem.addActionListener(this);
 										//popup.add(resetTransmitCoverageToCircleMenuItem);
 										resetTransmitCoverageToPolygonMenuItem = new JMenuItem("Reset transmit coverage to polygon");
 										resetTransmitCoverageToPolygonMenuItem.addActionListener(this);
 										popup.add(resetTransmitCoverageToPolygonMenuItem);
 										last = new JSeparator();
 										popup.add(last);
 									}
 									if (interferenceCoverageLayer.isVisible()) {
 										editInterferenceCoverageMenuItem = new JMenuItem("Edit interference coverage");
 										editInterferenceCoverageMenuItem.addActionListener(this);
 										popup.add(editInterferenceCoverageMenuItem);							
 										//resetInterferenceCoverageToCircleMenuItem = new JMenuItem("Reset interference coverage to circle");
 										//resetInterferenceCoverageToCircleMenuItem.addActionListener(this);
 										//popup.add(resetInterferenceCoverageToCircleMenuItem);
 										resetInterferenceCoverageToPolygonMenuItem = new JMenuItem("Reset interference coverage to polygon");
 										resetInterferenceCoverageToPolygonMenuItem.addActionListener(this);
 										popup.add(resetInterferenceCoverageToPolygonMenuItem);
 										last = new JSeparator();
 										popup.add(last);
 									}						
 								}								
 								if (receiveCoverageLayer.isVisible() && antenna != null) {
 									editReceiveCoverageMenuItem = new JMenuItem("Edit receive coverage");
 									editReceiveCoverageMenuItem.addActionListener(this);
 									popup.add(editReceiveCoverageMenuItem);							
 									//resetReceiveCoverageToCircleMenuItem = new JMenuItem("Reset receive coverage to circle");
 									//resetReceiveCoverageToCircleMenuItem.addActionListener(this);
 									//popup.add(resetReceiveCoverageToCircleMenuItem);
 									resetReceiveCoverageToPolygonMenuItem = new JMenuItem("Reset receive coverage to polygon");
 									resetReceiveCoverageToPolygonMenuItem.addActionListener(this);
 									popup.add(resetReceiveCoverageToPolygonMenuItem);								
 									last = new JSeparator();
 									popup.add(last);
 								}
 							} else {
 								if (currentlySelectedOMBaseStation.getStationData().getStationType() != AISFixedStationType.RECEIVER) {	
 									if (transmitCoverageLayer.isVisible() && antenna != null) {
 										editTransmitCoverageMenuItem = new JMenuItem("Edit transmit coverage");
 										editTransmitCoverageMenuItem.addActionListener(this);								
 										popup.add(editTransmitCoverageMenuItem);							
 										resetTransmitCoverageToCircleMenuItem = new JMenuItem("Reset transmit coverage to circle");
 										resetTransmitCoverageToCircleMenuItem.addActionListener(this);
 										popup.add(resetTransmitCoverageToCircleMenuItem);
 										resetTransmitCoverageToPolygonMenuItem = new JMenuItem("Reset transmit coverage to polygon");
 										resetTransmitCoverageToPolygonMenuItem.addActionListener(this);
 										popup.add(resetTransmitCoverageToPolygonMenuItem);
 										last = new JSeparator();
 										popup.add(last);
 									}
 									if (interferenceCoverageLayer.isVisible()) {
 										editInterferenceCoverageMenuItem = new JMenuItem("Edit interference coverage");
 										editInterferenceCoverageMenuItem.addActionListener(this);
 										popup.add(editInterferenceCoverageMenuItem);							
 										resetInterferenceCoverageToCircleMenuItem = new JMenuItem("Reset interference coverage to circle");
 										resetInterferenceCoverageToCircleMenuItem.addActionListener(this);
 										popup.add(resetInterferenceCoverageToCircleMenuItem);
 										resetInterferenceCoverageToPolygonMenuItem = new JMenuItem("Reset interference coverage to polygon");
 										resetInterferenceCoverageToPolygonMenuItem.addActionListener(this);
 										popup.add(resetInterferenceCoverageToPolygonMenuItem);
 										last = new JSeparator();
 										popup.add(last);
 									}						
 								}								
 								if (receiveCoverageLayer.isVisible() && antenna != null) {
 									editReceiveCoverageMenuItem = new JMenuItem("Edit receive coverage");
 									editReceiveCoverageMenuItem.addActionListener(this);
 									popup.add(editReceiveCoverageMenuItem);							
 									resetReceiveCoverageToCircleMenuItem = new JMenuItem("Reset receive coverage to circle");
 									resetReceiveCoverageToCircleMenuItem.addActionListener(this);
 									popup.add(resetReceiveCoverageToCircleMenuItem);
 									resetReceiveCoverageToPolygonMenuItem = new JMenuItem("Reset receive coverage to polygon");
 									resetReceiveCoverageToPolygonMenuItem.addActionListener(this);
 									popup.add(resetReceiveCoverageToPolygonMenuItem);								
 									last = new JSeparator();
 									popup.add(last);
 								}
 							}
 						}
 						popup.remove(last);
                         popup.show(mapBean, e.getX(), e.getY());
                         return true;
                     }
                 }
             }
     	}
 		return false;
 	}
 
 	@Override
 	public boolean mouseDragged(MouseEvent e) {
 	    /*
 		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
 		for (OMGraphic omGraphic : allClosest) {
 			if (omGraphic instanceof OMBaseStation) {
 				System.out.println("Mouse dragged on omGraphic: " + omGraphic);
 				OMBaseStation r = (OMBaseStation) omGraphic;
 				Point2D p = ((MapMouseEvent) e).getLatLon();
 				r.setLatLon(p.getY(), p.getX());
 				r.generate(getProjection());
 				this.repaint();
 
 				// if (this.infoDelegator != null) {
 				// this.infoDelegator.setLabel("FOO");
 				// }
 				
 				// Consumed by this
 				return true;
 			}
 		}
 		*/
 		return false;		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {}
 
 	@Override
 	public void mouseExited(MouseEvent e) {}
 
 	@Override
 	public void mouseMoved() {}
 
 	@Override
 	public boolean mouseMoved(MouseEvent e) {
 		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
 		for (OMGraphic omGraphic : allClosest) {
 			if (omGraphic instanceof OMBaseStation) {	    
                 OMBaseStation r = (OMBaseStation) omGraphic;
                 AISFixedStationData stationData = r.getStationData();
 				String text = 
 					"Base station '" + stationData.getStationName() + "' at "
 				    + stationData.getLat()
 					+ (stationData.getLon() > 0 ? "N" : "S")
 					+ ", " + stationData.getLat()
 					+ (stationData.getLon() > 0 ? "E" : "W");
 				this.infoDelegator.requestShowToolTip(new InfoDisplayEvent(this, text));					
 				return true;
 		    }
 		}
 	    this.infoDelegator.requestHideToolTip();		
 		return false;
 	}
 
 	@Override
 	public boolean mousePressed(MouseEvent e) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseReleased(MouseEvent e) {
 		return false;
 	}
 	
 	@Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == editStationMenuItem) {
 		
 		    if (currentlySelectedOMBaseStation.getDatasetSource() == null) {
 				new StationInformationMenu(eavdamMenu, StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL + "/" +
 					StationInformationMenuItem.OPERATIVE_LABEL, currentlySelectedOMBaseStation.getName()).doClick();        
 			} else if (currentlySelectedOMBaseStation.getDatasetSource() instanceof String) {  // simulation
 				String selectedSimulation = (String) currentlySelectedOMBaseStation.getDatasetSource();
 				new StationInformationMenu(eavdamMenu, StationInformationMenuItem.SIMULATION_LABEL + ": " +
 					selectedSimulation, currentlySelectedOMBaseStation.getName()).doClick(); 
 			} else if (currentlySelectedOMBaseStation.getDatasetSource() instanceof EAVDAMUser) {  // Other user's dataset
 				String selectedOrganization = ((EAVDAMUser) currentlySelectedOMBaseStation.getDatasetSource()).getOrganizationName();
 				new StationInformationMenu(eavdamMenu, StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL + " " +
 					selectedOrganization, currentlySelectedOMBaseStation.getName()).doClick(); 							
             }
 		
 		} else if (e.getSource() == showOnMapMenuItem) {
 		
 			initiallySelectedStations = getCurrentSelections();
 		    initiallySelectedShowStationNamesOnMap = showStationNamesOnMapCheckBox.isSelected();
 			initiallySelectedShowMMSINumbersOnMap = showMMSINumbersOnMapCheckBox.isSelected();
 		
 			showOnMapDialog = new JDialog(openMapFrame, "Show on Map", false);				
 									
 			JPanel showStationNameMMSIOnMapPanel = new JPanel();
 			showStationNameMMSIOnMapPanel.setPreferredSize(new Dimension(640, 90));	
 			showStationNameMMSIOnMapPanel.setMinimumSize(new Dimension(640, 90));
 			showStationNameMMSIOnMapPanel.setMaximumSize(new Dimension(640, 90));			
 			showStationNameMMSIOnMapPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Select information to show on map")));
 			showStationNameMMSIOnMapPanel.setLayout(new BoxLayout(showStationNameMMSIOnMapPanel, BoxLayout.PAGE_AXIS));			
 			showStationNameMMSIOnMapPanel.add(showStationNamesOnMapCheckBox);
 			showStationNameMMSIOnMapPanel.add(showMMSINumbersOnMapCheckBox);	
 			
 			createTree();
 			
 			JScrollPane scrollPane = new JScrollPane(tree);
 			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 			scrollPane.setPreferredSize(new Dimension(600, 540));	
 			scrollPane.setMinimumSize(new Dimension(600, 540));
 			scrollPane.setMaximumSize(new Dimension(600, 540));				
 			scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Select stations to show on map")));
 			
 			JPanel containerPanel = new JPanel();
 			containerPanel.setLayout(new BorderLayout());
 			containerPanel.add(showStationNameMMSIOnMapPanel, BorderLayout.NORTH);
 			containerPanel.add(scrollPane, BorderLayout.CENTER);			
 			
 			showOnMapDialog.getContentPane().add(containerPanel, BorderLayout.CENTER);
 		
 			okShowOnMapMenuButton = new JButton("Ok");
 			okShowOnMapMenuButton.addActionListener(this);
 			cancelShowOnMapMenuButton = new JButton("Cancel");
 			cancelShowOnMapMenuButton.addActionListener(this);		
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
 			buttonPanel.add(okShowOnMapMenuButton);
 			buttonPanel.add(cancelShowOnMapMenuButton);
 			showOnMapDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 		
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 			showOnMapDialog.setBounds((int) screenSize.getWidth()/2 - 640/2, (int) screenSize.getHeight()/2 - 640/2, 640, 640);
 
 			showOnMapDialog.setVisible(true);		
 		
 		} else if (e.getSource() == okShowOnMapMenuButton) {
 			updateStations();
 			showOnMapDialog.dispose();
 			
 		} else if (e.getSource() == cancelShowOnMapMenuButton) {
 			restoreInitiallySelectedStations();
 		    showStationNamesOnMapCheckBox.setSelected(initiallySelectedShowStationNamesOnMap);
 			showMMSINumbersOnMapCheckBox.setSelected(initiallySelectedShowMMSINumbersOnMap);
 			showOnMapDialog.dispose();			
 		
 		} else if (e.getSource() == hideStationMenuItem) {
 
 			if (hiddenBaseStations == null) {
 				hiddenBaseStations = new ArrayList<OMGraphic>();
 			}
 			hiddenBaseStations.add(currentlySelectedOMBaseStation);
 			currentlySelectedOMBaseStation.setVisible(false);			
 			if (hiddenTransmitCoverageAreas == null) {
 				hiddenTransmitCoverageAreas = new ArrayList<OMGraphic>();
 			}
 			if (transmitCoverageAreas != null && transmitCoverageAreas.containsKey(currentlySelectedOMBaseStation)) {			
 				hiddenTransmitCoverageAreas.add(transmitCoverageAreas.get(currentlySelectedOMBaseStation));
 				transmitCoverageAreas.get(currentlySelectedOMBaseStation).setVisible(false);
 			}
 			if (hiddenReceiveCoverageAreas == null) {
 				hiddenReceiveCoverageAreas = new ArrayList<OMGraphic>();
 			}
 			if (receiveCoverageAreas != null && receiveCoverageAreas.containsKey(currentlySelectedOMBaseStation)) {				
 				hiddenReceiveCoverageAreas.add(receiveCoverageAreas.get(currentlySelectedOMBaseStation));
 				receiveCoverageAreas.get(currentlySelectedOMBaseStation).setVisible(false);			
 			}
 			if (hiddenInterferenceCoverageAreas == null) {
 				hiddenInterferenceCoverageAreas = new ArrayList<OMGraphic>();
 			}
 			if (interferenceCoverageAreas != null && interferenceCoverageAreas.containsKey(currentlySelectedOMBaseStation)) {				
 				hiddenInterferenceCoverageAreas.add(interferenceCoverageAreas.get(currentlySelectedOMBaseStation));
 				interferenceCoverageAreas.get(currentlySelectedOMBaseStation).setVisible(false);			
 			}
             this.repaint();
 		    this.validate();
             transmitCoverageLayer.repaint();
 		    transmitCoverageLayer.validate();
             receiveCoverageLayer.repaint();
 		    receiveCoverageLayer.validate();
 	        interferenceCoverageLayer.repaint();
 		    interferenceCoverageLayer.validate();				
 
 		} else if (e.getSource() == showHiddenStationsMenuItem) {
 		
 			for (OMGraphic omGraphic : hiddenBaseStations) {
 				omGraphic.setVisible(true);
 			}
 			hiddenBaseStations.clear();
 			for (OMGraphic omGraphic : hiddenTransmitCoverageAreas) {
 				omGraphic.setVisible(true);
 			}
 			hiddenTransmitCoverageAreas.clear();
 			for (OMGraphic omGraphic : hiddenReceiveCoverageAreas) {
 				omGraphic.setVisible(true);
 			}
 			hiddenReceiveCoverageAreas.clear();
 			for (OMGraphic omGraphic : hiddenInterferenceCoverageAreas) {
 				omGraphic.setVisible(true);
 			}
 			hiddenInterferenceCoverageAreas.clear();
             this.repaint();
 		    this.validate();
             transmitCoverageLayer.repaint();
 		    transmitCoverageLayer.validate();
             receiveCoverageLayer.repaint();
 		    receiveCoverageLayer.validate();
 	        interferenceCoverageLayer.repaint();
 		    interferenceCoverageLayer.validate();	
 			
 		} else if (e.getSource() == generateSlotMapMenuItem) {
 		
 			if (currentX != -1 && currentY != -1) {		
 				
 				Projection projection = mapBean.getProjection();
 				Point2D point = projection.inverse(currentX, currentY);
 				double latitude = point.getY();
 				double longitude = point.getX();
 				
 				if (data == null) {
 					data = DBHandler.getData();                        
 				}					
 				
 				slotMapDialog = null;
 				new GetSlotMapDialogThread(this, data, openMapFrame, latitude, longitude).start();
 
 				waitDialog = new JDialog(openMapFrame, "Please wait...", true);
 
 				progressBar = new JProgressBar();
 				progressBar = new JProgressBar();
 				progressBar.setIndeterminate(true);		
 				progressBar.setPreferredSize(new Dimension(330, 20));
 				progressBar.setMaximumSize(new Dimension(330, 20));
 				progressBar.setMinimumSize(new Dimension(330, 20));					
 				
 				JPanel panel = new JPanel();							
 				panel.setLayout(new GridBagLayout());
 				GridBagConstraints c = new GridBagConstraints();
 				c.gridx = 0;
 				c.gridy = 0;                   
 				c.anchor = GridBagConstraints.FIRST_LINE_START;
 				c.insets = new Insets(10,10,10,10);			
 				JLabel titleLabel = new JLabel("<html><body><p>Please wait, while the slotmap is being created...<p></body></html>");
 				titleLabel.setPreferredSize(new Dimension(330, 15));
 				titleLabel.setMaximumSize(new Dimension(330, 15));
 				titleLabel.setMinimumSize(new Dimension(330, 15));
 				panel.add(titleLabel, c);
 				c.gridy = 1;
 				c.anchor = GridBagConstraints.CENTER;
 				panel.add(progressBar, c);
 				waitDialog.getContentPane().add(panel);
 				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 				waitDialog.setBounds((int) screenSize.getWidth()/2 - 380/2, (int) screenSize.getHeight()/2 - 150/2, 380, 150);
 				new WaitThread(this).start();
 				waitDialog.setVisible(true);
 			}
 			
 		} else if (e.getSource() == editTransmitCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(transmitCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = transmitCoverageLayer;
 			}
 
 		} else if (e.getSource() == editReceiveCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(receiveCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = receiveCoverageLayer;
 			}
 
 		} else if (e.getSource() == editInterferenceCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(interferenceCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = interferenceCoverageLayer;
 			}
 			
 		} else if (e.getSource() == resetTransmitCoverageToCircleMenuItem) {
 		
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the transmit coverage to a circle calculated\n" +
 				"based on the station's antenna information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 					
 				AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 				Antenna antenna = stationData.getAntenna();
 				if (antenna != null) {							
 					ArrayList<double[]> points = new ArrayList<double[]>();
 					double radius = RoundCoverage.getRoundCoverageRadius(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4);
 					double[] radiuses = new double[2];
 					radiuses[0] = radius;
 					radiuses[1] = radius;
 					points.add(radiuses);
 
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, currentlySelectedOMBaseStation, points, transmitCoverageLayer);
                 }
                       
 				DBHandler.saveData(data);    					
 				updateStations();
                 
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		} else if (e.getSource() == resetTransmitCoverageToPolygonMenuItem) {
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the transmit coverage to a polygon calculated\n" +
 				"based on the station's antenna information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					Antenna antenna = stationData.getAntenna();
 					if (antenna != null) {
 						ArrayList<double[]> points = null;
 						if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4,
 								stationData.getLat(), stationData.getLon(), (double) antenna.getHeading().intValue(), (double) antenna.getFieldOfViewAngle().intValue(), numberOfPoints);								
 						} else {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4,
 								stationData.getLat(), stationData.getLon(), numberOfPoints);
 						}
 						if (data == null) {
 							data = DBHandler.getData();                        
 						}					
 						data = saveCoverage(data, currentlySelectedOMBaseStation, points, transmitCoverageLayer);
 					}
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}		
 		
 		} else if (e.getSource() == resetReceiveCoverageToCircleMenuItem) {
 
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the receive coverage to a circle calculated\n" +
 				"based on the station's antenna information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 					
 				Antenna antenna = currentlySelectedOMBaseStation.getStationData().getAntenna();
 				if (antenna != null) {							
 					ArrayList<double[]> points = new ArrayList<double[]>();
 					double radius = RoundCoverage.getRoundCoverageRadius(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4);
 					double[] radiuses = new double[2];
 					radiuses[0] = radius;
 					radiuses[1] = radius;
 					points.add(radiuses);
 
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, currentlySelectedOMBaseStation, points, receiveCoverageLayer);
                 }
                       
 				DBHandler.saveData(data);    					
 				updateStations();
 			
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		} else if (e.getSource() == resetReceiveCoverageToPolygonMenuItem) {
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the receive coverage to a polygon calculated\n" +
 				"based on the station's antenna information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					Antenna antenna = stationData.getAntenna();
 					if (antenna != null) {
 						ArrayList<double[]> points = null;
 						if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4,
 								stationData.getLat(), stationData.getLon(), (double) antenna.getHeading().intValue(), (double) antenna.getFieldOfViewAngle().intValue(), numberOfPoints);								
 						} else {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4,
 								stationData.getLat(), stationData.getLon(), numberOfPoints);
 						}					
 						if (data == null) {
 							data = DBHandler.getData();                        
 						}					
 						data = saveCoverage(data, currentlySelectedOMBaseStation, points, receiveCoverageLayer);
 					}
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}				
 		
 		} else if (e.getSource() == resetInterferenceCoverageToCircleMenuItem) {
 		
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the interference coverage to a circle calculated\n" +
 				"based on the station's information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 
 			ArrayList<double[]> points = new ArrayList<double[]>();
 				double radius = 120*1.852;
 				double[] radiuses = new double[2];
 				radiuses[0] = radius;
 				radiuses[1] = radius;
 				points.add(radiuses);
 
 				if (data == null) {
 					data = DBHandler.getData();                        
 				}					
 				data = saveCoverage(data, currentlySelectedOMBaseStation, points, interferenceCoverageLayer);                
                       
 				DBHandler.saveData(data);    					
 				updateStations();                
 				
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		
 		} else if (e.getSource() == resetInterferenceCoverageToPolygonMenuItem) {				
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the interference coverage to a polygon calculated\n" +
 				"based on the station's information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					Antenna antenna = stationData.getAntenna();
 					ArrayList<double[]> points = null;
 					if (antenna != null) {						
 						if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4,
 								stationData.getLat(), stationData.getLon(), (double) antenna.getHeading().intValue(),
 								(double) antenna.getFieldOfViewAngle().intValue(), numberOfPoints);								
 						} else {
 							points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(stationData.getLat(), stationData.getLon(), numberOfPoints);
 						}					
 					} else {					
 						points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(stationData.getLat(), stationData.getLon(), numberOfPoints);
 					}
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, currentlySelectedOMBaseStation, points, interferenceCoverageLayer);					
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}				
 		
         } else {
             updateStations();
         }
     }
 	
 	private EAVDAMData saveCoverage(EAVDAMData data, OMBaseStation baseStation, ArrayList<double[]> points, OMGraphicHandlerLayer activeLayer) {
 		if (baseStation.getDatasetSource() == null) {
 			List<ActiveStation> activeStations = data.getActiveStations();
 			if (activeStations != null) {
 				for (int i=0; i< activeStations.size(); i++) {
 					ActiveStation as = activeStations.get(i);
 					if (as.getStations() != null) {
 						for (int j=0; j<as.getStations().size(); j++) {
 							AISFixedStationData stationData = as.getStations().get(j);
 							if (stationData.getStationDBID() == baseStation.getStationData().getStationDBID()) {
 								if (activeLayer == transmitCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getTransmissionCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setTransmissionCoverage(coverage);
 								} else if (activeLayer == receiveCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getReceiveCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setReceiveCoverage(coverage);
 								} else if (activeLayer == interferenceCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getInterferenceCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setInterferenceCoverage(coverage);										
 								}								
 								as.getStations().set(j, stationData);
 								data.getActiveStations().set(i, as);		
 								break;									
 							}
 						}
 					}
 				}
 			}
 		} else if (baseStation.getDatasetSource() instanceof String) {  // simulation
 			String selectedSimulation = (String) baseStation.getDatasetSource();				
 			List<Simulation> simulatedStations = data.getSimulatedStations();
 			for (Simulation s : data.getSimulatedStations()) {
 				if (selectedSimulation.equals(s.getName())) {
 					List<AISFixedStationData> stations = s.getStations();
 					for (int i=0; i<stations.size(); i++) {
 						AISFixedStationData stationData = stations.get(i);
 						if (stationData.getStationDBID() == baseStation.getStationData().getStationDBID()) {								
 							if (activeLayer == transmitCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getTransmissionCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setTransmissionCoverage(coverage);
 							} else if (activeLayer == receiveCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getReceiveCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setReceiveCoverage(coverage);
 							} else if (activeLayer == interferenceCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getInterferenceCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setInterferenceCoverage(coverage);										
 							}							
 							stations.set(i, stationData);
 							s.setStations(stations);
 							data.setSimulatedStations(simulatedStations);
 							break;																	
 						}
 					}
 				}	
 			}
 		} else if (baseStation.getDatasetSource() instanceof EAVDAMUser) {  // other user's station
 			EAVDAMUser user = (EAVDAMUser) baseStation.getDatasetSource();                
 			List<OtherUserStations> otherUserStations = data.getOtherUsersStations();
             if (otherUserStations != null && !otherUserStations.isEmpty()) {
 				for (int i=0; i<otherUserStations.size(); i++) {
 					OtherUserStations ous = otherUserStations.get(i);
                     if (ous.getUser().getOrganizationName().equals(user.getOrganizationName())) {
 						List<ActiveStation> activeStations = ous.getStations();
 						if (activeStations != null) {
 							for (int j=0; j<activeStations.size(); j++) {
 								ActiveStation as = activeStations.get(j);
 								List<AISFixedStationData> stations = as.getStations();
 								if (stations != null) {
 									for (int k=0; k<stations.size(); k++) {
 									AISFixedStationData stationData = stations.get(k);								
 									if (stationData.getStationDBID() == baseStation.getStationData().getStationDBID()) {								
 										if (activeLayer == transmitCoverageLayer) {
 												AISFixedStationCoverage coverage = stationData.getTransmissionCoverage();
 												if (coverage == null) {
 													coverage = new AISFixedStationCoverage();
 												}
 												coverage.setCoveragePoints(points);
 												stationData.setTransmissionCoverage(coverage);
 											} else if (activeLayer == receiveCoverageLayer) {
 												AISFixedStationCoverage coverage = stationData.getReceiveCoverage();
 												if (coverage == null) {
 													coverage = new AISFixedStationCoverage();
 												}
 												coverage.setCoveragePoints(points);
 												stationData.setReceiveCoverage(coverage);
 											} else if (activeLayer == interferenceCoverageLayer) {
 												AISFixedStationCoverage coverage = stationData.getInterferenceCoverage();
 												if (coverage == null) {
 													coverage = new AISFixedStationCoverage();
 												}
 												coverage.setCoveragePoints(points);
 												stationData.setInterferenceCoverage(coverage);															
 											}							
 											stations.set(k, stationData);
 											as.setStations(stations);
 											activeStations.set(j, as);
 											ous.setStations(activeStations);
 											otherUserStations.set(i, ous);
 											data.setOtherUsersStations(otherUserStations);
 											break;																	
 										}
 									}
 								}
 							}
 						}
 					}
 				}	
 			}
 		}
 		
 		return data;	
 	}
 	
 	private void createTree() {
 			
 		// loads earlier selections
 		Map<String, Boolean> currentSelections = getCurrentSelections();
 	
 		if (data == null) {
 			data = DBHandler.getData(); 
 		}
 		
 		List<String> operativeStations = new ArrayList<String>();
 		List<String> plannedStations = new ArrayList<String>();
 		
 		List<ActiveStation> activeStations = data.getActiveStations();
 		if (activeStations != null) {
 			for (ActiveStation as : activeStations) {
 				if (as.getStations() != null) {
 					for (AISFixedStationData stationData : as.getStations()) {
 						if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 							operativeStations.add(stationData.getStationName());
 						}
 						if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
 							plannedStations.add(stationData.getStationName());
 						}
 					}
 				}
 			}
 		}	
 
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("All stations");
 				
 		if (!operativeStations.isEmpty()) {
 			DefaultMutableTreeNode node = new DefaultMutableTreeNode("Own operative stations");
 			root.add(node);
 			for (String s : operativeStations) {
 				DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(s);
 				node.add(node2);				
 			}		
 		}
 		
 		if (!plannedStations.isEmpty()) {
 			DefaultMutableTreeNode node = new DefaultMutableTreeNode("Own planned stations");
 			root.add(node);
 			for (String s : plannedStations) {
 				DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(s);			
 				node.add(node2);
 			}		
 		}			
 		
 		if (data.getSimulatedStations() != null) {
 			for (Simulation s : data.getSimulatedStations()) {
 				DefaultMutableTreeNode node = new DefaultMutableTreeNode("Simulation: " + s.getName());
 				root.add(node);
 				List<AISFixedStationData> stations = s.getStations();
 				for (AISFixedStationData stationData : stations) {
 					DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(stationData.getStationName());
 					node.add(node2);
 				}   
 			}
 		}
 		
 		if (data.getOtherUsersStations() != null) {
 			for (OtherUserStations ous : data.getOtherUsersStations()) {
 				EAVDAMUser user = ous.getUser();
 				DefaultMutableTreeNode node = new DefaultMutableTreeNode("Stations of organization: " + user.getOrganizationName());
 				root.add(node);					
 				List<ActiveStation> otherUsersActiveStations = ous.getStations();
 				for (ActiveStation as : otherUsersActiveStations) {
 					List<AISFixedStationData> stations = as.getStations();
 					for (AISFixedStationData station : stations) {
 						if (station.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 							DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(station.getStationName());
 							node.add(node2);														
 						}
 					}
 				}	
 			}
 		}
 		
 		tree = new JTree(root);
 		
 		checkTreeManager = new CheckTreeManager(tree, true, null);
 		
 		CheckTreeSelectionModel selectionModel = checkTreeManager.getSelectionModel();
 		
 		TreeModel model = tree.getModel();
 
 		if (model != null) {
 			Object treeRoot = model.getRoot();
 			for (int i=0; i<model.getChildCount(treeRoot); i++) {
 				Object child = model.getChild(treeRoot, i);
 				if (child instanceof DefaultMutableTreeNode) {
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
 					if (node.toString().equals("Own operative stations")) {								
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (currentSelections != null && currentSelections.containsKey("Own operative stations /// " + node2.toString())) {
 									if (currentSelections.get("Own operative stations /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								} else {
 									Object[] paths = new Object[3];
 									paths[0] = treeRoot;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath[] selectionPaths = new TreePath[1];
 									selectionPaths[0] = new TreePath(paths);
 									selectionModel.addSelectionPaths(selectionPaths);
 								}
 							}							
 						}
 					} else if (node.toString().equals("Own planned stations")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (currentSelections != null && currentSelections.containsKey("Own planned stations /// " + node2.toString())) {
 									if (currentSelections.get("Own planned stations /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}									
 							}
 						}							
 					} else if (node.toString().startsWith("Simulation: ")) {							
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (currentSelections != null && currentSelections.containsKey(node.toString() + " /// " + node2.toString())) {
 									if (currentSelections.get(node.toString() + " /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}																
 							}
 						}	
 					} else if (node.toString().startsWith("Stations of organization: ")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (currentSelections != null && currentSelections.containsKey(node.toString() + " /// " + node2.toString())) {
 									if (currentSelections.get(node.toString() + " /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								} else {									
 									Object[] paths = new Object[3];
 									paths[0] = treeRoot;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath[] selectionPaths = new TreePath[1];
 									selectionPaths[0] = new TreePath(paths);
 									selectionModel.addSelectionPaths(selectionPaths);
 								}	
 							}							
 						}
 					}
 				}
 			}
 		}		
 	}
 
 	private Map<String, Boolean> getCurrentSelections() {
 	
 		Map<String, Boolean> currentSelections = new HashMap<String, Boolean>();
 		
 		if (tree != null && checkTreeManager != null) {				
 			TreeModel model = tree.getModel();
 			TreePath checkedPaths[] = checkTreeManager.getSelectionModel().getSelectionPaths(); 
 			if (model != null) {
 				Object root = model.getRoot();
 				for (int i=0; i<model.getChildCount(root); i++) {
 					Object child = model.getChild(root, i);
 					if (child instanceof DefaultMutableTreeNode) {
 						DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
 						if (node.toString().equals("Own operative stations")) {								
 							for (int j=0; j<model.getChildCount(child); j++) {
 								Object child2 = model.getChild(child, j);
 								if (child2 instanceof DefaultMutableTreeNode) {
 									DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 									Object[] paths = new Object[3];
 									paths[0] = root;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath temp = new TreePath(paths);
 									boolean isSelected = false;
 									for (int k=0; k<checkedPaths.length; k++) {
 										if (temp.equals(checkedPaths[k]) || isDescendant(temp, checkedPaths[k])) {			
 											isSelected = true;
 											break;
 										}
 									}
 									if (isSelected) {
 										currentSelections.put("Own operative stations /// " + node2.toString(), new Boolean(true));										
 									} else {
 										currentSelections.put("Own operative stations /// " + node2.toString(), new Boolean(false));
 									}
 								}
 							}							
 						} else if (node.toString().equals("Own planned stations")) {
 							for (int j=0; j<model.getChildCount(child); j++) {
 								Object child2 = model.getChild(child, j);
 								if (child2 instanceof DefaultMutableTreeNode) {
 									DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 									Object[] paths = new Object[3];
 									paths[0] = root;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath temp = new TreePath(paths);
 									boolean isSelected = false;
 									for (int k=0; k<checkedPaths.length; k++) {
 										if (temp.equals(checkedPaths[k]) || isDescendant(temp, checkedPaths[k])) {											
 											isSelected = true;
 											break;
 										}
 									}
 									if (isSelected) {
 										currentSelections.put("Own planned stations /// " + node2.toString(), new Boolean(true));	
 									} else {
 										currentSelections.put("Own planned stations /// " + node2.toString(), new Boolean(false));
 									}																	
 								}
 							}							
 						} else if (node.toString().startsWith("Simulation: ")) {							
 							for (int j=0; j<model.getChildCount(child); j++) {
 								Object child2 = model.getChild(child, j);
 								if (child2 instanceof DefaultMutableTreeNode) {
 									DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 									Object[] paths = new Object[3];
 									paths[0] = root;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath temp = new TreePath(paths);
 									boolean isSelected = false;
 									for (int k=0; k<checkedPaths.length; k++) {
 										if (temp.equals(checkedPaths[k]) || isDescendant(temp, checkedPaths[k])) {									
 											isSelected = true;
 											break;
 										}
 									}
 									if (isSelected) {
 										currentSelections.put(node.toString() + " /// " + node2.toString(), new Boolean(true));										
 									} else {
 										currentSelections.put(node.toString() + " /// " + node2.toString(), new Boolean(false));
 									}																	
 								}
 							}	
 						} else if (node.toString().startsWith("Stations of organization: ")) {
 							for (int j=0; j<model.getChildCount(child); j++) {
 								Object child2 = model.getChild(child, j);
 								if (child2 instanceof DefaultMutableTreeNode) {
 									DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 									Object[] paths = new Object[3];
 									paths[0] = root;
 									paths[1] = node;
 									paths[2] = node2;
 									TreePath temp = new TreePath(paths);
 									boolean isSelected = false;
 									for (int k=0; k<checkedPaths.length; k++) {
 										if (temp.equals(checkedPaths[k]) || isDescendant(temp, checkedPaths[k])) {										
 											isSelected = true;
 											break;
 										}
 									}
 									if (isSelected) {
 										currentSelections.put(node.toString() + " /// " + node2.toString(), new Boolean(true));										
 									} else {
 										currentSelections.put(node.toString() + " /// " + node2.toString(), new Boolean(false));
 									}										
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return currentSelections;
 	}
 	
 	private void restoreInitiallySelectedStations() {
 			
 		CheckTreeSelectionModel selectionModel = checkTreeManager.getSelectionModel();
 		
 		TreeModel model = tree.getModel();
 		
 		if (model != null) {
 			Object treeRoot = model.getRoot();
 			
 			// removes current selections			
 			
 			for (int i=0; i<model.getChildCount(treeRoot); i++) {
 				Object child = model.getChild(treeRoot, i);
 				if (child instanceof DefaultMutableTreeNode) {
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
 					if (node.toString().equals("Own operative stations")) {								
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								Object[] obj = new Object[3];
 								obj[0] = treeRoot;
 								obj[1] = node;
 								obj[2] = node2;
 								TreePath[] paths = new TreePath[1];
 								paths[0] = new TreePath(obj);
 								selectionModel.removeSelectionPaths(paths);
 							}							
 						}
 					} else if (node.toString().equals("Own planned stations")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								Object[] obj = new Object[3];
 								obj[0] = treeRoot;
 								obj[1] = node;
 								obj[2] = node2;
 								TreePath[] paths = new TreePath[1];
 								paths[0] = new TreePath(obj);
 								selectionModel.removeSelectionPaths(paths);									
 							}
 						}							
 					} else if (node.toString().startsWith("Simulation: ")) {							
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								Object[] obj = new Object[3];
 								obj[0] = treeRoot;
 								obj[1] = node;
 								obj[2] = node2;
 								TreePath[] paths = new TreePath[1];
 								paths[0] = new TreePath(obj);
 								selectionModel.removeSelectionPaths(paths);																
 							}
 						}	
 					} else if (node.toString().startsWith("Stations of organization: ")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								Object[] obj = new Object[3];
 								obj[0] = treeRoot;
 								obj[1] = node;
 								obj[2] = node2;
 								TreePath[] paths = new TreePath[1];
 								paths[0] = new TreePath(obj);
 								selectionModel.removeSelectionPaths(paths);	
 							}							
 						}
 					}
 				}
 			}				
 		
 			// adds initial selections
 		
 			for (int i=0; i<model.getChildCount(treeRoot); i++) {
 				Object child = model.getChild(treeRoot, i);
 				if (child instanceof DefaultMutableTreeNode) {
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
 					if (node.toString().equals("Own operative stations")) {								
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (initiallySelectedStations != null && initiallySelectedStations.containsKey("Own operative stations /// " + node2.toString())) {
 									if (initiallySelectedStations.get("Own operative stations /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}
 							}							
 						}
 					} else if (node.toString().equals("Own planned stations")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (initiallySelectedStations != null && initiallySelectedStations.containsKey("Own planned stations /// " + node2.toString())) {
 									if (initiallySelectedStations.get("Own planned stations /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}									
 							}
 						}							
 					} else if (node.toString().startsWith("Simulation: ")) {							
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (initiallySelectedStations != null && initiallySelectedStations.containsKey(node.toString() + " /// " + node2.toString())) {
 									if (initiallySelectedStations.get(node.toString() + " /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}																
 							}
 						}	
 					} else if (node.toString().startsWith("Stations of organization: ")) {
 						for (int j=0; j<model.getChildCount(child); j++) {
 							Object child2 = model.getChild(child, j);
 							if (child2 instanceof DefaultMutableTreeNode) {
 								DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) child2;
 								if (initiallySelectedStations != null && initiallySelectedStations.containsKey(node.toString() + " /// " + node2.toString())) {
 									if (initiallySelectedStations.get(node.toString() + " /// " + node2.toString()).booleanValue() == true) {
 										Object[] paths = new Object[3];
 										paths[0] = treeRoot;
 										paths[1] = node;
 										paths[2] = node2;
 										TreePath[] selectionPaths = new TreePath[1];
 										selectionPaths[0] = new TreePath(paths);
 										selectionModel.addSelectionPaths(selectionPaths);
 									}
 								}
 							}							
 						}
 					}
 				}
 			}		
 		}	
 	}
 	
 	@Override
 	public void findAndInit(Object obj) {
 		if (initiallySelectedLayerClassNames == null) {
 			initiallySelectedLayerClassNames = new HashMap<String, Boolean>();			
 			try {
 				FileInputStream fstream = new FileInputStream(StationLayer.INITIAL_LAYERS_FILE_NAME);
 				DataInputStream in = new DataInputStream(fstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(in));
 				String strLine;
 				while ((strLine = br.readLine()) != null) {
 					String[] temp = strLine.split(":");
 					if (temp.length == 2) {
 						String layerClassName = temp[0].trim();
 						String visibility = temp[1].trim();
 						initiallySelectedLayerClassNames.put(layerClassName, new Boolean(visibility));
 					}
 				}			
 				in.close();
 			} catch (Exception e) {}
 		}
 		if (initiallySelectedLayerClassNames.containsKey(obj.getClass().getCanonicalName())) {
 			if (initiallySelectedLayers == null) {
 				initiallySelectedLayers = new ArrayList<Layer>();
 			}
 			if (initiallySelectedLayersVisibilities == null) {
 				initiallySelectedLayersVisibilities = new HashMap<Layer, Boolean>();
 			}			
 			Boolean visibility = initiallySelectedLayerClassNames.get(obj.getClass().getCanonicalName());
 			if (!initiallySelectedLayers.contains((Layer) obj)) {
 				initiallySelectedLayers.add((Layer) obj);
 				initiallySelectedLayersVisibilities.put((Layer) obj, visibility);	
 			}
 		}
 	    if (obj instanceof MapBean) {
 			this.mapBean = (MapBean) obj;
 		} else if (obj instanceof InformationDelegator) {
 			this.infoDelegator = (InformationDelegator) obj;
 		} else if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;
 			((OpenMapFrame) obj).addWindowListener(this);
 		} else if (obj instanceof OMAISBaseStationTransmitCoverageLayer) {
 			this.transmitCoverageLayer = (OMAISBaseStationTransmitCoverageLayer) obj;			
 		} else if (obj instanceof OMAISBaseStationReceiveCoverageLayer) {
 			this.receiveCoverageLayer = (OMAISBaseStationReceiveCoverageLayer) obj;
 		} else if (obj instanceof OMAISBaseStationInterferenceCoverageLayer) {
 			this.interferenceCoverageLayer = (OMAISBaseStationInterferenceCoverageLayer) obj;			
 		} else if (obj instanceof EavdamMenu) {
 		    this.eavdamMenu = (EavdamMenu) obj;
 		} else if (obj instanceof SidePanel) {
 		    this.sidePanel = (SidePanel) obj;
 		} else if (obj instanceof DrawingTool) {
             setDrawingTool((DrawingTool) obj);
         } else if (obj instanceof LayerHandler) {
 			this.layerHandler = (LayerHandler) obj;
 		} else if (obj instanceof LayersMenu) {
 			this.layersMenu = (LayersMenu) obj;
 		}
 	}
 	
 	public void	windowActivated(WindowEvent e) {}
 	
 	public void windowClosed(WindowEvent e) {}
 	
 	public void windowClosing(WindowEvent e) {
 		try {
 			new File(StationLayer.INITIAL_LAYERS_FILE_NAME).delete();
 		} catch (Exception ex) {}
 		try {
 			FileWriter fstream = new FileWriter(StationLayer.INITIAL_LAYERS_FILE_NAME);
 			BufferedWriter out = new BufferedWriter(fstream);	
 			Layer[] layers = layerHandler.getLayers();
 			for (Layer layer : layers) {
 				out.write(layer.getClass().getCanonicalName() + ": " + layer.isVisible() + System.getProperty("line.separator"));
 			}			
 			out.close();
 		} catch (Exception ex) {}	
 	}
 	
 	public void	windowDeactivated(WindowEvent e) {}
 	
 	public void	windowDeiconified(WindowEvent e) {}
 	
 	public void	windowIconified(WindowEvent e) {}
 	
 	public void	windowOpened(WindowEvent e) {
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension dimension = toolkit.getScreenSize();
 		if (dimension.width-100 < SlotMapDialog.SLOTMAP_WINDOW_WIDTH) {
 			SlotMapDialog.SLOTMAP_WINDOW_WIDTH = dimension.width-100;
 		}
 		if (dimension.height-100 < SlotMapDialog.SLOTMAP_WINDOW_HEIGHT) {
 			SlotMapDialog.SLOTMAP_WINDOW_HEIGHT = dimension.height-100;
 		}
 		Layer aisDatalinkCheckIssueLayer = null;		
 		if (initiallySelectedLayers != null && initiallySelectedLayers.size() == layerHandler.getLayers().length) {
 			layersMenu.removeAll();
 			Layer[] inLayers = new Layer[initiallySelectedLayers.size()];
 			int i = 0;
 			for (Layer layer : initiallySelectedLayers) {
 				if (layer.getClass().getCanonicalName().equals("dk.frv.eavdam.layers.AISDatalinkCheckIssueLayer")) {
 					aisDatalinkCheckIssueLayer = layer;
 				}
 				if (layer.getClass().getCanonicalName().equals("dk.frv.eavdam.layers.AISDatalinkCheckBandwidthAreasLayer")) {  // no need for this layer to be on at startup
 					layerHandler.turnLayerOn(false, layer);
 					inLayers[i] = layer;
 					i++;
 				} else {
 					boolean setting = initiallySelectedLayersVisibilities.get(layer).booleanValue();
 					layerHandler.turnLayerOn(setting, layer);
 					inLayers[i] = layer;
 					i++;
 				}
 			}
 			if (aisDatalinkCheckIssueLayer != null) {
 				layerHandler.moveLayer(aisDatalinkCheckIssueLayer, 0);		
 			}			
 			layersMenu.setLayers(inLayers);
 		}
 		if (eavdamMenu != null && transmitCoverageLayer != null && receiveCoverageLayer != null && interferenceCoverageLayer != null && sidePanel != null && !stationsInitiallyUpdated) {
 			//eavdamMenu.getShowOnMapMenu().updateCoverageItems(receiveCoverageLayer.isVisible(), transmitCoverageLayer.isVisible(), interferenceCoverageLayer.isVisible());
 			updateStations();
 			stationsInitiallyUpdated = true;
 		}
 		StationLayer.windowReady = true;
 		if (aisDatalinkCheckIssueLayer != null) {
 			((AISDatalinkCheckIssueLayer) aisDatalinkCheckIssueLayer).doPrepare();		
 		}
 	}
 
 	private byte[] getImage(String filename) {    
         try {
             File file = new File(filename); 
             int size = (int) file.length(); 
             byte[] bytes = new byte[size]; 
             DataInputStream dis = new DataInputStream(new FileInputStream(file)); 
             int read = 0;
             int numRead = 0;
             while (read < bytes.length && (numRead=dis.read(bytes, read, bytes.length-read)) >= 0) {
                 read = read + numRead;
             }
             return bytes;
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return null;
     }
 
 	private boolean isDescendant(TreePath path1, TreePath path2) {
 		Object obj1[] = path1.getPath();
         Object obj2[] = path2.getPath();
         for (int i = 0; i < obj2.length; i++)
         {
             if (obj1[i] != obj2[i])
                 return false;
         }
         return true;
 	}
 	
     public void updateStations() {
 	
 		if (eavdamMenu == null) {
 			return;
 		}
 
 		createTree();
 
         data = DBHandler.getData();                        
         if (data != null) {
 		
             Options options = OptionsMenuItem.loadOptions();
 			graphics.clear();
 			transmitCoverageLayer.getGraphicsList().clear();
 			receiveCoverageLayer.getGraphicsList().clear();
 			interferenceCoverageLayer.getGraphicsList().clear();			 
 			
 			Map<String, Boolean> currentSelections = getCurrentSelections();
 			
 			boolean anyOwnStations = false;
 			for (Object key: currentSelections.keySet()) {			
 				if (((String) key).startsWith("Own")) {
 					anyOwnStations = true;
 					break;
 				}
 			}
 			boolean anySimulations = false;
 			for (Object key: currentSelections.keySet()) {			
 				if (((String) key).startsWith("Simulation")) {
 					anySimulations = true;
 					break;
 				}
 			}			
 			boolean anyOtherUsersStations = false;
 			for (Object key: currentSelections.keySet()) {			
 				if (((String) key).startsWith("Stations of organization")) {
 					anyOtherUsersStations = true;
 					break;
 				}
 			}			
 			
 			List<ActiveStation> activeStations = data.getActiveStations();
             if (activeStations != null) {
 				for (ActiveStation as : activeStations) {
 					if (as.getStations() != null && anyOwnStations) {
 						for (AISFixedStationData stationData : as.getStations()) {
 							if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 								if (currentSelections.containsKey("Own operative stations /// " + stationData.getStationName())) {
 									if (currentSelections.get("Own operative stations /// " + stationData.getStationName()).booleanValue() == true) {
 										this.addBaseStation(null, data.getUser(), stationData);
 									}
 								} else {
 									this.addBaseStation(null, data.getUser(), stationData);								
 								}
 							} else if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
 								if (currentSelections.containsKey("Own planned stations /// " + stationData.getStationName())) {
 									if (currentSelections.get("Own planned stations /// " + stationData.getStationName()).booleanValue() == true) {
 										this.addBaseStation(null, data.getUser(), stationData);
 									}							
 								} else {
 									this.addBaseStation(null, data.getUser(), stationData);								
 								}
 							}
 						}
 					}
 				}
 			}
 
 			if (data.getSimulatedStations() != null && anySimulations) {
 				for (Simulation s : data.getSimulatedStations()) {
 					List<AISFixedStationData> stations = s.getStations();
 					for (AISFixedStationData stationData : stations) {
 						if (currentSelections.containsKey("Simulation: " + s.getName() + " /// " + stationData.getStationName())) {
 							if (currentSelections.get("Simulation: " + s.getName() + " /// " + stationData.getStationName()).booleanValue() == true) {
 								this.addBaseStation(s.getName(), data.getUser(), stationData);
 							}							
 						} else {
 							this.addBaseStation(s.getName(), data.getUser(), stationData);							
 						}
 					}
 				}
 			}   
 			
 			if (data.getOtherUsersStations() != null && anyOtherUsersStations) {
 				for (OtherUserStations ous : data.getOtherUsersStations()) {
 					EAVDAMUser user = ous.getUser();
 					List<ActiveStation> otherUsersActiveStations = ous.getStations();
 					for (ActiveStation as : otherUsersActiveStations) {
 						List<AISFixedStationData> stations = as.getStations();
 						for (AISFixedStationData station : stations) {
 							if (station.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 								if (currentSelections.containsKey("Stations of organization: " + user.getOrganizationName() + " /// " + station.getStationName())) {
 									if (currentSelections.get("Stations of organization: " + user.getOrganizationName() + " /// " + station.getStationName()).booleanValue() == true) {
 										this.addBaseStation(user, user, station);
 									}							
 								} else {
 									this.addBaseStation(user, user, station);					
 								}		
 							}
 						}
 					}
 				}
 			}
                          
             this.repaint();
 		    this.validate();
             transmitCoverageLayer.repaint();
 		    transmitCoverageLayer.validate();
             receiveCoverageLayer.repaint();
 		    receiveCoverageLayer.validate();
 	        interferenceCoverageLayer.repaint();
 		    interferenceCoverageLayer.validate();		
         }
     }
 
 }
 
 
 class GetSlotMapDialogThread extends Thread {
 	
 	StationLayer stationLayer;
 	EAVDAMData data;	
 	OpenMapFrame openMapFrame;
 	double latitude;
 	double longitude;
 	
 	GetSlotMapDialogThread(StationLayer stationLayer, EAVDAMData data, OpenMapFrame openMapFrame, double latitude, double longitude) {
 		this.stationLayer = stationLayer;
 		this.data = data;
 		this.openMapFrame = openMapFrame;
 		this.latitude = latitude;
 		this.longitude = longitude;
 	}
 	
 	public void run() {
 		HealthCheckHandler hch = new HealthCheckHandler(data);
 		AISSlotMap slotmap = hch.slotMapAtPoint(latitude, longitude, true);
 		stationLayer.setSlotMapDialog(new SlotMapDialog(openMapFrame, latitude, longitude, slotmap));
 	}
 
 }
 
 
 class WaitThread extends Thread {
 
 	StationLayer stationLayer;
 	
 	WaitThread(StationLayer stationLayer) {
 		this.stationLayer = stationLayer;
 	}
 	
 	public void run() {	
 		while (stationLayer.getSlotMapDialog() == null) {
 			try {
 				Thread.sleep(1000);							
 			} catch (InterruptedException ex) {}
 		}
 		stationLayer.getWaitDialog().dispose();
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		stationLayer.getSlotMapDialog().setBounds((int) screenSize.getWidth()/2 - SlotMapDialog.SLOTMAP_WINDOW_WIDTH/2,
 				(int) screenSize.getHeight()/2 - SlotMapDialog.SLOTMAP_WINDOW_HEIGHT/2,
 				SlotMapDialog.SLOTMAP_WINDOW_WIDTH, SlotMapDialog.SLOTMAP_WINDOW_HEIGHT);
 		stationLayer.getSlotMapDialog().setVisible(true);		
 	}
 
 }
 				
 				
