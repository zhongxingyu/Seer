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
 package de.unioninvestment.eai.portal.portlet.crud.mvp.views;
 
 import static de.unioninvestment.eai.portal.support.vaadin.PortletUtils.getMessage;
 import static java.util.Collections.emptySet;
 import static java.util.Collections.singleton;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.core.task.TaskExecutor;
 import org.vaadin.cssinject.CSSInject;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.Buffered.SourceException;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ConversionException;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.terminal.gwt.server.WebBrowser;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.AbstractSelect.MultiSelectMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Table.ColumnGenerator;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window.Notification;
 
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.BeforeCommitEvent;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.BeforeCommitEventHandler;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table.Mode;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.TableAction;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.TableAction.DownloadActionCallback;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.TableColumn;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.TableColumns;
 import de.unioninvestment.eai.portal.portlet.crud.export.CsvExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.export.DownloadExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.export.ExcelExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.export.ExportDialog;
 import de.unioninvestment.eai.portal.portlet.crud.export.ExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.BLobColumnGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.CrudCellStyleGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.CrudFieldFactory;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.CrudTable;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.CrudTableColumnGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.VaadinCustomColumnGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.VaadinExportableColumnGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.ValidationFieldFactoryWrapper;
 import de.unioninvestment.eai.portal.support.vaadin.PortletApplication;
 import de.unioninvestment.eai.portal.support.vaadin.support.BufferedTable;
 
 /**
  * View-Objekt, dass die Anzeige eine Tabelle kapselt.
  * 
  * @author carsten.mjartan
  */
 @Configurable
 public class DefaultTableView extends VerticalLayout implements TableView {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(DefaultTableView.class);
 
 	public static final int HUNDRET = 100;
 
 	private Button insertButton;
 
 	private Button editButton;
 
 	private Button revertButton;
 
 	/**
 	 * @deprecated Use {@link TableAction#isExportAction()} instead
 	 */
 	private Button excelExportButton;
 
 	/**
 	 * @deprecated Use {@link TableAction#isExportAction()} instead
 	 */
 	private Button csvExportButton;
 
 	private CrudTable table;
 
 	private Button removeButton;
 
 	private Map<String, Button> actionButtons = new HashMap<String, Button>();
 	private Map<Button, TableAction> buttonToTableActionMap = new HashMap<Button, TableAction>();
 
 	private TableView.Presenter presenter;
 
 	DataContainer container;
 
 	private boolean ignoreSelectionChange = false;
 
 	private Object uncommittedItemId;
 
 	private Set<String> lastCollapsedColumns;
 
 	private boolean removalInProgress = false;
 
 	Table tableModel;
 
 	private int tableActionDummyIdCounter = 0;
 
 	@Autowired
 	@Qualifier("exportExecutor")
 	private TaskExecutor exportExecutor;
 
 	/**
 	 * Konstruktor.
 	 */
 	DefaultTableView() {
 		setSpacing(true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#initialize(de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView.Presenter,
 	 *      de.unioninvestment.eai.portal.portlet.crud.domain.model.Table,
 	 *      de.unioninvestment.eai.portal.portlet.crud.domain.model.DatabaseContainer)
 	 */
 	@Override
 	public void initialize(TableView.Presenter presenter,
 			DataContainer databaseContainer, Table tableModel, int pageLength,
 			double cacheRate) {
 
 		this.presenter = presenter;
 		this.container = databaseContainer;
 		this.tableModel = tableModel;
 
 		// @since 1.45
 		if (tableModel.getWidth() != null) {
 			setWidth(tableModel.getWidth());
 		}
 		// @since 1.45
 		if (tableModel.getHeight() != null) {
 			setHeight(tableModel.getHeight());
 		}
 
 		table = new CrudTable(container, tableModel.getColumns(),
 				tableModel.isSortingEnabled());
 		table.disableContentRefreshing();
 		table.setColumnCollapsingAllowed(true);
 		table.setColumnReorderingAllowed(true);
 		table.setSizeFull();
 		table.setSelectable(true);
 		table.setNullSelectionAllowed(false);
 		table.setImmediate(true);
 		table.setEditable(false);
 		table.setMultiSelect(true);
 		table.setMultiSelectMode(MultiSelectMode.DEFAULT);
 
 		if (!tableModel.isSortingEnabled()) {
 			table.setSortDisabled(true);
 		}
 
 		table.addStyleName("crudViewMode");
 		table.addStyleName("crudTable");
 
 		Integer rowHeight = tableModel.getRowHeight();
 		if (rowHeight != null) {
 			table.addStyleName("rowheight" + rowHeight);
			String css = ".v-table-rowheight" + rowHeight
					+ " .v-table-cell-content { height: " + rowHeight + "px; }";
			css += "div.crudTable td div.v-table-cell-wrapper { max-height: "
					+ rowHeight + "px; }";
			CSSInject injector = new CSSInject(css);
 			addComponent(injector);
 		}
 
 		table.setPageLength(pageLength);
 		table.setCacheRate(cacheRate);
 
 		initializeTableFieldFactory();
 
 		// since 1.45
 		table.setHeight("100%");
 		addComponent(table);
 		setExpandRatio(table, 1);
 
 		Layout buttonBar = initButtonBar();
 		if (buttonBar.getComponentIterator().hasNext()) {
 			addComponent(buttonBar);
 		}
 
 		renderTableHeader();
 
 		updateColumnWidths();
 
 		setColumnGenerator(table, rowHeight);
 
 		updateVisibleColumns(false);
 
 		initializeListener();
 
 		setupErrorHandling();
 
 		if (!presenter.isFormEditEnabled()) {
 			container
 					.addBeforeCommitEventHandler(new BeforeCommitEventHandler() {
 						private static final long serialVersionUID = 1L;
 
 						@Override
 						public void beforeCommit(BeforeCommitEvent event) {
 							commitTable();
 						}
 					});
 		}
 
 		setTableStyleRenderer();
 
 		presenter.doInitialize();
 		table.enableContentRefreshing(false);
 	}
 
 	public BufferedTable getTable() {
 		return table;
 	}
 
 	private void initializeListener() {
 		addSelectionChangeListener();
 		addDoubleClickListener();
 
 		if (!presenter.isReadonly()) {
 			addCrudButtonListeners();
 		}
 		addExportButtonListeners();
 		addCustomButtonsListeners();
 	}
 
 	private void setTableStyleRenderer() {
 		this.table.setCellStyleGenerator(new CrudCellStyleGenerator(tableModel,
 				container));
 	}
 
 	private void addSelectionChangeListener() {
 		table.addListener(new Property.ValueChangeListener() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				onSelectionChanged();
 			}
 		});
 	}
 
 	private void addDoubleClickListener() {
 		table.addListener(new ItemClickEvent.ItemClickListener() {
 			private static final long serialVersionUID = 1L;
 
 			public void itemClick(ItemClickEvent event) {
 				if (event.isDoubleClick()) {
 					Item item = event.getItem();
 					presenter.doubleClick(item);
 				}
 			}
 		});
 	}
 
 	private void addCrudButtonListeners() {
 		editButton.addListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				onChangeMode();
 			}
 		});
 		revertButton.addListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				onRevertChanges();
 			}
 		});
 		if (presenter.isInsertable()) {
 			insertButton.addListener(new Button.ClickListener() {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void buttonClick(ClickEvent event) {
 					onAddBlankRow();
 				}
 			});
 		}
 		if (presenter.isDeleteable()) {
 			removeButton.addListener(new Button.ClickListener() {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void buttonClick(ClickEvent event) {
 					onRemoveRow();
 				}
 			});
 		}
 	}
 
 	private void addExportButtonListeners() {
 		if (presenter.isExcelExport()) {
 			excelExportButton.addListener(new Button.ClickListener() {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void buttonClick(ClickEvent event) {
 					exportExcelSheet();
 				}
 
 			});
 		}
 
 		if (presenter.isCSVExport()) {
 			csvExportButton.addListener(new Button.ClickListener() {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void buttonClick(ClickEvent event) {
 					exportCSVSheet();
 				}
 			});
 		}
 	}
 
 	private void addCustomButtonsListeners() {
 		if (!actionButtons.isEmpty()) {
 			for (Button button : actionButtons.values()) {
 				final TableAction action = buttonToTableActionMap.get(button);
 				button.addListener(new Button.ClickListener() {
 
 					private static final long serialVersionUID = 42L;
 
 					@Override
 					public void buttonClick(ClickEvent event) {
 						presenter.callClosure(action);
 
 						if (action.isExportAction()) {
 							switch (action.getExportType()) {
 							case XLS:
 								exportExcelSheet();
 								break;
 							case CSV:
 								exportCSVSheet();
 								break;
 							default:
 								throw new IllegalArgumentException(
 										"Unknown export type '"
 												+ action.getExportType()
 												+ "' set on action with title '"
 												+ action.getTitle()
 												+ "' and id '" + action.getId()
 												+ "'");
 							}
 						}
 					}
 				});
 			}
 		}
 	}
 
 	private void initializeTableFieldFactory() {
 		CrudFieldFactory fieldFactory = new CrudFieldFactory(table, tableModel);
 
 		fieldFactory.setCreateFormFieldForTable(!presenter.isFormEditEnabled());
 
 		ValidationFieldFactoryWrapper validatingFieldFactory = new ValidationFieldFactoryWrapper(
 				container, fieldFactory, tableModel.getColumns());
 		table.setTableFieldFactory(validatingFieldFactory);
 	}
 
 	/**
 	 * Ersetzt den Spaltennamen mit einem alternativen Titel. Wenn ein
 	 * "longtitle" angegeben ist, wird dieser als Tooltip in die Seite
 	 * gerendert.
 	 */
 	void renderTableHeader() {
 		if (tableModel.getColumns() != null) {
 			for (TableColumn tc : tableModel.getColumns()) {
 				String title = tc.getTitle();
 				String longTitle = tc.getLongTitle();
 				if (longTitle != null) {
 					if (title == null) {
 						title = tc.getName();
 					}
 					table.setColumnHeader(tc.getName(), "<span title=\""
 							+ longTitle + "\">" + title + "</span>");
 
 				} else if (title != null) {
 					table.setColumnHeader(tc.getName(), title);
 				}
 			}
 		}
 	}
 
 	private void setColumnGenerator(BufferedTable tableComponent,
 			Integer columnHeight) {
 		TableColumns columns = tableModel.getColumns();
 		if (columns != null) {
 			Iterator<TableColumn> iter = columns.iterator();
 			while (iter.hasNext()) {
 				TableColumn c = iter.next();
 				String columnName = c.getName();
 				if (c.isGenerated()) {
 					VaadinCustomColumnGenerator generator = createVaadinColumnGenerator(c);
 					LOG.debug(
 							"Adding column generator for generated column '{}'",
 							columnName);
 					tableComponent.addGeneratedColumn(columnName, generator);
 
 				} else if (c.isBinary()) {
 
 					BLobColumnGenerator generator = new BLobColumnGenerator(
 							PortletApplication.getCurrentApplication(),
 							container, columns);
 					tableComponent.addGeneratedColumn(columnName, generator);
 				} else {
 					Class<?> columnType = container.getType(columnName);
 					EditorSupport editor = container.findEditor(columnName);
 					CrudTableColumnGenerator generator = new CrudTableColumnGenerator(
 							columnName, columnType, columnHeight, columns,
 							columns.getAllNames().get(0), table, container,
 							editor);
 					if (LOG.isDebugEnabled()) {
 						LOG.debug(
 								"Adding column generator for column '{}' of type '{}'",
 								columnName, columnType.getName());
 					}
 					tableComponent.addGeneratedColumn(columnName, generator);
 				}
 			}
 		}
 	}
 
 	private VaadinCustomColumnGenerator createVaadinColumnGenerator(
 			TableColumn c) {
 		if (c.getGeneratedValueGenerator() != null
 				&& c.getGeneratedType() != null) {
 			return new VaadinExportableColumnGenerator(c, container);
 		} else {
 			return new VaadinCustomColumnGenerator(c, container);
 		}
 	}
 
 	private void updateColumnWidths() {
 		if (tableModel.getColumns() != null) {
 			Collection<?> props = table.getContainerPropertyIds();
 			for (Object p : props) {
 				for (TableColumn c : tableModel.getColumns()) {
 					if (c.getName().equals(p)) {
 						if (c.getWidth() != null && c.getWidth() > 0)
 							table.setColumnWidth(c.getName(), c.getWidth());
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * On Selection.
 	 */
 	@SuppressWarnings("unchecked")
 	public void onSelectionChanged() {
 
 		Set<Object> selection = (Set<Object>) table.getValue();
 
 		if (!ignoreSelectionChange && !removalInProgress && inEditMode()
 				&& !presenter.isFormEditEnabled()
 				&& isLeavingUncommittedRow(selection)) {
 			if (!commit()) {
 				rollbackSelection();
 			} else {
 				updateUncommittedItemId(selection);
 				presenter.changeSelection(selection);
 			}
 
 			table.enableContentRefreshing(true);
 		} else {
 			applySelection(selection);
 			presenter.changeSelection(selection);
 			if (inEditMode() && !presenter.isFormEditEnabled()
 					&& isSingleSelection(selection)) {
 				table.refreshRowCache();
 				table.enableContentRefreshing(true);
 			}
 		}
 	}
 
 	private boolean isSingleSelection(Set<Object> selection) {
 		return selection != null && selection.size() == 1;
 	}
 
 	private boolean isLeavingUncommittedRow(Set<Object> selection) {
 		if (uncommittedItemId == null) {
 			return false;
 		} else if (selection.size() == 1
 				&& uncommittedItemId.equals(selection.iterator().next())) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	void updateUncommittedItemId(Set<Object> selection) {
 		if (selection == null || selection.size() != 1) {
 			uncommittedItemId = null;
 		} else {
 			uncommittedItemId = selection.iterator().next();
 		}
 	}
 
 	private boolean inEditMode() {
 		return table.isEditable();
 	}
 
 	private void rollbackSelection() {
 		if (uncommittedItemId == null) {
 			table.setValue(emptySet());
 		} else {
 			table.setValue(singleton(uncommittedItemId));
 		}
 	}
 
 	private void applySelection(Set<Object> selection) {
 		updateUncommittedItemId(selection);
 		table.setValue(selection);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#addItemToTable()
 	 */
 	@Override
 	public Object addItemToTable() {
 		return table.addItem();
 	}
 
 	/**
 	 * Action Methode fuer das Einfuegen einer neuen Datenzeile.
 	 */
 	void onAddBlankRow() {
 		if (commit()) {
 			Object newItemId = addItemToTable();
 			selectNewRow(newItemId, true);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#selectItemForEditing(java.lang.Object,
 	 *      boolean)
 	 */
 	@Override
 	public void selectItemForEditing(Object newItemId, boolean suppressCommit) {
 		selectNewRow(newItemId, suppressCommit);
 	}
 
 	private void unselectAll() {
 		table.setValue(null);
 	}
 
 	private void selectNewRow(Object newItemId, boolean suppressCommit) {
 		try {
 			// Der Selection-Change-Listener löst bei unselectAll u. a. implizit
 			// ein Commit aus
 			ignoreSelectionChange = suppressCommit;
 			unselectAll();
 			uncommittedItemId = newItemId;
 			table.select(newItemId);
 			table.setCurrentPageFirstItemId(newItemId);
 			if (presenter.isFormEditEnabled()) {
 				presenter.openRowEditingForm();
 			}
 		} finally {
 			ignoreSelectionChange = false;
 		}
 	}
 
 	/**
 	 * Action Methode fuer das Löschen von Datenzeilen.
 	 */
 	public void onRemoveRow() {
 
 		if (table.isEditable() && uncommittedItemId != null) {// Löschen einer
 																// Datenzeile
 			Object nextItemId = table.nextItemId(uncommittedItemId);
 			if (nextItemId == null) {
 				nextItemId = table.prevItemId(uncommittedItemId);
 			}
 			try {
 				removalInProgress = true;
 
 				table.removeItem(uncommittedItemId);
 				commit();
 
 			} finally {
 				removalInProgress = false;
 			}
 
 			uncommittedItemId = nextItemId;
 			table.setValue(singleton(nextItemId));
 
 		} else {// Löschen einer Selektion
 			@SuppressWarnings("unchecked")
 			Set<Object> selection = (Set<Object>) table.getValue();
 			try {
 				removalInProgress = true;
 				for (Object selectedRow : selection) {
 					table.removeItem(selectedRow);
 				}
 				commit();
 			} finally {
 				removalInProgress = false;
 			}
 		}
 	}
 
 	@Override
 	public void onRevertChanges() {
 		try {
 			table.discardFieldValues();
 			table.discard();
 
 			container.rollback();
 			try {
 				ignoreSelectionChange = true;
 				unselectAll();
 			} finally {
 				ignoreSelectionChange = false;
 			}
 
 		} catch (Exception e) {
 			onError(e);
 		}
 	}
 
 	/**
 	 * Wechselt zwischen Anzeige- und Editiermodus. Beim Verlassen des
 	 * Editiermodus wird ein Commit durchgeführt.
 	 */
 	public void onChangeMode() {
 		// Wechsel zwischen View und Edit Mode
 		if (table.isEditable() && commit()) {
 
 			table.setEditable(false);
 
 			editButton.setCaption(getMessage("portlet.crud.button.editMode"));
 			table.removeStyleName("crudEditMode");
 			table.addStyleName("crudViewMode");
 
 			insertButton.setVisible(false);
 			revertButton.setVisible(false);
 			removeButton.setVisible(false);
 
 			table.setColumnCollapsingAllowed(true);
 			setCollapsedColumns(lastCollapsedColumns);
 
 			updateVisibleColumns(false);
 			presenter.switchMode(Mode.VIEW);
 			LOG.debug("Setze den Ansichtsmodus");
 
 		} else {
 			table.setEditable(true);
 
 			editButton.setCaption(getMessage("portlet.crud.button.viewMode"));
 			table.removeStyleName("crudViewMode");
 			table.addStyleName("crudEditMode");
 
 			insertButton.setVisible(true);
 			revertButton.setVisible(true);
 			removeButton.setVisible(true);
 
 			lastCollapsedColumns = getCollapsedColumns();
 			table.setColumnCollapsingAllowed(false);
 			updateVisibleColumns(true);
 			presenter.switchMode(Mode.EDIT);
 			LOG.debug("Setze den Editiermodus");
 		}
 	}
 
 	private Layout initButtonBar() {
 		HorizontalLayout buttonbar = new HorizontalLayout();
 		buttonbar.setSpacing(true);
 
 		if (!this.presenter.isReadonly()) {
 			if (!this.presenter.isReadonly()) {
 				editButton = new Button(
 						getMessage("portlet.crud.button.editMode"));
 				editButton.setEnabled(true);
 				buttonbar.addComponent(editButton);
 
 				revertButton = new Button(
 						getMessage("portlet.crud.button.reset"));
 				revertButton.setVisible(false);
 				revertButton.setEnabled(true);
 				buttonbar.addComponent(revertButton);
 
 				insertButton = new Button(
 						getMessage("portlet.crud.button.blankRow"));
 				insertButton.setVisible(false);
 				insertButton.setEnabled(presenter.isInsertable());
 				buttonbar.addComponent(insertButton);
 
 				removeButton = new Button(
 						getMessage("portlet.crud.button.removeRow"));
 				removeButton.setVisible(false);
 				removeButton.setEnabled(presenter.isDeleteable());
 				buttonbar.addComponent(removeButton);
 			}
 		}
 		if (presenter.isExcelExport()) {
 			excelExportButton = new Button("Excel");
 			buttonbar.addComponent(excelExportButton);
 		}
 
 		if (presenter.isCSVExport()) {
 			csvExportButton = new Button("CSV");
 			buttonbar.addComponent(csvExportButton);
 		}
 
 		for (TableAction action : tableModel.getActions()) {
 			Button actionButton = new Button(action.getTitle());
 			if (action.getId() != null) {
 				actionButtons.put(action.getId(), actionButton);
 			} else {
 				actionButtons.put(
 						"table-action-" + tableActionDummyIdCounter++,
 						actionButton);
 			}
 			if (action.isDownloadAction()) {
 				action.setDownloadActionCallback(new DownloadActionCallback() {
 					/**
 					 * Provides thread- and resource-handling.
 					 */
 					private DownloadExportTask exportTask;
 
 					@Override
 					public void start(String filename, String mimeType) {
 						exportTask = new DownloadExportTask(table
 								.getApplication(), table, tableModel,
 								automaticDownloadIsPossible(), filename,
 								mimeType);
 						executeExport(exportTask);
 					}
 
 					@Override
 					public void updateProgess(float progress) {
 						exportTask.updateProgress(progress);
 					}
 
 					@Override
 					public void finish(InputStream stream) {
 						exportTask.setContent(stream);
 					}
 				});
 			}
 			buttonToTableActionMap.put(actionButton, action);
 			buttonbar.addComponent(actionButton);
 		}
 
 		return buttonbar;
 	}
 
 	private void commitTable() {
 
 		Map<String, Object> changedFieldNames = table.getModifiedColumnNames();
 
 		table.commitFieldValues();
 
 		handleRowChange(changedFieldNames);
 
 		table.commit();
 	}
 
 	/**
 	 * @return <code>true</code>, falls der Commit erfolgreich durchgeführt
 	 *         werden konnte.
 	 */
 	private boolean commit() {
 		try {
 			container.commit();
 			LOG.debug("Committing changes successfull");
 			return true;
 
 		} catch (Buffered.SourceException e) {
 			LOG.debug("Committing changes failed: " + e.getMessage(), e);
 			onError(e);
 			return false;
 		} catch (Exception e) {
 			LOG.error("Committing changes failed: " + e.getMessage(), e);
 			onError(e);
 			return false;
 		}
 	}
 
 	/**
 	 * Initiiert das RowChange-Event.
 	 * 
 	 * @param changedValues
 	 *            Alle Tabellenzellen, die verändert wurden
 	 */
 	private void handleRowChange(Map<String, Object> changedValues) {
 		if (!changedValues.isEmpty() && uncommittedItemId != null) {
 			Item item = table.getItem(uncommittedItemId);
 			presenter.rowChange(item, changedValues);
 		}
 	}
 
 	/**
 	 * Konfiguriert einen ErrorHandler, der Fehler möglichst lesbar als
 	 * Notification anzeigt.
 	 */
 	@SuppressWarnings("serial")
 	protected void setupErrorHandling() {
 		table.setErrorHandler(new ComponentErrorHandler() {
 
 			@Override
 			public boolean handleComponentError(ComponentErrorEvent event) {
 				Throwable throwable = event.getThrowable();
 				if (throwable instanceof SourceException) {
 					onError(throwable);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Erzeugt eine Notification für den angegebenen Fehler.
 	 * 
 	 * @param throwable
 	 *            der anzuzeigende Fehler
 	 */
 	private void onError(Throwable throwable) {
 		String message = throwable.getMessage();
 		if (throwable.getCause() instanceof ConversionException
 				|| throwable.getCause() instanceof InvalidValueException) {
 			message = throwable.getCause().getMessage();
 		}
 		if (throwable instanceof SourceException
 				&& ((SourceException) throwable).getSource() instanceof Component) {
 			Buffered source = ((SourceException) throwable).getSource();
 			String sourceCaption = ((AbstractField) source).getCaption();
 
 			LOG.debug("Field error for {}: {}", sourceCaption,
 					throwable.getMessage());
 
 			showNotification(sourceCaption + ": " + message,
 					Notification.TYPE_ERROR_MESSAGE);
 			return;
 		} else {
 			LOG.error("Error in table operation", throwable);
 		}
 
 		if (message == null || message.length() == 0) {
 			message = getMessage("portlet.crud.error.ofType", throwable
 					.getClass().getName());
 		}
 		showNotification(message, Notification.TYPE_ERROR_MESSAGE);
 	}
 
 	private void updateVisibleColumns(boolean isEditMode) {
 		Object[] visibles;
 		TableColumns columns = tableModel.getColumns();
 		if (columns != null) {
 			visibles = hideUnsupportedDataTypes(
 					columns.getVisibleNamesForTable(), columns);
 		} else {
 			visibles = hideUnsupportedDataTypes(
 					Arrays.asList(table.getVisibleColumns()), columns);
 		}
 		table.setVisibleColumns(visibles);
 	}
 
 	private Object[] hideUnsupportedDataTypes(List<?> visibles,
 			TableColumns columns) {
 		ArrayList<String> visibleCols = new ArrayList<String>();
 		for (Object id : visibles) {
 			if (columns != null && columns.get((String) id).isGenerated()) {
 				visibleCols.add((String) id);
 			} else if (columns != null && columns.get((String) id).isBinary()) {
 				visibleCols.add((String) id);
 			} else {
 				Class<?> type = container.getType((String) id);
 				if (type != null
 						&& container.findDisplayer((String) id) != null) {
 					visibleCols.add((String) id);
 				}
 			}
 		}
 		return visibleCols.toArray();
 	}
 
 	private Set<String> getCollapsedColumns() {
 		Set<String> collapsedColumns = new HashSet<String>();
 		for (Object id : table.getContainerPropertyIds()) {
 			if (table.isColumnCollapsed(id)) {
 				collapsedColumns.add((String) id);
 			}
 		}
 		return collapsedColumns;
 	}
 
 	private void setCollapsedColumns(Set<String> collapsedColumns) {
 		for (Object id : table.getContainerPropertyIds()) {
 			table.setColumnCollapsed(id, collapsedColumns.contains(id));
 		}
 	}
 
 	@Override
 	public void showNotification(String msgKey, int notificationType) {
 		getApplication().getMainWindow().showNotification(msgKey,
 				notificationType);
 	}
 
 	void setTable(CrudTable table) {
 		this.table = table;
 	}
 
 	private void exportCSVSheet() {
 		executeExport(new CsvExportTask(table.getApplication(), table,
 				tableModel, automaticDownloadIsPossible()));
 	}
 
 	private void exportExcelSheet() {
 		executeExport(new ExcelExportTask(table.getApplication(), table,
 				tableModel, automaticDownloadIsPossible()));
 	}
 
 	private void executeExport(ExportTask exportTask) {
 		table.setEnabled(false);
 		boolean automaticDownload = automaticDownloadIsPossible();
 
 		ExportDialog dialog = new ExportDialog(table, exportTask,
 				automaticDownload);
 		this.getApplication().getMainWindow().addWindow(dialog);
 		exportExecutor.execute(exportTask);
 	}
 
 	private boolean automaticDownloadIsPossible() {
 		WebBrowser browser = (WebBrowser) table.getApplication()
 				.getMainWindow().getTerminal();
 		return browser != null && !browser.isIE();
 	}
 
 	@Override
 	public void selectionUpdatedExternally(Set<Object> selection) {
 		if (tableModel.isFormEditEnabled()) {
 			table.setValue(selection);
 		} else {
 			throw new UnsupportedOperationException(
 					"Currently only implemented to be used from inside form-edit dialog");
 		}
 	}
 
 	private Object getcurrentItemId() {
 		@SuppressWarnings("unchecked")
 		Set<Object> selectedValue = (Set<Object>) table.getValue();
 
 		if (selectedValue != null && selectedValue.size() == 1) {
 			return selectedValue.iterator().next();
 		}
 
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#addGeneratedColumn(java.lang.String,
 	 *      com.vaadin.ui.Table.ColumnGenerator)
 	 */
 	@Override
 	public void addGeneratedColumn(String id, String title,
 			ColumnGenerator columnGenerator) {
 		this.table.addGeneratedColumn(id, columnGenerator);
 		this.table.setColumnHeader(id, title);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#removeGeneratedColumn(java.lang.String)
 	 */
 	@Override
 	public void removeGeneratedColumn(String id) {
 		this.table.removeGeneratedColumn(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#enableContentRefreshing(boolean)
 	 */
 	@Override
 	public void enableContentRefreshing(boolean refreshContent) {
 		table.enableContentRefreshing(refreshContent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#disableContentRefreshing()
 	 */
 	@Override
 	public boolean disableContentRefreshing() {
 		return table.disableContentRefreshing();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#getVisibleColumns()
 	 */
 	@Override
 	public List<String> getVisibleColumns() {
 		List<String> visibleColumns = new ArrayList<String>();
 		for (Object vaadinColumnId : table.getVisibleColumns()) {
 			if (vaadinColumnId instanceof String) {
 				visibleColumns.add((String) vaadinColumnId);
 			} else {
 				throw new IllegalStateException(
 						"columnId ["
 								+ vaadinColumnId.toString()
 								+ "] ist nicht vom Typ java.lang.String sondern vom Typ "
 								+ vaadinColumnId.getClass().getName() + ".");
 			}
 		}
 		return visibleColumns;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#setVisibleColumns(java.util.List)
 	 */
 	@Override
 	public void setVisibleColumns(List<String> visibleColumns) {
 		table.setVisibleColumns(visibleColumns.toArray());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#setTableActionVisibility(java.lang.String,
 	 *      boolean)
 	 */
 	public void setTableActionVisibility(String id, boolean visible) {
 		Button button = actionButtons.get(id);
 		if (button != null) {
 			button.setVisible(visible);
 		} else {
 			LOG.warn(
 					"Ein Button mit der ID {} existiert nicht. Die Sichtbarkeit kann nicht auf {} gesetzt werden.",
 					id, visible);
 		}
 	}
 
 	Map<String, Button> getActionButtons() {
 		return Collections.unmodifiableMap(actionButtons);
 	}
 
 }
