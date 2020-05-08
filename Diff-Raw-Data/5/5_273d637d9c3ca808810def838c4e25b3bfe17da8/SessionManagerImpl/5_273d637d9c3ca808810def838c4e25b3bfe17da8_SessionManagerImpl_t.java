 package com.google.gwt.dist.compiler.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import com.google.gwt.dev.CompilePerms.CompilePermsOptions;
 import com.google.gwt.dist.Node;
 import com.google.gwt.dist.ProcessingState;
 import com.google.gwt.dist.SessionManager;
 import com.google.gwt.dist.comm.CommMessage;
 import com.google.gwt.dist.comm.CommMessageResponse;
 import com.google.gwt.dist.comm.ProcessingStateResponse;
 import com.google.gwt.dist.comm.SendDataPayload;
 import com.google.gwt.dist.comm.CommMessage.CommMessageType;
 import com.google.gwt.dist.compiler.communicator.Communicator;
 import com.google.gwt.dist.impl.ProcessingStateMessage;
 import com.google.gwt.dist.impl.SendDataMessage;
 import com.google.gwt.dist.util.ZipCompressor;
 import com.google.gwt.dist.util.ZipDecompressor;
 
 /**
  * Concrete SessionManager implementation.
  */
 public class SessionManagerImpl implements SessionManager {
 
 	private CompilePermsOptions compilePermsOptions;
 	private ZipCompressor compressor;
 	private ZipDecompressor decompressor;
 	private Communicator communicator;
 	private Node node;
 
 	public SessionManagerImpl(Communicator communicator, Node node,
 			CompilePermsOptions options, ZipCompressor compressor,
 			ZipDecompressor decompressor) {
 		this.compilePermsOptions = options;
 		this.compressor = compressor;
 		this.decompressor = decompressor;
 		this.communicator = communicator;
 		this.node = node;
 	}
 
 	@Override
 	public boolean compilePermsCompleted(Node node) {
 		return false;
 	}
 
 	@Override
 	public boolean readyToReceiveData(Node node) {
 		return false;
 	}
 
 	@Override
 	public void run() {
 	}
 
 	@Override
 	public void sendDataToAgent(byte[] data) {
 		communicator.sendData(data, node);
 	}
 
 	public <T extends CommMessageResponse> T sendMessageToAgent(
 			CommMessage<T> message) {
 		T response = communicator.sendMessage(message, this.node);
 		return response;
 
 	}
 
 	@Override
 	public Communicator getCommunicator() {
 		return this.communicator;
 	}
 
 	@Override
 	public void setCommunicator(Communicator communicator) {
 		this.communicator = communicator;
 	}
 
 	public boolean start() {
 		ProcessingStateResponse response = sendMessageToAgent(new ProcessingStateMessage(
 				CommMessageType.QUERY));
 		ProcessingState currentState = null;
 		if (response != null) {
 			currentState = ((ProcessingStateResponse) response)
 					.getCurrentState();
 			if (currentState != null) {
 				switch (currentState) {
 				case READY:
 					SendDataMessage message = new SendDataMessage();
 					SendDataPayload payload = new SendDataPayload();
 					payload.setPayload(generateDataForProcessing());
 					payload.setCompilePermsOptions(compilePermsOptions);
 					message.setResponse(payload);
 					communicator.sendMessage(message, this.node);
 					return false;
 				case INPROGRESS:
 					System.out.println("Agent " + this.node.getIpaddress()
 							+ " is in progress.");
 					return false;
 				case COMPLETED:
 					try {
 						byte[] retrievedData = communicator
 								.retrieveData(this.node);
 						File temp = new File("work");
 						decompressor.decompressAndStoreToFile(retrievedData,
 								temp);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					return true;
 				default:
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Generates data to be processed by the agent.
 	 * 
 	 * @return
 	 */
 	private byte[] generateDataForProcessing() {
 		File source = new File(System.getProperty("user.dir"));
 
 		ByteArrayOutputStream srcFolder = compressor.archiveAndCompressDir(
				new File(source + File.separator + "src"), true);
 
 		ByteArrayOutputStream workFolder = compressor.archiveAndCompressDir(
				new File(source + File.separator + "work"), true);
 
 		ByteArrayInputStream bais1 = new ByteArrayInputStream(srcFolder
 				.toByteArray());
 		CheckedInputStream checksum1 = new CheckedInputStream(bais1,
 				new Adler32());
 		ZipInputStream zis1 = new ZipInputStream(checksum1);
 
 		ByteArrayInputStream bais2 = new ByteArrayInputStream(workFolder
 				.toByteArray());
 		CheckedInputStream checksum2 = new CheckedInputStream(bais2,
 				new Adler32());
 		ZipInputStream zis2 = new ZipInputStream(checksum2);
 
 		ZipInputStream mergedStream = compressor.mergeZippedStreams(zis1, zis2);
 
 		ByteArrayOutputStream dataAsByteArrayOutputStream = new ByteArrayOutputStream();
 		ZipOutputStream compressedResultStream = new ZipOutputStream(
 				dataAsByteArrayOutputStream);
 
 		try {
 			ZipEntry ze = null;
 			while ((ze = mergedStream.getNextEntry()) != null) {
 				compressedResultStream.putNextEntry(ze);
 				byte[] buff = new byte[2048];
 				int bytesRead = 0;
 				while ((bytesRead = mergedStream.read(buff)) > -1) {
 					compressedResultStream.write(buff, 0, bytesRead);
 				}
 				compressedResultStream.flush();
 				compressedResultStream.closeEntry();
 				mergedStream.closeEntry();
 			}
 			compressedResultStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			zis1.close();
 			zis2.close();
 			bais1.close();
 			bais2.close();
 			workFolder.close();
 			srcFolder.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return dataAsByteArrayOutputStream.toByteArray();
 	}
 }
