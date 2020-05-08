 package de.ralfrojahn.web.person.models;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.model.LoadableDetachableModel;
 
 import de.ralfrojahn.spring.entities.IPerson;
 import de.ralfrojahn.spring.services.IPersonService;
 
 @SuppressWarnings("serial")
 public class PersonListModel extends LoadableDetachableModel<List<IPerson>> {
 
 	private IPersonService personService;
 
 	public PersonListModel(IPersonService personService) {
 		super();
 		this.personService = personService;
 	}
 	
 	@Override
 	protected List<IPerson> load() {
		if (getPersonService() != null) {
			return getPersonService().getPersonDAO().find();
		}
		return new ArrayList<IPerson>();
 	}
 	
 	public IPersonService getPersonService() {
 		return personService;
 	}
 }
