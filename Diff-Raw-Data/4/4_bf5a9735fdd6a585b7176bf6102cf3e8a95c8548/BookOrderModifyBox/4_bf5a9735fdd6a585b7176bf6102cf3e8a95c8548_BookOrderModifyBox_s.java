 package com.tap.ui;
 
 import java.util.TimeZone;
 
 import hirondelle.date4j.DateTime;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import com.hexapixel.widgets.generic.Utils;
 import com.tap.bizlogic.*;
 import com.tap.tableordersys.*;
 
 public class BookOrderModifyBox {
 	protected Object result;
 	private Text textOrderID;
 	private Text textOperatorID;
 	private Text textTableID;
 	private Text textGusetsID;
 	private Text textGusetsAmount;
 	private Text textBookTime;
 	
 	private Order order;
 	private OrderLogic ol;
 	boolean modifyID=false;
 
 	protected Shell shell;
 
 	public BookOrderModifyBox() {}
 	public BookOrderModifyBox(Order o,OrderLogic ol) {
 		this.order = o;
 		this.ol = ol;
 	}
 	public BookOrderModifyBox(boolean modifyID) {
 		this.modifyID = modifyID;
 	}
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			BookOrderModifyBox window = new BookOrderModifyBox();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shell = new Shell();
 		shell.setSize(362, 325);
 		shell.setText("Modify order");
 		Utils.centerDialogOnScreen(shell);
 		
 		if(null==order)return;
 		
 		Label lblNewLabel = new Label(shell, SWT.NONE);
 		lblNewLabel.setBounds(33, 39, 61, 17);
 		lblNewLabel.setText("ID:");
 		
 		if(null==order.getOrderID())return;
 		textOrderID = new Text(shell, SWT.BORDER);
 		textOrderID.setBounds(141, 36, 137, 23);
 		textOrderID.setText(order.getOrderID());
 		textOrderID.setEnabled(modifyID);
 		
 		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
 		lblNewLabel_1.setBounds(33, 72, 61, 17);
 		lblNewLabel_1.setText("Operator:");
 		
 		textOperatorID = new Text(shell, SWT.BORDER);
 		textOperatorID.setBounds(141, 72, 137, 23);
 		if(null!=order.getOperatorID())
 			textOperatorID.setText(order.getOperatorID());
 		textOperatorID.setEnabled(false);
 		
 		Label lblNewLabel_2 = new Label(shell, SWT.NONE);
 		lblNewLabel_2.setBounds(33, 107, 61, 17);
 		lblNewLabel_2.setText("Table ID:");
 		
 		textTableID = new Text(shell, SWT.BORDER);
 		textTableID.setBounds(141, 101, 137, 23);
 		if(null!=order.getTable())
 			textTableID.setText(order.getTable().getId());
 
 		Label lblNewLabel_3 = new Label(shell, SWT.NONE);
 		lblNewLabel_3.setBounds(33, 139, 61, 17);
 		lblNewLabel_3.setText("Guest ID:");
 		
 		textGusetsID = new Text(shell, SWT.BORDER);
 		textGusetsID.setBounds(141, 133, 137, 23);
 		if(null!=order.getGusets())
 			textGusetsID.setText(order.getGusets().getId());
 
 		Label lblNewLabel_4 = new Label(shell, SWT.NONE);
 		lblNewLabel_4.setBounds(33, 165, 73, 17);
 		lblNewLabel_4.setText("Guest amout:");
 		
 		textGusetsAmount = new Text(shell, SWT.BORDER);
 		textGusetsAmount.setBounds(141, 162, 137, 23);
 		if(null!=order.getGusets())
 			textGusetsAmount.setText(order.getGusets().getAmountString());
 		
 		if(!(order instanceof BookOrder)){
 			System.err.println("Order is not a book order!");
 			return;
 		}
 			Label lblBookTime = new Label(shell, SWT.NONE);
 			lblBookTime.setBounds(33, 194, 73, 17);
 			lblBookTime.setText("Book time:");
 			
 			textBookTime = new Text(shell, SWT.BORDER);
 			textBookTime.setBounds(141, 191, 137, 23);
 			textBookTime.setText( ((BookOrder)order).getBookTime().toString() );
 		
 
 		Button btnSave = new Button(shell, SWT.NONE);
 		btnSave.setBounds(33, 238, 80, 27);
 		btnSave.setText("Save");
 		btnSave.addSelectionListener(new btSaveListener());
 		
 		Button btnDelete = new Button(shell, SWT.NONE);
 		btnDelete.setBounds(141, 238, 80, 27);
 		btnDelete.setText("Cancel");
 		btnDelete.addSelectionListener(new btCancelListener());
 		
 		Button btnCheckin = new Button(shell, SWT.NONE);
 		btnCheckin.setBounds(242, 238, 80, 27);
 		btnCheckin.setText("CheckIn");
 		btnCheckin.addSelectionListener(new btCheckInListener());
 
 	}
 
 	class btSaveListener implements SelectionListener{
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Table t = ol.getRestaurant().findTableByID(textOrderID.getText());
 			if(null!=t){
 				Table modifyTable = ol.getRestaurant().findTableByID(textTableID.getText());
 				if(modifyTable!=null){
 					order.setTable(modifyTable);
 				}else{
 					MessageBox msgBox = new MessageBox(shell, 1);
 					msgBox.setMessage("No such Table!");
 					msgBox.open();
 					return;
 				}
 				Guests g = order.getGusets();
 				boolean change = g.setAmountString( textGusetsAmount.getText() );
 				if(!change){
 					MessageBox msgBox = new MessageBox(shell, 1);
 					msgBox.setMessage("Wrong amount input!");
 					msgBox.open();
 					return;
 				}
 				if(order instanceof BookOrder){
 					BookOrder saveOrder = (BookOrder) order;
 					t.saveBookOrderByID(saveOrder);
 				}else{
 					t.saveOrderByID(order);
 				}
 			}
 			ol.saveResturant();
 			shell.close();
 		}
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {}
 	}
 	class btCancelListener implements SelectionListener{
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Order o = ol.getRestaurant().findOrderByID(textOrderID.getText());
 			boolean change = false;
 			if(null!=o){
 				if(o instanceof BookOrder){
 					o.cancel();
 					BookOrder cOrder = (BookOrder) o;
 					change = o.getTable().deleteBookOrder(cOrder);
 				}else{
 					change = o.getTable().deleteOrder(o);
 				}
 			}
 			System.out.println(change);
 			ol.saveResturant();
 			shell.close();
 		}
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {}
 	}
 	class btCheckInListener implements SelectionListener{
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			
			Order o = ol.getRestaurant().findOrderByID(textOrderID.getText());
 			boolean change = false;
 			if(null!=o){
 				if(o instanceof BookOrder){
 					BookOrder cOrder = (BookOrder) o;
 					DateTime now = DateTime.now(TimeZone.getDefault());
 					if(false==cOrder.canBookTime(now)){//Means is in his book time
 						
 					}else{
 						MessageBox msgBox = new MessageBox(shell, 1);
 						msgBox.setMessage("Check in fail, only check in in booked time:"+cOrder.getBookTime()+"!");
 						msgBox.open();
 						return;
 					}
 					cOrder.checkIn();
 					change = cOrder.getTable().deleteBookOrder(cOrder);
 					cOrder.getTable().addOrder(cOrder);
 				}
 			}
 			ol.saveResturant();
 			shell.close();
 		}
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {}
 	}
 }
