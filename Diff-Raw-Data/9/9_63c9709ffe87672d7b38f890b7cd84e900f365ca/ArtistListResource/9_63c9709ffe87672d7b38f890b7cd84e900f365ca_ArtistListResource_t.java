 package dsa.colourmylife.rest;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import dsa.colourmylife.rest.model.Artist;
 import dsa.colourmylife.rest.util.APIErrorBuilder;
 import dsa.colourmylife.rest.util.DataSourceSAP;
 
 @Path("/artists")
 public class ArtistListResource {
 	// Recurso ArtistList: ./artists
 	// GET → Lista artistas.
 	// POST → Añadir artistas.
 	// id int(11) NOT NULL AUTO_INCREMENT,
 	// name varchar(50) NOT NULL,
 	// idgenre1 int(11) NOT NULL,
 	// idgenre2 int(11) NULL,
 	// info varchar(150),
 
 	@Context
 	private UriInfo uri;
 
 	@Context
 	protected HttpServletRequest request;
 
 	@Context
 	private SecurityContext security;
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Artist> getArtistListJSON() {
 		return getArtistList();
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response createArtistJSON(Artist artist) {
 		insertArtist(artist);
 		Response response = null;
 		try {
 			response = Response
 					.status(204)
 					.location(
							new URI("/artist"
 									+ artist.getName())).build();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 		return response;
 	}
 
 	private void insertArtist(Artist artist) {
 		if (!security.isUserInRole("admin")) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.FORBIDDEN)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.FORBIDDEN.getStatusCode(),
 							"FORBIDDEN", request)).build());
 		}
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 
 		try {
 			if (artist.getName() == null || artist.getGenre() == null) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.BAD_REQUEST)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.BAD_REQUEST.getStatusCode(),
 								"The Artist and Genre camps mustn't be empty",
 								request)).build());
 
 			}
 
 			if (getArtist(artist.getName()) != null) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.CONFLICT)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.CONFLICT.getStatusCode(),
 								"Artist already exists", request)).build());
 			}
 
 			connection.setAutoCommit(false);
 			// Insertamos artista en la BD
 			try {
 				Statement stmt = connection.createStatement();
 				// INSERT INTO artist VALUES (NULL, "Florence", 4, NULL,
 				// "Grupo imprescindible");
 				StringBuilder sb = new StringBuilder(
 						"INSERT INTO artist VALUES (NULL, '" + artist.getName()
 								+ "', " + artist.getGenreId() + ", "
 								+ artist.getGenre2Id() + ", '"
 								+ artist.getInfo() + "');");
 				System.out.println(sb);
 				int rs = stmt.executeUpdate(sb.toString());
 				if (rs == 0)
 					throw new WebApplicationException(Response
 							.status(Response.Status.NOT_FOUND)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.NOT_FOUND.getStatusCode(),
 									"Artist not found.", request)).build());
 				stmt.close();
 			} catch (SQLException e) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.INTERNAL_SERVER_ERROR)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.INTERNAL_SERVER_ERROR
 										.getStatusCode(),
 								"Error accessing to database.", request))
 						.build());
 			}
 			connection.commit();
 		} catch (SQLException e) {
 			try {
 				connection.rollback();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		} finally {
 			try {
 				connection.setAutoCommit(true);
 				connection.close();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 
 	private List<Artist> getArtistList() {
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM artist;");
 			List<Artist> artistList = new ArrayList<>();
 
 			while (rs.next()) {
 				Artist artist = new Artist();
 				artist.setArtistid(rs.getInt("id"));
 				artist.setName(rs.getString("name"));
 				artist.setGenreId(rs.getInt("idgenre1"));
 				artist.setGenre2Id(rs.getInt("idgenre2"));
 				artist.setInfo(rs.getString("info"));
 				// TODO OPTIONAL: Convert genreId into a genre, podría ser otro
 				// stmt
 				// artist.setGenre("genre");
 				// artist.setGenre2("genre2");
 				artistList.add(artist);
 			}
 			stmt.close();
 			connection.close();
 			return artistList;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 
 	public Artist getArtist(String name) {
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 
 		try {
 			Statement stmt = connection.createStatement();
 			// SELECT * FROM artist WHERE name='Florence';
 			ResultSet rs = stmt
 					.executeQuery("SELECT * FROM artist WHERE name = '" + name
 							+ "';");
 
 			if (!rs.next()) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"Artist not found.", request)).build());
 			}
 
 			Artist artist = new Artist();
 			artist.setArtistid(rs.getInt("id"));
 			artist.setName(rs.getString("name"));
 			artist.setGenreId(rs.getInt("idgenre1"));
 			artist.setGenre2Id(rs.getInt("idgenre2"));
 			// TODO OPTIONAL: Convert genreId into a genre, podría ser otro stmt
 			// artist.setGenre("genre");
 			// artist.setGenre2("genre2");
 			stmt.close();
 			connection.close();
 			return artist;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 }
