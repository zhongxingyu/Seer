 package expensable.client.view.expensereport;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 
import expensable.client.view.helloworld.HelloWorld;
 
 public class ExpenseReportViewImpl extends Composite implements ExpenseReportView {
 
   private static Binder binder = GWT.create(Binder.class);
 
   interface Binder extends UiBinder<Widget, ExpenseReportViewImpl>{}
 
   private ExpenseReportPresenter presenter;
   @UiField FlowPanel panel;
   
   public ExpenseReportViewImpl() {
     initWidget(binder.createAndBindUi(this));
    HelloWorld helloWorld = new HelloWorld();
    helloWorld.setName("Ashwani");
    panel.add(helloWorld);
   }
 
   @Override
   public void setPresenter(ExpenseReportPresenter presenter) {
     this.presenter = presenter;
   }
 
 }
