 package dsa.colourmylife.rest;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import dsa.colourmylife.rest.model.Artist;
 import dsa.colourmylife.rest.model.Event;
 import dsa.colourmylife.rest.util.APIErrorBuilder;
 import dsa.colourmylife.rest.util.DataSourceSAP;
 
 @Path("/users/{username}/events")
 public class UserEventListResource {
 	@Context
 	private UriInfo uri;
 	@Context
 	protected HttpServletRequest request;
 	@Context
 	private SecurityContext security;
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Event> getEventListJSON(@PathParam("username") String username,
 			@QueryParam("kind") String kind) {
 		// All of this are Not NULL
 		return getEventList(username, kind);
 	}
 
 	private List<Event> getEventList(String username, String kind) {
 		if (security.isUserInRole("registered")
				|| security.isUserInRole("admin")) {
 			if (security.isUserInRole("registered")
 					&& !security.getUserPrincipal().getName().equals(username)) {
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
 				throw new WebApplicationException(Response
 						.status(Response.Status.SERVICE_UNAVAILABLE)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.SERVICE_UNAVAILABLE
 										.getStatusCode(),
 								"Service unavailable.", request)).build());
 			}
 			try {
 				StringBuilder sb = null;
 				int iduser = obtainIdUser(username);
 				List<Artist> artist = getArtistName(iduser);
 				List<Event> artistEventList = new ArrayList<>();
 				for (Artist A : artist) {
 					Statement stmt = connection.createStatement();
 					if (kind == null) {
 						sb = new StringBuilder(
 								"SELECT * FROM event WHERE artist='"
 										+ A.getName() + "';");
 						System.out.println(sb);
 					} else {
 						int kindId = obtainKindId(kind);
 						sb = new StringBuilder(
 								"SELECT * FROM event WHERE artist='"
 										+ A.getName() + "' AND idkind='"
 										+ kindId + "';");
 						System.out.println(sb);
 					}
 					ResultSet rs = stmt.executeQuery(sb.toString());
 					while (rs.next()) {
 						Event event = new Event();
 						event.setEventId(rs.getInt("id"));
 						event.setKindId(rs.getInt("idkind"));
 						String kind2 = obtainKind(rs.getInt("idkind"));
 						event.setKind(kind2);
 						event.setArtist(rs.getString("artist"));
 						event.setDate(rs.getString("date"));
 						event.setPlace(rs.getString("place"));
 						event.setCity(rs.getString("city"));
 						event.setCountry(rs.getString("country"));
 						event.setInfo(rs.getString("info"));
 						event.setInsertdate(rs.getString("insertdate"));
 						event.setLink(uri.getBaseUri().toString() + "artists/"
 								+ event.getArtist() + "/events/"
 								+ event.getEventId());
 						System.out.println("URL: " + event.getLink());
 						event.setSameKindLink(uri.getBaseUri().toString()
 								+ "artists/" + event.getArtist()
 								+ "/events?kind=" + event.getKind());
 						event.setSameCountryLink(uri.getBaseUri().toString()
 								+ "artists/" + event.getArtist()
 								+ "/events?country=" + event.getCountry());
 						artistEventList.add(event);
 					}
 					stmt.close();
 				}
 				connection.close();
 				return artistEventList;
 			} catch (SQLException e) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.INTERNAL_SERVER_ERROR)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.INTERNAL_SERVER_ERROR
 										.getStatusCode(),
 								"Error accessing to database.", request))
 						.build());
 			}
 		}
		return null;
 	}
 
 	public int obtainIdUser(String username) {
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
 			// SELECT id FROM user WHERE username='ubuntu';
 			StringBuilder sb = new StringBuilder(
 					"SELECT id FROM user WHERE username='" + username + "';");
 			System.out.println(sb);
 			ResultSet rs = stmt.executeQuery(sb.toString());
 			if (!rs.next()) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"User not found.", request)).build());
 			}
 			int id = rs.getInt("id");
 			System.out.println("User id: " + id);
 			stmt.close();
 			connection.close();
 			return id;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 
 	public String obtainArtistName(int id) {
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
 			// SELECT name FROM artist WHERE id=1;
 			StringBuilder sb = new StringBuilder(
 					"SELECT name FROM artist WHERE id=" + id + ";");
 			System.out.println(sb);
 			ResultSet rs = stmt.executeQuery(sb.toString());
 			if (!rs.next()) {
 				throw new WebApplicationException(
 						Response.status(Response.Status.NOT_FOUND)
 								.entity(APIErrorBuilder.buildError(
 										Response.Status.NOT_FOUND
 												.getStatusCode(),
 										"Artist not found maybe you need to follow someone.",
 										request)).build());
 			}
 			String name = rs.getString("name");
 			System.out.println("Artist :" + name);
 			stmt.close();
 			connection.close();
 			return name;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 
 	public List<Artist> getArtistName(int userId) {
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
 			StringBuilder sb = new StringBuilder(
 					"SELECT idartist FROM follow where iduser=" + userId + ";");
 			System.out.println(sb);
 			ResultSet rs = stmt.executeQuery(sb.toString());
 			List<Artist> artistList = new ArrayList<>();
 			while (rs.next()) {
 				Artist artist = new Artist();
 				artist.setArtistid(rs.getInt("idartist"));
 				System.out.println("Artist id: " + artist.getArtistId());
 				String name = obtainArtistName(artist.getArtistId());
 				System.out.println("Name check: " + name);
 				artist.setName(name);
 				System.out.println("Artist: " + artist.getName());
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
 
 	public int obtainKindId(String kind) {
 		System.out.println("kind: " + kind);
 		if (kind.equals("Concert") || kind.equals("concert")
 				|| kind.equals("concierto") || kind.equals("festival")
 				|| kind.equals("live") || kind.equals("directo")
 				|| kind.equals("fest")) {
 			int kindId = 1;
 			return kindId;
 		}
 		if (kind.equals("Studio Album Release")
 				|| kind.equals("studio album release") || kind.equals("CD")
 				|| kind.equals("album") || kind.equals("soundtrack")
 				|| kind.equals("single") || kind.equals("sencillo")
 				|| kind.equals("studio")) {
 			int kindId = 2;
 			return kindId;
 		}
 		if (kind.equals("Videoclip Release")
 				|| kind.equals("videoclip release") || kind.equals("video")
 				|| kind.equals("clip") || kind.equals("videoclip")) {
 			int kindId = 3;
 			return kindId;
 		} else {
 			Connection connection = null;
 			try {
 				connection = DataSourceSAP.getInstance().getDataSource()
 						.getConnection();
 			} catch (SQLException e) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.SERVICE_UNAVAILABLE)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.SERVICE_UNAVAILABLE
 										.getStatusCode(),
 								"Service unavailable.", request)).build());
 			}
 
 			try {
 				Statement stmt = connection.createStatement();
 				// SELECT id FROM kind WHERE name ='Concert';
 				StringBuilder sb = new StringBuilder(
 						"SELECT id FROM kind WHERE name='" + kind + "';");
 				System.out.println(sb);
 				ResultSet rs = stmt.executeQuery(sb.toString());
 				if (!rs.next()) {
 					throw new WebApplicationException(Response
 							.status(Response.Status.NOT_FOUND)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.NOT_FOUND.getStatusCode(),
 									"Kind not found.", request)).build());
 				}
 				int kindId = rs.getInt("id");
 				System.out.println("kindId: " + kindId);
 				stmt.close();
 				connection.close();
 				return kindId;
 			} catch (SQLException e) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.INTERNAL_SERVER_ERROR)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.INTERNAL_SERVER_ERROR
 										.getStatusCode(),
 								"Error accessing to database.", request))
 						.build());
 			}
 		}
 	}
 
 	public String obtainKind(int kindid) {
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
 			// SELECT name FROM kind WHERE id=1;
 			StringBuilder sb = new StringBuilder(
 					"SELECT name FROM kind WHERE id=" + kindid + ";");
 			System.out.println(sb);
 			ResultSet rs = stmt.executeQuery(sb.toString());
 			if (!rs.next()) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"Kind not found.", request)).build());
 			}
 			String kind = rs.getString("name");
 			System.out.println("kind: " + kind);
 			stmt.close();
 			connection.close();
 			return kind;
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
