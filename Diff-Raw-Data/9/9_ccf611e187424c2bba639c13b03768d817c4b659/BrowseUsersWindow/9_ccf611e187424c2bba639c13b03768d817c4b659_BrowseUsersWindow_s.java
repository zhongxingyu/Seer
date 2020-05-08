 package org.iucn.sis.client.api.ui.users.panels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
 import org.iucn.sis.client.api.caches.UserCache;
 import org.iucn.sis.client.api.caches.RecentlyAccessedCache.RecentUser;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.users.panels.UserSearchController.SearchResults;
 import org.iucn.sis.client.api.utils.BasicWindow;
 import org.iucn.sis.shared.api.models.RecentlyAccessed;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
 import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.DNDListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TextBox;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 /**
  * BrowseUsersWindow.java
  * 
  * Allows browsing of the database and find a list of users, and has an abstract
  * callback method when a selection has been made.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  * 
  */
 public class BrowseUsersWindow extends BasicWindow {
 
 	/**
 	 * SearchResultsComparator
 	 * 
 	 * Wrapper for the PortableAlphanumericComparators that integrates with the
 	 * StoreSorter.
 	 * 
 	 * @author carl.scott <carl.scott@solertium.com>
 	 * 
 	 */
 	private static class SearchResultsComparator extends
 			StoreSorter<SearchResults> {
 		private static final long serialVersionUID = 1L;
 		private final PortableAlphanumericComparator c = new PortableAlphanumericComparator();
 
 		@Override
 		public int compare(Store<SearchResults> store, SearchResults m1,
 				SearchResults m2, String property) {
 			return c.compare(m1.get("name"), m2.get("name"));
 		}
 	}
 
 	private final ListView<SearchResults> results, selected, recent;
 	private final String basicInstructions;
 	private final HTML status;
 
 	public final static int windowWidth = 625;
 	public final static int windowHeight = 515;
 	public final static int listHeight = 400;
 	public final static int listWidth = 195;
 	private Html instructions;
 	private Html recentUsersHeading = new Html("<b>Recent Users</b>");
 	private Html selectedUsersHeading = new Html("<b>Selected Users</b>");
 	private Html possibleUsersHeading = new Html("<b>Possible Users</b>");
 	private boolean drawn = false;
 	
 	private ComplexListener<List<ClientUser>> listener;
 
 	public BrowseUsersWindow() {
 		super("Search for Users");
 
 		basicInstructions = "<b>Add User:</b> Choose a recent user or Search for a user and then drag and drop the user to the selected list.  </br></br>"
 			+ "<b>Remove User:</b> Drag name out of the selected users list into the possible users list. </br></br>";
 		
 		instructions = new Html(basicInstructions);
 
 		selected = new ListView<SearchResults>();
 		selected.setHeight(listHeight);
 		selected.setWidth(listWidth);
 		selected.setDisplayProperty("name");
 		selected.setStore(new ListStore<SearchResults>());
 		selected.getStore().setStoreSorter(new SearchResultsComparator());
 
 		ListViewSelectionModel<SearchResults> sm = new ListViewSelectionModel<SearchResults>();
 		sm.setSelectionMode(SelectionMode.MULTI);
 
 		results = new ListView<SearchResults>();
 		results.setHeight(listHeight);
 		results.setWidth(listWidth);
 		results.setSelectionModel(sm);
 		results.setDisplayProperty("name");
 		results.setStore(new ListStore<SearchResults>());
 
 		sm = new ListViewSelectionModel<SearchResults>();
 		sm.setSelectionMode(SelectionMode.MULTI);
 		recent = new ListView<SearchResults>();
 		recent.setHeight(listHeight);
 		recent.setWidth(listWidth);
 		recent.setSelectionModel(sm);
 		recent.setDisplayProperty("name");
 		recent.setStore(new ListStore<SearchResults>());
 
 		status = new HTML();
 		draw();
 		setSize(windowWidth, windowHeight);

 	}
 
 	public boolean containsSearchResult(ListStore<SearchResults> store,
 			SearchResults result) {
 		for (int i = 0; i < store.getCount(); i++) {
 			if (store.getAt(i).getUser().getId() == result.getUser().getId())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Draws the UI, only need be called once.
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	protected void draw() {
 		if (!drawn) {
 			drawn = true;
 			final TextBox first = new TextBox();
 			first.setName(UserSearchController.SEARCH_KEY_FIRST_NAME);
 			final TextBox last = new TextBox();
 			last.setName(UserSearchController.SEARCH_KEY_LAST_NAME);
 			final TextBox nickname = new TextBox();
 			nickname.setName(UserSearchController.SEARCH_KEY_NICKNAME);
 			final TextBox affiliation = new TextBox();
 			affiliation.setName(UserSearchController.SEARCH_KEY_AFFILIATION);
 
 			final FlexTable form = new FlexTable();
 			final ButtonBar bar = new ButtonBar();
 			bar.setAlignment(HorizontalAlignment.RIGHT);
 			bar.add(new Button("Search", new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					final Map<String, String> params = new HashMap<String, String>();
 					for (int i = 0; i < form.getRowCount(); i++) {
 						if (form.getCellCount(i) > 1) {
 							TextBox box = (TextBox) form.getWidget(i, 1);
 							if (!box.getText().equals(""))
 								params.put(box.getName(), box.getText());
 						}
 					}
 					if (params.isEmpty())
 						WindowUtils.errorAlert("You must specify at least one criteria");
 					else
 						search(params);
 				}
 			}));
 			form.setHTML(0, 0, "First Name: ");
 			form.setWidget(0, 1, first);
 			form.setHTML(1, 0, "Last Name: ");
 			form.setWidget(1, 1, last);
 			form.setHTML(2, 0, "Nickname: ");
 			form.setWidget(2, 1, nickname);
 			form.setHTML(3, 0, "Affiliation: ");
 			form.setWidget(3, 1, affiliation);
 			form.setWidget(4, 0, bar);
 			form.getFlexCellFormatter().setColSpan(4, 0, 2);
 			
 			final TableData headers = new TableData();
 			headers.setHorizontalAlign(HorizontalAlignment.CENTER);
 			headers.setHeight("25px");
 			
 			final TableData panels = new TableData();
 			panels.setMargin(5);
 			panels.setHeight(listHeight + "px");
 			
 			final TableData statusData = new TableData();
 			statusData.setMargin(5);
 			statusData.setColspan(1);
 			
 			final TableData filler = new TableData();
 			filler.setColspan(2);
 
 			final LayoutContainer container = new LayoutContainer(); {
 				final TableLayout clayout = new TableLayout(3);
 				clayout.setWidth("100%");
 				clayout.setHeight("100%");
 				container.setLayout(clayout);
 			}
 			
 			container.add(recentUsersHeading, headers);
 			container.add(selectedUsersHeading, headers);
 			container.add(possibleUsersHeading, headers);
 			
 			container.add(recent, panels);
 			container.add(selected, panels);
 			container.add(results, panels);
 			
 			container.add(new HTML(""), filler);
 			
 			container.add(status, statusData);
 
 			new ListViewDragSource(results);
 			new ListViewDragSource(selected);
 			new ListViewDragSource(recent);
 
 			new ListViewDropTarget(results);
 			ListViewDropTarget selectedTarget = new ListViewDropTarget(selected); 
 			selectedTarget.addDNDListener(new DNDListener() {
 				@Override
 				public void dragEnter(DNDEvent e) {
 					try {
 						List<SearchResults> data = (List<SearchResults>)(e.getData());
 						if( data.size() == 1 ) {
 							if( detectCollision(data.get(0))) {
 								e.setCancelled(true);
 								e.getStatus().update("Duplicate User Detected");
 							}
 						} else {
 							List<SearchResults> toRemove = new ArrayList<SearchResults>();
 							for( SearchResults datum : data ) {
 								if( detectCollision(datum)) {
 									e.setCancelled(true);
 									e.getStatus().update("Duplicate User Detected");
 									break;
 								}
 							}
 							
 							for( SearchResults cur : toRemove )
 								selected.getStore().remove(cur);
 						}
 					} catch (Throwable e1) {
 						e1.printStackTrace();
 					}
 				}
 
 				private boolean detectCollision(SearchResults datum) {
 					for( SearchResults cur : selected.getStore().getModels() ) {
 						if( datum.get("userid").equals(cur.get("userid")) )
 							return true;
 					}
 					return false;
 				}
 			});
 			
 			final LayoutContainer searchWrapper = new LayoutContainer();
 			searchWrapper.setLayout(new RowLayout(Orientation.HORIZONTAL));
 			searchWrapper.add(instructions,
 					new com.extjs.gxt.ui.client.widget.layout.RowData(1, 50));
 			searchWrapper.add(form,
 					new com.extjs.gxt.ui.client.widget.layout.RowData(-1, 1));
 
 			final LayoutContainer wrapper = new LayoutContainer();
 			wrapper.setLayout(new BorderLayout());
 			BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH,
					110, 200, 110);
 			data.setMargins(new Margins(10));
 			wrapper.add(searchWrapper, data);
 			wrapper.add(container, new BorderLayoutData(LayoutRegion.CENTER));
 
 			setLayout(new FillLayout());
 			add(wrapper);
 
 			addButtons();
 
 		}
 	}
 	
 	protected void addButtons() {
 		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				onSave();
 				hide();
 			}
 		}));
 		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 		}));
 	}
 
 	protected void onSave() {
 		final ArrayList<ClientUser> selectedUsers = new ArrayList<ClientUser>();
 		final List<RecentUser> recent = new ArrayList<RecentUser>();
 		final Iterator<SearchResults> iterator = selected
 				.getStore().getModels().iterator();
 		while (iterator.hasNext()) {
 			ClientUser user = iterator.next().getUser();
 			
 			selectedUsers.add(user);
 			recent.add(new RecentUser(user));
 		}
 		
 		UserCache.impl.addUsers(selectedUsers);
 		RecentlyAccessedCache.impl.add(RecentlyAccessed.USER, recent);
 		onSelect(selectedUsers);
 	}
 
 	/**
 	 * When the user clicks "Save", this function will be called, passing a list
 	 * of user IDs as selected.
 	 * 
 	 * @param selectedUsers
 	 */
 	public final void onSelect(final ArrayList<ClientUser> selectedUsers) {
 		if (listener != null)
 			listener.handleEvent(selectedUsers);
 	}
 	
 	public void setSelectionListener(ComplexListener<List<ClientUser>> listener) {
 		this.listener = listener;
 	}
 
 	public void refresh(List<ClientUser> selected) {
 		ListStore<SearchResults> store = new ListStore<SearchResults>();
 		store.setStoreSorter(new SearchResultsComparator());
 
 		for (ClientUser user : selected) {
 			SearchResults result = new SearchResults(user);
 			store.add(result);
 		}
 
 		this.selected.setStore(store);
 
 		ListStore<SearchResults> recentUserStore = new ListStore<SearchResults>();
 		recentUserStore.setStoreSorter(new SearchResultsComparator());
 
 		List<RecentUser> users = RecentlyAccessedCache.impl.list(RecentlyAccessed.USER); 
 		
 		for (RecentUser user : users)
 			recentUserStore.add(new SearchResults(user.getUser()));
 		
 		this.recent.setStore(recentUserStore);
 	}
 
 	/**
 	 * Performs the search and updates the status text. Appropriate parameter
 	 * keys are "lastname", "firstname", and "affiliation", although "email"
 	 * could be added in the future as any column in the profile table is
 	 * applicable at this time. A like query will be performed if parameters are
 	 * supplied, with multiple parameters being OR'd together.
 	 * 
 	 * @param params
 	 *            - search paramteers.
 	 */
 	protected void search(Map<String, String> params) {
 		GenericCallback<List<SearchResults>> callback = new GenericCallback<List<SearchResults>>() {
 
 			public void onFailure(Throwable caught) {
 				BrowseUsersWindow.this.hide();
 				WindowUtils.errorAlert("Error",
 						"Could not load results, please try again later");
 
 			}
 
 			public void onSuccess(List<SearchResults> result) {
 				final ListStore<SearchResults> store = new ListStore<SearchResults>();
 				store.setStoreSorter(new SearchResultsComparator());
 				for (SearchResults res : result) {
 					if (!containsSearchResult(selected.getStore(), res)) {
 						store.add(res);
 					}
 				}
 
 				results.setStore(store);
 
 				String statusText = "Found " + store.getCount() + " results.";
 				status.setHTML(statusText);
 			}
 		};
 
 		Map<String, List<String>> newParams = new HashMap<String, List<String>>();
 		for (Entry<String, String> entry : params.entrySet()) {
 			ArrayList<String> list = new ArrayList<String>();
 			list.add(entry.getValue());
 			newParams.put(entry.getKey(), list);
 		}
 		
 		UserSearchController.search(newParams, callback);
 	}
 
 	public void setInstructions(String instructions) {
 		setInstructions(instructions, false);
 	}
 	
 	public void setInstructions(String instructions, boolean appendBasicInstructions) {
 		if (!appendBasicInstructions)
 			this.instructions.setHtml(instructions);
 		else
 			this.instructions.setHtml(basicInstructions + instructions);
 	}
 
 	public void setPossibleUsersHeading(String possibleUsersHeading) {
 		this.possibleUsersHeading.setHtml("<b>" + possibleUsersHeading + "</b>");
 	}
 
 	public void setRecentUsersHeading(String recentUsersHeading) {
 		this.recentUsersHeading.setHtml("<b>" + recentUsersHeading + "</b>");
 	}
 
 	public void setSelectedUsersHeading(String selectedUsersHeading) {
 		this.selectedUsersHeading
 				.setHtml("<b>" + selectedUsersHeading + "</b>");
 	}
 
 }
