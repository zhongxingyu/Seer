 package services;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 
 import javax.swing.Timer;
 
 import datatypes.Datagram;
 
 public class TTPConnEndPoint {
 	private DatagramService ds;
 	private Datagram datagram;
 	private Datagram recdDatagram;
 	private int base;
 	private int nextSeqNum;
 	private int N;
 	private int acknNum;
 	private int expectedSeqNum;
 	private int time;
 	private Timer clock;
 	private HashMap<Integer,Datagram> unacknowledgedPackets;
 
 	public static final int SYN = 0;
 	public static final int ACK = 1;
 	public static final int FIN = 2;
 	public static final int DATA = 3;
 	public static final int EOFDATA = 4;
 	public static final int SYNACK = 5;
 	public static final int FINACK = 6;
 	public static final int FINACKACK = 7;
 
 	public TTPConnEndPoint() {
 		datagram = new Datagram();
 		recdDatagram = new Datagram();
 		unacknowledgedPackets = new HashMap<Integer,Datagram>();
 
 		System.out.println("Enter Send Window Size");
 		Scanner read = new Scanner(System.in);
 		N = read.nextInt();
 
 		System.out.println("Enter Retransmission Timer Interval in milliseconds");		
 		time = read.nextInt();
 
 		clock = new Timer(time,listener);
 		clock.setInitialDelay(time);
 
 		Random rand = new Random();
 		nextSeqNum = rand.nextInt(65536);
 	}
 	
 	public TTPConnEndPoint(DatagramService ds) {
 		this();
 		this.ds = ds;
 	}
 
 	public void open(String src, String dest, short srcPort, short destPort, int verbose) throws IOException, ClassNotFoundException {
 		
 		this.ds = new DatagramService(srcPort, verbose);
 		
 		datagram.setSrcaddr(src);
 		datagram.setDstaddr(dest);
 		datagram.setSrcport((short) srcPort);
 		datagram.setDstport((short) destPort);
 		datagram.setSize((short) 9);
 		datagram.setData(createPayloadHeader(TTPConnEndPoint.SYN));
 		datagram.setChecksum(calculateChecksum(datagram));
 		this.ds.sendDatagram(datagram);
 		System.out.println("SYN sent to " + datagram.getDstaddr() + ":" + datagram.getDstport() + " with ISN " + nextSeqNum);
 
 		base = nextSeqNum;
 		clock.start();
 
 		unacknowledgedPackets.put(nextSeqNum, datagram);
 		nextSeqNum++;
 
 		receiveData();
 	}
 
 	private byte[] createPayloadHeader(int flags) {
 		byte[] header = new byte[9];
 		byte[] isnBytes = ByteBuffer.allocate(4).putInt(nextSeqNum).array();
 		byte[] ackBytes = ByteBuffer.allocate(4).putInt(acknNum).array();
 
 		switch (flags) {
 		case SYN:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = (byte) 0;
 			}
 			header[8] = (byte) 4;
 			break;
 
 		case ACK:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = ackBytes[i - 4];
 			}
 			header[8] = (byte) 2;
 			break;
 
 		case FIN:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = (byte) 0;
 			}
 			header[8] = (byte) 1;
 			break;
 
 		case DATA:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = (byte) 0;
 			}
 			header[8] = (byte) 0;
 			break;
 
 		case EOFDATA:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = (byte) 0;
 			}
 			header[8] = (byte) 8;
 			break;
 			
 		case SYNACK:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = ackBytes[i - 4];
 			}
 			header[8] = (byte) 6;
 			break;
 			
 		case FINACK:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = ackBytes[i - 4];
 			}
 			header[8] = (byte) 3;
 			break;
 		
 		case FINACKACK:
 			for (int i = 0; i < 4; i++) {
 				header[i] = isnBytes[i];
 			}
 			for (int i = 4; i < 8; i++) {
 				header[i] = ackBytes[i - 4];
 			}
 			header[8] = (byte) 16;
 			break;
 		}
 		return header;
 	}
 
 	public short calculateChecksum(Datagram datagram) throws IOException {
 		Checksum checksum = new CRC32();
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream(1500);
 		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
 		oStream.writeObject(datagram);
 		byte[] data = bStream.toByteArray();
 		checksum.update(data, 0, data.length);
 		return (short) checksum.getValue();
 	}
 
 	public void sendData(byte[] data) throws IOException {
 
 		if (nextSeqNum < base + N) {
 
 			int lengthOfData = data.length;
 			byte[] fragment = null;
 			int dataCounter = 0;
 			int currentCounter;
 			int indexController = 0;
 
 			if (lengthOfData > 1281) {
 
 				do {
 					currentCounter = dataCounter;
 					indexController = Math.min(lengthOfData , 1281);
 					fragment = new byte[indexController];
 					
 					for (int i = currentCounter; i < currentCounter + indexController; dataCounter++, i++) {
 						fragment[i % 1281] = data[i];
 					}
 
 					if (lengthOfData > 1281)
 						encapsulateAndSendFragment(fragment, false);
 					else
 						encapsulateAndSendFragment(fragment, true);
 					
 					lengthOfData -= 1281;
 					
 				} while (lengthOfData > 0);
 			} else {
 				fragment = data.clone();
 				encapsulateAndSendFragment(fragment, true);
 			}
 		} else {
 			refuse_data(data);
 		}
 	}
 
 	private void encapsulateAndSendFragment (byte[] fragment, boolean lastFragment) throws IOException {
 
 		byte[] header = new byte[9];
 		if (lastFragment) {
 			header = createPayloadHeader(TTPConnEndPoint.EOFDATA);
 		} else {
 			header = createPayloadHeader(TTPConnEndPoint.DATA);
 		}
 
 		byte[] headerPlusData = new byte[fragment.length + header.length];
 		System.arraycopy(header, 0, headerPlusData, 0, header.length);
 		System.arraycopy(fragment, 0, headerPlusData, header.length, fragment.length);
 
 		datagram.setData(headerPlusData);
 		datagram.setSize((short)headerPlusData.length);
 		datagram.setChecksum(calculateChecksum(datagram));
 		ds.sendDatagram(datagram);
 		System.out.println("Data sent to " + datagram.getDstaddr() + ":" + datagram.getDstport() + " with Seq no " + nextSeqNum);
 
 		if (base == nextSeqNum) {
 			clock.restart();
 		}
 
 		unacknowledgedPackets.put(nextSeqNum, datagram);
 		nextSeqNum++;
 	}
 
 	private void refuse_data(byte[] data) {
 		System.out.println("Send Window full! Please try again later!");
 	}
 
 	public byte[] receiveData() throws IOException, ClassNotFoundException {
 		recdDatagram = ds.receiveDatagram(); 
 
 		byte[] data = (byte[]) recdDatagram.getData();
 		byte[] app_data = null;
 
 		if (recdDatagram.getSize() > 9) {
 			if(byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]}) == expectedSeqNum) {
 				acknNum = byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]});
 				System.out.println("Received data with Seq no " + acknNum);
 				if(data[8]==8) {
 					app_data = new byte[data.length - 9];
 					for (int i=0; i < app_data.length; i++) {
 						app_data[i] = data[i+9];
 					}
 					sendAcknowledgement();
 					expectedSeqNum++;
 				}
 				else if(data[8]== 0) {
 					expectedSeqNum++;
 					ArrayList<Byte> dataList = reassemble(data);
 					app_data = new byte[dataList.size()];
 					for (int i=0;i<dataList.size();i++) {
 						app_data[i] = (byte)dataList.get(i);
 					}
 				}
 			}
 			else {
 				sendAcknowledgement();
 			}
 
 
 		} else {
 			if (data[8] == (byte)6) {				
 				acknNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]});
 				expectedSeqNum =  acknNum + 1;
 				base = byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}) + 1;
 				clock.stop();
 				System.out.println("Received SYNACK with seq no:" + acknNum + " and Acknowledgement No " + (base-1));
 				sendAcknowledgement();
 			}
 			if(data[8]== (byte)2) {
 				base = byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}) + 1;
 				System.out.println("Received ACK for packet no:" + byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}));
 			}
 			if(data[8]== (byte)3) {
 				acknNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]});
 				expectedSeqNum =  acknNum + 1;
 				base = byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}) + 1;
 				clock.stop();
 				System.out.println("Received FINACK with seq no:" + acknNum );
 				sendFinackAcknowledgement();
 			}
 			if(base == nextSeqNum) {
 				clock.stop();
 			} else {
 				clock.restart();
 			}
 		}
 		return app_data;
 	}
 
 	private void sendFinackAcknowledgement() throws IOException {
 		datagram.setData(createPayloadHeader(FINACKACK));
 		datagram.setSize((short)9);
 		ds.sendDatagram(datagram);
 		System.out.println("Acknowledgement sent for FINACK! No:" + acknNum);
 		
 		clock.removeActionListener(listener);
 		clock.addActionListener(deleteClient);
 		clock.restart();
 	}
 	
 	ActionListener deleteClient = new ActionListener(){
 		public void actionPerformed(ActionEvent event){
 			System.out.println("Deleting the client by setting ds to null");
 			ds = null;
 		}
 	};
 
 	private ArrayList<Byte> reassemble(byte[] data2) throws IOException, ClassNotFoundException {
 		ArrayList<Byte> reassembledData = new ArrayList<Byte>();
 
 		for(int i=9;i < data2.length;i++) {
 			reassembledData.add(data2[i]);
 		}
 
 		while(true) {
 			recdDatagram = ds.receiveDatagram(); 
 			byte[] data = (byte[]) recdDatagram.getData();
 
 			if(byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]}) == expectedSeqNum) {
 				acknNum = byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]});
 
 				for(int i=9;i < data.length;i++) {
 					reassembledData.add(data[i]);
 				}
 
 				sendAcknowledgement();
 				nextSeqNum++;
 				expectedSeqNum++;
 				
 				if(data[8]==0) {
 					continue;
 				}
 				else if(data[8]==8) {
 					break;
 				}
 			}
 			else {
 				sendAcknowledgement();
 			}
 		}
 		return reassembledData;
 	}
 
 	public void sendAcknowledgement() throws IOException {
 		datagram.setData(createPayloadHeader(ACK));
 		datagram.setChecksum(calculateChecksum(datagram));
 		ds.sendDatagram(datagram);
 		System.out.println("Acknowledgement sent! No:" + acknNum);
 	}
 
 	public static int byteArrayToInt(byte[] b) {
 		int value = 0;
 		for (int i = 0; i < 4; i++) {
 			int shift = (4 - 1 - i) * 8;
 			value += (b[i] & 0x000000FF) << shift;
 		}
 		return value;
 	}
 
 	public static byte[] intToByteArray(int a) {
 		byte[] ret = new byte[4];
 		ret[0] = (byte) (a & 0xFF);
 		ret[1] = (byte) ((a >> 8) & 0xFF);
 		ret[2] = (byte) ((a >> 16) & 0xFF);
 		ret[3] = (byte) ((a >> 24) & 0xFF);
 		return ret;
 	}
 
 	ActionListener listener = new ActionListener(){
 		public void actionPerformed(ActionEvent event){
 			System.out.println("Timeout for Packet " + base);
 			for (Datagram d: unacknowledgedPackets.values()) {
 				try {
 					ds.sendDatagram(d);
 					System.out.println("Datagram resent!!");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			clock.restart();
 		}
 	};
 
 	public void respond(Datagram request, TTPServer parent) throws IOException, ClassNotFoundException {
 
 		byte[] data = (byte[]) request.getData();
 		byte[] app_data = null;
 		byte[] clientInfo = new byte[6];
 				
 		if (request.getSize() > 9) {
 			if(byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]}) == expectedSeqNum) {
 				acknNum = byteArrayToInt(new byte[] { data[0], data[1], data[2], data[3]});
 				
 				String[] temp = datagram.getDstaddr().split("\\.");				
 				for (int i=0;i<4;i++) {
 					clientInfo[i] = (byte) (Integer.parseInt(temp[i]));
 				}
 				clientInfo[4] = (byte)(datagram.getDstport() & 0xFF);
 				clientInfo[5] = (byte) ((datagram.getDstport() >> 8) & 0xFF);
 					
 				if(data[8]==8) {
 					System.out.println("Received data from " + datagram.getDstaddr() + ":" + datagram.getDstport());
 					app_data = new byte[data.length - 9];
 					for (int i=0; i < app_data.length; i++) {
 						app_data[i] = data[i+9];
 					}
 					sendAcknowledgement();
 					expectedSeqNum++;
 				}
 				else if(data[8]== 0) {
 					ArrayList<Byte> dataList = reassemble(data);
 					app_data = new byte[dataList.size()];
 					System.arraycopy(dataList, 0, app_data, 0, app_data.length);
 				}
 			}
 			else {
 				sendAcknowledgement();
 			}
 		} else {
 			if (data[8]==(byte)4) {
 				acknNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]});
 				
 				datagram.setSrcaddr(request.getDstaddr());
 				datagram.setDstaddr(request.getSrcaddr());
 				datagram.setSrcport((short) request.getDstport());
 				datagram.setDstport((short) request.getSrcport());
 				datagram.setSize((short) 9);
 				datagram.setData(createPayloadHeader(TTPConnEndPoint.SYNACK));
 				datagram.setChecksum(calculateChecksum(datagram));
 				this.ds.sendDatagram(datagram);
 				System.out.println("SYNACK sent to " + datagram.getDstaddr() + ":" + datagram.getDstport());
 				
 				expectedSeqNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]}) + 1;
 						
 				base = nextSeqNum;
 				clock.start();
 
 				unacknowledgedPackets.put(nextSeqNum, datagram);
 				nextSeqNum++;
 
 			}
 			if (data[8] == (byte)6) {				
 				acknNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]});
 				expectedSeqNum =  acknNum + 1;
 				base = byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}) + 1;
 				clock.stop();
 				System.out.println("Received SYNACK with seq no:" + acknNum);
 				sendAcknowledgement();
 			}
 			if(data[8]== (byte)2) {
 				base = byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}) + 1;
 				System.out.println("Received ACK for packet no:" + byteArrayToInt(new byte[]{ data[4], data[5], data[6], data[7]}));
 			}
 			if(data[8] == (byte)1){
 				unacknowledgedPackets.clear();
 				acknNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]});
 				datagram.setSize((short) 9);
 				datagram.setData(createPayloadHeader(FINACK));
 				ds.sendDatagram(datagram);
 				
 				System.out.println("FINACK sent to " + datagram.getDstaddr() + ":" + datagram.getDstport());
 				expectedSeqNum = byteArrayToInt(new byte[]{ data[0], data[1], data[2], data[3]}) + 1;
 						
 				clock.restart();
 				unacknowledgedPackets.put(nextSeqNum, datagram);
 				nextSeqNum++;
 			}
 			if(base == nextSeqNum) {
 				clock.stop();
 			} else {
 				clock.restart();
 			}
 		}
 		if (app_data != null) {
 			byte[] totalData = new byte[clientInfo.length + app_data.length];
 			System.arraycopy(clientInfo, 0, totalData, 0, clientInfo.length);
 			System.arraycopy(app_data, 0, totalData, clientInfo.length, app_data.length);
 			parent.addData(totalData);
 			System.out.println("Data written to TTPServer buffer");
 		}
 	}
 
 	public void close() throws IOException, ClassNotFoundException {
 		datagram.setData(createPayloadHeader(FIN));
 		datagram.setSize((short)9);
 		ds.sendDatagram(datagram);
 		System.out.println("FIN sent! Seq No:" + nextSeqNum);
 		
 		unacknowledgedPackets.put(nextSeqNum, datagram);
 		nextSeqNum++;
 		
		clock.restart();
 		ds.receiveDatagram();
 	}
 	
 }
