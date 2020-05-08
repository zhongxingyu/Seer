 package mayo.edu.cts2.editor.client.widgets;
 
 import mayo.edu.cts2.editor.client.Cts2Editor;
 import mayo.edu.cts2.editor.client.datasource.ValueSetItemXmlDS;
 import mayo.edu.cts2.editor.client.datasource.ValueSetsXmlDS;
 import mayo.edu.cts2.editor.client.widgets.versions.VersionWindow;
 
 import com.smartgwt.client.data.Criteria;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.SortSpecifier;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.SelectionAppearance;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.types.SortDirection;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.ImgButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.events.DoubleClickEvent;
 import com.smartgwt.client.widgets.events.DoubleClickHandler;
 import com.smartgwt.client.widgets.grid.CellFormatter;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 public class ValueSetsListGrid extends BaseValueSetsListGrid {
 
 	public static final String DEFAULT_VERSION_COMMENT = "Initial Version";
 
 	private final ValueSetsXmlDS i_valueSetsXmlDS;
 	private String i_xmlData;
 
 	public ValueSetsListGrid() {
 		super();
 
 		i_valueSetsXmlDS = ValueSetsXmlDS.getInstance();
 
 		setWidth100();
 		setHeight100();
 		setShowAllRecords(true);
 		setWrapCells(false);
 		setDataSource(i_valueSetsXmlDS);
 		setEmptyMessage(EMPTY_MESSAGE);
 
 		// hover attributes
 		// setCanHover(true);
 		// setShowHover(true);
 		// setShowHoverComponents(true);
 		setHoverMoveWithMouse(true);
 		setHoverWidth(200);
 		setHoverWrap(false);
 
 		setShowRecordComponents(true);
 		setShowRecordComponentsByCell(true);
 
 		// this value needs to be set to create the expander list grid.
 		setCanExpandRecords(true);
 
 		ListGridField resourceNamefField = new ListGridField(ID_VALUE_SET_NAME, TITLE_VALUE_SET_NAME);
 		resourceNamefField.setWidth("20%");
 		resourceNamefField.setWrap(false);
 		resourceNamefField.setShowHover(true);
 		resourceNamefField.setCanEdit(false);
 
 		ListGridField formalNameField = new ListGridField(ID_FORMAL_NAME, TITLE_FORMAL_NAME);
 		formalNameField.setWidth("20%");
 		formalNameField.setWrap(false);
 		formalNameField.setShowHover(true);
 		formalNameField.setCanEdit(false);
 
 		// ListGridField currentVersionField = new
 		// ListGridField(ID_CURRENT_VERSION, TITLE_CURRENT_VERSION);
 		ListGridField currentVersionField = new ListGridField(ID_CURRENT_VERSION, TITLE_CURRENT_VERSION);
 		currentVersionField.setWidth("20%");
 		currentVersionField.setWrap(false);
 		currentVersionField.setShowHover(true);
 		currentVersionField.setCanEdit(false);
 
 		// If the version is 1, then add a comment, else add the user's comment.
 		currentVersionField.setCellFormatter(new CellFormatter() {
 
 			@Override
 			public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
 				if (record == null) {
 					return null;
 				} else {
 					String version = record.getAttribute(ID_URI);
 					String comment = record.getAttribute(ID_COMMENT);
 					return getVersion(version, comment);
 				}
 			}
 		});
 
 		ListGridField versionsChangeField = new ListGridField(ID_CHANGE_VERSION, TITLE_CHANGE_VERSION);
 		versionsChangeField.setWidth("64px");
 		versionsChangeField.setWrap(false);
 		versionsChangeField.setShowHover(true);
 		versionsChangeField.setCanEdit(false);
 
 		ListGridField actionField = new ListGridField(ID_ACTION, TITLE_ACTION);
 		actionField.setWrap(false);
 		actionField.setWidth("126px");
 		actionField.setCanEdit(false);
 		actionField.setAttribute(ID_HIDDEN_ACTION, ACTION_NONE);
 		if (Cts2Editor.getReadOnly())
 			actionField.setHidden(true);
 
 		setFields(formalNameField, resourceNamefField, currentVersionField, versionsChangeField, actionField);
 
 		setSelectOnEdit(true);
 		setSelectionAppearance(SelectionAppearance.ROW_STYLE);
 		setSelectionType(SelectionStyle.SINGLE);
 
 		// set the initial sort
 		SortSpecifier[] sortspec = new SortSpecifier[1];
 		sortspec[0] = new SortSpecifier(ID_FORMAL_NAME, SortDirection.ASCENDING);
 		setInitialSort(sortspec);
 
 		// When the user clicks on the triangle to collapse, verify there are no
 		// unsaved changes.
 		// addRecordCollapseHandler(new RecordCollapseHandler() {
 		//
 		// @Override
 		// public void onRecordCollapse(final RecordCollapseEvent event) {
 		// synchronized (event) {
 		//
 		// final Record record = event.getRecord();
 		//
 		// if (record != null) {
 		//
 		// // get the expansion component and see if any unsaved
 		// // changes are pending
 		// final ValueSetEntitiesLayout entitiesLayout =
 		// getExpansionComponentById(record
 		// .getAttribute("_expansionComponentID"));
 		// // if (!entitiesLayout.getOkToClose()) {
 		// if (entitiesLayout != null) {
 		// if (entitiesLayout.checkForUnsavedChanges()) {
 		//
 		// BooleanCallback booleanCallback = new BooleanCallback() {
 		// @Override
 		// public void execute(Boolean value) {
 		// if (value != null && !value) {
 		// System.out.println("User canceled close");
 		// event.cancel();
 		// } else {
 		// System.out.println("User closing and not saving");
 		// // entitiesLayout.setOkToClose(true);
 		// // collapseRecord((ListGridRecord)
 		// // record);
 		//
 		// }
 		// }
 		// };
 		//
 		// entitiesLayout.warnUserOfUnsavedChanges(/*booleanCallback*/);
 		//
 		// // if (!entitiesLayout.getOkToClose()) {
 		// // event.cancel();
 		// // }
 		// System.out.println("exiting method");
 		// }
 		// }
 		// // }
 		// }
 		// }
 		// }
 		// });
 
 		// Expand/collapse the row on a double click.
 		addDoubleClickHandler(new DoubleClickHandler() {
 
 			@Override
 			public void onDoubleClick(DoubleClickEvent event) {
 
 				ListGridRecord record = getSelectedRecord();
 
 				if (isExpanded(record) != null && isExpanded(record)) {
 
 					// get the expansion component and see if any unsaved
 					// changes are pending
 					ValueSetEntitiesLayout entitiesLayout = getExpansionComponentById(record
 					        .getAttribute("_expansionComponentID"));
 					if (entitiesLayout != null) {
 						if (entitiesLayout.checkForUnsavedChanges()) {
 							entitiesLayout.warnUserOfUnsavedChanges();
 						} else {
 							collapseRecord(record);
 						}
 					}
 
 				} else {
 					expandRecord(record);
 				}
 			}
 		});
 
 	}
 	/**
 	 * Get the related datasource to show the inner grid in.
 	 */
 	@Override
 	public DataSource getRelatedDataSource(ListGridRecord record) {
 
 		String oid = record.getAttribute("valueSetName");
 
		// create a unique ID for the datasource id.
		oid = oid.trim().replace('.', '_');
 		oid = "ValueSetItemXmlDS" + oid;
 
 		return ValueSetItemXmlDS.getInstance(oid);
 	}
 
 	@Override
 	/**
 	 * Create the ListGrid that has the entities for this parent record.
 	 */
 	protected Canvas getExpansionComponent(final ListGridRecord record) {
 
 		VLayout layout = new VLayout(5);
 		layout.setPadding(5);
 
 		DataSource childDatasource = getRelatedDataSource(record);
 		ValueSetEntitiesLayout valueSetEntitiesLayout = new ValueSetEntitiesLayout(record, childDatasource, this);
 
 		// save the id for later. Can use it to refresh the expansion component.
 		record.setAttribute("_expansionComponentID", valueSetEntitiesLayout.getID());
 
 		return valueSetEntitiesLayout;
 	}
 
 	@Override
 	/**
 	 * Update the Action field
 	 */
 	protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
 
 		String fieldName = this.getFieldName(colNum);
 
 		if (fieldName.equals(ID_ACTION) && record.getAttribute(ID_HIDDEN_ACTION) != null
 		        && record.getAttribute(ID_HIDDEN_ACTION).equals(ACTION_ADD)) {
 
 			HLayout recordCanvas = new HLayout(1);
 			recordCanvas.setHeight(22);
 			recordCanvas.setAlign(Alignment.LEFT);
 			ImgButton undoImg = createImage("undo.png", "Remove added value set");
 
 			undoImg.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					removeData(record);
 				}
 			});
 
 			recordCanvas.addMember(undoImg);
 
 			Label dataLabel = new Label("<em style=\"font-weight:bold; color:green;margin-left:8px\">" + ACTION_ADD
 			        + "</em>");
 			dataLabel.setAutoFit(true);
 			dataLabel.setWrap(false);
 			recordCanvas.addMember(dataLabel);
 
 			return recordCanvas;
 
 		} else if (fieldName.equals(ID_CHANGE_VERSION)) {
 
 			// Add a clickable image to change the version for this column
 			HLayout recordCanvas = new HLayout(2);
 			recordCanvas.setHeight(22);
 			recordCanvas.setWidth(18);
 			recordCanvas.setAlign(Alignment.RIGHT);
 			ImgButton versionImg = createImage("version.png", "Change Version...");
 
 			versionImg.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 
 					String userName = Cts2Editor.getUserName();
 					String valueSetId = record.getAttribute(ID_VALUE_SET_NAME);
 
 					Criteria criteria = new Criteria();
 					criteria.setAttribute(ID_VALUE_SET_NAME, record.getAttribute(ID_VALUE_SET_NAME));
 					criteria.setAttribute(ID_FORMAL_NAME, record.getAttribute(ID_FORMAL_NAME));
 					criteria.setAttribute(ID_URI, record.getAttribute(ID_URI));
 					criteria.setAttribute(ID_COMMENT, record.getAttribute(ID_COMMENT));
 					criteria.setAttribute("userName", userName);
 
 					VersionWindow versionWindow = new VersionWindow(criteria);
 					versionWindow.show();
 				}
 
 			});
 
 			recordCanvas.addMember(versionImg);
 			return recordCanvas;
 		}
 
 		return super.createRecordComponent(record, colNum);
 	}
 
 	/**
 	 * Call the search to get the matching data.
 	 * 
	 * @param searchText
 	 */
 	public void populateData(String xmlData) {
 
 		// System.out.println(xmlData);
 
 		i_xmlData = xmlData;
 		i_valueSetsXmlDS.setData(i_xmlData);
 
 		fetchData();
 		redraw();
 	}
 
 	/**
 	 * Get the text to display as the version
 	 * 
 	 * @param version
 	 * @return
 	 */
 	private String getVersion(String version, String comment) {
 		if (version.equals("1")) {
 			return version + " (" + DEFAULT_VERSION_COMMENT + ")";
 		}
 		if (comment != null) {
 			return version + " (" + comment + ")";
 		} else {
 			return version;
 		}
 	}
 
 	/**
 	 * Create and add a new record.
 	 * 
 	 * @param formalName
 	 * @param vsIdentifier
 	 * @param description
 	 */
 	public void createNewRecord(String formalName, String vsIdentifier, String description) {
 
 		ListGridRecord newRecord = new ListGridRecord();
 		newRecord.setAttribute(ID_FORMAL_NAME, formalName);
 		newRecord.setAttribute(ID_VALUE_SET_NAME, vsIdentifier);
 		newRecord.setAttribute(ID_DESCRIPTION, description);
 
 		// add a hidden attribute to indicate it was added.
 		newRecord.setAttribute(ID_HIDDEN_ACTION, ACTION_ADD);
 		addData(newRecord);
 	}
 
 	/**
 	 * Update the version of an existing value set record.
 	 * 
 	 * @param recordToUpdate
 	 * @param vsIdentifier
 	 * @param versionId
 	 * @param comment
 	 * @param changeSetId
 	 */
 	public void updateRecord(ListGridRecord recordToUpdate, String vsIdentifier, String versionId, String comment,
 	        String changeSetId, String documentUri) {
 
 		if (versionId != null) {
 
 			recordToUpdate.setAttribute(ID_CHANGE_SET_URI, changeSetId);
 			recordToUpdate.setAttribute(ID_DOCUMENT_URI, documentUri);
 			recordToUpdate.setAttribute(ID_URI, versionId);
 			recordToUpdate.setAttribute(ID_COMMENT, comment);
 			recordToUpdate.setAttribute(ID_CURRENT_VERSION, getVersion(versionId, comment));
 		}
 
 		updateData(recordToUpdate);
 		updateExpansionComponent(recordToUpdate);
 	}
 
 	/**
 	 * If the value set record is expanded, then we need to update the expansion
 	 * record (value set entities).
 	 * 
 	 * @param recordToUpdate
 	 */
 	private void updateExpansionComponent(ListGridRecord recordToUpdate) {
 
 		// if the updated record is expanded, then we need to update it.
 		// System.out.println("Record expanded ==> " +
 		// isExpanded(recordToUpdate));
 
 		if (isExpanded(recordToUpdate) != null && isExpanded(recordToUpdate)) {
 
 			ValueSetEntitiesLayout entitiesLayout = getExpansionComponentById(recordToUpdate
 			        .getAttribute("_expansionComponentID"));
 
 			// String expansionComponentID =
 			// recordToUpdate.getAttribute("_expansionComponentID");
 			// if (expansionComponentID != null) {
 			// Canvas expansionComponent = Canvas.getById(expansionComponentID);
 			// if (expansionComponent instanceof ValueSetEntitiesLayout) {
 			//
 			// // refresh
 			// ValueSetEntitiesLayout entitiesLayout = (ValueSetEntitiesLayout)
 			// expansionComponent;
 			// Criteria criteria =
 			// entitiesLayout.getCriteriaFromValueSetRecord(recordToUpdate);
 			// entitiesLayout.updateEntitiesListGrid(criteria);
 			// }
 			// }
 
 			if (entitiesLayout != null) {
 				// refresh
 				Criteria criteria = entitiesLayout.getCriteriaFromValueSetRecord(recordToUpdate);
 				entitiesLayout.updateEntitiesListGrid(criteria);
 			}
 		}
 	}
 
 	/**
 	 * Get the expansionComponent based on an id. This id was set when creating
 	 * the expansion component.
 	 * 
 	 * @param expansionComponentID
 	 * @return
 	 */
 	private ValueSetEntitiesLayout getExpansionComponentById(String expansionComponentID) {
 
 		ValueSetEntitiesLayout entitiesLayout = null;
 
 		if (expansionComponentID != null) {
 			Canvas expansionComponent = Canvas.getById(expansionComponentID);
 			if (expansionComponent instanceof ValueSetEntitiesLayout) {
 
 				// refresh
 				entitiesLayout = (ValueSetEntitiesLayout) expansionComponent;
 			}
 		}
 		return entitiesLayout;
 	}
 
 	private ImgButton createImage(String imgName, String prompt) {
 
 		ImgButton img = new ImgButton();
 		img.setShowDown(false);
 		img.setShowRollOver(false);
 		img.setLayoutAlign(Alignment.CENTER);
 		img.setSrc(imgName);
 		img.setPrompt(prompt);
 		img.setHeight(16);
 		img.setWidth(16);
 
 		return img;
 	}
 
 }
