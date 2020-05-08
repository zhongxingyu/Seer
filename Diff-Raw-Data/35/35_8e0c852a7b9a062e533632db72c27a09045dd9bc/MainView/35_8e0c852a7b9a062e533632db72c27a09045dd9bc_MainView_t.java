  package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTabbedPane;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements.EditRequirementPanel;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.overview.OverviewPanel;
 
 /**
  * This class sets the main view when user goes to the RequirementManager tab
  * It also allows opened tabs to be closed by the user
  * 
  * @author Arianna
  *
  */
 public class MainView extends JTabbedPane {
 
 	/**
 	 * Adds main subtab when user goes to RequirementManager
 	 */
 	public MainView() {
 		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		OverviewPanel overview = new OverviewPanel();
 		this.addTab("Overview", overview);
 		
 		JMenuItem closeAll = new JMenuItem("Close All Tabs");
 		closeAll.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ViewEventController.getInstance().closeAllTabs();
 				
 			}	
 		});		
 		
 		final JPopupMenu popup = new JPopupMenu();
 		popup.add(closeAll);
 		
 		this.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				if(e.isPopupTrigger()) popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 
 	/**
 	 * Overridden insertTab function to add the closable tab element.
 	 * 
 	 * @param title	Title of the tab
 	 * @param icon	Icon for the tab
 	 * @param component	The tab
 	 * @param tip	Showing mouse tip when hovering over tab
 	 * @param index	Location of the tab
 	 */
 	@Override
 	public void insertTab(String title, Icon icon, Component component,
 			String tip, int index) {
 		super.insertTab(title, icon, component, tip, index);
 		if (!(component instanceof OverviewPanel)) {
 			setTabComponentAt(index, new ClosableTabComponent(this));
 		}
 	}
 }
