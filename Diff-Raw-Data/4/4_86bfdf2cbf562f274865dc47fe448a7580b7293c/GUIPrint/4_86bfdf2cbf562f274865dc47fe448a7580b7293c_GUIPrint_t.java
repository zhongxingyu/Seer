 package gui;
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 //for textbox?
 import java.awt.TextArea;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowAdapter;
 
 import java.awt.image.BufferedImage;
 
 import org.scilab.forge.jlatexmath.TeXConstants;
 import org.scilab.forge.jlatexmath.TeXFormula;
 import org.scilab.forge.jlatexmath.TeXIcon;
 
 import storage.Saver;
 
 /**
  * GUIPrint
  * Prints all formulas in database. I guess.
  * @author Jonathan Tan
  *
  */
 //Forms = Formulas
 public class GUIPrint extends JPanel{
 
 	private JTextPane output = new JTextPane();
 	private StyledDocument doc = output.getStyledDocument();
 
 	public GUIPrint(){
 		//formatting text pane
 		addStylesToDocument(doc);
 		output.setPreferredSize(new Dimension(706,450));
 
 		//Scroller
 		JScrollPane scroller = new JScrollPane(output);
 		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		String writeBuffer = "";
 
 		//FORMULA HEADER
 		//		writeBuffer ="[FORMULAS] \n";
 		//		try{
 		//			doc.insertString(doc.getLength(),writeBuffer,doc.getStyle("bold"));
 		//			}catch(BadLocationException ble){
 		//				System.err.println("Couldn't insert text into text pane.");
 		//			}
 		output.insertIcon(new ImageIcon("img/formulaheader.png"));
 		//This is retarded.
 		//There has to be a better way to force newlines in a TextPane.
 		//I fucking hate TextPanes.
 		try{
 			doc.insertString(doc.getLength(),"\n",doc.getStyle("regular"));
 		}catch(BadLocationException ble){
 			System.err.println("Couldn't insert text into text pane.");
 		}
 
 		for(int i=0; i<GUIMain.FORMULAS.size();i++) {
 			infoLabel formulaLabel = new infoLabel(GUIMain.FORMULAS.get(i).toLaTeXIcon(),i);
 			output.insertComponent(formulaLabel);
 			formulaLabel.addMouseListener(formulaLabel);
 			writeBuffer = "\n" + GUIMain.FORMULAS.get(i).allInfoToString() + "\n \n";
 			try{
 				doc.insertString(doc.getLength(),writeBuffer,doc.getStyle("regular"));
 			}catch(BadLocationException ble){
 				System.err.println("Couldn't insert text into text pane.");
 			}
 		}
 
 
 		//VARIABLE HEADER
 		output.insertIcon(new ImageIcon("img/varheader.png"));
 		//This is retarded.
 		//There has to be a better way to force newlines in a TextPane.
 		//I fucking hate TextPanes.
 		try{
 			doc.insertString(doc.getLength(),"\n",doc.getStyle("regular"));
 		}catch(BadLocationException ble){
 			System.err.println("Couldn't insert text into text pane.");
 		}
 
 
 		for(int i=0; i<GUIMain.VARIABLES.size();i++) {
 			writeBuffer =
 					GUIMain.VARIABLES.get(i).getVar() + "   Units: " +
 							GUIMain.VARIABLES.get(i).getUnit() + "\n" + "Info: " +
 							GUIMain.VARIABLES.get(i).getInfo() + "\n" + "Tags: " +
 							GUIMain.VARIABLES.get(i).getTags() + "\n\n";
 			try{
 				doc.insertString(doc.getLength(),writeBuffer,doc.getStyle("regular"));
 			}catch(BadLocationException ble){
 				System.err.println("Couldn't insert text into text pane.");
 			}
 		}
 
 
 		//UNIT HEADER
 		output.insertIcon(new ImageIcon("img/unitheader.png"));
 		//This is retarded.
 		//There has to be a better way to force newlines in a TextPane.
 		//I fucking hate TextPanes.
 		try{
 			doc.insertString(doc.getLength(),"\n",doc.getStyle("regular"));
 		}catch(BadLocationException ble){
 			System.err.println("Couldn't insert text into text pane.");
 		}
 
 
 		for(int i=0; i<GUIMain.UNITS.size();i++) {
 			writeBuffer =
 					GUIMain.UNITS.get(i).getName() + "\n" + "Info: " +
 							GUIMain.UNITS.get(i).getInfo() + "\n" + "Tags: " +
 							GUIMain.UNITS.get(i).getAllTags() + "\n\n";
 			try{
 				doc.insertString(doc.getLength(),writeBuffer,doc.getStyle("regular"));
 			}catch(BadLocationException ble){
 				System.err.println("Couldn't insert text into text pane.");
 			}
 		}
 
 		add(scroller);
 	}
 
 	private void addStylesToDocument(StyledDocument doc) {
 		//Taken right out of the TextSamplerDemo from Oracle.
 		//Initialize some styles.
 		Style def = StyleContext.getDefaultStyleContext().
 				getStyle(StyleContext.DEFAULT_STYLE);
 
 		Style regular = doc.addStyle("regular", def);
 		StyleConstants.setFontFamily(def, "SansSerif");
 
 		Style s = doc.addStyle("italic", regular);
 		StyleConstants.setItalic(s, true);
 
 		s = doc.addStyle("bold", regular);
 		StyleConstants.setBold(s, true);
 
 		s = doc.addStyle("small", regular);
 		StyleConstants.setFontSize(s, 10);
 
 		s = doc.addStyle("large", regular);
 		StyleConstants.setFontSize(s, 16);
 
 	}
 
 	//Part of my old idea to add this to the TextPane as a StyledDocument.
 	//	private void addFormulaIconToDocument(StyledDocument doc, int i) {
 	//	//LaTeX support goes here
 	//		Style def = StyleContext.getDefaultStyleContext().
 	//                getStyle(StyleContext.DEFAULT_STYLE);
 	//		Style regular = doc.addStyle("regular", def);
 	//		
 	//		Style s = doc.addStyle("icon",regular);
 	//		ImageIcon formulaIcon = new ImageIcon(GUIMain.FORMULAS.get(i).toLaTeXImage());
 	//		if (formulaIcon != null) {
 	//			StyleConstants.setIcon(s, formulaIcon);
 	//		}
 	//		
 	//	}
 	
 	/**
 	 * infoLabel inner class allows the JLabel to be used as its own MouseListener, so that
 	 * it can replace this panel with the appropriate info page when clicked, or take other action.
	 * Right now, it deletes the formula.
 	 * @author Jonny
 	 * @version WIP
 	 */
 	public class infoLabel extends JLabel implements MouseListener{
 		
 		public int index;
 		
 		public infoLabel(Icon image, int i){
 			super(image);
 			index = i;
 		}
 		
 		public void mouseClicked(MouseEvent arg0) {
			System.out.println(GUIMain.FORMULAS.get(index).getName() + " was removed.");
 			GUIMain.FORMULAS.rmFormula(index);
 			Saver.saveForms(GUIMain.FORMULAS);
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 		
 	}//class delimit
 }
