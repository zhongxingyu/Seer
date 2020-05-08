 package GUI;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.text.html.HTML.Tag;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import Commands.CutCommand;
 import Commands.IndentCommand;
 import Commands.InsertCommand;
 import Commands.PasteCommand;
 import Commands.SaveAsCommand;
 import Commands.SaveCommand;
 import Commands.ToggleIndentCommand;
 import Commands.ToggleWrapCommand;
 import Commands.UndoCommand;
 import Entities.DocumentMenu;
 import Entities.TheDocument;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 
 /**
  * GUI for each document
  * @author Chris Timmons
  * @author Ben Kantor (bdk3079@rit.edu)
  */
 @SuppressWarnings("serial")
 public class DocumentGUI extends JPanel{
 	
 	private TheDocument thisDoc;
 	private DocumentMenu docMenu;
 	private LinkView linkView;
 	private TreeView treeView;
 	
 	private JPanel menuPanel,insertPanel,docMenuPanel,settingsPanel; 
 	private JPanel indentPanel,wordWrapPanel,linkViewPanel;
 	private JButton linkViewBtn, treeViewBtn;
 	private JRadioButton wordWrapOn,wordWrapOff,indentOn,indentOff;
 	private JLabel wordWrapLabel = new JLabel("Word-Wrap:");
 	private JLabel indentLabel = new JLabel("Auto-Indent:");
 	private final JTextArea textArea = new JTextArea();
 	
 	private JMenu documentMenu;
 	private JMenuItem save,saveAs,cut,paste,undo;
 	
 	private int indentSize = 5;
 	private JSpinner indentChanger;
 	
 	/**
 	 * Constructor for the document GUI
 	 * @param docParam TheDocument that is being visually represented
 	 */
 	public DocumentGUI(TheDocument docParam) {		
 		thisDoc = docParam;
 		docMenu = createDocumentMenu(thisDoc);
 		
 		DocumentMenuListener docMenuListener = new DocumentMenuListener();
 		this.setLayout(new BorderLayout());
 		
 		//Tab-Size Changer
 		JPanel tabSizePanel = new JPanel();
 		JLabel tabSizeLabel = new JLabel("Indent-Size");
 		indentChanger = new JSpinner();
 		indentChanger.setValue(indentSize);
 		tabSizePanel.add(tabSizeLabel,BorderLayout.WEST);
 		tabSizePanel.add(indentChanger,BorderLayout.CENTER);
 		
 		//JMenu
 		documentMenu = new JMenu("Document");
 		save = new JMenuItem("Save");
 		saveAs = new JMenuItem("Save As");
 		cut = new JMenuItem("Cut");
 		paste = new JMenuItem("Paste");
 		undo = new JMenuItem("Undo");
 		documentMenu.add(documentMenu);
 		documentMenu.add(save);
 		documentMenu.add(saveAs);
 		documentMenu.add(cut);
 		documentMenu.add(paste);
 		documentMenu.add(undo);
 		
 		
 		//Create Panels
 		menuPanel = new JPanel();
 		insertPanel = new JPanel(new BorderLayout());
 		indentPanel = new JPanel(new GridLayout(1,3));
 		wordWrapPanel = new JPanel(new GridLayout(1, 3));
 		docMenuPanel = new JPanel(new GridLayout(1,2));
 		docMenuPanel.setMaximumSize(new Dimension(150,75));
 		settingsPanel = new JPanel(new GridLayout(4,1));
 		linkViewPanel = new JPanel (new GridLayout(1,2));
 		
 		
 		//Create Buttons
 		linkViewBtn = new JButton("Toggle Link View");
 		treeViewBtn = new JButton ("Toggle Outline Mode");		
 		wordWrapOn = new JRadioButton("On ", true);
 		wordWrapOff = new JRadioButton("Off ");
 		indentOn = new JRadioButton("On", true);
 		indentOff = new JRadioButton("Off");
 		
 		//Insert Panel
 		SimpleTagButton paraBtn,boldBtn,italBtn,listItemBtn,tblBtn,
 						tblRowBtn,tblDataBtn,tblHeadBtn,headerBtn,
 						linkBtn,imgBtn;
 		paraBtn = new SimpleTagButton(textArea,"Paragraph",Tag.P,thisDoc);
 		boldBtn = new SimpleTagButton(textArea,"Bold",Tag.B,thisDoc);
 		italBtn = new SimpleTagButton(textArea,"Italic",Tag.I,thisDoc);
 		listItemBtn = new SimpleTagButton(textArea,"List",Tag.OL,thisDoc);
 		tblRowBtn = new SimpleTagButton(textArea,"Table Row",Tag.TR,thisDoc);
 		tblDataBtn = new SimpleTagButton(textArea,"Table Data",Tag.TD,thisDoc);
 		tblHeadBtn = new SimpleTagButton(textArea,"Table Header",Tag.TH,thisDoc);
 		headerBtn = new SimpleTagButton(textArea,"Header",Tag.H1,thisDoc);
 		tblBtn = new SimpleTagButton(textArea,"Table",Tag.TABLE,thisDoc);
 		linkBtn = new SimpleTagButton(textArea,"Link",Tag.A,thisDoc);
 		imgBtn = new SimpleTagButton(textArea,"Image",Tag.IMG,thisDoc);
 		
 		
 		//Add Listeners
 		save.addActionListener(docMenuListener);
 		saveAs.addActionListener(docMenuListener);
 		cut.addActionListener(docMenuListener);
 		paste.addActionListener(docMenuListener);
 		indentOn.addActionListener(docMenuListener);
 		indentOff.addActionListener(docMenuListener);
 		wordWrapOn.addActionListener(docMenuListener);
 		wordWrapOff.addActionListener(docMenuListener);
 		undo.addActionListener(docMenuListener);
 		
 		linkViewBtn.addActionListener(new LinkViewListener());
 		treeViewBtn.addActionListener(new TreeViewListener());
 		
 
 		//Define WordWrapPanel		
 		wordWrapPanel.add(wordWrapLabel);
 		wordWrapPanel.add(wordWrapOn);
 		wordWrapPanel.add(wordWrapOff);
 		ButtonGroup wordWrapGroup = new ButtonGroup();
 		wordWrapGroup.add(wordWrapOn);
 		wordWrapGroup.add(wordWrapOff);
 		
 		//Define IndentWrapPanel		
 		indentPanel.add(indentLabel);
 		indentPanel.add(indentOn);
 		indentPanel.add(indentOff);
 		ButtonGroup indentGroup = new ButtonGroup();
 		indentGroup.add(indentOn);
 		indentGroup.add(indentOff);
 		
 		//Define LinkViewPanel
 		linkViewPanel.add(linkViewBtn);
 		linkViewPanel.add(treeViewBtn);
 		
 		//Define MenuPanel
 		menuPanel.setLayout(new GridLayout(1,2));
 		settingsPanel.add(indentPanel);
 		settingsPanel.add(wordWrapPanel);
 		settingsPanel.add(tabSizePanel);
 		settingsPanel.add(linkViewPanel);
 		menuPanel.add(settingsPanel);
 		
 		//Define Insert Panel
 		//JLabel insertLabel = new JLabel("Insert");
 		JPanel insertButtons = new JPanel(new GridLayout(3,6));
 		
 		insertButtons.add(paraBtn);
 		insertButtons.add(italBtn);
 		insertButtons.add(boldBtn);
 		insertButtons.add(listItemBtn);
 		insertButtons.add(tblRowBtn);
 		insertButtons.add(tblDataBtn);
 		insertButtons.add(tblHeadBtn);
 		insertButtons.add(headerBtn);
 		insertButtons.add(tblBtn);
 		insertButtons.add(linkBtn);
 		insertButtons.add(imgBtn);
 		JScrollPane buttonScrollPane = new JScrollPane(insertButtons);
 		buttonScrollPane.setPreferredSize(new Dimension(50,100));
 		insertPanel.add(buttonScrollPane,BorderLayout.NORTH);
 		insertPanel.setPreferredSize(new Dimension(150,100));
 		
 		//Define docMenuPanel
 		docMenuPanel.add(menuPanel);
 		docMenuPanel.add(insertPanel);
 		
		
		
 		//Setup Main Panel
 		
 		textArea.setEditable(true);
 		textArea.setText(thisDoc.getXml());
 		textArea.setTabSize(5);
 		textArea.registerKeyboardAction(new AutoIndentAction(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 		JScrollPane textPane = new JScrollPane(textArea);
 		textArea.addKeyListener(new mementoListener());
		
 		add(docMenuPanel,BorderLayout.NORTH);
 		add(textPane,BorderLayout.CENTER);
 		
 		indentChanger.addChangeListener(new ChangeListener(){
 
             @Override
             public void stateChanged(ChangeEvent arg0) {
                 int tab = (Integer) indentChanger.getValue();
                 textArea.setTabSize(tab);
                 
             }
             
         });
 	}
 	
 	public JMenu getDocMenu(){
 		return documentMenu;
 	}
 	
 	/**
 	 * creates a doc menu for the document
 	 * @param theDocument
 	 * @return
 	 */
 	private DocumentMenu createDocumentMenu(TheDocument theDocument){
 		CutCommand cut = new CutCommand(theDocument);
 		PasteCommand paste = new PasteCommand(theDocument);
 		InsertCommand insert = new InsertCommand(theDocument);
 		IndentCommand indent = new IndentCommand(theDocument);
 		SaveCommand save = new SaveCommand(theDocument);
 		SaveAsCommand saveAs = new SaveAsCommand(theDocument);
 		ToggleWrapCommand toggleWrap = new ToggleWrapCommand(theDocument);
 		ToggleIndentCommand toggleIndent = new ToggleIndentCommand(theDocument);
 		UndoCommand undo = new UndoCommand(theDocument);
 		DocumentMenu docMenu = new DocumentMenu(cut,paste,insert,indent,save,saveAs,toggleWrap,toggleIndent,undo);
 		return docMenu;
 	}
 	
 
 	/**
 	 * Gets the text in the JTextArea
 	 * @return the text
 	 */
 	public String getText() {
 		return textArea.getText();
 	}
 	
 	/**
 	 * listener for each of the document buttons
 	 * @author Chris Timmons
 	 *
 	 */
 	private class DocumentMenuListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String action = e.getActionCommand();
 			if(action == "Save"){
 			    try {
                     thisDoc.getReader().quickParse(textArea.getText());
                 } catch (Exception ex){
                     thisDoc.setWellFormed(false);
                 }
 			    
 			    if(thisDoc.isWellFormed() == true){
 			        thisDoc.save(textArea.getText());
 			    }
 			    else{
 			        int dialogButton = JOptionPane.YES_NO_OPTION;
 	                int result = JOptionPane.showConfirmDialog (null, "This document is not well formed. To save anyway, click YES. For auto-fix, click NO.","Warning",dialogButton);
 
 			        if (result == JOptionPane.YES_OPTION){
 			            thisDoc.save(textArea.getText());
 			        }
 			        else{
 			            try {
                             thisDoc.getReader().parseAndPretty(textArea.getText(),"2");
                         } catch (Exception e1){}
 			            textArea.setText(thisDoc.getXml());
 			            thisDoc.setWellFormed(true);
 			        }
 			    }
 			} else if(action == "SaveAs"){
 			    try {
                     thisDoc.getReader().quickParse(textArea.getText());
                 } catch (Exception ex){
                     thisDoc.setWellFormed(false);
                 }
                 if(thisDoc.isWellFormed() == true){
                     thisDoc.save(textArea.getText());
                 }
                 else{
                     int dialogButton = JOptionPane.YES_NO_OPTION;
                     int result = JOptionPane.showConfirmDialog (null, "This document is not well formed. To save anyway, click YES. For auto-fix, click NO.","Warning",dialogButton);
 
                     if (result == JOptionPane.YES_OPTION){
                         thisDoc.save(textArea.getText());
                     }
                     else{
                         try {
                             thisDoc.getReader().parseAndPretty(textArea.getText(),"2");
                         } catch (Exception e1){}
                         textArea.setText(thisDoc.getXml());
                         thisDoc.setWellFormed(true);
                     }
                 }
 			}else if(action == "Undo"){
 			    docMenu.undo();
 			    textArea.setText(thisDoc.getXml());
 			} else if(action == "Copy"){	    
 			} else if(action == "Cut"){
 				docMenu.cut();
 			} else if(action == "Paste"){
 				docMenu.paste();
 			} else if (action == "On "){
				docMenu.toggleWrap();
 			} else if (action == "Off "){
				docMenu.toggleWrap();
 			} else if (action == "On"){
 			    textArea.registerKeyboardAction(new AutoIndentAction(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 			} else if (action == "Off"){
 			    textArea.registerKeyboardAction(new AutoIndentAction(false), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 			}
 		}		
 	}
 	
 	private class TreeViewListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			 if ((treeView == null) || (!treeView.isShowing())) {
  		        DefaultMutableTreeNode tree = null;
                  
                      try {
                          tree = thisDoc.buildTree(textArea.getText());
                      } catch (Exception ex){
                          System.err.print("Parsing failed");
                      }
                 
                  if(thisDoc.isWellFormed() == true){
                      treeView = new TreeView(DocumentGUI.this, "Tree View: " + thisDoc.getName(), tree );
                      add(treeView,BorderLayout.WEST);
                      revalidate();
                      repaint();
                  }
                  else{                      
                      JFrame frame = new JFrame("Cannot Create Tree.");
                      JOptionPane.showMessageDialog(frame, "Document is not well-formed. Cannot create Tree.","Inane error",JOptionPane.ERROR_MESSAGE);
                  }
                  
              } else{
              	remove(treeView);
              	revalidate();
              	repaint();
              }			
 		}
 		
 	}
 	
 	private class LinkViewListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if ((linkView == null) || (!linkView.isShowing())) {
 				linkView = new LinkView(DocumentGUI.this, "Link View: " + thisDoc.getName());
 				add(linkView, BorderLayout.EAST);
 				revalidate();
 				repaint();
 			} else{
 				remove(linkView);
 				revalidate();
 				repaint();
 			}
 			
 		}
 		
 	}
 	
 	private class mementoListener implements KeyListener{
 
 	    int count = 0;
         @Override
        public void keyPressed(KeyEvent arg0) {
        }

         @Override
         public void keyReleased(KeyEvent arg0) {
            count++;
            if(count == 5){
                count = 0;
                thisDoc.setXml(textArea.getText());
                thisDoc.createMomento();
                thisDoc.getCareTaker().storeState(thisDoc.getMomento());
            }  
         }

         @Override
        public void keyTyped(KeyEvent arg0) {
            // TODO Auto-generated method stub
            
        }

  
	    
 	}
 		
 }
