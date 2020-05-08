 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.gui.impl.servlets;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CombinedDomainXYPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.monitor.MonitorAdmin;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.MonitoringJob;
 import org.osgi.service.monitor.StatusVariable;
 
 import com.sun.corba.se.spi.orb.DataCollector;
 
 public class ChartServlet extends HttpServlet implements EventHandler, ServiceListener {
 	private static final long serialVersionUID = 1L;	
 	
 	/**
 	 * Memory used by the java runtime
 	 */
 	private static final String TSERIES_MEMORY_USAGE = "java.lang.runtime/memory.used";		
 	
 	/**
 	 * Current size of the lucene index
 	 * @see org.paxle.se.index.IIndexSearcher#getDocCount()
 	 */
 	public static final String TSERIES_INDEX_SIZE = "org.paxle.lucene-db/docs.known";
 	
 	/**
 	 * Current free diskspace 
 	 */
 	public static final String TSERIES_DISK_USAGE = "os.disk/disk.space.free";
 	
 	/**
 	 * Current PPM of the crawler
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_CRAWLER = "org.paxle.crawler/ppm";
 	
 	/**
 	 * Current PPM of the parser
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_PARSER = "org.paxle.parser/ppm";
 	
 	/**
 	 * Current PPM of the indexer
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_INDEXER = "org.paxle.indexer/ppm";	
 	
 	/**
 	 * Total CPU Usage
 	 */
 	public static final String TSERIES_CPU_TOTAL = "os.usage.cpu/cpu.usage.total";
 	
 	/**
 	 * User CPU Usage
 	 */
 	public static final String TSERIES_CPU_USER = "os.usage.cpu/cpu.usage.user";
 	
 	/**
 	 * System CPU Usage
 	 */
 	public static final String TSERIES_CPU_SYSTEM = "os.usage.cpu/cpu.usage.system";
 
 	/**
 	 * An arraylist containing all full-path names of all {@link StatusVariable variables}
 	 * to monitor
 	 */
 	public static final String[] TSERIES = new String[] {
 		TSERIES_MEMORY_USAGE,
 		TSERIES_INDEX_SIZE,
 		TSERIES_DISK_USAGE,
 		TSERIES_PPM_CRAWLER,
 		TSERIES_PPM_PARSER,
 		TSERIES_PPM_INDEXER,
 		TSERIES_CPU_TOTAL,
 		TSERIES_CPU_USER,
 		TSERIES_CPU_SYSTEM
 	};
 	
 	private final HashMap<String,HashSet<String>> variableTree;	
 
 	/**
 	 * An OSGi bundle-context used to access other services registered to the system
 	 */
 	private final BundleContext context;
 	
 	/**
 	 * Currently active monitoring job
 	 */
 	private MonitoringJob currentMonitorJob;
 	
 	/**
 	 * OSGI Monitor Admin Service
 	 */
 	private final MonitorAdmin monitorService;
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A map containing all {@link TimeSeries} that are filled by the {@link DataCollector}-thread
 	 */
 	private HashMap<String,TimeSeries> seriesMap = new HashMap<String,TimeSeries>();
 
 	/**
 	 * A map containing all available {@link JFreeChart charts} as their naomes 
 	 */
 	private HashMap<String, JFreeChart> chartMap = new HashMap<String, JFreeChart>();
 	
 	public ChartServlet(BundleContext context, MonitorAdmin monitorService) {
 		this.context = context;
 		this.monitorService = monitorService;
 		
 		this.variableTree = new HashMap<String, HashSet<String>>();
 		this.buildVariableTree();
 		
 		// create charts
 		this.chartMap.put("mem", this.createMemoryChart());
 		this.chartMap.put("ppm", this.createPPMChart());
 		this.chartMap.put("index", this.createIndexChart());
 		this.chartMap.put("system" ,this.createCPUChart());		
 		
 		// registering servlet as event-handler: required to receive monitoring-events
 		Dictionary<String,Object> properties = new Hashtable<String,Object>();
 		properties.put(EventConstants.EVENT_TOPIC, new String[]{"org/osgi/service/monitor"});
 		properties.put(EventConstants.EVENT_FILTER, String.format("(mon.listener.id=%s)",ChartServlet.class.getName()));
 		this.context.registerService(EventHandler.class.getName(), this, properties);						
 		
 		try {
 			// detecting already registered monitorables and
 			// determine which of their variables we need to monitor 
 			final HashSet<String> variableNames = new HashSet<String>();
 			final ServiceReference[] services = context.getServiceReferences(Monitorable.class.getName(), null);
 			if (services != null) {
 				for (ServiceReference reference : services) {
 					this.addVariables4Monitor(reference, variableNames);
 				}
 				this.startScheduledJob(variableNames);
 			}
 			
 			// registering this class as service-listener
 			this.context.addServiceListener(this, String.format("(%s=%s)",Constants.OBJECTCLASS, Monitorable.class.getName()));			
 		} catch (InvalidSyntaxException e) {
 			// this should not occur
 			this.logger.error(e);
 		}
 	}
 		
 	private void buildVariableTree() {
 		for (String fullPath : TSERIES) {
 			int idx = fullPath.indexOf('/');
 			String monitorableId = fullPath.substring(0, idx);
 			String variableId = fullPath.substring(idx+1);
 			
 			HashSet<String> variableIds;
 			if (variableTree.containsKey(monitorableId)) {
 				variableIds = variableTree.get(monitorableId);
 			} else {
 				variableIds = new HashSet<String>();
 				variableTree.put(monitorableId, variableIds);
 			}
 				
 			variableIds.add(variableId);
 		}
 	}	
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 	}
 	
 	@Override
 	public void destroy() {	
 		// clear series map
 		this.seriesMap.clear();
 		
 		super.destroy();
 	}
 	
 	private JFreeChart createPPMChart() {
 		/*
 		 * INIT TIME-SERIES 
 		 */
         final TimeSeriesCollection dataset = new TimeSeriesCollection();
         
         TimeSeries crawlerPPM = new TimeSeries("Crawler PPM", Minute.class);
         crawlerPPM.setMaximumItemAge(24*60);
         dataset.addSeries(crawlerPPM);
         this.seriesMap.put(TSERIES_PPM_CRAWLER, crawlerPPM);
 
         TimeSeries parserPPM = new TimeSeries("Parser PPM", Minute.class);
         parserPPM.setMaximumItemAge(24*60);
         dataset.addSeries(parserPPM);
         this.seriesMap.put(TSERIES_PPM_PARSER, parserPPM);
         
         TimeSeries indexerPPM = new TimeSeries("Indexer PPM", Minute.class);
         indexerPPM.setMaximumItemAge(24*60);
         dataset.addSeries(indexerPPM);
         this.seriesMap.put(TSERIES_PPM_INDEXER, indexerPPM);
         
 		/*
 		 * INIT CHART
 		 */        
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "PPM",
                 dataset,
                 true,
                 true,
                 false
             );
         
         // change axis data format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;        
 	}
 	
 	private JFreeChart createCPUChart() {
 		final TimeSeriesCollection dataset = new TimeSeriesCollection();
 		
         TimeSeries series = null;
 
     	series = new TimeSeries("Total CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_TOTAL, series);
         
     	series = new TimeSeries("User CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_USER, series);
         
     	series = new TimeSeries("System CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_SYSTEM, series);           
 		
 		// init chart
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "Usage",
                 dataset,
                 true,
                 true,
                 false
             );
         
         // change axis data format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.getXYPlot().getRangeAxis().setRange(0, 1);
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;   
 	}
 	
 	private JFreeChart createIndexChart() {
 		// init Time-Series
 		TimeSeries indexSizeSeries = new TimeSeries("Index Size", Minute.class);
 		indexSizeSeries.setMaximumItemAge(24*60);
 		this.seriesMap.put(TSERIES_INDEX_SIZE, indexSizeSeries);
 		
 		// init chart
 		JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "#Docs",
                 new TimeSeriesCollection(indexSizeSeries),
                 true,
                 true,
                 false
             );
 		
 		// change axis date format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;
 	}
 
 	private JFreeChart createMemoryChart() {
     	// init time series
     	TimeSeries usedmemSeries = new TimeSeries("Used MEM", Minute.class);
     	usedmemSeries.setMaximumItemAge(24*60);
     	this.seriesMap.put(TSERIES_MEMORY_USAGE, usedmemSeries);
     	
     	TimeSeries freeDiskSeries = new TimeSeries("Free Disk", Minute.class);
     	freeDiskSeries.setMaximumItemAge(24*60);
     	this.seriesMap.put(TSERIES_DISK_USAGE, freeDiskSeries);
 		
 		// init collections and chart
         final TimeSeriesCollection dataset = new TimeSeriesCollection();
         dataset.addSeries(usedmemSeries);
         
         // create subplot 1...
         long maxMemory = Runtime.getRuntime().maxMemory();
         NumberAxis memYAxis = new NumberAxis("Memory");
         if (maxMemory != Long.MAX_VALUE) {
         	memYAxis.setRange(0, maxMemory / (1024*1024));
         }
         final XYPlot subplot1 = new XYPlot(dataset, null, memYAxis, new StandardXYItemRenderer());
         subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);        
         
         // create subplot 2...
         final XYPlot subplot2 = new XYPlot(new TimeSeriesCollection(freeDiskSeries), null, new NumberAxis("Disk"), new StandardXYItemRenderer());
         subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
 
         // parent plot...
         DateAxis dateaxis = new DateAxis("Time");
         dateaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
         final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dateaxis);
         plot.setBackgroundPaint(Color.white);
         plot.setGap(10.0);
         
         // add the subplots...
         plot.add(subplot1, 1);
         plot.add(subplot2, 1);
         plot.setOrientation(PlotOrientation.VERTICAL);        
         
         JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
         chart.setBackgroundPaint(Color.WHITE);
         return chart;
 	}
 	
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
 	 */
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String wStrg = request.getParameter("w");
 		int width = Integer.valueOf(wStrg == null ? "385": wStrg);
 		
 		String hStrg = request.getParameter("h");
 		int height = Integer.valueOf(hStrg == null ? "200": hStrg);
 		
 		// getting the reuested chart
 		String type = request.getParameter("t");
 		JFreeChart chart = this.chartMap.get(type);
 		if (chart == null) {
 			response.setStatus(404);
 			return;
 		}
 				
 		// set response type
 		response.setContentType("image/png");		
 		
 		// render image
 		ServletOutputStream out = response.getOutputStream();
 		ChartUtilities.writeChartAsPNG(out, chart, width, height);
 		out.flush();
 	}
 
 	/**
 	 * @see EventHandler#handleEvent(Event)
 	 */
 	public void handleEvent(Event event) {
 		String pid = (String) event.getProperty("mon.monitorable.pid");
 		String name = (String) event.getProperty("mon.statusvariable.name");
 		Object value = event.getProperty("mon.statusvariable.value");
 		
 		String fullPath = pid + "/" + name;
 		TimeSeries series = this.seriesMap.get(fullPath); 
 		if (series != null) {
 			if (value instanceof Number) {
 				Number num = (Number)value;
 				
 				if (fullPath.equalsIgnoreCase(TSERIES_MEMORY_USAGE)) {
 					num = new Integer(num.intValue() / ( 1024 * 1024));
 				}
 				
 				series.addOrUpdate(new Minute(new Date()), num);
 			} else {
 				this.logger.warn(String.format(
 						"Unexpected type of monitoring variable '%s/%s': %s",
 						value.getClass().getSimpleName(),
 						pid,
 						name
 				));
 			}
 		}
 	}
 
 	/**
 	 * @see ServiceListener#serviceChanged(ServiceEvent)
 	 */
 	public void serviceChanged(ServiceEvent event) {
 		// getting the service reference
 		ServiceReference reference = event.getServiceReference();
 		this.serviceChanged(reference, event.getType());
 	}		
 	
 	private void serviceChanged(ServiceReference reference, int eventType) {
 		if (reference == null) return;
 		if (eventType == ServiceEvent.MODIFIED) return;
 		
 		// ignoring unknown services
 		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
 		if (!variableTree.containsKey(pid)) return;
 		
 		// getting currently monitored variables
 		final HashSet<String> currentVariableNames = new HashSet<String>();
 		if (this.currentMonitorJob != null) {
 			String[] temp = this.currentMonitorJob.getStatusVariableNames();
 			if (temp != null) {
 				currentVariableNames.addAll(Arrays.asList(temp));
 			}
 			
			// stopping old monitoring-job
			this.currentMonitorJob.stop();
 			this.currentMonitorJob = null;
 		}		
 		
 		// getting variables of changed service
 		final HashSet<String> diffVariableNames = new HashSet<String>();
 		this.addVariables4Monitor(reference, diffVariableNames);
 		
 		if (eventType == ServiceEvent.REGISTERED) {			
 			// adding new variable
 			currentVariableNames.addAll(diffVariableNames);
 		} else if (eventType == ServiceEvent.UNREGISTERING) {
 			currentVariableNames.removeAll(diffVariableNames);
 		}
 		
 		// restarting monitoring job
 		this.startScheduledJob(currentVariableNames);
 	}
 	
 	/**
 	 * Add the full-path of all {@link StatusVariable variables} of the given {@link Monitorable} into the set
 	 * @param reference a reference to a {@link Monitorable}
 	 * @param variableNames the set where the variable-names should be appended
 	 */
 	private void addVariables4Monitor(ServiceReference reference, HashSet<String> variableNames) {
 		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
 		if (!variableTree.containsKey(pid)) return;
 		
 		for (String name : variableTree.get(pid)) {
 			variableNames.add(pid + "/" + name);
 		}
 	}
 	
 	/**
 	 * Starting a new monitoring job with the given variables to monitor
 	 * @param variableNames full-path of the {@link StatusVariable variables} to monitor
 	 */
 	private void startScheduledJob(Set<String> variableNames) {
 		if (variableNames.size() == 0) return;
 		this.currentMonitorJob = this.monitorService.startScheduledJob(
 				ChartServlet.class.getName(), // listener.id
 				variableNames.toArray(new String[variableNames.size()]),
 				60, // seconds
 				0   // Forever
 		);		
 	}
 }
