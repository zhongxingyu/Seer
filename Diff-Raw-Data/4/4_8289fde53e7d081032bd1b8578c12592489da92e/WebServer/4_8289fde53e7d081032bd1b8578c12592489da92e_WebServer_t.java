 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public final class WebServer
 {
 	public static void main(String argv[]) throws Exception
 	{
 		/* port number to be used for the server */
 		final int PORT = 55000;
 
 		/* establish the listen socket */
 		ServerSocket sock = new ServerSocket(PORT);
 		Socket toSend = null;
 
 		while (true)
 		{
 			/* listen for a TCP connection request */
 			// TODO: try to remove the try/catch
 			try {
 				toSend = sock.accept();
 			} catch ( Exception e ) {
 				System.out.printf("E/WebServer: Error accepting TCP request!\n");
 			}
 
 			/* construct an object to process the HTTP request message. */
 			HttpRequest request = new HttpRequest(toSend);
 
 			/* Create a new thread to process the request */
 			Thread thread = new Thread(request);
 
 			/* start the tread */
 			thread.start();
 		}
 	}
 }
 
 final class HttpRequest implements Runnable
 {
 	final static String CRLF = "\r\n";
 	Socket sock;
 
 	/**
 	 * Public constructor.
 	 * @param  socket   the socket that accepted the request
 	 * @return          object of type HttpRequest
 	 */
 	public HttpRequest(Socket socket) throws Exception
 	{
 		this.sock = socket;
 	}
 
 	/**
 	 * Calls processRequest() or prints an exception.
 	 */
 	 public void run()
 	 {
 	 	try {
 			processRequest();
 		} catch (Exception e) {
 			System.out.println("E/HttpRequest.run(): " +e);
 		}
 	}
 
 	/**
 	 * Processes an HTTP request.
 	 */
 	private void processRequest() throws Exception
 	{
 		/* get a reference to the socket's input and output streams */
 		InputStream is = sock.getInputStream();
 		DataOutputStream os = new DataOutputStream(sock.getOutputStream());
 
 		/* set up input stream filters */
 		InputStreamReader in = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(in);
 
 		/* get the request line of the HTTP reqeuest message */
 		String requestLine = br.readLine();
 
 		/* display the request line */
 		System.out.println();
 		System.out.println(requestLine);
 
 		/* get and display the header lines */
 		String headerLine = null;
 		while( (headerLine = br.readLine()).length() != 0 )
 			System.out.println(headerLine);
 
 		/* extract the filename from the request line */
 		StringTokenizer tokens = new StringTokenizer(requestLine);
 
 		/* skip the method, which we assume to be 'GET' */
 		tokens.nextToken();
 
 		/* get the filename */
 		String filename = tokens.nextToken();
 
 		/* prepend a '.' so that the file request is within pwd */
 		filename = "." + filename;
 
 		/* open the requested file */
 		FileInputStream fis = null;
 		boolean fileExists = true;
 		try {
 			fis = new FileInputStream(filename);
 		} catch ( FileNotFoundException e ) {
 			fileExists = false;
 		}
 
 		/* construct the response message */
 		String statusLine, contentTypeLine, entityBody = null;
 
 		if( fileExists )
 		{
 			statusLine = "200" + CRLF;
 			contentTypeLine = "Content-type: " + contentType( filename ) + CRLF;
 		}
 		else
 		{
 			statusLine = "404" + CRLF;
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" +
 				"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
 				"<BODY>Not Found</BODY></HTML>";
 		}
 
 		/* send the status line */
 		os.writeBytes(statusLine);
 
 		/* send content type line */
 		os.writeBytes(contentTypeLine);
 
 		/* send a blank line to indicate end of header lines */
 		os.writeBytes(CRLF);
 
 		/* send the file if it exists */
 		if( fileExists )
 		{
 			sendBytes(fis, os);
 			fis.close();
 		}
 		else
 			os.writeBytes(entityBody);
 
 		/* close streams and socket */
 		os.close();
 		br.close();
 		sock.close();
 	}
 
 	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
 	{
 		/* construct a 1K buffer to hold byte on their way to the socket */
 		byte[] buffer = new byte[1024];
 		int bytes = 0;
 
 		/* copy requested file into the socket's output stream */
 		while( (bytes = fis.read(buffer)) != -1 )
 			os.write(buffer, 0, bytes);
 	}
 
 	private static String contentType(String filename)
 	{
 		if( filename.endsWith(".htm") || filename.endsWith(".html")  )
 			return "text/html";
 		if ( filename.endsWith(".txt") )
 			return "text/plain";
 		if ( filename.endsWith(".jpg") )
 			return "image/jpeg";
 		if ( filename.endsWith(".png") )
 			return "image/png";
 		if ( filename.endsWith(".gif") )
 			return "image/gif";
 		return "application/octet-stream";
 	}
 }
