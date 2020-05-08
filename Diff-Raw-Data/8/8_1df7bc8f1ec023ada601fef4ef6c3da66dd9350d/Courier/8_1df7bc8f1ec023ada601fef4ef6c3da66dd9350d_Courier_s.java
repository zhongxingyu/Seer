 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sim.courier;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import sim.broker.Broker;
 import sim.courierworld.*;
 
 /**
  *
  * @author drew
  */
 public class Courier {
 
     public double profit;
     public Warehouse myPackages;
     private boolean isGlobal;
     // map the edges to costs
     private HashMap<NodeKey, Double> myNetwork;
     private List<Node> sourceNode;
     private double policy;
     private Broker userBroker; // the broker i used to give user a quote and used to give success rate.
 
     public Courier(boolean isGlobal) {
         myNetwork = new HashMap<>();
         sourceNode = new ArrayList<>();
         myPackages = new Warehouse();
         this.isGlobal = isGlobal;
     }
 
     public void insertKeyValPair(NodeKey k, double weight) {
         myNetwork.put(k, weight);
     }
 
     public void randInit(List<Node> nodeChoice, CourierWorld state, Node hubNode) {
         // Set up the couriers
         // generate a random fully connected graph
 
 
         // all combonations
         for (Node n : nodeChoice) {
             for (Node m : nodeChoice) {
                 if (!n.equals(m)) {
                     double randWeight = state.minWeight + state.random.nextDouble() * (state.maxWeight - state.minWeight);
                     insertKeyValPair(new NodeKey(n, m), randWeight);
                 }
             }
         }
 
 
         if (!isGlobal) {
             // must also connect to the global hub
             for (Node n : nodeChoice) {
                 double randWeight = state.minWeight + state.random.nextDouble() * (state.maxWeight - state.minWeight);
                 insertKeyValPair(new NodeKey(n, hubNode), randWeight);
             }
 
             // loop over the nodes that the courier occupies
             List<Node> allNodes = new ArrayList<>(nodeChoice);
             allNodes.add(hubNode);
             for (Node n : getSourceNodes()) {
                 Node nextNode = n, prevNode = n;
                 while (nextNode != hubNode) {
                     nextNode = allNodes.get(state.random.nextInt(allNodes.size()));
                     // ensure that we have no self loops
                     while (nextNode == prevNode) {
                         nextNode = allNodes.get(state.random.nextInt(allNodes.size()));
                     }
                     double randWeight = state.minViableWeight + state.random.nextDouble() * (state.maxWeight - state.minViableWeight);
                     getMap().put(new NodeKey(prevNode, nextNode), randWeight);
                 }
             }
         } else {
             double randWeight = state.minViableWeight + state.random.nextDouble() * (state.maxWeight - state.minViableWeight);
 
             Node a = nodeChoice.get(state.random.nextInt(nodeChoice.size()));
             Node b = nodeChoice.get(state.random.nextInt(nodeChoice.size()));
             // make sure the weight isn't back to itself
             while (a == b) {
                 a = nodeChoice.get(state.random.nextInt(nodeChoice.size()));
             }
             insertKeyValPair(new NodeKey(a, b), randWeight);
 
         }
     }
 
     /**
      * The cost network for the courier.
      *
      * @return
      */
     public HashMap<NodeKey, Double> getMap() {
         return myNetwork;
     }
 
     /**
      * Add a node that the courier is located in.
      *
      * @param userNode
      */
     public void addSource(Node userNode) {
         sourceNode.add(userNode);
     }
 
     /**
      * Gets the list of nodes that the courier occupies.
      *
      * @return
      */
     public List<Node> getSourceNodes() {
         return sourceNode;
     }
 
     public int getSomeStacks(Map.Entry<Warehouse.Key, Integer> stack, double rate, Node hubNode, CourierWorld world) {
 
         if (isGlobal == false && myNetwork.containsKey(new NodeKey(hubNode, stack.getKey().dest))) {
             return (int) (stack.getValue() * Math.random());
         } else if (isGlobal == true && hubNode != getDestinationGlobalHub(stack, world)) {
             // don't want to buy it if already at the right global hub
             //System.err.println(hubNode + "  " + getDestinationGlobalHub(stack, world));
 
             return (int) (stack.getValue() * Math.random());
         }
         return 0;
     }
 
     /**
      * What is the probability of success. Calculated based on the broker
      * success rate. Can't calculate the success rate directly since not
      * tracking each package individually.
      *
      * @return
      */
     public double getSuccessRate() {
         // 1 = defaulRate + succRate -> succRate = 1 - defaultRate
         return 1.0 - userBroker.getDefaultRate();
     }
 
     /**
      * User gives me the packages and I add the packages to myPackages and I pay
      * myself
      *
      * @param stack
      * @param fee
      */
     public void recievePackage(Entry<Warehouse.Key, Integer> stack, double fee) {
         myPackages.updateStack(stack.getKey().dest, stack.getKey().priority, stack.getValue());
         profit += fee; //TODO
     }
 
     /**
      * Returns the best quote from the list of brokers
      *
      * @param stack
      * @param hubNode
      * @param brokers
      * @return
      */
     public double getBrokerQuote(Entry<Warehouse.Key, Integer> stack, List<Broker> brokers) {
         Warehouse h = new Warehouse();
 
         h.updateStack(stack.getKey(), stack.getValue());
         // loop over the brokers and get a quote.
 
 
         double bestQuote = -1;
 
         // for each of the brokers get a quote
         for (Broker b : brokers) {
             double quote = b.getQuote(h);
             double succRate = b.getDefaultRate();
 
             if (bestQuote == -1) {
                 bestQuote = policy * quote * (1 - succRate) + (1 - policy) * quote;
                 userBroker = b;
             } else if (bestQuote > policy * quote * (1 - succRate) + (1 - policy) * quote) {
                 bestQuote = policy * quote * (1 - succRate) + (1 - policy) * quote;
                 userBroker = b;
             }
         }
         return bestQuote;
     }
 
     /**
      * Returns the amount that I will charge to take the stack from a user.
      *
      * @param stack
      * @param hubNode
      * @param brokers
      * @return
      */
     public double getQuote(Entry<Warehouse.Key, Integer> stack, List<Broker> brokers) {
         // return the best quote also need to factor in a profit margin...
         return getBrokerQuote(stack, brokers);
 
     }
 
     /**
      * Gives the myPacks to the best broker and clears myPacks
      *
      * @param brokers
      * @param myPacks
      */
     public void sendStacks(List<Broker> brokers, Warehouse myPacks) {
         if (myPacks.hasStack()) {
             Broker bestBroker = null;
             double bestQuote = -1;
 
             // for each of the brokers get a quote
             for (Broker b : brokers) {
                 double quote = b.getQuote(myPacks);
                 double succRate = b.getDefaultRate();
 
                 if (bestQuote == -1) {
                     bestQuote = policy * quote * (1 - succRate) + (1 - policy) * quote;
                     bestBroker = b;
                 } else if (bestQuote > policy * quote * (1 - succRate) + (1 - policy) * quote) {
                     bestQuote = policy * quote * (1 - succRate) + (1 - policy) * quote;
                     bestBroker = b;
                 }
             }
 
             // give the broker his fee and the packages
             if (bestBroker != null) {
                 bestBroker.addPackage(myPacks, bestQuote / (1.0 - policy + policy * (1 - bestBroker.getDefaultRate())));
                 // i have to reset myPackages I have given them to the broker
                 myPacks.clear();
             }
         }
     }
 
     /**
      * Sends all my packages to the best broker given the list of brokers
      *
      * @param brokers
      */
     public void sendPackageToBroker(List<Broker> brokers) {
         sendStacks(brokers, myPackages);
     }
 
     /**
      * Returns the global node that costs the courier the least to deliver the
      * stack to from sourceHub.
      *
      * @param sourceHub
      * @param stack
      * @param world
      * @return
      */
     public Node getBestGlobalNode(Node sourceHub, Entry<Warehouse.Key, Integer> stack, CourierWorld world) {
         // probability of delivering to global node that has the local node
         // stemming from it given priority
         // loop over the packages
 
         Node best = null;
 
         // basically same as when doing for local to global except
         // so loop over all of the hubs except sourceHub
         Warehouse wh = new Warehouse();
         wh.updateStack(stack.getKey(), stack.getValue());
 
         double bestCost = -1;
 
         for (Node globalNode : world.hubNodes) {
             //get the cost to transfer to that particular hub
 
             if (globalNode != sourceHub) {
                 double costToDeliver = myNetwork.get(new NodeKey(sourceHub, globalNode)) * stack.getValue();
                 double curQuote = getBrokerQuote(stack, globalNode.getHub().brokers);
                 if (bestCost == -1) {
                     bestCost = curQuote + costToDeliver;
                     best = globalNode;
                 } else if (bestCost > curQuote + costToDeliver) {
                     bestCost = curQuote + costToDeliver;
                     best = globalNode;
                 }
             }
 
         }
 
 
 
         return best;
     }
 
     public Node getDestinationGlobalHub(Entry<Warehouse.Key, Integer> destination, CourierWorld world) {
         Node globalDest = null;
 
         for (Node glob : world.hubNodes) {
             if (glob.getHub().localNodes.contains(destination.getKey().dest)) {
                 // found the global hub that has the local node as a branch
                 return glob;
             }
         }
 
         // return null otherwise it is bad...
         return globalDest;
 
     }
 
     /**
      * The globalNode is where I am located world is the whole world the
      * SimState
      *
      * @param globalNode
      * @param world
      */
     public void movePacksGlobally(Node globalNode, CourierWorld world) {
         // moves packages to most profitable broker
         // global couriers are globally connected so
         // they have the option of transporting to the
         // correct hub where the local destination stems
         // from or to a different hub where it the stacks
         // will be auctioned off next timestep
 
         // for each package decide whether I should deliver to the correct
         // global hub or to deliver to a different hub
         Iterator<Entry<Warehouse.Key, Integer>> iter = myPackages.getIterator();
         Warehouse wh = new Warehouse();
         while (iter.hasNext()) {
             Entry<Warehouse.Key, Integer> stacks = iter.next();
             wh.clear();
             wh.updateStack(stacks.getKey(), stacks.getValue());
             Node finalGlobDest = null;
             // deliver to final destination if priority is highest
             if (stacks.getKey().priority.equals(Warehouse.Priority.EXPRESS)) {
                 // find the final destination global hub
                 finalGlobDest = getDestinationGlobalHub(stacks, world);
                 //System.err.println(globalNode + "  " + finalGlobDest);
             } else {
                 // find the node that costs me the least
                 finalGlobDest = getBestGlobalNode(globalNode, stacks, world);
             }
 
 
             if (finalGlobDest != null) {
                 // send to the best broker at the destination hub
                 sendStacks(finalGlobDest.getHub().brokers, wh);
                 // must remove from myPackages since delivered
                 iter.remove();
 
                 // subtract cost to get to that hub
                 profit -= myNetwork.get(new NodeKey(globalNode, finalGlobDest)) * stacks.getValue();
 
             } else {
                 System.err.println("finalGlobDest was null");
             }
 
         }
 
 
     }
 
     /**
      * Delivers the packages and tries to get customers along the way.
      */
     public void deliverStacks(Node globalNode, CourierWorld world) {
 
         // loop over the packages
         Iterator<Entry<Warehouse.Key, Integer>> iter = myPackages.getIterator();
         int maxRandTravel = world.maxRandTravel;
 
         ArrayList<Entry<Warehouse.Key, Integer>> collectedWarehouse = new ArrayList<>();
         while (iter.hasNext()) {
             Entry<Warehouse.Key, Integer> stacks = iter.next();
             Node curNode = globalNode;// where I am right now
             int randTrav = world.random.nextInt(maxRandTravel + 1);
 
             // travel to randTrav number of nodes.
             for (int i = 0; i < randTrav; i++) {
                 double best = world.maxWeight;
                 Node next = null;
                 // find the best next node.
                 for (Node localNode : globalNode.getHub().localNodes) {
                     if (curNode != localNode) {
                        if (myNetwork.get(new NodeKey(curNode, localNode)) < best) {
                             best = myNetwork.get(new NodeKey(curNode, localNode));
                             next = localNode;
                         }
                     }
                 }
 
                 if (next != null) {
                     // not sure if i should do the division...
                    profit -= (best * (stacks.getValue() / randTrav));
                     // check if the node has any packages for me.
                     Entry<Warehouse.Key, Integer> wh = next.getUser().randGivePackage(this, world);
                     if (wh != null) {
                         collectedWarehouse.add(wh);
                     }
                     curNode = next;
                 }
 
             }
             // So the courier's profit can get into the negatives
             if (curNode != stacks.getKey().dest) {
 
                 // deliver to the destination node
                 profit -= (myNetwork.get(new NodeKey(curNode, stacks.getKey().dest)) * (stacks.getValue()));
             }
 
         }
 
         for (Entry<Warehouse.Key, Integer> wh : collectedWarehouse) {
             myPackages.updateStack(wh.getKey(), wh.getValue());
         }
 
 
     }
 
     public boolean isGlobal() {
         return isGlobal;
     }
 }
