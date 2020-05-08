 package eu.margiel.components.menu;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import eu.margiel.domain.MenuItem;
 import eu.margiel.utils.Components;
 
 @SuppressWarnings({ "serial" })
 public class MainMenuPanel extends Panel {
 
 	private final MenuLinks menuItemList;
 
 	public MainMenuPanel(String id, MenuItem mainMenu, MenuLinks menuItemList) {
 		super(id);
 		this.menuItemList = menuItemList;
 		add(new ListView<MenuItem>("menuItems", mainMenu.getChildren()) {
 			@Override
 			protected void populateItem(ListItem<MenuItem> item) {
 				MenuItem menuItem = item.getModelObject();
 				item.setVisible(menuItem.isPublished());
 				item.add(createLink("menuItem", menuItem));
				MarkupContainer submenu = new WebMarkupContainer("submenu");
 				submenu.setVisible(menuItem.hasChildren());
 				item.add(submenu);
				submenu.add(new ListView<MenuItem>("submenuItems", menuItem.getChildren()) {
 					@Override
 					protected void populateItem(ListItem<MenuItem> item) {
 						MenuItem submenu = item.getModelObject();
 						item.setVisible(submenu.isPublished());
 						item.add(createLink("submenuItem", submenu));
 					}
 				});
 			}
 		});
 	}
 
 	private Component createLink(String id, MenuItem menuItem) {
 		MenuLink menuLink = menuItemList.getMenuLinkFor(menuItem);
 		if (menuLink != null)
 			return new MenuVisualLink(id, menuItem, menuLink);
 		return Components.label(id, menuItem.getName());
 	}
 
 }
