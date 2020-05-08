 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Used in conjunction with <code>CollaboratorService.getDocument()</code>.
  */
 public class DocReader implements AsyncCallback<UnlockedDocument> {
 
 	private Collaborator collaborator;
 	private String side; // is the current doc on the left or the right?
 	private int index; // which tab is the current doc on?
 
 	/**
 	 * This function creates a new DocReader and calls it.  This 
 	 * function basically allows for multiple requests to be sent
 	 * at the same time with different internal state associated with them.
 	 * @param collaborator
 	 * @param key
 	 * @param side
 	 * @param ind
 	 * @return
 	 */
 	public static DocReader readDoc(Collaborator collaborator, String key,
 			String side, int ind) {
 		DocReader r = new DocReader(collaborator);
 		r.getDocument(key, side, ind);
 		return r;
 	}
 
 	public DocReader(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 
 	/**
 	 * Makes a request for a document
 	 * @param key
 	 * @param side
 	 * @param ind
 	 */
 	public void getDocument(String key, String side, int ind) {
 		if (key != null) {
 			this.index = ind;
 			this.side = side;
 			collaborator.collabService.getDocument(key, this);
 		} else
 			collaborator.statusUpdate("Can't fetch a doc with a null key!");
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		collaborator.statusUpdate("Error retrieving document"
 				+ "; caught exception " + caught.getClass() + " with message: "
 				+ caught.getMessage());
 		GWT.log("Error getting document lock.", caught);
 
 		// make sure the correct 'show button' is still enabled
 		if (side.equals("left"))
 			collaborator.showButtonL.setEnabled(true);
 		else
 			collaborator.showButtonR.setEnabled(true);
 		
 		
 	}
 
 	@Override
 	public void onSuccess(UnlockedDocument result) {
 		collaborator.setDoc(result, index, side);
 	}
 }
