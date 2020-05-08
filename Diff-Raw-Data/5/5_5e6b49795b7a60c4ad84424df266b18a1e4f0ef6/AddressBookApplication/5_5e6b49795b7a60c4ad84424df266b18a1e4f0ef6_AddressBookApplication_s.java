 package com.vaadin.addon.sqlcontainer.demo.addressbook;
 
 import com.vaadin.Application;
 import com.vaadin.addon.sqlcontainer.SQLContainer;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.data.DatabaseHelper;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.data.SearchFilter;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.HelpWindow;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.ListView;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.NavigationTree;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.PersonForm;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.PersonList;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.SearchView;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.ui.SharingOptions;
 import com.vaadin.addon.sqlcontainer.query.Filter;
 import com.vaadin.addon.sqlcontainer.query.FilteringMode;
 import com.vaadin.addon.sqlcontainer.query.QueryDelegate;
 import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
 import com.vaadin.addon.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.SplitPanel;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Window.Notification;
 
 @SuppressWarnings("serial")
 public class AddressBookApplication extends Application implements
         ClickListener, ValueChangeListener, ItemClickListener,
         QueryDelegate.RowIdChangeListener {
 
     private NavigationTree tree = new NavigationTree(this);
 
     private Button newContact = new Button("Add contact");
     private Button search = new Button("Search");
     private Button share = new Button("Share");
     private Button help = new Button("Help");
     private SplitPanel horizontalSplit = new SplitPanel(
             SplitPanel.ORIENTATION_HORIZONTAL);
 
     // Lazily created UI references
     private ListView listView = null;
     private SearchView searchView = null;
     private PersonList personList = null;
     private PersonForm personForm = null;
     private HelpWindow helpWindow = null;
     private SharingOptions sharingOptions = null;
 
     /* Helper class that creates the tables and SQLContainers. */
     private DatabaseHelper dbHelp = new DatabaseHelper();
 
     @Override
     public void init() {
         buildMainLayout();
         setMainComponent(getListView());
         dbHelp.getPersonContainer().addListener(this);
     }
 
     private void buildMainLayout() {
         setMainWindow(new Window("Address Book + SQLContainer Demo application"));
 
         setTheme("contacts");
 
         VerticalLayout layout = new VerticalLayout();
         layout.setSizeFull();
 
         layout.addComponent(createToolbar());
         layout.addComponent(horizontalSplit);
         layout.setExpandRatio(horizontalSplit, 1);
 
         horizontalSplit.setSplitPosition(200, SplitPanel.UNITS_PIXELS);
         horizontalSplit.setFirstComponent(tree);
 
         getMainWindow().setContent(layout);
     }
 
     private HorizontalLayout createToolbar() {
         HorizontalLayout lo = new HorizontalLayout();
         lo.addComponent(newContact);
         lo.addComponent(search);
         lo.addComponent(share);
         lo.addComponent(help);
 
         search.addListener((ClickListener) this);
         share.addListener((ClickListener) this);
         help.addListener((ClickListener) this);
         newContact.addListener((ClickListener) this);
 
         search.setIcon(new ThemeResource("icons/32/folder-add.png"));
         share.setIcon(new ThemeResource("icons/32/users.png"));
         help.setIcon(new ThemeResource("icons/32/help.png"));
         newContact.setIcon(new ThemeResource("icons/32/document-add.png"));
 
         lo.setMargin(true);
         lo.setSpacing(true);
 
         lo.setStyleName("toolbar");
 
         lo.setWidth("100%");
 
         Embedded em = new Embedded("", new ThemeResource("images/logo.png"));
         lo.addComponent(em);
         lo.setComponentAlignment(em, Alignment.MIDDLE_RIGHT);
         lo.setExpandRatio(em, 1);
 
         return lo;
     }
 
     private void setMainComponent(Component c) {
         horizontalSplit.setSecondComponent(c);
     }
 
     /*
      * View getters exist so we can lazily generate the views, resulting in
      * faster application startup time.
      */
     private ListView getListView() {
         if (listView == null) {
             personList = new PersonList(this);
             personForm = new PersonForm(this);
             listView = new ListView(personList, personForm);
         }
         return listView;
     }
 
     private SearchView getSearchView() {
         if (searchView == null) {
             searchView = new SearchView(this);
         }
         return searchView;
     }
 
     private HelpWindow getHelpWindow() {
         if (helpWindow == null) {
             helpWindow = new HelpWindow();
         }
         return helpWindow;
     }
 
     private SharingOptions getSharingOptions() {
         if (sharingOptions == null) {
             sharingOptions = new SharingOptions();
         }
         return sharingOptions;
     }
 
     public void buttonClick(ClickEvent event) {
         final Button source = event.getButton();
 
         if (source == search) {
             showSearchView();
         } else if (source == help) {
             showHelpWindow();
         } else if (source == share) {
             showShareWindow();
         } else if (source == newContact) {
             addNewContact();
         }
     }
 
     private void showHelpWindow() {
         getMainWindow().addWindow(getHelpWindow());
     }
 
     private void showShareWindow() {
         getMainWindow().addWindow(getSharingOptions());
     }
 
     private void showListView() {
         setMainComponent(getListView());
         personList.fixVisibleAndSelectedItem();
     }
 
     private void showSearchView() {
         setMainComponent(getSearchView());
         personList.fixVisibleAndSelectedItem();
     }
 
     public void valueChange(ValueChangeEvent event) {
         Property property = event.getProperty();
         if (property == personList) {
             Item item = personList.getItem(personList.getValue());
             if (item != personForm.getItemDataSource()) {
                 personForm.setItemDataSource(item);
             }
         }
     }
 
     public void itemClick(ItemClickEvent event) {
         if (event.getSource() == tree) {
             Object itemId = event.getItemId();
             if (itemId != null) {
                 if (NavigationTree.SHOW_ALL.equals(itemId)) {
                     /* Clear all filters from person container */
                     getDbHelp().getPersonContainer()
                             .removeAllContainerFilters();
                     showListView();
                 } else if (NavigationTree.SEARCH.equals(itemId)) {
                     showSearchView();
                 } else if (itemId instanceof SearchFilter[]) {
                     search((SearchFilter[]) itemId);
                 }
             }
         }
     }
 
     private void addNewContact() {
         showListView();
         personForm.addContact();
     }
 
     public void search(SearchFilter... searchFilters) {
         if (searchFilters.length == 0) {
             return;
         }
         SQLContainer c = getDbHelp().getPersonContainer();
 
         /* Clear all filters from person container. */
         getDbHelp().getPersonContainer().removeAllContainerFilters();
         /*
          * Set the appropriate filtering mode. In this application, multiple
          * filters are only used to filter for more than one city since they are
          * actually filtered by their keys. This filter has to be of the
          * exclusive type.
          */
         if (searchFilters.length > 1) {
             c.setFilteringMode(FilteringMode.FILTERING_MODE_EXCLUSIVE);
         } else {
             c.setFilteringMode(FilteringMode.FILTERING_MODE_INCLUSIVE);
         }
         /* Add the filter(s) to the person container. */
         for (SearchFilter searchFilter : searchFilters) {
             Filter f = new Filter((String) searchFilter.getPropertyId(),
                     ComparisonType.CONTAINS, searchFilter.getTerm());
             if (Integer.class.equals(c.getType(searchFilter.getPropertyId()))) {
                 try {
                     f = new Filter((String) searchFilter.getPropertyId(),
                             ComparisonType.EQUALS, Integer
                                     .parseInt(searchFilter.getTerm()));
                 } catch (NumberFormatException nfe) {
                     getMainWindow().showNotification("Invalid search term!");
                     return;
                 }
             }
             c.addFilter(f);
         }
         showListView();
 
         getMainWindow().showNotification(
                 "Searched for:<br/> "
                         + searchFilters[0].getPropertyIdDisplayName() + " = *"
                         + searchFilters[0].getTermDisplayName()
                         + "*<br/>Found " + c.size() + " item(s).",
                 Notification.TYPE_TRAY_NOTIFICATION);
     }
 
     public void saveSearch(SearchFilter... searchFilter) {
         tree.addItem(searchFilter);
         tree.setItemCaption(searchFilter, searchFilter[0].getSearchName());
         tree.setParent(searchFilter, NavigationTree.SEARCH);
         // mark the saved search as a leaf (cannot have children)
         tree.setChildrenAllowed(searchFilter, false);
         // make sure "Search" is expanded
         tree.expandItem(NavigationTree.SEARCH);
         // select the saved search
         tree.setValue(searchFilter);
     }
 
     public DatabaseHelper getDbHelp() {
         return dbHelp;
     }
 
     public void rowIdChange(RowIdChangeEvent event) {
         /* Select the added item and fix the table scroll position */
         personList.select(event.getNewRowId());
         personList.fixVisibleAndSelectedItem();
     }
 }
