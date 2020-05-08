 package business;
 
 import hibernate.ConnectionManagement;
 import hibernate.FieldManagement;
 import hibernate.UserManagement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Connection;
 import models.Field;
 import models.User;
 
 public class SeekUsersWithGivenSubject {
 	
 	//skal bruk en metode fra usermanagement for  f en liste med brukere
 	private ArrayList<User> mentors;
 	private ArrayList<User> trainee;
 	private static FieldManagement fm = new FieldManagement();
 	private static UserManagement um = new UserManagement();
 	private static ConnectionManagement xm = new ConnectionManagement();
 	private static boolean ispopulated = false;
 	private Field foundsubject;
 	private ArrayList<Connection> connections;
 	
 	public SeekUsersWithGivenSubject(String subject) {
 		if(!ispopulated){
 			populateDatabase();
 		}
 		foundsubject = fm.getSingleByTitle(subject);
		connections = (ArrayList<Connection>) xm.getOpenMentorConnections(foundsubject);
 		mentors = new ArrayList<User>();
 		trainee = new ArrayList<User>();
 	}
 	
 	
 
 	private static void populateDatabase(){
		
 		Field subJava3D = new Field("Java 3D", "Old 3D graphics API for n00bs");
 		Field subJava2D = new Field("Java 2D", "2D Java graphics API");
 		
 		Field subCSh1 = new Field("C# field 1", "C# for everybody");
 		Field subCSh2 = new Field("C#", "C# for extreme");
 		Field subCSh3 = new Field("C#","C# General");
 		Field subCpluss = new Field("C++","C++ General");
 		
 		fm.addField(subJava3D);
 		fm.addField(subJava2D);
 		fm.addField(subCSh1);
 		fm.addField(subCSh2);
 		fm.addField(subCSh3);
 		fm.addField(subCpluss);
 		
 		User user1 = new User("Bob", "Smith", "bob@smith.com", "here", "", "345678");
 		User user2 = new User("Glen", "Smiths", "basdfg@smith.com", "there", "",  "2323");
 		User user3 = new User("Tom", "Smite", "bob@adrbafd.com", "over here", "", "7865425");
 		User user4 = new User("Alex", "Smile", "bob@6ytghj.com", "over there", "", "456412");
 		User user5 = new User("Dave", "Store", "bob@adfbafv.com", "here", "", "34898745");
 		User user6 = new User("John", "Robertson", "65tghj@smith.com", "here", "", "3893564");
 		
 		um.addUser(user1);
 		xm.createOpenMentor(user1, subCSh3);
 		xm.createOpenMentor(user1, subJava2D);
 		xm.createOpenTrainee(user1, subCpluss);
 		xm.createOpenTrainee(user1, subJava3D);
 		
 		um.addUser(user2);
 		xm.createOpenMentor(user2, subCSh2);
 		xm.createOpenMentor(user2, subJava2D);
 		xm.createOpenTrainee(user2, subJava3D);
 		
 		um.addUser(user3);
 		xm.createOpenTrainee(user3, subCpluss);
 		xm.createOpenTrainee(user3, subJava2D);
 		
 		um.addUser(user4);
 		xm.createOpenMentor(user4, subCpluss);
 		
 		um.addUser(user5);
 		xm.createOpenMentor(user5, subCpluss);
 		xm.createOpenMentor(user5, subJava3D);
 		xm.createOpenTrainee(user5, subCSh1);
 		
 		um.addUser(user6);
 		xm.createOpenMentor(user6, subJava2D);
 		xm.createOpenTrainee(user6, subJava3D);
 		ispopulated = true;
 	}
 	
 	public ArrayList<User> getMentorsWithSbject() {
 		for(Connection conn : connections){
 			mentors.add(conn.getMentor());
 		}
 		return mentors;
 	}
 	
 	public ArrayList<User> getTrainee() {
 		for(Connection conn : connections){
 			trainee.add(conn.getTrainee());
 		}
 		
 		return trainee;
 	}
 
 	public void setTrainee(ArrayList<User> trainee) {
 		this.trainee = trainee;
 	}
 	
 	public Field getFoundsubject() {
 		return foundsubject;
 	}
 
 	public void setFoundsubject(Field foundsubject) {
 		this.foundsubject = foundsubject;
 	}
 	
 	public ArrayList<Connection> getConnections() {
 		return connections;
 	}
 
 	public void setConnections(ArrayList<Connection> connections) {
 		this.connections = connections;
 	}
 	
 	
 }
