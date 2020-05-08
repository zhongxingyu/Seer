 package main.java;
 
 import javax.jms.Connection;
 import javax.jms.Destination;
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Scanner;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CombinedDomainCategoryPlot;
 import org.jfree.chart.plot.CombinedDomainXYPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.ui.RectangleInsets;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 
//import com.sun.jmx.snmp.Timestamp;
 
 public class FrontEnd extends JPanel{
 	
 	private static MonitorClient monitorClient;
 	
 	private TimeSeries total;
 	private TimeSeries used;
 	private TimeSeries cputime;
 	
 	private static long usedmem;
 	private static long totalmem;
 	private static long cpu;
 	private static java.sql.Timestamp lastReadTime;
 	
 	public FrontEnd(int maxAge) {
 		
 		super(new BorderLayout());
 		this.total = new TimeSeries("Total Memory", Millisecond.class);
 		this.total.setMaximumItemAge(maxAge);
 		this.used = new TimeSeries("Used Memory", Millisecond.class);
 		this.used.setMaximumItemAge(maxAge);
 		this.cputime = new TimeSeries("Cpu %", Millisecond.class);
 		this.cputime.setMaximumItemAge(maxAge);
 
 		TimeSeriesCollection dataset1 = new TimeSeriesCollection();
 		dataset1.addSeries(this.total);
 		dataset1.addSeries(this.used);
 		
 		TimeSeriesCollection dataset2 = new TimeSeriesCollection();
 		dataset2.addSeries(this.cputime);
 		
 		DateAxis domain1 = new DateAxis("Time");
 		NumberAxis range1 = new NumberAxis("Memory");
 		domain1.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		range1.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
 		domain1.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		range1.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 
 		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
 		renderer.setSeriesPaint(0, Color.red);
 		renderer.setSeriesPaint(1, Color.blue);
 		renderer.setBaseStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
 		
		DateAxis time = new DateAxis("Time");
		time.setStandardTickUnits(DateAxis.createStandardDateTickUnits());
 		time.setFixedAutoRange(300000);
 		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(time);
 		plot.setGap(10.0);
 		plot.setOrientation(PlotOrientation.VERTICAL);
 			
 		XYPlot plot1 = new XYPlot(dataset1, domain1, range1, renderer);
 		plot1.setBackgroundPaint(Color.black);
 		plot1.setDomainGridlinePaint(Color.white);
 		plot1.setRangeGridlinePaint(Color.white);
 		plot1.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
 		domain1.setAutoRange(true);
 		domain1.setLowerMargin(0.0);
 		domain1.setUpperMargin(0.0);
 		domain1.setTickLabelsVisible(true);
 		range1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 		plot.add(plot1);
 		
 		DateAxis domain2 = new DateAxis("Time");
 		NumberAxis range2 = new NumberAxis("Cpu");
 		domain2.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		range2.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
 		domain2.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		range2.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
 		renderer2.setSeriesPaint(0, Color.green);
 		renderer2.setBaseStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
 		range2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 		XYPlot plot2 = new XYPlot(dataset2, domain2, range2, renderer2);
 		plot2.setBackgroundPaint(Color.black);
 		plot2.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
 		domain2.setAutoRange(true);
 		domain2.setLowerMargin(0.0);
 		domain2.setUpperMargin(0.0);
 		domain2.setTickLabelsVisible(true);	
 		plot.add(plot2);
 			
 		JFreeChart chart = new JFreeChart("", new Font("SansSerif", Font.BOLD, 22), plot, true);
 		chart.setBackgroundPaint(Color.white);
 
 		ChartPanel chartPanel = new ChartPanel(chart);
 		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createLineBorder(Color.black)));
 		add(chartPanel);
 	}
 	
 	private void addTotalObservation(double y) {
 		this.total.add(new Millisecond(), y);
 	}
 
 
 	private void addUsedObservation(double y) {
 		this.used.add(new Millisecond(), y);
 	}
 	
 	private void addCpuObservation(double y){
 		this.cputime.add(new Millisecond(), y);
 	}
 	
 	class MemGenerator extends Timer implements ActionListener {
 
 		MemGenerator(int interval) {
 			super(interval, null);
 			addActionListener(this);
 		}
 
 		public void actionPerformed(ActionEvent event) {
 			HashMap<String, InfoPacket> ipStore = (HashMap<String, InfoPacket>) monitorClient.getmDaemon().getIpStore().clone();
 			monitorClient.getmDaemon().getIpStore().clear();
 			if(ipStore != null){
 				this.dataAverageUpdater(ipStore);
 			}else{
 				System.out.println("No data");
 			}			
 			if(usedmem != 0) {
 				addTotalObservation(totalmem);
 				addUsedObservation(usedmem);
 				addCpuObservation(cpu);
 			}
 		}
 		
 		public void dataAverageUpdater(HashMap<String, InfoPacket> ipStore){
 			long daemons = ipStore.keySet().size();
 
 			java.util.Date ts = new java.util.Date();
 			java.sql.Timestamp thisTime = new java.sql.Timestamp(ts.getTime());
 			java.sql.Timestamp testTime = new java.sql.Timestamp(ts.getTime() - (Long)MonitorConstants.SYS_MONITOR_DTIMEOUT);
 			if(lastReadTime == null){
 				lastReadTime = thisTime;
 			}
 			if(daemons == 0 && (lastReadTime.getTime() <= testTime.getTime())){
 				usedmem = -1;
 				totalmem= -1;
 				cpu = 0;
 				return;
 			}else if(daemons == 0){
 				return;
 			}
 			lastReadTime = thisTime;
 			long usedmemAvg = 0;
 			long totalmemAvg = 0;
 			long cpuAvg = 0;
 			
 		    for (InfoPacket curIp : ipStore.values()) {
 				usedmemAvg += curIp.getMemInfo().getUsed();
 				totalmemAvg += curIp.getMemInfo().getTotal();
 				long tmp = (long) (curIp.getCpuPerc().getCombined() * 100);
 				cpuAvg += tmp;
 		    }
 			usedmem = usedmemAvg/daemons;
 			totalmem = totalmemAvg/daemons;
 			cpu = cpuAvg/daemons;
 		}
 	}
 		
 	public static void main(String[] args) throws Exception {   
 		boolean running = true;
 		
 		Properties props = new Properties();
 		InputStream inStream = null;
 		try{
 			  inStream = System.class.getResourceAsStream("/main/resources/"+"config.properties");
 			  props.load(inStream);
 		}catch(FileNotFoundException e){
 			System.out.println("ERROR: Unable to find properties file.");
 			e.printStackTrace();
 		}catch(IOException e1){
 			System.out.println("ERROR: Unable to load properties file.");
 			e1.printStackTrace();			
 		}finally {
 			if( null != inStream ) try { inStream.close(); } catch( IOException e ) { /* .... */ }
 		}
 		monitorClient = new MonitorClient(props);
 		
 		JFrame frame = new JFrame("System Monitor");
 		FrontEnd panel = new FrontEnd(310000);
 		frame.getContentPane().add(panel, BorderLayout.CENTER);
 		frame.setBounds(200, 120, 1000, 500);
 		frame.setVisible(true);
 		panel.new MemGenerator(500).start();
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				monitorClient.terminateWorker();
 				System.exit(0);
 			}
 		});
 		
 		while(true){
 	        Thread.sleep(1000);
 	    }
 	}
 
 	public static void thread(Runnable runnable, boolean daemon) {
         Thread brokerThread = new Thread(runnable);
         brokerThread.setDaemon(daemon);
         brokerThread.start();
     }
 	
 	public static class Consumer implements Runnable, ExceptionListener {
         public void run() {
             try {
                 // Create a ConnectionFactory
                 ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://129.79.49.181:61616");
                 
                 // Create a Connection
                 Connection connection = connectionFactory.createConnection();
                 connection.start();
 
                 connection.setExceptionListener(this);
 
                 // Create a Session
                 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                 // Create the destination (Topic or Queue)
                 Destination destination = session.createQueue("G17_007.TOPIC");
 
                 // Create a MessageConsumer from the Session to the Topic or Queue
                 MessageConsumer consumer = session.createConsumer(destination);
 
                 // Wait for a message
                 Message message = consumer.receive(1000);
 
                 if(message == null){
                 	System.out.println("Nothing Received from Broker, skipping.");	
                 	usedmem = 0;	                	
                 }else if(message instanceof ObjectMessage){
                 	ObjectMessage obj = (ObjectMessage)message;
                 	InfoPacket ip = (InfoPacket)obj.getObject();
                     usedmem = ip.getMemInfo().getUsed();
                     totalmem = ip.getMemInfo().getTotal();
                     //cpu = (long) ip.getCpuPerc().getSys();
                 	System.out.println(ip.getReportString() + "***");
                 }else{
                 	usedmem = 0;
                 }
                 
                 // Clean up
                 consumer.close();
                 session.close();
                 connection.close();
                 
             } catch (Exception e) {
                 System.out.println("Caught: " + e);
                 e.printStackTrace();
             }
         }
 
 		@Override
 		public void onException(JMSException arg0) {
 			// TODO Auto-generated method stub			
 		}
 	}
 }
