 package WardPP;
 
 import Data.Nurse;
 import Data.Ward;
 import Data.Wards;
 import Data.XmlHandler;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: James
  * Date: 9/29/13
  * Time: 4:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WardPPModel {
     private Wards wardList;
     private XmlHandler xml = new XmlHandler();
 
     WardPPModel(){
         wardList = xml.getWardsFromXML();
     }
     public void newWardDataToXML(String name, int roster){
         ArrayList<Nurse> nurses = setupDefaultNurses();
         Ward newWard = new Ward(name,roster,generateNewId(), nurses);
         ArrayList<Ward> w = wardList.getListOfWards();
         w.add(newWard);
         wardList.setListOfWards(w);
         xml.writeWardsToXML(wardList);
     }
     private int generateNewId(){
         ArrayList<Ward> w = wardList.getListOfWards();
     int id = w.get(w.size()-1).getId()+1;
     return id;
     }
     //Used for now to replicate Default nurse information.
     private ArrayList<Nurse> setupDefaultNurses(){
         ArrayList<Nurse> nurses = new ArrayList<Nurse>();
         Nurse testNurse1 = new Nurse("TestNurse1", 0, "SRN",5,"DN");
         Nurse testNurse2 = new Nurse("TestNurse2", 1, "SRN",5,"DN");
        Nurse testNurse3 = new Nurse("TestNurse3", 0, "SRN",5,"D");
        Nurse testNurse4 = new Nurse("TestNurse4", 1, "RN",5,"N");
        Nurse testNurse5 = new Nurse("TestNurse5", 0, "RN",5,"D");
        Nurse testNurse6 = new Nurse("TestNurse6", 1, "RN",5,"N");
         nurses.add(testNurse1);
         nurses.add(testNurse2);
         nurses.add(testNurse3);
         nurses.add(testNurse4);
         nurses.add(testNurse5);
         nurses.add(testNurse6);
         return nurses;
     }
 }
