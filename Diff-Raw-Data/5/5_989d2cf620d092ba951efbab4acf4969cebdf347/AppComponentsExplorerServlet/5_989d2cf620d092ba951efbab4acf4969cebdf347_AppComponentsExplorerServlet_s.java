 package com.xaf.ace;
 
 import java.io.*;
 import java.lang.reflect.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.*;
 
 import com.xaf.ace.page.*;
 import com.xaf.config.*;
 import com.xaf.form.*;
 import com.xaf.html.*;
 import com.xaf.html.component.*;
 import com.xaf.page.*;
 import com.xaf.security.*;
 import com.xaf.skin.*;
 import com.xaf.value.*;
 
 public class AppComponentsExplorerServlet extends PageControllerServlet
 {
 	protected static final String CONFIGITEM_DEFAULT_PREFIX = "framework.ace.";
 	private Hashtable styleSheetParams = new Hashtable();
 	private Component[] menus;
 	private VirtualPath homePath;
 
     public void init(ServletConfig config) throws ServletException
 	{
         super.init(config);
 
 		List menuBar = new ArrayList();
 
 		List mainMenu = getPagesPath().getChildrenList();
 		int menuNum = 1;
 		for(Iterator i = mainMenu.iterator(); i.hasNext(); )
 		{
 			VirtualPath path = (VirtualPath) i.next();
 			if(path.getChildrenList().size() > 0)
 				menuBar.add(new HierarchicalMenu(menuNum, 171 + (66 * (menuNum-1)), 110, 38, path, getSharedScriptsRootURL()));
 			menuNum++;
 		}
 
 		menus = (Component[]) menuBar.toArray(new Component[menuBar.size()]);
 	}
 
 	public Component[] getMenuBar()
 	{
 		return menus;
 	}
 
 	public Hashtable getStyleSheetParams()
 	{
 		return styleSheetParams;
 	}
 
 	public String getConfigItemsPrefix()
 	{
 		return CONFIGITEM_DEFAULT_PREFIX;
 	}
 
 	public VirtualPath getHomePath()
 	{
 		return homePath;
 	}
 
 	public void registerPages(ServletConfig config) throws ServletException
 	{
 		VirtualPath pagesPath = getPagesPath();
 
 		ServletPage homePage = new HomePage();
 		homePath = pagesPath.registerPage("/", homePage);
 		pagesPath.registerPage("/home", homePage);
 
 		pagesPath.registerPage("/application", new RedirectPage("application", "Application", null));
 		pagesPath.registerPage("/application/dialogs", new AppDialogsPage());
 		pagesPath.registerPage("/application/config", new AppConfigurationPage());
 		pagesPath.registerPage("/application/servlet-context", new AppInitParamsPage());
 		pagesPath.registerPage("/application/acl", new AppAccessControlListPage());
 		pagesPath.registerPage("/application/system-properties", new SystemPropertiesPage());
 
 		pagesPath.registerPage("/application/factory", new AppFactoryPage());
 		pagesPath.registerPage("/application/factory/value-sources", new AppFactoryPage("value-sources", "Value Sources", AppFactoryPage.FACTORY_VALUESOURCE));
 		pagesPath.registerPage("/application/factory/dialog-fields", new AppFactoryPage("dialog-fields", "Dialog Fields", AppFactoryPage.FACTORY_DIALOG_FIELD));
 		pagesPath.registerPage("/application/factory/report-comps", new AppFactoryPage("report-comps", "Report Components", AppFactoryPage.FACTORY_REPORT_COMPS));
 		pagesPath.registerPage("/application/factory/tasks", new AppFactoryPage("tasks", "Tasks", AppFactoryPage.FACTORY_TASK));
 		pagesPath.registerPage("/application/factory/skins", new AppFactoryPage("skins", "Skins", AppFactoryPage.FACTORY_SKIN));
 		pagesPath.registerPage("/application/factory/sql-comparisons", new AppFactoryPage("sql-comparisons", "SQL Comparisons", AppFactoryPage.FACTORY_SQL_COMPARE));
 
 		pagesPath.registerPage("/database", new DatabasePage());
 		pagesPath.registerPage("/database/sql", new DatabaseSqlPage());
 		pagesPath.registerPage("/database/query-defn", new DatabaseQueryDefnPage());
 		pagesPath.registerPage("/database/schema", new DatabaseSchemaDocPage());
 		pagesPath.registerPage("/database/generate-ddl", new DatabaseGenerateDDLPage());
 		pagesPath.registerPage("/database/data-sources", new DataSourcesPage());
 
 		Configuration appConfig = getAppConfig();
 		ValueContext vc = new ServletValueContext(config.getServletContext(), this, null, null);
 
 		pagesPath.registerPage("/application/monitor", new MonitorLogPage());
		Collection logs = appConfig.getValues(vc, "framework.ace.monitor.logs");
 		if(logs != null)
 		{
 			for(Iterator i = logs.iterator(); i.hasNext(); )
 			{
 				Object entry = i.next();
 				if(entry instanceof Property)
 				{
 					Property logProperty = (Property) entry;
 					String logName = logProperty.getName();
 					String logStyle = appConfig.getValue(vc, logProperty, null);
 					MonitorLogPage page = new MonitorLogPage(logName, logStyle);
 					pagesPath.registerPage("/application/monitor/" + page.getName(), page);
 				}
 			}
 		}
 
 		pagesPath.registerPage("/documents", new DocumentsPage());
		Collection bookmarks = appConfig.getValues(vc, "framework.ace.bookmarks");
 		if(bookmarks != null)
 		{
 			for(Iterator i = bookmarks.iterator(); i.hasNext(); )
 			{
 				Object entry = i.next();
 				if(entry instanceof Property)
 				{
 					Property bookmark = (Property) entry;
 					String info = bookmark.getName();
 					String dest = appConfig.getValue(vc, bookmark, null);
 					DocumentsPage page = new DocumentsPage(info, dest);
 					pagesPath.registerPage("/documents/" + page.getName(), page);
 				}
 			}
 		}
 	}
 }
