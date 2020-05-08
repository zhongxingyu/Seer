 package vms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 
 import common.Vessel;
 import common.Vessel.VesselType;
 
 import vms.*;
 
 public class RadarDisplay implements WindowListener {
 	
 	private final String VIEW[] = { "Table view", "Map view"};
 	
 	MainGUI _Main;
 	
 	JFrame _Frame;
 	JPanel _LeftPane;
 	FilterPanel _FilterPanel;
 	TablePanel _TablePanel;
 	MapPanel _MapPanel;
 	JTabbedPane _TabbedPane;
 	AlertPanel _AlertPanel;
 	
 	MainGUI.UserIdentity _CurrentIdentity;
 	
 	public RadarDisplay(MainGUI main) {
 		_Main = main;
 		_Frame = new JFrame("Vessel Monitoring System");
 		_Frame.addWindowListener(this);
 		_Frame.setSize(1300,600);
 		_Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		_FilterPanel = new FilterPanel();
 		_AlertPanel = new AlertPanel();
 		_TablePanel = new TablePanel();
 		_MapPanel = new MapPanel();
 		_LeftPane = new JPanel();
 		
 		
 		_TabbedPane = new JTabbedPane();		
 		_TabbedPane.add(_TablePanel, VIEW[0]);
 		_TabbedPane.add(_MapPanel,VIEW[1]);
 		
 		
 		_LeftPane.setLayout(new BoxLayout(_LeftPane, BoxLayout.Y_AXIS));	
 		_LeftPane.add(_AlertPanel);
 		_LeftPane.add(_FilterPanel);
 
 		_Frame.setJMenuBar(new MenuBar(_Frame));
 		_Frame.add(_LeftPane, BorderLayout.WEST);
 		_Frame.add(_TabbedPane, BorderLayout.CENTER);
 	}
 	public void show(MainGUI.UserIdentity identity) {
 		_CurrentIdentity = identity;
 		if (_CurrentIdentity == MainGUI.UserIdentity.NORMAL_USER) {
			_FilterPanel.setVisible(false);
 		}
 		else {
 			_FilterPanel.setVisible(true);
 		}
 		_TablePanel.changeIdentity(identity);
 		_Frame.setVisible(true);
 	}
 	
 	public void refresh(List<Alert> alerts, List<Vessel> vessels) {
 		vessels = filterData(vessels);
 		_TablePanel.update(alerts, vessels);
 		_MapPanel.update(alerts, vessels);
 		_AlertPanel.update(alerts);
 	}
 	
 	public List<Vessel> filterData(List<Vessel> vessels) {
 		if (_CurrentIdentity == MainGUI.UserIdentity.NORMAL_USER) {
 			return vessels; //No filtering
 		}
 		List<Vessel> copy = new ArrayList<Vessel>();
 		Map<VesselType, Boolean> filters = _FilterPanel.getActiveFilters();
 		for (Vessel v : vessels) {
 			if (filters.get(v.getType())) {
 				copy.add(v);
 			}
 		}
 		return copy;
 	}
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowClosed(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowClosing(WindowEvent e) {
 		_Main.stopServer();
 	}
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
