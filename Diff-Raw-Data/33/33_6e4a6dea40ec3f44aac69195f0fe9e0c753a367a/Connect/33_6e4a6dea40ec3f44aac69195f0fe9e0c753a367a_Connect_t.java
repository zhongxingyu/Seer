 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.io.StringWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Vector;
 
 public class Connect extends Thread {
 	private Socket socket = null;
 	private ServerSocket server = null;
 	private ParallelStream oos = null;
 	private ParallelStream ois = null;
 	private static ParallelStream consolidated_oos[] = new ParallelStream[peerProcess.nofPeers];
 	private static int consolidated_index=0;
 
 	public int chunkNumber;
 
 	public boolean complete = false;
 	public boolean haveChunk = false;
 	public static boolean choked = true;
 	int hisRank;
 	public static int[][] listOfPeersandChunks = new int[peerProcess.nofPeers][peerProcess.nofPieces]; 
 	static ArrayList<Integer> haveChunkList = new ArrayList<Integer>();
 	static ArrayList<Integer> dontHaveChunkList = new ArrayList<Integer>();
 	static ArrayList<Integer> unchokedList = new ArrayList<Integer>();
 
 	String fileDirectory=peerProcess.myID+"/";
 	String _host = null;
 	int _port = 0;
 	boolean isServer = false;
 	
 	
 	public Connect() {}
 
 	public Connect(ServerSocket serverSocket) {
 		server = serverSocket;
 		isServer = true;
 		
 		this.start();
 	}
 
 	public Connect(String host, int port){
 		_host = host;
 		_port = port;
 
 		this.start();
 	}
 
 	public int getRank(String peerId) {
 		int rank=0;
 		String st;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
 			int count=1; //to calculate myrank
 			while((st = in.readLine()) != null) {
 
 				String[] tokens = st.split("\\s+");
 				if(peerId.equals(tokens[0])){
 					rank = count; 
 				}
 				count++;
 			}
 			in.close();
 		}
 		catch (Exception ex) {
 			peerProcess.logger.println(ex.toString());
 		}
 		return rank;
 	}
 
 
 	public void run() {
 		
 		try {
 			if(isServer) {
 				socket = server.accept(); 
 				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));
 				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));
 		
 
 			} else {
 				sleep(500);
 				socket = new Socket(_host,_port);
 				oos= new ParallelStream(new ObjectOutputStream(socket.getOutputStream()));
 				ois= new ParallelStream(new ObjectInputStream(socket.getInputStream()));
 				
 
 			}
 
 			consolidated_oos[consolidated_index++]=oos;
 
 
 			//Update its list about itself
 			peerProcess.logger.print("My Rank is " +peerProcess.myRank);
 			peerProcess.logger.print("Nofpeersand pieces " +peerProcess.nofPeers+" "+peerProcess.nofPieces);
 			if(peerProcess.haveFile==1) {
 
 
 				for(int u=0;u<peerProcess.nofPieces;u++) {
 					listOfPeersandChunks[peerProcess.myRank-1][u] = 1;
 				}
 			}
 
 			//Server sending handshake message
 			HandshakeMessage hm = new HandshakeMessage(peerProcess.myID);
 			oos.writeObject(hm);
 
 			//Recving handshake message
 			HandshakeMessage hmRecvd = (HandshakeMessage)ois.readObject();
 			hisRank = getRank(Integer.toString(hmRecvd.peerID));
 			if(isServer){
 				peerProcess.logger.print(peerProcess.myID+" is connected from "+hmRecvd.peerID);
 			} else {
 				peerProcess.logger.print(peerProcess.myID+" makes a connection to "+hmRecvd.peerID);
 			}
 
 			//Sending bitfield message if it has file or any chunk initially
 			if(peerProcess.haveFile==1) {
 				ActualMessage bitfieldMessage = new ActualMessage(peerProcess.nofPieces);
 				//<Set bitfield here>
 				peerProcess.logger.print("nofpieces "+peerProcess.nofPieces);
 				peerProcess.logger.print("Sending bitfield of size "+ bitfieldMessage.messagePayload.length);
 				oos.writeObject(bitfieldMessage);
 			} else {
 
 				peerProcess.logger.println("Bitfield Expected");
 
 			}
 
 			//				peerProcess.logger.print("Going to sent message");
 			//				ActualMessage testChunk = new ActualMessage(makeChunk(16),16);
 			//				oos.writeObject(testChunk);
 
 
 
 			while(!complete) {
 
 				//Recving message
 				ActualMessage messageRcvd = (ActualMessage)ois.readObject();
 				switch(messageRcvd.messageType) {
 				case 0:
 					//choke
 					//set choke flag
 					choked = true;
 					peerProcess.logger.print(peerProcess.myID+" is choked by "+hmRecvd.peerID);
 					break;
 
 				case 1:
 					//recvd unchoke - send a request for any piece you dont have, set choke flag to flase
 					peerProcess.logger.println("Received unchoke message from "+hmRecvd.peerID);
 					choked = false;
 					
 					//selecting randomly a chunk number from the dont have list only if any chunk is missing.
 					ArrayList<Integer> dontHaveChunkList_temp= new ArrayList<Integer>(dontHaveChunkList);
 					if(dontHaveChunkList_temp.size()>0) {
 						Random rand = new Random();
 						while(dontHaveChunkList_temp.size()>0) {
 							int chunkRequestedFor = dontHaveChunkList_temp.get(rand.nextInt(dontHaveChunkList_temp.size()));
 							if(listOfPeersandChunks[hisRank-1][chunkRequestedFor]==1) {
 								oos.writeObject(new ActualMessage("request",chunkRequestedFor));
 								peerProcess.logger.println("Received unchoke and sending request to "+hmRecvd.peerID+" for chunk: "+chunkRequestedFor);
 								break;
 							}
 							
 							peerProcess.logger.println("Attempting to remove element "+chunkRequestedFor+"having index"+dontHaveChunkList_temp.indexOf(chunkRequestedFor));
 //							peerProcess.logger.println("the don't have chunk list is now"+dontHaveChunkList.toString());
 							dontHaveChunkList_temp.remove(dontHaveChunkList_temp.indexOf(chunkRequestedFor));
 							
 						}
 					}
 					peerProcess.logger.print("After copy and remove at top : "+dontHaveChunkList.toString());
 					break;
 
 
 				case 2:
 					//recvd interested
 					peerProcess.logger.println("Received interested message from "+hmRecvd.peerID);
 					//send unchoke after selecting neighbour <ds>
 					if((unchokedList.size()<peerProcess.nofPreferredNeighbour) && !unchokedList.contains(hmRecvd.peerID)) {
 						unchokedList.add(hmRecvd.peerID);	
 						oos.writeObject(new ActualMessage("unchoke"));
 						peerProcess.logger.println("Sending unchoke to "+hmRecvd.peerID);
 					}
 
 					break;
 
 
 				case 3:
 					//recvd not interested
 					peerProcess.logger.println("Received a not interested message from "+hmRecvd.peerID);
 					break;
 
 
 				case 4:
 					//recvd have message
 					//update your list
 					int chunkIndex = messageRcvd.getChunkid();
 					listOfPeersandChunks[hisRank-1][chunkIndex]=1;
 
 					//send interested if you want that piece else not interested
					
					peerProcess.logger.print("DontHaveChunk list after recieving have is "+dontHaveChunkList.toString());
						if(dontHaveChunkList.contains(chunkIndex)) {
 							ActualMessage interested = new ActualMessage("interested");
 							oos.writeObject(interested);
							peerProcess.logger.print("Sent an interested message to"+hmRecvd.peerID+"for chunkid "+chunkIndex);
						} else {
 							ActualMessage notinterested = new ActualMessage("notinterested");
 							oos.writeObject(notinterested);
							peerProcess.logger.print("Sent a NOT interested message to"+hmRecvd.peerID+"for chunkid "+chunkIndex);
 						}
 					break;
 
 
 				case 5:
 					//Recving bitfield message
 					peerProcess.logger.print("Inside bitfield - switch case.--------");
 //					ActualMessage bitfieldMessage = (ActualMessage) ois.readObject();
 //					ActualMessage bitfieldMessage = (ActualMessage) ois.readObject();
 					//Recving bitfield message
 					peerProcess.logger.print(peerProcess.myID + " recieved bitfield message of type "
 							+messageRcvd.messageType +" size "
 					+ messageRcvd.messagePayload.length 
 					+ " from " +hmRecvd.peerID);
 					BitSet myRecvBits = new BitSet(peerProcess.nofPieces);
 					myRecvBits = messageRcvd.toBitSet(messageRcvd.messagePayload);
 
 					String toPrint2 = new String();
 					for(int[] a:listOfPeersandChunks){
 						for(int b:a){
 							toPrint2+=b+",";
 						}
 						toPrint2+="\n";
 					}
 					peerProcess.logger.print("\n Peer and Chunk Info 1: \n"+toPrint2);	
 
 					//Updating the list of the chunks the other peer has
 					if(!myRecvBits.isEmpty())
 					{
 						hisRank = getRank(Integer.toString(hmRecvd.peerID));
 						for(int x=myRecvBits.nextSetBit(0); x>=0; x=myRecvBits.nextSetBit(x+1)) {
 							peerProcess.logger.print(" x "+x);	
 							listOfPeersandChunks[hisRank-1][x] =1;
 						}
 					}
 
 					String toPrint = new String();
 					for(int[] a:listOfPeersandChunks){
 						for(int b:a){
 							toPrint+=b+",";
 						}
 						toPrint+="\n";
 					}
 					peerProcess.logger.print("\n Peer and Chunk Info: \n"+toPrint);				
 
 					//updating your dont have list
 					for(int j=0;j<peerProcess.nofPieces;j++) {
 						if(listOfPeersandChunks[peerProcess.myRank-1][j]==0) {
 							dontHaveChunkList.add(j);
 						}
 
 					}
 					
 					boolean sentInterested = false;
 					//send interested message if he has any piece you dont and only if you need atleast one piece
 					if(dontHaveChunkList.size()>0) {
 						Random rand2 = new Random();
 						//selecting randomly from the dont have list.
 						while(true) {
 							peerProcess.logger.println("Got bitfield, dontHAveChunkList is "+dontHaveChunkList.toString()+
 									" with size"+ dontHaveChunkList.size());
 							if(listOfPeersandChunks[hisRank-1][dontHaveChunkList.get(rand2.nextInt(dontHaveChunkList.size()))]==1) {
 								ActualMessage interested = new ActualMessage("interested");
 								oos.writeObject(interested);
 								sentInterested = true;
 								break;
 							}
 						}
 
 					}
 
 					if(!sentInterested) {
 						ActualMessage interested = new ActualMessage("notinterested");
 						oos.writeObject(interested);
 					}
 					
 					break;
 
 
 				case 6:
 					//recvd request
 					int reqIndex = messageRcvd.getChunkid();
 					peerProcess.logger.print(hmRecvd.peerID+" requested for chunk "+reqIndex);
 					//if unchoked send piece
 					if(unchokedList.contains(hmRecvd.peerID)) {
 						if(peerProcess.haveFile==1) {
 							oos.writeObject(new ActualMessage(makeChunk(reqIndex),reqIndex));
 							peerProcess.logger.print("Making and sending "+hmRecvd.peerID+" the chunk "+reqIndex);
 						}
 						else {
							peerProcess.logger.print("case 6: else The listOfPeersandChunks["+ (peerProcess.myRank-1) +"]["+reqIndex+"]="+listOfPeersandChunks[peerProcess.myRank-1][reqIndex]);
							if(listOfPeersandChunks[peerProcess.myRank-1][reqIndex]==1){
 								File chunkFile = new File(fileDirectory+reqIndex+".part");
 								FileInputStream partInput = new FileInputStream(chunkFile);
								peerProcess.logger.println("Length of chunk "+chunkFile.length()+"in ints "+(int)chunkFile.length() );
 								byte[] chunkb = new byte[(int)chunkFile.length()];
 								peerProcess.logger.println("Length of byte array"+ chunkb.length);
 								partInput.read(chunkb, 0, (int)chunkFile.length());
 								oos.writeObject(new ActualMessage(chunkb,reqIndex));
 								peerProcess.logger.print("Forwarding "+hmRecvd.peerID+" the chunk "+reqIndex);
 							}
 						}
 						
 					}
 					else {
 						peerProcess.logger.print(hmRecvd.peerID+" is choked and does not receive "+reqIndex + " from me.");
 					}
 
 					break;
 
 				case 7:
 
 					// recv piece 
 					chunkNumber = messageRcvd.getChunkid();
 					makePartFile(messageRcvd.getPayload(),chunkNumber);
 
 					peerProcess.logger.print("Got chunk "+chunkNumber);
 					peerProcess.logger.print("Dont have list: "+dontHaveChunkList.toString());
 					//update matrix and dont have list
					if (dontHaveChunkList.indexOf(chunkNumber)>=0){
 					dontHaveChunkList.remove(dontHaveChunkList.indexOf(chunkNumber));
					}
					listOfPeersandChunks[peerProcess.myRank-1][chunkNumber]=1;
					peerProcess.logger.print("case 7 :Updated the listOfPeersandChunks["+ (peerProcess.myRank-1) +"]["+chunkNumber+"]="+listOfPeersandChunks[peerProcess.myRank-1][chunkNumber]);
 					
 
 					//after recieving broadcast have
 					broadcastHave(chunkNumber);
 					haveChunk = true;  //initiate the server sequence even if it has one chunk and only if not already a server
 					
 					//put a logic to see if all pieces are complete
 					
 					//check if unchoked and then request for next piece
 					
 					if(!choked) {
 						//selecting randomly a chunk number from the dont have list only if any chunk is missing.
 						ArrayList<Integer> dontHaveChunkList_temp2= new ArrayList<Integer>(dontHaveChunkList);
 						if(dontHaveChunkList_temp2.size()>0) {
 							Random rand = new Random();
 							while(true) {
 								peerProcess.logger.print("Processing While");
 								int chunkRequestedFor = dontHaveChunkList_temp2.get(rand.nextInt(dontHaveChunkList_temp2.size()));
 								if(listOfPeersandChunks[hisRank-1][chunkRequestedFor]==1) {
 									oos.writeObject(new ActualMessage("request",chunkRequestedFor));
 									peerProcess.logger.println("Requesting again to "+hmRecvd.peerID+" for chunk: "+chunkRequestedFor);
 									break;
 								}
								dontHaveChunkList_temp2.remove(dontHaveChunkList_temp2.indexOf(chunkRequestedFor));
 							}
 						}
 						peerProcess.logger.print("After copy and remove : "+dontHaveChunkList.toString());
 					}
 					
 					
 					break;
 
 				}//end switch case
 
 
 				//					mergeChunks();
 				//					peerProcess.logger.print("Merged chunks");
 
 			}//end while
 
 
 
 
 
 
 		}catch(Exception e) {
 			peerProcess.logger.print(e);
 
 		}       
 	}
 
 	public byte[] makeChunk(int chunkNo) {
 		byte[] outChunks = null;
 		if(peerProcess.haveFile ==1) {
 			try {
 
 				peerProcess.theFile = new File(peerProcess.fileName);
 				assert (!peerProcess.theFile.exists());
 				RandomAccessFile ramFile = new RandomAccessFile(peerProcess.theFile, "r");
 				ramFile.seek((long)peerProcess.pieceSize*chunkNo);
 				peerProcess.logger.print("Chunk number : "+chunkNo+" Now at offset: " +ramFile.getFilePointer());
 				if(peerProcess.pieceSize*(chunkNo+1)>ramFile.length()){
 					outChunks = new byte[(int) (peerProcess.pieceSize*(chunkNo+1)-ramFile.length())];
 					ramFile.read(outChunks, 0, (int) (peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
 					peerProcess.logger.print("in IF :"+chunkNo+" "+(peerProcess.pieceSize*(chunkNo+1)-ramFile.length()));
 				} else {
 					outChunks = new byte[peerProcess.pieceSize];
 					ramFile.read(outChunks, 0, peerProcess.pieceSize);
 				}
 				//			peerProcess.logger.print("\nRead \n"+new String(outChunks)+"\n");
 				ramFile.close();	
 				peerProcess.logger.println("I have the file. SPLIT it into "+outChunks.length+" chunks ");
 
 
 			} catch (Exception e) {
 
 				peerProcess.logger.println(e.toString());
 			}
 
 
 		}
 		peerProcess.logger.println("Chunk size is "+outChunks.length);
 		return outChunks;
 	}
 
 	public void makePartFile(byte[] inputChunk, int chunkNumber) {
 		String partName = chunkNumber + ".part";
 		File outputFile = new File(partName);
 		try {
 			FileOutputStream fos = new FileOutputStream(peerProcess.myID+"/"+outputFile);
 			fos.write(inputChunk);
 			fos.flush();
 			fos.close();
 			fos = null;
 		} catch (Exception e) {
 
 			peerProcess.logger.println(e.toString());
 		}
 
 	}
 
 	public void mergeChunks() {
 		try {
 			File outputFile = new File("output.dat");
 
 			FileOutputStream opfos = new FileOutputStream(outputFile,false);
 			for(int j=0;j<peerProcess.nofPieces;j++) {
 
 				String partNameHere = j + ".part";
 				peerProcess.logger.print("Merging piece :"+partNameHere+" /"+peerProcess.nofPieces);
 
 
 				File partFile = new File(partNameHere);
 				FileInputStream pffis = new FileInputStream(partFile);
 				peerProcess.logger.println("Length of partfile "+partFile.length());
 				byte[] fb = new byte[(int)partFile.length()];
 				peerProcess.logger.println("Length of byte array"+ fb.length);
 				pffis.read(fb, 0, (int)partFile.length());
 				opfos.write(fb);
 				opfos.flush();
 				fb = null;
 				pffis.close();
 				pffis =null;
 			}
 
 			opfos.close();
 		} catch (Exception e) {
 
 			peerProcess.logger.println(e.toString());
 		}
 
 
 	}
 	
 	void broadcastHave(int chunkIndex){
 		for(int i =0; i<consolidated_index-1; i++){
 			consolidated_oos[i].writeObject(new ActualMessage("have",chunkIndex));
 		}
 		peerProcess.logger.print("Broadcasted the have message");
 	}
 
 	private void printConnections(){
 		//		String toPrint = new String();
 		//		for(boolean[] a:isConnected){
 		//			for(boolean b:a){
 		//				toPrint+=b+",";
 		//			}
 		//			toPrint+="\n";
 		//		}
 		//		peerProcess.logger.print("\nisConnected\n"+toPrint);
 	}
 
 }
