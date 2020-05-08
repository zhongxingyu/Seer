 package org.restdoc.server.impl;
 
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response.Status;
 
 import org.restdoc.api.MethodDefinition;
 import org.restdoc.api.ResponseDefinition;
 import org.restdoc.api.RestResource;
 import org.restdoc.api.Schema;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * @author thoeger
  * 
  */
 @Path("/api/strings")
 public class MyCrudBean implements IProvideRestDoc {
 
 	/**
 	 * @param id
 	 * @return the message
 	 */
 	@GET
 	@Produces("text/plain")
 	@Consumes("text/plain")
 	public List<String> getList() {
 		return Lists.newArrayList();
 	}
 
 	/**
 	 * @param id
 	 * @return the message
 	 */
 	@Path("/{id}")
 	@GET
 	@Produces("text/plain")
 	@Consumes("text/plain")
 	public String getSingleItem(@PathParam("id") String id) {
 		throw new WebApplicationException(Status.NOT_FOUND);
 	}
 
 	/**
 	 * @param id
 	 * @param content
 	 * @return the created message
 	 */
 	@Path("/{id}")
 	@POST
 	@Produces("text/plain")
 	@Consumes("text/plain")
 	public String updateItem(@PathParam("id") String id, String content) {
 		return content;
 	}
 
 	/**
 	 * @param msg
 	 *            the message
 	 * @return the created message
 	 */
 	@POST
 	@Produces("text/plain")
 	@Consumes("text/plain")
 	public String createItem(String msg) {
 		return msg;
 	}
 
 	/**
 	 * @param id
 	 */
 	@DELETE
 	public void deleteItem(@PathParam("id") String id) {
 		// delete object
 	}
 
 	@Override
 	public RestResource[] getRestDocResources() {
		final RestResource rootRes = new RestResource();
		rootRes.id("getItemlist").description("list of items").path("/api/strings");

 		rootRes.method("GET", new MethodDefinition().description("get list of items").statusCode("200", "OK"));
 
 		final MethodDefinition createItem = new MethodDefinition().description("create new item").statusCode("201", "Created");
 		createItem.response(new ResponseDefinition().header("Location", "URI of the created resource", true));
 		rootRes.method("POST", createItem);
 
 		final RestResource idRes = new RestResource().id("getItem").description("single items").path("/api/strings/{id}");
 		idRes.param("id", "the item id");
 
 		idRes.method("GET", new MethodDefinition().description("get item").statusCode("200", "OK"));
 		idRes.method("POST", new MethodDefinition().description("update item").statusCode("200", "OK"));
 		idRes.method("DELETE", new MethodDefinition().description("delete item").statusCode("200", "OK"));
 
 		return new RestResource[] { rootRes, idRes };
 	}
 
 	@Override
 	public HashMap<String, Schema> getRestDocSchemas() {
 		return Maps.newHashMap();
 	}
 
 }
