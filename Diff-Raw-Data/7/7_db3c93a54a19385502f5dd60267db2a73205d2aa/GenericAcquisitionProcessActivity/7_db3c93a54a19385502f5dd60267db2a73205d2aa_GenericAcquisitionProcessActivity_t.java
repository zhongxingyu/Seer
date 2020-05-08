 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.EmailSender;
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.OperationLog;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.ActivityException;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public abstract class GenericAcquisitionProcessActivity extends AbstractActivity<AcquisitionProcess> {
 
     @Override
     protected void logExecution(AcquisitionProcess process, String operationName, User user) {
 	new OperationLog(process, user.getPerson(), operationName, process.getAcquisitionProcessStateType(), new DateTime());
     }
 
     @Override
     public String getLocalizedName() {
 	return RenderUtils.getResourceString("ACQUISITION_RESOURCES", "label." + getClass().getName());
     }
 
     protected void notifyAcquisitionRequester(AcquisitionProcess process) {
 	Person person = process.getRequestor();
	ResourceBundle bundle = ResourceBundle.getBundle("resources/ExpenditureResources", Language.getLocale());
 	if (person.getOptions().getReceiveNotificationsByEmail()) {
 	    try {
 		EmailSender.sendEmail(person.getUsername(), bundle.getString("process.label.notifyTopic"), bundle
 			.getString("process.label.notifyMessage"));
 	    } catch (XFireRuntimeException e) {
 		e.printStackTrace();
 		throw new ActivityException("activities.messages.exception.webserviceProblem", getLocalizedName());
 	    }
 	}
     }
 
     protected void notifyUnitsResponsibles(AcquisitionProcess process) {
 	List<Person> people = new ArrayList<Person>();
 	for (Unit unit : process.getPayingUnits()) {
 	    for (Authorization authorization : unit.getAuthorizations()) {
 		Person person = authorization.getPerson();
 		if (person.getOptions().getReceiveNotificationsByEmail()) {
 		    people.add(authorization.getPerson());
 		}
 	    }
 	}
 
	ResourceBundle bundle = ResourceBundle.getBundle("resources/ExpenditureResources", Language.getLocale());
 
 	try {
 	    for (Person person : people) {
 		EmailSender.sendEmail(person.getUsername(), bundle.getString("process.label.notifyTopic"), bundle
 			.getString("process.label.notifyMessage"));
 	    }
 	} catch (XFireRuntimeException e) {
 	    e.printStackTrace();
 	    throw new ActivityException("activities.messages.exception.webserviceProblem", getLocalizedName());
 	}
     }
 }
