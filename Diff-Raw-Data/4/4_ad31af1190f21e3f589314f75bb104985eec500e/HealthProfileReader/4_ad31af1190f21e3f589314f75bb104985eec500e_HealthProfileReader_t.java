 import java.util.HashMap;
 import java.util.Map;
 
 import pojos.HealthProfile;
 import pojos.Person;
 
 
 public class HealthProfileReader {
 	
 	public static Map<String,Person> database = new HashMap<String,Person>();
 	
 	static
 	{
 	    Person pallino = new Person();
 		Person pallo = new Person("Pinco", "Pallo");
		HealthProfile hp = new HealthProfile(68.0, 1.72);
		Person john = new Person("John", "Doe", hp);
 		
 		database.put(pallino.getFirstname() + " " + pallino.getLastname(), pallino);
 		database.put(pallo.getFirstname() + " " + pallo.getLastname(), pallo);
 		database.put(john.getFirstname() + " " + john.getLastname(), john);
 	}
 	
 	public void addPerson(String fname, String lname)
 	{
 	    Person p = new Person(fname, lname);
 	    database.put(p.getFirstname() + " " + p.getLastname(), p);
 	}
 	
 	public HealthProfile getHealthProfile(String fname, String lname)
 	{
 	    HealthProfile hp = null;
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        hp = p.gethProfile();
 	    }
 	    return hp;
 	}
 	
 	public double getWeight(String fname, String lname)
 	{
 	    double w = 0;
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        w = p.gethProfile().getWeight();
 	    }
 	    return w;
 	}
 	
 	public void setWeight(String fname, String lname, double value)
 	{
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        p.gethProfile().setWeight(value);
 	    }
 	}
 	
 	public double getHeight(String fname, String lname)
 	{
 	    double h = 0;
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        h = p.gethProfile().getHeight();
 	    }
 	    return h;
 	}
 	
 	public void setHeight(String fname, String lname, double value)
 	{
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        p.gethProfile().setHeight(value);
 	    }
 	}
 	
 	public double getBMI(String fname, String lname)
 	{
 	    double bmi = 0;
 	    Person p = database.get(fname + " " + lname);
 	    if (p != null) {
 	        bmi = p.gethProfile().getBMI();
 	    }
 	    return bmi;
 	}
 }
