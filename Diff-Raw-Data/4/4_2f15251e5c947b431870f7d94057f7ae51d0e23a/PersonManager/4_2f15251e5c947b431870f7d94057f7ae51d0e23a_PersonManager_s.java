 package getbuzee.web;
 
 import getbuzee.authentication.AccountController;
 import getbuzee.entity.Person;
 import getbuzee.exception.CatchException;
 import getbuzee.service.PersonService;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import utils.Loggable;
 
 @Named
 //@RequestScoped TODO should be request scoped
 @SessionScoped
 @Loggable
 @CatchException
 public class PersonManager  implements Serializable{
 	
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Inject
     private PersonService personService;
     
     @Inject
     private AccountController accountController;
     
     private Person currentPerson;
     
     private List<Person> allPersons;
     
     private List<Person> myFriends;
     
     private List<Person> personsAskedMe;
     
     private List<Person> personsIAsked;
     
     public List<Person> findAllPersons(){
     	this.allPersons = personService.findAllPersons();
     	return allPersons;
     }
     
     public List<Person> findAllMyFriends(){
     	this.myFriends = (List<Person>) accountController.getloggedInPerson().getFriends();
     	return myFriends;
     }
 
 	public List<Person> getAllPersons() {
 		this.allPersons = personService.findAllPersons();
 		return allPersons;
 	}
 
 	public void setAllPersons(List<Person> allPersons) {
 		this.allPersons = allPersons;
 	}
     
     public boolean isPersonIAsked(Person person){
     	if (getPersonsIAsked() != null){
     		return getPersonsIAsked().contains(person);
     	}
     	else
     		return false;
     }
     
     public boolean isPersonAskedMe(Person person){
     	return getPersonsAskedMe().contains(person);
     }
     
     public boolean isFriend(Person person){
     	return getMyFriends().contains(person);
     }
     
     public void addOrConfirmFriend(Person person){
 		currentPerson = person;
 		Person loggedInPerson = accountController.getloggedInPerson();
     	List<Person> friendsIAsked = loggedInPerson.getFriendsIAsked();
     	
     	if (!friendsIAsked.contains(currentPerson) && !currentPerson.equals(loggedInPerson)){
     		loggedInPerson.getFriendsIAsked().add(currentPerson);
	    	personService.updatePerson(loggedInPerson);	    	
     	}
     }
     
     public void suppressFriend(Person person){
 		currentPerson = person;
 		Person loggedInPerson = accountController.getloggedInPerson();
 		personService.removeFriend(loggedInPerson, currentPerson);
 		accountController.getloggedInPerson().getFriendsAskedMe().remove(currentPerson);
 		accountController.getloggedInPerson().getFriendsIAsked().remove(currentPerson);
 		accountController.getloggedInPerson().getFriends().remove(currentPerson);
     }
 
 	public Person getCurrentPerson() {
 		return currentPerson;
 	}
 
 	public void setCurrentPerson(Person currentPerson) {
 		this.currentPerson = currentPerson;
 	}
 
 	public List<Person> getMyFriends() {
 		//this.myFriends = (List<Person>) accountController.getloggedInPerson().getFriends();
 		this.myFriends = personService.findFriends(accountController.getloggedInPerson());
 		return myFriends;
 	}
 	
 	public List<Person> getPersonsAskedMe(){
 		Person loggedInPerson = (Person) accountController.getloggedInPerson();
 		this.personsAskedMe = new ArrayList<Person>(loggedInPerson.getFriendsAskedMe());
 		if (this.personsAskedMe != null && myFriends != null) this.personsAskedMe.removeAll(myFriends);
 		return personsAskedMe;
 	}
 	
 	public List<Person> getPersonsIAsked(){
 		Person loggedInPerson = (Person) accountController.getloggedInPerson();
 		this.personsIAsked = new ArrayList<Person>(loggedInPerson.getFriendsIAsked());
 		if (this.personsIAsked != null && myFriends != null) this.personsIAsked.removeAll(myFriends);
 		return personsIAsked;
 	}
 
 	public void setMyFriends(List<Person> myFriends) {
 		this.myFriends = myFriends;
 	}
     
     
 
 }
