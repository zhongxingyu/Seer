 /*
  * OMSelectionMapPanel.java
  *
  * Created on December 11, 2007, 1:31 PM
  *
  * Applied Science Associates, Inc.
  * Copyright 2007.  All rights reserved.
  */
 package com.asascience.openmap.ui;
 
 import com.asascience.openmap.layer.BasemapLayer;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.Properties;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.softsmithy.lib.swing.JDoubleField;
 
 import ucar.unidata.geoloc.LatLonPointImpl;
 import ucar.unidata.geoloc.LatLonRect;
 
 import com.asascience.openmap.layer.ExtentRectangleLayer;
 import com.asascience.openmap.mousemode.AreaSelectionMouseMode;
 import com.asascience.openmap.mousemode.InformationMouseMode;
 import com.asascience.openmap.mousemode.MeasureMouseMode;
 import com.asascience.openmap.mousemode.NavMouseMode3;
 import com.asascience.openmap.mousemode.PanMouseMode2;
 import com.asascience.openmap.utilities.GeoConstraints;
 import com.asascience.sos.SensorContainer;
 import com.asascience.sos.SosLayer;
 import com.asascience.utilities.NumFieldUtilities;
 import com.asascience.utilities.Utils;
 import com.bbn.openmap.InformationDelegator;
 import com.bbn.openmap.LatLonPoint;
 import com.bbn.openmap.LayerHandler;
 import com.bbn.openmap.MapBean;
 import com.bbn.openmap.MapHandler;
 import com.bbn.openmap.MouseDelegator;
 import com.bbn.openmap.gui.BasicMapPanel;
 import com.bbn.openmap.gui.MouseModeButtonPanel;
 import com.bbn.openmap.gui.OMToolSet;
 import com.bbn.openmap.gui.ProjectionStackTool;
 import com.bbn.openmap.gui.ToolPanel;
 import com.bbn.openmap.layer.shape.ShapeLayer;
 import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
 import com.bbn.openmap.proj.ProjectionStack;
 import com.bbn.openmap.util.DataBounds;
 import java.util.List;
 
 /**
  * 
  * @author CBM
  */
 public class OMSelectionMapPanel extends BasicMapPanel implements PropertyChangeListener {
 
   public static final String AOI_CLEAR = "aoiclear";
   public static final String AOI_SAVE = "aoisave";
   public static final String AOI_APPLY = "aoiapply";
   public static final String AOI_REMALL = "aoiremall";
   public static final String AOI_MANUAL = "aoimanual";
   protected PropertyChangeSupport pcs;
   protected GeoConstraints geoCons;
   protected String dataDir;
   protected MapHandler mHandler;
   private LayerHandler layerHandler;
   private ShapeLayer basemapLayer;
   private SosLayer sensorLayer;
   protected MapBean mBean;
   protected Properties lyrProps;
   protected OMToolSet tools;
   protected ToolPanel toolPanel;
   protected MouseDelegator mouseDelegator;
   protected AreaSelectionMouseMode asmm;
   protected ExtentRectangleLayer selectedExtent;
   protected ExtentRectangleLayer dataExtent;
   protected boolean showAOIButton = false;
 
   /**
    * Creates a new instance of OMSelectionMapPanel
    *
    * @param cons
    * @param dataDir
    * @param showAOIButton
    */
   public OMSelectionMapPanel(GeoConstraints cons, String dataDir, boolean showAOIButton) {
     this.dataDir = Utils.appendSeparator(dataDir);
     this.showAOIButton = showAOIButton;
     geoCons = cons;
     if (geoCons == null) {
       System.err.println("geoCons == null");
     }
 
     pcs = new PropertyChangeSupport(this);
 
     initComponents();
   }
 
   public OMSelectionMapPanel(GeoConstraints cons, String dataDir) {
     this(cons, dataDir, false);
   }
   protected JPopupMenu aoiMenu;
   protected JButton aoiButton;
   protected JMenuItem addAoi;
   protected JMenuItem useAoi;
   protected JMenuItem clearAoi;
   protected JMenuItem remAllAoi;
   protected JMenuItem manEntryAoi;
 
   public void initComponents() {
     mHandler = this.getMapHandler();
     mBean = this.getMapBean();
     mBean.setBackground(Color.WHITE);
 
     layerHandler = new LayerHandler();
     mHandler.add(layerHandler);
 
     // String homeDir = Utils.retrieveHomeDirectory(dataDir);
 
     dataDir = (dataDir.endsWith(File.separator) ? dataDir : dataDir + File.separator);
 
     // appName = System.getProperty("user.dir");
 
     // add the basemap
     basemapLayer = new BasemapLayer(dataDir);
 
     tools = new OMToolSet();
     toolPanel = new ToolPanel();
 
     mHandler.add(tools);
     mHandler.add(toolPanel);
 
     mouseDelegator = new MouseDelegator(mBean);
     mHandler.add(mouseDelegator);
     // mapHandler.add(new NullMouseMode());
     // mHandler.add(new NavMouseMode());//ratio-box
     mHandler.add(new NavMouseMode3());// manual-box
     mHandler.add(new PanMouseMode2());
     mHandler.add(new MeasureMouseMode());
     mHandler.add(new InformationMouseMode());
     asmm = new AreaSelectionMouseMode(true, geoCons);
     asmm.addPropertyChangeListener(this);
     mHandler.add(asmm);
     mouseDelegator.setActiveMouseMode(asmm);
 
     mHandler.add(new StandardMapMouseInterpreter());
     mHandler.add(new MouseModeButtonPanel());
     mHandler.add(new InformationDelegator());
     mHandler.add(new ProjectionStack());
     mHandler.add(new ProjectionStackTool());
 
     // <editor-fold defaultstate="collapsed" desc="AOI Menu">
     // if desired, add the AOI Menu Button to the toolbar
     if (showAOIButton) {
       // create an action listener for the AOI menu items
       ActionListener menuActionListener = new ActionListener() {
 
         public void actionPerformed(ActionEvent e) {
           if (e.getActionCommand().equals(AOI_SAVE)) {
             if (selectedExtent != null) {
               String defName = "AOI_[";
               LatLonRect rect = selectedExtent.getExtentRectangle();
               DecimalFormat df = new DecimalFormat("#,##0.00");
               defName += df.format(rect.getLatMin()) + "," + df.format(rect.getLonMin()) + ","
                       + df.format(rect.getLatMax()) + "," + df.format(rect.getLonMax());
               defName += "]";
 
               pcs.firePropertyChange(AOI_SAVE, defName, rect);
             }
           } else if (e.getActionCommand().equals(AOI_APPLY)) {
             pcs.firePropertyChange(AOI_APPLY, false, true);
           } else if (e.getActionCommand().equals(AOI_CLEAR)) {
             pcs.firePropertyChange(AOI_CLEAR, false, true);
           } else if (e.getActionCommand().equals(AOI_REMALL)) {
             pcs.firePropertyChange(AOI_REMALL, false, true);
           } else if (e.getActionCommand().equals(AOI_MANUAL)) {
             pcs.firePropertyChange(AOI_MANUAL, false, true);
           }
         }
       };
 
       // make the AOI menu items
       aoiMenu = new JPopupMenu("AOIs");
       addAoi = new JMenuItem("Save Current AOI...");
       addAoi.setActionCommand(AOI_SAVE);
       addAoi.addActionListener(menuActionListener);
 
       useAoi = new JMenuItem("Apply Existing AOI...");
       useAoi.setActionCommand(AOI_APPLY);
       useAoi.addActionListener(menuActionListener);
 
       clearAoi = new JMenuItem("Clear Current AOI");
       clearAoi.setActionCommand(AOI_CLEAR);
       clearAoi.addActionListener(menuActionListener);
 
       remAllAoi = new JMenuItem("Clear AOI List");
       remAllAoi.setActionCommand(AOI_REMALL);
       remAllAoi.addActionListener(menuActionListener);
 
       manEntryAoi = new JMenuItem("Enter an AOI Maually...");
       manEntryAoi.setActionCommand(AOI_MANUAL);
       manEntryAoi.addActionListener(menuActionListener);
 
       // construct the AOI menu
       aoiMenu.add(addAoi);
       aoiMenu.add(useAoi);
       aoiMenu.add(manEntryAoi);
       aoiMenu.add(new JSeparator());
       aoiMenu.add(clearAoi);
       aoiMenu.add(new JSeparator());
       aoiMenu.add(remAllAoi);
 
       // attach the AOI menu to a button on the toolbar
       aoiButton = new JButton("AOIs");
       aoiButton.addActionListener(new ActionListener() {
 
         public void actionPerformed(ActionEvent ae) {
           if (selectedExtent != null) {
             addAoi.setEnabled(true);
             clearAoi.setEnabled(true);
           } else {
             addAoi.setEnabled(false);
             clearAoi.setEnabled(false);
           }
 
           aoiMenu.show(aoiButton, 0, aoiButton.getHeight());
         }
       });
 
       toolPanel.add(aoiButton);
     }
 
     // </editor-fold>
 
     layerHandler.addLayer(basemapLayer, 0);
     mBean.setMinimumSize(new Dimension(200, 200));
   }
 
   public void resetGeoCons(GeoConstraints geoCons) {
     this.geoCons = geoCons;
     asmm.setGeoCons(geoCons);
   }
 
   public void zoomToSelectedExtent(DataBounds db) {
     LatLonPoint lr = new LatLonPoint(db.getMin().getY(), db.getMax().getX());
     LatLonPoint ul = new LatLonPoint(db.getMax().getY(), db.getMin().getX());
     centerAndScaleToSelectedExtent(ul, lr);
   }
 
   public void zoomToDataExtent(LatLonRect bbox) {
     LatLonPoint lr = new LatLonPoint(bbox.getLatMin(), bbox.getLonMax());
     LatLonPoint ul = new LatLonPoint(bbox.getLatMax(), bbox.getLonMin());
     centerAndScaleToDataExtent(ul, lr, bbox);
   }
 
   protected void centerAndScaleToSelectedExtent(LatLonPoint pUL, LatLonPoint pLR) {
     // Figure out the new scale
     float ncScale = com.bbn.openmap.proj.ProjMath.getScale(pUL, pLR, mBean.getProjection());
     float newScale = ncScale + (ncScale * 0.75f);
     mBean.setCenter(selectedExtent.getExtentCenterPoint());
     mBean.setScale(newScale);
   }
 
   protected void centerAndScaleToDataExtent(LatLonPoint pUL, LatLonPoint pLR, LatLonRect bbox) {
     // Figure out the new scale
     float ncScale = com.bbn.openmap.proj.ProjMath.getScale(pUL, pLR, mBean.getProjection());
     float newScale = ncScale + (ncScale * 0.75f);
     mBean.setCenter((pUL.getLatitude() + pLR.getLatitude()) / 2, Float.parseFloat(Double.toString(bbox.getCenterLon())));
     mBean.setScale(newScale);
   }
 
   protected void centerAndScaleToDataExtent(LatLonPoint pUL, LatLonPoint pLR) {
     // Figure out the new scale
     float ncScale = com.bbn.openmap.proj.ProjMath.getScale(pUL, pLR, mBean.getProjection());
     float newScale = ncScale + (ncScale * 0.75f);
     mBean.setCenter(dataExtent.getExtentCenterPoint());
     mBean.setScale(newScale);
   }
 
   public void makeDataExtentLayer(double latMin, double lonMin, double latMax, double lonMax) {
     LatLonRect llr = new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
 
     makeDataExtentLayer(llr);
   }
 
   // used
   public void makeDataExtentLayer(LatLonRect llRect) {
     if (dataExtent != null) {
       layerHandler.removeLayer(dataExtent);
     }
     if (selectedExtent != null) {
       layerHandler.removeLayer(selectedExtent);
     }
 
     dataExtent = new ExtentRectangleLayer("Data Extent", true);
 
     lyrProps = new Properties();
     lyrProps.put("prettyName", "Data Extent");
     lyrProps.put("lineColor", "88FF0000");// 000000=black
     lyrProps.put("fillColor", "88FF0000");
     dataExtent.setProperties(lyrProps);
     layerHandler.addLayer(dataExtent);
 
     dataExtent.addExtentRectangle(llRect);
 
     LatLonPoint ul = new LatLonPoint();
     ul.setLatLon((float) llRect.getLatMax(), (float) llRect.getLonMin());
     LatLonPoint lr = new LatLonPoint();
     lr.setLatLon((float) llRect.getLatMin(), (float) llRect.getLonMax());
 
     // System.err.println("ul:" + ul.getLongitude() + " " + "lr:" +
     // lr.getLongitude());
     centerAndScaleToDataExtent(ul, lr);
   }
 
   public void makeSelectedExtentLayer(double latMin, double lonMin, double latMax, double lonMax) {
     LatLonRect llr = new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
     makeSelectedExtentLayer(llr);
   }
 
   public void makeSelectedExtentLayer(LatLonRect llRect) {
     if (selectedExtent != null) {
       layerHandler.removeLayer(selectedExtent);
     }
 
     if (!layerHandler.hasLayer(selectedExtent)) {
       selectedExtent = new ExtentRectangleLayer("Desired Extent", false);
       layerHandler.addLayer(selectedExtent);
     }
 
     lyrProps = new Properties();
     lyrProps.put("prettyName", "Desired Extent");
     lyrProps.put("lineColor", "00000000");// 000000 = black
     lyrProps.put("fillColor", "8800FF00");
     selectedExtent.setProperties(lyrProps);
 
     selectedExtent.addExtentRectangle(llRect);
   }
 
   public LatLonRect getSelectedExtent() {
     if (selectedExtent != null) {
       return selectedExtent.getExtentRectangle();
     }
     return null;
   }
 
   public void clearSelectedExtent() {
     if (selectedExtent != null) {
       this.getLayerHandler().removeLayer(selectedExtent);
       selectedExtent = null;
     }
   }
 
   public void propertyChange(PropertyChangeEvent evt) {
     String propName = evt.getPropertyName();
     // User drew a bounding box to select an area
     if (propName.equals("boundsStored")) {
       if ((Boolean) evt.getNewValue()) {
         makeSelectedExtentLayer(geoCons.getBoundingBox());
        if (sensorLayer != null) {
          sensorLayer.setPickedByBBOX(geoCons.getBoundingBox());
        }
       }
     }
     pcs.firePropertyChange(evt);// pass the event along to the calling class
   }
 
   @Override
   public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.addPropertyChangeListener(l);
   }
 
   @Override
   public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.removePropertyChangeListener(l);
   }
 
   public LayerHandler getLayerHandler() {
     return layerHandler;
   }
 
   public MouseDelegator getMouseDelegator() {
     return mouseDelegator;
   }
 
   public SosLayer getSensorLayer() {
     return sensorLayer;
   }
 
   public void addSensors(List<SensorContainer> sensorList) {
     sensorLayer = new SosLayer();
     layerHandler.addLayer(sensorLayer);
     sensorLayer.addPropertyChangeListener(new PropertyChangeListener() {
       public void propertyChange(PropertyChangeEvent evt) {
         pcs.firePropertyChange("loaded", false, true);
       }
     });
     sensorLayer.setSensors(sensorList);
   }
 
   public JDialog makeRectEntryDialog(JFrame parent, String title, boolean modal) {
     return new RectangleEntryDialog(parent, title, modal);
   }
   private JDoubleField jdfLatMin;
   private JDoubleField jdfLonMin;
   private JDoubleField jdfLonMax;
   private JDoubleField jdfLatMax;
   private NumFieldUtilities numUtils;
   private JButton btnOK;
   private JButton btnCancel;
 
   private class RectangleEntryDialog extends JDialog {
 
     private RectangleEntryDialog(JFrame parent, String title, boolean modal) {
       super(parent instanceof JFrame ? (JFrame) parent : null, title, modal);
 
       setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
       setLayout(new MigLayout("center"));
       setResizable(false);
 
       numUtils = new NumFieldUtilities();
 
       jdfLatMin = new JDoubleField(NumFieldUtilities.makeDff4Places());
       jdfLatMin.addFocusListener(numUtils);
       jdfLatMin.setMaximumDoubleValue(90);
       jdfLatMin.setMinimumDoubleValue(-90);
 
       jdfLonMin = new JDoubleField(NumFieldUtilities.makeDff4Places());
       jdfLonMin.addFocusListener(numUtils);
       jdfLonMin.setMaximumDoubleValue(180);
       jdfLonMin.setMinimumDoubleValue(-180);
 
       jdfLonMax = new JDoubleField(NumFieldUtilities.makeDff4Places());
       jdfLonMax.addFocusListener(numUtils);
       jdfLonMax.setMaximumDoubleValue(180);
       jdfLonMax.setMinimumDoubleValue(-180);
 
       jdfLatMax = new JDoubleField(NumFieldUtilities.makeDff4Places());
       jdfLatMax.addFocusListener(numUtils);
       jdfLatMax.setMaximumDoubleValue(90);
       jdfLatMax.setMinimumDoubleValue(-90);
 
       add(new JLabel("Northern Latitude:"));
       add(jdfLatMax, "w 90!, wrap");
       add(new JLabel("Western Longitude:"));
       add(jdfLonMin, "w 90!, wrap");
       add(new JLabel("Eastern Longitude:"));
       add(jdfLonMax, "w 90!, wrap");
       add(new JLabel("Southern Latitude:"));
       add(jdfLatMin, "w 90!, wrap");
 
       ActionListener btnListener = new ActionListener() {
 
         public void actionPerformed(ActionEvent e) {
           String name = e.getActionCommand();
           boolean close = false;
           String msg = "";
           if (name.equals("ok")) {
             // update the number fields
             NumFieldUtilities.updateNumFieldValues(RectangleEntryDialog.this);
 
             double latMin, latMax, lonMin, lonMax;
             latMin = jdfLatMin.getDoubleValue();
             latMax = jdfLatMax.getDoubleValue();
             lonMin = jdfLonMin.getDoubleValue();
             lonMax = jdfLonMax.getDoubleValue();
             if (latMax > latMin) {
               if (lonMax > lonMin) {
                 makeSelectedExtentLayer(latMin, lonMin, latMax, lonMax);
                 zoomToSelectedExtent(new DataBounds(lonMin, latMin, lonMax, latMax));
                 close = true;
               } else {
                 msg = "The western longitude is greater than the eastern longitude.";
               }
             } else {
               msg = "The southern latitude is greater than the northern latitude.";
             }
 
             if (!close) {
               msg += "\nPlease correct the coordinates and try again.";
               JOptionPane.showMessageDialog(RectangleEntryDialog.this, msg, "Invalid Coordinates",
                       JOptionPane.WARNING_MESSAGE);
             }
           } else if (name.equals("cancel")) {
             close = true;
           }
 
           if (close) {
             setVisible(false);
           }
         }
       };
 
       btnOK = new JButton("Accept");
       btnOK.setActionCommand("ok");
       btnOK.addActionListener(btnListener);
       btnCancel = new JButton("Cancel");
       btnCancel.setActionCommand("cancel");
       btnCancel.addActionListener(btnListener);
 
       add(btnOK);
       add(btnCancel);
 
       pack();
       setLocationRelativeTo(parent);
 
     }
 
     @Override
     protected JRootPane createRootPane() {
       ActionListener cancelListener = new ActionListener() {
 
         public void actionPerformed(ActionEvent actionEvent) {
           btnCancel.doClick();
         }
       };
       ActionListener okListener = new ActionListener() {
 
         public void actionPerformed(ActionEvent actionEvent) {
           btnOK.doClick();
         }
       };
       JRootPane rPane = new JRootPane();
       KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
       KeyStroke stroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
       rPane.registerKeyboardAction(cancelListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
       rPane.registerKeyboardAction(okListener, stroke1, JComponent.WHEN_IN_FOCUSED_WINDOW);
 
       return rPane;
     }
   }
 }
