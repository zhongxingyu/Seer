 /**
  * @author:Alex Bogart, Justin Peterson
  * TextWindow.java represents the actual Swing window that will be kept in the
  * main focus of the HTML editor.  
  * 
  */
 
 
 package Views;
 
 import Views.TextTabWindow;
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
public class TextWindow extends JTextPane{
 	
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructs an individual window to add to the TextTabWindow. 
 	 * @param tabWindow - the main TextTabWindow of the editor. 
 	 */
 	public TextWindow(String windowName, TextTabWindow tabWindow){
 		setLayout(new BorderLayout(5,10));
		new JTextPane();
		//setEditorKit(new HTMLEditorKit());
		//HTMLDocument doc = new HTMLDocument();
		//setStyledDocument(doc);
 		
 		tabWindow.addTab(windowName, this);
 		setVisible(true);
 	}
 
 }
