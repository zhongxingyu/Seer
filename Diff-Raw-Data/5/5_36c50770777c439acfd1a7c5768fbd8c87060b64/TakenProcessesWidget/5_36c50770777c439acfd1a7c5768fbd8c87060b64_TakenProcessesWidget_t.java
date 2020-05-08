 package pt.ist.expenditureTrackingSystem.presentationTier.widgets;
 
 import java.util.List;
 
 import module.dashBoard.presentationTier.WidgetRequest;
 import module.dashBoard.widgets.WidgetController;
 import myorg.util.ClassNameBundle;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 @ClassNameBundle(bundle = "resources/ExpenditureResources", key = "process.title.takenProcesses")
 public class TakenProcessesWidget extends WidgetController {
 
     @Override
     public void doView(WidgetRequest request) {
	Person loggedPerson = Person.getLoggedPerson();
	List<PaymentProcess> takenProcesses = loggedPerson.getProcesses(PaymentProcess.class);
 	request.setAttribute("takenProcesses", takenProcesses.subList(0, Math.min(10, takenProcesses.size())));
	request.setAttribute("person", loggedPerson);
     }
 }
