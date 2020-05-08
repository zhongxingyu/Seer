 package vinna;
 
 public class Validation {
     private boolean hasErrors = false;
     private Model model = new Model();
 
     public Validation required(String value, String name) {
         if (value == null || value.trim().isEmpty()) {
             hasErrors = true;
            model.put("error-" + name, Messages.format("vinna.required", name));
         }
         return this;
     }
 
     //TODO: moar validations
 
     public void mergeInto(Model model) {
         model.merge(this.model);
     }
 
     public Model getModel() {
         return model;
     }
 
     public boolean hasErrors() {
         return hasErrors;
     }
 }
