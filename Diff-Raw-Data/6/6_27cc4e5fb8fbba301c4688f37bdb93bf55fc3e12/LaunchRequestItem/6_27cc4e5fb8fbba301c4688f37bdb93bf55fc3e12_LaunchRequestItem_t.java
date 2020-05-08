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
		gui.launchOffer(service);
 	}
 
 	public void showItem(Service service) {
 		this.service = service;
 		setVisible(true);
 	}
 
 	
 	public void hideItem() {
 		setVisible(false);
 	}
 }
