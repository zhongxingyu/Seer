 package uk.frequency.glance.server.service;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import uk.frequency.glance.server.business.GenericBL;
 import uk.frequency.glance.server.model.GenericEntity;
 import uk.frequency.glance.server.transfer.GenericDTO;
 
 
 /**
  * @author Victor Basso
  * TODO inject the DALs through spring
  */
 public abstract class GenericSL<T extends GenericEntity, U extends GenericDTO> {
 
 	GenericBL<T> business;
 	
 	@Context UriInfo uriInfo;
 	@Context Request request;
 
 	public GenericSL(GenericBL<T> dao) {
 		this.business = dao;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<U> findAll(){
 		List<T> list = business.findAll();
 		return toDTO(list);
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public U findById(@PathParam("id") long id) {
 		T entity = business.findById(id);
 		return toDTO(entity);
 	}
 	
	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	public Response create(U dto){
 		T entity = fromDTO(dto);
 		business.create(entity);
 		URI uri = uriInfo.getBaseUriBuilder().path(""+entity.getId()).build(); //FIXME missing the enitity type in path
 		return Response.created(uri).build();
 	}
 	
 	protected List<U> toDTO(List<? extends T> list){
 		List<U> dto = new ArrayList<U>();
 		for(T entity : list){
 			dto.add(toDTO(entity));
 		}
 		return dto;
 	}
 	
 	protected abstract U toDTO(T entity);
 	
 	protected abstract T fromDTO(U dto);
 	
 }
