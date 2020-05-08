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
 package org.lafayette.server.resources;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.lafayette.server.http.MediaType;
 import org.lafayette.server.http.UriList;
 import org.pegdown.PegDownProcessor;
 
 /**
  * Serves the service resource.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Path("/service")
 public class ServiceResource extends BaseResource {
 
    private static final String MARKDOWN_INDEX = "/org/lafayette/server/resources/service.index.md";
    private static final String HTML_INDEX = "/org/lafayette/server/resources/service.index.html";
 
     @Override
     protected void addUrisToIndexList(final UriList indexUriList) throws URISyntaxException {
         final UriBuilder ub = uriInfo().getAbsolutePathBuilder();
         ub.path("/data");
         indexUriList.add(ub.build());
     }
 
     @GET
     @Produces(MediaType.TEXT_PLAIN)
     public Response indexAsPlainText() throws IOException, URISyntaxException {
         final String markdown = readFile(MARKDOWN_INDEX);
         return Response.status(Response.Status.OK)
                 .type(MediaType.TEXT_PLAIN)
                 .entity(markdown)
                 .build();
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Response indexAsHtml() throws URISyntaxException, IOException {
         // TODO Use Freemarker
         final PegDownProcessor processor = new PegDownProcessor();
         final String markdown = readFile(MARKDOWN_INDEX);
         final String content = processor.markdownToHtml(markdown);
         final String html = readFile(HTML_INDEX);
         return Response.status(Response.Status.OK)
                 .type(MediaType.TEXT_HTML)
                 .entity(html.replaceAll("--CONTENT--", content))
                 .build();
     }
 
     private String readFile(final String fileName) throws IOException, URISyntaxException {
         final URL resource = getClass().getResource(fileName);
         return FileUtils.readFileToString(new File(resource.toURI()), "UTF-8");
     }
 }
