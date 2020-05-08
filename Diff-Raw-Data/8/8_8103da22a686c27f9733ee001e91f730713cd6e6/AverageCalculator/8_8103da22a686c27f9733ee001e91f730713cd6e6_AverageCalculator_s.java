 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.*;
 import jade.wrapper.AgentContainer;
 
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.UnreadableException;
 
 import java.io.Serializable;
 import java.io.IOException;
 
 import java.util.Set;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.HashMap;
 
 public class AverageCalculator extends Agent {
     private static int total;
     private static Set<AID> agents;
     private static AID manager = new AID("manager",false);
 
     private static enum PHASE { READY, SEND_NUMBER, SEND_AVERAGE, SET_AVERAGE };
     private static int  sended = 0;
     private static int  links  = 0;
 
     private static double proportion = 0;
 
     private double number = 0;
 
     protected void setup() {
         final Object[] args = getArguments();
         if ("manager".equals((String)args[0])) {
             total  = args.length - 3;
             final int limit = Integer.parseInt((String)args[1]);
             proportion = Double.parseDouble((String)args[2]);
             agents = new HashSet<AID>();
             for (int i = 3; i < 3 + total; i++) {
                 agents.add(new AID((String)args[i],false));
             }
             addBehaviour(new SimpleBehaviour(this) {
                 private int     iteration = 0;
                 private boolean finished  = false;
                 public void action() {
                     iteration++;
                     if (iteration >= limit - 1) {
                         finished = true;
                     }
                     System.out.print("Iteration " + iteration + " of " + limit + ".\r");
                     final ACLMessage go = new ACLMessage(ACLMessage.PROPOSE);
                     go.setContent("go");
                     for (final AID agent : agents) {
                         go.addReceiver(agent);
                     }
                     send(go);
 
                     if (finished) {
                         System.out.println("Agents after " + limit + " iterations and proportion " + proportion + ":");
                     }
                     int received = 0;
                     while (received < total) {
                         final ACLMessage acl = blockingReceive(NumberMessage.matcher);
                         if (finished) {
                             final NumberMessage msg = NumberMessage.parse(acl.getContent());
                            System.out.println(msg.name.getName() + " = " + msg.number);
                         }
                         received++;
                     }
                     if (finished) {
                         System.out.println("Total agents: " + total);
                         System.out.println("Total links: " + total);
                         System.out.println("Sended messages: " + sended);
                         System.exit(/* ^_^ */ 0);
                     }
                 }
                 public boolean done() {
                     return finished;
                 }
             });
         } else {
             number = Double.parseDouble((String)args[0]);
             final TopologyUnit unit = new TopologyUnit(args, 1);
             links += unit.toSize;
             addBehaviour(new SimpleBehaviour(this) {
                 private PHASE phase = PHASE.READY;
 
                 private int receivedCount = 0;
                 private Map<AID,Double> received;
 
                 private void timestamp() {
                     String phaseStr;
                     switch (phase) {
                         case READY:        phaseStr = "READY";        break;
                         case SEND_NUMBER:  phaseStr = "SEND_NUMBER";  break;
                         case SEND_AVERAGE: phaseStr = "SEND_AVERAGE"; break;
                         case SET_AVERAGE:  phaseStr = "SET_AVERAGE";  break;
                         default:           phaseStr = "?";
                     }
                    System.out.println(getAID().getName() + " " + phaseStr);
                 }
 
                 private int temp = 0;
 
                 public void action() {
                     switch (phase) {
                         case READY: {
                             final ACLMessage acl = receive(matcher("go"));
                             if (acl != null) {
                                 phase = PHASE.SEND_NUMBER;
                             }
                             return;
                         }
                         case SEND_NUMBER: {
                             final int k = random(unit.toSize - 1);
                             int i = 0;
                             for (final AID to : unit.to) {
                                 final ACLMessage acl = new ACLMessage(ACLMessage.PROPOSE);
                                 acl.addReceiver(to);
                                 if (k == i) {
                                     acl.setContent(new NumberMessage(getAID(),number).toString());
                                 } else {
                                     acl.setContent(NumberMessage.nothing);
                                 }
                                 wrappedSend(acl);
                                 i++;
                             }
                             receivedCount = 0;
                             received = new HashMap<AID,Double>();
                             phase = PHASE.SEND_AVERAGE;
                             return;
                         }
                         case SEND_AVERAGE: {
                             if (receivedCount < unit.fromSize) {
                                 final ACLMessage acl = receive(NumberMessage.maybeMatcher);
                                 if (acl != null) {
                                     if (!NumberMessage.nothingMatcher.match(acl)) {
                                         final NumberMessage msg = NumberMessage.parse(acl.getContent());
                                         final int size = received.keySet().size();
                                         received.put(msg.name,msg.number);
                                     }
                                     receivedCount++;
                                 } else {
                                     return;
                                 }
                             }
                             final Set<AID> receivedFrom = received.keySet();
                             final int k = random(receivedFrom.size());
                             int i = 0;
                             for (final AID from : unit.from) {
                                 final ACLMessage acl = new ACLMessage(ACLMessage.PROPOSE);
                                 acl.addReceiver(from);
                                 if (receivedFrom.contains(from)) {
                                     if (k == i) {
                                         acl.setContent(new AverageMessage(proportion * number + (1 - proportion) * received.get(from)).toString());
                                     } else {
                                         acl.setContent(AverageMessage.nothing);
                                     }
                                     i++;
                                 } else {
                                     acl.setContent(AverageMessage.nothing);
                                 }
                                 wrappedSend(acl);
                             }
                             phase = PHASE.SET_AVERAGE;
                             return;
                         }
                         case SET_AVERAGE: {
                             if (unit.toSize != 0) {
                                 final ACLMessage acl = receive(AverageMessage.maybeMatcher);
                                 if (acl == null) {
                                     return;
                                 }
                                 if (!AverageMessage.nothingMatcher.match(acl)) {
                                     number = AverageMessage.parse(acl.getContent()).average;
                                 }
                             }
                             ACLMessage ignore = null;
                             while (ignore != null) {
                                 ignore = receive();
                             }
                             final ACLMessage ready = new ACLMessage(ACLMessage.PROPOSE);
                             ready.addReceiver(manager);
                             ready.setContent(new NumberMessage(getAID(),number).toString());
                             wrappedSend(ready);
                             phase = PHASE.READY;
                             return;
                         }
                     }
                 }
                 private void wrappedSend(final ACLMessage acl) {
                     sended++;
                     send(acl);
                 }
                 public boolean done() {
                     return false;
                 }
             });
         }
     }
 
     private static int random(final int max) {
         return (int)(Math.random() * (max + 1));
     }
 
     private static class NumberMessage {
         private final AID    name;
         private final double number;
         public NumberMessage(final AID name, final double number) {
             this.name   = name;
             this.number = number;
         }
         @Override
         public String toString() {
            return "number" + " " + name.getName() + " " + String.valueOf(number);
         }
         public static NumberMessage parse(final String input) {
             final String[] parts = input.split("\\s+");
             if (!match(input)) {
                 throw new IllegalStateException("Wrong serialization.");
             }
             return new NumberMessage(new AID(parts[1],false), Double.parseDouble(parts[2]));
         }
         private static final MessageTemplate matcher = new MessageTemplate(new MessageTemplate.MatchExpression() {
                 @Override
                 public boolean match(final ACLMessage msg) {
                     return NumberMessage.match(msg.getContent());
                 }
             });
         private static boolean match(final String msg) {
             final String[] parts = msg.split("\\s+");
             return parts.length == 3 && "number".equals(parts[0]);
         }
         public static final String nothing = "no_number";
         public static final MessageTemplate nothingMatcher = matcher(nothing);
         public static final MessageTemplate maybeMatcher = MessageTemplate.or(matcher,matcher(nothing));
     }
 
     private static class AverageMessage {
         private final double average;
         private AverageMessage(final double average) {
             this.average = average;
         }
         @Override
         public String toString() {
             return "average" + " " + String.valueOf(average);
         }
         public static AverageMessage parse(final String input) {
             final String[] parts = input.split("\\s+");
             if (!match(input)) {
                 throw new IllegalStateException("Wrong serialization.");
             }
             return new AverageMessage(Double.parseDouble(parts[1]));
         }
         public static final MessageTemplate matcher = new MessageTemplate(new MessageTemplate.MatchExpression() {
                 @Override
                 public boolean match(final ACLMessage msg) {
                     return AverageMessage.match(msg.getContent());
                 }
             });
         private static boolean match(final String msg) {
             final String[] parts = msg.split("\\s+");
             return parts.length == 2 && "average".equals(parts[0]);
         }
         public static final String nothing = "no_average";
         public static final MessageTemplate nothingMatcher = matcher(nothing);
         public static final MessageTemplate maybeMatcher = MessageTemplate.or(matcher,matcher(nothing));
     }
 
     private static class StringMatcher implements MessageTemplate.MatchExpression {
         private String string;
         public StringMatcher(final String string) {
             this.string = string;
         }
 
         @Override
         public boolean match(final ACLMessage msg) {
             return string.equals(msg.getContent());
         }
     }
 
     private static MessageTemplate matcher(final String string) {
         return new MessageTemplate(new StringMatcher(string));
     }
 }
