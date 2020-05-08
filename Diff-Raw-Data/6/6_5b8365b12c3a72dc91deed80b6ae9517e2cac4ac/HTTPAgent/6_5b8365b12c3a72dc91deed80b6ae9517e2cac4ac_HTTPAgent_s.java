 package prj.httpApplication.agent;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import prj.cyclo.Agent;
 import prj.cyclo.TCPReactor;
 import prj.httpApplication.app.WebApp;
 import prj.httpApplication.connection.HTTPSocket;
 import prj.httpApplication.connection.HTTPSocketFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class HTTPAgent extends Agent
 {
     private int _port = 8080;
     private final HTTPSocketFactory _socketFactory;
     private Map<Socket, HTTPSocket> _sockets;
     private Logger _logger;
     private WebApp _application;
 
     public HTTPAgent(TCPReactor reactor, WebApp application)
     {
         super(reactor);
         _socketFactory = new HTTPSocketFactory(this);
         _sockets = new HashMap<>();
         _application = application;
         _logger = LoggerFactory.getLogger(HTTPAgent.class.getSimpleName());
     }
 
     @Override
     public void connectionMade(Socket socket)
     {
     }
 
     @Override
     public boolean isServer()
     {
         return true;
     }
 
     @Override
     public InetSocketAddress getSocketAddress() throws UnknownHostException
     {
         return new InetSocketAddress("0.0.0.0", _port);
     }
 
     @Override
     public void receive(Socket socket, byte[] incomingData)
     {
         getSocket(socket).parse(new String(incomingData));
     }
 
     private HTTPSocket getSocket(Socket socket)
     {
         HTTPSocket httpSocket;
         if ((httpSocket = _sockets.get(socket)) == null)
         {
             _sockets.put(socket, (httpSocket = _socketFactory.newSocket(socket)));
             _application.clientConnected(httpSocket);
         }
         return httpSocket;
     }
 
     @Override
     public void registrationFailed(IOException e)
     {
         _logger.debug("registration failed for httpAgent on port " + _port);
     }
 
     @Override
     public void close(Socket socket)
     {
         super.close(socket);
         _sockets.remove(socket);
     }
 
     @Override
     public void onClose(Socket socket)
     {
        _sockets.remove(socket).close();
     }
 }
