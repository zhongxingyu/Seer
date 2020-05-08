 package com.hexcore.cas.control.protocol;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 public class CAPMessageProtocol extends Thread
 {
 	private static final int NO_BYTE = -2;
 	
 	private volatile boolean running = false;
 	private LinkedBlockingQueue<Message> messageQueue = null;
 	private Socket socket = null;

 	private BufferedInputStream inputStream;
 	private int currentByte = NO_BYTE;
 	
 	public CAPMessageProtocol(Socket socket)
 	{
 		this.socket = socket;
 		this.messageQueue = new LinkedBlockingQueue<Message>();
 		
 		try
 		{
 			inputStream = new BufferedInputStream(socket.getInputStream());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private Node readNode()
 	{
 		Node node = null;
 		byte b = peakByte();
 		switch(b)
 		{
 			case 'l':
 				node = list();
 				break;
 			case 'i':
 				node = intValue();
 				break;
 			case 'd':
 				node = dictionary();
 				break;
 			case 'f':
 				node = doubleValue();
 				break;
 			case '0':
 			case '1':
 			case '2':
 			case '3':
 			case '4':
 			case '5':
 			case '6':
 			case '7':
 			case '8':
 			case '9':
 				node = byteString();
 				break;
 			default:
 				System.out.println("Expected start of a node, got '" + (char)b + "'");
 		}
 		
 		return node;
 	}
 		
 	private ByteNode byteString()
 	{		
 		int num = 0;
 		while(peakByte() != ':') num = num * 10 + (nextByte() - '0');
 		
 		expect(':');
 		
 		ArrayList<Byte> list = new ArrayList<Byte>();
 		int cnt = 0;
 		while(cnt++ < num)
 		{
 			list.add(new Byte(peakByte()));
 			nextByte();
 		}
 		int s = list.size();
 		byte[] b = new byte[s];
 		for(int i = 0; i < s; i++)
 			b[i] = list.get(i).byteValue();
 		return new ByteNode(b);
 	}
 	
 	private DictNode dictionary()
 	{		
 		DictNode d = new DictNode();
 		
 		expect('d');
 		
 		while(peakByte() != 'e')
 		{			
 			String key;
 			Node value;
 						
 			//Keys
 			byte b = peakByte();
 			switch(b)
 			{
 				case '0':
 				case '1':
 				case '2':
 				case '3':
 				case '4':
 				case '5':
 				case '6':
 				case '7':
 				case '8':
 				case '9':
 					key = byteString().toString();
 					break;
 				default:
 					System.out.println("Expected start of byte string, got '" + (char)b + "'");
 					sendState(2, "KEY FOR DICTIONARY NOT A BYTE STRING");
 					System.out.println("Key for dictionary not a byte string.");
 					return null;
 			}
 			
 			//Values
 			value = readNode();
 			if (value == null)
 			{
 				sendState(2, "VALUE FOR DICTIONARY NOT A NODE TYPE");
 				System.out.println("Value for dictionary not a valid choice.");
 				break;
 			}
 			
 			d.addToDict(key, value);
 		}
 		
 		expect('e');
 		
 		return d;
 	}
 	
 	public void disconnect()
 	{
 		running = false;
 	}
 	
 	private DoubleNode doubleValue()
 	{
 		ByteBuffer buff = ByteBuffer.allocate(8);
 		
 		expect('f');
 		
 		for (int i = 0; i < 8; i++) buff.put(nextByte());
 		
 		expect('e');
 		
 		return new DoubleNode(buff.getDouble());
 	}
 	
 	public Socket getSocket()
 	{
 		return socket;
 	}
 	
 	private IntNode intValue()
 	{
 		int value = 0;
 		expect('i');
 		while(peakByte() != 'e') value = value * 10 + (nextByte() - '0');
 		expect('e');
 		return new IntNode(value);
 	}
 	
 	private ListNode list()
 	{
 		expect('l');
 		
 		ListNode list = new ListNode();
 		
 		while(peakByte() != 'e')
 		{
 			Node node = readNode();
 			if (node == null) break;
 			list.addToList(node);
 		}
 		
 		expect('e');
 		
 		return list;
 	}
 	
 	private void receiveMessage()
 	{
 		Node body = null;
 				
 		DictNode header = dictionary();
 				
 		expect(';');
 		
 		// Read in body
 		if(peakByte() != ';') body = dictionary();
 				
 		// Read in last separator
 		expect(';');
 		
 		// Add message
 		try
 		{
 			messageQueue.put(new Message(header, body));
 		}
 		catch(InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void sendMessage(Message message)
 	{
 		try
 		{
 			OutputStream out = socket.getOutputStream();	
 			message.write(out);
 			out.flush();
 		}
 		catch(IOException e)
 		{
 			System.out.println("Error: Could not send message, OutputStream raised a IOException");
 		}
 	}
 	
 	public void sendState(int is, String mess)
 	{
 		DictNode header = new DictNode();
 		header.addToDict("TYPE", new ByteNode("STATUS"));
 		header.addToDict("VERSION", new IntNode(1));
 		
 		DictNode body = new DictNode();
 		body.addToDict("STATE", new IntNode(is));
 		body.addToDict("MSG", new ByteNode(mess));
 		Message msg = new Message(header, body);
 		sendMessage(msg);
 	}
 	
 	public Message waitForMessage()
 	{
 		try
 		{
 			return messageQueue.poll(1000, TimeUnit.MILLISECONDS);
 		}
 		catch(InterruptedException e)
 		{
 			return null;
 		}
 	}
 	
 	private void expect(char c)
 	{
 		expect((byte)c);
 	}
 	
 	private void expect(byte b)
 	{
 		byte g = nextByte(); 
 		if (g != b)
 			System.err.println("Protocol Stream Error: Byte '" + (char)b + "' expected, got '" + (char)g + "'");
 	}
 	
 	private byte nextByte()
 	{
 		byte b = 0;
 		
 		if (currentByte == NO_BYTE)
 		{
 			try
 			{
 				b = (byte)inputStream.read();
 			}
 			catch (IOException e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 		else
 		{
 			b = (byte)currentByte;
 			currentByte = NO_BYTE;
 		}
 		
 		return b;
 	}	
 	
 	private byte peakByte()
 	{
 		if (currentByte == NO_BYTE)
 		{
 			try
 			{
 				currentByte = inputStream.read();
 			}
 			catch (IOException e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 		
 		return (byte)currentByte;
 	}
 	
 	public void run()
 	{
 		running = true;
 
 		while (running) receiveMessage();
 		
 		try
 		{
 			socket.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
