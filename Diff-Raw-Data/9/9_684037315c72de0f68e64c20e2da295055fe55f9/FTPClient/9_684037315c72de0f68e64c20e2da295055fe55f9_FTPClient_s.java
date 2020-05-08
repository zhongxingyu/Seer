 /*
  * Copyright (c) 2002, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.ftp;
 
 import java.io.*;
 import java.net.*;
 
 /**
  * An FTP client. See RFC-959.
  *
  * Pathnames must be specified using '/' for directory separator.
  * Passive mode will be used for all transfers.
  * <em>Not</em> thread-safe, i.e. you cannot start a new file while another one is in
  * progress. 
  *
  * <strong>Note:</strong> This class will transmit password in clear text over
  * the network.
  */
 public class FTPClient
 {
 	private static final boolean DEBUG = false;
 
     private Socket control;
     private InputStream controlIn;
     private OutputStream controlOut;
    	private String respString;
 
     private String lastPath = "";
     private int lastPathLen = 0;
 
 
 	private void sendLine(String str)
 		throws IOException
 	{
 		controlOut.write(str.getBytes("US-ASCII"));
 		controlOut.write('\r');
 		controlOut.write('\n');
 		controlOut.flush();
 	}
 
 	
 	private String recvLine()
 		throws EOFException, IOException
 	{
 		StringBuffer sb = new StringBuffer();
 
 		while (true)
 		{
 			int i = controlIn.read();
 			if (i < 0) throw new EOFException("Unexpected EOF when reading socket");
 			byte b = (byte)i;
 			if (b == '\n') break;
 			if (b != '\r') sb.append((char)b);
 		}
 
 		return sb.toString();
 	}
 
 
 	private int recvResponse()
 		throws EOFException, IOException
 	{
 		respString = recvLine();
 		String code = respString.substring(0,3);
 		if (respString.charAt(3) == '-') // multiline response
 		{
 			String endMark = code + ' ';
 			while (true)
 			{
 				respString = recvLine();
 				if (respString.startsWith(endMark))
 					break;
 			}
 		}
 		return Integer.parseInt(code);
 	}
 
 	
 	/**
 	 * Connect to an FTP server and login.
 	 *
 	 * @param url  an URL specifying host, port, username and optionally
 	 *             an initial path to change to
 	 * @param password  the password to use for logging in
 	 *
 	 * @throws MalformedURLException  if there are any syntactic error in the URL
 	 * @throws UnknownHostException  if the hostname specified doesn't exist
 	 * @throws FTPAuthenticationException  if the password is wrong
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
 	 */
     public FTPClient(String url, String password)
 	    throws MalformedURLException, UnknownHostException,
         	FTPAuthenticationException, FTPException, IOException 
     {
 		if (!url.startsWith("ftp://"))
 			throw new MalformedURLException(url);
 
 		int userPos = 5;
 		int hostPos = url.indexOf('@', userPos+1);
 		if (hostPos < 0)
 			throw new MalformedURLException(url);
 		int portPos = url.indexOf(':', hostPos+1);
 		int pathPos = url.indexOf('/', ((portPos<0) ? hostPos : portPos)+1);
 
 		String username;
 		String host;
 		int port;
 		String path;
 
 		try
 		{
 			username = url.substring(userPos+1,hostPos);
 			host = url.substring(hostPos+1,(portPos<0) ? pathPos : portPos);
 			port = (portPos<0) ? 21 : Integer.parseInt(url.substring(portPos+1,pathPos));
 			path = url.substring(pathPos+1);
 		}
 		catch (NumberFormatException e)
 		{
 			throw new MalformedURLException(url);
 		}
 
 		if (port < 0 || port > 65535 ||
 			username.length() < 1 ||
 			host.length() < 1 ||
 			(path.length() > 0 && path.charAt(path.length()-1) != '/'))
 			throw new MalformedURLException(url);
 			
 		connect(host, port, username, password);
 		initialDir(path);				
 	}
 
 		
 	/**
 	 * Connect to an FTP server and login.
 	 *
 	 * @param host  the host to connect to, may be a domain name or IP address
	 * @param post  the control channel port (default 21)
 	 * @param username  the username to use for logging in
 	 * @param password  the password to use for logging in
 	 * @param path  initial path, realtive to home directory unless starting with '/',
 	 *              may be <code>null</code> to use home directory
 	 *
 	 * @throws UnknownHostException  if the hostname specified doesn't exist
 	 * @throws FTPAuthenticationException  if the password is wrong
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
 	 */
 	public FTPClient(String host, int port, String username, String password, String path)
 	    throws UnknownHostException, FTPAuthenticationException, FTPException, IOException 
 	{
 		connect(host, port, username, password);
 		initialDir(path);
 	}
 	
 
 	private void connect(String host, int port, String username, String password)
 	    throws UnknownHostException, FTPAuthenticationException, FTPException, IOException 
 	{
 		if (port < 0 || port > 65535) port = 21;
 			
 		control = new Socket(host, port);
 		controlIn = control.getInputStream();
 		controlOut = control.getOutputStream();
 
 		int resp;
 
 		// Receive greeting message
 		greeting: while (true)
 		{
 			resp = recvResponse();
 			switch (resp)
 			{
 				case 120:
 					continue greeting;
 
 				case 220:
 					break greeting;
 
 				case 421:
 					throw new FTPException("FTP server not avaliable (421)");
 
 				default:
 					throw new FTPException("Unexpected response from FTP server: " + respString);
 			}
 		}
 
 		sendLine("USER " + username);
 		resp = recvResponse();
 		switch (resp)
 		{
 			case 230:
 				break;
 
 			case 331:
 				sendLine("PASS " + password);
 				resp = recvResponse();
 				switch (resp)
 				{
 					case 230:
 						break;
 
 					case 530:
                         throw new FTPAuthenticationException();
 
 					case 421:
 						throw new FTPException("FTP server not avaliable (421)");
 
 					default:
 						throw new FTPException("Unexpected response from FTP server: " + respString);
 				}
 				break;
 
 			case 530:
 				throw new FTPException("Invalid username");
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 	}
 	
 
 	private void initialDir(String path)
 		throws FTPException, IOException	
 	{
 		if (path == null) return;
 		int pos, oldPos = 0;
         while (true)
         {
             pos = path.indexOf('/', oldPos);
             if (pos < 0) break;
             String comp = path.substring(oldPos, pos);
             if (!chdir(comp))
 				throw new FTPException("Path not found: " + path);
             oldPos = pos + 1;
         }	
 	}
 	
 
     /**
      * Logout and disconnect from the FTP server.
      *
      * After this method has been invoked, no other method may be invoked.
      */
     public void close()
     	throws IOException
     {
 		sendLine("QUIT");
 		int resp = recvResponse();
 		control.close();
         control = null;
     }
 
 	
 	/**
 	 * Change the current directory.
 	 * 
 	 * @param dir  the directory to change into
 	 * 
 	 * @return true if successful, false otherwise
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
 	 */
 	private boolean chdir(String dir)
 		throws FTPException, IOException
 	{
 		if (DEBUG) System.out.println("CWD " + dir);
 		sendLine("CWD " + dir);
 		int resp = recvResponse();
 		switch (resp)
 		{
 			case 250:
 				return true;
 
 			case 550:
 				return false;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 	}
 
 	
 	/**
 	 * Change the current directory to the parent directory.
 	 * 
 	 * @return true if successful, false otherwise
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
 	 */
 	private boolean cdup()
 		throws FTPException, IOException
 	{
 		if (DEBUG) System.out.println("CDUP");
 		sendLine("CDUP");
 		int resp = recvResponse();
 		switch (resp)
 		{
 			case 200:
 			case 250:
 				return true;
 
 			case 550:
 				return false;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 	}
 
 
 	/**
 	 * Create a new directory.
 	 *
 	 * @param dir  the directory to create
 	 * 
 	 * @return true if successful, false otherwise
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
 	 */
 	private boolean mkdir(String dir)
 		throws FTPException, IOException
 	{
 		if (DEBUG) System.out.println("MKD " + dir);
 		sendLine("MKD " + dir);
 		int resp = recvResponse();
 		switch (resp)
 		{
 			case 257:
 				return true;
 
 			case 550:
 				return false;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 	}
 
 
     /**
      * Create a new file, or overwrite an existing file. 
 	 * Will create directories as nessesary.
      *
      * @param pathname  path to the file
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream store(String pathname)
         throws FTPException, IOException
 	{
 		return store(pathname, false);
 	}
 
 
     /**
      * Create a new file, or overwrite an existing file. 
 	 * Will create directories as nessesary.
      *
      * @param pathname  path to the file
 	 * @param last  close the FTPClient after transferring this file
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream store(String pathname, boolean last)
         throws FTPException, IOException
 	{
 		int pos = pathname.lastIndexOf('/');
 	    String path = pathname.substring(0, pos+1);
 	    String filename = pathname.substring(pos+1);
 
 		changeDir(path);
 		return upload("STOR", filename, last);		
 	}
     
 
     /**
      * Create a new file, or append to an existing file. 
 	 * Will create directories as nessesary.
      *
      * @param pathname  path to the file
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream append(String pathname)
         throws FTPException, IOException
 	{
 		return append(pathname, false);
 	}
 
 
     /**
      * Create a new file, or append to an existing file. 
 	 * Will create directories as nessesary.
      *
      * @param pathname  path to the file
 	 * @param last  close the FTPClient after transferring this file
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream append(String pathname, boolean last)
         throws FTPException, IOException
 	{
 		int pos = pathname.lastIndexOf('/');
 	    String path = pathname.substring(0, pos+1);
 	    String filename = pathname.substring(pos+1);
 
 		changeDir(path);
 		return upload("APPE", filename, last);		
 	}
 
 
     /**
      * Create a new file with an unique name. 
 	 * Will create directories as nessesary.
      *
      * @param path  path to the file, must end with '/' or be empty
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream storeUnique(String path)
         throws FTPException, IOException
 	{
 		return storeUnique(path, false);		
 	}
 
 
     /**
      * Create a new file with an unique name. 
 	 * Will create directories as nessesary.
      *
      * @param path  path to the file, must end with '/' or be empty
 	 * @param last  close the FTPClient after transferring this file
 	 *
 	 * @return an OutputStream to write to, close() it when finished
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public OutputStream storeUnique(String path, boolean last)
         throws FTPException, IOException
 	{
 		changeDir(path);
 		return upload("STOU", null, last);		
 	}
 
 
 	private void changeDir(String path)
         throws FTPException, IOException
 	{
 		if (!path.equals(lastPath))
 		{
 			if (path.length() > 0 && path.charAt(0) != '/') path = '/' + path; 
 			
 			// change directory
 			for (int i = 0; i < lastPathLen; i++)
 				if (!cdup())
 					throw new FTPException("Unable to change to parent directory");
 
 			lastPathLen = 0;
 			int pos, oldPos = 1;
 			boolean mkd = false;
 			while (true)
 			{
 				pos = path.indexOf('/', oldPos);
 				if (pos < 0) break;
 				lastPathLen++;
 				String comp = path.substring(oldPos, pos);
 				if (mkd)
 				{
 					if (!mkdir(comp))
 						throw new FTPException("Unable to create directory: " + comp);
 					if (!chdir(comp))
 						throw new FTPException("Unable to change into newly created directory: " + comp);
 				}
 				else
 				{
 					if (!chdir(comp))
 					{
 						if (!mkdir(comp))
 							throw new FTPException("Unable to create directory: " + comp);
 						if (!chdir(comp))
 							throw new FTPException("Unable to change into newly created directory: " + comp);
 						mkd = true;
 					}
 				}
 				oldPos = pos + 1;
 			}
 		}
 		lastPath = path;		
 	}
 	
 	
 	private OutputStream upload(String cmd, String filename, boolean last)
         throws FTPException, IOException
 	{
 		int resp;
 
 		sendLine("TYPE I");
 		resp = recvResponse();
 		switch (resp)
 		{
 			case 200:
 				break;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 
 		sendLine("PASV");
 		resp = recvResponse();
 		switch (resp)
 		{
 			case 227:
 				break;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 
 		InetAddress addr;
 		int port;
 		String s = respString.replace(',', '.');
 		int i = 3;
 
 		while (i < s.length() && !Character.isDigit(s.charAt(i))) i++;
 		if (i == s.length()) throw new FTPException("invalid response to PASV command");
 		int c1 = s.indexOf('.',i);
 		if (c1 < 0) throw new FTPException("invalid response to PASV command");
 		int c2 = s.indexOf('.',c1+2);
 		if (c2 < 0) throw new FTPException("invalid response to PASV command");
 		int c3 = s.indexOf('.',c2+1);
 		if (c3 < 0) throw new FTPException("invalid response to PASV command");
 		int c4 = s.indexOf('.',c3+1);
 		if (c4 < 0) throw new FTPException("invalid response to PASV command");
 		int c5 = s.indexOf('.',c4+1);
 		if (c5 < 0) throw new FTPException("invalid response to PASV command");
 		try {
 			addr = InetAddress.getByName(s.substring(i,c4));
 
 			i = c5+1;
 			while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
 			int portA = Integer.parseInt(s.substring(c4+1,c5));
 			int portB = Integer.parseInt(s.substring(c5+1,i));
 			port = (portA<<8) + portB;
 		}
 		catch (UnknownHostException e)
 		{
 			throw new FTPException("invalid response to PASV command");
 		}
 		catch (NumberFormatException e)
 		{
 			throw new FTPException("invalid response to PASV command");
 		}
 
 		if (cmd == null) 
 			sendLine(cmd); 
 		else
 			sendLine(cmd + " " + filename);
 			
 		Socket data = new Socket(addr, port);
 		resp = recvResponse();
 		switch (resp)
 		{
 			case 125:
 			case 150:
 				break;
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 
 		return new FTPOutputStream(data.getOutputStream(), data, last ? this : null);
     }
 
 	
     /**
      * Deletes a file.
 	 *
      * @param pathname  path to the file
 	 *
 	 * @return true if successful, false otherwise (file didn't exsist)
 	 * @throws FTPException if any FTP protocol error occurs
 	 * @throws IOException if any other IO error occurs
      */
     public boolean deleteFile(String pathname)
         throws FTPException, IOException
     {
 		String path;
 		String fn;
 		int pos = pathname.lastIndexOf('/');
 		path = pathname.substring(0, pos+1);
 		fn = pathname.substring(pos+1);
 
 		if (!path.equals(lastPath))
 		{
 			// change directory
 			for (int i = 0; i < lastPathLen; i++)
 				if (!cdup())
 					throw new FTPException("Unable to change to parent directory");
 
 			lastPathLen = 0;
 			int oldPos = 1;
 			while (true)
 			{
 				pos = path.indexOf('/', oldPos);
 				if (pos < 0) break;
 				lastPathLen++;
 				String comp = path.substring(oldPos, pos);
 				if (!chdir(comp))
 				{
 					return false; // file doesn't exist
 				}
 				oldPos = pos + 1;
 			}
 		}
 		lastPath = path;
 
 		sendLine("DELE " + fn);
 		int resp = recvResponse();
 		switch (resp)
 		{
 			case 250:
 				return true;
 			
 			case 550:
 				return false;
 
 			case 450:
 				throw new FTPException("Unable to delete file: " + respString);
 
 			case 421:
 				throw new FTPException("FTP server not avaliable (421)");
 
 			default:
 				throw new FTPException("Unexpected response from FTP server: " + respString);
 		}
 	}
 
 
 	class FTPOutputStream extends OutputStream
 	{
 		private Socket data;
 		private OutputStream out;
 		private FTPClient ftp;
 
 		
 		FTPOutputStream(OutputStream out, Socket data, FTPClient ftp)
 		{
 			this.out = out;
 			this.data = data;
 			this.ftp = ftp;
 		}
 
 
 		public void write(int b)
             throws IOException
 		{
 			out.write(b);	
 		}
 
 					
 		public void write(byte[] b)
             throws IOException
 		{
 			out.write(b);	
 		}
 
 		   
 		public void write(byte[] b, int off, int len)
             throws IOException
 		{
 			out.write(b, off, len);	
 		}
 
 		   
 		public void flush()
         	throws IOException
 		{
 			out.flush();	
 		}
 
 		
 		public void close()
 			throws IOException
 		{
 			out.close();
 
 			if (data != null)
 			{
 				data.close();
 				data = null;
 			}
 	
 			theLoop: while (true)
 			{
 				int resp = recvResponse();
 				switch (resp)
 				{
 					case 226:
 					case 250:
 						break;
 	
 					case 425:
 					case 426:
 					case 451:
 					case 551:
 					case 552:
 						throw new FTPException("Error in file transfer (" + resp + ")");
 	
 					case 421:
 						throw new FTPException("FTP server not avaliable (421)");
 	
 					default:
 						throw new FTPException("Unexpected response from FTP server: " + respString);
 				}
 				break;
 			}
 			
 			if (ftp != null) ftp.close(); 
 		}				
 	}	
 	
 }
 
