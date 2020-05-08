 package dolda.jsvc.j2ee;
 
 import dolda.jsvc.*;
 import dolda.jsvc.util.*;
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class J2eeRequest extends ResponseBuffer {
     private ServletConfig cfg;
     private HttpServletRequest req;
     private HttpServletResponse resp;
     private String method, path;
     private URL url, context;
     private MultiMap<String, String> params = null;
     private Map<Object, Object> props = new HashMap<Object, Object>();
     
     public J2eeRequest(ServletConfig cfg, HttpServletRequest req, HttpServletResponse resp) {
 	this.cfg = cfg;
 	this.req = req;
 	this.resp = resp;
 	{
 	    /* Ewwww, this is disgusting! */
 	    String scheme = req.getScheme();
 	    int port = -1;
 	    String host = req.getHeader("Host");
 	    if((host == null) || (host.length() < 1)) {
 		host = req.getLocalAddr();
 		port = req.getLocalPort();
 		if((port == 80) && scheme.equals("http"))
 		    port = -1;
 		else if((port == 443) && scheme.equals("https"))
 		    port = -1;
 	    } else {
 		int p;
 		if((host.charAt(0) == '[') && ((p = host.indexOf(']', 1)) > 1)) {
 		    String newhost = host.substring(1, p);
 		    if((p = host.indexOf(':', p + 1)) >= 0) {
 			try {
 			    port = Integer.parseInt(host.substring(p + 1));
 			} catch(NumberFormatException e) {}
 		    }
 		    host = newhost;
 		} else if((p = host.indexOf(':')) >= 0) {
 		    try {
 			port = Integer.parseInt(host.substring(p + 1));
 			host = host.substring(0, p);
 		    } catch(NumberFormatException e) {}
 		}
 	    }
 	    String pi = req.getPathInfo();
 	    if(pi == null)
 		pi = "";
 	    String q = req.getQueryString();
 	    if(q != null)
 		q = "?" + q;
 	    else
 		q = "";
 	    try {
 		url = new URL(scheme, host, port, req.getContextPath() + req.getServletPath() + pi + q);
 		context = new URL(scheme, host, port, req.getContextPath());
 	    } catch(MalformedURLException e) {
 		throw(new Error(e));
 	    }
 	}
 	method = req.getMethod().toUpperCase().intern();
 	path = req.getPathInfo();
 	while((path.length() > 0) && (path.charAt(0) == '/'))
 	    path = path.substring(1);
     }
     
     public Map<Object, Object> props() {
 	return(props);
     }
     
     public SocketAddress remoteaddr() {
 	try {
	    /* Apparently getRemotePort returns -1 when running on Tomcat over AJP. */
	    int port = req.getRemotePort();
	    if(port < 0)
		port = 0;
	    return(new InetSocketAddress(InetAddress.getByName(req.getRemoteAddr()), port));
 	} catch(UnknownHostException e) {
 	    /* req.getRemoteAddr should always be a valid IP address,
 	     * so this should never happen. */
 	    throw(new Error(e));
 	}
     }
     
     public SocketAddress localaddr() {
 	try {
 	    return(new InetSocketAddress(InetAddress.getByName(req.getLocalAddr()), req.getLocalPort()));
 	} catch(UnknownHostException e) {
 	    /* req.getRemoteAddr should always be a valid IP address,
 	     * so this should never happen. */
 	    throw(new Error(e));
 	}
     }
     
     public URL url() {
 	return(url);
     }
     
     public URL rooturl() {
 	return(context);
     }
     
     public ServerContext ctx() {
 	return(ThreadContext.current().server());
     }
     
     public String method() {
 	return(method);
     }
     
     public String path() {
 	return(path);
     }
 
     public InputStream input() {
 	try {
 	    return(req.getInputStream());
 	} catch(IOException e) {
 	    /* It is not obvious why this would happen, so I'll wait
 	     * until I know whatever might happen to try and implement
 	     * meaningful behavior. */
 	    throw(new RuntimeException(e));
 	}
     }
 
     public MultiMap<String, String> inheaders() {
 	MultiMap<String, String> h = new HeaderTreeMap();
 	Enumeration ki = req.getHeaderNames();
 	if(ki != null) {
 	    while(ki.hasMoreElements()) {
 		String k = (String)ki.nextElement();
 		Enumeration vi = req.getHeaders(k);
 		if(vi != null) {
 		    while(vi.hasMoreElements()) {
 			String v = (String)vi.nextElement();
 			h.add(k, v);
 		    }
 		}
 	    }
 	}
 	return(h);
     }
     
     public MultiMap<String, String> params() {
 	if(params == null) {
 	    params = Params.urlparams(this);
 	    if(method == "POST") {
 		MultiMap<String, String> pp = Params.postparams(this);
 		if(pp != null)
 		    params.putAll(pp);
 	    }
 	}
 	return(params);
     }
     
     protected void backflush() {
 	resp.setStatus(respcode);
 	for(String key : outheaders().keySet()) {
 	    boolean first = true;
 	    for(String val : outheaders().values(key)) {
 		if(first) {
 		    resp.setHeader(key, val);
 		    first = false;
 		} else {
 		    resp.addHeader(key, val);
 		}
 	    }
 	}
     }
     
     protected OutputStream realoutput() {
 	try {
 	    return(resp.getOutputStream());
 	} catch(IOException e) {
 	    /* It is not obvious why this would happen, so I'll wait
 	     * until I know whatever might happen to try and implement
 	     * meaningful behavior. */
 	    throw(new RuntimeException(e));
 	}
     }
 }
