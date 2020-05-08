 package pt.ist.expenditureTrackingSystem.presentationTier.widgets;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import module.dashBoard.presentationTier.WidgetRequest;
 import module.dashBoard.widgets.WidgetController;
 import myorg.util.ClassNameBundle;
 import myorg.util.Counter;
 import myorg.util.MultiCounter;
 
 import org.apache.commons.beanutils.BeanComparator;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.util.ProcessMapGenerator;
 
 @ClassNameBundle(bundle = "resources/ExpenditureResources", key = "title.widget.refundProcedure")
 public class PendingRefundWidget extends WidgetController {
 
     @Override
     public void doView(WidgetRequest request) {
	Person loggedPerson = Person.getLoggedPerson();
 	Map<RefundProcessStateType, MultiCounter<RefundProcessStateType>> refundMap = ProcessMapGenerator
		.generateRefundMap(loggedPerson);
 	List<Counter<RefundProcessStateType>> refundCounters = new ArrayList<Counter<RefundProcessStateType>>();
 
 	for (MultiCounter<RefundProcessStateType> multiCounter : refundMap.values()) {
 	    Counter<RefundProcessStateType> defaultCounter = ProcessMapGenerator.getDefaultCounter(multiCounter);
 	    if (defaultCounter != null) {
 		refundCounters.add(defaultCounter);
 	    }
 	}
 
 	Collections.sort(refundCounters, new BeanComparator("countableObject"));
 	request.setAttribute("refundCounters", refundCounters);
	request.setAttribute("person", loggedPerson);
     }
 }
