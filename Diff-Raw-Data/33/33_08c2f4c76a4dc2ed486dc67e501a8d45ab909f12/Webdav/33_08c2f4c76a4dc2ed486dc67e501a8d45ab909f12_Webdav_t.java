 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import util.Base64;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerConfigurationException;
 
 /**
 	<p>References:<ul>
 		<li><a href="http://www.webdav.org/specs/rfc4918.html">RFC 4918</a>
 		<li><a href="http://en.wikipedia.org/wiki/Basic_access_authentication">Wikipedia Basic Access Authentication</a>
 		<li><a href="http://www.webdav.org/specs/rfc2518.html">RFC 2518</a> (Obsoleted)
 	</ul>
 		<p><b>TODO</b><ul>
 			<li>Allow realm to be set
 			<li>Create the correct date strings
 		</ul>
 
 */
 public class Webdav implements HTTPServer.Handler {
 	public Webdav() {
 	}
 	public void log(Exception exception) {
 		exception.printStackTrace();
 		log(0, exception.toString());
 	}
 	public void log(int level, String message) {
 		System.err.println("LOG "+level+": "+message);
 	}
 	private Document _parseXML(InputStream xmlStream) throws IOException {
 		try	{
 			DocumentBuilderFactory	factory= DocumentBuilderFactory.newInstance();
 			DocumentBuilder			builder;
 
 			factory.setNamespaceAware(true);
 			builder= factory.newDocumentBuilder();
 			return builder.parse(xmlStream);
 		} catch(ParserConfigurationException exception1) {
 			log(exception1);
 		} catch(SAXException exception2) {
 			log(exception2);
 		}
 		return null;
 	}
 	public boolean handle(InputStream in, OutputStream out, HTTPServer.KeyValuesMap headers, HTTPServer.KeyValuesMap query, HTTPServer.CookieJar cookies) throws IOException {
 		if(!headers.containsKey("Authorization")) {
 			out.write("HTTP/1.1 401 Unauthorized\r\n".getBytes());
 			out.write("Content-Length: 0\r\n".getBytes());
 			out.write("Www-Authenticate: Basic realm=\"WebDav\"\r\n".getBytes());
 			//out.write("Date: Fri, 30 Sep 2011 14:46:06 GMT\r\n".getBytes());
 			out.write("Accept-Ranges: bytes\r\n\r\n".getBytes());
 		} else {
 			byte[]		data= Base64.decode(headers.firstValue("Authorization","").split("\\s+")[1]);
 			String[]	namePassword= (new String(data)).split(":");
 
 			log(100, "Username: "+namePassword[0]);
 			log(100, "Password: "+namePassword[1]);
 
 			if(headers.firstValue("METHOD","").equalsIgnoreCase("OPTIONS")) {
 				out.write("HTTP/1.1 200 OK\r\n".getBytes());
 				out.write("Content-Length: 0\r\n".getBytes());
 				//out.write("Date: Fri, 30 Sep 2011 15:31:45 GMT\r\n".getBytes());
 				out.write("Accept-Ranges: bytes\r\n".getBytes());
 				out.write("DAV: 1\r\n".getBytes()); // 1,2, addressbook
 				out.write("Allow: PROPFIND, DELETE, MKCOL, PUT, MOVE, COPY, PROPPATCH, LOCK, UNLOCK, REPORT\r\n\r\n".getBytes());
 			} else if(headers.firstValue("METHOD","").equalsIgnoreCase("PROPFIND")) {
 				Document			request= _parseXML(in);
 
 				try	{
 					TransformerFactory	tf= TransformerFactory.newInstance();
 					Transformer			t= tf.newTransformer();
 
 					t.setOutputProperty(OutputKeys.INDENT, "yes");
 					t.transform(new DOMSource(request), new StreamResult(System.out));
 				} catch(TransformerException exception1) {
 					exception1.printStackTrace();
 				}
 				if(headers.firstValue("PATH","").equals("/")) {
 					byte[]	contents= ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
 									+"<D:multistatus xmlns:D=\"DAV:\">\r\n"
 									+"<D:response>\r\n"
 									+"<D:href>/bash_history</D:href><D:propstat><D:status>HTTP/1.1 200 OK</D:status><D:prop><D:executable>F</D:executable><D:getcontentlength>16901</D:getcontentlength><D:getlastmodified>2011-09-30 13:16:47 +0000</D:getlastmodified><D:creationdate>2011-09-30 13:16:47 +0000</D:creationdate><D:modificationdate>2011-09-30 13:16:47 +0000</D:modificationdate><D:resourcetype/></D:prop></D:propstat></D:response>\r\n"
 									+"<D:response><D:href>/MacOSX/</D:href><D:propstat><D:status>HTTP/1.1 200 OK</D:status><D:prop><D:getlastmodified>2011-07-08 14:08:57 +0000</D:getlastmodified><D:ishidden>0</D:ishidden><D:getcontenttype>text/plain</D:getcontenttype><D:getcontentlength>0</D:getcontentlength><D:iscollection>1</D:iscollection><D:modificationdate>2011-07-08 14:08:57 +0000</D:modificationdate><D:resourcetype><D:collection/></D:resourcetype></D:prop></D:propstat></D:response>\r\n"
 									+"</D:response></D:multistatus>\r\n").getBytes();
 
 					log(100, "Listng path: "+headers.firstValue("PATH",""));
 					out.write("HTTP/1.1 207 Multi-Status\r\n".getBytes());
 					out.write(("Content-Length: "+contents.length+"\r\n").getBytes());
 					out.write("Date: Fri, 30 Sep 2011 15:50:15 GMT\r\n".getBytes());
 					out.write("Accept-Ranges: bytes\r\n".getBytes());
 					out.write("Content-Type: text/xml; charset=\"utf-8\"\r\n\r\n".getBytes());
 
 					out.write(contents);
 				} else {
 					out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
 					out.write("Content-Length: 0\r\n".getBytes());
 					//out.write("Date: Fri, 30 Sep 2011 15:31:45 GMT\r\n".getBytes());
 					out.write("Accept-Ranges: bytes\r\n".getBytes());
 				}
 			}
 		}
 		System.out.println("Headers");
 		for(String key : headers.keySet()) {
 			String	separator= "";
 
 			System.out.print("\t"+key+"=");
 			for(String value : headers.get(key)) {
 				System.out.print(separator+value);
 				separator= ",";
 			}
 			System.out.println();
 		}
 		System.out.println("Query");
 		for(String key : query.keySet()) {
 			String	separator= "";
 
 			System.out.print("\t"+key+"=");
 			for(String value : query.get(key)) {
 				System.out.print(separator+value);
 				separator= ",";
 			}
 			System.out.println();
 		}
 		System.out.println("Cookies");
 		for(String key : cookies.keySet()) {
 			String	separator= "";
 
 			System.out.print("\t"+key+"=");
 			for(HTTPServer.Cookie value : cookies.get(key)) {
 				System.out.print(separator+value.toString(false));
 				separator= ",";
 			}
 			System.out.println();
 		}
 		byte[]	buffer= new byte[4096];
 		int		read= in.read(buffer);
 
 		while(read > 0) {
 			System.out.write(buffer, 0, read);
 			read= in.read(buffer);
 		}
 		return true;
 	}
 	private Pattern	_authenticationPattern= Pattern.compile("Authorization:\\s+Basic\\s+(\\S+)\\s+");
 	private Pattern _propfindPattern= Pattern.compile("PROPFIND\\s+(.*)\\s+HTTP/1.1\\s+");
 	public void handle(SocketServer server, Socket connection) throws IOException {
 		System.out.println("Handling new connection on port "+server.port()+" from "+connection);
 		InputStream		in= connection.getInputStream();
 		OutputStream	out= connection.getOutputStream();
 		byte[]			buffer= new byte[4096];
 		int				read= in.read(buffer);
 		while(read >= 0) {
 			String			text= new String(buffer, 0, read);
 			Matcher			isAuthenticating= _authenticationPattern.matcher(text);
 
 			if(isAuthenticating.find()) {
 				String	authentication= new String(Base64.decode(isAuthenticating.group(1)));
 				Matcher	isPropFind= _propfindPattern.matcher(text);
 
 				System.out.println("Authenticating: "+authentication);
 
 				if(isPropFind.find()) {
 					byte[]	contents= ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
 									+"<D:multistatus xmlns:D=\"DAV:\">\r\n"
 									+"<D:response>\r\n"
 									+"<D:href>/bash_history</D:href><D:propstat><D:status>HTTP/1.1 200 OK</D:status><D:prop><D:executable>F</D:executable><D:getcontentlength>16901</D:getcontentlength><D:getlastmodified>2011-09-30 13:16:47 +0000</D:getlastmodified><D:creationdate>2011-09-30 13:16:47 +0000</D:creationdate><D:modificationdate>2011-09-30 13:16:47 +0000</D:modificationdate><D:resourcetype/></D:prop></D:propstat></D:response>\r\n"
 									+"<D:response><D:href>/MacOSX/</D:href><D:propstat><D:status>HTTP/1.1 200 OK</D:status><D:prop><D:getlastmodified>2011-07-08 14:08:57 +0000</D:getlastmodified><D:ishidden>0</D:ishidden><D:getcontenttype>text/plain</D:getcontenttype><D:getcontentlength>0</D:getcontentlength><D:iscollection>1</D:iscollection><D:modificationdate>2011-07-08 14:08:57 +0000</D:modificationdate><D:resourcetype><D:collection/></D:resourcetype></D:prop></D:propstat></D:response>\r\n"
 									+"</D:response></D:multistatus>\r\n").getBytes();
 
 					out.write("HTTP/1.1 207 Multi-Status\r\n".getBytes());
 					out.write(("Content-Length: "+contents.length+"\r\n").getBytes());
 					out.write("Date: Fri, 30 Sep 2011 15:50:15 GMT\r\n".getBytes());
 					out.write("Accept-Ranges: bytes\r\n".getBytes());
 					out.write("Content-Type: text/xml; charset=\"utf-8\"\r\n\r\n".getBytes());
 
 					out.write(contents);
 
 				} else {
 					out.write("HTTP/1.1 200 OK\r\n".getBytes());
 					out.write("Content-Length: 0\r\n".getBytes());
 					out.write("Date: Fri, 30 Sep 2011 15:31:45 GMT\r\n".getBytes());
 					out.write("Accept-Ranges: bytes\r\n".getBytes());
 					out.write("DAV: 1,2, addressbook\r\n".getBytes());
 					out.write("Allow: PROPFIND, DELETE, MKCOL, PUT, MOVE, COPY, PROPPATCH, LOCK, UNLOCK, REPORT\r\n\r\n".getBytes());
 				}
 			} else {
 				System.out.println("No Authentication");
 				out.write("HTTP/1.1 401 Unauthorized\r\n".getBytes());
 				out.write("Content-Length: 0\r\n".getBytes());
 				out.write("Www-Authenticate: Basic realm=\"defaultRealm@host.com\"\r\n".getBytes());
 				out.write("Date: Fri, 30 Sep 2011 14:46:06 GMT\r\n".getBytes());
 				out.write("Accept-Ranges: bytes\r\n\r\n".getBytes());
 			}
 			System.out.write(buffer, 0, read);
 			read= in.read(buffer);
 		}
 	}
 	public static void main(String... args) {
 		try	{
 			new SocketServer(Integer.parseInt(args[0]), new HTTPServer(new Webdav()));
 			//new SocketServer(Integer.parseInt(args[0]), new Webdav());
 		} catch(IOException exception) {
 			System.err.println(exception);
 		}
 	}
 }
 
 /*
 
 	Safari connection
 GET / HTTP/1.1
 Host: localhost:8086
 User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_1) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3
 Accept: text/html,application/xhtml+xml,application/xml;q=0.9,* /*;q=0.8
 Accept-Language: en-us
 Accept-Encoding: gzip, deflate
 Connection: keep-alive
 
 	Finder Connection
 
 Headers
 	MINOR-VERSION=1
 	VERSION=1.1
 	Host=localhost:8086
 	HTTP-VERSION=HTTP/1.1
 	Content-Length=0
 	REQUEST-URI=/
 	PATH=/
 	MAJOR-VERSION=1
 	User-Agent=WebDAVFS/1.9.0 (01908000) Darwin/11.1.0 (i386)
 	METHOD=OPTIONS
 	Connection=keep-alive
 	Accept=* /*
 
 Query
 Cookies
 */
 
 /* Session
 
 Request
 -------
 OPTIONS / HTTP/1.1
 Host: localhost:8086
 User-Agent: WebDAVLib/1.3
 Content-Length: 0
 Accept: * /*
 Connection: close
 
 Response
 --------
 HTTP/1.1 401 Unauthorized
 Content-Length: 0
 Www-Authenticate: Basic realm="defaultRealm@host.com"
 Date: Fri, 30 Sep 2011 14:46:06 GMT
 Accept-Ranges: bytes
 
 Request - username: marcp password: [blank]
 -------
 OPTIONS / HTTP/1.1
 Host: localhost:8043
 User-Agent: WebDAVLib/1.3
 Content-Length: 0
 Accept: * /*
 Authorization: Basic bWFyY3A6
 Connection: close
 
 Response
 --------
 HTTP/1.1 200 OK
 Content-Length: 0
 Date: Fri, 30 Sep 2011 15:31:45 GMT
 Accept-Ranges: bytes
 DAV: 1,2, addressbook
 Allow: PROPFIND, DELETE, MKCOL, PUT, MOVE, COPY, PROPPATCH, LOCK, UNLOCK, REPORT
 
 Request
 -------
 PROPFIND / HTTP/1.1
 Host: localhost:8051
 User-Agent: WebDAVFS/1.9.0 (01908000) Darwin/11.1.0 (i386)
 Content-Length: 179
 Accept: * /*
 Content-Type: text/xml
 Depth: 0
 Authorization: Basic bWFyY3A6
 Connection: keep-alive
 
 <?xml version="1.0" encoding="utf-8"?>
 <D:propfind xmlns:D="DAV:">
 <D:prop>
 <D:getlastmodified/>
 <D:getcontentlength/>
 <D:creationdate/>
 <D:resourcetype/>
 </D:prop>
 </D:propfind>
 
 Response
 --------
 HTTP/1.1 207 Multi-Status
 Content-Length: 13552
 Date: Fri, 30 Sep 2011 15:50:15 GMT
 Accept-Ranges: bytes
 Content-Type: text/xml; charset="utf-8"
 
 <?xml version="1.0" encoding="utf-8"?>
 <D:multistatus xmlns:D="DAV:">
 	<D:response>
 		<D:href>/bash_history</D:href>
 		<D:propstat>
 			<D:status>HTTP/1.1 200 OK</D:status>
 			<D:prop>
 				<D:executable>F</D:executable>
 				<D:getcontentlength>16901</D:getcontentlength>
 				<D:getlastmodified>2011-09-30 13:16:47 +0000</D:getlastmodified>
 				<D:creationdate>2011-09-30 13:16:47 +0000</D:creationdate>
 				<D:modificationdate>2011-09-30 13:16:47 +0000</D:modificationdate>
 				<D:resourcetype/>
 			</D:prop>
 		</D:propstat>
 	</D:response>
 	<D:response>
 		<D:href>/MacOSX/</D:href>
 		<D:propstat>
 			<D:status>HTTP/1.1 200 OK</D:status>
 			<D:prop>
 				<D:getlastmodified>2011-07-08 14:08:57 +0000</D:getlastmodified>
 				<D:ishidden>0</D:ishidden>
 				<D:getcontenttype>text/plain</D:getcontenttype>
 				<D:getcontentlength>0</D:getcontentlength>
 				<D:iscollection>1</D:iscollection>
 				<D:modificationdate>2011-07-08 14:08:57 +0000</D:modificationdate>
 				<D:resourcetype>
 					<D:collection/>
 				</D:resourcetype>
 			</D:prop>
 		</D:propstat>
 	</D:response>
 </D:multistatus>
 
 Headers
 	Host=localhost:8050
 	Content-Length=175
 	PATH=/
 	MAJOR-VERSION=1
 	User-Agent=WebDAVFS/1.9.0 (01908000) Darwin/11.1.0 (i386)
 	Depth=0
 	Connection=keep-alive
 	MINOR-VERSION=1
 	Authorization=Basic TWFyYzphZnNkZmE=
 	VERSION=1.1
 	HTTP-VERSION=HTTP/1.1
 	REQUEST-URI=/
 	METHOD=PROPFIND
 	Accept=* /*
 	Content-Type=text/xml
 Query
 Cookies
 <?xml version="1.0" encoding="utf-8"?>
 <D:propfind xmlns:D="DAV:">
 <D:prop>
 <D:quota-available-bytes/>
 <D:quota-used-bytes/>
 <D:quota/>
 <D:quotaused/>
 </D:prop>
 </D:propfind>
 
 HTTP/1.1 207 Multi-Status
 Content-Length: 13552
 Date: Fri, 30 Sep 2011 17:43:25 GMT
 Accept-Ranges: bytes
 Content-Type: text/xml; charset="utf-8"
 
 <?xml version="1.0" encoding="utf-8"?>
 <D:multistatus xmlns:D="DAV:">
 	<D:response>
 		<D:href>/bash_history</D:href>
 		<D:propstat>
 			<D:status>HTTP/1.1 200 OK</D:status>
 			<D:prop>
 				<D:executable>F</D:executable>
 				<D:getcontentlength>17478</D:getcontentlength>
 				<D:getlastmodified>2011-09-30 16:05:46 +0000</D:getlastmodified>
 				<D:creationdate>2011-09-30 16:05:46 +0000</D:creationdate>
 				<D:modificationdate>2011-09-30 16:05:46 +0000</D:modificationdate>
 				<D:resourcetype/>
 			</D:prop>
 		</D:propstat>
 	</D:response>
 	<D:response>
 		<D:href>/MacOSX/</D:href>
 		<D:propstat>
 			<D:status>HTTP/1.1 200 OK</D:status>
 			<D:prop>
 				<D:getlastmodified>2011-07-08 14:08:57 +0000</D:getlastmodified>
 				<D:ishidden>0</D:ishidden>
 				<D:getcontenttype>text/plain</D:getcontenttype>
 				<D:getcontentlength>0</D:getcontentlength>
 				<D:iscollection>1</D:iscollection>
 				<D:modificationdate>2011-07-08 14:08:57 +0000</D:modificationdate>
 				<D:resourcetype>
 					<D:collection/>
 				</D:resourcetype>
 			</D:prop>
 		</D:propstat>
 	</D:response>
 </D:multistatus>
 
 */
