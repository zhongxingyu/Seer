 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ogo.spec.game.lobby;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import ogo.spec.game.model.*;
 import ogo.spec.game.multiplayer.PeerInfo;
 import ogo.spec.game.multiplayer.client.Client;
 import ogo.spec.game.multiplayer.initserver.ChatServer;
 import ogo.spec.game.multiplayer.GameProto;
 
 /**
  *
  * @author florian
  */
 public class Lobby {
     GameRun game;
 
     GUI theGui;
 
     Client client;
     ChatServer initServer;
 
     List<PeerInfo> serverList;
 
     boolean isHost;
 
     public Lobby(){
         isHost = false;
     }
     
     private GameMap generateMap(){
         Random generator = new Random(0);
         TileType[][] types = new TileType[50][50];
         for (int i = 0; i < types.length; i++) {
             for (int j = 0; j < types[0].length; j++) {
                 int type = generator.nextInt(3);
                 switch (type) {
                     case 0:
                         types[j][i] = TileType.DEEP_WATER;
                         break;
                     case 1:
                         types[j][i] = TileType.LAND;
                         break;
                     case 2:
                         types[j][i] = TileType.SHALLOW_WATER;
                         break;
                 }
             }
         }
         return new GameMap(types);
     }
 
     private void initGame(int[][] data, String[] names, int id){
         Player[] players = new Player[names.length];
         for (int i = 0; i < names.length; i++) {
             players[i] = new Player(names[i]);
         }
         GameMap map = generateMap();
         
         for(int i = 0; i < data.length; i++){
             for(int j = 0; j < data[i].length; j++){
                Inhabitant inh;
                 if(data[i][j] == 0){
                     inh = new LandCreature(map.getTile(i*6, j*6), map);
                 }else if(data[i][j] == 1){
                     inh = new SeaCreature(map.getTile(i*6, j*6), map);
                 }else{
                     inh = new AirCreature(map.getTile(i*6, j*6), map);
                 }
                 map.getTile(i*6, j*6).setInhabitant(inh);
             }
         }
         
         Game game2 = new Game(players, generateMap());
         game = new GameRun(game2, id);
         client.setTokenChangeListener(game);
     }
 
     public void runGUI() throws Exception{
         theGui = new GUI(this);
         theGui.init();
     }
 
     private String[] convertServerList(List<PeerInfo> l){
         /* Moet nog wat descriptiever worden.. */
         String[] names = new String[l.size()];
         for(int i = 0; i < l.size(); i++){
             names[i] = l.get(i).ip.toString();
         }
         return names;
     }
 
     public String[] getServerNames() throws Exception{
         client = new Client();
         serverList = client.findServers();
         return convertServerList(serverList);
     }
 
     class DatagramReceiverRunnable implements Runnable
     {
         DatagramSocket sock;
 
         ConcurrentLinkedQueue<DatagramPacket> buffer = new ConcurrentLinkedQueue<DatagramPacket>();
 
         boolean read;
 
         DatagramReceiverRunnable(DatagramSocket sock)
         {
             this.sock = sock;
             this.read = true;
         }
 
         public void stop(){
             read = false;
         }
 
         public void run()
         {
             try {
                 while (read) {
                     DatagramPacket p = new DatagramPacket(new byte[1], 1);
 
                     sock.receive(p);
 
                     buffer.add(p);
                 }
             } catch (IOException e) {
                 if(read){
                     System.err.println("I/O Error");
                     e.printStackTrace();
                     System.exit(1);
                 }
             }
         }
     }
 
     public final static int INIT_PORT = 25945; // this is a UDP port
     public final static int INIT_LISTEN_PORT = 4444; // this is a UDP port
     public final static String BROADCAST_IP = "192.168.1.255";
     
     public final static int MAX_CONNECTION_TRIES = 20;
 
     public boolean openLobby() throws Exception{
         isHost = true;
 
         /* Start new Server to connect to */
         initServer = new ChatServer();
         initServer.run();
 
         boolean done = false;
 
         DatagramPacket packet;
         DatagramSocket sendSock;
         DatagramSocket receiveSock = new DatagramSocket(INIT_PORT);
 
         int count = 0;
         while(!done && count < MAX_CONNECTION_TRIES){
             packet = new DatagramPacket(new byte[]{2}, 1, InetAddress.getByName(BROADCAST_IP), INIT_PORT);
             sendSock = new DatagramSocket();
             sendSock.send(packet);
 
             DatagramReceiverRunnable run = new DatagramReceiverRunnable(receiveSock);
             new Thread(run).start();
 
             Thread.sleep(100);
 
             while((packet = run.buffer.poll()) != null){
                 //System.out.println("Data: " + packet.getData()[0]);
                 if(packet.getData()[0] == 2){
                     break;
                 }
 
             }
             if(packet != null){
                 //System.err.println("Found Packet!");
                 getServerNames();
 
                 PeerInfo ownServer = null;
                 for(PeerInfo p : serverList){
                     if(packet.getAddress().toString().equals(p.ip.toString())){
                         ownServer = p;
                     }
                 }
 
                 client.connectToInitServer(ownServer);
 
                 done = true;
             }else{
                 System.err.println("LOBBY: Could not find own server; unable to connect self to lobby");
             }
             run.stop();
             count ++;
         }
         receiveSock.close();
         if(!done){
             initServer.close();
         }
         return done;
     }
 
     public void joinLobby(int serverNum) throws Exception{
         isHost = false;
 
         client.connectToInitServer(serverList.get(serverNum));
     }
 
     private GameProto.IsReady.Builder parseReadyInfo(){
         int[] creatures = theGui.getCreatureInfo();
         return  GameProto.IsReady.newBuilder()
                 .setCreature1(creatures[0])
                 .setCreature2(creatures[1])
                 .setCreature3(creatures[2]);
     }
 
     class TokenRingRunnable implements Runnable
     {
         Client client;
         public TokenRingRunnable(Client c){
             client = c;
         }
 
         public void run(){
             try{
                 client.startTokenRing();
             }catch (Exception e){
                 e.printStackTrace();
             }
         }
     }
 
     public void finishConnection() throws Exception
     {
         GameProto.InitialGameState data = client.receiveInitialGameState();
         
         int players = data.getDataCount()/3;
         int[][] creatureData = new int[players][3];
         String[] names = new String[players];
         for(int i = 0; i < players; i++){
             for (int j = 0; j < 3; j++) {
                 creatureData[i][j] = data.getData(i*3 + j);
             }
             names[i] = data.getNames(i);
         }
         
         int id = data.getId();
         
         client.connectToPeer();
         
         theGui.stop();
         
         initGame(creatureData, names, id);
 
         new Thread(new TokenRingRunnable(client)).start();
     }
 
     public void setReady() throws Exception{
         GameProto.IsReady.Builder ready = parseReadyInfo();
         client.setReady(ready.setName(theGui.nickname).build());
     }
 
     class InitConnectionRunnable implements Runnable{
         ChatServer init;
         int[][] data;
         String[] names;
         public InitConnectionRunnable(ChatServer initServer, int[][] creatureData, String[] playerNames){
             init = initServer;
             data = creatureData;
             names = playerNames;
         }
 
         public void run(){
             try{
                 init.sendInitialGameState(data, names);
                 
                 init.initConnection();
             } catch (Exception e){
                 System.err.println("Problem with Init Server:\n" + e.getMessage());
             }
         }
     }
 
     public void startGame() throws Exception{
         assert(isHost && canStartGame());
         
         setReady();
 
         initServer.stopReadyState();
         
         int[][] creatureData = initServer.getCreatureTypes();
         
         String[] names = initServer.getNames();
         for(int i = 0; i < names.length; i++){
             names[i] = i + "-" + names[i];
         }
         
         new Thread(new InitConnectionRunnable(initServer, creatureData, names)).start();
 
         finishConnection();
     }
 
     public int getClientCount(){
         return initServer.getClientCount();
     }
 
     public boolean canStartGame(){
         return initServer.canStartGame();
     }
 
     public static void main(String[] args) throws Exception{
         new Lobby().runGUI();
     }
 }
