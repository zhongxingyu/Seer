 /*
  * $Id$
  * $Revision$
  * $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.phonebook.web.page;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 
 import wicket.Component;
 import wicket.MarkupContainer;
 import wicket.contrib.phonebook.Contact;
 import wicket.contrib.phonebook.web.ContactsDataProvider;
import wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
 import wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import wicket.extensions.markup.html.repeater.data.table.IColumn;
 import wicket.extensions.markup.html.repeater.data.table.filter.ChoiceFilteredPropertyColumn;
 import wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
 import wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
 import wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
 import wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
 import wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
 import wicket.extensions.markup.html.repeater.refreshing.Item;
 import wicket.markup.html.link.Link;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * Display a Pageable List of Contacts.
  * 
  * @author igor
  */
 public class ListContactsPage extends BasePage {
 
 	/**
 	 * Constructor. Having this constructor public means that the page is
 	 * 'bookmarkable' and hence can be called/ created from anywhere.
 	 */
 	public ListContactsPage() {
 
 		add(new Link(this,"createLink") {
 			/**
 			 * Go to the Edit page when the link is clicked, passing an empty
 			 * Contact details
 			 */
 			public void onClick() {
 				setResponsePage(new EditContactPage(getPage(), 0));
 			}
 
 		});
 
 		IColumn[] columns = new IColumn[5];
 
 		/*
 		 * This is a composite column, created by extending
 		 * FilteredAbstractColumn. This column adds a UserActionsPanel as its
 		 * cell contents. It also provides the go-and-clear filter control
 		 * panel.
 		 */
 		columns[0] = new FilteredAbstractColumn(new Model("Actions")) {
 
 			// addd the UserActionsPanel to the cell item
 			public void populateItem(Item cellItem, String componentId,
 					IModel model) {
 				final Contact contact = (Contact) model.getObject(cellItem);
 				cellItem.add(new UserActionsPanel(cellItem,componentId, contact));
 			}
 
 			// return the go-and-clear filter for the filter toolbar
			public Component getFilter(MarkupContainer parent, String componentId, FilterForm form) {
 				return new GoAndClearFilter(parent,componentId, form);
 			}
 
 		};
 
 		// creates a column with a text filter
		columns[1] = new TextFilteredPropertyColumn(new Model<String>("First Name"),
 				"firstname", "firstname");
 
 		List names = getDao().getUniqueLastNames();
 		
		columns[2] = new ChoiceFilteredPropertyColumn(new Model<String>("Last Name"),
				"lastname", "lastname", new Model<List>(names)) ;
 		
		columns[3] = new TextFilteredPropertyColumn(new Model<String>("Phone Number"), "phone",
 				"phone");
 		
		columns[4] = new TextFilteredPropertyColumn(new Model<String>("Email"), "email", "email");
 
 		// set up data provider
		final ContactsDataProvider dataProvider = new ContactsDataProvider(getDao());
 
 		// create the data table
		final DefaultDataTable users = new DefaultDataTable(this,"users", Arrays
 				.asList(columns), dataProvider, 10);
 
		users.addTopToolbar(new DataTable.IToolbarFactory() {
			public AbstractToolbar newToolbar(MarkupContainer parent, String id, DataTable dataTable) 
			{
				return new FilterToolbar(parent,id,users, dataProvider);
			};
				
		});
 		add(users);
 
 	}
 
 	/**
 	 * Provides a composite User Actions panel for the Actions column.
 	 * 
 	 * @author igor
 	 */
 	private static class UserActionsPanel extends Panel {
 
 		public UserActionsPanel(MarkupContainer<?> panel,String id, Contact contact) {
 			super(panel,id);
 			final long contactId = contact.getId();
 
 			add(new Link(this,"editLink") {
 				/**
 				 * Go to the Edit page, passing this page and the id of the
 				 * Contact involved.
 				 */
 				public void onClick() {
 					setResponsePage(new EditContactPage(getPage(), contactId));
 				}
 
 			});
 
 			add(new Link(this,"deleteLink") {
 				/**
 				 * Go to the Delete page, passing this page and the id of the
 				 * Contact involved.
 				 */
 				public void onClick() {
 					setResponsePage(new DeleteContactPage(getPage(), contactId));
 				}
 
 			});
 
 		}
 
 	}
 }
