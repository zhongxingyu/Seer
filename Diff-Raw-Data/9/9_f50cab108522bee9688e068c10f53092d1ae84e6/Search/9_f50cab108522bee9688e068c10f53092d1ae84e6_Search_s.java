 package Entities;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import com.google.gson.Gson;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.cmd.Query;
 
 @Entity
 public class Search {	
 	@Id String email;
 	public static SearchResult performSearch( SearchRequest sr) {
 		//TODO: alter order based on user input
 		//TODO: disallow searching on input not filled out in a user profile
 		//TODO: call method by string name to get rid of if statemetns
 		
 		Mentee mentee = OfyService.ofy().load().type(Mentee.class).id( sr.mentee ).get();
 		Query<Mentee> q = OfyService.ofy().load().type(Mentee.class); //TODO: Decide what happens when the user does not select anything.
 		q = q.filter("Email !=", sr.mentee); //TODO: optimize this
 		
 		for ( String param : sr.getParameters() ) {
 
 			if ( param.equals("Majors") ) {
 				q = q.filter("Majors in", mentee.getMajors());
 			}		
 			if ( param.equals("ZipCode") ) {
 				q = q.filter("ZipCode", mentee.getZipCode() );
 			}		
 			if ( param.equals("Interests") ) {
 				q = q.filter("Interests in", mentee.getInterests() );
 			}
 			if ( param.equals("Current_Courses") ) {
 				q = q.filter("Current_Courses in", mentee.getCurrentCourses() );
 			}
 			if ( param.equals("Past_Courses") ) {
 				q = q.filter("Past_Courses in", mentee.getPastCourses() );
 			}
 			if ( param.equals("Classification") ) {
 				q = q.filter("Classification", mentee.getClassification() );
 			}
 			ArrayList<String> SearchTerms = new ArrayList<String>();
 			StringTokenizer st = new StringTokenizer(param, " ");
 			while (st.hasMoreElements()) {
 				SearchTerms.add((st.nextElement().toString()).replaceAll("\\W", "").toLowerCase());
 			}
 			if (SearchTerms.contains("muscle")) {
 				ArrayList<String> list = new ArrayList<String>();
 				list.add("Weight Lifting");
 				q = q.filter("Interests in", list );
 			}
 			if ( SearchTerms.contains("ee") ) {
 				ArrayList<String> list = new ArrayList<String>();
 				list.add("Electrical Engineering");
 				q = q.filter("Majors in", list);
 				list.remove(0);
 				if(SearchTerms.indexOf("ee") != (SearchTerms.size()-1)) {
 					list.add("EE " + SearchTerms.get((SearchTerms.indexOf("ee")+1)).toUpperCase());
 					q = q.filter("Past_Courses in", list);
					Query<Mentee> q2 = OfyService.ofy().load().type(Mentee.class);
 					q2 = q2.filter("Current_Courses in", list);
 					ArrayList<Mentee> union = new ArrayList<Mentee>();
 					for(Mentee m : q)
 						union.add(m);
 					for(Mentee m : q2)
 					{
 						if(!union.contains(m))
 							union.add(m);
 					}
 					SearchResult result = new SearchResult();
 					result.setMatches(union);
 					return result;
 				}
 			}
 			if ( SearchTerms.contains("party") || SearchTerms.contains("frat") || SearchTerms.contains("greek") || SearchTerms.contains("social") || SearchTerms.contains("fraternity")) {
 				ArrayList<String> list = new ArrayList<String>();
 				list.add("Greek Life");
 				list.add("Partying");
 				q = q.filter("Interests in", list);
 			}
 		}
 				
 		SearchResult result = new SearchResult();
 		result.setMatches( q.list() );
 		return result;
 		
 	}
 	
 	public Search() {}
 	
 	public String toJson() {
 		Gson gson = new Gson();
 		return gson.toJson(this);
 	}
 	
 	public static Search fromJson(String json) {
 		Gson gson = new Gson();
 		return gson.fromJson(json, Search.class);
 		
 	}
 	
 	
 }
