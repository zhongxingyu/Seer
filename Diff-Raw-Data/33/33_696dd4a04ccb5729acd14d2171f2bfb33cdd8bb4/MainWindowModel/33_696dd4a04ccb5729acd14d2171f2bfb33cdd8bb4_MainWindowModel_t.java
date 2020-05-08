 package presentation.model;
 
 import java.util.Observable;
 
 import presentation.view.MenuTabbedPane;
 import domain.Book;
 
 /**
  * Manages the current status of the main window with its active user, tab,
  * menubar and status panel.
  */
 public class MainWindowModel extends Observable {
 
	private final String PROGRAM_NAME = " - BücherBox";
	public final static int SEARCH_TAB = 0;
	public final static int BOOK_TAB = 1;
	public final static int USER_TAB = 2;
 	private MenuTabbedPane tabs;
 	private int activeTab = SEARCH_TAB;
 	private Book activeBook;
 
 	public MainWindowModel() {
 		setTabs(new MenuTabbedPane(this));
 	}
 	
 	public Book getActiveBook() {
 		return activeBook;
 	}
 
 	public void setActiveBook(Book book) {
 		this.activeBook = book;
 		setChanged();
 		notifyObservers();
 	}
 
 	public void setActiveTab(int newTabIndex) {
 		this.activeTab = newTabIndex;
 		setChanged();
 		notifyObservers();
 	}
 
 	public int getActiveTabIndex() {
 		return activeTab;
 	}
	
 	public void setTabs(MenuTabbedPane tabs) {
 		this.tabs = tabs;
 	}
 
 	public MenuTabbedPane getTabs() {
 		return tabs;
 	}
 
 	public String getWindowTitle() {
 		return tabs.getActiveTabTitle() + PROGRAM_NAME;
 	}
 
 	// Statusbar-Text
 	// aktive Ansicht / Tab / Modus
 	// aktiver User
 
 }
