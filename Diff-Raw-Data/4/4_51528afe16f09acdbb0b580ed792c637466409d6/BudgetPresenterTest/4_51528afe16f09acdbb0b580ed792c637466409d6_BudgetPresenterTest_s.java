 package info.whiter4bbit.gwt.test.client;
 
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import info.whiter4bbit.gwt.client.BudgetPresenter;
 import info.whiter4bbit.gwt.client.BudgetView;
 import info.whiter4bbit.gwt.client.ExpensePanel;
 import info.whiter4bbit.gwt.client.data.ExpensesDataProvider;
 import info.whiter4bbit.gwt.client.events.AddExpenseHandler;
 import info.whiter4bbit.gwt.shared.Expense;
 import org.junit.Test;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
 public class BudgetPresenterTest {
 
     private class MockButton implements HasClickHandlers {
         private ClickHandler clickHandler;
 
         @Override
         public HandlerRegistration addClickHandler(ClickHandler handler) {
             this.clickHandler = handler;
             return null;
         }
 
         @Override
         public void fireEvent(GwtEvent<?> event) {
 
         }
 
         public void click() {
             if (this.clickHandler != null) {
                 this.clickHandler.onClick(null);
             }
         }
     }
 
     private class MockExpensePanel implements ExpensePanel {
         private AddExpenseHandler handler;
         private String amount;
         private String description;
         @Override
         public void addAddExpenseHandler(AddExpenseHandler handler) {
             this.handler = handler;
         }
 
         @Override
         public String getAmount() {
             return amount;
         }
 
         @Override
         public String getDescription() {
             return description;
         }
 
         public void addExpense(String amount, String description) {
             this.amount = amount;
             this.description = description;
             if (handler != null) {
                 handler.onAdd(null);
             }
         }
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testAddExpense() {
         final MockButton mockAddButton = new MockButton();
         final MockExpensePanel expensePanel = new MockExpensePanel();
 
         ExpensesDataProvider dataProvider = createMock(ExpensesDataProvider.class);
         dataProvider.bind(null); expectLastCall();
 
         BudgetView viewMock = createMock(BudgetView.class);
         expect(viewMock.getTable()).andReturn(null);
         expect(viewMock.getAddButton()).andReturn(mockAddButton);
         expect(viewMock.showExpensePanel()).andReturn(expensePanel);
 
        dataProvider.add(new Expense("Some description", "", 8989f));
 
         replay(viewMock);
         replay(dataProvider);
 
         new BudgetPresenter(dataProvider, viewMock);
         mockAddButton.click();
         expensePanel.addExpense("8989.0", "Some description");
 
         verify(viewMock);
     }
     
 }
