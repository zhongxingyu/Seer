 package net.marcuswhybrow.uni.g52gui.cw2.visual.tabs;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import net.marcuswhybrow.uni.g52gui.cw2.Browser;
 import net.marcuswhybrow.uni.g52gui.cw2.Settings;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.Window;
 import net.marcuswhybrow.uni.g52gui.cw2.visual.tabs.Tab.TabType;
 
 /**
  *
  * @author Marcus Whybrow
  */
 public class Tabs extends JTabbedPane implements ChangeListener
 {
 	/** The window these tabs belong to */
 	private Window window;
 	private ArrayList<Tab> tabs;
 	private Tab activeTab;
 
 	public Tabs(Window window)
 	{
 		this.window = window;
 		tabs = new ArrayList<Tab>();
 
 		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		addChangeListener(this);
 
 		openNewTab();
 	}
 
 	public void openWebPageTab()
 	{
 		openWebPageTab(null);
 	}
 
 	public void openWebPageTab(String url)
 	{
 		openTab(new Tab(this, url));
 	}
 
 	public void openNewTabTab()
 	{
 		openTab(new Tab(this, TabType.NEW_TAB_PAGE));
 	}
 
 
 	public void openNewTab()
 	{
 		switch (Settings.get().getNewTabState())
 		{
 			case USE_HOME_PAGE:
 				openWebPageTab(Settings.get().getHomePage());
 				break;
 			case NOT_SET:
 			case USE_NEW_TAB_PAGE:
 				openNewTabTab();
 				break;
 		}
 	}
 
 	public void openBookmarkManagerTab()
 	{
 		openTab(new Tab(this, TabType.BOOKMARK_MANAGER_PAGE));
 	}
 
 	public void openHistoryTab()
 	{
 		openTab(new Tab(this, TabType.HISTORY_PAGE));
 	}
 
 	public void openTab(Tab tab)
 	{
 		if (! tabs.contains(tab))
 			tabs.add(tab);
 
 		this.setActiveTab(tab);
 	}
 
 	public void selectNextTab()
 	{
 		int nextTabIndex = (this.tabs.indexOf(this.getActiveTab()) + 1) % this.tabs.size();
 		this.setActiveTab(this.tabs.get(nextTabIndex));
 	}
 
 	public void selectPreviousTab()
 	{
 		int nextTabIndex = (this.tabs.indexOf(this.getActiveTab()) - 1) % this.tabs.size();
 		this.setActiveTab(this.tabs.get(nextTabIndex));
 	}
 
 	public void setActiveTab(Tab tab)
 	{
 		try
 		{
 			setSelectedComponent((Component) tab);
 		}
 		catch (IllegalArgumentException e)
 		{
 			System.err.println("Tried to set '" + tab.toString() + "' to the active tab but it didn't exist");
 		}
 		activeTab = tab;
 	}
 
 	public void closeTab(Tab tab)
 	{
 		tabs.remove(tab);
 		remove(tab);
 		Browser.get().addClosedItem(tab);
 		if (tabs.size() == 0)
 			window.close(true);
 	}
 
 	public Window getWindow()
 	{
 		return window;
 	}
 
 	public Tab getActiveTab()
 	{
 		return activeTab;
 	}
 
 	public void stateChanged(ChangeEvent e)
 	{
 		activeTab = (Tab) getSelectedComponent();
		if (activeTab.getCurrentLocation() != null)
			window.setTitle(activeTab.getCurrentLocation().getTitle());
 	}
 
 	public ArrayList<Tab> getAllTabs()
 	{
 		return this.tabs;
 	}
 }
