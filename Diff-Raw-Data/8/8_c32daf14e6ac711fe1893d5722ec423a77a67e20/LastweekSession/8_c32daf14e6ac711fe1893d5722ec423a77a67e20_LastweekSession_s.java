 /*
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.session;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebSession;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.marc.lastweek.business.services.aaa.AaaService;
 import com.marc.lastweek.business.views.aaa.AuthenticatedUserData;
 import com.marc.lastweek.business.views.classifiedad.FilterParameters;
 
 
 public class LastweekSession extends WebSession {    
     private static final long serialVersionUID = 6817054705718877022L;
     
     @Autowired
     AaaService aaaService;
     
     private AuthenticatedUserData user = null;
     private List<Long> favorites;
     private List<FilterParameters> filterParametersHistory = null;
     
     public LastweekSession(Request request) {
         super(request);     
         this.favorites = new ArrayList<Long>();
         this.filterParametersHistory = new ArrayList<FilterParameters>();
     }
 
     
     public AuthenticatedUserData getUser() {
         return this.user;
     }
     
     
     public String getUserLogin() {
         return (this.user == null)?  null : this.user.getLogin();
     }
     
     public static LastweekSession get() {
         return (LastweekSession) Session.get();
     }
     
     public List<Long> getFavorites() {
 		return this.favorites;
 	}
 	
 	public void addFavorite(Long id) {
 		this.favorites.add(id);
 	}
 	
 	public void removeFavorite(Long id) {
 		this.favorites.remove(id);
 	}
 	
 	public boolean containsFavorite(Long id) {
 		return this.favorites.contains(id);
 	}
 	
 	public int favoritesCount() {
 		return this.favorites.size();
 	}
 
 	public void addFilterParametersToHistory(FilterParameters filterParameters) {
 	    this.filterParametersHistory.add(filterParameters);
 	}
 	
 	public FilterParameters getRamdomFilterParametersFromHistory() {
 	    FilterParameters result = null;
 	    if ( !this.filterParametersHistory.isEmpty() ) {
 	        int size = this.filterParametersHistory.size();
	        int upperLimitAndSeed  = ( size - 1 < 0 )  ? 0 : size -1;	        
	        Random rand = new Random( upperLimitAndSeed );
	        result = this.filterParametersHistory.get( rand.nextInt( upperLimitAndSeed ) );
 	    } else {
 	        result = new FilterParameters();
 	    }
 	    return result;
 	}
 	
 
 }
