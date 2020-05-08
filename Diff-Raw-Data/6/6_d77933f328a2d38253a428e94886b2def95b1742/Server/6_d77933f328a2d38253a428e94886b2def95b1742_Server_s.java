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
 import java.util.Set;
 
 @Path("/server")
 public class Server {
     
    Model model = new DefaultModel();
    Mapping map = model.getMapping();
    
    
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response repondre() throws FileNotFoundException, IOException, JSONException {
         JSONObject data = null;
         JSONObject dataStruct = null;
         JSONObject dataConst = null;
         String chaine = "";
         String path = "./src/main/ressources/g5kMock.json";
 
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
 
         
         /*
         
         Node node = model.newNode();
         Node node2 = model.newNode();
         VM vm = model.newVM();
         VM vm2 = model.newVM();
         
         map.addOnlineNode(node);
         map.addOnlineNode(node2);
         map.addRunningVM(vm, node);
         map.addRunningVM(vm2, node2);
         
         ArrayList<VM> vms = new ArrayList<VM>();
         vms.add(vm);
         vms.add(vm2);
         
         Gather gather = new Gather(vms);
         
         System.out.println(gather.isSatisfied(model));
         */
         mapBuild(dataStruct.optJSONObject("struct"));
         Set<VM> vms = map.getAllVMs();
         Set<Node> nodes = map.getAllNodes();
         
        
        //System.out.println(model.getViews());
         
         return Response.ok(data.toString()).build();
 
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
     
     public JSONObject mapBuild(JSONObject jo) throws JSONException {
         JSONArray children = jo.optJSONArray("children");
         if(isServer(jo)) {
             
             Node node = model.newNode();
             map.addOnlineNode(node);
             String name = jo.optString("name");
             int cpuCAP = jo.optInt("CPU");
             int memCAP = jo.optInt("RAM");
             int diskCAP = jo.optInt("DiskSpace");
             
             ShareableResource rcCPU = new ShareableResource("cpu_" + name, cpuCAP, 0);
             ShareableResource rcMEM = new ShareableResource("mem_" + name, memCAP, 0);
             ShareableResource rcDS = new ShareableResource("disk_" + name, diskCAP, 0);
             
             
             VM vm;
             for(int i =0; i< children.length(); i++) {
                 vm = model.newVM();
                 map.addRunningVM(vm, node);
                 children.optJSONObject(i).put("btrpID", vm.id());
                 rcCPU.setConsumption(vm, children.optJSONObject(i).optInt("CPU"));
                 rcMEM.setConsumption(vm, children.optJSONObject(i).optInt("RAM"));
                 rcDS.setConsumption(vm, children.optJSONObject(i).optInt("DiskSpace"));
             }
             jo.put("btrpID", node.id());
             //System.out.println(rcCPU);
             model.attach(rcCPU);
             model.attach(rcMEM);
             model.attach(rcDS);
         } else {
             if(! isVM(jo)) {
                 for(int i =0; i< children.length(); i++) {
                     mapBuild(children.optJSONObject(i));
                 }
             }
         }
 
         return jo;
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
 
 
 
