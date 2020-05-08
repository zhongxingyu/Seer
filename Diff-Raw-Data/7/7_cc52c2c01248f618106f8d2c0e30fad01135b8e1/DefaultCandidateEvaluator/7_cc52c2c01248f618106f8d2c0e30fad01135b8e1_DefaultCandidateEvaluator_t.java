 package org.rosenvold.spring.convention.candidateevaluators;
 
 import org.rosenvold.spring.convention.CandidateEvaluator;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Kristian Rosenvold
  */
 public class DefaultCandidateEvaluator implements CandidateEvaluator {
     public boolean isBean(Class prospect) {
         //noinspection unchecked
         return prospect.isAnnotationPresent(Repository.class) ||
                 prospect.isAnnotationPresent(Component.class) ||
                 prospect.isAnnotationPresent(Service.class) ||
                 prospect.isAnnotationPresent(Controller.class);
     }
 }
