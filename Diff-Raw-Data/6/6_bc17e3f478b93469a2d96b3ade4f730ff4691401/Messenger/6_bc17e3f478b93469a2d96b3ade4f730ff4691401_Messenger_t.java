 package clear4j.msg;
 
 import clear4j.msg.beans.DefaultMessage;
 import clear4j.msg.beans.DefaultQueue;
 import clear4j.msg.beans.DefaultReceiver;
 import clear4j.msg.queue.*;
 import clear4j.msg.queue.beans.HostPort;
 import clear4j.msg.queue.management.QueueManager;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.Socket;
 import java.util.concurrent.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * User: alexb
  * Date: 25/05/12
  * Time: 14:41
  */
 public final class Messenger {
     private Messenger() {
     }
 
     private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 
     private static final Logger LOG = Logger.getLogger(Messenger.class.getName());
 
 
     /*
      * SENDING 
      */
 
     public static <T extends Serializable> void send(final String queue, final T payload) {
         send(new DefaultMessage<T>(new DefaultQueue(queue, Host.LOCAL_HOST), payload));
     }
 
 //    public static <T extends Serializable> Message<T> track(final String queue, final T payload) throws ExecutionException, InterruptedException {
 //        return track(new DefaultMessage<T>(new DefaultQueue(queue, Host.LOCAL_HOST), payload));
 //    }
 
     public static <T extends Serializable> void send(final String host, final int port, final String queue, final T payload) {
         send(new DefaultMessage<T>(new DefaultQueue(queue, new HostPort(host, port)), payload));
     }
 
 //    public static <T extends Serializable> Message<T> track(final String host, final int port, final String queue, final T payload) throws ExecutionException, InterruptedException {
 //        return track(new DefaultMessage<T>(new DefaultQueue(queue, new HostPort(host, port)), payload));
 //    }
 
     //todo simplify above to use this method instead
     public static <T extends Serializable> void send(final Queue target, final T payload) {
         send(new DefaultMessage<T>(target, payload));
     }
 
     private static <T extends Serializable> void send(final Message<T> message) {
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, String.format("sending message: %s", message));
         }
 
         executor.execute(new Runnable() {
             @Override
             public void run() {
 
                 if (message.isLocal()) {
                     QueueManager.add(message);
                 } else {
                     try {
                         Host targetHost = message.getTarget().getHost();
                         Socket socket = new Socket(targetHost.getHost(), targetHost.getPort());
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                         out.flush();
                         out.writeObject(message);
                         out.flush();
                         out.close();
                         socket.close();
                     } catch (IOException e) {
                         LOG.log(Level.SEVERE, e.getMessage()); //TODO put on local exception queue
                     }
                 }
 
             }
         });
 
     }
 
     public static <T extends Serializable> Receiver<T> register(final String target, final MessageListener<T> listener) {
         return register(new DefaultQueue(target, Host.LOCAL_HOST), listener);
     }
 
     public static <T extends Serializable> Receiver<T> register(final String host, final int port, final String target, final MessageListener<T> listener) {
         return register(new DefaultQueue(target, new HostPort(host, port)), listener);
     }
 
     /*
     * RECEIVING
     */
     //TODO string queue should be Queue target
     private static <T extends Serializable> Receiver<T> register(final Queue target, final MessageListener<T> listener) {
 
         final Receiver<T> receiver = new DefaultReceiver<T>(target, listener);
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, String.format("registering receiver: %s", receiver));
         }
 
         if (receiver.isLocal()) {
             QueueManager.add(receiver);
         } else {
             //create local proxy queue for this receiver.
             Queue localProxyQueue = new DefaultQueue(receiver.getId(), Host.LOCAL_HOST);
             Receiver<T> localProxy = register(localProxyQueue, listener);
             //send request to remote host, ie put a receiver message to its receivers queue
             //this message will get picked up by the RemoteAdapter and a ('local' to the remote host) receiver created.
             //which proxies all messages received back to the localProxyQueue.
             Queue remoteReceivers = new DefaultQueue("remote-receivers", target.getHost());
             send(new DefaultMessage<Receiver<T>>(remoteReceivers, receiver));
         }
 
         return receiver;
     }
 
     //TODO remote
     public static <T extends Serializable> void unregister(final Receiver<T> receiver) {
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, String.format("un-registering receiver: %s", receiver));
         }
 
         QueueManager.remove(receiver);
     }
 
     static synchronized <T extends Serializable> void wait(final String queue) {
         wait(new DefaultMessage<String>(new DefaultQueue(queue, Host.LOCAL_HOST), "wait"));
     }
 
     private static synchronized <T extends Serializable> void wait(final Message<T> message) {
 
         try {
             if (LOG.isLoggable(Level.INFO)) {
                 LOG.log(Level.INFO, String.format("waiting message received: %s", track(message)));
             }
         } catch (Exception e) {
             throw new RuntimeException(e); //TODO
         }
     }
 
     /* //TODO remote tracking is broken.
      * implicitly creates a Receiver to receive a message from a queue
      * initially used primarily for testing. better use Messenger.register(clear4j.msg.Receiver).to(queue);
      */
     private static <T extends Serializable> Message<T> track(final Message<T> original) throws ExecutionException, InterruptedException {
        if (!original.getTarget().getHost().isLocal()){
            throw new UnsupportedOperationException("remote tracking is not yet implemented...");
        }
 
         final CountDownLatch latch = new CountDownLatch(1);
 
         final Message<T>[] returned = new Message[1];
 
         final Receiver<T> receiver = register(original.getTarget(), new MessageListener<T>() {
             @Override
             public void onMessage(clear4j.msg.queue.Message<T> message) {
                 if (message.getId().equals(original.getId())) {
                     returned[0] = message;
                     latch.countDown();
                 }
             }
         });
 
         FutureTask<clear4j.msg.queue.Message<T>> futureTask = new FutureTask<Message<T>>(
                 new Callable<Message<T>>() {
                     @Override
                     public Message<T> call() throws InterruptedException {
                         send(original);
                         latch.await();
                         unregister(receiver);
                         return returned[0];
                     }
                 }
         );
 
         executor.execute(futureTask);
 
         return futureTask.get();
     }
 
 }
