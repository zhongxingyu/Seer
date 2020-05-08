 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito.client.widgets.form;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import burrito.client.widgets.layout.VerticalSpacer;
 import burrito.client.widgets.validation.HasValidators;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasChangeHandlers;
 import com.google.gwt.event.dom.client.HasKeyDownHandlers;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 
 @SuppressWarnings("deprecation")
 public abstract class EditForm extends Composite {
 
 	
 
 	public static interface LoadedCallback {
 		/**
 		 * Called when a job has finished
 		 */
 		void done();
 	}
 
 	public static interface SaveCallback {
 		/**
 		 * The save operation succeeded
 		 */
 		void success();
 
 		void partialSuccess(String warning);
 
 		/**
 		 * The save operation failed for some reason
 		 * 
 		 * @param message
 		 *            an error message
 		 */
 		void failed(String message);
 
 	}
 
 	public static interface SaveCancelListener {
 		/**
 		 * Called when the cancel button is pressed
 		 */
 		void onCancel();
 
 		/**
 		 * Called when the save button has been pressed and a succesful response
 		 * has been received
 		 */
 		void onSave();
 
 		/**
 		 * Called when the save button has been pressed and there was a partial
 		 * fail
 		 * 
 		 * @param warning
 		 */
 		void onPartialSave(String warning);
 
 	}
 
 	static class InfoMessage extends Label {
 
 		public InfoMessage() {
 			super();
 			addStyleName("k5-InfoMessage");
 		}
 
 		@Override
 		public void setText(String text) {
 			super.setText(text);
 			if (text == null) {
 				setVisible(false);
 			} else {
 				setVisible(true);
 			}
 		}
 	}
 
 	private EditFormMessages messages = GWT.create(EditFormMessages.class);
 	private DockPanel dock = new DockPanel();
 	private FlexTable main = new FlexTable();
 	private int currentRow = 0;
 	private DeckPanel wrapper = new DeckPanel();
 	private List<HasValidators> validateables = new ArrayList<HasValidators>();
 	private Label loading = new Label(messages.loading());
 	private Button save = new Button(messages.save());
 	private Button cancel = new Button(messages.cancel());
 	private InfoMessage infoMessage = new InfoMessage();
 	private SaveCancelListener saveCancelListener;
 	private List<EditFormChangeHandler> changeHandlers;
 	private HashMap<Widget,List<Widget>> companionWidgetsMap = new HashMap<Widget,List<Widget>>();
 	private KeyDownHandler saveEnablerKeyDownAction = new KeyDownHandler() {
 
 		@Override
 		public void onKeyDown(KeyDownEvent event) {
 			if (event.getNativeKeyCode() == KeyCodes.KEY_TAB) {
 				return;
 			}
 			handleChange();
 		}
 	};
 	private ChangeHandler saveEnablerChangeHandler = new ChangeHandler() {
 
 		@Override
 		public void onChange(ChangeEvent event) {
 			handleChange();
 		}
 	};
 	private ClickHandler saveEnablerClickHandler = new ClickHandler() {
 
 		@Override
 		public void onClick(ClickEvent event) {
 			handleChange();
 		}
 	};
 
 	private boolean newForm = false;
 	private SimplePanel buttonWrapper;
 
 	public EditForm() {
 		save.addStyleName("k5-EditForm-button-save");
 		cancel.addStyleName("k5-EditForm-button-cancel");
		SimplePanel mainWrapper = new SimplePanel();
		mainWrapper.add(main);
		dock.add(mainWrapper, DockPanel.CENTER);
 		buttonWrapper = new SimplePanel();
 		SimplePanel buttonWrapperInner = new SimplePanel();
 		buttonWrapper.add(buttonWrapperInner);
 		HorizontalPanel hp = new HorizontalPanel();
 		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		buttonWrapperInner.addStyleName("k5-EditForm-buttons");
 		
 		// start with save button disabled
 		save.setEnabled(false);
 		save.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				save();
 			}
 		});
 		cancel.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				saveCancelListener.onCancel();
 			}
 		});
 		hp.add(save);
 		hp.add(cancel);
 		hp.add(infoMessage);
 		buttonWrapperInner.setWidget(hp);
 		dock.add(buttonWrapper, DockPanel.SOUTH);
 		wrapper.add(dock);
 		wrapper.add(loading);
 		wrapper.showWidget(0);
 		initWidget(wrapper);
 		addStyleName("k5-EditForm");
		mainWrapper.addStyleName("k5-EditForm-main");
 	}
 
 	/**
 	 * Gets the save button
 	 * 
 	 * @return
 	 */
 	public Button getSaveButton() {
 		return save;
 	}
 
 	/**
 	 * Gets the cancel button
 	 * 
 	 * @return
 	 */
 	public Button getCancelButton() {
 		return cancel;
 	}
 
 	/**
 	 * Sets the cancel listener for this form
 	 * 
 	 * @param cancelListener
 	 */
 	public void setSaveCancelListener(SaveCancelListener cancelListener) {
 		this.saveCancelListener = cancelListener;
 	}
 
 	/**
 	 * Loads this edit form from an id
 	 * 
 	 * @param id
 	 * @param loadedCallback
 	 */
 	public abstract void doLoad(String id, LoadedCallback loadedCallback);
 
 	public final void load(String id) {
 		save.setEnabled(false);
 		for (HasValidators v : validateables) {
 			v.setValidationError(null);
 		}
 		infoMessage.setText(null);
 		// show loading widget
 		wrapper.showWidget(1);
 		if (id == null) {
 			doLoadNew();
 			wrapper.showWidget(0);
 			DeferredCommand.addCommand(new Command() {
 
 				@Override
 				public void execute() {
 					focus();
 					save.setEnabled(true);
 				}
 			});
 		} else {
 			doLoad(id, new LoadedCallback() {
 
 				@Override
 				public void done() {
 					wrapper.showWidget(0);
 					focus();
 					save.setEnabled(true);
 				}
 			});
 		}
 	}
 
 	public abstract void doLoadNew();
 
 	protected void save() {
 		infoMessage.setText(null);
 		for (HasValidators v : validateables) {
 			if (!v.validate()) {
 				v.highlight();
 				infoMessage.setText(messages.thereAreValidationErrors());
 				return;
 			}
 		}
 		save.addStyleName("saving");
 		save.setEnabled(false);
 		save.setText(messages.saving());
 		SaveCallback callback = new SaveCallback() {
 
 			@Override
 			public void success() {
 				save.setText(messages.save());
 				save.removeStyleName("saving");
 				save.setEnabled(true);
 				// infoMessage.setText(messages.yourChangesHaveBeenSaved());
 				if (saveCancelListener != null) {
 					saveCancelListener.onSave();
 				}
 			}
 
 			@Override
 			public void partialSuccess(String warning) {
 				save.setText(messages.save());
 				save.setEnabled(true);
 				save.removeStyleName("saving");
 				if (saveCancelListener != null) {
 					saveCancelListener.onPartialSave(warning);
 				}
 			}
 
 			@Override
 			public void failed(String message) {
 				save.setText(messages.save());
 				save.setEnabled(true);
 				save.removeStyleName("saving");
 				infoMessage.setText(messages.anErrorHasOccured(message));
 			}
 		};
 		if (newForm) {
 			doSaveNew(callback);
 		} else {
 			doSave(callback);
 		}
 
 	}
 
 	/**
 	 * Called when the save button has been clicked. Be sure to calls
 	 * callback.done() when the asynchronous action has been performed. This
 	 * method is called when all validatable fields ({@link HasValidators}) have
 	 * been validated.
 	 * 
 	 * @param saveCallback
 	 */
 	public abstract void doSave(SaveCallback saveCallback);
 
 	/**
 	 * Called when the save button is pressed in an {@link EditForm} that
 	 * handles a new object
 	 * 
 	 * @param saveCallback
 	 */
 	public abstract void doSaveNew(SaveCallback saveCallback);
 
 	public void add(Widget widget, String label, String description) {
 		if (widget instanceof HasValidators) {
 			validateables.add((HasValidators) widget);
 		}
 		if (widget instanceof HasKeyDownHandlers) {
 			((HasKeyDownHandlers) widget)
 					.addKeyDownHandler(saveEnablerKeyDownAction);
 		}
 		if (widget instanceof HasChangeHandlers) {
 			((HasChangeHandlers) widget)
 					.addChangeHandler(saveEnablerChangeHandler);
 		}
 		if (widget instanceof CheckBox) {
 			((CheckBox) widget).addClickHandler(saveEnablerClickHandler);
 		}
 
 		List<Widget> companionWidgets = new ArrayList<Widget>();
 		if (label != null) {
 			Label l = new Label(label);
 			l.addStyleName("k5-EditForm-label");
 			if (!widget.isVisible()) l.setVisible(false);
 			main.setWidget(this.currentRow, 0, l);
 			companionWidgets.add(l);
 		}
 		main.setWidget(this.currentRow, 1, widget);
 		main.getCellFormatter().addStyleName(this.currentRow, 1, "cell-widget");
 		main.getCellFormatter().addStyleName(this.currentRow, 0, "cell-label");
 		if (description != null) {
 			this.currentRow++;
 			Label desc = new Label(description);
 			desc.addStyleName("k5-EditForm-description");
 			if (!widget.isVisible()) desc.setVisible(false);
 			main.setWidget(this.currentRow, 1, desc);
 			main.getCellFormatter().addStyleName(this.currentRow, 1, "cell-description");
 			companionWidgets.add(desc);
 		}
 
 		this.currentRow++;
 		VerticalSpacer spacer = new VerticalSpacer(10);
 		main.setWidget(this.currentRow, 1, spacer);
 		companionWidgets.add(spacer);
 		this.currentRow++;
 		companionWidgetsMap.put(widget, companionWidgets);
 	}
 
 	public void setWidgetVisible(Widget widget, boolean visible) {
 		widget.setVisible(visible);
 		for (Widget companionWidget : companionWidgetsMap.get(widget)) {
 			companionWidget.setVisible(visible);
 		}
 	}
 
 	/**
 	 * Method called when the form is loaded and visible. Normally, the first
 	 * input field in the form is focused.
 	 */
 	public abstract void focus();
 
 	public void loadNew() {
 		newForm = true;
 		load(null);
 	}
 
 	/**
 	 * Clears all fields
 	 */
 	public void clear() {
 		main.clear();
 		validateables.clear();
 	}
 
 	/**
 	 * Adds a value change handler that receives events whenever one of the
 	 * components within this {@link EditForm} has changed its value.
 	 */
 	public void addEditFormChangeHandler(
 			EditFormChangeHandler handler) {
 		if (changeHandlers == null) {
 			changeHandlers = new ArrayList<EditFormChangeHandler>();
 		}
 		changeHandlers.add(handler);
 	}
 
 	private void handleChange() {
 		if (!save.isEnabled()) {
 			save.setEnabled(true);
 		}
 		if (changeHandlers != null) {
 			for (EditFormChangeHandler h : changeHandlers) {
 				h.onChange();
 			}
 		}
 	}
 
 	public void makeButtonsStick(boolean stick) {
 		getElement().getStyle().setPropertyPx("minHeight", getOffsetHeight());
 		Style style = buttonWrapper.getElement().getStyle();
 		buttonWrapper.addStyleName("k5-EditForm-fixedButtons");
 		if (stick) {
 			style.setPosition(Position.FIXED);
 			style.setBottom(0, Unit.PX);
 			style.setLeft(getAbsoluteLeft(), Unit.PX);
 			style.setWidth(getOffsetWidth(), Unit.PX);
 		} else {
 			style.clearPosition();
 			style.clearBottom();
 			style.clearLeft();
 			style.clearWidth();
 		}
 		
 	}
 
 }
