 /**
  * RasterGrid.java
  * ----------------------------------------------------------------------------------
  * 
  * Copyright (C) 2008 www.integratedmodelling.org
  * Created: Jan 17, 2008
  *
  * ----------------------------------------------------------------------------------
  * This file is part of ThinklabGeospacePlugin.
  * 
  * ThinklabGeospacePlugin is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ThinklabGeospacePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with the software; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  * 
  * ----------------------------------------------------------------------------------
  * 
  * @copyright 2008 www.integratedmodelling.org
  * @author    Ferdinando Villa (fvilla@uvm.edu)
  * @date      Jan 17, 2008
  * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
  * @link      http://www.integratedmodelling.org
  **/
 package org.integratedmodelling.geospace.implementations.observations;
 
 import java.util.Hashtable;
import java.util.Properties;
 
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.integratedmodelling.corescience.CoreScience;
 import org.integratedmodelling.corescience.implementations.observations.Observation;
 import org.integratedmodelling.corescience.interfaces.cmodel.IConceptualModel;
 import org.integratedmodelling.geospace.Geospace;
 import org.integratedmodelling.geospace.implementations.cmodels.RegularRasterModel;
 import org.integratedmodelling.geospace.implementations.cmodels.SubdividedCoverageConceptualModel;
 import org.integratedmodelling.geospace.literals.ShapeValue;
 import org.integratedmodelling.thinklab.exception.ThinklabException;
 import org.integratedmodelling.thinklab.exception.ThinklabInternalErrorException;
 import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
 import org.integratedmodelling.thinklab.interfaces.annotations.InstanceImplementation;
 import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
 import org.integratedmodelling.thinklab.interfaces.knowledge.IRelationship;
 import org.integratedmodelling.utils.MalformedListException;
 import org.integratedmodelling.utils.Pair;
 import org.integratedmodelling.utils.Polylist;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /**
  * An observation class that represents a gridded view of space, perfect to serve
  * as the spatial extent observation of another observation. Will create all its
  * conceptual model etc. from the OWL specs, so it's typically all you need to
  * define to provide a raster spatial context to an observation.
  * 
  * @author Ferdinando Villa
  */
 @InstanceImplementation(concept="geospace:RasterGrid")
 public class RasterGrid extends Observation {
 
 	int xRO, xRM, yRO, yRM;
 	double latLB, lonLB, latUB, lonUB;
 	CoordinateReferenceSystem crs;
 	
 	@Override
 	public String toString() {
 		return ("raster-grid("  + getRows() + " x " + getColumns() +")");
 	}
 	
 	public void initialize(IInstance i) throws ThinklabException {
 
 		/*
 		 * link the observable - do it now, so that super.initialize() finds it.
 		 * NOTE: if we subclass the observable to a grid-specific one, no 
 		 * rasterization of vector coverages will take place.
 		 */
 		i.addObjectRelationship(
 					CoreScience.HAS_OBSERVABLE, 
 //					Geospace.get().absoluteRasterGridInstance());
 					Geospace.get().absoluteSpatialCoverageInstance());
 		
 		String crsId = null;
 				
 		// read requested parameters from properties
 		for (IRelationship r : i.getRelationships()) {
 			
 			/* for speed */
 			if (r.isLiteral()) {
 				
 				if (r.getProperty().equals(Geospace.X_RANGE_OFFSET)) {
 					xRO = r.getValue().asNumber().asInteger();
 				} else if (r.getProperty().equals(Geospace.X_RANGE_MAX)) {
 					xRM = r.getValue().asNumber().asInteger();
 				} else if (r.getProperty().equals(Geospace.Y_RANGE_OFFSET)) {
 					yRO = r.getValue().asNumber().asInteger();
 				} else if (r.getProperty().equals(Geospace.Y_RANGE_MAX)) {
 					yRM = r.getValue().asNumber().asInteger();
 				} else if (r.getProperty().equals(Geospace.LAT_LOWER_BOUND)) {
 					latLB = r.getValue().asNumber().asDouble();
 				} else if (r.getProperty().equals(Geospace.LON_LOWER_BOUND)) {
 					lonLB = r.getValue().asNumber().asDouble();
 				} else if (r.getProperty().equals(Geospace.LAT_UPPER_BOUND)) {
 					latUB = r.getValue().asNumber().asDouble();
 				} else if (r.getProperty().equals(Geospace.LON_UPPER_BOUND)) {
 					lonUB = r.getValue().asNumber().asDouble();
 				} else if (r.getProperty().equals(Geospace.CRS_CODE)) {
 					crsId = r.getValue().toString();
 				} 			
 			}
 		}
 
 		if (crsId != null)
 			crs = SubdividedCoverageConceptualModel.getCRSFromID(crsId);
 		
 		super.initialize(i);
 
 	}
 	
 	@Override
 	public IConceptualModel createMissingConceptualModel()
 			throws ThinklabException {
 		
 		return new RegularRasterModel(
 				xRO, xRM, yRO, yRM, latLB, latUB, lonLB, lonUB, crs);
 	}
 	
 	
 	public int getRows() {
 		
 		try {
 			return ((RegularRasterModel)getConceptualModel()).getRows();
 		} catch (ThinklabException e) {
 			throw new ThinklabRuntimeException(e);
 		}
 	}
 
 	
 	public int getColumns() {
 		
 		try {
 			return ((RegularRasterModel)getConceptualModel()).getColumns();
 		} catch (ThinklabException e) {
 			throw new ThinklabRuntimeException(e);
 		}
 	}
 
 	/**
 	 * Determine the width and height (in cells) of the bounding box for the passed
 	 * shape when we want the max linear resolution to be the passed one and the
 	 * cells square.
 	 * 
 	 * @param shape
 	 * @param maxLinearResolution
 	 * @return
 	 */
 	public static Pair<Integer, Integer> getRasterBoxDimensions(ShapeValue shape, int maxLinearResolution) {
 		
 		ReferencedEnvelope env = shape.getEnvelope();
 		
 		int x = 0, y = 0;
 		if (env.getWidth() > env.getHeight()) {
 			x = maxLinearResolution;
 			y = (int)(maxLinearResolution * (env.getHeight()/env.getWidth()));
 		} else {
 			y = maxLinearResolution;
 			x = (int)(maxLinearResolution * (env.getWidth()/env.getHeight()));			
 		}
 		
 		return new Pair<Integer, Integer>(x,y);
 	}
 	
 	/**
 	 * Create the rastergrid definition that will define the envelope of the passed
 	 * shape, with the passed max resolution as the resolution of the longest
 	 * dimension and the other dimension defined in order to keep the cells square.
 	 * Maximum raster res will be <= maxLinearResolution^2.
 	 * @param shape
 	 * @param maxLinearResolution
 	 * @return
 	 * @throws ThinklabException
 	 */
 	public static Polylist createRasterGrid(ShapeValue shape, int maxLinearResolution) throws ThinklabException {
 
 		Polylist ret = null;
 		
 		/*
 		 * Create the list representation of a RasterGrid object and substitute the 
 		 * envelope values in it.
 		 */
 		String grid = 
 				"(geospace:RasterGrid" + 
 				"	(geospace:hasXRangeOffset $xRangeOffset)" + 
 				"	(geospace:hasXRangeMax $xRangeMax)" + 
 				"	(geospace:hasYRangeOffset $yRangeOffset)" + 
 				"	(geospace:hasYRangeMax $yRangeMax)" + 
 				"	(geospace:hasCoordinateReferenceSystem $crsCode)" + 
 				"	(geospace:hasLatLowerBound $latLowerBound)" + 
 				"	(geospace:hasLonLowerBound $lonLowerBound)" + 
 				"	(geospace:hasLatUpperBound $latUpperBound)" + 
 				"	(geospace:hasLonUpperBound $lonUpperBound))";
 		
 		Hashtable<String, Object> sym = new Hashtable<String, Object>();
 
 		/*
 		 * calculate aspect ratio and define resolution from it
 		 */
 		Pair<Integer, Integer> xy = getRasterBoxDimensions(shape, maxLinearResolution);
 		ReferencedEnvelope env = shape.getEnvelope();
 
 		sym.put("xRangeOffset", 0);
 		sym.put("xRangeMax", xy.getFirst());
 		sym.put("yRangeOffset", 0);
 		sym.put("yRangeMax", xy.getSecond());
 		sym.put("crsCode", Geospace.getCRSIdentifier(shape.getCRS(), true));
 		sym.put("latLowerBound", env.getMinY());
 		sym.put("lonLowerBound", env.getMinX());
 		sym.put("latUpperBound", env.getMaxY());
 		sym.put("lonUpperBound", env.getMaxX());
 		
 		try {
 			ret = Polylist.parseWithTemplate(grid, sym);
 		} catch (MalformedListException e) {
 			throw new ThinklabInternalErrorException(e);
 		}
 		
 		return ret;
 	}
 }
