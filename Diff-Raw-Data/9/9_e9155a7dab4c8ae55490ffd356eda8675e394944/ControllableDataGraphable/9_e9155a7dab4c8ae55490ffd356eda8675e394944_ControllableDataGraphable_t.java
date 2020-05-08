 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.23 $
 * $Date: 2007-06-01 14:20:23 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.engine;
 
 import java.awt.BasicStroke;
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.EventObject;
 import java.util.Vector;
 
 import org.concord.data.stream.DataStoreFunctionUtil;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.data.stream.WritableDataStore;
 import org.concord.graph.engine.CoordinateSystem;
 import org.concord.graph.engine.Cursorable;
 import org.concord.graph.engine.DefaultControllable;
 import org.concord.graph.engine.MouseControllable;
 import org.concord.graph.engine.MouseMotionReceiver;
 import org.concord.graph.event.CursorListener;
 import org.concord.graph.util.engine.DrawingObject;
 
 
 /**
  * ControllableDataGraphable
  * Class name and description
  *
  * Date created: Oct 27, 2004
  *
  * @author imoncada<p>
  *
  */
 public class ControllableDataGraphable extends DataGraphable
 	implements MouseControllable, MouseMotionReceiver, Cursorable, DrawingObject
 {
 	public final static int DRAGMODE_NONE = 0;
 	public final static int DRAGMODE_MOVEPOINTS = 1;
 	public final static int DRAGMODE_ADDPOINTS = 2;
 	public final static int DRAGMODE_ADDMULTIPLEPOINTS = 3;
 	public final static int DRAGMODE_REMOVEPOINTS = 4;
 	
 	protected int dragMode = DRAGMODE_NONE;
 	
 	public final static int LINETYPE_FREE = 0;
 	public final static int LINETYPE_FUNCTION = 1;
 	
 	protected int lineType = LINETYPE_FUNCTION;//LINETYPE_FREE;	
 	
 	Rectangle2D boundingBox;
 	
 	private boolean mouseClicked = false;
 	private int indexPointClicked = -1;
 	protected Point2D lastPointW; 
 	
 	protected boolean drawAlwaysConnected = true;
 	
 	//private float startDragX = Float.NaN;
 	//private float startDragY = Float.NaN;
 	private DataStoreFunctionUtil functionUtil;
 		
 	protected int drawingMode = DrawingObject.DRAWING_DRAG_MODE_NONE;
 	
 	protected boolean bNewShape;
 	
 	protected Cursor cursor;
 
 	protected Vector cursorListeners;
 	
 	protected boolean isMouseInside = false;
 	
 	/**
 	 * 
 	 */
 	public ControllableDataGraphable()
 	{
 		super();
 		functionUtil = new DataStoreFunctionUtil();
 		lastPointW = new Point2D.Double();
 		cursorListeners = new Vector();
 		bNewShape = true;
 	}
 	
 	/**
 	 * @see org.concord.datagraph.engine.DataGraphable#setDataStore(org.concord.framework.data.stream.DataStore)
 	 */
 	public void setDataStore(DataStore dataStore)
 	{
 		//This Data Graphable only makes sense with a Writable Data Store!
		if (dataStore != null && !(dataStore instanceof WritableDataStore)) {
 			throw new IllegalArgumentException("The Data Store "+dataStore+" is not Writable!");
 		}
 		
 		super.setDataStore(dataStore);
 		functionUtil.setDataStore((WritableDataStore)dataStore);
 	}
 	
 	/**
 	 * @see org.concord.datagraph.engine.DataGraphable#setChannelX(int)
 	 */
 	public void setChannelX(int channelX)
 	{
 		super.setChannelX(channelX);
 		functionUtil.setXChannel(channelX);
 	}
 	
 	/**
 	 * @see org.concord.graph.engine.MouseControllable#mousePressed(java.awt.Point)
 	 */
 	public boolean mousePressed(Point p)
 	{
 		mouseClicked = true;
 
 		if (dragMode == DRAGMODE_NONE) return false;
 		
 		if (indexPointClicked == -1 &&
 				(dragMode == DRAGMODE_MOVEPOINTS || dragMode == DRAGMODE_REMOVEPOINTS)) return false;
 		
 		if (graphArea == null) return false;
 		CoordinateSystem cs = graphArea.getCoordinateSystem();
 		
 		lastPointW = cs.transformToWorld(p);
 		
 		if (dragMode == DRAGMODE_ADDPOINTS || dragMode == DRAGMODE_ADDMULTIPLEPOINTS){
 			//Add a new point
 			if (lineType == LINETYPE_FREE){
 				if (!drawAlwaysConnected){
 					addPoint(Float.NaN, Float.NaN);
 				}
 				addPoint(lastPointW.getX(), lastPointW.getY());
 			}
 			else if (lineType == LINETYPE_FUNCTION){
 				
 				addPointOrder((float)lastPointW.getX(), (float)lastPointW.getY());
 				//startDragX = (float)pointClickedW.getX();
 				//startDragY = (float)pointClickedW.getY();
 			}
 		}
 		else if (dragMode == DRAGMODE_REMOVEPOINTS){
 			//Remove the current point
 			removeSampleAt(indexPointClicked);
 		}
 		
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.graph.engine.MouseControllable#mouseDragged(java.awt.Point)
 	 */
 	public boolean mouseDragged(Point p)
 	{
 		//System.out.println("dragged "+p);
 		double startDragX = Float.NaN;
 		
 		if (lastPointW != null){
 			startDragX = lastPointW.getX();
 		}
 		
 		if (dragMode == DRAGMODE_NONE) return false;
 		
 		if (indexPointClicked == -1 &&
 				(dragMode == DRAGMODE_MOVEPOINTS || dragMode == DRAGMODE_REMOVEPOINTS)) return false;
 		
 		if (graphArea == null) return false;
 		CoordinateSystem cs = graphArea.getCoordinateSystem();
 		
 		lastPointW = cs.transformToWorld(p);
 		
 		if (dragMode == DRAGMODE_MOVEPOINTS){
 			//Drag the current point
 			setValueAt(indexPointClicked, 0, new Float(lastPointW.getX()));
 			setValueAt(indexPointClicked, 1, new Float(lastPointW.getY()));
 		}
 		else if (dragMode == DRAGMODE_ADDMULTIPLEPOINTS){
 			//Add a new point
 			if (lineType == LINETYPE_FREE){
 				addPoint(lastPointW.getX(), lastPointW.getY());
 			}
 			else if (lineType == LINETYPE_FUNCTION){
 				
 				addPointOrderFromTo((float)lastPointW.getX(), (float)lastPointW.getY(), (float)startDragX);
 				//startDragX = (float)pW.getX();
 				//startDragY = (float)pW.getY();
 			}
 		}
 		else{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.graph.engine.MouseControllable#mouseReleased(java.awt.Point)
 	 */
 	public boolean mouseReleased(Point p)
 	{
 		if (bNewShape){
 			Cursor standard = new Cursor(Cursor.DEFAULT_CURSOR);
 			setCursor(standard);
 			isMouseInside = false;
 		}
 		bNewShape = false;
 		
 		indexPointClicked = -1;
 		mouseClicked = false;
 		lastPointW = null;
 		//startDragX = Float.NaN;
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.graph.engine.MouseControllable#isMouseControlled()
 	 */
 	public boolean isMouseControlled()
 	{
 		return mouseClicked;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.graph.engine.MouseSensitive#isPointInProximity(java.awt.Point)
 	 */
 	public boolean isPointInProximity(Point p)
 	{		
 		indexPointClicked = -1;
 		
 		if (graphArea == null) return false;
 		
 		if (dragMode == DRAGMODE_NONE) return false;
 		
 		if (dragMode == DRAGMODE_MOVEPOINTS || dragMode == DRAGMODE_REMOVEPOINTS){
 			return isPointAValue(p);
 		}
 		if (dragMode == DRAGMODE_ADDPOINTS || dragMode == DRAGMODE_ADDMULTIPLEPOINTS){
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @param p
 	 * @return
 	 */
 	protected boolean isPointAValue(Point p)
 	{
 		indexPointClicked = getIndexValueAtDisplay(p, 5);
 		if (indexPointClicked == -1){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 
 	/**
 	 * @return Returns the dragMode.
 	 */
 	public int getDragMode()
 	{
 		return dragMode;
 	}
 	
 	public Rectangle2D getBoundingRectangle(){
 		return boundingBox;
 	}
 	
 	/**
 	 * @param dragMode The dragMode to set.
 	 */
 	public void setDragMode(int dragMode)
 	{
 		if (this.dragMode != dragMode){
 			this.dragMode = dragMode;
 			notifyChange();
 		}
 	}
 	
 	/* Function stuff */
 
 	/**
 	 * @param f
 	 * @param g
 	 */
 	private void addPointOrder(float x, float y)
 	{
 		float value;
 		int i = functionUtil.findSamplePositionValue(x);
 		value = functionUtil.getXValueAt(i);
 		
 		addPointOrder(x, y, i, value);
 	}
 
 	/**
 	 * @param x
 	 * @param i
 	 * @param value
 	 */
 	private void addPointOrder(float x, float y, int i, float value)
 	{
 		//float v = functionUtil.getXValueAt(i);
 		//if (v != value){
 		//	System.err.println("values different: "+value+" "+v);
 		//}
 		if (i < getTotalNumSamples()){
 			if (value != x){
 				insertSampleAt(i);
 			}
 			for (int j=0; j < getTotalNumChannels(); j++){
 				if (j == channelX){
 					setValueAt(i, j, new Float(x));
 					//System.out.println("addPointOrder set "+getFloatVectorStr(channel));
 				}
 				else if (j == channelY){
 					setValueAt(i, j, new Float(y));
 				}
 				else{
 					setValueAt(i, j, null);
 				}				
 			}
 			//notifyDataChanged();
 		}
 		else{
 			addPoint(x,y);
 		}
 	}
 	
 	public void addPointOrderFromTo(float x, float y, float otherX)
 	{
 		if (otherX <= x){
 			addPointOrderFrom(x, y, otherX);
 		}
 		else{
 			addPointOrderTo(x, y, otherX);
 		}
 	}
 	
 	public void addPointOrderFrom(float x, float y, float startX)
 	{
 		float value;
 		int i;
 		int startI;
 		int t;
 		int ti;
 		
 		if (x < startX){
 			return;
 		}
 		if (x == startX){
 			addPointOrder(x, y);
 			return;
 		}
 		
 		i = functionUtil.findSamplePositionValue(x);
 		t = getTotalNumSamples();
 		startI = functionUtil.findSamplePositionValue(startX);
 		value = functionUtil.getXValueAt(i);
 		
 		//System.out.println("addPointOrderFrom("+x+" "+i+" "+startX+" "+startI+") in "+getFloatVectorStr((Vector)channelsValues.elementAt(0)));
 		
 		//Removing all values from startI to i
 		ti = Math.min(i, t);
 		for (int ii = startI+1; ii < ti; ii++){
 			//for (int j=0; j < channelsValues.size(); j++){
 			//	Vector channel = (Vector)channelsValues.elementAt(j);
 			//	channel.remove(startI+1);
 			//}
 			//The only problem with doing this is that removeSampleAt fires a removed event
 			removeSampleAt(startI+1);
 			i--;
 		}
 		//i = i - (ti - (startI +1));
 		
 		//System.out.println("after removing, i:"+i+" "+getFloatVectorStr((Vector)channelsValues.elementAt(0)));
 		
 		addPointOrder(x, y, i, value);
 		
 	}
 	
 	public void addPointOrderTo(float x, float y, float endX)
 	{
 		float value;
 		int i;
 		int endI;
 		int t;
 		int ti;
 		
 		if (x > endX){
 			return;
 		}
 		if (x == endX){
 			addPointOrder(x, y);
 			return;
 		}
 		
 		i = functionUtil.findSamplePositionValue(x);
 		t = getTotalNumSamples();
 		endI = functionUtil.findSamplePositionValue(endX);
 		value = functionUtil.getXValueAt(i);
 		
 		//System.out.println("addPointOrderFrom("+x+" "+i+" "+endX+" "+endI+") in "+getFloatVectorStr((Vector)channelsValues.elementAt(0)));
 		
 		//Removing all values from i to endI
 		ti = Math.min(endI, t);
 		for (int ii = i; ii < ti; ii++){
 			removeSampleAt(i);
 		}
 		
 		//System.out.println("after removing, i:"+i+" "+getFloatVectorStr((Vector)channelsValues.elementAt(0)));
 		
 		addPointOrder(x, y, i, value);
 	}
 	
 	/**
 	 * @return Returns the lineType.
 	 */
 	public int getLineType()
 	{
 		return lineType;
 	}
 	
 	/**
 	 * @param lineType The lineType to set.
 	 */
 	public void setLineType(int lineType)
 	{
 		this.lineType = lineType;
 	}
 
 	/**
 	 * @see org.concord.graph.util.engine.DrawingObject#erase(java.awt.geom.Rectangle2D)
 	 */
 	public boolean erase(Rectangle2D rectDisplay)
 	{
 		throw new UnsupportedOperationException("erase is not supported by ControllableDataGraphable yet");
 	}
 
 	/**
 	 * @see org.concord.graph.util.engine.DrawingObject#setDrawingDragMode(int)
 	 */
 	public boolean setDrawingDragMode(int mode)
 	{
 		drawingMode = mode;
 		
 		if (mode == DrawingObject.DRAWING_DRAG_MODE_NONE){
 			setDragMode(DRAGMODE_NONE);
 			return true;
 		}
 		else if (mode == DrawingObject.DRAWING_DRAG_MODE_DRAW){
 			setDragMode(DRAGMODE_ADDMULTIPLEPOINTS);
 			return true;
 		}
 		else if (mode == DrawingObject.DRAWING_DRAG_MODE_MOVE){
 			setDragMode(DRAGMODE_NONE);
 			return true;
 		}
 		
 		//return false;
 		throw new UnsupportedOperationException("setDrawingDragMode("+mode+")is not supported by ControllableDataGraphable yet");
 	}
 
 	/**
 	 * @see org.concord.graph.util.engine.DrawingObject#getDrawingDragMode()
 	 */
 	public int getDrawingDragMode()
 	{
 		return drawingMode;
 	}
 	/**
 	 * @return Returns the drawAlwaysConnected.
 	 */
 	public boolean isDrawAlwaysConnected()
 	{
 		return drawAlwaysConnected;
 	}
 	/**
 	 * @param drawAlwaysConnected The drawAlwaysConnected to set.
 	 */
 	public void setDrawAlwaysConnected(boolean drawAlwaysConnected)
 	{
 		this.drawAlwaysConnected = drawAlwaysConnected;
 	}
 
 	/**
 	 * @see org.concord.graph.util.engine.DrawingObject#isResizeEnabled()
 	 */
 	public boolean isResizeEnabled()
 	{
 		return false;
 	}
 
 	public boolean isMouseReceiving() {
 		return isMouseInside;
 	}
 
 	public boolean mouseEntered(Point p) {
 		isMouseInside = true;
 		return false;
 	}
 
 	public boolean mouseExited(Point p) {
 		isMouseInside = false;
 		return false;
 	}
 
 	public boolean mouseMoved(Point p) {
 		if (bNewShape)
 			return false;
 		if (isMouseInside){
 			Cursor move = new Cursor(Cursor.HAND_CURSOR);
 			setCursor(move);
 		} else {
 			Cursor standard = new Cursor(Cursor.DEFAULT_CURSOR);
 			setCursor(standard);
 		}
 		return false;
 	}
 
 	public boolean isInsideBox(Rectangle2D box) {
 		Stroke perimeter = new BasicStroke(1);
 		Shape outline = perimeter.createStrokedShape(path);
 		if (box.getHeight() < 1 || box.getWidth() < 1){
 			// make a slightly larger rectangle
 			double iniX = box.getMinX()-1;
 			double iniY = box.getMinY()-1;
 			double w = (box.getMaxX() - iniX) + 2;
 			double h = (box.getMaxY() - iniY) + 2;
 			return outline.intersects(iniX, iniY, w, h);
 		}
 		return outline.intersects(box);
 	}
 
 	/**
 	 * @see org.concord.graph.engine.Cursorable#setCursor(java.awt.Cursor)
 	 */
 	public void setCursor(Cursor cursor) {
 		this.cursor = cursor;
 		notifyCursorChange();
 	}
 
 	/**
 	 * 
 	 */
 	protected void notifyCursorChange() {
 		EventObject e = new EventObject(this);
 		for (int i = 0; i < cursorListeners.size(); i++) {
 			CursorListener l = (CursorListener) cursorListeners.elementAt(i);
 			l.cursorChanged(e);
 		}
 	}
 
 	/**
 	 * @see org.concord.graph.engine.Cursorable#getCursor()
 	 */
 	public Cursor getCursor() {
 		return cursor;
 	}
 
 	/**
 	 * @see org.concord.graph.engine.Cursorable#addCursorListener(org.concord.graph.event.CursorListener)
 	 */
 	public void addCursorListener(CursorListener l) {
 		if (!cursorListeners.contains(l)) {
 			cursorListeners.add(l);
 		}
 	}
 
 	/**
 	 * @see org.concord.graph.engine.Cursorable#removeCursorListener(org.concord.graph.event.CursorListener)
 	 */
 	public void removeCursorListener(CursorListener l) {
 		cursorListeners.remove(l);
 	}
 
 	public void moveInRelation(Point2D start, Point2D end) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setAllSelectedDrawingObjects(DefaultControllable[] objects) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setCurrentLocationAsOriginal() {
 		// TODO Auto-generated method stub
 		
 	}		
 }
