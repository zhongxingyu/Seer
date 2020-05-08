 /**
  * 
  */
 package gui;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.DocumentFilter;
 import javax.swing.text.Element;
 
 
 
 public class ConsoleLock extends DocumentFilter {
 
 	private int doNotEdit = 0;
 	private boolean useDocumentFilter;
 
 	public ConsoleLock(boolean consoleLockOn) {
 		this.useDocumentFilter = consoleLockOn;
 	}
 
 	
 
 	public void setUseDocumentFilter(boolean useDocumentFilter) {
 		this.useDocumentFilter = useDocumentFilter;
 	}
 
 
 
 	@Override
 	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
 		//System.out.println("================= REMOVE =================");
 		/*Document d = fb.getDocument();
 		Element root = d.getDefaultRootElement();
 
 		fb.remove(offset, length);*/
		if(useDocumentFilter){
 			Document doc = fb.getDocument();
 			Element root = doc.getDefaultRootElement();
 			int count = root.getElementCount();
 			int index = root.getElementIndex(offset);
 			System.out.println("OFF: " + offset);
 			System.out.println("doNotEdit: " + this.doNotEdit);
 			/* Element cur = root.getElement(index);
         String obsah = doc.getText(cur.getStartOffset(), doc.getLength()-cur.getStartOffset()) + text;
         int promptPosition = obsah.indexOf("$ ");
 			 */
 			if(index==count-1 && offset-this.doNotEdit > 1) 
 			{
 				fb.remove(offset, length);
 			}
 
 			return;
 		}
 
 		fb.remove(offset, length);
 	}
 
 
 	@Override
 	public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
 		//System.out.println("================= INSERT =================");
 		/*Document d = fb.getDocument();
 		Element root = d.getDefaultRootElement();
 
 
 
 		fb.insertString(offset, text, attr);*/
		if(useDocumentFilter){
 			Document doc = fb.getDocument();
 			Element root = doc.getDefaultRootElement();
 			int index = root.getElementIndex(offset);
 			Element cur = root.getElement(index);
 			String obsah = doc.getText(cur.getStartOffset(), doc.getLength()-cur.getStartOffset()) + text;
 			int promptPosition = obsah.lastIndexOf("$ ");
 			this.doNotEdit = promptPosition + cur.getStartOffset();
 			/*System.out.println("doNotEdit: " + doNotEdit);
 		System.out.println(obsah);*/
 		}
 		fb.insertString(offset, text, attr);
 
 
 
 
 	}
 
 
 	@Override
 	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
 		//System.out.println("================= REPLACE =================");
		if(useDocumentFilter){
 			Document doc = fb.getDocument();
 			Element root = doc.getDefaultRootElement();
 			int count = root.getElementCount();
 			int index = root.getElementIndex(offset);
 			//Element cur = root.getElement(index);
 			//String obsah = doc.getText(cur.getStartOffset(), doc.getLength()-cur.getStartOffset()) + text;
 			//int promptPosition = obsah.indexOf("$ ") + cur.getStartOffset();
 			//this.doNotEdit = promptPosition;
 			/*System.out.println("doNotEdit: " + doNotEdit);
 		System.out.println(obsah);
 		System.out.println("OFF: " +offset);
 		System.out.println("Prompt: " + promptPosition);*/
 			if(index==count-1 && offset-this.doNotEdit > 1) 
 			{
 				fb.replace(offset, length, text, attrs);
 			}
 			return;
 		}
 		
 		fb.replace(offset, length, text, attrs);
 
 
 
 
 	}
 }
