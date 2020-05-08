 package mmMap;
 import java.awt.*;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.awt.print.Book;
 import java.awt.print.PageFormat;
 import java.awt.print.Paper;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.URL;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.print.Doc;
 import javax.print.DocFlavor;
 import javax.print.DocPrintJob;
 import javax.print.PrintService;
 import javax.print.PrintServiceLookup;
 import javax.print.SimpleDoc;
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.Copies;
 import javax.swing.*;
 
 import mmAccordionMenu.*;
 import mmEvents.*;
 
 import mmGPSCoordinates.*;
 import mmLanguage.MmLanguage;
 import mmObjWriter.MmObjWriter;
 import mmStationEvents.*;
 
 import org.jdesktop.swingx.*;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
 import org.jdesktop.swingx.mapviewer.DefaultWaypoint;
 import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
 import org.jdesktop.swingx.mapviewer.Waypoint;
 import org.jdesktop.swingx.mapviewer.WaypointPainter;
 import org.jdesktop.swingx.mapviewer.WaypointRenderer;
 import org.jdesktop.swingx.painter.Painter;
 import org.json.*;
 import org.json.simple.parser.*;
 
 
 
 
 public class MmMapViewer extends JPanel implements Printable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public JXMapKit mapKit;
 	public JLabel locLabel;
 	private JPanel panel;
 	
 	public MmGPSCoordReader geoCoordReader;
 	public Set wayPoints,wayLines;
 	float lat=0,lon=0;
 	
 	public WaypointPainter markGeoPos = new WaypointPainter();
 	
 	public WaypointPainter markGeoPos1 = new WaypointPainter();
 	
 	double widthx,heighty;
 	int prev_mousex,prev_mousey,mousex,mousey;
 	
 	boolean mousePressed,mouseDragged=false;
 	
 	
 	
 	//array to hold MmMousePoints class objects
 	public ArrayList<MmMousePoints> mousePos = new ArrayList<MmMousePoints>();
 	
 	//array for all mosuse points on the map
 	public ArrayList<MmMousePoints> geoMousePoints = new ArrayList<MmMousePoints>();
 	
 	//array to hold all the station lat,lan on the map, these values will be changing according to station new position
 	public ArrayList<GeoPosition> geoPos = new ArrayList<GeoPosition>();
 	
 	//array to hold station lat,lan except for first station, these values are constant and do not change until the station is deleted
 	public ArrayList<GeoPosition> initialGeoPos = new ArrayList<GeoPosition>();
 	
 	//array for swing points 
 	public ArrayList<GeoPosition> swingPoints = new ArrayList<GeoPosition>();
 	
 	//array for swing points in screen coordinates
 	public ArrayList<MmSwingPoints> swingPos= new ArrayList<MmSwingPoints>(); 
 	
 	//array for all points on map stations and swing points
 	ArrayList<GeoPosition> geoPoints = new ArrayList<GeoPosition>();
 	
 		
 	//holds the current moving station Position
 	public GeoPosition movingStation;
 	
 	//variable for current edge
 	int edgeIndex=-1;
 	
 	//variable for check status of station
 	public boolean isStationDelete = false;
 	
 	//variable for current station selected
 	public int station_index = -1;
 	
 	//variable for open file name
 	public File openFileName;
 	
 	boolean plotPnt=false;
 	
 	boolean drawPnts=true;
 	boolean addEvents=false;
 	
 	boolean isMarkerPicked=false;
 	
 	JLabel moveLabel;
 	JLabel mapMoveLabel;
 	
 	MmMapStationMarkers markersDialog;
 	
 	int iconWidth,iconHeight,index;
 	
 	Point pnt;
 
 	public MmAddEvents events;
 	public MmAddGlobalMarkers globalMarkers;
 	
 	public MmAccordionMenu menuItems;
 	
 	MmStartEvents startEvents;
 	MmAddEventsDialog eventProperties;
 	
 	JPanel markerPanel;
 	
 	public JFrame frame1;
 	
 	ArrayList<MmAddEventsDialog> mapMarkers; 
 	
 	public int markersPlaced=0;
 	
 	MmMapViewer mapView;
 	
 	MmMapStationMarkers mapMarkerWindow;
 	
 	ArrayList<Point> geoPnts;
 	
 	boolean lineDrawn = false;
 	
 	public int indexPos=0;
 	
 	public boolean isPointPresent=false;
 	
 	ImageIcon icon;
 	
 	boolean isSaved;
 	
 	String ledImage = null;
 	
 	//checks is mouse clicked on line drawn between stations
 	boolean isPointPickedOnLine = false;
 	
 	//variable to check if print option selected
 	public boolean isPrintSelected=false;
 	
 	//variable to check if print option selected
 	public boolean isPointOnSwingPoint=false;
 	
 	
 	//index of current swing point
 	int swingPointIndex = -1;
 	
 	//variable for current swing point selected
 	Point moveSwingPoint;
 	
 	//variable for edge clicked 
 	int clickedEdge = -1;
 	
 	//variable for current station point
 	Point currentStationPoint;
 	
 	//marker iamges
 	ImageIcon[] icons = new ImageIcon[6];
 	
 	//adds all initial actions
 		
 	JSONObject initializeEvents = new JSONObject();
 	JSONObject initializeActions = new JSONObject();
 	JSONArray action;
 	
 	JSONArray startActionArray;
 	
 	//key:552b7cf0010662a89bcbd90b9727186c4fb97ffe7883e11a2ebba59765d7f461
 	
 	double latitude,longitude;
 	
 	int geoIndex = -1;
 	
 	//geo distance between two points
 	float geoDistance = -1;
 	
 	JSONArray readJSONObjects;
 	
 	MmGenericEvent genericEvent = new MmGenericEvent("launch");
 	
 	public JWindow window = new JWindow();
 	
 	JFrame markerFrame2 = new JFrame();
 	
 	JFrame markerFrame = new JFrame();
 	
 	JLabel frameLabel = new JLabel();
 	
 	JFrame mainWindow;
 	
 	//map markers dialog
 	JDialog mapMarkersDialog;
     
 	//events dialog
 	JDialog eventsDialog;
 
 	//file open
 	boolean isFileOpen;
 
     //language settings
    	int language;
 	
 
 	
 
 	public MmMapViewer()
 	{
 		panel=this;
 		
 		mainWindow = new JFrame();
 		
 	   
 	    
 	    mapMarkers = new ArrayList<MmAddEventsDialog>();
 	    
 	    icon = new ImageIcon("/Users/Umapathi/Desktop/MinnesmarkEditor/images/map_marker.png");
 	    		
 		mapKit = new JXMapKit();
 		
 			    		
 		mapKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
 		
 		mapKit.setAddressLocationShown(false);
 		mapKit.setDataProviderCreditShown(false);
 		
 		
 		
 		markerPanel = new JPanel();
 		markerPanel.add(new JLabel(icon));
 		markerPanel.add(new JLabel(icon));
 		
 		
 		
 		markerFrame.setAlwaysOnTop(true);
 		markerFrame.setUndecorated(true);
 		markerFrame.setBackground(new Color(255,255,255,0));
 		markerFrame.pack();
 		markerFrame.setSize(50,50);
 		
 		
 				
 		for(int i=0;i<6;i++)
 		{
 			icons[i] = new ImageIcon(getClass().getResource("/map_marker_"+Integer.toString((i+1))+".png"));
 		}
 				
 		locLabel = new JLabel("coords");
 		locLabel.setVisible(false);
 				
 		wayPoints = new HashSet();
 		
 		wayLines = new HashSet();
 		
 	    //geoCoordReader = new MmGPSCoordReader("/Users/Umapathi/Desktop/MinnesmarkEditor/xml/track2.gpx");
 		
 		//mapKit.getMainMap().setCenterPosition(new GeoPosition(58.39748,15.57348));
 		//mapKit.getMainMap().setAddressLocation(new GeoPosition(58.39748,15.57348));
 	    
 	    
 	    
 	    getMapCurrentLocation();
 	    
 	    
 	    
 	    //mapKit.getMainMap().setCenterPosition(new GeoPosition(56.034054,12.738867));
 		//mapKit.getMainMap().setAddressLocation(new GeoPosition(56.034054,12.738867));
 		
 			
 		mapKit.setZoomSliderVisible(false);
 		mapKit.setZoomButtonsVisible(false);
 		mapKit.getZoomInButton().setAlignmentY(TOP_ALIGNMENT);
 		mapKit.setMiniMapVisible(false);
 		mapKit.getMainMap().setZoom(1);
 		mapKit.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		((DefaultTileFactory)mapKit.getMainMap().getTileFactory()).setThreadPoolSize(8);
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		mapKit.setPreferredSize(new Dimension(toolkit.getScreenSize().width-460,toolkit.getScreenSize().height-200));
 		add(mapKit);
 		mapKit.setAddressLocationShown(true);
 		mapKit.getMainMap().setPanEnabled(true);
 		mapKit.getMainMap().setZoomEnabled(false);
 		
 		    
 	    GridBagConstraints constraints = new GridBagConstraints(); 
 	    
 	    constraints.anchor = GridBagConstraints.NORTH;
 		constraints.fill = GridBagConstraints.HORIZONTAL;
 		constraints.gridx=0;
 		constraints.gridy=0;
 		constraints.weighty=0.25;
 		constraints.insets = new Insets(225,625,175,125);
 		
 		
 	    
 	    //mapKit.getMainMap().add(markerPanel,constraints);
 	    markerPanel.setLocation(300,300);
 	    
 	    //JOptionPane.showMessageDialog(mapKit, mapKit.getMainMap().getComponentCount());
 	    //mapKit.getMainMap().getComponent(2).setLocation(300,300);
 	    //JOptionPane.showMessageDialog(mapKit, mapKit.getMainMap().getComponent(2).getLocation());
 		
         mapKit.getMainMap().addMouseMotionListener(new MouseMotionListener(){
 	    	
     	    @Override
 	    	public void mouseMoved(MouseEvent e)
 			{
     	    	
     	    	Toolkit toolKit = Toolkit.getDefaultToolkit();
     	    	
     	    	    	    	
     	    	if(isMouseOnStation(e.getPoint()))
     	    	{	
     	    	    Image curs_image = new ImageIcon(getClass().getResource("/station_move1.png")).getImage();
     	    	    Cursor cursor = toolKit.createCustomCursor(curs_image, new Point(0,0), "cursor");
 				    mapKit.getMainMap().setCursor(cursor);
 				    
 				    if(station_index!=-1)
 				    {	
 				       
 				       if(!events.getStations().get(station_index).getStationType() && events.getStations().get(station_index).getStationsEvents().isEmpty())
 				       {	
 				           JComponent cmp = (JComponent)e.getSource();
 				           cmp.setToolTipText(MmLanguage.language_media[language][0]);
 				       }
 				       else if(station_index!=-1)
 				       {
 				    	   JComponent cmp = (JComponent)e.getSource();
 				    	   cmp.setToolTipText(null);
 				           cmp.setToolTipText(events.getStations().get(station_index).getStationsEvents());
 				           
 				       }
 				    }    
     	    	}
     	    	else if(isMouseOnLine(e.getPoint()))
     	    	{	
     	    		Image curs_image = new ImageIcon(getClass().getResource("/line_move.png")).getImage();
     	    	    Cursor cursor = toolKit.createCustomCursor(curs_image, new Point(0,0), "cursor");
 				    mapKit.getMainMap().setCursor(cursor);
 				    JComponent cmp = (JComponent)e.getSource();
 				    cmp.setToolTipText("Distance "+Float.toString(geoDistance)+"m");
 				    isPointOnSwingPoint = false;
 				    
     	    	}
     	    	else if(isMouseOnSwingPoint(e.getPoint()))
     	    	{
     	    		mapKit.getMainMap().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
     	    		JComponent cmp = (JComponent)e.getSource();
 				    cmp.setToolTipText("Sväng");
 				    isPointOnSwingPoint = true;
 				    
     	    	} 
     	    	else
     	    	{	
     	    		mapKit.getMainMap().setCursor(new Cursor(Cursor.HAND_CURSOR));
     	    		
     	    		JComponent cmp = (JComponent)e.getSource();
 				    cmp.setToolTipText(null);
 				    isPointOnSwingPoint = false;
 				    
     	    	}	
     	    	
 				mapKit.getMainMap().setPanEnabled(true);
 				mapKit.getMainMap().revalidate();
 				
 				/*if(e.getButton()==3)
 				{
 					mapKit.getMainMap().setCursor(new Cursor(Cursor.HAND_CURSOR));
 				}*/
 			}
 			
     	    @Override
 			public void mouseDragged(MouseEvent event) {
     	    	
     	    	 
     	    	
     	    	mouseDragged=true;
     	    	
     	    	//mapMarkerWindow.showMarkers();
     	    	
     	    	
     	    	/*if(mousePos.get(index).isPointInRegion(new MmMousePoints(e.getX(),e.getY())))
 				{
 					
 					window.setVisible(true);
 					window.add(new JLabel("Marker"));
 					window.setLocation(e.getPoint());
 	    	    	window.setAlwaysOnTop(true);
 	    	    	window.toFront();
 	    	    	window.repaint();
 	    	    	mapKit.getMainMap().repaint();
 				}*/
     	    	
     	    	
     	    	/*for(int i=0;i<mousePos.size();i++)
         		{ 
         			isPointPresent = mousePos.get(i).isPointInRegion(new MmMousePoints(event.getX(),event.getY()));
         			
         		
         			
         			if(!isPointPresent)
         				continue;
         			else
         			{
         				isPointPresent=true;
         				index=i;
         				      				   				
         				break;
         			}
         					
         		}*/
     	    	
     	    	
     	    	
     	    	Toolkit toolKit = Toolkit.getDefaultToolkit();
     	    	
     	    	if(isPointPresent)
     	    	{
     	    	   
     	    	   Image curs_image = new ImageIcon(getClass().getResource("/station_move1.png")).getImage();
    	    	       Cursor cursor = toolKit.createCustomCursor(curs_image, new Point(0,0), "cursor");
 				   
    	    	       mapKit.getMainMap().setCursor(cursor);	
     	    	   movingStation = 	getGeoPosition(event.getPoint());
     	    	   geoPos.remove(index);	
     	    	   geoPos.add(index,getGeoPosition(event.getPoint()));
     	    	   mousePos.get(index).setPoint(event.getPoint());
     	    	   if(!swingPos.isEmpty() && edgeIndex!=-1)
     	    	   {
     	    		   //System.out.println("edge index "+swingPos.get(edgeIndex).getEdgeIndex());
     	    		   //System.out.println("edge index "+edgeIndex+ "  "+clickedEdge);
     	    		   if(clickedEdge==0)
     	    		   {
     	    			   
     	    			   swingPos.get(edgeIndex).setStartPoint(mousePos.get(index).getPoint());
     	    			   swingPos.get(edgeIndex).setStartGeoPosition(getGeoPosition(mousePos.get(index).getPoint()));
     	    			   //System.out.println("geo index "+geoPos.get(index)+"  "+getGeoPosition(swingPos.get(edgeIndex).getStartPoint())+"  "+index);
     	    			   events.updateStationGeoPosition(edgeIndex, geoPos.get(index));
     	    			   
     	    		   }
     	    		   
     	    		   if(clickedEdge==1)
     	    		   {
     	    			   
     	    			   swingPos.get(edgeIndex).setEndPoint(mousePos.get(index).getPoint());
     	    			   swingPos.get(edgeIndex).setEndGeoPosition(getGeoPosition(mousePos.get(index).getPoint()));
     	    			   //JOptionPane.showMessageDialog(null, "Edge index "+edgeIndex+" "+index);
     	    			   if(edgeIndex==0 && index == 0)
     	    			   {
     	    				   events.updateStationGeoPosition(edgeIndex, geoPos.get(index));
     	    			   }
     	    			   
     	    			   if(edgeIndex!=events.getStations().size()-1)
     	    			   {
     	    				   //System.out.println("edge index "+(edgeIndex+1));
     	    				   events.updateStationGeoPosition(edgeIndex+1, geoPos.get(index));
     	    			   }
     	    			  	    			   
     	    		   }
     	    	   	    		   
     	    	   }
     	    	   else
     	    	   {
     	    		   events.updateStationGeoPosition(index, geoPos.get(index));
     	    	   }
     	    	   
     	    	   drawPoints();
     	    	   
     	    	}   
     	    	
     	    	
     	    	if(isPointPickedOnLine && !isPointPresent)
     	    	{
     	    		
     	    		//Image curs_image = new ImageIcon(System.getProperty("user.dir")+"/images/line_move.png").getImage();
     	    	    //Cursor cursor = toolKit.createCustomCursor(curs_image, new Point(0,0), "cursor");
 				    mapKit.getMainMap().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
 				    
 				        	    	      	    	   
     	    	   if(!swingPos.isEmpty())
     	    	   {
     	    		  swingPos.get(edgeIndex).setEndPoint(event.getPoint());
     	    		  swingPos.get(edgeIndex).setEndGeoPosition(getGeoPosition(event.getPoint()));
     	    		  //swingPoints.set(swingPointIndex,swingPos.get(edgeIndex).getEndGeoPosition());
     	    		  swingPos.get(edgeIndex+1).setStartPoint(event.getPoint());
     	    		  swingPos.get(edgeIndex+1).setStartGeoPosition(getGeoPosition(event.getPoint()));
     	    		  events.updateSwingPoints(edgeIndex+1, swingPos.get(edgeIndex).getEndGeoPosition().getLatitude(), swingPos.get(edgeIndex).getEndGeoPosition().getLongitude());
     	    	   }
     	    	   
     	    	   //swingPoints.remove(swingPointIndex);
     	    	       	    	   
     	    	   drawPoints();
     	    	}
     	    	
     	    	if(eventsDialog.isVisible())
     	    	{	
     	    	    //frame1.toFront();
     	    	
     	       	    mapKit.getMainMap().repaint();
     	    	
     	    	    if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()<190)
     	    	    {	
     	    	    	eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety()+50);
     	    	    }
 				
 				    if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()>190)
     	      	    {	
 				    	eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety());
     	    	    }
 				
 				    if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()>590)
     	    	    {	
 				    	eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety()-100);
     	    	    }
     	    	
     	    	    if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()>190)
     	    	    {	
     	    	    	eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety());
     	    	    }
     	    	
     	    	    if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()<190)
     	    	    {	
     	    	    	eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety()+50);
     	    	    } 
     	    	
     	    	    if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()>590)
     	    	    {	
     	    	    	eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety()-100);
     	    	    }
     	    	
     	    	}    
     	    	
     	    	
     	    	
     	    	if((event.getX()>0 && event.getX()<400) && (event.getY()>0 && event.getY()<125))
     	    	{
     	    		   	    		
     	    	   	frameLabel.setIcon(icons[index]);
     	    		int x = event.getPoint().x - frameLabel.getWidth()/2;
  				    int y = event.getPoint().y - frameLabel.getHeight()/2;
  				    Point pt = new Point(x,y);
  				
  				    SwingUtilities.convertPointToScreen(pt, frameLabel); 
  				    
  				    
     	    		markerFrame.add(frameLabel);
     	    		
     	    		//JOptionPane.showMessageDialog(null,markerFrame.getComponentCount());
  				    
  				    
     	    		markerFrame.setVisible(true);
     	    		markerFrame.validate();
     	    		markerFrame.setLocation(event.getX()+400,event.getY()+50);
     	    		
     	    	}
     	    	else
     	    	{
     	    		markerFrame.setVisible(false);
     	    	}
     	    	
     	    	
 		    }
 	    });
         
         mapKit.getMainMap().addMouseListener( new MouseListener(){
         	
         	public void mouseClicked(MouseEvent e)
         	{
         		        		        		
         		for(int i=0;i<mousePos.size();i++)
         		{ 
         			isPointPresent = mousePos.get(i).isPointInRegion(new MmMousePoints(e.getX(),e.getY()));
         			
         			/*if(e.getClickCount()==2)
         			{
         				JOptionPane.showMessageDialog(null, e.getX()+" "+e.getY());
         				eventsDialog.setVisible(false);
         			}*/
         			
         			if(!isPointPresent)
         				continue;
         			else
         			{
         				isPointPresent=true;
         				eventsDialog.setVisible(true);
         				events.setGeoIndex(index);
         				events.updateStationEvents(geoPos.get(index));
         				index=i;
         				break;
         			}	
         					
         		}
         		
         		int swingPointsIndex = -1;
         		
         		
         		/*if(isMouseOnSwingPoint(e.getPoint()) && e.getButton()==3)
         		{
         			//JOptionPane.showMessageDialog(null, "Entered ");
         	        for(int i=0;i<swingPos.size();i++)
         	        {
         	        	if(swingPos.get(i).isSwingPointPresent(e.getPoint()))
         	        	{
         	        		
         	        		 int option = JOptionPane.showConfirmDialog(null, "Vill du ta bort swing point");
         	        		 if(option==JOptionPane.OK_OPTION)
         	        		 {	 
         	        	         //swingPointsIndex = swingPoints.indexOf(getGeoPosition(swingPos.get(i).getEndPoint()));
         	        	         //JOptionPane.showMessageDialog(null, swingPoints.size()+"  "+swingPointsIndex);
         	        			          	        			 
         	        			 swingPos.get(i).setEndPoint(swingPos.get(i+1).getEndPoint());
         	        	         events.removeSwingPoint(i+1);
         	        	         //swingPoints.remove(swingPointsIndex);
         	        	         swingPos.remove(i+1);
             	        	     
             	        	     drawPoints();
         	        		 }    
         	        	      
         	        	     break;
         	        	}
         	        }
         			
         			 int option = JOptionPane.showConfirmDialog(null, "Vill du ta bort swing point");
 	        		 if(option==JOptionPane.OK_OPTION)
 	        		 {
         			     swingPos.get(swingPointIndex).setEndPoint(swingPos.get(swingPointIndex+1).getEndPoint());
         	             events.removeSwingPoint(swingPointIndex+1);
         	             swingPos.remove(swingPointIndex+1);
 	        		 }    
 	        	     
 	        	     drawPoints();
         		}*/
         			
         		        		
         		if(swingPointsIndex == -1)
         		{
         			
         			/*JOptionPane.showMessageDialog(null, "data "+e.getButton());
         			if(e.getButton()==3)
             		{		
             		   int option = JOptionPane.showConfirmDialog(null, "Ta bort markör", "markör", JOptionPane.YES_NO_OPTION);
             		   
             		   if(option==JOptionPane.OK_OPTION)
             		   {
             			  //JOptionPane.showMessageDialog(null, index);   
             			  geoPos.remove(index);
             			  mousePos.remove(index);
             			  if(!events.getStations().isEmpty())
             			      events.getStations().remove(index);
             			  drawPoints();
             			  mapMarkerWindow.hideMarker(index);
             			  mapMarkerWindow.isDeleted=true;
             			  if(mousePos.size()==0)
             			  {
             				  mousePos.clear();
             				  geoPos.clear();
             				  events.getStations();
             				  mapMarkerWindow.resetMapMarkers();
             			  }
             			  
             			  
             		   }
             		}*/
         			
         			if(eventsDialog.isVisible())
         			{	
         			  	
         				if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()<190)
             	    	{	
         					eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety()+50);
             	    	}
         				
         				if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()>190)
             	    	{	
         					eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety());
             	    	}
         				
         				if(mousePos.get(index).getx()<368 && mousePos.get(index).gety()>=598)
             	    	{	
         					eventsDialog.setLocation(mousePos.get(index).getx()+450, mousePos.get(index).gety()-100);
             	    	}
             	    	
             	    	if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()>190)
             	    	{	
             	    		eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety());
             	    	}
             	    	
             	    	if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()<190)
             	    	{	
             	    		eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety()+50);
             	    	}
             	    	
             	    	if(mousePos.get(index).getx()>368 && mousePos.get(index).gety()>=598)
             	    	{	
             	    		eventsDialog.setLocation(mousePos.get(index).getx()-25, mousePos.get(index).gety()-100);
             	    	}
             	    	
             	    	/*if(isPointPresent)
             	    	{	
         		            frame1.setVisible(true);
         		            //events.addStation(geoPos.get(index).getLatitude(), geoPos.get(index).getLongitude(), index);
         		            events.updateStationEvents(geoPos.get(index));
             	    	}*/    
         		       
         			}
         		}
         	      	  
         	}
         	
         	public void mouseExited(MouseEvent e){ }
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent event) {
 				// TODO Auto-generated method stub
 								
 				for(int i=0;i<mousePos.size();i++)
         		{ 
         			isPointPresent = mousePos.get(i).isPointInRegion(new MmMousePoints(event.getX(),event.getY()));
         			
         			if(!isPointPresent)
         				continue;
         			else
         			{
         				currentStationPoint = mousePos.get(i).getPoint();
         				isPointPresent=true;
         				
         				index=i;
         				mapKit.getMainMap().setPanEnabled(false);
         				if(!swingPos.isEmpty())
         				{
         					        					
         					for(int j=0;j<swingPos.size();j++)
         					{
         					    
         					    clickedEdge = swingPos.get(j).getEdgeIndex(mousePos.get(index).getPoint()); 
         						if(clickedEdge!= -1)
         						{
         							edgeIndex = j;
         							break;
         						}
                                      				
         					}
         				}
         				
         				break;
         			}	
         			
            		}
 				
 				if(!swingPos.isEmpty() && !isPointPresent)
 				{
 					//mapKit.getMainMap().setPanEnabled(false);
 					for(int i=0;i<swingPos.size();i++)
 					{
 						if(swingPos.get(i).isSwingPointPresent(event.getPoint()))
 						{
 							mapKit.getMainMap().setPanEnabled(false);
 							
 							isPointPickedOnLine = true;
 							moveSwingPoint = event.getPoint();
 							edgeIndex = i;
 							break;
 							
 						}
 					}
 				}
 				
 				//if(swingPointIndex==-1)
 				//{
 				    boolean pnt = isPointOnLine(event.getPoint());
 				    //System.out.println("data "+pnt+"  "+isPointPresent+" "+isPointPickedOnLine);
 				
 				    if(pnt && !isPointPresent && !isPointPickedOnLine)
 				    {	
 				    	mapKit.getMainMap().setPanEnabled(false);
 					    isPointPickedOnLine = true;
 					    //System.out.println("clicked on point ");
 					    //swingPoints.add(getGeoPosition(event.getPoint()));
 					    
 					    
 					    //if(!swingPoints.isEmpty())
 					    //{
 					    	 //swingPointIndex=swingPoints.indexOf(getGeoPosition(event.getPoint()));
 					    	 //System.out.println("swing index "+edgeIndex);
 					    	 int stationIndex;
 					    	 
 					    	 
 					    	 //if(swingPos.size()==1)
 					    	 {	 
 					    		 
 					    		 moveSwingPoint = swingPos.get(edgeIndex).getEndPoint();
 					    		 stationIndex = swingPos.get(edgeIndex).getStationIndex();
 						    	 //System.out.println("Station index "+stationIndex+"  "+(edgeIndex+1));
 					    	     events.insertStation(swingPos.get(edgeIndex).getEndGeoPosition().getLatitude(), swingPos.get(edgeIndex).getEndGeoPosition().getLongitude(), edgeIndex+1,stationIndex, 10, true);
 					    	 }    
 					    	  
 					    	 
                         //}
 					    
 				    }
 				    else if(event.getButton()==3 && isMouseOnSwingPoint(event.getPoint()))
         		    {
         			   //JOptionPane.showMessageDialog(null, "Entered ");
         	           for(int i=0;i<swingPos.size();i++)
         	           {
         	        	  if(swingPos.get(i).isSwingPointPresent(event.getPoint()))
         	        	  {
        	        		
        	        		 int option = JOptionPane.showConfirmDialog(null, MmLanguage.language_exception[language][4]);
         	        		 //JOptionPane.showMessageDialog(null, "swing pos size "+swingPos.size());
         	        		 if(option==JOptionPane.OK_OPTION)
         	        		 {	 
         	        			 isPointPickedOnLine = false;   
         	        			 MmSwingPoints swgPnts = new MmSwingPoints();
         	 					 swgPnts.setStartPoint(swingPos.get(i).getStartPoint());
         	 					 swgPnts.setStartGeoPosition(getGeoPosition(swingPos.get(i).getStartPoint()));
         	 					 swgPnts.setEndPoint(swingPos.get(i+1).getEndPoint());
         	 					 swgPnts.setEndGeoPosition(swingPos.get(i+1).getEndGeoPosition());
         	 					 swgPnts.setStationIndex(swingPos.get(i).getStationIndex());
         	 					 swgPnts.setEndPointType(true);
         	 					 
         	 					 swingPos.set(i,swgPnts);
         	 					 
         	        	         events.removeSwingPoint(i+1);
         	        	         swingPos.remove(i+1);
         	        	         //swingPos.get(swingPos.size()-1).print();
         	        	         //System.out.println("data "+swingPos.size()+"  "+ swingPos.get(swingPos.size()-1).getStartPoint()+" "+swingPos.get(swingPos.size()-1).getEndPoint());
             	        	     //JOptionPane.showMessageDialog(null, "swing pos size "+swingPos.size());
         	        	         drawPoints();
             	        	     break;
         	        		 }    
         	        	      
         	        	     
         	        	}
         	        }
         	        
         			System.out.println("data ");
         	        //swingPos.get(swingPos.size()-1).print();
         	        System.out.println("data1 ");
         			
         		}
 				
 				//
 				    //bringTofront();    
 				//frame1.toFront();
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				if(isPointPresent)
 				{
 					
 					if((event.getX()>0 && event.getX()<400) && (event.getY()>0 && event.getY()<125))
 					{
 																	
 						int option = JOptionPane.showConfirmDialog(null, MmLanguage.language_exception[language][5],MmLanguage.language_exception[language][6],JOptionPane.YES_NO_OPTION);
 						
 						if(option == JOptionPane.OK_OPTION)
 						{
 							markerFrame.setVisible(false);
 							mapMarkerWindow.hideMarker(index);
 							
 							if(swingPos.size()==1)
 								swingPos.remove(swingPos.size()-1);
 							
 							if(swingPos.size()>1)
 							{
 								deleteStations_WayPoints(mousePos.get(index).getPoint());
 							}
 							
 							geoPos.remove(index);
 							mousePos.remove(index);
 							if(index==0)
 							    events.removeStation(index);
 							else
 								events.removeStation((edgeIndex+1));
 							isStationDelete = true;
 						}
 						
 						
 						if(option == JOptionPane.NO_OPTION)
 						{
 							markerFrame.setVisible(false);
 							//geoPos.remove(index);
 							geoPos.set(index,getGeoPosition(currentStationPoint));
 							if(!swingPos.isEmpty())
 							{	
 								if(clickedEdge==0)
 			    	    		{
 			    	    			   
 			    	    			   swingPos.get(edgeIndex).setStartPoint(currentStationPoint);
 			    	    			   swingPos.get(edgeIndex).setStartGeoPosition(getGeoPosition(currentStationPoint));
 			    	    			   //System.out.println("geo index "+geoPos.get(index)+"  "+getGeoPosition(swingPos.get(edgeIndex).getStartPoint())+"  "+index);
 			    	    			   events.updateStationGeoPosition(edgeIndex, geoPos.get(index));
 			    	    			   
 			    	    		}
 			    	    		   
 			    	    		if(clickedEdge==1)
 			    	    		{
 			    	    			   
 			    	    			   swingPos.get(edgeIndex).setEndPoint(currentStationPoint);
 			    	    			   swingPos.get(edgeIndex).setEndGeoPosition(getGeoPosition(currentStationPoint));
 			    	    			   
 			    	    			   if(edgeIndex!=swingPos.size()-1)
 			    	    			   {
 			    	    				   events.updateStationGeoPosition(edgeIndex, geoPos.get(index));
 			    	    			   }
 			    	    			   else
 			    	    				   events.updateStationGeoPosition(edgeIndex+1, geoPos.get(index));
 			    	    			   
 			    	    		}
 							    
 							}  
 							else
 			    	    	{
 			    	    		events.updateStationGeoPosition(index, geoPos.get(index));
 			    	    	}
 						}
 						
 						
 					}
 					
 					if(!isStationDelete &&!checkDistance() && geoPos.size()>1)
 					{
 						JOptionPane.showMessageDialog(null, MmLanguage.language_exception[language][1]);
 						
 						if(index==0)
 						{	
 						    geoPos.set(index,getGeoPosition(currentStationPoint));
 						    swingPos.get(edgeIndex).setStartPoint(currentStationPoint);
 					  	    swingPos.get(edgeIndex).setStartGeoPosition(getGeoPosition(currentStationPoint));
 					  	    events.updateStationGeoPosition(edgeIndex, getGeoPosition(currentStationPoint));
 						}
 						else
 						{
 							geoPos.set(index,getGeoPosition(currentStationPoint));
 						    swingPos.get(edgeIndex).setEndPoint(currentStationPoint);
 					  	    swingPos.get(edgeIndex).setEndGeoPosition(getGeoPosition(currentStationPoint));
 					  	    events.updateStationGeoPosition(edgeIndex+1, getGeoPosition(currentStationPoint));
 						}
 					}
 				}
 				
 				if(isPointPickedOnLine && !isPointPresent)
 				{
 					
 					isPointPickedOnLine = false;
 					if(!swingPos.isEmpty())
 					{
 												
 						if(!isPointMoveableToCurrentLocation(event.getPoint()))
 						{
 							
 							swingPos.get(edgeIndex).setEndPoint(moveSwingPoint);
 							swingPos.get(edgeIndex).setEndGeoPosition(getGeoPosition(moveSwingPoint));
 							swingPos.get(edgeIndex+1).setStartPoint(moveSwingPoint);
 							swingPos.get(edgeIndex+1).setStartGeoPosition(getGeoPosition(moveSwingPoint));
 							
 							events.updateSwingPoints(edgeIndex+1, swingPos.get(edgeIndex).getEndGeoPosition().getLatitude(), swingPos.get(edgeIndex).getEndGeoPosition().getLongitude());
 							
 							//JOptionPane.showMessageDialog(null, "swing index "+swingPointIndex);
 							//swingPoints.remove(swingPointIndex);
 							//swingPoints.add(swingPointIndex,getGeoPosition(moveSwingPoint));
 							
 							JOptionPane.showMessageDialog(null, MmLanguage.language_exception[language][2]);
 							
 							if(!checkSwingPointsDistance(swingPos.get(edgeIndex).getEndGeoPosition()))
 							{
 								swingPos.get(edgeIndex).setEndPoint(swingPos.get(edgeIndex+1).getEndPoint());
 								swingPos.get(edgeIndex).setEndGeoPosition(swingPos.get(edgeIndex+1).getEndGeoPosition());
 								
 								events.updateSwingPoints(edgeIndex+1, swingPos.get(edgeIndex).getEndGeoPosition().getLatitude(), swingPos.get(edgeIndex).getEndGeoPosition().getLongitude());
 								//swingPos.remove(edgeIndex+1);
 								//swingPoints.remove(swingPointIndex);
 							}
 						}
 						
 							
 						
 					}
 				}
 								
 				isPointPresent = false;
 				isPointPickedOnLine=false;
 				swingPointIndex=-1;
 				edgeIndex = -1;
 				drawPoints();
 				
 			}
         	
 			
         });
         
         mapKit.getMainMap().addKeyListener(new KeyListener(){
 
 			@Override
 			public void keyPressed(KeyEvent key) {
 				// TODO Auto-generated method stub
 				int id = key.getID();
 				
 				
 			    if(id==KeyEvent.VK_DELETE)
 			    {
 			    	if(isPointOnSwingPoint)
 			    	{
 			    		 swingPos.get(swingPointIndex).setEndPoint(swingPos.get(swingPointIndex+1).getEndPoint());
 	        	         events.removeSwingPoint(swingPointIndex+1);
 	        	         swingPos.remove(swingPointIndex+1);
 		        	     
 		        	     drawPoints();
 			    	}
 			    }
 			    	
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
         	
         });
         
 	}
 	
 	public int getLanguage() {
 		return language;
 	}
 
 
 	public void setLanguage(int language) {
 		this.language = language;
 	}
 
 	public void setMarkerDialogText()
 	{
 		mapMarkerWindow.setLanguage(language);
 		mapMarkerWindow.setTitleLabel();
 		
 		events.setLanguage(language);
 		
 		events.setLanguageText();
 		
 	}
 	
 	public JFrame getMainWindow() {
 		return mainWindow;
 	}
 
 	public void setMainWindow(JFrame mainWindow) {
 		this.mainWindow = mainWindow;
 	}
 	
 	public JDialog getMapMarkersDialog() {
 		return mapMarkersDialog;
 	}
 
 	public boolean isFileOpen() {
 		return isFileOpen;
 	}
 
 
 	public void setFileOpen(boolean isFileOpen) {
 		this.isFileOpen = isFileOpen;
 	}
 
 
 	public void setMapMarkersDialog(JDialog mapMarkersDialog) {
 		this.mapMarkersDialog = mapMarkersDialog;
 	}
 	
 	public void showDialog()
 	{
 		
 		
 		//map markers window 
 		
         mapMarkersDialog = new JDialog(mainWindow);
 	    
 	    mapMarkersDialog.setSize(300, 125);
 	    mapMarkersDialog.setLocation(425,75);
         
 		mapMarkerWindow = new MmMapStationMarkers(mapMarkersDialog,this);
 		mapMarkerWindow.setLanguage(language);
 		
 		mapMarkersDialog.getContentPane().add(mapMarkerWindow);
 		
 		mapMarkersDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		
 		mapMarkersDialog.setResizable(false);
 		
 		mapMarkersDialog.setVisible(true);
 		
 		mapMarkersDialog.pack();
 		
 		
 		eventsDialog = new JDialog(mainWindow);
 		
 		
 		
 	    //frame1 = new JFrame();
 	    //frame1.setAlwaysOnTop(true);
 		
 		//eventsDialog = new JDialog(window);
 		        
 		events = new MmAddEvents(eventsDialog);
 		
 		events.setMainWindow(mainWindow);
 		events.setLanguage(language);
 		    
 		events.setOpaque(true);
 		events.setMap(this);
 		    
 		        	    
 		    
 		eventsDialog.pack();
 		eventsDialog.setSize(425, 200);
 		eventsDialog.setVisible(false);
 		
 		
 		
 	    
 	   /* markerFrame2.setSize(300, 125);
 	    markerFrame2.setLocation(425,75);
 	    markerFrame2.getContentPane().add(mapMarkerWindow);
 	    //markerFrame2.setAlwaysOnTop(true);
 	    
 	    markerFrame2.setVisible(true);
 	    markerFrame2.pack();*/
 	    
 	    
 	    
 	    
 	    
 	}
 	
 	public void bringTofront()
 	{
 		//markerFrame2.toFront();
 		/*if(!markerFrame2.isFocused())
 		{	
 			//markerFrame2.toBack();
 		    markerFrame2.toFront();
 		}
 		
 		if(eventsDialog.isVisible())
 		{	
 			eventsDialog.toFront();
 		   
 		} */
 	}
 	
 	//delets the station and way points 
 	public void deleteStations_WayPoints(Point point)
 	{
 		Point nextStationPoint;
 		
 		Point swingPoint = new Point();
 		
 		final int numIndices = swingPos.size();
 		
 		int indices[] = new int[numIndices];
 		
 		for(int i=0;i<numIndices;i++)
 		{
 			indices[i] = -1;
 		}
 		
 		if(!mousePos.isEmpty())
 		{
 			if(index==mousePos.size()-1)
 			   nextStationPoint = mousePos.get(index-1).getPoint();
 			else
 			   nextStationPoint = mousePos.get(index+1).getPoint();	
 		}
 		
 		//for(int i=0;i<swingPos.size();i++)
 			 //swingPos.get(i).print();
 		
 		
 		
 		if(index==0)
 		{
 			 int swgIndx = swingPos.get(index).getStationIndex();
 			 			 		 
 			 if(swingPos.get(index).getStartGeoPosition().equals(geoPos.get(index)))
 		     {
 		         swingPoint.x = swingPos.get(index).getEndPoint().x;
 		    	 swingPoint.y = swingPos.get(index).getEndPoint().y;
 		    	 swingPos.remove(index);
 		    	    		    	
 		     }	
 			 
 			 int i=index;
 			 
 			 //remove all swing points with same station index
 			 while(!swingPos.isEmpty())
 			 {
 				 if(swingPos.get(i).getStationIndex()==swgIndx)
 				 {
 					 //if(swingPoints.indexOf(swingPos.get(i).getStartGeoPosition())!=-1)
 					 //{
 						 //swingPoints.remove(swingPos.get(i).getStartGeoPosition());
 						 swingPos.remove(i);
 						 i=index;
 					 //}
 					 //else
 					    //return;	 
 					 
 				 }
 				 else
 					 return;
 			 }
 			 
 			 //update remaining station indices
 			 for(i=0;i<swingPos.size();i++)
 			 {
 				 swingPos.get(i).setStationIndex(swingPos.get(i).getStationIndex()-1);
 			 }
 				    
 		}
 		
 				
 		//System.out.println("station point "+point+"  "+(edgeIndex+1));
 		
 		if((edgeIndex+1)>0 && (edgeIndex+1)<=swingPos.size()-1)
 		{
 			Point pnt1 = new Point(point.x,point.y+1);
 			for(int i=0;i<swingPos.size();i++)
 			{
 				//System.out.println("station point "+point+"  "+pnt1);
 				//swingPos.get(i).print();
 				
 			     if(swingPos.get(i).getEndPoint().equals(point) || (swingPos.get(i).getEndPoint().equals(pnt1)))
 			     {
 			    	 swingPos.get(i).setEndPoint(swingPos.get(i+1).getEndPoint());
 			    	 swingPos.get(i).setEndGeoPosition(swingPos.get(i+1).getEndGeoPosition());
 			         swingPos.remove(i+1);
 			         return;
 			    	    		    	
 			     }
 			     
 		   }        
 			
 		}
 		
 		if(edgeIndex==swingPos.size()-1)
 		{
 		   	
 		   int swingIndx = swingPos.get(edgeIndex).getStationIndex();	
 		   swingPos.remove(edgeIndex);
 		   int indx = swingPos.size()-1;
 		   
 		   while(!swingPos.isEmpty())
 		   {
 			   if(swingPos.get(indx).getStationIndex()==(swingIndx))
 			   {
 				   //swingPoints.remove(swingPos.get(indx).getStartGeoPosition());
 				   swingPos.remove(indx);
 				   indx = swingPos.size()-1;
 				   
 			   }
 			   else
 				   return;
 			   
 		   }
 		  
 		   /*Point pnt1 = new Point(point.x,point.y+1);
 		   for(int i=swingPos.size()-1;i>=0;)
 		   {
 			   
 		        if(swingPos.get(i).getEndPoint().equals(point)||swingPos.get(i).getEndPoint().equals(pnt1))
 		        {
 		    	    swingPoint.x = swingPos.get(i).getStartPoint().x;
 		    	    swingPoint.y = swingPos.get(i).getStartPoint().y;
 		    	    swingPos.remove(i);
 		    	    i--; 
 		        }
 		        
 		        		        
 		        swingPos.get(i).print();
 		        System.out.println("Swing Point "+swingPoint);
 		    
 		        if(swingPos.get(i).getEndPoint().equals(swingPoint))
 		        {
 		    	    JOptionPane.showMessageDialog(null, swingPoint+"   "+swingPos.get(i).getEndPoint());
 		    	    swingPoints.remove(getGeoPosition(swingPos.get(i).getEndPoint()));
 		    	    swingPoints.remove(getGeoPosition(swingPos.get(i).getStartPoint()));
 		    	    
 		    	    if(swingPos.get(i).getStartPoint().equals(nextStationPoint) || swingPos.get(i).getStartPoint().equals(new Point(nextStationPoint.x,nextStationPoint.y+1)))
 				    {
 			     		 JOptionPane.showMessageDialog(null, "entered ");
 			     		 swingPos.remove(i);
 				    	 break;
 				    }
 		    	    else
 		    	       swingPos.remove(i);
 		    	    	    
 		     	    i--;
 		     	  	  
 		     	    
 		     	   if(i<0)
 		     	    	break;
 		     	    
 		     	    swingPoint.x = swingPos.get(i).getEndPoint().x;
 		    	    swingPoint.y = swingPos.get(i).getEndPoint().y;
 		     	    
 		    	    JOptionPane.showMessageDialog(null, "index "+i);
 		        }
 		    
 		        
 		   }
 		   
 		   JOptionPane.showMessageDialog(null, swingPos.size());*/
 		   
 		    
 		}
 		
 			
 	}
 	
 	public boolean isMouseOnStation(Point point)
 	{
 		for(int i=0;i<mousePos.size();i++)
 		{ 
 			if(mousePos.get(i).isPointInRegion(new MmMousePoints(point.x,point.y)))
 			{
 				if(i==0)
 					station_index=i;
 				else
 				{
 					if(!swingPos.isEmpty())
     				{
     					        					
     					for(int j=0;j<swingPos.size();j++)
     					{
     					    
     					    clickedEdge = swingPos.get(j).getEdgeIndex(mousePos.get(i).getPoint()); 
     						if(clickedEdge!= -1)
     						{
     							station_index = j+1;
     							break;
     						}
                                  				
     					}
     				}
 				}
 				return true;
 			}	
 			
 		}	
 		return false;
 	}
 	
 	
 	public boolean isMouseOnLine(Point point)
 	{
 		geoMousePoints.clear();
 		
 		
 				
 		if(!swingPos.isEmpty())
 		{
 			geoMousePoints.add(new MmMousePoints(swingPos.get(0).getStartPoint()));
 			geoMousePoints.add(new MmMousePoints(swingPos.get(0).getEndPoint()));
 			swingPos.get(0).print();
 			for(int i=1;i<swingPos.size();i++)
 			{
 				 geoMousePoints.add(new MmMousePoints(swingPos.get(i).getEndPoint()));
 			}
 		}
 		
 		
 				
 		for(int i=0;i<geoMousePoints.size()-1;i++)
 		{
 			Point firstStationPoint = geoMousePoints.get(i).getPoint();
 			Point secondStationPoint = geoMousePoints.get(i+1).getPoint();
 		    
 			
 			
 			double pnt1 = (secondStationPoint.y-firstStationPoint.y);
 			double pnt2 = (secondStationPoint.x-firstStationPoint.x);	
 			
 			
 			double dist = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 						
 			pnt1 = (secondStationPoint.y-point.y);
 			pnt2 = (secondStationPoint.x-point.x);
 			
 			
 			double dist1 = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 						
 			pnt1 = (firstStationPoint.y-point.y);
 			pnt2 = (firstStationPoint.x-point.x);
 			
 			
 			double dist2 = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 			//System.out.println("distance  "+Math.abs(dist-dist2));
 						
 			if(Math.abs(dist-(dist1+dist2))<0.1)
 			{
 				 geoDistance = calculateDistance(firstStationPoint,secondStationPoint);
 				 return true;
 			}
 			
 						
 		}
 		
 		return false;
 	}
 	
 	//checks the mouse on swing point
 	 public boolean isMouseOnSwingPoint(Point point)
 	 {
 		 if(swingPos.size()==1)
 			 return false;
 		 
 		 for(int i=0;i<swingPos.size();i++)
 		 {
 		     if(swingPos.get(i).getStartPointType() || swingPos.get(i).getStartPointType())
 		     { 	 
 			     if(swingPos.get(i).isSwingPointPresent(point))
 			     {
 			          return true;
 			     }
 		     }    
 			 	 
 		 }
 		 
 		 return false;
 	 }
 	
 	
 	//checks weather the current swing point selected can be place at current mouse position
 	public boolean isPointMoveableToCurrentLocation(Point point)
 	{
 		if(checkDistance(getGeoPosition(point)) && checkSwingPointsDistance(getGeoPosition(point)))
 			return true;
 		
 		return false;
 	}
 	
 	//add the edge between the two stations
 	public void addEdge()
 	{
 		MmSwingPoints swgPnts = new MmSwingPoints(new Point(-1,-1));
 		
 		swgPnts.setStartPoint(mousePos.get(mousePos.size()-2).getPoint());
 		swgPnts.setEndPoint(mousePos.get(mousePos.size()-1).getPoint());
 		swgPnts.setStartGeoPosition(getGeoPosition(mousePos.get(mousePos.size()-2).getPoint()));
 		swgPnts.setEndGeoPosition(getGeoPosition(mousePos.get(mousePos.size()-1).getPoint()));
 		swgPnts.setStationIndex(geoPos.size()-1);
 		swingPos.add(swgPnts);
 		
 		
 		
 	}
 	
 	//check if the mouse clicked point is between two stations 
 	public boolean isPointOnLine(Point point)
 	{
 		geoMousePoints.clear();
 		
 				
 		if(!swingPos.isEmpty())
 		{
 			geoMousePoints.add(new MmMousePoints(swingPos.get(0).getStartPoint()));
 			geoMousePoints.add(new MmMousePoints(swingPos.get(0).getEndPoint()));
 			
 			for(int i=1;i<swingPos.size();i++)
 			{
 				 geoMousePoints.add(new MmMousePoints(swingPos.get(i).getEndPoint()));
 				 //swingPos.get(i).print();
 			}
 		}
 		
 				
 		for(int i=0;i<geoMousePoints.size()-1;i++)
 		{
 			Point firstStationPoint = geoMousePoints.get(i).getPoint();
 			Point secondStationPoint = geoMousePoints.get(i+1).getPoint();
 			
 			//using line euation  y = mx+c
 			//first calculate the slope
 			//float slope = (float)(secondStationPoint.y-firstStationPoint.y)/(secondStationPoint.x-firstStationPoint.x);
 			//second calulating the constant part
 			//float lineConst = (float) (- (slope*firstStationPoint.x) + firstStationPoint.y);
 			
 			double pnt1 = (secondStationPoint.y-firstStationPoint.y);
 			double pnt2 = (secondStationPoint.x-firstStationPoint.x);	
 			
 			
 			
 			double dist = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 						
 			pnt1 = (secondStationPoint.y-point.y);
 			pnt2 = (secondStationPoint.x-point.x);
 			
 			
 			double dist1 = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 						
 			pnt1 = (firstStationPoint.y-point.y);
 			pnt2 = (firstStationPoint.x-point.x);
 			
 			
 			double dist2 = Math.sqrt((pnt1*pnt1)+(pnt2*pnt2));
 			
 			//System.out.println("point on line "+Math.abs(dist-(dist1+dist2)));
 						
 			if((Math.abs(dist-(dist1+dist2))<0.1) && !isMouseOnSwingPoint(point))
 			{
 					
 				if(!swingPos.isEmpty())
 				{
 					//System.out.println("Entered data ");
 					int swing_index = swingPos.get(i).getStationIndex();
 					swingPos.remove(i);
 					MmSwingPoints swgPnts = new MmSwingPoints(point);
 					swgPnts.setStartPoint(firstStationPoint);
 					swgPnts.setStartGeoPosition(getGeoPosition(firstStationPoint));
 					swgPnts.setEndPointType(true);
 					swgPnts.setEndPoint(point);
 					swgPnts.setEndGeoPosition(getGeoPosition(point));
 					swgPnts.setStationIndex(swing_index);
 					swingPos.add(i,swgPnts);
 					
 					
 					MmSwingPoints swgPnts1 = new MmSwingPoints(point);
 					swgPnts1.setStartPoint(point);
 					swgPnts1.setStartGeoPosition(getGeoPosition(point));
 					swgPnts1.setEndPoint(secondStationPoint);
 					swgPnts1.setEndGeoPosition(getGeoPosition(secondStationPoint));
 					swgPnts1.setEndPointType(true);
 					swgPnts1.setStationIndex(swing_index);
 		 			swingPos.add(i+1,swgPnts1);
 		 			edgeIndex = i;
 		 					 					 			
 		 		    return true;
 		 		    
 			
 					/*for(int j=0;j<swingPos.size();j++)
 					{
 						if(swingPos.get(j).getSwingPoint().equals(new Point(-1,-1)))
 						{
 							System.out.println("swing point index "+j);
 							swingPos.remove(j);
 							
 							MmSwingPoints swgPnts = new MmSwingPoints(point);
 							swgPnts.setStartPoint(firstStationPoint);
 							swgPnts.setEndPoint(point);
 							swingPos.add(j,swgPnts);
 							
 							MmSwingPoints swgPnts1 = new MmSwingPoints(point);
 							swgPnts1.setStartPoint(point);
 							swgPnts1.setEndPoint(secondStationPoint);
 				 			swingPos.add(swgPnts1);
 				 			
 				 			edgeIndex = j;
 				 			
 				 			System.out.println("data swing "+ swingPos.size());
 				 			
 				 			return true;
 						}
 						else
 						{
 							for(int x=0;x<swingPos.size();x++)
 							{
 								if(swingPos.get(x).getStartPoint().equals(firstStationPoint) && swingPos.get(x).getEndPoint().equals(secondStationPoint))
 								{
 									swingPos.remove(x);
 									MmSwingPoints swgPnts = new MmSwingPoints(point);
 									swgPnts.setStartPoint(firstStationPoint);
 									swgPnts.setEndPoint(point);
 									swingPos.add(x,swgPnts);
 									
 									
 									MmSwingPoints swgPnts1 = new MmSwingPoints(point);
 									swgPnts1.setStartPoint(point);
 									swgPnts1.setEndPoint(secondStationPoint);
 						 			swingPos.add(x+1,swgPnts1);
 						 			edgeIndex = x;
 						 			System.out.println("data swing "+ swingPos.size());
 						 			break;
 								}
 							}
 							
 				 			return true;
 						}
 					}*/
 				}
 								
 			}
 			
 						
 		}
 		
 		return false;
 	}
 	
 	public JXMapKit getMap()
 	{
 		return mapKit;
 	}
 	
 	public void getMapCurrentLocation()
 	{
 		
 		InputStream locationData = null;
 		
 		String jsonString = new String("");
 		
 		InetAddress IP = null;
 		
 		try {
 
 		       
 			  IP=InetAddress.getLocalHost();
              //JOptionPane.showMessageDialog(null, IP.getHostAddress().toString());
         
              URL geocodeUrl = new URL("http://api.ipinfodb.com/v3/ip-city/?key=552b7cf0010662a89bcbd90b9727186c4fb97ffe7883e11a2ebba59765d7f461&format=json");
          
         
         
             locationData = geocodeUrl.openStream();
         
             
         
             InputStreamReader is1 = new InputStreamReader(locationData);
             StringBuilder sb=new StringBuilder();
             BufferedReader br = new BufferedReader(is1);
             String read = br.readLine();
 
             while(read != null) {
                //System.out.println(read);
                sb.append(read);
                read = br.readLine();
 
             }
         
             jsonString = sb.toString();
         
             
         
         } catch (IOException ex) {
     	
     	    JOptionPane.showMessageDialog(null, ex);
         
        } finally {
         try {
         	if(locationData!=null)
         	    locationData.close();
            } catch (IOException ex) {
             
           }
         }
 		
 		
 		JSONTokener jsonTokens = new JSONTokener(jsonString);
 		
 		try
 		{
 	       JSONObject mapLocationObject = new  JSONObject(jsonTokens);
 	       
 	       	       
 	       latitude = mapLocationObject.optDouble("latitude");
 	       longitude = mapLocationObject.optDouble("longitude");
 	       
 	      
 	       
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 	    mapKit.getMainMap().setCenterPosition(new GeoPosition(latitude,longitude));
 		mapKit.getMainMap().setAddressLocation(new GeoPosition(latitude,longitude));
 		
 	}
 	
 	/*
 	 * calculate distance between two geo points
 	 */
 	public float calculateDistance(Point firstPoint, Point secondPoint)
 	{
 		
 		double earthRadius = 3958.75; 
 				
 		GeoPosition firstGeoPosition = getGeoPosition(firstPoint);
 		
 		GeoPosition secondGeoPosition = getGeoPosition(secondPoint);
 		
 	   double dLat = Math.toRadians((firstGeoPosition.getLatitude()-secondGeoPosition.getLatitude()));
 	   double dLng = Math.toRadians((firstGeoPosition.getLongitude()-secondGeoPosition.getLongitude()));
 	        
 	   double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    		   Math.cos(DegreesToRadians(secondGeoPosition.getLatitude())) *
 	    		   Math.cos(DegreesToRadians(firstGeoPosition.getLatitude())) *
 	    		   Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	   double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	   double dist = earthRadius * c;
 	    
 	   int meterConversion = 1609;
 	   float pntDist = new Float(dist * meterConversion).floatValue();
 	       
 	   return pntDist;
 	}
 	
 		
 	public boolean checkDistance(GeoPosition pnt)
 	{
 		
 		double earthRadius = 3958.75; 
 		boolean insertPnt = false;
 		
 		int stationIndex = geoPos.indexOf(pnt);
 		
 	    for(int i=0;i<geoPos.size();i++)
 	    {	
 	    	if(i!=stationIndex)
 	    	{	
 	            double dLat = Math.toRadians((geoPos.get(i).getLatitude()-pnt.getLatitude()));
 	            double dLng = Math.toRadians((geoPos.get(i).getLongitude()-pnt.getLongitude()));
 	        
 	            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    		   Math.cos(DegreesToRadians(pnt.getLatitude())) *
 	    		   Math.cos(DegreesToRadians(geoPos.get(i).getLatitude())) *
 	    		   Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	           double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	           double dist = earthRadius * c;
 	    
 	           int meterConversion = 1609;
 	           float pntDist = new Float(dist * meterConversion).floatValue();
 	       
 	       
 	       	       
 	           if(pntDist>35)
 	           {	   
 	    	      insertPnt=true;
 	    	      continue;
 	           }	   
 	           else
 	           {	   
 	    	      insertPnt=false;
 	    	      break;
 	           } 
 	    	}   
 	    }   
 	    
 	    if(swingPos.size()>=2)
 	    {
 	    	for(int i=0;i<swingPos.size()-1;i++)
 	    	{
 	    		
 	    	}
 	    }
 	    
 	    return insertPnt;
 	}
 	
 	
 	/*
 	 * check distance between the stations and swing points
 	 */
 	public boolean checkDistances(GeoPosition pnt)
 	{
 		
 		double earthRadius = 3958.75; 
 		boolean insertPnt = false;
 		boolean insertPnt1 = true;
 		
 		int stationIndex = geoPos.indexOf(pnt);
 		
 	    for(int i=0;i<geoPos.size();i++)
 	    {	
 	    	if(i!=stationIndex)
 	    	{	
 	            double dLat = Math.toRadians((geoPos.get(i).getLatitude()-pnt.getLatitude()));
 	            double dLng = Math.toRadians((geoPos.get(i).getLongitude()-pnt.getLongitude()));
 	        
 	            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    		   Math.cos(DegreesToRadians(pnt.getLatitude())) *
 	    		   Math.cos(DegreesToRadians(geoPos.get(i).getLatitude())) *
 	    		   Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	           double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	           double dist = earthRadius * c;
 	    
 	           int meterConversion = 1609;
 	           float pntDist = new Float(dist * meterConversion).floatValue();
 	            
 	       	       
 	           if(pntDist>50)
 	           {	   
 	    	      insertPnt=true;
 	    	      continue;
 	           }	   
 	           else
 	           {	   
 	    	      insertPnt=false;
 	    	      break;
 	           } 
 	    	}   
 	    }   
 	    
 	    if(swingPos.size()>=2)
 	    {
 	    	insertPnt1=false;
 	    	for(int i=0;i<swingPos.size()-1;i++)
 	    	{
 	    		double dLat = Math.toRadians((swingPos.get(i).getEndGeoPosition().getLatitude()-pnt.getLatitude()));
 	            double dLng = Math.toRadians((swingPos.get(i).getEndGeoPosition().getLongitude()-pnt.getLongitude()));
 	        
 	            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    		   Math.cos(DegreesToRadians(pnt.getLatitude())) *
 	    		   Math.cos(DegreesToRadians(swingPos.get(i).getEndGeoPosition().getLatitude())) *
 	    		   Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	           double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	           double dist = earthRadius * c;
 	    
 	           int meterConversion = 1609;
 	           float pntDist = new Float(dist * meterConversion).floatValue();
 	       
 	       
 	       	       
 	           if(pntDist>35)
 	           {	   
 	    	      insertPnt1=true;
 	    	      continue;
 	           }	   
 	           else
 	           {	   
 	    	      insertPnt1=false;
 	    	      break;
 	           } 
 	    	}
 	    	
 	    }
 	    
 	    if(insertPnt && insertPnt1)
 	        return true;
 	    else
 	    	return false;
 	}
 	
 	
 	public boolean checkDistance()
 	{
 		
 		double earthRadius = 3958.75; 
 		boolean insertPnt = false;
 		GeoPosition pnt = null;
 		
 		if(!geoPos.isEmpty())
 		    pnt = geoPos.get(index);
 		
 	    for(int i=0;i<geoPos.size();i++)
 	    {	
 	    	if(i!=index && pnt!=null)
 	    	{ 
 	          double dLat = Math.toRadians((geoPos.get(i).getLatitude()-pnt.getLatitude()));
 	          double dLng = Math.toRadians((geoPos.get(i).getLongitude()-pnt.getLongitude()));
 	          double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    	  Math.cos(DegreesToRadians(pnt.getLatitude())) *
 	    	  Math.cos(DegreesToRadians(geoPos.get(i).getLatitude())) *
 	      	  Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	          double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	          double dist = earthRadius * c;
 	    
 	          int meterConversion = 1609;
 	          float pntDist = new Float(dist * meterConversion).floatValue();
 	       
 	          	       
 	          if(pntDist>35)
 	          {	   
 	    	      insertPnt=true;
 	    	      continue;
 	          }	   
 	          else
 	          {	   
 	    	      insertPnt=false;
 	    	      break;
 	          }
 	       }   
 	    }   
 	    
 	    return insertPnt;
 	}
 	
 	//checks distance between the swing points 
 	public boolean checkSwingPointsDistance(GeoPosition pnt)
 	{
 		
 		double earthRadius = 3958.75; 
 		boolean insertPnt = false;
 		
 		
 		
 		if(swingPos.size()<=2 && !isPointPresent)
 			return true;
 		
 		
 		
 		
 	    for(int i=0;i<swingPos.size()-1;i++)
 	    {	
 	    	
 	    	if(!swingPos.get(i).getEndGeoPosition().equals(pnt) || !swingPos.get(i+1).getStartGeoPosition().equals(pnt))
 	    	{	
 	    		
 	            double dLat = Math.toRadians((swingPos.get(i).getEndGeoPosition().getLatitude()-pnt.getLatitude()));
 	            double dLng = Math.toRadians((swingPos.get(i).getEndGeoPosition().getLongitude()-pnt.getLongitude()));
 	        
 	            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
 	    		   Math.cos(DegreesToRadians(pnt.getLatitude())) *
 	    		   Math.cos(DegreesToRadians(swingPos.get(i).getEndGeoPosition().getLatitude())) *
 	    		   Math.sin(dLng/2) * Math.sin(dLng/2);
 	   
 	           double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	           double dist = earthRadius * c;
 	    
 	           int meterConversion = 1609;
 	           float pntDist = new Float(dist * meterConversion).floatValue();
 	       
 	           
 	       	       
 	           if(pntDist>20)
 	           {	   
 	    	      insertPnt=true;
 	    	      continue;
 	           }	   
 	           else
 	           {	   
 	        	  insertPnt=false;
 	    	      break;
 	           }
 	    	}   
 	    }   
 	    
 	    return insertPnt;
 	}
 	
 	public double DegreesToRadians(double degrees)
 	{
 	    return degrees * Math.PI / 180;
 	}
 	
 	public GeoPosition getGeoPosition(Point mousePos)
 	{
 		double widthx = mousePos.getX();
         double heighty = mousePos.getY();
         
         Rectangle rect = mapKit.getMainMap().getViewportBounds();
         
         widthx = widthx+rect.x;
         heighty = heighty+rect.y;
           
         TileFactoryInfo titleInfo = mapKit.getMainMap().getTileFactory().getInfo();
         
         int zoom = mapKit.getMainMap().getZoom();
         
         double lan = (widthx-titleInfo.getMapCenterInPixelsAtZoom(zoom).getX())/titleInfo.getLongitudeDegreeWidthInPixels(zoom);
         double lat_coord = (heighty-titleInfo.getMapCenterInPixelsAtZoom(zoom).getY())/(-1*titleInfo.getLongitudeRadianWidthInPixels(zoom));
         
         double lat = (2*Math.atan(Math.exp(lat_coord))-Math.PI/2) / (Math.PI / 180.0);
         
         return (new GeoPosition(lat,lan));
         
        
 	}
 	
 	public void showGeoCoords(Point mousePos)
 	{
 		
 		boolean found = false;
 		Point pnt1 = null;
 		
 		double widthx = mousePos.getX();
         double heighty = mousePos.getY();
         
         Rectangle rect = mapKit.getMainMap().getViewportBounds();
         
         widthx = widthx+rect.x;
         heighty = heighty+rect.y;
         
         
         TileFactoryInfo titleInfo = mapKit.getMainMap().getTileFactory().getInfo();
         
         
         int zoom = mapKit.getMainMap().getZoom();
         
         double lan = (widthx-titleInfo.getMapCenterInPixelsAtZoom(zoom).getX())/titleInfo.getLongitudeDegreeWidthInPixels(zoom);
         double lat_coord = (heighty-titleInfo.getMapCenterInPixelsAtZoom(zoom).getY())/(-1*titleInfo.getLongitudeRadianWidthInPixels(zoom));
         
         double lat = (2*Math.atan(Math.exp(lat_coord))-Math.PI/2) / (Math.PI / 180.0);
         
         locLabel.setText(String.valueOf(lat)+","+String.valueOf(lan));
     	locLabel.setLocation(new Point(800,600));
         locLabel.setVisible(true); 
 		
 	}
 	
 	public void createJSONFile(String fileName,String filePath,ArrayList<MmGlobalMarkerEvents> markerEvents,MmStartEvents startEvents)
 	{
 	
 		action = new JSONArray();
 		JSONArray eventsArray = new JSONArray();
 		
 		JSONObject numCollectItems = new JSONObject();
 		JSONObject numCollectItemsAttributes = new JSONObject();
 		
 				
 		initialEvents(eventsArray,markerEvents,filePath,startEvents,fileName);
 		
 		JSONObject compassData = new JSONObject(); 
 		JSONObject compassAttributes = new JSONObject();
 		
 		try
 		{
 			compassData.put("name", "compassView");
 			compassData.put("type", "compassView");
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		if(!geoPos.isEmpty())
 		{	
 			
 		    MmAudioEvent stationAudioEvent = new MmAudioEvent();
 		    stationAudioEvent.setEventName("stationEnterAudio");
   	        stationAudioEvent.setAudioFileName("magic-chime.mp3");
 		    stationAudioEvent.setSourcePath(System.getProperty("user.dir")+"/audios/magic-chime.mp3");
 		    stationAudioEvent.setDestinationPath(filePath);
 		    stationAudioEvent.makeJSONObject();
 		    if(!stationAudioEvent.JSONActions())
 		    {
 		    	JOptionPane.showMessageDialog(null, MmLanguage.language_exception[language][0]);
 		    	File file = new File(filePath);
 		    	if(file.isDirectory())
 		    	{
 		    		File[] files = file.listFiles();
 		    		if(files.length>0)
 		    		{
 		    			files[0].delete();
 		    		}
 		    		file.delete();
 		    	}	
 		    	return;
 		    }	
 		
 		    eventsArray.put(stationAudioEvent.getAudioEvent());
 		
 		    for(int i=0;i<events.getStations().size();i++)
 		    {	
 		       if(events.getStations().get(i).getStationType())
 		       {	   
 		          MmAudioEvent swingAudioEvent = new MmAudioEvent();
 		          swingAudioEvent.setEventName("wayPointAudio");
   	              swingAudioEvent.setAudioFileName("wayPoint.m4a");
 		          swingAudioEvent.setSourcePath(System.getProperty("user.dir")+"/audios/wayPoint.m4a");
 		          swingAudioEvent.setDestinationPath(filePath);
 	  	          swingAudioEvent.makeJSONObject();
 		          if(!swingAudioEvent.JSONActions())
 		          {	
 		    	       JOptionPane.showMessageDialog(null, MmLanguage.language_exception[language][0]);
 		    	       File file = new File(filePath);
 		    	       if(file.isDirectory())
 		    	       {
 		    		        File[] files = file.listFiles();
 		    		        if(files.length>0)
 		    		        {
 		    			         files[0].delete();
 		    		        }
 		    		       file.delete();
 		    	       }	
 		    	       return;
 		          }
 		          eventsArray.put(swingAudioEvent.getAudioEvent());
 		          break;
 		       }  
 		    }   
 		    
 		    
 		    
 		    try
 		    {
 		        compassAttributes.put("showCompassView",true);
 		    }
 		    catch(Exception e)
 		    {
 		    	
 		    }
 		} 
 		else
 		{
 			 try
 			 {
 			      compassAttributes.put("showCompassView",false);
 		     }
 			 catch(Exception e)
 			 {
 			    	
 			 }
 		}
 		
 		try
 		{
 		   compassData.put("attributes", compassAttributes);
 		   eventsArray.put(compassData);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 	
 		try
 		{
 		   
 			if(!events.getStations().isEmpty())
 			{   
 			   
 			   mapMarkers(eventsArray,filePath);
 			   if(!writeMap3DObjects(filePath))
 			   {
 				   JOptionPane.showMessageDialog(null, MmLanguage.language_exception[language][0]);
 				   
 				   File file = new File(filePath);
 			    	if(file.isDirectory())
 			    	{
 			    		File[] files = file.listFiles();
 			    		if(files.length>0)
 			    		{
 			    			files[0].delete();
 			    		}
 			    		file.delete();
 			    	}	
 			    	
 				   
 				   return;
 			   }
 			   
 				
 		       for(int i=0;i<events.getStations().size();i++)
 		       {
 		    	   		   	   
 			   	   
 			   	  compassEvents(eventsArray,events.getStations().get(i));
 			   	  
 			   	   
 			   	   if((i+1)<events.getStations().size())
 			   	   {	   
 			   	         events.getStations().get(i).writeJson(filePath,events.getStations().get(i+1));
 			   	   }   
 			   	       
                    if(events.getStations().size()==1 || i==events.getStations().size()-1)
 			   	   {
                 	   
 			   	       events.getStations().get(i).writeJson(filePath,null);
 			   	   }
 			   	    	   
 			   	   			   	   
 		           events.getStations().get(i).writeJsonObjects(eventsArray);
  
 		       }
 			}   
 			
 			writeInitialEvents();
 		   
 			numCollectItems.put("name", "NumberOfCollectItems");
 			numCollectItems.put("type", "NumberOfCollectItems");
 			   
 			int count = 0;
 			
 			for(int i=0;i<events.getStations().size();i++)
 		    {
 				 //for(int j=0;j<events.getStations().get(i).getItemsCollected().size();j++)
 				 {
 					  //JOptionPane.showMessageDialog(null, "objects collect "+events.getStations().get(i).getItemsCollected().size()+"   "+count);
 					 if(!events.getStations().get(i).getItemsCollected().isEmpty())
 					     count+=events.getStations().get(i).getItemsCollected().size();
 				 }
 		    }
 			
 			for(int i=0;i<markerEvents.size();i++)
 			{
 				if(markerEvents.get(i).getCollectedItems()!=0)
 				    count+=markerEvents.get(i).getCollectedItems();
 			}
 			
 		    
 			   
 			/*if(count>1)   
 			   numCollectItemsAttributes.put("numCollectItems", count-1);
 			else
 				numCollectItemsAttributes.put("numCollectItems", count);*/
 			
 			numCollectItemsAttributes.put("numCollectItems", count);
 			   
 			numCollectItems.put("attributes",numCollectItemsAttributes);
 			
 			
 			eventsArray.put(numCollectItems);
 			
 			try
 			{
 				JSONObject doneEvent = new JSONObject();
 				JSONObject doneAction = new JSONObject();
 				
 				doneEvent.put("name", "Done");
 				doneEvent.put("type", "done");
 				
 				JSONArray doneArry = new JSONArray();
 				
 				doneAction.put("application-done",doneArry);
 				
 				doneEvent.put("actions", doneAction);
 				
 				eventsArray.put(doneEvent);
 				
 			}
 			catch(Exception e)
 			{
 				
 			}
 		   
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 			
 		 try
 	       {
 			   
 			   if(!fileName.contains(".json"))
 				   fileName = fileName+".json";
 				   
 			   FileWriter  writeJsonFile = new FileWriter(fileName);
 			   writeJsonFile.write(eventsArray.toString(4));
 	    	   writeJsonFile.flush();
 	    	   writeJsonFile.close();
 	       }
 	       catch(Exception e)
 	       {
 	    	    JOptionPane.showMessageDialog(null,MmLanguage.language_exception[language][0]);
 	    	   
 	    	    File file = new File(filePath);
 		    	if(file.isDirectory())
 		    	{
 		    		File[] files = file.listFiles();
 		    		if(files.length>0)
 		    		{
 		    			files[0].delete();
 		    		}
 		    		file.delete();
 		    	}	
 		    	return;
 	       }
 		
 	}
 	
 	public void initialEvents(JSONArray jsonObjects,ArrayList<MmGlobalMarkerEvents> globalMarkers,String filePath,MmStartEvents startEvents,String fileName)
 	{
 		
 		JSONObject trailName = new JSONObject();
 		JSONObject trailAttributes = new JSONObject();
 		
 		JSONObject startName = new JSONObject();
 		JSONObject startAction = new JSONObject();
 		startActionArray =  new JSONArray();
 		
 		JSONObject numRegions = new JSONObject();
 		JSONObject numRegionsAttributes = new JSONObject();
 		
 		
 			
 		
 		try {
 			
 			   trailName.put("name", "Title");
 			   trailName.put("type", "title");
 			   
 			   File title = new File(fileName);
 			   
 			   //showMessage("file name "+file_name[file_name.length-1]);
 			   
 			   //start event 
 			   startName.put("name", "Start");
 			   startName.put("type", "start");
 			   
 			   //number of regions events
 			   numRegions.put("name", "NumberOfRegions");
 			   numRegions.put("type", "NumberOfRegions");
 			   
 			   //JOptionPane.showMessageDialog(null, "geo pos "+geoPoints.size());
 			   
 			   int count = 0;
 			   
 			   for(int i=0;i<events.getStations().size();i++)
 			   {
 				   if(!events.getStations().get(i).getStationType())
 				   {
 					   count++;
 				   }
 			   }
 			   
 			   numRegionsAttributes.put("numRegions",count);
 			   
 			   numRegions.put("attributes",numRegionsAttributes);
 			   
 			   
 			   
 			   		   
 			   String ledName = title.getName();
 			   
 			     			   
 			   ledName = ledName.substring(0, ledName.indexOf("."));
 			   
 			   trailAttributes.put("ledTitle",ledName);
 			   
 			   trailName.put("attributes", trailAttributes);
 			   
 			   jsonObjects.put(trailName);
 			
 			   initializeEvents.put("name", "launch");
 			   initializeEvents.put("type", "generic");
 			   
 			   if(!this.getStations().isEmpty() && (startEvents.getTexts().size()<=1))
 			       startActionArray.put("compassView");
 			   
 			   action.put("Start");
 			   			   
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 		
 		//writing start events data
 		
 		if(!startEvents.getTexts().isEmpty())
 		{
 			 startEvents.writeJson(filePath);
 			 startEvents.writeJsonObjects(jsonObjects);
 			 startEvents.writeBackgroundImage(jsonObjects);
 		}
 		
 		
 		if(!startEvents.getTexts().isEmpty())
 		{
 			if(startEvents.getTextPaths().size()>=2)
 			{	
 			   for(int i=1;i<2;i++)
 			   {
 				   if(!startEvents.eventNames[i].isEmpty())
 				   {
 					   startActionArray.put(startEvents.eventNames[i]);
 				   }
 			   }	   
 			}
 			else
 			{
 				if(!startEvents.eventNames[0].isEmpty())
 			    {
 					 startActionArray.put(startEvents.eventNames[0]);
 				 }
 			}
 		}
 		
 		if(!geoPoints.isEmpty())
 		{
 			jsonObjects.put(numRegions);
 			startActionArray.put("NumberOfRegions");
 			startActionArray.put("NumberOfCollectItems");
 		}
 		
 		try
 		{
 		    startAction.put("application-start", startActionArray);
 		    startName.put("actions", startAction);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		
 		/*if(!startEvents.getTexts().isEmpty())
 		{
 			if((startEvents.getTexts().get(0).contains(".jpg"))||(startEvents.getTexts().get(0).contains(".png"))||(startEvents.getTexts().get(0).contains(".bmp")))
 			{	
 			   for(int i=1;i<startEvents.getTexts().size();)
 			   {
 				  
 			      if(startEvents.getTexts().get(i).contains(".mp3")||startEvents.getTexts().get(1).contains(".m4a")||startEvents.getTexts().get(1).contains(".aiff"))
 			      {
 				      action.put(startEvents.audioEvents.get(0).getEventName());
 			      }
 			   
 			      if(startEvents.getTexts().get(i).contains(".m4v"))
 			      {
 				      action.put(startEvents.videoEvents.get(0).getEventName());
 			      }
 			   
 			      if(startEvents.getTexts().get(i).contains(".txt"))
 			      {
 				      action.put(startEvents.messageEvents.get(0).getEventName());
 			      }
 			      
 			      break;
 			   }    
 			   
 			}
 		}*/
 		
 		
 		jsonObjects.put(initializeEvents);
 		jsonObjects.put(startName);
 		
 		//writing marker associated events data
 		
 		if(!globalMarkers.isEmpty())
 		{
 			for(int i=0;i<globalMarkers.size();i++)
 			{
 				if(globalMarkers.get(i).getNumberOfEvents()!=0)
 				{	
 				   if(i==globalMarkers.size()-1 && events.getStations().isEmpty())
 				   {
 					   globalMarkers.get(i).setLastMarker(true);
 				   }
 				   globalMarkers.get(i).writeJson(filePath);
 				   globalMarkers.get(i).writeMarkerEvent(jsonObjects);
 				   globalMarkers.get(i).writeJsonObjects(jsonObjects);
 				   
 				   
 				}   
 			}
 		}
 		
 		
 		if(!globalMarkers.isEmpty())
 		{
 			for(int i=0;i<globalMarkers.size();i++)
 			{
 				//JOptionPane.showMessageDialog(null, globalMarkers.get(i).getMarkerName());
 				if(globalMarkers.get(i).getNumberOfEvents()!=0)
 				{
 					startActionArray.put(globalMarkers.get(i).getMarkerName());
 				}		
 			}
 		}	
 		
 	}
 	
 	public void writeInitialEvents()
 	{
 		try {
 			initializeActions.put("application-launched", action);
 			initializeEvents.put("actions", initializeActions);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public void writeGeoPoints_WayPoints(JSONArray jsonObjects)
 	{
 		
 		int geoIndex = -1, swingIndex = -1, currentSwingIndex = -1;
 		
 		try
 		{
 			if(swingPos.size()==1)
 			{
 				if(!geoPos.isEmpty())
 				{
 						
 					geoIndex = geoPos.indexOf(getGeoPosition(swingPos.get(0).getStartPoint()));
 					//JOptionPane.showMessageDialog(null, "Entered "+geoIndex);
 					writeGeoPosition(geoPos.get(geoIndex),jsonObjects,geoIndex+1);
 						
 					geoIndex = geoPos.indexOf(getGeoPosition(swingPos.get(0).getEndPoint()));
 					writeGeoPosition(geoPos.get(geoIndex),jsonObjects,geoIndex+1);
 			    }
 					
 			 }
 			 else
 			 {
 				  if(!geoPos.isEmpty())
 				  {
 						//JOptionPane.showMessageDialog(null,"graeter than "+swingPos.size());
 						
 						geoIndex=0;
 						swingIndex=0;
 						
 						//geoIndex = geoPos.indexOf(getGeoPosition(swingPos.get(0).getStartPoint()));
 						
 						//JOptionPane.showMessageDialog(null,"graeter than "+geoIndex);
 						 
 						//if(geoIndex!=-1)
 						 writeGeoPosition(geoPos.get(0),jsonObjects,geoIndex+1);
 						
 						//swingIndex = swingPoints.indexOf(getGeoPosition(swingPos.get(0).getEndPoint()));
 						//if(swingIndex!=-1)
 						 
 						 writeSwingPosition(swingPoints.get(0),jsonObjects,swingIndex+1,geoIndex+1);
 						
 						
 						
 						for(int i=1;i<swingPos.size();i++)
 						{
 							
 							
 						   //swingPos.get(i).print();	
 						   GeoPosition point = getGeoPosition(swingPos.get(i).getEndPoint());
 						   
 						   int currentGeoIndex = geoPos.indexOf(point);
 						   
 						    
 						   
 						   if(currentGeoIndex!=-1)
 						   {	    
 							   writeGeoPosition(geoPos.get(currentGeoIndex),jsonObjects,currentGeoIndex+1);
 							   currentGeoIndex = - 1;
 						   }	   
 						   
 						   if(currentGeoIndex==-1)
 						       currentSwingIndex = swingPoints.indexOf(point);
 						   
 						   //JOptionPane.showMessageDialog(null,"graeter than "+currentGeoIndex+"  "+currentSwingIndex);
 						   
 						   if(currentSwingIndex!=-1 && currentGeoIndex==-1)
 						   {	   
 							   writeSwingPosition(swingPoints.get(currentSwingIndex),jsonObjects,currentSwingIndex+1,geoIndex+1);
 							   currentSwingIndex = -1;
 						   }	  	   
 						   
 						   if(currentSwingIndex!=-1 && currentGeoIndex!=-1)
 						   {	   
 							   writeSwingPosition(swingPoints.get(currentSwingIndex),jsonObjects,currentSwingIndex+1,currentGeoIndex+1);
 							   currentSwingIndex = -1; 
 						   }	   
 						   
 						   						   
 						}
 						
 					}
 					
 				}
 				
 			
 			
 		   /*for(int i=0;i<geoPos.size();i++)
 		   {
 			   JSONObject geoPoint = new JSONObject();
 			   JSONObject attributes = new JSONObject();
 		       geoPoint.put("geoStation", "Sattion "+Integer.toString(i));
 		       geoPoint.put("type", "station");
 		       attributes.put("latitude",geoPos.get(i).getLatitude());
 		       attributes.put("longitude",geoPos.get(i).getLongitude());
 		       geoPoint.put("attributes", attributes);
 		       jsonObjects.put(geoPoint);
 		   }  
 		   
 		   for(int i=0;i<swingPoints.size();i++)
 		   {
 			   JSONObject geoPoint = new JSONObject();
 			   JSONObject attributes = new JSONObject();
 			   geoPoint.put("geoSwingPoint", "swingPoint "+Integer.toString(i));
 		       geoPoint.put("type", "swingPoint");
 		       attributes.put("latitude",swingPoints.get(i).getLatitude());
 		       attributes.put("longitude",swingPoints.get(i).getLongitude());
 		       geoPoint.put("attributes", attributes);
 		       jsonObjects.put(geoPoint);
 		   }*/
 		   
 		   
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 	
 	public void writeGeoPosition(GeoPosition point,JSONArray jsonObject,int geoIndex)
 	{
 		try
 		{
 			JSONObject geoPoint = new JSONObject();
 			JSONObject attributes = new JSONObject();
 		    geoPoint.put("name", "station "+Integer.toString(geoIndex));
 		    geoPoint.put("type", "station");
 		    attributes.put("latitude",point.getLatitude());
 		    attributes.put("longitude",point.getLongitude());
 		    geoPoint.put("attributes", attributes);
 		    jsonObject.put(geoPoint);
 		}
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null,e);
 		}
 	}
 	
 	
 	public void writeSwingPosition(GeoPosition point,JSONArray jsonObject,int swingIndex,int geoIndex)
 	{
 		try
 		{
 			JSONObject geoPoint = new JSONObject();
 			JSONObject attributes = new JSONObject();
 		    geoPoint.put("name", "swing_"+Integer.toString(swingIndex)+"station"+Integer.toString(geoIndex));
 		    geoPoint.put("type", "swingPoint");
 		    attributes.put("latitude",point.getLatitude());
 		    attributes.put("longitude",point.getLongitude());
 		    geoPoint.put("attributes", attributes);
 		    jsonObject.put(geoPoint);
 		}
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null,e);
 		}
 	}
 	
 	/*
 	 * compassEvents takes jsonArray,station event
 	 */
 	
 	public void compassEvents(JSONArray jsonObjects,MmStationEvents currentStation)
 	{
 		JSONObject compassEvents,compassAttributes,compassEvents1,compassAttributes1;
 		
 		compassEvents = new JSONObject();
 		compassAttributes = new JSONObject();
 		
 		try {
 			   if(!currentStation.getStationType())
 			   {	   
 			       compassEvents.put("name", "enableStation"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 			       if(currentStation.getCurrentStationIndex()==0)
 			    	   startActionArray.put("enableStation"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 		   	       compassEvents.put("type", "compassmarker");
 			       compassAttributes.put("regionIdentifier", currentStation.getStationName());
 			       compassAttributes.put("showCompassMarker",true);
 			       compassEvents.put("attributes", compassAttributes);
 			   }
 			   
 			   if(currentStation.getStationType())
 			   {	   
 				   
 			       compassEvents.put("name", "enableStation"+Integer.toString(currentStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 			       compassEvents.put("type", "compassmarker");
 			       compassAttributes.put("regionIdentifier", currentStation.getStationName());
 			       compassAttributes.put("showCompassMarker",true);
 			       compassEvents.put("attributes", compassAttributes);
 			       
 			   }
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		jsonObjects.put(compassEvents);
 		
 		
 		compassEvents1 = new JSONObject();
 		compassAttributes1 = new JSONObject();
 		
 		try {
 			   
 			   if(!currentStation.getStationType())
 		       {	   
 			       compassEvents1.put("name", "disableStation"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 			       if(currentStation.getCurrentStationIndex()!=0)
 			    	   startActionArray.put("disableStation"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 		   	       compassEvents1.put("type", "compassmarker");
 			       compassAttributes1.put("regionIdentifier", currentStation.getStationName());
 			       compassAttributes1.put("showCompassMarker",false);
 			       compassEvents1.put("attributes", compassAttributes1);
 	           }
 			   
 		       if(currentStation.getStationType())
 		       {	   
 				   compassEvents1.put("name", "disableStation"+Integer.toString(currentStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 			       compassEvents1.put("type", "compassmarker");
 			       compassAttributes1.put("regionIdentifier", currentStation.getStationName());
 			       compassAttributes1.put("showCompassMarker",false);
 			       compassEvents1.put("attributes", compassAttributes1);
 			       startActionArray.put("disableStation"+Integer.toString(currentStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(currentStation.getIndexStationNameIndex())+"Compass");
 			   }
 		    
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if(!geoPos.isEmpty())
 		    jsonObjects.put(compassEvents1); 
 		
 		
 	}
 	
 	public void mapMarkers(JSONArray jsonObjects,String filePath)
 	{
 		try
 		{
 			for(int i=0;i<geoPos.size();i++)
 			{
 				JSONObject object = new JSONObject();
 				object.put("name", "mapMarker"+Integer.toString(i+1));
 				object.put("type", "marker");
 				
 				JSONObject attribute = new JSONObject();
 				attribute.put("markerName", "patt.map_marker"+Integer.toString(i+2));
 				attribute.put("modelName", "base.osg");
 				object.put("attributes", attribute);
 				jsonObjects.put(object);
 				
 				if(i==0)
 					action.put("mapMarker"+Integer.toString(i+1));
 				else
 					startActionArray.put("mapMarker"+Integer.toString(i+1));
 				
 				writeMarkerFiles("patt.map_marker"+Integer.toString(i+2),filePath);
 				
 				JSONObject object1 = new JSONObject();
 				object1.put("name", "mapEditMarker"+Integer.toString(i+1));
 				object1.put("type", "editModel");
 				
 				JSONObject attribute1 = new JSONObject();
 				attribute1.put("markerName", "patt.map_marker"+Integer.toString(i+2));
 				attribute1.put("modelName", "diamond.osg");
 				object1.put("attributes", attribute1);
 				jsonObjects.put(object1);
 				
 				
 				
 			}
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 	public boolean writeMarkerFiles(String markerFile, String filePath)
 	{
 		FileChannel src = null,des=null;
 		try {
 			  
 			  
 			  File file = new File(filePath+"/markers");
 			  
 			  String sourcePath = System.getProperty("user.dir")+"/markers/"+markerFile;
 			  
 			  			  
 			  String[] desPath = sourcePath.split("/");
 			  
 			  File desFile = new File(filePath+"/markers/"+desPath[desPath.length-1]);
 			  
 			  if(!desFile.exists())
 			  {	  
 			     if(file.isDirectory())
 			     {
 				    src = new FileInputStream(sourcePath).getChannel(); 
 			        des = new FileOutputStream(filePath+"/markers/"+desPath[desPath.length-1]).getChannel();
 			     }
 			     else
 			     {
 				    file.mkdir();
 				    src = new FileInputStream(sourcePath).getChannel();
 				    des = new FileOutputStream(filePath+"/markers/"+desPath[desPath.length-1]).getChannel();
 			     }
 			  }   
 			  
 			  
 			  
 			  try {
 				  if(des!=null)
 					     des.transferFrom(src, 0, src.size());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(null, e);
 				e.printStackTrace();
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			JOptionPane.showMessageDialog(null, e);
 			e.printStackTrace();
 			return false;
 		}
 
 		finally
 		{
 			try {
 				  if(src!=null)
 				  {	
 				     src.close();
 				     des.close();
 				  }
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(null, e);
 				e.printStackTrace();
 				return false;
 			}
 			
 		}
 		
 		return true;
 
 	}
 	
 	/*
 	 * writes the 3d content files 
 	 */
 	public boolean writeMap3DObjects(String filePath)
 	{
 		if(!writeFile("base.osg",filePath))
               return false;
 		if(!writeFile("diamond.osg",filePath))
 			  return false;   
 		   
 		if(!writeFile("base1.jpg",filePath))
 			return false;
 		
 		if(!writeFile("diamond2.jpg",filePath))
 			return false;
 		
 		return true;
 	}
 	
 	/*
 	 * writes the files content from source to destination
 	 */
 	public boolean writeFile(String sourceFileName,String filePath)
 	{
 		FileChannel src = null,des=null;
 		
 		try {
 			  
 			  
 			  File file = new File(filePath+"/osg_obj");
 			  
 			  String sourcePath = System.getProperty("user.dir")+"/osg_obj/"+sourceFileName;
 			  
 			  String[] desPath = sourcePath.split("/");
 			  
 			  if(file.isDirectory())
 			  {
 				 src = new FileInputStream(sourcePath).getChannel(); 
 			     des = new FileOutputStream(filePath+"/osg_obj/"+desPath[desPath.length-1]).getChannel();
 			  }
 			  else
 			  {
 				  file.mkdir();
 				  src = new FileInputStream(sourcePath).getChannel();
 				  des = new FileOutputStream(filePath+"/osg_obj/"+desPath[desPath.length-1]).getChannel();
 			  }
 			  
 			  
 			  
 			  try {
 				des.transferFrom(src, 0, src.size());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(null,e);
 				e.printStackTrace();
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			JOptionPane.showMessageDialog(null,e);
 			e.printStackTrace();
 			return false;
 		}
 
 		finally
 		{
 			try {
 				src.close();
 				des.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(null,e);
 				e.printStackTrace();
 				return false;
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	public ArrayList<MmStationEvents> getStations()
 	{
 		return events.getStations();
 	}
 	
 	public void showStationEventWindow()
 	{
 		/*markerFrame2.setAlwaysOnTop(true);
 		markerFrame2.setVisible(true);
 		
 		if(eventsDialog.isVisible())
 		{	
 			eventsDialog.setVisible(true);
 			eventsDialog.setAlwaysOnTop(true);
 		}*/   
 	}
 	
 	public void hideStationEventWindow()
 	{
 		/*markerFrame2.setVisible(false);
 		markerFrame2.setAlwaysOnTop(false);
 		markerFrame2.toBack();
 		
 		if(!eventsDialog.isVisible())
 		{	
 			eventsDialog.setVisible(false);
 			eventsDialog.setAlwaysOnTop(false);
 		}*/	   
 		
 	}
 	
 	public boolean isStationEventWindowHidden()
 	{
 		
 		return mapMarkersDialog.isVisible();
 		
 	}
 	
 	public void setSaved(boolean save)
 	{
 		isSaved = save;
 		events.setSaved(save);
 		mapMarkerWindow.setSaved(save);
 	}
 	
 	public boolean getSavedState()
 	{
 		
 		if(events.getSavedState() && mapMarkerWindow.getSavedState())
 			return true;
 			
 		return false;
 	}
 	
 	public void drawMapMarkers()
 	{
 
 		JDialog markerFrame = new JDialog(mainWindow);
 	    
 	    MmAddEventsDialog frame2 = new MmAddEventsDialog(markerFrame,300,100,this);
 	    markerFrame.pack();
 	    markerFrame.setSize(100, 100);
 	    markerFrame.setContentPane(frame2);
 	    markerFrame.setLocation(650,100);
 	    markerFrame.setVisible(true);
 	    mapMarkers.add(frame2);
 	    
 	    
 	    	    	    
         JDialog markerFrame1 = new JDialog(mainWindow);
 	    
 	    MmAddEventsDialog frame3 = new MmAddEventsDialog(markerFrame1,300,100,this);
 	    markerFrame1.pack();
 	    markerFrame1.setSize(100, 100);
 	    markerFrame1.setContentPane(frame3);
 	    markerFrame1.setLocation(700,100);
 	    markerFrame1.setVisible(true);
 	    mapMarkers.add(frame3);
 	    
         JDialog markerFrame3 = new JDialog(mainWindow);
 	    
 	    MmAddEventsDialog frame4 = new MmAddEventsDialog(markerFrame3,300,100,this);
 	    markerFrame3.pack();
 	    markerFrame3.setSize(100, 100);
 	    markerFrame3.setContentPane(frame4);
 	    markerFrame3.setLocation(750,100);
 	    markerFrame3.setVisible(true);
 	    mapMarkers.add(frame4);
 	    
         JDialog markerFrame4 = new JDialog(mainWindow);
 	    
 	    MmAddEventsDialog frame5 = new MmAddEventsDialog(markerFrame4,300,100,this);
 	    markerFrame4.pack();
 	    markerFrame4.setSize(100, 100);
 	    markerFrame4.setContentPane(frame5);
 	    markerFrame4.setLocation(800,100);
 	    markerFrame4.setVisible(true);
 	    mapMarkers.add(frame5);
 	    
 	}
 	
 	
 	
 	public void resetMapContents()
 	{
 		geoPos.clear();
 		events.getStations().clear();
 		swingPos.clear();
 		mapMarkerWindow.resetMapMarkers();
 		drawPoints();
 	}
 	
 	//reads a json file and parsers them to corresponding events
 	public void readJSONFileContents(String fileName,MmAccordionMenu menuItem)
 	{
 		//JSONParser mapParser = new JSONParser();
 				
 		menuItems = menuItem;
 		
 		
 		
 		//JOptionPane.showMessageDialog(null, "filename "+fileName);
 		
 		openFileName = new File(fileName);
 		
 		BufferedReader br;
 		try {
 			   br = new BufferedReader(new FileReader(fileName));
 		
 	           try {
 	                 StringBuilder sb = new StringBuilder();
 	                 String line = br.readLine();
 
 	                 while (line != null) {
 	                       sb.append(line);
 	                       line = br.readLine();
 	                }
 	                String jsonContent = sb.toString();
 	                JSONTokener jsonTokens = new JSONTokener(jsonContent);
 	                
 	                try
 	                {
 	                   //JSONObject readSource  = new JSONObject(jsonTokens);
 	                   readJSONObjects = new JSONArray(jsonTokens);
 	                   //JOptionPane.showMessageDialog(null, readJSONObjects.length());
 	                   for(int i=0;i<readJSONObjects.length();i++)
 	                   {	   
 	                      	                      
 	                	  parseJsonObject((JSONObject) readJSONObjects.get(i),i);
 	                	  
 	                	  
 	                                	  
 	                   }   
 	                }
 	                catch(Exception e)
 	                {
 	                	JOptionPane.showMessageDialog(null, "error"+e);
 	                }
 	                
 	                
 	                
 	              }     
 		            catch (FileNotFoundException e1) {
 			        // TODO Auto-generated catch block
 			        e1.printStackTrace();
 	               }
 	           finally {
 		   	        br.close();
 		   	    }
 		}       
 	    catch(Exception e)
 	    {
 	    	
 	    }
 		
 		for(int i=0;i<events.getStations().size();i++)
 		{
 			
 			if(!events.getStations().get(i).getStationType())
 			{	
 			    GeoPosition gp = new GeoPosition(events.getStations().get(i).getLatitude(),events.getStations().get(i).getLongitude());
 			    geoPos.add(gp);
 			    
 			    Rectangle rect = mapKit.getMainMap().getViewportBounds();
 				
 				Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
 				
 				MmMousePoints pnts = new MmMousePoints((int)pt.getX()-rect.x,(int)pt.getY()-rect.y);
 				
 				pnts.drawRegion();
 								
 				mousePos.add(pnts);
 				
 			}
 			/*else
 			{
 				GeoPosition gp = new GeoPosition(events.getStations().get(i).getLatitude(),events.getStations().get(i).getLongitude());
 			    swingPoints.add(gp);
 			}*/
 			
 		}
 		
         for(int i=0;i<events.getStations().size()-1;i++)
         {
         	GeoPosition gp = new GeoPosition(events.getStations().get(i).getLatitude(),events.getStations().get(i).getLongitude());
 		    		    
 		    Rectangle rect = mapKit.getMainMap().getViewportBounds();
 			
 			Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
 			
 			Point pnts = new Point((int)pt.getX()-rect.x,(int)pt.getY()-rect.y);
 			
 			
 			GeoPosition gp1 = new GeoPosition(events.getStations().get(i+1).getLatitude(),events.getStations().get(i+1).getLongitude());
 			
 			pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp1, mapKit.getMainMap().getZoom());
 		    		
 			Point pnts1 = new Point((int)pt.getX()-rect.x,(int)pt.getY()-rect.y);
 			
         	MmSwingPoints swingPoint = new MmSwingPoints();
         	
         	//set start and points of line
         	swingPoint.setStartPoint(pnts);
         	swingPoint.setEndPoint(pnts1);
         	
         	//set the geo position
         	swingPoint.setStartGeoPosition(gp);
         	swingPoint.setEndGeoPosition(gp1);
         	
         	swingPoint.setStationIndex(events.getStations().get(i).getStationIndex());
         	
         	if(events.getStations().get(i).getStationName().length()==8 && events.getStations().get(i+1).getStationName().length()==8)
         	{
         		swingPoint.setSwingPoint(new Point(-1,-1));
         	}
         	else
         	{
         		swingPoint.setSwingPoint(pnts1);
         	}
         	
         	swingPos.add(swingPoint);
         	
         }
         
         //JOptionPane.showMessageDialog(null, "swingPos size"+ swingPos.size());
         
         if(!geoPos.isEmpty())
         {	
             mapKit.getMainMap().setCenterPosition(geoPos.get(0));
             mapKit.getMainMap().setAddressLocation(geoPos.get(0));
         } 
         
         if(!geoPos.isEmpty())
 		{	
 		    mapMarkerWindow.setMarkerIndex(geoPos.size()+1);
 		    mapMarkerWindow.hideMarker(geoPos.size()-1);
 		}
 		
 		drawPoints();
 		
 		
 		
 		if(ledImage!=null)
 		{
 			menuItem.addMenuItem("start", ledImage,"",0);
 		}	
 		
 		
 		for(int i=0;i<menuItems.getStartEvents().getTextPaths().size();i++)
 		{
 				menuItem.addMenuItem("start", menuItems.getStartEvents().getTexts().get(i),menuItems.getStartEvents().getTextPaths().get(i), i+1);
 		}
 		
 			
 		for(int i=0;i<menuItems.getGlobalMarkers().getStations().size();i++)
 		{
 			int numLabels = menuItems.getGlobalMarkers().getStations().get(i).getActualLabelsCount();
 			
 			if(numLabels!=0)
 			{	
 			   menuItem.addMarkerMenuItem("markers",Integer.toString(numLabels),i);
 			}   
 		}
 			
 	}
 	
 	public void parseJsonObject(JSONObject jsonObject,int index)
 	{
 	
 		int stationIndex=-1;
 		try
 		{
 			if(!jsonObject.isNull("type"))
 			{	
 												
 			   if(jsonObject.optString("type").equals("region"))
 			   {
 
 				   MmStationEvents station = new MmStationEvents();
 				   
 				   String stationName = jsonObject.get("name").toString();
 				   
 				   if(stationName.indexOf("station")==0 && stationName.length()==8)
 				   {	 
 					    
 					    station.setStationName(stationName);
 					    station.setType(jsonObject.get("type").toString());
 				    	stationIndex= Integer.parseInt(stationName.substring(stationName.length()-1));
 				    	stationIndex-=1;
 				    	station.setStationIndex(stationIndex);
 				   } 	  
 				   
 				   
 				   if(stationName.contains("swingPoint") && stationName.indexOf("swingPoint")==9 && stationName.length()>8)
 				   {	 
 					    //JOptionPane.showMessageDialog(null, "Stion name "+stationName);
 					    station.setStationName(stationName);
 					    station.setType(jsonObject.get("type").toString());
 					    String stationPoint = stationName.substring(0,8);
 					    stationIndex = Integer.parseInt(stationPoint.substring(stationPoint.length()-1));
 				    	station.setStationIndex(stationIndex-1);
 				   }
 				   
 				   
 				   if(events.getStations().isEmpty())
 				   {
 					   station.setCurrentStationIndex(0);
 				   }
 				   else
 				   {	 
 					   station.setCurrentStationIndex(events.getStations().size());
 				   }    
 				   
 				   JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 				
 				   station.setGPSRadius(attributes.getInt("radius"));
 				   station.setLatLon(attributes.getDouble("latitude"), attributes.getDouble("longitude"));
 				   station.setStationType(attributes.getBoolean("swingPoint"));
 				   
 				   if(!station.getStationType())
 				        station.initializeLabels();
 				   
 				   events.getStations().add(station);
 				   
 				
 				   JSONObject actions = (JSONObject) jsonObject.get("actions");
 				
 				   JSONArray array = (JSONArray) actions.get("enter-region");
 				   
 				      
 					   String action = new String();
 					
 					   for(int i=0;i<array.length();i++)
 					   {
 						   
 						   if(isMedia(array.get(i).toString()))
 					          action = array.get(i).toString();
 					       
 					   }
 					   
 					   
 					   
 					   
 					   for(int i=0;i<readJSONObjects.length();i++)
 					   {
 						   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 						   if(jsObject.optString("type").equals("image"))
 						   {
 	 					      
 	 					      if(jsObject.get("name").toString().equals(action))
 	 					      {
 	 						       parseImageEvent(jsObject);
 	 						       break;
 	 					      }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("panorama"))
 						   {
 	 					      
 	 					      if(jsObject.get("name").toString().equals(action))
 	 					      {
 	 						       parsePanoramaEvent(jsObject);
 	 						       break;
 	 					      }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("message"))
 						   {
 						      
 						      if(jsObject.get("name").toString().equals(action))
 						      {
 							       parseMessageEvent(jsObject);
 							       break;
 						      }
 						  
 						      if(action.isEmpty())
 						      {
 							     break;
 						      }
 						 
 						   }
 						   
 						   if(jsObject.optString("type").equals("audio"))
 						   {
 	 					       
 	 					       if(jsObject.get("name").toString().equals(action))
 	 					       {
 	 						       parseAudioEvent(jsObject);
 	 						       break;
 	 					       }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("video"))
 						   {
 	 					       
 	 					       if(jsObject.get("name").toString().equals(action))
 	 					       {
 	 						       parseVideoEvent(jsObject);
 	 						       break;
 	 					       }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 				   }
 				   
 								  
 			   }	
 			}   
 			
 			if(jsonObject.optString("type").equals("generic"))
 			{
 				
 				parseGenericEvent(jsonObject);
 			}
 			
 			/*if(jsonObject.optString("type").equals("image"))
 			{
 			    parseImageEvent(jsonObject);
 			}
 			      
 		    if(jsonObject.optString("type").equals("panorama"))
 			{
 				parsePanoramaEvent(jsonObject);
 			} 
 			   
 			   
 			if(jsonObject.optString("type").equals("message"))
 			{
 				parseMessageEvent(jsonObject);
 			}*/
 			       
 			   
 			if(jsonObject.optString("type").equals("model"))
 			{
 				parseModelEvent(jsonObject);
 			}
 			       
 			   
 			/*if(jsonObject.optString("type").equals("audio"))
 			{
 				parseAudioEvent(jsonObject);
 				
 			}
 			   
 			
 			   
 			if(jsonObject.optString("type").equals("video"))
 			{
 				parseVideoEvent(jsonObject);
 			}*/
 			       
 			   
 			if(jsonObject.optString("type").equals("compass"))
 			{
 				parseCompassEvent(jsonObject);
 			}
 			
 			if(jsonObject.optString("type").equals("compassmarker"))
 			{
 				parseCompassEvent(jsonObject);
 			}
 			
 			if(jsonObject.optString("type").equals("marker"))
 			{
 			    parseMarkerEvent(jsonObject);
 			}
 			
 			if(jsonObject.optString("type").equals("backgroundimage"))
 			{
 				
 				JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 				String attrs = attributes.get("ledImage").toString();
 				ledImage=attrs;
 								
 			}
 			
 			
 				
 		}
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null,e);
 		}
 	}
 	
 	
 	public void parseGenericEvent(JSONObject jsonObject)
 	{
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("generic"))
 		      {
 		    	  		    	  
 			      JSONObject actions = (JSONObject) jsonObject.get("actions");
 			      
 			      JSONArray action = (JSONArray) actions.get("application-launched");
 			      
 			      for(int i=0;i<action.length();i++)
     			      genericEvent.addActions(action.getString(i));
 			      
 			    
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseImageEvent(JSONObject jsonObject)
 	{
 		int index=-1;
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 			    
 		      if(jsonObject.get("type").toString().equals("image"))
 		      {
 		    	  
 			      JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 			      
 			      
 			      
 			      /*Every event belong to a particular station. Every event starts with station name.
 	    		   To get the stations related event indexOf method is used, which returns the first occurrence of station name with index
 	    		   in current string. The index should be zero that is every events station name */
 		    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index = events.getStationIndex(i);
 			    		 String attrs = attributes.get("imageName").toString();
 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }    
 						 
 			    		 File imageFile = new File(openFileName.getParent()+"/images/"+attributes.get("imageName").toString());
 			    		
 			    		 if(imageFile.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,imageFile.getAbsolutePath());
 			    		 else
 			    			 showMessage(MmLanguage.language_jsonException[language][0]);
 			    		 break;
 			    	 }
 			      }
 		    	  
 		    	  /*Every event belong to a particular marker. Every event starts with marker name.
 	    		   To get the marker related event indexof method is used, which returns the first occurrence of marker name with index
 	    		   in current string. The index should be zero that is every events marker name */
 		    	  
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    		     //JOptionPane.showMessageDialog(null, jsonObject.get("name").toString());
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("imageName").toString();
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=MmLanguage.language_events[language][0];
 			    			 }    
 			    			 File imageFile = new File(openFileName.getParent()+"/images/"+attributes.get("imageName").toString());
 					    		
 				    		 if(imageFile.exists())
 							     menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,imageFile.getAbsolutePath());
 				    		 else
 				    			 showMessage(MmLanguage.language_jsonException[language][0]);
 				    		 
 			    			 break;
 			    	   }	  
 			    			      
 			      }
 			      
 			     if(jsonObject.get("name").toString().indexOf("start")==0)
 				 {
 			    	 
 				     String attrs = attributes.get("imageName").toString();
 				     menuItems.getStartEvents().getTexts().add(attrs);
 				     File filename = new File(openFileName.getParent()+"/images/"+attributes.get("imageName").toString());
 		    		  if(filename.exists())
 		    		  {	  
 			              menuItems.getStartEvents().getTextPaths().add(filename.getAbsolutePath());
 			              
 		    		  }    
 		    		  else
 		    			  showMessage(MmLanguage.language_jsonException[language][0]);
 				 } 
 			     
 			     
 			     
 			       JSONObject actions = (JSONObject) jsonObject.get("actions");
 					
 				   JSONArray array = (JSONArray) actions.get("image-disappeared");
 				   
 				      
 					   String action = new String();
 					
 					   for(int i=0;i<array.length();i++)
 					   {
 						   
 						   if(isMedia(array.get(i).toString()))
 					          action = array.get(i).toString();
 					       
 					   }
 					   
 					   
 					   
 					   
 					   for(int i=0;i<readJSONObjects.length();i++)
 					   {
 						   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 						   if(jsObject.optString("type").equals("image"))
 						   {
 	 					      
 	 					      if(jsObject.get("name").toString().equals(action))
 	 					      {
 	 						       parseImageEvent(jsObject);
 	 						       break;
 	 					      }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("panorama"))
 						   {
 	 					      
 	 					      if(jsObject.get("name").toString().equals(action))
 	 					      {
 	 						       parsePanoramaEvent(jsObject);
 	 						       break;
 	 					      }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("message"))
 						   {
 						      
 						      if(jsObject.get("name").toString().equals(action))
 						      {
 							       parseMessageEvent(jsObject);
 							       break;
 						      }
 						  
 						      if(action.isEmpty())
 						      {
 							     break;
 						      }
 						 
 						   }
 						   
 						   if(jsObject.optString("type").equals("audio"))
 						   {
 	 					       
 	 					       if(jsObject.get("name").toString().equals(action))
 	 					       {
 	 						       parseAudioEvent(jsObject);
 	 						       break;
 	 					       }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 						   
 						   if(jsObject.optString("type").equals("video"))
 						   {
 	 					       
 	 					       if(jsObject.get("name").toString().equals(action))
 	 					       {
 	 						       parseVideoEvent(jsObject);
 	 						       break;
 	 					       }
 	 					  
 	 					      if(action.isEmpty())
 	 					      {
 	 						     break;
 	 					      }
 	 					 
 						   }
 				   }
 		 	      
 		       }
 		    } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parsePanoramaEvent(JSONObject jsonObject)
 	{
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("panorama"))
 		      {
 		    	  JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 		    	  
 		    	  /*Every event belong to a particular station. Every event starts with station name.
 	    		   To get the stations related event indexof method is used, which returns the first occurrence of station name with index
 	    		   in current string. The index should be zero that is every events station name */
 		    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index = events.getStationIndex(i);
 			    		 String attrs = attributes.get("imageName").toString();
 			    		 
 			    		 attrs +=MmLanguage.language_events[language][2];
 			    		 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }    
 						 
 			    		 File filename = new File(openFileName.getParent()+"/images/"+attributes.get("imageName").toString());
 				    		
 			    		 if(filename.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,filename.getAbsolutePath());
 			    		 else
 			    			 showMessage(MmLanguage.language_jsonException[language][0]);
 			    		 break;
 			    	 }
 			      }
 		    	  
 		    	  /*Every event belong to a particular marker. Every event starts with marker name.
 	    		   To get the marker related event indexof method is used, which returns the first occurrence of marker name with index
 	    		   in current string. The index should be zero that is every events marker name */
 		    	  
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("imageName").toString();
 			    			 attrs +=MmLanguage.language_events[language][2];
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=MmLanguage.language_events[language][0];
 			    			 }    
 			    			 File filename = new File(openFileName.getParent()+"/images/"+attributes.get("imageName").toString());
 			    			 if(filename.exists())
 			    				 menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,filename.getAbsolutePath());
 			    			 else
 			    				 showMessage(MmLanguage.language_jsonException[language][0]);
 			    			 break;
 			    	   }	  
 			    			      
 			      }
 			      
 			      JSONObject actions = (JSONObject) jsonObject.get("actions");
 					
 				  JSONArray array = (JSONArray) actions.get("panorama-finished-viewing");
 				   
 				   String action = new String();
 				
 				   for(int i=0;i<array.length();i++)
 				   {
 					   
 					   if(isMedia(array.get(i).toString()))
 				          action = array.get(i).toString();
 				       
 				   }
 				   
 				   
 				   
 				   
 				   for(int i=0;i<readJSONObjects.length();i++)
 				   {
 					   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 					   if(jsObject.optString("type").equals("image"))
 					   {
  					      
  					      if(jsObject.get("name").toString().equals(action))
  					      {
  						       parseImageEvent(jsObject);
  						       break;
  					      }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("panorama"))
 					   {
  					      
  					      if(jsObject.get("name").toString().equals(action))
  					      {
  						       parsePanoramaEvent(jsObject);
  						       break;
  					      }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("message"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseMessageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("audio"))
 					   {
  					       
  					       if(jsObject.get("name").toString().equals(action))
  					       {
  						       parseAudioEvent(jsObject);
  						       break;
  					       }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("video"))
 					   {
  					       
  					       if(jsObject.get("name").toString().equals(action))
  					       {
  						       parseVideoEvent(jsObject);
  						       break;
  					       }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 				   }
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseMessageEvent(JSONObject jsonObject)
 	{
 		try
 		{
 	  	  	
 		   if(!jsonObject.isNull("type"))
 		   {	   
 			   if(jsonObject.get("type").toString().equals("message"))
 		       {
 			      JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 			      
 			      /*Every event belong to a particular station. Every event starts with station name.
 	    		   To get the stations related event indexof method is used, which returns the first occurrence of station name with index
 	    		   in current string. The index should be zero that is every events station name */
 		    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index = events.getStationIndex(i);
 			    		 String attrs = attributes.get("fileName").toString();
 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }  
 			    		 
 			    		 File filename = new File(openFileName.getParent()+"/messages/"+attributes.get("fileName").toString());
 			    		 
 			    		 if(filename.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,filename.getAbsolutePath());
 			    		 
 			    		 break;
 			    	 }
 			      }
 		    	  
 		    	  /*Every event belong to a particular marker. Every event starts with marker name.
 	    		   To get the marker related event indexof method is used, which returns the first occurrence of marker name with index
 	    		   in current string. The index should be zero that is every events marker name */
 		    	  
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("fileName").toString();
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=MmLanguage.language_events[language][0];
 			    			 }    
 			    			 
 			    			 File filename = new File(openFileName.getParent()+"/messages/"+attributes.get("fileName").toString());
 				    		 
 				    		 if(filename.exists())
 				    			 menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,filename.getAbsolutePath());
 				    		 
 			    			 break;
 			    	   }	  
 			    			      
 			      }
 			      
 			     
 			     if(jsonObject.get("name").toString().indexOf("start")==0)
 			     {
 			    	 String attrs = attributes.get("fileName").toString();
 			    	 menuItems.getStartEvents().getTexts().add(attrs);
 			    	 menuItems.getStartEvents().getTextPaths().add("");
 		    		 
 			     }
 			     
 			     JSONObject actions = (JSONObject) jsonObject.get("actions");
 					
 				  JSONArray array = (JSONArray) actions.get("message-disappeared");
 				   
 				   String action = new String();
 				
 				   for(int i=0;i<array.length();i++)
 				   {
 					   
 					   if(isMedia(array.get(i).toString()))
 				          action = array.get(i).toString();
 				       
 				   }
 				   
 				   
 				   
 				   
 				   for(int i=0;i<readJSONObjects.length();i++)
 				   {
 					   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 					   if(jsObject.optString("type").equals("image"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseImageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("panorama"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parsePanoramaEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("message"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseMessageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("audio"))
 					   {
 					       
 					       if(jsObject.get("name").toString().equals(action))
 					       {
 						       parseAudioEvent(jsObject);
 						       break;
 					       }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("video"))
 					   {
 					       
 					       if(jsObject.get("name").toString().equals(action))
 					       {
 						       parseVideoEvent(jsObject);
 						       break;
 					       }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 				   }
 			      
 		       }
 		   }  
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseModelEvent(JSONObject jsonObject)
 	{
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("model"))
 		      {
 			      JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 			   
 			      JSONObject actions = null ;
 			   
 			      /*Every event belong to a particular station. Every event starts with station name.
 	    		   To get the stations related event indexof method is used, which returns the first occurrence of station name with index
 	    		   in current string. The index should be zero that is every events station name */
 		    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index  = events.getStationIndex(i);
 			    		 String attrs = attributes.get("filename").toString();
 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }    
 			    		 
 			    		 File filename = new File(openFileName.getParent()+"/models/"+attributes.get("filename").toString());
 				    		
 			    		 if(filename.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,filename.getAbsolutePath());
 			    		 else
 			    			 showMessage(MmLanguage.language_jsonException[language][1]);
 			    		 break;
 			    	 }
 			      }
 		    	  
 		    	  /*Every event belong to a particular marker. Every event starts with marker name.
 	    		   To get the marker related event indexof method is used, which returns the first occurrence of marker name with index
 	    		   in current string. The index should be zero that is every events marker name */
 		    	  
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("filename").toString();
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=":skatt";
 			    			 }    
 			    			 File filename = new File(openFileName.getParent()+"/models/"+attributes.get("filename").toString());
 			    			 if(filename.exists())
 							     menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,filename.getAbsolutePath());
 			    			 else
 			    				 showMessage(MmLanguage.language_jsonException[language][1]);
 			    			 break;
 			    	   }	  
 			    			      
 			      }
 			   
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseAudioEvent(JSONObject jsonObject)
 	{
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("audio"))
 		      {
 			      JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 			     		      
 			      for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 //gets the current station index		    		 
 			    		 index = events.getStationIndex(i);
 			    		 //JOptionPane.showMessageDialog(null, "stationIndex "+index);
 			    		 String attrs = attributes.get("filename").toString();
 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }    
 						 
 			    		 File filename = new File(openFileName.getParent()+"/audios/"+attributes.get("filename").toString());
 				    		
 			    		 
 			    		 if(index!=-1 && filename.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,filename.getAbsolutePath());
 			    		 else
 			    			 showMessage(MmLanguage.language_jsonException[language][2]);
 			    		 
 			    		  break;
 			    	 }
 			      }
 			          
 			           			      
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    		     //JOptionPane.showMessageDialog(null, jsonObject.get("name").toString()); 
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("filename").toString();
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=MmLanguage.language_events[language][0];
 			    			 }    
 			    			 File filename = new File(openFileName.getParent()+"/audios/"+attributes.get("filename").toString());
 			    			 if(filename.exists())
 			    				 menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,filename.getAbsolutePath());
 			    			 else
 			    				 showMessage(MmLanguage.language_jsonException[language][2]);
 			    			 break;
 			    	   }	  
 			    			      
 			     }
 				
 			      
 			     if(jsonObject.get("name").toString().indexOf("start")==0)
 				 {
 			    	 String attrs = attributes.get("filename").toString();
 				     menuItems.getStartEvents().getTexts().add(attrs);
 				     
 				     File filename = new File(openFileName.getParent()+"/audios/"+attributes.get("filename").toString());
 		    		 if(filename.exists())
 			               menuItems.getStartEvents().getTextPaths().add(filename.getAbsolutePath());
 		    		 else
 		    			  showMessage(MmLanguage.language_jsonException[language][2]);
 				 } 
 			     
 			     
 			     JSONObject actions = (JSONObject) jsonObject.get("actions");
 					
 				 JSONArray array = (JSONArray) actions.get("audio-finished-playing");
 				 
 				    
 				   String action = new String();
 				
 				   for(int i=0;i<array.length();i++)
 				   {
 					   
 					   if(isMedia(array.get(i).toString()))
 				          action = array.get(i).toString();
 				       
 				   }
 				   
 				   
 				   
 				   
 				   for(int i=0;i<readJSONObjects.length();i++)
 				   {
 					   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 					   if(jsObject.optString("type").equals("image"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseImageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("panorama"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parsePanoramaEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("message"))
 					   {
 					      
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseMessageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 
 					   
 					   if(jsObject.optString("type").equals("audio"))
 					   {
 					       
 					       if(jsObject.get("name").toString().equals(action))
 					       {
 						       parseAudioEvent(jsObject);
 						       break;
 					       }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("video"))
 					   {
 					       
 					       if(jsObject.get("name").toString().equals(action))
 					       {
 						       parseVideoEvent(jsObject);
 						       break;
 					       }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 				   }
 			      
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, "error "+e);
 		}
 	}
 	
 	public void parseVideoEvent(JSONObject jsonObject)
 	{
 		try
 		{
 			
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("video"))
 		      {
 		    	  JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 		    	  
 		    	  /*Every event belong to a particular station. Every event starts with station name.
 	    		   To get the stations related event indexof method is used, which returns the first occurrence of station name with index
 	    		   in current string. The index should be zero that is every events station name */
 		    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index = events.getStationIndex(i);
 			    		 
 			    		 String attrs = attributes.get("filename").toString();
 						
 			    		 if(attributes.get("collectItem").toString().equals("true"))
 			    		 {	 
 						     attrs +=MmLanguage.language_events[language][0];
 			    		 }    
 						 
 			    		 File filename = new File(openFileName.getParent()+"/videos/"+attributes.get("filename").toString());
 			    		 if(filename.exists())
 			    		     events.getStations().get(index).setLabelsText(attrs,filename.getAbsolutePath());
 			    		 else
 			    			 showMessage(MmLanguage.language_jsonException[language][3]);
 			    		 
 			    		 break;
 			    	 }
 			      }
 		    	  
 		    	  /*Every event belong to a particular marker. Every event starts with marker name.
 	    		   To get the marker related event indexof method is used, which returns the first occurrence of marker name with index
 	    		   in current string. The index should be zero that is every events marker name */
 		    	  
 			      int markerIndex=-1;
 			      for(int i=1;i<19;i++)
 			      {
 			    	   if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 			    	   {	  
 			    			 markerIndex=i-1;
 			    			 String attrs = attributes.get("filename").toString();
 			    			 if(attributes.get("collectItem").toString().equals("true"))
 			    			 {		 
 							     attrs +=":skatt";
 			    			 }    
 			    			 File filename = new File(openFileName.getParent()+"/videos/"+attributes.get("filename").toString());
 			    			 if(filename.exists())
 			    				 menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(attrs,filename.getAbsolutePath());
 			    			 else
 			    				 showMessage(MmLanguage.language_jsonException[language][3]);
 			    			 break;
 			    	   }	  
 			    			      
 			      }
 			      
 			      
 			      		     
 		          if(jsonObject.get("name").toString().indexOf("start")==0)
 			      {
 		        	  //JOptionPane.showMessageDialog(null, "image Event "+jsonObject.get("name"));
 			          String attrs = attributes.get("filename").toString();
 			          menuItems.getStartEvents().getTexts().add(attrs);
 			          
 			          File filename = new File(openFileName.getParent()+"/videos/"+attributes.get("filename").toString());
 		    		  if(filename.exists())
 			               menuItems.getStartEvents().getTextPaths().add(filename.getAbsolutePath());
 		    		  else
 		    			  showMessage(MmLanguage.language_jsonException[language][3]);
 			      } 
 		          
 		          
 		          JSONObject actions = (JSONObject) jsonObject.get("actions");
 					
 				  JSONArray array = (JSONArray) actions.get("video-finished-playing");
 				  
 				  
 				   
 				   String action = new String();
 				
 				   for(int i=0;i<array.length();i++)
 				   {
 					   
 					   if(isMedia(array.get(i).toString()))
 				          action = array.get(i).toString();
 				       
 				   }
 				   
 				   
 				   
 				   
 				   for(int i=0;i<readJSONObjects.length();i++)
 				   {
 					   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 					   if(jsObject.optString("type").equals("image"))
 					   {
  					      
  					      if(jsObject.get("name").toString().equals(action))
  					      {
  						       parseImageEvent(jsObject);
  						       break;
  					      }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("panorama"))
 					   {
  					      
  					      if(jsObject.get("name").toString().equals(action))
  					      {
  						       parsePanoramaEvent(jsObject);
  						       break;
  					      }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   System.out.println("print data "+action);
 					   
 					   if(jsObject.optString("type").equals("message"))
 					   {
 						  
 					      if(jsObject.get("name").toString().equals(action))
 					      {
 						       parseMessageEvent(jsObject);
 						       break;
 					      }
 					  
 					      if(action.isEmpty())
 					      {
 						     break;
 					      }
 					 
 					   }
 
 					   
 					   if(jsObject.optString("type").equals("audio"))
 					   {
  					       
  					       if(jsObject.get("name").toString().equals(action))
  					       {
  						       parseAudioEvent(jsObject);
  						       break;
  					       }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 					   
 					   if(jsObject.optString("type").equals("video"))
 					   {
  					       
  					       if(jsObject.get("name").toString().equals(action))
  					       {
  						       parseVideoEvent(jsObject);
  						       break;
  					       }
  					  
  					      if(action.isEmpty())
  					      {
  						     break;
  					      }
  					 
 					   }
 				   }
 
 		    	        
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseCompassEvent(JSONObject jsonObject)
 	{
 		try
 		{
 		   if(!jsonObject.isNull("type"))
 		   {	   
 		      if(jsonObject.get("type").toString().equals("compass"))
 		      {
 		    	  //adds a compass event to a station	    	  
 		    	  for(int i=1;i<7;i++)
 			      {
 			    	
 		    		 /*Every event belong to a particular station starts with station with its index.
 		    		   To get the current event related station indexof method is used, which returns the first occurrence of station with index
 		    		   in current string. That index should be zero that is every events station name */
 		    		  
 			    	 if(jsonObject.get("name").toString().indexOf("station"+Integer.toString(i))==0)
 			    	 {
 			    		 index = i-1;
 			    		 //compass events are not shown in event list dialog,so they are added as seprate list and used when saving the file
 			    		 events.getStations().get(index).addOtherActions(jsonObject.get("type").toString());
 			    		 break;
 			    	 }
 			      }
 			    
 		     }
 		  } 
 		}   
 		catch(Exception e)
 		{
 			JOptionPane.showMessageDialog(null, e);
 		}
 	}
 	
 	public void parseMarkerEvent(JSONObject jsonObject)
 	{
 		int markerIndex=0;
 		
 		
 		
 		for(int i=1;i<19;i++)
 		{	
 			
 			try
 			{
 		       if(jsonObject.get("name").toString().indexOf("marker"+Integer.toString(i))==0)
 		       {
 		    	   
 		    	   markerIndex = i-1;
 		    	   
 		    	   JSONObject attributes = (JSONObject) jsonObject.get("attributes");
 		    	   
 		    	    String attrs =null;
 		    	    if(attributes.opt("modelName")!=null)
 		    	    {	
 		               attrs = attributes.get("modelName").toString();
 		               if(jsonObject.get("name").toString().contains("_Model"))
 		               {
 		            	   attrs +=MmLanguage.language_events[language][1];
 		            	
 		            	   MmObjWriter objRead = new MmObjWriter(openFileName.getParent()+"/osg_obj/"+attributes.get("modelName").toString());
 		            	
 		             	   String fileName = objRead.getImage();
 		            	
 		            	   File imageFile = new File(openFileName.getParent()+"/osg_obj/"+fileName);
 		            	   
 		            	   String markerName = jsonObject.get("name").toString();  
 				    	   //JOptionPane.showMessageDialog(null, markerName+"  "+markerIndex);
 						   if(markerName.contains("_Model"))
 						   {
 							   markerName = markerName.substring(0, markerName.indexOf('_') );
 							   menuItems.getGlobalMarkers().addStation(markerName, markerIndex); 
 						   }
 			    		
 						   //JOptionPane.showMessageDialog(null, markerName+"  "+markerIndex);
 						   
 				   		   if(imageFile.exists())
 							     menuItems.getGlobalMarkers().getStations().get(markerIndex).setLabelsText(fileName+MmLanguage.language_events[language][1],imageFile.getAbsolutePath());
 				   		   else
 				   			 showMessage(MmLanguage.language_jsonException[language][0]);
 		            	
 		               }
 		               
 		               if(attributes.get("collectItem").toString().equals("true"))
 				   	   {		 
 						    attrs +=MmLanguage.language_events[language][0];
 					   }
 		    	    }    
 		    	    else
 		    	    { 	
 		    	      menuItems.getGlobalMarkers().addStation(jsonObject.get("name").toString(), markerIndex);
 		    	      
 		    	      JSONObject actions = (JSONObject) jsonObject.get("actions");
 						
 					   JSONArray array = (JSONArray) actions.get("marker-found");
 					   
 					      
 						   String action = new String();
 						
 						   for(i=0;i<array.length();i++)
 						   {
 							   
 							   if(isMedia(array.get(i).toString()))
 						          action = array.get(i).toString();
 						       
 						   }
 						   
 						   
 						   
 						   
 						   for(i=0;i<readJSONObjects.length();i++)
 						   {
 							   JSONObject jsObject = readJSONObjects.getJSONObject(i);
 							   if(jsObject.optString("type").equals("image"))
 							   {
 		 					      
 		 					      if(jsObject.get("name").toString().equals(action))
 		 					      {
 		 						       parseImageEvent(jsObject);
 		 						       break;
 		 					      }
 		 					  
 		 					      if(action.isEmpty())
 		 					      {
 		 						     break;
 		 					      }
 		 					 
 							   }
 							   
 							   if(jsObject.optString("type").equals("panorama"))
 							   {
 		 					      
 		 					      if(jsObject.get("name").toString().equals(action))
 		 					      {
 		 						       parsePanoramaEvent(jsObject);
 		 						       break;
 		 					      }
 		 					  
 		 					      if(action.isEmpty())
 		 					      {
 		 						     break;
 		 					      }
 		 					 
 							   }
 							   
 							   if(jsObject.optString("type").equals("message"))
 							   {
 							      
 							      if(jsObject.get("name").toString().equals(action))
 							      {
 								       parseMessageEvent(jsObject);
 								       break;
 							      }
 							  
 							      if(action.isEmpty())
 							      {
 								     break;
 							      }
 							 
 							   }
 							   
 							   if(jsObject.optString("type").equals("audio"))
 							   {
 		 					       
 		 					       if(jsObject.get("name").toString().equals(action))
 		 					       {
 		 						       parseAudioEvent(jsObject);
 		 						       break;
 		 					       }
 		 					  
 		 					      if(action.isEmpty())
 		 					      {
 		 						     break;
 		 					      }
 		 					 
 							   }
 							   
 							   if(jsObject.optString("type").equals("video"))
 							   {
 		 					       
 		 					       if(jsObject.get("name").toString().equals(action))
 		 					       {
 		 						       parseVideoEvent(jsObject);
 		 						       break;
 		 					       }
 		 					  
 		 					      if(action.isEmpty())
 		 					      {
 		 						     break;
 		 					      }
 		 					 
 							   }
 					   }
 		    	    }  
 		    	    
 		           break;
 		       }
 		       
 		       
 		       
 		       
 			}
 			catch(Exception e)
 			{
 				JOptionPane.showMessageDialog(null, "message "+e);
 			}
 		}    
 		
 	}
 	
 	
 	public boolean isMedia(String text)
 	{
 		System.out.println("Text "+text);
 		if(text.contains("Image")||text.contains("image")||text.contains("panorama")||text.contains("audio")||text.contains("video")||
 		   text.contains("Model")||text.contains("model")||text.contains("message"))
 		{
 			return true;
 		}
 		
 		return false;
 			
 	}
 	
 	public void updateMapMarkerPositions(int x,int y)
 	{
 		mousePos.clear();
 		
 			
 		for (final GeoPosition gp : geoPos) {
             // convert geo to world bitmap pixel
             final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
             
             MmMousePoints pnts = new MmMousePoints((int)(pt.getX()-x),(int)(pt.getY()-y));
             pnts.drawRegion();
             
             mousePos.add(pnts);
             
         }
 		
 		if(!swingPos.isEmpty())
 		{
 			for(int i=0;i<swingPos.size();i++)
 			{
 				//swingPos.get(i).print();
 				GeoPosition gp = swingPos.get(i).getStartGeoPosition();
 				
 				final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
 				
 				swingPos.get(i).setStartPoint(new Point((int)(pt.getX()-x),(int)(pt.getY()-y)));
 				
 				final Point2D pt1 = mapKit.getMainMap().getTileFactory().geoToPixel(swingPos.get(i).getEndGeoPosition(), mapKit.getMainMap().getZoom());
 				
 				swingPos.get(i).setEndPoint(new Point((int)(pt1.getX()-x),(int)(pt1.getY()-y)));
 				
 			}
 		}
 		
 		
 		
 	}
 	
 
     public void drawPoints()
     {
     	
     		    	
     	final Painter<JXMapViewer> lineOverlay = new Painter<JXMapViewer>() {
 
             @Override
             public void paint(Graphics2D g, final JXMapViewer map, final int w, final int h) {
                 g = (Graphics2D) g.create();
                 // convert from viewport to world bitmap
                 final Rectangle rect = mapKit.getMainMap().getViewportBounds();
                 
                 
                 
                 System.out.println("rect "+rect);
                 
                 //g.translate(-rect.x, -rect.y);
                 
                 if(!isPrintSelected)
                 {	
                     int indx = -1;
                 
                     geoPoints.clear();
                 
                 //JOptionPane.showMessageDialog(null, "swingPos "+swingPos.size());
                 
                 if(swingPos.size()==0||swingPos.size()==1)
                 {
                 	for(int i=0;i<geoPos.size();i++)
                 	{	
                 	    geoPoints.add(geoPos.get(i));
                 	}    
                 }
                 else
                 {	
                 	  geoPoints.add(getGeoPosition(swingPos.get(0).getStartPoint()));
                 	  geoPoints.add(getGeoPosition(swingPos.get(0).getEndPoint()));
                 	  
                 	  for(int i=1;i<swingPos.size();i++)
                       {
                 		   geoPoints.add(getGeoPosition(swingPos.get(i).getEndPoint()));
                       }
                 	    
                 	  
                 }
                 
                                           
                 // do the drawing
                 g.setColor(Color.BLACK);
                 g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g.setStroke(new BasicStroke(2));
 
                 int lastX = -1;
                 int lastY = -1;
                 for (int i=0;i<events.getStations().size();i++) {
                     // convert geo to world bitmap pixel
                 	GeoPosition gp = new GeoPosition(events.getStations().get(i).getLatitude(),events.getStations().get(i).getLongitude());
                     final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                     if (lastX != -1 && lastY != -1) {
                         g.drawLine(lastX, lastY, (int) (pt.getX()-rect.x), (int) (pt.getY()-rect.y));
                         
                     }
                     lastX = (int) (pt.getX()-rect.x);
                     lastY = (int) (pt.getY()-rect.y);
                     
                     
                     
                 }
 
                 
                 for (final GeoPosition gp : geoPos) {
                     // convert geo to world bitmap pixel
                 	
                 	
                 	
                     final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                     
                     g.setPaint(new Color(182,81,74,125));
                     g.fillOval((int)(pt.getX()-rect.x)-20, (int)(pt.getY()-rect.y)-25, 40, 40);  
                     
                     
                 }
                 
                 
                 //g.drawImage(geoMarker, -geoMarker.getHeight(null)/2+5, -geoMarker.getHeight(null)/2-15, null);
                 
                 int imageIndex=0;
                 
                 for (final GeoPosition gp : geoPos) {
                     // convert geo to world bitmap pixel
                 	
                 	//Image geoMarker = new ImageIcon(System.getProperty("user.dir")+"/images/map_marker_"+Integer.toString(imageIndex+1)+".png").getImage();
                 	ImageIcon geoMarker = new ImageIcon(getClass().getResource("/map_marker_"+Integer.toString(imageIndex+1)+".png"));
                 	
                     final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                     
                     pt.setLocation(pt.getX()-geoMarker.getIconWidth()/2,pt.getY()-geoMarker.getIconHeight()/2-20);
                     g.drawImage(geoMarker.getImage(), (int)(pt.getX()-rect.x),(int)(pt.getY()-rect.y), null);  
                     
                     imageIndex++;
                 }
                 
                
                 
                 
                 /*for (final GeoPosition gp : swingPoints) {
                     // convert geo to world bitmap pixel
                 	
                     final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                     
                     //pt.setLocation(pt.getX()-geoMarker.getHeight(null)/2,pt.getY()-geoMarker.getHeight(null)/2-20);
                     g.setPaint(Color.black);
                     g.fillOval((int)(pt.getX()-rect.x)-5, (int)(pt.getY()-rect.y)-5, 10, 10);
                     
                     
                 }*/
                 
                 /*for(int i=0;i<swingPos.size()-1;i++)
                 {
                     
                 	if(i==0)
                 	{	
                 	    final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(swingPos.get(i).getEndGeoPosition(), mapKit.getMainMap().getZoom());
                         //pt.setLocation(pt.getX()-geoMarker.getHeight(null)/2,pt.getY()-geoMarker.getHeight(null)/2-20);
                         g.setPaint(Color.black);
                         g.fillOval((int)(pt.getX()-rect.x)-5, (int)(pt.getY()-rect.y)-5, 10, 10);
                 	}
                 	else if(swingPos.get(i-1).getStationIndex()==swingPos.get(i).getStationIndex())
                 	{
                 		final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(swingPos.get(i).getEndGeoPosition(), mapKit.getMainMap().getZoom());
                         //pt.setLocation(pt.getX()-geoMarker.getHeight(null)/2,pt.getY()-geoMarker.getHeight(null)/2-20);
                         g.setPaint(Color.black);
                         g.fillOval((int)(pt.getX()-rect.x)-5, (int)(pt.getY()-rect.y)-5, 10, 10);
                 	}
                 }*/
                 
                 for(int i=0;i<events.getStations().size();i++)
                 {
                 	if(events.getStations().get(i).getStationType())
                 	{
                 		GeoPosition gp = new GeoPosition(events.getStations().get(i).getLatitude(),events.getStations().get(i).getLongitude());
                 		final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                         //pt.setLocation(pt.getX()-geoMarker.getHeight(null)/2,pt.getY()-geoMarker.getHeight(null)/2-20);
                         g.setPaint(Color.black);
                         g.fillOval((int)(pt.getX()-rect.x)-5, (int)(pt.getY()-rect.y)-5, 10, 10);
                 	}
                 }
                 
                 }
                 else
                 {
                 	int imageIndex=0;
                     
                     for (final GeoPosition gp : geoPos) {
                         // convert geo to world bitmap pixel
                     	
                     	//Image geoMarker = new ImageIcon(System.getProperty("user.dir")+"/markers/map_marker"+Integer.toString(imageIndex+1)+".png").getImage();
                     	Image geoMarker = new ImageIcon(getClass().getResource("/map_marker"+Integer.toString(imageIndex+1)+".png")).getImage();
                     	
                         final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                         
                         pt.setLocation(pt.getX()-geoMarker.getHeight(null)/2,pt.getY()-geoMarker.getHeight(null)/2-20);
                         g.drawImage(geoMarker, (int)(pt.getX()-rect.x),(int)(pt.getY()-rect.y), null);  
                         
                         imageIndex++;
                     }
                     
                     imageIndex = 2;
                     
                     /*for (final GeoPosition gp : geoPos) {
                         // convert geo to world bitmap pixel
                     	
                     	                    	
                         final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
                         
                         g.setPaint(Color.black);
                         g.drawString(Integer.toString(imageIndex-1)+":patt.map_marker"+Integer.toString(imageIndex), (int)(pt.getX()-rect.x-20),(int)(pt.getY()-rect.y+20));  
                         
                         imageIndex++;
                     }*/
                 }
                 
                             
                 g.dispose();
                 
                 
                 updateMapMarkerPositions(rect.x,rect.y);
                  
                 
                 
                 
             }
 
         };
         
         
 
         mapKit.getMainMap().setOverlayPainter(lineOverlay);
         mapKit.getMainMap().repaint();
         
     }
     
  
 	
     
     public void printMap()
     {
     	PrinterJob printer = PrinterJob.getPrinterJob();
     	printer.setPrintable(this);
     	boolean ok = printer.printDialog();
     	
     	if(ok)
     	{
     		try
     		{
     			PageFormat pPageFormat = printer.defaultPage();
     			Paper pPaper = pPageFormat.getPaper();
     			pPaper.setImageableArea(1.0, 1.0, pPaper.getWidth(), pPaper.getHeight());
     			pPageFormat.setPaper(pPaper);
     			pPageFormat = printer.pageDialog(pPageFormat);
     			Book pBook = new Book();
     			pBook.append(this, pPageFormat);
     			printer.setPageable(pBook);
     			printer.print();
     			
     			
     		}
     		catch(Exception e)
     		{
     			JOptionPane.showMessageDialog(this, e);
     		}
     	}
     	
     	
     }
     
     public int print(Graphics g,PageFormat print,int page) throws PrinterException
     {
     	
     	if (page > 0) {
             return NO_SUCH_PAGE;
        }
 
        // User (0,0) is typically outside the
        // imageable area, so we must translate
        // by the X and Y values in the PageFormat
        // to avoid clipping.
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint
        (RenderingHints.KEY_ANTIALIASING, 
          RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(print.getImageableX(), print.getImageableY());
   
       
 
        mapKit.getMainMap().printAll(g2d);
       
 
        // tell the caller that this page is part
        // of the printed document
        return PAGE_EXISTS;
     	
         	
     }
     
     public void showMessage(String message)
     {
     	JOptionPane.showMessageDialog(mapKit, message);
     }
 }
 
 //commented code of mouseClick function
 /*if(drawPnts)
 {	
 	       			
     widthx = e.getX();
     heighty = e.getY();
 
     mousex=e.getX();
     mousey=e.getY();
 
     Rectangle rect = mapKit.getMainMap().getViewportBounds();
     
     System.out.println("mouse pos "+widthx+"  "+heighty);
 
 
    widthx = widthx+rect.x;
    heighty = heighty+rect.y;
 
            
 
 
   TileFactoryInfo titleInfo = mapKit.getMainMap().getTileFactory().getInfo();
 
 
 
  int zoom = mapKit.getMainMap().getZoom();
 
  double lan = (widthx-titleInfo.getMapCenterInPixelsAtZoom(zoom).getX())/titleInfo.getLongitudeDegreeWidthInPixels(zoom);
  double lat_coord = (heighty-titleInfo.getMapCenterInPixelsAtZoom(zoom).getY())/(-1*titleInfo.getLongitudeRadianWidthInPixels(zoom));
 
  double lat = (2*Math.atan(Math.exp(lat_coord))-Math.PI/2) / (Math.PI / 180.0);
 
    	        
 wayPoints.clear();
 wayLines.clear();
 
 int geoPnts = geoPos.size();
 
 if(geoPnts==0) 
 {	
 	geoPos.add(getGeoPosition(e.getPoint()));
 	
 	markGeoPos.setWaypoints(wayPoints);
 	mousePos.add(new MmMousePoints(mousex,mousey));
 	mousePos.get(mousePos.size()-1).drawRegion();
 	mousePos.get(mousePos.size()-1).setClickCount();
 	
 	if(markersDialog.labelMoved)
 	{	
 	   mapMoveLabel.setVisible(false);
 	   markersDialog.labelMoved=false;
 	   markersDialog.markerIndex+=1;
 	   markersDialog.selectMarker(markersDialog.markerIndex);
 	   markersDialog.setLabelIcon(markersDialog.markerIndex);
 	}   
 	
 }	
 
 if(geoPnts>=1)
 {
 	
 	plotPnt = checkDistance(getGeoPosition(e.getPoint()));
 	
 	if(plotPnt)
 	{	
 	      	        	 
 	   //if(markersDialog.labelMoved)
 		   {
 		   geoPos.add(getGeoPosition(e.getPoint()));
     	   markGeoPos.setWaypoints(wayPoints);
     	   mousePos.add(new MmMousePoints(mousex,mousey));
     	   mousePos.get(mousePos.size()-1).drawRegion();
     	   mousePos.get(mousePos.size()-1).setClickCount();
     	   
 	       mapMoveLabel.setVisible(false);
 		       markersDialog.labelMoved=false;
 		       markersDialog.markerIndex+=1;
 		       markersDialog.selectMarker(markersDialog.markerIndex);
 		       markersDialog.setLabelIcon(markersDialog.markerIndex);
 		   }
 	   
 	   
 	}  
 	else
 	{
 		boolean isPointPresent = false;
 		for(int i=0;i<mousePos.size();i++)
 		{ 
 			isPointPresent = mousePos.get(i).isPointInRegion(new MmMousePoints(mousex,mousey));
 			if(!isPointPresent)
 				continue;
 			else
 			{
 				isPointPresent=true;
 				index=i;
 				break;
 			}	
 					
 		}
 		
 		if(!isPointPresent)
 		    JOptionPane.showMessageDialog(panel, "Cannot Add a Position");
 		else
 		{	
 			
 		    
 		    frame1.setLocation(mousex+250, mousey-125);
 		    frame1.setVisible(true);
 		    events.addStation(geoPos.get(index).getLatitude(), geoPos.get(index).getLongitude(), index);
 		    
 		}  
 		
 	}
   
 }
 
 for(int i=0;i<geoPos.size();i++)
 {
 	wayPoints.add(new Waypoint(geoPos.get(i).getLatitude(),geoPos.get(i).getLongitude()));
 }
 
 
 if(geoPos.size()>1)
 {	
    for(int i=0;i<geoPos.size()-1;i++)
    {
 	   wayLines.add(new Waypoint(geoPos.get(i).getLatitude(),geoPos.get(i).getLongitude()));
    }
    markGeoPos1.setWaypoints(wayLines);
 }    
    
   	        
 markGeoPos.setRenderer(new WaypointRenderer(){
 	
 	public boolean paintWaypoint(Graphics2D graph,JXMapViewer map,Waypoint wp)
 	{
 		Image geoMarker = new ImageIcon("/Users/Umapathi/Desktop/MinnesmarkEditor/images/map_marker.png").getImage();
 		
 		BasicStroke stroke = new BasicStroke(2.0f);
 		
 		graph.setRenderingHint
         (RenderingHints.KEY_ANTIALIASING, 
           RenderingHints.VALUE_ANTIALIAS_ON);
         graph.setStroke( stroke );
         
         
         mapKit.getMainMap().repaint();
         
         graph.drawOval(0,0, 15, 15);
         
         graph.drawImage(geoMarker, -geoMarker.getHeight(null)/2+5, -geoMarker.getHeight(null)/2-15, null);
         
 		return true;
 	}
 });
 
 prev_mousex =  mousex;
 prev_mousey = mousey;
 mapKit.getMainMap().setOverlayPainter(markGeoPos);
 mapKit.getMainMap().repaint();
 
 }*/
 
 //commented code of drawPoints function
 /*wayPoints.clear();
 
 geoPnts = pnts;
 
 for(int i=0;i<mapMarkers.size();i++)
 {
 	if(mapMarkers.get(i).getWindowDisplacment())
 	{
 		markersPlaced++;
 	}
 }
 
 //if(markersPlaced==0)
 {
 	for(int i=0;i<geoPos.size();i++)
     {
     	 wayPoints.add(new Waypoint(geoPos.get(i).getLatitude(),geoPos.get(i).getLongitude()));
     }
        
      markGeoPos.setWaypoints(wayPoints);
 }
 
 //if(markersPlaced>1)
 {
 	if(geoPos.size()>1)
     {	
 		
        for(int i=0;i<geoPos.size();i++)
        {
     	    wayPoints.add(new Waypoint(geoPos.get(i).getLatitude(),geoPos.get(i).getLongitude()));
        }
        
        markGeoPos.setWaypoints(wayPoints);
        
     }
 }		 
 
 
 
 		markGeoPos.setRenderer(new WaypointRenderer(){
     	
     	public boolean paintWaypoint(Graphics2D graph,JXMapViewer map,Waypoint wp)
     	{
     		Image geoMarker = new ImageIcon("/Users/Umapathi/Desktop/MinnesmarkEditor/images/map_marker.png").getImage();
 
     		    		
     		
     		BasicStroke stroke = new BasicStroke(2.0f);
     		
     		graph.setRenderingHint
             (RenderingHints.KEY_ANTIALIASING, 
               RenderingHints.VALUE_ANTIALIAS_ON);
             graph.setStroke( stroke );
             
             
             mapKit.getMainMap().repaint();
             
             
             /*if(geoPnts.size()>1 && indexPos<geoPos.size()-1)
             {
                int lat,lan,lat1,lan1;
                                   
                	                   
                if(geoPos.get(geoPos.size()-1).getLatitude()!=wp.getPosition().getLatitude() && geoPos.get(geoPos.size()-1).getLongitude()!=wp.getPosition().getLongitude())
                {
             	   
             	   lat=geoPnts.get(indexPos).x;
             	   lat1 =geoPnts.get(indexPos+1).x; 
             	   
             	   lan=geoPnts.get(indexPos).y;
             	   lan1 =geoPnts.get(indexPos+1).y; 
             	                   	   
             	  
             	  if(lan1<lan && lat<lat1)
             	  {	  
                       //graph.drawLine(0,0, geoPnts.get(indexPos+1).x-geoPnts.get(indexPos).x, geoPnts.get(indexPos+1).y-geoPnts.get(indexPos).y);
             		  graph.drawLine(0,0, (lat1-lat), - (lan1-lan));
             	  } 
             	  
             	  if(lan<lan1 && lat<lat1)
             	  {	  
                       //graph.drawLine(0,0, geoPnts.get(indexPos+1).x-geoPnts.get(indexPos).x, geoPnts.get(indexPos+1).y-geoPnts.get(indexPos).y);
             		  graph.drawLine(0,0, (lat1-lat),  (lan1-lan));
             	  } 
             	  
             	  if(lat1<lat && lan1<lan)
             	  {	  
                       //graph.drawLine(0,0, geoPnts.get(indexPos+1).x-geoPnts.get(indexPos).x, geoPnts.get(indexPos+1).y-geoPnts.get(indexPos).y);
             		  graph.drawLine(0,0, - (lat1-lat), - (lan1-lan));
             	  } 
             	  
             	  if(lat1<lat && lan<lan1)
             	  {	  
                       //graph.drawLine(0,0, geoPnts.get(indexPos+1).x-geoPnts.get(indexPos).x, geoPnts.get(indexPos+1).y-geoPnts.get(indexPos).y);
             		  graph.drawLine(0,0, -(lat1-lat),  (lan1-lan));
             	  } 
             	  
             	  
             	  
             	  
                }   
                
             	   
             }  
             else
                graph.drawLine(0,0, 0,0); 
             
             
             int lastX = 0;
             int lastY = 0;
             for (final Point gp : geoPnts) {
                 // convert geo to world bitmap pixel
                 //final Point2D pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom());
             	Point2D pt = gp;
                 if (lastX != -1 && lastY != -1) {
                     graph.drawLine(lastX, lastY, (int) (pt.getX()-lastX), (int) (pt.getY()-lastY));
                     System.out.println("data");
                 }
                 lastX = (int) pt.getX()-lastX;
                 lastY = (int) pt.getY()-lastY;
             }
                
             
             indexPos++;
             
             if(indexPos>=geoPos.size())
             	indexPos=0;
             
             graph.drawImage(geoMarker, -geoMarker.getHeight(null)/2+5, -geoMarker.getHeight(null)/2-15, null);
             
     		return true;
     	}
     });
 	
 	mapKit.getMainMap().setOverlayPainter(markGeoPos);
     mapKit.getMainMap().repaint();
 	mapKit.setFocusable(true);*/
 
