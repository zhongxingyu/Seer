 package fr.epsi.i4.bookmark.web;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.OPTIONS;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import fr.epsi.i4.bookmark.Bookmark;
 import fr.epsi.i4.bookmark.BookmarkRepository;
 import fr.epsi.i4.bookmark.InvalidBookmarkException;
 
 public class BookmarkResource {
 	
 	private final BookmarkRepository bookmarkRepository;
 	private final String id;
 	
 	public BookmarkResource(BookmarkRepository bookmarkRepository, String id) {
 		this.bookmarkRepository = bookmarkRepository;
 		this.id = id;
 	}
 
 	@PUT
 	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	public void merge(Bookmark bookmark) {
		if (bookmark == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

 		try {
 			bookmarkRepository.add(id, bookmark);
 		} catch (InvalidBookmarkException e) {
 			Response response = Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(e.getMessage()).build();
 			throw new WebApplicationException(response);
 		}
 	}
 
 	@GET
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	public BookmarkResponse get(@Context UriInfo uriInfo) {
 		return new BookmarkResponse(getBookmark(), uriInfo.getRequestUriBuilder());
 	}
 
 	@DELETE
 	public void delete() {
 		bookmarkRepository.delete(id);
 	}
 
 	@GET
 	@Path("qrcode")
 	@Produces("image/png")
 	public String getQrCode() {
 		return getBookmark().getUrl();
 	}
 
 	@OPTIONS
 	@Path("qrcode")
 	public Response getQrCodeHead() {
 		return Response.ok().header("Allow", "GET,OPTIONS,HEAD").build();
 	}
 	
 	public Bookmark getBookmark() {
 		Bookmark bookmark = bookmarkRepository.get(id);
 		if (bookmark == null) {
 			throw new WebApplicationException(Status.NOT_FOUND);
 		}
 		return bookmark;
 	}
 }
