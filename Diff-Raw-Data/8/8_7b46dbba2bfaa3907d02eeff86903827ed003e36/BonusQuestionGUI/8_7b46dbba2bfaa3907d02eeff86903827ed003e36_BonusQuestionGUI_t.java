 /**
  * BonusQuestionGUI -- Panel to display past weeks bonus questions
  * and with  an option to add new bonus questions for the current week
  * 
  * @author Manor Freeman, Hazel Rivera, Martin Grabarczyk, Liam Corrigan, Jeff
  *         Westaway, Delerina Hill
  *  V 1.0 03/15/12
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseListener;
 import java.util.Random;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.BoxLayout;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.text.AbstractDocument.Content;
 
 public class BonusQuestionGUI extends JPanel implements ActionListener {
 
 	private static final int MAX_CHARACTERS = 200;
 	private static final int TAB_WIDTH = 100;
 	private int currentRound, totalRounds;
 	private Round[] round;
 	private JTabbedPane questionPane;
 	private JButton newSAQ, newMCQ;
 	private JScrollPane newQuestionScrollPane;
 	private JPanel newAPanel;
 	private TextArea questionArea, correct;
 	private JPanel answerArea;
 	private JTextField fake1, fake2, fake3;
 	private JLabel fakeAnswersLabel;
 	private JButton save, cancel;
 
 	public BonusQuestionGUI(Round[] rounds, int currentRound) {
 		super();
 		if (currentRound == 0 || rounds == null) {
 			this.round = null;
 			this.totalRounds = 0;
 			this.currentRound = 0;
 		} else {
 			this.round = rounds;
 			this.totalRounds = rounds.length;
 			this.currentRound = currentRound;
 		}
 		repaint();
 		refresh();
 	}
 
 	public void addQuestion(boolean isMC) {
 
 		this.removeAll();
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		// make answer area
 		newAPanel = new JPanel();
		//newAPanel.setOpaque(false);
 		newAPanel.setLayout(new GridBagLayout());
 		// Constraints for lables
 		GridBagConstraints labelsCon = new GridBagConstraints();
 		GridBagConstraints fieldsCon = new GridBagConstraints();
 		labelsCon.gridwidth = GridBagConstraints.REMAINDER;
 		labelsCon.fill = GridBagConstraints.HORIZONTAL;
 		// Constraints for fields
 		fieldsCon.fill = GridBagConstraints.BOTH;
 		fieldsCon.weightx = 1.0;
 		fieldsCon.weighty = 1.0;
 		// Create blank question area
 		questionArea = new TextArea("Question:", "");
 		this.add(questionArea);
 		// MakeButtons
 		JPanel bPanel = new JPanel();
 		bPanel.setOpaque(false);
 		bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.X_AXIS));
 		save = new JButton(createImageIcon("images/q-save.png"));
 		save.setPressedIcon(createImageIcon("images/q-save-click.png"));
 		save.setOpaque(false);
 		save.setFocusPainted(false);
 		save.setBorderPainted(false);
 		save.setContentAreaFilled(false);
		save.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
 		save.setToolTipText("Save");
 		save.addActionListener(this);
 		cancel = new JButton(createImageIcon("images/q-discard.png"));
 		cancel.setPressedIcon(createImageIcon("images/q-discard-click.png"));
 		cancel.setOpaque(false);
 		cancel.setFocusPainted(false);
 		cancel.setBorderPainted(false);
 		cancel.setContentAreaFilled(false);
		cancel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
 		cancel.setToolTipText("Cancel");
 		cancel.addActionListener(this);
 		cancel.setActionCommand("cancel");
 		cancel.addActionListener(this);
 		bPanel.add(save);
 		bPanel.add(cancel);
 		// Create the answer text fields/areas & set them up
 		if (isMC) {
 			// create correct answer area
 			correct = new TextArea("CorrectAnswer:", "");
 			this.add(correct);
 
 			/*
 			 * correctAnswerLabel = new JLabel("Correct Answer:");
 			 * newAPanel.add(correctAnswerLabel, labelsCon); correct = new
 			 * JTextField(); correct.setColumns(20); correct.setDocument(d4);
 			 * newAPanel.add(correct, labelsCon);
 			 */
 			// fake answers
 			fakeAnswersLabel = new JLabel("Fake Answers:");
 			newAPanel.add(fakeAnswersLabel, labelsCon);
 			fake1 = new JTextField();
 			fake1.setColumns(20);
 			fake1.setDocument(new Doc(MAX_CHARACTERS));
 			newAPanel.add(fake1, fieldsCon);
 			fake2 = new JTextField();
 			fake2.setColumns(20);
 			fake2.setDocument(new Doc(MAX_CHARACTERS));
 			newAPanel.add(fake2, fieldsCon);
 			fake3 = new JTextField();
 			fake3.setColumns(20);
 			fake3.setDocument(new Doc(MAX_CHARACTERS));
 			newAPanel.add(fake3, fieldsCon);
 			save.setActionCommand("saveMC");
 			this.add(newAPanel);
 		} else {
 			answerArea = new TextArea("Answer:", "");
 			save.setActionCommand("saveSA");
 			this.add(answerArea);
 		}
 		this.add(newAPanel);
 		this.add(bPanel);
 		this.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
 		// newQuestionPanel.add(buttonPane);
 		this.remove(newSAQ);
 		this.revalidate();
 	}
 
 	// TODO return to home uses this getter to save bonus questions
 	/**
 	 * getter to access updated round array
 	 * 
 	 * @return
 	 */
 	public Round[] getRoundsArray() {
 		return this.round;
 	}
 
 	/**
 	 * makes tabbed panel to display old questions
 	 */
 	private void initQuestionPane() {
 
 		questionPane = new JTabbedPane();
 		questionPane.setUI(new CustomTabUI(TAB_WIDTH));
 		questionPane.setTabPlacement(SwingConstants.LEFT);
 		// questionPane.setBorder(new);
 		for (int i = totalRounds; i > 0; i--) {
 			String tabName = "Week " + Integer.toString(i);
 			questionPane.addTab(tabName, makeQPanel(i));
 		}
 
 		for (int i = currentRound; i < totalRounds; i++) {
 			questionPane.setEnabledAt(totalRounds - i - 1, false);
 		}
 		try {
 			questionPane.setSelectedIndex(totalRounds - currentRound);
 		} catch (Exception e) {
 
 		}
 
 	}
 
 	/**
 	 * makes scrollable panel showing the bonus questions for the round number
 	 * that is supplied
 	 * 
 	 * @param rNumber
 	 *            Round number to get bonus questions from round array
 	 * @return a scrollable panel to add to each tab
 	 */
 	private JComponent makeQPanel(int rNumber) {
 		// main panel vertical layout scrollable
 		JPanel panel = new JPanel();
 		panel.setOpaque(false);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		JScrollPane sp = new JScrollPane(panel);
 
 		BonusQuestion[] b;
 		// get bonus question array
 		// if no questions this round then display message
 		try {
 			b = round[rNumber - 1].getBonusQuestion();
 			if (b == null || b.length == 0) {
 				panel.setLayout(new BorderLayout());
 				panel.add(new JLabel("No Questions This Round."), BorderLayout.CENTER);
 				panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 				sp.setBorder(BorderFactory.createMatteBorder(-1, -1, -1, -1,
 						createImageIcon("images/vine-border.png")));
 				return sp;		
 			}
 		} catch (NullPointerException e) {
 			panel.setLayout(new BorderLayout());
 			panel.add(new JLabel("No Questions This Round."), BorderLayout.CENTER);
 			sp.setBorder(BorderFactory.createMatteBorder(-1, -1, -1, -1,
 					createImageIcon("images/vine-border.png")));
 			return sp;			
 		}
 		// if there are questions add them to the panel
 		try {
 			for (int i = 0; i < b.length; i++) {
 				JPanel borderQuestion = new JPanel();
 				borderQuestion.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
 				panel.add(borderQuestion);
 				JPanel eachQuestion = new JPanel();
 				eachQuestion.setLayout(new BoxLayout(eachQuestion, BoxLayout.X_AXIS));
 				eachQuestion.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
 				QuestionPanel qp = new QuestionPanel(b[i],
 						(currentRound == rNumber));
 				eachQuestion.add(qp);
 				borderQuestion.add(eachQuestion);
 				// if panel is being made for the current round
 				// add edit buttons to it
 				if (currentRound == rNumber) {
 					JPanel buttonPanel = new JPanel();
 					buttonPanel.setOpaque(false);
 					buttonPanel.setLayout(new BoxLayout(buttonPanel,
 							BoxLayout.Y_AXIS));
 					JButton update, cancel, delete;
 										
 					update = new JButton(createImageIcon("images/q-save.png"));
 					update.setPressedIcon(createImageIcon("images/q-save-click.png"));
 					update.setToolTipText("Update Question");
 					update.setOpaque(false);
 					update.setFocusPainted(false);
 					//update.setBorderPainted(false);
 					update.setContentAreaFilled(false);
 					update.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
 					cancel = new JButton(createImageIcon("images/q-discard.png"));
 					cancel.setPressedIcon(createImageIcon("images/q-discard-click.png"));
 					cancel.setToolTipText("Discard Changes");
 					cancel.setOpaque(false);
 					//cancel.setFocusPainted(false);
 					cancel.setBorderPainted(false);
 					cancel.setContentAreaFilled(false);
 					cancel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
 					delete = new JButton(createImageIcon("images/q-del.png"));
 					delete.setPressedIcon(createImageIcon("images/q-del-click.png"));
 					delete.setToolTipText("Delete Question");
 					delete.setOpaque(false);
 					delete.setFocusPainted(false);
 					//delete.setBorderPainted(false);
 					delete.setContentAreaFilled(false);
 					delete.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
 					// pass question number to action listener
 					update.setActionCommand("update");
 					cancel.setActionCommand("cancel");
 					delete.setActionCommand("delete");
 					update.addActionListener(new ButtonListener(
 							round[currentRound - 1], i + 1, qp, buttonPanel,
 							panel, this));
 					cancel.addActionListener(new ButtonListener(
 							round[currentRound - 1], i + 1, qp, buttonPanel,
 							panel, this));
 					delete.addActionListener(new ButtonListener(
 							round[currentRound - 1], i + 1, qp, buttonPanel,
 							panel, this));
 					buttonPanel.add(update);
 					buttonPanel.add(cancel);
 					buttonPanel.add(delete);
 
 					eachQuestion.add(buttonPanel);
 				}
 			}
 		} catch (NullPointerException e) {
 		}
 		
 
 		sp.setBorder(BorderFactory.createMatteBorder(-1, -1, -1, -1,
 				createImageIcon("images/vine-border.png")));
 		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		return sp;
 
 	}
 
 	public ImageIcon createImageIcon(String path) {
 		java.net.URL imgURL = SurvivorPoolAdminGUI.class.getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL);
 		} else {
 			System.err.println("Couldn't find file: " + path);
 			return null;
 		}
 	}
 
 	private void makeButtons() {
 		newSAQ = new JButton();
 		newSAQ.setIcon(createImageIcon("images/new-sa.png"));
 		newSAQ.setPressedIcon(createImageIcon("images/new-sa-click.png"));
 		newSAQ.setToolTipText("New Short Answer Question");
 		newSAQ.setActionCommand("newSA");
 		newSAQ.setOpaque(false);
 		newSAQ.setFocusPainted(false);
 		newSAQ.setBorderPainted(false);
 		newSAQ.setContentAreaFilled(false);
 		newSAQ.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		newSAQ.addActionListener(this);
 
 		newMCQ = new JButton();
 		newMCQ.setIcon(createImageIcon("images/new-mc.png"));
 		newMCQ.setPressedIcon(createImageIcon("images/new-mc-click.png"));
 		newMCQ.setToolTipText("New Multiple Choice Question");
 		newMCQ.setActionCommand("newMC");
 		newMCQ.setOpaque(false);
 		newMCQ.setFocusPainted(false);
 		newMCQ.setBorderPainted(false);
 		newMCQ.setContentAreaFilled(false);
 		newMCQ.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		newMCQ.addActionListener(this);
 	}
 
 	public void refresh() {
 		this.removeAll();
 		// game not started - display error label
 		if (currentRound == 0 || round == null) {
 			this.setLayout(new BorderLayout());
 			JLabel lbl = new JLabel();
 			java.net.URL imgURL = SurvivorPoolAdminGUI.class
 					.getResource("images/no-bq.png");
 			if (imgURL != null) {
 				lbl.setIcon(new ImageIcon(imgURL));
 			} else {
 				System.err.println("Couldn't find file: no-bq.png");
 			}
 			this.add(lbl, BorderLayout.CENTER);
 		}
 		// game started
 		else {
 			//this.setLayout(new BorderLayout());
 			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 			initQuestionPane();
 			this.add(questionPane);
 			//this.add(questionPane, BorderLayout.WEST);
 			makeButtons();
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setOpaque(false);
 			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 			buttonPanel.add(newSAQ);
 			buttonPanel.add(newMCQ);
 			//this.add(buttonPanel, BorderLayout.EAST);
 			this.add(buttonPanel);
 		}
 		this.setOpaque(false);
 		this.setVisible(true);
 		this.setBounds(new Rectangle(800, 400));
 		this.revalidate();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String cmd = arg0.getActionCommand();
 		if (cmd.equals("newMC")) {
 			addQuestion(true);
 		} else if (cmd.equals("newSA")) {
 			addQuestion(false);
 		} else if (cmd.equals("saveMC")) {
 			String[] answers, answersRandom;
 			String cAnswer, question;
 			cAnswer = correct.getText();
 			question = questionArea.getText();
 			answers = new String[4];
 			answersRandom = new String[4];
 			answers[0] = correct.getText();
 			answers[1] = fake1.getText();
 			answers[2] = fake2.getText();
 			answers[3] = fake3.getText();
 			// Check input lengths
 			if (question.length() < 1 || answers[0].length() < 1
 					|| answers[1].length() < 1 || answers[2].length() < 1
 					|| answers[3].length() < 1) {
 				Toolkit.getDefaultToolkit().beep();
 				return;
 			}
 			// Randomize answers
 			Random rand = new Random();
 			int randomNumber = rand.nextInt(4);
 			for (int i = 0; i < 4; i++) {
 				answersRandom[(randomNumber + i) % 4] = answers[i];
 			}
 			// New Bonus Question to round and load current week tab
 			BonusQuestion b = new BonusQuestion(question, answersRandom,
 					cAnswer);
 			round[currentRound - 1].addBonusQuestion(b);
 			refresh();
 		} else if (cmd.equals("saveSA")) {
 			String[] answers;
 			String cAnswer, question;
 			cAnswer = ((TextArea) answerArea).getText();
 			question = questionArea.getText();
 			answers = new String[1];
 			answers[0] = cAnswer;
 			// Check input length
 			if (answers[0].length() < 1) {
 				Toolkit.getDefaultToolkit().beep();
 				return;
 			}
 			// Add new bonus question to round
 			BonusQuestion b = new BonusQuestion(question, answers, cAnswer);
 			round[currentRound - 1].addBonusQuestion(b);
 			refresh();
 		} else if (cmd.equals("cancel")) {
 			refresh();
 		}
 
 	}
 }
