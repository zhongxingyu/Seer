 package ru.xrm.app.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.xrm.app.domain.Employer;
 
 public class EmployerSet {
 
 	private static EmployerSet instance;
 	private List<Employer> employers;
 	private Long counter;
 	
 	public static synchronized EmployerSet getInstance(){
 		if (instance==null){
 			instance=new EmployerSet();
 		}
 		return instance;
 	}
 	
 	private EmployerSet(){
 		employers = new ArrayList<Employer>();
 		counter=0L;
 	}
 
 	public synchronized Employer findOrCreate(String from) {
 		for (Employer e:employers){
 			if (e.getName().equals(from)){
 				return e;
 			}
 		}
 		Employer result=new Employer(counter,from);
 		employers.add(result);
 		counter++;
		return result;
 	}
 	
 	public synchronized List<Employer> getEmployers(){
 		return employers;
 	}
 	
 }
