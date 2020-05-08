 package core;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class Dispatcher extends HttpServlet {
 	private static final long serialVersionUID = -1399448137949576706L;
 	private Map<String, Handler> handlers = new HashMap<String, Handler>();
 	
 	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Action action = Action.create(request.getPathInfo());
 		try {
 			Handler handler = handlers.get(action.getVerb());
 			if(handler == null){
 				@SuppressWarnings("unchecked")
 				Class<Handler> clazz = (Class<Handler>) Class.forName("handler."+action.getVerb()+"Handler");
 				Constructor<Handler> constr = clazz.getDeclaredConstructor();
 				handler = constr.newInstance();
 				handlers.put(action.getVerb(), handler);
 			}	
 			handler.serve(action, request, response);
 		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Could not find handler");
			return;
 		}
 	}
 
 }
