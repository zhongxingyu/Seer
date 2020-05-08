 package pt.ist.expenditureTrackingSystem.presentationTier.renderers.autoCompleteProvider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference;
 
 public class CPVAutoCompleteProvider implements AutoCompleteProvider {
 
     public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
 	List<CPVReference> result = new ArrayList<CPVReference>();
 	
	String[] values = value.split(" ");
 	for (final CPVReference cpvCode : ExpenditureTrackingSystem.getInstance().getCPVReferences()) {
	    if (cpvCode.getCode().startsWith(value) || match(cpvCode.getDescription(), values)) {
 		result.add(cpvCode);
 	    }
 	}
 	return result;
     }
 
     private boolean match(String description, String[] inputParts) {
 	
 	for (final String namePart : inputParts) {
 	    if (description.indexOf(namePart) == -1) {
 		return false;
 	    }
 	}
 	return true;
     }
 
 }
