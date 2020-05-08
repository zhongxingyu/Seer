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
  * $Revision: 1.22 $
  * $Date: 2007-03-18 05:33:35 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.text.NumberFormat;
 
 import javax.swing.JMenuItem;
 
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataStoreEvent;
 import org.concord.framework.data.stream.DataStoreListener;
 import org.concord.graph.engine.CoordinateSystem;
 import org.concord.graph.engine.GraphableList;
 import org.concord.graph.util.ui.PointTextLabel;
 
 
 /**
  * DataPointLabel
  * Class name and description
  *
  * Date created: Mar 1, 2005
  *
  * @author imoncada<p>
  *
  */
 public class DataPointLabel extends PointTextLabel 
 	implements DataStoreListener, DataAnnotation
 {	
 	//
 	//Variables to watch the graphables that it's mousing over
 	protected GraphableList objList;
 	protected int indexPointOver = -1;
 	protected DataGraphable graphableOver = null;
 	//
 	
 	//Actual graphable that the label is linked to 
 	//(this is temporary because it should be a data point)
 	protected DataGraphable dataGraphable;
 	
 	protected float fx = Float.NaN;
 	protected float fy = Float.NaN;
 	
 	private DashedDataLine verticalDDL = new DashedDataLine(DashedDataLine.VERTICAL_LINE);
 	private DashedDataLine horizontalDDL = new DashedDataLine(DashedDataLine.HORIZONTAL_LINE);
 	
 	//Labels and Units
 	protected String xLabel = null;
 	protected String xUnits = null;
 	protected String yLabel = null;
 	protected String yUnits = null;
 	protected int xPrecision = 2;
 	protected int yPrecision = 2;
 	protected boolean precisionOverridden = false;
 	protected String pointLabel = null;	// format: (x, y)
 	protected String pointInfoLabel = null;	//format: xlabel: x unit   ylabel: y unit
 	private boolean showCoordinates = true;
 	private boolean showInfoLabel = false;
 	private boolean mouseDown;
 	
 	/**
 	 * 
 	 */
 	public DataPointLabel()
 	{
 		this("Message");
 	}
 	
 	public DataPointLabel(boolean newNote)
 	{
 		this();
 		this.newNote = newNote;
 	}
 	
 	/**
 	 * 
 	 */
 	public DataPointLabel(String msg)
 	{
 		super(msg);
 	}
 	
 	/**
 	 * @param gList The GraphableList to set.
 	 */
 	public void setGraphableList(GraphableList gList)
 	{
 		this.objList = gList;
 	}
 	
 	/**
 	 * @see org.concord.graph.util.ui.BoxTextLabel#populatePopUpMenu()
 	 */
 	@Override
     protected void populatePopUpMenu()
 	{
 		super.populatePopUpMenu();
 
 		
 		JMenuItem disconnectItem = new JMenuItem("Disconnect");
 		popUpMenu.add(disconnectItem);
 		
 		disconnectItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				setDataPoint(null);
 				setDataGraphable(null);
 			}
 		});
 		
 	}
 	
 	/**
 	 * This method is used to see whether this label should be listening
 	 * to mouse actions. In this case, we want the label to only listen to mouse
 	 * actions if the mouse is directly over a data line.
 	 */
 	@Override
     public boolean isPointInProximity(Point location)
 	{
 		if (newNote){
 			findAvailablePointOver(location);
 			return (indexPointOver > -1);
 		} else {
 			if (isSelected() && mouseDown)
 				return true;
 			
 			return super.isPointInProximity(location);
 		}
 	}
 	
 	/**
 	 * @see org.concord.graph.engine.MouseMotionReceiver#mouseMoved(java.awt.Point)
 	 */
 	@Override
     public boolean mouseMoved(Point p)
 	{
 		if (newNote){
 			findAvailablePointOver(p);
 		}
 		return super.mouseMoved(p);
 	}
 	
 	/**
 	 * 
 	 */
 	private void findAvailablePointOver(Point p)
 	{
 		if (objList != null){
 			//Look for a point in one of the graphables in the list
 			int index = -1;
 			DataGraphable dg = null;
 			for (int i=0; i<objList.size(); i++){
 				Object obj = objList.elementAt(i);
 				if (obj instanceof DataGraphable){
 					dg = (DataGraphable)obj;
 					if(dg.isVisible()) {
 						index = dg.getIndexValueAtDisplay(p, 10);
 					}
 					if (index != -1) break;
 				}
 			}
 			
 			if(index == -1) {
 				if (index != indexPointOver || graphableOver != null){
 					indexPointOver = index;
 					graphableOver = null;
 				}
 			} else {
 				//Found a point!
 				if (index != indexPointOver || dg != graphableOver){
 					indexPointOver = index;
 					graphableOver = dg;
 					foreColor = dg.getColor(); 
 				}
 			}
 			notifyChange();
 		}			
 	}
 	
 	/**
 	 * @see org.concord.graph.engine.MouseControllable#mouseDragged(java.awt.Point)
 	 */
 	@Override
     public boolean mouseDragged(Point p)
 	{
 		if (mouseInsideDataPoint || newEndPoint){
 			findAvailablePointOver(p);
 			notifyChange();
 		}
 		pointLocationWorld = graphArea.getCoordinateSystem().transformToWorld(p, pointLocationWorld);
 		
 		Point2D newP = new Point2D.Double(originalLocation.getX() + (pointLocationWorld.getX() - clickPointWorld.getX()),
 			originalLocation.getY() + (pointLocationWorld.getY() - clickPointWorld.getY()));
 		
 		Point2D dp = getGraphArea().getCoordinateSystem().transformToDisplay(newP);
 		
 		Dimension gSize = getGraphArea().getSize();
 		Dimension lSize = rectBox.getBounds().getSize();
 
 		// Block label from being dragged outside graph
 		Rectangle bounds = new Rectangle(new Point(lSize.width/2,lSize.height/2), new Dimension(gSize.width - (lSize.width), gSize.height - (lSize.height)));
 		if (!bounds.contains(dp))
 			return false;
 		
 		return super.mouseDragged(p);
 	}
 	
 	@Override
     public boolean mousePressed(Point p){
 		mouseDown = true;
 		return super.mousePressed(p);
 	}
 	
 	/**
 	 * @see org.concord.graph.engine.MouseControllable#mouseReleased(java.awt.Point)
 	 */
 	@Override
     public boolean mouseReleased(Point p)
 	{
 		mouseDown = false;
 		if (dragEnabled){
 			if (indexPointOver != -1 && graphableOver != null){
 				Point2D pW = getPointDataGraphable(graphableOver, indexPointOver);
 				setDataPoint(pW);
 			}
 			else{
 				restoreOriginalDataPoint();
 			}
 		}
 		indexPointOver = -1;
 		graphableOver = null;
 		
 		findAvailablePointOver(p);
 		
 		if (indexPointOver != -1 && graphableOver != null){
     		float f1 = Float.NaN;
     		float f2 = Float.NaN;
     
     		Point2D p2 = getPointDataGraphable(graphableOver, indexPointOver);
     		setDataPoint(p2);
     		newEndPoint = false;
 		}
 		return super.mouseReleased(p);
 	}
 	
 	/**
 	 * 
 	 */
 	private void restoreOriginalDataPoint()
 	{
 		if (dataPoint != null){
 			setDataPoint(originalDataPoint);
 		}
 	}
 
 	/**
 	 * @see org.concord.graph.util.ui.BoxTextLabel#addAtPoint(java.awt.Point)
 	 */
 	@Override
     public boolean addAtPoint(Point2D pD, Point2D pW)
 	{
 		if (indexPointOver != -1 && graphableOver != null){
 			setDataGraphable(graphableOver);
 			Point2D p = getPointDataGraphable(graphableOver, indexPointOver);
 			return super.addAtPoint(null, p);
 		}
 		else{
 			//super.addAtPoint(pD, pW);
 			setDataGraphable(null);
 			return false;
 			
 		}
 	}
 	
 	/**
 	 * @param graphableOver2
 	 * @param indexPointOver2
 	 * @return
 	 */
 	public static Point2D getPointDataGraphable(DataGraphable dg, int index)
 	{
 		Object objVal;
 		double x,y;
 		
 		objVal = dg.getValueAt(index, 0);
 
 		if (!(objVal instanceof Float)) return null;
 		x = ((Float)objVal).floatValue();
 		
 		objVal = dg.getValueAt(index, 1);
 		if (!(objVal instanceof Float)) return null;
 		y = ((Float)objVal).floatValue();
 		
 		return new Point2D.Double(x, y);
 	}
 
 	/**
 	 * @see org.concord.graph.engine.Drawable#draw(java.awt.Graphics2D)
 	 */
 	@Override
     public void draw(Graphics2D g)
 	{
 		if (newNote || mouseInsideDataPoint || newEndPoint){
 			if (indexPointOver != -1 && graphableOver != null){
 				float f1 = Float.NaN;
 				float f2 = Float.NaN;
 
 				Point2D p = getPointDataGraphable(graphableOver, indexPointOver);
 				CoordinateSystem cs = graphArea.getCoordinateSystem();
 				Point2D pD = cs.transformToDisplay(p);
 				
 				if (p != null){
 					f1 = (float)(p.getX());
 					f2 = (float)(p.getY());
 					fx = f1;
 					fy = f2;
 					
 					g.drawOval((int)pD.getX() - 7, (int)pD.getY() - 7, 13, 13);
 
 					drawDashedLines(g, fx, fy);
 					updateDataPointLabels(p);
 				}
 			}
 		}
 		if(isSelected()) {
 			g.setColor(foreColor);
 			if (getShowInfoLabel()) {
 				int pointInfoLabelLeft = graphArea.getInsets().left + 20;
 				int pointInfoLabelTop = Math.max(graphArea.getInsets().top, 20);
 				if(pointInfoLabel != null)
 					g.drawString(pointInfoLabel, pointInfoLabelLeft, pointInfoLabelTop);
 			}
 		}
 		
 		// If the graphable is null we draw ourselves no matter what
 		// if the graphable is not null then we draw ourselves only if it's visible 
 		if(dataGraphable == null || dataGraphable.isVisible()) {
 			super.draw(g);
 		}
 	}
 	
 	/**
 	 * @return Returns the dataGraphable.
 	 */
 	public DataGraphable getDataGraphable()
 	{
 		return dataGraphable;
 	}
 	
 	/**
 	 * @param dataGraphable The dataGraphable to set.
 	 */
 	public void setDataGraphable(DataGraphable dataGraphable)
 	{
 		if (this.dataGraphable == dataGraphable) return;
 		
 		if (this.dataGraphable != null){
 			this.dataGraphable.removeDataStoreListener(this);
 		}
 		this.dataGraphable = dataGraphable;
 		if (this.dataGraphable != null){
 			this.dataGraphable.addDataStoreListener(this);
 
 			int numberOfChannels = dataGraphable.getTotalNumChannels();
 			if(numberOfChannels < 2) return;
 			
 			DataChannelDescription dcd1 = dataGraphable.getDataChannelDescription(0);
 			DataChannelDescription dcd2 = dataGraphable.getDataChannelDescription(1);
 			
 			if(dcd1 == null) return;
 			if(dcd2 == null) return;
 			xLabel = dcd1.getName();
 			if(xLabel == null || xLabel.length() == 0) xLabel = "";
 			else xLabel = xLabel +": ";
 			
 			if(dcd1.getUnit() != null) xUnits = dcd1.getUnit().getDimension();
 			else if (dcd1.getName().equals("time") || dcd1.getName().equals("dt")) xUnits = "s";
 			else xUnits = "";
 
 			if (!precisionOverridden) {
 				if(dcd1.isUsePrecision()) xPrecision = Math.abs(dcd1.getPrecision()) + 1;
 				else xPrecision = 2;
 			}
 			
 			yLabel = dcd2.getName();
 			if(yLabel == null || yLabel.length() == 0) yLabel = "";
 			else yLabel = yLabel + ": ";
 			if(dcd2.getUnit() != null) yUnits = dcd2.getUnit().getDimension();
 			else yUnits = "";
 			if (!precisionOverridden) {
 				if(dcd2.isUsePrecision()) yPrecision = Math.abs(dcd2.getPrecision()) + 1;
 				else yPrecision = 2;
 			}
             
             Point2D point = getDataPoint();
             if(point != null) {
                 updateDataPointLabels();
             }
 		}
 	}
 	
 	/**
 	 * @see org.concord.graph.util.ui.BoxTextLabel#doRemove()
 	 */
 	@Override
     protected void doRemove()
 	{
 		setDataGraphable(null);
 		super.doRemove();
 	}
 	
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataAdded(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataAdded(DataStoreEvent evt)
 	{
 	}
 	
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChanged(DataStoreEvent evt)
 	{
 	}
 		
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataRemoved(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataRemoved(DataStoreEvent evt)
 	{
 		//FIXME See if the point is still in the DataGraphable?
 		//For now, I'll check if the graphable is empty
 		if(this.dataGraphable == null) return;
 		if (this.dataGraphable.getTotalNumSamples() == 0){
 			remove();
 		}
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChannelDescChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChannelDescChanged(DataStoreEvent evt)
 	{
 	}
 	
 	protected void drawDashedLines(Graphics2D g, float d1, float d2) {
 		setDashedLines(d1, d2);
 		verticalDDL.draw(g);
 		horizontalDDL.draw(g);		
 	}
 
 	protected void setDashedLines(float d1, float d2) {
 		Point2D pVO = new Point2D.Double(d1,0);
 		Point2D pD = new Point2D.Double(d1,d2);
 		Point2D pHO = new Point2D.Double(0, d2);
 		
 		verticalDDL.setDataPrecision(xPrecision);
 		horizontalDDL.setDataPrecision(yPrecision);
 
 		verticalDDL.setPoints(pVO, pD);
 		horizontalDDL.setPoints(pHO, pD);
 
 		DashedDataLine.setGraphArea(graphArea);	
 	}
 	
     protected void updateDataPointLabels(Point2D p)
     {
         float f1 = (float)p.getX();
         float f2 = (float)p.getY();
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMaximumFractionDigits(xPrecision);
         pointInfoLabel = ((xLabel== null)?"":xLabel) + nf.format(f1) + ((xUnits== null)?"":xUnits) + "        ";
         pointLabel = "(" + nf.format(f1) + ((xUnits== null)?"":xUnits) + ", ";
         nf.setMaximumFractionDigits(yPrecision);
         pointInfoLabel += ((yLabel== null)?"":yLabel) + nf.format(f2) + ((yUnits== null)?"":yUnits);
         pointLabel += nf.format(f2) + ((yUnits== null)?"":yUnits) + ")";        
     }
     
     protected void updateDataPointLabels()
     {
         Point2D p = getDataPoint();
         if (p == null){
         	pointLabel = "";
         	return;
         }
         updateDataPointLabels(p); 
     }
     
 	@Override
     public void setDataPoint(Point2D p) {
 	    if (! isReadOnly()) {
 	        super.setDataPoint(p);
 	        updateDataPointLabels();
 	    }
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
     protected void drawMessage(Graphics2D g, boolean bDraw)
 	{
 		String words[];
 		String word;
 		double xIni = displayPositionIni.getX() + 3;
 		double yIni = displayPositionIni.getY() + 12;
 		double x = xIni;
 		double y = yIni;
 		double ww, w = 0, h;
 		FontMetrics fontM;
 		
 		if (message == null) return;
 		
 		fontM = g.getFontMetrics();
 		
 		h = fontM.getHeight() - 2;
 		
 		g.setColor(foreColor);
 		g.setFont(font);
 		
 		words = message.split(" ");
 		
 		for (int i=0; i < words.length; i++){
 			word = words[i] + " ";
 			
 			//System.out.println("\""+word+"\"");
 			
 			w = fontM.stringWidth(word);
 			
 			if (x + w - xIni > maxWidth){
 				y += h;
 				x = xIni;
 			}
 
 			if (bDraw){
 				g.drawString(word, (int)x, (int)y);
 			} 
 			
 			
 			x += w;				
 		}
 
 		///////////////////////////
 		//// Draw uneditable coordinate values
 		double labelWidth = Double.NaN;
 		if(pointLabel != null && pointLabel.length() > 0 && getShowCoordinates()) {
 			y+= h;
 			Color oldColor = g.getColor();
 			Color backColor = g.getBackground();
 			Color newColor = new Color(255-backColor.getRed(), 255-backColor.getGreen(), 255-backColor.getBlue());
 			g.setColor(newColor);
 			g.drawString(pointLabel, (int)xIni, (int)y);
 			labelWidth = fontM.stringWidth(pointLabel);
 			g.setColor(oldColor);
 			msgChanged = true;
 		}
 		////
 		///////////////////////////////////////
 		
 		if (msgChanged){
 			msgChanged = false;
 
 			if(labelWidth != Double.NaN) ww = Math.max(maxWidth, labelWidth); 
 			else ww = maxWidth;
 			
 			if (y == yIni && !message.equals("")){
 				ww = x - xIni;
 			}
 			y += h;
 			Dimension d = new Dimension((int)(ww), (int)(y - yIni + 6));
 			setSize(d);
 		}
 	}
 
 	public void setShowCoordinates(boolean showCoordinates)
     {
 	    this.showCoordinates = showCoordinates;
     }
 
 	public boolean getShowCoordinates()
     {
 	    return showCoordinates;
     }
 
 	public boolean getShowInfoLabel() {
 		return showInfoLabel;
 	}
 
 	public void setShowInfoLabel(boolean showInfoLabel) {
 		this.showInfoLabel = showInfoLabel;
 	}
 
 	public void setCoordinateDecimalPlaces(int coordinateDecimalPlaces)
     {
 		this.precisionOverridden  = true;
 	    this.xPrecision = coordinateDecimalPlaces;
 	    this.yPrecision = coordinateDecimalPlaces;
 	    updateDataPointLabels();
     }	
 	
 }
