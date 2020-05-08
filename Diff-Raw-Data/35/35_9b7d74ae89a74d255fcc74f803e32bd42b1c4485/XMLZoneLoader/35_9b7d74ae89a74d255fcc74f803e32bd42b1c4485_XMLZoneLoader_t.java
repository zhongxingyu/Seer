 /*
  * XMLMapLoader.java
  *
  * Copyright (C) 2012 Matt Falkoski
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.replica.core.zoneloder;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.util.Xml;
 
 import com.replica.R;
 import com.replica.core.collision.LineSegment;
 import com.replica.core.components.RenderComponent;
 import com.replica.core.components.ScrollerComponent;
 import com.replica.core.graphics.Texture;
 import com.replica.core.graphics.TiledVertexGrid;
 import com.replica.core.graphics.VertexGrid;
 import com.replica.core.zoneloder.TileSet.Sheet;
 import com.replica.utility.DebugLog;
 import com.replica.utility.FixedSizeArray;
 import com.replica.utility.SortConstants;
 import com.replica.utility.Utils;
 
 /**
  * @author matt
  * 
  */
 public class XMLZoneLoader implements ZoneLoader {
 
 	public XMLZoneLoader(int viewWidth, int viewHeight) {
 		tileSet_ = new TileSet();
 		drawLevel_ = new int[MAX_LAYERS][2];
 		viewWidth_ = viewWidth;
 		viewHeight_ = viewHeight;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.falko.android.pinhead.MapLoader#loadMap(java.io.InputStream,
 	 * android.content.Context)
 	 */
 	public Zone loadZone(InputStream in, Context c) {
 
 		context_ = c;
 		try {
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 			parser.setInput(in, null);
 			parseXML(parser);
 		} catch (XmlPullParserException e) {
 			DebugLog.v("SnowBall", e.getMessage());
 		} catch (IOException e) {
 			DebugLog.v("SnowBall", e.getMessage());
 		} finally {
 			try {
 				in.close();
 			} catch (IOException e) {
 				// stuff happens
 			}
 		}
 
 		Zone map = buildMap();
 		assert map != null;
 		return map;
 	}
 
 	private Zone buildMap() {
 		float[][] uvWorkspace = new float[4][2];
 		
 		Zone map = new Zone();
 		
 		for (int i = 0; i < layerCount_; ++i) {
 			Sheet[] sheets = tileSet_.getSheets(mapData_[i]);
 			int gridCount = sheets.length;
 
 			VertexGrid[] grids = new VertexGrid[gridCount];
 			Texture[] textures = new Texture[gridCount];
			
			DebugLog.v("SnowBall", "Layer " + i + " sheet count: " + gridCount);
 
 			for (int j = 0; j < grids.length; ++j) {
 				grids[j] = new VertexGrid(worldWidth_, worldHeight_);
 				textures[j] = sheets[j].texture_;
 			}
			
			DebugLog.v("SnowBall", "Loaded Layer " + i);
 
 			final int tileHeight = tileSet_.getTileHeight();
 			final int tileWidth = tileSet_.getTileWidth();
 
 			for (int tileY = 0; tileY < worldHeight_; tileY++) {
 				for (int tileX = 0; tileX < worldWidth_; tileX++) {
 
 					final long GID = mapData_[i][tileY][tileX];
 					final float offsetX = tileX * tileWidth;
 					final float offsetY = tileY * tileHeight;
 
 					final float[] p0 = { offsetX, offsetY + tileHeight, 0.0f };
 					final float[] p1 = { offsetX, offsetY, 0.0f };
 					final float[] p2 = { offsetX + tileWidth, offsetY, 0.0f };
 					final float[] p3 = { offsetX + tileWidth,
 							offsetY + tileHeight, 0.0f };
 
 					final float[][] positions = { p0, p1, p2, p3 };
 					int gridIndex = tileSet_.GIDToUV(GID, uvWorkspace, sheets);
 					grids[gridIndex].set(tileX, tileY, positions, uvWorkspace);
 				}
 			}
 			
 			TiledVertexGrid layer = new TiledVertexGrid(textures, 
 														grids,
 														viewWidth_,
     													viewHeight_,
 														tileWidth, 
 														tileHeight,
 														worldWidth_,
 														worldHeight_);
 			
 			RenderComponent backgroundRender = new RenderComponent();
 			int priority = drawLevel_[i][1];
 			if (drawLevel_[i][0] == BACKGROUND)
 				priority += SortConstants.BACKGROUND_START;
 			else
 				priority += SortConstants.HIGHGROUND_START;
 	        backgroundRender.setPriority(priority);
 	        
 	        //TODO: The map format should really just output independent speeds for x and y,
 	        // but as a short term solution we can assume parallax layers lock in the smaller
 	        // direction of movement.
 	        // 4/25: this needs to allow for different scroll speeds, so far no parallax scrolling
 	        float xScrollSpeed = 1.0f;
 	        float yScrollSpeed = 1.0f;
 	        
 	        ScrollerComponent scroller = new ScrollerComponent(xScrollSpeed,
 	        													yScrollSpeed,
 	        													viewWidth_,
 	        													viewHeight_,
 	        													layer);
 	        scroller.setRenderComponent(backgroundRender);
 
 	        map.add(scroller);
 	        map.add(backgroundRender);
 	        backgroundRender.setCameraRelative(false);
 			
 		}
 
 
 		map.setWorldHeight(worldHeight_ * tileSet_.getTileHeight());
 		map.setWorldWidth(worldWidth_ * tileSet_.getTileWidth());
 		
 		map.setcollisionLines(backgroundCollisionLines_);
 
 		return map;
 	}
 
 	private void parseXML(XmlPullParser parser) throws XmlPullParserException,
 			IOException {
 
 		while (parser.next() != XmlPullParser.END_DOCUMENT) {
 			if (parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String tagName = parser.getName();
 
 			if (tagName.equals("map")) {
 
 				parseMap(parser);
 
 			} else if (tagName.equals("tileset")) {
 
 				parseTileSet(parser);
 
 			} else if (tagName.equals("layer")) {
 
 				parseLayer(parser);
 
 			} else if (tagName.equals("collision")) {
 
 				parseCollisionGroup(parser);
 
 			}
 		}
 	}
 
 	private void parseMap(XmlPullParser parser) {
 
 		String attributeName = "";
 		int ac = parser.getAttributeCount();
 		for (int i = 0; i < ac; ++i) {
 			attributeName = parser.getAttributeName(i);
 			if (attributeName.equals("MapTileWidth")) {
 				worldWidth_ = Integer.parseInt(parser.getAttributeValue(i));
 			} else if (attributeName.equals("MapTileHeight")) {
 				worldHeight_ = Integer.parseInt(parser.getAttributeValue(i));
 			} else if (attributeName.equals("TilePixilHeight")) {
 				
 				tileSet_.setTileHeight(Integer.parseInt(parser.getAttributeValue(i)));
 				
 			} else if (attributeName.equals("TilePixilWidth")) {
 				
 				tileSet_.setTileWidth(Integer.parseInt(parser.getAttributeValue(i)));
 				
 			}
 		}
 		mapData_ = new long[MAX_LAYERS][worldHeight_][worldWidth_];
 	}
 
 	private void parseTileSet(XmlPullParser parser)
 			throws XmlPullParserException, IOException {
 		int firstGID = 0;
 		int resourceID = 0;
 		int imageWidth = 0;
 		int imageHeight = 0;
 
 		String attributeName = "";
 		int ac = parser.getAttributeCount();
 		for (int i = 0; i < ac; ++i) {
 			attributeName = parser.getAttributeName(i);
 			if (attributeName.equals("name")) {
 				resourceID = Utils.getResId(parser.getAttributeValue(i),
 						context_, R.drawable.class);
 			} else if (attributeName.equals("firstGID")) {
 				firstGID = Integer.parseInt(parser.getAttributeValue(i));
 			} else if (attributeName.equals("imageHeight")) {
 				imageHeight = Integer.parseInt(parser.getAttributeValue(i));
 			} else if (attributeName.equals("imageWidth")) {
 				imageWidth = Integer.parseInt(parser.getAttributeValue(i));
 			}
 		}
 		tileSet_.addSheet(firstGID, resourceID, imageWidth, imageHeight);
 	}
 
 	private void parseLayer(XmlPullParser parser) throws IOException,
 			XmlPullParserException {
 		
 		String attributeName = "";
 		int ac = parser.getAttributeCount();
 		for (int i = 0; i < ac; ++i) {
 			attributeName = parser.getAttributeName(i);
 			if (attributeName.equals("drawLevel")) {
 				
 				drawLevel_[layerCount_][0] = parser.getAttributeValue(i)
 						.equalsIgnoreCase("background")? BACKGROUND : HIGHGROUND;
 			} else if (attributeName.equals("priority")) {
 				drawLevel_[layerCount_][1] = Integer.parseInt(parser.getAttributeValue(i));
 			}
 		}
 		
 
 		parser.nextTag();
 		if (parser.next() != XmlPullParser.TEXT) {
 			throw new IOException("No Tile Data");
 		}
 
 		String mapDataStr = parser.getText();
 		String[] mapStrArray = mapDataStr.split(",");
 
 		for (int tileY = worldHeight_ - 1; tileY >= 0; tileY--) {
 			for (int tileX = 0; tileX < worldWidth_; tileX++) {
 				final int y = worldHeight_ - tileY - 1;
 				mapData_[layerCount_][tileY][tileX] = Long
 						.parseLong(mapStrArray[y * worldWidth_ + tileX].trim());
 			}
 		}
 		layerCount_++;
 	}
 
 	private void parseCollisionGroup(XmlPullParser parser) throws XmlPullParserException, IOException {
 		
 		String lineCountAttrName = parser.getAttributeName(0);
 		if (!lineCountAttrName.equals("lineCount")) {
 			DebugLog.e("SnowBall", "Collision");
 			return;
 		}
 		
 		int lineCount = Integer.parseInt(parser.getAttributeValue(0));
 		backgroundCollisionLines_ = new FixedSizeArray<LineSegment>(lineCount);
 		String attributeName = "";
 		for (int i = 0; i < lineCount; ++i) {
 			
 			parser.nextTag();
 			
 			int startx = 0;
 			int starty = 0;
 			int endx = 0;
 			int endy = 0;
 			
 			int ac = parser.getAttributeCount();
 			for (int j = 0; j < ac; ++j) {
 				attributeName = parser.getAttributeName(j);
 				if (attributeName.equals("startX")) {
 					startx = Integer.parseInt(parser.getAttributeValue(j));
 				} else if (attributeName.equals("startY")) {
 					starty = Integer.parseInt(parser.getAttributeValue(j));
 				} else if (attributeName.equals("endX")) {
 					endx = Integer.parseInt(parser.getAttributeValue(j));
 				} else if (attributeName.equals("endY")) {
 					endy = Integer.parseInt(parser.getAttributeValue(j));
 				}
 			}
 			
 			backgroundCollisionLines_.add(new LineSegment(startx, starty, endx, endy));
 			parser.nextTag();
 		}	
 	}
 
 	private FixedSizeArray<LineSegment> backgroundCollisionLines_;
 	private static final int MAX_LAYERS = 20;
 	private static final int BACKGROUND = 0;
 	private static final int HIGHGROUND = 1;
 	private long[][][] mapData_;
 	private int[][] drawLevel_;
 	private TileSet tileSet_;
 	private Context context_;
 	private int worldWidth_;
 	private int worldHeight_;
 	private int layerCount_;
 	
 	private int viewWidth_;
 	private int viewHeight_;
 }
