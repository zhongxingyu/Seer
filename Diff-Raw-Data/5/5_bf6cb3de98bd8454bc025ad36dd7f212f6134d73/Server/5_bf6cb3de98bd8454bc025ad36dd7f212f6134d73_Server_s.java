 package server;
 
 import java.io.*;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import btrplace.model.*;
 import btrplace.model.constraint.*;
 import btrplace.model.view.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 @Path("/server")
 public class Server {
     
    Model model = new DefaultModel();
    Mapping map = model.getMapping();
    ArrayList<String> resourceList = new ArrayList<>();
    
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response repond() throws FileNotFoundException, IOException, JSONException {
         JSONObject data = null;
         JSONObject dataStruct = null;
         JSONObject dataConst = null;
         String chaine = "";
         String path = "./src/main/ressources/g5kMock.json";
         System.out.println("--------------------------------------------------------------------------BEGIN");
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             chaine = Charset.defaultCharset().decode(bb).toString();
             fc.close();
         } finally {
             stream.close();
         }
 
         try {
             data = new JSONObject(chaine);
             String keys[] = {"struct"};
             String keys2[] = {"const"};
             dataStruct = new JSONObject(data, keys);
             dataConst = new JSONObject(data, keys2);
             
         } catch (JSONException JSe) {
             System.out.println("pbs JSON file");
         }
 
         //Mapping of VMs and Nodes
         mapBuild(dataStruct.optJSONObject("struct"));
         String jsonResource = " \"resources\": [ ";
         for(String s : resourceList){
             jsonResource += "\"" + s + "\"";
             if(! s.equals(resourceList.get(resourceList.size() -1)))
                 jsonResource += ",";
         }
         jsonResource += "] ";
         data.put("resources", resourceList);
         
 
         
         Set<VM> vms = map.getAllVMs();
         Set<Node> nodes = map.getAllNodes();
         System.out.println(vms.size() + " VMs");
         System.out.println(nodes.size() + " Nodes");
         //Build constraints and writes satisfaction to structure JSON
         buildConstraints(vms, nodes, dataConst.optJSONObject("const"), dataStruct.optJSONObject("struct"));
         
         return Response.ok(data.toString()).build();
 
     }
     
     public JSONObject mapBuild(JSONObject jo) throws JSONException {
         JSONArray children = jo.optJSONArray("children");
         if(isServer(jo)) {
             
             Node node = model.newNode();
             map.addOnlineNode(node);
             
             String name = jo.optString("name");
             
             JSONObject resources = jo.optJSONObject("resources");
             ArrayList<Integer> capacity = new ArrayList<>();
             Iterator it = resources.keys();
             int nbResources = 0;
             ArrayList<String> rcName = new ArrayList();
             while(it.hasNext()) {
                 
                 String rname = (String)it.next();
                 if(resourceList.indexOf(rname) == -1)
                     resourceList.add(rname);
                 capacity.add(resources.optInt(rname));
                rcName.add(rname.toLowerCase());
                 nbResources++;
                 
             }
             
             Object[] rcNames = rcName.toArray();
             ShareableResource[] rc = new ShareableResource[nbResources];
             for(int j=0; j<nbResources; j++) {
                rc[j] = new ShareableResource((String)rcNames[j] + "_" + name, (int)capacity.get(j), 0);
             }
 
             VM vm;
             for(int i =0; i< children.length(); i++) {
                 
 
                 vm = model.newVM();
                 map.addRunningVM(vm, node);
                 children.optJSONObject(i).put("btrpID", vm.id());
                 resources = children.optJSONObject(i).optJSONObject("resources");
                 Iterator itChild = resources.keys();
                 int nbRc = 0;
                 while(itChild.hasNext() && (nbRc < nbResources)){
                     String childRName = (String)itChild.next();
                     rc[nbRc].setConsumption(vm, resources.optInt(childRName));
                     nbRc++;
                 }
             
             }
             jo.put("btrpID", node.id());
             
             for(int j=0; j<nbResources; j++) {
                 model.attach(rc[j]);
             }
  
         } else {
             if(! isVM(jo)) {
                 for(int i =0; i< children.length(); i++) {
                     mapBuild(children.optJSONObject(i));
                 }
             }
         }
 
         return jo;
     }
     
     public void buildConstraints(Set<VM> vms, Set<Node> nodes, JSONObject joConst, JSONObject struct) throws JSONException {
         
         JSONArray consts = joConst.optJSONArray("list");
         
         for(int i =0; i< consts.length(); i++) {
             JSONObject constr = consts.optJSONObject(i);
             switch(constr.optString("id")) {
                 
                 case "Ban": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Ban ban = new Ban(vmList, nodeList);
                     boolean satisfied = ban.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 }
                 case "Fence": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Fence fence = new Fence(vmList, nodeList);
                     boolean satisfied = fence.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Gather": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Gather gather = new Gather(vmList);
                     boolean satisfied = gather.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     //btrpConstraints.add(new Gather(vmList));
                     break;
                 }
                 case "Killed": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Killed killed = new Killed(vmList);
                     boolean satisfied = killed.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Lonely": {
                     
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Set<VM> set = new HashSet<VM>(vmList);
                     Lonely lonely = new Lonely(set);
                     boolean satisfied = lonely.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 }
                 case "Ready": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Ready ready = new Ready(vmList);
                     boolean satisfied = ready.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Root": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Root root = new Root(vmList);
                     boolean satisfied = root.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Running": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Running running = new Running(vmList);
                     boolean satisfied = running.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "SequentialVMTransitions": {
                     
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     SequentialVMTransitions svt = new SequentialVMTransitions((List)vmList);
                     boolean satisfied = svt.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 }
                 case "Sleeping": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Sleeping sleeping = new Sleeping(vmList);
                     boolean satisfied = sleeping.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Spread": {
                     
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Set<VM> set = new HashSet<VM>(vmList);
                     Spread spread = new Spread(set);
                     boolean satisfied = spread.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 }
                 case "CumulatedResourceCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
                     
                     Set<Node> set = new HashSet<Node>(nodeList);
                     CumulatedResourceCapacity crc = new CumulatedResourceCapacity(set, rc, amount);
                     boolean satisfied = crc.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "SingleResourceCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
                     
                     Set<Node> set = new HashSet<>(nodeList);
                     SingleResourceCapacity src = new SingleResourceCapacity(set, rc, amount);
                     boolean satisfied = src.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "CumulatedRunningCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     int amount = constr.optInt("amount");
                     
                     Set<Node> set = new HashSet<>(nodeList);
                     CumulatedRunningCapacity crc = new CumulatedRunningCapacity(set, amount);
                     boolean satisfied = crc.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "SingleRunningCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     int amount = constr.optInt("amount");
                     
                     Set<Node> set = new HashSet<>(nodeList);
                     SingleRunningCapacity src = new SingleRunningCapacity(set, amount);
                     boolean satisfied = src.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Offline": {
      
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Offline offline = new Offline(nodeList);
 
                     boolean satisfied = offline.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Online": {
      
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Online online = new Online(nodeList);
 
                     boolean satisfied = online.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Quarantine": {
      
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Quarantine quarantine = new Quarantine(nodeList);
 
                     boolean satisfied = quarantine.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Overbook": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     String rc = constr.optString("rcid");
                     double amount = constr.optDouble("amount");
                     
                     Set<Node> set = new HashSet<>(nodeList);
                     Overbook overbook = new Overbook(set, rc, amount);
                     boolean satisfied = overbook.isSatisfied(model);
                     for(Node n : nodeList)
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "Preserve": {
                     
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
                     Preserve preserve = new Preserve(vmList, rc, amount);
                     boolean satisfied = preserve.isSatisfied(model);
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 }
                 case "Among": {
                     
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Collection<Node>> nodeParts = getNodeParts(constr, nodes, struct);
                     Among among = new Among(vmList, nodeParts);
                     boolean satisfied = among.isSatisfied(model);
 
                     for(VM v : vmList)
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     for(Collection<Node> colN : nodeParts)
                         for(Node n : colN)
                             addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     break;
                 } 
                 case "Split": {
                     
                     Collection<Collection<VM>> vmParts = getVMParts(constr, vms, struct);
                     Split split = new Split(vmParts);
                     boolean satisfied = split.isSatisfied(model);
                     for(Collection<VM> colV : vmParts)
                         for(VM v : colV)
                             addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
 
                     break;
                 }
                 case "SplitAmong" : {
                     
                     Collection<Collection<VM>> vmParts = getVMParts(constr, vms, struct);
                     Collection<Collection<Node>> nodeParts = getNodeParts(constr, nodes, struct);
                     SplitAmong sa = new SplitAmong(vmParts, nodeParts);
                     boolean satisfied = sa.isSatisfied(model);
                     for(Collection<VM> colV : vmParts)
                         for(VM v : colV)
                             addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     for(Collection<Node> colN : nodeParts)
                         for(Node n : colN)
                             addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     
                     
                     break;
                 }
             }
             
         }
         
     }
     
     public Collection<VM> getVMList(JSONObject constr, Set<VM> vms, JSONObject struct) throws JSONException {
         JSONArray jVMs = constr.optJSONArray("VMs").optJSONObject(0).optJSONArray("VMs");
         Collection<VM> vmList = new ArrayList<VM>();
         for(int j =0; j< jVMs.length(); j++)
             vmList.add(getVM(vms, getBtrpVMID(struct, jVMs.optString(j))));
         return vmList;
     }
     
     public Collection<Node> getNodeList(JSONObject constr, Set<Node> nodes, JSONObject struct) throws JSONException {
         JSONArray jnodes = constr.optJSONArray("Nodes").optJSONObject(0).optJSONArray("Nodes");
         Collection<Node> nodeList = new ArrayList<Node>();
         for(int j =0; j< jnodes.length(); j++) 
             nodeList.add(getNode(nodes, getBtrpServerID(struct, jnodes.optString(j))));
         return nodeList;
     }
     
     public Collection<Collection<Node>> getNodeParts(JSONObject constr, Set<Node> nodes, JSONObject struct) throws JSONException {
         Collection<Collection<Node>> nodeList = new ArrayList<>();
         for(int i =0; i< constr.optJSONArray("Nodes").length(); i++) {
             JSONObject jnodes = constr.optJSONArray("Nodes").optJSONObject(i);
             JSONArray servers = jnodes.optJSONArray("Nodes");
             ArrayList<Node> groupNode = new ArrayList<Node>();
             for (int j = 0; j < servers.length(); j++) {
                 groupNode.add(getNode(nodes, getBtrpServerID(struct, servers.optString(j))));
             }
             nodeList.add(groupNode);
         }
 
         return nodeList;
     }
     
     public Collection<Collection<VM>> getVMParts(JSONObject constr, Set<VM> allVMs, JSONObject struct) throws JSONException {
         Collection<Collection<VM>> vmList = new ArrayList<>();
         for(int i =0; i< constr.optJSONArray("VMs").length(); i++) {
             JSONObject jvms = constr.optJSONArray("VMs").optJSONObject(i);
 
             JSONArray vms = jvms.optJSONArray("VMs");
             ArrayList<VM> groupVM = new ArrayList<VM>();
             for (int j = 0; j < vms.length(); j++) {
                 groupVM.add(getVM(allVMs, getBtrpVMID(struct, vms.optString(j))));
             }
             vmList.add(groupVM);
             
         }
             
 
         return vmList;
     }
     
     public void addConstraintToJSON(JSONObject jo, VM vm, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         int vmID = vm.id();
         int fatherID = map.getVMLocation(vm).id();
         JSONArray children = jo.optJSONArray("children");
         if(isServer(jo)) {
             //found the father node
             if(jo.optInt("btrpID") == fatherID) {
                 for(int i =0; i< children.length(); i++){
                     //found the VM
                     if(children.optJSONObject(i).optInt("btrpID") == vmID)
                         writeToJson(children.optJSONObject(i), constraintID, constraintName, satisfied);
                 }
             }
         } else {
             if(! isVM(jo)) {
                 for(int i =0; i< children.length(); i++){
                     //Recursively check sons
                     addConstraintToJSON(children.optJSONObject(i), vm, constraintID, constraintName, satisfied);
                 }
             }
         }
         
     }
     
     public void addConstraintToJSON(JSONObject jo, Node node, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         int nodeID = node.id();
         JSONArray children = jo.optJSONArray("children");
         if(isServer(jo)) {
             //found the node
             if(jo.optInt("btrpID") == nodeID) {
                 writeToJson(jo, constraintID, constraintName, satisfied);
             }
         } else {
             if(! isVM(jo)) {
                 for(int i =0; i< children.length(); i++){
                     //Recursively check sons
                     addConstraintToJSON(children.optJSONObject(i), node, constraintID, constraintName, satisfied);
                 }
             }
         }
         
     }
     
     public void writeToJson(JSONObject children, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         //Have already some constraints set
         if (children.has("Constraints")) {
             JSONArray constList = children.optJSONArray("Constraints");
             JSONObject c = new JSONObject();
             c.put("name", "" + constraintName);
             c.put("satisfied", satisfied);
             c.put("type", constraintID);
             constList.put(c);
 
             // This is the first Constraint to be added
         } else {
             
             JSONArray constList = new JSONArray();
             JSONObject c = new JSONObject();
             c.put("name", "" + constraintName);
             c.put("satisfied", satisfied);
             c.put("type", constraintID);
             constList.put(c);
             children.put("Constraints", constList);
 
         }
         
     }
     
     public VM getVM(Set<VM> vms, int id) {
         for(VM vm : vms) {
             if(vm.id() == id)
                 return vm;
         }
         return null;
     }
     
     public Node getNode(Set<Node> nodes, int id) {
         for(Node node : nodes) {
             if(node.id() == id)
                 return node;
         }
         return null;
     }
     
     public boolean isVM(JSONObject jo) {
         if(jo.has("children"))
             return false;
         return true;
     }
     
     //returns true if node is a server
     public boolean isServer(JSONObject jo) {
         if(! isVM(jo)) {
             JSONArray children = jo.optJSONArray("children");
             if(isVM(children.optJSONObject(0)))
                 return true;
         }
         return false;
     }
     
     public int getBtrpServerID(JSONObject jo, String uuid) throws JSONException {
         
         if(isServer(jo)) {
             if(jo.get("UUID").equals(uuid))
                 return jo.getInt("btrpID");
             
         }
         
         if (!isVM(jo)) {
             JSONArray children = jo.getJSONArray("children");
             for (int i = 0; i < children.length(); i++) {
                 int res = getBtrpServerID(children.optJSONObject(i), uuid);
                 if(res != -1)
                     return res;
             }
         }
         
         return -1;
     }
     
     public int getBtrpVMID(JSONObject jo, String uuid) throws JSONException {
         
         if(isVM(jo)) {
             if(jo.get("UUID").equals(uuid))
                 return jo.getInt("btrpID");
             
         } else {
         
             JSONArray children = jo.getJSONArray("children");
             for (int i = 0; i < children.length(); i++) {
                 int res = getBtrpVMID(children.optJSONObject(i), uuid);
                 if(res != -1)
                     return res;
             }
         }
         
         return -1;
     }
     
 }
 
 
 
