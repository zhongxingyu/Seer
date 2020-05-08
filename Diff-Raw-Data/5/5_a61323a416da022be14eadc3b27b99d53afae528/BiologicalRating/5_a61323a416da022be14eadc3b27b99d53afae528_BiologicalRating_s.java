 package de.hswt.hrm.inspection.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class BiologicalRating {
 
     private final int id;
     private SchemeComponent component;
     private Inspection inspection;
     private Optional<Integer> bacteriaCount;
     private Optional<Integer> rating;
     private Optional<Integer> quantifier;
     private Optional<String> comment;
     private Optional<String> flag;
 
     //private static final String IS_MANDATORY = "Field is a mandatory.";
     //private static final String INVALID_NUMBER = "%d is an invalid number.%n Must be greater 0";
 
     public BiologicalRating(int id, Inspection inspection, SchemeComponent component, int bacteriaCount,
     		int rating, int quantifier, String comment, String flag) {
         this.id = id;
         setInspection(inspection);
         setComponent(component);
         setBacteriaCount(bacteriaCount);
         setRating(rating);
         setQuantifier(quantifier);
         setComment(comment);
         setFlag(flag);
     }
     
     public BiologicalRating(Inspection inspection, SchemeComponent component){
     	this(inspection, component, -1, -1, -1, null, null);
     }
 
     public BiologicalRating(Inspection inspection, SchemeComponent component, int bacteriaCount,
     		int rating, int quantifier, String comment, String flag) {
         this(-1, inspection, component, bacteriaCount, rating, quantifier, comment, flag);
     }
 
     public SchemeComponent getComponent() {
         return component;
     }
 
     public void setComponent(SchemeComponent component) {
         checkNotNull(component);
         this.component = component;
     }
 
     public Inspection getInspection() {
         return inspection;
     }
 
     public void setInspection(Inspection inspection) {
         checkNotNull(inspection);
         this.inspection = inspection;
     }
 
     public int getBacteriaCount() {
         return bacteriaCount.get();
     }
 
     public void setBacteriaCount(int bacteriaCount) {
         this.bacteriaCount = Optional.of(bacteriaCount);
     }
 
     public int getRating() {
         return rating.get();
     }
 
     public void setRating(int rating) {
         this.rating = Optional.of(rating);
     }
 
     public int getQuantifier() {
         return quantifier.get();
     }
 
     public void setQuantifier(int quantifier) {
         this.quantifier = Optional.of(quantifier);
     }
 
     public String getComment() {
         return comment.get();
     }
 
     public void setComment(String comment) {
         this.comment = Optional.of(comment);
     }
 
     public String getFlag() {
         return flag.get();
     }
 
     public void setFlag(String flag) {
         this.flag = Optional.of(flag);
     }
 
     public int getId() {
         return id;
     }
     
     public boolean isValid(){
     	return bacteriaCount.isPresent() &&
     		   rating.isPresent() &&
     		   quantifier.isPresent() &&
     		   comment.isPresent() &&
     		   flag.isPresent() &&
     		   bacteriaCount.get() > 0 &&
     		   rating.get() > 0 &&
     		   quantifier.get() > 0 &&
     		   !isNullOrEmpty(comment.get()) &&
     		   !isNullOrEmpty(flag.get());  
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + bacteriaCount.get();
        result = prime * result + ((comment == null) ? 0 : comment.get().hashCode());
         result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((flag == null) ? 0 : flag.get().hashCode());
         result = prime * result + id;
         result = prime * result + ((inspection == null) ? 0 : inspection.hashCode());
         result = prime * result + quantifier.get();
         result = prime * result + rating.get();
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         BiologicalRating other = (BiologicalRating) obj;
         if (bacteriaCount != other.bacteriaCount) {
             return false;
         }
         if (comment == null) {
             if (other.comment != null) {
                 return false;
             }
         }
         else if (!comment.equals(other.comment)) {
             return false;
         }
         if (component == null) {
             if (other.component != null) {
                 return false;
             }
         }
         else if (!component.equals(other.component)) {
             return false;
         }
         if (flag == null) {
             if (other.flag != null) {
                 return false;
             }
         }
         else if (!flag.equals(other.flag)) {
             return false;
         }
         if (id != other.id) {
             return false;
         }
         if (inspection == null) {
             if (other.inspection != null) {
                 return false;
             }
         }
         else if (!inspection.equals(other.inspection)) {
             return false;
         }
         if (quantifier != other.quantifier) {
             return false;
         }
         if (rating != other.rating) {
             return false;
         }
         return true;
     }
 
 }
