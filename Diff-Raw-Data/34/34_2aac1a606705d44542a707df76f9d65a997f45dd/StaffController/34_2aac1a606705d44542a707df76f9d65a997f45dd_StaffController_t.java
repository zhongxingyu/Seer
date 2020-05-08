 package oscar.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.awt.event.MouseEvent;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
import org.joda.time.Days;
import org.joda.time.LocalDate;
 import oscar.MVC.Controller;
 import oscar.model.CarClass;
 import oscar.model.OscarComboBoxModelItem;
 import oscar.model.Person;
 import oscar.model.Rental;
 import oscar.util.TableModelHelper;
 import oscar.util.TableUpdateTask;
 import oscar.util.Utility;
 import oscar.util.Popup;
 import oscar.view.StaffView;
 import oscar.view.dialog.ExtensionDialog;
 
 /**
  *
  * @author Draga
  */
 public class StaffController extends Controller {
     // Views and dialogs
 
     private StaffView staffView;
     private ExtensionDialog extensionDialog;
     // Timer to update tables
     private Timer timer;
     // The runnable that update the tables
     private TableUpdateTask tableUpdateTask;
     // Filters to apply to queries
     private HashMap<String, String> filters;
     private boolean lastRequest = false;
     private boolean foundAndFilled = false;
     private int personId;
     private Rental _rental_ref = null;
     private int rentalId = -1;
     private boolean hasParent = false;
     // dialogs editing variables
     private String editingReturnId;
 
     StaffController(boolean hasParent) {
         this.hasParent = hasParent;
     }
 
     @Override
     public void run() {
         this.setName("Staff");
         staffView = new StaffView();
         this.addElement(staffView);
         if (hasParent) {
             staffView.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         }
         // Hire tab
 
         staffView.getHireClassCB().setModel(new CarClass().getComboModel("displayName", "---- select car class ---"));
         staffView.getHireClassCB().setSelectedIndex(0);
         extensionDialog = new ExtensionDialog(staffView, true);
         this.addElement(extensionDialog);
         this.addElement(extensionDialog.getjButton1());
         this.addElement(extensionDialog.getExtendsbtn());
         this.addElement(extensionDialog);
         this.addElement(staffView.getHireAddressTxt());
         this.addElement(staffView.getHireBtn());
         this.addElement(staffView.getHireChauffeuredCB());
         this.addElement(staffView.getHireClassCB());
         this.addElement(staffView.getHireClearBtn());
         this.addElement(staffView.getHireDateOfBirthDP());
         this.addElement(staffView.getHireEmailTxt());
         this.addElement(staffView.getHireFromDP());
         this.addElement(staffView.getHireInsuranceCB());
         this.addElement(staffView.getHireNameTxt());
         this.addElement(staffView.getHirePersonLoadBtn());
         this.addElement(staffView.getHirePhoneTxt());
         this.addElement(staffView.getHireRefCodeSearchBtn());
         this.addElement(staffView.getHireRefCodeTxt());
         this.addElement(staffView.getHireSurnameTxt());
         this.addElement(staffView.getHireToDP());
 
         this.addElement(staffView.getSurnameTxt2());
         this.addElement(staffView.getPlateTxt3());
         this.addElement(staffView.getRefCodeTxt2());
         this.addElement(staffView.getjXTable1());
 
         /**
          * Action Listener for the Combobox
          * When someone selects a CarClass the cars in the selected carclass would appear
          */
         staffView.getHireClassCB().addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 OscarComboBoxModelItem item = (OscarComboBoxModelItem) ((JComboBox) e.getSource()).getSelectedItem();
                 if (item.Id >= 0) {
                     Date startDate = staffView.getHireFromDP().getDate();
                     Date endDate = staffView.getHireToDP().getDate();
                     if(startDate==null || endDate==null)
                         return;
                     ArrayList<HashMap<String, String>> data = new CarClass().getCars(item.Id, startDate, endDate);
                     if (data != null) {
                         staffView.getHireTbl().setModel(TableModelHelper.getTableModel(data, new Object[]{"Plate Number", "Brand", "Model"}, new Object[]{"plate", "brand", "model"}));
                     }
                 }
 
             }
         });
         timer = new Timer();
         tableUpdateTask = new TableUpdateTask(null, null, null);
 
         updateRentalTbl();
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         // Hire tab
         if (e.getSource().equals(staffView.getHireBtn())) {
             actionHire();
         } else if (e.getSource().equals(staffView.getHireClearBtn())) {
             actionHireClearFields();
         } else if (e.getSource().equals(staffView.getHirePersonLoadBtn())) {
             actionHireLoadPerson();
         } else if (e.getSource().equals(staffView.getHireRefCodeSearchBtn())) {
             actionHireSearchRefCode();
         } // extension dialog
         else if (e.getSource().equals(extensionDialog.getExtendsbtn())) {
             actionExtensionDlgExtend();
         } else if (e.getSource().equals(extensionDialog.getjButton1())) {
             actionExtensionDlgCancel();
         }
     }
 
     private void actionLogout() {
         new LoginController().start();
         this.removeAllElement();
     }
 
     /**
      * Add a Rental Data
      */
     private void actionHire() {
             if(this.personId<=0){
                 //add the customer and get the id
                  Person person = new Person();
                 try{
                         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 
                         person.setAddress(staffView.getHireAddressTxt().getText());
                         person.setName( staffView.getHireNameTxt().getText());
                         person.setSurname(staffView.getHireSurnameTxt().getText());
                         person.setEmail(  staffView.getHireEmailTxt().getText());
                         person.setPhone(staffView.getHirePhoneTxt().getText());
                         person.setDateOfBirth(df.format(staffView.getHireDateOfBirthDP().getDate()));
                 }catch(Exception e){
                     
                 }
                 HashMap<String,String> errors = person.validate();
                             if(errors.size()>0){
                                 Popup.popup(errors, staffView);
                                 return;
                             }
                 
                 this.personId = person.addPk();
             }
             
             if(this.personId>0){
                         OscarComboBoxModelItem item = (OscarComboBoxModelItem) this.staffView.getHireClassCB().getSelectedItem();   
                         //System.out.println(this.rentalId);
                         String plateNumber = "";
                         try{
                         if(item.Id>0){
                          int row  = staffView.getHireTbl().getSelectedRow();
                          plateNumber = (String) staffView.getHireTbl().getValueAt(row, 0);
                         }
                         }catch(Exception e){
                             plateNumber = "-1";
                         }
                         Rental rental = new Rental();
                         
                         if(this.rentalId==-1){
                             try{
                                 DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                 Date startDate = staffView.getHireFromDP().getDate();
                                 Date endDate = staffView.getHireToDP().getDate();
                                 if(item.Id>0){
                                     String _priceFromDb_str = new CarClass().getSingleValue("price","classId",item.Id+"");
                                     double priceOfClass = Double.parseDouble(_priceFromDb_str);
                                    LocalDate d1 = new LocalDate(startDate);
                                    LocalDate d2 = new LocalDate(endDate);
                                    Days days = Days.daysBetween(d2, d1);
                                    double amountPaid = priceOfClass * days.getDays();       
                                     double deposit = 10 * priceOfClass;
                                     rental.setDepositAmount((float)deposit);
                                     rental.setAmountPaid((float)amountPaid);
                                 }
                                 rental.setStartDatetime(df.format(startDate));
                                 rental.setEndDateTime(df.format(endDate));
                             }catch(Exception e){
                                 rental.setEndDateTime(null);
                                 rental.setStartDatetime(null);
                             }
                                 rental.setReferenceCode(Utility.generateReferenceKey());
                         }
                         rental.setIsBooking(false);
                         rental.setCustomerid(this.personId);
                          if(item.Id>0){
                                 rental.setCarPlate(plateNumber);
                          }
                         
                         rental.setIsChauffeur(staffView.getHireChauffeuredCB().isSelected());
                         rental.setIsInsured(staffView.getHireInsuranceCB().isSelected());
                         
                         
                         HashMap<String,String> errors = rental.validate();
                             if(errors.size()>0){
                                 Popup.popup(errors, staffView);
                                 return;
                             }
                         
                         if(this.rentalId==-1){
                                 if(rental.add()){
                                     //System.out.println("ok added the rental data");
                                     JOptionPane.showMessageDialog(staffView, "Hiring was Successful", "Hiring Response", JOptionPane.INFORMATION_MESSAGE);
                                     this.actionHireClearFields();
                                 }else{
                                     JOptionPane.showMessageDialog(staffView, "Hiring Failed", "Hiring Response", JOptionPane.ERROR_MESSAGE);
 
                                 }
                         }else{
                             
                             rental.setRentalId(this.rentalId);
                             rental.setDepositAmount(this._rental_ref.getDepositAmount());
                             rental.setAmountPaid(this._rental_ref.getAmountPaid());
                             if(rental.update()){
                                   
                                 JOptionPane.showMessageDialog(staffView, "Hiring was Successful From a Booking Information", "Hiring Response", JOptionPane.INFORMATION_MESSAGE);
                                  this.actionHireClearFields();
                             }else{
                                 JOptionPane.showMessageDialog(staffView, "Hiring Failed", "Hiring Response", JOptionPane.ERROR_MESSAGE);
 
                             }
                             
                         }
         } else {
             System.out.append("");
         }
     }
 
     private void actionHireSearchRefCode() {
 
         String refCode = staffView.getHireRefCodeTxt().getText();
         if (!refCode.equals("")) {
 
             HashMap<String, String> record = new Rental().getRentalRecordByReferenceNumber(refCode);
             if (record == null) {
                 JOptionPane.showMessageDialog(staffView, "Reference Code '" + refCode + "' was Not Found", "Search Response", JOptionPane.ERROR_MESSAGE);
 
             } else {
                 this._rental_ref = new Rental();
                 Utility.fill(record, this._rental_ref);
                 this.populateFields(record, true);
             }
 
         } else {
             JOptionPane.showMessageDialog(staffView, "Please provide Reference Code", "Search Response", JOptionPane.ERROR_MESSAGE);
 
         }
     }
 
     private void actionHireLoadPerson() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private void actionHireClearFields() {
         staffView.getHireChauffeuredCB().setSelected(false);
         staffView.getHireClassCB().setSelectedIndex(0);
         staffView.getHireInsuranceCB().setSelected(false);
         staffView.getHireDateOfBirthDP().setDate(null);
         staffView.getHireFromDP().setDate(null);
         staffView.getHireToDP().setDate(null);
         staffView.getHireAddressTxt().setText("");
         staffView.getHireEmailTxt().setText("");
         staffView.getHireNameTxt().setText("");
         staffView.getHirePhoneTxt().setText("");
         staffView.getHireRefCodeTxt().setText("");
         staffView.getHireSurnameTxt().setText("");
         staffView.getHireTbl().removeAll();
         // staffView.getHireTbl().setModel(null);
         this.setEditablePersonFields(true);
         staffView.getHireNameTxt().requestFocusInWindow();
         this.lastRequest = false;
         this.foundAndFilled = false;
         this.personId = 0;
         this.rentalId = -1;
         this._rental_ref = null;
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
 
         // extend tab
         if (e.getSource().equals(staffView.getSurnameTxt2())
                 || e.getSource().equals(staffView.getPlateTxt3())
                 || e.getSource().equals(staffView.getRefCodeTxt2())) {
             actionExtendUpdateTable();
         }
 
         if (!staffView.getSearchModeCheckBox().isSelected()) {
             return;
         }
 
         if (this.lastRequest == false && !foundAndFilled) {
             //find the record
             this.lastRequest = true;
             ArrayList<HashMap<String, String>> records = new Person().findAllLike(this.getColumns());
             if (records != null) {
                 if (records.size() == 1) {
                     this.populateFields(records.get(0), false);
                 } else {
                     this.setButtonStatus(false);
                 }
             } else {
                 System.out.println("No records found");
             }
             this.lastRequest = false;
 
         } else {
             try {
                 Thread.sleep(TABLE_FILTERING_DELAY);
             } catch (InterruptedException ex) {
                 Logger.getLogger(StaffController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private void setEditablePersonFields(boolean status) {
         staffView.getHireNameTxt().setEnabled(status);
         staffView.getHireSurnameTxt().setEnabled(status);
         staffView.getHireEmailTxt().setEnabled(status);
         staffView.getHireDateOfBirthDP().setEnabled(status);
         staffView.getHireAddressTxt().setEnabled(status);
         staffView.getHirePhoneTxt().setEnabled(status);
         this.setButtonStatus(!status);
     }
 
     private void setButtonStatus(boolean status) {
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(StaffView.class);
 
         if (status) {
             staffView.getHirePersonLoadBtn().setIcon(resourceMap.getIcon("hirePersonLoadBtnActive.icon"));
         } else {
             staffView.getHirePersonLoadBtn().setIcon(resourceMap.getIcon("hirePersonLoadBtn.icon"));
         }
         staffView.getHirePersonLoadBtn().setEnabled(status);
 
     }
 
     private void populateFields(HashMap<String, String> data, boolean withHireFields) {
         if (data == null) {
             return;
         }
         try {
             this.personId = Integer.parseInt(data.get("personId"));
         } catch (Exception ex) {
             this.personId = 0;
         }
         staffView.getHireNameTxt().setText(data.get("name"));
         staffView.getHireSurnameTxt().setText(data.get("surname"));
 
         try {
             DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
             Date dateOfBirth = df.parse(data.get("dateOfBirth"));
             staffView.getHireDateOfBirthDP().setDate(dateOfBirth);
 
             if (withHireFields) {
 
                 Date startDate = df.parse(data.get("startDatetime"));
                 Date endDate = df.parse(data.get("endDatetime"));
 
                 staffView.getHireFromDP().setDate(startDate);
                 staffView.getHireToDP().setDate(endDate);
                 try {
                     this.rentalId = Integer.parseInt(data.get("rentalId"));
                 } catch (Exception e) {
                     this.rentalId = -1;
                 }
 
                 if ("1".equals(data.get("isChauffeur").toString())) {
                     staffView.getHireChauffeuredCB().setSelected(true);
                 }
 
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         staffView.getHireEmailTxt().setText(data.get("email"));
 
         staffView.getHireAddressTxt().setText(data.get("address"));
 
         staffView.getHirePhoneTxt().setText(data.get("phone"));
 
         if (withHireFields) {
         }
 
         foundAndFilled = true; //to stop the searching process
         this.setEditablePersonFields(false);
 
     }
 
     private HashMap<String, String> getColumns() {
         HashMap<String, String> columns = new HashMap<String, String>();
         if (!staffView.getHireNameTxt().getText().equals("")) {
             columns.put("name", staffView.getHireNameTxt().getText());
         }
         if (!staffView.getHireSurnameTxt().getText().equals("")) {
             columns.put("surname", staffView.getHireSurnameTxt().getText());
         }
 
         Date birthDate = staffView.getHireDateOfBirthDP().getDate();
         if (birthDate != null) {
             DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
             columns.put("dateOfBirth", df.format(birthDate));
         }
 
         if (!staffView.getHireEmailTxt().getText().equals("")) {
             columns.put("email", staffView.getHireAddressTxt().getText());
         }
         if (!staffView.getHireAddressTxt().getText().equals("")) {
             columns.put("address", staffView.getHireAddressTxt().getText());
         }
         if (!staffView.getHirePhoneTxt().getText().equals("")) {
             columns.put("phone", staffView.getHirePhoneTxt().getText());
         }
         return columns;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         // extension table
         if (e.getSource().equals(staffView.getjXTable1())) {
             actionExtensionSelectRow(staffView.getjXTable1().getSelectedRow());
         }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     private void updateRentalTbl() {
         staffView.getjXTable1().setModel(new Rental().getTableModel());
     }
 
     private void actionExtendUpdateTable() {
         filters = new HashMap<String, String>();
 
         if (!staffView.getSurnameTxt2().getText().isEmpty()) {
             filters.put("surname", staffView.getSurnameTxt2().getText());
         }
         if (!staffView.getRefCodeTxt2().getText().isEmpty()) {
             filters.put("model", staffView.getRefCodeTxt2().getText());
         }
         if (!staffView.getPlateTxt3().getText().isEmpty()) {
             filters.put("carPlate", staffView.getPlateTxt3().getText());
         }
 
         if (!filters.isEmpty()) {
             tableUpdateTask.cancel();
             tableUpdateTask = new TableUpdateTask(staffView.getjXTable1(), filters, new Rental());
             timer.schedule(tableUpdateTask, this.TABLE_FILTERING_DELAY);
         } else {
             updateRentalTbl();
         }
     }
 
     private void actionExtensionSelectRow(int selectedRow) {
 
         editingReturnId = (String) staffView.getjXTable1().getValueAt(selectedRow, 0);
         HashMap<String, String> rentalRecord = new Rental().findByPK(editingReturnId);
 
         DateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm");
         try {
             String substring = rentalRecord.get("endDatetime").substring(0, 16);
             Date parse = df.parse(substring);
             extensionDialog.getOldDateDP().setDate(parse);
         } catch (ParseException ex) {
             Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
         }
         // shows the dialog
         extensionDialog.setVisible(true);
     }
 
     private void actionExtensionDlgExtend() {
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Date date = extensionDialog.getNewDateDP().getDate();
         String query = "update rental set endDatetime='"
                 + df.format(extensionDialog.getNewDateDP().getDate())
                 + "' where rentalId='"
                 + editingReturnId
                 + "'";
         new Rental().nonQuery(query);
         actionExtensionDlgCancel();
         updateRentalTbl();
     }
 
     private void actionExtensionDlgCancel() {
         extensionDialog.getNewDateDP().setDate(null);
         extensionDialog.getOldDateDP().setDate(null);
         extensionDialog.setVisible(false);
     }
 }
