 package eu.uberdust.util;
 
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.websockets.commands.CommandWSListener;
 import eu.wisebed.wisedb.listeners.AbstractNodeReadingListener;
 import eu.wisebed.wisedb.listeners.LastNodeReadingConsumer;
 import eu.wisebed.wisedb.model.NodeReading;
 import org.apache.log4j.Logger;
 
import java.util.ArrayList;
import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: amaxilatis
  * Date: 3/10/12
  * Time: 9:29 PM
  */
 public class CommandDispatcher implements AbstractNodeReadingListener {
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(CommandDispatcher.class);
 
     private static CommandDispatcher instance;
     /**
      * A HashMap<CapabilityID:NodeID>.
      */
    private final transient List<CommandWSListener> listeners;
 
     public CommandDispatcher() {
        listeners = new ArrayList<CommandWSListener>();
         LastNodeReadingConsumer.getInstance().registerVirtualReadingListener(this);
     }
 
     public synchronized static CommandDispatcher getInstance() {
         if (instance == null) {
             instance = new CommandDispatcher();
         }
         return instance;
     }
 
     public void sendCommand(int id, final String destination, final String byteString) {
         LOGGER.info("Sending command to {" + destination + "} :" + byteString);
         final Message.Envelope envelope = Message.Envelope.newBuilder()
                 .setType(Message.Envelope.Type.CONTROL)
                 .setControl(Message.Control
                         .newBuilder()
                         .setDestination(destination)
                         .setPayload(byteString).build())
                 .build();
 
         for (CommandWSListener listener : listeners) {
             LOGGER.info("Sending command to Listener");
             if (listener.getTestbedId() == id) {
                 listener.update(envelope);
             }
         }
     }
 
     public void sendCommand(final NodeReading nodeReading) {
         if (!nodeReading.getCapability().getNode().getName().contains("virtual")) return;
 
         LOGGER.info("Sending command for " + nodeReading);
 
         final Message.Envelope envelope;
         if (nodeReading.getReading() != null) {
             envelope = Message.Envelope.newBuilder()
                     .setType(Message.Envelope.Type.CONTROL)
                     .setControl(Message.Control
                             .newBuilder()
                             .setDestination(nodeReading.getCapability().getNode().getName())
                             .setCapability(nodeReading.getCapability().getCapability().getName())
                             .setLastValue(nodeReading.getReading().toString()).build())
                     .build();
         } else {
             envelope = Message.Envelope.newBuilder()
                     .setType(Message.Envelope.Type.CONTROL)
                     .setControl(Message.Control
                             .newBuilder()
                             .setDestination(nodeReading.getCapability().getNode().getName())
                             .setCapability(nodeReading.getCapability().getCapability().getName())
                             .setLastValue(nodeReading.getStringReading()).build())
                     .build();
         }
 
         int testbedId = nodeReading.getCapability().getNode().getSetup().getId();
 
         for (CommandWSListener listener : listeners) {
             LOGGER.info("Sending command to Listener");
             if (listener.getTestbedId() == testbedId) {
                 listener.update(envelope);
             }
         }
     }
 
     public void add(CommandWSListener thisListener) {
         listeners.add(thisListener);
     }
 
     @Override
     public void update(final NodeReading nodeReading) {
         sendCommand(nodeReading);
     }
 }
