 package lv.k2611a;
 
 import java.io.IOException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.jetty.websocket.WebSocket;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 
 import com.alibaba.fastjson.JSON;
 
 import lv.k2611a.network.req.Request;
 import lv.k2611a.network.resp.CustomSerializationHeader;
 import lv.k2611a.network.resp.Left;
 import lv.k2611a.network.resp.Response;
 import lv.k2611a.service.connection.ConnectionState;
 import lv.k2611a.service.game.GameService;
import lv.k2611a.service.game.GameSessionsService;
 import lv.k2611a.service.global.GlobalSessionService;
 import lv.k2611a.service.global.GlobalUsernameService;
 import lv.k2611a.service.global.LobbyService;
 import lv.k2611a.service.scope.ConnectionKey;
 import lv.k2611a.service.scope.ContextService;
 import lv.k2611a.service.scope.GameKey;
 
 public class ClientConnection implements WebSocket.OnTextMessage, Runnable {
 
     private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);
 
     private volatile Connection _connection;
 
     @Autowired
     private GameService gameService;
 
     @Autowired
     private GlobalSessionService globalSessionService;
 
     @Autowired
    private GameSessionsService gameSessionsService;

    @Autowired
     private GlobalUsernameService globalUsernameService;
 
     @Autowired
     private AutowireCapableBeanFactory autowireCapableBeanFactory;
 
     @Autowired
     private ContextService contextService;
 
     @Autowired
     private LobbyService lobbyService;
 
     @Autowired
     private ConnectionState connectionState;
 
     private BlockingQueue<Response> queue = new LinkedBlockingQueue<Response>();
 
     private ExecutorService exec = Executors.newFixedThreadPool(1);
 
     private volatile boolean closed = false;
 
     private final ConnectionKey connectionKey;
 
     public ClientConnection(long id) {
         this.connectionKey = new ConnectionKey(id);
     }
 
     public void onOpen(Connection connection) {
         contextService.setConnectionKey(connectionKey);
         processOnOpen(connection);
         contextService.clearCurrentConnectionKey();
     }
 
     private void processOnOpen(Connection connection) {
         _connection = connection;
         connectionState.setConnection(this);
         exec.execute(this);
         globalSessionService.add(this);
         log.info("Connection opened");
 
     }
 
     public void onClose(int closeCode, String message) {
         closeInContext();
     }
 
     private void closeInContext() {
         try {
             contextService.setConnectionKey(connectionKey);
             contextService.setGameKey(connectionState.getGameKey());
             processOnClose();
         } finally {
             contextService.clearConnectionKey(connectionKey);
             contextService.clearCurrentGameKey();
             contextService.clearCurrentConnectionKey();
         }
 
     }
 
     private void processOnClose() {
         if (!closed) {
             closed = true;
         } else {
             return;
         }
 
         try {
             Integer playerId = connectionState.getPlayerId();
             if (playerId != null) {
                 gameService.freePlayer(playerId);
             }
         } catch (Throwable e) {
             log.error("Exception while freeing player", e);
         }
 
         try {
             exec.shutdown();
         } catch (Throwable e) {
             log.error("Exception while stopping the executor thread");
         }
 
         globalSessionService.remove(this);
 
         String username = connectionState.getUsername();
         if (username != null) {
             globalUsernameService.freeUsername(username);
             lobbyService.removeUserFromAllGames(username);
             Left left = new Left();
             left.setNickname(username);
            gameSessionsService.sendUpdate(left);
         }
 
         log.info("Connection closed. Id : " + connectionKey);
     }
 
     public void onMessage(String data) {
         processMessageInContext(data);
 
     }
 
     private void processMessageInContext(String data) {
         try {
             contextService.setConnectionKey(connectionKey);
             contextService.setGameKey(connectionState.getGameKey());
             processMessage(data);
         } finally {
             contextService.clearCurrentGameKey();
             contextService.clearCurrentConnectionKey();
         }
     }
 
     private void processMessage(String data) {
         NetworkPacket networkPacket = JSON.parseObject(data, NetworkPacket.class);
         Request request = null;
         try {
             request = (Request) JSON.parseObject(networkPacket.getPayload(), Class.forName("lv.k2611a.network.req." + networkPacket.getMessageName()));
             autowireCapableBeanFactory.autowireBean(request);
             request.process();
         } catch (Exception e) {
             log.error("Exception while processing message", e);
         }
     }
 
     @Override
     public void run() {
         long byteCount = 0;
         long timeStart = System.currentTimeMillis();
         while (!Thread.interrupted()) {
             try {
                 if (!_connection.isOpen()) {
                     break;
                 }
                 Response response = queue.poll(2, TimeUnit.SECONDS);
                 if (response == null) {
                     continue;
                 }
                 if (response instanceof CustomSerializationHeader) {
                     byteCount = serializeAndSendCustomSerialization(byteCount, (CustomSerializationHeader) response);
                 } else {
                     byteCount = serializeAndSendJson(byteCount, response);
                 }
             } catch (IOException e) {
                 log.error("Exception happened while working with websocket", e);
                 break;
             } catch (InterruptedException e) {
                 break;
             } catch (RuntimeException e) {
                 log.error("Exception happened while working with websocket", e);
                 break;
             }
         }
         if (_connection.isOpen()) {
             _connection.close();
         }
         long timeEnd = System.currentTimeMillis();
         long timePassed = timeEnd - timeStart;
         double speed = 0;
         if (timePassed / 1000 != 0) {
             speed = byteCount / (timePassed / 1000);
         }
         log.info("Message writer closed. Total bytes sent : " + byteCount + " . Time passed : " + timePassed + " . Speed : " + speed + " bytes / sec");
         closeInContext();
     }
 
     private long serializeAndSendJson(long byteCount, Response response) throws IOException {
         NetworkPacket networkPacket = new NetworkPacket();
         networkPacket.setMessageName(response.getClass().getSimpleName());
         networkPacket.setPayload(JSON.toJSONString(response));
         String toSend = JSON.toJSONString(networkPacket);
         byteCount += toSend.length() * 2;
         _connection.sendMessage(toSend);
         return byteCount;
     }
 
     private long serializeAndSendCustomSerialization(long byteCount, CustomSerializationHeader response)
             throws IOException {
         CustomSerializationHeader responseCasted = response;
         byte[] data = responseCasted.toBytes();
         byte[] payload = new byte[data.length + 1];
         payload[0] = responseCasted.serializerId();
         System.arraycopy(data, 0, payload, 1, data.length);
         _connection.sendMessage(payload, 0, payload.length);
         byteCount += data.length;
         return byteCount;
     }
 
     public void sendMessage(Response response) {
         queue.add(response);
     }
 
     public void processInConnectionsContext(Runnable runnable) {
         ConnectionKey connectionKeyWas = contextService.getCurrentConnectionKey();
         GameKey gameKeyWas = contextService.getCurrentGameKey();
         try {
 
             contextService.setConnectionKey(connectionKey);
             contextService.setGameKey(connectionState.getGameKey());
 
             runnable.run();
 
         } finally {
 
             contextService.setGameKey(gameKeyWas);
             contextService.setConnectionKey(connectionKeyWas);
         }
     }
 }
