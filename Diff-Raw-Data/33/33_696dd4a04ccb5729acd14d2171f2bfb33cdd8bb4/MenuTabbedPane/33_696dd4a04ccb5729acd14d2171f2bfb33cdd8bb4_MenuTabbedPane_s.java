 package presentation.view;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import presentation.model.MainWindowModel;
 
 /**
  * Represents the tabbed pane on which the (three) main panes are placed.
  */
 public class MenuTabbedPane extends JTabbedPane {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel[] tabPanel;
 	private MainWindowModel model;
 	private String[][] tabInformation = {
 			{ "Recherche", "img/search.png",
 					"Suchen nach Benutzern oder Büchern" },
 			{ "Buch", "img/book.png", "Details eines Buches anzeigen" },
 			{ "Benutzer", "img/user.png",
 					"Personalien und Ausleihen eines Benutzers anzeigen" } };
 
 	public MenuTabbedPane(MainWindowModel model) {
 		this.model = model;
 		initGUI();
 		initChangeListener();
 	}
 
 	private void initGUI() {
 		tabPanel = new JPanel[tabInformation.length];
 
		tabPanel[0] = new SearchTabPanel();
 		// TODO: Replace with fully featured panes [Martin -> bis 27.10.2009]
 		tabPanel[1] = new JPanel();
 		tabPanel[2] = new JPanel();
 
 		for (int i = 0; i < tabInformation.length; i++) {
 			this.addTab(null, null, tabPanel[i], tabInformation[i][2]);
 			ImageIcon image = new ImageIcon(tabInformation[i][1]);
 			JLabel paneTitle = new JLabel(tabInformation[i][0], image,
 					JLabel.TRAILING);
 			paneTitle.setVerticalTextPosition(JLabel.BOTTOM);
 			paneTitle.setHorizontalTextPosition(JLabel.CENTER);
 			this.setTabComponentAt(i, paneTitle);
 		}
 
 		this.setTabPlacement(JTabbedPane.LEFT);
 	}
 
 	private void initChangeListener() {
 		final JTabbedPane t = this;
 		addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				model.setActiveTab(t.getSelectedIndex());
 			}
 		});
 	}
 
 	/**
 	 * Dient dem ActionListener der JMenuBar oder einem anderen Controller, den
 	 * Tab zu wechseln.
 	 * 
 	 * @param newTabIndex
 	 *            TabsPanel.SEARCH, TabsPanel.BOOK oder TabsPanel.USER
 	 */
 	public void switchTo(int newTabIndex) {
 		if (newTabIndex >= getTabCount())
 			return;
 		setSelectedIndex(newTabIndex);
 	}
 
 	public SearchTabPanel getSearchPanel() {
 		return (SearchTabPanel) (tabPanel[model.SEARCH_TAB]);
 	}
 	
 	public String getActiveTabTitle() {
 		if (getSelectedIndex() > tabInformation.length)
 			return "";
 		return tabInformation[getSelectedIndex()][0];
 	}
 
 }
