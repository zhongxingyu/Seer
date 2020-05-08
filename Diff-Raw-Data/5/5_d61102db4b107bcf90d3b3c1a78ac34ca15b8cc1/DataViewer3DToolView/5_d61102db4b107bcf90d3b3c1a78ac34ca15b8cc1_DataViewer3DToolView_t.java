 /**
  * 
  */
 package org.esa.beam.dataViewer3D.beamIntegration;
 
 import static org.esa.beam.dataViewer3D.utils.NumberTypeUtils.castToType;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dialog.ModalityType;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.print.PageFormat;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.InputVerifier;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 import javax.swing.plaf.basic.BasicComboBoxEditor;
 import javax.swing.text.JTextComponent;
 
 import org.esa.beam.dataViewer3D.data.axis.Axis;
 import org.esa.beam.dataViewer3D.data.color.AbstractColorProvider;
 import org.esa.beam.dataViewer3D.data.color.ColorProvider;
 import org.esa.beam.dataViewer3D.data.coordinates.CoordinatesSystem;
 import org.esa.beam.dataViewer3D.data.dataset.AbstractDataSet;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet3D;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet4D;
 import org.esa.beam.dataViewer3D.data.grid.GridFromTicks;
 import org.esa.beam.dataViewer3D.data.point.DataPoint3D;
 import org.esa.beam.dataViewer3D.data.point.DataPoint4D;
 import org.esa.beam.dataViewer3D.data.source.BandDataSource;
 import org.esa.beam.dataViewer3D.data.source.BandDataSourceSet3D;
 import org.esa.beam.dataViewer3D.data.source.BandDataSourceSet4D;
 import org.esa.beam.dataViewer3D.data.source.DataSourceSet;
 import org.esa.beam.dataViewer3D.data.type.NumericType;
 import org.esa.beam.dataViewer3D.gui.GraphicalDataViewer;
 import org.esa.beam.dataViewer3D.gui.ImageCaptureCallback;
 import org.esa.beam.dataViewer3D.gui.JOGLDataViewer;
 import org.esa.beam.framework.barithm.PossiblyInvalidExpression;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.Mask;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductNode;
 import org.esa.beam.framework.datamodel.ProductNodeListener;
 import org.esa.beam.framework.datamodel.ProductNodeListenerAdapter;
 import org.esa.beam.framework.datamodel.RasterDataNode;
 import org.esa.beam.framework.datamodel.TiePointGrid;
 import org.esa.beam.framework.datamodel.VectorDataNode;
 import org.esa.beam.framework.datamodel.VirtualBand;
 import org.esa.beam.framework.dataop.barithm.BandArithmetic;
 import org.esa.beam.framework.dataop.barithm.RasterDataSymbol;
 import org.esa.beam.framework.help.HelpSys;
 import org.esa.beam.framework.param.ParamChangeEvent;
 import org.esa.beam.framework.param.ParamChangeListener;
 import org.esa.beam.framework.param.ParamEditorFactory;
 import org.esa.beam.framework.param.ParamFormatException;
 import org.esa.beam.framework.param.ParamGroup;
 import org.esa.beam.framework.param.ParamParseException;
 import org.esa.beam.framework.param.ParamProperties;
 import org.esa.beam.framework.param.ParamValidateException;
 import org.esa.beam.framework.param.ParamValidator;
 import org.esa.beam.framework.param.ParamValidatorRegistry;
 import org.esa.beam.framework.param.Parameter;
 import org.esa.beam.framework.param.editors.ComboBoxEditor;
 import org.esa.beam.framework.param.editors.FontEditor;
 import org.esa.beam.framework.param.validators.FontValidator;
 import org.esa.beam.framework.ui.GridBagUtils;
 import org.esa.beam.framework.ui.ModalDialog;
 import org.esa.beam.framework.ui.UIUtils;
 import org.esa.beam.framework.ui.application.support.AbstractToolView;
 import org.esa.beam.framework.ui.product.ProductExpressionPane;
 import org.esa.beam.framework.ui.product.ProductSceneView;
 import org.esa.beam.framework.ui.product.ProductTreeListener;
 import org.esa.beam.framework.ui.product.ProductTreeListenerAdapter;
 import org.esa.beam.framework.ui.product.VectorDataLayer;
 import org.esa.beam.framework.ui.product.VectorDataLayerFilterFactory;
 import org.esa.beam.framework.ui.tool.ToolButtonFactory;
 import org.esa.beam.util.Debug;
 import org.esa.beam.util.PropertyMap;
 import org.esa.beam.util.SystemUtils;
 import org.esa.beam.util.UniversalDocumentListener;
 import org.esa.beam.util.io.BeamFileChooser;
 import org.esa.beam.visat.VisatApp;
 import org.jfree.ui.ExtensionFileFilter;
 
 import com.bc.ceres.core.ProgressMonitor;
 import com.bc.ceres.core.SubProgressMonitor;
 import com.bc.ceres.glayer.Layer;
 import com.bc.ceres.glayer.support.LayerUtils;
 import com.bc.ceres.swing.TableLayout;
 import com.bc.ceres.swing.progress.DialogProgressMonitor;
 import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
 import com.bc.jexp.Namespace;
 import com.bc.jexp.ParseException;
 import com.bc.jexp.Parser;
 import com.bc.jexp.Symbol;
 import com.bc.jexp.Term;
 import com.bc.jexp.impl.DefaultNamespace;
 import com.bc.jexp.impl.NamespaceImpl;
 import com.bc.jexp.impl.ParserImpl;
 
 /**
  * A tool view that displays the 3D data viewer.
  * 
  * @author Martin Pecka
  */
 public class DataViewer3DToolView extends AbstractToolView implements SingleRoiComputePanel.ComputeMask
 {
 
     private static final String         NO_DATA_MESSAGE      = "No scatter plot computed yet.\n"
                                                                      + "Use wheel for zooming (Ctrl+wheel or right mouse button+wheel - fast zoom).\n"
                                                                      + "Use left or left+right mouse button to rotate.\n"
                                                                      + "Use right mouse button or Ctrl+left mouse button to navigate through the plot.";
     private static final String         CHART_TITLE          = "3D Scatter Plot";
     private static final String         TITLE_PREFIX         = CHART_TITLE;
     private static final String         VAR_INDEX            = "varIndex";
 
     private static final int            X_VAR                = 0;
     private static final int            Y_VAR                = 1;
     private static final int            Z_VAR                = 2;
     private static final int            W_VAR                = 3;
 
     private static final String[]       VAR_NAMES            = new String[] { "X", "Y", "Z", "W" };
     private static final String[]       BAND_NAMES           = new String[] { "scatter_x", "scatter_y", "scatter_z",
             "scatter_w"                                     };
 
     private ParamGroup                  paramGroup;
     private ParamGroup                  chartPropertiesGroup;
 
     private Parameter[]                 rasterNameParams     = new Parameter[4];
     private Parameter[]                 autoMinMaxParams     = new Parameter[4];
     private Parameter[]                 minParams            = new Parameter[4];
     private Parameter[]                 maxParams            = new Parameter[4];
     private Parameter[]                 scaleParams          = new Parameter[3];
     private Parameter[]                 logScaleParams       = new Parameter[4];
     /** The maximum number of displayed points. */
     private Parameter                   maxPointsParam;
 
     // plot visual style parameters
     private Parameter                   showTitleParam;
     private Parameter                   titleTextParam;
     private Parameter                   titleFontParam;
     private Parameter                   titleColorParam;
     private Parameter[]                 axisTitleTextParams  = new Parameter[4];
     private Parameter[]                 axisTitleFontParams  = new Parameter[4];
     private Parameter[]                 axisTitleColorParams = new Parameter[4];
     private Parameter[]                 axisShowLabelsParams = new Parameter[4];
     private Parameter[]                 axisShowTicksParams  = new Parameter[4];
     private Parameter[]                 axisLabelFontParams  = new Parameter[4];
     private Parameter                   backgroundColorParam;
     private Parameter                   showGridParam;
 
     private SingleRoiComputePanel       computePanel;
 
     /** The bands this data viewer uses to generate the source data. */
     protected final List<Band>          involvedBands        = new LinkedList<Band>();
     /** The data viewer used to display the data. */
     protected GraphicalDataViewer       dataViewer           = createDataViewer();
     /** The popup menu of the viewer panel. */
     protected final JPopupMenu          popupMenu            = new JPopupMenu();
 
     protected final String              HELP_ID              = "";
 
     /** The product selected in the main app window. */
     protected Product                   product;
     /** Wheteher this.product has changed since last call to upadteUI(). */
     protected boolean                   productChanged;
     /** The raster selected in the main app window. */
     protected RasterDataNode            raster;
     /** Wheteher this.raster has changed since last call to upadteUI(). */
     protected boolean                   rasterChanged;
     /** The vector data selected in the main app window. */
     protected VectorDataNode            vectorData;
     /** Wheteher this.vectorData has changed since last call to upadteUI(). */
     protected boolean                   vectorDataChanged;
     /** True when the data set has changed and the view hasn't been updated since then. */
     protected boolean                   dataSetChanged;
 
     /** The array of all active products compatible with the selected one. */
     protected Product[]                 compatibleProducts   = new Product[0];
 
     /** The array of all rasters that can be used. */
     protected RasterDataNode[]          availableRasters     = new RasterDataNode[0];
 
     /** The array of names of all rasters that can be used. */
     protected String[]                  availableRasterNames = new String[0];
 
     protected Namespace                 productNamespace     = null;
 
     /** The listener added to the product tree of the application when the view is opened. */
     private final ProductTreeListener   productTreeListener;
     /** The listener added to the application's frame when the view is opened. */
     private final InternalFrameListener internalFrameListener;
     /** The listener added to the selected product. */
     private final ProductNodeListener   productNodeListener;
 
     /** Chart panel buttons. */
     protected AbstractButton            printButton, saveButton, propertiesButton, zoomAllButton;
 
     public DataViewer3DToolView()
     {
         /*
          * HACK The following classes have to be static but they cannot be due to their natural function, so we simulate
          * it.
          */
         BandExpressionEditor._this = this;
         BandInputValidator._this = this;
 
         productTreeListener = createProductTreeListener();
         internalFrameListener = createInternalFrameListener();
         productNodeListener = createProductNodeListener();
     }
 
     /**
      * A callback to be called whenever the selected node in the app window has changed.
      * 
      * @param product The selected product.
      * @param raster The selected raster.
      * @param vectorDataNode The selected vector data node.
      */
     protected void selectionChanged(Product product, RasterDataNode raster, VectorDataNode vectorDataNode)
     {
         if (raster != getRaster() || product != getProduct() || vectorDataNode != getVectorDataNode()) {
             if (raster != null)
                 setRaster(raster);
             if (product != null)
                 setProduct(product);
             if (vectorDataNode != null)
                 setVectorDataNode(vectorDataNode);
         }
         invokeUpdateUI();
     }
 
     /**
      * Update the current selected product/raster/vector data nodes.
      */
     public void updateCurrentSelection()
     {
         final ProductNode selectedNode = VisatApp.getApp().getSelectedProductNode();
         if (selectedNode != null) {
             setProduct(selectedNode.getProduct());
         }
 
         if (selectedNode instanceof RasterDataNode) {
             setRaster((RasterDataNode) selectedNode);
         } else if (selectedNode instanceof VectorDataNode) {
             setVectorDataNode((VectorDataNode) selectedNode);
         }
     }
 
     @Override
     protected JComponent createControl()
     {
         initParameters();
         return createUI();
     }
 
     @Override
     public void componentOpened()
     {
         VisatApp.getApp().addProductTreeListener(productTreeListener);
         transferProductNodeListener(product, null);
         VisatApp.getApp().addInternalFrameListener(internalFrameListener);
         updateCurrentSelection();
         transferProductNodeListener(null, product);
         invokeUpdateUI();
     }
 
     @Override
     public void componentShown()
     {
         updateCurrentSelection();
         invokeUpdateUI();
         getControl().doLayout();
     }
 
     @Override
     public void componentClosed()
     {
         VisatApp.getApp().removeProductTreeListener(productTreeListener);
         transferProductNodeListener(product, null);
         VisatApp.getApp().removeInternalFrameListener(internalFrameListener);
     }
 
     private void initParameters()
     {
         paramGroup = new ParamGroup();
 
         initParameters(X_VAR, availableRasterNames);
         initParameters(Y_VAR, availableRasterNames);
         initParameters(Z_VAR, availableRasterNames);
         initParameters(W_VAR, availableRasterNames);
 
         maxPointsParam = new Parameter("maxPoints", 0);
         maxPointsParam.getProperties().setLabel("Max nr. of displayed points:"); /* I18N */
         maxPointsParam.getProperties().setDescription(
                 "Maximum number of displayed points. Zero means to display all points."); /* I18N */
         maxPointsParam.getProperties().setNumCols(10);
         maxPointsParam.getProperties().setMinValue(0);
         maxPointsParam.getProperties().setMaxValue(Integer.MAX_VALUE);
         paramGroup.addParameter(maxPointsParam);
 
         // CHART PROPERTIES DIALOG
 
         chartPropertiesGroup = new ParamGroup();
 
         showTitleParam = new Parameter("showTitle", Boolean.FALSE);
         showTitleParam.getProperties().setLabel("Show title?");
         showTitleParam.getProperties().setDescription("Show the main title of the plot?");
         paramGroup.addParameter(showTitleParam);
         chartPropertiesGroup.addParameter(showTitleParam);
         showTitleParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 dataViewer.getRenderParams().showTitle = (Boolean) event.getParameter().getValue();
             }
         });
 
         titleTextParam = new Parameter("titleText", "");
         titleTextParam.getProperties().setLabel("Text");
         titleTextParam.getProperties().setDescription("The main title of the plot.");
         paramGroup.addParameter(titleTextParam);
         chartPropertiesGroup.addParameter(titleTextParam);
         titleTextParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 String value = (String) event.getParameter().getValue();
                 if (value != null && value.length() == 0)
                     value = null;
                 dataViewer.setTitle(value);
             }
         });
 
         ParamValidatorRegistry.registerValidator(Font.class, new FontValidator());
         titleFontParam = new Parameter("titleFont", Font.decode("Arial"));
         titleFontParam.getProperties().setLabel("Font");
         titleFontParam.getProperties().setDescription("The font of the main title.");
         titleFontParam.getProperties().setEditorClass(FontEditor.class);
         paramGroup.addParameter(titleFontParam);
         chartPropertiesGroup.addParameter(titleFontParam);
         titleFontParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 dataViewer.getRenderParams().titleFont = (Font) event.getParameter().getValue();
             }
         });
 
         titleColorParam = new Parameter("titleColor", Color.black);
         titleColorParam.getProperties().setLabel("Color");
         titleColorParam.getProperties().setDescription("The color of the main title.");
         paramGroup.addParameter(titleColorParam);
         chartPropertiesGroup.addParameter(titleColorParam);
         titleColorParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 dataViewer.getRenderParams().titleColor = (Color) event.getParameter().getValue();
             }
         });
 
         for (int i = 0; i < 4; i++) {
             final int index = i;
             axisTitleTextParams[i] = new Parameter(VAR_NAMES[i] + "axisTitleText", "");
             axisTitleTextParams[i].getProperties().setLabel("Label text");
             axisTitleTextParams[i].getProperties().setDescription(
                     "The label of the axis. Leave empty to use the default label.");
             paramGroup.addParameter(axisTitleTextParams[i]);
             chartPropertiesGroup.addParameter(axisTitleTextParams[i]);
 
             axisTitleTextParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     if (index < dataViewer.getCoordinatesSystem().getAces().length) {
                         final String value = (String) event.getParameter().getValue();
                         if (value != null && value.length() > 0)
                             dataViewer.getCoordinatesSystem().getAces()[index].setLabel(value);
                         else
                             dataViewer.getCoordinatesSystem().getAces()[index].setLabel(null);
                     }
                 }
             });
 
             axisTitleFontParams[i] = new Parameter(VAR_NAMES[i] + "axisTitleFont", Font.decode("Arial"));
             axisTitleFontParams[i].getProperties().setLabel("Label font");
             axisTitleFontParams[i].getProperties().setDescription("The font of the label.");
             axisTitleFontParams[i].getProperties().setEditorClass(FontEditor.class);
             paramGroup.addParameter(axisTitleFontParams[i]);
             chartPropertiesGroup.addParameter(axisTitleFontParams[i]);
             axisTitleFontParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     dataViewer.getRenderParams().axisTitleFont[index] = (Font) event.getParameter().getValue();
                 }
             });
 
             axisTitleColorParams[i] = new Parameter(VAR_NAMES[i] + "axisTitleColor", Color.black);
             axisTitleColorParams[i].getProperties().setLabel("Label color");
             axisTitleColorParams[i].getProperties().setDescription("The color of the label.");
             paramGroup.addParameter(axisTitleColorParams[i]);
             chartPropertiesGroup.addParameter(axisTitleColorParams[i]);
             axisTitleColorParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     dataViewer.getRenderParams().axisTitleColor[index] = (Color) event.getParameter().getValue();
                 }
             });
 
             axisShowLabelsParams[i] = new Parameter(VAR_NAMES[i] + "axisShowLabels", Boolean.TRUE);
             axisShowLabelsParams[i].getProperties().setLabel("Show tick labels");
             axisShowLabelsParams[i].getProperties().setDescription("Show the labels for ticks?");
             paramGroup.addParameter(axisShowLabelsParams[i]);
             chartPropertiesGroup.addParameter(axisShowLabelsParams[i]);
             axisShowLabelsParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     dataViewer.getRenderParams().axisShowLabels[index] = (Boolean) event.getParameter().getValue();
                 }
             });
 
             axisShowTicksParams[i] = new Parameter(VAR_NAMES[i] + "axisShowTicks", Boolean.TRUE);
             axisShowTicksParams[i].getProperties().setLabel("Show tick marks");
             axisShowTicksParams[i].getProperties().setDescription("Show the marks for ticks?");
             paramGroup.addParameter(axisShowTicksParams[i]);
             chartPropertiesGroup.addParameter(axisShowTicksParams[i]);
             axisShowTicksParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     dataViewer.getRenderParams().axisShowTicks[index] = (Boolean) event.getParameter().getValue();
                 }
             });
 
             axisLabelFontParams[i] = new Parameter(VAR_NAMES[i] + "axisLabelFont", Font.decode("Arial 10"));
             axisLabelFontParams[i].getProperties().setLabel("Tick label font");
             axisLabelFontParams[i].getProperties().setDescription("The font of tick labels.");
             axisLabelFontParams[i].getProperties().setEditorClass(FontEditor.class);
             paramGroup.addParameter(axisLabelFontParams[i]);
             chartPropertiesGroup.addParameter(axisLabelFontParams[i]);
             axisLabelFontParams[i].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     dataViewer.getRenderParams().axisLabelFont[index] = (Font) event.getParameter().getValue();
                 }
             });
         }
 
         backgroundColorParam = new Parameter("backgroundColor", Color.white);
         backgroundColorParam.getProperties().setLabel("Background color");
         backgroundColorParam.getProperties().setDescription("The color of the background of the plot.");
         paramGroup.addParameter(backgroundColorParam);
         chartPropertiesGroup.addParameter(backgroundColorParam);
         backgroundColorParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 dataViewer.getRenderParams().backgroundColor = (Color) event.getParameter().getValue();
             }
         });
 
         showGridParam = new Parameter("showGrid", Boolean.TRUE);
         showGridParam.getProperties().setLabel("Show grid?");
         showGridParam.getProperties().setDescription("Show the grid?");
         paramGroup.addParameter(showGridParam);
         chartPropertiesGroup.addParameter(showGridParam);
         showGridParam.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 dataViewer.getRenderParams().showGrid = (Boolean) event.getParameter().getValue();
             }
         });
 
         paramGroup.addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 updateUI();
             }
         });
     }
 
     /**
      * Set the selected product.
      * 
      * @param product The selected product.
      */
     private void setProduct(Product product)
     {
         if (this.product == product)
             return;
 
         transferProductNodeListener(product, this.product);
         this.product = product;
         productChanged = true;
 
         compatibleProducts = createCompatibleProducts();
         availableRasters = createAvailableRastersArray();
         availableRasterNames = createAvailableRasterNamesArray();
 
         if (product != null)
             productNamespace = BandArithmetic.createDefaultNamespace(compatibleProducts, 0);
         else
             productNamespace = new NamespaceImpl(new DefaultNamespace());
 
         rasterNameParams[X_VAR].setValueSet(availableRasterNames);
         rasterNameParams[Y_VAR].setValueSet(availableRasterNames);
         rasterNameParams[Z_VAR].setValueSet(availableRasterNames);
         rasterNameParams[W_VAR].setValueSet(availableRasterNames);
     }
 
     /**
      * Return the product considered selected.
      * 
      * @return The product considered selected.
      */
     protected Product getProduct()
     {
         return product;
     }
 
     /**
      * Create an array of all open products compatible with the selected one. The selected product is located at index
      * 0.
      * 
      * @return The array of compatible products.
      */
     protected Product[] createCompatibleProducts()
     {
         final Product[] openProducts = VisatApp.getApp().getProductManager().getProducts();
         final List<Product> compatibleProducts = new ArrayList<Product>(openProducts.length);
         final float geolocationEps = (float) VisatApp.getApp().getPreferences()
                 .getPropertyDouble(VisatApp.PROPERTY_KEY_GEOLOCATION_EPS, VisatApp.PROPERTY_DEFAULT_GEOLOCATION_EPS);
 
         if (this.product != null)
             compatibleProducts.add(this.product);
         for (Product product : openProducts) {
             if (this.product != product) {
                 final boolean isCompatibleProduct = this.product == null
                         || this.product.isCompatibleProduct(product, geolocationEps);
                 if (isCompatibleProduct) {
                     compatibleProducts.add(product);
                 }
             }
         }
         return compatibleProducts.toArray(new Product[compatibleProducts.size()]);
     }
 
     /**
      * Create an array of all available raster nodes. Places the nodes belonging to the selected product to the
      * beginning of this array.
      * <p>
      * If you want this function to return the most up-to-date result, call
      * <code>this.compatibleProducts = {@link #createCompatibleProducts()};</code> before.
      * 
      * @return The array of available rasters.
      */
     protected RasterDataNode[] createAvailableRastersArray()
     {
         final List<RasterDataNode> availableBandList = new ArrayList<RasterDataNode>(17);
         if (compatibleProducts != null) {
             for (Product product : compatibleProducts) {
                 for (Band band : product.getBands()) {
                     availableBandList.add(band);
                 }
                 for (TiePointGrid grid : product.getTiePointGrids()) {
                     availableBandList.add(grid);
                 }
             }
         }
         // if raster is only bound to the product and does not belong to it
         final RasterDataNode raster = getRaster();
         if (raster != null && raster.getProduct() == product) {
             if (!availableBandList.contains(raster)) {
                 availableBandList.add(raster);
             }
         }
 
         return availableBandList.toArray(new RasterDataNode[availableBandList.size()]);
     }
 
     /**
      * Create an array of the names of all available raster nodes. Places the nodes belonging to the selected product to
      * the beginning of this array. Rasters not belonging to the selected product are prefixed with the product ref.
      * <p>
      * Call <code>this.availableRasters = {@link #createAvailableRastersArray()};</code> before this function if you
      * need the most up-to-date results.
      * 
      * @return The array of available raster names.
      */
     protected String[] createAvailableRasterNamesArray()
     {
         final String[] names = new String[availableRasters.length];
         for (int i = 0; i < availableRasters.length; i++) {
             final RasterDataNode raster = availableRasters[i];
             if (raster.getProduct() == null || raster.getProduct() == this.product) {
                 names[i] = raster.getName();
             } else {
                 names[i] = BandArithmetic.getProductNodeNamePrefix(raster.getProduct()) + raster.getName();
             }
         }
         return names;
     }
 
     /**
      * Return the selected raster or <code>null</code> if no raster is selected.
      * 
      * @return The selected raster.
      */
     protected RasterDataNode getRaster()
     {
         return raster;
     }
 
     /**
      * Set the selected raster.
      * 
      * @param raster The new selected raster.
      */
     protected void setRaster(RasterDataNode raster)
     {
         if (this.raster != raster) {
             this.raster = raster;
             rasterChanged = true;
         }
     }
 
     /**
      * Return the selected vector data node or <code>null</code> if no vector data node is selected.
      * 
      * @return The selected vector data node.
      */
     public VectorDataNode getVectorDataNode()
     {
         return vectorData;
     }
 
     /**
      * Set the selected vector data node.
      * 
      * @param vectorDataNode The new selected vector data node.
      */
     protected void setVectorDataNode(VectorDataNode vectorDataNode)
     {
         if (this.vectorData != vectorDataNode) {
             this.vectorData = vectorDataNode;
             vectorDataChanged = true;
         }
     }
 
     private void initParameters(final int varIndex, String[] availableBands)
     {
 
         final String paramPrefix = "var" + varIndex + ".";
 
         final RasterDataNode raster = getRaster(varIndex);
         final String rasterName;
         if (raster != null) {
             rasterName = raster.getName();
         } else if (availableBands.length > 0) {
             rasterName = availableBands[0];
         } else {
             rasterName = "";
         }
         final ParamProperties rasterParamProperties = new ParamProperties(BandExpression.class, null, availableBands);
         rasterParamProperties.setDescription("Band name"); /* I18N */
         rasterParamProperties.setValueSetBound(false);
         rasterParamProperties.setEditorClass(BandExpressionEditor.class);
         rasterParamProperties.setPropertyValue(VAR_INDEX, varIndex);
         rasterParamProperties.setValidatorClass(BandInputValidator.class);
         if (varIndex == W_VAR)
             rasterParamProperties.setNullValueAllowed(true);
         rasterNameParams[varIndex] = new Parameter(paramPrefix + "rasterName", new BandExpression(rasterName,
                 productNamespace), rasterParamProperties);
         paramGroup.addParameter(rasterNameParams[varIndex]);
 
         autoMinMaxParams[varIndex] = new Parameter(paramPrefix + "autoMinMax", Boolean.TRUE);
         autoMinMaxParams[varIndex].getProperties().setLabel("Auto min/max");/* I18N */
         autoMinMaxParams[varIndex].getProperties().setDescription("Automatically detect min/max"); /* I18N */
         paramGroup.addParameter(autoMinMaxParams[varIndex]);
 
         minParams[varIndex] = new Parameter(paramPrefix + "min", 0.0);
         minParams[varIndex].getProperties().setLabel("Min:");/* I18N */
         minParams[varIndex].getProperties().setDescription("Minimum display value"); /* I18N */
         minParams[varIndex].getProperties().setNumCols(7);
         paramGroup.addParameter(minParams[varIndex]);
 
         maxParams[varIndex] = new Parameter(paramPrefix + "max", 100.0);
         maxParams[varIndex].getProperties().setLabel("Max:");/* I18N */
         maxParams[varIndex].getProperties().setDescription("Maximum display value"); /* I18N */
         maxParams[varIndex].getProperties().setNumCols(7);
         paramGroup.addParameter(maxParams[varIndex]);
 
         if (varIndex != W_VAR) {
             scaleParams[varIndex] = new Parameter(paramPrefix + "scale", 1.0);
             scaleParams[varIndex].getProperties().setLabel("Axis scale:"); /* I18N */
             scaleParams[varIndex].getProperties().setDescription("The scale of the axis."); /* I18N */
             scaleParams[varIndex].getProperties().setNumCols(7);
             paramGroup.addParameter(scaleParams[varIndex]);
             scaleParams[varIndex].addParamChangeListener(new ParamChangeListener() {
                 @Override
                 public void parameterValueChanged(ParamChangeEvent event)
                 {
                     final Axis<?>[] aces = dataViewer.getCoordinatesSystem().getAces();
                     aces[varIndex].setScale((Double) event.getParameter().getValue());
                     aces[varIndex].updateTicks();
                     dataViewer.getCoordinatesSystem().setGrid(new GridFromTicks(aces[0], aces[1], aces[2]));
                     // dataViewer.resetTransformation();
                     dataViewer.update();
                 }
             });
         }
 
         logScaleParams[varIndex] = new Parameter(paramPrefix + "logScale", false);
         logScaleParams[varIndex].getProperties().setLabel("Logarithmic axis scaling?"); /* I18N */
         logScaleParams[varIndex].getProperties().setDescription("Scale this axis logarithmically?"); /* I18N */
         paramGroup.addParameter(logScaleParams[varIndex]);
         logScaleParams[varIndex].addParamChangeListener(new ParamChangeListener() {
             @Override
             public void parameterValueChanged(ParamChangeEvent event)
             {
                 final Axis<?>[] aces = dataViewer.getCoordinatesSystem().getAces();
                 aces[varIndex].setLogScale((Boolean) event.getParameter().getValue());
                 aces[varIndex].updateTicks();
                 dataViewer.getCoordinatesSystem().setGrid(new GridFromTicks(aces[0], aces[1], aces[2]));
                 // dataViewer.resetTransformation();
                 dataViewer.update();
             }
         });
     }
 
     /**
      * Update the visible state of the min and max value range inputs for the given variable.
      * 
      * @param varIndex The variable to update te inputs visibility for.
      */
     protected void updateMinMaxVisibility(int varIndex)
     {
         boolean showMinMax = !(Boolean) autoMinMaxParams[varIndex].getValue();
         minParams[varIndex].getEditor().getComponent().setVisible(showMinMax);
         minParams[varIndex].getEditor().getLabelComponent().setVisible(showMinMax);
         maxParams[varIndex].getEditor().getComponent().setVisible(showMinMax);
         maxParams[varIndex].getEditor().getLabelComponent().setVisible(showMinMax);
     }
 
     /**
      * Update the parameter values based on the actual available rasters list.
      * 
      * @param varIndex Index of the variable to set.
      */
     protected void updateParameter(int varIndex)
     {
         RasterDataNode raster = getRaster();
         if (raster == null && availableRasters.length > 0) {
             raster = availableRasters[0];
         }
 
         if (raster != null) {
             rasterNameParams[varIndex].getProperties().setValueSet(availableRasterNames);
             // we don't have to care about product ref here, because raster always has the selected product as parent
             rasterNameParams[varIndex].setValue(new BandExpression(raster.getName(), productNamespace), null);
         } else {
             rasterNameParams[varIndex].getProperties().setValueSet(new String[0]);
         }
 
         if ((Boolean) autoMinMaxParams[varIndex].getValue()) {
             minParams[varIndex].setDefaultValue();
             maxParams[varIndex].setDefaultValue();
         }
     }
 
     protected AbstractButton getHelpButton()
     {
         if (HELP_ID != null) {
             final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Help24.gif"),
                     false);
             helpButton.setToolTipText("Help.");
             helpButton.setName("helpButton");
             HelpSys.enableHelpOnButton(helpButton, HELP_ID);
             return helpButton;
         }
 
         return null;
     }
 
     protected JComponent createUI()
     {
         computePanel = new SingleRoiComputePanel(this, getRaster());
         dataViewer = createDataViewer();
         dataViewer.setNoDataMessage(NO_DATA_MESSAGE);
         setNoDataCoordinatesSystem();
 
         setupPopupMenu();
 
         final TableLayout rightPanelLayout = new TableLayout(1);
         final JPanel rightPanel = new JPanel(rightPanelLayout);
         rightPanelLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
         rightPanelLayout.setRowWeightY(3, 1.0);
         rightPanelLayout.setCellFill(5, 1, TableLayout.Fill.NONE);
         rightPanelLayout.setCellAnchor(5, 1, TableLayout.Anchor.EAST);
         rightPanel.add(computePanel);
 
         final TableLayout maxPointsLayout = new TableLayout(1);
         maxPointsLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
         maxPointsLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
         maxPointsLayout.setTableWeightX(1.0);
         final JPanel maxPointsPanel = new JPanel(maxPointsLayout);
         maxPointsPanel.add(maxPointsParam.getEditor().getLabelComponent());
         maxPointsPanel.add(maxPointsParam.getEditor().getComponent());
         rightPanel.add(maxPointsPanel);
 
         rightPanel.add(createOptionsPane());
 
         rightPanel.add(createChartButtonPanel(dataViewer));
 
         rightPanel.add(new JPanel()); // filler
 
         final JPanel helpPanel = new JPanel(new BorderLayout());
         helpPanel.add(getHelpButton(), BorderLayout.EAST);
         rightPanel.add(helpPanel);
 
         final JPanel mainPanel = new JPanel(new BorderLayout());
         HelpSys.enableHelpKey(mainPanel, HELP_ID);
 
         mainPanel.add((Component) dataViewer, BorderLayout.CENTER);
         mainPanel.add(rightPanel, BorderLayout.EAST);
 
         setTitle(TITLE_PREFIX);
 
         final ComponentListener updateListener = new ComponentAdapter() {
             private void perform()
             {
                 dataViewer.update();
             }
 
             @Override
             public void componentResized(ComponentEvent e)
             {
                 perform();
             }
 
             @Override
             public void componentShown(ComponentEvent e)
             {
                 perform();
             }
         };
         mainPanel.addComponentListener(updateListener);
         ((Component) dataViewer).addComponentListener(updateListener);
 
         dataViewer.update();
 
         return mainPanel;
     }
 
     private RasterDataNode getRaster(int varIndex)
     {
         if (product == null) {
             return null;
         }
         final BandExpression expression = (BandExpression) rasterNameParams[varIndex].getValue();
         final RasterDataNode raster = expression.getNode(varIndex);
         Debug.assertTrue(raster != null);
         return raster;
     }
 
     /**
      * Update the content to reflect the current selected nodes.
      */
     protected void updateContent()
     {
         if (dataViewer != null) {
             availableRasters = createAvailableRastersArray();
             availableRasterNames = createAvailableRasterNamesArray();
 
             updateParameter(X_VAR);
             updateParameter(Y_VAR);
             updateParameter(Z_VAR);
             updateParameter(W_VAR);
         }
     }
 
     /**
      * @return Whether it is needed to update the content of this view.
      */
     protected boolean mustUpdateContent()
     {
         return isRasterChanged() || isProductChanged();
     }
 
     /**
      * @return Whether the selected product has changed since last {@link #updateUI()} call.
      */
     public boolean isProductChanged()
     {
         return productChanged;
     }
 
     /**
      * @return Whether the selected raster has changed since last {@link #updateUI()} call.
      */
     public boolean isRasterChanged()
     {
         return rasterChanged;
     }
 
     private void updateUI()
     {
         if (mustUpdateContent()) {
             updateContent();
             rasterChanged = false;
             productChanged = false;
 
             updateComputePane();
         }
 
         if (dataSetChanged) {
             updateChartButtonsState();
             dataSetChanged = false;
         }
 
         updateUIState(X_VAR);
         updateUIState(Y_VAR);
         updateUIState(Z_VAR);
         updateUIState(W_VAR);
 
         getControl().updateUI();
        dataViewer.update();
     }
 
     /**
      * Update the UI of this component in EDT regardless of what thread is this method called from.
      */
     public void invokeUpdateUI()
     {
         if (SwingUtilities.isEventDispatchThread()) {
             updateUI();
         } else {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run()
                 {
                     updateUI();
                 }
             });
         }
     }
 
     private void updateComputePane()
     {
         computePanel.setRaster(getRaster(X_VAR));
     }
 
     private void updateUIState(int varIndex)
     {
         final double min = ((Number) minParams[varIndex].getValue()).doubleValue();
         final double max = ((Number) maxParams[varIndex].getValue()).doubleValue();
         if (min > max) {
             minParams[varIndex].setValue(max, null);
             maxParams[varIndex].setValue(min, null);
         }
         final boolean autoMinMaxEnabled = (Boolean) autoMinMaxParams[varIndex].getValue();
         minParams[varIndex].setUIEnabled(!autoMinMaxEnabled);
         maxParams[varIndex].setUIEnabled(!autoMinMaxEnabled);
         updateMinMaxVisibility(varIndex);
     }
 
     private JPanel createOptionsPane()
     {
         final JPanel optionsPane = GridBagUtils.createPanel();
         final GridBagConstraints gbc = GridBagUtils
                 .createConstraints("fill=HORIZONTAL,weightx=1,insets.top=7,anchor=NORTH");
 
         GridBagUtils.addToPanel(optionsPane, createOptionsPane(X_VAR), gbc, "gridx=0,gridy=0");
         GridBagUtils.addToPanel(optionsPane, createOptionsPane(Y_VAR), gbc, "gridx=1,gridy=0");
         GridBagUtils.addToPanel(optionsPane, createOptionsPane(Z_VAR), gbc, "gridx=0,gridy=1");
         GridBagUtils.addToPanel(optionsPane, createOptionsPane(W_VAR), gbc, "gridx=1,gridy=1");
 
         // TODO maybe better layout
         // final JPanel optionsPane = new JPanel(new FlowLayout());
         // optionsPane.add(createOptionsPane(X_VAR));
         // optionsPane.add(createOptionsPane(Y_VAR));
         // optionsPane.add(createOptionsPane(Z_VAR));
         // optionsPane.add(createOptionsPane(W_VAR));
 
         return optionsPane;
     }
 
     private JPanel createOptionsPane(final int varIndex)
     {
 
         final JPanel optionsPane = GridBagUtils.createPanel();
         final GridBagConstraints gbc = GridBagUtils.createConstraints("anchor=WEST,fill=HORIZONTAL");
 
         GridBagUtils.setAttributes(gbc, "gridwidth=2,gridy=0,insets.top=0");
         GridBagUtils.addToPanel(optionsPane, rasterNameParams[varIndex].getEditor().getComponent(), gbc,
                 "gridx=0,weightx=1");
 
         GridBagUtils.setAttributes(gbc, "gridwidth=2,gridy=1,insets.top=4");
         GridBagUtils.addToPanel(optionsPane, autoMinMaxParams[varIndex].getEditor().getComponent(), gbc,
                 "gridx=0,weightx=1");
 
         GridBagUtils.setAttributes(gbc, "gridwidth=1,gridy=2,insets.top=2");
         GridBagUtils.addToPanel(optionsPane, minParams[varIndex].getEditor().getLabelComponent(), gbc,
                 "gridx=0,weightx=0");
         GridBagUtils.addToPanel(optionsPane, minParams[varIndex].getEditor().getComponent(), gbc, "gridx=1,weightx=1");
 
         GridBagUtils.setAttributes(gbc, "gridwidth=1,gridy=3,insets.top=2");
         GridBagUtils.addToPanel(optionsPane, maxParams[varIndex].getEditor().getLabelComponent(), gbc,
                 "gridx=0,weightx=0");
         GridBagUtils.addToPanel(optionsPane, maxParams[varIndex].getEditor().getComponent(), gbc, "gridx=1,weightx=1");
 
         if (varIndex != W_VAR) {
             GridBagUtils.setAttributes(gbc, "gridwidth=1,gridy=4,insets.top=4");
             GridBagUtils.addToPanel(optionsPane, scaleParams[varIndex].getEditor().getLabelComponent(), gbc,
                     "gridx=0,weightx=1");
             GridBagUtils.addToPanel(optionsPane, scaleParams[varIndex].getEditor().getComponent(), gbc,
                     "gridx=1,weightx=1");
         }
 
         GridBagUtils.setAttributes(gbc, "gridwidth=2,gridy=5,insets.top=4");
         GridBagUtils.addToPanel(optionsPane, logScaleParams[varIndex].getEditor().getComponent(), gbc,
                 "gridx=0,weightx=1");
 
         optionsPane.setBorder(BorderFactory.createTitledBorder(VAR_NAMES[varIndex] + "-Band"
                 + (varIndex == W_VAR ? " (optional)" : ""))); /* I18N */
 
         return optionsPane;
     }
 
     @Override
     public void compute(final Mask selectedMask)
     {
         final RasterDataNode rasterX = getRaster(X_VAR);
         final RasterDataNode rasterY = getRaster(Y_VAR);
         final RasterDataNode rasterZ = getRaster(Z_VAR);
         final RasterDataNode rasterW = getRaster(W_VAR);
 
         final Boolean autoX = (Boolean) autoMinMaxParams[X_VAR].getValue();
         final Boolean autoY = (Boolean) autoMinMaxParams[Y_VAR].getValue();
         final Boolean autoZ = (Boolean) autoMinMaxParams[Z_VAR].getValue();
         final Boolean autoW = (Boolean) autoMinMaxParams[W_VAR].getValue();
 
         final Double minX = autoX || minParams[X_VAR].getValue() == null ? null
                 : ((Number) minParams[X_VAR].getValue()).doubleValue();
         final Double minY = autoY || minParams[Y_VAR].getValue() == null ? null
                 : ((Number) minParams[Y_VAR].getValue()).doubleValue();
         final Double minZ = autoZ || minParams[Z_VAR].getValue() == null ? null
                 : ((Number) minParams[Z_VAR].getValue()).doubleValue();
         final Double minW = autoW || minParams[W_VAR].getValue() == null ? null
                 : ((Number) minParams[W_VAR].getValue()).doubleValue();
 
         final Double maxX = autoX || maxParams[X_VAR].getValue() == null ? null
                 : ((Number) maxParams[X_VAR].getValue()).doubleValue();
         final Double maxY = autoY || maxParams[Y_VAR].getValue() == null ? null
                 : ((Number) maxParams[Y_VAR].getValue()).doubleValue();
         final Double maxZ = autoZ || maxParams[Z_VAR].getValue() == null ? null
                 : ((Number) maxParams[Z_VAR].getValue()).doubleValue();
         final Double maxW = autoW || maxParams[W_VAR].getValue() == null ? null
                 : ((Number) maxParams[W_VAR].getValue()).doubleValue();
 
         if (rasterX == null || rasterY == null || rasterZ == null) { // rasterW is optional
             return;
         }
 
         final ProgressMonitorSwingWorker<DataSet, Object> swingWorker = new ProgressMonitorSwingWorker<DataSet, Object>(
                 getControl(), "Computing scatter plot") {
 
             @Override
             protected DataSet doInBackground(ProgressMonitor pm) throws Exception
             {
                 final BandDataSource<?> sourceX = BandDataSource.createForBand(rasterX, null, minX, maxX);
                 final BandDataSource<?> sourceY = BandDataSource.createForBand(rasterY, null, minY, maxY);
                 final BandDataSource<?> sourceZ = BandDataSource.createForBand(rasterZ, null, minZ, maxZ);
                 final BandDataSource<?> sourceW = rasterW != null ? BandDataSource.createForBand(rasterW, null, minW,
                         maxW) : null;
 
                 final DataSourceSet sourceSet = (sourceW == null) ? BandDataSourceSet3D.create(sourceX, sourceY,
                         sourceZ, selectedMask) : BandDataSourceSet4D.create(sourceX, sourceY, sourceZ, sourceW,
                         selectedMask);
 
                 final Integer maxPoints = maxPointsParam.getValue().equals(0) ? null : (Integer) maxPointsParam
                         .getValue();
 
                 pm.beginTask("Computing scatter plot...", 100);
                 final ProgressMonitor subPM = SubProgressMonitor.create(pm, 100);
                 try {
                     return AbstractDataSet.createFromDataSources(maxPoints, sourceSet, subPM);
                 } catch (Exception e) {
                     System.err.println(e);
                     e.printStackTrace();
                     return null;
                 } finally {
                     pm.done();
                 }
             }
 
             @Override
             public void done()
             {
                 DataSet result = null;
                 Exception ex = null;
                 try {
                     result = get();
                 } catch (Exception e) {
                     ex = e;
                 }
 
                 computePanel.enableComputeButton();
 
                 if (result != null) {
                     dataViewer.setDataSet(result);
 
                     final String labelX = ((ComboBoxEditor) rasterNameParams[X_VAR].getEditor()).getTextComponent()
                             .getText() + " (x)"; /* I18N */
                     final String labelY = ((ComboBoxEditor) rasterNameParams[Y_VAR].getEditor()).getTextComponent()
                             .getText() + " (y)"; /* I18N */
                     final String labelZ = ((ComboBoxEditor) rasterNameParams[Z_VAR].getEditor()).getTextComponent()
                             .getText() + " (z)"; /* I18N */
                     final String labelW = ((ComboBoxEditor) rasterNameParams[W_VAR].getEditor()).getTextComponent()
                             .getText() + " (w)"; /* I18N */
 
                     final Double scaleX = (Double) scaleParams[X_VAR].getValue();
                     final Double scaleY = (Double) scaleParams[Y_VAR].getValue();
                     final Double scaleZ = (Double) scaleParams[Z_VAR].getValue();
 
                     final Boolean logScaleX = (Boolean) logScaleParams[X_VAR].getValue();
                     final Boolean logScaleY = (Boolean) logScaleParams[Y_VAR].getValue();
                     final Boolean logScaleZ = (Boolean) logScaleParams[Z_VAR].getValue();
                     final Boolean logScaleW = (Boolean) logScaleParams[W_VAR].getValue();
 
                     setCoordinatesSystemFromCurrentDataSet(minX, maxX, labelX, scaleX, logScaleX, minY, maxY, labelY,
                             scaleY, logScaleY, minZ, maxZ, labelZ, scaleZ, logScaleZ, minW, maxW, labelW, logScaleW,
                             AbstractColorProvider.getDefaultColorProvider());
 
                     logScaleParams[X_VAR].setValue(rasterX.isLog10Scaled(), null);
                     logScaleParams[Y_VAR].setValue(rasterY.isLog10Scaled(), null);
                     logScaleParams[Z_VAR].setValue(rasterZ.isLog10Scaled(), null);
                     if (rasterW != null)
                         logScaleParams[W_VAR].setValue(rasterW.isLog10Scaled(), null);
                 } else {
                     if (ex != null) {
                         JOptionPane.showMessageDialog(getControl(),
                                 "Failed to compute scatter plot.\n" + ex.getCause() != null ? ex.getCause()
                                         .getMessage() : ex.getMessage(), CHART_TITLE, JOptionPane.ERROR_MESSAGE);
                     } else {
                         JOptionPane.showMessageDialog(getControl(), "Failed to compute scatter plot.", CHART_TITLE,
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     dataViewer.setDataSet(null);
                     setNoDataCoordinatesSystem();
                 }
                 dataSetChanged = true;
                 updateUI();
             }
         };
         swingWorker.execute();
     }
 
     /**
      * @return The textual summary of the displayed data.
      */
     protected String getDataAsText()
     {
         final StringBuffer sb = new StringBuffer(dataViewer.getDataSet().size());
 
         final Product product = getRaster(X_VAR).getProduct();
         sb.append("Product name:\t").append(product.getName()).append("\n");
 
         final int[] vars = (getRaster(W_VAR) != null) ? new int[] { X_VAR, Y_VAR, Z_VAR, W_VAR } : new int[] { X_VAR,
                 Y_VAR, Z_VAR };
         for (int varIndex : vars) {
             if (getRaster(varIndex).getProduct() != product)
                 sb.append("Dataset ").append(VAR_NAMES[varIndex]).append(" product name:\t")
                         .append(getRaster(varIndex).getProduct().getName()).append("\n");
             sb.append("Dataset ").append(VAR_NAMES[varIndex]).append(" name:\t")
                     .append(dataViewer.getDataSet().getSourceName(varIndex)).append("\n");
         }
 
         for (int varIndex : vars) {
             sb.append('\n');
             sb.append(dataViewer.getDataSet().getSourceName(varIndex)).append(" minimum:\t")
                     .append(dataViewer.getDataSet().getMin(varIndex)).append("\t")
                     .append(getRaster(varIndex).getUnit()).append("\n");
             sb.append(dataViewer.getDataSet().getSourceName(varIndex)).append(" maximum:\t")
                     .append(dataViewer.getDataSet().getMax(varIndex)).append("\t")
                     .append(getRaster(varIndex).getUnit()).append("\n");
         }
         sb.append('\n');
 
         sb.append("Displayed points:\n");
         for (int varIndex : vars) {
             sb.append(dataViewer.getDataSet().getSourceName(varIndex));
             sb.append('\t');
         }
         sb.append('\n');
 
         if (getRaster(W_VAR) != null) {
             DataPoint4D<NumericType<?>, NumericType<?>, NumericType<?>, NumericType<?>> point = null;
             @SuppressWarnings("unchecked")
             final Iterator<DataPoint4D<NumericType<?>, NumericType<?>, NumericType<?>, NumericType<?>>> it = (Iterator<DataPoint4D<NumericType<?>, NumericType<?>, NumericType<?>, NumericType<?>>>) dataViewer
                     .getDataSet().pointIterator();
             while (it.hasNext()) {
                 point = it.next();
                 sb.append(point.getX());
                 sb.append('\t');
                 sb.append(point.getY());
                 sb.append('\t');
                 sb.append(point.getZ());
                 sb.append('\t');
                 sb.append(point.getW());
                 sb.append('\n');
             }
         } else {
             DataPoint3D<NumericType<?>, NumericType<?>, NumericType<?>> point = null;
             @SuppressWarnings("unchecked")
             final Iterator<DataPoint3D<NumericType<?>, NumericType<?>, NumericType<?>>> it = (Iterator<DataPoint3D<NumericType<?>, NumericType<?>, NumericType<?>>>) dataViewer
                     .getDataSet().pointIterator();
             while (it.hasNext()) {
                 point = it.next();
                 sb.append(point.getX());
                 sb.append('\t');
                 sb.append(point.getY());
                 sb.append('\t');
                 sb.append(point.getZ());
                 sb.append('\n');
             }
         }
 
         return sb.toString();
     }
 
     protected JPanel createChartButtonPanel(final GraphicalDataViewer dataViewer)
     {
         final TableLayout tableLayout = new TableLayout(2);
         final JPanel buttonPane = new JPanel(tableLayout);
         buttonPane.setBorder(BorderFactory.createTitledBorder("Plot"));
 
         zoomAllButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/ZoomAll24.gif"), false);
         zoomAllButton.setToolTipText("Zoom all."); /* I18N */
         zoomAllButton.setName("zoomAllButton.");
         zoomAllButton.addActionListener(new AutoRangeAction());
 
         propertiesButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Edit24.gif"), false);
         propertiesButton.setToolTipText("Edit properties."); /* I18N */
         propertiesButton.setName("propertiesButton.");
         propertiesButton.addActionListener(new PropertiesAction());
 
         saveButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Export24.gif"), false);
         saveButton.setToolTipText("Save chart as image."); /* I18N */
         saveButton.setName("saveButton.");
         saveButton.addActionListener(new SaveAction());
 
         printButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Print24.gif"), false);
         printButton.setToolTipText("Print chart."); /* I18N */
         printButton.setName("printButton.");
         printButton.addActionListener(new PrintAction());
 
         buttonPane.add(zoomAllButton);
         buttonPane.add(propertiesButton);
         buttonPane.add(saveButton);
         buttonPane.add(printButton);
 
         updateChartButtonsState();
         return buttonPane;
     }
 
     /**
      * Update the state of the chart buttons based on the state of other things.
      */
     protected void updateChartButtonsState()
     {
         zoomAllButton.setEnabled(true);
         propertiesButton.setEnabled(true);
 
         final boolean enable = dataViewer.getDataSet() != null;
         printButton.setEnabled(enable);
         saveButton.setEnabled(enable);
     }
 
     /**
      * Set the viewer's coordinates system to a dummy one.
      */
     public void setNoDataCoordinatesSystem()
     {
         dataViewer.setCoordinatesSystem(CoordinatesSystem.createNoDataCoordinatesSystem());
         final Axis<?>[] aces = dataViewer.getCoordinatesSystem().getAces();
         dataViewer.setDataSetToEmpty(aces[X_VAR].getMin().doubleValue(), aces[X_VAR].getMax().doubleValue(),
                 aces[Y_VAR].getMin().doubleValue(), aces[Y_VAR].getMax().doubleValue(), aces[Z_VAR].getMin()
                         .doubleValue(), aces[Z_VAR].getMax().doubleValue());
         dataViewer.getCoordinatesSystem().setShowGrid(true);
     }
 
     /**
      * Set the viewer's coordinates system for the current data set. Non-null parameters are used as bounds,
      * <code>null</code> parameters are autocomputed from the data set's minima and maxima.
      * 
      * @param minX Minimum value for x axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param maxX Maximum value for x axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param xLabel Label for x axis.
      * @param xScale Scale of the x axis.
      * @param xLogScale Use log-scaling for x axis?
      * @param minY Minimum value for y axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param maxY Maximum value for y axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param yLabel Label for y axis.
      * @param yScale Scale of the y axis.
      * @param yLogScale Use log-scaling for y axis?
      * @param minZ Minimum value for z axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param maxZ Maximum value for z axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param zLabel Label for z axis.
      * @param zScale Scale of the z axis.
      * @param zLogScale Use log-scaling for z axis?
      * @param minW Minimum value for w axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param maxW Maximum value for w axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param wLabel Label for w axis.
      * @param wLogScale Use log-scaling for w axis?
      * @param colorProvider The color provider for coloring data points.
      */
     @SuppressWarnings("unchecked")
     public <X extends Number, Y extends Number, Z extends Number, W extends Number> void setCoordinatesSystemFromCurrentDataSet(
             Double minX, Double maxX, String xLabel, double xScale, boolean xLogScale, Double minY, Double maxY,
             String yLabel, double yScale, boolean yLogScale, Double minZ, Double maxZ, String zLabel, double zScale,
             boolean zLogScale, Double minW, Double maxW, String wLabel, boolean wLogScale, ColorProvider colorProvider)
     {
         if (dataViewer.getDataSet() instanceof DataSet3D<?, ?, ?>) {
             DataSet3D<X, Y, Z> dataSet = (DataSet3D<X, Y, Z>) dataViewer.getDataSet();
             dataViewer.setCoordinatesSystem(CoordinatesSystem.createCoordinatesSystem(
                     castToType(dataSet.getMinX(), minX), castToType(dataSet.getMinX(), maxX), xLabel, xScale,
                     xLogScale, castToType(dataSet.getMinY(), minY), castToType(dataSet.getMaxY(), maxY), yLabel,
                     yScale, yLogScale, castToType(dataSet.getMinZ(), minZ), castToType(dataSet.getMaxZ(), maxZ),
                     zLabel, zScale, zLogScale, dataSet));
         } else {
             DataSet4D<X, Y, Z, W> dataSet = (DataSet4D<X, Y, Z, W>) dataViewer.getDataSet();
             dataViewer.setCoordinatesSystem(CoordinatesSystem.createCoordinatesSystem(
                     castToType(dataSet.getMinX(), minX), castToType(dataSet.getMinX(), maxX), xLabel, xScale,
                     xLogScale, castToType(dataSet.getMinY(), minY), castToType(dataSet.getMaxY(), maxY), yLabel,
                     yScale, yLogScale, castToType(dataSet.getMinZ(), minZ), castToType(dataSet.getMaxZ(), maxZ),
                     zLabel, zScale, zLogScale, castToType(dataSet.getMinW(), minW),
                     castToType(dataSet.getMaxW(), maxW), wLabel, wLogScale, dataSet));
         }
         dataViewer.getCoordinatesSystem().setShowGrid(true);
         dataViewer.getCoordinatesSystem().setColorProvider(colorProvider);
     }
 
     /**
      * Creates the data viewer instance to be used within this class.
      * 
      * @return The data viewer.
      */
     private final GraphicalDataViewer createDataViewer()
     {
         GraphicalDataViewer viewer = createDataViewerImpl();
         if (!(viewer instanceof JComponent))
             throw new IllegalStateException(getClass()
                     + ": The viewer returned by createDataViewerImpl() must extend JComponent.");
         return viewer;
     }
 
     /**
      * Create an instance of the data viewer to be used within this class.
      * <p>
      * The returned object must extend {@link JComponent}!
      * 
      * @return The viewer instance.
      */
     protected GraphicalDataViewer createDataViewerImpl()
     {
         return new JOGLDataViewer();
     }
 
     /**
      * Create the right-click popup menu and register the needed mouse listeners.
      */
     protected void setupPopupMenu()
     {
         final List<JMenuItem> disabledWhenNoData = new ArrayList<JMenuItem>(5);
 
         final JMenuItem propertiesItem = new JMenuItem("Properties...", KeyEvent.VK_P); /* I18N */
         propertiesItem.addActionListener(new PropertiesAction());
 
         final JMenuItem saveImageMenuItem = new JMenuItem("Save as...", KeyEvent.VK_S); /* I18N */
         saveImageMenuItem.addActionListener(new SaveAction());
         disabledWhenNoData.add(saveImageMenuItem);
 
         final JMenuItem copyItem = new JMenuItem("Copy", KeyEvent.VK_C); /* I18N */
         copyItem.addActionListener(new CopyAction());
         disabledWhenNoData.add(copyItem);
 
         final JMenuItem printItem = new JMenuItem("Print...", KeyEvent.VK_R); /* I18N */
         printItem.addActionListener(new PrintAction());
         disabledWhenNoData.add(printItem);
 
         final JMenuItem zoomInItem = new JMenuItem("Zoom in", KeyEvent.VK_I); /* I18N */
         zoomInItem.addActionListener(new ZoomInAction());
 
         final JMenuItem zoomOutItem = new JMenuItem("Zoom out", KeyEvent.VK_O); /* I18N */
         zoomOutItem.addActionListener(new ZoomOutAction());
 
         final JMenuItem autoRangeItem = new JMenuItem("Auto range", KeyEvent.VK_A);/* I18N */
         autoRangeItem.addActionListener(new AutoRangeAction());
         disabledWhenNoData.add(autoRangeItem);
 
         final JMenuItem resetViewItem = new JMenuItem("Reset the view", KeyEvent.VK_R);/* I18N */
         resetViewItem.addActionListener(new ResetViewAction());
 
         final JMenuItem copyDataItem = new JMenuItem("Copy data to clipboard", KeyEvent.VK_L);/* I18N */
         copyDataItem.addActionListener(new CopyDataAction());
         disabledWhenNoData.add(copyDataItem);
 
         popupMenu.add(propertiesItem);
         popupMenu.add(new JSeparator());
         popupMenu.add(copyItem);
         popupMenu.add(saveImageMenuItem);
         popupMenu.add(new JSeparator());
         popupMenu.add(printItem);
         popupMenu.add(new JSeparator());
         popupMenu.add(zoomInItem);
         popupMenu.add(zoomOutItem);
         popupMenu.add(new JSeparator());
         popupMenu.add(autoRangeItem);
         popupMenu.add(resetViewItem);
         popupMenu.add(new JSeparator());
         popupMenu.add(copyDataItem);
 
         MouseAdapter popupMouseAdapter = new MouseAdapter() {
             private int       pixelsMoved                      = 0;
             private final int pixelsMovedToDisablePopupShowing = 25;
             private boolean   showPopup                        = false;
             private Integer   prevX                            = null, prevY = null;
 
             @Override
             public void mouseWheelMoved(MouseWheelEvent e)
             {
                 showPopup = false;
             }
 
             @Override
             public void mouseDragged(MouseEvent e)
             {
                 if (!SwingUtilities.isRightMouseButton(e))
                     return;
 
                 int diffX = (prevX == null ? 0 : e.getX() - prevX);
                 int diffY = (prevY == null ? 0 : e.getY() - prevY);
                 prevX = e.getX();
                 prevY = e.getY();
                 pixelsMoved += Math.abs(diffX) + Math.abs(diffY);
 
                 if (pixelsMoved >= pixelsMovedToDisablePopupShowing) {
                     showPopup = false;
                     if (popupMenu.isShowing()) {
                         popupMenu.setVisible(false);
                     }
                 }
             }
 
             @Override
             public void mouseMoved(MouseEvent e)
             {
                 mouseDragged(e);
             }
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 if (!SwingUtilities.isRightMouseButton(e)) {
                     showPopup = false;
                     return;
                 }
 
                 pixelsMoved = 0;
                 showPopup = true;
                 maybeShowPopup(e);
             }
 
             @Override
             public void mouseReleased(MouseEvent e)
             {
                 maybeShowPopup(e);
             }
 
             private void maybeShowPopup(MouseEvent e)
             {
                 if (pixelsMoved >= pixelsMovedToDisablePopupShowing) {
                     showPopup = false;
                     if (popupMenu.isShowing()) {
                         popupMenu.setVisible(false);
                     }
                     return;
                 }
                 if (showPopup && e.isPopupTrigger()) {
                     popupMenu.show(e.getComponent(), e.getX(), e.getY());
                 }
                 final boolean enabled = dataViewer.getDataSet() != null;
                 for (Component item : disabledWhenNoData) {
                     item.setEnabled(enabled);
                 }
             }
         };
         ((JComponent) dataViewer).addMouseListener(popupMouseAdapter);
         ((JComponent) dataViewer).addMouseMotionListener(popupMouseAdapter);
         ((JComponent) dataViewer).addMouseWheelListener(popupMouseAdapter);
     }
 
     /**
      * Return the progress monitor for monitoring the loading of bands.
      * 
      * @return The progress monitor for monitoring the loading of bands.
      */
     protected ProgressMonitor createBandsLoadingProgressMonitor()
     {
         ProgressMonitor progressMonitor = new DialogProgressMonitor(getControl(), "Reading data", ModalityType.MODELESS); /* I18N */
         return progressMonitor;
     }
 
     /**
      * Copy the textual data to the clipboard.
      * 
      * @author Martin Pecka
      */
     protected class CopyDataAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             final Cursor oldCursor = getControl().getCursor();
             try {
                 getControl().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 final String dataAsText = getDataAsText();
                 if (dataAsText != null) {
                     SystemUtils.copyToClipboard(dataAsText);
                 }
             } finally {
                 getControl().setCursor(oldCursor);
             }
         }
     }
 
     /**
      * Reset the view transformation.
      * 
      * @author Martin Pecka
      */
     protected class ResetViewAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             dataViewer.resetTransformation();
         }
     }
 
     /**
      * Zoom out.
      * 
      * @author Martin Pecka
      */
     protected class ZoomOutAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             dataViewer.zoomOut();
         }
     }
 
     /**
      * Zoom in.
      * 
      * @author Martin Pecka
      */
     protected class ZoomInAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             dataViewer.zoomIn();
         }
     }
 
     /**
      * Set zoom to view all the data.
      * 
      * @author Martin Pecka
      */
     protected class AutoRangeAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             dataViewer.setAutoRange();
         }
     }
 
     /**
      * Print the viewer's view.
      * 
      * @author Martin Pecka
      */
     protected class PrintAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             PrinterJob job = PrinterJob.getPrinterJob();
             PageFormat pf = job.defaultPage();
             PageFormat pf2 = job.pageDialog(pf);
             job.setPrintable(dataViewer, pf2);
             if (job.printDialog()) {
                 try {
                     job.print();
                 } catch (PrinterException ex) {
                     JOptionPane.showMessageDialog(getControl(), ex);
                 }
             }
         }
     }
 
     /**
      * Show the plot properties dialog.
      * 
      * @author Martin Pecka
      */
     protected class PropertiesAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             new ChartPropertiesDialog(VisatApp.getApp().getMainFrame(), HELP_ID).show();
         }
     }
 
     /**
      * Copy the viewr's view into clipboard.
      * 
      * @author Martin Pecka
      */
     protected class CopyAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             dataViewer.copyImageToClipboard(new ImageCaptureCallback() {
                 @Override
                 public void onOk()
                 {
                     VisatApp.getApp().setStatusBarMessage("Image copied to the clipboard."); /* I18N */
                 }
 
                 @Override
                 public void onFail()
                 {
                     VisatApp.getApp().showErrorDialog("Message not copied",
                             "There was an error copying the image to the clipboard."); /* I18N */
                 }
 
                 @Override
                 public void onException(Exception e)
                 {
                     onFail();
                     VisatApp.getApp().getLogger().severe(e.toString());
                 }
             });
         }
     }
 
     /**
      * Save the viewer's current view as image.
      * 
      * @author Martin Pecka
      */
     protected class SaveAction implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
             final BeamFileChooser fileChooser = new BeamFileChooser();
             fileChooser.setDialogTitle("Export as image");/* I18N */
             fileChooser.setFileFilter(new ExtensionFileFilter("PNG images", "png"));
             if (fileChooser.showSaveDialog(getPaneWindow()) == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 if (!file.getName().toLowerCase().endsWith(".png")) {
                     final String absolutePathname = file.getAbsolutePath();
                     file = new File(absolutePathname + ".png");
                 }
                 final File targetFile = file;
                 if (targetFile.exists()) {
                     final int overwrite = JOptionPane.showConfirmDialog(getControl(), "Overwrite existing file?",
                             "Overwrite confirmation", JOptionPane.YES_NO_OPTION); /* I18N */
                     if (overwrite != JOptionPane.YES_OPTION)
                         return;
                 }
                 dataViewer.saveImage(targetFile, "PNG", new ImageCaptureCallback() {
 
                     @Override
                     public void onOk()
                     {
                         VisatApp.getApp().setStatusBarMessage("Image successfully saved to " + targetFile.toString()); /* I18N */
                     }
 
                     @Override
                     public void onException(Exception e)
                     {
                         onFail();
                         VisatApp.getApp().getLogger().severe(e.toString());
                     }
 
                     @Override
                     public void onFail()
                     {
                         VisatApp.getApp().showErrorDialog("Image not saved", "There was an error saving the image."); /* I18N */
                     }
 
                 });
             }
         }
     }
 
     /**
      * 
      * <p>
      * This class has to be static in order {@link ParamEditorFactory} to be able to instantiate it.
      * 
      * @author Martin Pecka
      */
     public static class BandInputValidator implements ParamValidator
     {
         protected static DataViewer3DToolView _this = null;
 
         @Override
         public Object parse(Parameter parameter, String text) throws ParamParseException
         {
             return _this.new BandExpression(text, _this.productNamespace);
         }
 
         @Override
         public String format(Parameter parameter, Object value) throws ParamFormatException
         {
             if (value == null)
                 return "";
             BandExpression expr = (BandExpression) value;
             return expr.getExpressionWithCast();
         }
 
         @Override
         public void validate(Parameter parameter, Object value) throws ParamValidateException
         {
             if (value == null) {
                 if (parameter.getProperties().getPropertyValue(VAR_INDEX, X_VAR) != W_VAR)
                     throw new ParamValidateException(parameter, "The value is required."); /* I18N */
             } else {
                 BandExpression expr = (BandExpression) value;
                 if (!expr.isValid())
                     throw new ParamValidateException(parameter,
                             "The given expression isn't a valid band maths expression."); /* I18N */
             }
         }
 
         @Override
         public boolean equalValues(Parameter parameter, Object value1, Object value2)
         {
             final BandExpression expr1 = (BandExpression) value1, expr2 = (BandExpression) value2;
             return expr1.getExpressionWithCast().equals(expr2.getExpressionWithCast());
         }
 
     }
 
     protected class BandExpression extends PossiblyInvalidExpression
     {
 
         protected VirtualBand   virtualBand       = null;
 
         protected final Pattern productRefPattern = Pattern.compile("^\\$([0-9]*)\\.(.*)$");
 
         /**
          * @param namespace
          */
         public BandExpression(Namespace namespace)
         {
             super(namespace);
         }
 
         /**
          * @param source
          */
         public BandExpression(PossiblyInvalidExpression source)
         {
             super(source);
         }
 
         /**
          * @param bandArithmeticExpression
          * @param namespace
          * @param castToType
          */
         public BandExpression(String bandArithmeticExpression, Namespace namespace, int castToType)
         {
             super(bandArithmeticExpression, namespace, castToType);
         }
 
         /**
          * @param bandArihmeticExpression
          * @param namespace
          */
         public BandExpression(String bandArihmeticExpression, Namespace namespace)
         {
             super(bandArihmeticExpression, namespace);
         }
 
         @Override
         protected boolean validateExpression(String expression, boolean canContainCast)
         {
             for (String s : BAND_NAMES) {
                 if (expression.contains(s))
                     return false;
             }
             return super.validateExpression(expression, canContainCast);
         }
 
         /**
          * Return the data node this expression represents.
          * 
          * @param varIndex Index of the variable to create the band for.
          * @return The data node. <code>null</code> if the expression isn't valid or is empty.
          */
         public RasterDataNode getNode(int varIndex)
         {
             // no raster is defined for invalid or empty expressions
             if (!isValid() || isEmpty())
                 return null;
 
             // we created a virtual band for the expression and it is still valid
             if (virtualBand != null && virtualBand.getExpression().equals(expression))
                 return virtualBand;
 
             // if the expression is affected by a cast, we must use a virtual band for it even if it is simple
             if (getExpressionNaturalType() != getType())
                 return setVirtualBand(createVirtualBand(varIndex), varIndex);
 
             // check if the expression is just a simple reference to a raster, or if it contains some more operators
             // for simple expressions, return directly the raster they represent, there's no need to create virtual
             // bands for them
             final Parser parser = new ParserImpl(namespace, false);
             try {
                 final Term term = parser.parse(expression);
                 if (term instanceof Term.Ref) {
                     final Symbol symbol = ((Term.Ref) term).getSymbol();
                     if (symbol instanceof RasterDataSymbol) {
                         setVirtualBand(null, varIndex);
                         return ((RasterDataSymbol) symbol).getRaster();
                     }
                 }
             } catch (ParseException e) { // ignore is fine here
             }
 
             // the expression is complex, we need to create a virtual band for it
             return setVirtualBand(createVirtualBand(varIndex), varIndex);
         }
 
         /**
          * Set the temporary virtual band to the given value (can be <code>null</code>) and registers it with the
          * selected product.
          * 
          * @param band The new band to set.
          * @param varIndex
          * @return The given band.
          */
         protected VirtualBand setVirtualBand(final VirtualBand band, int varIndex)
         {
             final Band oldBand = product.getBand(BAND_NAMES[varIndex]);
             virtualBand = band;
             if (oldBand != null)
                 product.removeBand(oldBand);
             if (band != null)
                 product.addBand(band);
             return virtualBand;
         }
 
         /**
          * Create the virtual band for the current expression and for the given variable index.
          * 
          * @param varIndex Index of the variable to create the band for.
          * @return The virtual band.
          */
         protected VirtualBand createVirtualBand(int varIndex)
         {
             return new VirtualBand(BAND_NAMES[varIndex], getType(), product.getSceneRasterWidth(),
                     product.getSceneRasterHeight(), expression);
         }
     }
 
     /**
      * Editor for band expressions.
      * <p>
      * This class has to be static in order {@link ParamEditorFactory} to be able to instantiate it.
      * 
      * @author Martin Pecka
      */
     public static class BandExpressionEditor extends ComboBoxEditor
     {
 
         protected static DataViewer3DToolView _this = null;
         protected final JComponent            panel;
 
         /**
          * @param parameter
          */
         public BandExpressionEditor(Parameter parameter)
         {
             super(parameter);
 
             panel = new JPanel(new TableLayout(2));
             panel.add(getEditorComponent());
             panel.add(createEditExpressionButton());
         }
 
         @Override
         protected InputVerifier getDefaultInputVerifier()
         {
             final InputVerifier superVerifier = super.getDefaultInputVerifier();
             return new InputVerifier() {
 
                 @Override
                 public boolean verify(JComponent input)
                 {
                     return superVerifier.verify(input);
                 }
 
                 @Override
                 public boolean shouldYieldFocus(JComponent input)
                 {
                     final boolean valid = verify(input);
                     if (!valid) {
                         JOptionPane.showMessageDialog(_this.getPaneControl(),
                                 "The given expression isn't a valid band maths expression.", "Input error", /* I18N */
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     return valid;
                 }
             };
         }
 
         @Override
         public JComponent getComponent()
         {
             return panel;
         }
 
         @Override
         public JComponent getEditorComponent()
         {
             final JComboBox comboBox = (JComboBox) super.getEditorComponent();
             comboBox.setEditor(new BasicComboBoxEditor() {
                 @Override
                 protected JTextField createEditorComponent()
                 {
                     final JTextField textField = super.createEditorComponent();
                     textField.setInputVerifier(getDefaultInputVerifier());
                     return textField;
                 }
 
                 @Override
                 public Component getEditorComponent()
                 {
                     final JTextComponent textEditor = (JTextComponent) super.getEditorComponent();
                     textEditor.getDocument().addDocumentListener(new UniversalDocumentListener() {
                         @Override
                         protected void update()
                         {
                             final String text = textEditor.getText();
                             final BandExpression expr = _this.new BandExpression(text, _this.productNamespace);
                             final int varIndex = getParameter().getProperties().getPropertyValue(VAR_INDEX, X_VAR);
                             if (expr.isValid() && (varIndex == W_VAR || !expr.isEmpty()))
                                 textEditor.setBackground(Color.white);
                             else
                                 textEditor.setBackground(Color.pink);
                         }
                     });
                     return textEditor;
                 }
             });
 
             return comboBox;
         }
 
         /**
          * Create the button that shows the dialog for editing math expression.
          * 
          * @return The button that shows the dialog for editing math expression.
          */
         protected AbstractButton createEditExpressionButton()
         {
             Icon icon = new ImageIcon(getClass().getResource("/images/Edit_Expression24.gif"));
             AbstractButton button = ToolButtonFactory.createButton(icon, false);
             button.setToolTipText("Edit the band maths expression"); /* I18N */
             button.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     final ProductExpressionPane pep = ProductExpressionPane.createGeneralExpressionPane(
                             _this.compatibleProducts, _this.product, VisatApp.getApp().getPreferences());
                     final BandExpression expr = _this.new BandExpression(getTextComponent().getText(),
                             _this.productNamespace);
                     pep.setCode(expr.getExpression());
                     int status = pep.showModalDialog(_this.getPaneWindow(), "Band Maths Expression Editor"); /* I18N */
                     if (status == ModalDialog.ID_OK) {
                         String newExpr = pep.getCode();
                         expr.setExpression(newExpr);
                         // workaround to call property change listeners
                         getParameter().setValue(expr, null);
                         getParameter().updateUI();
                     }
                     pep.dispose();
                 }
             });
             return button;
         }
 
     }
 
     /**
      * The listener added to the product tree of the application when the view is opened.
      * 
      * @return The listener.
      */
     private ProductTreeListener createProductTreeListener()
     {
         return new ProductTreeListenerAdapter() {
             @Override
             public void productNodeSelected(ProductNode productNode, int clickCount)
             {
                 RasterDataNode raster = null;
                 if (productNode instanceof RasterDataNode) {
                     raster = (RasterDataNode) productNode;
                 }
                 VectorDataNode vector = null;
                 if (productNode instanceof VectorDataNode) {
                     vector = (VectorDataNode) productNode;
                     final ProductSceneView sceneView = VisatApp.getApp().getSelectedProductSceneView();
                     if (sceneView != null) {
                         raster = sceneView.getRaster();
                     }
                 }
                 Product product = productNode.getProduct();
                 // if (raster != null)
                 selectionChanged(product, raster, vector);
             }
         };
     }
 
     /**
      * The listener added to the application's frame when the view is opened.
      * 
      * @return The listener.
      */
     private InternalFrameListener createInternalFrameListener()
     {
         return new InternalFrameAdapter() {
             @Override
             public void internalFrameActivated(InternalFrameEvent e)
             {
                 final Container contentPane = e.getInternalFrame().getContentPane();
                 if (contentPane instanceof ProductSceneView) {
                     final ProductSceneView view = (ProductSceneView) contentPane;
                     VectorDataNode vectorDataNode = getVectorDataNode(view);
                     selectionChanged(view.getRaster().getProduct(), view.getRaster(), vectorDataNode);
                 }
             }
 
             private VectorDataNode getVectorDataNode(ProductSceneView view)
             {
                 final Layer rootLayer = view.getRootLayer();
                 final Layer layer = LayerUtils.getChildLayer(rootLayer, LayerUtils.SearchMode.DEEP,
                         VectorDataLayerFilterFactory.createGeometryFilter());
                 VectorDataNode vectorDataNode = null;
                 if (layer instanceof VectorDataLayer) {
                     VectorDataLayer vdl = (VectorDataLayer) layer;
                     vectorDataNode = vdl.getVectorDataNode();
                 }
                 return vectorDataNode;
             }
 
             @Override
             public void internalFrameDeactivated(InternalFrameEvent e)
             {
                 final Container contentPane = e.getInternalFrame().getContentPane();
                 if (contentPane instanceof ProductSceneView) {
                     selectionChanged(null, null, null);
                 }
             }
         };
     }
 
     /**
      * Create the listener added to the selected product.
      * 
      * @return The listener.
      */
     private ProductNodeListener createProductNodeListener()
     {
         return new ProductNodeListenerAdapter() {};
     }
 
     /**
      * Remove the product node listener from oldProduct and add it to newProduct.
      * 
      * @param oldProduct The old selected product.
      * @param newProduct The new selected product.
      */
     private void transferProductNodeListener(Product oldProduct, Product newProduct)
     {
         if (oldProduct != newProduct) {
             if (oldProduct != null) {
                 oldProduct.removeProductNodeListener(productNodeListener);
             }
             if (newProduct != null) {
                 newProduct.addProductNodeListener(productNodeListener);
             }
         }
     }
 
     /**
      * The dialog for setting chart properties.
      * 
      * @author Martin Pecka
      */
     protected class ChartPropertiesDialog extends ModalDialog
     {
         private final ParamChangeListener paramListener       = new ParamChangeListener() {
 
                                                                   @Override
                                                                   public void parameterValueChanged(
                                                                           ParamChangeEvent event)
                                                                   {
                                                                       ChartPropertiesDialog.this.getJDialog().pack();
                                                                   }
                                                               };
 
         private final PropertyMap         propertiesUnchanged = new PropertyMap();
 
         @Override
         public int show()
         {
             chartPropertiesGroup.getParameterValues(propertiesUnchanged);
             chartPropertiesGroup.addParamChangeListener(paramListener);
 
             // sometimes the editors remain in an old state, so make sure to update them all
             for (int i = 0; i < chartPropertiesGroup.getNumParameters(); i++) {
                 chartPropertiesGroup.getParameterAt(i).getEditor().reconfigureUI();
             }
 
             return super.show();
         }
 
         @Override
         public void hide()
         {
             super.hide();
             chartPropertiesGroup.removeParamChangeListener(paramListener);
         }
 
         public ChartPropertiesDialog(Window parent, String helpId)
         {
             super(parent, "Chart properties", ID_OK | ID_RESET | ID_CANCEL, helpId); /* I18N */
 
             {
                 // BEAM puts the buttons in order OK | CANCEL | RESET, which is counter-intuitive, so reorder them
                 JComponent button = getButton(ID_CANCEL);
                 Container buttonParent = button.getParent();
                 buttonParent.remove(button);
                 buttonParent.add(button, 0);
 
                 button = getButton(ID_RESET);
                 buttonParent = button.getParent();
                 buttonParent.remove(button);
                 buttonParent.add(button, 0);
             }
 
             final JTabbedPane tabs = new JTabbedPane();
             setContent(tabs);
 
             final JPanel titlePanel = GridBagUtils.createPanel();
             final JTabbedPane plotPanel = new JTabbedPane();
             final JPanel gridPanel = GridBagUtils.createPanel();
             final JPanel otherPanel = GridBagUtils.createPanel();
             tabs.add("Title", titlePanel);/* I18N */
             tabs.add("Plot", plotPanel);/* I18N */
             tabs.add("Grid", gridPanel);/* I18N */
             tabs.add("Other", otherPanel);/* I18N */
 
             GridBagConstraints gbc = GridBagUtils.createConstraints("fill=HORIZONTAL,weightx=1,weighty=0,anchor=NORTH");
 
             // TITLE->GENERAL
             final JPanel general = GridBagUtils.createPanel();
             GridBagUtils.addToPanel(titlePanel, general, gbc, "gridx=0,gridy=0,weighty=1");
             general.setBorder(BorderFactory.createTitledBorder("General"));/* I18N */
 
             gbc = GridBagUtils.createConstraints("fill=HORIZONTAL,weightx=1,weighty=0,anchor=NORTHWEST,gridheight=1");
 
             GridBagUtils.addToPanel(general, showTitleParam.getEditor().getComponent(), gbc,
                     "gridx=0,gridy=0,gridwidth=2");
 
             GridBagUtils.addToPanel(general, titleTextParam.getEditor().getLabelComponent(), gbc,
                     "gridx=0,gridy=1,gridwidth=1,weightx=0");
             GridBagUtils.addToPanel(general, titleTextParam.getEditor().getComponent(), gbc,
                     "gridx=1,gridy=1,weightx=1");
 
             GridBagUtils.addToPanel(general, titleFontParam.getEditor().getLabelComponent(), gbc,
                     "gridx=0,gridy=2,weightx=0");
             GridBagUtils.addToPanel(general, titleFontParam.getEditor().getComponent(), gbc,
                     "gridx=1,gridy=2,weightx=1");
 
             GridBagUtils.addToPanel(general, titleColorParam.getEditor().getLabelComponent(), gbc,
                     "gridx=0,gridy=3,weightx=0");
             GridBagUtils.addToPanel(general, titleColorParam.getEditor().getComponent(), gbc,
                     "gridx=1,gridy=3,weightx=1,weighty=0");
 
             // PLOT->AXIS
             for (int i = 0; i < 4; i++) {
                 final JPanel axisPanel = GridBagUtils.createPanel();
                 plotPanel.addTab(VAR_NAMES[i] + " axis", axisPanel); /* I18N */
 
                 final JPanel axisGeneralPanel = GridBagUtils.createPanel();
                 final JPanel axisTicksPanel = GridBagUtils.createPanel();
                 final JPanel axisOuter = GridBagUtils.createPanel();
                 GridBagUtils.addToPanel(axisPanel, axisOuter, gbc,
                         "gridx=0,gridy=0,fill=HORIZONTAL,anchor=NORTHWEST,weighty=1");
                 GridBagUtils.addToPanel(axisOuter, axisGeneralPanel, gbc,
                         "gridx=0,gridy=0,fill=BOTH,weightx=1,weighty=0,anchor=NORTHWEST,gridwidth=1");
                 GridBagUtils.addToPanel(axisOuter, axisTicksPanel, gbc, "gridx=0,gridy=1");
 
                 // PLOT->AXIS->GENERAL
 
                 axisGeneralPanel.setBorder(BorderFactory.createTitledBorder("General")); /* I18N */
 
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleTextParams[i].getEditor().getLabelComponent(), gbc,
                         "gridx=0,gridy=0,gridwidth=1,weightx=0,weighty=0,fill=HORIZONTAL");
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleTextParams[i].getEditor().getComponent(), gbc,
                         "gridx=1,gridy=0,weightx=1");
 
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleFontParams[i].getEditor().getLabelComponent(), gbc,
                         "gridx=0,gridy=1,weightx=0");
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleFontParams[i].getEditor().getComponent(), gbc,
                         "gridx=1,gridy=1,weightx=1");
 
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleColorParams[i].getEditor().getLabelComponent(), gbc,
                         "gridx=0,gridy=2,weightx=0");
                 GridBagUtils.addToPanel(axisGeneralPanel, axisTitleColorParams[i].getEditor().getComponent(), gbc,
                         "gridx=1,gridy=2,weightx=1");
 
                 // PLOT->AXIS->TICKS
 
                 axisTicksPanel.setBorder(BorderFactory.createTitledBorder("Ticks")); /* I18N */
 
                 GridBagUtils.addToPanel(axisTicksPanel, axisShowLabelsParams[i].getEditor().getComponent(), gbc,
                         "gridx=0,gridy=0,gridwidth=2,weightx=1,weighty=0,fill=HORIZONTAL");
 
                 GridBagUtils.addToPanel(axisTicksPanel, axisLabelFontParams[i].getEditor().getLabelComponent(), gbc,
                         "gridx=0,gridy=1,weightx=0,gridwidth=1");
                 GridBagUtils.addToPanel(axisTicksPanel, axisLabelFontParams[i].getEditor().getComponent(), gbc,
                         "gridx=1,gridy=1,weightx=1");
 
                 GridBagUtils.addToPanel(axisTicksPanel, axisShowTicksParams[i].getEditor().getComponent(), gbc,
                         "gridx=0,gridy=2,weightx=1,gridwidth=2");
             }
 
             // GRID -> GENERAL
             final JPanel gridGeneral = GridBagUtils.createPanel();
             GridBagUtils.addToPanel(gridPanel, gridGeneral, gbc, "gridx=0,gridy=0,weighty=1");
             gridGeneral.setBorder(BorderFactory.createTitledBorder("General"));/* I18N */
 
             gbc = GridBagUtils.createConstraints("fill=HORIZONTAL,weightx=1,weighty=0,anchor=NORTHWEST,gridheight=1");
 
             GridBagUtils.addToPanel(gridGeneral, showGridParam.getEditor().getComponent(), gbc, "gridx=0,gridy=0");
 
             // OTHER -> GENERAL
             final JPanel otherGeneral = GridBagUtils.createPanel();
             GridBagUtils.addToPanel(otherPanel, otherGeneral, gbc, "gridx=0,gridy=0,weighty=1");
             otherGeneral.setBorder(BorderFactory.createTitledBorder("General"));/* I18N */
 
             gbc = GridBagUtils.createConstraints("fill=HORIZONTAL,weightx=1,weighty=0,anchor=NORTHWEST,gridheight=1");
 
             GridBagUtils.addToPanel(otherGeneral, backgroundColorParam.getEditor().getComponent(), gbc,
                     "gridx=1,gridy=0");
             GridBagUtils.addToPanel(otherGeneral, backgroundColorParam.getEditor().getLabelComponent(), gbc,
                     "gridx=0,gridy=0,weightx=0");
         }
 
         @Override
         protected void onReset()
         {
             for (int i = 0; i < chartPropertiesGroup.getNumParameters(); i++) {
                 final Parameter param = chartPropertiesGroup.getParameterAt(i);
                 param.setDefaultValue();
             }
             super.onReset();
         }
 
         @Override
         protected void onOK()
         {
             super.onOK();
             updateUI();
         }
 
         @Override
         protected void onCancel()
         {
             chartPropertiesGroup.setParameterValues(propertiesUnchanged, null);
             super.onCancel();
             updateUI();
         }
     }
 }
