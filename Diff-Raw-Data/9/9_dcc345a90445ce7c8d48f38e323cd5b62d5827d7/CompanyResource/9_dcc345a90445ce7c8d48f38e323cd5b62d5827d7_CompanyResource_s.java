 package no.sb1.lpt.resources;
 
 import no.sb1.lpt.model.Company;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
import no.sb1.lpt.repository.DataStore;

 import java.util.Collection;
 
 import static no.sb1.lpt.Util.JSON_CONTENT_TYPE;
 import static no.sb1.lpt.repository.DataStore.companies;
 import static no.sb1.lpt.repository.DataStore.company;
 
 @Path("/companies")
 @Produces(JSON_CONTENT_TYPE)
 public class CompanyResource {
 	
     @GET
     @Produces(JSON_CONTENT_TYPE)
     public Collection<Company> getCompanies(){
         return companies.values();
     }
 
     @GET
     @Produces(JSON_CONTENT_TYPE)
     @Path("/{companyId}")
     public Company getCompany(@PathParam("companyId") int companyId){
         return company(companyId);
     }
         
 }
