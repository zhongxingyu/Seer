 package net.renalias.xmlvalidator.ui.components;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rtextarea.SearchEngine;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class SearchToolBar extends JToolBar implements ActionListener {
 
 	private JTextField searchField;
 	private JCheckBox regexCB;
 	private JCheckBox matchCaseCB;
 	RSyntaxTextArea textArea;
 
 	public SearchToolBar(RSyntaxTextArea textArea) {
 		super();
 		this.textArea = textArea;
 		build();
 	}
 
 	public void build() {
 		// Create a searchToolbar with searching options.
 		searchField = new JTextField(30);
 		searchField.addKeyListener(new KeyListener() {
 			public void handleKeyPress(KeyEvent keyEvent) {
 				if(searchField.getText().length() > 1) {
 					textArea.markAll(searchField.getText(), matchCaseCB.isSelected(), false, regexCB.isSelected());
 					System.out.println(keyEvent);
 				}
 				else
 					textArea.clearMarkAllHighlights();
 			}
 
 			public void keyPressed(KeyEvent keyEvent) {}
 			public void keyTyped(KeyEvent keyEvent) {}
 			public void keyReleased(KeyEvent keyEvent) {
 				handleKeyPress(keyEvent);
 			}
 		});
 
 		add(searchField);
 		JButton b = new JButton("Find Next");
 		b.setActionCommand("FindNext");
 		b.addActionListener(this);
 		add(b);
 		b = new JButton("Find Previous");
 		b.setActionCommand("FindPrev");
 		b.addActionListener(this);
 		add(b);
 		regexCB = new JCheckBox("Regex");
 		add(regexCB);
 		matchCaseCB = new JCheckBox("Match Case");
 		add(matchCaseCB);
 
 		setFloatable(false);
 		setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 
 		if ("FindNext".equals(command)) {
 			String text = searchField.getText();
 			if (text.length() == 0) {
 				return;
 			}
 			boolean forward = true;
 			boolean matchCase = matchCaseCB.isSelected();
 			boolean wholeWord = false;
 			boolean regex = regexCB.isSelected();
 			boolean found = SearchEngine.find(textArea, text, forward, matchCase, wholeWord, regex);
 
			if (!found) {
				JOptionPane.showMessageDialog(this, "Text not found");
			}
 		} else if ("FindPrev".equals(command)) {
 			String text = searchField.getText();
 			if (text.length() == 0) {
 				return;
 			}
 			boolean forward = false;
 			boolean matchCase = matchCaseCB.isSelected();
 			boolean wholeWord = false;
 			boolean regex = regexCB.isSelected();
 			boolean found = SearchEngine.find(textArea, text, forward, matchCase, wholeWord, regex);
			if (!found) {
				JOptionPane.showMessageDialog(this, "Text not found");
			}
 		}
 	}
 }
