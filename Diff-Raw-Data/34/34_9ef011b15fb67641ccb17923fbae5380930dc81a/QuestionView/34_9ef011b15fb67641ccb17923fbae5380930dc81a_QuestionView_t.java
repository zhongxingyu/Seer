 package com.heralli.ekonsehan;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 public class QuestionView extends JPanel implements MouseListener{
 	BackgroundPanel blankPanel;
 	JPanel typePanel;
 	JPanel questionPanel;
 	BackgroundPanel timesUpPanel;
 	BackgroundPanel answerPanel;
 	
 	Question q;
 	
 	int state=0; 
 	
 	public QuestionView(Question q)
 	{
 		ImageIcon im;
 		this.setLayout(new CardLayout());
 		this.setBackground(new Color(31,73,125));
 		
 		im = new ImageIcon(getClass().getResource("/resources/teaser.jpg"));
 		blankPanel = new BackgroundPanel(im.getImage(),BackgroundPanel.SCALED);
 		
 		this.add(blankPanel,"0");
 		
 		
 		//--------------------------------------------------------------------
 		JPanel top = new JPanel();
 		top.setBackground(new Color(31,73,125));
 		
 		JPanel bottom = new JPanel();
 		bottom.setBackground(new Color(31,73,125));
 		
 		JPanel center = new JPanel(new GridLayout(2,1));
 		center.setBackground(new Color(255,255,255));
 		
 		JLabel lblType = new JLabel(q.getType());
 		lblType.setHorizontalAlignment(SwingConstants.CENTER);
 		lblType.setFont(new Font("Times New Roman", Font.BOLD, 80));
 		
 		JLabel lblLevel = new JLabel("(" +q.getLevel()+" - " + q.getPoints() + " points )");
 		lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
 		lblLevel.setFont(new Font("Times New Roman", Font.BOLD, 70));
 		lblLevel.setForeground(new Color(100,100,100));
 		center.add(lblType);
 		center.add(lblLevel);
 		
 		typePanel = new JPanel(new GridLayout(3,1));
 		typePanel.add(top);
 		typePanel.add(center);
 		typePanel.add(bottom);
 		//-----------------------------------------------------------------
 		
 		this.add(typePanel,"1");
 		
 		
 		
 		GridBagLayout layout = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		questionPanel = new JPanel(layout);
 		
 		JPanel leftTop = new JPanel();
 		leftTop.setBackground(new Color(31,73,125));
 		c.gridx =0;
 		c.gridy =0;
 		layout.setConstraints(leftTop,c);
 		
 		JPanel rightTop = new JPanel(new BorderLayout());
 		rightTop.setBackground(new Color(255,255,255));		
 		c.gridx =1;
 		c.gridy =0;
 		layout.setConstraints(rightTop,c);
		JTextArea displayQuestion = new JTextArea(q.getQuestion());
		displayQuestion.setFont(new Font("Arial", Font.PLAIN, 70));
		displayQuestion.setLineWrap(true);
		displayQuestion.setWrapStyleWord(true);
		rightTop.add(new JScrollPane(displayQuestion));
 		
 		JPanel leftCenter = new JPanel();
 		leftCenter.setBackground(new Color(31,73,125));
 		c.gridx =0;
 		c.gridy =1;
 		layout.setConstraints(leftCenter,c);
 		
 		JPanel rightCenter = new JPanel(new BorderLayout());
 		rightCenter.setBackground(new Color(255,255,255));		
 		c.gridx =1;
 		c.gridy =1;
 		layout.setConstraints(rightCenter,c);
 				
 		JPanel leftBottom = new JPanel();
 		leftBottom.setBackground(new Color(31,73,125));
 		c.gridx =0;
 		c.gridy =2;
 		layout.setConstraints(leftBottom,c);
 		
 		JPanel rightBottom = new JPanel(new GridLayout(2,2));
 		rightBottom.setBackground(new Color(255,255,255));		
 		c.gridx =1;
 		c.gridy =2;
 		layout.setConstraints(rightBottom,c);
 		if (q.getType().equals(Question.MULTIPLE_CHOICE))
 		{
 			MultipleChoiceQuestion q2=(MultipleChoiceQuestion)q;
 			
 			JLabel choiceA = new JLabel(q2.getChoice(0));
			choiceA.setFont(new Font("Arial", Font.PLAIN, 70));
 			JLabel choiceB = new JLabel(q2.getChoice(1));
			choiceB.setFont(new Font("Arial", Font.PLAIN, 70));
 			JLabel choiceC = new JLabel(q2.getChoice(2));
			choiceC.setFont(new Font("Arial", Font.PLAIN, 70));
 			JLabel choiceD = new JLabel(q2.getChoice(3));
			choiceD.setFont(new Font("Arial", Font.PLAIN, 70));
 						
 			rightBottom.add(choiceA);
 			rightBottom.add(choiceB);
 			rightBottom.add(choiceC);
 			rightBottom.add(choiceD);
 		}
 		
 		
 		
 		
 		questionPanel.add(leftTop);
 		questionPanel.add(rightTop);
 		questionPanel.add(leftCenter);
 		questionPanel.add(rightCenter);
 		questionPanel.add(leftBottom);
 		questionPanel.add(rightBottom);
 		
 		layout.columnWeights = new double[]{0.0f,4.0f};
 		layout.rowWeights = new double[]{2.0f,2.0f};
 		
 		
 		this.setColumnMinWidth(layout, 0, 300);
		this.setRowMinHeight(layout, 0, 550);
 		this.setRowMinHeight(layout, 1, 300);
 		this.setRowMinHeight(layout, 2, 300);
 		
 		this.add(questionPanel,"2");
 		
 		addMouseListener(this);
 		
 		
 	}
 	
 	
 	// Sets the minimum width for column c to be w pixels wide
 	public void setColumnMinWidth(GridBagLayout gbl, int c, int w) {
 	    int[] ws = gbl.columnWidths;
 	    if (ws == null) {
 	        ws = new int[c+1];
 	    } else if (ws.length < c+1) {
 	        ws = new int[c+1];
 	        System.arraycopy(gbl.columnWidths, 0, ws, 0, gbl.columnWidths.length);
 	    }
 	    ws[c] = w;
 	    gbl.columnWidths = ws;
 	}
 
 	// Sets the minimum height for row r to be h pixels high
 	public void setRowMinHeight(GridBagLayout gbl, int r, int h) {
 	    int[] hs = gbl.rowHeights;
 	    if (hs == null) {
 	        hs = new int[r+1];
 	    } else if (hs.length < r+1) {
 	        hs = new int[r+1];
 	        System.arraycopy(gbl.rowHeights, 0, hs, 0, gbl.rowHeights.length);
 	    }
 	    hs[r] = h;
 	    gbl.rowHeights = hs;
 	}
 	public void mousePressed(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseEntered(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseExited(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseClicked(MouseEvent e) {
 	       CardLayout cl = (CardLayout)(this.getLayout());
 	       cl.show(this, state+"");
 	       state++;
 	    }
 	
 }
