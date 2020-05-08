 package net.cyklotron.cms.modules.actions.files;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.util.ResourceSelectionState;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.modules.actions.related.BaseRelatedAction;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Accepts uploaded file and adds it to the Files repository, and optionally binds it with the
  * currently selected resource through the Related application.
  */
 public class UploadAndBindFile
     extends UploadFile
 {
 
     private final RelatedService relatedService;
 
     public UploadAndBindFile(Logger logger, StructureService structureService,
        CmsDataFactory cmsDataFactory, FilesService filesService, FileUpload fileUpload, RelatedService relatedService)
     {
         super(logger, structureService, cmsDataFactory, filesService, fileUpload);
         this.relatedService = relatedService;
     }
 
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         super
             .execute(context, parameters, mvcContext, templatingContext, httpContext, coralSession);
        
         boolean quickAdd = parameters.getBoolean("quick_bind", false);
         String result = (String)templatingContext.get("result");
		if( result.equals("uploaded_successfully") && quickAdd )
         {
             Resource file = (Resource)templatingContext.get("file");
             ResourceSelectionState relatedState = ResourceSelectionState.getState(context,
                 BaseRelatedAction.RELATED_SELECTION_STATE + ":" + parameters.get("res_id", ""));
             relatedState.setValue(file, "selected");
         }
     }
 
 }
