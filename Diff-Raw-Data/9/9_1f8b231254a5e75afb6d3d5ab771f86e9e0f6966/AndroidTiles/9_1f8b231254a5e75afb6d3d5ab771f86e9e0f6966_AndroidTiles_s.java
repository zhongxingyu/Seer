 package com.example.androidtiles;
 
 import java.util.*;
 
 import java.io.*;
 import java.net.*;
 
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.SimpleGraph;
 import org.jgrapht.traverse.DepthFirstIterator;
 import org.jgrapht.*;
 import org.jgrapht.generate.*;
 import org.jgrapht.graph.*;
 import org.jgrapht.traverse.*;
 
 import android.view.inputmethod.InputMethodManager;
 import android.content.Context;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.util.Log;
 import processing.core.*;
 import processing.data.XML;
 
 @SuppressWarnings("unused")
 public class AndroidTiles extends PApplet {
 	int MODE;
 	int CAPTURING = 0;
 	int SCREENSHOT = 1;
 	boolean EMAILSCREEN = false;
 	
 	float acceptable_distance = 4;
 	boolean debug_snapshot_done = false;
 	PImage sample;
 	
 	XML xml;
 	XML output_template;
 
 	//array for all the data from tileset.xml
 	Tile[] tileset;
 	
 	//array for all the tiles that are in the captured image
 	Tile[] present_tiles;
 	
 	//JGraphT stuff
 	SimpleGraph<Tile, DefaultEdge> tileGraph;
 	
 	//misc.
 	int lastID;
 
 	Scanner scanner; // top code scanner
 	List<TopCode> codes = new ArrayList<TopCode>();
 	int[] pixels = null;
 	
 	// Set up camera globals:
 	CameraSurfaceView gCamSurfView;
 	// This is the physical image drawn on the screen representing the camera:
 	PImage gBuffer;
 	
 	public void setup() {
 		size(width, height);
 		frameRate(10);
 		
 		hideVirtualKeyboard();
 	  
 		imageMode(CENTER);
 		stroke(255);
 		textSize(24); // (3)
 
 		scanner = new Scanner();
 		
 lastID = 0;
 		
 		// xml stuff
 		xml = loadXML("tileset.xml");
 		XML[] children = xml.getChildren("tile");
 		
 		tileset = new Tile[children.length];
 		present_tiles = new Tile[0];
 				
 		for (int i = 0; i < children.length; i++) {
 			String topcodeID;
 			String topcodeName;
 			int[] connections = new int[4];
 
 			int id = children[i].getInt("topcodeID");
 			String name = children[i].getChild("name").getContent();
 			String type = children[i].getString("type");
 
 			// connections are in order: TRBL
 			connections[0] = children[i].getChild("connections").getInt("T");
 			connections[1] = children[i].getChild("connections").getInt("R");
 			connections[2] = children[i].getChild("connections").getInt("B");
 			connections[3] = children[i].getChild("connections").getInt("L");
 
 			//println(id + " " + name + "  { L" + connections[3] + "; R"
 			//		+ connections[1] + "; T" + connections[0] + "; B"
 			//		+ connections[2] + " }");
 			// pass all data to appropriate tile object
 			tileset[i] = new Tile (id, name, type, connections[0], connections[1], connections[2], connections[3]);
 		}
 		for (int i = 0; i < tileset.length; i ++) {
 			Log.v ("Msg", tileset[i].topcodeID + " " + tileset[i].topcodeName + "  { L" + tileset[i].connections[3] + "; R"
 					+ tileset[i].connections[1] + "; T" + tileset[i].connections[0] + "; B"
 					+ tileset[i].connections[2] + " }"); //print every available tile that is defined in tileset.xml
 		}
 		
 		// JGraphT stuff
 		tileGraph = new SimpleGraph<Tile, DefaultEdge>(DefaultEdge.class);
 	}
 	
 	public void draw() {
 		if (EMAILSCREEN == true)  {
 			// draw text box
 			fill (255);
 			stroke(0);
 			rectMode (CENTER);
 			rect(width/2, height/10, width/2, 50);
 			// draw email text
 			fill(0);
 			text(typing, width/4 + 20, height/10 + 7);
 			fill(255);
 		}
 	}
 	
 	public List<TopCode> getTopcodes(PImage cam) {
 		// we need a copy of the pixels array
 		// because the scanner modifies the image it is given
 		/*if (pixels == null) pixels = new int[gBuffer.pixels.length];
 		System.arraycopy(gBuffer.pixels, 0, pixels, 0, gBuffer.pixels.length);
 
 		codes = scanner.scan(pixels, gBuffer.width, gBuffer.height);*/
 		
 		//image(cam, width / 2, height / 2, width, height);
 		
 		// we need a copy of the pixels array
 		// because the scanner modifies the image it is given
 		if (pixels == null) pixels = new int[cam.pixels.length];
 		System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);
 
 		codes = scanner.scan(pixels, cam.width, cam.height);
 		
 		// draw the codes (if any)
 		rectMode(CENTER);
 		stroke(0, 255, 0);
 		noFill();
 		/*for (TopCode code : codes) {
 			pushMatrix();
 			translate(code.getCenterX(), code.getCenterY());
 			text(code.getCode(), 0, 0);
 			rotate(code.getOrientation());
 			rect(0, 0, code.getDiameter(), code.getDiameter());
 			popMatrix();
 			println(code.getCode());
 		}*/
 
 		return codes;
 	}
 	
 	public void snapShot(PImage img) {
 		//normal behaviour: live video with topcodes being identified
 		//image(cam, 0, 0);
 		rectMode(CENTER);
 		
 		codes = getTopcodes(gBuffer);
 	    
 		image(gBuffer, width/2,height/2); // draw cam image
 
 			//println("present tiles:");
 		for (TopCode code : codes) {
 			String tileName = "";
 
 			int thisCode = code.getCode();
 			for (int i = 0; i < tileset.length; i++) {
 				if (thisCode == tileset[i].topcodeID) {
 					tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
 					println("    " + tileName);
 				}
 				// drawing all the topcode boundaries and names
 				pushMatrix();
 				stroke(0, 255, 0);
 				noFill();
 				translate(code.getCenterX(), code.getCenterY());
 				text(tileName, 0, 0);
 				rotate(code.getOrientation());
 				rect(0, 0, code.getDiameter(), code.getDiameter());
 				popMatrix();
 
 			}
 		}
 	}
 	public void makeGraph(PImage img) {
 		// remove all vertices from graph (reverting it to an empty state)
 		for (int i = 0; i < present_tiles.length; i ++) {
 			tileGraph.removeVertex(present_tiles[i]);
 		}
 		
 		// get all the topcodes from the snapshot
 		List<TopCode> codes = getTopcodes(img);
 		
 		lastID =  0; // reset lastID
 
 		present_tiles = new Tile[0]; // reset array to empty
 		present_tiles = new Tile[codes.size()]; // reset array with length of the number of topcodes there are
 		//println(present_tiles.length);
 		
 		// add all the topcodes to the present_tiles array
 		for (TopCode code : codes) {
 			int thisCode = code.getCode();
 			
 			//println (code.getCode() + " " + code.getCenterX() + ", " + code.getCenterY());
 			for (int i = 0; i < tileset.length; i++) {
 				if (thisCode == tileset[i].topcodeID) { // if we've found a thing that matches
 					// make a new present_tile object that has all the info of the tile, plus info about the topcode
 					Tile newTile = new Tile(tileset[i].topcodeID, tileset[i].topcodeName, tileset[i].type, tileset[i].connections[0], tileset[i].connections[1], tileset[i].connections[2], tileset[i].connections[3]);
 					// give the tile a unique ID
 					newTile.id = lastID;
 					// give the tile all the properties of the topcode
 					newTile.centerX = code.getCenterX();
 					newTile.centerY = code.getCenterY();
 					newTile.diameter = code.getDiameter();
 					// add tile to the present_tiles array
 					present_tiles[lastID] = newTile;
 					//println (present_tiles[lastID].toString());
 					lastID ++;
 					
 					//println(present_tiles[0].toString());
 				} 
 			}
 		}
 		
 		for (Tile tiles : present_tiles) {
 			// add all present_tiles as vertices to the graph
 			tileGraph.addVertex(tiles);
 			// print out all the tiles
 			Log.v ("Msg", "Present-tile: " + tiles.toString());
 		}
 		
 		// get distances between tiles
 		for (Tile tile1 : present_tiles) {
 			for (int tile1c : tile1.connections) { //cycle through every connection on tile1
 				float shortest_distance = acceptable_distance*tile1.diameter; // for determining the closest tile
 				Tile short_tile = null;
 				for (Tile tile2 : present_tiles) {
 					float distance = dist (tile1.centerX, tile1.centerY, 
 					tile2.centerX, tile2.centerY);
 				
 					if (distance > 0 && distance < tile1.diameter*acceptable_distance) { //they are close enough
 						for (int tile2c : tile2.connections) { //cycle through tile2 connections
 							if (tile1c == tile2c && tile1c != 0) {
 								if (distance < shortest_distance) {
 									shortest_distance = distance;
 									short_tile = tile2;
 								}
 							}
 						}
 					}
 				}
 				if (short_tile != null) {							
 					//make edge between the two things (that are the closest)
 					tileGraph.addEdge(tile1, short_tile); 
 					//edge visualization
 					line(tile1.centerX,tile1.centerY,short_tile.centerX,short_tile.centerY);
 				}
 			}
 		}
 		
 		emailScreen(); //get the user's email via a prompt
 		
 		/*try {
 			//sendToServer(getOutput()); 
 			Log.v("Xml",  (getOutput())); 
 		} catch (Exception e) {
 			//do nothing
 			e.printStackTrace();
 		}*/
 	}
 	
 		// for keyboard email entry
 		String typing = "";
 		String saved = "";
 	public void emailScreen() {
 		EMAILSCREEN = true;
 		showVirtualKeyboard();
 	}
 	
 	public void keyPressed() {
 		if (key == '\n'){ // on enter, print full string to console. this is where
 			saved = typing;
 			typing = "";
 			// call some output function
 			Log.v("Msg", "email: " + saved);
 			hideVirtualKeyboard();
 			EMAILSCREEN = false;
 
 			typing = "an email was sent";
 			MODE = CAPTURING;
 
 			try {
 				sendToServer(getOutput(saved)); 
 			} catch (Exception e) {
 				//do nothing
 				e.printStackTrace();
 			}
 		} else if (key != CODED) {
 			typing = typing + key; // add key to the end of the in=progress string
 		}
 		//Log.v("Msg", "key: " + key);
 		//Log.v("Msg",  typing);
 	}
 	
 	// keyboard functions from http://forum.processing.org/topic/show-hide-the-keyboard
 	void showVirtualKeyboard() {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
 	}
 
 	void hideVirtualKeyboard() {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 	}
 	public void sendToServer(String message) throws Exception { // from http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
 		
 		String stringToSend = URLEncoder.encode(message, "UTF-8");
 
         //URL url = new URL("http://imagearts.ryerson.ca/kathryn.hartog/tilesProcessor.php");
 		URL url = new URL("http://www.deadpixel.ca/tiles/template.cgi");
         URLConnection connection = url.openConnection();
         connection.setDoOutput(true);
 
         OutputStreamWriter out = new OutputStreamWriter(
                                          connection.getOutputStream());
         out.write("message=" + stringToSend);
         out.close();
 
         BufferedReader in = new BufferedReader(
                                     new InputStreamReader(
                                     connection.getInputStream()));
         String decodedString;
         while ((decodedString = in.readLine()) != null) {
             //System.out.println(decodedString);
         	Log.v("Xml", decodedString);
         }
         in.close();
 	}
 
 	public String getOutput(String email) {
 		println("output:");		
 		// reload the output template every time		
 		output_template = loadXML("outputTemplate.xml");
 		// get email node
 			XML emailNode = output_template.getChild("email");
 			emailNode.setContent(email);
 		// adding all NODES
 			XML nodes = output_template.getChild("nodes");
 		for (Tile tiles : present_tiles) {
 			// add all present_tiles as children to output_template in <nodes>
 			XML newNode = nodes.addChild("node"); //new node
 			//populating the node
 			XML newID = newNode.addChild("id");
 			newID.setContent(str(tiles.id));
 			XML newTileName = newNode.addChild("tileName");
 			newTileName.setContent(tiles.topcodeName);
 			XML newTopcodeID = newNode.addChild("topcodeID");
 			newTopcodeID.setContent(str(tiles.topcodeID));
 			XML newType = newNode.addChild("type");
 			newType.setContent(tiles.type);
 		}
 		
 		//making PATHS
 			XML paths = output_template.getChild("paths");
 		for (Tile present : present_tiles)	{
 			// println(present.type);
 			
 			if (present.type.equals("hub")){ //check if tile is Arduino or Processing (hubs)
 				println(present.toString() + " is a hub");
 
 				Set<DefaultEdge> allEdges = tileGraph.edgesOf(present); 				
 				int numEdges = allEdges.size(); 
 				println(numEdges + " edges connected to this tile"); //number of edges connected to hub
 				if (numEdges == 1) { // only hubs at the very end
 					
 					XML newPath = paths.addChild("path");
 					//iterate through graph from the hub
 					Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph, present);
 			        Tile vertex;
 			        while (iter.hasNext()) {
 			            vertex = iter.next();
 				            println("    " + vertex.toString()); // print node
 							XML newNode = newPath.addChild("node"); // add node to output_template
 							populateNode(vertex, newNode);
 						
 			            if (vertex != present && vertex.type.equals("hub")) { //ends the path if another hub is encountered
 			            	allEdges = tileGraph.edgesOf(vertex); 				
 							numEdges = allEdges.size(); 
 							println(numEdges + " edges connected to this tile"); //number of edges connected to hub
 							if (numEdges > 1) {
 			            	
 				            	//new path with the last hub to start with
 								newPath = paths.addChild("path");
 								//add middle hub again, in this new path
 					            println("    " + vertex.toString()); // print node
 								newNode = newPath.addChild("node"); // add node to output_template
 								populateNode(vertex, newNode);
 							}
 			            }
 			        }
 				}
 			}
 		}		
 		String output = output_template.format(4);
 		//println(output_template); // prints xml file
 		
 		return output;
 	}
 	
 	public void populateNode(Tile tiles, XML newNode) {
 		//populating the node
 		XML newID = newNode.addChild("id");
 		newID.setContent(str(tiles.id));
 		XML newTileName = newNode.addChild("tileName");
 		newTileName.setContent(tiles.topcodeName);
 		XML newTopcodeID = newNode.addChild("topcodeID");
 		newTopcodeID.setContent(str(tiles.topcodeID));
 		XML newType = newNode.addChild("type");
 		newType.setContent(tiles.type);
 	}
 	
 	protected void onResume() {
 		super.onResume();
 		Log.v("Msg", "onResume()!");
 		orientation(LANDSCAPE);
 		gCamSurfView = new CameraSurfaceView(this.getApplicationContext());
 	}
 	
 	public void mousePressed() {
 		Log.v("Msg", "Touch");
 		if (EMAILSCREEN == false) {
 			if (MODE == CAPTURING) {
 				MODE = SCREENSHOT;
 			    snapShot(gBuffer); // make one SINGLE snapshot
 				makeGraph(gBuffer);
 			} else if (MODE == SCREENSHOT) {
 				MODE = CAPTURING;
 				//do stuff related to capturing
 			}
 		} else {
 			if (mouseY < height/2) {
 				typing = "";
 			}
 		}
 	}
 
 	// camera stuff is from http://www.akeric.com/blog/?p=1342
 	/**
 	CameraPixelData
 	Eric Pavey - 2010-11-15
 
 	Set Sketch Permissions : CAMERA
 	Add to AndroidManifest.xml:
 	    uses-feature android:name="android.hardware.camera"
 	    uses-feature android:name="android.hardware.camera.autofocus"
 	*/
 	public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
 		  SurfaceHolder mHolder;
 		  Camera cam = null;
 		  Camera.Size prevSize;
 		
 		  // SurfaceView Constructor:  : ---------------------------------------------------
 		  CameraSurfaceView(Context context) {
 		    super(context);
 		    // Processing PApplets come with their own SurfaceView object which can be accessed
 		    // directly via its object name, 'surfaceView', or via the below function:
 		    // mHolder = surfaceView.getHolder();
 		    mHolder = getSurfaceHolder();
 		    // Add this object as a callback:
 		    mHolder.addCallback(this);
 		  }
 		
 		  // SurfaceHolder.Callback stuff: ------------------------------------------------------
 		  public void surfaceCreated (SurfaceHolder holder) {
 		    // When the SurfaceHolder is created, create our camera, and register our
 		    // camera's preview callback, which will fire on each frame of preview:
 		    cam = Camera.open();
 		    cam.setPreviewCallback(this);
 		
 		    Camera.Parameters parameters = cam.getParameters();
 		    parameters.setPreviewSize(displayWidth, displayHeight);
 		    cam.setParameters(parameters);
 		    // Find our preview size, and init our global PImage:
 		    prevSize = parameters.getPreviewSize();
 		    gBuffer = createImage(prevSize.width, prevSize.height, RGB);
 		  }  
 		
 		  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		    // Start our camera previewing:
 		    cam.startPreview();
 		  }
 		
 		  public void surfaceDestroyed (SurfaceHolder holder) {
 		    // Give the cam back to the phone:
 		    cam.stopPreview();
 		    cam.release();
 		    cam = null;
 		    hideVirtualKeyboard();
 		  }
 		
 		  //  Camera.PreviewCallback stuff: ------------------------------------------------------
 		  public void onPreviewFrame(byte[] data, Camera cam) {
 			  if (MODE == CAPTURING) {
 			    // This is called every frame of the preview.  Update our global PImage.
 			    gBuffer.loadPixels();
 			    // Decode our camera byte data into RGB data:
 			    decodeYUV420SP(gBuffer.pixels, data, prevSize.width, prevSize.height);
 			    gBuffer.updatePixels();
 			    // Draw to screen:
 			    //image(gBuffer, width/2,height/2); // draw cam image
 			  snapShot(gBuffer);
 			  } 
 		  }
 		
 		  //  Byte decoder : ---------------------------------------------------------------------
 		  void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
 		    // Pulled directly from:
 		    // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
 		    final int frameSize = width * height;
 		
 		    for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
 		      for (int i = 0; i < width; i++, yp++) {
 		        int y = (0xff & ((int) yuv420sp[yp])) - 16;
 		        if (y < 0)
 		          y = 0;
 		        if ((i & 1) == 0) {
 		          v = (0xff & yuv420sp[uvp++]) - 128;
 		          u = (0xff & yuv420sp[uvp++]) - 128;
 		        }
 		
 		        int y1192 = 1192 * y;
 		        int r = (y1192 + 1634 * v);
 		        int g = (y1192 - 833 * v - 400 * u);
 		        int b = (y1192 + 2066 * u);
 		
 		        if (r < 0)
 		           r = 0;
 		        else if (r > 262143)
 		           r = 262143;
 		        if (g < 0)
 		           g = 0;
 		        else if (g > 262143)
 		           g = 262143;
 		        if (b < 0)
 		           b = 0;
 		        else if (b > 262143)
 		           b = 262143;
 		
 		        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
 		      }
 		    }
 		  }
 		}
 }
