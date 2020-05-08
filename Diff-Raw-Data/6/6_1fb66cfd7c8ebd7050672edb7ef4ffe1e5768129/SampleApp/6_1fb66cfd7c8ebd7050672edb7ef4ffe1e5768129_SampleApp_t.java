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
 import java.util.ArrayList;
 import java.util.concurrent.*;
 
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 
 
 
 
 /**
  * @author Preshoth
  * 
  */
 /**
  * @author Preshoth
  *
  */
 /**
  * @author Preshoth
  *
  */
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
 
 /**
  * Added a default start up
  * Uses Seneca address: 70 Pond Rd Toronto Ontario
  * @author Preshoth 
  */
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
     address.setText("70 Pond Rd Toronto Ontario");
    A = new MapMarker(90, 180);
	B = new MapMarker(90, 180);
     
     
   }
   catch (Exception e) {
     System.out.println(e);
   }
   
   _setupTask();
   btnGetMap.doClick();
   
 }
 
 /** create a test task and wire it up with a task handler that dumps output to the textarea */
 
 /**
  * @author Preshoth
  * @Description 
  * XML and HTTP, converts string address input into latitude and longitude in order to display correctly and add markers
  * Uses XML to pull encoded path data in order to draw directions accurately
  */
 @SuppressWarnings("unchecked")
 
 
 
 private void _setupTask() {
 
   TaskExecutorIF<ByteBuffer> functor = new TaskExecutorAdapter<ByteBuffer>() {
     public ByteBuffer doInBackground(Future<ByteBuffer> swingWorker,
                                      SwingUIHookAdapter hook) throws Exception
     {
 
       _initHook(hook);
       
       
       if (entry == true && closeMatch.getItemCount() == 0 && address.getText().equals("")==false){
       //URI that gets the XML file
       String xml = MapLookup.getMap(address.getText());
       
       //Handle XML - http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       DocumentBuilder db = dbf.newDocumentBuilder();
       Document doc = db.parse(new URL(xml).openStream());
       NodeList nodes = doc.getElementsByTagName("result");
       xmlLat = new ArrayList(nodes.getLength());
       xmlLon = new ArrayList(nodes.getLength());
 		for (int temp = 0; temp < nodes.getLength(); temp++) {
 			 
 			   Node nNode = nodes.item(temp);
 			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				   
 			      Element eElement = (Element) nNode;
 			      closeMatch.addItem(getTagValue("formatted_address", eElement));
 			      xmlLat.add(getTagValue("lat", eElement));
 			      xmlLon.add(getTagValue("lng", eElement)); 
 			   }
 			}
       }
       
      // get the uri for the static map
 	 if (entry == true)
 	 {
 	ttfLat.setText(xmlLat.get(matchIndex).toString());
 	 ttfLon.setText(xmlLon.get(matchIndex).toString());
 	 }
 	
       String uri = MapLookup.getMap(
     		  Double.parseDouble(ttfLat.getText()),
               Double.parseDouble(ttfLon.getText()),
               Integer.parseInt(ttfSizeW.getText()),
               Integer.parseInt(ttfSizeH.getText()),
               mapZoom,
               getEncodedPath(),
               A,
               B 
       								
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
 
 /**
  * Grabs the XML from Google Direction Service in order to pull encoded paths
  * @return encoded path
  * @throws ParserConfigurationException
  * @throws MalformedURLException
  * @throws SAXException
  * @throws IOException
  */
 private static String getEncodedPath() throws ParserConfigurationException, MalformedURLException, SAXException, IOException{
 	
 	
     String xml = MapLookup.getMap(A.getLat(), A.getLon(), B.getLat(), B.getLon());
     
     //Handle XML - http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     DocumentBuilder db = dbf.newDocumentBuilder();
     Document doc = db.parse(new URL(xml).openStream());
     NodeList nodes = doc.getElementsByTagName("overview_polyline");
 
 		for (int temp = 0; temp < nodes.getLength(); temp++) {
 			 
 			   Node nNode = nodes.item(temp);
 			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				   
 			      Element eElement = (Element) nNode;
 			      path = getTagValue("points", eElement);
 			   }
 			}
     
 	
 	
 	return path;
 }
 
 /**
  *@author Preshoth 
  * @Description map image to a new panel, this allows for key listener to interact with panel and allows panning by arrow keys
  */
 private void _displayImgInFrame() {
   JLabel imgLbl = new JLabel(new ImageIcon(_img));
   imgLbl.setToolTipText(MessageFormat.format("<html>Image downloaded from URI<br>size: w={0}, h={1}</html>",
                                              _img.getWidth(), _img.getHeight()));
   
   mapPane.removeAll();
   panelMap.removeAll();
 	mapPane.add(panel4, BorderLayout.WEST);
 
   panelMap.add(imgLbl, BorderLayout.CENTER);
   mapPane.add(panelMap, BorderLayout.CENTER);
   mapPane.add(sldZoom, BorderLayout.SOUTH);
   mapPane.add(searchPanel, BorderLayout.NORTH);
   panelMap.requestFocus();
   
 	
 }
 
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
   mapPane = new JPanel();	
   
   dialogPane = new JPanel();
   contentPanel = new JPanel();
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
   setTitle("Google Static Maps - Preshoth Paramalingam");
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
 
   			//---- ttfZoom ----
   			mapZoom = 14;
   			ttfZoom.setText("14");
   			
   			
   		}
 
   		//======== scrollPane1 ========
   		{
   			scrollPane1.setBorder(new TitledBorder("System.out - displays all status and progress messages, etc."));
   			scrollPane1.setOpaque(false);
 
   			//---- ttaStatus ----
   			ttaStatus.setBorder(Borders.createEmptyBorder("1dlu, 1dlu, 1dlu, 1dlu"));
   			ttaStatus.setToolTipText("<html>Task progress updates (messages) are displayed here,<br>along with any other output generated by the Task.<html>");
   			scrollPane1.setViewportView(ttaStatus);
   			//scrollPane1.setVisible(false);
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
 
   			btnPanUp = new JButton();
   			
   			btnPanUp.setSize(2,2);
   			btnPanUp.setText("^");
   			btnPanUp.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanUp.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanUp.setOpaque(true);
   			btnPanUp.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					entry = false;
   	  				double x = Double.parseDouble(ttfLat.getText());
   	  				
   	  				x = x+ 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x > 85)
   	  					x = x - 170;
   	  				ttfLat.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			
   			btnAddMarkerA = new JButton("Add Marker A");
   			btnAddMarkerA.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					A = new MapMarker(Double.parseDouble(ttfLat.getText()),  Double.parseDouble(ttfLon.getText()), MapMarker.MarkerColor.green, 'A');
   					startTaskAction();
   					
   				}
   			});
   			GridBagConstraints gbc_btnAddMarkerA = new GridBagConstraints();
   			gbc_btnAddMarkerA.insets = new Insets(0, 0, 5, 5);
   			gbc_btnAddMarkerA.gridx = 0;
   			gbc_btnAddMarkerA.gridy = 0;
   			panel4.add(btnAddMarkerA, gbc_btnAddMarkerA);
   			
   			btnAddMarkerB = new JButton("Add Marker B");
   			btnAddMarkerB.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					B = new MapMarker(Double.parseDouble(ttfLat.getText()),  Double.parseDouble(ttfLon.getText()), MapMarker.MarkerColor.red, 'B');
   					startTaskAction();
   					
   				}
   			});
   			GridBagConstraints gbc_btnAddMarkerB = new GridBagConstraints();
   			gbc_btnAddMarkerB.insets = new Insets(0, 0, 5, 5);
   			gbc_btnAddMarkerB.gridx = 0;
   			gbc_btnAddMarkerB.gridy = 1;
   			panel4.add(btnAddMarkerB, gbc_btnAddMarkerB);
   			GridBagConstraints gbc_btnPanUp = new GridBagConstraints();
   			gbc_btnPanUp.insets = new Insets(0, 0, 5, 5);
   			gbc_btnPanUp.gridx = 3;
   			gbc_btnPanUp.gridy = 1;
   			panel4.add(btnPanUp, gbc_btnPanUp);
   			btnPanRight = new JButton();
   			
   			btnPanRight.setSize(2,2);
   			btnPanRight.setText(">");
   			btnPanRight.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanRight.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanRight.setOpaque(true);
   			btnPanRight.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					entry = false;
   	  				double x = Double.parseDouble(ttfLon.getText());
   	  				
   	  				x = x + 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x > 175)
   	  					x = x - 350;
   	  				ttfLon.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			btnPanLeft = new JButton();
   			
   			btnPanLeft.setSize(2,2);
   			btnPanLeft.setText("<");
   			btnPanLeft.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanLeft.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanLeft.setOpaque(true);
   			btnPanLeft.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					entry = false;
   	  				double x = Double.parseDouble(ttfLon.getText());
   	  				
   	  				x = x - 131.072/java.lang.Math.pow (2,mapZoom+1);
   	  				if (x < -175)
   	  					x = x + 350;
   	  				ttfLon.setText(Double.toString(x));
   	  			  
   	  			  
   	  				startTaskAction();
   	  				mapPane.repaint();
   				}
   			});
   			GridBagConstraints gbc_btnPanLeft = new GridBagConstraints();
   			gbc_btnPanLeft.insets = new Insets(0, 0, 5, 5);
   			gbc_btnPanLeft.gridx = 2;
   			gbc_btnPanLeft.gridy = 2;
   			panel4.add(btnPanLeft, gbc_btnPanLeft);
   			GridBagConstraints gbc_btnPanRight = new GridBagConstraints();
   			gbc_btnPanRight.insets = new Insets(0, 0, 5, 0);
   			gbc_btnPanRight.gridx = 4;
   			gbc_btnPanRight.gridy = 2;
   			panel4.add(btnPanRight, gbc_btnPanRight);
   			btnPanDown = new JButton();
   			
   			btnPanDown.setSize(2,2);
   			btnPanDown.setText("v");
   			btnPanDown.setHorizontalAlignment(SwingConstants.LEFT);
   			btnPanDown.setHorizontalTextPosition(SwingConstants.RIGHT);
   			btnPanDown.setOpaque(true);
   			btnPanDown.addActionListener(new ActionListener() {
   				public void actionPerformed(ActionEvent e) {
   					entry = false;
   				  				double x = Double.parseDouble(ttfLat.getText());
   				  				
   				  				x = x - 131.072/java.lang.Math.pow (2,mapZoom+1);
   				  				if (x < -85)
   				  					x = x + 170;
   				  				ttfLat.setText(Double.toString(x));
   				  			  
   				  			  
   				  				startTaskAction();
   				  				mapPane.repaint();
   				}
   			});
   			GridBagConstraints gbc_btnPanDown = new GridBagConstraints();
   			gbc_btnPanDown.insets = new Insets(0, 0, 0, 5);
   			gbc_btnPanDown.gridx = 3;
   			gbc_btnPanDown.gridy = 3;
   			panel4.add(btnPanDown, gbc_btnPanDown);
 	mapPane.add(panel4, BorderLayout.WEST);
   	
   }
   contentPane.add(dialogPane, BorderLayout.WEST);
   panel1 = new JPanel();
   dialogPane.add(panel1, BorderLayout.NORTH);
   panel1.setOpaque(false);
   panel1.setBorder(new CompoundBorder(
   	new TitledBorder("Configure the inputs to Google Static Maps"),
   	Borders.DLU2_BORDER));
   GridBagLayout gbl_panel1 = new GridBagLayout();
   gbl_panel1.columnWidths = new int[]{123, 123, 123, 123, 46, 197, 0};
   gbl_panel1.rowHeights = new int[]{23, 23, 20, 0};
   gbl_panel1.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
   gbl_panel1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
   panel1.setLayout(gbl_panel1);
   label2 = new JLabel();
   
     			//---- label2 ----
     			label2.setText("Size Width");
     			label2.setHorizontalAlignment(SwingConstants.RIGHT);
     			GridBagConstraints gbc_label2 = new GridBagConstraints();
     			gbc_label2.fill = GridBagConstraints.BOTH;
     			gbc_label2.insets = new Insets(0, 0, 5, 5);
     			gbc_label2.gridx = 0;
     			gbc_label2.gridy = 0;
     			panel1.add(label2, gbc_label2);
     			ttfSizeW = new JTextField();
     			
     			  			//---- ttfSizeW ----
     			  			ttfSizeW.setText("512");
     			  			GridBagConstraints gbc_ttfSizeW = new GridBagConstraints();
     			  			gbc_ttfSizeW.fill = GridBagConstraints.BOTH;
     			  			gbc_ttfSizeW.insets = new Insets(0, 0, 5, 5);
     			  			gbc_ttfSizeW.gridx = 1;
     			  			gbc_ttfSizeW.gridy = 0;
     			  			panel1.add(ttfSizeW, gbc_ttfSizeW);
     			  			label4 = new JLabel();
     			  			
     			  			  			//---- label4 ----
     			  			  			label4.setText("Latitude");
     			  			  			label4.setHorizontalAlignment(SwingConstants.RIGHT);
     			  			  			GridBagConstraints gbc_label4 = new GridBagConstraints();
     			  			  			gbc_label4.fill = GridBagConstraints.BOTH;
     			  			  			gbc_label4.insets = new Insets(0, 0, 5, 5);
     			  			  			gbc_label4.gridx = 2;
     			  			  			gbc_label4.gridy = 0;
     			  			  			panel1.add(label4, gbc_label4);
     			  			  			ttfLat = new JTextField();
     			  			  			ttfLat.setEditable(false);
     			  			  			
     			  			  			  			//---- ttfLat ----
     			  			  			  			
     			  			  			  			GridBagConstraints gbc_ttfLat = new GridBagConstraints();
     			  			  			  			gbc_ttfLat.fill = GridBagConstraints.BOTH;
     			  			  			  			gbc_ttfLat.insets = new Insets(0, 0, 5, 5);
     			  			  			  			gbc_ttfLat.gridx = 3;
     			  			  			  			gbc_ttfLat.gridy = 0;
     			  			  			  			panel1.add(ttfLat, gbc_ttfLat);
     			  			  			  			btnGetMap = new JButton();
     			  			  			  			
     			  			  			  			  			//---- btnGetMap ----
     			  			  			  			  			btnGetMap.setText("Get Map");
     			  			  			  			  			btnGetMap.setHorizontalAlignment(SwingConstants.CENTER);
     			  			  			  			  			btnGetMap.setMnemonic('G');
     			  			  			  			  			btnGetMap.addActionListener(new ActionListener() {
     			  			  			  			  				public void actionPerformed(ActionEvent e) {
    			  			  			  			  					
     			  			  			  			  					closeMatch.removeAllItems();
     			  			  			  			  					entry = true;	
     			  			  			  			  					address.setCaretPosition(0);
     			  			  			  			  					if (address.getText().equals("")==false)
     			  			  			  			  						startTaskAction();
     			  			  			  			  				}
     			  			  			  			  			});
     			  			  			  			  			GridBagConstraints gbc_btnGetMap = new GridBagConstraints();
     			  			  			  			  			gbc_btnGetMap.anchor = GridBagConstraints.NORTH;
     			  			  			  			  			gbc_btnGetMap.fill = GridBagConstraints.HORIZONTAL;
     			  			  			  			  			gbc_btnGetMap.insets = new Insets(0, 0, 5, 0);
     			  			  			  			  			gbc_btnGetMap.gridx = 5;
     			  			  			  			  			gbc_btnGetMap.gridy = 0;
     			  			  			  			  			panel1.add(btnGetMap, gbc_btnGetMap);
     			  			  			  			  			label3 = new JLabel();
     			  			  			  			  			
     			  			  			  			  			  			//---- label3 ----
     			  			  			  			  			  			label3.setText("Size Height");
     			  			  			  			  			  			label3.setHorizontalAlignment(SwingConstants.RIGHT);
     			  			  			  			  			  			GridBagConstraints gbc_label3 = new GridBagConstraints();
     			  			  			  			  			  			gbc_label3.fill = GridBagConstraints.BOTH;
     			  			  			  			  			  			gbc_label3.insets = new Insets(0, 0, 5, 5);
     			  			  			  			  			  			gbc_label3.gridx = 0;
     			  			  			  			  			  			gbc_label3.gridy = 1;
     			  			  			  			  			  			panel1.add(label3, gbc_label3);
     			  			  			  			  			  			ttfSizeH = new JTextField();
     			  			  			  			  			  			
     			  			  			  			  			  			  			//---- ttfSizeH ----
     			  			  			  			  			  			  			ttfSizeH.setText("512");
     			  			  			  			  			  			  			GridBagConstraints gbc_ttfSizeH = new GridBagConstraints();
     			  			  			  			  			  			  			gbc_ttfSizeH.fill = GridBagConstraints.BOTH;
     			  			  			  			  			  			  			gbc_ttfSizeH.insets = new Insets(0, 0, 5, 5);
     			  			  			  			  			  			  			gbc_ttfSizeH.gridx = 1;
     			  			  			  			  			  			  			gbc_ttfSizeH.gridy = 1;
     			  			  			  			  			  			  			panel1.add(ttfSizeH, gbc_ttfSizeH);
     			  			  			  			  			  			  			label5 = new JLabel();
     			  			  			  			  			  			  			
     			  			  			  			  			  			  			  			//---- label5 ----
     			  			  			  			  			  			  			  			label5.setText("Longitude");
     			  			  			  			  			  			  			  			label5.setHorizontalAlignment(SwingConstants.RIGHT);
     			  			  			  			  			  			  			  			GridBagConstraints gbc_label5 = new GridBagConstraints();
     			  			  			  			  			  			  			  			gbc_label5.fill = GridBagConstraints.BOTH;
     			  			  			  			  			  			  			  			gbc_label5.insets = new Insets(0, 0, 5, 5);
     			  			  			  			  			  			  			  			gbc_label5.gridx = 2;
     			  			  			  			  			  			  			  			gbc_label5.gridy = 1;
     			  			  			  			  			  			  			  			panel1.add(label5, gbc_label5);
     			  			  			  			  			  			  			  			ttfLon = new JTextField();
     			  			  			  			  			  			  			  			ttfLon.setEditable(false);
     			  			  			  			  			  			  			  			
     			  			  			  			  			  			  			  			  			//---- ttfLon ----
     			  			  			  			  			  			  			  			  			//ttfLon.setText("-77.3489");
     			  			  			  			  			  			  			  			  			GridBagConstraints gbc_ttfLon = new GridBagConstraints();
     			  			  			  			  			  			  			  			  			gbc_ttfLon.fill = GridBagConstraints.BOTH;
     			  			  			  			  			  			  			  			  			gbc_ttfLon.insets = new Insets(0, 0, 5, 5);
     			  			  			  			  			  			  			  			  			gbc_ttfLon.gridx = 3;
     			  			  			  			  			  			  			  			  			gbc_ttfLon.gridy = 1;
     			  			  			  			  			  			  			  			  			panel1.add(ttfLon, gbc_ttfLon);
     			  			  			  			  			  			  			  			  			btnQuit = new JButton();
     			  			  			  			  			  			  			  			  			
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
     			  			  			  			  			  			  			  			  			  			GridBagConstraints gbc_btnQuit = new GridBagConstraints();
     			  			  			  			  			  			  			  			  			  			gbc_btnQuit.anchor = GridBagConstraints.NORTH;
     			  			  			  			  			  			  			  			  			  			gbc_btnQuit.fill = GridBagConstraints.HORIZONTAL;
     			  			  			  			  			  			  			  			  			  			gbc_btnQuit.insets = new Insets(0, 0, 5, 0);
     			  			  			  			  			  			  			  			  			  			gbc_btnQuit.gridx = 5;
     			  			  			  			  			  			  			  			  			  			gbc_btnQuit.gridy = 1;
     			  			  			  			  			  			  			  			  			  			panel1.add(btnQuit, gbc_btnQuit);
   contentPane.add(mapPane, BorderLayout.EAST);
   sldZoom = new JSlider(SwingConstants.HORIZONTAL, 0, 19, 14);
   mapPane.add(sldZoom, BorderLayout.SOUTH);
   
   sldZoom.setMajorTickSpacing(19);
   sldZoom.setMinorTickSpacing(1);
   sldZoom.setForeground(Color.black);
   sldZoom.setPaintTicks(true);
   sldZoom.setPaintLabels(true);
   sldZoom.setPaintTrack(true);
   sldZoom.setSnapToTicks(true);
   
   panelMap = new JPanel();
   panelMap.addKeyListener(new KeyAdapter() {
   	@Override
   	public void keyPressed(KeyEvent e) {
   	  	int key = e.getKeyCode();
   	  	if (key == KeyEvent.VK_UP)
   	  		btnPanUp.doClick();
   	  	else if (key == KeyEvent.VK_DOWN)
   	  		btnPanDown.doClick();
   	  	else if (key == KeyEvent.VK_LEFT)
   	  		btnPanLeft.doClick();
   	  	else if (key == KeyEvent.VK_RIGHT)
   	  		btnPanRight.doClick();
   	}
   });
   mapPane.add(panelMap, BorderLayout.CENTER);
   
   searchPanel = new JPanel();
   mapPane.add(searchPanel, BorderLayout.NORTH);
   GridBagLayout gbl_searchPanel = new GridBagLayout();
   gbl_searchPanel.columnWidths = new int[]{194, 0, 194, 0, 0, 0, 0, 194, 0, 0, 194, 0};
   gbl_searchPanel.rowHeights = new int[]{20, 0};
   gbl_searchPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
   gbl_searchPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
   searchPanel.setLayout(gbl_searchPanel);
   label6 = new JLabel();
   GridBagConstraints gbc_label6 = new GridBagConstraints();
   gbc_label6.fill = GridBagConstraints.BOTH;
   gbc_label6.insets = new Insets(0, 0, 0, 5);
   gbc_label6.gridx = 1;
   gbc_label6.gridy = 0;
   searchPanel.add(label6, gbc_label6);
   
     			//---- label6 ----
     			label6.setText("Address");
     			label6.setHorizontalAlignment(SwingConstants.LEFT);
   address = new JTextField();
   GridBagConstraints gbc_address = new GridBagConstraints();
   gbc_address.gridwidth = 3;
   gbc_address.fill = GridBagConstraints.BOTH;
   gbc_address.insets = new Insets(0, 0, 0, 5);
   gbc_address.gridx = 2;
   gbc_address.gridy = 0;
   searchPanel.add(address, gbc_address);
   address.addKeyListener(new KeyAdapter() {
   	@Override
   	public void keyPressed(KeyEvent e) {
   		int key = e.getKeyCode();
   		if (key == KeyEvent.VK_ENTER)
   			btnGetMap.doClick();
   	}
   });
   
   lblClosestMatch = new JLabel("Closest Match");
   GridBagConstraints gbc_lblClosestMatch = new GridBagConstraints();
   gbc_lblClosestMatch.fill = GridBagConstraints.BOTH;
   gbc_lblClosestMatch.insets = new Insets(0, 0, 0, 5);
   gbc_lblClosestMatch.gridx = 6;
   gbc_lblClosestMatch.gridy = 0;
   searchPanel.add(lblClosestMatch, gbc_lblClosestMatch);
   
   closeMatch = new JComboBox();
   GridBagConstraints gbc_closeMatch = new GridBagConstraints();
   gbc_closeMatch.insets = new Insets(0, 0, 0, 5);
   gbc_closeMatch.gridwidth = 3;
   gbc_closeMatch.fill = GridBagConstraints.BOTH;
   gbc_closeMatch.gridx = 7;
   gbc_closeMatch.gridy = 0;
   searchPanel.add(closeMatch, gbc_closeMatch);
   closeMatch.addItemListener(new ItemListener() {
   	public void itemStateChanged(ItemEvent e) {
   		matchIndex = closeMatch.getSelectedIndex();
   		entry = true;
   		startTaskAction();
   		
 
   	}
   });
   sldZoom.addChangeListener(new ChangeListener(){
   	public void stateChanged(ChangeEvent e) {
   		JSlider source = (JSlider)e.getSource();
   		mapZoom = (int)source.getValue();
   		startTaskAction();
   		mapPane.repaint();
   	}
   });
   setSize(1600, 485);
   setLocationRelativeTo(null);
   
 
 
 
   // JFormDesigner - End of component initialization  //GEN-END:initComponents
 }
 
 // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
 // Generated using JFormDesigner non-commercial license
 private JPanel panel4;
 private JPanel mapPane;
 private JButton btnPanUp;
 private JButton btnPanDown;
 private JButton btnPanLeft;
 private JButton btnPanRight;
 private JSlider sldZoom;
 private JTextField address;
 private ArrayList xmlLat;
 private ArrayList xmlLon;
 private int matchIndex;
 private Boolean entry=true;
 private static MapMarker A;
 private static MapMarker B;
 private static String path="";
 
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
 private JComboBox closeMatch;
 private JLabel lblClosestMatch;
 private JButton btnAddMarkerA;
 private JButton btnAddMarkerB;
 private JPanel panelMap;
 private JPanel searchPanel;
 // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
