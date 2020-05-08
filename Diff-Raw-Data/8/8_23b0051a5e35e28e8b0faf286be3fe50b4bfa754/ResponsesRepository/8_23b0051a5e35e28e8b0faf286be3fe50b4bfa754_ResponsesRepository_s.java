 package org.diveintojee.codestory2013;
 
 import com.google.common.collect.ImmutableMap;
 import org.springframework.stereotype.Component;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 public class ResponsesRepository {
 
     private static ImmutableMap<String, String> repository;
 
     static {
         repository = new ImmutableMap.Builder<String, String>()
                 .put("Quelle est ton adresse email", "louis.gueye@gmail.com")
                 .put("Es tu heureux de participer(OUI/NON)", "OUI")
                 .put("Es tu abonne a la mailing list(OUI/NON)", "OUI")
                 .put("Es tu pret a recevoir une enonce au format markdown par http post(OUI/NON)", "OUI")
                 .put("Est ce que tu reponds toujours oui(OUI/NON)", "NON")
                 .put("As tu bien recu le premier enonce(OUI/NON)", "OUI")
                 .build();
     }
 
     public String getAnswer(String question) {
         return repository.get(question);
     }
 }
