 package org.timadorus.webapp.server.rpc.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import org.timadorus.webapp.client.character.Character;
 import org.timadorus.webapp.client.rpc.service.CreateCharacterService;
 import org.timadorus.webapp.server.RegisteredUserList;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 
 // Implementing CreateCharacterService according to GWT-RPC-Plumbing-diagramm
 public class CreateCharacterServiceImpl extends RemoteServiceServlet implements CreateCharacterService {
 
   private static final PersistenceManagerFactory PMF = RegisteredUserList.PMF;
 
   
   private static final long serialVersionUID = 2463839761006931303L;
   @Override
   public String createCharacter(Character character) {
 //    Character char1 = Character.getInstance();
 //    return char1;
    if (character != null) {
      System.out.println("\n\n**********" + character.toString() + "\n" + character.toStringPart2()
                         + "\n****************\n\n");
       saveCharacterToDB(character);
       return "SUCCESS";
     } else {
       return "FAILURE";
     }
     
   }
   
   public void saveCharacterToDB(Character character) {
 
     PersistenceManager pm = PMF.getPersistenceManager();
 
     try {
 //       Character c = Character.getInstance();//
       if (character.getName().startsWith("-")) {
         character.setName("MyCharacter1");
       }
        
        character.setAllAttrInfo();
 
        pm.makePersistent(character);
        System.out.println("\n\n******WRITE TO DB FOLLOW CHARACTER-OBJECT **********");
        System.out.println("\n" + character.getAllAttrInfo_Part1() + "\n" + character.getAllAttrInfo_Part2());
        System.out.println("\n*********************************************************\n");
        
       pm.close();
     } catch (Exception e) {
       e.printStackTrace();
     }
 
 //    getC("testx");
 
   }
   
   public Character getCharObjectFromAppEngine(String cname) {
 //  cname = cname.toLowerCase();
 
   try {
     PersistenceManager pm = PMF.getPersistenceManager();
 
     // **
     List<Character> entries = new ArrayList<Character>();
 
     Query query = pm.newQuery("SELECT FROM " + Character.class.getName());
 
     entries = (List<Character>) query.execute();
 
 //    pm.close();
 
     for (Character character : entries) {
       if (character.getName().equals(cname)) {
 //        System.out.println("YES ! Character-Object: \"" + cname+ "\" saved to Datastore and read from there !");
         System.out.println("\n\n**********Read from DB FOLLOW CHARACTER-OBJECT*********\n" 
                            + character.getAllAttrInfo_Part1() + "\n" + character.getAllAttrInfo_Part2() 
                            + "\n****************\n\n");
         System.out.println("\n*********************************************************\n");
         
         pm.close();
         return character;
       }
     }
   } catch (Exception e) {
     e.printStackTrace();
   }
 
   return null;
 }
 
   
   
 }
