 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.neethi.All;
 import org.apache.neethi.Assertion;
 import org.apache.neethi.ExactlyOne;
 import org.apache.neethi.IntersectableAssertion;
 import org.apache.neethi.Policy;
 import org.apache.neethi.PolicyComponent;
 import org.apache.neethi.PolicyContainingAssertion;
 
 /**
  * This class contains methods dealing with policy intersection.
  * Intersection of two assertions, i.e. computation if a compatible assertion,
  * is domain specific and relies on AssertionBuilder.buildCompatible.
  * See Section 4.5 in http://www.w3.org/TR/2006/WD-ws-policy-20061117.
  */
 public class PolicyIntersector {
     
     private boolean strict;
     
     public PolicyIntersector() {
         strict = true;
     }
     public PolicyIntersector(boolean s) {
         strict = s;
     }
     
     public boolean isStrict() {
         return strict;
     }
 
     public void setStrict(boolean s) {
         strict = s;
     }
 
     private Assertion intersect(Assertion a1, Assertion a2) {
         if (a1 instanceof IntersectableAssertion) {
             if (!((IntersectableAssertion)a1).isCompatible(a2, strict)) {
                 return null;
             }
             return ((IntersectableAssertion)a1).intersect(a2, strict);
         }
         //the assertion doesn't implement IntersectableAssertion so we
         //need to try doing a basic intersect ourself
         if (a1.getName().equals(a2.getName())) {
             if (a1 instanceof PolicyContainingAssertion 
                 && a2 instanceof PolicyContainingAssertion) {
                 PolicyContainingAssertion pc1 = (PolicyContainingAssertion)a1;
                 PolicyContainingAssertion pc2 = (PolicyContainingAssertion)a2;
                 Policy p1 = pc1.getPolicy();
                 Policy p2 = pc2.getPolicy();
                 PolicyIntersector pi = new PolicyIntersector(strict);
                 if (pi.compatiblePolicies(p1, p2)) {
                     return a1;
                 }
             } else {
                 return a1;
             }
         }
         return null;
     }
     private Assertion findCompatibleAssertion(Assertion assertion, 
                                               Collection<? extends PolicyComponent> alt,
                                               boolean remove) {
         Iterator<? extends PolicyComponent> iterator = alt.iterator();
         while (iterator.hasNext()) {
             PolicyComponent a = iterator.next();
             if (a instanceof Assertion) {
                 Assertion compatible = intersect(assertion, (Assertion)a);
                 if (null != compatible) {
                     if (remove) {
                         iterator.remove();
                     }
                     return compatible;
                 }
             }
         }
         return null;
     } 
     
     
     boolean compatibleAlternatives(Collection<? extends PolicyComponent> alt1, 
                                    Collection<? extends PolicyComponent> alt2) {
         if (alt1.isEmpty() && alt2.isEmpty()) {
             return true;
         }
         
         All all = createCompatibleAlternatives(alt1, alt2, true);
         if (all == null) {
             return false;
         }
         return !all.getAssertions().isEmpty();
     }
     
     All createCompatibleAlternatives(Collection<? extends PolicyComponent> alt1, 
                                      Collection<? extends PolicyComponent> alt2,
                                      boolean remove) {
         All all = new All();
         if (alt1.isEmpty() && alt2.isEmpty()) {
             return all;
         }
         
         alt1 = new ArrayList<PolicyComponent>(alt1);
         alt2 = new ArrayList<PolicyComponent>(alt2);
         
         Iterator<? extends PolicyComponent> iterator = alt1.iterator();
         while (iterator.hasNext()) {
             PolicyComponent a1 = iterator.next();
             if (a1 instanceof Assertion) {
                 Assertion assertion = findCompatibleAssertion((Assertion)a1, alt2, remove);
                 if (assertion != null) {
                     if (remove) {
                         iterator.remove();
                     }
                     all.addPolicyComponent(assertion);
                 } else if (!strict && ((Assertion)a1).isIgnorable()) {
                     all.addPolicyComponent(a1);
                 } else if (strict || !((Assertion)a1).isIgnorable()) {
                     return null;
                 }
             }
         }
         iterator = alt2.iterator();
         while (iterator.hasNext()) {
             PolicyComponent a2 = iterator.next();
             if (a2 instanceof Assertion) {
                 Assertion assertion = findCompatibleAssertion((Assertion)a2, alt1, remove);
                 if (assertion != null) { 
                     all.addPolicyComponent(assertion);
                 } else if (!strict && ((Assertion)a2).isIgnorable()) {
                     all.addPolicyComponent(a2);
                 } else if (strict || !((Assertion)a2).isIgnorable()) {
                     return null;
                 }
             }
         }
         return all;
     }
     
     public boolean compatiblePolicies(Policy p1, Policy p2) {       
         Iterator<List<Assertion>> i1 = p1.getAlternatives();
         while (i1.hasNext()) {
             List<Assertion> alt1 = i1.next();
             Iterator<List<Assertion>> i2 = p2.getAlternatives();
             if (!i2.hasNext() && alt1.isEmpty()) {
                 return true;
             }
             while (i2.hasNext()) {                
                 List<Assertion> alt2 = i2.next();
                 if (compatibleAlternatives(alt1, alt2)) {
                     return true;                    
                 }
            }
         }        
        return false;
     }
     
     public Policy intersect(Policy p1, Policy p2) {
         return intersect(p1, p2, false);
     }
     public Policy intersect(Policy p1, Policy p2, boolean allowDups) {
         Policy compatible = new Policy(p1.getPolicyRegistry(), p1.getNamespace());
         ExactlyOne eo = new ExactlyOne(compatible);
         if (!compatiblePolicies(p1, p2)) {
             return compatible;
         }
         Iterator<List<Assertion>> i1 = p1.getAlternatives();
         while (i1.hasNext()) {
             List<Assertion> alt1 = i1.next();
             Iterator<List<Assertion>> i2 = p2.getAlternatives();
             while (i2.hasNext()) {                
                 List<Assertion> alt2 = i2.next();
                 All all = createCompatibleAlternatives(alt1, alt2, !allowDups);
                 if (all != null) {
                     eo.addPolicyComponent(all);
                 }
             }            
         }
         
         return compatible;
     }
     
 }
