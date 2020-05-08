 package eu.dm2e.ws.api;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 
import eu.dm2e.utils.UriUtils;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 
 /**
  * Abstract Base Class for ConfigPojos.
  *
  * <p>
  * Every config, workflow or webservice, consists of assignments of values to parameters
  * </p>
  *
  * @param <T>  Type argument
  * @author Konstantin Baierer
  */
 public abstract class AbstractConfigPojo<T> extends AbstractPersistentPojo<T> implements IValidatable {
 
 	public abstract ParameterPojo getParamByName(String needle);
 
 	/**
 	 * Get ParameterAssignmentPojo for a specific parameter.
 	 *
 	 * @see ParameterPojo#matchesParameterName(String)
 	 * @param param  ParameterPojo to match
 	 * @return The ParameterAssignmentPojo if found or null otherwise
 	 * @see #getParameterAssignmentForParam(String)
 	 */
 	public ParameterAssignmentPojo getParameterAssignmentForParam(ParameterPojo param) {
 		return getParameterAssignmentForParam(param.getId());
 	}
 
 	/**
 	 * Get ParameterAssignmentPojo for a parameter by name.
 	 *
 	 * @see ParameterPojo#matchesParameterName(String)
 	 * @param paramName  parameter name to match
 	 * @return The ParameterAssignmentPojo if found or null otherwise
 	 */
 	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
 		log.info("Access to param assignment by name: " + paramName);
         for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
 			log.trace("Parameter being checked " + ass.getForParam());
 			if (ass.getForParam().matchesParameterName(paramName)) {
 				log.info("GOTCHA : " + ass.getParameterValue());
                 return ass;
 			}
 		}
         log.info("No assignment for " + paramName + " in config " + this);
 		return null;
 	}
 
 	/**
 	 * Creates a ParameterAssignmentPojo and adds it to the object's assignments.
 	 *
 	 * @param paramName  Name of the parameter, used to match a parameter and as the assignment's label
 	 * @param paramValue Value to set the parameter to.
 	 * @return the created ParameterAssignmentPojo
 	 *
 	 * @throws RuntimeException if no parameter matches paramName
 	 * @throws RuntimeException if paramValue is null
 	 */
 	public ParameterAssignmentPojo addParameterAssignment(String paramName, String paramValue) {
 		log.info("Adding parameter assignment for {}", paramName);
 		ParameterPojo param = this.getParamByName(paramName);
 		if (null == param) {
 			throw new RuntimeException("Webservice/Workflow contains no such parameter: " + paramName);
 		}
 		if (null == paramValue) {
 			throw new RuntimeException("Parameter value for param " + param.getId() + " is null!");
 		}
 
 		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
 		ass.setLabel(paramName);
 		ass.setForParam(param);
 		ass.setParameterValue(paramValue);
 		this.getParameterAssignments().add(ass);
 		return ass;
 	}
 
 	/** Add a ParameterAssignmentPojo to the assignments */
 	public void addParameterAssignment(ParameterAssignmentPojo ass) {
 		this.getParameterAssignments().add(ass);
 	}
 
 	/** 
 	 * Get the value of an assignment by the name of the parameter 
 	 * @param needle  A parameter's ID, label or substring thereof
 	 * @return The value if an assignment could be found, null otherwise
 	 */
     public String getParameterValueByName(String needle) {
 		ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
 		if (null != ass) {
			String value = ass.getParameterValue();
			value = UriUtils.sanitizeInput(value);
			return value;
 		}
         log.info("No value found for: " + needle);
 		return null;
     }
 
 
     /*********************
      * GETTERS/SETTERS
      ********************/
 
 	@RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
 	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
 	/** parameterAssignments getter */
 	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
 	/** parameterAssignments setter */
 	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }
 
 
 	@RDFProperty(NS.DCTERMS.PROP_CREATOR)
 	private UserPojo creator;
 	/** creator getter */
 	public UserPojo getCreator() { return creator; }
 	/** creator setter */
 	public void setCreator(UserPojo creator) { this.creator = creator; }
 
 	@RDFProperty(NS.DCTERMS.PROP_MODIFIED)
 	private DateTime modified = DateTime.now();
 	/** modified getter */
 	public DateTime getModified() { return modified; }
 	/** modified setter */
 	public void setModified(DateTime modified) { this.modified = modified; }
 }
