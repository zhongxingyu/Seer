 import java.net.*;
 import java.io.*;
 
 public class ClientHandler implements Runnable
 {
 	private Socket client;
 
 	ClientHandler(Socket client)
 	{
 		this.client = client;
 	}
 
 	public void run()
 	{
 		BufferedReader inputReader;
 		PrintStream outputStream;
 		URL url;
 		HttpRequest request;
 		HttpResponse response;
 		Socket upstreamSocket;
 
 		try
 		{
 			inputReader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
 			outputStream = new PrintStream(this.client.getOutputStream());
 		}
 		catch(IOException e)
 		{
 			System.out.println("Failed to initialize input/output streams!");
 			return;
 		}
 
 		while(true)
 		{
 			if(this.client.isClosed()) break;
 			request = null;
 			response = null;
 			upstreamSocket = null;
 			url = null;
 
 			try
 			{
 				request = new HttpRequest(inputReader);
 			}
 			catch(HttpParseException e)
 			{
 				System.out.println("Error parsing HTTP message: " + e.getMessage() + " (" +
 					this.client.getInetAddress() + ":" + this.client.getPort() + ")");
 				response = new HttpResponse(400, "Bad Request");
 			}
 
 			if(request != null && request.method.equals("CONNECT"))
 			{
 				response = new HttpResponse(501, "Not Implemented");
 			}
 			
 			if(response == null)
 			{
 				try
 				{
 					url = new URL(request.uri);
 				}
 				catch(MalformedURLException e)
 				{
 					response = new HttpResponse(400, "Bad Request");
 				}
 				if(url != null)
 				{
 					// Rewrite the request with the upstream URL and host
 					request.uri = url.getPath();
 					if(url.getQuery() != null) request.uri += "?" + url.getQuery();
 
 					request.removeHeader("Host");
 					request.addHeader("Host", url.getHost());
 
 					try
 					{
 						upstreamSocket = new Socket(DnsCache.get(url.getHost()), url.getPort() < 0 ? 80 : url.getPort());
 					}
 					catch(UnknownHostException e)
 					{
 						response = new HttpResponse(404, "Not Found");
 					}
 					catch(IOException e)
 					{
 						response = new HttpResponse(504, "Gateway Timeout");
 					}
 
 					if(response == null && upstreamSocket != null)
 					{
 						try
 						{
 							OutputStream output = upstreamSocket.getOutputStream();
 							output.write(request.serialize().getBytes());
 						}
 						catch(IOException e)
 						{
 							response = new HttpResponse(500, "Internal Server Error");
 						}
 					}
 
 					if(response == null)
 					{
 						try
 						{
 							response = new HttpResponse(upstreamSocket.getInputStream());
 						}
 						catch(IOException e)
 						{
 							response = new HttpResponse(502, "Bad Gateway");
 						}
 						catch(HttpParseException e)
 						{
 							response = new HttpResponse(502, "Bad Gateway");
 						}
 					}
 
 					try
 					{
 						upstreamSocket.close();
 					}
 					catch(IOException e)
 					{
 						// If we got this far, might as well finish handling the request
 					}
 				}
 
 				try
 				{
 					outputStream.write(response.serialize().toByteArray());
 					if(url != null)
 						System.out.println(request.method + " " + url.getHost() + request.uri + " " + response.statusCode);
 					else
 						System.out.println(request.method + " " + " " + response.statusCode);
 				}
 				catch(IOException e)
 				{
 					System.out.println("Failed to send response to client " +
 						this.client.getInetAddress() + ":" + this.client.getPort());
 				}
 				if(response.statusCode == 400)
 				{
 					// Chrome in particular doesn't give up when sending its
 					// cute not-really HTTP requests
 					try
 					{
 						this.client.close();
 					}
 					catch(IOException e) { }
 				}
 			}
 		}
 	}
 }
