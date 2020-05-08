 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.browser.germplasm;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.generationcp.browser.application.Message;
 import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.api.GermplasmListManager;
 import org.generationcp.middleware.pojos.GermplasmList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 
 
 @Configurable
 public class SaveGermplasmListDialog extends GridLayout implements InitializingBean, InternationalizableComponent,Property.ValueChangeListener, AbstractSelect.NewItemHandler{
 
 	private static final Logger LOG = LoggerFactory.getLogger(SaveGermplasmListDialog.class);
 	private static final long serialVersionUID = 1L;
 	public static final Object SAVE_BUTTON_ID = "Save Germplasm List";
 	public static final String CANCEL_BUTTON_ID = "Cancel Saving";
 	private Label labelListName;
 	private Label labelDescription;
 	private TextField txtDescription;
 	private Label labelType;
 	private TextField txtType;
 	private Window dialogWindow;
 	private Window mainWindow;
 
 	@Autowired
 	private GermplasmListManager germplasmListManager;
 
 	@Autowired
 	private SimpleResourceBundleMessageSource messageSource;
 	private Button btnSave;
 	private Button btnCancel;
 	private TabSheet tabSheet;
 	private ComboBox comboBoxListName;
 	private Select selectType;
 	private List<GermplasmList> germplasmList;
 	private boolean lastAdded = false;
 	private Map<String,Integer> mapExistingList;
 
 
 	public SaveGermplasmListDialog(Window mainWindow, Window dialogWindow, TabSheet tabSheet) {
 		this.dialogWindow = dialogWindow;
 		this.mainWindow = mainWindow;
 		this.tabSheet = tabSheet;
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		setRows(8);
 		setColumns(3);
 		setSpacing(true);
 		setMargin(true);
 
 		labelListName = new Label();
 		labelDescription = new Label();
 		labelType = new Label();
 
 		comboBoxListName = new ComboBox();
 		populateComboBoxListName();
 		comboBoxListName.setNewItemsAllowed(true);
 		comboBoxListName.setNewItemHandler(this);
 		comboBoxListName.setNullSelectionAllowed(false);
 		comboBoxListName.addListener(this);
 
 		txtDescription = new TextField();
 		txtDescription.setWidth("400px");
 
 		txtType = new TextField();
 		txtType.setWidth("200px");
 
 		selectType = new Select ();
 		populateSelectType(selectType);
 		selectType.setNullSelectionAllowed(false);
 		selectType.select("LST");
 
 		HorizontalLayout hButton = new HorizontalLayout();
 		hButton.setSpacing(true);
 		btnSave = new Button();
 		btnSave.setWidth("80px");
 		btnSave.setData(SAVE_BUTTON_ID);
 		btnSave.setDescription("Save Germplasm List ");
 		btnSave.addListener(new GermplasmButtonClickListener(this));
 
 		hButton.addComponent(btnSave);
 		btnCancel = new Button();
 		btnCancel.setWidth("80px");
 		btnCancel.setData(CANCEL_BUTTON_ID);
 		btnCancel.setDescription("Cancel Saving Germplasm List");
 		btnCancel.addListener(new GermplasmButtonClickListener(this));
 		hButton.addComponent(btnCancel);
 
 		addComponent(labelListName, 1, 1);
 		addComponent(comboBoxListName, 2, 1);
 		addComponent(labelDescription, 1,2);
 		addComponent(txtDescription, 2, 2);
 		addComponent(labelType, 1,3);
 		addComponent(selectType, 2, 3);
 		addComponent(hButton, 1, 6);
 	}
 
 
 
 	private void populateComboBoxListName() throws MiddlewareQueryException {
 		// TODO Auto-generated method stub
 		germplasmList=germplasmListManager.getAllGermplasmLists(0, 100, Database.LOCAL);
 		mapExistingList= new HashMap<String,Integer>();
 		comboBoxListName.addItem("");
 		for(GermplasmList gList:germplasmList){
 			comboBoxListName.addItem(gList.getName());
 			mapExistingList.put(gList.getName(),new Integer(gList.getId()));
 		}
 		comboBoxListName.select("");
 	}
 
 	private void populateSelectType(Select selectType) {
 		selectType.addItem("LST");
 		selectType.addItem("HB");
 		selectType.addItem("F1");
 		selectType.addItem("F2");
 		selectType.addItem("PN");
 		selectType.addItem("OYT");
 		selectType.addItem("RYT");
 		selectType.addItem("FOLDER");
 		selectType.addItem("EXTACQ");
 		selectType.addItem("EXTREQ");
 		selectType.addItem("INTREQ");
 		selectType.addItem("COLLMIS");
 		selectType.addItem("INTACQ");
 		selectType.addItem("SI");
 		selectType.addItem("SEEDSTCK");
 		selectType.addItem("TRNGENC");
 	}
 
 	@Override
 	public void attach() {
 		super.attach();
 		updateLabels();
 	}
 
 	@Override
 	public void updateLabels() {
 		messageSource.setCaption(labelListName, Message.LIST_NAME_LABEL);
 		messageSource.setCaption(labelDescription, Message.DESCRIPTION_LABEL);
 		messageSource.setCaption(labelType, Message.TYPE_LABEL);
 		messageSource.setCaption(btnSave, Message.SAVE_LABEL);
 		messageSource.setCaption(btnCancel, Message.CANCEL_LABEL);
 	}
 
 	public void saveGermplasmListButtonClickAction() throws InternationalizableException {
 
 		SaveGermplasmListAction saveGermplasmAction = new SaveGermplasmListAction();
 		String listName = comboBoxListName.getValue().toString();
 		String listNameId=String.valueOf(mapExistingList.get(comboBoxListName.getValue()));
 
 		if(listName.trim().length()==0){
 
 			getWindow().showNotification(
 					"List Name Input Error...",
 					"Please specify a List Name before saving",
 					Notification.TYPE_WARNING_MESSAGE);
 
 		}else if (listName.trim().length() > 50  ) {
 			
 			getWindow().showNotification(
 					"List Name Input Error...",
 					"Listname input is too large limit the name only up to 50 characters",
 					Notification.TYPE_WARNING_MESSAGE);
 			comboBoxListName.setValue("");
 
 		}else{
 			saveGermplasmAction.addGermplasListNameAndData(listName,listNameId, this.tabSheet,txtDescription.getValue().toString(),selectType.getValue().toString());
 			closeSavingGermplasmListDialog();
 		}
 	}
 
 	public void cancelGermplasmListButtonClickAction() {
 		closeSavingGermplasmListDialog();
 	}
 
 	public void closeSavingGermplasmListDialog() {
 		this.mainWindow.removeWindow(dialogWindow);
 	}
 
 	/*
 	 * Shows a notification when a selection is made.
 	 */
 	public void valueChange(ValueChangeEvent event) {
 		if (!lastAdded) {
 			String listNameId=String.valueOf(mapExistingList.get(comboBoxListName.getValue()));
 			if(listNameId!="null"){
 				GermplasmList gList=germplasmListManager.getGermplasmListById(Integer.valueOf(listNameId));
 				txtDescription.setValue(gList.getDescription());
 				txtDescription.setEnabled(false);
 				selectType.select(gList.getType());
 				selectType.setEnabled(false);
 			}else{
 				txtDescription.setValue("");
 				txtDescription.setEnabled(true);
 				selectType.select("LST");
 				selectType.setEnabled(true);
 			}
 		}else{
 			txtDescription.setValue("");
 			txtDescription.setEnabled(true);
 			selectType.select("LST");
 			selectType.setEnabled(true);
 		}
 		lastAdded = false;
 	}
 
 	@Override
 	public void addNewItem(String newItemCaption) {
 		if (!comboBoxListName.containsId(newItemCaption)) {
 			lastAdded = true;
 			comboBoxListName.addItem(newItemCaption);
 			comboBoxListName.setValue(newItemCaption);
 		}
 
 	}
 }
