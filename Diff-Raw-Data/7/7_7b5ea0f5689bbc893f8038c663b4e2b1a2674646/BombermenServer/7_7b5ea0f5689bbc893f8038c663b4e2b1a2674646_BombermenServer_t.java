 package pt.up.fe.pt.lpoo.bombermen;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import pt.up.fe.pt.lpoo.bombermen.messages.CMSG_JOIN;
 import pt.up.fe.pt.lpoo.bombermen.messages.CMSG_MOVE;
 import pt.up.fe.pt.lpoo.bombermen.messages.CMSG_PLACE_BOMB;
 import pt.up.fe.pt.lpoo.bombermen.messages.Message;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_DESTROY;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_MOVE;
 import pt.up.fe.pt.lpoo.bombermen.messages.SMSG_SPAWN_PLAYER;
 import pt.up.fe.pt.lpoo.utils.Direction;
 import pt.up.fe.pt.lpoo.utils.Ref;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class BombermenServer implements Runnable
 {
     private ServerSocket _socket = null;
     private int _lastId = 0;
     private HashMap<Integer, ClientHandler> _clients = new HashMap<Integer, ClientHandler>();
     private HashMap<Integer, Entity> _entities = new HashMap<Integer, Entity>();
     private int _numberOfClients = 0;
     private MessageHandler _messageHandler;
 
     private Queue<ClientMessage> _messageQueue = new LinkedList<ClientMessage>();
 
     public void PushMessage(int guid, Message msg)
     {
         _messageQueue.add(new ClientMessage(guid, msg));
     }
 
     public void Update(int diff)
     {
         synchronized (_messageQueue)
         {
             while (!_messageQueue.isEmpty())
             {
                 _messageHandler.HandleMessage(_messageQueue.poll());
             }
         }
 
         for (Entity e1 : _entities.values())
             for (Entity e2 : _entities.values())
                 if (e1.GetGuid() != e2.GetGuid())
                     if (e1.Collides(e2))
                         e1.OnCollision(e2);
 
         synchronized (_clients)
         {
             ArrayList<Integer> removed = new ArrayList<Integer>();
             Iterator<ClientHandler> it = _clients.values().iterator();
             while (it.hasNext())
             {
                 ClientHandler ch = it.next();
 
                 ch.Update(diff);
 
                 if (!ch.IsStillConnected())
                 {
                     removed.add(ch.Guid);
                     it.remove();
                     System.out.println("Client Removed.");
                 }
             }
 
             for (Integer i : removed)
             {
                 _entities.remove(i);
                 SMSG_DESTROY msg = new SMSG_DESTROY(i);
                 for (ClientHandler ch : _clients.values())
                 {
                     ch.ClientSender.Send(msg);
                 }
             }
         }
 
         if (_numberOfClients != _clients.size())
         {
             _numberOfClients = _clients.size();
             System.out.println("Number of Clients: " + _numberOfClients);
         }
 
     }
 
     public BombermenServer(int port) throws IOException
     {
         _socket = new ServerSocket(port);
         System.out.println("Server created - " + InetAddress.getLocalHost().getHostAddress() + ":" + _socket.getLocalPort());
 
         _messageHandler = new MessageHandler()
         {
             @Override
             protected void CMSG_MOVE_Handler(int guid, CMSG_MOVE msg)
             {
                 Player p = _entities.get(guid).ToPlayer();
                 if (p == null) return;
 
                 boolean moved = true;
 
                 float x = p.GetX();
                 float y = p.GetY();
 
                 switch (msg.Dir)
                 {
                     case Direction.NORTH:
                         p.SetY(p.GetY() + p.GetSpeed());
                         break;
                     case Direction.SOUTH:
                         p.SetY(p.GetY() - p.GetSpeed());
                         break;
                     case Direction.EAST:
                         p.SetX(p.GetX() + p.GetSpeed());
                         break;
                     case Direction.WEST:
                         p.SetX(p.GetX() - p.GetSpeed());
                         break;
                     default:
                         moved = false;
                 }
 
                 if (moved)
                 {
                     for (Entity e : _entities.values())
                     {
                         if (p.GetGuid() != e.GetGuid() && p.Collides(e))
                         {
                             if (e.IsExplosion())
                             {
                                 p.OnCollision(e);
                                e.OnCollision(p);
                             }
                             else
                             {
                                 moved = false;
                                 p.SetPosition(x, y);
                             }
                             break;
                         }
                     }
                 }
 
                 if (moved)
                 {
                     SMSG_MOVE msg1 = new SMSG_MOVE(p.GetGuid(), p.GetPosition());
                     for (ClientHandler ch : _clients.values())
                         ch.ClientSender.Send(msg1);
 
                     System.out.println("Move message received from " + guid + " : " + msg + ", new Position: " + p.GetPosition());
                 }
             }
 
             @Override
             protected void CMSG_PLACE_BOMB_Handler(int guid, CMSG_PLACE_BOMB msg)
             {
                 System.out.println("Place bomb message received from " + guid + " : " + msg);
             }
 
             @Override
             protected void Default_Handler(int guid, Message msg)
             {
                 System.out.println("Unhandled message received from " + guid + " : " + msg);
             }
 
             @Override
             protected void CMSG_JOIN_Handler(int guid, CMSG_JOIN msg)
             {
                 Player p = new Player(guid, msg.Name, new Vector2(40, 40));
                 _entities.put(guid, p);
                 System.out.println("Player '" + msg.Name + "' (guid: " + guid + ") just joined.");
                
                 SMSG_SPAWN_PLAYER msg1 = new SMSG_SPAWN_PLAYER(guid, msg.Name, p.GetX(), p.GetY());
                 for (ClientHandler ch : _clients.values())
                    if (ch.Guid != guid)
                        ch.ClientSender.Send(msg1);
 
                 ClientHandler ch = _clients.get(guid);
                 for (Entity e : _entities.values())
                     ch.ClientSender.Send(e.GetSpawnMessage());
             }
         };
 
         MapLoader builder = new MapLoader(_entities, _lastId);
 
         Ref<Integer> width = new Ref<Integer>(0);
         Ref<Integer> height = new Ref<Integer>(0);
         Ref<Integer> lastId = new Ref<Integer>(_lastId);
 
         if (!builder.TryLoad(0, width, height, lastId))
         {
             System.out.println("Could not load map " + 0);
             return;
         }
 
         _lastId = lastId.Get();
 
         new Thread(this).start();
     }
 
     public static void main(String[] args) throws IOException
     {
         BombermenServer sv = new BombermenServer(7777);
 
         long millis = System.currentTimeMillis();
 
         while (true)
         {
             int dt = (int) (System.currentTimeMillis() - millis);
             millis = System.currentTimeMillis();
 
             sv.Update(dt);
 
             try
             {
                 Thread.sleep(20);
             }
             catch (InterruptedException e)
             {
                 e.printStackTrace();
             }
         }
 
     }
 
     @Override
     public void run()
     {
         do
         {
             Socket socket;
             try
             {
                 socket = _socket.accept();
 
                 int clientId = _lastId++;
 
                 ClientHandler ch;
 
                 ch = new ClientHandler(clientId, socket, this);
 
                 synchronized (_clients)
                 {
                     _clients.put(clientId, ch);
                 }
 
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
         while (true);
     }
 }
 
 class ClientHandler
 {
     public final int Guid;
     private Socket _socket;
     private final BombermenServer _server;
     private boolean _stillConnected = true;
     private int _timer = 0;
 
     public final Sender<Message> ClientSender;
 
     public class ServerReceiver extends Receiver<Message>
     {
         public ServerReceiver(Socket socket)
         {
             super(socket);
         }
 
         @Override
         public void run()
         {
             try
             {
                 ObjectInputStream in = new ObjectInputStream(_socket.getInputStream());
 
                 while (!_done)
                 {
                     try
                     {
                         Message msg = (Message) in.readObject();
                         if (_done) break;
                         if (msg == null) continue;
 
                         _server.PushMessage(Guid, msg);
 
                     }
                     catch (ClassNotFoundException e)
                     {
                         e.printStackTrace();
                     }
                     catch (EOFException e)
                     {
                     }
 
                 }
 
                 in.close();
 
             }
             catch (SocketException e)
             {
             }
             catch (IOException e1)
             {
                 e1.printStackTrace();
             }
         }
 
     };
 
     public final Receiver<Message> ClientReceiver;
 
     public ClientHandler(int guid, Socket socket, BombermenServer server)
     {
         Guid = guid;
         _socket = socket;
         _server = server;
         ClientSender = new Sender<Message>(_socket);
         ClientReceiver = new ServerReceiver(_socket);
     }
 
     public boolean IsStillConnected()
     {
         return _stillConnected;
     }
 
     public void Update(int diff)
     {
         _timer += diff;
 
         if (_timer >= 1000)
         {
             _timer = 0;
             try
             {
                 ClientSender.TrySend(null);
                 _stillConnected = true;
             }
             catch (IOException e)
             {
                 _stillConnected = false;
             }
         }
     }
 
 }
