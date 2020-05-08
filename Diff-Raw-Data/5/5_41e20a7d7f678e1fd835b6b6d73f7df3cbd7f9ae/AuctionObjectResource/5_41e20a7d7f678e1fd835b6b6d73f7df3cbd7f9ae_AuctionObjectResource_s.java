 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.src;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 /**
  *
  * @author Alexander Lissenko
  */
 
 @Path("auction/{auctionId}")    //eventuellt annan path, unik för varje auctionobject
 public class AuctionObjectResource {
     
     private final static SuperSite site = SuperSite.getInstance();   //funkar när SuperSite är @Singleton
     
     AuctionObjectProxy objectP;
     private UriInfo uriInfo;
     
    @PUT
     @Path("??") //TODO
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response setBid(@FormParam("newBid") Double newBid ) {
         //kopplas ihop med att kolla att budet är valid
    }
     
     @GET
     @Path("title")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getTitle() {
         String t = objectP.getTitle(); //byts till site.något.getTitle
         PrimitiveJSONWrapper<String> wrapTitle = new PrimitiveJSONWrapper<String>(t);
         return Response.ok(wrapTitle).build();
     }
     
     @GET
     @Path("info")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getInfo() {
         String t = objectP.getInfo(); //byts till site.något.getInfo
         PrimitiveJSONWrapper<String> wrapInfo = new PrimitiveJSONWrapper<String>(t);
         return Response.ok(wrapInfo).build();
     }
     
     @GET
     @Path("price")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getPrice() {
         Double d = objectP.getPrice(); //byts till site.något.getPrice
         PrimitiveJSONWrapper<Double> wrapPrice = new PrimitiveJSONWrapper<Double>(d);
         return Response.ok(wrapPrice).build();
     }
     
     
     //kan behöva wrappas på annat sätt, osäker på om Date är primitiv typ
     @GET
     @Path("expire")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getExpire() {
         Date d = objectP.getExpire(); //byts till site.något.getExpire
         PrimitiveJSONWrapper<Date> wrapExp = new PrimitiveJSONWrapper<Date>(d);
         return Response.ok(wrapExp).build();
     }
     
     @GET
     @Path("id")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getId() {
         Long i = objectP.getId(); //byts till site.något.getId
         PrimitiveJSONWrapper<Long> wrapId = new PrimitiveJSONWrapper<Long>(i);
         return Response.ok(wrapId).build();
     }
     
     
     //osäker på om detta funkar, kanske inte primitive
     @GET
     @Path("??") //TODO
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getBidder() {
         List<HashMap> list = objectP.getBidder(); //byts till site.något.getBidder
         GenericEntity<List<HashMap>> ge = new GenericEntity<List<HashMap>>(list){};
         return Response.ok(ge).build();
     }
     
     @GET
     @Path("??") //TODO
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getFlagList() {
         List<Customer> cList = objectP.getFlagList(); //byts till site.något.getFlagList
         GenericEntity<List<Customer>> gc = new GenericEntity<List<Customer>>(cList){};
         return Response.ok(gc).build();
     }
 }
