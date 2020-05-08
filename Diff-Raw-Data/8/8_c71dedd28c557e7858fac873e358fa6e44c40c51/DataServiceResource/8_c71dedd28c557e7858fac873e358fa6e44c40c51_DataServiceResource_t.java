 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package org.lafayette.server.webapp.api.resources;
 
 import freemarker.template.TemplateException;
 import org.lafayette.server.webapp.api.Converter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.lafayette.server.core.log.Log;
 import org.lafayette.server.core.log.Logger;
 import org.lafayette.server.web.http.MediaType;
 import org.lafayette.server.web.http.UriList;
 import org.lafayette.server.web.service.data.DataService;
 import org.lafayette.server.webapp.api.template.Template;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Path("/service/data")
 public class DataServiceResource extends BaseResource {
 
     private static final String INDEX_MARKDOWN = "api.service.data.index.md";
     private static final String INDEX_TITLE = "Data Services";
 
     /**
      * Logger facility.
      */
     private  final Logger log = Log.getLogger(DataServiceResource.class);
 
 
     @Override
     protected String getTitle() {
         return INDEX_TITLE;
     }
 
     @Override
     protected void addUrisToIndexList(final UriList indexUriList) throws URISyntaxException {
         log.debug("Add URIs to index list.");
         indexUriList.add(createRelativeUri("/{user}"));
     }
 
     /**
      * Reads the markdown template file, assign some values and renders it.
      *
      * @return the rendered markdown string
      * @throws TemplateException if template couldn't be parsed
      * @throws IOException if template file couldn't be read
      */
     @Override
     protected String indexAsMarkdown() throws TemplateException, IOException {
         final Template tpl = createTemplate(INDEX_MARKDOWN);
         assignBaseVariables(tpl);
         tpl.assignVariable("services", getIndexUriList().toArray());
         return tpl.render();
     }
 
     @GET
     @Path("/{user}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getAllResourcesOfUserAsJson(@PathParam("user") final String user) {
         final Collection ids = getDataService().getIds(user);
 
         if (null == ids) {
            return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
         }
 
         return Response.status(Status.OK).entity(ids).build();
     }
 
     @GET
     @Path("/{user}/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getDataAsJson(@PathParam("user") final String user, @PathParam("id") final String id) {
         final JSONObject datum = getData(user, id);
 
         if (null == datum) {
             return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
         }
 
         return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(datum).build();
     }
 
     @GET
     @Path("/{user}/{id}")
     @Produces(MediaType.APPLICATION_XML)
     public Response getDataAsXml(@PathParam("user") final String user, @PathParam("id") final String id) throws JSONException {
         final JSONObject datum = getData(user, id);
 
         if (null == datum) {
            return Response.status(Status.NOT_FOUND)
                    .entity("")
                    .type(MediaType.APPLICATION_XML)
                    .build();
         }
 
         return Response.status(Status.OK).type(MediaType.APPLICATION_XML).entity(Converter.xml(datum)).build();
     }
 
     @GET
     @Path("/{user}/{id}")
     @Produces(MediaType.APPLICATION_X_MSGPACK)
     public JSONObject getDataAsMessagePack(@PathParam("user") final String user, @PathParam("id") final String id) {
         return getData(user, id);
     }
 
     @PUT
     @Path("/{user}/{id}")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response putData(@PathParam("user") final String user, @PathParam("id") final String id, final JSONObject json) throws IOException, JSONException {
         final JSONObject datum = getDataService().putData(user, id, json);
         final Response.ResponseBuilder response = Response.status(Status.CREATED);
         final UriBuilder uri = uriInfo().getBaseUriBuilder();
         response.type(MediaType.APPLICATION_XML).header("Location", uri.build(user, id).toString());
 
         if (null != datum) {
             response.entity(Converter.mpack(datum));
         }
 
         return response.build();
     }
 
     @DELETE
     @Path("/{user}/{id}")
     public Response deleteData(@PathParam("user") final String user, @PathParam("id") final String id) {
         final JSONObject datum = getDataService().deleteData(user, id);
 
         if (null == datum) {
             return Response.status(Status.NOT_FOUND).build();
         }
 
         return Response.status(Status.ACCEPTED).build();
     }
 
     private JSONObject getData(final String user, final String id) {
         return getDataService().getData(user, id);
     }
 
     private DataService getDataService() {
         return services().getApiServices().getDataService();
     }
 }
