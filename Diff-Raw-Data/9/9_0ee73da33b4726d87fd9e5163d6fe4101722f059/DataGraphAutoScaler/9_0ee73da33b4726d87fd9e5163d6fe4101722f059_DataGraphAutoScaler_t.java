 
 
 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01741
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
  */
 /*
  * Last modification information:
 * $Revision: 1.7 $
 * $Date: 2004-11-15 19:50:08 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.engine;
 
 
 
 /**
  * AutoScaleGraphable
  * Class name and description
  *
  * Date created: Oct 21, 2004
  *
  * @author imoncada<p>
  *
  */
 public class DataGraphAutoScaler extends DataGraphDaemon
 {
 	protected boolean autoScaleX = true;
 	protected boolean autoScaleY = false;
 	
 	/**
 	 * 
 	 */
 	public void handleUpdate()
 	{
 		DataGraphable dg;
 		float minX=0, maxX=0, minY=0, maxY=0;
 		float val;
 		
 		if (!autoScaleX && !autoScaleY) return;
 		
 		for (int i=0; i<graphables.size(); i++){
 			Object obj = graphables.elementAt(i);
 			if (obj instanceof DataGraphable){
 				dg = (DataGraphable)obj;
 				if(dg.getTotalNumSamples() < 1) {
 					continue;
 				}
 				
 				minX = Math.min(dg.getMinXValue(), minX);
 				maxX = Math.max(dg.getMaxXValue(), maxX);
 				minY = Math.min(dg.getMinYValue(), minY);
 				maxY = Math.max(dg.getMaxYValue(), maxY);
 			}
 		}
 		if (!Float.isNaN(minX) && !Float.isNaN(maxX) && !Float.isNaN(minY) && !Float.isNaN(maxY)){
 			
 			//System.out.println(minX + " " + maxX +  " " + minY +  " " + maxY);
 			
 			if (autoScaleX && autoScaleY){			
				graph.setLimitsAxisWorld(minX, maxX, minY, maxY);
 			}
 			else if (autoScaleX){			
 				graph.setLimitsAxisWorld(minX, maxX, graph.getMinYAxisWorld(), graph.getMaxYAxisWorld());
 			}
 			else if (autoScaleY){			
 				graph.setLimitsAxisWorld(graph.getMinXAxisWorld(), graph.getMaxXAxisWorld(), minY, maxY);
 			}
 		}
 	}
 	
 	
 	/**
 	 * @param autoScaleX The autoScaleX to set.
 	 */
 	public void setAutoScaleX(boolean autoScaleX)
 	{
 		this.autoScaleX = autoScaleX;
 	}
 	
 	
 	/**
 	 * @param autoScaleY The autoScaleY to set.
 	 */
 	public void setAutoScaleY(boolean autoScaleY)
 	{
 		this.autoScaleY = autoScaleY;
 	}
 }
