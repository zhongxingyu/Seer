 /*
  *  UniCrypt Cryptographic Library
  *  Copyright (c) 2013 Berner Fachhochschule, Biel, Switzerland.
  *  All rights reserved.
  *
  *  Distributable under GPL license.
  *  See terms of license at gnu.org.
  *  
  */
 
 package ch.bfh.instacircle.service;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.SecretKeySpec;
 
 import ch.bfh.instacircle.Message;
 
 /**
  * This class implements a Thread which is waiting for incoming TCP messages and
  * dispatches them to the NetworkService to process them.
  * 
  * @author Juerg Ritter (rittj1@bfh.ch)
  */
 public class TCPUnicastReceiverThread extends Thread {
 
 	private static final String TAG = TCPUnicastReceiverThread.class
 			.getSimpleName();
 	public ServerSocket serverSocket;
 
 	NetworkService service;
 
 	private String cipherKey;
 
 	/**
 	 * @param service
 	 *            the service to which the message is being dispatched after
 	 *            receiving it
 	 * @param cipherKey
 	 *            the cipher key which will be used for decrypting the messages
 	 */
 	public TCPUnicastReceiverThread(NetworkService service, String cipherKey) {
 		this.setName(TAG);
 		this.service = service;
 		this.cipherKey = cipherKey;
 	}
 
 	public void run() {
 		Socket clientSocket;
 		Message msg;
 		InputStream in = null;
 		try {
 			serverSocket = new ServerSocket(12345);
 			while (!Thread.currentThread().isInterrupted()) {
 
 				try {
 					clientSocket = serverSocket.accept();
 					in = clientSocket.getInputStream();
 					DataInputStream dis = new DataInputStream(in);
 
 					// Reading the first 4 bytes which represent a 32 Bit
 					// integer and indicates the length of the encrypted payload
 					byte[] lenght = new byte[4];
 					dis.read(lenght);
 
 					// Initialize and read an array with the previously
 					// determined length
 					byte[] encryptedData = new byte[ByteBuffer.wrap(lenght)
 							.getInt()];
 					dis.readFully(encryptedData);
 
 					byte[] data = decrypt(cipherKey.getBytes(), encryptedData);
 
 					// let's try to deserialize the payload only if the
 					// decryption process has been successful
 					if (data != null) {
 
 						// deserializing the payload into a Message object
 						ByteArrayInputStream bis = new ByteArrayInputStream(
 								data);
 						ObjectInput oin = null;
 						try {
 							oin = new ObjectInputStream(bis);
 							msg = (Message) oin.readObject();
 
 						} finally {
 							bis.close();
 							oin.close();
 						}
 
 						// Dispatch it to the service after adding the sender IP
 						// address to the message
 						if (!Thread.currentThread().isInterrupted()) {
 							msg.setSenderIPAddress((clientSocket
 									.getInetAddress()).getHostAddress());
 							service.processUnicastMessage(msg);
 						}
 
 					}
 				} catch (IOException e) {
 					serverSocket.close();
 					Thread.currentThread().interrupt();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			try {
				if (in != null) {
					in.close();
				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Method which encrypts data using a key
 	 * 
 	 * @param rawSeed
 	 *            The symetric key as byte array
 	 * @param encrypted
 	 *            The data to be decrypted
 	 * @return A byte array of the decrypted data if decryption was successful,
 	 *         null otherwise
 	 */
 	private byte[] decrypt(byte[] rawSeed, byte[] encrypted) {
 		Cipher cipher;
 		MessageDigest digest;
 		byte[] decrypted = null;
 		try {
 			// we need a 256 bit key, let's use a SHA-256 hash of the rawSeed
 			// for that
 			digest = MessageDigest.getInstance("SHA-256");
 			digest.reset();
 			SecretKeySpec skeySpec = new SecretKeySpec(digest.digest(rawSeed),
 					"AES");
 			cipher = Cipher.getInstance("AES");
 			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
 			decrypted = cipher.doFinal(encrypted);
 		} catch (NoSuchAlgorithmException e) {
 			return null;
 		} catch (NoSuchPaddingException e) {
 			return null;
 		} catch (InvalidKeyException e) {
 			return null;
 		} catch (IllegalBlockSizeException e) {
 			return null;
 		} catch (BadPaddingException e) {
 			return null;
 		}
 		return decrypted;
 	}
 }
