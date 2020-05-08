 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
         if (query.contains("programming")) {
             return "Computer programming is the comprehensive process that leads from an original " 
                    + "formulation of a computing problem to executable programs.";
         }
 
         if (query.contains("name")){
             return "Damp Peak";
         }
 
         if (query.contains("github"))
             return "GitHub is the best place to share code with friends, co-workers, classmates, " +
                     "and complete strangers. " +
                     "Over three million people use GitHub to build amazing things together.";
	if(query.contains("name")) return "Damp Peak";
 
         return "";
     }
 }
