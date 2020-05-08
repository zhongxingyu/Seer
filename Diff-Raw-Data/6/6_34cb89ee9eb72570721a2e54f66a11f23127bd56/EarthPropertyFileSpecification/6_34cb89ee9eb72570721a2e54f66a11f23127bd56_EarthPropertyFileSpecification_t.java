 package org.eclipse.stem.internal.data.geography.specifications;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.StringTokenizer;
 
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.data.geography.ISOKey;
 import org.eclipse.stem.definitions.labels.EarthScienceLabel;
 import org.eclipse.stem.definitions.labels.EarthScienceLabelValue;
 import org.eclipse.stem.definitions.labels.LabelsFactory;
 import org.eclipse.stem.definitions.labels.impl.EarthScienceLabelImpl;
 import org.eclipse.stem.definitions.nodes.impl.RegionImpl;
 import org.eclipse.stem.internal.data.geography.propertydata.EarthSciencePropertyData;
 import org.eclipse.stem.internal.data.propertydata.PropertyData;
 
 /**
  * This class represents a {@link Graph} containing {@link EarthScienceLabel}s of
  * {@link Node}s specific to a country.
  */
 public class EarthPropertyFileSpecification extends
 		CountryLabelPropertyFileSpecification {
 
 	public final static String DATATYPE = "DATA_TYPE";
 	public final static String DATAUNITS = "UNITS";
 	
 	private String dataType;
 	private String dataUnits;
 	
 	/**
 	 * This is the name used to identify earth science labels.
 	 */
 	public final static String EARTH_LABEL_NAME = "earthscience"; //$NON-NLS-1$
 
 	/**
 	 * Constructor
 	 */
 	public EarthPropertyFileSpecification() {
 		super(EARTH_LABEL_NAME);
 	} // EarthPropertyFileSpecification
 
 	@Override
 	protected PropertyData createPropertyDataInstanceFromProperty(
 			final String dataPropertyKey, final String propertyValue) {
 		
 		if(dataPropertyKey.equals(DATATYPE)) 
			{this.dataType = propertyValue;
			this.labelName = EARTH_LABEL_NAME+"_"+dataType;  
			return null;
		}
				
 		if(dataPropertyKey.equals(DATAUNITS)) 
 			{this.dataUnits = propertyValue;return null;}
 		
 		return new EarthSciencePropertyData(new ISOKey(dataPropertyKey), propertyValue);
 	} // createDataSetData
 
 	@Override
 	protected NodeLabel createLabel(final AdminLevel adminLevel,
 			final ISOKey isoKey, final PropertyData graphData) {
 
 		final EarthSciencePropertyData eaData = (EarthSciencePropertyData) graphData;
 		final String nodeISOKey = eaData.getISOKey().toString();
 
 		final EarthScienceLabel retValue = LabelsFactory.eINSTANCE.createEarthScienceLabel();
 		retValue.setURI(EarthScienceLabelImpl.createEarthScienceLabelURI(adminLevel.intValue(),
 				isoKey.toString(), nodeISOKey, dataType));
 		retValue.setURIOfIdentifiableToBeLabeled(RegionImpl
 				.createRegionNodeURI(nodeISOKey));
 		
 		EarthScienceLabelValue value = retValue.getCurrentEarthScienceValue();
 		
 		
 		value.setDataType(dataType);
 		value.setUnits(dataUnits);
 		
 		StringTokenizer st = new StringTokenizer(eaData.getEarthScienceData(),",");
 		
 		while(st.hasMoreTokens()) {
 			String d = st.nextToken();
 			value.getData().add(Double.parseDouble(d));
 		}
 
 		return retValue;
 	} // createAreaLabel
 
 } // CountryAreaLabelPropertyFileSpecification
