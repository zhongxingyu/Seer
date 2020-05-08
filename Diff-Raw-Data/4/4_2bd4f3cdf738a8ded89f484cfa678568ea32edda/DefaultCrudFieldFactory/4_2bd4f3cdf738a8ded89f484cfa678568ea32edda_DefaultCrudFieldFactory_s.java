 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui;
 
import java.awt.Checkbox;
 import java.util.Set;
 
 import org.vaadin.tokenfield.TokenField;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.server.Sizeable.Unit;
 import com.vaadin.ui.AbstractComponent;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.PopupDateField;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TableFieldFactory;
 import com.vaadin.ui.TextArea;
 
 import de.unioninvestment.eai.portal.portlet.crud.config.DateDisplayType;
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.CheckBoxSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.DatePickerSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.SelectSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.TokenFieldSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRowId;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.OptionList;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.SelectionContext;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.SelectionTableColumn;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table.Mode;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.TableColumn;
 import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.TableColumnSelectionContext;
 import de.unioninvestment.eai.portal.support.vaadin.context.Context;
 import de.unioninvestment.eai.portal.support.vaadin.table.DisplaySupport;
 
 /**
  * TableFieldFactory feur die Crud Datentabelle. Delegiert an
  * datentypspezifische Implementierungen von {@link DisplaySupport}.
  * 
  * @author markus.bonsch
  */
 public class DefaultCrudFieldFactory implements TableFieldFactory,
 		CrudFieldFactory {
 
 	private enum Target {
 		FORM, TABLE
 	}
 
 	private static final int DEFAULT_ROWS = 1;
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int HUNDRED = 100;
 
 	private final DataContainer dataContainer;
 
 	private final Table vaadinTable;
 
 	private final de.unioninvestment.eai.portal.portlet.crud.domain.model.Table modelTable;
 
 	private boolean createFormFieldForTable = true;
 
 	/**
 	 * @param vaadinTable
 	 *            die Tabelle
 	 * @param tableColumns
 	 *            Spaltenkonfiguration aus XML
 	 */
 	public DefaultCrudFieldFactory(
 			Table vaadinTable,
 			de.unioninvestment.eai.portal.portlet.crud.domain.model.Table modelTable) {
 		this.vaadinTable = vaadinTable;
 		this.modelTable = modelTable;
 		this.dataContainer = modelTable.getContainer();
 	}
 
 	public void setCreateFormFieldForTable(boolean createFormFieldForTable) {
 		this.createFormFieldForTable = createFormFieldForTable;
 
 	}
 
 	@Override
 	public Field<?> createField(Container container, Object itemId,
 			Object propertyId, Component uiContext) {
 		if (createFormFieldForTable && isSelectedRow(itemId)) {
 			Item item = container.getItem(itemId);
 			Field<?> field = createField(item, propertyId, false, Target.TABLE);
 			if (field != null && field.isReadOnly()) {
 				return null;
 			}
 			return field;
 		}
 		return null;
 	}
 
 	@Override
 	public Field<?> createField(Item item, Object propertyId) {
 		return createField(item, propertyId, true, Target.FORM);
 	}
 
 	private Field<?> createField(Item item, Object propertyId,
 			boolean createReadonlyFields, Target target) {
 		ContainerRow row = dataContainer.convertItemToRow(item, false, true);
 		Property<?> property = item.getItemProperty(propertyId);
 		String propertyIdString = propertyId.toString();
 
 		DisplaySupport displayer = dataContainer
 				.findDisplayer(propertyIdString);
 		if (displayer != null) {
 			boolean readonly = isReadonly(row, propertyId, propertyIdString);
 
 			Field<?> field = getFieldFromDisplayer(property.getType(), row,
 					propertyId, displayer, property, readonly);
 			applyCommonFieldSettings(field, propertyId, target);
 			field.setReadOnly(readonly);
 			return field;
 		}
 		return null;
 	}
 
 	private boolean isReadonly(ContainerRow row, Object propertyId,
 			String propertyIdString) {
 		if (!modelTable.isEditable()) {
 			return true;
 		}
 		if (modelTable.getMode() == Mode.VIEW) {
 			return true;
 		}
 		boolean fieldIsReadonlyByContainer = row.getFields().get(propertyId)
 				.isReadonly();
 		if (fieldIsReadonlyByContainer) {
 			return true;
 		}
 		if (modelTable.getColumns() != null) {
 			if (!modelTable.isRowEditable(row)) {
 				return true;
 			}
 			TableColumn column = modelTable.getColumns().get(propertyIdString);
 			boolean cellIsConfiguredAsReadonly = !column.isEditable(row);
 			if (cellIsConfiguredAsReadonly) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void fillSelections(AbstractSelect select, ContainerRowId rowId,
 			Object propertyId) {
 		OptionListContainer container = createOptionListContainer(rowId,
 				propertyId);
 
 		select.setContainerDataSource(container);
 		select.setItemCaptionMode(ItemCaptionMode.ITEM);
 	}
 
 	private void fillSelections(TokenField tokens, ContainerRowId rowId,
 			Object propertyId) {
 		OptionListContainer container = createOptionListContainer(rowId,
 				propertyId);
 
 		tokens.setContainerDataSource(container);
 		tokens.setTokenCaptionMode(ItemCaptionMode.ITEM);
 	}
 
 	private OptionListContainer createOptionListContainer(ContainerRowId rowId,
 			Object propertyId) {
 		SelectionContext context = new TableColumnSelectionContext(rowId,
 				propertyId.toString());
 		OptionList selection = modelTable.getColumns().getDropdownSelections(
 				(String) propertyId);
 		OptionListContainer container = new OptionListContainer(selection,
 				context);
 		return container;
 	}
 
 	private boolean isSelectedRow(Object itemId) {
 		if (itemId != null && ((Set<?>) vaadinTable.getValue()).size() == 1) {
 			return itemId.equals(((Set<?>) vaadinTable.getValue()).iterator()
 					.next());
 		}
 		return false;
 	}
 
 	private Field<?> getFieldFromDisplayer(Class<?> type, ContainerRow row,
 			Object propertyId, DisplaySupport displayer, Property<?> property,
 			boolean readonly) {
 		if (isComboBox(propertyId, displayer)) {
 			AbstractSelect select = ((SelectSupport) displayer).createSelect(
 					type, propertyId,
 					dataContainer.getFormat(propertyId.toString()));
 			fillSelections(select, row.getId(), propertyId);
 			return select;
 
 		} else if (isTokenField(propertyId, displayer)) {
 			String delimiter = ((SelectionTableColumn) modelTable.getColumns()
 					.get((String) propertyId)).getSeparator();
 			TokenField tokens = ((TokenFieldSupport) displayer)
 					.createTokenField(type, propertyId, delimiter,
 							dataContainer.getFormat(propertyId.toString()));
 			fillSelections(tokens, row.getId(), propertyId);
 			return tokens;
 
 		} else if (isCheckbox(propertyId, displayer)) {
 
 			de.unioninvestment.eai.portal.portlet.crud.domain.model.CheckBoxTableColumn checkBoxModel = modelTable
 					.getColumns().getCheckBox(propertyId.toString());
 
 			CheckBox checkBox = ((CheckBoxSupport) displayer).createCheckBox(
 					type, checkBoxModel.getCheckedValue(),
 					checkBoxModel.getUncheckedValue(),
 					dataContainer.getFormat(propertyId.toString()));
 
 			if (readonly) {
 				checkBox.setEnabled(false);
 			}
 			return checkBox;
 
 		} else if (!readonly && isDatePicker(propertyId, displayer)) {
 			String format = modelTable.getColumns() == null ? null : modelTable
 					.getColumns().get(propertyId.toString()).getDisplayFormat();
 			PopupDateField datePicker = ((DatePickerSupport) displayer)
 					.createDatePicker(type, propertyId, null, format);
 			datePicker.setLocale(Context.getLocale());
 			return datePicker;
 
 		} else {
 			String prompt = null;
 			if (modelTable.getColumns() != null) {
 				prompt = modelTable.getColumns().getInputPrompt(
 						propertyId.toString());
 			}
 
 			return displayer.createField(type, propertyId,
 					isMultiline(propertyId), prompt,
 					dataContainer.getFormat(propertyId.toString()));
 		}
 	}
 
 	private boolean isDatePicker(Object propertyId, DisplaySupport displayer) {
 		String columnName = propertyId.toString();
 		return modelTable.getColumns() != null
 				&& modelTable.getColumns().isDate(columnName)
 				&& modelTable.getColumns().getDateColumn(columnName)
 						.getDateDisplayType() == DateDisplayType.PICKER
 				&& displayer instanceof DatePickerSupport;
 	}
 
 	private boolean isMultiline(Object propertyId) {
 		boolean multiline = false;
 		if (modelTable.getColumns() != null) {
 			multiline = modelTable.getColumns().isMultiline(
 					propertyId.toString());
 		}
 		return multiline;
 	}
 
 	private int getRows(Object propertyId) {
 		if (modelTable.getColumns() != null) {
 			Integer rows = modelTable.getColumns().get(propertyId.toString())
 					.getRows();
 			if (rows != null) {
 				return rows;
 			}
 		}
 		return DEFAULT_ROWS;
 	}
 
 	private boolean isComboBox(Object propertyId, DisplaySupport displayer) {
 		return modelTable.getColumns() != null
 				&& modelTable.getColumns().isComboBox(propertyId.toString())
 				&& displayer instanceof SelectSupport;
 	}
 
 	private boolean isTokenField(Object propertyId, DisplaySupport displayer) {
 		return modelTable.getColumns() != null
 				&& modelTable.getColumns().isTokenfield(propertyId.toString())
 				&& displayer instanceof TokenFieldSupport;
 	}
 
 	private boolean isEditForm() {
 		return modelTable.getColumns() != null
 				&& modelTable.isFormEditEnabled();
 	}
 
 	private boolean isCheckbox(Object propertyId, DisplaySupport displayer) {
 		return modelTable.getColumns() != null
 				&& modelTable.getColumns().isCheckbox(propertyId.toString())
 				&& displayer instanceof CheckBoxSupport;
 	}
 
 	private void applyCommonFieldSettings(Field<?> field, Object propertyId,
 			Target target) {
 		setCaptionAndTooltipIfGiven(field, propertyId, target);
 		field.setWidth(HUNDRED, Unit.PERCENTAGE);
 		field.setBuffered(true);
 		passThroughConversionExceptionMessage(field);
 		if (field instanceof TextArea) {
 			TextArea area = (TextArea) field;
 			if (isEditForm()) {
 				area.setRows(getRows(propertyId));
 			} else {
 				area.setHeight(getRows(propertyId), Unit.EM);
 			}
 		}
 	}
 
 	private void passThroughConversionExceptionMessage(Field<?> field) {
 		if (field instanceof AbstractField<?>) {
 			AbstractField<?> abstractField = (AbstractField<?>) field;
 			abstractField.setConversionError("{1}");
 		}
 	}
 
 	private void setCaptionAndTooltipIfGiven(Field<?> field, Object propertyId,
 			Target target) {
		boolean leaveCaptionEmpty = (field instanceof Checkbox && target == Target.TABLE);
 		if (modelTable.getColumns() != null) {
 			TableColumn column = modelTable.getColumns().get(
 					propertyId.toString());
 			String longTitle = column.getLongTitle();
 			if (longTitle != null) {
 				if (field instanceof AbstractComponent) {
 					((AbstractComponent) field).setDescription(longTitle);
 				}
 			}
 			String title = column.getTitle();
 			if (title != null && !leaveCaptionEmpty) {
 				field.setCaption(title);
 				return;
 			}
 		}
 		if (!leaveCaptionEmpty) {
 			field.setCaption(DefaultFieldFactory
 					.createCaptionByPropertyId(propertyId));
 		}
 	}
 
 }
