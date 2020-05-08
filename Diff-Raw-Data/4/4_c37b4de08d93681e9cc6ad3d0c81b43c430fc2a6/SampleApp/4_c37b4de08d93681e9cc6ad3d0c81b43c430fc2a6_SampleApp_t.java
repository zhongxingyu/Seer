 /*
  * Created by JFormDesigner on Mon Apr 21 12:50:34 EDT 2008
  */
 
 package Provider.GoogleMapsStatic.TestUI;
 
 import Provider.GoogleMapsStatic.*;
 import Task.*;
 import Task.Manager.*;
 import Task.ProgressMonitor.*;
 import Task.Support.CoreSupport.*;
 import Task.Support.GUISupport.*;
 import com.jgoodies.forms.factories.*;
 import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
 
 import info.clearthought.layout.*;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 
 import javax.imageio.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.beans.*;
 import java.text.*;
 import java.util.concurrent.*;
 
 import java.io.StringReader;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /** @author nazmul idris */
 public class SampleApp extends JFrame {
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // data members
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 /** reference to task */
 private SimpleTask _task;
 /** this might be null. holds the image to display in a popup */
 private BufferedImage _img;
 /** this might be null. holds the text in case image doesn't display */
 private String _respStr;
 
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // main method...
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
 public static void main(String[] args) {
   Utils.createInEDT(SampleApp.class);
 }
 
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 // constructor
 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
 private void doInit() {
   GUIUtils.setAppIcon(this, "burn.png");
   GUIUtils.centerOnScreen(this);
   setVisible(true);
 
   int W = 28, H = W;
   boolean blur = false;
   float alpha = .7f;
 
   try {
     btnGetMap.setIcon(ImageUtils.loadScaledBufferedIcon("ok1.png", W, H, blur, alpha));
     btnQuit.setIcon(ImageUtils.loadScaledBufferedIcon("charging.png", W, H, blur, alpha));
   }
   catch (Exception e) {
     System.out.println(e);
   }
 
   _setupTask();
 }
 
 /** create a test task and wire it up with a task handler that dumps output to the textarea */
 @SuppressWarnings("unchecked")
 
 
 
 private void _setupTask() {
 
   TaskExecutorIF<ByteBuffer> functor = new TaskExecutorAdapter<ByteBuffer>() {
     public ByteBuffer doInBackground(Future<ByteBuffer> swingWorker,
                                      SwingUIHookAdapter hook) throws Exception
     {
 
       _initHook(hook);
 
       // set the license key
       MapLookup.setLicenseKey(ttfLicense.getText());
      // address.setText("38 Fairview Rd West");
      // city.setText("Mississauga");
     //  state.setText("ON");
       
       String xml = MapLookup.getMap(address.getText(), city.getText(), state.getText());
       
       //Handle XML - http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       DocumentBuilder db = dbf.newDocumentBuilder();
       Document doc = db.parse(new URL(xml).openStream());
       NodeList nodes = doc.getElementsByTagName("GeocodeResponse");
 		for (int temp = 0; temp < nodes.getLength(); temp++) {
 			 
 			   Node nNode = nodes.item(temp);
 			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 	 
 			      Element eElement = (Element) nNode;
 			      String check = getTagValue("status", eElement);
			      if ((address.getText().isEmpty()==false) && (check.equals("OK") && lat.equals("empty")==false)){
 			    	   lat = getTagValue("lat", eElement);
 			    	   lon = getTagValue("lng", eElement);
 			    	   ttfLat.setText(lat);
 			    	   ttfLon.setText(lon);
 			    	   lat = "empty";
 			    	   lon = "empty";
 			    	   address.setText("");
 			      }
 			       
 			   }
 			}
       
       // get the uri for the static map
 		
       double x = Double.parseDouble(ttfLat.getText()) + 3;
       double y = Double.parseDouble(ttfLon.getText()) - 3;
       String uri = MapLookup.getMap( /*"1600 Amphitheatre Pky",
     		  						 "ountain View",
     		  						 "CA",
                                      Integer.parseInt(ttfSizeW.getText()),
                                      Integer.parseInt(ttfSizeH.getText()),
                                      mapZoom,
                                      new MapMarker(38.931099, -77.3489, MapMarker.MarkerColor.green, 'v'),
                                      new MapMarker(x+2, y-1, MapMarker.MarkerColor.red, 'n')*/
     		  Double.parseDouble(ttfLat.getText()),
               Double.parseDouble(ttfLon.getText()),
               Integer.parseInt(ttfSizeW.getText()),
               Integer.parseInt(ttfSizeH.getText()),
               mapZoom,
               new MapMarker(38.931099, -77.3489, MapMarker.MarkerColor.green, 'v'),
               new MapMarker(x+2, y-1, MapMarker.MarkerColor.red, 'n') 
       								
       );
       		  						 
       sout("Google Maps URI=" + uri);
 
       // get the map from Google
       GetMethod get = new GetMethod(uri);
       new HttpClient().executeMethod(get);
 
       
       ByteBuffer data = HttpUtils.getMonitoredResponse(hook, get);
 
       try {
         _img = ImageUtils.toCompatibleImage(ImageIO.read(data.getInputStream()));
         sout("converted downloaded data to image...");
       }
       catch (Exception e) {
         _img = null;
         sout("The URI is not an image. Data is downloaded, can't display it as an image.");
         _respStr = new String(data.getBytes());
       }
 
       return data;
     }
 
     @Override public String getName() {
       return _task.getName();
     }
   };
 
   _task = new SimpleTask(
       new TaskManager(),
       functor,
       "HTTP GET Task",
       "Download an image from a URL",
       AutoShutdownSignals.Daemon
   );
 
   _task.addStatusListener(new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       sout(":: task status change - " + ProgressMonitorUtils.parseStatusMessageFrom(evt));
       lblProgressStatus.setText(ProgressMonitorUtils.parseStatusMessageFrom(evt));
     }
   });
 
   _task.setTaskHandler(new
       SimpleTaskHandler<ByteBuffer>() {
         @Override public void beforeStart(AbstractTask task) {
           sout(":: taskHandler - beforeStart");
         }
         @Override public void started(AbstractTask task) {
           sout(":: taskHandler - started ");
         }
         /** {@link SampleApp#_initHook} adds the task status listener, which is removed here */
         @Override public void stopped(long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- stopped");
           sout(":: time = " + time / 1000f + "sec");
           task.getUIHook().clearAllStatusListeners();
         }
         @Override public void interrupted(Throwable e, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- interrupted - " + e.toString());
         }
         @Override public void ok(ByteBuffer value, long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- ok - size=" + (value == null
               ? "null"
               : value.toString()));
           if (_img != null) {
             _displayImgInFrame();
           }
           else _displayRespStrInFrame();
 
         }
         @Override public void error(Throwable e, long time, AbstractTask task) {
           sout(":: taskHandler [" + task.getName() + "]- error - " + e.toString());
         }
         @Override public void cancelled(long time, AbstractTask task) {
           sout(" :: taskHandler [" + task.getName() + "]- cancelled");
         }
       }
   );
 }
 
 private SwingUIHookAdapter _initHook(SwingUIHookAdapter hook) {
   hook.enableRecieveStatusNotification(checkboxRecvStatus.isSelected());
   hook.enableSendStatusNotification(checkboxSendStatus.isSelected());
 
   hook.setProgressMessage(ttfProgressMsg.getText());
 
   PropertyChangeListener listener = new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       SwingUIHookAdapter.PropertyList type = ProgressMonitorUtils.parseTypeFrom(evt);
       int progress = ProgressMonitorUtils.parsePercentFrom(evt);
       String msg = ProgressMonitorUtils.parseMessageFrom(evt);
 
       progressBar.setValue(progress);
       progressBar.setString(type.toString());
 
       sout(msg);
     }
   };
 
   hook.addRecieveStatusListener(listener);
   hook.addSendStatusListener(listener);
   hook.addUnderlyingIOStreamInterruptedOrClosed(new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent evt) {
       sout(evt.getPropertyName() + " fired!!!");
     }
   });
 
   return hook;
 }
 
 //Method Source from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
 private static String getTagValue(String sTag, Element eElement) {
 	NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
       Node nValue = (Node) nlList.item(0);
 
 	return nValue.getNodeValue();
 }
 
 private void _displayImgInFrame() {
 /*
   final JFrame frame = new JFrame("Google Static Map");
   GUIUtils.setAppIcon(frame, "71.png");
   frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 */
   JLabel imgLbl = new JLabel(new ImageIcon(_img));
   imgLbl.setToolTipText(MessageFormat.format("<html>Image downloaded from URI<br>size: w={0}, h={1}</html>",
                                              _img.getWidth(), _img.getHeight()));
 /*  imgLbl.addMouseListener(new MouseListener() {
     public void mouseClicked(MouseEvent e) {}
     public void mousePressed(MouseEvent e) { frame.dispose();}
     public void mouseReleased(MouseEvent e) { }
     public void mouseEntered(MouseEvent e) { }
     public void mouseExited(MouseEvent e) { }
   });
 
   frame.setContentPane(imgLbl);
   frame.pack();
 
   GUIUtils.centerOnScreen(frame);
   frame.setVisible(true);
 */  
   mapPane.removeAll();
  // mapPane.add(btnZoomin);
  // mapPane.add(btnZoomout);
  // mapPane.add(btnPanUp);
 	mapPane.add(panel4, BorderLayout.WEST);
 
   mapPane.add(imgLbl, BorderLayout.CENTER);
   
 	
 }
 
 /*private void _displayImgAfterZoom(){
 	
 	  
 		
 	  JLabel imgLbl = new JLabel(new ImageIcon(_img));
 	  imgLbl.setToolTipText(MessageFormat.format("<html>Image downloaded from URI<br>size: w={0}, h={1}</html>",
 	                                             _img.getWidth(), _img.getHeight()));
 	  
 	  mapPane.add(imgLbl, BorderLayout.CENTER);
 	
 }*/
 private void _displayRespStrInFrame() {
 
   final JFrame frame = new JFrame("Google Static Map - Error");
   GUIUtils.setAppIcon(frame, "69.png");
   frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
   JTextArea response = new JTextArea(_respStr, 25, 80);
   response.addMouseListener(new MouseListener() {
     public void mouseClicked(MouseEvent e) {}
     public void mousePressed(MouseEvent e) { frame.dispose();}
     public void mouseReleased(MouseEvent e) { }
     public void mouseEntered(MouseEvent e) { }
     public void mouseExited(MouseEvent e) { }
   });
 
   frame.setContentPane(new JScrollPane(response));
   frame.pack();
 
   GUIUtils.centerOnScreen(frame);
   frame.setVisible(true);
 }
 
 /** simply dump status info to the textarea */
 private void sout(final String s) {
   Runnable soutRunner = new Runnable() {
     public void run() {
       if (ttaStatus.getText().equals("")) {
         ttaStatus.setText(s);
       }
       else {
         ttaStatus.setText(ttaStatus.getText() + "\n" + s);
       }
     }
   };
 
   if (ThreadUtils.isInEDT()) {
     soutRunner.run();
   }
   else {
     SwingUtilities.invokeLater(soutRunner);
   }
 }
 
 private void startTaskAction() {
   try {
     _task.execute();
   }
   catch (TaskException e) {
     sout(e.getMessage());
   }
 }
 
 
 public SampleApp() {
   initComponents();
   doInit();
 }
 
 private void quitProgram() {
   _task.shutdown();
   System.exit(0);
 }
 
 private void initComponents() {
   // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
   // Generated using JFormDesigner non-commercial license
   panel4 = new JPanel();
   btnZoomin = new JButton();
   btnZoomout = new JButton();
   mapPane = new JPanel();	
   btnPanUp = new JButton();
   btnPanDown = new JButton();
   btnPanLeft = new JButton();
   btnPanRight = new JButton();
   sldZoom = new JSlider(JSlider.VERTICAL, 0, 19, 14);
   address = new JTextField();
   city = new JTextField();
   state = new JTextField();
   
   dialogPane = new JPanel();
   contentPanel = new JPanel();
   panel1 = new JPanel();
   label2 = new JLabel();
   ttfSizeW = new JTextField();
   label4 = new JLabel();
   ttfLat = new JTextField();
   btnGetMap = new JButton();
   label3 = new JLabel();
   ttfSizeH = new JTextField();
   label5 = new JLabel();
   ttfLon = new JTextField();
   btnQuit = new JButton();
   label1 = new JLabel();
   ttfLicense = new JTextField();
   label6 = new JLabel();
   ttfZoom = new JTextField();
   scrollPane1 = new JScrollPane();
   ttaStatus = new JTextArea();
   panel2 = new JPanel();
   panel3 = new JPanel();
   checkboxRecvStatus = new JCheckBox();
   checkboxSendStatus = new JCheckBox();
   ttfProgressMsg = new JTextField();
   progressBar = new JProgressBar();
   lblProgressStatus = new JLabel();
   
 
   //======== this ========
   setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
   setTitle("Google Static Maps");
   setIconImage(null);
   Container contentPane = getContentPane();
   contentPane.setLayout(new GridLayout(1,2));
 
   //======== dialogPane ========
   {
   	dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
   	dialogPane.setOpaque(false);
   	dialogPane.setLayout(new BorderLayout());
 
   	//======== contentPanel ========
   	{
   		contentPanel.setOpaque(false);
   		contentPanel.setLayout(new TableLayout(new double[][] {
   			{TableLayout.FILL},
   			{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
   		((TableLayout)contentPanel.getLayout()).setHGap(8);
   		((TableLayout)contentPanel.getLayout()).setVGap(8);
 
   		//======== panel1 ========
   		{
   			panel1.setOpaque(false);
   			panel1.setBorder(new CompoundBorder(
   				new TitledBorder("Configure the inputs to Google Static Maps"),
   				Borders.DLU2_BORDER));
   			panel1.setLayout(new TableLayout(new double[][] {
   				{0.17, 0.17, 0.17, 0.17, 0.05, TableLayout.FILL},
   				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
   			((TableLayout)panel1.getLayout()).setHGap(5);
   			((TableLayout)panel1.getLayout()).setVGap(5);
 
   			//---- label2 ----
   			label2.setText("Size Width");
   			label2.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfSizeW ----
   			ttfSizeW.setText("512");
   			panel1.add(ttfSizeW, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label4 ----
   			label4.setText("Latitude");
   			label4.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label4, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLat ----
   			//ttfLat.setText("38.931099");
   			panel1.add(ttfLat, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- btnGetMap ----
   			btnGetMap.setText("Get Map");
   			btnGetMap.setHorizontalAlignment(SwingConstants.CENTER);
   			btnGetMap.setMnemonic('G');
   			btnGetMap.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					startTaskAction();
   				}
   			});
   			panel1.add(btnGetMap, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label3 ----
   			label3.setText("Size Height");
   			label3.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label3, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfSizeH ----
   			ttfSizeH.setText("512");
   			panel1.add(ttfSizeH, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label5 ----
   			label5.setText("Longitude");
   			label5.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label5, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLon ----
   			//ttfLon.setText("-77.3489");
   			panel1.add(ttfLon, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- btnQuit ----
   			btnQuit.setText("Quit");
   			btnQuit.setMnemonic('Q');
   			btnQuit.setHorizontalAlignment(SwingConstants.LEFT);
   			btnQuit.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnQuit.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					quitProgram();
   				}
   			});
   			panel1.add(btnQuit, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label1 ----
   			label1.setText("License Key");
   			label1.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label1, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfLicense ----
   			ttfLicense.setToolTipText("Enter your own URI for a file to download in the background");
   			panel1.add(ttfLicense, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- label6 ----
   			label6.setText("Address");
   			label6.setHorizontalAlignment(SwingConstants.RIGHT);
   			panel1.add(label6, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfZoom ----
   			mapZoom = 14;
   			ttfZoom.setText("14");
   			panel1.add(address, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   			panel1.add(city, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   			
   			
   		}
   		contentPanel.add(panel1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   		//======== scrollPane1 ========
   		{
   			scrollPane1.setBorder(new TitledBorder("System.out - displays all status and progress messages, etc."));
   			scrollPane1.setOpaque(false);
 
   			//---- ttaStatus ----
   			ttaStatus.setBorder(Borders.createEmptyBorder("1dlu, 1dlu, 1dlu, 1dlu"));
   			ttaStatus.setToolTipText("<html>Task progress updates (messages) are displayed here,<br>along with any other output generated by the Task.<html>");
   			scrollPane1.setViewportView(ttaStatus);
   		}
   		contentPanel.add(scrollPane1, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   		//======== panel2 ========
   		{
   			panel2.setOpaque(false);
   			panel2.setBorder(new CompoundBorder(
   				new TitledBorder("Status - control progress reporting"),
   				Borders.DLU2_BORDER));
   			panel2.setLayout(new TableLayout(new double[][] {
   				{0.45, TableLayout.FILL, 0.45},
   				{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
   			((TableLayout)panel2.getLayout()).setHGap(5);
   			((TableLayout)panel2.getLayout()).setVGap(5);
 
   			//======== panel3 ========
   			{
   				panel3.setOpaque(false);
   				panel3.setLayout(new GridLayout(1, 2));
 
   				//---- checkboxRecvStatus ----
   				checkboxRecvStatus.setText("Enable \"Recieve\"");
   				checkboxRecvStatus.setOpaque(false);
   				checkboxRecvStatus.setToolTipText("Task will fire \"send\" status updates");
   				checkboxRecvStatus.setSelected(true);
   				panel3.add(checkboxRecvStatus);
 
   				//---- checkboxSendStatus ----
   				checkboxSendStatus.setText("Enable \"Send\"");
   				checkboxSendStatus.setOpaque(false);
   				checkboxSendStatus.setToolTipText("Task will fire \"recieve\" status updates");
   				panel3.add(checkboxSendStatus);
   			}
   			panel2.add(panel3, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- ttfProgressMsg ----
   			ttfProgressMsg.setText("Loading map from Google Static Maps");
   			ttfProgressMsg.setToolTipText("Set the task progress message here");
   			panel2.add(ttfProgressMsg, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- progressBar ----
   			progressBar.setStringPainted(true);
   			progressBar.setString("progress %");
   			progressBar.setToolTipText("% progress is displayed here");
   			panel2.add(progressBar, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
 
   			//---- lblProgressStatus ----
   			lblProgressStatus.setText("task status listener");
   			lblProgressStatus.setHorizontalTextPosition(SwingConstants.LEFT);
   			lblProgressStatus.setHorizontalAlignment(SwingConstants.LEFT);
   			lblProgressStatus.setToolTipText("Task status messages are displayed here when the task runs");
   			panel2.add(lblProgressStatus, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   		}
   		contentPanel.add(panel2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
   	}
   	dialogPane.add(contentPanel, BorderLayout.CENTER);
   }
   
   //======== mapPane ========
   {
   	mapPane.setBorder(new EmptyBorder(8, 8, 8, 8));
   	mapPane.setOpaque(false);
   	mapPane.setLayout(new BorderLayout());
   	
   		//===== panel4 =====
   		panel4.setOpaque(false);
 		panel4.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 /*			//---- btnZoomin ----
   			btnZoomin.setSize(2, 2);
   			btnZoomin.setText("+");
   			btnZoomin.setHorizontalAlignment(SwingConstants.LEFT);
   			btnZoomin.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnZoomin.setOpaque(true);
   			btnZoomin.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   				int x = Integer.parseInt(ttfZoom.getText());
   				x++;
   				mapZoom = x;
   				ttfZoom.setText(Integer.toString(x));
   			  
   			  
   				startTaskAction();
   				mapPane.repaint();
 				//quitProgram();
   				}
   			});
   			c.gridwidth = 3;
   			panel4.add(btnZoomin, c);
   			
 			//---- btnZoomout ----
   			btnZoomout.setSize(2, 2);
   			btnZoomout.setText("-");
   			btnZoomout.setHorizontalAlignment(SwingConstants.LEFT);
   			btnZoomout.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnZoomout.setOpaque(true);
   			btnZoomout.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   				int x = Integer.parseInt(ttfZoom.getText());
   				x--;
   				mapZoom = x;
   				ttfZoom.setText(Integer.toString(x));
   			  
   			  
   				startTaskAction();
   				mapPane.repaint();
 				//quitProgram();
   				}
   			});
   			panel4.add(btnZoomout, c);*/
   			
   			btnPanUp.setSize(2,2);
   			btnPanUp.setText("^");
   			btnPanUp.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanUp.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanUp.setOpaque(true);
   			btnPanUp.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   	  				double x = Double.parseDouble(ttfLat.getText());
   	  				
   	  				x = x+ 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x > 85)
   	  					x = x - 170;
   	  				ttfLat.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			panel4.add(btnPanUp, c);
   			
   			btnPanDown.setSize(2,2);
   			btnPanDown.setText("v");
   			btnPanDown.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanDown.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanDown.setOpaque(true);
   			btnPanDown.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   	  				double x = Double.parseDouble(ttfLat.getText());
   	  				
   	  				x = x - 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x < -85)
   	  					x = x + 170;
   	  				ttfLat.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			panel4.add(btnPanDown, c);
   			
   			btnPanLeft.setSize(2,2);
   			btnPanLeft.setText("<");
   			btnPanLeft.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanLeft.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanLeft.setOpaque(true);
   			btnPanLeft.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   	  				double x = Double.parseDouble(ttfLon.getText());
   	  				
   	  				x = x - 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x < -175)
   	  					x = x + 350;
   	  				ttfLon.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			panel4.add(btnPanLeft, c);
   			
   			btnPanRight.setSize(2,2);
   			btnPanRight.setText(">");
   			btnPanRight.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanRight.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanRight.setOpaque(true);
   			btnPanRight.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   	  				double x = Double.parseDouble(ttfLon.getText());
   	  				
   	  				x = x + 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x > 175)
   	  					x = x - 350;
   	  				ttfLon.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			panel4.add(btnPanRight, c);
   			
   			sldZoom.setMajorTickSpacing(19);
   			sldZoom.setMinorTickSpacing(1);
   			sldZoom.setForeground(Color.black);
   			sldZoom.setPaintTicks(true);
   			sldZoom.setPaintLabels(true);
   			sldZoom.setPaintTrack(true);
   			sldZoom.setSnapToTicks(true);
   			sldZoom.addChangeListener(new ChangeListener(){
   				public void stateChanged(ChangeEvent e) {
   					JSlider source = (JSlider)e.getSource();
   					mapZoom = (int)source.getValue();
   					startTaskAction();
   					mapPane.repaint();
   				}
   			});
   			panel4.add(sldZoom, c);
 	mapPane.add(panel4, BorderLayout.WEST);
   	
   }
   contentPane.add(dialogPane, BorderLayout.WEST);
   contentPane.add(mapPane, BorderLayout.EAST);
   setSize(1600, 485);
   setLocationRelativeTo(null);
   // JFormDesigner - End of component initialization  //GEN-END:initComponents
 }
 
 // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
 // Generated using JFormDesigner non-commercial license
 private JPanel panel4;
 private JButton btnZoomin;
 private JButton btnZoomout;
 private JPanel mapPane;
 private JButton btnPanUp;
 private JButton btnPanDown;
 private JButton btnPanLeft;
 private JButton btnPanRight;
 private JSlider sldZoom;
 private JTextField address;
 private JTextField city;
 private JTextField state;
 private String lat ="";
 private String lon ="";
 
 private JPanel dialogPane;
 private JPanel contentPanel;
 private JPanel panel1;
 private JLabel label2;
 private JTextField ttfSizeW;
 private JLabel label4;
 private JTextField ttfLat;
 private JButton btnGetMap;
 private JLabel label3;
 private JTextField ttfSizeH;
 private JLabel label5;
 private JTextField ttfLon;
 private JButton btnQuit;
 private JLabel label1;
 private JTextField ttfLicense;
 private JLabel label6;
 private JTextField ttfZoom;
 private JScrollPane scrollPane1;
 private JTextArea ttaStatus;
 private JPanel panel2;
 private JPanel panel3;
 private JCheckBox checkboxRecvStatus;
 private JCheckBox checkboxSendStatus;
 private JTextField ttfProgressMsg;
 private JProgressBar progressBar;
 private JLabel lblProgressStatus;
 private int mapZoom;
 // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
