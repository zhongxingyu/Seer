 /*
  * Copyright 2001-2004 The Apache Software Foundation.
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
 
 package org.apache.ws.policy;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 import org.apache.ws.policy.util.PolicyRegistry;
 
 /**
  * Policy is the access point for policy framework. It the object model that
  * represents a policy at runtime.
  *  
  */
 public class Policy extends AndCompositeAssertion implements Assertion {
     private Log log = LogFactory.getLog(this.getClass().getName());
 
     private String xmlBase = null;
 
     private String id = null;
 
     public Policy() {
     }
 
     public Policy(String id) {
         this(null, id);
         setNormalized(false);
     }
 
     public Policy(String xmlBase, String id) {
         this.xmlBase = xmlBase;
         this.id = id;
         setNormalized(false);
     }
 
     public void setBase(String xmlBase) {
         this.xmlBase = xmlBase;
     }
 
     public String getBase() {
         return xmlBase;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public String getPolicyURI() {
     	if (id != null) {
     		if (xmlBase != null) {
     			return xmlBase + "#" + id;
     		}
     		return "#" + id;
     	}
     	return null;
     }
 
     public Assertion normalize() {
         return normalize(null);
     }
 
     public Assertion normalize(PolicyRegistry reg) {
         log.debug("Enter: Policy::normalize");
 
         if (isNormalized()) {
             return this;
         }
 
         String xmlBase = getBase();
         String id = getId();
         Policy policy = new Policy(xmlBase, id);
 
         AndCompositeAssertion AND = new AndCompositeAssertion();
         XorCompositeAssertion XOR = new XorCompositeAssertion();
 
         ArrayList childAndTermList = new ArrayList();
         ArrayList childXorTermList = new ArrayList();
 
         Iterator terms = getTerms().iterator();
         Assertion term;
 
         while (terms.hasNext()) {
             term = (Assertion) terms.next();
             term = term.normalize(reg);
 
             if (term instanceof Policy) {
                 XorCompositeAssertion Xor = (XorCompositeAssertion) ((Policy) term)
                         .getTerms().get(0);
                 
                 if (Xor.size() != 1) {
                     term = Xor;
 
                 } else {
                     AND
                             .addTerms(((AndCompositeAssertion) Xor.getTerms()
                                     .get(0)).getTerms());
                     continue;
                 }
             }
 
             if (term instanceof XorCompositeAssertion) {
                 
                 if (((XorCompositeAssertion) term).isEmpty()) {
                     XorCompositeAssertion emptyXor = new XorCompositeAssertion();
                     emptyXor.setNormalized(true);
 
                     policy.addTerm(emptyXor);
                     policy.setNormalized(true);
 
                     return policy;
                 }
 
                 childXorTermList.add(term);
                 continue;
             }
 
             if (term instanceof AndCompositeAssertion) {
 
                 if (((AndCompositeAssertion) term).isEmpty()) {
                     AndCompositeAssertion emptyAnd = new AndCompositeAssertion();
                     XOR.addTerm(emptyAnd);
 
                 } else {
                     AND.addTerms(((AndCompositeAssertion) term).getTerms());
                 }
                 continue;
             }
             AND.addTerm((Assertion) term);
         }
 
         // processing child-XORCompositeAssertions
         if (childXorTermList.size() > 1) {
 
             for (int i = 0; i < childXorTermList.size(); i++) {
                 
                 for (int j = i; j < childXorTermList.size(); j++) {
 
                     if (i != j) {
                         XorCompositeAssertion xorTermA = (XorCompositeAssertion) childXorTermList
                                 .get(i);
                         XorCompositeAssertion xorTermB = (XorCompositeAssertion) childXorTermList
                                 .get(j);
 
                         Iterator iterA = xorTermA.getTerms().iterator();
 
                         while (iterA.hasNext()) {
                             CompositeAssertion andTermA = (CompositeAssertion) iterA
                                     .next();
 
                             Iterator iterB = xorTermB.getTerms().iterator();
 
                             while (iterB.hasNext()) {
                                 CompositeAssertion andTermB = (CompositeAssertion) iterB
                                         .next();
                                 AndCompositeAssertion anAndTerm = new AndCompositeAssertion();
                                 anAndTerm.addTerms(andTermA.getTerms());
                                 anAndTerm.addTerms(andTermB.getTerms());
                                 XOR.addTerm(anAndTerm);
                             }
                         }
                     }
                 }
             }
 
         } else if (childXorTermList.size() == 1) {
             CompositeAssertion xorTerm = (CompositeAssertion) childXorTermList
                     .get(0);
             XOR.addTerms(xorTerm.getTerms());
         }
 
         if (childXorTermList.isEmpty()) {
             XorCompositeAssertion xor = new XorCompositeAssertion();
 
             xor.addTerm(AND);
             policy.addTerm(xor);
             policy.setNormalized(true);
             return policy;
         }
 
         List primTerms = AND.getTerms();
         Iterator andTerms = XOR.getTerms().iterator();
 
         while (andTerms.hasNext()) {
             CompositeAssertion anAndTerm = (CompositeAssertion) andTerms.next();
             anAndTerm.addTerms(primTerms);
         }
 
         policy.addTerm(XOR);
         policy.setNormalized(true);
         return policy;
     }
 
     public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
         log.debug("Enter: Policy::intersect");
 
         Policy result = new Policy(getBase(), getId());
         Policy normalizedMe = (Policy) ((isNormalized()) ? this
                 : normalize(reg));
 
         XorCompositeAssertion alters = (XorCompositeAssertion) normalizedMe
                 .getTerms().get(0);
 
         if (assertion instanceof PrimitiveAssertion) {
             result.addTerm(alters.intersect(assertion, reg));
             return result;
 
         } else {
             CompositeAssertion target = (CompositeAssertion) assertion;
             target = (CompositeAssertion) ((target.isNormalized()) ? target
                     : target.normalize(reg));
 
             if (target instanceof Policy) {
                 XorCompositeAssertion alters2 = (XorCompositeAssertion) target
                         .getTerms().get(0);
                 result.addTerm(alters.intersect(alters2));
                 return result;
             } else {
                 result.addTerm(alters.intersect(target));
                 return result;
             }
         }
     }
 
     public Assertion merge(Assertion assertion, PolicyRegistry reg) {
         log.debug("Enter: Policy::merge");
 
         Policy result = new Policy();
         Policy normalizedMe = (Policy) ((isNormalized()) ? this
                 : normalize(reg));
         XorCompositeAssertion alters = (XorCompositeAssertion) normalizedMe
                 .getTerms().get(0);
         Assertion test = alters.merge(assertion, reg);
 
         result.addTerm(test);
         result.setNormalized(true);
         return result;
     }
 }
