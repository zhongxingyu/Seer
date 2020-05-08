 package controllers;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import javax.management.Query;
 
 import extjs.*;
 
 import models.*;
 import models.sendObject.QuestList;	
 import models.sendObject.UserLogin;

 import play.mvc.*;
 
 public class Consultation extends Controller {
 
     public static void index() {
     	list();
     }
     public static void list() {
     	User user = Application.connected();
     	StoreArgs storeArgs = new StoreArgs();
     	//String find ="  order by ";
     	String find =" user = ? order by ";
     	String sort = storeArgs.OrderString();
     	if(sort==null)
     		find+=" dateIn ";
     	else
     		find+=sort;
     	System.out.println("find="+find);
     	List<Quest> quests = Quest.find(find, user ).fetch(storeArgs.page, storeArgs.limit);
     	List<QuestList> list = QuestList.Conver(quests);
         renderJSON(list);
     }
     
    public static void add(Long patient_id, String diagnosIn, Long speciality_id,  Long advisor_id, String question, Date dateRes, BigDecimal price	) {
     	User user = Application.connected();
     	Quest quest=new Quest();
     	quest.user = user;
     	quest.patient = models.Patient.findById(patient_id);
     	quest.diagnosIn = diagnosIn; 
     	quest.dateRes = dateRes;
     	quest.save();
     	History history=new History();
     	history.advisor = User.findById(advisor_id);
     	history.speciality = models.Speciality.findById(speciality_id);
     	history.question = question;
     	history.price = price;
     	history.quest = quest;
     	history.save();
         renderJSON( new  extjs.Response(true, quest.id));
     }
     
 
 }
