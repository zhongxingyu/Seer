 package ui;
 
 import javax.swing.JFrame;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import java.awt.Color;
 import net.miginfocom.swing.MigLayout;
 
 public class Mainframe extends JFrame {
 
 	private JPanel header;
 	private JPanel body;
 	private JPanel lists;
 
 	public Mainframe() {
 		this.setSize(800, 600);
 		this.setExtendedState(MAXIMIZED_BOTH);
 		JPanel container = new JPanel();
 		JScrollPane scrPane = new JScrollPane(container);
 		getContentPane().add(scrPane);
 		
 		container.setLayout(new MigLayout("", "[grow]", "[50px][300px][grow]"));
 		
 		JPanel header = new JPanel();
 		container.add(header, "cell 0 0,alignx center,aligny center");
 
		 //JPanel body = new SchoolTotalsAndBreakdowns();
 		String authorName = "Robert Stevens";
 		SchoolTotalsAndBreakdowns body = new SchoolTotalsAndBreakdowns();
 		container.add(body, "cell 0 1,alignx left,aligny top");
 
 		JPanel lists = new SchoolAuthorList();
 		container.add(lists, "cell 0 2,alignx left,aligny top");
 		
 		 setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Mainframe mf = new Mainframe();
 		mf.setVisible(true);
 	}
 	
 	
 
 }
