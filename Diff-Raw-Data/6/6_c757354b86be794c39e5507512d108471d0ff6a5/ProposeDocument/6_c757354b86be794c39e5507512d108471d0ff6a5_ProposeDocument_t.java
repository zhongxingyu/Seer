 package net.cyklotron.cms.modules.actions.structure;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.datatypes.ResourceList;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.upload.UploadLimitExceededException;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.utils.StringUtils;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeAlreadyExistException;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Propose new navigation node in document tree.
  * 
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @author <a href="mailo:mover@caltha.pl">Michal Mach</a>
  * @version $Id: ProposeDocument.java,v 1.22 2008-11-05 23:21:37 rafal Exp $
  */
 
 public class ProposeDocument
     extends BaseAddEditNodeAction
 {
     private CategoryService categoryService;
 
     private final FileUpload uploadService;
 
     private final FilesService filesService;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private final RelatedService relatedService;
 
     public ProposeDocument(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, CategoryService categoryService,
         FileUpload uploadService, FilesService filesService,
         CoralSessionFactory coralSessionFactory, RelatedService relatedService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.categoryService = categoryService;
         this.uploadService = uploadService;
         this.filesService = filesService;
         this.coralSessionFactory = coralSessionFactory;
         this.relatedService = relatedService;
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         // basic setup
 
         Subject subject = coralSession.getUserSubject();
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
         DocumentNodeResource node = null;
         boolean valid = true;
         CategoryResource[] parentCategories = null;
 
         try
         {
             // get parameters
             ProposedDocumentData data = new ProposedDocumentData(screenConfig);
             data.fromParameters(parameters, coralSession);
 
             // check required parameters
             if(!data.isValid())
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
         
             // file upload - checking
            if(valid && !data.isFileUploadValid(coralSession, uploadService))
             {
                valid = false;
                templatingContext.put("result", data.getValidationFailure());
             }
 
             // find parent node
             NavigationNodeResource parent = null;
             if(valid)
             {
                 long parentId = screenConfig.getLong("parent_id", -1L);
                 if(parentId != -1L)
                 {
                     try
                     {
                         parent = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,
                             parentId);
                     }
                     catch(EntityDoesNotExistException e)
                     {
                         templatingContext.put("result", "parent_misconfigured");
                         valid = false;
                     }
                 }
                 else
                 {
                     // when no parent is selected in component config, add new node as child of the node where proposal form resides
                     parent = cmsData.getNode();
                 }
             }
                 
             if(valid)
             {    
                 parentCategories = categoryService.getCategories(coralSession, parent, false);
 
                 if(data.isCalendarTree() && data.getValidityStart() != null)
                 {
                     parent = structureService.getParent(coralSession, parent, data.getValidityStart(),
                         StructureService.DAILY_CALENDAR_TREE_STRUCTURE, subject);
                 }
                 try
                 {
                     // add navigation node
                     node = structureService.addDocumentNode(coralSession, data.getName(), data.getTitle(),
                         parent, subject);
                 }
                 catch(NavigationNodeAlreadyExistException e)
                 {
                     templatingContext.put("result", "navi_name_repeated");
                     valid = false;
                 }
             }
 
             if(valid)
             {
                 data.toNode(node);
                 node.setSequence(getMaxSequence(coralSession, parent));
                 assignCategories(data, coralSession, node, parentCategories);
                 uploadAndAttachFiles(node, parameters, screenConfig, coralSession);        
                 setState(coralSession, subject, node);
                 structureService.updateNode(coralSession, node, data.getName(), true, subject);
                 
                 data.logProposal(logger, node);
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             logger.error("StructureException: ", e);
             templatingContext.put("trace", new StackTrace(e));
             valid = false;
         }
         // make the newly created node a current node
         if(valid)
         {
             parameters.set("state", "Result");
             templatingContext.put("result", "added_successfully");
         }
         else
         {
             parameters.set("state", "AddDocument");
         }
     }
 
     private void uploadAndAttachFiles(DocumentNodeResource node, Parameters parameters, Parameters screenConfig,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             if(screenConfig.getBoolean("attachments_enabled", false))
             {
                 int attachmentsMaxCount = screenConfig.getInt("attachments_max_count", 0);
                 ResourceList<FileResource> attachments = new ResourceList<FileResource>(coralSessionFactory);
                 for (int i = 0; i < attachmentsMaxCount; i++)
                 {
                     UploadContainer file = uploadService.getContainer("attachment_" + (i + 1));
                     if(file != null)
                     {
                         String description = parameters.get("attachment_description_" + (i + 1));
                         long dirId = screenConfig.getLong("attachments_dir_id", -1);
                         DirectoryResource dir = DirectoryResourceImpl.getDirectoryResource(
                             coralSession, dirId);
                         FileResource f = filesService.createFile(coralSession,
                             getAttachmentName(file.getFileName()), file.getInputStream(), file
                                 .getMimeType(), null, dir);
                         f.setDescription(description);
                         f.update();
                         attachments.add(f);
                     }
                 }
                 relatedService.setRelatedTo(coralSession, node, attachments.toArray(new Resource[attachments.size()])); 
                 node.setRelatedResourcesSequence(attachments);
                 node.update();
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("problem while processing attachments", e);
         }
     }
 
     private String getAttachmentName(String fileName)
     {
         StringBuilder buff = new StringBuilder();
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
         buff.append(df.format(new Date())); // timestamp
         buff.append("_"); // separator
         fileName = StringUtils.iso1toUtf8(fileName);
         fileName = StringUtils.unaccentLatinChars(fileName); // unaccent accented latin characters
         fileName = fileName.replaceAll("[^A-Za-z0-9-_.]+", "_"); // squash everything except alphanumerics and allowed punctuation
         fileName = fileName.replaceAll("_{2,}", "_"); // contract sequences of multiple _
         buff.append(fileName);
         return buff.toString();
     }
 
     private void setState(CoralSession coralSession, Subject subject, DocumentNodeResource node)
         throws StructureException
     {
         // set the state to taken if user is redactor (logged in)
         Permission permission = coralSession.getSecurity().getUniquePermission(
             "cms.structure.modify_own");
         if(subject.hasPermission(node, permission))
         {
             structureService.enterState(coralSession, node, "taken", subject);
         }
         else
         {
             structureService.enterState(coralSession, node, "new", subject);
         }
     }
 
     private void assignCategories(ProposedDocumentData data, CoralSession coralSession,
         DocumentNodeResource node, CategoryResource[] parentCategories)
         throws EntityDoesNotExistException
     {
         if(data.isInheritCategories() || data.getSelectedCategories().size() > 0)
         {
             Relation refs = categoryService.getResourcesRelation(coralSession);
             RelationModification diff = new RelationModification();
             Permission classifyPermission = coralSession.getSecurity().getUniquePermission(
                 "cms.category.classify");
             if(data.isInheritCategories() && parentCategories != null)
             {                
                 for (CategoryResource category : parentCategories)
                 {
                     if(coralSession.getUserSubject().hasPermission(category, classifyPermission))
                     {
                         diff.add(category, node);
                     }
                 }
             }
             for (CategoryResource categoryResource : data.getSelectedCategories())
             {
                 if(coralSession.getUserSubject().hasPermission(categoryResource, classifyPermission))
                 {
                     diff.add(categoryResource, node);
                 }
             }
             coralSession.getRelationManager().updateRelation(refs, diff);
         }
     }
 
     private int getMaxSequence(CoralSession coralSession, NavigationNodeResource parent)
     {
         // get greatest sequence number to put new node on top of
         // sequence-sorted list
         int sequence = 0;
         Resource[] children = coralSession.getStore().getResource(parent);
         for (int i = 0; i < children.length; i++)
         {
             Resource child = children[i];
             if(child instanceof NavigationNodeResource)
             {
                 int childSeq = ((NavigationNodeResource)child).getSequence(0);
                 sequence = sequence < childSeq ? childSeq : sequence;
             }
         }
         return sequence;
     }
 
     protected String getViewName()
     {
         return "";
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         try
         {
             Permission permission = coralSession.getSecurity().getUniquePermission(
                 "cms.structure.submit");
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             long parentId = screenConfig.getLong("parent_id", -1L);
             Resource parent = parentId != -1L ? NavigationNodeResourceImpl
                 .getNavigationNodeResource(coralSession, parentId) : cmsData.getNode();
             return coralSession.getUserSubject().hasPermission(parent, permission);
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured during access rights checking ", e);
         }
     }
 
     /**
      * @{inheritDoc
      */
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return false;
     }
 }
