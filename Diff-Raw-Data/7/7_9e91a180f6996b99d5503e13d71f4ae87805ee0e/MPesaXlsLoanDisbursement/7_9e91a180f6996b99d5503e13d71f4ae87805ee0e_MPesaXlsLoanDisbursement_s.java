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
 
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.joda.time.LocalDate;
 import org.mifos.StandardImport;
 import org.mifos.accounts.api.AccountPaymentParametersDto;
 import org.mifos.accounts.api.AccountReferenceDto;
 import org.mifos.accounts.api.InvalidPaymentReason;
 import org.mifos.accounts.api.PaymentTypeDto;
 import org.mifos.spi.ParseResultDto;
 
 public class MPesaXlsLoanDisbursement extends StandardImport {
     private static final String EXPECTED_STATUS = "Completed";
     protected static final String DISBURSE_TYPE = "MPESA/ZAP";
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
 
     @Override
     public String getDisplayName() {
         return "M-PESA Disburse Loan Excel 97(-2007)";
     }
 
     @Override
     public void store(InputStream input) throws Exception {
         getAccountService().disburseLoans(parse(input).getSuccessfullyParsedRows());
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         final List<String> errorsList = new LinkedList<String>();
 
         try {
             final Iterator<Row> rowIterator = new HSSFWorkbook(input).getSheetAt(0).iterator();
             int friendlyRowNum = 0;
             Row row;
 
             setPaymentType();
 
             skipToTransactionData(rowIterator, errorsList);
 
             if (!errorsList.isEmpty()) {
                 return new ParseResultDto(errorsList, pmts);
             }
 
             /* Parse loan disbursals */
 
             while (rowIterator.hasNext()) {
                 try {
                     row = rowIterator.next();
 
                     friendlyRowNum = row.getRowNum() + 1;
 
                     if (!isRowValid(row, friendlyRowNum, errorsList)) {
                         continue;
                     }
 
                     Date transDate;
                     try {
                         transDate = getDate(row.getCell(TRANSACTION_DATE));
                     } catch (Exception e) {
                         errorsList.add("Date in Row " + friendlyRowNum
                                 + "  does not begin with expected format (YYYY-MM-DD), it contains "
                                 + row.getCell(TRANSACTION_DATE).getStringCellValue());
                         continue;
                     }
 
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
 
                     final String accountId = row.getCell(TRANSACTION_PARTY_DETAILS).getStringCellValue();
 
                     // FIXME: possible data loss converting double to BigDecimal?
                     final BigDecimal withdrawnAmount = BigDecimal.valueOf(row.getCell(WITHDRAWN).getNumericCellValue());
 
                     if (StringUtils.isBlank(accountId)) {
                         errorsList.add("Loan account ID could not be extracted from row " + friendlyRowNum);
                     }
 
                     final AccountReferenceDto account = getAccountService()
                             .lookupLoanAccountReferenceFromGlobalAccountNumber(accountId);
 
                     final AccountPaymentParametersDto loanAccDisbursementPayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), account, withdrawnAmount, paymentDate, getPaymentTypeDto(), "");
 
                     if (!isPaymentValid(loanAccDisbursementPayment, friendlyRowNum, errorsList)) {
                         continue;
                     }
 
                     pmts.add(loanAccDisbursementPayment);
 
                 } catch (Exception e) {
                     /* catch row specific exception and continue for other rows */
                     errorsList.add(e.getMessage() + ". Input line number: " + friendlyRowNum);
                     continue;
                 }
             }
         } catch (Exception e) {
             /* Catch any exception in the process */
             e.printStackTrace(System.err);
             errorsList.add(e.getMessage() + ". Got error before reading rows");
 
         }
         return new ParseResultDto(errorsList, pmts);
     }
 
     private void setPaymentType() throws Exception {
         final PaymentTypeDto paymentType = findDisbursementType(DISBURSE_TYPE);
 
         if (paymentType == null) {
             throw new MPesaXlsLoanDisbursementException("Disbursement type " + DISBURSE_TYPE
                     + " not found. Have you configured" + " this disbursement type?");
         }
         setPaymentTypeDto(paymentType);
     }
 
     private boolean isRowValid(final Row row, final int friendlyRowNum, List<String> errorsList) {
         String missingDataMsg = "Row " + friendlyRowNum + " is missing data: ";
         if (row.getLastCellNum() < MAX_CELL_NUM) {
             errorsList.add(missingDataMsg + "not enough fields.");
             return false;
         }
         if (null == row.getCell(TRANSACTION_DATE)) {
             errorsList.add(missingDataMsg + "Date field is empty.");
             return false;
         }
         if (null == row.getCell(TRANSACTION_PARTY_DETAILS)) {
             errorsList.add(missingDataMsg + "\"Transaction party details\" field is empty.");
             return false;
         }
         if (null == row.getCell(WITHDRAWN)) {
             errorsList.add(missingDataMsg + "\"Withdrawn\" field is empty.");
             return false;
         }
         if (row.getCell(STATUS) == null) {
             errorsList.add(missingDataMsg + "Status field is empty");
             return false;
         } else {
             if (!row.getCell(STATUS).getStringCellValue().trim().equals(EXPECTED_STATUS)) {
                 errorsList.add("Status in row " + friendlyRowNum + " is " + row.getCell(STATUS) + " instead of "
                         + EXPECTED_STATUS);
                 return false;
             }
         }
         return true;
     }
 
     private boolean isPaymentValid(final AccountPaymentParametersDto cumulativePayment, final int friendlyRowNum,
             List<String> errorsList) throws Exception {
         final List<InvalidPaymentReason> errors = getAccountService().validateLoanDisbursement(cumulativePayment);
 
         if (!errors.isEmpty()) {
             for (InvalidPaymentReason error : errors) {
                 switch (error) {
                 case INVALID_DATE:
                     errorsList.add("Invalid transaction date in row " + friendlyRowNum);
                     break;
                 case UNSUPPORTED_PAYMENT_TYPE:
                     errorsList.add("Unsupported payment type in row " + friendlyRowNum);
                     break;
                 case INVALID_PAYMENT_AMOUNT:
                     errorsList.add("Invalid payment amount in row " + friendlyRowNum);
                     break;
                 default:
                     errorsList.add("Invalid payment in row " + friendlyRowNum + " (reason unknown).");
                     break;
                 }
             }
             return false;
         }
         return true;
     }
 
     private void skipToTransactionData(final Iterator<Row> rowIterator, List<String> errorsList) {
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
 
     @Override
     public int getNumberOfTransactionsPerRow() {
         return 1;
     }
 
     class MPesaXlsLoanDisbursementException extends RuntimeException {
 
         private static final long serialVersionUID = 731436914098659043L;
 
         MPesaXlsLoanDisbursementException(final String msg) {
             super(msg);
         }
 
     }
 }
