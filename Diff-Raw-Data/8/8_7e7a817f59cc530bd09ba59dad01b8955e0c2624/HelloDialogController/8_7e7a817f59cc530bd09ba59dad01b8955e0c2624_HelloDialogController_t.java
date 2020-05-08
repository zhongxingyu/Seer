 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.beans.common.AbstractBean;
 import org.eclipse.riena.example.client.application.ExampleIcons;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.annotation.OnActionCallback;
import org.eclipse.riena.ui.ridgets.annotation.OnActionCallbacks;
 import org.eclipse.riena.ui.ridgets.controller.AbstractWindowController;
 
 /**
  * The controller for the hello dialog of the dialog example.
  */
 public class HelloDialogController extends AbstractWindowController {
 
 	private final String[] carPlates = { "JM5B0ND", "1 SPY", "MNY PNY", "BN D07", "Q RULE2", "MI64EVR" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 	private ISingleChoiceRidget compositeCarModel;
 	private ISingleChoiceRidget compositeCarWarranty;
 	private IMultipleChoiceRidget compositeCarPlates;
 	private IMultipleChoiceRidget compositeCarExtras;
 	private CarConfig carConfig;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void configureRidgets() {
 		super.configureRidgets();
 
 		getWindowRidget().setTitle("James' Car Configurator"); //$NON-NLS-1$
 		getWindowRidget().setIcon(ExampleIcons.ICON_SAMPLE);
 
 		carConfig = new CarConfig();
 
 		compositeCarModel = getRidget("compositeCarModel"); //$NON-NLS-1$
 		compositeCarModel.bindToModel(toList(CarModels.values()),
 				BeansObservables.observeValue(carConfig, CarConfig.PROP_MODEL));
 		compositeCarModel.addMarker(new MandatoryMarker());
 		compositeCarModel.updateFromModel();
 
 		compositeCarExtras = getRidget("compositeCarExtras"); //$NON-NLS-1$
 		final String[] labels = { "Front Machine Guns", "Self Destruct Button", "Underwater Package", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				"Park Distance Control System", }; //$NON-NLS-1$
 		compositeCarExtras.bindToModel(toList(CarOptions.values()), Arrays.asList(labels), carConfig,
 				CarConfig.PROP_OPTIONS);
 		compositeCarExtras.updateFromModel();
 
 		compositeCarWarranty = getRidget("compositeCarWarranty"); //$NON-NLS-1$
 		compositeCarWarranty.bindToModel(toList(CarWarranties.values()),
 				BeansObservables.observeValue(carConfig, CarConfig.PROP_WARRANTY));
 		compositeCarWarranty.addMarker(new MandatoryMarker());
 		compositeCarWarranty.updateFromModel();
 
 		compositeCarPlates = getRidget("compositeCarPlates"); //$NON-NLS-1$
 		compositeCarPlates
 				.bindToModel(toList(carPlates), PojoObservables.observeList(carConfig, CarConfig.PROP_PLATES));
 		compositeCarPlates.addMarker(new MandatoryMarker());
 		compositeCarPlates.updateFromModel();
 
 		final ITextRidget txtPrice = getRidget("txtPrice"); //$NON-NLS-1$
 		txtPrice.setOutputOnly(true);
 		final DataBindingContext dbc = new DataBindingContext();
 		dbc.bindValue(BeansObservables.observeValue(txtPrice, ITextRidget.PROPERTY_TEXT),
 				BeansObservables.observeValue(carConfig, CarConfig.PROP_PRICE), null, null);
 
 		final IActionRidget buttonPreset = getRidget("buttonPreset"); //$NON-NLS-1$
 		buttonPreset.setText("&Quick Config"); //$NON-NLS-1$
 
 		final IActionRidget buttonReset = getRidget("buttonReset"); //$NON-NLS-1$
 		buttonReset.setText("&Reset"); //$NON-NLS-1$
 	}
 
 	@OnActionCallback(ridgetId = "buttonPreset")
 	public void onPreset() {
 		compositeCarModel.setSelection(CarModels.BMW);
 		compositeCarExtras.setSelection(Arrays.asList(new CarOptions[] { CarOptions.PDCS }));
 		compositeCarWarranty.setSelection(CarWarranties.EXTENDED);
 		compositeCarPlates.setSelection(Arrays.asList(new String[] { carPlates[0] }));
 	}
 
 	@OnActionCallback(ridgetId = "buttonReset")
 	public void onReset() {
 		carConfig.reset();
 		compositeCarModel.updateFromModel();
 		compositeCarExtras.updateFromModel();
 		compositeCarWarranty.updateFromModel();
 		compositeCarPlates.updateFromModel();
 	}
 
	@SuppressWarnings("unused")
	@OnActionCallbacks({ @OnActionCallback(ridgetId = "buttonPreset"), @OnActionCallback(ridgetId = "buttonReset") })
	private void demonstrateMutlipleOnActionCallbacksAnnotation() {
		System.out.println("Demonstrate multiple @OnActionCallback annotations!"); //$NON-NLS-1$
	}

 	// helping methods
 	// ////////////////
 
 	private WritableList toList(final Object[] values) {
 		return new WritableList(Arrays.asList(values), Object.class);
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Bean that holds a single car configuration composed of: model, option(s),
 	 * warranty, plate(s).
 	 */
 	private static final class CarConfig extends AbstractBean {
 		public static final String PROP_MODEL = "model"; //$NON-NLS-1$
 		public static final String PROP_OPTIONS = "options"; //$NON-NLS-1$
 		public static final String PROP_WARRANTY = "warranty"; //$NON-NLS-1$
 		public static final String PROP_PLATES = "plates"; //$NON-NLS-1$
 		public static final String PROP_PRICE = "price"; //$NON-NLS-1$
 
 		private CarModels model;
 		private List<CarOptions> options = new ArrayList<CarOptions>();
 		private CarWarranties warranty;
 		private List<String> plates = new ArrayList<String>();
 
 		@SuppressWarnings("unused")
 		public CarModels getModel() {
 			return model;
 		}
 
 		public void setModel(final CarModels model) {
 			firePropertyChanged(PROP_MODEL, this.model, this.model = model);
 			firePropertyChanged(PROP_PRICE, null, getPrice());
 		}
 
 		@SuppressWarnings("unused")
 		public List<CarOptions> getOptions() {
 			return Collections.unmodifiableList(options);
 		}
 
 		public void setOptions(final List<CarOptions> options) {
 			firePropertyChanged(PROP_OPTIONS, this.options, this.options = new ArrayList<CarOptions>(options));
 			firePropertyChanged(PROP_PRICE, null, getPrice());
 		}
 
 		@SuppressWarnings("unused")
 		public CarWarranties getWarranty() {
 			return warranty;
 		}
 
 		public void setWarranty(final CarWarranties warranty) {
 			firePropertyChanged(PROP_WARRANTY, this.warranty, this.warranty = warranty);
 			firePropertyChanged(PROP_PRICE, null, getPrice());
 		}
 
 		@SuppressWarnings("unused")
 		public List<String> getPlates() {
 			return Collections.unmodifiableList(plates);
 		}
 
 		public void setPlates(final List<String> plates) {
 			firePropertyChanged(PROP_PLATES, this.plates, this.plates = new ArrayList<String>(plates));
 			firePropertyChanged(PROP_PRICE, null, getPrice());
 		}
 
 		public void reset() {
 			setModel(null);
 			setOptions(new ArrayList<CarOptions>());
 			setWarranty(null);
 			setPlates(new ArrayList<String>());
 		}
 
 		public long getPrice() {
 			long price = 0;
 			if (model != null) {
 				price += 100000;
 			}
 			price += options.size() * 25000L;
 			if (warranty == CarWarranties.EXTENDED) {
 				price += 10000;
 			}
 			price += plates.size() * 200L;
 			return price;
 		}
 	}
 
 	private enum CarModels {
 		ASTON_MARTIN("Aston Martin V-12 Vanquish"), LOTUS("Lotus Esprit Turbo"), BMW("BMW Z8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		private final String label;
 
 		private CarModels(final String label) {
 			Assert.isNotNull(label);
 			this.label = label;
 		}
 
 		@Override
 		public String toString() {
 			return label;
 		}
 	}
 
 	private enum CarOptions {
 		FRONT_GUNS, SELF_DESTRUCT, UNDERWATER, PDCS
 	}
 
 	private enum CarWarranties {
 		STANDARD, EXTENDED;
 
 		@Override
 		public String toString() {
 			final char[] result = super.toString().toLowerCase().toCharArray();
 			result[0] = Character.toUpperCase(result[0]);
 			return String.valueOf(result);
 		}
 	}
 
 }
