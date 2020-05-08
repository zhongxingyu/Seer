 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.DaoPersistence;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import restaurante.Beans.Receive;
 import restaurante.Beans.SalesOrder;
 import restaurante.Utilities.ContentValues;
 import restaurante.Utilities.Functions;
 
 /**
  *
  * @author Alan
  */
 public class ReceiveDao extends AbstractDao<Receive> {
     public static final String TABLE = "receive";
     /**
      * "id_customer", "value", "emissionDate", "dueDate", "paymentDate", "interest", 
      * "discount", "id_salesorder", "species", "id_branch", "description", "documentNumber", "documentType"
      */
     public static final String[] FIELDS = new String[] {
         "id_customer", "value", "emissionDate", "dueDate", "paymentDate", "interest", "discount", 
         "id_salesorder", "species", "id_branch", "description", "documentNumber", "documentType"
     };
     
     public ReceiveDao() throws Exception { }
     
     /*
      * Insert the current Bean
      */
     public int insert(Receive register) throws SQLException, Exception {
         return insert(register.getCustomer().getId(), register.getValue(), 
                 Functions.dateToDateStringSql(register.getEmissionDate()),
                 Functions.dateToDateStringSql(register.getDueDate()),
                 Functions.dateToDateStringSql(register.getPaymentDate()),
                 register.getInterest(), register.getDiscount(), register.getSaleOrder().getId(),
                 register.getSpecie().ordinal(), register.getBranch().getId(), 
                 register.getDescription(), register.getDocumentNumber(), register.getDocumentType());
     }
     
     /*
      * Update the current Bean
      */
     public void update(Receive register) throws SQLException, ParseException, Exception {
         update(register.getId(), register.getCustomer().getId(), register.getValue(), 
                 Functions.dateToDateStringSql(register.getEmissionDate()),
                 Functions.dateToDateStringSql(register.getDueDate()),
                 Functions.dateToDateStringSql(register.getPaymentDate()),
                 register.getInterest(), register.getDiscount(), register.getSaleOrder().getId(),
                 register.getSpecie().ordinal(), register.getBranch().getId(),
                 register.getDescription(), register.getDocumentNumber(), register.getDocumentType());
     }
     
     @Override
     protected Receive toBean(ContentValues values) throws SQLException, ClassNotFoundException, Exception {
         Receive register = new Receive();
         register.setId(values.getInt(FIELD_ID));
         register.setCustomer(new CustomersDao().findById(values.getInt(FIELDS[0])));
         register.setValue(values.getDouble(FIELDS[1]));
         register.setEmissionDate(values.getDate(FIELDS[2]));
         register.setDueDate(values.getDate(FIELDS[3]));
         register.setPaymentDate(values.getDate(FIELDS[4]));
         register.setInterest(values.getDouble(FIELDS[5])); 
         register.setDiscount(values.getDouble(FIELDS[6])); 
        register.setSaleOrder(new SalesOrderDao().findById(values.getInt(FIELDS[7])));
         register.setSpecie(SalesOrder.PaymentMethod.values()[values.getInt(FIELDS[8])]);
         register.setBranch(new BranchesDao().findById(values.getInt(FIELDS[9])));
         register.setDescription(values.getString(FIELDS[10]));
         register.setDocumentNumber(values.getString(FIELDS[11]));
         register.setDocumentType(values.getString(FIELDS[12]));
         return register;
     }
 
     @Override
     protected String getTableName() {
         return TABLE;
     }
 
     @Override
     protected String[] getFields() {
         return FIELDS;
     }
 
     public void saveBatch(Receive[] receiveList) throws Exception {
         //Start the transaction to save batch of products from menu
         try {
             this.startTransation();
             for(Receive receive : receiveList) {
                 update(receive);
             }
             this.finishTransaction();
         } catch (Exception e) {
             this.finishTransaction();
             throw e;
         }
         //Remember to finish the transaction!
     }
 }
