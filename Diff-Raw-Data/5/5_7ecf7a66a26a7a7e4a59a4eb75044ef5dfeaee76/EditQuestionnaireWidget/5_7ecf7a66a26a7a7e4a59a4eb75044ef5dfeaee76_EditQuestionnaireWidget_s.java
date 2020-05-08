 package com.szas.server.gwt.client.widgets;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.szas.data.FieldDataTuple;
 import com.szas.data.FieldTextBoxDataTuple;
 import com.szas.data.QuestionnaireTuple;
 import com.szas.data.FieldTextAreaDataTuple;
 import com.szas.server.gwt.client.sync.StaticGWTSyncer;
 import com.szas.server.gwt.client.universalwidgets.FieldDataWidget;
 import com.szas.server.gwt.client.universalwidgets.UniversalWidget;
 import com.szas.sync.local.LocalDAO;
 
 public class EditQuestionnaireWidget extends UniversalWidget<QuestionnaireTuple> {
 
 	public EditQuestionnaireWidget(QuestionnaireTuple tuple) {
 		super(tuple);
 		initWidget();
 	}
 	public static String NAME = "edituestionnaire";
 
 	private static EditQuestionnaireWidgetUiBinder uiBinder = GWT
 	.create(EditQuestionnaireWidgetUiBinder.class);
 
 	interface EditQuestionnaireWidgetUiBinder extends
 	UiBinder<Widget, EditQuestionnaireWidget> {
 	}
 
 	@UiField Button deleteButton;
 	@UiField TextBox nameTextBox;
 	@UiField Button addItemButton;
 	@UiField ListBox itemTypesListBox;
 	@UiField VerticalPanel verticalPanel;
 
 	private ArrayList<FieldDataTuple> fields;
 
 	private ArrayList<FieldDataWidget> fieldWidgets;
 
 	@Override
 	protected LocalDAO<QuestionnaireTuple> getLocalDAO() {
 		return StaticGWTSyncer.getQuestionnairedao();
 	}
 
 	@Override
 	protected void initWidget() {		
 		initWidget(uiBinder.createAndBindUi(this));
 		itemTypesListBox.addItem("Input field", FieldTextBoxDataTuple.class.getName());
 		itemTypesListBox.addItem("Long input filed", FieldTextAreaDataTuple.class.getName());
 	}
 
 	@Override
 	protected void updateTuple() {
 		tuple.setName(nameTextBox.getText());
 		tuple.setFields(fields);
 		updateFields();
 	}
 
 	@Override
 	protected void updateWidgets() {
 		nameTextBox.setText(tuple.getName());
 		fields = tuple.getFields();
 		fieldWidgets = new ArrayList<FieldDataWidget>();
 		updateList();
 	}
 	
 	private void updateFields() {
 		for (FieldDataWidget widget : fieldWidgets) {
 			widget.updateField();
 		}
 	}
 
 	private void updateList() {
 		updateFields();
 		verticalPanel.clear();
 		fieldWidgets = new ArrayList<FieldDataWidget>();
 		for (FieldDataTuple field : fields) {
 			FieldDataWidget widget;
 			if (field instanceof FieldTextBoxDataTuple) {
 				widget = new FieldTextBoxDataWidget((FieldTextBoxDataTuple) field);
 			} else if (field instanceof FieldTextAreaDataTuple) {
 				widget = new FieldTextAreaDataWidget((FieldTextAreaDataTuple) field);
 			} else {
 				continue;
 			}
 			widget.updateWidget();
 			
 			fieldWidgets.add(widget);
 			if (fieldWidgets.size() % 2 == 0)
 				widget.setStyleName("even");
 			else
 				widget.setStyleName("odd");
 			verticalPanel.add(widget);
 		}
 	}
 
 	@Override
 	protected void setDeleteable(boolean deletable) {
 		deleteButton.setVisible(update);
 	}
 
 	@UiHandler("deleteButton")
 	void onDeleteButtonClick(ClickEvent event) {
 		onDelete();
 	}
 	@UiHandler("saveButton")
 	void onSaveButtonClick(ClickEvent event) {
 		onSave();
 	}
 	@UiHandler("addItemButton")
 	void onAddItemButtonClick(ClickEvent event) {
 		int selected = itemTypesListBox.getSelectedIndex();
 		if (selected == -1)
 			return;
 		String value = itemTypesListBox.getValue(selected);
 		if (value.equals(FieldTextBoxDataTuple.class.getName())) {
 			FieldTextBoxDataTuple field = new FieldTextBoxDataTuple();
 			fields.add(field);
 			updateList();
 		} else if (value.equals(FieldTextAreaDataTuple.class.getName())) {
 			FieldTextAreaDataTuple field = new FieldTextAreaDataTuple();
 			fields.add(field);
 			updateList();
 		} else {
 			return;
 		}
 	}
 }
