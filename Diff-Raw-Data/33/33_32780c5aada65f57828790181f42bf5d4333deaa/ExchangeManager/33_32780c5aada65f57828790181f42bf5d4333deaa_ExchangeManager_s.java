 package com.trendmicro.tme.grapheditor;
 
 import java.io.IOException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBException;
 
 import com.sun.jersey.api.view.Viewable;
 import com.trendmicro.codi.CODIException;
 import com.trendmicro.codi.ZNode;
 import com.trendmicro.mist.proto.ZooKeeperInfo;
 import com.trendmicro.mist.proto.ZooKeeperInfo.DropConfig;
 import com.trendmicro.tme.broker.BrokerAdmin;
 import com.trendmicro.tme.mfr.Exchange;
 import com.trendmicro.tme.mfr.ExchangeFarm;
 
 @Path("/exchange")
 public class ExchangeManager {
     
     public ExchangeModel getExchange(@PathParam("name") String name) {
         return new ExchangeModel(name);
     }
     
     @Path("/{name}")
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Viewable getPage(@PathParam("name") String name) throws CODIException, JAXBException, IOException {
         return new Viewable("/exchange/exchange.jsp", getExchange(name));
     }
     
     @Path("/{name}/drop")
     @PUT
     @Consumes(MediaType.TEXT_PLAIN)
    public void setDrop(@PathParam("name") String name, String policy) throws CODIException {
         Exchange ex = new Exchange(name);
         String path = "/tme2/global/drop_exchange/" + ex.getName();
         ZooKeeperInfo.DropConfig dropConfig = ZooKeeperInfo.DropConfig.newBuilder().setPolicy(policy.equals("newest") ? DropConfig.Policy.NEWEST: DropConfig.Policy.OLDEST).build();
         
         ZNode node = new ZNode(path);
         if(node.exists()) {
             node.setContent(dropConfig.toString().getBytes());
         }
         else {
             node.create(false, dropConfig.toString().getBytes());
         }
         
         String broker = ExchangeFarm.getCurrentExchangeHost(ex);
         if(broker != null) {
            if(dropConfig.getPolicy().equals(ZooKeeperInfo.DropConfig.Policy.NEWEST)) {
                new BrokerAdmin(broker).setExchangeAttrib(ex, "LimitBehavior", "REJECT_NEWEST");
             }
            else {
                new BrokerAdmin(broker).setExchangeAttrib(ex, "LimitBehavior", "REMOVE_OLDEST");
             }
         }
     }
     
     @Path("/{name}/drop")
     @DELETE
    public void setBlock(@PathParam("name") String name) throws CODIException {
         Exchange ex = new Exchange(name);
         String path = "/tme2/global/drop_exchange/" + ex.getName();
         
         new ZNode(path).delete();
         String broker = ExchangeFarm.getCurrentExchangeHost(ex);
         if(broker != null) {
            new BrokerAdmin(broker).setExchangeAttrib(ex, "LimitBehavior", "FLOW_CONTROL");
         }
     }
 }
