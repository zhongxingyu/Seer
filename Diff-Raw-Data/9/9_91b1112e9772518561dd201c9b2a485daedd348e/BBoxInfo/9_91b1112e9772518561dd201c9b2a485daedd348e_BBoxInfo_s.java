 package org.eclipse.stem.ui.ge.kml;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.StringTokenizer;
 
 import org.eclipse.stem.ui.ge.GELog;
 
 /**
  * Obtain the BoundingBox info for GoogleEarth 
  * from the Servlet.  
  * GoogleEarth sends the current Screen viewing area to the servlet 
  * as a BBOX parameter.  The SlideShowServlet saves this BBOX string
  * each time it is sent.  
  * 
  * This class runs as a thread and every N seconds makes a request to the
  *  BBoxServlet to access the latest BBOX string and return it.
  *  We then convert it to a bounding box rectangle and make it available 
  *  for access.  KMLDisplay will access it and use it to filter out 
  *  display of Admin areas that are not within the screen Bounding box.
  * 
  *
  */
 public class BBoxInfo implements Runnable {
     
 	private static final long SLEEPTIME = 10*1000;
 	
 	/**
 	 * id used to access the session data in servlet
 	 */
 	private String servletId = null;
 	/**
 	 * url to access the servlet
 	 */
 	private String servletUrl = null;
 	
 	/**
 	 * BoundingBox last gotten from servlet
 	 */
 	private static Rectangle bBox = null;
 	
 	private String bboxOld = null;
 	/**
 	 * constructor
 	 * @param url  URL for the servlet server
 	 * @param id Id to identify the servlet session.
 	 */
 	public BBoxInfo(String url,String id) {
 		servletId = id;
 		servletUrl = url;
 	}
 	
 	
 	/**
 	 * This thread will sit in a loop and every N seconds it 
 	 * will send a request to the SlideShowServlet to give it
 	 * the latest BBox info that was sent from GoogleEarth
 	 */
 	public void run() {
 		
 		while (true) {
 			try {
 				Rectangle bbox = readBBox();
 				if (bbox != null) 
 					setBBox(bbox);
 				Thread.sleep(SLEEPTIME);
 			} catch (InterruptedException e) {
 				
 			}
 		}
 
 	}
 	/**
 	 * readBBox 
 	 *   read the GoogleEarth BBox info from the servlet.
 	 *   GoogleEarth sends the viewport bounding box to the 
 	 *   servlet which stores it.  This will request the servelet
 	 *   to forward it to us.
 	 *   
 	 *
 	 * @return BBox Rectangle 
 	 */
 	public  Rectangle  readBBox() {
 		Rectangle bbox = null;
 		
 		try {
 			URL url = new URL(servletUrl);
 			URLConnection connect = url.openConnection();
 			connect.setUseCaches(false);
 			connect.setDoInput(true);
 			connect.setDoOutput(true);
 			connect.setRequestProperty("Content-type", "application/octet-stream");
 			
 			  // use ByteArray output
 			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
 			DataOutputStream output =
 				new DataOutputStream(byteOutput);
 			// tell it what we want and give key to info
 			output.writeUTF("BBOX "+servletId);
 			
 			output.flush();
 			byte[] buf = byteOutput.toByteArray();
 			
 			connect.setRequestProperty("Content-length", ""+buf.length);
 			DataOutputStream dataOutput =
 				new DataOutputStream(connect.getOutputStream());
 			dataOutput.write(buf);
 			dataOutput.flush();
 			dataOutput.close();
 			
 			DataInputStream in = 
 				new DataInputStream(connect.getInputStream());
 			String stringValue = in.readUTF();
 			
 			in.close();
 			
 			
 			// TODO convert string to rectangle
 			String bboxstr = stringValue.substring(5);
 			if (! bboxstr.equals(bboxOld)) {
 			   GELog.debug(this,"BBOX: "+bboxstr);
 			   // convert to Bounding box rectangle
 			   bbox = getBBox(bboxstr);
 			   GELog.debug(this,"bbox: "+bbox);
 			   bboxOld = bboxstr;
 			}
 			
 			
 		} catch (MalformedURLException e) {	
 			
 			GELog.debug(e.getMessage());
 		
 			//error ="ERROR Invalid URL "+urlstr;
 			
 		} catch (IOException e) {			
 			GELog.debug(e.getMessage());
 			
 		}
 		
 		return bbox;
 	}
 	/**
 	 * Given a BBOX string returned from GoogleEarth 
 	 * generate the BoundingBox rectangle.
 	 * GoogleEarth returns the BBOX in the following format.
 	 *  BBOX x1,y1,x2,y2
 	 *  where x1,y1 are latitude/longitude of bottom left point 
 	 *  and x2,y2 are latitude/longitude of top right point
 	 *  Values are doubles like -114.14578934
 	 *   
 	 * Note that the Rectangle coordinate system uses the origin  
 	 * (0,0) as the top left of the screen, 
 	 *  with the x and y values increasing as they move to the right 
 	 *  and down respectively.
 	 *  
 	 * @param String containing BBOX info from GE
 	 * @return Rectangle
 	 */
 	private static Rectangle getBBox(String bboxString) {
 
 		Rectangle bBox = null;
 
 		Polygon polygon = new Polygon();
 		//GELog.debug(BBoxInfo.class, "BBOX: " + bboxString);
 
 		StringTokenizer st = new StringTokenizer(bboxString, ",");
 		if (st.countTokens() == 4) {
 			// GE returns the BBox as bottom left, top right coordinates
 			// we have to translate to top left + width+height
 			double x1 = Double.parseDouble(st.nextToken());
 			double y1 = Double.parseDouble(st.nextToken());
 			double x2 = Double.parseDouble(st.nextToken());
 			double y2 = Double.parseDouble(st.nextToken());
 			double[] longitudes = { x1, x1, x2, x2, x1 };
 			double[] latitudes = { y1, y2, y2, y2, y1 };
             // ignore if entire globe
 			if (x1 == -180 && x2 == 180) {
				x1 = -180;
 				x2 = 0;
 				GELog.debug(BBoxInfo.class,"****** modify BBOX to 180,0 ******");
 			}
 				
 			
 			for (int p = 0; p < longitudes.length; p++) {
 				double x = longitudes[p];
 				double y = latitudes[p];
 				int xint = (int) ((x + 180) * 1000);
 				int yint = (int) ((y + 90) * 1000);
 
 				polygon.addPoint(xint, yint);
 			}
 		}
 	
 		
 		bBox = polygon.getBounds();
 		
 		return bBox;
 	}
 
 	/**
 	 * @return the bBox
 	 */
 	public synchronized static Rectangle getBBox() {
 		return bBox;
 	}
 
 
 	/**
 	 * @param box the bBox to set
 	 */
 	public synchronized static void setBBox(Rectangle box) {
 		bBox = box;
 	}
 	
 	/**
 	 * Test  containment of bounding box 1 in Bounding box 2
 	 * If any corner of BBox 1 is contained in BBox 2 or any corner
 	 * of BBox 2 is contained in BBox 1 then they are adjacent.
 	 * 
 	 * @param r1  bounding box 1
 	 * @param r2  bounding box 2
 	 * @return true if adjacent or containment true
 	 */
 	public static boolean testContainment (Rectangle r1, Rectangle r2) {
 		
 		// coords of corners of first bounding box
 		int xMin1 = r1.x;
 		int  xMax1 = r1.x+r1.width;
 		int  yMin1 = r1.y-r1.height;
 		int  yMax1 = r1.y;
 		// test overlap
 		if (r2.contains(xMin1,yMin1)) return true;
 		if (r2.contains(xMin1,yMax1)) return true;
 		if (r2.contains(xMax1,yMax1)) return true;
 		if (r2.contains(xMax1,yMin1)) return true;
 		
         // coords of corners of second bounding box
 		int  xMin2 = r2.x;
 		int  xMax2 = r2.x+r2.width;
 		int  yMin2 = r2.y-r2.height;
 		int  yMax2 = r2.y;
         // test overlap
 		if (r1.contains(xMin2,yMin2)) return true;
 		if (r1.contains(xMin2,yMax2)) return true;
 		if (r1.contains(xMax2,yMax2)) return true;
 		if (r1.contains(xMax2,yMin2)) return true;
 		 
 		return false;
 	}
 }
