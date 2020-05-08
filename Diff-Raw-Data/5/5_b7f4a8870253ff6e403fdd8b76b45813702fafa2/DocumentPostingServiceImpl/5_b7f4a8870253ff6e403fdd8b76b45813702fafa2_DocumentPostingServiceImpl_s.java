 package net.cyklotron.cms.docimport;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.doc;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.dom4jToText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 
 import java.io.ByteArrayInputStream;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dom4j.Document;
 import org.objectledge.authentication.AuthenticationException;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * DocumentPostingService implementation.
  * 
  * @author rafal.krzewski@objectledge.org
  */
 public class DocumentPostingServiceImpl
     implements DocumentPostingService
 {
     private final StructureService structureService;
 
     private final CategoryService categoryService;
 
     private final FilesService filesService;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private final UserManager userManager;
 
     private final RelatedService relatedService;
 
     public DocumentPostingServiceImpl(StructureService structureService,
         CategoryService categoryService, FilesService filesService, RelatedService relatedService,
         CoralSessionFactory coralSessionFactory, UserManager userManager)
     {
         this.structureService = structureService;
         this.categoryService = categoryService;
         this.filesService = filesService;
         this.relatedService = relatedService;
         this.coralSessionFactory = coralSessionFactory;
         this.userManager = userManager;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<DocumentData, DocumentNodeResource> postDocuments(ImportTargetConfiguration config,
         Collection<DocumentData> documents)
         throws StructureException
     {
         Principal ownerPrincipal = null;
         CoralSession coralSession = null;
         String docName = null;
         try
         {
             ownerPrincipal = userManager.getUserByLogin(config.getContentOwnerLogin());
             coralSession = coralSessionFactory.getSession(ownerPrincipal);
             Subject ownerSubject = coralSession.getUserSubject();
             Map<DocumentData, DocumentNodeResource> docMap = new HashMap<DocumentData, DocumentNodeResource>();
             for(DocumentData docData : documents)
             {
                 NavigationNodeResource docParent = structureService.getParent(coralSession,
                     config.getTargetLocation(), docData.getCreationDate(),
                     config.getCalendarStructureType(), ownerSubject);
 
                 docName = docData.getOriginalName();
                 DocumentNodeResource docNode = structureService.addDocumentNode(coralSession,
                     docName, docData.getTitle(), docParent, ownerSubject);
 
                 fillDocument(docData, docNode);
 
                 assignCategories(docNode, config, coralSession);
 
                 Collection<FileResource> atts = storeAttachments(docData, docName, config,
                     coralSession);
 
                 addAttachmentRelations(docNode, atts, coralSession);
                 docMap.put(docData, docNode);
             }
             return docMap;
         }
         catch(AuthenticationException e)
         {
             throw new StructureException("user " + config.getContentOwnerLogin() + " not found", e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new StructureException("subject " + ownerPrincipal.getName() + " not found", e);
         }
         catch(InvalidResourceNameException e)
         {
             throw new StructureException("invalid document name " + docName, e);
         }
         catch(FilesException e)
         {
             throw new StructureException("filed to save the attachments", e);
         }
         finally
         {
             if(coralSession != null)
             {
                 coralSession.close();
             }
         }
     }
 
     private void fillDocument(DocumentData docData, DocumentNodeResource docNode)
     {
         docNode.setAbstract(docData.getAbstract());
        docNode.setContent(docNode.getContent());
         docNode.setCustomModificationTime(docData.getModificationDate());
         // @formatter:off
         Document meta = doc(
             elm("meta",
                 elm("authors"),
                 elm("sources", 
                     elm("source", 
                         elm("name", docData.getSourceName()), 
                         elm("url", docData.getOriginalURI().toString()))),
                 elm("editor"),
                 elm("event",
                     elm("address", 
                         elm("street"), 
                         elm("postcode"), 
                         elm("city"), 
                         elm("province"))),
                 elm("organizations")));
         // @formatter:on
         docNode.setMeta(dom4jToText(meta));
         docNode.update();
     }
 
     private void assignCategories(DocumentNodeResource docNode, ImportTargetConfiguration config,
         CoralSession coralSession)
     {
         Relation catRes = categoryService.getResourcesRelation(coralSession);
         RelationModification mod = new RelationModification();
         for(CategoryResource cat : config.getCategories())
         {
             mod.add(cat, docNode);
         }
         coralSession.getRelationManager().updateRelation(catRes, mod);
     }
 
     private Collection<FileResource> storeAttachments(DocumentData docData, String docName,
         ImportTargetConfiguration config, CoralSession coralSession)
         throws FilesException
     {
         Collection<FileResource> files = new ArrayList<FileResource>();
         if(docData.getAttachments().size() > 0)
         {
             DirectoryResource docDirectory = filesService.createDirectory(coralSession, docName,
                 config.getAttachmentsLocation());
             for(AttachmentData attData : docData.getAttachments())
             {
                 String attName = attData.getOriginalName();
                 String mimeType;
                 mimeType = filesService.detectMimeType(
                     new ByteArrayInputStream(attData.getContents()), attName);
                 files.add(filesService.createFile(coralSession, attName, new ByteArrayInputStream(
                     attData.getContents()), mimeType, null, docDirectory));
             }
         }
         return files;
     }
 
     private void addAttachmentRelations(DocumentNodeResource docNode,
         Collection<FileResource> atts, CoralSession coralSession)
     {
         relatedService.setRelatedTo(coralSession, docNode, atts.toArray(new Resource[atts.size()]));
     }
 }
