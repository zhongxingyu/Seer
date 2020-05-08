 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.SelectableDataModel;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.SelectableDataModelForSubjects;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.repository.IPersonDAO;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.service.ISubjectChoosingService;
 
 import javax.faces.bean.ViewScoped;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 @ViewScoped
 public class SubjectChoosingController implements ISubjectChoosingService {
 
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SubjectChoosingController.class.getName());
     private Subject[] chosenSubjects;
     private SelectableDataModel<Subject> model;
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private IPersonDAO personDAO;
 
     public boolean userAlreadySubmitedSubjects() {
         return false;
     }
 
    @Override
    public List<Subject> getAvailableSubjectForEnrollment(Enroll enroll) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

     public void setModel(SelectableDataModel<Subject> model) {
         this.model = model;
     }
 
     public SelectableDataModel<Subject> getModel() {
         return model;
     }
 
     public void createModel(Enroll enrollment) {
         //TODO: get already chosen and assign to chosenSubjects, now assign null
         chosenSubjects = null;
         List<Subject> subjects = subjectDAO.getSubjectsByEnrollment(enrollment);
         model = new SelectableDataModelForSubjects(subjects);
     }
 
     public void setChosenSubjects(Subject[] chosenSubjects) {
         this.chosenSubjects = chosenSubjects;
     }
 
     public Subject[] getChosenSubjects() {
         return chosenSubjects;
     }
 }
