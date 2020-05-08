 package tw.elliot.info;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class SystemInfoServlet
  */
 @WebServlet(name = "systemInfo", urlPatterns = { "/systemInfo" })
 public class SystemInfoServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default constructor.
 	 */
 	public SystemInfoServlet() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HashMap<Object, Object> map = new HashMap<Object, Object>();
 		ServletContext application = getServletConfig().getServletContext();
 
 		Manifest manifest = new Manifest(application.getResourceAsStream("/META-INF/MANIFEST.MF"));
 		Attributes attr = manifest.getMainAttributes();
 		for (Object key : attr.keySet()) {
 			map.put(key, attr.get(key));
 		}
 
 		request.setAttribute("infoMap", map);
 		request.getRequestDispatcher("/SystemInfo.jsp").forward(request,response);
 	}
 
 }
