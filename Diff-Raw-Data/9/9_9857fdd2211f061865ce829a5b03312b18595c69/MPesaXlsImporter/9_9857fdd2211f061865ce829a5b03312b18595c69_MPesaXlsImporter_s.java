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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
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
 
 public class MPesaXlsImporter extends StandardImport {
     private static final String EXPECTED_STATUS = "Completed";
     static final String PAYMENT_TYPE = "MPESA/ZAP";
     static final int RECEIPT = 0, TRANS_DATE = 1, DETAILS = 2, STATUS = 3, WITHDRAWN = 4, PAID_IN = 5, BALANCE = 6,
             BALANCE_CONFIRMED = 7, TRANSACTION_TYPE = 8, OTHER_PARTY_INFO = 9, TRANSACTION_PARTY_DETAILS = 10,
             MAX_CELL_NUM = 11;
 
     @Override
     public String getDisplayName() {
         return "M-PESA (Excel 2007)";
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         final List<String> errorsList = new ArrayList<String>();
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         int friendlyRowNum = 0;
         boolean skippingRowsBeforeTransactionData = true;
         Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
 
         try {
             {
                 final PaymentTypeDto paymentType = findPaymentType(PAYMENT_TYPE);
                 if (null != paymentType) {
                     setPaymentTypeDto(paymentType);
                 } else {
                    throw new RuntimeException("Payment type " + PAYMENT_TYPE + " not found. Have you configured" +
                    		" this payment type?");
                 }
             }
 
             final HSSFWorkbook workbook = new HSSFWorkbook(input);
             final HSSFSheet sheet = workbook.getSheetAt(0);
 
             Row row = null;
 
             final Iterator<Row> rowIterator = sheet.iterator();
 
             /* Ignore everything prior to transaction data */
             while (errorsList.isEmpty() && skippingRowsBeforeTransactionData) {
                 if (!rowIterator.hasNext()) {
                     errorsList.add("No rows found with import data.");
                     break;
                 }
                 row = rowIterator.next();
 
                 if (row.getCell(0).getStringCellValue().trim().equals("Transactions")) {
                     skippingRowsBeforeTransactionData = false;
                     /* skip row with column descriptions */
                     rowIterator.next();
                 }
             }
 
             /* Parse transaction data */
             if (errorsList.isEmpty()) {
                 row = null;
 
                 while (true) {
                     if (rowIterator.hasNext()) {
                         row = rowIterator.next();
                     } else {
                         break;
                     }
                     friendlyRowNum = (row.getRowNum() + 1);
 
                     Cell firstCell = row.getCell(0);
                     if (null == firstCell || StringUtils.isBlank(firstCell.toString())) {
                         /*
                          * Justification: this is similar to skipping blank input lines in a text file.
                          */
                         continue;
                     }
 
                     if (row.getLastCellNum() < MAX_CELL_NUM) {
                         errorsList.add("Row " + friendlyRowNum + " is missing data: not enough fields.");
                         continue;
                     }
 
                     final Cell statusCell = row.getCell(STATUS);
                     String status = null;
                     if (null != statusCell) {
                         status = statusCell.getStringCellValue().trim();
 
                         if (!status.equals(EXPECTED_STATUS)) {
                            errorsList.add("Row " + friendlyRowNum + " has a status other than \"" + EXPECTED_STATUS
                                    + "\".");
                         }
                     }
 
                     final Cell detailsCell = row.getCell(TRANSACTION_PARTY_DETAILS);
                     String governmentId = "", loanProductShortName = "";
                     if (null != detailsCell) {
                         String[] result = parseClientIdentifiers(detailsCell.getStringCellValue());
                         governmentId = result[0];
                         loanProductShortName = result[1];
                     }
 
                     if (StringUtils.isBlank(governmentId)) {
                         errorsList.add("Government ID could not be extracted from row " + friendlyRowNum);
                         continue;
                     }
 
                     if (StringUtils.isBlank(loanProductShortName)) {
                         errorsList.add("Product short name could not be extracted from row " + friendlyRowNum);
                         continue;
                     }
 
                     final Cell amountCell = row.getCell(PAID_IN);
                     BigDecimal paymentAmount = null;
                     if (null == amountCell) {
                         errorsList.add("Invalid amount in row " + friendlyRowNum);
                         continue;
                     } else {
                         // FIXME: possible data loss converting double to BigDecimal?
                         paymentAmount = new BigDecimal(amountCell.getNumericCellValue());
                     }
                     final AccountReferenceDto account;
 
                     try {
                         account = getAccountService()
                                 .lookupLoanAccountReferenceFromClientGovernmentIdAndLoanProductShortName(governmentId,
                                         loanProductShortName);
                     } catch (Exception e) {
                         errorsList
                                 .add("Error looking up account ID from row " + friendlyRowNum + ": " + e.getMessage());
                         continue;
                     }
 
                     final Cell transDateCell = row.getCell(TRANS_DATE);
                     if (null == transDateCell) {
                         errorsList.add("No valid transaction date in row " + friendlyRowNum);
                         continue;
                     }
                     final Date transDate = getDate(transDateCell);
                     if (null == transDateCell) {
                         errorsList.add("Could not parse transaction date from row " + friendlyRowNum
                                 + ". Date column contained [" + transDateCell + "]");
                         continue;
                     }
                     final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                     final BigDecimal totalPaymentAmountForAccount = addToRunningTotalForAccount(paymentAmount,
                             cumulativeAmountByAccount, account);
 
                     final String comment = "";
                     AccountPaymentParametersDto cumulativePayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), account, totalPaymentAmountForAccount, paymentDate,
                             getPaymentTypeDto(), comment);
 
                     AccountPaymentParametersDto payment = new AccountPaymentParametersDto(getUserReferenceDto(),
                             account, paymentAmount, paymentDate, getPaymentTypeDto(), comment);
 
                     List<InvalidPaymentReason> errors = getAccountService().validatePayment(cumulativePayment);
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
 
                         continue;
                     }
 
                     pmts.add(payment);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace(System.err);
             errorsList.add(e + ". Input line number: " + friendlyRowNum);
         }
 
         return new ParseResultDto(errorsList, pmts);
     }
 
     static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
 
     Date getDate(Cell transDateCell) throws ParseException {
         Date date = null;
         switch (transDateCell.getCellType()) {
         case Cell.CELL_TYPE_STRING:
             final SimpleDateFormat dateAsText = new SimpleDateFormat(dateFormat);
             dateAsText.setLenient(false);
             date = dateAsText.parse(transDateCell.getStringCellValue());
             break;
         case Cell.CELL_TYPE_NUMERIC:
             date = transDateCell.getDateCellValue();
             break;
         }
         return date;
     }
 
     String[] parseClientIdentifiers(String stringCellValue) {
         return stringCellValue.split(" ");
     }
 }
