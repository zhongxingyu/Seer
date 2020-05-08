 
 // compile: javac -cp "jfreechart-1.0.13/lib/*" PVGraph.java
 
 // run: java -cp ".:jfreechart-1.0.13/*:/usr/share/java/mysql-connector-java.jar" PVGraph
 
 import java.sql.*;
 import java.util.*;
 import java.io.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.text.SimpleDateFormat;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
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
     
     static LinkedList<PVGraph> graphs = new LinkedList<PVGraph>();
     
     private Connection conn;
     private Calendar date;
     private JTabbedPane tabPane;
     private boolean trackDay = true;
     
     private class DayData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
         java.util.List<Timestamp> times = new java.util.ArrayList<Timestamp>(12 * 24);
         java.util.List<Integer> powers = new java.util.ArrayList<Integer>(12 * 24);
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
     }
     
     public PVGraph(Connection conn) {
         super("PV Power");
         this.conn = conn;
         date = new GregorianCalendar();
         synchronized(graphs) {
             graphs.add(this);
         }
 
         DayView dayView = new DayView();
         MonthView monthView = new MonthView();
         YearView yearView = new YearView();
         
         tabPane = new JTabbedPane();
         tabPane.addTab("Day", dayView.makePanel());
         tabPane.addTab("Month", monthView.makePanel());
         tabPane.addTab("Year", yearView.makePanel());
         setContentPane(tabPane);
         pack();
         setVisible(true);
     }
     
     public void updateView() {
         if(trackDay)
             date = new GregorianCalendar();
         // FIXME - this kludge works but surely there is a better way
         Component selectedComponent = tabPane.getSelectedComponent();
         selectedComponent.setVisible(false);
         selectedComponent.setVisible(true);
     }
     
     private JPanel makeCommonButtonsPanel(final PVGraphView view) {
         JPanel commonButtonsPanel = new JPanel();
         commonButtonsPanel.setBorder(new EtchedBorder());
 
         JButton newGraphButton = new JButton("New Graph");
         newGraphButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     new PVGraph(conn);
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
         
         public void updateChart() {
             System.out.println("Updating day view for " + date.getTime());
             dayChartPanel.setChart(createChart());
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
                     s.add(new Minute(dd.times.get(i)), dd.powers.get(i));
                 dataset.addSeries(s);
                 totalDayPower += dd.endTotalPower - dd.startTotalPower;
             }
             
             String dayPower = totalDayPower < 1.0? String.format("%d W", (int)(totalDayPower * 1000)) : String.format("%.3f KW", totalDayPower);
             JFreeChart chart = ChartFactory.createXYAreaChart(
                 day + " / " + month + " / " + year + "      " + dayPower, // title
                 "Time",     // x-axis label
                 "Watts",    // y-axis label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             chart.setBackgroundPaint(Color.white);
             
             XYPlot plot = (XYPlot) chart.getPlot();
             if(dayData.size() == 1)
                 plot.setForegroundAlpha(1.0f);
             plot.setBackgroundPaint(Color.lightGray);
             plot.setDomainGridlinePaint(Color.white);
             plot.setRangeGridlinePaint(Color.white);
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
                 String colour = props.getProperty("plotcolour." + dd.serial, props.getProperty("plotcolour", null));
                 if(colour != null)
                     r.setSeriesPaint(i, new Color(Integer.decode(colour)));
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
         
         public void updateChart() {
             System.out.println("Updating month view for " + date.getTime());
             monthChartPanel.setChart(createChart());
         }
         
         public JPanel makePanel() {
             
             JPanel monthPanel = new JPanel();
             monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));
             
             monthChartPanel = new ChartPanel(null);
             monthChartPanel.setFillZoomRectangle(true);
             monthChartPanel.setMouseWheelEnabled(true);
             monthChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
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
             
             for(PeriodData pd : periodData) {
                 String series = pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : "");
                 double lastPower = pd.startTotalPower;
                 for(int i = 0; i < pd.numPowers; ++i) {
                     if(pd.powers[i] != 0) {
                         dataset.addValue(pd.powers[i] - lastPower, series, "" + (i + 1));
                         lastPower = pd.powers[i];
                     }
                     else
                         dataset.addValue(0, series, "" + (i + 1));
                 }
                 totalPeriodPower += pd.endTotalPower - pd.startTotalPower;
             }
             
             String periodPower = totalPeriodPower < 1.0? String.format("%d W", (int)(totalPeriodPower * 1000)) : String.format("%.3f KW", totalPeriodPower);
             
             JFreeChart chart = ChartFactory.createBarChart(
                 month + " / " + year + "      " + periodPower, // title
                 "Day",      // domain label
                 "KW",       // range label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             chart.setBackgroundPaint(Color.white);
             
             CategoryPlot plot = (CategoryPlot)chart.getPlot();
             plot.setBackgroundPaint(Color.lightGray);
             plot.setDomainGridlinePaint(Color.white);
             plot.setRangeGridlinePaint(Color.white);
             plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
             plot.setDomainCrosshairVisible(true);
             plot.setRangeCrosshairVisible(true);
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
                 String colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
                 if(colour != null)
                     r.setSeriesPaint(i, new Color(Integer.decode(colour)));
             }
             return chart;   
         }
     }
     
     private class YearView implements PVGraphView {
         
         ChartPanel yearChartPanel;
         JRadioButton detailedButton;
         
         public void updateChart() {
             System.out.println("Updating year view for " + date.getTime());
             yearChartPanel.setChart(createChart(detailedButton.isSelected()));
         }
         
         public JPanel makePanel() {
             
             JPanel yearPanel = new JPanel();
             yearPanel.setLayout(new BoxLayout(yearPanel, BoxLayout.Y_AXIS));
             
             yearChartPanel = new ChartPanel(null);
             yearChartPanel.setFillZoomRectangle(true);
             yearChartPanel.setMouseWheelEnabled(true);
             yearChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
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
             
             for(PeriodData pd : periodData) {
                 TimeSeries s = new TimeSeries(pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : ""));
                 dataset.addSeries(s);
                 double lastPower = pd.startTotalPower;
                 for(int i = 0; i < (detailed? 365 : 12); ++i) {
                     double power = 0;
                     if(pd.powers[i] != 0) {
                         power = pd.powers[i] - lastPower;
                         lastPower = pd.powers[i];
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
             
             String periodPower = totalPeriodPower < 1.0? String.format("%d W", (int)(totalPeriodPower * 1000)) : String.format("%.3f KW", totalPeriodPower);
             
             JFreeChart chart = ChartFactory.createXYAreaChart(
                 year + "      " + periodPower, // title
                 (detailed? "Day" : "Month"),      // x-axis label
                 "KW",       // y-axis label
                 dataset,    // data
                 PlotOrientation.VERTICAL,
                 true,       // create legend?
                 true,       // generate tooltips?
                 false       // generate URLs?
                 );
             
             chart.setBackgroundPaint(Color.white);
             
             XYPlot plot = (XYPlot) chart.getPlot();
             if(periodData.size() == 1)
                 plot.setForegroundAlpha(1.0f);
             plot.setBackgroundPaint(Color.lightGray);
             plot.setDomainGridlinePaint(Color.white);
             plot.setRangeGridlinePaint(Color.white);
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
                 String colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
                 if(colour != null)
                     r.setSeriesPaint(i, new Color(Integer.decode(colour)));
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
     
     public static void main (String[] args) {
         props = new Properties(System.getProperties());
         try {
             props.load(new FileInputStream("pvgraph.properties"));
         }
         catch (IOException ioe) {
             // relax
         }
         String user = props.getProperty("mysql.user", props.getProperty("user"));
         String password = props.getProperty("mysql.password", props.getProperty("password"));
         String url = props.getProperty("mysql.url", props.getProperty("url"));
         Connection conn = null;
         try {
             Class.forName ("com.mysql.jdbc.Driver").newInstance();
             conn = DriverManager.getConnection (url, user, password);
             System.out.println ("Database connection established");
             new PVGraph(conn);
             int smatoolPeriod = Integer.decode(props.getProperty("smatool.period", "0"));
             if(smatoolPeriod > 0) {
                 int smatoolStartHour = Integer.decode(props.getProperty("smatool.starthour", "0"));
                 int smatoolEndHour = Integer.decode(props.getProperty("smatool.endhour", "24"));
                 for(;;) {
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
         catch (SQLException e) {
             System.err.println("Cannot connect to " + url + ": " + e.getMessage());
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
