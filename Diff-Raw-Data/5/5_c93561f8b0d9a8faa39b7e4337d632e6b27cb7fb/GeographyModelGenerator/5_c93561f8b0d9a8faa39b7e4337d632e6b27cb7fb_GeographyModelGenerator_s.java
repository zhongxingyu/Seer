 // GeographyModelGenerator.java
 package org.eclipse.stem.internal.data.geography.models;
 
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
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.stem.core.Constants;
 import org.eclipse.stem.core.graph.GraphPackage;
 import org.eclipse.stem.data.geography.ISOKey;
 import org.eclipse.stem.definitions.nodes.Region;
 import org.eclipse.stem.internal.data.ModelGenerator;
 import org.eclipse.stem.internal.data.PluginFileGenerator;
 import org.eclipse.stem.internal.data.generatedplugin.DublinCore;
 import org.eclipse.stem.internal.data.generatedplugin.Extension;
 import org.eclipse.stem.internal.data.generatedplugin.GeneratedpluginFactory;
 import org.eclipse.stem.internal.data.generatedplugin.Plugin;
 import org.eclipse.stem.internal.data.generatedplugin.StemCategory;
 import org.eclipse.stem.internal.data.geography.GeographyPluginFileGenerator;
 import org.eclipse.stem.internal.data.geography.specifications.AdminLevel;
 import org.eclipse.stem.internal.data.geography.specifications.AdminLevelSet;
 import org.eclipse.stem.internal.data.geography.specifications.CommonBorderGeographicRelationshipPropertyFileSpecification;
 import org.eclipse.stem.internal.data.geography.specifications.CountryAreaLabelPropertyFileSpecification;
 import org.eclipse.stem.internal.data.geography.specifications.CountryGraphPropertyFileSpecification;
 import org.eclipse.stem.internal.data.geography.specifications.RelativePhysicalGeographicRelationshipPropertyFileSpecification;
 import org.eclipse.stem.internal.data.specifications.GeographicModelSpecification;
 import org.eclipse.stem.internal.data.specifications.GraphPropertyFileSpecification;
 import org.eclipse.stem.internal.data.specifications.ModelSpecification;
 
 /**
  * This class processes the generated Geography Graph files to create Geography
  * Models.
  */
 public class GeographyModelGenerator extends ModelGenerator {
 
 	/**
 	 * The id of the Geography category
 	 */
 	public static final String ID_MODEL_GEOGRAPHY_CATEGORY = PluginFileGenerator.ID_MODEL_CATEGORY
 			+ ".geography"; //$NON-NLS-1$
 
 	/**
 	 * The id of the Political category
 	 */
 	public static final String ID_MODEL_GEOGRAPHY_POLITICAL_CATEGORY = ID_MODEL_GEOGRAPHY_CATEGORY
 			+ ".political"; //$NON-NLS-1$
 
 	/**
 	 * The id of the Countries category
 	 */
 	public static final String ID_MODEL_GEOGRAPHY_POLITICAL_COUNTRIES_CATEGORY = ID_MODEL_GEOGRAPHY_POLITICAL_CATEGORY
 			+ ".countries"; //$NON-NLS-1$
 
 	private static final Subject REGION_SUBJECT = new Subject(
 			Region.DUBLIN_CORE_SUBJECT);
 	private static final Subject AREA_SUBJECT = new Subject(
 			CountryAreaLabelPropertyFileSpecification.AREA_LABEL_NAME);
 	private static final Subject COMMON_BORDER_SUBJECT = new Subject(
 			CommonBorderGeographicRelationshipPropertyFileSpecification.COMMON_BORDER_EDGE_NAME);
 	private static final Subject RELATIVE_PHYSICAL_EDGE_NAME_SUBJECT = new Subject(
 			RelativePhysicalGeographicRelationshipPropertyFileSpecification.RELATIVE_PHYSICAL_EDGE_NAME);
 
 	/**
 	 * @param args
 	 *            the URI of the generated geography plugin.xml file
 	 */
 	public static void main(final String[] args) {
 		// Is the plugin file specified?
 		if (args.length == 0) {
 			// Yes
 			System.err.println("Missing specification of the plugin file"); //$NON-NLS-1$
 		} // if
 		else {
 
 			final String GENERATED_FILES_PATH = args[0];
 
 			final String sourceProjectName = args[1];
 
 			final String GENERATED_MODELS_PATH = GENERATED_FILES_PATH
 					+ File.separator + "resources" + File.separator + "data"; //$NON-NLS-1$ //$NON-NLS-2$
 
 			final File file = new File(
 					".."	+ File.separator + sourceProjectName + File.separator + PluginFileGenerator.PLUGIN_XML_FILE_NAME); //$NON-NLS-1$
 			final URI pluginFileURI = URI.createFileURI(file.getAbsolutePath());
 
 			final GeographyModelGenerator dmg = new GeographyModelGenerator();
 			final Map<ISOKey, List<GeographicModelSpecification>> modelSpecifications = dmg
 					.processFiles(pluginFileURI);
 
 			for (final ISOKey isoKey : sortISOKeys(modelSpecifications)) {
 				// Now serialize the Identifiables
 				for (final ModelSpecification gms : modelSpecifications
 						.get(isoKey)) {
 					try {
 						gms.serialize(GENERATED_MODELS_PATH);
 					} catch (final IOException e) {
 						e.printStackTrace();
 					}
 				} // for gms
 			} // for isoKey
 
 			// Create the instance of plugin.xml that we'll serialize later
 			final Plugin pluginxml = GeneratedpluginFactory.eINSTANCE
 					.createPlugin();
 			final Extension extension = GeneratedpluginFactory.eINSTANCE
 					.createExtension();
 
 			// Add the "model" extension point
 			extension.setPoint(Constants.ID_MODEL_EXTENSION_POINT);
 
 			// Add the categories to the extension
 			addCatagoriesToExtension(extension);
 
 			pluginxml.getExtensionelement().add(extension);
 
 			// Now add the dublin core entries to the plugin.xml file for each
 			// of the models
 			for (final ISOKey isoKey : sortISOKeys(modelSpecifications)) {
 				final StemCategory countryCategory = GeneratedpluginFactory.eINSTANCE
 						.createStemCategory();
 				final String COUNTRY_CATAGORY_STRING = ID_MODEL_GEOGRAPHY_POLITICAL_COUNTRIES_CATEGORY
 						+ "." + isoKey.toString().toLowerCase(); //$NON-NLS-1$
 				countryCategory.setId(COUNTRY_CATAGORY_STRING);
 				countryCategory.setName(isoKey.toString());
 				countryCategory
 						.setParentId(ID_MODEL_GEOGRAPHY_POLITICAL_COUNTRIES_CATEGORY);
 				extension.getCategories().add(countryCategory);
 				for (final ModelSpecification gms : modelSpecifications
 						.get(isoKey)) {
 					final DublinCore dc = GeneratedpluginFactory.eINSTANCE
 							.createDublinCore();
 					dc.setCategoryId(COUNTRY_CATAGORY_STRING);
 					extension.getDublinCores().add(
 							populateGeneratedDC(dc, gms.getDublinCore()));
 				} // for gms
 			} // for isoKey
 
 			final URI TEMP_PLUGINXML_URI = URI
 					.createFileURI(GENERATED_FILES_PATH + File.separator
 							+ PluginFileGenerator.PLUGIN_XML_FILE_NAME);
 
 			// Serialize the plugin.xml file.
 			ModelGenerator.writePluginxml(pluginxml, TEMP_PLUGINXML_URI);
 
 			// Create the plugin.properties file
 			createPluginPropertties(GENERATED_FILES_PATH);
 
 		} // else
 	} // main
 
 	/**
 	 * @param path
 	 */
 	public static void createPluginPropertties(final String path) {
 		final File pluginProperties = new File(path + File.separator
 				+ PluginFileGenerator.PLUGIN_PROPERTIES_FILE_NAME);
 		try {
 			final BufferedOutputStream pluginPropertiesOS = new BufferedOutputStream(
 					new FileOutputStream(pluginProperties));
 			System.out.println();
 			final PrintStream ps = new PrintStream(pluginPropertiesOS);
 			ps
 					.println("pluginName = STEM Geographic Model Definitions"); //$NON-NLS-1$
 			ps.println("providerName = " + PluginFileGenerator.PROVIDER_NAME); //$NON-NLS-1$
 			ps.println(PluginFileGenerator.UI_STEM_CATEGORY_NAME
 					+ " = " + PluginFileGenerator.STEM_CATEGORY_NAME); //$NON-NLS-1$
 			ps
 					.println(GeographyPluginFileGenerator.UI_GEOGRAPHY_CATEGORY_NAME
 							+ " = " + GeographyPluginFileGenerator.GEOGRAPHY_CATEGORY_NAME); //$NON-NLS-1$
 			ps
 					.println(GeographyPluginFileGenerator.UI_POLITICAL_CATEGORY_NAME
 							+ " = " + GeographyPluginFileGenerator.POLITITCAL_CATEGORY_NAME); //$NON-NLS-1$
 			ps
 					.println(GeographyPluginFileGenerator.UI_COUNTRIES_CATEGORY_NAME
 							+ " = " + GeographyPluginFileGenerator.COUNTRIES_CATEGORY_NAME); //$NON-NLS-1$
 			ps.flush();
 			ps.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	} // createPluginPropertties
 
 	private static void addCatagoriesToExtension(final Extension extension) {
 
 		final StemCategory modelCatagory = GeneratedpluginFactory.eINSTANCE
 				.createStemCategory();
 		modelCatagory.setId(PluginFileGenerator.ID_MODEL_CATEGORY);
 		modelCatagory.setName("%" + PluginFileGenerator.UI_STEM_CATEGORY_NAME); //$NON-NLS-1$
 		modelCatagory.setParentId("/"); //$NON-NLS-1$
 		extension.getCategories().add(modelCatagory);
 
 		final StemCategory geographyCatagory = GeneratedpluginFactory.eINSTANCE
 				.createStemCategory();
 		geographyCatagory.setId(ID_MODEL_GEOGRAPHY_CATEGORY);
 		geographyCatagory
 				.setName("%" + GeographyPluginFileGenerator.UI_GEOGRAPHY_CATEGORY_NAME); //$NON-NLS-1$
 		geographyCatagory.setParentId(PluginFileGenerator.ID_MODEL_CATEGORY);
 		extension.getCategories().add(geographyCatagory);
 
 		final StemCategory politicalCatagory = GeneratedpluginFactory.eINSTANCE
 				.createStemCategory();
 		politicalCatagory.setId(ID_MODEL_GEOGRAPHY_POLITICAL_CATEGORY);
 		politicalCatagory
 				.setName("%" + GeographyPluginFileGenerator.UI_POLITICAL_CATEGORY_NAME); //$NON-NLS-1$
 		politicalCatagory.setParentId(ID_MODEL_GEOGRAPHY_CATEGORY);
 		extension.getCategories().add(politicalCatagory);
 
 		final StemCategory countriesCatagory = GeneratedpluginFactory.eINSTANCE
 				.createStemCategory();
 		countriesCatagory
 				.setId(ID_MODEL_GEOGRAPHY_POLITICAL_COUNTRIES_CATEGORY);
 		countriesCatagory
 				.setName("%" + GeographyPluginFileGenerator.UI_COUNTRIES_CATEGORY_NAME); //$NON-NLS-1$
 		countriesCatagory.setParentId(ID_MODEL_GEOGRAPHY_POLITICAL_CATEGORY);
 		extension.getCategories().add(countriesCatagory);
 	} // addCatagoriesToExtension
 
 	/**
 	 * @param map
 	 *            a mapping between {@link ISOKey} and Object
 	 * @return a list of the {@link ISOKey}s sorted in descending alphabetical
 	 *         order
 	 */
 	public static List<ISOKey> sortISOKeys(
 			final Map<ISOKey, ? extends Object> map) {
 		final List<ISOKey> retValue = new ArrayList<ISOKey>();
 
 		retValue.addAll(map.keySet());
 		Collections.sort(retValue);
 		return retValue;
 	} // sortISOKeys
 
 	@SuppressWarnings("unused")
 	private static void printModelSpecifications(
 			final Map<ISOKey, List<GeographicModelSpecification>> modelSpecifications) {
 		final List<ISOKey> sortedISOKeys = sortISOKeys(modelSpecifications);
 		for (final ISOKey isoKey : sortedISOKeys) {
 			final List<GeographicModelSpecification> temp = modelSpecifications
 					.get(isoKey);
 			System.out.println(isoKey + " : " + temp); //$NON-NLS-1$
 		} // for isoKey
 
 	} // printModelSpecifications
 
 	protected Map<ISOKey, List<GeographicModelSpecification>> processFiles(
 			final URI pluginFileURI) {
 		final Map<ISOKey, List<GeographicModelSpecification>> retValue = new HashMap<ISOKey, List<GeographicModelSpecification>>();
 
 		final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> nodeMap = new HashMap<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>();
 		final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> labelMap = new HashMap<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>();
 		final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap = new HashMap<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>>();
 
 		// Get the contents of the plug.xml file
 		final Plugin plugin = getPluginxml(pluginFileURI);
 
 		for (final Extension extension : plugin.getExtensionelement()) {
 			for (final DublinCore dc : extension.getDublinCores()) {
 				final String[] typeSubject = GraphPropertyFileSpecification
 						.parseDublinCoreSubjectString(dc.getSubject());
 
 				final Subject subject = new Subject(typeSubject[1]);
 
 				final Map<ISOKey, AdminLevelSet> isoKeyAdminLevelSetMap = CountryGraphPropertyFileSpecification
 						.parseDublinCoreCoverageString(dc.getCoverage());
 
 				// Edge?
 				if (typeSubject[0].equals(GraphPackage.Literals.EDGE.getName())) {
 					populateEdgeMap(isoKeyAdminLevelSetMap, subject, dc,
 							edgeMap);
 				} // if Edge
 				else {
 					// No
 					final ISOKey isoKey = (ISOKey) isoKeyAdminLevelSetMap
 							.keySet().toArray()[0];
 					final AdminLevel adminLevel = isoKeyAdminLevelSetMap.get(
 							isoKey).getMaxAdminLevel();
 
 					poulateMap(isoKey, adminLevel, subject, dc,
 							typeSubject[0].equals(GraphPackage.Literals.NODE
 									.getName()) ? nodeMap : labelMap);
 				} // if Node
 			} // else not Edge
 		} // for each extension
 
 		final Map<ISOKey, AdminLevelSet> isoKeyAdminMap = extractISOKeyAdminLevels(nodeMap);
 		for (final ISOKey isoKey : isoKeyAdminMap.keySet()) {
 			retValue.put(isoKey, makeAllModels(isoKey, isoKeyAdminMap
 					.get(isoKey), nodeMap, labelMap, edgeMap));
 		} // for each isoKey
 
 		// printMapAdminLevels(nodeMap);
 
 		// printMap(nodeMap);
 		//
 		// printMap(labelMap);
 		//
 		// printEdgeMap(edgeMap);
 
 		return retValue;
 	} // processFiles
 
 	private List<GeographicModelSpecification> makeAllModels(
 			final ISOKey isoKey,
 			final AdminLevelSet adminLevelSet,
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> nodeMap,
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> labelMap,
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 		final List<GeographicModelSpecification> retValue = new ArrayList<GeographicModelSpecification>();
 
 		for (final AdminLevelSet levelSet : adminLevelSet.makeAllLevelSets()) {
 			final GeographicModelSpecification temp = new GeographicModelSpecification(
 					isoKey, levelSet);
 
 			temp.addNodeDCs(findDCs(REGION_SUBJECT, levelSet, nodeMap
 					.get(isoKey)));
 
 			temp.addAreaDCs(findDCs(AREA_SUBJECT, levelSet, labelMap
 					.get(isoKey)));
 
 			final List<DublinCore> commonBorderEdgeDCs = findEdgeDCs(
 					COMMON_BORDER_SUBJECT, isoKey, levelSet, isoKey, levelSet,
 					edgeMap);
 
 			temp.addCommonBorderEdgeDCs(commonBorderEdgeDCs);
 
 			final List<DublinCore> containmentEdgeDCs = findEdgeDCs(
 					RELATIVE_PHYSICAL_EDGE_NAME_SUBJECT, isoKey, levelSet,
 					isoKey, levelSet, edgeMap);
 			temp.addContainmentEdgeDCs(containmentEdgeDCs);
 			retValue.add(temp);
 		} // for each admin level set
 
 		Collections.sort(retValue);
 		return retValue;
 	} // makeAllModels
 
 	private List<DublinCore> findDCs(
 			final Subject subject,
 			final AdminLevelSet levelSet,
 			final Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap) {
 		final List<DublinCore> retValue = new ArrayList<DublinCore>();
 		for (final AdminLevel adminLevel : levelSet.getAdminLevels()) {
 			final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = adminMap
 					.get(adminLevel);
 			final Map<ValidDateRange, DublinCore> dateRangeMap = subjectMap
 					.get(subject);
 			// Should be just one
 			final DublinCore dc = (DublinCore) dateRangeMap.values().toArray()[0];
 			retValue.add(dc);
 		} // for adminLevel
 
 		return retValue;
 	} // findNodeDCs
 
 	private List<DublinCore> findEdgeDCs(
 			final Subject subject,
 			final ISOKey isoKey0,
 			final AdminLevelSet levelSet0,
 			final ISOKey isoKey1,
 			final AdminLevelSet levelSet1,
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 		final List<DublinCore> retValue = new ArrayList<DublinCore>();
 
 		// Same ISOKey?
 		if (isoKey0.equals(isoKey1)) {
 			// Yes
 			// Common Border?
 			if (subject.equals(COMMON_BORDER_SUBJECT)) {
 				// Yes
 				for (final AdminLevel adminLevel : levelSet0.getAdminLevels()) {
 					final DublinCore tempDC = getEdgeDC(subject, isoKey0,
 							adminLevel, isoKey0, adminLevel, edgeMap);
 					if (tempDC != null) {
 						retValue.add(tempDC);
 					}
 				} // for
 			} // if common border
 			else {
 				// No
 				for (final AdminLevel adminLevel0 : levelSet0.getAdminLevels()) {
 					for (final AdminLevel adminLevel1 : levelSet1
 							.getAdminLevels()) {
 						final DublinCore tempDC = getEdgeDC(subject, isoKey0,
 								adminLevel0, isoKey0, adminLevel1, edgeMap);
 						if (tempDC != null) {
 							retValue.add(tempDC);
 						}
 					} // for adminLevel1
 				} // for adminLevel0
 			} // else
 
 		} // if same ISOKey
 		else {
 			// No
 			// Different ISOKeys
 			final DublinCore tempDC = getEdgeDC(subject, isoKey0, levelSet0
 					.getMaxAdminLevel(), isoKey1, levelSet1.getMaxAdminLevel(),
 					edgeMap);
 			if (tempDC != null) {
 				retValue.add(tempDC);
 			}
 		} // else
 
 		return retValue;
 	} // findEdgeDCs
 
 	private DublinCore getEdgeDC(
 			final Subject subject,
 			final ISOKey isoKey0,
 			final AdminLevel adminLevel0,
 			final ISOKey isoKey1,
 			final AdminLevel adminLevel1,
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 		DublinCore retValue = null;
 
 		final Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>> adminMap0 = edgeMap
 				.get(isoKey0);
 		if (adminMap0 != null) {
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> isoMap0 = adminMap0
 					.get(adminLevel0);
 			if (isoMap0 != null) {
 				final Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap1 = isoMap0
 						.get(isoKey1);
 				if (adminMap1 != null) {
 					final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = adminMap1
 							.get(adminLevel1);
 					if (subjectMap != null) {
 						final Map<ValidDateRange, DublinCore> dateRangeMap = subjectMap
 								.get(subject);
 						if (dateRangeMap != null) {
 							retValue = (DublinCore) dateRangeMap.values()
 									.toArray()[0];
 						}
 					} // if subjectMap
 
 				} // if adminMap1
 			}
 		} // if adminMap0
 		return retValue;
 	} // getEdgeDC
 
 	private void populateEdgeMap(
 			final Map<ISOKey, AdminLevelSet> isoKeyAdminLevelSetMap,
 			final Subject subject,
 			final DublinCore dc,
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 
 		final List<ISOKey> sortedISOKeys = sortISOKeys(isoKeyAdminLevelSetMap);
 
 		// How many keys?
 		switch (sortedISOKeys.size()) {
 		case 1:
 			final AdminLevelSet adminLevelSet = isoKeyAdminLevelSetMap
 					.get(sortedISOKeys.get(0));
 
 			// How many levels?
 			switch (adminLevelSet.getNumAdminLevels()) {
 			case 1:
 				// Within the same level
 				final AdminLevel adminLevel = adminLevelSet.getMinAdminLevel();
 
 				final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = getSubjectMap(
 						sortedISOKeys.get(0), adminLevel, sortedISOKeys.get(0),
 						adminLevel, edgeMap);
 				getDateRangeMap(subject, subjectMap).put(
 						new ValidDateRange(dc.getValid()), dc);
 				break;
 			case 2:
 				// Between administrative levels
 				final AdminLevel adminLevel0 = adminLevelSet.getMinAdminLevel();
 				final AdminLevel adminLevel1 = adminLevelSet.getMaxAdminLevel();
 
 				final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap2 = getSubjectMap(
 						sortedISOKeys.get(0), adminLevel0,
 						sortedISOKeys.get(0), adminLevel1, edgeMap);
 				getDateRangeMap(subject, subjectMap2).put(
 						new ValidDateRange(dc.getValid()), dc);
 
 				break;
 			default:
 				System.out.println("Problem!"); //$NON-NLS-1$
 				break;
 			}
 			break;
 
 		case 2:
 			// Between countries
 			final AdminLevelSet adminLevelSet0 = isoKeyAdminLevelSetMap
 					.get(sortedISOKeys.get(0));
 			final AdminLevelSet adminLevelSet1 = isoKeyAdminLevelSetMap
 					.get(sortedISOKeys.get(1));
 
 			final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = getSubjectMap(
 					sortedISOKeys.get(0), adminLevelSet0.getMinAdminLevel(),
 					sortedISOKeys.get(1), adminLevelSet1.getMinAdminLevel(),
 					edgeMap);
 			getDateRangeMap(subject, subjectMap).put(
 					new ValidDateRange(dc.getValid()), dc);
 			break;
 		default:
 			System.out.println("Problem!!"); //$NON-NLS-1$
 			break;
 		} // switch
 
 	} // populateEdgeMap
 
 	private void poulateMap(
 			final ISOKey isoKey,
 			final AdminLevel adminLevel,
 			final Subject subject,
 			final DublinCore dc,
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> map) {
 		Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap = map
 				.get(isoKey);
 		// Got a adminMap map for this ISO Key?
 		if (adminMap == null) {
 			// NO
 			adminMap = new HashMap<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>();
 			map.put(isoKey, adminMap);
 		}
 		Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = adminMap
 				.get(adminLevel);
 
 		// Got an subjectMap map?
 		if (subjectMap == null) {
 			// No
 			subjectMap = new HashMap<Subject, Map<ValidDateRange, DublinCore>>();
 			adminMap.put(adminLevel, subjectMap);
 		}
 
 		getDateRangeMap(subject, subjectMap).put(
 				new ValidDateRange(dc.getValid()), dc);
 	} // poulateMap
 
 	private Map<Subject, Map<ValidDateRange, DublinCore>> getSubjectMap(
 			final ISOKey isoKey1,
 			final AdminLevel adminLevel1,
 			final ISOKey isoKey2,
 			final AdminLevel adminLevel2,
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 		Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>> adminMap1 = edgeMap
 				.get(isoKey1);
 		if (adminMap1 == null) {
 			adminMap1 = new HashMap<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>();
 			edgeMap.put(isoKey1, adminMap1);
 		}
 		Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> isoMap1 = adminMap1
 				.get(adminLevel1);
 		if (isoMap1 == null) {
 			isoMap1 = new HashMap<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>();
 			adminMap1.put(adminLevel1, isoMap1);
 		}
 		Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap2 = isoMap1
 				.get(isoKey2);
 		if (adminMap2 == null) {
 			adminMap2 = new HashMap<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>();
 			isoMap1.put(isoKey2, adminMap2);
 		}
 		Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap = adminMap2
 				.get(adminLevel2);
 		if (subjectMap == null) {
 			subjectMap = new HashMap<Subject, Map<ValidDateRange, DublinCore>>();
 			adminMap2.put(adminLevel2, subjectMap);
 		}
 
 		return subjectMap;
 	} // getSubjectMap
 
 	private Map<ValidDateRange, DublinCore> getDateRangeMap(
 			final Subject subject,
 			final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap) {
 		Map<ValidDateRange, DublinCore> dateRangeMap = subjectMap.get(subject);
 
 		// Got a valid date range map?
 		if (dateRangeMap == null) {
 			// No
 			dateRangeMap = new HashMap<ValidDateRange, DublinCore>();
 			subjectMap.put(subject, dateRangeMap);
 		}
 		return dateRangeMap;
 	} // getDateRangeMap
 
 	private Map<ISOKey, AdminLevelSet> extractISOKeyAdminLevels(
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> map) {
 		final Map<ISOKey, AdminLevelSet> retValue = new HashMap<ISOKey, AdminLevelSet>();
 
 		for (final ISOKey isoKey : sortISOKeys(map)) {
 			final Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap = map
 					.get(isoKey);
 			final AdminLevelSet temp = new AdminLevelSet();
 			for (final AdminLevel adminLevel : adminMap.keySet()) {
 				temp.addAdminLevel(adminLevel);
 			} // for adminLevel
 			retValue.put(isoKey, temp);
 		} // for isoKey
 		return retValue;
 	} // extractISOKeyAdminLevels
 
 	@SuppressWarnings("unused")
 	private void printMap(
 			final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> map) {
 		System.out
 				.println("****************************************************************"); //$NON-NLS-1$
 
 		for (final ISOKey isoKey : sortISOKeys(map)) {
 			System.out.println(isoKey);
 			final Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap = map
 					.get(isoKey);
 			for (final AdminLevel adminLevel : adminMap.keySet()) {
 				System.out.println("\t" + adminLevel); //$NON-NLS-1$
 				printSubjectMap(adminMap.get(adminLevel));
 			} // for adminLevel
 		} // for nodeMap isoKey
 	} // printMap
 
 	@SuppressWarnings("unused")
 	private void printEdgeMap(
 			final Map<ISOKey, Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>>> edgeMap) {
 		System.out
 				.println("****************************************************************"); //$NON-NLS-1$
 
 		for (final ISOKey isoKey0 : sortISOKeys(edgeMap)) {
 			System.out.println(isoKey0);
 			final Map<AdminLevel, Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>>> adminMap0 = edgeMap
 					.get(isoKey0);
 			for (final AdminLevel adminLeve0 : adminMap0.keySet()) {
 				System.out.println("\t" + adminLeve0); //$NON-NLS-1$
 				final Map<ISOKey, Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>>> isoKeyMap1 = adminMap0
 						.get(adminLeve0);
 
 				for (final ISOKey isoKey1 : sortISOKeys(isoKeyMap1)) {
 					System.out.println("\t\t" + isoKey1); //$NON-NLS-1$
 					final Map<AdminLevel, Map<Subject, Map<ValidDateRange, DublinCore>>> adminMap1 = isoKeyMap1
 							.get(isoKey1);
 					for (final AdminLevel adminLevel1 : adminMap1.keySet()) {
 						System.out.println("\t\t\t" + adminLevel1); //$NON-NLS-1$
 						printSubjectMap(adminMap1.get(adminLevel1));
 					} // for adminLevel
 				} // for isoKey1
 			} // for each adminLevel
 		} // for each ISO Key
 
 	} // printEdgeMap
 
 	private void printSubjectMap(
 			final Map<Subject, Map<ValidDateRange, DublinCore>> subjectMap) {
 		for (final Subject subject : subjectMap.keySet()) {
 			System.out.println("\t\t\t\t" + subject); //$NON-NLS-1$
 			final Map<ValidDateRange, DublinCore> dateRangeMap = subjectMap
 					.get(subject);
 			for (final ValidDateRange validDateRange : dateRangeMap.keySet()) {
 				final DublinCore dc = dateRangeMap.get(validDateRange);
 				System.out
 						.println("\t\t\t\t\t" + validDateRange + " : " + dc.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
 			} // for validDateRange
 		} // for subject
 	}
 
 	private static class ValidDateRange {
 		String dcValidString;
 
 		/**
 		 * @param dcValidString
 		 */
 		public ValidDateRange(final String dcValidString) {
 			this.dcValidString = dcValidString;
 		}
 
 		/**
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return dcValidString.hashCode();
 		}
 
 		/**
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(final Object obj) {
			return dcValidString.equals(obj);
 		}
 
 		/**
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return dcValidString;
 		}
 
 	} // ValidDateRange
 
 	private static class Subject {
 		String subject;
 
 		/**
 		 * @param subject
 		 */
 		public Subject(final String subject) {
 			this.subject = subject;
 		}
 
 		/**
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return subject.hashCode();
 		}
 
 		/**
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			final Subject other = (Subject) obj;
 			if (subject == null) {
 				if (other.subject != null) {
 					return false;
 				}
 			} else if (!subject.equals(other.subject)) {
 				return false;
 			}
 			return true;
 		}
 
 		/**
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return subject;
 		}
 
 	} // Subject
 
 } // GeographyModelGenerator
