 package org.vaadin.demo.crm.ui;
 
 import org.vaadin.demo.crm.Backend;
 import org.vaadin.demo.crm.data.Account;
 
 import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
 import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
 import com.vaadin.addon.touchkit.ui.NavigationView;
 import com.vaadin.addon.touchkit.ui.Popover;
 import com.vaadin.data.Container;
 import com.vaadin.data.Container.Filter;
 import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 
 public class AccountListView extends NavigationView implements
 		ItemClickListener, ClickListener {
 	Table accountList = new Table();
 	Button findButton = new Button("Find");
 
 	public AccountListView() {
 		getNavigationBar().setCaption("Accounts");
 		setContent(accountList);
 
 		accountList.addListener(this);
 		accountList.setSizeFull();
 		accountList.setImmediate(true);
 		accountList.setContainerDataSource(generateDummyAccounts());
 		accountList.setVisibleColumns(new Object[] { "name", "sales" });
 
 		getNavigationBar().setLeftComponent(findButton);
 		findButton.addListener(this);
 	}
 
 	/* Choose an account */
 	public void itemClick(ItemClickEvent event) {
		if (getNavigationManager().getCurrentComponent() != this) return;
		@SuppressWarnings("unchecked")
 		Account account = ((BeanItemContainer<Account>) accountList
 				.getContainerDataSource()).getItem(event.getItemId()).getBean();
 		AccountView accountView = new AccountView(account);
 		getNavigationManager().navigateTo(accountView);
 		((CrmApp) getApplication()).showDetails(account);
 		getNavigationManager().addListener(new NavigationListener() {
 			public void navigate(NavigationEvent event) {
 				((CrmApp) getApplication()).hideDetails();
 			}
 		});
 	}
 
 	/* Find */
 	public void buttonClick(ClickEvent event) {
 		final Popover p = new Popover();
 		((Container.Filterable) accountList.getContainerDataSource())
 				.removeAllContainerFilters();
 		getWindow().addWindow(p);
 		p.showRelativeTo(findButton);
 
 		HorizontalLayout lo = new HorizontalLayout();
 		lo.setMargin(true);
 		lo.setSpacing(true);
 
 		final TextField name = new TextField();
 		name.setInputPrompt("Customer name");
 		Button search = new Button("Search");
 		lo.addComponent(name);
 		lo.addComponent(search);
 		lo.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
 		p.setContent(lo);
 
 		search.addListener(new ClickListener() {
 			public void buttonClick(ClickEvent event) {
 				((Container.Filterable) accountList.getContainerDataSource())
 						.addContainerFilter(new Filter() {
 
 							public boolean passesFilter(Object itemId, Item item) {
 								return ("" + item.getItemProperty("name")
 										.getValue()).contains(("" + name
 										.getValue()));
 							}
 
 							public boolean appliesToProperty(Object propertyId) {
 								return "Name".equals(propertyId);
 							}
 						});
 				getWindow().removeWindow(p);
 			}
 		});
 	}
 
 	private Container generateDummyAccounts() {
 		
 		BeanItemContainer<Account> container = new BeanItemContainer<Account>(
 				Account.class);
 		
 		// for (int i = 0; i < 100; i++) {
 		// Account a = new Account();
 		// a.setName("" + i + " Inc");
 		// a.setSales((int) (Math.random() * 10000000));
 		// container.addBean(a);
 		// }
 
 		for (Account a : Backend.getAllAccounts()) {
 			container.addBean(a);
 		}
 
 		return container;
 	}
 
 }
