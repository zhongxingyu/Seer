 package de.hswt.hrm.inspection.model;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkArgument;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class BiologicalRating 
 	implements Rating
 {
 
     public static final String AIR_GERMS_CONCENTRATION = "AIR_GERMS_CONCENTRATION";
 
     public static final String CONTACT_CULTURE = "CONTACT_CULTURE";
 
     private final int id;
     private SchemeComponent component;
     private Inspection inspection;
     private int bacteriaCount;
     private int rating;
     private int quantifier;
     private String comment;
     private String flag;
     private SamplingPointType samplingPointType;
 
     // private static final String IS_MANDATORY = "Field is a mandatory.";
     private static final String INVALID_NUMBER = "%s is an invalid number. Must be greater or equals 0.";
 
     public BiologicalRating(int id, Inspection inspection, SchemeComponent component,
             int bacteriaCount, int rating, int quantifier, String comment, String flag) {
         this.id = id;
         samplingPointType = null;
         setInspection(inspection);
         setComponent(component);
         setBacteriaCount(bacteriaCount);
         setRating(rating);
         setQuantifier(quantifier);
         setComment(comment);
         setFlag(flag);
     }
 
     public BiologicalRating(Inspection inspection, SchemeComponent component) {
         this(inspection, component, 0, 0, 0, null, null);
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
         return bacteriaCount;
     }
 
     public void setBacteriaCount(int bacteriaCount) {
     	checkArgument(bacteriaCount >= 0, INVALID_NUMBER, bacteriaCount);
         this.bacteriaCount = bacteriaCount;
     }
 
     public int getRating() {
         return rating;
     }
 
     public void setRating(int rating) {
     	checkArgument(rating >= 0, INVALID_NUMBER, rating);
         this.rating = rating;
     }
 
     public int getQuantifier() {
         return quantifier;
     }
 
     public void setQuantifier(int quantifier) {
     	checkArgument(quantifier >= 0, INVALID_NUMBER, quantifier);
         this.quantifier = quantifier;
     }
 
     public Optional<String> getComment() {
         return Optional.fromNullable(comment);
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public Optional<String> getFlag() {
         return Optional.fromNullable(flag);
     }
 
     public void setFlag(String flag) {
         this.flag = flag;
     }
 
     public int getId() {
         return id;
     }
 
     public Optional<SamplingPointType> getSamplingPointType() {
         return Optional.fromNullable(samplingPointType);
     }
 
     public void setSamplingPointType(SamplingPointType samplingPointType) {
         this.samplingPointType = samplingPointType;
     }
 
     public boolean isValid() {
         return getBacteriaCount() >= 0 
         		&& getRating() >= 0 
         		&& getQuantifier() >= 0
                 && !getFlag().or("").isEmpty(); 
     }
 
     public boolean isAirGermsConcentration() {
        return getFlag().or("").equals(BiologicalRating.AIR_GERMS_CONCENTRATION);
     }
 
     public boolean isContactCultures() {
        return getFlag().or("").equals(BiologicalRating.CONTACT_CULTURE);
     }
 
     @Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + bacteriaCount;
 		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
 		result = prime * result
 				+ ((component == null) ? 0 : component.hashCode());
 		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
 		result = prime * result + id;
 		result = prime * result
 				+ ((inspection == null) ? 0 : inspection.hashCode());
 		result = prime * result + quantifier;
 		result = prime * result + rating;
 		result = prime
 				* result
 				+ ((samplingPointType == null) ? 0 : samplingPointType
 						.hashCode());
 		return result;
 	}
 
     @Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BiologicalRating other = (BiologicalRating) obj;
 		if (bacteriaCount != other.bacteriaCount)
 			return false;
 		if (comment == null) {
 			if (other.comment != null)
 				return false;
 		} else if (!comment.equals(other.comment))
 			return false;
 		if (component == null) {
 			if (other.component != null)
 				return false;
 		} else if (!component.equals(other.component))
 			return false;
 		if (flag == null) {
 			if (other.flag != null)
 				return false;
 		} else if (!flag.equals(other.flag))
 			return false;
 		if (id != other.id)
 			return false;
 		if (inspection == null) {
 			if (other.inspection != null)
 				return false;
 		} else if (!inspection.equals(other.inspection))
 			return false;
 		if (quantifier != other.quantifier)
 			return false;
 		if (rating != other.rating)
 			return false;
 		if (samplingPointType != other.samplingPointType)
 			return false;
 		return true;
 	}
 
 }
