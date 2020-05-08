 /*
  File: CalculatorIO.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 
 
 //----------------------------------------------------------------------------
 import static cytoscape.visual.VisualPropertyType.EDGE_LINETYPE;
 import static cytoscape.visual.VisualPropertyType.EDGE_LINE_STYLE;
 import static cytoscape.visual.VisualPropertyType.EDGE_LINE_WIDTH;
 import static cytoscape.visual.VisualPropertyType.EDGE_SRCARROW;
 import static cytoscape.visual.VisualPropertyType.EDGE_SRCARROW_COLOR;
 import static cytoscape.visual.VisualPropertyType.EDGE_SRCARROW_SHAPE;
 import static cytoscape.visual.VisualPropertyType.EDGE_TGTARROW;
 import static cytoscape.visual.VisualPropertyType.EDGE_TGTARROW_COLOR;
 import static cytoscape.visual.VisualPropertyType.EDGE_TGTARROW_SHAPE;
 import static cytoscape.visual.VisualPropertyType.NODE_BORDER_COLOR;
 import static cytoscape.visual.VisualPropertyType.NODE_FILL_COLOR;
 import static cytoscape.visual.VisualPropertyType.NODE_HEIGHT;
 import static cytoscape.visual.VisualPropertyType.NODE_LINETYPE;
 import static cytoscape.visual.VisualPropertyType.NODE_LINE_STYLE;
 import static cytoscape.visual.VisualPropertyType.NODE_LINE_WIDTH;
 import static cytoscape.visual.VisualPropertyType.NODE_SIZE;
 import static cytoscape.visual.VisualPropertyType.NODE_WIDTH;
 
 import java.io.BufferedReader;
 import java.io.Writer;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.calculators.CalculatorFactory;
 
 
 //----------------------------------------------------------------------------
 /**
  * This class defines static methods for reading calculator definitions from a
  * properties object and installing them into a CalculatorCatalog, and for
  * constructing a properties object that describes all the calculators in a
  * CalculatorCatalog.
  */
 public class CalculatorIO {
 	private static final String nodeColorBaseKey = "nodeColorCalculator";
 	private static final String nodeSizeBaseKey = "nodeSizeCalculator";
 	private static final String edgeArrowBaseKey = "edgeArrowCalculator";
 
 	// appearance labels
 	private static final String nodeAppearanceBaseKey = "nodeAppearanceCalculator";
 	private static final String edgeAppearanceBaseKey = "edgeAppearanceCalculator";
 	private static final String globalAppearanceBaseKey = "globalAppearanceCalculator";
 
 	/**
 	 * Writes the contents of a CalculatorCatalog to the specified file as a
 	 * properties file. This method sorts the lines of text produced by the
 	 * store method of Properties, so that the properties descriptions of the
 	 * calculators are reasonably human-readable.
 	 */
 	public static void storeCatalog(CalculatorCatalog catalog, File outFile) {
 		try {
 			final Writer writer = new FileWriter(outFile);
 			storeCatalog(catalog,writer);
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Writes the contents of a CalculatorCatalog to the specified writer as a
 	 * properties file. This method sorts the lines of text produced by the
 	 * store method of Properties, so that the properties descriptions of the
 	 * calculators are reasonably human-readable.
 	 */
 	public static void storeCatalog(CalculatorCatalog catalog, Writer writer) {
 		// construct the header comment for the file
 		final String lineSep = System.getProperty("line.separator");
 		final StringBuffer header = new StringBuffer();
 		header.append("This file specifies visual mappings for Cytoscape");
 		header.append(" and has been automatically generated.").append(lineSep);
 		header.append("# WARNING: any changes you make to this file while");
 		header.append(" Cytoscape is running may be overwritten.").append(lineSep);
 		header.append("# Any changes may make these visual mappings unreadable.");
 		header.append(lineSep);
 		header.append("# Please make sure you know what you are doing before");
 		header.append(" modifying this file by hand.").append(lineSep);
 		
 		final BufferedReader reader;
 		final ByteArrayOutputStream buffer;
 
 		try {
 
 			// get a Properties description of the catalog
 			final Properties props = getProperties(catalog);
 
 			// and dump it to a buffer of bytes
 			buffer = new ByteArrayOutputStream();
 			props.store(buffer, header.toString());
 
 			// convert the bytes to a String we can read from
 			reader = new BufferedReader(new StringReader(buffer.toString()));
 
 			// read all the lines and store them in a container object
 			// store the header lines separately so they don't get sorted
 			final List<String> headerLines = new ArrayList<String>();
 			final List<String> lines = new ArrayList<String>();
 
 			String oneLine = reader.readLine();
 
 			while (oneLine != null) {
 				if (oneLine.startsWith("#"))
 					headerLines.add(oneLine);
				else if (oneLine.toUpperCase().contains("EDGELINETYPE") == false
				         && oneLine.toUpperCase().contains("NODELINETYPE") == false
				         && oneLine.toUpperCase().contains("ARROW=") == false
				         )
 					lines.add(oneLine);
 
 				oneLine = reader.readLine();
 			}
 
 			buffer.close();
 			reader.close();
 
 			// now sort all the non-header lines
 			Collections.sort(lines);
 
 			// and write to file
 			for (String theLine : headerLines) {
 				writer.write(theLine, 0, theLine.length());
 				writer.write(lineSep);
 			}
 
 			for (String theLine : lines) {
 				writer.write(theLine, 0, theLine.length());
 				writer.write(lineSep);
 			}
 
 			writer.flush();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Given a CalculatorCatalog, assembles a Properties object representing all
 	 * of the calculators contained in the catalog. The resulting Properties
 	 * object, if passed to the loadCalculators method, would reconstruct all
 	 * the calculators. This method works by getting each set of calculators
 	 * from the catalog and calling the getProperties method on each calculator
 	 * with the proper header for the property key.
 	 */
 	public static Properties getProperties(CalculatorCatalog catalog) {
 		final Properties newProps = new Properties();
 
 		for (Calculator c : catalog.getCalculators()) {
 			newProps.putAll(c.getProperties());
 		}
 
 		// visual styles
 		final Set<String> visualStyleNames = catalog.getVisualStyleNames();
 
 		VisualStyle vs;
 		Properties styleProps;
 
 		for (String name : visualStyleNames) {
 			vs = catalog.getVisualStyle(name);
 			styleProps = new Properties();
 
 			try {
 				styleProps.putAll(vs.getNodeAppearanceCalculator()
 				                    .getProperties(nodeAppearanceBaseKey + "." + name));
 				styleProps.putAll(vs.getEdgeAppearanceCalculator()
 				                    .getProperties(edgeAppearanceBaseKey + "." + name));
 				styleProps.putAll(vs.getGlobalAppearanceCalculator()
 				                    .getProperties(globalAppearanceBaseKey + "." + name));
 
 				// now that we've constructed all the properties for this visual
 				// style without Exceptions, store in the global properties
 				// object
 				newProps.putAll(styleProps);
 			} catch (Exception e) {
 				System.out.println("Exception while saving visual style " + name);
 				e.printStackTrace();
 			}
 		}
 
 		return newProps;
 	}
 
 	/**
 	 * Equivalent to loadCalculators(props, catalog, true);
 	 */
 	public static void loadCalculators(Properties props, CalculatorCatalog catalog) {
 		loadCalculators(props, catalog, true);
 	}
 
 	/**
 	 * Loads calculators from their description in a Properties object into a
 	 * supplied CalculatorCatalog object. This method searches the Properties
 	 * object for known keys identifying calculators, then delegates to other
 	 * methods that use the preprocessed properties to construct valid
 	 * calculator objects. For any calculator defined by the Properties, it is
 	 * possible for the catalog to already hold a calculator with the same name
 	 * and interface type (especially if this method has already been run with
 	 * the same Properties object and catalog). If the overWrite argument is
 	 * true, this method will remove any such duplicate calculator before adding
 	 * the new one to prevent duplicate name exceptions. If overwrite is false,
 	 * this method will get a unique name from the catalog and change the name
 	 * of the installed calculator as needed.
 	 */
 	public static void loadCalculators(Properties props, CalculatorCatalog catalog,
 	                                   boolean overWrite) {
 		// The supplied Properties object may contain any kinds of properties.
 		// We look for keys that start with a name we recognize, identifying a
 		// particular type of calculator. The second field of the key should
 		// then be an identifying name. For example,
 		// nodeFillColorCalculator.mySpecialCalculator.{anything else}
 		//
 		// We begin by creating a map of calculator types
 		// (nodeFillColorCalculator) to a map of names (mySpecialCalculator) to
 		// properties. Note that this will create maps for _any_ "calculator"
 		// that appears, even if it isn't a Calculator. This is OK, because the
 		// CalculatorFactory won't create anything that isn't actually a
 		// Calculator.
 		//
 		// Note that we need separate constructs for each type of calculator,
 		// because calculators of different types are allowed to share the same
 		// name.
 		final Map<String, Map<String, Properties>> calcNames = new HashMap<String, Map<String, Properties>>();
 
 		// use the propertyNames() method instead of the generic Map iterator,
 		// because the former method recognizes layered properties objects.
 		// see the Properties javadoc for details
 		String key;
 
 		for (Enumeration eI = props.propertyNames(); eI.hasMoreElements();) {
 			key = (String) eI.nextElement();
 
 			// handle legacy names In these cases the old calculator base key
 			// was applicable to more than one calculator. In the new system
 			// it's one key to one calculator, so we simply apply the old
 			// calculator to all of the new types of calculators that the old
 			// calculator mapped to.
 
 			// separate color into fill color and border color
 			if (key.startsWith(nodeColorBaseKey + ".")) {
 				key = updateLegacyKey(key, props, nodeColorBaseKey,
 				                      NODE_FILL_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeFillColorCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, NODE_FILL_COLOR.getPropertyLabel(),
 				                      NODE_BORDER_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeBorderColorCalculator");
 				storeKey(key, props, calcNames);
 
 				// separate size into uniform, width, and height 
 			} else if (key.startsWith(nodeSizeBaseKey + ".")) {
 				key = updateLegacyKey(key, props, nodeSizeBaseKey, NODE_SIZE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeUniformSizeCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, NODE_SIZE.getPropertyLabel(),
 				                      NODE_WIDTH.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeWidthCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, NODE_WIDTH.getPropertyLabel(),
 				                      NODE_HEIGHT.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeHeightCalculator");
 				storeKey(key, props, calcNames);
 
 				// separate arrow into source shape, source color, target shape, target color
 			} else if (key.startsWith(edgeArrowBaseKey + ".")) {
 				// the first two separations are to support the 
 				// deprecated EDGE_SRCARROW and EDGE_TGTARROW
 				key = updateLegacyKey(key, props, edgeArrowBaseKey,
 				                      EDGE_SRCARROW.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_SRCARROW.getPropertyLabel(),
 				                      EDGE_TGTARROW.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowCalculator");
 				storeKey(key, props, calcNames);
 
 				// eventually (4/2008), these should be the only separations
 				key = updateLegacyKey(key, props, EDGE_TGTARROW.getPropertyLabel(),
 				                      EDGE_SRCARROW_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowColorCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_SRCARROW_COLOR.getPropertyLabel(),
 				                      EDGE_SRCARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_SRCARROW_SHAPE.getPropertyLabel(),
 				                      EDGE_TGTARROW_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowColorCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_TGTARROW_COLOR.getPropertyLabel(),
 				                      EDGE_TGTARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 
 				// separated source arrow into source color and source shape
 			} else if (key.startsWith(EDGE_SRCARROW.getPropertyLabel() + ".")) {
 				// this first store is to support deprecated EDGE_SRCARROW
 				//storeKey(key, props, calcNames);
 
 				// eventually, these should be the only separations
 				key = updateLegacyKey(key, props, EDGE_SRCARROW.getPropertyLabel(),
 				                      EDGE_SRCARROW_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowColorCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_SRCARROW_COLOR.getPropertyLabel(),
 				                      EDGE_SRCARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowShapeCalculator");
 
 				storeKey(key, props, calcNames);
 
 				// separated target arrow into target color and target shape
 			} else if (key.startsWith(EDGE_TGTARROW.getPropertyLabel() + ".")) {
 				// this first store is to support deprecated EDGE_TGTARROW
 				//storeKey(key, props, calcNames);
 
 				// eventually, these should be the only separations
 				key = updateLegacyKey(key, props, EDGE_TGTARROW.getPropertyLabel(),
 				                      EDGE_TGTARROW_COLOR.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowColorCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_TGTARROW_COLOR.getPropertyLabel(),
 				                      EDGE_TGTARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowShapeCalculator");
 
 				storeKey(key, props, calcNames);
 
 				// handle normal names
 				// This is how all "modern" properties files should work.
 			} else if (key.startsWith(EDGE_LINETYPE.getPropertyLabel() + ".")) {
 				// This first store is to support deprecated EDGE_LINETYPE.
 				// This should be replaced with line style and line width.
 				//storeKey(key, props, calcNames);
 
 				// eventually, these should be the only separations
 				key = updateLegacyKey(key, props, EDGE_LINETYPE.getPropertyLabel(),
 				                      EDGE_LINE_STYLE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, EDGE_LINE_STYLE.getPropertyLabel(),
 				                      EDGE_LINE_WIDTH.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeLineWidthCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.startsWith(NODE_LINETYPE.getPropertyLabel() + ".")) {
 				// This first store is to support deprecated EDGE_LINETYPE.
 				// This should be replaced with line style and line width.
 				//storeKey(key, props, calcNames);
 
 				// eventually, these should be the only separations
 				key = updateLegacyKey(key, props, NODE_LINETYPE.getPropertyLabel(),
 				                      NODE_LINE_STYLE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 
 				key = updateLegacyKey(key, props, NODE_LINE_STYLE.getPropertyLabel(),
 				                      NODE_LINE_WIDTH.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeLineWidthCalculator");
 				storeKey(key, props, calcNames);
 
 			// These change the visual styles (rather than calculators) so that the
 			// visual style, instead of mapping a TGTARROW, now maps a TGTARROW_SHAPE.
 			} else if (key.endsWith(EDGE_TGTARROW.getPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_TGTARROW.getPropertyLabel(),
 				                      EDGE_TGTARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(EDGE_SRCARROW.getPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_SRCARROW.getPropertyLabel(),
 				                      EDGE_SRCARROW_SHAPE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(EDGE_LINETYPE.getPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_LINETYPE.getPropertyLabel(),
 				                      EDGE_LINE_STYLE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(NODE_LINETYPE.getPropertyLabel())) {
 				key = updateLegacyKey(key, props, NODE_LINETYPE.getPropertyLabel(),
 				                      NODE_LINE_STYLE.getPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 
 			// Likewise, these change the default values of visual styles to so that
 			// the default LINETYPE gets turned into the default LINE_STYLE.
 			} else if (key.endsWith(NODE_LINETYPE.getDefaultPropertyLabel())) {
 				key = updateLegacyKey(key, props, NODE_LINETYPE.getDefaultPropertyLabel(),
 				                      NODE_LINE_STYLE.getDefaultPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericNodeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(EDGE_LINETYPE.getDefaultPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_LINETYPE.getDefaultPropertyLabel(),
 				                      EDGE_LINE_STYLE.getDefaultPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeLineStyleCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(EDGE_TGTARROW.getDefaultPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_TGTARROW.getDefaultPropertyLabel(),
 				                      EDGE_TGTARROW_SHAPE.getDefaultPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeTargetArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 			} else if (key.endsWith(EDGE_SRCARROW.getDefaultPropertyLabel())) {
 				key = updateLegacyKey(key, props, EDGE_SRCARROW.getDefaultPropertyLabel(),
 				                      EDGE_SRCARROW_SHAPE.getDefaultPropertyLabel(),
 				                      "cytoscape.visual.calculators.GenericEdgeSourceArrowShapeCalculator");
 				storeKey(key, props, calcNames);
 
 			// Store the key as is.
 			} else
 				storeKey(key, props, calcNames);
 		}
 
 		// Now that we have all the properties in groups, we pass each Map of
 		// names and Properties objects to a helper function that creates a
 		// calculator for each entry and stores the calculators in the catalog.
 		// Before storing the calculator, we either remove any existing
 		// calculator with the same name, or get a unique name from the
 		// calculator, depending on the value of the overWrite argument.
 		for (String calcTypeKey : calcNames.keySet())
 			handleCalculators(calcNames.get(calcTypeKey), catalog, overWrite, calcTypeKey);
 
 		// Map structure to hold visual styles that we build here
 		final Map<String, VisualStyle> visualStyles = new HashMap<String, VisualStyle>();
 
 		// now that all the individual calculators are loaded, load the
 		// Node/Edge/Global appearance calculators
 		final Map<String, Properties> nacNames = calcNames.get(nodeAppearanceBaseKey);
 
 		VisualStyle vs;
 
 		for (String name : nacNames.keySet()) {
 			// store in the matching visual style, creating as needed
 			vs = visualStyles.get(name);
 
 			if (vs == null) {
 				vs = new VisualStyle(name);
 				visualStyles.put(name, vs);
 			}
 
 			vs.setNodeAppearanceCalculator(new NodeAppearanceCalculator(name, nacNames.get(name),
 			                                                            nodeAppearanceBaseKey + "."
 			                                                            + name, catalog));
 		}
 
 		Map<String, Properties> eacNames = calcNames.get(edgeAppearanceBaseKey);
 
 		for (String name : eacNames.keySet()) {
 			// store in the matching visual style, creating as needed
 			vs = visualStyles.get(name);
 
 			if (vs == null) {
 				vs = new VisualStyle(name);
 				visualStyles.put(name, vs);
 			}
 
 			vs.setEdgeAppearanceCalculator(new EdgeAppearanceCalculator(name, eacNames.get(name),
 			                                                            edgeAppearanceBaseKey + "."
 			                                                            + name, catalog));
 		}
 
 		Map<String, Properties> gacNames = calcNames.get(globalAppearanceBaseKey);
 
 		for (String name : gacNames.keySet()) {
 			// store in the matching visual style, creating as needed
 			vs = visualStyles.get(name);
 
 			if (vs == null) {
 				vs = new VisualStyle(name);
 				visualStyles.put(name, vs);
 			}
 
 			vs.setGlobalAppearanceCalculator(new GlobalAppearanceCalculator(name,
 			                                                                gacNames.get(name),
 			                                                                globalAppearanceBaseKey
 			                                                                + "." + name, catalog));
 		}
 
 		// now store the visual styles in the catalog
 		for (VisualStyle visualStyle : visualStyles.values())
 			catalog.addVisualStyle(visualStyle);
 	}
 
 	/**
 	 * The supplied Map m maps calculator types to a map of names to Properties
 	 * objects that hold all the properties entries associated with that name.
 	 * Given a new key, this method first extract the calculator type, and then
 	 * finds the name to prop map for that calc type. It then extracts the name
 	 * field from the key, gets the matching Properties object from the name to
 	 * props map (creating a new map entry if needed) and stores the (key,
 	 * value) property pair in that Properties object).
 	 */
 	private static void storeKey(String key, Properties props,
 	                             Map<String, Map<String, Properties>> calcNames) {
 		// get the name->props map for the given calculator type, as
 		// defined by the key.
 		final String calcTypeKey = extractCalcType(key);
 
 		if (calcTypeKey == null) {
 			System.err.println("couldn't parse calcTypeKey from '" + key + "'");
 
 			return;
 		}
 
 		Map<String, Properties> name2props = calcNames.get(calcTypeKey);
 
 		// if the props don't yet exist, create them
 		if (name2props == null) {
 			name2props = new HashMap<String, Properties>();
 			calcNames.put(calcTypeKey, name2props);
 		}
 
 		// now the get the props from the name->props map
 		final String name = extractName(key);
 
 		if (name != null) {
 			// calcProps contains all of the properties for this calculator,
 			// e.g. the mappings, the controller, etc.
 			Properties calcProps = name2props.get(name);
 
 			// create a new entry for this name if it doesn't already exist
 			if (calcProps == null) {
 				calcProps = new Properties();
 				name2props.put(name, calcProps);
 			}
 
 			calcProps.setProperty(key, props.getProperty(key));
 		} // should report parse errors if we can't get a name
 	}
 
 	/**
 	 * Given the key of a property entry, extract the second field (i.e.,
 	 * between the first and second period) and return it.
 	 */
 	private static String extractName(final String key) {
 		if (key == null)
 			return null;
 
 		// find index of first period character
 		final int dot1 = key.indexOf(".");
 
 		// return null if not found, or found at end of string
 		if ((dot1 == -1) || (dot1 >= (key.length() - 1)))
 			return null;
 
 		// find the second period character
 		final int dot2 = key.indexOf(".", dot1 + 1);
 
 		if (dot2 == -1) {
 			return null;
 		} // return null if not found
 		  // return substring between the periods
 
 		return key.substring(dot1 + 1, dot2);
 	}
 
 	/**
 	 * Extracts the base key from the string.
 	 */
 	private static String extractCalcType(final String key) {
 		if (key == null)
 			return null;
 
 		// find index of first period character
 		final int dot1 = key.indexOf(".");
 
 		// return null if not found, or found at end of string
 		if ((dot1 == -1) || (dot1 >= (key.length() - 1)))
 			return null;
 
 		// return substring between the periods
 		return key.substring(0, dot1);
 	}
 
 	/**
 	 * Construct and store Calculators. Ensures that there will be no name
 	 * collision by either removing an existing duplicate or renaming the new
 	 * calculator as needed.
 	 */
 	private static void handleCalculators(Map<String, Properties> nameMap,
 	                                      CalculatorCatalog catalog, boolean overWrite,
 	                                      String calcTypeKey) {
 		// for each calculator name
 		for (String name : nameMap.keySet()) {
 			// get the properties object that contains all info for
 			// that particular calculator
 
 			// create a calculator based on the calculator name and type
 			final Calculator c = CalculatorFactory.newCalculator(name, nameMap.get(name),
 			                                                     calcTypeKey + "." + name);
 
 			if (c != null) {
 				// remove any existing calculator of same name and type
 				if (overWrite) {
 					catalog.removeCalculator(c);
 
 					// otherwise ensure a unique name
 				} else
 					renameAsNeeded(c, catalog);
 
 				catalog.addCalculator(c);
 			}
 		}
 	}
 
 	/**
 	 * Given a Calculator of a given type and a CalculatorCatalog, checks for an
 	 * existing catalog with the same name and type. If one exists, gets a new
 	 * unique name from the catalog and applied it to the calculator argument.
 	 */
 	public static void renameAsNeeded(Calculator c, CalculatorCatalog catalog) {
 		final String name = c.toString();
 		final String newName = catalog.checkCalculatorName(c.getVisualPropertyType(), name);
 
 		if (!newName.equals(name))
 			c.setName(newName);
 	}
 
 	/**
 	 * Used for updating calculator names from old style to new style. Only used
 	 * in a few cases where the old and new don't align.
 	 */
 	private static String updateLegacyKey(String key, Properties props, String oldKey,
 	                                      String newKey, String newClass) {
 		String value = props.getProperty(key);
 
 		// Update arrow
 		if ((key.endsWith("equal") || key.endsWith("greater") || key.endsWith("lesser"))
 		    && (key.startsWith(EDGE_TGTARROW.getPropertyLabel() + ".")
 		       || key.startsWith(EDGE_SRCARROW.getPropertyLabel() + "."))) {
 			value = Arrow.parseArrowText(value).getShape().toString();
 		}
 
 		if (key.endsWith(EDGE_TGTARROW.getDefaultPropertyLabel())
 		    || key.endsWith(EDGE_SRCARROW.getDefaultPropertyLabel())) {
 			value = Arrow.parseArrowText(value).getShape().toString();
 		}
 
 		key = key.replace(oldKey, newKey);
 
 		if (key.endsWith(".class"))
 			props.setProperty(key, newClass);
 		else
 			props.setProperty(key, value);
 
 		return key;
 	}
 }
