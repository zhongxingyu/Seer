 package Entities;
 
 import java.util.Stack;
 
 /**
  * 
  * @author Ben Kantor (bdk3079@rit.edu)
  *
  */
 public class DocCaretaker {	
     
     public DocCaretaker(){}
     
 	private Stack<DocMomento> docStates = new Stack<DocMomento>();
 	
 	/**
 	 * Method: Stores a new document state to the caretaker
 	 * @param node Root node of the HTML document tree
 	 */
 	public void storeState(DocMomento text){
 		docStates.push(text);
 	}
 	
 	/**
 	 * Method: Returns the most recent saved state of the document
 	 */
 	public DocMomento returnLastState(){
 		if (docStates.size() == 1){
 			return docStates.peek();
 		} else {
 			return docStates.pop();
 		}
 	}
 }
