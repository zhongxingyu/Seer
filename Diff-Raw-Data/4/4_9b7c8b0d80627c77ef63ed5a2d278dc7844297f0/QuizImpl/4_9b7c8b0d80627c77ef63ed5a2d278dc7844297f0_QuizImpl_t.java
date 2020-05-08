 package com.flexyquiz.app.shared.func.model;
 
 import org.springframework.data.mongodb.core.mapping.Document;
 
 import com.flexyquiz.app.shared.core.model.AbstractPersistent;
import com.flexyquiz.app.shared.core.model.Persistent;
 
 @Document(collection = "quiz")
 public class QuizImpl extends AbstractPersistent implements Quiz {
   private String name;
   private String description;
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public String getDescription() {
     return description;
   }
 
   public void setDescription(String description) {
     this.description = description;
   }
 
   @Override
  public int compareTo(Persistent o) {
     return getName().compareTo(((QuizImpl) o).getName());
   }
 }
