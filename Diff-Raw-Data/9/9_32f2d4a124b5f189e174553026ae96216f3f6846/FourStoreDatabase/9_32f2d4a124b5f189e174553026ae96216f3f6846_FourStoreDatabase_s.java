 package utils;
 
 import FourStore.Store;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import zone.utils.Config;
 import zone.utils.Item;
 import zone.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class FourStoreDatabase {
     private static Store st = null;
     private static String uri = Config.getVar("FourStore-server");
     
     public static Store getStore(){
         try {
             if(st == null){
                 
                     st = new Store(uri);
             }
             return st;
         } catch (MalformedURLException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public static void addItems(ArrayList<Item> items){
         addItems(items.toArray(new Item[items.size()]));
     }
     
     public static void addItems(Item[] items){
         for(int i=0; i < items.length;i++){
             addItem(items[i]);
         }
     }
 
     public static void addItem(Item item){
         Model model = ModelFactory.createDefaultModel();
         Resource itemNode = model.createResource(item.getUri());
         Iterator it = item.values.iterator();
         while (it.hasNext()){
             Prop cle = (Prop)it.next();
             System.out.println(cle.getType());
             if(cle.isLiteral()){
                 itemNode.addLiteral(cle.getType(), model.createLiteral(cle.getValue()));
             }
             else{
                 itemNode.addProperty(cle.getType(), model.createResource(cle.getValue()));
             }
         }
         StringWriter sw = new StringWriter();
         model.write(sw);
         String graph = sw.toString();
         model.write(System.out);
 
         String response="";
         try {
            getStore().insertModel(model, "http://ZONE.zouig.org/data");
         } catch (MalformedURLException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ProtocolException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println(response);
     }
       
     public static boolean ItemURIExist(String uri){
         String queryString = "ASK { <"+uri+"> <http://purl.org/rss/1.0/title> ?z}";
         String response1="";
         try {
             response1 = getStore().query(queryString);
         } catch (MalformedURLException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         } catch (ProtocolException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         } catch (IOException ex) {
             Logger.getLogger(FourStoreDatabase.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         System.out.println(response1);
         return false;
     }
       
     public static void verifyItemsList(ArrayList<Item> items){
         for (Iterator<Item> iterator = items.iterator(); iterator.hasNext(); ) {
             Item o = iterator.next();
             if (ItemURIExist(o.getUri())) {
                 iterator.remove();
             }
         }
     }
     
     public static void main(String[] args){
         Item it = new Item("http://test","bienvenue à Antibes","",new Date());
         FourStoreDatabase.addItem(it);
         
     }
     
 }
