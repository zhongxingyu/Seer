 package game.net;
 
 import java.io.*;
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.concurrent.*;
 
 import game.Game;
 import game.states.*;
 
 import common.BufferQueue;
 import common.DataPacket;
 import game.net.packets.*;
 
 public class NetworkingClient extends NetworkMode implements Runnable {
 
 	Socket clientSocket;
 	Thread clientThread;
 	
 	DataInputStream in;
 	DataOutputStream out;
 	
 	public String remoteHost = "localhost";
 	
 	private static final int BUFFER_SIZE = 80000;
 	
 	BufferQueue bq = new BufferQueue(BUFFER_SIZE);
 	BlockingQueue<DataPacket> dataPackets = new LinkedBlockingQueue<DataPacket>();
 	
 	@Override
 	public void modeStart() 
 	{
 		try {
 			this.clientSocket = new Socket(remoteHost, 14121);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(clientSocket.isConnected())
 		{
 			this.clientThread = new Thread(this);
 			this.clientThread.start();
 			System.out.println("Connected!");
 		}	
 	}
 
 	@Override
 	public void destroy()
 	{
 	}
 
 	@Override
 	public void run()
 	{
 		try {
 			this.in = new DataInputStream(this.clientSocket.getInputStream());
 			this.out = new DataOutputStream(this.clientSocket.getOutputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		short packetSize = -1;
 		
 		while(true)
 		{
 			byte data[] = new byte[BUFFER_SIZE];
 			int rx = -1;
 			try {
 				rx = this.in.read(data, 0, BUFFER_SIZE);
 			} catch(EOFException e) {
 				break;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			if(rx < 0) break;
 			
 			byte[] actualData = Arrays.copyOf(data, rx);
 			this.bq.append(actualData);
 			
 			while(true)
 			{
 				if(packetSize < 0 && this.bq.getCount() >= 2)
 				{
 					byte[] lenData = new byte[2];
 					this.bq.read(lenData, 0, 2);
 					packetSize = peekShort(lenData);		
 				}
 				if(packetSize > 0 && this.bq.getCount() >= packetSize)
 				{
 					byte[] pData = new byte[packetSize];
 					this.bq.read(pData, 0, packetSize);
 					DataPacket dp = new DataPacket(pData, false);
 					processPacket(dp);
 					packetSize = -1;
 				} else break;
 			}
 		}
 	}
 	
 	private void processPacket(DataPacket dp) {
 		switch(dp.peekShort())
 		{
 			case WelcomePacket.ID:
 				dp.getShort();
 				int pIndex = dp.getShort();
 				Game.getInstance().setPlayerIndex(pIndex);
 				Game.getInstance().setMaxPlayers(dp.getShort());
 				break;
 				
 			case PingPacket.ID:
 				PingPacket pp = new PingPacket();
 				pp.fromData(dp);
 				if(pp.isReply) return;
 				
 				long offset = pp.time + pp.predictedLatency - System.currentTimeMillis();
 				Game.getInstance().setPredictedLatency(pp.predictedLatency);
 				Game.getInstance().setTimeOffset(offset);
 				
 				pp.isReply = true;
 				this.send(pp.toData());
 				break;
 				
 			case EnterGamePacket.ID:
 				dp.getShort();
 				Game.getInstance().setGameStarting();
 				Game.getInstance().changeState(new LoadingGameState());
 				break;
 				
 			case EnterPrePostPacket.ID:
 				EnterPrePostPacket epp = new EnterPrePostPacket();
 				epp.fromData(dp);
 				if(epp.isPre())
 					Game.getInstance().changeState(new PregameState());
 				
 				break;
 			
 			case MovementPacket.ID:
 				
 				MovementPacket mp = new MovementPacket();
 				mp.fromData(dp);
 				
 				Game.getInstance().movePlayer(mp.playerIndex, mp.position, mp.velocity, mp.orientation);
 				break;
 				
 			case SetReadyPacket.ID:
 				SetReadyPacket srp = new SetReadyPacket();
 				srp.fromData(dp);
 				Game.getInstance().setPregameReady(srp.pIndex, srp.newReadyStatus);
 				break;
 				
 			case NotifyPlayerJoinedPacket.ID:
 				NotifyPlayerJoinedPacket np = new NotifyPlayerJoinedPacket();
 				np.fromData(dp);
 				System.out.println("here, also!");
 				Game.getInstance().setLobbyCurrentPlayers(np.nPlayers);
 				break;
 			
 			case ProjectileLifePacket.ID:
 				ProjectileLifePacket plp = new ProjectileLifePacket();
 				plp.fromData(dp);
 				Game.getInstance().selfCreateProjectile(plp.eid, plp.pos, plp.vel, plp.ori, plp.creator, plp.createTime);
 				break;
 				
 			case ChangeAttributePacket.ID:
 				ChangeAttributePacket cap = new ChangeAttributePacket();
 				cap.fromData(dp);
 				if(cap.isHealth())
 					Game.getInstance().selfSetEntityHealth(cap.eid, cap.newVal);
 				break;
 				
 			case ItemLifePacket.ID:
 				ItemLifePacket ilp = new ItemLifePacket();
 				ilp.fromData(dp);
 				System.out.println("Recieved create item packet");
				if(ilp.isCreate())
 					Game.getInstance().selfSpawnItem(ilp.getItem());
 				break;
 		}
 	}
 
 	private short peekShort(byte[] data)
 	{
 		ByteBuffer bb = ByteBuffer.allocate(2);
 		bb.put(data[0]);
 		bb.put(data[1]);
 		return bb.getShort(0);
 	}
 
 	public void send(DataPacket data) {
 		try {
 			this.out.write(data.getData());
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 	}
 
 	public void setRemoteTarget(String text) {
 		this.remoteHost = text;
 	}
 }
