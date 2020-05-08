 package edu.sjsu.cmpe.library.api.resources;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response;
 
 import com.yammer.metrics.annotation.Timed;
 import edu.sjsu.cmpe.library.domain.JsonBook;
 import edu.sjsu.cmpe.library.domain.Book;
 import edu.sjsu.cmpe.library.domain.Author;
 import edu.sjsu.cmpe.library.dto.BookDto;
 import edu.sjsu.cmpe.library.dto.LinkDto;
 import edu.sjsu.cmpe.library.dto.LinksDto;
 import edu.sjsu.cmpe.library.domain.Library;
 
 import java.util.List;
 
 @Path("/v1/books")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class BookResource {
 	protected Library library;
     public BookResource(Library library) {
     	this.library = library;
     	}
     @GET
     @Path("/{isbn}")
     @Timed(name = "view-book")
     public Response getBookByIsbn(@PathParam("isbn") int isbn) {
 	Book book = new Book();
     book = library.getBook(isbn);
     if(book == null)
     	return Response.status(404).entity("No Book found with the given ISBN number").build();
     BookDto bookResponse = new BookDto(book);
 	LinksDto authorlinks = new LinksDto();
 	int num_authors = library.getNumAuthors(isbn);
 	for(int i = 0; i < num_authors; i++)
 	{
 		int j = i+1;
 		authorlinks.addLink(new LinkDto("view-author","/books/" + book.getIsbn() + "/authors/" + j, "GET"));
 	}
 	if(authorlinks != null)
 		bookResponse.setAuthors(authorlinks);
 
 	LinksDto reviewlinks = new LinksDto();
 	int num_reviews = library.getNumReviews(isbn);
 	for(int i = 0; i < num_reviews; i++)
 	{
 		int j = i+1;	
 		reviewlinks.addLink(new LinkDto("view-review","/books/" + book.getIsbn() + "/reviews/" + j, "GET"));
 	}
 	if(reviewlinks != null)
 		bookResponse.setReviews(reviewlinks);
 	bookResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(),"GET"));
 	bookResponse.addLink(new LinkDto("update-book",	"/books/" + book.getIsbn(),"PUT"));
 	bookResponse.addLink(new LinkDto("delete-book","/books/" + book.getIsbn(),"DELETE"));
	bookResponse.addLink(new LinkDto("create-review","/books/" + book.getIsbn(),"POST"));
	bookResponse.addLink(new LinkDto("view-all-reviews","/books" + book.getIsbn(),"GET"));
 	return Response.status(200).entity(bookResponse).build();
     }
    
     @POST
     @Path("/")
     @Timed(name = "create-book")
     public Response createBook(JsonBook jsonbook) 
     {
     	if((jsonbook.getStatus().compareTo("available") == 0) || (jsonbook.getStatus().compareTo("lost") == 0) || (jsonbook.getStatus().compareTo("in-queue") == 0) ||(jsonbook.getStatus().compareTo("lost") == 0))
     	{
     		Book book = new Book();
     		book.setDate(jsonbook.getPublicationDate());
     		book.setLanguage(jsonbook.getLanguage());
     		book.setPages(jsonbook.getPages());
     		book.setStatus(jsonbook.getStatus());
     		book.setTitle(jsonbook.getTitle());
     		List<Author> list = jsonbook.getAuthors();
     		if(!library.addBook(book))
     				return Response.status(500).entity("Problem adding book. All ISBN numbers taken.").build();
     		if(!library.addAuthors(book.getIsbn(), list))
     			return Response.status(500).entity("Unable to add authors. Please try again later.").build();
     		LinksDto bookpostResponse = new LinksDto();
     		if(book.getIsbn() <= 0)
     			return Response.status(500).entity("Problem retrieving ISBN of book").build();
     		bookpostResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(),"GET"));
     		bookpostResponse.addLink(new LinkDto("update-book",	"/books/" + book.getIsbn(),"PUT"));
     		bookpostResponse.addLink(new LinkDto("delete-book","/books/" + book.getIsbn(),"DELETE"));
     		bookpostResponse.addLink(new LinkDto("create-review","/books/" + book.getIsbn() + "/reviews","POST"));
     		
     		return Response.status(200).entity(bookpostResponse).build();
     	}
     	else
     	{
     		String message = "Invalid Status Attribute for book.";
     		return Response.status(400).entity(message).build();
     	}
     }
     
     @DELETE
     @Path("/{isbn}")
     @Timed(name = "delete-book")
     public Response deleteBook(@PathParam("isbn") int isbn) 
     {
     if(!library.deleteBook(isbn))
     {
     	return Response.status(404).entity("Problem deleting book. Check ISBN number").build();
     }
     LinksDto bookdeleteResponse = new LinksDto();
     bookdeleteResponse.addLink(new LinkDto("create-book", "/books/","POST"));
     
 	return Response.status(200).entity(bookdeleteResponse).build();
     }
     
     @PUT
     @Path("/{isbn}")
     @Timed(name = "update-book")
     public Response updateBook(@PathParam("isbn") int isbn, @QueryParam("status") String status) 
     {
     	if((status.compareTo("available") == 0) || (status.compareTo("lost") == 0) || (status.compareTo("in-queue") == 0) ||(status.compareTo("lost") == 0))
     	{
     		Book book = library.getBook(isbn);
     		if(book == null)
     		{
     			return Response.status(404).entity("Book not found").build();
     		}
     		book.setStatus(status);
     		if(!library.updateBook(isbn, book))
     			return Response.status(404).entity("Unable to update status").build();
     		LinksDto bookupdateResponse = new LinksDto();
     		bookupdateResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(),"GET"));
     		bookupdateResponse.addLink(new LinkDto("update-book", "/books/" + book.getIsbn(),"PUT"));
     		bookupdateResponse.addLink(new LinkDto("delete-book","/books/" + book.getIsbn(),"DELETE"));
     		bookupdateResponse.addLink(new LinkDto("create-review","/books/" + book.getIsbn() + "/reviews","POST"));
     		
     		return Response.status(200).entity(bookupdateResponse).build();
     	}
     	else
     	{
     		String message = "Invalid Status Attribute for book.";
     		return Response.status(400).entity(message).build();
     	}
     }
 }
