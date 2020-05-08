 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.equalizer;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D.Double;
 import java.awt.geom.Rectangle2D;
 
 import java.lang.reflect.Field;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.TreeMap;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.EventListenerList;
 import javax.swing.plaf.SliderUI;
 import javax.swing.plaf.basic.BasicSliderUI;
 
 /**
  * This component is a view to an {@link EqualizerModel}. It displays the different categories of the model using
  * sliders which can be used to adjust the value of the respective category. Optionally, the range of the model can be
  * visualised right next to the slider group and a spline graph can be drawn to connect all the slider knobs of the
  * categories to visually indicate their togetherness.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0
  */
 public class EqualizerPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** The default paint used for the spline, a pastel greenish tone. */
     public static final Paint DEFAULT_PAINT;
     /** The default stroke used for the spline, a dashed line, 3 pixels wide. */
     public static final Stroke DEFAULT_STROKE;
 
     private static final String PROP_MODEL_INDEX;
 
     static {
         // cannot use single integer because java only supports 31-bit integers (32nd bit is for sign indication)
         DEFAULT_PAINT = new Color(Integer.decode("0x17"), // NOI18N
                 Integer.decode("0xA8"), // NOI18N
                 Integer.decode("0x14"), // NOI18N
                 Integer.decode("0xDD")); // NOI18N
         DEFAULT_STROKE = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 7, 3 }, 0);
 
         PROP_MODEL_INDEX = "__prop_model_index__"; // NOI18N
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private final EqualizerModelListener equalizerModelL;
     private final ChangeListener sliderChangeL;
 
     private EqualizerModel model;
     private TreeMap<Integer, JSlider> sliderMap;
     private boolean updateInProgress;
     private Paint splinePaint;
     private Stroke splineStroke;
     private boolean rangeAxisPainted;
     private boolean splinePainted;
     private boolean updateSplineWhileAdjusting;
     private boolean updateModelWhileAdjusting;
     private JLabel lblRangeAxis;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel pnlEqualizer;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EqualizerPanel object using:
      *
      * <ul>
      *   <li>a single category model</li>
      *   <li>splinePaint=<code>DEFAULT_PAINT</code></li>
      *   <li>splineStroke=<code>DEFAULT_STROKE</code></li>
      *   <li>splinePainted=<code>true</code></li>
      *   <li>rangeAxisName= <code>null</code></li>
      *   <li>rangeAxisPainted= <code>true</code></li>
      *   <li>updateModelWhileAdjusting= <code>false</code></li>
      *   <li>updateSplineWhileAdjusting= <code>true</code></li>
      * </ul>
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      *
      * @see     EqualizerPanel(de.cismet.commons.gui.equalizer.EqualizerModel, java.awt.Paint, java.awt.Stroke, boolean,
      *          java.lang.String, boolean, boolean, boolean)
      */
     public EqualizerPanel() {
         this(
             new EqualizerModel() {
 
                 private final EventListenerList list = new EventListenerList();
 
                 private final int[] value = new int[1];
 
                 @Override
                 public Range getRange() {
                     return new Range(0, 10);
                 }
 
                 @Override
                 public String getEqualizerCategory(final int index) {
                     return "EQ1"; // NOI18N
                 }
 
                 @Override
                 public int getEqualizerCategoryCount() {
                     return 1;
                 }
 
                 @Override
                 public int getValueAt(final int index) {
                     return value[index];
                 }
 
                 @Override
                 public void setValueAt(final int index, final int value) {
                     if ((value < 0) || (value > 10)) {
                         throw new IllegalArgumentException("value out of range: " + value); // NOI18N
                     }
                     final int oldValue = this.value[index];
                     this.value[index] = value;
                     for (final Object o : list.getListeners(EqualizerModelListener.class)) {
                         ((EqualizerModelListener)o).equalizerChanged(
                             new EqualizerModelEvent(this, index, oldValue, value));
                     }
                 }
 
                 @Override
                 public void addEqualizerModelListener(final EqualizerModelListener eml) {
                     list.add(EqualizerModelListener.class, eml);
                 }
 
                 @Override
                 public void removeEqualizerModelListener(final EqualizerModelListener eml) {
                     list.remove(EqualizerModelListener.class, eml);
                 }
             },
             DEFAULT_PAINT,
             DEFAULT_STROKE,
             true,
             null,
             true,
             false,
             true);
     }
 
     /**
      * Creates a new EqualizerPanel object from the provided <code>EqualizerModel</code> using:
      *
      * <ul>
      *   <li>splinePaint=<code>DEFAULT_PAINT</code></li>
      *   <li>splineStroke=<code>DEFAULT_STROKE</code></li>
      *   <li>splinePainted=<code>true</code></li>
      *   <li>rangeAxisName= <code>null</code></li>
      *   <li>rangeAxisPainted= <code>true</code></li>
      *   <li>updateModelWhileAdjusting= <code>false</code></li>
      *   <li>updateSplineWhileAdjusting= <code>true</code></li>
      * </ul>
      *
      * @param  model  the underlying <code>EqualizerModel</code>
      *
      * @see    EqualizerPanel(de.cismet.commons.gui.equalizer.EqualizerModel, java.awt.Paint, java.awt.Stroke, boolean,
      *         java.lang.String, boolean, boolean, boolean)
      */
     public EqualizerPanel(final EqualizerModel model) {
         this(model, DEFAULT_PAINT, DEFAULT_STROKE, true, null, true, false, true);
     }
 
     /**
      * Creates a new EqualizerPanel object.
      *
      * @param  model                       the underlying <code>EqualizerModel</code>
      * @param  splinePaint                 the paint for the spline
      * @param  splineStroke                the stroke for the spline
      * @param  splinePainted               if the spline is painted at all
      * @param  rangeAxisName               the name of the range axis
      * @param  rangeAxisPainted            if the range axis is painted at all
      * @param  updateModelWhileAdjusting   if updates are sent to the model while the user is adjusting the value
      * @param  updateSplineWhileAdjusting  if the spline is updated while the user is adjusting the value
      */
     public EqualizerPanel(final EqualizerModel model,
             final Paint splinePaint,
             final Stroke splineStroke,
             final boolean splinePainted,
             final String rangeAxisName,
             final boolean rangeAxisPainted,
             final boolean updateModelWhileAdjusting,
             final boolean updateSplineWhileAdjusting) {
         this.equalizerModelL = new EqualizerModelL();
         this.sliderChangeL = new SliderChangeL();
         this.updateInProgress = false;
 
         initComponents();
 
         // we don't use the setter since there is nothing to check and we don't want to createComponents twice
         this.updateModelWhileAdjusting = updateModelWhileAdjusting;
         this.updateSplineWhileAdjusting = updateSplineWhileAdjusting;
         this.splinePainted = splinePainted;
         this.rangeAxisPainted = rangeAxisPainted;
         this.lblRangeAxis = new JLabel(rangeAxisName);
         this.lblRangeAxis.setOpaque(false);
 
         setModel(model);
         setSplinePaint(splinePaint);
         setSplineStroke(splineStroke);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Getter for the current <code>EqualizerModel.</code>
      *
      * @return  the current <code>EqualizerModel</code>
      */
     public EqualizerModel getModel() {
         return model;
     }
 
     /**
      * Sets a new <code>EqualizerModel.</code>
      *
      * @param   model  the new model
      *
      * @throws  IllegalArgumentException  if the model is <code>null</code>
      */
     public final void setModel(final EqualizerModel model) {
         if (model == null) {
             throw new IllegalArgumentException("model must not be null"); // NOI18N
         }
 
         if (this.model != model) {
             this.model = model;
 
             this.model.addEqualizerModelListener(WeakListeners.create(
                     EqualizerModelListener.class,
                     equalizerModelL,
                     this.model));
 
             recreateComponents();
         }
     }
 
     /**
      * Indicates if the spline is painted or not.
      *
      * @return  if the spline is painted or not
      */
     public boolean isSplinePainted() {
         return splinePainted;
     }
 
     /**
      * Sets if the spline is painted or not.
      *
      * @param  splinePainted  if the spline is painted or not
      */
     public void setSplinePainted(final boolean splinePainted) {
         this.splinePainted = splinePainted;
 
         repaint();
     }
 
     /**
      * Getter for the current spline paint.
      *
      * @return  the current spline paint
      */
     public Paint getSplinePaint() {
         return splinePaint;
     }
 
     /**
      * Sets the new spline paint.
      *
      * @param   splinePaint  the new spline paint
      *
      * @throws  IllegalArgumentException  if the spline paint is <code>null</code>
      */
     public final void setSplinePaint(final Paint splinePaint) {
         if (splinePaint == null) {
             throw new IllegalArgumentException("splinePaint must not be null"); // NOI18N
         }
 
         this.splinePaint = splinePaint;
 
         repaint();
     }
 
     /**
      * Getter for the current spline stroke.
      *
      * @return  the current spline stroke
      */
     public Stroke getSplineStroke() {
         return splineStroke;
     }
 
     /**
      * Sets the new spline stroke.
      *
      * @param   splineStroke  the new spline stroke
      *
      * @throws  IllegalArgumentException  if the spline stroke is <code>null</code>
      */
     public final void setSplineStroke(final Stroke splineStroke) {
         if (splineStroke == null) {
             throw new IllegalArgumentException("splineStroke must not be null"); // NOI18N
         }
 
         this.splineStroke = splineStroke;
 
         repaint();
     }
 
     /**
      * Indicates if the range axis is painted or not.
      *
      * @return  if the range axis is painted or not.
      */
     public boolean isRangeAxisPainted() {
         return rangeAxisPainted;
     }
 
     /**
      * Sets if the range axis is painted or not.
      *
      * @param  rangeAxisPainted  paint the range axis of not
      */
     public final void setRangeAxisPainted(final boolean rangeAxisPainted) {
         if (this.rangeAxisPainted != rangeAxisPainted) {
             this.rangeAxisPainted = rangeAxisPainted;
 
             recreateComponents();
         }
     }
 
     /**
      * Getter for the current range axis name.
      *
      * @return  the current range axis name
      */
     public String getRangeAxisName() {
         return lblRangeAxis.getText();
     }
 
     /**
      * Sets the new range axis name.
      *
      * @param  rangeAxisName  the new range axis name
      */
     public final void setRangeAxisName(final String rangeAxisName) {
         this.lblRangeAxis.setText(rangeAxisName);
     }
 
     /**
      * Whether or not the model is updated while the user is adjusting a slider value.
      *
      * @return  whether or not the model is updated while the user is adjusting a slider value
      *
      * @see     JSlider#getValueIsAdjusting()
      */
     public boolean isUpdateModelWhileAdjusting() {
         return updateModelWhileAdjusting;
     }
 
     /**
      * Through this setter one can control if user interaction with a slider will constantly update the model while the
      * user is still choosing the new slider value.
      *
      * @param  updateModelWhileAdjusting  whether or not update the model while adjusting
      *
      * @see    JSlider#getValueIsAdjusting()
      */
     public void setUpdateModelWhileAdjusting(final boolean updateModelWhileAdjusting) {
         this.updateModelWhileAdjusting = updateModelWhileAdjusting;
     }
 
     /**
      * Whether or not the spline curve will reflect the user's adjusting activity.
      *
      * @return  whether or not the spline curve will reflect the user's adjusting activity
      */
     public boolean isUpdateSplineWhileAdjusting() {
         return updateSplineWhileAdjusting;
     }
 
     /**
      * Through this setter one can control if the user interaction with a slider will constantly update the spline curve
      * while the user is still choosing the new slider value.
      *
      * @param  updateSplineWhileAdjusting  whether or not update the spline curve while adjusting
      */
     public void setUpdateSplineWhileAdjusting(final boolean updateSplineWhileAdjusting) {
         this.updateSplineWhileAdjusting = updateSplineWhileAdjusting;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void recreateComponents() {
         createComponents();
         invalidate();
         validate();
         // strangely the spline is not correctly painted after these steps and calling repaint directly does not help
         // only if the repaint is invoked later than the commands above it renders correctly
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     repaint();
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JPanel createRangeComponent() {
         final JPanel panel = new JPanel(new GridBagLayout());
         panel.setOpaque(false);
         // TODO: support for rotating the label text
         final JPanel rangePanel = new RangePanel();
 
         final GridBagConstraints rangeConstraints = new GridBagConstraints(
                 0,
                 0,
                 1,
                 1,
                 0,
                 1,
                 GridBagConstraints.SOUTH,
                 GridBagConstraints.VERTICAL,
                 new Insets(5, 5, 5, 5),
                 0,
                 0);
         final GridBagConstraints labelConstraints = new GridBagConstraints(
                 0,
                 1,
                 1,
                 1,
                 0,
                 0,
                 GridBagConstraints.NORTH,
                 GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5),
                 0,
                 0);
         panel.add(rangePanel, rangeConstraints);
         panel.add(lblRangeAxis, labelConstraints);
 
         return panel;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void createComponents() {
         sliderMap = new TreeMap<Integer, JSlider>(new Comparator<Integer>() {
 
                     @Override
                     public int compare(final Integer o1, final Integer o2) {
                         return o1.compareTo(o2);
                     }
                 });
         pnlEqualizer.removeAll();
 
         final GridBagConstraints baseConstraints = new GridBagConstraints(
                 0,
                 0,
                 1,
                 1,
                 0,
                 1,
                 GridBagConstraints.SOUTH,
                 GridBagConstraints.VERTICAL,
                 new Insets(5, 5, 5, 5),
                 0,
                 0);
 
         if (rangeAxisPainted) {
             final JPanel scaleComp = createRangeComponent();
             final GridBagConstraints constraints = (GridBagConstraints)baseConstraints.clone();
             pnlEqualizer.add(scaleComp, constraints);
         }
 
         for (int i = 0; i < model.getEqualizerCategoryCount(); ++i) {
             final JPanel sliderComp = createSliderComponent(i);
             final GridBagConstraints constraints = (GridBagConstraints)baseConstraints.clone();
             // we start to add it at index one in case of scale is available
             constraints.gridx = i + 1;
             pnlEqualizer.add(sliderComp, constraints);
         }
 
         pnlEqualizer.repaint();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   index  category DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JPanel createSliderComponent(final int index) {
         final JPanel panel = new JPanel(new GridBagLayout());
         panel.setOpaque(false);
         final JLabel label = new JLabel(model.getEqualizerCategory(index));
         label.setOpaque(false);
         // TODO: support for rotating the label text
         final JSlider slider = new JSlider(JSlider.VERTICAL);
         slider.setOpaque(false);
         slider.setMinimum(model.getRange().getMin());
         slider.setMaximum(model.getRange().getMax());
         slider.setValue(model.getValueAt(index));
         slider.putClientProperty(PROP_MODEL_INDEX, index);
         slider.addChangeListener(WeakListeners.change(sliderChangeL, slider));
         sliderMap.put(index, slider);
 
         final GridBagConstraints sliderConstraints = new GridBagConstraints(
                 0,
                 0,
                 1,
                 1,
                 0,
                 1,
                 GridBagConstraints.SOUTH,
                 GridBagConstraints.VERTICAL,
                 new Insets(5, 5, 5, 5),
                 0,
                 0);
         final GridBagConstraints labelConstraints = new GridBagConstraints(
                 0,
                 1,
                 1,
                 1,
                 0,
                 0,
                 GridBagConstraints.NORTH,
                 GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5),
                 0,
                 0);
         panel.add(slider, sliderConstraints);
         panel.add(label, labelConstraints);
 
         return panel;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         final java.awt.GridBagConstraints gridBagConstraints;
 
         pnlEqualizer = new SplinePanel();
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlEqualizer.setOpaque(false);
         pnlEqualizer.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(pnlEqualizer, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class SplinePanel extends JPanel {
 
         //~ Instance fields ----------------------------------------------------
 
         // the fallback is used if the retrieval of the slider knob position has failed only once
         private boolean useSliderKnobPositionFallback;
         private Double[] currentPoints;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SplinePanel object.
          */
         public SplinePanel() {
             this.setOpaque(false);
             this.useSliderKnobPositionFallback = false;
             this.currentPoints = null;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void paintComponent(final Graphics g) {
             super.paintComponent(g);
 
            if (!splinePainted || (model.getEqualizerCategoryCount() < 2)) {
                 // nothing to do, bail out
                 return;
             }
 
             final Collection<JSlider> sliders = sliderMap.values();
             Double[] p = new Double[sliders.size()];
             int i = 0;
             boolean isAdjusting = false;
             for (final JSlider s : sliderMap.values()) {
                 if (!updateSplineWhileAdjusting && s.getValueIsAdjusting()) {
                     isAdjusting = true;
                     break;
                 }
 
                 final Point handle = new Point();
 
                 // hack to get position of the knob
                 final SliderUI sui = s.getUI();
                 if (!useSliderKnobPositionFallback && (sui instanceof BasicSliderUI)) {
                     final BasicSliderUI bui = (BasicSliderUI)sui;
                     Field field = null;
                     Boolean accessible = null;
                     try {
                         field = BasicSliderUI.class.getDeclaredField("thumbRect"); // NOI18N
                         accessible = field.isAccessible();
                         field.setAccessible(true);
                         final Rectangle r = (Rectangle)field.get(bui);
                         final Point loc = r.getLocation();
                         handle.x = loc.x + (r.width / 2);
                         handle.y = loc.y + (r.height / 2);
                     } catch (final Exception e) {
                         useSliderKnobPositionFallback = true;
                     } finally {
                         if ((field != null) && (accessible != null)) {
                             field.setAccessible(accessible);
                         }
                     }
                 } else {
                     useSliderKnobPositionFallback = true;
                 }
 
                 if (useSliderKnobPositionFallback) {
                     // calculate position using slider value which is most likely inaccurate for y location
                     final int min = s.getMinimum();
                     final int max = s.getMaximum();
                     final int val = s.getValue();
 
                     final Dimension sliderSize = s.getSize();
                     handle.x = sliderSize.width / 2;
 
                     // slider percentage from upper bound (reversed)
                     final double sliderPercentage = 1 - (val / (double)(max - min));
                     // assume offset because of shape of knob and internal insets
                     final double offset = 13 * (1 - (2 * sliderPercentage));
                     handle.y = (int)((sliderSize.height * sliderPercentage) + offset);
                 }
 
                 final Point rel = SwingUtilities.convertPoint(s, handle, this);
                 p[i] = new Double(rel.x, rel.y);
 
                 i++;
             }
 
             if (isAdjusting) {
                 p = currentPoints;
             }
             final Double[] p1 = new Double[p.length - 1];
             final Double[] p2 = new Double[p.length - 1];
             calculateControlPoints(p, p1, p2);
 
             final Graphics2D g2 = (Graphics2D)g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
             final GeneralPath path = new GeneralPath();
             path.moveTo(p[0].x, p[0].y);
 
             for (i = 0; i < p1.length; ++i) {
                 path.curveTo(p1[i].x, p1[i].y, p2[i].x, p2[i].y, p[i + 1].x, p[i + 1].y);
             }
 
             g2.setPaint(splinePaint);
             g2.setStroke(splineStroke);
             g2.draw(path);
 
             g2.dispose();
 
             currentPoints = p;
         }
 
         /**
          * Algorithm from
          * {@link http://www.codeproject.com/Articles/31859/Draw-a-Smooth-Curve-through-a-Set-of-2D-Points-wit}.
          *
          * @param   p   DOCUMENT ME!
          * @param   p1  DOCUMENT ME!
          * @param   p2  DOCUMENT ME!
          *
          * @throws  IllegalArgumentException  DOCUMENT ME!
          * @throws  IllegalStateException     DOCUMENT ME!
          */
         private void calculateControlPoints(final Double[] p, final Double[] p1, final Double[] p2) {
             if (p == null) {
                 throw new IllegalArgumentException("points must not be null"); // NOI18N
             } else if (p1 == null) {
                 throw new IllegalArgumentException("p1 must not be null");     // NOI18N
             } else if (p2 == null) {
                 throw new IllegalArgumentException("p2 must not be null");     // NOI18N
             }
 
             final int n = p.length - 1;
 
             if (n == 0) {
                 throw new IllegalStateException(
                     "at least two points must be available to calculate control points for");             // NOI18N
             } else if (p1.length != n) {
                 throw new IllegalStateException("p1 must have length of " + n + ", but is " + p1.length); // NOI18N
             } else if (p2.length != n) {
                 throw new IllegalStateException("p2 must have length of " + n + ", but is " + p1.length); // NOI18N
             }
 
             // straight line
             if (n == 1) {
                 p1[0].x = ((2 * p[0].x) + p[1].x) / 3;
                 p1[0].y = ((2 * p[0].y) + p[1].y) / 3;
 
                 p2[0].x = (2 * p1[0].x) - p[0].x;
                 p2[0].y = (2 * p1[0].y) - p[0].y;
             } else {
                 final double[] rhs = new double[n];
 
                 // x vals
                 rhs[0] = p[0].x + (2 * p[1].x);
                 for (int i = 1; i < (n - 1); ++i) {
                     rhs[i] = (4 * p[i].x) + (2 * p[i + 1].x);
                 }
                 rhs[n - 1] = ((8 * p[n - 1].x) + p[n].x) / 2.0;
                 final double[] x1 = getFirstControlPoints(rhs);
 
                 // y vals
                 rhs[0] = p[0].y + (2 * p[1].y);
                 for (int i = 1; i < (n - 1); ++i) {
                     rhs[i] = (4 * p[i].y) + (2 * p[i + 1].y);
                 }
                 rhs[n - 1] = ((8 * p[n - 1].y) + p[n].y) / 2.0;
                 final double[] y1 = getFirstControlPoints(rhs);
 
                 for (int i = 0; i < n; ++i) {
                     p1[i] = new Double(x1[i], y1[i]);
                     final double x2;
                     final double y2;
                     if (i < (n - 1)) {
                         x2 = (2 * p[i + 1].x) - x1[i + 1];
                         y2 = (2 * p[i + 1].y) - y1[i + 1];
                     } else {
                         x2 = (p[n].x + x1[n - 1]) / 2.0;
                         y2 = (p[n].y + y1[n - 1]) / 2.0;
                     }
                     p2[i] = new Double(x2, y2);
                 }
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   rhs  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private double[] getFirstControlPoints(final double[] rhs) {
             final int n = rhs.length;
             final double[] x = new double[n];
             final double[] tmp = new double[n];
 
             double b = 2.0;
             x[0] = rhs[0] / b;
             for (int i = 1; i < n; ++i) {
                 tmp[i] = 1 / b;
                 b = ((i < (n - 1)) ? 4.0 : 3.5) - tmp[i];
                 x[i] = (rhs[i] - x[i - 1]) / b;
             }
             for (int i = 1; i < n; ++i) {
                 x[n - i - 1] -= tmp[n - i] * x[n - i];
             }
 
             return x;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class RangePanel extends JPanel {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final int INSETS_WIDTH = 5;
         private static final int STROKE_WIDTH = 3;
         private static final int MAJOR_TICK_WIDTH = 10;
         private static final int MINOR_TICK_WIDTH = 7;
         private static final int TEXT_GAP_WIDTH = 8;
 
         //~ Instance fields ----------------------------------------------------
 
         private boolean useSliderTrackRectangleFallback;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ScalePanel object.
          */
         public RangePanel() {
             this.setOpaque(false);
 
             this.useSliderTrackRectangleFallback = false;
 
             final Font font = this.getFont().deriveFont(10f);
             final FontRenderContext frc = new FontRenderContext(font.getTransform(), true, true);
             final Rectangle2D maxBounds = font.getStringBounds(String.valueOf(model.getRange().getMax()), frc);
             final Rectangle2D minBounds = font.getStringBounds(String.valueOf(model.getRange().getMin()), frc);
 
             final int minX1 = INSETS_WIDTH + (int)maxBounds.getWidth() + TEXT_GAP_WIDTH + MAJOR_TICK_WIDTH
                         + INSETS_WIDTH;
             final int minX2 = INSETS_WIDTH + (int)minBounds.getWidth() + TEXT_GAP_WIDTH + MAJOR_TICK_WIDTH
                         + INSETS_WIDTH;
             final int minX = Math.max(minX1, minX2);
 
             final int minY = INSETS_WIDTH + (int)maxBounds.getHeight() + TEXT_GAP_WIDTH + (int)minBounds.getHeight()
                         + INSETS_WIDTH;
 
             this.setMinimumSize(new Dimension(minX, minY));
             this.setPreferredSize(new Dimension(minX, minY));
             this.setFont(font);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void paintComponent(final Graphics g) {
             super.paintComponent(g);
 
             final JSlider slider = sliderMap.values().iterator().next();
             final SliderUI sui = slider.getUI();
             final Dimension size = getSize();
             int upperY = 0;
             int lowerY = 0;
             if (!useSliderTrackRectangleFallback && (sui instanceof BasicSliderUI)) {
                 final BasicSliderUI bui = (BasicSliderUI)sui;
                 Field field = null;
                 Boolean accessible = null;
                 try {
                     field = BasicSliderUI.class.getDeclaredField("trackRect"); // NOI18N
                     accessible = field.isAccessible();
                     field.setAccessible(true);
                     final Rectangle r = (Rectangle)field.get(bui);
                     upperY = r.getLocation().y;
                     lowerY = upperY + (int)r.getHeight();
                 } catch (final Exception e) {
                     useSliderTrackRectangleFallback = true;
                 } finally {
                     if ((field != null) && (accessible != null)) {
                         field.setAccessible(accessible);
                     }
                 }
             } else {
                 useSliderTrackRectangleFallback = true;
             }
 
             if (useSliderTrackRectangleFallback) {
                 // assume offset because of internal insets
                 upperY = 13;
                 lowerY = size.height - 13;
             }
 
             final int midY = ((lowerY - upperY) / 2) + upperY;
             final int rangeLineX = size.width - INSETS_WIDTH - STROKE_WIDTH;
             final int majorTickX = rangeLineX - MAJOR_TICK_WIDTH;
             final int minorTickX = rangeLineX - MINOR_TICK_WIDTH;
             // baseline of text on the lower boundary of the tick line
             final int upperRangeY = upperY + STROKE_WIDTH;
             final int lowerRangeY = lowerY + STROKE_WIDTH;
             final int midRangeY = midY + STROKE_WIDTH;
 
             final int rangeMin = model.getRange().getMin();
             final int rangeMax = model.getRange().getMax();
             final int rangeMid = (rangeMax + rangeMin) / 2;
 
             // TODO: nicer rendering
             final Graphics2D g2 = (Graphics2D)g.create();
             g2.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
             g2.drawLine(rangeLineX, upperY, rangeLineX, lowerY);
             g2.drawLine(majorTickX, upperY, rangeLineX, upperY);
             g2.drawLine(majorTickX, lowerY, rangeLineX, lowerY);
             g2.drawLine(minorTickX, midY, rangeLineX, midY);
 
             g2.drawString(String.valueOf(rangeMax), INSETS_WIDTH, upperRangeY);
             g2.drawString(String.valueOf(rangeMin), INSETS_WIDTH, lowerRangeY);
             g2.drawString(String.valueOf(rangeMid), INSETS_WIDTH, midRangeY);
 
             g2.dispose();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  1.0
      */
     private final class SliderChangeL implements ChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void stateChanged(final ChangeEvent e) {
             if (!updateInProgress) {
                 pnlEqualizer.repaint();
                 final JSlider slider = (JSlider)e.getSource();
                 if (updateModelWhileAdjusting || !slider.getValueIsAdjusting()) {
                     final int index = (Integer)slider.getClientProperty(PROP_MODEL_INDEX);
                     model.setValueAt(index, slider.getValue());
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  1.0
      */
     private final class EqualizerModelL implements EqualizerModelListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void equalizerChanged(final EqualizerModelEvent event) {
             if ((event.getSource() == model)) {
                 updateInProgress = true;
                 try {
                     if (event.getIndex() < 0) {
                         // full update
                         for (int i = 0; i < model.getEqualizerCategoryCount(); ++i) {
                             sliderMap.get(i).setValue(model.getValueAt(i));
                         }
                     } else {
                         final JSlider slider = sliderMap.get(event.getIndex());
                         final int newValue = event.getNewValue();
 
                         if (slider.getValue() != newValue) {
                             slider.setValue(newValue);
                         }
                     }
                    pnlEqualizer.repaint();
                 } finally {
                     updateInProgress = false;
                 }
             }
         }
     }
 }
