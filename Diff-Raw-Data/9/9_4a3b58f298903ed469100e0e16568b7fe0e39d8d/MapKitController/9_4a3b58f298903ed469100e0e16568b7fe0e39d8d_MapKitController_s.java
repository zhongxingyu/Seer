 package com.md.dm.infovis.vast.controller;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JDialog;
 
 import org.jdesktop.swingx.JXMapKit;
 import org.jdesktop.swingx.JXMapViewer;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
 import org.jdesktop.swingx.painter.CompoundPainter;
 import org.jdesktop.swingx.painter.Painter;
 
 import com.md.dm.infovis.vast.map.EmptyTileFactory;
 import com.md.dm.infovis.vast.map.HoverLabelManager;
 import com.md.dm.infovis.vast.map.PieChartWaypontPainter;
 import com.md.dm.infovis.vast.map.PolygonPainter;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.QueryBuilder;
 
 public class MapKitController {
 
 	private JXMapKit mapKit;
 
 	// TODO: Remove this and inject
 	private DataController dataController;
 
 	private HoverLabelManager hoverLabelManager;
 	
 	public MapKitController(JXMapKit mapKit) throws Exception {
 		super();
 		this.mapKit = mapKit;
 		dataController = new DataController();
 	}
 
 	public MapKitController() throws Exception {
 		mapKit = new JXMapKit();
 		// dataController = new DataController();
 
 		final int max = 17;
 		TileFactoryInfo info = new TileFactoryInfo(1, max - 2, // max allowed
 																// level
 				max, // max level
 				256, // tile size
 				true, true, // x/y orientation is normal
 				"file://Users/diego/Documents/Maestria/VI/vast2012/tiles/", // base
 																			// url
 				"x", "y", "z" // url args for x, y & z
 		) {
 			public String getTileUrl(int x, int y, int zoom) {
 
 				return this.baseURL + (this.getMaximumZoomLevel() - zoom) + "/" + x + "/" + y
 						+ ".png";
 			}
 		};
 
		// mapKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
		mapKit.setTileFactory(new EmptyTileFactory());
 		// mapKit.setTileFactory(new DefaultTileFactory(info));
 
 		mapKit.setAddressLocationShown(true);
 		mapKit.setDataProviderCreditShown(true);
 		// jXMapKit.setCenterPosition(new GeoPosition(0, 0));
 		mapKit.setMiniMapVisible(true);
 		mapKit.setZoomSliderVisible(true);
 		mapKit.setZoomButtonsVisible(true);
 		mapKit.setAddressLocationShown(true);
		mapKit.getMainMap().setDrawTileBorders(true);
		mapKit.getMainMap().setHorizontalWrapped(true);
 		mapKit.getMainMap().setRecenterOnClickEnabled(true);
 		mapKit.getMainMap().setRestrictOutsidePanning(true);
 		mapKit.getMainMap().setPanEnabled(true);
 		mapKit.setZoom(mapKit.getMainMap().getTileFactory().getInfo().getMinimumZoomLevel());
 
 		hoverLabelManager = new HoverLabelManager(mapKit);
 		mapKit.getMainMap().addMouseMotionListener(hoverLabelManager);
 
 		
 		DataController machineDataController = new DataController(new Mongo("localhost:27022"), "vast", "region");
 		DBCursor cursor = machineDataController.find(QueryBuilder.start().get());
 		//hoverLabelManager
 		
 		// ((DefaultTileFactory) mapKit.getMainMap().getTileFactory())
 		// .setThreadPoolSize(8);
 		//
 		// CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter(
 		// new PieChartWaypontPainter(dataController.filter("region-1",
 		// "branch1")), new PolygonPainter());
 		//
 		// compoundPainter.setCacheable(false);
 		//
 		//
 		//
 		// this.setOverlayPainter(compoundPainter);
 		mapKit.setCenterPosition(new GeoPosition(45, -90));
 		// jXMapKit.setAddressLocation(new GeoPosition(1, 1));
 		mapKit.setZoom(15);
 //		((DefaultTileFactory) mapKit.getMainMap().getTileFactory())
 //				.setThreadPoolSize(8);
 //
 //		CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter(
 //				new PieChartWaypontPainter(dataController.filter("region-1", "branch1")), new PolygonPainter());
 //		
 //		compoundPainter.setCacheable(false);
 //
 //		
 //		
 //		this.setOverlayPainter(compoundPainter);
 	}
 
 	public void showData(DBObject dBObject) {
 		CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter(
 				new PieChartWaypontPainter(dBObject), new PolygonPainter());
 
 		compoundPainter.setCacheable(false);
 
 		this.setOverlayPainter(compoundPainter);
 	}
 	
 	public void showData(DBCursor cursor){
 		
 		CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter(
 				new PieChartWaypontPainter(cursor), new PolygonPainter());
 		
 		compoundPainter.setCacheable(false);
 
 		this.setOverlayPainter(compoundPainter);
 	}
 		
 		
 	public void addWaypoints() {
 
 	}
 
 	private void setOverlayPainter(Painter<JXMapViewer> painter) {
 		mapKit.getMainMap().setOverlayPainter(painter);
 	}
 
 	public JXMapKit getMapKit() {
 		return mapKit;
 	}
 
 	public void setMapKit(JXMapKit mapKit) {
 		this.mapKit = mapKit;
 	}
 
 	@Override
 	public String toString() {
 		return "MapKitController [mapKit=" + mapKit + "]";
 	}
 
 	public static void main(String[] args) {
 
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				JDialog dialog = new JDialog(new javax.swing.JFrame(), true);
 				dialog.getContentPane().setLayout(new BorderLayout());
 				try {
 					dialog.add(new MapKitController().getMapKit(), BorderLayout.CENTER);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 				dialog.setSize(600, 400);
 				dialog.setLocationRelativeTo(null);
 
 				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 					public void windowClosing(java.awt.event.WindowEvent e) {
 						System.exit(0);
 					}
 				});
 				dialog.setVisible(true);
 			}
 		});
 	}
 }
