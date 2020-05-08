 package net.sf.laja.cdd.validator;
 
 import com.google.common.collect.ImmutableSet;
 
 import java.util.Iterator;
 
 public class ValidationErrors implements Iterable<ValidationErrors.ValidationError> {
     private final ImmutableSet<ValidationError> errors;
 
     private static final String NULL_ERROR = "is_null";
     private static final String TYPE_CONVERSION_ERROR = "type_conversion";
 
     public static String concatenate(String parent, String attribute) {
         return parent == null || parent.isEmpty() ? attribute : parent + "." + attribute;
     }
 
     public static Builder builder() {
         return new Builder();
     }
 
     private ValidationErrors(ImmutableSet<ValidationError> errors) {
         this.errors = errors;
     }
 
     public Iterator<ValidationError> iterator() {
         return errors.iterator();
     }
 
     public boolean isValid() {
         return errors.isEmpty();
     }
 
     public boolean isInvalid() {
         return !isValid();
     }
 
     public int size() {
         return errors.size();
     }
 
     public ValidationError first() {
         return iterator().next();
     }
 
     public String getErrorMessage() {
         return errors.toString();
     }
 
     @Override
     public int hashCode() {
         return errors != null ? errors.hashCode() : 0;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof ValidationErrors)) return false;
 
         ValidationErrors that = (ValidationErrors) obj;
 
         if (errors != null ? !errors.equals(that.errors) : that.errors != null) return false;
 
         return true;
     }
 
     @Override
     public String toString() {
         return errors.toString();
     }
 
     public static class Builder {
         private int size;
         private final ImmutableSet.Builder<ValidationError> errors = ImmutableSet.<ValidationError>builder();
 
         private Builder() {
         }
 
         public ValidationErrors build() {
             return new ValidationErrors(errors.build());
         }
 
         public Builder addIsNullError(Object rootElement, String attribute, String parent) {
             return addError(rootElement, attribute, NULL_ERROR, parent);
         }
 
         public Builder addTypeConversionError(Object rootElement, String attribute, String parent) {
             return addError(rootElement, attribute, TYPE_CONVERSION_ERROR, parent);
         }
 
         public Builder addError(Object rootElement, String attribute, String errorType, String parent) {
             size++;
             errors.add(new ValidationError(concatenate(parent, attribute), errorType, rootElement));
             return this;
         }
 
         public boolean isEmpty() {
             return size == 0;
         }
 
         public int size() {
             return size;
         }
     }
 
     public static class ValidationError implements Comparable<ValidationError> {
         public final String attribute;
         public final String errorType;
         public final Object element;
 
         public ValidationError(String attribute, String errorType, Object element) {
             this.attribute = attribute;
             this.errorType = errorType;
             this.element = element;
         }
 
         public boolean isSameAs(String attribute, String errorType) {
             return this.attribute.equals(attribute) && this.errorType.equals(errorType);
         }
 
         public boolean isNullError(String attribute) {
             return isSameAs(attribute, NULL_ERROR);
         }
 
         public boolean isTypeConversionError(String attribute) {
             return isSameAs(attribute, TYPE_CONVERSION_ERROR);
         }
 
         public int compareTo(ValidationError error) {
             return (attribute + ":" + errorType).compareTo(error.attribute + ":" + error.errorType);
         }
 
         @Override
         public int hashCode() {
             int result = attribute != null ? attribute.hashCode() : 0;
             result = 31 * result + (errorType != null ? errorType.hashCode() : 0);
             return result;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof ValidationError)) return false;
 
             ValidationError that = (ValidationError) o;
 
             if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null) return false;
             if (errorType != null ? !errorType.equals(that.errorType) : that.errorType != null) return false;
 
             return true;
         }
 
         @Override
         public String toString() {
             return "ValidationError{" +
                     "attribute='" + attribute + '\'' +
                     ", errorType='" + errorType + '\'' +
                     '}';
         }
     }
 }
