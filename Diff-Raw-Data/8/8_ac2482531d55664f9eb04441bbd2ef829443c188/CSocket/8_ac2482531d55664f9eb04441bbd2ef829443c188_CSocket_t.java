 package ngmud.network;
 
 import java.net.*;
 import java.io.*;
 import ngmud.ngMUDException;
 
 
 public class CSocket{
 	public CSocket()
 	{
 		Sock=null;
 		Inited=false;
 	}
 	public CSocket(CServer Server)
 	{
 		Sock=null;
 		Inited=false;
 		this.Server=Server;
 	}
 	
 	protected void finalize()
 	{
 		UnInit();
 	}
 	
 	public synchronized boolean IsInited()
 	{
 		return Inited;
 	}
 	
 	public synchronized boolean IsConnected()
 	{
 		if(!Inited || Sock==null)
 		{
 			return false;
 		}
 		if(Sock.isConnected())
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public synchronized int DataAvailable()
 	{
 		if(!Inited)
 		{	return -1;	}
 		try
 		{
 			return BufIn.available();
 		}
 		catch(IOException e)
 		{
 			return -1;
 		}
 	}
 	
 	public int Available()
 	{
 		return DataAvailable();
 	}
 	
 	protected synchronized void ThrowInitException() throws ngMUDException
 	{
 		throw new ngMUDException("Socket already initialized.");
 	}
 	
 	public synchronized boolean Init(Socket Sock) throws ngMUDException
 	{
 		if(Inited)
 		{
 			ThrowInitException();
 		}
 		if(!Sock.isConnected())
 		{
 			return false;
 		}
 		
 		this.Sock=Sock;
 		return InitStreams();
 	}
 	
 	public synchronized boolean SetSoOptions(boolean NoDelay,int Timeout)
 	{
 		if(!Inited)
 		{
 			return false;
 		}
 		try
 		{
 			Sock.setTcpNoDelay(NoDelay);
 			Sock.setSoTimeout(Timeout);
 		}
 		catch(IOException e)
 		{
 			return false;
 		}
 		return true;
 	}
 	
 	public synchronized boolean Init(InetSocketAddress Address,int Port,
 									 InetSocketAddress LocalAddress,int LocalPort,int Timeout,
 									 boolean NoDelay) throws ngMUDException
 	{
 		if(Inited)
 		{
 			ThrowInitException();
 		}
 		try
 		{
 			if(LocalAddress!=null)
 			{
 				Sock=new Socket(Address.getAddress(),Port,LocalAddress.getAddress(),LocalPort);
 			}
 			else
 			{
 				Sock=new Socket(Address.getAddress(),Port);
 			}
 		}
 		catch(IOException e)
 		{
 			return false;
 		}
 		return InitStreams();
 	}
 
 	protected synchronized boolean InitStreams()
 	{
 		if(!Sock.isConnected())
 		{
 			return false;
 		}
 		
 		try
 		{
 			BufIn=new BufferedInputStream(this.Sock.getInputStream());
 			StreamOut=new DataOutputStream(this.Sock.getOutputStream());
 			StreamIn=new DataInputStream(BufIn);
 		}
 		catch(IOException e)
 		{
 			return false;
 		}
 		if(Server!=null)
 		{
 			Server.ConChange(true);
 		}
 		Inited=true;
 		return true;
 	}
 	
 	public synchronized DataInputStream in()
 	{	return StreamIn;	}
 	
 	public synchronized DataOutputStream out()
 	{	return StreamOut;	}
 	
 	public long Skip(long Skip)
 	{
 		if(!Inited)
 		{	return -1;	}
 		try {
 			return BufIn.skip(Skip);
 		}
 		catch(IOException e)
 		{	return -1;	}
 	}
 	
 	public boolean Flush()
 	{
 		if(!Inited)
 		{	return false;	}
 		try {
 			StreamOut.flush();
 		}
 		catch(IOException e)
 		{	return false;	}
 		
 		return true;
 	}
 	
 	public synchronized void UnInit()
 	{
 		try {
 			StreamIn.close();
 			StreamIn=null;
 			StreamOut.close();
 			StreamOut=null;
 			BufIn.close();
 			BufIn=null;
 			Sock.close();
 			Sock=null;
 		}
 		catch (IOException e)
 		{
 		}
 		Inited=false;
 		if(Server!=null)
 		{
 			Server.ConChange(false);
 		}
 	}
 	
 	protected boolean Inited;
	protected Socket Sock;
 	protected CServer Server;
 	protected DataInputStream StreamIn;
 	protected DataOutputStream StreamOut;
 	protected BufferedInputStream BufIn;
 }
