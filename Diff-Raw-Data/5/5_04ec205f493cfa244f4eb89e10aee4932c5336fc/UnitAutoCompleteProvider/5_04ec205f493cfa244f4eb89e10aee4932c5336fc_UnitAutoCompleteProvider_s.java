 package pt.ist.expenditureTrackingSystem.presentationTier.renderers.autoCompleteProvider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 public class UnitAutoCompleteProvider implements AutoCompleteProvider {
 
     public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
 	final List<Unit> units = new ArrayList<Unit>();
 
 	final String trimmedValue = value.trim();
 
 	if (StringUtils.isNumeric(trimmedValue)) {
 	    final int code = Integer.parseInt(trimmedValue);
 	    for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnits()) {
 		if (unit instanceof CostCenter) {
 		    final CostCenter costCenter = (CostCenter) unit;
 		    final String unitCode = costCenter.getCostCenter();
		    if (unitCode != null && code == Integer.parseInt(unitCode)) {
 			units.add(unit);
 		    }
 		} else if (unit instanceof Project) {
 		    final Project project = (Project) unit;
 		    final String unitCode = project.getProjectCode();
		    if (unitCode != null && code == Integer.parseInt(unitCode)) {
 			if (unit.hasAnySubUnits()) {
 			    addAllSubUnits(units, unit);
 			} else {
 			    units.add(unit);
 			}
 		    }
 		}
 	    }
 	} else {
 	    final String[] input = trimmedValue.split(" ");
 	    StringNormalizer.normalize(input);
 
 	    for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnits()) {
 		if (unit instanceof CostCenter || unit instanceof Project) {
 		    final String unitName = StringNormalizer.normalize(unit.getName());
 		    if (hasMatch(input, unitName)) {
 			units.add(unit);
 		    }
 		}
 	    }
 	}
 
 	Collections.sort(units, Unit.COMPARATOR_BY_PRESENTATION_NAME);
 
 	return units;
     }
 
     private void addAllSubUnits(final List<Unit> units, final Unit unit) {
 	for (final Unit subUnit : unit.getSubUnitsSet()) {
 	    units.add(subUnit);
 	    addAllSubUnits(units, subUnit);
 	}
     }
 
     private boolean hasMatch(final String[] input, final String unitNameParts) {
 	for (final String namePart : input) {
 	    if (unitNameParts.indexOf(namePart) == -1) {
 		return false;
 	    }
 	}
 	return true;
     }
 
 }
