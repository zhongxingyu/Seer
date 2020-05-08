 package com.project.fms.admin.widgets;
 
import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.project.fms.admin.widgets.data.DoctorData;
 import com.smartgwt.client.data.DSRequest;
 import com.smartgwt.client.data.OperationBinding;
 import com.smartgwt.client.data.RestDataSource;
 import com.smartgwt.client.data.fields.DataSourceTextField;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.DSDataFormat;
 import com.smartgwt.client.types.DSOperationType;
 import com.smartgwt.client.types.DSProtocol;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
 import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
 import com.smartgwt.client.widgets.grid.events.EditFailedEvent;
 import com.smartgwt.client.widgets.grid.events.EditFailedHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 public class EditDoctor extends Canvas {
 
 	ListGrid doctorGrid;
 
 	public EditDoctor() {
 
 		RestDataSource clinicDataDs = new RestDataSource();
 
 		OperationBinding clinicFetch = new OperationBinding();
 		clinicFetch.setOperationType(DSOperationType.FETCH);
 		clinicFetch.setDataProtocol(DSProtocol.GETPARAMS);
 		clinicFetch.setDataFormat(DSDataFormat.XML);
 
 		clinicDataDs.setOperationBindings(clinicFetch);
 		clinicDataDs.setSendMetaData(false);
 		clinicDataDs.setFetchDataURL("/fds/clinics/allabbrs");
 
 		doctorGrid = new ListGrid();
 		
 		ListGridField docAbbrField = new ListGridField("doctorAbbr");
 		ListGridField docNameField = new ListGridField("doctorName");
 		ListGridField doctorTollFreeIdField = new ListGridField(
 				"doctorTollFreeId");
 		ListGridField doctorResourceIdField = new ListGridField(
 				"doctorResourceId");
 		ListGridField doctorTagAndIPField = new ListGridField(
 				"doctorComputerTagsIp");
		
 		SelectItem clinicAbbrSelect = new SelectItem("clinicAbbr",
 				"Select Clinic");
 		clinicAbbrSelect.setDisplayField("clinicAbbr");
 		clinicAbbrSelect.setOptionDataSource(clinicDataDs);
 		clinicAbbrSelect.addChangeHandler(new ChangeHandler() {
 
 			@Override
 			public void onChange(ChangeEvent event) {
 
 				doctorGrid.setDataSource(createDynamicDataSource(event
 						.getItem().getSelectedRecord()
 						.getAttributeAsString("clinicId")));
 				doctorGrid.setAutoFetchData(true);
 				doctorGrid.fetchData();
 				doctorGrid.setCanEdit(true);
 			}
 		});
 
 		doctorGrid.setWidth100();
 		doctorGrid.setHeight100();
 		
 		doctorGrid.setShowFilterEditor(true);
 		doctorGrid.setFilterOnKeypress(true);
 		doctorGrid.setCanRemoveRecords(true);
 		doctorGrid.setAutoSaveEdits(false);
 
 		doctorGrid.setFields(docAbbrField, docNameField, doctorTollFreeIdField,
 				doctorResourceIdField, doctorTagAndIPField);
 			
 //		doctorGrid.addEditCompleteHandler(new EditCompleteHandler() {
 //			
 //			@Override
 //			public void onEditComplete(EditCompleteEvent event) {
 //				SC.say("All changes added to the db!");
 //				doctorGrid.fetchData();
 //			}
 //		});
 //		
 //		doctorGrid.addEditFailedHandler(new EditFailedHandler() {
 //			
 //			@SuppressWarnings("rawtypes")
 //			@Override
 //			public void onEditFailed(EditFailedEvent event) {
 //			 System.out.println("in edit failed!");
 //			 String errorMsg = "Failed to persist the change into db!\n";
 //			 errorMsg += "Reason:";
 //			 Iterator setIter = ((Map)event.getDsResponse().getErrors()).entrySet().iterator();
 //			 while (setIter.hasNext())
 //			 {
 //				 errorMsg += ((Entry) setIter.next()).getKey().toString() + "::" 
 //						 	+ ((Entry) setIter.next()).getValue().toString() + "\n";
 //			 }
 //			 errorMsg += "Discarding changes made to that row!";
 //			 doctorGrid.discardEdits(event.getRowNum(), event.getColNum(), true);
 //			 SC.say(errorMsg);
 //			 
 //			}
 //		});
 		
 		Label titleLabel = new Label("Welcome to Edit Doctor Screen");
 		titleLabel.setWidth100();
 		titleLabel.setBorder("1px solid #808080");
 		titleLabel.setBackgroundColor("#C3D9FF");
 		titleLabel.setAlign(Alignment.CENTER);
 		titleLabel.setHeight(50);
 
 		VLayout layout = new VLayout(10);
 		layout.setWidth100();
 		layout.setHeight100();
 		layout.addMember(titleLabel);
 
 		// Form to hold the clinc abbr select item
 		DynamicForm selectForm = new DynamicForm();
 		selectForm.setItems(clinicAbbrSelect);
 
 		layout.addMember(selectForm);
 		layout.addMember(doctorGrid);
 
 		HLayout buttonLayout = new HLayout();
 
 		IButton addButton = new IButton("Add New");
 		addButton.setTop(250);
 		addButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				doctorGrid.startEditingNew(new DoctorData("", "", "", "", ""));
 			}
 		});
 
 		addButton.setWidth(120);
 		addButton.setAlign(Alignment.CENTER);
 
 		IButton submitButton = new IButton("Submit");
 		submitButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (doctorGrid.hasChanges())
 				{
 					doctorGrid.saveAllEdits();
 				}
 			}
 		});
 
 		submitButton.setWidth(120);
 		submitButton.setAlign(Alignment.CENTER);
 
 		IButton discardButton = new IButton("Discard");
 		discardButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				doctorGrid.discardAllEdits();
 			}
 
 		});
 		discardButton.setWidth(120);
 		discardButton.setAlign(Alignment.CENTER);
 
 		buttonLayout.addMember(addButton);
 		buttonLayout.addMember(submitButton);
 		buttonLayout.addMember(discardButton);
 		buttonLayout.setMembersMargin(10);
 		layout.addMember(buttonLayout);
 
 		addChild(layout);
 	}
 
 	private RestDataSource createDynamicDataSource(String clinicId) {
 		RestDataSource doctorDataDs = new RestDataSource();
 		OperationBinding fetch = new OperationBinding();
 		fetch.setOperationType(DSOperationType.FETCH);
 		fetch.setDataProtocol(DSProtocol.GETPARAMS);
 		fetch.setDataFormat(DSDataFormat.XML);
 
 		OperationBinding add = new OperationBinding();
 		add.setOperationType(DSOperationType.ADD);
 		add.setDataProtocol(DSProtocol.POSTXML);
 		add.setDataFormat(DSDataFormat.XML);
 
 		OperationBinding update = new OperationBinding();
 		update.setOperationType(DSOperationType.UPDATE);
 		update.setDataProtocol(DSProtocol.POSTXML);
 		update.setDataFormat(DSDataFormat.XML);
 
 		DSRequest putRequest = new DSRequest();
 		putRequest.setHttpMethod("PUT");
 		update.setRequestProperties(putRequest);
 
 		OperationBinding delete = new OperationBinding();
 		delete.setOperationType(DSOperationType.REMOVE);
 		delete.setDataProtocol(DSProtocol.POSTMESSAGE);
 
 		// DSRequest deleteRequest = new DSRequest();
 		// deleteRequest.setHttpMethod("DELETE");
 		// delete.setRequestProperties(deleteRequest);
 
 		doctorDataDs.setOperationBindings(fetch, add, update, delete);
 		doctorDataDs.setSendMetaData(false);
 
 		 DataSourceTextField docId = new DataSourceTextField("doctorId",
 		 "Doctor Id");
 		 docId.setPrimaryKey(true);
 		 docId.setCanEdit(false);
 
 		DataSourceTextField docAbbr = new DataSourceTextField("doctorAbbr",
 				"Doctor Abbr");
 		DataSourceTextField docName = new DataSourceTextField("doctorName",
 				"Doctor Name");
 		DataSourceTextField doctorTollFreeId = new DataSourceTextField(
 				"doctorTollFreeId", "DoctorTollFreeId");
 		DataSourceTextField doctorResourceId = new DataSourceTextField(
 				"doctorResourceId", "DoctorResourceId");
 		DataSourceTextField doctorTagAndIP = new DataSourceTextField(
 				"doctorComputerTagsIp", "DoctorComputerTag & IP");
 
 		 doctorDataDs.setFields(docId, docAbbr, docName, doctorTollFreeId,
 		 doctorResourceId, doctorTagAndIP);
 
 //		doctorDataDs.setFields(docAbbr, docName, doctorTollFreeId,
 //				doctorResourceId, doctorTagAndIP);
 
 		String dataUrl = "/fds/clinics/" + clinicId + "/doctors";
 
 		doctorDataDs.setDataURL(dataUrl);
 
 		return doctorDataDs;
 	}
 }
