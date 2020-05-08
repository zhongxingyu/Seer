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
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.swt.graphics.Image;
 
 import org.eclipse.riena.beans.common.ListBean;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 import org.eclipse.riena.ui.swt.utils.ImageStore;
 
 /**
  * Controller for the {@link ComboCompletionSubModuleView} example.
  */
 public class CompletionComboSubModuleController extends SubModuleController {
 
 	@Override
 	public void configureRidgets() {
 		final ListBean input = createInput();
 
 		final IComboRidget combo1 = configureCombo(input, "combo1", "selection1", "text1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		combo1.setMarkSelectionMismatch(true);
 
 		configureCombo(input, "combo2", "selection2", "text2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		final IComboRidget combo3 = configureCombo(input, "combo3", "selection3", "text3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		combo3.setMarkSelectionMismatch(true);
 		final CityFormatter formatter = new CityFormatter();
 		combo3.setColumnFormatter(formatter);
 
 		final IComboRidget combo4 = configureCombo(input, "combo4", "selection4", "text4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		combo4.setColumnFormatter(formatter);
 	}
 
 	// helping methods
 	//////////////////
 
 	private IComboRidget configureCombo(final Object input, final String comboId, final String selectionId,
 			final String textId) {
 		final ITextRidget selectionRidget = getRidget(ITextRidget.class, selectionId);
 		selectionRidget.setOutputOnly(true);
 
 		final ITextRidget textRidget = getRidget(ITextRidget.class, textId);
 		textRidget.setOutputOnly(true);
 
 		final IComboRidget result = getRidget(IComboRidget.class, comboId);
 		final WritableValue selection = new WritableValue() {
 			@Override
 			public void doSetValue(final Object value) {
				final String text = value == null ? "" : ((City) value).getName(); //$NON-NLS-1$
				selectionRidget.setText(text);
 			}
 		};
 		result.bindToModel(input, ListBean.PROPERTY_VALUES, City.class, "getName", selection, "value"); //$NON-NLS-1$ //$NON-NLS-2$
 		result.addPropertyChangeListener(IComboRidget.PROPERTY_TEXT, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				textRidget.setText((String) evt.getNewValue());
 			}
 		});
 		result.updateFromModel();
 
 		return result;
 	}
 
 	private ListBean createInput() {
 		final List<City> values = new ArrayList<City>();
 		values.add(new City("Rome", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Milan", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Napoli", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Torino", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Palermo", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Genova", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Bologna", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Firenze", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Venezia", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Pisa", Country.ITALY)); //$NON-NLS-1$
 		values.add(new City("Berlin", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Hamburg", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Kln", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Frankfurt am Main", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Stuttgart", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Dortmund", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Essen", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Dsseldorf", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Mnchen", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Mannheim", Country.GERMANY)); //$NON-NLS-1$
 		values.add(new City("Paris", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Marseille", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Lyon", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Toulouse", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Nice", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Nantes", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Strasbourg", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Montpellier", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Bordeaux", Country.FRANCE)); //$NON-NLS-1$
 		values.add(new City("Lille", Country.FRANCE)); //$NON-NLS-1$
 		Collections.sort(values, new Comparator<City>() {
 			public int compare(final City o1, final City o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 		return new ListBean(values);
 	}
 
 	// helping classes
 	//////////////////
 
 	public enum Country {
 		ITALY, FRANCE, GERMANY
 	}
 
 	public static final class City {
 		private final String name;
 		private final Country country;
 
 		City(final String name, final Country country) {
 			this.name = name;
 			this.country = country;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Country getCountry() {
 			return country;
 		}
 	}
 
 	private static final class CityFormatter extends ColumnFormatter {
 
 		@Override
 		public String getText(final Object element) {
			return ((City) element).getName();
 		}
 
 		@Override
 		public Image getImage(final Object element) {
 			final Country country = ((City) element).getCountry();
 			final String key = getImageKey(country);
 			Image result = null;
 			if (key != null) {
 				result = ImageStore.getInstance().getImage(key);
 			}
 			return result;
 		}
 
 		private String getImageKey(final Country country) {
 			switch (country) {
 			case ITALY:
 				return "flag_italy.png"; //$NON-NLS-1$
 			case GERMANY:
 				return "flag_germany.png"; //$NON-NLS-1$
 			case FRANCE:
 				return "flag_france.png"; //$NON-NLS-1$
 			}
 			return null;
 		}
 
 	}
 
 }
