 // Copyright (C) 2011 Associated Universities, Inc. Washington DC, USA.
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 // 
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // General Public License for more details.
 // 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 // 
 // Correspondence concerning GBT software should be addressed as follows:
 //       GBT Operations
 //       National Radio Astronomy Observatory
 //       P. O. Box 2
 //       Green Bank, WV 24944-0002 USA
 
 package edu.nrao.dss.client.forms;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SliderEvent;
 import com.extjs.gxt.ui.client.widget.Slider;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.Radio;
 import com.extjs.gxt.ui.client.widget.form.SliderField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.FillData;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.NumberFormat;
 
 import edu.nrao.dss.client.forms.fields.ComboModel;
 import edu.nrao.dss.client.forms.fields.GeneralCombo;
 import edu.nrao.dss.client.forms.fields.GeneralRadioGroup;
 import edu.nrao.dss.client.forms.fields.GeneralText;
 
 
 public class SourceForm extends BasicForm {
 	private GeneralText restFreq, sourceVelocity, redshift,
 			topoFreq, rightAscension, tBG;
 	private GeneralCombo doppler;
 	private GeneralRadioGroup galactic, frame;
 	private Radio topoFrame, restFrame;
 	private Slider diameter, minElevation, declination, dutyCycle, effectiveBw;
 	private SliderField diameterSF, minElevationSF, declinationSF, dutyCycleSF, effectiveBwSF;
 	private LabelField diameter_display, minElevationDisplay, decDisplay, dutyCycleDisplay, effectiveBwDisplay;
 	private double c      = 2.99792458e10; // speed of light in cm/s
 	private String rx;
 	
 	
 	public SourceForm() {
 		super("Source Information");
 	}
 
 	public void initLayout() {
 		setCollapsible(true);
 		
 		initSourceFrequencyFields();
 		initTsysContributionFields();
 		initSourcePositionFields();
 		initPulsarFields();
 		
 		topoFreq.hide();
 		topoFreq.setAllowBlank(true);
 		redshift.hide();
 		redshift.setAllowBlank(true);
 		rightAscension.hide();
 		rightAscension.setAllowBlank(true);
 		dutyCycleDisplay.hide();
 		dutyCycleSF.hide();
 		effectiveBwDisplay.hide();
 		effectiveBw.hide();
 
 		initListeners();
 
 		FormData fd = new FormData(60, 20);
 		 
 		// attaching fields
 		add(frame);
 		add(restFreq, fd);
 		add(topoFreq, fd);
 		add(doppler);
 		add(redshift, fd);
 		add(sourceVelocity, fd);
 		
 		add(diameter_display);
 		add(diameterSF); // source diameter
 		
 		add(dutyCycleDisplay);
 		add(dutyCycleSF);
 		add(effectiveBwDisplay);
 		add(effectiveBwSF);
 		
 		FieldSet fs = new FieldSet();
 		fs.setHeading("Source Contribution Corrections");
 		FormLayout layout = new FormLayout();  
 		layout.setLabelWidth(200);  
 		fs.setLayout(layout);
 		
 		fs.add(galactic);
 		fs.add(rightAscension, fd);
 		fs.add(tBG, fd);
 		add(fs);
 		
 		add(decDisplay);
 		add(declinationSF);  // declination
 		add(minElevationDisplay);
 		add(minElevationSF);  // min el
 	}
 	
 	private void initSourceFrequencyFields() {
 		restFreq  = new GeneralText("rest_freq", "Rest Frequency (MHz)");
 		restFreq.setValue("1420");
 		topoFreq  = new GeneralText("topocentric_freq", "Topocentric Frequency (MHz)");
 		topoFreq.setValue("1420");
 		doppler   = new GeneralCombo("doppler", "Doppler Correction", getDoppler());
 		doppler.setValue(new ComboModel("Optical"));
 		sourceVelocity = new GeneralText("source_velocity", "Source Velocity (km/s)");
 		sourceVelocity.setValue("0");
 		redshift = new GeneralText("redshift", "Redshift");
 		redshift.setValue("0");
 		
 		frame = new GeneralRadioGroup("frame");
 		frame.setFieldLabel("Frequency Specified in the");
 		frame.setName("frame");
 		frame.setId("frame");
 		frame.setOrientation(Orientation.VERTICAL);
 		
 		topoFrame = new Radio();
 		topoFrame.setBoxLabel("Topocentric Frame");
 		topoFrame.setValueAttribute("Topocentric Frame");
 		topoFrame.setName("topocentric_frame");
 		topoFrame.setLabelSeparator("");
 		frame.add(topoFrame);
 		
 		restFrame = new Radio();
 		restFrame.setBoxLabel("Rest Frame");
 		restFrame.setValueAttribute("Rest Frame");
 		restFrame.setName("rest_frame");
 		restFrame.setLabelSeparator("");
 		restFrame.setValue(true);
 		frame.add(restFrame);
 	}
 	
 	private void initTsysContributionFields(){
 		galactic = new GeneralRadioGroup("galactic");
 		galactic.setFieldLabel("Source Contribution to System Temperature");
 		galactic.setName("galactic");
 		galactic.setId("galactic");
 		galactic.setOrientation(Orientation.VERTICAL);
 		
 		Radio choice = new Radio();
 		choice = new Radio();
 		choice.setBoxLabel("User Estimated Correction");
 		choice.setToolTip("Enter a value for the expected contribution from the source to Tsys.");
 		choice.setValueAttribute("estimated");
 		choice.setName("estimated");
 		choice.setLabelSeparator("");
 		choice.setValue(true);
 		galactic.add(choice);
 		
 		choice = new Radio();
 		choice.setBoxLabel("Internal Galactic Model");
 		choice.setToolTip("Use a model of the galactic contribution to Tsys.");
 		choice.setValueAttribute("model");
 		choice.setName("model");
 		choice.setLabelSeparator("");
 		galactic.add(choice);
 		
 		tBG            = new GeneralText("estimated_continuum", "Contribution (K)");
 		tBG.setToolTip("Enter contribution of source to continuum level in units of K.");
 		tBG.setValue("0");
 	}
 	
 	private void initSourcePositionFields() {
 		diameter_display = new LabelField("0.0");
 		diameter_display.setFieldLabel("Source Diameter (arc minutes)");
 		diameter_display.setLabelSeparator(":");
 		
 		diameter = new Slider();
 		diameter.setMinValue(0);
 		diameter.setMaxValue(12);
 		diameter.setValue(0);
 		diameter.setIncrement(1);
 		diameter.setUseTip(false);
 		
 		diameterSF = new SliderField(diameter);
 		diameterSF.setName("source_diameter_slider");
 		diameterSF.setId("source_diameter_slider");
 		diameterSF.setLabelSeparator("");
 		
 		minElevationDisplay = new LabelField("5");
 		minElevationDisplay.setFieldLabel("Minimum Elevation (Deg)");
 		minElevationDisplay.setLabelSeparator(":");
 		
 		minElevation = new Slider();
 		minElevation.setMinValue(5);
 		minElevation.setMaxValue(90);
 		minElevation.setValue(5);
 		minElevation.setIncrement(1);
 		minElevation.setUseTip(false);
 		
 		minElevationSF = new SliderField(minElevation);
 		minElevationSF.setLabelSeparator("");
 		minElevationSF.setName("min_elevation");
 		minElevationSF.setId("min_elevation");
 		
 		decDisplay = new LabelField("0");
 		decDisplay.setFieldLabel("Source Declination (Deg)");
 		decDisplay.setLabelSeparator(":");
 		
 		declination = new Slider();
 		declination.setMinValue(-47);
 		declination.setMaxValue(90);
 		declination.setValue(0);
 		declination.setIncrement(1);
 		declination.setUseTip(false);
 		
 		declinationSF = new SliderField(declination);
 		declinationSF.setLabelSeparator("");
 		declinationSF.setName("declination");
 		declinationSF.setId("declination");
 		
 		rightAscension = new GeneralText("right_ascension", "Approx Right Ascension (HH:MM)");
 		rightAscension.setMessageTarget("side");
 		rightAscension.setValue("0");
 		rightAscension.setValidator(new Validator () {
 
 			@Override
 			public String validate(Field<?> field, String value) {
 				if (value.isEmpty() || value.equals("-")) {
 					return null;
 				}
 				
 				String[] values = value.split(":");
 				if (values.length < 2) {
 					return null;
 				}
 				
 				if (Float.valueOf(values[1]) >= 60.0) {
 					return "Minutes cannot be greater than or equal to 60.";
 				} else if (Float.valueOf(values[0]) < 0.0){
 					return "Right Ascension cannot be negative.";
 				}
 				return null;
 			}
 			
 		});
 	}
 	
 	private void initPulsarFields() {
 		dutyCycleDisplay = new LabelField("10");
 		dutyCycleDisplay.setFieldLabel("Duty Cycle (%)");
 		dutyCycleDisplay.setLabelSeparator(":");
 		
 		dutyCycle = new Slider();
 		dutyCycle.setMinValue(1);
 		dutyCycle.setMaxValue(100);
 		dutyCycle.setValue(10);
 		dutyCycle.setIncrement(5);
 		dutyCycle.setUseTip(false);
 		
 		dutyCycleSF = new SliderField(dutyCycle);
 		dutyCycleSF.setLabelSeparator("");
 		dutyCycleSF.setName("duty_cycle");
 		dutyCycleSF.setId("duty_cycle");
 		
 		effectiveBwDisplay = new LabelField("0");
 		effectiveBwDisplay.setFieldLabel("Effective BW");
 		effectiveBwDisplay.setLabelSeparator(":");
 		
 		effectiveBw = new Slider();
 		effectiveBw.setMinValue(1);
 		effectiveBw.setMaxValue(100);
 		effectiveBw.setUseTip(false);
 		
 		effectiveBwSF = new SliderField(effectiveBw);
 		effectiveBwSF.setLabelSeparator("");
 		effectiveBwSF.setName("effective_bw");
 		effectiveBwSF.setId("effective_bw");
 		
 	}
 	
 	private void initListeners() {
 		diameter.addListener(Events.Change, new Listener<SliderEvent> () {
 
 			@Override
 			public void handleEvent(SliderEvent se) {
 				calcDiameter();
 			}
 		});
 		frame.addListener(Events.Change, new HandleFrame());
 		frame.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 		minElevation.addListener(Events.Change, new Listener<SliderEvent> () {
 
 			@Override
 			public void handleEvent(SliderEvent se) {
 				minElevationDisplay.setValue("" + minElevation.getValue());
 			}
 			
 		});
 		declination.addListener(Events.Change, new Listener<SliderEvent> () {
 
 			@Override
 			public void handleEvent(SliderEvent se) {
 				decDisplay.setValue("" + declination.getValue());
 			}
 			
 		});
 		dutyCycle.addListener(Events.Change, new Listener<SliderEvent> () {
 
 			@Override
 			public void handleEvent(SliderEvent se) {
 				dutyCycleDisplay.setValue("" + dutyCycle.getValue());
 			}
 			
 		});
 		effectiveBw.addListener(Events.Change, new Listener<SliderEvent> () {
 
 			@Override
 			public void handleEvent(SliderEvent se) {
 				effectiveBwDisplay.setValue("" + effectiveBw.getValue());
 			}
 			
 		});
 		
 		doppler.addListener(Events.Select, new HandleDoppler());
 		doppler.addListener(Events.Select, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 		galactic.addListener(Events.Change, new HandleGalactic());
 		restFreq.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 		topoFreq.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 		redshift.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 		sourceVelocity.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent se) {
 				calcDiameter();
 				notifyAllForms();
 			}
 		});
 	}
 	
 	private void calcDiameter() {
 		// Do we have everything we need?	
 		Radio frameChoice = frame.getValue();
 		if (frameChoice != null & !topoFreq.getValue().isEmpty() & !restFreq.getValue().isEmpty() & !sourceVelocity.getValue().isEmpty() & !redshift.getValue().isEmpty()) {
 			if (frameChoice.getValueAttribute().equals("Topocentric Frame")) {
 				double d = 0.1 * diameter.getValue() * calcFWHM(Double.valueOf(topoFreq.getValue()) * 1e6);
 				diameter_display.setValue(NumberFormat.getFormat("#.##").format(d));
 			} else {
 				//  TODO: Refactor to use Functions.velocity2Frequency()
 				double rfreq = Double.valueOf(restFreq.getValue()) * 1e6;
 				double tfreq = 0;
 				if (doppler.getSelected().equals("Redshift")) {
 					tfreq = rfreq / (1 + Double.valueOf(redshift.getValue()));
 				} else if (doppler.getSelected().equals("Radio")) {
 					tfreq = rfreq * (1 - (Double.valueOf(sourceVelocity.getValue()) * 1e3) / (c * 1e-2));
 				} else {
 					tfreq = rfreq / (1 + (Double.valueOf(sourceVelocity.getValue()) * 1e3) / (c * 1e-2));
 				}
 				double d = 0.1 * diameter.getValue() * calcFWHM(tfreq);
 				diameter_display.setValue(NumberFormat.getFormat("#.##").format(d));
 				topoFreq.setValue("" + tfreq * 1e-6);
 			}
 		}
 	}
 	
 	private double calcFWHM(double freq) {
 		double TeDB   = 13;
 		double lambda = c / freq;
 		int dish_radius = 50;
 		return (1.02 + 0.0135 * TeDB) * 3437.7 * (lambda / (2 * dish_radius * 100));
 	}
 	
 	public void validate() {
 
 	}
 
 	public void notify(String name, String value) {
 		// handler for mode
 		if (name.equals("mode")) {
 			if (value.equals("Pulsar")) {
 				// Pulsars are point sources, so hide the slide, 
 				// but still use its default value (zero).
 				diameter_display.hide();
 				diameterSF.hide();
 				dutyCycleDisplay.show();
 				dutyCycleSF.show();
 				effectiveBwDisplay.show();
 				effectiveBw.show();
 			} else {
 				diameter_display.show();
 				diameterSF.show();
 				dutyCycleDisplay.hide();
 				dutyCycleSF.hide();
 				effectiveBwDisplay.hide();
 				effectiveBw.hide();
 			}
 			
 			if (value.equals("Spectral Line")) {
 				frame.show();
 				restFrame.setValue(true);
 				topoFreq.hide();
 				topoFreq.setAllowBlank(true);
 				restFreq.show();
 				restFreq.setAllowBlank(false);
 				doppler.show();
 				redshift.hide();
 				redshift.setAllowBlank(true);
 				sourceVelocity.show();
 				sourceVelocity.setAllowBlank(false);
 			} else {
 				frame.hide();
 				topoFrame.setValue(true);
 				restFreq.hide();
 				restFreq.setAllowBlank(true);
 				doppler.hide();
 				topoFreq.show();
 				topoFreq.setAllowBlank(false);
 				redshift.hide();
 				redshift.setAllowBlank(true);
 				sourceVelocity.hide();
 				sourceVelocity.setAllowBlank(true);
 			}
 
 		} else if (name.equals("receiver")) {
 			
 			if (value.contains("(") && !value.equals(rx)) {
 				float freq_low = Float.valueOf(value.substring(value.indexOf("(") + 1, value.indexOf("-") - 1)).floatValue();
 				float freq_hi  = Float.valueOf(value.substring(value.indexOf("-") + 2, value.indexOf("G") - 1)).floatValue();
 				float freq_mid = (freq_low + (freq_hi - freq_low) / 2) * 1000;
 				restFreq.setValue("" + freq_mid);
 				topoFreq.setValue("" + freq_mid);
 			}
 			rx = value;
 		} else if (name.equals("backend")) {
 			if (value.equals("Mustang")) {
 				topoFreq.setValue("90000");
 			}
 		} else if (name.equals("bandwidth")) {
 			if (!value.equals("NOTHING")) {
				int bw = Integer.valueOf(value);
 				effectiveBw.setMaxValue(bw);
 				effectiveBw.setValue(bw);
				effectiveBw.setIncrement(bw / 20);
 			}
 		}
 		notifyAllForms();
 
 	}
 	
 	private void updateDopplerFields() {
 		if (doppler.getValue().getName("name") == "Redshift") {
 			redshift.show();
 			redshift.setAllowBlank(false);
 			sourceVelocity.hide();
 			sourceVelocity.setAllowBlank(true);
 		} else {
 			redshift.hide();
 			redshift.setAllowBlank(true);
 			sourceVelocity.show();
 			sourceVelocity.setAllowBlank(false);
 		}
 	}
 
 	class HandleFrame implements Listener<FieldEvent> {
 		public void handleEvent(FieldEvent fe) {
 			Radio choice = frame.getValue();
 			if (choice != null) {
 				if (choice.getValueAttribute().equals("Rest Frame")) {
 					topoFreq.hide();
 					topoFreq.setAllowBlank(true);
 					restFreq.show();
 					restFreq.setAllowBlank(false);
 					doppler.show();
 					updateDopplerFields();
 				} else {
 					topoFreq.show();
 					topoFreq.setAllowBlank(false);
 					restFreq.hide();
 					restFreq.setAllowBlank(true);
 					doppler.hide();
 					sourceVelocity.hide();
 					sourceVelocity.setAllowBlank(true);
 					redshift.hide();
 					redshift.setAllowBlank(true);
 				}
 				notifyAllForms();
 			}
 		}
 	}
 
 	class HandleDoppler implements Listener<FieldEvent> {
 		public void handleEvent(FieldEvent fe) {
 			updateDopplerFields();
 			notifyAllForms();
 		}
 	}
 
 	class HandleGalactic implements Listener<FieldEvent> {
 		public void handleEvent(FieldEvent fe) {
 			if (galactic.getValue().getValueAttribute().equals("model")) {
 				rightAscension.show();
 				rightAscension.setAllowBlank(false);
 				tBG.hide();
 				tBG.setAllowBlank(true);
 				tBG.setValue("0");
 			} else if (galactic.getValue().getValueAttribute().equals("estimated")) {
 				rightAscension.hide();
 				rightAscension.setAllowBlank(true);
 				tBG.show();
 				tBG.setAllowBlank(false);
 			} else {
 				tBG.setValue("0");
 				rightAscension.hide();
 				rightAscension.setAllowBlank(true);
 				tBG.hide();
 				tBG.setAllowBlank(true);
 			}
 		}
 	}
 	
 	public static ArrayList<String> getDoppler() {
 		return new ArrayList<String>(Arrays.asList("Radio", "Optical",
 				"Redshift"));
 	}
 
 }
