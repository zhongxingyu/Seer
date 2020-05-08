 package com.olivelabs.loadbalancer.implementation;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.olivelabs.data.IMetric;
 import com.olivelabs.loadbalancer.IClient;
import com.olivelabs.util.ByteArrayConvertor;
 
 public class Client implements IClient {
 
 	private final InetAddress server;
 	private final int port;
 	private final int byteLength = 28192;
 	private Socket socket;
 	private IMetric metric;
 	
 	
 	public Client(InetAddress server, int port){
 		this.server = server;
 		this.port = port;
 		
 	}
 	
 	
 
 	@Override
 	public void handleRequest(Socket incoming) throws Exception {
 		byte[] data = read(incoming);
 		metric.setRequestServedSizeInMB(calculateSizeInMB(data.length));
 		byte[] response = send(data);
 		metric.setRequestServedSizeInMB(calculateSizeInMB(response.length));
 		boolean result = write(incoming,response);
 		metric.setNumberOfRequestServed(Long.valueOf(1));
 		socket.close();
 		if(!result) throw new RuntimeException("Error Occured when sending the request from the node's client!");
 	}
 
 	private Double calculateSizeInMB(int size){
 		double value = (size * 1.0) / (1024*1024);
 		return Double.valueOf(value);
 	}
 
 	private byte[] send(byte[] data)
 			throws Exception {
 		try {
 			if(socket == null || socket.isClosed()){
 				socket = new Socket(server, port); 
 				socket.setKeepAlive(true);
 				System.out.println("Connection established....");
 			}
 			OutputStream out = socket.getOutputStream();
 			out.write(data);
 			out.flush();
 			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
 			byte[] bufferOut = new byte[byteLength];
 			ArrayList buffers = new ArrayList();
 			int read = 0;
 			while((read = in.read(data)) != -1){
 				int status = in.read(bufferOut);
 				byte[] bufferCopy = new byte[byteLength];
 				System.arraycopy(bufferOut, 0, bufferCopy, 0, byteLength);
 				buffers.add(bufferCopy);
 				if(read < byteLength){
 					//break;
 				}
 				
 			}
 			
 			
 			int completeLenght = buffers.size()*byteLength;
 			byte[] finalBytes = new byte[completeLenght];
 			int currStart=0;
 			int count=1;
 			for(Object o : buffers){
 				byte[] buff = (byte[]) o;
 				System.arraycopy(buff, 0, finalBytes, currStart, byteLength);
 				currStart = byteLength * count++;
 			}
 			return finalBytes;
 			//System.out.println("Node: Received Response:"+ByteArrayConvertor.convertToString(finalBytes));
 		} catch (Exception ioe) { 
     		throw new RuntimeException("Client failure: "+ioe.getMessage());
     	} finally {
     		try {
     			socket.close();
     		} catch (Exception e) {
     			// do nothing - server failed
     		}
     	}
 		
 	}
 	
 	@Override
 	public void setMetrics(IMetric metric) {
 		this.metric = metric;
 		
 	}
 	
 	private byte[] read(Socket incoming){
 		ArrayList buffers = new ArrayList();
 		int length = 8192;
 		try {
 			BufferedInputStream in = new BufferedInputStream(incoming.getInputStream());
 			byte[] data = new byte[length];
 			int read = 0;
 			while((read = in.read(data)) != -1){
 				System.out.println("READ DATA: "+read);
 				buffers.add(data);
 				data = new byte[length];
 				if(read < length){
 					break;
 				}
 				
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int completeLenght = buffers.size()*length;
 		byte[] finalBytes = new byte[completeLenght]; 
 			int currStart=0;
 		int count=1;
 		for(Object o : buffers){
 			byte[] buff = (byte[]) o;
 			System.arraycopy(buff, 0, finalBytes, currStart, length);
 			currStart = length * count++;
 		}
 		return finalBytes;
 	}
 	
 	private boolean write(Socket incoming, byte[] data){
 		try {
 			BufferedOutputStream out = new BufferedOutputStream(incoming.getOutputStream());
 			out.write(data);
 			out.flush();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 }
