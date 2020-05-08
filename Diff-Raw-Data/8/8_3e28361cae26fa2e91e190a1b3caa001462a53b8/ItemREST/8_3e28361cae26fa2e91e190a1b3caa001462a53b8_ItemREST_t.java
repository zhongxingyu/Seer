 package controllers;
 
 import models.Item;
 import org.codehaus.jackson.JsonNode;
 
 import org.codehaus.jackson.node.ObjectNode;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ItemREST extends Controller {
 
     public static <T extends Item> Result getItems(Class<T> klass) {
         List<T> items =  T.all(klass);
         if (items.size() == 0) {
             return notFound(Constants.JSON_EMPTY.toString());
         } else {
             List<JsonNode> nodes = new ArrayList<JsonNode>();
             for (T itm : items)
                 nodes.add(itm.toJson());
             return ok(Json.toJson(nodes));
         }
     }
 
     public static <T extends Item> Result getItem(String id, Class<T> klass) {
         T item =  T.findById(id, klass);
         if (item == null) {
             return badRequest(Constants.JSON_EMPTY.toString());
         } else {
             return ok(item.toJson());
         }
     }
 
     public static <T extends Item> Result addItem(Class<T> klass) {
         JsonNode json = request().body().asJson();
         if(json == null) {
             return badRequest(Constants.JSON_EMPTY.toString());
         } else {
             ((ObjectNode)json).remove("id");
             T item = T.fromJson(json, klass);
             item.save();
             return ok(item.toJson());
         }
     }
 
     public static <T extends Item> Result updateItem(String id, Class<T> klass) {
         JsonNode json = request().body().asJson();
         if(json == null || id == null) {
             return badRequest(Constants.JSON_EMPTY.toString());
         } else if (T.findById(id, klass) == null) {
             return notFound(Constants.JSON_EMPTY.toString());
         } else {
             ((ObjectNode)json).put("id", id);
            System.out.println(json);
             T item = T.fromJson(json, klass);
             item.save();
             return ok(item.toJson());
         }
     }
 
     public static <T extends Item> Result deleteItem(String id, Class<T> klass) {
         T item = T.findById(id, klass);
         if (item == null) {
             return notFound(Constants.JSON_EMPTY.toString());
         } else {
             item.delete();
             return ok(item.toJson());
         }
     }
 
 }
