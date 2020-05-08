 package jsclassloader.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import jsclassloader.Bundler;
 import jsclassloader.Config;
 import jsclassloader.dependency.DependencyGraph;
 
 @SuppressWarnings("serial")
 public class JsClassVisualizerServlet extends HttpServlet {
 
 	private Config jsClassLoaderConfig;
 	private String propFileName = "js-class-loader.properties";
 	
 	@Override
 	public void init(final ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			jsClassLoaderConfig = new Config(
 				this.getClass().getClassLoader().getResourceAsStream(propFileName));
 		}
 		catch(IOException ioe) {
 			throw new ServletException(
 					"JSClassLoader servlet can't load properties file '" + propFileName + 
 					"' from classpath\n" + 
 					"message: " + ioe.getMessage());
 		}
 	}
 
 	@Override
 	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
 		try {
			Bundler bundler = new Bundler(jsClassLoaderConfig);
 			response.setContentType("text/plain");
			response.getWriter().print(bundler.getDependencyGraph().renderDotFile(bundler.getSeedClassNameList()));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
