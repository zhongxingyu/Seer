 package com.hxdcml.protocol;
 
 import com.google.gson.GsonBuilder;
 import com.hxdcml.card.magic.Card;
 import com.hxdcml.lang.Constant;
 import com.hxdcml.parse.Parser;
 import com.hxdcml.sql.QueryMap;
 import com.hxdcml.sql.QueryNode;
 import com.hxdcml.sql.SQLite;
 import com.hxdcml.sql.UpdateMap;
 import com.hxdcml.wrapper.MagicWrapper;
 
 import java.sql.SQLException;
 import java.util.AbstractMap;
 
 /**
  * User: Souleiman Ayoub
  * Date: 1/2/13
  * Time: 7:28 PM
  */
 public class MagicProtocol extends Protocol {
     protected MagicProtocol() throws SQLException {
         procedure = new MagicWrapper(new SQLite(Constant.TYPE_MAGIC));
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        gson = builder.create();
     }
 
     /**
      * Requests to search the Database based on the message
      *
      *
      * @param message Contains details on what to search
      * @return a String that may contain the search result.
      */
     @Override
     protected String requestSearch(ProtocolMessage message) throws SQLException {
         AbstractMap query = (AbstractMap) message.getObject();
         QueryMap map = new QueryMap(query);
         Card[] cards = (Card[]) procedure.query(map);
 
         QueryNode node = map.get(Constant.NAME);
         if (node != null && node.isExact() && cards.length == 0) {
             cards = (Card[]) Parser.parse(node.getValue(), Constant.TYPE_MAGIC);
             for (Card card : cards)
                 procedure.insert(card);
         }
 
         if (cards.length != 0)
             return gson.toJson(cards, Card[].class);
         return null;
     }
 
     /**
      * Requests an update on a specific detail based on the message given.
      *
      *
      * @param message contains detail on what to search
      * @return a boolean true if successfully updated, otherwise false. In a String for Json.
      */
     @Override
     protected String requestUpdate(ProtocolMessage message) throws SQLException {
         AbstractMap<String, String> map = (AbstractMap<String, String>) message.getObject();
         UpdateMap update = new UpdateMap(map);
         boolean result = procedure.update(message.getName(), update);
         return gson.toJson(result);
     }
 
     /**
      * Requests a delete on a specific detail based on the message given.
      *
      *
      * @param message contains detail on what to delete
      * @return a boolean true if successfully updated, otherwise false. In a String for Json.
      */
     @Override
     protected String requestDelete(ProtocolMessage message) throws SQLException {
         String name = message.getName();
         boolean delete = procedure.delete(name);
         return gson.toJson(delete);
     }
 }
