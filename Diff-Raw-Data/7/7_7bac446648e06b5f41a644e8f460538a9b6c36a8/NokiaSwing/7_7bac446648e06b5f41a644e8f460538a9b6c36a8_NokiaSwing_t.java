 package org.agile.grenoble.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.agile.grenoble.Configuration;
 import org.agile.grenoble.Messages;
 import org.agile.grenoble.questions.AnswerType;
 import org.agile.grenoble.questions.AnswersType;
 import org.agile.grenoble.questions.ConfigurationType;
 import org.agile.grenoble.questions.QuestionType;
 import org.agile.grenoble.questions.QuestionsType;
 import org.agile.grenoble.user.User;
 
 class MyActionLogicListener implements ActionListener, ChangeListener {
 
 	
 	AnswerType at = null; 
 	public MyActionLogicListener(AnswerType pAt) {
 		at = pAt ;
 	}
 
 //	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		//select or not select ? 
 		AbstractButton ab = (AbstractButton) arg0.getSource();
 		if (ab.isSelected()== true) {
 			at.setSelected(AnswerType.Selected.TRUE);				
 		} else{
 			at.setSelected(AnswerType.Selected.FALSE);
 		}
 		//System.out.println("Yeh, a action is performed , my model should be updated :"  +at.getLabel()) ;
 	}
 	
 
 //	@Override
 	public void stateChanged(ChangeEvent arg0) {
 		//occure too often, seems selected/unselected/highligth/unhighlight
 		//select or not select ? 
 		AbstractButton ab = (AbstractButton) arg0.getSource();
 		if (ab.isSelected()== true) {
 			at.setSelected(AnswerType.Selected.TRUE);				
 		} else{
 			at.setSelected(AnswerType.Selected.FALSE);
 		}
 		//System.out.println("Yeh, a state change is detected, my model should be updated :" +at.getLabel() + "/" + at.getSelected()) ;
 	}
 
 }
 
 
 /*
  * myCheckGroup handle the logic of a set of checkbox,
  * with a maximun number of selected elements
  */
 class MyCheckGroup implements ActionListener {
 	
 	int iMaxSelected = -1 ;
 	int iNbCheckedItem = 0 ;
 	Vector<JCheckBox> v = new Vector<JCheckBox>();
 	
 	/*
 	 * maximun of element selectable in this group
 	 */
 	MyCheckGroup(int maxSelected) {
 		iMaxSelected = maxSelected ; 
 	}
 	
 	/* 
 	 * add a checkbox to this list of elements
 	 */
 	public void add(JCheckBox aBox) {
 	  aBox.addActionListener(this);
 	  v.add(aBox);
 	}
 
 	/*
 	 * when clicking on a checkbox, checks if max is reached. And forbid the selection 
 	 * if max is reached. 
 	 * note : allow unselection of elements
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 //	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		//select or not select ? 
 		JCheckBox cb = (JCheckBox) arg0.getSource();
 		if (cb.isSelected())
 		{
 			iNbCheckedItem++;
 			if (iNbCheckedItem > iMaxSelected) {
 				for ( Enumeration<JCheckBox> e = v.elements(); e.hasMoreElements(); ) {
 					JCheckBox value = e.nextElement();
 					if (value == cb) {
 						iNbCheckedItem--;
 						value.setSelected(false);
 						//display each vegetable
 						System.out.println( value );
 					}
 				 }
 			} else if (iNbCheckedItem < iMaxSelected) {
 				//nothing
 			} else {
 				//nothing
 			}
 		} else {
 			//unselect 
 			iNbCheckedItem--;
 		}
 		
 	}
 	
 }
 
 /*
  * main graphical class
  * */
 
 public class NokiaSwing  extends JFrame {
 	
 	private static final int globalHeight = 500;
 	private static final int globalWidth = 600;
 	private static final long serialVersionUID = -8322709712160036058L;
 	JPanel [] iPanel=  null;
 	JPanel iNavBar = null ;
 	JButton  nextOrTerminate = null ;
 	JPanel welcome = null;
 
	private String userName = "undefined";
 
 	public NokiaSwing ()  { 
 		super();
 	}
 
 	private void initContent() {
 		setTitle("Nokia Test - user '" + this.userName + "'" ) ;		
 	}
 	
 	//temporary 
 	public void generateQuestionDisplay(QuestionsType pQuestions) throws Exception {		
 		//JPanel questionArea = new JPanel();
 		iPanel=  generateQuestionsPanels(pQuestions);
 
 		//welcome = HomePage(); 
 		//getContentPane().add(welcome);
 		getContentPane().setBackground(Color.lightGray);
 		iNavBar = generateNavigationBar();
 
 		pack();
 		repaint();
 	}
 	
 	
 	/* 
 	 * Navigation Bar (may be a standalone class soon )
 	 * */
 	
 	public JPanel generateNavigationBar() {
 		JPanel navigationBar = new JPanel();
 		nextOrTerminate = new JButton("NextorTerminate");
 		navigationBar.add(nextOrTerminate,BorderLayout.EAST);
 		return navigationBar ;
 	}
 	
 	
 	/* 
 	 * Took a list of question, and build the panel. There's a panel per question.
 	 * */
 	private JPanel[] generateQuestionsPanels(QuestionsType pQuestions) throws Exception {		
 	   JPanel[] questionsPanels = new JPanel[pQuestions.getQuestionArray().length];
 		
 		for (int i=0; i < pQuestions.getQuestionArray().length; i++) {
 			questionsPanels[i] = generateQuestionPanel(pQuestions.getQuestionArray()[i]);
 
 		}
 		return questionsPanels;
 	}
 		
 
 	/** 
 	 * We return a panel per question, allowing us to display several panel in the same page
 	 * or one panel per page ... 
 	 * May be the change is the "Next " button inside the panel or outside it 
 	 * @throws Exception 
 	 * */ 
 	private JPanel generateQuestionPanel(QuestionType pQuestion) throws Exception {
 		JPanel questionContainer = generateQuestionArea();
 		if (pQuestion.getConfiguration().getType() != ConfigurationType.Type.COMPLEXE) {
 			
 			questionContainer.setLayout(new GridLayout(pQuestion.getAnswers().getAnswerArray().length+1, 1));
 	 		
 			Label questionText = new Label(pQuestion.getLabel());
 			questionContainer.add(questionText);
 			JLabel questionMark= Utils.getImage(Configuration.getString("NokiaTest.questionLogo"));
 			
 			JPanel questionPanel = new JPanel();
 			questionPanel.setLayout(new GridLayout(1, 2));
 			
 			Font font = new Font("sansserif", Font.BOLD, 16);
 			
 			questionText.setBackground(Color.blue);
 			questionText.setForeground(Color.yellow);
 			questionMark.setBackground(Color.blue);
 			questionText.setFont(font);
 			
 			questionPanel.add(questionText);
 			questionPanel.add(questionMark);
 		    
 		    questionContainer.add(questionPanel);
 			addAnswers(pQuestion.getConfiguration(), pQuestion.getAnswers(), questionContainer);
 		} else {
 			//the question if made of several question 
 			   JPanel childrenPanel = null ;
 			   questionContainer.setLayout(new GridLayout(pQuestion.getQuestionArray().length, 1));
 			   for (int i=0; i < pQuestion.getQuestionArray().length; i++) {
 				   childrenPanel = generateQuestionPanel(pQuestion.getQuestionArray()[i]);
 				   questionContainer.add(childrenPanel);
 			   } //end for 
 		} //end if 	
 
 		return questionContainer;
 	}
 
 	/*
 	 *  add answers to the question panel.
 	 * 
 	 */
 	private void addAnswers(ConfigurationType conf, AnswersType pAnswers, JPanel questionContainer) throws Exception {
 		 if (conf == null || conf.getType() == null ) {
 			//don't add anything
 			return ;
 		}
 		if (conf.getType() == ConfigurationType.Type.SINGLE) {
 			addRadioAnswers(pAnswers,questionContainer);	
 		} else if (conf.getType() == ConfigurationType.Type.MULTIPLE) {
 			addCheckAnswers(conf,pAnswers,questionContainer);
 		} else {
 			//ERROR 
 			throw new Exception("UNKNOWN question type :" + conf.getType());
 		}
 	}
 	
 	/* 
 	 * Add check box button look answers
 	 * 
 	 */
 	private void addCheckAnswers(ConfigurationType conf,AnswersType pAnswers, JPanel questionContainer) {
 		MyCheckGroup buttonGroup = new MyCheckGroup(conf.getNumber()) ;
 
 		for (int i = 0; i< pAnswers.getAnswerArray().length; i++){
 			String answer =  pAnswers.getAnswerArray()[i].getLabel();
 			JCheckBox answerText = new JCheckBox(answer);
 			//listen to click / unclick 
 			answerText.addChangeListener(new MyActionLogicListener(pAnswers.getAnswerArray()[i]));
 			questionContainer.add(answerText);
 			buttonGroup.add(answerText);
 		}
 	}
 
 	
 	/* 
 	 * Add radio button look answers
 	 * 
 	 */
 	private void addRadioAnswers(AnswersType pAnswers, JPanel questionContainer) {
 		ButtonGroup buttonGroup = new ButtonGroup() ;
 		
 		for (int i = 0; i< pAnswers.getAnswerArray().length; i++){
 			String answer =  pAnswers.getAnswerArray()[i].getLabel();
 			JRadioButton answerText = new JRadioButton(answer);
 			questionContainer.add(answerText);
 			//listen to click / unclick
 			answerText.addChangeListener(new MyActionLogicListener(pAnswers.getAnswerArray()[i]));
 			buttonGroup.add(answerText);
 		}
 	}
 	
 	
 
 	private JPanel generateQuestionArea() {
 		JPanel questionArea = new JPanel();
 		// Preferred height is irrelevant, since using WEST region
 		
 		questionArea.setPreferredSize(new Dimension(globalHeight,globalWidth));
 		questionArea.setBorder(BorderFactory.createLineBorder (Color.blue, 2));
 		questionArea.setBackground(Color.white);
 		return questionArea;
 	}
 
 	@SuppressWarnings("deprecation")
 	public void displayFirstquestion() {
 		initContent();
 		getContentPane().setBackground(Color.lightGray);
 		//TODO remove welcome message
 		//getContentPane().remove(welcome);
 		getContentPane().add(iPanel[0], BorderLayout.NORTH);
 		//TODO add i18n
 		nextOrTerminate.setLabel("Next");
 		getContentPane().add(iNavBar, BorderLayout.SOUTH );
 		pack();
 		repaint();
 		
 	}
 
 	public void registerOnClickNavBarEvent(NokiaControler nokiaControler) {
 		nextOrTerminate.addActionListener(nokiaControler);	
 	}
 
 	public void nextQuestion(int currentQuestionIndex,boolean isLast) {
 		getContentPane().remove(iPanel[currentQuestionIndex-1]);
 		getContentPane().add(iPanel[currentQuestionIndex], BorderLayout.NORTH);
 		//TODO add i18n
 		if (isLast) {
 			nextOrTerminate.setLabel(Messages.getString("NokiaTest.goHome") + " " + this.userName + " !");
 		} else {
 			nextOrTerminate.setLabel(Messages.getString("NokiaTest.next"));
 		}
 		//getContentPane().add(iNavBar, BorderLayout.SOUTH );
 		pack();
 		repaint();
 		
 	}
 
 	public void terminateTest() {
 		dispose();
 		
 	}
 
 	public void setUserName(User user) {
		if (user == null) {
			this.userName = user.getName();
		}
 	}
 }
