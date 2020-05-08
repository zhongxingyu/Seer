 package com.choas.jsvalidator.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
import validate.*;

 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 // http://wiki.eclipse.org/Jetty/Tutorial/Jetty_HelloWorld
 // https://sites.google.com/site/gson/gson-user-guide#TOC-Object-Examples
 // http://download.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 
 public class JsValidationHandler extends AbstractHandler {
 
 	public void handle(String target, Request baseRequest,
 			HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 
 		if (target.equals("/favicon.ico")) {
 			response.setContentType("image/x-icon");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 
 			// http://www.favicon.cc/?action=icon&file_id=33004
 			String faviconBase64 = "AAABAAEAEBAAAAAAAABoBQAAFgAAACgAAAAQAAAAIAAAAAEACAAAAAAAAAEAAAAAAAAAAAAAAAEAAAAAAAAAAAAAzMzMAOjo6AD///8AZmZmAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAQEBAQEBAQAAAAAAAAABAEBAQEBAQEBBAAAAAAAAAQBAgICAgICAQQAAAAAAAAEAQICAgICAgEEBAQEAAAABAECAgICAgIBBAEBBAAAAAQBAgICAgICAQQDAQQAAAAEAQICAgICAgEEAQEEAAAABAEBAQEBAQEBBAQEBAAAAAQEBAQEBAQEBAQAAAAAAAAAAQEBAQEBAQEAAAAAAAAAAAABAQEBAQEBAAAAAAAAAAAAAQABAQAAAQAAAAAAAAAAAAEAAQEAAAEAAAAAAAAAAAAAAAEAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAP//AADAPwAAgB8AAIAfAACAAwAAgAMAAIADAACAAwAAgAMAAIAfAADAPwAA4D8AAOm/AADpvwAA+78AAPv/AAA=";
 			System.out.println("favicon");
 
 			response.getWriter().println(
 					org.eclipse.jetty.http.security.B64Code
 							.decode(faviconBase64));
 			return;
 		}
 		if (target.toLowerCase().indexOf("json") >= 0) {
 			response.setContentType("application/json;charset=utf-8");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 
 			List<String> validateMsgs = new ArrayList<String>();
 			try {
 				Gson gson = new Gson();
 				Person person = 
 					gson.fromJson(request.getReader(), Person.class);
 				validate(person, validateMsgs);
 			} catch (JsonParseException e) {
 				e.printStackTrace();
 				validateMsgs.add("SYSTEM " + e.getCause().getMessage());
 			}
 
 			Gson gsonValidateMsgs = new Gson();
 			String jsonResponse = gsonValidateMsgs.toJson(validateMsgs);
 			System.out.println(jsonResponse);
 			response.getWriter().println(jsonResponse);
 			return;
 		}
 
 		if (target.equals("/")) {
 			response.setContentType("text/html;charset=utf-8");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 			InputStream is = this.getClass().getResourceAsStream("/index.html");
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line;
 			while ((line = br.readLine()) != null) {
 				response.getWriter().println(line);
 			}
 			is.close();
 			return;
 		}
 		if (target.toLowerCase().lastIndexOf(".js") > 0) {
 			System.out.println("load javascript " + target);
 			response.setContentType("text/javascript");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 
			InputStream is = this.getClass().getResourceAsStream(target);
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line;
 			while ((line = br.readLine()) != null) {
 				response.getWriter().println(line);
 			}
 			is.close();
 			return;
 		}
 		System.out.println("UNKNOWN TARGET: '" + target + "'");
 		response.setContentType("application/json;charset=utf-8");
 		response.setStatus(HttpServletResponse.SC_OK);
 		baseRequest.setHandled(true);
 
 		Gson gson = new Gson();
 		List<String> m = new ArrayList<String>();
 		m.add("SYSTEM unknown target '" + target + "'");
 		String jsonResponse = gson.toJson(m);
 		System.out.println(jsonResponse);
 		response.getWriter().println(jsonResponse);
 	}
 
 	private void validate(Person person, List<String> validateMsgs) {
 		System.out.println("js validate");
 		JsValidation jv = new JsValidation();
 		//jv.runWithDebug(person, validateMsgs);
 		jv.run(person, validateMsgs);
 	}
 
 	public static void main(String[] args) throws Exception {
 		Server server = new Server(8080);
 		server.setHandler(new JsValidationHandler());
 
 		server.start();
 		server.join();
 	}
 }
