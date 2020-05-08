 package admin.panel.bonus;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.MainFrame;
 import data.GameData;
 import data.GameData.UpdateTag;
 import data.bonus.Bonus;
 import data.bonus.BonusQuestion;
 import data.bonus.BonusQuestion.BONUS_TYPE;
 
 /**
  * 
  * @author Kevin Brightwell, Justin MacDonald, Ramesh Raj
  *
  */
 public class BonusPanel extends JPanel implements Observer{
 
 	private static final long serialVersionUID = 1L;
 	
 	private JPanel pnlListQ = new JPanel();
 	private JPanel pnlViewWeek = new JPanel();
 	private ViewQPanel pnlQuestion = new ViewQPanel();
 
 	private JLabel lblViewWeek = new JLabel("View Week:");
 	
 	private SpinnerNumberModel weekModel = new SpinnerNumberModel(1, 1, 1, 1); // default,low,min,step
 	private JSpinner spnWeek = new JSpinner(weekModel);
 	
 	private JLabel lblViewQuestion = new JLabel("View Question:");
 	
 	private SpinnerNumberModel snmQuestion = new SpinnerNumberModel(1, 1, 1, 1); // default,low,min,step
 	private JSpinner spnQuestion = new JSpinner(snmQuestion);
 	
 	private JButton btnModify = new JButton("Modify");
 	
 	private JTextArea txtQuestionList;
 
 	private List<JTextField> tfMultiList;
 	
 	private List<JRadioButton> rbAnsList;
 	
 	private ChangeListener clWeek;
 	private ChangeListener clQuestion;
 	
 	////////////////// EDITS:
 	
 	private JPanel pnlQuestionEdit;
 	private CardLayout cardsQPanel;
 	
 	/* first STEP question */
 	private JPanel pnlNewQ1;
 	
 	private JLabel lblPrompt;
 	private JTextArea tfPromptInput;
 	
 	private JRadioButton rbMultChoice;
 	private JRadioButton rbShortAnswer;
 	
 	private JButton btnNextPart;
 	
 	private static final String STEP_1 = "step 1",
 			STEP_2 = "step 2";
 	
 	/* Second STEP panel */
 	private JPanel pnlNewQ2;
 	
 	private JPanel pnlQTypeSwap;
 	private CardLayout cardQType;
 	
 	private JPanel pnlMultiAns;
 	
 	private JPanel pnlShortAns;
 	private JLabel lblQAnswer;
 	private JTextArea tfShortAnswer;
 	
 	private JButton btnNewQBack;
 	private JButton btnNewQSubmit;
 	
 	private final static String TYPE_MULTI = "multii",
 			TYPE_SHORT = "shortt";
 	
 	/* end step panel */
 	
 	private JButton btnNewQ;
 	
 	private BonusQuestion currentQ;
 	
 	/**
 	 * Constructor for Bonus Panel
 	 */
 	public BonusPanel() {
 		super();
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 	
 		buildQuestionPaneAll();
 		
 		GameData g = GameData.getCurrentGame();
 		
 		// TODO: replace
 		initPnlQuestionListing();
 		
 		add(pnlQuestionEdit);
 		add(Box.createVerticalStrut(5));
 		add(pnlListQ);
 		
 		
 		//check if bonus questions already exist
 		initExistingBonus();
 		
 		initListeners();
 		
 		setEnableNewQPanel(false);
 		
 		btnModify.setEnabled(GameData.getCurrentGame().isSeasonStarted());
 		btnNewQ.setEnabled(GameData.getCurrentGame().isSeasonStarted());
 		
 		g.addObserver(this);
 	}
 	
 	private void buildQuestionPanelP1() {
 		// starting card:
 		pnlNewQ1 = new JPanel();
 		pnlNewQ1.setLayout(new BoxLayout(pnlNewQ1, BoxLayout.X_AXIS));
 		
 		lblPrompt = new JLabel("Prompt:");
 		lblPrompt.setAlignmentX(JLabel.LEFT_ALIGNMENT);
 		
 		tfPromptInput = new JTextArea("");
 		JScrollPane scroll = new JScrollPane(tfPromptInput);
 		tfPromptInput.setBorder(scroll.getBorder());
 		scroll.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
 		
 		// build the radio buttons for type:
 		JPanel rbPane = new JPanel();
 		rbPane.setLayout(new BoxLayout(rbPane, BoxLayout.Y_AXIS));
 		rbPane.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
 		
 		ButtonGroup bg = new ButtonGroup();
 		rbMultChoice = new JRadioButton("Multiple Choice");
 		rbShortAnswer = new JRadioButton("Short Answer");
 		bg.add(rbMultChoice); // link them together
 		bg.add(rbShortAnswer);
 		rbMultChoice.setSelected(true);
 		
 		rbPane.add(rbMultChoice);
 		rbPane.add(Box.createVerticalStrut(10));
 		rbPane.add(rbShortAnswer);
 		
 		btnNextPart = new JButton("Next");
 		btnNextPart.setAlignmentX(JButton.RIGHT_ALIGNMENT);
 		
 		// left pane
 		JPanel left = new JPanel();
 		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
 		
 		left.add(lblPrompt);
 		left.add(Box.createVerticalStrut(10));
 		left.add(scroll);
 		
 		left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
 		left.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
 		
 		// right:
 		JPanel right = new JPanel();
 		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
 		
 		right.add(Box.createVerticalGlue());
 		right.add(rbPane);
 		right.add(Box.createVerticalStrut(10));
 		right.add(Box.createVerticalGlue());
 		right.add(btnNextPart);
 		
 		right.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
 		right.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
 		
 		pnlNewQ1.add(left);
 		pnlNewQ1.add(Box.createHorizontalStrut(20));
 		pnlNewQ1.add(Box.createHorizontalGlue());
 		pnlNewQ1.add(right);
 		
 	}
 	
 	
 	/**
 	 * TODO:
 	 */
 	private void buildQuestionPanelP2() {
 		pnlNewQ2 = new JPanel();
 		pnlNewQ2.setLayout(new BoxLayout(pnlNewQ2, BoxLayout.X_AXIS));
 		
 		pnlQTypeSwap = new JPanel();
 		cardQType = new CardLayout();
 		pnlQTypeSwap.setLayout(cardQType);
 		
 		
 		/* Multiple choice: */
 		ButtonGroup bg = new ButtonGroup();
 		rbAnsList = new ArrayList<JRadioButton>(4);
 		
 		// multiple choice text fields:
 		tfMultiList = new ArrayList<JTextField>(4);
 		
 		pnlMultiAns = new JPanel();
 		pnlMultiAns.setLayout(new GridLayout(4, 0, 0, 5));
 		
 		// build the table:
 		String[] labels = { "A", "B", "C", "D" };
 		for (int i = 0; i < 4; i++) {
 			JRadioButton rb = new JRadioButton(labels[i]);
 		//	rb.setPreferredSize(new Dimension((int)(rb.getWidth()*1.5d), rb.getHeight()));
 			JTextField ans = new JTextField("");
 			
 			rbAnsList.add(rb);
 			tfMultiList.add(ans);
 			// add to button group
 			bg.add(rb);
 			
 			// layout the row:
 			rb.setAlignmentX(JTextField.LEFT_ALIGNMENT);
 			
 			JPanel subPane = new JPanel();
 			subPane.setLayout(new BoxLayout(subPane, BoxLayout.X_AXIS));
 			subPane.add(rb);
 			subPane.add(Box.createHorizontalStrut(5));
 			subPane.add(ans);
 			
 			// put the sub panel inside the main container for the questions
 			pnlMultiAns.add(subPane);
 		}
 		
 		rbAnsList.get(0).setSelected(true);
 		
 		/* end multi choice */
 		
 		/* short answer: */
 		
 		pnlShortAns = new JPanel();
 		pnlShortAns.setLayout(new BoxLayout(pnlShortAns, BoxLayout.Y_AXIS));
 		
 		lblQAnswer = new JLabel("Answer:");
 		tfShortAnswer = new JTextArea();
 		JScrollPane scroll = new JScrollPane(tfShortAnswer);
 		tfShortAnswer.setBorder(scroll.getBorder());
 		
 		lblQAnswer.setAlignmentX(JLabel.LEFT_ALIGNMENT);
 		scroll.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
 		
 		pnlShortAns.add(lblQAnswer);
 		pnlShortAns.add(Box.createVerticalStrut(10));
 		pnlShortAns.add(scroll);
 		
 		/* end short */
 		
 		pnlQTypeSwap.add(pnlMultiAns, TYPE_MULTI);
 		pnlQTypeSwap.add(pnlShortAns, TYPE_SHORT);
 		pnlQTypeSwap.setAlignmentX(JPanel.LEFT_ALIGNMENT);
 		//pnlQTypeSwap.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
 		
 		/* buttons: */
 		
 		btnNewQBack = new JButton("Back");
 		btnNewQSubmit = new JButton("Submit");
 		
 		btnNewQBack.setAlignmentX(JButton.RIGHT_ALIGNMENT);
 		btnNewQSubmit.setAlignmentX(JButton.RIGHT_ALIGNMENT);
 		
 		//btnNewQBack.setAlignmentY(JButton.BOTTOM_ALIGNMENT);
 		btnNewQSubmit.setAlignmentY(JButton.BOTTOM_ALIGNMENT);
 		
 		btnNewQBack.setPreferredSize(btnNewQSubmit.getPreferredSize());
 		
 		JPanel sub = new JPanel();
 		sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
 		
 		sub.add(Box.createVerticalGlue());
 		sub.add(btnNewQBack);
 		sub.add(Box.createVerticalStrut(5));
 		sub.add(btnNewQSubmit);
 		//sub.add(Box.createVerticalStrut(5));
 		
 		sub.setAlignmentY(JPanel.CENTER_ALIGNMENT);
 		
 		/* end buttons */
 		
 		
 		pnlNewQ2.add(pnlQTypeSwap);
 		pnlNewQ2.add(Box.createHorizontalStrut(20));
 		pnlNewQ2.add(Box.createHorizontalGlue());
 		pnlNewQ2.add(sub);
 		
 		if (rbMultChoice.isSelected()) {
 			cardQType.show(pnlQTypeSwap, TYPE_MULTI);
 		} else {
 			cardQType.show(pnlQTypeSwap, TYPE_SHORT);
 		}
 		
 		
 	}
 	
 	/**
 	 * Builds the entire question panel (all others included internally).
 	 */
 	private void buildQuestionPaneAll() {
 		pnlQuestionEdit = new JPanel();
 		pnlQuestionEdit.setBorder(BorderFactory.createTitledBorder("Question"));
 		cardsQPanel = new CardLayout(5, 5);
 		
 		pnlQuestionEdit.setLayout(cardsQPanel);
 		
 		buildQuestionPanelP1();
 		buildQuestionPanelP2();
 		
 		pnlQuestionEdit.add(pnlNewQ1, STEP_1);
 		pnlQuestionEdit.add(pnlNewQ2, STEP_2);
 		
 		cardsQPanel.show(pnlQuestionEdit, STEP_1);
 	}
 	
 	/**
 	 * construct the questing LISTING panel
 	 */
 	private void initPnlQuestionListing() {		
 		pnlListQ.setLayout(new BorderLayout());
 		
 		btnNewQ = new JButton("New");
 		
 		pnlListQ.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		txtQuestionList = new JTextArea("");
 		txtQuestionList.setBorder(null);
 		txtQuestionList.setEditable(false);
 		
 		pnlViewWeek.add(lblViewWeek);
 		pnlViewWeek.add(spnWeek);
 		pnlViewWeek.add(lblViewQuestion);
 		pnlViewWeek.add(spnQuestion);
 		pnlViewWeek.add(btnNewQ);
 		pnlViewWeek.add(btnModify);
 
 		
 		pnlListQ.add(pnlViewWeek, BorderLayout.NORTH);
 		pnlListQ.add(pnlQuestion, BorderLayout.CENTER);
 	}
 
 	/**
 	 * initialise the bonus panel if bonus questions already exist
 	 */
 	private void initExistingBonus() {
 		List<BonusQuestion> list = Bonus.getAllQuestions();
 		GameData g = GameData.getCurrentGame();
 		
 		setWeekSpinner(Bonus.getMaxWeek(), g.getCurrentWeek());
 		if (list == null || list.size() == 0) {
 			return; // nothing to load
 		}
 		
 		
 		setQuestionSpinner(1, Bonus.getNumQuestionsInWeek(1));
 		
 		setQuestionView(Bonus.getQuestion(getCurrentWeek(), getCurrentQNum()));
 	}
 	
 	/**
 	 * add the indicated bonus question to the LISTING pane basically, turn 
 	 * the bonus question into readable text
 	 * @param q
 	 */
 	private void setQuestionView(BonusQuestion q) {
 		pnlQuestion.updateLabels(q);	
 	}
 	
 	/**
 	 * set the value, and maximum value of the WEEK spinner
 	 * @param wValue
 	 * @param wMax
 	 */
 	private void setWeekSpinner(int wValue, int wMax) {
 		spnWeek.removeChangeListener(clWeek);
 		weekModel.setValue(wValue);
 		weekModel.setMaximum(wMax);
 		spnWeek.addChangeListener(clWeek);
 		
 		// update the state of the enable button
 		int gw =  GameData.getCurrentGame().getCurrentWeek();
 		btnModify.setEnabled(wValue == gw);
 	}
 	
 	/**
 	 * set the value and maximum value of the QUESTION spinner
 	 * @param qValue
 	 * @param qMax
 	 */
 	private void setQuestionSpinner(int qValue, int qMax) {
 		spnQuestion.removeChangeListener(clQuestion);
 		snmQuestion.setValue(qValue);
 		snmQuestion.setMaximum(qMax);
 		spnQuestion.addChangeListener(clQuestion);
 	}
 	
 	private void setEnableNewQPanel(boolean enabled) {
 		tfPromptInput.setEnabled(enabled);
 		btnNextPart.setEnabled(enabled);
 		
 		rbMultChoice.setEnabled(enabled);
 		rbShortAnswer.setEnabled(enabled);
 	}
 	
 	/**
 	 * checks if all multiple choice answers are 1-200 characters 
 	 * 		AND if at least one answer exists
 	 * @return
 	 */
 	// returns whether all four are valid as of now.. 
 	private Boolean isValidMultiAnswers() {
 		boolean res = true;
 		for (int i=0; i < 4 && res; i++) {
 			String s = tfMultiList.get(i).getText();
 			// checks each tf is (1,200)
 			res = res && (s.length() > 0 && s.length() <= 200); 
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * checks if a String is 1-200 characters
 	 * @param t
 	 * @return true if text is 1-200 char
 	 */
 	private Boolean isValidQuestionOrAnswer(String t){
 		return (t.length() > 0 && t.length() < 201);
 	}
 	
 	private int getCurrentWeek() {
 		return (Integer)spnWeek.getValue();
 	}
 	
 	private int getCurrentQNum() {
 		return (Integer)spnQuestion.getValue();
 	}
 	
 	private void setupNewQuestion() {
 		tfPromptInput.setText("");
 			
 		for (JTextField tf : tfMultiList) {
 			tf.setText("");
 		}
 		tfShortAnswer.setText("");
 		
 		cardsQPanel.show(pnlQuestionEdit, STEP_1);
 		
 		setEnableNewQPanel(true);
 	}
 	
 	/**
 	 * Sets a the question interface to display a question for input or
 	 * modification
 	 * @param bq
 	 */
 	private void setupFromQuestion(BonusQuestion bq) {
 		tfPromptInput.setText(bq.getPrompt());
 		
 		if (bq.getBonusType() == BonusQuestion.BONUS_TYPE.MULTI) {
 			rbMultChoice.setSelected(true);
 			
 			String[] choice = bq.getChoices();
 			for (int i = 0; i < tfMultiList.size(); i++) {
 				tfMultiList.get(i).setText(choice[i]);
 				
 				// set the correct answer to be selected
 				if (choice[i].equals(bq.getAnswer()))
 					rbAnsList.get(i).setSelected(true);
 			}
 		} else { // its a short answer then
 			rbShortAnswer.setSelected(true);
 			
 			tfShortAnswer.setText(bq.getAnswer());
 		}
 		
 		cardsQPanel.show(pnlQuestionEdit, STEP_1);
 		
 		setEnableNewQPanel(true);
 	}
 	
 	private BonusQuestion loadFromPanel() {
 		BonusQuestion bq = new BonusQuestion();
 		
 		bq.setPrompt(tfPromptInput.getText().trim());
 		
 		if (rbMultChoice.isSelected()) {
 			bq.setBonusType(BONUS_TYPE.MULTI);
 			
 			String[] answers = new String[rbAnsList.size()];
 			for (int i = 0; i < rbAnsList.size(); i++) {
 				answers[i] = tfMultiList.get(i).getText().trim();
 				
 				if (rbAnsList.get(i).isSelected()) {
 					bq.setAnswer(answers[i]);
 				}
 			}
 			
 			bq.setChoices(answers);
 		} else { // its a short answer
 			bq.setBonusType(BONUS_TYPE.SHORT);
 			
 			bq.setAnswer(tfShortAnswer.getText().trim());
 		}
 		
 		return bq;
 	}
 	
 	/**
 	 * initialises all of the listeners
 	 */
 	private void initListeners(){
 		
 		/*
 		 * Just moves towards next step of question modification
 		 */
 		btnNextPart.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				cardsQPanel.show(pnlQuestionEdit, STEP_2);
 			}		
 		});
 		
 		btnNewQBack.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				cardsQPanel.show(pnlQuestionEdit, STEP_1);
 			}
 		});
 		
 		/**
 		 * Adds the new question into the bonus array.
 		 */
 		btnNewQSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				MainFrame mf = MainFrame.getRunningFrame();
 				
 				if (rbMultChoice.isSelected()) {
 					if (!isValidMultiAnswers()) {
 						mf.setStatusErrorMsg("Answer Length must be between 1 " +
 								"and 200 characters", pnlQTypeSwap);
 						return;
 					}
 				} else {
 					if (!isValidQuestionOrAnswer(tfShortAnswer.getText().trim())) {
 						mf.setStatusErrorMsg("Answer Length must be between 1 " +
 								"and 200 characters", pnlQTypeSwap);
 						return;
 					}
 				}
 				
 				// no errors in input at this point:
 				BonusQuestion temp = loadFromPanel();
 				
 				temp.setWeek(currentQ.getWeek());
 				temp.setNumber(currentQ.getNumber());
 				
 				//since currentQ is already in the bonus array, just 
 				//update the value
 				currentQ.copy(temp);
 				
 				// check if it's present first
 				if (!Bonus.getAllQuestions().contains(currentQ)) 
 					Bonus.addNewQuestion(currentQ);
 				
 				// update the spinners and models. 
 				setQuestionView(currentQ);
 				setWeekSpinner(currentQ.getWeek(), Bonus.getMaxWeek());
 				int w = currentQ.getWeek();
 				setQuestionSpinner(w, Bonus.getNumQuestionsInWeek(w));
 				
 				// show the question we just made
 				spnWeek.setValue(w);
 				spnQuestion.setValue(currentQ.getNumber());
 				
 				// create a new question for the next one. 
 				setupNewQuestion();
 
 				setEnableNewQPanel(false);
 				
 				System.out.println(Bonus.getAllQuestions());
 			}		
 		});
 		
 		/*
 		 * Creates a new Bonus Question, clears the interface
 		 */
 		btnNewQ.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				GameData g = GameData.getCurrentGame();
 				int w = g.getCurrentWeek();
 				currentQ = new BonusQuestion(w, Bonus.getNumQuestionsInWeek(w) + 1);
 				
 				setupNewQuestion();
 			}
 			
 		});
 		
 		btnModify.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				currentQ = Bonus.getQuestion(getCurrentWeek(), getCurrentQNum());
 				
 				setupFromQuestion(currentQ);
 			}			
 		});
 		
 		// Action listener to show the correct pane on next screen
 		ActionListener rbClick = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (ae.getSource() == rbMultChoice){
 					cardQType.show(pnlQTypeSwap, TYPE_MULTI);
 				} else if (ae.getSource() == rbShortAnswer) {
 					cardQType.show(pnlQTypeSwap, TYPE_SHORT);
 				}
 			}
 		};
 		
 		rbMultChoice.addActionListener(rbClick);
 		rbShortAnswer.addActionListener(rbClick);
 		
 		clWeek = new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				// set the question set to the current week
 				int cw = getCurrentWeek();
 				setQuestionSpinner(1, Bonus.getNumQuestionsInWeek(cw));
 				
 				setQuestionView(Bonus.getQuestion(cw, getCurrentQNum()));
 				
 				spnQuestion.setValue(getCurrentQNum());
 				
 				int gameWeek = GameData.getCurrentGame().getCurrentWeek();
 				btnModify.setEnabled(cw == gameWeek);
 			}			
 		};
 		
 		spnWeek.addChangeListener(clWeek);
 		
 		clQuestion = new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				int cq = getCurrentQNum(); 
 				int max = (Integer)snmQuestion.getMaximum();
 				
 				if (cq > max)
 					return;
 				setQuestionView(Bonus.getQuestion(getCurrentWeek(), getCurrentQNum()));
 				currentQ = Bonus.getQuestion(getCurrentWeek(), getCurrentQNum());
 			}
 		};
 		
 		spnQuestion.addChangeListener(clQuestion);
 	}
 
 	public void update(Observable observ, Object obj) {
 		GameData g = (GameData)observ;
 		
 		EnumSet<UpdateTag> update = (EnumSet<UpdateTag>)obj;
 		
 		if (update.contains(UpdateTag.END_GAME)){
 			setEnableNewQPanel(false);	
 			
 			btnModify.setEnabled(false);
 			btnNewQ.setEnabled(false);
 		}
 		
 		
 		if (update.contains(UpdateTag.START_SEASON)){
			btnModify.setEnabled(GameData.getCurrentGame().isSeasonStarted());
			btnNewQ.setEnabled(GameData.getCurrentGame().isSeasonStarted());
 			
 		}
 		
 		if (update.contains(UpdateTag.ADVANCE_WEEK)) {
 			weekModel.setMaximum(g.getCurrentWeek());
 		}
 
 	}
 }
