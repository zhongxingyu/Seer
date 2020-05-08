 package net.cyklotron.cms.modules.views.link;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.CoralTableModel;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsConstants;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.link.LinkService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.StructureService;
 /**
  *
  */
 public class LinkSource
     extends BaseLinkScreen
 {
     public LinkSource(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, LinkService linkService,
         StructureService structureService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, linkService,
                         structureService);
         
     }
 
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
 
         Boolean fromComponent = (Boolean)httpContext.getSessionAttribute(CmsConstants.FROM_COMPONENT);
         if(fromComponent != null && fromComponent.booleanValue())
         {
             templatingContext.put("from_component",fromComponent);
             templatingContext.put("component_node",(Long)httpContext.getSessionAttribute(CmsConstants.COMPONENT_NODE));
             templatingContext.put("component_instance",(String)httpContext.getSessionAttribute(CmsConstants.COMPONENT_INSTANCE));
         }
 
         SiteResource site = getSite();
         if(site != null)
         {
             try
             {
                 Resource root = structureService.getRootNode(coralSession, site);
 
                 String componentName = "link_structure_tree";
                 String rootId = root.getIdString();
                 TableState state = tableStateManager.getState(context, componentName);
                boolean viewType = parameters.getBoolean("viewType", true);
                 state.setTreeView(viewType);
                 state.setShowRoot(true);
                 state.setRootId(rootId);
                 state.setExpanded(rootId);
                 if(state.getSortColumnName() == null)
                 {
                     state.setSortColumnName("creation.time");
                 }
                 TableModel model = new CoralTableModel(coralSession, i18nContext.getLocale());
                 TableTool helper = null;
                 helper = new TableTool(state, null, model);
                 templatingContext.put("table", helper);
             }
             catch(StructureException e)
             {
                 throw new ProcessingException("Cannot create TableTool", e);
             }
             catch(TableException e)
             {
                 throw new ProcessingException("Cannot create TableTool", e);
             }
         }
     }
 }
