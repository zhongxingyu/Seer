 package net.sourceforge.squirrel_sql.plugins.sqlscript;
 
 import net.sourceforge.squirrel_sql.client.session.ISession;
 import net.sourceforge.squirrel_sql.client.session.ISQLPanelAPI;
 import net.sourceforge.squirrel_sql.client.session.IObjectTreeAPI;
 
 
 /**
  * This class was introduced to make the plugin compilable for the time it takes
  * to completely introduce the multible session windows framework.
  * It may be removed after that.
  */
 public class FrameWorkAcessor
 {
    public static ISQLPanelAPI getSQLPanelAPI(ISession session, SQLScriptPlugin plugin)
    {
      return session.getSQLPanelAPI(plugin);
      //return session.getSessionSheet().getSQLPaneAPI();
    }
 
    public static IObjectTreeAPI getObjectTreeAPI(ISession session, SQLScriptPlugin sqlScriptPlugin)
    {
      return session.getObjectTreeAPI(sqlScriptPlugin);
      //return session.getSessionSheet().getObjectTreePanel();
    }
 }
