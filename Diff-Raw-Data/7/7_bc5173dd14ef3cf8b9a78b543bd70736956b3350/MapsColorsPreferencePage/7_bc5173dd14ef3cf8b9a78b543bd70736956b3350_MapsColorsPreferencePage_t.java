 // MapsColorsPreferencePage.java
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
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.resource.StringConverter;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.stem.ui.Activator;
import org.eclipse.stem.ui.adapters.color.ColorProvider;
 import org.eclipse.stem.ui.adapters.color.IPreferencesContributer;
 import org.eclipse.stem.ui.adapters.color.RelativeValueColorPreferences;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * This class is a preference page for Maps Colors. It holds common maps colors
  * preferences and an extension point where other preferences contributers (
  * {@link org.eclipse.stem.ui.adapters.color.ColorProvider}s) can add their own
  * preferences.
  */
 public class MapsColorsPreferencePage extends FieldEditorPreferencePage
 		implements IWorkbenchPreferencePage {
 
 	/**
 	 * ID for the this preference page
 	 */
 	public static final String ID_STEM_MAPS_COLORS_PAGE = "org.eclipse.stem.ui.preferences.MapsColorsPreferencePage";
 	/**
 	 * ID for the foreground color field
 	 */
 	public static final String FOREGROUND_COLOR_ID = "org.eclipse.stem.ui.views.geographic.map.preferences.foregroundcolor";
 	/**
 	 * ID for the background color field
 	 */
 	public static final String BACKGROUND_COLOR_ID = "org.eclipse.stem.ui.views.geographic.map.preferences.backgroundcolor";
 	/**
 	 * ID for the borders color field
 	 */
 	public static final String BORDERS_COLOR_ID = "org.eclipse.stem.ui.views.geographic.map.preferences.borderscolor";
 	/**
 	 * ID for the edges color field
 	 */
 	public static final String EDGES_COLOR_ID = "org.eclipse.stem.ui.views.geographic.map.preferences.edgescolor";
 	/**
 	 * Default foreground color
 	 */
 	public static final String FOREGROUND_COLOR_DEFAULT_RGB_STRING = "255,0,0"; // Default
 	// is
 	// Red
 	/**
 	 * Default background color
 	 */
 	public static final String BACKGROUND_COLOR_DEFAULT_RGB_STRING = "0,0,0"; // Default
 	// is
 	// Black
 	/**
 	 * Default borders color
 	 */
	public static final String BORDERS_COLOR_DEFAULT_RGB_STRING = "0,80,80"; // Default
 	// is
	// Dark blue green
 	/**
 	 * Default borders color
 	 */
 	public static final String EDGES_COLOR_DEFAULT_RGB_STRING = "255,0,255"; // Default
 	// is
 	// Magenta
 	/**
 	 * The color field editor for the background color
 	 */
 	private static ColorFieldEditor backgroundColorFieldEditor;
 	/**
 	 * The color field editor for the foreground color
 	 */
 	private static ColorFieldEditor foregroundColorFieldEditor;
 	/**
 	 * The color field editor for the borders color
 	 */
 	private static ColorFieldEditor bordersColorFieldEditor;
 	/**
 	 * The color field editor for the edges color
 	 */
 	private static ColorFieldEditor edgesColorFieldEditor;
 	/**
 	 * The list of {@link IPreferencesContributer}s
 	 */
 	private final List<IPreferencesContributer> preferencesExtenders = new CopyOnWriteArrayList<IPreferencesContributer>();
 
 	/**
 	 * Constructor
 	 */
 	public MapsColorsPreferencePage() {
 		super(GRID);
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 		setDescription("Choose the colors to be used for drawing maps");
 
 		// TODO support extension-points mechanism
 		final IPreferencesContributer preferencesExtender = new RelativeValueColorPreferences();
 		preferencesExtender.setPreferencePage(this);
 
 		preferencesExtenders.add(preferencesExtender);
 	} // MapsColorsPreferencePage
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
 	 */
 	@Override
 	protected void createFieldEditors() {
 		backgroundColorFieldEditor = new ColorFieldEditor(BACKGROUND_COLOR_ID,
 				"Background Color", getFieldEditorParent());
 		foregroundColorFieldEditor = new ColorFieldEditor(FOREGROUND_COLOR_ID,
 				"Foreground Color", getFieldEditorParent());
 		bordersColorFieldEditor = new ColorFieldEditor(BORDERS_COLOR_ID,
 				"Borders Color", getFieldEditorParent());
 		edgesColorFieldEditor = new ColorFieldEditor(EDGES_COLOR_ID,
 				"Edges Color", getFieldEditorParent());
 
 		addField(backgroundColorFieldEditor);
 		addField(foregroundColorFieldEditor);
 		addField(bordersColorFieldEditor);
 		addField(edgesColorFieldEditor);
 
 		for (final IPreferencesContributer preferencesExtender : preferencesExtenders) {
 			preferencesExtender.createFieldEditors();
 		}
 	} // createFieldEditors
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
 	 */
 	@Override
 	protected void initialize() {
 		for (final IPreferencesContributer preferencesExtender : preferencesExtenders) {
 			preferencesExtender.initialize();
 		}
 		super.initialize();
 	} // initialize
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 	 */
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		for (final IPreferencesContributer preferencesExtender : preferencesExtenders) {
 			preferencesExtender.propertyChange(event);
 		}
 		super.propertyChange(event);
 	} // propertyChange
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
 	 */
 	@Override
 	protected void performDefaults() {
 		foregroundColorFieldEditor.getColorSelector().setColorValue(
 				StringConverter.asRGB(FOREGROUND_COLOR_DEFAULT_RGB_STRING));
 		backgroundColorFieldEditor.getColorSelector().setColorValue(
 				StringConverter.asRGB(BACKGROUND_COLOR_DEFAULT_RGB_STRING));
 		bordersColorFieldEditor.getColorSelector().setColorValue(
 				StringConverter.asRGB(BORDERS_COLOR_DEFAULT_RGB_STRING));
 		edgesColorFieldEditor.getColorSelector().setColorValue(
 				StringConverter.asRGB(EDGES_COLOR_DEFAULT_RGB_STRING));
 
 		for (final IPreferencesContributer preferencesExtender : preferencesExtenders) {
 			preferencesExtender.performDefaults();
 		}
 	} // performDefaults
 
 	/**
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(@SuppressWarnings("unused")
 	final IWorkbench arg0) {
 		// Do nothing
 	} // init
 
 	/**
 	 * This makes addField public
 	 */
 	@Override
 	public void addField(final FieldEditor editor) {
 		super.addField(editor);
 	} // addField
 
 	/**
 	 * This makes getFieldEditorParent public
 	 */
 	@Override
 	public Composite getFieldEditorParent() {
 		return super.getFieldEditorParent();
 	} // getFieldEditorParent
 
 } // MapsColorsPreferencePage
