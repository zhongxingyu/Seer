 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import server.game.PlayerMP;
 import server.packet.Packet01PlayerJoin;
 import server.packet.Packet02GiveID;
 import server.packet.Packet05StartGame;
 
 public class MMP
 {
 	
 	public static void main(String[] args)
 	{
 		serverAcces = new MMP();
 		getServer().Host();
 	}
 	
 	private MMP()
 	{
 	}
 
 	public void Host()
 	{
 		Log("Launching Server...");
 		try
 		{
 			Log("Preparing OpenMMP Server...");
 			game = new ServerGame();
 			
 			Log("Binding to port 27960... ");
 			hostSocket = new ServerSocket(27960);
 			
 			Log("Launching Console Command Handler...");
 			conComm = new ConsoleCommand(this);
 			Thread t = new Thread(conComm);
 			t.start();
 			
 			playGame();
 			
 			Log("Stopping Server...");
 			hostSocket.close();
 		}
 		catch(Exception e)
 		{
 			Log("Error Occured!");
 			e.printStackTrace();
 		}
 	}
 	
 	public void addPlayer(Socket player)
 	{
 		Log("Incoming Connection... (" + nextId + ")");
 		try {
 			PlayerMP playermp = new PlayerMP(player);
 			playermp.setId(nextId++);
 			game.setPlayer(playermp, playermp.getId());
 			Packet02GiveID packet = new Packet02GiveID();
 			packet.id = playermp.getId();
 			Monopoly().sendPacket(packet, packet.id);
 			Log("Player " + packet.id + " has joined");
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public void startGame()
 	{
 		if(game.isPhase(1) && nextId >= 2)
 		{
 			Log("Stopping Player Acception");
 			game.setPhase(2);
 		}
 		else
 			Log("Cannot start the Game (Already Started / Only 1 player)");
 	}
 	
 	private void playGame()
 	{
 		// Phase 1 - Waiting for players
 		Log("Phase 1 - Waiting for players...");
 		ThreadPlayerAccept pAccepter = new ThreadPlayerAccept();
 		while(game.isPhase(1))
 		{
 			try
 			{
 				Thread.sleep(100);
 			}
 			catch(InterruptedException e)
 			{
 			}
 		}
 		pAccepter.interrupt();
 		Log("Preparing Game...");
 		
 		Monopoly().sendPacket(new Packet05StartGame(nextId));
 		for(int i = 0; i < Monopoly().getPlayers().length; i++)
 		{
 			PlayerMP player = Monopoly().getPlayer(i);
 			if(player != null)
 				Monopoly().sendPacket(new Packet01PlayerJoin(player.Username(), player.getColorCode(), player.getId()));
 		}
 		
 		Log("Starting Game...");
 		// Phase 2 - Let's Play
 		Log("Game has ended...");
 		try
 		{
 			hostSocket.close();
 		} catch (IOException e)
 		{
 			System.out.println("Failed to close port!");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void Log(String line)
 	{
 		System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] " + line);
 	}
 	
 	//Game
 	private ServerGame game;
 	public ServerGame Monopoly() { return game; }
 	
 	//Connection Data
 	private byte nextId = 0;
 	
 	//Server
 	private ServerSocket hostSocket;
 	private ConsoleCommand conComm;
 	
 	//Getter
 	public ServerSocket getSocket() { return hostSocket; }
 	
 	//Public Server Acces
 	private static MMP serverAcces;
 	public static MMP getServer() { return serverAcces; }
 
 }
