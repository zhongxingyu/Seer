 /*
  * Copyright (c) 2005-2010 Grameen Foundation USA
  * All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package ke.co.safaricom;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import org.apache.commons.io.IOUtils;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.mifos.StandardImport;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.CustomerDto;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.spi.ParseResultDto;
 
 /**
  * This class implements mpesa plugin which export transactions from an XLS sheet to Mifos database.
  * It uses the standard mifos API/SPI. <br>
  * <a href='http://www.mifos.org/developers/wiki/PluginManagement'>http://www.mifos.org/developers/wiki/PluginManagement</a>
  * 
  * 
  * 
  */
 public class MPesaXlsImporter extends StandardImport {
     private static final String IMPORT_TRANSACTION_ORDER = "ImportTransactionOrder";
     private static final String EXPECTED_STATUS = "Completed";
     protected static final String PAYMENT_TYPE = "MPESA/ZAP";
     protected static final int RECEIPT = 0;
     protected static final int TRANSACTION_DATE = 1;
     protected static final int DETAILS = 2;
     protected static final int STATUS = 3;
     protected static final int WITHDRAWN = 4;
     protected static final int PAID_IN = 5;
     protected static final int BALANCE = 6;
     protected static final int BALANCE_CONFIRMED = 7;
     protected static final int TRANSACTION_TYPE = 8;
     protected static final int OTHER_PARTY_INFO = 9;
     protected static final int TRANSACTION_PARTY_DETAILS = 10;
     protected static final int MAX_CELL_NUM = 11;
 
     private static Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount;
     private static List<AccountPaymentParametersDto> pmts;
     private static List<String> errorsList;
     private static List<String> importTransactionOrder;
     private static int successfullyParsedRows;
     @Override
     public String getDisplayName() {
         return "M-PESA Excel 97(-2007)";
     }
 
     @SuppressWarnings("unchecked")
     protected List<String> getImportTransactionOrder() {
         if (importTransactionOrder == null) {
             final String importTransactionOrderKey = MPesaXlsImporter.class.getCanonicalName() + "."
                     + IMPORT_TRANSACTION_ORDER;
             importTransactionOrder = (List<String>) getAccountService()
                     .getMifosConfiguration(importTransactionOrderKey);
             if (importTransactionOrder == null) {
                 importTransactionOrder = new ArrayList<String>();
             }
         }
         return importTransactionOrder;
     }
     
     private String cellStringValue(Cell cell) {
     	if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
     		return Double.toString(cell.getNumericCellValue());
     	} else {
     		return cell.getStringCellValue();
     	}
     }
     
     private String formatErrorMessage(Row row, String message) {
     	if (row == null) {
     		return String.format("Error - %s", message);
     	}
     	if (row.getCell(RECEIPT) == null) {
     		return String.format("Row <%d> error - %s",
         			row.getRowNum() + 1,
         			message);
     	}
     	return String.format("Row <%d> error - %s - %s",
     			row.getRowNum() + 1,
     			cellStringValue(row.getCell(RECEIPT)),
     			message);
     }
     
     private String formatIgnoredErrorMessage(Row row, String message) {
     	return String.format("Row <%d> ignored - %s - %s",
     			row.getRowNum() + 1,
     			cellStringValue(row.getCell(RECEIPT)),
     			message);
     }
 
     private ByteArrayInputStream copyInputIntoByteInput(InputStream input) throws IOException {
         return new ByteArrayInputStream(IOUtils.toByteArray(input));
     }
 
     private String getPhoneNumberCandidate(Row row) {
 	String cellContents = cellStringValue(row.getCell(OTHER_PARTY_INFO));
 	String[] splitted = cellContents.split(" ");
 	if (splitted == null || splitted.length == 0)
 	    return null;
 	return splitted[0];
     }
 
     private void validatePhoneNumber(Row row) {
 	String phoneNumber = getPhoneNumberCandidate(row);
 	if (phoneNumber == null)
 	    return;
 	List<CustomerDto> customers = getCustomerSearchService().findCustomersWithGivenPhoneNumber(phoneNumber);
 	if (customers == null || customers.isEmpty()) {
 	    errorsList.add(formatErrorMessage(row,
 				String.format("Client with mobile number %s was not found", phoneNumber)));
 	} else if (customers.size() >= 2) {
 	   errorsList.add(formatErrorMessage(row,
 				String.format("More than 1 client with mobile number %s was found", phoneNumber)));
 	}
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
         pmts = new ArrayList<AccountPaymentParametersDto>();
         errorsList = new LinkedList<String>();
         successfullyParsedRows = 0;
 
         try {
             Iterator<Row> rowIterator = null;
             // Copy input into byte input to try two implementations of POI parsers: HSSF and XSSF (XML formats)
             ByteArrayInputStream copiedInput = copyInputIntoByteInput(input);
             copiedInput.mark(0);
             try {
                 rowIterator = new HSSFWorkbook(copiedInput).getSheetAt(0).iterator();
             } catch (Exception e) {
                 copiedInput.reset();
                 try {
                     rowIterator = new XSSFWorkbook(copiedInput).getSheetAt(0).iterator();
                 } catch (Exception e2) {
                     throw new MPesaXlsImporterException("Unknown file format. Supported file formats are: XLS (from Excel 2003 or older), XLSX");
                 }
             }
             int friendlyRowNum = 0;
             Row row = null;
 
             setPaymentType();
 
             skipToTransactionData(rowIterator);
 
             if (!errorsList.isEmpty()) {
                 return new ParseResultDto(errorsList, pmts);
             }
 
             /* Parse transaction data */
 
             while (rowIterator.hasNext()) {
                 try {
                     row = rowIterator.next();
 
                     friendlyRowNum = row.getRowNum() + 1;
 
                     if (!isRowValid(row, friendlyRowNum, errorsList)) {
                         continue;
                     }
                     
                     String receipt = cellStringValue(row.getCell(RECEIPT));
 
                     Date transDate;
                     try {
                         transDate = getDate(row.getCell(TRANSACTION_DATE));
                     } catch (Exception e) {
                         errorsList.add(formatErrorMessage(row, "Date does not begin with expected format (YYYY-MM-DD)"));
                         continue;
                     }
 
 		    validatePhoneNumber(row);
 
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     String transactionPartyDetails = null;
                     
                     if(row.getCell(TRANSACTION_PARTY_DETAILS).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                         transactionPartyDetails = row.getCell(TRANSACTION_PARTY_DETAILS).getNumericCellValue() +"";
                         if(transactionPartyDetails.endsWith(".0")){
                             transactionPartyDetails = transactionPartyDetails.replace(".0", "");
                         } else {
                             throw new IllegalArgumentException("Unknown format of cell "+ TRANSACTION_PARTY_DETAILS);
                         }
                     } else if (row.getCell(TRANSACTION_PARTY_DETAILS).getCellType() == Cell.CELL_TYPE_STRING) {
                     transactionPartyDetails = row.getCell(TRANSACTION_PARTY_DETAILS).getStringCellValue();
                     }
                     List<String> parameters = checkAndGetValues(transactionPartyDetails);
 
                     String governmentId = parameters.get(0);
                     List<String> loanPrds = new LinkedList<String>();
                     String lastInTheOrderProdSName = parameters.get(parameters.size() - 1);
                     loanPrds.addAll(parameters.subList(1, parameters.size() - 1));
 
                     checkBlank(governmentId, "Government ID", row);
                     checkBlank(lastInTheOrderProdSName, "Savings product short name", row);
 
                     BigDecimal paidInAmount = BigDecimal.ZERO;
 
                     // FIXME: possible data loss converting double to BigDecimal?
                     paidInAmount = BigDecimal.valueOf(row.getCell(PAID_IN).getNumericCellValue());
                     boolean cancelTransactionFlag = false;
 
                     List<AccountPaymentParametersDto> loanPaymentList = new ArrayList<AccountPaymentParametersDto>();
 
                     for (String loanPrd : loanPrds) {
                         BigDecimal loanAccountPaymentAmount = BigDecimal.ZERO;
                         BigDecimal loanAccountTotalDueAmount = BigDecimal.ZERO;
 
                         final AccountReferenceDto loanAccountReference = getLoanAccount(governmentId, loanPrd);
                         
                      // skip not found accounts as per specs P1 4.9 M-Pesa plugin
                         if(loanAccountReference == null){
                             continue;
                         }
                         
                         loanAccountTotalDueAmount = getTotalPaymentDueAmount(loanAccountReference);
 
                         if (paidInAmount.compareTo(BigDecimal.ZERO) > 0) {
                             if (paidInAmount.compareTo(loanAccountTotalDueAmount) > 0) {
                                 loanAccountPaymentAmount = loanAccountTotalDueAmount;
                                 paidInAmount = paidInAmount.subtract(loanAccountTotalDueAmount);
                             } else {
                                 loanAccountPaymentAmount = paidInAmount;
                                 paidInAmount = BigDecimal.ZERO;
                             }
                         } else {
                             loanAccountPaymentAmount = BigDecimal.ZERO;
                         }
 
                         AccountPaymentParametersDto cumulativeLoanPayment = createPaymentParametersDto(
                                 loanAccountReference, loanAccountPaymentAmount, paymentDate);
 
                         if (!isPaymentValid(cumulativeLoanPayment, row)) {
                             cancelTransactionFlag = true;
                             break;
                         }
                         loanPaymentList.add(new AccountPaymentParametersDto(getUserReferenceDto(),
                                 loanAccountReference, loanAccountPaymentAmount, paymentDate, getPaymentTypeDto(), "", new LocalDate(), receipt));
 
                     }
 
                     if (cancelTransactionFlag) {
                         continue;
                     }
 
                     BigDecimal lastInOrderAmount;
                     AccountReferenceDto lastInOrderAcc;
                     lastInOrderAcc = getSavingsAccount(governmentId, lastInTheOrderProdSName);
                     
                     if(lastInOrderAcc == null) {
                     	lastInOrderAcc = getLoanAccount(governmentId, lastInTheOrderProdSName);
                     	if (lastInOrderAcc != null) {
                     		BigDecimal totalPaymentDueAmount = getTotalPaymentDueAmount(lastInOrderAcc);
                     		if(paidInAmount.compareTo(totalPaymentDueAmount) != 0) {
                     			errorsList.add("Last account is a laon account but the total payment amount is less than amount paid in. Input line number: "+ friendlyRowNum);
                     			continue;  
                     		}
                     	}
                     }
                     
                     if(lastInOrderAcc == null) {
                         errorsList.add(formatErrorMessage(row, "No account found"));
                         continue;
                     }
 
                     if (paidInAmount.compareTo(BigDecimal.ZERO) > 0) {
                         lastInOrderAmount = paidInAmount;
                         paidInAmount = BigDecimal.ZERO;
                     } else {
                         lastInOrderAmount = BigDecimal.ZERO;
                     }
                     
                     final AccountPaymentParametersDto cumulativePaymentlastAcc = createPaymentParametersDto(lastInOrderAcc,
                             lastInOrderAmount, paymentDate);
                     final AccountPaymentParametersDto lastInTheOrderAccPayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), lastInOrderAcc, lastInOrderAmount, paymentDate, getPaymentTypeDto(), "", new LocalDate(), receipt);
 
                     if (!isPaymentValid(cumulativePaymentlastAcc, row)) {
                         continue;
                     }
                     successfullyParsedRows+=1;
                     for (AccountPaymentParametersDto loanPayment : loanPaymentList) {
                         pmts.add(loanPayment);
                     }
                     pmts.add(lastInTheOrderAccPayment);
                 } catch (Exception e) {
                     /* catch row specific exception and continue for other rows */
                 	e.printStackTrace();
                     errorsList.add(formatErrorMessage(row, e.getMessage()));
                     continue;
                 }
             }
         } catch (Exception e) {
             /* Catch any exception in the process */
             e.printStackTrace();
             errorsList.add(e.getMessage() + ". Got error before reading rows");
 
         }
         return new ParseResultDto(errorsList, pmts);
     }
 
     /**
      * Get the parameter list from transaction party details field or ImportTransactionOrder property. This method is
      * executed after validation of input.
      * 
      * @param transactionPartyDetails
      * @return list of parameters
      */
     protected List<String> checkAndGetValues(String transactionPartyDetails) {
         List<String> parameters = new LinkedList<String>();
         String[] result = transactionPartyDetails.split(" ");
         parameters.addAll(Arrays.asList(result));
         if (result.length == 1) {
             List<String> importTransactionOrder = getImportTransactionOrder();
             if (importTransactionOrder == null || importTransactionOrder.isEmpty()) {
                 throw new MPesaXlsImporterException("No Product name in \"Transaction Party Details\" field and "
                         + IMPORT_TRANSACTION_ORDER + " property is not set");
 
             }
             parameters.addAll(importTransactionOrder);
         }
         return parameters;
     }
 
     private AccountPaymentParametersDto createPaymentParametersDto(final AccountReferenceDto accountReference,
             final BigDecimal paymentAmount, final LocalDate paymentDate) {
         BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount, cumulativeAmountByAccount,
                 accountReference);
         return new AccountPaymentParametersDto(getUserReferenceDto(), accountReference, totalPaymentAmountForAccount,
                 paymentDate, getPaymentTypeDto(), "");
     }
 
     /**
      * @throws Exception
      */
     private void setPaymentType() throws Exception {
         final PaymentTypeDto paymentType = findPaymentType(PAYMENT_TYPE);
 
         if (paymentType == null) {
             throw new MPesaXlsImporterException("Payment type " + PAYMENT_TYPE + " not found. Have you configured"
                     + " this payment type?");
         }
         setPaymentTypeDto(paymentType);
     }
 
     private boolean isRowValid(final Row row, final int friendlyRowNum, List<String> errorsList) throws Exception {
     	if (row.getLastCellNum() < MAX_CELL_NUM) {
             errorsList.add(formatErrorMessage(row, "Missing required data"));
             return false;
         }
     	if (!row.getCell(STATUS).getStringCellValue().trim().equals(EXPECTED_STATUS)) {
     		errorsList.add(formatIgnoredErrorMessage(row, "Status of " + row.getCell(STATUS) + " instead of Completed"));
     		return false;
     	}
     	
         if (null == row.getCell(TRANSACTION_DATE)) {
             errorsList.add(formatErrorMessage(row, "Date field is empty"));
             return false;
         }
         if (null == row.getCell(TRANSACTION_PARTY_DETAILS)) {
             errorsList.add(formatErrorMessage(row, "\"Transaction party details\" field is empty."));
             return false;
         }
         if (null == row.getCell(PAID_IN)) {
             errorsList.add(formatErrorMessage(row, "\"Paid in\" field is empty."));
             return false;
         }
         if (row.getCell(STATUS) == null) {
             errorsList.add(formatErrorMessage(row, "Status field is empty"));
             return false;
         }
         else {
             String receiptNumber = cellStringValue(row.getCell(RECEIPT));
 			if (receiptNumber != null && !receiptNumber.isEmpty()) {
             	if (getAccountService().receiptExists(receiptNumber)) {
             		errorsList.add(formatErrorMessage(row, "Transactions with same Receipt ID have already been imported"));
             		return false;
             	}
             }
         }
         return true;
     }
 
     private void checkBlank(final String value, final String name, final Row row) {
         if (StringUtils.isBlank(value)) {
             errorsList.add(formatErrorMessage(row, name + " could not be extracted"));
         }
     }
 
     private boolean isPaymentValid(final AccountPaymentParametersDto cumulativePayment, final Row row)
             throws Exception {
         final List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
 
         if (!errors.isEmpty()) {
             for (InvalidPaymentReason error : errors) {
                 switch (error) {
                 case INVALID_DATE:
                     errorsList.add(formatErrorMessage(row, "Invalid transaction date"));
                     break;
                 case UNSUPPORTED_PAYMENT_TYPE:
                     errorsList.add(formatErrorMessage(row, "Unsupported payment type"));
                     break;
                 case INVALID_PAYMENT_AMOUNT:
                     errorsList.add(formatErrorMessage(row, "Invalid payment amount"));
                     break;
                 case INVALID_LOAN_STATE:
                     errorsList.add(formatErrorMessage(row, "Invalid account state"));
                     break;
                 default:
                     errorsList.add(formatErrorMessage(row, "Invalid payment (reason unknown)"));
                     break;
                 }
             }
             return false;
         }
         return true;
     }
 
     private BigDecimal getTotalPaymentDueAmount(final AccountReferenceDto advanceLoanAccount) throws Exception {
         return getAccountService().getTotalPaymentDueAmount(advanceLoanAccount);
 
     }
 
     protected AccountReferenceDto getSavingsAccount(final String governmentId, final String savingsProductShortName) throws Exception {
         AccountReferenceDto account = null;
         try {
             account = getAccountService().lookupSavingsAccountReferenceFromClientGovernmentIdAndSavingsProductShortName(governmentId, savingsProductShortName);
         } catch (Exception e) {
             if (!e.getMessage().equals("savings not found for client government id " + governmentId + " and savings product short name " + savingsProductShortName)) {
                 throw e;
             }
         }
         return account;
     }
 
     protected AccountReferenceDto getLoanAccount(final String governmentId, final String loanProductShortName) throws Exception {
         AccountReferenceDto account = null;
         try {
             account = getAccountService().lookupLoanAccountReferenceFromClientGovernmentIdAndLoanProductShortName(governmentId, loanProductShortName);
         } catch (Exception e) {
             if(!e.getMessage().equals("loan not found for client government id " + governmentId  + " and loan product short name " + loanProductShortName)) {
                 throw e;
             }
         }
         return account;
     }
 
     private void skipToTransactionData(final Iterator<Row> rowIterator) {
         boolean skippingRowsBeforeTransactionData = true;
         while (errorsList.isEmpty() && skippingRowsBeforeTransactionData) {
             if (!rowIterator.hasNext()) {
                 errorsList.add("No rows found with import data.");
                 break;
             }
             final Row row = rowIterator.next();
             if (row.getCell(0).getStringCellValue().trim().equals("Transactions")) {
                 skippingRowsBeforeTransactionData = false;
                 /* skip row with column descriptions */
                 rowIterator.next();
             }
         }
     }
 
     protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 
     protected Date getDate(final Cell transDateCell) throws ParseException {
         Date date = null;
         if (transDateCell.getCellType() == Cell.CELL_TYPE_STRING) {
             final SimpleDateFormat dateAsText = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
             dateAsText.setLenient(false);
             date = dateAsText.parse(transDateCell.getStringCellValue());
         } else if (transDateCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
             date = transDateCell.getDateCellValue();
         }
         return date;
     }
 
     /**
      * M-PESA imports have multiple transactions per row. Two loan accounts and
      * one savings account, I think.
      * 
      * See <a href="http://mifosforge.jira.com/browse/MIFOS-2909">MIFOS-2909</a>.
      */
     @Override
     public int getSuccessfullyParsedRows() {
         return successfullyParsedRows;
     }
 
     class MPesaXlsImporterException extends RuntimeException {
 
         private static final long serialVersionUID = 731436914098659043L;
 
         MPesaXlsImporterException(final String msg) {
             super(msg);
         }
 
     }
 }
