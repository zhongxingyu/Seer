 /**
  * 
  */
 package module.mission.presentationTier.dto;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Formatter;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import module.mission.domain.Mission;
 import module.mission.domain.util.SearchMissions;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.apache.commons.collections.comparators.ReverseComparator;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * @author Shezad Anavarali Date: Aug 25, 2009
  * 
  */
 public class SearchMissionsDTO extends SearchMissions {
 
     private String sortBy;
 
     private String orderBy;
 
     public SearchMissionsDTO() {
 	super();
     }
 
     public SearchMissionsDTO(final HttpServletRequest request) {
	setProcessNumber(StringUtils.isEmpty(request.getParameter("processNumber")) ? "" : request.getParameter("processNumber"));
 	setMissionResponsible(StringUtils.isEmpty(request.getParameter("ruOID")) ? null : (Party) Party.fromExternalId(request
 		.getParameter("ruOID")));
 	setPayingUnit(StringUtils.isEmpty(request.getParameter("puOID")) ? null : (Unit) Unit.fromExternalId(request
 		.getParameter("puOID")));
 	setForeign(StringUtils.isEmpty(request.getParameter("f")) ? null : Boolean.valueOf(request.getParameter("f")));
 	setDate(StringUtils.isEmpty(request.getParameter("d")) ? null : new LocalDate(Long.valueOf(request.getParameter("d"))));
 	setRequestingPerson(StringUtils.isEmpty(request.getParameter("rpOID")) ? null : (Person) Person.fromExternalId(request
 		.getParameter("rpOID")));
 	setParticipant(StringUtils.isEmpty(request.getParameter("pOID")) ? null : (Person) Person.fromExternalId(request
 		.getParameter("pOID")));
 
 	final String sortByParameter = request.getParameter("sortBy");
 	if (!StringUtils.isEmpty(sortByParameter) && sortByParameter.indexOf('=') != -1) {
 	    final String[] splittedSort = sortByParameter.split("=");
 	    if (splittedSort.length > 1) {
 		this.sortBy = splittedSort[0];
 		this.orderBy = splittedSort[1];
 	    }
 	}
 
     }
 
     public List<Mission> sortedSearch() {
 	ArrayList<Mission> results = new ArrayList<Mission>(super.search());
 	Collections.sort(results, getComparator());
 	return results;
     }
 
     private Comparator<Mission> getComparator() {
 	if (!StringUtils.isEmpty(this.sortBy)) {
 	    Comparator<Mission> comparator = new BeanComparator(this.sortBy, new NullComparator());
 	    if (!StringUtils.isEmpty(this.orderBy) && this.orderBy.startsWith("desc")) {
 		return new ReverseComparator(comparator);
 	    }
 	    return comparator;
 	}
 	return new BeanComparator("missionProcess.processIdentification");
     }
 
     public String getRequestParameters() {
	return new Formatter().format("processNumber=%s&ruOID=%s&puOID=%s&f=%s&d=%s&rpOID=%s&pOID=%s", getProcessNumber(),
		getRequestingUnitParameter(), getPayingUnitParameter(), getForeignParameter(), getDateParameter(),
		getRequestingPersonParameter(), getParticipantParameter(), getSortByParameter(), getOrderByParameter())
		.toString();
     }
 
     public String getRequestParametersWithSort() {
 	return getRequestParameters()
 		+ new Formatter().format("&sortBy=%s=%s", getSortByParameter(), getOrderByParameter()).toString();
     }
 
     private String getSortByParameter() {
 	return StringUtils.isEmpty(sortBy) ? StringUtils.EMPTY : sortBy;
     }
 
     private String getOrderByParameter() {
 	return StringUtils.isEmpty(orderBy) ? StringUtils.EMPTY : orderBy;
     }
 
     public String getRequestingUnitParameter() {
 	return getMissionResponsible() != null ? getMissionResponsible().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getPayingUnitParameter() {
 	return getPayingUnit() != null ? getPayingUnit().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getForeignParameter() {
 	return getForeign() != null ? getForeign().toString() : StringUtils.EMPTY;
     }
 
     public String getDateParameter() {
 	return getDate() != null ? String.valueOf(getDate().toDateTimeAtStartOfDay().getMillis()) : StringUtils.EMPTY;
     }
 
     public String getRequestingPersonParameter() {
 	return getRequestingPerson() != null ? getRequestingPerson().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getParticipantParameter() {
 	return getParticipant() != null ? getParticipant().getExternalId() : StringUtils.EMPTY;
     }
 
 }
