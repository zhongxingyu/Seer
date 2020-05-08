 package com.trifork.hr.resource;
 
 import com.trifork.hr.model.Employee;
 import com.trifork.hr.persistence.EmployeeRepository;
 import com.trifork.hr.representation.EmployeeListRepresentation;
 import com.trifork.hr.representation.EmployeeRepresentation;
 import com.trifork.hr.representation.Link;
 import org.apache.log4j.Logger;
 
import javax.persistence.EntityNotFoundException;
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import java.net.URI;
 
 @Path("/employee")
 public class EmployeeResource {
     private static final Logger logger = Logger.getLogger(EmployeeResource.class);
     private EmployeeRepository repository = new EmployeeRepository();
     @Context
     UriInfo uriInfo;
 
     @GET
     public EmployeeListRepresentation listEmployees() {
         logger.debug("listEmployees");
         return new EmployeeListRepresentation(repository.listAll());
     }
 
     @POST
     public Response createEmployee(Employee employee) {
         logger.debug("Create " + employee);
 
         Employee newEmpl = new Employee(employee.getName());
         final Employee createdEmpl = repository.save(newEmpl);
 
         final URI createdUri = uriInfo.getRequestUriBuilder().path(this.getClass(), "getEmployee").build(createdEmpl.getId());
 
         return Response.created(createdUri).build();
     }
 
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getEmployee(@PathParam("id") long id) {
         logger.debug("get id=" + id);
         Employee employee;
         try {
             employee = repository.get(id);
        } catch (EntityNotFoundException e) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         Link selfLink = new Link(Link.REL_SELF, uriInfo.getRequestUri());
 
         EmployeeRepresentation employeeRepr = new EmployeeRepresentation(employee, selfLink);
 
         CacheControl caching = new CacheControl();
         caching.setPrivate(true);
         caching.setMaxAge(240);
         
         return Response.ok().cacheControl(caching).entity(employeeRepr).build();
     }
 
     @DELETE
     @Path("{id}")
     public void deleteEmployee(@PathParam("id") long id) {
         repository.remove(id);
     }
 
     @PUT
     @Path("{id}")
     public Response updateEmployee(@PathParam("id") long id, Employee toUpdate) {
         if (toUpdate.getId() != id) {
             Response.status(Response.Status.BAD_REQUEST).entity("Will not change employee at URI " + uriInfo.getRequestUri() + " to have id " + toUpdate.getId());
         }
         repository.save(toUpdate);
 
         return Response.noContent().build();
     }
 
 }
