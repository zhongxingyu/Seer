 package main;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 
 import net.miginfocom.swing.MigLayout;
 
 public class GUI extends JFrame{
 	private static final long serialVersionUID = 1L;
 	//dynamic Gift Fields
 	private ArrayList<Matching> list = new ArrayList<Matching>();
 	private ArrayList<Multichoice> list1 = new ArrayList<Multichoice>();
 	private ArrayList<ShortAnswer> list2 = new ArrayList<ShortAnswer>();
 	private ArrayList<MissingWord> list3 = new ArrayList<MissingWord>();
 	private ArrayList<StringBuilder> previewList = new ArrayList<StringBuilder>();
 
 	JPanel panel1 = new JPanel();
 	JPanel panel2 = new JPanel();
 	JPanel panel3 = new JPanel();
 	JPanel panel4 = new JPanel();
 
 	JPanel scrollPanel = new JPanel(new MigLayout("wrap 1", "grow, fill", ""));
 	JPanel scrollPane2 = new JPanel(new MigLayout("wrap 1", "grow, fill", ""));
 	JPanel scrollPane3 = new JPanel(new MigLayout("wrap 1", "grow, fill", ""));
 	JPanel scrollPane4 = new JPanel(new MigLayout("wrap 1", "grow, fill", ""));
 	
 	int numberOfQustions = 0;
 	int numberOfMissingWords = 0;
 	
 	//True/False fields
 	JTextArea TFtextArea;
 	JTextField TFtextField;
 	JRadioButton truebtn;
 	boolean titleSet = false;
 	//Matching fields
 	JTextField mtextField;
 	JTextArea mtextArea;
 	//Multi choice fields
 	JTextField mulTextField;
 	JTextArea mulTextArea;
 	//Short answer fields
 	JTextField sTextField;
 	JTextArea sTextArea;
 	//numerical fields
 	JTextField nTextField;
 	JTextArea nTextArea;
 	JTextField nCorrectTextArea;
 	JTextField nMinTextArea;
 	//Essay
 	JTextArea eTextArea;
 	JTextField eTextField;
 	//Description
 	JTextArea dTextArea;
 	JTextField dTextField;
 	//general fields
 	//missing word
 	JTextField misTextField;
 	JTextArea previewText;
 	
 	
 	
 	String onlyNumbers;
 	//gift building objects
 	FormatAccess formatAccess = new FormatAccess(); 
 	GiftBuilder giftBuilder = new GiftBuilder();
 
 	GUI() {
 		super("GIFT Format Creator");
 		JTabbedPane tabbedPane = new JTabbedPane();
 
 		JPanel panel1 = trueFalsePanel("Panel #1");
 		tabbedPane.addTab("True-False", panel1);
 		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 
 		JPanel panel2 = matchingPanel("Panel #2");
 		tabbedPane.addTab("Matching", panel2);
 		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
 
 		JPanel panel3 = multiChoicePanel("Panel #3");
 		tabbedPane.addTab("Multi Choice", panel3);
 		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
 
 		JPanel panel4 = shortAnswerPanel("Panel #4");
 		tabbedPane.addTab("Short Answer", panel4);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
 
 		JPanel panel5 = numericalPanel("Panel #5");
 		tabbedPane.addTab("Numerical", panel5);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_5);
 
 		JPanel panel6 = missingWordPanel("Panel #6");
 		tabbedPane.addTab("Missing Word", panel6);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_6);
 
 		JPanel panel7 = essayPanel("Panel #7 ");
 		tabbedPane.addTab("Essay", panel7);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_7);
 
 		JPanel panel8 = descriptionPanel("Panel #8");
 		tabbedPane.addTab("Description", panel8);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_8);
 		
 		JPanel panel9 = previewPanel("Panel #9");
 		tabbedPane.addTab("Preview", panel9);
 		tabbedPane.setMnemonicAt(3, KeyEvent.VK_9);
 
 		
 		getContentPane().add(tabbedPane);
 		setSize(660, 400);
 		setLocationRelativeTo(null);
 		setVisible(true);
 		setResizable(false);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 	}
 	private JPanel previewPanel(String string) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 1", "[grow]", "grow"));
 		panel.add(new JScrollPane(previewText = new JTextArea()),"grow");
 		JButton allQbtn= new JButton("Display all Questions");
 		JButton currentQbtn= new JButton("Write To File");
 		panel.add(allQbtn,"split 2");
 		panel.add(currentQbtn,"");
 		
 		//action Listeners
 		allQbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				previewText.setText("");
 				previewList =giftBuilder.giftOutPutBuilderList;
 				for(StringBuilder i:previewList){
 					previewText.append(i.toString());
 				}
 			}
 		});
 		currentQbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.writeQuestionsTofile();
 			}
 		});
 		return panel;
 	}
 	
 
 	JPanel trueFalsePanel(String panelName) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(TFtextField = new JTextField(), "growx,right,gapy 10");
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(TFtextArea = new JTextArea()), "grow,hmin 130,gapy 10");
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 		panel.add(new JLabel("Correct Answer"), "skip,split 3");
 		ButtonGroup radioGroupbtn = new ButtonGroup();
 		truebtn = new JRadioButton("True");
 		truebtn.setSelected(true);
 		JRadioButton falsebtn = new JRadioButton("False");
 		radioGroupbtn.add(truebtn);
 		radioGroupbtn.add(falsebtn);
 		panel.add(truebtn);
 		panel.add(falsebtn);
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn);
 		JButton addbtn = new JButton("Add Another True/False");
 		panel.add(addbtn, "split 2");
 		JButton deletebtn = new JButton("Delete Last Question");
 		panel.add(deletebtn, "gapy 10");
 		
 		//action Listeners
 		deletebtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.deleteLastQuestion("//true/false");		
 			}
 		});
 		addbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				if(!titleSet){ 
 					giftBuilder.append("//true/false");
 					giftBuilder.append(formatAccess.formatTitle(TFtextField.getText()));
 					titleSet=true;
 				}
 				giftBuilder.append(formatAccess.formatTrueFalse(TFtextArea.getText(),truebtn.isSelected())); 
 				TFtextArea.setText("");
 			}
 		});
 		clearbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				TFtextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(!titleSet){ 
 					giftBuilder.append("//true/false");
 					giftBuilder.append(formatAccess.formatTitle(TFtextField.getText()));
 				}
 				giftBuilder.append(formatAccess.formatTrueFalse(TFtextArea.getText(),truebtn.isSelected())); 
 				titleSet=false;
 				giftBuilder.append("\r\n\r\n"); //add new line
 				giftBuilder.appendQuestion();
 				TFtextField.setText("");
 				TFtextArea.setText("");
 			}
 		});
 		return panel;
 	}//end of true/false
 
 	JPanel matchingPanel(String panelName) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(mtextField = new JTextField(), "growx,right,gapy 10");
 		
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(mtextArea = new JTextArea()), "grow,hmin 70,gapy 10");
 		
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 
 		JButton addbtn;
 		addbtn = new JButton("Add Q&A");
 		panel.add(addbtn, "right, top,gapy 20");
 		panel.add(new JScrollPane(scrollPanel), "grow,right,gapy 10,hmin 90");
 
 		JButton deletebtn = new JButton("Delete Selected");
 		panel.add(deletebtn, "skip,al right,wrap");
 
 		panel.add(new DrawLine(), "skip 1");
 
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 20");
 		addbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				scrollPanel.add(panel1);
 				list.add(new Matching(panel1, numberOfQustions));
 				repaint();
 				numberOfQustions++;
 			}
 		});
 		deletebtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				int inter=0;
 				//boolean delete = true;
 				while(true){
 				for (Matching i : list) {
 					if(i.isChecked()){
 						i.delete(inter);
 						list.remove(i);
 						repaint();
 						inter=0;
 						break;
 					}
 					inter++;
 				}
 				if(list.size()<=inter) break;
 				}
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//matching");
 				giftBuilder.append(formatAccess.formatTitle(mtextField.getText()));
 				giftBuilder.append(mtextArea.getText()+" {"); 
 				titleSet=false;
 				for (Matching i : list) {
 					giftBuilder.append(formatAccess.formatMatching(i.getTextField1().getText(), i.getTextField2().getText()));
 				}
 				if(!list.isEmpty()) list.get(0).deleteAll();;
 				
 				list=new ArrayList<GUI.Matching>();
 				repaint();
 				numberOfQustions=0;
 				mtextField.setText("");
 				mtextArea.setText("");
 				giftBuilder.append("}\r\n"); //add new line
 				giftBuilder.appendQuestion();
 			}
 		});
 		clearbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				TFtextArea.setText("");
 			}
 		});
 
 		return panel;
 	}
 
 	JPanel multiChoicePanel(String panelName) {
 		JPanel panel = new JPanel();
 		// panel.setOpaque(false);
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(mulTextField=new JTextField(), "growx,right,gapy 10");
 
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(mulTextArea=new JTextArea()), "growx,hmax 90,hmin 90,gapy 20");
 
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 
 		panel.add(new DrawLine(), "skip 1");
 
 		JButton addansbtn;
 		addansbtn = new JButton("Add Answer");
 		panel.add(addansbtn, "right, top,gapy 10");
 		panel.add(new JScrollPane(scrollPane2),"grow,hmax 90,hmin 90,right,gapy 10");
 
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 10");
 
 		addansbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				scrollPane2.add(panel2);
 				list1.add(new Multichoice(panel2, numberOfQustions));
 				repaint();
 			}
 		});
 		clearbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				mulTextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//Multichoice");
 				giftBuilder.append(formatAccess.formatTitle(mulTextField.getText()));
 				giftBuilder.append(mulTextArea.getText()+" {"); 
 				for (Multichoice i : list1) {
 					giftBuilder.append(formatAccess.formatMultiChoice(i.getTextField1().getText(), i.getSpinner().getModel().getValue().toString()));
 				}
 				if(!list1.isEmpty()) list1.get(0).deleteAll();;
 				list1=new ArrayList<Multichoice>();
 				repaint();
 				mulTextField.setText("");
 				mulTextArea.setText("");
 				giftBuilder.append("}\n"); //add new line
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	JPanel shortAnswerPanel(String panelName) {
 		JPanel panel = new JPanel();
 		// panel.setOpaque(false);
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(sTextField=new JTextField(), "growx,right,gapy 10");
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(sTextArea= new JTextArea()), "growx,hmax 90,hmin 90,gapy 20");
 
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 		panel.add(new DrawLine(), "skip 1");
 		JButton addansbtn;
 		addansbtn = new JButton("Add Answer");
 		panel.add(addansbtn, "right, top,gapy 10");
 		panel.add(new JScrollPane(scrollPane3),"grow,hmax 90,hmin 90,right,gapy 10");
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 10");
 		addansbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				scrollPane3.add(panel3);
 				list2.add(new ShortAnswer(panel3, numberOfQustions));
 				repaint();
 			}
 		});
 		clearbtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				sTextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//Short Answer");
 				giftBuilder.append(formatAccess.formatTitle(sTextField.getText()));
 				giftBuilder.append(sTextArea.getText()+" {"); 
 				for (ShortAnswer i : list2) {
 					giftBuilder.append(formatAccess.formatShortAnswer(i.getTextField1().getText()));
 				}
 				if(!list2.isEmpty()) list2.get(0).deleteAll();
 				list2=new ArrayList<ShortAnswer>();
 				repaint();
 				sTextField.setText("");
 				sTextArea.setText("");
 				giftBuilder.append("}\n"); //add new line
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	JPanel numericalPanel(String panelName) {
 		JPanel panel = new JPanel();
 		SpinnerModel sm = new SpinnerNumberModel(0, 0, 100, 1);;
 		final JSpinner spinner;
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(nTextField = new JTextField(), "growx,right,gapy 10");
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(nTextArea = new JTextArea()), "growx,gapy 20");
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 		panel.add(new DrawLine(), "skip 1");
 		panel.add(new JLabel("Correct Answer"));
 		panel.add(nCorrectTextArea = new JTextField(), "growx");
 		panel.add(new JLabel("Range Margin: "), "");
 		panel.add(nMinTextArea = new JTextField(), "growx,split 2");
 		panel.add(spinner =new JSpinner(sm));
 		
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 20");
 		nCorrectTextArea.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				e.getSource();
 				int charCode = e.getKeyChar();
 				if (charCode > 31 && (charCode < 48 || charCode > 57)) {
 				}
 			}
 		});
 		clearbtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				nTextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//Numerical");
 				giftBuilder.append(formatAccess.formatTitle(nTextField.getText()));
 				giftBuilder.append(nTextArea.getText()+" {"); 
 				giftBuilder.append(formatAccess.formatNumerical(nCorrectTextArea.getText(), nMinTextArea.getText(),spinner.getModel().getValue().toString()));
 				nTextField.setText("");
 				nTextArea.setText("");
 				nCorrectTextArea.setText("");
 				nMinTextArea.setText("");		
 				giftBuilder.append("}\n"); //add new line
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	JPanel missingWordPanel(String panelName) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(misTextField = new JTextField(""),"growx,right,gapy 10");
 		JButton addansbtn;
 		addansbtn = new JButton("Add another missing word");
 		panel.add(addansbtn, "right,top,gapy 10,wrap");
 		panel.add(new JScrollPane(scrollPane4), "grow,right,span 2");
 		JButton savebtn = new JButton("Save");
 		panel.add(savebtn, "gapy 10");
 		addansbtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				numberOfMissingWords++;
 				scrollPane4.add(panel4);
 				list3.add(new MissingWord(panel4, numberOfMissingWords));
 				repaint();
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				giftBuilder.append("//Missing Word");
 				giftBuilder.append(formatAccess.formatTitle(misTextField.getText()));
 				
 				for (MissingWord i : list3) {
 					giftBuilder.append(formatAccess.formatMissingWord(i.getTextArea1().getText(),i.getTextField().getText(),i.getTextArea2().getText()));
 				}
 
 				if(!list3.isEmpty()) list3.get(0).deleteAll();
 				list3=new ArrayList<MissingWord>();
 				repaint();
 				misTextField.setText("");
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	JPanel essayPanel(String panelName) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(eTextField=new JTextField(), "growx,right,gapy 10");
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(eTextArea = new JTextArea()), "growx,gapy 20");
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 		panel.add(new DrawLine(), "skip 1");
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 20");
 		clearbtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				eTextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//Essay");
 				giftBuilder.append(formatAccess.formatTitle(eTextField.getText()));
 				giftBuilder.append(eTextArea.getText()+" {}"); 
 				eTextField.setText("");
 				eTextArea.setText("");
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	JPanel descriptionPanel(String panelName) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("wrap 2", "[][grow]", "grow"));
 		panel.add(new JLabel("Question Title (optional)"), "right");
 		panel.add(dTextField=new JTextField(), "growx,right,gapy 10");
 		panel.add(new JLabel("Question"), "right,top,gapy 20");
 		panel.add(new JScrollPane(dTextArea=new JTextArea()), "growx,gapy 20");
 		JButton clearbtn = new JButton("Clear Text");
 		panel.add(clearbtn, "skip,al right,wrap");
 		panel.add(new DrawLine(), "skip 1");
 		JButton savebtn = new JButton("Save Question");
 		panel.add(savebtn, "gapy 20");
 		clearbtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dTextArea.setText("");
 			}
 		});
 		savebtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				giftBuilder.append("//Description");
 				giftBuilder.append(formatAccess.formatTitle(eTextField.getText()));
 				giftBuilder.append(eTextArea.getText()+" {}"); 
				eTextField.setText("");
				eTextArea.setText("");
 				giftBuilder.appendQuestion();
 			}
 		});
 		return panel;
 	}
 
 	class DrawLine extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		public Dimension getPreferredSize() {
 			return new Dimension(520, 20);
 		}
 
 		protected void paintComponent(Graphics g) {
 			g.setColor(Color.red);
 			// X Start, Y Start, X End, Y End
 			g.drawLine(0, 0, 1000, 0);
 
 		}
 	}
 
 	// ****Inner Classes
 	class Matching {
 		private JTextField textField1;
 		private JTextField textField2;
 		private JLabel label;
 		private JCheckBox checkBox;
 		private JPanel panel;
 
 		Matching(JPanel panel, int count) {
 			this.panel=panel;
 			panel.setLayout(new MigLayout("wrap 5",
 					"[][grow,fill][][grow,fill][]", ""));
 			char letter = (char) ('A' + count);
 			label = new JLabel("" + letter);
 			panel.add(label);
 			panel.add(textField1 = new JTextField(), "grow");
 			panel.add(new JLabel("" + letter));
 			panel.add(textField2 = new JTextField(), "grow");
 			panel.add(checkBox=new JCheckBox("Delete"), "right");
 		}
 
 		public JTextField getTextField1() {
 			return textField1;
 		}
 
 		public JTextField getTextField2() {
 			return textField2;
 		}
 		public boolean isChecked(){
 			if(checkBox.isSelected()) return true;
 			
 			return false;
 		}
 		public void deleteAll(){
 			panel.removeAll();
 		}
 		public void delete(int multiplyer){
 			int compomentNumber=multiplyer*5;
 			panel.remove(compomentNumber);
 			panel.remove(compomentNumber);
 			panel.remove(compomentNumber);
 			panel.remove(compomentNumber);
 			panel.remove(compomentNumber);
 		}
 	}
 
 	class Multichoice {
 		private SpinnerModel sm = new SpinnerNumberModel(0, 0, 100, 1);;
 		private JSpinner spinner;
 		private JTextField textField1;
 		private JPanel panel;
 
 		Multichoice(JPanel panel, int count) {
 			this.panel=panel;
 			panel.setLayout(new MigLayout("wrap 2", "[grow, fill][]", "grow"));
 			panel.add(textField1 = new JTextField(), "grow");
 			panel.add(spinner = new JSpinner(sm));
 		}
 
 		public void deleteAll() {
 			panel.removeAll();
 		}
 
 		public JTextField getTextField1() {
 			return textField1;
 		}
 
 		public JSpinner getSpinner() {
 			return spinner;
 		}
 	}
 
 	class ShortAnswer {
 		private JTextField textField1;
 		private JPanel panel;
 
 		ShortAnswer(JPanel panel, int count) {
 			this.panel=panel;
 			panel.setLayout(new MigLayout("wrap 2", "[][grow, fill]", "grow"));
 			panel.add(new JLabel("Correct Answer: "), "al right");
 			panel.add(textField1 = new JTextField(), "growx");
 
 		}
 		public void deleteAll() {
 			panel.removeAll();	
 		}
 
 		public JTextField getTextField1() {
 			return textField1;
 		}
 	}
 
 	class MissingWord {
 		private int count;
 		private JTextArea textArea1;
 		private JTextField textField;
 		private JTextArea textArea2;
 		private JPanel panel;
 		
 		MissingWord(JPanel panel, int count) {
 			this.count = count;
 			this.panel=panel;
 			panel.setLayout(new MigLayout("wrap 2","[][grow,fill]", "grow"));
 			panel.add(new JLabel("Mising word" + count), "right,top");
 			panel.add(new DrawLine());
 			panel.add(new JLabel("Text Before mising word"), "right,top");
 			panel.add(textArea1 = new JTextArea(), "grow");
 			panel.add(new JLabel("Corret Missing Word"));
 			panel.add(textField = new JTextField(),"grow");
 			panel.add(new JLabel("Text After mising word"), "right,top");
 			panel.add(textArea2 = new JTextArea(), "grow");
 		}
 		public int getCount() {
 			return count;
 		}
 
 		public JTextArea getTextArea1() {
 			return textArea1;
 		}
 		public void deleteAll(){
 			panel.removeAll();
 		}
 
 		public JTextField getTextField() {
 			return textField;
 		}
 
 		public JTextArea getTextArea2() {
 			return textArea2;
 		}
 
 	}
 }
