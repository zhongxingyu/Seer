 // AbstractRelativeValueColorProviderAdapter.java
 package org.eclipse.stem.ui.adapters.color;
 
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
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.jface.resource.StringConverter;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapterFactory;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.preferences.MapsColorsPreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.RGB;
 
 /**
  * This is an abstract class that Relative Value Color Provider Adapter should extend.
  * It holds the common information for all color providers and has few abstract methods
  * that the concrete class should override.
  */
 public abstract class AbstractRelativeValueColorProviderAdapter extends ColorProviderAdapter
 		implements RelativeValueColorProvider {
 	
 	/**
 	 * Holds the {@link Color} for relative value of 1
 	 */
 	private Color max_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values between 0.8 and 1
 	 */
 	private Color range_08_1_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values between 0.6 and 0.8
 	 */
 	private Color range_06_08_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values between 0.4 and 0.6
 	 */
 	private Color range_04_06_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values between 0.2 and 0.4
 	 */
 	private Color range_02_04_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values between 0 and 0.2
 	 */
 	private Color range_0_02_FillColor;
 	/**
 	 * Holds the {@link Color} for relative values of 0
 	 */
 	protected Color zero_FillColor;
 	/**
 	 * Holds the default foreground {@link Color} (the base color for AlphaComposite)
 	 */
 	protected Color foregroundFillColor;	
 	/**
 	 * The threshold under which values are considered zero
 	 */
 	protected final float ZERO_RELATIVE_VALUE_THRESHOLD = 0.000000001f;
 	/**
 	 * Reference to the selected {@link Decorator}
 	 */
 	protected Decorator selectedDecorator = null;
 	/**
 	 * Instance of a relative value provider
 	 */
 	protected RelativeValueProviderAdapter rvp = null;
 	/**
 	 * Current device
 	 */
 	protected Device device = null;
 	
 	/**
 	 * Constructor
 	 */
 	public AbstractRelativeValueColorProviderAdapter() {
 		updateColorsFromPreferences();
 	} // AbstractRelativeValueColorProviderAdapter
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.ColorProvider#getColor(Device)
 	 */
 	public Color getColor(Device device) {
 		double relativeValue = getRelativeValue();
 		this.device = device;
 		return getColorForRelativeValue(relativeValue);
 	} // getColor
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.ColorProvider#updateGC(GC)
 	 */
 	public void updateGC(GC gcToUpdate) {
 		this.updateGC(gcToUpdate, 1.0f, false);
 	} // setG2DColor
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.ColorProvider#updateGC(GC, float, boolean)
 	 */
 	public void updateGC(GC gcToUpdate, 
 			@SuppressWarnings("unused")float gainFactor, 
 			@SuppressWarnings("unused")boolean useLogScaling) {
 		//If the concrete color provider uses the gainFactor and/or useLogScaling parameters
 		//then it should override this method with its implementation
 		Color foreground = getColor(gcToUpdate.getDevice());
 		gcToUpdate.setBackground(foreground);
 	} // setG2DColor
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.RelativeValueColorProvider#getRelativeValue()
 	 */
 	abstract public float getRelativeValue();
 	
 	/**
 	 * The method returns the {@link Color} that matches the specified relative value.
 	 * 
 	 * @param relativeValue the relative value
 	 * @return the matching {@link Color}
 	 */
 	protected Color getColorForRelativeValue(final double relativeValue) {
 		Color retValue = device.getSystemColor(SWT.COLOR_BLACK);
 		updateColorsFromPreferences();
 		if (relativeValue == 1) {
 			retValue = max_FillColor;
 		}
 		else if (relativeValue > 0.8) {
 			retValue = range_08_1_FillColor;
 		} 
 		else if (relativeValue > 0.6) {
 			// Yes
 			retValue = range_06_08_FillColor;
 		} // if
 		else if (relativeValue > 0.4) {
 			retValue = range_04_06_FillColor;
 		}
 		else if (relativeValue > 0.2) {
 			retValue = range_02_04_FillColor;
 		}
 		else if (relativeValue > ZERO_RELATIVE_VALUE_THRESHOLD) {
 			retValue = range_0_02_FillColor;
 		}
 		else if (relativeValue <= ZERO_RELATIVE_VALUE_THRESHOLD) {
 			retValue = zero_FillColor;
 		}
 
 		return retValue;
 	} // getColorForRelativeValue
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.ColorProviderAdapter#isAdapterForType(java.lang.Object)
 	 */
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type == RelativeValueColorProvider.class;
 	} // isAdapterForType
 	
 	/**
 	 * The method will convert the RGB string into {@link Color}
 	 * @param rgbString the RGB string
 	 * @return the matching {@link Color}
 	 */
 	private Color getColorFromString(final String rgbString) {
 		if (rgbString.equals("")) {
 			return device.getSystemColor(SWT.COLOR_BLACK);
 		}
 		RGB rgb = StringConverter.asRGB(rgbString);
 		return new Color(device, rgb.red, rgb.green, rgb.blue);
 	} // getColorFromString
 	
 	/**
 	 * The method will update the colors fields for the value ranges from the preferences page.
 	 */
 	private void updateColorsFromPreferences() {
 		final Preferences prefs = Activator.getDefault().getPluginPreferences();
 		max_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_MAXIMUM_RELATIVE_VALUE_ID));
 		range_08_1_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_RANGE_5_ID));
 		range_06_08_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_RANGE_4_ID));
 		range_04_06_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_RANGE_3_ID));
 		range_02_04_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_RANGE_2_ID));
 		range_0_02_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_RANGE_1_ID));
 		zero_FillColor = getColorFromString(prefs.getString(RelativeValueColorPreferences.FOREGROUND_COLOR_ZERO_RELATIVE_VALUE_ID));
 		foregroundFillColor = getColorFromString(prefs.getString(MapsColorsPreferencePage.FOREGROUND_COLOR_ID));
 	} // updateColorsFromPreferences
 	
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.ColorProvider#setSelectedDecorator(org.eclipse.stem.core.model.Decorator)
 	 */
 	public void setSelectedDecorator(Decorator selectedDecorator) {
 		this.selectedDecorator = selectedDecorator;
 	} // setSelectedDecorator
 	
 	/**
 	 * Method will set the target object at the instance of {@link RelativeValueProvider}.
 	 * @param target The new target object
 	 */
 	protected void setRVPTarget(Notifier target) {
 		if (rvp == null) {
 			rvp = (RelativeValueProviderAdapter)RelativeValueProviderAdapterFactory.INSTANCE.adapt(target, RelativeValueProvider.class);
 		}
 		else {
 			rvp.setTarget(target);
 		}
 	} // setRVPTarget
 	
 } // AbstractRelativeValueColorProviderAdapter
