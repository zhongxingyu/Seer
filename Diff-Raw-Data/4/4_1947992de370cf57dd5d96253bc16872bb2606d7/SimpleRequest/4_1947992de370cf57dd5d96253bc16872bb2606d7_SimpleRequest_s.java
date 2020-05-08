 package server.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import server.io.RequestMethods.Method;
 
 /**
  * Implementation of the IRequest interface
  * 
  * @author dkirby
  *
  */
 public class SimpleRequest implements IRequest{
 	
 	private final String ENCODING = "UTF-8";
 	
 	private InetAddress _InetAddress;	// the client's InetAddress
 	private Method _method;	// the request method
 	private String _url;	// the requested url (with url parameters included)
 	
 	private HashMap<String, String> _urlParams; // the url parameters
 	private HashMap<String, String> _headers; // the request headers
 	private HashMap<String, String> _postData;	// the request post data
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param socket the socket connection to build the request around
 	 * 
 	 * @throws IOException if an I/O error occurs during construction
 	 * @throws MalformedRequestException if the request is malformed
 	 * @throws UnsupportedMethodException if the request method is unsupported
 	 */
 	public SimpleRequest(Socket socket) throws IOException, MalformedRequestException, UnsupportedMethodException {
 		_InetAddress = socket.getInetAddress();
 		
 		InputStream input = socket.getInputStream();
 		parseInput(input);
 	}
 	
 	/**
 	 * Parses the request and stores the results locally
 	 * 
 	 * @param input the InputStream to read from
 	 * @throws IOException
 	 * @throws MalformedRequestException
 	 * @throws UnsupportedMethodException 
 	 */
 	void parseInput(InputStream input) throws IOException, MalformedRequestException, UnsupportedMethodException 
 	{
 		String s = readInputStream(input);
 		if (s == null || s.isEmpty())
 			throw new IOException("No data");
 			
 		Scanner request = new Scanner(s);
 		
 		try
 		{
 			// Parse first line data
 			try
 			{
 				_method = parseMethod(request.next().trim());
 				_url = request.next().trim();
 			}
 			catch (NoSuchElementException e)
 			{
 				throw new MalformedRequestException("Malformed request");
 			}
 			
 			// the next token in the request should be HTTP/x.x, so make sure it is
 			if (!verifyHTTPFormat(request.nextLine().trim()))
 				throw new MalformedRequestException("Malformed request");
 			
 			// Parse the URL parameters (if any)
 			_urlParams = parseUrlParams(_url);
 			
 			// Parse the remainder of the header
 			_headers = parseHeaders(request);
 			
 			// Parse the post data (if any)
 			if (_method == Method.POST)
 				_postData = parsePostData(request);
 			else
 				// set it to an empty hashmap so that the getPostData() method doesn't throw an exception
 				_postData = new HashMap<>();
 		}
 		finally
 		{
 			// close the scanner
 			request.close();
 		}
 			
 	}
 	
 	/**
 	 * Reads the InputStream provided and returns the read data as a String
 	 * 
 	 * @param input the InputStream to read
 	 * @return a String containing the data read from the InputStream
 	 * @throws IOException if an I/O error occurs while reading
 	 */
 	String readInputStream(InputStream input) throws IOException
 	{					
 		// Since an EOF will not be reached until the socket is closed, we read the request
 		// in line by line until a blank line is reached.		
 		BufferedReader bReader = new BufferedReader(new InputStreamReader(input));
 		
 		String result = bReader.readLine();
 		String tmp = result;
 		while (tmp != null && !tmp.isEmpty())
 		{
 			tmp = bReader.readLine();
 			
 			if (tmp != null)
 			{
 				result = result + "\n" + tmp;
 			}
 		}
 		
 		// try to read another line, in case there is a request body after the first blank line
		if (tmp != null)
		{
 			tmp = bReader.readLine();
 			
 			// loop until request body is read (if any)
 			while (tmp != null && !tmp.isEmpty())
 			{
 				result = result + "\n" + tmp;
 				
 				tmp = bReader.readLine();
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Parses the request method from the request
 	 * 
 	 * @param request a Scanner wrapped around the string representation of the request
 	 * @return the request method
 	 * @throws UnsupportedMethodException if the request method is unsupported
 	 */
 	Method parseMethod(String sMethod) throws UnsupportedMethodException, NoSuchElementException
 	{	
 		RequestMethods.Method method = RequestMethods.mapMethod(sMethod);
 		
 		if (method == Method.INVALID)
 			throw new UnsupportedMethodException("Invalid request method");
 		
 		return method;
 	}
 	
 	/**
 	 * Parses the url parameters from the supplied URL
 	 * 
 	 * @param url the url to parse
 	 * @return a HashMap containing the URL parameters
 	 * @throws MalformedRequestException
 	 * @throws UnsupportedEncodingException
 	 */
 	HashMap<String, String> parseUrlParams(String url) throws MalformedRequestException, UnsupportedEncodingException
 	{
 		HashMap<String, String> params = new HashMap<>();
 		
 		int index = url.indexOf("?");
 		if (index != -1)
 		{
 			String s = url.substring(index + 1);
 			
 			String[] par = s.split("&");
 			for (int i = 0; i < par.length; i++)
 			{
 				if (!par[i].contains("="))
 					throw new MalformedRequestException("Malformed URL Parameter");
 				
 				String[] key_val = par[i].split("=");
 				
 				params.put(key_val[0], URLDecoder.decode(key_val[1], ENCODING));
 			}
 		}
 		
 		return params;
 	}
 	
 	/**
 	 * Parses the post data from the request
 	 * 
 	 * @param request a Scanner wrapped around a String representation of the request. It is assumed that the headers
 	 * have already been scanned
 	 * 
 	 * @return a HashMap containing the post data
 	 * 
 	 * @throws MalformedRequestException
 	 * @throws UnsupportedEncodingException
 	 */
 	HashMap<String, String> parsePostData(Scanner request) throws MalformedRequestException, UnsupportedEncodingException
 	{
 		HashMap<String, String> postData = new HashMap<>();
 		
 		if (!request.hasNextLine())
 			return postData;
 		
 		String s = request.nextLine();
 		while (request.hasNextLine())
 			s += request.nextLine();
 		
 		String[] data = s.split("&");
 		for (int i = 0; i < data.length; i++)
 		{
 			if (!data[i].contains("="))
 				throw new MalformedRequestException("Malformed URL Parameter");
 			
 			String[] key_val = data[i].split("=");
 			
 			postData.put(key_val[0], URLDecoder.decode(key_val[1], ENCODING));
 		}
 		
 		return postData;
 	}
 	
 	/**
 	 * Parses the header information from the request
 	 * 
 	 * @param request a Scanner wrapped around a string representation of the request. It is assumed that the first line
 	 * of the request (the method, url, version) has already been scanned
 	 * 
 	 * @return a HashMap containing the header information
 	 * 
 	 * @throws MalformedRequestException
 	 * @throws UnsupportedEncodingException
 	 */
 	HashMap<String, String> parseHeaders(Scanner request) throws MalformedRequestException, UnsupportedEncodingException
 	{
 		HashMap<String, String> headers = new HashMap<>();
 		
 		if (!request.hasNextLine())
 			return headers;
 		
 		String line = request.nextLine();
 		
 		while (line != null && !line.isEmpty())
 		{
 			// Get field
 			int index = line.indexOf(":");
 			if (index == -1)
 				throw new MalformedRequestException("Malformed request");
 			
 			String field = line.substring(0, index);
 			
 			// Get value
 			String content = line.substring(index + 1).trim();
 			if (!request.hasNextLine())
 			{
 				headers.put(field, URLDecoder.decode(content, ENCODING));
 				break;
 			}
 			
 			String part = request.nextLine();
 			while (part != null && !part.contains(":") && !part.isEmpty())
 			{
 				content += " " + part.trim();
 					
 				if (!request.hasNextLine())
 					break;
 					
 				part = request.nextLine();
 			}
 			
 			// Record the header data
 			headers.put(field, URLDecoder.decode(content, ENCODING));			 
 			
 			// move to the next line to process 
 			line = part;
 		}
 		
 		return headers;
 	}
 	
 	/**
 	 * Verifies that the HTTP version is valid (part of ensuring that the request is properly formatted)
 	 * 
 	 * @param request a Scanner wrapped around a string representation of the request. It is assumed that only the method
 	 * and url have been scanned
 	 * 
 	 * @return true if the HTTP version is properly formatted, false otherwise
 	 */
 	boolean verifyHTTPFormat(String version)
 	{		
 		return version.matches("HTTP/[0-9]+\\.[0-9]+");
 	}
 
 	@Override
 	public String getHost() {
 		return _headers.get("Host");
 	}
 
 	@Override
 	public String getUrlPath() {
 		return _url;
 	}
 
 	@Override
 	public String getUrlParameter(String name) {
 		return _urlParams.get(name);
 	}
 
 	@Override
 	public String getHeader(String name) {
 		return _headers.get(name);
 	}
 
 	@Override
 	public InetAddress getClientAddress() {
 		return _InetAddress;
 	}
 
 	@Override
 	public Method getMethod() {
 		return _method;
 	}
 
 	@Override
 	public String getPostParameter(String name) {
 		return _postData.get(name);
 	}
 	
 }
