 package net.styleguise.converge;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import redstone.xmlrpc.XmlRpcServer;
 import redstone.xmlrpc.interceptors.DebugInvocationInterceptor;
 
 @SuppressWarnings("serial")
 public class ConvergeServlet extends HttpServlet {
 	
 	private ConvergeService convergeService;
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		
 		super.init(config);
 		
		String delegateClass = config.getInitParameter("covergeServiceDelegateClass");
 		if( delegateClass == null )
			throw new ServletException("Init parameter [covergeServiceDelegateClass] must be specified");
 		
 		try{
 			Class<?> clazz = Class.forName(delegateClass);
 			Object obj = clazz.newInstance();
 			if( obj instanceof ConvergeServiceDelegate )
 				convergeService = new ConvergeService((ConvergeServiceDelegate)obj);
 			else
				throw new ServletException("The [covergeServiceDelegateClass] must be a subclass of " + ConvergeServiceDelegate.class.getName());
 		}
 		catch(InstantiationException e){
 			throw new ServletException("Unable to instantiate " + delegateClass + ". It should have a non-private, no-arg constructor", e);
 		}
 		catch(IllegalAccessException e){
 			throw new ServletException("Unable to instantiate " + delegateClass + ". It should have a non-private, no-arg constructor", e);
 		}
 		catch(ClassNotFoundException e){
 			throw new ServletException("Unable to find class named " + delegateClass, e);
 		}
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		if( req.getParameter("info") != null ){
 			String xml = convergeService.convergeInfoXml();
 			resp.setContentType("text/xml");
 			resp.getWriter().write(xml);
 			resp.getWriter().flush();
 		}
 		else if( req.getParameter("postlogin") != null ){
 //			String sessionId = req.getParameter("session_id");
 //			int memberId = Integer.parseInt(req.getParameter("member_id"));
 //			int productId = Integer.parseInt(req.getParameter("product_id"));
 //			String key = req.getParameter("key");
 			//TODO log user in
 		}
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		StringWriter buffer = new StringWriter(2048);
 		XmlRpcServer xmlRpcServer = new XmlRpcServer();
 		xmlRpcServer.addInvocationHandler("ipConverge", convergeService);
 		xmlRpcServer.addInvocationInterceptor(new DebugInvocationInterceptor(getServletContext()));
 		xmlRpcServer.execute(req.getInputStream(), buffer);
 		
 		String response = buffer.toString();
 		resp.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/xml");
 		resp.setContentLength(response.getBytes("UTF-8").length);
 		resp.getWriter().write(response);
 		resp.getWriter().flush();
 	}
 }
