 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able
  * to manipulate and create vector and raster spatial information. OrbisGIS
  * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
  *    Erwan BOCHER, scientific researcher,
  *    Thomas LEDUC, scientific researcher,
  *    Fernando GONZALEZ CORTES, computer engineer.
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OrbisGIS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult:
  *    <http://orbisgis.cerma.archi.fr/>
  *    <http://sourcesup.cru.fr/projects/orbisgis/>
  *
  * or contact directly:
  *    erwan.bocher _at_ ec-nantes.fr
  *    fergonco _at_ gmail.com
  *    thomas.leduc _at_ cerma.archi.fr
  */
 package org.orbisgis.core.ui.editors.map.tools;
 
 import java.awt.Graphics;
 import java.awt.geom.Point2D;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import javax.swing.AbstractButton;
 
 import org.gdms.data.SpatialDataSourceDecorator;
 import org.gdms.driver.DriverException;
import org.orbisgis.core.images.IconLoader;
import org.orbisgis.core.images.IconNames;
 import org.orbisgis.core.layerModel.ILayer;
 import org.orbisgis.core.layerModel.MapContext;
 import org.orbisgis.core.ui.editors.map.tool.CannotChangeGeometryException;
 import org.orbisgis.core.ui.editors.map.tool.DrawingException;
 import org.orbisgis.core.ui.editors.map.tool.FinishedAutomatonException;
 import org.orbisgis.core.ui.editors.map.tool.Handler;
 import org.orbisgis.core.ui.editors.map.tool.ToolManager;
 import org.orbisgis.core.ui.editors.map.tool.TransitionException;
 import org.orbisgis.core.ui.editors.map.tools.generated.VertexDeletion;
 import org.orbisgis.core.ui.pluginSystem.PlugInContext;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 public class VertexDeletionTool extends VertexDeletion {
 
 	AbstractButton button;
 
 	@Override
 	public AbstractButton getButton() {
 		return button;
 	}
 
 	public void setButton(AbstractButton button) {
 		this.button = button;
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		PlugInContext.checkTool(this);
 	}
 
 	@Override
 	public void transitionTo_Standby(MapContext vc, ToolManager tm)
 			throws FinishedAutomatonException, TransitionException {
 
 	}
 
 	@Override
 	public void transitionTo_Done(MapContext mc, ToolManager tm)
 			throws FinishedAutomatonException, TransitionException {
 		Point2D p = tm.getLastRealMousePosition();
 		ArrayList<Handler> handlers = tm.getCurrentHandlers();
 		ILayer activeLayer = mc.getActiveLayer();
 		SpatialDataSourceDecorator sds = activeLayer.getDataSource();
 
 		for (Handler handler : handlers) {
 			if (p.distance(handler.getPoint()) < tm.getTolerance()) {
 				try {
 					if (p.distance(handler.getPoint()) < tm.getTolerance()) {
 						sds.setGeometry(handler.getGeometryIndex(), handler
 								.remove());
 						break;
 					}
 				} catch (CannotChangeGeometryException e) {
 					throw new TransitionException(e.getMessage());
 				} catch (DriverException e) {
 					throw new TransitionException(e.getMessage());
 				}
 			}
 		}
 		transition("init"); //$NON-NLS-1$
 	}
 
 	@Override
 	public void transitionTo_Cancel(MapContext vc, ToolManager tm)
 			throws FinishedAutomatonException, TransitionException {
 
 	}
 
 	@Override
 	public void drawIn_Standby(Graphics g, MapContext vc, ToolManager tm)
 			throws DrawingException {
 		Point2D p = tm.getLastRealMousePosition();
 		ArrayList<Handler> handlers = tm.getCurrentHandlers();
 
 		for (Handler handler : handlers) {
 			try {
 				if (p.distance(handler.getPoint()) < tm.getTolerance()) {
 					Geometry geom = handler.remove();
 					if (geom != null) {
 						tm.addGeomToDraw(geom);
 						break;
 					}
 				}
 			} catch (CannotChangeGeometryException e) {
 				throw new DrawingException(Messages
 						.getString("VertexDeletionTool.1")); //$NON-NLS-1$
 			}
 		}
 	}
 
 	@Override
 	public void drawIn_Done(Graphics g, MapContext vc, ToolManager tm)
 			throws DrawingException {
 
 	}
 
 	@Override
 	public void drawIn_Cancel(Graphics g, MapContext vc, ToolManager tm)
 			throws DrawingException {
 
 	}
 
 	public boolean isEnabled(MapContext vc, ToolManager tm) {
 		return ToolUtilities.activeSelectionGreaterThan(vc, 0)
 				&& ToolUtilities.isActiveLayerEditable(vc);
 	}
 
 	public boolean isVisible(MapContext vc, ToolManager tm) {
 		return isEnabled(vc, tm);
 	}
 
 	public String getName() {
 		return "Remove a vertex";
 	}
 }
