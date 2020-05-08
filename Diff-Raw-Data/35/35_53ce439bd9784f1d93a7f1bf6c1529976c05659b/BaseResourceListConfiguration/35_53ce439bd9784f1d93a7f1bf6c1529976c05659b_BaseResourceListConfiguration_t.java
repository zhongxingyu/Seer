 package net.cyklotron.cms.category.components;
 
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsNodeResource;
 
 /**
  * This is a base class for classes that provide default parameter values for categorized
  * resource lists' configurations.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: BaseResourceListConfiguration.java,v 1.7 2005-04-12 06:14:28 rafal Exp $
  */
 public abstract class BaseResourceListConfiguration
 {
     /** The header string for this component. */
     private String header;
     /** The name of a sorting column for resource list. */
     private String sortColumn;
     /** The direction of sorting for resource list. */
     private boolean sortDir;
     /** Maximal number of resources visible in list. */
     private int maxResNumber;
     /** Number of seconds the calculated resource list will be kept in the cache. */
     private int cacheInterval;
 
     private CmsNodeResource configOriginNode;
     
     /** <code>true</code> if the config object was created during current request. */
     protected boolean newConfig;
 
     public BaseResourceListConfiguration()
     {
         newConfig = true;
     }
 
     public boolean isNew()
     {
         return newConfig;
     }
 
 	/** Short initialisation used during component preparation. */
 	public void shortInit(Parameters componentConfig)
 	{
 		setParams(componentConfig);
 		// config was modified
 		newConfig = false;
 	}
 
     /** Initialisation used during component configuration. */
     public void init(Parameters componentConfig)
     {
         setParams(componentConfig);
         // config was modified
         newConfig = false;
     }
 
     /** Updates the config after a form post during configuration. 
      * @param cmsData */
     public void update(CmsData cmsData, Parameters parameters)
     throws ProcessingException
     {
         setParams(parameters);
         // config was modified
         newConfig = false;
     }
 
     /** Thi one sets basic parameters from parameter container - either from component config or
      * current request, MUST be called before any other modification in init, shortInit or update
      */
     protected void setParams(Parameters params)
     {
         header = params.get("header","");
 
         sortColumn = params.get("listSortColumn","index.title");
         // NOTE: 0 in parameters means ascending (true), 1 means descending (false)
        sortDir = params.getInt("listSortDir", 0) == 0;
 
         maxResNumber = params.getInt("maxResNumber",0);
         cacheInterval = params.getInt("cacheInterval",0);
     }
     
     // getters /////////////////////////////////////////////////////////////////////////////////////
 
     public String getHeader()
     {
         return header;
     }
 
     public String getSortColumn()
     {
         return sortColumn;
     }
 
     public boolean getSortDir()
     {
         return sortDir;
     }
 
     /** Returns a maximal number of resources visible in resource list.
      * @return Value of property maxResNumber.
      */
     public int getMaxResNumber()
     {
         return maxResNumber;
     }
 
     public int getCacheInterval()
     {
         return cacheInterval;
     }
     
     /**
      * @return Returns the configOriginNode.
      */
     public CmsNodeResource getConfigOriginNode()
     {
         return configOriginNode;
     }
     
     /**
      * @param configOriginNode The configOriginNode to set.
      */
     public void setConfigOriginNode(CmsNodeResource configOriginNode)
     {
         this.configOriginNode = configOriginNode;
     }
 }
