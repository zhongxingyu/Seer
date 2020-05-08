 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RSLBench.Algorithms.MS;
 
 import RSLBench.Assignment.Assignment;
 import RSLBench.Assignment.DCOP.DCOPAgent;
 import RSLBench.Comm.Message;
 import RSLBench.Comm.CommunicationLayer;
 import RSLBench.Helpers.Utility.ProblemDefinition;
 
 import rescuecore2.config.Config;
 import rescuecore2.worldmodel.EntityID;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import messages.MailMan;
 import messages.MessageFactory;
 import messages.MessageFactoryArrayDouble;
 import factorgraph.NodeVariable;
 import factorgraph.NodeFunction;
 import factorgraph.NodeArgument;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import operation.OPlus;
 import operation.OPlus_MaxSum;
 import operation.OTimes;
 import operation.OTimes_MaxSum;
 
 /**
  * This class implements the MaxSum algorithm according to RMASBench specification.
  */
 public class MaxSumAgent implements DCOPAgent {
     private static final Logger Logger = LogManager.getLogger(MaxSumAgent.class);
 
     /* Variables to setup the jMaxSum library */
     private static final MessageFactory JMS_MSG_FACTORY = new MessageFactoryArrayDouble();
     private static final OTimes JMS_OTIMES = new OTimes_MaxSum(JMS_MSG_FACTORY);
     private static final OPlus JMS_OPLUS = new OPlus_MaxSum(JMS_MSG_FACTORY);
 
     /* Class-wide variables (seriously...) */
     private static MSumOperator_Sync jMSOperator = new MSumOperator_Sync(JMS_OTIMES, JMS_OPLUS);
     private static MailMan jMSMailMan = new MailMan();
     private static HashSet<NodeVariable> allJMSVariables = new HashSet<>();
     private static HashSet<NodeFunction> allJMSFunctions = new HashSet<>();
     private static ArrayList<MaxSumAgent> allMaxSumAgents = new ArrayList<>();
 
     private static ProblemDefinition problemDefinition = null;
 
     private EntityID agentID;
     private EntityID targetID = Assignment.UNKNOWN_TARGET_ID;
     private JMSAgent jMSAgent;
     private int nFGMessages = 0;
     private long nFGSentBytes = 0;
 
     private static HashMap<EntityID, ArrayList<EntityID>> _consideredVariables = new HashMap<>();
 
     /**
      * Get the jMaxSum agent for this MaxSum agent.
      *
      * @return the jMaxSum agent
      */
     public JMSAgent getJMSAgent() {
         return jMSAgent;
     }
 
     @Override
     public void initialize(Config config, EntityID agentID, ProblemDefinition definition) {
         allMaxSumAgents.add(this);
         problemDefinition = definition;
 
         this.agentID = agentID;
         jMSAgent = JMSAgent.getAgent(agentID.getValue());
         jMSAgent.setPostservice(jMSMailMan);
         jMSAgent.setOp(jMSOperator);
 
         // Each agent controls only one variable, so we can associate it with the agentid
         NodeVariable variableNode = NodeVariable.getNodeVariable(agentID.getValue());
         allJMSVariables.add(variableNode);
         jMSAgent.addVariable(variableNode);
 
         // Assign agents to functions
         for (EntityID fire : problemDefinition.getFireAgentNeighbors(agentID)) {
             NodeFunction functionNode = NodeFunction.putNodeFunction(fire.getValue(), new RMASTabularFunction());
             // If this is the first agent seen for this function, it becomes the function runner
             if (!allJMSFunctions.contains(functionNode)) {
                 allJMSFunctions.add(functionNode);
                 jMSAgent.addFunction(functionNode);
             }
             functionNode.addNeighbour(variableNode);
             variableNode.addNeighbour(functionNode);
            variableNode.addValue(NodeArgument.getNodeArgument(fire.getValue()));
         }
     }
 
     private static void tupleBuilder() {
         for (NodeFunction function : allJMSFunctions) {
             double cost = 0;
             int countAgent = 0;
             int target = function.getId();
             int[] possibleValues = {0, target};
             int[][] combinations = createCombinations(function.size(), possibleValues);
             for (int[] arguments : combinations) {
                 NodeArgument[] arg = new NodeArgument[function.size()];
 
                 for (int i = 0; i < function.size(); i++) {
                     arg[i] = NodeArgument.getNodeArgument(arguments[i]);
                     Iterator<NodeVariable> prova = function.getNeighbour().iterator();
                    if (arg[i].getValue() == target) {
                         countAgent++;
                         NodeVariable var = prova.next();
                         cost = cost + problemDefinition.getFireUtility(new EntityID(var.getId()), new EntityID(target));
                     }
                 }
                 cost -= problemDefinition.getUtilityPenalty(new EntityID(target), countAgent);
 
                 function.getFunction().addParametersCost(arg, cost);
             }
         }
     }
 
     @Override
     public boolean improveAssignment() {
         Set<NodeVariable> vars = jMSAgent.getVariables();
         Iterator<NodeVariable> it = vars.iterator();
         NodeVariable var = it.next();
         HashSet<NodeFunction> func = var.getNeighbour();
         if (!func.isEmpty()) {
             jMSAgent.updateVariablesValues();
         }
 
         for (NodeVariable variable : jMSAgent.getVariables()) {
             try {
                 String target = variable.getStateArgument().getValue().toString();
                 targetID = new EntityID(Integer.parseInt(target));
             } catch (exception.VariableNotSetException e) {
                 Logger.warn("Agent " + getAgentID() + " unassigned!");
                 targetID = problemDefinition.getHighestTargetForAgent(agentID);
             }
         }
 
         return true;
     }
 
     @Override
     public EntityID getAgentID() {
         return agentID;
     }
 
     @Override
     public EntityID getTargetID() {
         return targetID;
     }
 
     @Override
     public Collection<Message> sendMessages(CommunicationLayer com) {
         Collection<MS_MessageQ> qMessages = jMSAgent.sendQMessages();
         for (MS_MessageQ messageQ : qMessages) {
             // Locate the agent that controls this function, and send the message to that ID
             for (MaxSumAgent agent : allMaxSumAgents) {
                 if (agent.isLocalFunction(messageQ.getFunction())) {
                     com.send(agent.getAgentID(), messageQ);
                     break;
                 }
             }
         }
 
 
         Collection<MS_MessageR> rMessages = jMSAgent.sendRMessages();
         for (MS_MessageR messageR : rMessages) {
             // The recipient variable id matches the EntityID of the recipient agent
             com.send(new EntityID(messageR.getVariable().getId()), messageR);
         }
 
         jMSAgent.sendZMessages();
 
         // Combine the lists of both message types and return all of them
         Collection<Message> allmex = new ArrayList<>();
         allmex.addAll(qMessages);
         allmex.addAll(rMessages);
         return allmex;
     }
 
     private boolean isLocalFunction(NodeFunction f) {
         return jMSAgent.getFunctions().contains(f);
     }
 
     @Override
     public void receiveMessages(Collection<Message> messages) {
         Collection<MS_MessageQ> mexQ = new ArrayList<>();
         Collection<MS_MessageR> mexR = new ArrayList<>();
 
         for (Message msg : messages) {
             MS_Message mex = (MS_Message)msg;
             if (mex instanceof MS_MessageQ) {
                 mexQ.add((MS_MessageQ)mex);
             } else if (mex instanceof MS_MessageR) {
                 mexR.add((MS_MessageR)mex);
             }
         }
 
         jMSAgent.readQMessages(mexQ);
         jMSAgent.readRMessages(mexR);
     }
 
     private static int[][] createCombinations(int functionSize, int[] possibleValues) {
         int totalCombinations = (int) Math.pow(2, functionSize);
 
         int[][] combinationsMatrix = new int[totalCombinations][functionSize];
         int changeIndex = 1;
 
         for (int i = 0; i < functionSize; i++) {
             int index = 0;
             int count = 1;
 
             changeIndex = changeIndex * possibleValues.length;
             for (int j = 0; j < totalCombinations; j++) {
                 combinationsMatrix[j][i] = possibleValues[index];
                 if (count == (totalCombinations / changeIndex)) {
                     count = 1;
                     index = (index + 1) % (possibleValues.length);
                 } else {
                     count++;
                 }
 
             }
         }
         return combinationsMatrix;
     }
 
     /**
      * Resets the static members of this class so that it gets ready for a new step of the
      * simulator.
      */
     public static void reset() {
         Logger.debug("Resetting!");
 
         JMSAgent.resetIds();
         NodeVariable.resetIds();
         NodeFunction.resetIds();
         NodeArgument.resetIds();
         jMSOperator = new MSumOperator_Sync(JMS_OTIMES, JMS_OPLUS);
         jMSMailMan = new MailMan();
 
         allJMSVariables.clear();
         allJMSFunctions.clear();
         _consideredVariables.clear();
         allMaxSumAgents.clear();
     }
 
     /**
      * Finishes the initialization phase (after constructing all agents)
      */
     public static void finishInitialization() {
         tupleBuilder();
     }
 
     @Override
     public long getConstraintChecks() {
         int totalnccc = 0;
         for (NodeFunction function : allJMSFunctions) {
             totalnccc += ((RMASTabularFunction) function.getFunction()).getNCCC();
         }
         return totalnccc;
     }
 
     /**
      * Prints the FactorGraph in .dot format (to be visualized with graphviz or similar)
      */
     public static void printFG(OutputStream os) {
         PrintStream fw = new PrintStream(os);
         fw.println("graph Factor {");
         for (NodeVariable var : allJMSVariables) {
             int agent_id = var.getId();
             JMSAgent agent = JMSAgent.getAgent(agent_id);
             Set<NodeFunction> agent_fun = agent.getFunctions();
 
             fw.println(agent_id + " [shape=box]");
             for (NodeFunction f : agent_fun) {
                 fw.println(agent_id + " -- " + f.getId() + " [color=blue]");
             }
             for (NodeFunction f : NodeVariable.getNodeVariable(agent_id).getNeighbour()) {
                 if (!agent_fun.contains(f)) {
                     fw.println(agent_id + " -- " + f.getId());
                 }
             }
             fw.flush();
         }
         fw.println("}");
     }
 
     public static void printDimTuples() {
         for (NodeVariable var : allJMSVariables) {
             int agent_id = var.getId();
             JMSAgent agent = JMSAgent.getAgent(agent_id);
             Set<NodeFunction> agent_fun = agent.getFunctions();
 
             try (FileWriter fw = new FileWriter("tuples_dim.txt", true)) {
                 fw.write("Agent: " + agent_id + "\n");
 
                 for (NodeFunction f : agent_fun) {
                     int num_tup = 1;
                     int num_real = 1;
                     for (NodeVariable v : f.getNeighbour()) {
                         num_tup *= v.getNeighbour().size();
                         num_real *= 2;
                     }
                     num_tup = (num_tup == 1) ? 0 : num_tup;
                     num_real = (num_real == 1) ? 0 : num_real;
                     fw.write("\t Funtion: " + f.id() + " dim: " + num_tup + "\n");
                     fw.write("\t Funtion: " + f.id() + " dim_real: " + num_real + "\n");
                 }
 
                 fw.write("----------------------------------------------------------------\n");
                 fw.flush();
             } catch (IOException ex) {
                 Logger.error(ex);
             }
         }
     }
 
     public void printNMex() {
         try (FileWriter fw = new FileWriter("tables.stats", true)) {
             for (NodeFunction function : (HashSet<NodeFunction>) jMSAgent.getFunctions()) {
                 fw.write("Number of tuples tried for function " + function.getId() + ": " + ((RMASTabularFunction) function.getFunction()).getNCCC() + "\n");
                 fw.flush();
             }
         } catch (IOException ex) {
             Logger.error(ex);
         }
     }
 
 }
 
