 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.SessionScoped;
 
 
 @ManagedBean(name = "person")
 @RequestScoped
 public class PersonBean {
 
 	String personName;
 	String personNameUpper;
 	String prompt;
 
 	public String getPersonNameUpper() {
 		return personNameUpper;
 	}
 
 	public void setPersonNameUpper(String personNameUpper) {
 		this.personNameUpper = personNameUpper;
 	}
 
 	public PersonBean() {
 		this.prompt = "Enter your name please : ";
 	}
 
 	public String getPrompt() {
 		return prompt;
 	}
 
 	public void setPrompt(String prompt) {
 		this.prompt = prompt;
 	}
 
 	public String getPersonName() {
 		return personName;
 	}
 
 	public void setPersonName(String name) {
 		personName = name;
 	}
 
	public void doUppercase() {
		personNameUpper = personName.toUpperCase();
 	}
 
 }
