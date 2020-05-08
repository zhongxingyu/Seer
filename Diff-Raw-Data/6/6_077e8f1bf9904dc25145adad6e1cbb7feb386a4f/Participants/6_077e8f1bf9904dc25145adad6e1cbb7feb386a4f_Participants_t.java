 package client.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 

 import common.UserI;
import common.UserListI;
 
 class Participants extends JPanel implements ActionListener {
 
 	protected JButton findP;
 	protected JList part;
 	protected JScrollPane scroll;
 	protected JLabel lPart;
 	protected AddParticipant adder;
 	protected DefaultListModel<UserI> model;
 	protected DefaultListSelectionModel selectionModel;
	protected UserListI users;
 
 	GridBagConstraints g = new GridBagConstraints();
 
 	public Participants() {
 		findP = new JButton("Legg til Deltaker");
 		findP.addActionListener(this);
 
 		model = new DefaultListModel<UserI>();
 
 		selectionModel = new DefaultListSelectionModel();
 		selectionModel
 				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		part = new JList(model);
 		part.setSelectionModel(selectionModel);
 
 		scroll = new JScrollPane(part);
 
 		lPart = new JLabel();
 		lPart.setText("Deltakere");
 
 		adder = new AddParticipant();
 
 		setLayout(new GridBagLayout());
 
 		g.gridy = 0;
 		add(lPart, g);
 		g.gridy = 1;
 		add(findP, g);
 		g.gridy = 2;
 		add(scroll, g);
 		g.gridy = 3;
 		g.gridx = 0;
 		g.gridheight = 4;
 		g.gridwidth = 2;
 		add(adder, g);
 		adder.setVisible(false);
 
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getActionCommand().toString().equals("Legg til Deltaker")) {
 			adder.setVisible(true);
 		}
 	}
 }
