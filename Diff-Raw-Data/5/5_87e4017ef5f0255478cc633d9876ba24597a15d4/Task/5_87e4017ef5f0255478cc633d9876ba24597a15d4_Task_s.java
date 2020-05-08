 package taskManagerConcurrent;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlRootElement;
 /**
  * JAXB class - task-element
  * This class is based on a code snippet by Rao and follows his structure with public fields.
  * We have added another constructor and some equality measurements
  * @author BieberFever (based on code snippet by rao)
  */
     @XmlRootElement(name = "task")
     public class Task implements Serializable{
     	private static final long serialVersionUID = 7526472295622771337L;
     	
     	/**
     	 * An alternative to the empty constructor in Rao's code snippet
     	 * @param id The ID of the Task
     	 * @param name The name of the Task
     	 * @param date The date of the Task
     	 * @param status The status of the Task
     	 * @param description The description of the Task
     	 * @param attendants The attendants of the Task
     	 * @param condtitions The tasks which must be completed before this Task
     	 * @param responses The Task, which this Task responds to
     	 */
     	public Task(String id, String name, String date, String status, boolean required, String description, String attendants, String conditions, String responses, String role){
     		this.id = id;
     		this.name = name;
     		this.date = date;
     		this.status = status;
     		this.required = required;
     		this.description = description;
     		this.attendants = attendants;
     		this.conditions = conditions;
     		this.responses = responses;
     		this.role = role;
     	}
     	
         @XmlID
         @XmlAttribute
         public String id;
         
         @XmlAttribute
         public String name;
         
         @XmlAttribute
         public String date;
         
         @XmlAttribute
         public String status;
         
         @XmlAttribute
         public boolean required;
         
         @XmlElement
         public String description;
         
         @XmlElement
         public String attendants;
         
         @XmlElement
         public String conditions;
         
         @XmlElement
         public String responses;
         
         @XmlElement
         public String role;
         
         //Overriding equals to help recognize two identical tasks
         @Override
         public boolean equals(Object obj) {
         	if ( this == obj ) return true;
         	if (!(obj instanceof Task)) return false;
         	Task that = (Task) obj;
         	return  this.id.equals(that.id) &&
         			this.name.equals(that.name) &&
         			this.date.equals(that.date) &&
         			this.status.equals(that.status) &&
         			this.required == that.required &&
         			this.description.equals(that.description) &&
         			this.attendants.equals(that.attendants) &&
         			this.conditions.equals(that.conditions) &&
         			this.responses.equals(that.responses) &&
         			this.role.equals(that.role);
         }
         
         //Overriding hashcode to be consistent with equals
         @Override
         public int hashCode() {
         	int hash = 17;
             hash = hash * 31 + (id == null ? 0 : id.hashCode());
             hash = hash * 31 + (name == null ? 0 : name.hashCode());
             hash = hash * 31 + (date == null ? 0 : date.hashCode());
             hash = hash * 31 + (status == null ? 0 : status.hashCode());
             hash = hash * 31 + (required ? 1231 : 1237);
             hash = hash * 31 + (description == null ? 0 : description.hashCode());
             hash = hash * 31 + (attendants == null ? 0 : attendants.hashCode());
             hash = hash * 31 + (conditions == null ? 0 : conditions.hashCode());
             hash = hash * 31 + (responses == null ? 0 : responses.hashCode());
             hash = hash * 31 + (role == null ? 0 : role.hashCode());
             return hash;
         }
     }
