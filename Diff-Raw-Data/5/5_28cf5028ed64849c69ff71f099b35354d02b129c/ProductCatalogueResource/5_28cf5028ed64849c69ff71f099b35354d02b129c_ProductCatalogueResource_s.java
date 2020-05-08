 
 package dit126.group4.group4shop_app.client;
 
 import dit126.group4.group4shop.core.Product;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 
 @Path ("/products")
 public class ProductCatalogueResource {
    /*
     @GET
     @Path("all")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getAll(){
         List<Product> allP = Group4Shop.INSTANCE.getProductCatalogue().getRange(0,
                 Group4Shop.INSTANCE.getProductCatalogue().getCount());
         List<ProductProxy> ppList = new ArrayList<ProductProxy>();
         for(Product p : allP){
             ppList.add(new ProductProxy(p));
         }
         GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
         return Response.ok(ge).build();
     }
     
     @GET
     @Path("{id}")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response find(@PathParam("id") Long id){
         Product found = Group4Shop.INSTANCE.getProductCatalogue().find(id);
         ProductProxy pp = new ProductProxy(found);
         return Response.ok(pp).build();
     }
     
     
     @GET
     //@Path("name")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getByName(@QueryParam("name") String name) {
         try{
             List<Product> rtn = Group4Shop.INSTANCE.getProductCatalogue().getByName(name);
             List<ProductProxy> ppList = new ArrayList<ProductProxy>();
             for(Product p : rtn){
                 ppList.add(new ProductProxy(p));
             }
             GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
             return Response.ok(ge).build();
         } catch(Exception e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
     
     @GET
     //@Path("name")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getByCategory(@QueryParam("category") String category) {
         try{
             List<Product> rtn = Group4Shop.INSTANCE.getProductCatalogue().getByCategory(category);
             List<ProductProxy> ppList = new ArrayList<ProductProxy>();
             for(Product p : rtn){
                 ppList.add(new ProductProxy(p));
             }
             GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
             return Response.ok(ge).build();
         } catch(Exception e) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
     
     
     
     @GET
     @Path("range")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getRange(@QueryParam("first")int first, @QueryParam("nItems") int nItems) {
         List<Product> productRange = Group4Shop.INSTANCE.getProductCatalogue().getRange(first, nItems);
         List<ProductProxy> ppList = new ArrayList<ProductProxy>();
         for(Product product : productRange){
             ppList.add(new ProductProxy(product));
         }
         GenericEntity<List<ProductProxy>> ge = new GenericEntity<List<ProductProxy>>(ppList){};
         return Response.ok(ge).build();
     }
     
     
     @GET
     @Path("count")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response getCount() {
         try{
             Integer rtn = Group4Shop.INSTANCE.getProductCatalogue().getCount();
             PrimitiveJSONWrapper pjw = new PrimitiveJSONWrapper(rtn);
             return Response.ok(pjw).build();
         } catch (Exception e){
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
     
    */
 }
