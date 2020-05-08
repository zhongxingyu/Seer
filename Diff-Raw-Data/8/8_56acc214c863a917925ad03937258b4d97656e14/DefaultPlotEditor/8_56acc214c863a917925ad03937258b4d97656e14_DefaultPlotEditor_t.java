 /* ===========================================================
  * JFreeChart : a free chart library for the Java(tm) platform
  * ===========================================================
  *
  * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
  *
  * Project Info:  http://www.jfree.org/jfreechart/index.html
  *
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
  * USA.
  *
  * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
  * in the United States and other countries.]
  *
  * ----------------------
  * DefaultPlotEditor.java
  * ----------------------
  * (C) Copyright 2005-2008, by Object Refinery Limited and Contributors.
  *
  * Original Author:  David Gilbert (for Object Refinery Limited);
  * Contributor(s):   Andrzej Porebski;
  *                   Arnaud Lelievre;
  *                   Daniel Gredler;
  *
  * Changes:
  * --------
  * 24-Nov-2005 : Version 1, based on PlotPropertyEditPanel.java (DG);
  * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
  *               Jess Thrysoee (DG);
  *
  */
 
 package org.jfree.chart.editor;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import java.awt.event.FocusEvent;
 import java.util.ResourceBundle;
 import java.text.MessageFormat;
 import java.text.DecimalFormat;
 
 import javax.swing.*;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 
 import org.jfree.chart.axis.Axis;
 import org.jfree.chart.plot.*;
 import org.jfree.chart.util.ResourceBundleWrapper;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
 import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
 import org.jfree.chart.labels.StandardXYItemLabelGenerator;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.ui.RectangleInsets;
 import org.jfree.util.Rotation;
 import org.jfree.chart.editor.components.*;
 import org.jfree.chart.editor.themes.PlotTheme;
 import org.jfree.chart.editor.themes.ChartBorder;
 import org.jfree.chart.editor.themes.AxisTheme;
 
 /**
  * A panel for editing the properties of a {@link Plot}.
  */
 public class DefaultPlotEditor extends BaseEditor implements ActionListener {
 
     private final static int PIE = -1, CATEGORY = -2, XY = -3;
 
     /** Orientation constants. */
     private final static String[] orientationNames = {"Vertical", "Horizontal"};
     private final static int ORIENTATION_VERTICAL = 0;
     private final static int ORIENTATION_HORIZONTAL = 1;
 
     protected PlotTheme theme;
 
     /** The panel to adjust the properties of the plot's background */
     private BackgroundEditingPanel backgroundPanel;
 
     /**
      * A panel used to display/edit the properties of the domain axis (if any).
      */
     private DefaultAxisEditor domainAxisPropertyPanel;
 
     /**
      * A panel used to display/edit the properties of the range axis (if any).
      */
     private DefaultAxisEditor rangeAxisPropertyPanel;
 
     /**
      * The orientation for the plot (for <tt>CategoryPlot</tt>s and
      * <tt>XYPlot</tt>s).
      */
     private PlotOrientation plotOrientation;
 
     /**
      * The orientation combo box (for <tt>CategoryPlot</tt>s and
      * <tt>XYPlot</tt>s).
      */
     private JComboBox orientationCombo;
 
     private JCheckBox shadowsVisible;
 
     private InsetPanel axisOffset;
 
     private LineEditorPanel domainPanel, rangePanel;
 
     private BorderPanel plotBorder;
 
     private InsetPanel insetPanel;
 
     private JSpinner startAngle;
     private JCheckBox circularPlot;
     private RotationComboBox pieDirection;
 
     // label controls
     private JPanel labelPanel = null;
     private JCheckBox labelsVisible;
     private FontControl labelFont;
     private PaintControl labelPaint, labelBackgroundPaint, labelOutlinePaint, labelShadowPaint;
     private JCheckBox labelLinkVisible;
     private LinkStyleComboBox labelLinkStyle;
     private StrokeControl labelOutlineStroke;
     private InsetPanel labelPadding;
     private JTextField labelFormat;
 
     private NumberFormatDisplay numberFormatDisplay, percentFormatDisplay;
 
     /** The resourceBundle for the localization. */
     protected static ResourceBundle localizationResources
             = ResourceBundleWrapper.getBundle(
                     "org.jfree.chart.editor.LocalizationBundle");
 
     /**
      * Standard constructor - constructs a panel for editing the properties of
      * the specified plot.
      * <P>
      * In designing the panel, we need to be aware that subclasses of Plot will
      * need to implement subclasses of PlotPropertyEditPanel - so we need to
      * leave one or two 'slots' where the subclasses can extend the user
      * interface.
      *
      * @param theme The theme that will be edited.
      * @param chart the chart, which should be changed.
      * @param plot  the plot, which should be changed.
      * @param immediateUpdate Whether changes to GUI controls should immediately be applied.
      */
     public DefaultPlotEditor(PlotTheme theme, JFreeChart chart, Plot plot, boolean immediateUpdate) {
         super(chart, immediateUpdate);
 
         this.theme = theme;
 
         this.backgroundPanel = new BackgroundEditingPanel(theme);
         this.plotOrientation = theme.getOrientation();
 
         domainPanel = new LineEditorPanel(localizationResources.getString("Domain_Axis"),
                 theme.isDomainGridlineVisible(), theme.getDomainGridlinePaint(),
                 theme.getDomainGridlineStroke());
 
         rangePanel = new LineEditorPanel(localizationResources.getString("Range_Axis"),
                 theme.isRangeGridlinesVisible(), theme.getRangeGridlinePaint(),
                 theme.getRangeGridlineStroke());
 
 
         setLayout(new BorderLayout());
 
         // create a panel for the settings...
         JPanel panel = new JPanel(new BorderLayout());
 
         JPanel general = new JPanel(new BorderLayout());
 
         JPanel interior = new JPanel(new GridBagLayout());
         GridBagConstraints c = getNewConstraints();
         interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
 
         if (plot instanceof CategoryPlot || plot instanceof XYPlot) {
             startNewRow(c);
             boolean isVertical
                 = this.plotOrientation.equals(PlotOrientation.VERTICAL);
             int index
                 = isVertical ? ORIENTATION_VERTICAL : ORIENTATION_HORIZONTAL;
             interior.add(
                 new JLabel(localizationResources.getString("Orientation")), c
             );
             c.gridx++; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
             c.fill = GridBagConstraints.NONE;
             this.orientationCombo = new JComboBox(orientationNames);
             this.orientationCombo.setSelectedIndex(index);
             this.orientationCombo.setActionCommand("Orientation");
             this.orientationCombo.addActionListener(updateHandler);
             this.orientationCombo.addActionListener(this);
             interior.add(this.orientationCombo,c);
 
             startNewRow(c);
             RectangleInsets offsets = theme.getAxisOffset();
             this.axisOffset = new InsetPanel(localizationResources.getString("Axis-Offset"), offsets);
             this.axisOffset.addChangeListener(updateHandler);
             c.gridwidth = 3; c.weightx = 1;
             interior.add(this.axisOffset, c);
         }
 
         if(hasPossibleShadows(plot)) {
             insertShadowsCheckBox(interior, c, theme.isShadowsVisible());
         }
 
         startNewRow(c);
         c.gridwidth = 3; c.weightx = 1;
         insetPanel = new InsetPanel(localizationResources.getString("Insets"), theme.getInsets());
         insetPanel.addChangeListener(updateHandler);
         interior.add(insetPanel, c);
 
         startNewRow(c);
         c.gridwidth = 3; c.weightx = 1;
         ChartBorder border = theme.getBorder();
         plotBorder = new BorderPanel(localizationResources.getString("Border"),
                 border.isVisible(), border.getStroke(), border.getPaint());
         plotBorder.addChangeListener(updateHandler);
         interior.add(plotBorder, c);
 
         startNewRow(c);
         backgroundPanel.setBorder(BorderFactory.createTitledBorder(
                 localizationResources.getString("Background")
         ));
         backgroundPanel.addChangeListener(updateHandler);
         c.gridwidth = 3; c.weightx = 1;
         interior.add(backgroundPanel,c);
 
         if(plot instanceof PiePlot) {
             JPanel piePanel = getPiePanel();
             startNewRow(c);
             c.gridwidth = 3; c.weightx = 1;
             interior.add(piePanel, c);
 
             labelPanel = getLabelPanel(PIE);
             enableDisableLabelControls();
         }
 
         general.add(interior, BorderLayout.NORTH);
 
         JPanel appearance = new JPanel(new BorderLayout());
         appearance.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
         appearance.add(general, BorderLayout.NORTH);
 
         JTabbedPane tabs = new JTabbedPane();
         tabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
 
         tabs.add(localizationResources.getString("Appearance"), new JScrollPane(appearance));
 
         Axis domainAxis = null;
         if (plot instanceof CategoryPlot) {
             domainAxis = ((CategoryPlot) plot).getDomainAxis();
         }
         else if (plot instanceof XYPlot) {
             domainAxis = ((XYPlot) plot).getDomainAxis();
         }
         AxisTheme dAxisTheme = theme.getDomainAxisTheme();
         AxisTheme rAxisTheme = theme.getRangeAxisTheme();
         this.domainAxisPropertyPanel
             = DefaultAxisEditor.getInstance(dAxisTheme, chart, domainAxis, immediateUpdate);
         if (this.domainAxisPropertyPanel != null) {
             this.domainAxisPropertyPanel.setBorder(
                 BorderFactory.createEmptyBorder(2, 2, 2, 2)
             );
             tabs.add(
                 localizationResources.getString("Domain_Axis"),
                 new JScrollPane(this.domainAxisPropertyPanel)
             );
         }
 
         Axis rangeAxis = null;
         if (plot instanceof CategoryPlot) {
             rangeAxis = ((CategoryPlot) plot).getRangeAxis();
             labelPanel = getLabelPanel(CATEGORY);
         }
         else if (plot instanceof XYPlot) {
             rangeAxis = ((XYPlot) plot).getRangeAxis();
             labelPanel = getLabelPanel(XY);
         }
 
         this.rangeAxisPropertyPanel
             = DefaultAxisEditor.getInstance(rAxisTheme, chart, rangeAxis, immediateUpdate);
         if (this.rangeAxisPropertyPanel != null) {
             this.rangeAxisPropertyPanel.setBorder(
                 BorderFactory.createEmptyBorder(2, 2, 2, 2)
             );
             tabs.add(
                 localizationResources.getString("Range_Axis"),
                 new JScrollPane(this.rangeAxisPropertyPanel)
             );
         }
 
         if (plot instanceof CategoryPlot || plot instanceof XYPlot) {
             tabs.add(localizationResources.getString("Grid_Lines"), new JScrollPane(getGridlinesPanel()));
         }
 
         addCustomTabs(tabs);
 
         if(labelPanel != null) {
             tabs.add(localizationResources.getString("Labels"), new JScrollPane(labelPanel));
         }
 
         panel.add(tabs);
 
         add(panel);
     }
 
     protected JPanel getLabelPanel(int plotType) {
         JPanel retVal = new JPanel(new GridBagLayout());
         GridBagConstraints c = getNewConstraints();
 
         labelsVisible = new JCheckBox(localizationResources.getString("Visible"), theme.isLabelsVisible());
         labelsVisible.addActionListener(updateHandler);
         labelsVisible.addActionListener(this);
 
         labelFormat = new JTextField(theme.getLabelFormat());
         DocHandler handler = new DocHandler();
         labelFormat.getDocument().addDocumentListener(handler);
         labelFormat.addFocusListener(handler);
         String tip;
         switch(plotType) {
             case CATEGORY:
                 tip = localizationResources.getString("Format_tip_cat");
                 break;
             case XY:
                 tip = localizationResources.getString("Format_tip_xy");
                 break;
             case PIE:
             default:
                 tip = localizationResources.getString("Format_tip_pie");
                 break;
         }
         labelFormat.setToolTipText(tip);
 
         numberFormatDisplay = buildNumberFormatDisplay(theme.getNumberFormatString());
         numberFormatDisplay.addChangeListener(updateHandler);
 
         percentFormatDisplay = buildNumberFormatDisplay(theme.getPercentFormatString());
         percentFormatDisplay.addChangeListener(updateHandler);
 
         labelFont = new FontControl(theme.getLabelFont());
         labelFont.addChangeListener(updateHandler);
 
         labelPaint = new PaintControl(theme.getLabelPaint(), false);
         labelPaint.addChangeListener(updateHandler);
 
         labelBackgroundPaint = new PaintControl(theme.getLabelBackgroundPaint(), true);
         labelBackgroundPaint.addChangeListener(updateHandler);
 
         labelShadowPaint = new PaintControl(theme.getLabelShadowPaint(), true);
         labelShadowPaint.addChangeListener(updateHandler);
 
         labelOutlinePaint = new PaintControl(theme.getLabelOutlinePaint(), true);
         labelOutlinePaint.addChangeListener(updateHandler);
 
         labelOutlineStroke = new StrokeControl(theme.getLabelOutlineStroke());
         labelOutlineStroke.addChangeListener(updateHandler);
 
         labelPadding = new InsetPanel(localizationResources.getString("Padding"), theme.getLabelPadding());
         labelPadding.addChangeListener(updateHandler);
 
         labelLinkVisible = new JCheckBox(localizationResources.getString("Links_Visible"), theme.isLabelLinksVisible());
         labelLinkVisible.addActionListener(updateHandler);
         labelLinkVisible.addActionListener(this);
 
         labelLinkStyle = new LinkStyleComboBox();
         labelLinkStyle.setSelectedObject(theme.getLabelLinkStyle());
         labelLinkStyle.addActionListener(updateHandler);
 
         c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
         retVal.add(labelsVisible, c);
 
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Format")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(labelFormat, c);
 
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Number_Format")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(numberFormatDisplay, c);
 
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Percent_Format")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(percentFormatDisplay, c);
 
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Font")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(labelFont, c);
 
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Paint")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(labelPaint, c);
 
         if (plotType == PIE) {
             startNewRow(c);
             retVal.add(new JLabel(localizationResources.getString("Background_paint")), c);
             c.gridx++; c.weightx = 1;
             retVal.add(labelBackgroundPaint, c);
 
             startNewRow(c);
             retVal.add(new JLabel(localizationResources.getString("Shadow_paint")), c);
             c.gridx++; c.weightx = 1;
             retVal.add(labelShadowPaint, c);
 
             startNewRow(c);
             retVal.add(new JLabel(localizationResources.getString("Outline_Paint")), c);
             c.gridx++; c.weightx = 1;
             retVal.add(labelOutlinePaint, c);
 
             startNewRow(c);
             retVal.add(new JLabel(localizationResources.getString("Outline_stroke")), c);
             c.gridx++; c.weightx = 1;
             retVal.add(labelOutlineStroke, c);
 
             startNewRow(c);
             c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
             retVal.add(labelLinkVisible, c);
 
             startNewRow(c);
             retVal.add(new JLabel(localizationResources.getString("Link_Style")), c);
             c.gridx++; c.weightx = 1;
             retVal.add(labelLinkStyle, c);
 
             startNewRow(c);
             c.gridwidth = 2; c.weightx = 1;
             retVal.add(labelPadding, c);
         }
 
         startNewRow(c);
         c.fill = GridBagConstraints.VERTICAL; c.weighty = 1;
         retVal.add(new JPanel(), c);
 
 
         return retVal;
     }
 
     protected NumberFormatDisplay buildNumberFormatDisplay(String formatStr) {
         return new NumberFormatDisplay(formatStr);
     }
 
     protected JPanel getGridlinesPanel() {
         JPanel retVal = new JPanel(new GridBagLayout());
 
         domainPanel.addChangeListener(updateHandler);
         rangePanel.addChangeListener(updateHandler);
 
         GridBagConstraints c = getNewConstraints(); c.weightx = 1;
         retVal.add(domainPanel, c);
         startNewRow(c); c.weightx = 1;
         retVal.add(rangePanel, c);
         startNewRow(c);
         c.fill = GridBagConstraints.BOTH; c.weighty = 1;
         retVal.add(new JPanel(), c);
 
         return retVal;
     }
 
     private JPanel getPiePanel() {
         JPanel retVal = new JPanel(new GridBagLayout());
         retVal.setBorder(BorderFactory.createTitledBorder(localizationResources.getString("Pie")));
         GridBagConstraints c = getNewConstraints();
 
         circularPlot = new JCheckBox(localizationResources.getString("Circular"), theme.isCircularPie());
         circularPlot.addActionListener(updateHandler);
 
         pieDirection = new RotationComboBox();
         pieDirection.setSelectedObject(theme.getPieDirection());
         pieDirection.addActionListener(updateHandler);
 
         startAngle = new JSpinner(new SpinnerNumberModel(theme.getStartAngle(), 0, 360, 1));
         startAngle.addChangeListener(updateHandler);
 
         c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
         retVal.add(circularPlot, c);
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Pie_Direction")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(pieDirection, c);
         startNewRow(c);
         retVal.add(new JLabel(localizationResources.getString("Start_Position_Degs")), c);
         c.gridx++; c.weightx = 1;
         retVal.add(startAngle, c);
 
         return retVal;
     }
 
     private boolean hasPossibleShadows(Plot plot) {
         if(plot instanceof PiePlot) {
             return true;
         } else if (plot instanceof CategoryPlot) {
             CategoryPlot cPlot = (CategoryPlot) plot;
             for(int i=0; i < cPlot.getRendererCount(); i++) {
                 if(cPlot.getRenderer(i) instanceof BarRenderer) {
                     return true;
                 }
             }
         } else if (plot instanceof XYPlot) {
             XYPlot xyPlot = (XYPlot) plot;
             for(int i=0; i < xyPlot.getRendererCount(); i++) {
                 if(xyPlot.getRenderer(i) instanceof XYBarRenderer) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private void insertShadowsCheckBox(JPanel interior, GridBagConstraints c, boolean shadowVisible) {
         startNewRow(c);
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
 
         shadowsVisible = new JCheckBox(localizationResources.getString("Shadows_Visible"));
         shadowsVisible.setSelected(shadowVisible);
         shadowsVisible.addActionListener(updateHandler);
         interior.add(shadowsVisible, c);
     }
 
     /**
      * Returns the current outline stroke.
      *
      * @return The current outline stroke.
      */
     public Stroke getOutlineStroke() {
         return this.plotBorder.getBorderStroke();
     }
 
     /**
      * Returns the current outline paint.
      *
      * @return The current outline paint.
      */
     public Paint getOutlinePaint() {
         return this.plotBorder.getBorderPaint();
     }
 
     /**
      * Returns a reference to the panel for editing the properties of the
      * domain axis.
      *
      * @return A reference to a panel.
      */
     public DefaultAxisEditor getDomainAxisPropertyEditPanel() {
         return this.domainAxisPropertyPanel;
     }
 
     /**
      * Returns a reference to the panel for editing the properties of the
      * range axis.
      *
      * @return A reference to a panel.
      */
     public DefaultAxisEditor getRangeAxisPropertyEditPanel() {
         return this.rangeAxisPropertyPanel;
     }
 
     public RectangleInsets getAxisOffset() {
         return axisOffset.getSelectedInsets();
     }
 
     /**
      * Handles user actions generated within the panel.
      * @param event     the event
      */
     public void actionPerformed(ActionEvent event) {
         String command = event.getActionCommand();
         if (command.equals("Orientation")) {
             attemptOrientationSelection();
         } else if (event.getSource() == labelsVisible) {
             enableDisableLabelControls();
         }
     }
 
     private void enableDisableLabelControls() {
         boolean b = labelsVisible.isSelected();
         boolean l = labelLinkVisible.isSelected();
         labelFont.setEnabled(b);
         labelPaint.setEnabled(b);
         labelBackgroundPaint.setEnabled(b);
         labelShadowPaint.setEnabled(b);
         labelOutlinePaint.setEnabled(b);
         labelOutlineStroke.setEnabled(b);
         labelPadding.setEnabled(b);
         labelFormat.setEnabled(b);
         numberFormatDisplay.setEnabled(b);
         percentFormatDisplay.setEnabled(b);
         labelLinkVisible.setEnabled(b);
         labelLinkStyle.setEnabled(b&&l);
     }
 
     /**
      * Allow the user to modify the plot orientation if this is an editor for a
      * <tt>CategoryPlot</tt> or a <tt>XYPlot</tt>.
      */
     private void attemptOrientationSelection() {
 
         int index = this.orientationCombo.getSelectedIndex();
 
         if (index == ORIENTATION_VERTICAL) {
             this.plotOrientation = PlotOrientation.VERTICAL;
         }
         else {
             this.plotOrientation = PlotOrientation.HORIZONTAL;
         }
     }
 
     /**
      * Updates the plot properties to match the properties defined on the panel.
      *
      * @param chart  The chart.
      */
     public void updateChart(JFreeChart chart) {
         Plot plot = chart.getPlot();
         // set the plot properties...
         boolean pBorderVisible = plotBorder.isBorderVisible();
 
         theme.setBorder(pBorderVisible, getOutlinePaint(), (BasicStroke) getOutlineStroke());
         plot.setOutlineVisible(pBorderVisible);
         plot.setOutlinePaint(getOutlinePaint());
         plot.setOutlineStroke(getOutlineStroke());
 
         RectangleInsets plotInsets = insetPanel.getSelectedInsets();
         theme.setInsets(plotInsets);
         plot.setInsets(plotInsets);
 
         plot.setBackgroundPaint(backgroundPanel.getBackgroundPaint());
         theme.setPlotBackgroundPaint(backgroundPanel.getBackgroundPaint());
 
         if(plot instanceof CategoryPlot) {
             CategoryPlot cPlot = (CategoryPlot) plot;
             RectangleInsets offset = axisOffset.getSelectedInsets();
             cPlot.setAxisOffset(offset);
             theme.setAxisOffset(offset);
 
             boolean domVis = domainPanel.isLineVisible();
             cPlot.setDomainGridlinesVisible(domVis);
             theme.setDomainGridlineVisible(domVis);
 
             boolean ranVis = rangePanel.isLineVisible();
             cPlot.setRangeGridlinesVisible(ranVis);
             theme.setRangeGridlinesVisible(ranVis);
 
             Paint domPaint = domainPanel.getLinePaint();
             BasicStroke domStroke = domainPanel.getLineStroke();
             cPlot.setDomainGridlinePaint(domPaint);
             theme.setDomainGridlinePaint(domPaint);
             cPlot.setDomainGridlineStroke(domStroke);
             theme.setDomainGridlineStroke(domStroke);
 
             Paint ranPaint = rangePanel.getLinePaint();
             BasicStroke ranStroke = rangePanel.getLineStroke();
             cPlot.setRangeGridlinePaint(ranPaint);
             theme.setRangeGridlinePaint(ranPaint);
             cPlot.setRangeGridlineStroke(ranStroke);
             theme.setRangeGridlineStroke(ranStroke);
 
             CategoryItemRenderer renderer = cPlot.getRenderer();
 
             boolean labelsVis = labelsVisible.isSelected();
             renderer.setBaseItemLabelsVisible(labelsVis);
             theme.setLabelsVisible(labelsVis);
 
             Font labelFont = this.labelFont.getChosenFont();
             renderer.setBaseItemLabelFont(labelFont);
             theme.setLabelFont(labelFont);
 
             Paint labelPaint = this.labelPaint.getChosenPaint();
             renderer.setBaseItemLabelPaint(labelPaint);
             theme.setLabelPaint(labelPaint);
 
             String formatText = labelFormat.getText();
             theme.setLabelFormat(formatText);
             String numFormatString = numberFormatDisplay.getFormatString();
             String perFormatString = percentFormatDisplay.getFormatString();
             theme.setNumberFormatString(numFormatString);
             theme.setPercentFormatString(perFormatString);
 
 
             DecimalFormat numF = new DecimalFormat(numFormatString);
             DecimalFormat perF = new DecimalFormat(perFormatString);
             StandardCategoryItemLabelGenerator sLabelGen = new StandardCategoryItemLabelGenerator(
                     formatText, numF, perF
             );
             renderer.setBaseItemLabelGenerator(sLabelGen);
 
         } else if (plot instanceof XYPlot) {
             XYPlot xyPlot = (XYPlot) plot;
             RectangleInsets offset = axisOffset.getSelectedInsets();
             xyPlot.setAxisOffset(offset);
             theme.setAxisOffset(offset);
 
             boolean domVis = domainPanel.isLineVisible();
             xyPlot.setDomainGridlinesVisible(domVis);
             theme.setDomainGridlineVisible(domVis);
 
             boolean ranVis = rangePanel.isLineVisible();
             xyPlot.setRangeGridlinesVisible(ranVis);
             theme.setRangeGridlinesVisible(ranVis);
 
             Paint domPaint = domainPanel.getLinePaint();
             BasicStroke domStroke = domainPanel.getLineStroke();
             xyPlot.setDomainGridlinePaint(domPaint);
             theme.setDomainGridlinePaint(domPaint);
             xyPlot.setDomainGridlineStroke(domStroke);
             theme.setDomainGridlineStroke(domStroke);
 
             Paint ranPaint = rangePanel.getLinePaint();
             BasicStroke ranStroke = rangePanel.getLineStroke();
             xyPlot.setRangeGridlinePaint(ranPaint);
             theme.setRangeGridlinePaint(ranPaint);
             xyPlot.setRangeGridlineStroke(ranStroke);
             theme.setRangeGridlineStroke(ranStroke);
 
             XYItemRenderer renderer = xyPlot.getRenderer();
 
             boolean labelsVis = labelsVisible.isSelected();
             renderer.setBaseItemLabelsVisible(labelsVis);
             theme.setLabelsVisible(labelsVis);
 
             Font labelFont = this.labelFont.getChosenFont();
             renderer.setBaseItemLabelFont(labelFont);
             theme.setLabelFont(labelFont);
 
             Paint labelPaint = this.labelPaint.getChosenPaint();
             renderer.setBaseItemLabelPaint(labelPaint);
             theme.setLabelPaint(labelPaint);
 
             String formatText = labelFormat.getText();
             theme.setLabelFormat(formatText);
             String numFormatString = numberFormatDisplay.getFormatString();
             String perFormatString = percentFormatDisplay.getFormatString();
             theme.setNumberFormatString(numFormatString);
             theme.setPercentFormatString(perFormatString);
 
             // 2 copies for thread safety.
             DecimalFormat numF = new DecimalFormat(numFormatString);
             DecimalFormat numF2 = new DecimalFormat(numFormatString);
             StandardXYItemLabelGenerator sLabelGen = new StandardXYItemLabelGenerator(
                     formatText, numF, numF2
             );
             renderer.setBaseItemLabelGenerator(sLabelGen);
 
         } else if (plot instanceof PiePlot) {
             PiePlot piePlot = (PiePlot) plot;
 
             boolean circular = circularPlot.isSelected();
             theme.setCircularPie(circular);
             piePlot.setCircular(circular);
 
             double angle = ((Number)startAngle.getValue()).doubleValue();
             theme.setStartAngle(angle);
             piePlot.setStartAngle(angle);
 
             Rotation direction = (Rotation)pieDirection.getSelectedObject();
             theme.setPieDirection(direction);
             piePlot.setDirection(direction);
 
             boolean visible = labelsVisible.isSelected();
             theme.setLabelsVisible(visible);
             String formatText = labelFormat.getText();
             theme.setLabelFormat(formatText);
             String numFormatString = numberFormatDisplay.getFormatString();
             String perFormatString = percentFormatDisplay.getFormatString();
             theme.setNumberFormatString(numFormatString);
             theme.setPercentFormatString(perFormatString);
 
             if(visible) {
                 DecimalFormat numF = new DecimalFormat(numFormatString);
                 DecimalFormat perF = new DecimalFormat(perFormatString);
                 StandardPieSectionLabelGenerator sLabelGen = new StandardPieSectionLabelGenerator(
                         formatText, numF, perF
                 );
                 piePlot.setLabelGenerator(sLabelGen);
             } else {
                 piePlot.setLabelGenerator(null);
             }
 
             if(visible) {
                 Font font = labelFont.getChosenFont();
                 theme.setLabelFont(font);
                 piePlot.setLabelFont(font);
 
                 Paint paint = labelPaint.getChosenPaint();
                 theme.setLabelPaint(paint);
                 piePlot.setLabelLinkPaint(paint);
 
                 paint = labelBackgroundPaint.getChosenPaint();
                 theme.setLabelBackgroundPaint(paint);
                 piePlot.setLabelBackgroundPaint(paint);
 
                 paint = labelOutlinePaint.getChosenPaint();
                 theme.setLabelOutlinePaint(paint);
                 piePlot.setLabelOutlinePaint(paint);
 
                 BasicStroke stroke = labelOutlineStroke.getChosenStroke();
                 theme.setLabelOutlineStroke(stroke);
                 piePlot.setLabelOutlineStroke(stroke);
 
                 RectangleInsets padding = labelPadding.getSelectedInsets();
                 theme.setLabelPadding(padding);
                 piePlot.setLabelPadding(padding);
 
                 Paint shadowPaint = labelShadowPaint.getChosenPaint();
                 theme.setLabelShadowPaint(shadowPaint);
                 piePlot.setLabelShadowPaint(shadowPaint);
 
                 boolean linkVis = labelLinkVisible.isSelected();
                 theme.setLabelLinksVisible(linkVis);
                 piePlot.setLabelLinksVisible(linkVis);
 
                 if(linkVis) {
                     PieLabelLinkStyle style = (PieLabelLinkStyle) labelLinkStyle.getSelectedObject();
                     theme.setLabelLinkStyle(style);
                     piePlot.setLabelLinkStyle(style);
                 }
             }
         }
 
         if(this.shadowsVisible != null) {
             boolean shadowsVisible = this.shadowsVisible.isSelected();
             theme.setShadowsVisible(shadowsVisible);
             if(plot instanceof PiePlot) {
                 PiePlot pPlot = (PiePlot) plot;
 
                 Paint shadow = shadowsVisible ? Color.LIGHT_GRAY : null;
                 pPlot.setShadowPaint(shadow);
             } else if (plot instanceof CategoryPlot) {
                 CategoryPlot cPlot = (CategoryPlot) plot;
                 for(int i = 0; i < cPlot.getRendererCount(); i++) {
                     if(cPlot.getRenderer(i) instanceof BarRenderer) {
                         ((BarRenderer)cPlot.getRenderer(i)).setShadowVisible(shadowsVisible);
                     }
                 }
             } else if (plot instanceof XYPlot) {
                 XYPlot xyPlot = (XYPlot) plot;
                 for(int i = 0; i < xyPlot.getRendererCount(); i++) {
                     if(xyPlot.getRenderer(i) instanceof XYBarRenderer) {
                         ((XYBarRenderer)xyPlot.getRenderer(i)).setShadowVisible(shadowsVisible);
                     }
                 }
             }
         }
 
         // then the axis properties...
         if (this.domainAxisPropertyPanel != null) {
             Axis domainAxis = null;
             if (plot instanceof CategoryPlot) {
                 CategoryPlot p = (CategoryPlot) plot;
                 domainAxis = p.getDomainAxis();
             }
             else if (plot instanceof XYPlot) {
                 XYPlot p = (XYPlot) plot;
                 domainAxis = p.getDomainAxis();
             }
             if (domainAxis != null) {
                 this.domainAxisPropertyPanel.updateChart(chart);
             }
         }
 
         if (this.rangeAxisPropertyPanel != null) {
             Axis rangeAxis = null;
             if (plot instanceof CategoryPlot) {
                 CategoryPlot p = (CategoryPlot) plot;
                 rangeAxis = p.getRangeAxis();
             }
             else if (plot instanceof XYPlot) {
                 XYPlot p = (XYPlot) plot;
                 rangeAxis = p.getRangeAxis();
             }
             if (rangeAxis != null) {
                 this.rangeAxisPropertyPanel.updateChart(chart);
             }
         }
 
         if (this.plotOrientation != null) {
             if (plot instanceof CategoryPlot) {
                 CategoryPlot p = (CategoryPlot) plot;
                 p.setOrientation(this.plotOrientation);
             }
             else if (plot instanceof XYPlot) {
                 XYPlot p = (XYPlot) plot;
                 p.setOrientation(this.plotOrientation);
             }
             theme.setOrientation(this.plotOrientation);
         }
 
         applyCustomEditors(chart);
     }
 
 
 
     private boolean formatValid() {
         String format = labelFormat.getText();
         try {
             new MessageFormat(format);
             return true;
         } catch (IllegalArgumentException e) {
             return false;
         }
     }
 
     public void setLiveUpdates(boolean val) {
         super.setLiveUpdates(val);
         if(domainAxisPropertyPanel != null) {
             domainAxisPropertyPanel.setLiveUpdates(val);
         }
         if(rangeAxisPropertyPanel != null) {
             rangeAxisPropertyPanel.setLiveUpdates(val);
         }
     }
 
     public void setChart(JFreeChart chart) {
         super.setChart(chart);
        if(domainAxisPropertyPanel != null) {
            domainAxisPropertyPanel.setChart(chart);
        }
        if(rangeAxisPropertyPanel != null) {
            rangeAxisPropertyPanel.setChart(chart);
        }
     }
 
 
     private class DocHandler implements DocumentListener, FocusListener {
         String lastGoodFormat = labelFormat.getText();
 
         public void changedUpdate(DocumentEvent e) {
             if(formatValid()) {
                 labelFormat.setBackground(Color.WHITE);
                 labelFormat.setForeground(Color.BLACK);
                 lastGoodFormat = labelFormat.getText();
                 updateHandler.changedUpdate(e);
             } else {
                 labelFormat.setBackground(Color.RED);
                 labelFormat.setForeground(Color.WHITE);
             }
         }
 
         public void insertUpdate(DocumentEvent e) {
             if(formatValid()) {
                 labelFormat.setBackground(Color.WHITE);
                 labelFormat.setForeground(Color.BLACK);
                 lastGoodFormat = labelFormat.getText();
                 updateHandler.removeUpdate(e);
             } else {
                 labelFormat.setBackground(Color.RED);
                 labelFormat.setForeground(Color.WHITE);
             }
         }
 
         public void removeUpdate(DocumentEvent e) {
             if(formatValid()) {
                 labelFormat.setBackground(Color.WHITE);
                 labelFormat.setForeground(Color.BLACK);
                 lastGoodFormat = labelFormat.getText();
                 updateHandler.removeUpdate(e);
             } else {
                 labelFormat.setBackground(Color.RED);
                 labelFormat.setForeground(Color.WHITE);
             }
         }
 
         public void focusGained(FocusEvent e) {
             // do nothing.
         }
 
         public void focusLost(FocusEvent e) {
             if(!formatValid()) {
                 labelFormat.setText(lastGoodFormat);
             }
         }
     }
 
 }
