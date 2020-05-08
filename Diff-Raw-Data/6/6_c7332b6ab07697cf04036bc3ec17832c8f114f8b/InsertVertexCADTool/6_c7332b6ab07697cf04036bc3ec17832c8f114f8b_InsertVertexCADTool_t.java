 /*
  * 
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
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.InputEvent;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
 import com.iver.cit.gvsig.fmap.core.FPoint2D;
 import com.iver.cit.gvsig.fmap.core.FPolygon2D;
 import com.iver.cit.gvsig.fmap.core.FPolyline2D;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.core.v02.FGraphicUtilities;
 import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.gui.cad.tools.smc.InsertVertexCADToolContext;
 import com.iver.cit.gvsig.gui.cad.tools.smc.InsertVertexCADToolContext.InsertVertexCADToolState;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Nacho Uve [Cartolab]
  */
 public class InsertVertexCADTool extends DefaultCADTool {
 	private InsertVertexCADToolContext _fsm;
 	private int numSelect=0;
 	private int numHandlers;
 	private boolean addVertex=false;
 
 	/**
 	 * Insert a new PolylineCADTool.
 	 */
 	public InsertVertexCADTool() {
 	}
 
 	/**
 	 * Mtodo de incio, para poner el cdigo de todo lo que se requiera de una
 	 * carga previa a la utilizacin de la herramienta.
 	 */
 	public void init() {
 		_fsm = new InsertVertexCADToolContext(this);
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
 		_fsm.addValue(d);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, java.lang.String)
 	 */
 	public void transition(String s) throws CommandException {
 		if (!super.changeCommand(s)){
 			_fsm.addOption(s);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	public void selection() {
 		ArrayList selectedRow=getSelectedRows();
 		if (selectedRow.size() == 0 && !CADExtension.getCADTool().getClass().getName().equals("com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool")) {
 			CADExtension.setCADTool("_selection",false);
 			((SelectionCADTool) CADExtension.getCADTool()).setNextTool(
 			"_insertVertex");
 		}
 	}
 
 	/**
 	 * Equivale al transition del prototipo pero sin pasarle como parmetro el
 	 * editableFeatureSource que ya estar creado.
 	 *
 	 * @param x parmetro x del punto que se pase en esta transicin.
 	 * @param y parmetro y del punto que se pase en esta transicin.
 	 */
 	public void addPoint(double x, double y,InputEvent event) {
 		
 		selectHandler(x,y);
 		addVertex=false;
 		
 	}
 
 	private IGeometry getSelectedGeometry() {
 		
 		ArrayList selectedRows=getSelectedRows();
 //		VectorialEditableAdapter vea = getCadToolAdapter().getVectorialAdapter();
 //		FBitSet selection = vea.getSelection();
 		IRowEdited row=null;
 		IGeometry ig=null;
 		if (selectedRows.size()==1){
 			row=(DefaultRowEdited) selectedRows.get(0);
 			//row = getCadToolAdapter().getVectorialAdapter().getRow(selection.nextSetBit(0));
 			ig=((IFeature)row.getLinkedRow()).getGeometry().cloneGeometry();
 			return ig;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Mtodo para dibujar la lo necesario para el estado en el que nos
 	 * encontremos.
 	 *
 	 * @param g Graphics sobre el que dibujar.
 	 * @param x parmetro x del punto que se pase para dibujar.
 	 * @param y parmetro x del punto que se pase para dibujar.
 	 */
 	public void drawOperation(Graphics g, double x, double y) {
 
 		try {
 			drawVertex(g,getCadToolAdapter().getMapControl().getViewPort());
 		} catch (DriverIOException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 		
 	}
 
 	/**
 	 * Add a diferent option.
 	 *
 	 * @param s Diferent option.
 	 */
 	public void addOption(String s) {
 		InsertVertexCADToolState actualState = (InsertVertexCADToolState) _fsm.getPreviousState();
 		String status = actualState.getName();
 		VectorialLayerEdited vle=getVLE();
 		VectorialEditableAdapter vea = vle.getVEA();
 		ArrayList selectedRows=vle.getSelectedRow();
 		IRowEdited row=null;
 		IGeometry ig=null;
 		Handler[] handlers=null;
 		if (selectedRows.size()==1){
 			row =  (DefaultRowEdited) selectedRows.get(0);
 			ig=((IFeature)row.getLinkedRow()).getGeometry().cloneGeometry();
 			handlers=ig.getHandlers(IGeometry.SELECTHANDLER);
 			numHandlers=handlers.length;
 			if (numHandlers ==0){
 
 					try {
 						vea.removeRow(row.getIndex(),getName(),EditionEvent.GRAPHIC);
 					} catch (ExpansionFileReadException e) {
 						  NotificationManager.addError(e.getMessage(),e);
 					} catch (ReadDriverException e) {
 						NotificationManager.addError(e.getMessage(),e);
 					}
 			}
 		}
 		
 		int dif=1;//En el caso de ser polgono.
 		if (ig instanceof FGeometryCollection){
 			dif=2;
 		}
 		
 		if (status.equals("InsertVertex.SelectVertexOrDelete")){
 			if(s.equals("s") || s.equals("S") || s.equals(PluginServices.getText(this,"next"))){
 				numSelect=numSelect-dif;
 				if (numSelect<0){
 					numSelect=numHandlers-1+(numSelect+1);
 				}
 			}else if(s.equals("a") || s.equals("A") || s.equals(PluginServices.getText(this,"previous"))){
 				numSelect=numSelect+dif;
 				if (numSelect>(numHandlers-1)){
 					numSelect=numSelect-(numHandlers);
 				}
 
 			}else if(s.equals("i") || s.equals("I") || s.equals(PluginServices.getText(this,"add"))){
 				addVertex=true;
 			}
 		}
 	}
 
 	private void drawVertex(Graphics g,ViewPort vp) throws DriverIOException{
 		
 		ArrayList selectedRows=getSelectedRows();
 		for (int i = 0; i<selectedRows.size(); i++) {
 			DefaultFeature fea = (DefaultFeature) ((DefaultRowEdited) selectedRows
 					.get(i)).getLinkedRow();
 			IGeometry ig = fea.getGeometry().cloneGeometry();
 			ig.drawInts((Graphics2D)g,vp,DefaultCADTool.drawingSymbol);
 			if (ig == null) continue;
 			Handler[] handlers=ig.getHandlers(IGeometry.SELECTHANDLER);
 			if (numSelect>=handlers.length){
 				numSelect=0;
 			}
 			//Next line makes new vertex have a cross drawing instead like the other handlers
 			//FGraphicUtilities.DrawVertex((Graphics2D)g,vp.getAffineTransform(),handlers[numSelect]);
 			FGraphicUtilities.DrawHandlers((Graphics2D)g,vp.getAffineTransform(),handlers, DefaultCADTool.drawingSymbol);
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#addvalue(double)
 	 */
 	public void addValue(double d) {
 	}
 	
 	
 //	private IGeometry removeVertex(IGeometry gp,Handler[] handlers,int numHandler) {
 //		GeneralPathX newGp = new GeneralPathX();
 //		double[] theData = new double[6];
 //
 //		PathIterator theIterator;
 //		int theType;
 //		int numParts = 0;
 //
 //		Point2D ptSrc = new Point2D.Double();
 //		boolean bFirst = false;
 //
 //		theIterator = gp.getPathIterator(null, FConverter.FLATNESS);
 //		int numSegmentsAdded = 0;
 //		while (!theIterator.isDone()) {
 //			theType = theIterator.currentSegment(theData);
 //			if (bFirst){
 //				newGp.moveTo(theData[0], theData[1]);
 //				numSegmentsAdded++;
 //				bFirst=false;
 //				theIterator.next();
 //				continue;
 //			}
 //			switch (theType) {
 //
 //			case PathIterator.SEG_MOVETO:
 //				numParts++;
 //				ptSrc.setLocation(theData[0], theData[1]);
 //				if (ptSrc.equals(handlers[numHandler].getPoint())){
 //					numParts--;
 //					bFirst=true;
 //					break;
 //				}
 //				newGp.moveTo(ptSrc.getX(), ptSrc.getY());
 //				numSegmentsAdded++;
 //				bFirst = false;
 //				break;
 //
 //			case PathIterator.SEG_LINETO:
 //				ptSrc.setLocation(theData[0], theData[1]);
 //				if (ptSrc.equals(handlers[numHandler].getPoint())){
 //					break;
 //				}
 //				newGp.lineTo(ptSrc.getX(), ptSrc.getY());
 //				bFirst = false;
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_QUADTO:
 //				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_CUBICTO:
 //				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_CLOSE:
 //				if (numSegmentsAdded < 3)
 //					newGp.lineTo(theData[0], theData[1]);
 //				newGp.closePath();
 //
 //				break;
 //			} //end switch
 //
 //			theIterator.next();
 //		} //end while loop
 //		FShape shp = null;
 //		switch (gp.getGeometryType())
 //		{
 //		case FShape.POINT: //Tipo punto
 //		case FShape.POINT + FShape.Z:
 //			shp = new FPoint2D(ptSrc.getX(), ptSrc.getY());
 //			break;
 //
 //		case FShape.LINE:
 //		case FShape.LINE + FShape.Z:
 //			shp = new FPolyline2D(newGp);
 //			break;
 //		case FShape.POLYGON:
 //		case FShape.POLYGON + FShape.Z:
 //			shp = new FPolygon2D(newGp);
 //			break;
 //		}
 //		IGeometry ig=ShapeFactory.createGeometry(shp);
 //		int dif=1;//En el caso de ser polgono.
 //		numSelect=numSelect-dif;
 //		if (numSelect<0){
 //			numSelect=numHandlers-1+(numSelect+1);
 //		}
 //		return ig;
 //	}
 //
 
 	private IGeometry addVertex(IGeometry geom,Point2D p,Rectangle2D rect) {
 		
 		IGeometry geometryCloned=geom.cloneGeometry();
 		IGeometry geom1=null;
 		GeneralPathX gpxAux;
 		boolean finish=false;
 		//FGeometry geom2=null;
 
 		//if (geometryCloned.getGeometryType() == FShape.POLYGON){
 		/////////////////
 
 		GeneralPathX newGp = new GeneralPathX();
 		double[] theData = new double[6];
 
 		PathIterator theIterator;
 		int theType;
 		int numParts = 0;
 		Point2D pLast=new Point2D.Double();
 		Point2D pAnt = new Point2D.Double();
 		Point2D firstPoint=null;
 		theIterator = geom.getPathIterator(null,FConverter.FLATNESS); //, flatness);
 		int numSegmentsAdded = 0;
 		while (!theIterator.isDone()) {
 			theType = theIterator.currentSegment(theData);
 			switch (theType) {
 			case PathIterator.SEG_MOVETO:
 				pLast.setLocation(theData[0], theData[1]);
 				if (numParts==0)
 					firstPoint=(Point2D)pLast.clone();
 				numParts++;
 
 				gpxAux=new GeneralPathX();
 				gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 				gpxAux.lineTo(pLast.getX(),pLast.getY());
 				geom1=ShapeFactory.createPolyline2D(gpxAux);
 				if (geom1.intersects(rect)){
 					finish=true;
 					newGp.moveTo(pLast.getX(), pLast.getY());
 					//newGp.lineTo(pLast.getX(),pLast.getY());
 				}else{
 					newGp.moveTo(pLast.getX(), pLast.getY());
 				}
 				pAnt.setLocation(pLast.getX(), pLast.getY());
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_LINETO:
 				pLast.setLocation(theData[0], theData[1]);
 				gpxAux=new GeneralPathX();
 				gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 				gpxAux.lineTo(pLast.getX(),pLast.getY());
 				geom1=ShapeFactory.createPolyline2D(gpxAux);
 				if (geom1.intersects(rect)){
 					newGp.lineTo(p.getX(), p.getY());
 					newGp.lineTo(pLast.getX(),pLast.getY());
 				}else{
 					newGp.lineTo(pLast.getX(), pLast.getY());
 				}
 				pAnt.setLocation(pLast.getX(), pLast.getY());
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_QUADTO:
 				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_CUBICTO:
 				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_CLOSE:
 				//if (numSegmentsAdded < 3){
 					gpxAux=new GeneralPathX();
 					gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 					gpxAux.lineTo(firstPoint.getX(),firstPoint.getY());
 					geom1=ShapeFactory.createPolyline2D(gpxAux);
 					if (geom1.intersects(rect)|| finish){
 						newGp.lineTo(p.getX(), p.getY());
//						newGp.lineTo(pLast.getX(),pLast.getY());
//					}else{
//						newGp.lineTo(pLast.getX(), pLast.getY());
 					}
 					//}
 					newGp.closePath();
 					break;
 			} //end switch
 
 			theIterator.next();
 		} //end while loop
 		FShape shp = null;
 		switch (geometryCloned.getGeometryType())
 		{
 		case FShape.POINT: //Tipo punto
 		case FShape.POINT + FShape.Z:
 			shp = new FPoint2D(pLast.getX(), pLast.getY());
 			break;
 
 		case FShape.LINE:
 		case FShape.LINE + FShape.Z:
 			shp = new FPolyline2D(newGp);
 			break;
 		case FShape.POLYGON:
 		case FShape.POLYGON + FShape.Z:
 		case FShape.CIRCLE:
 		case FShape.ELLIPSE:
 			shp = new FPolygon2D(newGp);
 			break;
 		}
 		return ShapeFactory.createGeometry(shp);
 
 	}
 	
 	private IGeometry addVertexGC(FGeometryCollection gc,Point2D p,Rectangle2D rect) {
 		IGeometry[] geoms=gc.getGeometries();
 		int pos=-1;
 		for (int i=0;i<geoms.length;i++) {
 			if (geoms[i].intersects(rect)) {
 				pos=i;
 			}
 		}
 		ArrayList newGeoms=new ArrayList();
 		for (int i=0;i<pos;i++) {
 			newGeoms.add(geoms[i]);
 		}
 		if (pos!=-1) {
 			GeneralPathX gpx1=new GeneralPathX();
 			GeneralPathX gpx2=new GeneralPathX();
 			Handler[] handlers=geoms[pos].getHandlers(IGeometry.SELECTHANDLER);
 			Point2D p1=handlers[0].getPoint();
 			Point2D p2=p;
 			Point2D p3=handlers[handlers.length-1].getPoint();
 			gpx1.moveTo(p1.getX(),p1.getY());
 			gpx1.lineTo(p2.getX(),p2.getY());
 			gpx2.moveTo(p2.getX(),p2.getY());
 			gpx2.lineTo(p3.getX(),p3.getY());
 			newGeoms.add(ShapeFactory.createPolyline2D(gpx1));
 			newGeoms.add(ShapeFactory.createPolyline2D(gpx2));
 			for (int i=pos+1;i<geoms.length;i++) {
 				newGeoms.add(geoms[i]);
 			}
 			return new FGeometryCollection((IGeometry[])newGeoms.toArray(new IGeometry[0]));
 		}else {
 			return null;
 		}
 	}
 	public String getName() {
 		return PluginServices.getText(this,"insert_vertex_");
 	}
 	
 	
 	private void selectHandler(double x, double y) {
 
 		addVertex = true;
 		
 		Point2D firstPoint = new Point2D.Double(x, y);		
 		VectorialLayerEdited vle = getVLE();
 		VectorialEditableAdapter vea=vle.getVEA();
 		ArrayList selectedRows = vle.getSelectedRow();		
 		double tam = getCadToolAdapter().getMapControl().getViewPort()
 		.toMapDistance(SelectionCADTool.tolerance);
 
 		Rectangle2D rect = new Rectangle2D.Double(firstPoint.getX() - tam,
 				firstPoint.getY() - tam, tam * 2, tam * 2);
 
 		if (selectedRows.size() < 1) {
 			return;
 		}
 
 		boolean isSelectedHandler = false;
 		IGeometry geometry = getSelectedGeometry();
 
 		if (geometry == null) {
 			return;
 		}
 
 		Handler[] handlers = geometry
 		.getHandlers(IGeometry.SELECTHANDLER);
 		for (int h = 0; h < handlers.length; h++) {
 			if (handlers[h].getPoint().distance(firstPoint) < tam) {
 				numSelect = h;
 				isSelectedHandler = true;
 			}
 		}
 
 		if (!isSelectedHandler) {
 			boolean isSelectedGeometry = false;
 			try {
 
 				//VectorialEditableAdapter vea = getCadToolAdapter()
 				//		.getVectorialAdapter();
 				//String strEPSG = getCadToolAdapter().getMapControl()
 				//		.getViewPort().getProjection().getAbrev()
 				//		.substring(5);
 				//IRowEdited[] feats = vea.getFeatures(rect, strEPSG);
 
 				//for (int i = 0; i < feats.length; i++) {
 				if (geometry.intersects(rect)) { // , 0.1)){
 					isSelectedGeometry = true;
 				}
 				//}
 				if (isSelectedGeometry && addVertex) {
 					selectedRows = getSelectedRows();
 					DefaultFeature fea = null;
 					DefaultRowEdited row = null;
 					row = (DefaultRowEdited) selectedRows.get(0);
 					fea = (DefaultFeature) row.getLinkedRow();
 					Point2D posVertex = new Point2D.Double(x, y);
 					IGeometry geom1=fea.getGeometry().cloneGeometry();
 					IGeometry geom=null;
 					if (geom1 instanceof FGeometryCollection) {
 						geom = addVertexGC((FGeometryCollection)geom1, posVertex, rect);
 					}else {
 						geom = addVertex(geom1, posVertex, rect);
 					}
 					if (geom!=null) {
 						DefaultFeature df = new DefaultFeature(geom, fea
 								.getAttributes(),row.getID());
 							vea.modifyRow(row.getIndex(), df,
 									PluginServices.getText(this,"add_vertex"),EditionEvent.GRAPHIC);
 
 						Handler[] newHandlers = geom
 						.getHandlers(IGeometry.SELECTHANDLER);
 						for (int h = 0; h < newHandlers.length; h++) {
 							if (newHandlers[h].getPoint().distance(
 									posVertex) < tam) {
 								numSelect = h;
 								isSelectedHandler = true;
 							}
 						}
 
 						clearSelection();
 						selectedRows.add(new DefaultRowEdited(df,
 								IRowEdited.STATUS_MODIFIED, row.getIndex()));
 					}
 				}
 
 				} catch (ExpansionFileWriteException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ExpansionFileReadException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ValidateRowException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ReadDriverException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				}
 		}
 
 	}
 
 	public String toString() {
 		return "_deleteVertex";
 	}
 
 	public boolean isApplicable(int shapeType) {
 		switch (shapeType) {
 		case FShape.POINT:
 		case FShape.MULTIPOINT:
 			return false;
 		}
 		return true;
 	}
 
 	public void drawOperation(Graphics g, ArrayList pointList) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean isMultiTransition() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void setMultiTransition(boolean condicion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setPreviousTool(DefaultCADTool tool) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void transition(InputEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
