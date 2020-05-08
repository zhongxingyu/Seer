 package gui.items;
 
 import javax.swing.JMenuItem;
 
 import data.Service;
 
 import gui.MainWindow;
 import interfaces.Command;
 import interfaces.Gui;
 
 public class LaunchRequestItem extends JMenuItem implements Command {
 	private MainWindow	window;
 	private Gui			gui;
 
 	private Service		service;
 
 	public LaunchRequestItem(MainWindow window, Gui gui) {
 		this.window = window;
 		this.gui = gui;
 
 		addActionListener(window.getActionListener());
 	}
 
 	@Override
 	public void execute() {
 		System.out.println("Launch Offer");
		int row = window.getTable().rowAtPoint(window.getListener().getEvent().getPoint());
		int column = window.getTable().columnAtPoint(window.getListener().getEvent().getPoint());
		System.out.println("Selected Row : " + row + " Column : " + column);
		// buildPopupMenu(window.getModel().getServiceFromRow(row), column);
		gui.launchOffer(window.getModel().getServiceFromRow(row).getKey());
 	}
 
 	public void showItem(Service service) {
 		this.service = service;
 		setVisible(true);
 	}
 
 	
 	public void hideItem() {
 		setVisible(false);
 	}
 }
