 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.model;
 
 import java.awt.Color;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.drawing.DrawnElement;
 
 /**
  * This object represents the maps that will appear for placement of {@link Token}s.  This
  * object extends Token because the background image is a scaled asset, which
  * is exactly the definition of a Token.
  */
 public class Zone extends Token {
     
     public enum Event {
         TOKEN_ADDED,
         TOKEN_REMOVED,
         TOKEN_CHANGED,
         GRID_CHANGED,
         DRAWABLE_ADDED,
         DRAWABLE_REMOVED,
         FOG_CHANGED,
         LABEL_ADDED,
         LABEL_REMOVED,
         LABEL_CHANGED
     }
     
     public enum Layer {
     	TOKEN,
     	STAMP,
     	BACKGROUND
     }
     
     public static final int DEFAULT_FEET_PER_CELL = 5;
     
     public interface Type {
         public static final int MAP = 0;
         public static final int INFINITE = 1;
     }
     
     // The zones should be ordered.  We could have the server assign each zone
     // an incrementing number as new zones are created, but that would take a lot
     // more ellegance than we really need.  Instead, let's just keep track of the
     // time when it was created.  This should give us sufficient granularity, because
     // come on what's the likelihood of two GMs separately creating a new zone at exactly
     // the same millisecond since the epoc.
     private long creationTime = System.currentTimeMillis();
     
     private Grid grid;
     private int gridColor = Color.darkGray.getRGB();
     private float imageScaleX = 1;
     private float imageScaleY = 1;
     
     private int type;
     
     private int feetPerCell = DEFAULT_FEET_PER_CELL;
     
     private List<DrawnElement> drawables = new LinkedList<DrawnElement>();
 
     private Map<GUID, Label> labels = new LinkedHashMap<GUID, Label>();
     private Map<GUID, Token> tokenMap = new HashMap<GUID, Token>();
     private List<Token> tokenOrderedList = new LinkedList<Token>();
 
     private Area exposedArea = new Area();
     private boolean hasFog;
     
     public static final Comparator<Token> TOKEN_Z_ORDER_COMPARATOR = new Comparator<Token>() {
     	public int compare(Token o1, Token o2) {
     		int lval = o1.getZOrder();
     		int rval = o2.getZOrder();
     		
     		return lval < rval ? -1 : lval == rval ? 0 : 1;
     	}
     };
     
     public Zone() {
         // Exists for serialization purposes
     }
 
     public Zone(int type, MD5Key backgroundAsset) {
         super(backgroundAsset);
         this.type = type;
         
         setGrid(new SquareGrid());
     }
     
     public void setGrid(Grid grid) {
     	this.grid = grid;
     	grid.setZone(this);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.GRID_CHANGED));
     }
 
     public Grid getGrid() {
     	return grid;
     }
     
     public int getGridColor() {
     	return gridColor;
     }
     
     public void setGridColor(int color) {
     	gridColor = color;
     }
     
     public boolean hasFog() {
     	return hasFog;
     }
     
     public float getImageScaleX() {
         return imageScaleX;
     }
 
     public void setImageScaleX(float imageScaleX) {
         this.imageScaleX = imageScaleX;
     }
 
     public float getImageScaleY() {
         return imageScaleY;
     }
 
     public void setImageScaleY(float imageScaleY) {
         this.imageScaleY = imageScaleY;
     }
 
     public void setHasFog(boolean flag) {
     	hasFog = flag;
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
 
     public boolean isPointVisible(ZonePoint point) {
     	
     	if (!hasFog() || MapTool.getPlayer().isGM()) {
     		return true;
     	}
     	
     	return exposedArea.contains(point.x, point.y);
     }
     
     public boolean isTokenVisible(Token token) {
 
         // Base case, nothing is visible
         if (!token.isVisible()) {
             return false;
         }
         
         // Base case, everything is visible
         if (!hasFog()) {
             return true;
         }
         
         // Token is visible, and there is fog
         int x = token.getX();
         int y = token.getY();
         int w = TokenSize.getWidth(token, grid);
         int h = TokenSize.getHeight(token, grid);
 
         return getExposedArea().intersects(x, y, w, h);
     }
     
     public void clearExposedArea() {
     	exposedArea = new Area();
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
     
     public void exposeArea(Area area) {
     	exposedArea.add(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
     
     public void hideArea(Area area) {
     	exposedArea.subtract(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
 
     public long getCreationTime() {
     	return creationTime;
     }
     
     public ZonePoint getNearestVertex(ZonePoint point) {
     	
    	int gridx = (int)Math.round((point.x - grid.getOffsetX()) / (double)grid.getCellWidth());
    	int gridy = (int)Math.round((point.y - grid.getOffsetY()) / (double)grid.getCellHeight());
     	
    	// Don't ask about the +1, I don't get it either.
    	return new ZonePoint((int)(gridx * grid.getCellWidth() + grid.getOffsetX() + 1), (int)(gridy * grid.getCellHeight() + grid.getOffsetY() + 1));
     }
     
     public Area getExposedArea() {
     	return exposedArea;
     }
     
     public int getFeetPerCell() {
     	return feetPerCell;
     }
     
     public void setFeetPerCell(int feetPerCell) {
     	this.feetPerCell = feetPerCell;
     }
     
     public void setType(int type) {
         this.type = type;
     }
     
     public int getType() {
         return type;
     }
     
     public int getLargestZOrder() {
         return tokenOrderedList.size() > 0 ? tokenOrderedList.get(tokenOrderedList.size()-1).getZOrder() : 0;
     }
     
     public int getSmallestZOrder() {
         return tokenOrderedList.size() > 0 ? tokenOrderedList.get(0).getZOrder() : 0;
     }
     
     ///////////////////////////////////////////////////////////////////////////
     // labels
     ///////////////////////////////////////////////////////////////////////////
     public void putLabel(Label label) {
         
         boolean newLabel = labels.containsKey(label.getId());
         labels.put(label.getId(), label);
         
         if (newLabel) {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_ADDED, label));
         } else {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_CHANGED, label));
         }
     }
     
     public List<Label> getLabels() {
         return new ArrayList<Label>(this.labels.values());
     }
     
     public void removeLabel(GUID labelId) {
         
         Label label = labels.remove(labelId);
         if (label != null) {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_REMOVED, label));
         }
       }
     
     
     ///////////////////////////////////////////////////////////////////////////
     // drawables
     ///////////////////////////////////////////////////////////////////////////
 
     public void addDrawable(DrawnElement drawnElement) {
     	drawables.add(drawnElement);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.DRAWABLE_ADDED, drawnElement));
     }
     
     public List<DrawnElement> getDrawnElements() {
     	return drawables;
     }
     
     public void removeDrawable(GUID drawableId) {
       ListIterator<DrawnElement> i = drawables.listIterator();
       while (i.hasNext()) {
           DrawnElement drawable = i.next();
           if (drawable.getDrawable().getId().equals(drawableId)) {
             i.remove();
             
             fireModelChangeEvent(new ModelChangeEvent(this, Event.DRAWABLE_REMOVED, drawable));
             return;
           }
       }
     }
     
     ///////////////////////////////////////////////////////////////////////////
     // tokens
     ///////////////////////////////////////////////////////////////////////////
     public void putToken(Token token) {
         boolean newToken = !tokenMap.containsKey(token.getId());
 
         this.tokenMap.put(token.getId(), token);
         
         // LATER: optimize this
         tokenOrderedList.remove(token);
         tokenOrderedList.add(token);
 
         Collections.sort(tokenOrderedList, TOKEN_Z_ORDER_COMPARATOR);
 
         if (newToken) {
             
             fireModelChangeEvent(new ModelChangeEvent(this, Event.TOKEN_ADDED, token));
         } else {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.TOKEN_CHANGED, token));
         }
     }
     
     public void removeToken(GUID id) {
         Token token = this.tokenMap.remove(id);
         if (token != null) {
         	tokenOrderedList.remove(token);
             fireModelChangeEvent(new ModelChangeEvent(this, Event.DRAWABLE_REMOVED, token));
         }
     }
 	
 	public Token getToken(GUID id) {
 		return tokenMap.get(id);
 	}
 	
 	/**
 	 * Returns the first token with a given name.  The name is matched case-insensitively.
 	 */
 	public Token getTokenByName(String name) {
 		for (Token token : getAllTokens()) {
 			if (token.getName().equalsIgnoreCase(name)) {
 				return token;
 			}
 		}
 		
 		return null;
 	}
 
     public List<Token> getAllTokens() {
     	List<Token> copy = new ArrayList<Token>();
     	copy.addAll(tokenOrderedList);
         return Collections.unmodifiableList(copy);
     }
 
     // TODO: Replace the following two methods with a more generic filter mechanism
     public List<Token> getNonBackgroundTokens() {
     	List<Token> copy = new ArrayList<Token>();
     	copy.addAll(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (token.isBackground()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     public List<Token> getBackgroundTokens() {
     	List<Token> copy = new ArrayList<Token>();
     	copy.addAll(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (!token.isBackground()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
 }
