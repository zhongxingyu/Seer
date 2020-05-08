 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 public class DocSaver implements AsyncCallback<UnlockedDocument> {
 	
 	private Collaborator collaborator;
 	
 	public DocSaver(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 	
 	public void saveDocument(LockedDocument lockedDoc) {
 		collaborator.statusUpdate("Attemping to save document.");
 		collaborator.updateVarsAndUi(lockedDoc.getKey(), UiState.SAVING);
 		collaborator.collabService.saveDocument(lockedDoc, this);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		if (caught instanceof LockExpired) {
 			LockExpired caughtEx = (LockExpired) caught;
 			if (caughtEx.getWrongCredentials()) {
 				collaborator.statusUpdate("Lock hasn't expired, but you have the" +
						"wrong credentials; save failed.");
 			} else {
				collaborator.statusUpdate("Lock has already expired; save failed.");
 			}
 			collaborator.updateVarsAndUi(caughtEx.getKey(), UiState.VIEWING);
 		} else {
 			collaborator.statusUpdate("Error saving document"
 					+ "; caught exception " + caught.getClass()
 					+ " with message: " + caught.getMessage());
 			GWT.log("Error saving document.", caught);
 		}
 	}
 
 	@Override
 	public void onSuccess(UnlockedDocument result) {
 		collaborator.statusUpdate("Document '" + result.getTitle()
 				+ "' successfully saved.");
 		
 		collaborator.updateVarsAndUi(result.getKey(),
 				result.getTitle(),
 				result.getContents(),
 				UiState.VIEWING);
 		
 		// Refresh list in case title was changed.
 		collaborator.lister.getDocumentList();
 	}
 }
