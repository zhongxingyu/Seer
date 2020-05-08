 package net.cyklotron.cms.category.query;
 
 import org.objectledge.parameters.Parameters;
 
 /**
  * Provides default parameter values for category query list component's configuration.
  * 
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CategoryQueryListConfiguration.java,v 1.4 2005-04-12 06:14:29 rafal Exp $ 
  */
 public class CategoryQueryListConfiguration
 {
 	private String header;
 	private String queryPoolName;
 	private String sortColumn;
 	private boolean sortDir;
 
 	public CategoryQueryListConfiguration(Parameters componentConfig)
 	{
 		header = componentConfig.get("header",null);
 		queryPoolName = componentConfig.get("queryPoolName",null);
 		sortColumn = componentConfig.get("querySortColumn","name");
         // NOTE: 0 in parameters means ascending (true), 1 means descending (false)
		sortDir = componentConfig.getInt("querySortDir", 0) == 0;
 	}
 
 	public String getHeader()
 	{
 		return header;
 	}
     
 	public String getQueryPoolName()
 	{
 		return queryPoolName;
 	}
     
 	public String getSortColumn()
 	{
 		return sortColumn;
 	}
 
 	public boolean getSortDir()
 	{
 		return sortDir;
 	}
 }
