 package topshelf.gwt.editor.client;
 
 import java.util.List;
 
 import topshelf.gwt.common.client.HasInputLock;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.editor.client.HasEditorErrors;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.event.dom.client.HasAllKeyHandlers;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FocusWidget;
 import com.google.gwt.user.client.ui.Focusable;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ValueBoxBase;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * http://stackoverflow.com/questions/7440349/how-to-style-gwt-editor-errors
  * 
  * @author bloo
  *
  * @param <T>
  */
 public abstract class InputEditorBase<T> extends Composite implements HasEditorErrors<T>,
 		LeafValueEditor<T>, Focusable, HasInputLock, HasAllKeyHandlers {
 
 	interface Binder extends UiBinder<Widget, InputEditorBase<?>> {
 		Binder BINDER = GWT.create(Binder.class);
 	}
 
 	@Ignore @UiField Label label;
 	@Ignore @UiField(provided=true) Widget input;
 	@Ignore Focusable focusable;
 	@Ignore HasValue<T> valueHolder;
 	@Ignore @UiField Label error;
 	
 	public Widget getInput() { return input; }
 	
 	String labelStyleDependentName = "error";
 	String inputStyleDependentName = "error";
 	String errorStyleDependentName = "error";
 	boolean clearInputOnError;
 	
 	@SuppressWarnings("unchecked")
 	public InputEditorBase(Composite comp) {
 		if (comp instanceof HasValue) {
 			this.valueHolder = (HasValue<T>)comp;
 		} else {
 			throw new IllegalArgumentException("Needs to implement HasValue");
 		}
 		this.input = comp.asWidget();
 		if (comp instanceof Focusable) {
 			this.focusable = (Focusable)comp;
 		}
 		initWidget(Binder.BINDER.createAndBindUi(this));
 		styles();
 	}
 	
 	public InputEditorBase(ValueBoxBase<T> input) {
 		this.input = input;
 		this.valueHolder = input;
 		this.focusable = input;
 		initWidget(Binder.BINDER.createAndBindUi(this));
 		styles();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public InputEditorBase(FocusWidget fw) {
 		if (fw instanceof HasValue) {
 			this.valueHolder = (HasValue<T>)fw;
 		} else {
 			throw new IllegalArgumentException("Needs to implement HasValue");
 		}
 		this.input = fw;
 		this.focusable = fw;	
 	}
 
 //	public InputEditorBase(FocusWidget ui, HasValue<T> hv) {
 //		this.input = ui;
 //		this.focusable = ui;
 //		this.valueHolder = hv;
 //		initWidget(Binder.BINDER.createAndBindUi(this));
 //		styles();
 //	}
 //	
 //	public InputEditorBase(Widget ui, HasValue<T> hv) {
 //		this.input = ui;
 //		this.valueHolder = hv;
 //		initWidget(Binder.BINDER.createAndBindUi(this));
 //		styles();
 //	}
 
 	private void styles() {
 		setStyleName("ts-InputEditor");
 		this.input.setStyleName("input");
 		this.label.setStyleName("label");
 		this.error.setStyleName("error");
 	}
 	
 	@Override
 	public void setValue(T value) {
 		valueHolder.setValue(value);
 	}
 	
 	@Override
 	public T getValue() {
 		T value = valueHolder.getValue();
 		String str = null != value ? value.toString() : null;
 		if (null == str) {
 			return null;
 		} else if ("".equals(str)) {
 			return null;
 		} else {
 			return value;
 		}
 	}
 	
 	@Override
 	public void showErrors(List<EditorError> errors) {
 		clearError();
 		for (EditorError err : errors) {
 			if (err.getEditor() == this) {
 				showError(err.getMessage());
 				break;
 			}
 		}		
 	}
 	
 	@Override
 	public void setInputLock(boolean lock) {
 		if (input instanceof ValueBoxBase) {
 			((ValueBoxBase<?>)input).setReadOnly(lock);
 		}
 	}
 
 	public void clearError() {
 		label.removeStyleDependentName(labelStyleDependentName);
 		input.removeStyleDependentName(inputStyleDependentName);
 		error.removeStyleDependentName(errorStyleDependentName);
 		error.setText(null);
 	}
 
 	public void showError(String msg) {
 		label.addStyleDependentName(labelStyleDependentName);
 		input.addStyleDependentName(inputStyleDependentName);
 		error.addStyleDependentName(errorStyleDependentName);
 		if (clearInputOnError) {
 			valueHolder.setValue(null);
 		}
 		error.setText(msg);
 	}
 
 	public void setLabel(String labelText) {
 		label.setText(labelText);
 		// use the label to set another styleName
 		String styleName = toCamelCase(labelText);
 		addStyleName("ts-InputEditor-" + styleName);
 	}
 	
 	private String toCamelCase(String s){
 	   String[] parts = s.replaceAll("\\W", " ").split(" ");
 	   String camelCaseString = "";
 	   for (String part : parts){
	      camelCaseString += (part.substring(0, 1).toUpperCase() +
	    		  part.substring(1).toLowerCase());
 	   }
 	   return camelCaseString;
 	}
 
 	public void setLabelStyleName(String labelStyleName) {
 		this.label.setStyleName(labelStyleName);
 	}
 
 	public void setInputStyleName(String inputStyleName) {
 		this.input.setStyleName(inputStyleName);
 	}
 
 	public void setInputStyleDependentName(String inputStyleDependentName) {
 		this.inputStyleDependentName = inputStyleDependentName;
 	}
 
 	public void setErrorStyleName(String errorStyleName) {
 		this.error.setStyleName(errorStyleName);
 	}
 
 	public void setErrorStyleDependentName(
 			String errorStyleDependentName) {
 		this.errorStyleDependentName = errorStyleDependentName;
 	}
 
 	public void setClearInputOnError(boolean clearInputOnError) {
 		this.clearInputOnError = clearInputOnError;
 	}
 	
 	@Override
 	public void setFocus(boolean focused) {
 		if (null != focusable) {
 			focusable.setFocus(focused);
 		}
 	}
 
 	@Override
 	public int getTabIndex() {
 		if (null != focusable) {
 			return focusable.getTabIndex();
 		} else {
 			return -1;
 		}
 	}
 
 	@Override
 	public void setAccessKey(char key) {
 		if (null != focusable) {
 			focusable.setAccessKey(key);
 		}
 	}
 
 	@Override
 	public void setTabIndex(int index) {
 		if (null != focusable) {
 			focusable.setTabIndex(index);
 		}
 	}
 	
 	protected FocusWidget asFocusWidget() {
 		return (FocusWidget)input;
 	}
 	
 	// keyboard controls
 	
 	@Override
 	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
 		return asFocusWidget().addKeyUpHandler(handler);
 	}
 	
 	@Override
 	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
 		return asFocusWidget().addKeyDownHandler(handler);
 	}
 	
 	@Override
 	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
 		return asFocusWidget().addKeyPressHandler(handler);
 	}
 }
