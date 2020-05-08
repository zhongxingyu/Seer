 // VisualizationPreferencePage.java
 package org.eclipse.stem.ui.preferences;
 
 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.jface.resource.StringConverter;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * This class represents the preferences for visualization of a simulation.
  */
 public class VisualizationPreferencePage extends FieldEditorPreferencePage
 		implements IWorkbenchPreferencePage {
 
 	/**
 	 * The ID of the STEM Preference page.
 	 */
 	public static final String ID_STEM_VISUALIZATION_PAGE = "org.eclipse.stem.ui.preferences.VisualizationPreferencePage";
 
 	/**
 	 * The default name of the attribute to display first in a visualization.
 	 */
 	public static final String DEFAULT_INITIAL_ATTRIBUTE_NAME_STRING = "I";
 
 	/**
 	 * The default name of the attribute to display first in a visualization.
 	 */
 	public static final String DEFAULT_INITIAL_ATTRIBUTE_NAME_STRING2 = "S";
 
 	
 	private static final int ATTRIBUTE_NAME_FIELD_WIDTH = 11;
 
 	/**
 	 * A mapping between attribute names and attribute colors is maintained in
 	 * this color preference map.
 	 */
 	private static Map<String, Color> colorPreferenceMap = new HashMap<String, Color>();
 
 	/**
 	 * Define several label names (strings) that might need unique foreground
 	 * color assignments.
 	 * 
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_DEFAULT_STRING = "DEFAULT";
 
 	/**
 	 * Susceptible
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_1_STRING = "S";
 	/**
 	 * Exposed
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_2_STRING = "E";
 	/**
 	 * Infectious
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_3_STRING = "I";
 	/**
 	 * Recovered
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_4_STRING = "R";
 	/**
 	 * Total Population
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_5_STRING = "Count";
 	
 	/**
 	 * Population Density
 	 */
 	public static final String FOREGROUND_ATTRIBUTE_NAME_6_STRING = "Density";
 	/**
 	 * Infectious that Die of the disease
 	 */
	public static final String FOREGROUND_ATTRIBUTE_NAME_7_STRING = "IF";
 
 	/**
 	 * Default color 
 	 */
 	public static final String FOREGROUND_COLOR_DEFAULT_RGB_STRING = "0,0,172";
 
 	/**
 	 * S color = blue
 	 */
 	public static final String FOREGROUND_COLOR_1_RGB_STRING = "0,0,255";
 
 	/**
 	 * E color = yellow
 	 */
 	public static final String FOREGROUND_COLOR_2_RGB_STRING = "255,255,0";
 
 	/**
 	 * I color = red
 	 */
 	public static final String FOREGROUND_COLOR_3_RGB_STRING = "255,0,0";
 
 	/**
 	 * R color = green
 	 */
 	public static final String FOREGROUND_COLOR_4_RGB_STRING = "0,255,0";
 
 	/**
 	 * P (Population) color = white
 	 */
 	public static final String FOREGROUND_COLOR_5_RGB_STRING = "255,255,255";
 	
 
 	/**
 	 * Density (Cyan)
 	 */
 	public static final String FOREGROUND_COLOR_6_RGB_STRING = "0,255,255";
 	
 
 	/**
	 * IF (Infectious that Die) color = magenta
 	 */
	public static final String FOREGROUND_COLOR_7_RGB_STRING = "255,0,255";
 
 	/**
 	 * define several primary foreground colors each can be associated with a different label
 	 * (e.g., S,E,I,R, P, IR, IF) If the Label is not found, use the default foreground
 	 * color field editor
 	 */
 	private static ColorFieldEditor foregroundColorFieldEditorDefault;
 	private static ColorFieldEditor foregroundColorFieldEditor1;
 	private static ColorFieldEditor foregroundColorFieldEditor2;
 	private static ColorFieldEditor foregroundColorFieldEditor3;
 	private static ColorFieldEditor foregroundColorFieldEditor4;
 	private static ColorFieldEditor foregroundColorFieldEditor5;
 	private static ColorFieldEditor foregroundColorFieldEditor6;
 	private static ColorFieldEditor foregroundColorFieldEditor7;
 
 	/**
 	 * Define several string field editors to assign string values to the labels
 	 * that require color assignments
 	 */
 	// private StringFieldEditor foregroundLabelFieldEditorDefault;
 	private static StringFieldEditor foregroundLabelFieldEditor1;
 	private static StringFieldEditor foregroundLabelFieldEditor2;
 	private static StringFieldEditor foregroundLabelFieldEditor3;
 	private static StringFieldEditor foregroundLabelFieldEditor4;
 	private static StringFieldEditor foregroundLabelFieldEditor5;
 	private static StringFieldEditor foregroundLabelFieldEditor6;
 	private static StringFieldEditor foregroundLabelFieldEditor7;
 
 	private StringFieldEditor initialAttributeNameFieldEditor1;
 	private StringFieldEditor initialAttributeNameFieldEditor2;
 
 	/**
 	 * Constructor
 	 */
 	public VisualizationPreferencePage() {
 		super(GRID);
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 		setDescription(Messages.getString("STEMVPage.Viz"));
 
 	} // VisualizationPreferencePage
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
 	 */
 	@Override
 	protected void createFieldEditors() {
 		initialAttributeNameFieldEditor1 = new StringFieldEditor(
 				PreferenceConstants.INITIAL_ATTRIBUTE_NAME_STRING_PREFERENCE,
 				Messages.getString("VizPPage.InitialName1"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 
 		initialAttributeNameFieldEditor2 = new StringFieldEditor(
 				PreferenceConstants.INITIAL_ATTRIBUTE_NAME_STRING_PREFERENCE2,
 				Messages.getString("VizPPage.InitialName2"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 
 		// For Color Assignment
 		foregroundColorFieldEditorDefault = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_DEFAULT, Messages
 						.getString("VizPPage.fgcd"), getFieldEditorParent());
 		foregroundColorFieldEditor1 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_1, Messages
 						.getString("VizPPage.fgc1"), getFieldEditorParent());
 		foregroundColorFieldEditor2 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_2, Messages
 						.getString("VizPPage.fgc2"), getFieldEditorParent());
 		foregroundColorFieldEditor3 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_3, Messages
 						.getString("VizPPage.fgc3"), getFieldEditorParent());
 		foregroundColorFieldEditor4 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_4, Messages
 						.getString("VizPPage.fgc4"), getFieldEditorParent());
 		foregroundColorFieldEditor5 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_5, Messages
 						.getString("VizPPage.fgc5"), getFieldEditorParent());
 		foregroundColorFieldEditor6 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_6, Messages
 						.getString("VizPPage.fgc6"), getFieldEditorParent());
 		foregroundColorFieldEditor7 = new ColorFieldEditor(
 				PreferenceConstants.FOREGROUND_COLOR_LABEL_7, Messages
 						.getString("VizPPage.fgc7"), getFieldEditorParent());
 
 		// Do NOT add the label field editor for name DEFAULT
 		// This must be hard coded and not exposed
 		/*
 		 * foregroundLabelFieldEditorDefault = new StringFieldEditor(
 		 * PreferenceConstants.FOREGROUND_STRING_LABEL_DEFAULT,
 		 * Messages.getString("VizPPage.BGdefaultName"),
 		 * ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		 */
 		// For label name BG1Name = S (see messages.properties)
 		foregroundLabelFieldEditor1 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_1, Messages
 						.getString("VizPPage.BG1Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG2Name = E (see messages.properties)
 		foregroundLabelFieldEditor2 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_2, Messages
 						.getString("VizPPage.BG2Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG3Name = I (see messages.properties)
 		foregroundLabelFieldEditor3 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_3, Messages
 						.getString("VizPPage.BG3Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG4Name = R (see messages.properties)
 		foregroundLabelFieldEditor4 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_4, Messages
 						.getString("VizPPage.BG4Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG5Name = P (see messages.properties)
 		foregroundLabelFieldEditor5 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_5, Messages
 						.getString("VizPPage.BG5Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG6Name = IR (see messages.properties)
 		foregroundLabelFieldEditor6 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_6, Messages
 						.getString("VizPPage.BG6Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 		// For label name BG7Name = IF (see messages.properties)
 		foregroundLabelFieldEditor7 = new StringFieldEditor(
 				PreferenceConstants.FOREGROUND_STRING_LABEL_7, Messages
 						.getString("VizPPage.BG7Name"),
 				ATTRIBUTE_NAME_FIELD_WIDTH, getFieldEditorParent());
 
 		// add all components
 		addField(initialAttributeNameFieldEditor1);
 		addField(initialAttributeNameFieldEditor2);
 		// Add the color field editors
 		addField(foregroundColorFieldEditorDefault);
 		// Do NOT add the label field editor for name DEFAULT
 		// This must be hard coded and not exposed
 		// addField(foregroundLabelFieldEditorDefault);
 
 		addField(foregroundColorFieldEditor1);
 		addField(foregroundLabelFieldEditor1);
 
 		addField(foregroundColorFieldEditor2);
 		addField(foregroundLabelFieldEditor2);
 
 		addField(foregroundColorFieldEditor3);
 		addField(foregroundLabelFieldEditor3);
 
 		addField(foregroundColorFieldEditor4);
 		addField(foregroundLabelFieldEditor4);
 
 		addField(foregroundColorFieldEditor5);
 		addField(foregroundLabelFieldEditor5);
 
 
 		addField(foregroundColorFieldEditor6);
 		addField(foregroundLabelFieldEditor6);
 
 
 		addField(foregroundColorFieldEditor7);
 		addField(foregroundLabelFieldEditor7);
 
 	}// createFieldEditors
 
 	/**
 	 * @return the mapping between the display name of an attribute and the
 	 *         color to be used to display its value.
 	 */
 	public static Map<String, Color> getColorMapping() {
 
 		//
 		final Preferences preferences = Activator.getDefault()
 				.getPluginPreferences();
 
 		Device device = Activator.getDefault().getWorkbench().getDisplay();
 		
 		// Add the default color (always the same key)
 		final RGB defaultRGB = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_DEFAULT));
 		
 		final Color colorDef = new Color(device, defaultRGB.red,
 				defaultRGB.green, defaultRGB.blue);
 		colorPreferenceMap.put(FOREGROUND_ATTRIBUTE_NAME_DEFAULT_STRING,
 				colorDef);
 
 		// Add the attribute labels as keys and the preferred colors
 		final RGB rgb1 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_1));
 		final Color color1 = new Color(device, rgb1.red, rgb1.green,
 				rgb1.blue);
 
 		final RGB rgb2 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_2));
 		final Color color2 = new Color(device, rgb2.red, rgb2.green,
 				rgb2.blue);
 
 		final RGB rgb3 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_3));
 		final Color color3 = new Color(device, rgb3.red, rgb3.green,
 				rgb3.blue);
 
 		final RGB rgb4 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_4));
 		final Color color4 = new Color(device, rgb4.red, rgb4.green,
 				rgb4.blue);
 
 		final RGB rgb5 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_5));
 		final Color color5 = new Color(device, rgb5.red, rgb5.green,
 				rgb5.blue);
 	
 		final RGB rgb6 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_6));
 		final Color color6 = new Color(device, rgb6.red, rgb6.green,
 				rgb6.blue);
 
 		final RGB rgb7 = StringConverter.asRGB(preferences
 				.getString(PreferenceConstants.FOREGROUND_COLOR_LABEL_7));
 		final Color color7 = new Color(device, rgb7.red, rgb7.green,
 				rgb7.blue);
 		
 
 
 		final String key1 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_1);
 		final String key2 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_2);
 		final String key3 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_3);
 		final String key4 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_4);
 		final String key5 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_5);
 		final String key6 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_6);
 		final String key7 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_7);
 
 		colorPreferenceMap.put(key1, color1);
 		colorPreferenceMap.put(key2, color2);
 		colorPreferenceMap.put(key3, color3);
 		colorPreferenceMap.put(key4, color4);
 		colorPreferenceMap.put(key5, color5);
 		colorPreferenceMap.put(key6, color6);
 		colorPreferenceMap.put(key7, color7);
 		
 		return colorPreferenceMap;
 	} // getColorMapping
 	
 	/**
 	 * @return a set of Strings (labels) for which the user has
 	 * defined color preferences.
 	 */
 	public static Set<String> getPreferenceSet() {
 		
 		HashSet<String> prefSet = new HashSet<String>();
 		final Preferences preferences = Activator.getDefault()
 				.getPluginPreferences();
 		final String key1 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_1);
 		final String key2 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_2);
 		final String key3 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_3);
 		final String key4 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_4);
 		final String key5 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_5);
 		final String key6 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_6);
 		final String key7 = preferences
 				.getString(PreferenceConstants.FOREGROUND_STRING_LABEL_7);
 
 		prefSet.add(key1);
 		prefSet.add(key2);
 		prefSet.add(key3);
 		prefSet.add(key4);
 		prefSet.add(key5);
 		prefSet.add(key6);
 		prefSet.add(key7);
 		
 		return prefSet;
 	}// get prefSet
 
 	/**
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(@SuppressWarnings("unused")
 	final IWorkbench workbench) {
 		// Nothing
 	} // init
 
 } // VisualizationPreferencePage
