 package org.gwtapp.core.client.ui;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.ContainerPanel;
 import com.google.gwt.user.client.ui.HasValue;
 
 public abstract class DelegatedPanel<X, Y> extends ContainerPanel implements
 		HasValue<X> {
 
 	public DelegatedPanel() {
 		this(DOM.createDiv());
 	}
 
 	public DelegatedPanel(Element element) {
 		setElement(element);
 	}
 	
 	public abstract HasValue<Y> getDelegated();
 
 	// cached value
 	private X value = null;
 
 	public abstract X convertToX(Y value);
 
 	public abstract Y convertToY(X value);
 
 	public boolean isDelegateToUpdate(X oldValue, X newValue) {
 		return true;
 	}
 
 	@Override
 	public X getValue() {
 		return value;
 	}
 
 	@Override
 	public void setValue(X value) {
 		setValue(value, false);
 	}
 
 	@Override
 	public void setValue(X value, boolean fireEvents) {
 		boolean isDelegateToUpdate = isDelegateToUpdate(getValue(), value);
 		this.value = value;
 		if (isDelegateToUpdate) {
 			getDelegated().setValue(convertToY(value));
 		}
 		if (fireEvents) {
 			ValueChangeEvent.fire(this, value);
 		}
 	};
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<X> handler) {
 		return addValueChangeHandler(handler, getDelegated());
 	}
 
 	/**
 	 * Delegated panel type is different than this panel type. It is also needed
 	 * to create a custom data converter on X getValue() and setValue(X value,
 	 * boolean fireEvents) methods.
 	 */
 	protected HandlerRegistration addValueChangeHandler(
 			final ValueChangeHandler<X> handler, final HasValue<Y> delegated) {
 		return delegated.addValueChangeHandler(new ValueChangeHandler<Y>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<Y> event) {
 				final HandlerRegistration hr = addHandler(handler,
 						ValueChangeEvent.getType());
 				DelegatedPanel.this
 						.setValue(convertToX(event.getValue()), true);
 				DeferredCommand.addCommand(new Command() {
 					@Override
 					public void execute() {
 						hr.removeHandler();
 					}
 				});
 			}
 		});
 	}
 }
