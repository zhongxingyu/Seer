 /*
  * $Id: PixelInfoView.java,v 1.5 2007/04/18 13:01:13 norman Exp $
  *
  * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.framework.ui;
 
 import com.bc.swing.dock.DockablePane;
 import com.bc.swing.dock.FloatingComponentFactory;
 import com.bc.swing.dock.FloatingDockableFrame;
 import com.jidesoft.docking.DockingManager;
 import com.jidesoft.swing.JideSplitPane;
 import org.esa.beam.framework.datamodel.*;
 import org.esa.beam.framework.dataop.maptransf.MapTransform;
 import org.esa.beam.framework.help.HelpSys;
 import org.esa.beam.framework.ui.product.ProductSceneView;
 import org.esa.beam.jai.ImageManager;
 import org.esa.beam.util.Debug;
 import org.esa.beam.util.Guardian;
 import org.esa.beam.util.math.MathUtils;
 
 import javax.media.jai.PlanarImage;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ImageIcon;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.Raster;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The pixel info view component is used to display the geophysical values for the pixel at a given pixel position
  * (x,y). The pixel info view can simultaneously display band, tie point grid and flag values.
  *
  * @author Norman Fomferra
  * @author Sabine Embacher
  * @version 1.2
  */
 public class PixelInfoView extends JPanel {
 
     public static final String HELP_ID = "pixelInfoView";
     /**
      * Preferences key for show all band pixel values in pixel info view
      */
     public static final String PROPERTY_KEY_SHOW_ONLY_DISPLAYED_BAND_PIXEL_VALUES = "pixelview.showOnlyDisplayedBands";
     public static final boolean PROPERTY_DEFAULT_SHOW_DISPLAYED_BAND_PIXEL_VALUES = true;
 
     private static final int _NAME_COLUMN = 0;
     private static final int _VALUE_COLUMN = 1;
     private static final int _UNIT_COLUMN = 2;
     private JideSplitPane multiSplitPane;
 
     public enum DockablePaneKey {
 
         GEOLOCATION, SCANLINE, BANDS, TIEPOINTS, FLAGS
     }
 
     private static final String _INVALID_POS_TEXT = "Invalid pos.";
     private static final String _NOT_LOADED_TEXT = "Not displayed";
 
     private final GeoPos _geoPos;
     private final PixelPos _pixelPos;
     private final Point2D _mapPoint;
     private final PropertyChangeListener _displayFilterListener;
     private final ProductNodeListener _productNodeListener;
 
     private DockablePane geolocInfoPane;
     private DockablePane scanLineInfoPane;
     private DockablePane bandPixelInfoPane;
     private DockablePane tiePointGridPixelInfoPane;
     private DockablePane flagPixelInfoPane;
     private Product currentProduct;
     private RasterDataNode currentRaster;
     private ProductSceneView currentView;
     private Band[] currentFlagBands;
     private boolean _showOnlyLoadedBands = PROPERTY_DEFAULT_SHOW_DISPLAYED_BAND_PIXEL_VALUES;
     private boolean _showPixelPosDecimals;
     private float _pixelOffsetX;
     private float _pixelOffsetY;
     private int _pixelX;
     private int _pixelY;
     private int _level;
     private int levelZeroX;
     private int levelZeroY;
     private boolean _pixelPosValid;
     private DisplayFilter _displayFilter;
     private final BasicApp app;
     private Map<DockablePaneKey,DockablePane> dockablePaneMap;
 
     /**
      * Constructs a new pixel info view.
      */
     public PixelInfoView(BasicApp app) {
         super(new BorderLayout());
         this.app = app;
         _geoPos = new GeoPos();
         _pixelPos = new PixelPos();
         _mapPoint = new Point2D.Double();
         _displayFilterListener = createDisplayFilterListener();
         _productNodeListener = createProductNodeListener();
         dockablePaneMap = new HashMap<DockablePaneKey, DockablePane>(5);
         createUI();
     }
 
     private ProductNodeListener createProductNodeListener() {
         return new ProductNodeListenerAdapter() {
             @Override
             public void nodeChanged(ProductNodeEvent event) {
                 resetTableModels();
             }
 
             @Override
             public void nodeAdded(ProductNodeEvent event) {
                 resetTableModels();
             }
 
             @Override
             public void nodeRemoved(ProductNodeEvent event) {
                 resetTableModels();
             }
         };
     }
 
     private PropertyChangeListener createDisplayFilterListener() {
         return new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent evt) {
                 if (getCurrentProduct() != null) {
                     resetBandTableModel();
                     updateDataDisplay();
                     clearSelectionInRasterTables();
                 }
             }
         };
     }
 
     /**
      * Returns the current product
      *
      * @return the current Product
      */
     public Product getCurrentProduct() {
         return currentProduct;
     }
 
     /**
      * Returns the current raster
      *
      * @return the current raster
      */
     public RasterDataNode getCurrentRaster() {
         return currentRaster;
     }
 
     /**
      * Sets the filter to be used to filter the displayed bands. <p/>
      *
      * @param displayFilter the filter, can be null
      */
     public void setDisplayFilter(DisplayFilter displayFilter) {
         if (_displayFilter != displayFilter) {
             if (_displayFilter != null) {
                 _displayFilter.removePropertyChangeListener(_displayFilterListener);
             }
             _displayFilter = displayFilter;
             _displayFilter.addPropertyChangeListener(_displayFilterListener);
         }
     }
 
     /**
      * Returns the display filter
      *
      * @return the display filter, can be null
      */
     public DisplayFilter getDisplayFilter() {
         return _displayFilter;
     }
 
     public void setShowPixelPosDecimals(boolean showPixelPosDecimals) {
         if (_showPixelPosDecimals != showPixelPosDecimals) {
             _showPixelPosDecimals = showPixelPosDecimals;
             updateDataDisplay();
         }
     }
 
     public void setPixelOffsetX(float pixelOffsetX) {
         if (_pixelOffsetX != pixelOffsetX) {
             _pixelOffsetX = pixelOffsetX;
             updateDataDisplay();
         }
     }
 
     public void setPixelOffsetY(float pixelOffsetY) {
         if (_pixelOffsetY != pixelOffsetY) {
             _pixelOffsetY = pixelOffsetY;
             updateDataDisplay();
         }
     }
 
     public void updatePixelValues(ProductSceneView view, int pixelX, int pixelY, int level, boolean pixelPosValid) {
         Guardian.assertNotNull("view", view);
         final RasterDataNode raster = view.getRaster();
         final Product product = raster.getProduct();
         if (product == currentProduct && view.isRGB()) {
             resetBandTableModel();
         }
         if (product != currentProduct) {
             if (currentProduct != null) {
                 currentProduct.removeProductNodeListener(_productNodeListener);
             }
             product.addProductNodeListener(_productNodeListener);
             currentProduct = product;
             registerFlagDatasets();
             resetTableModels();
         }
         if (raster != currentRaster) {
             currentRaster = raster;
             registerFlagDatasets();
             resetTableModels();
         }
         if (getBandTableModel().getRowCount() != getBandRowCount()) {
             resetTableModels();
         }
         if (view != currentView) {
             currentView = view;
             resetTableModels();
             clearSelectionInRasterTables();
         }
         Debug.assertTrue(currentProduct != null);
         _pixelX = pixelX;
         _pixelY = pixelY;
         _level = level;
         _pixelPosValid = pixelPosValid;
         AffineTransform i2mTransform = currentView.getBaseImageLayer().getImageToModelTransform(level);
         Point2D modelP = i2mTransform.transform(new Point2D.Double(pixelX + 0.5, pixelY + 0.5), null);
         AffineTransform m2iTransform = view.getBaseImageLayer().getModelToImageTransform();
         Point2D levelZeroP = m2iTransform.transform(modelP, null);
         levelZeroX = (int)Math.floor(levelZeroP.getX());
         levelZeroY = (int)Math.floor(levelZeroP.getY());
         updateDataDisplay();
     }
     
     public boolean allDocked() {
         return geolocInfoPane.isDocked() && scanLineInfoPane.isDocked()
                 && bandPixelInfoPane.isDocked() && tiePointGridPixelInfoPane.isDocked()
                 && flagPixelInfoPane.isDocked();
     }
 
     public void showDockablePanel(DockablePaneKey key, boolean show) {
         final DockablePane dockablePane = dockablePaneMap.get(key);
         if(multiSplitPane.indexOfPane(dockablePane) < 0 && show) {
             multiSplitPane.addPane(dockablePane);
             multiSplitPane.invalidate();
         }
         dockablePane.setShown(show);
     }
 
    public boolean isDockablePaneShown(DockablePaneKey key) {
        final DockablePane dockablePane = dockablePaneMap.get(key);
        return dockablePane.isContentShown();
    }

     public DockablePane getDockablePane(DockablePaneKey key) {
         return dockablePaneMap.get(key);
     }
 
     private void createUI() {
         geolocInfoPane = createDockablePane("Geo-location", 0, UIUtils.loadImageIcon("icons/WorldMap16.gif"),
                                             new String[]{"Coordinate", "Value", "Unit"});
         scanLineInfoPane = createDockablePane("Time Info", 1, UIUtils.loadImageIcon("icons/Clock16.gif"),
                                               new String[]{"Time", "Value", "Unit"});
         bandPixelInfoPane = createDockablePane("Bands", 2, UIUtils.loadImageIcon("icons/RsBandAsSwath16.gif"),
                                                new String[]{"Band", "Value", "Unit"});
         tiePointGridPixelInfoPane = createDockablePane("Tie Point Grids", 3,
                                                        UIUtils.loadImageIcon("icons/RsBandAsTiePoint16.gif"),
                                                        new String[]{"Tie Point Grid", "Value", "Unit"});
         flagPixelInfoPane = createDockablePane("Flags", 4, UIUtils.loadImageIcon("icons/RsBandFlags16.gif"),
                                                new String[]{"Flag", "Value",});
 
         geolocInfoPane.setPreferredSize(new Dimension(128, 128));
         scanLineInfoPane.setPreferredSize(new Dimension(128, 128));
         bandPixelInfoPane.setPreferredSize(new Dimension(128, 512));
         tiePointGridPixelInfoPane.setPreferredSize(new Dimension(128, 128));
         flagPixelInfoPane.setPreferredSize(new Dimension(128, 128));
         flagPixelInfoPane.setVisible(false);
 
         dockablePaneMap.put(DockablePaneKey.GEOLOCATION, geolocInfoPane);
         dockablePaneMap.put(DockablePaneKey.SCANLINE, scanLineInfoPane);
         dockablePaneMap.put(DockablePaneKey.TIEPOINTS, tiePointGridPixelInfoPane);
         dockablePaneMap.put(DockablePaneKey.BANDS, bandPixelInfoPane);
         dockablePaneMap.put(DockablePaneKey.FLAGS, flagPixelInfoPane);
 
         multiSplitPane = new JideSplitPane();
         multiSplitPane.setOrientation(JideSplitPane.VERTICAL_SPLIT);
         multiSplitPane.addPane(geolocInfoPane);
         multiSplitPane.addPane(scanLineInfoPane);
         multiSplitPane.addPane(tiePointGridPixelInfoPane);
         multiSplitPane.addPane(bandPixelInfoPane);
         // Flags are not added, they are only displayed on request
 //        multiSplitPane.addPane(flagPixelInfoPane);
 
         final JTable flagsTable = getTable(flagPixelInfoPane);
         flagsTable.setDefaultRenderer(String.class, new FlagCellRenderer());
         flagsTable.setDefaultRenderer(Object.class, new FlagCellRenderer());
 
         addComponentListener();
         add(multiSplitPane, BorderLayout.CENTER);
 
         HelpSys.enableHelpKey(this, HELP_ID);
     }
 
     private void addComponentListener() {
         addComponentListener(new ComponentAdapter() {
 
             @Override
             public void componentResized(ComponentEvent e) {
                 super.componentResized(e);
                 setPreferredSize(getSize());
             }
         });
     }
 
     private DockablePane createDockablePane(String name, int index, ImageIcon icon, String[] columnNames) {
         JTable table = new JTable(new DefaultTableModel(columnNames, 0) {
 
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         });
         table.setCellSelectionEnabled(false);
         table.setColumnSelectionAllowed(false);
         table.setRowSelectionAllowed(true);
         table.setTableHeader(null);
         table.removeEditor();
         JScrollPane scrollPane = new JScrollPane(table,
                                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setBorder(null);
         scrollPane.setViewportBorder(null);
 
         final DockingManager dockingManager = app.getMainFrame().getDockingManager();
         final FloatingComponentFactory componentFactory = FloatingDockableFrame.getFactory(dockingManager);
         return new DockablePane(name, icon, scrollPane, index, true, componentFactory);
     }
 
     private DefaultTableModel getGeolocTableModel() {
         return getTableModel(geolocInfoPane);
     }
 
     private DefaultTableModel getScanLineTableModel() {
         return getTableModel(scanLineInfoPane);
     }
 
     private DefaultTableModel getBandTableModel() {
         return getTableModel(bandPixelInfoPane);
     }
 
     private DefaultTableModel getTiePointGridTableModel() {
         return getTableModel(tiePointGridPixelInfoPane);
     }
 
     private DefaultTableModel getFlagTableModel() {
         return getTableModel(flagPixelInfoPane);
     }
 
     private DefaultTableModel getTableModel(JTable table) {
         return (DefaultTableModel) table.getModel();
     }
 
     private DefaultTableModel getTableModel(DockablePane pane) {
         return getTableModel(getTable(pane));
     }
 
     private static JTable getTable(DockablePane pane) {
         return (JTable) ((JScrollPane) pane.getContent()).getViewport().getView();
     }
 
     private void resetTableModels() {
         resetGeolocTableModel();
         resetScanLineTableModel();
         resetBandTableModel();
         resetTiePointGridTableModel();
         resetFlagTableModel();
     }
 
     private void clearSelectionInRasterTables() {
         final String rasterName = currentView.getRaster().getName();
         final JTable bandTable = getTable(bandPixelInfoPane);
         final JTable tiePointGridTable = getTable(tiePointGridPixelInfoPane);
         bandTable.clearSelection();
         tiePointGridTable.clearSelection();
         if (!selectCurrentRaster(rasterName, bandTable)) {
             selectCurrentRaster(rasterName, tiePointGridTable);
         }
     }
 
     public void clearProductNodeRefs() {
         currentProduct = null;
         currentRaster = null;
         currentView = null;
         currentFlagBands = new Band[0];
     }
 
     private void updateDataDisplay() {
        if(isDockablePaneShown(DockablePaneKey.GEOLOCATION)) {
             updateGeolocValues();
         }
        if(isDockablePaneShown(DockablePaneKey.SCANLINE)) {
             updateScanLineValues();
         }
        if(isDockablePaneShown(DockablePaneKey.BANDS)) {
             updateBandPixelValues();
         }
        if(isDockablePaneShown(DockablePaneKey.TIEPOINTS)) {
             updateTiePointGridPixelValues();
         }
        if(isDockablePaneShown(DockablePaneKey.FLAGS)){
             updateFlagPixelValues();
         }
     }
 
     private void updateScanLineValues() {
         final Product currentProduct = getCurrentProduct();
         if (currentProduct == null) {
             return;
         }
         final TableModel model = getScanLineTableModel();
 
         final ProductData.UTC utcStartTime = currentProduct.getStartTime();
         final ProductData.UTC utcEndTime = currentProduct.getEndTime();
 
         final double dStart = utcStartTime != null ? utcStartTime.getMJD() : 0;
         final double dStop = utcEndTime != null ? utcEndTime.getMJD() : 0;
         if (dStart == 0 || dStop == 0) {
             model.setValueAt("No date information", 0, _VALUE_COLUMN);
             model.setValueAt("No time information", 1, _VALUE_COLUMN);
             return;
         }
         final double vPerLine = (dStop - dStart) / (currentProduct.getSceneRasterHeight() - 1);
         final double currentLine = vPerLine * _pixelY + dStart;
         final ProductData.UTC utcCurrentLine = new ProductData.UTC(currentLine);
         final Calendar currentLineTime = utcCurrentLine.getAsCalendar();
 
         final String dateString = String.format("%1$tF", new Object[]{currentLineTime});
         final String timeString = String.format("%1$tI:%1$tM:%1$tS:%1$tL %1$Tp", new Object[]{currentLineTime});
 
         model.setValueAt(dateString, 0, _VALUE_COLUMN);
         model.setValueAt(timeString, 1, _VALUE_COLUMN);
 
 
     }
 
     private void updateGeolocValues() {
         if (getCurrentProduct() == null) {
             return;
         }
         final TableModel model = getGeolocTableModel();
         final boolean available = isSampleValueAvailable(levelZeroX, levelZeroY, _pixelPosValid);
         final float pX = levelZeroX + _pixelOffsetX;
         final float pY = levelZeroY + _pixelOffsetY;
 
         String tix, tiy, tmx, tmy, tgx, tgy;
         tix = tiy = tmx = tmy = tgx = tgy = _INVALID_POS_TEXT;
 
         GeoCoding geoCoding = getCurrentRaster().getGeoCoding();
         if (available) {
             _pixelPos.x = pX;
             _pixelPos.y = pY;
             if (_showPixelPosDecimals) {
                 tix = String.valueOf(pX);
                 tiy = String.valueOf(pY);
             } else {
                 tix = String.valueOf(levelZeroX);
                 tiy = String.valueOf(levelZeroY);
             }
             if (geoCoding != null) {
                 geoCoding.getGeoPos(_pixelPos, _geoPos);
                 tgx = _geoPos.getLonString();
                 tgy = _geoPos.getLatString();
                 if (geoCoding instanceof MapGeoCoding) {
                     final MapGeoCoding mapGeoCoding = (MapGeoCoding) geoCoding;
                     final MapTransform mapTransform = mapGeoCoding.getMapInfo().getMapProjection().getMapTransform();
                     mapTransform.forward(_geoPos, _mapPoint);
                     tmx = String.valueOf(MathUtils.round(_mapPoint.getX(), 10000.0));
                     tmy = String.valueOf(MathUtils.round(_mapPoint.getY(), 10000.0));
                 }
             }
         }
         model.setValueAt(tix, 0, 1);
         model.setValueAt(tiy, 1, 1);
         if (geoCoding != null) {
             model.setValueAt(tgx, 2, 1);
             model.setValueAt(tgy, 3, 1);
             if (geoCoding instanceof MapGeoCoding) {
                 model.setValueAt(tmx, 4, 1);
                 model.setValueAt(tmy, 5, 1);
             }
         }
     }
 
     private void updateBandPixelValues() {
         final DefaultTableModel model = getBandTableModel();
         Product currentProduct = getCurrentProduct();
         if (currentProduct == null) {
             return;
         }
         Band[] bands = currentProduct.getBands();
         int rowIndex = 0;
         for (final Band band : bands) {
             if (shouldDisplayBand(band)) {
                 Debug.assertTrue(band.getName().equals(model.getValueAt(rowIndex, _NAME_COLUMN)));
                 model.setValueAt(getPixelString(band), rowIndex, _VALUE_COLUMN);
                 rowIndex++;
             }
         }
     }
     
     private String getPixelString(Band band) {
         if (!_pixelPosValid) {
             return RasterDataNode.INVALID_POS_TEXT;
         }
         if (isPixelValid(band, _pixelX, _pixelY, _level)) {
         	if (band.isFloatingPointType()) {
         		return String.valueOf(getSampleFloat(band, _pixelX, _pixelY, _level));
         	}else {
         		return String.valueOf(getSampleInt(band, _pixelX, _pixelY, _level));
         	}
         } else {
             return RasterDataNode.NO_DATA_TEXT;
         }
     }
 
     private boolean isPixelValid(Band band, int pixelX, int pixelY, int level) {
     	if (band.isValidMaskUsed()) {
     		PlanarImage image = ImageManager.getInstance().getValidMaskImage(band, level);
     		Raster data = getRasterTile(image, pixelX, pixelY);
     		return data.getSample(pixelX, pixelY, 0) != 0;
 		} else {
     		return true;
     	}
     }
     
     private float getSampleFloat(Band band, int pixelX, int pixelY, int level) {
     	PlanarImage image = ImageManager.getInstance().getSourceImage(band, level);
         Raster data = getRasterTile(image, pixelX, pixelY);
     	float sampleFloat = data.getSampleFloat(pixelX, pixelY, 0);
     	if (band.isScalingApplied()) {
     	    sampleFloat = (float) band.scale(sampleFloat);
     	}
         return sampleFloat;
     }
     
     private int getSampleInt(Band band, int pixelX, int pixelY, int level) {
     	PlanarImage image = ImageManager.getInstance().getSourceImage(band, level);
         Raster data = getRasterTile(image, pixelX, pixelY);
     	int sampleInt = data.getSample(pixelX, pixelY, 0);
     	if (band.isScalingApplied()) {
     	    sampleInt = (int) band.scale(sampleInt);
     	}
         return sampleInt;
     }
     
     private Raster getRasterTile(PlanarImage image, int pixelX, int pixelY) {
         final int tileX = image.XToTileX(pixelX);
         final int tileY = image.YToTileY(pixelY);
         return image.getTile(tileX, tileY);
     }
 
     private void updateTiePointGridPixelValues() {
         final DefaultTableModel model = getTiePointGridTableModel();
         Product currentProduct = getCurrentProduct();
         if (currentProduct == null) {
             return;
         }
         final int numTiePointGrids = currentProduct.getNumTiePointGrids();
         int rowIndex = 0;
         for (int i = 0; i < numTiePointGrids; i++) {
             final TiePointGrid grid = getCurrentProduct().getTiePointGridAt(i);
             Debug.assertTrue(grid.getName().equals(model.getValueAt(i, _NAME_COLUMN)));
             model.setValueAt(grid.getPixelString(levelZeroX, levelZeroY), rowIndex, _VALUE_COLUMN);
             rowIndex++;
         }
     }
 
     private void updateFlagPixelValues() {
         Product currentProduct = getCurrentProduct();
         if (currentProduct == null) {
             return;
         }
 
         final boolean available = isSampleValueAvailable(levelZeroX, levelZeroY, _pixelPosValid);
 
         final DefaultTableModel model = getFlagTableModel();
         if (model.getRowCount() != getFlagRowCount()) {
             resetFlagTableModel();
         }
         int rowIndex = 0;
         for (Band band : currentFlagBands) {
             int pixelValue = available ? getSampleInt(band, _pixelX, _pixelY, _level) : 0;
 
             for (int j = 0; j < band.getFlagCoding().getNumAttributes(); j++) {
                 MetadataAttribute attribute = band.getFlagCoding().getAttributeAt(j);
 
                 Debug.assertTrue(
                         (band.getName() + "." + attribute.getName()).equals(model.getValueAt(rowIndex, _NAME_COLUMN)));
 
                 if (available) {
                     int mask = attribute.getData().getElemInt();
                     model.setValueAt(String.valueOf((pixelValue & mask) == mask), rowIndex, _VALUE_COLUMN);
                 } else {
                     model.setValueAt(_INVALID_POS_TEXT, rowIndex, _VALUE_COLUMN);
                 }
                 rowIndex++;
             }
         }
     }
 
     private void registerFlagDatasets() {
         final Band[] bands = getCurrentProduct().getBands();
         Vector<Band> flagBandsVector = new Vector<Band>();
         for (Band band : bands) {
             if (isFlagBand(band)) {
                 flagBandsVector.add(band);
             }
         }
         currentFlagBands = flagBandsVector.toArray(new Band[flagBandsVector.size()]);
     }
 
     private boolean isSampleValueAvailable(int pixelX, int pixelY, boolean pixelValid) {
         return getCurrentProduct() != null
                 && pixelValid
                 && pixelX >= 0
                 && pixelY >= 0
                 && pixelX < getCurrentProduct().getSceneRasterWidth()
                 && pixelY < getCurrentProduct().getSceneRasterHeight();
     }
 
     private void resetGeolocTableModel() {
         final DefaultTableModel model = getGeolocTableModel();
         if (getCurrentRaster() != null) {
             final GeoCoding geoCoding = getCurrentRaster().getGeoCoding();
             if (geoCoding instanceof MapGeoCoding) {
                 model.setRowCount(6);
             } else if (geoCoding != null) {
                 model.setRowCount(4);
             } else {
                 model.setRowCount(2);
             }
 
             model.setValueAt("Image-X", 0, 0);
             model.setValueAt("pixel", 0, 2);
 
             model.setValueAt("Image-Y", 1, 0);
             model.setValueAt("pixel", 1, 2);
 
             if (geoCoding != null) {
                 model.setValueAt("Longitude", 2, 0);
                 model.setValueAt("degree", 2, 2);
 
                 model.setValueAt("Latitude", 3, 0);
                 model.setValueAt("degree", 3, 2);
 
                 if (geoCoding instanceof MapGeoCoding) {
                     final MapGeoCoding mapGeoCoding = (MapGeoCoding) geoCoding;
                     final String mapUnit = mapGeoCoding.getMapInfo().getMapProjection().getMapUnit();
 
                     model.setValueAt("Map-X", 4, 0);
                     model.setValueAt(mapUnit, 4, 2);
 
                     model.setValueAt("Map-Y", 5, 0);
                     model.setValueAt(mapUnit, 5, 2);
                 }
             }
         } else {
             model.setRowCount(0);
         }
     }
 
     private void resetScanLineTableModel() {
         final DefaultTableModel model = getScanLineTableModel();
         if (getCurrentRaster() != null) {
             model.setRowCount(2);
             model.setValueAt("Date", 0, _NAME_COLUMN);
             model.setValueAt("YYYY-MM-DD", 0, _UNIT_COLUMN);
 
             model.setValueAt("Time (UTC)", 1, _NAME_COLUMN);
             model.setValueAt("HH:MM:SS:mm [AM/PM]", 1, _UNIT_COLUMN);
         }
     }
 
     private void resetBandTableModel() {
         final DefaultTableModel model = getBandTableModel();
         if (getCurrentProduct() != null) {
             final int numBands = getCurrentProduct().getNumBands();
             int rowCount = getBandRowCount();
             model.setRowCount(rowCount);
             int rowIndex = 0;
             for (int i = 0; i < numBands; i++) {
                 final Band band = getCurrentProduct().getBandAt(i);
                 if (shouldDisplayBand(band)) {
                     model.setValueAt(band.getName(), rowIndex, _NAME_COLUMN);
                     model.setValueAt(band.getUnit(), rowIndex, _UNIT_COLUMN);
                     rowIndex++;
                 }
             }
         } else {
             model.setRowCount(0);
         }
     }
 
     private boolean shouldDisplayBand(final Band band) {
         if (_displayFilter != null) {
             return _displayFilter.accept(band);
         }
         return (band.hasRasterData() || !_showOnlyLoadedBands);
     }
 
     private void resetTiePointGridTableModel() {
         final DefaultTableModel model = getTiePointGridTableModel();
         if (getCurrentProduct() != null) {
             final int numTiePointGrids = getCurrentProduct().getNumTiePointGrids();
             int rowCount = numTiePointGrids;
             model.setRowCount(rowCount);
             int rowIndex;
             for (int i = 0; i < numTiePointGrids; i++) {
                 rowIndex = i;
                 final TiePointGrid tiePointGrid = getCurrentProduct().getTiePointGridAt(i);
                 model.setValueAt(tiePointGrid.getName(), rowIndex, _NAME_COLUMN);
                 model.setValueAt(tiePointGrid.getUnit(), rowIndex, _UNIT_COLUMN);
             }
         } else {
             model.setRowCount(0);
         }
     }
 
     private void resetFlagTableModel() {
         final DefaultTableModel model = getFlagTableModel();
         if (getCurrentProduct() != null) {
             model.setRowCount(getFlagRowCount());
             int rowIndex = 0;
             for (Band band : currentFlagBands) {
                 final FlagCoding flagCoding = band.getFlagCoding();
                 final int numFlags = flagCoding.getNumAttributes();
                 final String bandNameDot = band.getName() + ".";
                 for (int j = 0; j < numFlags; j++) {
                     String name = bandNameDot + flagCoding.getAttributeAt(j).getName();
                     model.setValueAt(name, rowIndex, _NAME_COLUMN);
                     rowIndex++;
                 }
             }
         } else {
             model.setRowCount(0);
         }
     }
 
     private boolean isFlagBand(final Band band) {
         return band.getFlagCoding() != null;
     }
 
 
     private boolean selectCurrentRaster(String rasterName, JTable table) {
         final DefaultTableModel model = getTableModel(table);
         for (int i = 0; i < model.getRowCount(); i++) {
             final String s = model.getValueAt(i, _NAME_COLUMN).toString();
             if (rasterName.equals(s)) {
                 table.changeSelection(i, _NAME_COLUMN, false, false);
                 return true;
             }
         }
         return false;
     }
 
     private int getBandRowCount() {
         int rowCount = 0;
         final Product currentProduct = getCurrentProduct();
         if (currentProduct != null) {
             Band[] bands = currentProduct.getBands();
             for (final Band band : bands) {
                 if (shouldDisplayBand(band)) {
                     rowCount++;
                 }
             }
         }
         return rowCount;
     }
 
     private int getFlagRowCount() {
         int rowCount = 0;
         for (Band band : currentFlagBands) {
             rowCount += band.getFlagCoding().getNumAttributes();
         }
         return rowCount;
     }
 
     private class FlagCellRenderer extends DefaultTableCellRenderer {
 
         /**
          * Returns the default table cell renderer.
          *
          * @param table      the <code>JTable</code>
          * @param value      the value to assign to the cell at <code>[row, column]</code>
          * @param isSelected true if cell is selected
          * @param hasFocus   true if cell has focus
          * @param row        the row of the cell to render
          * @param column     the column of the cell to render
          * @return the default table cell renderer
          */
         @Override
         public Component getTableCellRendererComponent(JTable table,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row,
                                                        int column) {
             Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
             c.setForeground(Color.black);
             c.setBackground(Color.white);
             if (column == _VALUE_COLUMN && value != null) {
                 if (value.equals("true")) {
                     c.setForeground(UIUtils.COLOR_DARK_RED);
                     final Color very_light_blue = new Color(230, 230, 255);
                     c.setBackground(very_light_blue);
                 } else if (value.equals("false")) {
                     c.setForeground(UIUtils.COLOR_DARK_BLUE);
                     final Color very_light_red = new Color(255, 230, 230);
                     c.setBackground(very_light_red);
                 }
             }
             return c;
         }
     }
 
     public static abstract class DisplayFilter {
 
         private final Vector<PropertyChangeListener> _pcl = new Vector<PropertyChangeListener>();
 
         public abstract boolean accept(final ProductNode node);
 
         public void addPropertyChangeListener(PropertyChangeListener displayFilterListener) {
             if (displayFilterListener != null && !_pcl.contains(displayFilterListener)) {
                 _pcl.add(displayFilterListener);
             }
         }
 
         public void removePropertyChangeListener(PropertyChangeListener displayFilterListener) {
             if (displayFilterListener != null && _pcl.contains(displayFilterListener)) {
                 _pcl.remove(displayFilterListener);
             }
         }
 
         protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
             final PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
             for (int i = 0; i < _pcl.size(); i++) {
                 (_pcl.elementAt(i)).propertyChange(event);
             }
         }
     }
 }
