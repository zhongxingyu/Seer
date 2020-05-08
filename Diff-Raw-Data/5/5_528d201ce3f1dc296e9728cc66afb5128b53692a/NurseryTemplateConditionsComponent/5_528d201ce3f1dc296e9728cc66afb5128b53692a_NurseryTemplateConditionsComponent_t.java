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
 
 package org.generationcp.breeding.manager.crosses;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 
 import org.generationcp.breeding.manager.application.Message;
 import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListWindow;
 import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporterException;
 import org.generationcp.breeding.manager.nurserytemplate.listeners.NurseryTemplateButtonClickListener;
 import org.generationcp.breeding.manager.nurserytemplate.util.NurseryTemplateManagerExporter;
 import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
 import org.generationcp.commons.util.FileDownloadResource;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.generationcp.middleware.manager.api.UserDataManager;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.Method;
 import org.generationcp.middleware.pojos.Person;
 import org.generationcp.middleware.pojos.User;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.vaadin.dialogs.ConfirmDialog;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * 
  * @author Mark Agarrado
  *
  */
 @Configurable
 public class NurseryTemplateConditionsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{
 
     /**
      * 
      */
     private static final long serialVersionUID = 6926035577490148208L;
     
     public static final String BACK_BUTTON_ID = "NurseryTemplateConditionsComponent Back Button";
     public static final String DONE_BUTTON_ID = "NurseryTemplateConditionsComponent Done Button";
     
     public static final String CONDITION_COLUMN = "Condition Column";
     public static final String DESCRIPTION_COLUMN = "Description Column";
     public static final String PROPERTY_COLUMN = "Property Column";
     public static final String SCALE_COLUMN = "Scale Column";
     public static final String VALUE_COLUMN = "Value Column";
     public static final String FEMALE_LIST="Female";
     public static final String MALE_LIST="Male";
     
     
     private Table nurseryConditionsTable;
     
     private Component buttonArea;
     private Button backButton;
     private Button doneButton;
     private TextField nid;
     private ComboBox comboBoxBreedingMethod;
     private TextField methodId;
     private ComboBox comboBoxSiteName;
     private TextField siteId;
     private ComboBox comboBoxBreedersName;
     private TextField breederId;
     private TextField femaleListName;
     private TextField femaleListId;
     private TextField maleListName;
     private TextField maleListId;
     
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
     
     @Autowired
     private GermplasmDataManager germplasmDataManager;
     
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
     
     @Autowired
     private UserDataManager userDataManager;
     
     private List<Method> method;
     private List<Location> locations;
     private List<User> users;
 
     private HashMap<String, Integer> mapBreedingMethod;
     private HashMap<String, Integer> mapSiteName;
     private HashMap<String, Integer> mapBreedersName;
 
     private NurseryTemplateMain source;
 
     public NurseryTemplateConditionsComponent(NurseryTemplateMain source) {
 	this.source=source;
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         assemble();
     }
     
     protected void assemble() {
         initializeComponents();
         initializeValues();
         initializeLayout();
         initializeActions();
     }
     
     protected void initializeComponents() {
 	
 	nid=new TextField();
 	
 	comboBoxBreedingMethod= new ComboBox();
 	comboBoxBreedingMethod.setImmediate(true);
 	
 	methodId=new TextField();
 	methodId.setImmediate(true);
 	
 	comboBoxSiteName= new ComboBox();
 	comboBoxSiteName.setImmediate(true);
 	
 	siteId=new TextField();
 	siteId.setImmediate(true);
 	
 	comboBoxBreedersName= new ComboBox();
 	comboBoxBreedersName.setImmediate(true);
 	
 	breederId=new TextField();
 	breederId.setImmediate(true);
 	
 	femaleListName=new TextField();
 	femaleListId=new TextField();
 	maleListName=new TextField();
 	maleListId=new TextField();
 	
 	generateConditionsTable();
         addComponent(nurseryConditionsTable);
         
         buttonArea = layoutButtonArea();
         addComponent(buttonArea);
         
     }
     
     protected void initializeValues() {
             
     }
     
     protected void initializeLayout() {
         setMargin(true);
         setSpacing(true);
         setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
     }
     
     protected void initializeActions() {
 	
 	backButton.addListener(new NurseryTemplateButtonClickListener(this));
 	doneButton.addListener(new NurseryTemplateButtonClickListener(this));
     }
     
     protected Component layoutButtonArea() {
         HorizontalLayout buttonLayout = new HorizontalLayout();
         buttonLayout.setSpacing(true);
         buttonLayout.setMargin(true, false, false, false);
 
         backButton = new Button();
         backButton.setData(BACK_BUTTON_ID);
         buttonLayout.addComponent(backButton);
         doneButton = new Button();
         doneButton.setData(DONE_BUTTON_ID);
         buttonLayout.addComponent(doneButton);
         return buttonLayout;
     }
     
     private void generateConditionsTable() {
         nurseryConditionsTable = new Table();
         nurseryConditionsTable.setStyleName("condition-rows");
         nurseryConditionsTable.setSizeFull();
         
         nurseryConditionsTable.addContainerProperty(CONDITION_COLUMN, String.class, null);
         nurseryConditionsTable.addContainerProperty(DESCRIPTION_COLUMN, String.class, null);
         nurseryConditionsTable.addContainerProperty(PROPERTY_COLUMN, String.class, null);
         nurseryConditionsTable.addContainerProperty(SCALE_COLUMN, String.class, null);
         nurseryConditionsTable.addContainerProperty(VALUE_COLUMN, Component.class, null);
         
         addConditionRows();
         nurseryConditionsTable.setPageLength(nurseryConditionsTable.size());
     }
     
     private void addConditionRows() {
         //TODO: populate this table using values read from the Nursery Template file
         nurseryConditionsTable.addItem(new Object[] {
                 "NID", "NURSERY SEQUENCE NUMBER", "NURSERY", "NUMBER", nid
         }, 
         "nid");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "BREEDER NAME", "PRINCIPAL INVESTIGATOR", "PERSON", "DBCV", getComboBoxBreedersName()
         }, 
         "breederName");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "BREEDER ID", "PRINCIPAL INVESTIGATOR", "PERSON", "DBID", getTextFieldBreederId()
         }, 
         "breederId");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "SITE", "NURSERY SITE NAME", "LOCATION", "DBCV", getComboBoxSiteName()
         }, 
         "site");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "SITE ID", "NURSERY SITE ID", "LOCATION", "DBID", getTextFieldSiteId()
         }, 
         "siteId");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "BREEDING METHOD", "Breeding method to be applied to this nursery", "METHOD", "DBCV", getComboBoxBreedingMethod()
         }, 
         "breedingMethod");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "BREEDING METHOD ID", "ID of Breeding Method", "METHOD", "DBID", getTextFieldMethodId()
         }, 
         "breedingMethodId");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "FEMALE LIST NAME", "FEMALE LIST NAME", "GERMPLASM LIST", "DBCV", getLayoutGermplasmListTextField(femaleListName,FEMALE_LIST)
         }, 
         "femaleListName");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "FEMALE LIST ID", "FEMALE LIST ID", "GERMPLASM LIST", "DBID", getLayoutGermplasmListTextField(femaleListId,FEMALE_LIST)
         }, 
         "femaleListId");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "MALE LIST NAME", "MALE LIST NAME", "GERMPLASM LIST", "DBCV", getLayoutGermplasmListTextField(maleListName,MALE_LIST)
         }, 
         "maleListName");
         
         nurseryConditionsTable.addItem(new Object[] {
                 "MALE LIST ID", "MALE LIST ID", "GERMPLASM LIST", "DBID", getLayoutGermplasmListTextField(maleListId,MALE_LIST)
         }, 
         "maleListId");
     }
     
     private ComboBox getComboBoxSiteName() {
 
 	mapSiteName = new HashMap<String, Integer>();
 	try {
 	    locations=germplasmDataManager.getAllBreedingLocations();
 	} catch (MiddlewareQueryException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	comboBoxSiteName.addItem("");
 	for (Location loc : locations) {
 	    if(loc.getLname().length()>0){
 		comboBoxSiteName.addItem(loc.getLname());
 		mapSiteName.put(loc.getLname(), new Integer(loc.getLocid()));
 	    }
 	}
 	
 	comboBoxSiteName.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		siteId.setValue(String.valueOf(mapSiteName.get(comboBoxSiteName.getValue())));
 	    }
 	});
 	comboBoxSiteName.select("");
 	siteId.setValue("");
 	return comboBoxSiteName;
     }
     
     private TextField getTextFieldSiteId(){
 
 	siteId.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		Location loc = new Location();
 		boolean noError=true;
 
 		try {
 		    loc = germplasmDataManager.getLocationByID(Integer.valueOf(siteId.getValue().toString()));
 		} catch (NumberFormatException e) {
 		    noError=false;
 		} catch (MiddlewareQueryException e) {
 		    noError=false;
 		}
 
 		if(loc!=null && noError){
 		    comboBoxSiteName.setValue(loc.getLname());
 		}else{
 		    getWindow().showNotification(messageSource.getMessage(Message.INVALID_SITE_ID));
 		    comboBoxSiteName.select("");
 		    siteId.setValue("");
 		}
 	    }
 	});
 
 	return siteId;
     }
     
     
     private ComboBox getComboBoxBreedersName() {
 
 	mapBreedersName = new HashMap<String, Integer>();
 	try {
 	    users=userDataManager.getAllUsers();
 	} catch (MiddlewareQueryException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	comboBoxBreedersName.addItem("");
 	setComboBoxBreederDefaultValue();
 	for (User u : users) {
 	    Person p = new Person();
 	    try {
 		p = userDataManager.getPersonById(u.getPersonid());
 	    } catch (MiddlewareQueryException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	    }
	    String name= u.getName();
	    if(p != null){
	        name = p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
	    }
 	    comboBoxBreedersName.addItem(name);
 	    mapBreedersName.put(name, new Integer(u.getUserid()));
 	}
 	
 	comboBoxBreedersName.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		breederId.setValue(String.valueOf(mapBreedersName.get(comboBoxBreedersName.getValue())));
 	    }
 	});
 	
 	return comboBoxBreedersName;
     }
     
     
     private void setComboBoxBreederDefaultValue() {
 	try {
 	    User user =workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
 	    Integer projectId= workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue();
 	    Integer ibdbUserId=workbenchDataManager.getLocalIbdbUserId(user.getUserid(),Long.valueOf(projectId));
 
 	    User u=userDataManager.getUserById(ibdbUserId);
 	    Person p=userDataManager.getPersonById(u.getPersonid());
 	    
 	    String name=p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
 	    comboBoxBreedersName.addItem(name);
 	    mapBreedersName.put(name, new Integer(u.getUserid()));
 	    comboBoxBreedersName.select(name);
 	    breederId.setValue(String.valueOf(u.getUserid()));
 	    
 	} catch (MiddlewareQueryException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
     }
 
     private TextField getTextFieldBreederId(){
 
 	breederId.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		Person p = new Person();
 		User u= new User();
 		String name="";
 		boolean noError=true;
 
 		try {
 		    u=userDataManager.getUserById(Integer.valueOf(breederId.getValue().toString()));
 		} catch (NumberFormatException e) {
 		    noError=false;
 		} catch (MiddlewareQueryException e) {
 		    noError=false;
 		}
 
 		if(u!=null && noError){
 		    try {
 			p = userDataManager.getPersonById(u.getPersonid());
 			name=p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
 		    } catch (MiddlewareQueryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		    }
 		    comboBoxBreedersName.setValue(name);
 		}else{
 		    getWindow().showNotification(messageSource.getMessage(Message.INVALID_BREEDER_ID));
 		    comboBoxBreedersName.select("");
 		    breederId.setValue("");
 		}
 	    }
 
 	});
 
 	return breederId;
     }
     
     private ComboBox getComboBoxBreedingMethod() {
 
 	mapBreedingMethod = new HashMap<String, Integer>();
 	try {
 	    method=germplasmDataManager.getMethodsByType("GEN");
 	} catch (MiddlewareQueryException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	comboBoxBreedingMethod.addItem("");
 	for (Method m : method) {
 	    if(m.getMname().length()>0){
 		comboBoxBreedingMethod.addItem(m.getMname());
 		mapBreedingMethod.put(m.getMname(), new Integer(m.getMid()));
 	    }
 	}
 	
 	comboBoxBreedingMethod.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		methodId.setValue(String.valueOf(mapBreedingMethod.get(comboBoxBreedingMethod.getValue())));
 	    }
 	});
 	comboBoxBreedingMethod.select("");
 	methodId.setValue("");
 	return comboBoxBreedingMethod;
     }
     
     private TextField getTextFieldMethodId(){
 
 	methodId.addListener(new Property.ValueChangeListener() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		Method m = new Method();
 		boolean noError=true;
 
 		try {
 		    m=germplasmDataManager.getMethodByID(Integer.valueOf(methodId.getValue().toString()));
 		} catch (NumberFormatException e) {
 		    noError=false;
 		} catch (MiddlewareQueryException e) {
 		    noError=false;
 		}
 
 		if(m!=null && noError){
 		    comboBoxBreedingMethod.setValue(m.getMname());
 		}else{
 		    getWindow().showNotification(messageSource.getMessage(Message.INVALID_METHOD_ID));
 		    comboBoxBreedingMethod.select("");
 		    methodId.setValue("");
 		}
 	    }
 	});
 
 	return methodId;
     }
     
     private VerticalLayout getLayoutGermplasmListTextField(final TextField txtField,final String germplasmListFor){
 	VerticalLayout germplasmListTextFieldLayout = new VerticalLayout();
 	germplasmListTextFieldLayout.addComponent(txtField);
 	germplasmListTextFieldLayout.addListener(new LayoutClickListener() {
 
 	    public void layoutClick(LayoutClickEvent event) {
 	        if (event.getChildComponent() == txtField) {
 	            SelectGermplasmListWindow selectListWindow = new SelectGermplasmListWindow(getMainClass(),germplasmListFor);
 	            getWindow().addWindow(selectListWindow);
 	        }
 	    }
 	});
 	
 	return germplasmListTextFieldLayout;
     }
     
     
     @Override
     public void attach() {
         super.attach();
         updateLabels();
     }
     
     @Override
     public void updateLabels() {
         messageSource.setColumnHeader(nurseryConditionsTable, CONDITION_COLUMN, Message.CONDITION_HEADER);
         messageSource.setColumnHeader(nurseryConditionsTable, DESCRIPTION_COLUMN, Message.DESCRIPTION_HEADER);
         messageSource.setColumnHeader(nurseryConditionsTable, PROPERTY_COLUMN, Message.PROPERTY_HEADER);
         messageSource.setColumnHeader(nurseryConditionsTable, SCALE_COLUMN, Message.SCALE_HEADER);
         messageSource.setColumnHeader(nurseryConditionsTable, VALUE_COLUMN, Message.VALUE_HEADER);
         
         messageSource.setCaption(backButton, Message.BACK);
         messageSource.setCaption(doneButton, Message.DONE);
     }
 
     public void backButtonClickAction() {
 	source.disableNurseryTemplateConditionsComponent();
     }
 
     public void doneButtonClickAction() {
 	String confirmDialogCaption=messageSource.getMessage(Message.CONFIRM_DIALOG_CAPTION_EXPORT_NURSERY_FILE);
 	String confirmDialogMessage=messageSource.getMessage(Message.CONFIRM_DIALOG_MESSAGE_EXPORT_NURSERY_FILE);
 
 	ConfirmDialog.show(this.getWindow(),confirmDialogCaption ,confirmDialogMessage ,
 		messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL), new ConfirmDialog.Listener() {
 		private static final long serialVersionUID = 1L;
 
 		public void onClose(ConfirmDialog dialog) {
 		    if (dialog.isConfirmed()) {
 			createAndDownloadNurseryTemplateFile();
 		    }
 		}
 	});
 
     }
     
     protected void createAndDownloadNurseryTemplateFile() {
 	// TODO Auto-generated method stub
 	ImportedGermplasmCrosses nurseryTemplateData=source.getSelectNurseryTemplateScreen().getCrossingManagerUploader().getImportedGermplasmCrosses();
 
 	String tempFileName = System.getProperty( "user.home" ) + "/temp.xls";
 	NurseryTemplateManagerExporter exporter = new NurseryTemplateManagerExporter(nurseryTemplateData,getNurseryConditionValue());
 
 	try {
 	    exporter.exportNurseryTemplateManagerExcel(tempFileName);
 	    FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
 	    fileDownloadResource.setFilename(nurseryTemplateData.getFilename());
 
 	    this.getWindow().open(fileDownloadResource);
 
 	} catch (CrossingManagerExporterException e) {
 	    MessageNotifier.showError(getWindow(), e.getMessage(), "");
 	}
 
 
     }
 
     private HashMap<String, String> getNurseryConditionValue() {
 	
 	HashMap<String, String> conditionValue=new HashMap<String,String>();
 	conditionValue.put("NID", nid.getValue().toString());
 	conditionValue.put("BREEDER NAME", comboBoxBreedersName.getValue().toString());
 	conditionValue.put("BREEDER ID", breederId.getValue().toString());
 	conditionValue.put("SITE", comboBoxSiteName.getValue().toString());
 	conditionValue.put("SITE ID", siteId.getValue().toString());
 	conditionValue.put("BREEDING METHOD", comboBoxBreedingMethod.getValue().toString());
 	conditionValue.put("BREEDING METHOD ID", methodId.getValue().toString());
 	conditionValue.put("FEMALE LIST NAME", femaleListName.getValue().toString());
 	conditionValue.put("FEMALE LIST ID", femaleListId.getValue().toString());
 	conditionValue.put("MALE LIST NAME", maleListName.getValue().toString());
 	conditionValue.put("MALE LIST ID", maleListId.getValue().toString());
 	
 	return conditionValue;
     }
 
 
     private NurseryTemplateConditionsComponent getMainClass(){
 	return this;
     }
 
     public TextField getFemaleListName() {
         return femaleListName;
     }
     
     public TextField getFemaleListId() {
         return femaleListId;
     }
     
     public TextField getMaleListName() {
         return maleListName;
     }
     
     public TextField getMaleListId() {
         return maleListId;
     }
 
 }
