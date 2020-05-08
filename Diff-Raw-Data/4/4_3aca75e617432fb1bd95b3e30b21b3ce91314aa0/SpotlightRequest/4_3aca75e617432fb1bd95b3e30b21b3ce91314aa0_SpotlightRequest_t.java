 package org.zoneproject.extractor.plugin.spotlight;
 
 /*
  * #%L
  * ZONE-plugin-Spotlight
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.ZoneOntology;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class SpotlightRequest {
     public enum Endpoints {
	EN("http://spotlight.dbpedia.org/rest/"),
	FR("http://spotlight.sztaki.hu:2225/rest"),
 	//NL("http://nl.dbpedia.org/spotlight/rest"),
 	DE("http://de.dbpedia.org/spotlight/rest"),
 	//HU("http://spotlight.sztaki.hu:2229/rest"),
 	IT("http://spotlight.sztaki.hu:2230/rest"),
 	//PT("http://spotlight.sztaki.hu:2228/rest"),
 	//RU("http://spotlight.sztaki.hu:2227/rest"),
 	ES("http://spotlight.sztaki.hu:2231/rest");
 	//TR("http://spotlight.sztaki.hu:2235/rest");
         
         private final String value;
 	Endpoints(String value) {this.value = value;}
 	public String getValue() {return this.value;}
     }
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(SpotlightRequest.class);
     
     public static ArrayList<Prop> getProperties(Item item){
         String endpoint="";
         try {
             ArrayList<Prop> result = new ArrayList<Prop>();
             String itemLang = item.getElements(ZoneOntology.PLUGIN_LANG)[0].toUpperCase();
             
             try{
                 endpoint = Endpoints.valueOf(itemLang).getValue();
             }catch(java.lang.IllegalArgumentException ex){
                 endpoint = Endpoints.valueOf("EN").getValue();
             }
             String json = getResponse(item.concat(), endpoint);
             String[] entities = SpotlightRequest.getNamedEntities(json);
             for(String e : entities){
                 Prop p = new Prop(ZoneOntology.PLUGIN_SPOTLIGHT_ENTITIES, e, false,true);
                 result.add(p);
             }
             return result;
         } catch (IOException ex) {
             logger.warn("The server "+ endpoint + " is not responding");
             return null;
         }
     }
         public static String getResponse(String text,String endPoint) throws IOException {
             URL dbpedia;
             HttpURLConnection dbpedia_connection;
             dbpedia = new URL(endPoint+"/annotate");
             dbpedia_connection = (HttpURLConnection) dbpedia.openConnection();
             dbpedia_connection.setDoOutput(true);
             dbpedia_connection.setRequestMethod("GET");
             dbpedia_connection.setRequestProperty("Accept", "application/json");
             String urlParameters = "confidance=0.5&support=80&text=";
             urlParameters = urlParameters.concat(URLEncoder.encode(text));
 
             dbpedia_connection.setDoOutput(true);
             DataOutputStream wr = new DataOutputStream(dbpedia_connection.getOutputStream());
             wr.writeBytes(urlParameters);
             wr.flush();
             wr.close();
             dbpedia_connection.connect();
             BufferedReader in = new BufferedReader(
                                 new InputStreamReader(
                                                 dbpedia_connection.getInputStream()));
             String inputLine;
             String output = "";
             while ((inputLine = in.readLine()) != null) {
                 output += inputLine;
             }
             in.close();
             return output;
         }   
         
     public static String[] getNamedEntities(String f){
         ObjectMapper mapper = new ObjectMapper();
         LinkedHashSet<String> result = new LinkedHashSet<String>();
         
         try {
             //first need to allow non-standard json
             mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
             mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
             mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
             Map<String,Object> map = mapper.readValue(f, Map.class);
             
             ArrayList<LinkedHashMap> documentElems = (ArrayList<LinkedHashMap>)map.get("Resources");
             if(documentElems != null){
                 for(int i=0; i < documentElems.size();i++){
                     LinkedHashMap cur = (LinkedHashMap) documentElems.get(i);
                     result.add(cur.get("@URI").toString());
                 }
             }
         } catch (Exception ex) {
             Logger.getLogger(SpotlightRequest.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
         return result.toArray(new String[result.size()]);
     }
     public static void main(String[] args) throws IOException {
         String text = "Un nouveau portail pour notre documentation en ligne.\\n Antidot met à disposition de l’ensemble de ses clients et partenaires un nouveau portail pour l’accès en ligne à la documentation de ses produits. Ce portail documentaire a pour ambition de faciliter vos recherches et de simplifier votre navigation au sein de près de 2000 pages de Guides, Notes techniques et Notes de version : […]. Antidot met à disposition de l’ensemble de ses clients et partenaires un nouveau portail pour l’accès en ligne à la documentation de ses produits .Ce portail documentaire a pour ambition de faciliter vos recherches et de simplifier votre navigation au sein de près de 2000 pages de Guides, Notes techniques et Notes de version :Ce service vous est aujourd’hui ouvert en version beta. N’hésitez pas à nous faire part de vos retours : tous les commentaires et suggestions que nous recueillerons seront étudiés avec la plus grande attention.Pour la petite histoire, ce portail documentaire est réalisé intégralement à partir de nos solutions dont il exploite les fonctionnalités avancées :AIF – Information Factory : pour la recomposition et l’analyse des unités documentaires fines,AFS – Finder Suite : pour le moteur de recherche et la lecture dynamique et continue.Il sera bientôt enrichi des fonctions d’alertes et d’annotation apportées par notre produit ACS – Collaboration Services .Nous vous remercions de votre confiance.Partagez";
         text = "Réserve parlementaire: Chevènement et Hue demandent à Fabius des explications sur des fuites dans \"Le Monde\".\n Dépêche AFP, mardi 10 septembre 2013, 17h46. Les sénateurs Jean-Pierre Chevènement et Robert Hue ont demandé lundi au ministre des Affaires étrangères Laurent Fabius, dans une lettre commune, des explications sur un \"fichier\" que le Quai d'Orsay aurait communiqué au journal Le Monde concernant l'usage par des élus de leur réserve parlementaire. Dans son édition datée de dimanche-lundi, Le Monde affirmait s'être vu communiquer par le ministère des Affaires étrangères un fichier détaillant les versements de députés et sénateurs en faveur de programmes de développement ou relatifs à l'action extérieure de la France. Pointant du doigt l'utilisation par certains élus de leur réserve parlementaire 2011 ou 2012 pour financer leurs propres associations, le journal citait MM. Hue, président du Mouvement unitaire progressiste (MUP), Chevènement, président d'honneur du Mouvement républicain et citoyen (MRC), et l'ancien président Valéry Giscard d'Estaing. \"J'observe que M. Giscard d'Estaing,";
         
         text = "la\",Syrie";
         String json = SpotlightRequest.getResponse(text,"http://spotlight.sztaki.hu:2225/rest");
         getNamedEntities(json);
     }
 }
