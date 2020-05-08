 package ua.kharkov.kture.ot.view.swing.additional.optimization;
 
 import com.google.inject.name.Named;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.XYToolTipGenerator;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
 import org.jfree.data.xy.DefaultTableXYDataset;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.VerticalAlignment;
 import sun.awt.VerticalBagLayout;
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.common.math.ComparableNumber;
 import ua.kharkov.kture.ot.common.math.Probability;
 import ua.kharkov.kture.ot.shared.OptimizerCriterionKeeper;
 import ua.kharkov.kture.ot.shared.simpleobjects.ComponentDTO;
 import ua.kharkov.kture.ot.shared.simpleobjects.VariantDTO;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.MinimizationOptimizationCriteria;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.OptimizerController;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.OptimizerController.SystemDescriber;
 import ua.kharkov.kture.ot.view.declaration.viewers.Window;
 import ua.kharkov.kture.ot.view.swing.additional.AbstractAdditionalWindow;
 import ua.kharkov.kture.ot.view.swing.additional.componetedit.ProbabilityRenderer;
 
 import javax.inject.Inject;
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.*;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 
 /**
  * @author: Stanislav Kurilin
  * @mail: st dot kurilin at gmail dot com
  * Date: 09.05.11
  */
 public class OptimizationWindow extends AbstractAdditionalWindow {
     private Collection<ComponentDTO> components;
     private OptimizerController optimizerController;
     private Map<String, MinimizationOptimizationCriteria> criteria;
     private Map<String, MinimizationOptimizationCriteria> coordinate;
     private MessageBundle bundle;
     private OptimizerCriterionKeeper criterionKeeper;
 
 
     private MinimizationOptimizationCriteria xAxis;
     private MinimizationOptimizationCriteria yAxis;
 
     private JComboBox criteriaSelection;
     //    private JCheckBox ignoreWorstCheck = new JCheckBox("Ignore worst");
     private JButton optimizeButton = new JButton("Optimize");
     private JTable resultTable = new JTable() {
 
         public String getToolTipText(MouseEvent e) {
             String tip = null;
             java.awt.Point p = e.getPoint();
             int rowIndex = rowAtPoint(p);
             int colIndex = columnAtPoint(p);
             tip = getValueAt(rowIndex, colIndex).toString();
             return tip;
         }
 
 
     };
     private ChartPanel chartPanel = null;
     private JPanel chartPanelWrapper = new JPanel();
 
     private ua.kharkov.kture.ot.view.declaration.viewers.Window.ApplicationPresenter presenter;
 
     public boolean isValidToDisplay() {
         if (components.isEmpty()) {
             return false;
         }
         for (ComponentDTO component : components) {
             if (component.getVariants().isEmpty()) {
                 return false;
             }
             for (VariantDTO variant : component.getVariants()) {
                 if (variant.getCost() == null || variant.getCrashProbability() == null || variant.getName() == null) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     @Override
     protected void go() {
         this.setTitle(bundle.getMessage("title"));
         optimizeButton.setText(bundle.getMessage("optimize"));
         this.setLayout(new VerticalBagLayout());
         tableAfterInit();
 //        this.setResizable(false);
 
         chartPanelWrapper.setSize(new Dimension((int) 800, 500));
         JPanel context = new JPanel(new VerticalBagLayout());
         context.add(chartPanelWrapper);
         JPanel selectionPanel = new JPanel();
         criteriaSelection = new JComboBox(getArray(criteria));
         criteriaSelection.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 criterionKeeper.set(((SelectionWrapper<MinimizationOptimizationCriteria>) (criteriaSelection.getSelectedItem())).getObject());
             }
         });
         selectionPanel.add(criteriaSelection);
 //        selectionPanel.add(ignoreWorstCheck);
         selectionPanel.add(optimizeButton);
         context.add(selectionPanel);
 
         resultTable.setFillsViewportHeight(true);
         resultTable.setAutoCreateRowSorter(true);
         resultTable.setModel(getNewTableModel());
         resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
         resultTable.getTableHeader().setReorderingAllowed(false);
 
         JPanel resultPanel = new JPanel(new BorderLayout());
         resultPanel.add(resultTable.getTableHeader(), BorderLayout.PAGE_START);
         resultPanel.add(resultTable, BorderLayout.PAGE_END);
         resultPanel.setBackground(Color.red);
         context.add(resultPanel);
 
         JScrollPane scrollPane = new JScrollPane(context, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         this.setContentPane(scrollPane);
 
         optimizeButton.addActionListener(new OptimizeButtonActionListener());
         optimizeButton.doClick();
         this.pack();
     }
 
     private void tableAfterInit() {
         resultTable.setDefaultRenderer(Probability.class, new ProbabilityRenderer());
         resultTable.setDefaultRenderer(ComparableNumber.class, new DefaultTableCellRenderer() {
             public void setValue(Object value) {
                 if (value == null) {
                     setText("");
                     return;
                 }
                 if (!(value instanceof ComparableNumber)) {
                     throw new AssertionError("Double's instance expected, but" + value + "found");
                 }
                 setText(new DecimalFormat("#0.######", new DecimalFormatSymbols(Locale.US)).format(((ComparableNumber) value).doubleValue()));
             }
         });
     }
 
     @Inject
     public void setPresenter(Window.ApplicationPresenter presenter) {
         this.presenter = presenter;
     }
 
     Map<SystemDescriber, List<?>> rows = newHashMap();
 
     private class OptimizeButtonActionListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             rows.clear();
             DefaultTableModel tableModel = getNewTableModel();
             Collection<SystemDescriber> systemVariants = optimizerController.withoutWorstBySystemVariant();
 
             Map<Double, Double> chartMap = new HashMap<Double, Double>();
             String xName = "";
             String yName = "";
             for (Map.Entry<String, MinimizationOptimizationCriteria> c : coordinate.entrySet()) {
                 if (xAxis == null) {
                     xAxis = c.getValue();
                     continue;
                 }
                 if (yAxis == null) {
                     yAxis = c.getValue();
                     continue;
                 }
             }
             for (final Map.Entry<String, MinimizationOptimizationCriteria> c : coordinate.entrySet()) {
                 if (c.getValue().equals(xAxis)) {
                     xName = c.getKey();
                 }
                 if (c.getValue().equals(yAxis)) {
                     yName = c.getKey();
                 }
             }
             int counter = 1;
             MinimizationOptimizationCriteria optCrit = ((SelectionWrapper<MinimizationOptimizationCriteria>) criteriaSelection.getSelectedItem()).getObject();
             SystemDescriber baseSysDesc = optimizerController.bestBy(optCrit);
             for (SystemDescriber systemVariant : systemVariants) {
                 chartMap.put(optimizerController.valueByCriteria(systemVariant, xAxis).doubleValue(), optimizerController.valueByCriteria(systemVariant, yAxis).doubleValue());
                 List<Object> row = new ArrayList<Object>();
                 row.add(counter++);
                 for (Map.Entry<String, MinimizationOptimizationCriteria> tableColomn : coordinate.entrySet()) {
                     ComparableNumber foo = optimizerController.valueByCriteria(systemVariant, tableColomn.getValue());
                    row.add(String.format("%.8f", foo.doubleValue()));
                 }
                 row.add(getConfig(systemVariant));
                 rows.put(systemVariant, row);
                 tableModel.addRow(row.toArray());
             }
             showChart(chartMap, optimizerController.valueByCriteria(baseSysDesc, xAxis).doubleValue(), xName, yName);
 
             resultTable.setModel(tableModel);
             pack();
         }
 
         private String getConfig(SystemDescriber systemVariant) {
             StringBuffer buf = new StringBuffer();
             for (Map.Entry<String, String> variant : systemVariant.configuration().entrySet()) {
                 buf.append(variant.getKey()).append("=>").append(variant.getValue()).append(", \n\r");
             }
             return buf.toString();
         }
     }
 
     private DefaultTableModel getNewTableModel() {
         List<String> row = new ArrayList<String>();
         row.add(bundle.getMessage("id"));
         for (Map.Entry<String, MinimizationOptimizationCriteria> tableColomn : coordinate.entrySet()) {
             row.add(tableColomn.getKey());
         }
         row.add(bundle.getMessage("config"));
 //        bundle.getMessage("config");
         DefaultTableModel defaultTableModel = new DefaultTableModel(row.toArray(), 0) {
 
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
 
             @Override
             public Class<?> getColumnClass(int column) {
                 try {
                     return getValueAt(0, column).getClass();
                 } catch (Exception e) {
                     return String.class;
                 }
             }
         };
         return defaultTableModel;
     }
 
     public void showChart(Map<Double, Double> map, Double base, String xName, String yName) {
 //        setVisible(false);
         JFreeChart chart = createChart(createDataset(map, base), xName, yName);
         if (chartPanel == null) {
             chartPanel = new ChartPanel(chart);
             chartPanel.setMouseZoomable(true);
             chartPanel.setPreferredSize(new Dimension(500, 250));
             chartPanel.setMaximumSize(new Dimension(500, 250));
             chartPanel.setPopupMenu(new ChartMenu());
             chartPanelWrapper.add(chartPanel);
         } else {
             chartPanel.setChart(chart);
         }
 //        chartPanel.setPreferredSize(new Dimension(450, 450));
 //        JScrollPane sp = new JScrollPane(chartPanel);
 //        sp.setPreferredSize(new Dimension(500, 500));
 //        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 //        add(sp);
 //        setVisible(true);
 
     }
 
     private DefaultTableXYDataset createDataset(Map<Double, Double> map, Double base) {
 
         DefaultTableXYDataset dataset = new DefaultTableXYDataset();
         XYSeries s1 = new XYSeries(bundle.getMessage("base"), true, false);
         XYSeries s2 = new XYSeries(bundle.getMessage("other"), true, false);
         for (Map.Entry<Double, Double> m : map.entrySet()) {
             if (m.getKey().equals(base)) {
                 s1.add(m.getKey(), m.getValue(), true);
             } else {
                 s2.add(m.getKey(), m.getValue(), true);
             }
         }
         dataset.addSeries(s1);
         dataset.addSeries(s2);
 
         dataset.setAutoWidth(true);
 
         return dataset;
 
     }
 
     private JFreeChart createChart(DefaultTableXYDataset dataset, String xName, String yName) {
 
         NumberAxis domainAxis = new NumberAxis(xName);
         domainAxis.setPositiveArrowVisible(true);
         if (Math.max(dataset.getSeries(0).getMaxX(), dataset.getSeries(1).getMaxX())
                 - Math.min(dataset.getSeries(0).getMinX(), dataset.getSeries(1).getMinX()) < 20) {
             domainAxis.setLowerBound(((Math.max(dataset.getSeries(0).getMaxX(), dataset.getSeries(1).getMaxX())
                     + Math.min(dataset.getSeries(0).getMinX(), dataset.getSeries(1).getMinX())) / 2) - 10);
             domainAxis.setUpperBound(((Math.max(dataset.getSeries(0).getMaxX(), dataset.getSeries(1).getMaxX())
                     + Math.min(dataset.getSeries(0).getMinX(), dataset.getSeries(1).getMinX())) / 2) + 10);
         } else {
             domainAxis.setLowerBound(Math.min(dataset.getSeries(0).getMinX(), dataset.getSeries(1).getMinX()) - 0.5);
             domainAxis.setUpperBound(Math.max(dataset.getSeries(0).getMaxX(), dataset.getSeries(1).getMaxX()) + 0.5);
         }
 //        domainAxis.setUpperMargin(0.2);
 
 
         NumberAxis rangeAxis = new NumberAxis(yName);
 //        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
 //        rangeAxis.setTickUnit(new NumberTickUnit(200));
 //        rangeAxis.setPositiveArrowVisible(true);
 
 
         // Render
         StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.02);
 //        renderer.setDrawBarOutline(false);
         renderer.setSeriesPaint(0, Color.blue);
         renderer.setSeriesPaint(1, Color.red);
         renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
             @Override
             public String generateToolTip(XYDataset arg0, int series, int item) {
                 final SystemDescriber best = optimizerController.bestBy((MinimizationOptimizationCriteria) ((SelectionWrapper) criteriaSelection.getSelectedObjects()[0]).getObject());
                 if (series == 0) {//best
                     return rows.get(best).get(0).toString();
                 }
                 if (series == 1) {//common
                     List<SystemDescriber> all = newArrayList(optimizerController.withoutWorstBySystemVariant());
                     Collections.sort(all, new Comparator<SystemDescriber>() {
                         @Override
                         public int compare(SystemDescriber o1, SystemDescriber o2) {
                             return optimizerController.valueByCriteria(o1, xAxis).compareTo(optimizerController.valueByCriteria(o2, xAxis));
                         }
                     });
                     return rows.get(all.get(item)).get(0).toString();
                 }
                 return "HZ";
             }
         });
 //        renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
 //        renderer.setSeriesItemLabelGenerator(1, new StandardXYItemLabelGenerator());
         renderer.setSeriesItemLabelsVisible(0, true);
         renderer.setSeriesItemLabelsVisible(1, true);
         renderer.setSeriesItemLabelFont(0, new Font("Serif", Font.BOLD, 10));
         renderer.setSeriesItemLabelFont(1, new Font("Serif", Font.BOLD, 10));
         renderer.setShadowVisible(false);
 
 
         // Plot
         XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
 //        plot.setBackgroundPaint(Color.white);
 //        plot.setDomainGridlinePaint(Color.white);
 //        plot.setRangeGridlinePaint(Color.white);
 //        plot.setAxisOffset(new RectangleInsets(0D, 0D, 10D, 10D));
 //        plot.setOutlinePaint(null);
 
 
         // Chart
         JFreeChart chart = new JFreeChart(plot);
         chart.setBackgroundPaint(Color.white);
         chart.getLegend().setPosition(RectangleEdge.RIGHT);
         chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
 
 
         return chart;
     }
 
     private static <T> T[] getArray(Map<String, T> map) {
         List<SelectionWrapper<T>> list = new ArrayList<SelectionWrapper<T>>();
         for (Map.Entry<String, T> entry : map.entrySet()) {
             list.add(new OptimizationWindow.SelectionWrapper<T>(entry));
         }
         return (T[]) list.toArray();
     }
 
     @Inject
     public void setComponents(Collection<ComponentDTO> components) {
         this.components = components;
     }
 
     @Inject
     public void setOptimizerController(OptimizerController optimizerController) {
         this.optimizerController = optimizerController;
     }
 
     @Inject
     public void setCriteria(@Named("criteria") Map<String, MinimizationOptimizationCriteria> criteria) {
         this.criteria = criteria;
     }
 
     @Inject
     public void setCoordinate(@Named("coordinate") Map<String, MinimizationOptimizationCriteria> coordinate) {
         this.coordinate = coordinate;
     }
 
     @Inject
     public void setBundle(@Named("optimization") MessageBundle bundle) {
         this.bundle = bundle;
     }
 
     @Inject
     public void setCriterionKeeper(OptimizerCriterionKeeper criterionKeeper) {
         this.criterionKeeper = criterionKeeper;
     }
 
     class ChartMenu extends JPopupMenu {
 
         Map<MinimizationOptimizationCriteria, JMenuItem> xAxisMenuItems = new HashMap<MinimizationOptimizationCriteria, JMenuItem>();
         Map<MinimizationOptimizationCriteria, JMenuItem> yAxisMenuItems = new HashMap<MinimizationOptimizationCriteria, JMenuItem>();
 
         public ChartMenu() {
             JMenu xAxisItem = new JMenu("X");
             JMenu yAxisItem = new JMenu("Y");
             add(xAxisItem);
             add(yAxisItem);
             for (final Map.Entry<String, MinimizationOptimizationCriteria> c : coordinate.entrySet()) {
                 JMenuItem xItem = new JMenuItem(c.getKey());
                 xItem.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         xAxis = c.getValue();
                         changeVisible(yAxisMenuItems, c.getValue());
                     }
                 });
                 xAxisItem.add(xItem);
                 xAxisMenuItems.put(c.getValue(), xItem);
 
                 JMenuItem yItem = new JMenuItem(c.getKey());
                 yItem.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         yAxis = c.getValue();
                         changeVisible(xAxisMenuItems, c.getValue());
                     }
                 });
                 yAxisItem.add(yItem);
                 yAxisMenuItems.put(c.getValue(), yItem);
 
             }
 
             xAxisMenuItems.get(xAxis).setEnabled(false);
             yAxisMenuItems.get(yAxis).setEnabled(false);
         }
 
         private void changeVisible(Map<MinimizationOptimizationCriteria, JMenuItem> axisMenuItems, MinimizationOptimizationCriteria select) {
             for (final Map.Entry<String, MinimizationOptimizationCriteria> c : coordinate.entrySet()) {
                 axisMenuItems.get(c.getValue()).setEnabled(true);
             }
             axisMenuItems.get(select).setEnabled(false);
             optimizeButton.doClick();
         }
 
 
     }
 
     static class SelectionWrapper<T> {
 
         private String name;
         private T object;
 
         public SelectionWrapper(Map.Entry<String, T> entry) {
             name = entry.getKey();
             object = entry.getValue();
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public T getObject() {
             return object;
         }
 
         public void setObject(T object) {
             this.object = object;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
     }
 
 }
