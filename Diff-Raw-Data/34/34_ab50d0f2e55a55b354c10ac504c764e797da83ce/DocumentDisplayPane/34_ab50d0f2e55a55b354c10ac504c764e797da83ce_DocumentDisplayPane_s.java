 package edu.cs319.client.customcomponents;
 
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.JTextArea;
 
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.SectionizedDocument;
 
 /**
  * 
  * @author Justin Nelson
  *
  */
 public class DocumentDisplayPane extends JTextArea {
 
 	private SectionizedDocument doc;
 
 	public void updateDocument(SectionizedDocument doc) {
 		this.doc = doc;
 		this.setText(doc.getFullText());
 	}
 
 	@Override
 	protected void paintComponent(Graphics g2) {
 		super.paintComponent(g2);
 		Graphics2D g = (Graphics2D) g2;
 		FontMetrics m = g.getFontMetrics();
 		int lineHeight = m.getHeight();
 		int curOffset = 0;
 		if (doc != null)
 			for (DocumentSubSection sec : doc.getAllSubSections()) {
				curOffset += lineCount(sec) * lineHeight;
 				g.drawLine(0, curOffset, this.getWidth(), curOffset);
 				curOffset++; // new line between subsections
 			}
 	}
 
 	private static int lineCount(DocumentSubSection sec) {
 		String text = sec.getText();
 		String[] textSplit = text.split("\\n", -1);
 		return textSplit.length;
 	}
 }
