 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.ui.components;
 
 import java.net.URL;
 import java.text.DecimalFormat;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.WidgetProperties;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.beans.ObservableModel;
 
 public class NumberEditorControl extends Composite {
 	
 	private static final Logger logger = LoggerFactory.getLogger(NumberEditorControl.class);
 	
 	public static final String EDITABLE_PROP_NAME = "editable";
 
 	private static final String ICONS_PATH = "icons/";
 	private static final int LARGE_INCREMENT_WIDTH_PADDING = 6;
 	private static final int MIN_STEP_LABEL_WIDTH = 43;
 	
	protected static final int DEFAULT_DECIMAL_PLACES = 3;
 	protected Object targetObject;
 	
 	private String propertyName;
 
 	private Label numberLabel;
 	private Control incrementButton;
 	private Control decrementButton;
 
 	private Image upArrowIcon;
 	private Image downArrowIcon;
 
 	private final StackLayout layout;
 	private Composite editorComposite;
 	private Text stepText;
 	private NumberEditorText motorPositionEditorText;
 
 	private Binding numberLabelValueBinding;
 
 	private boolean commitOnOutOfFocus = true;
 
 	protected NumberEditorWidgetModel controlModel;
 
 	protected final DataBindingContext ctx = new DataBindingContext();
 	private final boolean useSpinner;
 	protected boolean horizonalSpinner;
 	private StackLayout stepLayout;
 	private Label incrementText;
 
 	private boolean isEditing;
 
 	private Composite incrementComposite;
 	private Binding incrementTextBinding;
 	private Binding incrementLabelBinding;
 
 	private Color disabledColor;
 	private Color enabledColor;
 
 	private static final int INITIAL_STEP = (int) (0.1 * Math.pow(10, DEFAULT_DECIMAL_PLACES));
 
 	private final StepListener incrementStepListener = new StepListener(true);
 	private final StepListener decrementStepListener = new StepListener(false);
 	private boolean binded;
 	private Binding decrementButtonEditableBinding;
 	private Binding incrementButtonEditableBinding;
 	private IConverter modelToTargetConverter;
 	private IConverter targetToModelConverter;
 	private IValidator modelToTargetValidator;
 	private IValidator targetToModelValidator;
 
 	public NumberEditorControl(final Composite parent, int style, Object targetObject, String propertyName, boolean useSpinner) throws Exception {
 		this(parent, style, targetObject, propertyName, useSpinner, false);
 	}
 
 	public NumberEditorControl(final Composite parent, int style, Object targetObject, String propertyName, boolean useSpinner,
 			boolean horizonalSpinner) throws Exception {
 		super(parent, style);
 		this.horizonalSpinner = horizonalSpinner;
 		this.useSpinner = useSpinner;
 		layout = new StackLayout();
 		this.setLayout(layout);
 		setupControls();
 		if (targetObject != null & propertyName != null) {
 			setModel(targetObject, propertyName);
 		}
 		if (this.useSpinner) {
 			setupIncrementCompWidthHint();
 		}
 		this.setTabList(new Control[]{editorComposite});
 	}
 
 	public NumberEditorControl(final Composite parent, int style, boolean useSpinner) throws Exception {
 		this(parent, style, null, null, useSpinner);
 	}
 
 	private NumberEditorWidgetModel createModel() throws Exception {
 		NumberEditorWidgetModel numberEditorWidgetModel = new NumberEditorWidgetModel();
 		try {
 			Class<?> objectType = PropertyUtils.getPropertyType(targetObject, propertyName);
 			if (objectType.equals(double.class)) {
 				numberEditorWidgetModel.setBindingPropertyType(objectType);
 				numberEditorWidgetModel.setDigits(DEFAULT_DECIMAL_PLACES);
 				numberEditorWidgetModel.setIncrement(INITIAL_STEP);
 			} else if  (objectType.equals(int.class)) {
 				numberEditorWidgetModel.setBindingPropertyType(objectType);
 				numberEditorWidgetModel.setIncrement(1);
 			} else {
 				throw new Exception("Unsupported property type");
 			}
 		} catch (Exception e) {
 			throw new Exception("Unable to process the perperty", e);
 		}
 		return numberEditorWidgetModel;
 	}
 
 
 	private void bind() {
 		IObservableValue objectValue = BeanProperties.value(propertyName).observe(targetObject);
 		ISWTObservableValue textValue = WidgetProperties.text().observe(numberLabel);
 		UpdateValueStrategy modelToTargetUpdateValueStrategy = new UpdateValueStrategy() {
 			@Override
 			public Object convert(Object value) {
 				if (modelToTargetConverter != null) {
 					value = modelToTargetConverter.convert(value);
 				}
 				return getFormattedText(value);
 			}
 		};
 		if (this.modelToTargetValidator != null) {
 			modelToTargetUpdateValueStrategy.setBeforeSetValidator(modelToTargetValidator);
 		}
		numberLabelValueBinding = ctx.bindValue(textValue, objectValue, null, modelToTargetUpdateValueStrategy);
 		if (useSpinner) {
 			incrementLabelBinding = ctx.bindValue(
 					WidgetProperties.text().observe(incrementText),
 					BeanProperties.value(NumberEditorWidgetModel.INCREMENT_PROP_NAME).observe(controlModel),
 					new UpdateValueStrategy() {
 						@Override
 						public Object convert(Object value) {
 							if (targetToModelConverter != null) {
 								value = targetToModelConverter.convert(value);
 							}
 							if (controlModel.getBindingPropertyType().equals(double.class)) {
 								return (((Number) value).doubleValue() * (int) Math.pow(10, controlModel.getDigits()));
 							}
 							return super.convert(value);
 						}
 					}, new UpdateValueStrategy() {
 						@Override
 						public Object convert(Object value) {
 							if (modelToTargetConverter != null) {
 								value = modelToTargetConverter.convert(value);
 							}
 							if (controlModel.getBindingPropertyType().equals(double.class)) {
 								double incrementValue = controlModel.getIncrement() / Math.pow(10, controlModel.getDigits());
 								return roundDoubletoString(incrementValue, controlModel.getDigits());
 							}
 							return value.toString();
 						}
 					});
 
 			decrementButtonEditableBinding = ctx.bindValue(
 					WidgetProperties.enabled().observe(decrementButton),
 					BeanProperties.value(EDITABLE_PROP_NAME).observe(controlModel));
 
 
 			incrementButtonEditableBinding = ctx.bindValue(
 					WidgetProperties.enabled().observe(incrementButton),
 					BeanProperties.value(EDITABLE_PROP_NAME).observe(controlModel));
 
 			incrementText.addListener(SWT.MouseUp, openStepEditorListener);
 			incrementText.addMouseTrackListener(editableMouseListener);
 			incrementText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 
 			incrementButton.addListener(SWT.MouseUp, incrementStepListener);
 			incrementButton.addMouseListener(incrementEnabledListener);
 			incrementButton.addMouseTrackListener(editableIncrementMouseListener);
 
 			decrementButton.addListener(SWT.MouseUp, decrementStepListener);
 			decrementButton.addMouseListener(incrementEnabledListener);
 			decrementButton.addMouseTrackListener(editableIncrementMouseListener);
 		}
 		numberLabel.addMouseTrackListener(editableMouseListener);
 		numberLabel.addListener(SWT.MouseUp, openEditorListener);
 		numberLabel.setBackground(enabledColor);
 
 		binded = true;
 	}
 
 	private void unbind() {
 		if (controlModel == null) {
 			return;
 		}
 		if (numberLabelValueBinding!=null && !numberLabelValueBinding.isDisposed()) {
 			numberLabelValueBinding.dispose();
 			ctx.removeBinding(numberLabelValueBinding);
 		}
 		if (useSpinner) {
 			if (incrementLabelBinding != null && !incrementLabelBinding.isDisposed()) {
 				incrementLabelBinding.dispose();
 				ctx.removeBinding(incrementLabelBinding);
 			}
 
 			if (decrementButtonEditableBinding != null && !decrementButtonEditableBinding.isDisposed()) {
 				decrementButtonEditableBinding.dispose();
 				ctx.removeBinding(decrementButtonEditableBinding);
 			}
 
 			if (incrementButtonEditableBinding != null && !incrementButtonEditableBinding.isDisposed()) {
 				incrementButtonEditableBinding.dispose();
 				ctx.removeBinding(incrementButtonEditableBinding);
 			}
 
 			incrementText.removeListener(SWT.MouseUp, openStepEditorListener);
 			incrementText.removeMouseTrackListener(editableMouseListener);
 			incrementText.setBackground(disabledColor);
 
 			incrementButton.removeListener(SWT.MouseUp, incrementStepListener);
 			incrementButton.removeMouseListener(incrementEnabledListener);
 			incrementButton.removeMouseTrackListener(editableIncrementMouseListener);
 
 			decrementButton.removeListener(SWT.MouseUp, decrementStepListener);
 			decrementButton.removeMouseListener(incrementEnabledListener);
 			decrementButton.removeMouseTrackListener(editableIncrementMouseListener);
 			incrementText.setText("");
 
 		}
 		numberLabel.removeMouseTrackListener(editableMouseListener);
 		numberLabel.removeListener(SWT.MouseUp, openEditorListener);
 		numberLabel.setText("");
 		numberLabel.setBackground(disabledColor);
 
 		binded = false;
 	}
 
 	public void setModel(Object targetObject, String propertyName) throws Exception {
 		if (binded) {
 			unbind();
 			controlModel = null;
 			this.targetObject = null;
 			this.propertyName = null;
 		}
 		if (targetObject != null & propertyName != null) {
 			this.targetObject = targetObject;
 			this.propertyName = propertyName;
 			controlModel = createModel();
 			bind();
 		}
 	}
 
 	@Override
 	public void dispose() {
 		if (upArrowIcon != null && !upArrowIcon.isDisposed()) {
 			upArrowIcon.dispose();
 		}
 		if (downArrowIcon != null && !downArrowIcon.isDisposed()) {
 			downArrowIcon.dispose();
 		}
 
 		if (disabledColor != null && !disabledColor.isDisposed()) {
 			disabledColor.dispose();
 		}
 		ctx.dispose();
 		super.dispose();
 	}
 
 	public void setRange(int minValue, int maxValue) {
 		controlModel.setMinValue(minValue);
 		controlModel.setMaxValue(maxValue);
 	}
 	
 	public void setRange(double minValue, double maxValue) {
 		controlModel.setMinValue(minValue);
 		controlModel.setMaxValue(maxValue);
 	}
 
 	public void setDigits(int value) throws NumberFormatException {
 		if (!controlModel.getBindingPropertyType().equals(double.class)) {
 			throw new NumberFormatException("Invalid data type set to digits");
 		}
 		controlModel.setDigits(value);
 		numberLabelValueBinding.updateModelToTarget();
 		if (useSpinner) {
 			incrementLabelBinding.updateModelToTarget();
 		}
 	}
 
 	public void setIncrement(int value) throws Exception {
 		if (!useSpinner) {
 			throw new Exception("Increment spinner is not used");
 		}
 		controlModel.setIncrement(value);
 	}
 
 	public static final String UNIT_PROP_NAME = "unit";
 	public void setUnit(String value) {
 		controlModel.setUnit(value);
 		numberLabelValueBinding.updateModelToTarget();
 		if (useSpinner) {
 			incrementLabelBinding.updateModelToTarget();
 		}
 	}
 
 	@Override
 	public void setToolTipText(String toolTopText) {
 		numberLabel.setToolTipText(toolTopText);
 	}
 
 	public void setConverters(IConverter modelToTargetConverter, IConverter targetToModelConverter) {
 		this.modelToTargetConverter = modelToTargetConverter;
 		this.targetToModelConverter = targetToModelConverter;
 	}
 	
 	public void setValidators(IValidator modelToTargetValidator, IValidator targetToModelValidator) {
 		this.modelToTargetValidator = modelToTargetValidator;
 		this.targetToModelValidator = targetToModelValidator;
 	}
 
 	public boolean isEditable() {
 		return controlModel.isEditable();
 	}
 
 	public void setEditable(boolean value) {
 		controlModel.setEditable(value);
 		if (!numberLabel.isDisposed()) {
 			Color color = controlModel.isEditable() ? Display.getDefault().getSystemColor(SWT.COLOR_WHITE) : this.disabledColor;
 			numberLabel.setBackground(color);
 		}
 	}
 
 	public boolean isEditing() {
 		return isEditing;
 	}
 
 	public boolean isCommitOnOutOfFocus() {
 		return commitOnOutOfFocus;
 	}
 
 	public void setCommitOnOutOfFocus(boolean commitOnOutOfFocus) {
 		this.commitOnOutOfFocus = commitOnOutOfFocus;
 	}
 
 	private final MouseListener incrementEnabledListener = new MouseListener() {
 		private Color color;
 		@Override
 		public void mouseUp(MouseEvent e) {
 			if (controlModel.isEditable()) {
 				((Control) e.getSource()).setBackground(color);
 			}
 		}
 		@Override
 		public void mouseDown(MouseEvent e) {
 			if (controlModel.isEditable()) {
 				color = ((Control) e.getSource()).getBackground();
 				((Control) e.getSource()).setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 			}
 		}
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {}
 	};
 
 
 	private final MouseTrackListener editableMouseListener = new MouseTrackListener() {
 		private Cursor cursor;
 		@Override
 		public void mouseHover(MouseEvent e) {}
 
 		@Override
 		public void mouseExit(MouseEvent e) {
 			if (cursor != null) {
 				((Control) e.getSource()).setCursor(cursor);
 			}
 		}
 		@Override
 		public void mouseEnter(MouseEvent e) {
 			if (!controlModel.isEditable()) {
 				return;
 			}
 			cursor = ((Control) e.getSource()).getCursor();
 			((Control) e.getSource()).setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
 		}
 	};
 
 	private final MouseTrackListener editableIncrementMouseListener = new MouseTrackListener() {
 		private Cursor cursor;
 		@Override
 		public void mouseHover(MouseEvent e) {}
 		@Override
 		public void mouseExit(MouseEvent e) {
 			if (cursor != null) {
 				((Control) e.getSource()).setCursor(cursor);
 			}
 		}
 		@Override
 		public void mouseEnter(MouseEvent e) {
 			if (!controlModel.isEditable()) {
 				cursor = ((Control) e.getSource()).getCursor();
 				((Control) e.getSource()).setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_NO));
 			}
 		}
 	};
 
 	protected void setupControls() {
 		disabledColor = new Color (this.getDisplay(), 230, 230, 230);
 		enabledColor = this.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 		editorComposite = new Composite(this, SWT.None);
 		editorComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		int columns = (useSpinner) ? 3 : 1;
 		GridLayout grid = new GridLayout(columns, false);
 		removeMargins(grid);
 		editorComposite.setLayout(grid);
 		numberLabel = new Label(editorComposite, SWT.BORDER);
 		GridData gridData = new GridData(GridData.FILL,GridData.CENTER, true, false);
 		gridData.heightHint = 23;
 		numberLabel.setLayoutData(gridData);
 		// TODO Use binding
 
 		if (useSpinner) {
 			Composite spinners = new Composite(editorComposite, SWT.None);
 			if (horizonalSpinner) {
 				grid = new GridLayout(2, true);
 				removeMargins(grid);
 				spinners.setLayout(grid);
 				gridData = new GridData(GridData.END, GridData.CENTER, false, false);
 				gridData.heightHint = 26;
 				spinners.setLayoutData(gridData);
 				decrementButton =  new Button(spinners, SWT.FLAT);
 				decrementButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 				((Button) decrementButton).setText("-");
 
 				incrementButton = new Button(spinners, SWT.FLAT);
 				incrementButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 				((Button) incrementButton).setText("+");
 			} else {
 				grid = new GridLayout(1, false);
 				removeMargins(grid);
 				spinners.setLayout(grid);
 				gridData = new GridData(GridData.END, GridData.BEGINNING, false, false);
 				gridData.heightHint = 27;
 				gridData.widthHint = 25;
 				spinners.setLayoutData(gridData);
 				incrementButton = new Label(spinners, SWT.BORDER);
 				upArrowIcon = getImageDescriptor("up_arrow.png").createImage();
 				((Label) incrementButton).setImage(upArrowIcon);
 				incrementButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 				decrementButton = new Label(spinners, SWT.BORDER);
 				decrementButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 				downArrowIcon = getImageDescriptor("down_arrow.png").createImage();
 				((Label) decrementButton).setImage(downArrowIcon);
 			}
 			incrementComposite = new Composite(editorComposite, SWT.None);
 			gridData = new GridData(SWT.END, SWT.CENTER, false, false);
 			gridData.heightHint = 26;
 			gridData.widthHint = MIN_STEP_LABEL_WIDTH;
 			incrementComposite.setLayoutData(gridData);
 			stepLayout = new StackLayout();
 			incrementComposite.setLayout(stepLayout);
 			incrementText = new Label(incrementComposite, SWT.BORDER);
 			incrementText.setAlignment(SWT.CENTER);
 			stepLayout.topControl = incrementText;
 		}
 		layout.topControl = editorComposite;
 //		
 //		numberLabel.addFocusListener(new FocusListener() {
 //			
 //			@Override
 //			public void focusLost(FocusEvent e) {
 //				// TODO Auto-generated method stub
 //				System.out.println("test");
 //			}
 //			
 //			@Override
 //			public void focusGained(FocusEvent e) {
 //				openEditor();
 //			}
 //		});
 	}
 
 	int getCompositeWidth(Composite cmp, String text){
 		
 		GC gc = new GC(cmp);
 		Point point = gc.stringExtent(text);
 		gc.dispose();
 		return point.x;
 	}
 	int getIncrementCompWidth(){
 		double incrementValue = controlModel.getIncrement() / Math.pow(10, controlModel.getDigits());
 		int widthOfText = getCompositeWidth(incrementComposite, roundDoubletoString(incrementValue, controlModel.getDigits()));
 		int width = MIN_STEP_LABEL_WIDTH;
 			
 		if (widthOfText > width) {
 			width = MIN_STEP_LABEL_WIDTH + LARGE_INCREMENT_WIDTH_PADDING;
 		}
 		return width;
 	}
 	void setupIncrementCompWidthHint(){
 		GridData gridData = (GridData) incrementComposite.getLayoutData();
 		gridData.widthHint = getIncrementCompWidth();
 		incrementComposite.layout();
 		incrementComposite.getParent().layout();		
 	}
 	@Override
 	public boolean setFocus() {
 		System.out.println(this.numberLabel.getText());
 			return false;
 	}
 
 	private class StepListener implements Listener {
 		private final boolean isIncrement;
 		public StepListener(boolean isIncrement) {
 			this.isIncrement = isIncrement;
 		}
 		@Override
 		public void handleEvent(Event event) {
 			if (controlModel.isEditable()) {
 				try {
 					if (controlModel.getBindingPropertyType().equals(double.class)) {
 						double value = ((Double) PropertyUtils.getProperty(targetObject, propertyName)).doubleValue();
 						double increment =  controlModel.getIncrement() / Math.pow(10, controlModel.getDigits());
 						if (isIncrement) {
 							if (!controlModel.isRangeSet()) {
 								PropertyUtils.setProperty(targetObject, propertyName, value + increment);
 							} else  {
 								double incremented = value + increment;
 								if (incremented <= controlModel.getMaxValue().doubleValue()) {
 									PropertyUtils.setProperty(targetObject, propertyName, incremented);
 								}
 							}
 						} else {
 							if (!controlModel.isRangeSet()) {
 								PropertyUtils.setProperty(targetObject, propertyName, value - increment);
 							} else  {
 								double decremented = value - increment;
 								if (decremented >= controlModel.getMinValue().doubleValue()) {
 									PropertyUtils.setProperty(targetObject, propertyName, decremented);
 								}
 							}
 						}
 					} else if (controlModel.getBindingPropertyType().equals(int.class)) {
 						int value = ((Number) PropertyUtils.getProperty(targetObject, propertyName)).intValue();
 						int increment =  controlModel.getIncrement() / (int) Math.pow(10, controlModel.getDigits());
 						if (isIncrement) {
 							if (!controlModel.isRangeSet()) {
 								PropertyUtils.setProperty(targetObject, propertyName, value + increment);
 							} else  {
 								double incremented = value + increment;
 								if (incremented <= controlModel.getMaxValue().intValue()) {
 									PropertyUtils.setProperty(targetObject, propertyName, (int) incremented);
 								}
 							}
 						} else {
 							if (!controlModel.isRangeSet()) {
 								PropertyUtils.setProperty(targetObject, propertyName, value - increment);
 							} else  {
 								double decremented = value - increment;
 								if (decremented >= controlModel.getMinValue().intValue()) {
 									PropertyUtils.setProperty(targetObject, propertyName, (int) decremented);
 								}
 							}
 						}
 					}
 				} catch (Exception e) {
 					logger.error("Error binding view", e);
 				}
 			}
 		}
 	}
 
 	private final Listener openEditorListener = new Listener() {
 		@Override
 		public void handleEvent(Event event) {
 			openEditor();
 		}
 	};
 
 	private void openEditor() {
 		if (controlModel.isEditable()) {
 			motorPositionEditorText = new NumberEditorText(NumberEditorControl.this, SWT.None);
 			GridData gridData = new GridData(SWT.FILL,SWT.BEGINNING, true, false);
 			motorPositionEditorText.setLayoutData(gridData);
 			layout.topControl = motorPositionEditorText;
 			NumberEditorControl.this.layout();
 			motorPositionEditorText.addDisposeListener(new DisposeListener() {
 				@Override
 				public void widgetDisposed(DisposeEvent e) {
 					isEditing = false;
 					motorPositionEditorText = null;
 					layout.topControl = editorComposite;
 					NumberEditorControl.this.layout(true, true);
 				}
 			});
 			isEditing = true;
 		}
 	}
 	
 	private final Listener openStepEditorListener = new Listener() {
 		@Override
 		public void handleEvent(Event event) {
 			if (controlModel.isEditable()) {
 				stepText = new Text(incrementComposite, SWT.BORDER);
 				incrementTextBinding = ctx.bindValue(
 						WidgetProperties.text(SWT.Modify).observe(stepText),
 						BeanProperties.value(NumberEditorWidgetModel.INCREMENT_PROP_NAME).observe(controlModel),
 						new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST).setConverter(new IConverter() {
 							@Override
 							public Object getToType() {
 								return int.class;
 							}
 							@Override
 							public Object getFromType() {
 								return String.class;
 							}
 							@Override
 							public Object convert(Object fromObject) {
 								if (targetToModelConverter != null) {
 									fromObject = targetToModelConverter.convert(fromObject);
 								}
 								if (controlModel.getBindingPropertyType().equals(double.class)) {
 									return (int) (Double.parseDouble((String) fromObject) * (int) Math.pow(10, controlModel.getDigits()));
 								}
 								return Integer.parseInt((String) fromObject);
 							}
 						}), new UpdateValueStrategy() {
 							@Override
 							public Object convert(Object value) {
 								if (modelToTargetConverter != null) {
 									value = modelToTargetConverter.convert(value);
 								}
 								if (controlModel.getBindingPropertyType().equals(double.class)) {
 									double incrementValue = controlModel.getIncrement() / Math.pow(10, controlModel.getDigits());
 									return roundDoubletoString(incrementValue, controlModel.getDigits());
 								}
 								return super.convert(value);
 							}
 						});
 				ControlDecorationSupport.create(incrementTextBinding, SWT.TOP | SWT.RIGHT);
 				stepText.addModifyListener(new ModifyListener() {
 					@Override
 					public void modifyText(ModifyEvent e) {
 						incrementTextBinding.validateTargetToModel();
 					}
 				});
 				stepText.addTraverseListener(new TraverseListener() {
 					@Override
 					public void keyTraversed(TraverseEvent event) {
 						if (event.detail == SWT.TRAVERSE_RETURN) {
 							updateIncrementChangesAndDispose(true);
 						}
 						if (event.detail == SWT.TRAVERSE_ESCAPE) {
 							updateIncrementChangesAndDispose(false);
 						}
 					}
 				});
 				stepText.addFocusListener(new FocusListener() {
 					@Override
 					public void focusLost(FocusEvent e) {
 						updateIncrementChangesAndDispose(true);
 					}
 
 					@Override
 					public void focusGained(FocusEvent e) {
 						// Do nothing
 					}
 				});
 				stepLayout.topControl = stepText;
 				incrementComposite.layout();
 				stepText.setFocus();
 				stepText.selectAll();
 			}
 		}
 	};
 
 	private void updateIncrementChangesAndDispose(boolean saveChanges) {
 		if (saveChanges) {
 			incrementTextBinding.updateTargetToModel();
 		} 
 		else {
 			incrementTextBinding.updateModelToTarget();
 		}
 		IStatus status = (IStatus) incrementTextBinding.getValidationStatus().getValue();
 		if (status.isOK()) {
 			ctx.removeBinding(incrementTextBinding);
 			incrementTextBinding.dispose();
 			incrementTextBinding = null;
 			GC gc = new GC(incrementComposite);
 			double incrementValue = controlModel.getIncrement() / Math.pow(10, controlModel.getDigits());
 			Point point = gc.stringExtent(roundDoubletoString(incrementValue, controlModel.getDigits()));
 			GridData gridData = (GridData) incrementComposite.getLayoutData();
 			if (point.x > MIN_STEP_LABEL_WIDTH) {
 				gridData.widthHint = point.x + LARGE_INCREMENT_WIDTH_PADDING;
 			} 
 			else {
 				gridData.widthHint = MIN_STEP_LABEL_WIDTH;
 			}
 			stepText.dispose();
 			gc.dispose();
 			stepLayout.topControl = incrementText;
 			incrementComposite.layout();
 			incrementComposite.getParent().layout();
 		}
 	}
 
 	private ImageDescriptor getImageDescriptor(String imageName) {
 		Bundle bundle = FrameworkUtil.getBundle(NumberEditorControl.class);
 		URL url = FileLocator.find(bundle,
 				new Path(ICONS_PATH + imageName), null);
 		return ImageDescriptor.createFromURL(url);
 	}
 
 	private static void removeMargins(GridLayout grid) {
 		grid.marginBottom = 0;
 		grid.marginTop = 0;
 		grid.marginHeight = 0;
 		grid.marginLeft = 0;
 		grid.marginRight = 0;
 		grid.verticalSpacing = 0;
 		grid.horizontalSpacing = 0;
 	}
 
 	protected class NumberEditorWidgetModel extends ObservableModel {
 		private boolean editable = true;
 		public static final String MAX_VALUE_PROP_NAME = "maxValue";
 		private Number maxValue;
 		public static final String MIN_VALUE_PROP_NAME = "minValue";
 		private Number minValue;
 		public static final String RANGE_SET_PROP_NAME = "rangeSet";
 		private boolean rangeSet;
 		public static final String DIGITS_PROP_NAME = "digits";
 		private int digits;
 		public static final String INCREMENT_PROP_NAME = "increment";
 		private int increment;
 		private String unit;
 		private Object bindingPropertyType;
 		private DecimalFormat decimalFormat;
 
 		public boolean isEditable() {
 			return editable;
 		}
 		
 		public void setEditable(boolean value) {
 			firePropertyChange(EDITABLE_PROP_NAME, editable, editable = value);
 		}
 		
 		public Number getMaxValue() {
 			return maxValue;
 		}
 		
 		public void setMaxValue(Number value) {
 			firePropertyChange(MAX_VALUE_PROP_NAME, maxValue, maxValue = value);
 			firePropertyChange(RANGE_SET_PROP_NAME, rangeSet, rangeSet = true);
 		}
 		
 		public Number getMinValue() {
 			return minValue;
 		}
 		
 		public void setMinValue(Number value) {
 			firePropertyChange(MIN_VALUE_PROP_NAME, minValue, minValue = value);
 			firePropertyChange(RANGE_SET_PROP_NAME, rangeSet, rangeSet = true);
 		}
 		
 		public boolean isRangeSet() {
 			return rangeSet;
 		}
 		
 		public int getDigits() {
 			return digits;
 		}
 		
 		public void setDigits(int value) {
 			firePropertyChange(DIGITS_PROP_NAME, digits, digits = value);
 			StringBuilder string = new StringBuilder("#.");
 			for (int i = 0; i < digits; i++) {
 				string.append("#");
 			}
 			decimalFormat = new DecimalFormat(string.toString());
 		}
 
 		public String getFormattedValue(double value) {
 			return decimalFormat.format(value);
 		}
 
 		public int getIncrement() {
 			return increment;
 		}
 		
 		public void setIncrement(int value) {
 			firePropertyChange(INCREMENT_PROP_NAME, increment, increment = value);
 		}
 		
 		public String getUnit() {
 			return unit;
 		}
 		
 		public void setUnit(String value) {
 			firePropertyChange(UNIT_PROP_NAME, unit, unit = value);
 		}
 		
 		public Object getBindingPropertyType() {
 			return bindingPropertyType;
 		}
 		
 		public void setBindingPropertyType(Object bindingPropertyType) {
 			this.bindingPropertyType = bindingPropertyType;
 		}
 	}
 
 	private class NumberEditorText extends Composite {
 		private final Text editorText;
 		private final Button editorAcceptButton;
 		private final Button editorCancelButton;
 		private final DataBindingContext editorCtx = new DataBindingContext();
 		private Binding binding;
 		private final ImageDescriptor cancelImageDescriptor = getImageDescriptor("cancel.png");
 		private final Image cancelImage = cancelImageDescriptor.createImage();
 		private final ImageDescriptor acceptImageDescriptor = getImageDescriptor("accept.png");
 		private final Image acceptImage = acceptImageDescriptor.createImage();
 		protected boolean lostFocus;
 		protected boolean cancelOrCommit;
 		private final Listener inFocus;
 		private final FocusListener textOutFocus;
 		public NumberEditorText(Composite parent, int style) {
 			super(parent, style);
 			GridLayout grid = new GridLayout(3, false);
 			removeMargins(grid);
 			this.setLayout(grid);
 			editorText = new Text(this, SWT.BORDER);
 			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
 			editorText.setLayoutData(gridData);
 			editorCancelButton = new Button(this, SWT.NONE);
 
 			editorCancelButton.setImage(cancelImage);
 			gridData = new GridData(SWT.END, SWT.BEGINNING, false, false);
 			editorCancelButton.setLayoutData(gridData);
 			editorAcceptButton = new Button(this, SWT.NONE);
 			editorAcceptButton.setLayoutData(gridData);
 
 			editorAcceptButton.setImage(acceptImage);
 			editorCancelButton.addListener(SWT.Selection, new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					cancelOrCommit = true;
 					updateChangesAndDispose(false);
 				}
 			});
 
 			editorAcceptButton.addListener(SWT.Selection, new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					cancelOrCommit = true;
 					updateChangesAndDispose(true);
 				}
 			});
 
 			bind();
 			editorText.selectAll();
 			editorText.setFocus();
 			inFocus = new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					if (lostFocus & event.widget != editorText & event.widget != editorCancelButton & event.widget != editorAcceptButton & !cancelOrCommit)
 						NumberEditorText.this.updateChangesAndDispose(commitOnOutOfFocus);
 				}
 			};
 			textOutFocus = new FocusListener() {
 				@Override
 				public void focusLost(FocusEvent e) {
 					lostFocus = true;
 				}
 
 				@Override
 				public void focusGained(FocusEvent e) {
 					// Do nothing
 				}
 			};
 			getDisplay().addFilter(SWT.FocusIn, inFocus);
 			editorText.addFocusListener(textOutFocus);
 		}
 
 		private void bind() {
 			IObservableValue objectValue = BeanProperties.value(propertyName).observe(targetObject);
 			ISWTObservableValue textValue = WidgetProperties.text().observe(editorText);
 
 			UpdateValueStrategy targetToModelUpdateValueStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST);
 			if (targetToModelValidator != null) {
 				targetToModelUpdateValueStrategy.setBeforeSetValidator(targetToModelValidator);
 			} else {
 				if (controlModel.isRangeSet()) {
 					targetToModelUpdateValueStrategy.setBeforeSetValidator(new IValidator() {
 						@Override
 						public IStatus validate(Object value) {
 							if (targetToModelConverter != null)
 								value = targetToModelConverter.convert(value);
 							// TODO Max and min are int, review
 							if (controlModel.getBindingPropertyType().equals(double.class)) {
 								if (((Number) value).doubleValue() >= controlModel.getMinValue().doubleValue() & ((Number) value).doubleValue() <= controlModel.getMaxValue().doubleValue())
 									return ValidationStatus.ok();
 								return ValidationStatus.error("Out of range");
 							} else if (controlModel.getBindingPropertyType().equals(int.class)) {
 								if (((Number) value).intValue() >= controlModel.getMinValue().intValue() & ((Number) value).intValue() <= controlModel.getMaxValue().intValue())
 									return ValidationStatus.ok();
 								return ValidationStatus.error("Out of range");
 							}
 							return ValidationStatus.error("Unknown type");
 						}
 					});
 				}
 			}
 			if (targetToModelConverter != null) {
 				targetToModelUpdateValueStrategy.setConverter(targetToModelConverter);
 			}
 
 			binding = editorCtx.bindValue(textValue, objectValue, targetToModelUpdateValueStrategy, new UpdateValueStrategy() {
 				@Override
 				public Object convert(Object value) {
 					if (modelToTargetConverter != null)
 						value = modelToTargetConverter.convert(value);
 					if (controlModel.getBindingPropertyType().equals(double.class))
 						return roundDoubletoString(((Number) value).doubleValue(), controlModel.getDigits());
 					return super.convert(value);
 				}
 			});
 
 			editorText.addModifyListener(new ModifyListener() {
 				@Override
 				public void modifyText(ModifyEvent e) {
 					binding.validateTargetToModel();
 				}
 			});
 			editorText.addTraverseListener(new TraverseListener() {
 				@Override
 				public void keyTraversed(TraverseEvent event) {
 					if (event.detail == SWT.TRAVERSE_RETURN)
 						updateChangesAndDispose(true);
 					if (event.detail == SWT.TRAVERSE_ESCAPE)
 						updateChangesAndDispose(false);
 				}
 			});
 			ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
 			binding.getValidationStatus().addValueChangeListener(new IValueChangeListener() {
 				@Override
 				public void handleValueChange(ValueChangeEvent event) {
 					IStatus status = (IStatus) binding.getValidationStatus().getValue();
 					editorAcceptButton.setEnabled(status.isOK() || status.getSeverity() == IStatus.INFO);
 				}
 			});
 		}
 
 		@Override
 		public void dispose() {
 			getDisplay().removeFilter(SWT.FocusIn, inFocus);
 			editorText.removeFocusListener(textOutFocus);
 			editorCtx.dispose();
 			acceptImage.dispose();
 			cancelImage.dispose();
 			super.dispose();
 		}
 
 		private void updateChangesAndDispose(boolean saveChanges) {
 			if (saveChanges)
 				binding.updateTargetToModel();
 			else
 				binding.updateModelToTarget();
 			IStatus status = (IStatus) binding.getValidationStatus().getValue();
 			if (status.isOK() || status.getSeverity() == IStatus.INFO)
 				NumberEditorText.this.dispose();
 		}
 	}
 
 	protected static String roundDoubletoString(double value, int digits) {
 		return String.format("%." + digits + "f", value);
 	}
 
 	protected String getFormattedText(Object value) {
 		String formattedValue = null;
 		if (controlModel.getBindingPropertyType().equals(double.class))
 			formattedValue = controlModel.getFormattedValue(((Number) value).doubleValue());
 		else if (controlModel.getBindingPropertyType().equals(int.class))
 			formattedValue = Integer.toString(((Number) value).intValue());
 		if (controlModel.getUnit() != null)
 			return formattedValue + " " + controlModel.getUnit();
 		return formattedValue;
 	}
 
 	public String _getTextForTesting() {
 		return numberLabel.getText();
 	}
 	
 }
