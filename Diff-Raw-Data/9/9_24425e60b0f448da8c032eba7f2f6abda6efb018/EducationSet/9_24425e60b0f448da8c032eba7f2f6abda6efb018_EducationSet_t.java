 package ru.xrm.app.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.xrm.app.domain.Education;
 
 public class EducationSet {
 
 	private static EducationSet instance;
 	private List<Education> educations;
 	private Long counter;
 	
 	public static synchronized EducationSet getInstance(){
 		if (instance==null){
 			instance=new EducationSet();
 		}
 		return instance;
 	}
 	
 	private EducationSet(){
 		educations = new ArrayList<Education>();
 		counter=0L;
 	}
 
 	public synchronized Education findOrCreate(String from) {
 		for (Education e:educations){
 			if (e.getName().equals(from)){
 				return e;
 			}
 		}
 		Education result=new Education(counter,from);
 		educations.add(result);
 		counter++;
		return result;
 	}
 	
 	public synchronized List<Education> getEducations(){
 		return educations;
 	}
 	
 }
