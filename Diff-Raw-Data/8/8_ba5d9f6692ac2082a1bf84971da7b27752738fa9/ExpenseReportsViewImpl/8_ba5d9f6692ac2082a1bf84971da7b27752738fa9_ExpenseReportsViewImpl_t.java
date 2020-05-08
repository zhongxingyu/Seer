 package expensable.client.view.expensereport;
 
 import java.util.Date;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.DatePickerCell;
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.SelectionModel;
 
 import expensable.client.activity.report.ShowExpenseReportsActivity;
 import expensable.shared.models.ExpenseReport;
 
 public class ExpenseReportsViewImpl extends Composite implements ExpenseReportsView {
 
   private static Binder uiBinder = GWT.create(Binder.class);
 
   interface Binder extends UiBinder<Widget, ExpenseReportsViewImpl> {
   }
 
   @UiField Button button;
   @UiField(provided = true) CellTable<ExpenseReport> reports;
   /*@UiField(provided = true)*/ SimplePager pager;
 
   private ExpenseReportPresenter presenter;
 
   public ExpenseReportsViewImpl() {
     reports = new CellTable<ExpenseReport>(ShowExpenseReportsActivity.KEY_PROVIDER);
 
     // Create a Pager to control the table.
     SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
     pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
     pager.setDisplay(reports);
 
     initWidget(uiBinder.createAndBindUi(this));
   }
 
   @Override
   public void setPresenter(ExpenseReportPresenter presenter) {
     this.presenter = presenter;
 
     // Add a selection model so we can select cells.
     final MultiSelectionModel<ExpenseReport> selectionModel
         = new MultiSelectionModel<ExpenseReport>(presenter.getKeyProvider());
     reports.setSelectionModel(selectionModel);
     initTableColumns(selectionModel);
     presenter.addReportsDisplay(reports);
   }
 
   @Override
   public CellTable<ExpenseReport> getReportsTable() {
     return reports;
   }
 
   @UiHandler("button")
   void onClick(ClickEvent e) {
     presenter.onButtonClick(e);
   }
 
  private volatile int numCols = 0; // TODO(dpurpura): get this from table somehow

   /**
    * Add the columns to the table.
    */
   private void initTableColumns(final SelectionModel<? super ExpenseReport> selectionModel) {
    while (numCols-- > 0) {
      reports.removeColumn(0);
    }
 
     // Checkbox column. This table will uses a checkbox column for selection.
     // Alternatively, you can call reports.setSelectionEnabled(true) to enable
     // mouse selection.
     Column<ExpenseReport, Boolean> checkColumn
         = new Column<ExpenseReport, Boolean>(new CheckboxCell(true)) {
       @Override
       public Boolean getValue(ExpenseReport object) {
         return selectionModel.isSelected(object);
       }
     };
     checkColumn.setFieldUpdater(new FieldUpdater<ExpenseReport, Boolean>() {
       @Override
       public void update(int index, ExpenseReport object, Boolean value) {
         // Called when the user clicks on a checkbox.
         selectionModel.setSelected(object, value);
       }
     });
     reports.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br>"));
 
     // Created date
     Column<ExpenseReport, Date> createdDateColumn
         = new Column<ExpenseReport, Date>(new DatePickerCell()) {
       @Override
       public Date getValue(ExpenseReport report) {
         return report.getCreatedDate();
       }
     };
     reports.addColumn(createdDateColumn, "Created Date");
     createdDateColumn.setFieldUpdater(new FieldUpdater<ExpenseReport, Date>() {
       @Override
       public void update(int index, ExpenseReport report, Date value) {
         // Called when the user changes the value.
         report.setCreatedDate(value);
         presenter.refreshDisplays();
       }
     });
 
     // Amount
     Column<ExpenseReport, Number> amountColumn
         = new Column<ExpenseReport, Number>(new NumberCell()) {
       @Override
       public Number getValue(ExpenseReport report) {
         return report.getAmount();
       }
     };
     reports.addColumn(amountColumn, "Amount");
     amountColumn.setFieldUpdater(new FieldUpdater<ExpenseReport, Number>() {
       @Override
       public void update(int index, ExpenseReport report, Number value) {
         // Called when the user changes the value.
         report.setAmount(value.intValue());
         presenter.refreshDisplays();
       }
     });
 
     // Name
     Column<ExpenseReport, String> nameColumn
         = new Column<ExpenseReport, String>(new EditTextCell()) {
       @Override
       public String getValue(ExpenseReport report) {
         return report.getName();
       }
     };
     nameColumn.setFieldUpdater(new FieldUpdater<ExpenseReport, String>() {
 
       @Override
       public void update(int index, ExpenseReport report, String value) {
         report.setName(value);
         presenter.refreshDisplays();
       }
 
     });
     reports.addColumn(nameColumn, "Name");
    numCols = 4;
   }
 
 }
