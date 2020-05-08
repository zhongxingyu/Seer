 /*
  * #%L
  * Cyni Implementation (cyni-impl)
  * $Id:$
  * $HeadURL:$
  * %%
  * Copyright (C) 2006 - 2013 The Cytoscape Consortium
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.cytoscape.cyni.internal.metrics;
 
 
 import java.util.*;
 
 import org.cytoscape.cyni.*;
 
 
 /**
  * The BasicInduction provides a very simple Induction, suitable as
  * the default Induction for Cytoscape data readers.
  */
 public class MDLMetric extends AbstractCyniMetric {
 	
 	private EntropyMetric entropy;
 	/**
 	 * Creates a new  object.
 	 */
 	public MDLMetric() {
 		super("MDL.cyni","Minimum Description Length Metric");
 		addType(CyniMetricTypes.INPUT_STRINGS.toString());
 		addType(CyniMetricTypes.LOCAL_METRIC_SCORE.toString());
 		entropy = new EntropyMetric();
 		
 	}
 	
 	public void resetParameters()
 	{
 		entropy.resetParameters();
 	}
 
 	
 	public Double getMetric(CyniTable table1, CyniTable table2, int indexBase, List<Integer> indexToCompare) { 
 		double result = 0.0;
 		int nCounts;
 		int numValues ;
 
 
 		result = entropy.getMetric(table1, table2, indexBase, indexToCompare);
 		numValues = table1.getAttributeStringValues().size();
 		nCounts  =  (int)Math.pow((double)numValues, (double)indexToCompare.size());
 		
		result += 0.5 * nCounts * (numValues - 1) * log2(table1.nColumns());
 		
 		
 		return  result;
 	}
 	
	static double log2(double x)
	{
	    return (double) (Math.log(x) / Math.log(2.0));
	}
 	
 	
 }
