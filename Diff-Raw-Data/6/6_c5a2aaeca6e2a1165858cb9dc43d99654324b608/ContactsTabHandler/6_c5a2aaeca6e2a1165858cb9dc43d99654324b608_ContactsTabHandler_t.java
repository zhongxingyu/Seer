 /**
  * 
  */
 package net.frontlinesms.ui.handler.contacts;
 
 // TODO remove static imports
 import static net.frontlinesms.FrontlineSMSConstants.ACTION_ADD_TO_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_CONTACTS_IN_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_CONTACTS_DELETED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUPS_AND_CONTACTS_DELETED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUP_ALREADY_EXISTS;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_REMOVING_CONTACTS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BUTTON_YES;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_FILTER;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_DELETE_NEW_CONTACT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUPS_MENU;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_MSG_HISTORY;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_VIEW_CONTACT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_DELETE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_SEND_SMS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_GROUP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_CONTACTS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_SEND_SMS_BUTTON;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_VIEW_CONTACT_BUTTON;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import net.frontlinesms.Utils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.handler.BaseTabHandler;
 import net.frontlinesms.ui.handler.ComponentPagingHandler;
 import net.frontlinesms.ui.handler.PagedComponentItemProvider;
 import net.frontlinesms.ui.handler.PagedListDetails;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 import org.apache.log4j.Logger;
 
 import thinlet.Thinlet;
 
 /**
  * Event handler for the Contacts tab and associated dialogs.
  * @author Alex alex@frontlinesms.com
  */
 public class ContactsTabHandler extends BaseTabHandler implements PagedComponentItemProvider, SingleGroupSelecterPanelOwner, ContactEditorOwner {
 //> STATIC CONSTANTS
 	/** UI XML File Path: the Home Tab itself */
 	private static final String UI_FILE_CONTACTS_TAB = "/ui/core/contacts/contactsTab.xml";
 	private static final String UI_FILE_DELETE_OPTION_DIALOG_FORM = "/ui/dialog/deleteOptionDialogForm.xml"; // TODO move this to the correct path
 	private static final String UI_FILE_NEW_GROUP_FORM = "/ui/dialog/newGroupForm.xml"; // TODO move this to the correct path
 	
 	private static final String COMPONENT_GROUP_SELECTER_CONTAINER = "pnGroupsContainer";
 	
 //> INSTANCE PROPERTIES
 	/** Logging object */
 	private final Logger LOG = Utils.getLogger(this.getClass()); // FIXME rename to log
 	
 //> DATA ACCESS OBJECTS
 	/** Data access object for {@link Group}s */
 	private final GroupDao groupDao;
 	/** Data access object for {@link Contact}s */
 	private final ContactDao contactDao;
 	private final GroupMembershipDao groupMembershipDao;
 	
 	
 //> CACHED THINLET UI COMPONENTS
 	/** UI Component component: contact list.  This is cached here to save searching for it later. */
 	private Object contactListComponent;
 	
 	/** Handler for paging of {@link #contactListComponent} */
 	private ComponentPagingHandler contactListPager;
 	/** String to filter the contacts by */
 	private String contactNameFilter;
 
 	private final GroupSelecterPanel groupSelecter;
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new instance of this class.
 	 * @param ui value for {@link #ui}
 	 * @param contactDao {@link #contactDao}
 	 * @param groupDao {@link #groupDao}
 	 */
 	public ContactsTabHandler(UiGeneratorController ui) {
 		super(ui);
 		this.contactDao = ui.getFrontlineController().getContactDao();
 		this.groupDao = ui.getFrontlineController().getGroupDao();
 		this.groupMembershipDao = ui.getFrontlineController().getGroupMembershipDao();
 		this.groupSelecter = new GroupSelecterPanel(ui, this);
 	}
 	
 	@Override
 	public void init() {
 		super.init();
 		this.groupSelecter.init(ui.getRootGroup());
 		ui.add(find(COMPONENT_GROUP_SELECTER_CONTAINER), this.groupSelecter.getPanelComponent(), 0);
 	}
 
 //> ACCESSORS
 	/** Refreshes the data displayed in the tab. */
 	public void refresh() {
 		updateGroupList();
 	}
 	
 //> GROUP SELECTION METHODS
 	/**
 	 * Method invoked when the group/contacts tree selection changes. 
 	 * This method updated the contact list according to the new selection.
 	 */
 	public void groupSelectionChanged(Group selectedGroup) {
 		System.out.println("Group selected: " + selectedGroup);
 		LOG.trace("ENTER");
 		this.ui.setText(this.ui.find(COMPONENT_CONTACT_MANAGER_CONTACT_FILTER), "");
 
 		Group g = this.groupSelecter.getSelectedGroup();
 		String toSet = InternationalisationUtils.getI18NString(COMMON_CONTACTS_IN_GROUP, g.getName());
 		this.ui.setText(find("pnContacts"), toSet); // FIXME this should be a constant
 		
 		Object buttonPanelContainer = find(COMPONENT_GROUP_SELECTER_CONTAINER);
 		Object deleteButton = this.ui.find(buttonPanelContainer, "deleteButton"); // FIXME this should be a constant
 		this.ui.setEnabled(deleteButton, !this.ui.isDefaultGroup(g));
 		
 		Object sms = this.ui.find(buttonPanelContainer, "sendSMSButtonGroupSide"); // FIXME this should be a constant
 		this.ui.setEnabled(sms, g != null);
 		
 		updateContactList();
 		LOG.trace("EXIT");
 	}
 	
 //> CONTACT EDITING METHODS
 	/** @see ContactEditorOwner#contactCreationComplete(Contact) */
 	public void contactCreationComplete(Contact contact) {
 		// Refresh the Contacts tab, and make sure that the group and contact who were previously selected are still selected
 		updateContactList();
 	};
 	/** @see ContactEditorOwner#contactEditingComplete(Contact) */
 	public void contactEditingComplete(Contact contact) {
 		contactCreationComplete(contact);
 	}
 	
 //> PAGING METHODS
 	public PagedListDetails getListDetails(Object list, int startIndex, int limit) {
 		if(list == this.contactListComponent) {
 			return getContactListDetails(startIndex, limit);
 		} else throw new IllegalStateException();
 	}
 	
 	private PagedListDetails getContactListDetails(int startIndex, int limit) {
 		Group selectedGroup = this.groupSelecter.getSelectedGroup();
 		
 		if(selectedGroup == null) {
 			return PagedListDetails.EMPTY;
 		} else {
 			int totalItemCount = groupMembershipDao.getMemberCount(selectedGroup);
 			
 			// TODO fix filtering
 			
 	//		int totalItemCount = (this.contactNameFilter == null || this.contactNameFilter.length() == 0) 
 	//				? this.contactDao.getContactCount()
 	//				: this.contactDao.getContactsFilteredByNameCount(this.contactNameFilter);
 	//		
 	//		List<Contact> contacts = (this.contactNameFilter == null || this.contactNameFilter.length() == 0) 
 	//				? this.contactDao.getAllContacts(startIndex, limit)
 	//				: this.contactDao.getContactsFilteredByName(this.contactNameFilter, startIndex, limit);
 			List<Contact> contacts = groupMembershipDao.getMembers(selectedGroup, startIndex, limit);
 			Object[] listItems = toThinletComponents(contacts);
 			
 			return new PagedListDetails(totalItemCount, listItems);
 		}
 		
 	}
 	
 	private Object[] toThinletComponents(List<Contact> contacts) {
 		Object[] components = new Object[contacts.size()];
 		for (int i = 0; i < components.length; i++) {
 			Contact c = contacts.get(i);
 			components[i] = ui.getRow(c);
 		}
 		return components;
 	}
 
 //> UI METHODS
 	/** Show editor for new contact. */
 	public void showNewContactDialog() {
 		ContactEditor editor = new ContactEditor(ui, this);
 		Group selectedGroup = this.groupSelecter.getSelectedGroup();
 		editor.show(selectedGroup);
 	}
 	
 	/**
 	 * Shows the delete option dialog, which asks the user if he/she wants to remove
 	 * the selected contacts from database.
 	 * @param list
 	 */
 	public void showDeleteOptionDialog() {
 		Group g = this.groupSelecter.getSelectedGroup();
 		if (!this.ui.isDefaultGroup(g)) {
 			Object deleteDialog = ui.loadComponentFromFile(UI_FILE_DELETE_OPTION_DIALOG_FORM, this);
 			ui.add(deleteDialog);
 		}
 	}
 
 	/**
 	 * Shows contact dialog to allow edition of the selected contact.
 	 * <br>This method affects the advanced mode.
 	 * @param list
 	 */
 	public void showContactDetails(Object list) {
 		Object selected = this.ui.getSelectedItem(list);
 		if(selected != null) {
 			ContactEditor editor = new ContactEditor(ui, this);
 			editor.show(this.ui.getAttachedObject(selected, Contact.class));
 		}
 	}
 	
 	/**
 	 * Populates the pop up menu with all groups create by users.
 	 * 
 	 * @param popUp
 	 * @param list
 	 */
 	public void populateGroups(Object popUp, Object list) {
 		Object[] selectedItems = this.ui.getSelectedItems(list);
 		this.ui.setVisible(popUp, this.ui.getSelectedItems(list).length > 0);
 		if (selectedItems.length == 0) {
 			//Nothing selected
 			boolean none = true;
 			for (Object o : this.ui.getItems(popUp)) {
 				if (this.ui.getName(o).equals(COMPONENT_NEW_GROUP)
 						|| this.ui.getName(o).equals("miNewContact")) {
 					this.ui.setVisible(o, true);
 					none = false;
 				} else {
 					this.ui.setVisible(o, false);
 				}
 			}
 			this.ui.setVisible(popUp, !none);
 		} else if (this.ui.getAttachedObject(selectedItems[0]) instanceof Contact) {
 			for (Object o : this.ui.getItems(popUp)) {
 				String name = this.ui.getName(o);
 				if (name.equals(COMPONENT_MENU_ITEM_MSG_HISTORY) 
 						|| name.equals(COMPONENT_MENU_ITEM_VIEW_CONTACT)) {
 					this.ui.setVisible(o, this.ui.getSelectedItems(list).length == 1);
 				} else if (!name.equals(COMPONENT_GROUPS_MENU)) {
 					this.ui.setVisible(o, true);
 				}
 			}
 			Object menu = this.ui.find(popUp, COMPONENT_GROUPS_MENU);
 			this.ui.removeAll(menu);
 			List<Group> allGroups = this.groupDao.getAllGroups();
 			for (Group g : allGroups) {
 				Object menuItem = Thinlet.create(Thinlet.MENUITEM);
 				this.ui.setText(menuItem, InternationalisationUtils.getI18NString(COMMON_GROUP) + "'" + g.getName() + "'");
 				this.ui.setIcon(menuItem, Icon.GROUP);
 				this.ui.setAttachedObject(menuItem, g);
 				this.ui.setAction(menuItem, "addToGroup(this)", menu, this);
 				this.ui.add(menu, menuItem);
 			}
 			this.ui.setVisible(menu, allGroups.size() != 0);
 			String menuName = InternationalisationUtils.getI18NString(ACTION_ADD_TO_GROUP);
 			this.ui.setText(menu, menuName);
 			
 			Object menuRemove = this.ui.find(popUp, "groupsMenuRemove");
 			if (menuRemove != null) {
 				Contact c = this.ui.getContact(this.ui.getSelectedItem(list));
 				this.ui.removeAll(menuRemove);
 				List<Group> groups = this.groupMembershipDao.getGroups(c);
 				for (Group g : groups) {
 					Object menuItem = Thinlet.create(Thinlet.MENUITEM);
 					this.ui.setText(menuItem, g.getName());
 					this.ui.setIcon(menuItem, Icon.GROUP);
 					this.ui.setAttachedObject(menuItem, g);
 					this.ui.setAction(menuItem, "removeFromGroup(this)", menuRemove, this);
 					this.ui.add(menuRemove, menuItem);
 				}
 				this.ui.setEnabled(menuRemove, groups.size() != 0);
 			}
 		} else {
 			Group g = this.ui.getGroup(this.ui.getSelectedItem(list));
 			//GROUPS OR BOTH
 			for (Object o : this.ui.getItems(popUp)) {
 				String name = this.ui.getName(o);
 				if (COMPONENT_NEW_GROUP.equals(name) 
 						|| COMPONENT_MI_SEND_SMS.equals(name)
 						|| COMPONENT_MI_DELETE.equals(name)
 						|| COMPONENT_MENU_ITEM_MSG_HISTORY.equals(name)
 						|| "miNewContact".equals(name)) {
 					this.ui.setVisible(o, true);
 				} else {
 					this.ui.setVisible(o, false);
 				}
 				if (COMPONENT_MI_DELETE.equals(name)) {
 					this.ui.setVisible(o, !this.ui.isDefaultGroup(g));
 				}
 				
 				// FIXME this is superfluous - always sets vis to true
 				if (COMPONENT_NEW_GROUP.equals(name)) {
 					this.ui.setVisible(o, true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Shows the new group dialog.
 	 * @param groupList
 	 */
 	public void showNewGroupDialog() {
 		Object newGroupForm = this.ui.loadComponentFromFile(UI_FILE_NEW_GROUP_FORM, this);
 		ui.setAttachedObject(newGroupForm, this.groupSelecter.getSelectedGroup());
 		this.ui.add(newGroupForm);
 	}
 
 	/** Updates the list of contacts with the new filter. */
 	public void filterContacts() {
 		updateContactList();
 	}
 
 	/**
 	 * Applies a text filter to the contact list.  The list is not updated until {@link #filterContacts()}
 	 * is called.
 	 * @param contactFilter The new filter.
 	 */	
 	public void setContactFilter(String contactFilter) {
 		this.contactNameFilter = contactFilter;
 	}
 
 	/**
 	 * Enables or disables the buttons on the Contacts tab (advanced mode).
 	 * @param contactList
 	 */
 	public void enabledButtonsAfterSelection(Object contactList) {
 		boolean enabled = this.ui.getSelectedItems(contactList).length > 0;
 		this.ui.setEnabled(find(COMPONENT_DELETE_NEW_CONTACT), enabled);
 		this.ui.setEnabled(find(COMPONENT_VIEW_CONTACT_BUTTON), enabled);
 		this.ui.setEnabled(find(COMPONENT_SEND_SMS_BUTTON), enabled);
 	}
 
 	/**
 	 * Adds selected contacts to group.
 	 * 
 	 * @param item The item holding the destination group.
 	 */
 	public void addToGroup(Object item) {
 		LOG.trace("ENTER");
 		Object[] selected = null;
 		selected = this.ui.getSelectedItems(contactListComponent);
 		// Add to the selected groups...
 		Group destination = this.ui.getGroup(item);
 		// Let's check all the selected items.  Any that are groups should be added to!
 		for (Object component : selected) {
 			if (this.ui.isAttachment(component, Contact.class)) {
 				Contact contact = this.ui.getContact(component);
 				LOG.debug("Adding Contact [" + contact.getName() + "] to [" + destination + "]");
 				if(this.groupMembershipDao.addMember(destination, contact)) {
 					groupDao.updateGroup(destination);
 				}
 			}
 		}
 		updateGroupList();
 		LOG.trace("EXIT");
 	}
 	
 	/**
 	 * Remove selected groups and contacts.
 	 * 
 	 * @param button
 	 * @param dialog
 	 */
 	public void removeSelectedFromGroupList(final Object button, Object dialog) {
 		LOG.trace("ENTER");
 		if (dialog != null) {
 			this.ui.removeDialog(dialog);
 		}
 
 		Group selectedGroup = this.groupSelecter.getSelectedGroup();
 		if(!ui.isDefaultGroup(selectedGroup)) {
 			boolean removeContactsAlso = false;
 			if (button != null) {
 				removeContactsAlso = ui.getName(button).equals(COMPONENT_BUTTON_YES);
 			}
 			LOG.debug("Selected Group [" + selectedGroup.getName() + "]");
 			LOG.debug("Remove Contacts from database [" + removeContactsAlso + "]");
 			if (!ui.isDefaultGroup(selectedGroup)) {
 				//Inside a default group
 				LOG.debug("Removing group [" + selectedGroup.getName() + "] from database");
 				groupDao.deleteGroup(selectedGroup, removeContactsAlso);
 			} else {
 				throw new IllegalStateException();
 //				if (removeContactsAlso) {
 //					LOG.debug("Group not destroyable, removing contacts...");
 //					for (Contact c : selectedGroup.getDirectMembers()) {
 //						LOG.debug("Removing contact [" + c.getName() + "] from database");
 //						contactDao.deleteContact(c);
 //					}
 //				}
 			}
 		}
 		
 		Object sms = ui.find(find(COMPONENT_GROUP_SELECTER_CONTAINER), "sendSMSButtonGroupSide");
 		ui.setEnabled(sms, selectedGroup != null);
 		ui.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUPS_AND_CONTACTS_DELETED));
 		refresh();
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Removes the contacts selected in the contacts list from the group which is selected in the groups tree.
 	 * @param selectedGroup A set of thinlet components with group members attached to them.
 	 */
 	public void removeFromGroup(Object selectedGroup) {
 		Group g = this.ui.getGroup(selectedGroup);
 		Contact c = this.ui.getContact(this.ui.getSelectedItem(contactListComponent));
 		if(this.groupMembershipDao.removeMember(g, c)) {
 			this.refresh();
 		}
 	}
 
 	/** Removes the selected contacts of the supplied contact list component. */
 	public void deleteSelectedContacts() {
 		LOG.trace("ENTER");
 		this.ui.removeConfirmationDialog();
 		this.ui.setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_CONTACTS));
 		final Object[] selected = this.ui.getSelectedItems(contactListComponent);
 		for (Object o : selected) {
 			Contact contact = ui.getContact(o);
 			LOG.debug("Deleting contact [" + contact.getName() + "]");
 			contactDao.deleteContact(contact);
 		}
 		ui.alert(InternationalisationUtils.getI18NString(MESSAGE_CONTACTS_DELETED));
 		refresh();
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Creates a new group with the supplied name.
 	 * 
 	 * @param newGroupName The desired group name.
 	 * @param dialog the dialog holding the information to where we should create this new group.
 	 */
 	public void createNewGroup(String newGroupName, Object dialog) {
 		// The selected parent group should be attached to this dialog.  Get it,
 		// create the new group, update the group list and then remove the dialog.
 		Group selectedParentGroup = this.ui.getGroup(dialog);
 		doGroupCreation(newGroupName, dialog, selectedParentGroup);		
 	}
 	
 //> PRIVATE UI HELPER METHODS
 	/**
 	 * Creates a group with the supplied name and inside the supplied parent .
 	 * @param newGroupName The desired group name.
 	 * @param dialog The dialog to be removed after the operation.
 	 * @param selectedParentGroup
 	 */
 	private void doGroupCreation(String newGroupName, Object dialog, Group selectedParentGroup) {
 		LOG.trace("ENTER");
 		if(LOG.isDebugEnabled()) {
 			String parentGroupName = selectedParentGroup == null ? "null" : selectedParentGroup.getName();
 			LOG.debug("Parent group [" + parentGroupName + "]");
 		}
		if(selectedParentGroup == null) {
			selectedParentGroup = ui.getRootGroup();
		}
 
 		LOG.debug("Group Name [" + newGroupName + "]");
 		try {
 			if(LOG.isDebugEnabled()) LOG.debug("Creating group with name: " + newGroupName + " and parent: " + selectedParentGroup);
 			
 			Group g = new Group(selectedParentGroup, newGroupName);
 			this.groupDao.saveGroup(g);
 			
 			this.groupSelecter.addGroup(g);
 			
 			if (dialog != null) this.ui.remove(dialog);
 			LOG.debug("Group created successfully!");
 		} catch (DuplicateKeyException e) {
 			LOG.debug("A group with this name already exists.", e);
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUP_ALREADY_EXISTS));
 		}
 		LOG.trace("EXIT");
 	}
 	
 	/** Repopulates the contact list according to the current filter. */
 	public void updateContactList() {
 		this.contactListPager.setCurrentPage(0);
 		this.contactListPager.refresh();
 		enabledButtonsAfterSelection(contactListComponent);
 	}
 
 	/** Updates the group tree. */
 	private void updateGroupList() {
 		this.groupSelecter.refresh();
 		updateContactList();
 	}
 	
 //> EVENT HANDLER METHODS
 	public void addToContactList(Contact contact, Group group) {
 		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
 		
 		Group g = this.groupSelecter.getSelectedGroup();
 		selectedGroupsFromTree.add(g);
 		
 		if (selectedGroupsFromTree.contains(group)) {
 			int limit = this.contactListPager.getMaxItemsPerPage();
 			//Adding
 			if (this.ui.getItems(contactListComponent).length < limit) {
 				this.ui.add(contactListComponent, this.ui.getRow(contact));
 			}
 		}
 		
 		this.groupSelecter.refresh();
 //		updateTree(group);
 	}
 	
 //> UI PASS-THROUGH METHODS TO UiGC
 	/** @see UiGeneratorController#groupList_expansionChanged(Object) */
 	public void groupList_expansionChanged(Object groupList) {
 		this.ui.groupList_expansionChanged(groupList);
 	}
 	/** Shows the compose message dialog for all members of the selected group. */
 	public void sendSmsToGroup() {
 		this.ui.show_composeMessageForm(this.groupSelecter.getSelectedGroup());
 	}
 	/** Shows the compose message dialog for all selected contacts. */
 	public void sendSmsToContacts() {
 		Object[] selectedItems = ui.getSelectedItems(contactListComponent);
 		if(selectedItems.length > 0) {
 			HashSet<Object> contacts = new HashSet<Object>(); // Must be Objects because of stupid method sig of show_comp...
 			for(Object selectedItem : selectedItems) {
 				contacts.add(ui.getAttachedObject(selectedItem, Contact.class));
 			}
 
 			this.ui.show_composeMessageForm(contacts);	
 		}
 		
 	}
 	/**
 	 * Shows the export wizard dialog for exporting contacts.
 	 * @param list The list to get selected items from.
 	 * @param type the name of the type to export
 	 */
 	public void showExportWizard(Object list) {
 		this.ui.showExportWizard(list, "contacts");
 	}
 
 //> INSTANCE HELPER METHODS
 	/** Initialise dynamic contents of the tab component. */
 	protected Object initialiseTab() {
 		Object tabComponent = ui.loadComponentFromFile(UI_FILE_CONTACTS_TAB, this);
 		
 		
 		// Cache Thinlet UI components
 		contactListComponent = this.ui.find(tabComponent, COMPONENT_CONTACT_MANAGER_CONTACT_LIST);
 		
 		this.contactListPager = new ComponentPagingHandler(this.ui, this, contactListComponent);
 		Object pnContacts = this.ui.find(tabComponent, COMPONENT_PN_CONTACTS);
 		this.ui.add(pnContacts, this.contactListPager.getPanel());
 		
 		return tabComponent;
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 }
