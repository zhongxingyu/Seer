 package de.puzzles.webapp.page.dashboard;
 
 import de.puzzles.core.DatabaseConnector;
 import de.puzzles.core.domain.CreditRequest;
 import de.puzzles.core.domain.CreditState;
 import de.puzzles.core.domain.RepaymentPlan;
 import de.puzzles.core.domain.Transaction;
 import de.puzzles.core.util.PuzzlesUtils;
 import de.puzzles.webapp.page.RequiresLoginPage;
 import de.puzzles.webapp.page.newrequest.RepaymentPlanPanel;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.request.cycle.RequestCycle;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrick Groß-Holtwick
  *         Date: 13.03.13
  */
 public class CreditDetailsPage extends RequiresLoginPage {
 
     CreditRequest request;
 
     public CreditDetailsPage() {
         super();
         String reqId = String.valueOf(RequestCycle.get().getRequest().getQueryParameters().getParameterValue("requestId"));
         final Integer requestId = PuzzlesUtils.parseInt(reqId);
         if (requestId != null) {
             request = DatabaseConnector.getInstance().getCreditRequestById(Integer.valueOf(reqId));
             if (request.getConsultantId() != getUserId()) {
                 setResponsePage(DashboardPage.class);
             }
             else {
                 final CompoundPropertyModel<CreditRequest> model = new CompoundPropertyModel<CreditRequest>(request);
                //add(new TextField<String>("name", model.<String>bind("customer.lastname")));
                //add(new TextField<String>("firstname", model.<String>bind("customer.firstname")));
                 add(new TextField<String>("state", model.<String>bind("state")));
                 add(new TextField<String>("interest", model.<String>bind("repaymentPlan.interest")));
                 add(new TextField<String>("amount", model.<String>bind("repaymentPlan.amount")));
                 add(new TextField<String>("duration", model.<String>bind("repaymentPlan.duration")));
                 add(new TextField<String>("rate", model.<String>bind("repaymentPlan.rate")));
                 WebMarkupContainer buttonContainer = new WebMarkupContainer("buttonContainer") {
                     @Override
                     public boolean isVisible() {
                         return model.getObject().getState() == CreditState.PENDING;
                     }
                 };
                 Button acceptButton = new Button("acceptButton");
                 acceptButton.add(new AjaxEventBehavior("onclick") {
                     @Override
                     protected void onEvent(AjaxRequestTarget target) {
                         DatabaseConnector.getInstance().changeRequestState(requestId, CreditState.ACCEPTED);
                         setResponsePage(DashboardPage.class);
                     }
                 });
                 buttonContainer.add(acceptButton);
 
                 Button declineButton = new Button("declineButton");
                 declineButton.add(new AjaxEventBehavior("onclick") {
                     @Override
                     protected void onEvent(AjaxRequestTarget target) {
                         DatabaseConnector.getInstance().changeRequestState(requestId, CreditState.REJECTED);
                         setResponsePage(DashboardPage.class);
                     }
                 });
                 buttonContainer.add(declineButton);
                 add(buttonContainer);
 
                 add(new ListView<Transaction>("earnings", new AbstractReadOnlyModel<List<? extends Transaction>>() {
                     @Override
                     public List<? extends Transaction> getObject() {
                         List<Transaction> list = new ArrayList<Transaction>();
                         for (Transaction t : model.getObject().getTransactions()) {
                             if (t.getValue() > 0.0) {
                                 list.add(t);
                             }
                         }
                         return list;
                     }
                 }) {
                     @Override
                     protected void populateItem(final ListItem<Transaction> item) {
                         item.add(new Label("earningDescription", new AbstractReadOnlyModel<String>() {
                             @Override
                             public String getObject() {
                                 String s = item.getModelObject().getDescription();
                                 if (item.getModelObject().getDescription1() != null && item.getModelObject().getDescription1().length() > 0) {
                                     s+=", "+ item.getModelObject().getDescription1();
                                 }
                                 if (item.getModelObject().getDescription2() != null && item.getModelObject().getDescription2().length() > 0) {
                                     s+=", "+ item.getModelObject().getDescription2();
                                 }
                                 return s;
                             }
                         }));
                         TextField<Double> value = new TextField<Double>("earningValue", new AbstractReadOnlyModel<Double>() {
                             @Override
                             public Double getObject() {
                                 return item.getModelObject().getValue();
                             }
                         });
                         value.setEnabled(false);
                         item.add(value);
                     }
                 });
                 final TextField<Double> resultField = new TextField<Double>("earningsTotal", new AbstractReadOnlyModel<Double>() {
                     @Override
                     public Double getObject() {
                         Double d = 0.0;
                         for (Transaction t : model.getObject().getTransactions()) {
                             if (t.getValue() > 0.0) {
                                 d += t.getValue();
                             }
                         }
                         return d;
                     }
                 });
                 resultField.setEnabled(false);
                 add(resultField);
 
                 final TextField<Double> resultSpendings = new TextField<Double>("spendingsTotal", new AbstractReadOnlyModel<Double>() {
                     @Override
                     public Double getObject() {
                         Double d = 0.0;
                         for (Transaction t : model.getObject().getTransactions()) {
                             if (t.getValue() < 0.0) {
                                 d += t.getValue();
                             }
                         }
                         return Math.abs(d);
                     }
                 });
                 resultSpendings.setEnabled(false);
                 add(resultSpendings);
 
                 add(new ListView<Transaction>("spendings", new AbstractReadOnlyModel<List<? extends Transaction>>() {
                     @Override
                     public List<? extends Transaction> getObject() {
                         List<Transaction> list = new ArrayList<Transaction>();
                         for (Transaction t : model.getObject().getTransactions()) {
                             if (t.getValue() < 0.0) {
                                 list.add(t);
                             }
                         }
                         return list;
                     }
                 }) {
                     @Override
                     protected void populateItem(final ListItem<Transaction> item) {
                         item.add(new Label("spendingDescription", new AbstractReadOnlyModel<String>() {
                             @Override
                             public String getObject() {
                                 String s = item.getModelObject().getDescription();
                                 if (item.getModelObject().getDescription1() != null && item.getModelObject().getDescription1().length() > 0) {
                                     s+=", "+ item.getModelObject().getDescription1();
                                 }
                                 if (item.getModelObject().getDescription2() != null && item.getModelObject().getDescription2().length() > 0) {
                                     s+=", "+ item.getModelObject().getDescription2();
                                 }
                                 return s;
                             }
                         }));
                         TextField<Double> value = new TextField<Double>("spendingValue", new AbstractReadOnlyModel<Double>() {
                             @Override
                             public Double getObject() {
                                 return item.getModelObject().getValue();
                             }
                         });
                         value.setEnabled(false);
                         item.add(value);
                     }
                 });
 
 
                 add(new Label("incomeTotal", resultField.getModel()));
                 add(new Label("spendingTotal", resultSpendings.getModel()));
                 add(new Label("rateTotal", model.<String>bind("repaymentPlan.rate")));
                 add(new Label("total", new AbstractReadOnlyModel<Double>() {
                     @Override
                     public Double getObject() {
                         return resultField.getModelObject() - resultSpendings.getModelObject() - request.getRepaymentPlan().getRate();
                     }
                 }));
 
                 add(new RepaymentPlanPanel("repaymentPlan", new AbstractReadOnlyModel<List<RepaymentPlan.Entry>>() {
                     @Override
                     public List<RepaymentPlan.Entry> getObject() {
                         return model.getObject().getRepaymentPlan().generateRepaymentPlan();
                     }
                 }));
             }
         }
         else {
             setResponsePage(DashboardPage.class);
         }
     }
 
     @Override
     public String getTitle() {
         return "Kreditdetails";
     }
 }
