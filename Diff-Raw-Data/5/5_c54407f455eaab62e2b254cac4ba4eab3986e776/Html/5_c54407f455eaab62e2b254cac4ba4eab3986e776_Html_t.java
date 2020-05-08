 package dolda.jsvc.next;
 
 import org.w3c.dom.*;
 import org.w3c.dom.ls.*;
 import javax.xml.validation.*;
 import java.net.*;
 import java.io.*;
 import dolda.jsvc.*;
 import dolda.jsvc.util.*;
 
 public class Html extends DocBuffer {
     public static final String ns = "http://www.w3.org/1999/xhtml";
     private static final Schema schema = DomUtil.loadxsd("xhtml1-strict.xsd");
 
     private Html(String pubid, String sysid) {
 	super(ns, "html", "html", pubid, sysid);
     }
 
     public static Html xhtml11(String title) {
 	Html buf = new Html("-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
 	Node html = buf.doc.getDocumentElement();
 	Node head = DomUtil.insertel(html, "head");
 	Node tit = DomUtil.insertel(head, "title");
 	head.appendChild(buf.makecursor("head"));
 	DomUtil.inserttext(tit, title);
 	Node body = DomUtil.insertel(html, "body");
 	body.appendChild(buf.makecursor("body"));
 	return(buf);
     }
     
     public Element el(String name, Node contents, String... attrs) {
 	return(el(ns, name, contents, attrs));
     }
     
     public Element csslink(String href, String name) {
 	Element el = el("link", null, "rel=stylesheet", "type=text/css");
 	if(name != null)
 	    el.setAttribute("title", name);
 	el.setAttribute("href", href);
 	return(el);
     }
     
     public void addcss(String href, String name) {
 	insert("head", csslink(href, name));
     }
     
     public void validate() {
	Validator val;
	synchronized(schema) {
	    val = schema.newValidator();
	}
 	try {
 	    val.validate(new javax.xml.transform.dom.DOMSource(doc));
 	} catch(org.xml.sax.SAXException e) {
 	    throw(new RuntimeException(e));
 	} catch(java.io.IOException e) {
 	    /* Should never happen. */
 	    throw(new Error(e));
 	}
     }
     
     private static boolean asxhtml(Request req) {
 	String ah = req.inheaders().get("Accept");
 	AcceptMap ctmap = AcceptMap.parse((ah == null)?"":ah);
 	AcceptMap.Entry ha = ctmap.accepts("text/html");
 	AcceptMap.Entry xa = ctmap.accepts("text/xhtml+xml");
 	if(xa == null)
 	    xa = ctmap.accepts("application/xhtml+xml");
 	if((ha == null) && (xa == null))
 	    return(false);
 	else if((ha != null) && (xa == null))
 	    return(false);
 	else if((ha == null) && (xa != null))
 	    return(true);
 	if(xa.q < ha.q)
 	    return(false);
 	return(true);
     }
 
     public void output(Request req) throws IOException {
 	finalise();
 	validate();
 	XmlWriter w;
 	if(asxhtml(req)) {
 	    req.outheaders().put("Content-Type", "application/xhtml+xml");
 	    w = new XHtmlWriter(doc);
 	} else {
 	    req.outheaders().put("Content-Type", "text/html; charset=utf-8");
 	    w = new HtmlWriter(doc);
 	}
 	w.write(req.output());
     }
 }
