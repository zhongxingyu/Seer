 package client;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import brute.Bonus;
 import brute.Brute;
 import network.Protocol;
 import network.Reader;
 import network.Writer;
 
 public class SessionClient {
 	
 	protected Socket socket;
 	
 	public SessionClient(Socket socket) throws IOException {
 		this.socket = socket;
 	}
 	
 	private boolean status(byte discriminant) {
 		System.out.print("Client received: " +  (byte) discriminant + " ");
 		
 		if (discriminant == Protocol.OK) {
 			System.out.println("[OK]");
 			return true;
 		}
 		System.out.println("[KO]");
 		return false;
 	}
 	
 	public int getLogin(String user) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_LOGIN + " [GET_LOGIN] " + user);
 		
 		w.writeDiscriminant(Protocol.GET_LOGIN);
 		w.writeString(user);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		byte d = r.readDiscriminant();
 		
 		if (d == Protocol.REPLY_LOGIN) {
 			int id = r.readInt();
 			System.out.println("Client received: " + Protocol.REPLY_LOGIN + " [REPLY_LOGIN] " + id);
 			return id;
 		}
 		return -1;
 			
 	}
 
 	public boolean query_test() throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.QUERY_TEST + " [QUERY_TEST]");
 		w.writeDiscriminant(Protocol.QUERY_TEST);
 		w.send();
 
 		Reader r = new ReaderClient(socket.getInputStream());
 		return status(r.readDiscriminant());
 	}
 	
 	public Brute getBruteInfo(int id) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_BRUTE_INFO + " [GET_BRUTE_INFO] " + id);
 		w.writeDiscriminant(Protocol.GET_BRUTE_INFO);
 		w.writeInt(id);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		byte d = r.readDiscriminant();
 		
 		System.out.print("Client received: " + d + " ");
 		
 		if (d == Protocol.REPLY_BRUTE_INFO) {
 			System.out.print("[REPLY_BRUTE_INFO] ");
 			String name = r.readString();
 			int level = r.readInt();
 			int life = r.readInt();
 			int strengh = r.readInt();
 			int speed = r.readInt();
 			System.out.println(name + " " + level + " " + life + " " + strengh + " " + speed);
 			return new Brute(name, level, life, strengh, speed);
 		}
 		
 		System.out.println("[KO]");
 		return null;
 	}
 
 	public ArrayList<Bonus> getBruteBonus(int id) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_BRUTE_BONUS + " [GET_BRUTE_BONUS] " + id);
 		w.writeDiscriminant(Protocol.GET_BRUTE_BONUS);
 		w.writeInt(id);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		byte d = r.readDiscriminant();
 				
 		System.out.print("Client received: " + d + " ");
 		
		if (d == Protocol.REPLY_BRUTE_BONUS) {		
			System.out.print("[REPLY_BRUTE_BONUS] ");
 			
 			int size = r.readInt();
 			System.out.print(size + " ");
 			
 			ArrayList<Bonus> bonus = new ArrayList<Bonus>();
 			for (int i=0; i<size; i++) {
 				String name = r.readString();
 				int level = r.readInt();
 				int life = r.readInt();
 				int strengh = r.readInt();
 				int speed = r.readInt();
 				System.out.print(name + " " + level + " " + life + " " + strengh + " " + speed + " ");
 				bonus.add(new Bonus(name, level, life, strengh, speed));
 			}
 			
 			System.out.println();
 			return bonus;
 		}
 		
 		System.out.println("[KO]");
 		return null;
 	}
 	
 	public int getAdversaire(int me) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_ADVERSAIRE + " [GET_ADVERSAIRE] " + me);
 		w.writeDiscriminant(Protocol.GET_ADVERSAIRE);
 		w.writeInt(me);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		byte d = r.readDiscriminant();
 		
 		System.out.print("Client received: " + d + " ");
 		
 		if (d == Protocol.REPLY_ADVERSAIRE) {
 			System.out.print("[REPLY_ADVERSAIRE] ");
 			int other = r.readInt();
 			System.out.println(other);
 			return other;
 		}
 		
 		System.out.println("[KO]");
 		return -1;
 	}
 	
 	public boolean getVictory(int one, int two) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_VICTORY + " [GET_VICTORY] " + one + " " + two);
 		w.writeDiscriminant(Protocol.GET_VICTORY);
 		w.writeInt(one);
 		w.writeInt(two);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		return status(r.readDiscriminant());
 	}
 	
 	public boolean getDefeat(int one, int two) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_DEFEAT + " [GET_DEFEAT] " + one + " " + two);
 		w.writeDiscriminant(Protocol.GET_DEFEAT);
 		w.writeInt(one);
 		w.writeInt(two);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		return status(r.readDiscriminant());
 	}
 
 	public int getCombat(int one, int two) throws IOException {
 		Writer w = new WriterClient(this.socket.getOutputStream());
 		
 		System.out.println("Client send: " + (byte) Protocol.GET_COMBAT + " [GET_COMBAT] " + one + " " + two);
 		w.writeDiscriminant(Protocol.GET_COMBAT);
 		w.writeInt(one);
 		w.writeInt(two);
 		w.send();
 		
 		Reader r = new ReaderClient(socket.getInputStream());
 		byte d = r.readDiscriminant();
 		
 		System.out.print("Client received: " + d + " ");
 		
 		if (d == Protocol.REPLY_COMBAT) {
 			System.out.print("[REPLY_COMBAT] ");
 			int winner = r.readInt();
 			System.out.println(winner);
 			return winner;
 		}
 		
 		System.out.println("[KO]");
 		return -1;
 	}
 	
 }
