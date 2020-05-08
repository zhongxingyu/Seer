 package skillsplanner.beans;
 
 import java.util.HashMap;
 
 
 /**
  * This defines the rules for any character class. Since all information
  * is loaded from an XML file, this only defines the structure of 
  * what information is being looked for. 
  * @author ryzngard
  *
  */
 public class DFOClass {
 	private String description;
 	private String name; 
 	private String baseClass;
 	public String getBaseClass() {
 		return baseClass.substring(baseClass.indexOf("/")+1);
 	}
 
 	public void setBaseClass(String baseClass) {
 		this.baseClass = baseClass;
 	}
 
 	private HashMap<String,Integer> skills;
 
 	
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void addSkill(Skill st) {
 		if(skills == null){
 			skills = new HashMap<String,Integer>();
 		}
 		
 		//don't add the skill if it exists
 		if(skills.containsKey(st.getName())){
 			return;
 		}
 		skills.put(st.getName(),st.getMinLevel());
 	}
 	
 	public HashMap<String,Integer> getSkills(){
 		if(skills == null){
 			skills = new HashMap<String,Integer>();
 		}
 		return skills;
 	}
 	
 	@Override
 	public String toString(){
		return this.getUniqueName().substring(getUniqueName().indexOf("/")+1);
 	}
 
 	/**
 	 * Returns parent/name in order to provide uniqueness for mapping.
 	 * @return
 	 */
 	public String getUniqueName() {
 		return (this.getBaseClass() + "/" + this.name).toLowerCase();
 	}
 
 	public void removeSkill(Skill sk) {
 		if(skills == null){
 			skills = new HashMap<String,Integer>();
 		}
 		
 		if(skills.containsKey(sk.getName())){
 			skills.remove(sk.getName());
 		}
 		
 	}
 
 }
