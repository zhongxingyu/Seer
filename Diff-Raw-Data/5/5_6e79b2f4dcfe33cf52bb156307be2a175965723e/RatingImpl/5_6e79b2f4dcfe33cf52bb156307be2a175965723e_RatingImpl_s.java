 package at.ac.tuwien.infosys.aicc11.services;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
 
 import at.ac.tuwien.infosys.aicc11.Ratings;
 import at.ac.tuwien.infosys.aicc11.legacy.LegacyCustomerRelationsManagement;
 import at.ac.tuwien.infosys.aicc11.legacy.LegacyException;
 
 public class RatingImpl implements Rating 
 {
     LegacyCustomerRelationsManagement backend = LegacyCustomerRelationsManagement.instance();
 	
 	public Ratings getRating(long customerId) 
 	{
 	    try {
 		return backend.getRating(customerId);
 	    }
 	    catch (LegacyException e) {
 		System.out.println(e);
 		ResponseBuilderImpl builder = new ResponseBuilderImpl();
		builder.status(Response.Status.NOT_FOUND);
		builder.entity("The requested customer id does not exist.");
 		Response resp = builder.build();
 		throw new WebApplicationException(resp);
 	    }
 	}
 }
