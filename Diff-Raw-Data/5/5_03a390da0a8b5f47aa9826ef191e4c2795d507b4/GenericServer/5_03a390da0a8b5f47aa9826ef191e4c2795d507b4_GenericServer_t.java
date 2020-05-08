 package com.davecoss.android.genericserver;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import org.apache.commons.io.input.BoundedInputStream;
 
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.FileNotFoundException;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.SocketException;
 import java.net.Socket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.net.ssl.SSLException;
 
 public class GenericServer implements Runnable {
 
 	protected int port = DEFAULT_PORT;
 	protected ServerSocket listener;
 	protected String userdir = null;
 	protected InetAddress addr = null;
 	protected ServerHandler handler;
 	protected String outfile_name = "output.dat";
 	protected boolean has_write_permission = false;
 
 	public static final String DEFAULT_HOSTNAME = "localhost";
 	public static final int DEFAULT_PORT = 4242;
 	
 	public GenericServer(ServerHandler handler) throws UnknownHostException {
 		this.handler = handler;
 		this.addr = InetAddress.getByName("localhost");
 		this.port = DEFAULT_PORT;
 	}
 
 	public GenericServer(InetAddress addr, ServerHandler handler) {
 		this.handler = handler;
 		this.port = DEFAULT_PORT;
 		this.addr = addr;
 	}
 	
 	public GenericServer(InetAddress addr, int port, ServerHandler handler) {
 		this.handler = handler;
 		this.port = port;
 		this.addr = addr;
 	}
 
 	public HTTPReply process_request(HTTPRequest client_request) 
 			throws EmptyRequest{
 		if(client_request == null)
 			throw new EmptyRequest();
 		String uri = client_request.get_path();
 		if (uri == null)
 			return HTMLReply.invalid_request();
 
 		ArrayList<String> request = new ArrayList<String>(
 				Arrays.asList(uri.split("/")));
 
 		if (request.size() < 2) {
 			return new HTMLReply("Welcome", "Welcome to the server.");
 		}
 		request.remove(0);
 		if (request.get(0).equals("date") || request.get(0).equals("unixtime")) {
 			return send_date(request);
 		} else if (request.get(0).equals("user") && request.size() > 1) {
 			try {
 				return process_user_request(request);	
 			}
 			catch(HTTPError httpe)
 			{
 				String err = "Error Processing user request: " + httpe.getMessage();
 				handler.debug("GenericServer.process_request", err);
 				return new HTMLReply("Error processing user request", err, HTTPReply.STATUS_ERROR);
 			}
 		} else if (request.get(0).equals("info")) {
 			String err_msg = "Error getting server info";
 			try {
 				ByteArrayOutputStream config_buffer = new ByteArrayOutputStream();
 			 	dump_config(config_buffer);
 				return new HTMLReply("Current config", config_buffer.toString().replaceAll("\n","<br/>\n"));
 			} catch (Exception e) {
 				handler.error("GenericServer.process_request", err_msg);
 				handler.traceback(e);
 				return new HTMLReply(err_msg, err_msg, HTTPReply.STATUS_ERROR);
 			}
 		} else if (request.get(0).equals("favicon.ico")) {
 			InputStream ico_stream = get_favicon();
 			if(ico_stream == null)
 			{
 				String no_favicon = "Favicon not found";
 				return new HTMLReply(no_favicon, no_favicon, HTTPReply.STATUS_NOT_FOUND);
 			}
 			else
 			{
 				 ImageReply retval = new ImageReply();
 				 retval.set_input_stream(ico_stream);
 				 retval.set_image_type("x-icon");
 				 return retval;
 			}
 		} else if (request.get(0).equals("echo")) {
 			return process_echo(client_request, request);
 		} else if (request.get(0).equals("teapot")) {
 		    return new HTTPReply("I'm a teapot", "short and stout", "HTTP/1.1 418 I'm a teapot");
 		} else if (request.get(0).equals("file")) {
 			try {
 				return process_file(client_request, request);
 			}
 			catch(IOException ioe)
 			{
 				String err = "Error Processing File: " + ioe.getMessage();
 				handler.debug("GenericServer.process_request", err);
 				return new HTMLReply("Error processing file", err, HTTPReply.STATUS_ERROR);
 			}
 			catch(HTTPError httpe)
 			{
 				String err = "Error Processing File: " + httpe.getMessage();
 				handler.error("GenericServer.process_request", err);
 				return new HTMLReply("Error processing file", err, HTTPReply.STATUS_ERROR);
 			}
 		}
 		
 		return new HTMLReply(request.get(0),
 					"You asked for (" + request.get(0) + ")");
 	}
 
 	@Override
 	public void run() {
 		boolean has_fatal_error = false;
 		handler.info("GenericServer.run", "Thread Run Called");
 		while (!Thread.currentThread().isInterrupted() && !has_fatal_error) {
 			try {
 				if (this.listener == null)
 					start_server();
 				handler.info("GenericServer.run", "Listening on port " + port);
 				Socket socket = listener.accept();
 				handler.info("GenericServer.run", "Opened socket on port " + port);
 				OutputStream out = socket.getOutputStream();
 				HTTPRequest request = null;
 				HTTPReply reply = null;
 				try {
 					@SuppressWarnings("resource") // This will be closed with socket.close()
 					BufferedReader input = new BufferedReader(new InputStreamReader(
 							new BoundedInputStream(socket.getInputStream(), UserFile.MAX_OUTFILE_SIZE)));
 					String input_text = input.readLine();
 					while (input_text != null
 							&& !Thread.currentThread().isInterrupted()) {
 						reply = HTMLReply.invalid_request();// Prove me wrong.
 						handler.info("GenericServer.run", "Client Said: " + input_text);
 						String[] request_tokens = input_text.split(" ");
 						int request_data_len = input_text.length();
 						if(request_data_len > 0 && request_tokens.length < 2)
 						{
 							handler.debug("GenericServer.run", "Invalid Request String Length: " + input_text);
 						}
 						else if (request_tokens[0].equals("HEAD") || 
 								request_tokens[0].equals("GET") || 
 								request_tokens[0].equals("POST"))
 						{
 							handler.debug("GenericServer.run", input_text);
 							request = new HTTPRequest(request_tokens[0], request_tokens[1]);
 						}
 						else if(request != null && request_data_len != 0)
 						{
 							try
 							{
 								request.put_request_data(input_text);
 							}
 							catch(InvalidRequestData ird)
 							{
 								handler.error("GenericServer.run", "Invalid Data from Client: " + ird);
 								handler.traceback(ird);
 							}
 						}
 						out.flush();
 
 						if (request_data_len == 0) {
 							if(request != null)
 							{
 								handler.info("GenericServer.run", "Received Request");
 								handler.info("GenericServer.run", request.toString()); 
 							}
 							break;
 						}
 						input_text = input.readLine();
 						if (input_text == "")
 							handler.debug("GenericServer.run", "Empty string");
 					}
 					try{
 						if(request != null && request.get_type() == HTTPRequest.RequestType.POST)
 						{
 							String len_as_str = request.get_request_data("Content-Length");
 							if(len_as_str != null)
 							{
 								try
 								{
 									int post_len = Integer.parseInt(len_as_str);
 									if(post_len >= UserFile.MAX_OUTFILE_SIZE)
 									{
 										String err = "Too much post data sent. Sent: " + post_len;
 										handler.debug("GenericServer.run", err);
 										reply = new HTMLReply("Error", err, HTTPReply.STATUS_FORBIDDEN);
 										continue;
 									}		
 									char[] buffer = new char[post_len];
 									handler.info("GenericServer.run", "Reading " + post_len + " bytes of post data");
 									input.read(buffer, 0, post_len);
 									String post_data = new String(buffer);
 									handler.info("GenericServer.run", "POST Data: " + post_data);
 									request.put_post_data(post_data);
 								}
 								catch(NumberFormatException nfe)
 								{
 									handler.error("GenericServer.run", "Invalid Content-Length: " + len_as_str);
 									handler.error("GenericServer.run", nfe.getMessage());
 									handler.traceback(nfe);
 								}
 							}
 						}
 						reply = process_request(request);
 					} catch (EmptyRequest er) {
 						handler.debug("GenericServer.run", "Empty Response");
 						reply = new HTMLReply("Empty Response", "Empty Response", HTTPReply.STATUS_ERROR);
 					} catch(HTTPError httperr) {
 						handler.error("GenericServer.run", "HTTP ERROR: " + httperr);
 						handler.traceback(httperr);
 						reply = new HTMLReply("ERROR", "Server Error", HTTPReply.STATUS_ERROR);
 					}
 				} finally {
 					if(reply != null && !socket.isClosed())
 					{
 						handler.debug("GenericServer.run", "Sending reply");
 						if(request != null && request.get_type() == HTTPRequest.RequestType.HEAD)
 						{
 							handler.debug("GenericServer.run", "Sending header");
 							reply.dump_head(out);
 						}
 						else
 						{
 							reply.write(out);
 						}
 					}
 					handler.info("GenericServer.run", "Closing socket");
 					socket.close();
 					handler.info("GenericServer.run", "Socket closed");
 				}
 			} catch (SSLException ssle) {
 				handler.error("GenericServer.run", "SSLException: " + ssle.getMessage());
 			} catch (SocketException se) {
 				if(!se.getMessage().equals("Socket closed") && !se.getMessage().equals("Socket is closed")) {
 					handler.error("GenericServer.run", "Socket closed");
 					handler.traceback(se);
 				}
 			} catch (IOException ioe) {
 				handler.error("GenericServer.run", "IOException: " + ioe.getMessage());
 				handler.traceback(ioe);
 			} catch(HTTPError httpe) {
 				handler.error("GenericServer.run", "Server Error");
 				handler.traceback(httpe);
 				has_fatal_error = true;
 			}
 		}// while not interrupted
 	}
 
 	protected ServerSocket get_new_socket() throws IOException, HTTPError {
 		handler.debug("GenericServer.get_new_socket", "Creating new Socket");
 		return new ServerSocket(this.port, 0, this.addr);
 	}
 	
 	protected synchronized void start_server() throws IOException, javax.net.ssl.SSLException, HTTPError {
 		handler.info("GenericServer.start_server", "Server starting " 
 				+ this.addr.getHostAddress() + ":" + this.port);
 		try {
 			if(listener != null && !listener.isClosed())
 				listener.close();
 			handler.info("GenericServer.start_server", "Getting new socket");
 			listener = get_new_socket();
 			handler.info("GenericServer.start_server", "Have socket");
 		} catch(javax.net.ssl.SSLException ssle) {
 			handler.error("GenericServer.start_server", "IOException: " + ssle.getMessage());
 			handler.traceback(ssle);
 			throw ssle;
 		} catch (IOException ioe) {
 			handler.error("GenericServer.start_server", "IOException: " + ioe.getMessage());
 			handler.traceback(ioe);
 		}
 	}
 
 	protected void start_server(InetAddress addr, int port) throws IOException, HTTPError {
 		this.port = port;
 		this.addr = addr;
 		start_server();
 	}
 
 	public void stop_server() {
 		if (this.listener != null && !this.listener.isClosed()) {
 			try {
 				handler.info("GenericServer.stop_server", "Closing ServerSocket");
 				this.listener.close();
 				handler.info("GenericServer.stop_server", "ServerSocket Closed");
 				
 			} catch (IOException ioe) {
 				handler.error("GenericServer.stop_server", "Could not close socket listener: " + ioe.getMessage());
 				handler.traceback(ioe);
 			}
 		}
 		this.listener = null;
 	}
 
 	public InetAddress get_address() {
 		return this.listener.getInetAddress();
 	}
 	
 	public void set_address(InetAddress new_address) {
 		addr = new_address;
 	}
 
 	public String get_port() {
 		if (this.listener == null)
 		{
 			handler.debug("GenericServer.get_port", "Port requested when listener is null");
 			return "";
 		}
 		return Integer.toString(this.listener.getLocalPort());
 	}
 	
 	public void set_port(int new_port) {
 		if (this.listener == null)
 			return;
 		this.port = new_port;
 	}
 
 	public String getdir() {
 		return userdir;
 	}
 
 	public String setdir(String dir) {
 		userdir = dir;
 		return dir;
 	}
 	
 	public synchronized boolean is_running() {
 		return (this.listener != null && !this.listener.isClosed());
 	}
 
 	protected HTTPReply send_date(ArrayList<String> request){
 		String date_string = "";
 		String arg = null;
 		long unixtime = System.currentTimeMillis() / 1000L;
 		
 		if(request.size() > 1) {
 			arg = request.get(1);
 		}
 		
 		if (request.get(0).equals("unixtime")) {
 			// Return a unix timestamp
 			if (arg != null) {
 				DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());  
 				try {
					unixtime = fmt.parse(arg).getTime()/1000L;
 				} catch (ParseException pe) {
 					handler.debug("GenericServer.send_date", "Error parsing date: " + arg);
 					return new HTTPReply("Invalid Date"," Invalid date: " + arg, HTTPReply.STATUS_ERROR);
 				}
 			}
 			date_string = Long.toString(unixtime);
 		} else {
 			// Return a date
 			if (arg != null) {
 				try {
 					unixtime = Long.valueOf(arg);
 				} catch(NumberFormatException nfe) {
 					return new HTTPReply("Invalid Unixtime", "Invalid unixtime: " + arg, HTTPReply.STATUS_ERROR);
 				}
 			}
			date_string = (new Date(unixtime*1000L)).toString();
 		}
 		
 		if (request.get(request.size() - 1).equals("json")) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("date", date_string);
 			return JSONReply.fromHashmap(map);
 		}
 		
 		return new HTMLReply(request.get(0), date_string);
 		
 	}
 
 	protected HTTPReply process_user_request(ArrayList<String> request) throws HTTPError {
 		if(request == null || request.size() < 2)
 			return HTMLReply.invalid_request();	
 
 		File requested_file = new File(request.get(1));	
 		for(int i = 2;i<request.size();i++)
 			requested_file = new File(requested_file, request.get(i));
 		String filename = requested_file.getPath();
 
 		String err = "";
 		if (userdir == null)
 			throw new HTTPError("User directory not defined.");
 		UserFile user_file = new UserFile(new File(userdir, filename));
 		UserFile.FileType filetype = user_file.get_filetype();
 		BufferedInputStream file = null;
 	
 		try {
 			file = user_file.get_input_stream();
 		} catch (SecurityException se) {
 			err = "Cannot read" + filename;
 			file = null;
 			handler.debug("GenericSerer.process_user_request", err + "\n" + se.getMessage());
 		} catch (FileNotFoundException fnfe) {
 			err = "File not found " + filename;
 			file = null;
 			handler.debug("GenericServer.process_user_request", err + "\n" + fnfe.getMessage());
 		}
 		
 		if (file == null) {
 			return new HTMLReply("File error", err, "HTTP/1.1 401 Permission Denied");
 		}
 		
 		FileReply retval = new FileReply();
 		retval.set_file_type(filetype);
 		retval.set_input_stream(file);
 
 		return retval;
 	}		
 
 	protected HTTPReply process_echo(HTTPRequest client_request, ArrayList<String> request) {
 		String echo = "";
 		if (request.size() > 1)
 			echo = request.get(1);
 		if (request.get(request.size() - 1).equals("json")) {
 			HashMap<String, String> content = new HashMap<String, String>();
 			content.put("data", echo);
 			if(client_request.has_post_data())
 			{
 				HashMap<String, String> post_data = client_request.get_full_post_data();
 				content.putAll(post_data);
 			}
 			return JSONReply.fromHashmap(content);
 			
 		} else {
 			if(!client_request.has_post_data())
 			{
 				return new HTMLReply(echo, echo);
 			}
 			else
 			{
 				String post_string = post_data_as_string(client_request);
 				return new HTMLReply(echo, echo + "\nPOST:\n" + post_string);
 			}
 		}
 	}
 
 	protected HTTPReply process_file(HTTPRequest client_request, ArrayList<String> request) throws HTTPError, IOException {
 		
 		if(!this.has_write_permission )
 			throw new FileError("File Writing Not Allowed");
 		
 		handler.info("GenericServer.process_file", "Processing file.");
 		if(!client_request.has_post_data())
 			throw new HTTPError("/file requires POST data");
 		
 		String filename = "";
 		if(request.size() >= 2)
 		{
 			filename = request.get(1);
 		}
 		else
 		{
 			filename = client_request.get_post_data("filename");
 			if(filename == null)
 				filename = "";
 		}
 		
 		String header = "\n--- Begin " + filename + " ---\n";
 		int header_len = header.length();
 		String footer = "\n--- End " + filename + " ---\n";
 		int footer_len = footer.length();
 	
 		String contents = client_request.get_post_data("content");
 		if(contents == null)
 			contents = "";
 		
 		if(userdir == null)
 			throw new HTTPError("User directory not specified");
 		
 		UserFile outfile = new UserFile(new File(userdir, this.outfile_name));
 		
 		try {
 			handler.info("GenericServer.process_file", "Opening " + outfile.get_absolute_path());
 			outfile.init_output();
 			outfile.write(header.getBytes(), 0, header_len); 
 			outfile.write(contents.getBytes(), 0, contents.length());
 			outfile.write(footer.getBytes(), 0, footer_len); 
 		}
 		finally {
 			if(outfile != null)
 			{
 				outfile.flush();
 				outfile.close();
 			}
 		}
 	
 		String msg = "Wrote " + contents.length() + " bytes to file.";
 		handler.info("GenericServer.process_file", msg);
 
 	
 		return new HTMLReply("Wrote to file", msg);
 	}
 
 
 	public static String post_data_as_string(HTTPRequest client_request) {
 		HashMap<String, String> post_data = client_request.get_full_post_data();
 		Iterator<String> it = post_data.keySet().iterator();
 		String post_string = "";
 		String key;
 		while(it.hasNext())
 		{
 			key = it.next();
 			post_string += key + "=" + post_data.get(key) + "\n";
 		}
 
 		return post_string;
 	}
 	
 	public void set_write_permission(boolean can_write)
 	{
 		this.has_write_permission = can_write;
 		handler.debug("GenericServer.set_write_permission", "Setting write permission to " + can_write);
 	}
 	
 	public boolean get_write_permission()
 	{
 		return this.has_write_permission;
 	}
 	
 	public void dump_config(OutputStream output) throws FileNotFoundException, IOException
 	{
 		handler.debug("GenericServer.dump_config", "Saving Configuration");
 		Properties config = new Properties();
 		
 		// Server Information
 		output.write("#Server Info\n".getBytes());
 		output.write("#Have Favicon? ".getBytes());
 		if(get_favicon() == null)
 			output.write("No\n".getBytes());
 		else
 			output.write("Yes\n".getBytes());
 		output.write("#Using SSL? ".getBytes());
 		if(this instanceof SSLServer)
 			output.write("Yes\n".getBytes());
 		else
 			output.write("No\n".getBytes());
 		
 		
 		Properties build_info = new BuildInfo().get_build_properties();
 		if(build_info != null)
 		{
 			handler.debug("GenericServer.dump_config", "Got Build Info");
 			Set<Entry<Object, Object>> keys = build_info.entrySet();
 			Iterator<Entry<Object, Object>> it = keys.iterator();
 			while(it.hasNext())
 			{
 				Entry<Object, Object> pair = it.next();
 				String prop_string = "#" + pair.getKey().toString() + ": " + pair.getValue().toString() + "\n";
 				//String prop_string = "#" + key + ": " + build_info.getProperty(key) + "\n";
 				output.write(prop_string.getBytes());
 			}
 		}
 		
 		output.write("#\n".getBytes());
 		
 		// Properties
 		config.setProperty("address", addr.getHostAddress());
 		config.setProperty("port", Integer.toString(port));
 		if(userdir != null)
 			config.setProperty("userdir", userdir);
 		config.setProperty("has_write_permission", Boolean.toString(has_write_permission));
 		
 		config.store(output, "Server Config");
 	}
 	
 	public void dump_config() throws FileNotFoundException, IOException
 	{
 		String conf_filename = "server.conf";
 		dump_config(new FileOutputStream(conf_filename));
 	}
 	
 	public void load_config() throws FileNotFoundException, IOException
 	{
 		String conf_filename = "server.conf";
 		Properties config = new Properties();
 		config.load(new FileInputStream(conf_filename));
 		
 		String val = config.getProperty("address", addr.getHostAddress());
 		addr = InetAddress.getByName(val);
 		
 		val = config.getProperty("port", Integer.toString(port));
 		port = Integer.parseInt(val);
 		
 		userdir = config.getProperty("userdir", userdir);
 		
 		val = config.getProperty("has_write_permission", Boolean.toString(has_write_permission));
 		has_write_permission = Boolean.parseBoolean(val);
 		
 		handler.debug("GenericServer.load_config", "Loaded configuration from " + conf_filename);
 	}
 	
 	public InputStream get_favicon() {
 		return this.getClass().getResourceAsStream("favicon.ico");
 	}
 	
 	public static ArrayList<String> get_ip_list() throws SocketException {
 		ArrayList<String> retval = new ArrayList<String>();
 		
 		for(Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements();)
 		{
 			NetworkInterface nic = nics.nextElement();
 			for(Enumeration<InetAddress> addrs = nic.getInetAddresses(); addrs.hasMoreElements();)
 			{
 				InetAddress addr = addrs.nextElement();
 				retval.add(addr.getHostAddress());
 			}
 		}
 		
 		return retval;
 	}
 	
 }
