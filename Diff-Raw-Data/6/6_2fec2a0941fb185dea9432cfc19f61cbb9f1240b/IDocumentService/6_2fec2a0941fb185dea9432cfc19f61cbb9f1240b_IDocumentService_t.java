 package fr.cg95.cvq.service.document;
 
 import java.util.Date;
import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.exception.CvqBadPageNumberException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.annotation.IsHomeFolder;
 import fr.cg95.cvq.security.annotation.IsIndividual;
 import fr.cg95.cvq.service.document.annotation.IsDocument;
 
 /**
  * @author bor@zenexity.fr
  */
 public interface IDocumentService {
 
     /** service name used by Spring's application context */
     String SERVICE_NAME = "documentService";
 
     String CREATION_ACTION = "CREATION_ACTION";
     String STATE_CHANGE_ACTION = "STATE_CHANGE_ACTION";
     
     /**
      * Add a document to the system.
      *
      * This step is only used to create the document's administrative information.
      * To add the document's binary data, use the {@link #addPage} method.
      *
      * @param document the document to add
      * @return the document's id
      */
     Long create(@IsDocument Document document)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Modify an existing document.
      */
     void modify(@IsDocument Document document)
         throws CvqException;
     
 	/**
      * Delete an existing document.
      */
     void delete(@IsDocument final Long id)
         throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Delete documents belonging to the given home folder.
      */
     void deleteHomeFolderDocuments(@IsHomeFolder final Long homeFolderId)
         throws CvqException;
     
     /**
      * Delete documents belonging to the given individual.
      */
     void deleteIndividualDocuments(@IsIndividual final Long individualId)
         throws CvqException;
 
     Document getById(@IsDocument final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Add a page to an existing document.
      *
      * If no page is specified, add document binary at the last page
      *
      * @throws CvqBadPageNumberException if a page is specified for the
      *         binary but page already exists
      */
     void addPage(@IsDocument final Long documentId, final DocumentBinary documentBinary)
         throws CvqException, CvqObjectNotFoundException,
                CvqBadPageNumberException;
 
     /**
      * Modify a page of an existing document.
      */
     void modifyPage(@IsDocument final Long documentId, final DocumentBinary documentBinary)
         throws CvqException, CvqBadPageNumberException;
 
     /**
      * Remove a page from an existing document.
      */
     void deletePage(@IsDocument final Long documentId, final Integer pageId)
         throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Get a specific page of an existing document.
      */
     DocumentBinary getPage(@IsDocument final Long documentId, final Integer pageId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get a the number of pages associated to an existing document.
      */
     Integer getPagesNumber(@IsDocument final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all binary data associated to a document.
      */
     Set<DocumentBinary> getAllPages(@IsDocument final Long documentId)
         throws CvqException;
 
     /**
      * Get already provided documents for the given
      *       {@link fr.cg95.cvq.business.document.DocumentType}.
      *
      * @param docType the document type to search for
      * @param homeFolderId the home folder for which we are searching
      * @param individualId an optional individual to restrict the search to
      */
     List<Document> getProvidedDocuments(final DocumentType docType, 
             @IsHomeFolder final Long homeFolderId, @IsIndividual final Long individualId)
         throws CvqException;
 
     /**
      * Get documents associated to an home folder.
      */
     List<Document> getHomeFolderDocuments(@IsHomeFolder final Long homeFolderId, int maxResults)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get documents associated to an individual.
      */
     List<Document> getIndividualDocuments(@IsIndividual final Long individualId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Check, for all known local authorities, that the end validity date of documents 
      * in state PENDING, CHECKED or VALIDATED has not been reached. If it has been reached,
      * the document state is set to OUTDATED.
      * 
      * This method is currently used by an internal job.
      */
     void checkDocumentsValidity() throws CvqException;
 
     /**
      * Dispatcher method for workflow document method.
      */
     void updateDocumentState(final Long id, final DocumentState ds, final String message, 
             final Date validityDate)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
     
     /**
      * Validate a document.
      *
      * @param id the document's id
      * @param validityDate the document's validity date. If none is provided, the
      *                     default value is used
      * @param message a optional message associated with the validation
      */
     void validate(final Long id, final Date validityDate, final String message)
         throws CvqException, CvqObjectNotFoundException,
                CvqInvalidTransitionException;
 
     /**
      * Refuse the validation of a document.
      *
      * @param id the document's id
      * @param message a mandatory message associated with the refusal
      */
     void refuse(final Long id, final String message)
         throws CvqException, CvqObjectNotFoundException,
                CvqInvalidTransitionException;
 
     /**
      * Notify that the document has been checked.
      *
      * @param id the document's id
      * @param message a optional message (eg 'bring the original ones')
      */
     void check(final Long id, final String message)
         throws CvqException, CvqObjectNotFoundException,
                CvqInvalidTransitionException;
 
     /**
      * Set the document state to out of date when its validity duration has
      * been reached. This method should not be called directly. Instead, an
      * autonomous validity checker should do this stuff ...
      */
     void outDated(final Long id)
         throws CvqException, CvqObjectNotFoundException,
                CvqInvalidTransitionException;
 
     /**
      * Get possible state transitions from the given document state.
      *
      * @return an array of {@link fr.cg95.cvq.business.document.DocumentState}
      *         objects
      * @see fr.cg95.cvq.business.document.DocumentState
      */
     DocumentState[] getPossibleTransitions(DocumentState rs)
         throws CvqException;
 
      
     /**
      * Provides ecitizen-oreinted document searching 
      * 
      * @param searchParams hash of parameters
      * @param max max rows to display
      * @param offset display offset
      * @return found documents
      */
     List<Document> search(Hashtable<String,Object> searchParams,int max,int offset);
     
     List<Document> search(Hashtable<String,Object> searchParams,int max);
 
     List<Document> search(Hashtable<String,Object> searchParams);
     
     Integer searchCount(Hashtable<String,Object> searchParams);
 }
