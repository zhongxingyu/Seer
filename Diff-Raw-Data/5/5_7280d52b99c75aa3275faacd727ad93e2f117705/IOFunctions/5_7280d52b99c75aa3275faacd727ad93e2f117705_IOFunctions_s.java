 package org.gpsanonymity.io;
 
 
 
 
 
 
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.gpsanonymity.merge.MergeGPS;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.data.gpx.GpxData;
 import org.openstreetmap.josm.data.gpx.GpxRoute;
 import org.openstreetmap.josm.data.gpx.GpxTrack;
 import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
 import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
 import org.openstreetmap.josm.data.gpx.ImmutableGpxTrackSegment;
 import org.openstreetmap.josm.data.gpx.WayPoint;
 import org.openstreetmap.josm.io.GpxReader;
 import org.openstreetmap.josm.io.GpxWriter;
 import org.xml.sax.SAXException;
 
 
 public class IOFunctions {
 	
 	//number of nearest waypoints
 	
 	public static void exportWayPoints(List<WayPoint> wayPoints,
 			String string){
 		GpxData gpxd = new GpxData();
 		gpxd.waypoints=wayPoints;
 		FileOutputStream fos;
 		try {
 			fos = new FileOutputStream(new File(string));
 			GpxWriter gpxWriter = new GpxWriter(fos);
 			gpxWriter.write(gpxd);
 		} catch (FileNotFoundException e) {
 			System.out.println("File "+string+" not found.");
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			System.out.println("Unsupported encoding.");
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 	}
 	public static void exportTrackSegments(Collection<GpxTrackSegment> segs,
 			String string){
 		List<GpxTrack> tracks = new LinkedList<GpxTrack>();
 		for (GpxTrackSegment seg : segs) {
 			Collection<Collection<WayPoint>> trackSegs = new LinkedList<Collection<WayPoint>>();
 			trackSegs.add(seg.getWayPoints());
 			tracks.add(new ImmutableGpxTrack(trackSegs, new HashMap<String, Object>()));
 		}
 		exportTracks(tracks, string);
 	}
 	public static void exportTracks(List<GpxTrack> tracks,
 			String string){
 		GpxData gpxd = new GpxData();
 		gpxd.tracks=tracks;
 		FileOutputStream fos;
 		try {
 			fos = new FileOutputStream(new File(string));
 			GpxWriter gpxWriter = new GpxWriter(fos);
 			gpxWriter.write(gpxd);
 			fos.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("File "+string+" not found.");
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			System.out.println("Unsupported encoding.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 	}
 	public static LinkedList<GpxTrackSegment> getAllTrackSegments(
 			GpxReader reader) {
 		LinkedList<GpxTrackSegment> result = new LinkedList<GpxTrackSegment>();
 		for (Iterator<GpxTrack> gpxIterator = reader.data.tracks.iterator(); gpxIterator.hasNext();) {
 			GpxTrack track = gpxIterator.next();
 			result.addAll(track.getSegments());
 		}
 		return result;
 	}
 	public static LinkedList<WayPoint> getAllWaypoints(GpxReader reader) {
 		LinkedList<WayPoint> result = new LinkedList<WayPoint>();
 		for (Iterator<GpxRoute> gpxRoute = reader.data.routes.iterator(); gpxRoute.hasNext();) {
 			GpxRoute route = (GpxRoute) gpxRoute.next();
 			result.addAll(route.routePoints);
 		}
         //For all Tracks get all TrackSeqments and add the Waypoints to to the LinkedList waypoints
         for (Iterator<GpxTrack> gpxIterator = reader.data.tracks.iterator(); gpxIterator.hasNext();) {
 			GpxTrack track = gpxIterator.next();
 			for (Iterator<GpxTrackSegment> trackseg = track.getSegments().iterator(); trackseg.hasNext();) {
 				GpxTrackSegment seq = (GpxTrackSegment) trackseg.next();
 				result.addAll(seq.getWayPoints());
 			}
 		}
         //add all waypoints
         result.addAll(reader.data.waypoints);
         System.out.println("Number of Waypoints:" + result.size());
 		return result;
 	}
 	public static LinkedList<WayPoint> getAllWaypoints(List<GpxTrackSegment> segments) {
 		LinkedList<WayPoint> result = new LinkedList<WayPoint>();
 		//For all Tracks get all TrackSeqments and add the Waypoints to to the LinkedList waypoints
 
 		for (Iterator<GpxTrackSegment> trackseg = segments.iterator(); trackseg.hasNext();) {
 			GpxTrackSegment seq = (GpxTrackSegment) trackseg.next();
 			result.addAll(seq.getWayPoints());
 		}
 		return result;
 	}
 	/**
 	 * imports the waypoints from a gpx file given in f and put it in to waypoints
 	 * 1. the routewaypoints
 	 * 2. the track waypoints
 	 * 3. the waypoints
 	 * 
 	 * @param f gpx file
 	 * @param waypoints 
 	 * @throws IOException if the file is not readable
 	 * @throws SAXException cant parse the gpx file f
 	 */
 	public static GpxReader importGPX(String fPath) {
 		try{
 			File f = new File(fPath);
 			FileInputStream fis = new FileInputStream(f);
 			final GpxReader r = new GpxReader(fis);
 			r.parse(true);
 			System.out.println("Has RoutePoints:" + r.data.hasRoutePoints());
 			System.out.println("Has Trackpoints:" + r.data.hasTrackPoints());
 			System.out.println("No WayPoints:" + !r.data.waypoints.isEmpty());
 			return r;
 		}catch (IOException e) {
 			System.out.println(fPath + " is not readable.");
 			e.printStackTrace();
 		} catch (SAXException e) {
 			System.out.println("No GPX file or not parsable.");
 			e.printStackTrace();
 		}
         return null;
         
 	}
 	public static List<GpxTrack> getAllTracks(GpxReader reader) {
 		return new LinkedList<GpxTrack>(reader.data.tracks);
 	}
 	public static List<WayPoint> getAllWaypointsFromTrack(List<GpxTrack> tracks) {
 		List<WayPoint> result = new LinkedList<WayPoint>();
 		for (GpxTrack gpxTrack : tracks) {
 			for (GpxTrackSegment seg : gpxTrack.getSegments()) {
 				for (WayPoint wayPoint : seg.getWayPoints()) {
 					result.add(wayPoint);
 				}
 			}
 		}
 		return result;
 	}
 	public static void getDataFromOSMWithCutting(Bounds bounds, String filename, String tempFile){
 		int coorMax = (int)Math.ceil(bounds.getArea()/0.01);
 		Integer x=0,y=-1;
 		LinkedList<Bounds> currentBounds = splittingBounds(bounds,0.01);
 		LinkedList<Bounds> spaceBounds= new LinkedList<Bounds>();
 		try{
 			for (Bounds bounds2 : currentBounds) {
 				spaceBounds.add(MergeGPS.getBoundsWithSpace(bounds2, 300));
 			}
			exportBoundsAsTracks(spaceBounds, "output/CuttingSpaceBounds.gpx");
 			Iterator<Bounds> spaceIter = spaceBounds.iterator();
 			Iterator<Bounds> boundsIter = currentBounds.iterator();
 			System.out.println("Downloading and Cutting... ");
 			while(spaceIter.hasNext() && boundsIter.hasNext()){
 				y++;
 				if(y>=coorMax){
 					y=0;
 					x++;
 				}
 				String coords=x.toString()+"_"+y.toString();
 				String realFilename=filename.replace(".gpx", coords+".gpx");
 				File file = new File(realFilename);
 				if (!file.exists()){
 					int tempFileCounter=0;
 					Bounds spaceBound = spaceIter.next();
 					Bounds currentBound = boundsIter.next();
 					Collection<GpxTrack> tempTracks = new LinkedList<GpxTrack>();
 					tempFileCounter=downloadingArea(spaceBound,tempFile,tempTracks,tempFileCounter);
 					for(int i=-1;i<tempFileCounter;i++){
 						if(i!=-1){
 							FileInputStream fis = new FileInputStream(new File(tempFile.replace(".gpx", i+".gpx")));
 							GpxReader tempReader = new GpxReader(fis);
 							tempReader.parse(false);
 							tempTracks.addAll(tempReader.data.tracks);
 							fis.close();
 						}
 					}
 					List<GpxTrack> exportTracks = cutAndCleanTracks(currentBound, tempTracks);
 					/*String coords="Min_"
 						+currentBound.getMin().getY()
 						+"_"
 						+currentBound.getMin().getX()
 						+currentBound.getMax().getY()
 						+currentBound.getMax().getX();*/
 
 					System.out.println("Exporting to " + realFilename);
 					exportTracks(exportTracks, realFilename);
 				}else{
 					System.out.println(realFilename+" exists already.");
 				}
 			}	
 			generateDatFile(filename,bounds,coorMax);
 		}catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public static void generateDatFile(String filename, Bounds bounds, int coorMax) {
 		try {
 		FileOutputStream fos;
 		
 			fos = new FileOutputStream(new File(filename.replace(".gpx", ".dat"))); 
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(new SerilizableBounds(bounds));
 			oos.writeInt(coorMax);
 			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	public static List<GpxTrack> getDataFromOSMWithOriginalGPXFiles(Bounds bounds, String filename, String tempFile){
 		Integer countTempFiles=0;
 		List<GpxTrack> allTracks = new LinkedList<GpxTrack>();
 		List<GpxTrack> result = null;
 		try {
 			LinkedList<Bounds> currentBounds = splittingBounds(bounds,0.2);
 			List<GpxTrack> tempTracks=new LinkedList<GpxTrack>();
 			System.out.println("Downloading ...");
 			
 			for(Bounds currentBound:currentBounds){
 				countTempFiles=downloadingArea(currentBound,tempFile,tempTracks,countTempFiles);
 			}
 			System.out.println("Downloading Tracks...");
 			int count=0,urlcount=0,notParsedCount=0;
 			HashSet<String> ids =new HashSet<String>();
 			for(int i=-1;i<countTempFiles;i++){
 				ids.addAll(getIDsFromUrlTags(i,tempFile));
 			}
 			int resultFileCounter=-1;
 			for (Iterator<String> idIter = ids.iterator(); idIter.hasNext();) {
 				String id = (String) idIter.next();
 				String trackUrlAddress= "http://www.openstreetmap.org/trace/"+id+"/data/";
 				System.out.println(trackUrlAddress);
 				URL trackUrl = new URL(trackUrlAddress);
 				try{	
 					GpxReader trackReader = new GpxReader(trackUrl.openStream());
 					trackReader.parse(false);
 					allTracks.addAll(trackReader.data.tracks);
 				}
 				catch (SAXException e) {
 					System.out.println("Could not parse!");
 					notParsedCount++;
 					//e.printStackTrace();
 				}
 				if(Runtime.getRuntime().freeMemory()/Runtime.getRuntime().maxMemory()<0.2 || !idIter.hasNext()){
 					resultFileCounter++;
 					System.out.println("Cutting..."+resultFileCounter);
 					result=cutAndCleanTracks(bounds,allTracks);
 					String realFilename=filename.replace(".gpx", resultFileCounter+".gpx");
 					GpxWriter writer = new GpxWriter(new FileOutputStream(new File(realFilename)));
 					GpxData resultData = new GpxData();
 					resultData.tracks=result;
 					writer.write(resultData);
 					System.out.println("Written to " + realFilename);
 					allTracks = new LinkedList<GpxTrack>();
 					result = new LinkedList<GpxTrack>();
 				}
 			}
 			System.out.println("Tracks with URLs:" + urlcount+"/"+count);
 			System.out.println("Tracks can't parse:" + notParsedCount+"/"+urlcount);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 		return allTracks;
 	}
 	private static List<GpxTrack> cutAndCleanTracks(Bounds bounds, Collection<GpxTrack> allTracks) {
 		while(allTracks.remove(null));
 		LinkedList<GpxTrack> result = new LinkedList<GpxTrack>(allTracks);
 		for (Iterator<GpxTrack> iterator = allTracks.iterator(); iterator.hasNext();) {
 			GpxTrack gpxTrack = (GpxTrack) iterator.next();
 			Bounds trackBounds=gpxTrack.getBounds();
 			if(trackBounds==null){
 				result.remove(gpxTrack);
 			}else if(!bounds.contains(trackBounds.getMin()) 
 					|| !bounds.contains(trackBounds.getMax())){
 				result.remove(gpxTrack);
 				Collection<Collection<WayPoint>> tempSegList = new LinkedList<Collection<WayPoint>>();
 				for (Iterator<GpxTrackSegment> iterator2 = gpxTrack.getSegments().iterator(); iterator2
 						.hasNext();) {
 					GpxTrackSegment seg = (GpxTrackSegment) iterator2.next();
 					Collection<WayPoint> tempWPList = new LinkedList<WayPoint>();
 					for (Iterator<WayPoint> iterator3 = seg.getWayPoints().iterator(); iterator3
 							.hasNext();) {
 						WayPoint wp = (WayPoint) iterator3.next();
 						if(bounds.contains(wp.getCoor())){
 							tempWPList.add(wp);
 						}else if(!tempWPList.isEmpty()){
 							tempSegList.add(tempWPList);
 							tempWPList=new LinkedList<WayPoint>();
 							result.add(new ImmutableGpxTrack(tempSegList, gpxTrack.getAttributes()));
 							tempSegList = new LinkedList<Collection<WayPoint>>();
 						}
 					}
 					if(!tempWPList.isEmpty()){
 						tempSegList.add(tempWPList);
 					}
 				}
 				if(!tempSegList.isEmpty()){
 					result.add(new ImmutableGpxTrack(tempSegList, gpxTrack.getAttributes()));
 				}
 			}
 		}
 		return result;
 		
 	}
 	private static HashSet<String> getIDsFromUrlTags(int i, String tempFile) throws IOException, SAXException {
 		HashSet<String> resultIds= new HashSet<String>();
 		//get tempfiles if tempTracks is through
 		Collection<GpxTrack> tempTracks=null;
 		if(i!=-1){
 			FileInputStream fis = new FileInputStream(new File(tempFile.replace(".gpx", i+".gpx")));
 			GpxReader tempReader = new GpxReader(fis);
 			tempReader.parse(false);
 			tempTracks = tempReader.data.tracks;
 		}
 		for(GpxTrack track :tempTracks){
 			//count++;
 			Object urlObject= track.getAttributes().get("url");
 			if (urlObject!=null){
 				//urlcount++;
 				assert(urlObject.getClass()==String.class);
 				String trackUrlAddress =(String)urlObject;
 				String id=trackUrlAddress.substring(trackUrlAddress.lastIndexOf("/")+1);
 				if(!resultIds.contains(id)){
 					resultIds.add(id);
 					System.out.println("ID from "+ id+ " from " + trackUrlAddress);
 				}
 			}
 		}
 		return resultIds;
 		
 	}
 	private static int downloadingArea(Bounds currentBound, String tempFile, Collection<GpxTrack> resultTracks, int countTempFiles) throws IOException, SAXException {
 		boolean theEnd = false;
 		for(int i =0;i==0 || !theEnd;i++){
 			String urlString= "http://api.openstreetmap.org/api/0.6/trackpoints?bbox="
 					+currentBound.getMin().getX()
 					+","
 					+currentBound.getMin().getY()
 					+","
 					+currentBound.getMax().getX()
 					+","
 					+currentBound.getMax().getY()
 					+"&page="
 					+i;
 			//System.out.println(urlString);
 			//Runnable adder=new TrackAdder(urlString,tempTracks,endIsNear);
 			//exServ.execute(adder);
 			URL url = new URL(urlString);
 			InputStream stream = url.openStream();
 			GpxReader reader = new GpxReader(stream);
 			reader.parse(false);
 			stream.close();
 			System.out.println(url+" tracks: " +reader.data.tracks.size());
 			if(reader.data.tracks.size()>0){
 				resultTracks.addAll(reader.data.tracks);
 			}else{
 				theEnd=true;;
 			}
 			double totalMemory=Runtime.getRuntime().totalMemory();
 			double freeMemory=Runtime.getRuntime().freeMemory();
 			double freeMemoryPercentage= (double)(freeMemory/totalMemory);
 			if(freeMemoryPercentage <0.2){
 				String fileName=tempFile.replace(".gpx", countTempFiles+".gpx");
 				System.out.println("Temporary file written: "+fileName);
 				FileOutputStream fos = new FileOutputStream(new File(fileName));
 				GpxWriter tempWriter = new GpxWriter(fos);
 				GpxData tempData = new GpxData();
 				tempData.tracks=resultTracks;
 				tempWriter.write(tempData);
 				fos.close();
 				countTempFiles++;
 				resultTracks = new LinkedList<GpxTrack>();
 			}
 		}
 		return countTempFiles;
 		
 	}
 	private static LinkedList<Bounds> splittingBounds(Bounds bounds, double area) {
 		LinkedList<Bounds> currentBounds = new LinkedList<Bounds>();
 		if(bounds.getArea()>area){
 			System.out.println("Splitting...");
 			double factor=Math.ceil(bounds.getArea()/area);
 			double latTileWidth=Math.abs((bounds.getMin().getY()-bounds.getMax().getY()))/factor;
 			double lonTileHeight=Math.abs((bounds.getMin().getX()-bounds.getMax().getX()))/factor;
 			for(int i=0;i<factor;i++){
 				for(int j=0;j<factor;j++){
 					Bounds currentBound =new Bounds(
 							new LatLon(bounds.getMin().getY()+latTileWidth*i
 									,bounds.getMin().getX()+lonTileHeight*j),
 									new LatLon(bounds.getMin().getY()+latTileWidth*(i+1)
 											,bounds.getMin().getX()+lonTileHeight*(j+1)));
 					currentBounds.add(currentBound);
 
 				}
 			}
 		}else{
 			currentBounds.add(bounds);
 		}
		exportBoundsAsTracks(currentBounds, "output/CuttingBounds.gpx");
 		return currentBounds;
 	}
 	public static void exportBoundsAsTracks(Collection<Bounds> bounds, String file) {
 		List<GpxTrackSegment> resultSegments = new LinkedList<GpxTrackSegment>();
 		for (Bounds bound : bounds) {
 			LatLon leftDownCorner = bound.getMin();
 			LatLon rightUpCorner = bound.getMax();
 			LatLon rightDownCorner = new LatLon(leftDownCorner.getY(),rightUpCorner.getX());
 			LatLon leftUpCorner= new LatLon(rightUpCorner.getY(),leftDownCorner.getX());
 			List<WayPoint> wps = new LinkedList<WayPoint>();
 			wps.add(new WayPoint(leftDownCorner));
 			wps.add(new WayPoint(rightDownCorner));
 			wps.add(new WayPoint(rightUpCorner));
 			wps.add(new WayPoint(leftUpCorner));
 			wps.add(new WayPoint(leftDownCorner));
 			resultSegments.add(new ImmutableGpxTrackSegment(wps));
 		}
 		exportTrackSegments(resultSegments, file);
 		
 	}
 
 }
