 /*
  File: GlobalAppearanceCalculator.java
 
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
 
 import cytoscape.CyNetwork;
 
 import cytoscape.visual.parsers.ColorParser;
 import cytoscape.visual.parsers.ObjectToString;
 
 //----------------------------------------------------------------------------
 import java.awt.Color;
 
 import java.lang.reflect.Method;
 
 import java.util.Properties;
 
 
 //----------------------------------------------------------------------------
 /**
  * This class calculates global visual attributes such as the background color
  * of the graph window. Currently dynamic calculators for these values are not
  * supported, only default values.
  */
 public class GlobalAppearanceCalculator extends SubjectBase implements Cloneable {
 	/*
 	 * Set default colors
 	 */
 	private Color defaultBackgroundColor = Color.WHITE;
 	@Deprecated
 	private Color defaultSloppySelectionColor = Color.GRAY;
 	private Color defaultNodeSelectionColor = Color.YELLOW;
 	private Color defaultNodeReverseSelectionColor = Color.GREEN;
 	private Color defaultEdgeSelectionColor = Color.RED;
 	private Color defaultEdgeReverseSelectionColor = Color.GREEN;
 
 	/**
 	 * Make shallow copy of this object
 	 */
 	public Object clone() throws CloneNotSupportedException {
 		Object copy = null;
 		copy = super.clone();
 
 		return copy;
 	}
 
 	/**
 	 * Creates a new GlobalAppearanceCalculator object.
 	 */
 	public GlobalAppearanceCalculator() {
 	}
 
 	/**
 	 *  Get default color for the given parameter name.
 	 * Should be used names in GlobalAppearenceNames enum.
 	 *
 	 * @param name DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public Color getDefaultColor(final String name) throws Exception {
 		final Class cls = this.getClass();
 		final String newName = name.replace(" ", "");
		final Method method = cls.getMethod("getDefault" + newName, new Class[] {});
		final Object obj = method.invoke(this, new Object[] {});
 
 		if ((obj != null) && obj instanceof Color) {
 			return (Color) obj;
 		} else
 
 			return null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param name DOCUMENT ME!
 	 * @param newColor DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void setDefaultColor(final String name, final Color newColor) throws Exception {
 		final Class cls = this.getClass();
 		final String newName = name.replace(" ", "");
 		final Method method = cls.getMethod("setDefault" + newName, new Class[] { Color.class });
 		method.invoke(this, new Object[] { newColor });
 	}
 
 	/**
 	 *  Get name of global appearances.
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static String[] getGlobalAppearanceNames() {
 		return GlobalAppearance.getCalculatorNames();
 	}
 
 	/**
 	 * Copy constructor. Returns a default object if the argument is null.
 	 */
 	public GlobalAppearanceCalculator(GlobalAppearanceCalculator toCopy) {
 		if (toCopy == null) {
 			return;
 		}
 
 		setDefaultBackgroundColor(toCopy.getDefaultBackgroundColor());
 		setDefaultSloppySelectionColor(toCopy.getDefaultSloppySelectionColor());
 		setDefaultNodeSelectionColor(toCopy.getDefaultNodeSelectionColor());
 		setDefaultNodeReverseSelectionColor(toCopy.getDefaultNodeReverseSelectionColor());
 		setDefaultEdgeSelectionColor(toCopy.getDefaultEdgeSelectionColor());
 		setDefaultEdgeReverseSelectionColor(toCopy.getDefaultEdgeReverseSelectionColor());
 	}
 
 	/**
 	 * Creates a new GlobalAppearanceCalculator and immediately customizes it by
 	 * calling applyProperties with the supplied arguments.
 	 */
 	public GlobalAppearanceCalculator(String name, Properties gProps, String baseKey,
 	                                  CalculatorCatalog catalog) {
 		applyProperties(name, gProps, baseKey, catalog);
 	}
 
 	/**
 	 * Constructs a new GlobalAppearance object containing the values for the
 	 * known global visual attributes.
 	 */
 	public GlobalAppearance calculateGlobalAppearance(CyNetwork network) {
 		GlobalAppearance appr = new GlobalAppearance();
 		calculateGlobalAppearance(appr, network);
 
 		return appr;
 	}
 
 	/**
 	 * The supplied GlobalAppearance object will be changed to hold new values
 	 * for the known global visual attributes.
 	 */
 	public void calculateGlobalAppearance(GlobalAppearance appr, CyNetwork network) {
 		appr.setBackgroundColor(calculateBackgroundColor(network));
 		appr.setSloppySelectionColor(calculateSloppySelectionColor(network));
 		appr.setNodeSelectionColor(calculateNodeSelectionColor(network));
 		appr.setNodeReverseSelectionColor(calculateNodeReverseSelectionColor(network));
 		appr.setEdgeSelectionColor(calculateEdgeSelectionColor(network));
 		appr.setEdgeReverseSelectionColor(calculateEdgeReverseSelectionColor(network));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color getDefaultBackgroundColor() {
 		return defaultBackgroundColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	public void setDefaultBackgroundColor(Color c) {
 		if (c != null) {
 			defaultBackgroundColor = c;
 			this.fireStateChanged();
 		}
 	}
 
 	/**
 	 * Currently no calculators are supported for global visual attributes, so
 	 * this method simply returns the default background color.
 	 */
 	public Color calculateBackgroundColor(CyNetwork network) {
 		return defaultBackgroundColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	@Deprecated
 	public Color getDefaultSloppySelectionColor() {
 		return defaultSloppySelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	@Deprecated
 	public void setDefaultSloppySelectionColor(Color c) {
 		if (c != null) {
 			this.fireStateChanged();
 			defaultSloppySelectionColor = c;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color getDefaultNodeSelectionColor() {
 		return defaultNodeSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	public void setDefaultNodeSelectionColor(Color c) {
 		if (c != null) {
 			defaultNodeSelectionColor = c;
 			this.fireStateChanged();
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color getDefaultNodeReverseSelectionColor() {
 		return defaultNodeReverseSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	public void setDefaultNodeReverseSelectionColor(Color c) {
 		if (c != null) {
 			defaultNodeReverseSelectionColor = c;
 			this.fireStateChanged();
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color getDefaultEdgeSelectionColor() {
 		return defaultEdgeSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	public void setDefaultEdgeSelectionColor(Color c) {
 		if (c != null) {
 			defaultEdgeSelectionColor = c;
 			this.fireStateChanged();
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color getDefaultEdgeReverseSelectionColor() {
 		return defaultEdgeReverseSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 */
 	public void setDefaultEdgeReverseSelectionColor(Color c) {
 		if (c != null) {
 			defaultEdgeReverseSelectionColor = c;
 			this.fireStateChanged();
 		}
 	}
 
 	/**
 	 * Currently no calculators are supported for global visual attributes, so
 	 * this method simply returns the default sloppy selection color.
 	 */
 	@Deprecated
 	public Color calculateSloppySelectionColor(CyNetwork network) {
 		return defaultSloppySelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color calculateNodeSelectionColor(CyNetwork network) {
 		return defaultNodeSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color calculateNodeReverseSelectionColor(CyNetwork network) {
 		return defaultNodeReverseSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color calculateEdgeSelectionColor(CyNetwork network) {
 		return defaultEdgeSelectionColor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Color calculateEdgeReverseSelectionColor(CyNetwork network) {
 		return defaultEdgeReverseSelectionColor;
 	}
 
 	/**
 	 * Returns a text description of this object's current state.
 	 */
 	public String getDescription() {
 		String lineSep = System.getProperty("line.separator");
 		StringBuffer sb = new StringBuffer();
 		sb.append("GlobalAppearanceCalculator:" + lineSep);
 		sb.append("defaultBackgroundColor = ");
 		sb.append(defaultBackgroundColor).append(lineSep);
 		sb.append("defaultSloppySelectionColor = ");
 		sb.append(defaultSloppySelectionColor).append(lineSep);
 
 		return sb.toString();
 	}
 
 	/**
 	 * This method customizes this object by searching the supplied properties
 	 * object for keys identifying default values and calculators. Recognized
 	 * keys are of the form "globalAppearanceCalculator." + name + ident, where
 	 * name is a supplied argument and ident is a String indicating a default
 	 * value for a specific visual attribute. Since calculators are not
 	 * supported for global visual attributes, the catalog argument is currently
 	 * ignored.
 	 */
 	public void applyProperties(String name, Properties nacProps, String baseKey,
 	                            CalculatorCatalog catalog) {
 		String value = null;
 
 		// look for default values
 		value = nacProps.getProperty(baseKey + ".defaultBackgroundColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultBackgroundColor(c);
 			}
 		}
 
 		value = nacProps.getProperty(baseKey + ".defaultSloppySelectionColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultSloppySelectionColor(c);
 			}
 		}
 
 		value = nacProps.getProperty(baseKey + ".defaultNodeSelectionColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultNodeSelectionColor(c);
 			}
 		}
 
 		value = nacProps.getProperty(baseKey + ".defaultNodeReverseSelectionColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultNodeReverseSelectionColor(c);
 			}
 		}
 
 		value = nacProps.getProperty(baseKey + ".defaultEdgeSelectionColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultEdgeSelectionColor(c);
 			}
 		}
 
 		value = nacProps.getProperty(baseKey + ".defaultEdgeReverseSelectionColor");
 
 		if (value != null) {
 			Color c = (new ColorParser()).parseColor(value);
 
 			if (c != null) {
 				setDefaultEdgeReverseSelectionColor(c);
 			}
 		}
 	}
 
 	/**
 	 * Returns a Properties description of this object, suitable for
 	 * customization by the applyProperties method.
 	 */
 	public Properties getProperties(String baseKey) {
 		String key = null;
 		String value = null;
 		Properties newProps = new Properties();
 
 		// save default values
 		key = baseKey + ".defaultBackgroundColor";
 		value = ObjectToString.getStringValue(getDefaultBackgroundColor());
 		newProps.setProperty(key, value);
 
 		key = baseKey + ".defaultSloppySelectionColor";
 		value = ObjectToString.getStringValue(getDefaultSloppySelectionColor());
 		newProps.setProperty(key, value);
 
 		key = baseKey + ".defaultNodeSelectionColor";
 		value = ObjectToString.getStringValue(getDefaultNodeSelectionColor());
 		newProps.setProperty(key, value);
 
 		key = baseKey + ".defaultNodeReverseSelectionColor";
 		value = ObjectToString.getStringValue(getDefaultNodeReverseSelectionColor());
 		newProps.setProperty(key, value);
 
 		key = baseKey + ".defaultEdgeSelectionColor";
 		value = ObjectToString.getStringValue(getDefaultEdgeSelectionColor());
 		newProps.setProperty(key, value);
 
 		key = baseKey + ".defaultEdgeReverseSelectionColor";
 		value = ObjectToString.getStringValue(getDefaultEdgeReverseSelectionColor());
 		newProps.setProperty(key, value);
 
 		return newProps;
 	}
 }
