 package charms;
 
 /**
  * @author LIS
  * 
  */
 public class CharmOption implements Comparable<CharmOption>{
 	private String name;
 	private String type;
 	private String defaultValue;
 	private String description;
 	private boolean optional;
 	private String originalName;
 
 	public CharmOption() {
 		optional = true;
 	}
 
 	public CharmOption(String name, String type, String defaultValue, String description, boolean optional) {
 		super();
 		this.name = name;
 		this.type = type;
 		this.defaultValue = defaultValue;
 		this.description = description;
 		this.optional = optional;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getOriginalName() {
 		return originalName;
 	}
 
 	public void setName(String name) {
 		this.originalName = name;
 		this.name = name.replace('-', '_');
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 
 	public void setDefaultValue(String defaultValue) {
 		this.defaultValue = defaultValue.replace("\"", "").replace("'", "").replace("\n", " ");
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description.replace("\n", " ");
 	}
 
 	public boolean isComplete() {
 		return (this.name != null && this.type != null && this.defaultValue != null && this.description != null);
 	}
 
 	public boolean isOptional() {
 		return optional;
 	}
 
 	public void setOptional(boolean optional) {
 		this.optional = optional;
 	}
 
 	public String toNShellParam() {
		return String.format("%s%s %s = %s # %s", optional ? "optional " : "", type, name, type.equals("string") ? '"' + defaultValue + '"' : defaultValue, optional ? originalName + ": " + description : description);
 	}
	
 	@Override
 	public String toString() {
 		return String.format("%s {\n\tname: %s\n\ttype: %s\n\tdefaultValue: %s\n\tdescription: %s\n\toptional: %s\n}", getClass().getName(), name, type, defaultValue, description, optional);
 	}
 
 	@Override
 	public int compareTo(CharmOption o) {
 		if(this.optional || !o.optional)
 			return 1;
 		if(!this.optional || o.optional)
 			return -1;
 		return 0;
 	}
 
 }
