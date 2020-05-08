 package edu.uc.cs.distsys.comms;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 public abstract class Message implements Serializable {
	
 	private static final long serialVersionUID = 6302040616367020125L;
 
 	private final int senderId;
	private int sessionId;
 	
 	public Message(int senderId) {
 		this.senderId = senderId;
 	}
 
 	public int getSenderId() {
 		return senderId;
 	}
 	
	public int getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	
 	public byte[] serialize() {
 		byte[] bytes = null;
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutput out = null;
 		try {
 			out = new ObjectOutputStream(bos);
 			out.writeObject(this);
 			bytes = bos.toByteArray();
 		} catch (IOException e) {
 			System.out.println("Error in serialize");
 			e.printStackTrace();
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				bos.close();
 			} catch (IOException e) {
 				System.out.println("Some weird IO error in serialize");
 			}
 		}
 		return bytes;
 	}
 
 	public static Message deserialize(byte[] rawData) {
 		Message msg = null;
 		ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
 		ObjectInput in = null;
 		try {
 			in = new ObjectInputStream(bis);
 			msg = (Message) in.readObject();
 		} catch (IOException e) {
 			System.out.println("Error in deserialize");
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("Unable to find class in deserialize");
 			e.printStackTrace();
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				bis.close();
 			} catch (IOException e) {
 				System.out.println("Some weird IO error in deserialize");
 				e.printStackTrace();
 			}
 		}
 		return msg;
 	}
 }
