 package com.github.aprestaux.funreco.api;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Recommendations {
	private Profile profile;
 	private Map<String, Recommendation> recommendations = new HashMap<String, Recommendation>();
 
 	
 	public Collection<Recommendation> getEntries() {
 		return recommendations.values();
 	}
 
 	public Recommendation forQuery(String query) {
 		return recommendations.get(query);
 	}
 
 	public void addRecommendation(Recommendation recommendation) {
 		recommendations.put(recommendation.getQuery(), recommendation);
 	}
 
 	public boolean equals(Recommendations recommendations) {
		return (this.profile.equals(recommendations.profile) && this.recommendations.equals(recommendations.recommendations));
 	}
 }
