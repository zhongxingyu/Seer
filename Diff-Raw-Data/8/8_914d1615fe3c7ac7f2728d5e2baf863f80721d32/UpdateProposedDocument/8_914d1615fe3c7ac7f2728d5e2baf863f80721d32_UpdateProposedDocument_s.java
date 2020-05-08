 package net.cyklotron.cms.modules.actions.structure;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.html.HTMLService;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.documents.DocumentService;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 public class UpdateProposedDocument
     extends BaseProposeDocumentAction
 {
     private final RelatedService relatedService;
 
     private final HTMLService htmlService;
 
     private final DocumentService documentService;
 
     public UpdateProposedDocument(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, FileUpload fileUpload,
         FilesService filesService, RelatedService relatedService, HTMLService htmlService,
         DocumentService documentService)
     {
         super(logger, structureService, cmsDataFactory, styleService, filesService, fileUpload);
         this.relatedService = relatedService;
         this.htmlService = htmlService;
         this.documentService = documentService;
     }
 
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         boolean valid = true;
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
 
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             ProposedDocumentData data = new ProposedDocumentData(screenConfig,
                 documentService.getPreferredImageSizes(), logger);
             data.fromParameters(parameters, coralSession);
             data.setOrigin(cmsData.getNode());
 
             // check required parameters
             if(!data.isValid(htmlService))
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
 
             // file upload - checking
             if(valid && !data.isFileUploadValid(coralSession, uploadService, filesService))
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
 
             if(valid && data.isAttachmentsEnabled())
             {
                 DirectoryResource dir = data.getAttachmenDirectory(coralSession);
                for(int i = 0; i < data.getNewAttachmentsCount()
                    && data.getCurrentAttachments().size() + i < data.getAttachmentsMaxCount(); i++)
                 {
                    FileResource attachment = createAttachment(data, data.getCurrentAttachments()
                        .size() + i, dir, coralSession);
                     data.addAttachment(attachment);
                 }
 
                 Set<Resource> publishedAttachments = new HashSet<Resource>(Arrays
                     .asList(relatedService.getRelatedTo(coralSession, node, node
                         .getRelatedResourcesSequence(), null)));
                 for(long id : parameters.getLongs("remove_attachment"))
                 {
                     FileResource file = data.removeAttachment(id, coralSession);
                     if(!publishedAttachments.contains(file) && !node.getThumbnail().equals(file))
                     {
                         filesService.deleteFile(coralSession, file);
                     }
                 }
             }
             
             if(valid)
             {
                 data.toProposal(node);
                 node.update();
             }
         }
         catch(Exception e)
         {
             logger.error("excception", e);
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             valid = false;
         }
         if(valid)
         {
             templatingContext.put("result", "update_request_submitted");
         }
         else
         {
             parameters.set("state", "EditDocument");
         }
     }
     
     @Override
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return true;
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws Exception
     {
         Parameters requestParameters = context.getAttribute(RequestParameters.class);
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         Subject userSubject = coralSession.getUserSubject();
 
         long id = requestParameters.getLong("doc_id", -1);
         Resource node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,
             id);
         Permission modifyPermission = coralSession.getSecurity().getUniquePermission(
             "cms.structure.modify");
         Permission modifyOwnPermission = coralSession.getSecurity().getUniquePermission(
             "cms.structure.modify_own");
         if(userSubject.hasPermission(node, modifyPermission))
         {
             return true;
         }
         if(node.getOwner().equals(userSubject)
             && userSubject.hasPermission(node, modifyOwnPermission))
         {
             return true;
         }
         if(node.getCreatedBy().equals(userSubject)
             && userSubject.hasPermission(node, modifyOwnPermission))
         {
             return true;
         }
         return false;
     }
 }
