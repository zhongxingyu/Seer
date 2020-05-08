 /*
  * ClasifiedAdRepository.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.business.entities.classifiedad.repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import loc.marc.commons.business.entities.general.GeneralRepository;
 
import org.springframework.stereotype.Repository;

 import com.marc.lastweek.business.entities.classifiedad.ClassifiedAd;
 import com.marc.lastweek.business.views.aaa.FilterParameters;
 
@Repository
 public class ClassifiedAdRepository extends GeneralRepository {
 
 	public List<ClassifiedAd> filterAdvertisements(FilterParameters parameters, int start, int count) {
 		List<ClassifiedAd> results = new ArrayList<ClassifiedAd>();
 		
 		// TODO: add criteria query
 		
 		return results;
 	}
 	
 	public Integer countFilterAdvertisements(FilterParameters parameters) {
 		
 		// TODO: add criteria query
 		
 		return 0;
 	}
 }
