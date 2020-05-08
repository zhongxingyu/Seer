 
 //   Copyright Mark Burton 2011
 //
 //   This program is free software: you can redistribute it and/or modify
 //   it under the terms of the GNU General Public License as published by
 //   the Free Software Foundation, either version 3 of the License, or
 //   (at your option) any later version.
 //
 //   This program is distributed in the hope that it will be useful,
 //   but WITHOUT ANY WARRANTY; without even the implied warranty of
 //   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //   GNU General Public License for more details.
 
 //   You should have received a copy of the GNU General Public License
 //   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.sql.*;
 import java.util.*;
 import java.io.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import java.text.SimpleDateFormat;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import javax.imageio.ImageIO;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.entity.ChartEntity;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.chart.entity.CategoryItemEntity;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.category.AreaRenderer;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.Month;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RectangleInsets;
 import org.jfree.ui.RefineryUtilities;
 
 public class PVGraph extends ApplicationFrame {
     
     static Properties props;
     static long propsLastLoadedAt;
     
     static LinkedList<PVGraph> graphs = new LinkedList<PVGraph>();
     
     static Connection conn;
 
     private Calendar date;
     private JTabbedPane tabPane;
     private boolean trackDay = false;
     private PVGraphView[] views;
     
     static final String WINDOW_TITLE_PREFIX = "PVGraph";
 
     static final int DAY_VIEW_INDEX   = 0;
     static final int MONTH_VIEW_INDEX = 1;
     static final int YEAR_VIEW_INDEX  = 2;
     static final int YEARS_VIEW_INDEX = 3;
     
     private class DayData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
         java.util.List<Timestamp> times = new java.util.ArrayList<Timestamp>(12 * 24);
         java.util.List<Integer> powers = new java.util.ArrayList<Integer>(12 * 24);
     };
 
     private class YearsData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
         java.util.Map<Integer, Double> powers = new java.util.LinkedHashMap<Integer, Double>();
     };
 
     private class PeriodData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
         double powers[] = new double[366];
         int numPowers;
     };
     
     private interface PVGraphView {
         void updateChart();
         String getTabLabel();
         JPanel makePanel();
         boolean handleKey(int charCode);
     }
 
     public PVGraph() {
         this(new GregorianCalendar(), DAY_VIEW_INDEX);
     }
 
     public PVGraph(Calendar date, int initialViewIndex) {
         super(WINDOW_TITLE_PREFIX);
         this.date = date;
         synchronized(graphs) {
             graphs.add(this);
         }
 
         views = new PVGraphView[4];
         views[DAY_VIEW_INDEX] = new DayView();
         views[MONTH_VIEW_INDEX] = new MonthView();
         views[YEAR_VIEW_INDEX] = new YearView();
         views[YEARS_VIEW_INDEX] = new YearsView();
         
         tabPane = new JTabbedPane();
         for(PVGraphView v : views)
             tabPane.addTab(v.getTabLabel(), v.makePanel());
         tabPane.setSelectedIndex(initialViewIndex);
         setContentPane(tabPane);
         pack();
         try {
             java.net.URL url = getClass().getResource("sun.png");
             if(url != null)
                 setIconImage(ImageIO.read(url));
         }
         catch (IOException ioe) {
             System.err.println(ioe.getMessage());
         };
         setVisible(true);
         
         KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                 public boolean dispatchKeyEvent(KeyEvent ke) {
                     Object src = ke.getSource();
                     if(src instanceof JComponent && ((JComponent)src).getRootPane().getContentPane() == tabPane) {
                         if(ke.getID() == KeyEvent.KEY_TYPED) {
                             switch(ke.getKeyChar()) {
                             case 'd':
                                 tabPane.setSelectedIndex(DAY_VIEW_INDEX);
                                 return true;
                             case 'm':
                                 tabPane.setSelectedIndex(MONTH_VIEW_INDEX);
                                 return true;
                             case 'N' - 0x40:
                                 new PVGraph((Calendar)PVGraph.this.date.clone(), tabPane.getSelectedIndex());
                                 return true;
                             case 'Q' - 0x40:
                                 dispatchEvent(new WindowEvent(PVGraph.this, WindowEvent.WINDOW_CLOSING));
                                 return true;
                             case 'R' - 0x40:
                                 loadProperties();
                                 updateView();
                                 return true;
                             case 'S':
                                 try {
                                     runSmatool();
                                     updateView();
                                 }
                                 catch (IOException ioe) {
                                     System.err.println(ioe.getMessage());
                                 }
                                 return true;
                             case 'y':
                                 tabPane.setSelectedIndex(YEAR_VIEW_INDEX);
                                 return true;
                             case 'Y':
                                 tabPane.setSelectedIndex(YEARS_VIEW_INDEX);
                                 return true;
                             default:
                                 return views[tabPane.getSelectedIndex()].handleKey(ke.getKeyChar());
                             }
                         }
                     }
                     return false;
                 }
         });
     }
     
     public void updateView() {
         if(trackDay)
             date = new GregorianCalendar();
         views[tabPane.getSelectedIndex()].updateChart();
     }
     
     private JPanel makeCommonButtonsPanel(final PVGraphView view) {
         JPanel commonButtonsPanel = new JPanel();
         commonButtonsPanel.setBorder(new EtchedBorder());
 
         JButton newGraphButton = new JButton("New Graph");
         newGraphButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     new PVGraph((Calendar)PVGraph.this.date.clone(), tabPane.getSelectedIndex());
                 }
         });
 
         JButton runSmatoolButton = new JButton("Run smatool");
         runSmatoolButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     try {
                         runSmatool();
                         view.updateChart();
                     }
                     catch (IOException ioe) {
                         System.err.println(ioe.getMessage());
                     }
                 }
         });
 
         commonButtonsPanel.add(newGraphButton);
         if(Integer.decode(props.getProperty("smatool.havebutton", "1")) != 0)
             commonButtonsPanel.add(runSmatoolButton);
 
         int smatoolPeriod = Integer.decode(props.getProperty("smatool.period", "0"));
         if(smatoolPeriod > 0) {
             final JRadioButton trackDayRadioButton = new JRadioButton("Track day");
             trackDayRadioButton.setSelected(trackDay);
             trackDayRadioButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         trackDay = trackDayRadioButton.isSelected();
                     }
             });
             commonButtonsPanel.add(trackDayRadioButton);
         }
         
         return commonButtonsPanel;
     }
     
     private class DayView implements PVGraphView {
         
         ChartPanel dayChartPanel;
         
         public String getTabLabel() {
             return "Day";
         }
         
         public void setWindowTitle() {
             setTitle(WINDOW_TITLE_PREFIX + " - " + new SimpleDateFormat("MMMMM d yyyy").format(date.getTime())); 
         }
 
         public void updateChart() {
             //System.out.println("Updating day view for " + date.getTime());
             dayChartPanel.setChart(createChart());
             setWindowTitle();
         }
         
         public boolean handleKey(int charCode) {
             if(charCode == '+' || charCode == '=') {
                 date.add(Calendar.DAY_OF_MONTH, 1);
                 updateChart();
                 return true;
             }
             if(charCode == '-') {
                 date.add(Calendar.DAY_OF_MONTH, -1);
                 updateChart();
                 return true;
             }
             return false;
         }
         
         public JPanel makePanel() {
             
             JPanel dayPanel = new JPanel();
             dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
             
             dayChartPanel = new ChartPanel(null);
             dayChartPanel.setFillZoomRectangle(true);
             dayChartPanel.setMouseWheelEnabled(true);
             dayChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
             dayPanel.add(dayChartPanel);
             
             JButton dayDecButton = new JButton("-");
             dayDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.DAY_OF_MONTH, -1);
                         updateChart();
                     }
             });
             JButton dayIncButton = new JButton("+");
             dayIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.DAY_OF_MONTH, 1);
                         updateChart();
                     }
             });
             
             JButton monthDecButton = new JButton("-");
             monthDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.MONTH, -1);
                         updateChart();
                     }
             });
             JButton monthIncButton = new JButton("+");
             monthIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.MONTH, 1);
                         updateChart();
                     }
             });
             
             JButton yearDecButton = new JButton("-");
             yearDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, -1);
                         updateChart();
                     }
             });
             JButton yearIncButton = new JButton("+");
             yearIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, 1);
                         updateChart();
                     }
             });
             
             JPanel buttonsPanel = new JPanel();
             buttonsPanel.setBorder(new EtchedBorder());
             dayPanel.add(buttonsPanel);
             
             JPanel dayButtonsPanel = new JPanel();
             dayButtonsPanel.setBorder(new EtchedBorder());
             dayButtonsPanel.add(new JLabel("Day"));
             dayButtonsPanel.add(dayDecButton);
             dayButtonsPanel.add(dayIncButton);
             buttonsPanel.add(dayButtonsPanel);
             
             JPanel monthButtonsPanel = new JPanel();
             monthButtonsPanel.setBorder(new EtchedBorder());
             monthButtonsPanel.add(new JLabel("Month"));
             monthButtonsPanel.add(monthDecButton);
             monthButtonsPanel.add(monthIncButton);
             buttonsPanel.add(monthButtonsPanel);
             
             JPanel yearButtonsPanel = new JPanel();
             yearButtonsPanel.setBorder(new EtchedBorder());
             yearButtonsPanel.add(new JLabel("Year"));
             yearButtonsPanel.add(yearDecButton);
             yearButtonsPanel.add(yearIncButton);
             buttonsPanel.add(yearButtonsPanel);
             
             buttonsPanel.add(makeCommonButtonsPanel(this));
             
             dayPanel.addComponentListener(new ComponentAdapter() {
                     public void componentShown(ComponentEvent ce) {
                         updateChart();
                     }
             });
             
             return dayPanel;
         }
         
         private JFreeChart createChart() {
             
             int year = date.get(Calendar.YEAR);
             int month = date.get(Calendar.MONTH) + 1;
             int day = date.get(Calendar.DAY_OF_MONTH);
             
             java.util.List<DayData> dayData = getDayData(year, month, day);
             
             TimeSeriesCollection dataset = new TimeSeriesCollection();
             double totalDayPower = 0;
             
             for(DayData dd : dayData) {
                 TimeSeries s = new TimeSeries(dd.inverter + (dayData.size() > 1? ("-" + dd.serial) : ""));
                 for(int i = 0; i < dd.times.size(); ++i)
                     s.addOrUpdate(new Minute(dd.times.get(i)), dd.powers.get(i));
                 dataset.addSeries(s);
                 totalDayPower += dd.endTotalPower - dd.startTotalPower;
             }
             
             String dayPower = totalDayPower < 1.0? String.format("%d WH", (int)(totalDayPower * 1000)) : String.format("%.2f KWH", totalDayPower);
             JFreeChart chart = ChartFactory.createXYAreaChart(
                 new SimpleDateFormat("MMMMM d yyyy").format(date.getTime()) + "      " + dayPower, // title
                 "Time",     // x-axis label
                 "Watts",    // y-axis label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             String chartColour = props.getProperty("colour.chart", "WHITE");
             if(chartColour != null)
                 chart.setBackgroundPaint(getColour(chartColour));
             
             XYPlot plot = (XYPlot) chart.getPlot();
             if(dayData.size() == 1)
                 plot.setForegroundAlpha(1.0f);
             String backgroundColour = props.getProperty("colour.background", "LIGHT_GRAY");
             if(backgroundColour != null)
                 plot.setBackgroundPaint(getColour(backgroundColour));
             String gridlineColour = props.getProperty("colour.gridline", "WHITE");
             if(gridlineColour != null) {
                 Color glc = getColour(gridlineColour);
                 plot.setDomainGridlinePaint(glc);
                 plot.setRangeGridlinePaint(glc);
             }
             plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
             /*
             plot.setDomainCrosshairVisible(true);
             plot.setRangeCrosshairVisible(true);
             */
             plot.setDomainPannable(true);
             plot.setRangePannable(true);
             double maxPower = Double.parseDouble(props.getProperty("maxpower.day", "0"));
             if(maxPower > 0) {
                 ValueAxis powerAxis = plot.getRangeAxis();
                 powerAxis.setAutoRange(false);
                 powerAxis.setLowerBound(0.0);
                 powerAxis.setUpperBound(maxPower * 1000);
             }
             
             XYItemRenderer r = plot.getRenderer();
             r.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
             for(int i = 0; i < dayData.size(); ++i) {
                 DayData dd = dayData.get(i);
                 String colour = props.getProperty("colour.plot." + dd.serial, props.getProperty("colour.plot", null));
                 if(colour == null) {
                     // maintain backwards compatibility
                     colour = props.getProperty("plotcolour." + dd.serial, props.getProperty("plotcolour", null));
                 }
                 if(colour != null)
                     r.setSeriesPaint(i, getColour(colour));
             }
             
             DateAxis axis = new DateAxis();
             axis.setLabel(plot.getDomainAxis().getLabel());
             axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
             GregorianCalendar lower = new GregorianCalendar();
             lower.setTime(date.getTime());
             lower.set(Calendar.HOUR_OF_DAY, 0);
             lower.set(Calendar.MINUTE, 0);
             lower.set(Calendar.SECOND, 0);
             GregorianCalendar upper = new GregorianCalendar();
             upper.setTime(lower.getTime());
             upper.add(Calendar.DAY_OF_MONTH, 1);
             axis.setRange(lower.getTime(), upper.getTime());
             plot.setDomainAxis(axis);
             
             return chart;   
         }
     }
     
     private class MonthView implements PVGraphView {
 
         ChartPanel monthChartPanel;
 
         public String getTabLabel() {
             return "Month";
         }
         
         public void setWindowTitle() {
             setTitle(WINDOW_TITLE_PREFIX + " - " + new SimpleDateFormat("MMMMM yyyy").format(date.getTime()));
         }
                 
         public void updateChart() {
             //System.out.println("Updating month view for " + date.getTime());
             monthChartPanel.setChart(createChart());
             setWindowTitle();
         }
         
         public boolean handleKey(int charCode) {
             if(charCode == '+' || charCode == '=') {
                 date.add(Calendar.MONTH, 1);
                 updateChart();
                 return true;
             }
             if(charCode == '-') {
                 date.add(Calendar.MONTH, -1);
                 updateChart();
                 return true;
             }
             return false;
         }
         
         public JPanel makePanel() {
             
             JPanel monthPanel = new JPanel();
             monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));
             
             monthChartPanel = new ChartPanel(null);
             monthChartPanel.setFillZoomRectangle(true);
             monthChartPanel.setMouseWheelEnabled(true);
             monthChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
             monthChartPanel.addChartMouseListener(new ChartMouseListener() {
                     public void chartMouseMoved(ChartMouseEvent cme) { }
                     public void chartMouseClicked(ChartMouseEvent cme) {
                         ChartEntity entity = cme.getEntity();
                         if(entity != null && entity instanceof CategoryItemEntity) {
                             Calendar d = (Calendar)date.clone();
                             d.set(Calendar.DAY_OF_MONTH, (Integer)((CategoryItemEntity)entity).getColumnKey());
                             new PVGraph(d, DAY_VIEW_INDEX);
                         }
                     }
             });
             monthPanel.add(monthChartPanel);
             
             JButton monthDecButton = new JButton("-");
             monthDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.MONTH, -1);
                         updateChart();
                     }
             });
             JButton monthIncButton = new JButton("+");
             monthIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.MONTH, 1);
                         updateChart();
                     }
             });
             
             JButton yearDecButton = new JButton("-");
             yearDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, -1);
                         updateChart();
                     }
             });
             JButton yearIncButton = new JButton("+");
             yearIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, 1);
                         updateChart();
                     }
             });
                         
             JPanel buttonsPanel = new JPanel();
             buttonsPanel.setBorder(new EtchedBorder());
             monthPanel.add(buttonsPanel);
             
             JPanel monthButtonsPanel = new JPanel();
             monthButtonsPanel.setBorder(new EtchedBorder());
             monthButtonsPanel.add(new JLabel("Month"));
             monthButtonsPanel.add(monthDecButton);
             monthButtonsPanel.add(monthIncButton);
             buttonsPanel.add(monthButtonsPanel);
             
             JPanel yearButtonsPanel = new JPanel();
             yearButtonsPanel.setBorder(new EtchedBorder());
             yearButtonsPanel.add(new JLabel("Year"));
             yearButtonsPanel.add(yearDecButton);
             yearButtonsPanel.add(yearIncButton);
             buttonsPanel.add(yearButtonsPanel);
             
             buttonsPanel.add(makeCommonButtonsPanel(this));
             
             monthPanel.addComponentListener(new ComponentAdapter() {
                     public void componentShown(ComponentEvent ce) {
                         updateChart();
                     }
             });
             
             return monthPanel;
         }
         
         private JFreeChart createChart() {
             
             int year = date.get(Calendar.YEAR);
             int month = date.get(Calendar.MONTH) + 1;
             
             java.util.List<PeriodData> periodData = getMonthData(year, month);
             
             DefaultCategoryDataset dataset = new DefaultCategoryDataset();
             double totalPeriodPower = 0;
             
             int indexOfLastNonZeroPower = 0;
             for(PeriodData pd : periodData) {
                 String series = pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : "");
                 double lastPower = pd.startTotalPower;
                 for(int i = 0; i < pd.numPowers; ++i) {
                     if(pd.powers[i] != 0) {
                         dataset.addValue(pd.powers[i] - lastPower, series, (Integer)(i + 1));
                         lastPower = pd.powers[i];
                         if(i > indexOfLastNonZeroPower)
                             indexOfLastNonZeroPower = i;
                     }
                     else
                         dataset.addValue(0, series, "" + (i + 1));
                 }
                 totalPeriodPower += pd.endTotalPower - pd.startTotalPower;
             }
             
             String periodPower = totalPeriodPower < 1.0? String.format("%d WH", (int)(totalPeriodPower * 1000)) : String.format("%.1f KWH", totalPeriodPower);
             double avg = totalPeriodPower / (indexOfLastNonZeroPower + 1);
             periodPower += "      " + (avg < 1.0? String.format("%d WH/Day", (int)(avg * 1000)) : String.format("%.2f KWH/Day", avg));
             
             JFreeChart chart = ChartFactory.createBarChart(
                 new SimpleDateFormat("MMMMM yyyy").format(date.getTime()) + "      " + periodPower, // title
                 "Day",      // domain label
                 "KWH",       // range label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             String chartColour = props.getProperty("colour.chart", "WHITE");
             if(chartColour != null)
                 chart.setBackgroundPaint(getColour(chartColour));
             
             CategoryPlot plot = (CategoryPlot)chart.getPlot();
             String backgroundColour = props.getProperty("colour.background", "LIGHT_GRAY");
             if(backgroundColour != null)
                 plot.setBackgroundPaint(getColour(backgroundColour));
             String gridlineColour = props.getProperty("colour.gridline", "WHITE");
             if(gridlineColour != null) {
                 Color glc = getColour(gridlineColour);
                 plot.setDomainGridlinePaint(glc);
                 plot.setRangeGridlinePaint(glc);
             }
             plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
             //plot.setDomainCrosshairVisible(true);
             //plot.setRangeCrosshairVisible(true);
             double maxPower = Double.parseDouble(props.getProperty("maxpower.month", "0"));
             if(maxPower > 0) {
                 ValueAxis powerAxis = plot.getRangeAxis();
                 powerAxis.setAutoRange(false);
                 powerAxis.setLowerBound(0.0);
                 powerAxis.setUpperBound(maxPower);
             }
             
             CategoryItemRenderer r = plot.getRenderer();
             for(int i = 0; i < periodData.size(); ++i) {
                 PeriodData pd = periodData.get(i);
                 String colour = props.getProperty("colour.plot." + pd.serial, props.getProperty("colour.plot", null));
                 if(colour == null) {
                     // maintain backwards compatibility
                     colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
                 }
                 if(colour != null)
                     r.setSeriesPaint(i, getColour(colour));
             }
             return chart;   
         }
     }
     
     private class YearView implements PVGraphView {
         
         ChartPanel yearChartPanel;
         JRadioButton detailedButton;
 
         public String getTabLabel() {
             return "Year";
         }
         
         public void setWindowTitle() {
             int year = date.get(Calendar.YEAR);
             setTitle(WINDOW_TITLE_PREFIX + " - " + year);
         }
         
         public void updateChart() {
             //System.out.println("Updating year view for " + date.getTime());
             yearChartPanel.setChart(createChart(detailedButton.isSelected()));
             setWindowTitle();
         }
         
         public boolean handleKey(int charCode) {
             if(charCode == '+' || charCode == '=') {
                 date.add(Calendar.YEAR, 1);
                 updateChart();
                 return true;
             }
             if(charCode == '-') {
                 date.add(Calendar.YEAR, -1);
                 updateChart();
                 return true;
             }
             return false;
         }
         
         public JPanel makePanel() {
             
             JPanel yearPanel = new JPanel();
             yearPanel.setLayout(new BoxLayout(yearPanel, BoxLayout.Y_AXIS));
             
             yearChartPanel = new ChartPanel(null);
             yearChartPanel.setFillZoomRectangle(true);
             yearChartPanel.setMouseWheelEnabled(true);
             yearChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
             yearChartPanel.addChartMouseListener(new ChartMouseListener() {
                     public void chartMouseMoved(ChartMouseEvent cme) { }
                     public void chartMouseClicked(ChartMouseEvent cme) {
                         ChartEntity entity = cme.getEntity();
                         if(entity != null && entity instanceof XYItemEntity) {
                             Calendar d = (Calendar)date.clone();
                             if(detailedButton.isSelected()) {
                                 d.set(Calendar.DAY_OF_YEAR, (Integer)(((XYItemEntity)entity).getItem() + 1));
                                 new PVGraph(d, DAY_VIEW_INDEX);
                             }
                             else {
                                 d.set(Calendar.MONTH, (Integer)((XYItemEntity)entity).getItem());
                                 new PVGraph(d, MONTH_VIEW_INDEX);
                             }
                         }
                     }
             });
             yearPanel.add(yearChartPanel);
             
             detailedButton = new JRadioButton("Detailed");
             detailedButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         updateChart();
                     }
             });
             
             JButton yearDecButton = new JButton("-");
             yearDecButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, -1);
                         updateChart();
                     }
             });
             JButton yearIncButton = new JButton("+");
             yearIncButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                         date.add(Calendar.YEAR, 1);
                         updateChart();
                     }
             });
             
             JPanel buttonsPanel = new JPanel();
             buttonsPanel.setBorder(new EtchedBorder());
             yearPanel.add(buttonsPanel);
             
             buttonsPanel.add(detailedButton);
             JPanel yearButtonsPanel = new JPanel();
             yearButtonsPanel.setBorder(new EtchedBorder());
             yearButtonsPanel.add(new JLabel("Year"));
             yearButtonsPanel.add(yearDecButton);
             yearButtonsPanel.add(yearIncButton);
             buttonsPanel.add(yearButtonsPanel);
             
             buttonsPanel.add(makeCommonButtonsPanel(this));
             
             yearPanel.addComponentListener(new ComponentAdapter() {
                     public void componentShown(ComponentEvent ce) {
                         updateChart();
                     }
             });
             
             return yearPanel;
         }
         
         private JFreeChart createChart(boolean detailed) {
             
             int year = date.get(Calendar.YEAR);
             
             java.util.List<PeriodData> periodData = getYearData(year, detailed);
             
             TimeSeriesCollection dataset = new TimeSeriesCollection();
             double totalPeriodPower = 0;
             
             int indexOfLastNonZeroPower = 0;
             for(PeriodData pd : periodData) {
                 TimeSeries s = new TimeSeries(pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : ""));
                 dataset.addSeries(s);
                 double lastPower = pd.startTotalPower;
                 for(int i = 0; i < (detailed? 365 : 12); ++i) {
                     double power = 0;
                     if(pd.powers[i] != 0) {
                         power = pd.powers[i] - lastPower;
                         lastPower = pd.powers[i];
                         if(i > indexOfLastNonZeroPower)
                             indexOfLastNonZeroPower = i;
                     }
                     if(detailed) {
                         GregorianCalendar gc = new GregorianCalendar();
                         gc.set(Calendar.DAY_OF_YEAR, i + 1);
                         s.add(new Day(gc.getTime()), power);
                     }
                     else {
                         s.add(new Month(i + 1, year), power);
                     }
                 }
                 totalPeriodPower += pd.endTotalPower - pd.startTotalPower;
             }
             
             String periodPower = totalPeriodPower < 1.0? String.format("%d WH", (int)(totalPeriodPower * 1000)) : (totalPeriodPower > 1000? String.format("%.3f MWH", totalPeriodPower / 1000.0) : String.format("%d KWH", (int)(totalPeriodPower + 0.5)));
             double avg = totalPeriodPower / (indexOfLastNonZeroPower + 1);
             if(detailed) {
                 periodPower += "      " + (avg < 1.0? String.format("%d WH/Day", (int)(avg * 1000)) : String.format("%.2f KWH/Day", avg));
             }
             else {
                 periodPower += "      " + (avg < 1.0? String.format("%d WH/Month", (int)(avg * 1000)) : String.format("%.2f KWH/Month", avg));
             }
             
             JFreeChart chart = ChartFactory.createXYAreaChart(
                 year + "      " + periodPower, // title
                 (detailed? "Day" : "Month"),      // x-axis label
                 "KWH",       // y-axis label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             String chartColour = props.getProperty("colour.chart", "WHITE");
             if(chartColour != null)
                 chart.setBackgroundPaint(getColour(chartColour));
             
             XYPlot plot = (XYPlot) chart.getPlot();
             if(periodData.size() == 1)
                 plot.setForegroundAlpha(1.0f);
             String backgroundColour = props.getProperty("colour.background", "LIGHT_GRAY");
             if(backgroundColour != null)
                 plot.setBackgroundPaint(getColour(backgroundColour));
             String gridlineColour = props.getProperty("colour.gridline", "WHITE");
             if(gridlineColour != null) {
                 Color glc = getColour(gridlineColour);
                 plot.setDomainGridlinePaint(glc);
                 plot.setRangeGridlinePaint(glc);
             }
             plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
             /*
             plot.setDomainCrosshairVisible(true);
             plot.setRangeCrosshairVisible(true);
             */
             plot.setDomainPannable(true);
             plot.setRangePannable(true);
             double maxPower = Double.parseDouble(props.getProperty("maxpower.year", "0"));
             if(maxPower > 0) {
                 ValueAxis powerAxis = plot.getRangeAxis();
                 powerAxis.setAutoRange(false);
                 powerAxis.setLowerBound(0.0);
                 powerAxis.setUpperBound(maxPower);
             }
             
             XYItemRenderer r = plot.getRenderer();
             r.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
             for(int i = 0; i < periodData.size(); ++i) {
                 PeriodData pd = periodData.get(i);
                 String colour = props.getProperty("colour.plot." + pd.serial, props.getProperty("colour.plot", null));
                 if(colour == null) {
                     // maintain backwards compatibility
                     colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
                 }
                 if(colour != null)
                     r.setSeriesPaint(i, getColour(colour));
             }
             
             DateAxis axis = new DateAxis();
             axis.setLabel(plot.getDomainAxis().getLabel());
             if(detailed)
                 axis.setDateFormatOverride(new SimpleDateFormat("MMM:d"));
             else
                 axis.setDateFormatOverride(new SimpleDateFormat("MMMM"));
             plot.setDomainAxis(axis);
             
             return chart;
         }
     }
   
     private class YearsView implements PVGraphView {
 
         ChartPanel yearsChartPanel;
 
         public String getTabLabel() {
             return "Years";
         }
         
         public void setWindowTitle() {
             setTitle(WINDOW_TITLE_PREFIX);
         }
         
         public void updateChart() {
             //System.out.println("Updating years view for " + date.getTime());
             yearsChartPanel.setChart(createChart());
             setWindowTitle();
         }
         
         public boolean handleKey(int charCode) {
             return false;
         }
 
         public JPanel makePanel() {
             
             JPanel yearsPanel = new JPanel();
             yearsPanel.setLayout(new BoxLayout(yearsPanel, BoxLayout.Y_AXIS));
             
             yearsChartPanel = new ChartPanel(null);
             yearsChartPanel.setFillZoomRectangle(true);
             yearsChartPanel.setMouseWheelEnabled(true);
             yearsChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
             yearsChartPanel.addChartMouseListener(new ChartMouseListener() {
                     public void chartMouseMoved(ChartMouseEvent cme) { }
                     public void chartMouseClicked(ChartMouseEvent cme) {
                         ChartEntity entity = cme.getEntity();
                         if(entity != null && entity instanceof CategoryItemEntity) {
                             Calendar d = (Calendar)date.clone();
                             d.set(Calendar.YEAR, (Integer)((CategoryItemEntity)entity).getColumnKey());
                             new PVGraph(d, YEAR_VIEW_INDEX);
                         }
                     }
             });
             yearsPanel.add(yearsChartPanel);
                                     
             JPanel buttonsPanel = new JPanel();
             buttonsPanel.setBorder(new EtchedBorder());
             yearsPanel.add(buttonsPanel);
                         
             buttonsPanel.add(makeCommonButtonsPanel(this));
             
             yearsPanel.addComponentListener(new ComponentAdapter() {
                     public void componentShown(ComponentEvent ce) {
                         updateChart();
                     }
             });
             
             return yearsPanel;
         }
         
         private JFreeChart createChart() {
                         
             java.util.List<YearsData> yearsData = getYearsData();
             
             DefaultCategoryDataset dataset = new DefaultCategoryDataset();
             double totalPeriodPower = 0;
             
             for(YearsData yd : yearsData) {
                 String series = yd.inverter + (yearsData.size() > 1? ("-" + yd.serial) : "");
                 double lastPower = yd.startTotalPower;
                 int lastYear = 0;
                 for(Integer year : yd.powers.keySet()) {
                     double power = yd.powers.get(year);
                     dataset.addValue(power - lastPower, series, year);
                     lastPower = power;
                     if(year > lastYear)
                         lastYear = year;
                 }
                 // avoid "fat bars" when we only have data for a small number of years
                 for(int i = 0; (i + yd.powers.size()) < 10; ++i)
                     dataset.addValue(0, series, new Integer(lastYear + i + 1));
                 totalPeriodPower += yd.endTotalPower - yd.startTotalPower;
             }
             
             String periodPower = totalPeriodPower < 1.0? String.format("%d WH", (int)(totalPeriodPower * 1000)) : (totalPeriodPower > 1000? String.format("%.3f MWH", totalPeriodPower / 1000.0) : String.format("%d KWH", (int)(totalPeriodPower + 0.5)));
             
             JFreeChart chart = ChartFactory.createBarChart(
                 periodPower, // title
                 "Year",      // domain label
                 "KWH",       // range label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             String chartColour = props.getProperty("colour.chart", "WHITE");
             if(chartColour != null)
                 chart.setBackgroundPaint(getColour(chartColour));
             
             CategoryPlot plot = (CategoryPlot)chart.getPlot();
             String backgroundColour = props.getProperty("colour.background", "LIGHT_GRAY");
             if(backgroundColour != null)
                 plot.setBackgroundPaint(getColour(backgroundColour));
             String gridlineColour = props.getProperty("colour.gridline", "WHITE");
             if(gridlineColour != null) {
                 Color glc = getColour(gridlineColour);
                 plot.setDomainGridlinePaint(glc);
                 plot.setRangeGridlinePaint(glc);
             }
             plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
             //plot.setDomainCrosshairVisible(true);
             //plot.setRangeCrosshairVisible(true);
             double maxPower = Double.parseDouble(props.getProperty("maxpower.years", "0"));
             if(maxPower > 0) {
                 ValueAxis powerAxis = plot.getRangeAxis();
                 powerAxis.setAutoRange(false);
                 powerAxis.setLowerBound(0.0);
                 powerAxis.setUpperBound(maxPower);
             }
             
             CategoryItemRenderer r = plot.getRenderer();
             for(int i = 0; i < yearsData.size(); ++i) {
                 YearsData yd = yearsData.get(i);
                 String colour = props.getProperty("colour.plot." + yd.serial, props.getProperty("colour.plot", null));
                 if(colour == null) {
                     // maintain backwards compatibility
                     colour = props.getProperty("plotcolour." + yd.serial, props.getProperty("plotcolour", null));
                 }
                 if(colour != null)
                     r.setSeriesPaint(i, getColour(colour));
             }
             return chart;
         }
     }
 
     public void windowClosing(java.awt.event.WindowEvent event) {
         synchronized(graphs) {
             graphs.remove(this);
             if(graphs.size() == 0) {
                 try {
                     conn.close();
                     System.out.println("Database connection terminated");
                 }
                 catch(Exception e) {
                     // relax
                 }
                 // this kills the application
                 super.windowClosing(event);
             }
         }
     }
     
     public java.util.List<DayData> getDayData(int year, int month, int day) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and dayofmonth(DateTime) = " + day + " order by DateTime";
         Map<String, DayData> result = new HashMap<String, DayData>();
         try {
             getDatabaseConnection();
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 DayData dd = result.get(serial);
                 if(dd == null) {
                     dd = new DayData();
                     dd.serial = serial;
                     dd.inverter = rs.getString("inverter");
                     dd.startTotalPower = rs.getDouble("ETotalToday");
                     result.put(serial, dd);
                 }
                 dd.times.add(rs.getTimestamp("DateTime"));
                 dd.powers.add(rs.getInt("CurrentPower"));
                 dd.endTotalPower = rs.getDouble("ETotalToday");
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<DayData>(result.values());
     }
     
     public java.util.List<PeriodData> getMonthData(int year, int month) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and CurrentPower != 0 order by DateTime";
         Map<String, PeriodData> result = new HashMap<String, PeriodData>();
         GregorianCalendar gc = new GregorianCalendar();
         try {
             getDatabaseConnection();
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 PeriodData pd = result.get(serial);
                 if(pd == null) {
                     pd = new PeriodData();
                     pd.serial = serial;
                     pd.inverter = rs.getString("inverter");
                     pd.startTotalPower = rs.getDouble("ETotalToday");
                     gc.setTime(rs.getTimestamp("DateTime"));
                     gc.set(Calendar.DAY_OF_MONTH, 1);
                     gc.add(Calendar.MONTH, 1);
                     gc.add(Calendar.DAY_OF_MONTH, -1);
                     pd.numPowers = gc.get(Calendar.DAY_OF_MONTH);
                     result.put(serial, pd);
                 }
                 double power = rs.getDouble("ETotalToday");
                 gc.setTime(rs.getTimestamp("DateTime"));
                 pd.powers[gc.get(Calendar.DAY_OF_MONTH) - 1] =  power;
                 pd.endTotalPower = power;
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<PeriodData>(result.values());
     }
     
     public java.util.List<PeriodData> getYearData(int year, boolean detailed) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and CurrentPower != 0 order by DateTime";
         Map<String, PeriodData> result = new HashMap<String, PeriodData>();
         GregorianCalendar gc = new GregorianCalendar();
         try {
             getDatabaseConnection();
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 PeriodData pd = result.get(serial);
                 if(pd == null) {
                     pd = new PeriodData();
                     pd.serial = serial;
                     pd.inverter = rs.getString("inverter");
                     pd.startTotalPower = rs.getDouble("ETotalToday");
                     result.put(serial, pd);
                 }
                 gc.setTime(rs.getTimestamp("DateTime"));
                 if(detailed)
                     pd.numPowers = gc.get(Calendar.DAY_OF_YEAR);
                 else
                     pd.numPowers = gc.get(Calendar.MONTH) + 1;
                 double power = rs.getDouble("ETotalToday");
                 pd.powers[pd.numPowers - 1] =  power;
                 pd.endTotalPower = power;
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<PeriodData>(result.values());
     }
 
     public java.util.List<YearsData> getYearsData() {
         Statement stmt = null;
         String query = "select * from DayData where CurrentPower != 0 order by DateTime";
         Map<String, YearsData> result = new HashMap<String, YearsData>();
         GregorianCalendar gc = new GregorianCalendar();
         try {
             getDatabaseConnection();
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 YearsData yd = result.get(serial);
                 if(yd == null) {
                     yd = new YearsData();
                     yd.serial = serial;
                     yd.inverter = rs.getString("inverter");
                     yd.startTotalPower = rs.getDouble("ETotalToday");
                     result.put(serial, yd);
                 }
                 gc.setTime(rs.getTimestamp("DateTime"));
                 int year = gc.get(Calendar.YEAR);
                 double totalPower = rs.getDouble("ETotalToday");
                 yd.powers.put(year, totalPower);
                 yd.endTotalPower = totalPower;
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<YearsData>(result.values());
     }
     
     private static void runSmatool() throws IOException {
         String cmd = props.getProperty("smatool.cmd", "smatool");
         System.out.println("Executing " + cmd + " at " + new java.util.Date());
         Process p = Runtime.getRuntime().exec(cmd);
         if(Integer.decode(props.getProperty("smatool.printstdout", "0")) != 0) {
             BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line;
             while((line = br.readLine()) != null) {
                 System.out.println(line);
             }
         }
         if(Integer.decode(props.getProperty("smatool.printstderr", "1")) != 0) {
             BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             String line;
             while((line = br.readLine()) != null) {
                 System.err.println(line);
             }
         }
         try {
             p.waitFor();
         }
         catch (InterruptedException ie) {
             // relax
         }
     }
     
     private static void getDatabaseConnection() throws SQLException {
         if(conn == null || !conn.isValid(30)) {
             String user = props.getProperty("mysql.user", props.getProperty("user"));
             String password = props.getProperty("mysql.password", props.getProperty("password"));
             String url = props.getProperty("mysql.url", props.getProperty("url"));
             conn = DriverManager.getConnection(url, user, password);
             System.out.println ("Database connection established");
         }
     }
 
     public static Color getColour(String colourName) {
         try {
             return (Color)Class.forName("org.jfree.chart.ChartColor").getField(colourName.toUpperCase()).get(null);
         }
         catch (NoSuchFieldException nsfe) {
             try {
                 return Color.decode(colourName);
             }
             catch (NumberFormatException nfe) {
                 System.err.println("Bad colour (should be colour name or hex RGB): " + colourName);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static boolean loadProperties() {
         File propsFile = new File("pvgraph.properties");
         if(props == null || propsFile.lastModified() > propsLastLoadedAt) {
             propsLastLoadedAt = System.currentTimeMillis();
             props = new Properties(System.getProperties());
             if(propsFile.canRead()) {
                 try {
                    FileInputStream propsInputStream = new FileInputStream(propsFile);
                    props.load(propsInputStream);
                    propsInputStream.close();
                 }
                 catch (IOException ioe) {
                     // relax
                 }
             }
             return true;
         }
         return false;
     }
 
     public static void main (String[] args) {
         loadProperties();
         try {
             Class.forName ("com.mysql.jdbc.Driver").newInstance();
             getDatabaseConnection();
         }
         catch (SQLException e) {
             System.err.println("Cannot establish database connection: " + e.getMessage());
         }
         catch (Exception e) {
             System.err.println(e.getMessage());
         }
         if(conn != null) {
             // create first window
             new PVGraph();
             int smatoolPeriod = Integer.decode(props.getProperty("smatool.period", "0"));
             while(smatoolPeriod > 0) {
                 loadProperties();
                 smatoolPeriod = Integer.decode(props.getProperty("smatool.period", "0"));
                 int smatoolStartHour = Integer.decode(props.getProperty("smatool.starthour", "0"));
                 int smatoolEndHour = Integer.decode(props.getProperty("smatool.endhour", "24"));
                 GregorianCalendar now = new GregorianCalendar();
                 int nowHour = now.get(Calendar.HOUR_OF_DAY);
                 if(nowHour >= smatoolStartHour && nowHour < smatoolEndHour) {
                     try {
                         runSmatool();
                         synchronized (graphs) {
                             for(PVGraph g : graphs)
                                 g.updateView();
                         }
                     }
                     catch (IOException ioe) {
                         System.err.println(ioe.getMessage());
                     }
                 }
                 try {
                     Thread.sleep(smatoolPeriod * 60 * 1000);
                 }
                 catch (InterruptedException ie) {
                     // break;
                 }
             }
         }
     }
 }
