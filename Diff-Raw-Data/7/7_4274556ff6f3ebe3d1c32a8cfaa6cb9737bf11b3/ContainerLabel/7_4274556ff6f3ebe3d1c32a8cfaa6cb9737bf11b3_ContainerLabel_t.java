 package solution;
 
 /**
  * @author Jacob Evans, Bodaniel Jeanes
  */
 
 public class ContainerLabel {
 
     int identifier, kind, kindLength, identifierLength;
 
     public ContainerLabel(Integer a_kind, Integer a_kindLength,
             Integer a_identifier, Integer a_identifierLength)
             throws LabelException, IllegalArgumentException {
 
         // No null parameters supported
         if ((a_kind == null) || (a_identifier == null)
                 || (a_kindLength == null) || (a_identifierLength == null)) {
             throw new IllegalArgumentException(
                     "Cannot give null values for any parameters");
         }
 
         // No negative numbers supported
        if ((a_kind < 0) || (a_identifier < 0) || (a_kindLength <= 0)
                || (a_identifierLength <= 0)) {
            throw new LabelException(
                    "Cannot give negative values for kind/identifier or zero lengths");
         }
 
         // Check that the label kind and identifier are valid range
         if ((a_kindLength > 3) || (a_identifierLength > 5)) {
             throw new LabelException("Cannot assign that high a value");
         }
 
         // TODO test to see if the length is larger than allowed values, smaller
         // than the length
 
         identifier = a_identifier;
         kind = a_kind;
         kindLength = a_kindLength;
         identifierLength = a_identifierLength;
 
     }
 
     Integer getIdentifier() {
         return identifier;
     }
 
     Integer getKind() {
         return kind;
     }
 
     public boolean matches(ContainerLabel a_label)
             throws IllegalArgumentException {
         if (a_label == null) {
             throw new IllegalArgumentException();
         }
 
         if ((getKind() == a_label.getKind())
                 && (getIdentifier() == a_label.getIdentifier())) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public String toString() {
         return String.format("%1$03d%2$05d", kind, identifier);
     }
 }
