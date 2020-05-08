 package za.co.voxtelecom.abmigration.main;
 
 import com.tcm.utils.guid.GuidGenerator;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.sql.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import za.co.voxtelecom.abmigration.util.AccountDateSoldPair;
 import za.co.voxtelecom.abmigration.util.AccountServiceRequestNoPair;
 import za.co.voxtelecom.abmigration.util.DatabaseLogger;
 
 /**
  *
  * @author khaya
  */
 public class AnnuityMigration {
 
     private Connection bigBlueConnection;
     private Connection voxzalConnection;
     private PreparedStatement bigBluePreparedStatement;
     private ResultSet bigBlueResultSet;
     private PreparedStatement voxcontractPreparedStatement;
     private PreparedStatement voxcontractInfoPreparedStatement;
     private PreparedStatement voxcontractItemBillingInfoPreparedStatement;
     private PreparedStatement voxcontractProductPreparedStatement;
     private PreparedStatement voxcontractHasProductPreparedStatement;
     private PreparedStatement bigBlueSubQueryPreparedStatement;
     private ResultSet bigBlueSubQueryResultSet;
     PreparedStatement bigBlueCustomerPreparedStatement;
     PreparedStatement bigBlueCancelReasonPreparedStatement;
     PreparedStatement bigBlueSalesStaffPreparedStatement;
     PreparedStatement bigBlueSourceBusinessPreparedStatement;
     PreparedStatement bigBlueDataSourceDataPreparedStatement;
     PreparedStatement bigBluePaymentPreparedStatement;
     PreparedStatement bigBlueProfiitPreparedStatement;
     PreparedStatement bigBlueRegionPreparedStatement;
     PreparedStatement bigBlueProductPreparedStatement;
     PreparedStatement voxzalCustomerPreparedStatement;
     PreparedStatement voxzalUsrPreparedStatement;
     PreparedStatement voxzalDataSourcePreparedStatement;
     PreparedStatement voxzalCancelPreparedStatement;
     PreparedStatement voxzalSourcePreparedStatement;
     PreparedStatement voxzalPaymentPreparedStatement;
     PreparedStatement voxzalProfitPreparedStatement;
     PreparedStatement voxzalRegionPreparedStatement;
     PreparedStatement voxzalLobPreparedStatement;
     private ResultSet bigBlueTempResultSet;
     private ResultSet bigBlueProductResultSet;
     private PreparedStatement voxzalPlanTypePreparedStatement;
     private PreparedStatement voxzalBaseProductPreparedStatement;
     private PreparedStatement voxzalLobProductPreparedStatement;
     private PreparedStatement voxzalGlobalCustomerPreparedStatement;
     private PreparedStatement voxzalReadGlobalCustomerPreparedStatement;
     private ResultSet voxzalTempResultSet;
     private String voxcontractId = null;
     private String voxcontractInfoId = null;
     private String batchNumber = null;
     private static Logger importLogger;
     private static Logger errorLogger;
     private static DatabaseLogger databaseLogger;
     private static FileHandler importHandler;
     private static FileHandler errorHandler;
     private Integer masterNumberId;
     private boolean ignoreAnnuity = false;
 
     static {
         System.out.println("Setting up loggers");
         try {
             errorHandler = new FileHandler("/home/pault/Desktop/errors.log", true);
             importHandler = new FileHandler("/home/pault/Desktop/imports.log", true);
             errorHandler.setFormatter(new SimpleFormatter());
             importHandler.setFormatter(new SimpleFormatter());
 
             errorLogger = Logger.getAnonymousLogger();
             importLogger = Logger.getAnonymousLogger();
 
             errorLogger.addHandler(errorHandler);
             importLogger.addHandler(importHandler);
 
             databaseLogger = new DatabaseLogger();
             //
         } catch (IOException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // get BigBlue connection
 
         AnnuityMigration am = new AnnuityMigration();
         am.bigBlueConnection = am.getConnection("bigblue.config");
         am.voxzalConnection = am.getConnection("voxzal.config");
         if (args.length > 1) {
             am.batchNumber = args[1];
         } else {
             am.batchNumber = "all";
         }
         if (am.bigBlueConnection != null) {
 
             am.initialiseStatements();
             am.getAnnuities(args[0]);
            am.executeBatches();
         }
     }
 
     /**
      * Get a data base connection
      *
      */
     public Connection getConnection(String fileName) {
         Connection connection = null;
         InputStream is = null;
         Properties prop = null;
         try {
             prop = new Properties();
             is = new FileInputStream(fileName);
             prop.load(is);
             String url = prop.getProperty("url");
             connection = DriverManager.getConnection(url, prop);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             errorLogger.info("FileNotFoundException getting connection for database : " + fileName);
         } catch (Exception ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             errorLogger.info("Exception getting connection for database : " + fileName);
         } finally {
             try {
                 is.close();
             } catch (IOException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return connection;
     }
 
     private void getAnnuities(String criteria) {
         if (criteria != null) {
             try {
                 if (criteria.equals("a")) {
                     System.out.println("Creating Voxcontracts Using ProductId");
                     long start = System.currentTimeMillis();
                     bigBluePreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ProductID='1888'");
                     bigBlueResultSet = bigBluePreparedStatement.executeQuery();
                     System.out.println("Time to get result set from bigblue" + (System.currentTimeMillis() - start) + "ms");
                     createVoxcontractPerAccountIdProductId();
                 } else if (criteria.equals("b")) {
                     System.out.println("Creating Voxcontracts Using ServiceRequestNo");
                     //select AccountId,ServiceRequestNo from AnnuityBilling where  ProductId <>'1888' and ( ServiceRequestNo<>'')
                     bigBluePreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ServiceRequestNo <> '' and ProductId <> '1888'");
                     bigBlueResultSet = bigBluePreparedStatement.executeQuery();
                     createVoxcontractPerAccountIdServiceRequestNo();
                 } else if (criteria.equals("c")) {
                     System.out.println("Creating Voxcontracts Using DateSold");
                     bigBluePreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where (ServiceRequestNo is Null or ServiceRequestNo='') and ProductId<>'1888'");
                     bigBlueResultSet = bigBluePreparedStatement.executeQuery();
                     createVoxcontractPerAccountIdDateSold();
                 }
 
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException selecting annuities : " + ex);
             }
         }
     }
 
     /**
      *
      * initialise prepared statements
      *
      */
     public void initialiseStatements() {
         System.out.println("Initialising Statements");
         try {
 
 
             voxcontractPreparedStatement = voxzalConnection.prepareStatement("INSERT INTO voxcontract(voxcontract_id,date_ended,date_started,contract_number,account_manager,division,customer_id,original_account_manager,quote_id, voxcontract_info_id)  VALUES (?,?,?,?,?,?,?,?,?,?)");
             voxcontractProductPreparedStatement = voxzalConnection.prepareStatement("INSERT INTO voxcontract_product(voxcontract_product_id,config_device_info,lob_id,lob_product_id,name,product_code,plan_type,voxcontract_product_package_id)  VALUES (?,?,?,?,?,?,?,?)");
             voxcontractInfoPreparedStatement = voxzalConnection.prepareStatement("INSERT INTO  voxcontract_info(voxcontract_info_id,expiry_date,note,ticket_number,voxcontract_expiry_type,voxcontract_period,voxcontract_status, voxcontract_type,group_company,reseller,voxcontract_datasource,voxcontract_source,voxcontract_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
             voxcontractItemBillingInfoPreparedStatement = voxzalConnection.prepareStatement("INSERT INTO voxcontract_item_billing_info(billing_info_id,billing_start_date,cancellation,cancelled_date,commisionable_value,core_price,installed_date,is_active,is_billable,is_prorata,lob_price,master_number,note,previous_master_number,salesman_id,selling_price,setup_fee,sold_date,upgrade_date, upgrade_downgrade,voxcontract_debit_order_day,account_manager_id,cancellation_reason_id,payment_type_id,profit_center_id,region_id,voxcontract_id,voxcontract_product_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             voxcontractHasProductPreparedStatement = voxzalConnection.prepareStatement("INSERT INTO voxcontract_has_product(voxcontract_id,voxcontract_product_id,billing_info_id) VALUES (?,?,?)");
 
             bigBlueCustomerPreparedStatement = bigBlueConnection.prepareStatement("select BrilliantAccountNumber,CompanyName from Account where AccountID=?");
             bigBlueCancelReasonPreparedStatement = bigBlueConnection.prepareStatement("select CancellationReason from CancellationReason where CancellationReasonID=?");
             bigBlueSalesStaffPreparedStatement = bigBlueConnection.prepareStatement("select Code from SalesStaff where StaffID=?");
             bigBlueSourceBusinessPreparedStatement = bigBlueConnection.prepareStatement("select SourceOfBusiness from Source where SourceID=?");
             bigBluePaymentPreparedStatement = bigBlueConnection.prepareStatement("select PaymentOption from PaymentOption where PaymentOptionID=?");
             bigBlueProfiitPreparedStatement = bigBlueConnection.prepareStatement("select ProfitCentreCode from ProfitCentre where ProfitCentreID=?");
             bigBlueRegionPreparedStatement = bigBlueConnection.prepareStatement("select Region from Region where RegionID=?");
             bigBlueProductPreparedStatement = bigBlueConnection.prepareStatement("select ProductCode from Product where ProductID=?");
 
             bigBlueDataSourceDataPreparedStatement = bigBlueConnection.prepareStatement("select SourceOfData from SourceDatabase where SourceDatabaseID=?");
 
 
 
             voxzalCustomerPreparedStatement = voxzalConnection.prepareStatement("select id from customer where pastel_code=?");
             voxzalCancelPreparedStatement = voxzalConnection.prepareStatement("select cancellation_reason_id from cancellation_reason where cancellation_reason=?");
             voxzalUsrPreparedStatement = voxzalConnection.prepareStatement("select id from usr where usr_name=?");
             voxzalDataSourcePreparedStatement = voxzalConnection.prepareStatement("select id from voxcontract_datasource where name=?");
             voxzalSourcePreparedStatement = voxzalConnection.prepareStatement("select SourceID from voxcontract_source where SourceOfBusiness=?");
             voxzalPaymentPreparedStatement = voxzalConnection.prepareStatement("select id from payment_type where type_name=?");
             voxzalProfitPreparedStatement = voxzalConnection.prepareStatement("select profit_center_id from profit_center where name like ?");
             voxzalRegionPreparedStatement = voxzalConnection.prepareStatement("select region_id from region where name=?");
             voxzalReadGlobalCustomerPreparedStatement = voxzalConnection.prepareStatement("select customer_id from global_customer where account_code=?");
             voxzalLobPreparedStatement = voxzalConnection.prepareStatement("select base_product_id,lob_id,lob_product_id,name from lob_product where lob_product_code=?");
             voxzalPlanTypePreparedStatement = voxzalConnection.prepareStatement("select plan_type_id from base_product where base_product_id=?");
 
 
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             errorLogger.info("SQLException initialising statements ");
         }
     }
 
     private String getId() {
         return GuidGenerator.getNext();
     }
 
     private String getAccountManager(Integer salesmanId) {
         String usrId = null;
         String code = null;
         if (salesmanId != 0) {
             try {
 
                 bigBlueSalesStaffPreparedStatement.setInt(1, salesmanId);
                 bigBlueTempResultSet = bigBlueSalesStaffPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 code = bigBlueTempResultSet.getString("Code");
                 if (code != null) {
                     code = code.equals("allie-sue") ? "AllieSue" : code;
                     code = code.equals("pauld") ? "jeand" : code;
 
                     voxzalUsrPreparedStatement.setString(1, code);
                     voxzalTempResultSet = voxzalUsrPreparedStatement.executeQuery();
                     voxzalTempResultSet.next();
                     usrId = voxzalTempResultSet.getString("id");
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, code + "\for SalesMan " + salesmanId, ex);
                 errorLogger.info("SQLException  in getting Salesman : " + salesmanId + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "sales_man_id", salesmanId, 1, "sales_man_id_strace", ex.toString()); //masternumberId,voxontractId,columnValue,columnName,imported,stackTrace
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return usrId;
     }
 
     private String getCancellationReason(Integer cancellationReasonId) {
         String cancellationReason = null;
         if (cancellationReasonId != 0) {
             try {
                 bigBlueCancelReasonPreparedStatement.setInt(1, cancellationReasonId);
                 bigBlueTempResultSet = bigBlueCancelReasonPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 String cr = bigBlueTempResultSet.getString("CancellationReason");
 
                 voxzalCancelPreparedStatement.setString(1, cr);
                 voxzalTempResultSet = voxzalCancelPreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 cancellationReason = voxzalTempResultSet.getString("cancellation_reason_id");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException  in getting cancellation reason : " + cancellationReasonId + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "cancellation_reason_id", cancellationReasonId, 1, "cancellation_reason_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return cancellationReason;
     }
 
     private String getVoxcontractDataSource(Integer sourceDatabaseId) {
         String voxcontractDatasourceId = null;
         String sourceOfData = null;
         if (sourceDatabaseId != 0) {
             try {
                 bigBlueDataSourceDataPreparedStatement.setInt(1, sourceDatabaseId);
                 bigBlueTempResultSet = bigBlueDataSourceDataPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 sourceOfData = bigBlueTempResultSet.getString("SourceOfData");
                 voxzalDataSourcePreparedStatement.setString(1, sourceOfData);
                 voxzalTempResultSet = voxzalDataSourcePreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 voxcontractDatasourceId = voxzalTempResultSet.getString("id");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException  in getting Voxcontract Data Source : " + sourceDatabaseId + "\t" + sourceOfData + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "source_database_id", sourceDatabaseId, 1, "source_database_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return voxcontractDatasourceId;
 
     }
 
     private String getVoxcontractSource(Integer sourceId) {
         String voxcontractSourceId = null;
         String sourceOfBusiness = null;
         if (sourceId != 0) {
             try {
                 bigBlueSourceBusinessPreparedStatement.setInt(1, sourceId);
                 bigBlueTempResultSet = bigBlueSourceBusinessPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 sourceOfBusiness = bigBlueTempResultSet.getString("SourceOfBusiness");
                 voxzalSourcePreparedStatement.setString(1, sourceOfBusiness);
                 voxzalTempResultSet = voxzalSourcePreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 voxcontractSourceId = voxzalTempResultSet.getString("SourceID");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException  in getting Voxcontract Source  : " + sourceId + "\t " + sourceOfBusiness + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "source_id", sourceId, 1, "source_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return voxcontractSourceId;
 
 
     }
 
     private String getPaymentType(Integer paymentOptionId) {
         String paymentTypeId = null;
         String paymentOption = null;
         if (paymentOptionId != 0) {
             try {
                 bigBluePaymentPreparedStatement.setInt(1, paymentOptionId);
                 bigBlueTempResultSet = bigBluePaymentPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 paymentOption = bigBlueTempResultSet.getString("PaymentOption");
                 paymentOption = paymentOption.equals("Credit Cards") ? "Credit Card" : paymentOption;
                 paymentOption = paymentOption.equals("Cheque/EFT") ? "Cheque" : paymentOption;
                 paymentOption = paymentOption.equals("Prepaids") ? "PrePaid" : paymentOption;
 
                 voxzalPaymentPreparedStatement.setString(1, paymentOption);
                 voxzalTempResultSet = voxzalPaymentPreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 paymentTypeId = voxzalTempResultSet.getString("id");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, paymentOption, ex);
                 errorLogger.info("SQLException  in getting Payment Type : " + paymentOptionId + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "payment_option_id", paymentOptionId, 1, "payment_option_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return paymentTypeId;
     }
 
     private String getProfitCenter(Integer profitCentreId) {
         String profitCenterId = null;
         if (profitCentreId != 0) {
             try {
                 bigBlueProfiitPreparedStatement.setInt(1, profitCentreId);
                 bigBlueTempResultSet = bigBlueProfiitPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 String profitCentreCode = bigBlueTempResultSet.getString("ProfitCentreCode");
                 voxzalProfitPreparedStatement.setString(1, profitCentreCode + "%");
                 voxzalTempResultSet = voxzalProfitPreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 profitCenterId = voxzalTempResultSet.getString("profit_center_id");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException  in getting Profit Center : " + profitCentreId + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "profit_center_id", profitCentreId, 1, "profit_center_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return profitCenterId;
 
     }
 
     private String getRegion(Integer regionId) {
         String rid = null;
         if (regionId != 0) {
             try {
                 bigBlueRegionPreparedStatement.setInt(1, regionId);
                 bigBlueTempResultSet = bigBlueRegionPreparedStatement.executeQuery();
                 bigBlueTempResultSet.next();
                 String region = bigBlueTempResultSet.getString("Region");
                 voxzalRegionPreparedStatement.setString(1, region);
                 voxzalTempResultSet = voxzalRegionPreparedStatement.executeQuery();
                 voxzalTempResultSet.next();
                 rid = voxzalTempResultSet.getString("region_id");
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
                 errorLogger.info("SQLException  in getting Region : " + regionId + " for MasterNumber " + masterNumberId);
                 databaseLogger.logError(masterNumberId, voxcontractId, "region_id", regionId, 1, "region_id_strace", ex.toString());
             } finally {
                 close(bigBlueTempResultSet);
                 close(voxzalTempResultSet);
             }
         }
         return rid;
     }
 
     private String getVoxcontractDebitOrderDay(String paymentday) {
         String voxcontractDebitOrderDay = "ONE";
         if (paymentday.equals("15")) {
             voxcontractDebitOrderDay = "FIFTEEN";
         } else if (paymentday.equals("25")) {
             voxcontractDebitOrderDay = "TWENTY_FIVE";
         } else if (paymentday.equals("30")) {
             voxcontractDebitOrderDay = "THIRTY";
         }
         return voxcontractDebitOrderDay;
     }
 
     private String getGlobalCustomer(Integer accountId) {
         System.out.println(" Getting a GlobalCustomer ");
         String customerId = null;
         String pastelCode = null;
         String companyName = null;
         try {
 
             bigBlueCustomerPreparedStatement.setInt(1, accountId);
             bigBlueTempResultSet = bigBlueCustomerPreparedStatement.executeQuery();
             bigBlueTempResultSet.next();
             pastelCode = bigBlueTempResultSet.getString("BrilliantAccountNumber");
             companyName = bigBlueTempResultSet.getString("CompanyName");
             System.out.println("have pastel code" + pastelCode);
             System.out.println("have company name" + companyName);
             voxzalReadGlobalCustomerPreparedStatement.setString(1, pastelCode);
             voxzalTempResultSet = voxzalReadGlobalCustomerPreparedStatement.executeQuery();
             voxzalTempResultSet.next();
             if (voxzalTempResultSet.isFirst()) {
                 customerId = voxzalTempResultSet.getString("customer_id");
             } else {
                 System.out.println("No record for :" + pastelCode);
                 voxzalCustomerPreparedStatement.setString(1, pastelCode);
                 voxzalTempResultSet = voxzalCustomerPreparedStatement.executeQuery();
                 //if(voxzalTempResultSet.last())
                 voxzalTempResultSet.next();
                 if (voxzalTempResultSet.isFirst()) {
                     // if there is a matching customer search for the global customer and update it
                     customerId = voxzalTempResultSet.getString("id");
                    voxzalReadGlobalCustomerPreparedStatement.setString(1, customerId);
                    voxzalTempResultSet = voxzalReadGlobalCustomerPreparedStatement.executeQuery();
                     voxzalTempResultSet.next();
                     if (voxzalTempResultSet.isFirst()) {
                         //if global customer exists update the global customer
                         updateGlobalCustomer(customerId, companyName, pastelCode, accountId);
                     } else {
                         //if global customer doesnt exist add a new global customer
                         customerId = getId();
                         addGlobalCustomer(customerId, customerId, pastelCode, accountId);
                     }
 
                 } else {
                     // if there is no customer create a  global customer and log it
                     customerId = getId();
                     addGlobalCustomer(customerId, customerId, pastelCode, accountId);
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, pastelCode, ex);
             errorLogger.info("SQLException  in getting Global Customer : " + accountId + " for MasterNumber " + masterNumberId);
             databaseLogger.logError(masterNumberId, voxcontractId, "account_id", accountId, 1, "account_id_strace", ex.toString());
         } finally {
 //            close(bigBlueTempResultSet);
 //            close(voxzalTempResultSet);
         }
         if (customerId == null) {
             System.out.println(" ***** Customer Id is Null ****** for MasterNumberId " + masterNumberId);
         }
         return customerId;
 
 
     }
 
     private String getVoxcontractPeriod() {
         return "TWENTY_FOUR";
     }
 
     private String getVoxcontractStatus() {
         return "APPROVED";
     }
 
     private String getVoxcontractType() {
         return "EVERGREEN";
     }
 
     private String getGroupCompany(String companySourceCode) {
         String groupCompanyId = "2311CBD7-5772-66A6-B6D7-01B3A1C0C43A"; //vox datapro
         if (companySourceCode.equals("VX")) {
             groupCompanyId = "3CA1EA00-A89C-ED57-0DE4-32154413E4CA"; // vox exchange
         } else if (companySourceCode.equals("A")) {
             groupCompanyId = "1E8C3AEB-4BBF-7E3D-360D-121216F10D19"; // atlantic
         } else if (companySourceCode.equals("O")) {
             groupCompanyId = "1E8C3AEB-4BBF-7E3D-360D-121216F10D19";//orion
         }
         return groupCompanyId;
     }
 
     private boolean getBoolean(String b) {
         boolean booleanResult = false;
         if (b != null && !b.equals("0")) {
             booleanResult = true;
         }
         return booleanResult;
     }
 
     private Integer getPreviousMasterNumber(String oldMasterNumber) {
         Integer previousMasterNumber = 0;
         try {
             previousMasterNumber = Integer.valueOf(oldMasterNumber);
         } catch (Exception ex) {
             //TODO log this exception so that invalid master numbers can be tracked
             // databaseLogger.logError(masterNumberId, voxcontractId, masterNumberId, masterNumberId, masterNumberId, masterNumberId, masterNumberId);
         }
         return previousMasterNumber;
     }
 
     /*
      * 
      * updates a global customer if the customerName,accountCode is null or 
      * an empty string
      * 
      */
     private void updateGlobalCustomer(String customerId, String customerName, String accountCode, Integer accountId) {
         importLogger.info("Updating a  Global Customer " + customerName + "for Master Number " + masterNumberId);
         databaseLogger.logImport(masterNumberId, customerId, accountCode, customerName, accountId, "update");
         try {
             voxzalGlobalCustomerPreparedStatement = voxzalConnection.prepareStatement("update global_customer set account_code=?,customer_name=? where customer_id=?");
             voxzalGlobalCustomerPreparedStatement.setString(1, customerId);
             voxzalGlobalCustomerPreparedStatement.setString(2, customerName);
             voxzalGlobalCustomerPreparedStatement.setString(3, accountCode);
             voxzalGlobalCustomerPreparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             errorLogger.info("SQLException  in adding Global Customer : " + customerId + " for MasterNumber " + masterNumberId);
             databaseLogger.logError(masterNumberId, voxcontractId, "account_id", accountId, 1, "account_id_strace", ex.toString());
         } finally {
             try {
                 voxzalGlobalCustomerPreparedStatement.close();
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private void addGlobalCustomer(String customerId, String customerName, String accountCode, Integer accountId) {
         String systemId = "E4C79946-CG62-1832-79A4-69B3F159EF47";
         String globalCustomerGuid = getId();
         importLogger.info("Adding a New Global Customer " + customerName + "for Master Number " + masterNumberId);
 
         databaseLogger.logImport(masterNumberId, customerId, accountCode, customerName, globalCustomerGuid, systemId, accountId, "new");
         try {
             voxzalGlobalCustomerPreparedStatement = voxzalConnection.prepareStatement("insert into global_customer(customer_id,customer_name,account_code,customer_global_guid,system_id) values(?,?,?,?,?)");
             voxzalGlobalCustomerPreparedStatement.setString(1, customerId);
             voxzalGlobalCustomerPreparedStatement.setString(2, customerName);
             voxzalGlobalCustomerPreparedStatement.setString(3, accountCode);
             voxzalGlobalCustomerPreparedStatement.setString(4, globalCustomerGuid);
             voxzalGlobalCustomerPreparedStatement.setString(5, systemId);
             voxzalGlobalCustomerPreparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             errorLogger.info("SQLException  in adding Global Customer : " + customerId + " for MasterNumber " + masterNumberId);
             databaseLogger.logError(masterNumberId, voxcontractId, "account_id", accountId, 1, "account_id_strace", ex.toString());
         } finally {
             try {
                 voxzalGlobalCustomerPreparedStatement.close();
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
     }
 
     private void addProducts(String pc) {
         String desc = null;
         try {
             bigBlueProductPreparedStatement = bigBlueConnection.prepareStatement("select * from Product where ProductCode=?");
             bigBlueProductPreparedStatement.setString(1, pc);
             bigBlueProductResultSet = bigBlueProductPreparedStatement.executeQuery();
             bigBlueProductResultSet.next();
             String bpid = getId();
             desc = getName(bigBlueProductResultSet.getString("Description"), pc);
             voxzalBaseProductPreparedStatement = voxzalConnection.prepareStatement("insert into base_product(base_product_id,product_code,name,description,is_active,plan_type_id,supplier_cost,recommended_core_cost,recommended_selling_price,recommended_billing_type_guid,last_updated) values(?,?,?,?,?,?,?,?,?,?,now())");
 
             voxzalBaseProductPreparedStatement.setString(1, bpid);
             voxzalBaseProductPreparedStatement.setString(2, pc); //productCode
             voxzalBaseProductPreparedStatement.setString(3, desc); // name
             voxzalBaseProductPreparedStatement.setString(4, desc);//description
             voxzalBaseProductPreparedStatement.setInt(5, getIsActive(bigBlueProductResultSet.getString("Active"))); //is_active
             voxzalBaseProductPreparedStatement.setInt(6, 1);//plan_type_id
             voxzalBaseProductPreparedStatement.setDouble(7, bigBlueProductResultSet.getDouble("SupplierCostPrice"));//supplier_cost
             voxzalBaseProductPreparedStatement.setDouble(8, bigBlueProductResultSet.getDouble("CoreCost"));// recommended_core_cost
             voxzalBaseProductPreparedStatement.setDouble(9, bigBlueProductResultSet.getDouble("ListPrice"));//recommended_selling_price
             voxzalBaseProductPreparedStatement.setString(10, "5E9DB8F5-FFB0-4AA7-90E0-7928F730D785");//recommended_billing_type_guid
             voxzalBaseProductPreparedStatement.executeUpdate();
             String lpid = getId();
             voxzalLobProductPreparedStatement = voxzalConnection.prepareStatement("insert into lob_product(lob_product_id,base_product_id,lob_id,lob_product_code,name,description,selling_price,lob_cost,product_billing_type_guid,is_active,is_deleted,last_updated) values(?,?,?,?,?,?,?,?,?,?,?,now())");
             voxzalLobProductPreparedStatement.setString(1, lpid);
             voxzalLobProductPreparedStatement.setString(2, bpid);
             voxzalLobProductPreparedStatement.setString(3, "2311CBD7-5772-66A6-B6D7-01B3A1C0C43A");//lob_id
             voxzalLobProductPreparedStatement.setString(4, pc); //lob_product_code
             voxzalLobProductPreparedStatement.setString(5, desc); //name
             voxzalLobProductPreparedStatement.setString(6, desc);//description
             voxzalLobProductPreparedStatement.setDouble(7, bigBlueProductResultSet.getDouble("ListPrice")); //selling_price
             voxzalLobProductPreparedStatement.setDouble(8, bigBlueProductResultSet.getDouble("CoreCost")); //lob_cost
             voxzalLobProductPreparedStatement.setString(9, "5E9DB8F5-FFB0-4AA7-90E0-7928F730D785"); //product_billing_type_guid
             voxzalLobProductPreparedStatement.setInt(10, getIsActive(bigBlueProductResultSet.getString("Active"))); //is_active
             voxzalLobProductPreparedStatement.setInt(11, 0); //is_deleted
             voxzalLobProductPreparedStatement.executeUpdate();
 
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, desc, ex);
         } finally {
             try {
                 voxzalBaseProductPreparedStatement.close();
                 voxzalLobProductPreparedStatement.close();
                 bigBlueProductResultSet.close();
                 bigBlueProductPreparedStatement.close();
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         }
     }
 
     private int getIsActive(String s) {
         if (s != null && s.equals("y")) {
             return 1;
         } else {
             return 0;
         }
     }
 
     private String getName(String desc, String productCode) {
         if (desc != null) {
             return desc = (desc.length() > 100) ? desc.substring(99) : desc;
         } else {
             return productCode;
         }
     }
 
     //TODO change this code to use an inner join
     private void setVoxcontractProductPreparedStatement(Integer productId) {
         String productCode = null;
         try {
 
             bigBlueProductPreparedStatement.setInt(1, productId);
             bigBlueTempResultSet = bigBlueProductPreparedStatement.executeQuery();
             bigBlueTempResultSet.next();
             productCode = bigBlueTempResultSet.getString("ProductCode");
 
             voxzalLobPreparedStatement.setString(1, productCode);
             voxzalTempResultSet = voxzalLobPreparedStatement.executeQuery();
             voxzalTempResultSet.next();
             //if the resultset is empty dont create a base product and lob product
             // set ignoreAnnuity to true
             // and return
             if (!voxzalTempResultSet.isFirst()) {
                 System.out.println("Skipping product : " + productCode);
                 databaseLogger.logError(masterNumberId, voxcontractId, "product_id", productId, 0, "product_id_strace", "missing product");
                 ignoreAnnuity = true;
                 return;
 //                addProducts(productCode);
 //                voxzalTempPreparedStatement = voxzalConnection.prepareStatement("select base_product_id,lob_id,lob_product_id,name from lob_product where lob_product_code=?");
 //                voxzalTempPreparedStatement.setString(1, productCode);
 //                voxzalTempResultSet = voxzalTempPreparedStatement.executeQuery();
 //                voxzalTempResultSet.next();
             }
             String baseProductId = voxzalTempResultSet.getString("base_product_id");
             String lobId = voxzalTempResultSet.getString("lob_id");
             String lobProductId = voxzalTempResultSet.getString("lob_product_id");
             String name = voxzalTempResultSet.getString("name");
             voxzalPlanTypePreparedStatement.setString(1, baseProductId);
             voxzalTempResultSet = voxzalPlanTypePreparedStatement.executeQuery();
             voxzalTempResultSet.next();
             String planType = voxzalTempResultSet.getString("plan_type_id");
             //voxcontractProductPreparedStatement.setString(1, getId()); //voxcontractract_product_id
             voxcontractProductPreparedStatement.setString(2, null); //config_device_info
             voxcontractProductPreparedStatement.setString(3, lobId); //lob_id
             voxcontractProductPreparedStatement.setString(4, lobProductId); //lob_product_id
             voxcontractProductPreparedStatement.setString(5, name); //name
             voxcontractProductPreparedStatement.setString(6, productCode); //product_code
             voxcontractProductPreparedStatement.setString(7, planType); //plan_type
             voxcontractProductPreparedStatement.setString(8, null); //voxcontract_product_package_id
             voxcontractProductPreparedStatement.addBatch();
 
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, productCode, ex);
         } finally {
             close(bigBlueTempResultSet);
             close(voxzalTempResultSet);
         }
     }
 
     private void setVoxcontractInfoPreparedStatement(ResultSet resultSet, String globalCustomerId) {
         try {
             masterNumberId = resultSet.getInt("masternumberid");
             voxcontractPreparedStatement.setString(1, voxcontractId); //voxcontract_id
             voxcontractPreparedStatement.setDate(2, null); //date_ended
             voxcontractPreparedStatement.setString(4, null);//ticket_number
             voxcontractPreparedStatement.setString(6, null);// division
             voxcontractPreparedStatement.setString(7, globalCustomerId); //customer_id
             voxcontractPreparedStatement.setString(9, null); //quote_id
             voxcontractPreparedStatement.setString(10, voxcontractInfoId); //voxcontract_info
             voxcontractPreparedStatement.setDate(3, resultSet.getDate("datesold")); //date_started or is it impdate
             voxcontractPreparedStatement.setString(5, getAccountManager(resultSet.getInt("salesmanid")));// account_manager
             voxcontractPreparedStatement.setString(8, getAccountManager(resultSet.getInt("originalsalesmanid"))); //original_account_manager
             voxcontractPreparedStatement.addBatch();
 
             voxcontractInfoPreparedStatement.setString(1, voxcontractInfoId); //voxcontract_info
             voxcontractInfoPreparedStatement.setDate(2, null); //expiry_date
             voxcontractInfoPreparedStatement.setString(3, null); //note
             voxcontractInfoPreparedStatement.setString(4, null); //ticket_number
             voxcontractInfoPreparedStatement.setString(5, null); //voxcontract_expiry_type
             voxcontractInfoPreparedStatement.setString(6, getVoxcontractPeriod()); //voxcontract_period
             voxcontractInfoPreparedStatement.setString(7, getVoxcontractStatus()); //voxcontract_status
             voxcontractInfoPreparedStatement.setString(8, getVoxcontractType()); //voxcontract_type
             voxcontractInfoPreparedStatement.setString(9, getGroupCompany(resultSet.getString("companysourcecode"))); //group_company
             voxcontractInfoPreparedStatement.setString(10, null); //reseller
             voxcontractInfoPreparedStatement.setString(11, getVoxcontractDataSource(resultSet.getInt("sourcedatabaseid"))); //voxcontract_datasource
             voxcontractInfoPreparedStatement.setString(12, getVoxcontractSource(resultSet.getInt("sourceid"))); //voxcontract_source
             voxcontractInfoPreparedStatement.setString(13, voxcontractId); //voxcontract_id
             voxcontractInfoPreparedStatement.addBatch();
 
         } catch (SQLException ex) {
             //log and skip
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void setPreparedStatementValues(ResultSet resultSet) {
         try {
 
 
             //get ids
             String voxcontractProductId = getId();
             String voxcontractItemBillingInfoId = getId();
 
             //setting voxcontractInfo values
             //TODO run this code when its the last resultset row
 
 
             //setting voxcontractProduct values
             voxcontractProductPreparedStatement.setString(1, voxcontractProductId); //voxcontractract_product_id
             setVoxcontractProductPreparedStatement(resultSet.getInt("productid"));
             if (!ignoreAnnuity) {
                 //setting voxcontractItemBillingInfo values
                 voxcontractItemBillingInfoPreparedStatement.setString(1, voxcontractItemBillingInfoId); // billing_info_id
                 voxcontractItemBillingInfoPreparedStatement.setDate(2, null); //billing_start_date
                 voxcontractItemBillingInfoPreparedStatement.setBoolean(3, getBoolean(resultSet.getString("cancellation"))); //cancellation
                 voxcontractItemBillingInfoPreparedStatement.setDate(4, resultSet.getDate("cancellationdate")); //cancellation_date
                 voxcontractItemBillingInfoPreparedStatement.setDouble(5, resultSet.getDouble("costprice")); //commisionable_value
                 voxcontractItemBillingInfoPreparedStatement.setDouble(6, resultSet.getDouble("corebilling")); //core_price
                 voxcontractItemBillingInfoPreparedStatement.setDate(7, resultSet.getDate("installeddate")); //installed_date
                 voxcontractItemBillingInfoPreparedStatement.setBoolean(8, getBoolean(resultSet.getString("billable"))); //is_active
                 voxcontractItemBillingInfoPreparedStatement.setBoolean(9, getBoolean(resultSet.getString("billable"))); //is_billable
                 voxcontractItemBillingInfoPreparedStatement.setBoolean(10, getBoolean(resultSet.getString("proratabilled"))); //is_prorata
                 voxcontractItemBillingInfoPreparedStatement.setDouble(11, 0.0d); //lob_price
 
                 voxcontractItemBillingInfoPreparedStatement.setInt(12, getMasterNumberId(resultSet.getInt("masternumberid"))); //master_number
                 voxcontractItemBillingInfoPreparedStatement.setString(13, resultSet.getString("notes")); //note
                 voxcontractItemBillingInfoPreparedStatement.setInt(14, getPreviousMasterNumber(resultSet.getString("oldmasternumber"))); //previous_master_number
                 voxcontractItemBillingInfoPreparedStatement.setString(15, null); //salesman_id
                 voxcontractItemBillingInfoPreparedStatement.setDouble(16, resultSet.getDouble("billings"));//selling_price
                 voxcontractItemBillingInfoPreparedStatement.setDouble(17, resultSet.getDouble("setupfee"));//setup_fee
                 voxcontractItemBillingInfoPreparedStatement.setDate(18, resultSet.getDate("datesold"));//sold_date
                 voxcontractItemBillingInfoPreparedStatement.setDate(19, resultSet.getDate("salesupgradedate"));//upgrade_date
                 voxcontractItemBillingInfoPreparedStatement.setBoolean(20, getBoolean(resultSet.getString("upgrade")));//upgrade_downgrade
                 voxcontractItemBillingInfoPreparedStatement.setString(21, getVoxcontractDebitOrderDay(resultSet.getString("paymentday")));//voxcontract_debit_order_day ?????
                 voxcontractItemBillingInfoPreparedStatement.setString(22, getAccountManager(resultSet.getInt("salesmanid"))); //account_manager_id
                 voxcontractItemBillingInfoPreparedStatement.setString(23, getCancellationReason(resultSet.getInt("cancellationreasonid"))); //cancellation_reason_id
                 voxcontractItemBillingInfoPreparedStatement.setString(24, getPaymentType(resultSet.getInt("paymentoptionid"))); //payment_type_id
                 voxcontractItemBillingInfoPreparedStatement.setString(25, getProfitCenter(resultSet.getInt("profitcentreid"))); //profit_center_id
                 voxcontractItemBillingInfoPreparedStatement.setString(26, getRegion(resultSet.getInt("regionid"))); //region_id
                 voxcontractItemBillingInfoPreparedStatement.setString(27, voxcontractId); //voxcontract_id
                 voxcontractItemBillingInfoPreparedStatement.setString(28, voxcontractProductId); //voxcontract_product_id
                 voxcontractItemBillingInfoPreparedStatement.addBatch();
 
                 //setting voxcontractHasProduct
                 voxcontractHasProductPreparedStatement.setString(1, voxcontractId); //voxcontract_id
                 voxcontractHasProductPreparedStatement.setString(2, voxcontractProductId); //voxcontract_product_id
                 voxcontractHasProductPreparedStatement.setString(3, voxcontractItemBillingInfoId); //billing_info_id
                 voxcontractHasProductPreparedStatement.addBatch();
             }
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
 
     }
 
     private void executeBatches() {
 
 
 
         System.out.println("Executing Batches : ");
 
 
         System.out.println("close all prepared read statements and free resources");
 
         close(bigBluePreparedStatement);
         close(bigBlueCustomerPreparedStatement);
         close(bigBlueCancelReasonPreparedStatement);
         close(bigBlueSalesStaffPreparedStatement);
         close(bigBlueDataSourceDataPreparedStatement);
         close(bigBluePaymentPreparedStatement);
         close(bigBlueProfiitPreparedStatement);
         close(bigBlueRegionPreparedStatement);
         close(bigBlueProductPreparedStatement);
 
         close(voxzalCustomerPreparedStatement);
         close(voxzalCancelPreparedStatement);
         close(voxzalUsrPreparedStatement);
         close(voxzalDataSourcePreparedStatement);
         close(voxzalSourcePreparedStatement);
         close(voxzalPaymentPreparedStatement);
         close(voxzalProfitPreparedStatement);
         close(voxzalRegionPreparedStatement);
         close(voxzalGlobalCustomerPreparedStatement);
         close(voxzalLobPreparedStatement);
         close(voxzalPlanTypePreparedStatement);
         close(voxzalReadGlobalCustomerPreparedStatement);
 
         try {
             if (bigBlueConnection != null) {
                 bigBlueConnection.close();
             }
         } catch (Exception e) {
 
         }
 
 
 
 
         try {
             int[] res;
 
             long start = System.currentTimeMillis();
 
             res = voxcontractPreparedStatement.executeBatch();
             System.out.println("time to execute voxcontractPreparedStatement batch " + (System.currentTimeMillis() - start) + "ms. Inserts:" + res.length);
 
             start = System.currentTimeMillis();
             res = voxcontractInfoPreparedStatement.executeBatch();
             System.out.println("time to execute voxcontractInfoPreparedStatement batch " + (System.currentTimeMillis() - start) + "ms. Inserts:" + res.length);
 
 
             start = System.currentTimeMillis();
             res = voxcontractItemBillingInfoPreparedStatement.executeBatch();
             System.out.println("time to execute voxcontractItemBillingInfoPreparedStatement batch " + (System.currentTimeMillis() - start) + "ms. Inserts:" + res.length);
 
             start = System.currentTimeMillis();
             res = voxcontractProductPreparedStatement.executeBatch();
             System.out.println("time to execute voxcontractProductPreparedStatement batch " + (System.currentTimeMillis() - start) + "ms. Inserts:" + res.length);
 
             start = System.currentTimeMillis();
             res = voxcontractHasProductPreparedStatement.executeBatch();
             System.out.println("time to execute voxcontractHasProductPreparedStatement batch " + (System.currentTimeMillis() - start) + "ms. Inserts:" + res.length);
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void close(PreparedStatement preparedStatement) {
         try {
             if (preparedStatement != null) {
                 preparedStatement.close();
             }
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void close(ResultSet resultSet) {
         try {
             if (resultSet != null) {
                 resultSet.close();
             }
         } catch (SQLException ex) {
             Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void createVoxcontractPerAccountIdProductId() {
         System.out.println("createVoxcontractPerAccountIdProductId start");
         long start = System.currentTimeMillis();
 
         List<Integer> accountsList = new ArrayList<Integer>();
         Set<Integer> accountsSet = new HashSet<Integer>();
 
         if (bigBlueResultSet != null) {
             try {
                 long startAccountList = System.currentTimeMillis();
                 while (bigBlueResultSet.next()) {
                     Integer acc = bigBlueResultSet.getInt("AccountID");
                     accountsList.add(acc);
                     accountsSet.add(acc);
                 }
                 System.out.println("\nBefore List size : " + accountsList.size() + "\t Set size " + accountsSet.size());
                 System.out.println("time to make account set/list " + (System.currentTimeMillis() - startAccountList) + " ms");
 
                 //bigBlueResultSet.beforeFirst(); //we can just close this now
                 bigBlueResultSet.close();
                 System.out.println("we have closed the bigblue result set");
 
                 int voxcontractCount = 0;
                 Integer aid = null;
                 int count = 0;
                 bigBlueSubQueryPreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ProductId='1888' and AccountID=?");
                 for (Integer account : accountsSet) {
                     voxcontractCount++;
                     voxcontractId = getId();
                     voxcontractInfoId = getId();
 
 
                     bigBlueSubQueryPreparedStatement.setInt(1, account);
                     bigBlueSubQueryResultSet = bigBlueSubQueryPreparedStatement.executeQuery();
                     count = 0;
                     bigBlueSubQueryResultSet.last();
                     aid = bigBlueSubQueryResultSet.getInt("accountid");
                     masterNumberId = bigBlueSubQueryResultSet.getInt("masternumberid");
 
                     String globalCustomerId = getGlobalCustomer(aid);
                     if (globalCustomerId == null) {
                         System.out.println("Skipping an Annuity " + masterNumberId);
                         databaseLogger.logError(masterNumberId, voxcontractId, "account_id", aid, 0, "account_id_strace", "non existent account");
                         continue;
                     }
                     setVoxcontractInfoPreparedStatement(bigBlueSubQueryResultSet, globalCustomerId);
 
                     bigBlueSubQueryResultSet.beforeFirst();
                     while (bigBlueSubQueryResultSet.next()) {
                         count++;
                         setPreparedStatementValues(bigBlueSubQueryResultSet);
                         ignoreAnnuity = false;
                     }
 //                    bigBlueSubQueryResultSet.close();
 //                    bigBlueSubQueryPreparedStatement.close();
 
                     // executeBatches();
 //                    if (voxcontractCount == 500) {
 //                        break;
 //                    }
                 }// end of for loop
                 //bigBlueConnection.close();
 
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
 
 
         }
         System.out.println("After List size : " + accountsList.size() + "\tSet size : " + accountsSet.size());
         System.out.println("Time to createVoxcontractPerAccountIdProductId" + (System.currentTimeMillis() - start) + "ms");
 
 
     }
 
     public void createVoxcontractPerAccountIdServiceRequestNo() {
         List<AccountServiceRequestNoPair> list = new ArrayList<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> accountServiceRequestNoPairs = new HashSet<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> firstBatch = new HashSet<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> secondBatch = new HashSet<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> thirdBatch = new HashSet<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> fourthBatch = new HashSet<AccountServiceRequestNoPair>();
         Set<AccountServiceRequestNoPair> iteratingBatch = new HashSet<AccountServiceRequestNoPair>();
         if (bigBlueResultSet != null) {
             int c = 0;
             try {
                 while (bigBlueResultSet.next()) {
                     String aid = bigBlueResultSet.getString("AccountID");
                     String srn = bigBlueResultSet.getString("ServiceRequestNo");
                     AccountServiceRequestNoPair pair = new AccountServiceRequestNoPair(aid, srn);
                     c++;
                     accountServiceRequestNoPairs.add(pair);
                     if (c <= 8000) {
                         firstBatch.add(pair);
                     } else if (c <= 16000) {
                         secondBatch.add(pair);
                     } else if (c <= 24000) {
                         thirdBatch.add(pair);
                     } else {
                         fourthBatch.add(pair);
                     }
                     list.add(pair);
                 }
                 if (batchNumber.equals("1")) {
                     System.out.println("Iterating with firstHalf");
                     iteratingBatch = firstBatch;
                 } else if (batchNumber.equals("2")) {
                     System.out.println("Iterating with secondHalf");
                     iteratingBatch = secondBatch;
                 } else if (batchNumber.equals("3")) {
                     System.out.println("Iterating with secondHalf");
                     iteratingBatch = thirdBatch;
                 } else {
                     System.out.println("Iterating with third set");
                     iteratingBatch = fourthBatch;
                 }
 
                 System.out.println("\nList size : " + list.size() + "\t Set size " + accountServiceRequestNoPairs.size());
                 //bigBlueResultSet.beforeFirst();
                 bigBlueResultSet.close();
                 int voxcontractCount = 0;
 
                 bigBlueSubQueryPreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ProductId<>'1888' and ServiceRequestNo=? and AccountID=?");
 
                 for (AccountServiceRequestNoPair asrnp : iteratingBatch) {
                     voxcontractCount++;
                     voxcontractId = getId();
                     voxcontractInfoId = getId();
 
                     // take out loop! bigBlueSubQueryPreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ProductId<>'1888' and ServiceRequestNo=? and AccountID=?");
                     bigBlueSubQueryPreparedStatement.setString(1, asrnp.getServiceRequestNo()); //servicerequestno
                     bigBlueSubQueryPreparedStatement.setString(2, asrnp.getAccountId()); // accountid
                     bigBlueSubQueryResultSet = bigBlueSubQueryPreparedStatement.executeQuery();
                     int count = 0;
                     bigBlueSubQueryResultSet.last();
                     String globalCustomerId = getGlobalCustomer(bigBlueSubQueryResultSet.getInt("accountid"));  //lookup once only
                     if (globalCustomerId == null) {
                         System.out.println("Skipping an Annuity " + masterNumberId);
                         masterNumberId = bigBlueSubQueryResultSet.getInt("masternummberid");
                         continue;
                     }
                     setVoxcontractInfoPreparedStatement(bigBlueSubQueryResultSet, globalCustomerId);
                     bigBlueSubQueryResultSet.beforeFirst();
                     while (bigBlueSubQueryResultSet.next()) {
                         count++;
 
                         setPreparedStatementValues(bigBlueSubQueryResultSet);
                         ignoreAnnuity = false;
                     }
                     bigBlueSubQueryResultSet.close();
                 }
                 bigBlueSubQueryPreparedStatement.close();
                bigBlueConnection.close();
                executeBatches();
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         } else {
             System.out.println(":(");
         }
     }
 
     public void createVoxcontractPerAccountIdDateSold() {
         List<AccountDateSoldPair> list = new ArrayList<AccountDateSoldPair>();
         Set<AccountDateSoldPair> accountDateSoldPairs = new HashSet<AccountDateSoldPair>();
         Set<AccountDateSoldPair> firstBatch = new HashSet<AccountDateSoldPair>();
         Set<AccountDateSoldPair> secondBatch = new HashSet<AccountDateSoldPair>();
         Set<AccountDateSoldPair> iteratingBatch = new HashSet<AccountDateSoldPair>();
         if (bigBlueResultSet != null) {
             int c = 0;
             try {
                 while (bigBlueResultSet.next()) {
                     String aid = bigBlueResultSet.getString("AccountID");
                     Date ds = bigBlueResultSet.getDate("DateSold");
                     AccountDateSoldPair pair = new AccountDateSoldPair(aid, ds);
                     c++;
                     accountDateSoldPairs.add(pair);
                     if (c <= 8000) {
                         firstBatch.add(pair);
                     } else {
                         secondBatch.add(pair);
                     }
                     list.add(pair);
                 }
                 if (batchNumber.equals("1")) {
                     System.out.println("Iterating with firstHalf");
                     iteratingBatch = firstBatch;
                 } else {
                     System.out.println("Iterating with secondHalf");
                     iteratingBatch = secondBatch;
                 }
 
                 System.out.println("\n List size : " + list.size() + "\t Set size " + accountDateSoldPairs.size());
                 bigBlueResultSet.beforeFirst();
                 int voxcontractCount = 0;
                 for (AccountDateSoldPair adsp : iteratingBatch) {
                     voxcontractCount++;
                     voxcontractId = getId();
                     voxcontractInfoId = getId();
 
                     bigBlueSubQueryPreparedStatement = bigBlueConnection.prepareStatement("select * from AnnuityBilling where ProductId<>'1888' and DateSold=? and AccountID=?");
                     bigBlueSubQueryPreparedStatement.setDate(1, adsp.getDateSold());  // datesold
                     bigBlueSubQueryPreparedStatement.setString(2, adsp.getAccountId()); // accountid
                     bigBlueSubQueryResultSet = bigBlueSubQueryPreparedStatement.executeQuery();
                     int count = 0;
                     bigBlueSubQueryResultSet.last();
 
                     String globalCustomerId = getGlobalCustomer(bigBlueSubQueryResultSet.getInt("accountid"));
                     if (globalCustomerId == null) {
                         System.out.println("Skipping an Annuity " + masterNumberId);
                         masterNumberId = bigBlueSubQueryResultSet.getInt("masternummberid");
                         continue;
                     }
                     setVoxcontractInfoPreparedStatement(bigBlueSubQueryResultSet, globalCustomerId);
                     bigBlueSubQueryResultSet.beforeFirst();
                     while (bigBlueSubQueryResultSet.next()) {
                         count++;
 
                         setPreparedStatementValues(bigBlueSubQueryResultSet);
                         ignoreAnnuity = false;
                     }
                     bigBlueSubQueryResultSet.close();
                     bigBlueSubQueryPreparedStatement.close();
 //                    if (voxcontractCount == 100) {
 //                        break;
 //                    }
                 }// end of for loop
             } catch (SQLException ex) {
                 Logger.getLogger(AnnuityMigration.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private Integer getMasterNumberId(Integer mn) {
         masterNumberId = mn;
         return masterNumberId;
     }
 }
