 package GUIProject;
 
 import javax.swing.JPanel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Image;
 import java.awt.Graphics;
 import java.awt.BorderLayout;
 /**
  * 
  * @author Stephen Wen
  *
  */
 public class Person extends JPanel implements ActionListener{
 	private Question[][] words;
 	private int pointMax, pointMin,currentPoints,questionMax, quesNum = 0;
 	private Image Face;
 	private String phoneNumber;
 	private Question currentQuestion;
 	/**
 	 * 
 	 */
 	public Person(){
 		super();
		questionMax = 20;
 		setLayout(new BorderLayout());
 		words = new Question[10][3];
 		pointMin = 0;
 		pointMax = words.length-1;
 		currentPoints = (int)(Math.random()*words.length);
 		for(int i = 0; i<words.length;i++){
 			for(int j = 0; j<words[0].length;j++){
 				words[i][j] = new Question("This is the "+ j + "th "+ i + " point question ");
 			}
 		}
 		currentQuestion = words[currentPoints][(int)(Math.random()*words[0].length)];
 		add(currentQuestion);
 		
 	}
 	
 	/**
 	 * 
 	 * @param q
 	 */
 	public void add(Question q){
 		super.add(q.getLabel(), BorderLayout.PAGE_START);
 		super.add(q.getAnswers()[0],BorderLayout.LINE_START);
 		super.add(q.getAnswers()[1],BorderLayout.CENTER);
 		super.add(q.getAnswers()[2],BorderLayout.LINE_END);
 		super.add(q.getConfirm(), BorderLayout.PAGE_END);
 		currentQuestion.addListeners(this);
 		validate();
 	}
 	/**
 	 * 
 	 */
 	public void end(){
		System.out.println("Ended the date");
 	}
 	public void actionPerformed(ActionEvent e){
 		quesNum++;
 		if(currentPoints>=pointMin && currentPoints<=pointMax&&quesNum<=questionMax){
 			for(int i = 0; i < currentQuestion.getAnswers().length;i++){
 				if(currentQuestion.getAnswers()[i].isSelected()){
 					removeAll();
 					currentPoints+=currentQuestion.getAnswers()[i].getPoints();
 					currentQuestion = words[currentPoints][(int)(Math.random()*words[0].length)];
 					add(currentQuestion);
 				}
 			}
 		}
 		else{
 			end();
 		}
 	}
 }
