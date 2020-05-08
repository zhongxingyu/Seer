 /*
  * Copyright 2008 Deputacin Provincial de A Corua
  * Copyright 2009 Deputacin Provincial de Pontevedra
  * Copyright 2010 CartoLab, Universidad de A Corua
  *
  * This file is part of openCADTools, developed by the Cartography
  * Engineering Laboratory of the University of A Corua (CartoLab).
  * http://www.cartolab.es
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  */
 
 package com.iver.cit.gvsig.gui.cad.tools;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.InputEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.core.v02.FGraphicUtilities;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.gui.cad.tools.smc.CutPolygonCADToolContext;
 import com.iver.cit.gvsig.gui.cad.tools.smc.CutPolygonCADToolContext.CutPolygonCADToolState;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineSegment;
 
 /**
  * Tool to cut a polygon geometry on twice  
  *
  * @author Jose Ignacio Lamas [LBD]
  * @author Nacho Varela [Cartolab]
  * @author Pablo Sanxiao [CartoLab]
 */
 public class CutPolygonCADTool extends DefaultCADTool{
 
 	private CutPolygonCADToolContext _fsm;
 	private IGeometry selectedGeom; // [LBD] Storing the geometry which contains the first point
 	private Point2D firstPoint;
 	private Point2D secondPoint;
 
 	private ArrayList oldPoints;
 	private ArrayList newPoints;
 
 	private int firstPointIdx = -1;
 	private int secondPointIdx = -1;
 	
 // These variables are used in order to store the intersection points betwen vertex or points 
 // in the same edge
 	private boolean firstPointContentVertex = true;
 	private boolean secondPointContentVertex = true;
 	private IRowEdited selectedRow;
 
 //	Storing the part of the polygon that we keep
 	private boolean doShortPath = true;
 //	Storing the number of the multi geometry that we are modifying
 	private int multiSelected;
 
 	double PROXIMITY_THRESHOLD = 0.000001;
 
 	public void init() {
 		//super.init();
 		clear();
 		_fsm = new CutPolygonCADToolContext(this);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, double, double)
 	 */
 	public void transition(double x, double y, InputEvent event) {
 		_fsm.addPoint(x, y, event);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, double)
 	 */
 	public void transition(double d) {
 //		_fsm.addValue(d);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, java.lang.String)
 	 */
 	public void transition(String s) throws CommandException {
 		if (!super.changeCommand(s)){
 			_fsm.addOption(s);
 		}
 	}
 
 	public void transition(InputEvent event) {
 		if (newPoints!=null) {
 			_fsm.removePoint(event, newPoints.size());
 		}else{
 			_fsm.removePoint(event, 0);
 		}
 	}
 
 	public void addPoint(double x, double y, InputEvent event) {
 		CutPolygonCADToolState actualState = (CutPolygonCADToolState) _fsm.getPreviousState();
 		String status = actualState.getName();
 
 		// Check if the point inserted intersects the current geometry
 		Geometry aux = ShapeFactory.createPoint2D(x, y).toJTSGeometry();
 		if (selectedGeom == null || !selectedGeom.toJTSGeometry().intersects(aux)) {
 			return;
 		}
 		newPoints.add(new Point2D.Double(x,y));
 	}
 
 	public void addValue(double d) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addOption(String s) {
 		// TODO Auto-generated method stub
 	}
 
 	public void drawOperation(Graphics g, ArrayList pointsList) {
 
 		if (firstPoint == null || oldPoints == null) {
 			return;
 		}
 		Point2D pointAux = null;
 		int sizePixels = 12;
 		int half = sizePixels / 2;
 		if (firstPoint!=null) {
 			pointAux = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(firstPoint);
 			g.drawRect((int) (pointAux.getX() - (half-2)),
 					(int) (pointAux.getY() - (half-2)),
 					sizePixels-4, sizePixels-4);
 			g.drawRect((int) (pointAux.getX() - half),
 					(int) (pointAux.getY() - half),
 					sizePixels, sizePixels);
 			g.drawString("C1", (int)pointAux.getX(), (int)(pointAux.getY()-10));
 
 //			Painting the line so that user choose the next point
 			if (secondPoint==null) {
 				GeneralPathX gpx=new GeneralPathX();
 				for(int i = 0; i<oldPoints.size(); i++){
 					Point2D point = (Point2D) oldPoints.get(i);
 					if (i==0) {
 						gpx.moveTo(point.getX(),point.getY());
 					}else{
 						gpx.lineTo(point.getX(),point.getY());
 					}
 				}
 				IGeometry geom=ShapeFactory.createPolygon2D(gpx);
 				geom.draw((Graphics2D)g,CADExtension.getEditionManager().getMapControl().getViewPort(),DefaultCADTool.drawingSymbol);
 
 //				Painting the vertex
 				AffineTransform at = CADExtension.getEditionManager().getMapControl().getViewPort().getAffineTransform();
 				Handler[] h = geom.getHandlers(IGeometry.SELECTHANDLER);
 				FGraphicUtilities.DrawHandlers((Graphics2D) g, at,h,DefaultCADTool.drawingSymbol );
 
 			}
 		}
 		if (secondPoint!=null) {
 			pointAux = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(secondPoint);
 			g.drawRect((int) (pointAux.getX() - (half-2)),
 					(int) (pointAux.getY() - (half-2)),
 					sizePixels-4, sizePixels-4);
 			g.drawRect((int) (pointAux.getX() - half),
 					(int) (pointAux.getY() - half),
 					sizePixels, sizePixels);
 			g.drawString("C2", (int)pointAux.getX(), (int)(pointAux.getY()-10));
 
 //			Painting the part of the line that we keep
 			GeneralPathX gpx=new GeneralPathX();
 			int firstCutIndex = firstPointIdx;
 			int secondCutIndex = secondPointIdx;
 			if (firstCutIndex>secondCutIndex) {
 				int aux = firstCutIndex;
 				firstCutIndex = secondCutIndex;
 				secondCutIndex = aux;
 
 				if (((((secondCutIndex-firstCutIndex)*2)<=(oldPoints.size())) &&
 						(doShortPath))||
 						((((secondCutIndex-firstCutIndex)*2)>(oldPoints.size()))&&
 								(!doShortPath))){
 
 //					Covering first the parts from last index to second insertion point, then the introduced
 //					by the user and finally the points that go from the first point to index O.
 					for(int i = oldPoints.size()-1;i>=secondCutIndex;i--){
 						Point2D point = (Point2D) oldPoints.get(i);
 						if (i==oldPoints.size()-1) {
 							gpx.moveTo(point.getX(),point.getY());
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 //					Points digitalized by the user
 					if(newPoints!=null && newPoints.size()>0){
 						for(int i=0; i<newPoints.size();i++){
 							Point2D point = (Point2D) newPoints.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 					
 //					Snappers points
 					if(pointsList!=null){
 						for(int i=0; i<pointsList.size();i++){
 							Point2D point = (Point2D) pointsList.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 							if(i<pointsList.size()-1){
 								Point2D actual = null;
 								actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 								int sizePixelsSnapper = 10;
 								int halfSnapper = sizePixelsSnapper / 2;
 								g.drawRect((int) (actual.getX() - halfSnapper),
 										(int) (actual.getY() - halfSnapper),
 										sizePixelsSnapper, sizePixelsSnapper);
 							}
 						}
 					}
 
 //					The rest of the points of the list
 					for(int i = firstCutIndex;i>=0;i--){
 						Point2D point = (Point2D) oldPoints.get(i);
 						gpx.lineTo(point.getX(),point.getY());
 					}
 
 				}else{
 //					We have to cover first the points between the second and the first
 //					index and then the introduced by the user
 					for(int i = firstCutIndex; i<=secondCutIndex;i++){
 						Point2D point = (Point2D) oldPoints.get(i);
 						if(i==firstCutIndex){
 							gpx.moveTo(point.getX(),point.getY());
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 //					Points introduced by the user
 					if(newPoints!=null && newPoints.size()>0){
 						for(int i=0; i<newPoints.size();i++){
 							Point2D point = (Point2D) newPoints.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 
 //					Snappers points
 					if(pointsList!=null){
 						for(int i=0; i<pointsList.size();i++){
 							Point2D point = (Point2D) pointsList.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 							if(i<pointsList.size()-1){
 								Point2D actual = null;
 								actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 								int sizePixelsSnapper = 10;
 								int halfSnapper = sizePixelsSnapper / 2;
 								g.drawRect((int) (actual.getX() - halfSnapper),
 										(int) (actual.getY() - halfSnapper),
 										sizePixelsSnapper, sizePixelsSnapper);
 							}
 						}
 					}
 					gpx.closePath();
 				}
 			}else{
 				if(((((secondCutIndex-firstCutIndex)*2)<=(oldPoints.size())) &&
 						(doShortPath))||
 						((((secondCutIndex-firstCutIndex)*2)>(oldPoints.size()))&&
 								(!doShortPath))){
 
 //					We have to cover first the points between the second and the first
 //					index and then the introduced by the user
 
 //					Points of the first part of the line
 					for(int i = 0; i<=firstCutIndex; i++){
 						Point2D point = (Point2D) oldPoints.get(i);
 						if(i==0){
 							gpx.moveTo(point.getX(),point.getY());
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 //					Points digitalized by the user
 					if(newPoints!=null && newPoints.size()>0){
 						for(int i=0; i<newPoints.size();i++){
 							Point2D point = (Point2D) newPoints.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 
 //					Snapper points
 					if(pointsList!=null){
 						for(int i=0; i<pointsList.size();i++){
 							Point2D point = (Point2D) pointsList.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 							if(i<pointsList.size()-1){
 								Point2D actual = null;
 								actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 								int sizePixelsSnapper = 8;
 								int halfSnapper = sizePixelsSnapper / 2;
 								g.drawRect((int) (actual.getX() - halfSnapper),
 										(int) (actual.getY() - halfSnapper),
 										sizePixelsSnapper, sizePixelsSnapper);
 							}
 						}
 					}
 					
 					for(int i = secondCutIndex;i<oldPoints.size();i++){
 						Point2D point = (Point2D) oldPoints.get(i);
 						gpx.lineTo(point.getX(),point.getY());
 					}
 
 				}else{
 
 //					We have to cover first the points between the second and the first
 //					index and then the introduced by the user
 					
 					for(int i = secondCutIndex; i>=firstCutIndex;i--){
 						Point2D point = (Point2D) oldPoints.get(i);
 						if(i==secondCutIndex){
 							gpx.moveTo(point.getX(),point.getY());
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 
 //					Points digitalized by the user
 					if(newPoints!=null && newPoints.size()>0){
 						for(int i=0; i<newPoints.size();i++){
 							Point2D point = (Point2D) newPoints.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 
 //					Snapper points
 					if(pointsList!=null){
 						for(int i=0; i<pointsList.size();i++){
 							Point2D point = (Point2D) pointsList.get(i);
 							gpx.lineTo(point.getX(),point.getY());
 							if(i<pointsList.size()-1){
 								Point2D actual = null;
 								actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 								int sizePixelsSnapper = 10;
 								int halfSnapper = sizePixelsSnapper / 2;
 								g.drawRect((int) (actual.getX() - halfSnapper),
 										(int) (actual.getY() - halfSnapper),
 										sizePixelsSnapper, sizePixelsSnapper);
 							}
 						}
 					}
 
 					gpx.closePath();
 				}
 			}
 
 //			Checking that the points are ordered
 			if(gpx.isCCW()){
 				gpx.flip();
 			}
 
 			IGeometry geom=ShapeFactory.createPolygon2D(gpx);
 			geom.draw((Graphics2D)g,CADExtension.getEditionManager().getMapControl().getViewPort(),DefaultCADTool.drawingSymbol);
 
 
 //			Painting vertex
 			AffineTransform at = CADExtension.getEditionManager().getMapControl().getViewPort().getAffineTransform();
 			Handler[] h = geom.getHandlers(IGeometry.SELECTHANDLER);
 			FGraphicUtilities.DrawHandlers((Graphics2D) g, at, h, DefaultCADTool.drawingSymbol);
 		}
 //		Cleaning the last point of the snappers
 		cleanSnapper();
 	}
 
 
 	public String getName() {
 		return PluginServices.getText(this, "cut_polygon_");
 	}
 
 	/**
 	 * It detects if the point is inside the outline of the selected geometry at this
 	 * time and store the information related to the situation and the rest of te points
 	 * of the geometry.
 	 *
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean pointInsideFeature(double x, double y){
 
 		boolean isInside = false;		
 		Coordinate c = new Coordinate(x, y);
 
 		System.out.println("------>>>>>> PointInsideFeature("+x+", "+y + ")");
 		VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension.getEditionManager()
 		.getActiveLayerEdited();
 
 		vle.selectWithPoint(x,y,false);
 
 		ArrayList selectedRows = getSelectedRows();
 		IRowEdited row = null;
 		IGeometry geom = null;
 
 		if (selectedRows.size()>0){
 			row=(DefaultRowEdited) selectedRows.get(0);
 			//row = getCadToolAdapter().getVectorialAdapter().getRow(selection.nextSetBit(0));
 			geom = ((IFeature)row.getLinkedRow()).getGeometry();
 			selectedGeom = geom;
 			selectedRow = row;
 			firstPoint = new Point2D.Double(x,y);
 
 //			Filling the points list
 			oldPoints = new ArrayList();
 
 			PathIterator theIterator = selectedGeom.getPathIterator(null, FConverter.FLATNESS);
 			boolean terminated = false;
 			double[] theData = new double[6];
 			Coordinate from = null;
 			Coordinate first = null;
 			Coordinate to = null;    		
 			int numberMultiActual = 0;
 
 			int index = 0;
 			while (!theIterator.isDone()&&!terminated) {
 
 				int theType = theIterator.currentSegment(theData);
 
 				switch (theType) {
 				case PathIterator.SEG_MOVETO:
 					from = new Coordinate(theData[0], theData[1]);
 					first = from;
 
 					numberMultiActual++;
 //					Initializing each time in order to storing only the geometry of the multi geometry
 //					where the point is
 //					
 					if (multiSelected!=0){
 						terminated = true;
 					} else {
 						oldPoints = new ArrayList();
 						index = 0;
 						oldPoints.add(index,new Point2D.Double(theData[0], theData[1]));
 					}
 					if (c.equals(from)){
 						firstPointContentVertex = true;
 						firstPointIdx = index;
 						multiSelected = numberMultiActual;
 						isInside = true;
 					}
 					break;
 
 				case PathIterator.SEG_LINETO:
 
 					// System.out.println("SEG_LINETO");
 					to = new Coordinate(theData[0], theData[1]);
 					LineSegment line = new LineSegment(from, to);
 					Coordinate closestPoint = line.closestPoint(c);
 					double dist = c.distance(closestPoint);
 					if (c.equals(to)){
 						firstPointContentVertex = true;
 						firstPointIdx = index;
 						multiSelected = numberMultiActual;
 						isInside = true;
 					} else if ((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))) {
 						firstPointContentVertex = false;
 						firstPointIdx=index;
 						oldPoints.add(index, new Point2D.Double(x,y));
 						index++;
 						multiSelected = numberMultiActual;
 						isInside = true;
 					}
 					oldPoints.add(index,new Point2D.Double(theData[0], theData[1]));
 					from = to;
 					break;
 				case PathIterator.SEG_CLOSE:
 					line = new LineSegment(from, first);
 
 					closestPoint = line.closestPoint(c);
 					dist = c.distance(closestPoint);
 					if(c.equals(first)){
 						firstPointContentVertex = true;
 						firstPointIdx = index;
 						multiSelected = numberMultiActual;
 						isInside = true;
 					}else if((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))){
 						firstPointContentVertex = false;
 						firstPointIdx=index;
 						oldPoints.add(index, new Point2D.Double(x,y));
 						index++;
 						multiSelected = numberMultiActual;
 						isInside = true;
 					}
 					oldPoints.add(index,new Point2D.Double(first.x, first.y));
 					from = first;
 					break;
 
 				} //end switch
 				index++;
 				theIterator.next();
 			}
 			if(!isInside){
 //				The point pressed will be inside the polygon but wont be inside the line
 //				that contains the polygon so we must to reset the geometry
 				clear();
 			}
 		}
 		getCadToolAdapter().setPreviousPoint((double[])null);
 		return isInside;
 	}
 
 	/**
 	 * It detects if the point is inside the outline of the selected geometry at this
 	 * time and store the information related to the situation and the rest of te points
 	 * of the geometry. This method must be executed when the first intersection point
 	 * was established
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean secondPointInsideFeature(double x, double y){
 		System.out.println("------>>>>>> llamada a secondPointInsideFeature "+x+", "+y);
 		boolean retorno = false;
 
 		VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension.getEditionManager()
 		.getActiveLayerEdited();
 
 		vle.selectWithPoint(x,y,false);
 
 		ArrayList selectedRows=getSelectedRows();
 		IRowEdited row=null;
 		IGeometry ig=null;
 		if (selectedRows.size()>0){
 //		The point presed is inside the feature of the selected layer so we must check if is also
 //		inside of the feature that contains the first point pressed
 
 			Coordinate c = new Coordinate(x, y);
 
 			PathIterator theIterator = selectedGeom.getPathIterator(null, FConverter.FLATNESS);
 			double[] theData = new double[6];
 			Coordinate from = null, first = null;
 			boolean found= false;
 			int index = 0;
 			boolean terminatePoints = (oldPoints==null || oldPoints.size()==0);
 			if(!terminatePoints){
 				Point2D point = (Point2D) oldPoints.get(0);
 				from = new Coordinate(point.getX(), point.getY());
 			}
 			while (!terminatePoints && !found) {
 				//while not done
 				Point2D point = (Point2D) oldPoints.get(index);
 				Coordinate to = new Coordinate(point.getX(), point.getY());
 				LineSegment line = new LineSegment(from, to);
 				Coordinate closestPoint = line.closestPoint(c);
 				double dist = c.distance(closestPoint);
 				if(c.equals(to)){
 					secondPointContentVertex = true;
 					secondPointIdx = index;
 					retorno = true;
 					found=true;
 				}else if ((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))){
 					secondPointContentVertex = false;
 					secondPointIdx=index;
 					oldPoints.add(index, new Point2D.Double(x,y));
 					index++;
 					retorno = true;
 					found=true;
 				}
 				from = to;
 
 				index++;
 				if(index==oldPoints.size()){
 					terminatePoints=true;
 				}
 			}
 		}
 
 		if(retorno){
 			secondPoint = new Point2D.Double(x,y);
 			if((firstPointIdx >= secondPointIdx)&&(!secondPointContentVertex)){
 				firstPointIdx++;
 			}
 			newPoints= new ArrayList();
 			this.setMultiTransition(true);
 		}
 		getCadToolAdapter().setPreviousPoint((double[])null);
 		return retorno;
 	}
 
 	public String toString() {
 		// TODO Auto-generated method stub
 		return "_cut_polygon";
 	}
 
 	public boolean isApplicable(int shapeType) {
 		switch (shapeType) {
 		case FShape.POLYGON://GeometryTypes.POLYGON:
 		case FShape.MULTI://GeometryTypes.MULTIPOLYGON:
 			return true;
 		}
 		return false;
 	}
 
 	public void clear(){
 		selectedGeom = null;
 		firstPoint = null;
 		secondPoint = null;
 		oldPoints = null;
 		newPoints = null;
 		firstPointIdx = -1;
 		secondPointIdx = -1;
 		firstPointContentVertex = true;
 		secondPointContentVertex = true;
 		this.setMultiTransition(false);
 		getCadToolAdapter().setPreviousPoint((double[])null);
 		selectedRow=null;
 		doShortPath = true;
 		multiSelected = 0;
 //		keys.clear();
 
 	}
 
 	public void removePoint(InputEvent event) {
 		newPoints.remove(newPoints.size()-1);
 		if(newPoints.size()>0){
 			getCadToolAdapter().setPreviousPoint((Point2D)newPoints.get(newPoints.size()-1));
 		}else{
 			getCadToolAdapter().setPreviousPoint((double[])null);
 		}
 	}
 
 	public void removeSecondPoint(InputEvent event) {
 		
 		if(!secondPointContentVertex){
 //			Eliminating vector points
 			oldPoints.remove(secondPointIdx);
 		}
 		secondPointIdx = -1;
 		secondPointContentVertex = true;
 		secondPoint = null;
 		this.setMultiTransition(false);
 		getCadToolAdapter().setPreviousPoint((double[]) null);
 		
 	}
 
 	public void removeFirstPoint(InputEvent event) {
 		clear();
 	}
 
 	public void drawOperation(Graphics g, double x, double y) {
 		
 		ArrayList lista = new ArrayList();
 		lista.add(new Point2D.Double(x,y));
 		System.out.println(lista);
 		drawOperation(g,lista);
 
 	}
 
 	/**
 	 * Deleting the memory of the snappers in order to click in two points of the
 	 * same geometry and they don't follow the ones that there are between them.
 	 *
 	 */
 	public void cleanSnapper(){
 		
 		if (newPoints==null || newPoints.size()==0){
 			getCadToolAdapter().setPreviousPoint((double[])null);
 		}
 		
 	}
 
 	/**
 	 * Storing the changes made to the geometry and ask user if wants to create a new
 	 * one, if it is possible, with the rest of the geometry. Also cut a secondary geometry
 	 * if so.
 	 */
 	public void saveChanges(){
 
 		if (selectedRow!=null){
 			int resp = JOptionPane.NO_OPTION;
 			resp = JOptionPane.showConfirmDialog((Component) PluginServices.getMainFrame(),
 					PluginServices.getText(this, "keep_remaining_feature"),
					PluginServices.getText(this, "cut_polygon"), JOptionPane.YES_NO_OPTION);
 
 			System.out.println("--->>> Salving changes in cutted geometry");
 			((IFeature)selectedRow.getLinkedRow()).setGeometry(getCuttedGeometry());
 			modifyFeature(selectedRow.getIndex(),(IFeature)selectedRow.getLinkedRow());
 
 
 			if (resp == JOptionPane.YES_OPTION) {
 
 				addNewElement(getRemainingGeometry(), selectedRow);
 
 			}
 
 		} else{
 
 			//TODO Delete when we check that this branch never is called
 			System.out.println("%$%$/$/%$/   CREO QUE ESTA RAMA NUNCA ES LLAMADA   ");
 			System.out.println("%$%$/$/%$/   CREO QUE ESTA RAMA NUNCA ES LLAMADA   ");
 			System.out.println("%$%$/$/%$/   CREO QUE ESTA RAMA NUNCA ES LLAMADA   ");
 			System.out.println("%$%$/$/%$/   CREO QUE ESTA RAMA NUNCA ES LLAMADA   ");
 			System.out.println("%$%$/$/%$/   CREO QUE ESTA RAMA NUNCA ES LLAMADA   ");
 
 			int resp = JOptionPane.NO_OPTION;
 			resp = JOptionPane.showConfirmDialog((Component) PluginServices.getMainFrame(),
 					PluginServices.getText(this, "cortar_linea_restante_fuera"),
					PluginServices.getText(this, "cut_polygon"), JOptionPane.YES_NO_OPTION);
 			if (resp != JOptionPane.YES_OPTION) { // CANCEL DELETE
 
 			}else{
 //				Storing the result geometry and discard the rest
 				System.out.println("--->>> Salvando los cambios en la geometria");
 				((IFeature)selectedRow.getLinkedRow()).setGeometry(getCuttedGeometry());
 				modifyFeature(selectedRow.getIndex(),(IFeature)selectedRow.getLinkedRow());
 //				updateGeometry(entidadSeleccionada.getIndex());
 			}
 		}
 //		}
 	}
 
 
 	/**
 	 * DOCUMENT ME! Copied from NewTankCADTool
 	 *
 	 * @param geometry
 	 *            DOCUMENT ME!
 	 */
 	public void addNewElement(IGeometry geometry, IRowEdited row) {
 		VectorialLayerEdited vle= getVLE();
 		VectorialEditableAdapter vea = getVLE().getVEA();
 
 
 		int numAttr;
 		try {
 			numAttr = vea.getRecordset().getFieldCount();
 			
 			Value[] values = new Value[numAttr];
 			values = row.getAttributes();
 			
 			String newFID;
 			
 			newFID = vea.getNewFID();
 			DefaultFeature df = new DefaultFeature(geometry, values, newFID);
 			int index = vea.addRow(df, getName(), EditionEvent.GRAPHIC);
 			clearSelection();
 			ArrayList selectedRow = vle.getSelectedRow();
 			
 			ViewPort vp = vle.getLayer().getMapContext().getViewPort();
 			BufferedImage selectionImage = new BufferedImage(vp.getImageWidth(), 
 					vp.getImageHeight(),
 					BufferedImage.TYPE_INT_ARGB);
 			Graphics2D gs = selectionImage.createGraphics();
 			int inversedIndex=vea.getInversedIndex(index);
 			selectedRow.add(new DefaultRowEdited(df, IRowEdited.STATUS_ADDED, inversedIndex ));
 			vea.getSelection().set(inversedIndex);
 			IGeometry geom = df.getGeometry();
 			geom.cloneGeometry().draw(gs, vp, DefaultCADTool.selectionSymbol);
 			vle.drawHandlers(geom.cloneGeometry(), gs, vp);
 			vea.setSelectionImage(selectionImage);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e);
 		} catch (ValidateRowException e) {
 			NotificationManager.addError(e);
 		}
 
 		draw(geometry.cloneGeometry());
 	}
 
 
 
 //	/**
 //	 * Accion that is executed when we cancel the insertion form
 //	 * Deleting the last row added to the VectorialEditableAdapter
 //	 * */
 //	public void cancelInsertion(){
 //		getCadToolAdapter().delete(virtualIndex.intValue());
 //	}
 
 
 	public IGeometry getCuttedGeometry() {
 
 		return getPieceOfGeometry();
 
 	}
 
 
 
 	/**
 	 * Based on getGeometriaResultante() and getGeometriaRestante()
 	 * @return
 	 */
 	public IGeometry getPieceOfGeometry() {
 
 		// PathX to obtain the cuttedGeometry() 
 		GeneralPathX gpx=new GeneralPathX();
 		int firstCutIndex = firstPointIdx;
 		int secondCutIndex = secondPointIdx;
 
 //		TODO [NachoV] We must pay attention if there are holes. Actually does not work!!!!
 
 		PathIterator theIterator = selectedGeom.getPathIterator(null, FConverter.FLATNESS);
 		double[] theData = new double[6];
 		int numberMultiActual=0;		
 
 		while (!theIterator.isDone()) {
 			int theType = theIterator.currentSegment(theData);
 
 			switch (theType) {
 			case PathIterator.SEG_MOVETO:
 				System.out.println(numberMultiActual + ": PathIterator.SEG_MOVETO ");
 				double x = theData[0];
 				double y = theData[1];
 				numberMultiActual++;
 
 				if (multiSelected == numberMultiActual){
 					//Storing here the geometry redigitalized
 
 					boolean reversePath = (firstCutIndex>secondCutIndex);
 					if (reversePath){
 						System.out.println("Primer punto > segundo Punto");
 						int aux = firstCutIndex;
 						firstCutIndex = secondCutIndex;
 						secondCutIndex = aux;
 					}
 
 					if (((((secondCutIndex-firstCutIndex)*2) <= (oldPoints.size())) && (doShortPath))  ||
 							((((secondCutIndex - firstCutIndex)*2)  > (oldPoints.size())) && (!doShortPath))){
 
 						if (reversePath) {
 
 							// reverse = T y condition = T
 //							We have to cover first the points between the second and the first
 //							index and then the introduced by the user
 							for (int i = oldPoints.size()-1;i>=secondCutIndex;i--) {
 								Point2D point = (Point2D) oldPoints.get(i);
 								if (i==oldPoints.size()-1){
 									System.out.println("moveTo: "+ point.getX() + ", " + point.getY());
 									gpx.moveTo(point.getX(),point.getY());
 								} else{
 									System.out.println("lineTo: "+point.getX() + ", " + point.getY());
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 						} else {
 							// reverse = F y condition = T
 //							We have to cover first the points between the second and the first
 //							index and then the introduced by the user
 							for (int i = 0; i<=firstCutIndex; i++){
 								Point2D point = (Point2D) oldPoints.get(i);
 								if (i==0){
 									System.out.println("moveTo: "+point.getX() + ", " + point.getY());
 									gpx.moveTo(point.getX(),point.getY());											
 								}else{
 									System.out.println("lineTo: " +point.getX() + ", " + point.getY());
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 						}
 
 						// reverse = ANY condition = T
 
 						//Points digitalized by the user
 						if (newPoints!=null && newPoints.size()>0){
 							for (int i=0; i<newPoints.size(); i++){
 								Point2D point = (Point2D) newPoints.get(i);
 								System.out.println("lineTo: " + point.getX() + ", " + point.getY());
 								gpx.lineTo(point.getX(),point.getY());
 							}
 						}
 
 						//The rest of the points of the list
 						if (reversePath) {
 							// [NachoV] reverse = T y condition = T
 							for (int i = firstCutIndex; i>=0; i--){
 								Point2D point = (Point2D) oldPoints.get(i);
 								System.out.println("lineTo: "+ point.getX() + ", " + point.getY());
 								gpx.lineTo(point.getX(),point.getY());
 							}
 						} else {
 							// [NachoV] reverse = F y condition = T
 							for (int i = secondCutIndex;i<oldPoints.size();i++){
 								Point2D point = (Point2D) oldPoints.get(i);
 								System.out.println(point.getX() + ", " + point.getY());
 								gpx.lineTo(point.getX(),point.getY());
 							}
 						}
 
 					} else {
 						// condition = F
 						for (int i = firstCutIndex; i<=secondCutIndex; i++){
 							Point2D point = (Point2D) oldPoints.get(i);
 							if (i==firstCutIndex){
 								System.out.println("moveTo: "+ point.getX() + ", " + point.getY());
 								gpx.moveTo(point.getX(),point.getY());
 							} else {
 								System.out.println("lineTo: "+ point.getX() + ", " + point.getY());
 								gpx.lineTo(point.getX(),point.getY());
 							}
 						}
 
 						//Point digitalized by the user (the order depends on reversePath)
 						if (newPoints!=null && newPoints.size()>0){
 							if (reversePath) {
 								for(int i=0; i<newPoints.size(); i++) {
 									Point2D point = (Point2D) newPoints.get(i);
 									System.out.println("lineTo: "+ point.getX() + ", " + point.getY());
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							} else {
 								for (int i=newPoints.size()-1; i>=0; i--) {
 									Point2D point = (Point2D) newPoints.get(i);
 									System.out.println("lineTo: "+ point.getX() + ", " + point.getY());
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 						}
 						gpx.closePath();
 					}
 
 				} else {
 					// If (multiSeleccionada == numeroMultiActual) is false
 					System.out.println("moveTo: "+ x + ", " + y);
 					gpx.moveTo(x,y);
 				}
 				break;
 
 			case PathIterator.SEG_LINETO:
 				System.out.println(numberMultiActual + ": PathIterator.SEG_LINETO ");
 				x = theData[0];
 				y = theData[1];
 				if (multiSelected == numberMultiActual){
 					//Nothing to do
 				}else{
 					System.out.println("lineTo: " + x + ", " + y);
 					gpx.lineTo(x,y);
 				}
 				break;
 			case PathIterator.SEG_CLOSE:
 				System.out.println(numberMultiActual + ": PathIterator.SEG_CLOSE ");
 				if (multiSelected == numberMultiActual){
 					//Nothing to do
 				}else{
 					System.out.println("Closing polygon");
 					gpx.closePath();
 				}
 				break;
 
 			} //end switch
 			theIterator.next();
 		}
 
 //		Checking if the points are well ordered
 		if(gpx.isCCW()){
 			gpx.flip();
 		}
 
 		IGeometry geom=ShapeFactory.createPolygon2D(gpx);
 		return geom;
 	}
 
 	public IGeometry getRemainingGeometry() {
 
 		//TODO [NachoV] I don't like LBD solution for get the remaining geometry. Now it can introduce wrong geometries 
 		// (with outside points, also following geometries on the same geometry).
 		//TODO Better result will be obtain the geometry difference.		
 		doShortPath = !doShortPath;
 		return getCuttedGeometry();
 
 	}
 
 
 	/**
 	 * Alternate the part of the geometry that we keep when the cut is made
 	 */
 	public void changePieceOfGeometry(){
 		doShortPath = !doShortPath;
 //		repainting
 		CADExtension.getEditionManager().getMapControl().repaint();
 	}
 
 	public boolean isMultiTransition() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	public void setMultiTransition(boolean condicion) {
 		// TODO Auto-generated method stub		
 	}
 
 	public void setPreviousTool(DefaultCADTool tool) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
