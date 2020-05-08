 package org.cotrix.web.manage.client.codelist.codes;
 
 import static com.google.gwt.dom.client.BrowserEvents.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.cotrix.web.common.client.error.ManagedFailureCallback;
 import org.cotrix.web.common.client.factory.UIFactories;
 import org.cotrix.web.common.client.feature.FeatureBinder;
 import org.cotrix.web.common.client.feature.FeatureToggler;
 import org.cotrix.web.common.client.resources.CommonResources;
 import org.cotrix.web.common.client.resources.CotrixSimplePager;
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.client.widgets.HasEditing;
 import org.cotrix.web.common.client.widgets.ItemToolbar;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedEvent;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedHandler;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ItemButton;
 import org.cotrix.web.common.client.widgets.PageSizer.PageSizerStyle;
 import org.cotrix.web.common.client.widgets.LoadingPanel;
 import org.cotrix.web.common.client.widgets.PageSizer;
 import org.cotrix.web.common.client.widgets.PageSizer.PagerSizerResource;
 import org.cotrix.web.common.client.widgets.cell.DoubleClickEditTextCell;
 import org.cotrix.web.common.client.widgets.cell.SafeHtmlRendererCell;
 import org.cotrix.web.common.client.widgets.cell.StyledSafeHtmlRenderer;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.ConfirmDialogListener;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.DialogButton;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.DialogButtonDefaultSet;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.manage.client.ManageServiceAsync;
 import org.cotrix.web.manage.client.codelist.codes.event.CodeSelectedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.CodeUpdatedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.GroupSwitchType;
 import org.cotrix.web.manage.client.codelist.codes.event.GroupSwitchedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.MarkerHighlightEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.SwitchGroupEvent;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerRenderer;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerType;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerTypeUtil;
 import org.cotrix.web.manage.client.codelist.common.RemoveItemController;
 import org.cotrix.web.manage.client.data.CodeAttribute;
 import org.cotrix.web.manage.client.data.DataEditor;
 import org.cotrix.web.manage.client.data.event.DataEditEvent;
 import org.cotrix.web.manage.client.data.event.DataEditEvent.DataEditHandler;
 import org.cotrix.web.manage.client.data.event.EditType;
 import org.cotrix.web.manage.client.di.CodelistBus;
 import org.cotrix.web.manage.client.di.CurrentCodelist;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.shared.AttributeGroup;
 import org.cotrix.web.manage.shared.Group;
 import org.cotrix.web.manage.shared.ManagerUIFeature;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.AbstractSafeHtmlCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeUri;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.gwt.text.shared.SafeHtmlRenderer;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.PatchedDataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.Style;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodesEditor extends LoadingPanel implements HasEditing {
 
 	interface Binder extends UiBinder<Widget, CodesEditor> {}
 	interface CodelistEditorEventBinder extends EventBinder<CodesEditor> {}
 	
 	private DialogButton CONTINUE_BUTTON;
 	private DialogButton MARK_BUTTON;
 	private DialogButton CANCEL_BUTTON;
 	
 	interface EditorStyle extends CssResource {
 		String dialogButton();
 		String cancelButton();
 	}
 
 	interface DataGridResources extends PatchedDataGrid.Resources {
 
 		@Source("CodelistEditor.css")
 		DataGridStyle dataGridStyle();
 	}
 
 	interface DataGridStyle extends PatchedDataGrid.Style {
 		
 		String codeCell();
 
 		String headerCell();
 		
 		String markerHeader();
 
 		String textCell();
 
 		String language();
 
 		String closeGroup();
 
 		String emptyTableWidget();
 	}
 	
 	interface GridPageSizerResource extends PagerSizerResource {
 		@Source({"PageSizer.css","definitions.css"})
 		GridPagerSizerStyle style();
 	}
 	
 	interface GridPagerSizerStyle extends PageSizerStyle {
 	}
 	
 	interface GridPagerResource extends CotrixSimplePager {
 	    @Source({"SimplePager.css","definitions.css"})
 	    Style simplePagerStyle();
 	}
 
 	@UiField(provided = true)
 	PatchedDataGrid<UICode> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	@UiField(provided = true)
 	PageSizer pageSizer;
 
 	@UiField ItemToolbar toolBar;
 	
 	@UiField EditorStyle style;
 
 	private static DataGridResources resource = GWT.create(DataGridResources.class);
 	private static GridPageSizerResource gridPageSizerResource = GWT.create(GridPageSizerResource.class);
 	private static GridPagerResource gridPagerResource = GWT.create(GridPagerResource.class);
 
 	private List<Group> groupsAsColumn = new ArrayList<Group>();
 	private Map<Group, Column<UICode, String>> groupsColumns = new HashMap<Group, Column<UICode,String>>(); 
 	private List<DoubleClickEditTextCell> editableCells = new ArrayList<DoubleClickEditTextCell>();
 	private boolean editable = true;
 
 	private Column<UICode, String> nameColumn;
 
 	@Inject @CodelistBus
 	private EventBus codelistBus;
 
 	private SingleSelectionModel<UICode> selectionModel;
 
 	@Inject
 	private CodesProvider dataProvider;
 
 	private DataEditor<UICode> codeEditor;
 
 	private DataEditor<CodeAttribute> attributeEditor;
 
 	@Inject
 	private CotrixManagerResources resources;
 
 	private StyledSafeHtmlRenderer cellRenderer;
 
 	@Inject
 	private ManageServiceAsync managerService;
 
 	@Inject @CurrentCodelist
 	private String codelistId;
 	
 	@Inject
 	private UIFactories factories;
 
 	@Inject
 	private RemoveItemController codeRemotionController;
 	
 	@Inject
 	private MarkerRenderer markerRenderer;
 	
 	@Inject
 	private ConfirmDialog confirmDialog;
 	
 	@Inject
 	private MarkerTypeUtil markerTypeResolver;
 	
 	@Inject
 	private CodesEditorRowHightlighter hightlighter;
 
 	@Inject
 	private void init() {
 		this.codeEditor = DataEditor.build(this);
 		this.attributeEditor = DataEditor.build(this);
 
 		cellRenderer = new StyledSafeHtmlRenderer(resource.dataGridStyle().textCell());
 		cellRenderer.setAddTitle(true);
 
 		dataGrid = new PatchedDataGrid<UICode>(20, resource, CodeKeyProvider.INSTANCE) {
 
 			/** 
 			 * {@inheritDoc}
 			 */
 			@Override
 			protected int getTableWidth() {
 				int internal = super.getTableWidth();
 				return internal>0?internal:CodesEditor.this.getElement().getOffsetWidth()-31;
 			}
 			
 		};
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 
 		Label emptyTable = new Label("No codes");
 		emptyTable.setStyleName(resource.dataGridStyle().emptyTableWidget());
 		dataGrid.setEmptyTableWidget(emptyTable);
 
 		dataGrid.setTableWidth(100, Unit.PCT);
 		dataGrid.setAutoAdjust(true);
 		dataGrid.setLastColumnSpan(true);
 		
 		dataGrid.setRowStyles(hightlighter);
 
 		AsyncHandler asyncHandler = new AsyncHandler(dataGrid);
 		dataGrid.addColumnSortHandler(asyncHandler);
 
 		// Create a Pager to control the table.
 		pager = new SimplePager(TextLocation.CENTER, gridPagerResource, false, 0, true);
 		pager.setDisplay(dataGrid);
 
 		setupColumns();
 
 		selectionModel = new SingleSelectionModel<UICode>(CodeKeyProvider.INSTANCE);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				UICode code = selectionModel.getSelectedObject();
 
 				codeRemotionController.setItemCanBeRemoved(code!=null);
 				updateRemoveButtonVisibility(false);
 					
 				Log.trace("onSelectionChange code: "+code);
 				codelistBus.fireEvent(new CodeSelectedEvent(code));
 			}
 		});
 
 		dataGrid.setSelectionModel(selectionModel);
 		dataGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
 
 		dataProvider.addDataDisplay(dataGrid);
 		
 		pageSizer = new PageSizer(new int[]{25,50,100}, 25, gridPageSizerResource);
 		pageSizer.setDisplay(dataGrid);
 		
 		Binder uiBinder = GWT.create(Binder.class);
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		toolBar.addButtonClickedHandler(new ButtonClickedHandler() {
 
 			@Override
 			public void onButtonClicked(ButtonClickedEvent event) {
 				switch (event.getButton()) {
 					case MINUS: removeSelectedCode(); break;
 					case PLUS: addNewCode(); break;
 				}
 			}
 		});
 		
 		CONTINUE_BUTTON = new ConfirmDialog.SimpleDialogButton("Continue And Remove", CommonResources.INSTANCE.css().blueButton() + " " + style.dialogButton(), 220);
 		MARK_BUTTON =  new ConfirmDialog.SimpleDialogButton("Mark Deleted Instead", CommonResources.INSTANCE.css().blueButton() + " " + style.dialogButton(), 220);
 		CANCEL_BUTTON =  new ConfirmDialog.SimpleDialogButton(DialogButtonDefaultSet.CANCEL.getLabel(), DialogButtonDefaultSet.CANCEL.getStyleName() + " " + style.cancelButton(), 98);
 		
 		codeRemotionController.setItemCanBeRemoved(false);
 		updateRemoveButtonVisibility(false);
 	}
 
 	@Inject
 	private void bind(CodelistEditorEventBinder binder) {
 		binder.bindEventHandlers(this, codelistBus);
 	}
 	
 	public void reload() {
 		selectionModel.clear();
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 	
 	private void updateRemoveButtonVisibility(boolean animate) {
 		toolBar.setEnabled(ItemButton.MINUS, codeRemotionController.canRemove(), animate);
 	}
 
 	private void setupColumns() {
 		
 		Column<UICode, UICode> markerColumn = new Column<UICode, UICode>(new SafeHtmlRendererCell<UICode>(markerRenderer)) {
 			
 			@Override
 			public UICode getValue(UICode object) {
 				return object;
 			}
 		};
 
 		dataGrid.addFixedWidthColumn(markerColumn, new TextHeader(""), 10);
 		
 		StyledSafeHtmlRenderer codeCellRenderer = new StyledSafeHtmlRenderer(resource.dataGridStyle().textCell() + " " + resource.dataGridStyle().codeCell());
 		cellRenderer.setAddTitle(true);
 		String codeCellStyle = CommonResources.INSTANCE.css().textBox() + " " + resources.css().editor() + " " + resource.dataGridStyle().codeCell();
 		final DoubleClickEditTextCell codeCell = new DoubleClickEditTextCell(codeCellStyle, codeCellRenderer);
 		codeCell.setReadOnly(!editable);
 		editableCells.add(codeCell);
 
 		nameColumn = new CodeColumn(codeCell);
 
 		nameColumn.setSortable(true);
 
 		nameColumn.setFieldUpdater(new FieldUpdater<UICode, String>() {
 
 			@Override
 			public void update(int index, UICode row, String value) {
 				if (value != null && !value.isEmpty()) {
 					if (!value.equals(row.getName().getLocalPart())) {
 						row.getName().setLocalPart(value);
 						codeEditor.updated(row);
 					}
 				} else {
 					codeCell.clearViewData(CodeKeyProvider.INSTANCE.getKey(row));
 					dataGrid.redrawRow(index);
 				}
 			}
 		});
 
 		TextHeader nameColumnHeader = new TextHeader("Code");
 		nameColumnHeader.setHeaderStyleNames(resource.dataGridStyle().headerCell() + " " + resource.dataGridStyle().codeCell());
 		dataGrid.addColumn(nameColumn, nameColumnHeader);
 		
 
 	}
 
 	public void showAllGroupsAsColumn(final boolean reloadData)
 	{
 		showLoader();
 		managerService.getGroups(codelistId, new ManagedFailureCallback<List<Group>>() {
 			
 			public void onCallFailed() {
 				hideLoader();
 			}
 
 			@Override
 			public void onSuccess(List<Group> groups) {
 				setGroups(groups);
 				hideLoader();
 				dataGrid.refreshColumnSizes();
 				if (reloadData) reload();
 			}
 		});
 	}
 
 	public void showAllGroupsAsNormal()
 	{
 		switchAllGroupsToNormal();
 	}
 
 	public void setEditable(boolean editable)
 	{
 		this.editable = editable;
 		for (DoubleClickEditTextCell cell:editableCells) cell.setReadOnly(!editable);
 	}
 
 	@Inject
 	private void bind(@CurrentCodelist String codelistId, FeatureBinder featureBinder)
 	{
 		featureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				toolBar.setEnabled(ItemButton.PLUS, active);
 			}
 		}, codelistId, ManagerUIFeature.ADD_CODE);
 
 		featureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				codeRemotionController.setUserCanEdit(active);
 				//we animate only if the user obtain the edit permission
 				updateRemoveButtonVisibility(active);
 			}
 		}, codelistId, ManagerUIFeature.REMOVE_CODE);
 
 		codelistBus.addHandler(DataEditEvent.getType(UICode.class), new DataEditHandler<UICode>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<UICode> event) {
 				Log.trace("onDataEdit row: "+event.getData());
 				if (event.getEditType()!=EditType.REMOVE) refreshCode(event.getData());
 			}
 		});
 
 		codelistBus.addHandler(DataEditEvent.getType(CodeAttribute.class), new DataEditHandler<CodeAttribute>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<CodeAttribute> event) {
 				refreshCode(event.getData().getCode());
 			}
 		});
 	}
 
 	@EventHandler
 	void onCodeUpdated(CodeUpdatedEvent event) {
 		refreshCode(event.getCode());
 	}
 
 	@EventHandler
 	void onSwitchAttribute(SwitchGroupEvent event) {
 		Group group = event.getGroup();
 		Log.trace("onSwitchAttribute group: "+group+" type: "+event.getSwitchType());
 		switch (event.getSwitchType()) {
 			case TO_COLUMN: switchToColumn(group); break;
 			case TO_NORMAL: switchToNormal(group); break;
 		}
 	}
 	
 	@EventHandler
 	void onMarkerHighlight(MarkerHighlightEvent event) {
 		dataGrid.redraw();
 	}
 
 	private void refreshCode(UICode code)
 	{
 		int absoluteIndex = dataProvider.getCache().indexOf(code) + dataGrid.getPageStart();
 		if (absoluteIndex>=0) dataGrid.redrawRow(absoluteIndex);
 	}
 
 	private void removeSelectedCode()
 	{
 		Log.trace("removeSelectedCode");
 		final UICode code = selectionModel.getSelectedObject();
 		if (code!=null) {
 			
 			confirmDialog.center("Do you want to go ahead, or just mark the code as DELETED (reccomended)?", new ConfirmDialogListener() {
 				
 				@Override
 				public void onButtonClick(DialogButton button) {
 					if (button == CONTINUE_BUTTON) doRemoveCode(code);
 					if (button == MARK_BUTTON) doMarkCodeDeleted(code);
 				}
 			}, CONTINUE_BUTTON, MARK_BUTTON, CANCEL_BUTTON);
 		}
 	}
 	
 	private void doRemoveCode(UICode code) {
 		dataProvider.remove(code);
 		codeEditor.removed(code);
 	}
 	
 	private void doMarkCodeDeleted(UICode code) {
 		UIAttribute attribute = markerTypeResolver.toAttribute(MarkerType.DELETED, "");
 		code.addAttribute(attribute);
 		attributeEditor.added(new CodeAttribute(code, attribute));
 	}
 
 	private void addNewCode()
 	{
 		Log.trace("addNewCode");
 		final UICode code = factories.createCode();
 		dataProvider.add(0, code);
 		
 		//workaround to avoid select model setSelect method use due to the missing id in the new code and used by the key provider to resolve the row
 		dataGrid.setKeyboardSelectedRow(0, true);
 	
 		dataGrid.getScrollPanel().setVerticalScrollPosition(0);
 		dataGrid.getScrollPanel().setHorizontalScrollPosition(0);
 		codeEditor.added(code);
 	}
 	
 	private DoubleClickEditTextCell createCell(boolean isEditable)
 	{
 		String editorStyle = CommonResources.INSTANCE.css().textBox() + " " + resources.css().editor();
 		DoubleClickEditTextCell cell = new DoubleClickEditTextCell(editorStyle, cellRenderer);
 		if (isEditable) {
 			cell.setReadOnly(!editable);
 			editableCells.add(cell);
 		}
 		return cell;
 	}
 
 	private Column<UICode, String> getGroupColumn(final Group group)
 	{
 		Log.trace("getGroupColumn group: "+group);
 		Column<UICode, String> column = groupsColumns.get(group);
 		if (column == null) {
 			column = new GroupColumn(createCell(group.isEditable()), group);
 			column.setCellStyleNames(group.isSystemGroup()?resources.css().systemProperty():"");
 			column.setSortable(group.isSortable());
 
 			if (group.isEditable()) {
 				column.setFieldUpdater(new FieldUpdater<UICode, String>() {
 
 					@Override
 					public void update(int index, UICode code, String value) {
 						groupColumnUpdated(code, group, value);
 					}
 				});
 			}
 
 
 			groupsColumns.put(group, column);
 		}
 		return column;
 	}
 
 	private void groupColumnUpdated(UICode code, Group group, String value) {
 
 		if (group instanceof AttributeGroup) {
 			AttributeGroup attributeGroup = (AttributeGroup)group;
 
 			UIAttribute attribute = attributeGroup.match(code.getAttributes());
 			if (attribute!=null) {
 				if (!value.equals(attribute.getValue())) {
 					attribute.setValue(value);
 					attributeEditor.updated(new CodeAttribute(code, attribute));
 				}
 			} else {
 				attribute = factories.createAttribute();
 				attribute.setName(attributeGroup.getName());
 				attribute.setLanguage(attributeGroup.getLanguage());
 				attribute.setValue(value);
 				code.addAttribute(attribute);
 				attributeEditor.added(new CodeAttribute(code, attribute));
 			}
 		}
 	}
 
 	private void switchToColumn(Group group)
 	{
 		addGroupColumn(group);
 		codelistBus.fireEvent(new GroupSwitchedEvent(group, GroupSwitchType.TO_COLUMN));
 	}
 
 	private void addGroupColumn(Group group)
 	{
 		if (groupsAsColumn.contains(group)) return;
 		Column<UICode, String> column = getGroupColumn(group);
 		groupsAsColumn.add(group);
 
 		GroupHeader header = new GroupHeader(group);
 		header.setHeaderStyleNames(resource.dataGridStyle().headerCell());
 		dataGrid.addColumn(column, header);
 	}
 
 	private void switchToNormal(Group group)
 	{
 		removeGroupColumn(group);
 		codelistBus.fireEvent(new GroupSwitchedEvent(group, GroupSwitchType.TO_NORMAL));
 	}
 
 	private void removeGroupColumn(Group group)
 	{
 		if (!groupsAsColumn.contains(group)) return;
 		Column<UICode, String> column = getGroupColumn(group);
 		groupsAsColumn.remove(group);
 		dataGrid.removeColumn(column);
 	}
 
 	private void setGroups(List<Group> groups) {
 		Log.trace("groups: "+groups);
 		
 		Set<Group> columnsToRemove = new HashSet<Group>(groupsAsColumn);
 		//can't use removeall because based on comparable interface
 		//no optimization for (Group group:groups) columnsToRemove.remove(group);
 		Log.trace("columns to remove: "+columnsToRemove);
 
 		for (Group toRemove:columnsToRemove) switchToNormal(toRemove);
 
 		for (Group group:groups) switchToColumn(group);
 	}
 
 	private void switchAllGroupsToNormal() {
 		Set<Group> groupsToNormal = new HashSet<Group>(groupsAsColumn);
 		for (Group group:groupsToNormal) switchToNormal(group);
 	}
 	
 	
 
 	public static abstract class CodelistEditorColumn<C> extends Column<UICode, C> {
 
 		public CodelistEditorColumn(Cell<C> cell) {
 			super(cell);
 		}
 	}
 
 	public static class CodeColumn extends CodelistEditorColumn<String> {
 
 		public CodeColumn(Cell<String> cell) {
 			super(cell);
 		}
 
 		@Override
 		public String getValue(UICode object) {
 			if (object == null) return null;
 			return ValueUtils.getValue(object.getName());
 		}
 	}
 
 	public static class GroupColumn extends CodelistEditorColumn<String> {
 
 		protected Group group;
 
 		public GroupColumn(Cell<String> cell, Group group) {
 			super(cell);
 			this.group = group;
 		}
 
 		@Override
 		public String getValue(UICode code) {
 			if (code == null) return "";
 			return group.getValue(code);
 		}
 
 		public Group getGroup() {
 			return group;
 		}
 	}
 
 	private class GroupHeader extends Header<Group> {
 
 		private Group group;
 
 		/**
 		 * Construct a new TextHeader.
 		 *
 		 * @param text the header text as a String
 		 */
 		public GroupHeader(Group group) {
 			super(new ClickableGroupCell(new SafeHtmlGroupRenderer()));
 			this.group = group;
 
 			setUpdater(new ValueUpdater<Group>() {
 
 				@Override
 				public void update(Group value) {
 					switchToNormal(value);
 				}
 			});
 		}
 
 		/** 
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean onPreviewColumnSortEvent(Context context, Element elem, NativeEvent event) {
 			Element element = event.getEventTarget().cast();
 			return !element.getId().equals(SafeHtmlGroupRenderer.CLOSE_IMG_ID);
 		}
 
 
 
 		/**
 		 * Return the header text.
 		 */
 		@Override
 		public Group getValue() {
 			return group;
 		}
 	}
 
 	public class ClickableGroupCell extends AbstractSafeHtmlCell<Group> {
 
 
 		/**
 		 * Construct a new ClickableTextCell that will use a given
 		 * {@link SafeHtmlRenderer}.
 		 * 
 		 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<Group>} instance
 		 */
 		public ClickableGroupCell(SafeHtmlRenderer<Group> renderer) {
 			super(renderer, CLICK, KEYDOWN);
 		}
 
 		@Override
 		public void onBrowserEvent(Context context, Element parent, Group value,
 				NativeEvent event, ValueUpdater<Group> valueUpdater) {
 			super.onBrowserEvent(context, parent, value, event, valueUpdater);
 			if (CLICK.equals(event.getType())) {
 				onEnterKeyDown(context, parent, value, event, valueUpdater);
 			}
 		}
 
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent, Group value,
 				NativeEvent event, ValueUpdater<Group> valueUpdater) {
 			Element element = event.getEventTarget().cast();
 
 			if (valueUpdater != null && element.getId().equals(SafeHtmlGroupRenderer.CLOSE_IMG_ID)) {
 				valueUpdater.update(value);
 			}
 		}
 
 		@Override
 		protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
 			if (value != null) {
 				sb.append(value);
 			}
 		}
 	}
 
 	static class SafeHtmlGroupRenderer extends AbstractSafeHtmlRenderer<Group> {
 
 		static interface GroupHeaderTemplate extends SafeHtmlTemplates {
 
 			@Template("<div style=\"height:16px\">{0}<img id=\"{3}\"  src=\"{1}\" class=\"{2}\" style=\"vertical-align:middle;\" title=\"Hide Column\" /></div>")
 			SafeHtml header(SafeHtml label, SafeUri img, String imgStyle, String imgId);
 		}
 
 		static final GroupHeaderTemplate HEADER_TEMPLATE = GWT.create(GroupHeaderTemplate.class);
 		public static final String CLOSE_IMG_ID = Document.get().createUniqueId();
 
 		@Override
 		public SafeHtml render(Group value) {
 			SafeHtml label = value.getLabel();
 			SafeUri img = CotrixManagerResources.INSTANCE.closeSmall().getSafeUri();
 			String imgStyle = resource.dataGridStyle().closeGroup();
 			return HEADER_TEMPLATE.header(label, img, imgStyle, CLOSE_IMG_ID);
 		}
 	}
 }
