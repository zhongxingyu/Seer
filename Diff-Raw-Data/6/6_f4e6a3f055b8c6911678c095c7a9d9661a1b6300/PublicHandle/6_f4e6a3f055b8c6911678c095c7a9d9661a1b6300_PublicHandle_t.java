 package com.emergentideas.page.editor.handles;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.emergentideas.logging.Logger;
 import com.emergentideas.logging.SystemOutLogger;
 import com.emergentideas.webhandle.handlers.Handle;
import com.emergentideas.webhandle.output.NoResponse;
 
 public class PublicHandle {
 	
 	protected Logger log = SystemOutLogger.get(PublicHandle.class);
 	protected String forwardLocation = "/index.html";
 	
 	
 	@Handle({"", "/"})
	public Object index(HttpServletResponse response, HttpServletRequest request) {
 		try {
 			request.getRequestDispatcher(forwardLocation).forward(request, response);
 		}
 		catch(Exception e) {
 			log.error("Could not forward to index.", e);
 		}
		return new NoResponse();
 	}
 
 
 	public Logger getLog() {
 		return log;
 	}
 	public void setLog(Logger log) {
 		this.log = log;
 	}
 	public String getForwardLocation() {
 		return forwardLocation;
 	}
 	public void setForwardLocation(String forwardLocation) {
 		this.forwardLocation = forwardLocation;
 	}
 }
