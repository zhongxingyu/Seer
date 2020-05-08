 /*
  * Created On: Jan 19, 2006
  */
 package com.thinkparity.ophelia.browser.platform.action.container;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.document.Document;
 
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.platform.action.AbstractAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.model.artifact.ArtifactModel;
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 import com.thinkparity.ophelia.model.container.ContainerModel;
 import com.thinkparity.ophelia.model.document.DocumentModel;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.13
  */
 public class AddDocument extends AbstractAction {
 
 	/** The browser application. */
 	private final Browser browser;
 
 	/**
 	 * Create Document.
 	 * 
 	 * @param browser
 	 *            The browser application.
 	 */
 	public AddDocument(final Browser browser) {
 		super(ActionId.CONTAINER_ADD_DOCUMENT);
 		this.browser = browser;
 	}
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
      * 
      */
 	public void invoke(final Data data) {
 	    final Long containerId = (Long) data.get(DataKey.CONTAINER_ID);
 		final File[] files = (File[]) data.get(DataKey.FILES);
 
         if(0 == files.length) {
             // prompt for files
             browser.runAddContainerDocuments(containerId);
         } else {
             final ArtifactModel artifactModel = getArtifactModel();
             final ContainerModel containerModel = getContainerModel();
             final DocumentModel documentModel = getDocumentModel();
             Document document;
             
             // Get the list of existing documents in this package.
             List<Document> existingDocuments = Collections.emptyList();
             final Container container = containerModel.read(containerId);
             if (container.isLocalDraft()) {
                 final ContainerDraft draft = containerModel.readDraft(containerId);
                 if (null != draft) {
                     existingDocuments = draft.getDocuments();
                 }
             }
             
             // Check for duplicates, these are not allowed.
             final List<File> duplicates = new ArrayList<File>();
             for(final File file : files) {
                 for (final Document existingDocument : existingDocuments) {
                    if (existingDocument.getName().equals(file.getName())) {
                         duplicates.add(file);
                         break;
                     }
                 }
             }
             
             if (duplicates.isEmpty()) {
                 for(final File file : files) {
                     try {
                         final InputStream inputStream = new FileInputStream(file);
                         try {
                             document = documentModel.create(file.getName(), inputStream);
                             containerModel.addDocument(containerId, document.getId());
                             artifactModel.applyFlagSeen(document.getId());
                         } finally {
                             inputStream.close();
                         }
                     }
                     catch(final IOException iox) { throw translateError(iox); }
                 }
             } else {
                 if (duplicates.size() == 1) {
                     browser.displayErrorDialog("ErrorAddDocumentNotUnique",
                             new Object[] {duplicates.get(0).getName()});
                 } else if (duplicates.size() == 2) {
                     browser.displayErrorDialog("ErrorAddDocumentTwoNotUnique",
                             new Object[] {duplicates.get(0).getName(), duplicates.get(1).getName()});
                 } else {
                     browser.displayErrorDialog("ErrorAddDocumentManyNotUnique",
                             new Object[] {duplicates.get(0).getName(), duplicates.get(1).getName()});
                 }
             }
         }
 	}
 
 	public enum DataKey { CONTAINER_ID, FILES }
 }
