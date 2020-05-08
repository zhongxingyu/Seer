 import java.awt.BorderLayout;
 import java.util.Timer;
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 
 public class MainGUI {
 	
 	MainGUI(int accessLevel) {
 		RadarSimulator rs = new RadarSimulator();
 		VMS v = new VMS(rs); 
 		final String VIEW[] = { "Table view", "Map view"};
 		JFrame f = new JFrame("TESTING VMS");		
 		f.setSize(700,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		FilterPanel fp = new FilterPanel();
 		TablePanel tp = new TablePanel(v.filterData(fp.getFilter()), rs, v, accessLevel);
 		MapPanel mp = new MapPanel(v, rs, fp);
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 		
 		tabbedPane.add(tp, VIEW[0]);
 		tabbedPane.add(mp,VIEW[1]);
 		f.setJMenuBar(new MenuBar());
 		if (accessLevel == 0)
 			f.add(fp, BorderLayout.WEST);
 		f.add(tabbedPane, BorderLayout.CENTER);
 				
 		Timer timer = new Timer("Update");		
 		UpdateTask ut = new UpdateTask(v, tp, fp, f, rs);
 		timer.schedule(ut, (int)(rs.getStartTime()*1000), (int)(rs.getTimeStep()*1000)); // (FUNCTION, DELAY, PERIOD)
 		
 		f.setVisible(true);
 		
 	}
 }
