 package eu.wisebed.wisedb.controller;
 
 import eu.wisebed.wisedb.exception.UnknownTestbedException;
 import eu.wisebed.wisedb.model.*;
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.*;
 
 /**
  * CRUD operations for NodeCapabilities entities.
  */
 @SuppressWarnings("unchecked")
 public class VirtualNodeDescriptionControllerImpl extends AbstractController<VirtualNodeDescription> implements VirtualNodeDescriptionController {
 
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static VirtualNodeDescriptionController ourInstance = null;
 
     private static final String USERNAME = "user";
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(VirtualNodeDescriptionControllerImpl.class);
 
     private Random rand;
 
     /**
      * Public constructor .
      */
     public VirtualNodeDescriptionControllerImpl() {
         // Does nothing
         super();
     }
 
     /**
      * NodeCapabilityController is loaded on the first execution of
      * NodeCapabilityController.getInstance() or the first access to
      * NodeCapabilityController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static VirtualNodeDescriptionController getInstance() {
         synchronized (VirtualNodeDescriptionControllerImpl.class) {
             if (ourInstance == null) {
                 ourInstance = new VirtualNodeDescriptionControllerImpl();
             }
         }
         return ourInstance;
     }
 
     @Override
     public void delete(int id) {
         LOGGER.info("delete(" + id + ")");
         super.delete(new VirtualNodeDescription(), id);
     }
 
 
     public List<VirtualNodeDescription> list() {
         LOGGER.info("list()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(VirtualNodeDescription.class);
         return criteria.list();
     }
 
     @Override
     public String rebuild(int testbedId) {
         StringBuilder response = new StringBuilder("+---------------------------------------------------------------+\n");
         List<VirtualNodeDescription> virtualNodes = list();
         for (VirtualNodeDescription virtualNode : virtualNodes) {
             response.append("VirtualNode:" + virtualNode.getNode().getName()).append("\n");
 
             Set<Node> nodesFound = null;
             try {
                 JSONArray conditionsJSON = new JSONArray(virtualNode.getDescription());
                 response.append("Conditions:").append("\n");
                 for (int i = 0; i < conditionsJSON.length(); i++) {
                     JSONObject curCondition = new JSONObject(conditionsJSON.get(i).toString());
                     HashSet<Node> curNodesFound = new HashSet<Node>();
                     try {
                         response.append(curCondition.toString()).append("\n");
                         String capabilityName = curCondition.getString("capability");
                         Capability capability = CapabilityControllerImpl.getInstance().getByID(capabilityName);
                         String capabilityValue = curCondition.getString("value");
                         final List<NodeCapability> nodeCapabilities = NodeCapabilityControllerImpl.getInstance().list(virtualNode.getNode().getSetup(), capability);
                         if ("*".equals(capabilityValue)) {
                             for (NodeCapability nodeCapability : nodeCapabilities) {
                                 curNodesFound.add(nodeCapability.getNode());
                             }
                         } else {
                             for (NodeCapability nodeCapability : nodeCapabilities) {
                                 try {
                                    if (nodeCapability.getLastNodeReading().getStringReading().equals(capabilityValue)) {
                                         curNodesFound.add(nodeCapability.getNode());
                                     }
                                 } catch (Exception e) {
                                     LOGGER.error(nodeCapability.toString(), e);
                                 }
                             }
                         }
                     } catch (JSONException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     if (nodesFound == null) {
                         nodesFound = curNodesFound;
                     } else {
                         nodesFound.retainAll(curNodesFound);
                     }
                 }
             } catch (JSONException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             response.append("Should Include : ").append("\n");
             Capability virtual = CapabilityControllerImpl.getInstance().getByID("virtual");
             for (Node node : nodesFound) {
                 response.append(node.getName());
                 final Link link = LinkControllerImpl.getInstance().getByID(virtualNode.getNode().getName(), node.getName());
                 if (link != null) {
                     final LinkCapability linkCapability = LinkCapabilityControllerImpl.getInstance().getByID(link, virtual);
                     if (linkCapability != null) {
                         if (linkCapability.getLastLinkReading().getReading() != 1.0) {
                             addLinkReading(virtualNode.getNode().getName(), node.getName(), 1.0);
                             response.append("<--");
                         }
                     } else {
                         addLinkReading(virtualNode.getNode().getName(), node.getName(), 1.0);
                         response.append("<--");
                     }
                 } else {
                     addLinkReading(virtualNode.getNode().getName(), node.getName(), 1.0);
                     response.append("<--");
                 }
                 response.append("\n");
             }
             List<Link> links = LinkControllerImpl.getInstance().getBySource(virtualNode.getNode());
             response.append("Should Disconnect: ").append("\n");
             for (Link link : links) {
                 final LinkCapability linkCapability = LinkCapabilityControllerImpl.getInstance().getByID(link, virtual);
                 if (linkCapability.getLastLinkReading().getReading() != 0.0 && !nodesFound.contains(link.getTarget())) {
                     addLinkReading(virtualNode.getNode().getName(), link.getTarget().getName(), 0.0);
                     response.append(link.getTarget().getName()).append("\n");
                 }
             }
             response.append("+---------------------------------------------------------------+\n");
         }
 
         return response.toString();
     }
 
     void addLinkReading(String source, String dest, Double value) {
         try {
             LinkReadingControllerImpl.getInstance().insertReading(source, dest, "virtual", value, null, new Date());
         } catch (UnknownTestbedException e) {
             LOGGER.error(e, e);
         }
     }
 
 
     public VirtualNodeDescription getByID(int entityId) {
         LOGGER.info("getByID(" + entityId + ")");
         return super.getByID(new VirtualNodeDescription(), entityId);
     }
 
     @Override
     public VirtualNodeDescription getByUsername(String username) {
         LOGGER.info("list()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(User.class);
         criteria.add(Restrictions.eq(USERNAME, username));
         return (VirtualNodeDescription) criteria.uniqueResult();
     }
 }
