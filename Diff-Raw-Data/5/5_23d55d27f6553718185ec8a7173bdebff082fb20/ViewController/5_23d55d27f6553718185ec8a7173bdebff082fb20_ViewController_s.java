 package de.phaenovum.controller;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import de.phaenovum.model.Team;
 import de.phaenovum.view.MultipleChoicePanel.MCListener;
 import de.phaenovum.view.OptionsPanel;
 import de.phaenovum.view.PatternPanel;
 import de.phaenovum.view.PatternPanel.PListener;
 import de.phaenovum.view.ScoreLabel;
 import de.phaenovum.view.ScorePanel;
 
 public class ViewController implements ActionListener, PListener, MCListener{
 
 	private MainController mc;
 	private JFrame frame;
 	private JPanel pnlMain = new JPanel();
 	private JPanel pnlLeft = new JPanel();
 	private JPanel pnlRight = new JPanel();
 	private JPanel pnlSouth = new JPanel();
 	private JPanel pnlCheck = new JPanel();
 	private JPanel pnlAdd = new JPanel();
 	private JPanel pnlSub = new JPanel();
 	private JButton btnAdd[] = new JButton[6];
 	private JButton btnSub[] = new JButton[6];
 	private OptionsPanel pnlOptions = new OptionsPanel();
 	private PatternPanel pnlPattern = new PatternPanel();
 	private ScoreLabel[] lblTeam = new ScoreLabel[6];
 	private ScorePanel pnlScore = new ScorePanel();
 	private JButton btnLoadQuest = new JButton("load Question");
 	private JButton btnLoadPattern = new JButton("load Pattern");
 	private JButton btnCorrect = new JButton("Correct");
 	private JButton btnWrong = new JButton("Wrong");
 	private JTextField tfQuest = new JTextField("0");
 	private Border border = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 	
 	private int questionNum = 0;
 	private int correct = -1;
 	
 	public ViewController(MainController mc){
 		this.mc = mc;
 		init();
 	}
 	
 	private void init(){
 		frame = new JFrame("Control Frame");
 		frame.setSize(400, 400);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pnlMain.setBorder(border);
 		pnlMain.setLayout(new BorderLayout(10, 10));
 		
 		pnlLeft.add(tfQuest);
 		pnlLeft.add(btnLoadQuest);
 
 		btnLoadQuest.addActionListener(this);
 		btnLoadPattern.addActionListener(this);
 		btnCorrect.addActionListener(this);
 		btnWrong.addActionListener(this);
 		
 		pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
 		pnlLeft.add(tfQuest);
 		pnlLeft.add(btnLoadQuest);
 		pnlLeft.add(btnLoadPattern);
 		
 		pnlScore = new ScorePanel();
 		for(int i = 0; i < 6; i++){
 			Team team = mc.teamController.getTeam(i);
 			lblTeam[i] = new ScoreLabel(team.teamName + ": " + team.points, team.teamColor);
 			btnAdd[i] = new JButton(team.teamName + " + ");
 			btnSub[i] = new JButton(team.teamName + " - ");
 			btnAdd[i].addActionListener(this);
 			btnSub[i].addActionListener(this);
 			pnlAdd.add(btnAdd[i]);
 			pnlSub.add(btnSub[i]);
 			pnlScore.add(lblTeam[i]);
 		}
 		
 		pnlPattern.setListener(this);
 		pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
 		pnlRight.add(btnCorrect);
 		pnlRight.add(btnWrong);
 		
 		pnlSouth.setLayout(new BoxLayout(pnlSouth, BoxLayout.Y_AXIS));
 		pnlSouth.add(pnlAdd);
 		pnlSouth.add(pnlScore);
 		pnlSouth.add(pnlSub);
 		
 		pnlMain.add(pnlLeft, BorderLayout.LINE_START);
 		pnlMain.add(pnlSouth, BorderLayout.SOUTH);
 		pnlMain.add(pnlRight, BorderLayout.LINE_END);
 		
 		frame.add(pnlMain);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	@Override
 	public void pAction(int id) {
 		mc.patternPanel.selectQuestion(id);
 		tfQuest.setText(Integer.toString(id));
 	}
 	
 	private void loadPattern(){
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlPattern);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.revalidate();
 		frame.repaint();
 		mc.loadPattern();
 	}
 	
 	private void loadQuestion(int id){
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlOptions = mc.questionTable.get(id).getOptionsPanel();
 		correct = mc.questionTable.get(id).correctAnswerID;
 		if(pnlOptions.pnlMC != null) pnlOptions.pnlMC.setMCListener(this);
 		pnlMain.add(BorderLayout.CENTER, pnlOptions);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.revalidate();
 		frame.repaint();
 		mc.loadQuestion(id);
 	}
 	
 	private void correct(){
 		pnlCheck.setBackground(Color.GREEN);
 		pnlMain.removeAll();
		pnlMain.add(BorderLayout.SOUTH,pnlScore);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlCheck);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.revalidate();
 		frame.repaint();
 		mc.correct();
 	}
 	
 	private void wrong(){
 		pnlCheck.setBackground(Color.RED);
 		pnlMain.removeAll();
		pnlMain.add(BorderLayout.SOUTH,pnlScore);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlCheck);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.revalidate();
 		frame.repaint();
 		mc.wrong();
 		
 	}
 	
 	private void updateTeam(){
 		Team team = null;
 		for(int i = 0; i < mc.teamController.teamList.size(); i++){
 			team = mc.teamController.getTeam(i);
 			lblTeam[i].setBackground(team.getTeamColor());
 			lblTeam[i].setText(team.teamName + ": " + team.points);
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource().equals(btnLoadPattern)){
 			loadPattern();
 		}else if(e.getSource().equals(btnLoadQuest)){
 			questionNum = Integer.parseInt(tfQuest.getText());
 			loadQuestion(questionNum);
 		}else if(e.getSource().equals(btnCorrect)){
 			correct();
 		}else if(e.getSource().equals(btnWrong)){
 			wrong();
 		}else{
 			for(int i = 0; i < 6; i++){
 				if(e.getSource().equals(btnAdd[i])){
 					mc.teamController.getTeam(i).addPoints(1);
 					mc.updateTeam();
 					updateTeam();
 				}else if(e.getSource().equals(btnSub[i])){
 					mc.teamController.getTeam(i).substractPoints(1);
 					updateTeam();
 					mc.updateTeam();
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void mcAction(int id) {
 		if(id == correct){
 			correct();
 		}else{
 			wrong();
 		}
 	}
 
 	@Override
 	public void mcSelect(int id) {
 		mc.questPanel.pnlMC.option[id].setBackground(Color.BLUE);
 	}
 
 	@Override
 	public void mcDeselect(int id) {
 		mc.questPanel.pnlMC.option[id].setBackground(Color.LIGHT_GRAY);	
 	}
 }
