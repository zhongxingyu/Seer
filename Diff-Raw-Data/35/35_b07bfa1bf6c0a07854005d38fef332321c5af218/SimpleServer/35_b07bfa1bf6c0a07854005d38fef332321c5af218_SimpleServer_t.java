 package com.github.zhongl.stompize;
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import static com.github.zhongl.stompize.Bytes.buf;
 
 /**
  * For testing.
  *
  * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
  */
 public abstract class SimpleServer implements StompV1_2 {
     @Override
     public void connect(@Required("accept-version") String acceptVersion, @Required("host") String host, @Optional("login") String login, @Optional("passcode") String passcode, @Optional("heart-beat") String heartBeat) {
         connected(acceptVersion, host + System.currentTimeMillis(), null, null);
     }
 
     @Override
     public void stomp(@Required("accept-version") String acceptVersion, @Required("host") String host, @Optional("login") String login, @Optional("passcode") String passcode, @Optional("heart-beat") String heartBeat) {
         connected(acceptVersion, host + System.currentTimeMillis(), null, null);
     }
 
     @Override
     public void send(@Required("destination") String destination, @Optional("transaction") String transaction, @Optional("receipt") String receipt, Content content) {
         topics.publish(destination, content);
         if (receipt != null) receipt(receipt);
     }
 
     @Override
     public void subscribe(@Required("destination") String destination, @Required("id") String id, @Optional("receipt") String receipt, @Optional("ack") String ack) {
         topics.subscribe(destination, id, this);
         if (receipt != null) receipt(receipt);
     }
 
     @Override
     public void unsubscribe(@Required("id") String id, @Optional("receipt") String receipt) {
         topics.unsubscribe(id);
         if (receipt != null) receipt(receipt);
     }
 
     @Override
     public void ack(@Required("id") String id, @Optional("receipt") String receipt, @Optional("transaction") String transaction) {
         if (receipt != null) receipt(receipt);
     }
 
     @Override
     public void nack(@Required("id") String id, @Optional("receipt") String receipt, @Optional("transaction") String transaction) {
         error("Unsupport Command", new Content(buf("NACK")));
     }
 
     @Override
     public void begin(@Required("transaction") String transaction, @Optional("receipt") String receipt) {
         error("Unsupport Command", new Content(buf("BEGIN")));
     }
 
     @Override
     public void commit(@Required("transaction") String transaction, @Optional("receipt") String receipt) {
         error("Unsupport Command", new Content(buf("COMMIT")));
     }
 
     @Override
     public void abort(@Required("transaction") String transaction, @Optional("receipt") String receipt) {
         error("Unsupport Command", new Content(buf("ABORT")));
     }
 
     @Override
     public void disconnect(@Optional("receipt") String receipt) {
         if (receipt != null) receipt(receipt);
     }
 
     public static void main(String[] args) throws Exception {
         int port = 9991;
         if (args.length > 0) port = Integer.valueOf(args[0]);
 
         ServerBootstrap bootstrap = new ServerBootstrap();
         bootstrap.group(new NioEventLoopGroup(1))
                  .channel(NioServerSocketChannel.class)
                  .localAddress(port)
                  .childHandler(new ChannelInitializer<Channel>() {
                      @Override
                      protected void initChannel(Channel ch) throws Exception {
                          final SimpleServer handle = Stompize.newInstance(SimpleServer.class, ch);
                         ch.pipeline().addLast(new ErrorHandler(handle), Stompize.inboundHandler(handle, 4096));
                      }
                  });
         bootstrap.bind().sync();
         System.out.println("Server listen at: " + port);
     }
 
     private static final Topics topics = new Topics();
 
     static class Topics {
 
         public void publish(String destination, Content content) {
             topic(destination).publish(content);
         }
 
         private Topic topic(String destination) {
             Topic t = map.get(destination);
             if (t != null) return t;
             t = new Topic(destination);
             Topic p = map.putIfAbsent(destination, t);
             return p == null ? t : p;
         }
 
         public void subscribe(String destination, String id, SimpleServer subscriber) {
             topic(destination).subscribe(id, subscriber);
         }
 
         private final ConcurrentMap<String, Topic> map = new ConcurrentHashMap<String, Topic>();
 
         public void unsubscribe(String id) {
             // TODO
         }
     }
 
     static class Topic {
 
         public Topic(String destination) {
             this.destination = destination;
         }
 
         public void publish(Content content) {
             if (subscribers.isEmpty()) {
                 System.err.println("Unsubscribed topic: " + destination);
                 return;
             }
 
             for (Map.Entry<String, SimpleServer> e : subscribers.entrySet()) {
                 e.getValue().message(destination, hash(content), e.getKey(), null, content);
             }
         }
 
         private static String hash(Content content) {
             return String.valueOf(content.hashCode());
         }
 
         public void subscribe(String id, SimpleServer subscriber) {
             subscribers.put(id, subscriber);
         }
 
         private final Map<String, SimpleServer> subscribers = new ConcurrentHashMap<String, SimpleServer>();
         private final String destination;
     }
 }
