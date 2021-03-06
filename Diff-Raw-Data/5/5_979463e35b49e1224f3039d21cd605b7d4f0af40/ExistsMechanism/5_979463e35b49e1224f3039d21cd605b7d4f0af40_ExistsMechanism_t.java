 /****************************************************************
  * Licensed to the Apache Software Foundation (ASF) under one   *
  * or more contributor license agreements.  See the NOTICE file *
  * distributed with this work for additional information        *
  * regarding copyright ownership.  The ASF licenses this file   *
  * to you under the Apache License, Version 2.0 (the            *
  * "License"); you may not use this file except in compliance   *
  * with the License.  You may obtain a copy of the License at   *
  *                                                              *
  *   http://www.apache.org/licenses/LICENSE-2.0                 *
  *                                                              *
  * Unless required by applicable law or agreed to in writing,   *
  * software distributed under the License is distributed on an  *
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
  * KIND, either express or implied.  See the License for the    *
  * specific language governing permissions and limitations      *
  * under the License.                                           *
  ****************************************************************/
 
 
 package org.apache.james.jspf.terms;
 
 import org.apache.james.jspf.core.DNSService;
 import org.apache.james.jspf.core.SPF1Data;
 import org.apache.james.jspf.exceptions.PermErrorException;
 import org.apache.james.jspf.exceptions.TempErrorException;
 import org.apache.james.jspf.util.SPFTermsRegexps;
 import org.apache.james.jspf.wiring.DNSServiceEnabled;
 
 import java.util.List;
 
 /**
  * This class represent the exists mechanism
  * 
  */
 public class ExistsMechanism extends GenericMechanism implements DNSServiceEnabled {
 
     /**
      * ABNF: exists = "exists" ":" domain-spec
      */
     public static final String REGEX = "[eE][xX][iI][sS][tT][sS]" + "\\:"
             + SPFTermsRegexps.DOMAIN_SPEC_REGEX;
     
     private DNSService dnsService;
 
     /**
      * 
      * @see org.apache.james.jspf.core.GenericMechanism#run(org.apache.james.jspf.core.SPF1Data)
      */
     public boolean run(SPF1Data spfData) throws PermErrorException,
             TempErrorException {
         List aRecords;
 
         // update currentDepth
         spfData.increaseCurrentDepth();
 
         String host = expandHost(spfData);
 
         try {
             aRecords = dnsService.getRecords(host,DNSService.A);
         } catch (DNSService.TimeoutException e) {
             return false;
         }
        
         if (aRecords != null && aRecords.size() > 0) {
             return true;
         }
 
         // No match found
         return false;
     }
     
     /**
      * @see java.lang.Object#toString()
      */
     public String toString() {
         return "exists:"+getDomain();
     }
 
     /**
      * @see org.apache.james.jspf.wiring.DNSServiceEnabled#enableDNSService(org.apache.james.jspf.core.DNSService)
      */
     public void enableDNSService(DNSService service) {
         this.dnsService = service;
     }
 
 
 }
