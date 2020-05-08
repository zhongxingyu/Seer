 package pl.agh.enrollme.service;
 
 import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.SelectablePersonDataModel;
 import pl.agh.enrollme.repository.IEnrollmentDAO;
 import pl.agh.enrollme.repository.IPersonDAO;
 
 import javax.faces.bean.ViewScoped;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Rafał Cymerys
  */
 @Service
 @ViewScoped
 public class PersonEnrollPermissionService {
 
     @Autowired
     private IEnrollmentDAO enrollmentDAO;
 
     @Autowired
     private IPersonDAO personDAO;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PersonEnrollPermissionService.class);
 
     private List<Person> peopleAllowedToEnroll = new ArrayList<Person>();
     private SelectablePersonDataModel selectableModel;
 
     public PersonEnrollPermissionService() {
 
     }
 
     /**
      * Prepares current instance of class to manage selection of people allowed to participate in enroll.
      * Basically, it retrieves current selection from DAO and prepares selectable model that can be used in DataTable.
      * @param enroll enroll
      */
     @Transactional
     public void prepareForEnroll(Enroll enroll) {
         LOGGER.debug("Preparing for enroll " + enroll.getEnrollID());
 
         selectableModel = new SelectablePersonDataModel(personDAO.getList());
 
         /* Getting enroll again from database, so that lazyLoading would fetch persons properly.
          * Correct me if there's nicer way to do this; merging detached object doesn't work. */
         Enroll retrievedEnroll = enrollmentDAO.getByPK(enroll.getEnrollID());
         peopleAllowedToEnroll.clear();
         peopleAllowedToEnroll.addAll(retrievedEnroll.getPersons());
 
     }
 
     /**
      * Stores current selection in database using DAO.
      * @param enroll enroll in which selection should be saved
      */
     public void saveCurrentSelectionIntoEnroll(Enroll enroll) {
         enroll.setPersons(peopleAllowedToEnroll);
         enrollmentDAO.update(enroll);
     }
 
     public void addNewPerson(Person person, Enroll enroll) {
         LOGGER.debug("Adding new person");
         personDAO.add(person);
         ((List<Person>)selectableModel.getWrappedData()).add(person);
         peopleAllowedToEnroll.add(person);
     }
 
     public void onSelect(SelectEvent event) {
         Person person = (Person)event.getObject();
         LOGGER.debug("Selected " + person.getFirstName() + " " + person.getLastName());
         peopleAllowedToEnroll.add(person);
     }
 
    public void onUnselect(UnselectEvent event) {
         Person person = (Person)event.getObject();
         LOGGER.debug("Unselected " + person.getFirstName() + " " + person.getLastName());
         peopleAllowedToEnroll.remove(person);
     }
 
     public List<Person> getPeopleAllowedToEnroll() {
         return peopleAllowedToEnroll;
     }
 
     public void setPeopleAllowedToEnroll(List<Person> peopleAllowedToEnroll) {
         /* Selection is managed by listeners.
          * Provided getter is used to allow table to get list of people it should render as selected,
          * but getter does nothing as it could break our selection
          */
     }
 
     public SelectablePersonDataModel getSelectableModel() {
         return selectableModel;
     }
 
     public void setSelectableModel(SelectablePersonDataModel selectableModel) {
         this.selectableModel = selectableModel;
     }
 
 }
