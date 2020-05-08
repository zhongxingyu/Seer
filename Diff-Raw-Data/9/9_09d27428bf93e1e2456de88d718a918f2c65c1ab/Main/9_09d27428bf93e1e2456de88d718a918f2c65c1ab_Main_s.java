 /**
  * Copyright (c) 2012 Thiago Leão Moreira. All rights reserved.
  *
  * Licensed under the LGPL License, Version 3.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.gnu.org/licenses/gpl-3.0.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.ofx4j.domain.data.ApplicationSecurity;
 import net.sf.ofx4j.domain.data.ResponseEnvelope;
 import net.sf.ofx4j.domain.data.ResponseMessageSet;
 import net.sf.ofx4j.domain.data.common.Currency;
 import net.sf.ofx4j.domain.data.common.Status;
 import net.sf.ofx4j.domain.data.common.Transaction;
 import net.sf.ofx4j.domain.data.common.TransactionList;
 import net.sf.ofx4j.domain.data.common.TransactionType;
 import net.sf.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
 import net.sf.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
 import net.sf.ofx4j.domain.data.creditcard.CreditCardStatementResponse;
 import net.sf.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
 import net.sf.ofx4j.io.AggregateMarshaller;
 import net.sf.ofx4j.io.v1.OFXV1Writer;
 
 import org.apache.commons.io.IOUtils;
 
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
 		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");  
 		if (args != null) {
 			for (String arg : args) {
 				File input = new File(arg);
 				if (input.exists()) {
 					List<String> lines = IOUtils.readLines(new FileInputStream(input), "ISO-8859-1");
 
 					Date start = null;
 					Date end = null;
 					double x=0;
 					double y=0;
 					float exchangeRate = 0;
 					String accountKey = null;
 
 					List<Transaction> transactions = new ArrayList<Transaction>();
 
 					for (String line : lines) {
 						if (line.trim().length() != 0) {
 							Pattern pattern = Pattern.compile("(\\d\\d/\\d\\d/\\d\\d) (.{38}) ((\\w|\\s){3}) ((\\s)*-?(\\.|\\d)*,\\d\\d) ((\\s)*-?(\\.|\\d)*,\\d\\d)");
 							Matcher matcher = pattern.matcher(line);
 							while (matcher.find()) {
 								Date date = dateFormat.parse(matcher.group(1));
 								String description = matcher.group(2);
 								String amountInReals = matcher.group(5).trim();
 								String amountInDollars = matcher.group(8).trim();
 								String id = String.valueOf(Math.abs((amountInReals+description).hashCode()));
 
 								if (description.contains("PGTO DEBITO CONTA")) {
 									continue;
 								}
 
 								if (start == null) {
 									start = date;
 									end = date;
 								}
 
 								if (end.before(date)) {
 									end = date;
 								}
 
 								Transaction transaction = new Transaction();
 
 								transaction.setTransactionType(TransactionType.OTHER);
 								transaction.setDatePosted(date);
 								transaction.setId(id);
 								transaction.setCheckNumber(id);
 								transaction.setReferenceNumber(id);
 								double amount = formatter.parse(amountInReals).doubleValue();
 								x = x + amount;
 
 								if (amount == 0) {
 									amount = formatter.parse(amountInDollars).doubleValue();
 									transaction.setCurrency(new Currency());
 									y = y + amount;
 								}
 								transaction.setAmount(-1 * amount);
 								transaction.setMemo(description);
 
 								transactions.add(transaction);
 							}
							pattern = Pattern.compile("X\\s*(\\d+\\.\\d\\d\\d\\d) =");
 							matcher = pattern.matcher(line);
 							while (matcher.find()) {
 								exchangeRate = Float.parseFloat(matcher.group(1));
 							}
 							pattern = Pattern.compile("Modalidade      : (.{30})");
 							matcher = pattern.matcher(line);
 							while (matcher.find()) {
 								accountKey = matcher.group(1).trim();
 							}
 						}
 					}
 					for (Transaction transaction : transactions) {
 						Currency currency = transaction.getCurrency();
 						if (currency != null) {
 							transaction.setCurrency(null);
 							double temp = exchangeRate * transaction.getAmount();
 							transaction.setAmount(temp);
 						}
 					}
 
 					TransactionList transactionList = new TransactionList();
 					transactionList.setStart(start);
 					transactionList.setEnd(end);
 					transactionList.setTransactions(transactions);
 
 					CreditCardAccountDetails creditCardAccountDetails = new CreditCardAccountDetails();
 					creditCardAccountDetails.setAccountNumber("7616-3");
 					creditCardAccountDetails.setAccountKey(accountKey);
 							
 					CreditCardStatementResponse creditCardStatementResponse = new CreditCardStatementResponse();
 					creditCardStatementResponse.setAccount(creditCardAccountDetails);
 					creditCardStatementResponse.setCurrencyCode("BRL");
 					creditCardStatementResponse.setTransactionList(transactionList);
 
 					Status status = new Status();
 					status.setCode(Status.KnownCode.SUCCESS);
 					status.setSeverity(Status.Severity.INFO);
 
 					CreditCardStatementResponseTransaction statementResponse = new CreditCardStatementResponseTransaction();
 					statementResponse.setClientCookie(UUID.randomUUID().toString());
 					statementResponse.setStatus(status);
 					statementResponse.setUID(UUID.randomUUID().toString());
 					statementResponse.setMessage(creditCardStatementResponse);
 
 					CreditCardResponseMessageSet creditCardResponseMessageSet = new CreditCardResponseMessageSet();
 					creditCardResponseMessageSet.setStatementResponse(statementResponse);
 
 					SortedSet<ResponseMessageSet> messageSets = new TreeSet<ResponseMessageSet>();
 					messageSets.add(creditCardResponseMessageSet);
 
 					ResponseEnvelope envelope = new ResponseEnvelope();
 					envelope.setUID(UUID.randomUUID().toString());
 					envelope.setSecurity(ApplicationSecurity.NONE);
 					envelope.setMessageSets(messageSets);
 
 					System.out.println(creditCardAccountDetails.getAccountKey());
					System.out.println("TOTAL EM RS" + x);
					System.out.println("TOTAL EM US" + y);
					System.out.println("TOTAL EM US+RS" + (y*exchangeRate));
 					System.out.println();
 
 					if (!transactions.isEmpty()) {
 						String fileName = arg.replace(".txt", ".ofx");
 						File output = new File("target", fileName);
 						FileOutputStream fos = new FileOutputStream(output);
 
 						OFXV1Writer writer = new OFXV1Writer(fos);
 						writer.setWriteAttributesOnNewLine(true);
 
 						AggregateMarshaller marshaller = new AggregateMarshaller();
 						marshaller.setConversion(new MyFinanceStringConversion());
 						marshaller.marshal(envelope, writer);
 
 						writer.flush();
 						writer.close();
 					}
 				}
 			}
 		}
 
 	}
 
 }
