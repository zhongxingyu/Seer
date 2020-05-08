 package views.gui.components;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.PlainDocument;
 
 public class LimitedDocument extends PlainDocument {
 	private static final long serialVersionUID = -3792500384846017205L;
 	int limit;
 
 	public LimitedDocument(int limit) {
 		this.limit = limit;
 	}
 	
 	@Override
 	public void insertString(int offs, String str, AttributeSet attr) throws BadLocationException {
		if(str == null)
 			return;
		if(getLength() + str.length() > limit)
			str = str.substring(0, limit-getLength());
 		super.insertString(offs, str, attr);	
 	}
 }
