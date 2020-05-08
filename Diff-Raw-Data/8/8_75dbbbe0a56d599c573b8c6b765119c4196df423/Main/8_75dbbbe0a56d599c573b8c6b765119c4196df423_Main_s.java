 /*******************************************************************************
  * This file is part of MPAF.
  * 
  * MPAF is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MPAF is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with MPAF.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package mpaf;
 
 import java.sql.SQLException;
 
 import mpaf.ice.IceController;
 import mpaf.ice.IceModel;
 import mpaf.servlets.ChannelList;
 import mpaf.servlets.DefaultCacheServlet;
 import mpaf.servlets.HandlerList;
 import mpaf.servlets.Login;
 import mpaf.servlets.Logout;
 import mpaf.servlets.ServerList;
 import mpaf.servlets.ServerManage;
 import mpaf.servlets.UserCreate;
 import mpaf.servlets.UserInfo;
 import mpaf.servlets.UserList;
 import mpaf.sql.SqlHandler;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.bio.SocketConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 public class Main {
 	public static void main(String[] args) {
 		// Apache commons configuration
 		CompositeConfiguration config = new CompositeConfiguration();
 		try {
 
 			XMLConfiguration user = new XMLConfiguration(
 					"mpaf.properties.user.xml");
 			XMLConfiguration defaults = new XMLConfiguration(
 					"mpaf.properties.default.xml");
 
 			// careful configuration is read from top to bottom if you want a
 			// config to overwrite the user config, add it as first element
 			// also make it optional to load, check if the file exists and THEN
 			// load it!
 			if (user != null)
 				config.addConfiguration(user);
 			config.addConfiguration(defaults);
 		} catch (ConfigurationException e1) {
 			e1.printStackTrace();
 		}
 
 		SqlHandler sqlH = null;
 		sqlH = new SqlHandler();
 		sqlH.setDbtype(config.getString("db.type"));
 		sqlH.setDbhost(config.getString("db.host"));
 		sqlH.setDbport(config.getString("db.port"));
 		sqlH.setDbname(config.getString("db.name"));
 		sqlH.setDbuser(config.getString("db.user"));
 		sqlH.setDbpass(config.getString("db.password"));
		Logger.debug(SqlHandler.class, "Db connection: "+config.getString("db.type")+config.getString("db.host")+config.getString("db.port")+config.getString("db.password"));
 		IceModel iceM = new IceModel(config);
 		IceController iceC;
 		try {
 			iceC = new IceController(iceM, sqlH.getConnection());
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return;
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return;
 		}
 
 		Server server = new Server();
 
 		ConsoleParser parser = new ConsoleParser(iceC, iceM, server);
 		new Thread(parser).start();
 
 		// will be called once a shutdown event is thrown(like ctrl+c or sigkill
 		// etc.)
 		ShutdownThread shutdown = new ShutdownThread(iceC);
 		Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
 		
 		if(config.getBoolean("jetty.enabled")) {
 			SocketConnector connector = new SocketConnector();
 			connector.setPort(config.getInt("jetty.ports.http", 10001));
 			server.setConnectors(new Connector[] { connector });
 	
 			ServletContextHandler servletC = new ServletContextHandler(
 					ServletContextHandler.SESSIONS);
 			servletC.setContextPath("/");
 			servletC.setAttribute("sqlhandler", sqlH);
 			servletC.setAttribute("iceController", iceC);
 			servletC.setAttribute("iceModel", iceM);
 			// To add a servlet:
 			ServletHolder holder = new ServletHolder(new DefaultCacheServlet());
 			holder.setInitParameter("cacheControl", "max-age=3600,public");
 			holder.setInitParameter("resourceBase", "web");
 			servletC.addServlet(holder, "/");
 			servletC.addServlet(new ServletHolder(new ServerList()), "/serverlist");
 			servletC.addServlet(new ServletHolder(new ChannelList()),
 					"/channellist");
 			servletC.addServlet(new ServletHolder(new HandlerList()),
 					"/handlerlist");
 			servletC.addServlet(new ServletHolder(new ServerManage()),
 					"/servermanage");
 			servletC.addServlet(new ServletHolder(new Login()), "/login");
 			servletC.addServlet(new ServletHolder(new Logout()), "/logout");
 			servletC.addServlet(new ServletHolder(new UserCreate()), "/usercreate");
 			servletC.addServlet(new ServletHolder(new UserInfo()), "/userinfo");
 			servletC.addServlet(new ServletHolder(new UserList()), "/userlist");
 	
 			server.setHandler(servletC);
 			try {
 				server.start();
 				server.join();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 }
