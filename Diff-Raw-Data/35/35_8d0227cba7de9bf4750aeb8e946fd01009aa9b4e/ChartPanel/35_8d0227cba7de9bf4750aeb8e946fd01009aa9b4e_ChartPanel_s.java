 package org.gladstoneinstitutes.customgraphicsui.internal.chart;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Arrays;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.CardLayout;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Color;
 import java.awt.Paint;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.RenderingHints;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JComponent;
 import javax.swing.JRadioButton;
 import javax.swing.JLabel;
 import javax.swing.ButtonGroup;
 import javax.swing.BorderFactory;
 
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.View;
 
 import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
 import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
 import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
 import org.cytoscape.view.presentation.customgraphics.PaintedShape;
 
 import org.gladstoneinstitutes.customgraphicsui.internal.CustomGraphicsFactoryManager;
 import org.gladstoneinstitutes.customgraphicsui.internal.util.EasyGBC;
 
 public class ChartPanel extends JPanel {
   final ChartPreview preview;
   final Map<String,ChartSubpanel> subpanels = new HashMap<String,ChartSubpanel>();
   ChartSubpanel currentSubpanel = null;
 
   public ChartPanel(final CustomGraphicsFactoryManager cgMgr) {
     super(new GridBagLayout());
     preview = new ChartPreview(cgMgr);
 
     final JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     typePanel.add(new JLabel("Type: "));
     final ButtonGroup typeGroup = new ButtonGroup();
     final PropertyChangeListener cgUpdater = new PropertyChangeListener() {
       public void propertyChange(final PropertyChangeEvent e) {
         preview.assignCg(currentSubpanel.getCgName(), currentSubpanel.buildCgString());
       }
     };
 
     final CardLayout cardLayout = new CardLayout();
     final JPanel subpanelsPanel = new JPanel(cardLayout);
     for (final ChartSubpanel subpanel : Arrays.asList(new BarChartSubpanel(), new PieChartSubpanel())) {
       subpanels.put(subpanel.getUserName(), subpanel);
       subpanelsPanel.add(subpanel, subpanel.getUserName());
       subpanel.addPropertyChangeListener(cgUpdater);
       final JRadioButton button = new JRadioButton(subpanel.getUserName());
       typeGroup.add(button);
       typePanel.add(button);
       button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           currentSubpanel = subpanel;
           cardLayout.show(subpanelsPanel, subpanel.getUserName());
           cgUpdater.propertyChange(null);
         }
       });
     }
 
     preview.setBorder(BorderFactory.createLineBorder(new Color(0x858585), 1));
 
     final EasyGBC c = new EasyGBC();
     super.add(preview, c.spanV(2).expandHV().insets(10, 10, 10, 0));
    super.add(typePanel, c.noSpan().right().noExpand().insets(10, 10, 0, 0));
     super.add(subpanelsPanel, c.down().right().expandV().insets(0, 10, 10, 10));
   }
 
   public void setup(final CyNetworkView networkView, final View<CyNode> nodeView) {
     preview.setup(networkView, nodeView);
     for (final ChartSubpanel subpanel : subpanels.values()) {
       subpanel.setup(networkView, nodeView);
     }
   }
 }
 
 class ChartPreview extends JComponent {
   final CustomGraphicsFactoryManager cgMgr;
 
   CyNetworkView networkView = null;
   View<CyNode> nodeView = null;
   CyCustomGraphicsFactory<? extends CustomGraphicLayer> factory = null;
   String cgString = null;
 
   public ChartPreview(final CustomGraphicsFactoryManager cgMgr) {
     super.setPreferredSize(new Dimension(100, 100));
     this.cgMgr = cgMgr;
   }
 
   public void setup(final CyNetworkView networkView, final View<CyNode> nodeView) {
     this.networkView = networkView;
     this.nodeView = nodeView;
   }
 
   public void assignCg(final String cgName, final String cgString) {
     this.factory = cgMgr.getFactory(cgName);
     this.cgString = cgString;
     super.repaint();
   }
 
   // members so that these objects are not allocated every time paintComponent is called
   final Rectangle2D.Float componentBounds = new Rectangle2D.Float();
   final Rectangle2D.Float chartBounds     = new Rectangle2D.Float();
   final Insets            insets = new Insets(0, 0, 0, 0);
   final AffineTransform   at     = new AffineTransform();
 
   protected void paintComponent(Graphics g) {
     if (networkView == null || nodeView == null || factory == null || cgString == null)
       return;
 
      // obtain the custom graphics
     final CyCustomGraphics<? extends CustomGraphicLayer> customGraphics = factory.getInstance(cgString);
     final float fit = customGraphics.getFitRatio();
 
       // setup g2d
     final Graphics2D g2d = (Graphics2D) g;
     g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
       // calculate componentBounds -- the rect wherein the chart is painted
     super.getInsets(insets);
     componentBounds.setRect(insets.left, insets.top, super.getWidth() - insets.left - insets.right, super.getHeight() - insets.top - insets.bottom);
 
       // calculate chartBounds -- the rect that's a union of each chart layer's shape boundaries
     chartBounds.setRect(0.0, 0.0, 0.0, 0.0);
    final List<? extends CustomGraphicLayer> layers = customGraphics.getLayers(networkView, nodeView);
    if (layers == null)
      return;
     for (final CustomGraphicLayer layer : layers) {
       final PaintedShape ps = (PaintedShape) layer;
       final Shape shape = ps.getShape();
       chartBounds.add(shape.getBounds2D());
     }
 
       // transform g2d such that chartBounds is scaled and translated to fit exactly in the middle of componentBounds
     final AffineTransform originalAt = g2d.getTransform();
     double factor;
     if (componentBounds.height / componentBounds.width > chartBounds.height / chartBounds.width) {
       factor = componentBounds.width / chartBounds.width;
     } else {
       factor = componentBounds.height / chartBounds.height;
     }
     factor *= fit;
     g2d.translate(componentBounds.x + componentBounds.width / 2.0, componentBounds.y + componentBounds.height / 2.0);
     g2d.scale(factor, factor);
     g2d.translate(-chartBounds.x - chartBounds.width / 2.0, -chartBounds.y - chartBounds.height / 2.0);
 
       // paint each layer
     for (final CustomGraphicLayer layer : layers) {
       final PaintedShape ps = (PaintedShape) layer;
       final Shape shape = ps.getShape();
 
       if (ps.getStroke() != null) {
         Paint strokePaint = ps.getStrokePaint();
         if (strokePaint == null) strokePaint = Color.BLACK;
         g2d.setPaint(strokePaint);
         g2d.setStroke(ps.getStroke());
         g2d.draw(shape);
       }
       g2d.setPaint(ps.getPaint());
       g2d.fill(shape);
     }
 
       // reset the transform so that other things being painted with this g2d won't get messed up like component border
     g2d.setTransform(originalAt);
   }
 }
