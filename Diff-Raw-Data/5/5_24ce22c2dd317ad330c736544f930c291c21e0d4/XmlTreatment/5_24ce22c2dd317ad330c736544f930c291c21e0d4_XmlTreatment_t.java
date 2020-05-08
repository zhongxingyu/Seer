 package socialNetwork.src;
 
 import java.io.*;
 
 import org.jdom2.*;
 import org.jdom2.output.*;
 import org.jdom2.input.*;
 import org.jdom2.filter.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 
 public class XmlTreatment {
 
 	static Element racine = new Element("personnes");
 	static org.jdom2.Document document = new Document(racine);
 
 	static void readFileXML(String fichier) throws Exception{
 		SAXBuilder sxb = new SAXBuilder();
 		document = sxb.build(new File(fichier));
 		racine = document.getRootElement();
 	}
 
 	// static void enregistreFichier(String fichier){
 	// try{
 	// XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
 	// sortie.output(document, new FileOutputStream(fichier));
 	// }
 	// catch(java.io.IOException e){}
 	// }
 	//
 	public static void deleteFriendXML(String name){
 		List listElement = racine.getChildren("name");
 		Iterator i = listElement.iterator();
 		while(i.hasNext()){
 			Element courant = (Element)i.next();
 			if(courant.getChild(name)!=null){
 				courant.removeChild(name);
 				courant.setName("name_modified");
 			}
 		}
 	}
 
 	public static void addFriendXML(String friendName, String friendHost, String status) throws Exception{
 		readFileXML("friendList");
 		Element friend = new Element("friend");
 		racine.addContent(friend);
 		Attribute name = new Attribute("name", friendName);
 		friend.setAttribute(name);
 		Attribute host = new Attribute("host", friendHost);
 		friend.setAttribute(host);
 		Attribute friendStatus = new Attribute("friendStatus", status);
 		friend.setAttribute(friendStatus);
 		Attribute friendPicture = new Attribute("Picture", "none");
 		friend.setAttribute(friendStatus);
 	}
 
 	/*Récupération liste d'amis*/
 	public static ArrayList<Friends> getFriendsXML(){
 		ArrayList<Friends> listFriends = new ArrayList<Friends>();
 		try {
 			readFileXML("friendList");
 			racine.getChildren();
 			List listTmp = racine.getChildren("friend");
 			Iterator i = listTmp.iterator();
 			while(i.hasNext()){
 				Element courant = (Element)i.next();
 				listFriends.add(new Friends(courant.getChild("name").getText(), courant.getChild("host").getText(), courant.getChild("friendStatus").toString()));
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return listFriends;
	}
}
