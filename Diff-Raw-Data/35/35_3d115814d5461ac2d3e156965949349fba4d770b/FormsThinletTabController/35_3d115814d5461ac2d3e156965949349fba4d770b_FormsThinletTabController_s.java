 /**
  * 
  */
 package net.frontlinesms.plugins.forms.ui;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import thinlet.Thinlet;
 
 import net.frontlinesms.csv.CsvExporter;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 import net.frontlinesms.plugins.forms.FormsPluginController;
 import net.frontlinesms.plugins.forms.csv.CsvFormExporter;
 import net.frontlinesms.plugins.forms.data.domain.*;
 import net.frontlinesms.plugins.forms.data.repository.*;
 import net.frontlinesms.plugins.forms.ui.components.*;
 import net.frontlinesms.plugins.BasePluginThinletTabController;
 import net.frontlinesms.ui.FileChooser;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.events.TabChangedNotification;
 import net.frontlinesms.ui.handler.ComponentPagingHandler;
 import net.frontlinesms.ui.handler.PagedComponentItemProvider;
 import net.frontlinesms.ui.handler.PagedListDetails;
 import net.frontlinesms.ui.handler.contacts.GroupSelecterDialog;
 import net.frontlinesms.ui.handler.contacts.SingleGroupSelecterDialogOwner;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.TextResourceKeyOwner;
 
 /**
  * Thinlet controller class for the FrontlineSMS Forms plugin.
  * @author Alex
  */
 @TextResourceKeyOwner(prefix={"I18N_", "COMMON_", "SENTENCE_", "TOOLTIP_"})
 public class FormsThinletTabController extends BasePluginThinletTabController<FormsPluginController> implements SingleGroupSelecterDialogOwner, PagedComponentItemProvider, EventObserver {
 //> CONSTANTS
 	/** XML file containing forms pane for viewing results of a form */
 	protected static final String UI_FILE_RESULTS_VIEW = "/ui/plugins/forms/formsTab_resultsView.xml";
 	/** XML file containing dialog for exporting form data */
 	private static final String UI_FILE_FORM_EXPORT_DIALOG = "/ui/plugins/forms/formExportDialogForm.xml";
 	/** XML file containing dialog for choosing which contacts to send a form to */
 	private static final String XML_CHOOSE_CONTACTS = "/ui/plugins/forms/dgChooseContacts.xml";
 	
 	/** Component name of the forms list */
 	private static final String FORMS_LIST_COMPONENT_NAME = "formsList";
 	
 //> I18N KEYS
 	/** i18n key: "Form Name" */
 	static final String I18N_KEY_FORM_NAME = "plugins.forms.editor.name.label";
 	/** i18n key: "Form Editor" */
 	static final String I18N_KEY_FORMS_EDITOR = "plugins.forms.editor.title";
 	/** i18n key: "You have not entered a name for this form" */
 	static final String I18N_KEY_MESSAGE_FORM_NAME_BLANK = "plugins.forms.name.blank";
 	/** i18n key: "You will not be able to edit this form again." */
 	private static final String I18N_KEY_CONFIRM_FINALISE = "plugins.forms.send.finalise.confirm";
 	/** i18n key: "There are no contacts to notify." */
 	private static final String I18N_KEY_NO_CONTACTS_TO_NOTIFY = "plugins.forms.send.nocontacts";
 	/** i18n key: "Form submitter" */
 	public static final String I18N_FORM_SUBMITTER = "plugins.forms.submitter";
 	/** i18n key: "Your form 'formname' has been sent to N contacts." */
 	private static final String I18N_FORM_SENT_DIALOG_MESSAGE = "plugins.forms.message.form.sent";
 
 	/** i18n key: "Currency field" */
 	public static final String I18N_FCOMP_CURRENCY = "plugins.forms.field.currency";
 	public static final String I18N_FCOMP_DROP_DOWN_LIST = "plugins.forms.field.dropdownlist";
 	public static final String I18N_FCOMP_NUMBER = "plugins.forms.field.number";
 	public static final String I18N_FCOMP_PASSWORD = "plugins.forms.field.password";
 	public static final String I18N_FCOMP_PHONENUMBER = "plugins.forms.field.phonenumber";
 	public static final String I18N_FCOMP_TEXT_AREA = "plugins.forms.field.textarea";
 	public static final String I18N_FCOMP_TEXT_FIELD = "plugins.forms.field.textfield";
 	public static final String I18N_FCOMP_CHECKBOX = "plugins.forms.field.checkbox";
 	public static final String I18N_FCOMP_TIME = "common.time";
 	public static final String I18N_FCOMP_TRUNCATED_TEXT = "plugins.forms.field.truncatedtext";
 	public static final String I18N_FCOMP_WRAPPED_TEXT = "plugins.forms.field.wrappedtext";
 
 	public static final String COMMON_PALETTE = "plugins.forms.palette";
 	public static final String COMMON_PREVIEW = "plugins.forms.preview";
 	public static final String COMMON_PROPERTY = "plugins.forms.property";
 	public static final String COMMON_VALUE = "plugins.forms.value";
 	public static final String TOOLTIP_DRAG_TO_REMOVE = "plugins.forms.tooltip.drag.to.remove";
 	public static final String TOOLTIP_DRAG_TO_PREVIEW = "plugins.forms.tooltip.drag.to.preview";
 	public static final String SENTENCE_DELETE_KEY = "plugins.forms.sentence.delete.key";
 	public static final String SENTENCE_UP_KEY = "plugins.forms.sentence.up.key";
 	public static final String SENTENCE_DOWN_KEY = "plugins.forms.sentence.down.key";
 	
 	private static final String MESSAGE_NO_FILENAME = "message.filename.blank";
 	private static final String MESSAGE_EXPORT_TASK_SUCCESSFUL = "message.export.successful";
 	private static final String MESSAGE_EXPORT_TASK_FAILED = "message.export.failed";
 	private static final String MESSAGE_BAD_DIRECTORY = "message.bad.directory";
 	private static final String MESSAGE_CONFIRM_FILE_OVERWRITE = "message.file.overwrite.confirm";
	private static final String I18N_KEY_SET_GROUP_BEFORE = "plugins.forms.set.group.before";
 	
 	private static final Object UI_FORM_TAB_NAME = ":forms";
 	
 //> INSTANCE PROPERTIES
 	/** DAO for {@link Contact}s */
 	private ContactDao contactDao;
 	/** DAO for {@link Form}s */
 	private FormDao formsDao;
 	/** DAO for {@link FormResponse}s */
 	private FormResponseDao formResponseDao;
 	/** DAO for getting {@link Contact}s in {@link Group}s */
 	private GroupMembershipDao groupMembershipDao;
 
 	/** UI table displaying the results. */
 	private Object formResultsComponent;
 	/** Paging handler for the results component */
 	private ComponentPagingHandler formResponseTablePageControls;
 	
 	private Object exportDialog;
 
 //> CONSTRUCTORS
 	public FormsThinletTabController(FormsPluginController pluginController, UiGeneratorController ui) {
 		super(pluginController, ui);
 		
 		this.ui.getFrontlineController().getEventBus().registerObserver(this);
 	}
 	
 //> INSTANCE METHODS	
 	/** Refresh the tab's display. */
 	public void refresh() {
 		Object formList = getFormsList();
 		
 		// If there was something selected previously, we will attempt to select it again after updating the list
 		Object previousSelectedItem = this.ui.getSelectedItem(formList);
 		Form previousSelectedForm = previousSelectedItem == null ? null : this.getForm(previousSelectedItem);
 		ui.removeAll(formList);
 		Object newSelectedItem = null;
 		for(Form f : formsDao.getAllForms()) {
 			Object formNode = getNode(f);
 			ui.add(formList, formNode);
 			if (previousSelectedForm != null && f.getFormMobileId() ==  previousSelectedForm.getFormMobileId()) {
 				newSelectedItem = formNode;
 			}
 		}
 		
 		// Restore the selected item
 		if(newSelectedItem != null) {
 			this.ui.setSelectedItem(formList, newSelectedItem);
 		}
 
 		// We should enable or disable buttons as appropriate
 		formsList_selectionChanged();
 	}
 	
 //> THINLET EVENT METHODS
 	public void showFormsPluginInfo() {
 		FormsAboutDialogHandler.createAndShow(this.ui, this.getPluginController());
 	}
 	
 	/** Show the dialog for exporting form results. */
 	public void showFormExportDialog() {
 		exportDialog = ui.loadComponentFromFile(UI_FILE_FORM_EXPORT_DIALOG, this);
 		ui.add(exportDialog);
 	}
 	
 	public void showSaveModeFileChooser (Object textFieldToBeSet) {
 		FileChooser fc = FileChooser.createFileChooser(this.ui, this, "saveChooseComplete");
 		fc.show();
 	}
 	
 	public void saveChooseComplete(String filename) {
 		this.ui.setText(this.ui.find("tfFilename"), filename);
 	}
 	
 	/** Show the AWT Forms Editor window */
 	public void showFormsEditor() {
 		VisualForm form = new VisualForm();
 		form = FormsUiController.getInstance().showFormsEditor(ui.getFrameLauncher(), form);
 		if (form != null) {
 			saveFormInformation(form);
 		}
 	}
 	
 	public void removeSelected(Object component) {
 		Object[] selected = this.ui.getSelectedItems(component);
 		if(selected != null) {
 			for(Object selectedComponent : selected) {
 				this.ui.remove(selectedComponent);
 			}
 		}
 	}
 	
 	/**
 	 * Calls the export method according to the supplied information,
 	 * and the user selection.
 	 * 
 	 * @param aggregate
 	 * @param dataPath
 	 * @param exportDialog
 	 */
 	public void formsTab_exportResults(String dataPath) {
 		if (!dataPath.contains(File.separator) || !(new File(dataPath.substring(0, dataPath.lastIndexOf(File.separator))).isDirectory())) {
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_BAD_DIRECTORY));
 		} else if (dataPath.substring(dataPath.lastIndexOf(File.separator), dataPath.length()).equals(File.separator)) {
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_FILENAME));
 		} else {
 			log.debug("Filename is [" + dataPath + "] before [" + CsvExporter.CSV_EXTENSION + "] check.");
 			if (!dataPath.endsWith(CsvExporter.CSV_EXTENSION)) {
 				dataPath += CsvExporter.CSV_EXTENSION;
 			}
 			log.debug("Filename is [" + dataPath + "] after [" + CsvExporter.CSV_EXTENSION + "] check.");
 			
 			File csvFile = new File(dataPath);
 			if(csvFile.exists() && csvFile.isFile()) {
 				// Show confirmation dialog
 				ui.showConfirmationDialog("doExport('" + dataPath + "')", this, MESSAGE_CONFIRM_FILE_OVERWRITE);
 			} else {
 				doExport(dataPath);
 			}
 		}
 	}
 
 	public void doExport(String filename) {
 		Object formsList = find("formsList");
 		Form selectedForm = getForm(ui.getSelectedItem(formsList));
 		
 		if (selectedForm == null) return;
 		
 		File file = new File(filename);
 		try {
 			CsvFormExporter.exportForm(file, selectedForm, contactDao, formResponseDao);
 			
 			this.ui.setStatus(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
 		}
 		catch (IOException e) {
 			log.debug(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_FAILED), e);
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_EXPORT_TASK_FAILED));
 		} finally {
 			removeDialog(exportDialog);
 		}
 	}
 
 	/**
 	 * Called when the user has selected a different item on the forms tree.
 	 * @param formsList
 	 */
 	public void formsList_selectionChanged() {
 		Form selectedForm = getForm(ui.getSelectedItem(getFormsList()));
 		
 		if (selectedForm != null && selectedForm.isFinalised()) {
 			showResultsPanel(selectedForm);
 		} else {
 			//Nothing selected
 			Object pnRight = find("pnRight");
 			ui.removeAll(pnRight);
 		}
 
 		enableMenuOptions(find("formsList_toolbar"));
 	}
 	
 	/**
 	 * Show the GUI to edit a form.
 	 * @param list Reference to the Forms tree object.
 	 */
 	public void formsList_editSelected() {
 		Form selectedForm = getSelectedForm();
 		if (selectedForm != null && !selectedForm.isFinalised()) {
 			VisualForm visualForm = VisualForm.getVisualForm(selectedForm);
 			List<PreviewComponent> old = new ArrayList<PreviewComponent>();
 			old.addAll(visualForm.getComponents());
 			visualForm = FormsUiController.getInstance().showFormsEditor(ui.getFrameLauncher(), visualForm);
 			if (visualForm != null) {
 				if (!visualForm.getName().equals(selectedForm.getName())) {
 					selectedForm.setName(visualForm.getName());
 				}
 				updateForm(old, visualForm.getComponents(), selectedForm);
 				formsList_selectionChanged();
 			}
 		}
 	}
 	
 	/** Shows a selecter for assigning a {@link Group} to a {@link Form} */
 	public void formsList_showGroupSelecter() {
 		Form selectedForm = getSelectedForm();
 		log.info("FormsThinletTabController.showGroupSelecter() : " + selectedForm);
 		if(selectedForm != null) {
			// FIXME i18n
//			ui.showGroupSelecter(selectedForm, false, "Choose a group", "setSelectedGroup(groupSelecter, groupSelecter_groupList)", this);
 			GroupSelecterDialog selecter = new GroupSelecterDialog(ui, this);
			selecter.init("Choose a group", ui.getRootGroup());
 			selecter.show();
 		}
 	}
 	
 	public void groupSelectionCompleted(Group group) {
 		// TODO Auto-generated method stub
 		Form form = getSelectedForm();
 		log.info("Form: " + form);
 		log.info("Group: " + group);
 		if(group != null) {
 			// Set the permitted group for this form, then save it
 			form.setPermittedGroup(group);
 			this.formsDao.updateForm(form);
 			this.refresh();
 		}
 	}
 
 	/**
 	 * @param groupSelecter
 	 * @param groupList
 	 */
 	public void setSelectedGroup(Object groupSelecter, Object groupList) {
 		Form form = getForm(groupSelecter);
 		log.info("Form: " + form);
 		Group group = ui.getGroup(ui.getSelectedItem(groupList));
 		log.info("Group: " + group);
 		if(group != null) {
 			// Set the permitted group for this form, then save it
 			form.setPermittedGroup(group);
 			this.formsDao.updateForm(form);
 			this.refresh();
 			
 			removeDialog(groupSelecter);
 		}
 	}
 	
 	/**
 	 * Attempt to send the form selected in the forms list
 	 * @param formsList the forms list component
 	 */
 	public void formsList_sendSelected() {
 		Form selectedForm = getSelectedForm();
 		if(selectedForm != null) {
 			// check the form has a group set
 			if(selectedForm.getPermittedGroup() == null) {
 				// The form has no group set, so we should explain that this needs to be done.
 				// FIXME i18n
 				ui.alert(InternationalisationUtils.getI18NString(I18N_KEY_SET_GROUP_BEFORE));
 			} else if(!selectedForm.isFinalised()) { // check the form is finalised.
 				// if form is not finalised, warn that it will be!
 				ui.showConfirmationDialog("showSendSelectionDialog", this, I18N_KEY_CONFIRM_FINALISE);
 			} else {
 				// show dialog for selecting group members to send the form to
 				showSendSelectionDialog();
 			}
 		}
 	}
 	
 	/**
 	 * Show dialog for selecting users to send a form to.  If the form is not finalised, it will be
 	 * finalised within this method.
 	 */
 	public void showSendSelectionDialog() {
 		ui.removeConfirmationDialog();
 		
 		Form form = getSelectedForm();
 		if(form != null) {
 			// if form is not finalised, finalise it now
 			if(!form.isFinalised()) {
 				formsDao.finaliseForm(form);
 				this.refresh();
 			}
 			
 			// show selection dialog for Contacts in the form's group
 			Object chooseContactsDialog = ui.loadComponentFromFile(XML_CHOOSE_CONTACTS, this);
 			ui.setAttachedObject(chooseContactsDialog, form);
 			
 			// Add each contact in the group to the list.  The user can then remove any contacts they don't
 			// want to be sent an SMS about the form at this time.
 			Object contactList = ui.find(chooseContactsDialog, "lsContacts");
 			for(Contact contact : this.groupMembershipDao.getActiveMembers(form.getPermittedGroup())) {
 				Object listItem = ui.createListItem(contact.getDisplayName(), contact);
 				ui.add(contactList, listItem);
 			}
 			ui.add(chooseContactsDialog);
 		}
 	}
 	
 	/**
 	 * Send a form to the contacts selected in the dialog.
 	 * @param dgChooseContacts Dialog containing the contact selection
 	 */
 	public void sendForm(Object dgChooseContacts) {
 		// Work out which contacts we should be sending the form to
 		Object[] recipientItems = ui.getItems(ui.find(dgChooseContacts, "lsContacts"));
 		Form form = getForm(dgChooseContacts);
 		if(recipientItems.length == 0) {
 			// There are no contacts in the "send to" list.  We should remove the dialog and inform the user
 			// of the problem.
 			ui.alert(InternationalisationUtils.getI18NString(I18N_KEY_NO_CONTACTS_TO_NOTIFY));
 			ui.removeDialog(dgChooseContacts);
 		} else {
 			HashSet<Contact> selectedContacts = new HashSet<Contact>();
 			for(Object o : recipientItems) {
 				Object attachment = ui.getAttachedObject(o);
 				if(attachment instanceof Contact) {
 					selectedContacts.add((Contact)attachment);
 				} else if(attachment instanceof Group) {
 					Group g = (Group)attachment;
 					selectedContacts.addAll(this.groupMembershipDao.getActiveMembers(g));
 				}
 			}
 		
 			// Issue the send command to the plugin controller
 			this.getPluginController().sendForm(form, selectedContacts);
 
 			ui.alert(InternationalisationUtils.getI18NString(I18N_FORM_SENT_DIALOG_MESSAGE, form.getName(), Integer.toString(selectedContacts.size())));
 			
 			ui.removeDialog(dgChooseContacts);
 		}
 	}
 	
 	/** Finds the forms list and deletes the selected item. */
 	public void formsList_deleteSelected() {
 		Form selectedForm = getSelectedForm();
 		if(selectedForm != null) {
 			this.formsDao.deleteForm(selectedForm);
 		}
 		this.refresh();
 		// Now remove the confirmation dialog.
 		ui.removeConfirmationDialog();
 	}
 	
 	/**
 	 * Duplicates the selected form.
 	 * @param formsList
 	 */
 	public void formsList_duplicateSelected() {
 		Form selected = getSelectedForm();
 		assert(selected != null) : "Duplicate Form button should not be enabled if there is no form selected!";
 		
 		Form clone = new Form(selected.getName() + '*');
 		for (FormField oldField : selected.getFields()) {
 			FormField newField = new FormField(oldField.getType(), oldField.getLabel());
 			clone.addField(newField, oldField.getPositionIndex());
 		}
 		this.formsDao.saveForm(clone);
 		this.refresh();
 	}
 	
 	/** Form selection has changed, so decide which toolbar and popup options should be available considering the current selection. */
 	public void formsTab_enabledFields(Object formsList_toolbar, Object formsList_popupMenu) {
 		enableMenuOptions(formsList_toolbar);
 		enableMenuOptions(formsList_popupMenu);
 	}
 	
 	/**
 	 * Enable menu options for the supplied menu component.
 	 * @param menuComponent Menu component, a button bar or popup menu
 	 * @param selectedComponent The selected object of the control that this menu applied to
 	 */
 	private void enableMenuOptions(Object menuComponent) {
 		Object selectedComponent = formsList_getSelected();
 		Form selectedForm = getForm(selectedComponent);
 		for (Object o : ui.getItems(menuComponent)) {
 			String name = ui.getName(o);
 			if(name != null) {
 				if (ui.getItems(getFormsList()).length == 0) {
 					ui.setVisible(o, (!name.startsWith("mi") && !name.startsWith("sp")) || name.endsWith("New"));
 				} else {
 					ui.setVisible(o, true);
 					if (name.contains("Delete")) {
 						// Tricky to remove the component for a form when the field is selected.  If someone wants to
 						// solve that, they're welcome to enable delete here for FormFields
 						ui.setEnabled(o, ui.getAttachedObject(selectedComponent) instanceof Form);
 					} else if (name.contains("Edit")) {
 						ui.setEnabled(o, selectedForm != null && !selectedForm.isFinalised());
 					} else if (name.contains("New")) {
 						ui.setEnabled(o, true);
 					} else {
 						ui.setEnabled(o, selectedForm != null);
 					}
 				}
 			}
 		}
 	}
 	
 	public PagedListDetails getListDetails(Object list, int startIndex, int limit) {
 		Form selectedForm = getSelectedForm();
 		int totalItemCount = this.formResponseDao.getFormResponseCount(selectedForm);
 
 		ArrayList<Object> responseRows = new ArrayList<Object>();
 		for (FormResponse response : formResponseDao.getFormResponses(selectedForm, startIndex, limit)) {
 			Object row = getRow(response);
 			responseRows.add(row);
 		}
 		
 		return new PagedListDetails(totalItemCount, responseRows.toArray(new Object[0]));
 	}
 	
 	/** Update the results for the selected form, taking into account the page number as well. */
 	public void formsTab_updateResults() {
 		this.formResponseTablePageControls.refresh();
 	}
 	
 	/**
 	 * Shows a confirmation dialog before calling a method.  The method to be called
 	 * is passed in as a string, and then called using reflection.
 	 * @param methodToBeCalled
 	 */
 	public void showFormConfirmationDialog(String methodToBeCalled){
 		ui.showConfirmationDialog(methodToBeCalled, this);
 	}
 
 //> THINLET EVENT HELPER METHODS
 	/** @return the {@link Form} selected in the {@link #getFormsList()}, or <code>null</code> if none is selected */
 	private Form getSelectedForm() {
 		Object selectedComponent = formsList_getSelected();
 		if(selectedComponent == null) return null;
 		else return getForm(selectedComponent);
 	}
 	
 	/** @return gets the ui component selected in the forms list */
 	private Object formsList_getSelected() {
 		return this.ui.getSelectedItem(getFormsList());
 	}
 
 	/** @return the forms list component */
 	private Object getFormsList() {
 		return find(FormsThinletTabController.FORMS_LIST_COMPONENT_NAME);
 	}
 	
 	/** Given a {@link VisualForm}, the form edit window, this saves its details. */
 	private void saveFormInformation(VisualForm visualForm) {
 		Form form = new Form(visualForm.getName());
 		for (PreviewComponent comp : visualForm.getComponents()) {
 			FormFieldType fieldType = FComponent.getFieldType(comp.getComponent().getClass());
 			FormField newField = new FormField(fieldType, comp.getComponent().getLabel());
 			form.addField(newField);
 		}
 		this.formsDao.saveForm(form);
 		this.refresh();
 	}
 	
 	private void updateForm(List<PreviewComponent> old, List<PreviewComponent> newComp, Form form) {
 		//Let's remove from database the ones the user removed
 		List<PreviewComponent> toRemove = new ArrayList<PreviewComponent>();
 		for (PreviewComponent c : old) {
 			if (!newComp.contains(c)) {
 				form.removeField(c.getFormField());
 				toRemove.add(c);
 			}
 		}
 		// Compare the lists
 		for (PreviewComponent c : newComp) {
 			if (c.getFormField() != null) {
 				FormField ff = c.getFormField();
 				if (ff.getPositionIndex() != newComp.indexOf(c)) {
 					ff.setPositionIndex(newComp.indexOf(c));
 				}
 				ff.setLabel(c.getComponent().getLabel());
 			} else {
 				FormFieldType fieldType = FComponent.getFieldType(c.getComponent().getClass());
 				FormField newField = new FormField(fieldType, c.getComponent().getLabel());
 				form.addField(newField, newComp.indexOf(c));
 			}
 		}
 		
 		this.formsDao.updateForm(form);
 		this.refresh();
 	}
 	
 	/** Adds the result panel to the forms tab. */
 	private void addFormResultsPanel() {
 		Object pnRight = find("pnRight");
 		ui.removeAll(pnRight);
 		Object resultsView = ui.loadComponentFromFile(UI_FILE_RESULTS_VIEW, this);
 
 		formResultsComponent = ui.find(resultsView, "formResultsList");
 		this.formResponseTablePageControls = new ComponentPagingHandler(ui, this, this.formResultsComponent);
 
 		Object placeholder = ui.find(resultsView, "pageControlsPanel");
 		int index = ui.getIndex(ui.getParent(placeholder), placeholder);
 		ui.add(ui.getParent(placeholder), this.formResponseTablePageControls.getPanel(), index);
 		ui.remove(placeholder);
 		
 		ui.add(pnRight, resultsView);
 	}
 
 	/**
 	 * Adds the form results panel to the GUI, and refreshes it for the selected form.
 	 * @param selected The form whose results should be displayed.
 	 */
 	private void showResultsPanel(Form selected) {
 		addFormResultsPanel();
 		Object pagePanel = find("pagePanel");
 		ui.setVisible(pagePanel, true);
 		Object pnResults = find("pnFormResults");
 		ui.setInteger(pnResults, "columns", 2);
 		
 		form_createColumns(selected);
 		formsTab_updateResults();
 		
 		ui.setEnabled(formResultsComponent, selected != null && ui.getItems(formResultsComponent).length > 0);
 		ui.setEnabled(find("btExportFormResults"), selected != null && ui.getItems(formResultsComponent).length > 0);
 	}
 
 	/**
 	 * @param selectedComponent Screen component's selectedItem
 	 * @return a {@link Form} if a form or formfield was selected, or <code>null</code> if none could be found
 	 */
 	private Form getForm(Object selectedComponent) {
 		Object selectedAttachment = ui.getAttachedObject(selectedComponent);
 		if (selectedAttachment == null
 				|| !(selectedAttachment instanceof Form)) {
 			// The selected item was not a form item, so probably was a child of that.  Get it's parent, and check if that was a form instead
 			selectedAttachment = this.ui.getAttachedObject(this.ui.getParent(selectedComponent));
 		}
 		
 		if (selectedAttachment == null
 				|| !(selectedAttachment instanceof Form)) {
 			// No form was found; return null
 			return null;
 		} else {
 			return (Form) selectedAttachment;
 		}
 	}
 	
 	/**
 	 * Gets {@link Thinlet} table row component for the supplied {@link FormResponse}
 	 * @param response the {@link FormResponse} to represent as a table row
 	 * @return row component to insert in a thinlet table
 	 */
 	private Object getRow(FormResponse response) {
 		Object row = ui.createTableRow(response);
 		Contact sender = contactDao.getFromMsisdn(response.getSubmitter());
 		String senderDisplayName = sender != null ? sender.getDisplayName() : response.getSubmitter();
 		ui.add(row, ui.createTableCell(senderDisplayName));
 		for (ResponseValue result : response.getResults()) {
 			ui.add(row, ui.createTableCell(result.toString()));
 		}
 		return row;
 	}
 	
 	/**
 	 * Creates a {@link Thinlet} tree node for the supplied form.
 	 * @param form The form to represent as a node.
 	 * @return node to insert in thinlet tree
 	 */
 	private Object getNode(Form form) {
 		log.trace("ENTER");
 		// Create the node for this form
 		
 		log.debug("Form [" + form.getName() + "]");
 		
 		Image icon = getIcon(form.isFinalised() ? FormIcon.FORM_FINALISED: FormIcon.FORM);
 		Object node = ui.createNode(form.getName(), form);
 		ui.setIcon(node, Thinlet.ICON, icon);
 
 		// Create a node showing the group for this form
 		Group g = form.getPermittedGroup();
		// FIXME i18n
		String groupName = g == null ? "(not set)" : g.getName();
		// FIXME i18n
		Object groupNode = ui.createNode("Group: " + groupName, null);
 		ui.setIcon(groupNode, Icon.GROUP);
 		ui.add(node, groupNode);
 		
 		for (FormField field : form.getFields()) {
 			Object child = ui.createNode(field.getLabel(), field);
 			ui.setIcon(child, Thinlet.ICON, getIcon(field.getType()));
 			ui.add(node, child);
 		}
 		log.trace("EXIT");
 		return node;
 	}
 
 	private void form_createColumns(Form selected) {
 		Object resultsTable = find("formResultsList");
 		Object header = Thinlet.get(resultsTable, Thinlet.HEADER);
 		ui.removeAll(header);
 		if (selected != null) {
 			// FIXME check if this constant can be removed from frontlinesmsconstants class
 			Object column = ui.createColumn(InternationalisationUtils.getI18NString(I18N_FORM_SUBMITTER), null);
 			ui.setWidth(column, 100);
 			ui.setIcon(column, Icon.PHONE_CONNECTED);
 			ui.add(header, column);
 			// For some reason we have a number column
 			int count = 0;
 			for (FormField field : selected.getFields()) {
 				if(field.getType().hasValue()) {
 					column = ui.createColumn(field.getLabel(), new Integer(++count));
 					ui.setInteger(column, "width", 100);
 					ui.setIcon(column, getIcon(field.getType()));
 					ui.add(header, column);
 				}
 			}
 		}
 	}
 
 //> ACCESSORS
 	/**
 	 * Set {@link FormDao}
 	 * @param formsDao new value for {@link #formsDao}
 	 */
 	public void setFormsDao(FormDao formsDao) {
 		this.formsDao = formsDao;
 	}
 	
 	/**
 	 * Set {@link FormResponseDao}
 	 * @param formResponseDao new value for {@link FormResponseDao}
 	 */
 	public void setFormResponseDao(FormResponseDao formResponseDao) {
 		this.formResponseDao = formResponseDao;
 	}
 	
 	/**
 	 * Set {@link #contactDao}
 	 * @param contactDao new value for {@link #contactDao}
 	 */
 	public void setContactDao(ContactDao contactDao) {
 		this.contactDao = contactDao;
 	}
 	
 //> TEMPORARY METHODS THAT NEED SORTING OUT
 	/**
 	 * Gets an icon with the specified name.
 	 * @param iconPath
 	 * @return currently this returns <code>null</code> - needs to be implemented!
 	 */
 	private Image getIcon(String iconPath) {
 		return this.ui.getIcon(iconPath);
 	}
 	
 	/**
 	 * Gets the icon for a particular {@link FComponent}.
 	 * @param fieldType
 	 * @return icon to use for a particular {@link FComponent}.
 	 */
 	public Image getIcon(FormFieldType fieldType) {
 		if(fieldType == FormFieldType.CHECK_BOX)			return getIcon(FormIcon.CHECKBOX);
 		if(fieldType == FormFieldType.CURRENCY_FIELD)		return getIcon(FormIcon.CURRENCY_FIELD);
 		if(fieldType == FormFieldType.DATE_FIELD)			return getIcon(FormIcon.DATE_FIELD);
 		if(fieldType == FormFieldType.EMAIL_FIELD)			return getIcon(FormIcon.EMAIL_FIELD);
 		if(fieldType == FormFieldType.NUMERIC_TEXT_FIELD)	return getIcon(FormIcon.NUMERIC_TEXT_FIELD);
 		if(fieldType == FormFieldType.PASSWORD_FIELD) 		return getIcon(FormIcon.PASSWORD_FIELD);
 		if(fieldType == FormFieldType.PHONE_NUMBER_FIELD) 	return getIcon(FormIcon.PHONE_NUMBER_FIELD);
 		if(fieldType == FormFieldType.TEXT_AREA)			return getIcon(FormIcon.TEXT_AREA);
 		if(fieldType == FormFieldType.TEXT_FIELD) 			return getIcon(FormIcon.TEXT_FIELD);
 		if(fieldType == FormFieldType.TIME_FIELD) 			return getIcon(FormIcon.TIME_FIELD);
 		if(fieldType == FormFieldType.TRUNCATED_TEXT) 		return getIcon(FormIcon.TRUNCATED_TEXT);
 		if(fieldType == FormFieldType.WRAPPED_TEXT) 		return getIcon(FormIcon.WRAPPED_TEXT);
 		throw new IllegalStateException("No icon is mapped for field type: " + fieldType);
 	}
 	
 	/**
 	 * Set the DAO for this class
 	 * @param groupMembershipDao
 	 */
 	public void setGroupMembershipDao(GroupMembershipDao groupMembershipDao) {
 		this.groupMembershipDao = groupMembershipDao;
 	}
 	
 	/**
 	 * Warn the user if he changes to another tab and has unsaved changes 
 	 */
 	public void notify(FrontlineEventNotification notification) {
 		// This object is registered to the UIGeneratorController and get notified when the users changes tab
 		if(notification instanceof TabChangedNotification) {
 			String newTabName = ((TabChangedNotification) notification).getNewTabName();
 			if (newTabName.equals(UI_FORM_TAB_NAME)) {
 				this.refresh();
 			}
 		}
 	}
 }
