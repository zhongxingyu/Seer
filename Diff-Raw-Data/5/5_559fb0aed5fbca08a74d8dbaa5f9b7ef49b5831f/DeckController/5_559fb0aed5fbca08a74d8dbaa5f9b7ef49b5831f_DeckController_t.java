 package org.walkingarchive.backend.controller;
 
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 import org.walkingarchive.backend.model.card.Card;
 import org.walkingarchive.backend.model.card.CardDAO;
 import org.walkingarchive.backend.model.card.Deck;
 import org.walkingarchive.backend.model.security.SecurityDAO;
 import org.walkingarchive.backend.model.security.User;
 
 @Path("/deck/")
 public class DeckController {
     @Context
     protected ServletContext context;
     @Context
     protected HttpServletRequest request;
     @Context
     protected HttpServletResponse response;
     @Context
     protected UriInfo uriInfo;
 
     @GET
    @Produces(MediaType.APPLICATION_JSON)
     @Path("user/{userId}")
     public List<Deck> getDecksByUser(@PathParam("userId") String userId) {
         //TODO - validate input
         User user = SecurityDAO.getInstance().getUserById(Integer.parseInt(userId));
         return CardDAO.getInstance().getDecks(user);
     }
     
     @GET
    @Produces(MediaType.APPLICATION_JSON)
     @Path("id/{id}")
     public Deck getDeck(@PathParam("id") int id) {
         //TODO - validate input
         Deck deck = CardDAO.getInstance().getDeck(id);
         return deck;
     }
     
     @PUT
     @Path("add")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response createDeck(String json) throws Exception {
         JSONObject jsonObject = new JSONObject(json);
         User user = SecurityDAO.getInstance().getUserById(jsonObject.getInt("user"));
         
         Deck deck = new Deck(user, jsonObject.getString("name"));
         deck = CardDAO.getInstance().createDeck(deck);
         
         return Response.ok(deck, MediaType.APPLICATION_JSON).build();
     }
     
     @POST
     @Path("update")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response update(String json) throws Exception {
         Response result;
         JSONObject jsonObject = new JSONObject(json);
         Deck deck = CardDAO.getInstance().getDeck(jsonObject.getInt("id"));
 
         JSONArray collection = jsonObject.getJSONArray("collection");
         
         for(int i = 0; i < collection.length(); i++) {
             Card card = CardDAO.getInstance().getCard(collection.getInt(i));
             deck.addCardToCollection(card);
         }
         if (jsonObject.get("name") != null) {
             deck.setName(jsonObject.getString("name"));
         }
         
         deck = CardDAO.getInstance().updateDeck(deck);
         
         if (deck != null) {
             result = Response.ok(deck, MediaType.APPLICATION_JSON).build();
         }
         else {
             result = Response.status(Status.NOT_FOUND).build();
         }
         
         return result;
     }
 
     @DELETE
     @Path("delete/{id}")
     public Response delete(@PathParam("id") int id) {
         Response result;
         Deck deck = CardDAO.getInstance().getDeck(id);
         if (deck != null) {
             CardDAO.getInstance().deleteDeck(deck);
             result = Response.ok().build();
         }
         else {
             result = Response.status(Status.NOT_FOUND).build();
         }
         
         return result;
     }
 }
