 /**
  * 
  */
 package app;
 
 import java.util.ArrayList;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class RecognisedResource {
 	private final int LANGUAGE_TAG_LENGTH = 3;
 	private final String LANGUAGE_TAG = "@";
 	private final String LANG = "@en";
	private final String[] LABELS_TO_IGNORE = {"thing, Thing"};
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String uri;
     @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String resourceLabel;
     @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> typeLabels;
 
     public RecognisedResource(String uri, String resourceLabel) {
            this.setUri(uri);
            this.setResourceLabel(resourceLabel);
            this.setTypeLabels(new ArrayList<String>());
     }
     
     public String getConcatenatedTypeLabels() {
     	if(this.getTypeLabels() == null)
     		return "";
     	String concatenatedTypeLabels = this.getTypeLabels().get(0);
     	for(int i = 1; i < this.getTypeLabels().size(); i++)
     		concatenatedTypeLabels += ", " + this.getTypeLabels().get(i);
     	
     	return concatenatedTypeLabels;
     }
     
     public String getFirstTypeLabel() {
     	if(this.getTypeLabels() == null)
     		return null;
     	return this.getTypeLabels().get(0);
     }
     
     public void addTypeLabel(String typeLabel) {
     	if(isNonEnglishLabel(typeLabel))
     		return; // ignore non-English language tags
     	String label = stripLanguageTag(typeLabel);
     	if(this.getTypeLabels().contains(label))
     		return; // ignore duplicate labels
     	
     	for(int i = 0; i < this.LABELS_TO_IGNORE.length; i++) {
    		if(this.LABELS_TO_IGNORE[i].equals(label))
     			return; // ignore the set of unwanted labels
     	}
     		
     	this.getTypeLabels().add(label);
     }
     
     /**
      * isNonEnglishLabel
      * @param label
      * @return true if the label has a language tag and the language specified is not English. Otherwise, return true
      * (i.e. returns true if the label is an English label or if it has no associated language tag)
      */
 	private boolean isNonEnglishLabel(String label) {
 			int positionOfLanguageTag = label.length() - this.LANGUAGE_TAG_LENGTH;
 			if(label.length() > this.LANGUAGE_TAG_LENGTH) {
 				if(label.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(this.LANGUAGE_TAG) 
 					&& !label.substring(positionOfLanguageTag, label.length()).equals(this.LANG))
 						return true;
 				else return false;
 			}
 			return false;
 	}
 	
 	/* This method is duplicated in other classes!
 	 *
 	 */
 	private String stripLanguageTag(String solutionText) {
 		int positionOfLanguageTag = solutionText.length() - LANGUAGE_TAG_LENGTH;
 		if(solutionText.length() > LANGUAGE_TAG_LENGTH) {
 			if(solutionText.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(LANGUAGE_TAG))
 				return solutionText.substring(0, positionOfLanguageTag);
 		}
 		return solutionText;
 	}
 }
