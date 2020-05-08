 package net.cyklotron.cms.category.components;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.util.ResourceSelectionState;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.web.HttpContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.category.CategoryConstants;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.category.query.CategoryResolver;
 import net.cyklotron.cms.integration.ResourceClassResource;
 
 /**
  * Provides default parameter values for resource list configuration.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelatedResourceListConfiguration.java,v 1.8 2007-01-28 11:50:39 rafal Exp $
  */
 public class RelatedResourceListConfiguration
 extends BaseResourceListConfiguration
 {
 	public static String KEY = "cms.category.related_resource_list.configuration";
 
     public static RelatedResourceListConfiguration getConfig(Context context)
         throws ProcessingException
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         RelatedResourceListConfiguration currentConfig = (RelatedResourceListConfiguration)
             httpContext.getSessionAttribute(KEY);
         if(currentConfig == null)
         {
             currentConfig = new RelatedResourceListConfiguration();
             httpContext.setSessionAttribute(KEY, currentConfig);
         }
         return currentConfig;
     }
 
     public static void removeConfig(Context context)
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         httpContext.removeSessionAttribute(KEY);
     }
 
 	public RelatedResourceListConfiguration()
 	{
 		super();
 	}
     
 	/** Set of resource classes names accepted in resource list. */
 	private Set resourceClassesNames = new HashSet();
 	/** Category selection state used during component configuration. */
 	private ResourceSelectionState categorySelectionState;
     private String[] activeCategoriesPaths;
 
 	public static String ACTIVE_CATEGORIES_PARAM_KEY = "activeCategories";
 
 	/** Short initialisation used during component preparation. Does not initialise category
 	 selection state. */
 	public void shortInit(Parameters componentConfig)
 	{
 		super.shortInit(componentConfig);	
 	}
 
 	/** Initialisation used during component configuration. Initialises category selection state. */
 	public void init(CoralSession coralSession, Parameters componentConfig, CategoryQueryService categoryQueryService)
 	{
 		super.init(componentConfig);
 		categorySelectionState =
 			new ResourceSelectionState(CategoryConstants.CATEGORY_SELECTION_STATE);
 		categorySelectionState.setPrefix("category");
 		categorySelectionState.init(buildInitialState(coralSession,componentConfig, categoryQueryService));
 	}
 
 	/** Updates the config after a form post during configuration. */
 	public void update(CmsData cmsData, Parameters parameters)
 	    throws ProcessingException
 	{
 		categorySelectionState.update(parameters);
 		super.update(cmsData, parameters);
 	}
 
     protected void setParams(Parameters params)
     {
         super.setParams(params);
         
 		resourceClassesNames.clear();
 		resourceClassesNames.addAll(Arrays.asList(params.getStrings("resourceClasses")));
 
 		String[] quotedPaths = params.getStrings(ACTIVE_CATEGORIES_PARAM_KEY);
 		activeCategoriesPaths = new String[quotedPaths.length];
 		for (int i = 0; i < quotedPaths.length; i++)
 		{
 			String quotedPath = quotedPaths[i];
            // fix for CYKLO-425
             if(quotedPath.startsWith("\\'") && quotedPath.endsWith("\\'")) {
                 activeCategoriesPaths[i] = quotedPath.substring(2, quotedPath.length()-2);
             } else {
                 activeCategoriesPaths[i] = quotedPath.substring(1, quotedPath.length()-1);
             }
 		}
     }
 
 	// special /////////////////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Checks if a given resource class resource is chosen for this configuration.
 	 */
 	public boolean hasResourceClass(ResourceClassResource rc)
 	{
 		return resourceClassesNames.contains(rc.getName());
 	}
 
     // getters /////////////////////////////////////////////////////////////////////////////////////
 
 	public String[] getResourceClasses()
 	{
 		String[] resClasses = new String[resourceClassesNames.size()];
 		return (String[])(resourceClassesNames.toArray(resClasses));
 	}
 
 	public ResourceSelectionState getCategorySelectionState()
 	{
 		return categorySelectionState;
 	}
 	
     public String[] getActiveCategoriesPaths()
     {
         return activeCategoriesPaths;
     }
 
     // category selection state initialisation /////////////////////////////////////////////////////
 
 	protected void buildCategorySelectionState(CoralSession coralSession,
 		Map initialState, String[] paths, String stateName, CategoryQueryService categoryQueryService)
 	{
 		if(paths != null)
 		{
 			CategoryResolver resolver = categoryQueryService.getCategoryResolver();
 			for(int i=0; i<paths.length; i++)
 			{
 				CategoryResource category = resolver.resolveCategoryIdentifier(paths[i]);
 				if(category != null)
 				{
 					initialState.put(category, stateName);
 				}
 			}
 		}
 	}
 
     protected Map buildInitialState(CoralSession coralSession,Parameters componentConfig, CategoryQueryService categoryQueryService)
     {
         // activeCategoriesIds is already prepared in setParams called from init()
         Map initialState = new HashMap();
         buildCategorySelectionState(coralSession, initialState, activeCategoriesPaths, "active", categoryQueryService);
         return initialState;
     }
 }
