 package dolda.jsvc.util;
 
 import dolda.jsvc.*;
 import java.net.*;
 import java.io.*;
 import java.util.logging.*;
 
 public class ErrorHandler implements Responder {
     private Responder next;
     private static Logger logger = Logger.getLogger("dolda.jsvc.context");
     
     public ErrorHandler(Responder next) {
 	this.next = next;
     }
     
     protected void log(Request req, Throwable t) {
 	logger.log(Level.SEVERE, "Unhandled error in responder", t);
     }
     
     protected void respdebug(Request req, Throwable t) {
 	req.status(500);
 	req.outheaders().put("content-type", "text/plain; charset=utf-8");
 	PrintWriter out;
 	try {
 	    out = new PrintWriter(new OutputStreamWriter(req.output(), "UTF-8"));
 	} catch(UnsupportedEncodingException e) {
 	    throw(new Error(e));
 	}
 	t.printStackTrace(out);
 	out.flush();
     }
     
     protected void resperr(Request req, Throwable t) {
 	req.status(500);
 	req.outheaders().put("content-type", "text/html; charset=us-ascii");
 	PrintWriter out;
 	try {
 	    out = new PrintWriter(new OutputStreamWriter(req.output(), "US-ASCII"));
 	} catch(UnsupportedEncodingException e) {
 	    throw(new Error(e));
 	}
 	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 	out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
 	out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\">");
 	out.println("<head><title>Internal error</title></head>");
 	out.println("<body>");
 	out.println("<h1>Internal error</h1>");
 	out.println("An error occurred on the server processing your request.");
 	out.println("</body>");
 	out.println("</html>");
 	out.flush();
     }
     
     protected boolean debug(Request req, Throwable t) {
 	SocketAddress rem = req.remoteaddr();
 	return((rem instanceof InetSocketAddress) && ((InetSocketAddress)rem).getAddress().isLoopbackAddress());
     }
 
     protected void handle(Request req, Throwable t) {
 	log(req, t);
 	if(req instanceof ResettableRequest) {
 	    ResettableRequest rr = (ResettableRequest)req;
 	    if(rr.canreset())
 		rr.reset();
 	}
 	if(debug(req, t))
 	    respdebug(req, t);
 	else
 	    resperr(req, t);
     }
     
     public void respond(Request req) {
 	try {
 	    next.respond(req);
 	} catch(Throwable t) {
 	    handle(req, t);
 	}
     }
 }
