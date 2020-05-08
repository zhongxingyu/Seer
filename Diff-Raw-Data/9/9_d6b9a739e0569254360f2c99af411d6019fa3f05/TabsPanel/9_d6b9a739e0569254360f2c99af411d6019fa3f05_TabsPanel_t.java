 /*
  * Framework written by Brad Johnson
  * NextBooks
  * 2011-2012
  */
 
 package gui;
 
 import java.awt.GridLayout;
 import javax.swing.JPanel;
 
 /**
  * This panel provides the buttons for selecting which display panel is shown
  * to the user.
  * @author Brad Johnson
  * NextBooks 2011-2012
  * @modified Jake Bolam - Refactored tab buttons into array
  */
 public class TabsPanel extends JPanel {
 
     /**
      *
      */
     private static final long serialVersionUID = 3357923531295006611L;
 
     /**
      * An array to hold the placeholder tabs.
      */
     private static Tab[] tabArray;
 
     /**
      * There are 11 tabs in total, including functional and placeholder.
      */
     private static final int NUMTABS = 11;
 
     /**
      * An array to hold the active, usable tabs.
      */
     private static int activeTabs;
 
     /**
      * A counter to keep track of the next tab to fill.
      */
     private static int nextTab;
 
     /**
      * Constructor; builds a panel with a stack of "Tab" buttons which, when
      * clicked, change the main panel.
      */
     public TabsPanel() {
 
         this.setLayout(new GridLayout(0, 1));
         this.setBackground(PanelsManager.BACKGROUNDBLUE);
         tabArray = new Tab[NUMTABS];
         activeTabs = 0;
         nextTab = 0;
 
         // 1st Tab, Catalogue tab and search tab
         // On click opens up search results
         setTab("Book Catalogue", PanelsManager.CATALOGUE);
 
         // 2nd Tab, "My Books" tab
         // On click opens up customer's book inventory
         setTab("My Books", PanelsManager.MYBOOKS);
 
         // 3rd Tab, "My Account" tab
         // On click opens up customer's account settings
         setTab("My Account", PanelsManager.MYACCOUNT);
 
         // 4th Tab, "My Cart" tab
         // On click opens up the shopping cart - start of the checkout process
         setTab("My Cart", PanelsManager.MYCART);
        
        setTab("View Books", PanelsManager.BOOKS);
 
         //unused tabs
         while (nextTab < tabArray.length) {
 
             setPlaceholderTab();
 
         }
 
         // Load in all the Tabs
         for (int ii = 0; ii < tabArray.length; ii++) {
             this.add(tabArray[ii]);
         }
 
         //TODO: JUNIT test, make sure only 12 panels have been added       
 
         // Setting the default tab, Catalogue (tab array 0)
         PanelsManager.setDefaultPanel(tabArray[0]);
 
     }
 
     /**
      * Changes the color of all functional tabs to "unselected blue".
      * @postcond - the color of all functional tabs is
      * PanelsManager.unselectedBlue
      */
     public static void deselectAllTabs() {
 
         //This only applies to functional tabs, not empty "placeholder" tabs
         for (int ii = 0; ii < activeTabs; ii++) {
             tabArray[ii].setBackground(PanelsManager.UNSELECTEDBLUE);
         }
     }
 
     /**
      * Sets the next available tab for a specific purpose.
      * @param label - the tab's text
      * @param intention - the tab's intention, defined in PanelsManager
      */
     private static void setTab(final String label, final int intention) {
 
         tabArray[nextTab] = new Tab(label, intention);
         nextTab++;
         activeTabs++;
 
     }
 
     /**
      * Sets the next available to tab to a placeholder.
      */
     private static void setPlaceholderTab() {
 
         tabArray[nextTab] = new Tab("");
         nextTab++;
 
     }
 
 }
