 package com.drtshock.willie.util;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public enum Dictionary {
     
     URBAN_DICTIONARY("http://api.urbandictionary.com/v0/define?term=%WORD%", 0, "Urban Dictionary"),
    DUCK_DUCK_GO("http://api.duckduckgo.com/?q=define+%WORD%&format=json", 1, "Duck Duck Go");
 
     private String url, name;
     private int id;
 
     Dictionary(String url, int id, String name) {
         this.url = url;
         this.id = id;
         this.name = name;
     }
 
     public URL getFormattedURL(String word) {
         try {
             return new URI(this.url.replace("%WORD%", word)).toURL();
         } catch (MalformedURLException | URISyntaxException e) {
             return null;
         }
     }
 
     public int getID() {
         return this.id;
     }
     
     @Override
     public String toString(){
         return this.name;
     }
 
     public static Dictionary getDictionaryFromID(int id) {
         for(Dictionary dict : Dictionary.values()) {
             if(dict.getID() == id)
                 return dict;
         }
         return null;
     }
     
     public Dictionary.Definition getDefinition(String word) throws IOException {
         if(this == Dictionary.URBAN_DICTIONARY) {
             JsonObject obj = new JsonParser().parse(WebHelper.readURLToString(this.getFormattedURL(word))).getAsJsonObject();
             if(!obj.get("result_type").getAsString().equals("no_results") && !obj.getAsJsonArray("list").isJsonNull())
                 return new Dictionary.Definition(obj.getAsJsonArray("list").get(0).getAsJsonObject().get("definition").getAsString(), WebHelper.shortenURL("http://www.urbandictionary.com/define.php?term=" + word));
         } else if(this == Dictionary.DUCK_DUCK_GO) {
             JsonObject obj = new JsonParser().parse(WebHelper.readURLToString(this.getFormattedURL(word))).getAsJsonObject();
             if(!obj.get("AbstractText").getAsString().equals(""))
                 return new Dictionary.Definition(obj.get("AbstractText").getAsString(), WebHelper.shortenURL("http://www.thefreedictionary.com/" + word));
         }
         return null;
     }
 
     public static class Definition {
         private String definition, url;
 
         public Definition(String definition, String url) {
             this.definition = definition;
             this.url = url;
         }
 
         public String getDefinition() {return definition;}
         public String getUrl() {return url;}
 
     }
 
 }
