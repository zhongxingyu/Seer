 package be.kuleuven.med.brainfuck.core;
 
 import java.awt.event.ActionEvent;
 import java.util.EventObject;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 
 import org.jdesktop.application.AbstractBean;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.Task.BlockingScope;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Binding;
 import org.jdesktop.beansbinding.BindingGroup;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.ELProperty;
 import org.jdesktop.swingbinding.SwingBindings;
 
 import be.kuleuven.med.brainfuck.bsaf.AppComponent;
 import be.kuleuven.med.brainfuck.entity.LedPosition;
 import be.kuleuven.med.brainfuck.io.LedMatrixConnector;
 import be.kuleuven.med.brainfuck.io.SerialPortConnector;
 import be.kuleuven.med.brainfuck.settings.ExperimentSettings;
 import be.kuleuven.med.brainfuck.settings.LedSettings;
 import be.kuleuven.med.brainfuck.task.AbstractTask;
 
 @AppComponent
 public class LedMatrixController {
 
 	public static final String UPDATE_SERIAL_PORTS_ACTION = "updateSerialPorts";
 
 	public static final String INIT_SERIAL_PORT_ACTION = "initializeSerialPort";
 
 	public static final String CLOSE_SERIAL_PORT_ACTION = "closeSerialPort";
 
 	public static final String UPDATE_LED_MATRIX_ACTION = "updateLedMatrix";
 
 	public static final String START_EXPERIMENT_ACTION = "startExperiment";
 
 	public static final String TOGGLE_LED_ACTION = "toggleLed";
 
 	private final static BeanProperty<JComponent, Boolean> ENABLED = BeanProperty.create("enabled");
 
 	private final static BeanProperty<JComponent, String> TEXT = BeanProperty.create("text");
 
 	private LedMatrixPanelView ledMatrixPanelView;
 	
 	private LedMatrixGfxView ledMatrixGfxView;
 
 	private LedMatrixPanelModel ledMatrixPanelModel;
 
 	private LedMatrixGfxModel ledMatrixGfxModel;
 
 	private LedMatrixConnector ledMatrixConnector;
 
 	public LedMatrixController(LedMatrixPanelModel ledMatrixPanelModel, LedMatrixGfxModel ledMatrixGfxModel, LedMatrixConnector ledMatrixConnector) {
 		this.ledMatrixPanelModel = ledMatrixPanelModel;
 		this.ledMatrixGfxModel = ledMatrixGfxModel;
 		this.ledMatrixConnector = ledMatrixConnector;
 	}
 
 	public void initViews(LedMatrixPanelView ledMatrixPanelView, LedMatrixGfxView ledMatrixGfxView) {
 		this.ledMatrixPanelView = ledMatrixPanelView;
 		this.ledMatrixGfxView = ledMatrixGfxView;
 
 		BindingGroup bindingGroup = new BindingGroup();
		Binding<?, Boolean, ? extends JComponent, Boolean> enabledBinding = null;
 		Binding<? extends AbstractBean, Integer, ? extends JComponent, String> valueBinding = null;
 		// bind width and height matrix properties
 		BeanProperty<LedMatrixPanelModel, Integer> widthProperty = BeanProperty.create("width");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, widthProperty, ledMatrixPanelView.getRowTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		BeanProperty<LedMatrixPanelModel, Integer> heightProperty = BeanProperty.create("height");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, heightProperty, ledMatrixPanelView.getColumnTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		// bind row and column pin numbers
 		BeanProperty<LedMatrixGfxModel, Integer> pinRowProperty = BeanProperty.create("ledMatrixGfxSelectionModel.rowPin");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, pinRowProperty, ledMatrixPanelView.getRowPinTextField(), TEXT);
 		valueBinding.setTargetNullValue(0);
 		bindingGroup.addBinding(valueBinding);
 		BeanProperty<LedMatrixGfxModel, Boolean> rowSelectedProperty = BeanProperty.create("ledMatrixGfxSelectionModel.rowSelected && !illuminated");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixGfxModel, rowSelectedProperty, ledMatrixPanelView.getRowPinTextField(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		BeanProperty<LedMatrixGfxModel, Integer> pinColumnProperty = BeanProperty.create("ledMatrixGfxSelectionModel.columnPin");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, pinColumnProperty, ledMatrixPanelView.getColumnPinTextField(), TEXT);
 		valueBinding.setTargetNullValue(0);
 		bindingGroup.addBinding(valueBinding);
 		BeanProperty<LedMatrixGfxModel, Boolean> columnSelectedProperty = BeanProperty.create("ledMatrixGfxSelectionModel.columnSelected && !illuminated");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixGfxModel, columnSelectedProperty, ledMatrixPanelView.getColumnPinTextField(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind serial port info
 		BeanProperty<LedMatrixPanelModel, List<String>> serialPortNamesProperty = BeanProperty.create("serialPortNames");
 		Binding<?, ?, ?, ?> comboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, serialPortNamesProperty, ledMatrixPanelView.getSerialPortNamesBox());
 		bindingGroup.addBinding(comboBoxBinding);
 		BeanProperty<JComboBox<?>, String> selectedItemProperty = BeanProperty.create("selectedItem");
 		BeanProperty<LedMatrixPanelModel, String> selectedSerialPortNameProperty = BeanProperty.create("selectedSerialPortName");
 		Binding<JComboBox<?>, String, LedMatrixPanelModel, String> selectedElementBinding = 
 				Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelView.getSerialPortNamesBox(), selectedItemProperty, ledMatrixPanelModel, selectedSerialPortNameProperty);
 		bindingGroup.addBinding(selectedElementBinding);
 		// bind serial port select box enabled state
 		ELProperty<LedMatrixPanelModel, Boolean> arduinoInitialized = ELProperty.create("${!arduinoInitialized}");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, arduinoInitialized, ledMatrixPanelView.getSerialPortNamesBox(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind led controls (just the enabled state)
		ELProperty<LedMatrixController, Boolean> ledControlsEnabledProperty = ELProperty.create("${!ledMatrixGfxModel.ledMatrixGfxSelectionModel.empty && ledMatrixPanelModel.arduinoInitialized && !ledMatrixPanelModel.experimentStarted}");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledControlsEnabledProperty, ledMatrixPanelView.getIntensitySlider(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledControlsEnabledProperty, ledMatrixPanelView.getToggleLedButton(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind experiment settings controls
 		ELProperty<LedMatrixPanelModel, Boolean> experimentInitialized = ELProperty.create("${arduinoInitialized && experimentInitialized && !experimentStarted}"); 
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, experimentInitialized, ledMatrixPanelView.getSecondsToRunTextField(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		BeanProperty<LedMatrixPanelModel, Integer> secondsToRunProperty = BeanProperty.create("experimentSettings.secondsToRun");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, secondsToRunProperty, ledMatrixPanelView.getSecondsToRunTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, experimentInitialized, ledMatrixPanelView.getFlickerFrequencyTextField(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		BeanProperty<LedMatrixPanelModel, Integer> flickerFrequencyProperty = BeanProperty.create("experimentSettings.flickerFrequency");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, flickerFrequencyProperty, ledMatrixPanelView.getFlickerFrequencyTextField(), TEXT);
 		valueBinding.setTargetNullValue(0);
 		bindingGroup.addBinding(valueBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, experimentInitialized, ledMatrixPanelView.getStartExperimentButton(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		bindingGroup.bind();
 	}
 
 	public void updateLedMatrix() {
 		// should regenerate led matrix??
 		int width = ledMatrixPanelModel.getWidth();
 		int height = ledMatrixPanelModel.getHeight();
 		if (width > 0 && height > 0) {
 			ledMatrixPanelModel.setExperimentInitialized(true);
 			ledMatrixGfxModel = new LedMatrixGfxModelBuilder(ledMatrixGfxModel).
 					resizeMatrix(width, height).build();
 			ledMatrixGfxView.resizeMatrix(ledMatrixGfxModel.getLedSettingsMap().keySet(), width, height);
 			// should send data to arduino as well??
 			ledMatrixGfxView.repaint();
 		}
 	}
 
 	public void updateSelection(LedPosition ledPosition, boolean isShiftDown, boolean isControlDown) {
 		if (ledPosition != null) {
 			LedSettings ledSettings = ledMatrixGfxModel.getLedSettings(ledPosition);
 			if (isShiftDown) {
 				// TODO implement shift down
 			} else if (isControlDown) {
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				ledMatrixGfxSelectionModel = new LedMatrixGfxSelectionModelBuilder(ledMatrixGfxSelectionModel)
 							.addRemoveLedSettings(ledSettings).build();
 				ledMatrixGfxModel.setLedMatrixGfxSelectionModel(ledMatrixGfxSelectionModel);
 			} else {
 				// single element selection in this case
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = 
 						LedMatrixGfxSelectionModelBuilder.of(ledSettings);
 				ledMatrixGfxModel.setLedMatrixGfxSelectionModel(ledMatrixGfxSelectionModel);
 			}
 		} else {
 			LedMatrixGfxSelectionModel emptySelection = LedMatrixGfxSelectionModelBuilder.EMPTY_SELECTION;
 			ledMatrixGfxModel.setLedMatrixGfxSelectionModel(emptySelection);
 		}
 		if (ledMatrixGfxModel.isIlluminated()) {
 			getContext().getTaskService().execute(updateIlluminatedLeds());
 		}
 		ledMatrixGfxView.repaint();
 	}
 
 	private void toggleName(AbstractButton button, String actionName) {
 		boolean isSelected = button.isSelected();
 		StringBuilder stringBuilder = new StringBuilder(actionName + ".Action.");
 		stringBuilder.append(isSelected ? "selectedText" : "text");
 		button.setText(getResourceMap().getString(stringBuilder.toString()));
 	}
 
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<?, ?> initializeSerialPort(ActionEvent event) {
 		toggleName((AbstractButton) event.getSource(), INIT_SERIAL_PORT_ACTION);
 		final String selectedSerialPortName  = ledMatrixPanelModel.getSelectedSerialPortName();
 		if (selectedSerialPortName != null && !"".equals(selectedSerialPortName) && !ledMatrixPanelModel.isArduinoInitialized()) {
 			return new AbstractTask<Void, Void>(INIT_SERIAL_PORT_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedSerialPortName);
 					ledMatrixConnector.initialize(selectedSerialPortName);
 					// will disable enabled state of in the gui..
 					ledMatrixPanelModel.setArduinoInitialized(true);
 					// should be updating the view on EDT
 					message("endMessage");
 					return null;
 				}
 
 			};
 		} else {
 			return new AbstractTask<Void, Void>(CLOSE_SERIAL_PORT_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedSerialPortName);
 					ledMatrixConnector.close();
 					ledMatrixPanelModel.setArduinoInitialized(false);
 					message("endMessage");
 					return null;
 				}
 
 			};
 		}
 	}
 
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<?, ?> updateSerialPortNames() {
 		return new AbstractTask<Void, Void>(UPDATE_SERIAL_PORTS_ACTION) {
 
 			protected Void doInBackground() throws Exception {
 				message("startMessage");
 				List<String> serialPortNames = SerialPortConnector.getSerialPortNames();
 				String selectedSerialPortName = ledMatrixConnector.getSelectedSerialPortName();
 				// should be updating the view on EDT
 				ledMatrixPanelModel.setSerialPortNames(serialPortNames);
 				ledMatrixPanelModel.setSelectedSerialPortName(selectedSerialPortName);
 				message("endMessage");
 				return null;
 			}
 
 		};
 	}
 
 	public void adjustIntensity(ChangeEvent event) {
 		getContext().getTaskService().execute(buildToggleLedTask(event));
 	}
 
 	@Action
 	public Task<?, ?> toggleLed(final ActionEvent event) {
 		return buildToggleLedTask(event);
 	}
 
 	private Task<?, ?> buildToggleLedTask(final EventObject event) {
 		return new AbstractTask<Void, Void>(TOGGLE_LED_ACTION) {
 
 			protected Void doInBackground() throws Exception {
 				Object source = event.getSource();
 				boolean illuminated = ledMatrixPanelView.getToggleLedButton().isSelected();
 				if (source instanceof JToggleButton) {
 					JToggleButton button = (JToggleButton) event.getSource();
 					toggleName(button, TOGGLE_LED_ACTION);
 					ledMatrixGfxModel.setIlluminated(illuminated);
 				} 
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for (LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					if (source instanceof JSlider) {
 						JSlider slider = (JSlider) event.getSource();
 						ledSettings.setIntensity(slider.getValue());
 					}
 					boolean selected = ledMatrixGfxModel.isSelected(ledSettings);
 					ledMatrixConnector.toggleLed(ledSettings, selected && illuminated);
 				}
 				// update gfx illumination state
 				ledMatrixGfxView.repaint();
 				message("endMessage");
 				return null;
 			}
 
 		};
 	}
 	
 	
 	public Task<?, ?> updateIlluminatedLeds() {
 		return new AbstractTask<Void, Void>(TOGGLE_LED_ACTION) {
 
 			@Override
 			protected Void doInBackground() throws Exception {
 				boolean illuminated = ledMatrixPanelView.getToggleLedButton().isSelected();
 				int intensity = ledMatrixPanelView.getIntensitySlider().getValue();
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for (LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					if (ledMatrixGfxModel.isSelected(ledSettings)) {
 						ledSettings.setIntensity(intensity);
 						ledMatrixConnector.toggleLed(ledSettings, illuminated);
 					}
 				}
 				message("endMessage");
 				return null;
 			}
 			
 		};
 	}
 
 	@Action
 	public Task<?, ?> startExperiment(ActionEvent event) {
 		JToggleButton source = (JToggleButton) event.getSource();
 		toggleName(source, START_EXPERIMENT_ACTION);
 		// set selected state to enable/disable ui controls
 		ledMatrixPanelModel.setExperimentStarted(source.isSelected());
 		return new AbstractTask<Void, Void>(START_EXPERIMENT_ACTION) {
 
 			private void doPeriodicToggle(LedSettings ledSettings, 
 					int flickerFrequency, long timePerLed) {
 				try {
 					if (flickerFrequency == 0) {
 						ledMatrixGfxModel.setIlluminated(true);
 						ledMatrixGfxView.repaint();
 						ledMatrixConnector.toggleLed(ledSettings, true);
 						Thread.sleep(timePerLed);
 						ledMatrixConnector.toggleLed(ledSettings, false);
 						ledMatrixGfxModel.setIlluminated(false);
 					} else {
 						long endTime = System.currentTimeMillis() + timePerLed;
 						while(System.currentTimeMillis() < endTime) {
 							ledMatrixGfxModel.setIlluminated(true);
 							ledMatrixGfxView.repaint();
 							ledMatrixConnector.toggleLed(ledSettings, true);
 							Thread.sleep(flickerFrequency);
 							ledMatrixConnector.toggleLed(ledSettings, false);
 							ledMatrixGfxModel.setIlluminated(false);
 						}
 					}
 					ledMatrixPanelView.repaint();
 				} catch(InterruptedException e) {
 					getLogger().error("Led toggling for " + ledSettings.getLedPosition() + " ended unexpectedly");
 				}
 			}
 
 			protected Void doInBackground() throws Exception {
 				ledMatrixConnector.disableAllLeds();
 				ledMatrixGfxModel.setIlluminated(false);
 				ledMatrixGfxView.repaint();
 				ExperimentSettings experimentSettings = ledMatrixPanelModel.getExperimentSettings();
 				int flickerFrequency = experimentSettings.getFlickerFrequency();
 				long secondsToRun = experimentSettings.getSecondsToRun();
 				message("startMessage", secondsToRun);
 				int ledCount = ledMatrixGfxModel.getHeight() + ledMatrixGfxModel.getWidth(); 
 				long timePerLed = secondsToRun / ledCount;
 				flickerFrequency = flickerFrequency > 0 ? 1000 / flickerFrequency : 0; 
 				for (int i = 0; i < ledMatrixGfxModel.getWidth(); i++) {
 					for (int j = 0; j < ledMatrixGfxModel.getHeight(); j++) {
 						LedPosition ledPosition = LedPosition.ledPositionFor(i, j);
 						LedSettings ledSettings = ledMatrixGfxModel.getLedSettings(ledPosition);
 						message("progressMessage", ledPosition);
 						// TODO we could send out an intensity to thorlabs driver here
 						doPeriodicToggle(ledSettings, flickerFrequency, timePerLed);
 					}
 				}
 				return null;
 			}
 
 		};
 	}
	
	public LedMatrixPanelModel getLedMatrixPanelModel() {
		return ledMatrixPanelModel;
	}
 
	public LedMatrixGfxModel getLedMatrixGfxModel() {
		return ledMatrixGfxModel;
	}

}
