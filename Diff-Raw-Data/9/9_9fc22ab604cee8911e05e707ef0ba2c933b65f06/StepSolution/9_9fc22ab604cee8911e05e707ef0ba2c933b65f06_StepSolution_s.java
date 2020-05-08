 package frontend.panels;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 
 import backend.blocks.Countable;
 import frontend.swing.AppFrame;
 import frontend.swing.Button;
 import frontend.swing.CurrentConstants;
 import frontend.swing.DialogStringListener;
 import frontend.swing.ScrollPane;
 import frontend.swing.StringDialog;
 
 public class StepSolution extends JPanel {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1780266591415441861L;
 	private Saved _savePanel;
 	private Solution _display, _answer, _comp;
 	private List<String> _solList, _compList, _exportList;
 	private int _stepNumber = 0;
 	private ScrollPane _scroll;
 	private JButton _forwardButton, _backButton;
 	private Countable _ans;
 	private JPanel _bottomBar;
 	private JLabel _stepNumberLabel;
 	private JTabbedPane _tabbedPane;
 	private JPanel _cards, _buttons;
 	private AppFrame _frame;
 	
 	public StepSolution(AppFrame frame){
 		super();
 		_frame = frame;
 		this.setLayout(new BorderLayout());
 		this.setBackground(CurrentConstants.STEPSOLUTION_BG);
 		this.setBorder(CurrentConstants.STEPSOLUTION_BORDER);
 		this.setPreferredSize(new Dimension(100,100));
 
 		_solList = new ArrayList<>();
 		
 		//main display
 		_display = new Solution("\\text{Solutions will be displayed here}");
 //		_display.addMouseListener(new Listener(this));
 		_scroll = new ScrollPane(_display);
 		_scroll.getVerticalScrollBar().setUnitIncrement(20);
 		_scroll.setBorder(CurrentConstants.STEPSOLUTION_SCROLL_BORDER);
 		_scroll.setBackground(CurrentConstants.STEPSOLUTION_SCROLL_BG);
 		
 		//bottom bar
 		_bottomBar = new JPanel(new BorderLayout());
 		_bottomBar.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBAR_BORDER);
 		_bottomBar.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBAR_BG);
 		
 		//bottom bar left
 		JPanel bottomBarLeft = new JPanel(new BorderLayout());
 		
 		JPanel bottomBarLeftUp = new JPanel();
 		bottomBarLeftUp.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFTUP_BORDER);
 		bottomBarLeftUp.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFTUP_BG);
 		_stepNumberLabel = new JLabel("");
 		_stepNumberLabel.setBorder(CurrentConstants.STEPSOLUTION_STEPNUMBERLABEL_BORDER);
 		_stepNumberLabel.setBackground(CurrentConstants.STEPSOLUTION_STEPNUMBERLABEL_BG);
 		_stepNumberLabel.setForeground(CurrentConstants.STEPSOLUTION_STEPNUMBERLABEL_FG);
 		_stepNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		_stepNumberLabel.setFont(CurrentConstants.STEPSOLUTION_STEPNUMBERLABEL_FONT);
 		bottomBarLeft.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFT_BG);
 		bottomBarLeft.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFT_BORDER);
 		_comp = new Solution(75,80);
 		_comp.setMargins(5, 5);
 		_comp.setUpperLimit(30);
 		_comp.setBorder(CurrentConstants.STEPSOLUTION_COMP_BORDER);
 		_comp.setBackground(CurrentConstants.STEPSOLUTION_COMP_BG);
 		ScrollPane compScroll = new ScrollPane(_comp);
 		compScroll.setBackground(CurrentConstants.STEPSOLUTION_COMPSCROLL_BG);
 		compScroll.setBorder(CurrentConstants.STEPSOLUTION_COMPSCROLL_BORDER);
 		compScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 		bottomBarLeft.add(compScroll, BorderLayout.CENTER);
 		_forwardButton = new Button("\u25ba", CurrentConstants.BUTTON_NEXT_BG, CurrentConstants.BUTTON_NEXT_FG, CurrentConstants.BUTTON_NEXT_HOVER_BG, CurrentConstants.BUTTON_NEXT_HOVER_FG, CurrentConstants.BUTTON_NEXT_PRESSED_BG, CurrentConstants.BUTTON_NEXT_PRESSED_FG, CurrentConstants.BUTTON_NEXT_BORDER);
 		_backButton = new Button("\u25c4", CurrentConstants.BUTTON_NEXT_BG, CurrentConstants.BUTTON_NEXT_FG, CurrentConstants.BUTTON_NEXT_HOVER_BG, CurrentConstants.BUTTON_NEXT_HOVER_FG, CurrentConstants.BUTTON_NEXT_PRESSED_BG, CurrentConstants.BUTTON_NEXT_PRESSED_FG, CurrentConstants.BUTTON_NEXT_BORDER);
 		_forwardButton.setFont(CurrentConstants.BUTTON_NEXT_FONT);
 		_backButton.setFont(CurrentConstants.BUTTON_NEXT_FONT);
 		_forwardButton.addActionListener(new ForwardBack(this, true));
 		_backButton.addActionListener(new ForwardBack(this, false));
 		bottomBarLeftUp.add(_backButton);
 		bottomBarLeftUp.add(_stepNumberLabel);
 		bottomBarLeftUp.add(_forwardButton);
 		bottomBarLeft.add(bottomBarLeftUp, BorderLayout.NORTH);
 		
 		JPanel bottomBarRight = new JPanel(new BorderLayout());
 		final JPanel bottomBarRightUp = new JPanel(new BorderLayout());
 		bottomBarRightUp.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBARRIGHTUP_BG);
 		bottomBarRightUp.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBARRIGHTUP_BORDER);
 		
 		final JLabel answerLabel = new JLabel("Final Answer");
 		answerLabel.setBorder(CurrentConstants.STEPSOLUTION_ANSWERLABEL_BORDER);
 		answerLabel.setBackground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_BG);
 		answerLabel.setForeground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_FG);
 		answerLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		answerLabel.setFont(CurrentConstants.STEPSOLUTION_ANSWERLABEL_FONT);
 		
 
 		bottomBarRightUp.add(answerLabel);
 		bottomBarRight.add(bottomBarRightUp, BorderLayout.NORTH);
 
 		_answer = new Solution(90, 90);
 		_answer.setMargins(5, 5);
 		_answer.setToolTipText("Double click to save answer");
 		_answer.setBorder(CurrentConstants.STEPSOLUTION_ANSWER_BORDER);
 		_answer.setBackground(CurrentConstants.STEPSOLUTION_ANSWER_BG);
 		_answer.setPreferredSize(new Dimension(0,90));
 		
 		_answer.addMouseListener(new MouseAdapter() {
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 				_answer.setBackground(CurrentConstants.STEPSOLUTION_ANSWER_HOVER_BG);
 			}
 			
 			public void mouseClicked(MouseEvent e) {
 				CardLayout cl = (CardLayout) (_cards.getLayout());
 				cl.last(_cards);
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				_answer.setBackground(CurrentConstants.STEPSOLUTION_ANSWER_BG);
 			}
 			
 		});
 		
 		_buttons = new JPanel(new BorderLayout());
 		_buttons.setBorder(CurrentConstants.STEPSOLUTION_ANSWER_BORDER);
 		_buttons.setBackground(CurrentConstants.STEPSOLUTION_ANSWER_BG);
 		
 		
 		Button saveButton = new Button("Save", CurrentConstants.SAVE_BUTTON_BG, CurrentConstants.SAVE_BUTTON_FG, CurrentConstants.SAVE_BUTTON_HOVER_BG, CurrentConstants.SAVE_BUTTON_HOVER_FG, CurrentConstants.SAVE_BUTTON_PRESSED_BG, CurrentConstants.SAVE_BUTTON_PRESSED_FG, CurrentConstants.SAVE_BUTTON_BORDER);
 		saveButton.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				if(_ans != null){
 					_savePanel.addCountable(_ans);
 					CardLayout cl = (CardLayout) (_cards.getLayout());
 					cl.first(_cards);
 				}
 			}
 		});
 		_buttons.add(saveButton, BorderLayout.NORTH);
 		
 		Button saveAsButton = new Button("Save As", CurrentConstants.SAVEAS_BUTTON_BG, CurrentConstants.SAVEAS_BUTTON_FG, CurrentConstants.SAVEAS_BUTTON_HOVER_BG, CurrentConstants.SAVEAS_BUTTON_HOVER_FG, CurrentConstants.SAVEAS_BUTTON_PRESSED_BG, CurrentConstants.SAVEAS_BUTTON_PRESSED_FG, CurrentConstants.SAVEAS_BUTTON_BORDER);
 		saveAsButton.addActionListener(new SaveAsListener());
 		_buttons.add(saveAsButton, BorderLayout.CENTER);
 		
 		Button exportButton = new Button("Export to PDF", CurrentConstants.EXPORT_BUTTON_BG, CurrentConstants.EXPORT_BUTTON_FG, CurrentConstants.EXPORT_BUTTON_HOVER_BG, CurrentConstants.EXPORT_BUTTON_HOVER_FG, CurrentConstants.EXPORT_BUTTON_PRESSED_BG, CurrentConstants.EXPORT_BUTTON_PRESSED_FG, CurrentConstants.EXPORT_BUTTON_BORDER);
 		exportButton.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if(_ans != null){
 					JFileChooser fc = new JFileChooser();
 					int ret = fc.showSaveDialog(_buttons);
 					if(ret == JFileChooser.APPROVE_OPTION){
 						File file;
 						if(!fc.getSelectedFile().getAbsolutePath().endsWith(".tex")){
 						    file = new File(fc.getSelectedFile() + ".tex");
 						} else {
 							file = fc.getSelectedFile();
 						}
 						exportPDF(file);
 						CardLayout cl = (CardLayout) (_cards.getLayout());
 						cl.first(_cards);
 					}
 				}
 			}
 		});
 		_buttons.add(exportButton, BorderLayout.SOUTH);
 		
 		_cards = new JPanel(new CardLayout());
 		_cards.add(_answer, "ANSWER");
 		
 		_cards.add(_buttons, "BUTTONS");
 		
 		bottomBarRight.add(_cards, BorderLayout.PAGE_END);
 		
 		_bottomBar.add(bottomBarLeft, BorderLayout.CENTER);
 		_bottomBar.add(bottomBarRight, BorderLayout.EAST);
 		
 		this.add(_scroll, BorderLayout.CENTER);
 		this.add(_bottomBar, BorderLayout.SOUTH);
 		_bottomBar.setVisible(false);
 		
 		checkButtons();
 		
 		answerLabel.addMouseListener(new MouseAdapter() {
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				if (_ans != null) {
 					answerLabel.setBackground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_HOVER_BG);
 					bottomBarRightUp.setBackground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_HOVER_BG);
 					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 				}
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (_ans != null) {
 					CardLayout cl = (CardLayout) (_cards.getLayout());
 					cl.next(_cards);
 				}
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				if (_ans != null) {
 					answerLabel.setBackground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_BG);
 					bottomBarRightUp.setBackground(CurrentConstants.STEPSOLUTION_ANSWERLABEL_BG);
 					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				}
 			}
 			
 		});
 		
 		this.revalidate();
 	}
 	
 	public void setTabbedPane(JTabbedPane tabbedPane) {
 		_tabbedPane = tabbedPane;
 	}
 	
 	public void exportPDF(File file){
 		if(_solList.size() != 0){
 			StringBuilder sb = new StringBuilder();
 			sb.append("\\documentclass[12pt,letterpaper]{article}\n");
 			sb.append("\\usepackage{amsmath,amsthm,amsfonts,amssymb,amscd}\n");
 			sb.append("\\setlength{\\parindent}{0.0in}");
 			sb.append("\\begin{document}\n");
 //			sb.append("\\begin{align}\n");
 			for(String ls : _exportList){
 				sb.append(ls + "\n");
 			}
 //			sb.append("\\end{align}\n");
 			sb.append("\\end{document}");
 			
 			FileWriter fw;
 			try {
 				fw = new FileWriter(file.getAbsoluteFile());
 				BufferedWriter bw = new BufferedWriter(fw);
 				bw.write(sb.toString());
 				bw.close();
 				fw.close();
 				Runtime.getRuntime().exec("pdflatex " + "--output-directory " + file.getParent() + " " + file.getAbsolutePath());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	
 	public void setSavedPanel(Saved savePanel){
 		_savePanel = savePanel;
 	}
 	
 	public void clear(){
 		_bottomBar.setVisible(false);
 		_display.setTex("\\text{Solutions will be displayed here}");
 		_comp.setTex("");
 		_answer.setTex("");
 		_stepNumber = 0;
 		_tabbedPane.setTitleAt(0, "Solution");
 		_solList.clear();
 		_ans = null;
 		checkButtons();
 	}
 	
 	public void setSolution(List<List<String>> steps, Countable answer){
 		_bottomBar.setVisible(true);
 		_solList = steps.get(1);
 		_compList = steps.get(0);
 		_exportList = steps.get(2);
 		_stepNumber = 0;
 		setStepNumbers();
 		_display.setTex(_solList.get(0));
 		_comp.setTex(_compList.get(0));
 		_answer.setTex(answer.toLatex());
 		_ans = answer;
 		checkButtons();
 		this.revalidate();
 		this.repaint();
 	}
 	
 	public void setError(String error){
 		clear();
 		_display.setTex(error);
 	}
 	
 	private void setStepNumbers() {
 		_stepNumberLabel.setText("<html>Step <font color=#BFD9F2>" + (_stepNumber + 1) + "</font> of " + _solList.size());
 		_tabbedPane.setTitleAt(0, "<html>Step <font color=#BFD9F2>" + (_stepNumber + 1) + "</font> of " + _solList.size());
 	}
 	
 	public void next(){
 		if(_stepNumber < _solList.size() - 1){
 			_stepNumber++;
 			_stepNumberLabel.setText("<html>Step <font color=#BFD9F2>" + (_stepNumber + 1) + "</font> of " + _solList.size());
 		}
 		checkButtons();
 		setStepNumbers();
 		_display.setTex(_solList.get(_stepNumber));
 		_comp.setTex(_compList.get(_stepNumber));
 		resetScroll();
 		this.repaint();
 	}
 	
 	public void prev(){
 		if(_stepNumber > 0){
 			_stepNumber--;
 		}
 		checkButtons();
 		setStepNumbers();
 		_display.setTex(_solList.get(_stepNumber));
 		_comp.setTex(_compList.get(_stepNumber));
 		resetScroll();
 		this.repaint();
 	}
 	
 	public void checkButtons(){
 		_backButton.setEnabled(true);
 		_forwardButton.setEnabled(true);
 		if(_stepNumber == 0){
 			_backButton.setEnabled(false);
 		}
 		if(_stepNumber == _solList.size()-1 || _solList.size() == 0){
 			_forwardButton.setEnabled(false);
 		}
 	}
 	
 	public void resetScroll(){
 		_scroll.getVerticalScrollBar().setValue(0);
 		_scroll.getHorizontalScrollBar().setValue(0);
 	}
 	
 	
 	@SuppressWarnings("unused")
 	private class ButtonListener extends MouseAdapter{
 		
 		@Override
 		public void mouseExited(MouseEvent e) {
 			CardLayout cl = (CardLayout) (_cards.getLayout());
 			cl.first(_cards);
 		}
 	}
 	
 	private class SaveAsListener implements DialogStringListener, ActionListener{
 		
 		public SaveAsListener() {
 			
 		}
 		
 		@Override
 		public void doDialogReturn(String s) {
 			_savePanel.addCountable(s, _ans);
 			CardLayout cl = (CardLayout) (_cards.getLayout());
 			cl.first(_cards);
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
			new StringDialog(_frame, this, StepSolution.this, _frame.getUILayer());
 		}
 		
 	}
 
 	
 	@SuppressWarnings("unused")
 	private class ExportListener extends MouseAdapter{
 		public void mouseClicked(MouseEvent e){
 			if(_ans != null){
 				if(e.getClickCount() == 2){
 					_savePanel.addCountable(_ans);
 				}
 			}
 		}
 	}
 	
 	private class ForwardBack implements ActionListener{
 		StepSolution _ss;
 		boolean _forward;
 		
 		public ForwardBack(StepSolution ss, Boolean forward){
 			_ss = ss;
 			_forward = forward; 
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if(_forward){
 				_ss.next();
 			} else {
 				_ss.prev();
 			}
 		}
 	}
 	
 }
