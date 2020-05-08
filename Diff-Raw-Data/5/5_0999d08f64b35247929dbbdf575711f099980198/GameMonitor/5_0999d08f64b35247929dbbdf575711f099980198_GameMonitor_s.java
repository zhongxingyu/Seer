 package GameServer;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayDeque;
 import java.util.HashMap;
 
 import utils.misc;
 
 
 public class GameMonitor {
 	
 	private ArrayDeque<Byte>[] incComs;
 	private HashMap<Long, Byte[]> outComs;
 	private int players;
 	private HashMap<GameServerOutputThread, Boolean> register;
 	
 	public GameMonitor(int nbrOfPlayers){
 		incComs = new ArrayDeque[nbrOfPlayers];
 		outComs = new HashMap<Long, Byte[]>();
 		
 		for(int i = 0; i < nbrOfPlayers; ++i){
 			incComs[i] = new ArrayDeque<Byte>();
 		}
 		this.players = nbrOfPlayers;
 		register = new HashMap<GameServerOutputThread, Boolean>();
 	}
 	
 	public void registerOThread(GameServerOutputThread gsot){
 		register.put(gsot, false);
 	}
 	
 	public void deRegisterOThread(GameServerOutputThread gsot){
 		register.remove(gsot);
 	}
 	
 	public int getNbrPlayers(){
 		return players;
 	}
 	
 	public synchronized void addIncomingCommand(byte b, int player){
 //		System.out.println("Adding incoming command: " + (int)b + " for player " + player);
 		incComs[player].add(b);
 	}
 	
 	public synchronized byte getIncomingCommand(int player){
 		Byte b = incComs[player].pollFirst();
 		if(b == null)
 			b = new Byte((byte) 0x00);
 		
 //		System.out.println("Reading incoming command: " + b + " from player " + player);
 		return b;
 	}
 	
 	public synchronized void setOutgoingCommands(Byte[] b, long frame){
 		outComs.put(frame, b);
 	//	System.out.println("In Game Monitor setting outgoing commands: " + misc.printByte(b));
 		notifyAll();
 	}
 	
 	public synchronized byte[] getOutGoingCommand(long frame, GameServerOutputThread gsot){
 		while(outComs.size() == 0 || register.get(gsot)){
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		byte[] coms = new byte[1*players + 8];
 		byte[] frameBytes = ByteBuffer.allocate(8).putLong(frame).array();
 		
 		for(int i = 0; i < 8; ++i){
 			coms[i] = frameBytes[i];
 		}
 		Byte[] b = null;
 		
 		b = outComs.get(frame);
 		register.put(gsot, true);
			b = outComs.remove(frame);
 			setAllReg(false);
 		if(b == null){
 			b = new Byte[players];
 			for(int i = 0; i < players; i++){
 				b[i] = 0;
 			}
 		}
 		if(b[0] != 0){
 			System.out.println("!0");
 		}
 		for(int i = 0; i < players; ++i){
 			coms[8+i] = b[i];
 		}
 		
 		return coms;
 	}
 	
 	private void setAllReg(boolean value) {
 		for(GameServerOutputThread gs : register.keySet()){
 			register.put(gs, value);
 		}
 		notifyAll();
 	}
 
 	private boolean allRegTrue(){
 		for(GameServerOutputThread gs : register.keySet()){
 			if(!register.get(gs)){
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
