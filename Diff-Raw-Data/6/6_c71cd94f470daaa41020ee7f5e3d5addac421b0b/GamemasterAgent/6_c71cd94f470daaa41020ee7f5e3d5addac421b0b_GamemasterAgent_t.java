 package sos.mas;
 
 import jade.content.ContentElement;
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.basic.Action;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.core.behaviours.ParallelBehaviour;
 import jade.core.behaviours.SequentialBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.JADEAgentManagement.ShutdownPlatform;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREInitiator;
 import sos.mas.ontology.GameOntology;
 import sos.mas.ontology.Guilty;
 import sos.mas.ontology.Prisoner;
 import sos.mas.ontology.Round;
 
 import java.util.Date;
 import java.util.Vector;
 
 public class GamemasterAgent extends Agent {
 
     private void out(String text, Object... args) {
         System.out.print("[" + getLocalName() + "] ");
         System.out.println(String.format(text, args));
     }
 
     private SubscriptionResponder subscriptionResponder;
     private Codec codec = new SLCodec(0);
     private Ontology ontology = GameOntology.getInstance();
     private GameHistory game;
 
     @Override
     protected void setup() {
         try {
             out("Starting");
 
             getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
             getContentManager().registerOntology(ontology);
 
             handleArguments();
             registerService();
 
             subscriptionResponder = new SubscriptionResponder(this);
             SequentialBehaviour gameRoundBehaviours = new SequentialBehaviour(this);
 
             for (int i = 0; i < game.getIterations(); i++) {
                 gameRoundBehaviours.addSubBehaviour(new BeginRoundBehaviour());
             }
 
             ParallelBehaviour runGameBehaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ANY);
             runGameBehaviour.addSubBehaviour(subscriptionResponder);
             runGameBehaviour.addSubBehaviour(gameRoundBehaviours);
 
             SequentialBehaviour behaviour = new SequentialBehaviour(this);
             behaviour.addSubBehaviour(runGameBehaviour);
             behaviour.addSubBehaviour(new EndGameBehaviour());
 
             addBehaviour(behaviour);
         } catch (FIPAException fe) {
             fe.printStackTrace();
 
             doDelete();
         }
     }
 
 
     private void handleArguments() {
         Object[] args = getArguments();
 
         if (args == null || args.length < 3 || args.length > 3) {
             out("Need to supply the names of the two prisoner agents and the number of iterations.");
 
             doDelete();
 
             return;
         }
 
         AID prisoner1 = new AID((String) args[0], AID.ISLOCALNAME);
         AID prisoner2 = new AID((String) args[1], AID.ISLOCALNAME);
         int iterations = Integer.parseInt((String) args[2]);
 
        if (iterations < 1) {
            out("Invalid value for iterations (must be > 0 but was %d)", iterations);

            doDelete();
        }

         game = new GameHistory(prisoner1, prisoner2, iterations);
     }
 
     private void registerService() throws FIPAException {
         DFAgentDescription dfd = new DFAgentDescription();
         dfd.setName(getAID());
         ServiceDescription sd = new ServiceDescription();
         sd.setName(getLocalName());
         sd.setType("prisoners-dilemma-gamemaster");
         sd.addOntologies(ontology.getName());
         sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL0);
         dfd.addServices(sd);
 
         DFService.register(this, dfd);
     }
 
     @Override
     protected void takeDown() {
         out("Stopping");
     }
 
     //
     // Behaviours
     //
 
     private class SubscriptionResponder extends jade.proto.SubscriptionResponder {
         public SubscriptionResponder(Agent a) {
             super(a, MessageTemplate.and(
                     MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                             MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
                     MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));
 
         }
 
         @Override
         protected ACLMessage handleSubscription(ACLMessage subscription) {
             // handle a subscription request
             // if subscription is ok, create it
             try {
                 createSubscription(subscription);
             } catch (Exception e) {
                 ACLMessage refuse = new ACLMessage(ACLMessage.REFUSE);
                 refuse.addReceiver(subscription.getSender());
                 refuse.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
 
                 return refuse;
             }
 
             // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
             ACLMessage agree = new ACLMessage(ACLMessage.AGREE);
             agree.addReceiver(subscription.getSender());
             agree.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
 
             return agree;
         }
 
         public void notify(ACLMessage inform) {
             // this is the method you invoke ("call-back") for creating a new inform message;
             // it is not part of the SubscriptionResponder API, so rename it as you like
 
             // go through every subscription
             Vector subs = getSubscriptions();
 
             for (int i = 0; i < subs.size(); i++)
                 ((jade.proto.SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
         }
     }
 
     private class BeginRoundBehaviour extends AchieveREInitiator {
         public BeginRoundBehaviour() {
             super(GamemasterAgent.this, null);
         }
 
         @Override
         protected Vector prepareRequests(ACLMessage request) {
             ACLMessage query = new ACLMessage(ACLMessage.QUERY_IF);
             query.addReceiver(game.getPrisoner1().getAgent());
             query.addReceiver(game.getPrisoner2().getAgent());
             query.setOntology(ontology.getName());
             query.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
             query.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
             query.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
 
             try {
                 myAgent.getContentManager().fillContent(query, new Guilty());
 
                 Vector<ACLMessage> result = new Vector<ACLMessage>(1);
                 result.add(query);
 
                 return result;
             } catch (Exception e) {
                 e.printStackTrace();
 
                 return null;
             }
         }
 
         private Guilty extracAnswerContent(Object notification) {
             ACLMessage inform = (ACLMessage) notification;
             try {
                 ContentElement content = getContentManager().extractContent(inform);
 
                 if (!(content instanceof Guilty)) {
                     out("ERROR: not instance of Guilty");
                     return null;
                 }
 
                 return (Guilty) content;
             } catch (Exception e) {
                 e.printStackTrace();
 
                 return null;
             }
         }
 
         @Override
         protected void handleFailure(ACLMessage failure) {
             if (failure.getSender().equals(myAgent.getAMS()))
                 // FAILURE notification from the JADE runtime: the receiver does not exist
                 out("Responder does not exist");
             else
                 out("Agent %s failed to perform the requested action", failure.getSender().getName());
         }
 
         @Override
         protected void handleAllResultNotifications(Vector notifications) {
             out("handling %d result notifications", notifications.size());
 
             if (notifications.size() < 0) {
                 out("ERROR: too few answers to query");
 
                 return;
             }
 
             if (notifications.size() > 2)
                 out("WARNING: more than two answers to query");
 
             String id = ((ACLMessage) notifications.get(0)).getConversationId();
 
             Round currentRound = game.newRound(id);
 
             Guilty guilty1 = extracAnswerContent(notifications.get(0));
             Guilty guilty2 = extracAnswerContent(notifications.get(1));
 
             if (game.getPrisoner1().getAgent().equals(guilty1.getPrisoner().getAgent())) {
                 currentRound.setConfession1(guilty1.getConfession());
                 currentRound.setConfession2(guilty2.getConfession());
             } else {
                 currentRound.setConfession1(guilty2.getConfession());
                 currentRound.setConfession2(guilty1.getConfession());
             }
 
             try {
                 ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                 inform.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
                 inform.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
                 inform.setOntology(ontology.getName());
 
                 getContentManager().fillContent(inform, currentRound);
 
                 subscriptionResponder.notify(inform);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private class EndGameBehaviour extends OneShotBehaviour {
         private EndGameBehaviour() {
             super(GamemasterAgent.this);
         }
 
         @Override
         public void action() {
             int[] yearsInJail = game.calculatePoints();
 
             out("%s got %d years in jail.", game.getPrisoner1().getAgent().getLocalName(), yearsInJail[0]);
             out("%s got %d years in jail.", game.getPrisoner2().getAgent().getLocalName(), yearsInJail[1]);
 
             Prisoner winner = null;
 
             if (yearsInJail[0] > yearsInJail[1])
                 winner = game.getPrisoner2();
             else if (yearsInJail[0] < yearsInJail[1])
                 winner = game.getPrisoner1();
 
             if (winner != null)
                 out("%s is the winner", winner.getAgent().getLocalName());
             else
                 out("the game is a draw");
 
             try {
                 ShutdownPlatform shutdown = new ShutdownPlatform();
                 Action action = new Action();
                 action.setActor(myAgent.getAMS());
                 action.setAction(shutdown);
 
                 ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                 message.setSender(myAgent.getAID());
                 message.addReceiver(myAgent.getAMS());
                 message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                 message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
                 message.setOntology(jade.domain.JADEAgentManagement.JADEManagementOntology.NAME);
                 myAgent.getContentManager().registerOntology(jade.domain.JADEAgentManagement.JADEManagementOntology
                         .getInstance());
                 myAgent.getContentManager().fillContent(message, action);
                 myAgent.send(message);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 }
