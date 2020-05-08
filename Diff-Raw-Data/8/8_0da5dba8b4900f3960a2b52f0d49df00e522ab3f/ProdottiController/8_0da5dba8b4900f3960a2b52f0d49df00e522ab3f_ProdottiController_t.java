 package it.aip.controller;
 
 import it.aip.models.*;
 import it.aip.service.BioProducerService;
 import it.aip.service.OrganicProductService;
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProdottiController extends Controller {
     
     private OrganicProductService organicProductService = new OrganicProductService();
     private BioProducerService bioProducerService = new BioProducerService();
     
     @Override
     public Navigation run() throws Exception {
        
         List<OrganicProduct> products = new ArrayList<OrganicProduct>();
         
         String categoria = request.getParameter("categoria");
         if(categoria != null){
             if(categoria.equals("all")){
                 products = organicProductService.getAllOrganicProducts();
             }
             else {
                 products = organicProductService.getProductByCategory(categoria);
             }
             requestScope("prodotti", products);
             requestScope("categoria", categoria);
             return forward("prodottiByCategory.jsp");
         }
         
         String produttore = request.getParameter("fromProducer");
         if(produttore != null){
             BioProducer producer = bioProducerService.getProducer(produttore);
             products = producer.getProductsListRef().getModelList();
 
             requestScope("prodotti", products);
             requestScope("produttore", producer);
             return forward("prodottiByProducer.jsp");
         }                
         
         List<String> categories = organicProductService.getAllCategories();
         requestScope("categorie", categories);
         
         return forward("prodotti.jsp");
     }
 }
