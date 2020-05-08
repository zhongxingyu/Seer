 package com.feeba.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Frame;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JButton;
 import javax.swing.JTabbedPane;
 
 import com.feeba.core.FeebaCore;
 import com.feeba.data.Question;
 import com.feeba.data.ReturnDataController;
 import com.feeba.data.Survey;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.awt.Component;
 
 import javax.swing.Box;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import java.awt.Color;
 
 import javax.swing.ImageIcon;
 
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.JLayeredPane;
 
 import java.awt.FlowLayout;
 
 import javax.swing.SpringLayout;
 
 import java.awt.Font;
 
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 
 import com.feeba.data.QuestionType;
 
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.ComponentOrientation;
 import java.awt.SystemColor;
 
 public class EditorGUI extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	public static JList questions;
 	public static JLabel backgroundPreview;
 	public static JTabbedPane tabbedPane;
 	private JTextField questionNameEdit;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JTextField textField_3;
 	private JTextField textField_4;
 	private JTextField textField_5;
 	private JTextField textField_6;
 	private JTextField textField_7;
 	private JTextField textField_8;
 	private JTextField textField_9;
 	public static JPanel editPanel;
 	boolean mouseDragging = false;
 	private JComboBox questionTypeEdit;
 	private JTextArea questionTextEdit;
 	public static JPanel results;
 	public static Box questionWrapper;
 	private JLabel lblAntwortmglichkeit;
 	private JPanel choicesEdit;
 	public final Color UICOLOR = FeebaCore.FEEBA_BLUE;
 	public static JComboBox comboBox;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					
 					EditorGUI frame = new EditorGUI();
 					frame.setState(Frame.NORMAL);
 					
 					//start editor maximized
 					Toolkit toolkit = Toolkit.getDefaultToolkit();
 					Dimension dimension = toolkit.getScreenSize();
 					frame.setSize(dimension);
 					frame.setVisible(true);
 					
 					updateBackgroundLabel(backgroundPreview,tabbedPane);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			private void updateBackgroundLabel(JLabel label, JTabbedPane pane) {
 				label.setSize(new Dimension(pane.getWidth(),pane.getHeight()));
 				
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	
 	public EditorGUI() {
 		setTitle("Feeba");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 821, 544);
 		contentPane = new JPanel();
 		contentPane.setBackground(Color.WHITE);
 		contentPane.setBorder(null);
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JPanel toolBar = new JPanel();
 		toolBar.setBackground(new Color(0x222325));
 		toolBar.setAlignmentY(Component.CENTER_ALIGNMENT);
 		toolBar.setPreferredSize(new Dimension(16, 50));
 		contentPane.add(toolBar, BorderLayout.NORTH);
 		SpringLayout sl_toolBar = new SpringLayout();
 		toolBar.setLayout(sl_toolBar);
 		
 		JLabel lblNewLabel = new JLabel("");
 		lblNewLabel.setIcon(new ImageIcon(EditorGUI.class.getResource("/images/logo_toolbar.png")));
 		toolBar.add(lblNewLabel);
 		
 		JPanel panel_1 = new JPanel();
 		sl_toolBar.putConstraint(SpringLayout.WEST, panel_1, 50, SpringLayout.EAST, lblNewLabel);
 		sl_toolBar.putConstraint(SpringLayout.NORTH, panel_1, 0, SpringLayout.NORTH, toolBar);
 		panel_1.setOpaque(false);
 		toolBar.add(panel_1);
 		
 		JButton btnNewButton = new JButton("Fragebogen Laden");
 		panel_1.add(btnNewButton);
 		btnNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		btnNewButton.setHorizontalTextPosition(SwingConstants.LEADING);
 		btnNewButton.setAlignmentY(Component.TOP_ALIGNMENT);
 		btnNewButton.setMargin(new Insets(0, 0, 0, 0));
 		btnNewButton.setPreferredSize(new Dimension(200, 50));
 		btnNewButton.setForeground(Color.WHITE);
 		btnNewButton.setFont(new Font("Helvetica", Font.BOLD, 10));
 		btnNewButton.setOpaque(true);
 		btnNewButton.setBackground(new Color(0x17748F));
 		btnNewButton.setBorder(new LineBorder(new Color(0x17748F), 7));
 		
 		Component horizontalStrut = Box.createHorizontalStrut(1);
 		panel_1.add(horizontalStrut);
 		
 		JButton btnFragebogenSpeichern = new JButton("Fragebogen Speichern");
 		panel_1.add(btnFragebogenSpeichern);
 		btnFragebogenSpeichern.setMargin(new Insets(0, 0, 0, 0));
 		btnFragebogenSpeichern.setPreferredSize(new Dimension(200, 50));
 		btnFragebogenSpeichern.setForeground(Color.WHITE);
 		btnFragebogenSpeichern.setFont(new Font("Helvetica", Font.BOLD, 10));
 		btnFragebogenSpeichern.setBackground(new Color(0x17748F));
 		btnFragebogenSpeichern.setOpaque(true);
 		btnFragebogenSpeichern.setBorder(new LineBorder(new Color(0x17748F), 7));
 		
 		Component horizontalStrut_1 = Box.createHorizontalStrut(1);
 		panel_1.add(horizontalStrut_1);
 		
 		JButton btnUmfrageStarten = new JButton("Umfrage Starten");
 		panel_1.add(btnUmfrageStarten);
 		btnUmfrageStarten.setMargin(new Insets(0, 0, 0, 0));
 		btnUmfrageStarten.setPreferredSize(new Dimension(200, 50));
 		btnUmfrageStarten.setForeground(Color.WHITE);
 		btnUmfrageStarten.setBackground(new Color(0x17748F));
 		btnUmfrageStarten.setOpaque(true);
 		btnUmfrageStarten.setBorder(new LineBorder(new Color(0x17748F), 7));
 		btnUmfrageStarten.setFont(new Font("Helvetica", Font.BOLD, 10));
 		btnUmfrageStarten.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(FeebaCore.currentSurvey!=null){
 				EditorController.startSurvey();}
 	        	else {JOptionPane.showMessageDialog(null, "Noch kein Fragebogen geladen!");}
 
 				
 			}
 		});
 		btnFragebogenSpeichern.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 								
 				saveFileChoser();
 			}
 
 		});
 		btnNewButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				
 				openFileChooser();
 				
 			}
 
 		});
 		
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBorder(new LineBorder(Color.WHITE, 12));
 		tabbedPane.setBackground(null);
 		
 		ChangeListener changeListener = new ChangeListener() {
 		      public void stateChanged(ChangeEvent changeEvent) {
 		        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
 		        int index = sourceTabbedPane.getSelectedIndex();
 		        if(index ==1) {
 		        	
 		        	if(FeebaCore.currentSurvey!=null){
 		        	EditorController.generateChart(results,questions.getSelectedIndex());}
 		        	else {JOptionPane.showMessageDialog(null, "Noch kein Fragebogen geladen!");}
 		        }
 		      }
 		    };
 		    
 		tabbedPane.addChangeListener(changeListener);
 		contentPane.add(tabbedPane, BorderLayout.CENTER);
 		
 		JPanel preview = new JPanel();
 		preview.setBorder(null);
 		preview.setBackground(null);
 		tabbedPane.addTab("Vorschau", null, preview, null);
 		SpringLayout sl_preview = new SpringLayout();
 		preview.setLayout(sl_preview);
 		
 		JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBackground(null);
 		sl_preview.putConstraint(SpringLayout.NORTH, layeredPane, 0, SpringLayout.NORTH, preview);
 		sl_preview.putConstraint(SpringLayout.WEST, layeredPane, 0, SpringLayout.WEST, preview);
 		sl_preview.putConstraint(SpringLayout.SOUTH, layeredPane, 0, SpringLayout.SOUTH, preview);
 		sl_preview.putConstraint(SpringLayout.EAST, layeredPane, 0, SpringLayout.EAST, preview);
 		preview.add(layeredPane);
 		SpringLayout sl_layeredPane = new SpringLayout();
 		layeredPane.setLayout(sl_layeredPane);
 		
 		JPanel panel_3 = new JPanel();
 		panel_3.setOpaque(false);
 		sl_layeredPane.putConstraint(SpringLayout.NORTH, panel_3, 0, SpringLayout.NORTH, layeredPane);
 		sl_layeredPane.putConstraint(SpringLayout.WEST, panel_3, 0, SpringLayout.WEST, layeredPane);
 		sl_layeredPane.putConstraint(SpringLayout.SOUTH, panel_3, 444, SpringLayout.NORTH, layeredPane);
 		sl_layeredPane.putConstraint(SpringLayout.EAST, panel_3, 0, SpringLayout.EAST, layeredPane);
 		panel_3.setBackground(null);
 		layeredPane.add(panel_3);
 		SpringLayout sl_panel_3 = new SpringLayout();
 		panel_3.setLayout(sl_panel_3);
 		
 		final JLabel questionName = new JLabel("");
 		questionName.setFont(new Font("Helvetica", Font.PLAIN, 30));
 		questionName.setForeground(Color.WHITE);
 		sl_panel_3.putConstraint(SpringLayout.NORTH, questionName, 103, SpringLayout.NORTH, panel_3);
 		sl_panel_3.putConstraint(SpringLayout.WEST, questionName, 0, SpringLayout.WEST, panel_3);
 		sl_panel_3.putConstraint(SpringLayout.EAST, questionName, 0, SpringLayout.EAST, panel_3);
 		questionName.setHorizontalAlignment(SwingConstants.CENTER);
 		questionName.setBackground(null);
 		panel_3.add(questionName);
 		
 		final JLabel questionText = new JLabel("");
 		questionText.setBackground(null);
 		sl_panel_3.putConstraint(SpringLayout.SOUTH, questionText, 100, SpringLayout.SOUTH, questionName);
 		questionText.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		questionText.setForeground(Color.WHITE);
 		questionText.setHorizontalAlignment(SwingConstants.CENTER);
 		sl_panel_3.putConstraint(SpringLayout.WEST, questionText, 0, SpringLayout.WEST, panel_3);
 		sl_panel_3.putConstraint(SpringLayout.EAST, questionText, 0, SpringLayout.EAST, panel_3);
 		panel_3.add(questionText);
 		
 		final JLabel questionChoices = new JLabel("");
 		questionChoices.setBackground(null);
 		sl_panel_3.putConstraint(SpringLayout.SOUTH, questionChoices, 80, SpringLayout.SOUTH, questionText);
 		questionChoices.setFont(new Font("Lucida Grande", Font.PLAIN, 23));
 		questionChoices.setForeground(Color.WHITE);
 		questionChoices.setHorizontalAlignment(SwingConstants.CENTER);
 		sl_panel_3.putConstraint(SpringLayout.WEST, questionChoices, 0, SpringLayout.WEST, panel_3);
 		sl_panel_3.putConstraint(SpringLayout.EAST, questionChoices, 0, SpringLayout.EAST, panel_3);
 		panel_3.add(questionChoices);
 		
 		backgroundPreview = new JLabel("");
 		backgroundPreview.setBackground(null);
 		sl_preview.putConstraint(SpringLayout.NORTH, backgroundPreview, 0, SpringLayout.NORTH, preview);
 		sl_preview.putConstraint(SpringLayout.WEST, backgroundPreview, 0, SpringLayout.WEST, preview);
 		sl_preview.putConstraint(SpringLayout.SOUTH, backgroundPreview, 0, SpringLayout.SOUTH, preview);
 		sl_preview.putConstraint(SpringLayout.EAST, backgroundPreview, 0, SpringLayout.EAST, preview);
 		preview.add(backgroundPreview);
 		backgroundPreview.setAlignmentY(Component.TOP_ALIGNMENT);
 		backgroundPreview.setIconTextGap(0);
 		
 
 		results = new JPanel();
 		results.setEnabled(false);
 		results.setBackground(Color.WHITE);
 		tabbedPane.addTab("Auswertung", null, results, null);
 		GridBagLayout gbl_results = new GridBagLayout();
 		gbl_results.columnWidths = new int[]{0};
 		gbl_results.rowHeights = new int[]{0};
 		gbl_results.columnWeights = new double[]{Double.MIN_VALUE};
 		gbl_results.rowWeights = new double[]{Double.MIN_VALUE};
 		results.setLayout(gbl_results);
 		questionWrapper = Box.createVerticalBox();
 		questionWrapper.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		questionWrapper.setAlignmentY(Component.TOP_ALIGNMENT);
 		questionWrapper.setBorder(null);
 		contentPane.add(questionWrapper, BorderLayout.WEST);
 		
 		questions = new JList();
		questions.setMinimumSize(new Dimension(200, 2000));
 		questions.setMaximumSize(new Dimension(200, 200));
 		questions.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		questions.setSelectionBackground(new Color(0x17748F));
 		questions.setBorder(null);
 		questions.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				
 				int selectedIndex = questions.getSelectedIndex();
 				fillPreviewFields(selectedIndex,questionName,questionText,questionChoices);
 				fillEditFields(selectedIndex,questionNameEdit,questionTextEdit,questionTypeEdit);
 				EditorController.generateChart(results,selectedIndex);
 				
 			}
 
 		});
 		
 		Component verticalStrut = Box.createVerticalStrut(20);
 		questionWrapper.add(verticalStrut);
 		questions.setPreferredSize(new Dimension(200, 10));
 		
 		JScrollPane questionScroller = new JScrollPane(questions);
 		questionScroller.setBorder(null);
 		questionWrapper.add(questionScroller);
 		
 		JPanel panel = new JPanel();
 		panel.setOpaque(false);
 		panel.setBorder(null);
 		panel.setBackground(new Color(0x2D2F31));
 		panel.setMaximumSize(new Dimension(32767, 30));
 		panel.setPreferredSize(new Dimension(200, 30));
 		panel.setSize(new Dimension(200, 40));
 		questionWrapper.add(panel);
 		
 		JButton button = new JButton("+");
 		button.setForeground(Color.DARK_GRAY);
 		button.setBackground(SystemColor.inactiveCaption);
 		button.setFont(new Font("Helvetica", Font.PLAIN, 22));
 		button.setOpaque(true);
 		button.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 		button.setMargin(new Insets(0, 0, 0, 0));
 		button.setPreferredSize(new Dimension(24, 24));
 		button.setBorder(null);
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		SpringLayout sl_panel = new SpringLayout();
 		sl_panel.putConstraint(SpringLayout.NORTH, button, 3, SpringLayout.NORTH, panel);
 		sl_panel.putConstraint(SpringLayout.EAST, button, -3, SpringLayout.EAST, panel);
 		panel.setLayout(sl_panel);
 		panel.add(button);
 		
 		JButton button_1 = new JButton("-");
 		button_1.setForeground(Color.DARK_GRAY);
 		sl_panel.putConstraint(SpringLayout.EAST, button_1, -3, SpringLayout.WEST, button);
 		button_1.setFont(new Font("Helvetica", Font.PLAIN, 25));
 		button_1.setOpaque(true);
 		button_1.setBackground(SystemColor.inactiveCaptionBorder);
 		sl_panel.putConstraint(SpringLayout.SOUTH, button_1, 0, SpringLayout.SOUTH, button);
 		button_1.setPreferredSize(new Dimension(24, 24));
 		button_1.setMargin(new Insets(0, 0, 0, 0));
 		button_1.setBorder(null);
 		button_1.setAlignmentY(1.0f);
 		panel.add(button_1);
 		
 		Component verticalStrut_1 = Box.createVerticalStrut(20);
 		questionWrapper.add(verticalStrut_1);
 		
 		editPanel = new JPanel();
 		editPanel.setBorder(new LineBorder(Color.WHITE, 10));
 		editPanel.setBackground(Color.WHITE);
 		editPanel.setPreferredSize(new Dimension(250, 10));
 		contentPane.add(editPanel, BorderLayout.EAST);
 		SpringLayout sl_editPanel = new SpringLayout();
 		editPanel.setLayout(sl_editPanel);
 		editPanel.setVisible(false);
 		
 		questionTypeEdit = new JComboBox();
 		sl_editPanel.putConstraint(SpringLayout.EAST, questionTypeEdit, -14, SpringLayout.EAST, editPanel);
 		questionTypeEdit.setModel(new DefaultComboBoxModel(QuestionType.values()));
 		questionTypeEdit.addItemListener(new ItemListener() {
 		     @Override
 		     public void itemStateChanged(ItemEvent e) {
 		    	 System.out.println("Change:" + e.paramString());
 		    	 //EditorController.loadedSurvey.getQuestions().get(questions.getSelectedIndex()).changeQuestionType((QuestionType)questionTypeEdit.getSelectedItem());
 			     toggleChoices();
 		     }
 		 });
 		editPanel.add(questionTypeEdit);
 		
 		JLabel questionType = new JLabel("Fragetyp:     ");
 		questionType.setBorder(new LineBorder(UICOLOR, 7));
 		questionType.setForeground(Color.WHITE);
 		questionType.setOpaque(true);
 		questionType.setBackground(UICOLOR);
 		questionType.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		sl_editPanel.putConstraint(SpringLayout.WEST, questionType, 10, SpringLayout.WEST, editPanel);
 		sl_editPanel.putConstraint(SpringLayout.NORTH, questionTypeEdit, 6, SpringLayout.SOUTH, questionType);
 		sl_editPanel.putConstraint(SpringLayout.WEST, questionTypeEdit, 10, SpringLayout.WEST, questionType);
 		sl_editPanel.putConstraint(SpringLayout.NORTH, questionType, 10, SpringLayout.NORTH, editPanel);
 		editPanel.add(questionType);
 		
 		JLabel lblFrage = new JLabel("Name:    ");
 		sl_editPanel.putConstraint(SpringLayout.WEST, lblFrage, 0, SpringLayout.WEST, questionType);
 		lblFrage.setOpaque(true);
 		lblFrage.setBackground(UICOLOR);
 		lblFrage.setBorder(new LineBorder(UICOLOR, 7));
 		lblFrage.setForeground(Color.WHITE);
 		lblFrage.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		sl_editPanel.putConstraint(SpringLayout.NORTH, lblFrage, 10, SpringLayout.SOUTH, questionTypeEdit);
 		editPanel.add(lblFrage);
 		
 		questionNameEdit = new JTextField();
 		sl_editPanel.putConstraint(SpringLayout.NORTH, questionNameEdit, 10, SpringLayout.SOUTH, lblFrage);
 		sl_editPanel.putConstraint(SpringLayout.WEST, questionNameEdit, 0, SpringLayout.WEST, questionTypeEdit);
 		sl_editPanel.putConstraint(SpringLayout.EAST, questionNameEdit, 0, SpringLayout.EAST, questionTypeEdit);
 		questionNameEdit.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				int selectedIndex = questions.getSelectedIndex();
 				FeebaCore.currentSurvey.getQuestions().get(selectedIndex).setName(questionNameEdit.getText().toString());
 				fillPreviewFields(selectedIndex,questionName,questionText,questionChoices);
 				
 				
 			}
 		});
 		questionNameEdit.setForeground(Color.WHITE);
 		questionNameEdit.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		questionNameEdit.setBackground(Color.LIGHT_GRAY);
 		questionNameEdit.setBorder(new LineBorder(Color.LIGHT_GRAY, 7));
 		editPanel.add(questionNameEdit);
 		questionNameEdit.setColumns(10);
 		
 		JLabel choicesLbl = new JLabel("Frage:    ");
 		sl_editPanel.putConstraint(SpringLayout.NORTH, choicesLbl, 10, SpringLayout.SOUTH, questionNameEdit);
 		choicesLbl.setOpaque(true);
 		choicesLbl.setBackground(UICOLOR);
 		choicesLbl.setBorder(new LineBorder(UICOLOR, 7));
 		choicesLbl.setForeground(Color.WHITE);
 		choicesLbl.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		sl_editPanel.putConstraint(SpringLayout.WEST, choicesLbl, 10, SpringLayout.WEST, editPanel);
 		editPanel.add(choicesLbl);
 		
 		choicesEdit = new JPanel();
 		sl_editPanel.putConstraint(SpringLayout.WEST, choicesEdit, 0, SpringLayout.WEST, questionNameEdit);
 		choicesEdit.setBackground(Color.WHITE);
 		editPanel.add(choicesEdit);
 		GridBagLayout gbl_choicesEdit = new GridBagLayout();
 		gbl_choicesEdit.columnWidths = new int[] {40, 150, 0};
 		gbl_choicesEdit.rowHeights = new int[] {20, 20, 20, 20, 20, 20, 20, 20, 20};
 		gbl_choicesEdit.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		gbl_choicesEdit.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		choicesEdit.setLayout(gbl_choicesEdit);
 		
 		
 		JLabel lblA = new JLabel("A : ");
 		lblA.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblA = new GridBagConstraints();
 		gbc_lblA.insets = new Insets(0, 0, 5, 5);
 		gbc_lblA.anchor = GridBagConstraints.WEST;
 		gbc_lblA.gridx = 0;
 		gbc_lblA.gridy = 0;
 		choicesEdit.add(lblA, gbc_lblA);
 		
 		textField_5 = new JTextField();
 		textField_5.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_5.setBorder(new LineBorder(new Color(192, 192, 192), 4));		textField_5.setBackground(Color.LIGHT_GRAY);
 		textField_5.setForeground(Color.WHITE);
 		textField_5.setBackground(Color.LIGHT_GRAY);
 		GridBagConstraints gbc_textField_5 = new GridBagConstraints();
 		gbc_textField_5.fill = GridBagConstraints.BOTH;
 		gbc_textField_5.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_5.gridx = 1;
 		gbc_textField_5.gridy = 0;
 		choicesEdit.add(textField_5, gbc_textField_5);
 		textField_5.setColumns(8);
 		
 		JLabel lblB = new JLabel("B :  ");
 		lblB.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblB = new GridBagConstraints();
 		gbc_lblB.fill = GridBagConstraints.BOTH;
 		gbc_lblB.insets = new Insets(0, 0, 5, 5);
 		gbc_lblB.gridx = 0;
 		gbc_lblB.gridy = 1;
 		choicesEdit.add(lblB, gbc_lblB);
 		
 		textField_8 = new JTextField();
 		textField_8.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_8.setForeground(Color.WHITE);
 		textField_8.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_8.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		textField_8.setBackground(Color.LIGHT_GRAY);
 		GridBagConstraints gbc_textField_8 = new GridBagConstraints();
 		gbc_textField_8.fill = GridBagConstraints.BOTH;
 		gbc_textField_8.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_8.gridx = 1;
 		gbc_textField_8.gridy = 1;
 		choicesEdit.add(textField_8, gbc_textField_8);
 		textField_8.setColumns(8);
 		
 		JLabel lblC = new JLabel("C :   ");
 		lblC.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblC = new GridBagConstraints();
 		gbc_lblC.fill = GridBagConstraints.BOTH;
 		gbc_lblC.insets = new Insets(0, 0, 5, 5);
 		gbc_lblC.gridx = 0;
 		gbc_lblC.gridy = 2;
 		choicesEdit.add(lblC, gbc_lblC);
 		
 		textField_7 = new JTextField();
 		GridBagConstraints gbc_textField_7 = new GridBagConstraints();
 		textField_7.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_7.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		textField_7.setBackground(Color.LIGHT_GRAY);
 		textField_7.setForeground(Color.WHITE);
 		gbc_textField_7.fill = GridBagConstraints.BOTH;
 		gbc_textField_7.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_7.gridx = 1;
 		gbc_textField_7.gridy = 2;
 		choicesEdit.add(textField_7, gbc_textField_7);
 		textField_7.setColumns(8);
 		
 		JLabel lblD = new JLabel("D :  ");
 		lblD.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblD = new GridBagConstraints();
 		gbc_lblD.fill = GridBagConstraints.BOTH;
 		gbc_lblD.insets = new Insets(0, 0, 5, 5);
 		gbc_lblD.gridx = 0;
 		gbc_lblD.gridy = 3;
 		choicesEdit.add(lblD, gbc_lblD);
 		
 		textField_6 = new JTextField();
 		GridBagConstraints gbc_textField_6 = new GridBagConstraints();
 		textField_6.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_6.setForeground(Color.WHITE);
 		textField_6.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		textField_6.setBackground(Color.LIGHT_GRAY);
 		gbc_textField_6.fill = GridBagConstraints.BOTH;
 		gbc_textField_6.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_6.gridx = 1;
 		gbc_textField_6.gridy = 3;
 		choicesEdit.add(textField_6, gbc_textField_6);
 		textField_6.setColumns(10);
 		
 		JLabel lblE = new JLabel("E :  ");
 		lblE.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblE = new GridBagConstraints();
 		gbc_lblE.fill = GridBagConstraints.BOTH;
 		gbc_lblE.insets = new Insets(0, 0, 5, 5);
 		gbc_lblE.gridx = 0;
 		gbc_lblE.gridy = 4;
 		choicesEdit.add(lblE, gbc_lblE);
 		
 		textField_4 = new JTextField();
 		GridBagConstraints gbc_textField_4 = new GridBagConstraints();
 		textField_4.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_4.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		textField_4.setBackground(Color.LIGHT_GRAY);
 		textField_4.setForeground(Color.WHITE);
 		gbc_textField_4.fill = GridBagConstraints.BOTH;
 		gbc_textField_4.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_4.gridx = 1;
 		gbc_textField_4.gridy = 4;
 		choicesEdit.add(textField_4, gbc_textField_4);
 		textField_4.setColumns(10);
 		
 		JLabel lblF = new JLabel("F :  ");
 		lblF.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblF = new GridBagConstraints();
 		gbc_lblF.anchor = GridBagConstraints.WEST;
 		gbc_lblF.fill = GridBagConstraints.BOTH;
 		gbc_lblF.insets = new Insets(0, 0, 5, 5);
 		gbc_lblF.gridx = 0;
 		gbc_lblF.gridy = 5;
 		choicesEdit.add(lblF, gbc_lblF);
 		
 		textField_3 = new JTextField();
 		GridBagConstraints gbc_textField_3 = new GridBagConstraints();
 		textField_3.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_3.setForeground(Color.WHITE);
 		textField_3.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		textField_3.setBackground(Color.LIGHT_GRAY);
 		gbc_textField_3.fill = GridBagConstraints.BOTH;
 		gbc_textField_3.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_3.gridx = 1;
 		gbc_textField_3.gridy = 5;
 		choicesEdit.add(textField_3, gbc_textField_3);
 		textField_3.setColumns(10);
 		
 		JLabel lblG = new JLabel("G :  ");
 		lblG.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblG = new GridBagConstraints();
 		gbc_lblG.fill = GridBagConstraints.BOTH;
 		gbc_lblG.insets = new Insets(0, 0, 5, 5);
 		gbc_lblG.gridx = 0;
 		gbc_lblG.gridy  = 6;
 		choicesEdit.add(lblG, gbc_lblG);
 		
 		textField_1 = new JTextField();
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		textField_1.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_1.setForeground(Color.WHITE);
 		textField_1.setBackground(Color.LIGHT_GRAY);
 		textField_1.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		gbc_textField_1.fill = GridBagConstraints.BOTH;
 		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_1.gridx = 1;
 		gbc_textField_1.gridy = 6;
 		choicesEdit.add(textField_1, gbc_textField_1);
 		textField_1.setColumns(10);
 		
 		JLabel lblH = new JLabel("H :  ");
 		lblH.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblH = new GridBagConstraints();
 		gbc_lblH.fill = GridBagConstraints.BOTH;
 		gbc_lblH.insets = new Insets(0, 0, 5, 5);
 		gbc_lblH.gridx = 0;
 		gbc_lblH.gridy = 7;
 		choicesEdit.add(lblH, gbc_lblH);
 		
 		textField_2 = new JTextField();
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		textField_2.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_2.setBackground(Color.LIGHT_GRAY);
 		textField_2.setForeground(Color.WHITE);
 		textField_2.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		gbc_textField_2.fill = GridBagConstraints.BOTH;
 		gbc_textField_2.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 7;
 		choicesEdit.add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
 		
 		JLabel lblI = new JLabel("I : ");
 		lblI.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblI = new GridBagConstraints();
 		gbc_lblI.anchor = GridBagConstraints.WEST;
 		gbc_lblI.fill = GridBagConstraints.VERTICAL;
 		gbc_lblI.insets = new Insets(0, 0, 0, 5);
 		gbc_lblI.gridx = 0;
 		gbc_lblI.gridy = 8;
 		choicesEdit.add(lblI, gbc_lblI);
 		
 		
 		lblAntwortmglichkeit = new JLabel("Antwortm\u00F6glichkeiten:    ");
 		lblAntwortmglichkeit.setOpaque(true);
 		lblAntwortmglichkeit.setBackground(UICOLOR);
 		lblAntwortmglichkeit.setBorder(new LineBorder(UICOLOR, 7));
 		lblAntwortmglichkeit.setForeground(Color.WHITE);
 		lblAntwortmglichkeit.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		sl_editPanel.putConstraint(SpringLayout.WEST, lblAntwortmglichkeit, 10, SpringLayout.WEST, editPanel);
 		sl_editPanel.putConstraint(SpringLayout.NORTH, choicesEdit, 14, SpringLayout.SOUTH, lblAntwortmglichkeit);
 		
 		textField_9 = new JTextField();
 		GridBagConstraints gbc_textField_9 = new GridBagConstraints();
 		gbc_textField_9.fill = GridBagConstraints.BOTH;
 		textField_9.setFont(new Font("Helvetica", Font.PLAIN, 20));
 		textField_9.setBackground(Color.LIGHT_GRAY);
 		textField_9.setForeground(Color.WHITE);
 		textField_9.setBorder(new LineBorder(new Color(192, 192, 192), 4));
 		gbc_textField_9.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_9.gridx = 1;
 		gbc_textField_9.gridy = 8;
 		choicesEdit.add(textField_9, gbc_textField_9);
 		textField_9.setColumns(10);
 		editPanel.add(lblAntwortmglichkeit);
 		
 		questionTextEdit = new JTextArea();
 		sl_editPanel.putConstraint(SpringLayout.NORTH, lblAntwortmglichkeit, 10, SpringLayout.SOUTH, questionTextEdit);
 		sl_editPanel.putConstraint(SpringLayout.NORTH, questionTextEdit, 10, SpringLayout.SOUTH, choicesLbl);
 		sl_editPanel.putConstraint(SpringLayout.WEST, questionTextEdit, 0, SpringLayout.WEST, questionTypeEdit);
 		sl_editPanel.putConstraint(SpringLayout.EAST, questionTextEdit, 0, SpringLayout.EAST, questionTypeEdit);
 		questionTextEdit.setLineWrap(true);
 		questionTextEdit.setWrapStyleWord(true);
 		sl_editPanel.putConstraint(SpringLayout.EAST, choicesEdit, 0, SpringLayout.EAST, questionTextEdit);
 		questionTextEdit.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				int selectedIndex = questions.getSelectedIndex();
 				FeebaCore.currentSurvey.getQuestions().get(selectedIndex).setQuestionText(questionTextEdit.getText().toString());
 				fillPreviewFields(selectedIndex,questionName,questionText,questionChoices);
 			}
 		});
 		questionTextEdit.setForeground(Color.WHITE);
 		questionTextEdit.setFont(new Font("Helvetica", Font.PLAIN, 16));
 		questionTextEdit.setBackground(Color.LIGHT_GRAY);
 		questionTextEdit.setBorder(new LineBorder(Color.LIGHT_GRAY, 6));
 		questionTextEdit.setRows(3);
 		editPanel.add(questionTextEdit);
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setOpaque(false);
 		panel_2.setPreferredSize(new Dimension(250, 10));
 		contentPane.add(panel_2, BorderLayout.EAST);
 		SpringLayout sl_panel_2 = new SpringLayout();
 		panel_2.setLayout(sl_panel_2);
 		
 		comboBox = new JComboBox();
 		sl_panel_2.putConstraint(SpringLayout.WEST, comboBox, 10, SpringLayout.WEST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.EAST, comboBox, -52, SpringLayout.EAST, panel_2);
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Kuchendiagramm", "Balkendiagramm", "Radardiagramm"}));
 		comboBox.addActionListener (new ActionListener () {
 		    public void actionPerformed(ActionEvent e) {
 		        EditorController.generateChart(results, questions.getSelectedIndex());}
 		});
 		panel_2.add(comboBox);
 		
 		JLabel lblDiagrammtyp = new JLabel("Diagrammtyp:     ");
 		sl_panel_2.putConstraint(SpringLayout.NORTH, comboBox, 50, SpringLayout.NORTH, lblDiagrammtyp);
 		sl_panel_2.putConstraint(SpringLayout.NORTH, lblDiagrammtyp, 50, SpringLayout.NORTH, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.WEST, lblDiagrammtyp, 0, SpringLayout.WEST, comboBox);
 		lblDiagrammtyp.setOpaque(true);
 		lblDiagrammtyp.setForeground(Color.WHITE);
 		lblDiagrammtyp.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		lblDiagrammtyp.setBorder(new LineBorder(UICOLOR, 7));
 		lblDiagrammtyp.setBackground(new Color(23, 116, 143));
 		panel_2.add(lblDiagrammtyp);
 		
 		JButton btnNewButton_1 = new JButton("Daten zur\u00FCcksetzen");
 		btnNewButton_1.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				EditorController.resetResults(questions.getSelectedIndex());
 				EditorController.generateChart(results, questions.getSelectedIndex());
 			}
 		});
 		sl_panel_2.putConstraint(SpringLayout.WEST, btnNewButton_1, 10, SpringLayout.WEST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.EAST, btnNewButton_1, 240, SpringLayout.WEST, panel_2);
 		panel_2.add(btnNewButton_1);
 		
 		JLabel lblDiagrammaktionen = new JLabel("Diagrammaktionen:     ");
 		sl_panel_2.putConstraint(SpringLayout.NORTH, btnNewButton_1, 50, SpringLayout.NORTH, lblDiagrammaktionen);
 		sl_panel_2.putConstraint(SpringLayout.NORTH, lblDiagrammaktionen, 50, SpringLayout.NORTH, comboBox);
 		sl_panel_2.putConstraint(SpringLayout.WEST, lblDiagrammaktionen, 0, SpringLayout.WEST, comboBox);
 		lblDiagrammaktionen.setOpaque(true);
 		lblDiagrammaktionen.setForeground(Color.WHITE);
 		lblDiagrammaktionen.setFont(new Font("Helvetica", Font.PLAIN, 15));
 		lblDiagrammaktionen.setBorder(new LineBorder(UICOLOR, 7));
 		lblDiagrammaktionen.setBackground(new Color(23, 116, 143));
 		panel_2.add(lblDiagrammaktionen);
 		
 		JButton btnDiagrammAlsBild = new JButton("Diagramm als Bild speichern...");
 		sl_panel_2.putConstraint(SpringLayout.NORTH, btnDiagrammAlsBild, 40, SpringLayout.NORTH, btnNewButton_1);
 		btnDiagrammAlsBild.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				
 				EditorController.saveChartImage((JLabel) results.getComponents()[0], questions.getSelectedIndex());
 			}
 		});
 		sl_panel_2.putConstraint(SpringLayout.WEST, btnDiagrammAlsBild, 0, SpringLayout.WEST, comboBox);
 		panel_2.add(btnDiagrammAlsBild);
 		
 	}
 
 	public static void openFileChooser() {
 		
 		final JFileChooser chooser = new JFileChooser("Fragebogen laden"); 
         chooser.setDialogType(JFileChooser.OPEN_DIALOG); 
         chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); 
         
         chooser.addChoosableFileFilter(new FileFilter() {
             public boolean accept(File f) {
               if (f.isDirectory()) return true;
               return f.getName().toLowerCase().endsWith(".feeba");
             }
             public String getDescription () { return "Feeba Fragebgen (*.feeba)"; }  
           });
           chooser.setMultiSelectionEnabled(false);
 
         chooser.setVisible(true); 
         
         final int result = chooser.showOpenDialog(null); 
 
         if (result == JFileChooser.APPROVE_OPTION) { 
             tabbedPane.setVisible(true);
             questionWrapper.setVisible(true);
             File inputFile = chooser.getSelectedFile(); 
             String inputDir = inputFile.getPath();
             EditorController.loadSurvey(inputDir,questions,backgroundPreview);
             editPanel.setVisible(true);
             
         } 
 			
 	}
 
 	private void saveFileChoser() {
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setSelectedFile(new File(FeebaCore.currentSurvey.getName()+".feeba"));
         chooser.setDialogTitle("Speichern unter...");
         chooser.setDialogType(JFileChooser.SAVE_DIALOG);
         chooser.addChoosableFileFilter(new FileFilter() {
             public boolean accept(File f) {
                 if (f.isDirectory())
                     return true;
                 return f.getName().toLowerCase().endsWith(".feeba");
             }
 
             public String getDescription() {
                 return "Feeba Fragebogen (*.feeba)";
             }
         });
         
       chooser.setVisible(true); 
         
         final int result = chooser.showSaveDialog(null); 
 
         if (result == JFileChooser.APPROVE_OPTION) { 
             File saveFile = chooser.getSelectedFile(); 
             String saveDir = saveFile.getPath(); 
             EditorController.saveSurvey(saveDir);
             
         } 
 		
 	}
 	
 	
 	private void fillPreviewFields(int selectedIndex, JLabel name, JLabel text, JLabel answers) {
 		
 
 		Question ques = FeebaCore.currentSurvey.getQuestions().get(selectedIndex);
 		name.setText("Frage " + (questions.getSelectedIndex()+1) + " - " + ques.getName());
 		text.setText(ques.getQuestionText());
 		answers.setText(ques.getChoicesText());
 		
 	}
 	
 	private void fillEditFields(int selectedIndex, JTextField questionNameEdit,
 			JTextArea questionTextEdit, JComboBox questionTypeEdit) {
 		
 		Question ques = FeebaCore.currentSurvey.getQuestions().get(selectedIndex);
 		questionNameEdit.setText(ques.getName());
 		questionTextEdit.setText(ques.getQuestionText());
 		questionTypeEdit.setSelectedItem(ques.getType());
 		toggleChoices();
 		
 	}
 
 	
 	
 	public void toggleChoices() {
 		JComboBox jcb = getQuestionTypeEdit();
 		if(jcb.getSelectedItem().equals(QuestionType.FREETEXT)) {
 			
 			lblAntwortmglichkeit.setVisible(false);
 			choicesEdit.setVisible(false);
 		}
 		else {
 			
 			lblAntwortmglichkeit.setVisible(true);
 			choicesEdit.setVisible(true);
 			
 		}
 		
 	}
 	
 	
 	protected JComboBox getQuestionTypeEdit() {
 		return questionTypeEdit;
 	}
 	public JLabel getLblAntwortmglichkeit() {
 		return lblAntwortmglichkeit;
 	}
 	public JPanel getChoicesEdit() {
 		return choicesEdit;
 	}
 }
