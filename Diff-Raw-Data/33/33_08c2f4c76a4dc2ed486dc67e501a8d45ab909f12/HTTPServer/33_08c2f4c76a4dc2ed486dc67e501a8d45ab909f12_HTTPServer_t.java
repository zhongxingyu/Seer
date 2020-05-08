 import java.net.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 
 /** Handler for http connections to a SocketServer.
 	<p><b>TODO</b>
 	<ul>
 		<li>handle file upload (and the encoding that goes with it)<ul>
 				<li> Add to Handler interface: void handleFile(InputStream file, KeyValueMap headers);
 				<li> Create an InputStream subclass for the file
 		</ul>
 		<li>handle Connection: Keep-Alive/close in the request and response
 		<li>handle compression<ul>
 			<li>Request: Accept-Encoding: gzip
 			<li>Response: Content-Encoding: gzip
 			<li>java.util.zip.GZIPOutputStream
 			<li>java.util.zip.GZIPInputStream
 		</ul>
 	</ul>
 */
 public class HTTPServer implements SocketServer.Handler {
 	/** Handles Cookie representations.
 	*/
 	static public class Cookie {
 		/** Creates a cookie from an http request string
 			@param cookieString	The value of the Set-Cookie http header
 		*/
 		public Cookie(String cookieString) {
 			String[]	parts= cookieString.split(";");
 			boolean		firstField= true;
 
 			secure= false;
 			name= null;
 			value= null;
 			expires= null;
 			domain= null;
 			path= null;
 			for(String part : parts) {
 				String[]	namevalue= part.split("=", 2);
 
 				if(firstField) {
 					firstField= false;
 					name= urlDecode(namevalue[0].trim());
 					value= urlDecode(namevalue[1].trim());
 				} else {
 					if(namevalue.length == 2) {
 						if(namevalue[0].trim().equalsIgnoreCase("domain")) {
 							domain= urlDecode(namevalue[1].trim());
 						} else if(namevalue[0].trim().equalsIgnoreCase("path")) {
 							path= urlDecode(namevalue[1].trim());
 						} else if(namevalue[0].trim().equalsIgnoreCase("expires")) {
 							expires= _parseDate(urlDecode(namevalue[1].trim()));
 						}
 					} else if(part.equalsIgnoreCase("secure")) {
 						secure= true;
 					}
 				}
 			}
 		}
 		Cookie secondsUntilExpiration(int seconds) {
 			expires= new Date((new Date()).getTime() + seconds * 1000);
 			return this;
 		}
 		/** Formats the cookie for the Cookie http response header
 			@param toBrowser	If true then the string will be formatted as it would be passed to a browser (Set-Cookie: header).
 								If false then the string will be formatted as it would be passed to a server (Cookie: header).
 			@return				The name=value pair for the Cookie http response header. If toBrowser, the Set-Cookie: will be prepended
 		*/
 		public String toString(boolean toBrowser) {
 			String	result= _cookieEncode(name)+"="+_cookieEncode(value);
 
 			if(toBrowser) {
 				if(null != expires) {
 					result+= "; expires="+date1.format(expires);
 				}
 				if(null != domain) {
 					result+= "; domain="+_cookieEncode(domain);
 				}
 				if(null != path) {
 					result+= "; path="+_cookieEncode(path);
 				}
 				if(secure) {
 					result+= "; secure";
 				}
 				result= "Set-Cookie: "+result;
 			}
 			return result;
 		}
 		/** The name of the cookie */
 		public String	name;
 		/** The value of the cookie */
 		public String	value;
 		/** The expiration date (or null if not set) */
 		public Date		expires;
 		/** The domain (or null if not set) */
 		public String	domain;
 		/** The path  (or null if not set) */
 		public String	path;
 		/** secure (false if not set) */
 		public boolean	secure;
 		/** Search for whitespace */
 		private static final Pattern			_WhitespacePattern= Pattern.compile("\\s");
 		/** Hexidecimal digits */
 		private static final String				_Hex= "0123456789ABCDEF";
 		/** A cookie date format */
 		private static final SimpleDateFormat	date1= new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
 		/** A cookie date format */
 		private static final SimpleDateFormat	date2= new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");
 		/** Encode cookies.
 			Cookies require minimal encoding.
 			plus (+), percent (%), semicolon (;) and comma (,) are replaced with their escapes, spaces are escaped with a plus
 			@param rawCookie	The actual value to encode
 			@return				The encoded value
 		*/
 		private static String _cookieEncode(String rawCookie) {
 			String	minimalyEscaped= rawCookie.replace("+","%2B").replace("%","%25").replace(";","%3B").replace(",","%2C").replace(" ","+");
 			String	escaped= "";
 			Matcher	matcher= _WhitespacePattern.matcher(minimalyEscaped);
 			int		lastOffset= 0;
 
 			while(matcher.find()) {
 				int	asciiValue= (int)matcher.group().charAt(0);
 
 				escaped+= minimalyEscaped.substring(lastOffset, matcher.start());
 				escaped+= "%"+_Hex.charAt((asciiValue&0xF0)>>4)+_Hex.charAt(asciiValue&0x0F);
 				lastOffset= matcher.end();
 			}
 			escaped+= minimalyEscaped.substring(lastOffset);
 			return escaped;
 		}
 		/** Parses the cookie expiration date.
 			@param date	The cookie date string
 			@return		The actual date, or null if unable to parse the date.
 		*/
 		private static Date _parseDate(String date) {
 			try	{
 				return date1.parse(date);
 			} catch(java.text.ParseException e) {
 				System.err.println(e);
 			}
 			try	{
 				return date2.parse(date);
 			} catch(java.text.ParseException e) {
 				System.err.println(e);
 			}
 			return null;
 		}
 	}
 	/** Representation of a set of cookies.
 	*/
 	static public class CookieJar extends HashMap<String, ArrayList<Cookie>> {
 		/** New cookie jar.
 		*/
 		public CookieJar() {
 		}
 		/** Creates a cookie jar from a Cookie http request header.
 		*/
 		public CookieJar(String cookieHeader) {
 			String[]	cookies= cookieHeader.split(";");
 
 			for(String cookie : cookies) {
 				add(new Cookie(cookie));
 			}
 		}
 		/** Merge two cookie jars.
 			@param cookies	The cookies to add to this jar.
 			@return			A reference to this
 		*/
 		public CookieJar add(CookieJar cookies) {
 			for(String name : cookies.keySet()) {
 				for(Cookie cookie : cookies.get(name)) {
 					add(cookie);
 				}
 			}
 			return this;
 		}
 		/** Adds a cookie to the jar.
 			Duplicates are added to the list of cookies for the name.
 			@param cookie	The cookie to add
 			@return			A reference to this for call chaining
 		*/
 		public CookieJar add(Cookie cookie) {
 			ArrayList<Cookie>	cookies= get(cookie.name);
 
 			if(null == cookies) {
 				cookies= new ArrayList<Cookie>(1);
 				cookies.add(cookie);
 				put(cookie.name, cookies);
 			}
 			return this;
 		}
 		/** Gets the first cookie associated with the given name.
 			@param name	The name of the cookie to look for.
 			@return		The first cookie added to the jar with that name. null if none added.
 		*/
 		public Cookie getFirst(String name) {
 			ArrayList<Cookie>	cookies= get(name);
 
 			if(null == cookies) {
 				return null;
 			}
 			return cookies.get(0);
 		}
 		/** Gets a http response header containing the cookies in the jar.
 			@return	The Cookie: name=value; name=value string
 		*/
 		public String toString() {
 			String	value= "Cookie: ";
 			String	prefix= "";
 
 			for(String name : keySet()) {
 				Cookie	cookie= getFirst(name);
 
 				value+= prefix + cookie.toString(false); // false == to server instead of to browser format
 				prefix= "; ";
 			}
 			return value;
 		}
 	}
 	/** Representation of a Key to Many Value Map.
 	*/
 	static public class KeyValuesMap extends HashMap<String, ArrayList<String>> {
 		/** New KeyValuesMap.
 		*/
 		public KeyValuesMap() {
 		}
 		/** Add elements from another KeyValuesMap.
 			Duplicates get overridden with values from <code>other</code>.
 		*/
 		public void add(KeyValuesMap other) {
 			for(String key : other.keySet()) {
 				for(String value : other.get(key)) {
 					addValue(key, value);
 				}
 			}
 		}
 		/** Adds a key/value to the KeyValuesMap.
 			@param key		The key to add
 			@param value	The associated value for the key
 		*/
 		public void addValue(String key, String value) {
 			if(!containsKey(key)) {
 				put(key, new ArrayList<String>());
 			}
 			get(key).add(value);
 		}
 		/** Adds a key/value to the KeyValuesMap.
 			@param key		The key to add
 			@param value	The associated value for the key
 		*/
 		public void add(String key, String value) {
 			addValue(key, value);
 		}
 		/** Adds a key/value to the KeyValuesMap.
 			@param key		The key to add
 			@param value	The associated value for the key
 		*/
 		public void put(String key, String value) {
 			addValue(key, value);
 		}
 		/** Gets the first value for a key.
 			@param key			The key to lookup
 			@param defaultValue	If the key does not exist, this value will be returned
 		*/
 		public String firstValue(String key, String defaultValue) {
 			if(containsKey(key)) {
 				return get(key).get(0);
 			}
 			return defaultValue;
 		}
 		/** Gets the first value for a key.
 			@param key			The key to lookup
 			@param defaultValue	If the key does not exist, this value will be returned
 		*/
 		public String getProperty(String key, String defaultValue) {
 			return firstValue(key, defaultValue);
 		}
 		/** Gets the first value for a key.
 			If the key is not found, null is returned.
 			@param key			The key to lookup
 		*/
 		public String getProperty(String key) {
 			return firstValue(key, null);
 		}
 		/** Needed for Serializable.
 		*/
 		public static final long serialVersionUID = 1L;
 	}
 	/** Handler for an HTTP request.
 	*/
 	public interface Handler {
 		/** Handles the http request.
 			@param in		The incoming data after the header.
 			@param out		The output. Header needs to be written.
 			@param headers	The header properties
 			@param query	The query value
 			@param cookies	Any cookies that were sent.
 			@return			Return true if the server should keep running, false if it needs to shut down
 		*/
 		public boolean handle(InputStream in, OutputStream out, KeyValuesMap headers, KeyValuesMap query, CookieJar cookies) throws IOException;
 		/** Logs exceptions
 			@param exception	The exception to log.
 		*/
 		public void log(Exception exception);
 		/** Logs messages.
 			@param level	0 = vital to display 100 = trivial to display
 			@param message	The message to handle
 		*/
 		public void log(int level, String message);
 	}
 	/** Creates a SocketServer handler that formats the connection for an HTTPServer.Handler.
 		@param handler	The object that handles the http connection
 	*/
 	public HTTPServer(Handler handler) {
 		_handler= handler;
 	}
 	/** Log exceptions to the handler.
 		@param exception	The exception to pass on
 	*/
 	public void log(Exception exception) {
 		_handler.log(exception);
 	}
 	/** Log messages to the handler.
 		@param level	0 = vital 100 = trivial
 		@param message	The message to log
 	*/
 	public void log(int level, String message) {
 		_handler.log(level, message);
 	}
 	/** Writes an http response line.
 		@param out					The http connection stream
 		@param httpVersionNumber	The http version number to support
 		@param statusCode			The http status code to send
 		@param responsePhrase		The http response phrase/description
 		@throws IOException			On io error
 	*/
 	public static void writeResponse(OutputStream out, String httpVersionNumber, int statusCode, String responsePhrase) throws IOException {
 		write(out, "HTTP/"+httpVersionNumber+" "+statusCode+" "+responsePhrase+"\r\n");
 	}
 	/** Writes out a header field.
 		@param out		http out stream
 		@param key		Header key
 		@param value	The value for the key (handles multiline values)
 		@throws IOException			On io error
 	*/
 	public static void writeHeader(OutputStream out, String key, String value) throws IOException {
 		write(out, key+":\t"+value.trim().replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n\t")+"\r\n");
 	}
 	/** Writes out a Cookie header field.
 		@param out		http out stream
 		@param cookie	The cookie to write a header for
 		@throws IOException			On io error
 	*/
 	public static void writeHeader(OutputStream out, Cookie cookie) throws IOException {
 		write(out, cookie.toString(true)+"\r\n"); // true == toBrowser
 	}
 	/** Writes out Cookie header fields.
 		@param out		http out stream
 		@param cookies	The cookies to write a header for
 		@throws IOException			On io error
 	*/
 	public static void writeHeader(OutputStream out, CookieJar cookies) throws IOException {
 		write(out, cookies.toString()+"\r\n");
 	}
 	/** Writes out the end of headers marker.
 		@param out	The http out stream
 		@throws IOException			On io error
 	*/
 	public static void finishHeaders(OutputStream out) throws IOException {
 		write(out, "\r\n");
 	}
 	/** Writes a string to an output stream.
 		@param out	The output stream
 		@param data	The raw bytes of the string will be written to <code>out</code>
 	*/
 	public static void write(OutputStream out, String data) throws IOException {
 		out.write(data.getBytes());
 	}
 	/** Decodes a URL encoded string.
 		Spaces are converted from pluses (+). Then %XX is converted to a character of that value.
 		@param urlEncoded	A URL encoded string
 		@return				the decoded version of <code>urlEncoded</code>
 	*/
 	public static String urlDecode(String urlEncoded) {
 		String	spacesFixed= urlEncoded.replace("+", " ");
 		String	unescaped= "";
 		Matcher	matcher= _URLEscapedPattern.matcher(spacesFixed);
 		int		lastOffset= 0;
 
 		while(matcher.find()) {
 			int	asciiValue= Integer.parseInt(matcher.group(1), 16);
 
 			unescaped+= spacesFixed.substring(lastOffset, matcher.start());
 			unescaped+= (char)asciiValue;
 			lastOffset= matcher.end();
 		}
 		unescaped+= spacesFixed.substring(lastOffset);
 		return unescaped;
 	}
 	/** Parses an http query string.
 		Everything after the ? in a URL.
 		@param queryString	Key/Value pairs. Key/Value separated by =, pairs separated by &
 		@return				Query map of Key/Value pairs
 	*/
 	public static KeyValuesMap parseQuery(String queryString) {
 		KeyValuesMap		query= new KeyValuesMap();
 		String[]	pairs= queryString.split("\\&");
 
 		if(pairs.length > 1) {
 			for(String pair : pairs) {
 				String	keyValue[]= pair.split("=", 2);
 
 				if(keyValue.length > 1) {
 					query.addValue(urlDecode(keyValue[0]), urlDecode(keyValue[1]));
 				}
 			}
 		}
 		return query;
 	}
 	/** Reads the entire body of the input stream.
 		If Content-Length header was set, that is used, else it reads until done
 		If the header "NO-BODY" == "already-read" then no reading is done
 			(it was a POST query that already read the body)
 		@param in			http input stream
 		@param headers		Headers from the http stream
 		@return				The contents of the http request
 		@throws IOException	If there is an io error
 	*/
 	public static String readBody(InputStream in, KeyValuesMap headers) throws IOException {
 		int		length= Integer.parseInt(headers.getProperty("Content-Length", "-1"));
 		String	contents;
 
 		if(headers.getProperty("NO-BODY", "").equals("already-read")) {
 			return "";
 		}
 		return new String(SocketServer.read(in, 500 /* timeout in ms */, length, -1 /* pick buffer size */));
 	}
 	/** Handles the Socket Server incoming request.
 		Sets common CGI environment variables in the properties
 			(METHOD, REQUEST-URI, HTTP-VERSION, VERSION, MAJOR-VERSION, MINOR-VERSION, PATH, URL-QUERY (optional)).
 		Reads the HTTP line and header fields (including multi-line header fields).
 		If it is a POST that has a Content-Length header and is encoded in application/x-www-form-urlencoded,
 			then the body is read to get the POST query values and sets the property NO-BODY=already-read.
 		Parses both URL Query and POST Query values and merges them.
 		Then calls the Handler to actually handle the request.
 		@param server		The SocketServer that is handing us the connection
 		@param connection	The http connection to handle
 		@throws IOException	When there is an io error
 	*/
 	public void handle(SocketServer server, Socket connection) throws IOException {
 		String			header;
 		InputStream		connectionIn= connection.getInputStream();
 		OutputStream	out= connection.getOutputStream();
 		boolean			keepAlive;
 
 		do	{
 			InputStream			in= connectionIn;
 			String				statusLine= _readHeaderLine(in);
 			String				statusParts[]= statusLine.split("\\s+", 3);
 			String				version= statusParts[2].split("/",2)[1];
 			String				versionParts[]= version.split("\\.", 2);
 			String				uriParts[];
 			String				lastHeader= null;
 			KeyValuesMap		headers= new KeyValuesMap();
 			KeyValuesMap		query, urlQuery;
 			CookieJar			cookies= new CookieJar();
 			LimitedInputStream	bodyStream= null;
 
 			headers.put("METHOD", statusParts[0]);
 			headers.put("REQUEST-URI", statusParts[1]);
 			headers.put("HTTP-VERSION", statusParts[2]);
 			headers.put("VERSION", version);
 			headers.put("MAJOR-VERSION", versionParts[0]);
 			headers.put("MINOR-VERSION", versionParts[1]);
 			log(100, "Method="+statusParts[0]);
 			log(100, "Request-URI="+statusParts[1]);
 			log(100, "HTTP-Version="+statusParts[2]);
 			log(100, "Version="+version);
 			log(100, "Major Version="+versionParts[0]);
 			log(100, "Minor Version="+versionParts[1]);
 			do	{
 				header= _readHeaderLine(in);
 				if(header.length() == 0) {
 					log(100, "End of headers");
 				} else if( (null != lastHeader)
 						&& (header.trim().charAt(0) != header.charAt(0)) ) {
 					String	lastValue= headers.getProperty(lastHeader);
 					String	augmented= lastValue+"\r\n"+header.trim();
 
 					headers.put(lastHeader, augmented);
 					log(100, "Updating header: "+lastHeader);
 				} else {
 					String	headerParts[]= header.split(":", 2);
 
 					lastHeader= headerParts[0].trim();
 					headers.put(lastHeader, headerParts[1].trim());
 					log(100, "Setting header: "+lastHeader);
 				}
 			} while(header.length() > 0);
 			if(headers.containsKey("Cookie")) {
 				for(String cookieSet : headers.get("Cookie")) {
 					cookies.add(new CookieJar(cookieSet));
 				}
 			}
 			if(statusParts[1].indexOf("?") >= 0) {
 				uriParts= statusParts[1].split("\\?", 2);
 				headers.put("PATH", uriParts[0]);
 				headers.put("URL-QUERY", uriParts[1]);
 			} else {
 				headers.put("PATH", statusParts[1]);
 			}
 			if(statusParts[0].equals("POST")) {
 				log(100, "It's a post");
 			}
 			if(headers.getProperty("Content-Length", "").length() > 0) {
 				log(100, "It has a Content-Length:"+headers.getProperty("Content-Length", ""));
 			}
 			if(headers.getProperty("Content-Type", "").length() > 0) {
 				log(100, "It has a Content-Type:"+headers.getProperty("Content-Type", ""));
 			}
 			if(statusParts[0].equals("POST")
 					&& (headers.getProperty("Content-Length", "").length() > 0)
 					&& headers.getProperty("Content-Type", "").equals("application/x-www-form-urlencoded")) {
 				int		length= Integer.parseInt(headers.getProperty("Content-Length"));
 				String	queryString= readBody(in, headers);
 
 				log(100, "POST QUERY READ: "+queryString);
 				headers.put("NO-BODY", "already-read");
 				headers.put("POST-QUERY", queryString);
			} else {
				long	bodyLength= Long.parseLong(headers.getProperty("Content-Length", "0"));
 
				log(100, "Non-POST BODY length="+bodyLength);
				bodyStream= new LimitedInputStream(in, bodyLength);
				in= bodyStream;
 			}
 			urlQuery= parseQuery(headers.getProperty("URL-QUERY", ""));
 			query= parseQuery(headers.getProperty("POST-QUERY", ""));
 			query.add(urlQuery);
 			log(100, "Query Details");
 			for(String key : query.keySet()) {
 				log(100, "- "+key);
 				for(String value : query.get(key)) {
 					log(100, "\t+ "+value);
 				}
 			}
 
 			if(!_handler.handle(in, out, headers, query, cookies)) {
 				log(100, "Handler requested a shutdown");
 				server.terminate();
 			}
 			if(null != bodyStream) {
 				long	skipped= bodyStream.finish();

				if(skipped > 0) {
					log(100, "Body was not fully read by  handler, "+skipped+" bytes were left");
				}
 			}
 			log(100, "Done handling request");
 			keepAlive= headers.firstValue("Connection", "close").trim().equalsIgnoreCase("keep-alive");
 		} while(keepAlive);
 		log(100, "Done handling connection");
 	}
 	/** URL escaped charater pattern (ie %20) */
 	private static final Pattern	_URLEscapedPattern= Pattern.compile("%([0-9A-Fa-f][0-9A-Fa-f])");
 	/** The handler to hand off the connection once we've parsed all the http stuff out of it */
 	private Handler	_handler;
 	/** Reads one line from an http header.
 		It is able to read \n and \r\n that separates lines. It does not, however, handle \r as a line ending.
 		@param in			The http in stream.
 		@return				The next line of an http header from the http stream
 		@throws IOException	if there is an io error or there is a \r but the next character is not \n
 	*/
 	private String _readHeaderLine(InputStream in) throws IOException {
 		int		oneByte= in.read();
 		String	line= "";
 
 		while( (-1 != oneByte) && ('\r' != oneByte) && ('\n' != oneByte) ) {
 			line+= new String(""+(char)oneByte);
 			oneByte= in.read();
 		}
 		if('\r' == oneByte) {
 			oneByte= in.read();
 			if(oneByte != '\n') {
 				throw new IOException("Header corrupted, \\r without \\n");
 			}
 		}
 		return line;
 	}
 	/** Test class to handle an Echo http server
 	*/
 	private static class Echo implements Handler {
 		/** Handles logging exceptions
 		*/
 		public void log(Exception exception) {
 			exception.printStackTrace();
 			log(0, exception.toString());
 		}
 		/** Handles an http request.
 			@param in		The incoming data after the header.
 			@param out		The output. Header needs to be written.
 			@param headers	The header properties
 			@param query	The query value
 			@param cookies	Any cookies that were sent.
 		*/
 		public boolean handle(InputStream in, OutputStream out, KeyValuesMap headers, KeyValuesMap query, CookieJar cookies) throws IOException {
 			int	retryCount= 5; // 5 * 100 ms = 1/2 second with no data means no more data
 
 			HTTPServer.writeResponse(out, "1.1", 200, "OK");
 			HTTPServer.writeHeader(out, "Content-Type", "text/html");
 			HTTPServer.writeHeader(out, (new Cookie("name=Nicholas")).secondsUntilExpiration(30));
 			HTTPServer.finishHeaders(out);
 			HTTPServer.write(out, "<html><head><title>Echo</title></head><body><h1><center>Echo</center></h1>\r\n");
 			HTTPServer.write(out, "<h2>Cookies</h2>\r\n<ul>");
 			for(String key : cookies.keySet()) {
 				HTTPServer.write(out, "<li><b>"+key+"</b><ol>\r\n");
 				for(Cookie value : cookies.get(key)) {
 					HTTPServer.write(out, "<li>"+value.toString(false)+"\r\n");
 				}
 				HTTPServer.write(out, "</ol>\r\n");
 			}
 			HTTPServer.write(out, "</ul>\r\n");
 			HTTPServer.write(out, "<h2>Headers</h2>\r\n<ul>");
 			for(String key : headers.keySet()) {
 				HTTPServer.write(out, "<li><b>"+key+"</b><ol>\r\n");
 				for(String value : headers.get(key)) {
 					HTTPServer.write(out, "<li>"+value+"\r\n");
 				}
 				HTTPServer.write(out, "</ol>\r\n");
 			}
 			HTTPServer.write(out, "</ul>\r\n");
 			HTTPServer.write(out, "</pre><h2>Query</h2>\r\n<ol>");
 			for(String key : query.keySet()) {
 				HTTPServer.write(out, "<li><b>"+key+"</b><ol>\r\n");
 				for(String value : query.get(key)) {
 					HTTPServer.write(out, "<li>"+value+"\r\n");
 				}
 				HTTPServer.write(out, "</ol>\r\n");
 			}
 			HTTPServer.write(out, "</ul>");
 			HTTPServer.write(out, "</pre><h2>Body</h2><pre>\r\n");
 			HTTPServer.write(out, HTTPServer.readBody(in, headers));
 			HTTPServer.write(out, "</pre>");
 			HTTPServer.write(out, "POST<form action=/test/me?test=5&something=6&something=7 method=POST><input name=test><input name=something><input type=submit></form>");
 			HTTPServer.write(out, "<form action=/quit method=POST><input type=submit value=Quit></form>");
 			HTTPServer.write(out, "</body></html>\r\n");
 			return !(headers.getProperty("PATH","").equalsIgnoreCase("/quit"));
 		}
 		/** Logs messages.
 			@param level	0 = vital to display 100 = trivial to display
 			@param message	The message to handle
 		*/
 		public void log(int level, String message) {
 			System.err.println("LOG "+level+": "+message);
 		}
 	}
 	/** Test.
 		@param args	arg[0] is the port to listen on.
 	*/
 	public static void main(String... args) {
 		try	{
 			new SocketServer(Integer.parseInt(args[0]), new HTTPServer(new Echo()));
 		} catch(IOException exception) {
 			System.err.println(exception);
 		}
 	}
 }
 /*
 
 Query String
 Letters (A-Z and a-z), numbers (0-9) and the characters '.','-','~' and '_' are left as-is
 SPACE is encoded as '+'
 All other characters are encoded as %FF hex representation with any non-ASCII characters first encoded as UTF-8 (or other specified encoding)
 
 Cookies RFC 2965 (http://www.faqs.org/rfcs/rfc2965.html)
 	(http://lib.ru/WEBMASTER/cookie_spec.txt)
 GET / HTTP/1.1
 Host: localhost:8080
 User-Agent: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-us) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4
 Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,* /*;q=0.5
 Accept-Language: en-us
 Accept-Encoding: gzip, deflate
 Connection: keep-alive
 
 HTTP/1.1 200 OK
 Date: Thu, 02 Dec 2010 22:15:17 GMT
 Server: Apache
 Accept-Ranges: bytes
 Transfer-Encoding: chunked
 Content-Type: text/html
 
 HTTP RFC 2616 (http://www.faqs.org/rfcs/rfc2616.html)
 Section 5.1
 Request line= Method SP Request-URI SP HTTP-Version CRLF
 	5.1.2
 	Request-URI    = "*" | absoluteURI | abs_path | authority
 Section 6.1
 Status line= HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 */
