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
 
     final List<String> errorsList = new ArrayList<String>();
     
 
     @Override
     public String getDisplayName() {
         return "M-PESA (Excel 2007)";
     }
 
     @Override
     public ParseResultDto parse(final InputStream input) {
         final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
         int friendlyRowNum = 0;
         Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
 
         try {
 
             final PaymentTypeDto paymentType = findPaymentType(PAYMENT_TYPE);
             if (null != paymentType) {
                 setPaymentTypeDto(paymentType);
             } else {
                 throw new RuntimeException("Payment type " + PAYMENT_TYPE + " not found. Have you configured"
                         + " this payment type?");
             }
 
             final HSSFWorkbook workbook = new HSSFWorkbook(input);
             final HSSFSheet sheet = workbook.getSheetAt(0);
 
             Row row = null;
 
             final Iterator<Row> rowIterator = sheet.iterator();
 
             skipReadingToTransactionData(rowIterator, row);
 
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
                             errorsList.add("Status in row " + friendlyRowNum + " is " + status + " instead of "
                                     + EXPECTED_STATUS);
                         }
                    } else {
                        errorsList.add("No status in row " + friendlyRowNum);
                     }
 
                     final Cell detailsCell = row.getCell(TRANSACTION_PARTY_DETAILS);
                     String governmentId = "";
                     String advanceLoanProductShortName = "";
                     String normalLoanProductShortName = "";
                     String savingsProductShortName = "";
                     if (null != detailsCell) {
                         String[] result = parseClientIdentifiers(detailsCell.getStringCellValue());
                         governmentId = result[0];
                         advanceLoanProductShortName = result[1];
                         normalLoanProductShortName = result[2];
                         savingsProductShortName = result[3];
                     }
 
                     checkBlank(governmentId, "Government ID", friendlyRowNum);
                     checkBlank(advanceLoanProductShortName, "Advance loan product short name", friendlyRowNum);
                     checkBlank(normalLoanProductShortName, "Normal loan product short", friendlyRowNum);
                     checkBlank(savingsProductShortName, "Savings product short name", friendlyRowNum);
 
                     final Cell amountCell = row.getCell(PAID_IN);
                     BigDecimal paymentAmount = BigDecimal.ZERO;
                     if (null == amountCell) {
                         errorsList.add("Invalid amount in row " + friendlyRowNum);
                         continue;
                     } else {
                         // FIXME: possible data loss converting double to BigDecimal?
                         paymentAmount = new BigDecimal(amountCell.getNumericCellValue()).setScale(0);
                     }
 
                     final AccountReferenceDto advanceLoanAccount;
                     final AccountReferenceDto savingsAccount;
                     final AccountReferenceDto normalLoanAccount;
 
                     final BigDecimal advanceLoanAccountPaymentAmount;
                     final BigDecimal normalLoanAccountPaymentAmount;
                     final BigDecimal savingsAccountPaymentAmount;
 
                     final BigDecimal advanceLoanAccountDue;
                     final BigDecimal normalLoanAccountDue;
 
                     advanceLoanAccount = getLoanAccount(governmentId, advanceLoanProductShortName, friendlyRowNum);
                     normalLoanAccount = getLoanAccount(governmentId, normalLoanProductShortName, friendlyRowNum);
                     savingsAccount = getSavingsAccount(governmentId, savingsProductShortName, friendlyRowNum);
 
                     advanceLoanAccountDue = getTotalPaymentDueAmount(advanceLoanAccount, friendlyRowNum);
                     normalLoanAccountDue = getTotalPaymentDueAmount(normalLoanAccount, friendlyRowNum);
 
                     if (paymentAmount.compareTo(advanceLoanAccountDue) > 0) {
                         advanceLoanAccountPaymentAmount = advanceLoanAccountDue;
                         paymentAmount = paymentAmount.subtract(advanceLoanAccountDue);
                     } else {
                         advanceLoanAccountPaymentAmount = paymentAmount;
                         paymentAmount = BigDecimal.ZERO;
                     }
 
                     if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                         if (paymentAmount.compareTo(normalLoanAccountDue) > 0) {
                             normalLoanAccountPaymentAmount = normalLoanAccountDue;
                             paymentAmount = paymentAmount.subtract(normalLoanAccountDue);
                         } else {
                             normalLoanAccountPaymentAmount = paymentAmount;
                             paymentAmount = BigDecimal.ZERO;
                         }
                     } else {
                         normalLoanAccountPaymentAmount = BigDecimal.ZERO;
                     }
 
                     if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                         savingsAccountPaymentAmount = paymentAmount;
                         paymentAmount = BigDecimal.ZERO;
                     } else {
                         savingsAccountPaymentAmount = BigDecimal.ZERO;
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
 
                     final BigDecimal totalPaymentAmountForAdvanceLoanAccount = addToRunningTotalForAccount(
                             advanceLoanAccountPaymentAmount, cumulativeAmountByAccount, advanceLoanAccount);
                     final BigDecimal totalPaymentAmountForNormalLoanAccount = addToRunningTotalForAccount(
                             normalLoanAccountPaymentAmount, cumulativeAmountByAccount, normalLoanAccount);
                     final BigDecimal totalPaymentAmountForSavingsAccount = addToRunningTotalForAccount(
                             savingsAccountPaymentAmount, cumulativeAmountByAccount, savingsAccount);
 
                     final String comment = "";
                     AccountPaymentParametersDto cumulativePaymentAdvanceLoan = new AccountPaymentParametersDto(
                             getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForAdvanceLoanAccount,
                             paymentDate, getPaymentTypeDto(), comment);
                     AccountPaymentParametersDto cumulativePaymentNormalLoan = new AccountPaymentParametersDto(
                             getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForNormalLoanAccount,
                             paymentDate, getPaymentTypeDto(), comment);
                     AccountPaymentParametersDto cumulativePaymentSavings = new AccountPaymentParametersDto(
                             getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForSavingsAccount,
                             paymentDate, getPaymentTypeDto(), comment);
 
                     AccountPaymentParametersDto advanceLoanPayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), advanceLoanAccount, advanceLoanAccountPaymentAmount, paymentDate,
                             getPaymentTypeDto(), comment);
                     AccountPaymentParametersDto normalLoanpayment = new AccountPaymentParametersDto(
                             getUserReferenceDto(), normalLoanAccount, normalLoanAccountPaymentAmount, paymentDate,
                             getPaymentTypeDto(), comment);
                     AccountPaymentParametersDto savingsPayment = new AccountPaymentParametersDto(getUserReferenceDto(),
                             savingsAccount, savingsAccountPaymentAmount, paymentDate, getPaymentTypeDto(), comment);
 
                     List<InvalidPaymentReason> errors = getAccountService().validatePayment(
                             cumulativePaymentAdvanceLoan);
                     errors.addAll(getAccountService().validatePayment(cumulativePaymentNormalLoan));
                     errors.addAll(getAccountService().validatePayment(cumulativePaymentSavings));
 
                     checkPaymentErrors(errors, friendlyRowNum);
 
                     pmts.add(advanceLoanPayment);
                     pmts.add(normalLoanpayment);
                     pmts.add(savingsPayment);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace(System.err);
             errorsList.add(e + ". Input line number: " + friendlyRowNum);
         }
 
         return new ParseResultDto(errorsList, pmts);
     }
 
     private void checkBlank(String value, String name, int friendlyRowNum) {
         if (StringUtils.isBlank(value)) {
             errorsList.add(name + " could not be extracted from row " + friendlyRowNum);
         }
     }
 
     private void checkPaymentErrors(List<InvalidPaymentReason> errors, int friendlyRowNum) {
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
         }
 
     }
 
     private BigDecimal getTotalPaymentDueAmount(AccountReferenceDto advanceLoanAccount, int friendlyRowNum) {
         BigDecimal totalPaymentDue = null;
         try {
             totalPaymentDue = getAccountService().getTotalPaymentDueAmount(advanceLoanAccount);
         } catch (Exception e) {
             errorsList.add("Error getting total payment due for row " + friendlyRowNum + ": " + e.getMessage());
         }
         if (totalPaymentDue == null) {
             return BigDecimal.ZERO;
         }
         return totalPaymentDue;
     }
 
     private AccountReferenceDto getSavingsAccount(String governmentId, String savingsProductShortName,
             int friendlyRowNum) {
         AccountReferenceDto savingsAccount = null;
         try {
             savingsAccount = getAccountService()
                     .lookupSavingsAccountReferenceFromClientGovernmentIdAndSavingsProductShortName(governmentId,
                             savingsProductShortName);
         } catch (Exception e) {
             errorsList.add("Error looking up account from row " + friendlyRowNum + ": " + e.getMessage());
         }
         return savingsAccount;
     }
 
     private AccountReferenceDto getLoanAccount(String governmentId, String loanProductShortName, int friendlyRowNum) {
         AccountReferenceDto loanAccount = null;
         try {
             loanAccount = getAccountService().lookupLoanAccountReferenceFromClientGovernmentIdAndLoanProductShortName(
                     governmentId, loanProductShortName);
         } catch (Exception e) {
             errorsList.add("Error looking up account from row " + friendlyRowNum + ": " + e.getMessage());
         }
         return loanAccount;
     }
 
     private void skipReadingToTransactionData(Iterator<Row> rowIterator, Row row) {
         boolean skippingRowsBeforeTransactionData = true;
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
