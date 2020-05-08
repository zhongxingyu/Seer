 package admin;
 
 //TODO: MAKE THIS PANEL LOOK BETTER!
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout; 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.Utils;
 
 import data.GameData;
 import data.Person;
 
 public class GeneralPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	Integer viewWeek = 0;
 	
 	JLabel lblWeek = new JLabel("View Week:");
 	JLabel lblTribes = new JLabel("Tribes:");
 	JLabel lblRemainingContestants = new JLabel("");
 	JLabel lblCastOffs = new JLabel("");
 
 	JTextField txtTribe1 = new JTextField();
 	JTextField txtTribe2 = new JTextField();
 	
 	JButton btnStartSeason = new JButton("Start Season");
 	JButton btnAdvanceWeek = new JButton("Advance Week");
 	JButton btnChangeTribeName = new JButton("Save Changes");
 	
 	SpinnerNumberModel weekModel = new SpinnerNumberModel(0,0,0,1); //default,low,min,step
 	JSpinner spnWeek = new JSpinner(weekModel);
 	
 	JPanel pnlRemainingContestants = new JPanel();
 	JPanel pnlCastOffs = new JPanel();	
 	
 	GridBagLayout gbl = new GridBagLayout();
 	GridBagConstraints gbc = new GridBagConstraints();
 	
 	
 	public GeneralPanel() {
 		this.setLayout(gbl);
 		initPnlInfo();
 		initListeners();
 	}
 
 	private void initPnlInfo() {
 		
 		txtTribe1.setText(GameData.getCurrentGame().getTribeNames()[0]);
 		txtTribe2.setText(GameData.getCurrentGame().getTribeNames()[1]);	
 		
 		btnAdvanceWeek.setEnabled(false);
 		
 		pnlRemainingContestants.setBorder(BorderFactory.createTitledBorder("Contestants"));
 		pnlCastOffs.setBorder(BorderFactory.createTitledBorder("Cast Offs"));
 		pnlCastOffs.setPreferredSize(new Dimension(150, 200));
 		
 		pnlRemainingContestants.add(lblRemainingContestants);
 		pnlCastOffs.add(lblCastOffs);
 		
 		gbc.gridx = 0;
 		gbc.gridy = 0;		
 		gbc.weighty = 1;
 		gbc.weightx = 1;	
 		gbc.gridwidth = 2;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.insets = new Insets(0, 20, 10, 20);
 		add(btnStartSeason, gbc);
 		
 		gbc.gridx = 2;	
 		add(btnAdvanceWeek, gbc);
 		
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		add(lblTribes, gbc);
 		
 		gbc.gridx++;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		add(txtTribe1, gbc);
 		
 		gbc.gridx++;
 		add(txtTribe2, gbc);
 		
 		gbc.gridx++;
 		add(btnChangeTribeName, gbc);
 		
 		gbc.gridx = 0;
 		gbc.gridy++;
 		add(lblWeek, gbc);
 		
 		gbc.gridx = 1;
 		add(spnWeek, gbc);
 		
 		gbc.gridx = 0;
 		gbc.gridy++;	
 		gbc.gridwidth = 2;
 		gbc.gridheight = 40;
 		gbc.fill = GridBagConstraints.BOTH;
 		add(pnlRemainingContestants, gbc);
 		
 		gbc.gridx = 2;	
 		add(pnlCastOffs, gbc);
 	}
 
 	private void setRemainingContestantsLabel(){
 		String s = "<HTML>";
 		for (int i = 0; i < GameData.getCurrentGame().getAllContestants().size(); i++){			
 			if (GameData.getCurrentGame().getAllContestants().get(i).getCastDate() >= (Integer)spnWeek.getValue() 
 					|| GameData.getCurrentGame().getAllContestants().get(i).getCastDate() == -1)
 				s += GameData.getCurrentGame().getAllContestants().get(i) + "<BR>";
 		}
 		s += "</HTML>";
 		lblRemainingContestants.setText(s);
 	}
 	
 	private void setCastOffContestantsLabel(){
 		String s = "<HTML>";
 		for (int i = 0; i < GameData.getCurrentGame().getAllContestants().size(); i++){
 			if (GameData.getCurrentGame().getAllContestants().get(i).getCastDate() < (Integer)spnWeek.getValue()
 					&& GameData.getCurrentGame().getAllContestants().get(i).getCastDate() != -1)
 				s += GameData.getCurrentGame().getAllContestants().get(i).toString() 
					+ ": Cast-Off Week "
 					+ GameData.getCurrentGame().getAllContestants().get(i).getCastDate()
 					+ "<BR>";
 		}		
 		s += "</HTML>";
 		lblCastOffs.setText(s);
 	}
 	
 	private void initListeners() {
 		btnStartSeason.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameData g = GameData.getCurrentGame();
 					if(g.getAllContestants().size() == 0){
 						JOptionPane.showMessageDialog(null,"This will be a boring game with no contestants.  Add some!");
 						return;
 					}
 					if(g.getAllContestants().size() != g.getInitialContestants()){
 						JOptionPane.showMessageDialog(null,"You need to have " + g.getInitialContestants() + " contestants to start.");
 						return;
 					}
 					else if (!g.isSeasonStarted()) {
 					String s = JOptionPane.showInputDialog("Enter weekly bet amount!");
 					if (Utils.checkString(s, "^[0-9]+$")) {
 						if (Integer.parseInt(s) >= 0) {
 							GameData.getCurrentGame().startSeason(Integer.parseInt(s));
 							MainFrame.getRunningFrame().seasonStarted();
 							btnStartSeason.setEnabled(false);	
 							btnAdvanceWeek.setEnabled(true);
 							setRemainingContestantsLabel();
							weekModel.setMaximum(GameData.getCurrentGame().getCurrentWeek());
							weekModel.setValue(GameData.getCurrentGame().getCurrentWeek());
 							return;
 						}
 						return;
 					}
 					JOptionPane.showMessageDialog(null, "Invalid amount entered.");
 				}
 			}
 		});
 
 		btnAdvanceWeek.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameData.getCurrentGame().advanceWeek();
 				btnAdvanceWeek.setEnabled(!GameData.getCurrentGame().isSeasonEnded());
 				setRemainingContestantsLabel();
 				setCastOffContestantsLabel();
 				weekModel.setMaximum(GameData.getCurrentGame().getCurrentWeek());
 				weekModel.setValue(GameData.getCurrentGame().getCurrentWeek());
 			}
 		});
 		
 		btnChangeTribeName.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!Utils.checkString(txtTribe1.getText(),
 						Person.TRIBE_PATTERN)) {
 					MainFrame.getRunningFrame().setStatusErrorMsg(
 							"Tribe 1 name invalid.", txtTribe1);
 				} else if (!Utils.checkString(txtTribe2.getText(),
 						Person.TRIBE_PATTERN)) {
 					MainFrame.getRunningFrame().setStatusErrorMsg(
 							"Tribe 2 name invalid.", txtTribe2);
 				} else if (txtTribe1.getText().equals(txtTribe2.getText())){
 					MainFrame.getRunningFrame().setStatusErrorMsg("Invalid tribe names, cannot be the same", txtTribe1, txtTribe2);
 					return;
 				} else {
 					GameData.getCurrentGame().setTribeNames(
 							txtTribe1.getText(), txtTribe2.getText());
 					MainFrame mf = MainFrame.getRunningFrame();
 					mf.setStatusMsg("Tribes changed.");
 					mf.forceGameDataRefresh();
 				}
 			}
 
 		});
 		
 		spnWeek.addChangeListener(new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				setRemainingContestantsLabel();
 				setCastOffContestantsLabel();				
 			}
 			
 		});
 	}
 }
