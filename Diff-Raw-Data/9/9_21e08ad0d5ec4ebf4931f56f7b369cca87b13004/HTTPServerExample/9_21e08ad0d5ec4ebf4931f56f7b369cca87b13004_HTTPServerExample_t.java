 /**
  * A simple example that uses a HTTPMessageBus to expose a service that
  * calculates whether or not a given number is a prime. Workload is spead
  * over three parallel calculators.
  * <p/>
  * Compile with:
  *     javac -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.0.1.jar HTTPServerExample.java
  *
  * Run with:
  *     java -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.0.1.jar:. HTTPServerExample
  * <p/>
  * JSON messages should be <code>POST</code>ed to
  * <a href="http://localhost:4567/actor/calc">http://localhost:4567/calc</a>
  * looking like:
  * <pre>
  * {
  *    "isPrime" : 123
  * }
  * </pre>
  *
  * To invoke the 'list' or 'ping' handlers you can point your browser at
  * <a href="http://localhost:4567/list">http://localhost:4567/list</a> or
  * <a href="http://localhost:4567/ping/calc">http://localhost:4567/ping/calc</a>.
  * Or simply use a command line tool like <code>wget</code> or <code>curl</code>
  * to send <code>GET</code> requests to those URLs.
  * <p/>
  * To POST an isPrime request using <code>curl</code> use the following command
  * in a Unix terminal:
  * <pre>
  *     curl http://localhost:4567/actor/calc --data '{ "isPrime" : 982451653 }'
  * </pre>
  */
 
 import juglr.*;
 
 import static juglr.Box.Type;
 
 import java.math.BigInteger;
 
 public class HTTPServerExample {
 
     static class CalcActor extends Actor {
 
         public void react(Message msg) {
             if (!(msg instanceof Box)) {
                 throw new MessageFormatException("Expected Box");
             }
 
             Box resp = Box.newMap();
             Box box = (Box)msg;
             if (!box.has("isPrime")) {
                 resp.put("error", "No 'isPrime' key in request");
                 send(resp, msg.getSender());
                 return;
             }
 
            Box test = box.get("isPrime");
             try {
                BigInteger bigInt = new BigInteger(test.toString());
                 // We guess right with 0.9990234375 probability
                 if (bigInt.isProbablePrime(10)) {
                     resp.put("response", "true");
                 } else {
                     resp.put("response", "false");
                 }
                 send(resp, msg.getSender());
             } catch (NumberFormatException e) {
                resp.put("error", "Not a valid integer: " + test.toString());
                 send(resp, msg.getSender());
             }
         }
     }
 
     public static void main (String[] args) throws Exception {
         // Make sure the default message bus is HTTPMessageBus
         System.setProperty("juglr.busclass", "juglr.net.HTTPMessageBus");
 
         // Delegate work to three CalcActors in a round-robin manner
         Actor actor = new DelegatingActor(
                 new CalcActor(), new CalcActor(), new CalcActor());
         MessageBus.getDefault().allocateNamedAddress(actor, "calc");
         MessageBus.getDefault().start(actor.getAddress());
 
         // Indefinite non-busy block
         synchronized (actor) {
             actor.wait();
         }
     }
 
 }
