 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.brekka.stillingar.test.intg.services.impl;
 
 import static java.lang.String.format;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import org.brekka.stillingar.annotations.ConfigurationListener;
 import org.brekka.stillingar.annotations.Configured;
 import org.brekka.stillingar.test.intg.services.BusinessService;
 import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyX;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 @Configured
 @Service
 public class BusinessServiceImpl implements BusinessService {
 
 	@Configured("//c:Fraud/c:TriggerFactor")
 	private BigDecimal frequency;
 	
 	private int configureCount = 0;
 	
 	private Date configureDate;
 	
	private CompanyX configureBusiness;
 
 	// Business method
 	public void doSomething() {
 	    System.out.println("Did something");
 	}
 	
 	@ConfigurationListener
 	public void configure(@Qualifier("dynamicBean") 
 	                      Date theDate, 
 	                      @Configured 
	                      CompanyX business) {
 	    configureCount++;
 	    this.configureDate = theDate;
 	    this.configureBusiness = business;
 	}
 	
 	public int getConfigureCount() {
         return configureCount;
     }
 	
	public CompanyX getConfigureBusiness() {
         return configureBusiness;
     }
 	
 	public Date getConfigureDate() {
         return configureDate;
     }
 	
 	public BigDecimal getFrequency() {
         return frequency;
     }
 	
 	@Override
 	public String toString() {
 	    return format("%s", getFrequency());
 	}
 }
