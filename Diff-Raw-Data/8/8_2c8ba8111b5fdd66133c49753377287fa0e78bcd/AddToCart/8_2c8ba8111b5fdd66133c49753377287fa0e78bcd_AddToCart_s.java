 package forms;
 
 import play.data.validation.Constraints;
 
 public class AddToCart {
 
    @Constraints.Required(message = "Variant required")
     public int variantId;
 
    @Constraints.Required(message = "How often required")
    @Constraints.Pattern(value = "1|2|4")
     public int howOften;
 
 
     public AddToCart() {
 
     }
 
 }
