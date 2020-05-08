 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.demo.roa;
 
 import com.smartitengineering.demo.roa.domains.Author;
 import com.smartitengineering.demo.roa.domains.Book;
 import com.smartitengineering.demo.roa.services.AuthorNotFoundException;
 import com.smartitengineering.demo.roa.services.Services;
 import java.util.Collection;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriBuilderException;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Link;
 
 /**
  *
  * @author imyousuf
  */
 @Path("/books/isbn/{isbn}")
 public class BookResource extends AbstractResource {
 
   static final UriBuilder BOOK_URI_BUILDER = UriBuilder.fromResource(BookResource.class);
   static final UriBuilder BOOK_CONTENT_URI_BUILDER;
 
   static {
     BOOK_CONTENT_URI_BUILDER = BOOK_URI_BUILDER.clone();
     try {
       BOOK_CONTENT_URI_BUILDER.path(BookResource.class.getMethod("getBook"));
     }
     catch (Exception ex) {
       throw new InstantiationError();
     }
   }
   private Book book;
 
   public BookResource(@PathParam("isbn") String isbn) {
     book = Services.getInstance().getBookService().getByIsbn(isbn);
   }
 
   @GET
   @Produces(MediaType.APPLICATION_ATOM_XML)
   public Response get() {
     Feed bookFeed = getBookFeed();
     ResponseBuilder responseBuilder = Response.ok(bookFeed);
     return responseBuilder.build();
   }
 
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/content")
   public Response getBook() {
     ResponseBuilder responseBuilder = Response.ok(book);
     return responseBuilder.build();
   }
 
   @PUT
   @Produces(MediaType.APPLICATION_ATOM_XML)
   @Consumes(MediaType.APPLICATION_JSON)
   public Response update(Book newBook) {
     ResponseBuilder responseBuilder = Response.status(Status.SERVICE_UNAVAILABLE);
     try {
       Services.getInstance().getAuthorService().populateAuthor(newBook);
      Services.getInstance().getBookService().update(newBook);
       book = Services.getInstance().getBookService().getByIsbn(newBook.getIsbn());
       responseBuilder = Response.ok(getBookFeed());
     }
     catch (AuthorNotFoundException ex) {
       responseBuilder = Response.status(Status.BAD_REQUEST);
     }
     catch (Exception ex) {
       responseBuilder = Response.status(Status.INTERNAL_SERVER_ERROR);
     }
     return responseBuilder.build();
   }
 
   @POST
   @Produces(MediaType.APPLICATION_ATOM_XML)
   @Consumes(MediaType.APPLICATION_JSON)
   public Response ammend(Book newBook) {
     return update(newBook);
   }
 
   @DELETE
   public Response delete() {
     Services.getInstance().getBookService().delete(book);
     ResponseBuilder responseBuilder = Response.ok();
     return responseBuilder.build();
   }
 
   private Feed getBookFeed() throws UriBuilderException, IllegalArgumentException {
     Feed bookFeed = getFeed(book.getName(), book.getLastModifiedDate());
     Link editLink = abderaFactory.newLink();
     editLink.setHref(uriInfo.getRequestUri().toString());
     editLink.setRel(Link.REL_EDIT);
     editLink.setMimeType(MediaType.APPLICATION_JSON);
     Link altLink = abderaFactory.newLink();
     altLink.setHref(BOOK_CONTENT_URI_BUILDER.clone().build(book.getIsbn()).toString());
     altLink.setRel(Link.REL_ALTERNATE);
     altLink.setMimeType(MediaType.APPLICATION_JSON);
     bookFeed.addLink(altLink);
     Collection<Author> authors = book.getAuthors();
     for (Author author : authors) {
       Entry entry = abderaFactory.newEntry();
       entry.setId(author.getNationalId());
       entry.setTitle(author.getName());
       entry.setSummary(author.getName());
       entry.setUpdated(author.getLastModifiedDate());
       Link authAltLink = abderaFactory.newLink();
       authAltLink.setRel(Link.REL_ALTERNATE);
       authAltLink.setMimeType(MediaType.APPLICATION_ATOM_XML);
       authAltLink.setHref(AuthorResource.AUTHOR_URI_BUILDER.clone().build(author.getId()).toString());
       entry.addLink(authAltLink);
       bookFeed.addEntry(entry);
     }
     return bookFeed;
   }
 }
