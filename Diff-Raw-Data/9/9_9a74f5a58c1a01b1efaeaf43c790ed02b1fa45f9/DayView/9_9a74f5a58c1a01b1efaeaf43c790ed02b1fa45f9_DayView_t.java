 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 
 public class DayView extends JFrame {
 
 	private JLabel view;
 	private JLabel nameL = new JLabel("Title: ");
 	private JLabel duration = new JLabel("Duration: ");
 	private JLabel descripL = new JLabel("Description: ");
 	private JLabel locationL = new JLabel("Location: ");
 	private JLabel space = new JLabel("<html><br></html>");
 	private JButton close = new JButton("Exit View");
 	private JFrame thisThing = this;
 	private JScrollPane scroll;
 	private JPanel masterPane = new JPanel();
 	private GridBagLayout layout;
 
 	DayView(StoreData viewData, final JFrame parent, int numOfEvents){
 		layout = new GridBagLayout();
 		GridBagConstraints con = new GridBagConstraints();
 		scroll = new JScrollPane(masterPane);
 		masterPane.setLayout(layout);
 		this.add(scroll, BorderLayout.CENTER);
 
 
 		close.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				thisThing.dispose();
 				parent.setVisible(true);
 			}
 		});
 
 		for (int i = 0; i < numOfEvents; i++){
 			JLabel combine = new JLabel("Title: " + viewData.getSingleDay().get(i).getName());
 			
 			nameL = combine;
 			duration = new JLabel("Duration: ");
 			descripL = new JLabel("Description: "+viewData.getSingleDay().get(i).getDescription() + "");
 			locationL = new JLabel("Location: "+viewData.getSingleDay().get(i).getLocation());
 			
 			combine.setFont(new Font("Serif", Font.PLAIN, 52));
 			nameL.setFont(new Font("Serif", Font.PLAIN, 46));
 			duration.setFont(new Font("Serif", Font.PLAIN, 46));
 			descripL.setFont(new Font("Serif", Font.PLAIN, 46));
 			locationL.setFont(new Font("Serif", Font.PLAIN, 46));
 			space.setFont(new Font("Serif", Font.PLAIN, 46));
 			close.setFont(new Font("Serif", Font.PLAIN, 30));
 
 			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
 			sep.setPreferredSize(new Dimension(300,10));
 			JPanel eventPanel = new JPanel();
 			eventPanel.setLayout(new GridBagLayout());
 			GridBagConstraints s = new GridBagConstraints();
 			s.anchor = GridBagConstraints.NORTH;
 			s.insets = new Insets(0,0,5,0);
 			s.gridx = 0;
 			s.gridy = 0;
 			eventPanel.add(nameL, s);
 			s.gridx = 0;
 			s.gridy = 1;
 			eventPanel.add(duration, s);
 			s.gridx = 0;
 			s.gridy = 2;
 			eventPanel.add(descripL, s);
 			s.gridx = 0;
 			s.gridy = 3;
 			eventPanel.add(locationL, s);
 			s.gridx = 0;
 			s.gridy = 4;
 			eventPanel.add(close, s);
 			s.gridx = 0;
 			s.gridy = 5;
 			eventPanel.add(space, s);
 			s.fill = GridBagConstraints.VERTICAL;
 			s.weightx = 1;
 			//s.gridx = 0;
 			//s.gridy = 6;
 			eventPanel.add(sep, s);
 			con.gridx = 0;
 			con.gridy = i;
 
 			masterPane.add(eventPanel, con);
 			masterPane.revalidate();
 			masterPane.repaint();
 
 		}
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(600,550);
 		this.setVisible(true);
 		this.setResizable(false);
 	}
 
 	void addConfirmListener(ActionListener listenForConfirm){
 		close.addActionListener(listenForConfirm);
 	}
 
 }
