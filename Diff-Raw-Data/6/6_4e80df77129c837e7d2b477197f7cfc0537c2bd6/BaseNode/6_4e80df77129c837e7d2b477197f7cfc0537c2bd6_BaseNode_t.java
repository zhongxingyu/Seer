 package edu.mason.insf.ann;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import edu.mason.insf.ann.utils.Constants;
 

 /**
  * Class that represents attributes that are common across all neural network architechtures.
  * The BaseNode class manages the receiving of input values and the distribution of the processed information to other
  * nodes in the network. THe BaseNode class does NOT define how the node objects process information; this task is left
  * to the BaseNode progeny.
  */
 public class BaseNode {
 
     private int ticket;
 
     protected int id;
 
     /*
     * The network node values define the network's state and result from the nodes processing input values. To
     * facilitate the implementation of various neural-network architechtures, a set of values is used for each instead
      * of a single value. If only one value is needed in the node object, the set will simply have a size of one. The
      * size of the value set is specified at the object's instantiation so no memory is wasted on unneeded array slots.
      * These operations take an id parameter specifying which value in the set is to be changed or retrieved.
     *
     * */
    protected ArrayList<Double> value = new ArrayList<Double>();
    protected ArrayList<Double> error = new ArrayList<Double>();
     protected LinkedList<BaseLink> inLinks = new LinkedList<BaseLink>();     //List for input Links
     protected LinkedList<BaseLink> outLinks = new LinkedList<BaseLink>();    //List for output links
 
     public BaseNode()
     {
         super();
     }
 
     public Double getValue(int id)
     {
         return value.get(id);
     }
 
     public void setValue(int id, Double newValue)
     {
         this.value.add(id,newValue);
     }
 
     public double getError(int id)
     {
         return this.error.get(id);
     }
 
     public void setError(int id, Double newError)
     {
         this.error.add(id, newError);
     }
 
     public int getId()
     {
         return id;
     }
 
     public LinkedList<BaseLink> getInLinks()
     {
         return inLinks;
     }
 
     public LinkedList<BaseLink> getOutLinks()
     {
         return outLinks;
     }
 
     public void setInLinks(LinkedList<BaseLink> inLinks) {
         this.inLinks = inLinks;
     }
 
     public void setOutLinks(LinkedList<BaseLink> outLinks) {
         this.outLinks = outLinks;
     }
 
 
     public int getValueSize()
     {
         return value.size();
     }
 
 
 
     public int getErrorSize()
     {
         return error.size();
     }
 
     /**
      * Create a link from this node to another node
      * @param toNode
      * @param link
      */
     public void createLinkTo(BaseNode toNode, BaseLink link)
     {
         outLinks.add(link);
         toNode.getInLinks().add(link);
         link.setOutNode(toNode);
         link.setInNode(this);
     }
 
     public void createLinkTo(BaseNode toNode)
     {
         BaseLink link = new BaseLink();
         outLinks.add(link);
         toNode.getInLinks().add(link);
         link.setOutNode(toNode);
         link.setInNode(this);
     }
 
 }
