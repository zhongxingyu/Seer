 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 
 /**
  * A wrapper class for the NNTP protocol.
  * Establishes a connection to the news server, checks for valid responses.
  * Supports reading articles only, no posting support.
  * Extends the InputStream Class for reading articles as InputStreams.
  
  */
 
 public class NNTPConnection extends InputStream {
 	
 	transient private TCPConnection network;
 	private boolean connected=false;
 	private static final byte[] dot = {0xd, 0xa, 0x2e, 0xd, 0xa};
 	private static final byte[] newline = {0xd, 0xa};
 	
 	private static final int BUFFER_SIZE=8192;
 	private  String endMarkerString;
 	
 	transient private boolean lock=false;
 	
 	// For BPS calculation.
 	transient private int total = 0;
 	transient private long start;
 	transient private long split;
 	transient private int splitBytes;
 	
 	// internal reader
 	private byte[] buffer;
 	private int buffer_pointer=0;
 	private int buffer_size=0;
 	private boolean end_of_data;
 	private int buffer2_size=0;
 	private byte[] buffer2;
 	
 	String groupName;
 	Long groupStart;
 	Long groupEnd;
 	Long groupLength;
 	
 	HashMap<String, String> articleHeader;
 	
 	private String articleName;
 	
 	private byte[] articleBuffer;
 //	private int articleMarker=0;
 //	private int articlePointer=0;
 	
 	/** 
 	 * Class constructor.
 	 */
 	public NNTPConnection () {		
 		buffer = new byte[BUFFER_SIZE];
 		buffer2 = new byte[BUFFER_SIZE];
 		
 		network = new TCPConnection();
 		
 		articleHeader = new HashMap<String, String>();
 	}
 	
 	/** 
 	 * Class constructor, specifying host address and port
 	 *
 	 * @param hostname  the hostname
 	 * @param port  the port number
 	 */
 	public NNTPConnection(String hostname, int port) throws UnknownHostException, IOException {		
 		this();
 		network = new TCPConnection(hostname,port);
 	}
 	
 	/** 
 	 * Class constructor, specifying host address. Uses default NNTP port
 	 *
 	 * @param hostname  the hostname
 	 */
 	public NNTPConnection(String hostname) throws UnknownHostException, IOException {		
 		this(hostname,119);
 	}
 	
 	
 	/** 
 	 * Enables printing of network messages
 	 */
 	public void enableDebug() { network.enableDebug(); }
 	
 	/** 
 	 * Disables printing of network messages
 	 */
 	public void disableDebug() { network.disableDebug(); }
 	
 	/** 
 	 * Returns the NNTP hostname
 	 *
 	 * @return the hostname
 	 */
 	
 	public String getHost() { return network.getHost(); }
 	
 	/** 
 	 * Returns the NNTP port
 	 *
 	 * @return the port
 	 */
 	
 	public int getPort() { return network.getPort(); }
 	
 	/** 
 	 * set the NNTP server hostname to connect to
 	 *
 	 * @param hostname  the hostname
 	 */
 	public void setHost(String hostname) { network.setHost(hostname); }
 	
 	/** 
 	 * set the NNTP server port number
 	 *
 	 * @param port  the port number
 	 */
 	public void setPort(int port)  { network.setPort(port); }
 	
 	/** 
 	 * set the lock semaphore.  This does nothing functional, is used for multi threaded applications that want to flag that this connection is busy.
 	 */
 	public void lock() { lock = true; }
 	/** 
 	 * unlock the lock semaphore.  This does nothing functional, is used for multi threaded applications that want to flag that this connection is busy.
 	 */	
 	public void unlock() { lock = false; }
 	
 	/** 
 	 * get the lock semaphore.  Is used for multi threaded applications that want to flag that this connection is busy.
 	 *
 	 * @return if the lock flag is set
 	 */
 	public boolean isLocked() { return lock; }
 	
 	//	public BufferedOutputStream getOutputStream() {return network.getOutputStream();}
 	//	public BufferedInputStream getInputStream() {return network.getInputStream();}
 	
 	/** 
 	 * Initiates the connection to the NNTP server.
 	 *
 	 * @exception IOException  If the server is uncontactable or a network error occurs
 	 * @exception NNTPConnectionResponseException  if the server is contactable but returns a non successful response.
 	 */
 	
 	public void connect() throws IOException, NNTPConnectionResponseException {
 		
 		// check for 200 ok
 		this.setEndCommand(newline);
 		network.connect();
 		
 		try{
 			checkResponse("200");
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPConnectionResponseException(e.getMessage());
 		}
 		connected = true;
 		internal_reset();
 	}
 	
 	/** 
 	 * terminates the connection to the NNTP server.
 	 *
 	 * @exception IOException  If a network error occurs
 	 * @exception NNTPConnectionResponseException  if the server returns a non successful response. 
 	 */
 	
 	
 	public void disconnect() throws IOException, NNTPUnexpectedResponseException {
 		
 		if (connected) {
 			this.setEndCommand(newline);
 			sendCommand("QUIT\r\n");
 		
 		// check for success, but what are we going to do if it fails? close connection?
 			checkResponse("205");
 		
 			network.disconnect();
 			connected = false;
 		}
 	}
 	
 	/** 
 	 * this does nothing as mark is not supported.
 	 *
 	 * @see java.io.InputStream#reset
 	 * @see #mark(int)
 	 */
 	public void reset() {
     }
 	
 	/** 
 	 * returns if the Stream is seekable.
 	 *
 	 * @see java.io.InputStream#markSupported
 	 */
 	public boolean markSupported() { return false; }
 	
 	
 	/** 
 	 * this resets the internal read buffer to the start.
 	 */
 	
 	private void internal_reset() {
 		buffer_size = 0; buffer_pointer = 0; buffer2_size = 0; end_of_data=false; 
 	}
 	
 	/** 
 	 * Sends raw data to the NNTP server.
 	 *
 	 * @param comm the String to send to the server
 	 * @exception IOException if a network error occurs
 	 */
 	
 	protected void sendCommand (String comm) throws IOException { internal_reset(); network.sendCommand(comm); }
 	/** 
 	 * Sends raw data to the NNTP server.
 	 *
 	 * @param comm the byte array to send to the server
 	 * @exception IOException if a network error occurs
 	 */
 	protected void sendCommand (byte[] comm) throws IOException { internal_reset(); network.sendCommand(comm); }
 	
 	
 	/** 
 	 * returns the number of bytes remaining before a blocking operation is required.
 	 *
 	 * @see java.io.InputStream#available
 	 */
 	
 	public int available() { return buffer_size - buffer_pointer; }
 	
 	/** 
 	 * returns data read from the NNTP server as a string. Until the terminating marker.
 	 *
 	 * @return a String containing the server response
 	 * @exception IOException if a network error occurs
 	 * @see #setEndCommand(byte[])
 	 */
 	public String readAsString() throws IOException {
		String s = "";
 		int i=0;
 		
 		byte[] b = new byte[1];
 		i = this.read();
 		
 		while (i != -1) {
 			b[0] = (byte)i;
 			s = s.concat(new String(b));
 			i = this.read();
 		}
 		return s;
 		
 	}
 	
 	
 	/** 
 	 * returns data read from the NNTP server as a string. Until the end of line character or end of stream, 
 	 * which ever is first.
 	 *
 	 * @return a String containing the server response
 	 * @exception IOException if a network error occurs
 	 */
 	
 	public String readLine () throws IOException {
 		
 		int i=0;
 		
 		byte[] b = new byte[1];
 		
 		String s = new String();
 
 		while (s.indexOf("\r\n") == -1 && i != -1) {
 			i = network.read();
 			if (i != -1) {
 				b[0] = (byte)i;
 				s = s.concat(new String(b));
 			}
 		}
 		s = s.replaceAll("(\\r|\\n)", "");
 		return s;
 	}
 	
 	//	public int read (byte[] b) throws IOException {
 	//
 	//	}
 	
 	// TODO: read entire article into string buffer and read from that.
 	
 	/** 
 	 * Marks the current position in the Input Stream.
 	 * Does nothing. as mark is not supported. But I am thinking about how to implement it.
 	 *
 	 * @param readlimit unused and is treated as infinite.
 	 */
 /*
 	public void mark(int readlimit)
 	{
 		articleMarker = articlePointer;
 	}
 */
 	
 	/** 
 	 * returns a single byte of data read from the NNTP server as an integer. Until the terminating marker.
 	 * This method reads directly from the network connection and does not cache any data.
 	 * Seeking operations using the read method are not supported.
 	 *
 	 * @return a byte of data or -1 if the terminating marker is seen.
 	 * @exception IOException if a network error occurs
 	 * @see #setEndCommand(byte[])
 	 * @see java.io.InputStream#read
 	 */
 	
 	public int read() throws IOException {
 		
 		if (buffer_pointer >= buffer_size) {
 			if (!end_of_data) {
 				
 				
 				if(buffer2_size == 0 ) {
 					
 					buffer_size=network.receiveResponse(buffer,0,BUFFER_SIZE);
 					buffer_pointer = 0;
 					
 					
 					//	printHex(buffer,buffer_size);
 					
 				} else {
 					
 					byte[] temp;
 					
 					temp = buffer2;
 					buffer2 = buffer;
 					buffer = temp;
 					
 					buffer_size = buffer2_size;
 					buffer2_size = 0;
 					buffer_pointer = 0;
 				}
 				
 				if (NNTPendofcommand(buffer,buffer_size)) {
 					
 					// horible hack
 					buffer_size -= (endMarkerString.length()-2) ;
 					end_of_data = true;
 				} else {
 					
 					buffer2_size=network.receiveResponse(buffer2,0,BUFFER_SIZE);
 					//printHex(buffer2,buffer2_size);
 					
 					//  check that the end of data marker doesn't cross the boundary.
 					if (buffer2_size < endMarkerString.length()) {
 						if (NNTPendofcommand(buffer,buffer_size,buffer2,buffer2_size)) {
 							
 							
 							end_of_data = true;
 							// horible hack
 
 							buffer_size -= (endMarkerString.length() - buffer2_size - 2);
 						}
 					}
 				}
 				
 				
 			} else {
 				return -1;
 			}
 		}
 		
 		return (int) buffer[buffer_pointer++];
 	}
 	
 	/** 
 	 * Closes the current operation's InputStream.
 	 * This does not disconnect from the NNTP server.  It simply closes this InputStream for reading from the server.
 	 *
 	 * @exception IOException if a network error occurs
 	 * @see java.io.InputStream#close
 	 */
 	public void close () throws IOException
 	{
 		while (this.read() != -1);
 		this.internal_reset();
 	}
 	
 	/** 
 	 * skips over n bytes of data from the current operation's InputStream.
 	 * @param n  the number of bytes to skip.
 	 * @return the number of bytes actually skipped. which may be less than n
 	 */
 	public long skip (long n)
 	{
 	
 		long oldPointer = buffer_pointer;
 		
 		buffer_pointer += n;
 		
 		if (buffer_pointer >= buffer_size) 
 			buffer_pointer = buffer_size -1;
 		
 		return buffer_pointer - oldPointer;
 	}
 	
 	
 	/** 
 	 * Checks that the response from the NNTP server matches the expected result.
 	 * throws an NNTPUnexpectedResponseException if the response does not match.
 	 
 	 * @param result  the expected result String.
 	 * @return the server response String.
 	 * @exception IOException if a network error occurs
 	 * @exception NNTPUnexpectedResponseException if the response does not match the result string.  
 	 *            This is usually caught by a supporting method and a more specific Exception is thrown.
 	 */
 	
 	
 	protected String checkResponse (String result) throws IOException, NNTPUnexpectedResponseException {
 		String s = this.readLine();
 		
 		if (s.startsWith("400")) {
 			connected = false;
 		}
 		
 		if (!s.startsWith(result)) {
 			throw new NNTPUnexpectedResponseException(s);
 		}
 		return s;
 	}
 	
 	/**
 	 * Checks the data buffer for the end of data marker.
 	 *
 	 * @param comm  the byte array buffer of the NNTP server response.
 	 * @param l  the length of the byte array.
 	 * @return if the end of data marker is seen.
 	 */
 	
 	private boolean NNTPendofcommand(byte[] comm, int l) {
 		return l > 0 &&  NNTPendofcommand ( new String (comm,0,l));
 	}
 	
 	
 	/**
 	 * Checks the data buffer for the end of data marker.
 	 * This method allows for the marker to appear on the boundary of two buffers.
 	 * First and second buffers are in order they have been read from the NNTP server.
 	 *
 	 * @param comm  the first byte array buffer of the NNTP server response.
 	 * @param l  the length of the first byte array.
 	 * @param comm2  the second byte array buffer of the NNTP server response.
 	 * @param l2  the length of the second byte array.
 	 * @return if the end of data marker is seen.
 	 */
 	
 	
 	private boolean NNTPendofcommand (byte[] comm, int l,byte[] comm2, int l2) {
 		if (l>0 && l2 >0) 
 			return NNTPendofcommand ( new String (comm,0,l) , new String (comm2,0,l2));
 		if (l>0)
 			return NNTPendofcommand ( new String (comm,0,l));
 		if (l2>0)
 			return NNTPendofcommand ( new String (comm2,0,2));
 		return false;
 	}
 	
 	/**
 	 * Checks the data buffer for the end of data marker.
 	 * This method allows for the marker to appear on the boundary of two buffers.
 	 * First and second buffers are in order they have been read from the NNTP server.
 	 *
 	 * @param comm1  the first String buffer of the NNTP server response.
 	 * @param comm2  the second String buffer of the NNTP server response.
 	 * @return if the end of data marker is seen.
 	 */
 	
 	private boolean NNTPendofcommand (String comm1, String comm2)
 	{
 		
 		if (comm1 != null) {
 			if (comm2 != null) {
 				return NNTPendofcommand(comm1.concat(comm2));
 			} else {
 				return NNTPendofcommand(comm1);
 			}
 		} else {
 			if (comm2 != null) {
 				return NNTPendofcommand(comm2);
 			} 
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks the data buffer for the end of data marker.
 	 *
 	 * @param comm  the String buffer of the NNTP server response.
 	 * @return if the end of data marker is seen.
 	 */
 	
 	/**
 	 * Checks the data buffer for the end of data marker.
 	 *
 	 * @param comm  the byte array buffer of the NNTP server response.
 	 * @return if the end of data marker is seen.
 	 */
 	
 	private boolean NNTPendofcommand (String comm) {
 		return comm.indexOf(endMarkerString) != -1;
 	}
 	
 	/**
 	 * Sets the end of data marker.  This is used to signify the end of data from the current NNTP command.
 	 * Some commands end with a newline and some end with a single . on a line.
 	 *
 	 * @deprecated use {@link setEndCommandNewline()} or {@link setEndCommandDot}
 	 * @param b  the byte array buffer of the end of data marker.
 	 */
 	
 	private void setEndCommand(byte[] b) 
 	{
 		endMarkerString = new String (b);
 	}
 	
 	private void setEndCommandNewline() { endMarkerString = new String (newline); }
 	private void setEndCommandDot() { endMarkerString = new String (dot); }
 
 	
 	/**
 	 * Prints a hex dump of a String to System.out.  Used for debugging.
 	 *
 	 * @param s  the String to be dumped as hex.
 	 */
 	
 	protected static void printHex (String s) 
 	{
 		printHex(s.getBytes(),s.length());
 	}
 	
 	/**
 	 * Prints a hex dump of a byte array. Used for debugging.
 	 *
 	 * @param b  the byte array to be dumped as hex.
 	 * @param s  the length of the byte array.
 	 */
 	
 	protected static void printHex (byte[]b , int s) 
 	{
 		int width  = 16;
 		
 		
 		for (int i=0; i < s; i+= width )
 		{
 			
 			System.out.printf("%04x  ",i);
 			
 			int j;
 			for (j=0; j<width && i+j <s; j++) 
 				System.out.printf("%02x ",b[i+j]);
 			
 			for (int k=j; k < width; k++)
 				System.out.printf("   ");
 			
 			for ( j=0; j<width && i+j <s; j++) 
 				if (b[i+j] >= 32 && b[i+j] <127) 
 					System.out.printf("%c",b[i+j]);
 				else 
 					System.out.printf(".");
 			
 			for (int k=j; k < width; k++)
 				System.out.printf(" ");
 			
 			
 			System.out.println();
 		}
 	}
 	
 	/**
 	 * Returns the bytes per second of the data since the transfer began.
 	 *
 	 * @return the bytes per second.
 	 */
 	
 	public long getAveBPS() {
 		long sec = System.currentTimeMillis() - start;
 		if (sec>0)
 			return 1000*total / sec; 
 		return 0;
 	}
 	
 	/**
 	 * Returns the bytes per second of the data since the transfer began or the last call to this method.
 	 * whichever was sooner.
 	 *
 	 * @return the bytes per second.
 	 */
 	
 	public long getSplitBPS() {
 		long sec = System.currentTimeMillis() - split;
 		int splitTotal = total - splitBytes;
 		splitBytes = total;
 		split = System.currentTimeMillis();
 		if (sec >0)
 			return 1000*splitTotal / sec; 
 		return 0;
 	}
 	
 	/**
 	 * Returns the number of  bytes read since the transfer began.
 	 *
 	 * @return the number of bytes.
 	 */
 	
 	public int getTotal() { return total; }
 	
 	
 	/**
 	 * Changes the News Server group and parses the response from the server.
 	 *
 	 * @param g the name of the group. 
 	 * @exception IOException if a network error occurs.
 	 * @exception NNTPNoSuchGroupException if the group does not exist.
 	 * @exception NNTPGroupResponseException if the server returns an invalid group command response.
 	 */
 	
 	
 	public void setGroup (String g) throws IOException, NNTPNoSuchGroupException, NNTPGroupResponseException
 	{
 		setEndCommandNewline();
 		sendCommand("GROUP " + g + "\r\n");
 		
 		try {
 			String s = checkResponse("211");
 			
 			String param[] = s.split(" ");
 			
 			if (param.length <= 4) {
 				throw new NNTPGroupResponseException(s);	
 			}
 			
 			groupLength=new Long(param[1]);
 			groupStart=new Long(param[2]);
 			groupEnd=new Long(param[3]);
 			groupName=param[4];
 			
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPNoSuchGroupException(e.getMessage());
 		} catch (NumberFormatException e) {
 			throw new NNTPGroupResponseException(e.getMessage());	
 		}
 		
 	}
 	
 	/**
 	 * returns the number of articles in the current group.
 	 *
 	 * @return the number of articles in the current group.
 	 * @see #setGroup(String)
 	 */
 	
 	public Long getGroupLength() {return groupLength;}
 	/**
 	 * returns the number of the first article in the current group.
 	 *
 	 * @return the number of the first article in the current group.
 	 * @see #setGroup(String)
 	 */
 	
 	public Long getGroupStart() {return groupStart;}
 	/**
 	 * returns the number of the last article in the current group.
 	 *
 	 * @return the number of the last article in the current group.
 	 * @see #setGroup(String)
 	 */
 	
 	public Long getGroupEnd() {return groupEnd;}
 	/**
 	 * returns the name of the current group.
 	 *
 	 * @return the name of the current group.
 	 * @see #setGroup(String)
 	 */
 	
 	public String getGroupName() {return groupName;}
 	
 	/**
 	 * sends the STAT command to the news server and sets the current article to the response.
 	 *
 	 * @param articleNumber the number of the article.  Will also accept article name but this would be a redundant command.
 	 * @return the name of the article.
 	 * @exception IOException if a network error occurs
 	 * @exception NNTPNoSuchArticleException if the article cannot be found or if there is no current group.
 	 */
 	
 	
 	public String statArticle (String articleNumber) throws IOException, NNTPNoSuchArticleException
 	{
		String s ;
 		
 		// System.out.println("STAT " + articleNumber );
 		setEndCommandNewline();
 		sendCommand("STAT " + articleNumber + "\r\n");
 		try {
 			s = checkResponse("223");
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPNoSuchArticleException(e.getMessage(),articleNumber);
 		}
 		s = s.replaceAll("(\\r|\\n)", ""); // is this needed anymore. readLine should strip end of line characters
 		String param[] = s.split(" ");
 		
 		if (param.length > 2) {
 			this.articleName = param[2];
 			return param[2];
 		}
 		
 		throw new IOException("NNTP Error stat response: " + s);	
 		
 	}
 		
 	
 	/**
 	 * returns the name of the current article in the form: <article-id@server>
 	 *
 	 * @return the name of the current article.
 	 */
 	
 	public String getArticleName() { return this.articleName; }
 	
 	/**
 	 * returns the subject of the current article 
 	 *
 	 * @return the subject of the current article.
 	 */
 	
 	
 	public String getArticleDateAsString () { return this.articleHeader.get("Date"); }
 	public Date getArticleDate() { 
 		String sDate = getArticleDateAsString(); 
 		if (sDate != null) {
 			SimpleDateFormat sdf;
 			Date checkDate;
 			
 			sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");
 			checkDate =  sdf.parse(sDate,new ParsePosition(0));
 			if (checkDate != null ) return checkDate;
 			
 			sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
 			checkDate =  sdf.parse(sDate,new ParsePosition(0));
 			if (checkDate != null ) return checkDate;
 			
 		}
 		return null;
 	}
 	public String getArticleSubject() { return this.articleHeader.get("Subject"); }
 	public String getArticleLines() { return this.articleHeader.get("Lines"); }
 	public String getArticleBytes() { return this.articleHeader.get("Bytes"); }
 	public String getArticlePath() { return this.articleHeader.get("Path"); }
 	public String getArticleFrom() { return this.articleHeader.get("From"); }
 	public String getArticleNewsgroups() { return this.articleHeader.get("Newsgroups"); }
 	public String getArticleMessageID() { return this.articleHeader.get("Message-ID"); }
 	public String getArticleOrganization() { return this.articleHeader.get("Organization"); }
 	public String getArticleNNTPPostingHost() { return this.articleHeader.get("NNTP-Posting-Host"); }
 	public String getArticleXref() { return this.articleHeader.get("Xref"); }
 	
 	/**
 	 * sets the current article 
 	 *
 	 * @deprecated use {@link #statArticle(String)}, {@link #headArticle(String)}, {@link #getArticle(String)}, {@link #bodyArticle(String)}
 	 * @param articleName the name of the article
 	 */
 	
 	public void setArticleName (String articleName) throws IOException , NNTPNoSuchArticleException { 
 		getArticle(articleName);
 	}
 		
 	
 	/**
 	 * Sends the ARTICLE command to the News server and allows reading by the IOStream calls. 
 	 *
 	 * @param articleName the name of the article
 	 * @exception NNTPNoSuchArticleException if the article cannot be found or if a articleNumber is used and there is no current group.
 	 * @exception IOException if a network error occurs
 	 */
 	
 	public void getArticle (String articleName) throws IOException , NNTPNoSuchArticleException { 
 
 		// this.close();
 		
 		
 		setEndCommandDot();
 		sendCommand("ARTICLE " + articleName + "\r\n");
 		try {
 			checkResponse("220");
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPNoSuchArticleException(e.getMessage(),articleName);
 		}
 		this.articleName = articleName; 
 
 		start = System.currentTimeMillis();
 		split = start;
 		
 		//articleBuffer = this.readAsString().getBytes();
 
 	}
 	
 	/**
 	 * Sends the BODY command to the News server and allows reading by the IOStream calls. 
 	 *
 	 * @param articleName the name of the article
 	 * @exception NNTPNoSuchArticleException if the article cannot be found or if a articleNumber is used and there is no current group.
 	 * @exception IOException if a network error occurs
 	 */
 	
 	public void bodyArticle (String articleName) throws IOException , NNTPNoSuchArticleException { 
 		
 		
 	//	this.close();
 		
 		
 		setEndCommandDot();
 		sendCommand("BODY " + articleName + "\r\n");
 		try {
 			checkResponse("222");
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPNoSuchArticleException(e.getMessage(),articleName);
 		}
 		this.articleName = articleName; 
 
 		start = System.currentTimeMillis();
 		split = start;
 		
 	//	articleBuffer = this.readAsString().getBytes();
 		
 		
 	//	System.out.println("Read bytes: " + articleBuffer.length);
 		
 	}
 	
 	/**
 	 * sends the HEAD command to the news server and sets the current article to the response.
 	 * stores the information about the current article.
 	 *
 	 * @param article the name of the article.
 	 * @exception IOException if a network error occurs
 	 * @exception NNTPNoSuchArticleException if the article cannot be found or if a articleNumber is used and there is no current group.
 	 */
 	
 	
 	public void headArticle(String article) throws IOException,NNTPNoSuchArticleException {
 		
 		articleHeader = new HashMap<String,String>();
 		String r ;
 		
 		// the dot end of data marker is only used if the correct response is received.
 		setEndCommandDot();
 		sendCommand("HEAD " + article + "\r\n");
 		
 		try {
 			r= checkResponse("221");		
 		} catch (NNTPUnexpectedResponseException e) {
 			throw new NNTPNoSuchArticleException(article + ": " + e.getMessage(),article);
 		}
 		
 		this.articleName = article; 
 
 		while (".".compareTo(r) != 0 ) {
 			r = readLine();
 			
 			// System.out.println("read: " + r);
 			
 			String[] s = splitHeader(r);
 			
 			articleHeader.put(s[0],s[1]);
 			
 		}
 		
 	}
 	
 	/**
 	 * splits the input string on the first colon seen.
 	 *
 	 * @param s the string to split
 	 * @return a String array containing the key and value.
 	 */
 	
 	
 	private String[] splitHeader(String s)
 	{
 		int colon = s.indexOf(": ");
 		String r[] = new String[2];
 		
 		if (colon != -1) {
 			r[0]  = new String(s.getBytes(),0,colon);
 			r[1] = new String(s.getBytes(),colon+2,s.length()-colon-2);
 		}
 		return r;
 	}
 	
 	
 	/**
 	 * reads the current article as a String
 	 *
 	 * @deprecated use {@link #readAsString()}
 	 * @exception IOException if a network error occurs
 	 */
 	
 	public String getArticleAsString() throws IOException {
 		return this.readAsString();
 	}
 	
 	
 	/**
 	 * reads the current article into a File.
 	 *
 	 * @param f the File to be written to.
 	 * @exception IOException if a network error or Disk error occurs
 	 */
 	
 	public long writeArticleToFile(File f) throws IOException {
 		
 		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
 		
 		return copy(this,out,true);
 		
 	}
 	
 	public void interupt() throws IOException { network.disconnect(); }
 	
 	// stolen from
 	// http://www.java2s.com/Code/Java/File-Input-Output/CopiesthecontentsofthegivenInputStreamtothegivenOutputStream.htm
 	
 	/**
      * Copies the contents of the given {@link InputStream}
      * to the given {@link OutputStream}. Shortcut for
      * <pre>
      *   copy(pInputStream, pOutputStream, new byte[8192]);
      * </pre>
      * @param pInputStream The input stream, which is being read.
      * It is guaranteed, that {@link InputStream#close()} is called
      * on the stream.
      * @param pOutputStream The output stream, to which data should
      * be written. May be null, in which case the input streams
      * contents are simply discarded.
      * @param pClose True guarantees, that {@link OutputStream#close()}
      * is called on the stream. False indicates, that only
      * {@link OutputStream#flush()} should be called finally.
      *
      * @return Number of bytes, which have been copied.
      * @throws IOException An I/O error occurred.
      */
     protected static long copy(InputStream pInputStream,
 							OutputStream pOutputStream, boolean pClose)
 	throws IOException {
         return copy(pInputStream, pOutputStream, pClose,
 					new byte[BUFFER_SIZE]);
     }
 	
     /**
      * Copies the contents of the given {@link InputStream}
      * to the given {@link OutputStream}.
      * @param pIn The input stream, which is being read.
      *   It is guaranteed, that {@link InputStream#close()} is called
      *   on the stream.
      * @param pOut The output stream, to which data should
      *   be written. May be null, in which case the input streams
      *   contents are simply discarded.
      * @param pClose True guarantees, that {@link OutputStream#close()}
      *   is called on the stream. False indicates, that only
      *   {@link OutputStream#flush()} should be called finally.
      * @param pBuffer Temporary buffer, which is to be used for
      *   copying data.
      * @return Number of bytes, which have been copied.
      * @throws IOException An I/O error occurred.
      */
     protected static long copy(InputStream pIn,
 							OutputStream pOut, boolean pClose,
 							byte[] pBuffer)
     throws IOException {
         OutputStream out = pOut;
         InputStream in = pIn;
         try {
             long total = 0;
             for (;;) {
                 int res = in.read(pBuffer);
                 if (res == -1) {
                     break;
                 }
                 if (res > 0) {
                     total += res;
                     if (out != null) {
                         out.write(pBuffer, 0, res);
                     }
                 }
             }
             if (out != null) {
                 if (pClose) {
                     out.close();
                 } else {
                     out.flush();
                 }
                 out = null;
             }
             in.close();
             in = null;
             return total;
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (Throwable t) {
                     /* Ignore me */
                 }
             }
             if (pClose  &&  out != null) {
                 try {
                     out.close();
                 } catch (Throwable t) {
                     /* Ignore me */
                 }
             }
         }
     }
 	
 	
 	
 }
